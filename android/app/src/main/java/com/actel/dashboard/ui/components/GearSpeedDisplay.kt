package com.actel.dashboard.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actel.dashboard.data.model.IndicatorData
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
fun GearSpeedDisplay(
    gear: Int,
    gearStr: String,
    speedKmh: Int,
    currentRpm: Int,
    maxRpm: Int,
    indicators: IndicatorData?,
    absActive: Boolean = false,
    tcActive: Boolean = false,
    drsAvailable: Boolean = false,
    drsActive: Boolean = false,
    pitActive: Boolean = false,
    brakeBias: Float = 0.58f,
    onAction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Automotive flasher keyframes: 0-299ms ON (1f), 300-600ms OFF (0f)
    val infiniteTransition = rememberInfiniteTransition(label = "indicatorBlink")
    val flasherAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 600
                1f at 0
                1f at 299
                0f at 300
                0f at 600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "flasherAlpha"
    )

    // Format Gear: 0 -> R, 1 -> N, 2+ -> 1, 2, 3...
    val displayGear = when {
        gear == 0 -> "R"
        gear == 1 -> "N"
        gear > 1 -> (gear - 1).toString()
        gearStr.isNotEmpty() -> gearStr
        else -> "N"
    }

    val gearColor = when (displayGear) {
        "R" -> NeonRed
        "N" -> NeonYellow
        else -> NeonCyan
    }

    val isHazardActive = indicators?.hazard == true
    val isLeftTurnActive = indicators?.turnLeft == true || isHazardActive
    val isRightTurnActive = indicators?.turnRight == true || isHazardActive
    val isLightsActive = indicators?.headlights == true
    val isFlasherActive = indicators?.flasher == true

    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ==================== SOL KANAT (5 Gösterge Kutusu) ====================
        Column(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Sol Sinyal (Aktifken yanıp söner, boştayken canlı sarı)
            val leftAlpha = if (isLeftTurnActive) flasherAlpha else 0.85f
            IndicatorBox(
                isActive = isLeftTurnActive,
                activeColor = NeonYellow,
                idleColor = NeonYellow.copy(alpha = 0.85f),
                onClick = { onAction("turn_left") }
            ) {
                Text(
                    text = "◀",
                    color = if (isLeftTurnActive) {
                        NeonYellow.copy(alpha = if (leftAlpha > 0.5f) 1f else 0.15f)
                    } else {
                        NeonYellow.copy(alpha = 0.85f)
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // 2. Farlar (Kısa Far: Kapalıyken sönük/koyu, Açılınca parlak neon yeşil)
            IndicatorBox(
                isActive = isLightsActive,
                activeColor = NeonGreen,
                idleColor = Color(0xFF4B6354).copy(alpha = 0.35f),
                onClick = { onAction("headlights") }
            ) {
                Text(
                    text = "💡",
                    color = if (isLightsActive) NeonGreen else Color(0xFF5A6E64),
                    fontSize = 16.sp
                )
            }

            // 3. Selektör (Uzun Far / Flaşör: Kapalıyken sönük, Açılınca parlak camgöbeği)
            IndicatorBox(
                isActive = isFlasherActive,
                activeColor = NeonCyan,
                idleColor = Color(0xFF436575).copy(alpha = 0.35f),
                onClick = { onAction("flasher") }
            ) {
                Text(
                    text = "⚡",
                    color = if (isFlasherActive) NeonCyan else Color(0xFF4B6274),
                    fontSize = 16.sp
                )
            }

            // 4. Dörtlü İkaz (Aktifken yanıp söner, boştayken canlı kırmızı)
            val hazardAlpha = if (isHazardActive) flasherAlpha else 0.85f
            IndicatorBox(
                isActive = isHazardActive,
                activeColor = NeonRed,
                idleColor = NeonRed.copy(alpha = 0.85f),
                onClick = { onAction("hazard") }
            ) {
                Text(
                    text = "⚠️",
                    color = if (isHazardActive) {
                        NeonRed.copy(alpha = if (hazardAlpha > 0.5f) 1f else 0.15f)
                    } else {
                        NeonRed.copy(alpha = 0.85f)
                    },
                    fontSize = 15.sp
                )
            }

            // 5. ABS Rozeti
            IndicatorBox(
                isActive = absActive,
                activeColor = NeonRed,
                idleColor = Color(0xFFFF5064).copy(alpha = 0.25f),
                onClick = {}
            ) {
                Text(
                    text = "ABS",
                    color = if (absActive) Color.White else Color(0xFFFF5064).copy(alpha = 0.45f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ==================== ORTA OMURGA: HIZ (ÇERÇEVESİZ), RPM, VİTES ====================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hız Display (Temiz, Çerçevesiz KM/H)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = "$speedKmh",
                    color = TextPrimary,
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 66.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "KM/H",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // 2. RPM Satırı
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format("%,d", currentRpm),
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "RPM",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // 3. Dijital Vites Kutusu
            Box(
                modifier = Modifier
                    .width(105.dp)
                    .height(86.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF070B12))
                    .border(2.5.dp, gearColor.copy(alpha = 0.9f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayGear,
                    color = gearColor,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 56.sp
                )
            }
        }

        // ==================== SAĞ KANAT (5 Gösterge Kutusu) ====================
        Column(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Sağ Sinyal (Aktifken yanıp söner, boştayken canlı sarı)
            val rightAlpha = if (isRightTurnActive) flasherAlpha else 0.85f
            IndicatorBox(
                isActive = isRightTurnActive,
                activeColor = NeonYellow,
                idleColor = NeonYellow.copy(alpha = 0.85f),
                onClick = { onAction("turn_right") }
            ) {
                Text(
                    text = "▶",
                    color = if (isRightTurnActive) {
                        NeonYellow.copy(alpha = if (rightAlpha > 0.5f) 1f else 0.15f)
                    } else {
                        NeonYellow.copy(alpha = 0.85f)
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // 2. DRS Rozeti
            IndicatorBox(
                isActive = drsAvailable || drsActive,
                activeColor = NeonGreen,
                idleColor = Color(0xFF00FF88).copy(alpha = 0.25f),
                onClick = { onAction("drs") }
            ) {
                Text(
                    text = "DRS",
                    color = if (drsAvailable || drsActive) NeonGreen else TextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 3. Pit Limiter Rozeti
            IndicatorBox(
                isActive = pitActive,
                activeColor = NeonYellow,
                idleColor = Color(0xFFFFB700).copy(alpha = 0.25f),
                onClick = { onAction("pit_limiter") }
            ) {
                Text(
                    text = "PIT",
                    color = if (pitActive) Color(0xFF070B12) else Color(0xFFFFB700).copy(alpha = 0.45f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 4. TC Rozeti
            IndicatorBox(
                isActive = tcActive,
                activeColor = NeonYellow,
                idleColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                onClick = {}
            ) {
                Text(
                    text = "TC",
                    color = if (tcActive) Color(0xFF070B12) else Color(0xFF00F0FF).copy(alpha = 0.45f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 5. BB Rozeti (Değer altında yazan)
            val bbPercent = String.format("%.1f%%", brakeBias * 100f)
            IndicatorBox(
                isActive = false,
                activeColor = NeonCyan,
                idleColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
                onClick = {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "BB",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 10.sp
                    )
                    Text(
                        text = bbPercent,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicatorBox(
    isActive: Boolean,
    activeColor: Color,
    idleColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bg = if (isActive) activeColor.copy(alpha = 0.32f) else Color(0xFF141822)
    val borderCol = if (isActive) activeColor else BorderDark

    Box(
        modifier = modifier
            .size(44.dp, 36.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .border(1.2.dp, borderCol, RoundedCornerShape(7.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
