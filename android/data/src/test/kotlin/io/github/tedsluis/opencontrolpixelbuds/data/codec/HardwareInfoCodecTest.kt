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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.ComponentSerial
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * `GetHardwareInfo` (DECISIONS.md ADR-058, `ai-sessions/0082` item 2): the request on both channels is byte-identical to the official app's, the answer's
 * field 7 decodes to the three component serials by position, an answer without them is an OK result (never a guessed serial), and the reader never throws.
 * Fixtures: [HardwareInfoFixtures] — OpenControl 1.2.0's own exchange of `CAP-072` (`ai-sessions/0084` item 2), the answers with the serials redacted
 * (device identifiers, `AGENTS.md` §9).
 */
class HardwareInfoCodecTest {

    private fun wire(address: Int, packet: RpcPacket) = Hdlc.encode(address, PW_HDLC_CONTROL_UI, PwRpc.encode(packet)).toHex()

    private fun payloadOf(frameHex: String): ByteArray =
        (PwRpc.decode((Hdlc.decode(hex(frameHex)) as BudsResult.Success).value.payload) as BudsResult.Success).value.payload

    @Test
    @DisplayName("the request is byte-identical to OpenControl 1.2.0's own CAP-072 A 7593 (channel 21) and A 8616 (channel 19) — the official app's CAP-036 1415 / CAP-024 801 bytes; empty payload, no call id")
    fun `request matches the official app's on both channels`() {
        assertEquals(HardwareInfoFixtures.REQUEST_CH21_CAP072_A7593, wire(4736, Maestro.getHardwareInfoRequest(21)))
        assertEquals(HardwareInfoFixtures.REQUEST_CH19_CAP072_A8616, wire(3712, Maestro.getHardwareInfoRequest(19)))
        val packet = Maestro.getHardwareInfoRequest(21)
        assertEquals(0, packet.payload.size)
        assertEquals(null, packet.callId)
        assertEquals(PwRpc.nameHash("GetHardwareInfo"), packet.methodId) // 0x28eca5e3, PROTOCOL.md §6
    }

    @Test
    @DisplayName("CAP-072 A 7596: field 7 -> three serials in field order 1, 2, 3; fields 1, 2, 5, 6, 8 are not read")
    fun `the answer's field 7 decodes to the three serials by position`() {
        val serials = HardwareInfo.serials(payloadOf(HardwareInfoFixtures.RESPONSE_CH21_CAP072_A7596_REDACTED))
        assertEquals(HardwareInfoFixtures.SERIALS_REDACTED.mapIndexed { i, s -> ComponentSerial(i + 1, s) }, serials)
    }

    @Test
    @DisplayName("CAP-072 A 8620 (channel 19): the same three strings in the same order — the answer does not depend on the hosting bud")
    fun `the channel-19 answer carries the same serials in the same order`() {
        val serials = HardwareInfo.serials(payloadOf(HardwareInfoFixtures.RESPONSE_CH19_CAP072_A8620_REDACTED))
        assertEquals(HardwareInfoFixtures.SERIALS_REDACTED, serials.map { it.serial })
        assertEquals(listOf(1, 2, 3), serials.map { it.index })
    }

    @Test
    @DisplayName("the router: the whole real frame routes to RoutedFrame.HardwareInfo; an OK answer without field 7 is an RpcResult (UnreadableAnswer upstream)")
    fun `the router routes the answer, and an answer without serials as a plain result`() {
        val routed = CodecRouter().feed(Dlci.MAESTRO, hex(HardwareInfoFixtures.RESPONSE_CH21_CAP072_A7596_REDACTED), timestampMillis = 0)
        assertEquals(RoutedFrame.HardwareInfo(HardwareInfoFixtures.SERIALS_REDACTED.mapIndexed { i, s -> ComponentSerial(i + 1, s) }), routed.single())

        // A labelled supplementary structural case (no capture holds it): the same answer with field 7 absent — fields 1, 2, 5, 6 only.
        val withoutSerials = RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_GET_HARDWARE_INFO, hex("08061011280930 07".replace(" ", "")))
        val plain = CodecRouter().feed(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, PwRpc.encode(withoutSerials)), timestampMillis = 0)
        assertEquals(RoutedFrame.RpcResult(21, Maestro.METHOD_GET_HARDWARE_INFO, PwRpc.TYPE_RESPONSE, null), plain.single())
        assertTrue(HardwareInfo.serials(withoutSerials.payload).isEmpty())

        // An error status for the method is a result too (never a serial).
        val rejected = RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_GET_HARDWARE_INFO, status = 5)
        val error = CodecRouter().feed(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, PwRpc.encode(rejected)), timestampMillis = 0)
        assertEquals(RoutedFrame.RpcResult(21, Maestro.METHOD_GET_HARDWARE_INFO, PwRpc.TYPE_RESPONSE, 5), error.single())
    }

    @Test
    @DisplayName("CAP-072 A 7596 whole: one RFCOMM read holds a runtime-info packet and then the answer — both routed, the serials from the second frame")
    fun `an answer that follows a runtime-info packet in the same read is routed`() {
        val routed = CodecRouter().feed(Dlci.MAESTRO, hex(HardwareInfoFixtures.RFCOMM_CH21_CAP072_A7596_REDACTED), timestampMillis = 0)
        assertEquals(2, routed.size, "two pw_hdlc frames, two routed frames")
        assertTrue(routed[0] is RoutedFrame.RuntimeInfo, "the runtime-info packet first: ${routed[0]}")
        assertEquals(RoutedFrame.HardwareInfo(HardwareInfoFixtures.SERIALS_REDACTED.mapIndexed { i, s -> ComponentSerial(i + 1, s) }), routed[1])
    }

    @Test
    @DisplayName("labelled structural cases: a non-printable, empty, over-long or repeated string is left out; index 4 is ignored — never a guessed serial")
    fun `only printable ASCII strings in fields 1 to 3 are serials`() {
        fun group(vararg entries: Pair<Int, ByteArray>): ByteArray {
            val inner = entries.fold(ByteArray(0)) { acc, (n, b) -> acc + Varint.encode((n shl 3) or 2) + Varint.encode(b.size) + b }
            return Varint.encode((7 shl 3) or 2) + Varint.encode(inner.size) + inner
        }
        val ok = "5707XXXXXXXX51".toByteArray()
        assertEquals(listOf(ComponentSerial(1, "5707XXXXXXXX51")), HardwareInfo.serials(group(1 to ok, 2 to byteArrayOf(0x01, 0x02), 3 to ByteArray(0), 4 to ok)))
        assertEquals(listOf(ComponentSerial(1, "5707XXXXXXXX51")), HardwareInfo.serials(group(1 to ok, 1 to "other".toByteArray())))
        assertTrue(HardwareInfo.serials(group(2 to ByteArray(65) { 'A'.code.toByte() })).isEmpty())
        assertTrue(HardwareInfo.serials(hex("0806")).isEmpty(), "no field 7")
        assertTrue(HardwareInfo.serials(ByteArray(0)).isEmpty())
    }

    @Test
    @DisplayName("fuzz: random payloads and the real answer with mutated bytes never throw (AGENTS.md §11)")
    fun `never throws on arbitrary input`() {
        val random = Random(2026_10_09)
        repeat(3_000) {
            HardwareInfo.serials(ByteArray(random.nextInt(0, 96)) { random.nextInt(256).toByte() })
        }
        val real = payloadOf(HardwareInfoFixtures.RESPONSE_CH21_CAP072_A7596_REDACTED)
        repeat(3_000) {
            val mutated = real.copyOf()
            repeat(random.nextInt(1, 4)) { mutated[random.nextInt(mutated.size)] = random.nextInt(256).toByte() }
            HardwareInfo.serials(mutated)
            HardwareInfo.serials(mutated.copyOfRange(0, random.nextInt(mutated.size)))
        }
        // Through the router, re-sealed with a valid CRC, so the reader is reached (the pattern of OversizedLengthTest).
        val router = CodecRouter()
        repeat(500) {
            val mutated = real.copyOf()
            repeat(random.nextInt(1, 4)) { mutated[random.nextInt(mutated.size)] = random.nextInt(256).toByte() }
            router.feed(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, mutated), timestampMillis = 0)
        }
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}
