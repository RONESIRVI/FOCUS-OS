import re

with open("app/src/main/java/com/example/util/NotificationSoundVibrationHelper.kt", "r") as f:
    content = f.read()

# Replace ringtone playback logic with MediaPlayer fallback

old_code = """    private var currentRingtone: Ringtone? = null

    fun getNotificationSoundUri(context: Context, soundKey: String = "PRIME_SIREN"): Uri {"""

new_code = """    private var currentRingtone: Ringtone? = null
    private var currentMediaPlayer: android.media.MediaPlayer? = null

    fun getNotificationSoundUri(context: Context, soundKey: String = "PRIME_SIREN"): Uri {"""

content = content.replace(old_code, new_code)

old_play = """    fun playSoundKey(context: Context, soundKey: String) {
        if (soundKey == "SILENT") return
        try {
            stopCurrentSound()
            val uri = getNotificationSoundUri(context, soundKey)
            if (uri != Uri.EMPTY) {
                currentRingtone = RingtoneManager.getRingtone(context.applicationContext, uri)
                currentRingtone?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        it.audioAttributes = android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        it.isLooping = false
                    }
                    it.play()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopCurrentSound() {
        try {
            currentRingtone?.stop()
            currentRingtone = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""

new_play = """    fun playSoundKey(context: Context, soundKey: String) {
        if (soundKey == "SILENT") return
        try {
            stopCurrentSound()
            val uri = getNotificationSoundUri(context, soundKey)
            if (uri != Uri.EMPTY) {
                if (soundKey.startsWith("content://") && !soundKey.contains("media/internal/audio")) {
                    // Use MediaPlayer for SAF custom picked audio files
                    currentMediaPlayer = android.media.MediaPlayer().apply {
                        setDataSource(context.applicationContext, uri)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            val attrs = android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                            setAudioAttributes(attrs)
                        }
                        prepare()
                        start()
                    }
                } else {
                    currentRingtone = RingtoneManager.getRingtone(context.applicationContext, uri)
                    currentRingtone?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            it.audioAttributes = android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            it.isLooping = false
                        }
                        it.play()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopCurrentSound() {
        try {
            currentRingtone?.stop()
            currentRingtone = null
            
            currentMediaPlayer?.stop()
            currentMediaPlayer?.release()
            currentMediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""

content = content.replace(old_play, new_play)

with open("app/src/main/java/com/example/util/NotificationSoundVibrationHelper.kt", "w") as f:
    f.write(content)
