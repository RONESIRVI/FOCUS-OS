sed -i 's/val context = getApplication<Application>()/val context = getApplication<Application>()\n        val sharedPrefs = context.getSharedPreferences("schedule_prefs", android.content.Context.MODE_PRIVATE)/g' app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt

sed -i 's/val sharedPrefs = context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)//g' app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt

