package com.franlop.android_firebase

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class AndroidFirebaseApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Canal de notificaciones para Firebase Cloud Messaging (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fcmChannel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                "FCM notification channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(fcmChannel)
        }
    }
}
