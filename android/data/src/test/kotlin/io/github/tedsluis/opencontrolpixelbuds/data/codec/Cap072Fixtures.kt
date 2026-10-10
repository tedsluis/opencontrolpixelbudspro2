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
 * Real Message Stream bytes from `CAP-072` (`captures/CAP-072-2026-10-09_17-27-26_18-23-39-Group_BH/`, OpenControl 1.2.0 `ec6d163` in the GrapheneOS user
 * without Google Play — `CAP-072-FINDINGS.md` §5), for DECISIONS.md ADR-061 (`ai-sessions/0084`): the Buds' `Notify` frames the app did not provoke, which
 * reached it only because a claim happened to be open — the case the hold makes the rule. Every value is the RFCOMM payload of the named HCI frame, unchanged.
 * "A" = `CAP-072-btsnoop_hci2.log.last`, "B" = `CAP-072-btsnoop_hci2.log`; the Buds' handle `0x000b` (Connection Complete A 5491, 9506, 13104, 16151, 20735).
 * Command (`ai-sessions/0084` RESULT §A.1): `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci in {4,5} && frame.number>=13480 &&
 * frame.number<=13525" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.frame_type -e data.data` (exit 0), and the same over B 810–830.
 */
internal object Cap072 {
    /** A 13499 (17:51:10.646548), phone → Buds: the automatic re-open's snapshot `Get ANC state` (ADR-032/044). */
    const val GET_A13499 = "08110000"

    /** A 13515 (17:51:10.921080), Buds → phone: the answer to A 13499 — OFF (`20`), Settable `00` (both buds still in the case). */
    const val NOTIFY_OFF_00_A13515 = "0813000401e80020"

    /** A 13519 (17:51:10.984345), Buds → phone, 63 ms later, **unprovoked** (no `Get`, no `Set` before it): TRANSPARENT (`80`), Settable `e8` — the buds taken out. */
    const val NOTIFY_TRANSPARENT_A13519 = "0813000401e8e880"

    /** A 11938 (17:44:32.496769), Buds → phone: the answer to a tap's `Get` — TRANSPARENT (`80`), Settable `e8` (both buds worn). */
    const val NOTIFY_TRANSPARENT_A11938 = "0813000401e8e880"

    /** B 825 (18:19:41.644679), Buds → phone, **unprovoked**: NOISE CANCELLATION (`08`), Settable `e8` — the buds put in; the Buds closed the session 1.5 ms later (B 826/827). */
    const val NOTIFY_ACTIVE_B825 = "0813000401e8e808"
}
