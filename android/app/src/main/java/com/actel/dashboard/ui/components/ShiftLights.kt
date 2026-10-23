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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.actel.dashboard.ui.theme.LedOff

@Composable
fun ShiftLights(
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

    // Idle: 1 dot steadily lit when engine running (currentRpm > 50)
    // Proportional progression from idle up to redline across 16 dots
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
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0D1117))
            .border(0.5.dp, Color(0xFF1E2638), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
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
                    .height(16.dp)
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
