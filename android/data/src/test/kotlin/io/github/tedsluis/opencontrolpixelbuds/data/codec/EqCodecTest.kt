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
    @DisplayName("decode() parses every captured EQ preset frame (pw_hdlc -> RpcPacket -> quintet)")
    fun `decodes real captured EQ preset frames`(fixture: Fixture) {
        val hdlcFrame = (Hdlc.decode(hex(fixture.rawHdlcHex)) as BudsResult.Success).value
        val rpc = (PwRpc.decode(hdlcFrame.payload) as BudsResult.Success).value
        assertEquals(19, rpc.channelId) // 0x13 — the byte the old code called the "correlation byte"
        assertEquals(Maestro.SERVICE_ID, rpc.serviceId)
        assertEquals(Maestro.METHOD_WRITE_SETTING, rpc.methodId)

        val eqResult = EqFrameDecoder.decode(rpc)
        assertInstanceOf(BudsResult.Success::class.java, eqResult)
        val eq = (eqResult as BudsResult.Success).value
        assertEquals(fixture.gains, eq.gains)
        assertEquals(fixture.persist, eq.persist)
        assertEquals(19, eq.channelId)
    }

    @Test
    @DisplayName("CAP-036 frames 1525/1531: a ReadSetting RESPONSE carries the quintet for field 16 (active) / 18 (saved)")
    fun `decodes real ReadSetting responses`() {
        val active = readResponse("7e00a5032a1e221c8201190dc0cccc3d15000000001da099993e25c0cc4c3e2dc0cc4c3e080110151dea71de7d5e2551aed0ae85b618ed7e")
        val saved = readResponse("7e00a5032a1e221c9201190dc0cccc3d15000000001da099993e25c0cc4c3e2dc0cc4c3e080110151dea71de7d5e2551aed0aeebdf2efe7e")
        // wire order low bass → upper treble = 0.1, 0.0, 0.3, 0.2, 0.2 (PROTOCOL.md §4.2); the raw float32s are e.g. 0x3dcccccc,
        // i.e. 0.0999999…, so compare with a tolerance.
        for (frame in listOf(active, saved)) {
            assertEquals(0.1f, frame.gains.lowBass, 1e-4f)
            assertEquals(0.0f, frame.gains.bass, 1e-4f)
            assertEquals(0.3f, frame.gains.mid, 1e-4f)
            assertEquals(0.2f, frame.gains.treble, 1e-4f)
            assertEquals(0.2f, frame.gains.upperTreble, 1e-4f)
        }
        assertEquals(false, active.persist)
        assertEquals(true, saved.persist)
        assertEquals(21, active.channelId)
    }

    private fun readResponse(rawHdlcHex: String): EqFrame {
        val hdlc = (Hdlc.decode(hex(rawHdlcHex)) as BudsResult.Success).value
        assertEquals(10496, hdlc.address) // 00 a5
        val rpc = (PwRpc.decode(hdlc.payload) as BudsResult.Success).value
        assertEquals(PwRpc.TYPE_RESPONSE, rpc.type)
        assertEquals(Maestro.METHOD_READ_SETTING, rpc.methodId)
        return (EqFrameDecoder.decode(rpc) as BudsResult.Success).value
    }

    @Nested
    @DisplayName("Fuzz-adjacent malformed-input handling (AGENTS.md §11)")
    inner class MalformedInputs {

        @Test
        fun `empty buffer never throws`() {
            val result = EqFrameDecoder.decodePayload(ByteArray(0), 19)
            assertInstanceOf(BudsResult.Failure::class.java, result)
            assertInstanceOf(BudsError.MalformedFrame::class.java, (result as BudsResult.Failure).error)
        }

        @Test
        fun `truncated payload never throws`() {
            assertTrue(EqFrameDecoder.decodePayload(hex("2202"), 19) is BudsResult.Failure)
        }

        @Test
        fun `unrecognized outer field never throws`() {
            // Same shape as a real quintet payload but the field-16 tag (82 01) replaced by field 7 (3a) — a different setting.
            val real = ((Hdlc.decode(hex(presetFixtures().toList()[1].rawHdlcHex)) as BudsResult.Success).value.payload)
            val rpc = (PwRpc.decode(real) as BudsResult.Success).value
            val mutated = rpc.payload.copyOf()
            mutated[2] = 0x3a
            assertTrue(EqFrameDecoder.decodePayload(mutated, 19) is BudsResult.Failure)
        }

        @Test
        fun `a non-finite float is rejected`() {
            val rpc = (PwRpc.decode((Hdlc.decode(hex(presetFixtures().toList()[1].rawHdlcHex)) as BudsResult.Success).value.payload) as BudsResult.Success).value
            val mutated = rpc.payload.copyOf()
            // first float32 (bytes 6..9 of the payload: 22 1c 82 01 19 0d <4 bytes>) := NaN
            mutated[6] = 0x00; mutated[7] = 0x00; mutated[8] = 0xc0.toByte(); mutated[9] = 0x7f
            assertTrue(EqFrameDecoder.decodePayload(mutated, 19) is BudsResult.Failure)
        }

        @Test
        fun `declared length overrunning the buffer never throws`() {
            assertTrue(EqFrameDecoder.decodePayload(hex("227f8201" + "00".repeat(10)), 19) is BudsResult.Failure)
        }

        @ParameterizedTest
        @MethodSource("io.github.tedsluis.opencontrolpixelbuds.data.codec.EqFrameDecoderTest#randomByteArrays")
        fun `random byte sequences never throw`(bytes: ByteArray) {
            EqFrameDecoder.decodePayload(bytes, 19)
        }
    }
}

class EqFrameEncoderTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("io.github.tedsluis.opencontrolpixelbuds.data.codec.EqFrameDecoderTest#presetFixtures")
    @DisplayName("encode() + Hdlc.encode() reproduce the real captured wire frame byte-for-byte (channel 19, address 3712)")
    fun `encodes a preset frame matching the real capture`(fixture: EqFrameDecoderTest.Fixture) {
        val realFrame = hex(fixture.rawHdlcHex)
        val realPayload = ((Hdlc.decode(realFrame) as BudsResult.Success).value.payload)

        val encoded = EqFrameEncoder.encode(EqFrame(fixture.gains, persist = fixture.persist, channelId = 19))
        assertTrue(realPayload.contentEquals(encoded)) {
            "expected ${realPayload.toHex()} but got ${encoded.toHex()}"
        }
        val wire = Hdlc.encode(address = 3712, control = PW_HDLC_CONTROL_UI, payload = encoded)
        assertTrue(realFrame.contentEquals(wire)) { "expected ${realFrame.toHex()} but got ${wire.toHex()}" }
    }

    @Test
    @DisplayName("the channel id is what the caller passes — never a hidden default of 0 (the ai-sessions/0041 root cause)")
    fun `encodes the given channel id`() {
        val gains = EqBandGains.FLAT
        for (channel in listOf(19, 21, 24, 26)) {
            val rpc = (PwRpc.decode(EqFrameEncoder.encode(EqFrame(gains, channelId = channel))) as BudsResult.Success).value
            assertEquals(channel, rpc.channelId)
        }
    }

    @Test
    @DisplayName("encode() then decode() round-trips arbitrary gains, including the negative extreme")
    fun `round-trips through decode`() {
        val gains = EqBandGains(upperTreble = -6.0f, treble = 5.9f, mid = 0.0f, bass = -6.0f, lowBass = 5.8f)
        val frame = EqFrame(gains, persist = true, channelId = 24)
        val rpc = (PwRpc.decode(EqFrameEncoder.encode(frame)) as BudsResult.Success).value
        val decoded = EqFrameDecoder.decode(rpc)
        assertInstanceOf(BudsResult.Success::class.java, decoded)
        val result = (decoded as BudsResult.Success).value
        assertEquals(gains, result.gains)
        assertEquals(true, result.persist)
        assertEquals(24, result.channelId)
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
