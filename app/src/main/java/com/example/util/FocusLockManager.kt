package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.MainActivity
import com.example.data.model.LockMode
import java.util.concurrent.CopyOnWriteArraySet

object FocusLockManager {
    private const val TAG = "FocusLockManager"

    @Volatile
    var isFocusActive: Boolean = false
        private set

    @Volatile
    var currentLockMode: LockMode = LockMode.NORMAL
        private set

    private val whitelistedPackages = CopyOnWriteArraySet<String>()

    var onDistractionListener: ((packageName: String) -> Unit)? = null

    // Essential system components allowed ONLY for phone dialer, emergency and keyboard
    private val SYSTEM_WHITELIST = setOf(
        "com.android.systemui",
        "android",
        "com.google.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.android.dialer",
        "com.google.android.dialer",
        "com.android.phone",
        "com.android.server.telecom"
    )

    fun updateFocusState(
        isActive: Boolean,
        lockMode: LockMode,
        allowedPackageNames: Collection<String>
    ) {
        isFocusActive = isActive
        currentLockMode = lockMode
        whitelistedPackages.clear()
        whitelistedPackages.addAll(allowedPackageNames)
        Log.d(TAG, "FocusLockState updated: isActive=$isActive, mode=$lockMode, allowedCount=${whitelistedPackages.size}")
        
        if (!isActive) {
            FocusLockOverlayManager.dismissOverlay()
        }
    }

    fun getAllowedPackages(): Set<String> {
        return whitelistedPackages.toSet()
    }

    fun isPackageAllowed(packageName: String, ourPackageName: String): Boolean {
        if (!isFocusActive || currentLockMode == LockMode.NORMAL) {
            return true
        }

        // Own app is always allowed
        if (packageName == ourPackageName) {
            return true
        }

        // Allow keyboard and core phone call dialer
        if (SYSTEM_WHITELIST.contains(packageName) || 
            packageName.contains("inputmethod", ignoreCase = true) ||
            packageName.contains("telecom", ignoreCase = true)
        ) {
            return true
        }

        return whitelistedPackages.contains(packageName)
    }

    fun handleBlockedAppOpened(
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
    }

    fun launchAllowedApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch $packageName", e)
            false
        }
    }
}
