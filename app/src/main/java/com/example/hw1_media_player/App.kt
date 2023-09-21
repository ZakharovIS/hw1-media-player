package com.example.hw1_media_player

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            "Media Player",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(channel)
    }
}