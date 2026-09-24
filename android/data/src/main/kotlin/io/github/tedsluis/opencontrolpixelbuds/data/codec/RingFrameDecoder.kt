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

import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream.CODE_RING
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream.GROUP
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream.VALUE_STOP
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget

/**
 * Parses a single already-delimited DLCI 0x04 Message Stream frame
 * ([Group][Code][Length:2BE][Data], PROTOCOL.md §2.1) into a [RingFrame].
 * Structural failures are always [BudsError.MalformedFrame], never thrown
 * (AGENTS.md §11). Fixtures: `CAP-025` frames 2040/2045/2120/2124 (Right),
 * 2131/2180 (Left), 2044/2048/2123/2127 (Ack) — see `RingFrameDecoderTest`.
 */
object RingFrameDecoder {

    fun decode(bytes: ByteArray): BudsResult<RingFrame> {
        if (bytes.size < 4) {
            return BudsResult.Failure(BudsError.MalformedFrame(bytes))
        }

        val group = bytes[0].toInt() and 0xFF
        val code = bytes[1].toInt() and 0xFF
        val declaredLength = ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
        val data = bytes.copyOfRange(4, bytes.size)

        if (data.size != declaredLength) {
            return BudsResult.Failure(BudsError.MalformedFrame(bytes))
        }

        return when {
            group == GROUP && code == CODE_RING && data.size == 1 -> {
                val value = data[0].toInt() and 0xFF
                when {
                    value == VALUE_STOP -> BudsResult.Success(RingFrame.Stop)
                    RingTarget.fromWireValue(value) != null ->
                        BudsResult.Success(RingFrame.Start(RingTarget.fromWireValue(value)!!))
                    else -> BudsResult.Failure(BudsError.MalformedFrame(bytes))
                }
            }


            else -> BudsResult.Failure(BudsError.MalformedFrame(bytes))
        }
    }
}
