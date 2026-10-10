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
 * OpenControl 1.2.0's own `GetHardwareInfo` exchange (DECISIONS.md ADR-058) on both control channels, from `CAP-072` (`ai-sessions/0084` item 2, replacing the
 * official app's frames of `CAP-036` 1415/1423 and `CAP-024` 801/832 that `ai-sessions/0082` used). "A" = `CAP-072-btsnoop_hci2.log.last`, the Buds' handle
 * `0x000b` (Connection Complete A 5491). Command: `tshark -r CAP-072-btsnoop_hci2.log.last -Y "frame.number in {7593,7596,8616,8620}" -T fields -e
 * frame.number -e data.data` (exit 0); the pw_hdlc frames split on `0x7e`, unescaped and CRC-32-checked by the scratch parser of `ai-sessions/0084` RESULT §A.3
 * — all four OK.
 *
 * The **requests are the real frames** (they carry no identifier) — byte-identical to the official app's `CAP-036` 1415 and `CAP-024` 801. The **answers are
 * the real frames with the serial numbers redacted**: serials are device identifiers (`AGENTS.md` §9, §11 — "redact any real device identifiers first"), so in
 * each of the three length-14 strings the eight middle characters are replaced by `X` (the first four and the last two kept) and **the CRC-32 is re-sealed**
 * over the redacted bytes — the real frames' CRC (`36 db 71 c0`, `a4 a6 c5 17`) no longer matches them; the envelope, every tag, length and other field are
 * byte-identical to the log. The real answer frames are byte-identical to the official app's `CAP-036` 1423 / `CAP-024` 832 (the Buds answer every client the
 * same), so the redacted constants are the same bytes `ai-sessions/0082` committed — only their source is now OpenControl's own run.
 */
internal object HardwareInfoFixtures {
    /** A 7593 (17:29:27.052338), phone → Buds, channel 21, address `00 4b`: the whole pw_hdlc frame of the request (no payload). */
    const val REQUEST_CH21_CAP072_A7593 = "7e004b0310151dea71de7d5e25e3a5ec28f96761b57e"

    /** A 8616 (17:35:57.196041), phone → Buds, channel 19, address `00 3b`: the whole RFCOMM payload, one pw_hdlc frame (the request, no payload). */
    const val REQUEST_CH19_CAP072_A8616 = "7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e"

    /**
     * A 7596 (17:29:27.087702, 35 ms after the request), Buds → phone, channel 21, address `00 a5`: the second pw_hdlc frame of that RFCOMM payload — `RESPONSE`
     * `1:6 2:17 5:9 6:7 7:{1:"5707…51" 2:"5708…09" 3:"5707…47"} 8:{…}` — **serials redacted**, CRC re-sealed.
     */
    const val RESPONSE_CH21_CAP072_A7596_REDACTED =
        "7e00a5032a4408061011280930073a300a0e3537303758585858585858583531120e35373038585858585858585830391a0e353730375858585858585858343742083030313332" +
            "303933080110151dea71de7d5e25e3a5ec28276dfa917e"

    /**
     * A 7596's **whole RFCOMM payload**: a runtime-info `SERVER_STREAM` packet (`SubscribeRuntimeInfo`, ADR-043 — the Case 57 %, both buds charging; no
     * identifier, real bytes and CRC) followed by [RESPONSE_CH21_CAP072_A7596_REDACTED] — two pw_hdlc frames in one read, as the Buds sent them.
     */
    const val RFCOMM_CH21_CAP072_A7596_REDACTED =
        "7e00a5032a1e180032120a04083910011204086410021a04086410023a06080110011800080710151dea71de7d5e2590821ee6531f0cd47e" +
            RESPONSE_CH21_CAP072_A7596_REDACTED

    /**
     * A 8620 (17:35:57.355802, 160 ms after the request), Buds → phone, channel 19, address `80 a3`: the only pw_hdlc frame of that RFCOMM payload — the same
     * three strings in the same order (field 8 differs: `…94` instead of `…93`) — **serials redacted**, CRC re-sealed.
     */
    const val RESPONSE_CH19_CAP072_A8620_REDACTED =
        "7e80a3032a4408061011280930073a300a0e3537303758585858585858583531120e35373038585858585858585830391a0e353730375858585858585858343742083030313332" +
            "303934080110131dea71de7d5e25e3a5ec28b5104e467e"

    /** The three strings as the redacted answers carry them, in field order 1, 2, 3 (Case, Right bud, Left bud per the official app's reading). */
    val SERIALS_REDACTED: List<String> = listOf("5707XXXXXXXX51", "5708XXXXXXXX09", "5707XXXXXXXX47")
}
