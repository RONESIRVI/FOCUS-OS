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
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.LockMode
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
    val subjectName: String = "UPSC GS",
    val lockMode: LockMode = LockMode.STRICT_LOCK,
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

    inner class LocalBinder : Binder() {
        fun getService(): FocusTimerService = this@FocusTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
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

    fun recordDistractionAttempt() {
        val current = _timerState.value.distractionAttempts
        _timerState.value = _timerState.value.copy(distractionAttempts = current + 1)
    }

    fun stopTimer() {
        timerJob?.cancel()
        audioEngine.stopSound()
        _timerState.value = TimerState(isRunning = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun setSound(soundType: SoundType) {
        _timerState.value = _timerState.value.copy(selectedSound = soundType)
        audioEngine.startSound(soundType, scope)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Session Active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing focus countdown timer and lock status"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val title = if (_timerState.value.subjectName.isNotBlank()) {
            "FOCUS OS — ${_timerState.value.subjectName}"
        } else {
            "FOCUS OS — Active Session"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(formatTimeText(_timerState.value.remainingSeconds) + " Remaining • " + _timerState.value.lockMode.title)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(createPendingIntent())
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
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
    }
}
