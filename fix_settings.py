import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

target = """                }
            },
            confirmButton = {
                TextButton(onClick = {
                    com.example.util.NotificationSoundVibrationHelper.stopCurrentSound()
                    activeSoundCategoryTarget = null
                }) {
                    Text("Done", color = FocusPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = FocusSurface
        )
    }"""

replacement = """                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider(color = FocusSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = android.content.Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER)
                                intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_NOTIFICATION)
                                intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                intent.putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                ringtonePickerLauncher.launch(intent)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.PhoneIphone, contentDescription = null, tint = FocusPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pick Sound from Phone",
                            color = FocusPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Divider(color = FocusSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            com.example.util.NotificationSoundVibrationHelper.stopCurrentSound()
                            activeSoundCategoryTarget = null
                        }) {
                            Text("Done", color = FocusPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            containerColor = FocusSurface
        )
    }"""

if target in content:
    content = content.replace(target, replacement)
    print("Success")
else:
    print("Failed")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

