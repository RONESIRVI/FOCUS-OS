package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class NotificationSoundItem(
    val key: String,
    val brand: String,
    val brandEmoji: String,
    val title: String,
    val description: String,
    val badge: String? = null
)

object NotificationSoundVibrationHelper {

    private var currentRingtone: Ringtone? = null
    private var currentMediaPlayer: MediaPlayer? = null
    private var currentAudioTrack: AudioTrack? = null

    val NOTIFICATION_SOUNDS_CATALOG: List<NotificationSoundItem> = listOf(
        // 1. Apple iPhone
        NotificationSoundItem(
            key = "IPHONE_TRITONE",
            brand = "Apple iPhone (iOS)",
            brandEmoji = "🍎",
            title = "iPhone Classic Tri-Tone",
            description = "Iconic 3-note G#-B-E bell chime",
            badge = "CLASSIC"
        ),
        NotificationSoundItem(
            key = "IPHONE_NOTE",
            brand = "Apple iPhone (iOS)",
            brandEmoji = "🍎",
            title = "iPhone Note (Glass Bell)",
            description = "Crisp and clear high-frequency D6 glass note",
            badge = "POPULAR"
        ),
        NotificationSoundItem(
            key = "IPHONE_POPCORN",
            brand = "Apple iPhone (iOS)",
            brandEmoji = "🍎",
            title = "iPhone Popcorn",
            description = "Playful triple popping bubble drops",
            badge = "PLAYFUL"
        ),
        NotificationSoundItem(
            key = "IPHONE_AURORA",
            brand = "Apple iPhone (iOS)",
            brandEmoji = "🍎",
            title = "iPhone Aurora",
            description = "Lush multi-harmonic ambient chime",
            badge = "AMBIENT"
        ),
        NotificationSoundItem(
            key = "IPHONE_CHORD",
            brand = "Apple iPhone (iOS)",
            brandEmoji = "🍎",
            title = "iPhone Harmonic Chord",
            description = "Warm resonant major 7th chord chime",
            badge = "CHORD"
        ),
        NotificationSoundItem(
            key = "IPHONE_BAMBOO",
            brand = "Apple iPhone (iOS)",
            brandEmoji = "🍎",
            title = "iPhone Bamboo",
            description = "Resonant wooden marimba strike",
            badge = "ORGANIC"
        ),

        // 2. Motorola (Moto)
        NotificationSoundItem(
            key = "MOTO_HELLO",
            brand = "Motorola (Moto)",
            brandEmoji = "📱",
            title = "Motorola 'Hello Moto'",
            description = "Famous 4-note signature melody (C-E-G-C)",
            badge = "ICONIC"
        ),
        NotificationSoundItem(
            key = "MOTO_PURE",
            brand = "Motorola (Moto)",
            brandEmoji = "📱",
            title = "Moto Pure Crystal Drop",
            description = "Clean metallic downward pulse",
            badge = "PURE"
        ),
        NotificationSoundItem(
            key = "MOTO_SPACELINE",
            brand = "Motorola (Moto)",
            brandEmoji = "📱",
            title = "Moto Modern Spaceline",
            description = "Futuristic double blip alert tone",
            badge = "MOTO"
        ),
        NotificationSoundItem(
            key = "MOTO_DROPDOWN",
            brand = "Motorola (Moto)",
            brandEmoji = "📱",
            title = "Moto Melodic Sweep",
            description = "Smooth descending 3-note chime",
            badge = "SMOOTH"
        ),

        // 3. Samsung Galaxy
        NotificationSoundItem(
            key = "SAMSUNG_HORIZON",
            brand = "Samsung Galaxy",
            brandEmoji = "🌟",
            title = "Samsung Over the Horizon",
            description = "Signature Galaxy 6-note orchestral motif",
            badge = "FLAGSHIP"
        ),
        NotificationSoundItem(
            key = "SAMSUNG_SPACELINE",
            brand = "Samsung Galaxy",
            brandEmoji = "🌟",
            title = "Samsung Galaxy Spaceline",
            description = "Crisp dual crystal bell chime",
            badge = "GALAXY"
        ),
        NotificationSoundItem(
            key = "SAMSUNG_HARP",
            brand = "Samsung Galaxy",
            brandEmoji = "🌟",
            title = "Samsung Horizon Harp",
            description = "Gentle arpeggiated harp melody",
            badge = "HARP"
        ),
        NotificationSoundItem(
            key = "SAMSUNG_WHISTLE",
            brand = "Samsung Galaxy",
            brandEmoji = "🌟",
            title = "Samsung Iconic Whistle",
            description = "Bright rising melodic whistle glide",
            badge = "WHISTLE"
        ),

        // 4. Google Pixel
        NotificationSoundItem(
            key = "PIXEL_EUREKA",
            brand = "Google Pixel",
            brandEmoji = "🔵",
            title = "Google Pixel Eureka",
            description = "Modern 2-tone melodic bubble chime",
            badge = "PIXEL"
        ),
        NotificationSoundItem(
            key = "PIXEL_HEY",
            brand = "Google Pixel",
            brandEmoji = "🔵",
            title = "Google Pixel Hey Bell",
            description = "Warm 3-note ascending marimba alert",
            badge = "MATERIAL"
        ),
        NotificationSoundItem(
            key = "PIXEL_POP",
            brand = "Google Pixel",
            brandEmoji = "🔵",
            title = "Google Pixel Pop",
            description = "Deep resonant acoustic pop blip",
            badge = "POP"
        ),

        // 5. OnePlus & Xiaomi
        NotificationSoundItem(
            key = "ONEPLUS_RHYTHM",
            brand = "OnePlus (OxygenOS)",
            brandEmoji = "🔴",
            title = "OnePlus Oxygen Rhythm",
            description = "Punchy energetic modern synth chime",
            badge = "OXYGEN"
        ),
        NotificationSoundItem(
            key = "ONEPLUS_MEET",
            brand = "OnePlus (OxygenOS)",
            brandEmoji = "🔴",
            title = "OnePlus Meet Alert",
            description = "Smooth descending 3-note melodic wave",
            badge = "ONEPLUS"
        ),
        NotificationSoundItem(
            key = "XIAOMI_DROP",
            brand = "Xiaomi (MIUI/HyperOS)",
            brandEmoji = "🟠",
            title = "Xiaomi Water Droplet",
            description = "Acoustic physical liquid drop ripple",
            badge = "HYPEROS"
        ),
        NotificationSoundItem(
            key = "XIAOMI_NATURE",
            brand = "Xiaomi (MIUI/HyperOS)",
            brandEmoji = "🟠",
            title = "Xiaomi Nature Woodblock",
            description = "Earthy organic acoustic block strike",
            badge = "NATURE"
        ),

        // 6. Nokia Classic
        NotificationSoundItem(
            key = "NOKIA_SPECIAL",
            brand = "Nokia Retro",
            brandEmoji = "☎️",
            title = "Nokia Special SMS Tune",
            description = "Iconic retro 8-bit SMS notification melody",
            badge = "RETRO"
        ),

        // 7. Prime Focus Alert Tones
        NotificationSoundItem(
            key = "PRIME_SIREN",
            brand = "Prime Focus Suite",
            brandEmoji = "📢",
            title = "Prime Dual Frequency Siren",
            description = "High-urgency security focus siren alert",
            badge = "PRIME"
        ),
        NotificationSoundItem(
            key = "PRIME_QUANTUM",
            brand = "Prime Focus Suite",
            brandEmoji = "⚡",
            title = "Prime Quantum Pulse Chime",
            description = "Clean modern synthetic focus pulse",
            badge = "PRIME"
        ),
        NotificationSoundItem(
            key = "PRIME_ZEN",
            brand = "Prime Focus Suite",
            brandEmoji = "🔮",
            title = "Prime Zen Solfeggio 528Hz Bell",
            description = "Harmonic peaceful crystal meditation bowl",
            badge = "ZEN"
        ),
        NotificationSoundItem(
            key = "PRIME_STROBE",
            brand = "Prime Focus Suite",
            brandEmoji = "🚨",
            title = "Prime High Strobe Warning Beep",
            description = "Urgent high-pitch distraction security beep",
            badge = "SECURITY"
        ),

        // 8. Device Native & Silent
        NotificationSoundItem(
            key = "SYSTEM_DEFAULT",
            brand = "Device Default",
            brandEmoji = "📱",
            title = "System Default Ringtone",
            description = "Device's active system notification sound",
            badge = "SYSTEM"
        ),
        NotificationSoundItem(
            key = "SILENT",
            brand = "Silent Mode",
            brandEmoji = "🔇",
            title = "Silent (Mute Sound)",
            description = "Vibration only, no audible tone",
            badge = "MUTE"
        )
    )

    fun getNotificationSoundUri(context: Context, soundKey: String = "PRIME_SIREN"): Uri {
        if (soundKey.startsWith("content://")) {
            return Uri.parse(soundKey)
        }
        if (soundKey == "SYSTEM_DEFAULT") {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: Uri.EMPTY
        }
        if (soundKey == "SILENT") {
            return Uri.EMPTY
        }
        val resId = when (soundKey) {
            "PRIME_QUANTUM" -> R.raw.prime_quantum_pulse
            "PRIME_ZEN" -> R.raw.prime_zen_chime
            "PRIME_STROBE" -> R.raw.prime_strobe_alarm
            "PRIME_SIREN" -> R.raw.notification_sound
            else -> null
        }
        return if (resId != null) {
            Uri.parse("android.resource://" + context.packageName + "/" + resId)
        } else {
            Uri.EMPTY
        }
    }

    fun playSoundKey(context: Context, soundKey: String) {
        if (soundKey == "SILENT") return
        try {
            stopCurrentSound()

            // Check if this is an OEM Synthesized tone
            if (isSynthesizedToneKey(soundKey)) {
                playSynthesizedTone(soundKey)
                return
            }

            // Otherwise, play via Uri (Resource, System default, or custom file)
            val uri = getNotificationSoundUri(context, soundKey)
            if (uri != Uri.EMPTY) {
                if (soundKey.startsWith("content://") && !soundKey.contains("media/internal/audio")) {
                    currentMediaPlayer = MediaPlayer().apply {
                        setDataSource(context.applicationContext, uri)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            val attrs = AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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
                            it.audioAttributes = AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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

    private fun isSynthesizedToneKey(key: String): Boolean {
        return key.startsWith("IPHONE_") ||
                key.startsWith("MOTO_") ||
                key.startsWith("SAMSUNG_") ||
                key.startsWith("PIXEL_") ||
                key.startsWith("ONEPLUS_") ||
                key.startsWith("XIAOMI_") ||
                key.startsWith("NOKIA_")
    }

    private fun playSynthesizedTone(soundKey: String) {
        val pcm = generateTonePcm(soundKey) ?: return
        try {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, pcm.size * 2)

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
                AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_NOTIFICATION,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STATIC
                )
            }

            track.write(pcm, 0, pcm.size)
            currentAudioTrack = track
            track.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateTonePcm(soundKey: String): ShortArray? {
        val sampleRate = 44100

        return when (soundKey) {
            "IPHONE_TRITONE" -> {
                // Classic G#5 -> B5 -> E6
                val part1 = synthBellNote(830.61, 130, sampleRate)
                val part2 = synthBellNote(987.77, 130, sampleRate)
                val part3 = synthBellNote(1318.51, 380, sampleRate)
                combinePcmParts(part1, part2, part3)
            }
            "IPHONE_NOTE" -> {
                // D6 glass bell
                synthBellNote(1174.66, 450, sampleRate, glassOvertones = true)
            }
            "IPHONE_POPCORN" -> {
                val p1 = synthChirp(650.0, 900.0, 45, sampleRate)
                val gap1 = ShortArray((sampleRate * 0.02).toInt())
                val p2 = synthChirp(850.0, 1150.0, 45, sampleRate)
                val gap2 = ShortArray((sampleRate * 0.02).toInt())
                val p3 = synthChirp(1100.0, 1550.0, 75, sampleRate)
                combinePcmParts(p1, gap1, p2, gap2, p3)
            }
            "IPHONE_AURORA" -> {
                synthPolyChord(doubleArrayOf(587.33, 880.00, 1174.66, 1479.98), 550, sampleRate)
            }
            "IPHONE_CHORD" -> {
                synthPolyChord(doubleArrayOf(698.46, 880.00, 1046.50, 1318.51), 600, sampleRate)
            }
            "IPHONE_BAMBOO" -> {
                synthMarimbaNote(587.33, 280, sampleRate)
            }
            "MOTO_HELLO" -> {
                // Iconic C5 -> E5 -> G5 -> C6
                val n1 = synthSynthNote(523.25, 110, sampleRate)
                val n2 = synthSynthNote(659.25, 110, sampleRate)
                val n3 = synthSynthNote(783.99, 110, sampleRate)
                val n4 = synthSynthNote(1046.50, 420, sampleRate)
                combinePcmParts(n1, n2, n3, n4)
            }
            "MOTO_PURE" -> {
                synthChirp(1600.0, 750.0, 260, sampleRate, isMetallic = true)
            }
            "MOTO_SPACELINE" -> {
                val b1 = synthBellNote(1200.0, 65, sampleRate)
                val gap = ShortArray((sampleRate * 0.04).toInt())
                val b2 = synthBellNote(1800.0, 220, sampleRate)
                combinePcmParts(b1, gap, b2)
            }
            "MOTO_DROPDOWN" -> {
                val d1 = synthBellNote(1318.51, 80, sampleRate)
                val d2 = synthBellNote(987.77, 80, sampleRate)
                val d3 = synthBellNote(830.61, 260, sampleRate)
                combinePcmParts(d1, d2, d3)
            }
            "SAMSUNG_HORIZON" -> {
                // Bb4 -> Db5 -> Eb5 -> Ab5 -> F5 -> Eb5
                val h1 = synthHarpNote(466.16, 100, sampleRate)
                val h2 = synthHarpNote(554.37, 100, sampleRate)
                val h3 = synthHarpNote(622.25, 120, sampleRate)
                val h4 = synthHarpNote(830.61, 140, sampleRate)
                val h5 = synthHarpNote(698.46, 110, sampleRate)
                val h6 = synthHarpNote(622.25, 480, sampleRate)
                combinePcmParts(h1, h2, h3, h4, h5, h6)
            }
            "SAMSUNG_SPACELINE" -> {
                val s1 = synthBellNote(1046.50, 90, sampleRate)
                val s2 = synthBellNote(1567.98, 340, sampleRate)
                combinePcmParts(s1, s2)
            }
            "SAMSUNG_HARP" -> {
                val a1 = synthHarpNote(523.25, 70, sampleRate)
                val a2 = synthHarpNote(783.99, 70, sampleRate)
                val a3 = synthHarpNote(1046.50, 70, sampleRate)
                val a4 = synthHarpNote(1318.51, 380, sampleRate)
                combinePcmParts(a1, a2, a3, a4)
            }
            "SAMSUNG_WHISTLE" -> {
                val w1 = synthChirp(1400.0, 2000.0, 130, sampleRate)
                val w2 = synthChirp(1800.0, 2600.0, 240, sampleRate)
                combinePcmParts(w1, w2)
            }
            "PIXEL_EUREKA" -> {
                val e1 = synthMarimbaNote(659.25, 95, sampleRate)
                val e2 = synthMarimbaNote(987.77, 340, sampleRate)
                combinePcmParts(e1, e2)
            }
            "PIXEL_HEY" -> {
                val p1 = synthMarimbaNote(783.99, 80, sampleRate)
                val p2 = synthMarimbaNote(1174.66, 80, sampleRate)
                val p3 = synthMarimbaNote(1567.98, 280, sampleRate)
                combinePcmParts(p1, p2, p3)
            }
            "PIXEL_POP" -> {
                synthChirp(350.0, 850.0, 85, sampleRate)
            }
            "ONEPLUS_RHYTHM" -> {
                val o1 = synthSynthNote(700.0, 60, sampleRate)
                val o2 = synthSynthNote(1050.0, 60, sampleRate)
                val o3 = synthSynthNote(1400.0, 260, sampleRate)
                combinePcmParts(o1, o2, o3)
            }
            "ONEPLUS_MEET" -> {
                val m1 = synthBellNote(1046.50, 90, sampleRate)
                val m2 = synthBellNote(880.00, 90, sampleRate)
                val m3 = synthBellNote(698.46, 300, sampleRate)
                combinePcmParts(m1, m2, m3)
            }
            "XIAOMI_DROP" -> {
                val d1 = synthChirp(520.0, 1650.0, 75, sampleRate, isWaterDrop = true)
                val d2 = synthChirp(1100.0, 1400.0, 85, sampleRate, isWaterDrop = true)
                combinePcmParts(d1, d2)
            }
            "XIAOMI_NATURE" -> {
                synthMarimbaNote(783.99, 160, sampleRate)
            }
            "NOKIA_SPECIAL" -> {
                val n1 = synthSquareNote(880.0, 70, sampleRate)
                val g1 = ShortArray((sampleRate * 0.02).toInt())
                val n2 = synthSquareNote(880.0, 70, sampleRate)
                val g2 = ShortArray((sampleRate * 0.02).toInt())
                val n3 = synthSquareNote(880.0, 70, sampleRate)
                val g3 = ShortArray((sampleRate * 0.02).toInt())
                val n4 = synthSquareNote(1318.51, 260, sampleRate)
                combinePcmParts(n1, g1, n2, g2, n3, g3, n4)
            }
            else -> null
        }
    }

    private fun synthBellNote(freq: Double, durationMs: Int, sampleRate: Int, glassOvertones: Boolean = false): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)
        val decayCoeff = if (glassOvertones) 7.0 else 5.5

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val envelope = exp(-decayCoeff * progress) * (if (t < 0.005) t / 0.005 else 1.0)

            val s1 = sin(2.0 * PI * freq * t)
            val s2 = 0.35 * sin(2.0 * PI * (freq * 2.0) * t)
            val s3 = 0.12 * sin(2.0 * PI * (freq * 3.0) * t)
            val s4 = if (glassOvertones) 0.08 * sin(2.0 * PI * (freq * 4.2) * t) else 0.0

            val sampleVal = (s1 + s2 + s3 + s4) * envelope * 0.65
            buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthMarimbaNote(freq: Double, durationMs: Int, sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val envelope = exp(-10.0 * progress) * (if (t < 0.003) t / 0.003 else 1.0)

            val s1 = sin(2.0 * PI * freq * t)
            val s2 = 0.25 * sin(2.0 * PI * (freq * 3.0) * t)
            val s3 = 0.10 * sin(2.0 * PI * (freq * 4.0) * t)

            val sampleVal = (s1 + s2 + s3) * envelope * 0.7
            buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthHarpNote(freq: Double, durationMs: Int, sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val envelope = exp(-4.5 * progress) * (if (t < 0.015) t / 0.015 else 1.0)

            val s1 = sin(2.0 * PI * freq * t)
            val s2 = 0.40 * sin(2.0 * PI * (freq * 2.0) * t)
            val s3 = 0.20 * sin(2.0 * PI * (freq * 3.0) * t)
            val s4 = 0.10 * sin(2.0 * PI * (freq * 4.0) * t)

            val sampleVal = (s1 + s2 + s3 + s4) * envelope * 0.6
            buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthSynthNote(freq: Double, durationMs: Int, sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val envelope = exp(-3.5 * progress) * (if (t < 0.01) t / 0.01 else 1.0)

            val s1 = sin(2.0 * PI * freq * t)
            val s2 = 0.30 * sin(2.0 * PI * (freq * 2.0) * t)

            val sampleVal = (s1 + s2) * envelope * 0.65
            buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthSquareNote(freq: Double, durationMs: Int, sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val envelope = exp(-4.0 * progress)

            val sinVal = sin(2.0 * PI * freq * t)
            val squareVal = if (sinVal >= 0.0) 0.5 else -0.5

            val sampleVal = squareVal * envelope * 0.5
            buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthChirp(startFreq: Double, endFreq: Double, durationMs: Int, sampleRate: Int, isWaterDrop: Boolean = false, isMetallic: Boolean = false): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val currentFreq = startFreq + (endFreq - startFreq) * (if (isWaterDrop) progress * progress else progress)
            phase += 2.0 * PI * currentFreq / sampleRate

            val envelope = if (isWaterDrop) {
                sin(PI * progress) * exp(-1.5 * progress)
            } else if (isMetallic) {
                exp(-4.5 * progress) * (if (t < 0.005) t / 0.005 else 1.0)
            } else {
                exp(-6.0 * progress)
            }

            var s = sin(phase)
            if (isMetallic) {
                s += 0.3 * sin(phase * 2.3)
            }

            val sampleVal = s * envelope * 0.7
            buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthPolyChord(freqs: DoubleArray, durationMs: Int, sampleRate: Int): ShortArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val envelope = exp(-3.0 * progress) * (if (t < 0.02) t / 0.02 else 1.0)

            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * PI * f * t) + 0.2 * sin(2.0 * PI * (f * 2.0) * t)
            }
            val sampleVal = (sum / freqs.size) * envelope * 0.65
            buffer[i] = (sampleVal * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun combinePcmParts(vararg parts: ShortArray): ShortArray {
        val totalLen = parts.sumOf { it.size }
        val combined = ShortArray(totalLen)
        var offset = 0
        for (part in parts) {
            System.arraycopy(part, 0, combined, offset, part.size)
            offset += part.size
        }
        return combined
    }

    fun stopCurrentSound() {
        try {
            currentRingtone?.stop()
            currentRingtone = null

            currentMediaPlayer?.stop()
            currentMediaPlayer?.release()
            currentMediaPlayer = null

            currentAudioTrack?.stop()
            currentAudioTrack?.release()
            currentAudioTrack = null
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

