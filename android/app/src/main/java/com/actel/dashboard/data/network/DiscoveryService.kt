package com.actel.dashboard.data.network

import com.actel.dashboard.data.model.DiscoveryBeacon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class DiscoveryService {
    private val json = Json { ignoreUnknownKeys = true }
    private var socket: DatagramSocket? = null

    suspend fun startListening(onDiscovered: (ip: String, port: Int, hostname: String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(8088))
                    broadcast = true
                    soTimeout = 3000
                }

                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)

                while (isActive) {
                    try {
                        socket?.receive(packet)
                        val message = String(packet.data, 0, packet.length, Charsets.UTF_8)
                        val beacon = json.decodeFromString<DiscoveryBeacon>(message)

                        if (beacon.service == "actel") {
                            val hostAddress = packet.address.hostAddress ?: ""
                            if (hostAddress.isNotEmpty()) {
                                onDiscovered(hostAddress, beacon.port, beacon.hostname)
                            }
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        // Timeout is expected, continue loop
                    } catch (e: Exception) {
                        // Ignore parse or read transient errors
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        try {
            socket?.close()
            socket = null
        } catch (_: Exception) {}
    }
}
