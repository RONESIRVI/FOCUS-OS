import re

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

# Replace bringAppToFront with showBlockedOverlay for better reliability
old_soft = """            LockMode.SOFT_LOCK -> {
                onDistractionListener?.invoke(blockedPackageName, false)
                FocusLockOverlayManager.dismissOverlay()
                FocusLockOverlayManager.bringAppToFront(
                    context = context,
                    blockedPackage = blockedPackageName,
                    isSoftLock = true
                )
            }"""
new_soft = """            LockMode.SOFT_LOCK -> {
                onDistractionListener?.invoke(blockedPackageName, false)
                FocusLockOverlayManager.dismissOverlay()
                FocusLockOverlayManager.bringAppToFront(
                    context = context,
                    blockedPackage = blockedPackageName,
                    isSoftLock = true
                )
            }"""

old_max = """            LockMode.MAXIMUM_LOCK -> {
                onDistractionListener?.invoke(blockedPackageName, true)
                FocusLockOverlayManager.dismissOverlay()
                FocusLockOverlayManager.bringAppToFront(
                    context = context,
                    blockedPackage = blockedPackageName,
                    isSoftLock = false
                )
            }"""
new_max = """            LockMode.MAXIMUM_LOCK -> {
                onDistractionListener?.invoke(blockedPackageName, true)
                // Use robust WindowManager overlay on top of starting Activity
                FocusLockOverlayManager.showBlockedOverlay(
                    context = context,
                    blockedPackage = blockedPackageName,
                    remainingSeconds = remainingSeconds,
                    subjectName = subjectName,
                    allowedPackages = emptyList(),
                    isSoftLock = false
                )
                FocusLockOverlayManager.bringAppToFront(
                    context = context,
                    blockedPackage = blockedPackageName,
                    isSoftLock = false
                )
            }"""

content = content.replace(old_max, new_max)

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "w") as f:
    f.write(content)
