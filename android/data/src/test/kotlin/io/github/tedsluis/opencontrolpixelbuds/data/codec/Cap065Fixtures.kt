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

/**
 * Real bytes from `CAP-065` (`captures/CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA/`), for `ai-sessions/0062` F-1/F-3/F-5. Every value is the RFCOMM payload
 * of the named HCI frame, unchanged (`ai-sessions/0062` RESULT §D): `tshark -r CAP-065-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 &&
 * frame.number==<n>" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data` (exit 0); the two closes with `-Y
 * "bthci_acl.chandle==0x000b && frame.number==<n>" … -e btrfcomm.frame_type` (`0x43` = `DISC`). In this capture the Message Stream ran on **DLCI 0x05**
 * and MAESTRO on 0x03 — session-local numbers (the Buds opened the multiplexer); the app addresses them by its own channel keys. Nothing is redacted: the
 * announcement's field 1 (`1779298694`) is a running-version number, not a serial (`PROTOCOL.md` §2.2a, 2026-09-30 Update, L-5).
 */
internal object Cap065 {
    // ---- F-1: the claim's Get and its Notify --------------------------------------------------------------------------------------------

    /** Frame 5453 (11:14:33.198711) / 6322 (11:16:41.550382) / 10321 (11:21:35.069919), phone → Buds: `Get ANC state`. */
    const val GET = "08110000"

    /** Frame 5465 (11:14:33.437690), Buds → phone: the answer to 5453 — Settable `00`, mode OFF (BA-5, both buds loose on the table). */
    const val NOTIFY_00_OFF_5465 = "0813000401e80020"

    /** Frame 6334 (11:16:41.787502), Buds → phone: the answer to 6322 — Settable `e8`, mode OFF (BA-8, both worn). */
    const val NOTIFY_E8_OFF_6334 = "0813000401e8e820"

    /** Frame 10790 (11:22:17.882051), phone → Buds: the app's `Set` ACTIVE (`08`, Noise cancellation), sent without a `Get` (F7). */
    const val SET_ACTIVE_10790 = "0812001401e8e80800000000000000000000000000000000"

    /** Frame 10799 (11:22:18.003074), Buds → phone: ACK of 10790. */
    const val ACK_ACTIVE_10799 = "ff010006081201e8e808"

    // ---- F-3: answers cut off by the claim's close ------------------------------------------------------------------------------------------

    /** Frame 2640 (11:10:06.345675), phone → Buds: the tile's `Set` ACTIVE — byte-identical to 10790. Then `DISC` 2649 (11:10:06.493974, phone). */
    const val SET_ACTIVE_2640 = "0812001401e8e80800000000000000000000000000000000"

    /** Frame 2651 (11:10:06.530139), Buds → phone: the ACK of 2640 — after the close, lost to the app. */
    const val ACK_ACTIVE_2651 = "ff010006081201e8e808"

    /** Frames 2654/2655 (11:10:06.597405/.598258), Buds → phone: `Notify` mode ACTIVE — after the close, lost to the app. */
    const val NOTIFY_E8_ACTIVE_2654 = "0813000401e8e808"

    /** Frame 10341 (11:21:35.232662), Buds → phone: "Battery updated" 100 % / 100 % charging — 3 ms **before** the `DISC` 10344 (11:21:35.235814). */
    const val BATTERY_10341 = "0303000364e4ff"

    /** Frame 10356 (11:21:35.408228), Buds → phone: the answer to the snapshot `Get` 10321 — Settable `00` — after the close 10344, lost to the app. */
    const val NOTIFY_00_OFF_10356 = "0813000401e80020"

    // ---- F-5: the announcement ------------------------------------------------------------------------------------------------------------

    /**
     * Frame 1883 (11:08:59.571488), Buds → phone, MAESTRO (DLCI 0x03): the whole pw_hdlc frame of the unsolicited `GetSoftwareInfo` RESPONSE — response
     * address `80 a3`, control `03`, `RpcPacket` { payload `4:{1:{1:"1779298694" 2:"release_5.203"} 2:{…} 3:{…}} 5:<fixed64> 6:0`, `08 01` RESPONSE, `10 13`
     * channel 19, call id `0xFFFFFFFF` }, CRC `b5 f9 b0 f5`. Only the Left bud was out of the case (L-1, `PROTOCOL.md` §2.2a 2026-10-01 Update).
     */
    const val ANNOUNCEMENT_FRAME_1883 =
        "7e80a3032a6422570a1b0a0a31373739323938363934120d72656c656173655f352e323033121b0a0a31373739323938363934120d72656c656173655f352e3230331a1b0a0a" +
            "31373739323938363934120d72656c656173655f352e3230332934293fc2f6cbd81a3000080110131dea71de7d5e2544fa997138ffffffff0fb5f9b0f57e"
}
