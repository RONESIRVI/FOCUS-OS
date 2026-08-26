import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

old_alarm_code = """            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
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
            }"""

new_alarm_code = """            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    // Fallback to inexact alarms
                    if (earlyTime > System.currentTimeMillis()) {
                        alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                    }
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
                } else {
                    if (earlyTime > System.currentTimeMillis()) {
                        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                    }
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
                }
            } catch (e: Exception) {
                // Fallback to inexact alarms in case of SecurityException
                if (earlyTime > System.currentTimeMillis()) {
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, earlyTime, earlyPendingIntent)
                }
                alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, scheduledStartTime, exactPendingIntent)
            }"""

content = content.replace(old_alarm_code, new_alarm_code)

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
