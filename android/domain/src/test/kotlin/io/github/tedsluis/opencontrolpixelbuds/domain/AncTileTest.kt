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
package io.github.tedsluis.opencontrolpixelbuds.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AncTileTest {

    @Test
    fun `a Ready session with no ANC report yet says Tap to switch, not Open the app`() = runBlocking {
        // A58-APP-01: the repository's ANC mode is a replay-1 SharedFlow with no value until the Buds report one (e.g. the Connect snapshot lost DLCI 0x04).
        val mode = MutableSharedFlow<AncMode>(replay = 1)
        val state = withTimeout(1_000) {
            ancTileStates(MutableStateFlow(ConnectionState.Ready), mode, MutableStateFlow(AncAvailability.UNKNOWN)).first()
        }
        assertTrue(state.ready)
        assertEquals("Tap to switch", state.subtitle)
        assertFalse(state.active)
    }

    @Test
    fun `the mapping follows the session, the availability and the mode`() {
        assertEquals("Open the app", ancTileState(ConnectionState.Disconnected, AncMode.ACTIVE, AncAvailability.ALLOWED).subtitle)
        assertEquals("Only while worn", ancTileState(ConnectionState.Ready, AncMode.ACTIVE, AncAvailability.NOT_ALLOWED).subtitle)
        val adaptive = ancTileState(ConnectionState.Ready, AncMode.ADAPTIVE, AncAvailability.ALLOWED)
        assertEquals("Adaptive", adaptive.subtitle)
        assertTrue(adaptive.active)
        assertFalse(ancTileState(ConnectionState.Ready, AncMode.OFF, AncAvailability.ALLOWED).active)
    }
}
