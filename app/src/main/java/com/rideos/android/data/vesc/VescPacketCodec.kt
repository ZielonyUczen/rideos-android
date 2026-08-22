package com.rideos.android.data.vesc

/**
 * VESC serial packet framing. The short frame is:
 * 0x02 | payload length | payload | CRC16 | 0x03
 *
 * Payload length is one byte for the short frame used by our telemetry requests.
 */
object VescPacketCodec {
    fun encode(payload: ByteArray): ByteArray {
        require(payload.size <= 255) { "Payload is too large for a short VESC frame" }

        val frame = ByteArray(payload.size + 5)
        frame[0] = 0x02
        frame[1] = payload.size.toByte()
        payload.copyInto(frame, destinationOffset = 2)

        val crc = crc16(payload)
        val crcIndex = payload.size + 2
        frame[crcIndex] = (crc ushr 8).toByte()
        frame[crcIndex + 1] = crc.toByte()
        frame[crcIndex + 2] = 0x03
        return frame
    }

    /** Extracts one or more complete short frames from an accumulated byte buffer. */
    fun decodeFrames(input: ByteArray): List<ByteArray> {
        val frames = mutableListOf<ByteArray>()
        var index = 0

        while (index + 5 <= input.size) {
            if ((input[index].toInt() and 0xFF) != 0x02) {
                index++
                continue
            }

            val length = input[index + 1].toInt() and 0xFF
            val totalLength = length + 5
            if (index + totalLength > input.size) break

            val payloadStart = index + 2
            val payloadEnd = payloadStart + length
            val payload = input.copyOfRange(payloadStart, payloadEnd)
            val receivedCrc = ((input[payloadEnd].toInt() and 0xFF) shl 8) or
                    (input[payloadEnd + 1].toInt() and 0xFF)

            if (crc16(payload) == receivedCrc &&
                (input[payloadEnd + 2].toInt() and 0xFF) == 0x03
            ) {
                frames += payload
                index += totalLength
            } else {
                // Bad CRC or terminator: advance one byte and resynchronise.
                index++
            }
        }
        return frames
    }

    private fun crc16(data: ByteArray): Int {
        var crc = 0
        for (byte in data) {
            crc = crc xor ((byte.toInt() and 0xFF) shl 8)
            repeat(8) {
                crc = if ((crc and 0x8000) != 0) {
                    (crc shl 1) xor 0x1021
                } else {
                    crc shl 1
                }
                crc = crc and 0xFFFF
            }
        }
        return crc
    }
}
