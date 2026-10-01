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
import androidx.compose.ui.test.onNodeWithText
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
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
}
