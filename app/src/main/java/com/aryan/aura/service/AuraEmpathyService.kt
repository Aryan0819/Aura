package com.aryan.aura.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aryan.aura.R

class AuraEmpathyService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Create a Foreground Notification so Android doesn't kill the service
        val notification = NotificationCompat.Builder(this, "AURA_CHANNEL")
            .setContentTitle("Aura is Active")
            .setContentText("Protecting your cognitive load...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)

        // 2. Start Listening to Stress Levels
        monitorUserVibes()

        return START_STICKY
    }

    private fun monitorUserVibes() {
        // In a real build, connect to Health Connect API here
        // If HeartRate > 100 && User is on phone -> Trigger EmpathyOverlay
    }

    override fun onBind(intent: Intent?): IBinder? = null
}