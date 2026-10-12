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
 * Real Message Stream bytes and timings from `CAP-074` (`captures/CAP-074-2026-10-12_06-40-16_06-45-31-Group_BJ/`, the released 1.2.0 `5b4d5db` in the
 * GrapheneOS Owner user **with** Google Play services — `CAP-074-FINDINGS.md` §4), for DECISIONS.md ADR-062 (`ai-sessions/0087`): the hold the app kept on
 * the noise-control tab was cut by Play services' Fast Pair recovering the channel, 4.7–5.1 s after the app's `SABM`. Every byte value is the RFCOMM payload of the
 * named HCI frame, unchanged; the timings are the frames' own. "B" = `CAP-074-btsnoop_hci.log`, the Buds' handle `0x000b` (Connection Complete B 10528).
 * Command (`ai-sessions/0087` RESULT §A.1): `tshark -r B -Y "frame.number==N" -T fields -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type
 * -e data.data` (exit 0); the claim inventory `tshark -r B -Y "bthci_acl.chandle==0x000b && btrfcomm && (btrfcomm.dlci==4 || btrfcomm.dlci==5)" …` (exit 0,
 * 1,418 frames).
 */
internal object Cap074 {
    /** B 15822 (06:42:30.709), phone → Buds: the `SABM` of the Connect's snapshot claim after the Connect tap of 06:42:29.7 — no payload; the reference point of the cut below. */
    const val SABM_B15822_TIME = "06:42:30.709"

    /** B 15853 (06:42:30.868), Buds → phone: the snapshot `Get`'s answer — ADAPTIVE (`40`), Settable `e8` (both buds worn). The tab's hold started 06:42:31.044 (E 318). */
    const val NOTIFY_ADAPTIVE_B15853 = "0813000401e8e840"

    /** B 15860 (06:42:33.204), Buds → phone, **unprovoked** (no `Get`, no `Set` before it): TRANSPARENT (`80`) — the one press-and-hold shown after that Connect (film t=138). */
    const val NOTIFY_TRANSPARENT_B15860 = "0813000401e8e880"

    /**
     * B 15909 (06:42:35.842), phone → Buds: the `DISC` of the app's claim — **not** the app's release (no "released" line in the export; E 321/322 "RFCOMM on-demand
     * channel 0x04 closed (IOException: bt socket closed, read return: -1)" / "hold ended: the channel was closed, not by this app" at 06:42:35.972/.974):
     * the stack closed the app's port on Play's recovery connect, 5.13 s after the app's `SABM`, 5.71 s after Play lost its socket (B 15774, 06:42:30.128).
     */
    const val CUT_AFTER_SABM_MS = 5_133L

    /** B 15931 (06:42:38.219), phone → Buds: Play services' `SABM` — its retry 2.38 s after the cut; it succeeded (B 15957 `0813000401e8e808` answered its `Get`). */
    const val PLAY_RETRY_AFTER_CUT_MS = 2_377L

    /** B 14552 (06:40:54.639), Buds → phone: the tap's `Get` answered on the held channel — ADAPTIVE, Settable `e8`. */
    const val NOTIFY_ADAPTIVE_B14552 = "0813000401e8e840"

    /** B 14553 (06:40:54.645), phone → Buds: the `Set` TRANSPARENT of the tap (the 16 trailing bytes `00`, `PROTOCOL.md` §4.1). */
    const val SET_TRANSPARENT_B14553 = "0812001401e8e880" + "00000000000000000000000000000000"

    /** B 14555 (06:40:54.727), Buds → phone: its ACK. */
    const val ACK_TRANSPARENT_B14555 = "ff010006081201e8e880"

    /** B 14556 (06:40:54.753), Buds → phone: the `Notify` of the new mode (TRANSPARENT). */
    const val NOTIFY_TRANSPARENT_B14556 = "0813000401e8e880"

    /** The `read return: -1` text of every cut, as `RfcommBudsTransport` reports it on `channelClosed` (E 161, 176, 189, …). */
    const val CUT_DETAIL = "IOException: bt socket closed, read return: -1"
}
