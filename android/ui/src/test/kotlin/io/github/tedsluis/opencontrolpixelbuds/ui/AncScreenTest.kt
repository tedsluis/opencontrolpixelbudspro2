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
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ChannelTaken
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
        channelTaken: ChannelTaken? = null,
        onReclaim: () -> Unit = {},
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
                channelTaken = channelTaken,
                onReclaimAncChannel = onReclaim,
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
    fun `ADR-061 - the (i) ends with the hold sentence, the maintainer's text (ADR-062 Update 2), and Find keeps the brief-claim hint`() {
        assertEquals(
            // ADR-062 Update 2 (the maintainer's text in chat 2026-10-13, "ANC (i)"): the card's new name; no "cannot use it meanwhile" (CAP-074).
            "While this tab is open the app keeps the Buds' noise-control channel, so a press-and-hold on a bud shows here; it is released 1.5 seconds " +
                "after you leave the tab. On a phone with Google Play services, Play's Fast Pair takes the channel back about 5 seconds later; what this app " +
                "does then is set on the Settings tab under Noise control with Google Play services.",
            ANC_HOLD_HINT_TEXT,
        )
        assertEquals(false, ANC_HOLD_HINT_TEXT.contains("cannot use it meanwhile"))
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

    // ---- DECISIONS.md ADR-062 Update 2 (`ai-sessions/0089`, the maintainer's texts in chat 2026-10-13): the channel-taken hint and its action ----

    @Test
    fun `ADR-062 - the hint-only line is the maintainer's text with the close's time, on the main surface and in the (i), before the Connection line`() {
        val taken = ChannelTaken(at + 5_133, reclaimAttempts = 0) // CAP-074: the cut 5.133 s after the SABM
        val time = formatUpdatedAt(at + 5_133)
        val expected = "Another app took the noise-control channel at $time — usually Google Play services' Fast Pair. A press-and-hold on a bud is not " +
            "shown until this app has the channel again: tap Take it back again, a mode, Refresh, or pull down."
        assertEquals(expected, ancChannelTakenLine(taken))
        show(channelTaken = taken)
        compose.onNodeWithText(expected).assertExists() // the main surface
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onAllNodesWithText(expected).assertCountEquals(2) // and the (i)
        val lines = ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null, channelTaken = taken)
        assertEquals(listOf(expected, "Connection: Ready", ANC_HOLD_HINT_TEXT), lines.takeLast(3))
    }

    @Test
    fun `ADR-062 - after the cap the line says how often the app took the channel back, and no hint means no line`() {
        val time = formatUpdatedAt(at)
        assertEquals(
            "Another app took the noise-control channel at $time — usually Google Play services' Fast Pair; this app took it back 20 times and stopped. " +
                "A press-and-hold on a bud is not shown until this app has the channel again: tap Take it back again, a mode, Refresh, or pull down.",
            ancChannelTakenLine(ChannelTaken(at, reclaimAttempts = 20)),
        )
        assertEquals(
            "Another app took the noise-control channel at $time — usually Google Play services' Fast Pair; this app took it back once and stopped. " +
                "A press-and-hold on a bud is not shown until this app has the channel again: tap Take it back again, a mode, Refresh, or pull down.",
            ancChannelTakenLine(ChannelTaken(at, reclaimAttempts = 1)),
        )
        show()
        compose.onNodeWithText("Another app took the noise-control channel", substring = true).assertDoesNotExist()
    }

    @Test
    fun `ADR-062 Update 2 - Take it back again is shown only with the hint, and a tap hands it over`() {
        assertEquals("Take it back again", TAKE_IT_BACK_AGAIN_LABEL)
        var taps = 0
        show(channelTaken = ChannelTaken(at, reclaimAttempts = 20), onReclaim = { taps++ })
        compose.onNodeWithText("Take it back again").assertIsEnabled().performClick()
        assertEquals(1, taps)
    }

    @Test
    fun `ADR-062 Update 2 - no hint, no Take it back again`() {
        show()
        compose.onNodeWithText("Take it back again").assertDoesNotExist()
    }

    @Test
    fun `ADR-062 Update 2 - Take it back again is disabled while the session is not ready`() {
        show(connectionState = ConnectionState.Disconnected, channelTaken = ChannelTaken(at, reclaimAttempts = 0))
        compose.onNodeWithText("Take it back again").assertIsNotEnabled()
    }

    @Test
    fun `ADR-062 Update 2 - a mode the app's own Get found changed carries the changed-since-last-read line (CAP-075 2980)`() {
        val time = formatUpdatedAt(at)
        val expected = "Changed since the app last saw it (read at $time): a press-and-hold on a bud or the Buds' own change, made while this app did not " +
            "have the channel."
        assertEquals(expected, ancChangedSinceLastReadLine(at))
        assertEquals(
            "Changed since the app last saw it: a press-and-hold on a bud or the Buds' own change, made while this app did not have the channel.",
            ancChangedSinceLastReadLine(null),
        )
        show(cause = AncModeCause.CHANGED_SINCE_LAST_READ)
        compose.onNodeWithContentDescription("Noise control: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText(expected).assertExists()
        compose.onNodeWithText("Changed by the Buds", substring = true).assertDoesNotExist()
        assertEquals(
            listOf("ANC mode: OFF (updated $time)", expected, "Connection: Ready", ANC_HOLD_HINT_TEXT),
            ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null, cause = AncModeCause.CHANGED_SINCE_LAST_READ),
        )
    }

    @Test
    fun `ADR-062 Update 2 - a reading, the app's own Set and the Buds' change carry no changed-since-last-read line`() {
        for (cause in listOf(AncModeCause.READ, AncModeCause.SET_BY_APP, AncModeCause.CHANGED_BY_BUDS, null)) {
            val lines = ancDetailLines(ConnectionState.Ready, AncMode.OFF, at, notAllowed = false, checkedAt = null, cause = cause)
            assertEquals("$cause", false, lines.any { it.startsWith("Changed since the app last saw it") })
        }
    }
}
