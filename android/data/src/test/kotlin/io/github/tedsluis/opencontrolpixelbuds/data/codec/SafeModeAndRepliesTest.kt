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

import io.github.tedsluis.opencontrolpixelbuds.data.SafeModeGate
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** Message Stream ACK/NAK decoding, the Model ID decoder, the DLCI 0x08 request and the Safe-Mode gate (ai-sessions/0045). */
class SafeModeAndRepliesTest {

    @Test
    @DisplayName("the spec's NAK example (ff 02 00 04 01 04 01 00, device busy for a Ring) decodes reason-first")
    fun `decodes the spec NAK example`() {
        val nak = (MessageStreamReplyDecoder.decode(hex("ff02000401040100")) as BudsResult.Success).value as MessageStreamReply.Nak
        assertEquals(0x01, nak.reason)
        assertEquals(0x04, nak.echoedGroup)
        assertEquals(0x01, nak.echoedCode)
        assertTrue(nak.answers(0x04, 0x01))
        assertEquals("device busy", MessageStreamAck.reasonName(nak.reason))
        assertEquals("incorrect message authentication code", MessageStreamAck.reasonName(0x03))
    }

    @Test
    fun `short, over-long or non-FF frames are malformed, never thrown`() {
        listOf("ff01", "ff010001", "ff0200020801", "ff01000308", "0812000401e8e840", "ff03000208 12".replace(" ", "")).forEach {
            assertInstanceOf(BudsResult.Failure::class.java, MessageStreamReplyDecoder.decode(hex(it)), it)
        }
    }

    @Test
    fun `fuzz - random bytes never throw in the reply and Model ID decoders`() {
        val random = java.util.Random(45)
        repeat(2_000) {
            val bytes = ByteArray(random.nextInt(12)) { random.nextInt(256).toByte() }
            if (random.nextBoolean() && bytes.isNotEmpty()) bytes[0] = 0xFF.toByte()
            MessageStreamReplyDecoder.decode(bytes)
            ModelIdFrameDecoder.decode(bytes)
        }
    }

    @Test
    @DisplayName("Model ID: CAP-059 frame 1049's embedded 03 01 00 03 da 2d b1")
    fun `decodes the Pro 2 model id`() {
        assertEquals("da2db1", (ModelIdFrameDecoder.decode(hex("03010003da2db1")) as BudsResult.Success).value.modelIdHex)
        assertInstanceOf(BudsResult.Failure::class.java, ModelIdFrameDecoder.decode(hex("03030003605fff"))) // battery, not model id
    }

    @Test
    fun `Safe Mode gate - verified firmware and the Pro 2 model id are allowed`() {
        assertEquals(SafeModeGate.Verdict.Allowed, SafeModeGate.evaluate(listOf("release_5.203"), "da2db1", requireModelId = true))
        assertEquals(SafeModeGate.Verdict.Allowed, SafeModeGate.evaluate(listOf("release_5.203"), null, requireModelId = false))
    }

    @Test
    fun `Safe Mode gate - every refusal reason`() {
        fun refused(fw: List<String>?, id: String?, req: Boolean) =
            assertInstanceOf(SafeModeGate.Verdict.Refused::class.java, SafeModeGate.evaluate(fw, id, req))
        refused(null, "da2db1", true) // not announced
        refused(emptyList(), "da2db1", true)
        refused(listOf("release_5.203", "release_5.204"), "da2db1", true) // one component unverified
        refused(listOf("release_5.203"), "aabbcc", false) // another model
        refused(listOf("release_5.203"), null, true) // no model id on the claim
    }

    @Test
    fun `Safe Mode observed state - nothing known is not Safe Mode, a contradiction is`() {
        assertNull(SafeModeGate.observed(null, null))
        assertNull(SafeModeGate.observed(listOf("release_5.203"), "da2db1"))
        assertEquals(listOf("release_6.000"), SafeModeGate.observed(listOf("release_6.000"), null)?.firmware)
        assertEquals("aabbcc", SafeModeGate.observed(null, "aabbcc")?.modelIdHex)
    }
}
