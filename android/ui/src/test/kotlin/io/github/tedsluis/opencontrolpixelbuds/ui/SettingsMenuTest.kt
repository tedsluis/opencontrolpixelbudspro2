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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.ComponentSerial
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
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

    /**
     * `ai-sessions/0078` (the maintainer's choice in chat 2026-10-07): the order of the three tabs was asserted nowhere — a mutation that swapped Settings and
     * Debug in [SettingsTab] survived the whole `:ui` suite (M7). The tabs are found by label, so their order is checked by their position on screen.
     */
    @Test
    fun `the tabs are Settings, Debug, Info from left to right, and Settings is selected when the menu opens`() {
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo("0.1.0-dev", "d541e00", "2026-10-01"), announced)
            }
        }
        val lefts = listOf("Settings", "Debug", "Info").map { label -> compose.onNode(hasText(label) and hasClickAction()).getBoundsInRoot().left }
        assertTrue("tab positions from the left: $lefts", lefts[0] < lefts[1] && lefts[1] < lefts[2])
        compose.onNode(hasText("Settings") and hasClickAction()).assertIsSelected()
        compose.onNodeWithText("Dark mode").assertExists()
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

    // ---- ai-sessions/0064 F-6 / 0066: licence (in the app), README and issues (DECISIONS.md ADR-050 and its Update) -------------------------------------------------------------------

    @Test
    fun `Info shows the licence line and the two links, each tap hands exactly its URL to the opener, no LICENSE link`() {
        val opened = mutableListOf<String>()
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, announced, onOpenUrl = { opened += it })
            }
        }
        compose.onNodeWithText("Info").performClick()
        compose.onNodeWithText("GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)").assertExists()
        compose.onNodeWithText("Links open in your browser; this app itself has no internet access.").assertExists()
        compose.onNodeWithText( // ai-sessions/0065, ADR-051: the maintainer's exact wording
            "Works with Google Pixel Buds Pro 2. Not affiliated with or endorsed by Google. Pixel Buds is a trademark of Google LLC.",
        ).assertExists()

        // The Robolectric screen is small (320 × 470 px): scroll each link into view before the tap.
        compose.onNodeWithText("Licence on GitHub").assertDoesNotExist() // ai-sessions/0066: the licence is read in the app only
        compose.onNodeWithText("README on GitHub").performScrollTo().performClick()
        compose.onNodeWithText("Report an issue on GitHub").performScrollTo().performClick()
        assertEquals(
            listOf(
                "https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/README.md",
                "https://github.com/tedsluis/opencontrolpixelbudspro2/issues",
            ),
            opened,
        )
        compose.onNodeWithText("Case: release_5.203").assertExists() // the Buds' part stays below
    }

    @Test
    fun `Read the licence shows the bundled text offline, nothing is opened, Close closes it`() {
        val opened = mutableListOf<String>()
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, null, onOpenUrl = { opened += it })
            }
        }
        compose.onNodeWithText("Info").performClick()
        compose.onNodeWithText("Read the licence").performScrollTo().performClick()
        compose.onNodeWithText("GNU AFFERO GENERAL PUBLIC LICENSE", substring = true).assertExists()
        assertTrue("no link opened", opened.isEmpty())
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText("GNU AFFERO GENERAL PUBLIC LICENSE", substring = true).assertDoesNotExist()
    }

    @Test
    fun `the bundled licence is byte-identical to the repository's LICENSE`() {
        // The unit tests run in the module directory (android/ui); the repository's LICENSE is two levels up. A changed LICENSE fails this until the copy is
        // updated (cp LICENSE android/ui/src/main/res/raw/license.txt).
        val repo = java.io.File("../../LICENSE").readBytes()
        val bundled = java.io.File("src/main/res/raw/license.txt").readBytes()
        assertTrue("LICENSE found (${repo.size} bytes)", repo.size > 30_000)
        assertTrue("res/raw/license.txt == LICENSE", repo.contentEquals(bundled))
    }

    // ---- ADR-058 (`ai-sessions/0082` item 2): the serial numbers on the Info tab, labelled by position as the official app does ----

    private val withSerials = announced.copy(
        serials = listOf(ComponentSerial(1, "5707XXXXXXXX51"), ComponentSerial(2, "5708XXXXXXXX09"), ComponentSerial(3, "5707XXXXXXXX47")),
        serialsReadAtMillis = at + 83,
    )

    @Test
    fun `Info shows the serial numbers under the firmware, labelled Case, Right bud, Left bud, with the note and the time`() {
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, withSerials)
            }
        }
        compose.onNodeWithText("Info").performClick()
        compose.onNodeWithText("Serial numbers (from the Buds, ${formatUpdatedAt(at + 83)}):").performScrollTo().assertExists()
        compose.onNodeWithText("Case: 5707XXXXXXXX51").performScrollTo().assertExists()
        compose.onNodeWithText("Right bud: 5708XXXXXXXX09").performScrollTo().assertExists()
        compose.onNodeWithText("Left bud: 5707XXXXXXXX47").performScrollTo().assertExists()
        compose.onNodeWithText(SERIALS_NOTE).performScrollTo().assertExists()
        // The firmware block (its own order: Case, Left, Right) is still above it.
        compose.onNodeWithText("Left bud: release_5.203").assertExists()
    }

    @Test
    fun `Info says not read before the answer, the reason after a failed read, and nothing without an announcement`() {
        assertEquals(listOf("Serial numbers: $SETTING_NOT_READ"), serialInfoLines(announced, null))
        assertEquals(listOf("Serial numbers: not read — The Buds didn't respond in time."), serialInfoLines(announced, BudsError.Timeout))
        assertEquals(
            listOf("Serial numbers: not read — The Buds answered with a value this app cannot read."),
            serialInfoLines(announced, BudsError.UnreadableAnswer),
        )
        assertEquals(emptyList<String>(), serialInfoLines(null, BudsError.Timeout))
        // The labels follow the field number, in index order, never guessed for an index the Buds never used.
        assertEquals(listOf("Case", "Right bud", "Left bud", "Part 4"), listOf(1, 2, 3, 4).map(::serialComponentLabel))
        val lines = serialInfoLines(withSerials.copy(serials = listOf(ComponentSerial(3, "c"), ComponentSerial(1, "a"))), null)
        assertEquals(listOf("Case: a", "Left bud: c"), lines.drop(1).dropLast(1))
    }

    @Test
    fun `the Info tab renders the failed-read line`() {
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, announced, serialsError = BudsError.Timeout)
            }
        }
        compose.onNodeWithText("Info").performClick()
        compose.onNodeWithText("Serial numbers: not read — The Buds didn't respond in time.").performScrollTo().assertExists()
    }

    // ---- `ai-sessions/0082` item 5: the Case sounds card on the Settings tab (ADR-054), between Dark mode and Use different Buds ----

    private val caseSoundsRead = BudsSettings(
        caseSoundEarbudsReplaced = SettingReading(true, at),
        caseSoundOtherAlerts = SettingReading(false, at, changedByApp = true),
    )

    @Test
    fun `the Settings tab shows the Case sounds card between Dark mode and Use different Buds, with its (i) lines and the case note`() {
        val taps = mutableListOf<String>()
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(
                    DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, announced,
                    connectionState = ConnectionState.Ready, settings = caseSoundsRead,
                    onCaseSoundEarbudsReplacedChanged = { taps += "earbudsReplaced=$it" }, onCaseSoundOtherAlertsChanged = { taps += "otherAlerts=$it" },
                )
            }
        }
        val darkMode = compose.onNodeWithText("Dark mode").getBoundsInRoot().top
        val caseSounds = compose.onNodeWithContentDescription("Case sounds: $DETAILS_DESCRIPTION").performScrollTo().getBoundsInRoot().top
        val useDifferent = compose.onNode(hasText(USE_DIFFERENT_BUDS_TITLE) and hasClickAction()).performScrollTo().getBoundsInRoot().top
        assertTrue("Dark mode < Case sounds < Use different Buds: $darkMode, $caseSounds, $useDifferent", darkMode < caseSounds && caseSounds < useDifferent)

        compose.onNodeWithContentDescription("Case sounds: $DETAILS_DESCRIPTION").performScrollTo().performClick()
        compose.onNodeWithText("Earbuds replaced: read ${formatUpdatedAt(at)}").assertExists()
        compose.onNodeWithText("Other alerts: changed ${formatUpdatedAt(at)}").assertExists()
        compose.onNodeWithText("These settings live on the case and are read when the app connects.").assertExists()
        compose.onNodeWithText("Close").performClick()
        // Both switches share the card with both labels (siblings): layout order, Earbuds replaced first, then Other alerts.
        val switches = compose.onAllNodes(isToggleable() and hasAnySibling(hasText(CASE_SOUND_EARBUDS_REPLACED_LABEL)) and hasAnySibling(hasText(CASE_SOUND_OTHER_ALERTS_LABEL)))
        switches.assertCountEquals(2)
        switches[0].performScrollTo().assertIsOn().performClick()
        switches[1].performScrollTo().assertIsOff().performClick()
        assertEquals(listOf("earbudsReplaced=false", "otherAlerts=true"), taps)
    }

    @Test
    fun `the Case sounds card not connected keeps the last values dimmed and marked, and unread values are dashes`() {
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, null, connectionState = ConnectionState.Disconnected, settings = caseSoundsRead)
            }
        }
        compose.onNodeWithContentDescription("Case sounds: $DETAILS_NOT_CURRENT_DESCRIPTION").performScrollTo().performClick()
        compose.onNodeWithText(FROM_LAST_CONNECTION_DETAIL).assertExists()
        compose.onNodeWithText("Close").performClick()
        compose.onAllNodes(isToggleable() and hasAnySibling(hasText(CASE_SOUND_EARBUDS_REPLACED_LABEL))).assertCountEquals(2)
    }

    @Test
    fun `the Case sounds card connected and unread shows two dashes with the screen-reader text`() {
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, announced, connectionState = ConnectionState.Ready)
            }
        }
        compose.onNodeWithContentDescription("Case sounds: $DETAILS_NOT_CURRENT_DESCRIPTION").performScrollTo().assertExists()
        val dash = hasText(NOT_READ_VALUE) and SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(NOT_READ_DESCRIPTION))
        compose.onAllNodes(dash).assertCountEquals(2)
        compose.onAllNodes(isToggleable()).assertCountEquals(0)
    }

    // ---- `ai-sessions/0069` A68-APP-05: "Use different Buds" on the Settings tab ----

    @Test
    fun `the Settings tab offers Use different Buds with what it does and what it leaves alone`() {
        var taps = 0
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, null, onUseDifferentBuds = { taps++ })
            }
        }
        compose.onNodeWithText(
            "Forgets which Buds this app controls (the Bluetooth pairing in Android stays). You then pick the Buds again with Pair a device.",
        ).assertExists()
        compose.onNode(hasText("Use different Buds") and hasClickAction()).performScrollTo().performClick()
        assertEquals(1, taps)
    }
}
