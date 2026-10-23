package com.actel.dashboard.ui.screens

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
import com.actel.dashboard.data.model.TelemetryData
import com.actel.dashboard.ui.theme.BgCard
import com.actel.dashboard.ui.theme.BorderDark
import com.actel.dashboard.ui.theme.NeonCyan
import com.actel.dashboard.ui.theme.NeonGreen
import com.actel.dashboard.ui.theme.NeonRed
import com.actel.dashboard.ui.theme.NeonYellow
import com.actel.dashboard.ui.theme.TextDim
import com.actel.dashboard.ui.theme.TextPrimary
import com.actel.dashboard.ui.theme.TextSecondary
import com.actel.dashboard.ui.theme.TyreCold
import com.actel.dashboard.ui.theme.TyreHot
import com.actel.dashboard.ui.theme.TyreOptimal
import com.actel.dashboard.ui.theme.TyreWarm

@Composable
fun TyresScreen(
    telemetry: TelemetryData?,
    modifier: Modifier = Modifier
) {
    val physics = telemetry?.physics
    val graphic = telemetry?.graphic
    val tyres = physics?.tyres

    val pressures = tyres?.pressure ?: listOf(0f, 0f, 0f, 0f)
    val tempsI = tyres?.tempI ?: listOf(0f, 0f, 0f, 0f)
    val tempsM = tyres?.tempM ?: listOf(0f, 0f, 0f, 0f)
    val tempsO = tyres?.tempO ?: listOf(0f, 0f, 0f, 0f)
    val wears = tyres?.wear ?: listOf(100f, 100f, 100f, 100f)
    val brakeTemps = tyres?.brakeTemp ?: listOf(0f, 0f, 0f, 0f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Top Info Bar (Temps, Compound, Fuel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(BgCard)
                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoStripItem("HAMUR", graphic?.tyreCompound ?: "Slick")
            InfoStripItem("PİST SIC.", "${physics?.roadTemp ?: 0}°C")
            InfoStripItem("HAVA SIC.", "${physics?.airTemp ?: 0}°C")
            InfoStripItem("PİST TUTUŞU", "${graphic?.surfaceGrip ?: 100f}%")
            InfoStripItem("YAKIT", "${String.format("%.1f", physics?.fuel ?: 0f)} L")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4 Tyres Matrix: Front (Left / Right) and Rear (Left / Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Tyres: Front-Left (0) & Rear-Left (2)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TyreCard(
                    title = "ÖN SOL (FL)",
                    pressure = pressures.getOrElse(0) { 0f },
                    tempI = tempsI.getOrElse(0) { 0f },
                    tempM = tempsM.getOrElse(0) { 0f },
                    tempO = tempsO.getOrElse(0) { 0f },
                    wear = wears.getOrElse(0) { 100f },
                    brakeTemp = brakeTemps.getOrElse(0) { 0f },
                    modifier = Modifier.weight(1f)
                )
                TyreCard(
                    title = "ARKA SOL (RL)",
                    pressure = pressures.getOrElse(2) { 0f },
                    tempI = tempsI.getOrElse(2) { 0f },
                    tempM = tempsM.getOrElse(2) { 0f },
                    tempO = tempsO.getOrElse(2) { 0f },
                    wear = wears.getOrElse(2) { 100f },
                    brakeTemp = brakeTemps.getOrElse(2) { 0f },
                    modifier = Modifier.weight(1f)
                )
            }

            // Right Tyres: Front-Right (1) & Rear-Right (3)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TyreCard(
                    title = "ÖN SAĞ (FR)",
                    pressure = pressures.getOrElse(1) { 0f },
                    tempI = tempsI.getOrElse(1) { 0f },
                    tempM = tempsM.getOrElse(1) { 0f },
                    tempO = tempsO.getOrElse(1) { 0f },
                    wear = wears.getOrElse(1) { 100f },
                    brakeTemp = brakeTemps.getOrElse(1) { 0f },
                    modifier = Modifier.weight(1f)
                )
                TyreCard(
                    title = "ARKA SAĞ (RR)",
                    pressure = pressures.getOrElse(3) { 0f },
                    tempI = tempsI.getOrElse(3) { 0f },
                    tempM = tempsM.getOrElse(3) { 0f },
                    tempO = tempsO.getOrElse(3) { 0f },
                    wear = wears.getOrElse(3) { 100f },
                    brakeTemp = brakeTemps.getOrElse(3) { 0f },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TyreCard(
    title: String,
    pressure: Float,
    tempI: Float,
    tempM: Float,
    tempO: Float,
    wear: Float,
    brakeTemp: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BgCard)
            .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${String.format("%.1f", pressure)} PSI",
                color = NeonCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "AŞINMA: ${String.format("%.1f", wear)}%",
                color = if (wear > 75) NeonGreen else if (wear > 50) NeonYellow else NeonRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }

        // IMO Temp Zones
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1.2f)
        ) {
            Text(
                text = "İÇ / ORTA / DIŞ",
                color = TextDim,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TempPill(tempI.toInt())
                TempPill(tempM.toInt())
                TempPill(tempO.toInt())
            }
        }

        // Brake Temp
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(0.9f)
        ) {
            Text(
                text = "FREN DİSKİ",
                color = TextDim,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            val isOverheat = brakeTemp > 650
            Text(
                text = "${brakeTemp.toInt()}°C",
                color = if (isOverheat) NeonRed else if (brakeTemp > 450) TyreWarm else TyreOptimal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun TempPill(temp: Int) {
    val color = when {
        temp < 70 -> TyreCold
        temp in 70..95 -> TyreOptimal
        temp in 96..110 -> TyreWarm
        else -> TyreHot
    }

    Box(
        modifier = Modifier
            .width(36.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$temp°",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun InfoStripItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextDim, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
