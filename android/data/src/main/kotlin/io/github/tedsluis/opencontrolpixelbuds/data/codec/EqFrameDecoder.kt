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
 * Parses an already-[Hdlc]-decoded DLCI 0x02 payload into an [EqFrame]. Any
 * structural mismatch (short buffer, an unexpected field/wiretype, a length
 * that doesn't match the actual remaining bytes) is [BudsError.MalformedFrame],
 * never thrown — this decoder receives attacker/environment-controlled bytes,
 * not trusted input (AGENTS.md §11). A structurally valid DLCI 0x02 payload
 * whose outer field is neither 16 nor 18 (i.e. some *other* `libmaestro`
 * setting entirely, per ARCHITECTURE.md §5a's gating table) is *not* this
 * decoder's job to interpret — callers should route it to
 * `UnidentifiedFrame` rather than treat this decoder's `MalformedFrame` as
 * meaning "corrupt," since it may simply be a different, still-gated
 * setting.
 */
object EqFrameDecoder {
    private const val PREFIX_LENGTH = 13
    private const val FIELD5_TAG = (5 shl 3) or 2
    private const val FIELD4_TAG = (4 shl 3) or 2
    private const val BAND_COUNT = 5
    private const val BAND_BYTES = BAND_COUNT * 5

    fun decode(payload: ByteArray): BudsResult<EqFrame> {
        if (payload.size < PREFIX_LENGTH + 2) {
            return BudsResult.Failure(BudsError.MalformedFrame(payload))
        }
        val correlationByte = payload[2].toInt() and 0xFF
        var i = PREFIX_LENGTH

        if ((payload[i].toInt() and 0xFF) != FIELD5_TAG) {
            return BudsResult.Failure(BudsError.MalformedFrame(payload))
        }
        i++
        val field5Len = payload.getOrNull(i)?.toInt()?.and(0xFF)
            ?: return BudsResult.Failure(BudsError.MalformedFrame(payload))
        i++
        if (i + field5Len != payload.size) {
            return BudsResult.Failure(BudsError.MalformedFrame(payload))
        }

        if (payload.getOrNull(i)?.toInt()?.and(0xFF) != FIELD4_TAG) {
            return BudsResult.Failure(BudsError.MalformedFrame(payload))
        }
        i++
        val field4Len = payload.getOrNull(i)?.toInt()?.and(0xFF)
            ?: return BudsResult.Failure(BudsError.MalformedFrame(payload))
        i++
        val field4End = i + field4Len
        if (field4End > payload.size) {
            return BudsResult.Failure(BudsError.MalformedFrame(payload))
        }

        val (outerTag, afterOuterTag) = Varint.decode(payload, i)
            ?: return BudsResult.Failure(BudsError.MalformedFrame(payload))
        i = afterOuterTag
        val outerField = outerTag shr 3
        if ((outerTag and 0x7) != 2 || (outerField != 16 && outerField != 18)) {
            return BudsResult.Failure(BudsError.MalformedFrame(payload))
        }

        val innerLen = payload.getOrNull(i)?.toInt()?.and(0xFF)
            ?: return BudsResult.Failure(BudsError.MalformedFrame(payload))
        i++
        if (innerLen != BAND_BYTES || i + innerLen != field4End) {
            return BudsResult.Failure(BudsError.MalformedFrame(payload))
        }

        val bandValues = FloatArray(BAND_COUNT)
        for (band in 0 until BAND_COUNT) {
            val tag = payload.getOrNull(i)?.toInt()?.and(0xFF)
                ?: return BudsResult.Failure(BudsError.MalformedFrame(payload))
            val fieldNum = tag shr 3
            if ((tag and 0x7) != 5 || fieldNum != band + 1) {
                return BudsResult.Failure(BudsError.MalformedFrame(payload))
            }
            i++
            if (i + 4 > payload.size) {
                return BudsResult.Failure(BudsError.MalformedFrame(payload))
            }
            val bits = (payload[i].toInt() and 0xFF) or
                ((payload[i + 1].toInt() and 0xFF) shl 8) or
                ((payload[i + 2].toInt() and 0xFF) shl 16) or
                ((payload[i + 3].toInt() and 0xFF) shl 24)
            bandValues[band] = Float.fromBits(bits)
            i += 4
        }

        val gains = EqBandGains(
            upperTreble = bandValues[4],
            treble = bandValues[3],
            mid = bandValues[2],
            bass = bandValues[1],
            lowBass = bandValues[0],
        )
        return BudsResult.Success(EqFrame(gains, persist = outerField == 18, correlationByte = correlationByte))
    }
}
