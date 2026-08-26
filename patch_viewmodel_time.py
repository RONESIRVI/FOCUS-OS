import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

new_method = """    fun addPenaltyTime(seconds: Int) {
        serviceConnection.addPenaltyTime(seconds)
    }
"""

content = content.replace("    fun completeFocusSession() {", new_method + "\n    fun completeFocusSession() {")

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
