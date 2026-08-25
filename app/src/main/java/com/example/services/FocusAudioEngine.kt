package com.example.services

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class SoundType(val label: String) {
    NONE("None"),
    WHITE_NOISE("White Noise"),
    RAIN_SOUNDS("Deep Rain"),
    ALPHA_WAVES("Alpha 10Hz Focus")
}

class FocusAudioEngine {
    private var audioTrack: AudioTrack? = null
    private var audioJob: Job? = null
    private var isPlaying = false

    fun startSound(soundType: SoundType, scope: CoroutineScope) {
        stopSound()
        if (soundType == SoundType.NONE) return

        val sampleRate = 44100
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        isPlaying = true

        audioJob = scope.launch(Dispatchers.Default) {
            val buffer = ShortArray(minBufferSize)
            var sampleIndex = 0L

            while (isActive && isPlaying) {
                for (i in buffer.indices) {
                    when (soundType) {
                        SoundType.WHITE_NOISE -> {
                            val sample = (Random.nextFloat() * 2f - 1f) * 0.15f
                            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                        }
                        SoundType.RAIN_SOUNDS -> {
                            // Filtered noise simulating rain patter
                            val r = Random.nextFloat()
                            val p = if (r > 0.98f) 0.4f else 0.08f
                            val sample = (r * 2f - 1f) * p
                            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                        }
                        SoundType.ALPHA_WAVES -> {
                            // 200 Hz carrier modulated at 10 Hz alpha wave
                            val time = sampleIndex / sampleRate.toDouble()
                            val carrier = sin(2.0 * Math.PI * 200.0 * time)
                            val mod = 0.5 + 0.5 * sin(2.0 * Math.PI * 10.0 * time)
                            val sample = carrier * mod * 0.2
                            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                            sampleIndex++
                        }
                        SoundType.NONE -> {
                            buffer[i] = 0
                        }
                    }
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopSound() {
        isPlaying = false
        audioJob?.cancel()
        audioJob = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
