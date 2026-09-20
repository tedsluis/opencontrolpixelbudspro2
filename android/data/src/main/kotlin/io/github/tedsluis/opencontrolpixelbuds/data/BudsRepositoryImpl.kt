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
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingFrame
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingFrameEncoder
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RoutedFrame
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsSdpUuids
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsTransport
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine
import kotlinx.coroutines.CoroutineScope
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
import kotlinx.coroutines.withTimeoutOrNull

/**
 * `BudsRepository` implementation (ARCHITECTURE.md §2.1, DECISIONS.md
 * ADR-001), wiring `:hardware`'s [transport]/[connectionState] and `:data`'s
 * own [CodecRouter] to the domain-facing interface. Implements
 * ARCHITECTURE.md §3.1's per-feature state-reconciliation table:
 *
 * - **ANC**: no active query needed — the peer's own connect-time
 *   `Get`/`Notify` pair (DECISIONS.md ADR-021/ADR-022) is simply observed.
 * - **EQ**: [eqProfile] resets to `null` ("unknown, not stale-and-trusted") on every fresh `Ready`
 *   transition and is filled by the Connect sequence's `ReadSetting 4:16` (DECISIONS.md ADR-034); a failed
 *   read or write is reported through [eqError] with its reason instead of being assumed to have worked.
 * - **Battery**: push-based via [hfpBatteryPercent] (HFP Option C) — no
 *   query, only listening.
 * - **Find My Buds**: no persisted state to reconcile.
 *
 * [hfpBatteryPercent] is injected as a plain [Flow] so this class stays
 * unit-testable against a [io.github.tedsluis.opencontrolpixelbuds.hardware.FakeBudsTransport]
 * and scripted flows, per AGENTS.md §11 — no real `BluetoothHeadset` involved
 * in a test of this class. [connectionStateMachine] is the real, already
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
    hfpBatteryPercent: Flow<Int>,
    debugModeEnabled: Flow<Boolean>,
    private val scope: CoroutineScope,
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

    private val _eqProfile = MutableStateFlow<EqBandGains?>(null)
    override val eqProfile: StateFlow<EqBandGains?> = _eqProfile

    private val _batteryStatus = MutableStateFlow(BatteryStatus())
    override val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus

    /** A Ring ACK arrived — what a Find My Buds tap waits for before the channel is released. */
    private val _ringAcks = MutableSharedFlow<Unit>(extraBufferCapacity = 8)

    private val _unidentifiedFrames = MutableSharedFlow<UnidentifiedFrame>(extraBufferCapacity = 32)
    override val unidentifiedFrames: Flow<UnidentifiedFrame> = _unidentifiedFrames

    init {
        scope.launch {
            debugModeEnabled.collect { debugModeEnabledSnapshot = it }
        }
        scope.launch {
            transport.inbound.collect { (channelId, bytes) ->
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
            hfpBatteryPercent.collect { percent ->
                // Charging state is not confirmed by this mechanism (BatteryLevel's own doc
                // comment) — never fabricated as true or false.
                _batteryStatus.update { it.copy(hfpEarbud = BatteryLevel.Known(percent, isCharging = null)) }
            }
        }
        scope.launch {
            connectionState.collect { state ->
                if (state is ConnectionState.Ready) {
                    // ARCHITECTURE.md §3.1: EQ has no confirmed read opcode, so a fresh
                    // connection's cached value is unknown, not trusted, until a real frame
                    // arrives — never keep showing a stale pre-reconnect quintet.
                    _eqProfile.value = null
                }
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
                    _lastConnectionError.value = BudsError.ChannelLost(loss.channelId, loss.detail)
                    cancelClaimJobs()
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
                is AncFrame.Notify -> anc.currentMode?.let {
                    _ancMode.tryEmit(it)
                    _ancModeFresh.tryEmit(it)
                }
                is AncFrame.Set -> _ancMode.tryEmit(anc.mode)
                is AncFrame.Get -> Unit
                // AncFrameDecoder accepts *every* Message Stream ACK (Group 0xFF), so the Buds' ACK of a
                // Find My Buds Ring arrives here, not as RoutedFrame.Ring — tell them apart by the
                // Group/Code the ACK echoes (PROTOCOL.md §2.1; real frame `ff 01 00 03 04 01 00`).
                is AncFrame.Ack -> if (anc.echoedGroup == RingMessageStream.GROUP && anc.echoedCode == RingMessageStream.CODE_RING) {
                    _ringAcks.tryEmit(Unit)
                }
            }

            // Field 16 is the *active* EQ (ADR-034); field 18 (the last saved custom curve) must never replace it.
            is RoutedFrame.Eq -> {
                if (!frame.frame.persist) {
                    _eqProfile.value = frame.frame.gains
                    _eqError.value = null
                }
                _maestroReplies.tryEmit(MaestroReply.Value(frame.frame))
            }

            is RoutedFrame.MaestroHello -> {
                BleLogger.logConnectionEvent("Maestro channel announced by the Buds: ${frame.channelId}")
                _maestroChannelId.value = frame.channelId
            }

            is RoutedFrame.RpcResult -> _maestroReplies.tryEmit(MaestroReply.Result(frame))

            // No persisted state (ARCHITECTURE.md §3.1's table); an ACK only releases a waiting tap.
            is RoutedFrame.Ring -> if (frame.frame is RingFrame.Ack) _ringAcks.tryEmit(Unit)

            // ADR-033. A byte the decoder does not interpret arrives as Unavailable and *replaces* any
            // earlier Known value: a stale percentage must never linger once the Buds report a
            // regime we cannot read (AGENTS.md §5). The Case and the HFP field are untouched.
            is RoutedFrame.Battery -> _batteryStatus.update {
                it.copy(left = frame.frame.left, right = frame.frame.right, case = frame.frame.case)
            }
        }
    }

    override suspend fun connect(): BudsResult<Unit> = connectMutex.withLock {
        // Already connected (e.g. a stale second tap): nothing to do — re-running connect() would tear
        // the live connection down (RfcommBudsTransport.connect() replaces any current one).
        if (connectionStateMachine.state.value is ConnectionState.Ready) return@withLock BudsResult.Success(Unit)
        val device = bondedDeviceProvider()
            ?: return@withLock BudsResult.Failure(BudsError.PermissionDenied)
        _lastConnectionError.value = null
        _eqError.value = null
        _maestroChannelId.value = null // announced afresh by this connection's first Buds packet (ADR-034)
        connectionStateMachine.onConnectRequested()
        // ADR-032: the session is the MAESTRO channel only. The Message Stream channel (DLCI 0x04) is shared
        // with Google Play services' Fast Pair, so it is claimed on demand, never held by Connect.
        val channels = mapOf(Dlci.MAESTRO to BudsSdpUuids.MAESTRO)
        when (val result = transport.connect(device, channels)) {
            is BudsResult.Success -> {
                connectionStateMachine.onLinkEstablished()
                // PROTOCOL.md §5.2's own "Discovering" step is HYPOTHESIS-level, not a confirmed
                // sequence this app can act on — SDP resolution already happened implicitly inside
                // createRfcommSocketToServiceRecord() above, so there is nothing further to
                // discover before the sockets are actually usable.
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
    }

    override suspend fun disconnect(): BudsResult<Unit> {
        _lastConnectionError.value = null
        _messageStreamError.value = null
        _eqError.value = null
        cancelClaimJobs()
        transport.disconnect()
        connectionStateMachine.onDisconnected()
        return BudsResult.Success(Unit)
    }

    override suspend fun setAncMode(mode: AncMode): BudsResult<Unit> = withMessageStream {
        val (result, notified) = sendAndAwait(_ancModeFresh, ACK_WAIT_MS) {
            transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, AncFrameEncoder.encode(AncFrame.Set(mode)))
        }
        // Optimistic update (ARCHITECTURE.md §3.1), applied only when the Buds did NOT answer in time:
        // if their Notify arrived it already set the real mode via handleRoutedFrame, and re-applying
        // the requested mode here would overwrite it (e.g. after a NAK). A failed send is never applied.
        if (result is BudsResult.Success && notified == null) _ancMode.tryEmit(mode)
        result
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
            val payload = EqFrameEncoder.encode(EqFrame(clamped, persist = false, channelId = channel.channelId))
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, payload)
            val (sent, reply) = sendAndAwait(_maestroReplies, EQ_WRITE_ACK_TIMEOUT_MS, { it.isResultFor(Maestro.METHOD_WRITE_SETTING) }) {
                transport.send(Dlci.MAESTRO, wire)
            }
            when {
                sent is BudsResult.Failure -> eqFailed(sent.error)
                reply == null -> eqFailed(BudsError.Timeout)
                reply is MaestroReply.Result && reply.result.isOk -> {
                    _eqProfile.value = clamped
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
                _eqProfile.value = reply.frame.gains
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
        sendAndAwait(_ringAcks, ACK_WAIT_MS) {
            transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, RingFrameEncoder.encode(RingFrame.Start(target)))
        }.first
    }

    override suspend fun stopRinging(): BudsResult<Unit> = withMessageStream {
        sendAndAwait(_ringAcks, ACK_WAIT_MS) {
            transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, RingFrameEncoder.encode(RingFrame.Stop))
        }.first
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
            for (attempt in 1..2) {
                if (!transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM)) {
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
            scheduleRelease()
            result
        }
    }

    private fun scheduleRelease() {
        releaseJob = scope.launch {
            delay(MESSAGE_STREAM_LINGER_MS)
            claimMutex.withLock { transport.closeChannel(Dlci.FAST_PAIR_MESSAGE_STREAM) }
        }
    }

    private fun launchInitialSnapshot() {
        snapshotJob?.cancel()
        snapshotJob = scope.launch { refreshAncMode(SNAPSHOT_TIMEOUT_MS) }
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

        /** How long a read/write waits for the Buds' channel announcement (their first Maestro packet). */
        private const val MAESTRO_ANNOUNCE_WAIT_MS = 3_000L

        /** How long a `ReadSetting` waits for its answer. Observed answers: ~50 ms (`CAP-015`/`CAP-036`). */
        private const val EQ_READ_TIMEOUT_MS = 2_000L

        /** How long a `WriteSetting` waits for its empty RESPONSE (`CAP-015`: ~50 ms after the request). */
        private const val EQ_WRITE_ACK_TIMEOUT_MS = 1_500L
    }
}

/** What the Buds answered to one of our Maestro requests (DECISIONS.md ADR-034). */
private sealed class MaestroReply {
    data class Value(val frame: EqFrame) : MaestroReply()
    data class Result(val result: RoutedFrame.RpcResult) : MaestroReply()

    fun isResultFor(methodId: Int): Boolean = this is Result && result.methodId == methodId
}
