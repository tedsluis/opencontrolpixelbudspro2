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
 * Real bytes from `CAP-064` (`captures/CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ/`), for `ai-sessions/0062` F-1 (ANC `Get` before every `Set`). Every
 * value is the RFCOMM payload of the named HCI frame, unchanged (`ai-sessions/0062` RESULT §D has the output):
 * `tshark -r CAP-064-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 && frame.number==<n>" -T fields -e frame.number -e frame.time
 * -e frame.p2p_dir -e btrfcomm.dlci -e data.data` (exit 0; DLCI 0x04 for all of them). Nothing is redacted: no identifier is in these frames.
 */
internal object Cap064 {
    /** Frame 3200 (10:07:47.360643), Buds → phone: the app's last `Notify` before the tile tap — Settable `e8`, mode ADAPTIVE. 18 s old at the tap. */
    const val NOTIFY_E8_ADAPTIVE_3200 = "0813000401e8e840"

    /** Frame 3433 (10:08:14.443932), phone → Buds: the tile's `Set` OFF (`20`), sent **without** a `Get` on that stale `e8`. */
    const val SET_OFF_3433 = "0812001401e8e82000000000000000000000000000000000"

    /** Frame 3440 (10:08:14.579657), Buds → phone: NAK of 3433, reason `0x02` "not allowed in the current state". */
    const val NAK_3440 = "ff020003020812"

    /** Frame 3443 (10:08:14.619365), Buds → phone: `Notify` — Settable `00`, mode OFF (both buds on the table, film). */
    const val NOTIFY_00_OFF_3443 = "0813000401e80020"

    /** Frame 4091 (10:09:49.104454), phone → Buds: the app's `Get ANC state`. */
    const val GET_4091 = "08110000"

    /** Frame 4103 (10:09:49.393403), Buds → phone: the answer to 4091 — Settable `e8`, mode OFF (AY-3a: Left on the table, Right worn). */
    const val NOTIFY_E8_OFF_4103 = "0813000401e8e820"
}
