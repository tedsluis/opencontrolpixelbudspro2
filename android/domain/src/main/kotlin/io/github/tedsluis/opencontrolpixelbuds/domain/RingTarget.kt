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
 * Find My Buds ring target. Left/Right only, per DECISIONS.md ADR-011's
 * implementation-unblock scope — Case and "ring both simultaneously" are a
 * deliberate, permanent v1 exclusion (DECISIONS.md ADR-027, PROJECT.md
 * non-goals), not represented here at all so the UI cannot imply a
 * capability this project does not provide (ARCHITECTURE.md §5a).
 */
enum class RingTarget(val wireValue: Int) {
    RIGHT(0x01),
    LEFT(0x02),
    ;

    companion object {
        fun fromWireValue(value: Int): RingTarget? = entries.firstOrNull { it.wireValue == value }
    }
}
