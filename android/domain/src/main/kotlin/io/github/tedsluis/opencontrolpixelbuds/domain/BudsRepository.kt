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
     * One reading of Android's Bluetooth adapter (`BluetoothAdapter.ACTION_STATE_CHANGED`, forwarded by `:app` while the UI is visible, as [onAndroidLink]):
     * [on] `false` for turning off / off, `true` for turning on / on. `ai-sessions/0064` F-3: an off reading around a session loss names its cause
     * ([SessionLossCause.BLUETOOTH_OFF]). Never opens, closes or sends anything.
     */
    fun onBluetoothAdapter(on: Boolean)

    /**
     * The app's UI became visible (`true`, on resume) or not (`false`, on stop) — DECISIONS.md ADR-044: the session is re-opened by itself only while
     * visible and Android reports the Buds connected (after a session loss, when Android's link comes back, on resume); never in the background.
     */
    fun onAppVisible(visible: Boolean)

    /**
     * The Buds' last reported ANC mode, `null` before any report in this app run. It is **not** cleared when a session ends: whether it is current is
     * decided by [isCurrent] from [ancModeUpdatedAt] and [sessionSince] (`ai-sessions/0069`, A68-APP-02). A state, not an event stream: the newest report
     * always replaces the one before it (A68-APP-03 — the replay-1 `SharedFlow` it was could drop a report while a collector was busy).
     */
    val ancMode: Flow<AncMode?>

    /**
     * Wall-clock time the current connection was requested (the start of the Connect, or of an automatic re-open) — `null` before the first one. A value
     * the Buds reported before it is from an earlier connection ([isFromThisSession]). Not cleared at Disconnect; [connectionState] says whether a session
     * is open.
     */
    val sessionSince: Flow<Long?>

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
    val settingsError: Flow<SettingsFailure?>

    /**
     * Why the last *Refresh battery* produced no new Left/Right reading (`null` = it did, or none was attempted this connection) —
     * [BudsError.NoNewBatteryReading] when the claim worked but the Buds sent no battery frame, otherwise the claim's own error
     * (`ai-sessions/0052`).
     */
    val batteryRefreshError: Flow<BudsError?>

    /**
     * Whether the Buds currently allow an ANC `Set`, from the last `Notify ANC state`'s Settable byte ([AncAvailability], `ai-sessions/0048` I-3) —
     * [AncAvailability.UNKNOWN] until one arrived on this connection. Shown to the user; [setAncMode] does not rely on it — every tap asks the Buds again
     * (`ai-sessions/0062` F-1).
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

    /**
     * ANC `Set` (ADR-009), on a Message Stream claim (ADR-032). **Since `ai-sessions/0062` (F-1)** every claim first sends the `Get` (ADR-021/022, ADR-032
     * item 5): the `Set` follows in the same claim only if that claim's `Notify` reads Settable non-zero; on `0x00` it fails with
     * [BudsError.AncNotAllowed] and nothing more is sent (before, only a known "not allowed" asked first — `ai-sessions/0054` I-1). An answer cut off by
     * the claim's close is [BudsError.AnswerCutOff] (F-3).
     */
    suspend fun setAncMode(mode: AncMode): BudsResult<Unit>

    /**
     * The Quick Settings tile's tap (`ai-sessions/0062` F-1): as [setAncMode], but the mode to set is [next] of the mode the **same claim's** `Notify`
     * reports — not of the mode shown before the tap, which can be old (the known limit "ANC tile after re-wearing", `ai-sessions/0054`).
     */
    suspend fun stepAncMode(next: (current: AncMode?) -> AncMode): BudsResult<Unit>
    suspend fun refreshAncMode(): BudsResult<AncMode>

    /**
     * When the last ANC change's answer was cut off (`ai-sessions/0062` F-3) — the Buds may or may not have switched, so the shown [ancMode] is "not
     * confirmed" until their next `Notify` (or ACK) clears this back to `null`.
     */
    val ancModeUnconfirmedAt: Flow<Long?>

    /** Reads the Buds' active EQ (`ReadSetting 4:16`, ADR-034) and updates [eqProfile]. Requires an open session. */
    suspend fun refreshEq(): BudsResult<EqBandGains>

    /**
     * Re-reads the DLCI 0x02 settings on the user's request — a pull on "Sound" or "Controls" (`ai-sessions/0057` D-11): the Connect-time `ReadSetting` pass
     * once more, with the same fields in the same order (2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29), sequential, each waiting ≤ 2 s, never retried (ADR-036). Requires an open
     * session — otherwise nothing is sent and [BudsError.ConnectionLost] is returned. A field that is not answered keeps its last value with its own time; the
     * reason is in [settingsError]. Reads are not gated by Safe Mode (ADR-042).
     */
    suspend fun refreshSettings(): BudsResult<Unit>

    suspend fun setEqGains(gains: EqBandGains): BudsResult<Unit>
    suspend fun applyEqPreset(preset: EqPreset): BudsResult<Unit>

    // ---- DLCI 0x02 settings writes (DECISIONS.md ADR-045 … ADR-047, ADR-052 … ADR-055) — one `WriteSetting` per call, through the Safe-Mode gate (ADR-042); the
    // value in [settings] changes only on the Buds' empty RESPONSE with status OK, otherwise the previous value stays and [settingsError] says why. A tap
    // while the session is being (re)opened fails with [BudsError.SessionOpening] — nothing is sent or queued.

    /** Volume balance −100 … +100, **+100 = Left** (ADR-026); clamped. */
    suspend fun setVolumeBalance(value: Int): BudsResult<Unit>
    suspend fun setMonoAudio(on: Boolean): BudsResult<Unit>
    suspend fun setConversationDetection(on: Boolean): BudsResult<Unit>
    suspend fun setTouchControls(on: Boolean): BudsResult<Unit>
    suspend fun setPressAndHold(bud: Bud, action: HoldAction): BudsResult<Unit>

    /**
     * Ticks or unticks [mode] in the press-and-hold ANC-mode list (`qhr` field 12, ADR-046): one write carrying **all four** booleans — the other three as
     * the Buds last reported them. Refused before anything is sent when the list was not read on this connection ([BudsError.AncModeListNotRead]) or
     * when fewer than two modes would stay ticked ([BudsError.AncModeListTooShort]).
     */
    suspend fun setAncModeSelected(mode: AncMode, selected: Boolean): BudsResult<Unit>

    /** The "In-ear detection" setting (`qhr` field 2, ADR-047): `WriteSetting 4:{2:0|1}`. */
    suspend fun setInEarDetection(on: Boolean): BudsResult<Unit>

    /**
     * "Multipoint" (`qhr` field 11, ADR-053): `WriteSetting 4:{11:0|1}`. The Buds' Fast Pair SASS answer goes to whichever client holds the Message Stream;
     * the app does not wait for it.
     */
    suspend fun setMultipoint(on: Boolean): BudsResult<Unit>

    /**
     * "Use head gestures" (`qhr` field 29, ADR-052): `WriteSetting 4:{29:2}` for on, `4:{29:1}` for off. The official app's "Optimize head gestures" dialog
     * is not reproduced; nothing is sent on GSND CONTROL.
     */
    suspend fun setHeadGestures(on: Boolean): BudsResult<Unit>

    /** Case sounds "Other alerts" (`qhr` field 27, ADR-054): `WriteSetting 4:{27:0|1}`. */
    suspend fun setCaseSoundOtherAlerts(on: Boolean): BudsResult<Unit>

    /** Case sounds "Earbuds replaced" (`qhr` field 28, ADR-054): `WriteSetting 4:{28:0|1}`. */
    suspend fun setCaseSoundEarbudsReplaced(on: Boolean): BudsResult<Unit>

    /** "Volume EQ" (`qhr` field 15, ADR-055): `WriteSetting 4:{15:0|1}`. */
    suspend fun setVolumeEq(on: Boolean): BudsResult<Unit>

    suspend fun ringBud(target: RingTarget): BudsResult<Unit>
    suspend fun stopRinging(): BudsResult<Unit>
}
