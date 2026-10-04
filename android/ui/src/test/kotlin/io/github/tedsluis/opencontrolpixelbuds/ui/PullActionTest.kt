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
package io.github.tedsluis.opencontrolpixelbuds.ui

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/** D-10's table (`ai-sessions/0057`): every tab × every state maps to exactly one existing action. Plain JVM, no Android. */
class PullActionTest {

    private val ready = ConnectionState.Ready

    @Test
    fun `while Ready each tab runs its own refresh - Find runs Refresh battery (the maintainer's checkpoint choice)`() {
        val status = DeviceStatus.ControlledByApp
        assertEquals(PullAction.REFRESH_BATTERY, pullActionFor(PullTab.CONNECTION, status, ready))
        assertEquals(PullAction.REFRESH_ANC, pullActionFor(PullTab.ANC, status, ready))
        assertEquals(PullAction.REFRESH_SOUND, pullActionFor(PullTab.SOUND, status, ready))
        assertEquals(PullAction.REFRESH_SETTINGS, pullActionFor(PullTab.CONTROLS, status, ready))
        assertEquals(PullAction.REFRESH_BATTERY, pullActionFor(PullTab.FIND, status, ready))
    }

    @Test
    fun `not connected but paired - every tab connects (Connect or Retry)`() {
        for (tab in PullTab.entries) {
            assertEquals(PullAction.CONNECT, pullActionFor(tab, DeviceStatus.ConnectedToPhone, ConnectionState.Disconnected))
            assertEquals(PullAction.CONNECT, pullActionFor(tab, DeviceStatus.PairedNotConnected, ConnectionState.Failed(BudsError.Timeout)))
        }
    }

    @Test
    fun `while connecting a pull does nothing`() {
        for (tab in PullTab.entries) {
            assertEquals(PullAction.NOTHING, pullActionFor(tab, DeviceStatus.Connecting, ConnectionState.Connecting))
            assertEquals(PullAction.NOTHING, pullActionFor(tab, DeviceStatus.Connecting, ConnectionState.Discovering))
        }
    }

    @Test
    fun `where Connect cannot start a pull does what that state's button does`() {
        for (tab in PullTab.entries) {
            val idle = ConnectionState.Disconnected
            assertEquals(PullAction.ENABLE_BLUETOOTH, pullActionFor(tab, DeviceStatus.BluetoothOff, idle))
            assertEquals(PullAction.REQUEST_PERMISSIONS, pullActionFor(tab, DeviceStatus.PermissionMissing(PermissionStatus.NOT_REQUESTED), idle))
            assertEquals(PullAction.REQUEST_PERMISSIONS, pullActionFor(tab, DeviceStatus.PermissionMissing(PermissionStatus.DENIED), idle))
            assertEquals(PullAction.OPEN_APP_SETTINGS, pullActionFor(tab, DeviceStatus.PermissionMissing(PermissionStatus.PERMANENTLY_DENIED), idle))
            assertEquals(PullAction.PAIR, pullActionFor(tab, DeviceStatus.NotPaired, idle))
            assertEquals(PullAction.PAIR, pullActionFor(tab, DeviceStatus.SeveralBudsPaired, idle))
        }
    }
}
