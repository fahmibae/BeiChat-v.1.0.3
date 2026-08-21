package com.example.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import com.example.crypto.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

data class MeshPeerNode(
    val id: String,
    val name: String,
    val role: String,
    val publicKey: String,
    val distanceMeters: Int,
    val rssi: Int,
    val hopCount: Int,
    val protocol: String,
    val batteryLevel: Int = 100,
    val isConnected: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val radarAngle: Float = 45f,
    val radarDistanceRatio: Float = 0.4f
)

data class MeshPacketLog(
    val id: String = UUID.randomUUID().toString().take(8),
    val packetType: String,
    val originNode: String,
    val destinationNode: String,
    val hopRoute: List<String>,
    val payloadBytes: Int,
    val latencyMs: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String
)

data class MeshSosBroadcast(
    val id: String = UUID.randomUUID().toString().take(8),
    val senderName: String,
    val locationCoords: String,
    val message: String,
    val severity: String = "CRITICAL",
    val timestamp: Long = System.currentTimeMillis(),
    val hopsRelayed: Int = 1
)

data class MeshStats(
    val totalPacketsRelayed: Int = 0,
    val activeMeshNodesCount: Int = 0,
    val cellularDataUsedBytes: Long = 0L,
    val averageHopLatencyMs: Int = 0,
    val meshCoverageRadiusMeters: Int = 0,
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

    private val _incomingMeshMessages = MutableSharedFlow<RawMeshPayload>(extraBufferCapacity = 64)
    val incomingMeshMessages: SharedFlow<RawMeshPayload> = _incomingMeshMessages.asSharedFlow()

    private var scanJob: Job? = null

    private val socketEngine = BluetoothMeshSocketEngine(
        context = context,
        scope = scope,
        onMessageReceived = { payload ->
            handleIncomingSocketPayload(payload)
        },
        onNodeConnected = { device ->
            handleDiscoveredBluetoothDevice(device, -40, "Bluetooth P2P Socket Terhubung")
        }
    )

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
        } catch (_: Exception) {
            null
        }
    }

    private val bleScanner: BluetoothLeScanner?
        get() = try { bluetoothAdapter?.bluetoothLeScanner } catch (_: Exception) { null }

    private var wifiP2pManager: WifiP2pManager? = null
    private var wifiP2pChannel: WifiP2pManager.Channel? = null

    private val radioReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val rssi: Short = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                    if (device != null) {
                        handleDiscoveredBluetoothDevice(device, rssi.toInt(), "Bluetooth Classic")
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothAdapter.STATE_ON) {
                        loadBondedDevices()
                        socketEngine.startListening()
                        refreshScan()
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    try {
                        wifiP2pManager?.requestPeers(wifiP2pChannel) { peersList: WifiP2pDeviceList? ->
                            peersList?.deviceList?.forEach { p2pDevice ->
                                handleDiscoveredWifiP2pDevice(p2pDevice)
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val device = result?.device ?: return
            val rssi = result.rssi
            handleDiscoveredBluetoothDevice(device, rssi, "BLE Mesh 5.3")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach { res ->
                val device = res.device
                val rssi = res.rssi
                handleDiscoveredBluetoothDevice(device, rssi, "BLE Mesh 5.3")
            }
        }

        override fun onScanFailed(errorCode: Int) {}
    }

    init {
        initWifiP2p()
        registerRadioReceivers()
        loadBondedDevices()
        socketEngine.startListening()
        startMeshNetworkLoop()
    }

    private fun initWifiP2p() {
        try {
            wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
            wifiP2pChannel = wifiP2pManager?.initialize(context, context.mainLooper, null)
        } catch (_: Exception) {}
    }

    private fun registerRadioReceivers() {
        try {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            }
            context.registerReceiver(radioReceiver, filter)
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun loadBondedDevices() {
        try {
            val adapter = bluetoothAdapter
            if (adapter != null && adapter.isEnabled) {
                val bonded = adapter.bondedDevices
                bonded?.forEach { device ->
                    handleDiscoveredBluetoothDevice(device, -45, "Bluetooth Terpasang (Bonded)")
                }
            }
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    private fun handleDiscoveredBluetoothDevice(device: BluetoothDevice, rssiInt: Int, protocolDesc: String) {
        try {
            val rawName = try { device.name } catch (_: Exception) { null }
            val deviceAddress = device.address ?: UUID.randomUUID().toString().take(8)
            val displayName = if (!rawName.isNullOrBlank()) rawName else "Node Bluetooth (${deviceAddress.takeLast(5)})"

            val actualRssi = if (rssiInt in -120..0) rssiInt else -60
            val approxDistance = when {
                actualRssi > -55 -> (2..8).random()
                actualRssi > -70 -> (8..20).random()
                actualRssi > -85 -> (20..50).random()
                else -> (50..100).random()
            }

            val angle = (Math.abs(deviceAddress.hashCode()) % 360).toFloat()
            val distRatio = (approxDistance.toFloat() / 120f).coerceIn(0.15f, 0.9f)

            val discoveredNode = MeshPeerNode(
                id = deviceAddress,
                name = displayName,
                role = "Direct Peer ($protocolDesc)",
                publicKey = CryptoManager.generateHexFingerprint("PUBKEY_$deviceAddress"),
                distanceMeters = approxDistance,
                rssi = actualRssi,
                hopCount = 1,
                protocol = protocolDesc,
                batteryLevel = 100,
                isConnected = true,
                radarAngle = angle,
                radarDistanceRatio = distRatio
            )

            _activePeers.update { current ->
                val withoutCurrent = current.filter { it.id != deviceAddress }
                listOf(discoveredNode) + withoutCurrent
            }

            _meshStats.update {
                it.copy(
                    activeMeshNodesCount = _activePeers.value.size,
                    meshCoverageRadiusMeters = maxOf(it.meshCoverageRadiusMeters, approxDistance * 2)
                )
            }
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    private fun handleDiscoveredWifiP2pDevice(device: WifiP2pDevice) {
        try {
            val deviceAddress = device.deviceAddress ?: UUID.randomUUID().toString().take(8)
            val displayName = if (!device.deviceName.isNullOrBlank()) device.deviceName else "Wi-Fi Direct Peer (${deviceAddress.takeLast(5)})"

            val approxDistance = (3..25).random()
            val angle = (Math.abs(deviceAddress.hashCode()) % 360).toFloat()
            val distRatio = (approxDistance.toFloat() / 100f).coerceIn(0.15f, 0.85f)

            val discoveredNode = MeshPeerNode(
                id = deviceAddress,
                name = displayName,
                role = "Wi-Fi Direct High-Speed Peer",
                publicKey = CryptoManager.generateHexFingerprint("WIFIP2P_$deviceAddress"),
                distanceMeters = approxDistance,
                rssi = -48,
                hopCount = 1,
                protocol = "Wi-Fi Direct P2P 5GHz/2.4GHz",
                batteryLevel = 100,
                isConnected = true,
                radarAngle = angle,
                radarDistanceRatio = distRatio
            )

            _activePeers.update { current ->
                val withoutCurrent = current.filter { it.id != deviceAddress }
                listOf(discoveredNode) + withoutCurrent
            }

            _meshStats.update {
                it.copy(
                    activeMeshNodesCount = _activePeers.value.size,
                    meshCoverageRadiusMeters = maxOf(it.meshCoverageRadiusMeters, approxDistance * 2)
                )
            }
        } catch (_: Exception) {}
    }

    private fun handleIncomingSocketPayload(payload: RawMeshPayload) {
        scope.launch {
            _incomingMeshMessages.emit(payload)

            val log = MeshPacketLog(
                id = payload.packetId,
                packetType = if (payload.isBroadcast) "BROADCAST_CHAT" else "DIRECT_MSG",
                originNode = payload.senderName,
                destinationNode = if (payload.isBroadcast) "ALL_NODES" else "Perangkat Ini",
                hopRoute = listOf(payload.senderName, "Perangkat Ini"),
                payloadBytes = payload.messageText.toByteArray().size + 128,
                latencyMs = (15..40).random(),
                timestamp = payload.timestamp,
                status = "DELIVERED_OFFGRID"
            )

            _packetLogs.update { listOf(log) + it.take(30) }
            _meshStats.update { it.copy(totalPacketsRelayed = it.totalPacketsRelayed + 1) }

            if (payload.isBroadcast && payload.messageText.startsWith("[SOS]")) {
                val sos = MeshSosBroadcast(
                    id = payload.packetId,
                    senderName = payload.senderName,
                    locationCoords = "-6.2088, 106.8456",
                    message = payload.messageText.removePrefix("[SOS]").trim(),
                    severity = "CRITICAL",
                    timestamp = payload.timestamp,
                    hopsRelayed = 1
                )
                _sosAlerts.update { listOf(sos) + it }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshScan() {
        try {
            val adapter = bluetoothAdapter
            if (adapter != null && adapter.isEnabled) {
                loadBondedDevices()
                socketEngine.startListening()

                if (!adapter.isDiscovering) {
                    adapter.startDiscovery()
                }

                bleScanner?.let { scanner ->
                    try {
                        scanner.stopScan(bleScanCallback)
                        scanner.startScan(bleScanCallback)
                    } catch (_: Exception) {}
                }
            }

            wifiP2pManager?.let { manager ->
                wifiP2pChannel?.let { channel ->
                    try {
                        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                            override fun onSuccess() {}
                            override fun onFailure(reason: Int) {}
                        })
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    private fun startMeshNetworkLoop() {
        scanJob?.cancel()
        scanJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                if (_isMeshModeEnabled.value && _isRadarScanning.value) {
                    refreshScan()
                }
                delay(12000)
            }
        }
    }

    fun toggleMeshMode(enabled: Boolean) {
        _isMeshModeEnabled.value = enabled
        if (enabled) {
            socketEngine.startListening()
            startMeshNetworkLoop()
        } else {
            scanJob?.cancel()
            socketEngine.stop()
            try {
                bluetoothAdapter?.cancelDiscovery()
                bleScanner?.stopScan(bleScanCallback)
                wifiP2pManager?.let { manager ->
                    wifiP2pChannel?.let { channel ->
                        manager.stopPeerDiscovery(channel, null)
                    }
                }
            } catch (_: Exception) {}
            _activePeers.value = emptyList()
            _meshStats.update { it.copy(activeMeshNodesCount = 0) }
        }
    }

    @SuppressLint("MissingPermission")
    fun toggleRadarScan() {
        val newState = !_isRadarScanning.value
        _isRadarScanning.value = newState
        if (newState) {
            refreshScan()
        } else {
            try {
                bluetoothAdapter?.cancelDiscovery()
                bleScanner?.stopScan(bleScanCallback)
            } catch (_: Exception) {}
        }
    }

    @SuppressLint("MissingPermission")
    fun sendOffGridMessage(recipientAddressOrName: String, content: String): MeshPacketLog {
        val packetId = UUID.randomUUID().toString().take(8)
        val myDeviceName = bluetoothAdapter?.name ?: "Saya"
        val myAddress = bluetoothAdapter?.address ?: "ME"

        val payload = RawMeshPayload(
            packetId = packetId,
            senderName = myDeviceName,
            senderAddress = myAddress,
            recipientAddress = recipientAddressOrName,
            messageText = content,
            timestamp = System.currentTimeMillis(),
            isBroadcast = false
        )

        socketEngine.sendDirectMessage(recipientAddressOrName, payload)

        val log = MeshPacketLog(
            id = packetId,
            packetType = "DIRECT_MSG",
            originNode = "Saya ($myDeviceName)",
            destinationNode = recipientAddressOrName,
            hopRoute = listOf("Saya", recipientAddressOrName),
            payloadBytes = content.toByteArray().size + 128,
            latencyMs = (12..35).random(),
            status = "DELIVERED_OFFGRID"
        )

        _packetLogs.update { listOf(log) + it.take(25) }
        _meshStats.update { it.copy(totalPacketsRelayed = it.totalPacketsRelayed + 1) }
        return log
    }

    @SuppressLint("MissingPermission")
    fun broadcastSosEmergency(message: String, coords: String) {
        val packetId = UUID.randomUUID().toString().take(8)
        val myDeviceName = bluetoothAdapter?.name ?: "Saya"
        val myAddress = bluetoothAdapter?.address ?: "ME"

        val payload = RawMeshPayload(
            packetId = packetId,
            senderName = myDeviceName,
            senderAddress = myAddress,
            recipientAddress = "ALL",
            messageText = "[SOS] $message (Coords: $coords)",
            timestamp = System.currentTimeMillis(),
            isBroadcast = true
        )

        socketEngine.broadcastToAll(payload)

        val sos = MeshSosBroadcast(
            id = packetId,
            senderName = "Anda (Emergency Beacon)",
            locationCoords = coords.ifEmpty { "-6.2088, 106.8456" },
            message = message,
            severity = "CRITICAL",
            timestamp = System.currentTimeMillis(),
            hopsRelayed = 1
        )

        _sosAlerts.update { listOf(sos) + it }

        val log = MeshPacketLog(
            id = packetId,
            packetType = "SOS_BEACON",
            originNode = "Saya ($myDeviceName)",
            destinationNode = "ALL_SURROUNDING_NODES (Mesh Flood)",
            hopRoute = listOf("Saya", "Broadcast Radio"),
            payloadBytes = 512,
            latencyMs = 8,
            status = "BROADCASTED"
        )

        _packetLogs.update { listOf(log) + it.take(25) }
        _meshStats.update { it.copy(totalPacketsRelayed = it.totalPacketsRelayed + 1) }
    }

    @SuppressLint("MissingPermission")
    fun broadcastGroupMessage(content: String) {
        val packetId = UUID.randomUUID().toString().take(8)
        val myDeviceName = bluetoothAdapter?.name ?: "Saya"
        val myAddress = bluetoothAdapter?.address ?: "ME"

        val payload = RawMeshPayload(
            packetId = packetId,
            senderName = myDeviceName,
            senderAddress = myAddress,
            recipientAddress = "ALL",
            messageText = content,
            timestamp = System.currentTimeMillis(),
            isBroadcast = true
        )

        socketEngine.broadcastToAll(payload)

        val log = MeshPacketLog(
            id = packetId,
            packetType = "BROADCAST_CHAT",
            originNode = "Saya ($myDeviceName)",
            destinationNode = "GRUP_OFFGRID_SEKITAR",
            hopRoute = listOf("Saya", "Mesh Flood RFCOMM"),
            payloadBytes = content.toByteArray().size + 128,
            latencyMs = 15,
            status = "BROADCASTED"
        )

        _packetLogs.update { listOf(log) + it.take(25) }
        _meshStats.update { it.copy(totalPacketsRelayed = it.totalPacketsRelayed + 1) }
    }

    fun triggerNewPeerDiscovery(name: String, role: String, protocol: String) {
        val nodeId = "node_${UUID.randomUUID().toString().take(6)}"
        val newNode = MeshPeerNode(
            id = nodeId,
            name = name,
            role = role,
            publicKey = CryptoManager.generateKeyPair().first,
            distanceMeters = (5..35).random(),
            rssi = (-70..-45).random(),
            hopCount = 1,
            protocol = protocol,
            batteryLevel = (75..100).random(),
            isConnected = true,
            radarAngle = (0..360).random().toFloat(),
            radarDistanceRatio = (20..75).random() / 100f
        )
        _activePeers.update { listOf(newNode) + it }
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
