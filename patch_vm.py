import sys

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

replacement = """        viewModelScope.launch(Dispatchers.IO) {
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
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    scheduledStartTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
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

old_target = """        viewModelScope.launch(Dispatchers.IO) {
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
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    scheduledStartTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    scheduledStartTime,
                    pendingIntent
                )
            }
        }"""

if old_target in content:
    content = content.replace(old_target, replacement)
    with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
