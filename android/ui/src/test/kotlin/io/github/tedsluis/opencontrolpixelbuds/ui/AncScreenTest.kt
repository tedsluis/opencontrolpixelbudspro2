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

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeCause
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList
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
        ancError: BudsError? = null,
        ancModeList: AncModeList? = null,
        unconfirmedAt: Long? = null,
        connectionState: ConnectionState = ConnectionState.Ready,
        sessionSince: Long? = at - 1_000, // the connection opened a second before the Buds reported the mode
        cause: AncModeCause? = null,
    ) = compose.setContent {
        OpenControlTheme(darkTheme = false) {
            AncScreen(
                connectionState = connectionState,
                ancError = ancError,
                ancModeList = ancModeList,
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
            "The Buds don't allow changing noise control right now (usually because no bud is in an ear). A tap still sends the mode; the Buds answer whether they accept it.",
        ).assertExists()
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText(ancNotAllowedLine(at)).assertExists()
    }

    @Test
    fun `the checked time sits inside the parenthesis of the (i) line`() {
        val line = ancNotAllowedLine(at)
        val time = formatUpdatedAt(at)!!
        assertEquals(
            "The Buds don't allow changing noise control right now (usually because no bud is in an ear; checked $time). A tap still sends the mode; the Buds answer whether they accept it.",
            line,
        )
    }

    @Test
    fun `an answer cut off is said as such, never as no response`() {
        show(ancError = BudsError.AnswerCutOff(0x04, "IOException: bt socket closed, read return: -1"))
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
        compose.onNodeWithText("ANC mode: Off — from the last connection (updated ${formatUpdatedAt(at)})").assertExists()
    }

    @Test
    fun `after Disconnect the last mode stays, marked as from the last connection`() {
        show(connectionState = ConnectionState.Disconnected)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_NOT_CURRENT_DESCRIPTION").performClick()
        compose.onNodeWithText("ANC mode: Off — from the last connection (updated ${formatUpdatedAt(at)})").assertExists()
    }

    // ---- `ai-sessions/0082` item 1: "Changed by the Buds" in the (i), only for a Notify the app did not provoke ----

    @Test
    fun `a mode the Buds changed says so in the (i), with the mode's time`() {
        show(cause = AncModeCause.CHANGED_BY_BUDS)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick() // the (i) is plain: the mode is current
        compose.onNodeWithText("Changed by the Buds at ${formatUpdatedAt(at)}: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves.").assertExists()
        compose.onNodeWithText("ANC mode: Off (updated ${formatUpdatedAt(at)})").assertExists()
    }

    @Test
    fun `a mode read from the Buds has no changed-by-the-Buds line`() {
        show(cause = AncModeCause.READ)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText("ANC mode: Off (updated ${formatUpdatedAt(at)})").assertExists()
        compose.onNodeWithText("Changed by the Buds", substring = true).assertDoesNotExist()
    }

    @Test
    fun `a mode set by this app has no changed-by-the-Buds line`() {
        show(cause = AncModeCause.SET_BY_APP)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText("ANC mode: Off (updated ${formatUpdatedAt(at)})").assertExists()
        compose.onNodeWithText("Changed by the Buds", substring = true).assertDoesNotExist()
    }

    @Test
    fun `the changed-by-the-Buds line leaves the time out when there is none, and needs a mode`() {
        assertEquals("Changed by the Buds: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves.", ancChangedByBudsLine(null))
        assertEquals(
            listOf("ANC mode: unknown", "Connection: connected", ANC_STREAM_HINT_TEXT),
            ancDetailLines(ConnectionState.Ready, null, null, notAllowed = false, checkedAt = null, cause = AncModeCause.CHANGED_BY_BUDS),
        )
    }

    @Test
    fun `ADR-065 - the (i) ends with the stream sentence, the maintainer's text (ai-sessions 0091) hedged as a hypothesis, and Find keeps the brief-claim hint`() {
        assertEquals(
            // The maintainer's text in chat 2026-10-14 ("ANC (i)"): the wear-change part is 🟡 (PROTOCOL.md §6) and says so — never a fact in the UI.
            "The Buds report every change of the mode on the app's own channel. A press-and-hold on a bud is one cause; on film the Buds also switched the " +
                "mode by themselves when a bud was put in or taken out — that part is a hypothesis.",
            ANC_STREAM_HINT_TEXT,
        )
        assertEquals(true, ANC_STREAM_HINT_TEXT.endsWith("that part is a hypothesis."))
        assertEquals(ANC_STREAM_HINT_TEXT, ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null).last())
        assertEquals(false, ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null).contains(MESSAGE_STREAM_HINT_TEXT))
        show(cause = AncModeCause.READ)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText(ANC_STREAM_HINT_TEXT).assertExists()
        // ADR-061/062/063 are gone (ADR-065 item 5): no hold sentence, no channel-taken line, no "Take it back again", no Play line.
        compose.onNodeWithText("keeps the Buds' noise-control channel", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Another app took the noise-control channel", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Take it back again").assertDoesNotExist()
        compose.onNodeWithText("Google Play services", substring = true).assertDoesNotExist()
    }

    @Test
    fun `the mode line of a current mode is unchanged`() {
        assertEquals("ANC mode: Off (updated ${formatUpdatedAt(at)})", ancModeLine(AncMode.OFF, at))
        assertEquals("ANC mode: unknown", ancModeLine(null, null, fromLastConnection = true))
    }

    // ---- DECISIONS.md ADR-065 (`ai-sessions/0091`, the maintainer's texts in chat 2026-10-14, "Cause lines"): the two cause lines ----

    @Test
    fun `ADR-065 - a mode the Buds pushed says so in the (i) with the new wording, the mode's time, and the two triggers`() {
        val time = formatUpdatedAt(at)
        val expected = "Changed by the Buds at $time: the Buds reported a new mode — a press-and-hold on a bud, or a change they made themselves."
        assertEquals(expected, ancChangedByBudsLine(at))
        show(cause = AncModeCause.CHANGED_BY_BUDS)
        compose.onNodeWithText("Changed by the Buds", substring = true).assertDoesNotExist() // nothing on the main surface
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText(expected).assertExists()
        assertEquals(
            listOf("ANC mode: Off (updated $time)", expected, "Connection: connected", ANC_STREAM_HINT_TEXT),
            ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null, cause = AncModeCause.CHANGED_BY_BUDS),
        )
    }

    @Test
    fun `ADR-065 - a mode a read found changed carries the changed-since-last-read line with the new wording (CAP-075 2980, CAP-051 2247)`() {
        val time = formatUpdatedAt(at)
        val expected = "Changed since the app last saw it (read at $time): a press-and-hold on a bud or the Buds' own change, made while this app was not " +
            "connected."
        assertEquals(expected, ancChangedSinceLastReadLine(at))
        assertEquals(
            "Changed since the app last saw it: a press-and-hold on a bud or the Buds' own change, made while this app was not connected.",
            ancChangedSinceLastReadLine(null),
        )
        show(cause = AncModeCause.CHANGED_SINCE_LAST_READ)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText(expected).assertExists()
        compose.onNodeWithText("Changed by the Buds", substring = true).assertDoesNotExist()
        assertEquals(
            listOf("ANC mode: Off (updated $time)", expected, "Connection: connected", ANC_STREAM_HINT_TEXT),
            ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null, cause = AncModeCause.CHANGED_SINCE_LAST_READ),
        )
    }

    @Test
    fun `ADR-065 - a reading, the app's own Set and the Buds' change carry no changed-since-last-read line`() {
        for (cause in listOf(AncModeCause.READ, AncModeCause.SET_BY_APP, AncModeCause.CHANGED_BY_BUDS, null)) {
            val lines = ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null, cause = cause)
            assertEquals("$cause", false, lines.any { it.startsWith("Changed since the app last saw it") })
        }
    }

    @Test
    fun `a mode not ticked for press and hold is disabled and the line says why (ADR-067)`() {
        show(ancModeList = AncModeList(noiseCancellation = true, off = true, transparency = true, adaptive = false))
        compose.onNodeWithText(AncMode.ADAPTIVE.label).assertIsNotEnabled()
        compose.onNodeWithText(AncMode.TRANSPARENT.label).assertIsEnabled()
        compose.onNodeWithText(
            "Only the modes ticked under Controls → \"Modes for press and hold\" can be chosen here. Tick a mode there to use it.",
        ).assertExists()
    }

    @Test
    fun `an unread hold list disables nothing and shows no line`() {
        show(ancModeList = null)
        compose.onNodeWithText(AncMode.ADAPTIVE.label).assertIsEnabled()
        compose.onNodeWithText(ANC_HOLD_LIST_HINT_TEXT).assertDoesNotExist()
    }
}
