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
 * The official app's `GetHardwareInfo` exchange (DECISIONS.md ADR-058, `ai-sessions/0082`) on both control channels. The **requests are the real frames**
 * (they carry no identifier). The **answers are the real frames with the serial numbers redacted**: serials are device identifiers (`AGENTS.md` §9, §11 —
 * "redact any real device identifiers first"), so in each of the three length-14 strings the eight middle characters are replaced by `X` (the first four and
 * the last two kept) and the CRC-32 is re-sealed — the envelope, every tag, length and other field are byte-identical to the log. The session that wrote
 * these fixtures verified the real frames' CRC-32 against the logs (`ai-sessions/0082` RESULT §A.2, §D). Commands:
 * `tshark -r CAP-036-btsnoop_hci.log -Y 'frame.number==1415 || frame.number==1423' -T fields -e frame.number -e frame.time_utc -e frame.p2p_dir
 * -e bthci_acl.chandle -e btrfcomm.dlci -e data.data` (handle `0x0005`, Connection Complete 906) and the same for `CAP-024` 801/832 (handle `0x0002`, 383);
 * `python3 scripts/pwrpc_decode.py <log> | grep GetHardwareInfo`.
 */
internal object HardwareInfoFixtures {
    /** `CAP-036` frame 1415 (04:36:32.628142 UTC), phone → Buds, channel 21, address `00 4b`: the whole pw_hdlc frame of the request (no payload). */
    const val REQUEST_CH21_CAP036_1415 = "7e004b0310151dea71de7d5e25e3a5ec28f96761b57e"

    /** `CAP-024` frame 801 (06:31:37.346823 UTC), phone → Buds, channel 19, address `00 3b`: the second pw_hdlc frame of that RFCOMM frame (the first is `ReadSetting 4:13`). */
    const val REQUEST_CH19_CAP024_801 = "7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e"

    /**
     * `CAP-036` frame 1423 (04:36:32.711117 UTC, 83 ms after the request), Buds → phone, channel 21, address `00 a5`: the first pw_hdlc frame of that RFCOMM
     * frame — `RESPONSE` `1:6 2:17 5:9 6:7 7:{1:"5707…51" 2:"5708…09" 3:"5707…47"} 8:{…}` — **serials redacted**, CRC re-sealed (the real frame: CRC `36 db 71 c0`,
     * verified OK).
     */
    const val RESPONSE_CH21_CAP036_1423_REDACTED =
        "7e00a5032a4408061011280930073a300a0e3537303758585858585858583531120e35373038585858585858585830391a0e353730375858585858585858343742083030313332" +
            "303933080110151dea71de7d5e25e3a5ec28276dfa917e"

    /**
     * `CAP-024` frame 832 (06:31:37.429407 UTC, 83 ms after the request), Buds → phone, channel 19, address `80 a3`: the second pw_hdlc frame of that RFCOMM
     * frame — the same three strings in the same order (field 8 differs: `…94` instead of `…93`) — **serials redacted**, CRC re-sealed (the real frame: CRC
     * `a4 a6 c5 17`, verified OK).
     */
    const val RESPONSE_CH19_CAP024_832_REDACTED =
        "7e80a3032a4408061011280930073a300a0e3537303758585858585858583531120e35373038585858585858585830391a0e353730375858585858585858343742083030313332" +
            "303934080110131dea71de7d5e25e3a5ec28b5104e467e"

    /** The three strings as the redacted answers carry them, in field order 1, 2, 3 (Case, Right bud, Left bud per the official app's reading). */
    val SERIALS_REDACTED: List<String> = listOf("5707XXXXXXXX51", "5708XXXXXXXX09", "5707XXXXXXXX47")
}
