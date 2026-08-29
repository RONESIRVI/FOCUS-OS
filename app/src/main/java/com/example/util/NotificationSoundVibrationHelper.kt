package com.example.util

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R

object NotificationSoundVibrationHelper {

    private var currentRingtone: Ringtone? = null

    fun getNotificationSoundUri(context: Context, soundKey: String = "PRIME_SIREN"): Uri {
        if (soundKey.startsWith("content://")) {
            return Uri.parse(soundKey)
        }
        val resId = when (soundKey) {
            "PRIME_QUANTUM" -> R.raw.prime_quantum_pulse
            "PRIME_ZEN" -> R.raw.prime_zen_chime
            "PRIME_STROBE" -> R.raw.prime_strobe_alarm
            "SILENT" -> return Uri.EMPTY
            else -> R.raw.notification_sound // "PRIME_SIREN" or default
        }
        return Uri.parse("android.resource://" + context.packageName + "/" + resId)
    }

    fun playSoundKey(context: Context, soundKey: String) {
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
    }

    fun triggerNotificationSoundAndVibration(context: Context, soundKey: String = "PRIME_SIREN", patternKey: String? = null) {
        val prefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE)
        val vibratePatternKey = patternKey ?: prefs.getString("NOTIF_VIBRATE_PATTERN", "PULSE") ?: "PULSE"
        if (soundKey != "SILENT") {
            playSoundKey(context, soundKey)
        }
        triggerVibration(context, vibratePatternKey)
    }

    fun triggerVibration(context: Context, patternKey: String? = null) {
        try {
            val prefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE)
            val selectedKey = patternKey ?: prefs.getString("NOTIF_VIBRATE_PATTERN", "PULSE") ?: "PULSE"
            if (selectedKey == "OFF") return

            val vibratePattern = when (selectedKey) {
                "DOUBLE_PULSE" -> longArrayOf(0, 250, 100, 250, 400, 250, 100, 250)
                "RHYTHM" -> longArrayOf(0, 120, 100, 120, 100, 120, 100, 300)
                "PULSE" -> longArrayOf(0, 400, 200, 400, 200, 600)
                else -> longArrayOf(0, 400, 200, 400)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    val effect = VibrationEffect.createWaveform(vibratePattern, -1)
                    vibrator.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val effect = VibrationEffect.createWaveform(vibratePattern, -1)
                        vibrator.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(vibratePattern, -1)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
