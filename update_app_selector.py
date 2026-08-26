import re

with open("app/src/main/java/com/example/ui/screens/AppSelectorScreen.kt", "r") as f:
    content = f.read()

# Change "Allowed" to "Blocked"
content = content.replace("STRICT SCHEDULE APPS", "STRICT FOCUS APPS")
content = content.replace("Select apps that are permitted to run", "Select apps to block")
content = content.replace("permitted", "blocked")
content = content.replace("Allowed apps during Strict Focus", "Blocked apps during Strict Focus")
content = content.replace("SAVE WHITELIST PERMISSIONS", "SAVE BLOCKLIST")

# Change icon mapping
content = content.replace("imageVector = if (app.isAllowed) Icons.Default.Check else Icons.Default.Lock,", "imageVector = if (app.isAllowed) Icons.Default.Lock else Icons.Default.Check,")
content = content.replace("tint = if (app.isAllowed) FocusPrimary else Color.Red,", "tint = if (app.isAllowed) Color.Red else FocusPrimary,")

# Change container color
content = content.replace("if (app.isAllowed) FocusPrimary.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.15f)", "if (app.isAllowed) Color.Red.copy(alpha = 0.15f) else FocusPrimary.copy(alpha = 0.2f)")
content = content.replace("if (app.isAllowed) FocusSurface else FocusSurfaceVariant.copy(alpha = 0.5f)", "if (app.isAllowed) FocusSurfaceVariant.copy(alpha = 0.5f) else FocusSurface")

with open("app/src/main/java/com/example/ui/screens/AppSelectorScreen.kt", "w") as f:
    f.write(content)
