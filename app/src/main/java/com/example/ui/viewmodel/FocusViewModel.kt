package com.example.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AllowedApp
import com.example.data.model.FocusSession
import com.example.data.model.LockMode
import com.example.data.model.SubjectTask
import com.example.data.repository.FocusRepository
import com.example.services.FocusTimerService
import com.example.services.SoundType
import com.example.services.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiSessionSetup(
    val sessionName: String = "UPSC GS STUDY",
    val subjectName: String = "UPSC GS STUDY",
    val durationMinutes: Int = 25,
    val lockMode: LockMode = LockMode.STRICT_LOCK,
    val selectedSound: SoundType = SoundType.NONE
)

data class StudySummaryStats(
    val todayFocusSeconds: Int = 0,
    val currentStreakDays: Int = 0,
    val totalFocusHours: Float = 0f,
    val focusScore: Int = 0,
    val totalSessions: Int = 0
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FocusRepository(db.focusDao())

    val allSessions: StateFlow<List<FocusSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allowedApps: StateFlow<List<AllowedApp>> = repository.allowedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedApps: StateFlow<List<AllowedApp>> = repository.whitelistedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectTask>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _setupState = MutableStateFlow(UiSessionSetup())
    val setupState: StateFlow<UiSessionSetup> = _setupState.asStateFlow()

    private val _serviceTimerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _serviceTimerState.asStateFlow()

    private val _showLockOverlay = MutableStateFlow(false)
    val showLockOverlay: StateFlow<Boolean> = _showLockOverlay.asStateFlow()

    private val _summaryStats = MutableStateFlow(StudySummaryStats())
    val summaryStats: StateFlow<StudySummaryStats> = _summaryStats.asStateFlow()

    private var timerService: FocusTimerService? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as FocusTimerService.LocalBinder
            timerService = binder.getService()
            isBound = true
            observeServiceTimer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isBound = false
        }
    }

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty(application)
        }
        bindTimerService()
    }

    private fun bindTimerService() {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusTimerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeServiceTimer() {
        timerService?.let { service ->
            viewModelScope.launch {
                service.timerState.collect { state ->
                    _serviceTimerState.value = state
                }
            }
        }
    }

    fun updateSetup(
        sessionName: String? = null,
        subjectName: String? = null,
        durationMinutes: Int? = null,
        lockMode: LockMode? = null,
        soundType: SoundType? = null
    ) {
        _setupState.value = _setupState.value.copy(
            sessionName = sessionName ?: _setupState.value.sessionName,
            subjectName = subjectName ?: _setupState.value.subjectName,
            durationMinutes = durationMinutes ?: _setupState.value.durationMinutes,
            lockMode = lockMode ?: _setupState.value.lockMode,
            selectedSound = soundType ?: _setupState.value.selectedSound
        )
    }

    fun startFocusSession() {
        val setup = _setupState.value
        val context = getApplication<Application>()
        val intent = Intent(context, FocusTimerService::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        timerService?.startTimer(
            durationMinutes = setup.durationMinutes,
            sessionName = setup.sessionName,
            subjectName = setup.subjectName,
            lockMode = setup.lockMode,
            soundType = setup.selectedSound
        )
    }

    fun pauseSession() {
        timerService?.pauseTimer()
    }

    fun resumeSession() {
        timerService?.resumeTimer()
    }

    fun triggerDistractionWarning() {
        if (_serviceTimerState.value.isRunning) {
            timerService?.recordDistractionAttempt()
            if (_serviceTimerState.value.lockMode != LockMode.NORMAL) {
                _showLockOverlay.value = true
            }
        }
    }

    fun dismissLockOverlay() {
        _showLockOverlay.value = false
    }

    fun completeFocusSession() {
        val current = _serviceTimerState.value
        viewModelScope.launch {
            if (current.totalSeconds > 0) {
                val completedSecs = current.totalSeconds - current.remainingSeconds
                val session = FocusSession(
                    sessionName = current.sessionName,
                    subjectName = current.subjectName,
                    targetDurationMinutes = current.totalSeconds / 60,
                    completedDurationSeconds = completedSecs,
                    lockMode = current.lockMode.name,
                    distractionAttempts = current.distractionAttempts,
                    allowedAppsCount = whitelistedApps.value.size
                )
                repository.saveSession(session)

                // Update summary statistics
                val updatedToday = _summaryStats.value.todayFocusSeconds + completedSecs
                val newScore = maxOf(60, 100 - (current.distractionAttempts * 5))
                _summaryStats.value = _summaryStats.value.copy(
                    todayFocusSeconds = updatedToday,
                    totalSessions = _summaryStats.value.totalSessions + 1,
                    focusScore = newScore
                )
            }
            timerService?.stopTimer()
            _showLockOverlay.value = false
        }
    }

    fun emergencyExitSession() {
        timerService?.stopTimer()
        _showLockOverlay.value = false
    }

    fun toggleAppAllowed(packageName: String, isAllowed: Boolean) {
        viewModelScope.launch {
            repository.toggleAppWhitelist(packageName, isAllowed)
        }
    }

    fun setSound(soundType: SoundType) {
        updateSetup(soundType = soundType)
        timerService?.setSound(soundType)
    }

    fun addCustomSubject(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.addSubject(name, colorHex)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isBound = false
        }
    }
}
