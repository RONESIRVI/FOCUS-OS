import sys

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    lines = f.readlines()

new_content = """        viewModelScope.launch(Dispatchers.IO) {
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
        }
"""

start_idx = -1
for i, line in enumerate(lines):
    if "viewModelScope.launch(Dispatchers.IO) {" in line and "val sessionId = repository.saveSession(session)" in lines[i+1]:
        start_idx = i
        break

end_idx = -1
if start_idx != -1:
    # Find the closing brace for this launch block
    # Note: it's around line 245
    for i in range(start_idx, len(lines)):
        if "    }" in lines[i] and "fun " in lines[i+2]: # hacky way to find the end of function
            end_idx = i + 1
            break

if start_idx != -1 and end_idx != -1:
    lines[start_idx:end_idx] = [new_content]
    with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
        f.writelines(lines)
    print("Success")
else:
    print(f"Failed to find indices. Start {start_idx}, End {end_idx}")

