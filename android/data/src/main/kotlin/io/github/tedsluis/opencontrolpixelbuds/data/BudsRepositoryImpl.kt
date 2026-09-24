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
import io.github.tedsluis.opencontrolpixelbuds.data.codec.GsndMessageStream
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
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RoutedFrame
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.DockState
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
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
 *   a reading), the Case from an on-demand claim of DLCI 0x08 that sends the one `0e 04 00 00` request (ADR-035/039).
 *   HFP `AT+BIEV` is not consumed (ADR-040).
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
    /** Wall clock for the provisional dock-state rule (ADR-024); a test passes its virtual clock. */
    private val clock: () -> Long = System::currentTimeMillis,
) : BudsRepository {

    override val connectionState: Flow<ConnectionState> = connectionStateMachine.state

    private val _lastConnectionError = MutableStateFlow<BudsError?>(null)
    override val lastConnectionError: Flow<BudsError?> = _lastConnectionError

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
    private var caseReleaseJob: Job? = null

    @Volatile
    private var snapshotJob: Job? = null

    /** One [connect] at a time — a double tap must not start two overlapping socket-opening runs. */
    private val connectMutex = Mutex()

    private val codecRouter = CodecRouter()

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

    /** A Case reading arrived on DLCI 0x08 — what an on-demand Case claim waits for (ADR-035). */
    private val _casePushes = MutableSharedFlow<BatteryLevel>(extraBufferCapacity = 8)

    private val _dockState = MutableStateFlow(DockState.UNKNOWN)
    override val dockState: Flow<DockState> = _dockState

    private val _dockStateUpdatedAt = MutableStateFlow<Long?>(null)
    override val dockStateUpdatedAt: StateFlow<Long?> = _dockStateUpdatedAt

    private val _dockStateProvisional = MutableStateFlow(false)
    override val dockStateProvisional: StateFlow<Boolean> = _dockStateProvisional

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

    private fun updateDockState(state: DockState) {
        _dockState.value = state
        _dockStateUpdatedAt.value = System.currentTimeMillis()
        // ADR-024 (2026-09-18 consequence, implemented 2026-09-24): a reading within ~2 s of the channel opening may be stale; a
        // later Notify in the same claim replaces it (the last value wins while the claim lingers).
        _dockStateProvisional.value = clock() - messageStreamOpenedAt < DOCK_PROVISIONAL_MS
    }

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    override val deviceInfo: Flow<DeviceInfo?> = _deviceInfo

    private val _ringingTarget = MutableStateFlow<RingTarget?>(null)
    override val ringingTarget: Flow<RingTarget?> = _ringingTarget

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
                    // ADR-024: the Settable-toggles byte is the dock state (0x00 both seated, 0xe8 otherwise).
                    updateDockState(DockState.fromSettableToggles(anc.settableToggles))
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

            // No persisted state (ARCHITECTURE.md §3.1's table); the Buds do not send Ring frames themselves (their ACK is a Reply).
            is RoutedFrame.Ring -> Unit

            // ADR-033. A byte the decoder does not interpret arrives as Unavailable and *replaces* any
            // earlier Known value: a stale percentage must never linger once the Buds report a
            // regime we cannot read (AGENTS.md §5). The Case and the HFP field are untouched.
            is RoutedFrame.Battery -> updateBatteryStatus {
                // Not `case`: b3 is 0xff on every claim (ADR-033, 60/60 in ai-sessions/0042) and must not overwrite the Case
                // value read from DLCI 0x08 (ADR-035).
                it.copy(left = frame.frame.left, right = frame.frame.right)
            }

            // ADR-035: only the Case, only from DLCI 0x08; a value the Buds did not mark fresh is kept as "last seen".
            is RoutedFrame.CaseBattery -> {
                updateBatteryStatus { it.copy(case = frame.frame.case) }
                _caseBatteryError.value = null
                _casePushes.tryEmit(frame.frame.case)
            }
        }
    }

    override suspend fun connect(): BudsResult<Unit> = connectMutex.withLock {
        // Already connected (e.g. a stale second tap): nothing to do — re-running connect() would tear
        // the live connection down (RfcommBudsTransport.connect() replaces any current one).
        if (connectionStateMachine.state.value is ConnectionState.Ready) return@withLock BudsResult.Success(Unit)
        // ARCHITECTURE.md §3.1: a fresh connection's EQ is unknown until read — reset here, before anything can reach Ready, so it
        // can never race the Connect-time read (0044 APP-7: an asynchronous Ready collector on Dispatchers.Default used to do this).
        updateEqProfile(null)
        // 0044 APP-8: no bonded device is "not paired", not a missing permission (AGENTS.md §8 — a specific message per cause).
        val device = bondedDeviceProvider()
            ?: return@withLock BudsResult.Failure(BudsError.NotPaired)
        _lastConnectionError.value = null
        _eqError.value = null
        _maestroChannelId.value = null // announced afresh by this connection's first Buds packet (ADR-034)
        _deviceInfo.value = null
        _caseBatteryError.value = null
        _ringingTarget.value = null // a new connection cannot know whether an earlier ring is still sounding
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
        BleLogger.logConnectionEvent(SessionDiagnostics.userDisconnectLine(connectionStateMachine.state.value::class.simpleName ?: "?"))
        _lastConnectionError.value = null
        _messageStreamError.value = null
        _eqError.value = null
        _caseBatteryError.value = null
        _deviceInfo.value = null
        _ringingTarget.value = null
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
    override suspend fun setAncMode(mode: AncMode): BudsResult<Unit> = withMessageStream {
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

    private suspend fun refreshAncMode(timeoutMs: Long): BudsResult<AncMode> = withMessageStream {
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

    override suspend fun ringBud(target: RingTarget): BudsResult<Unit> = withMessageStream {
        val result = sendRing(RingFrame.Start(target))
        // The ring keeps sounding after the channel is released (`ai-sessions/0042`, heard on the recording), so this state stays
        // until Stop succeeds — a failed, refused or unanswered ring is never claimed.
        if (result is BudsResult.Success) _ringingTarget.value = target
        result
    }

    override suspend fun stopRinging(): BudsResult<Unit> = withMessageStream {
        val result = sendRing(RingFrame.Stop)
        if (result is BudsResult.Success) _ringingTarget.value = null
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

    // ---- on-demand Case-battery channel (DECISIONS.md ADR-035) ----------------------------------

    override suspend fun refreshBattery(): BudsResult<Unit> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) return BudsResult.Failure(BudsError.ConnectionLost)
        val leftRight = refreshAncMode(GET_RESPONSE_TIMEOUT_MS) // a Message Stream claim yields the Left/Right battery burst (ADR-033)
        val case = readCaseBattery()
        return when {
            leftRight is BudsResult.Failure -> BudsResult.Failure(leftRight.error)
            else -> case
        }
    }

    /**
     * Claims DLCI 0x08 for one user action, sends the one `0e 04 00 00` request (DECISIONS.md ADR-039), waits for the Buds' `0e 01`
     * push and releases the channel [CASE_LINGER_MS] later (ADR-035). Serialised with the Message Stream claims by [claimMutex]; a
     * failure is reported through [caseBatteryError], the session stays `Ready`.
     *
     * Why the request: every post-open push in `CAP-059`/`CAP-060` answered Play services' `0e 04`, and none of the app's 8
     * receive-only claims got a push (`CAP-060-FINDINGS.md` §2). Contention is real too: **if the channel closes out from under the
     * wait** (another claimant bounced ours) the claim is retried **once** (ADR-038). The Message Stream's release linger is ended
     * first, so the app never holds both shared channels at once (ADR-039 item 3, 0044 APP-9).
     */
    private suspend fun readCaseBattery(): BudsResult<Unit> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) return BudsResult.Failure(BudsError.ConnectionLost)
        return claimMutex.withLock {
            caseReleaseJob?.cancel()
            caseReleaseJob = null
            releaseMessageStreamNow()
            var result: BudsResult<Unit> = BudsResult.Failure(BudsError.Timeout)
            try {
                for (attempt in 1..2) {
                    result = coroutineScope {
                        val waiter = async(start = CoroutineStart.UNDISPATCHED) { withTimeoutOrNull(CASE_PUSH_WAIT_MS) { _casePushes.first() } }
                        if (!transport.isChannelOpen(Dlci.GSND_CONTROL)) {
                            codecRouter.reset(Dlci.GSND_CONTROL)
                            val opened = transport.openChannel(Dlci.GSND_CONTROL, BudsSdpUuids.GSND_CONTROL)
                            if (opened is BudsResult.Failure) {
                                waiter.cancel()
                                return@coroutineScope BudsResult.Failure(opened.error)
                            }
                        }
                        val sent = transport.send(Dlci.GSND_CONTROL, GsndMessageStream.batteryRequest())
                        if (sent is BudsResult.Failure) {
                            waiter.cancel()
                            return@coroutineScope BudsResult.Failure(
                                (sent.error as? BudsError.ChannelLost) ?: BudsError.ChannelLost(Dlci.GSND_CONTROL, "request not sent"),
                            )
                        }
                        if (waiter.await() != null) {
                            return@coroutineScope BudsResult.Success(Unit)
                        }
                        if (!transport.isChannelOpen(Dlci.GSND_CONTROL)) {
                            BudsResult.Failure(BudsError.ChannelLost(Dlci.GSND_CONTROL, "closed while waiting for the Case push"))
                        } else {
                            BudsResult.Failure(BudsError.Timeout)
                        }
                    }
                    val diedUnderUs = result is BudsResult.Failure && (result as BudsResult.Failure).error is BudsError.ChannelLost
                    if (!diedUnderUs) break
                }
                _caseBatteryError.value = (result as? BudsResult.Failure)?.error
                (result as? BudsResult.Failure)?.let {
                    BleLogger.logConnectionEvent("Case battery not read: ${it.error::class.simpleName}")
                }
            } finally {
                // Always released — also when the tap's coroutine is cancelled mid-wait (0044 APP-4).
                caseReleaseJob = scope.launch {
                    delay(CASE_LINGER_MS)
                    claimMutex.withLock { transport.closeChannel(Dlci.GSND_CONTROL) }
                }
            }
            result
        }
    }

    /** Ends a lingering Message Stream claim immediately (caller holds [claimMutex]). */
    private suspend fun releaseMessageStreamNow() {
        releaseJob?.cancel()
        releaseJob = null
        if (transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM)) {
            transport.closeChannel(Dlci.FAST_PAIR_MESSAGE_STREAM)
            codecRouter.reset(Dlci.FAST_PAIR_MESSAGE_STREAM)
            _modelIdThisClaim.value = null
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
    private suspend fun <T> withMessageStream(action: suspend () -> BudsResult<T>): BudsResult<T> {
        if (connectionStateMachine.state.value !is ConnectionState.Ready) {
            return BudsResult.Failure(BudsError.ConnectionLost)
        }
        return claimMutex.withLock {
            releaseJob?.cancel()
            releaseJob = null
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
            readCaseBattery() // ADR-035: the Connect tap also reads the Case, once, after the Message Stream claim
        }
    }

    /** Started by [connect] once the session is `Ready` (ADR-034); `internal` so a test can start it without a `BluetoothDevice`. */
    internal fun launchInitialEqRead() {
        eqReadJob?.cancel()
        eqReadJob = scope.launch { readEq() }
    }

    private fun cancelClaimJobs() {
        eqReadJob?.cancel()
        eqReadJob = null
        releaseJob?.cancel()
        releaseJob = null
        snapshotJob?.cancel()
        snapshotJob = null
        caseReleaseJob?.cancel()
        caseReleaseJob = null
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

        /** Wait for the ANC state during the Connect-time snapshot. */
        private const val SNAPSHOT_TIMEOUT_MS = 1_000L

        /** How long the channel stays claimed after an action so replies land, then it is released. */
        private const val MESSAGE_STREAM_LINGER_MS = 1_500L

        /** Wait for the Buds' Case push after DLCI 0x08 opened (observed: 165 ms in `CAP-059`), ADR-035. */
        private const val CASE_PUSH_WAIT_MS = 2_000L

        /** How long DLCI 0x08 stays open after its push before it is released so Play services can take it back (ADR-035). */
        private const val CASE_LINGER_MS = 1_000L

        /** How long a read/write waits for the Buds' channel announcement (their first Maestro packet). */
        private const val MAESTRO_ANNOUNCE_WAIT_MS = 3_000L

        /** How long a `ReadSetting` waits for its answer. Observed answers: ~50 ms (`CAP-015`/`CAP-036`). */
        private const val EQ_READ_TIMEOUT_MS = 2_000L

        /** How long a `WriteSetting` waits for its empty RESPONSE (`CAP-015`: ~50 ms after the request). */
        private const val EQ_WRITE_ACK_TIMEOUT_MS = 1_500L

        /** How long a Message Stream command waits for the claim's Model ID frame (observed 30–340 ms after the open, `CAP-059`). */
        private const val MODEL_ID_WAIT_MS = 1_000L

        /** A dock reading received this soon after the Message Stream opened is provisional (DECISIONS.md ADR-024: ~2 s). */
        private const val DOCK_PROVISIONAL_MS = 2_000L
    }
}

/** What the Buds answered to one of our Maestro requests (DECISIONS.md ADR-034). */
private sealed class MaestroReply {
    data class Value(val frame: EqFrame) : MaestroReply()
    data class Result(val result: RoutedFrame.RpcResult) : MaestroReply()

    fun isResultFor(methodId: Int): Boolean = this is Result && result.methodId == methodId
}
