package com.actel.dashboard.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.actel.dashboard.data.model.ConnectionState
import com.actel.dashboard.data.network.DiscoveryService
import com.actel.dashboard.data.network.TelemetryWebSocketClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("actel_settings", Context.MODE_PRIVATE)

    private val wsClient = TelemetryWebSocketClient(viewModelScope)
    private val discoveryService = DiscoveryService()

    val telemetryState = wsClient.telemetry
    val connectionState = wsClient.connectionState

    private val _discoveredHost = MutableStateFlow<String?>(null)
    val discoveredHost = _discoveredHost.asStateFlow()

    private val _discoveredHostname = MutableStateFlow<String?>(null)
    val discoveredHostname = _discoveredHostname.asStateFlow()

    private val _currentHost = MutableStateFlow(prefs.getString("saved_host", "") ?: "")
    val currentHost = _currentHost.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean("haptic_enabled", true))
    val hapticEnabled = _hapticEnabled.asStateFlow()

    private val _hapticIntensity = MutableStateFlow(prefs.getFloat("haptic_intensity", 0.85f))
    val hapticIntensity = _hapticIntensity.asStateFlow()

    private val _surfaceHapticEnabled = MutableStateFlow(prefs.getBoolean("surface_haptic_enabled", true))
    val surfaceHapticEnabled = _surfaceHapticEnabled.asStateFlow()

    private val _currentMode = MutableStateFlow("AUTO")
    val currentMode = _currentMode.asStateFlow()

    init {
        startDiscovery()
        val savedHost = _currentHost.value
        if (savedHost.isNotBlank()) {
            connect(savedHost, 8080)
        }
    }

    private fun startDiscovery() {
        viewModelScope.launch {
            discoveryService.startListening { ip, port, hostname ->
                _discoveredHost.value = ip
                _discoveredHostname.value = hostname

                // If not connected and no manual host yet, auto connect!
                if (connectionState.value == ConnectionState.DISCONNECTED && _currentHost.value.isEmpty()) {
                    connect(ip, port)
                }
            }
        }
    }

    fun connect(host: String, port: Int = 8080) {
        if (host.isBlank()) return
        _currentHost.value = host
        prefs.edit().putString("saved_host", host).apply()
        wsClient.connect(host, port)
    }

    fun disconnect() {
        wsClient.disconnect()
    }

    fun triggerAction(action: String, state: String? = null) {
        wsClient.sendControlAction(action, state)
    }

    fun setMode(mode: String) {
        _currentMode.value = mode
        wsClient.setMode(mode)
    }

    fun toggleHaptic(enabled: Boolean) {
        _hapticEnabled.value = enabled
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
    }

    fun setHapticIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0.2f, 1.0f)
        _hapticIntensity.value = clamped
        prefs.edit().putFloat("haptic_intensity", clamped).apply()
    }

    fun toggleSurfaceHaptic(enabled: Boolean) {
        _surfaceHapticEnabled.value = enabled
        prefs.edit().putBoolean("surface_haptic_enabled", enabled).apply()
    }

    override fun onCleared() {
        super.onCleared()
        discoveryService.stop()
        wsClient.disconnect()
    }
}
