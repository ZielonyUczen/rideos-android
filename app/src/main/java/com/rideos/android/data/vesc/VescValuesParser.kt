package com.rideos.android.data.vesc

import com.rideos.android.domain.telemetry.TelemetrySnapshot
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Parses the common COMM_GET_VALUES response fields used by RideOS. */
object VescValuesParser {
    fun parse(payload: ByteArray, wheelCircumferenceM: Double = 2.23): TelemetrySnapshot? {
        if (payload.isEmpty() || (payload[0].toInt() and 0xFF) != VescCommand.GET_VALUES.id) return null

        // Firmware 7.x exposes the standard values in big-endian scaled fields.
        val data = ByteBuffer.wrap(payload, 1, payload.size - 1).order(ByteOrder.BIG_ENDIAN)
        if (data.remaining() < 26) return null

        val tempFet = data.short / 10.0
        val tempMotor = data.short / 10.0
        val motorCurrent = data.int / 100.0
        val batteryCurrent = data.int / 100.0
        data.int // Id current
        data.int // Iq current
        val duty = data.short / 1000.0
        val rpm = data.int.toDouble()
        val voltage = data.short / 10.0

        // VESC reports electrical RPM. Speed conversion is configuration-dependent.
        // This parser therefore leaves speed at 0 until pole-pair / gear configuration
        // is supplied by the application layer.
        return TelemetrySnapshot(
            erpm = rpm.toInt(),
            batteryVoltage = voltage,
            batteryCurrent = batteryCurrent,
            motorCurrent = motorCurrent,
            dutyCycle = duty,
            motorTemperatureC = maxOf(tempMotor, 0.0),
            speedKmh = 0.0,
            connected = true
        )
    }
}
