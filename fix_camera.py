import re

with open("app/src/main/java/com/example/ui/screens/CameraVerificationScreen.kt", "r") as f:
    content = f.read()

# Make the VERIFY & PROCEED button always enabled
content = content.replace("enabled = photoUri != null", "enabled = true")
# Also change the button color to always be Primary if enabled=true, or just leave it
content = content.replace("containerColor = if (photoUri != null) FocusPrimary else FocusSurfaceVariant", "containerColor = FocusPrimary")

with open("app/src/main/java/com/example/ui/screens/CameraVerificationScreen.kt", "w") as f:
    f.write(content)
