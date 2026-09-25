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
 * One `SubscribeRuntimeInfo` stream packet as far as this app reads it (`ai-sessions/0048`, I-4/I-5).
 *
 * @param case entry 6.1: [BatteryLevel.Known] (Case %), [BatteryLevel.Unavailable] (present, value not `0..100`), or `null` when the packet
 * carries **no** 6.1 — which the Buds do whenever no bud is charging (PROTOCOL.md §4.3 Option F, 397/403; `CAP-062` frames 2782, 7118).
 * @param leftCharging / [rightCharging] whether that bud is charging (🟢 FACT: 6.2/6.3 field 2 = `2` exactly when its charging bit is set, 401/403;
 * 7.2/7.1 = `1` when charging, 397/403); `null` = not reported. 🟡 "charging" = "in the case" (untested with an empty case).
 */
data class RuntimeInfo(val case: BatteryLevel?, val leftCharging: Boolean?, val rightCharging: Boolean?)

/**
 * The Buds' `maestro_pw.Maestro/SubscribeRuntimeInfo` stream on DLCI 0x02 (DECISIONS.md ADR-043 and its 2026-09-25 Update; PROTOCOL.md §4.3
 * Option F). The payload is `2:<epoch ms> 3:0 6:{1:{1:<case %> 2:…} 2:{1:<%> 2:1|2} 3:{1:<%> 2:1|2}} 7:{1:0|1 2:0|1 3:…}`.
 *
 * Read — and nothing else (field 3, the entries' field 1 for the buds, field 7.3 stay uninterpreted; the bud percentages come from DLCI 0x04):
 * - 6.1 field 1 = Case % (🟢, `CAP-061-FINDINGS.md` §2a, 13/13 + `CAP-062`);
 * - 6.2 = Left, 6.3 = Right, their field 2: `2` = charging, `1` = not (🟢, 401/403);
 * - 7.2 = Left, 7.1 = Right: `1` = charging, `0` = not (🟢, 397/403) — used only when 6.x field 2 is absent or another value. Where the two
 *   disagree **6.x field 2 wins** (agent detail, `ai-sessions/0048`): it agrees with the DLCI 0x04 charging bit in 401 of 403 packets against
 *   397 for 7.x, and in `CAP-062` frame 4500 (6.3.2 = 2, 7.1 = 0) the battery frame 4520 0.45 s later reads Right charging (`64 e4`).
 */
internal object RuntimeInfoDecoder {
    private const val FIELD_COMPONENTS = 6
    private const val FIELD_IN_CASE = 7
    private const val ENTRY_CASE = 1
    private const val ENTRY_LEFT = 2
    private const val ENTRY_RIGHT = 3
    private const val FIELD_LEVEL = 1
    private const val FIELD_CHARGING = 2
    private const val IN_CASE_RIGHT = 1
    private const val IN_CASE_LEFT = 2

    /** The packet's readable parts, or `null` when the payload is not of this shape (the caller reports it as unidentified). Never throws. */
    fun decode(payload: ByteArray): RuntimeInfo? {
        val top = Proto.fields(payload) ?: return null
        val components = top.firstOrNull { it.number == FIELD_COMPONENTS && it.bytes != null }?.bytes ?: return null
        val entries = Proto.fields(components) ?: return null
        fun entry(number: Int): List<Proto.Field>? = entries.firstOrNull { it.number == number && it.bytes != null }?.bytes?.let(Proto::fields)

        val case = entries.firstOrNull { it.number == ENTRY_CASE && it.bytes != null }?.let {
            val level = entry(ENTRY_CASE)?.firstOrNull { f -> f.number == FIELD_LEVEL && f.bytes == null }?.varint
            if (level != null && level in 0..100) BatteryLevel.Known(level, isCharging = null, isStale = false) else BatteryLevel.Unavailable
        }
        val inCase = top.firstOrNull { it.number == FIELD_IN_CASE && it.bytes != null }?.bytes?.let(Proto::fields)
        fun charging(entryNumber: Int, inCaseNumber: Int): Boolean? {
            when (entry(entryNumber)?.firstOrNull { it.number == FIELD_CHARGING && it.bytes == null }?.varint) {
                2 -> return true
                1 -> return false
            }
            return when (inCase?.firstOrNull { it.number == inCaseNumber && it.bytes == null }?.varint) {
                1 -> true
                0 -> false
                else -> null
            }
        }
        return RuntimeInfo(case, charging(ENTRY_LEFT, IN_CASE_LEFT), charging(ENTRY_RIGHT, IN_CASE_RIGHT))
    }
}
