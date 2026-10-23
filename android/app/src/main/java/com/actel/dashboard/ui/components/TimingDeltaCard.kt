package com.actel.dashboard.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun TimingDeltaCard(
    currentTime: String,
    lastTime: String,
    bestTime: String,
    split: String,
    lap: Int,
    sector: Int,
    modifier: Modifier = Modifier
) {
    val isDeltaNegative = split.startsWith("-")
    val deltaColor = when {
        isDeltaNegative -> NeonGreen
        split == "+0.000" || split == "0.000" -> TextSecondary
        else -> NeonRed
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BgCard)
            .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
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

        Spacer(modifier = Modifier.height(4.dp))

        // Current Lap & Delta
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "CANLI SÜRE", color = TextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = if (currentTime.isNotEmpty()) currentTime else "0:00.000",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "DELTA (EN İYİ)", color = TextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = if (split.isNotEmpty()) split else "+0.000",
                    color = deltaColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Best & Last Lap
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
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
