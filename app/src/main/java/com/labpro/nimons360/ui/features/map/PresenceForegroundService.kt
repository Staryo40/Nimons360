package com.labpro.nimons360.ui.features.map

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.labpro.nimons360.MainActivity
import com.labpro.nimons360.MainApplication
import com.labpro.nimons360.R
import com.labpro.nimons360.data.enums.InternetStatus
import com.labpro.nimons360.data.model.map.MapPoint
import com.labpro.nimons360.data.model.map.MapSocket
import com.labpro.nimons360.data.model.map.PresenceEvent
import com.labpro.nimons360.data.model.map.PresenceSend
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PresenceForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var locationTracker: LocationTracker
    private lateinit var orientationTracker: OrientationTracker
    private lateinit var batteryTracker: BatteryTracker
    private lateinit var netTracker: NetTracker
    private lateinit var socket: PresenceSocket

    private var location: MapPoint? = null
    private var rotation = 0f
    private var batteryLevel: Int? = null
    private var charging: Boolean? = null
    private var internetStatus: String? = null
    private var sender: Job? = null
    private var trackersStarted = false

    private val app: MainApplication
        get() = application as MainApplication

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        locationTracker = LocationTracker(applicationContext)
        orientationTracker = OrientationTracker(applicationContext)
        batteryTracker = BatteryTracker(applicationContext)
        netTracker = NetTracker(applicationContext)
        socket = PresenceSocket(
            scope = scope,
            token = { app.tokenManager.getToken() },
            onState = ::handleSocketState,
            onEvent = ::ignoreIncomingPresence,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_FULL_NAME)
            ?.takeIf(String::isNotBlank)
            ?.let(app.tokenManager::setPresenceName)

        if (!app.tokenManager.isLocationSharingEnabled() || !hasLocationPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }

        socket.resume()
        socket.connect()
        startTrackers()
        startSender()
        return START_STICKY
    }

    override fun onDestroy() {
        sender?.cancel()
        locationTracker.stop()
        orientationTracker.stop()
        batteryTracker.stop()
        netTracker.stop()
        socket.close()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTrackers() {
        if (trackersStarted) return
        trackersStarted = true
        locationTracker.start(
            onPoint = {
                location = it
                scope.launch(Dispatchers.IO) {
                    app.analyticsRepository.recordLocation(it.latitude, it.longitude)
                }
            },
            onError = { },
        )
        orientationTracker.start { rotation = it }
        batteryTracker.start { level, isCharging ->
            batteryLevel = level
            charging = isCharging
        }
        netTracker.start { status ->
            internetStatus = MapNetMapper.toPayload(status)
            if (status != InternetStatus.NO_INTERNET) socket.connect()
        }
    }

    private fun startSender() {
        if (sender?.isActive == true) return
        sender = scope.launch {
            while (isActive) {
                delay(SEND_INTERVAL_MS)
                if (!app.tokenManager.isLocationSharingEnabled()) {
                    stopSelf()
                    return@launch
                }
                val point = location ?: continue
                val net = internetStatus ?: continue
                socket.sendPresence(
                    PresenceSend(
                        payload = PresenceSend.Payload(
                            name = app.tokenManager.getPresenceName(),
                            latitude = point.latitude,
                            longitude = point.longitude,
                            rotation = rotation,
                            batteryLevel = batteryLevel,
                            isCharging = charging,
                            internetStatus = net,
                        ),
                        timestamp = Instant.now().toString(),
                    )
                )
            }
        }
    }

    private fun handleSocketState(state: MapSocket) {
        if (state is MapSocket.Error && state.message.contains("session", ignoreCase = true)) {
            stopSelf()
        }
    }

    private fun ignoreIncomingPresence(event: PresenceEvent) = Unit

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.presence_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_recenter)
        .setContentTitle(getString(R.string.presence_notification_title))
        .setContentText(getString(R.string.presence_notification_body))
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        )
        .build()

    companion object {
        private const val CHANNEL_ID = "live_location"
        private const val NOTIFICATION_ID = 360
        private const val SEND_INTERVAL_MS = 1_000L
        const val EXTRA_FULL_NAME = "full_name"
    }
}
