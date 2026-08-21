package com.example.mesh

import android.content.Context
import com.example.crypto.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class MeshPeerNode(
    val id: String,
    val name: String,
    val role: String, // e.g. "Direct Peer", "Relay Node", "Field Unit", "Gateway"
    val publicKey: String,
    val distanceMeters: Int,
    val rssi: Int, // e.g. -48 dBm
    val hopCount: Int, // 1 = Direct BLE/Wi-Fi, 2 = 1-Hop Relay, 3 = Multi-Hop Mesh
    val protocol: String, // "BLE Mesh 5.3", "Wi-Fi Direct P2P", "Ad-Hoc Radio"
    val batteryLevel: Int,
    val isConnected: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val radarAngle: Float = 45f,
    val radarDistanceRatio: Float = 0.4f
)

data class MeshPacketLog(
    val id: String = UUID.randomUUID().toString().take(8),
    val packetType: String, // "DIRECT_MSG", "MULTI_HOP_RELAY", "KEY_HANDSHAKE", "SOS_BEACON", "ACK"
    val originNode: String,
    val destinationNode: String,
    val hopRoute: List<String>,
    val payloadBytes: Int,
    val latencyMs: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // "DELIVERED_OFFGRID", "RELAYING", "BROADCASTED"
)

data class MeshSosBroadcast(
    val id: String = UUID.randomUUID().toString().take(8),
    val senderName: String,
    val locationCoords: String,
    val message: String,
    val severity: String = "CRITICAL",
    val timestamp: Long = System.currentTimeMillis(),
    val hopsRelayed: Int = 2
)

data class MeshStats(
    val totalPacketsRelayed: Int = 142,
    val activeMeshNodesCount: Int = 4,
    val cellularDataUsedBytes: Long = 0L, // 0 Bytes! 100% Offline
    val averageHopLatencyMs: Int = 18,
    val meshCoverageRadiusMeters: Int = 450,
    val encryptionStandard: String = "AES-256-GCM Direct-Over-Air"
)

class MeshNetworkManager private constructor(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _isMeshModeEnabled = MutableStateFlow(true)
    val isMeshModeEnabled: StateFlow<Boolean> = _isMeshModeEnabled.asStateFlow()

    private val _isRadarScanning = MutableStateFlow(true)
    val isRadarScanning: StateFlow<Boolean> = _isRadarScanning.asStateFlow()

    private val _activePeers = MutableStateFlow<List<MeshPeerNode>>(emptyList())
    val activePeers: StateFlow<List<MeshPeerNode>> = _activePeers.asStateFlow()

    private val _packetLogs = MutableStateFlow<List<MeshPacketLog>>(emptyList())
    val packetLogs: StateFlow<List<MeshPacketLog>> = _packetLogs.asStateFlow()

    private val _sosAlerts = MutableStateFlow<List<MeshSosBroadcast>>(emptyList())
    val sosAlerts: StateFlow<List<MeshSosBroadcast>> = _sosAlerts.asStateFlow()

    private val _meshStats = MutableStateFlow(MeshStats())
    val meshStats: StateFlow<MeshStats> = _meshStats.asStateFlow()

    private var scanSimulationJob: Job? = null

    init {
        seedInitialMeshPeers()
        startMeshNetworkLoop()
    }

    private fun seedInitialMeshPeers() {
        val initialNodes = listOf(
            MeshPeerNode(
                id = "node_radit_01",
                name = "Raditya Pratama",
                role = "Direct Peer (10m)",
                publicKey = CryptoManager.generateKeyPair().first,
                distanceMeters = 8,
                rssi = -46,
                hopCount = 1,
                protocol = "BLE Mesh 5.3 (High Speed)",
                batteryLevel = 88,
                isConnected = true,
                radarAngle = 45f,
                radarDistanceRatio = 0.28f
            ),
            MeshPeerNode(
                id = "node_sarah_02",
                name = "Sarah Chen",
                role = "Wi-Fi Direct Node",
                publicKey = CryptoManager.generateKeyPair().first,
                distanceMeters = 24,
                rssi = -64,
                hopCount = 1,
                protocol = "Wi-Fi Direct P2P (Lossless)",
                batteryLevel = 92,
                isConnected = true,
                radarAngle = 135f,
                radarDistanceRatio = 0.52f
            ),
            MeshPeerNode(
                id = "node_bima_relay",
                name = "Bima Satria (Relay)",
                role = "Multi-Hop Relay Node",
                publicKey = CryptoManager.generateKeyPair().first,
                distanceMeters = 85,
                rssi = -78,
                hopCount = 2,
                protocol = "Ad-Hoc Mesh Relay",
                batteryLevel = 74,
                isConnected = true,
                radarAngle = 225f,
                radarDistanceRatio = 0.75f
            ),
            MeshPeerNode(
                id = "node_rescue_alpha",
                name = "Posko Darurat Alpha",
                role = "Mesh Base Station",
                publicKey = CryptoManager.generateKeyPair().first,
                distanceMeters = 160,
                rssi = -86,
                hopCount = 3,
                protocol = "Off-Grid LoRa/BLE Bridge",
                batteryLevel = 99,
                isConnected = true,
                radarAngle = 310f,
                radarDistanceRatio = 0.88f
            )
        )
        _activePeers.value = initialNodes

        _packetLogs.value = listOf(
            MeshPacketLog(
                packetType = "KEY_HANDSHAKE",
                originNode = "Local Device",
                destinationNode = "Raditya Pratama",
                hopRoute = listOf("Local", "Raditya"),
                payloadBytes = 256,
                latencyMs = 12,
                status = "DELIVERED_OFFGRID"
            ),
            MeshPacketLog(
                packetType = "MULTI_HOP_RELAY",
                originNode = "Sarah Chen",
                destinationNode = "Posko Darurat Alpha",
                hopRoute = listOf("Sarah", "Bima Relay", "Posko Alpha"),
                payloadBytes = 1024,
                latencyMs = 38,
                status = "DELIVERED_OFFGRID"
            )
        )

        _sosAlerts.value = listOf(
            MeshSosBroadcast(
                senderName = "Tim Lapangan Sektor 3",
                locationCoords = "-6.2088, 106.8456",
                message = "Mode Off-Grid Aktif: Jalur komunikasi lokal terhubung via 3 node mesh tanpa pulsa & internet.",
                severity = "INFO",
                hopsRelayed = 2
            )
        )
    }

    private fun startMeshNetworkLoop() {
        scanSimulationJob?.cancel()
        scanSimulationJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(4000)
                if (_isMeshModeEnabled.value) {
                    // Periodically update RSSI fluctuation to simulate real-world radio signals
                    _activePeers.update { current ->
                        current.map { peer ->
                            val deltaRssi = (-2..2).random()
                            val newRssi = (peer.rssi + deltaRssi).coerceIn(-95, -35)
                            peer.copy(rssi = newRssi, lastSeenTimestamp = System.currentTimeMillis())
                        }
                    }

                    _meshStats.update {
                        it.copy(
                            totalPacketsRelayed = it.totalPacketsRelayed + (1..3).random(),
                            activeMeshNodesCount = _activePeers.value.size
                        )
                    }
                }
            }
        }
    }

    fun toggleMeshMode(enabled: Boolean) {
        _isMeshModeEnabled.value = enabled
        if (enabled) {
            startMeshNetworkLoop()
        } else {
            scanSimulationJob?.cancel()
        }
    }

    fun toggleRadarScan() {
        _isRadarScanning.value = !_isRadarScanning.value
    }

    fun triggerNewPeerDiscovery(name: String, role: String, protocol: String) {
        val newNode = MeshPeerNode(
            id = "node_${UUID.randomUUID().toString().take(6)}",
            name = name,
            role = role,
            publicKey = CryptoManager.generateKeyPair().first,
            distanceMeters = (5..60).random(),
            rssi = (-75..-40).random(),
            hopCount = (1..2).random(),
            protocol = protocol,
            batteryLevel = (60..98).random(),
            isConnected = true,
            radarAngle = (0..360).random().toFloat(),
            radarDistanceRatio = (20..80).random() / 100f
        )
        _activePeers.update { listOf(newNode) + it }

        // Add log
        _packetLogs.update {
            listOf(
                MeshPacketLog(
                    packetType = "DISCOVERY_BEACON",
                    originNode = name,
                    destinationNode = "Local Device",
                    hopRoute = listOf(name, "Local Device"),
                    payloadBytes = 128,
                    latencyMs = (10..30).random(),
                    status = "DELIVERED_OFFGRID"
                )
            ) + it.take(20)
        }
    }

    fun sendOffGridMessage(recipientName: String, content: String): MeshPacketLog {
        val hopCount = if (recipientName.contains("Relay", ignoreCase = true) || recipientName.contains("Alpha", ignoreCase = true)) 2 else 1
        val hopRoute = if (hopCount == 1) listOf("Local Device", recipientName) else listOf("Local Device", "Bima Relay", recipientName)

        val log = MeshPacketLog(
            packetType = "DIRECT_MSG",
            originNode = "Local Device (Offline P2P)",
            destinationNode = recipientName,
            hopRoute = hopRoute,
            payloadBytes = content.toByteArray().size + 128,
            latencyMs = (12..45).random(),
            status = "DELIVERED_OFFGRID"
        )

        _packetLogs.update { listOf(log) + it.take(25) }
        _meshStats.update { it.copy(totalPacketsRelayed = it.totalPacketsRelayed + 1) }
        return log
    }

    fun broadcastSosEmergency(message: String, coords: String) {
        val sos = MeshSosBroadcast(
            senderName = "Anda (Emergency Beacon)",
            locationCoords = coords.ifEmpty { "-6.2088, 106.8456" },
            message = message,
            severity = "CRITICAL",
            timestamp = System.currentTimeMillis(),
            hopsRelayed = 3
        )

        _sosAlerts.update { listOf(sos) + it }

        _packetLogs.update {
            listOf(
                MeshPacketLog(
                    packetType = "SOS_BEACON",
                    originNode = "Local Device (SOS)",
                    destinationNode = "ALL_SURROUNDING_NODES (Mesh Flood)",
                    hopRoute = listOf("Local", "Raditya", "Sarah", "Bima Relay", "Posko Alpha"),
                    payloadBytes = 512,
                    latencyMs = 8,
                    status = "BROADCASTED"
                )
            ) + it.take(25)
        }

        _meshStats.update { it.copy(totalPacketsRelayed = it.totalPacketsRelayed + 5) }
    }

    companion object {
        @Volatile
        private var INSTANCE: MeshNetworkManager? = null

        fun getInstance(context: Context): MeshNetworkManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val scope = CoroutineScope(Dispatchers.Default)
                    val manager = MeshNetworkManager(context.applicationContext, scope)
                    INSTANCE = manager
                    manager
                }
            }
        }
    }
}
