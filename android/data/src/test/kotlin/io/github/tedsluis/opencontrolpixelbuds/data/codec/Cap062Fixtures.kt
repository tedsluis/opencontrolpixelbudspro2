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
 * Real bytes from `CAP-062` (`captures/CAP-062-2026-09-25_06-38-36_06-57-10-Group_AX/`), for the `ai-sessions/0048` improvements. Every value is
 * the RFCOMM payload of the named HCI frame, unchanged (`ai-sessions/0048` RESULT §4 has the commands and the decode):
 * `tshark -r CAP-062-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 && frame.number==<n>" -T fields -e data.data`.
 * Nothing is redacted: these frames carry no identifier (no serial, no address) — only levels, flags and the pw_rpc header.
 */
internal object Cap062 {
    // ---- DLCI 0x04 Message Stream (I-3, I-6) ----

    /** Frame 5989, app → Buds: ANC Set TRANSPARENT (`80`), 16 zero bytes. */
    const val ANC_SET_TRANSPARENT = "0812001401e8e88000000000000000000000000000000000"

    /** Frame 5998: NAK, reason `0x02` "not allowed in the current state", for `08 12`. */
    const val NAK_NOT_ALLOWED = "ff020003020812"

    /** Frame 6000: Notify — Settable `00`, mode OFF (buds on the table, not worn). */
    const val NOTIFY_SETTABLE_00 = "0813000401e80020"

    /** Frame 8694, app → Buds: ANC Set ADAPTIVE (`40`). */
    const val ANC_SET_ADAPTIVE = "0812001401e8e84000000000000000000000000000000000"

    /** Frame 8705: ACK of that Set. */
    const val ACK_ADAPTIVE = "ff010006081201e8e840"

    /** Frame 8706: Notify — Settable `e8`, mode ADAPTIVE (worn). */
    const val NOTIFY_SETTABLE_E8 = "0813000401e8e840"

    /** Frame 9772, app → Buds: Ring Left. */
    const val RING_LEFT = "0401000102"

    /** Frame 9783: ACK of a Ring. */
    const val RING_ACK = "ff010003040100"

    // ---- the *Refresh battery* claim at 06:46:29 (`ai-sessions/0052`; debug export lines 308–314) ----
    // `tshark -r CAP-062-btsnoop_hci.log -Y 'bthci_acl.chandle==0x000b && btrfcomm.dlci==4 && frame.time >= "2026-09-25 06:46:29" &&
    //  frame.time <= "2026-09-25 06:46:31.5"' -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.frame_type -e data.data`:
    // SABM 7088, UA 7090, app `08 11 00 00` 7098, Device Information 7101, burst 7106 + 7109 (two frames in one RFCOMM payload), Notify 7110.

    /** Frame 2777 (06:41:07.121), app → Buds on DLCI 0x02, channel 19: the Connect-time `SubscribeRuntimeInfo` REQUEST (ADR-043) — the bytes a
     *  Refresh re-sends (ADR-043 Update 2026-09-26). Answered by the stream packet 2782 ([STREAM_NONE_2782]). */
    const val SUBSCRIBE_RUNTIME_INFO_2777 = "7e003b0310131dea71de7d5e2590821ee6602d65a97e"

    /** Frame 7098, app → Buds: `Get ANC state`. */
    const val GET_ANC_7098 = "08110000"

    /** Frame 7106: "Battery updated" — Left `e4` = 100 % charging, Right `64` = 100 % not charging, Case byte `ff`. */
    const val BATTERY_BURST_7106 = "03030003e464ff"

    /** Frame 7110: the Notify answering 7098 — Settable `00`, mode OFF. */
    const val NOTIFY_7110 = "0813000401e80020"

    // ---- DLCI 0x02 `SubscribeRuntimeInfo` SERVER_STREAM packets, whole pw_hdlc frames (flags, escapes and CRC as on the wire) ----

    /** Frame 2782 (ch 19, 06:41:07): `3:0 6:{2:{1:100 2:1} 3:{1:100 2:1}} 7:{1:0 2:0 3:0}` — no bud charging, no Case entry. */
    const val STREAM_NONE_2782 =
        "7e80a3032a181800320c1204086410011a04086410013a06080010001800080710131dea71de7d5e2590821ee6227fa42c7e"

    /** Frame 3760 (ch 19, 06:42:28.851): `6:{1:{1:60 2:1} 2:{1:100 2:1} 3:{1:100 2:2}} 7:{1:1 2:0 3:0}` — Right charging, Case 60 %. */
    const val STREAM_RIGHT_3760 =
        "7e80a3032a1e180032120a04083c10011204086410011a04086410023a06080110001800080710131dea71de7d5e2590821ee639a8c8637e"

    /** Frame 4845 (ch 19, 06:43:32.080): both buds charging (6.2.2 = 6.3.2 = 2, 7.1 = 7.2 = 1), Case 60 %. */
    const val STREAM_BOTH_4845 =
        "7e80a3032a1e180032120a04083c10011204086410021a04086410023a06080110011800080710131dea71de7d5e2590821ee6160866417e"

    /** Frame 7033 (ch 21, 06:46:25.732; debug export line 298): Left charging only (Right taken out), Case 60 %. */
    const val STREAM_LEFT_7033 =
        "7e00a5032a1e180032120a04083c10011204086410021a04086410013a06080010011800080710151dea71de7d5e2590821ee696e7a89b7e"

    /** Frame 7118 (ch 21, 06:46:35.578): the Left bud removed too — no bud charging, no Case entry. */
    const val STREAM_NONE_7118 =
        "7e00a5032a181800320c1204086410011a04086410013a06080010001800080710151dea71de7d5e2590821ee6f061fae47e"

    /**
     * Frame 4500 (DLCI **0x03** — session-local numbering of the MAESTRO channel on a Buds-initiated connection; ch 19, 06:43:03.552):
     * `6:{2:{1:100 2:1} 3:{1:100 2:2}} 7:{1:0 2:0 3:0}` — 6.3.2 says Right charging, 7.1 says not; the DLCI 0x05 battery frame 4520
     * (`0303000364e4ff`, 0.45 s later) says Right charging. A real 6.x/7.x disagreement.
     */
    const val STREAM_DISAGREE_4500 =
        "7e80a3032a181800320c1204086410011a04086410023a06080010001800080710131dea71de7d5e2590821ee61f46415a7e"
}
