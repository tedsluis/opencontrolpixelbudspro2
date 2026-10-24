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
 * Real bytes from `CAP-079` (`captures/CAP-079-2026-10-18_07-07-31_08-42-33-Group_BO/`, release 1.4.0 and experiment 1.4.0-exp.1, user 10, channel 19) for
 * `ai-sessions/0100`. H1 = CAP-079-btsnoop_hci1.log, H3 = CAP-079-btsnoop_hci4.log.last (`CAP-079-EVENT-NOTES.md`'s names). The DLCI 0x02 values are one pw_hdlc frame
 * each (flags included) from `python3 scripts/pwhdlc_crc_table.py <log> 0x000b` (its raw column; CRC-32 checked, 0 bad); the Message Stream bytes (ring) are the
 * RFCOMM payloads of `tshark -r CAP-079-btsnoop_hci1.log -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci==0x04 && btrfcomm.len>0" -T fields -e frame.number -e data.data`
 * (`CAP-079-FINDINGS.md` §3.3, §5, §11). No identifier is in any of them.
 */
internal object Cap079 {
    /** H1 2501 (07:13:36.631), Buds → phone: the `RESPONSE` to `WriteSetting 4:{13:2}` (H1 2495) with no bud worn — status 9 `FAILED_PRECONDITION`. */
    const val WRITE_RESP_FAILED_PRECONDITION_CH19_2501 = "7e80a303080110131dea71de7d5e251d9a8c9e3009c8b1e5cb7e"

    /** H1 2742 (07:14:14.174), Buds → phone: the settings-stream push `4:{13:4}` (Adaptive) after a bud went in — the red line stayed (§3.3). */
    const val STREAM_13_ADAPTIVE_CH19_2742 = "7e80a3032a0422026804080710131dea71de7d5e25f5ad21289ac572667e"

    /** H3 2563 (08:13:11.720), Buds → phone: the empty `RESPONSE` to the experiment's `SetWallclock` (H3 2559) — the answer F1 reported as a time-out (§11). */
    const val SET_WALLCLOCK_RESP_OK_CH19_2563 = "7e80a303080110131dea71de7d5e254eed3b6712a9053e7e"

    // ---- the ring (DLCI 0x04, RFCOMM payloads), H1 ----

    /** H1 3848 (07:21:19.361), phone → Buds: Ring Left — the ring the 20-s hold gave up on before the tone ended (07:21:43.3, §5.2). */
    const val RING_LEFT_3848 = "0401000102"

    /** H1 3856 (07:21:19.392): the Buds' ACK of the Ring. */
    const val RING_ACK_3856 = "ff010003040100"

    /** H1 3859 (07:21:19.416): the Buds' ringing status — Left. */
    const val RING_STATUS_LEFT_3859 = "0401000102"

    /** H1 3862 (07:21:19.422), phone → Buds: the app's ACK of that status. */
    const val RING_STATUS_ACK_3862 = "ff0100020401"
}
