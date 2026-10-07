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
 * Real balance writes (`ai-sessions/0064` F-2; the `[‹]`/`[›]` steps were removed again in `0066` — the wire is unchanged, ADR-045). Every value is the RFCOMM payload
 * of the named HCI frame, unchanged (`ai-sessions/0064` RESULT §D): `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 && frame.number==<n>" -T
 * fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data` (exit 0); decoded with `python3 scripts/pwrpc_decode.py <log>`. Nothing is
 * redacted: these frames carry only the pw_rpc header, field 17 and its value. All written by OpenControl.
 */
internal object Cap066Balance {
    /**
     * `CAP-066-btsnoop_hci.log.last` frame 7723 (2026-10-01 16:30:40.202401, phone → Buds, DLCI 0x02), channel 19: `WriteSetting 4:{17:11}` = zigzag −6 =
     * **Right 6** — the closest the 32 drags of `CAP-066` came to the target Right 4 on the right side (`CAP-066-FINDINGS.md` §5).
     */
    const val RIGHT_6_A7723 = "7e003b0310131dea71de7d5e251d9a8c9e2a05220388010bc2f4d82c7e"

    /**
     * `CAP-066-btsnoop_hci.log.last` frame 7873 (16:31:27.678821, phone → Buds, DLCI 0x02), channel 19: `WriteSetting 4:{17:0}` = **Centre**, the last write of
     * the run — byte-identical to `CAP-046` 1873 ([SettingsWrites.BALANCE_CENTRE_1873]).
     */
    const val CENTRE_A7873 = "7e003b0310131dea71de7d5e251d9a8c9e2a0522038801004a2d0abb7e"

    /**
     * `CAP-064-btsnoop_hci.log` frame 6671 (2026-10-01 10:14:28.241526, phone → Buds, DLCI 0x02), channel 21: `WriteSetting 4:{17:7}` = zigzag −4 = **Right 4**,
     * the value `CAP-066` could not reach. Byte-identical to `CAP-063` 5211 (2026-09-27 16:07:49.546053). Until `CAP-070` no channel-19 `17:7` write existed in
     * any capture (`pwrpc_decode.py` over every capture HCI log, `grep -E "4:\{17:[0-9]+\}"`, `ai-sessions/0064` RESULT §D → 26 lines with `4:{17:7}`:
     * 4 writes, all channel 21; positive control 6 lines with `17:11`); the channel-19 form is [Settings070.BAL_R4_CH19_A3747] (`ai-sessions/0076`).
     */
    const val RIGHT_4_CH21_6671 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203880107a97d5edf037e"

    /** `CAP-064` frame 6673 (10:14:28.338493, Buds → phone): the empty `RESPONSE` status OK to 6671 — byte-identical to [SettingsWrites.ACK_CH21_1731]. */
    const val ACK_CH21_6673 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"
}
