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
 * Real DLCI 0x02 pw_hdlc frames (flags, escapes and CRC exactly as on the wire) for the settings of `ai-sessions/0052` (ADR-036 reads, ADR-045
 * writes). Every value is the RFCOMM payload of the named HCI frame:
 * `tshark -r CAP-0NN-btsnoop_hci.log -Y "btrfcomm.len>0 && frame.number==<n>" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data`,
 * decoded with `python3 scripts/pwrpc_decode.py CAP-0NN-btsnoop_hci.log`. Nothing is redacted: these frames carry no identifier (only the pw_rpc
 * header, a setting number and its value). "app" = the official Pixel Buds app.
 */
internal object Settings036 {
    // `CAP-036` (2026-09-04), the official app's connect-time sweep on channel 21 (request address 00 4b, response address 00 a5).

    /** Frame 1445, app → Buds: `ReadSetting 4:2`. */
    const val READ_2_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a0220020f9f143c7e"

    /** Frame 1447: `RESPONSE ReadSetting 4:{2:1}` — in-ear detection setting on. */
    const val READ_2_RESP = "7e00a5032a0422021001080110151dea71de7d5e2551aed0aedbec86017e"

    /** Frame 1451: `ReadSetting 4:4`. */
    const val READ_4_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a0220043a3a77d57e"

    /** Frame 1453: `4:{4:1}` — touch controls on. */
    const val READ_4_RESP = "7e00a5032a0422022001080110151dea71de7d5e2551aed0ae38a99ef07e"

    /** Frame 1457: `ReadSetting 4:7`. */
    const val READ_7_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a022007806b7d5e4c7e"

    /** Frame 1462: `4:{7:{1:{4:{1:5}} 2:{4:{1:5}}}}` — both buds: Active noise control. */
    const val READ_7_RESP = "7e00a5032a10220e3a0c0a0422020805120422020805080110151dea71de7d5e2551aed0ae1dff38c97e"

    /** Frame 1526: `ReadSetting 4:17`. */
    const val READ_17_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a022011d1deaab87e"

    /** Frame 1528: `4:{17:10}` — zigzag 10 = +5. */
    const val READ_17_RESP = "7e00a5032a05220388010a080110151dea71de7d5e2551aed0ae001feeab7e"

    /** Frame 1532: `ReadSetting 4:19`. */
    const val READ_19_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a022013fdbfa4567e"

    /** Frame 1534: `4:{19:0}` — mono off. */
    const val READ_19_RESP = "7e00a5032a052203980100080110151dea71de7d5e2551aed0ae6bfbb0db7e"

    /** Frame 1538: `ReadSetting 4:22`. */
    const val READ_22_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a022016724bce267e"

    /** Frame 1540: `4:{22:1}` — conversation detection on. */
    const val READ_22_RESP = "7e00a5032a052203b00101080110151dea71de7d5e2551aed0aee75df8aa7e"

    /** Frame 1471 (04:36:33.056 UTC): `ReadSetting 4:11` — byte-identical to `CAP-069` 1261 and `CAP-058` 2918/4470 (`ai-sessions/0074`). */
    const val READ_11_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a02200bab27c8457e"

    /** Frame 1513 (04:36:33.143): `4:{11:1}` — Multipoint on; byte-identical to `CAP-069` 1267. */
    const val READ_11_RESP = "7e00a5032a0422025801080110151dea71de7d5e2551aed0ae6e85eb5c7e"

    /** Frame 1520 (04:36:33.258): `ReadSetting 4:15` — byte-identical to `CAP-058` 2928/4479 and `CAP-069` 1155 (`ai-sessions/0074`). */
    const val READ_15_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a02200fb2e3a5427e"

    /** Frame 1522 (04:36:33.310): `4:{15:1}` — Volume EQ on; byte-identical to `CAP-058` 2930 and `CAP-041` 868/2007. */
    const val READ_15_RESP = "7e00a5032a0422027801080110151dea71de7d5e2551aed0ae13fed44b7e"

    /** Frame 1553 (04:36:33.879): `ReadSetting 4:27` — byte-identical to `CAP-069` 1215 and `CAP-058` 4512 (`ai-sessions/0074`). */
    const val READ_27_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a02201bcf377f587e"

    /** Frame 1555 (04:36:33.914): `4:{27:1}` — case sounds "Other alerts" on; byte-identical to `CAP-058` 4514 and `CAP-069` 1220. */
    const val READ_27_RESP = "7e00a5032a052203d80101080110151dea71de7d5e2551aed0ae77450d3f7e"

    /** Frame 1556 (04:36:33.920): `ReadSetting 4:28` — byte-identical to `CAP-069` 1227 and `CAP-058` 4515. */
    const val READ_28_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a02201c6ca21bc67e"

    /** Frame 1558 (04:36:33.970): `4:{28:1}` — case sounds "Earbuds replaced" on; byte-identical to `CAP-058` 4517 and `CAP-069` 1236. */
    const val READ_28_RESP = "7e00a5032a052203e00101080110151dea71de7d5e2551aed0ae6ea1efe07e"

    /** Frame 1559 (04:36:33.977): `ReadSetting 4:29` — byte-identical to `CAP-069` 1222 and `CAP-058` 4518 (`ai-sessions/0074`). */
    const val READ_29_REQ = "7e004b0310151dea71de7d5e2551aed0ae2a02201dfa921cb17e"

    /** Frame 1561 (04:36:34.034): `4:{29:2}` — head gestures **on** (2 = on, ADR-052); byte-identical to `CAP-069` 1226 and `CAP-058` 4520. */
    const val READ_29_RESP = "7e00a5032a052203e80102080110151dea71de7d5e2551aed0ae898482177e"

    /** Frame 1525: `4:{16:[0.1, 0.0, 0.3, 0.2, 0.2]}` — the EQ read answer of the same sweep. */
    const val READ_16_RESP =
        "7e00a5032a1e221c8201190dc0cccc3d15000000001da099993e25c0cc4c3e2dc0cc4c3e080110151dea71de7d5e2551aed0ae85b618ed7e"

    /** Frame 1441, the 4th pw_hdlc frame of that RFCOMM payload: `RESPONSE ReadSetting status UNKNOWN 4:{1:0}` — a read the Buds reject. */
    const val READ_REJECTED_1441 = "7e00a5032a0422020800080110151dea71de7d5e2551aed0ae300276cfb5057e"

    /** The answers by field, for a scripted Buds. */
    val ANSWERS: Map<Int, String> = mapOf(
        2 to READ_2_RESP, 4 to READ_4_RESP, 7 to READ_7_RESP, 16 to READ_16_RESP, 17 to READ_17_RESP, 19 to READ_19_RESP, 22 to READ_22_RESP,
        12 to Settings056.READ_12_RESP_OFF_UNTICKED_1516, // the same sweep's field-12 answer (frame 1516, request 1514) — ai-sessions/0056
        11 to READ_11_RESP, 15 to READ_15_RESP, 27 to READ_27_RESP, 28 to READ_28_RESP, 29 to READ_29_RESP, // ai-sessions/0074
    )
}

/** Real official-app writes and the Buds' empty `RESPONSE`s (ADR-045). Channel 19 = request address 00 3b; channel 21 = 00 4b. */
internal object SettingsWrites {
    // `CAP-019` (channel 21): conversation detection.
    /** Frame 1720 (07:36:28.596, filmed ON→OFF): `WriteSetting 4:{22:0}`. */
    const val CONV_OFF_1720 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203b00100225fc3b77e"

    /** Frame 1731: the empty `RESPONSE` status OK to 1720 (channel 21). */
    const val ACK_CH21_1731 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"

    /** Frame 1808 (07:36:40.239, ON): `4:{22:1}`. */
    const val CONV_ON_1808 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203b00101b46fc4c07e"

    // `CAP-020` (channel 21): touch controls.
    /** Frame 1741 (07:46:44.850, ON): `4:{4:1}`. */
    const val TOUCH_ON_1741 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422022001c5a08a3c7e"

    /** Frame 1995 (07:47:20.097, filmed ON→OFF): `4:{4:0}`. */
    const val TOUCH_OFF_1995 = "7e004b0310151dea71de7d5e251d9a8c9e2a042202200053908d4b7e"

    // `CAP-021` (channel 19): press-and-hold.
    /** Frame 1895: `4:{7:{1:{4:{1:6}}}}` — Left: Digital assistant. */
    const val HOLD_LEFT_ASSISTANT_1895 = "7e003b0310131dea71de7d5e251d9a8c9e2a0a22083a060a0422020806bcae58b07e"

    /** Frame 3619: `4:{7:{2:{4:{1:6}}}}` — Right: Digital assistant. */
    const val HOLD_RIGHT_ASSISTANT_3619 = "7e003b0310131dea71de7d5e251d9a8c9e2a0a22083a061204220208064a2edd5f7e"

    /** Frame 4315: `4:{7:{1:{4:{1:5}}}}` — Left: Active noise control. */
    const val HOLD_LEFT_ANC_4315 = "7e003b0310131dea71de7d5e251d9a8c9e2a0a22083a060a042202080506ff51297e"

    /** Frame 4976: `4:{7:{2:{4:{1:5}}}}` — Right: Active noise control. */
    const val HOLD_RIGHT_ANC_4976 = "7e003b0310131dea71de7d5e251d9a8c9e2a0a22083a06120422020805f07fd4c67e"

    // `CAP-022` (channel 19): mono and balance.
    /** Frame 1621: `4:{19:1}` — mono on. */
    const val MONO_ON_1621 = "7e003b0310131dea71de7d5e251d9a8c9e2a052203980101acbe2bd07e"

    /** Frame 1823: `4:{19:0}` — mono off. */
    const val MONO_OFF_1823 = "7e003b0310131dea71de7d5e251d9a8c9e2a0522039801003a8e2ca77e"

    /** Frame 1629: the empty `RESPONSE` status OK (channel 19; identical bytes in 1835, 1927, 1950, … 2104 and `CAP-021` 1905 …). */
    const val ACK_CH19_1629 = "7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e"

    /**
     * `CAP-046` (channel 19) frame 1873 (2026-09-12 17:05:09.216, the official app's centre-return sample of Group AK): `WriteSetting 4:{17:0}` —
     * the only real `17:0` write in any capture (`ai-sessions/0054`: `pwrpc_decode.py` over every capture's HCI log, `grep WriteSetting | grep
     * '4:{17:0}'`). Answered by frame 1878, the empty `RESPONSE` status OK, byte-identical to [ACK_CH19_1629].
     */
    const val BALANCE_CENTRE_1873 = "7e003b0310131dea71de7d5e251d9a8c9e2a0522038801004a2d0abb7e"

    /** Frames 1922, 1944, 2019, 2039, 2056, 2073, 2099: the balance drag, `4:{17:n}` → value after zigzag decoding. */
    val BALANCE_DRAG: List<Pair<String, Int>> = listOf(
        "7e003b0310131dea71de7d5e251d9a8c9e2a0622048801c701bcfac4347e" to -100, // 1922, 17:199
        "7e003b0310131dea71de7d5e251d9a8c9e2a05220388017bfe85dd7c7e" to -62, // 1944, 17:123
        "7e003b0310131dea71de7d5e251d9a8c9e2a052203880131702dd4ea7e" to -25, // 2019, 17:49
        "7e003b0310131dea71de7d5e251d9a8c9e2a05220388011e291005417e" to 15, // 2039, 17:30
        "7e003b0310131dea71de7d5e251d9a8c9e2a06220488019601a99664977e" to 75, // 2056, 17:150
        "7e003b0310131dea71de7d5e251d9a8c9e2a0622048801c80173e65cb37e" to 100, // 2073, 17:200
        "7e003b0310131dea71de7d5e251d9a8c9e2a05220388010a54c4df5b7e" to 5, // 2099, 17:10
    )
}

/**
 * `ai-sessions/0074`: the official app's writes and reads of the five switches of ADR-052…055, re-derived from the logs with the frame's CRC-32 checked
 * (`ai-sessions/0074_FEATURE_RESULT_2026_10_06.md` §A.1 — `tshark -r CAP-0NN-btsnoop_hci.log -Y "btrfcomm.len>0 && bthci_acl.chandle==<h> &&
 * frame.number==<n>" -T fields -e frame.number -e frame.time_utc -e data.data`, decoded with `python3 scripts/pwrpc_decode.py`). Times are UTC.
 * Channel 21 = request address 00 4b / response 00 a5; channel 19 = 00 3b / 80 a3. No identifier is carried.
 */
internal object Settings074 {
    // ---- Multipoint, `qhr` field 11 (ADR-053) ----

    /** `CAP-069` frame 3161 (14:12:06.338, filmed OFF tap): `WriteSetting 4:{11:0}` on channel 21; mirrored 3165, empty `RESPONSE` 3170. Same bytes: 3245. */
    const val MP_OFF_CH21_3161 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422025800ad636bac7e"

    /** `CAP-069` frame 3212 (14:12:14.919, filmed ON tap): `4:{11:1}` on channel 21; mirrored 3214, `RESPONSE` 3219. Same bytes: 3275. */
    const val MP_ON_CH21_3212 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220258013b536cdb7e"

    /** `CAP-019` frame 2482 (2026-08-21 05:39:13.783, filmed OFF): `4:{11:0}` on channel 21 — the same bytes as [MP_OFF_CH21_3161], 44 days earlier. */
    const val MP_OFF_CH21_CAP019_2482 = "7e004b0310151dea71de7d5e251d9a8c9e2a0422025800ad636bac7e"

    /** `CAP-019` frame 2293 (05:38:01.609, ON): `4:{11:1}` on channel 21 — the same bytes as [MP_ON_CH21_3212]. */
    const val MP_ON_CH21_CAP019_2293 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220258013b536cdb7e"

    /** `CAP-069` frame 3170 (14:12:06.743): the empty `RESPONSE` status OK to 3161 (channel 21; byte-identical to [SettingsWrites.ACK_CH21_1731]). */
    const val ACK_CH21_3170 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"

    /** `CAP-024` frame 1009 (06:31:38.152): `ReadSetting 4:11` on channel 19 (= `CAP-069` 7270). */
    const val READ_11_REQ_CH19_1009 = "7e003b0310131dea71de7d5e2551aed0ae2a02200b11675aae7e"

    /** `CAP-069` frame 7272 (14:20:10.681): `4:{11:1}` on channel 19 (= `CAP-024` 1014). */
    const val READ_11_RESP_CH19_7272 = "7e80a3032a0422025801080110131dea71de7d5e2551aed0aecd273bcd7e"

    /** `CAP-069` frame 1267 (14:06:00.947): `4:{11:1}` on channel 21 (= `CAP-036` 1513, [Settings036.READ_11_RESP]). */
    const val READ_11_RESP_CH21_1267 = "7e00a5032a0422025801080110151dea71de7d5e2551aed0ae6e85eb5c7e"

    // ---- Head gestures, `qhr` field 29 (ADR-052): 1 = off, 2 = on ----

    /** `CAP-069` frame 2492 (14:09:18.409, filmed OFF tap): `WriteSetting 4:{29:1}` on channel 21; mirrored 2502, `RESPONSE` 2506. Same bytes: 2737, 2923. */
    const val HG_OFF_CH21_2492 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203e80101bc106ba27e"

    /** `CAP-069` frame 2564 (14:09:29.409, filmed ON tap): `4:{29:2}` on channel 21; mirrored 2566, `RESPONSE` 2568. Same bytes: 2831, 3025. */
    const val HG_ON_CH21_2564 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203e801020641623b7e"

    /** `CAP-020` frame 2038 (2026-08-21 05:47:35.122, filmed OFF): `4:{29:1}` on channel 21 — the same bytes as [HG_OFF_CH21_2492]. */
    const val HG_OFF_CH21_CAP020_2038 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203e80101bc106ba27e"

    /** `CAP-020` frame 1935 (05:47:00.005, ON): `4:{29:2}` on channel 21 — the same bytes as [HG_ON_CH21_2564]. */
    const val HG_ON_CH21_CAP020_1935 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203e801020641623b7e"

    /** `CAP-041` frame 2268 (2026-09-06 15:14:42.431, handle 0x0004): `4:{29:1}` on channel 21 — the same bytes as [HG_OFF_CH21_2492]. */
    const val HG_OFF_CH21_CAP041_2268 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203e80101bc106ba27e"

    /** `CAP-069` frame 2506 (14:09:18.765): the empty `RESPONSE` status OK to 2492 (channel 21). */
    const val ACK_CH21_2506 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"

    /** `CAP-024` frame 1097 (06:31:39.791): `ReadSetting 4:29` on channel 19 (= `CAP-069` 7202). */
    const val READ_29_REQ_CH19_1097 = "7e003b0310131dea71de7d5e2551aed0ae2a02201d40d28e5a7e"

    /** `CAP-069` frame 1226 (14:06:00.664): `4:{29:2}` on channel 21 — on (= [Settings036.READ_29_RESP]). */
    const val READ_29_ON_CH21_1226 = "7e00a5032a052203e80102080110151dea71de7d5e2551aed0ae898482177e"

    /** `CAP-069` frame 7209 (14:20:09.097): `4:{29:2}` on channel 19 — on. */
    const val READ_29_ON_CH19_7209 = "7e80a3032a052203e80102080110131dea71de7d5e2551aed0aedac9445f7e"

    /** `CAP-024` frame 1100 (2026-08-21 06:31:39.865): `4:{29:1}` on channel 19 — off. */
    const val READ_29_OFF_CH19_CAP024_1100 = "7e80a3032a052203e80101080110131dea71de7d5e2551aed0aea3a3394e7e"

    /** `CAP-020` frame 1183 (2026-08-21 05:46:22.291): `4:{29:1}` on channel 21 — off. */
    const val READ_29_OFF_CH21_CAP020_1183 = "7e00a5032a052203e80101080110151dea71de7d5e2551aed0aef0eeff067e"

    // ---- Case sounds, `qhr` fields 27 ("Other alerts") and 28 ("Earbuds replaced") (ADR-054) — both channels are real here ----

    /** `CAP-024` frame 2053 (2026-08-21 06:33:02.060): `WriteSetting 4:{27:0}` on channel 19; mirrored 2060, `RESPONSE` 2061. */
    const val CS27_OFF_CH19_2053 = "7e003b0310131dea71de7d5e251d9a8c9e2a052203d80100fa03b6d77e"

    /** `CAP-024` frame 2084 (06:33:12.485): `4:{27:1}` on channel 19; mirrored 2090, `RESPONSE` 2091. */
    const val CS27_ON_CH19_2084 = "7e003b0310131dea71de7d5e251d9a8c9e2a052203d801016c33b1a07e"

    /** `CAP-058` frame 5680 (2026-10-05 19:44:43.023, handle 0x000d, filmed "Other alerts" off): `4:{27:0}` on channel 21; mirrored 5682, `RESPONSE` 5683. */
    const val CS27_OFF_CH21_5680 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203d80100bac507f17e"

    /** `CAP-058` frame 5697 (19:44:52.871, filmed on): `4:{27:1}` on channel 21; mirrored 5701, `RESPONSE` 5702. */
    const val CS27_ON_CH21_5697 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203d801012cf500867e"

    /** `CAP-024` frame 1988 (06:32:38.084): `4:{28:0}` on channel 19 (a tap or the screen opening — the film does not decide, ADR-054); `RESPONSE` 1992. */
    const val CS28_OFF_CH19_1988 = "7e003b0310131dea71de7d5e251d9a8c9e2a052203e00100d2b7cefd7e"

    /** `CAP-024` frame 2023 (06:32:50.500): `4:{28:1}` on channel 19; mirrored 2029, `RESPONSE` 2031. */
    const val CS28_ON_CH19_2023 = "7e003b0310131dea71de7d5e251d9a8c9e2a052203e001014487c98a7e"

    /** `CAP-058` frame 5623 (19:44:25.807): `4:{28:0}` on channel 21; mirrored 5627, `RESPONSE` 5628. */
    const val CS28_OFF_CH21_5623 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203e0010092717fdb7e"

    /** `CAP-058` frame 5643 (19:44:33.955): `4:{28:1}` on channel 21; mirrored 5647, `RESPONSE` 5648. */
    const val CS28_ON_CH21_5643 = "7e004b0310151dea71de7d5e251d9a8c9e2a052203e00101044178ac7e"

    /** `CAP-024` frame 2061 (06:33:02.287): the empty `RESPONSE` OK to 2053 on channel 19 (= [SettingsWrites.ACK_CH19_1629]). */
    const val ACK_CH19_2061 = "7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e"

    /** `CAP-058` frame 5683 (19:44:43.047): the empty `RESPONSE` OK to 5680 on channel 21. */
    const val ACK_CH21_5683 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"

    /** `CAP-024` frames 1089 / 1093 (06:31:39.631 / .699): `ReadSetting 4:27` / `4:28` on channel 19. */
    const val READ_27_REQ_CH19_1089 = "7e003b0310131dea71de7d5e2551aed0ae2a02201b7577edb37e"
    const val READ_28_REQ_CH19_1093 = "7e003b0310131dea71de7d5e2551aed0ae2a02201cd6e2892d7e"

    /** `CAP-024` frames 1092 / 1096 (06:31:39.689 / .783): `4:{27:1}` / `4:{28:1}` on channel 19. */
    const val READ_27_RESP_CH19_1092 = "7e80a3032a052203d80101080110131dea71de7d5e2551aed0ae2408cb777e"
    const val READ_28_RESP_CH19_1096 = "7e80a3032a052203e00101080110131dea71de7d5e2551aed0ae3dec29a87e"

    /** `CAP-058` frames 4514 / 4517 (19:42:27.927 / .975): `4:{27:1}` / `4:{28:1}` on channel 21. */
    const val READ_27_RESP_CH21_4514 = "7e00a5032a052203d80101080110151dea71de7d5e2551aed0ae77450d3f7e"
    const val READ_28_RESP_CH21_4517 = "7e00a5032a052203e00101080110151dea71de7d5e2551aed0ae6ea1efe07e"

    /** `CAP-058` frame 5682 (19:44:43.040): the Buds' own `SubscribeToSettingsChanges` push `4:{27:0}` after 5680 — a real "off" of field 27 on the read side. */
    const val STREAM_27_OFF_CH21_5682 = "7e00a5032a052203d80100080710151dea71de7d5e25f5ad2128a08720ef7e"

    /** `CAP-058` frame 5627 (19:44:26.295): the push `4:{28:0}` after 5623 — a real "off" of field 28 on the read side. */
    const val STREAM_28_OFF_CH21_5627 = "7e00a5032a052203e00100080710151dea71de7d5e25f5ad2128b963c2307e"

    // ---- Volume EQ, `qhr` field 15 (ADR-055) ----

    /** `CAP-022` frame 1871 (2026-08-21 06:16:15.825, filmed off): `WriteSetting 4:{15:0}` on channel 19; mirrored 1876, `RESPONSE` 1877. */
    const val VEQ_OFF_CH19_1871 = "7e003b0310131dea71de7d5e251d9a8c9e2a04220278003fab19517e"

    /** `CAP-022` frame 1895 (06:16:24.090, filmed on): `4:{15:1}` on channel 19; mirrored 1899, `RESPONSE` 1900. */
    const val VEQ_ON_CH19_1895 = "7e003b0310131dea71de7d5e251d9a8c9e2a0422027801a99b1e267e"

    /** `CAP-015` frames 3487 / 3505 (2026-08-18 04:17:19.705 / 04:17:29.417, handle 0x0004, not matched to the film): the same two bytes strings as 1871 / 1895. */
    const val VEQ_OFF_CH19_CAP015_3487 = "7e003b0310131dea71de7d5e251d9a8c9e2a04220278003fab19517e"
    const val VEQ_ON_CH19_CAP015_3505 = "7e003b0310131dea71de7d5e251d9a8c9e2a0422027801a99b1e267e"

    /** `CAP-041` frame 2461 (2026-09-06 15:15:05.973, handle 0x0004): `4:{15:0}` on channel 21; mirrored 2463, `RESPONSE` 2465. */
    const val VEQ_OFF_CH21_CAP041_2461 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220278000f47ef397e"

    /** `CAP-022` frame 1877 (06:16:16.101): the empty `RESPONSE` OK to 1871 (channel 19). */
    const val ACK_CH19_1877 = "7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e"

    /** `CAP-041` frame 2465 (15:15:06.001): the empty `RESPONSE` OK to 2461 (channel 21). */
    const val ACK_CH21_2465 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"

    /** `CAP-024` frame 1031 (06:31:38.684): `ReadSetting 4:15` on channel 19 (= `CAP-069` 7160). */
    const val READ_15_REQ_CH19_1031 = "7e003b0310131dea71de7d5e2551aed0ae2a02200f08a337a97e"

    /** `CAP-024` frame 1038 (06:31:38.756): `4:{15:1}` on channel 19 (= `CAP-069` 7162). */
    const val READ_15_ON_CH19_1038 = "7e80a3032a0422027801080110131dea71de7d5e2551aed0aeb05c04da7e"

    /** `CAP-058` frame 2930 (19:41:24.811, handle 0x0005): `4:{15:1}` on channel 21. */
    const val READ_15_ON_CH21_2930 = "7e00a5032a0422027801080110151dea71de7d5e2551aed0ae13fed44b7e"

    /** `CAP-041` frame 3239 (15:15:41.784, handle 0x0005): `4:{15:0}` on channel 21 — the read after the write 2461 (ADR-055). */
    const val READ_15_OFF_CH21_CAP041_3239 = "7e00a5032a0422027800080110151dea71de7d5e2551aed0aefb252ff27e"
}

/**
 * `ai-sessions/0076`: OpenControl **1.1.0**'s own writes on the wire in `CAP-070` (2026-10-07, the release run, `CAP-070-FINDINGS.md` §3) — the request forms
 * that no official-app capture holds: fields 11 and 29 and the balance `17:7` on channel 19, field 15 "on" on channel 21. Each is the RFCOMM payload of
 * the named frame of `captures/CAP-070-2026-10-07_06-10-41_06-40-59-Group_BF/CAP-070-btsnoop_hci2.log.last` (handle `0x000b`, DLCI 0x02, one RFCOMM frame each):
 * `tshark -r CAP-070-btsnoop_hci2.log.last -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 && frame.number==<n>" -T fields -e frame.number -e frame.time_utc
 * -e data.data`, decoded with `python3 scripts/pwrpc_decode.py --handle 0x000b CAP-070-btsnoop_hci2.log.last`; the CRC-32 of every frame checked
 * (`zlib.crc32` over address, control and payload). Times are UTC (the film's overlay is UTC + 2 h); each write is a filmed tap (`CAP-070-EVENT-NOTES.md`).
 * They are byte-identical to the frames `ai-sessions/0074` derived for these forms. No identifier is carried.
 */
internal object Settings070 {
    /** Frame A3668 (04:21:20.250, filmed tap, BF-14): `WriteSetting 4:{11:0}` (Multipoint off) on channel 19; answered by A3671. */
    const val MP_OFF_CH19_A3668 = "7e003b0310131dea71de7d5e251d9a8c9e2a04220258009d8f9dc47e"

    /** Frame A3679 (04:21:24.246, filmed tap, BF-14): `4:{11:1}` (Multipoint on) on channel 19; answered by A3681. */
    const val MP_ON_CH19_A3679 = "7e003b0310131dea71de7d5e251d9a8c9e2a04220258010bbf9ab37e"

    /** Frame A3614 (04:20:30.497, filmed tap, BF-15): `4:{29:1}` (head gestures off — 1, ADR-052) on channel 19; answered by A3616. */
    const val HG_OFF_CH19_A3614 = "7e003b0310131dea71de7d5e251d9a8c9e2a052203e80101fcd6da847e"

    /** Frame A3685 (04:21:46.224, filmed tap, BF-15): `4:{29:2}` (head gestures on — 2) on channel 19; answered by A3688. */
    const val HG_ON_CH19_A3685 = "7e003b0310131dea71de7d5e251d9a8c9e2a052203e801024687d31d7e"

    /** Frame A3747 (04:22:58.188, a balance drag, BF-16): `4:{17:7}` = zigzag −4 = "Right 4" on channel 19; answered by A3750. */
    const val BAL_R4_CH19_A3747 = "7e003b0310131dea71de7d5e251d9a8c9e2a052203880107e9b86e257e"

    /** Frame A4800 (04:30:57.072, filmed tap, BF-20): `4:{15:1}` (Volume EQ on) on channel 21 — the frame ADR-055 left uncaptured; answered by A4802. */
    const val VEQ_ON_CH21_A4800 = "7e004b0310151dea71de7d5e251d9a8c9e2a04220278019977e84e7e"

    /** Frame A3671 (04:21:20.684): the empty `RESPONSE` status OK to A3668 (channel 19; the same bytes in A3616, A3681, A3688, A3750 and [SettingsWrites.ACK_CH19_1629]). */
    const val ACK_CH19_A3671 = "7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e"

    /** Frame A4802 (04:30:57.160): the empty `RESPONSE` status OK to A4800 (channel 21; byte-identical to [SettingsWrites.ACK_CH21_1731]). */
    const val ACK_CH21_A4802 = "7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e"
}
