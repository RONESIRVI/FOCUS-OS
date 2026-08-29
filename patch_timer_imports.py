import re

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "r") as f:
    content = f.read()

imports_to_add = """import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
"""

# Insert right after package
content = content.replace("package com.example.ui.screens\n", f"package com.example.ui.screens\n\n{imports_to_add}")

with open("app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt", "w") as f:
    f.write(content)
