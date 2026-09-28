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
 * Real bytes from `CAP-063` (`captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/`), for the `ai-sessions/0054` improvements. Every value is the
 * RFCOMM payload of the named HCI frame, unchanged (`ai-sessions/0054` RESULT §4 has the output):
 * `tshark -r CAP-063-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm && btrfcomm.len>0 && frame.number==<n>" -T fields -e frame.number
 * -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data` (handle `0x000b` = the Buds' classic link; `bluetooth.addr` is empty with this
 * encapsulation). Nothing is redacted: these frames carry no identifier — only levels, flags, modes and the pw_rpc header.
 */
internal object Cap063 {
    // ---- DLCI 0x04 Message Stream (I-1) ----

    /** Frame 4774 (16:06:11.245), Buds → phone: Notify — Settable `00`, mode OFF (buds on the table, film). The Notify that disabled ANC for 6 min. */
    const val NOTIFY_SETTABLE_00_4774 = "0813000401e80020"

    /** Frame 4184 (16:04:32.537), Buds → phone: Notify — Settable `e8`, mode ACTIVE (answer to a Refresh claim's `Get`; buds off film, worn). */
    const val NOTIFY_SETTABLE_E8_4184 = "0813000401e8e808"

    /** The app's `Get ANC state` (`08 11 00 00`) — `CAP-062` 7098 / `CAP-036` 1169; in `CAP-063` sent on every snapshot/Refresh claim (e.g. before 4184). */
    const val GET_ANC = "08110000"

    /** Frame 4233 (16:04:42.496), phone → Buds: the app's ANC Set ADAPTIVE (`40`), 16 zero bytes. */
    const val SET_ADAPTIVE_4233 = "0812001401e8e84000000000000000000000000000000000"

    /** Frame 4241 (16:04:42.610), Buds → phone: ACK of 4233. */
    const val ACK_ADAPTIVE_4241 = "ff010006081201e8e840"

    /** Frame 4497 (16:05:27.967), phone → Buds: the app's ANC Set ACTIVE (`08`), a tap on ACTIVE. */
    const val SET_ACTIVE_4497 = "0812001401e8e80800000000000000000000000000000000"

    /** Frame 4508 (16:05:28.144), Buds → phone: ACK of 4497. */
    const val ACK_ACTIVE_4508 = "ff010006081201e8e808"

    // ---- battery / runtime info around a Connect (I-4) ----

    /** Frame 2756 (16:00:17.315), Buds → phone, DLCI 0x04: "Battery updated" — Left `e4`/Right `e4` = 100 % charging (both docked). */
    const val BATTERY_BOTH_CHARGING_2756 = "03030003e4e4ff"

    /** Frame 2723 (16:00:17.184), Buds → phone, DLCI 0x02 ch 21: `6:{1:{1:36 2:1} 2:{1:100 2:2} 3:{1:100 2:2}} 7:{1:1 2:1 3:0}` — both charging, Case 36. */
    const val STREAM_BOTH_CHARGING_2723 =
        "7e00a5032a1e180032120a04082410011204086410021a04086410023a06080110011800080710151dea71de7d5e2590821ee630af35847e"

    /** Frame 3046 (16:01:03.872), the first report after the 16:01:02 Connect, DLCI 0x04: Left `64`/Right `64` = 100 % not charging (both out). */
    const val BATTERY_NONE_CHARGING_3046 = "030300036464ff"

    /** Frame 3059 (16:01:04.208), DLCI 0x02 ch 21: `6:{2:{1:100 2:1} 3:{1:100 2:1}} 7:{1:0 2:0 3:0}` — neither charging, no Case entry
     *  (byte-identical to `CAP-062` 7118). */
    const val STREAM_NONE_CHARGING_3059 =
        "7e00a5032a181800320c1204086410011a04086410013a06080010001800080710151dea71de7d5e2590821ee6f061fae47e"
}
