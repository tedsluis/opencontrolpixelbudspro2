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
package io.github.tedsluis.opencontrolpixelbuds.app

import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingFailure
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * `:app`'s first JVM tests (`ai-sessions/0098`, `TODO.md` 2.5): the user-facing texts `MainActivity.kt` builds — each case has its own message (AGENTS.md
 * §8: no generic "something went wrong"), and the tile-request codes of `StatusBarManager` (developer.android.com, checked 2026-09-24) map to their words.
 */
class AppTextsTest {

    @Test
    @DisplayName("every pairing failure has its own message, and none is empty or generic")
    fun `pairing failures are distinct`() {
        val failures = listOf(
            PairingFailure.CompanionUnavailable, PairingFailure.AssociationFailed("detail"), PairingFailure.DeviceNotResolved,
            PairingFailure.PermissionMissing, PairingFailure.CouldNotStartBond, PairingFailure.NotInPairingMode, PairingFailure.BondRejected,
            PairingFailure.BondTimeout, PairingFailure.AlreadyInProgress,
        )
        val texts = failures.map { it.toUserMessage() }
        assertEquals(texts.size, texts.toSet().size, "one message per failure")
        texts.forEach { assertTrue(it.length > 20 && !it.contains("something went wrong", ignoreCase = true), it) }
        assertTrue(PairingFailure.AssociationFailed("canceled").toUserMessage().contains("\"canceled\""), "the dialog's own reason is quoted")
    }

    @Test
    fun `a failed pairing state says the failure's message`() {
        assertEquals(PairingFailure.BondTimeout.toUserMessage(), PairingState.Failed(PairingFailure.BondTimeout).toUserMessage())
        assertEquals("Paired with the Buds.", PairingState.Bonded(null).toUserMessage())
    }

    @Test
    @DisplayName("the tile-request results: 2 added, 1 already added, 0 not added, anything else an error with its code")
    fun `tile request results`() {
        assertEquals("The ANC tile was added to Quick Settings.", ancTileResultText(2))
        assertTrue(ancTileResultText(1).startsWith("The ANC tile is already in Quick Settings"))
        assertEquals("The ANC tile was not added.", ancTileResultText(0))
        assertEquals("Android could not add the ANC tile (code 1001).", ancTileResultText(1001))
    }

    @Test
    fun `the no-browser text shows the address`() {
        assertTrue(noBrowserText("https://example.org/x").endsWith("https://example.org/x"))
    }
}
