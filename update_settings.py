import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add ringtonePickerLauncher
ringtone_launcher = """
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val uri: Uri? = result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                uri?.let {
                    val key = it.toString()
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
        }
    )
"""

content = content.replace("val photoPickerLauncher = rememberLauncherForActivityResult(", ringtone_launcher + "\n    val photoPickerLauncher = rememberLauncherForActivityResult(")

# Add "Pick from Phone" option
# We need to change primeSounds to also have a way to display custom sounds.
# Or just add a button at the end of the prime sounds list.

button_code = """
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = FocusSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
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
                        verticalAlignment = Alignment.CenterVertically
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
"""

content = content.replace("                }\n            },\n            confirmButton = {\n                TextButton(onClick = {", button_code + "                }\n            },\n            confirmButton = {\n                TextButton(onClick = {")


with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

print("done")
