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

import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel

/**
 * The Case battery from the Buds' `maestro_pw.Maestro/SubscribeRuntimeInfo` stream on DLCI 0x02 (DECISIONS.md ADR-043; PROTOCOL.md §4.3
 * Option F). The payload is `2:<epoch ms> 3:0 6:{1:{1:<case %> 2:…} 2:{…} 3:{…}} 7:{…}`; 🟢 FACT (maintainer-approved 2026-09-24): entry
 * 6.1, field 1 is the Case percentage — equal to the DLCI 0x08 Case value (ADR-014) in 13 of 13 captures that carry both
 * (`CAP-061-FINDINGS.md` §2a). **Only that value is read**; 6.2/6.3, the entries' field 2 and field 7 are 🔴 and never interpreted.
 */
internal object RuntimeInfoDecoder {
    private const val FIELD_COMPONENTS = 6
    private const val ENTRY_CASE = 1
    private const val FIELD_LEVEL = 1

    /**
     * The Case reading of one stream packet's payload: [BatteryLevel.Known] for a level in `0..100`, [BatteryLevel.Unavailable] when the
     * packet carries no Case entry (it is absent in many captures) or another value, `null` when the payload is not of this shape (the
     * caller reports it as unidentified). Never throws (`AGENTS.md` §11).
     */
    fun caseBattery(payload: ByteArray): BatteryLevel? {
        val top = Proto.fields(payload) ?: return null
        val components = top.firstOrNull { it.number == FIELD_COMPONENTS && it.bytes != null }?.bytes ?: return null
        val entries = Proto.fields(components) ?: return null
        val case = entries.firstOrNull { it.number == ENTRY_CASE && it.bytes != null }?.bytes ?: return BatteryLevel.Unavailable
        val level = Proto.fields(case)?.firstOrNull { it.number == FIELD_LEVEL && it.bytes == null }?.varint
        return if (level != null && level in 0..100) BatteryLevel.Known(level, isCharging = null, isStale = false) else BatteryLevel.Unavailable
    }
}
