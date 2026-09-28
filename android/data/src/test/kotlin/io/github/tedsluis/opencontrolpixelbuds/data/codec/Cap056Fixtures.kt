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
 * Real DLCI 0x02 pw_hdlc frames for the press-and-hold ANC-mode list (`qhr` field 12, DECISIONS.md ADR-046) and the "In-ear detection" switch (field 2,
 * ADR-047) — `ai-sessions/0056`. Every value is the RFCOMM payload of the named HCI frame, CRC-32 checked:
 * `tshark -r CAP-0NN-btsnoop_hci.log -Y 'btrfcomm.len>0 && frame.number in {…}' -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data`,
 * decoded with `python3 scripts/pwrpc_decode.py CAP-0NN-btsnoop_hci.log`. "app" = the official Pixel Buds app. No identifier is carried (pw_rpc header,
 * a setting number and its value only). Channel 19 = request address `00 3b` / response `80 a3`; channel 21 = `00 4b` / `00 a5`.
 */
internal object Settings056 {
    // ---- `CAP-056` (2026-09-28, Group AR, Pixel 7a, official app), channel 19 ----

    /** Frame 1529 (17:31:17.163), app → Buds: `ReadSetting 4:12`. */
    const val READ_12_REQ_CH19 = "7e003b0310131dea71de7d5e2551aed0ae2a02200cb2f23e307e"

    /** Frame 1531 (17:31:17.224): `RESPONSE ReadSetting 4:{12:{1:1 2:1 3:1 4:1}}` — all four ticked (film 17:31:27). */
    const val READ_12_RESP_ALL_1531 = "7e80a3032a0c220a62080801100118012001080110131dea71de7d5e2551aed0aebe26a1e07e"

    /** Frame 1502 (17:31:16.524): `RESPONSE ReadSetting 4:{2:0}` — in-ear detection off (switch OFF on film 17:31:13). */
    const val READ_2_RESP_OFF_1502 = "7e80a3032a0422021000080110131dea71de7d5e2551aed0ae9095ad297e"

    /** Frame 1689 (17:31:30.889, untick Noise cancellation): `WriteSetting 4:{12:{1:0 2:1 3:1 4:1}}` (= `CAP-021` 5237). */
    const val WRITE_NC_OFF_1689 = "7e003b0310131dea71de7d5e251d9a8c9e2a0c220a62080800100118012001f81ce2ad7e"

    /** Frame 1696 (17:31:31.425): the Buds' `SubscribeToSettingsChanges` push mirroring 1689, `4:{12:{1:0 2:1 3:1 4:1}}`. */
    const val PUSH_NC_OFF_1696 = "7e80a3032a0c220a62080800100118012001080710131dea71de7d5e25f5ad21281fbcad167e"

    /** Frame 1697 (17:31:31.530): the empty `RESPONSE` status OK to 1689 (byte-identical to [SettingsWrites.ACK_CH19_1629]). */
    const val ACK_CH19_1697 = "7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e"

    /** Frame 1725 (17:31:36.773, re-tick Noise cancellation): `4:{12:{1:1 2:1 3:1 4:1}}` (= `CAP-021` 5247). */
    const val WRITE_ALL_1725 = "7e003b0310131dea71de7d5e251d9a8c9e2a0c220a620808011001180120014c17950b7e"

    /** Frame 1786 (17:31:42.598, untick Off): `4:{12:{1:1 2:0 3:1 4:1}}` (= `CAP-021` 5255). */
    const val WRITE_OFF_OFF_1786 = "7e003b0310131dea71de7d5e251d9a8c9e2a0c220a62080801100018012001fc3ef5367e"

    /** Frame 1815 (17:31:54.306, untick **Adaptive** on film): `4:{12:{1:1 2:1 3:1 4:0}}` — boolean 4 = Adaptive. */
    const val WRITE_ADAPTIVE_OFF_1815 = "7e003b0310131dea71de7d5e251d9a8c9e2a0c220a62080801100118012000da27927c7e"

    /** Frame 1843 (17:32:05.982, untick **Transparency** on film): `4:{12:{1:1 2:1 3:0 4:1}}` — boolean 3 = Transparency. */
    const val WRITE_TRANSPARENCY_OFF_1843 = "7e003b0310131dea71de7d5e251d9a8c9e2a0c220a620808011001180020017b7d5d570a7e"

    /** Frame 2173 (17:33:35.629, the switch "In-ear detection" → ON on film): `WriteSetting 4:{2:1}` (= `CAP-024` 1912). ACK 2179 = [ACK_CH19_1697]. */
    const val WRITE_INEAR_ON_2173 = "7e003b0310131dea71de7d5e251d9a8c9e2a0422021001067a3a8b7e"

    /** Frame 4048 (17:35:33.419, → OFF): `WriteSetting 4:{2:0}` (= `CAP-024` 1850). ACK 4056 = [ACK_CH19_1697]. */
    const val WRITE_INEAR_OFF_4048 = "7e003b0310131dea71de7d5e251d9a8c9e2a0422021000904a3dfc7e"

    // ---- `CAP-056`, channel 21 ----

    /** Frame 2849 (17:34:25.565, → OFF): `WriteSetting 4:{2:0}` on channel 21. */
    const val WRITE_INEAR_OFF_CH21_2849 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422021000a0a6cb947e"

    /** Frame 2855 (17:34:25.609): the empty `RESPONSE` status OK to 2849 (= [SettingsWrites.ACK_CH21_1731]). */
    const val ACK_CH21_2855 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"

    /** Frame 3627 (17:35:18.806, → ON): `WriteSetting 4:{2:1}` on channel 21. */
    const val WRITE_INEAR_ON_CH21_3627 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220210013696cce37e"

    // ---- channel 21 field 12: real frames from other captures (the prompt expected none; `ai-sessions/0056` §3) ----

    /** `CAP-041` frame 2192 (2026-09-06 17:14:24.391, official app, Group AH): `4:{12:{1:1 2:1 3:1 4:0}}` on channel 21 — Adaptive unticked. */
    const val WRITE_ADAPTIVE_OFF_CH21_2192 = "7e004b0310151dea71de7d5e251d9a8c9e2a0c220a620808011001180120004c7d5ed6577e"

    /** `CAP-041` frame 2176 (17:14:22.831): `4:{12:{1:1 2:1 3:0 4:1}}` on channel 21 — Transparency unticked. Answered OK by 2181 (= [ACK_CH21_2855]). */
    const val WRITE_TRANSPARENCY_OFF_CH21_2176 = "7e004b0310151dea71de7d5e251d9a8c9e2a0c220a62080801100118002001ed2413217e"

    /** `CAP-041` frame 2198 (17:14:24.840): `4:{12:{1:1 2:0 3:1 4:0}}` — exactly two ticked (NC, Transparency). */
    const val WRITE_TWO_LEFT_CH21_2198 = "7e004b0310151dea71de7d5e251d9a8c9e2a0c220a62080801100018012000fc57b66a7e"

    /** `CAP-036` frame 1514 (2026-09-04 06:36:33.153, official app's connect sweep): `ReadSetting 4:12` on channel 21. */
    const val READ_12_REQ_CH21_1514 = "7e004b0310151dea71de7d5e2551aed0ae2a02200c08b2acdb7e"

    /** `CAP-036` frame 1516 (06:36:33.195): `RESPONSE 4:{12:{1:1 2:0 3:1 4:1}}` — Off unticked. */
    const val READ_12_RESP_OFF_UNTICKED_1516 = "7e00a5032a0c220a62080801100018012001080110151dea71de7d5e2551aed0ae115784487e"
}
