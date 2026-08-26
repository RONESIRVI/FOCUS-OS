package com.example.util

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import com.example.services.FocusAccessibilityService

data class PermissionItemState(
    val id: String,
    val title: String,
    val category: String, // "CRITICAL", "STABILITY", "SUPPORTING"
    val description: String,
    val isGranted: Boolean,
    val isSystemMandatory: Boolean = true
)

object LockPermissionHelper {

    // 1. Accessibility Service
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC or AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = FocusAccessibilityService::class.java.name
        return enabledServices.any { 
            it.resolveInfo.serviceInfo.packageName == context.packageName && 
            (it.resolveInfo.serviceInfo.name == expectedServiceName || it.id.contains(expectedServiceName))
        }
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppSettings(context)
        }
    }

    // 2. Usage Stats (PACKAGE_USAGE_STATS)
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageStatsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    // 3. Query All Packages
    fun hasQueryAllPackagesPermission(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            installed.size > 5
        } catch (e: Exception) {
            false
        }
    }

    // 4. Foreground Service & Special Use
    fun isForegroundServiceConfigured(): Boolean {
        return true // Declared in Manifest & configured with specialUse
    }

    // 6. Battery Optimization Exemption
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        }
        return true
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                openBatteryOptimizationSettings(context)
            }
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                openAppSettings(context)
            }
        }
    }

    // 7. Run at Startup (RECEIVE_BOOT_COMPLETED)
    fun hasBootPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED
    }

    // 8. Draw Over Other Apps (SYSTEM_ALERT_WINDOW)
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
            }
        }
    }

    // 9. Notifications (POST_NOTIFICATIONS)
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun openNotificationSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                openAppSettings(context)
            }
        } catch (e: Exception) {
            openAppSettings(context)
        }
    }

    // 10. Schedule Exact Alarms
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                openAppSettings(context)
            }
        }
    }

    // General App Settings fallback
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignored
        }
    }

    // Full system permission health status
    fun getAllPermissionsStatus(context: Context): List<PermissionItemState> {
        return listOf(
            PermissionItemState(
                id = "ACCESSIBILITY",
                title = "1. Accessibility Service",
                category = "CRITICAL",
                description = "Instantly redirects away from blocked distracting apps to keep your study focus unbroken.",
                isGranted = isAccessibilityServiceEnabled(context)
            ),
            PermissionItemState(
                id = "USAGE_ACCESS",
                title = "2. Usage Access",
                category = "CRITICAL",
                description = "Monitors foreground application activity to detect distraction attempts in real time.",
                isGranted = hasUsageStatsPermission(context)
            ),
            PermissionItemState(
                id = "QUERY_PACKAGES",
                title = "3. Query All Packages",
                category = "CRITICAL",
                description = "Loads all installed device apps to configure customized study whitelist & blocklists.",
                isGranted = hasQueryAllPackagesPermission(context)
            ),
            PermissionItemState(
                id = "FOREGROUND_SERVICE",
                title = "4. Foreground Service",
                category = "STABILITY",
                description = "Keeps study timer, anti-cheat lockdown, and ambient sound engine running persistently.",
                isGranted = isForegroundServiceConfigured()
            ),
            PermissionItemState(
                id = "SPECIAL_USE",
                title = "5. Special Use FGS Type",
                category = "STABILITY",
                description = "Designated high-priority background status for continuous focus lockdown.",
                isGranted = true
            ),
            PermissionItemState(
                id = "BATTERY_OPT",
                title = "6. Ignore Battery Optimization",
                category = "STABILITY",
                description = "Prevents aggressive Android OEM battery savers and Doze mode from killing the focus session.",
                isGranted = isIgnoringBatteryOptimizations(context)
            ),
            PermissionItemState(
                id = "BOOT_STARTUP",
                title = "7. Run at Startup (Boot)",
                category = "STABILITY",
                description = "Restores all scheduled study timers and alarms automatically after phone reboots.",
                isGranted = hasBootPermission(context)
            ),
            PermissionItemState(
                id = "OVERLAY",
                title = "8. Draw Over Other Apps",
                category = "SUPPORTING",
                description = "Displays the security lock shield directly over unauthorized apps when opened.",
                isGranted = hasOverlayPermission(context)
            ),
            PermissionItemState(
                id = "NOTIFICATIONS",
                title = "9. Notifications",
                category = "SUPPORTING",
                description = "Displays active countdown bar and 2-minute pre-schedule study alerts.",
                isGranted = hasNotificationPermission(context)
            ),
            PermissionItemState(
                id = "EXACT_ALARMS",
                title = "10. Schedule Exact Alarms",
                category = "SUPPORTING",
                description = "Triggers planned study sessions at the exact scheduled second.",
                isGranted = canScheduleExactAlarms(context)
            )
        )
    }
}
