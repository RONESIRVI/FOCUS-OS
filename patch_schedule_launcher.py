import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

# Add launcher
launcher_code = """    var selectedSound by remember { mutableStateOf(SoundType.NONE) }
    
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
content = content.replace("    var selectedSound by remember { mutableStateOf(SoundType.NONE) }", launcher_code)

# Add clickable condition
old_click = ".clickable { selectedSound = sound }"
new_click = """.clickable { 
                                            if (sound == com.example.services.SoundType.CUSTOM_AUDIO) {
                                                audioPickerLauncher.launch(arrayOf("audio/*"))
                                            } else {
                                                selectedSound = sound 
                                            }
                                        }"""
content = content.replace(old_click, new_click)

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
