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
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * `ai-sessions/0064`/`0066`: the balance slider writes once per completed drag (test at the end; the `0064` steps were removed again in `0066`).
 *
 * `ai-sessions/0059` (A58-APP-02, the maintainer's choice "Disable until read"): while the EQ has not been read the five band sliders are disabled — a drag
 * would write four bands the Buds never reported — and the presets (full quintets) stay usable.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EqScreenTest {

    @get:Rule
    val compose = createComposeRule()

    /** The five EQ band sliders: the only sliders with the EQ's ±6.0 range (the balance slider's range is different). */
    private val eqSlider = SemanticsMatcher("EQ band slider") {
        it.config.getOrNull(SemanticsProperties.ProgressBarRangeInfo)?.range == EqBandGains.RANGE.start..EqBandGains.RANGE.endInclusive
    }

    private fun show(gains: EqBandGains?, connectionState: ConnectionState = ConnectionState.Ready, updatedAt: Long? = null) = compose.setContent {
        OpenControlTheme(darkTheme = false) {
            EqScreen(
                connectionState = connectionState,
                gains = gains,
                eqProfileUpdatedAt = updatedAt,
                eqError = null,
                onGainsChanged = {},
                onPresetSelected = {},
                onRefresh = {},
            )
        }
    }

    @Test
    fun `an unread EQ disables the five sliders and keeps the presets`() {
        show(gains = null)
        val sliders = compose.onAllNodes(eqSlider)
        sliders.assertCountEquals(5)
        for (i in 0 until 5) sliders[i].assertIsNotEnabled()
        compose.onNodeWithText(EqPreset.entries.first().name.replace('_', ' ')).assertIsEnabled()
    }

    @Test
    fun `a read EQ enables the sliders`() {
        show(gains = EqBandGains.FLAT)
        val sliders = compose.onAllNodes(eqSlider)
        sliders.assertCountEquals(5)
        for (i in 0 until 5) sliders[i].assertIsEnabled()
    }

    // ---- `ai-sessions/0069` A68-APP-08 (visible text only) and A68-APP-16 ----

    @Test
    fun `an unread EQ shows a dash for each band, never 0,0`() {
        show(gains = null)
        // The five bands; the balance card below (a lazy-list item of its own) adds a sixth when it is composed.
        val dashes = compose.onAllNodesWithText("—").fetchSemanticsNodes().size
        assertTrue("dashes shown: $dashes", dashes == 5 || dashes == 6)
        compose.onAllNodesWithText("%.1f".format(0f)).assertCountEquals(0)
    }

    @Test
    fun `an unread balance shows a dash, a read one its value`() {
        showBalance(balance = null)
        compose.onNodeWithText("—").performScrollTo().assertExists()
        compose.onAllNodesWithText("Centre").assertCountEquals(0)
    }

    @Test
    fun `a read EQ shows its numbers`() {
        show(gains = EqBandGains(upperTreble = 5f, treble = 3f, mid = 2f, bass = 0f, lowBass = -2f))
        compose.onNodeWithText("%.1f".format(5f)).assertExists()
        compose.onNodeWithText("%.1f".format(-2f)).assertExists()
        assertTrue(compose.onAllNodesWithText("—").fetchSemanticsNodes().size <= 1) // at most the unread balance, never a band
    }

    @Test
    fun `the Flat preset is offered next to the five others`() {
        val chosen = mutableListOf<EqPreset>()
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                EqScreen(ConnectionState.Ready, EqBandGains.FLAT, null, null, {}, { chosen += it }, {})
            }
        }
        compose.onNodeWithText("FLAT").performScrollTo().performClick()
        assertEquals(listOf(EqPreset.FLAT), chosen)
    }

    // ---- `ai-sessions/0069` A68-APP-02: while not connected the last connection's values stay, marked ----

    @Test
    fun `after Disconnect the EQ card marks its (i) and names the last connection`() {
        show(gains = EqBandGains.FLAT, connectionState = ConnectionState.Disconnected, updatedAt = 1_727_600_000_000L)
        compose.onNodeWithContentDescription("Equalizer: $DETAILS_NOT_CURRENT_DESCRIPTION").performClick()
        compose.onNodeWithText("From the last connection — the app is not connected to the Buds now.").assertExists()
        compose.onNodeWithText("EQ updated: ${formatUpdatedAt(1_727_600_000_000L)}").assertExists()
    }

    @Test
    fun `a read EQ on an open session has a plain (i)`() {
        show(gains = EqBandGains.FLAT, updatedAt = 1_727_600_000_000L)
        compose.onNodeWithContentDescription("Equalizer: $DETAILS_DESCRIPTION").assertExists()
    }

    @Test
    fun `the settings card on Sound is marked only when it still holds values and nothing is connected`() {
        assertEquals(true, settingsFromLastConnection(ready = false, readings = listOf(SettingReading(true, 1L), null)))
        assertEquals("nothing was ever read: nothing to mark", false, settingsFromLastConnection(ready = false, readings = listOf(null, null)))
        assertEquals(false, settingsFromLastConnection(ready = true, readings = listOf(SettingReading(true, 1L))))
        assertEquals(listOf(FROM_LAST_CONNECTION_DETAIL, "x"), withLastConnectionLine(listOf("x"), true))
        assertEquals(listOf("x"), withLastConnectionLine(listOf("x"), false))
    }

    // ---- the balance slider: one write per completed drag (ai-sessions/0064, kept in 0066) --------------------------------------------------------------------------------------------------

    /** The balance slider: the only slider with the ±100 range. */
    private val balanceSlider = SemanticsMatcher("balance slider") {
        it.config.getOrNull(SemanticsProperties.ProgressBarRangeInfo)?.range == -100f..100f
    }

    /** The Sound screen with the balance the Buds reported ([balance] `null` = not read yet); every `onVolumeBalanceChanged` call is recorded. */
    private fun showBalance(balance: Int?, connection: ConnectionState = ConnectionState.Ready): MutableList<Int> {
        val writes = mutableListOf<Int>()
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                EqScreen(
                    connectionState = connection,
                    gains = EqBandGains.FLAT,
                    eqProfileUpdatedAt = null,
                    eqError = null,
                    onGainsChanged = {},
                    onPresetSelected = {},
                    onRefresh = {},
                    settings = BudsSettings(volumeBalance = balance?.let { SettingReading(it, 1_727_600_000_000L) }),
                    onVolumeBalanceChanged = { writes += it },
                )
            }
        }
        // The balance card is the second item of the screen's LazyColumn: scroll it into composition first.
        compose.onNode(hasScrollAction()).performScrollToNode(balanceSlider)
        return writes
    }

    @Test
    fun `a drag on the slider is still exactly one write, on release`() {
        val writes = showBalance(0)
        compose.onNode(balanceSlider).performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(width / 40f, 0f)) } // ten drag frames toward R
            up()
        }
        compose.waitForIdle()
        assertEquals("one write per completed gesture, not per drag frame", 1, writes.size)
        assertTrue("dragged toward R = a Right value", writes.single() < 0)
    }
}
