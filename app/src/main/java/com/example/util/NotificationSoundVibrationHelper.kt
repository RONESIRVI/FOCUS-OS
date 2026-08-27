package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R

object NotificationSoundVibrationHelper {

    fun getNotificationSoundUri(context: Context): Uri {
        return Uri.parse("android.resource://" + context.packageName + "/" + R.raw.notification_sound)
    }

    fun triggerNotificationSoundAndVibration(context: Context) {
        // 1. Play exclusively attached raw audio sound
        try {
            val soundUri = getNotificationSoundUri(context)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
            ringtone?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    it.isLooping = false
                }
                it.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Mobile Vibration System
        triggerVibration(context)
    }

    fun triggerVibration(context: Context) {
        try {
            val vibratePattern = longArrayOf(0, 350, 150, 350, 200, 500)
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
