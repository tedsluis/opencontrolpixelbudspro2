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

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * `ai-sessions/0064` F-1: a configuration change keeps the tab. `CAP-066` (FINDINGS §6, K4r): rotating to landscape relaunched the activity (logcat
 * `wm_on_create` 14:31:47.326 UTC) and the screen went from **Sound** (film 16:31:46.5) to **Connection** (16:31:47.5). [StateRestorationTester] emulates
 * that relaunch: the saved state (the nav back stack, the pager page, the settings menu's tab) is restored into a fresh composition.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TabRestoreTest {

    @get:Rule
    val compose = createComposeRule()

    private fun uiState() = OpenControlUiState(
        connectionState = ConnectionState.Ready,
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
    )

    private val noActions = OpenControlActions(
        onRequestEnableBluetooth = {}, onPair = {}, onRequestPermissions = {}, onOpenAppSettings = {}, onConnect = {}, onDisconnect = {},
        onAncModeSelected = {}, onRefreshAncMode = {}, onRequestAddAncTile = {}, onEqGainsChanged = {}, onEqPresetSelected = {}, onRefreshEq = {},
        onRing = {}, onStopRinging = {}, onRefreshBattery = {}, onDebugModeChanged = {}, onExportLog = {},
    )

    /** Each bottom tab's label and a text only that tab shows ("Noise control" is also a press-and-hold chip on Controls). */
    private val tabs = listOf(
        "Connection" to "Battery",
        "ANC" to "Add ANC Quick Settings tile",
        "Sound" to "Equalizer",
        "Controls" to "Touch controls",
        "Find" to "Find My Buds",
    )

    /** The bottom-bar item (a `Tab`-role node) with this label — not the card title or any other text. */
    private fun bottomItem(label: String) = compose.onNode(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))

    private fun restoreOn(tabIndex: Int) {
        val restorer = StateRestorationTester(compose)
        restorer.setContent { OpenControlTheme(darkTheme = false) { OpenControlNavHost(uiState(), noActions) } }
        val (label, title) = tabs[tabIndex]
        bottomItem(label).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(title).assertExists()

        restorer.emulateSavedInstanceStateRestore()
        compose.waitForIdle()

        compose.onNodeWithText(title).assertExists()
        bottomItem(label).assertIsSelected()
        tabs.filter { it.first != label }.forEach { (other, otherTitle) ->
            bottomItem(other).assertIsNotSelected()
            compose.onNodeWithText(otherTitle).assertDoesNotExist()
        }
    }

    @Test
    fun `Connection is kept across a configuration change`() = restoreOn(0)

    @Test
    fun `ANC is kept across a configuration change`() = restoreOn(1)

    @Test
    fun `Sound is kept across a configuration change (CAP-066 K4r)`() = restoreOn(2)

    @Test
    fun `Controls is kept across a configuration change`() = restoreOn(3)

    @Test
    fun `Find is kept across a configuration change`() = restoreOn(4)

    @Test
    fun `the settings menu and its Info tab are kept across a configuration change, back still returns to the tab it came from`() {
        val restorer = StateRestorationTester(compose)
        restorer.setContent { OpenControlTheme(darkTheme = false) { OpenControlNavHost(uiState(), noActions) } }
        bottomItem("Sound").performClick()
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.onNodeWithText("Info").performClick()
        compose.onNodeWithText("This app").assertExists()

        restorer.emulateSavedInstanceStateRestore()
        compose.waitForIdle()

        compose.onNodeWithText("This app").assertExists() // still the Info tab of the menu
        compose.onNodeWithText("Equalizer").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Equalizer").assertExists() // back returns to Sound, as before the relaunch
        bottomItem("Sound").assertIsSelected()
    }
}
