import re

with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "r") as f:
    content = f.read()

old_func = """    fun triggerDistractionWarning(blockedPackage: String = "") {
        if (_serviceTimerState.value.isRunning) {
            if (blockedPackage.isNotBlank()) {
                _lastBlockedPackage.value = blockedPackage
            }
            val currentMode = _serviceTimerState.value.lockMode
            if (currentMode == LockMode.STRICT_LOCK || currentMode == LockMode.MAXIMUM_LOCK) {
                _showLockOverlay.value = true
            }
        }
    }"""

new_func = """    fun triggerDistractionWarning(blockedPackage: String = "", showRedModal: Boolean = false) {
        if (_serviceTimerState.value.isRunning) {
            if (blockedPackage.isNotBlank()) {
                _lastBlockedPackage.value = blockedPackage
            }
            if (showRedModal) {
                _showLockOverlay.value = true
            }
        }
    }"""

content = content.replace(old_func, new_func)
with open("app/src/main/java/com/example/ui/viewmodel/FocusViewModel.kt", "w") as f:
    f.write(content)

