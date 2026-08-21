package com.example.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class RawMeshPayload(
    val packetId: String,
    val senderName: String,
    val senderAddress: String,
    val recipientAddress: String,
    val messageText: String,
    val timestamp: Long,
    val isBroadcast: Boolean
)

class BluetoothMeshSocketEngine(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onMessageReceived: (RawMeshPayload) -> Unit,
    private val onNodeConnected: (BluetoothDevice) -> Unit
) {
    companion object {
        private const val TAG = "BtMeshSocketEngine"
        private const val SERVICE_NAME = "BitChatMeshNetwork"
        val APP_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        try {
            BluetoothAdapter.getDefaultAdapter()
        } catch (_: Exception) {
            null
        }
    }

    private var serverJob: Job? = null
    private var serverSocket: BluetoothServerSocket? = null
    private val activeConnections = ConcurrentHashMap<String, BluetoothSocket>()

    fun startListening() {
        if (serverJob?.isActive == true) return
        serverJob = scope.launch(Dispatchers.IO) {
            val adapter = bluetoothAdapter ?: return@launch
            if (!adapter.isEnabled) return@launch

            while (isActive) {
                try {
                    @SuppressLint("MissingPermission")
                    val sSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
                    serverSocket = sSocket
                    Log.d(TAG, "Bluetooth RFCOMM Server listening for peer connections...")

                    while (isActive) {
                        val socket = try {
                            sSocket.accept()
                        } catch (e: Exception) {
                            null
                        }

                        if (socket != null) {
                            val device = socket.remoteDevice
                            val address = device.address ?: UUID.randomUUID().toString()
                            activeConnections[address] = socket
                            onNodeConnected(device)
                            listenToSocketStream(socket, address)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Server socket error, restarting in 3s", e)
                    try { serverSocket?.close() } catch (_: Exception) {}
                    kotlinx.coroutines.delay(3000)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice, onConnected: ((Boolean) -> Unit)? = null) {
        val address = device.address ?: return
        if (activeConnections.containsKey(address)) {
            onConnected?.invoke(true)
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                bluetoothAdapter?.cancelDiscovery()
                val socket = device.createInsecureRfcommSocketToServiceRecord(APP_UUID)
                socket.connect()
                activeConnections[address] = socket
                onNodeConnected(device)
                listenToSocketStream(socket, address)
                onConnected?.invoke(true)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to connect to ${device.name} ($address): ${e.message}")
                onConnected?.invoke(false)
            }
        }
    }

    private fun listenToSocketStream(socket: BluetoothSocket, address: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                while (isActive && socket.isConnected) {
                    val line = reader.readLine() ?: break
                    if (line.isNotBlank()) {
                        parseIncomingMessage(line)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Socket read stream closed: ${e.message}")
            } finally {
                activeConnections.remove(address)
                try { socket.close() } catch (_: Exception) {}
            }
        }
    }

    private fun parseIncomingMessage(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val payload = RawMeshPayload(
                packetId = json.optString("id", UUID.randomUUID().toString().take(8)),
                senderName = json.optString("senderName", "Node Radio"),
                senderAddress = json.optString("senderAddress", ""),
                recipientAddress = json.optString("recipientAddress", "ALL"),
                messageText = json.optString("text", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                isBroadcast = json.optBoolean("isBroadcast", false)
            )
            onMessageReceived(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse mesh message: $jsonString", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendDirectMessage(targetAddress: String, payload: RawMeshPayload, onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch(Dispatchers.IO) {
            var socket = activeConnections[targetAddress]
            if (socket == null || !socket.isConnected) {
                try {
                    val device = bluetoothAdapter?.getRemoteDevice(targetAddress)
                    if (device != null) {
                        bluetoothAdapter?.cancelDiscovery()
                        val newSocket = device.createInsecureRfcommSocketToServiceRecord(APP_UUID)
                        newSocket.connect()
                        activeConnections[targetAddress] = newSocket
                        listenToSocketStream(newSocket, targetAddress)
                        socket = newSocket
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error connecting to send direct message", e)
                }
            }

            if (socket != null && socket.isConnected) {
                try {
                    val json = JSONObject().apply {
                        put("id", payload.packetId)
                        put("senderName", payload.senderName)
                        put("senderAddress", payload.senderAddress)
                        put("recipientAddress", payload.recipientAddress)
                        put("text", payload.messageText)
                        put("timestamp", payload.timestamp)
                        put("isBroadcast", payload.isBroadcast)
                    }.toString()

                    val writer = PrintWriter(OutputStreamWriter(socket.outputStream), true)
                    writer.println(json)
                    onComplete?.invoke(true)
                    return@launch
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to write to socket", e)
                }
            }
            onComplete?.invoke(false)
        }
    }

    @SuppressLint("MissingPermission")
    fun broadcastToAll(payload: RawMeshPayload) {
        scope.launch(Dispatchers.IO) {
            val json = JSONObject().apply {
                put("id", payload.packetId)
                put("senderName", payload.senderName)
                put("senderAddress", payload.senderAddress)
                put("recipientAddress", "ALL")
                put("text", payload.messageText)
                put("timestamp", payload.timestamp)
                put("isBroadcast", true)
            }.toString()

            activeConnections.values.forEach { socket ->
                try {
                    if (socket.isConnected) {
                        val writer = PrintWriter(OutputStreamWriter(socket.outputStream), true)
                        writer.println(json)
                    }
                } catch (_: Exception) {}
            }

            bluetoothAdapter?.bondedDevices?.forEach { device ->
                val addr = device.address ?: return@forEach
                if (!activeConnections.containsKey(addr)) {
                    try {
                        val newSocket = device.createInsecureRfcommSocketToServiceRecord(APP_UUID)
                        newSocket.connect()
                        activeConnections[addr] = newSocket
                        listenToSocketStream(newSocket, addr)
                        val writer = PrintWriter(OutputStreamWriter(newSocket.outputStream), true)
                        writer.println(json)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun stop() {
        serverJob?.cancel()
        try { serverSocket?.close() } catch (_: Exception) {}
        activeConnections.values.forEach {
            try { it.close() } catch (_: Exception) {}
        }
        activeConnections.clear()
    }
}
