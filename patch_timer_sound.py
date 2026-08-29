import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

# 1. Add SoundType import if not present
if "import com.example.services.SoundType" not in content:
    content = content.replace("import com.example.services.TimerState", "import com.example.services.TimerState\nimport com.example.services.SoundType")

# 2. Add audioPickerLauncher inside FocusTimerScreen
launcher_code = """    var showEmergencyConfirm by remember { mutableStateOf(false) }

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
                viewModel.setSound(com.example.services.SoundType.CUSTOM_AUDIO)
            }
        }
    )"""
content = content.replace("    var showEmergencyConfirm by remember { mutableStateOf(false) }", launcher_code)

# 3. Add the UI section
ui_section = """            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Ambient Focus Sound Generator (for Active Session)
            Card(
                colors = CardDefaults.cardColors(containerColor = FocusSurface),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, FocusSurfaceVariant, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = FocusPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ambient Focus Sound",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SoundType.entries.forEach { st ->
                            val isSel = timerState.selectedSound == st
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSel) FocusPrimary.copy(alpha = 0.1f) else FocusBackground,
                                border = BorderStroke(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) FocusPrimary else FocusSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        if (st == SoundType.CUSTOM_AUDIO) {
                                            audioPickerLauncher.launch(arrayOf("audio/*"))
                                        } else {
                                            viewModel.setSound(st) 
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                if (isSel) FocusPrimary.copy(alpha = 0.2f) else FocusSurfaceVariant.copy(alpha = 0.4f),
                                                androidx.compose.foundation.shape.CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (st.isBinaural) Icons.Default.Headphones else Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (isSel) FocusPrimary else FocusTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = st.label,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSel) FocusPrimary else Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    RadioButton(
                                        selected = isSel,
                                        onClick = { 
                                            if (st == SoundType.CUSTOM_AUDIO) {
                                                audioPickerLauncher.launch(arrayOf("audio/*"))
                                            } else {
                                                viewModel.setSound(st) 
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = FocusPrimary,
                                            unselectedColor = FocusTextSecondary
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Controls"""

content = content.replace("            }\n\n            // Bottom Action Controls", ui_section)

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
