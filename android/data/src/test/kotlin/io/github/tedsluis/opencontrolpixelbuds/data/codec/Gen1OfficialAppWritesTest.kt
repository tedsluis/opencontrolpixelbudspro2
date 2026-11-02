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

import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList
import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Gen 1 support: every write this app sends to a Gen 1 against the official Pixel Buds app's own write to a Gen 1 (`CAP-081`), whole frame incl. the CRC.
 * The official app writes on channel 21; the app uses whichever channel the Buds announce, so the channel-21 frame is the comparable one.
 */
class Gen1OfficialAppWritesTest {

    private fun wire(packet: RpcPacket): String {
        val address = MaestroChannel.forChannel(packet.channelId)!!.requestAddress
        return Hdlc.encode(address, PW_HDLC_CONTROL_UI, PwRpc.encode(packet)).toHex()
    }

    private fun wireEq(frame: EqFrame): String {
        val address = MaestroChannel.forChannel(frame.channelId)!!.requestAddress
        return Hdlc.encode(address, PW_HDLC_CONTROL_UI, EqFrameEncoder.encode(frame)).toHex()
    }

    @Test
    @DisplayName("flags 2, 4, 15, 19, 22 on channel 21: CAP-081 5014/5070, 1765/1786, 2681/2692, 3189/3171, 2035/2012")
    fun flags() {
        assertEquals(Cap081.IN_EAR_OFF_5014, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_IN_EAR_DETECTION, false)!!))
        assertEquals(Cap081.IN_EAR_ON_5070, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_IN_EAR_DETECTION, true)!!))
        assertEquals(Cap081.TOUCH_OFF_1765, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_TOUCH_CONTROLS, false)!!))
        assertEquals(Cap081.TOUCH_ON_1786, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_TOUCH_CONTROLS, true)!!))
        assertEquals(Cap081.VOLUME_EQ_OFF_2681, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_VOLUME_EQ, false)!!))
        assertEquals(Cap081.VOLUME_EQ_ON_2692, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_VOLUME_EQ, true)!!))
        assertEquals(Cap081.MONO_OFF_3189, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_MONO_AUDIO, false)!!))
        assertEquals(Cap081.MONO_ON_3171, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_MONO_AUDIO, true)!!))
        assertEquals(Cap081.CONVERSATION_OFF_2035, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_CONVERSATION_DETECTION, false)!!))
        assertEquals(Cap081.CONVERSATION_ON_2012, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_CONVERSATION_DETECTION, true)!!))
    }

    @Test
    @DisplayName("press-and-hold, one bud per write: CAP-081 1848 / 1885 / 1924 / 1939")
    fun pressAndHold() {
        assertEquals(Cap081.HOLD_LEFT_ANC_1848, wire(SettingsCodec.pressAndHoldRequest(21, Bud.LEFT, HoldAction.NOISE_CONTROL)))
        assertEquals(Cap081.HOLD_LEFT_ASSISTANT_1885, wire(SettingsCodec.pressAndHoldRequest(21, Bud.LEFT, HoldAction.ASSISTANT)))
        assertEquals(Cap081.HOLD_RIGHT_ASSISTANT_1924, wire(SettingsCodec.pressAndHoldRequest(21, Bud.RIGHT, HoldAction.ASSISTANT)))
        assertEquals(Cap081.HOLD_RIGHT_ANC_1939, wire(SettingsCodec.pressAndHoldRequest(21, Bud.RIGHT, HoldAction.NOISE_CONTROL)))
    }

    @Test
    @DisplayName("the hold list as three booleans, never a fourth: CAP-081 5200 / 5205 / 5280")
    fun ancModeList() {
        fun list(off: Boolean, transparency: Boolean) = AncModeList(noiseCancellation = true, off = off, transparency = transparency, adaptive = false)
        assertEquals(Cap081.LIST_ALL_THREE_5200, wire(SettingsCodec.ancModeListRequest(21, list(off = true, transparency = true), withAdaptive = false)!!))
        assertEquals(Cap081.LIST_NC_TRANSPARENCY_5205, wire(SettingsCodec.ancModeListRequest(21, list(off = false, transparency = true), withAdaptive = false)!!))
        assertEquals(Cap081.LIST_NC_OFF_5280, wire(SettingsCodec.ancModeListRequest(21, list(off = true, transparency = false), withAdaptive = false)!!))
    }

    @Test
    @DisplayName("balance -20 / +20 / 0: CAP-081 3114 / 3131 / 3159")
    fun balance() {
        assertEquals(Cap081.BALANCE_MINUS_20_3114, wire(SettingsCodec.balanceRequest(21, -20)))
        assertEquals(Cap081.BALANCE_PLUS_20_3131, wire(SettingsCodec.balanceRequest(21, 20)))
        assertEquals(Cap081.BALANCE_0_3159, wire(SettingsCodec.balanceRequest(21, 0)))
    }

    @Test
    @DisplayName("EQ: live field 16 (CAP-081 2129) and the saved field 18 (CAP-081 2825), the official app's exact float bits")
    fun eq() {
        val live = EqBandGains(upperTreble = Float.fromBits(0x3fa66668), treble = 0f, mid = 0f, bass = 0f, lowBass = 0f)
        assertEquals(Cap081.EQ_LIVE_2129, wireEq(EqFrame(live, persist = false, channelId = 21)))
        val saved = EqBandGains(
            lowBass = Float.fromBits(0x3fcccccc),
            bass = Float.fromBits(0x3fc00000),
            mid = Float.fromBits(0x3fd99998),
            treble = Float.fromBits(0x3fc00000),
            upperTreble = Float.fromBits(0x3f999998),
        )
        assertEquals(Cap081.EQ_SAVED_2825, wireEq(EqFrame(saved, persist = true, channelId = 21)))
    }

    @Test
    @DisplayName("Find on the Message Stream: ring left 1416, ring right 1491, stop 1477 (CAP-081, DLCI 0x04)")
    fun find() {
        assertEquals(Cap081.RING_LEFT_1416, RingFrameEncoder.encode(RingFrame.Start(RingTarget.LEFT)).toHex())
        assertEquals(Cap081.RING_RIGHT_1491, RingFrameEncoder.encode(RingFrame.Start(RingTarget.RIGHT)).toHex())
        assertEquals(Cap081.RING_STOP_1477, RingFrameEncoder.encode(RingFrame.Stop).toHex())
    }
}

/**
 * Phone → Buds frames of the official Pixel Buds app (1.0.996096625, Android 13, Pixel 4 XL) writing to a Gen 1 on `release_5.11_signed`, from
 * `CAP-081-btsnoop_hci.log`, extracted with `tshark -r CAP-081-btsnoop_hci.log -Y "frame.number in {…}" -T fields -e frame.number -e data.data`
 * (DLCI 0x02 frames: the whole pw_hdlc frame; DLCI 0x04: the Message Stream message). No device identifier in any of them.
 */
internal object Cap081 {
    const val TOUCH_OFF_1765 = "7e004b0310151dea71de7d5e251d9a8c9e2a042202200053908d4b7e"
    const val TOUCH_ON_1786 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422022001c5a08a3c7e"
    const val HOLD_LEFT_ANC_1848 = "7e004b0310151dea71de7d5e251d9a8c9e2a0a22083a060a04220208059601c6257e"
    const val HOLD_LEFT_ASSISTANT_1885 = "7e004b0310151dea71de7d5e251d9a8c9e2a0a22083a060a04220208062c50cfbc7e"
    const val HOLD_RIGHT_ASSISTANT_1924 = "7e004b0310151dea71de7d5e251d9a8c9e2a0a22083a06120422020806dad04a537e"
    const val HOLD_RIGHT_ANC_1939 = "7e004b0310151dea71de7d5e251d9a8c9e2a0a22083a06120422020805608143ca7e"
    const val CONVERSATION_ON_2012 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203b00101b46fc4c07e"
    const val CONVERSATION_OFF_2035 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203b00100225fc3b77e"
    const val EQ_LIVE_2129 = "7e004b0310151dea71de7d5e251d9a8c9e2a1e221c8201190d0000000015000000001d0000000025000000002d6866a63ff114ff0b7e"
    const val VOLUME_EQ_OFF_2681 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220278000f47ef397e"
    const val VOLUME_EQ_ON_2692 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220278019977e84e7e"
    const val EQ_SAVED_2825 = "7e004b0310151dea71de7d5e251d9a8c9e2a1e221c9201190dcccccc3f150000c03f1d9899d93f250000c03f2d9899993f9294c3c07e"
    const val BALANCE_MINUS_20_3114 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203880127615eb1387e"
    const val BALANCE_PLUS_20_3131 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203880128f0430ea87e"
    const val BALANCE_0_3159 = "7e004b0310151dea71de7d5e251d9a8c9e2a0522038801000aebbb9d7e"
    const val MONO_ON_3171 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203980101ec789af67e"
    const val MONO_OFF_3189 = "7e004b0310151dea71de7d5e251d9a8c9e2a0522039801007a489d817e"
    const val IN_EAR_OFF_5014 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422021000a0a6cb947e"
    const val IN_EAR_ON_5070 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220210013696cce37e"
    const val LIST_ALL_THREE_5200 = "7e004b0310151dea71de7d5e251d9a8c9e2a0a22086206080110011801a59fe6337e"
    const val LIST_NC_TRANSPARENCY_5205 = "7e004b0310151dea71de7d5e251d9a8c9e2a0a2208620608011000180192f524327e"
    const val LIST_NC_OFF_5280 = "7e004b0310151dea71de7d5e251d9a8c9e2a0a2208620608011001180033afe1447e"

    const val RING_LEFT_1416 = "0401000102"
    const val RING_STOP_1477 = "0401000100"
    const val RING_RIGHT_1491 = "0401000101"
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
