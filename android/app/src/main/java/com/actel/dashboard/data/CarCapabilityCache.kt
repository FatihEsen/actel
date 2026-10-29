package com.actel.dashboard.data

object CarCapabilityCache {
    private val turboCars = mutableSetOf<String>()
    private val kersCars = mutableSetOf<String>()
    private val drsCars = mutableSetOf<String>()
    private val maxTurboMap = mutableMapOf<String, Float>()

    fun record(
        car: String,
        staticHasTurbo: Boolean,
        staticHasErs: Boolean,
        staticHasKers: Boolean,
        staticHasDrs: Boolean,
        turboBoost: Float,
        kersCharge: Float,
        drsAvailable: Boolean,
        drsActive: Boolean,
        staticMaxTurbo: Float
    ) {
        if (car.isBlank() || car == "---") return
        val lower = car.lowercase()

        // Known motorsport models
        val isKnownTurbo = lower.contains("turbo") || lower.contains("919") || lower.contains("r18") || lower.contains("488") || lower.contains("gtr") || lower.contains("gt-r") || lower.contains("nismo") || lower.contains("gt2")
        val isKnownKers = lower.contains("919") || lower.contains("hybrid") || lower.contains("r18")

        if (staticHasTurbo || isKnownTurbo || turboBoost > 0.02f) {
            turboCars.add(car)
            turboCars.add(lower)
        }
        if (staticHasErs || staticHasKers || isKnownKers || kersCharge > 0.5f) {
            kersCars.add(car)
            kersCars.add(lower)
        }
        if (staticHasDrs || drsAvailable || drsActive) {
            drsCars.add(car)
            drsCars.add(lower)
        }
        if (staticMaxTurbo > 0.2f) {
            maxTurboMap[car] = staticMaxTurbo
        }
        if (turboBoost > (maxTurboMap[car] ?: 1.5f)) {
            maxTurboMap[car] = turboBoost
        }
    }

    fun hasTurbo(car: String): Boolean {
        if (car.isBlank() || car == "---") return false
        val lower = car.lowercase()
        return turboCars.contains(car) || turboCars.contains(lower) || lower.contains("turbo") || lower.contains("919") || lower.contains("r18") || lower.contains("488") || lower.contains("gtr") || lower.contains("gt-r") || lower.contains("nismo")
    }

    fun hasKers(car: String): Boolean {
        if (car.isBlank() || car == "---") return false
        val lower = car.lowercase()
        return kersCars.contains(car) || kersCars.contains(lower) || lower.contains("919") || lower.contains("hybrid") || lower.contains("r18")
    }

    fun hasDrs(car: String): Boolean {
        if (car.isBlank() || car == "---") return false
        val lower = car.lowercase()
        return drsCars.contains(car) || drsCars.contains(lower)
    }

    fun getMaxTurbo(car: String): Float {
        return maxTurboMap[car] ?: 2.5f
    }
}
