import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

old_click = """                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSpecialWhitelistPopup = true }"""
new_click = """                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            if (!timerState.isRunning) {
                                selectedSpecialWhitelist = "SPECIAL"
                                showQuickDurationDialog = true 
                            } else if (timerState.isSpecialSession) {
                                viewModel.completeFocusSession()
                            }
                        }"""
content = content.replace(old_click, new_click)

old_switch = """                val isSpecialRunning = timerState.isRunning && timerState.isSpecialSession
                Switch(
                    checked = isSpecialRunning,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            showSpecialWhitelistPopup = true
                        } else {
                            viewModel.completeFocusSession() // Turn off
                        }
                    },"""
new_switch = """                val isSpecialRunning = timerState.isRunning && timerState.isSpecialSession
                Switch(
                    checked = isSpecialRunning,
                    onCheckedChange = { isChecked ->
                        if (isChecked && !timerState.isRunning) {
                            selectedSpecialWhitelist = "SPECIAL"
                            showQuickDurationDialog = true
                        } else if (!isChecked && timerState.isSpecialSession) {
                            viewModel.completeFocusSession()
                        }
                    },"""
content = content.replace(old_switch, new_switch)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
