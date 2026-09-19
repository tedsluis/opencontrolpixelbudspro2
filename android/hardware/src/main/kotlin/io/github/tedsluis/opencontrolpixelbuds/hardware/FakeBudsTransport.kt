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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

/**
 * Scripted [BudsTransport] for domain/data unit tests — no real
 * `BluetoothSocket` involved (AGENTS.md §11). [connect]/[disconnect] are
 * trivial state flips, not real socket I/O — `RfcommBudsTransport` is the
 * real implementation, unit-tested only up to what doesn't require actual
 * hardware (see its own doc comment).
 */
class FakeBudsTransport : BudsTransport {
    override var connected: Boolean = true

    private val _inbound = MutableSharedFlow<Pair<Int, ByteArray>>(extraBufferCapacity = 64)
    override val inbound: SharedFlow<Pair<Int, ByteArray>> = _inbound

    private val _connectionLost = MutableSharedFlow<ConnectionLoss>(extraBufferCapacity = 8)
    override val connectionLost: SharedFlow<ConnectionLoss> = _connectionLost

    val sent = mutableListOf<Pair<Int, ByteArray>>()
    var sendShouldFail: BudsError? = null
    var connectShouldFail: BudsError? = null

    /** Test hook: push a scripted inbound frame as if the Buds had sent it. */
    suspend fun emit(channelId: Int, frame: ByteArray) {
        _inbound.emit(channelId to frame)
    }

    /** Test hook: simulate the peer dropping the link (range loss, OS teardown) without an
     * explicit [disconnect] call — see [BudsTransport.connectionLost]'s own doc comment. */
    suspend fun emitConnectionLost(loss: ConnectionLoss = ConnectionLoss(channelId = 4, detail = "scripted loss")) {
        connected = false
        _connectionLost.emit(loss)
    }

    override suspend fun connect(device: BluetoothDevice, channels: Map<Int, UUID>): BudsResult<Unit> {
        connectShouldFail?.let { return BudsResult.Failure(it) }
        connected = true
        return BudsResult.Success(Unit)
    }

    override suspend fun disconnect() {
        connected = false
    }

    override suspend fun send(channelId: Int, frame: ByteArray): BudsResult<Unit> {
        sendShouldFail?.let { return BudsResult.Failure(it) }
        if (!connected) return BudsResult.Failure(BudsError.ConnectionLost)
        sent += channelId to frame
        return BudsResult.Success(Unit)
    }
}
