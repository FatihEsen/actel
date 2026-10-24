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
import com.actel.dashboard.ui.components.CompactTyresCard
import com.actel.dashboard.ui.components.GForceMeter
import com.actel.dashboard.ui.components.GearSpeedDisplay
import com.actel.dashboard.ui.components.PedalBars
import com.actel.dashboard.ui.components.ShiftLights
import com.actel.dashboard.ui.components.TimingDeltaCard
import com.actel.dashboard.ui.theme.BgCard
import com.actel.dashboard.ui.theme.BorderDark
import com.actel.dashboard.ui.theme.NeonCyan
import com.actel.dashboard.ui.theme.NeonGreen
import com.actel.dashboard.ui.theme.NeonRed
import com.actel.dashboard.ui.theme.NeonYellow
import com.actel.dashboard.ui.theme.TextDim
import com.actel.dashboard.ui.theme.TextPrimary
import com.actel.dashboard.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    telemetry: TelemetryData?,
    modifier: Modifier = Modifier
) {
    val physics = telemetry?.physics
    val graphic = telemetry?.graphic
    val staticInfo = telemetry?.staticInfo

    val currentRpm = physics?.rpms ?: 0
    val maxRpm = if (staticInfo != null && staticInfo.limiterRpm > 0) staticInfo.limiterRpm else (staticInfo?.maxRpm ?: 8500)
    val speedKmh = physics?.speedKmh ?: 0
    val gear = physics?.gear ?: 1
    val gearStr = physics?.gearStr ?: "N"

    val displayCarName = when {
        !staticInfo?.carName.isNullOrEmpty() -> staticInfo!!.carName
        !staticInfo?.carModel.isNullOrEmpty() -> staticInfo!!.carModel
        else -> "---"
    }

    val displayTrackName = when {
        !staticInfo?.trackName.isNullOrEmpty() -> staticInfo!!.trackName
        !staticInfo?.track.isNullOrEmpty() -> staticInfo!!.track
        else -> "---"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Top Shift Light Bar
        ShiftLights(
            currentRpm = currentRpm,
            maxRpm = maxRpm,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main Dashboard Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==================== SOL SÜTUN: ARAÇ/PİST + ZAMANLAMA + AYARLAR ====================
            Column(
                modifier = Modifier
                    .weight(1.05f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Araç & Pist Bilgi Kartı (Timer Üstünde)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgCard)
                        .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ARAÇ: ",
                                color = TextDim,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = displayCarName,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PİST: ",
                                color = TextDim,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = displayTrackName,
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 2. Timing & Delta Kartı
                TimingDeltaCard(
                    currentTime = graphic?.currentTime ?: "0:00.000",
                    lastTime = graphic?.lastTime ?: "--:--.---",
                    bestTime = graphic?.bestTime ?: "--:--.---",
                    split = graphic?.split ?: "+0.000",
                    lap = graphic?.completedLaps ?: 0,
                    sector = graphic?.currentSectorIndex ?: 1,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Setup Values (TC, ABS, Bias, DRS)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgCard)
                        .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SetupItem(label = "TC", value = "${(physics?.tc ?: 0f).toInt()}")
                    SetupItem(label = "ABS", value = "${(physics?.abs ?: 0f).toInt()}")
                    SetupItem(
                        label = "BB",
                        value = "${String.format("%.1f", (physics?.brakeBias ?: 0.58f) * 100)}%"
                    )
                    val drsAvail = physics?.drsAvailable == true || physics?.drs == true
                    val drsColor = if (drsAvail) NeonGreen else NeonRed
                    val kersPct = (physics?.kersCharge ?: 100f).toInt()
                    SetupItem(
                        label = "DRS",
                        value = "${kersPct}%",
                        color = drsColor
                    )
                }
            }

            // ==================== ORTA SÜTUN: HIZ, RPM, VİTES ====================
            Column(
                modifier = Modifier.weight(1.25f)
            ) {
                GearSpeedDisplay(
                    gear = gear,
                    gearStr = gearStr,
                    speedKmh = speedKmh,
                    currentRpm = currentRpm,
                    maxRpm = maxRpm,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ==================== SAĞ SÜTUN: PEDALLAR + G-FORCE & SADELEŞTİRİLMİŞ LASTİKLER ====================
            Column(
                modifier = Modifier
                    .weight(1.10f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Üst Kısım (50% yükseklik): Pedallar ve G-Force yan yana
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PedalBars(
                        gas = physics?.gas ?: 0f,
                        brake = physics?.brake ?: 0f,
                        clutch = physics?.clutch ?: 0f,
                        modifier = Modifier.weight(1.1f)
                    )

                    GForceMeter(
                        lateralG = physics?.accG?.lateral ?: 0f,
                        longitudinalG = physics?.accG?.longitudinal ?: 0f,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                // Alt Kısım (50% yükseklik): Sadeleştirilmiş 4 Lastik Kartı
                CompactTyresCard(
                    tyres = physics?.tyres,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SetupItem(
    label: String,
    value: String,
    color: Color = NeonCyan
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TextDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
