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

import io.github.tedsluis.opencontrolpixelbuds.data.DeviceProfiles
import io.github.tedsluis.opencontrolpixelbuds.data.SafeModeGate
import io.github.tedsluis.opencontrolpixelbuds.data.WriteTarget
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsModel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Gen 1 (original Pixel Buds Pro) support: model recognition, the write gate, and the codec's Gen 1 forms.
 *
 * **External fixtures, supplementary (AGENTS.md §11).** [Gen1MacOsFixtures.GEN1_HELLO] and [Gen1MacOsFixtures.GEN1_ANC_PUSH] are not from a registered capture of this project. They are frames 1
 * and 5 of PixelBudsMacOS `Reference/captured-frames-2026-05-16.txt` (commit `0ffd4f9`, © 2026 Jesús Ramírez Ramos, MIT licence), captured from a Gen 1 on
 * `release_5.11_signed`. Their CRC-32 is valid. They are replaced by this project's own Gen 1 capture when one exists.
 */
class Gen1SupportTest {

    @Test
    fun `the Gen 1 announcement is a Maestro hello on channel 19 with release_5_11_signed`() {
        val routed = CodecRouter().feed(Dlci.MAESTRO, hex(Gen1MacOsFixtures.GEN1_HELLO), timestampMillis = 0)
        val hello = routed.single() as RoutedFrame.MaestroHello
        assertEquals(19, hello.channelId)
        assertEquals(listOf("release_5.11_signed"), hello.firmware)
        assertEquals(listOf(2, 3), hello.entries.map { it.index }) // no Case entry in this frame
        assertNotNull(MaestroChannel.forChannel(hello.channelId))
        assertEquals(BudsModel.PIXEL_BUDS_PRO, DeviceProfiles.forFirmware(hello.firmware)?.model)
    }

    @Test
    fun `the Gen 1 settings push decodes as noise-control mode Transparency`() {
        val routed = CodecRouter().feed(Dlci.MAESTRO, hex(Gen1MacOsFixtures.GEN1_ANC_PUSH), timestampMillis = 0)
        val setting = routed.single() as RoutedFrame.Setting
        assertTrue(setting.stream)
        assertEquals(SettingValue.AncState(AncMode.TRANSPARENT), setting.value)
    }

    @Test
    fun `firmware maps to exactly one model, mixed or unknown firmware to none`() {
        assertEquals(BudsModel.PIXEL_BUDS_PRO_2, DeviceProfiles.forFirmware(listOf("release_5.203"))?.model)
        assertEquals(BudsModel.PIXEL_BUDS_PRO, DeviceProfiles.forFirmware(listOf("release_5.11_signed"))?.model)
        assertNull(DeviceProfiles.forFirmware(listOf("release_5.203", "release_5.11_signed")))
        assertNull(DeviceProfiles.forFirmware(listOf("release_5.12_signed")))
        assertNull(DeviceProfiles.forFirmware(emptyList()))
    }

    @Test
    fun `Gen 1 writes - the verified targets with device type 4, never 27 28 29, never on a contradiction`() {
        val fw = listOf("release_5.11_signed")
        val allowed = listOf(WriteTarget.AncMode, WriteTarget.Eq) + listOf(2, 4, 7, 11, 12, 15, 17, 19, 22).map { WriteTarget.Setting(it) }
        for (target in allowed) {
            assertEquals(SafeModeGate.Verdict.Allowed, SafeModeGate.evaluate(fw, null, requireModelId = false, target = target, deviceType = 4), "$target")
            // the device type is required, and must be the Gen 1's
            assertInstanceOf(SafeModeGate.Verdict.Refused::class.java, SafeModeGate.evaluate(fw, null, requireModelId = false, target = target), "$target")
            assertInstanceOf(SafeModeGate.Verdict.Refused::class.java, SafeModeGate.evaluate(fw, null, requireModelId = false, target = target, deviceType = 6))
            assertInstanceOf(SafeModeGate.Verdict.Refused::class.java, SafeModeGate.evaluate(fw, "da2db1", requireModelId = false, target = target, deviceType = 4))
        }
        for (field in listOf(1, 3, 5, 6, 8, 9, 10, 14, 18, 21, 27, 28, 29)) {
            val verdict = SafeModeGate.evaluate(fw, "6edaf7", requireModelId = false, target = WriteTarget.Setting(field), deviceType = 4)
            assertInstanceOf(SafeModeGate.Verdict.Refused::class.java, verdict, "field $field")
        }
        assertEquals(SafeModeGate.Verdict.Allowed, SafeModeGate.evaluate(fw, "6edaf7", requireModelId = true, target = WriteTarget.Find, deviceType = 4))
        // The official app offers Ring left and Ring right only on a Gen 1 (CAP-081); the Pro 2 keeps Ring both (ADR-070).
        assertInstanceOf(SafeModeGate.Verdict.Refused::class.java, SafeModeGate.evaluate(fw, "6edaf7", requireModelId = true, target = WriteTarget.RingBoth, deviceType = 4))
        assertEquals(SafeModeGate.Verdict.Allowed, SafeModeGate.evaluate(listOf("release_5.203"), "da2db1", requireModelId = true, target = WriteTarget.RingBoth))
        assertInstanceOf(SafeModeGate.Verdict.Refused::class.java, SafeModeGate.evaluate(fw, null, requireModelId = true, target = WriteTarget.Find, deviceType = 4))
        // The UI shows no Safe Mode for a Gen 1, also before its device type is read.
        assertNull(SafeModeGate.observed(fw, null))
        assertNull(SafeModeGate.observed(fw, "6edaf7", 4))
        assertEquals(BudsModel.PIXEL_BUDS_PRO, SafeModeGate.observed(fw, null, 6)?.model)
    }

    @Test
    fun `own Gen 1 capture - hello, settings and hardware info decode`() {
        val hello = CodecRouter().feed(Dlci.MAESTRO, hex(Gen1OwnFixtures.HELLO_CH21_1458), 0).single() as RoutedFrame.MaestroHello
        assertEquals(21, hello.channelId)
        assertEquals(listOf(1, 2, 3), hello.entries.map { it.index })
        assertEquals(listOf("release_5.11_signed"), hello.firmware)
        val list = CodecRouter().feed(Dlci.MAESTRO, hex(Gen1OwnFixtures.READ_12_RESP_1522), 0).single() as RoutedFrame.Setting
        assertEquals(SettingValue.AncModes(AncModeList(noiseCancellation = true, off = true, transparency = true, adaptive = false), adaptiveReported = false), list.value)
        val hold = CodecRouter().feed(Dlci.MAESTRO, hex(Gen1OwnFixtures.READ_7_RESP_1509), 0).single() as RoutedFrame.Setting
        assertEquals(SettingValue.PressAndHold(HoldAction.ASSISTANT, HoldAction.NOISE_CONTROL), hold.value)
        val hw = CodecRouter().feed(Dlci.MAESTRO, hex(Gen1OwnFixtures.HARDWARE_INFO_RESP_1591_REDACTED), 0).single() as RoutedFrame.HardwareInfo
        assertEquals(4, hw.deviceType)
        assertEquals("1=4 2=1 5=7 6=7 7=<3 serials>", hw.shape)
    }

    @Test
    fun `Gen 1 writes carry exactly the shape the Gen 1 reports`() {
        fun readPayload(frame: String) = (PwRpc.decode((Hdlc.decode(hex(frame)) as BudsResult.Success).value.payload) as BudsResult.Success).value.payload
        val list = AncModeList(noiseCancellation = true, off = true, transparency = true, adaptive = false)
        assertArrayEquals(readPayload(Gen1OwnFixtures.READ_12_RESP_1522), SettingsCodec.ancModeListRequest(21, list, withAdaptive = false)!!.payload)
        assertNull(SettingsCodec.ancModeListRequest(19, list.copy(adaptive = true), withAdaptive = false), "Adaptive can never be written to a Gen 1")
        // The flags, the balance and the mode: the same `4:{N:v}` the Gen 1 answered with.
        assertArrayEquals(readPayload(Gen1OwnFixtures.READ_4_RESP_1490), SettingsCodec.flagRequest(21, SettingsCodec.FIELD_TOUCH_CONTROLS, true)!!.payload)
        assertArrayEquals(readPayload(Gen1OwnFixtures.READ_17_RESP_1543), SettingsCodec.balanceRequest(21, 0).payload)
        assertArrayEquals(readPayload(Gen1OwnFixtures.READ_13_RESP_1586), SettingsCodec.ancStateRequest(21, AncMode.TRANSPARENT).payload)
        // The Pro 2 form keeps its fourth boolean.
        assertEquals(12, SettingsCodec.ancModeListRequest(19, list)!!.payload.size) // 4:{12:{1 2 3 4}} vs the Gen 1's 10 bytes
        assertEquals(10, SettingsCodec.ancModeListRequest(19, list, withAdaptive = false)!!.payload.size)
    }

    @Test
    fun `Pro 2 is unchanged, and a contradicting device type refuses it`() {
        for (target in listOf(null, WriteTarget.AncMode, WriteTarget.Eq, WriteTarget.Setting(SettingsCodec.FIELD_HEAD_GESTURES))) {
            assertEquals(SafeModeGate.Verdict.Allowed, SafeModeGate.evaluate(listOf("release_5.203"), "da2db1", requireModelId = false, target = target, deviceType = 6))
            assertEquals(SafeModeGate.Verdict.Allowed, SafeModeGate.evaluate(listOf("release_5.203"), null, requireModelId = false, target = target))
        }
        assertEquals(SafeModeGate.Verdict.Allowed, SafeModeGate.evaluate(listOf("release_5.203"), "da2db1", requireModelId = true, target = WriteTarget.Find))
        assertInstanceOf(
            SafeModeGate.Verdict.Refused::class.java,
            SafeModeGate.evaluate(listOf("release_5.203"), null, requireModelId = false, target = WriteTarget.Eq, deviceType = 4),
        )
        assertNull(SafeModeGate.observed(listOf("release_5.203"), "da2db1", 6))
        assertEquals(setOf("release_5.203"), SafeModeGate.VERIFIED_FIRMWARE)
    }

    @Test
    fun `the Gen 1 read sweep leaves out the Pro 2-only fields`() {
        val gen1 = DeviceProfiles.PRO_GEN1.readFields
        assertFalse(SettingsCodec.FIELD_HEAD_GESTURES in gen1)
        assertFalse(SettingsCodec.FIELD_CASE_SOUND_OTHER_ALERTS in gen1)
        assertFalse(SettingsCodec.FIELD_CASE_SOUND_EARBUDS_REPLACED in gen1)
        assertEquals(DeviceProfiles.PRO_2.readFields - setOf(27, 28, 29), gen1)
        assertEquals(listOf(2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29), DeviceProfiles.PRO_2.readFields) // the Pro 2 sweep as before
    }

    @Test
    fun `a three-boolean ANC-mode list decodes without Adaptive and is marked as such (structural, hand-built)`() {
        // Hand-built: 4:{12:{1:1 2:0 3:1}} — the form expected from the Gen 1 (pbpctrl's 2022 AncrGestureLoop); no Gen 1 capture of it exists yet.
        val three = SettingsCodec.decode(hex("2208620608011000180" + "1")) as SettingValue.AncModes
        assertFalse(three.adaptiveReported)
        assertTrue(three.list.noiseCancellation)
        assertFalse(three.list.off)
        assertTrue(three.list.transparency)
        assertFalse(three.list.adaptive)
        // The Pro 2's four-boolean form is unchanged and marked as reporting Adaptive.
        val four = SettingsCodec.decode(hex("220a6208080110001801200" + "1")) as SettingValue.AncModes
        assertTrue(four.adaptiveReported)
        assertTrue(four.list.adaptive)
        // Two booleans, or 1-3 with a value other than 0/1, are still not interpreted.
        assertNull(SettingsCodec.decode(hex("2206620408011000")))
        assertNull(SettingsCodec.decode(hex("2208620608011002180" + "1")))
    }

    @Test
    fun `the device type is read from GetHardwareInfo field 1 - Pro 2 reports 6`() {
        val routed = CodecRouter().feed(Dlci.MAESTRO, hex(HardwareInfoFixtures.RESPONSE_CH21_CAP072_A7596_REDACTED), timestampMillis = 0)
        val info = routed.single() as RoutedFrame.HardwareInfo
        assertEquals(6, info.deviceType)
        assertEquals(3, info.serials.size)
        assertTrue(info.shape.startsWith("1=6 2=17 5=9 6=7 7=<3 serials>"), info.shape)
        assertFalse(info.shape.contains("5707"), "no serial text in the log line")
        assertNull(HardwareInfo.deviceType(hex("0a03414243"))) // field 1 as bytes, not a varint
    }
}

/**
 * Supplementary Gen 1 fixtures from PixelBudsMacOS `Reference/captured-frames-2026-05-16.txt` (commit `0ffd4f9`, © 2026 Jesús Ramírez Ramos, MIT licence) — see
 * [Gen1SupportTest].
 */
internal object Gen1MacOsFixtures {
    /** Frame 1: Left bud → MaestroA (channel 19), `GetSoftwareInfo` RESPONSE, call id 0xFFFFFFFF, entries 2 and 3 = `release_5.11_signed`. */
    const val GEN1_HELLO =
        "7e80a3032a4c10ef8898d4e4332238121a0a03353735121372656c656173655f352e31315f7369676e65641a1a0a03353735121372656c656173655f" +
        "352e31315f7369676e656429284b58f50363520e3000080110131dea71de7d5e2544fa997138ffffffff0f42c5694e7e"

    /** Frame 5: Right bud → MaestroA (channel 21), `SubscribeToSettingsChanges` SERVER_STREAM, payload `4:{13:3}` (Transparency). */
    const val GEN1_ANC_PUSH =
        "7e00a5032a0422026803080710151dea71de7d5e25f5ad2128380908e45ad27e"
}

/**
 * Buds → phone frames from `CAP-081-btsnoop_hci2.log` (the Gen 1 on `release_5.11_signed` answering this app's Connect on a Pixel 9 Pro, 2026-11-02),
 * extracted with `tshark -r CAP-081-btsnoop_hci2.log -Y "frame.number in {…}" -T fields -e frame.number -e data.data` and decoded with
 * `scripts/pwrpc_decode.py`. Channel 21, response address `00 a5`. In the hardware-info answer the three serials are redacted in the committed log
 * (`scripts/redact_btsnoop.py`, 14 × `X`), which leaves its CRC unmatched; the fixture re-seals the CRC (AGENTS.md §9, §11).
 */
internal object Gen1OwnFixtures {
    /** Frame 1458 — the unsolicited announcement: three entries (Case, Left, Right), each `575` / `release_5.11_signed`. */
    const val HELLO_CH21_1458 =
        "7e00a5032a6810c1d284cf993422540a1a0a03353735121372656c656173655f352e31315f7369676e6564121a0a03353735121372656c656173655f" +
            "352e31315f7369676e65641a1a0a03353735121372656c656173655f352e31315f7369676e656429ccc2b2fb23aff42f3000080110151dea71de" +
            "7d5e2544fa997138ffffffff0fc0fb23107e"

    /** Frame 1490 — `ReadSetting` answer `4:{4:1}` (touch controls on). */
    const val READ_4_RESP_1490 = "7e00a5032a0422022001080110151dea71de7d5e2551aed0ae38a99ef07e"

    /** Frame 1509 — `ReadSetting` answer `4:{7:{1:{4:{1:6}} 2:{4:{1:5}}}}` (left: Assistant, right: noise control). */
    const val READ_7_RESP_1509 = "7e00a5032a10220e3a0c0a0422020806120422020805080110151dea71de7d5e2551aed0aefe7d5d26b27e"

    /** Frame 1522 — `ReadSetting` answer `4:{12:{1:1 2:1 3:1}}`: three booleans, no fourth. */
    const val READ_12_RESP_1522 = "7e00a5032a0a22086206080110011801080110151dea71de7d5e2551aed0aed82d13a17e"

    /** Frame 1543 — `ReadSetting` answer `4:{17:0}` (balance centred). */
    const val READ_17_RESP_1543 = "7e00a5032a052203880100080110151dea71de7d5e2551aed0ae1662e1cc7e"

    /** Frame 1586 — `ReadSetting` answer `4:{13:3}` (Transparency). */
    const val READ_13_RESP_1586 = "7e00a5032a0422026803080110151dea71de7d5e2551aed0ae1c7175057e"

    /** Frame 1591 — `GetHardwareInfo` answer `1:4 2:1 5:7 6:7 7:{3 serials}`, serials redacted, CRC re-sealed. */
    const val HARDWARE_INFO_RESP_1591_REDACTED =
        "7e00a5032a3a08041001280730073a300a0e5858585858585858585858585858120e58585858585858585858585858581a0e5858585858585858585858" +
            "585858080110151dea71de7d5e25e3a5ec288f89a0d07e"
}
