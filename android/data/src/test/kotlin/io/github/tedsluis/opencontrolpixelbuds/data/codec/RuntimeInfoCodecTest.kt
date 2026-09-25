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

import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * `SubscribeRuntimeInfo` — the Case battery source of DECISIONS.md ADR-043 (PROTOCOL.md §4.3 Option F). Every fixture is a real pw_rpc
 * packet (HDLC-unescaped, CRC-32 verified, `ai-sessions/0046`): the official app's request `CAP-036` frame 1410, a stream packet with the
 * Case entry `CAP-041` frame 782 (Case 0x4f = 79, the on-screen and DLCI 0x08 value of that capture), one without it `CAP-050` frame 1163.
 */
class RuntimeInfoCodecTest {

    /** `CAP-041` frame 782, 5th pw_hdlc frame: `2:<ms> 3:0 6:{1:{1:79 2:1} 2:{1:100 2:2} 3:{1:100 2:2}} 7:{1:1 2:1 3:0}`, SERVER_STREAM ch 21. */
    private val withCase = hex("2a2510ce829bba8734180032120a04084f10011204086410021a04086410023a06080110011800080710151dea71de7e2590821ee6")

    /** `CAP-050` frame 1163: `2:<ms> 3:0 6:{2:{1:37 2:1} 3:{1:45 2:1}} 7:{1:0 2:0 3:0}` — no Case entry. */
    private val withoutCase = hex("2a1f10a584ae8a8a341800320c1204082510011a04082d10013a06080010001800080710151dea71de7e2590821ee6")

    private fun packet(bytes: ByteArray) = (PwRpc.decode(bytes) as io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult.Success).value

    @Test
    @DisplayName("the request is byte-identical to the official app's CAP-036 frame 1410 (channel 21, address 00 4b)")
    fun `request matches CAP-036 frame 1410`() {
        val wire = Hdlc.encode(4736, PW_HDLC_CONTROL_UI, PwRpc.encode(Maestro.subscribeRuntimeInfoRequest(21)))
        assertEquals("7e004b0310151dea71de7d5e2590821ee66654bfab7e", wire.joinToString("") { "%02x".format(it) })
    }

    @Test
    @DisplayName("CAP-041 frame 782: entry 6.1 = Case 79 % (a 64-bit timestamp in field 2 must not make the packet unreadable)")
    fun `stream packet with the Case entry`() {
        assertEquals(BatteryLevel.Known(79, null, false), RuntimeInfoDecoder.caseBattery(packet(withCase).payload))
        val routed = CodecRouter().feed(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, withCase), timestampMillis = 0)
        assertEquals(RoutedFrame.RuntimeInfoCase(BatteryLevel.Known(79, null, false)), routed.single())
    }

    @Test
    @DisplayName("CAP-050 frame 1163: no entry 6.1 → Case unavailable (never carried over, AGENTS.md §5)")
    fun `stream packet without the Case entry`() {
        assertEquals(BatteryLevel.Unavailable, RuntimeInfoDecoder.caseBattery(packet(withoutCase).payload))
    }

    @Test
    fun `a payload of another shape is not a Case reading`() {
        assertNull(RuntimeInfoDecoder.caseBattery(ByteArray(0)))
        assertNull(RuntimeInfoDecoder.caseBattery(hex("0b0c")))
    }

    @Test
    fun `64-bit varints decode (CAP-041 frame 782's timestamp field)`() {
        assertEquals(1788707520846L to 6, Varint.decodeLong(hex("ce829bba8734"), 0))
        assertNull(Varint.decodeLong(hex("ffffffffffffffffffff01"), 0), "more than 10 bytes")
    }

    @Test
    @DisplayName("fuzz (AGENTS.md §11): random, truncated and mutated payloads never throw")
    fun `decoder never throws`() {
        val random = Random(46)
        repeat(3_000) { RuntimeInfoDecoder.caseBattery(ByteArray(random.nextInt(0, 80)).also(random::nextBytes)) }
        val payload = packet(withCase).payload
        for (cut in payload.indices) RuntimeInfoDecoder.caseBattery(payload.copyOfRange(0, cut))
        repeat(1_000) {
            RuntimeInfoDecoder.caseBattery(payload.copyOf().also { b -> b[random.nextInt(b.size)] = random.nextInt(256).toByte() })
        }
    }
}
