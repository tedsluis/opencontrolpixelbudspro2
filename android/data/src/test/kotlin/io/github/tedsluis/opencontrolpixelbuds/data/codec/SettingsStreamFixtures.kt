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
 * Real DLCI 0x02 pw_hdlc frames (flags, escapes and CRC as on the wire) of the settings-changes stream — `SubscribeToSettingsChanges`
 * (DECISIONS.md ADR-064/065, `ai-sessions/0091`). Every value is one HDLC frame of the named HCI frame's RFCOMM payload (a payload holding several
 * frames was split on `7e`; the CRC-32 of each verified):
 * `tshark -r CAP-0NN-btsnoop_hci.log -Y 'frame.number==N' -T fields -E separator='|' -e frame.number -e frame.time -e frame.p2p_dir -e bthci_acl.chandle
 * -e btrfcomm.dlci -e data.data`, decoded with `python3 scripts/pwrpc_decode.py CAP-0NN-btsnoop_hci.log`. Nothing is redacted: these frames carry no
 * identifier (the pw_rpc header, a setting number and its value). "app" = the official Pixel Buds app. Channel 21: request address `00 4b`, response
 * `00 a5`; channel 19: `00 3b` / `80 a3` (ADR-034).
 */
internal object SettingsStream {
    // ---- the two requests of every official-app MAESTRO open ----

    /** `CAP-051` frame 1177 (21:43:13.004), app → Buds, channel 21: `SubscribeToSettingsChanges` REQUEST, empty payload. */
    const val SUBSCRIBE_REQ_CH21_CAP051_1177 = "7e004b0310151dea71de7d5e25f5ad2128019754b07e"

    /** `CAP-056` frame 1451 (17:31:15.770), channel 19 (= `CAP-051` 2229, the re-open after the Buds' own `DISC`). */
    const val SUBSCRIBE_REQ_CH19_CAP056_1451 = "7e003b0310131dea71de7d5e25f5ad212807ee8eb27e"

    /** `CAP-051` frame 1178, channel 21: `ReadSetting 4:13` REQUEST (= `CAP-045` 2021, `CAP-036` 1517). */
    const val READ_13_REQ_CH21_CAP051_1178 = "7e004b0310151dea71de7d5e2551aed0ae2a02200d9e82abac7e"

    /** `CAP-056` frame 1450, channel 19 (= `CAP-021` 1069, `CAP-027` 910, `CAP-051` 2231). */
    const val READ_13_REQ_CH19_CAP056_1450 = "7e003b0310131dea71de7d5e2551aed0ae2a02200d24c239477e"

    // ---- RESPONSE (type 1) to `ReadSetting 4:13` — `4:{13:n}` ----

    /** `CAP-051` frame 1225 (the 2nd of 5 HDLC frames in that RFCOMM payload), channel 21: `4:{13:4}` = Adaptive. */
    const val READ_13_RESP_ADAPTIVE_CH21_CAP051_1225 = "7e00a5032a0422026804080110151dea71de7d5e2551aed0ae067d5e749e7e"

    /** `CAP-056` frame 1468, channel 19: `4:{13:2}` = Noise cancellation. */
    const val READ_13_RESP_ACTIVE_CH19_CAP056_1468 = "7e80a3032a0422026802080110131dea71de7d5e2551aed0ae57085e2d7e"

    /** `CAP-027` frame 923 (the 4th of 5), channel 19: `4:{13:3}` = Transparency. */
    const val READ_13_RESP_TRANSPARENT_CH19_CAP027_923 = "7e80a3032a0422026803080110131dea71de7d5e2551aed0aebfd3a5947e"

    /** `CAP-021` frame 1089 (the 2nd of 4), channel 19: `4:{13:1}` = Off. */
    const val READ_13_RESP_OFF_CH19_CAP021_1089 = "7e80a3032a0422026801080110131dea71de7d5e2551aed0ae2e62233c7e"

    /** `CAP-045` frame 2033 (the 1st of 3), channel 21: `4:{13:0}` = no mode settable (= `CAP-036` 1519 / 1421's 3rd frame). */
    const val READ_13_RESP_NONE_CH21_CAP045_2033 = "7e00a5032a0422026800080110151dea71de7d5e2551aed0ae651b08147e"

    /** `CAP-051` frame 2247 (the 2nd of 2), channel 19: `4:{13:0}` — the re-open's read after the Buds' own `DISC` (1996), 21:45:50.260. */
    const val READ_13_RESP_NONE_CH19_CAP051_2247 = "7e80a3032a0422026800080110131dea71de7d5e2551aed0aec6b9d8857e"

    // ---- SERVER_STREAM (type 7) packets of the subscription — `4:{13:n}` ----

    /** `CAP-051` frame 1717 (21:43:43.479), channel 21: `4:{13:2}` — 144 ms after the Notify 1707 of a press-and-hold. */
    const val STREAM_13_ACTIVE_CH21_CAP051_1717 = "7e00a5032a0422026802080710151dea71de7d5e25f5ad2128cbb358d57e"

    /** `CAP-051` frame 1755 (21:43:56.004), channel 21: `4:{13:4}` — 2 ms after the Notify 1754. */
    const val STREAM_13_ADAPTIVE_CH21_CAP051_1755 = "7e00a5032a0422026804080710151dea71de7d5e25f5ad21283967a2f77e"

    /** `CAP-051` frame 1802 (21:44:05.150) and 1534 (28.376, 269 ms after the app's own Set 1527), channel 21: `4:{13:3}`. */
    const val STREAM_13_TRANSPARENT_CH21_CAP051_1802 = "7e00a5032a0422026803080710151dea71de7d5e25f5ad21282368a36c7e"

    /** `CAP-038` frame 2170 (06:50:50.589), channel 19 (DLCI 0x03 in that log, no Message Stream open): `4:{13:3}`. */
    const val STREAM_13_TRANSPARENT_CH19_CAP038_2170 = "7e80a3032a0422026803080710131dea71de7d5e25f5ad212880ca73fd7e"

    /** `CAP-038` frame 2205, channel 19 (= `CAP-056` 2324 and 4028's 1st frame): `4:{13:2}`. */
    const val STREAM_13_ACTIVE_CH19_CAP038_2205 = "7e80a3032a0422026802080710131dea71de7d5e25f5ad2128681188447e"

    /** `CAP-045` frame 2277, channel 21 (= `CAP-038` 2479): `4:{13:0}` — no mode settable. */
    const val STREAM_13_NONE_CH21_CAP045_2277 = "7e00a5032a0422026800080710151dea71de7d5e25f5ad21285a02de7d5d7e"

    /** `CAP-021` frame 1842, channel 19: `4:{13:1}` = Off. */
    const val STREAM_13_OFF_CH19_CAP021_1842 = "7e80a3032a0422026801080710131dea71de7d5e25f5ad2128117bf5557e"

    /** `CAP-056` frame 2259 (17:33:46.727, 11 ms after the Notify 2257) and 4025, channel 19: `4:{13:4}`. */
    const val STREAM_13_ADAPTIVE_CH19_CAP056_2259 = "7e80a3032a0422026804080710131dea71de7d5e25f5ad21289ac572667e"

    // ---- SERVER_STREAM packets for other fields ----

    /** `CAP-056` frame 1696, channel 19: `4:{12:{1:0 2:1 3:1 4:1}}` — the press-and-hold mode list after the app's write 1689. */
    const val STREAM_12_CH19_CAP056_1696 = "7e80a3032a0c220a62080800100118012001080710131dea71de7d5e25f5ad21281fbcad167e"

    /** `CAP-056` frame 2175, channel 19: `4:{2:1}` — in-ear detection on, after the app's write 2173. */
    const val STREAM_2_ON_CH19_CAP056_2175 = "7e80a3032a0422021001080710131dea71de7d5e25f5ad2128475780f97e"

    /** `CAP-069` frame 2502, channel 21: `4:{29:1}` — head gestures off, the mirror of the app's write 2492 (its RESPONSE 2506). */
    const val STREAM_29_OFF_CH21_CAP069_2502 = "7e00a5032a052203e80101080710151dea71de7d5e25f5ad2128cff7296f7e"

    /** `CAP-015` frame 2116, channel 19: field 16, the all-zero EQ quintet mirrored by the Buds after the app's write 2111 (2117 is that write's ACK). */
    const val STREAM_16_FLAT_CH19_CAP015_2116 =
        "7e80a3032a1e221c8201190d0000000015000000001d0000000025000000002d00000000080710131dea71de7d5e25f5ad2128b1cbb10b7e"

    // ---- the Message Stream frames around the same changes (DLCI 0x04 payloads) ----

    /** `CAP-051` 1707 (21:43:43.336): `Notify ANC state` Noise cancellation, 144 ms before the stream packet 1717. */
    const val NOTIFY_ACTIVE_CAP051_1707 = "0813000401e8e808"

    /** `CAP-051` 1754 (Adaptive) and 2257 of `CAP-056` (17:33:46.716). */
    const val NOTIFY_ADAPTIVE_CAP051_1754 = "0813000401e8e840"

    /** `CAP-051` 1801 and 1530/1533 (Transparency); 1993 (21:45:45.603, before the Buds' own `DISC` 1996). */
    const val NOTIFY_TRANSPARENT_CAP051_1801 = "0813000401e8e880"

    /** `CAP-051` 1527 (21:43:28.107): the app's own `Set` Transparency. */
    const val SET_TRANSPARENT_CAP051_1527 = "0812001401e8e880" + "00000000000000000000000000000000"

    /** `CAP-051` 1529 (21:43:28.219): the Buds' ACK of that Set. */
    const val ACK_TRANSPARENT_CAP051_1529 = "ff010006081201e8e880"

    /** `CAP-051`: Set 1527 → stream 1534, in ms. */
    const val SET_TO_STREAM_MS = 269L

    /** `CAP-056`: the second Notify 4001 (17:35:31.534) → the stream packet 4028 (31.589), in ms. */
    const val NOTIFY_TO_STREAM_MS = 55L
}
