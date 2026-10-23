package com.actel.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.actel.dashboard.data.model.TyresData
import com.actel.dashboard.ui.theme.BgCard
import com.actel.dashboard.ui.theme.BorderDark
import com.actel.dashboard.ui.theme.NeonCyan
import com.actel.dashboard.ui.theme.NeonGreen
import com.actel.dashboard.ui.theme.NeonRed
import com.actel.dashboard.ui.theme.NeonYellow
import com.actel.dashboard.ui.theme.TextPrimary
import com.actel.dashboard.ui.theme.TyreCold
import com.actel.dashboard.ui.theme.TyreHot
import com.actel.dashboard.ui.theme.TyreOptimal
import com.actel.dashboard.ui.theme.TyreWarm

@Composable
fun CompactTyresCard(
    tyres: TyresData?,
    modifier: Modifier = Modifier
) {
    val pressures = tyres?.pressure ?: listOf(0f, 0f, 0f, 0f)
    val temps = tyres?.tempCore ?: listOf(0f, 0f, 0f, 0f)
    val wears = tyres?.wear ?: listOf(100f, 100f, 100f, 100f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Front Tyres (FL / FR)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactTyreCell(
                tag = "FL",
                pressure = pressures.getOrElse(0) { 0f },
                temp = temps.getOrElse(0) { 0f },
                wear = wears.getOrElse(0) { 100f },
                modifier = Modifier.weight(1f)
            )
            CompactTyreCell(
                tag = "FR",
                pressure = pressures.getOrElse(1) { 0f },
                temp = temps.getOrElse(1) { 0f },
                wear = wears.getOrElse(1) { 100f },
                modifier = Modifier.weight(1f)
            )
        }

        // Rear Tyres (RL / RR)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CompactTyreCell(
                tag = "RL",
                pressure = pressures.getOrElse(2) { 0f },
                temp = temps.getOrElse(2) { 0f },
                wear = wears.getOrElse(2) { 100f },
                modifier = Modifier.weight(1f)
            )
            CompactTyreCell(
                tag = "RR",
                pressure = pressures.getOrElse(3) { 0f },
                temp = temps.getOrElse(3) { 0f },
                wear = wears.getOrElse(3) { 100f },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactTyreCell(
    tag: String,
    pressure: Float,
    temp: Float,
    wear: Float,
    modifier: Modifier = Modifier
) {
    val tempColor = when {
        temp < 65f -> TyreCold
        temp < 75f -> TyreWarm
        temp < 98f -> TyreOptimal
        else -> TyreHot
    }

    val displayWear = if (wear > 100f) wear / 100f else wear

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F141E))
            .border(1.dp, BorderDark.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Row 1: Tag & Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tag,
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${temp.toInt()}°C",
                    color = tempColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Row 2: Pressure & Wear %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%.1f", pressure),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${displayWear.toInt()}%",
                    color = if (displayWear > 75) NeonGreen else if (displayWear > 50) NeonYellow else NeonRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}
