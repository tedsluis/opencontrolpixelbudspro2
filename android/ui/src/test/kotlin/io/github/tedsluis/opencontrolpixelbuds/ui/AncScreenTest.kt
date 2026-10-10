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

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeCause
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The ANC tab after `ai-sessions/0062` (F-2 wording, F-3 cut-off), rendered under Robolectric: the not-allowed note in the maintainer's words, the cut-off
 * text instead of "didn't respond in time", and a not-confirmed mode marked by the (i) dot and its line.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AncScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val at = 1_727_600_000_000L

    private fun show(
        availability: AncAvailability = AncAvailability.ALLOWED,
        messageStreamError: BudsError? = null,
        unconfirmedAt: Long? = null,
        connectionState: ConnectionState = ConnectionState.Ready,
        sessionSince: Long? = at - 1_000, // the connection opened a second before the Buds reported the mode
        cause: AncModeCause? = null,
    ) = compose.setContent {
        OpenControlTheme(darkTheme = false) {
            AncScreen(
                connectionState = connectionState,
                messageStreamError = messageStreamError,
                ancMode = AncMode.OFF,
                ancModeUpdatedAt = at,
                ancAvailability = availability,
                onAncModeSelected = {},
                onRefreshAncMode = {},
                onRequestAddAncTile = {},
                ancAvailabilityUpdatedAt = at,
                ancModeUnconfirmedAt = unconfirmedAt,
                sessionSince = sessionSince,
                ancModeCause = cause,
            )
        }
    }

    @Test
    fun `the not-allowed note says what the byte means, the checked time is in the (i)`() {
        show(availability = AncAvailability.NOT_ALLOWED)
        compose.onNodeWithText(
            "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first.",
        ).assertExists()
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText(ancNotAllowedLine(at)).assertExists()
    }

    @Test
    fun `the checked time sits inside the parenthesis of the (i) line`() {
        val line = ancNotAllowedLine(at)
        val time = formatUpdatedAt(at)!!
        assertEquals(
            "The Buds don't allow changing noise control right now (usually because no bud is in an ear; checked $time). Tapping a mode checks again first.",
            line,
        )
    }

    @Test
    fun `an answer cut off is said as such, never as no response`() {
        show(messageStreamError = BudsError.AnswerCutOff(0x04, "IOException: bt socket closed, read return: -1"))
        compose.onNodeWithText(
            "The channel was closed before the Buds' answer arrived (possibly by another app using it). Tap Refresh to see the current mode.",
        ).assertExists()
        compose.onNodeWithText("The Buds didn't respond in time.").assertDoesNotExist()
    }

    @Test
    fun `a not-confirmed mode marks the (i) and says why in it`() {
        show(unconfirmedAt = at)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_NOT_CURRENT_DESCRIPTION").performClick()
        compose.onNodeWithText(ancUnconfirmedLine(at)).assertExists()
        // The literal sentence too (A68-APP-06: the line above compares the helper with itself).
        compose.onNodeWithText(
            "Not confirmed: the answer to the change at ${formatUpdatedAt(at)} was cut off — the Buds may have switched. Tap Refresh.",
        ).assertExists()
    }

    @Test
    fun `a confirmed mode has a plain (i)`() {
        show()
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").assertExists()
    }

    // ---- `ai-sessions/0069` A68-APP-02: a mode from the last connection is kept, marked and named — never shown as the Buds' current mode ----

    @Test
    fun `a mode reported before this connection opened marks the (i) and says so`() {
        show(sessionSince = at + 5_000) // a new Connect five seconds after the last report
        compose.onNodeWithContentDescription("Noise control: $DETAILS_NOT_CURRENT_DESCRIPTION").performClick()
        compose.onNodeWithText("ANC mode: OFF — from the last connection (updated ${formatUpdatedAt(at)})").assertExists()
    }

    @Test
    fun `after Disconnect the last mode stays, marked as from the last connection`() {
        show(connectionState = ConnectionState.Disconnected)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_NOT_CURRENT_DESCRIPTION").performClick()
        compose.onNodeWithText("ANC mode: OFF — from the last connection (updated ${formatUpdatedAt(at)})").assertExists()
    }

    // ---- `ai-sessions/0082` item 1: "Changed by the Buds" in the (i), only for a Notify the app did not provoke ----

    @Test
    fun `a mode the Buds changed says so in the (i), with the mode's time`() {
        show(cause = AncModeCause.CHANGED_BY_BUDS)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick() // the (i) is plain: the mode is current
        compose.onNodeWithText("Changed by the Buds at ${formatUpdatedAt(at)} (a press-and-hold on a bud, or the Buds' own change).").assertExists()
        compose.onNodeWithText("ANC mode: OFF (updated ${formatUpdatedAt(at)})").assertExists()
    }

    @Test
    fun `a mode read from the Buds has no changed-by-the-Buds line`() {
        show(cause = AncModeCause.READ)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText("ANC mode: OFF (updated ${formatUpdatedAt(at)})").assertExists()
        compose.onNodeWithText("Changed by the Buds", substring = true).assertDoesNotExist()
    }

    @Test
    fun `a mode set by this app has no changed-by-the-Buds line`() {
        show(cause = AncModeCause.SET_BY_APP)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText("ANC mode: OFF (updated ${formatUpdatedAt(at)})").assertExists()
        compose.onNodeWithText("Changed by the Buds", substring = true).assertDoesNotExist()
    }

    @Test
    fun `the changed-by-the-Buds line leaves the time out when there is none, and needs a mode`() {
        assertEquals("Changed by the Buds (a press-and-hold on a bud, or the Buds' own change).", ancChangedByBudsLine(null))
        assertEquals(
            listOf("ANC mode: unknown", "Connection: Ready", ANC_HOLD_HINT_TEXT),
            ancDetailLines(ConnectionState.Ready, null, null, notAllowed = false, checkedAt = null, cause = AncModeCause.CHANGED_BY_BUDS),
        )
    }

    @Test
    fun `ADR-061 - the (i) ends with the hold sentence, the maintainer's text, and Find keeps the brief-claim hint`() {
        assertEquals(
            "While this tab is open the app keeps the Buds' noise-control channel open, so a change made on a bud (a press-and-hold) shows here. Another " +
                "app that uses the channel (for example Google Play services' Fast Pair) cannot use it meanwhile; it is released 1.5 seconds after you leave " +
                "the tab.",
            ANC_HOLD_HINT_TEXT,
        )
        assertEquals(ANC_HOLD_HINT_TEXT, ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null).last())
        assertEquals(false, ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null).contains(MESSAGE_STREAM_HINT_TEXT))
        show(cause = AncModeCause.READ)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText(ANC_HOLD_HINT_TEXT).assertExists()
    }

    @Test
    fun `the mode line of a current mode is unchanged`() {
        assertEquals("ANC mode: OFF (updated ${formatUpdatedAt(at)})", ancModeLine(AncMode.OFF, at))
        assertEquals("ANC mode: unknown", ancModeLine(null, null, fromLastConnection = true))
    }
}
