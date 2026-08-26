import re

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "r") as f:
    content = f.read()

old_func = """    fun handleBlockedAppOpened(
        context: Context,
        blockedPackageName: String,
        remainingSeconds: Int = 0,
        subjectName: String = "Deep Focus"
    ) {
        Log.w(TAG, "BLOCKED APP DETECTED: $blockedPackageName. Initiating lock enforcement...")
        
        val hasOverlayPerm = android.provider.Settings.canDrawOverlays(context)
        
        if (currentLockMode == LockMode.SOFT_LOCK) {
            onDistractionListener?.invoke(blockedPackageName, false)
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
            onDistractionListener?.invoke(blockedPackageName, true)
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
            onDistractionListener?.invoke(blockedPackageName, false)
            FocusLockOverlayManager.showBlockedOverlay(
                context = context,
                blockedPackage = blockedPackageName,
                remainingSeconds = remainingSeconds,
                subjectName = subjectName
            )
        }
    }"""

new_func = """    fun handleBlockedAppOpened(
        context: Context,
        blockedPackageName: String,
        remainingSeconds: Int = 0,
        subjectName: String = "Deep Focus"
    ) {
        Log.w(TAG, "BLOCKED APP DETECTED: $blockedPackageName. Initiating lock enforcement...")
        
        val hasOverlayPerm = android.provider.Settings.canDrawOverlays(context)
        
        when (currentLockMode) {
            LockMode.SOFT_LOCK -> {
                onDistractionListener?.invoke(blockedPackageName, false)
                if (hasOverlayPerm) {
                    FocusLockOverlayManager.showBlockedOverlay(
                        context = context,
                        blockedPackage = blockedPackageName,
                        remainingSeconds = remainingSeconds,
                        subjectName = subjectName,
                        isSoftLock = true
                    )
                } else {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        android.widget.Toast.makeText(
                            context, 
                            "⚠️ Focus Reminder: You are leaving your study session!", 
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            LockMode.STRICT_LOCK -> {
                onDistractionListener?.invoke(blockedPackageName, false)
                if (hasOverlayPerm) {
                    FocusLockOverlayManager.showBlockedOverlay(
                        context = context,
                        blockedPackage = blockedPackageName,
                        remainingSeconds = remainingSeconds,
                        subjectName = subjectName,
                        isSoftLock = false
                    )
                } else {
                    val redirectIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        // Intentionally omitting BLOCKED_PACKAGE_EVENT to avoid red modal for Strict Lock
                    }
                    try {
                        context.startActivity(redirectIntent)
                    } catch (e: Exception) {}
                }
            }
            LockMode.MAXIMUM_LOCK -> {
                // Instantly yank back to app with RED warning modal
                onDistractionListener?.invoke(blockedPackageName, true)
                val redirectIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("BLOCKED_PACKAGE_EVENT", blockedPackageName)
                }
                try {
                    context.startActivity(redirectIntent)
                } catch (e: Exception) {}
            }
            else -> {}
        }
    }"""

content = content.replace(old_func, new_func)

with open("app/src/main/java/com/example/util/FocusLockManager.kt", "w") as f:
    f.write(content)

