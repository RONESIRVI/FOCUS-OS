import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Replace LockMode.entries.filter { it != LockMode.NORMAL }.forEachIndexed
# Wait, it's better to just do `val lockModes = LockMode.entries.filter { it != LockMode.NORMAL }`
# and then `lockModes.forEachIndexed { index, mode -> ... if (index < lockModes.size - 1) ... }`
old_code = """                LockMode.entries.filter { it != LockMode.NORMAL }.forEachIndexed { index, mode ->
                    val isSelected = setup.lockMode == mode
                    Row("""

new_code = """                val selectableModes = LockMode.entries.filter { it != LockMode.NORMAL }
                selectableModes.forEachIndexed { index, mode ->
                    val isSelected = setup.lockMode == mode
                    Row("""
content = content.replace(old_code, new_code)

old_div = """                    if (index < LockMode.entries.size - 2) {
                        Divider(color = FocusSurfaceVariant)
                    }"""

new_div = """                    if (index < selectableModes.size - 1) {
                        Divider(color = FocusSurfaceVariant)
                    }"""
content = content.replace(old_div, new_div)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
