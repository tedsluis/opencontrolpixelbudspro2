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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import kotlinx.coroutines.flow.Flow

/**
 * Abstracts the underlying RFCOMM `BluetoothSocket` (primary transport) so
 * `:data` never touches raw Android BT types directly (ARCHITECTURE.md §2.1).
 * [channelId] addresses one of the three coexisting RFCOMM DLCIs
 * (PROTOCOL.md §2.3) — required, not optional, since each channel has
 * independent framing and cannot share one send/receive path.
 */
interface BudsTransport {
    val connected: Boolean

    /** Inbound frames as `(channelId, frame)` pairs, one element per already
     * frame-delimited payload — buffering/frame-boundary detection is each
     * DLCI's own `FrameDecoder`'s job (ARCHITECTURE.md §5), not this
     * interface's. */
    val inbound: Flow<Pair<Int, ByteArray>>

    suspend fun send(channelId: Int, frame: ByteArray): BudsResult<Unit>
}
