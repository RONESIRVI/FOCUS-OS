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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

data class UiSessionSetup(
    val sessionName: String = "",
    val subjectName: String = "",
    val durationMinutes: Int = 25,
    val lockMode: LockMode = LockMode.STRICT_LOCK,
    val selectedSound: SoundType = SoundType.NONE,
    val scheduledStartTime: Long? = null,
    val scheduledEndTime: Long? = null,
    val requiresPhoto: Boolean = true,
    val requiresSelfie: Boolean = true
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

    val scheduledSessions: StateFlow<List<FocusSession>> = repository.scheduledSessions
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

    fun loadScheduledSession(sessionId: Long) {
        viewModelScope.launch {
            val sessions = repository.allSessions.first()
            val session = sessions.find { it.id == sessionId }
            if (session != null) {
                _setupState.value = _setupState.value.copy(
                    sessionName = session.sessionName,
                    subjectName = session.subjectName,
                    durationMinutes = session.targetDurationMinutes,
                    lockMode = LockMode.valueOf(session.lockMode),
                    scheduledStartTime = session.scheduledStartTime,
                    scheduledEndTime = session.scheduledEndTime,
                    requiresPhoto = session.requiresPhoto,
                    requiresSelfie = session.requiresSelfie
                )
            }
        }
    }

    fun updateSetup(
        sessionName: String? = null,
        subjectName: String? = null,
        durationMinutes: Int? = null,
        lockMode: LockMode? = null,
        soundType: SoundType? = null,
        scheduledStartTime: Long? = null,
        scheduledEndTime: Long? = null,
        requiresPhoto: Boolean? = null,
        requiresSelfie: Boolean? = null
    ) {
        _setupState.value = _setupState.value.copy(
            sessionName = sessionName ?: _setupState.value.sessionName,
            subjectName = subjectName ?: _setupState.value.subjectName,
            durationMinutes = durationMinutes ?: _setupState.value.durationMinutes,
            lockMode = lockMode ?: _setupState.value.lockMode,
            selectedSound = soundType ?: _setupState.value.selectedSound,
            scheduledStartTime = scheduledStartTime ?: _setupState.value.scheduledStartTime,
            scheduledEndTime = scheduledEndTime ?: _setupState.value.scheduledEndTime,
            requiresPhoto = requiresPhoto ?: _setupState.value.requiresPhoto,
            requiresSelfie = requiresSelfie ?: _setupState.value.requiresSelfie
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

    fun scheduleFocusSession(hour: Int, minute: Int) {
        val setup = _setupState.value
        val context = getApplication<Application>()
        
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }
        
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        val scheduledStartTime = calendar.timeInMillis
        val scheduledEndTime = scheduledStartTime + (setup.durationMinutes * 60 * 1000L)
        
        val session = com.example.data.model.FocusSession(
            sessionName = setup.sessionName,
            subjectName = setup.subjectName,
            targetDurationMinutes = setup.durationMinutes,
            completedDurationSeconds = 0,
            lockMode = setup.lockMode.name,
            status = "SCHEDULED",
            scheduledStartTime = scheduledStartTime,
            scheduledEndTime = scheduledEndTime,
            requiresPhoto = true,
            requiresSelfie = true,
            timestamp = System.currentTimeMillis()
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            val sessionId = repository.saveSession(session)
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, com.example.receivers.FocusScheduleReceiver::class.java).apply {
                putExtra("SESSION_ID", sessionId)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                sessionId.toInt(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        scheduledStartTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    scheduledStartTime,
                    pendingIntent
                )
            }
        }
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

    fun deleteCustomSubject(subject: SubjectTask) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
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
