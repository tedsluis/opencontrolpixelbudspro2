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
package io.github.tedsluis.opencontrolpixelbuds.data.experiment

import io.github.tedsluis.opencontrolpixelbuds.data.codec.Hdlc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Maestro
import io.github.tedsluis.opencontrolpixelbuds.data.codec.MaestroChannel
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RpcPacket
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * Round 1's experiment frames (`ai-sessions/0096`). Real bytes where a capture holds them (the envelope control, the Ring frames, the ACK); a frame no
 * capture holds yet is a **labelled structural test** (`AGENTS.md` §11) — its expected bytes come from the same envelope the controls pin, and `CAP-078` will
 * replace them by the frames on the wire.
 */
class ExperimentFramesTest {

    private fun wire(packet: RpcPacket, channel: Int): String {
        val ch = MaestroChannel.forChannel(channel)!!
        return Hdlc.encode(ch.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(packet)).joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    private fun bytes(s: String) = s.replace(" ", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun `control - the envelope reproduces CAP-077 frames byte for byte`() {
        // CAP-077-btsnoop_hci.log, ReadSetting 4:2 on channel 21 and 19 (S3 1791, S4 2228) — real bytes.
        assertEquals("7e004b0310151dea71de7d5e2551aed0ae2a0220020f9f143c7e", wire(Maestro.readSettingRequest(21, 2)!!, 21))
        assertEquals("7e003b0310131dea71de7d5e2551aed0ae2a022002b5df86d77e", wire(Maestro.readSettingRequest(19, 2)!!, 19))
    }

    @Test
    fun `structural - WriteSetting 4 13 n on both channels`() {
        val expected19 = listOf(
            "7e003b0310131dea71de7d5e251d9a8c9e2a0422026801f889dc6c7e", "7e003b0310131dea71de7d5e251d9a8c9e2a042202680242d8d5f57e",
            "7e003b0310131dea71de7d5e251d9a8c9e2a0422026803d4e8d2827e", "7e003b0310131dea71de7d5e251d9a8c9e2a0422026804777d5db61c7e",
        )
        val expected21 = listOf(
            "7e004b0310151dea71de7d5e251d9a8c9e2a0422026801c8652a047e", "7e004b0310151dea71de7d5e251d9a8c9e2a04220268027234239d7e",
            "7e004b0310151dea71de7d5e251d9a8c9e2a0422026803e40424ea7e", "7e004b0310151dea71de7d5e251d9a8c9e2a0422026804479140747e",
        )
        (1..4).forEach { n ->
            assertEquals(expected19[n - 1], wire(ExperimentFrames.writeAncState(19, n), 19))
            assertEquals(expected21[n - 1], wire(ExperimentFrames.writeAncState(21, n), 21))
        }
        assertThrows(IllegalArgumentException::class.java) { ExperimentFrames.writeAncState(19, 0) }
        assertThrows(IllegalArgumentException::class.java) { ExperimentFrames.writeAncState(19, 5) }
    }

    @Test
    fun `structural - ReadSetting 8 9 10 and SubscribeToOobeActions`() {
        assertEquals("7e003b0310131dea71de7d5e2551aed0ae2a022008ab3653377e", wire(ExperimentFrames.readSetting(19, 8), 19))
        assertEquals("7e004b0310151dea71de7d5e2551aed0ae2a0220098746c6ab7e", wire(ExperimentFrames.readSetting(21, 9), 21))
        assertEquals("7e003b0310131dea71de7d5e2551aed0ae2a02200a87575dd97e", wire(ExperimentFrames.readSetting(19, 10), 19))
        assertThrows(IllegalArgumentException::class.java) { ExperimentFrames.readSetting(19, 3) }
        assertThrows(IllegalArgumentException::class.java) { ExperimentFrames.readSetting(19, 6) }
        assertEquals(0x57f3becb, ExperimentFrames.METHOD_SUBSCRIBE_TO_OOBE_ACTIONS)
        assertEquals("7e003b0310131dea71de7d5e25cbbef3578cb7aecf7e", wire(ExperimentFrames.subscribeToOobeActions(19), 19))
        assertEquals("7e004b0310151dea71de7d5e25cbbef3578ace74cd7e", wire(ExperimentFrames.subscribeToOobeActions(21), 21))
    }

    @Test
    fun `the Ring frames and the status ACK equal CAP-025`() {
        // CAP-025-btsnoop_hci.log: 2040 Ring Right, 2131 Ring Left, 2048 the official app's ACK of the Buds' status — real bytes.
        assertArrayEquals(bytes("04 01 00 01 01"), ExperimentFrames.ringStart(RingTarget.RIGHT))
        assertArrayEquals(bytes("04 01 00 01 02"), ExperimentFrames.ringStart(RingTarget.LEFT))
        assertArrayEquals(bytes("ff 01 00 02 04 01"), ExperimentFrames.RING_STATUS_ACK)
        // Structural (no capture holds it): Ring both, the Device Action spec's 0x03.
        assertArrayEquals(bytes("04 01 00 01 03"), ExperimentFrames.RING_BOTH)
    }

    @Test
    fun `messages splits a payload with several messages`() {
        // CAP-077 export 07:07:59.640: two battery messages in one RFCOMM frame — real bytes.
        val two = ExperimentFrames.messages(bytes("03 03 00 03 64 64 ff 03 03 00 03 64 64 ff"))
        assertEquals(2, two.size)
        assertEquals(0x03, two[1].first)
        // CAP-025 2044 + 2045 as one payload: the Buds' ACK, then their ring status.
        val ring = ExperimentFrames.messages(bytes("ff 01 00 03 04 01 00 04 01 00 01 01"))
        assertEquals(listOf(0xff, 0x04), ring.map { it.first })
        assertArrayEquals(bytes("01"), ring[1].third)
    }

    @Test
    fun `messages never throws on hostile input`() {
        val random = Random(96)
        repeat(20_000) {
            val b = ByteArray(random.nextInt(0, 64)).also { random.nextBytes(it) }
            ExperimentFrames.messages(b)
        }
        assertEquals(emptyList<Triple<Int, Int, ByteArray>>(), ExperimentFrames.messages(bytes("04 01 ff ff 00")))
    }
}
