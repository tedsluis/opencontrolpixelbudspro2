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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains

/**
 * Reads an EQ quintet out of a Maestro settings payload (`4:{16|18:{5 x float32}}`, PROTOCOL.md §4.2) — the shape
 * of a `WriteSetting` request, a `ReadSetting` response and a `SubscribeToSettingsChanges` stream message alike.
 * Any structural mismatch (short buffer, an unexpected field/wiretype, a length that doesn't match the remaining
 * bytes, an outer field other than 16/18, a non-finite float) is [BudsError.MalformedFrame], never thrown — this
 * decoder receives attacker/environment-controlled bytes (AGENTS.md §11). A structurally valid payload for a
 * *different* setting is not this decoder's job: callers route it to `UnidentifiedFrame`.
 */
object EqFrameDecoder {
    private const val FIELD4_TAG = (4 shl 3) or 2
    private const val BAND_COUNT = 5
    private const val BAND_BYTES = BAND_COUNT * 5

    fun decode(packet: RpcPacket): BudsResult<EqFrame> = decodePayload(packet.payload, packet.channelId)

    fun decodePayload(payload: ByteArray, channelId: Int): BudsResult<EqFrame> {
        fun bad() = BudsResult.Failure(BudsError.MalformedFrame(payload))
        var i = 0
        if (payload.size < 2 || (payload[i].toInt() and 0xFF) != FIELD4_TAG) return bad()
        i++
        val field4Len = payload[i].toInt() and 0xFF
        i++
        if (field4Len >= 0x80 || i + field4Len != payload.size) return bad()

        val (outerTag, afterOuterTag) = Varint.decode(payload, i) ?: return bad()
        i = afterOuterTag
        val outerField = outerTag shr 3
        if ((outerTag and 0x7) != 2 || outerField !in Maestro.READABLE_FIELDS) return bad()

        val innerLen = payload.getOrNull(i)?.toInt()?.and(0xFF) ?: return bad()
        i++
        if (innerLen != BAND_BYTES || i + innerLen != payload.size) return bad()

        val bandValues = FloatArray(BAND_COUNT)
        for (band in 0 until BAND_COUNT) {
            val tag = payload.getOrNull(i)?.toInt()?.and(0xFF) ?: return bad()
            if ((tag and 0x7) != 5 || (tag shr 3) != band + 1) return bad()
            i++
            if (i + 4 > payload.size) return bad()
            val bits = (payload[i].toInt() and 0xFF) or
                ((payload[i + 1].toInt() and 0xFF) shl 8) or
                ((payload[i + 2].toInt() and 0xFF) shl 16) or
                ((payload[i + 3].toInt() and 0xFF) shl 24)
            val value = Float.fromBits(bits)
            if (!value.isFinite()) return bad()
            bandValues[band] = value
            i += 4
        }

        val gains = EqBandGains(
            upperTreble = bandValues[4],
            treble = bandValues[3],
            mid = bandValues[2],
            bass = bandValues[1],
            lowBass = bandValues[0],
        )
        return BudsResult.Success(EqFrame(gains, persist = outerField == Maestro.FIELD_EQ_SAVED, channelId = channelId))
    }
}
