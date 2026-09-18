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
import android.bluetooth.BluetoothSocket
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/**
 * Real `BluetoothSocket`-backed [BudsTransport], one socket per RFCOMM DLCI
 * (PROTOCOL.md §2.3 — each channel has independent framing and cannot share
 * one socket). **Sketched, not verified against real hardware — no physical
 * Pixel Buds Pro 2 is available in this environment** (ARCHITECTURE.md §5a).
 * This class compiles and follows AGENTS.md §3's rules (all I/O on
 * `Dispatchers.IO`, every socket call site wrapped and converted to a
 * [BudsError], never a bare `catch (e: Exception) {}`), and resolves this
 * session's previously-open `// TODO(verify)` for per-DLCI socket selection —
 * but connecting and demultiplexing against real hardware is exactly the part
 * this environment cannot validate.
 *
 * Each socket's `InputStream` is read on its own coroutine and forwarded to
 * the shared [inbound] flow as raw, not-necessarily-frame-aligned chunks
 * (`BudsTransport.inbound`'s own doc comment) — `:data`'s `CodecRouter`
 * (Phase 3) does the actual frame-boundary detection.
 */
class RfcommBudsTransport(
    private val device: BluetoothDevice,
    private val socketFactory: (channelId: Int, uuid: UUID) -> BluetoothSocket,
) : BudsTransport {

    private val sockets = mutableMapOf<Int, BluetoothSocket>()
    private val supervisor = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisor)
    private var readerJobs: List<Job> = emptyList()

    override var connected: Boolean = false
        private set

    private val _inbound = MutableSharedFlow<Pair<Int, ByteArray>>(extraBufferCapacity = 256)
    override val inbound: SharedFlow<Pair<Int, ByteArray>> = _inbound

    /**
     * Opens one socket per entry in [channels] (channelId -> SDP UUID, see
     * [BudsSdpUuids]) and starts a reader coroutine for each. If any channel
     * fails to connect, every already-opened socket in this call is closed
     * before returning failure — this app has no use for a partially-usable
     * transport where, say, ANC works but EQ silently doesn't.
     */
    suspend fun connect(channels: Map<Int, UUID>): BudsResult<Unit> = withContext(Dispatchers.IO) {
        val opened = mutableMapOf<Int, BluetoothSocket>()
        try {
            for ((channelId, uuid) in channels) {
                val socket = socketFactory(channelId, uuid)
                socket.connect()
                opened[channelId] = socket
            }
            sockets.putAll(opened)
            connected = true
            readerJobs = opened.map { (channelId, socket) -> startReader(channelId, socket) }
            BudsResult.Success(Unit)
        } catch (e: IOException) {
            opened.values.forEach { runCatching { it.close() } }
            connected = false
            BudsResult.Failure(BudsError.ConnectionLost)
        } catch (e: SecurityException) {
            opened.values.forEach { runCatching { it.close() } }
            BudsResult.Failure(BudsError.PermissionDenied)
        }
    }

    private fun startReader(channelId: Int, socket: BluetoothSocket): Job = scope.launch {
        val input = socket.inputStream
        val buffer = ByteArray(1024)
        try {
            while (isActive) {
                val read = input.read(buffer)
                if (read < 0) break // peer closed the stream — normal disconnect, not an error.
                _inbound.emit(channelId to buffer.copyOf(read))
            }
        } catch (e: IOException) {
            // Socket torn down (OS teardown, range loss, peer disconnect) — a normal,
            // expected transition (ARCHITECTURE.md §6), not a crash. connected is left
            // for send()/the caller's next attempt to discover and report via
            // BudsError.ConnectionLost, rather than this reader coroutine racing to
            // flip shared state on its own.
        } finally {
            connected = false
        }
    }

    override suspend fun send(channelId: Int, frame: ByteArray): BudsResult<Unit> =
        withContext(Dispatchers.IO) {
            val output = sockets[channelId]?.outputStream
                ?: return@withContext BudsResult.Failure(BudsError.ConnectionLost)
            try {
                output.write(frame)
                output.flush()
                BudsResult.Success(Unit)
            } catch (e: IOException) {
                connected = false
                BudsResult.Failure(BudsError.ConnectionLost)
            }
        }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        readerJobs.forEach { it.cancel() }
        sockets.values.forEach { socket ->
            try {
                socket.close()
            } catch (e: IOException) {
                // Closing an already-broken socket — nothing further to report.
            }
        }
        sockets.clear()
        connected = false
        scope.coroutineContext.cancel()
    }
}
