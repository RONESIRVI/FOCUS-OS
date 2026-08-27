package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.receivers.FocusScheduleReceiver

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun schedulePreciseAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerTimeMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val now = System.currentTimeMillis()
        if (triggerTimeMillis <= now) {
            Log.w(TAG, "Skipping alarm setup because trigger time $triggerTimeMillis is in the past (now: $now)")
            return
        }

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val clockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, showPendingIntent)
                alarmManager.setAlarmClock(clockInfo, pendingIntent)
                Log.d(TAG, "Scheduled AlarmClock for timestamp: $triggerTimeMillis")
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                Log.d(TAG, "Scheduled Exact RTC_WAKEUP for timestamp: $triggerTimeMillis")
            }
        } catch (e: Exception) {
            Log.e(TAG, "setAlarmClock failed, trying setExactAndAllowWhileIdle fallback", e)
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "setExactAndAllowWhileIdle failed, falling back to set", e2)
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
        }
    }

    fun scheduleSessionAlarms(
        context: Context,
        sessionId: Long,
        sessionTitle: String,
        scheduledStartTime: Long,
        reminderMinutesList: List<Int>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        // 1. Pre-schedule reminders
        reminderMinutesList.forEachIndexed { index, minsBefore ->
            val reminderTime = scheduledStartTime - (minsBefore * 60 * 1000L)
            if (reminderTime > now) {
                val earlyIntent = Intent(context, FocusScheduleReceiver::class.java).apply {
                    action = "ACTION_PRE_SCHEDULE"
                    putExtra("SESSION_ID", sessionId)
                    putExtra("SESSION_NAME", sessionTitle)
                    putExtra("MINUTES_BEFORE", minsBefore)
                }
                val earlyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (sessionId * 100).toInt() + index + 1,
                    earlyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                schedulePreciseAlarm(context, alarmManager, reminderTime, earlyPendingIntent)
            }
        }

        // 2. Exact session start time notification
        if (scheduledStartTime > now) {
            val exactIntent = Intent(context, FocusScheduleReceiver::class.java).apply {
                action = "ACTION_EXACT_SCHEDULE"
                putExtra("SESSION_ID", sessionId)
                putExtra("SESSION_NAME", sessionTitle)
            }
            val exactPendingIntent = PendingIntent.getBroadcast(
                context,
                (sessionId * 10).toInt() + 2,
                exactIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            schedulePreciseAlarm(context, alarmManager, scheduledStartTime, exactPendingIntent)
        }
    }

    fun cancelSessionAlarms(
        context: Context,
        sessionId: Long,
        reminderMinutesList: List<Int> = listOf(1, 15, 30, 60, 90, 120, 1440)
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Cancel all pre-schedule reminder alarms
        reminderMinutesList.forEachIndexed { index, _ ->
            val earlyIntent = Intent(context, FocusScheduleReceiver::class.java).apply {
                action = "ACTION_PRE_SCHEDULE"
            }
            val earlyPendingIntent = PendingIntent.getBroadcast(
                context,
                (sessionId * 100).toInt() + index + 1,
                earlyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(earlyPendingIntent)
        }

        // Also cancel legacy request codes (1 to 10)
        (1..10).forEach { legacyIdx ->
            val legacyIntent = Intent(context, FocusScheduleReceiver::class.java).apply {
                action = "ACTION_PRE_SCHEDULE"
            }
            val legacyPendingIntent = PendingIntent.getBroadcast(
                context,
                (sessionId * 10).toInt() + legacyIdx,
                legacyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(legacyPendingIntent)
        }

        // Cancel exact start time alarm
        val exactIntent = Intent(context, FocusScheduleReceiver::class.java).apply {
            action = "ACTION_EXACT_SCHEDULE"
        }
        val exactPendingIntent = PendingIntent.getBroadcast(
            context,
            (sessionId * 10).toInt() + 2,
            exactIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(exactPendingIntent)
    }
}
