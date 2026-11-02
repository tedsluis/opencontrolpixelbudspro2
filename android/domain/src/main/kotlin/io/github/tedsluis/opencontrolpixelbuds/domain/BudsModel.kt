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

/**
 * Which Pixel Buds model the connected Buds are, and the features the app shows for it.
 *
 * The model is recognised from the firmware string the Buds announce (see `DeviceProfiles` in `:data`). It is not taken from the
 * Bluetooth name, which the user can change.
 *
 * The Gen 1 feature set is what the official app offers on a Gen 1 (`CAP-081`, Pixel Buds app 1.0.996096625, firmware `release_5.11_signed`):
 * - no Adaptive mode (Noise cancellation, Off and Transparency only);
 * - no head gestures (field 29) and no case sounds (fields 27/28);
 * - Find rings the left or the right bud, never both at once.
 */
enum class BudsModel(
    val label: String,
    /** The noise-control modes this model has, in the on-screen order of the official Pro 2 app. */
    val ancModes: List<AncMode>,
    val hasHeadGestures: Boolean,
    val hasCaseSounds: Boolean,
    /** Whether "Ring both" (`RingTarget.BOTH`, ADR-070) is offered. */
    val hasRingBoth: Boolean,
) {
    PIXEL_BUDS_PRO_2(
        label = "Pixel Buds Pro 2",
        ancModes = listOf(AncMode.ACTIVE, AncMode.OFF, AncMode.ADAPTIVE, AncMode.TRANSPARENT),
        hasHeadGestures = true,
        hasCaseSounds = true,
        hasRingBoth = true,
    ),
    PIXEL_BUDS_PRO(
        label = "Pixel Buds Pro (1st generation)",
        ancModes = listOf(AncMode.ACTIVE, AncMode.OFF, AncMode.TRANSPARENT),
        hasHeadGestures = false,
        hasCaseSounds = false,
        hasRingBoth = false,
    ),
    ;

    fun hasAncMode(mode: AncMode): Boolean = mode in ancModes

    companion object {
        /** What the UI assumes before the model is known: today's (Pro 2) screens, unchanged. */
        val DEFAULT: BudsModel = PIXEL_BUDS_PRO_2
    }
}
