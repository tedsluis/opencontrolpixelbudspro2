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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConnectionStateMachineTest {

    @Test
    fun `starts disconnected`() {
        val sm = ConnectionStateMachine()
        assertEquals(ConnectionState.Disconnected, sm.state.value)
    }

    @Test
    fun `walks Disconnected to Connecting to Discovering to Ready`() {
        val sm = ConnectionStateMachine()
        sm.onConnectRequested()
        assertEquals(ConnectionState.Connecting, sm.state.value)
        sm.onLinkEstablished()
        assertEquals(ConnectionState.Discovering, sm.state.value)
        sm.onReady()
        assertEquals(ConnectionState.Ready, sm.state.value)
    }

    @Test
    fun `a socket IOException moves Ready to Disconnected, never a crash`() {
        val sm = ConnectionStateMachine()
        sm.onConnectRequested()
        sm.onLinkEstablished()
        sm.onReady()
        sm.onDisconnected()
        assertEquals(ConnectionState.Disconnected, sm.state.value)
    }

    @Test
    fun `an error surfaces as Failed with the specific BudsError, not a generic message`() {
        val sm = ConnectionStateMachine()
        sm.onConnectRequested()
        sm.onError(BudsError.PermissionDenied)
        val state = sm.state.value
        assertTrue(state is ConnectionState.Failed)
        assertEquals(BudsError.PermissionDenied, (state as ConnectionState.Failed).error)
    }

    @Test
    fun `a stale onReady from Disconnected is ignored, not a spurious transition`() {
        val sm = ConnectionStateMachine()
        sm.onReady()
        assertEquals(ConnectionState.Disconnected, sm.state.value)
    }

    @Test
    fun `fake transport records what the pipeline sends without real hardware`() = runTest {
        val transport = FakeBudsTransport()
        val result = transport.send(channelId = 4, frame = byteArrayOf(0x08, 0x11, 0x00, 0x00))
        assertTrue(result is io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult.Success)
        assertEquals(1, transport.sent.size)
        assertEquals(4, transport.sent[0].first)
    }

    @Test
    fun `fake transport reports ConnectionLost when disconnected, per ARCHITECTURE md 6`() = runTest {
        val transport = FakeBudsTransport()
        transport.connected = false
        val result = transport.send(channelId = 4, frame = byteArrayOf(0x08, 0x11, 0x00, 0x00))
        assertTrue(result is io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult.Failure)
        assertEquals(
            BudsError.ConnectionLost,
            (result as io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult.Failure).error,
        )
    }
}
