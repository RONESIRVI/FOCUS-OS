import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

btn = """                Button(
                    onClick = {
                        viewModel.updateSetup(
                            sessionName = sessionName,
                            subjectName = selectedSubject?.name ?: "General Focus",
                            durationMinutes = customDuration,
                            lockMode = selectedLockMode
                        )
                        onStartSession()
                    },"""
                    
new_btn = """                Button(
                    onClick = {
                        viewModel.updateSetup(
                            sessionName = sessionName,
                            subjectName = selectedSubject?.name ?: "General Focus",
                            durationMinutes = customDuration,
                            lockMode = selectedLockMode
                        )
                        onStartSession()
                    },"""
                    
content = content.replace(btn, new_btn)

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(content)
