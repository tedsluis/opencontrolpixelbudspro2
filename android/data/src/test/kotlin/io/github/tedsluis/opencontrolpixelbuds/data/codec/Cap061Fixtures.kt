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
 * Real bytes from `CAP-061` (`captures/CAP-061-2026-09-24_17-24-59_17-31-14-Group_AW/`), shared by the codec and repository tests.
 *
 * [ANNOUNCEMENT_RPC_PACKET] is HCI frame 1508 — the Buds' unsolicited `GetSoftwareInfo` RESPONSE on DLCI 0x02, channel 21 — HDLC-unescaped,
 * between the control byte `03` and the CRC. Derivation (`ai-sessions/0046`): `tshark -r CAP-061-btsnoop_hci.log -Y "frame.number==1508"
 * -T fields -e data.data`, split on `7e`, unescape `7d x` → `x ^ 0x20`; the CRC-32 of the unescaped `00 a5 03 …` equals the frame's own
 * `e8 a9 58 66`. **Redacted (`AGENTS.md` §11):** the 10-digit serial in each of the three entries (field 1) is replaced by `0000000000`
 * (same length); every other byte is the wire's own, including field 5 — tag `0x29`, wire type 1 (fixed64) — whose value
 * `34 29 3f c2 f6 cb d8 1a` is identical in `CAP-001`/`CAP-036`/`CAP-050`/`CAP-061` and is already in the committed captures.
 * Structure: `2a 64` (payload) { `22 57` field 4 {3 × `{1: serial, 2: "release_5.203"}`}, `29 <8 bytes>` field 5, `30 00` field 6 },
 * `08 01` RESPONSE, `10 15` channel 21, service `maestro_pw.Maestro`, method `GetSoftwareInfo`, `38 ff ff ff ff 0f` call id 0xFFFFFFFF.
 */
internal object Cap061 {
    const val ANNOUNCEMENT_RPC_PACKET: String =
        "2a6422570a1b0a0a30303030303030303030120d72656c656173655f352e323033" +
            "121b0a0a30303030303030303030120d72656c656173655f352e323033" +
            "1a1b0a0a30303030303030303030120d72656c656173655f352e323033" +
            "2934293fc2f6cbd81a3000" +
            "080110151dea71de7e2544fa997138ffffffff0f"

    /** The pw_hdlc response address of channel 21 (`00 a5`, a one-terminated varint = 10496; ADR-034's table). */
    const val CHANNEL_21_RESPONSE_ADDRESS: Int = 10496

    /** Frame 1508 re-framed (HDLC flag/escape/CRC recomputed over the redacted packet). */
    fun announcementFrame(): ByteArray = Hdlc.encode(CHANNEL_21_RESPONSE_ADDRESS, PW_HDLC_CONTROL_UI, hex(ANNOUNCEMENT_RPC_PACKET))

    /** The RpcPacket payload (field 5) alone — what [SoftwareInfo.firmwareStrings] receives. */
    fun announcementPayload(): ByteArray = hex(ANNOUNCEMENT_RPC_PACKET).copyOfRange(2, 2 + 0x64)
}
