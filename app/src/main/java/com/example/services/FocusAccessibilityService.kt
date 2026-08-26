package com.example.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.util.FocusLockManager

class FocusAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            
            val openedPackage = event.packageName?.toString() ?: return

            if (FocusLockManager.isFocusActive) {
                val isAllowed = FocusLockManager.isPackageAllowed(openedPackage, packageName)
                if (!isAllowed) {
                    Log.w(TAG, "Accessibility detected blocked app launch: $openedPackage")
                    FocusLockManager.handleBlockedAppOpened(this, openedPackage)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "FocusAccessibilityService interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "FocusAccessibilityService connected & monitoring active")
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 100
        }
        this.serviceInfo = info
    }

    companion object {
        private const val TAG = "FocusAccessibility"
    }
}
