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

import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

// Fixed byte-array fixtures pulled directly from raw captures via `tshark`
// (AGENTS.md §11) — no fabricated bytes. Every hex string below is this
// project's own already-committed capture data, not a device identifier
// needing redaction (PROJECT_RULES.md §7's ADR-010 exception covers this
// project's own `captures/`). `hex()` is the shared helper in TestHex.kt.

class AncFrameDecoderTest {

    data class SetFixture(val label: String, val setHex: String, val ackHex: String, val mode: AncMode)

    companion object {
        // CAP-001 (captures/CAP-001-2026-08-09_08-51-00_08-52-20-Group_Z/CAP-001-btsnoop_hci.log),
        // DLCI 0x04, frames 2039/2041, 2132/2134, 2159/2162, 2193/2195 (PROTOCOL.md §4.1).
        // CAP-006 (captures/CAP-006-2026-08-15_17-23-49_17-25-06-Group_B/CAP-006-btsnoop_hci.log),
        // DLCI 0x04, frames 1393/1398, 1627/1630, 1731/1735, 1862/1864 (DECISIONS.md ADR-009 update).
        @JvmStatic
        fun setAckFixtures(): Stream<SetFixture> = Stream.of(
            SetFixture(
                "CAP-001 frame 2039/2041 (Adaptive)",
                "0812001401e8e840cf360aeb181f8107640a737b15b27384",
                "ff010006081201e8e840",
                AncMode.ADAPTIVE,
            ),
            SetFixture(
                "CAP-001 frame 2132/2134 (Transparent)",
                "0812001401e8e88020f970cf9056aafa1f23e86d3aee6a37",
                "ff010006081201e8e880",
                AncMode.TRANSPARENT,
            ),
            SetFixture(
                "CAP-001 frame 2159/2162 (Active)",
                "0812001401e8e8080892480f802a6213a2149f7f590ce6c5",
                "ff010006081201e8e808",
                AncMode.ACTIVE,
            ),
            SetFixture(
                "CAP-001 frame 2193/2195 (Off)",
                "0812001401e8e8205d5fc87f8bb37ae65103d3184a50f7ee",
                "ff010006081201e8e820",
                AncMode.OFF,
            ),
            SetFixture(
                "CAP-006 frame 1393/1398 (Active)",
                "0812001401e8e808dab2a21d8219ab1368b5dd668ecde0a8",
                "ff010006081201e8e808",
                AncMode.ACTIVE,
            ),
            SetFixture(
                "CAP-006 frame 1627/1630 (Off)",
                "0812001401e8e820ff04a960081016aef820c5f14ba07d24",
                "ff010006081201e8e820",
                AncMode.OFF,
            ),
            SetFixture(
                "CAP-006 frame 1731/1735 (Adaptive)",
                "0812001401e8e84026c82eea590a563c42c8e9001d91cd30",
                "ff010006081201e8e840",
                AncMode.ADAPTIVE,
            ),
            SetFixture(
                "CAP-006 frame 1862/1864 (Transparent)",
                "0812001401e8e880ae472e9e51a58f9f69061924c72defa9",
                "ff010006081201e8e880",
                AncMode.TRANSPARENT,
            ),
        )

        @JvmStatic
        fun randomByteArrays(): Stream<ByteArray> {
            val random = java.util.Random(42)
            return Stream.iterate(0) { it + 1 }.limit(200).map {
                ByteArray(random.nextInt(64)) { random.nextInt(256).toByte() }
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("setAckFixtures")
    @DisplayName("decode() parses every captured Set/Ack fixture pair")
    fun `decodes real captured Set and Ack frames`(fixture: SetFixture) {
        val setResult = AncFrameDecoder.decode(hex(fixture.setHex))
        assertInstanceOf(BudsResult.Success::class.java, setResult)
        val setFrame = (setResult as BudsResult.Success).value
        assertInstanceOf(AncFrame.Set::class.java, setFrame)
        setFrame as AncFrame.Set
        assertEquals(fixture.mode, setFrame.mode)
        assertEquals(0x01, setFrame.seekerVersion)
        assertEquals(0xe8, setFrame.settableModesMask)
        assertEquals(0xe8, setFrame.enabledModesMask)
        assertEquals(16, setFrame.reserved.size)

        val ackResult = AncFrameDecoder.decode(hex(fixture.ackHex))
        assertInstanceOf(BudsResult.Success::class.java, ackResult)
        val ackFrame = (ackResult as BudsResult.Success).value
        assertInstanceOf(AncFrame.Ack::class.java, ackFrame)
        ackFrame as AncFrame.Ack
        assertEquals(0x08, ackFrame.echoedGroup)
        assertEquals(0x12, ackFrame.echoedCode)
        // Echoed data: version, settable, enabled, mode — mode byte matches the Set's own.
        assertEquals(fixture.mode.wireBit, ackFrame.data.last().toInt() and 0xFF)
    }

    @Test
    @DisplayName("CAP-036 frame 1169: Get ANC state (DECISIONS.md ADR-021)")
    fun `decodes the Get ANC state frame`() {
        val result = AncFrameDecoder.decode(hex("08110000"))
        assertInstanceOf(BudsResult.Success::class.java, result)
        assertEquals(AncFrame.Get, (result as BudsResult.Success).value)
    }

    @Test
    @DisplayName("CAP-036 frame 1182: Notify ANC state, Settable=0x00 (dock state, DECISIONS.md ADR-024)")
    fun `decodes the Notify ANC state frame with dock-state settable byte`() {
        val result = AncFrameDecoder.decode(hex("0813000401e80020"))
        assertInstanceOf(BudsResult.Success::class.java, result)
        val frame = (result as BudsResult.Success).value
        assertInstanceOf(AncFrame.Notify::class.java, frame)
        frame as AncFrame.Notify
        assertEquals(0x01, frame.version)
        assertEquals(0xe8, frame.uiToggles)
        assertEquals(0x00, frame.settableToggles)
        assertEquals(AncMode.OFF, frame.currentMode)
    }

    @Nested
    @DisplayName("Fuzz-adjacent malformed-input handling (AGENTS.md §11)")
    inner class MalformedInputs {

        @Test
        fun `empty buffer never throws`() {
            val result = AncFrameDecoder.decode(ByteArray(0))
            assertInstanceOf(BudsResult.Failure::class.java, result)
            assertInstanceOf(BudsError.MalformedFrame::class.java, (result as BudsResult.Failure).error)
        }

        @Test
        fun `truncated header never throws`() {
            val result = AncFrameDecoder.decode(hex("0811"))
            assertTrue(result is BudsResult.Failure)
        }

        @Test
        fun `oversized declared length never throws`() {
            // Group 0x08, Code 0x12 (Set), declares 20 data bytes but supplies none.
            val result = AncFrameDecoder.decode(hex("08120014"))
            assertTrue(result is BudsResult.Failure)
        }

        @Test
        fun `undersized declared length never throws`() {
            // Real Get frame's data truncated: declares 0 but 4 bytes follow.
            val result = AncFrameDecoder.decode(hex("081100000101e8e8"))
            assertTrue(result is BudsResult.Failure)
        }

        @Test
        fun `unrecognized Set mode bit is reported as malformed, not silently accepted`() {
            // Same shape as CAP-001 frame 2039, mode byte replaced with an
            // unmapped bit (0x04 — not one of the four confirmed one-hot values).
            val result = AncFrameDecoder.decode(
                hex("0812001401e8e804cf360aeb181f8107640a737b15b27384"),
            )
            assertTrue(result is BudsResult.Failure)
        }

        @Test
        fun `unrecognized Group Code pair never throws`() {
            val result = AncFrameDecoder.decode(hex("aabb0002ccdd"))
            assertTrue(result is BudsResult.Failure)
        }

        @ParameterizedTest
        @MethodSource("io.github.tedsluis.opencontrolpixelbuds.data.codec.AncFrameDecoderTest#randomByteArrays")
        fun `random byte sequences never throw`(bytes: ByteArray) {
            // The property under test IS "never throws" — any BudsResult is acceptable.
            AncFrameDecoder.decode(bytes)
        }
    }
}

class AncFrameEncoderTest {

    @Test
    @DisplayName("encode(Get) matches CAP-036 frame 1169 byte-for-byte")
    fun `encodes the Get frame`() {
        assertEquals("08110000", AncFrameEncoder.encode(AncFrame.Get).toHexString())
    }

    @ParameterizedTest
    @MethodSource("io.github.tedsluis.opencontrolpixelbuds.data.codec.AncFrameDecoderTest#setAckFixtures")
    @DisplayName("encode(Set) round-trips through decode() back to the same mode")
    fun `round-trips a Set frame through decode`(fixture: AncFrameDecoderTest.SetFixture) {
        val encoded = AncFrameEncoder.encode(AncFrame.Set(mode = fixture.mode))
        val decoded = AncFrameDecoder.decode(encoded)
        assertInstanceOf(BudsResult.Success::class.java, decoded)
        val frame = (decoded as BudsResult.Success).value as AncFrame.Set
        assertEquals(fixture.mode, frame.mode)
    }
}

private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
