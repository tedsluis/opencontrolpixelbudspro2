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

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.RingNotice
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The Find tab (`ai-sessions/0069`; it had no UI test — `ai-sessions/0068` A68-APP-06): what each button calls and what the ring notice says. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FindMyBudsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun show(connectionState: ConnectionState, ringing: RingNotice?, stoppedOnBudAt: Long? = null): MutableList<String> {
        val calls = mutableListOf<String>()
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                FindMyBudsScreen(
                    connectionState, null, ringing, onRing = { calls += "ring $it" }, onStop = { calls += "stop" }, ringStoppedOnBudAt = stoppedOnBudAt,
                )
            }
        }
        return calls
    }

    @Test
    fun `connected - each button calls its own action and nothing else`() {
        val calls = show(ConnectionState.Ready, null)
        compose.onNodeWithText("Ring Left").performClick()
        compose.onNodeWithText("Ring Right").performClick()
        compose.onNodeWithText("Ring both").performClick()
        compose.onNodeWithText("Stop").performClick()
        assertEquals(listOf("ring LEFT", "ring RIGHT", "ring BOTH", "stop"), calls)
    }

    @Test
    fun `not connected - the buttons are off and a running ring says how to end it`() {
        show(ConnectionState.Disconnected, RingNotice(RingTarget.LEFT))
        for (label in listOf("Ring Left", "Ring Right", "Ring both", "Stop")) compose.onNodeWithText(label).assertIsNotEnabled()
        compose.onNodeWithText("A ring was started on the Left earbud — reconnect and tap Stop to end it.").assertExists()
    }

    @Test
    fun `the ring notice says what the app knows`() {
        assertEquals("Ringing: Right earbud — tap Stop to end it.", ringingNoticeText(RingNotice(RingTarget.RIGHT), sessionReady = true))
        assertEquals(
            "A ring was started on the Left earbud before the app reconnected — it may still be ringing. Tap Stop to end it.",
            ringingNoticeText(RingNotice(RingTarget.LEFT, fromEarlierSession = true), sessionReady = true),
        )
        show(ConnectionState.Ready, RingNotice(RingTarget.RIGHT))
        compose.onNodeWithText("Ringing: Right earbud — tap Stop to end it.").assertExists()
        compose.onNodeWithText("Stop").assertIsEnabled()
    }

    @Test
    fun `ADR-070 - one warning caption above the rings, and Ring both has its own notice`() {
        show(ConnectionState.Ready, RingNotice(RingTarget.BOTH))
        compose.onNodeWithText("Take the Buds out of your ears before ringing — the sound is loud.").assertExists()
        compose.onNodeWithText(ringingNoticeText(RingNotice(RingTarget.BOTH), sessionReady = true)).assertExists()
        assertEquals(true, ringingNoticeText(RingNotice(RingTarget.BOTH), sessionReady = true).contains("both earbuds", ignoreCase = true))
    }

    @Test
    fun `the (i) says a ring holds the channel, literally (ai-sessions 0100)`() {
        show(ConnectionState.Ready, null)
        compose.onNodeWithContentDescription("Find My Buds: $DETAILS_DESCRIPTION").performClick()
        compose.onNodeWithText(
            "Ring and Stop use the Buds' Message Stream channel. While a bud rings the app keeps that channel (up to about a minute) to hear when " +
                "the ringing stops. Another app that uses it (for example Google Play services' Fast Pair) may take it back; the app then cannot see the stop.",
        ).assertExists()
    }

    @Test
    fun `ADR-069 - a stop on the bud is said with its time`() {
        val at = 1_727_600_000_000L
        show(ConnectionState.Ready, null, stoppedOnBudAt = at)
        compose.onNodeWithText(ringStoppedOnBudText(at)).assertExists()
        assertEquals(true, ringStoppedOnBudText(at).startsWith("Stopped on the bud at "))
    }
}
