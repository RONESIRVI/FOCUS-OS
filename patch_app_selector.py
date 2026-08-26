import re

with open("app/src/main/java/com/example/ui/screens/AppSelectorScreen.kt", "r") as f:
    content = f.read()

header = """                Text(
                    text = "SELECT ALLOWED APPS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "Whitelisted apps will stay accessible during Focus Lock",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusTextSecondary
                )"""

new_header = """                Text(
                    text = if (currentProfile == "STRICT") "STRICT SCHEDULE APPS" else "FOCUS SESSION APPS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = if (currentProfile == "STRICT") "Only these apps will be allowed during strict schedule" else "Whitelisted apps will stay accessible during manual focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = FocusTextSecondary
                )"""

content = content.replace(header, new_header)

with open("app/src/main/java/com/example/ui/screens/AppSelectorScreen.kt", "w") as f:
    f.write(content)
