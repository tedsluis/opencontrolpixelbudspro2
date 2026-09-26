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

import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** ADR-036 reads and ADR-045 writes against the real frames of [Settings036] / [SettingsWrites] (`ai-sessions/0052`). */
class SettingsCodecTest {

    /** The whole pw_hdlc frame (address, control, flags, escapes, CRC) for a request on [channelId], as the app would put it on the wire. */
    private fun wire(packet: RpcPacket): String {
        val address = MaestroChannel.forChannel(packet.channelId)!!.requestAddress
        return Hdlc.encode(address, PW_HDLC_CONTROL_UI, PwRpc.encode(packet)).toHex()
    }

    /** Hdlc → pw_rpc → the router's Maestro classification, the path every inbound frame takes. */
    private fun route(frameHex: String): RoutedFrame? {
        val hdlc = (Hdlc.decode(hex(frameHex)) as BudsResult.Success).value
        val rpc = (PwRpc.decode(hdlc.payload) as BudsResult.Success).value
        return routeMaestro(rpc)
    }

    // ---- writes: byte-identical to the official app's, whole frame incl. CRC ----

    @Test
    @DisplayName("conversation detection: CAP-019 1720 (4:{22:0}) and 1808 (4:{22:1}) on channel 21")
    fun conversationDetection() {
        assertEquals(SettingsWrites.CONV_OFF_1720, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_CONVERSATION_DETECTION, false)!!))
        assertEquals(SettingsWrites.CONV_ON_1808, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_CONVERSATION_DETECTION, true)!!))
    }

    @Test
    @DisplayName("touch controls: CAP-020 1741 (4:{4:1}) and 1995 (4:{4:0}) on channel 21")
    fun touchControls() {
        assertEquals(SettingsWrites.TOUCH_ON_1741, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_TOUCH_CONTROLS, true)!!))
        assertEquals(SettingsWrites.TOUCH_OFF_1995, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_TOUCH_CONTROLS, false)!!))
    }

    @Test
    @DisplayName("mono: CAP-022 1621 (4:{19:1}) and 1823 (4:{19:0}) on channel 19")
    fun mono() {
        assertEquals(SettingsWrites.MONO_ON_1621, wire(SettingsCodec.flagRequest(19, SettingsCodec.FIELD_MONO_AUDIO, true)!!))
        assertEquals(SettingsWrites.MONO_OFF_1823, wire(SettingsCodec.flagRequest(19, SettingsCodec.FIELD_MONO_AUDIO, false)!!))
    }

    @Test
    @DisplayName("balance: the seven CAP-022 drag writes 1922 … 2099 (zigzag, channel 19), −100 … +100")
    fun balance() {
        for ((frame, value) in SettingsWrites.BALANCE_DRAG) assertEquals(frame, wire(SettingsCodec.balanceRequest(19, value)), "value $value")
        assertEquals(SettingsWrites.BALANCE_DRAG[0].first, wire(SettingsCodec.balanceRequest(19, -250)), "clamped to −100 (ADR-026)")
    }

    @Test
    @DisplayName("press-and-hold: CAP-021 1895 / 3619 / 4315 / 4976 — Left/Right × Assistant/ANC, channel 19")
    fun pressAndHold() {
        assertEquals(SettingsWrites.HOLD_LEFT_ASSISTANT_1895, wire(SettingsCodec.pressAndHoldRequest(19, Bud.LEFT, HoldAction.ASSISTANT)))
        assertEquals(SettingsWrites.HOLD_RIGHT_ASSISTANT_3619, wire(SettingsCodec.pressAndHoldRequest(19, Bud.RIGHT, HoldAction.ASSISTANT)))
        assertEquals(SettingsWrites.HOLD_LEFT_ANC_4315, wire(SettingsCodec.pressAndHoldRequest(19, Bud.LEFT, HoldAction.NOISE_CONTROL)))
        assertEquals(SettingsWrites.HOLD_RIGHT_ANC_4976, wire(SettingsCodec.pressAndHoldRequest(19, Bud.RIGHT, HoldAction.NOISE_CONTROL)))
    }

    @Test
    fun `nothing but the ADR-045 flag fields can be written - not 2, not 12`() {
        assertNull(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_IN_EAR_DETECTION, true))
        assertNull(SettingsCodec.flagRequest(21, 12, true))
        assertNull(SettingsCodec.flagRequest(21, 11, true))
    }

    // ---- reads ----

    @Test
    @DisplayName("ReadSetting requests byte-identical to CAP-036 1445/1451/1457/1526/1532/1538 (channel 21); field 12 is never readable")
    fun readRequests() {
        val expected = mapOf(
            2 to Settings036.READ_2_REQ, 4 to Settings036.READ_4_REQ, 7 to Settings036.READ_7_REQ,
            17 to Settings036.READ_17_REQ, 19 to Settings036.READ_19_REQ, 22 to Settings036.READ_22_REQ,
        )
        for ((field, frame) in expected) assertEquals(frame, wire(Maestro.readSettingRequest(21, field)!!), "field $field")
        assertNull(Maestro.readSettingRequest(21, 12))
        assertNull(Maestro.readSettingRequest(21, 11))
    }

    @Test
    @DisplayName("the real CAP-036 answers decode to their values through the router (1447, 1453, 1462, 1528, 1534, 1540)")
    fun readAnswers() {
        assertEquals(RoutedFrame.Setting(SettingValue.Flag(2, true)), route(Settings036.READ_2_RESP))
        assertEquals(RoutedFrame.Setting(SettingValue.Flag(4, true)), route(Settings036.READ_4_RESP))
        assertEquals(
            RoutedFrame.Setting(SettingValue.PressAndHold(HoldAction.NOISE_CONTROL, HoldAction.NOISE_CONTROL)),
            route(Settings036.READ_7_RESP),
        )
        assertEquals(RoutedFrame.Setting(SettingValue.Balance(5)), route(Settings036.READ_17_RESP), "zigzag 10 = +5")
        assertEquals(RoutedFrame.Setting(SettingValue.Flag(19, false)), route(Settings036.READ_19_RESP))
        assertEquals(RoutedFrame.Setting(SettingValue.Flag(22, true)), route(Settings036.READ_22_RESP))
    }

    @Test
    fun `the EQ answer of the same sweep is still an EQ frame, not a setting`() {
        assertEquals(true, route(Settings036.READ_16_RESP) is RoutedFrame.Eq)
    }

    @Test
    fun `a read the Buds reject (CAP-036 frame 1441, status UNKNOWN) is an error result, not a value`() {
        val r = route(Settings036.READ_REJECTED_1441) as RoutedFrame.RpcResult
        assertEquals(Maestro.METHOD_READ_SETTING, r.methodId)
        assertEquals(false, r.isOk)
    }

    @Test
    fun `the Buds' empty RESPONSE to a write (CAP-019 1731, CAP-022 1629) is an OK WriteSetting result`() {
        for (ack in listOf(SettingsWrites.ACK_CH21_1731, SettingsWrites.ACK_CH19_1629)) {
            val r = route(ack) as RoutedFrame.RpcResult
            assertEquals(Maestro.METHOD_WRITE_SETTING, r.methodId)
            assertEquals(true, r.isOk)
        }
    }

    @Test
    @DisplayName("the official app's own write payloads decode back to the value (one bud per press-and-hold write)")
    fun writePayloadsDecode() {
        fun payload(frame: String) = (PwRpc.decode((Hdlc.decode(hex(frame)) as BudsResult.Success).value.payload) as BudsResult.Success).value.payload
        assertEquals(SettingValue.Flag(22, false), SettingsCodec.decode(payload(SettingsWrites.CONV_OFF_1720)))
        assertEquals(SettingValue.PressAndHold(HoldAction.ASSISTANT, null), SettingsCodec.decode(payload(SettingsWrites.HOLD_LEFT_ASSISTANT_1895)))
        assertEquals(SettingValue.PressAndHold(null, HoldAction.NOISE_CONTROL), SettingsCodec.decode(payload(SettingsWrites.HOLD_RIGHT_ANC_4976)))
        for ((frame, value) in SettingsWrites.BALANCE_DRAG) assertEquals(SettingValue.Balance(value), SettingsCodec.decode(payload(frame)))
    }

    @Test
    @DisplayName("supplementary structural (hand-built, labelled): other values/shapes are not interpreted")
    fun notInterpreted() {
        assertNull(SettingsCodec.decode(hex("2203b00102")), "22:2 is not a flag value")
        assertNull(SettingsCodec.decode(hex("22026001")), "field 12 as a varint")
        assertNull(SettingsCodec.decode(hex("22048801ca01")), "17:202 = +101 is outside ±100 (ADR-026)")
        assertNull(SettingsCodec.decode(ByteArray(0)), "empty payload")
        assertNull(SettingsCodec.decode(hex("22083a060a04220208075a")), "a press-and-hold value 7 (not 5/6) with a trailing byte")
        assertNull(SettingsCodec.decode(hex("2206220488019701ff")), "trailing garbage")
    }

    @Test
    @DisplayName("fuzz (AGENTS.md §11): random, truncated and mutated setting payloads and frames never throw")
    fun fuzz() {
        val random = java.util.Random(52)
        val frames = Settings036.ANSWERS.values + SettingsWrites.BALANCE_DRAG.map { it.first } + listOf(
            SettingsWrites.CONV_OFF_1720, SettingsWrites.TOUCH_OFF_1995, SettingsWrites.HOLD_LEFT_ANC_4315, SettingsWrites.MONO_ON_1621,
            Settings036.READ_REJECTED_1441,
        )
        repeat(3_000) {
            val bytes = ByteArray(random.nextInt(40)).also(random::nextBytes)
            SettingsCodec.decode(bytes)
        }
        for (f in frames) {
            val raw = hex(f)
            for (cut in 0..raw.size) {
                val part = raw.copyOf(cut)
                CodecRouter().feed(Dlci.MAESTRO, part, 0L)
                SettingsCodec.decode(part)
            }
            val payload = (Hdlc.decode(raw) as BudsResult.Success).value.payload
            repeat(300) {
                val mutated = payload.copyOf().also { b -> b[random.nextInt(b.size)] = random.nextInt(256).toByte() }
                SettingsCodec.decode(mutated)
                PwRpc.decode(mutated).let { r -> if (r is BudsResult.Success) routeMaestro(r.value) }
                CodecRouter().feed(Dlci.MAESTRO, Hdlc.encode(0x2880, PW_HDLC_CONTROL_UI, mutated), 0L)
            }
        }
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
