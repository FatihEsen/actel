package com.actel.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.actel.dashboard.ui.theme.NeonCyan
import com.actel.dashboard.ui.theme.NeonRed
import com.actel.dashboard.ui.theme.NeonYellow
import com.actel.dashboard.ui.theme.TextPrimary
import com.actel.dashboard.ui.theme.TextSecondary

@Composable
fun GearSpeedDisplay(
    gear: Int,
    gearStr: String,
    speedKmh: Int,
    currentRpm: Int,
    maxRpm: Int,
    modifier: Modifier = Modifier
) {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ==================== 1. EN ÜST: BÜYÜK HIZ + SAĞ ALTINDA KM/H ====================
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
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                lineHeight = 70.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BorderDark)
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "KM/H",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ==================== 2. ORTA: RPM YAZISI (BAR KALDIRILDI) ====================
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = String.format("%,d", currentRpm),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "RPM",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        // ==================== 3. EN ALT: DİJİTAL VİTES KUTUSU ====================
        Box(
            modifier = Modifier
                .width(112.dp)
                .height(92.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF070B12))
                .border(2.5.dp, gearColor.copy(alpha = 0.9f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayGear,
                color = gearColor,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                lineHeight = 60.sp
            )
        }
    }
}
