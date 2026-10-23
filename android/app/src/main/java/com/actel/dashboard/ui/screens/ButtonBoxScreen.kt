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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.actel.dashboard.ui.theme.TextDim
import com.actel.dashboard.ui.theme.TextSecondary

@Composable
fun ButtonBoxScreen(
    telemetry: TelemetryData?,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPitLimiterOn = telemetry?.physics?.pitLimiterOn == true
    val isDrsOn = telemetry?.physics?.drs == true

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ==================== SÜTUN 1: YARDIMCILAR & YARIŞ ====================
        Column(
            modifier = Modifier.weight(1.1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: TC + / TC -
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("TC +", "tc_up", NeonCyan, { onAction("tc_up") }, Modifier.weight(1f))
                BoxButton("TC -", "tc_down", NeonCyan.copy(alpha = 0.75f), { onAction("tc_down") }, Modifier.weight(1f))
            }
            // Row 2: ABS + / ABS -
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("ABS +", "abs_up", NeonYellow, { onAction("abs_up") }, Modifier.weight(1f))
                BoxButton("ABS -", "abs_down", NeonYellow.copy(alpha = 0.75f), { onAction("abs_down") }, Modifier.weight(1f))
            }
            // Row 3: BIAS + / BIAS -
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("BIAS +", "bb_up", NeonGreen, { onAction("bb_up") }, Modifier.weight(1f))
                BoxButton("BIAS -", "bb_down", NeonGreen.copy(alpha = 0.75f), { onAction("bb_down") }, Modifier.weight(1f))
            }
            // Row 4: PIT LIMITER / DRS
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton(
                    title = if (isPitLimiterOn) "PIT LIM (ON)" else "PIT LIMITER",
                    sub = "pit_limiter",
                    color = if (isPitLimiterOn) NeonGreen else NeonOrange,
                    onClick = { onAction("pit_limiter") },
                    modifier = Modifier.weight(1f)
                )
                BoxButton(
                    title = if (isDrsOn) "DRS (ON)" else "DRS",
                    sub = "drs",
                    color = if (isDrsOn) NeonGreen else NeonCyan,
                    onClick = { onAction("drs") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ==================== SÜTUN 2: AYDINLATMA, SİNYAL & HAZARD ====================
        Column(
            modifier = Modifier.weight(1.1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: FARLAR / SELEKTÖR
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("FARLAR", "headlights", Color(0xFF4FC3F7), { onAction("headlights") }, Modifier.weight(1f))
                BoxButton("SELEKTÖR", "flasher", Color(0xFFFFF176), { onAction("flasher") }, Modifier.weight(1f))
            }
            // Row 2: SİLECEK / KORNA
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("SİLECEK", "wipers", Color(0xFF81C784), { onAction("wipers") }, Modifier.weight(1f))
                BoxButton("KORNA", "horn", Color(0xFFFFB74D), { onAction("horn") }, Modifier.weight(1f))
            }
            // Row 3: SOL SİNYAL / SAĞ SİNYAL
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("◀ SOL SİNYAL", "turn_left", Color(0xFFFFB300), { onAction("turn_left") }, Modifier.weight(1f))
                BoxButton("SAĞ SİNYAL ▶", "turn_right", Color(0xFFFFB300), { onAction("turn_right") }, Modifier.weight(1f))
            }
            // Row 4: DÖRTLÜ / HAZARD (Full Width)
            Row(modifier = Modifier.weight(1f)) {
                BoxButton("⚠️ DÖRTLÜ FLAŞÖR (HAZARD)", "hazard", NeonRed, { onAction("hazard") }, Modifier.fillMaxWidth())
            }
        }

        // ==================== SÜTUN 3: SEANS, KAMERA & BAKIŞ ====================
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: PİTE DÖN / KAMERA
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("PİTE DÖN", "teleport_pits", NeonRed, { onAction("teleport_pits") }, Modifier.weight(1f))
                BoxButton("KAMERA", "camera_change", Color(0xFF64B5F6), { onAction("camera_change") }, Modifier.weight(1f))
            }
            // Row 2: START / DURAKLAT
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("START", "starter", NeonGreen, { onAction("starter") }, Modifier.weight(1f))
                BoxButton("DURAKLAT", "pause", Color(0xFFE57373), { onAction("pause") }, Modifier.weight(1f))
            }
            // Row 3: BAKIŞLAR (SOLA / ARKAYA / SAĞA)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                BoxButton("SOLA", "look_left", TextSecondary, { onAction("look_left") }, Modifier.weight(1f))
                BoxButton("ARKA", "look_back", TextSecondary, { onAction("look_back") }, Modifier.weight(1f))
                BoxButton("SAĞA", "look_right", TextSecondary, { onAction("look_right") }, Modifier.weight(1f))
            }
            // Row 4: YENİDEN BAŞLAT
            Row(modifier = Modifier.weight(1f)) {
                BoxButton("🔄 YENİDEN BAŞLAT", "restart_session", Color(0xFFFF7043), { onAction("restart_session") }, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun BoxButton(
    title: String,
    sub: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(BgCard)
            .border(1.5.dp, color.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                color = TextDim,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
