package com.blocker.hush

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class BlockerService : NotificationListenerService() {

    companion object {
        val sessionLogs = mutableListOf<String>() // Temporary in-memory logs
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        // 1. Skip if it's a system app
        if (isSystemApp(packageName)) return

        val sharedPrefs = getSharedPreferences("BlockList", Context.MODE_PRIVATE)
        val blockedApps = sharedPrefs.getStringSet("blocked_packages", emptySet())

        if (blockedApps?.contains(packageName) == true) {
            cancelNotification(sbn.key) // Remove from status bar
            
            val title = sbn.notification.extras.getString("android.title") ?: "No Title"
            val text = sbn.notification.extras.getString("android.text") ?: ""
            sessionLogs.add(0, "[$packageName] $title: $text") // Add to top
        }
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val ai = packageManager.getApplicationInfo(packageName, 0)
            (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) { true }
    }
}
