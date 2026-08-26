package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.MainActivity

class FocusScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getLongExtra("SESSION_ID", -1L)
        Log.d("FocusSchedule", "Alarm triggered for session $sessionId")
        
        // Launch MainActivity directly to show the Start Photo / Session Active screen
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("START_SESSION_ID", sessionId)
        }
        context.startActivity(launchIntent)
    }
}
