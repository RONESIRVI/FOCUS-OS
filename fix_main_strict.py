import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_block_logic = """                        // Check against block list
                        val blockedApps = viewModel.whitelistedAppsManual.value
                        val isBlocked = blockedApps.any { it.packageName == recentApp && !it.isAllowed }
                        shouldBlock = isBlocked"""

new_block_logic = """                        // Check against block list
                        val blockedApps = if (timerState.lockMode == LockMode.STRICT_LOCK) {
                            viewModel.whitelistedAppsStrict.value
                        } else {
                            viewModel.whitelistedAppsManual.value
                        }
                        val isBlocked = blockedApps.any { it.packageName == recentApp && !it.isAllowed }
                        shouldBlock = isBlocked"""

content = content.replace(old_block_logic, new_block_logic)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
