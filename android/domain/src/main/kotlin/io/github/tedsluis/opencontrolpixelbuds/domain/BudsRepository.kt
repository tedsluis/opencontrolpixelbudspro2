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

    /** Wall-clock time ([System.currentTimeMillis]) the current [ancMode] value was received, or `null`
     * before any value has arrived this app run (`ai-sessions/0043` Phase H — replaces a vague "last
     * known" qualifier with an actual timestamp; recorded only at the moment a value is received, no
     * polling). */
    val ancModeUpdatedAt: Flow<Long?>

    /** `null` = no value read yet this connection (ARCHITECTURE.md §3.1): the Connect sequence reads it with
     * `ReadSetting 4:16` (ADR-034), so it stays `null` only until that answer arrives or if it failed — see [eqError]. */
    val eqProfile: Flow<EqBandGains?>

    /** Wall-clock time the current [eqProfile] value was received; `null` when [eqProfile] is `null`. */
    val eqProfileUpdatedAt: Flow<Long?>

    val batteryStatus: Flow<BatteryStatus>

    /** Wall-clock time [batteryStatus] was last updated (any of Left/Right/Case), or `null` before any
     * reading has arrived this app run. */
    val batteryStatusUpdatedAt: Flow<Long?>

    /**
     * Why the Case battery could not be read by the last on-demand claim of DLCI 0x08 (`null` = it was read, or none was
     * attempted this connection) — DECISIONS.md ADR-035. Distinct from [messageStreamError]: a different, shared channel.
     */
    val caseBatteryError: Flow<BudsError?>

    /** Whether the earbuds sit in the case, from the last `Notify ANC state` (DECISIONS.md ADR-024) — [DockState.UNKNOWN] until one arrived. */
    val dockState: Flow<DockState>

    /** Wall-clock time [dockState] was last updated, or `null` before any `Notify` has arrived this app run. */
    val dockStateUpdatedAt: Flow<Long?>

    /** What the Buds announced at connect (firmware), or `null` before the announcement / after a disconnect. */
    val deviceInfo: Flow<DeviceInfo?>

    /** The earbud a Find My Buds ring was last started on and not yet stopped this connection (`null` = none) — the ring keeps sounding on
     * the Buds after the channel is released (`ai-sessions/0042`: heard on the recording), so the UI must say so. */
    val ringingTarget: Flow<RingTarget?>

    /**
     * Why the last EQ read or write did not succeed (`null` = it did, or none was attempted this connection) —
     * shown on the EQ screen instead of silently assuming success (`ai-sessions/0041`, DECISIONS.md ADR-034).
     */
    val eqError: Flow<BudsError?>

    /**
     * Why the **Message Stream channel** (DLCI 0x04, used by ANC, Find My Buds and battery) could not
     * be claimed for the most recent action that needed it — `null` when the last claim succeeded or
     * none has been attempted (DECISIONS.md ADR-032). Distinct from [connectionState]: the session (the
     * MAESTRO channel) can be perfectly healthy while another app (Google Play services' Fast Pair)
     * holds this shared channel.
     */
    val messageStreamError: Flow<BudsError?>
    val unidentifiedFrames: Flow<UnidentifiedFrame>

    /** Connects to the already-bonded Buds (ARCHITECTURE.md §9.0a step 6) —
     * fails with [BudsError.PermissionDenied] if no bonded device exists yet
     * (pairing is a separate, prior step, `ai-sessions/0036`). */
    suspend fun connect(): BudsResult<Unit>
    suspend fun disconnect(): BudsResult<Unit>

    /**
     * Re-reads the battery on the user's request: a short Message Stream claim (Left/Right, ADR-033) and a short claim of
     * DLCI 0x08 for the Case (ADR-035). Requires an open session.
     */
    suspend fun refreshBattery(): BudsResult<Unit>

    suspend fun setAncMode(mode: AncMode): BudsResult<Unit>
    suspend fun refreshAncMode(): BudsResult<AncMode>

    /** Reads the Buds' active EQ (`ReadSetting 4:16`, ADR-034) and updates [eqProfile]. Requires an open session. */
    suspend fun refreshEq(): BudsResult<EqBandGains>

    suspend fun setEqGains(gains: EqBandGains): BudsResult<Unit>
    suspend fun applyEqPreset(preset: EqPreset): BudsResult<Unit>

    suspend fun ringBud(target: RingTarget): BudsResult<Unit>
    suspend fun stopRinging(): BudsResult<Unit>
}
