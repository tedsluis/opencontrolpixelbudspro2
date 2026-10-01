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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DarkMode
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.FirmwareEntry
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The settings menu of `ai-sessions/0062` (F-4/F-5/F-6, the maintainer's design in chat 2026-10-01), rendered under Robolectric: the gear opens it, its three
 * tabs render, back returns to the tab it was opened from, Info shows the build and the firmware (or "not connected yet"), and the dark-mode choice changes
 * the colour scheme at once.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsMenuTest {

    @get:Rule
    val compose = createComposeRule()

    private val at = 1_727_600_000_000L

    /** `CAP-065` frame 1883's announcement as the repository turns it into [DeviceInfo]: three entries `release_5.203`, channel 19. */
    private val announced = DeviceInfo(
        firmware = listOf("release_5.203"),
        entries = listOf(FirmwareEntry(1, "release_5.203"), FirmwareEntry(2, "release_5.203"), FirmwareEntry(3, "release_5.203")),
        maestroChannel = 19,
        announcedAtMillis = at,
    )

    private fun uiState(deviceInfo: DeviceInfo? = announced, darkMode: DarkMode = DarkMode.SYSTEM) = OpenControlUiState(
        connectionState = ConnectionState.Ready,
        bluetoothEnabled = true,
        hasBondedDevice = true,
        deviceStatus = DeviceStatus.ControlledByApp,
        permissionState = PermissionState(PermissionStatus.GRANTED, PermissionStatus.GRANTED),
        pairingStatusText = null,
        lastConnectionError = null,
        androidLink = AndroidLink.CONNECTED,
        messageStreamError = null,
        ancMode = null,
        eqProfile = null,
        eqError = null,
        batteryStatus = BatteryStatus(),
        unidentifiedFrames = emptyList(),
        debugModeEnabled = false,
        deviceInfo = deviceInfo,
        darkMode = darkMode,
        appBuild = AppBuildInfo("0.1.0-dev", "d541e00", "2026-10-01"),
    )

    private val noActions = OpenControlActions(
        onRequestEnableBluetooth = {}, onPair = {}, onRequestPermissions = {}, onOpenAppSettings = {}, onConnect = {}, onDisconnect = {},
        onAncModeSelected = {}, onRefreshAncMode = {}, onRequestAddAncTile = {}, onEqGainsChanged = {}, onEqPresetSelected = {}, onRefreshEq = {},
        onRing = {}, onStopRinging = {}, onRefreshBattery = {}, onDebugModeChanged = {}, onExportLog = {},
    )

    @Test
    fun `the gear opens the menu with its three tabs, the Debug tab is the Debug screen, back returns to the tab it came from`() {
        compose.setContent { OpenControlTheme(darkTheme = false) { OpenControlNavHost(uiState(), noActions) } }
        compose.onNodeWithText("ANC").performClick() // a tab other than the start one
        compose.onNodeWithText("Noise control").assertExists()

        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onAllNodesWithText("Settings").assertCountEquals(2) // the top bar's title and the first tab
        compose.onNodeWithText("Debug").assertExists()
        compose.onNodeWithText("Info").assertExists()
        compose.onNodeWithText("Dark mode").assertExists() // the Settings tab is first
        compose.onNodeWithText("Noise control").assertDoesNotExist()

        compose.onNodeWithText("Debug").performClick()
        compose.onNodeWithText("Debug mode (verbose hex-dump logging)").assertExists()
        compose.onNodeWithText("Export debug log").assertExists()
        compose.onNodeWithText("Unidentified frames (0)").assertExists()

        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Noise control").assertExists()
        compose.onNodeWithText("Dark mode").assertDoesNotExist()
    }

    @Test
    fun `Info shows the build and the firmware of each part with the control channel`() {
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo("0.1.0-dev", "d541e00-dirty", "2026-10-01"), announced)
            }
        }
        compose.onNodeWithText("Info").performClick()
        compose.onNodeWithText("App: 0.1.0-dev, build d541e00-dirty (2026-10-01)").assertExists()
        compose.onNodeWithText("Case: release_5.203").assertExists()
        compose.onNodeWithText("Left bud: release_5.203").assertExists()
        compose.onNodeWithText("Right bud: release_5.203").assertExists()
        compose.onNodeWithText("Control channel: 19").assertExists()
        compose.onNodeWithText("Firmware (from the Buds' announcement, ${formatUpdatedAt(at)}):").assertExists()
    }

    @Test
    fun `Info without an announcement says not connected yet, an unknown build says unknown`() {
        compose.setContent {
            OpenControlTheme(darkTheme = false) { SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, null) }
        }
        compose.onNodeWithText("Info").performClick()
        compose.onNodeWithText(INFO_NOT_CONNECTED).assertExists()
        compose.onNodeWithText("App: unknown, build unknown (unknown)").assertExists()
    }

    @Test
    fun `the entry labels are the official app's mapping, other indexes are never guessed`() {
        assertEquals(listOf("Case", "Left bud", "Right bud", "Part 4"), listOf(1, 2, 3, 4).map(::firmwareComponentLabel))
        val lines = firmwareInfoLines(announced.copy(entries = listOf(FirmwareEntry(3, "r3"), FirmwareEntry(1, "r1"), FirmwareEntry(2, "r2"))))
        assertEquals("in index order, each with its own string", listOf("Case: r1", "Left bud: r2", "Right bud: r3"), lines.drop(1).take(3))
    }

    @Test
    fun `dark mode On, Off and System change the scheme at once`() {
        var background = Color.Unspecified
        compose.setContent {
            var mode by remember { mutableStateOf(DarkMode.SYSTEM) }
            OpenControlTheme(darkTheme = mode.isDark(systemIsDark = false)) { // Android itself in light mode
                background = MaterialTheme.colorScheme.background
                SettingsMenuScreen(mode, { mode = it }, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, null)
            }
        }
        compose.onNodeWithText("System (follows Android)").assertIsSelected()
        assertTrue("System follows Android (light)", background.luminance() > 0.5f)

        compose.onNodeWithText("On").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("On").assertIsSelected()
        assertTrue("On is dark", background.luminance() < 0.5f)

        compose.onNodeWithText("Off").performClick()
        compose.waitForIdle()
        assertTrue("Off is light", background.luminance() > 0.5f)
    }

    @Test
    fun `the stored choice reads back, an unknown value is the default`() {
        assertEquals(DarkMode.ON, DarkMode.fromStored("ON"))
        assertEquals(DarkMode.SYSTEM, DarkMode.fromStored(null))
        assertEquals(DarkMode.SYSTEM, DarkMode.fromStored("SEPIA"))
        assertEquals(true, DarkMode.SYSTEM.isDark(systemIsDark = true))
        assertEquals(false, DarkMode.OFF.isDark(systemIsDark = true))
    }
}
