import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add audioFilePickerLauncher

old_launcher = """    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->"""

new_launcher = """    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                val key = uri.toString()
                val targetCat = activeSoundCategoryTarget
                if (targetCat != null) {
                    val prefKey = when (targetCat) {
                        "SCHEDULE" -> "NOTIF_SCHEDULE_SOUND"
                        "WARNING" -> "NOTIF_WARNING_SOUND"
                        "COMPLETE" -> "NOTIF_COMPLETE_SOUND"
                        "SOFTLOCK" -> "NOTIF_SOFTLOCK_SOUND"
                        else -> "NOTIF_SCHEDULE_SOUND"
                    }
                    when (targetCat) {
                        "SCHEDULE" -> notifScheduleSound = key
                        "WARNING" -> notifWarningSound = key
                        "COMPLETE" -> notifCompleteSound = key
                        "SOFTLOCK" -> notifSoftlockSound = key
                    }
                    sharedPrefs.edit().putString(prefKey, key).apply()
                    com.example.util.NotificationSoundVibrationHelper.triggerNotificationSoundAndVibration(context, key)
                }
            }
        }
    )

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->"""

content = content.replace(old_launcher, new_launcher)

# Add the button in the dialog
old_button = """                    Divider(color = FocusSurfaceVariant)
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
                    }"""

new_button = """                    Divider(color = FocusSurfaceVariant)
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
                            text = "System Notifications",
                            color = FocusPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Divider(color = FocusSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                audioFilePickerLauncher.launch(arrayOf("audio/*"))
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📁 Pick Audio File from Storage",
                            color = FocusPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }"""

content = content.replace(old_button, new_button)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
