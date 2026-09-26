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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Random

/** pw_rpc packet codec and the Maestro constants (DECISIONS.md ADR-034). Pure bytes in/out (AGENTS.md §11). */
class PwRpcTest {

    @Test
    @DisplayName("the 65599 name hashes equal the ids seen on the wire (CAP-015 frame 2165, CAP-036 frame 1523, DESKRESEARCH 2026-09-19)")
    fun `name hashes equal the wire ids`() {
        assertEquals(0x7ede71ea, Maestro.SERVICE_ID)
        assertEquals(0x9e8c9a1d.toInt(), Maestro.METHOD_WRITE_SETTING)
        assertEquals(0xaed0ae51.toInt(), Maestro.METHOD_READ_SETTING)
        assertEquals(0x2821adf5, Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES)
        assertEquals(0x7199fa44, Maestro.METHOD_GET_SOFTWARE_INFO)
        // The connect-time burst (PROTOCOL.md §6, CAP-036 frames 1404–1570), names for the debug log only.
        assertEquals(0x28eca5e3, Maestro.METHOD_GET_HARDWARE_INFO)
        assertEquals(0xe61e8290.toInt(), Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO)
        assertEquals(0x673bed4e, Maestro.METHOD_SET_WALLCLOCK)
        assertEquals("GetHardwareInfo", PwRpc.methodName(Maestro.SERVICE_ID, 0x28eca5e3))
        assertEquals("Maestro/0x12345678", PwRpc.methodName(Maestro.SERVICE_ID, 0x12345678))
    }

    @Test
    @DisplayName("ReadSetting 4:16 on channel 21 is byte-identical to CAP-036 frame 1523 (RpcPacket bytes and the whole wire frame)")
    fun `read request matches the real capture`() {
        val request = Maestro.readSettingRequest(channelId = 21, field = Maestro.FIELD_EQ_ACTIVE)!!
        val bytes = PwRpc.encode(request)
        assertEquals("10151dea71de7e2551aed0ae2a022010", bytes.toHex())
        val wire = Hdlc.encode(4736, PW_HDLC_CONTROL_UI, bytes) // address 00 4b
        assertEquals("7e004b0310151dea71de7d5e2551aed0ae2a02201047eeadcf7e", wire.toHex())
        val saved = PwRpc.encode(Maestro.readSettingRequest(21, Maestro.FIELD_EQ_SAVED)!!)
        assertEquals("7e004b0310151dea71de7d5e2551aed0ae2a0220126b8fa3217e", Hdlc.encode(4736, PW_HDLC_CONTROL_UI, saved).toHex()) // frame 1529
    }

    @Test
    @DisplayName("only the fields ADR-034/036 unblock and ai-sessions/0052 reads can be read — never 12, no arbitrary setting read exists")
    fun `refuses to build a read for any other field`() {
        for (field in listOf(0, 1, 11, 12, 15, 27, 28, 29, 31, 200)) assertNull(Maestro.readSettingRequest(21, field), "field $field")
        assertEquals(setOf(2, 4, 7, 16, 17, 18, 19, 22), Maestro.READABLE_FIELDS)
    }

    @Test
    @DisplayName("decode(): the real unsolicited GetSoftwareInfo header — RESPONSE, channel 21, call_id 0xFFFFFFFF (frame 1405), channel 24 (CAP-007 frame 792)")
    fun `decodes the announcement header`() {
        for ((channel, hexTail) in listOf(21 to "08011015", 24 to "08011018")) {
            val packet = (PwRpc.decode(hex(hexTail + "1dea71de7e2544fa997138ffffffff0f")) as BudsResult.Success).value
            assertEquals(PwRpc.TYPE_RESPONSE, packet.type)
            assertEquals(channel, packet.channelId)
            assertEquals(Maestro.SERVICE_ID, packet.serviceId)
            assertEquals(Maestro.METHOD_GET_SOFTWARE_INFO, packet.methodId)
            assertEquals(PwRpc.CALL_ID_UNSOLICITED, packet.callId)
            assertEquals("pw_rpc RESPONSE ch=$channel method=GetSoftwareInfo status=OK", packet.summary())
        }
    }

    @Test
    fun `encode then decode round-trips every field`() {
        val packet = RpcPacket(PwRpc.TYPE_SERVER_ERROR, 300, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, byteArrayOf(1, 2, 3), status = 5, callId = 4_000_000_000L)
        assertEquals(packet, (PwRpc.decode(PwRpc.encode(packet)) as BudsResult.Success).value)
    }

    @Test
    fun `an absent status reads as OK and names of the seen statuses are stable`() {
        assertEquals("OK", PwRpc.statusName(null))
        assertEquals("NOT_FOUND", PwRpc.statusName(5))
        assertEquals("UNKNOWN", PwRpc.statusName(2))
        assertEquals("REQUEST", PwRpc.typeName(0))
    }

    @Test
    fun `structural failures are MalformedFrame, never thrown`() {
        val bad = listOf(
            "", "08", "1d", "10", "1dea71de", "2a05aabb", "0100", "ffffffffffffffffffff", "0801" /* no service/method */,
        )
        for (h in bad) {
            val result = PwRpc.decode(hex(h))
            assertInstanceOf(BudsResult.Failure::class.java, result, h)
            assertInstanceOf(BudsError.MalformedFrame::class.java, (result as BudsResult.Failure).error, h)
        }
    }

    @Test
    @DisplayName("fuzz: random and mutated packets never throw (AGENTS.md §11)")
    fun `never throws on arbitrary input`() {
        val random = Random(41)
        repeat(6_000) { PwRpc.decode(ByteArray(random.nextInt(40)).also(random::nextBytes)) }
        val valid = PwRpc.encode(Maestro.readSettingRequest(21, 16)!!)
        repeat(3_000) {
            val mutated = valid.copyOf().also { it[random.nextInt(it.size)] = random.nextInt(256).toByte() }
            PwRpc.decode(mutated)
        }
        repeat(2_000) { PwRpc.decode(valid.copyOf(random.nextInt(valid.size))) } // truncations
    }

    @Test
    @DisplayName("MaestroChannel: the four channel/address pairs seen in the captures; anything else has no address (never guessed)")
    fun `channel address table`() {
        assertEquals("003b", Hdlc.encodeAddress(MaestroChannel.forChannel(19)!!.requestAddress).toHex())
        assertEquals("004b", Hdlc.encodeAddress(MaestroChannel.forChannel(21)!!.requestAddress).toHex())
        assertEquals("803d", Hdlc.encodeAddress(MaestroChannel.forChannel(24)!!.requestAddress).toHex())
        assertEquals("804d", Hdlc.encodeAddress(MaestroChannel.forChannel(26)!!.requestAddress).toHex())
        for (unknown in listOf(0, 1, 20, 22, 25, 27, 255)) assertNull(MaestroChannel.forChannel(unknown), "channel $unknown")
        assertTrue(MaestroChannel.forChannel(0) == null) // the old default channel of the codec — never valid
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
