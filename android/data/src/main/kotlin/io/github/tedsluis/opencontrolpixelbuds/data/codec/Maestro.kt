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
 * The pw_rpc service every DLCI 0x02 packet belongs to (`maestro_pw.Maestro`) and the methods the captures
 * name (PROTOCOL.md §2.2a, DECISIONS.md ADR-034). Ids are the 65599 name hashes — computed here, and
 * asserted against the literals seen on the wire in `PwRpcTest`.
 */
object Maestro {
    val SERVICE_ID: Int = PwRpc.nameHash("maestro_pw.Maestro") // 0x7ede71ea
    val METHOD_WRITE_SETTING: Int = PwRpc.nameHash("WriteSetting") // 0x9e8c9a1d
    val METHOD_READ_SETTING: Int = PwRpc.nameHash("ReadSetting") // 0xaed0ae51
    val METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES: Int = PwRpc.nameHash("SubscribeToSettingsChanges") // 0x2821adf5
    val METHOD_GET_SOFTWARE_INFO: Int = PwRpc.nameHash("GetSoftwareInfo") // 0x7199fa44

    // The official app's connect-time burst (PROTOCOL.md §6, 🟢 FACT 2026-09-24, `CAP-036` frames 1404–1570). GetHardwareInfo and
    // SetWallclock are names only, for the debug log's method column — this app never sends them. SubscribeRuntimeInfo is sent once
    // per Connect for the Case battery (DECISIONS.md ADR-043).
    val METHOD_GET_HARDWARE_INFO: Int = PwRpc.nameHash("GetHardwareInfo") // 0x28eca5e3
    val METHOD_SUBSCRIBE_RUNTIME_INFO: Int = PwRpc.nameHash("SubscribeRuntimeInfo") // 0xe61e8290
    val METHOD_SET_WALLCLOCK: Int = PwRpc.nameHash("SetWallclock") // 0x673bed4e

    /** `qhr` field numbers for the EQ (PROTOCOL.md §4.2, ADR-034): 16 = the active quintet, 18 = the last-saved custom one. */
    const val FIELD_EQ_ACTIVE = 16
    const val FIELD_EQ_SAVED = 18

    /** The EQ quintet fields (the only ones [EqFrameDecoder] reads). */
    val EQ_FIELDS: Set<Int> = setOf(FIELD_EQ_ACTIVE, FIELD_EQ_SAVED)

    /**
     * The only settings a read may name: EQ 16/18 (ADR-034) and — `ai-sessions/0052`, ADR-036 — 2 (in-ear detection setting), 4 (touch
     * controls), 7 (press-and-hold), 17 (balance), 19 (mono), 22 (conversation detection). Not 11, 15, 27, 28 (allowed by ADR-036, not asked
     * for) and never 12 (not in ADR-036; its bit order is disputed, PROTOCOL.md §4.5.3).
     */
    val READABLE_FIELDS: Set<Int> = EQ_FIELDS + setOf(
        SettingsCodec.FIELD_IN_EAR_DETECTION,
        SettingsCodec.FIELD_TOUCH_CONTROLS,
        SettingsCodec.FIELD_PRESS_AND_HOLD,
        SettingsCodec.FIELD_VOLUME_BALANCE,
        SettingsCodec.FIELD_MONO_AUDIO,
        SettingsCodec.FIELD_CONVERSATION_DETECTION,
    )

    /**
     * `ReadSetting` request for one `qhr` field: payload = protobuf `4:N` (`20 <N>`), byte-identical to
     * `CAP-036` frame 1523 (`10 15 1d ea 71 de 7e 25 51 ae d0 ae 2a 02 20 10`) and, for the settings, frames 1445/1451/1457/1526/1532/1538.
     * Returns null for a field not in [READABLE_FIELDS] — a caller can never send an arbitrary setting read.
     */
    fun readSettingRequest(channelId: Int, field: Int): RpcPacket? {
        if (field !in READABLE_FIELDS) return null
        return RpcPacket(
            type = PwRpc.TYPE_REQUEST,
            channelId = channelId,
            serviceId = SERVICE_ID,
            methodId = METHOD_READ_SETTING,
            payload = byteArrayOf(0x20, field.toByte()),
        )
    }

    /**
     * `SubscribeRuntimeInfo` request (DECISIONS.md ADR-043): no payload, no call id — byte-identical to the official app's `CAP-036` frame
     * 1410 (`10 15 1d ea 71 de 7e 25 90 82 1e e6` on channel 21). The Buds answer with `SERVER_STREAM` packets on their own afterwards.
     */
    fun subscribeRuntimeInfoRequest(channelId: Int): RpcPacket = RpcPacket(
        type = PwRpc.TYPE_REQUEST,
        channelId = channelId,
        serviceId = SERVICE_ID,
        methodId = METHOD_SUBSCRIBE_RUNTIME_INFO,
    )
}

/**
 * The (channel id, request HDLC address) pairs seen in the captures (PROTOCOL.md §2.2a: 19 ↔ `00 3b`, 21 ↔ `00 4b`,
 * 24 ↔ `80 3d`, 26 ↔ `80 4d`). The address is **not derived** from the channel — only tabulated — so a channel
 * outside this table has no known address and [forChannel] returns null (ADR-034: never guess).
 */
data class MaestroChannel(val channelId: Int, val requestAddress: Int) {
    companion object {
        private val KNOWN = listOf(
            MaestroChannel(19, 3712), // 00 3b
            MaestroChannel(21, 4736), // 00 4b
            MaestroChannel(24, 3904), // 80 3d
            MaestroChannel(26, 4928), // 80 4d
        ).associateBy { it.channelId }

        fun forChannel(channelId: Int): MaestroChannel? = KNOWN[channelId]
    }
}

/** HDLC control byte of every captured pw_hdlc frame (PROTOCOL.md §2.2a: 304 of 304 in `CAP-015`). */
const val PW_HDLC_CONTROL_UI = 0x03
