package com.actel.dashboard.ui.components

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
import com.actel.dashboard.ui.theme.LedBlue
import com.actel.dashboard.ui.theme.LedGreen
import com.actel.dashboard.ui.theme.LedRed
import com.actel.dashboard.ui.theme.TextDim
import com.actel.dashboard.ui.theme.TextSecondary

@Composable
fun PedalBars(
    gas: Float,
    brake: Float,
    clutch: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VerticalPedalBar(label = "CLUTCH", value = clutch.coerceIn(0f, 1f), color = LedBlue)
            VerticalPedalBar(label = "BRAKE", value = brake.coerceIn(0f, 1f), color = LedRed)
            VerticalPedalBar(label = "THROTTLE", value = gas.coerceIn(0f, 1f), color = LedGreen)
        }
    }
}

@Composable
private fun VerticalPedalBar(
    label: String,
    value: Float,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0D1117))
                .border(0.5.dp, Color(0xFF212836), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${(value * 100).toInt()}%",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label.take(3),
            color = TextDim,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
