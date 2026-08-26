import re

with open("app/src/main/java/com/example/ui/screens/AppSelectorScreen.kt", "r") as f:
    content = f.read()

# Let's fix the Switch and Icons so that "isAllowed = false" means it's BLOCKED, 
# and the UI shows it as selected when blocked.

def replace_switch(m):
    return """                        Switch(
                            checked = !app.isAllowed,
                            onCheckedChange = { isBlocked ->
                                viewModel.toggleAppAllowed(app.packageName, !isBlocked, currentProfile)
                            },"""
content = re.sub(r"                        Switch\(\n.*?checked = app\.isAllowed,\n.*?onCheckedChange = \{ isChecked ->\n.*?viewModel\.toggleAppAllowed\(app\.packageName, isChecked, currentProfile\)\n.*?\},", replace_switch, content, flags=re.DOTALL)

# Fix the icons and colors back to represent "Blocked" state clearly
# Checked state (!isAllowed) means Blocked (Red). 
content = content.replace("if (app.isAllowed) Icons.Default.Lock else Icons.Default.Check", "if (!app.isAllowed) Icons.Default.Lock else Icons.Default.Check")
content = content.replace("if (app.isAllowed) Color.Red else FocusPrimary", "if (!app.isAllowed) Color.Red else FocusPrimary")
content = content.replace("if (app.isAllowed) Color.Red.copy(alpha = 0.15f) else FocusPrimary.copy(alpha = 0.2f)", "if (!app.isAllowed) Color.Red.copy(alpha = 0.15f) else FocusPrimary.copy(alpha = 0.2f)")
content = content.replace("if (app.isAllowed) FocusSurfaceVariant.copy(alpha = 0.5f) else FocusSurface", "if (!app.isAllowed) FocusSurface else FocusSurfaceVariant.copy(alpha = 0.5f)")

with open("app/src/main/java/com/example/ui/screens/AppSelectorScreen.kt", "w") as f:
    f.write(content)
