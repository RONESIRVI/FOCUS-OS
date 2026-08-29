import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace clickable
old_click = ".clickable { showSpecialWhitelistPopup = true }"
new_click = """.clickable { 
                        if (!timerState.isRunning) {
                            selectedSpecialWhitelist = "SPECIAL"
                            showQuickDurationDialog = true 
                        } else if (timerState.isSpecialSession) {
                            viewModel.completeFocusSession()
                        }
                    }"""
content = content.replace(old_click, new_click)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
