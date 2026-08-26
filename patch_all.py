import re

# 1. FocusLockManager.kt
with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

content = content.replace(
    'var onDistractionListener: ((packageName: String) -> Unit)? = null',
    'var onDistractionListener: ((packageName: String, showRedModal: Boolean) -> Unit)? = null'
)

content = content.replace(
    'onDistractionListener?.invoke(blockedPackageName)\n            // Show a non-blocking toast',
    'onDistractionListener?.invoke(blockedPackageName, false)\n            // Show a non-blocking toast'
)
content = content.replace(
    'onDistractionListener?.invoke(blockedPackageName)\n            val redirectIntent',
    'onDistractionListener?.invoke(blockedPackageName, true)\n            val redirectIntent'
)
content = content.replace(
    'onDistractionListener?.invoke(blockedPackageName)\n            FocusLockOverlayManager.showBlockedOverlay(',
    'onDistractionListener?.invoke(blockedPackageName, false)\n            FocusLockOverlayManager.showBlockedOverlay('
)

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "w") as f:
    f.write(content)

# 2. MainActivity.kt
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content2 = f.read()

content2 = content2.replace(
    'FocusLockManager.onDistractionListener = { blockedPkg ->\n            runOnUiThread {\n                viewModel.triggerDistractionWarning(blockedPkg, showRedModal = true)\n            }\n        }',
    'FocusLockManager.onDistractionListener = { blockedPkg, showRedModal ->\n            runOnUiThread {\n                viewModel.triggerDistractionWarning(blockedPkg, showRedModal = showRedModal)\n            }\n        }'
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content2)

