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

    @Test
    fun `the mode line of a current mode is unchanged`() {
        assertEquals("ANC mode: OFF (updated ${formatUpdatedAt(at)})", ancModeLine(AncMode.OFF, at))
        assertEquals("ANC mode: unknown", ancModeLine(null, null, fromLastConnection = true))
    }
}
