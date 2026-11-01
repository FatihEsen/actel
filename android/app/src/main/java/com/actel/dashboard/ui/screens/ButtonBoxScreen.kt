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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    val isPitLimiterOn = telemetry?.physics?.pitLimiterOn == true || telemetry?.indicators?.pitLimiter == true
    val isDrsOn = telemetry?.physics?.drs == true || telemetry?.indicators?.drsActive == true
    val isHazardOn = telemetry?.indicators?.hazard == true
    val isHeadlightsOn = telemetry?.indicators?.headlights == true
    val isTurnLeftOn = telemetry?.indicators?.turnLeft == true || isHazardOn
    val isTurnRightOn = telemetry?.indicators?.turnRight == true || isHazardOn

    var wiperStage by remember { mutableIntStateOf(0) }

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
                BoxButton(
                    title = "💡 FARLAR",
                    action = "headlights",
                    color = if (isHeadlightsOn) Color(0xFF90CAF9) else Color(0xFF64B5F6),
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isActive = isHeadlightsOn
                )
                BoxButton("🔆 SELEKTÖR", "flasher", Color(0xFFFFF176), onAction, Modifier.weight(1f))
            }
            // Satır 2: İÇ AYDINLATMA / SİS FARI
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("🛋️ İÇ IŞIK", "extra_a", Color(0xFFBA68C8), onAction, Modifier.weight(1f))
                BoxButton("🌫️ SİS FARI", "extra_b", Color(0xFFE040FB), onAction, Modifier.weight(1f))
            }
            // Satır 3: DÖRTLÜ / KERS
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton(
                    title = "⚠️ DÖRTLÜ",
                    action = "hazard",
                    color = NeonRed,
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isActive = isHazardOn
                )
                BoxButton(
                    title = "⚡ KERS",
                    action = "kers",
                    color = Color(0xFF00E676),
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isHold = true
                )
            }
            // Satır 4: SOL SİNYAL / KAMERA
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton(
                    title = "◀️ SOL SİNYAL",
                    action = "turn_left",
                    color = Color(0xFFFFB300),
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isActive = isTurnLeftOn
                )
                BoxButton("🎥 KAMERA", "camera_change", Color(0xFF42A5F5), onAction, Modifier.weight(1f))
            }
            // Satır 5: SOLA BAK / ARKA BAKIŞ
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("👁️◀ SOLA BAK", "look_left", Color(0xFF00E5FF), onAction, Modifier.weight(1f))
                BoxButton("🔼 ARKA", "look_back", Color(0xFF90A4AE), onAction, Modifier.weight(1f))
            }
        }

        // ==================== PANEL 3: SİLECEK, KORNA, DRS, SAĞ SİNYAL, SAĞA BAK & START (Sütun 5 & 6) ====================
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Satır 1: SİLECEK (5 Nokta Kademeli) / PIT LIMITER
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                WiperButton(
                    wiperStage = wiperStage,
                    onClick = {
                        wiperStage = (wiperStage + 1) % 6
                        onAction("wipers", null)
                    },
                    modifier = Modifier.weight(1f)
                )
                BoxButton(
                    title = "🛑 PIT LIM",
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
            // Satır 3: DRS / YENİDEN BAŞLAT
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton(
                    title = "💨 DRS",
                    action = "drs",
                    color = if (isDrsOn) NeonCyan else Color(0xFF00E676),
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isActive = isDrsOn
                )
                BoxButton("🔄 YENİDEN", "restart_session", Color(0xFFFF5722), onAction, Modifier.weight(1f))
            }
            // Satır 4: SAĞ SİNYAL / DURAKLAT
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton(
                    title = "SAĞ SİNYAL ▶️",
                    action = "turn_right",
                    color = Color(0xFFFFB300),
                    onAction = onAction,
                    modifier = Modifier.weight(1f),
                    isActive = isTurnRightOn
                )
                BoxButton("⏸️ DURAKLAT", "pause", Color(0xFFE57373), onAction, Modifier.weight(1f))
            }
            // Satır 5: SAĞA BAK / START
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                BoxButton("SAĞA BAK ▶️👁️", "look_right", Color(0xFF00E5FF), onAction, Modifier.weight(1f))
                BoxButton("🏁 START", "starter", NeonGreen, onAction, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WiperButton(
    wiperStage: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val baseColor = Color(0xFF81C784)
    val effectiveBg = if (isPressed || wiperStage > 0) baseColor.copy(alpha = 0.25f) else BgCard
    val effectiveBorder = if (isPressed || wiperStage > 0) baseColor else baseColor.copy(alpha = 0.65f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(effectiveBg)
            .border(1.5.dp, effectiveBorder, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🌧️ SİLECEK",
                color = if (isPressed || wiperStage > 0) Color.White else baseColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.padding(top = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 5) {
                    val dotColor = if (wiperStage > 0 && i < wiperStage) NeonGreen else Color(0xFF29B6F6)
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
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
