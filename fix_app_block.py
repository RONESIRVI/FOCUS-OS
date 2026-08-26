import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.ui.navigation.FocusBottomNavigation", "import com.example.ui.navigation.FocusBottomNavigation\nimport android.app.usage.UsageStatsManager\nimport android.content.Context\nimport android.util.Log")

new_enforce = """    private fun enforceFocusLock() {
        val timerState = viewModel.timerState.value
        if (timerState.isRunning && timerState.lockMode != LockMode.NORMAL) {
            
            // Check if current foreground app is in the blocklist
            var shouldBlock = true // Default to true if we can't check
            try {
                val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
                
                if (stats != null && stats.isNotEmpty()) {
                    var recentApp = ""
                    var lastTime = 0L
                    for (usageStats in stats) {
                        if (usageStats.lastTimeUsed > lastTime) {
                            lastTime = usageStats.lastTimeUsed
                            recentApp = usageStats.packageName
                        }
                    }
                    
                    if (recentApp == packageName || recentApp.contains("launcher") || recentApp.contains("systemui")) {
                        shouldBlock = false
                    } else {
                        // Check against block list
                        val blockedApps = viewModel.whitelistedAppsManual.value
                        val isBlocked = blockedApps.any { it.packageName == recentApp && !it.isAllowed }
                        shouldBlock = isBlocked
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking usage stats", e)
            }
            
            if (shouldBlock) {
                viewModel.triggerDistractionWarning()

                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        }
    }"""

content = re.sub(r"    private fun enforceFocusLock\(\) \{.*?\n    \}", new_enforce, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
