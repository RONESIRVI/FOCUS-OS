import re

with open("app/src/main/java/com/example/services/FocusAudioEngine.kt", "r") as f:
    content = f.read()

# Replace SoundType enum
old_enum = """enum class SoundType(val label: String, val isBinaural: Boolean) {
    NONE("Silent Mode", false),
    BROWN_NOISE("Brown Noise Focus", false),
    PINK_NOISE("Pink Noise Soft", false),
    WHITE_NOISE("White Noise Fan", false),
    RAIN_SOUNDS("Calm Rain Patters", false),
    ALPHA_WAVES("Alpha 10Hz (Deep Focus)", true),
    BETA_WAVES("Beta 16Hz (Active Study)", true),
    BAROQUE_60BPM("Baroque 60BPM Harmonics", false)
}"""
new_enum = """enum class SoundType(val label: String, val isBinaural: Boolean) {
    NONE("Silent Mode", false),
    CUSTOM_AUDIO("Upload Audio / MP3 File", false)
}"""
content = content.replace(old_enum, new_enum)

with open("app/src/main/java/com/example/services/FocusAudioEngine.kt", "w") as f:
    f.write(content)
