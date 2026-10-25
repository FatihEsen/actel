package com.actel.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ControlCommand(
    val type: String = "control",
    val action: String,
    val state: String? = null
)

@Serializable
data class SetModeCommand(
    val type: String = "set_mode",
    val mode: String
)

@Serializable
data class DiscoveryBeacon(
    val service: String = "",
    val version: String = "",
    val port: Int = 8080,
    val hostname: String = "",
    val mode: String = ""
)

enum class ConnectionState {
    DISCONNECTED,
    SEARCHING,
    CONNECTING,
    CONNECTED
}
