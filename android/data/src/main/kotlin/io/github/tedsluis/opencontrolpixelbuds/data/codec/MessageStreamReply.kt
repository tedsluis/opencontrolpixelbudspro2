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

/**
 * The Fast Pair Message Stream's Acknowledgement group (`0xFF`), one type for every command it answers (0044 audit
 * finding APP-3, `TODO.md`'s "give ACKs their own routed type"). Spec (`developers.google.com/nearby/fast-pair/specifications/
 * extensions/acknowledgement`, fetched 2026-09-24): ACK = `FF 01 <len:2> <echoed group> <echoed code> [state…]`; NAK =
 * `FF 02 <len:2> <reason> <echoed group> <echoed code> [state…]` with reasons `0x00` not supported, `0x01` device busy,
 * `0x02` not allowed in the current state, `0x03` incorrect message authentication code, `0x04` redundant device action.
 * Real ACKs: `CAP-001` frame 2041 `ff 01 00 06 08 12 01 e8 e8 40` (ANC Set), `CAP-025` frames 2044/2048 (Ring, with and without a
 * trailing state byte). No NAK has been captured yet.
 */
object MessageStreamAck {
    const val GROUP: Int = 0xFF
    const val CODE_ACK: Int = 0x01
    const val CODE_NAK: Int = 0x02

    fun reasonName(reason: Int): String = when (reason) {
        0x00 -> "not supported"
        0x01 -> "device busy"
        0x02 -> "not allowed in the current state"
        0x03 -> "incorrect message authentication code"
        0x04 -> "redundant device action"
        else -> "reason 0x%02x".format(reason)
    }
}

/** An ACK or NAK for the message identified by [echoedGroup]/[echoedCode]. */
sealed class MessageStreamReply {
    abstract val echoedGroup: Int
    abstract val echoedCode: Int

    fun answers(group: Int, code: Int): Boolean = echoedGroup == group && echoedCode == code

    data class Ack(override val echoedGroup: Int, override val echoedCode: Int, val state: ByteArray) : MessageStreamReply() {
        override fun equals(other: Any?): Boolean =
            other is Ack && echoedGroup == other.echoedGroup && echoedCode == other.echoedCode && state.contentEquals(other.state)

        override fun hashCode(): Int = (echoedGroup * 31 + echoedCode) * 31 + state.contentHashCode()
    }

    data class Nak(val reason: Int, override val echoedGroup: Int, override val echoedCode: Int, val state: ByteArray) :
        MessageStreamReply() {
        override fun equals(other: Any?): Boolean =
            other is Nak && reason == other.reason && echoedGroup == other.echoedGroup && echoedCode == other.echoedCode &&
                state.contentEquals(other.state)

        override fun hashCode(): Int = ((reason * 31 + echoedGroup) * 31 + echoedCode) * 31 + state.contentHashCode()
    }
}

/** Decodes one already-delimited DLCI 0x04 frame as an ACK/NAK; anything else is [BudsError.MalformedFrame], never thrown. */
object MessageStreamReplyDecoder {
    fun decode(bytes: ByteArray): BudsResult<MessageStreamReply> {
        fun bad() = BudsResult.Failure(BudsError.MalformedFrame(bytes))
        if (bytes.size < 4) return bad()
        val group = bytes[0].toInt() and 0xFF
        val code = bytes[1].toInt() and 0xFF
        val length = ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
        if (group != MessageStreamAck.GROUP || bytes.size - 4 != length) return bad()
        val data = bytes.copyOfRange(4, bytes.size)
        return when {
            code == MessageStreamAck.CODE_ACK && data.size >= 2 -> BudsResult.Success(
                MessageStreamReply.Ack(data[0].toInt() and 0xFF, data[1].toInt() and 0xFF, data.copyOfRange(2, data.size)),
            )
            code == MessageStreamAck.CODE_NAK && data.size >= 3 -> BudsResult.Success(
                MessageStreamReply.Nak(
                    reason = data[0].toInt() and 0xFF,
                    echoedGroup = data[1].toInt() and 0xFF,
                    echoedCode = data[2].toInt() and 0xFF,
                    state = data.copyOfRange(3, data.size),
                ),
            )
            else -> bad()
        }
    }
}
