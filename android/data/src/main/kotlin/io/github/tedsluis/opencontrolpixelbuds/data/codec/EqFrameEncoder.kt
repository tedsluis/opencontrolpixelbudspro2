/*
 * OpenControl for Pixel Buds Pro 2
 * Copyright (C) 2026 Ted Sluis
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.tedsluis.opencontrolpixelbuds.data.codec

/**
 * Builds the DLCI 0x02 payload bytes [EqFrame] documents — the bytes that
 * still need [Hdlc.encode]'s address/control/flag/CRC wrapping before being
 * written to the wire. Modeled byte-for-byte on `CAP-015` frames
 * 2111/2165/2227; see `EqFrameEncoderTest`.
 */
object EqFrameEncoder {

    private val PREFIX_TAIL = byteArrayOf(
        0x1d, 0xea.toByte(), 0x71, 0xde.toByte(), 0x7e, 0x25, 0x1d, 0x9a.toByte(), 0x8c.toByte(), 0x9e.toByte(),
    )
    private const val FIELD5 = 5
    private const val FIELD4 = 4
    private const val WIRETYPE_LEN = 2
    private const val WIRETYPE_FIXED32 = 5

    fun encode(frame: EqFrame): ByteArray {
        val bandBytes = encodeBands(frame.gains)

        val outerField = if (frame.persist) 18 else 16
        val outerTag = Varint.encode((outerField shl 3) or WIRETYPE_LEN)
        val innerMessage = outerTag + byteArrayOf(bandBytes.size.toByte()) + bandBytes

        val field4Wrapped = byteArrayOf(
            ((FIELD4 shl 3) or WIRETYPE_LEN).toByte(),
            innerMessage.size.toByte(),
        ) + innerMessage

        val field5Wrapped = byteArrayOf(
            ((FIELD5 shl 3) or WIRETYPE_LEN).toByte(),
            field4Wrapped.size.toByte(),
        ) + field4Wrapped

        return byteArrayOf(0x03, 0x10, frame.correlationByte.toByte()) + PREFIX_TAIL + field5Wrapped
    }

    /** Wire order is [PROTOCOL.md] §4.2's confirmed field-to-band mapping — the
     * *reverse* of [EqBandGains]'s on-screen top-to-bottom order. */
    private fun encodeBands(gains: io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains): ByteArray {
        val wireOrder = floatArrayOf(gains.lowBass, gains.bass, gains.mid, gains.treble, gains.upperTreble)
        val out = ByteArray(wireOrder.size * 5)
        var offset = 0
        for ((index, value) in wireOrder.withIndex()) {
            val fieldNum = index + 1
            out[offset] = ((fieldNum shl 3) or WIRETYPE_FIXED32).toByte()
            val bits = java.lang.Float.floatToRawIntBits(value)
            out[offset + 1] = (bits and 0xFF).toByte()
            out[offset + 2] = ((bits shr 8) and 0xFF).toByte()
            out[offset + 3] = ((bits shr 16) and 0xFF).toByte()
            out[offset + 4] = ((bits shr 24) and 0xFF).toByte()
            offset += 5
        }
        return out
    }
}
