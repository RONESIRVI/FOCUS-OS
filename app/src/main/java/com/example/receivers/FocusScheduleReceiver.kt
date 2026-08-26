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
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Upcoming Focus Session")
                .setContentText("Your scheduled session '$sessionName' starts in 2 minutes. Get ready!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            notificationManager.notify((sessionId * 10).toInt() + 1, notification)
        } else if (action == "ACTION_EXACT_SCHEDULE") {
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
