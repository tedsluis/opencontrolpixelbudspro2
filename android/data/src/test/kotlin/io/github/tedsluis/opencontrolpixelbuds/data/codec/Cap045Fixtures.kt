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
 * Real bytes from `CAP-045` (`captures/CAP-045-2026-09-12_08-22-51_08-24-32-Group_AJ/`, the official app, Group AJ: press-and-hold ANC changes on a bud
 * while the official client held the Message Stream — `CAP-045-FINDINGS.md` §3, `TOUCH-007`), for `ai-sessions/0082` item 1 ("Changed by the Buds").
 * Every value is the RFCOMM payload of the named HCI frame, unchanged. Command (`ai-sessions/0082` RESULT §A.1; `bluetooth.addr` matches nothing in this
 * log, the Buds' handle `0x0002` comes from the Connection Complete event, frame 294):
 * `tshark -r CAP-045-btsnoop_hci.log -Y 'btrfcomm.dlci==4 and btrfcomm.len>0' -T fields -e frame.number -e frame.time_utc -e frame.p2p_dir
 * -e bthci_acl.chandle -e data.data | grep -E "\s08(11|12|13)"` (exit 0). The whole log holds no `08 12`: every change below is a bare `Notify`.
 */
internal object Cap045 {
    /** Frame 609 (06:22:57.420909 UTC), phone → Buds: the official client's connect-time `Get ANC state`. */
    const val GET_609 = "08110000"

    /** Frame 612 (06:22:57.471104), Buds → phone: the answer to 609 — Settable `e8`, mode ADAPTIVE (`40`). */
    const val NOTIFY_ADAPTIVE_612 = "0813000401e8e840"

    /** Frame 1583 (06:23:21.948918), Buds → phone: `Notify` only (no `Get`, no `Set` before it) — a press-and-hold: TRANSPARENT (`80`). */
    const val NOTIFY_TRANSPARENT_1583 = "0813000401e8e880"

    /** Frame 1755 (06:23:32.507031), Buds → phone: `Notify` only — ACTIVE (`08`, Noise cancellation). */
    const val NOTIFY_ACTIVE_1755 = "0813000401e8e808"

    /** Frame 1818 (06:23:46.898794), Buds → phone: `Notify` only — ADAPTIVE (`40`) again. */
    const val NOTIFY_ADAPTIVE_1818 = "0813000401e8e840"

    /** Frame 1849 (06:23:54.405932), Buds → phone: `Notify` only — OFF (`20`) with Settable `00` (a bud removed). */
    const val NOTIFY_OFF_SETTABLE_00_1849 = "0813000401e80020"
}
