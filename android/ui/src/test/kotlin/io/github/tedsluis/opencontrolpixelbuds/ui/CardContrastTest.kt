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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.test.core.app.ApplicationProvider
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * `ai-sessions/0062` F-7: the Connection card's text — its "Disconnect" action included — must be legible on the coloured card in the light **and** the dark
 * scheme. `CAP-065` (film 11:23:50, dark mode, a camera image of the screen, so approximate): the label, then in TextButton's default `primary`, measured
 * ≈ 1.2:1 against the card, the card's own text ≈ 4.2:1. WCAG 2.x 1.4.3 asks 4.5:1 for normal text; the contrast ratio is (L1 + 0.05) / (L2 + 0.05) of the
 * relative luminances ([Color.luminance] is that luminance).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CardContrastTest {

    private fun contrast(a: Color, b: Color): Float {
        val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (hi + 0.05f) / (lo + 0.05f)
    }

    private fun schemes(): Map<String, ColorScheme> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return mapOf(
            "Material baseline light" to lightColorScheme(),
            "Material baseline dark" to darkColorScheme(),
            "dynamic light (Robolectric's system palette)" to dynamicLightColorScheme(context),
            "dynamic dark (Robolectric's system palette)" to dynamicDarkColorScheme(context),
        )
    }

    @Test
    fun `the card's text and its action reach 4_5 to 1 in every scheme`() {
        for ((name, scheme) in schemes()) {
            for (session in listOf(SessionLine.OPEN, SessionLine.FAILED)) {
                val pair = connectionCardColors(scheme, session)!!
                val ratio = contrast(pair.content, pair.container)
                assertTrue("$name, $session: ${"%.2f".format(ratio)}:1 < 4.5:1", ratio >= 4.5f)
            }
        }
    }

    @Test
    fun `the action takes the card's own content colour, not primary`() {
        for ((name, scheme) in schemes()) {
            assertEquals(name, scheme.onPrimaryContainer, connectionCardColors(scheme, SessionLine.OPEN)!!.content)
            assertEquals(name, scheme.onErrorContainer, connectionCardColors(scheme, SessionLine.FAILED)!!.content)
        }
    }
}
