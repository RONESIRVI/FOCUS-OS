package com.example.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

enum class SoundType(val label: String, val isBinaural: Boolean) {
    NONE("Silent Mode", false),
    CUSTOM_AUDIO("Upload Audio / MP3 File", false)
}

class FocusAudioEngine {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    fun startSound(soundType: SoundType, scope: CoroutineScope, context: Context? = null) {
        stopSound()
        if (soundType == SoundType.NONE) return

        if (soundType == SoundType.CUSTOM_AUDIO && context != null) {
            val prefs = context.getSharedPreferences("FocusPrefs", Context.MODE_PRIVATE)
            val customAudioUriStr = prefs.getString("AMBIENT_CUSTOM_AUDIO_URI", null)
            
            if (!customAudioUriStr.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(customAudioUriStr)
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(context.applicationContext, uri)
                        val attrs = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                        setAudioAttributes(attrs)
                        isLooping = true
                        prepare()
                        start()
                    }
                    isPlaying = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopSound() {
        isPlaying = false
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
    }
}
