import re

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

old_handle = """        } else {
            FocusLockOverlayManager.showBlockedOverlay(
                context = context,
                blockedPackage = blockedPackageName,
                remainingSeconds = remainingSeconds,
                subjectName = subjectName
            )
        }
    }"""

new_handle = """        } else {
            onDistractionListener?.invoke(blockedPackageName)
            FocusLockOverlayManager.showBlockedOverlay(
                context = context,
                blockedPackage = blockedPackageName,
                remainingSeconds = remainingSeconds,
                subjectName = subjectName
            )
        }
    }"""

content = content.replace(old_handle, new_handle)
with open("app/src/main/java/com/example/util/FocusLockManager.kt", "w") as f:
    f.write(content)

