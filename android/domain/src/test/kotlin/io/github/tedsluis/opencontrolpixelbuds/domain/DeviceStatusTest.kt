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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** The permission state machine and the Connection status model (`ai-sessions/0041`) — pure logic, no Android. */
class DeviceStatusTest {

    @Test
    @DisplayName("permissionStatus: granted / never asked / denied with rationale / blocked (asked, no rationale left)")
    fun `permission state machine`() {
        assertEquals(PermissionStatus.GRANTED, permissionStatus(granted = true, requestedThisRun = false, shouldShowRationale = false))
        assertEquals(PermissionStatus.GRANTED, permissionStatus(granted = true, requestedThisRun = true, shouldShowRationale = true))
        assertEquals(PermissionStatus.NOT_REQUESTED, permissionStatus(granted = false, requestedThisRun = false, shouldShowRationale = false))
        assertEquals(PermissionStatus.DENIED, permissionStatus(granted = false, requestedThisRun = true, shouldShowRationale = true))
        assertEquals(PermissionStatus.PERMANENTLY_DENIED, permissionStatus(granted = false, requestedThisRun = true, shouldShowRationale = false))
    }

    private fun status(
        bluetoothEnabled: Boolean = true,
        permission: PermissionStatus = PermissionStatus.GRANTED,
        bonded: Boolean = true,
        link: AndroidLink = AndroidLink.UNKNOWN,
        session: ConnectionState = ConnectionState.Disconnected,
    ) = deriveDeviceStatus(bluetoothEnabled, permission, bonded, link, session)

    @Test
    fun `Bluetooth off wins over everything`() {
        assertEquals(DeviceStatus.BluetoothOff, status(bluetoothEnabled = false, permission = PermissionStatus.DENIED, bonded = false))
    }

    @Test
    @DisplayName("a missing permission is its own state and is never reported as 'not paired' (the ai-sessions/0041 defect)")
    fun `missing permission is not not-paired`() {
        for (p in listOf(PermissionStatus.NOT_REQUESTED, PermissionStatus.DENIED, PermissionStatus.PERMANENTLY_DENIED)) {
            assertEquals(DeviceStatus.PermissionMissing(p), status(permission = p, bonded = false))
            assertEquals(DeviceStatus.PermissionMissing(p), status(permission = p, bonded = true, link = AndroidLink.CONNECTED))
        }
    }

    @Test
    fun `not paired only when permitted and nothing is bonded`() {
        assertEquals(DeviceStatus.NotPaired, status(bonded = false))
        assertEquals(DeviceStatus.NotPaired, status(bonded = false, link = AndroidLink.CONNECTED, session = ConnectionState.Ready))
    }

    @Test
    @DisplayName("Android × app session: every combination of link and session state")
    fun `every android-by-session combination`() {
        val expected = mapOf(
            // (link, session) -> status
            (AndroidLink.UNKNOWN to ConnectionState.Disconnected) to DeviceStatus.PairedNotConnected,
            (AndroidLink.NOT_CONNECTED to ConnectionState.Disconnected) to DeviceStatus.PairedNotConnected,
            (AndroidLink.CONNECTED to ConnectionState.Disconnected) to DeviceStatus.ConnectedToPhone,
            (AndroidLink.CONNECTED to ConnectionState.Failed(BudsError.Timeout)) to DeviceStatus.ConnectedToPhone,
            (AndroidLink.NOT_CONNECTED to ConnectionState.Failed(BudsError.Timeout)) to DeviceStatus.PairedNotConnected,
            (AndroidLink.CONNECTED to ConnectionState.Connecting) to DeviceStatus.Connecting,
            (AndroidLink.NOT_CONNECTED to ConnectionState.Discovering) to DeviceStatus.Connecting,
            (AndroidLink.CONNECTED to ConnectionState.Ready) to DeviceStatus.ControlledByApp,
            (AndroidLink.NOT_CONNECTED to ConnectionState.Ready) to DeviceStatus.ControlledByApp, // an open session is authoritative
            (AndroidLink.UNKNOWN to ConnectionState.Ready) to DeviceStatus.ControlledByApp,
        )
        for ((input, want) in expected) assertEquals(want, status(link = input.first, session = input.second), input.toString())
    }

    @Test
    @DisplayName("the card: Android connected + session closed = prominent Connect; failed session = Retry; ready = Disconnect")
    fun `card per combination`() {
        val closed = ConnectionState.Disconnected
        val failed = ConnectionState.Failed(BudsError.Timeout)
        assertEquals(StatusCard(AndroidLine.CONNECTED, SessionLine.NOT_OPEN, CardAction.CONNECT), statusCard(DeviceStatus.ConnectedToPhone, AndroidLink.CONNECTED, closed))
        assertEquals(StatusCard(AndroidLine.CONNECTED, SessionLine.FAILED, CardAction.RETRY), statusCard(DeviceStatus.ConnectedToPhone, AndroidLink.CONNECTED, failed))
        assertEquals(StatusCard(AndroidLine.NOT_CONNECTED, SessionLine.NOT_OPEN, CardAction.CONNECT), statusCard(DeviceStatus.PairedNotConnected, AndroidLink.NOT_CONNECTED, closed))
        assertEquals(StatusCard(AndroidLine.NOT_CONNECTED, SessionLine.NOT_OPEN, CardAction.CONNECT), statusCard(DeviceStatus.PairedNotConnected, AndroidLink.UNKNOWN, closed))
        assertEquals(StatusCard(AndroidLine.NOT_CONNECTED, SessionLine.FAILED, CardAction.RETRY), statusCard(DeviceStatus.PairedNotConnected, AndroidLink.NOT_CONNECTED, failed))
        assertEquals(StatusCard(AndroidLine.CONNECTED, SessionLine.OPEN, CardAction.DISCONNECT), statusCard(DeviceStatus.ControlledByApp, AndroidLink.CONNECTED, ConnectionState.Ready))
        assertEquals(
            StatusCard(AndroidLine.NOT_REPORTED_WHILE_CONTROLLED, SessionLine.OPEN, CardAction.DISCONNECT),
            statusCard(DeviceStatus.ControlledByApp, AndroidLink.NOT_CONNECTED, ConnectionState.Ready),
        )
        assertEquals(StatusCard(AndroidLine.CONNECTED, SessionLine.OPENING, CardAction.NONE), statusCard(DeviceStatus.Connecting, AndroidLink.CONNECTED, ConnectionState.Connecting))
    }

    @Test
    fun `no card for the statuses that have their own panel`() {
        assertNull(statusCard(DeviceStatus.BluetoothOff, AndroidLink.UNKNOWN, ConnectionState.Disconnected))
        assertNull(statusCard(DeviceStatus.NotPaired, AndroidLink.UNKNOWN, ConnectionState.Disconnected))
        assertNull(statusCard(DeviceStatus.PermissionMissing(PermissionStatus.DENIED), AndroidLink.UNKNOWN, ConnectionState.Disconnected))
    }
}
