import sys
import re

with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "r") as f:
    content = f.read()

# Fix the button logic
old_logic = """                                        .clickable {
                                            if (isScheduled) {
                                                viewModel.scheduleFocusSession(scheduleHour, scheduleMinute)
                                                // After scheduling, just go back
                                                onBack()
                                            } else {
                                                viewModel.startFocusSession()
                                                onStartSession()
                                            }
                                        }"""
new_logic = """                                        .clickable {
                                            viewModel.startFocusSession()
                                            onStartSession()
                                        }"""
content = content.replace(old_logic, new_logic)

old_text = 'text = if (isScheduled) "SCHEDULE SESSION" else "LAUNCH FOCUS SHIELD",'
new_text = 'text = "LAUNCH FOCUS SHIELD",'
content = content.replace(old_text, new_text)

# Fix lingering isScheduled usages from line 704
# Let's just find the whole scheduling row and delete it.
start_idx = content.find('// START SCHEDULE TOGGLE ROW (if it exists)')
# Actually, the previous regex was meant to remove a block.
# Let's remove from "Row(" down to just before "// Section 2: Allowed Apps Whitelist"
# Wait, I didn't successfully remove it.
# Let's search for "isScheduled" and see where it still is.
with open("app/src/main/java/com/example/ui/screens/FocusSetupScreen.kt", "w") as f:
    f.write(content)
