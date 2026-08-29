import re

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

old_pending = """        if (!isFocusActive && hasPendingSchedule()) {
            val pId = pendingSessionId
            val pName = pendingSessionName ?: "Scheduled Focus"
            
            FocusLockOverlayManager.dismissOverlay()
            FocusLockOverlayManager.bringAppToFront(
                context = context,
                blockedPackage = blockedPackageName,
                sessionId = pId,
                isPending = true,
                pendingName = pName
            )
            return
        }"""
new_pending = """        if (!isFocusActive && hasPendingSchedule()) {
            val pId = pendingSessionId
            val pName = pendingSessionName ?: "Scheduled Focus"
            
            FocusLockOverlayManager.dismissOverlay()
            FocusLockOverlayManager.showBlockedOverlay(
                context = context,
                blockedPackage = blockedPackageName,
                remainingSeconds = 0,
                subjectName = pName,
                allowedPackages = emptyList(),
                isSoftLock = false
            )
            FocusLockOverlayManager.bringAppToFront(
                context = context,
                blockedPackage = blockedPackageName,
                sessionId = pId,
                isPending = true,
                pendingName = pName
            )
            return
        }"""
content = content.replace(old_pending, new_pending)

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "w") as f:
    f.write(content)
