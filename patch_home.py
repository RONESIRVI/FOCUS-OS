import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Add timerState
content = content.replace(
    "val allSessions by viewModel.allSessions.collectAsState()",
    "val allSessions by viewModel.allSessions.collectAsState()\n    val timerState by viewModel.timerState.collectAsState()"
)

# Update Switch
old_switch = """                Switch(
                    checked = false,
                    onCheckedChange = { showSpecialWhitelistPopup = true },"""
new_switch = """                val isSpecialRunning = timerState.isRunning && timerState.isSpecialSession
                Switch(
                    checked = isSpecialRunning,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            showSpecialWhitelistPopup = true
                        } else {
                            viewModel.completeFocusSession() // Turn off
                        }
                    },"""
content = content.replace(old_switch, new_switch)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
