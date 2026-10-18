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
package io.github.tedsluis.opencontrolpixelbuds.data

import android.bluetooth.BluetoothDevice
import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncFrame
import io.github.tedsluis.opencontrolpixelbuds.data.codec.CodecRouter
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Dlci
import io.github.tedsluis.opencontrolpixelbuds.data.codec.EqFrame
import io.github.tedsluis.opencontrolpixelbuds.data.codec.EqFrameEncoder
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Hdlc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Maestro
import io.github.tedsluis.opencontrolpixelbuds.data.codec.MaestroChannel
import io.github.tedsluis.opencontrolpixelbuds.data.codec.MessageStreamAck
import io.github.tedsluis.opencontrolpixelbuds.data.codec.MessageStreamReply
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingFrame
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingFrameEncoder
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RpcPacket
import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RoutedFrame
import io.github.tedsluis.opencontrolpixelbuds.data.codec.SettingValue
import io.github.tedsluis.opencontrolpixelbuds.data.codec.SettingsCodec
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeCause
import io.github.tedsluis.opencontrolpixelbuds.domain.AdapterOffReading
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.LinkReading
import io.github.tedsluis.opencontrolpixelbuds.domain.LINK_LOST_AFTER_MS
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
import io.github.tedsluis.opencontrolpixelbuds.domain.classifySessionLoss
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingSource
import io.github.tedsluis.opencontrolpixelbuds.domain.markedFromEarlierSession
import io.github.tedsluis.opencontrolpixelbuds.domain.ComponentSerial
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
import io.github.tedsluis.opencontrolpixelbuds.domain.RingNotice
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.domain.SafeModeState
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import io.github.tedsluis.opencontrolpixelbuds.domain.WornReading
import io.github.tedsluis.opencontrolpixelbuds.domain.wornReading
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsSdpUuids
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsTransport
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * `BudsRepository` implementation (ARCHITECTURE.md §2.1, DECISIONS.md
 * ADR-001), wiring `:hardware`'s [transport]/[connectionState] and `:data`'s
 * own [CodecRouter] to the domain-facing interface. Implements
 * ARCHITECTURE.md §3.1's per-feature state-reconciliation table:
 *
 * - **ANC** (DECISIONS.md ADR-067, `ai-sessions/0098`): a change is one `WriteSetting 4:{13:n}` on the session channel — done only on the Buds' `RESPONSE`
 *   status 0; `FAILED_PRECONDITION` = no bud worn; no answer keeps the previous mode. No Message Stream claim (before 1.4.0: the `Set` of ADR-009).
 * - **EQ**: [eqProfile] is reset to `null` ("unknown, not stale-and-trusted") inside [connect], before the session is
 *   `Ready`, and filled by the Connect sequence's `ReadSetting 4:16` (DECISIONS.md ADR-034); a failed
 *   read or write is reported through [eqError] with its reason instead of being assumed to have worked.
 * - **Battery**: push-based — Left, Right and the Case from the `SubscribeRuntimeInfo` stream on the session channel, requested once per Connect
 *   (ADR-043, ADR-068 — no Message Stream claim at Connect since 1.4.0); a `03 03` on a Find claim still updates Left/Right (ADR-033, newest wins).
 *   HFP `AT+BIEV` is not consumed (ADR-040).
 * - **Safe Mode** (ARCHITECTURE.md §8.1, ADR-042): every write/control command passes [SafeModeGate] first.
 * - **Find My Buds**: no persisted state to reconcile.
 * - **The noise-control mode without a claim** (DECISIONS.md ADR-064/065, `ai-sessions/0091`): once per MAESTRO session the Connect sequence subscribes to
 *   the Buds' settings stream and reads setting 13 ([subscribeToSettingsChanges], [readAncSetting]); every change of the mode — a press-and-hold on a bud,
 *   the Buds' own switch, the app's own `Set` — then arrives as a `4:{13:n}` stream packet on the app's own channel (`CAP-051` 1717/1755/1802, `CAP-038`
 *   2170 with no Message Stream open), and every other setting the app reads follows the same stream. The Message Stream claim (ADR-032) is again only a
 *   tap's: ADR-061's hold, ADR-062's re-claim and ADR-063's Play-services line are gone (ADR-065 item 5).
 *
 * This class stays unit-testable against a [io.github.tedsluis.opencontrolpixelbuds.hardware.FakeBudsTransport]
 * and scripted flows, per AGENTS.md §11. [connectionStateMachine] is the real, already
 * independently-tested `:hardware` class (not just its `Flow`) — [connect]/
 * [disconnect] need to drive its transitions, not just observe them.
 * [bondedDeviceProvider] resolves the current bonded device lazily at
 * [connect] time (never cached at construction — pairing can happen well
 * after this repository singleton is built, `ai-sessions/0037`).
 */
class BudsRepositoryImpl(
    // `internal` (was `private`) for the experiment build type's ExperimentRunner (`ai-sessions/0096`, src/experiment) — here and on the twelve members
    // marked "(experiment seam)" below: a visibility change only; debug and release have no caller outside this class.
    internal val transport: BudsTransport,
    private val connectionStateMachine: ConnectionStateMachine,
    private val bondedDeviceProvider: () -> BluetoothDevice?,
    debugModeEnabled: Flow<Boolean>,
    private val scope: CoroutineScope,
    /** Wall clock for the provisional Settable rule (ADR-049, which superseded ADR-024's dock-state reading), the loss cause (I-7) and the re-open rules (ADR-044); a test passes its virtual clock. */
    private val clock: () -> Long = System::currentTimeMillis,
    /** Whether ADR-044's automatic re-open is on before the first Connect/Disconnect tap of this process. */
    autoReopenInitially: Boolean = true,
) : BudsRepository {

    override val connectionState: Flow<ConnectionState> = connectionStateMachine.state

    private val _lastConnectionError = MutableStateFlow<BudsError?>(null)
    override val lastConnectionError: Flow<BudsError?> = _lastConnectionError

    private val _lastLossCause = MutableStateFlow<SessionLossCause?>(null)
    override val lastLossCause: Flow<SessionLossCause?> = _lastLossCause

    /** Recent readings of Android's link (I-7), newest last; guarded by itself. */
    private val linkReadings = ArrayDeque<LinkReading>()

    /** When Android last reported its Bluetooth adapter turning off or off (F-3, `ai-sessions/0064`), newest last; guarded by itself. */
    private val adapterOffReadings = ArrayDeque<AdapterOffReading>()

    /** When the current [lastConnectionError]'s loss happened (`null` = no loss to explain). */
    @Volatile
    private var lossAtMillis: Long? = null

    /** Whether the app's UI is visible (resume … stop, [onAppVisible]); readings of Android's link arrive only then. */
    @Volatile
    private var appVisible = false

    /**
     * `ai-sessions/0054` I-2: the current loss happened while the app was not visible, or the app left before a reading decided its cause — then the
     * first reading after it, taken on return, decides ([classifySessionLoss] rule 3).
     */
    @Volatile
    private var lossWhileHidden = false

    private val _messageStreamError = MutableStateFlow<BudsError?>(null)
    override val messageStreamError: Flow<BudsError?> = _messageStreamError

    private val _eqError = MutableStateFlow<BudsError?>(null)
    override val eqError: Flow<BudsError?> = _eqError

    /** ADR-067: why the last noise-control change or Refresh failed (`null` after a success). */
    private val _ancError = MutableStateFlow<BudsError?>(null)
    override val ancError: Flow<BudsError?> = _ancError

    /** The pw_rpc channel the Buds announced for this connection (their unsolicited `GetSoftwareInfo`, ADR-034). */
    private val _maestroChannelId = MutableStateFlow<Int?>(null)

    /** Answers to our Maestro requests (an EQ value, or an empty/error result) — what a read/write waits for. */
    internal val _maestroReplies = MutableSharedFlow<MaestroReply>(extraBufferCapacity = 16) // (experiment seam)

    /** One EQ read or write at a time, so a reply can only belong to the request that is waiting for it. */
    internal val eqMutex = Mutex() // (experiment seam)

    @Volatile
    private var eqReadJob: Job? = null

    /**
     * A58-APP-04 (`ai-sessions/0059`, ADR-045 Update 2026-09-30): until this time (the injected [clock]) no Maestro request is sent — set when a `WriteSetting`
     * got no answer within [EQ_WRITE_ACK_TIMEOUT_MS]. pw_rpc answers carry no `call_id` to match on, so a late answer to that write must not be taken as the
     * answer to the next one; it arrives while nothing waits and is dropped (logged in [handleRoutedFrame]).
     */
    @Volatile
    private var writeQuarantineUntil: Long = Long.MIN_VALUE

    /**
     * Serialises every use of the shared Message Stream channel (DLCI 0x04) — claim, action, release
     * — so a release can never close the channel under a command that is still waiting for its reply
     * (DECISIONS.md ADR-032).
     */
    private val claimMutex = Mutex()

    @Volatile
    private var releaseJob: Job? = null

    @Volatile
    private var resubscribeJob: Job? = null

    /** One [connect] at a time — a double tap must not start two overlapping socket-opening runs. */
    private val connectMutex = Mutex()

    /**
     * `ai-sessions/0068` A68-APP-13 (fixed `ai-sessions/0098`): [disconnect] runs outside [connectMutex] — a Disconnect tapped while a Connect waits for its
     * sockets must win. Each Disconnect counts up; a Connect whose sockets open after a Disconnect it did not see closes them again and stays disconnected.
     */
    private val disconnectGeneration = java.util.concurrent.atomic.AtomicInteger(0)

    private val codecRouter = CodecRouter()

    /** DECISIONS.md ADR-044: the foreground-only, one-attempt-per-event re-open of the session (`ai-sessions/0048` I-1). */
    private val reopener = SessionReopener(
        scope = scope,
        clock = clock,
        sessionOpenOrOpening = { connectionStateMachine.state.value.let { it is ConnectionState.Ready || it is ConnectionState.Connecting || it is ConnectionState.Discovering } },
        reopen = { reopenSession() },
        enabled = autoReopenInitially,
    )

    // Read by the logging call sites below — ARCHITECTURE.md §12/AGENTS.md §9: raw hex dumps are
    // gated behind this, connection-state logging (ConnectionStateMachine/RfcommBudsTransport's
    // own BleLogger calls) never is.
    @Volatile
    private var debugModeEnabledSnapshot = false

    // A state, not a replay-1 SharedFlow (`ai-sessions/0069`, A68-APP-03): `tryEmit` on that flow returned false — and dropped the report — whenever a
    // collector was still busy with the previous one, while [_ancModeUpdatedAt] advanced. A StateFlow always holds the newest report.
    private val _ancMode = MutableStateFlow<AncMode?>(null)
    override val ancMode: StateFlow<AncMode?> = _ancMode

    /** When the current connection was requested ([BudsRepository.sessionSince]); values reported before it are "from the last connection" (A68-APP-02). */
    private val _sessionSince = MutableStateFlow<Long?>(null)
    override val sessionSince: StateFlow<Long?> = _sessionSince

    private val _ancModeUpdatedAt = MutableStateFlow<Long?>(null)
    override val ancModeUpdatedAt: StateFlow<Long?> = _ancModeUpdatedAt

    /** Why the current [_ancMode] is what it is (`ai-sessions/0082` item 1, the maintainer's rule in chat 2026-10-09); `null` until this connection's first report. */
    private val _ancModeCause = MutableStateFlow<AncModeCause?>(null)
    override val ancModeCause: StateFlow<AncModeCause?> = _ancModeCause

    /** The mode of the app's `Set` from its write until its wait ends (ACK, NAK, timeout, cut-off): a `Notify` of that mode meanwhile is [AncModeCause.SET_BY_APP]. */
    @Volatile
    private var setPendingMode: AncMode? = null

    /**
     * ADR-065 item 4 (`ai-sessions/0091`): the mode and the time of the app's last `Set` — the settings stream's `4:{13:n}` for it can arrive after the ACK
     * ended the Set's wait (`CAP-051` 1527 Set → 1529 ACK → 1534 stream, 269 ms) or between two Notifies (`CAP-056` 4001 → 4028, 55 ms); a stream value equal
     * to it within [ANC_SET_WINDOW_MS] is the app's own change, not the Buds'.
     */
    @Volatile
    private var lastSetMode: AncMode? = null

    @Volatile
    private var lastSetAt: Long = Long.MIN_VALUE

    /**
     * A93-APP-02 (`ai-sessions/0094`, `CAP-076` §6.2 / P1): a session read the mode twice — the Connect's snapshot `Get` and `ReadSetting 4:13` — and in 12 of
     * 12 `CAP-076` sessions the `Get`'s `Notify` came first. Since ADR-068 (`ai-sessions/0098`) only the `ReadSetting 4:13` remains (and a Refresh's). [readRound] counts the sessions' read sequences ([launchInitialEqRead]); [changedSinceBy] is the
     * round and the source of the read answer that last gave "changed since the app last saw it", so the other read of the same round, finding the same mode,
     * keeps that cause instead of replacing it with a reading.
     */
    @Volatile
    private var readRound = 0

    @Volatile
    private var changedSinceBy: Pair<Int, ReadSource>? = null

    private enum class ReadSource { GET, READ_SETTING }

    private val _eqProfile = MutableStateFlow<EqBandGains?>(null)
    override val eqProfile: StateFlow<EqBandGains?> = _eqProfile

    private val _eqProfileUpdatedAt = MutableStateFlow<Long?>(null)
    override val eqProfileUpdatedAt: StateFlow<Long?> = _eqProfileUpdatedAt

    private val _batteryStatus = MutableStateFlow(BatteryStatus())
    override val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus

    private val _batteryStatusUpdatedAt = MutableStateFlow<Long?>(null)
    override val batteryStatusUpdatedAt: StateFlow<Long?> = _batteryStatusUpdatedAt

    private val _caseBatteryError = MutableStateFlow<BudsError?>(null)
    override val caseBatteryError: Flow<BudsError?> = _caseBatteryError

    private val _settings = MutableStateFlow(BudsSettings())
    override val settings: StateFlow<BudsSettings> = _settings

    private val _settingsError = MutableStateFlow<SettingsFailure?>(null)
    override val settingsError: Flow<SettingsFailure?> = _settingsError

    private val _batteryRefreshError = MutableStateFlow<BudsError?>(null)
    override val batteryRefreshError: Flow<BudsError?> = _batteryRefreshError

    /** Every runtime-info packet as it arrives (replay 0) — what a battery Refresh waits for (ADR-068). */
    private val _runtimeInfoPackets = MutableSharedFlow<Unit>(extraBufferCapacity = 8)

    private val _ancAvailability = MutableStateFlow(AncAvailability.UNKNOWN)
    override val ancAvailability: Flow<AncAvailability> = _ancAvailability

    private val _ancAvailabilityUpdatedAt = MutableStateFlow<Long?>(null)
    override val ancAvailabilityUpdatedAt: StateFlow<Long?> = _ancAvailabilityUpdatedAt

    private val _ancAvailabilityProvisional = MutableStateFlow(false)
    override val ancAvailabilityProvisional: StateFlow<Boolean> = _ancAvailabilityProvisional

    private val _ancModeUnconfirmedAt = MutableStateFlow<Long?>(null)
    override val ancModeUnconfirmedAt: StateFlow<Long?> = _ancModeUnconfirmedAt

    /** When the current Message Stream claim opened (the ADR-024 "provisional within ~2 s" reference point). */
    @Volatile
    private var messageStreamOpenedAt: Long = Long.MIN_VALUE

    private val _safeMode = MutableStateFlow<SafeModeState?>(null)
    override val safeMode: StateFlow<SafeModeState?> = _safeMode

    /** ADR-059: the pure derivation of `:domain`, over the four flows it reads — nothing is requested for it. */
    override val wornReading: Flow<WornReading> =
        combine(_ancAvailability, _ancAvailabilityUpdatedAt, _settings, _batteryStatus) { availability, checkedAt, settings, battery ->
            wornReading(availability, checkedAt, settings.inEarDetection, battery.leftCharging, battery.rightCharging)
        }

    /** The Model ID the Buds sent on the *current* Message Stream claim (`null` until it arrives; reset on every new open). */
    private val _modelIdThisClaim = MutableStateFlow<String?>(null)

    /** The last Model ID seen on this connection (for DLCI 0x02 writes, which have no Message Stream claim of their own). */
    @Volatile
    private var modelIdSeen: String? = null

    /** Every ACK/NAK on the Message Stream (its own routed type, 0044 APP-3) — what a command waits for. */
    internal val _replies = MutableSharedFlow<MessageStreamReply>(extraBufferCapacity = 16) // (experiment seam)

    /** Sibling helpers so every mutation of the value above also stamps its own [System.currentTimeMillis]
     * timestamp, in one place (`ai-sessions/0043` Phase H) — never a polling timer, only recorded when a
     * value is actually received. */
    private fun emitAncMode(mode: AncMode, cause: AncModeCause? = null) {
        _ancMode.value = mode
        _ancModeUpdatedAt.value = clock()
        if (cause != null) _ancModeCause.value = cause
    }

    /**
     * The cause a `Notify` gives the mode it carries (`ai-sessions/0082` item 1, the maintainer's rule; ADR-065 item 4 and the maintainer's choice in chat
     * 2026-10-14, `ai-sessions/0091` Q4 — one rule for every read answer): the answer to the app's `Get` is a reading — unless a mode was shown before and the
     * answer differs from it: then the mode changed while this app was not connected or did not have the channel (`CAP-075` 2980, 5102, 5361; `CAP-051` 2247
     * after the Buds' own `DISC`) — also across a Connect or a re-open, since `ai-sessions/0091` (the kept mode of the ended session is the reference; before,
     * [_ancModeCause] was reset at Connect and a connection's first read was always a reading). The requested mode while the app's `Set` waits is the app's own
     * change; a mode other than the shown one, with nothing waiting, is the Buds' — a press-and-hold on a bud or their own change; the first mode ever shown this
     * run is a reading (nothing was shown that could have changed); the same mode again changes nothing (`null`).
     */
    private fun causeOfNotify(mode: AncMode, answersGet: Boolean): AncModeCause? = when {
        answersGet -> causeOfReadAnswer(mode, ReadSource.GET)
        setPendingMode == mode -> AncModeCause.SET_BY_APP
        _ancMode.value == null -> AncModeCause.READ
        mode != _ancMode.value -> AncModeCause.CHANGED_BY_BUDS
        else -> null
    }

    /**
     * ADR-065 item 4: the cause of a read answer — the `Notify` to the app's `Get`, or the RESPONSE to its `ReadSetting 4:13`: one that differs from the shown
     * mode is "changed since the app last saw it"; the same mode is a reading — except the other read of the same session after such an answer, which keeps
     * the cause (`null`, A93-APP-02: before `ai-sessions/0094` the equal `4:13` answer replaced it, `CAP-076` H1 4094 → 4135).
     */
    private fun causeOfReadAnswer(mode: AncMode, source: ReadSource): AncModeCause? {
        val shown = _ancMode.value
        val changedSince = changedSinceBy
        return when {
            shown != null && mode != shown -> {
                changedSinceBy = readRound to source
                AncModeCause.CHANGED_SINCE_LAST_READ
            }
            _ancModeCause.value == AncModeCause.CHANGED_SINCE_LAST_READ && changedSince != null &&
                changedSince.first == readRound && changedSince.second != source -> null
            else -> AncModeCause.READ
        }
    }

    /**
     * ADR-065 item 4 (`ai-sessions/0091`): the cause of a `4:{13:n}` settings-stream packet — the app's own `Set` of that mode within [ANC_SET_WINDOW_MS]
     * (whether its wait is still open or already ended by the ACK), else the push is the Buds' ("Changed by the Buds": a press-and-hold on a bud is one
     * trigger; the Buds' own switch on a wear change is 🟡, PROTOCOL.md §6); the first mode ever shown is a reading; the same mode again changes nothing.
     */
    private fun causeOfStream(mode: AncMode): AncModeCause? = when {
        setPendingMode == mode -> AncModeCause.SET_BY_APP
        lastSetMode == mode && clock() - lastSetAt <= ANC_SET_WINDOW_MS -> AncModeCause.SET_BY_APP
        _ancMode.value == null -> AncModeCause.READ
        mode != _ancMode.value -> AncModeCause.CHANGED_BY_BUDS
        else -> null
    }

    /**
     * Setting 13 from the Buds (DECISIONS.md ADR-064/065, `ai-sessions/0091`): the RESPONSE to the app's `ReadSetting 4:13` ([stream] false) or a
     * `SubscribeToSettingsChanges` push ([stream] true). 1–4 is the mode, shown with the packet's receive time and its cause ([causeOfReadAnswer] /
     * [causeOfStream]); the availability follows (ADR-065 item 3, the maintainer's choice in chat 2026-10-14, Q5): 0 = no mode settable — ADR-049's Settable
     * `00` — never shown as a mode (`CAP-045` 2277, `CAP-051` 2247), 1–4 = allowed. A stream value equal to the shown mode moves only its time.
     */
    private fun applyAncState(state: SettingValue.AncState, stream: Boolean) {
        val mode = state.mode
        updateAncAvailability(if (mode == null) AncAvailability.NOT_ALLOWED else AncAvailability.ALLOWED, fromClaim = false)
        val source = if (stream) "the settings stream" else "the mode read"
        if (mode == null) {
            BleLogger.logConnectionEvent("Noise-control mode from $source: no mode settable")
            return
        }
        // `ai-sessions/0100` (`CAP-079` §3.3): a settable mode reported by the Buds ends an earlier "not allowed" — the red line must not outlive it.
        if (_ancError.value == BudsError.AncNotAllowed) _ancError.value = null
        val cause = if (stream) causeOfStream(mode) else causeOfReadAnswer(mode, ReadSource.READ_SETTING)
        _ancModeUnconfirmedAt.value = null // the Buds' own report — whatever a cut-off answer would have said, this is their mode now (F-3)
        emitAncMode(mode, cause)
        // A93-APP-04: a Refresh waits for its own `ReadSetting 4:13` RESPONSE — a stream packet never ends that wait (`MaestroReply.Setting.stream`).
        // A93-APP-08 (`CAP-076` defect (c)): a packet that changes nothing logs "unchanged", not the cause it kept from an earlier packet.
        BleLogger.logConnectionEvent("Noise-control mode from $source (${cause?.name?.lowercase() ?: "unchanged"})")
    }

    private fun updateEqProfile(gains: EqBandGains?) {
        _eqProfile.value = gains
        _eqProfileUpdatedAt.value = if (gains == null) null else clock()
    }

    private fun updateBatteryStatus(transform: (BatteryStatus) -> BatteryStatus) {
        _batteryStatus.update(transform)
        _batteryStatusUpdatedAt.value = clock()
    }

    /**
     * [fromClaim]: the reading is a Message Stream `Notify` (ADR-024: provisional within ~2 s of the claim's open); a setting-13 reading (ADR-067/068) is
     * the Buds' own current report and never provisional.
     */
    private fun updateAncAvailability(availability: AncAvailability, fromClaim: Boolean = true) {
        _ancAvailability.value = availability
        _ancAvailabilityUpdatedAt.value = clock() // the injected wall clock (a test's virtual one) — shown as "(checked HH:MM:SS)", I-1
        // ADR-024 (2026-09-18 consequence): a reading within ~2 s of the channel opening may be stale; a later Notify in the same claim
        // replaces it (the last value wins while the claim lingers). Compared only once a claim has opened (`Long.MIN_VALUE` would overflow).
        _ancAvailabilityProvisional.value = fromClaim && messageStreamOpenedAt != Long.MIN_VALUE &&
            clock() - messageStreamOpenedAt < AVAILABILITY_PROVISIONAL_MS
    }

    override fun onAndroidLink(link: AndroidLink) {
        synchronized(linkReadings) {
            linkReadings.addLast(LinkReading(link, clock()))
            while (linkReadings.size > MAX_LINK_READINGS) linkReadings.removeFirst()
        }
        reclassifyLoss()
        reopener.onAndroidLink(link)
    }

    override fun onBluetoothAdapter(on: Boolean) {
        if (on) return // only an off reading can name a loss (F-3)
        synchronized(adapterOffReadings) {
            adapterOffReadings.addLast(AdapterOffReading(clock()))
            while (adapterOffReadings.size > MAX_LINK_READINGS) adapterOffReadings.removeFirst()
        }
        reclassifyLoss()
    }

    override fun onAppVisible(visible: Boolean) {
        appVisible = visible
        // I-2: leaving before any reading decided the cause (the observer stops now) — the reading taken on return will decide it.
        if (!visible && lossAtMillis != null && _lastLossCause.value == SessionLossCause.UNDETERMINED) lossWhileHidden = true
        reopener.onVisible(visible)
    }

    /**
     * Stores one reported setting value with its time; a press-and-hold packet only touches the bud(s) it names. ADR-065 item 2 (`ai-sessions/0091`): a
     * settings-stream packet carries [changedByApp] `false` — except that the Buds' mirror of the app's own write (`CAP-069` 2492 → 2502, the same value
     * 300 ms later) keeps the reading's "changed by this app" mark instead of turning it into a plain read ([keepsOwnMark]).
     */
    private fun applySetting(value: SettingValue, atMillis: Long, changedByApp: Boolean = false) {
        _settings.update { s ->
            val changedByApp = changedByApp || keepsOwnMark(s, value)
            when (value) {
                is SettingValue.Flag -> {
                    val r = SettingReading(value.on, atMillis, changedByApp)
                    when (value.field) {
                        SettingsCodec.FIELD_IN_EAR_DETECTION -> s.copy(inEarDetection = r)
                        SettingsCodec.FIELD_TOUCH_CONTROLS -> s.copy(touchControls = r)
                        SettingsCodec.FIELD_MONO_AUDIO -> s.copy(monoAudio = r)
                        SettingsCodec.FIELD_CONVERSATION_DETECTION -> s.copy(conversationDetection = r)
                        SettingsCodec.FIELD_MULTIPOINT -> s.copy(multipoint = r)
                        SettingsCodec.FIELD_CASE_SOUND_OTHER_ALERTS -> s.copy(caseSoundOtherAlerts = r)
                        SettingsCodec.FIELD_CASE_SOUND_EARBUDS_REPLACED -> s.copy(caseSoundEarbudsReplaced = r)
                        SettingsCodec.FIELD_VOLUME_EQ -> s.copy(volumeEq = r)
                        else -> s
                    }
                }
                is SettingValue.Balance -> s.copy(volumeBalance = SettingReading(value.value, atMillis, changedByApp))
                is SettingValue.PressAndHold -> s.copy(
                    holdLeft = value.left?.let { SettingReading(it, atMillis, changedByApp) } ?: s.holdLeft,
                    holdRight = value.right?.let { SettingReading(it, atMillis, changedByApp) } ?: s.holdRight,
                )
                is SettingValue.AncModes -> s.copy(ancModeList = SettingReading(value.list, atMillis, changedByApp))
                is SettingValue.HeadGestures -> s.copy(headGestures = SettingReading(value.on, atMillis, changedByApp))
                is SettingValue.AncState -> s // the mode is not a settings-card value: [handleRoutedFrame] gives it to [emitAncMode]
            }
        }
    }

    /** Whether the current reading of [value]'s setting already holds the same value as the app's own change (the stream mirrors the app's write). */
    private fun keepsOwnMark(s: BudsSettings, value: SettingValue): Boolean {
        val current: SettingReading<*>? = when (value) {
            is SettingValue.Flag -> when (value.field) {
                SettingsCodec.FIELD_IN_EAR_DETECTION -> s.inEarDetection
                SettingsCodec.FIELD_TOUCH_CONTROLS -> s.touchControls
                SettingsCodec.FIELD_MONO_AUDIO -> s.monoAudio
                SettingsCodec.FIELD_CONVERSATION_DETECTION -> s.conversationDetection
                SettingsCodec.FIELD_MULTIPOINT -> s.multipoint
                SettingsCodec.FIELD_CASE_SOUND_OTHER_ALERTS -> s.caseSoundOtherAlerts
                SettingsCodec.FIELD_CASE_SOUND_EARBUDS_REPLACED -> s.caseSoundEarbudsReplaced
                SettingsCodec.FIELD_VOLUME_EQ -> s.volumeEq
                else -> null
            }
            is SettingValue.Balance -> s.volumeBalance
            is SettingValue.AncModes -> s.ancModeList
            is SettingValue.HeadGestures -> s.headGestures
            is SettingValue.PressAndHold, is SettingValue.AncState -> null
        }
        val reported: Any = when (value) {
            is SettingValue.Flag -> value.on
            is SettingValue.Balance -> value.value
            is SettingValue.AncModes -> value.list
            is SettingValue.HeadGestures -> value.on
            is SettingValue.PressAndHold, is SettingValue.AncState -> return false
        }
        return current != null && current.changedByApp && current.value == reported
    }

    /** ADR-044: the same Connect sequence as a tap, but it leaves the re-open policy as it is; a failure outside the state machine is shown. */
    private suspend fun reopenSession(): BudsResult<Unit> {
        val result = openSession()
        if (result is BudsResult.Failure && connectionStateMachine.state.value !is ConnectionState.Failed) {
            _lastConnectionError.value = result.error
        }
        return result
    }

    /**
     * I-7: the cause of the last loss from the readings around it; logged once per change (always-on, no address, AGENTS.md §9). T-1 (`ai-sessions/0062`): a
     * cause other than "the link went down" decided before loss + [LINK_LOST_AFTER_MS] is logged as provisional — a "not connected" reading can still come.
     * F-3 (`ai-sessions/0064`, the maintainer's choice "Final at once"): "Bluetooth was switched off" is never provisional — the adapter reading is direct
     * evidence, and no later link reading can arrive while Bluetooth is off (`CAP-066`: the provisional "undetermined" stayed the last line, twice).
     */
    private fun reclassifyLoss() {
        val lossAt = lossAtMillis
        val cause = if (lossAt == null) {
            null
        } else {
            classifySessionLoss(
                lossAt,
                synchronized(linkReadings) { linkReadings.toList() },
                lossWhileHidden,
                synchronized(adapterOffReadings) { adapterOffReadings.toList() },
            )
        }
        // A68-APP-13 (fixed `ai-sessions/0098`): three callers (a link reading, an adapter reading, a loss) can run this at once — one compare-and-set, so a
        // cause is stored and logged once, never twice or overwritten by an older evaluation.
        val previous = _lastLossCause.value
        if (previous != cause && _lastLossCause.compareAndSet(previous, cause)) {
            if (cause != null && lossAt != null) {
                val provisional = cause != SessionLossCause.ANDROID_LINK_LOST && cause != SessionLossCause.BLUETOOTH_OFF &&
                    clock() < lossAt + LINK_LOST_AFTER_MS
                BleLogger.logConnectionEvent(SessionDiagnostics.lossCauseLine(cause, provisional))
            }
        }
    }

    private fun clearLoss() {
        lossAtMillis = null
        lossWhileHidden = false
        _lastLossCause.value = null
    }

    /**
     * I-6 (`ai-sessions/0048`): the session a ring was started in has ended. The ring is kept — only an ACKed Stop ends it — but the app can no
     * longer know whether it has stopped by itself, so after a reconnect the screen says it *may* still be ringing.
     */
    private fun markRingFromEarlierSession() {
        _ringing.update { it?.copy(fromEarlierSession = true) }
    }

    /**
     * A68-APP-02 (`ai-sessions/0069`): the session has ended (Disconnect, or a loss) — the battery values it reported are "last seen" / "last connection" from
     * now, not only from the next Connect; until then they were shown as current while nothing was connected. The ANC mode, the EQ and the settings need no
     * mark of their own: none of them is current while the session is not Ready ([isCurrent]).
     */
    private fun markValuesFromEndedSession() {
        _batteryStatus.update { it.markedFromEarlierSession() }
    }

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    override val deviceInfo: Flow<DeviceInfo?> = _deviceInfo

    private val _serialsError = MutableStateFlow<BudsError?>(null)
    override val serialsError: Flow<BudsError?> = _serialsError

    /**
     * ADR-058: the serials the Buds answered — into [DeviceInfo] with their receive time. The announcement normally precedes them (the request waits for it);
     * should it come again on the same connection, the serials of this connection are kept (see the `MaestroHello` branch).
     */
    private fun applySerials(serials: List<ComponentSerial>, atMillis: Long) {
        _deviceInfo.update { (it ?: DeviceInfo(emptyList())).copy(serials = serials, serialsReadAtMillis = atMillis) }
    }

    private val _ringing = MutableStateFlow<RingNotice?>(null)
    override val ringing: Flow<RingNotice?> = _ringing

    /** ADR-069: when the Buds last said the ringing stopped without the app's Stop (`null` while ringing or none seen). */
    private val _ringStoppedOnBudAt = MutableStateFlow<Long?>(null)
    override val ringStoppedOnBudAt: Flow<Long?> = _ringStoppedOnBudAt

    /** ADR-069: the job that holds the Ring's claim until the Buds' stop status, the user's Stop or [RING_HOLD_MAX_MS]. */
    @Volatile
    private var ringHoldJob: Job? = null

    /** ADR-069: when the app last sent its own Stop — a status `00` within [ACK_WAIT_MS] + [MESSAGE_STREAM_LINGER_MS] answers it, not a stop on the bud. */
    @Volatile
    private var stopSentAt: Long = Long.MIN_VALUE

    private val _unidentifiedFrames = MutableSharedFlow<UnidentifiedFrame>(extraBufferCapacity = 32)
    override val unidentifiedFrames: Flow<UnidentifiedFrame> = _unidentifiedFrames

    /** Channel and time of the last inbound frame — only for the "why did the session end" log line (`ai-sessions/0042`). */
    @Volatile
    private var lastInboundChannel: Int? = null

    @Volatile
    private var lastInboundAtMillis: Long = 0L

    init {
        scope.launch {
            debugModeEnabled.collect { debugModeEnabledSnapshot = it }
        }
        scope.launch {
            transport.inbound.collect { (channelId, bytes) ->
                lastInboundChannel = channelId
                lastInboundAtMillis = clock()
                BleLogger.logHexDump(channelId, bytes, debugModeEnabledSnapshot)
                val routed = codecRouter.feed(
                    channelId,
                    bytes,
                    timestampMillis = clock(),
                    onUnidentified = { _unidentifiedFrames.tryEmit(it) },
                    onMalformed = { BleLogger.logMalformedFrame(channelId, debugModeEnabledSnapshot) },
                    // Always-on, payload-free (AGENTS.md §9): shows whether the Buds accept or reject a request.
                    onRpcPacket = { BleLogger.logConnectionEvent(it.summary()) },
                    // A reader threw on this frame (`ai-sessions/0069` A68-APP-01): the frame is dropped as malformed, the class is logged.
                    onDecoderFault = { BleLogger.logConnectionEvent("Decoder fault on channel $channelId: ${BleLogger.describe(it)}") },
                )
                routed.forEach(::handleRoutedFrame)
            }
        }
        scope.launch {
            // An on-demand channel died on its own (another client took it back, ADR-032): drop any partial frame buffered for it,
            // so the next claim cannot start misaligned (0044 finding APP-2).
            transport.channelClosed.collect { closed ->
                codecRouter.reset(closed.channelId)
                // ADR-065 (`ai-sessions/0091`): nothing re-claims by itself — the next tap, pull or Refresh claims again (ADR-032); the mode meanwhile
                // comes from the settings stream on the app's own channel.
                if (closed.channelId == Dlci.FAST_PAIR_MESSAGE_STREAM) _modelIdThisClaim.value = null
            }
        }
        scope.launch {
            // Peer disconnect/range loss (ARCHITECTURE.md §6, `BudsTransport.connectionLost`'s own
            // doc comment) — without this, `transport.connected` could silently go false with
            // nothing telling `ConnectionStateMachine`, leaving the UI stuck showing `Ready` for a
            // link that had actually already died (`ai-sessions/0038`).
            //
            // `ai-sessions/0039`: only a loss of a *live* session (Discovering/Ready) is acted on. The
            // transport already suppresses losses from replaced connections; this is the second line
            // of defence — a loss observed while Connecting/Disconnected/Failed cannot belong to the
            // session the UI is showing, and must not knock a fresh attempt back to Disconnected (the
            // "Failed -> Disconnected 7-50 ms later" pattern seen in the round-1/2 logs).
            transport.connectionLost.collect { loss ->
                val state = connectionStateMachine.state.value
                if (state is ConnectionState.Ready || state is ConnectionState.Discovering) {
                    val seen = lastInboundChannel
                    BleLogger.logConnectionEvent(
                        SessionDiagnostics.lossLine(
                            loss.channelId,
                            loss.detail,
                            seen,
                            if (seen == null) null else clock() - lastInboundAtMillis,
                        ),
                    )
                    _lastConnectionError.value = BudsError.ChannelLost(loss.channelId, loss.detail)
                    lossAtMillis = clock()
                    lossWhileHidden = !appVisible // I-2: no reading can come until the app is back on screen
                    reclassifyLoss()
                    markRingFromEarlierSession()
                    markValuesFromEndedSession()
                    reopener.onSessionLost()
                    cancelClaimJobs()
                    codecRouter.resetAll()
                    connectionStateMachine.onDisconnected()
                } else {
                    BleLogger.logConnectionEvent(
                        "Ignoring connection-loss report on channel 0x%02x while ${state::class.simpleName}"
                            .format(loss.channelId),
                    )
                }
            }
        }
    }

    private fun handleRoutedFrame(frame: RoutedFrame) {
        when (frame) {
            is RoutedFrame.Anc -> when (val anc = frame.frame) {
                is AncFrame.Notify -> {
                    // Since ADR-067/068 (`ai-sessions/0098`) the app sends no `Get`: a `Notify` arrives only on a Find claim and is never the answer to a read.
                    val answersGet = false
                    // I-3 (`ai-sessions/0048`, ADR-024 Update 2026-09-25): Settable 0x00 = the Buds refuse a Set (NAK 0x02); non-zero = allowed.
                    val availability = AncAvailability.fromSettableToggles(anc.settableToggles)
                    updateAncAvailability(availability)
                    anc.currentMode?.let {
                        // F-3: the Buds' own report — whatever a cut-off answer would have said, this is their mode now.
                        _ancModeUnconfirmedAt.value = null
                        emitAncMode(it, causeOfNotify(it, answersGet))
                    }
                }
                is AncFrame.Set -> emitAncMode(anc.mode)
                is AncFrame.Get -> Unit
            }

            // ACK/NAK for any Message Stream command (0044 APP-3): the command that waits picks its own by the echoed Group/Code.
            is RoutedFrame.Reply -> _replies.tryEmit(frame.reply)

            // The Buds' Fast Pair Model ID for this claim (ADR-042's gate).
            is RoutedFrame.ModelId -> {
                _modelIdThisClaim.value = frame.frame.modelIdHex
                modelIdSeen = frame.frame.modelIdHex
                refreshObservedSafeMode()
            }

            // Field 16 is the *active* EQ (ADR-034); field 18 (the last saved custom curve) must never replace it.
            is RoutedFrame.Eq -> {
                if (!frame.frame.persist) {
                    updateEqProfile(frame.frame.gains)
                    _eqError.value = null
                }
                _maestroReplies.tryEmit(MaestroReply.Value(frame.frame, frame.stream))
            }

            is RoutedFrame.MaestroHello -> {
                BleLogger.logConnectionEvent("Maestro channel announced by the Buds: ${frame.channelId}")
                _maestroChannelId.value = frame.channelId
                if (frame.firmware.isNotEmpty()) {
                    // + entries/channel/time for Info (F-5); the serials already read on this connection stay (ADR-058).
                    _deviceInfo.update { DeviceInfo(frame.firmware, frame.entries, frame.channelId, clock(), it?.serials ?: emptyList(), it?.serialsReadAtMillis) }
                    refreshObservedSafeMode()
                }
            }

            // ADR-058 (`ai-sessions/0082`): the answer to the app's one GetHardwareInfo per Connect — field 7's serials, with their receive time. Never logged.
            is RoutedFrame.HardwareInfo -> {
                applySerials(frame.serials, clock())
                _maestroReplies.tryEmit(MaestroReply.Hardware(frame.serials.size))
            }

            is RoutedFrame.RpcResult -> {
                if (frame.methodId == Maestro.METHOD_WRITE_SETTING && clock() < writeQuarantineUntil) {
                    // A58-APP-04: the late answer to a write that already timed out — nothing waits for it, it is dropped (no payload logged).
                    BleLogger.logConnectionEvent("Late WriteSetting answer dropped (its write had already timed out)")
                }
                _maestroReplies.tryEmit(MaestroReply.Result(frame))
            }

            // ADR-036/045 (`ai-sessions/0052`): a setting the Buds reported — a read answer or, since ADR-064/065 (`ai-sessions/0091`), a settings-stream
            // push. Applied with its receive time; the last value received wins, whatever its channel (ADR-065 item 3).
            is RoutedFrame.Setting -> {
                val value = frame.value
                if (value is SettingValue.AncState) applyAncState(value, frame.stream) else applySetting(value, clock())
                _maestroReplies.tryEmit(MaestroReply.Setting(value, frame.stream))
            }

            // ADR-065 item 2: a pushed change of a field this app does not read — said (payload-free, AGENTS.md §9), not shown, not an UnidentifiedFrame.
            is RoutedFrame.UnreadSetting -> BleLogger.logConnectionEvent("Settings stream: field ${frame.field} not shown (not read by this app)")

            // DECISIONS.md ADR-069 (`ai-sessions/0098`, 🟢 FIND-005 — PROTOCOL.md §4.4 Update 2026-10-17): the Buds' own ringing status `04 01 00 01 xx`,
            // sent once to the Message Stream holder after a Ring and at its stop (`CAP-078` 7775, 7816).
            is RoutedFrame.Ring -> onRingStatus(frame.frame)

            // ADR-033. A byte the decoder does not interpret arrives as Unavailable and *replaces* any
            // earlier Known value: a stale percentage must never linger once the Buds report a
            // regime we cannot read (AGENTS.md §5). The Case is untouched: b3 is 0xff on every claim (ADR-033, 60/60 in ai-sessions/0042).
            // I-8 (`ai-sessions/0048`): each bud's charging bit is also a charging report, newest wins against the runtime-info stream.
            is RoutedFrame.Battery -> {
                val now = clock()
                updateBatteryStatus {
                    it.copy(
                        left = frame.frame.left.stamped(now),
                        right = frame.frame.right.stamped(now),
                        leftCharging = frame.frame.left.chargingReading(now) ?: it.leftCharging,
                        rightCharging = frame.frame.right.chargingReading(now) ?: it.rightCharging,
                    )
                }
            }

            // ADR-035's DLCI 0x08 decode stays in the codec, but the app no longer opens that channel (ADR-043) — nothing arrives here.
            is RoutedFrame.CaseBattery -> Unit

            // ADR-043 and its 2026-09-25 Update (`ai-sessions/0048` I-4/I-5/I-8): the Case, and each bud's charging state, from the runtime-info
            // stream. A packet without the Case entry (no bud charging) keeps the last Case value as "last seen" with its own time — a dated
            // last-seen value, never a fabricated or silently carried-over one (AGENTS.md §5).
            is RoutedFrame.RuntimeInfo -> {
                val now = clock()
                val info = frame.info
                // ADR-068 (`ai-sessions/0098`): Left/Right from 6.2/6.3 as the Case — an absent entry keeps the last value "last seen" with its own time.
                fun current(reported: BatteryLevel?, kept: BatteryLevel): BatteryLevel = when (reported) {
                    null -> (kept as? BatteryLevel.Known)?.copy(isStale = true) ?: kept
                    is BatteryLevel.Known -> reported.copy(receivedAtMillis = now)
                    BatteryLevel.Unavailable -> BatteryLevel.Unavailable
                }
                updateBatteryStatus {
                    it.copy(
                        case = current(info.case, it.case),
                        left = current(info.left, it.left),
                        right = current(info.right, it.right),
                        leftCharging = info.leftCharging?.let { c -> ChargingReading(c, now, ChargingSource.RUNTIME_INFO) } ?: it.leftCharging,
                        rightCharging = info.rightCharging?.let { c -> ChargingReading(c, now, ChargingSource.RUNTIME_INFO) } ?: it.rightCharging,
                    )
                }
                _caseBatteryError.value = null
                _runtimeInfoPackets.tryEmit(Unit)
            }
        }
    }

    override suspend fun connect(): BudsResult<Unit> {
        reopener.onUserConnect()
        return openSession()
    }

    private suspend fun openSession(): BudsResult<Unit> = connectMutex.withLock {
        // Already connected (e.g. a stale second tap): nothing to do — re-running connect() would tear
        // the live connection down (RfcommBudsTransport.connect() replaces any current one).
        if (connectionStateMachine.state.value is ConnectionState.Ready) return@withLock BudsResult.Success(Unit)
        // ARCHITECTURE.md §3.1: a fresh connection's EQ is unknown until read — reset here, before anything can reach Ready, so it
        // can never race the Connect-time read (0044 APP-7: an asynchronous Ready collector on Dispatchers.Default used to do this).
        updateEqProfile(null)
        // Likewise the settings (ADR-036): unknown until this connection's reads answer — never an earlier session's values shown as current.
        _settings.value = BudsSettings()
        // I-5 (`0048`) and I-4 (`ai-sessions/0054`): the Case, each bud's percentage and each bud's charging state from an earlier session are shown as
        // "last seen" / "from the last connection" (with their own times) until this connection's reports replace them; kept only in memory, never
        // persisted (ARCHITECTURE.md §3.1). Before the bonded-device lookup: they are the previous connection's, whatever this attempt does.
        _batteryStatus.update { it.markedFromEarlierSession() }
        // A68-APP-02 (`ai-sessions/0069`): from here on, whatever the Buds reported earlier is "from the last connection" — the ANC mode is kept and
        // judged by this time ([isCurrent]); it becomes current again with this connection's first Notify or ACK.
        _sessionSince.value = clock()
        // ADR-065 item 4 (the maintainer's choice in chat 2026-10-14, `ai-sessions/0091` Q4): the kept mode and its cause stay — this connection's first read
        // that differs from it is "changed since the app last saw it" (before `0091`, item 1 of `ai-sessions/0082` reset the cause here and a connection's
        // first read was always a reading). The Settable byte is re-read (I-3: unknown ⇒ enabled; ADR-059's reading then starts at "—" with the settings,
        // which are reset above).
        _ancAvailability.value = AncAvailability.UNKNOWN // re-read by this connection's ReadSetting 4:13 (ADR-068)
        // 0044 APP-8: no bonded device is "not paired", not a missing permission (AGENTS.md §8 — a specific message per cause).
        val device = bondedDeviceProvider()
            ?: return@withLock BudsResult.Failure(BudsError.NotPaired)
        _lastConnectionError.value = null
        clearLoss()
        _eqError.value = null
        _ancError.value = null
        _maestroChannelId.value = null // announced afresh by this connection's first Buds packet (ADR-034)
        _deviceInfo.value = null
        _caseBatteryError.value = null
        _serialsError.value = null
        _batteryRefreshError.value = null
        _settingsError.value = null
        _safeMode.value = null
        _modelIdThisClaim.value = null
        modelIdSeen = null
        lastInboundChannel = null
        codecRouter.resetAll() // a new connection starts with empty per-channel buffers (0044 APP-2)
        val generation = disconnectGeneration.get()
        connectionStateMachine.onConnectRequested()
        // ADR-032: the session is the MAESTRO channel only. The Message Stream channel (DLCI 0x04) is shared
        // with Google Play services' Fast Pair, so it is claimed on demand, never held by Connect.
        val channels = mapOf(Dlci.MAESTRO to BudsSdpUuids.MAESTRO)
        try {
            when (val result = transport.connect(device, channels)) {
                is BudsResult.Success if disconnectGeneration.get() != generation -> {
                    // A68-APP-13: the user disconnected while the sockets were opening — close them again; the Disconnect's state stands.
                    BleLogger.logConnectionEvent("Connect finished after a Disconnect tap: its channels are closed again")
                    transport.disconnect()
                    connectionStateMachine.onDisconnected()
                    BudsResult.Failure(BudsError.ConnectionLost)
                }
                is BudsResult.Success -> {
                    connectionStateMachine.onLinkEstablished()
                    // `Discovering` is a zero-length pass-through (ARCHITECTURE.md §2.1): SDP resolution already happened inside
                    // createRfcommSocketToServiceRecord(); `Ready` means "the MAESTRO channel is open" (ADR-032).
                    connectionStateMachine.onReady()
                    // DECISIONS.md ADR-068 (`ai-sessions/0098`): no Message Stream claim at Connect — the mode comes from `ReadSetting 4:13` and the settings
                    // stream, Left/Right/Case from the runtime-info stream, all on this channel.
                    launchInitialEqRead()
                    BudsResult.Success(Unit)
                }
                is BudsResult.Failure -> {
                    connectionStateMachine.onError(result.error)
                    result
                }
            }
        } catch (e: CancellationException) {
            // 0044 APP-4: a cancelled Connect must not leave the state stuck at Connecting with half-open sockets.
            withContext(NonCancellable) {
                transport.disconnect()
                connectionStateMachine.onDisconnected()
            }
            throw e
        }
    }

    override suspend fun disconnect(): BudsResult<Unit> {
        disconnectGeneration.incrementAndGet()
        reopener.onUserDisconnect()
        BleLogger.logConnectionEvent(SessionDiagnostics.userDisconnectLine(connectionStateMachine.state.value::class.simpleName ?: "?"))
        _lastConnectionError.value = null
        clearLoss()
        _messageStreamError.value = null
        _eqError.value = null
        _ancError.value = null
        _caseBatteryError.value = null
        _serialsError.value = null
        _batteryRefreshError.value = null
        _settingsError.value = null
        _deviceInfo.value = null
        // Not the ring notice (I-6): the ring keeps sounding after Disconnect until Stop is sent (`CAP-062` frame 9772, heard until 06:53:50).
        markRingFromEarlierSession()
        markValuesFromEndedSession()
        _safeMode.value = null
        cancelClaimJobs()
        transport.disconnect()
        codecRouter.resetAll()
        connectionStateMachine.onDisconnected()
        return BudsResult.Success(Unit)
    }

    /**
     * The noise-control mode (DECISIONS.md ADR-067, `ai-sessions/0098`): one `WriteSetting 4:{13:n}` on the session channel — no Message Stream claim.
     */
    override suspend fun setAncMode(mode: AncMode): BudsResult<Unit> = writeAncMode { _ -> mode }

    /**
     * The tile: [next] of the shown mode, stepped on while the result is not ticked in the press-and-hold list (ADR-067 item 3 — at most once around the four
     * modes; the list is read under the Maestro lock, so the step and the write see the same list).
     */
    override suspend fun stepAncMode(next: (current: AncMode?) -> AncMode): BudsResult<Unit> = writeAncMode { list ->
        var mode = next(_ancMode.value)
        repeat(AncMode.entries.size - 1) { if (!list.isSelected(mode)) mode = next(mode) }
        mode
    }

    /**
     * ADR-067: the write itself, through the settings write path's lock, channel, Safe-Mode gate (firmware) and quarantine. [choose] gets the press-and-hold
     * list in force (field 12) and returns the mode to write; a mode not ticked there, or an unread list, is refused before anything is sent (ADR-067 item 3
     * and its Note of 2026-10-17). Done on `RESPONSE` status 0 — the mode is shown as set by this app (the Buds' push of it within [ANC_SET_WINDOW_MS] is
     * the app's own, [causeOfStream]); status 9 `FAILED_PRECONDITION` is [BudsError.AncNotAllowed] (`CAP-078` H 6775 → 6777, no bud worn) and marks the
     * mode not changeable; another status is [BudsError.MaestroRejected]; no answer is [BudsError.Timeout] and starts the write quarantine. The previous
     * mode stays on every failure.
     */
    private suspend fun writeAncMode(choose: (list: io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList) -> AncMode): BudsResult<Unit> {
        when (connectionStateMachine.state.value) {
            is ConnectionState.Ready -> Unit
            is ConnectionState.Connecting, is ConnectionState.Discovering -> return ancFailed(BudsError.SessionOpening)
            else -> return ancFailed(BudsError.ConnectionLost)
        }
        return eqMutex.withLock {
            val channel = when (val c = awaitMaestroChannel()) {
                is BudsResult.Failure -> return@withLock ancFailed(c.error)
                is BudsResult.Success -> c.value
            }
            // ADR-042: the Safe-Mode gate first — on an unverified firmware the refusal says why, whatever the list.
            writeGate(requireModelIdOfClaim = false)?.let { return@withLock ancFailed(it) }
            val list = _settings.value.ancModeList?.value ?: return@withLock ancFailed(BudsError.AncModeListNotRead)
            val mode = choose(list)
            if (!list.isSelected(mode)) return@withLock ancFailed(BudsError.AncModeNotInHoldList(mode))
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(SettingsCodec.ancStateRequest(channel.channelId, mode)))
            awaitWriteQuarantine()
            // Item 1 (`ai-sessions/0082`) / ADR-065 item 4: while this write waits, and for [ANC_SET_WINDOW_MS] after it, a push of this mode is the app's own.
            setPendingMode = mode
            lastSetMode = mode
            lastSetAt = clock()
            val (sent, reply) = try {
                awaitWriteOrQuarantine { sendAndAwait(_maestroReplies, EQ_WRITE_ACK_TIMEOUT_MS, { it.isResultFor(Maestro.METHOD_WRITE_SETTING) }) { transport.send(Dlci.MAESTRO, wire) } }
            } finally {
                setPendingMode = null
            }
            when {
                sent is BudsResult.Failure -> ancFailed(sent.error)
                reply == null -> {
                    startWriteQuarantine()
                    ancFailed(BudsError.Timeout)
                }
                reply is MaestroReply.Result && reply.result.isOk -> {
                    emitAncMode(mode, AncModeCause.SET_BY_APP)
                    _ancModeUnconfirmedAt.value = null
                    _ancError.value = null
                    BleLogger.logConnectionEvent("Noise-control mode written (channel ${channel.channelId}): ${mode.label}")
                    BudsResult.Success(Unit)
                }
                reply is MaestroReply.Result && reply.result.status == PW_STATUS_FAILED_PRECONDITION -> {
                    updateAncAvailability(AncAvailability.NOT_ALLOWED, fromClaim = false)
                    BleLogger.logConnectionEvent("Noise-control mode not written: the Buds allow no change (FAILED_PRECONDITION — no bud worn?)")
                    ancFailed(BudsError.AncNotAllowed)
                }
                reply is MaestroReply.Result ->
                    ancFailed(BudsError.MaestroRejected("${PwRpc.typeName(reply.result.type)} ${PwRpc.statusName(reply.result.status)}"))
                else -> ancFailed(BudsError.Unknown(IllegalStateException("unexpected reply to WriteSetting")))
            }
        }
    }

    private fun <T> ancFailed(error: BudsError): BudsResult<T> {
        BleLogger.logConnectionEvent("Noise-control request failed: ${eqErrorLogText(error)}")
        _ancError.value = error
        return BudsResult.Failure(error)
    }

    /** A Message Stream command the Buds refused (NAK) — logged with the reason's name (Find's Ring/Stop). */
    private fun rejected(reason: Int): BudsError {
        val name = MessageStreamAck.reasonName(reason)
        BleLogger.logConnectionEvent("Message Stream command rejected by the Buds: NAK $name")
        return BudsError.CommandRejected(reason, name)
    }

    /** ADR-067/068: a Refresh is one `ReadSetting 4:13` on the session channel; 0 (no mode settable) is [BudsError.AncNotAllowed]. */
    override suspend fun refreshAncMode(): BudsResult<AncMode> {
        when (connectionStateMachine.state.value) {
            is ConnectionState.Ready -> Unit
            is ConnectionState.Connecting, is ConnectionState.Discovering -> return ancFailed(BudsError.SessionOpening)
            else -> return ancFailed(BudsError.ConnectionLost)
        }
        return when (val read = readAncSetting()) {
            is BudsResult.Failure -> ancFailed(read.error)
            is BudsResult.Success -> read.value?.let {
                _ancError.value = null
                BudsResult.Success(it)
            } ?: ancFailed(BudsError.AncNotAllowed)
        }
    }

    override suspend fun setEqGains(gains: EqBandGains): BudsResult<Unit> {
        when (connectionStateMachine.state.value) {
            is ConnectionState.Ready -> Unit
            // As writeSetting's U-2 (`ai-sessions/0056`; 0058 A58-APP-08): a re-open says so on the EQ tab; nothing is sent and nothing is queued.
            is ConnectionState.Connecting, is ConnectionState.Discovering -> return eqFailed(BudsError.SessionOpening)
            else -> return BudsResult.Failure(BudsError.ConnectionLost)
        }
        // A68-APP-11 (`ai-sessions/0069`): a band that is not a finite number is refused here — nothing is encoded, nothing is sent.
        val clamped = gains.clampedOrNull()
            ?: return eqFailed(BudsError.Unknown(IllegalArgumentException("an equalizer gain is not a number")))
        return eqMutex.withLock {
            val channel = when (val c = awaitMaestroChannel()) {
                is BudsResult.Failure -> return@withLock eqFailed(c.error)
                is BudsResult.Success -> c.value
            }
            writeGate(requireModelIdOfClaim = false)?.let { return@withLock eqFailed(it) }
            val payload = EqFrameEncoder.encode(EqFrame(clamped, persist = false, channelId = channel.channelId))
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, payload)
            awaitWriteQuarantine()
            val (sent, reply) = awaitWriteOrQuarantine { sendAndAwait(_maestroReplies, EQ_WRITE_ACK_TIMEOUT_MS, { it.isResultFor(Maestro.METHOD_WRITE_SETTING) }) {
                transport.send(Dlci.MAESTRO, wire)
            } }
            when {
                sent is BudsResult.Failure -> eqFailed(sent.error)
                reply == null -> {
                    startWriteQuarantine()
                    eqFailed(BudsError.Timeout)
                }
                reply is MaestroReply.Result && reply.result.isOk -> {
                    updateEqProfile(clamped)
                    _eqError.value = null
                    // `CAP-077` P1 (`ai-sessions/0098`): the EQ write is logged like every other write — before, only the pw_rpc summary line said it.
                    BleLogger.logConnectionEvent("EQ written (channel ${channel.channelId})")
                    BudsResult.Success(Unit)
                }
                reply is MaestroReply.Result -> eqFailed(
                    BudsError.MaestroRejected("${PwRpc.typeName(reply.result.type)} ${PwRpc.statusName(reply.result.status)}"),
                )
                else -> eqFailed(BudsError.Unknown(IllegalStateException("unexpected reply to WriteSetting")))
            }
        }
    }

    override suspend fun refreshEq(): BudsResult<EqBandGains> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) return BudsResult.Failure(BudsError.ConnectionLost)
        return readEq()
    }

    /**
     * `ai-sessions/0057` D-11: the Connect-time settings pass ([readSettings], unchanged) once more on a user pull — nothing is sent while the session is not
     * `Ready` (as [refreshEq]); the last failure is cleared first, as at Connect, so [settingsError] then says only what this pass could not read.
     */
    override suspend fun refreshSettings(): BudsResult<Unit> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) return BudsResult.Failure(BudsError.ConnectionLost)
        _settingsError.value = null
        readSettings()
        return _settingsError.value?.let { BudsResult.Failure(it.error) } ?: BudsResult.Success(Unit)
    }

    /**
     * `ReadSetting 4:16` (DECISIONS.md ADR-034): waits for the Buds' channel announcement, sends the request on that
     * channel, and waits for the value — or for an error result. Nothing is retried; a failure is reported with its reason.
     * A fresh client needs no opening message: the app's request was answered at every Connect of `CAP-062`/`CAP-063`
     * (`CAP-059-FINDINGS.md`; `ai-sessions/0058` A58-APP-07).
     */
    private suspend fun readEq(): BudsResult<EqBandGains> = eqMutex.withLock {
        val channel = when (val c = awaitMaestroChannel()) {
            is BudsResult.Failure -> return@withLock eqFailed(c.error)
            is BudsResult.Success -> c.value
        }
        val request = Maestro.readSettingRequest(channel.channelId, Maestro.FIELD_EQ_ACTIVE)
            ?: return@withLock eqFailed(BudsError.SettingNotReadable)
        val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(request))
        // An OK answer that is not a decodable EQ value ends the wait too (A58-APP-03): reported as unreadable, not as "no answer".
        val accept: (MaestroReply) -> Boolean = {
            (it is MaestroReply.Value && !it.stream && !it.frame.persist) ||
                (it is MaestroReply.Result && it.result.methodId == Maestro.METHOD_READ_SETTING)
        }
        awaitWriteQuarantine()
        val (sent, reply) = sendAndAwait(_maestroReplies, EQ_READ_TIMEOUT_MS, accept) { transport.send(Dlci.MAESTRO, wire) }
        when {
            sent is BudsResult.Failure -> eqFailed(sent.error)
            reply == null -> eqFailed(BudsError.Timeout)
            reply is MaestroReply.Result && reply.result.isOk -> eqFailed(BudsError.UnreadableAnswer)
            reply is MaestroReply.Value -> {
                BleLogger.logConnectionEvent("EQ read ok (channel ${channel.channelId})")
                updateEqProfile(reply.frame.gains)
                _eqError.value = null
                BudsResult.Success(reply.frame.gains)
            }
            reply is MaestroReply.Result -> eqFailed(
                BudsError.MaestroRejected("${PwRpc.typeName(reply.result.type)} ${PwRpc.statusName(reply.result.status)}"),
            )
            else -> eqFailed(BudsError.Unknown(IllegalStateException("unexpected reply to ReadSetting")))
        }
    }

    /** The channel the Buds announced and its known request address — or the reason there is none (never guessed). */
    internal suspend fun awaitMaestroChannel(): BudsResult<MaestroChannel> { // (experiment seam)
        val id = _maestroChannelId.value
            ?: withTimeoutOrNull(MAESTRO_ANNOUNCE_WAIT_MS) { _maestroChannelId.filterNotNull().first() }
            ?: return BudsResult.Failure(BudsError.MaestroChannelUnknown(null))
        return MaestroChannel.forChannel(id)?.let { BudsResult.Success(it) }
            ?: BudsResult.Failure(BudsError.MaestroChannelUnknown(id))
    }

    /** Waits out a running write quarantine (A58-APP-04) — one bounded delay, called inside [eqMutex] right before a Maestro request is sent. */
    internal suspend fun awaitWriteQuarantine() { // (experiment seam)
        val until = writeQuarantineUntil
        val now = clock()
        if (now < until) { // compare first: `until - now` would overflow while `until` is still Long.MIN_VALUE
            val remaining = until - now
            BleLogger.logConnectionEvent("Maestro request held ${remaining} ms: an earlier WriteSetting got no answer (its late answer must not be taken as this one's)")
            delay(remaining)
        }
    }

    internal fun startWriteQuarantine() { // (experiment seam)
        writeQuarantineUntil = clock() + EQ_WRITE_ACK_TIMEOUT_MS
    }

    /**
     * `ai-sessions/0068` A68-APP-13 (fixed `ai-sessions/0098`): a write cancelled while it waits for its answer (a tap's coroutine ended, a Disconnect) starts the
     * quarantine as a timeout does — its late answer must not be taken as the next request's (A58-APP-04: pw_rpc answers carry no call id to match).
     */
    private suspend fun <T> awaitWriteOrQuarantine(wait: suspend () -> T): T = try {
        wait()
    } catch (e: CancellationException) {
        startWriteQuarantine()
        throw e
    }

    private fun <T> eqFailed(error: BudsError): BudsResult<T> {
        BleLogger.logConnectionEvent("EQ request failed: ${eqErrorLogText(error)}")
        _eqError.value = error
        return BudsResult.Failure(error)
    }

    private fun eqErrorLogText(error: BudsError): String = when (error) {
        is BudsError.MaestroChannelUnknown -> "no usable Maestro channel (announced: ${error.channelId})"
        is BudsError.MaestroRejected -> "rejected by the Buds (${error.detail})"
        BudsError.Timeout -> "no answer from the Buds"
        BudsError.UnreadableAnswer -> "answered with a value this app cannot read (raw bytes: Debug mode hex dump only)"
        else -> error::class.simpleName ?: "error"
    }

    override suspend fun applyEqPreset(preset: EqPreset): BudsResult<Unit> = setEqGains(preset.gains)

    // ---- settings writes (DECISIONS.md ADR-045 … ADR-047, ADR-052 … ADR-055) ----------------------------------------------------------------------

    override suspend fun setVolumeBalance(value: Int): BudsResult<Unit> {
        val v = value.coerceIn(BudsSettings.BALANCE_RANGE)
        return writeSetting(SettingValue.Balance(v)) { SettingsCodec.balanceRequest(it, v) }
    }

    override suspend fun setMonoAudio(on: Boolean) = writeFlag(SettingsCodec.FIELD_MONO_AUDIO, on)

    override suspend fun setConversationDetection(on: Boolean) = writeFlag(SettingsCodec.FIELD_CONVERSATION_DETECTION, on)

    override suspend fun setTouchControls(on: Boolean) = writeFlag(SettingsCodec.FIELD_TOUCH_CONTROLS, on)

    override suspend fun setPressAndHold(bud: Bud, action: HoldAction): BudsResult<Unit> = writeSetting(
        SettingValue.PressAndHold(left = action.takeIf { bud == Bud.LEFT }, right = action.takeIf { bud == Bud.RIGHT }),
    ) { SettingsCodec.pressAndHoldRequest(it, bud, action) }

    override suspend fun setInEarDetection(on: Boolean) = writeFlag(SettingsCodec.FIELD_IN_EAR_DETECTION, on)

    /** ADR-053: the same write path, lock and quarantine as every other flag; the SASS answer on DLCI 0x04 is not waited for. */
    override suspend fun setMultipoint(on: Boolean) = writeFlag(SettingsCodec.FIELD_MULTIPOINT, on)

    /** ADR-054: case sounds "Other alerts" (27) and "Earbuds replaced" (28) — plain 0/1 flags on the same write path. */
    override suspend fun setCaseSoundOtherAlerts(on: Boolean) = writeFlag(SettingsCodec.FIELD_CASE_SOUND_OTHER_ALERTS, on)

    override suspend fun setCaseSoundEarbudsReplaced(on: Boolean) = writeFlag(SettingsCodec.FIELD_CASE_SOUND_EARBUDS_REPLACED, on)

    /** ADR-055: "Volume EQ" (field 15), a 0/1 flag on the same write path (the channel-21 "on" frame is still to be seen on the wire — `SettingsCodec`). */
    override suspend fun setVolumeEq(on: Boolean) = writeFlag(SettingsCodec.FIELD_VOLUME_EQ, on)

    /** ADR-052: field 29 is 1/2 on the wire — its own value type and request, through the same write path, lock and quarantine as every flag. */
    override suspend fun setHeadGestures(on: Boolean): BudsResult<Unit> =
        writeSetting(SettingValue.HeadGestures(on)) { SettingsCodec.headGesturesRequest(it, on) }

    /**
     * ADR-046: the new list is built from the one the Buds last reported — inside the Maestro lock, so a second tap cannot build on a value the first tap's
     * write has not been acknowledged for yet — and refused (nothing sent) when unread or when fewer than two modes would stay ticked.
     */
    override suspend fun setAncModeSelected(mode: AncMode, selected: Boolean): BudsResult<Unit> = writeSetting(
        prepare = {
            val current = _settings.value.ancModeList?.value
            val next = current?.with(mode, selected)
            when {
                next == null -> BudsResult.Failure(BudsError.AncModeListNotRead)
                !next.isValid -> BudsResult.Failure(BudsError.AncModeListTooShort)
                else -> BudsResult.Success(SettingValue.AncModes(next))
            }
        },
        request = { channelId, value -> SettingsCodec.ancModeListRequest(channelId, (value as SettingValue.AncModes).list) },
    )

    private suspend fun writeFlag(field: Int, on: Boolean): BudsResult<Unit> =
        writeSetting(SettingValue.Flag(field, on)) { SettingsCodec.flagRequest(it, field, on) }

    private suspend fun writeSetting(value: SettingValue, request: (channelId: Int) -> RpcPacket?): BudsResult<Unit> =
        writeSetting(prepare = { BudsResult.Success(value) }, request = { channelId, _ -> request(channelId) })

    /**
     * One `WriteSetting` (ADR-045) on the announced channel with its ADR-034 address, after the Safe-Mode gate (ADR-042). The value is applied
     * only on the Buds' empty `RESPONSE` with status OK — never optimistically; an error status, no answer within [EQ_WRITE_ACK_TIMEOUT_MS] or a
     * refused gate keeps the previous value and puts the reason in [settingsError]. Serialised with every other Maestro request ([eqMutex]).
     */
    private suspend fun writeSetting(
        prepare: () -> BudsResult<SettingValue>,
        request: (channelId: Int, value: SettingValue) -> RpcPacket?,
    ): BudsResult<Unit> {
        when (connectionStateMachine.state.value) {
            is ConnectionState.Ready -> Unit
            // U-2 (`ai-sessions/0056`): the session is being (re)opened (ADR-044 or a Connect) — say so; nothing is sent and nothing is queued.
            is ConnectionState.Connecting, is ConnectionState.Discovering -> return settingFailed(BudsError.SessionOpening)
            else -> return settingFailed(BudsError.ConnectionLost)
        }
        return eqMutex.withLock {
            val value = when (val p = prepare()) {
                is BudsResult.Failure -> return@withLock settingFailed(p.error)
                is BudsResult.Success -> p.value
            }
            val channel = when (val c = awaitMaestroChannel()) {
                is BudsResult.Failure -> return@withLock settingFailed(c.error)
                is BudsResult.Success -> c.value
            }
            writeGate(requireModelIdOfClaim = false)?.let { return@withLock settingFailed(it) }
            val packet = request(channel.channelId, value)
                ?: return@withLock settingFailed(BudsError.Unknown(IllegalStateException("setting ${value.field} is not writable")))
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(packet))
            awaitWriteQuarantine()
            val (sent, reply) = awaitWriteOrQuarantine { sendAndAwait(_maestroReplies, EQ_WRITE_ACK_TIMEOUT_MS, { it.isResultFor(Maestro.METHOD_WRITE_SETTING) }) {
                transport.send(Dlci.MAESTRO, wire)
            } }
            when {
                sent is BudsResult.Failure -> settingFailed(sent.error)
                reply == null -> {
                    startWriteQuarantine()
                    settingFailed(BudsError.Timeout)
                }
                reply is MaestroReply.Result && reply.result.isOk -> {
                    applySetting(value, clock(), changedByApp = true)
                    _settingsError.value = null
                    BleLogger.logConnectionEvent("Setting ${value.field} written (channel ${channel.channelId})")
                    BudsResult.Success(Unit)
                }
                reply is MaestroReply.Result ->
                    settingFailed(BudsError.MaestroRejected("${PwRpc.typeName(reply.result.type)} ${PwRpc.statusName(reply.result.status)}"))
                else -> settingFailed(BudsError.Unknown(IllegalStateException("unexpected reply to WriteSetting")))
            }
        }
    }

    private fun settingFailed(error: BudsError): BudsResult<Unit> {
        BleLogger.logConnectionEvent("Setting write failed: ${eqErrorLogText(error)}")
        _settingsError.value = SettingsFailure(error, write = true)
        return BudsResult.Failure(error)
    }

    override suspend fun ringBud(target: RingTarget): BudsResult<Unit> {
        endRingHold() // a new Ring while one is held: the hold ends first (its claim is reused)
        val result = withMessageStream {
            val sent = sendRing(RingFrame.Start(target))
            // The ring keeps sounding after the channel is released (`ai-sessions/0042`, heard on the recording), so this state stays
            // until Stop succeeds or the Buds report the stop (ADR-069) — a failed, refused or unanswered ring is never claimed.
            if (sent is BudsResult.Success) {
                _ringing.value = RingNotice(target)
                _ringStoppedOnBudAt.value = null
            }
            sent
        }
        if (result is BudsResult.Success) startRingHold()
        return result
    }

    override suspend fun stopRinging(): BudsResult<Unit> {
        endRingHold()
        return withMessageStream {
            stopSentAt = clock()
            val result = sendRing(RingFrame.Stop)
            if (result is BudsResult.Success) _ringing.value = null
            result
        }
    }

    /**
     * DECISIONS.md ADR-069 item 3: hold the Ring's claim — the release scheduled by [withMessageStream] is cancelled under [claimMutex] — until the Buds report
     * the stop, the user's Stop or a new Ring ([endRingHold]), the channel's close by another client (Play services, ≈ 4.5 s — item 4: no re-claim) or
     * [RING_HOLD_MAX_MS]; then the usual release after [MESSAGE_STREAM_LINGER_MS]. Not a timer that sends anything: it only keeps the socket open.
     */
    private fun startRingHold() {
        val startedAt = clock()
        ringHoldJob = scope.launch {
            try {
                claimMutex.withLock {
                    releaseJob?.cancel()
                    releaseJob = null
                    if (!transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM)) return@withLock
                    BleLogger.logConnectionEvent("Ring: claim held until the Buds report the stop (at most ${RING_HOLD_MAX_MS / 1000} s)")
                    coroutineScope {
                        val stopped = async(start = CoroutineStart.UNDISPATCHED) { _ringStoppedOnBudAt.first { it != null && it >= startedAt } }
                        val closed = async(start = CoroutineStart.UNDISPATCHED) {
                            transport.channelClosed.first { it.channelId == Dlci.FAST_PAIR_MESSAGE_STREAM }
                        }
                        val why = withTimeoutOrNull(RING_HOLD_MAX_MS) {
                            select<String> {
                                stopped.onAwait { "the Buds reported the stop" }
                                closed.onAwait { "the claim was closed by another client" }
                            }
                        } ?: "no stop report within ${RING_HOLD_MAX_MS / 1000} s"
                        stopped.cancel()
                        closed.cancel()
                        BleLogger.logConnectionEvent("Ring: hold ended — $why")
                    }
                }
            } finally {
                scheduleRelease()
            }
        }
    }

    /** Ends a running Ring hold (the user's Stop, a new Ring, Disconnect) and waits until its claim is free again. */
    private suspend fun endRingHold() {
        ringHoldJob?.let {
            it.cancel()
            it.join()
        }
        ringHoldJob = null
    }

    /**
     * DECISIONS.md ADR-069 items 1–2: a ringing status from the Buds. While the app holds the claim it is ACKed with `ff 01 00 02 04 01` (the specification's
     * Seeker rule; the official app's `CAP-025` 2048, `CAP-078` B2 7970); `01`/`02`/`03` mark the ring as running on that side, `00` ends it — "stopped on the
     * bud" unless it answers the app's own Stop. Nothing else is sent.
     */
    private fun onRingStatus(frame: RingFrame) {
        val now = clock()
        val text = when (frame) {
            is RingFrame.Start -> "ringing ${frame.target.name.lowercase()}"
            RingFrame.Stop -> "stopped"
        }
        BleLogger.logConnectionEvent("Ring status from the Buds: $text")
        if (transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM)) {
            scope.launch {
                if (transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, RING_STATUS_ACK) is BudsResult.Success) {
                    BleLogger.logConnectionEvent("Ring status acknowledged (ff 01 00 02 04 01)")
                }
            }
        }
        when (frame) {
            is RingFrame.Start -> _ringing.value = (_ringing.value?.copy(target = frame.target) ?: RingNotice(frame.target)).copy(fromEarlierSession = false)
            RingFrame.Stop -> {
                _ringing.value = null
                // `Long.MIN_VALUE` = no Stop sent yet (compared first: `now - Long.MIN_VALUE` would overflow).
                if (stopSentAt == Long.MIN_VALUE || now - stopSentAt > ACK_WAIT_MS + MESSAGE_STREAM_LINGER_MS) _ringStoppedOnBudAt.value = now
            }
        }
    }

    /** One Ring command: gated (ADR-042), then its ACK (done), NAK ([BudsError.CommandRejected]) or nothing within 1 s — which the
     * Device Action spec says the Seeker must treat as "not supported" ([BudsError.Timeout]). */
    private suspend fun sendRing(frame: RingFrame): BudsResult<Unit> {
        writeGate(requireModelIdOfClaim = true)?.let { return BudsResult.Failure(it) }
        val (result, reply) = sendAndAwait(_replies, ACK_WAIT_MS, { it.answers(RingMessageStream.GROUP, RingMessageStream.CODE_RING) }) {
            transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, RingFrameEncoder.encode(frame))
        }
        return when {
            result is BudsResult.Failure -> result
            reply == null -> BudsResult.Failure(BudsError.Timeout)
            reply is MessageStreamReply.Nak -> BudsResult.Failure(rejected(reply.reason))
            else -> BudsResult.Success(Unit)
        }
    }

    // ---- Safe Mode (ARCHITECTURE.md §8.1, DECISIONS.md ADR-042) -----------------------------------------------------------

    /**
     * `null` when a write may be sent; otherwise the reason it may not ([BudsError.UnsupportedFirmware], with [safeMode] set).
     * Waits a bounded time for what the Buds announce on their own (firmware at connect; Model ID right after a claim opens) —
     * never guesses either.
     */
    internal suspend fun writeGate(requireModelIdOfClaim: Boolean): BudsError? { // (experiment seam)
        val firmware = _deviceInfo.value?.firmware
            ?: withTimeoutOrNull(MAESTRO_ANNOUNCE_WAIT_MS) { _deviceInfo.filterNotNull().first() }?.firmware
        val modelId = if (requireModelIdOfClaim) {
            _modelIdThisClaim.value ?: withTimeoutOrNull(MODEL_ID_WAIT_MS) { _modelIdThisClaim.filterNotNull().first() }
        } else {
            modelIdSeen
        }
        return when (val verdict = SafeModeGate.evaluate(firmware, modelId, requireModelIdOfClaim)) {
            SafeModeGate.Verdict.Allowed -> null
            is SafeModeGate.Verdict.Refused -> {
                BleLogger.logConnectionEvent("Safe Mode: write refused — ${verdict.state.reason}")
                _safeMode.value = verdict.state
                BudsError.UnsupportedFirmware
            }
        }
    }

    private fun refreshObservedSafeMode() {
        val observed = SafeModeGate.observed(_deviceInfo.value?.firmware, modelIdSeen)
        if (observed != null) {
            if (_safeMode.value == null) BleLogger.logConnectionEvent("Safe Mode: ${observed.reason}")
            _safeMode.value = observed
        }
    }

    // ---- battery ---------------------------------------------------------------------------------

    /**
     * DECISIONS.md ADR-068 (`ai-sessions/0098`): one `SubscribeRuntimeInfo` on the session channel (no Message Stream claim) — a second subscription is answered
     * by a stream packet (`PROTOCOL.md` §4.5 Update 2026-09-28, 8 of 8 in `CAP-063`, 19–355 ms). Done when a runtime-info packet arrives within
     * [BATTERY_BURST_WAIT_MS]; none is [BudsError.NoNewBatteryReading] — the values and their times stay as they were, never a suggested new reading.
     */
    override suspend fun refreshBattery(): BudsResult<Unit> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) return BudsResult.Failure(BudsError.ConnectionLost)
        val result = coroutineScope {
            val packet = async(start = CoroutineStart.UNDISPATCHED) { withTimeoutOrNull(BATTERY_BURST_WAIT_MS) { _runtimeInfoPackets.first() } }
            when (val sent = subscribeRuntimeInfo(fromRefresh = true)) {
                is BudsResult.Failure -> {
                    packet.cancel()
                    sent
                }
                is BudsResult.Success -> if (packet.await() != null) BudsResult.Success(Unit) else BudsResult.Failure(BudsError.NoNewBatteryReading)
            }
        }
        _batteryRefreshError.value = (result as? BudsResult.Failure)?.error
        if (result is BudsResult.Failure) BleLogger.logConnectionEvent("Battery refresh: ${result.error::class.simpleName}")
        return result
    }

    /**
     * `SubscribeRuntimeInfo` (DECISIONS.md ADR-043): one request per Connect on the channel the Buds announced, with its ADR-034 address —
     * nothing is sent without an announced, known channel. The Buds then push `SERVER_STREAM` packets by themselves; only their Case entry
     * and each bud's charging state are read ([RoutedFrame.RuntimeInfo]). A failure to send is reported through [caseBatteryError].
     */
    private suspend fun subscribeRuntimeInfo(fromRefresh: Boolean = false): BudsResult<Unit> = eqMutex.withLock {
        // ADR-043 Update 2026-09-26: a Refresh's re-subscription changes nothing visible when it cannot be sent (the Case keeps its value and time).
        val channel = when (val c = awaitMaestroChannel()) {
            is BudsResult.Failure -> {
                if (!fromRefresh) _caseBatteryError.value = c.error
                return@withLock BudsResult.Failure(c.error)
            }
            is BudsResult.Success -> c.value
        }
        val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(Maestro.subscribeRuntimeInfoRequest(channel.channelId)))
        awaitWriteQuarantine()
        when (val sent = transport.send(Dlci.MAESTRO, wire)) {
            is BudsResult.Failure -> {
                if (!fromRefresh) _caseBatteryError.value = sent.error
                sent
            }
            is BudsResult.Success -> {
                BleLogger.logConnectionEvent(
                    (if (fromRefresh) "Runtime info re-requested on Refresh" else "Runtime info requested") + " (channel ${channel.channelId})",
                )
                BudsResult.Success(Unit)
            }
        }
    }

    // ---- on-demand Message Stream channel (DECISIONS.md ADR-032) --------------------------------

    /**
     * Claims the Message Stream channel (DLCI 0x04) for one user action, runs [action], and schedules
     * its release [MESSAGE_STREAM_LINGER_MS] later so Google Play services can take the channel back
     * and hold it stably (ADR-061's hold while the noise-control tab was shown is gone — DECISIONS.md ADR-065, `ai-sessions/0091`: the mode comes from the
     * settings stream on the app's own channel). Serialised by [claimMutex]. Requires an open session (the MAESTRO channel);
     * a channel that is busy is retried inside `transport.openChannel` and, if still busy, reported
     * through [messageStreamError] and the returned failure — never silently dropped.
     *
     * If the channel dies under [action] (another client's failed connect closed it) the claim is
     * retried **once** — the failed attempt has just freed the port.
     */
    internal suspend fun <T> withMessageStream(freshOpen: Boolean = false, action: suspend () -> BudsResult<T>): BudsResult<T> { // (experiment seam)
        if (connectionStateMachine.state.value !is ConnectionState.Ready) {
            return BudsResult.Failure(BudsError.ConnectionLost)
        }
        return claimMutex.withLock {
            releaseJob?.cancel()
            releaseJob = null
            if (freshOpen && transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM)) {
                // `ai-sessions/0052`: release the lingering claim now, so the open below is a new one (the Buds' open-time battery burst).
                BleLogger.logConnectionEvent("Message Stream still claimed from an earlier action: released for a fresh claim")
                transport.closeChannel(Dlci.FAST_PAIR_MESSAGE_STREAM)
                codecRouter.reset(Dlci.FAST_PAIR_MESSAGE_STREAM)
                _modelIdThisClaim.value = null
            }
            var result: BudsResult<T> = BudsResult.Failure(BudsError.ConnectionLost)
            try {
                for (attempt in 1..2) {
                    if (!transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM)) {
                        // A new claim: empty buffer, no Model ID yet, and the reference point for the provisional Settable reading (ADR-049).
                        codecRouter.reset(Dlci.FAST_PAIR_MESSAGE_STREAM)
                        _modelIdThisClaim.value = null
                        messageStreamOpenedAt = clock()
                        val opened = transport.openChannel(Dlci.FAST_PAIR_MESSAGE_STREAM, BudsSdpUuids.FAST_PAIR_MESSAGE_STREAM)
                        if (opened is BudsResult.Failure) {
                            _messageStreamError.value = opened.error
                            return@withLock BudsResult.Failure(opened.error)
                        }
                    }
                    result = action()
                    // Kotlin 2.4 smart-casts `result` here; the explicit cast was reported as USELESS_CAST (`ai-sessions/0078`).
                    val diedUnderUs = result is BudsResult.Failure && result.error is BudsError.ChannelLost
                    if (!diedUnderUs) break
                }
                // A re-check the Buds refused (I-1) is their answer, not a channel problem: the claim itself worked.
                _messageStreamError.value = (result as? BudsResult.Failure)?.error?.takeIf { it != BudsError.AncNotAllowed }
            } finally {
                // Always released — a tap cancelled mid-action must not leave DLCI 0x04 claimed and Play services locked out
                // (0044 APP-4).
                scheduleRelease()
            }
            result
        }
    }

    private fun scheduleRelease() {
        releaseJob = scope.launch {
            delay(MESSAGE_STREAM_LINGER_MS)
            claimMutex.withLock {
                transport.closeChannel(Dlci.FAST_PAIR_MESSAGE_STREAM)
                codecRouter.reset(Dlci.FAST_PAIR_MESSAGE_STREAM)
                _modelIdThisClaim.value = null
            }
        }
    }

    /**
     * Started by [openSession] once the session is `Ready` — at Connect and at every ADR-044 re-open (ADR-034): the EQ read, the settings reads (ADR-036,
     * `ai-sessions/0052`), the one runtime-info subscription (ADR-043), then — DECISIONS.md ADR-064/065, `ai-sessions/0091`, the order the maintainer chose in
     * chat 2026-10-14 (Q6) — the one settings-changes subscription and the one read of setting 13, and last the one hardware-info read (ADR-058) — all wait for
     * the Buds' channel announcement. Each step fails on its own and is never retried (a lost session fails them all; the re-open runs the sequence again).
     * `internal` so a test can start it without a `BluetoothDevice`.
     */
    internal fun launchInitialEqRead() {
        readRound++ // A93-APP-02: a new session's reads (since ADR-068 only its ReadSetting 4:13 — no snapshot `Get`)
        eqReadJob?.cancel()
        eqReadJob = scope.launch {
            readEq()
            readSettings()
            subscribeRuntimeInfo()
            subscribeToSettingsChanges()
            readAncSetting()
            readHardwareInfo()
        }
    }

    /**
     * `SubscribeToSettingsChanges` (DECISIONS.md ADR-064 item 1, ADR-065 item 1): one REQUEST per MAESTRO session, byte-identical to the official app's
     * `CAP-051` 1177 (channel 21) / `CAP-056` 1451 (channel 19), on the announced channel with its ADR-034 address, after [subscribeRuntimeInfo]. Never
     * retried, never polled; a lost session is subscribed again by the re-open's own sequence (its failure is logged; the sequence goes on, as every step).
     * A read, not gated by Safe Mode (ADR-042 item 3). The Buds
     * answer nothing to it directly — the stream's packets reach [handleRoutedFrame] as [RoutedFrame.Setting] with `stream = true`.
     */
    private suspend fun subscribeToSettingsChanges(): BudsResult<Unit> = eqMutex.withLock {
        val channel = when (val c = awaitMaestroChannel()) {
            is BudsResult.Failure -> {
                BleLogger.logConnectionEvent("Settings stream not subscribed: ${eqErrorLogText(c.error)}")
                return@withLock BudsResult.Failure(c.error)
            }
            is BudsResult.Success -> c.value
        }
        val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(Maestro.subscribeToSettingsChangesRequest(channel.channelId)))
        awaitWriteQuarantine()
        when (val sent = transport.send(Dlci.MAESTRO, wire)) {
            is BudsResult.Failure -> {
                BleLogger.logConnectionEvent("Settings stream not subscribed: ${eqErrorLogText(sent.error)}")
                sent
            }
            is BudsResult.Success -> {
                BleLogger.logConnectionEvent("Settings stream subscribed (channel ${channel.channelId})")
                BudsResult.Success(Unit)
            }
        }
    }

    /**
     * `ReadSetting 4:13` (DECISIONS.md ADR-064 item 1, ADR-065 item 1): one read of the noise-control mode per MAESTRO session, right after the subscription —
     * byte-identical to `CAP-051` 1178 / `CAP-056` 1450 — waiting ≤ [SETTING_READ_TIMEOUT_MS] for its answer, never retried. The answer reaches
     * [applyAncState] through [handleRoutedFrame] (a read answer: "changed since the app last saw it" when it differs from the shown mode, ADR-065 item 4);
     * a failure is logged; the settings stream is the other source of the mode (no Connect snapshot claim since ADR-068). Also a Refresh's read (ADR-067).
     */
    private suspend fun readAncSetting(): BudsResult<AncMode?> = eqMutex.withLock {
        val channel = when (val c = awaitMaestroChannel()) {
            is BudsResult.Failure -> {
                BleLogger.logConnectionEvent("Noise-control mode not read: ${eqErrorLogText(c.error)}")
                return@withLock BudsResult.Failure(c.error)
            }
            is BudsResult.Success -> c.value
        }
        val request = Maestro.readSettingRequest(channel.channelId, SettingsCodec.FIELD_ANC_STATE)
            ?: return@withLock BudsResult.Failure(BudsError.SettingNotReadable)
        val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(request))
        val accept: (MaestroReply) -> Boolean = {
            (it is MaestroReply.Setting && !it.stream && it.value.field == SettingsCodec.FIELD_ANC_STATE) ||
                (it is MaestroReply.Result && it.result.methodId == Maestro.METHOD_READ_SETTING)
        }
        awaitWriteQuarantine()
        val (sent, reply) = sendAndAwait(_maestroReplies, SETTING_READ_TIMEOUT_MS, accept) { transport.send(Dlci.MAESTRO, wire) }
        val error = when {
            sent is BudsResult.Failure -> sent.error
            reply == null -> BudsError.Timeout
            reply is MaestroReply.Result && reply.result.isOk -> BudsError.UnreadableAnswer
            reply is MaestroReply.Result -> BudsError.MaestroRejected("${PwRpc.typeName(reply.result.type)} ${PwRpc.statusName(reply.result.status)}")
            else -> null
        }
        if (error != null) {
            BleLogger.logConnectionEvent("Noise-control mode read failed: ${eqErrorLogText(error)}")
            BudsResult.Failure(error)
        } else {
            BleLogger.logConnectionEvent("Noise-control mode read (channel ${channel.channelId})")
            BudsResult.Success(((reply as MaestroReply.Setting).value as SettingValue.AncState).mode)
        }
    }

    /**
     * `GetHardwareInfo` (DECISIONS.md ADR-058): one unary request per Connect on the announced channel with its ADR-034 address — byte-identical to the official
     * app's `CAP-036` 1415 / `CAP-024` 801 — after the runtime-info subscription; waits ≤ [SETTING_READ_TIMEOUT_MS] for the answer, never retried. A read: not
     * gated by Safe Mode (ADR-042 item 3). The answer's serials reach [deviceInfo] through [handleRoutedFrame]; a failure goes to [serialsError]. The log
     * carries the count only — a serial is a device identifier (`AGENTS.md` §9).
     */
    private suspend fun readHardwareInfo(): Unit = eqMutex.withLock {
        val channel = when (val c = awaitMaestroChannel()) {
            is BudsResult.Failure -> {
                _serialsError.value = c.error
                return@withLock
            }
            is BudsResult.Success -> c.value
        }
        val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(Maestro.getHardwareInfoRequest(channel.channelId)))
        val accept: (MaestroReply) -> Boolean = {
            it is MaestroReply.Hardware || (it is MaestroReply.Result && it.result.methodId == Maestro.METHOD_GET_HARDWARE_INFO)
        }
        awaitWriteQuarantine()
        val (sent, reply) = sendAndAwait(_maestroReplies, SETTING_READ_TIMEOUT_MS, accept) { transport.send(Dlci.MAESTRO, wire) }
        val error = when {
            sent is BudsResult.Failure -> sent.error
            reply == null -> BudsError.Timeout
            reply is MaestroReply.Result && reply.result.isOk -> BudsError.UnreadableAnswer // an OK answer without the three strings
            reply is MaestroReply.Result -> BudsError.MaestroRejected("${PwRpc.typeName(reply.result.type)} ${PwRpc.statusName(reply.result.status)}")
            else -> null
        }
        if (error != null) {
            BleLogger.logConnectionEvent("Hardware info read failed: ${eqErrorLogText(error)}")
            _serialsError.value = error
        } else {
            BleLogger.logConnectionEvent("Hardware info read (channel ${channel.channelId}): ${(reply as MaestroReply.Hardware).count} serial numbers")
            _serialsError.value = null
        }
    }

    /**
     * `ReadSetting 4:N` for [SETTING_READ_ORDER] (ADR-036): one pass per Connect, sequential, each waiting ≤ [SETTING_READ_TIMEOUT_MS] for its answer,
     * never retried. A field that is not answered (or answered with an error) stays "not read"; the last reason is kept in [settingsError].
     */
    private suspend fun readSettings(): Unit = eqMutex.withLock {
        val channel = when (val c = awaitMaestroChannel()) {
            is BudsResult.Failure -> {
                _settingsError.value = SettingsFailure(c.error, write = false)
                return@withLock
            }
            is BudsResult.Success -> c.value
        }
        for (field in SETTING_READ_ORDER) {
            val request = Maestro.readSettingRequest(channel.channelId, field) ?: continue
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(request))
            // A58-APP-03 (`ai-sessions/0059`): an OK `ReadSetting` answer whose value the codec rejects arrives as an OK [MaestroReply.Result] — it ends
            // the wait at once as [BudsError.UnreadableAnswer] (the field stays "not read") instead of a 2 s `Timeout` that would say the Buds did not answer.
            val accept: (MaestroReply) -> Boolean = {
                (it is MaestroReply.Setting && !it.stream && it.value.field == field) ||
                    (it is MaestroReply.Result && it.result.methodId == Maestro.METHOD_READ_SETTING)
            }
            awaitWriteQuarantine()
            val (sent, reply) = sendAndAwait(_maestroReplies, SETTING_READ_TIMEOUT_MS, accept) { transport.send(Dlci.MAESTRO, wire) }
            val error = when {
                sent is BudsResult.Failure -> sent.error
                reply == null -> BudsError.Timeout
                reply is MaestroReply.Result && reply.result.isOk -> BudsError.UnreadableAnswer
                reply is MaestroReply.Result -> BudsError.MaestroRejected("${PwRpc.typeName(reply.result.type)} ${PwRpc.statusName(reply.result.status)}")
                else -> null
            }
            if (error != null) {
                BleLogger.logConnectionEvent("Setting read failed (field $field): ${eqErrorLogText(error)}")
                _settingsError.value = SettingsFailure(error, write = false)
                if (sent is BudsResult.Failure) return@withLock // the session is going away: nothing more to read
            }
        }
        BleLogger.logConnectionEvent("Settings read (channel ${channel.channelId})")
    }

    private fun cancelClaimJobs() {
        ringHoldJob?.cancel()
        ringHoldJob = null
        eqReadJob?.cancel()
        eqReadJob = null
        releaseJob?.cancel()
        releaseJob = null
        resubscribeJob?.cancel()
        resubscribeJob = null
    }

    /**
     * Subscribes to [reply] **before** sending, then waits up to [timeoutMs] for one emission that [accept]s.
     * Subscribing first matters: the Buds answer within tens of milliseconds, and a subscription made
     * after `send()` returns can miss a fast reply. A send failure cancels the wait.
     */
    internal suspend fun <R, T> sendAndAwait( // (experiment seam)
        reply: SharedFlow<R>,
        timeoutMs: Long,
        accept: (R) -> Boolean,
        send: suspend () -> BudsResult<T>,
    ): Pair<BudsResult<T>, R?> = coroutineScope {
        val waiter = async(start = CoroutineStart.UNDISPATCHED) { withTimeoutOrNull(timeoutMs) { reply.first(accept) } }
        val result = send()
        if (result is BudsResult.Failure) {
            waiter.cancel()
            result to null
        } else {
            result to waiter.await()
        }
    }

    /** What a request on the claimed Message Stream got (`ai-sessions/0062` F-3): its answer, the claim's close first, nothing in time, or no send. */
    private sealed class ClaimWait<out R> {
        data class Answered<R>(val value: R) : ClaimWait<R>()
        data class CutOff(val detail: String?) : ClaimWait<Nothing>()
        data object TimedOut : ClaimWait<Nothing>()
        data class SendFailed(val error: BudsError) : ClaimWait<Nothing>()
    }

    /**
     * [sendAndAwait] for a request on the claimed Message Stream, which can also end by the claim's **close** (`ai-sessions/0062` F-3): the stack closes
     * this app's port when another client connects to the same channel, and an answer sent after that never reaches the app (`CAP-065` 2649 → 2651,
     * 10344 → 10356). Both waits are subscribed before the send. If the close comes first, the answer still wins when it turns up within
     * [CUT_OFF_GRACE_MS] — it may have arrived just before the close and still be on its way through the codec (one bounded wait, never a loop).
     * A timeout with the channel still open stays [ClaimWait.TimedOut].
     */
    private suspend fun <R> sendAndAwaitOnClaim(
        reply: SharedFlow<R>,
        timeoutMs: Long,
        accept: (R) -> Boolean = { true },
        send: suspend () -> BudsResult<Unit>,
    ): ClaimWait<R> = coroutineScope<ClaimWait<R>> {
        val answer = async(start = CoroutineStart.UNDISPATCHED) { withTimeoutOrNull(timeoutMs) { reply.first(accept) } }
        val closed = async(start = CoroutineStart.UNDISPATCHED) {
            transport.channelClosed.first { it.channelId == Dlci.FAST_PAIR_MESSAGE_STREAM }
        }
        try {
            when (val sent: BudsResult<Unit> = send()) {
                is BudsResult.Failure -> ClaimWait.SendFailed(sent.error)
                is BudsResult.Success -> select<ClaimWait<R>> {
                    answer.onAwait { value -> if (value != null) ClaimWait.Answered(value) else ClaimWait.TimedOut }
                    closed.onAwait { close ->
                        withTimeoutOrNull(CUT_OFF_GRACE_MS) { answer.await() }?.let { ClaimWait.Answered(it) } ?: ClaimWait.CutOff(close.detail)
                    }
                }
            }
        } finally {
            answer.cancel()
            closed.cancel()
        }
    }

    companion object {
        /** Wait for the Buds' ACK/Notify after a Set or Ring before the channel may be released. */
        internal const val ACK_WAIT_MS = 1_000L // (experiment seam)

        /** After the claim's close, how long an answer that arrived just before it may still take through the codec (F-3) — in-process, milliseconds. */
        private const val CUT_OFF_GRACE_MS = 100L

        /** How long a battery Refresh waits for a runtime-info packet after its re-subscription (ADR-068; observed 19–355 ms, `CAP-063` 3863→3870 … 8417→8426). */
        private const val BATTERY_BURST_WAIT_MS = 2_000L

        /**
         * DECISIONS.md ADR-069 item 3 (its Update of 2026-10-18): the longest a Ring's claim is held for the Buds' stop report — 65 s, just above the
         * 62.9 s an untouched ring lasted in `CAP-079` (`CAP-079-FINDINGS.md` §5.2); touched stops came 14.8–17.1 s in (`CAP-078` §4.1).
         */
        internal const val RING_HOLD_MAX_MS = 65_000L

        /** DECISIONS.md ADR-069 item 2: the Seeker's ACK of a ringing status — `ff 01 00 02 04 01` (`CAP-025` 2048, `CAP-078` 7970). */
        private val RING_STATUS_ACK = byteArrayOf(0xff.toByte(), 0x01, 0x00, 0x02, 0x04, 0x01)

        /** pw_rpc status 9 (`FAILED_PRECONDITION`, pigweed `PW_STATUS_FAILED_PRECONDITION`): the Buds' answer to a mode write with no bud worn (ADR-067). */
        private const val PW_STATUS_FAILED_PRECONDITION = 9

        /** How long the channel stays claimed after an action so replies land, then it is released (ADR-065 item 5: no hold — `ai-sessions/0094` dropped ADR-061's tab clause). */
        private const val MESSAGE_STREAM_LINGER_MS = 1_500L

        /**
         * ADR-065 item 4 (`ai-sessions/0091`, ARCHITECTURE.md §3.2): how long after the app's own `Set` a settings-stream `4:{13:n}` of that mode still counts as
         * the app's change. Observed: 269 ms from the Set (`CAP-051` 1527 → 1534, after the ACK and two Notifies) and 55 ms after the second Notify (`CAP-056`
         * 4001 → 4028). A hold on a bud to the mode just set changes nothing, so a wrong "set by this app" inside the window has no visible effect. Not a timer:
         * compared at the packet's arrival.
         */
        internal const val ANC_SET_WINDOW_MS: Long = 2_000L

        /** How long a read/write waits for the Buds' channel announcement (their first Maestro packet). */
        private const val MAESTRO_ANNOUNCE_WAIT_MS = 3_000L

        /** How long a `ReadSetting` waits for its answer. Observed answers: ~50 ms (`CAP-015`/`CAP-036`). */
        private const val EQ_READ_TIMEOUT_MS = 2_000L

        /** The Connect-time settings reads (ADR-036, ADR-046), in the official app's sweep order (ascending field number: `CAP-036` 1445 … 1538, `CAP-024` 837 … 1097). */
        private val SETTING_READ_ORDER = listOf(
            SettingsCodec.FIELD_IN_EAR_DETECTION,
            SettingsCodec.FIELD_TOUCH_CONTROLS,
            SettingsCodec.FIELD_PRESS_AND_HOLD,
            SettingsCodec.FIELD_MULTIPOINT, // ADR-036/053 (`ai-sessions/0074`); between 7 and 12 as in the official sweep (`CAP-024` 973 → 1009 → 1015)
            SettingsCodec.FIELD_ANC_MODE_LIST, // ADR-046; between 7 and 17 as in the official sweep (`CAP-036` 1457 → 1514 → 1526)
            SettingsCodec.FIELD_VOLUME_EQ, // ADR-055 (`ai-sessions/0074`); between 12 and 17 (`CAP-036` 1514 → 1520 → 1526)
            SettingsCodec.FIELD_VOLUME_BALANCE,
            SettingsCodec.FIELD_MONO_AUDIO,
            SettingsCodec.FIELD_CONVERSATION_DETECTION,
            SettingsCodec.FIELD_CASE_SOUND_OTHER_ALERTS, // ADR-054 (`ai-sessions/0074`), `CAP-024` 1089
            SettingsCodec.FIELD_CASE_SOUND_EARBUDS_REPLACED, // ADR-054, `CAP-024` 1093
            SettingsCodec.FIELD_HEAD_GESTURES, // ADR-052 (`ai-sessions/0074`); the last of the official sweep's switches (`CAP-024` 1097)
        )

        /** How long one settings read waits for its answer (ADR-036: ≤ 3 s; observed 14–64 ms in `CAP-036` 1445→1447 … 1538→1540). */
        private const val SETTING_READ_TIMEOUT_MS = 2_000L

        /** How long a `WriteSetting` waits for its empty RESPONSE (`CAP-015`: ~50 ms after the request). */
        internal const val EQ_WRITE_ACK_TIMEOUT_MS = 1_500L // (experiment seam)

        /** How long a Message Stream command waits for the claim's Model ID frame (observed 30–340 ms after the open, `CAP-059`). */
        private const val MODEL_ID_WAIT_MS = 1_000L

        /** Readings of Android's link kept for [classifySessionLoss] — its windows span a few seconds; readings arrive per broadcast/refresh. */
        private const val MAX_LINK_READINGS = 32

        /** A Settable reading received this soon after the Message Stream opened is provisional (DECISIONS.md ADR-024: ~2 s). */
        private const val AVAILABILITY_PROVISIONAL_MS = 2_000L
    }
}

private fun BatteryLevel.stamped(atMillis: Long): BatteryLevel = if (this is BatteryLevel.Known) copy(receivedAtMillis = atMillis) else this

private fun BatteryLevel.chargingReading(atMillis: Long): ChargingReading? =
    (this as? BatteryLevel.Known)?.isCharging?.let { ChargingReading(it, atMillis, ChargingSource.MESSAGE_STREAM) }

/**
 * What the Buds answered to one of our Maestro requests (DECISIONS.md ADR-034) — or pushed: [Value.stream] / [Setting.stream] mark a settings-stream packet,
 * which never ends a read's wait (`ai-sessions/0094`, A93-APP-04 — only the read's RESPONSE does).
 */
internal sealed class MaestroReply { // (experiment seam: was file-private)
    data class Value(val frame: EqFrame, val stream: Boolean = false) : MaestroReply()
    data class Result(val result: RoutedFrame.RpcResult) : MaestroReply()
    data class Setting(val value: SettingValue, val stream: Boolean = false) : MaestroReply()

    /** The `GetHardwareInfo` answer carried serials (ADR-058) — only their [count] travels here; the strings went straight to `deviceInfo`. */
    data class Hardware(val count: Int) : MaestroReply()

    fun isResultFor(methodId: Int): Boolean = this is Result && result.methodId == methodId
}
