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

import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains

/**
 * Builds the `RpcPacket` bytes ([PwRpc.encode]) of an EQ `WriteSetting` request — the bytes that still need
 * [Hdlc.encode]'s address/control/flag/CRC wrapping before being written to the wire. Byte-for-byte equal to the
 * official app's `CAP-015` frames 2111/2165/2227 when built for channel 19 (see `EqCodecTest`).
 */
object EqFrameEncoder {

    fun encode(frame: EqFrame): ByteArray = PwRpc.encode(
        RpcPacket(
            type = PwRpc.TYPE_REQUEST,
            channelId = frame.channelId,
            serviceId = Maestro.SERVICE_ID,
            methodId = Maestro.METHOD_WRITE_SETTING,
            payload = settingsPayload(frame),
        ),
    )

    /** Field 4 { field 16|18 { 5 x float32 } } — also the shape of a `ReadSetting` response (PROTOCOL.md §4.2). */
    internal fun settingsPayload(frame: EqFrame): ByteArray {
        val bandBytes = encodeBands(frame.gains)
        val outerField = if (frame.persist) Maestro.FIELD_EQ_SAVED else Maestro.FIELD_EQ_ACTIVE
        val inner = Varint.encode((outerField shl 3) or WIRETYPE_LEN) + byteArrayOf(bandBytes.size.toByte()) + bandBytes
        return byteArrayOf(((FIELD4 shl 3) or WIRETYPE_LEN).toByte(), inner.size.toByte()) + inner
    }

    /** Wire order is [PROTOCOL.md] §4.2's confirmed field-to-band mapping — the *reverse* of
     * [EqBandGains]'s on-screen top-to-bottom order. */
    private fun encodeBands(gains: EqBandGains): ByteArray {
        val wireOrder = floatArrayOf(gains.lowBass, gains.bass, gains.mid, gains.treble, gains.upperTreble)
        val out = ByteArray(wireOrder.size * 5)
        var offset = 0
        for ((index, value) in wireOrder.withIndex()) {
            out[offset] = (((index + 1) shl 3) or WIRETYPE_FIXED32).toByte()
            val bits = java.lang.Float.floatToRawIntBits(value)
            out[offset + 1] = (bits and 0xFF).toByte()
            out[offset + 2] = ((bits shr 8) and 0xFF).toByte()
            out[offset + 3] = ((bits shr 16) and 0xFF).toByte()
            out[offset + 4] = ((bits shr 24) and 0xFF).toByte()
            offset += 5
        }
        return out
    }

    private const val FIELD4 = 4
    private const val WIRETYPE_LEN = 2
    private const val WIRETYPE_FIXED32 = 5
}
