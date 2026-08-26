import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Allowed apps during Strict Focus", "Apps to block during Strict Focus")
content = content.replace("Allowed apps during Quick Focus", "Apps to block during Quick Focus")
content = content.replace("SAVE WHITELIST PERMISSIONS", "SAVE BLOCKLIST")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
