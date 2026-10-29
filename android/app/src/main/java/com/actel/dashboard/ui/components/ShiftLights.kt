package com.actel.dashboard.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actel.dashboard.ui.theme.LedOff
import java.util.Locale

@Composable
fun ShiftLights(
    currentRpm: Int,
    maxRpm: Int,
    hasTurbo: Boolean = false,
    turboBoost: Float = 0f,
    maxTurboBoost: Float = 2.5f,
    hasKers: Boolean = false,
    kersCharge: Float = 0f,
    hasDrs: Boolean = false,
    drsAvailable: Boolean = false,
    drsActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val showTurbo = hasTurbo || turboBoost > 0.05f
    val showKersOrDrs = hasKers || kersCharge > 0.5f || hasDrs

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 1. ÜST BAR: Sidekick tarzı Turbo Basınç Göstergesi (Sadece turbo araçlarda)
        if (showTurbo) {
            TurboBoostBar(
                boost = turboBoost,
                maxBoost = if (maxTurboBoost > 0.1f) maxTurboBoost else 2.5f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. ORTA: 16 LED Shift Light Devir Barı
        ShiftLightsLedBar(
            currentRpm = currentRpm,
            maxRpm = maxRpm,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. ALT BAR: Sidekick tarzı ERS / KERS Batarya & DRS Göstergesi (Sadece hibrit/KERS/DRS araçlarda)
        if (showKersOrDrs) {
            ErsKersBar(
                hasKers = hasKers || kersCharge > 0.5f,
                kersCharge = kersCharge,
                hasDrs = hasDrs,
                drsAvailable = drsAvailable,
                drsActive = drsActive,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 16 LED Kademeli Shift Light Çubuğu
 */
@Composable
private fun ShiftLightsLedBar(
    currentRpm: Int,
    maxRpm: Int,
    modifier: Modifier = Modifier
) {
    val safeMax = if (maxRpm > 1000) maxRpm else 8500
    val rpmRatio = (currentRpm.toFloat() / safeMax.toFloat()).coerceIn(0f, 1.10f)

    // Flash animation at redline (above 96%)
    val isRedline = rpmRatio >= 0.96f
    val infiniteTransition = rememberInfiniteTransition(label = "RedlineFlash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRedline) 0.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(80),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaFlash"
    )

    val idleThreshold = 0.12f
    val redlineThreshold = 0.96f

    val activeLedCount = when {
        currentRpm <= 50 -> 0
        rpmRatio <= idleThreshold -> 1
        else -> {
            val fraction = ((rpmRatio - idleThreshold) / (redlineThreshold - idleThreshold)).coerceIn(0f, 1f)
            1 + (fraction * 15f).toInt().coerceIn(0, 15)
        }
    }

    // Color gradient across 16 dots: Green -> Lime -> Yellow -> Amber -> Orange -> Red
    val ledColors = listOf(
        Color(0xFF00E676), // 0 (Idle dot: Green)
        Color(0xFF00E676), // 1
        Color(0xFF00E676), // 2
        Color(0xFF76FF03), // 3
        Color(0xFFAEEA00), // 4
        Color(0xFFC6FF00), // 5
        Color(0xFFFFEA00), // 6
        Color(0xFFFFD600), // 7
        Color(0xFFFFAB00), // 8
        Color(0xFFFF9100), // 9
        Color(0xFFFF6D00), // 10
        Color(0xFFFF5722), // 11
        Color(0xFFFF3D00), // 12
        Color(0xFFFF1744), // 13
        Color(0xFFFF1744), // 14
        Color(0xFFD50000)  // 15 (Peak: Deep Crimson)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF0D1117))
            .border(0.5.dp, Color(0xFF1E2638), RoundedCornerShape(5.dp))
            .padding(horizontal = 5.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until 16) {
            val isOn = i < activeLedCount
            val baseColor = ledColors[i]

            val ledColor = if (isOn) {
                if (isRedline) {
                    Color(0xFFFF1744).copy(alpha = flashAlpha)
                } else {
                    baseColor
                }
            } else {
                LedOff
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ledColor)
                    .border(
                        0.5.dp,
                        if (isOn) ledColor.copy(alpha = 0.8f) else Color(0xFF181F2C),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

/**
 * Sidekick Tarzı Turbo Boost İnce Çubuğu (Üst Bar)
 */
@Composable
private fun TurboBoostBar(
    boost: Float,
    maxBoost: Float,
    modifier: Modifier = Modifier
) {
    val safeMax = if (maxBoost > 0.2f) maxBoost else 2.5f
    val ratio = (boost / safeMax).coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0A0E17))
            .border(0.5.dp, Color(0xFF1A2333), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "TURBO",
            color = Color(0xFF88A0B8),
            fontSize = 8.5.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        // İnce kanal
        Box(
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(Color(0xFF141A26))
                .border(0.5.dp, Color(0xFF242E42), RoundedCornerShape(2.5.dp))
        ) {
            // Gri / Beyaz dolgu
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .fillMaxHeight()
                    .background(Color(0xFFE2E8F0))
            )

            // Turuncu / Amber uç imleci (Sidekick görselindeki turuncu çizgi)
            if (ratio > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ratio)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFFF9100))
                    )
                }
            }
        }

        Text(
            text = "${String.format(Locale.US, "%.2f", boost)} BAR",
            color = if (ratio > 0.85f) Color(0xFFFF9100) else Color(0xFFDCE6F2),
            fontSize = 8.5.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Sidekick Tarzı ERS / KERS Batarya & DRS İnce Çubuğu (Alt Bar)
 */
@Composable
private fun ErsKersBar(
    hasKers: Boolean,
    kersCharge: Float,
    hasDrs: Boolean,
    drsAvailable: Boolean,
    drsActive: Boolean,
    modifier: Modifier = Modifier
) {
    val chargeRatio = (kersCharge / 100f).coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0A0E17))
            .border(0.5.dp, Color(0xFF1A2333), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (hasKers) {
            Text(
                text = "ERS",
                color = Color(0xFF00E676),
                fontSize = 8.5.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // İnce batarya kanalı
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(Color(0xFF141A26))
                    .border(0.5.dp, Color(0xFF242E42), RoundedCornerShape(2.5.dp))
            ) {
                // Sol referans beyaz çizgi
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                )

                // Canlı yeşil batarya dolgusu
                Box(
                    modifier = Modifier
                        .fillMaxWidth(chargeRatio)
                        .fillMaxHeight()
                        .background(Color(0xFF00E676))
                )

                // Kırmızı tüketim/tahsis imleci (Sidekick görselindeki kırmızı işaret)
                val deliveryPosition = (chargeRatio * 0.72f).coerceIn(0.08f, 0.95f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(deliveryPosition)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFFF1744))
                    )
                }
            }

            Text(
                text = "${kersCharge.toInt()}%",
                color = Color(0xFF00E676),
                fontSize = 8.5.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // DRS Göstergesi
        if (hasDrs) {
            val drsBgColor = when {
                drsActive -> Color(0xFF00E5FF)
                drsAvailable -> Color(0xFF00E676).copy(alpha = 0.25f)
                else -> Color(0xFF141A26)
            }
            val drsTextColor = when {
                drsActive -> Color(0xFF060910)
                drsAvailable -> Color(0xFF00E676)
                else -> Color(0xFF4A5B70)
            }
            val drsBorderColor = when {
                drsActive -> Color(0xFF00E5FF)
                drsAvailable -> Color(0xFF00E676)
                else -> Color(0xFF242E42)
            }
            val drsText = when {
                drsActive -> "DRS OPEN"
                drsAvailable -> "DRS READY"
                else -> "DRS"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(drsBgColor)
                    .border(0.5.dp, drsBorderColor, RoundedCornerShape(3.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = drsText,
                    color = drsTextColor,
                    fontSize = 7.5.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
