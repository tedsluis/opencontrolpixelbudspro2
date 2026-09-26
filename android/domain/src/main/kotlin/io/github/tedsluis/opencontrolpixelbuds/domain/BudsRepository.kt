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
 * for exactly how each Flow below is reconciled (push-based battery on each
 * on-demand claim, a `Get`/`Notify` query per Message Stream claim for ANC,
 * a `ReadSetting` at Connect for EQ).
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

    /**
     * Why the last session loss happened, as far as Android's link state around it shows ([classifySessionLoss], `ai-sessions/0048` I-7) —
     * `null` when [lastConnectionError] has nothing to explain. Re-evaluated whenever a new [onAndroidLink] reading arrives.
     */
    val lastLossCause: Flow<SessionLossCause?>

    /**
     * One reading of Android's own link state for the bonded Buds (`OsConnectionObserver`, forwarded by `:app` while the UI is visible). Used for
     * [lastLossCause] and — DECISIONS.md ADR-044 — to decide whether the session may be re-opened by itself. Never opens anything by itself.
     */
    fun onAndroidLink(link: AndroidLink)

    /**
     * The app's UI became visible (`true`, on resume) or not (`false`, on stop) — DECISIONS.md ADR-044: the session is re-opened by itself only while
     * visible and Android reports the Buds connected (after a session loss, when Android's link comes back, on resume); never in the background.
     */
    fun onAppVisible(visible: Boolean)
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
     * Why the Case battery could not be requested: the one `SubscribeRuntimeInfo` request per Connect was not sent (`null` = it was, or a packet
     * arrived) — DECISIONS.md ADR-043 (the DLCI 0x08 claim of ADR-035 is withdrawn).
     */
    val caseBatteryError: Flow<BudsError?>

    /**
     * The DLCI 0x02 settings as the Buds reported them on this connection ([BudsSettings]; ADR-036 reads at Connect, ADR-045 acknowledged
     * writes), each with its receive time. Reset to "not read" at every Connect.
     */
    val settings: Flow<BudsSettings>

    /** Why the last settings read or write did not succeed (`null` = it did, or none was attempted this connection) — `ai-sessions/0052`. */
    val settingsError: Flow<BudsError?>

    /**
     * Why the last *Refresh battery* produced no new Left/Right reading (`null` = it did, or none was attempted this connection) —
     * [BudsError.NoNewBatteryReading] when the claim worked but the Buds sent no battery frame, otherwise the claim's own error
     * (`ai-sessions/0052`).
     */
    val batteryRefreshError: Flow<BudsError?>

    /**
     * Whether the Buds currently allow an ANC `Set`, from the last `Notify ANC state`'s Settable byte ([AncAvailability], `ai-sessions/0048` I-3) —
     * [AncAvailability.UNKNOWN] until one arrived on this connection. While [AncAvailability.NOT_ALLOWED], [setAncMode] sends nothing.
     */
    val ancAvailability: Flow<AncAvailability>

    /** Wall-clock time [ancAvailability] was last updated, or `null` before any `Notify` has arrived this connection. */
    val ancAvailabilityUpdatedAt: Flow<Long?>

    /**
     * `true` while the current [ancAvailability] was received within ~2 s of the Message Stream channel opening — DECISIONS.md ADR-024's
     * 2026-09-18 consequence: such a first reading can be stale and self-corrects with a spontaneous re-Notify; a later `Notify` in the same
     * claim replaces it.
     */
    val ancAvailabilityProvisional: Flow<Boolean>

    /** Non-null while the app is in read-only Safe Mode (ARCHITECTURE.md §8.1, DECISIONS.md ADR-042). */
    val safeMode: Flow<SafeModeState?>

    /** What the Buds announced at connect (firmware), or `null` before the announcement / after a disconnect. */
    val deviceInfo: Flow<DeviceInfo?>

    /** The earbud a Find My Buds ring was last started on and not yet stopped (`null` = none) — the ring keeps sounding on the Buds after the
     * channel is released and after Disconnect (`ai-sessions/0042`, `CAP-062`), so the UI must say so. Kept across Disconnect/Connect; cleared
     * only by an ACKed Stop (or replaced by a new Ring) — `ai-sessions/0048` I-6. */
    val ringing: Flow<RingNotice?>

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

    /** The user's Connect tap: connects to the already-bonded Buds (ARCHITECTURE.md §9.0a step 6) —
     * fails with [BudsError.NotPaired] if no bonded device exists yet
     * (pairing is a separate, prior step, `ai-sessions/0036`). Re-enables the automatic re-open of ADR-044. */
    suspend fun connect(): BudsResult<Unit>

    /** The user's Disconnect tap. Switches the automatic re-open of ADR-044 off until the next [connect]. */
    suspend fun disconnect(): BudsResult<Unit>

    /**
     * Re-reads Left/Right on the user's request: a **fresh** Message Stream claim (ADR-033) — a channel still open from an earlier claim is
     * released and opened again, because the Buds send their battery burst only when the channel opens (`ai-sessions/0052`). No burst ⇒
     * [BudsError.NoNewBatteryReading] in [batteryRefreshError], values unchanged. Each bud's charging state and the Case arrive on the runtime-info
     * stream (ADR-043); since ADR-043's 2026-09-26 Update a Refresh also sends one `SubscribeRuntimeInfo` request (no retry). Requires an open session.
     */
    suspend fun refreshBattery(): BudsResult<Unit>

    /** ANC `Set` (ADR-009). Fails with [BudsError.AncNotAllowed] and sends nothing while [ancAvailability] is [AncAvailability.NOT_ALLOWED]. */
    suspend fun setAncMode(mode: AncMode): BudsResult<Unit>
    suspend fun refreshAncMode(): BudsResult<AncMode>

    /** Reads the Buds' active EQ (`ReadSetting 4:16`, ADR-034) and updates [eqProfile]. Requires an open session. */
    suspend fun refreshEq(): BudsResult<EqBandGains>

    suspend fun setEqGains(gains: EqBandGains): BudsResult<Unit>
    suspend fun applyEqPreset(preset: EqPreset): BudsResult<Unit>

    // ---- DLCI 0x02 settings writes (DECISIONS.md ADR-045) — one `WriteSetting` per call, through the Safe-Mode gate (ADR-042); the value in
    // [settings] changes only on the Buds' empty RESPONSE with status OK, otherwise the previous value stays and [settingsError] says why.

    /** Volume balance −100 … +100, **+100 = Left** (ADR-026); clamped. */
    suspend fun setVolumeBalance(value: Int): BudsResult<Unit>
    suspend fun setMonoAudio(on: Boolean): BudsResult<Unit>
    suspend fun setConversationDetection(on: Boolean): BudsResult<Unit>
    suspend fun setTouchControls(on: Boolean): BudsResult<Unit>
    suspend fun setPressAndHold(bud: Bud, action: HoldAction): BudsResult<Unit>

    suspend fun ringBud(target: RingTarget): BudsResult<Unit>
    suspend fun stopRinging(): BudsResult<Unit>
}
