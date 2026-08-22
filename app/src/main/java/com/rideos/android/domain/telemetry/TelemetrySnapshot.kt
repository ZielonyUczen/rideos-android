package com.rideos.android.domain.telemetry

/** Immutable telemetry model. The UI depends on this abstraction, not on VESC. */
data class TelemetrySnapshot(
    val speedKmh: Double = 0.0,
    val erpm: Int = 0,
    val batteryVoltage: Double = 0.0,
    val batteryCurrent: Double = 0.0,
    val motorCurrent: Double = 0.0,
    val inputPowerWatts: Double = 0.0,
    val dutyCycle: Double = 0.0,
    val motorTemperatureC: Double = 0.0,
    val batteryPercent: Double = 0.0,
    val gear: Int = 1,
    val gearCount: Int = 10,
    val connected: Boolean = false
)

interface TelemetrySource {
    fun snapshot(): TelemetrySnapshot
}

class MockTelemetrySource : TelemetrySource {
    override fun snapshot(): TelemetrySnapshot = TelemetrySnapshot(
        speedKmh = 42.7,
        erpm = 6840,
        batteryVoltage = 57.8,
        batteryCurrent = 28.4,
        motorCurrent = 76.0,
        inputPowerWatts = 1641.0,
        dutyCycle = 0.71,
        motorTemperatureC = 54.0,
        batteryPercent = 78.0,
        gear = 6,
        connected = true
    )
}
