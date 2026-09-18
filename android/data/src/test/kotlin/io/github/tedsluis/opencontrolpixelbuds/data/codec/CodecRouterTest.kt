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
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CodecRouterTest {

    companion object {
        @JvmStatic
        fun randomChunks(): Stream<Pair<Int, ByteArray>> {
            val random = java.util.Random(99)
            return Stream.iterate(0) { it + 1 }.limit(150).map {
                val channel = listOf(Dlci.MAESTRO, Dlci.FAST_PAIR_MESSAGE_STREAM, 0x08).random(random.asKotlinRandom())
                channel to ByteArray(random.nextInt(50)) { random.nextInt(256).toByte() }
            }
        }

        private fun java.util.Random.asKotlinRandom() = kotlin.random.Random(this.nextLong())
    }

    @Test
    @DisplayName("DLCI 0x04: splits two back-to-back real ANC Set frames from one socket-read chunk")
    fun `routes two concatenated Message Stream frames from one chunk`() {
        val router = CodecRouter()
        // CAP-001 frames 2039 (Adaptive) + 2132 (Transparent), concatenated as if
        // delivered in a single socket read.
        val chunk = hex(
            "0812001401e8e840cf360aeb181f8107640a737b15b27384" +
                "0812001401e8e88020f970cf9056aafa1f23e86d3aee6a37",
        )
        val routed = router.feed(Dlci.FAST_PAIR_MESSAGE_STREAM, chunk, timestampMillis = 0)
        assertEquals(2, routed.size)
        assertEquals(AncMode.ADAPTIVE, (routed[0] as RoutedFrame.Anc).let { (it.frame as AncFrame.Set).mode })
        assertEquals(AncMode.TRANSPARENT, (routed[1] as RoutedFrame.Anc).let { (it.frame as AncFrame.Set).mode })
    }

    @Test
    @DisplayName("DLCI 0x04: a frame split across two chunks is only routed once the second chunk arrives")
    fun `buffers a Message Stream frame split across two reads`() {
        val router = CodecRouter()
        val full = hex("0401000101") // CAP-025 frame 2040, Start Right
        val first = router.feed(Dlci.FAST_PAIR_MESSAGE_STREAM, full.copyOfRange(0, 2), timestampMillis = 0)
        assertTrue(first.isEmpty())
        val second = router.feed(Dlci.FAST_PAIR_MESSAGE_STREAM, full.copyOfRange(2, full.size), timestampMillis = 0)
        assertEquals(1, second.size)
        assertEquals(RingTarget.RIGHT, (second[0] as RoutedFrame.Ring).let { (it.frame as RingFrame.Start).target })
    }

    @Test
    @DisplayName("DLCI 0x04: a well-formed but unrecognized Group/Code is UnidentifiedFrame, not dropped")
    fun `surfaces an unrecognized Message Stream frame as UnidentifiedFrame`() {
        val router = CodecRouter()
        val unidentified = mutableListOf<UnidentifiedFrame>()
        // Group 0x03 Code 0x03 ("Battery updated", DECISIONS.md ADR-031) — a real, FACT-identified
        // message, but its implementation gate is unresolved (ARCHITECTURE.md §5a), so this router
        // correctly does not decode it as anything, only flags it.
        val routed = router.feed(
            Dlci.FAST_PAIR_MESSAGE_STREAM,
            hex("03030003") + byteArrayOf(0x50, 0x50, 0xff.toByte()),
            timestampMillis = 123L,
            onUnidentified = { unidentified += it },
        )
        assertTrue(routed.isEmpty())
        assertEquals(1, unidentified.size)
        assertEquals(0x03, unidentified[0].group)
        assertEquals(0x03, unidentified[0].code)
        assertEquals(123L, unidentified[0].timestampMillis)
    }

    @Test
    @DisplayName("DLCI 0x02: splits two back-to-back real EQ preset frames sharing a flag byte")
    fun `routes two concatenated HDLC frames from one chunk`() {
        val router = CodecRouter()
        val frame2111 =
            "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000000015" +
                "000000001d0000000025000000002d00000000881667fe7e"
        val frame2165 =
            "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015" +
                "000040401d0000000025000000002d000000007b1bccbe7e"
        // Real streams share the boundary flag (one frame's trailing 0x7E doubles
        // as the next frame's leading 0x7E) — keep frame2111's trailing flag, drop
        // frame2165's own leading one, so exactly one flag separates them.
        val concatenated = hex(frame2111 + frame2165.drop(2))
        val routed = router.feed(Dlci.MAESTRO, concatenated, timestampMillis = 0)
        assertEquals(2, routed.size)
        assertEquals(0f, (routed[0] as RoutedFrame.Eq).frame.gains.lowBass)
        assertEquals(5.0f, (routed[1] as RoutedFrame.Eq).frame.gains.lowBass)
    }

    @Test
    @DisplayName("out-of-scope DLCIs (e.g. 0x08) are ignored, not guessed at")
    fun `ignores channels outside this session's implementation scope`() {
        val router = CodecRouter()
        val routed = router.feed(0x08, byteArrayOf(1, 2, 3, 4, 5), timestampMillis = 0)
        assertTrue(routed.isEmpty())
    }

    @ParameterizedTest
    @MethodSource("randomChunks")
    @DisplayName("fuzz: random chunks on any in-scope or out-of-scope channel never throw")
    fun `random chunks never throw`(input: Pair<Int, ByteArray>) {
        val router = CodecRouter()
        router.feed(input.first, input.second, timestampMillis = 0)
    }
}
