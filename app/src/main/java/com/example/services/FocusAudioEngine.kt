package com.example.services

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SoundType(
    val label: String,
    val hindiTitle: String,
    val description: String,
    val isBinaural: Boolean,
    val badge: String
) {
    NONE(
        label = "Silent / Off",
        hindiTitle = "शांत मोड",
        description = "Pure silence with no background sound.",
        isBinaural = false,
        badge = "Silent"
    ),
    ALPHA_WAVES(
        label = "Alpha Waves (10 Hz)",
        hindiTitle = "मेमोरी और रिलैक्स्ड फोकस",
        description = "432Hz Carrier & 10Hz Binaural Beat. New concepts memorization & deep relaxed flow state.",
        isBinaural = true,
        badge = "Flow State • 10Hz"
    ),
    BETA_WAVES(
        label = "Beta Waves (16 Hz)",
        hindiTitle = "कठिन प्रॉब्लम सॉल्विंग",
        description = "250Hz Carrier & 16Hz Binaural Beat. High alertness, coding, math numericals & exam problem solving.",
        isBinaural = true,
        badge = "Deep Alert • 16Hz"
    ),
    BROWN_NOISE(
        label = "Brown Noise",
        hindiTitle = "भटकाव और ओवरथिंकिंग बंद",
        description = "Warm deep low-frequency rumble. Eliminates mind chatter, ADHD restlessness & background noise.",
        isBinaural = false,
        badge = "Deep Calming"
    ),
    PINK_NOISE(
        label = "Pink Noise",
        hindiTitle = "शांत और स्थिर एकाग्रता",
        description = "1/f Natural soothing spectrum. Promotes steady concentration and soothing study immersion.",
        isBinaural = false,
        badge = "Steady Focus"
    ),
    BAROQUE_60BPM(
        label = "60 BPM Baroque & Ambient",
        hindiTitle = "स्ट्रेस-फ्री कंसंट्रेशन",
        description = "60 Beats-per-minute harmonic progression. Synchronizes alpha brainwaves with resting heart rate.",
        isBinaural = false,
        badge = "60 BPM Harmony"
    ),
    RAIN_SOUNDS(
        label = "Deep Rain & Thunder",
        hindiTitle = "प्राकृतिक बारिश",
        description = "Gentle atmospheric rain patter & natural ambient resonance for cozy study environment.",
        isBinaural = false,
        badge = "Nature Rain"
    ),
    WHITE_NOISE(
        label = "White Noise",
        hindiTitle = "ब्रॉडबैंड नॉइज़",
        description = "Full equal-energy spectrum static masking all surrounding speech and sudden interruptions.",
        isBinaural = false,
        badge = "Sound Masking"
    )
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
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

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
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        isPlaying = true

        audioJob = scope.launch(Dispatchers.Default) {
            val buffer = ShortArray(bufferSize / 2) // Stereo: left, right pairs
            var sampleIndex = 0L

            // Noise filter state variables
            var brownL = 0f
            var brownR = 0f
            
            // Pink noise 3-pole filter state
            var b0L = 0f; var b1L = 0f; var b2L = 0f
            var b0R = 0f; var b1R = 0f; var b2R = 0f

            // Baroque 60 BPM chord progression frequencies (Hz)
            // 60 BPM = 1 chord per second (44,100 samples)
            val chord1 = floatArrayOf(293.66f, 349.23f, 440.00f, 587.33f) // D minor (D4, F4, A4, D5)
            val chord2 = floatArrayOf(261.63f, 329.63f, 392.00f, 523.25f) // C major (C4, E4, G4, C5)
            val chord3 = floatArrayOf(246.94f, 311.13f, 369.99f, 493.88f) // B dim / G7 (B3, D#4, F#4, B4)
            val chord4 = floatArrayOf(220.00f, 261.63f, 329.63f, 440.00f) // A minor (A3, C4, E4, A4)
            val chords = listOf(chord1, chord2, chord3, chord4)

            while (isActive && isPlaying) {
                var i = 0
                while (i < buffer.size - 1) {
                    val time = sampleIndex.toDouble() / sampleRate.toDouble()
                    var leftSample = 0f
                    var rightSample = 0f

                    when (soundType) {
                        SoundType.ALPHA_WAVES -> {
                            // 432 Hz carrier in left ear, 442 Hz in right ear = 10 Hz Alpha Binaural Beat
                            val leftCarrier = sin(2.0 * Math.PI * 432.0 * time).toFloat()
                            val rightCarrier = sin(2.0 * Math.PI * 442.0 * time).toFloat()
                            val subDrone = (0.25f * sin(2.0 * Math.PI * 108.0 * time).toFloat())
                            leftSample = (leftCarrier * 0.18f) + subDrone
                            rightSample = (rightCarrier * 0.18f) + subDrone
                        }
                        SoundType.BETA_WAVES -> {
                            // 250 Hz carrier in left ear, 266 Hz in right ear = 16 Hz Beta Binaural Beat
                            val leftCarrier = sin(2.0 * Math.PI * 250.0 * time).toFloat()
                            val rightCarrier = sin(2.0 * Math.PI * 266.0 * time).toFloat()
                            val softPulsar = (0.5f + 0.5f * sin(2.0 * Math.PI * 8.0 * time).toFloat())
                            leftSample = leftCarrier * (0.16f + 0.04f * softPulsar)
                            rightSample = rightCarrier * (0.16f + 0.04f * softPulsar)
                        }
                        SoundType.BROWN_NOISE -> {
                            // Red/Brownian deep integrated noise filter
                            val whiteL = Random.nextFloat() * 2f - 1f
                            val whiteR = Random.nextFloat() * 2f - 1f
                            brownL = (brownL * 0.96f) + (whiteL * 0.07f)
                            brownR = (brownR * 0.96f) + (whiteR * 0.07f)
                            leftSample = brownL.coerceIn(-1f, 1f) * 0.35f
                            rightSample = brownR.coerceIn(-1f, 1f) * 0.35f
                        }
                        SoundType.PINK_NOISE -> {
                            // Paul Kellet's 3-pole pink noise filter
                            val whiteL = Random.nextFloat() * 2f - 1f
                            val whiteR = Random.nextFloat() * 2f - 1f
                            b0L = 0.99765f * b0L + whiteL * 0.0990460f
                            b1L = 0.96300f * b1L + whiteL * 0.2965164f
                            b2L = 0.57000f * b2L + whiteL * 1.0526913f
                            val pinkL = b0L + b1L + b2L + whiteL * 0.1848f

                            b0R = 0.99765f * b0R + whiteR * 0.0990460f
                            b1R = 0.96300f * b1R + whiteR * 0.2965164f
                            b2R = 0.57000f * b2R + whiteR * 1.0526913f
                            val pinkR = b0R + b1R + b2R + whiteR * 0.1848f

                            leftSample = (pinkL * 0.05f).coerceIn(-1f, 1f)
                            rightSample = (pinkR * 0.05f).coerceIn(-1f, 1f)
                        }
                        SoundType.BAROQUE_60BPM -> {
                            // 60 BPM harmonic chord synthesizer with exponential pulse decay
                            val beatSample = (sampleIndex % 44100).toFloat()
                            val beatProgress = beatSample / 44100f // 0.0 to 1.0 per second
                            val chordIndex = ((sampleIndex / 44100) % chords.size).toInt()
                            val activeChord = chords[chordIndex]

                            // Gentle harmonic envelope: fast soft attack + exponential decay
                            val envelope = (1f - exp(-beatProgress * 20f)) * exp(-beatProgress * 2.8f)

                            var chordTone = 0f
                            for (freq in activeChord) {
                                chordTone += sin(2.0 * Math.PI * freq * time).toFloat()
                            }
                            chordTone = (chordTone / activeChord.size) * envelope * 0.22f

                            // Low calming ambient sub-pad
                            val ambientPad = 0.06f * sin(2.0 * Math.PI * 110.0 * time).toFloat()

                            leftSample = chordTone + ambientPad
                            rightSample = chordTone + ambientPad
                        }
                        SoundType.RAIN_SOUNDS -> {
                            // Soothing rain patter with background ambient wash
                            val whiteL = Random.nextFloat() * 2f - 1f
                            val whiteR = Random.nextFloat() * 2f - 1f
                            brownL = (brownL * 0.94f) + (whiteL * 0.06f)
                            brownR = (brownR * 0.94f) + (whiteR * 0.06f)

                            // Randomized raindrop spikes
                            val dropL = if (Random.nextFloat() > 0.985f) (Random.nextFloat() * 0.15f) else 0f
                            val dropR = if (Random.nextFloat() > 0.985f) (Random.nextFloat() * 0.15f) else 0f

                            leftSample = (brownL * 0.25f) + dropL
                            rightSample = (brownR * 0.25f) + dropR
                        }
                        SoundType.WHITE_NOISE -> {
                            val whiteL = (Random.nextFloat() * 2f - 1f) * 0.08f
                            val whiteR = (Random.nextFloat() * 2f - 1f) * 0.08f
                            leftSample = whiteL
                            rightSample = whiteR
                        }
                        SoundType.NONE -> {
                            leftSample = 0f
                            rightSample = 0f
                        }
                    }

                    buffer[i] = (leftSample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
                    buffer[i + 1] = (rightSample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
                    i += 2
                    sampleIndex++
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopSound() {
        isPlaying = false
        audioJob?.cancel()
        audioJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Track clean-up safe
        }
        audioTrack = null
    }
}
