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

    /** Frame 1525: `4:{16:[0.1, 0.0, 0.3, 0.2, 0.2]}` — the EQ read answer of the same sweep. */
    const val READ_16_RESP =
        "7e00a5032a1e221c8201190dc0cccc3d15000000001da099993e25c0cc4c3e2dc0cc4c3e080110151dea71de7d5e2551aed0ae85b618ed7e"

    /** Frame 1441, the 4th pw_hdlc frame of that RFCOMM payload: `RESPONSE ReadSetting status UNKNOWN 4:{1:0}` — a read the Buds reject. */
    const val READ_REJECTED_1441 = "7e00a5032a0422020800080110151dea71de7d5e2551aed0ae300276cfb5057e"

    /** The answers by field, for a scripted Buds. */
    val ANSWERS: Map<Int, String> = mapOf(
        2 to READ_2_RESP, 4 to READ_4_RESP, 7 to READ_7_RESP, 16 to READ_16_RESP, 17 to READ_17_RESP, 19 to READ_19_RESP, 22 to READ_22_RESP,
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
