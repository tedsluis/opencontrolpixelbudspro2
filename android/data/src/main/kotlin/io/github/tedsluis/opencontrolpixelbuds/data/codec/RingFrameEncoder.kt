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

import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream.ACK_CODE
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream.ACK_GROUP
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream.CODE_RING
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream.GROUP
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream.VALUE_STOP

/**
 * Builds the exact byte layout PROTOCOL.md §4.4 documents for DLCI 0x04's
 * Action Group (0x04) — modeled on `CAP-025` frames 2040/2120 (Right),
 * 2131/2180 (Left), see `RingFrameEncoderTest`.
 */
object RingFrameEncoder {

    fun encode(frame: RingFrame): ByteArray = when (frame) {
        is RingFrame.Start -> header(GROUP, CODE_RING, 1) + byteArrayOf(frame.target.wireValue.toByte())
        is RingFrame.Stop -> header(GROUP, CODE_RING, 1) + byteArrayOf(VALUE_STOP.toByte())
        is RingFrame.Ack -> {
            val data = byteArrayOf(frame.echoedGroup.toByte(), frame.echoedCode.toByte()) + frame.data
            header(ACK_GROUP, ACK_CODE, data.size) + data
        }
    }

    private fun header(group: Int, code: Int, dataLength: Int): ByteArray = byteArrayOf(
        group.toByte(),
        code.toByte(),
        ((dataLength shr 8) and 0xFF).toByte(),
        (dataLength and 0xFF).toByte(),
    )
}
