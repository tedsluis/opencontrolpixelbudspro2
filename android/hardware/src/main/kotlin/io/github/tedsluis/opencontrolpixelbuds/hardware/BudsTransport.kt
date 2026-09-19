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
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Abstracts the underlying RFCOMM `BluetoothSocket` (primary transport) so
 * `:data` never touches raw Android BT types directly (ARCHITECTURE.md §2.1).
 * [channelId] addresses one of the three coexisting RFCOMM DLCIs
 * (PROTOCOL.md §2.3) — required, not optional, since each channel has
 * independent framing and cannot share one send/receive path.
 */
interface BudsTransport {
    val connected: Boolean

    /**
     * Inbound bytes as `(channelId, bytes)` pairs. Each element is whatever a
     * single `InputStream.read()` call returned — **not** necessarily one
     * complete logical frame, since RFCOMM is a byte stream with no
     * message-delimiting at the socket level. Buffering and frame-boundary
     * detection are `:data`'s `CodecRouter`'s job (ARCHITECTURE.md §5),
     * consuming this raw stream — this interface deliberately does no
     * framing of its own so that job stays in exactly one place.
     */
    val inbound: Flow<Pair<Int, ByteArray>>

    /**
     * Emits **at most once per connection** whenever this transport notices the connection was lost
     * for a reason other than this app's own [disconnect] call (peer disconnect, range loss, an OS-triggered socket
     * teardown) — ARCHITECTURE.md §6's "an `IOException` from the socket always moves to
     * Disconnected, a normal expected transition" rule, wired here so `BudsRepositoryImpl` can
     * react to it instead of only ever transitioning [io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine]
     * on an explicit user action. Never emitted for a caller-initiated [disconnect] (`ai-sessions/0038`
     * — before this, [connected] silently flipped to `false` with nothing downstream ever noticing,
     * so the UI kept showing `Ready` after the peer actually dropped the link).
     *
     * `ai-sessions/0039`: by the time this emits, the transport has already closed **every** socket
     * of the lost connection (an implementation must not leave a surviving channel's socket open —
     * the Android stack refuses a second RFCOMM connection to a channel that is still open, so a
     * zombie socket makes the very next [connect] fail), and a loss from a connection that has
     * already been replaced by a newer [connect] or ended by [disconnect] is never emitted.
     */
    val connectionLost: Flow<ConnectionLoss>

    /**
     * Opens one socket per entry in [channels] (channelId -> SDP UUID) against
     * [device] — [device] is a parameter here, not a constructor argument,
     * because it is only known once pairing has actually happened, which can
     * be well after this transport instance itself is constructed
     * (`ai-sessions/0037`).
     */
    suspend fun connect(device: BluetoothDevice, channels: Map<Int, UUID>): BudsResult<Unit>

    suspend fun disconnect()

    /**
     * Opens one more channel on the **current** connection, on demand (DECISIONS.md ADR-032): the
     * Message Stream channel (DLCI 0x04) is shared with other apps (Google Play services' Fast Pair)
     * and is claimed only for the duration of a user action. Unlike a channel opened by [connect],
     * an on-demand channel's loss is **not** a connection loss — it is reported on [channelClosed]
     * and leaves the connection (and its [connect]-opened channels) untouched. Idempotent: succeeds
     * immediately if [channelId] is already open. Fails with `BudsError.ConnectionLost` when there is
     * no current connection.
     */
    suspend fun openChannel(channelId: Int, uuid: UUID): BudsResult<Unit>

    /** Deliberately closes an on-demand channel. Never reported on [channelClosed]. No-op if not open. */
    suspend fun closeChannel(channelId: Int)

    /** Whether [channelId] is currently open on the current connection. */
    fun isChannelOpen(channelId: Int): Boolean

    /**
     * Emits when an on-demand channel ([openChannel]) died on its own (the peer or another client's
     * failed connect closed it) rather than through [closeChannel] — a normal, expected event for the
     * shared Message Stream channel (ADR-032), not a connection loss.
     */
    val channelClosed: Flow<ChannelClosed>

    suspend fun send(channelId: Int, frame: ByteArray): BudsResult<Unit>
}

/**
 * A connection-loss report from [BudsTransport.connectionLost]: which RFCOMM channel's read/write
 * noticed the loss first ([channelId]) and a short, address-redacted description of why ([detail]).
 */
data class ConnectionLoss(val channelId: Int, val detail: String?)

/** An on-demand channel died on its own ([BudsTransport.channelClosed]); [detail] is address-redacted. */
data class ChannelClosed(val channelId: Int, val detail: String?)
