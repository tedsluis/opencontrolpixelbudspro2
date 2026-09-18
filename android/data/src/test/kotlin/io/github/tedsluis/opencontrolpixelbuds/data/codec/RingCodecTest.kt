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
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class RingFrameDecoderTest {

    companion object {
        @JvmStatic
        fun randomByteArrays(): Stream<ByteArray> {
            val random = java.util.Random(21)
            return Stream.iterate(0) { it + 1 }.limit(200).map {
                ByteArray(random.nextInt(48)) { random.nextInt(256).toByte() }
            }
        }
    }

    // CAP-025 (captures/CAP-025-2026-08-21_08-40-52_08-45-26-Group_K/CAP-025-btsnoop_hci.log),
    // DLCI 0x04, frames 2040/2044/2048 (Right), 2131 (Left), 2120/2123/2127 (Stop) — PROTOCOL.md
    // §4.4, DECISIONS.md ADR-011.

    @Test
    @DisplayName("CAP-025 frame 2040: Start ringing Right")
    fun `decodes Start Right`() {
        val result = RingFrameDecoder.decode(hex("0401000101"))
        assertInstanceOf(BudsResult.Success::class.java, result)
        assertEquals(RingFrame.Start(RingTarget.RIGHT), (result as BudsResult.Success).value)
    }

    @Test
    @DisplayName("CAP-025 frame 2131: Start ringing Left")
    fun `decodes Start Left`() {
        val result = RingFrameDecoder.decode(hex("0401000102"))
        assertInstanceOf(BudsResult.Success::class.java, result)
        assertEquals(RingFrame.Start(RingTarget.LEFT), (result as BudsResult.Success).value)
    }

    @Test
    @DisplayName("CAP-025 frame 2120: Stop/mute (shared, not per-earbud)")
    fun `decodes Stop`() {
        val result = RingFrameDecoder.decode(hex("0401000100"))
        assertInstanceOf(BudsResult.Success::class.java, result)
        assertEquals(RingFrame.Stop, (result as BudsResult.Success).value)
    }

    @Test
    @DisplayName("CAP-025 frame 2044: 3-data-byte ACK variant")
    fun `decodes the longer Ack variant`() {
        val result = RingFrameDecoder.decode(hex("ff010003040100"))
        assertInstanceOf(BudsResult.Success::class.java, result)
        val ack = (result as BudsResult.Success).value
        assertInstanceOf(RingFrame.Ack::class.java, ack)
        ack as RingFrame.Ack
        assertEquals(0x04, ack.echoedGroup)
        assertEquals(0x01, ack.echoedCode)
        assertTrue(byteArrayOf(0x00).contentEquals(ack.data))
    }

    @Test
    @DisplayName("CAP-025 frame 2048: 2-data-byte ACK variant")
    fun `decodes the shorter Ack variant`() {
        val result = RingFrameDecoder.decode(hex("ff0100020401"))
        assertInstanceOf(BudsResult.Success::class.java, result)
        val ack = (result as BudsResult.Success).value
        assertInstanceOf(RingFrame.Ack::class.java, ack)
        ack as RingFrame.Ack
        assertEquals(0x04, ack.echoedGroup)
        assertEquals(0x01, ack.echoedCode)
        assertTrue(ack.data.isEmpty())
    }

    @Nested
    @DisplayName("Fuzz-adjacent malformed-input handling (AGENTS.md §11)")
    inner class MalformedInputs {

        @Test
        fun `empty buffer never throws`() {
            val result = RingFrameDecoder.decode(ByteArray(0))
            assertInstanceOf(BudsResult.Failure::class.java, result)
            assertInstanceOf(BudsError.MalformedFrame::class.java, (result as BudsResult.Failure).error)
        }

        @Test
        fun `unrecognized ring value never throws`() {
            assertTrue(RingFrameDecoder.decode(hex("0401000103")) is BudsResult.Failure)
        }

        @Test
        fun `oversized declared length never throws`() {
            assertTrue(RingFrameDecoder.decode(hex("040100ff")) is BudsResult.Failure)
        }

        @ParameterizedTest
        @MethodSource("io.github.tedsluis.opencontrolpixelbuds.data.codec.RingFrameDecoderTest#randomByteArrays")
        fun `random byte sequences never throw`(bytes: ByteArray) {
            RingFrameDecoder.decode(bytes)
        }
    }
}

class RingFrameEncoderTest {

    @Test
    fun `encodes Start Right matching CAP-025 frame 2040`() {
        assertEquals("0401000101", RingFrameEncoder.encode(RingFrame.Start(RingTarget.RIGHT)).toHexString())
    }

    @Test
    fun `encodes Start Left matching CAP-025 frame 2131`() {
        assertEquals("0401000102", RingFrameEncoder.encode(RingFrame.Start(RingTarget.LEFT)).toHexString())
    }

    @Test
    fun `encodes Stop matching CAP-025 frame 2120`() {
        assertEquals("0401000100", RingFrameEncoder.encode(RingFrame.Stop).toHexString())
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(RingTarget::class)
    @DisplayName("encode(Start) round-trips through decode() back to the same target")
    fun `round-trips a Start frame through decode`(target: RingTarget) {
        val decoded = RingFrameDecoder.decode(RingFrameEncoder.encode(RingFrame.Start(target)))
        assertEquals(RingFrame.Start(target), (decoded as BudsResult.Success).value)
    }
}

private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
