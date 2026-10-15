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
 * Real bytes from `CAP-076` H1 (`CAP-076-btsnoop_hci1.log`, the Buds' ACL handle `0x000b`; `ai-sessions/0092`, app 1.3.0) for `ai-sessions/0094`
 * (A93-APP-02, `CAP-076-FINDINGS.md` §6.2 / §13 P1). Extracted with
 * `tshark -r CAP-076-btsnoop_hci1.log -Y "frame.number==4094 || frame.number==4135" -T fields -e frame.number -e bthci_acl.chandle -e btrfcomm.dlci -e data.data`.
 */
internal object Cap076 {
    /** H1 4094 (06:41:33.418), Buds → phone, DLCI 0x04: the S4 re-open's snapshot `Get` answered — Noise cancellation (`08`), after the last push 3972 (Transparency). */
    const val NOTIFY_ACTIVE_H1_4094 = "0813000401e8e808"

    /** H1 4135 (06:41:34.165, +747 ms), Buds → phone, DLCI 0x02, channel 21: the same session's `ReadSetting 4:13` answered — `4:{13:2}` = Noise cancellation. */
    const val READ_13_RESP_ACTIVE_CH21_H1_4135 = "7e00a5032a0422026802080110151dea71de7d5e2551aed0aef4aa8ebc7e"
}
