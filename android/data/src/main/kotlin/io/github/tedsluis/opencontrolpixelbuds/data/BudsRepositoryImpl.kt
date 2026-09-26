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
import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncFrameEncoder
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
import io.github.tedsluis.opencontrolpixelbuds.data.codec.AncMessageStream
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
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.LinkReading
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
import io.github.tedsluis.opencontrolpixelbuds.domain.classifySessionLoss
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingSource
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
 * - **ANC**: each Message Stream claim sends this app's own `Get` (a client that sends nothing gets no `Notify`, ARCHITECTURE.md
 *   §3.1); a `Set` counts as done only on the Buds' ACK or `Notify` — a NAK or no answer keeps the previous mode (0044 APP-3).
 * - **EQ**: [eqProfile] is reset to `null` ("unknown, not stale-and-trusted") inside [connect], before the session is
 *   `Ready`, and filled by the Connect sequence's `ReadSetting 4:16` (DECISIONS.md ADR-034); a failed
 *   read or write is reported through [eqError] with its reason instead of being assumed to have worked.
 * - **Battery**: push-based — Left/Right from the Message Stream's `Group 0x03 Code 0x03` (ADR-033, each claim yields
 *   a reading), the Case from the `SubscribeRuntimeInfo` stream on the session channel, requested once per Connect (ADR-043;
 *   DLCI 0x08 is no longer opened). HFP `AT+BIEV` is not consumed (ADR-040).
 * - **Safe Mode** (ARCHITECTURE.md §8.1, ADR-042): every write/control command passes [SafeModeGate] first.
 * - **Find My Buds**: no persisted state to reconcile.
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
    private val transport: BudsTransport,
    private val connectionStateMachine: ConnectionStateMachine,
    private val bondedDeviceProvider: () -> BluetoothDevice?,
    debugModeEnabled: Flow<Boolean>,
    private val scope: CoroutineScope,
    /** Wall clock for the provisional dock-state rule (ADR-024), the loss cause (I-7) and the re-open rules (ADR-044); a test passes its virtual clock. */
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

    /** When the current [lastConnectionError]'s loss happened (`null` = no loss to explain). */
    @Volatile
    private var lossAtMillis: Long? = null

    private val _messageStreamError = MutableStateFlow<BudsError?>(null)
    override val messageStreamError: Flow<BudsError?> = _messageStreamError

    private val _eqError = MutableStateFlow<BudsError?>(null)
    override val eqError: Flow<BudsError?> = _eqError

    /** The pw_rpc channel the Buds announced for this connection (their unsolicited `GetSoftwareInfo`, ADR-034). */
    private val _maestroChannelId = MutableStateFlow<Int?>(null)

    /** Answers to our Maestro requests (an EQ value, or an empty/error result) — what a read/write waits for. */
    private val _maestroReplies = MutableSharedFlow<MaestroReply>(extraBufferCapacity = 16)

    /** One EQ read or write at a time, so a reply can only belong to the request that is waiting for it. */
    private val eqMutex = Mutex()

    @Volatile
    private var eqReadJob: Job? = null

    /**
     * Serialises every use of the shared Message Stream channel (DLCI 0x04) — claim, action, release
     * — so a release can never close the channel under a command that is still waiting for its reply
     * (DECISIONS.md ADR-032).
     */
    private val claimMutex = Mutex()

    @Volatile
    private var releaseJob: Job? = null

    @Volatile
    private var snapshotJob: Job? = null

    @Volatile
    private var resubscribeJob: Job? = null

    /** One [connect] at a time — a double tap must not start two overlapping socket-opening runs. */
    private val connectMutex = Mutex()

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

    private val _ancMode = MutableSharedFlow<AncMode>(replay = 1)
    override val ancMode: Flow<AncMode> = _ancMode

    /** replay=0 counterpart of [_ancMode], used only so [refreshAncMode] waits for a
     * genuinely fresh response rather than immediately observing [_ancMode]'s replay cache. */
    private val _ancModeFresh = MutableSharedFlow<AncMode>(extraBufferCapacity = 8)

    private val _ancModeUpdatedAt = MutableStateFlow<Long?>(null)
    override val ancModeUpdatedAt: StateFlow<Long?> = _ancModeUpdatedAt

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

    private val _settingsError = MutableStateFlow<BudsError?>(null)
    override val settingsError: Flow<BudsError?> = _settingsError

    private val _batteryRefreshError = MutableStateFlow<BudsError?>(null)
    override val batteryRefreshError: Flow<BudsError?> = _batteryRefreshError

    /** Every "Battery updated" frame as it arrives (replay 0) — what a Refresh waits for to know a new reading came (`ai-sessions/0052`). */
    private val _batteryFrames = MutableSharedFlow<Unit>(extraBufferCapacity = 8)

    private val _ancAvailability = MutableStateFlow(AncAvailability.UNKNOWN)
    override val ancAvailability: Flow<AncAvailability> = _ancAvailability

    private val _ancAvailabilityUpdatedAt = MutableStateFlow<Long?>(null)
    override val ancAvailabilityUpdatedAt: StateFlow<Long?> = _ancAvailabilityUpdatedAt

    private val _ancAvailabilityProvisional = MutableStateFlow(false)
    override val ancAvailabilityProvisional: StateFlow<Boolean> = _ancAvailabilityProvisional

    /** When the current Message Stream claim opened (the ADR-024 "provisional within ~2 s" reference point). */
    @Volatile
    private var messageStreamOpenedAt: Long = Long.MIN_VALUE

    private val _safeMode = MutableStateFlow<SafeModeState?>(null)
    override val safeMode: StateFlow<SafeModeState?> = _safeMode

    /** The Model ID the Buds sent on the *current* Message Stream claim (`null` until it arrives; reset on every new open). */
    private val _modelIdThisClaim = MutableStateFlow<String?>(null)

    /** The last Model ID seen on this connection (for DLCI 0x02 writes, which have no Message Stream claim of their own). */
    @Volatile
    private var modelIdSeen: String? = null

    /** Every ACK/NAK on the Message Stream (its own routed type, 0044 APP-3) — what a command waits for. */
    private val _replies = MutableSharedFlow<MessageStreamReply>(extraBufferCapacity = 16)

    /** What an ANC `Set` waits for: the Buds' ACK/NAK for `08 12`, or any `Notify` with a known mode. */
    private sealed class AncOutcome {
        data object Acked : AncOutcome()
        data class Rejected(val reason: Int) : AncOutcome()
        data class Notified(val mode: AncMode) : AncOutcome()
    }

    private val _ancOutcomes = MutableSharedFlow<AncOutcome>(extraBufferCapacity = 16)

    /** Sibling helpers so every mutation of the value above also stamps its own [System.currentTimeMillis]
     * timestamp, in one place (`ai-sessions/0043` Phase H) — never a polling timer, only recorded when a
     * value is actually received. */
    private fun emitAncMode(mode: AncMode) {
        _ancMode.tryEmit(mode)
        _ancModeUpdatedAt.value = System.currentTimeMillis()
    }

    private fun updateEqProfile(gains: EqBandGains?) {
        _eqProfile.value = gains
        _eqProfileUpdatedAt.value = if (gains == null) null else System.currentTimeMillis()
    }

    private fun updateBatteryStatus(transform: (BatteryStatus) -> BatteryStatus) {
        _batteryStatus.update(transform)
        _batteryStatusUpdatedAt.value = System.currentTimeMillis()
    }

    private fun updateAncAvailability(availability: AncAvailability) {
        _ancAvailability.value = availability
        _ancAvailabilityUpdatedAt.value = System.currentTimeMillis()
        // ADR-024 (2026-09-18 consequence): a reading within ~2 s of the channel opening may be stale; a later Notify in the same claim
        // replaces it (the last value wins while the claim lingers).
        _ancAvailabilityProvisional.value = clock() - messageStreamOpenedAt < AVAILABILITY_PROVISIONAL_MS
    }

    override fun onAndroidLink(link: AndroidLink) {
        synchronized(linkReadings) {
            linkReadings.addLast(LinkReading(link, clock()))
            while (linkReadings.size > MAX_LINK_READINGS) linkReadings.removeFirst()
        }
        reclassifyLoss()
        reopener.onAndroidLink(link)
    }

    override fun onAppVisible(visible: Boolean) = reopener.onVisible(visible)

    /** Stores one reported setting value with its time; a press-and-hold packet only touches the bud(s) it names. */
    private fun applySetting(value: SettingValue, atMillis: Long, changedByApp: Boolean = false) {
        _settings.update { s ->
            when (value) {
                is SettingValue.Flag -> {
                    val r = SettingReading(value.on, atMillis, changedByApp)
                    when (value.field) {
                        SettingsCodec.FIELD_IN_EAR_DETECTION -> s.copy(inEarDetection = r)
                        SettingsCodec.FIELD_TOUCH_CONTROLS -> s.copy(touchControls = r)
                        SettingsCodec.FIELD_MONO_AUDIO -> s.copy(monoAudio = r)
                        SettingsCodec.FIELD_CONVERSATION_DETECTION -> s.copy(conversationDetection = r)
                        else -> s
                    }
                }
                is SettingValue.Balance -> s.copy(volumeBalance = SettingReading(value.value, atMillis, changedByApp))
                is SettingValue.PressAndHold -> s.copy(
                    holdLeft = value.left?.let { SettingReading(it, atMillis, changedByApp) } ?: s.holdLeft,
                    holdRight = value.right?.let { SettingReading(it, atMillis, changedByApp) } ?: s.holdRight,
                )
            }
        }
    }

    /** ADR-044: the same Connect sequence as a tap, but it leaves the re-open policy as it is; a failure outside the state machine is shown. */
    private suspend fun reopenSession(): BudsResult<Unit> {
        val result = openSession()
        if (result is BudsResult.Failure && connectionStateMachine.state.value !is ConnectionState.Failed) {
            _lastConnectionError.value = result.error
        }
        return result
    }

    /** I-7: the cause of the last loss from the readings around it; logged once per change (always-on, no address, AGENTS.md §9). */
    private fun reclassifyLoss() {
        val lossAt = lossAtMillis
        val cause = if (lossAt == null) null else classifySessionLoss(lossAt, synchronized(linkReadings) { linkReadings.toList() })
        if (_lastLossCause.value != cause) {
            _lastLossCause.value = cause
            if (cause != null) BleLogger.logConnectionEvent(SessionDiagnostics.lossCauseLine(cause))
        }
    }

    private fun clearLoss() {
        lossAtMillis = null
        _lastLossCause.value = null
    }

    /**
     * I-6 (`ai-sessions/0048`): the session a ring was started in has ended. The ring is kept — only an ACKed Stop ends it — but the app can no
     * longer know whether it has stopped by itself, so after a reconnect the screen says it *may* still be ringing.
     */
    private fun markRingFromEarlierSession() {
        _ringing.update { it?.copy(fromEarlierSession = true) }
    }

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    override val deviceInfo: Flow<DeviceInfo?> = _deviceInfo

    private val _ringing = MutableStateFlow<RingNotice?>(null)
    override val ringing: Flow<RingNotice?> = _ringing

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
                lastInboundAtMillis = System.currentTimeMillis()
                BleLogger.logHexDump(channelId, bytes, debugModeEnabledSnapshot)
                val routed = codecRouter.feed(
                    channelId,
                    bytes,
                    timestampMillis = System.currentTimeMillis(),
                    onUnidentified = { _unidentifiedFrames.tryEmit(it) },
                    onMalformed = { BleLogger.logMalformedFrame(channelId, debugModeEnabledSnapshot) },
                    // Always-on, payload-free (AGENTS.md §9): shows whether the Buds accept or reject a request.
                    onRpcPacket = { BleLogger.logConnectionEvent(it.summary()) },
                )
                routed.forEach(::handleRoutedFrame)
            }
        }
        scope.launch {
            // An on-demand channel died on its own (another client took it back, ADR-032): drop any partial frame buffered for it,
            // so the next claim cannot start misaligned (0044 finding APP-2).
            transport.channelClosed.collect { closed ->
                codecRouter.reset(closed.channelId)
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
                            if (seen == null) null else System.currentTimeMillis() - lastInboundAtMillis,
                        ),
                    )
                    _lastConnectionError.value = BudsError.ChannelLost(loss.channelId, loss.detail)
                    lossAtMillis = clock()
                    reclassifyLoss()
                    markRingFromEarlierSession()
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
                    // I-3 (`ai-sessions/0048`, ADR-024 Update 2026-09-25): Settable 0x00 = the Buds refuse a Set (NAK 0x02); non-zero = allowed.
                    updateAncAvailability(AncAvailability.fromSettableToggles(anc.settableToggles))
                    anc.currentMode?.let {
                        emitAncMode(it)
                        _ancModeFresh.tryEmit(it)
                        _ancOutcomes.tryEmit(AncOutcome.Notified(it))
                    }
                }
                is AncFrame.Set -> emitAncMode(anc.mode)
                is AncFrame.Get -> Unit
            }

            // ACK/NAK for any Message Stream command (0044 APP-3): the command that waits picks its own by the echoed Group/Code.
            is RoutedFrame.Reply -> {
                _replies.tryEmit(frame.reply)
                if (frame.reply.answers(AncMessageStream.GROUP, AncMessageStream.CODE_SET)) {
                    _ancOutcomes.tryEmit(
                        when (val r = frame.reply) {
                            is MessageStreamReply.Ack -> AncOutcome.Acked
                            is MessageStreamReply.Nak -> AncOutcome.Rejected(r.reason)
                        },
                    )
                }
            }

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
                _maestroReplies.tryEmit(MaestroReply.Value(frame.frame))
            }

            is RoutedFrame.MaestroHello -> {
                BleLogger.logConnectionEvent("Maestro channel announced by the Buds: ${frame.channelId}")
                _maestroChannelId.value = frame.channelId
                if (frame.firmware.isNotEmpty()) {
                    _deviceInfo.value = DeviceInfo(frame.firmware)
                    refreshObservedSafeMode()
                }
            }

            is RoutedFrame.RpcResult -> _maestroReplies.tryEmit(MaestroReply.Result(frame))

            // ADR-036/045 (`ai-sessions/0052`): a setting the Buds reported — a read answer (or a settings-change push). Applied with its receive time.
            is RoutedFrame.Setting -> {
                applySetting(frame.value, clock())
                _maestroReplies.tryEmit(MaestroReply.Setting(frame.value))
            }

            // No persisted state (ARCHITECTURE.md §3.1's table); the Buds do not send Ring frames themselves (their ACK is a Reply).
            is RoutedFrame.Ring -> Unit

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
                _batteryFrames.tryEmit(Unit)
            }

            // ADR-035's DLCI 0x08 decode stays in the codec, but the app no longer opens that channel (ADR-043) — nothing arrives here.
            is RoutedFrame.CaseBattery -> Unit

            // ADR-043 and its 2026-09-25 Update (`ai-sessions/0048` I-4/I-5/I-8): the Case, and each bud's charging state, from the runtime-info
            // stream. A packet without the Case entry (no bud charging) keeps the last Case value as "last seen" with its own time — a dated
            // last-seen value, never a fabricated or silently carried-over one (AGENTS.md §5).
            is RoutedFrame.RuntimeInfo -> {
                val now = clock()
                val info = frame.info
                updateBatteryStatus {
                    it.copy(
                        case = when (val case = info.case) {
                            null -> (it.case as? BatteryLevel.Known)?.copy(isStale = true) ?: it.case
                            is BatteryLevel.Known -> case.copy(receivedAtMillis = now)
                            BatteryLevel.Unavailable -> BatteryLevel.Unavailable
                        },
                        leftCharging = info.leftCharging?.let { c -> ChargingReading(c, now, ChargingSource.RUNTIME_INFO) } ?: it.leftCharging,
                        rightCharging = info.rightCharging?.let { c -> ChargingReading(c, now, ChargingSource.RUNTIME_INFO) } ?: it.rightCharging,
                    )
                }
                _caseBatteryError.value = null
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
        // 0044 APP-8: no bonded device is "not paired", not a missing permission (AGENTS.md §8 — a specific message per cause).
        val device = bondedDeviceProvider()
            ?: return@withLock BudsResult.Failure(BudsError.NotPaired)
        _lastConnectionError.value = null
        clearLoss()
        _eqError.value = null
        _maestroChannelId.value = null // announced afresh by this connection's first Buds packet (ADR-034)
        _deviceInfo.value = null
        _caseBatteryError.value = null
        _batteryRefreshError.value = null
        _settingsError.value = null
        _ancAvailability.value = AncAvailability.UNKNOWN // re-read by this connection's snapshot claim (I-3: unknown ⇒ enabled)
        // I-5: a Case value from an earlier session is shown as "last seen" (with its time) until this session's stream reports it again;
        // kept only in memory, never persisted (ARCHITECTURE.md §3.1).
        _batteryStatus.update { it.copy(case = (it.case as? BatteryLevel.Known)?.copy(isStale = true) ?: it.case) }
        _safeMode.value = null
        _modelIdThisClaim.value = null
        modelIdSeen = null
        lastInboundChannel = null
        codecRouter.resetAll() // a new connection starts with empty per-channel buffers (0044 APP-2)
        connectionStateMachine.onConnectRequested()
        // ADR-032: the session is the MAESTRO channel only. The Message Stream channel (DLCI 0x04) is shared
        // with Google Play services' Fast Pair, so it is claimed on demand, never held by Connect.
        val channels = mapOf(Dlci.MAESTRO to BudsSdpUuids.MAESTRO)
        try {
            when (val result = transport.connect(device, channels)) {
                is BudsResult.Success -> {
                    connectionStateMachine.onLinkEstablished()
                    // `Discovering` is a zero-length pass-through (ARCHITECTURE.md §2.1): SDP resolution already happened inside
                    // createRfcommSocketToServiceRecord(); `Ready` means "the MAESTRO channel is open" (ADR-032).
                    connectionStateMachine.onReady()
                    // ADR-032 (agent detail): one short claim, started by this Connect tap, so the ANC mode
                    // and the battery the Buds push on DLCI 0x04 are known without a further tap.
                    launchInitialSnapshot()
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
        reopener.onUserDisconnect()
        BleLogger.logConnectionEvent(SessionDiagnostics.userDisconnectLine(connectionStateMachine.state.value::class.simpleName ?: "?"))
        _lastConnectionError.value = null
        clearLoss()
        _messageStreamError.value = null
        _eqError.value = null
        _caseBatteryError.value = null
        _batteryRefreshError.value = null
        _settingsError.value = null
        _deviceInfo.value = null
        // Not the ring notice (I-6): the ring keeps sounding after Disconnect until Stop is sent (`CAP-062` frame 9772, heard until 06:53:50).
        markRingFromEarlierSession()
        _safeMode.value = null
        cancelClaimJobs()
        transport.disconnect()
        codecRouter.resetAll()
        connectionStateMachine.onDisconnected()
        return BudsResult.Success(Unit)
    }

    /**
     * ANC `Set` (ADR-009). Counts as done only when the Buds answer (0044 APP-3; ARCHITECTURE.md §3.1 "provisional until the
     * acknowledgement confirms it"): their ACK applies the requested mode, their `Notify` has already applied the real one; a NAK is
     * [BudsError.CommandRejected] and no answer is [BudsError.Timeout] — in both cases the previous mode stays.
     */
    override suspend fun setAncMode(mode: AncMode): BudsResult<Unit> {
        // I-3: the Buds said no mode is switchable now — they would NAK the Set (reason 0x02, CAP-062 10/10). Send nothing, claim nothing.
        if (_ancAvailability.value == AncAvailability.NOT_ALLOWED) {
            BleLogger.logConnectionEvent("ANC Set not sent: the Buds' last Notify reported no switchable mode (Settable 0x00)")
            return BudsResult.Failure(BudsError.AncNotAllowed)
        }
        return sendAncSet(mode)
    }

    private suspend fun sendAncSet(mode: AncMode): BudsResult<Unit> = withMessageStream {
        writeGate(requireModelIdOfClaim = true)?.let { return@withMessageStream BudsResult.Failure(it) }
        val (result, outcome) = sendAndAwait(_ancOutcomes, ACK_WAIT_MS) {
            transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, AncFrameEncoder.encode(AncFrame.Set(mode)))
        }
        when {
            result is BudsResult.Failure -> result
            outcome == null -> BudsResult.Failure(BudsError.Timeout)
            outcome is AncOutcome.Rejected -> BudsResult.Failure(rejected(outcome.reason))
            outcome is AncOutcome.Acked -> {
                emitAncMode(mode)
                BudsResult.Success(Unit)
            }
            else -> BudsResult.Success(Unit) // Notified: handleRoutedFrame already applied the Buds' own mode.
        }
    }

    private fun rejected(reason: Int): BudsError {
        val name = MessageStreamAck.reasonName(reason)
        BleLogger.logConnectionEvent("Message Stream command rejected by the Buds: NAK $name")
        return BudsError.CommandRejected(reason, name)
    }

    override suspend fun refreshAncMode(): BudsResult<AncMode> = refreshAncMode(GET_RESPONSE_TIMEOUT_MS)

    private suspend fun refreshAncMode(timeoutMs: Long, freshOpen: Boolean = false): BudsResult<AncMode> = withMessageStream(freshOpen) {
        val (sendResult, mode) = sendAndAwait(_ancModeFresh, timeoutMs) {
            transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, AncFrameEncoder.encode(AncFrame.Get))
        }
        when {
            sendResult is BudsResult.Failure -> BudsResult.Failure(sendResult.error)
            mode != null -> BudsResult.Success(mode)
            else -> BudsResult.Failure(BudsError.Timeout)
        }
    }

    override suspend fun setEqGains(gains: EqBandGains): BudsResult<Unit> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) return BudsResult.Failure(BudsError.ConnectionLost)
        val clamped = gains.clamped()
        return eqMutex.withLock {
            val channel = when (val c = awaitMaestroChannel()) {
                is BudsResult.Failure -> return@withLock eqFailed(c.error)
                is BudsResult.Success -> c.value
            }
            writeGate(requireModelIdOfClaim = false)?.let { return@withLock eqFailed(it) }
            val payload = EqFrameEncoder.encode(EqFrame(clamped, persist = false, channelId = channel.channelId))
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, payload)
            val (sent, reply) = sendAndAwait(_maestroReplies, EQ_WRITE_ACK_TIMEOUT_MS, { it.isResultFor(Maestro.METHOD_WRITE_SETTING) }) {
                transport.send(Dlci.MAESTRO, wire)
            }
            when {
                sent is BudsResult.Failure -> eqFailed(sent.error)
                reply == null -> eqFailed(BudsError.Timeout)
                reply is MaestroReply.Result && reply.result.isOk -> {
                    updateEqProfile(clamped)
                    _eqError.value = null
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
     * `ReadSetting 4:16` (DECISIONS.md ADR-034): waits for the Buds' channel announcement, sends the request on that
     * channel, and waits for the value — or for an error result. Nothing is retried; a failure is reported with its reason.
     * // TODO(verify): a fresh client may have to send other requests first (PROTOCOL.md §2.2a) — hardware re-test.
     */
    private suspend fun readEq(): BudsResult<EqBandGains> = eqMutex.withLock {
        val channel = when (val c = awaitMaestroChannel()) {
            is BudsResult.Failure -> return@withLock eqFailed(c.error)
            is BudsResult.Success -> c.value
        }
        val request = Maestro.readSettingRequest(channel.channelId, Maestro.FIELD_EQ_ACTIVE)
            ?: return@withLock eqFailed(BudsError.Unknown(IllegalStateException("EQ field not readable")))
        val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(request))
        val accept: (MaestroReply) -> Boolean = {
            (it is MaestroReply.Value && !it.frame.persist) ||
                (it is MaestroReply.Result && it.result.methodId == Maestro.METHOD_READ_SETTING && !it.result.isOk)
        }
        val (sent, reply) = sendAndAwait(_maestroReplies, EQ_READ_TIMEOUT_MS, accept) { transport.send(Dlci.MAESTRO, wire) }
        when {
            sent is BudsResult.Failure -> eqFailed(sent.error)
            reply == null -> eqFailed(BudsError.Timeout)
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
    private suspend fun awaitMaestroChannel(): BudsResult<MaestroChannel> {
        val id = _maestroChannelId.value
            ?: withTimeoutOrNull(MAESTRO_ANNOUNCE_WAIT_MS) { _maestroChannelId.filterNotNull().first() }
            ?: return BudsResult.Failure(BudsError.MaestroChannelUnknown(null))
        return MaestroChannel.forChannel(id)?.let { BudsResult.Success(it) }
            ?: BudsResult.Failure(BudsError.MaestroChannelUnknown(id))
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
        else -> error::class.simpleName ?: "error"
    }

    override suspend fun applyEqPreset(preset: EqPreset): BudsResult<Unit> = setEqGains(preset.gains)

    // ---- settings writes (DECISIONS.md ADR-045) --------------------------------------------------------------------------------

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

    private suspend fun writeFlag(field: Int, on: Boolean): BudsResult<Unit> =
        writeSetting(SettingValue.Flag(field, on)) { SettingsCodec.flagRequest(it, field, on) }

    /**
     * One `WriteSetting` (ADR-045) on the announced channel with its ADR-034 address, after the Safe-Mode gate (ADR-042). The value is applied
     * only on the Buds' empty `RESPONSE` with status OK — never optimistically; an error status, no answer within [EQ_WRITE_ACK_TIMEOUT_MS] or a
     * refused gate keeps the previous value and puts the reason in [settingsError]. Serialised with every other Maestro request ([eqMutex]).
     */
    private suspend fun writeSetting(value: SettingValue, request: (channelId: Int) -> RpcPacket?): BudsResult<Unit> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) return settingFailed(BudsError.ConnectionLost)
        return eqMutex.withLock {
            val channel = when (val c = awaitMaestroChannel()) {
                is BudsResult.Failure -> return@withLock settingFailed(c.error)
                is BudsResult.Success -> c.value
            }
            writeGate(requireModelIdOfClaim = false)?.let { return@withLock settingFailed(it) }
            val packet = request(channel.channelId)
                ?: return@withLock settingFailed(BudsError.Unknown(IllegalStateException("setting ${value.field} is not writable")))
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(packet))
            val (sent, reply) = sendAndAwait(_maestroReplies, EQ_WRITE_ACK_TIMEOUT_MS, { it.isResultFor(Maestro.METHOD_WRITE_SETTING) }) {
                transport.send(Dlci.MAESTRO, wire)
            }
            when {
                sent is BudsResult.Failure -> settingFailed(sent.error)
                reply == null -> settingFailed(BudsError.Timeout)
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
        _settingsError.value = error
        return BudsResult.Failure(error)
    }

    override suspend fun ringBud(target: RingTarget): BudsResult<Unit> = withMessageStream {
        val result = sendRing(RingFrame.Start(target))
        // The ring keeps sounding after the channel is released (`ai-sessions/0042`, heard on the recording), so this state stays
        // until Stop succeeds — a failed, refused or unanswered ring is never claimed.
        if (result is BudsResult.Success) _ringing.value = RingNotice(target)
        result
    }

    override suspend fun stopRinging(): BudsResult<Unit> = withMessageStream {
        val result = sendRing(RingFrame.Stop)
        if (result is BudsResult.Success) _ringing.value = null
        result
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
    private suspend fun writeGate(requireModelIdOfClaim: Boolean): BudsError? {
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
     * Left/Right: the Buds push their battery burst (ADR-033) only when the Message Stream channel **opens** (120 of 120 app opens,
     * `ai-sessions/0051` §9) — so a Refresh always uses a fresh claim: a channel still lingering from an earlier action is released and opened
     * again (two ordinary ADR-032 claim operations, no new message type; `ai-sessions/0052`). If no `03 03` frame arrives on that claim the
     * values and their times stay as they were and [batteryRefreshError] says so ([BudsError.NoNewBatteryReading]) — never a suggested new
     * reading. Each value keeps its own receive time.
     */
    override suspend fun refreshBattery(): BudsResult<Unit> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) return BudsResult.Failure(BudsError.ConnectionLost)
        // DECISIONS.md ADR-043 Update 2026-09-26: one SubscribeRuntimeInfo per Refresh (the Connect-time bytes), no wait, no retry — an answer, if
        // the Buds give one, arrives on the stream like any other packet; none changes nothing. Queued behind any running settings request.
        resubscribeJob = scope.launch { subscribeRuntimeInfo(fromRefresh = true) }
        val result = coroutineScope {
            // Subscribed before the claim opens: the burst arrives tens to hundreds of ms after the open, before the Notify (CAP-062 7106/7109 → 7110).
            val burst = async(start = CoroutineStart.UNDISPATCHED) { withTimeoutOrNull(BATTERY_BURST_WAIT_MS) { _batteryFrames.first() } }
            when (val claim = refreshAncMode(GET_RESPONSE_TIMEOUT_MS, freshOpen = true)) {
                is BudsResult.Failure -> {
                    burst.cancel()
                    // A burst can still have arrived on the claim (e.g. the Notify timed out): that is a new reading, but the claim's error is shown.
                    BudsResult.Failure(claim.error)
                }
                is BudsResult.Success -> if (burst.await() != null) BudsResult.Success(Unit) else BudsResult.Failure(BudsError.NoNewBatteryReading)
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
     * and hold it stably. Serialised by [claimMutex]. Requires an open session (the MAESTRO channel);
     * a channel that is busy is retried inside `transport.openChannel` and, if still busy, reported
     * through [messageStreamError] and the returned failure — never silently dropped.
     *
     * If the channel dies under [action] (another client's failed connect closed it) the claim is
     * retried **once** — the failed attempt has just freed the port.
     */
    private suspend fun <T> withMessageStream(freshOpen: Boolean = false, action: suspend () -> BudsResult<T>): BudsResult<T> {
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
                        // A new claim: empty buffer, no Model ID yet, and the reference point for the provisional dock state.
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
                    val diedUnderUs = result is BudsResult.Failure &&
                        (result as BudsResult.Failure).error is BudsError.ChannelLost
                    if (!diedUnderUs) break
                }
                _messageStreamError.value = (result as? BudsResult.Failure)?.error
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

    private fun launchInitialSnapshot() {
        snapshotJob?.cancel()
        snapshotJob = scope.launch {
            refreshAncMode(SNAPSHOT_TIMEOUT_MS)
        }
    }

    /**
     * Started by [connect] once the session is `Ready` (ADR-034): the EQ read, the settings reads (ADR-036, `ai-sessions/0052`), then the one
     * runtime-info subscription (ADR-043) — all wait for the Buds' channel announcement. `internal` so a test can start it without a `BluetoothDevice`.
     */
    internal fun launchInitialEqRead() {
        eqReadJob?.cancel()
        eqReadJob = scope.launch {
            readEq()
            readSettings()
            subscribeRuntimeInfo()
        }
    }

    /**
     * `ReadSetting 4:N` for [SETTING_READ_ORDER] (ADR-036): one pass per Connect, sequential, each waiting ≤ [SETTING_READ_TIMEOUT_MS] for its answer,
     * never retried. A field that is not answered (or answered with an error) stays "not read"; the last reason is kept in [settingsError].
     */
    private suspend fun readSettings(): Unit = eqMutex.withLock {
        val channel = when (val c = awaitMaestroChannel()) {
            is BudsResult.Failure -> {
                _settingsError.value = c.error
                return@withLock
            }
            is BudsResult.Success -> c.value
        }
        for (field in SETTING_READ_ORDER) {
            val request = Maestro.readSettingRequest(channel.channelId, field) ?: continue
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(request))
            val accept: (MaestroReply) -> Boolean = {
                (it is MaestroReply.Setting && it.value.field == field) ||
                    (it is MaestroReply.Result && it.result.methodId == Maestro.METHOD_READ_SETTING && !it.result.isOk)
            }
            val (sent, reply) = sendAndAwait(_maestroReplies, SETTING_READ_TIMEOUT_MS, accept) { transport.send(Dlci.MAESTRO, wire) }
            val error = when {
                sent is BudsResult.Failure -> sent.error
                reply == null -> BudsError.Timeout
                reply is MaestroReply.Result -> BudsError.MaestroRejected("${PwRpc.typeName(reply.result.type)} ${PwRpc.statusName(reply.result.status)}")
                else -> null
            }
            if (error != null) {
                BleLogger.logConnectionEvent("Setting read failed (field $field): ${eqErrorLogText(error)}")
                _settingsError.value = error
                if (sent is BudsResult.Failure) return@withLock // the session is going away: nothing more to read
            }
        }
        BleLogger.logConnectionEvent("Settings read (channel ${channel.channelId})")
    }

    private fun cancelClaimJobs() {
        eqReadJob?.cancel()
        eqReadJob = null
        releaseJob?.cancel()
        releaseJob = null
        snapshotJob?.cancel()
        snapshotJob = null
        resubscribeJob?.cancel()
        resubscribeJob = null
    }

    /**
     * Subscribes to [reply] **before** sending, then waits up to [timeoutMs] for one emission.
     * Subscribing first matters: the Buds answer within tens of milliseconds, and a subscription made
     * after `send()` returns can miss a fast reply. A send failure cancels the wait.
     */
    private suspend fun <R, T> sendAndAwait(
        reply: SharedFlow<R>,
        timeoutMs: Long,
        send: suspend () -> BudsResult<T>,
    ): Pair<BudsResult<T>, R?> = sendAndAwait(reply, timeoutMs, { true }, send)

    private suspend fun <R, T> sendAndAwait(
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

    companion object {
        /** Wait for a fresh ANC Notify after a manual Refresh. Kept short: the channel is held meanwhile,
         * and Google Play services re-opens it 2.7–5.0 s after losing it (`ai-sessions/0040` §2.1). */
        private const val GET_RESPONSE_TIMEOUT_MS = 2_000L

        /** Wait for the Buds' ACK/Notify after a Set or Ring before the channel may be released. */
        private const val ACK_WAIT_MS = 1_000L

        /** How long a Refresh waits for the Buds' battery burst on its fresh claim (observed 0.3 s after the open, `CAP-062` 7088 → 7106). */
        private const val BATTERY_BURST_WAIT_MS = 2_000L

        /** Wait for the ANC state during the Connect-time snapshot. */
        private const val SNAPSHOT_TIMEOUT_MS = 1_000L

        /** How long the channel stays claimed after an action so replies land, then it is released. */
        private const val MESSAGE_STREAM_LINGER_MS = 1_500L

        /** How long a read/write waits for the Buds' channel announcement (their first Maestro packet). */
        private const val MAESTRO_ANNOUNCE_WAIT_MS = 3_000L

        /** How long a `ReadSetting` waits for its answer. Observed answers: ~50 ms (`CAP-015`/`CAP-036`). */
        private const val EQ_READ_TIMEOUT_MS = 2_000L

        /** The Connect-time settings reads (ADR-036), in the official app's sweep order (`CAP-036` 1445 … 1538). */
        private val SETTING_READ_ORDER = listOf(
            SettingsCodec.FIELD_IN_EAR_DETECTION,
            SettingsCodec.FIELD_TOUCH_CONTROLS,
            SettingsCodec.FIELD_PRESS_AND_HOLD,
            SettingsCodec.FIELD_VOLUME_BALANCE,
            SettingsCodec.FIELD_MONO_AUDIO,
            SettingsCodec.FIELD_CONVERSATION_DETECTION,
        )

        /** How long one settings read waits for its answer (ADR-036: ≤ 3 s; observed 14–64 ms in `CAP-036` 1445→1447 … 1538→1540). */
        private const val SETTING_READ_TIMEOUT_MS = 2_000L

        /** How long a `WriteSetting` waits for its empty RESPONSE (`CAP-015`: ~50 ms after the request). */
        private const val EQ_WRITE_ACK_TIMEOUT_MS = 1_500L

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

/** What the Buds answered to one of our Maestro requests (DECISIONS.md ADR-034). */
private sealed class MaestroReply {
    data class Value(val frame: EqFrame) : MaestroReply()
    data class Result(val result: RoutedFrame.RpcResult) : MaestroReply()
    data class Setting(val value: SettingValue) : MaestroReply()

    fun isResultFor(methodId: Int): Boolean = this is Result && result.methodId == methodId
}
