package com.example.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class FocusScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getLongExtra("SESSION_ID", -1L)
        val sessionName = intent.getStringExtra("SESSION_NAME") ?: "Focus Session"
        val action = intent.action
        
        Log.d("FocusSchedule", "Alarm triggered for session $sessionId, action: $action")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "schedule_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Scheduled Sessions", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        
        if (action == "ACTION_PRE_SCHEDULE") {
            val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 15)
            val timeText = when (minutesBefore) {
                1 -> "1 minute"
                90 -> "1.5 hours"
                120 -> "2 hours"
                60 -> "1 hour"
                1440 -> "1 day"
                else -> "$minutesBefore minutes"
            }
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("⏰ Upcoming Focus Reminder")
                .setContentText("Your scheduled session '$sessionName' starts in $timeText. Get ready!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            notificationManager.notify((sessionId * 100).toInt() + minutesBefore, notification)
        } else if (action == "ACTION_EXACT_SCHEDULE") {
            com.example.util.FocusLockManager.setPendingSchedule(sessionId, sessionName)
            
            try {
                val serviceIntent = Intent(context, com.example.services.FocusTimerService::class.java).apply {
                    this.action = "ACTION_START_PENDING_MONITOR"
                    putExtra("SESSION_ID", sessionId)
                    putExtra("SESSION_NAME", sessionName)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("FocusSchedule", "Failed to start pending monitor service", e)
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("START_SESSION_ID", sessionId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                (sessionId * 10).toInt() + 2,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Time to Focus!")
                .setContentText("Your strict session '$sessionName' is starting now. Tap here to verify.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                
                .setContentIntent(pendingIntent)
                .build()
            notificationManager.notify((sessionId * 10).toInt() + 2, notification)
        }
    }
}
