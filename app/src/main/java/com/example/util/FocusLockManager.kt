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

    // Always allowed system components to prevent device bricking / freeze
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
    }

    fun isPackageAllowed(packageName: String, ourPackageName: String): Boolean {
        if (!isFocusActive || currentLockMode == LockMode.NORMAL) {
            return true
        }

        // Own app is always allowed
        if (packageName == ourPackageName) {
            return true
        }

        // Essential system UI / dialer / keyboard
        if (SYSTEM_WHITELIST.contains(packageName) || 
            packageName.contains("launcher", ignoreCase = true) ||
            packageName.contains("inputmethod", ignoreCase = true)
        ) {
            // Note: launchers might be permitted or blocked depending on preference; allowing launcher allows navigating to allowed apps
            return true
        }

        return whitelistedPackages.contains(packageName)
    }

    fun handleBlockedAppOpened(context: Context, blockedPackageName: String) {
        Log.w(TAG, "BLOCKED APP DETECTED: $blockedPackageName. Initiating lock enforcement...")
        
        onDistractionListener?.invoke(blockedPackageName)

        // Launch MainActivity over the blocked app
        val redirectIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("BLOCKED_PACKAGE_EVENT", blockedPackageName)
        }
        context.startActivity(redirectIntent)
    }
}
