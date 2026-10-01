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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
import org.junit.Assert.assertEquals
import org.junit.Test

/** `ai-sessions/0064` F-3: the Connection card's text for a Bluetooth-off loss (the maintainer's wording, chat 2026-10-01). */
class LossWordingTest {
    @Test
    fun `a Bluetooth-off loss says so on the card`() {
        assertEquals(
            "Bluetooth was switched off on this phone, which closed the app's channel. Tap Connect to reconnect.",
            BudsError.ChannelLost(0x02, "IOException: bt socket closed, read return: -1").userMessage(SessionLossCause.BLUETOOTH_OFF),
        )
    }
}
