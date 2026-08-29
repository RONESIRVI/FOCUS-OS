import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

old_list_rendering = """                    primeSounds.forEach { (key, label) ->
                        val isSelected = currentSelectedKey == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (targetCat) {
                                        "SCHEDULE" -> notifScheduleSound = key
                                        "WARNING" -> notifWarningSound = key
                                        "COMPLETE" -> notifCompleteSound = key
                                        "SOFTLOCK" -> notifSoftlockSound = key
                                    }
                                    sharedPrefs.edit().putString(prefKey, key).apply()
                                    com.example.util.NotificationSoundVibrationHelper.triggerNotificationSoundAndVibration(context, key)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    when (targetCat) {
                                        "SCHEDULE" -> notifScheduleSound = key
                                        "WARNING" -> notifWarningSound = key
                                        "COMPLETE" -> notifCompleteSound = key
                                        "SOFTLOCK" -> notifSoftlockSound = key
                                    }
                                    sharedPrefs.edit().putString(prefKey, key).apply()
                                    com.example.util.NotificationSoundVibrationHelper.triggerNotificationSoundAndVibration(context, key)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = FocusPrimary, unselectedColor = FocusTextSecondary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                color = if (isSelected) FocusPrimary else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }"""

new_list_rendering = """                    val isCustomSelected = !primeSounds.any { it.first == currentSelectedKey } && currentSelectedKey.isNotEmpty()
                    
                    if (isCustomSelected) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = true,
                                onClick = { com.example.util.NotificationSoundVibrationHelper.triggerNotificationSoundAndVibration(context, currentSelectedKey) },
                                colors = RadioButtonDefaults.colors(selectedColor = FocusPrimary, unselectedColor = FocusTextSecondary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "🎵 Custom Selected Audio",
                                color = FocusPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    primeSounds.forEach { (key, label) ->
                        val isSelected = currentSelectedKey == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (targetCat) {
                                        "SCHEDULE" -> notifScheduleSound = key
                                        "WARNING" -> notifWarningSound = key
                                        "COMPLETE" -> notifCompleteSound = key
                                        "SOFTLOCK" -> notifSoftlockSound = key
                                    }
                                    sharedPrefs.edit().putString(prefKey, key).apply()
                                    com.example.util.NotificationSoundVibrationHelper.triggerNotificationSoundAndVibration(context, key)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    when (targetCat) {
                                        "SCHEDULE" -> notifScheduleSound = key
                                        "WARNING" -> notifWarningSound = key
                                        "COMPLETE" -> notifCompleteSound = key
                                        "SOFTLOCK" -> notifSoftlockSound = key
                                    }
                                    sharedPrefs.edit().putString(prefKey, key).apply()
                                    com.example.util.NotificationSoundVibrationHelper.triggerNotificationSoundAndVibration(context, key)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = FocusPrimary, unselectedColor = FocusTextSecondary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                color = if (isSelected) FocusPrimary else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }"""

content = content.replace(old_list_rendering, new_list_rendering)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
