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
import androidx.compose.ui.test.junit4.createComposeRule
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

    private fun show(connectionState: ConnectionState, ringing: RingNotice?): MutableList<String> {
        val calls = mutableListOf<String>()
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                FindMyBudsScreen(connectionState, null, ringing, onRing = { calls += "ring $it" }, onStop = { calls += "stop" })
            }
        }
        return calls
    }

    @Test
    fun `connected - each button calls its own action and nothing else`() {
        val calls = show(ConnectionState.Ready, null)
        compose.onNodeWithText("Ring Left").performClick()
        compose.onNodeWithText("Ring Right").performClick()
        compose.onNodeWithText("Stop").performClick()
        assertEquals(listOf("ring LEFT", "ring RIGHT", "stop"), calls)
    }

    @Test
    fun `not connected - the buttons are off and a running ring says how to end it`() {
        show(ConnectionState.Disconnected, RingNotice(RingTarget.LEFT))
        for (label in listOf("Ring Left", "Ring Right", "Stop")) compose.onNodeWithText(label).assertIsNotEnabled()
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
}
