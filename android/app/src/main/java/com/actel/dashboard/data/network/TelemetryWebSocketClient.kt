package com.actel.dashboard.data.network

import com.actel.dashboard.data.model.ConnectionState
import com.actel.dashboard.data.model.ControlCommand
import com.actel.dashboard.data.model.SetModeCommand
import com.actel.dashboard.data.model.TelemetryData
import com.actel.dashboard.data.model.TelemetryEnvelope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class TelemetryWebSocketClient(
    private val scope: CoroutineScope
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(5, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var currentHost: String = ""
    private var currentPort: Int = 8080
    private var shouldReconnect: Boolean = true
    private var reconnectJob: Job? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _telemetry = MutableStateFlow<TelemetryData?>(null)
    val telemetry = _telemetry.asStateFlow()

    fun connect(host: String, port: Int = 8080) {
        currentHost = host
        currentPort = port
        shouldReconnect = true
        reconnectJob?.cancel()

        disconnect(clearAutoReconnect = false)

        val url = "ws://$host:$port"
        val request = Request.Builder().url(url).build()

        _connectionState.value = ConnectionState.CONNECTING

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val envelope = json.decodeFromString<TelemetryEnvelope>(text)
                    if (envelope.type == "telemetry" && envelope.data != null) {
                        _telemetry.value = envelope.data
                    }
                } catch (_: Exception) {}
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
                triggerReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.DISCONNECTED
                triggerReconnect()
            }
        })
    }

    private fun triggerReconnect() {
        if (!shouldReconnect || currentHost.isEmpty()) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch(Dispatchers.IO) {
            delay(2500)
            if (isActive && shouldReconnect && _connectionState.value != ConnectionState.CONNECTED) {
                connect(currentHost, currentPort)
            }
        }
    }

    fun sendControlAction(action: String, state: String? = null) {
        val cmd = ControlCommand(type = "control", action = action, state = state)
        val text = json.encodeToString(cmd)
        val wsSent = webSocket?.send(text) ?: false
        Log.d("ACTelWS", "Action '$action' (state: $state) sent via WS: $wsSent")

        // Dual path: Also send via REST API for maximum reliability
        val host = currentHost
        val port = currentPort
        if (host.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
                try {
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val reqBody = text.toRequestBody(mediaType)
                    val req = Request.Builder()
                        .url("http://$host:$port/api/control")
                        .post(reqBody)
                        .build()
                    client.newCall(req).execute().use { resp ->
                        Log.d("ACTelWS", "Action '$action' sent via HTTP status: ${resp.code}")
                    }
                } catch (e: Exception) {
                    Log.w("ACTelWS", "HTTP control dispatch warning: ${e.message}")
                }
            }
        }
    }

    fun setMode(mode: String) {
        val cmd = SetModeCommand(type = "set_mode", mode = mode)
        val text = json.encodeToString(cmd)
        webSocket?.send(text)
    }

    fun disconnect(clearAutoReconnect: Boolean = true) {
        if (clearAutoReconnect) {
            shouldReconnect = false
            reconnectJob?.cancel()
        }
        try {
            webSocket?.close(1000, "Normal Closure")
        } catch (_: Exception) {}
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
