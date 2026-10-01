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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * `ai-sessions/0064` F-3: the adapter's state changes are logged (always-on — `CAP-066`'s exports had no trace of either Bluetooth-off) and only TURNING_OFF / OFF
 * reach the repository as "off". The `CAP-066` system logs show the sequence ON → TURNING_OFF → OFF (`System-log-d55db7f4e1c8` 38635, 38754) and back
 * OFF → TURNING_ON → ON (39034, 39078).
 */
class BluetoothAdapterLogTest {
    @Test
    fun `the CAP-066 off and on sequences give one line per change`() {
        assertEquals("Bluetooth adapter: ON -> TURNING_OFF", BluetoothStateObserver.adapterTransitionLine(BluetoothAdapterState.ON, BluetoothAdapterState.TURNING_OFF))
        assertEquals("Bluetooth adapter: TURNING_OFF -> OFF", BluetoothStateObserver.adapterTransitionLine(BluetoothAdapterState.TURNING_OFF, BluetoothAdapterState.OFF))
        assertEquals("Bluetooth adapter: OFF -> TURNING_ON", BluetoothStateObserver.adapterTransitionLine(BluetoothAdapterState.OFF, BluetoothAdapterState.TURNING_ON))
        assertNull(BluetoothStateObserver.adapterTransitionLine(BluetoothAdapterState.ON, BluetoothAdapterState.ON), "no change, no line")
        assertEquals("Bluetooth adapter: UNKNOWN -> OFF", BluetoothStateObserver.adapterTransitionLine(null, BluetoothAdapterState.OFF))
    }

    @Test
    fun `only turning off and off are off for the repository`() {
        assertTrue(BluetoothStateObserver.isOn(BluetoothAdapterState.ON))
        assertTrue(BluetoothStateObserver.isOn(BluetoothAdapterState.TURNING_ON))
        assertFalse(BluetoothStateObserver.isOn(BluetoothAdapterState.TURNING_OFF))
        assertFalse(BluetoothStateObserver.isOn(BluetoothAdapterState.OFF))
    }
}
