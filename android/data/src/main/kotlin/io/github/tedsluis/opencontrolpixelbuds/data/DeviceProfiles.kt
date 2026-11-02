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
package io.github.tedsluis.opencontrolpixelbuds.data

import io.github.tedsluis.opencontrolpixelbuds.data.codec.DeviceInformation
import io.github.tedsluis.opencontrolpixelbuds.data.codec.SettingsCodec
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsModel

/** What a write is for — the Safe-Mode gate allows writes per model and per target. */
sealed interface WriteTarget {
    data object AncMode : WriteTarget
    data object Eq : WriteTarget

    /** A `WriteSetting 4:{N: …}` for setting field [field]. */
    data class Setting(val field: Int) : WriteTarget
    data object Find : WriteTarget

    /** Ring both buds at once (`RingTarget.BOTH`, ADR-070) — separate from [Find] because not every model has it. */
    data object RingBoth : WriteTarget
}

/**
 * One model the app knows: the firmware strings verified for it, what identifies it, and which writes are verified on it.
 *
 * [deviceType] and [modelIdHex] are cross-checks. When a value is known and the Buds report a different one, every write is refused.
 * `null` means "not known yet", so nothing is checked against it.
 */
internal data class DeviceProfile(
    val model: BudsModel,
    val firmware: Set<String>,
    val deviceType: Int?,
    val modelIdHex: String?,
    /** The settings fields the Connect-time read sweep asks for, in order. */
    val readFields: List<Int>,
    private val writable: (WriteTarget) -> Boolean,
    /** Writes need the device type read on this connection (and equal to [deviceType]) — a second signal besides the firmware. */
    val requireDeviceType: Boolean = false,
) {
    fun allows(target: WriteTarget): Boolean = writable(target)

    /** Whether any write at all is verified on this model. */
    val allowsAnyWrite: Boolean
        get() = listOf(WriteTarget.AncMode, WriteTarget.Eq, WriteTarget.Find).any(writable) || readFields.any { writable(WriteTarget.Setting(it)) }
}

internal object DeviceProfiles {

    /** The official app's sweep order (`CAP-036` 1445 … 1538, `CAP-024` 837 … 1097), as before this change. */
    private val PRO_2_READ_FIELDS = listOf(
        SettingsCodec.FIELD_IN_EAR_DETECTION,
        SettingsCodec.FIELD_TOUCH_CONTROLS,
        SettingsCodec.FIELD_PRESS_AND_HOLD,
        SettingsCodec.FIELD_MULTIPOINT,
        SettingsCodec.FIELD_ANC_MODE_LIST,
        SettingsCodec.FIELD_VOLUME_EQ,
        SettingsCodec.FIELD_VOLUME_BALANCE,
        SettingsCodec.FIELD_MONO_AUDIO,
        SettingsCodec.FIELD_CONVERSATION_DETECTION,
        SettingsCodec.FIELD_CASE_SOUND_OTHER_ALERTS,
        SettingsCodec.FIELD_CASE_SOUND_EARBUDS_REPLACED,
        SettingsCodec.FIELD_HEAD_GESTURES,
    )

    /**
     * The Pixel Buds Pro 2: firmware `release_5.203` (ADR-012/042), device type 6 (`REVERSE_ENGINEERING.md`, 2026-10-06 Update: "The Pixel Buds Pro 2
     * report type 6"), Model ID `da2db1` (`PROTOCOL.md` §0.1). Every write the app has is verified on it.
     */
    val PRO_2 = DeviceProfile(
        model = BudsModel.PIXEL_BUDS_PRO_2,
        firmware = setOf("release_5.203"),
        deviceType = 6,
        modelIdHex = DeviceInformation.PIXEL_BUDS_PRO_2_MODEL_ID,
        readFields = PRO_2_READ_FIELDS,
        writable = { true },
    )

    /**
     * The settings a Gen 1 write may name: 2, 4, 7 (one bud per write), 11, 12 (three booleans), 15, 17, 19, 22. Apart from 11, each is a `WriteSetting`
     * the official app sent to the Gen 1 with the same bytes this app sends, answered OK (`CAP-081`). Never 27, 28, 29 (not on the Gen 1) or anything
     * outside the codec's own allowlists.
     */
    private val GEN1_WRITABLE_FIELDS = setOf(
        SettingsCodec.FIELD_IN_EAR_DETECTION,
        SettingsCodec.FIELD_TOUCH_CONTROLS,
        SettingsCodec.FIELD_PRESS_AND_HOLD,
        // TODO(verify): the official app switches the Gen 1's multipoint over the Message Stream (`07 12`, signed; `CAP-081`), not field 11.
        SettingsCodec.FIELD_MULTIPOINT,
        SettingsCodec.FIELD_ANC_MODE_LIST,
        SettingsCodec.FIELD_VOLUME_EQ,
        SettingsCodec.FIELD_VOLUME_BALANCE,
        SettingsCodec.FIELD_MONO_AUDIO,
        SettingsCodec.FIELD_CONVERSATION_DETECTION,
    )

    /**
     * The original Pixel Buds Pro (Gen 1), as `CAP-081` shows it: firmware `release_5.11_signed` (hello, frame 1458 of `CAP-081-btsnoop_hci2.log`; also in
     * PixelBudsMacOS's third-party capture), `GetHardwareInfo` field 1 = **4** (frame 1591; the official app's "ALLEGRO"), Fast Pair Model ID **`6edaf7`**
     * (`03 01 00 03 6e da f7` on the Message Stream, frame 568 of `CAP-081-btsnoop_hci1.log`). The read sweep leaves out the Pro 2-only fields 27, 28 and 29.
     *
     * Writes: the noise-control mode (1–3), the EQ (16), Find left/right, and [GEN1_WRITABLE_FIELDS]. 🟡 HYPOTHESIS until the maintainer accepts the Gen 1
     * ADRs: the EQ, Find and the settings match the official app's Gen 1 writes byte for byte (`CAP-081`); the mode and multipoint do not (TODO(verify) above).
     * Every write needs the device type 4 read on the same connection ([requireDeviceType]).
     */
    val PRO_GEN1 = DeviceProfile(
        model = BudsModel.PIXEL_BUDS_PRO,
        firmware = setOf("release_5.11_signed"),
        deviceType = 4,
        modelIdHex = "6edaf7",
        readFields = PRO_2_READ_FIELDS - setOf(
            SettingsCodec.FIELD_CASE_SOUND_OTHER_ALERTS,
            SettingsCodec.FIELD_CASE_SOUND_EARBUDS_REPLACED,
            SettingsCodec.FIELD_HEAD_GESTURES,
        ),
        writable = { target ->
            when (target) {
                WriteTarget.Eq, WriteTarget.Find -> true
                // TODO(verify): the official app sets the Gen 1's mode over the Message Stream (`08 12`, signed with the account key; `CAP-081`);
                // this app's `WriteSetting 4:{13:n}` (ADR-067) is accepted by the Gen 1 and pushed back as `13 = n`, but no official-app capture shows it.
                WriteTarget.AncMode -> true
                WriteTarget.RingBoth -> false // the official app offers Ring left and Ring right only (`CAP-081`)
                is WriteTarget.Setting -> target.field in GEN1_WRITABLE_FIELDS
            }
        },
        requireDeviceType = true,
    )

    val ALL: List<DeviceProfile> = listOf(PRO_2, PRO_GEN1)

    /**
     * The profile every announced firmware string belongs to, or `null` when one is unknown or the strings belong to different models.
     */
    fun forFirmware(firmware: List<String>?): DeviceProfile? {
        if (firmware.isNullOrEmpty()) return null
        return ALL.firstOrNull { profile -> firmware.all { it in profile.firmware } }
    }

    fun forModel(model: BudsModel): DeviceProfile = ALL.first { it.model == model }
}
