package com.blocker.hush

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.view.accessibility.AccessibilityEvent
import android.content.Context
import android.content.pm.ApplicationInfo

class BlockerService : AccessibilityService() {

    companion object {
        val sessionLogs = mutableListOf<String>()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Skip system apps
            if (isSystemApp(packageName)) return

            val sharedPrefs = getSharedPreferences("BlockList", Context.MODE_PRIVATE)
            val blockedApps = sharedPrefs.getStringSet("blocked_packages", emptySet())

            if (blockedApps?.contains(packageName) == true) {
                // Extract notification data
                val notification = event.parcelableData as? Notification
                val extras = notification?.extras
                val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "No Title"
                val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

                sessionLogs.add(0, "[$packageName] $title: $text")
                
                // Note: AccessibilityService cannot programmatically "delete" a notification 
                // from the tray like NotificationListenerService can. 
                // It functions here as a stealth logger for restricted devices.
            }
        }
    }

    override fun onInterrupt() {}

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val ai = packageManager.getApplicationInfo(packageName, 0)
            (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) { true }
    }
}
