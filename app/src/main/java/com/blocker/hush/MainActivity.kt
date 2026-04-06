package com.blocker.hush

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        
        val btnLogs = Button(this).apply { text = "View Session Logs" }
        val listView = ListView(this)
        
        val sharedPrefs = getSharedPreferences("BlockList", Context.MODE_PRIVATE)
        val blockedApps = sharedPrefs.getStringSet("blocked_packages", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        // Filter: Only User Apps (exclude FLAG_SYSTEM)
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .sortedBy { it.loadLabel(packageManager).toString() }

        val adapter = object : ArrayAdapter<ApplicationInfo>(this, android.R.layout.simple_list_item_multiple_choice, apps) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent) as CheckedTextView
                val app = getItem(position)
                view.text = app?.loadLabel(packageManager)
                view.isChecked = blockedApps.contains(app?.packageName)
                return view
            }
        }

        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.setOnItemClickListener { _, _, position, _ ->
            val pkg = apps[position].packageName
            if (blockedApps.contains(pkg)) blockedApps.remove(pkg) else blockedApps.add(pkg)
            sharedPrefs.edit().putStringSet("blocked_packages", blockedApps).apply()
        }

        layout.addView(btnLogs)
        layout.addView(listView)
        setContentView(layout)

        if (!isNotificationServiceEnabled()) {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        btnLogs.setOnClickListener {
            val logString = if (BlockerService.sessionLogs.isEmpty()) "No logs yet." 
                           else BlockerService.sessionLogs.joinToString("\n\n")
            android.app.AlertDialog.Builder(this)
                .setTitle("Session Block History")
                .setMessage(logString)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val names = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return names?.contains(packageName) == true
    }
}
