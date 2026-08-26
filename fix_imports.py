import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.foundation.border" not in content:
    content = content.replace("import androidx.compose.foundation.clickable", "import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.border")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "r") as f:
    content2 = f.read()

if "import androidx.compose.ui.graphics.nativeCanvas" not in content2:
    content2 = content2.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.nativeCanvas")

with open("app/src/main/java/com/example/ui/screens/StatisticsScreen.kt", "w") as f:
    f.write(content2)
