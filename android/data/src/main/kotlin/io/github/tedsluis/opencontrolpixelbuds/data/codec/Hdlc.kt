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
import java.util.zip.CRC32

/**
 * Generic Pigweed `pw_hdlc` framing shared by every DLCI 0x02 payload
 * (PROTOCOL.md §2.2a, 🟢 FACT for the framing mechanism itself — verified
 * 640/640 sub-frames across CAP-001/CAP-002/CAP-003, and re-verified against
 * this session's own CAP-015 fixtures, see [EqFrameDecoderTest]): flag `0x7E`
 * delimits every frame, `0x7D`-prefixed byte-stuffing escapes any literal
 * `0x7E`/`0x7D` in the body, followed by a pw_hdlc **address** — a variable-length
 * integer, 7 bits per byte, low byte first, where the byte whose bit 0 is set is the *last*
 * one (`00 3b` = 3712, `80 a3` = 10432) — a 1-byte Control field (always `0x03` in the
 * captures), the payload (a pw_rpc `RpcPacket`, see [PwRpc]), and a little-endian CRC-32
 * (IEEE 802.3/zlib polynomial) trailer over the unescaped Address+Control+Payload.
 *
 * `ai-sessions/0041` corrected an earlier reading of the same bytes (address `0x00`, control
 * `0x3b`, payload starting `03`): the wire bytes are identical, but `3b` belongs to the
 * address and `03` is the control byte (DECISIONS.md ADR-034).
 *
 * This is the shared transport layer only — it says nothing about what a
 * DLCI 0x02 payload's *content* means. Per ARCHITECTURE.md §5a, only EQ's
 * payload content is implementation-unblocked on this channel today; every
 * other DLCI 0x02 setting stays gated pending a consolidated unblock ADR.
 */
object Hdlc {
    private const val FLAG: Int = 0x7E
    private const val ESCAPE: Int = 0x7D
    private const val ESCAPE_XOR: Int = 0x20
    private const val CRC_LENGTH: Int = 4

    /** [address] is the decoded pw_hdlc address value (not its raw bytes). */
    data class Frame(val address: Int, val control: Int, val payload: ByteArray) {
        override fun equals(other: Any?): Boolean =
            other is Frame && address == other.address && control == other.control &&
                payload.contentEquals(other.payload)

        override fun hashCode(): Int {
            var result = address
            result = 31 * result + control
            result = 31 * result + payload.contentHashCode()
            return result
        }
    }

    fun encode(address: Int, control: Int, payload: ByteArray): ByteArray {
        val body = encodeAddress(address) + control.toByte() + payload
        val crc = crc32Le(body)
        return byteArrayOf(FLAG.toByte()) + escape(body + crc) + byteArrayOf(FLAG.toByte())
    }

    /**
     * Decodes one already flag-delimited frame (including both flag bytes).
     * Any structural failure — missing flags, a dangling escape byte, a CRC
     * mismatch, a body too short to hold an address+control+CRC — is reported
     * as [BudsError.MalformedFrame], never thrown (AGENTS.md §11 — this
     * decoder receives attacker/environment-controlled bytes).
     */
    fun decode(frame: ByteArray): BudsResult<Frame> {
        if (frame.size < 2 ||
            (frame.first().toInt() and 0xFF) != FLAG ||
            (frame.last().toInt() and 0xFF) != FLAG
        ) {
            return BudsResult.Failure(BudsError.MalformedFrame(frame))
        }

        val unescaped = unescape(frame.copyOfRange(1, frame.size - 1))
            ?: return BudsResult.Failure(BudsError.MalformedFrame(frame))

        if (unescaped.size < 1 + CRC_LENGTH) {
            return BudsResult.Failure(BudsError.MalformedFrame(frame))
        }

        val body = unescaped.copyOfRange(0, unescaped.size - CRC_LENGTH)
        val trailer = unescaped.copyOfRange(unescaped.size - CRC_LENGTH, unescaped.size)
        if (!crc32Le(body).contentEquals(trailer)) {
            return BudsResult.Failure(BudsError.MalformedFrame(frame))
        }

        val (address, afterAddress) = decodeAddress(body, 0)
            ?: return BudsResult.Failure(BudsError.MalformedFrame(frame))
        if (afterAddress >= body.size) {
            return BudsResult.Failure(BudsError.MalformedFrame(frame))
        }
        val control = body[afterAddress].toInt() and 0xFF
        val payload = body.copyOfRange(afterAddress + 1, body.size)
        return BudsResult.Success(Frame(address, control, payload))
    }

    private fun escape(data: ByteArray): ByteArray {
        val out = ArrayList<Byte>(data.size + 4)
        for (b in data) {
            val v = b.toInt() and 0xFF
            if (v == FLAG || v == ESCAPE) {
                out.add(ESCAPE.toByte())
                out.add((v xor ESCAPE_XOR).toByte())
            } else {
                out.add(b)
            }
        }
        return out.toByteArray()
    }

    private fun unescape(data: ByteArray): ByteArray? {
        val out = ArrayList<Byte>(data.size)
        var i = 0
        while (i < data.size) {
            val v = data[i].toInt() and 0xFF
            if (v == ESCAPE) {
                i++
                if (i >= data.size) return null // dangling escape byte — malformed
                out.add((data[i].toInt() xor ESCAPE_XOR).toByte())
            } else {
                out.add(data[i])
            }
            i++
        }
        return out.toByteArray()
    }

    private fun crc32Le(data: ByteArray): ByteArray {
        val crc = CRC32()
        crc.update(data)
        val value = crc.value
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte(),
        )
    }

    /** pw_hdlc address: 7 value bits per byte shifted left by one, low bits first; bit 0 of the last byte is 1. */
    internal fun encodeAddress(value: Int): ByteArray {
        var v = value
        val out = ArrayList<Byte>(4)
        do {
            var b = (v and 0x7F) shl 1
            v = v ushr 7
            if (v == 0) b = b or 1
            out.add(b.toByte())
        } while (v != 0)
        return out.toByteArray()
    }

    /** Returns `(value, indexAfterAddress)`, or `null` if the buffer ends first or the address exceeds 4 bytes. */
    internal fun decodeAddress(data: ByteArray, start: Int): Pair<Int, Int>? {
        var value = 0
        var shift = 0
        var i = start
        while (true) {
            if (i >= data.size || shift >= 28) return null
            val b = data[i].toInt() and 0xFF
            value = value or (((b shr 1) and 0x7F) shl shift)
            i++
            if (b and 1 == 1) return value to i
            shift += 7
        }
    }
}
