import re

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "r") as f:
    content = f.read()

# Let's see how tick() works and add addPenaltyTime
new_method = """    fun addPenaltyTime(seconds: Int) {
        _timerState.update { 
            it.copy(
                remainingSeconds = it.remainingSeconds + seconds,
                totalSeconds = it.totalSeconds + seconds
            ) 
        }
    }
"""

content = content.replace("    fun stopSession() {", new_method + "\n    fun stopSession() {")

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "w") as f:
    f.write(content)
