// IntelliJ
// src/main/kotlin/com/marketplace/service/FcmService.kt
package com.marketplace.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message

object FcmService {

    fun initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = FcmService::class.java
                .getResourceAsStream("/firebase-service-account.json")
                ?: throw IllegalStateException("Firebase service account file not found")

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            FirebaseApp.initializeApp(options)
        }
    }

    fun sendNotification(
        token: String,
        title: String,
        body: String,
        badgeCount: Int = 1
    ) {
        try {
            // Data-only message — no Notification block.
            // This ensures onMessageReceived() always fires on the device,
            // even when the app is in the background or killed.
            // The Android app builds and shows the notification itself,
            // which means our channel settings (sound, vibration) are always applied.
            val message = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("body", body)
                .putData("badge", badgeCount.toString())
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build()
                )
                .build()

            FirebaseMessaging.getInstance().send(message)
            println("FCM notification sent successfully")
        } catch (e: Exception) {
            println("FCM send failed: ${e.message}")
        }
    }
}