import re

# Patch FocusLockManager.kt
with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

old_handle = """    fun handleBlockedAppOpened(
        context: Context,
        blockedPackageName: String,
        remainingSeconds: Int = 0,
        subjectName: String = "Deep Focus"
    ) {
        Log.w(TAG, "BLOCKED APP DETECTED: $blockedPackageName. Initiating lock enforcement...")
        
        onDistractionListener?.invoke(blockedPackageName)

        // Show window overlay immediately
        FocusLockOverlayManager.showBlockedOverlay(
            context = context,
            blockedPackage = blockedPackageName,
            remainingSeconds = remainingSeconds,
            subjectName = subjectName
        )

        // Also launch MainActivity with high-priority flags over the blocked app
        val redirectIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("BLOCKED_PACKAGE_EVENT", blockedPackageName)
        }
        try {
            context.startActivity(redirectIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to redirect from blocked app: $blockedPackageName", e)
        }
    }"""

new_handle = """    fun handleBlockedAppOpened(
        context: Context,
        blockedPackageName: String,
        remainingSeconds: Int = 0,
        subjectName: String = "Deep Focus"
    ) {
        Log.w(TAG, "BLOCKED APP DETECTED: $blockedPackageName. Initiating lock enforcement...")
        
        val hasOverlayPerm = android.provider.Settings.canDrawOverlays(context)
        
        if (currentLockMode == LockMode.SOFT_LOCK || !hasOverlayPerm) {
            onDistractionListener?.invoke(blockedPackageName)
            val redirectIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("BLOCKED_PACKAGE_EVENT", blockedPackageName)
            }
            try {
                context.startActivity(redirectIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to redirect from blocked app: $blockedPackageName", e)
            }
        } else {
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

# Patch FocusLockOverlayManager.kt
with open("app/src/main/java/com/example/util/FocusLockOverlayManager.kt", "r") as f:
    content2 = f.read()

old_bring = """    private fun bringAppToFront(context: Context, blockedPackage: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("BLOCKED_PACKAGE_EVENT", blockedPackage)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting MainActivity from background", e)
        }
    }"""

new_bring = """    private fun bringAppToFront(context: Context, blockedPackage: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                // Intentionally NOT sending BLOCKED_PACKAGE_EVENT so the Red in-app warning doesn't show over the timer
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting MainActivity from background", e)
        }
    }"""
content2 = content2.replace(old_bring, new_bring)

with open("app/src/main/java/com/example/util/FocusLockOverlayManager.kt", "w") as f:
    f.write(content2)

