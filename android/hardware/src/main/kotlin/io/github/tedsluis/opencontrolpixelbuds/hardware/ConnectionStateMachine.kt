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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Explicit state machine driving [ConnectionState] (ARCHITECTURE.md §2.1).
 *
 * // TODO(verify): only the `Disconnected -> Connecting -> Ready` shape for
 * // the classic BR/EDR link steps is 🟢 FACT (PROTOCOL.md §5.1). The
 * // `Discovering` step and the exact point at which "Ready" should fire
 * // relative to DLCI 0x04's own connect-time Get/Notify handshake
 * // (PROTOCOL.md §5.2) are still ⚪ ASSUMPTION — this class is intentionally
 * // a thin, honestly-scoped skeleton, not a claim that every transition
 * // below has been observed on the wire. See PROTOCOL.md §5.
 */
@Singleton
class ConnectionStateMachine @Inject constructor() {
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state: StateFlow<ConnectionState> = _state

    fun onConnectRequested() {
        transition(from = { it is ConnectionState.Disconnected || it is ConnectionState.Failed }) {
            ConnectionState.Connecting
        }
    }

    fun onLinkEstablished() {
        transition(from = { it is ConnectionState.Connecting }) {
            ConnectionState.Discovering
        }
    }

    fun onReady() {
        transition(from = { it is ConnectionState.Discovering || it is ConnectionState.Connecting }) {
            ConnectionState.Ready
        }
    }

    /** ARCHITECTURE.md §6: an `IOException` from the socket (OS-triggered
     * teardown, range loss, peer disconnect) always moves to Disconnected —
     * this is a normal, expected transition, never a crash condition. */
    fun onDisconnected() {
        _state.value = ConnectionState.Disconnected
    }

    fun onError(error: BudsError) {
        _state.value = ConnectionState.Failed(error)
    }

    private inline fun transition(from: (ConnectionState) -> Boolean, next: () -> ConnectionState) {
        val current = _state.value
        if (from(current)) {
            _state.value = next()
        }
    }
}
