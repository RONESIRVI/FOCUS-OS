import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

old_func = """    fun triggerDistractionWarning(blockedPackage: String = "") {
        if (_serviceTimerState.value.isRunning) {
            timerService?.recordDistractionAttempt()
            if (blockedPackage.isNotBlank()) {
                _lastBlockedPackage.value = blockedPackage
            }
            if (_serviceTimerState.value.lockMode != LockMode.NORMAL) {
                _showLockOverlay.value = true
            }
        }
    }"""

new_func = """    fun triggerDistractionWarning(blockedPackage: String = "") {
        if (_serviceTimerState.value.isRunning) {
            // Intentionally omitting timerService?.recordDistractionAttempt() here to prevent double-counting.
            // FocusTimerService already records it when it detects the distraction.
            if (blockedPackage.isNotBlank()) {
                _lastBlockedPackage.value = blockedPackage
            }
            if (_serviceTimerState.value.lockMode != LockMode.NORMAL) {
                _showLockOverlay.value = true
            }
        }
    }"""
content = content.replace(old_func, new_func)

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)
