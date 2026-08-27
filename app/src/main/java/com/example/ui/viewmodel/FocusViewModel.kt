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
import com.example.util.FocusLockManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

data class UiSessionSetup(
    val sessionName: String = "",
    val subjectName: String = "",
    val durationMinutes: Int = 25,
    val lockMode: LockMode = LockMode.MAXIMUM_LOCK,
    val selectedSound: SoundType = SoundType.NONE,
    val scheduledStartTime: Long? = null,
    val scheduledEndTime: Long? = null,
    val requiresPhoto: Boolean = true,
    val requiresSelfie: Boolean = true,
    val startPhotoUri: String? = null,
    val endSelfieUri: String? = null
)

data class StudySummaryStats(
    val todayFocusSeconds: Int = 0,
    val currentStreakDays: Int = 0,
    val totalFocusHours: Float = 0f,
    val focusScore: Int = 100,
    val totalSessions: Int = 0,
    val dailyGoalSeconds: Int = 5 * 3600 // 5 hours default goal
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FocusRepository(db.focusDao())

    val allSessions: StateFlow<List<FocusSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledSessions: StateFlow<List<FocusSession>> = repository.scheduledSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allowedAppsManual: StateFlow<List<AllowedApp>> = repository.allowedApps("MANUAL")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allowedAppsStrict: StateFlow<List<AllowedApp>> = repository.allowedApps("STRICT")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedAppsManual: StateFlow<List<AllowedApp>> = repository.whitelistedApps("MANUAL")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whitelistedAppsStrict: StateFlow<List<AllowedApp>> = repository.whitelistedApps("STRICT")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectTask>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _setupState = MutableStateFlow(UiSessionSetup())
    
    private val _dismissedNotificationIds = MutableStateFlow<Set<String>>(emptySet())
    val dismissedNotificationIds: StateFlow<Set<String>> = _dismissedNotificationIds
    
    fun dismissNotification(id: String) {
        _dismissedNotificationIds.value = _dismissedNotificationIds.value + id
    }
    val setupState: StateFlow<UiSessionSetup> = _setupState.asStateFlow()

    private val _serviceTimerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _serviceTimerState.asStateFlow()

    private val _showLockOverlay = MutableStateFlow(false)
    val showLockOverlay: StateFlow<Boolean> = _showLockOverlay.asStateFlow()

    private val _lastBlockedPackage = MutableStateFlow<String?>(null)
    val lastBlockedPackage: StateFlow<String?> = _lastBlockedPackage.asStateFlow()

    private val _currentAppSelectorProfile = MutableStateFlow("MANUAL")
    val currentAppSelectorProfile: StateFlow<String> = _currentAppSelectorProfile.asStateFlow()

    fun setAppSelectorProfile(profile: String) {
        _currentAppSelectorProfile.value = profile
    }

    val summaryStats: StateFlow<StudySummaryStats> = repository.allSessions.map { sessions ->
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        
        var todaySec = 0
        var totalSec = 0
        var totalDistractions = 0
        val completedSessions = sessions.filter { it.completedDurationSeconds > 0 || it.status == "COMPLETED" || it.status == "ARCHIVED" }
        
        completedSessions.forEach {
            totalSec += it.completedDurationSeconds
            if (it.timestamp >= todayStart) {
                todaySec += it.completedDurationSeconds
                totalDistractions += it.distractionAttempts
            }
        }

        // Real Streak Calculation: consecutive days with study activity
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val activeDates = completedSessions.map { sdf.format(java.util.Date(it.timestamp)) }.toSet()

        val cal = java.util.Calendar.getInstance()
        val todayStr = sdf.format(cal.time)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        var streak = 0
        val checkCal = java.util.Calendar.getInstance()

        if (activeDates.contains(todayStr)) {
            while (activeDates.contains(sdf.format(checkCal.time))) {
                streak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
        } else if (activeDates.contains(yesterdayStr)) {
            checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            while (activeDates.contains(sdf.format(checkCal.time))) {
                streak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
        }
        
        StudySummaryStats(
            todayFocusSeconds = todaySec,
            currentStreakDays = streak,
            totalFocusHours = totalSec / 3600f,
            totalSessions = completedSessions.size,
            focusScore = maxOf(60, 100 - (totalDistractions * 5)),
            dailyGoalSeconds = 5 * 3600
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudySummaryStats())

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
        val sharedPrefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
        val intent = Intent(context, FocusTimerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeServiceTimer() {
        timerService?.let { service ->
            viewModelScope.launch {
                service.timerState.collect { state ->
                    _serviceTimerState.value = state
                    syncFocusLockState(state)
                }
            }
        }
    }

    private fun syncFocusLockState(state: TimerState) {
        val allowedPackages = if (state.lockMode == LockMode.MAXIMUM_LOCK) {
            whitelistedAppsStrict.value.filter { it.isAllowed }.map { it.packageName }
        } else {
            whitelistedAppsManual.value.filter { it.isAllowed }.map { it.packageName }
        }
        FocusLockManager.updateFocusState(
            isActive = state.isRunning,
            lockMode = state.lockMode,
            allowedPackageNames = allowedPackages
        )
    }

    private val _activeScheduledSessionId = MutableStateFlow<Long?>(null)
    val activeScheduledSessionId: StateFlow<Long?> = _activeScheduledSessionId.asStateFlow()

    fun loadScheduledSession(sessionId: Long) {
        _activeScheduledSessionId.value = sessionId
        viewModelScope.launch {
            val sessions = repository.allSessions.first()
            val session = sessions.find { it.id == sessionId }
            if (session != null) {
                _setupState.value = _setupState.value.copy(
                    sessionName = session.sessionName,
                    subjectName = session.subjectName,
                    durationMinutes = session.targetDurationMinutes,
                    lockMode = try { LockMode.valueOf(session.lockMode) } catch (e: Exception) { LockMode.MAXIMUM_LOCK },
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
        requiresSelfie: Boolean? = null,
        startPhotoUri: String? = null,
        endSelfieUri: String? = null
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
            requiresSelfie = requiresSelfie ?: _setupState.value.requiresSelfie,
            startPhotoUri = startPhotoUri ?: _setupState.value.startPhotoUri,
            endSelfieUri = endSelfieUri ?: _setupState.value.endSelfieUri
        )
    }

    fun setStartPhotoUri(uri: String) {
        _setupState.value = _setupState.value.copy(startPhotoUri = uri)
    }

    fun setEndSelfieUri(uri: String) {
        _setupState.value = _setupState.value.copy(endSelfieUri = uri)
    }

    fun startFocusSession() {
        val setup = _setupState.value
        val context = getApplication<Application>()
        val sharedPrefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
        val intent = Intent(context, FocusTimerService::class.java)

        val scheduledId = _activeScheduledSessionId.value
        
        if (scheduledId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val sessions = repository.allSessions.first()
                val session = sessions.find { it.id == scheduledId }
                if (session != null) {
                    repository.updateSession(session.copy(status = "ACTIVE"))
                }
            }
        }

        // Immediately update FocusLockManager allowed packages
        val allowedList = if (scheduledId != null && sharedPrefs.getString("scheduled_apps_$scheduledId", null) != null) {
            sharedPrefs.getString("scheduled_apps_$scheduledId", "")!!.split(",").filter { it.isNotBlank() }
        } else if (setup.lockMode == LockMode.MAXIMUM_LOCK) {
            whitelistedAppsStrict.value.filter { it.isAllowed }.map { it.packageName }
        } else {
            whitelistedAppsManual.value.filter { it.isAllowed }.map { it.packageName }
        }
        FocusLockManager.updateFocusState(
            isActive = true,
            lockMode = setup.lockMode,
            allowedPackageNames = allowedList
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        val name = if (setup.sessionName.isNotBlank()) setup.sessionName else "Deep Focus"
        val subject = if (setup.subjectName.isNotBlank()) setup.subjectName else name

        timerService?.startTimer(
            durationMinutes = setup.durationMinutes,
            sessionName = name,
            subjectName = subject,
            lockMode = setup.lockMode,
            soundType = setup.selectedSound
        )
    }

    fun scheduleFocusSession(
        hour: Int,
        minute: Int,
        targetYear: Int? = null,
        targetMonth: Int? = null,
        targetDayOfMonth: Int? = null
    ) {
        val setup = _setupState.value
        val context = getApplication<Application>()
        val sharedPrefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
        
        val calendar = java.util.Calendar.getInstance().apply {
            if (targetYear != null && targetMonth != null && targetDayOfMonth != null) {
                set(java.util.Calendar.YEAR, targetYear)
                set(java.util.Calendar.MONTH, targetMonth)
                set(java.util.Calendar.DAY_OF_MONTH, targetDayOfMonth)
            }
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        if (targetYear == null && calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        val scheduledStartTime = calendar.timeInMillis
        val scheduledEndTime = scheduledStartTime + (setup.durationMinutes * 60 * 1000L)
        val sessionTitle = if (setup.sessionName.isNotBlank()) setup.sessionName else "Focus Study Session"
        val subjectTitle = if (setup.subjectName.isNotBlank()) setup.subjectName else sessionTitle
        
        val session = com.example.data.model.FocusSession(
            sessionName = sessionTitle,
            subjectName = subjectTitle,
            targetDurationMinutes = setup.durationMinutes,
            completedDurationSeconds = 0,
            lockMode = setup.lockMode.name,
            status = "SCHEDULED",
            scheduledStartTime = scheduledStartTime,
            scheduledEndTime = scheduledEndTime,
            requiresPhoto = setup.requiresPhoto,
            requiresSelfie = setup.requiresSelfie,
            timestamp = System.currentTimeMillis()
        )
        
        viewModelScope.launch(Dispatchers.IO) {
            val sessionId = repository.saveSession(session)
            // Save fixed allowed apps for this schedule
            val currentAllowed = if (setup.lockMode == LockMode.MAXIMUM_LOCK) whitelistedAppsStrict.value.filter { it.isAllowed }.map { it.packageName } else whitelistedAppsManual.value.filter { it.isAllowed }.map { it.packageName }
            context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE).edit().putString("scheduled_apps_$sessionId", currentAllowed.joinToString(",")).apply()
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            
            // Alarm 1: 2 minutes early notification
            val earlyIntent = Intent(context, com.example.receivers.FocusScheduleReceiver::class.java).apply {
                action = "ACTION_PRE_SCHEDULE"
                putExtra("SESSION_ID", sessionId)
                putExtra("SESSION_NAME", sessionTitle)
            }
            val earlyPendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                (sessionId * 10).toInt() + 1,
                earlyIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val earlyTime = scheduledStartTime - (2 * 60 * 1000)
            
            // Alarm 2: Exact start time notification
            val exactIntent = Intent(context, com.example.receivers.FocusScheduleReceiver::class.java).apply {
                action = "ACTION_EXACT_SCHEDULE"
                putExtra("SESSION_ID", sessionId)
                putExtra("SESSION_NAME", sessionTitle)
            }
            val exactPendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                (sessionId * 10).toInt() + 2,
                exactIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    if (earlyTime > System.currentTimeMillis()) {
                        alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                    }
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
                } else {
                    if (earlyTime > System.currentTimeMillis()) {
                        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                    }
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
                }
            } catch (e: Exception) {
                if (earlyTime > System.currentTimeMillis()) {
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                }
                alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
            }
            
            launch(Dispatchers.Main) {
                val formatter = java.text.SimpleDateFormat("EEE, d MMM • h:mm a", java.util.Locale.getDefault())
                val timeStr = formatter.format(java.util.Date(scheduledStartTime))
                val diffMins = ((scheduledStartTime - System.currentTimeMillis()) / 60000).coerceAtLeast(0)
                val diffHours = diffMins / 60
                val durationText = if (diffHours >= 24) "${diffHours / 24}d ${diffHours % 24}h" else "${diffMins}m"
                android.widget.Toast.makeText(context, "✅ Strict Focus scheduled for $timeStr (in $durationText)", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteScheduledSession(session: FocusSession) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
        val sharedPrefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            
            if (alarmManager != null) {
                val earlyIntent = Intent(context, com.example.receivers.FocusScheduleReceiver::class.java).apply {
                    action = "ACTION_PRE_SCHEDULE"
                }
                val earlyPendingIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    (session.id * 10).toInt() + 1,
                    earlyIntent,
                    android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                if (earlyPendingIntent != null) {
                    alarmManager.cancel(earlyPendingIntent)
                }

                val exactIntent = Intent(context, com.example.receivers.FocusScheduleReceiver::class.java).apply {
                    action = "ACTION_EXACT_SCHEDULE"
                }
                val exactPendingIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    (session.id * 10).toInt() + 2,
                    exactIntent,
                    android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                if (exactPendingIntent != null) {
                    alarmManager.cancel(exactPendingIntent)
                }
            }

            if (session.status == "COMPLETED" || session.completedDurationSeconds > 0) {
                repository.updateSession(session.copy(status = "ARCHIVED"))
            } else {
                repository.deleteSession(session)
            }
        }
    }

    fun pauseSession() {
        timerService?.pauseTimer()
    }

    fun resumeSession() {
        timerService?.resumeTimer()
    }

    fun triggerDistractionWarning(blockedPackage: String = "", showRedModal: Boolean = false) {
        if (_serviceTimerState.value.isRunning) {
            if (blockedPackage.isNotBlank()) {
                _lastBlockedPackage.value = blockedPackage
            }
            if (showRedModal) {
                _showLockOverlay.value = true
            }
        }
    }

    fun getAppDisplayName(packageName: String): String {
        return try {
            val pm = getApplication<Application>().packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
        }
    }

    fun dismissLockOverlay() {
        _showLockOverlay.value = false
    }

    fun addPenaltyTime(seconds: Int) {
        timerService?.addExtraTime(seconds)
    }

    fun completeFocusSession() {
        val current = _serviceTimerState.value
        val scheduledId = _activeScheduledSessionId.value
        
        val setup = _setupState.value
        viewModelScope.launch(Dispatchers.IO) {
            if (current.totalSeconds > 0) {
                val completedSecs = current.totalSeconds - current.remainingSeconds
                if (scheduledId != null) {
                    val sessions = repository.allSessions.first()
                    val existing = sessions.find { it.id == scheduledId }
                    if (existing != null) {
                        repository.updateSession(
                            existing.copy(
                                status = "COMPLETED",
                                completedDurationSeconds = completedSecs,
                                distractionAttempts = current.distractionAttempts,
                                lockMode = current.lockMode.name,
                                startPhotoUri = setup.startPhotoUri ?: existing.startPhotoUri,
                                endSelfieUri = setup.endSelfieUri ?: existing.endSelfieUri
                            )
                        )
                    } else {
                        val session = FocusSession(
                            sessionName = current.sessionName,
                            subjectName = current.subjectName,
                            targetDurationMinutes = current.totalSeconds / 60,
                            completedDurationSeconds = completedSecs,
                            lockMode = current.lockMode.name,
                            distractionAttempts = current.distractionAttempts,
                            allowedAppsCount = whitelistedAppsManual.value.size,
                            status = "COMPLETED",
                            startPhotoUri = setup.startPhotoUri,
                            endSelfieUri = setup.endSelfieUri
                        )
                        repository.saveSession(session)
                    }
                } else {
                    val session = FocusSession(
                        sessionName = current.sessionName,
                        subjectName = current.subjectName,
                        targetDurationMinutes = current.totalSeconds / 60,
                        completedDurationSeconds = completedSecs,
                        lockMode = current.lockMode.name,
                        distractionAttempts = current.distractionAttempts,
                        allowedAppsCount = whitelistedAppsManual.value.size,
                        status = "COMPLETED",
                        startPhotoUri = setup.startPhotoUri,
                        endSelfieUri = setup.endSelfieUri
                    )
                    repository.saveSession(session)
                }
            }
            _activeScheduledSessionId.value = null
            timerService?.stopTimer()
            _showLockOverlay.value = false
            FocusLockManager.updateFocusState(false, LockMode.NORMAL, emptyList())
        }
    }

    fun emergencyExitSession() {
        completeFocusSession()
    }

    fun toggleAppAllowed(packageName: String, isAllowed: Boolean, profile: String = "MANUAL") {
        viewModelScope.launch {
            repository.toggleAppWhitelist(packageName, isAllowed, profile)
            syncFocusLockState(_serviceTimerState.value)
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
