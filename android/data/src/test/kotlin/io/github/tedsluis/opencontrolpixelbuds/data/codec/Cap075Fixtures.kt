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
 * Real Message Stream bytes and timings from `CAP-075` set 2 (`captures/CAP-075-2026-10-12_17-22-31_2026-10-13_06-34-32-Group_BK/`, OpenControl 1.2.1
 * `31ee183` in the GrapheneOS Owner user **with** Google Play services — `CAP-075-FINDINGS.md` §1, §4, §5), for DECISIONS.md ADR-062 Update 2
 * (`ai-sessions/0089`): the constant 3-s re-claim step and the cause of a mode learned by a re-claim's own `Get`. Every byte value is the RFCOMM payload of the
 * named HCI frame, unchanged; the timings are the frames' own. "H2" = `CAP-075-btsnoop_hci2.log`, the Buds' handle `0x000b`. Command (`ai-sessions/0089`
 * RESULT §A.1): `tshark -r H2 -Y "frame.number==N" -T fields -e frame.time -e frame.p2p_dir -e bthci_acl.chandle -e btrfcomm.dlci -e data.data` (exit 0).
 */
internal object Cap075 {
    /** H2 2724 (06:30:35.887) / 2968 / 5090 / 5349, phone → Buds: the app's `Get` on each re-claim — `08 11 00 00`, the only thing the app sends on a re-claim. */
    const val GET_2968 = "08110000"

    /** H2 2735 (06:30:36.129), Buds → phone: re-claim 2/3's `Get` answered — ADAPTIVE (`40`); the mode the app last saw before 2980. */
    const val NOTIFY_ADAPTIVE_2735 = "0813000401e8e840"

    /**
     * H2 2980 (06:30:54.437), Buds → phone: re-claim 3/3's `Get` (2968) answered — TRANSPARENT (`80`): the mode changed on Play's claim #9 (2832, 06:30:43.406)
     * while the app did not have the channel — `CAP-075-FINDINGS.md` §4 hold #1, shown ≈ 11 s late with no cause line (film 2 s110).
     */
    const val NOTIFY_TRANSPARENT_2980 = "0813000401e8e880"

    /** H2 3006 (06:30:58.178), Buds → phone, **unprovoked** on the app's re-claimed hold: ACTIVE (`08`) — hold #6, a press-and-hold shown within a second. */
    const val NOTIFY_ACTIVE_3006 = "0813000401e8e808"

    /** H2 4893 (06:32:50.814), Buds → phone, unprovoked on chain 2's re-claim 1/3: ADAPTIVE (`40`) — the mode the app last saw before 5102. */
    const val NOTIFY_ADAPTIVE_4893 = "0813000401e8e840"

    /** H2 5102 (06:33:02.775), Buds → phone: re-claim 2/3's `Get` (5090) answered — ACTIVE (`08`), changed in the gap 06:33:01.3–02.2 (hold #22). */
    const val NOTIFY_ACTIVE_5102 = "0813000401e8e808"

    /** H2 5361 (06:33:21.189), Buds → phone, one RFCOMM frame: two "Battery updated" (`03 03`, 100 % / 100 %) and re-claim 3/3's `Get` (5349) answer — TRANSPARENT after 5102's ACTIVE (hold #25; the (i) of film 2 s265–269 showed it with no cause line). */
    const val FRAME_5361 = "030300036464ff030300036464ff0813000401e8e880"

    /** H2 3538 (06:31:32.667), Buds → phone: the tab claim's `Get` answered — OFF (`20`), the app's last report before the Disconnect of 06:31:51.8. */
    const val NOTIFY_OFF_3538 = "0813000401e8e820"

    /** H2 3964 (06:31:58.136), Buds → phone: the snapshot `Get` of the Connect of 06:31:55.5 answered — ACTIVE (`08`): a new session's first report, differing from 3538. */
    const val NOTIFY_ACTIVE_3964 = "0813000401e8e808"

    /** The step: E2's "re-claim 1/3" line 3 008 ms after the "closed" line (06:30:19.322 → 22.330); attempt 1 refused 537 ms later (X 9079), `SABM` 2502 06:30:23.760. */
    const val STEP_MS = 3_000L

    /** H2 2502 `SABM` (06:30:23.760) → 2570 `DISC` (06:30:28.509): a re-claimed hold's life, cut by Play's recovery 5.64 s after its loss (2476, 06:30:22.867). */
    const val REHELD_MS = 4_749L

    /** H2 2369 (06:30:19.281) → 2502 (06:30:23.760): the cut to the re-claim's `SABM` — 3.0 s step + attempt 1 refused + attempt 2. */
    const val CUT_TO_SABM_MS = 4_479L

    /** The `read return: -1` text of every cut (E2 160, 172, 184, …). */
    const val CUT_DETAIL = "IOException: bt socket closed, read return: -1"
}
