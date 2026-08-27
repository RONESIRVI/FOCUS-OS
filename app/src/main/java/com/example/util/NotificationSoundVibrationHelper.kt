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

    fun triggerNotificationSoundAndVibration(context: Context, soundKey: String = "PRIME_SIREN") {
        if (soundKey != "SILENT") {
            playSoundKey(context, soundKey)
        }
        triggerVibration(context)
    }

    fun triggerVibration(context: Context, patternKey: String = "PULSE") {
        try {
            val vibratePattern = when (patternKey) {
                "DOUBLE_PULSE" -> longArrayOf(0, 200, 100, 200)
                "RHYTHM" -> longArrayOf(0, 100, 100, 100, 100, 200)
                "OFF" -> return
                else -> longArrayOf(0, 350, 150, 350, 200, 500)
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
