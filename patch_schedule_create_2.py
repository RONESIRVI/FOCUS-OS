import re

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "r") as f:
    content = f.read()

# Make the create schedule button actually set the properties in the view model before calling scheduleFocusSession
btn = """                Button(
                    onClick = {
                        viewModel.scheduleFocusSession(startHour, startMinute)
                        onScheduleCreated()
                    },"""
                    
new_btn = """                Button(
                    onClick = {
                        viewModel.updateSetup(
                            sessionName = sessionName,
                            durationMinutes = durationMinutes,
                            lockMode = com.example.data.model.LockMode.STRICT_LOCK
                        )
                        viewModel.scheduleFocusSession(startHour, startMinute)
                        onScheduleCreated()
                    },"""
                    
content = content.replace(btn, new_btn)

with open("app/src/main/java/com/example/ui/screens/ScheduleCreateScreen.kt", "w") as f:
    f.write(content)
