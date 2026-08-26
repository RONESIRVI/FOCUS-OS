import re

with open("app/src/main/java/com/example/util/FocusLockOverlayManager.kt", "r") as f:
    content = f.read()

content = content.replace(
    'fun dismissOverlay() {',
    'fun dismissOverlay() {\n        softLockRunnable?.let { mainHandler.removeCallbacks(it) }\n        softLockRunnable = null'
)

with open("app/src/main/java/com/example/util/FocusLockOverlayManager.kt", "w") as f:
    f.write(content)

