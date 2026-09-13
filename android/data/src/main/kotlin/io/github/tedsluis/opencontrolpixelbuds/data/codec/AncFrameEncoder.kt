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

import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncMessageStream.ACK_CODE
import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncMessageStream.ACK_GROUP
import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncMessageStream.CODE_GET
import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncMessageStream.CODE_NOTIFY
import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncMessageStream.CODE_SET
import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncMessageStream.GROUP

/**
 * Builds the exact byte layout PROTOCOL.md §4.1 documents for DLCI 0x04's
 * Hearable Controls Group (0x08) — modeled on `CAP-001` frames
 * 2039/2132/2159/2193 (Set) and `CAP-036` frame 1169 (Get), see
 * `AncFrameEncoderTest` for the byte-for-byte fixtures this is checked
 * against. This is the observed wire *behavior* reconstructed from capture
 * evidence, not code copied from the official app or `pbpctrl`
 * (AGENTS.md §12).
 */
object AncFrameEncoder {

    fun encode(frame: AncFrame): ByteArray = when (frame) {
        is AncFrame.Get -> byteArrayOf(GROUP.toByte(), CODE_GET.toByte(), 0x00, 0x00)

        is AncFrame.Set -> {
            val data = ByteArray(4 + frame.reserved.size)
            data[0] = frame.seekerVersion.toByte()
            data[1] = frame.settableModesMask.toByte()
            data[2] = frame.enabledModesMask.toByte()
            data[3] = frame.mode.wireBit.toByte()
            frame.reserved.copyInto(data, destinationOffset = 4)
            header(GROUP, CODE_SET, data.size) + data
        }

        is AncFrame.Notify -> {
            val data = byteArrayOf(
                frame.version.toByte(),
                frame.uiToggles.toByte(),
                frame.settableToggles.toByte(),
                frame.currentModeBit.toByte(),
            )
            header(GROUP, CODE_NOTIFY, data.size) + data
        }

        is AncFrame.Ack -> {
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
