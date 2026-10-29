package com.actel.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryEnvelope(
    val type: String = "",
    val data: TelemetryData? = null
)

@Serializable
data class IndicatorData(
    val turnLeft: Boolean = false,
    val turnRight: Boolean = false,
    val hazard: Boolean = false,
    val headlights: Boolean = false,
    val flasher: Boolean = false,
    val pitLimiter: Boolean = false
)

@Serializable
data class TelemetryData(
    val connected: Boolean = false,
    val source: String = "",
    val timestamp: Long = 0L,
    val physics: PhysicsData? = null,
    val graphic: GraphicData? = null,
    val staticInfo: StaticInfoData? = null,
    val indicators: IndicatorData? = null,
    val ghostDelta: String = "",
    val delta: Float = 0f,
    val deltaTrend: String = "neutral",
    val isLapCut: Boolean = false,
    val gapAhead: String = "",
    val gapBehind: String = "",
    val totalCars: Int = 0
)

@Serializable
data class PhysicsData(
    val packetId: Long = 0L,
    val gas: Float = 0f,
    val brake: Float = 0f,
    val clutch: Float = 0f,
    val fuel: Float = 0f,
    val gear: Int = 0,
    val gearStr: String = "N",
    val rpms: Int = 0,
    val steerAngle: Float = 0f,
    val speedKmh: Int = 0,
    val accG: AccGData? = null,
    val tyres: TyresData? = null,
    val drs: Boolean = false,
    val drsAvailable: Boolean = false,
    val drsEnabled: Boolean = false,
    val tc: Float = 0f,
    val tcActive: Boolean = false,
    val abs: Float = 0f,
    val absActive: Boolean = false,
    val pitLimiterOn: Boolean = false,
    val performanceMeter: Float = 0f,
    val turboBoost: Float = 0f,
    val airTemp: Int = 0,
    val roadTemp: Int = 0,
    val brakeBias: Float = 0.58f,
    val engineBrake: Int = 0,
    val kersCharge: Float = 0f,
    val finalFF: Float = 0f,
    val surfaceRumble: Float = 0f,
    val collision: Boolean = false,
    val numberOfTyresOut: Int = 0
)

@Serializable
data class AccGData(
    val lateral: Float = 0f,
    val longitudinal: Float = 0f,
    val vertical: Float = 0f
)

@Serializable
data class TyresData(
    val pressure: List<Float> = listOf(0f, 0f, 0f, 0f),
    val tempPractical: List<Float> = listOf(0f, 0f, 0f, 0f),
    val tempCore: List<Float> = listOf(0f, 0f, 0f, 0f),
    val tempI: List<Float> = listOf(0f, 0f, 0f, 0f),
    val tempM: List<Float> = listOf(0f, 0f, 0f, 0f),
    val tempO: List<Float> = listOf(0f, 0f, 0f, 0f),
    val wear: List<Float> = listOf(100f, 100f, 100f, 100f),
    val brakeTemp: List<Float> = listOf(0f, 0f, 0f, 0f)
)

@Serializable
data class GraphicData(
    val packetId: Long = 0L,
    val status: Int = 0,
    val session: Int = 0,
    val currentTime: String = "0:00.000",
    val lastTime: String = "0:00.000",
    val bestTime: String = "0:00.000",
    val split: String = "+0.000",
    val completedLaps: Int = 0,
    val position: Int = 0,
    val iCurrentTime: Long = 0L,
    val iLastTime: Long = 0L,
    val iBestTime: Long = 0L,
    val sessionTimeLeft: Int = 0,
    val distanceTraveled: Float = 0f,
    val isInPit: Boolean = false,
    val isInPitLane: Boolean = false,
    val currentSectorIndex: Int = 0,
    val numberOfLaps: Int = 0,
    val tyreCompound: String = "Dry",
    val surfaceGrip: Float = 100f
)

@Serializable
data class StaticInfoData(
    val carModel: String = "Unknown Car",
    val carName: String = "",
    val brand: String = "",
    val track: String = "Unknown Track",
    val trackName: String = "",
    val trackConfiguration: String = "",
    val playerName: String = "Driver",
    val sectorCount: Int = 3,
    val maxRpm: Int = 8500,
    val limiterRpm: Int = 8500,
    val shiftRpm: Int = 8100,
    val idleRpm: Int = 800,
    val maxFuel: Int = 100,
    val hasDRS: Boolean = false,
    val hasERS: Boolean = false,
    val hasKERS: Boolean = false,
    val hasTurbo: Boolean = false,
    val maxTurboBoost: Float = 2.5f,
    val isOnline: Boolean = false
)
