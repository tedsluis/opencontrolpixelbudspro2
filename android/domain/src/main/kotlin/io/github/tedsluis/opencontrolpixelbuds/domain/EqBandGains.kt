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
 * 5-band EQ gains, in the on-screen top-to-bottom order (PROTOCOL.md §4.2's
 * field-to-band mapping table — the wire quintet order is the *reverse* of
 * this; [io.github.tedsluis.opencontrolpixelbuds.data] codecs are responsible
 * for that re-index, not this domain type). Units are not independently
 * confirmed (plausibly dB) — PROTOCOL.md §4.2.
 */
data class EqBandGains(
    val upperTreble: Float,
    val treble: Float,
    val mid: Float,
    val bass: Float,
    val lowBass: Float,
) {
    /** Clamps every band to the confirmed ±6.0 range (DECISIONS.md ADR-016). */
    fun clamped(): EqBandGains = EqBandGains(
        upperTreble = upperTreble.coerceIn(RANGE),
        treble = treble.coerceIn(RANGE),
        mid = mid.coerceIn(RANGE),
        bass = bass.coerceIn(RANGE),
        lowBass = lowBass.coerceIn(RANGE),
    )

    companion object {
        val RANGE = -6.0f..6.0f
        val FLAT = EqBandGains(0f, 0f, 0f, 0f, 0f)
    }
}

/**
 * Confirmed preset quintets (PROTOCOL.md §4.2, DECISIONS.md ADR-016) — bands
 * listed in on-screen order to match [EqBandGains]. `Last saved` is
 * deliberately excluded: PROTOCOL.md §4.2 notes it is per-account/session
 * state, not a fixed constant, so it is represented as the repository's
 * current cached [EqBandGains] rather than a member of this enum.
 */
enum class EqPreset(val gains: EqBandGains) {
    HEAVY_BASS(EqBandGains(upperTreble = 0.0f, treble = 0.0f, mid = 0.0f, bass = 3.0f, lowBass = 5.0f)),
    LIGHT_BASS(EqBandGains(upperTreble = 0.0f, treble = 0.0f, mid = 0.0f, bass = -1.5f, lowBass = -5.0f)),
    BALANCED(EqBandGains(upperTreble = 2.5f, treble = -1.0f, mid = 1.0f, bass = 0.5f, lowBass = -3.5f)),
    VOCAL_BOOST(EqBandGains(upperTreble = 0.0f, treble = 2.0f, mid = 4.0f, bass = 0.0f, lowBass = -1.0f)),
    CLARITY(EqBandGains(upperTreble = 5.0f, treble = 3.0f, mid = 2.0f, bass = 0.0f, lowBass = -2.0f)),
}
