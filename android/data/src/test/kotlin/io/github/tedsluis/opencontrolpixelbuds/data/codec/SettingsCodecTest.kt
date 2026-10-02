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
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList
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

    /** The `RpcPacket` payload of a whole pw_hdlc frame (what [SettingsCodec.decode] reads). */
    private fun rpcPayload(frameHex: String): ByteArray =
        (PwRpc.decode((Hdlc.decode(hex(frameHex)) as BudsResult.Success).value.payload) as BudsResult.Success).value.payload

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
    @DisplayName("balance, ai-sessions/0064 F-2: CAP-066 A7723 (Right 6) / A7873 (Centre) on channel 19, CAP-064 6671 (Right 4 = 17:7) on channel 21, byte for byte")
    fun balanceStepTargets() {
        assertEquals(Cap066Balance.RIGHT_6_A7723, wire(SettingsCodec.balanceRequest(19, -6)))
        assertEquals(Cap066Balance.CENTRE_A7873, wire(SettingsCodec.balanceRequest(19, 0)))
        assertEquals(Cap066Balance.RIGHT_4_CH21_6671, wire(SettingsCodec.balanceRequest(21, -4)), "Right 4, the CAP-066 BB-10 target")
        assertEquals(SettingValue.Balance(-4), SettingsCodec.decode(rpcPayload(Cap066Balance.RIGHT_4_CH21_6671)))
    }

    @Test
    @DisplayName("balance, supplementary (derived, not captured): Right 4 on channel 19 = A7723's header and field with the value 07 instead of 0b")
    fun balanceRight4OnChannel19Derived() {
        // No channel-19 `17:7` write exists in any capture (Cap066Balance.RIGHT_4_CH21_6671's comment); this checks the derived frame's structure only.
        val derived = wire(SettingsCodec.balanceRequest(19, -4))
        val header = Cap066Balance.RIGHT_6_A7723.substringBefore("88010b")
        assertEquals(header + "880107", derived.substring(0, header.length + 6), "same pw_hdlc address, channel, service, method and field 17 tag")
        assertEquals(SettingValue.Balance(-4), SettingsCodec.decode(rpcPayload(derived)))
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
    fun `only the ADR-045 and ADR-047 flag fields can be written as a flag - not 12, not 11`() {
        assertEquals(setOf(2, 4, 19, 22), SettingsCodec.WRITABLE_FLAG_FIELDS)
        assertNull(SettingsCodec.flagRequest(21, 12, true))
        assertNull(SettingsCodec.flagRequest(21, 11, true))
    }

    // ---- field 2, the "In-ear detection" switch (ADR-047, ai-sessions/0056) ----

    @Test
    @DisplayName("in-ear detection: CAP-056 2173 (4:{2:1}) / 4048 (4:{2:0}) on channel 19 and 3627 / 2849 on channel 21, byte for byte")
    fun inEarDetectionWrites() {
        assertEquals(Settings056.WRITE_INEAR_ON_2173, wire(SettingsCodec.flagRequest(19, SettingsCodec.FIELD_IN_EAR_DETECTION, true)!!))
        assertEquals(Settings056.WRITE_INEAR_OFF_4048, wire(SettingsCodec.flagRequest(19, SettingsCodec.FIELD_IN_EAR_DETECTION, false)!!))
        assertEquals(Settings056.WRITE_INEAR_ON_CH21_3627, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_IN_EAR_DETECTION, true)!!))
        assertEquals(Settings056.WRITE_INEAR_OFF_CH21_2849, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_IN_EAR_DETECTION, false)!!))
    }

    @Test
    fun `the CAP-056 read 1502 decodes to in-ear detection off, and its OK responses 2179 (= 1697) and 2855 are OK results`() {
        assertEquals(RoutedFrame.Setting(SettingValue.Flag(2, false)), route(Settings056.READ_2_RESP_OFF_1502))
        for (ack in listOf(Settings056.ACK_CH19_1697, Settings056.ACK_CH21_2855)) {
            val r = route(ack) as RoutedFrame.RpcResult
            assertEquals(Maestro.METHOD_WRITE_SETTING, r.methodId)
            assertEquals(true, r.isOk)
        }
    }

    // ---- field 12, the press-and-hold ANC-mode list (ADR-046, ai-sessions/0056) ----

    private val all = AncModeList(noiseCancellation = true, off = true, transparency = true, adaptive = true)

    @Test
    @DisplayName("field 12 on channel 19: CAP-056 1689 / 1725 / 1786 / 1815 / 1843 byte for byte (incl. CRC)")
    fun ancModeListWritesCh19() {
        assertEquals(Settings056.WRITE_NC_OFF_1689, wire(SettingsCodec.ancModeListRequest(19, all.with(AncMode.ACTIVE, false))!!))
        assertEquals(Settings056.WRITE_ALL_1725, wire(SettingsCodec.ancModeListRequest(19, all)!!))
        assertEquals(Settings056.WRITE_OFF_OFF_1786, wire(SettingsCodec.ancModeListRequest(19, all.with(AncMode.OFF, false))!!))
        assertEquals(Settings056.WRITE_ADAPTIVE_OFF_1815, wire(SettingsCodec.ancModeListRequest(19, all.with(AncMode.ADAPTIVE, false))!!))
        assertEquals(Settings056.WRITE_TRANSPARENCY_OFF_1843, wire(SettingsCodec.ancModeListRequest(19, all.with(AncMode.TRANSPARENT, false))!!))
    }

    @Test
    @DisplayName("the bit order (PROTOCOL.md §4.5.3 2026-09-28): unticking Adaptive clears boolean 4 (1815), Transparency boolean 3 (1843)")
    fun ancModeListBitOrder() {
        fun payload(frame: String) = (PwRpc.decode((Hdlc.decode(hex(frame)) as BudsResult.Success).value.payload) as BudsResult.Success).value.payload
        val adaptiveOff = SettingsCodec.ancModeListRequest(19, all.with(AncMode.ADAPTIVE, false))!!.payload.toHex()
        val transparencyOff = SettingsCodec.ancModeListRequest(19, all.with(AncMode.TRANSPARENT, false))!!.payload.toHex()
        assertEquals("220a6208080110011801" + "2000", adaptiveOff, "12:{1:1 2:1 3:1 4:0}")
        assertEquals("220a620808011001" + "1800" + "2001", transparencyOff, "12:{1:1 2:1 3:0 4:1}")
        assertEquals(payload(Settings056.WRITE_ADAPTIVE_OFF_1815).toHex(), adaptiveOff)
        assertEquals(payload(Settings056.WRITE_TRANSPARENCY_OFF_1843).toHex(), transparencyOff)
    }

    @Test
    @DisplayName("field 12 on channel 21: the real CAP-041 frames 2192 (Adaptive off), 2176 (Transparency off), 2198 (two ticked) — no hand-built frame needed")
    fun ancModeListWritesCh21() {
        assertEquals(Settings056.WRITE_ADAPTIVE_OFF_CH21_2192, wire(SettingsCodec.ancModeListRequest(21, all.with(AncMode.ADAPTIVE, false))!!))
        assertEquals(Settings056.WRITE_TRANSPARENCY_OFF_CH21_2176, wire(SettingsCodec.ancModeListRequest(21, all.with(AncMode.TRANSPARENT, false))!!))
        val two = AncModeList(noiseCancellation = true, off = false, transparency = true, adaptive = false)
        assertEquals(Settings056.WRITE_TWO_LEFT_CH21_2198, wire(SettingsCodec.ancModeListRequest(21, two)!!))
    }

    @Test
    fun `a list with fewer than two modes ticked cannot be encoded at all (hgj_java 165-168)`() {
        val one = AncModeList(noiseCancellation = true, off = false, transparency = false, adaptive = false)
        assertNull(SettingsCodec.ancModeListRequest(19, one))
        assertNull(SettingsCodec.ancModeListRequest(21, one.with(AncMode.ACTIVE, false)))
    }

    @Test
    @DisplayName("field 12 decodes: the read 1531 (all four), the CAP-036 read 1516 (Off unticked), the push 1696 and every write back to its list")
    fun ancModeListDecodes() {
        assertEquals(RoutedFrame.Setting(SettingValue.AncModes(all)), route(Settings056.READ_12_RESP_ALL_1531))
        assertEquals(RoutedFrame.Setting(SettingValue.AncModes(all.with(AncMode.OFF, false))), route(Settings056.READ_12_RESP_OFF_UNTICKED_1516))
        assertEquals(RoutedFrame.Setting(SettingValue.AncModes(all.with(AncMode.ACTIVE, false))), route(Settings056.PUSH_NC_OFF_1696))
        fun payload(frame: String) = (PwRpc.decode((Hdlc.decode(hex(frame)) as BudsResult.Success).value.payload) as BudsResult.Success).value.payload
        assertEquals(SettingValue.AncModes(all.with(AncMode.ADAPTIVE, false)), SettingsCodec.decode(payload(Settings056.WRITE_ADAPTIVE_OFF_1815)))
        assertEquals(SettingValue.AncModes(all.with(AncMode.TRANSPARENT, false)), SettingsCodec.decode(payload(Settings056.WRITE_TRANSPARENCY_OFF_1843)))
    }

    @Test
    @DisplayName("supplementary structural (hand-built, labelled): a field-12 shape other than the four booleans 1-4, each 0/1 once, is not interpreted")
    fun ancModeListOtherShapes() {
        assertNull(SettingsCodec.decode(hex("2209620808011001180120")), "a declared length longer than the bytes")
        assertNull(SettingsCodec.decode(hex("22086206080110011801")), "only three booleans")
        assertNull(SettingsCodec.decode(hex("220c620a08011001180120012801")), "a fifth field")
        assertNull(SettingsCodec.decode(hex("220a62080801100118012002")), "a boolean with value 2")
        assertNull(SettingsCodec.decode(hex("220a62080801080118012001")), "field 1 twice, field 2 missing")
    }

    // ---- reads ----

    @Test
    @DisplayName("ReadSetting requests byte-identical to CAP-036 1445/1451/1457/1514/1526/1532/1538 (channel 21) and CAP-056 1529 (channel 19)")
    fun readRequests() {
        val expected = mapOf(
            2 to Settings036.READ_2_REQ, 4 to Settings036.READ_4_REQ, 7 to Settings036.READ_7_REQ,
            17 to Settings036.READ_17_REQ, 19 to Settings036.READ_19_REQ, 22 to Settings036.READ_22_REQ,
        )
        for ((field, frame) in expected) assertEquals(frame, wire(Maestro.readSettingRequest(21, field)!!), "field $field")
        // ADR-046: field 12 is readable — CAP-036 frame 1514 (channel 21) and CAP-056 frame 1529 (channel 19), byte for byte.
        assertEquals(Settings056.READ_12_REQ_CH21_1514, wire(Maestro.readSettingRequest(21, 12)!!))
        assertEquals(Settings056.READ_12_REQ_CH19, wire(Maestro.readSettingRequest(19, 12)!!))
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
            Settings036.READ_REJECTED_1441, Settings056.READ_12_RESP_ALL_1531, Settings056.READ_12_RESP_OFF_UNTICKED_1516,
            Settings056.PUSH_NC_OFF_1696, Settings056.WRITE_ADAPTIVE_OFF_1815, Settings056.WRITE_INEAR_ON_2173,
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
