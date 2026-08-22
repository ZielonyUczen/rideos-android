package com.rideos.android.data.vesc

import com.rideos.android.domain.telemetry.TelemetrySnapshot
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

interface VescTelemetryListener {
    fun onTelemetry(snapshot: TelemetrySnapshot)
    fun onTelemetryTimeout()
    fun onTelemetryError(message: String)
}

/**
 * Polls COMM_GET_VALUES and converts fragmented BLE responses into telemetry.
 * Transport lifecycle remains owned by the connection layer.
 */
class VescTelemetryEngine(
    private val transport: VescBleTransport,
    private val listener: VescTelemetryListener,
    private val pollIntervalMs: Long = 200L,
    private val timeoutMs: Long = 2_000L
) {
    private val accumulator = VescFrameAccumulator()
    private val running = AtomicBoolean(false)
    private var timer: Timer? = null
    @Volatile private var lastTelemetryAtMs: Long = 0L

    fun start() {
        if (!running.compareAndSet(false, true)) return
        lastTelemetryAtMs = System.currentTimeMillis()
        timer = Timer("vesc-telemetry", true).apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() = poll()
            }, 0L, pollIntervalMs)
        }
    }

    fun stop() {
        running.set(false)
        timer?.cancel()
        timer = null
        accumulator.clear()
    }

    fun onBleData(chunk: ByteArray) {
        for (payload in accumulator.append(chunk)) {
            val snapshot = VescValuesParser.parse(payload) ?: continue
            lastTelemetryAtMs = System.currentTimeMillis()
            listener.onTelemetry(snapshot)
        }
    }

    private fun poll() {
        if (!running.get()) return

        val now = System.currentTimeMillis()
        if (now - lastTelemetryAtMs > timeoutMs) {
            lastTelemetryAtMs = now
            listener.onTelemetryTimeout()
        }

        val request = VescPacketCodec.encode(byteArrayOf(VescCommand.GET_VALUES.id.toByte()))
        if (!transport.write(request)) {
            listener.onTelemetryError("Unable to write COMM_GET_VALUES")
        }
    }
}
