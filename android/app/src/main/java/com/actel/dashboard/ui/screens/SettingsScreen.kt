package com.actel.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actel.dashboard.data.model.ConnectionState
import com.actel.dashboard.ui.theme.BgCard
import com.actel.dashboard.ui.theme.BorderDark
import com.actel.dashboard.ui.theme.NeonCyan
import com.actel.dashboard.ui.theme.NeonGreen
import com.actel.dashboard.ui.theme.NeonRed
import com.actel.dashboard.ui.theme.NeonYellow
import com.actel.dashboard.ui.theme.TextDim
import com.actel.dashboard.ui.theme.TextPrimary
import com.actel.dashboard.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    connectionState: ConnectionState,
    currentHost: String,
    discoveredHost: String?,
    discoveredHostname: String?,
    hapticEnabled: Boolean,
    hapticIntensity: Float,
    surfaceHapticEnabled: Boolean,
    currentMode: String,
    onConnect: (String, Int) -> Unit,
    onDisconnect: () -> Unit,
    onModeChange: (String) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onHapticIntensityChange: (Float) -> Unit,
    onToggleSurfaceHaptic: (Boolean) -> Unit,
    onPreviewHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    var ipInput by remember(currentHost) { mutableStateOf(currentHost.ifEmpty { "192.168.1." }) }
    var portInput by remember { mutableStateOf("8080") }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sol Kolon: Sunucu Bağlantısı
        Column(
            modifier = Modifier
                .weight(1.1f)
                .clip(RoundedCornerShape(12.dp))
                .background(BgCard)
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SUNUCU BAĞLANTISI",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Otomatik Keşif Kartı
            if (discoveredHost != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1A1F))
                        .border(1.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable {
                            ipInput = discoveredHost
                            onConnect(discoveredHost, 8080)
                        }
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📡 ", fontSize = 13.sp)
                            Text(
                                text = "AĞDA PC BULUNDU (OTOMATİK)",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${discoveredHostname ?: "Assetto Corsa PC"} ($discoveredHost:8080)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Bağlanmak için dokunun",
                            color = TextDim,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // IP & Port Girişi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("PC IP Adresi", color = TextSecondary, fontSize = 11.sp) },
                    modifier = Modifier.weight(2f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = portInput,
                    onValueChange = { portInput = it },
                    label = { Text("Port", color = TextSecondary, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderDark
                    ),
                    singleLine = true
                )
            }

            // Bağlan / Kes Butonu
            if (connectionState == ConnectionState.CONNECTED) {
                Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "BAĞLANTIYI KES", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val port = portInput.toIntOrNull() ?: 8080
                        onConnect(ipInput.trim(), port)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTING) "BAĞLANILIYOR..." else "BAĞLAN",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Sağ Kolon: Sürüş & Haptik Tercihleri
        Column(
            modifier = Modifier
                .weight(1.1f)
                .clip(RoundedCornerShape(12.dp))
                .background(BgCard)
                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "UYGULAMA VE SÜRÜŞ AYARLARI",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Ana Haptik Titreşim Anahtarı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Haptik Titreşim", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Tuşlar, devir kesici ve yol hissi", color = TextDim, fontSize = 10.sp)
                }
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onToggleHaptic,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextDim,
                        uncheckedTrackColor = Color(0xFF1E2633)
                    )
                )
            }

            // Titreşim Şiddeti Slider'ı
            if (hapticEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F131A))
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TİTREŞİM ŞİDDETİ",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "%${(hapticIntensity * 100).toInt()}",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = hapticIntensity,
                        onValueChange = onHapticIntensityChange,
                        onValueChangeFinished = onPreviewHaptic,
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color(0xFF222B38)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    )
                }

                // Bordür & Kasis Titreşimi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Bordür & Kasis Titreşimi", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Kerb, kasis ve çimen titreşimi", color = TextDim, fontSize = 10.sp)
                    }
                    Switch(
                        checked = surfaceHapticEnabled,
                        onCheckedChange = onToggleSurfaceHaptic,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextDim,
                            uncheckedTrackColor = Color(0xFF1E2633)
                        )
                    )
                }
            }

            // Telemetri Modu
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Çalışma Modu", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ModePill("AUTO", currentMode == "AUTO") { onModeChange("AUTO") }
                    ModePill("LIVE", currentMode == "LIVE_ONLY") { onModeChange("LIVE_ONLY") }
                    ModePill("DEMO", currentMode == "DEMO_ONLY") { onModeChange("DEMO_ONLY") }
                }
            }
        }
    }
}

@Composable
private fun ModePill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) NeonCyan else BorderDark
    val bgColor = if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color(0xFF0F131A)
    val textColor = if (isSelected) NeonCyan else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
