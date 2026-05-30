package com.aryan.aura.service

import android.app.Notification
import android.app.RemoteInput
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.aryan.aura.util.AICoreHelper
import com.aryan.aura.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuraNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var aiHelper: AICoreHelper

    override fun onCreate() {
        super.onCreate()
        aiHelper = AICoreHelper(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        MainViewModel.addDebugLog("Service Connected ✅")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        MainViewModel.addDebugLog("Service Disconnected ❌")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        
        // Log all packages to help troubleshoot visibility
        MainViewModel.addDebugLog("Seen: ${packageName.substringAfterLast(".")}")

        // Check if it's an ongoing notification (like music or call)
        if (sbn.isOngoing) return

        // Dynamic package detection
        val isWhatsApp = packageName.contains("whatsapp", ignoreCase = true) && MainViewModel.isWhatsAppEnabled
        val isInstagram = packageName.contains("instagram", ignoreCase = true) && MainViewModel.isInstagramEnabled

        if (isWhatsApp || isInstagram) {
            val appName = if (isWhatsApp) "WhatsApp" else "Instagram"
            val extras = sbn.notification.extras
            
            val text = extras.getCharSequence("android.text")?.toString() ?: 
                       extras.getCharSequence("android.bigText")?.toString() ?: ""
            
            val title = extras.getCharSequence("android.title")?.toString() ?: "Someone"

            if (text.isNotEmpty() && !text.contains("Checking", ignoreCase = true)) {
                MainViewModel.addDebugLog("Processing $appName message...")
                
                // Extract RemoteInput for Quick Reply
                val actions = sbn.notification.actions
                if (actions != null) {
                    for (action in actions) {
                        if (action.remoteInputs != null) {
                            for (remoteInput in action.remoteInputs) {
                                MainViewModel.setReplyData(remoteInput, action.actionIntent)
                                break
                            }
                        }
                    }
                }

                serviceScope.launch {
                    try {
                        val result = aiHelper.getEmpatheticSummary("In $appName, $title says: $text")
                        MainViewModel.updateInsight(result)
                    } catch (e: Exception) {
                        MainViewModel.updateInsight("Aura sensed a $appName message but Gemini is resting.")
                    }
                }
            }
        }
    }
}
