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
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
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
        // Group 0x07 (SASS) Code 0x34 — a real periodic message the Buds push on this channel, whose
        // meaning is still an open question (PROTOCOL.md §6), so this router correctly does not decode
        // it as anything, only flags it. (Payload bytes here are dummies; only Group/Code/length matter.)
        // This test used to use Group 0x03 Code 0x03 (battery) — that message is now decoded (ADR-033).
        val routed = router.feed(
            Dlci.FAST_PAIR_MESSAGE_STREAM,
            hex("0734000c") + ByteArray(12) { 0x01 },
            timestampMillis = 123L,
            onUnidentified = { unidentified += it },
        )
        assertTrue(routed.isEmpty())
        assertEquals(1, unidentified.size)
        assertEquals(0x07, unidentified[0].group)
        assertEquals(0x34, unidentified[0].code)
        assertEquals(123L, unidentified[0].timestampMillis)
    }

    @Test
    @DisplayName("DLCI 0x04: a Battery updated frame is decoded and routed, not left unidentified (ADR-033)")
    fun `routes a Battery updated frame`() {
        val router = CodecRouter()
        val unidentified = mutableListOf<UnidentifiedFrame>()
        val routed = router.feed(
            Dlci.FAST_PAIR_MESSAGE_STREAM,
            hex("03030003" + "605fff"),
            timestampMillis = 1L,
            onUnidentified = { unidentified += it },
        )
        assertEquals(1, routed.size)
        val battery = (routed[0] as RoutedFrame.Battery).frame
        assertEquals(BatteryLevel.Known(96, false), battery.left)
        assertEquals(BatteryLevel.Known(95, false), battery.right)
        assertTrue(unidentified.isEmpty())
    }

    @Test
    @DisplayName("DLCI 0x04: two battery frames glued into one socket read (seen in real logs) both decode")
    fun `routes two concatenated Battery updated frames`() {
        val router = CodecRouter()
        val routed = router.feed(
            Dlci.FAST_PAIR_MESSAGE_STREAM,
            hex("03030003" + "6464ff" + "03030003" + "6464ff"),
            timestampMillis = 1L,
        )
        assertEquals(2, routed.size)
    }

    @Test
    @DisplayName("DLCI 0x02: splits two back-to-back real pw_hdlc frames sharing a flag byte (CAP-015 frames 2116 and 2117)")
    fun `routes two concatenated HDLC frames from one chunk`() {
        val router = CodecRouter()
        val frame2116 = // SERVER_STREAM SubscribeToSettingsChanges: the all-zero EQ mirrored by the Buds
            "7e80a3032a1e221c8201190d0000000015000000001d0000000025000000002d00000000080710131dea71de7d5e25f5ad2128b1cbb10b7e"
        val frame2117 = "7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e" // the empty RESPONSE to the WriteSetting
        // Real streams share the boundary flag (one frame's trailing 0x7E doubles as the next frame's leading 0x7E).
        val routed = router.feed(Dlci.MAESTRO, hex(frame2116 + frame2117.drop(2)), timestampMillis = 0)
        assertEquals(2, routed.size)
        assertEquals(0f, (routed[0] as RoutedFrame.Eq).frame.gains.lowBass)
        assertTrue((routed[1] as RoutedFrame.RpcResult).isOk)
    }

    // Real frames (CAP-036 / CAP-015; the GetSoftwareInfo tail is the real header with the serial-carrying payload left out).
    private val readResponse16 =
        "7e00a5032a1e221c8201190dc0cccc3d15000000001da099993e25c0cc4c3e2dc0cc4c3e080110151dea71de7d5e2551aed0ae85b618ed7e"
    private val writeAck2117 = "7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e"

    @Test
    @DisplayName("DLCI 0x02: a ReadSetting RESPONSE (CAP-036 frame 1525) is routed as the EQ value and logged as a pw_rpc packet")
    fun `routes a ReadSetting response as an EQ value`() {
        val router = CodecRouter()
        val packets = mutableListOf<RpcPacket>()
        val routed = router.feed(Dlci.MAESTRO, hex(readResponse16), timestampMillis = 0, onRpcPacket = { packets += it })
        assertEquals(1, routed.size)
        val eq = (routed[0] as RoutedFrame.Eq).frame
        assertEquals(21, eq.channelId)
        assertEquals(false, eq.persist)
        assertEquals(1, packets.size)
        assertEquals("pw_rpc RESPONSE ch=21 method=ReadSetting status=OK", packets[0].summary())
    }

    @Test
    @DisplayName("DLCI 0x02: the empty RESPONSE to a WriteSetting (CAP-015 frame 2117) is an OK RpcResult")
    fun `routes a WriteSetting acknowledgement`() {
        val routed = CodecRouter().feed(Dlci.MAESTRO, hex(writeAck2117), timestampMillis = 0)
        val result = routed.single() as RoutedFrame.RpcResult
        assertEquals(Maestro.METHOD_WRITE_SETTING, result.methodId)
        assertTrue(result.isOk)
        assertEquals(19, result.channelId)
    }

    @Test
    @DisplayName("DLCI 0x02: a RESPONSE with status UNKNOWN / an error packet for our request is a non-OK RpcResult")
    fun `routes error results`() {
        val router = CodecRouter()
        fun frame(packet: RpcPacket) = Hdlc.encode(3712, PW_HDLC_CONTROL_UI, PwRpc.encode(packet))
        val readUnknown = RpcPacket(PwRpc.TYPE_RESPONSE, 19, Maestro.SERVICE_ID, Maestro.METHOD_READ_SETTING, status = 2) // CAP-015 frame 1920
        val serverError = RpcPacket(PwRpc.TYPE_SERVER_ERROR, 19, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, status = 5)
        for (packet in listOf(readUnknown, serverError)) {
            val result = router.feed(Dlci.MAESTRO, frame(packet), timestampMillis = 0).single() as RoutedFrame.RpcResult
            assertTrue(!result.isOk)
            assertEquals(packet.status, result.status)
        }
    }

    @Test
    @DisplayName("DLCI 0x02: the Buds' unsolicited GetSoftwareInfo push (real header, CAP-036 frame 1405) announces the channel")
    fun `routes the unsolicited announcement`() {
        val tail = hex("08011015" + "1dea71de7e25" + "44fa9971" + "38ffffffff0f") // type 1, ch 21, svc, method, call_id 0xFFFFFFFF
        val frame = Hdlc.encode(10496, PW_HDLC_CONTROL_UI, tail)
        val routed = CodecRouter().feed(Dlci.MAESTRO, frame, timestampMillis = 0)
        assertEquals(RoutedFrame.MaestroHello(21), routed.single())
    }

    @Test
    @DisplayName("DLCI 0x02: a Maestro packet for anything else (other service, other method) stays UnidentifiedFrame")
    fun `other pw_rpc packets stay unidentified`() {
        val unidentified = mutableListOf<UnidentifiedFrame>()
        val other = RpcPacket(PwRpc.TYPE_REQUEST, 21, 0x73d5d805, 0x73b772ce)
        val routed = CodecRouter().feed(
            Dlci.MAESTRO,
            Hdlc.encode(4736, PW_HDLC_CONTROL_UI, PwRpc.encode(other)),
            timestampMillis = 5L,
            onUnidentified = { unidentified += it },
        )
        assertTrue(routed.isEmpty())
        assertEquals(1, unidentified.size)
    }

    @Test
    @DisplayName("DLCI 0x02: a stream update for the *saved* EQ (field 18) is routed with persist = true, never as the active EQ")
    fun `field 18 is flagged as persisted`() {
        val eq = EqFrame(io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains.FLAT, persist = true, channelId = 21)
        val payload = EqFrameEncoder.settingsPayload(eq)
        val packet = RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_READ_SETTING, payload)
        val routed = CodecRouter().feed(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, PwRpc.encode(packet)), timestampMillis = 0)
        assertEquals(true, (routed.single() as RoutedFrame.Eq).frame.persist)
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
