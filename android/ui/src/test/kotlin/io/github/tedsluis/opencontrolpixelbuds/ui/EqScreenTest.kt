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
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
 * `ai-sessions/0064` F-2: the balance's `[‹]`/`[›]` steps (tests at the end).
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

    private fun show(gains: EqBandGains?) = compose.setContent {
        OpenControlTheme(darkTheme = false) {
            EqScreen(
                connectionState = ConnectionState.Ready,
                gains = gains,
                eqProfileUpdatedAt = null,
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

    // ---- ai-sessions/0064 F-2: the balance steps --------------------------------------------------------------------------------------------------

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
        compose.onNode(hasScrollAction()).performScrollToNode(hasContentDescription(BALANCE_STEP_RIGHT_DESCRIPTION))
        return writes
    }

    @Test
    fun `Right 4 is reachable - one tap on the right step from Right 3 writes exactly -4 (17 colon 7, CAP-064 6671)`() {
        val writes = showBalance(-3)
        compose.onNodeWithContentDescription(BALANCE_STEP_RIGHT_DESCRIPTION).performClick()
        assertEquals(listOf(-4), writes)
        compose.onNodeWithText("Right 3").assertExists() // the label is the Buds' value until they acknowledge (nothing acknowledged here)
    }

    @Test
    fun `a step from Centre writes 1 or minus 1 - the centre snap does not apply to a step`() {
        val writes = showBalance(0)
        compose.onNodeWithContentDescription(BALANCE_STEP_LEFT_DESCRIPTION).performClick()
        compose.onNodeWithContentDescription(BALANCE_STEP_RIGHT_DESCRIPTION).performClick()
        assertEquals("each tap one write, from the Buds' value", listOf(1, -1), writes)
    }

    @Test
    fun `the step toward an end is disabled at that end, both are disabled before the balance was read or while not connected`() {
        showBalance(100)
        compose.onNodeWithContentDescription(BALANCE_STEP_LEFT_DESCRIPTION).assertIsNotEnabled()
        compose.onNodeWithContentDescription(BALANCE_STEP_RIGHT_DESCRIPTION).assertIsEnabled()
    }

    @Test
    fun `at Right 100 only the left step is enabled`() {
        showBalance(-100)
        compose.onNodeWithContentDescription(BALANCE_STEP_RIGHT_DESCRIPTION).assertIsNotEnabled()
        compose.onNodeWithContentDescription(BALANCE_STEP_LEFT_DESCRIPTION).assertIsEnabled()
    }

    @Test
    fun `not read yet - both steps disabled`() {
        val writes = showBalance(null)
        compose.onNodeWithContentDescription(BALANCE_STEP_LEFT_DESCRIPTION).assertIsNotEnabled()
        compose.onNodeWithContentDescription(BALANCE_STEP_RIGHT_DESCRIPTION).assertIsNotEnabled()
        compose.onNodeWithContentDescription(BALANCE_STEP_RIGHT_DESCRIPTION).performClick()
        assertTrue(writes.isEmpty())
    }

    @Test
    fun `not connected - both steps disabled`() {
        showBalance(0, connection = ConnectionState.Disconnected)
        compose.onNodeWithContentDescription(BALANCE_STEP_LEFT_DESCRIPTION).assertIsNotEnabled()
        compose.onNodeWithContentDescription(BALANCE_STEP_RIGHT_DESCRIPTION).assertIsNotEnabled()
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
