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
 * second field: HFP Option C (this session's only implemented battery
 * source) never confirms a charging bit anywhere in PROTOCOL.md §4.3 — only
 * `null` ("unknown") is honest for a value sourced from that mechanism;
 * defaulting it to `false` would silently fabricate "not charging" the same
 * way a fabricated percentage would.
 */
sealed class BatteryLevel {
    data class Known(val percent: Int, val isCharging: Boolean?) : BatteryLevel()
    data object Unavailable : BatteryLevel()
}

/**
 * Per-component battery state (PROTOCOL.md §4.3). All fields default to
 * [BatteryLevel.Unavailable] — a fresh [BudsRepository] must never start out
 * claiming a known value it hasn't actually received yet.
 *
 * [hfpEarbud] exists separately from [left]/[right] because HFP `AT+BIEV`
 * (Option C, this session's only implemented battery source) is confirmed
 * per-earbud, not aggregate (DECISIONS.md ADR-015) — but *which* physical
 * earbud it reports (a fixed side, or whichever is currently HFP-primary) is
 * still 🟡 HYPOTHESIS, unresolved across two sessions that each saw a
 * different side (PROTOCOL.md §4.3 Option C). Guessing a side would violate
 * AGENTS.md §5's "never fabricate a value" rule just as much as inventing a
 * percentage would — so this value is surfaced honestly as
 * "one earbud, side unknown" rather than forced into [left] or [right].
 */
data class BatteryStatus(
    val left: BatteryLevel = BatteryLevel.Unavailable,
    val right: BatteryLevel = BatteryLevel.Unavailable,
    val case: BatteryLevel = BatteryLevel.Unavailable,
    val hfpEarbud: BatteryLevel = BatteryLevel.Unavailable,
)
