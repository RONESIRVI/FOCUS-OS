import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace if needed
content = content.replace("Icons.Default.Bolt", "Icons.Default.FlashOn")
content = content.replace("Icons.Default.MenuBook", "Icons.Default.MenuBook") # MenuBook usually exists

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

