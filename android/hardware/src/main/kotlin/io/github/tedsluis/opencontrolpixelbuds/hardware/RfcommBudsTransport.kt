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
package io.github.tedsluis.opencontrolpixelbuds.hardware

import android.bluetooth.BluetoothDevice
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Real `BluetoothSocket`-backed [BudsTransport], one socket per RFCOMM DLCI
 * (PROTOCOL.md §2.3 — each channel has independent framing and cannot share
 * one socket). Every socket call site is wrapped and converted to a
 * [BudsError] (AGENTS.md §3/§8), all I/O runs on [ioDispatcher].
 *
 * [device] is a per-call [connect] parameter, not a constructor argument —
 * the target device is only known once pairing has actually happened
 * (`BudsCompanionPairing.bondedDevice()`), which can be well after this
 * transport singleton itself is constructed by Hilt.
 *
 * Each socket's `InputStream` is read on its own coroutine and forwarded to
 * the shared [inbound] flow as raw, not-necessarily-frame-aligned chunks
 * (`BudsTransport.inbound`'s own doc comment) — `:data`'s `CodecRouter`
 * does the actual frame-boundary detection.
 *
 * ## Connection-lifecycle rules (`ai-sessions/0039`, evidence in its RESULT file §3)
 *
 * The Android Bluetooth stack allows **one** open RFCOMM connection per (device, channel) across
 * *all* apps, and a second client's `connect()` to a channel that is already open fails with
 * "already at opened state" — and, worse, the stack's failure path then closes the **incumbent**
 * port too. On the maintainer's phone, Google Play Services' Fast Pair event stream (channel 2,
 * DLCI 0x04) and this app's own zombie sockets were the incumbents. Hence:
 *
 * 1. **A connection is one unit.** Any channel's loss (read failure, EOF, write failure) closes
 *    *all* of that connection's sockets before [connectionLost] emits — a surviving socket would
 *    still be "open" in the stack and make the very next [connect] fail against ourselves.
 * 2. **At most one [connectionLost] per connection**, and none from a connection already replaced
 *    by a newer [connect] or ended by [disconnect] (stale-event suppression).
 * 3. **A failed [connect] closes the socket that failed**, not only the ones that succeeded.
 * 4. **Bounded retry inside one user-initiated [connect]**: a *fast* failure (collision failures
 *    return in well under a second) is retried up to [connectAttempts] times, [retryDelayMs]
 *    apart — because the colliding attempt itself frees the port. Slow failures (device
 *    unreachable) are never retried, so an out-of-range Buds does not stall for minutes. This is
 *    not a background retry loop (ARCHITECTURE.md §6): it ends with the one tap that started it.
 * 5. **The underlying exception is never discarded** — class + address-redacted message go into
 *    [BudsError.ChannelUnavailable]/[BudsError.ChannelLost] and the always-on log.
 */
class RfcommBudsTransport(
    private val socketFactory: (channelId: Int, uuid: UUID, device: BluetoothDevice) -> RfcommSocket,
    private val connectAttempts: Int = DEFAULT_CONNECT_ATTEMPTS,
    private val retryDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
    private val fastFailureThresholdMs: Long = DEFAULT_FAST_FAILURE_THRESHOLD_MS,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BudsTransport {

    /**
     * One successfully opened set of channel sockets plus the coroutines reading them. [sockets] are
     * the **session** channels opened by [connect] (loss of any = loss of the connection);
     * [auxSockets] are **on-demand** channels opened later by [openChannel] (loss = that channel
     * only, ADR-032). [open] is how this connection opens further channels.
     */
    private inner class Connection(
        val sockets: Map<Int, RfcommSocket>,
        val open: (channelId: Int, uuid: UUID) -> RfcommSocket,
    ) {
        val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
        val lossReported = AtomicBoolean(false)
        val auxSockets = ConcurrentHashMap<Int, RfcommSocket>()
        val auxJobs = ConcurrentHashMap<Int, Job>()
        private val closed = AtomicBoolean(false)

        val isOpen: Boolean get() = !closed.get()

        /** Cancels the readers, then closes every socket (session and on-demand). Idempotent. */
        fun closeAll() {
            if (!closed.compareAndSet(false, true)) return
            scope.cancel()
            sockets.values.forEach(::closeQuietly)
            auxSockets.values.forEach(::closeQuietly)
            auxSockets.clear()
        }
    }

    private val lock = Any()

    @Volatile
    private var current: Connection? = null

    override val connected: Boolean
        get() = current?.isOpen == true

    private val _inbound = MutableSharedFlow<Pair<Int, ByteArray>>(extraBufferCapacity = 256)
    override val inbound: SharedFlow<Pair<Int, ByteArray>> = _inbound

    // extraBufferCapacity, not replay — a loss must reach the collector once, never be replayed to
    // a later subscriber as if it described a newer connection.
    private val _connectionLost = MutableSharedFlow<ConnectionLoss>(extraBufferCapacity = 4)
    override val connectionLost: SharedFlow<ConnectionLoss> = _connectionLost

    private val _channelClosed = MutableSharedFlow<ChannelClosed>(extraBufferCapacity = 8)
    override val channelClosed: SharedFlow<ChannelClosed> = _channelClosed

    /**
     * Opens one socket per entry in [channels] (channelId -> SDP UUID, see
     * [BudsSdpUuids]) against [device] and starts a reader coroutine for
     * each. If any channel fails to connect, every already-opened socket in
     * this call is closed before returning failure — this app has no use for
     * a partially-usable transport where, say, ANC works but EQ silently
     * doesn't. (Per-channel tolerance was evaluated in `ai-sessions/0039` and
     * deliberately not adopted: `ConnectionState` has no "degraded" state, and a
     * `Ready` that silently lacks a channel would be the same lie this rule prevents.)
     */
    override suspend fun connect(device: BluetoothDevice, channels: Map<Int, UUID>): BudsResult<Unit> =
        connectWith(channels) { channelId, uuid -> socketFactory(channelId, uuid, device) }

    /** [connect]'s real body, with the per-channel socket source injected so a unit test can
     * drive it with scripted fakes (`BluetoothDevice` itself cannot be constructed in a JVM test). */
    internal suspend fun connectWith(
        channels: Map<Int, UUID>,
        open: (channelId: Int, uuid: UUID) -> RfcommSocket,
    ): BudsResult<Unit> = withContext(ioDispatcher) {
        // A previous connection (possibly a zombie whose peer already dropped it) must be gone
        // before a new one is attempted, or its still-open sockets collide with the new ones.
        replaceCurrent(null)?.closeAll()

        val opened = LinkedHashMap<Int, RfcommSocket>()
        try {
            for ((channelId, uuid) in channels) {
                when (val result = openSocketWithRetry(channelId, uuid, open)) {
                    is ChannelOpen.Opened -> opened[channelId] = result.socket
                    is ChannelOpen.Failed -> {
                        opened.values.forEach(::closeQuietly)
                        return@withContext BudsResult.Failure(result.error)
                    }
                }
            }
        } catch (e: CancellationException) {
            opened.values.forEach(::closeQuietly)
            throw e
        }

        val connection = Connection(opened, open)
        replaceCurrent(connection)
        opened.forEach { (channelId, socket) -> startReader(connection, channelId, socket, onDemand = false) }
        BudsResult.Success(Unit)
    }

    private sealed interface ChannelOpen {
        class Opened(val socket: RfcommSocket) : ChannelOpen
        class Failed(val error: BudsError) : ChannelOpen
    }

    private suspend fun openSocketWithRetry(
        channelId: Int,
        uuid: UUID,
        open: (Int, UUID) -> RfcommSocket,
    ): ChannelOpen {
        var lastDetail: String? = null
        for (attempt in 1..connectAttempts) {
            val startedNanos = System.nanoTime()
            var socket: RfcommSocket? = null
            try {
                socket = open(channelId, uuid)
                socket.connect()
                BleLogger.logConnectionEvent("RFCOMM channel 0x%02x connected (attempt %d/%d)".format(channelId, attempt, connectAttempts))
                return ChannelOpen.Opened(socket)
            } catch (e: SecurityException) {
                // BLUETOOTH_CONNECT is runtime-revocable (AGENTS.md §2) — not retryable.
                socket?.let(::closeQuietly)
                return ChannelOpen.Failed(BudsError.PermissionDenied)
            } catch (e: IOException) {
                // The socket that failed is closed too — not only those that succeeded (rule 3).
                socket?.let(::closeQuietly)
                lastDetail = BleLogger.describe(e)
                val elapsedMs = (System.nanoTime() - startedNanos) / 1_000_000
                BleLogger.logConnectionEvent(
                    "RFCOMM channel 0x%02x connect failed after %d ms (attempt %d/%d): %s"
                        .format(channelId, elapsedMs, attempt, connectAttempts, lastDetail),
                )
                if (elapsedMs > fastFailureThresholdMs) break // slow failure: peer unreachable, not a collision.
            } catch (e: RuntimeException) {
                socket?.let(::closeQuietly)
                return ChannelOpen.Failed(BudsError.Unknown(e))
            }
            if (attempt < connectAttempts) delay(retryDelayMs)
        }
        return ChannelOpen.Failed(BudsError.ChannelUnavailable(channelId, lastDetail))
    }

    private fun startReader(connection: Connection, channelId: Int, socket: RfcommSocket, onDemand: Boolean): Job =
        connection.scope.launch {
            val buffer = ByteArray(1024)
            try {
                val input = socket.inputStream
                while (isActive) {
                    val read = input.read(buffer)
                    if (read < 0) {
                        // EOF: the peer/stack closed this channel. Not "normal" — the channel is
                        // dead, and leaving the UI on Ready would be wrong.
                        endOfChannel(connection, channelId, "stream closed (EOF)", onDemand)
                        break
                    }
                    _inbound.emit(channelId to buffer.copyOf(read))
                }
            } catch (e: IOException) {
                // isActive is false when this IOException was *caused* by our own disconnect()/
                // closeChannel()/teardown closing the socket under a blocked read() — an expected,
                // silent end.
                if (isActive) endOfChannel(connection, channelId, BleLogger.describe(e), onDemand)
            }
        }

    private fun endOfChannel(connection: Connection, channelId: Int, detail: String?, onDemand: Boolean) {
        if (onDemand) reportChannelClosed(connection, channelId, detail) else reportLoss(connection, channelId, detail)
    }

    /**
     * An on-demand channel (ADR-032) died on its own: drop just that socket and announce it. If
     * [closeChannel] already removed it (a deliberate close) the removal below finds nothing and
     * nothing is announced. The connection and its other channels are untouched.
     */
    private fun reportChannelClosed(connection: Connection, channelId: Int, detail: String?) {
        val socket = connection.auxSockets.remove(channelId) ?: return
        connection.auxJobs.remove(channelId)?.cancel()
        closeQuietly(socket)
        if (current !== connection) return
        BleLogger.logConnectionEvent(
            "RFCOMM on-demand channel 0x%02x closed (%s) — session channels untouched".format(channelId, detail),
        )
        _channelClosed.tryEmit(ChannelClosed(channelId, detail))
    }

    /** Rules 1 and 2: dedupe per connection, tear everything down, and only then announce it. */
    private fun reportLoss(connection: Connection, channelId: Int, detail: String?) {
        if (!connection.lossReported.compareAndSet(false, true)) return
        val wasCurrent = synchronized(lock) {
            if (current === connection) {
                current = null
                true
            } else {
                false
            }
        }
        connection.closeAll()
        if (!wasCurrent) return // stale: already replaced by a newer connect() or ended by disconnect().
        BleLogger.logConnectionEvent(
            "RFCOMM channel 0x%02x lost (%s) — all channels of this connection closed".format(channelId, detail),
        )
        _connectionLost.tryEmit(ConnectionLoss(channelId, detail))
    }

    private fun replaceCurrent(next: Connection?): Connection? = synchronized(lock) {
        val previous = current
        current = next
        previous
    }

    override suspend fun send(channelId: Int, frame: ByteArray): BudsResult<Unit> =
        withContext(ioDispatcher) {
            val connection = current ?: return@withContext BudsResult.Failure(BudsError.ConnectionLost)
            val onDemand = connection.auxSockets.containsKey(channelId)
            val socket = connection.sockets[channelId] ?: connection.auxSockets[channelId]
                ?: return@withContext BudsResult.Failure(BudsError.ConnectionLost)
            try {
                val output = socket.outputStream
                output.write(frame)
                output.flush()
                BudsResult.Success(Unit)
            } catch (e: IOException) {
                val detail = BleLogger.describe(e)
                endOfChannel(connection, channelId, detail, onDemand)
                BudsResult.Failure(BudsError.ChannelLost(channelId, detail))
            }
        }

    override suspend fun openChannel(channelId: Int, uuid: UUID): BudsResult<Unit> = withContext(ioDispatcher) {
        val connection = current ?: return@withContext BudsResult.Failure(BudsError.ConnectionLost)
        if (connection.sockets.containsKey(channelId) || connection.auxSockets.containsKey(channelId)) {
            return@withContext BudsResult.Success(Unit)
        }
        when (val result = openSocketWithRetry(channelId, uuid, connection.open)) {
            is ChannelOpen.Failed -> BudsResult.Failure(result.error)
            is ChannelOpen.Opened -> {
                // The connection may have been lost or replaced while this (possibly retried) open
                // was in flight — never attach a socket to a dead connection.
                if (current !== connection || !connection.isOpen) {
                    closeQuietly(result.socket)
                    return@withContext BudsResult.Failure(BudsError.ConnectionLost)
                }
                connection.auxSockets[channelId] = result.socket
                connection.auxJobs[channelId] = startReader(connection, channelId, result.socket, onDemand = true)
                BudsResult.Success(Unit)
            }
        }
    }

    override suspend fun closeChannel(channelId: Int) = withContext(ioDispatcher) {
        val connection = current ?: return@withContext
        // Remove first, so the reader's resulting IOException finds nothing to report (deliberate close).
        val socket = connection.auxSockets.remove(channelId) ?: return@withContext
        connection.auxJobs.remove(channelId)?.cancel()
        closeQuietly(socket)
        BleLogger.logConnectionEvent("RFCOMM on-demand channel 0x%02x released".format(channelId))
    }

    override fun isChannelOpen(channelId: Int): Boolean {
        val connection = current ?: return false
        return connection.isOpen &&
            (connection.sockets.containsKey(channelId) || connection.auxSockets.containsKey(channelId))
    }

    override suspend fun disconnect() = withContext(ioDispatcher) {
        replaceCurrent(null)?.closeAll()
        Unit
    }

    private fun closeQuietly(socket: RfcommSocket) {
        try {
            socket.close()
        } catch (e: IOException) {
            // Closing an already-broken socket — the only thing left to do is note it.
            BleLogger.logConnectionEvent("RFCOMM socket close: ${BleLogger.describe(e)}")
        }
    }

    companion object {
        /** Attempts per channel within one [connect] call (rule 4). */
        const val DEFAULT_CONNECT_ATTEMPTS = 3

        /** Pause between attempts — long enough for the stack's DISC/UA teardown of a port the
         * failed attempt itself just closed (~50 ms in the `ai-sessions/0039` logs), short enough
         * to be invisible next to a human tap. */
        const val DEFAULT_RETRY_DELAY_MS = 400L

        /** Collision failures returned in 60–190 ms in the `ai-sessions/0039` logs; an unreachable
         * peer blocks for seconds. Anything slower than this is not retried. */
        const val DEFAULT_FAST_FAILURE_THRESHOLD_MS = 2_000L
    }
}
