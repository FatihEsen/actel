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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.actel.dashboard.ui.theme.DigitalNumbersFontFamily
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
    absLevel: Int = 0,
    hasAbs: Boolean = true,
    tcActive: Boolean = false,
    tcLevel: Int = 0,
    hasTc: Boolean = true,
    drsAvailable: Boolean = false,
    drsActive: Boolean = false,
    handbrakeActive: Boolean = false,
    pitActive: Boolean = false,
    brakeBias: Float = 0.58f,
    fuelText: String = "0L",
    isFuelLow: Boolean = false,
    tempText: String = "0°C",
    isOverheat: Boolean = false,
    damageText: String = "OK",
    isDamageWarn: Boolean = false,
    tyreText: String = "GRIP",
    isTyreSlip: Boolean = false,
    onAction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
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

    val rapidSlipTransition = rememberInfiniteTransition(label = "slipBlink")
    val slipBlinkAlpha by rapidSlipTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 200
                1f at 0
                1f at 99
                0f at 100
                0f at 200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "slipBlinkAlpha"
    )

    val displayGear = when {
        gear == 0 -> "R"
        gear == 1 -> "N"
        gear > 1 -> (gear - 1).toString()
        gearStr.isNotEmpty() -> gearStr
        else -> "N"
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
        // ==================== SOL KANAT (7 Gösterge Kutusu) ====================
        Column(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Sol Sinyal
            val isLeftBlinking = isLeftTurnActive && (flasherAlpha > 0.5f)
            IndicatorBox(
                isActive = isLeftBlinking,
                activeColor = NeonYellow,
                onClick = { onAction("turn_left") }
            ) {
                Text(
                    text = "◀",
                    color = if (isLeftBlinking) Color.White else NeonYellow.copy(alpha = 0.50f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // 2. Farlar
            IndicatorBox(
                isActive = isLightsActive,
                activeColor = NeonGreen,
                onClick = { onAction("headlights") }
            ) {
                Text(
                    text = "💡",
                    color = if (isLightsActive) Color.White else NeonGreen.copy(alpha = 0.50f),
                    fontSize = 14.sp
                )
            }

            // 3. Selektör
            IndicatorBox(
                isActive = isFlasherActive,
                activeColor = NeonCyan,
                onClick = { onAction("flasher") }
            ) {
                Text(
                    text = "⚡",
                    color = if (isFlasherActive) Color.White else NeonCyan.copy(alpha = 0.35f),
                    fontSize = 14.sp
                )
            }

            // 4. ABS Rozeti (Normalde soluk, devreye girdiğinde flaş yaparak yanıp söner)
            val isAbsBlinking = absActive && (slipBlinkAlpha > 0.5f)
            IndicatorBox(
                isActive = isAbsBlinking,
                activeColor = NeonRed,
                onClick = { onAction("abs_up") }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ABS",
                        color = if (isAbsBlinking) Color.White else NeonRed.copy(alpha = 0.50f),
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    val absText = when {
                        !hasAbs -> "N/A"
                        absLevel > 0 -> absLevel.toString()
                        else -> "OFF"
                    }
                    Text(
                        text = absText,
                        color = if (isAbsBlinking) Color.White else Color(0xFFE2E8F0),
                        fontSize = 9.5.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 5. El Freni Rozeti
            IndicatorBox(
                isActive = handbrakeActive,
                activeColor = NeonRed,
                onClick = { onAction("handbrake") }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "(P)",
                        color = if (handbrakeActive) Color.White else NeonRed.copy(alpha = 0.75f),
                        fontSize = 9.5.sp,
                        lineHeight = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "FREN",
                        color = if (handbrakeActive) Color.White else Color(0xFFE2E8F0),
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 6. Yakıt Rozeti (Değer ve Başlık, TC/ABS gibi)
            IndicatorBox(
                isActive = isFuelLow,
                activeColor = Color(0xFFFFB300),
                onClick = {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⛽ YKT",
                        color = if (isFuelLow) Color.White else Color(0xFFFFB300).copy(alpha = 0.85f),
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = fuelText,
                        color = if (isFuelLow) Color.White else Color(0xFFE2E8F0),
                        fontSize = 9.sp,
                        lineHeight = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 7. Hasar Rozeti (Değer ve Başlık)
            IndicatorBox(
                isActive = isDamageWarn,
                activeColor = NeonRed,
                onClick = {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚠️ HSR",
                        color = if (isDamageWarn) Color.White else NeonRed.copy(alpha = 0.75f),
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = damageText,
                        color = if (isDamageWarn) Color.White else Color(0xFFE2E8F0),
                        fontSize = 9.sp,
                        lineHeight = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // ==================== ORTA OMURGA: HIZ, RPM, DİJİTAL VİTES ====================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. HIZ GÖSTERGESİ (Büyük, Net ve Merkeze Hizalı Tipografi)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                val speedFontSize = if (speedKmh >= 100) 62.sp else 72.sp
                Text(
                    text = speedKmh.toString(),
                    color = Color.White,
                    fontSize = speedFontSize,
                    lineHeight = speedFontSize,
                    fontWeight = FontWeight.Normal,
                    fontFamily = DigitalNumbersFontFamily,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "KM/H",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            // 2. RPM DEĞERİ (Canlı & Okunaklı)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val formattedRpm = String.format("%,d", currentRpm).replace(',', '.')
                Text(
                    text = formattedRpm,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = " RPM",
                    color = TextDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 3. 7-SEGMENT DİJİTAL VİTES GÖSTERGESİ
            val gearNeonColor = when (displayGear) {
                "R" -> NeonRed
                "N" -> NeonYellow
                else -> NeonCyan
            }

            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(106.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B12))
                    .border(2.dp, gearNeonColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayGear,
                    color = gearNeonColor,
                    fontSize = 82.sp,
                    lineHeight = 82.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = DigitalNumbersFontFamily,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ==================== SAĞ KANAT (7 Gösterge Kutusu) ====================
        Column(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Sağ Sinyal
            val isRightBlinking = isRightTurnActive && (flasherAlpha > 0.5f)
            IndicatorBox(
                isActive = isRightBlinking,
                activeColor = NeonYellow,
                onClick = { onAction("turn_right") }
            ) {
                Text(
                    text = "▶",
                    color = if (isRightBlinking) Color.White else NeonYellow.copy(alpha = 0.50f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // 2. Dörtlü İkaz
            val isHazardBlinking = isHazardActive && (flasherAlpha > 0.5f)
            IndicatorBox(
                isActive = isHazardBlinking,
                activeColor = NeonRed,
                onClick = { onAction("hazard") }
            ) {
                Text(
                    text = "⚠️",
                    color = if (isHazardBlinking) Color.White else NeonRed.copy(alpha = 0.35f),
                    fontSize = 14.sp
                )
            }

            // 3. Pit Limiter Rozeti
            val isPitFlashingOn = pitActive && (flasherAlpha > 0.5f)
            val pitVividColor = Color(0xFFFFD600)
            IndicatorBox(
                isActive = isPitFlashingOn,
                activeColor = pitVividColor,
                onClick = { onAction("pit_limiter") }
            ) {
                Text(
                    text = "PIT",
                    color = if (isPitFlashingOn) Color(0xFF050810) else if (pitActive) pitVividColor else pitVividColor.copy(alpha = 0.60f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 4. TC Rozeti (Normalde soluk, devreye girdiğinde flaş yaparak yanıp söner)
            val isTcBlinking = tcActive && (slipBlinkAlpha > 0.5f)
            IndicatorBox(
                isActive = isTcBlinking,
                activeColor = NeonCyan,
                onClick = { onAction("tc_up") }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "TC",
                        color = if (isTcBlinking) Color.White else NeonCyan.copy(alpha = 0.50f),
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    val tcText = when {
                        !hasTc -> "N/A"
                        tcLevel > 0 -> tcLevel.toString()
                        else -> "OFF"
                    }
                    Text(
                        text = tcText,
                        color = if (isTcBlinking) Color.White else Color(0xFFE2E8F0),
                        fontSize = 9.5.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 5. Brake Bias Rozeti
            IndicatorBox(
                isActive = false,
                activeColor = Color(0xFFB0BEC5),
                onClick = {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "BB",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%.1f%%", brakeBias * 100f),
                        color = Color(0xFFE2E8F0),
                        fontSize = 8.5.sp,
                        lineHeight = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 6. Sıcaklık Rozeti (Değer ve Başlık, TC/ABS gibi)
            IndicatorBox(
                isActive = isOverheat,
                activeColor = Color(0xFFFF5252),
                onClick = {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🌡️ TMP",
                        color = if (isOverheat) Color.White else Color(0xFFFF5252).copy(alpha = 0.85f),
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = tempText,
                        color = if (isOverheat) Color.White else Color(0xFFE2E8F0),
                        fontSize = 9.sp,
                        lineHeight = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 7. Lastik Tutuş / Kayma Rozeti (Değer ve Başlık)
            IndicatorBox(
                isActive = isTyreSlip,
                activeColor = NeonCyan,
                onClick = {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🛞 LST",
                        color = if (isTyreSlip) Color.White else NeonCyan.copy(alpha = 0.75f),
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = tyreText,
                        color = if (isTyreSlip) Color.White else Color(0xFFE2E8F0),
                        fontSize = 8.5.sp,
                        lineHeight = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Tüm 14 gösterge kutusu için standartlaştırılmış tasarım:
 * - Deaktifken: Standart koyu zarif çerçeve (Color(0xFF1E293B)), homojen arka plan (Color(0xFF0B1019)).
 * - Aktifken: Tam parlak neon aktif çerçeve (activeColor), neon zemin dolgusu (alpha = 0.24f).
 */
@Composable
private fun ColumnScope.IndicatorBox(
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val idleBorderColor = Color(0xFF1E293B)
    val idleBgColor = Color(0xFF0B1019)

    val activeBorderColor = activeColor
    val activeBgColor = activeColor.copy(alpha = 0.24f)

    val boxBg = if (isActive) activeBgColor else idleBgColor
    val boxBorder = if (isActive) activeBorderColor else idleBorderColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(boxBg)
            .border(1.dp, boxBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
