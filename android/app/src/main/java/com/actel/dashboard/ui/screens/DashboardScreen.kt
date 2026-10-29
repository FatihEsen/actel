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
import com.actel.dashboard.data.CarCapabilityCache
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
    onAction: (String) -> Unit = {},
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

    val turboBoost = physics?.turboBoost ?: 0f
    val kersCharge = physics?.kersCharge ?: 0f
    val drsAvailable = physics?.drsAvailable == true || physics?.drs == true
    val drsActive = physics?.drs == true

    // Araç yeteneklerini kalıcı hafızaya kaydet (Porsche 919 veya herhangi bir turbo araç durduğunda veya sekme değiştiğinde kaybolmaz!)
    CarCapabilityCache.record(
        car = displayCarName,
        staticHasTurbo = staticInfo?.hasTurbo == true,
        staticHasErs = staticInfo?.hasERS == true,
        staticHasKers = staticInfo?.hasKERS == true,
        staticHasDrs = staticInfo?.hasDRS == true,
        turboBoost = turboBoost,
        kersCharge = kersCharge,
        drsAvailable = drsAvailable,
        drsActive = drsActive,
        staticMaxTurbo = staticInfo?.maxTurboBoost ?: 0f
    )

    val hasTurbo = CarCapabilityCache.hasTurbo(displayCarName)
    val hasKers = CarCapabilityCache.hasKers(displayCarName)
    val hasDrs = CarCapabilityCache.hasDrs(displayCarName)
    val maxTurboBoost = CarCapabilityCache.getMaxTurbo(displayCarName)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Top Shift Light Bar with Sidekick-style Turbo & ERS/DRS bars
        ShiftLights(
            currentRpm = currentRpm,
            maxRpm = maxRpm,
            hasTurbo = hasTurbo,
            turboBoost = turboBoost,
            maxTurboBoost = maxTurboBoost,
            hasKers = hasKers,
            kersCharge = kersCharge,
            hasDrs = hasDrs,
            drsAvailable = drsAvailable,
            drsActive = drsActive,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Main Dashboard Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ==================== SOL SÜTUN: ARAÇ/PİST + ZAMANLAMA + GAPS ====================
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

                // 2. Timing, Canlı Süre, Ghost Delta & Lap Telemetri Kartı (Tek Kart)
                TimingDeltaCard(
                    currentTime = graphic?.currentTime ?: "0:00.000",
                    lastTime = graphic?.lastTime ?: "--:--.---",
                    bestTime = graphic?.bestTime ?: "--:--.---",
                    ghostDelta = telemetry?.ghostDelta ?: "",
                    delta = telemetry?.delta ?: (physics?.performanceMeter ?: 0f),
                    deltaTrend = telemetry?.deltaTrend ?: "neutral",
                    isLapCut = telemetry?.isLapCut ?: false,
                    lap = graphic?.completedLaps ?: 0,
                    sector = graphic?.currentSectorIndex ?: 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ==================== ORTA SÜTUN: HIZ, RPM, VİTES VE KANAT GÖSTERGELERİ ====================
            Column(
                modifier = Modifier.weight(1.35f)
            ) {
                GearSpeedDisplay(
                    gear = gear,
                    gearStr = gearStr,
                    speedKmh = speedKmh,
                    currentRpm = currentRpm,
                    maxRpm = maxRpm,
                    indicators = telemetry?.indicators,
                    absActive = physics?.absActive == true,
                    tcActive = physics?.tcActive == true,
                    drsAvailable = drsAvailable,
                    drsActive = drsActive,
                    pitActive = physics?.pitLimiterOn == true || graphic?.isInPitLane == true || telemetry?.indicators?.pitLimiter == true,
                    brakeBias = physics?.brakeBias ?: 0.58f,
                    onAction = onAction,
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
