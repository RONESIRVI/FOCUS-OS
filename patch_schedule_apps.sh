sed -i 's/val scheduledId = _activeScheduledSessionId.value/val scheduledId = _activeScheduledSessionId.value\n        val sharedPrefs = context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)/g' app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt

sed -i 's/val allowedList = if (setup.lockMode == LockMode.STRICT_LOCK || setup.lockMode == LockMode.MAXIMUM_LOCK) {/val allowedList = if (scheduledId != null \&\& sharedPrefs.getString("scheduled_apps_$scheduledId", null) != null) {\n            sharedPrefs.getString("scheduled_apps_$scheduledId", "")!!.split(",").filter { it.isNotBlank() }\n        } else if (setup.lockMode == LockMode.STRICT_LOCK || setup.lockMode == LockMode.MAXIMUM_LOCK) {/g' app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt

