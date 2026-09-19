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

import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing surface onto the Buds, implemented in `:data` (ARCHITECTURE.md
 * §2.1). Local state exposed here is a cache of the hardware's last-known
 * state, never the authority on it (ARCHITECTURE.md §3.1) — [ancMode] is
 * reconciled against a fresh read on every (re)connection, not assumed to
 * still hold across a reconnect. See ARCHITECTURE.md §3.1's per-feature table
 * for exactly how each Flow below is reconciled (push-based for battery,
 * query/response for ANC, provisional-until-an-unsolicited-update for EQ).
 */
interface BudsRepository {
    val connectionState: Flow<ConnectionState>

    /**
     * Why the most recent session ended without the user asking for it (an open channel died) —
     * `null` after a fresh [connect] starts, after a successful connect, and after an explicit
     * [disconnect]. Exists so a drop back to plain [ConnectionState.Disconnected] never happens
     * without a visible explanation (`ai-sessions/0039`); a failed *attempt* is reported through
     * [ConnectionState.Failed] instead, not through this flow.
     */
    val lastConnectionError: Flow<BudsError?>
    val ancMode: Flow<AncMode>

    /** `null` = no confirmed value yet this connection (ARCHITECTURE.md §3.1 —
     * EQ has no confirmed read/query opcode, unlike ANC, so this starts
     * unknown on every fresh connection rather than trusting a stale cache). */
    val eqProfile: Flow<EqBandGains?>

    val batteryStatus: Flow<BatteryStatus>
    val unidentifiedFrames: Flow<UnidentifiedFrame>

    /** Connects to the already-bonded Buds (ARCHITECTURE.md §9.0a step 6) —
     * fails with [BudsError.PermissionDenied] if no bonded device exists yet
     * (pairing is a separate, prior step, `ai-sessions/0036`). */
    suspend fun connect(): BudsResult<Unit>
    suspend fun disconnect(): BudsResult<Unit>

    suspend fun setAncMode(mode: AncMode): BudsResult<Unit>
    suspend fun refreshAncMode(): BudsResult<AncMode>

    suspend fun setEqGains(gains: EqBandGains): BudsResult<Unit>
    suspend fun applyEqPreset(preset: EqPreset): BudsResult<Unit>

    suspend fun ringBud(target: RingTarget): BudsResult<Unit>
    suspend fun stopRinging(): BudsResult<Unit>
}
