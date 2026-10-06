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

    /** The `RpcPacket` payload of an already HDLC-decoded pw_rpc packet. */
    private fun rpcPayloadOf(rpc: ByteArray): ByteArray = (PwRpc.decode(rpc) as BudsResult.Success).value.payload

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
    fun `only the ADR-045, ADR-047 and ADR-053 flag fields can be written as a flag - not 12, not 13, not 29`() {
        assertEquals(setOf(2, 4, 11, 15, 19, 22, 27, 28), SettingsCodec.WRITABLE_FLAG_FIELDS)
        assertNull(SettingsCodec.flagRequest(21, 12, true))
        assertNull(SettingsCodec.flagRequest(21, 13, true), "setting 13 is not approved for anything (ai-sessions/0074 §3)")
        assertNull(SettingsCodec.flagRequest(21, 29, true), "head gestures are 1/2 on the wire, never a 0/1 flag (ADR-052)")
    }

    // ---- field 11, "Multipoint" (ADR-053, ai-sessions/0074) ----

    @Test
    @DisplayName("Multipoint on channel 21: CAP-069 3161 (4:{11:0}) / 3212 (4:{11:1}) and CAP-019 2482 / 2293, byte for byte incl. the CRC")
    fun multipointWritesCh21() {
        assertEquals(Settings074.MP_OFF_CH21_3161, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_MULTIPOINT, false)!!))
        assertEquals(Settings074.MP_ON_CH21_3212, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_MULTIPOINT, true)!!))
        assertEquals(Settings074.MP_OFF_CH21_CAP019_2482, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_MULTIPOINT, false)!!))
        assertEquals(Settings074.MP_ON_CH21_CAP019_2293, wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_MULTIPOINT, true)!!))
        // The frame check of the real frames is what the decoder accepts: Hdlc.decode verifies the CRC-32.
        for (f in listOf(Settings074.MP_OFF_CH21_3161, Settings074.MP_ON_CH21_3212)) assertEquals(true, Hdlc.decode(hex(f)) is BudsResult.Success)
        assertEquals(SettingValue.Flag(11, false), SettingsCodec.decode(rpcPayload(Settings074.MP_OFF_CH21_3161)))
        assertEquals(SettingValue.Flag(11, true), SettingsCodec.decode(rpcPayload(Settings074.MP_ON_CH21_3212)))
    }

    @Test
    @DisplayName("supplementary structural (derived, not captured): Multipoint on channel 19 = the channel-19 header of CAP-022 1871 with field 11 (58 00 / 58 01)")
    fun multipointOnChannel19Derived() {
        // No channel-19 write of field 11 exists in any capture (ai-sessions/0074 §A.1); the hardware run records it (named step). Structure only.
        val header = SettingsWrites.MONO_ON_1621.substringBefore("2a052203")
        for ((on, value) in listOf(false to "5800", true to "5801")) {
            val derived = wire(SettingsCodec.flagRequest(19, SettingsCodec.FIELD_MULTIPOINT, on)!!)
            assertEquals(header + "2a042202" + value, derived.substring(0, header.length + 12), "channel 19, address 00 3b, WriteSetting, 4:{11:$value}")
            assertEquals(true, Hdlc.decode(hex(derived)) is BudsResult.Success, "its own CRC-32")
            assertEquals(SettingValue.Flag(11, on), SettingsCodec.decode(rpcPayload(derived)))
        }
        // The whole frame against an independent computation (python zlib.crc32 over address 00 3b … value, ai-sessions/0074 §I) — still not a capture.
        assertEquals("7e003b0310131dea71de7d5e251d9a8c9e2a04220258009d8f9dc47e", wire(SettingsCodec.flagRequest(19, SettingsCodec.FIELD_MULTIPOINT, false)!!))
        assertEquals("7e003b0310131dea71de7d5e251d9a8c9e2a04220258010bbf9ab37e", wire(SettingsCodec.flagRequest(19, SettingsCodec.FIELD_MULTIPOINT, true)!!))
    }

    @Test
    @DisplayName("Multipoint reads: requests = CAP-036 1471 (ch 21) / CAP-024 1009 (ch 19); answers CAP-069 1267 (ch 21), 7272 (ch 19) = on")
    fun multipointReads() {
        assertEquals(Settings036.READ_11_REQ, wire(Maestro.readSettingRequest(21, SettingsCodec.FIELD_MULTIPOINT)!!))
        assertEquals(Settings074.READ_11_REQ_CH19_1009, wire(Maestro.readSettingRequest(19, SettingsCodec.FIELD_MULTIPOINT)!!))
        for (f in listOf(Settings074.READ_11_RESP_CH21_1267, Settings074.READ_11_RESP_CH19_7272, Settings036.READ_11_RESP)) {
            assertEquals(RoutedFrame.Setting(SettingValue.Flag(11, true)), route(f), f)
        }
        assertEquals(true, (route(Settings074.ACK_CH21_3170) as RoutedFrame.RpcResult).isOk)
    }

    // ---- field 29, "Use head gestures" (ADR-052, ai-sessions/0074): 1 = off, 2 = on ----

    @Test
    @DisplayName("head gestures on channel 21: CAP-069 2492 (4:{29:1} = off) / 2564 (4:{29:2} = on), CAP-020 2038 / 1935, CAP-041 2268, byte for byte")
    fun headGesturesWritesCh21() {
        assertEquals(Settings074.HG_OFF_CH21_2492, wire(SettingsCodec.headGesturesRequest(21, on = false)))
        assertEquals(Settings074.HG_ON_CH21_2564, wire(SettingsCodec.headGesturesRequest(21, on = true)))
        assertEquals(Settings074.HG_OFF_CH21_CAP020_2038, wire(SettingsCodec.headGesturesRequest(21, on = false)))
        assertEquals(Settings074.HG_ON_CH21_CAP020_1935, wire(SettingsCodec.headGesturesRequest(21, on = true)))
        assertEquals(Settings074.HG_OFF_CH21_CAP041_2268, wire(SettingsCodec.headGesturesRequest(21, on = false)))
        for (f in listOf(Settings074.HG_OFF_CH21_2492, Settings074.HG_ON_CH21_2564)) assertEquals(true, Hdlc.decode(hex(f)) is BudsResult.Success)
    }

    @Test
    @DisplayName("head gestures: the wire value is 1 for off and 2 for on (ADR-052) — never the 0/1 of a flag; the write payloads decode back")
    fun headGesturesWireValues() {
        assertEquals("2203e80101", SettingsCodec.headGesturesRequest(19, on = false).payload.toHex(), "off = 1")
        assertEquals("2203e80102", SettingsCodec.headGesturesRequest(19, on = true).payload.toHex(), "on = 2")
        assertEquals(SettingValue.HeadGestures(false), SettingsCodec.decode(rpcPayload(Settings074.HG_OFF_CH21_2492)))
        assertEquals(SettingValue.HeadGestures(true), SettingsCodec.decode(rpcPayload(Settings074.HG_ON_CH21_2564)))
        assertNull(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_HEAD_GESTURES, true), "no 0/1 path to field 29")
    }

    @Test
    @DisplayName("supplementary structural (derived, not captured): head gestures on channel 19 = the channel-19 header of CAP-022 1621 with 4:{29:1|2}")
    fun headGesturesOnChannel19Derived() {
        // No channel-19 write of field 29 exists in any capture (ai-sessions/0074 §A.1); the hardware run records it (named step). Structure only.
        val header = SettingsWrites.MONO_ON_1621.substringBefore("2a052203")
        for ((on, value) in listOf(false to "e80101", true to "e80102")) {
            val derived = wire(SettingsCodec.headGesturesRequest(19, on))
            assertEquals(header + "2a052203" + value, derived.substring(0, header.length + 14), "channel 19, address 00 3b, 4:{29:$value}")
            assertEquals(true, Hdlc.decode(hex(derived)) is BudsResult.Success, "its own CRC-32")
            assertEquals(SettingValue.HeadGestures(on), SettingsCodec.decode(rpcPayload(derived)))
        }
        // The whole frame against an independent computation (python zlib.crc32, ai-sessions/0074 §I) — still not a capture.
        assertEquals("7e003b0310131dea71de7d5e251d9a8c9e2a052203e80101fcd6da847e", wire(SettingsCodec.headGesturesRequest(19, on = false)))
        assertEquals("7e003b0310131dea71de7d5e251d9a8c9e2a052203e801024687d31d7e", wire(SettingsCodec.headGesturesRequest(19, on = true)))
    }

    @Test
    @DisplayName("head gestures reads: requests CAP-036 1559 (ch 21) / CAP-024 1097 (ch 19); answers CAP-069 1226, 7209 = on; CAP-024 1100, CAP-020 1183 = off")
    fun headGesturesReads() {
        assertEquals(Settings036.READ_29_REQ, wire(Maestro.readSettingRequest(21, SettingsCodec.FIELD_HEAD_GESTURES)!!))
        assertEquals(Settings074.READ_29_REQ_CH19_1097, wire(Maestro.readSettingRequest(19, SettingsCodec.FIELD_HEAD_GESTURES)!!))
        for (f in listOf(Settings074.READ_29_ON_CH21_1226, Settings074.READ_29_ON_CH19_7209, Settings036.READ_29_RESP)) {
            assertEquals(RoutedFrame.Setting(SettingValue.HeadGestures(true)), route(f), f)
        }
        for (f in listOf(Settings074.READ_29_OFF_CH19_CAP024_1100, Settings074.READ_29_OFF_CH21_CAP020_1183)) {
            assertEquals(RoutedFrame.Setting(SettingValue.HeadGestures(false)), route(f), f)
        }
    }

    @Test
    @DisplayName("supplementary structural (hand-built, labelled): a head-gesture read of 0 or 3 is not interpreted — an OK result, never on/off")
    fun headGesturesOtherValues() {
        // The real CAP-069 frame 1226 (4:{29:2}) with the value byte changed and the frame re-sealed (new CRC) — no capture holds such a value.
        val real = (Hdlc.decode(hex(Settings074.READ_29_ON_CH21_1226)) as BudsResult.Success).value
        for (v in listOf("00", "03")) {
            val payload = hex(real.payload.toHex().replace("2203e80102", "2203e801$v"))
            assertNull(SettingsCodec.decode(rpcPayloadOf(payload)), "29:$v")
            val routed = route(Hdlc.encode(real.address, real.control, payload).toHex())
            assertEquals(true, routed is RoutedFrame.RpcResult && routed.isOk, "29:$v reaches the read as an OK answer it cannot use (UnreadableAnswer)")
        }
    }

    // ---- fields 27 / 28, case sounds "Other alerts" / "Earbuds replaced" (ADR-054, ai-sessions/0074) ----

    @Test
    @DisplayName("case sounds: all eight official writes byte for byte — CAP-024 2053/2084 (27) and 1988/2023 (28) on ch 19, CAP-058 5680/5697 and 5623/5643 on ch 21")
    fun caseSoundWrites() {
        val other = SettingsCodec.FIELD_CASE_SOUND_OTHER_ALERTS
        val replaced = SettingsCodec.FIELD_CASE_SOUND_EARBUDS_REPLACED
        val expected = listOf(
            Triple(19, other, false) to Settings074.CS27_OFF_CH19_2053, Triple(19, other, true) to Settings074.CS27_ON_CH19_2084,
            Triple(21, other, false) to Settings074.CS27_OFF_CH21_5680, Triple(21, other, true) to Settings074.CS27_ON_CH21_5697,
            Triple(19, replaced, false) to Settings074.CS28_OFF_CH19_1988, Triple(19, replaced, true) to Settings074.CS28_ON_CH19_2023,
            Triple(21, replaced, false) to Settings074.CS28_OFF_CH21_5623, Triple(21, replaced, true) to Settings074.CS28_ON_CH21_5643,
        )
        for ((key, frame) in expected) {
            val (channel, field, on) = key
            assertEquals(frame, wire(SettingsCodec.flagRequest(channel, field, on)!!), "ch $channel 4:{$field:${if (on) 1 else 0}}")
            assertEquals(SettingValue.Flag(field, on), SettingsCodec.decode(rpcPayload(frame)), "decodes back")
        }
    }

    @Test
    @DisplayName("case sounds reads: requests CAP-036 1553/1556 (ch 21), CAP-024 1089/1093 (ch 19); answers CAP-024 1092/1096, CAP-058 4514/4517 = on; ACKs 2061, 5683")
    fun caseSoundReads() {
        assertEquals(Settings036.READ_27_REQ, wire(Maestro.readSettingRequest(21, 27)!!))
        assertEquals(Settings036.READ_28_REQ, wire(Maestro.readSettingRequest(21, 28)!!))
        assertEquals(Settings074.READ_27_REQ_CH19_1089, wire(Maestro.readSettingRequest(19, 27)!!))
        assertEquals(Settings074.READ_28_REQ_CH19_1093, wire(Maestro.readSettingRequest(19, 28)!!))
        for (f in listOf(Settings074.READ_27_RESP_CH19_1092, Settings074.READ_27_RESP_CH21_4514, Settings036.READ_27_RESP)) {
            assertEquals(RoutedFrame.Setting(SettingValue.Flag(27, true)), route(f), f)
        }
        for (f in listOf(Settings074.READ_28_RESP_CH19_1096, Settings074.READ_28_RESP_CH21_4517, Settings036.READ_28_RESP)) {
            assertEquals(RoutedFrame.Setting(SettingValue.Flag(28, true)), route(f), f)
        }
        for (ack in listOf(Settings074.ACK_CH19_2061, Settings074.ACK_CH21_5683)) assertEquals(true, (route(ack) as RoutedFrame.RpcResult).isOk)
        // The Buds' own pushes of an "off" (CAP-058 5682 / 5627): 27 and 28 are told apart on the read side too.
        assertEquals(RoutedFrame.Setting(SettingValue.Flag(27, false)), route(Settings074.STREAM_27_OFF_CH21_5682))
        assertEquals(RoutedFrame.Setting(SettingValue.Flag(28, false)), route(Settings074.STREAM_28_OFF_CH21_5627))
    }

    // ---- field 15, "Volume EQ" (ADR-055, ai-sessions/0074) ----

    @Test
    @DisplayName("Volume EQ: CAP-022 1871 / 1895 and CAP-015 3487 / 3505 on ch 19, CAP-041 2461 (off) on ch 21 — byte for byte incl. the CRC")
    fun volumeEqWrites() {
        val f = SettingsCodec.FIELD_VOLUME_EQ
        assertEquals(Settings074.VEQ_OFF_CH19_1871, wire(SettingsCodec.flagRequest(19, f, false)!!))
        assertEquals(Settings074.VEQ_ON_CH19_1895, wire(SettingsCodec.flagRequest(19, f, true)!!))
        assertEquals(Settings074.VEQ_OFF_CH19_CAP015_3487, wire(SettingsCodec.flagRequest(19, f, false)!!))
        assertEquals(Settings074.VEQ_ON_CH19_CAP015_3505, wire(SettingsCodec.flagRequest(19, f, true)!!))
        assertEquals(Settings074.VEQ_OFF_CH21_CAP041_2461, wire(SettingsCodec.flagRequest(21, f, false)!!))
        assertEquals(SettingValue.Flag(15, false), SettingsCodec.decode(rpcPayload(Settings074.VEQ_OFF_CH21_CAP041_2461)))
        assertEquals(SettingValue.Flag(15, true), SettingsCodec.decode(rpcPayload(Settings074.VEQ_ON_CH19_1895)))
    }

    /** TODO(verify): ADR-055 — the channel-21 "on" frame is not captured; `CAP-070` (Group BF) records it and its real bytes replace the derived ones. */
    @Test
    @DisplayName("supplementary structural (derived, not captured): Volume EQ on, channel 21 = CAP-041 2461 with value 01 and its own CRC (zlib: 9977e84e)")
    fun volumeEqOnChannel21Derived() {
        val built = wire(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_VOLUME_EQ, true)!!)
        assertEquals(Settings074.VEQ_ON_CH21_DERIVED, built, "the app's CRC-32 agrees with an independent zlib computation")
        assertEquals(Settings074.VEQ_OFF_CH21_CAP041_2461.substringBefore("78000f47ef397e") + "7801", built.substringBefore("9977e84e7e"))
        assertEquals(true, Hdlc.decode(hex(built)) is BudsResult.Success)
        assertEquals(SettingValue.Flag(15, true), SettingsCodec.decode(rpcPayload(built)))
    }

    @Test
    @DisplayName("Volume EQ reads: requests CAP-036 1520 (ch 21) / CAP-024 1031 (ch 19); answers CAP-024 1038, CAP-058 2930 = on, CAP-041 3239 = off; ACKs 1877, 2465")
    fun volumeEqReads() {
        assertEquals(Settings036.READ_15_REQ, wire(Maestro.readSettingRequest(21, SettingsCodec.FIELD_VOLUME_EQ)!!))
        assertEquals(Settings074.READ_15_REQ_CH19_1031, wire(Maestro.readSettingRequest(19, SettingsCodec.FIELD_VOLUME_EQ)!!))
        for (f in listOf(Settings074.READ_15_ON_CH19_1038, Settings074.READ_15_ON_CH21_2930, Settings036.READ_15_RESP)) {
            assertEquals(RoutedFrame.Setting(SettingValue.Flag(15, true)), route(f), f)
        }
        assertEquals(RoutedFrame.Setting(SettingValue.Flag(15, false)), route(Settings074.READ_15_OFF_CH21_CAP041_3239))
        for (ack in listOf(Settings074.ACK_CH19_1877, Settings074.ACK_CH21_2465)) assertEquals(true, (route(ack) as RoutedFrame.RpcResult).isOk)
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
        assertNull(Maestro.readSettingRequest(21, 13), "setting 13: no read is approved (ai-sessions/0074 §3)")
        assertNull(Maestro.readSettingRequest(21, 39))
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
