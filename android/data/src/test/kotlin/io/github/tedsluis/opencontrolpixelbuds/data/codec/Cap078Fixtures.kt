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
 * Real bytes from `CAP-078` (`captures/CAP-078-2026-10-17_16-23-51_17-17-41-Group_BN/`, the experiment build 1.3.1-exp.1 of `ai-sessions/0096`, user 10,
 * channel 21 unless named) for DECISIONS.md ADR-067 … ADR-070 (`ai-sessions/0098`). Each value is one pw_hdlc frame (flags included, escapes as on the wire)
 * from `python3 scripts/pwhdlc_crc_table.py CAP-078-btsnoop_hci3.log.last 0x000b` (its raw column; CRC-32 checked, 667 of 667 ok) — a frame number with two
 * HDLC frames (e.g. 6495: the RESPONSE and a stream packet in one RFCOMM frame) is split into the two. The Message Stream bytes (ring) are the RFCOMM payloads
 * of `tshark -r CAP-078-btsnoop_hci3.log.last -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci==0x04 && btrfcomm.len>0" -T fields -e frame.number -e data.data`
 * (`CAP-078-FINDINGS.md` §3–§5). No identifier is in any of them.
 */
internal object Cap078 {
    /** H 6491 (16:48:37.994), phone → Buds, channel 21: `WriteSetting 4:{13:2}` (Noise cancellation) — the request of [SettingsCodec.ancStateRequest]. */
    const val WRITE_13_ACTIVE_REQ_CH21_6491 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220268027234239d7e"

    /** H 6493 (16:48:38.058), Buds → phone: the settings-stream push `4:{13:2}` 64 ms after the request (byte-identical to `CAP-051` 1717). */
    const val STREAM_13_ACTIVE_CH21_6493 = "7e00a5032a0422026802080710151dea71de7d5e25f5ad2128cbb358d57e"

    /** H 6495 (16:48:38.063), Buds → phone, first HDLC frame: the empty `RESPONSE` to the write, status 0 (the same bytes for every write on channel 21). */
    const val WRITE_RESP_OK_CH21_6495 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"

    /** H 7279 (16:52:09.917): the push `4:{13:1}` (Off) after [WRITE_13_OFF_REQ_CH21_7277]. */
    const val STREAM_13_OFF_CH21_7279 = "7e00a5032a0422026801080710151dea71de7d5e25f5ad2128b2d925c47e"

    /** H 6540 (16:48:45.700): `WriteSetting 4:{13:3}` (Transparency), channel 21. */
    const val WRITE_13_TRANSPARENT_REQ_CH21_6540 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422026803e40424ea7e"

    /** H 7277 (16:52:09.828): `WriteSetting 4:{13:1}` (Off), channel 21. */
    const val WRITE_13_OFF_REQ_CH21_7277 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422026801c8652a047e"

    /** H 7298 (16:52:27.182): `WriteSetting 4:{13:4}` (Adaptive), channel 21. */
    const val WRITE_13_ADAPTIVE_REQ_CH21_7298 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422026804479140747e"

    /** H 6756 (16:49:24.909): the push `4:{13:0}` — no mode settable, no bud worn (byte-identical to `CAP-045` 2277). */
    const val STREAM_13_NONE_CH21_6756 = "7e00a5032a0422026800080710151dea71de7d5e25f5ad21285a02de7d5d7e"

    /** H 6775 (16:49:37.050): `WriteSetting 4:{13:2}` with no bud worn — the same bytes as [WRITE_13_ACTIVE_REQ_CH21_6491]. */
    const val WRITE_13_ACTIVE_REQ_NO_BUD_CH21_6775 = WRITE_13_ACTIVE_REQ_CH21_6491

    /** H 6777 (16:49:37.123): its `RESPONSE`, status 9 `FAILED_PRECONDITION` (field 6 = `30 09`); no push followed. */
    const val WRITE_RESP_FAILED_PRECONDITION_CH21_6777 = "7e00a503080110151dea71de7d5e251d9a8c9e3009c00a6dff7e"

    /** H 5921 (16:46:20.072), channel 19: `WriteSetting 4:{13:1}` (the reference frame of `ai-sessions/0096`). */
    const val WRITE_13_OFF_REQ_CH19_5921 = "7e003b0310131dea71de7d5e251d9a8c9e2a0422026801f889dc6c7e"

    /** H 5924 (16:46:20.147), channel 19: the empty `RESPONSE`, status 0. */
    const val WRITE_RESP_OK_CH19_5924 = "7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e"

    // ---- the ring (DLCI 0x04, RFCOMM payloads) ----

    /** H 7772 (16:54:31.177), phone → Buds: Ring Left (`CAP-025` 2131's bytes). */
    const val RING_LEFT_7772 = "0401000102"

    /** H 7774: the Buds' ACK of a Ring, with its state byte (`ff 01 00 03 04 01 00`). */
    const val RING_ACK_7774 = "ff010003040100"

    /** H 7775 (16:54:31.261): the Buds' own ringing status — Left. */
    const val RING_STATUS_LEFT_7775 = "0401000102"

    /** H 7816 (16:54:46.193): the Buds' status — stopped. */
    const val RING_STATUS_STOPPED_7816 = "0401000100"

    /** H 7970 (16:55:37.289), phone → Buds: the Seeker's ACK of a status (B2; the official app's `CAP-025` 2048). */
    const val RING_STATUS_ACK_7970 = "ff0100020401"

    /** H 8203 (16:56:37.848), phone → Buds: Ring both. */
    const val RING_BOTH_8203 = "0401000103"

    /** H 8211 (16:56:37.904): the Buds' status — both ringing. */
    const val RING_STATUS_BOTH_8211 = "0401000103"
}
