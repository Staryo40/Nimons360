package com.labpro.nimons360.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.labpro.nimons360.MainActivity
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.notification.SubscribeTokenRequest
import com.labpro.nimons360.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NimonsFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = application as MainApplication
        val tokenManager = app.tokenManager
        
        if (tokenManager.isLoggedIn() && tokenManager.isNotificationsEnabled()) {
            serviceScope.launch {
                try {
                    val response = RetrofitClient.apiService.subscribeDeviceToken(
                        SubscribeTokenRequest(token)
                    )
                    if (response.isSuccessful && response.body()?.data?.subscribed == true) {
                        Log.d(TAG, "Successfully subscribed refreshed FCM token to backend")
                    } else {
                        Log.e(TAG, "Failed to subscribe refreshed FCM token: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error subscribing refreshed FCM token: ${e.message}")
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Received FCM message from: ${remoteMessage.from}")

        val app = application as MainApplication
        val tokenManager = app.tokenManager

        // Requirement: "Jika notifikasi dinonaktifkan, pengguna tidak akan menerima notifikasi apapun."
        if (!tokenManager.isNotificationsEnabled()) {
            Log.d(TAG, "Notifications are disabled in profile preferences. Discarding message.")
            return
        }

        // Extract title and body from notification payload or custom data payload
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Family Alert"
        val messageBody = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: remoteMessage.data["message"] ?: ""

        if (messageBody.isNotBlank()) {
            sendNotification(title, messageBody)
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round) // Fallback to launcher icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Android 8.0 Oreo, a notification channel is strictly required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.default_notification_channel_name)
            val channelDescription = "Alerts and greetings from family members"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMessaging"
    }
}
