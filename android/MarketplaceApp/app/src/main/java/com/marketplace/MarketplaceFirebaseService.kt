package com.marketplace

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.marketplace.api.FcmTokenRequest
import com.marketplace.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MarketplaceFirebaseService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "marketplace_messages"
        const val CHANNEL_NAME = "Messages"
        private var notificationId = 0

        fun clearAllNotifications(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }

        fun setBadgeCount(context: Context, count: Long) {
            if (count <= 0) {
                clearAllNotifications(context)
                return
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    setShowBadge(true)
                    setSound(null, null)
                    enableLights(false)
                    enableVibration(false)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("$count unread message${if (count > 1) "s" else ""}")
                .setContentText("Tap to view your messages")
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(pendingIntent)
                .setNumber(count.toInt())
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setSilent(true)
                .build()

            notificationManager.notify(999, notification)
        }

        // Returns true if the app is currently visible in the foreground
        fun isAppInForeground(context: Context): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses ?: return false
            val packageName = context.packageName
            return appProcesses.any {
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && it.processName == packageName
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "New Message"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "You have a new message"

        val badgeCount = remoteMessage.data["badge"]?.toIntOrNull() ?: 1

        // If the app is in the foreground the chat screen is actively polling
        // and will mark messages as read — do not show a notification or set the badge
        if (isAppInForeground(this)) {
            return
        }

        createNotificationChannel()
        showNotification(title, body, badgeCount)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM_TOKEN", "Token rotated — new token: $token")

        serviceScope.launch {
            try {
                RetrofitClient.authApi.saveFcmToken(FcmTokenRequest(token))
                android.util.Log.d("FCM_TOKEN", "Rotated token saved to backend successfully")
            } catch (e: Exception) {
                android.util.Log.d("FCM_TOKEN", "Rotated token not saved (user likely not logged in): ${e.message}")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, body: String, badgeCount: Int) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setNumber(badgeCount)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId++, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}