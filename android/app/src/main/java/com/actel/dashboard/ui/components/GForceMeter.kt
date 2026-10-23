package com.actel.dashboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actel.dashboard.ui.theme.BgCard
import com.actel.dashboard.ui.theme.BorderDark
import com.actel.dashboard.ui.theme.NeonCyan
import com.actel.dashboard.ui.theme.TextDim

@Composable
fun GForceMeter(
    lateralG: Float,
    longitudinalG: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.minDimension / 2 - 4).coerceAtLeast(10f)

                // Outer and inner target rings
                drawCircle(
                    color = Color(0xFF263042),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1f)
                )
                drawCircle(
                    color = Color(0xFF1B2332),
                    radius = radius * 0.5f,
                    center = center,
                    style = Stroke(width = 1f)
                )

                // Crosshairs
                drawLine(
                    color = Color(0xFF263042),
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y + radius),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF263042),
                    start = Offset(center.x - radius, center.y),
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = 1f
                )

                // G-point position (mapping max 2.5G to radius)
                val maxG = 2.5f
                val clampedLat = (lateralG / maxG).coerceIn(-1f, 1f)
                val clampedLon = (-longitudinalG / maxG).coerceIn(-1f, 1f) // -lon is accel/decel

                val dotCenter = Offset(
                    x = center.x + clampedLat * radius,
                    y = center.y + clampedLon * radius
                )

                // Draw glowing G-dot
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.35f),
                    radius = 8f,
                    center = dotCenter
                )
                drawCircle(
                    color = NeonCyan,
                    radius = 4.5f,
                    center = dotCenter
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "LAT: ${String.format("%.2f", lateralG)}G",
            color = TextDim,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
