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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** The ANC Quick Settings tile's fixed cycle, and the dock-state byte mapping (`ai-sessions/0042`). */
class TileCycleTest {

    @Test
    @DisplayName("the tile visits every mode once and returns: ANC -> Transparent -> Adaptive -> Off -> ANC")
    fun `cycle order`() {
        var mode = AncMode.ACTIVE
        val visited = mutableListOf(mode)
        repeat(4) {
            mode = mode.nextInTileCycle()
            visited += mode
        }
        assertEquals(listOf(AncMode.ACTIVE, AncMode.TRANSPARENT, AncMode.ADAPTIVE, AncMode.OFF, AncMode.ACTIVE), visited)
        assertEquals(AncMode.entries.toSet(), visited.toSet(), "no mode is skipped")
    }

    @Test
    fun `an unknown current mode starts the cycle at noise cancelling`() {
        assertEquals(AncMode.ACTIVE, AncMode.nextForTile(null))
        assertEquals(AncMode.TRANSPARENT, AncMode.nextForTile(AncMode.ACTIVE))
    }

    @Test
    @DisplayName("dock state: only the two confirmed bytes (ADR-024) are interpreted")
    fun `dock state mapping`() {
        assertEquals(DockState.BOTH_IN_CASE, DockState.fromSettableToggles(0x00))
        assertEquals(DockState.NOT_BOTH_IN_CASE, DockState.fromSettableToggles(0xE8))
        for (other in listOf(0x01, 0x08, 0x20, 0x7F, 0xFF)) assertEquals(DockState.UNKNOWN, DockState.fromSettableToggles(other), "0x%02x".format(other))
    }
}
