import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

header = """            Text(
                text = "CREATE FOCUS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )"""

new_header = """            Text(
                text = "FOCUS SESSION",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )"""

content = content.replace(header, new_header)

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(content)
