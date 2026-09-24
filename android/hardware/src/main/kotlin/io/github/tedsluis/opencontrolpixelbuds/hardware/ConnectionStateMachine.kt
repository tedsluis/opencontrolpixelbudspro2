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
 * As built (DECISIONS.md ADR-032, ARCHITECTURE.md §2.1): `Ready` means "the MAESTRO channel (DLCI 0x02) is open" — the
 * session; the Message Stream (DLCI 0x04) is claimed on demand afterwards and plays no part in these states. `Discovering`
 * is a zero-length pass-through between `onLinkEstablished()` and `onReady()` (SDP resolution happens inside the socket
 * open), kept only so the transition log reads the same as before. These are app states, not wire-observed ones.
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
        // Already Disconnected: nothing to transition to — logging/re-emitting would only produce
        // the duplicate "Disconnected -> Disconnected" lines `ai-sessions/0039` found (two sockets'
        // readers each reporting the same loss).
        if (_state.value is ConnectionState.Disconnected) return
        BleLogger.logConnectionEvent("ConnectionState: ${_state.value::class.simpleName} -> Disconnected")
        _state.value = ConnectionState.Disconnected
    }

    fun onError(error: BudsError) {
        BleLogger.logConnectionEvent("ConnectionState: ${_state.value::class.simpleName} -> Failed(${error::class.simpleName})")
        _state.value = ConnectionState.Failed(error)
    }

    private inline fun transition(from: (ConnectionState) -> Boolean, next: () -> ConnectionState) {
        val current = _state.value
        if (from(current)) {
            val nextState = next()
            BleLogger.logConnectionEvent("ConnectionState: ${current::class.simpleName} -> ${nextState::class.simpleName}")
            _state.value = nextState
        }
    }
}
