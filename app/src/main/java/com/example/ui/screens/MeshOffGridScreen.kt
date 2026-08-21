package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesh.MeshPacketLog
import com.example.mesh.MeshPeerNode
import com.example.mesh.MeshSosBroadcast
import com.example.mesh.MeshStats
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SecurityShieldGold
import com.example.ui.theme.SecurityShieldRed
import com.example.ui.theme.SleekBlueBorderLight
import com.example.ui.theme.SleekBlueDark
import com.example.ui.theme.SleekBlueLight
import com.example.ui.theme.SleekBluePrimary
import com.example.ui.theme.SleekBlueSoft
import com.example.ui.theme.SleekBlueTintLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeshOffGridScreen(
    isMeshActive: Boolean,
    isRadarScanning: Boolean,
    activePeers: List<MeshPeerNode>,
    packetLogs: List<MeshPacketLog>,
    sosAlerts: List<MeshSosBroadcast>,
    meshStats: MeshStats,
    onToggleMeshActive: (Boolean) -> Unit,
    onToggleRadarScan: () -> Unit,
    onDirectChatWithPeer: (MeshPeerNode) -> Unit,
    onSendSosEmergency: (String, String) -> Unit,
    onAddCustomNode: (String, String, String) -> Unit
) {
    var showSosDialog by remember { mutableStateOf(false) }
    var showAddNodeDialog by remember { mutableStateOf(false) }
    var selectedTabSection by remember { mutableStateOf(0) } // 0 = Radar & Node, 1 = SOS Alert, 2 = Paket Radio

    val isDarkTheme = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("mesh_off_grid_screen"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Off-Grid Status Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMeshActive) {
                        if (isDarkTheme) Color(0xFF131D31) else SleekBlueTintLight
                    } else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMeshActive) {
                        if (isDarkTheme) SleekBluePrimary.copy(alpha = 0.4f) else SleekBlueBorderLight
                    } else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isMeshActive) SleekBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiTethering,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Chat Tanpa Internet (Off-Grid)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isMeshActive) "📡 P2P BLE Mesh & Wi-Fi Direct Aktif" else "Mode Offline Nonaktif",
                                    fontSize = 12.sp,
                                    color = if (isMeshActive) {
                                        if (isDarkTheme) SleekBlueSoft else SleekBlueDark
                                    } else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isMeshActive,
                            onCheckedChange = onToggleMeshActive,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SleekBluePrimary
                            ),
                            modifier = Modifier.testTag("mesh_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Stat Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MeshStatMiniCard(
                            icon = Icons.Default.Hub,
                            title = "Node Aktif",
                            value = "${activePeers.size} Terhubung",
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                        MeshStatMiniCard(
                            icon = Icons.Default.Sensors,
                            title = "Latensi Radio",
                            value = "${meshStats.averageHopLatencyMs} ms",
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                        MeshStatMiniCard(
                            icon = Icons.Default.CellTower,
                            title = "Penggunaan Kuota",
                            value = "0 KB (Gratis)",
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Sub-tabs: Radar & Nodes | SOS Emergency | Paket Radio
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        0.5.dp,
                        if (isDarkTheme) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabPillButton(
                    title = "Radar P2P (${activePeers.size})",
                    isSelected = selectedTabSection == 0,
                    isDarkTheme = isDarkTheme,
                    onClick = { selectedTabSection = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabPillButton(
                    title = "Beacon SOS (${sosAlerts.size})",
                    isSelected = selectedTabSection == 1,
                    isDarkTheme = isDarkTheme,
                    onClick = { selectedTabSection = 1 },
                    modifier = Modifier.weight(1f)
                )
                TabPillButton(
                    title = "Log Paket Radio",
                    isSelected = selectedTabSection == 2,
                    isDarkTheme = isDarkTheme,
                    onClick = { selectedTabSection = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (selectedTabSection) {
            0 -> {
                // Radar View Section
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Radar Pemindaian Node Lokal",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Radius pancaran gelombang ~450m",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = onToggleRadarScan,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Scan",
                                            tint = SleekBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { showAddNodeDialog = true },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Tambah Node",
                                            tint = SleekBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Canvas Radar
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                MeshRadarVisualizer(
                                    isScanning = isRadarScanning && isMeshActive,
                                    nodes = activePeers,
                                    onNodeClick = onDirectChatWithPeer
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Node Hijau = Direct P2P • Biru = Relay Mesh (Multi-hop)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Section Header: Daftar Node Terhubung
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NODE DI SEKITAR ANDA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${activePeers.size} Perangkat Ditemukan",
                            fontSize = 11.sp,
                            color = SleekBluePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                items(activePeers, key = { it.id }) { peer ->
                    MeshPeerCardItem(
                        peer = peer,
                        onChatClick = { onDirectChatWithPeer(peer) }
                    )
                }
            }

            1 -> {
                // SOS Emergency Beacon Center
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SecurityShieldRed.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecurityShieldRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(SecurityShieldRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Beacon Darurat SOS (Banjir Mesh)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SecurityShieldRed
                                    )
                                    Text(
                                        text = "Memancarkan pesan darurat ke semua perangkat dalam radius mesh tanpa internet.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showSosDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityShieldRed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("send_sos_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pancarkan Sinyal SOS Darurat", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "RIWAYAT BEACON DARURAT MESH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(sosAlerts, key = { it.id }) { alert ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecurityShieldRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = SecurityShieldRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = alert.senderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SecurityShieldRed.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${alert.hopsRelayed} Hop Relay",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SecurityShieldRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = alert.message,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SleekBluePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Koordinat: ${alert.locationCoords} (GPS Offline)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Radio Packet Log Stream
                item {
                    Text(
                        text = "MONITOR PAKET RADIO MESH (REAL-TIME)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(packetLogs, key = { it.id }) { log ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.4f else 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkTheme) Color(0xFF1E293B) else SleekBlueTintLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = if (isDarkTheme) SleekBlueSoft else SleekBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${log.packetType} [${log.payloadBytes} B]",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${log.latencyMs}ms",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnlineGreen
                                    )
                                }

                                Text(
                                    text = "Jalur: ${log.hopRoute.joinToString(" ➔ ")}",
                                    fontSize = 11.sp,
                                    color = if (isDarkTheme) SleekBlueSoft else SleekBlueDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "Status: ${log.status} • AES-256 Otentikasi",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // SOS Emergency Dialog
    if (showSosDialog) {
        var sosMsg by remember { mutableStateOf("⚠️ BANTUAN MEDIS: Membutuhkan pertolongan pertama di sektor koordinat lokal.") }
        var coords by remember { mutableStateOf("-6.2088, 106.8456") }

        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SecurityShieldRed)
                    Text("Kirim Beacon Darurat SOS")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pesan ini akan dikirimkan ke SELURUH perangkat di sekitar melalui gelombang radio Bluetooth & Wi-Fi Direct Mesh tanpa memerlukan pulsa, internet, atau sinyal seluler.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = sosMsg,
                        onValueChange = { sosMsg = it },
                        label = { Text("Pesan Darurat") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = coords,
                        onValueChange = { coords = it },
                        label = { Text("Koordinat GPS Offline") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSendSosEmergency(sosMsg, coords)
                        showSosDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecurityShieldRed)
                ) {
                    Text("Pancarkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Add Custom Node Dialog
    if (showAddNodeDialog) {
        var nodeName by remember { mutableStateOf("") }
        var nodeRole by remember { mutableStateOf("Direct Peer (BLE 5.3)") }
        var nodeProtocol by remember { mutableStateOf("BLE Mesh 5.3") }

        AlertDialog(
            onDismissRequest = { showAddNodeDialog = false },
            title = { Text("Tambahkan Node Mesh Lokal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Hubungkan perangkat baru ke topologi mesh off-grid untuk memperluas jangkauan komunikasi tanpa internet.",
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = nodeName,
                        onValueChange = { nodeName = it },
                        label = { Text("Nama Node / Pengguna") },
                        placeholder = { Text("mis. Tim Penyelamat Beta") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nodeRole,
                        onValueChange = { nodeRole = it },
                        label = { Text("Peran / Deskripsi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nodeName.isNotBlank()) {
                            onAddCustomNode(nodeName, nodeRole, nodeProtocol)
                            showAddNodeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBluePrimary)
                ) {
                    Text("Hubungkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNodeDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun MeshStatMiniCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDarkTheme) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isDarkTheme) Color(0xFF334155) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDarkTheme) SleekBlueSoft else SleekBluePrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TabPillButton(
    title: String,
    isSelected: Boolean,
    isDarkTheme: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) {
            if (isDarkTheme) Color(0xFF132B45) else SleekBlueTintLight
        } else Color.Transparent,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDarkTheme) SleekBluePrimary.copy(alpha = 0.5f) else SleekBlueBorderLight
            )
        } else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    if (isDarkTheme) SleekBlueSoft else SleekBlueDark
                } else {
                    if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                },
                maxLines = 1
            )
        }
    }
}

@Composable
fun MeshPeerCardItem(
    peer: MeshPeerNode,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDarkTheme) 0.4f else 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Node avatar / Signal badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (peer.hopCount == 1) {
                            if (isDarkTheme) Color(0xFF1E293B) else SleekBlueTintLight
                        } else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (peer.hopCount == 1) Icons.Default.Sensors else Icons.Default.Hub,
                    contentDescription = null,
                    tint = if (peer.hopCount == 1) {
                        if (isDarkTheme) SleekBlueSoft else SleekBluePrimary
                    } else {
                        if (isDarkTheme) Color(0xFF94A3B8) else SleekBlueDark
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = peer.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (peer.rssi > -60) OnlineGreen.copy(alpha = 0.15f) else SecurityShieldGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${peer.rssi} dBm",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (peer.rssi > -60) OnlineGreen else SecurityShieldGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${peer.protocol} • Jarak ~${peer.distanceMeters}m (${peer.hopCount} Hop)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = OnlineGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "P2P Ephemeral X25519",
                        fontSize = 10.sp,
                        color = OnlineGreen
                    )
                    Text(
                        text = "• Baterai: ${peer.batteryLevel}%",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onChatClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SleekBluePrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MeshRadarVisualizer(
    isScanning: Boolean,
    nodes: List<MeshPeerNode>,
    onNodeClick: (MeshPeerNode) -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == Color(0xFF0B0F19)

    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val radarBgColor = if (isDarkTheme) Color(0xFF0B101D) else Color(0xFFF1F6FF)
    val gridColor = if (isDarkTheme) SleekBlueLight.copy(alpha = 0.35f) else SleekBluePrimary.copy(alpha = 0.22f)
    val pulseColor = if (isDarkTheme) SleekBlueSoft else SleekBluePrimary
    val selfColor = if (isDarkTheme) SleekBlueSoft else SleekBluePrimary

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable { }
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2 - 12.dp.toPx()

        // Radar circular backing screen
        drawCircle(
            color = radarBgColor,
            radius = maxRadius,
            center = center
        )

        // 3 Concentric Range Rings (10m, 50m, 100m)
        drawCircle(
            color = gridColor,
            radius = maxRadius * 0.33f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = gridColor,
            radius = maxRadius * 0.66f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = gridColor,
            radius = maxRadius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Crosshairs
        drawLine(
            color = gridColor,
            start = Offset(center.x, center.y - maxRadius),
            end = Offset(center.x, center.y + maxRadius),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = gridColor,
            start = Offset(center.x - maxRadius, center.y),
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 1.dp.toPx()
        )

        if (isScanning) {
            // Pulse wave
            drawCircle(
                color = pulseColor.copy(alpha = 0.2f * (1f - pulseScale)),
                radius = maxRadius * pulseScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Radar Sweep line & cone
            val rad = Math.toRadians(sweepAngle.toDouble())
            val lineEnd = Offset(
                x = center.x + (maxRadius * cos(rad)).toFloat(),
                y = center.y + (maxRadius * sin(rad)).toFloat()
            )

            drawLine(
                color = pulseColor,
                start = center,
                end = lineEnd,
                strokeWidth = 2.dp.toPx()
            )
        }

        // Center Device (Self)
        drawCircle(
            color = selfColor,
            radius = 6.dp.toPx(),
            center = center
        )

        // Draw Peer Nodes
        nodes.forEach { node ->
            val rad = Math.toRadians(node.radarAngle.toDouble())
            val dist = maxRadius * node.radarDistanceRatio
            val nodePos = Offset(
                x = center.x + (dist * cos(rad)).toFloat(),
                y = center.y + (dist * sin(rad)).toFloat()
            )

            val nodeColor = if (node.hopCount == 1) OnlineGreen else (if (isDarkTheme) SleekBlueSoft else SleekBlueDark)

            // Outer glow ring
            drawCircle(
                color = nodeColor.copy(alpha = 0.35f),
                radius = 9.dp.toPx(),
                center = nodePos
            )
            // Node point
            drawCircle(
                color = nodeColor,
                radius = 5.dp.toPx(),
                center = nodePos
            )
        }
    }
}
