package com.example.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "BootReceiver triggered with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.focusDao()
                    val allSessions = dao.getAllSessions().first()
                    val now = System.currentTimeMillis()

                    val upcomingSessions = allSessions.filter {
                        it.status == "SCHEDULED" && 
                        it.scheduledStartTime != null && 
                        it.scheduledStartTime > now
                    }

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    if (alarmManager != null) {
                        for (session in upcomingSessions) {
                            val scheduledStartTime = session.scheduledStartTime ?: continue
                            val earlyTime = scheduledStartTime - (2 * 60 * 1000L)

                            // 1. Early 2-min warning alarm
                            val earlyIntent = Intent(context, FocusScheduleReceiver::class.java).apply {
                                this.action = "ACTION_PRE_SCHEDULE"
                                putExtra("SESSION_ID", session.id)
                                putExtra("SESSION_NAME", session.sessionName)
                            }
                            val earlyPendingIntent = PendingIntent.getBroadcast(
                                context,
                                (session.id * 10).toInt() + 1,
                                earlyIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            // 2. Exact session start alarm
                            val exactIntent = Intent(context, FocusScheduleReceiver::class.java).apply {
                                this.action = "ACTION_EXACT_SCHEDULE"
                                putExtra("SESSION_ID", session.id)
                                putExtra("SESSION_NAME", session.sessionName)
                            }
                            val exactPendingIntent = PendingIntent.getBroadcast(
                                context,
                                (session.id * 10).toInt() + 2,
                                exactIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                    if (earlyTime > now) {
                                        alarmManager.set(AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                                    }
                                    alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
                                } else {
                                    if (earlyTime > now) {
                                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                                    }
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
                                }
                                Log.d(TAG, "Restored exact alarm for scheduled session: ${session.sessionName} (ID: ${session.id})")
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to schedule restored alarm for session ${session.id}", e)
                            }
                        }
                    }
                    Log.i(TAG, "Successfully restored ${upcomingSessions.size} scheduled alarms after reboot.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring alarms on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
