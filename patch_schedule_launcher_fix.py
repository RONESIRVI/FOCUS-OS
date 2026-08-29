import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

launcher_code = """    var selectedSound by remember { mutableStateOf(setup.selectedSound) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val audioPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri: android.net.Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val prefs = context.getSharedPreferences("FocusPrefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putString("AMBIENT_CUSTOM_AUDIO_URI", uri.toString()).apply()
                selectedSound = com.example.services.SoundType.CUSTOM_AUDIO
            }
        }
    )"""

content = content.replace("    var selectedSound by remember { mutableStateOf(setup.selectedSound) }", launcher_code)

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
