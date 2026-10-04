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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** The one "is this value current?" rule of `ai-sessions/0069` (A68-APP-02), used by every tab, the tile and the notification. */
class ValueCurrencyTest {

    @Test
    fun `a value is current only while Ready and reported on this connection`() {
        assertTrue(isCurrent(ready = true, valueAtMillis = 2_000, sessionSinceMillis = 1_000))
        assertTrue(isCurrent(ready = true, valueAtMillis = 1_000, sessionSinceMillis = 1_000), "reported in the millisecond the session opened")
        assertFalse(isCurrent(ready = true, valueAtMillis = 999, sessionSinceMillis = 1_000), "from the connection before this one")
        assertFalse(isCurrent(ready = false, valueAtMillis = 2_000, sessionSinceMillis = 1_000), "not connected: the last connection's value")
        assertFalse(isCurrent(ready = true, valueAtMillis = null, sessionSinceMillis = 1_000), "never reported")
        assertFalse(isCurrent(ready = true, valueAtMillis = 2_000, sessionSinceMillis = null), "no session was ever opened")
    }

    @Test
    fun `currentAncMode hides a mode from the last connection until this connection reports one`() = runBlocking {
        val state = MutableStateFlow<ConnectionState>(ConnectionState.Ready)
        val mode = MutableStateFlow<AncMode?>(AncMode.ACTIVE)
        val updatedAt = MutableStateFlow<Long?>(500)
        val since = MutableStateFlow<Long?>(1_000)
        val current = currentAncMode(state, mode, updatedAt, since)
        assertNull(current.first(), "reported before this connection opened")
        updatedAt.value = 1_200
        assertEquals(AncMode.ACTIVE, current.first())
        state.value = ConnectionState.Disconnected
        assertNull(current.first(), "not connected")
    }

    @Test
    fun `the tile says Tap to switch for a mode that is only known from the last connection`() = runBlocking {
        val tile = ancTileStates(
            MutableStateFlow(ConnectionState.Ready),
            currentAncMode(MutableStateFlow(ConnectionState.Ready), MutableStateFlow(AncMode.ADAPTIVE), MutableStateFlow(500L), MutableStateFlow(1_000L)),
            MutableStateFlow(AncAvailability.ALLOWED),
        ).first()
        assertEquals("Tap to switch", tile.subtitle)
        assertFalse(tile.active)
    }
}
