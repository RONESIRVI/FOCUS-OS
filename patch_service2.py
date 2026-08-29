import re

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "r") as f:
    content = f.read()

# Fix in startAppLockMonitoring just in case
old_pending = """                            if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                                event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND
                            ) {
                                lastEventPackage = event.packageName
                            }"""
new_pending = """                            if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                                event.eventType == 1) { // 1 is MOVE_TO_FOREGROUND
                                lastEventPackage = event.packageName
                            }"""
content = content.replace(old_pending, new_pending)

with open("app/src/main/java/com/example/services/FocusTimerService.kt", "w") as f:
    f.write(content)
