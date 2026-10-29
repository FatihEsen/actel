package com.actel.dashboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.actel.dashboard.ui.theme.BgCard
import com.actel.dashboard.ui.theme.BorderDark
import com.actel.dashboard.ui.theme.NeonGreen
import com.actel.dashboard.ui.theme.NeonRed
import com.actel.dashboard.ui.theme.TextDim
import com.actel.dashboard.ui.theme.TextPrimary
import com.actel.dashboard.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TimingDeltaCard(
    currentTime: String,
    lastTime: String,
    bestTime: String,
    ghostDelta: String,
    delta: Float,
    deltaTrend: String,
    isLapCut: Boolean,
    lap: Int,
    sector: Int,
    modifier: Modifier = Modifier
) {
    // Determine ghost time display & color
    val displayGhost = when {
        ghostDelta.isNotEmpty() -> ghostDelta
        delta != 0f -> if (delta >= 0f) "+${String.format(Locale.US, "%.2f", delta)}s" else "-${String.format(Locale.US, "%.2f", kotlin.math.abs(delta))}s"
        else -> "+0.00s"
    }
    val isAhead = displayGhost.startsWith("-") || delta < 0f
    val isZero = displayGhost == "+0.00s" || displayGhost == "0.00s" || delta == 0f
    val ghostColor = when {
        isAhead -> NeonGreen
        isZero -> TextSecondary
        else -> NeonRed
    }

    // Live lap time color: Red if track cut until next lap, else TextPrimary
    val liveLapColor = if (isLapCut) NeonRed else TextPrimary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BgCard)
            .border(1.dp, if (isLapCut) NeonRed.copy(alpha = 0.5f) else BorderDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Üst Satır: Tur & Sektör (veya Cut Uyarısı)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BorderDark)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "TUR $lap",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (isLapCut) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonRed.copy(alpha = 0.2f))
                        .border(1.dp, NeonRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⚠️ GEÇERSİZ (CUT)",
                        color = NeonRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BorderDark)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SEKTÖR S$sector",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. Canlı Tur Süresi (Etiket kaldırıldı, cut durumunda kırmızı)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentTime.isNotEmpty()) currentTime else "0:00.000",
                color = liveLapColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // 3. Ghost Farkı & Dinamik Trend Okları (Tekil Ghost / Delta Alanı)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(BorderDark.copy(alpha = 0.45f))
                .border(1.dp, BorderDark, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GHOST FARKI",
                color = TextDim,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Yeşil Yukarı Ok (⇧) - Zaman kazandıkça / iyileştikçe parlar
                BlockArrowUp(
                    isActive = deltaTrend == "improving",
                    modifier = Modifier.size(15.dp, 20.dp)
                )

                // Ghost Zaman Farkı
                Text(
                    text = displayGhost,
                    color = ghostColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // Kırmızı Aşağı Ok (⇩) - Zaman kaybettikçe / kötüleştikçe parlar
                BlockArrowDown(
                    isActive = deltaTrend == "worsening",
                    modifier = Modifier.size(15.dp, 20.dp)
                )
            }
        }

        // 4. En İyi Tur & Son Tur
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "EN İYİ TUR", color = TextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = if (bestTime.isNotEmpty()) bestTime else "--:--.---",
                    color = Color(0xFFD4AF37), // Gold
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "SON TUR", color = TextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = if (lastTime.isNotEmpty()) lastTime else "--:--.---",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Kullanıcı görselindeki kalın yeşil yukarı blok ok (⇧)
 */
@Composable
fun BlockArrowUp(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFF76FF03) // Lime Green
    val inactiveColor = Color(0xFF76FF03).copy(alpha = 0.22f)
    val color = if (isActive) activeColor else inactiveColor

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.48f)
            lineTo(w * 0.68f, h * 0.48f)
            lineTo(w * 0.68f, h)
            lineTo(w * 0.32f, h)
            lineTo(w * 0.32f, h * 0.48f)
            lineTo(0f, h * 0.48f)
            close()
        }

        if (isActive) {
            drawPath(path, color = color)
            drawPath(
                path,
                color = Color.Black.copy(alpha = 0.45f),
                style = Stroke(width = 2.dp.toPx())
            )
        } else {
            drawPath(
                path,
                color = color,
                style = Stroke(width = 1.8.dp.toPx())
            )
        }
    }
}

/**
 * Kullanıcı görselindeki kalın kırmızı aşağı blok ok (⇩)
 */
@Composable
fun BlockArrowDown(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFFFF1744) // Bright Red
    val inactiveColor = Color(0xFFFF1744).copy(alpha = 0.22f)
    val color = if (isActive) activeColor else inactiveColor

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h)
            lineTo(w, h * 0.52f)
            lineTo(w * 0.68f, h * 0.52f)
            lineTo(w * 0.68f, 0f)
            lineTo(w * 0.32f, 0f)
            lineTo(w * 0.32f, h * 0.52f)
            lineTo(0f, h * 0.52f)
            close()
        }

        if (isActive) {
            drawPath(path, color = color)
            drawPath(
                path,
                color = Color.Black.copy(alpha = 0.45f),
                style = Stroke(width = 2.dp.toPx())
            )
        } else {
            drawPath(
                path,
                color = color,
                style = Stroke(width = 1.8.dp.toPx())
            )
        }
    }
}
