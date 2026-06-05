package com.labpro.nimons360.ui.features.profile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.labpro.nimons360.R
import com.labpro.nimons360.data.model.map.CustomPin
import com.labpro.nimons360.data.repository.CustomPinRepository
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class CustomPinDownloadService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient()
    private val activeDownloads = AtomicInteger(0)
    private lateinit var repository: CustomPinRepository

    override fun onCreate() {
        super.onCreate()
        repository = CustomPinRepository(applicationContext)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pin = CustomPin.find(intent?.getStringExtra(EXTRA_PIN_ID))
        if (pin == null) {
            stopSelfResult(startId)
            return START_NOT_STICKY
        }

        activeDownloads.incrementAndGet()
        startForeground(notificationId(pin), notification(pin, 0, true))
        scope.launch {
            val result = runCatching { download(pin) }
            publishResult(pin, result.isSuccess)
            val isLastDownload = activeDownloads.decrementAndGet() == 0
            if (isLastDownload) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
            if (result.isSuccess) {
                notifyComplete(pin)
            } else {
                notifyFailed(pin)
            }
            if (isLastDownload) stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun download(pin: CustomPin) {
        val target = repository.file(pin)
        val temporary = repository.temporaryFile(pin)
        val request = Request.Builder().url(pin.url).get().build()

        try {
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
                val body = checkNotNull(response.body)
                val total = body.contentLength()
                body.byteStream().use { input ->
                    temporary.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var copied = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            copied += count
                            if (total > 0L) {
                                val progress = ((copied * 100L) / total).toInt().coerceIn(0, 100)
                                getSystemService(NotificationManager::class.java).notify(
                                    notificationId(pin),
                                    notification(pin, progress, false),
                                )
                            }
                        }
                    }
                }
            }
            check(temporary.length() > 0L) { "Downloaded file is empty" }
            check(BitmapFactory.decodeFile(temporary.absolutePath) != null) {
                "Downloaded file is not a valid image"
            }
            if (target.exists()) target.delete()
            check(temporary.renameTo(target)) { "Unable to store downloaded pin" }
        } finally {
            if (temporary.exists()) temporary.delete()
        }
    }

    private fun publishResult(pin: CustomPin, successful: Boolean) {
        sendBroadcast(
            Intent(ACTION_DOWNLOAD_FINISHED)
                .setPackage(packageName)
                .putExtra(EXTRA_PIN_ID, pin.id)
                .putExtra(EXTRA_SUCCESS, successful)
        )
    }

    private fun notification(pin: CustomPin, progress: Int, indeterminate: Boolean) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle(getString(R.string.pin_downloading_title, pin.label))
            .setContentText(getString(R.string.pin_downloading_body))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, progress, indeterminate)
            .setContentIntent(contentIntent())
            .build()

    private fun notifyComplete(pin: CustomPin) {
        getSystemService(NotificationManager::class.java).notify(
            notificationId(pin),
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle(getString(R.string.pin_download_complete, pin.label))
                .setContentText(getString(R.string.pin_download_select_hint))
                .setAutoCancel(true)
                .setContentIntent(contentIntent())
                .build()
        )
    }

    private fun notifyFailed(pin: CustomPin) {
        getSystemService(NotificationManager::class.java).notify(
            notificationId(pin),
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle(getString(R.string.pin_download_failed, pin.label))
                .setAutoCancel(true)
                .setContentIntent(contentIntent())
                .build()
        )
    }

    private fun contentIntent(): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, CustomizePinActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun createChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.pin_download_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
    }

    private fun notificationId(pin: CustomPin): Int = NOTIFICATION_BASE + pin.id.hashCode().and(0x3ff)

    companion object {
        const val ACTION_DOWNLOAD_FINISHED = "com.labpro.nimons360.PIN_DOWNLOAD_FINISHED"
        const val EXTRA_PIN_ID = "pin_id"
        const val EXTRA_SUCCESS = "success"
        private const val CHANNEL_ID = "custom_pin_downloads"
        private const val NOTIFICATION_BASE = 1_400
    }
}
