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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** The press-and-hold ANC-mode list's rules (DECISIONS.md ADR-046, `ai-sessions/0056`): the mode ↔ boolean mapping and "at least two". */
class AncModeListTest {
    private val all = AncModeList(noiseCancellation = true, off = true, transparency = true, adaptive = true)

    @Test
    fun `each mode sets its own boolean (1 NC, 2 Off, 3 Transparency, 4 Adaptive - CAP-056 1689, 1786, 1843, 1815)`() {
        assertEquals(all.copy(noiseCancellation = false), all.with(AncMode.ACTIVE, false))
        assertEquals(all.copy(off = false), all.with(AncMode.OFF, false))
        assertEquals(all.copy(transparency = false), all.with(AncMode.TRANSPARENT, false))
        assertEquals(all.copy(adaptive = false), all.with(AncMode.ADAPTIVE, false))
        for (mode in AncMode.entries) assertFalse(all.with(mode, false).isSelected(mode))
    }

    @Test
    fun `at least two stay ticked - the last two are locked, one is invalid`() {
        val two = AncModeList(noiseCancellation = true, off = false, transparency = true, adaptive = false) // CAP-041 2198
        assertTrue(two.isValid)
        assertTrue(two.isLocked(AncMode.ACTIVE))
        assertTrue(two.isLocked(AncMode.TRANSPARENT))
        assertFalse(two.isLocked(AncMode.OFF), "an unticked box can always be ticked")
        assertFalse(two.with(AncMode.ACTIVE, false).isValid)
        assertFalse(all.isLocked(AncMode.ADAPTIVE))
        assertEquals(3, all.with(AncMode.OFF, false).selectedCount)
        assertFalse(all.with(AncMode.OFF, false).isLocked(AncMode.ACTIVE))
    }
}
