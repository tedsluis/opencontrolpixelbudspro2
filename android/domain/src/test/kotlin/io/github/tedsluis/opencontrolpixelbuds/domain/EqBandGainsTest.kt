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
package io.github.tedsluis.opencontrolpixelbuds.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** `ai-sessions/0069`: the "Flat" preset (A68-APP-16) and the rejection of gains that are not numbers (A68-APP-11). */
class EqBandGainsTest {

    @Test
    fun `Flat is the sixth preset and is all zeros`() {
        assertEquals(listOf("HEAVY_BASS", "LIGHT_BASS", "BALANCED", "VOCAL_BOOST", "CLARITY", "FLAT"), EqPreset.entries.map { it.name })
        assertEquals(EqBandGains(0f, 0f, 0f, 0f, 0f), EqPreset.FLAT.gains)
    }

    @Test
    fun `gains within range are clamped as before`() {
        assertEquals(EqBandGains(6f, -6f, 0f, 3f, -1.5f), EqBandGains(9f, -7f, 0f, 3f, -1.5f).clampedOrNull())
    }

    @Test
    fun `a gain that is not a finite number is rejected, not clamped`() {
        // `Float.NaN.coerceIn(-6f..6f)` is NaN: every comparison with NaN is false, so clamping passed it on to the encoder.
        assertNull(EqBandGains(Float.NaN, 0f, 0f, 0f, 0f).clampedOrNull())
        assertNull(EqBandGains(0f, 0f, 0f, 0f, Float.POSITIVE_INFINITY).clampedOrNull())
        assertNull(EqBandGains(0f, Float.NEGATIVE_INFINITY, 0f, 0f, 0f).clampedOrNull())
    }
}
