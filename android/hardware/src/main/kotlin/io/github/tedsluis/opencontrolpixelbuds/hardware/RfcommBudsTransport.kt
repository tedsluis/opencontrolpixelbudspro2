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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Real `BluetoothSocket`-backed [BudsTransport]. **Sketched, not verified —
 * no physical Pixel Buds Pro 2 is available in this environment, per this
 * phase's own explicit scope note.** This class compiles and follows
 * AGENTS.md §3's rules (all I/O on `Dispatchers.IO`, every socket call site
 * wrapped and converted to a [BudsError], never a bare
 * `catch (e: Exception) {}`), but its actual multi-DLCI multiplexing over one
 * `BluetoothSocket` is intentionally left as a `TODO(verify)` — RFCOMM
 * exposes one `BluetoothSocket` per SDP-negotiated channel, so a real
 * implementation needs one socket per DLCI (PROTOCOL.md §2.3), opened against
 * the two SDP UUIDs `DECISIONS.md` ADR-018/`CAP-033-FINDINGS.md` §3 already
 * identify — connecting and demultiplexing those against real hardware is
 * exactly the part this environment cannot validate.
 *
 * // TODO(verify): per-DLCI socket selection/opening (PROTOCOL.md §2.3's
 * // three-channel table) is not implemented here — this class demonstrates
 * // only the error-handling/dispatcher shape a real implementation must
 * // follow, against a single caller-supplied, already-connected socket.
 */
class RfcommBudsTransport(
    private val device: BluetoothDevice,
    private val socketFactory: () -> BluetoothSocket,
) : BudsTransport {

    private var socket: BluetoothSocket? = null
    override var connected: Boolean = false
        private set

    private val _inbound = MutableSharedFlow<Pair<Int, ByteArray>>(extraBufferCapacity = 64)
    override val inbound: SharedFlow<Pair<Int, ByteArray>> = _inbound

    suspend fun connect(channelId: Int): BudsResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val newSocket = socketFactory()
            newSocket.connect()
            socket = newSocket
            connected = true
            BudsResult.Success(Unit)
        } catch (e: IOException) {
            connected = false
            BudsResult.Failure(BudsError.ConnectionLost)
        } catch (e: SecurityException) {
            BudsResult.Failure(BudsError.PermissionDenied)
        }
    }

    override suspend fun send(channelId: Int, frame: ByteArray): BudsResult<Unit> =
        withContext(Dispatchers.IO) {
            val output = socket?.outputStream
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
        try {
            socket?.close()
        } catch (e: IOException) {
            // Closing an already-broken socket — nothing further to report.
        } finally {
            connected = false
            socket = null
        }
    }
}
