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
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingFrame
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
 * - **EQ**: no confirmed read opcode exists, so [eqProfile] resets to `null`
 *   ("unknown, not stale-and-trusted") on every fresh `Ready` transition,
 *   populated only once an actual DLCI 0x02 EQ frame is observed.
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
                is AncFrame.Get, is AncFrame.Ack -> Unit
            }

            is RoutedFrame.Eq -> _eqProfile.value = frame.frame.gains

            is RoutedFrame.Ring -> Unit // No persisted state (ARCHITECTURE.md §3.1's table).
        }
    }

    override suspend fun connect(): BudsResult<Unit> = connectMutex.withLock {
        // Already connected (e.g. a stale second tap): nothing to do — re-running connect() would tear
        // the live connection down (RfcommBudsTransport.connect() replaces any current one).
        if (connectionStateMachine.state.value is ConnectionState.Ready) return@withLock BudsResult.Success(Unit)
        val device = bondedDeviceProvider()
            ?: return@withLock BudsResult.Failure(BudsError.PermissionDenied)
        _lastConnectionError.value = null
        connectionStateMachine.onConnectRequested()
        val channels = mapOf(
            Dlci.MAESTRO to BudsSdpUuids.MAESTRO,
            Dlci.FAST_PAIR_MESSAGE_STREAM to BudsSdpUuids.FAST_PAIR_MESSAGE_STREAM,
        )
        when (val result = transport.connect(device, channels)) {
            is BudsResult.Success -> {
                connectionStateMachine.onLinkEstablished()
                // PROTOCOL.md §5.2's own "Discovering" step is HYPOTHESIS-level, not a confirmed
                // sequence this app can act on — SDP resolution already happened implicitly inside
                // createRfcommSocketToServiceRecord() above, so there is nothing further to
                // discover before the sockets are actually usable.
                connectionStateMachine.onReady()
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
        transport.disconnect()
        connectionStateMachine.onDisconnected()
        return BudsResult.Success(Unit)
    }

    override suspend fun setAncMode(mode: AncMode): BudsResult<Unit> {
        val result = transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, AncFrameEncoder.encode(AncFrame.Set(mode)))
        // Optimistic update (ARCHITECTURE.md §3.1) — provisional until the peer's own
        // Notify confirms it via handleRoutedFrame above; a failed send is not applied.
        if (result is BudsResult.Success) _ancMode.tryEmit(mode)
        return result
    }

    override suspend fun refreshAncMode(): BudsResult<AncMode> {
        val sendResult = transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, AncFrameEncoder.encode(AncFrame.Get))
        if (sendResult is BudsResult.Failure) return BudsResult.Failure(sendResult.error)
        val mode = withTimeoutOrNull(GET_RESPONSE_TIMEOUT_MS) { _ancModeFresh.first() }
        return mode?.let { BudsResult.Success(it) } ?: BudsResult.Failure(BudsError.Timeout)
    }

    override suspend fun setEqGains(gains: EqBandGains): BudsResult<Unit> {
        val clamped = gains.clamped()
        val payload = EqFrameEncoder.encode(EqFrame(clamped, persist = false))
        val frame = Hdlc.encode(address = EQ_HDLC_ADDRESS, control = EQ_HDLC_CONTROL, payload = payload)
        val result = transport.send(Dlci.MAESTRO, frame)
        if (result is BudsResult.Success) _eqProfile.value = clamped
        return result
    }

    override suspend fun applyEqPreset(preset: EqPreset): BudsResult<Unit> = setEqGains(preset.gains)

    override suspend fun ringBud(target: RingTarget): BudsResult<Unit> =
        transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, RingFrameEncoder.encode(RingFrame.Start(target)))

    override suspend fun stopRinging(): BudsResult<Unit> =
        transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, RingFrameEncoder.encode(RingFrame.Stop))

    companion object {
        private const val GET_RESPONSE_TIMEOUT_MS = 5_000L

        // TODO(verify): PROTOCOL.md §2.2a — DLCI 0x02's HDLC Address field is
        // per-connection-negotiated, not a small fixed set; every capture to date
        // observed `0x0000` for the phone's own "Sent" EQ writes specifically
        // (CAP-015 frames 2111/2165/2227), which is what this default reflects, but
        // this project has no evidence for what a *different* session's negotiated
        // address would be, or whether the peer accepts an address the app didn't
        // itself negotiate via the (undocumented) pw_rpc channel-open exchange.
        private const val EQ_HDLC_ADDRESS = 0x0000

        // Control byte observed on every CAP-015 EQ "Sent" frame (2111/2165/2227) —
        // PROJECT_RULES.md §8 rule 22's hardcoded-wire-literal exception.
        private const val EQ_HDLC_CONTROL = 0x3b
    }
}
