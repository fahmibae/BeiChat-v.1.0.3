package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.CryptoManager
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SecurityShieldGold
import com.example.ui.theme.SecurityShieldRed
import com.example.ui.theme.SleekBlueBorderLight
import com.example.ui.theme.SleekBlueDark
import com.example.ui.theme.SleekBlueLight
import com.example.ui.theme.SleekBluePrimary
import com.example.ui.theme.SleekBlueSoft
import com.example.ui.theme.SleekBlueTintLight

import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import com.example.ui.viewmodel.AppThemeMode

import androidx.compose.ui.platform.LocalContext
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.provider.Settings

private fun getDeviceInitialName(context: Context): String {
    val prefs = context.getSharedPreferences("bitchat_profile", Context.MODE_PRIVATE)
    val savedName = prefs.getString("user_display_name", null)
    if (!savedName.isNullOrBlank()) return savedName

    // 1. Try Bluetooth Adapter name
    try {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val btAdapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
        val btName = btAdapter?.name
        if (!btName.isNullOrBlank()) {
            return btName
        }
    } catch (_: Exception) {}

    // 2. Try Settings device name
    try {
        val devName = Settings.Global.getString(context.contentResolver, "device_name")
        if (!devName.isNullOrBlank()) return devName
    } catch (_: Exception) {}

    try {
        val btName = Settings.Secure.getString(context.contentResolver, "bluetooth_name")
        if (!btName.isNullOrBlank()) return btName
    } catch (_: Exception) {}

    // 3. Fallback to Android Device Model
    val model = Build.MODEL ?: "Android Device"
    val manufacturer = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: ""
    return if (manufacturer.isNotEmpty() && !model.startsWith(manufacturer, ignoreCase = true)) "$manufacturer $model" else model
}

@Composable
fun AccountScreen(
    isBiometricLockEnabled: Boolean,
    isMeshModeActive: Boolean,
    onToggleBiometricLock: (Boolean) -> Unit,
    onToggleMeshMode: (Boolean) -> Unit,
    onOpenLinkedDevices: () -> Unit,
    onOpenSecurityLogs: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bitchat_profile", Context.MODE_PRIVATE) }

    var userName by remember {
        mutableStateOf(prefs.getString("user_display_name", null) ?: getDeviceInitialName(context))
    }
    var userStatus by remember {
        mutableStateOf(prefs.getString("user_status", null) ?: "Tersedia • Enkripsi E2EE & Mesh P2P Aktif 🔒")
    }
    var userPhone by remember {
        mutableStateOf(prefs.getString("user_phone", null) ?: "P2P Mesh Node (Bebas Server & Pulsa)")
    }
    var userHandle by remember(userName) {
        val cleanName = userName.filter { it.isLetterOrDigit() }.lowercase().ifEmpty { "node" }
        mutableStateOf("@${cleanName}_p2p")
    }

    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    var isQrDialogOpen by remember { mutableStateOf(false) }
    var isKeyBackupDialogOpen by remember { mutableStateOf(false) }

    var autoDownloadMediaLossless by remember { mutableStateOf(true) }
    var securityNotificationsEnabled by remember { mutableStateOf(true) }

    val clipboardManager = LocalClipboardManager.current
    val identityKeyFingerprint = remember {
        CryptoManager.localUserPublicKey.chunked(4).take(8).joinToString(" ")
    }

    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("account_screen"),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131D31) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isDark) Color(0xFF1E293B) else SleekBlueBorderLight
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar with edit badge
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(SleekBluePrimary, Color(0xFF1D4ED8))
                                    )
                                )
                                .clickable { isEditProfileDialogOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Edit mini icon
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF131D31) else MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.5.dp,
                                        if (isDark) SleekBlueSoft else SleekBluePrimary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = if (isDark) SleekBlueSoft else SleekBluePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = userName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Verified",
                                    tint = if (isDark) SleekBlueSoft else SleekBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = userHandle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) SleekBlueSoft else SleekBluePrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = userPhone,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // QR Code Button
                        IconButton(
                            onClick = { isQrDialogOpen = true },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E293B) else SleekBlueTintLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Code Profil",
                                tint = if (isDark) SleekBlueSoft else SleekBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(
                        color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bio/Status Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEditProfileDialogOpen = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Info & Status",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = userStatus,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Status",
                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Section: Identitas Kriptografi & Kunci E2EE
        item {
            Text(
                text = "IDENTITAS KRIPTOGRAFI (E2EE)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131D31) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(OnlineGreen.copy(alpha = if (isDark) 0.25f else 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = OnlineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Fingerprint Kunci Publik X25519",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Sidik jari kriptografi terotentikasi",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(identityKeyFingerprint))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Kunci",
                                tint = if (isDark) SleekBlueSoft else SleekBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = identityKeyFingerprint,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) SleekBlueSoft else SleekBlueDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isKeyBackupDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else SleekBlueTintLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = if (isDark) SleekBlueSoft else SleekBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cadangkan Kunci",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) SleekBlueSoft else SleekBluePrimary
                            )
                        }

                        Button(
                            onClick = { isQrDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekBluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kode QR Verifikasi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Section: Tema & Tampilan (Sistem Otomatis)
        item {
            Text(
                text = "TEMA & TAMPILAN SISTEM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131D31) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else SleekBlueTintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = if (isDark) SleekBlueSoft else SleekBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sinkronisasi Tema Ponsel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isDark) "Mode Gelap aktif otomatis mengikuti setelan perangkat" else "Mode Terang aktif otomatis mengikuti setelan perangkat",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0xFF0F172A) else SleekBlueTintLight
                    ) {
                        Text(
                            text = if (isDark) "Gelap" else "Terang",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) SleekBlueSoft else SleekBluePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Section: Pengaturan Akun & Privasi
        item {
            Text(
                text = "PENGATURAN & KEAMANAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131D31) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    AccountSettingToggleItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Kunci Aplikasi Biometrik",
                        subtitle = "Wajibkan sidik jari/PIN saat membuka BeiChat",
                        checked = isBiometricLockEnabled,
                        isDarkTheme = isDark,
                        onCheckedChange = onToggleBiometricLock
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    AccountSettingToggleItem(
                        icon = Icons.Default.WifiTethering,
                        title = "P2P Mesh Off-Grid Default",
                        subtitle = "Otomatis kirim via gelombang radio saat offline",
                        checked = isMeshModeActive,
                        isDarkTheme = isDark,
                        onCheckedChange = onToggleMeshMode
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    AccountSettingToggleItem(
                        icon = Icons.Default.Storage,
                        title = "Unduh Otomatis Media HD (Lossless)",
                        subtitle = "Simpan foto/video 4K tanpa kompresi secara instan",
                        checked = autoDownloadMediaLossless,
                        isDarkTheme = isDark,
                        onCheckedChange = { autoDownloadMediaLossless = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    AccountSettingToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifikasi Kunci Keamanan",
                        subtitle = "Beri peringatan jika nomor keamanan kontak berubah",
                        checked = securityNotificationsEnabled,
                        isDarkTheme = isDark,
                        onCheckedChange = { securityNotificationsEnabled = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    AccountSettingNavigationItem(
                        icon = Icons.Default.Devices,
                        title = "Perangkat Tertaut",
                        subtitle = "Sinkronisasi Web, Desktop, & Tablet terenkripsi",
                        isDarkTheme = isDark,
                        onClick = onOpenLinkedDevices
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    AccountSettingNavigationItem(
                        icon = Icons.Default.Security,
                        title = "Log Audit Keamanan",
                        subtitle = "Pantau verifikasi kunci dan deteksi tamper",
                        isDarkTheme = isDark,
                        onClick = onOpenSecurityLogs
                    )
                }
            }
        }

        // Section: Info Aplikasi & Keamanan
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131D31) else SleekBlueTintLight
                ),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (isDark) Color(0xFF1E293B) else SleekBlueBorderLight
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isDark) SleekBlueSoft else SleekBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BeiChat Off-Grid Edition v2.5.0",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) SleekBlueSoft else SleekBlueDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Arsitektur Zero-Knowledge dengan Enkripsi AES-256-GCM + X25519 & Protokol Komunikasi Radio Mesh P2P Bebas Pulsa/Internet.",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    // Dialog Edit Profile
    if (isEditProfileDialogOpen) {
        var editName by remember { mutableStateOf(userName) }
        var editStatus by remember { mutableStateOf(userStatus) }
        var editPhone by remember { mutableStateOf(userPhone) }

        AlertDialog(
            onDismissRequest = { isEditProfileDialogOpen = false },
            title = { Text("Edit Profil Akun") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editStatus,
                        onValueChange = { editStatus = it },
                        label = { Text("Info / Status") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Nomor Telepon") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userName = editName
                        userStatus = editStatus
                        userPhone = editPhone
                        prefs.edit()
                            .putString("user_display_name", editName)
                            .putString("user_status", editStatus)
                            .putString("user_phone", editPhone)
                            .apply()
                        isEditProfileDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBluePrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditProfileDialogOpen = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog QR Code Identity
    if (isQrDialogOpen) {
        AlertDialog(
            onDismissRequest = { isQrDialogOpen = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = SleekBluePrimary)
                    Text("Kode QR Kunci Akun")
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pindai kode ini dari perangkat lain untuk memverifikasi sidik jari kriptografi E2EE tanpa pihak ketiga.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Production Identity QR Box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.size(180.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Code",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(150.dp)
                            )
                        }
                    }

                    Text(
                        text = "$userName ($userHandle)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(
                        text = identityKeyFingerprint,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (isDark) SleekBlueSoft else SleekBlueDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { isQrDialogOpen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBluePrimary)
                ) {
                    Text("Tutup")
                }
            }
        )
    }

    // Dialog Backup Keys
    if (isKeyBackupDialogOpen) {
        AlertDialog(
            onDismissRequest = { isKeyBackupDialogOpen = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = if (isDark) SleekBlueSoft else SleekBluePrimary
                    )
                    Text("Cadangan Kunci Privat")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kunci privat Anda dienkripsi dengan password master perangkat. Simpan passphrase cadangan ini di tempat yang aman.",
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "bitchat-mesh-passphrase-${identityKeyFingerprint.take(16).replace(" ", "-").lowercase()}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) SleekBlueSoft else SleekBlueDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { isKeyBackupDialogOpen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekBluePrimary)
                ) {
                    Text("Selesai")
                }
            }
        )
    }
}

@Composable
fun AccountSettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    isDarkTheme: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xFF1E293B) else SleekBlueTintLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDarkTheme) SleekBlueSoft else SleekBluePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SleekBluePrimary,
                uncheckedTrackColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
fun AccountSettingNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDarkTheme: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xFF1E293B) else SleekBlueTintLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDarkTheme) SleekBlueSoft else SleekBluePrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = if (isDarkTheme) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}
