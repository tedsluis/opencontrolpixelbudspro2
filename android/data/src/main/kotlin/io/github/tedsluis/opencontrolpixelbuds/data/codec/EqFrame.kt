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

import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains

/**
 * One EQ quintet inside a `maestro_pw.Maestro` pw_rpc packet on DLCI 0x02 (PROTOCOL.md §4.2, DECISIONS.md
 * ADR-013/016/020/034). The whole packet is `RpcPacket{channel_id, service, method, payload}` (see [PwRpc]) wrapped in
 * a pw_hdlc frame (see [Hdlc]); its `payload` is the Maestro settings message
 *
 * ```
 * [0x22 <len>]                                            -- field 4 (the setting oneof wrapper)
 * [<varint tag: field 16 or 18, wiretype 2> <len=25>]     -- 16 = active EQ, 18 = last-saved custom EQ (ADR-034)
 * [(tag:1 wiretype5 + float32LE:4)] x5                    -- fields 1..5 = Low bass/Bass/Mid/Treble/Upper treble
 * ```
 *
 * `persist == false` is field 16, `true` is field 18. **[channelId] is the pw_rpc channel** — earlier code called the
 * same byte a "correlation byte" and defaulted it to 0, which no capture ever uses (channels are 19/21/24/26, chosen
 * per connection — ADR-034). It has no default on purpose.
 *
 * // TODO(verify): field 16 vs. 18 write semantics ("preview" vs. "slider-release" vs. "Save button") remain
 * // 🟡 HYPOTHESIS (ADR-020's open item); do not ship a "Save as preset" affordance against [persist] without
 * // re-checking PROTOCOL.md §4.2/§6.
 */
data class EqFrame(
    val gains: EqBandGains,
    val persist: Boolean = false,
    val channelId: Int,
)
