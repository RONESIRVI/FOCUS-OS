import re

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

old_handle = """    fun handleBlockedAppOpened(
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

new_handle = """    fun handleBlockedAppOpened(
        context: Context,
        blockedPackageName: String,
        remainingSeconds: Int = 0,
        subjectName: String = "Deep Focus"
    ) {
        Log.w(TAG, "BLOCKED APP DETECTED: $blockedPackageName. Initiating lock enforcement...")
        
        val hasOverlayPerm = android.provider.Settings.canDrawOverlays(context)
        
        if (currentLockMode == LockMode.SOFT_LOCK) {
            onDistractionListener?.invoke(blockedPackageName)
            // Show a non-blocking toast reminder, do NOT stop the user
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(
                    context, 
                    "⚠️ Focus Reminder: You are leaving your study session!", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } else if (!hasOverlayPerm) {
            // Strict/Max mode but no overlay permission -> fallback to redirecting
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

