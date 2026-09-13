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
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult

/**
 * Parses a single already-delimited DLCI 0x04 Message Stream frame
 * ([Group][Code][Length:2BE][Data], PROTOCOL.md §2.1) into an [AncFrame].
 * Any structural failure (short buffer, a declared length that doesn't match
 * the actual remaining bytes, an unrecognized Code for a Group this decoder
 * handles) is reported as [BudsError.MalformedFrame], never thrown — this
 * decoder receives attacker/environment-controlled bytes (a nearby device or
 * a corrupted transmission), not trusted input (AGENTS.md §11).
 *
 * Fixtures this is verified against (see `AncFrameDecoderTest`): `CAP-001`
 * frames 2039/2041/2132/2134/2159/2162/2193/2195 and `CAP-006` frames
 * 1393/1398/1627/1630/1731/1735/1862/1864 (Set/Ack pairs), `CAP-036` frames
 * 1169/1182 (Get/Notify).
 */
object AncFrameDecoder {

    fun decode(bytes: ByteArray): BudsResult<AncFrame> {
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
            group == GROUP && code == CODE_GET && data.isEmpty() ->
                BudsResult.Success(AncFrame.Get)

            group == GROUP && code == CODE_SET && data.size >= 4 ->
                BudsResult.Success(
                    AncFrame.Set(
                        mode = AncMode.fromWireBit(data[3].toInt() and 0xFF)
                            ?: return BudsResult.Failure(BudsError.MalformedFrame(bytes)),
                        seekerVersion = data[0].toInt() and 0xFF,
                        settableModesMask = data[1].toInt() and 0xFF,
                        enabledModesMask = data[2].toInt() and 0xFF,
                        reserved = data.copyOfRange(4, data.size),
                    ),
                )

            group == GROUP && code == CODE_NOTIFY && data.size == 4 ->
                BudsResult.Success(
                    AncFrame.Notify(
                        version = data[0].toInt() and 0xFF,
                        uiToggles = data[1].toInt() and 0xFF,
                        settableToggles = data[2].toInt() and 0xFF,
                        currentModeBit = data[3].toInt() and 0xFF,
                    ),
                )

            group == ACK_GROUP && code == ACK_CODE && data.size >= 2 ->
                BudsResult.Success(
                    AncFrame.Ack(
                        echoedGroup = data[0].toInt() and 0xFF,
                        echoedCode = data[1].toInt() and 0xFF,
                        data = data.copyOfRange(2, data.size),
                    ),
                )

            else -> BudsResult.Failure(BudsError.MalformedFrame(bytes))
        }
    }
}
