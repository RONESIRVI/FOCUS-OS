package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.example.MainActivity
import com.example.data.db.AppDatabase
import com.example.data.model.LockMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    var onDistractionListener: ((packageName: String, showRedModal: Boolean) -> Unit)? = null

    // Cache home launchers
    private var cachedHomeLaunchers: Set<String>? = null
    private var lastHomeLauncherCheckTime = 0L

    private fun getHomeLaunchers(context: Context): Set<String> {
        val now = System.currentTimeMillis()
        cachedHomeLaunchers?.let {
            if (now - lastHomeLauncherCheckTime < 60_000) return it
        }
        val launchers = mutableSetOf(
            "com.android.systemui",
            "com.google.android.apps.nexuslauncher",
            "com.sec.android.app.launcher",
            "com.miui.home",
            "com.oneplus.launcher",
            "com.oppo.launcher",
            "com.huawei.android.launcher",
            "com.android.launcher",
            "com.android.launcher3"
        )
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
            for (info in resolveInfos) {
                info.activityInfo?.packageName?.let { launchers.add(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying home launchers", e)
        }
        cachedHomeLaunchers = launchers
        lastHomeLauncherCheckTime = now
        return launchers
    }

    private fun isPhoneCallOrCommunication(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return lower.contains("dialer") ||
                lower.contains("phone") ||
                lower.contains("telecom") ||
                lower.contains("telephony") ||
                lower.contains("incallui") ||
                lower.contains("contacts") ||
                lower.contains("messaging") ||
                lower.contains("mms") ||
                lower.contains("sms") ||
                lower.contains("message") ||
                packageName == "com.google.android.apps.messaging" ||
                packageName == "com.samsung.android.messaging" ||
                packageName == "com.android.mms" ||
                packageName == "com.google.android.dialer" ||
                packageName == "com.android.dialer" ||
                packageName == "com.android.phone" ||
                packageName == "com.android.server.telecom"
    }

    private fun isSystemUtilityOrKeyboard(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return lower == "android" ||
                lower == "com.android.systemui" ||
                lower == "com.android.settings" ||
                lower.contains("inputmethod") ||
                lower.contains("keyboard") ||
                lower.contains("honeyboard") ||
                lower.contains("permissioncontroller") ||
                lower.contains("packageinstaller")
    }

    private fun isPhoneCallActive(context: Context): Boolean {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            tm != null && tm.callState != TelephonyManager.CALL_STATE_IDLE
        } catch (e: Exception) {
            false
        }
    }

    @Volatile
    var isCameraVerificationActive: Boolean = false
        private set

    fun setCameraVerificationActive(active: Boolean) {
        isCameraVerificationActive = active
        Log.d(TAG, "setCameraVerificationActive: $active")
    }

    fun isCameraPackage(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return lower.contains("camera") ||
                lower.contains("cam") ||
                packageName == "com.android.camera" ||
                packageName == "com.google.android.GoogleCamera" ||
                packageName == "com.sec.android.app.camera" ||
                packageName == "com.miui.camera" ||
                packageName == "com.oneplus.camera" ||
                packageName == "com.oppo.camera" ||
                packageName == "com.huawei.camera"
    }

    @Volatile
    var pendingSessionId: Long? = null
        private set

    @Volatile
    var pendingSessionName: String? = null
        private set

    fun setPendingSchedule(sessionId: Long, sessionName: String, context: Context? = null) {
        pendingSessionId = sessionId
        pendingSessionName = sessionName
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = context.getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)
                    val savedSnapshot = prefs.getString("scheduled_apps_$sessionId", null)
                    val apps = if (!savedSnapshot.isNullOrBlank()) {
                        savedSnapshot.split(",").filter { it.isNotBlank() }
                    } else {
                        val dao = com.example.data.db.AppDatabase.getDatabase(context.applicationContext).focusDao()
                        val session = dao.getSessionById(sessionId)
                        val profile = session?.whitelistProfile ?: "STRICT"
                        dao.getWhitelistedAppsList(profile).filter { it.isAllowed }.map { it.packageName }
                    }
                    
                    withContext(Dispatchers.Main) {
                        whitelistedPackages.clear()
                        whitelistedPackages.addAll(apps)
                        Log.d(TAG, "Pending schedule set: sessionId=$sessionId, sessionName=$sessionName, allowedApps=${apps.size}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading whitelisted apps for pending schedule", e)
                }
            }

            // Ensure FocusTimerService pending monitor is running
            try {
                val serviceIntent = Intent(context, com.example.services.FocusTimerService::class.java).apply {
                    action = "ACTION_START_PENDING_MONITOR"
                    putExtra("SESSION_ID", sessionId)
                    putExtra("SESSION_NAME", sessionName)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start pending monitor service from setPendingSchedule", e)
            }
        } else {
            Log.d(TAG, "Pending schedule set: sessionId=$sessionId, sessionName=$sessionName")
        }
    }

    fun clearPendingSchedule() {
        pendingSessionId = null
        pendingSessionName = null
        Log.d(TAG, "Pending schedule cleared.")
    }

    fun hasPendingSchedule(): Boolean = pendingSessionId != null

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
        
        if (isActive) {
            clearPendingSchedule()
        } else {
            FocusLockOverlayManager.dismissOverlay()
        }
    }

    fun getAllowedPackages(): Set<String> {
        return whitelistedPackages.toSet()
    }

    fun isPackageAllowed(context: Context?, packageName: String, ourPackageName: String): Boolean {
        if (packageName.isBlank() || packageName == ourPackageName) {
            return true
        }

        // Camera capture in progress during photo verification -> always allowed!
        if (isCameraVerificationActive && isCameraPackage(packageName)) {
            return true
        }

        // Active phone call in progress -> always allowed!
        if (context != null && isPhoneCallActive(context)) {
            return true
        }

        // Phone calls, Dialer, SMS, Messages, Contacts -> always allowed!
        if (isPhoneCallOrCommunication(packageName)) {
            return true
        }

        // Keyboards and core system utilities -> always allowed!
        if (isSystemUtilityOrKeyboard(packageName)) {
            return true
        }

        // Home Screen / Launchers -> always allowed!
        if (context != null) {
            val launchers = getHomeLaunchers(context)
            if (launchers.contains(packageName) || packageName.lowercase().contains("launcher")) {
                return true
            }
        } else {
            val lower = packageName.lowercase()
            if (lower.contains("launcher") || lower.contains("home")) {
                return true
            }
        }

        if (!isFocusActive && !hasPendingSchedule()) {
            return true
        }

        if (isFocusActive) {
            if (currentLockMode == LockMode.NORMAL) return true
            return whitelistedPackages.contains(packageName)
        }

        // Pending schedule is active: check if opened package is in Strict Schedule Whitelist
        if (hasPendingSchedule()) {
            return whitelistedPackages.contains(packageName)
        }

        return false
    }

    // Overload for backward compatibility when context is not passed
    fun isPackageAllowed(packageName: String, ourPackageName: String): Boolean {
        return isPackageAllowed(null, packageName, ourPackageName)
    }

    fun handleBlockedAppOpened(
        context: Context,
        blockedPackageName: String,
        remainingSeconds: Int = 0,
        subjectName: String = "Deep Focus"
    ) {
        // Double check: if package is actually allowed (Home launcher, Call, SMS, etc.), dismiss overlay
        if (isPackageAllowed(context, blockedPackageName, context.packageName)) {
            FocusLockOverlayManager.dismissOverlay()
            return
        }
        Log.w(TAG, "BLOCKED APP DETECTED: $blockedPackageName. Initiating lock enforcement...")
                
        if (!isFocusActive && hasPendingSchedule()) {
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
        }

        when (currentLockMode) {
            LockMode.SOFT_LOCK -> {
                onDistractionListener?.invoke(blockedPackageName, false)
                FocusLockOverlayManager.dismissOverlay()
                FocusLockOverlayManager.bringAppToFront(
                    context = context,
                    blockedPackage = blockedPackageName,
                    isSoftLock = true
                )
            }
                        
            LockMode.MAXIMUM_LOCK -> {
                onDistractionListener?.invoke(blockedPackageName, true)
                FocusLockOverlayManager.dismissOverlay()
                FocusLockOverlayManager.bringAppToFront(
                    context = context,
                    blockedPackage = blockedPackageName,
                    isSoftLock = false
                )
            }
            else -> {}
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

