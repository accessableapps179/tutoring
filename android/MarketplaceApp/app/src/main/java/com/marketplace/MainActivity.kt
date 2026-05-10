// app/src/main/java/com/marketplace/MainActivity.kt
package com.marketplace

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.marketplace.ui.navigation.AppNavGraph
import com.marketplace.ui.theme.MarketplaceAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestRuntimePermissions()
        logFcmToken()

        setContent {
            MarketplaceAppTheme {
                AppNavGraph()
            }
        }
    }

    /** Requests notification + AV permissions on API 33+. */
    private fun requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    /** Logs the current FCM token to Logcat for debugging. */
    private fun logFcmToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
            .token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("FCM_TOKEN", "Token: ${task.result}")
                }
            }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}