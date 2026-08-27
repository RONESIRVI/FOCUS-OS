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

                    val sharedPrefs = context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)
                    for (session in upcomingSessions) {
                        val scheduledStartTime = session.scheduledStartTime ?: continue
                        val offsetsStr = sharedPrefs.getString("reminder_offsets_${session.id}", "15") ?: "15"
                        val offsets = offsetsStr.split(",").mapNotNull { it.trim().toIntOrNull() }

                        com.example.util.AlarmScheduler.scheduleSessionAlarms(
                            context = context,
                            sessionId = session.id,
                            sessionTitle = session.sessionName,
                            scheduledStartTime = scheduledStartTime,
                            reminderMinutesList = offsets
                        )
                        Log.d(TAG, "Restored alarms for scheduled session: ${session.sessionName} (ID: ${session.id})")
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
