package com.rideos.android.data.vesc

/** Accumulates fragmented BLE notifications and emits complete VESC short frames. */
class VescFrameAccumulator {
    private var buffer = ByteArray(0)

    fun append(chunk: ByteArray): List<ByteArray> {
        if (chunk.isEmpty()) return emptyList()
        buffer += chunk

        val frames = mutableListOf<ByteArray>()
        var consumed = 0
        var index = 0

        while (index + 5 <= buffer.size) {
            if ((buffer[index].toInt() and 0xFF) != 0x02) {
                index++
                consumed = index
                continue
            }

            val payloadLength = buffer[index + 1].toInt() and 0xFF
            val frameLength = payloadLength + 5
            if (index + frameLength > buffer.size) break

            val candidate = buffer.copyOfRange(index, index + frameLength)
            val decoded = VescPacketCodec.decodeFrames(candidate)
            if (decoded.isNotEmpty()) {
                frames += decoded
                index += frameLength
                consumed = index
            } else {
                index++
                consumed = index
            }
        }

        if (consumed > 0) buffer = buffer.copyOfRange(consumed, buffer.size)
        return frames
    }

    fun clear() {
        buffer = ByteArray(0)
    }
}
