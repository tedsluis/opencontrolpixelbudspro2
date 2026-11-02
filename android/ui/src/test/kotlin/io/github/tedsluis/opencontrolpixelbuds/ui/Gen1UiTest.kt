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
import androidx.compose.ui.test.onNodeWithText
import io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsModel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DarkMode
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.SafeModeState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Gen 1 support (`GEN1_SUPPORT_INVESTIGATION.md`): what the screens leave out for the original Pixel Buds Pro, and what they say about it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Gen1UiTest {

    @get:Rule
    val compose = createComposeRule()

    private fun anc(model: BudsModel) = compose.setContent {
        OpenControlTheme(darkTheme = false) {
            AncScreen(ConnectionState.Ready, null, null, null, AncAvailability.UNKNOWN, {}, {}, {}, model = model)
        }
    }

    private fun find(model: BudsModel) = compose.setContent {
        OpenControlTheme(darkTheme = false) {
            FindMyBudsScreen(ConnectionState.Ready, null, null, {}, {}, model = model)
        }
    }

    @Test
    fun `the Gen 1 Find tab has no Ring both button`() {
        find(BudsModel.PIXEL_BUDS_PRO)
        compose.onNodeWithText("Ring Left").assertExists()
        compose.onNodeWithText("Ring Right").assertExists()
        compose.onNodeWithText("Ring both").assertDoesNotExist()
    }

    @Test
    fun `the Pro 2 Find tab keeps Ring both`() {
        find(BudsModel.PIXEL_BUDS_PRO_2)
        compose.onNodeWithText("Ring both").assertExists()
    }

    @Test
    fun `the Gen 1 noise-control tab has no Adaptive button`() {
        anc(BudsModel.PIXEL_BUDS_PRO)
        compose.onNodeWithText("Transparency").assertExists()
        compose.onNodeWithText("Noise cancellation").assertExists()
        compose.onNodeWithText("Adaptive").assertDoesNotExist()
    }

    @Test
    fun `the Pro 2 noise-control tab keeps Adaptive`() {
        anc(BudsModel.PIXEL_BUDS_PRO_2)
        compose.onNodeWithText("Adaptive").assertExists()
    }

    @Test
    fun `the Gen 1 Controls tab has no head gestures`() {
        compose.setContent {
            OpenControlTheme(darkTheme = false) {
                ControlsScreen(ConnectionState.Ready, BudsSettings(), null, {}, { _, _ -> }, { _, _ -> }, {}, model = BudsModel.PIXEL_BUDS_PRO)
            }
        }
        compose.onNodeWithText("Use touch controls").assertExists()
        compose.onNodeWithText(HEAD_GESTURES_TITLE).assertDoesNotExist()
    }

    @Test
    fun `the Gen 1 Settings tab has no case sounds`() {
        val gen1 = DeviceInfo(listOf("release_5.11_signed"), model = BudsModel.PIXEL_BUDS_PRO)
        compose.setContent {
            OpenControlTheme(darkTheme = false) { SettingsMenuScreen(DarkMode.SYSTEM, {}, false, {}, emptyList(), {}, AppBuildInfo.UNKNOWN, gen1) }
        }
        compose.onNodeWithText("Dark mode").assertExists()
        compose.onNodeWithText(CASE_SOUNDS_TITLE).assertDoesNotExist()
    }

    @Test
    fun `Info names the model and the device type`() {
        val lines = firmwareInfoLines(DeviceInfo(listOf("release_5.11_signed"), maestroChannel = 19, model = BudsModel.PIXEL_BUDS_PRO, deviceType = 4))
        assertTrue(lines.toString(), "Model: Pixel Buds Pro (1st generation)" in lines)
        assertTrue(lines.toString(), "Device type: 4" in lines)
        val unknown = firmwareInfoLines(DeviceInfo(listOf("release_9.9")))
        assertTrue(unknown.toString(), "Model: not recognised from this firmware" in unknown)
    }

    @Test
    fun `the Safe Mode card says a Gen 1 is read-only on purpose`() {
        val text = safeModeText(SafeModeState(listOf("release_5.11_signed"), null, "Pixel Buds Pro (1st generation) detected.", BudsModel.PIXEL_BUDS_PRO))
        assertTrue(text, "changes are switched off" in text)
        assertFalse(text, "Pixel Buds Pro 2 firmware" in text)
    }
}
