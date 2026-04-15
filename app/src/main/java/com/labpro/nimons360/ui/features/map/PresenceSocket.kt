package com.labpro.nimons360.ui.features.map

import android.util.Log
import com.google.gson.Gson
import com.labpro.nimons360.core.events.AuthEventBus
import com.labpro.nimons360.data.model.map.MapSocket
import com.labpro.nimons360.data.model.map.PresenceEvent
import com.labpro.nimons360.data.model.map.PresenceSend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class PresenceSocket(
    private val scope: CoroutineScope,
    private val token: () -> String?,
    private val onState: (MapSocket) -> Unit,
    private val onEvent: (PresenceEvent) -> Unit,
) {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var socket: WebSocket? = null
    private var retryJob: Job? = null
    private var pingJob: Job? = null
    private var closed = false

    fun connect() {
        if (socket != null || closed) return

        val rawToken = token()
        if (rawToken.isNullOrBlank()) {
            onState(MapSocket.Error("Your session has expired. Please sign in again."))
            scope.launch { AuthEventBus.emitSessionExpired() }
            return
        }

        onState(MapSocket.Connecting)
        val request = Request.Builder()
            .url(URL)
            .addHeader("Authorization", "Bearer $rawToken")
            .build()

        socket = client.newWebSocket(request, SocketListener())
    }

    fun sendPresence(body: PresenceSend) {
        val active = socket ?: return
        active.send(gson.toJson(body))
    }

    fun close() {
        closed = true
        retryJob?.cancel()
        pingJob?.cancel()
        socket?.close(1000, "Map closed")
        socket = null
        onState(MapSocket.Idle)
    }

    fun resume() {
        if (!closed) return
        closed = false
        connect()
    }

    private fun retry() {
        if (closed || retryJob?.isActive == true) return
        retryJob = scope.launch {
            delay(RETRY_MS)
            socket = null
            connect()
        }
    }

    private fun startPing() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive && !closed) {
                delay(PING_MS)
                socket?.send(PING)
            }
        }
    }

    private inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            onState(MapSocket.Connected)
            startPing()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (text == PONG) return

            runCatching { gson.fromJson(text, PresenceEvent::class.java) }
                .onSuccess { event ->
                    if (event.type == EVENT && event.payload != null) {
                        onEvent(event)
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to parse message", error)
                }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            pingJob?.cancel()
            socket = null
            onState(MapSocket.Idle)
            retry()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Socket failure", t)
            pingJob?.cancel()
            socket = null

            if (response?.code == 401) {
                onState(MapSocket.Error("Your session has expired. Please sign in again."))
                scope.launch { AuthEventBus.emitSessionExpired() }
                return
            }

            onState(MapSocket.Error("Live map connection lost. Reconnecting..."))
            retry()
        }
    }

    companion object {
        private const val TAG = "PresenceSocket"
        private const val URL = "wss://mad.labpro.hmif.dev/ws/live"
        private const val EVENT = "member_presence_updated"
        private const val PING = "ping"
        private const val PONG = "pong"
        private const val RETRY_MS = 3_000L
        private const val PING_MS = 15_000L
    }
}
