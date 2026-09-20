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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class HdlcTest {

    companion object {
        @JvmStatic
        fun randomByteArrays(): Stream<ByteArray> {
            val random = java.util.Random(7)
            return Stream.iterate(0) { it + 1 }.limit(200).map {
                ByteArray(random.nextInt(64)) { random.nextInt(256).toByte() }
            }
        }

        @JvmStatic
        fun randomFlagDelimitedArrays(): Stream<ByteArray> {
            // Frames that at least pass the "starts and ends with 0x7E" gate, to
            // exercise the escape/CRC-check paths rather than only the early exit.
            val random = java.util.Random(9)
            return Stream.iterate(0) { it + 1 }.limit(200).map {
                val body = ByteArray(random.nextInt(40)) { random.nextInt(256).toByte() }
                byteArrayOf(0x7e) + body + byteArrayOf(0x7e)
            }
        }
    }

    @Test
    @DisplayName("CAP-015 frame 2111: pw_hdlc address 00 3b (=3712), control 0x03, CRC-32 verified (PROTOCOL.md §2.2a)")
    fun `decodes a real DLCI 0x02 frame`() {
        val raw = hex(
            "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000000015" +
                "000000001d0000000025000000002d00000000881667fe7e",
        )
        val result = Hdlc.decode(raw)
        assertInstanceOf(BudsResult.Success::class.java, result)
        val frame = (result as BudsResult.Success).value
        assertEquals(3712, frame.address) // 00 3b: 0 | (0x3b >> 1) << 7
        assertEquals(0x03, frame.control)
        assertEquals(44, frame.payload.size)
        assertEquals(0x10, frame.payload[0].toInt() and 0xFF) // RpcPacket field 2 (channel_id)
    }

    @Test
    @DisplayName("pw_hdlc addresses seen in the captures decode to their values and re-encode to the same bytes")
    fun `address varint round-trips the captured addresses`() {
        // (address bytes) -> value: 00 3b, 00 4b (requests, ch 19/21), 80 a3, 00 a5 (responses), 80 3d, 80 d3 (ch 24) — PROTOCOL.md §2.2a.
        val cases = mapOf("003b" to 3712, "004b" to 4736, "803d" to 3904, "804d" to 4928, "80a3" to 10432, "00a5" to 10496, "80d3" to 13504)
        for ((bytes, value) in cases) {
            assertEquals(value, Hdlc.decodeAddress(hex(bytes), 0)!!.first, bytes)
            assertEquals(bytes, Hdlc.encodeAddress(value).joinToString("") { "%02x".format(it) }, bytes)
        }
    }

    @Test
    fun `an address without a terminating byte never decodes`() {
        assertEquals(null, Hdlc.decodeAddress(hex("0000000000"), 0))
        assertEquals(null, Hdlc.decodeAddress(hex("00"), 0))
    }

    @Test
    @DisplayName("encode() then decode() round-trips address, control, and payload")
    fun `round-trips through encode and decode`() {
        val payload = byteArrayOf(0x01, 0x7e, 0x7d, 0x02, 0x03) // includes bytes that must be escaped
        val encoded = Hdlc.encode(address = 0x1e80, control = 0x03, payload = payload)
        val decoded = Hdlc.decode(encoded)
        assertInstanceOf(BudsResult.Success::class.java, decoded)
        val frame = (decoded as BudsResult.Success).value
        assertEquals(0x1e80, frame.address)
        assertEquals(0x03, frame.control)
        assertTrue(payload.contentEquals(frame.payload))
    }

    @Test
    @DisplayName("a corrupted CRC is reported as MalformedFrame, never accepted")
    fun `rejects a corrupted CRC`() {
        val encoded = Hdlc.encode(address = 3712, control = 0x03, payload = byteArrayOf(1, 2, 3))
        val corrupted = encoded.copyOf()
        corrupted[corrupted.size - 3] = (corrupted[corrupted.size - 3] + 1).toByte()
        val result = Hdlc.decode(corrupted)
        assertInstanceOf(BudsResult.Failure::class.java, result)
        assertInstanceOf(BudsError.MalformedFrame::class.java, (result as BudsResult.Failure).error)
    }

    @Test
    fun `missing flags never throws`() {
        val result = Hdlc.decode(hex("00010203"))
        assertInstanceOf(BudsResult.Failure::class.java, result)
    }

    @Test
    fun `dangling escape byte never throws`() {
        val result = Hdlc.decode(hex("7e00017d7e"))
        assertInstanceOf(BudsResult.Failure::class.java, result)
    }

    @ParameterizedTest
    @MethodSource("randomByteArrays")
    fun `random byte sequences never throw`(bytes: ByteArray) {
        Hdlc.decode(bytes)
    }

    @ParameterizedTest
    @MethodSource("randomFlagDelimitedArrays")
    fun `random flag-delimited sequences never throw`(bytes: ByteArray) {
        Hdlc.decode(bytes)
    }
}
