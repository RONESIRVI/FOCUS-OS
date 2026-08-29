import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

launcher_code = """    var customGoal by remember { mutableStateOf(setup.sessionName) }
    
    val audioPickerLauncher = androidx.compose.activity.compose.rememberLauncherForActivityResult(
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
                viewModel.updateSetup(soundType = com.example.services.SoundType.CUSTOM_AUDIO)
            }
        }
    )"""

content = content.replace("    var customGoal by remember { mutableStateOf(setup.sessionName) }", launcher_code)

old_click = """.clickable { viewModel.updateSetup(soundType = st) }"""
new_click = """.clickable { 
                                                    if (st == com.example.services.SoundType.CUSTOM_AUDIO) {
                                                        audioPickerLauncher.launch(arrayOf("audio/*"))
                                                    } else {
                                                        viewModel.updateSetup(soundType = st) 
                                                    }
                                                }"""

content = content.replace(old_click, new_click)

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(content)
