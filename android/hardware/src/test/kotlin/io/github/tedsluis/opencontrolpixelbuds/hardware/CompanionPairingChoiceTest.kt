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
package io.github.tedsluis.opencontrolpixelbuds.hardware

import android.content.ContextWrapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * `ai-sessions/0094` (A93-APP-01): "Use different Buds" ran on the Activity's own [BudsCompanionPairing], while the repository connects through the
 * application singleton — the singleton kept taking the single bonded "Pixel Buds" by name. Both instances now share one [PairingChoiceMemory].
 * The context is an empty [ContextWrapper] (`unitTests.isReturnDefaultValues`): no CompanionDeviceManager, no adapter.
 */
@DisplayName("Use different Buds — one memory for every pairing instance (A93-APP-01)")
class CompanionPairingChoiceTest {

    @Test
    fun `use different Buds on one instance turns the name fallback off on the other`() {
        val memory = PairingChoiceMemory()
        val activityInstance = BudsCompanionPairing(ContextWrapper(null), memory)
        val repositoryInstance = BudsCompanionPairing(ContextWrapper(null), memory)
        assertTrue(repositoryInstance.nameFallbackAllowed())

        activityInstance.forgetAssociations()

        assertFalse(repositoryInstance.nameFallbackAllowed())
        assertFalse(activityInstance.nameFallbackAllowed())
    }

    @Test
    fun `a new Activity instance after a rotation still sees the choice`() {
        val memory = PairingChoiceMemory()
        BudsCompanionPairing(ContextWrapper(null), memory).forgetAssociations()

        assertFalse(BudsCompanionPairing(ContextWrapper(null), memory).nameFallbackAllowed())
    }
}
