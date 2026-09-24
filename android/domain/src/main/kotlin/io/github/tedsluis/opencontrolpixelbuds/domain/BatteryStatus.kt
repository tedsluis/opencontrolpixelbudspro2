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
package io.github.tedsluis.opencontrolpixelbuds.domain

/**
 * One component's battery reading. [Unavailable] is a first-class case, not a
 * null/zero placeholder — AGENTS.md §5 requires the UI to show "Battery
 * unavailable" rather than ever fabricate or carry over a stale percentage
 * silently. [isCharging] is nullable for the identical reason applied to a
 * second field: a source that carries no charging bit (the DLCI 0x08 Case push,
 * ADR-035) must report `null` ("unknown"); defaulting it to `false` would
 * silently fabricate "not charging" the same way a fabricated percentage would.
 * (The Message Stream battery message does carry one, ADR-033.)
 */
sealed class BatteryLevel {
    /**
     * @param isStale `true` when the Buds reported this value without marking it fresh — shown as "last seen", never as current
     * (`ai-sessions/0042`, DECISIONS.md ADR-035: the Case value the Buds keep while they are out of the case).
     */
    data class Known(val percent: Int, val isCharging: Boolean?, val isStale: Boolean = false) : BatteryLevel()
    data object Unavailable : BatteryLevel()
}

/**
 * Per-component battery state (PROTOCOL.md §4.3). All fields default to
 * [BatteryLevel.Unavailable] — a fresh [BudsRepository] must never start out
 * claiming a known value it hasn't actually received yet.
 *
 * (HFP `AT+BIEV`, which reports one earbud of unconfirmed side, is not consumed by the app any more — removed `ai-sessions/0042`.)
 */
data class BatteryStatus(
    val left: BatteryLevel = BatteryLevel.Unavailable,
    val right: BatteryLevel = BatteryLevel.Unavailable,
    val case: BatteryLevel = BatteryLevel.Unavailable,
)
