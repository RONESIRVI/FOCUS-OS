import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

old_schedule = """        viewModelScope.launch(Dispatchers.IO) {
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
            
            launch(Dispatchers.Main) {
                val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                val timeStr = formatter.format(java.util.Date(scheduledStartTime))
                android.widget.Toast.makeText(context, "✅ Strict Focus scheduled for $timeStr", android.widget.Toast.LENGTH_LONG).show()
            }
        }"""

new_schedule = """        viewModelScope.launch(Dispatchers.IO) {
            val sessionId = repository.saveSession(session)
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            
            // Alarm 1: 2 minutes early
            val earlyIntent = Intent(context, com.example.receivers.FocusScheduleReceiver::class.java).apply {
                action = "ACTION_PRE_SCHEDULE"
                putExtra("SESSION_ID", sessionId)
                putExtra("SESSION_NAME", setup.sessionName)
            }
            val earlyPendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                (sessionId * 10).toInt() + 1,
                earlyIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val earlyTime = scheduledStartTime - (2 * 60 * 1000)
            
            // Alarm 2: Exact time
            val exactIntent = Intent(context, com.example.receivers.FocusScheduleReceiver::class.java).apply {
                action = "ACTION_EXACT_SCHEDULE"
                putExtra("SESSION_ID", sessionId)
                putExtra("SESSION_NAME", setup.sessionName)
            }
            val exactPendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                (sessionId * 10).toInt() + 2,
                exactIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    if (earlyTime > System.currentTimeMillis()) {
                        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                    }
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
                }
            } else {
                if (earlyTime > System.currentTimeMillis()) {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                }
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
            }
            
            launch(Dispatchers.Main) {
                val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                val timeStr = formatter.format(java.util.Date(scheduledStartTime))
                android.widget.Toast.makeText(context, "✅ Strict Focus scheduled for $timeStr", android.widget.Toast.LENGTH_LONG).show()
            }
        }"""

content = content.replace(old_schedule, new_schedule)

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
