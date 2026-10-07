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

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The Controls tab (`ai-sessions/0069`; it had no UI test — `ai-sessions/0068` A68-APP-06): what each card's (i) says while connected, before a setting is
 * read, and — A68-APP-02 — while nothing is connected and the card still shows the last connection's values.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ControlsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val at = 1_727_600_000_000L

    private val read = BudsSettings(
        touchControls = SettingReading(true, at),
        holdLeft = SettingReading(HoldAction.ASSISTANT, at),
        holdRight = SettingReading(HoldAction.ASSISTANT, at),
        inEarDetection = SettingReading(true, at),
        multipoint = SettingReading(true, at),
        headGestures = SettingReading(false, at),
        caseSoundEarbudsReplaced = SettingReading(true, at),
        caseSoundOtherAlerts = SettingReading(false, at, changedByApp = true),
    )

    /** The switch taps the screen reported, as "<setting>=<value>" (`ai-sessions/0074`). */
    private val taps = mutableListOf<String>()

    private fun show(connectionState: ConnectionState, settings: BudsSettings) = compose.setContent {
        OpenControlTheme(darkTheme = false) {
            ControlsScreen(
                connectionState = connectionState,
                settings = settings,
                settingsError = null,
                onTouchControlsChanged = {},
                onPressAndHoldChanged = { _, _ -> },
                onAncModeSelectedChanged = { _, _ -> },
                onInEarDetectionChanged = {},
                onMultipointChanged = { taps += "multipoint=$it" },
                onHeadGesturesChanged = { taps += "headGestures=$it" },
                onCaseSoundEarbudsReplacedChanged = { taps += "earbudsReplaced=$it" },
                onCaseSoundOtherAlertsChanged = { taps += "otherAlerts=$it" },
            )
        }
    }

    @Test
    fun `connected and read - every card has a plain (i) with the read time`() {
        show(ConnectionState.Ready, read)
        for (title in listOf("Touch controls", "Press and hold", "Head gestures", "In-ear detection", "Multipoint", "Case sounds")) {
            compose.onNodeWithContentDescription("$title: $DETAILS_DESCRIPTION").assertExists()
        }
        compose.onNodeWithContentDescription("Touch controls: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText("Use touch controls: read ${formatUpdatedAt(at)}").assertExists()
    }

    @Test
    fun `ai-sessions 0074 - the Multipoint card - label only, its time in the (i), a tap asks for the other value`() {
        show(ConnectionState.Ready, read.copy(multipoint = SettingReading(true, at, changedByApp = true)))
        compose.onNodeWithContentDescription("Multipoint: $DETAILS_DESCRIPTION").performScrollTo().performClick()
        compose.onNodeWithText("Multipoint: changed ${formatUpdatedAt(at)}").assertExists()
        compose.onNodeWithText("Close").performClick()
        compose.onNode(isToggleable() and hasAnySibling(hasText(MULTIPOINT_LABEL))).performScrollTo().assertIsOn().performClick()
        assertEquals("one call with the other value; the switch itself follows only the Buds' answer", listOf("multipoint=false"), taps)
    }

    @Test
    fun `ai-sessions 0074 - the Head gestures card - Use head gestures, off read, a tap asks for on`() {
        show(ConnectionState.Ready, read)
        compose.onNodeWithContentDescription("Head gestures: $DETAILS_DESCRIPTION").performScrollTo().performClick()
        compose.onNodeWithText("Use head gestures: read ${formatUpdatedAt(at)}").assertExists()
        compose.onNodeWithText("Close").performClick()
        compose.onNode(isToggleable() and hasAnySibling(hasText(HEAD_GESTURES_LABEL))).performScrollTo().assertIsOff().performClick()
        assertEquals(listOf("headGestures=true"), taps)
    }

    @Test
    fun `ai-sessions 0074 - the Case sounds card - two switches, each its own time line and its own callback`() {
        show(ConnectionState.Ready, read)
        compose.onNodeWithContentDescription("Case sounds: $DETAILS_DESCRIPTION").performScrollTo().performClick()
        compose.onNodeWithText("Earbuds replaced: read ${formatUpdatedAt(at)}").assertExists()
        compose.onNodeWithText("Other alerts: changed ${formatUpdatedAt(at)}").assertExists()
        compose.onNodeWithText("Close").performClick()
        // Both switches share the card with both labels (siblings), so they are taken in layout order: Earbuds replaced first, then Other alerts.
        val switches = compose.onAllNodes(isToggleable() and hasAnySibling(hasText(CASE_SOUND_EARBUDS_REPLACED_LABEL)) and hasAnySibling(hasText(CASE_SOUND_OTHER_ALERTS_LABEL)))
        switches.assertCountEquals(2)
        switches[0].performScrollTo().assertIsOn().performClick()
        switches[1].performScrollTo().assertIsOff().performClick()
        assertEquals(listOf("earbudsReplaced=false", "otherAlerts=true"), taps)
    }

    @Test
    fun `connected and not read yet - the (i) is marked and says not read`() {
        show(ConnectionState.Ready, BudsSettings())
        compose.onNodeWithContentDescription("In-ear detection: $DETAILS_NOT_CURRENT_DESCRIPTION").assertExists()
        compose.onNodeWithContentDescription("Touch controls: $DETAILS_NOT_CURRENT_DESCRIPTION").performClick() // the first card: on screen in the test window
        compose.onNodeWithText("Use touch controls: Not read from the Buds yet").assertExists()
    }

    @Test
    fun `not connected - the last connection's values are marked and named, and nothing can be changed`() {
        show(ConnectionState.Disconnected, read)
        for (title in listOf("Touch controls", "Press and hold", "Head gestures", "In-ear detection", "Multipoint", "Case sounds")) {
            compose.onNodeWithContentDescription("$title: $DETAILS_NOT_CURRENT_DESCRIPTION").assertExists()
        }
        compose.onNodeWithContentDescription("Touch controls: $DETAILS_NOT_CURRENT_DESCRIPTION").performClick()
        compose.onNodeWithText("From the last connection — the app is not connected to the Buds now.").assertExists()
        compose.onNodeWithText("Use touch controls: read ${formatUpdatedAt(at)}").assertExists()
    }

    // ---- `ai-sessions/0074`: "—" in place of an unread switch, read by a screen reader as "Not read from the Buds yet" ----

    private val notReadDash = hasText(NOT_READ_VALUE) and
        SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(NOT_READ_DESCRIPTION))

    @Test
    fun `connected and nothing read - every switch is a dash with the screen-reader text, no switch is shown`() {
        show(ConnectionState.Ready, BudsSettings())
        // Use touch controls, Use head gestures, In-ear detection, Multipoint, Earbuds replaced, Other alerts.
        compose.onAllNodes(notReadDash).assertCountEquals(6)
        compose.onAllNodesWithText(NOT_READ_VALUE).assertCountEquals(6)
        compose.onAllNodes(isToggleable()).assertCountEquals(0)
    }

    @Test
    fun `one unread switch - only that one is a dash, the others are switches`() {
        show(ConnectionState.Ready, read.copy(multipoint = null))
        compose.onNode(notReadDash and hasAnySibling(hasText(MULTIPOINT_LABEL))).performScrollTo().assertExists()
        compose.onAllNodes(notReadDash).assertCountEquals(1)
        compose.onAllNodes(isToggleable() and hasAnySibling(hasText(MULTIPOINT_LABEL))).assertCountEquals(0)
    }

    @Test
    fun `not connected with the last connection's values - switches stay switches, no dash`() {
        show(ConnectionState.Disconnected, read)
        compose.onAllNodes(notReadDash).assertCountEquals(0)
        compose.onNode(isToggleable() and hasAnySibling(hasText(MULTIPOINT_LABEL))).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun `not connected and never read - nothing is marked as from the last connection`() {
        show(ConnectionState.Disconnected, BudsSettings())
        compose.onNodeWithContentDescription("Touch controls: $DETAILS_DESCRIPTION").assertExists()
    }
}
