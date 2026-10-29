package com.actel.dashboard.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

            // 4. DRS Rozeti (Kullanıcı isteğiyle Sol Kanat 4. Sıraya Taşındı)
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

        // ==================== ORTA OMURGA: HIZ (BÜYÜTÜLDÜ), RPM (BÜYÜTÜLDÜ), DİJİTAL VİTES ====================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hız Display (Büyütüldü: 78.sp)
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
                    fontSize = 78.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 74.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "KM/H",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // 2. RPM Satırı (Büyütüldü: 26.sp)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format("%,d", currentRpm),
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "RPM",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            // 3. Otantik 7-Segment Dijital Vites Kutusu
            Box(
                modifier = Modifier
                    .width(115.dp)
                    .height(94.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF060910))
                    .border(2.5.dp, gearColor.copy(alpha = 0.9f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                SevenSegmentGear(
                    gear = displayGear,
                    color = gearColor,
                    modifier = Modifier.size(52.dp, 72.dp)
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

            // 2. Dörtlü İkaz (Kullanıcı isteğiyle Sağ Kanat 2. Sıraya Taşındı)
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

            // 5. Brake Bias Rozeti (BB Başlığı ve Altında Yüzdelik Değer)
            IndicatorBox(
                isActive = false,
                activeColor = Color(0xFFB0BEC5),
                idleColor = Color(0xFF78909C).copy(alpha = 0.35f),
                onClick = {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "BB",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%.1f%%", brakeBias * 100f),
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Otantik 7-Segment Motorsport Dijital Vites Çizici
 * R, N ve 1-9 viteslerini dijital LCD segmentleri şeklinde çizer.
 */
@Composable
fun SevenSegmentGear(
    gear: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val inactiveColor = color.copy(alpha = 0.08f)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Segment kalınlığı ve boşlukları
        val t = w * 0.18f
        val g = t * 0.22f
        val halfH = h * 0.5f

        // 7-Segment Aktiflik Mantığı
        val segA: Boolean
        val segB: Boolean
        val segC: Boolean
        val segD: Boolean
        val segE: Boolean
        val segF: Boolean
        val segG: Boolean
        var drawDiagR = false
        var drawDiagN = false

        when (gear) {
            "0" -> { segA = true; segB = true; segC = true; segD = true; segE = true; segF = true; segG = false }
            "1" -> { segA = false; segB = true; segC = true; segD = false; segE = false; segF = false; segG = false }
            "2" -> { segA = true; segB = true; segC = false; segD = true; segE = true; segF = false; segG = true }
            "3" -> { segA = true; segB = true; segC = true; segD = true; segE = false; segF = false; segG = true }
            "4" -> { segA = false; segB = true; segC = true; segD = false; segE = false; segF = true; segG = true }
            "5" -> { segA = true; segB = false; segC = true; segD = true; segE = false; segF = true; segG = true }
            "6" -> { segA = true; segB = false; segC = true; segD = true; segE = true; segF = true; segG = true }
            "7" -> { segA = true; segB = true; segC = true; segD = false; segE = false; segF = false; segG = false }
            "8" -> { segA = true; segB = true; segC = true; segD = true; segE = true; segF = true; segG = true }
            "9" -> { segA = true; segB = true; segC = true; segD = true; segE = false; segF = true; segG = true }
            "N" -> { segA = false; segB = true; segC = true; segD = false; segE = true; segF = true; segG = false; drawDiagN = true }
            "R" -> { segA = true; segB = true; segC = false; segD = false; segE = true; segF = true; segG = true; drawDiagR = true }
            else -> { segA = false; segB = false; segC = false; segD = false; segE = false; segF = false; segG = true }
        }

        // Segment A: Üst Yatay
        val pathA = Path().apply {
            moveTo(t + g, 0f)
            lineTo(w - t - g, 0f)
            lineTo(w - t - g - t * 0.5f, t)
            lineTo(t + g + t * 0.5f, t)
            close()
        }
        drawPath(pathA, if (segA) color else inactiveColor)

        // Segment B: Sağ Üst Dikey
        val pathB = Path().apply {
            moveTo(w, t + g)
            lineTo(w, halfH - g * 0.5f)
            lineTo(w - t, halfH - g * 0.5f - t * 0.5f)
            lineTo(w - t, t + g + t * 0.5f)
            close()
        }
        drawPath(pathB, if (segB) color else inactiveColor)

        // Segment C: Sağ Alt Dikey
        val pathC = Path().apply {
            moveTo(w, halfH + g * 0.5f)
            lineTo(w, h - t - g)
            lineTo(w - t, h - t - g - t * 0.5f)
            lineTo(w - t, halfH + g * 0.5f + t * 0.5f)
            close()
        }
        drawPath(pathC, if (segC) color else inactiveColor)

        // Segment D: Alt Yatay
        val pathD = Path().apply {
            moveTo(t + g + t * 0.5f, h - t)
            lineTo(w - t - g - t * 0.5f, h - t)
            lineTo(w - t - g, h)
            lineTo(t + g, h)
            close()
        }
        drawPath(pathD, if (segD) color else inactiveColor)

        // Segment E: Sol Alt Dikey
        val pathE = Path().apply {
            moveTo(0f, halfH + g * 0.5f)
            lineTo(t, halfH + g * 0.5f + t * 0.5f)
            lineTo(t, h - t - g - t * 0.5f)
            lineTo(0f, h - t - g)
            close()
        }
        drawPath(pathE, if (segE) color else inactiveColor)

        // Segment F: Sol Üst Dikey
        val pathF = Path().apply {
            moveTo(0f, t + g)
            lineTo(t, t + g + t * 0.5f)
            lineTo(t, halfH - g * 0.5f - t * 0.5f)
            lineTo(0f, halfH - g * 0.5f)
            close()
        }
        drawPath(pathF, if (segF) color else inactiveColor)

        // Segment G: Orta Yatay (Hexagonal)
        val pathG = Path().apply {
            moveTo(t + g, halfH)
            lineTo(t + g + t * 0.5f, halfH - t * 0.5f)
            lineTo(w - t - g - t * 0.5f, halfH - t * 0.5f)
            lineTo(w - t - g, halfH)
            lineTo(w - t - g - t * 0.5f, halfH + t * 0.5f)
            lineTo(t + g + t * 0.5f, halfH + t * 0.5f)
            close()
        }
        drawPath(pathG, if (segG) color else inactiveColor)

        // Özel 'R' Harfi Sağ Alt Çapraz Bacak
        if (drawDiagR) {
            val pathR = Path().apply {
                moveTo(t + g, halfH + g)
                lineTo(t + g + t, halfH + g)
                lineTo(w - g, h - g)
                lineTo(w - t - g, h - g)
                close()
            }
            drawPath(pathR, color)
        }

        // Özel 'N' Harfi İç Çapraz Çizgi
        if (drawDiagN) {
            val pathN = Path().apply {
                moveTo(t, t + g)
                lineTo(t + t * 0.8f, t + g)
                lineTo(w - t, h - t - g)
                lineTo(w - t - t * 0.8f, h - t - g)
                close()
            }
            drawPath(pathN, color)
        }
    }
}

@Composable
private fun IndicatorBox(
    isActive: Boolean,
    activeColor: Color,
    idleColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val boxBg = if (isActive) activeColor.copy(alpha = 0.28f) else Color(0xFF0F1522)
    val boxBorder = if (isActive) activeColor else idleColor

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(boxBg)
            .border(1.2.dp, boxBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
