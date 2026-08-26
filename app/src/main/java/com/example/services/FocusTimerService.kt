package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.LockMode
import com.example.util.FocusLockManager
import com.example.util.FocusLockOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val sessionName: String = "Study Session",
    val subjectName: String = "Deep Focus",
    val lockMode: LockMode = LockMode.MAXIMUM_LOCK,
    val distractionAttempts: Int = 0,
    val selectedSound: SoundType = SoundType.NONE
)

class FocusTimerService : Service() {

    private val binder = LocalBinder()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val audioEngine = FocusAudioEngine()

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var appMonitorJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): FocusTimerService = this@FocusTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE_RESUME -> {
                if (_timerState.value.isPaused) {
                    resumeTimer()
                } else {
                    pauseTimer()
                }
            }
            ACTION_ADD_TIME -> {
                val extraSeconds = intent.getIntExtra(EXTRA_ADD_SECONDS, 300)
                addExtraTime(extraSeconds)
            }
            ACTION_STOP -> {
                stopTimer()
            }
        }
        return START_STICKY
    }

    fun startTimer(
        durationMinutes: Int,
        sessionName: String,
        subjectName: String,
        lockMode: LockMode,
        soundType: SoundType
    ) {
        val totalSecs = durationMinutes * 60
        _timerState.value = TimerState(
            isRunning = true,
            isPaused = false,
            remainingSeconds = totalSecs,
            totalSeconds = totalSecs,
            sessionName = sessionName,
            subjectName = subjectName,
            lockMode = lockMode,
            selectedSound = soundType
        )

        audioEngine.startSound(soundType, scope)
        startForeground(NOTIFICATION_ID, buildNotification())
        runCountdown()
        startAppLockMonitoring()
    }

    private fun startAppLockMonitoring() {
        appMonitorJob?.cancel()
        appMonitorJob = scope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? android.app.usage.UsageStatsManager
            while (_timerState.value.isRunning) {
                try {
                    if (FocusLockManager.isFocusActive && !_timerState.value.isPaused) {
                        val endTime = System.currentTimeMillis()
                        val startTime = endTime - 8000
                        var lastEventPackage: String? = null
                        
                        val events = usageStatsManager?.queryEvents(startTime, endTime)
                        if (events != null) {
                            val event = android.app.usage.UsageEvents.Event()
                            while (events.hasNextEvent()) {
                                events.getNextEvent(event)
                                if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                                    event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND
                                ) {
                                    lastEventPackage = event.packageName
                                }
                            }
                        }
                        
                        // Fallback check if events were not captured
                        if (lastEventPackage == null) {
                            val stats = usageStatsManager?.queryUsageStats(
                                android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                                endTime - 8000,
                                endTime
                            )
                            if (!stats.isNullOrEmpty()) {
                                val mostRecent = stats.maxByOrNull { it.lastTimeUsed }
                                if (mostRecent != null && (endTime - mostRecent.lastTimeUsed) < 4000) {
                                    lastEventPackage = mostRecent.packageName
                                }
                            }
                        }

                        if (lastEventPackage != null && !FocusLockManager.isPackageAllowed(lastEventPackage, packageName)) {
                            recordDistractionAttempt()
                            FocusLockManager.handleBlockedAppOpened(
                                context = this@FocusTimerService,
                                blockedPackageName = lastEventPackage,
                                remainingSeconds = _timerState.value.remainingSeconds,
                                subjectName = _timerState.value.subjectName
                            )
                        } else if (lastEventPackage == packageName) {
                            // Returned to our app -> dismiss overlay
                            FocusLockOverlayManager.dismissOverlay()
                        }
                    }
                } catch (e: Exception) {
                    // Ignore query failures
                }
                delay(300)
            }
        }
    }

    private fun runCountdown() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                if (!_timerState.value.isPaused) {
                    delay(1000)
                    val newRemaining = _timerState.value.remainingSeconds - 1
                    _timerState.value = _timerState.value.copy(remainingSeconds = newRemaining)
                    updateNotification()
                } else {
                    delay(500)
                }
            }
            if (_timerState.value.remainingSeconds <= 0 && _timerState.value.isRunning) {
                // Timer complete
                _timerState.value = _timerState.value.copy(isRunning = false)
                audioEngine.stopSound()
                appMonitorJob?.cancel()
                FocusLockOverlayManager.dismissOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isPaused = true)
        updateNotification()
    }

    fun resumeTimer() {
        _timerState.value = _timerState.value.copy(isPaused = false)
        updateNotification()
    }

    fun addExtraTime(seconds: Int = 300) {
        val currentRem = _timerState.value.remainingSeconds
        val currentTot = _timerState.value.totalSeconds
        _timerState.value = _timerState.value.copy(
            remainingSeconds = currentRem + seconds,
            totalSeconds = currentTot + seconds
        )
        updateNotification()
    }

    fun recordDistractionAttempt() {
        val current = _timerState.value.distractionAttempts
        _timerState.value = _timerState.value.copy(distractionAttempts = current + 1)
        updateNotification()
    }

    fun stopTimer() {
        timerJob?.cancel()
        appMonitorJob?.cancel()
        audioEngine.stopSound()
        FocusLockOverlayManager.dismissOverlay()
        _timerState.value = TimerState(isRunning = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun setSound(soundType: SoundType) {
        _timerState.value = _timerState.value.copy(selectedSound = soundType)
        audioEngine.startSound(soundType, scope)
        updateNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Session Active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing focus countdown timer, lock status and quick controls"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val state = _timerState.value
        val formattedTime = formatTimeText(state.remainingSeconds)
        val totalSec = if (state.totalSeconds > 0) state.totalSeconds else 1
        val completedSec = (state.totalSeconds - state.remainingSeconds).coerceAtLeast(0)
        val progressPercent = ((completedSec.toFloat() / totalSec.toFloat()) * 100).toInt().coerceIn(0, 100)

        val subjectTitle = if (state.subjectName.isNotBlank()) state.subjectName else "DEEP STUDY"
        val sessionTitle = if (state.sessionName.isNotBlank()) state.sessionName else "Focus Session"

        // 1. Collapsed Notification View
        val collapsedView = RemoteViews(packageName, R.layout.notification_focus_collapsed).apply {
            setTextViewText(R.id.notif_title, "FOCUS OS")
            setTextViewText(R.id.notif_mode_badge, state.lockMode.title.uppercase())
            setTextViewText(R.id.notif_subject_text, "$subjectTitle • $sessionTitle")
            setTextViewText(
                R.id.notif_timer_text,
                if (state.isPaused) "⏸️ PAUSED • $formattedTime" else "⏱️ $formattedTime Remaining ($progressPercent%)"
            )
            setProgressBar(R.id.notif_progress_bar, 100, progressPercent, false)
            
            // Toggle icon
            setImageViewResource(
                R.id.notif_btn_quick_toggle,
                if (state.isPaused) R.drawable.ic_notif_play else R.drawable.ic_notif_pause
            )
            setOnClickPendingIntent(
                R.id.notif_btn_quick_toggle,
                createServiceActionPendingIntent(ACTION_PAUSE_RESUME, 101)
            )
        }

        // 2. Expanded Rich Notification View
        val expandedView = RemoteViews(packageName, R.layout.notification_focus_expanded).apply {
            setTextViewText(R.id.notif_exp_app_title, "FOCUS OS")
            setTextViewText(R.id.notif_exp_mode_badge, state.lockMode.title.uppercase())
            setTextViewText(R.id.notif_exp_distraction_badge, "🛡️ ${state.distractionAttempts} Blocked")
            setTextViewText(R.id.notif_exp_subject, "📚 $subjectTitle • $sessionTitle")
            setTextViewText(R.id.notif_exp_timer, formattedTime)
            setTextViewText(
                R.id.notif_exp_status_label,
                if (state.isPaused) "PAUSED • TAP RESUME" else "REMAINING FOCUS TIME"
            )
            setProgressBar(R.id.notif_exp_progress_bar, 100, progressPercent, false)
            setTextViewText(R.id.notif_exp_progress_percent, "$progressPercent% Completed")

            val soundTitle = if (state.selectedSound != SoundType.NONE) state.selectedSound.label else "Silent Mode"
            setTextViewText(R.id.notif_exp_sound_info, "🎵 $soundTitle")

            // Quick Action 1: Pause / Resume Button
            setImageViewResource(
                R.id.notif_btn_pause_resume_icon,
                if (state.isPaused) R.drawable.ic_notif_play else R.drawable.ic_notif_pause
            )
            setTextViewText(
                R.id.notif_btn_pause_resume_text,
                if (state.isPaused) "RESUME" else "PAUSE"
            )
            setOnClickPendingIntent(
                R.id.notif_btn_pause_resume,
                createServiceActionPendingIntent(ACTION_PAUSE_RESUME, 102)
            )

            // Quick Action 2: +5 Mins Button
            setOnClickPendingIntent(
                R.id.notif_btn_add_time,
                createServiceActionPendingIntent(ACTION_ADD_TIME, 103)
            )

            // Quick Action 3: Open App Button
            setOnClickPendingIntent(
                R.id.notif_btn_open_app,
                createPendingIntent()
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_shield)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(createPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification() {
        if (!_timerState.value.isRunning) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun createServiceActionPendingIntent(actionStr: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, FocusTimerService::class.java).apply {
            action = actionStr
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTimeText(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    companion object {
        const val CHANNEL_ID = "focus_os_timer_channel"
        const val NOTIFICATION_ID = 8801

        const val ACTION_PAUSE_RESUME = "com.example.services.ACTION_PAUSE_RESUME"
        const val ACTION_ADD_TIME = "com.example.services.ACTION_ADD_TIME"
        const val ACTION_STOP = "com.example.services.ACTION_STOP"
        const val EXTRA_ADD_SECONDS = "extra_add_seconds"
    }
}
