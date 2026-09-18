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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class EqFrameDecoderTest {

    data class Fixture(val label: String, val rawHdlcHex: String, val gains: EqBandGains, val persist: Boolean) {
        override fun toString() = label
    }

    companion object {
        // CAP-015 (captures/CAP-015-2026-08-18_06-11-06_06-17-40-Group_T/CAP-015-btsnoop_hci.log),
        // DLCI 0x02, frames 2111/2165/2227 (PROTOCOL.md §4.2, preset quintets, DECISIONS.md ADR-016).
        @JvmStatic
        fun presetFixtures(): Stream<Fixture> = Stream.of(
            Fixture(
                "CAP-015 frame 2111 (Last saved baseline, all-zero)",
                "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000000015" +
                    "000000001d0000000025000000002d00000000881667fe7e",
                EqBandGains(upperTreble = 0f, treble = 0f, mid = 0f, bass = 0f, lowBass = 0f),
                persist = false,
            ),
            Fixture(
                "CAP-015 frame 2165 (Heavy bass preset)",
                "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015" +
                    "000040401d0000000025000000002d000000007b1bccbe7e",
                EqBandGains(upperTreble = 0f, treble = 0f, mid = 0f, bass = 3.0f, lowBass = 5.0f),
                persist = false,
            ),
            Fixture(
                "CAP-015 frame 2227 (Light bass preset)",
                "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a0c015" +
                    "0000c0bf1d0000000025000000002d00000000e774823b7e",
                EqBandGains(upperTreble = 0f, treble = 0f, mid = 0f, bass = -1.5f, lowBass = -5.0f),
                persist = false,
            ),
        )

        @JvmStatic
        fun randomByteArrays(): Stream<ByteArray> {
            val random = java.util.Random(13)
            return Stream.iterate(0) { it + 1 }.limit(200).map {
                ByteArray(random.nextInt(80)) { random.nextInt(256).toByte() }
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("presetFixtures")
    @DisplayName("decode() parses every captured EQ preset frame")
    fun `decodes real captured EQ preset frames`(fixture: Fixture) {
        val hdlcResult = Hdlc.decode(hex(fixture.rawHdlcHex))
        assertInstanceOf(BudsResult.Success::class.java, hdlcResult)
        val hdlcFrame = (hdlcResult as BudsResult.Success).value
        assertEquals(0x13, hdlcFrame.payload[2].toInt() and 0xFF) // this session's correlation byte

        val eqResult = EqFrameDecoder.decode(hdlcFrame.payload)
        assertInstanceOf(BudsResult.Success::class.java, eqResult)
        val eq = (eqResult as BudsResult.Success).value
        assertEquals(fixture.gains, eq.gains)
        assertEquals(fixture.persist, eq.persist)
    }

    @Nested
    @DisplayName("Fuzz-adjacent malformed-input handling (AGENTS.md §11)")
    inner class MalformedInputs {

        @Test
        fun `empty buffer never throws`() {
            val result = EqFrameDecoder.decode(ByteArray(0))
            assertInstanceOf(BudsResult.Failure::class.java, result)
            assertInstanceOf(BudsError.MalformedFrame::class.java, (result as BudsResult.Failure).error)
        }

        @Test
        fun `truncated prefix never throws`() {
            assertTrue(EqFrameDecoder.decode(hex("0310")) is BudsResult.Failure)
        }

        @Test
        fun `unrecognized outer field never throws`() {
            // Same shape as a real preset frame's payload but field 5 tag replaced
            // with an unrelated field (7) — a real, gated §4.5 setting, not EQ.
            val real = Hdlc.decode(
                hex(
                    "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000000015" +
                        "000000001d0000000025000000002d00000000881667fe7e",
                ),
            ) as BudsResult.Success
            val mutated = real.value.payload.copyOf()
            mutated[13] = 0x3a // field 7, wiretype 2 — a different setting entirely
            assertTrue(EqFrameDecoder.decode(mutated) is BudsResult.Failure)
        }

        @Test
        fun `declared length overrunning the buffer never throws`() {
            assertTrue(
                EqFrameDecoder.decode(hex("0310131dea71de7e251d9a8c9e2a7f22" + "00".repeat(10))) is BudsResult.Failure,
            )
        }

        @ParameterizedTest
        @MethodSource("io.github.tedsluis.opencontrolpixelbuds.data.codec.EqFrameDecoderTest#randomByteArrays")
        fun `random byte sequences never throw`(bytes: ByteArray) {
            EqFrameDecoder.decode(bytes)
        }
    }
}

class EqFrameEncoderTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("io.github.tedsluis.opencontrolpixelbuds.data.codec.EqFrameDecoderTest#presetFixtures")
    @DisplayName("encode() matches the real captured payload byte-for-byte")
    fun `encodes a preset frame matching the real capture`(fixture: EqFrameDecoderTest.Fixture) {
        val hdlcResult = Hdlc.decode(hex(fixture.rawHdlcHex)) as BudsResult.Success
        val realPayload = hdlcResult.value.payload

        val encoded = EqFrameEncoder.encode(EqFrame(fixture.gains, persist = fixture.persist, correlationByte = 0x13))
        assertTrue(realPayload.contentEquals(encoded)) {
            "expected ${realPayload.toHex()} but got ${encoded.toHex()}"
        }
    }

    @Test
    @DisplayName("encode() then decode() round-trips arbitrary gains, including the negative extreme")
    fun `round-trips through decode`() {
        val gains = EqBandGains(upperTreble = -6.0f, treble = 5.9f, mid = 0.0f, bass = -6.0f, lowBass = 5.8f)
        val frame = EqFrame(gains, persist = true, correlationByte = 0x42)
        val encoded = EqFrameEncoder.encode(frame)
        val decoded = EqFrameDecoder.decode(encoded)
        assertInstanceOf(BudsResult.Success::class.java, decoded)
        val result = (decoded as BudsResult.Success).value
        assertEquals(gains, result.gains)
        assertEquals(true, result.persist)
        assertEquals(0x42, result.correlationByte)
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
