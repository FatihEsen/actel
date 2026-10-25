package com.actel.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actel.dashboard.data.model.TelemetryData
import com.actel.dashboard.ui.theme.BgCard
import com.actel.dashboard.ui.theme.NeonCyan
import com.actel.dashboard.ui.theme.NeonGreen
import com.actel.dashboard.ui.theme.NeonOrange
import com.actel.dashboard.ui.theme.NeonRed
import com.actel.dashboard.ui.theme.NeonYellow

@Composable
fun ButtonBoxScreen(
    telemetry: TelemetryData?,
    onAction: (action: String, state: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPitLimiterOn = telemetry?.physics?.pitLimiterOn == true
    val isDrsOn = telemetry?.physics?.drs == true

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ==================== PANEL 1: SÜRÜŞ & ELEKTRONİK (Sütun 1 & 2) ====================
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Satır 1: TC + / TC -
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("🎛️ TC +", "tc_up", NeonCyan, onAction, Modifier.weight(1f))
                BoxButton("🎛️ TC -", "tc_down", NeonCyan.copy(alpha = 0.85f), onAction, Modifier.weight(1f))
            }
            // Satır 2: ABS + / ABS -
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("🛡️ ABS +", "abs_up", NeonYellow, onAction, Modifier.weight(1f))
                BoxButton("🛡️ ABS -", "abs_down", NeonYellow.copy(alpha = 0.85f), onAction, Modifier.weight(1f))
            }
            // Satır 3: BIAS + / BIAS -
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("⚖️ BIAS +", "bb_up", NeonGreen, onAction, Modifier.weight(1f))
                BoxButton("⚖️ BIAS -", "bb_down", NeonGreen.copy(alpha = 0.85f), onAction, Modifier.weight(1f))
            }
            // Satır 4: TURBO + / TURBO -
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("🚀 TURBO +", "turbo_up", NeonRed, onAction, Modifier.weight(1f))
                BoxButton("🚀 TURBO -", "turbo_down", NeonRed.copy(alpha = 0.85f), onAction, Modifier.weight(1f))
            }
            // Satır 5: ENG BRAKE + / ENG BRAKE -
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("⚙️ EB +", "eb_up", Color(0xFF00E5FF), onAction, Modifier.weight(1f))
                BoxButton("⚙️ EB -", "eb_down", Color(0xFF00B0FF), onAction, Modifier.weight(1f))
            }
        }

        // ==================== PANEL 2: AYDINLATMA, SİNYAL & BAKIŞ (Sütun 3 & 4) ====================
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Satır 1: FARLAR / SELEKTÖR
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("💡 FARLAR", "headlights", Color(0xFF64B5F6), onAction, Modifier.weight(1f))
                BoxButton("🔆 SELEKTÖR", "flasher", Color(0xFFFFF176), onAction, Modifier.weight(1f))
            }
            // Satır 2: İÇ AYDINLATMA / SİS FARI (Alt + Numpad 7 / 8)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("🛋️ İÇ IŞIK", "extra_a", Color(0xFFBA68C8), onAction, Modifier.weight(1f))
                BoxButton("🌫️ SİS FARI", "extra_b", Color(0xFFE040FB), onAction, Modifier.weight(1f))
            }
            // Satır 3: SOL SİNYAL / SAĞ SİNYAL
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("◀️ SOL SİNYAL", "turn_left", Color(0xFFFFB300), onAction, Modifier.weight(1f))
                BoxButton("SAĞ SİNYAL ▶️", "turn_right", Color(0xFFFFB300), onAction, Modifier.weight(1f))
            }
            // Satır 4: DÖRTLÜ / ARKA BAKIŞ
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("⚠️ DÖRTLÜ", "hazard", NeonRed, onAction, Modifier.weight(1f))
                BoxButton("🔼 ARKA", "look_back", Color(0xFF90A4AE), onAction, Modifier.weight(1f))
            }
            // Satır 5: SOLA BAK / SAĞA BAK
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("◀️ SOLA", "look_left", Color(0xFF90A4AE), onAction, Modifier.weight(1f))
                BoxButton("SAĞA ▶️", "look_right", Color(0xFF90A4AE), onAction, Modifier.weight(1f))
            }
        }

        // ==================== PANEL 3: SİLECEK, SEANS, KERS & START (Sütun 5 & 6) ====================
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Satır 1: SİLECEK / PIT LIMITER
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("🌧️ SİLECEK", "wipers", Color(0xFF81C784), onAction, Modifier.weight(1f))
                BoxButton(
                    title = if (isPitLimiterOn) "🛑 PIT (ON)" else "🛑 PIT LIM",
                    action = "pit_limiter",
                    color = if (isPitLimiterOn) NeonGreen else NeonOrange,
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isActive = isPitLimiterOn
                )
            }
            // Satır 2: KORNA / PİTE DÖN
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("📢 KORNA", "horn", Color(0xFFFFB74D), onAction, Modifier.weight(1f))
                BoxButton("🔧 PİTE DÖN", "teleport_pits", NeonRed, onAction, Modifier.weight(1f))
            }
            // Satır 3: KAMERA / YENİDEN BAŞLAT
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("🎥 KAMERA", "camera_change", Color(0xFF42A5F5), onAction, Modifier.weight(1f))
                BoxButton("🔄 YENİDEN", "restart_session", Color(0xFFFF5722), onAction, Modifier.weight(1f))
            }
            // Satır 4: KERS (BOOST SİLİNDİ - HOLD) / DURAKLAT
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton(
                    title = "⚡ KERS",
                    action = "kers",
                    color = Color(0xFF00E676),
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isHold = true
                )
                BoxButton("⏸️ DURAKLAT", "pause", Color(0xFFE57373), onAction, Modifier.weight(1f))
            }
            // Satır 5: DRS (HOLD) / START
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton(
                    title = if (isDrsOn) "💨 DRS (ON)" else "💨 DRS",
                    action = "drs",
                    color = if (isDrsOn) NeonGreen else Color(0xFF00E5FF),
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isHold = true,
                    isActive = isDrsOn
                )
                BoxButton("🏁 START", "starter", NeonGreen, onAction, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BoxButton(
    title: String,
    action: String,
    color: Color,
    onAction: (action: String, state: String?) -> Unit,
    modifier: Modifier = Modifier,
    isHold: Boolean = false,
    isActive: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    val effectiveBg = when {
        isPressed || isActive -> color.copy(alpha = 0.35f)
        else -> BgCard
    }
    val effectiveBorder = when {
        isPressed || isActive -> color
        else -> color.copy(alpha = 0.65f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(effectiveBg)
            .border(1.5.dp, effectiveBorder, RoundedCornerShape(8.dp))
            .then(
                if (isHold) {
                    Modifier.pointerInput(action) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                onAction(action, "down")
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    isPressed = false
                                    onAction(action, "up")
                                }
                            }
                        )
                    }
                } else {
                    Modifier.clickable {
                        onAction(action, null)
                    }
                }
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isPressed || isActive) Color.White else color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
