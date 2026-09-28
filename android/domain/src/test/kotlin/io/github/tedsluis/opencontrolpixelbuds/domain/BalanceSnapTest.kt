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
import org.junit.jupiter.api.Test

/** I-3 (`ai-sessions/0054`): a released balance within ±3 of the centre is written as 0 ("Centre"); every other value is written as released. */
class BalanceSnapTest {
    @Test
    fun `the CAP-063 releases near the centre that were within 3 now write 0`() {
        // CAP-063 frames 5180, 5208, 5211, 5215, 5219, 5223, 5226, 5229, 5232, 5236, 5242, 5245 (16:07:43–16:08:00): the app's `WriteSetting 4:{17:n}`
        // with raw varints n = 21, 19, 7, 1, 1, 3, 22, 14, 11, 6, 7, 1 (`pwrpc_decode.py` prints them raw) — zigzag-decoded (`(n>>1) ^ -(n&1)`, ADR-019):
        val released = listOf(-11, -10, -4, -1, -1, -2, 11, 7, -6, 3, -4, -1)
        assertEquals(listOf(-11, -10, -4, 0, 0, 0, 11, 7, -6, 0, -4, 0), released.map(BudsSettings::snapBalance), "5 of the 12 would have been Centre")
    }

    @Test
    fun `the edges of the snap, both sides, and the clamp`() {
        assertEquals(0, BudsSettings.snapBalance(-3))
        assertEquals(0, BudsSettings.snapBalance(3))
        assertEquals(-4, BudsSettings.snapBalance(-4))
        assertEquals(4, BudsSettings.snapBalance(4))
        assertEquals(100, BudsSettings.snapBalance(100))
        assertEquals(-100, BudsSettings.snapBalance(-140))
    }
}
