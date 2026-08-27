package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import java.util.concurrent.atomic.AtomicBoolean

object FocusLockOverlayManager {
    private const val TAG = "FocusLockOverlayManager"

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val isShowing = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun showBlockedOverlay(
        context: Context,
        blockedPackage: String,
        remainingSeconds: Int,
        subjectName: String,
        allowedPackages: List<Pair<String, String>> = emptyList(), // Pair(packageName, appName)
        isSoftLock: Boolean = false
    ) {
        if (!Settings.canDrawOverlays(context)) {
            Log.w(TAG, "Cannot draw overlays: permission not granted.")
            bringAppToFront(context, blockedPackage)
            return
        }

        mainHandler.post {
            try {
                if (isShowing.get() && overlayView != null) {
                    updateOverlayContent(context, blockedPackage, remainingSeconds, subjectName, allowedPackages)
                    return@post
                }

                windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                
                val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutParamsType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                }

                val view = createOverlayView(context, blockedPackage, remainingSeconds, subjectName, allowedPackages, isSoftLock)
                windowManager?.addView(view, params)
                overlayView = view
                isShowing.set(true)
                Log.d(TAG, "Blocked overlay displayed successfully for: $blockedPackage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add overlay view", e)
                bringAppToFront(context, blockedPackage)
            }
        }
    }

    fun dismissOverlay() {
        mainHandler.post {
            try {
                if (isShowing.get() && overlayView != null && windowManager != null) {
                    windowManager?.removeView(overlayView)
                    overlayView = null
                    isShowing.set(false)
                    Log.d(TAG, "Blocked overlay dismissed.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove overlay view", e)
            }
        }
    }

    fun showPendingScheduleOverlay(
        context: Context,
        blockedPackage: String,
        sessionName: String,
        sessionId: Long? = null
    ) {
        if (!Settings.canDrawOverlays(context)) {
            Log.w(TAG, "Cannot draw overlays for pending schedule: permission not granted.")
            bringAppToFront(context, blockedPackage, sessionId)
            return
        }

        mainHandler.post {
            try {
                if (isShowing.get() && overlayView != null) {
                    return@post
                }

                windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                
                val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutParamsType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                }

                val view = createPendingScheduleOverlayView(context, blockedPackage, sessionName, sessionId)
                windowManager?.addView(view, params)
                overlayView = view
                isShowing.set(true)
                Log.d(TAG, "Pending schedule overlay displayed successfully for: $blockedPackage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add pending schedule overlay view", e)
                bringAppToFront(context, blockedPackage, sessionId)
            }
        }
    }

    private fun bringAppToFront(context: Context, blockedPackage: String, sessionId: Long? = null) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (sessionId != null) {
                    putExtra("START_SESSION_ID", sessionId)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting MainActivity from background", e)
        }
    }

    private fun createOverlayView(
        context: Context,
        blockedPackage: String,
        remainingSeconds: Int,
        subjectName: String,
        allowedPackages: List<Pair<String, String>>,
        isSoftLock: Boolean
    ): View {
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#F50F172A")) // 96% slate dark
            setPadding(48, 64, 48, 64)
        }

        // Shield / Warning Badge
        val badge = TextView(context).apply {
            text = "🛡️ STRICT FOCUS LOCK ACTIVE"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#F59E0B")) // Warning amber
            gravity = Gravity.CENTER
            setPadding(24, 12, 24, 12)
            setBackgroundColor(android.graphics.Color.parseColor("#33F59E0B"))
        }
        rootLayout.addView(badge)

        // Blocked App Warning Title
        val readableName = getReadableAppName(context, blockedPackage)
        val titleView = TextView(context).apply {
            id = View.generateViewId()
            tag = "overlay_title"
            text = "⚠️ '$readableName' is BLOCKED!"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 12)
        }
        rootLayout.addView(titleView)

        // Subtitle / Subject message
        val subjectMsg = if (subjectName.isNotBlank()) subjectName else "Deep Study"
        val subtitleView = TextView(context).apply {
            text = "Social media and distracting apps are restricted during your '$subjectMsg' focus session."
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#94A3B8")) // Slate 400
            gravity = Gravity.CENTER
            setPadding(16, 0, 16, 24)
        }
        rootLayout.addView(subtitleView)

        // Timer Display
        val timeStr = formatTimer(remainingSeconds)
        val timerView = TextView(context).apply {
            id = View.generateViewId()
            tag = "overlay_timer"
            text = "⏱️ $timeStr REMAINING"
            textSize = 28f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#38BDF8")) // Sky blue
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        rootLayout.addView(timerView)

        // Primary Action: Return to Focus Timer
        val returnBtn = Button(context).apply {
            text = if (isSoftLock) "RETURN TO STUDY APP" else "RETURN TO FOCUS TIMER"
            setBackgroundColor(android.graphics.Color.parseColor("#0284C7")) // Primary blue
            setOnClickListener {
                dismissOverlay()
                bringAppToFront(context, blockedPackage)
            }
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setPadding(32, 24, 32, 24)
        }
        rootLayout.addView(returnBtn)

        // Allowed study apps section
        if (allowedPackages.isNotEmpty()) {
            val allowedLabel = TextView(context).apply {
                text = "OR OPEN ALLOWED STUDY APP:"
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#64748B"))
                gravity = Gravity.CENTER
                setPadding(0, 36, 0, 16)
            }
            rootLayout.addView(allowedLabel)

            val appsContainer = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            allowedPackages.take(3).forEach { (pkg, name) ->
                val appBtn = Button(context).apply {
                    text = "📖 $name"
                    textSize = 12f
                    setTextColor(android.graphics.Color.WHITE)
                    setBackgroundColor(android.graphics.Color.parseColor("#1E293B"))
                    setOnClickListener {
                        dismissOverlay()
                        FocusLockManager.launchAllowedApp(context, pkg)
                    }
                }
                appsContainer.addView(appBtn)
            }
            rootLayout.addView(appsContainer)
        }

        return rootLayout
    }

    private fun createPendingScheduleOverlayView(
        context: Context,
        blockedPackage: String,
        sessionName: String,
        sessionId: Long?
    ): View {
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#F50F172A")) // 96% slate dark
            setPadding(48, 64, 48, 64)
        }

        // Shield / Pending Badge
        val badge = TextView(context).apply {
            text = "⏳ SCHEDULED SESSION PENDING"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#F59E0B")) // Amber
            gravity = Gravity.CENTER
            setPadding(24, 12, 24, 12)
            setBackgroundColor(android.graphics.Color.parseColor("#33F59E0B"))
        }
        rootLayout.addView(badge)

        // Blocked App Title
        val readableName = getReadableAppName(context, blockedPackage)
        val titleView = TextView(context).apply {
            text = "⚠️ '$readableName' is Restricted!"
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 12)
        }
        rootLayout.addView(titleView)

        // Subtitle / Prompt
        val subtitleView = TextView(context).apply {
            text = "Your scheduled focus session '$sessionName' is pending! Please start your study session now."
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#94A3B8")) // Slate 400
            gravity = Gravity.CENTER
            setPadding(16, 0, 16, 32)
        }
        rootLayout.addView(subtitleView)

        // Primary Action: Start Session Now
        val startBtn = Button(context).apply {
            text = "🚀 START SCHEDULED SESSION NOW"
            setBackgroundColor(android.graphics.Color.parseColor("#10B981")) // Emerald green
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setPadding(32, 24, 32, 24)
            setOnClickListener {
                dismissOverlay()
                bringAppToFront(context, blockedPackage, sessionId)
            }
        }
        rootLayout.addView(startBtn)

        return rootLayout
    }

    private fun updateOverlayContent(
        context: Context,
        blockedPackage: String,
        remainingSeconds: Int,
        subjectName: String,
        allowedPackages: List<Pair<String, String>>
    ) {
        val root = overlayView as? LinearLayout ?: return
        val title = root.findViewWithTag<TextView>("overlay_title")
        val timer = root.findViewWithTag<TextView>("overlay_timer")

        val readableName = getReadableAppName(context, blockedPackage)
        title?.text = "⚠️ '$readableName' is BLOCKED!"
        timer?.text = "⏱️ ${formatTimer(remainingSeconds)} REMAINING"
    }

    private fun getReadableAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            when {
                packageName.contains("instagram", ignoreCase = true) -> "Instagram"
                packageName.contains("youtube", ignoreCase = true) -> "YouTube"
                packageName.contains("facebook", ignoreCase = true) -> "Facebook"
                packageName.contains("whatsapp", ignoreCase = true) -> "WhatsApp"
                packageName.contains("twitter", ignoreCase = true) || packageName.contains("x.android", ignoreCase = true) -> "X / Twitter"
                packageName.contains("tiktok", ignoreCase = true) -> "TikTok"
                packageName.contains("snapchat", ignoreCase = true) -> "Snapchat"
                packageName.contains("chrome", ignoreCase = true) -> "Browser"
                else -> packageName.substringAfterLast(".")
            }
        }
    }

    private fun formatTimer(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }
}
