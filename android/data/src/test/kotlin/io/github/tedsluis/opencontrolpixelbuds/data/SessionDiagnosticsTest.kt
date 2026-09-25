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
package io.github.tedsluis.opencontrolpixelbuds.data

import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** The "why did the session end" lines (`ai-sessions/0042`). */
class SessionDiagnosticsTest {

    @Test
    @DisplayName("a lost channel names the channel, the (redacted) detail and how long ago the last frame arrived")
    fun `loss line`() {
        assertEquals(
            "Session lost: channel 0x02 closed (IOException: bt socket closed, read return: -1); " +
                "last inbound frame was on channel 0x04, 1412 ms earlier; not a user disconnect",
            SessionDiagnostics.lossLine(0x02, "IOException: bt socket closed, read return: -1", 0x04, 1412),
        )
    }

    @Test
    fun `loss line without any earlier frame or detail`() {
        assertEquals(
            "Session lost: channel 0x02 closed (no detail); no frame had arrived in this session; not a user disconnect",
            SessionDiagnostics.lossLine(0x02, null, null, null),
        )
    }

    @Test
    @DisplayName("a user disconnect is logged as such — the first hardware run's only trace of it was the HCI log")
    fun `user disconnect line`() {
        assertEquals("Session ended by the user's Disconnect tap (was Ready)", SessionDiagnostics.userDisconnectLine("Ready"))
        assertFalse(SessionDiagnostics.userDisconnectLine("Ready").contains("lost"))
    }

    @Test
    @DisplayName("I-7: the cause line never guesses another app, and says when nothing could be determined")
    fun `loss cause lines`() {
        assertEquals(
            "Session loss cause: Android still showed the Buds connected right after the loss — the Buds closed the channel",
            SessionDiagnostics.lossCauseLine(SessionLossCause.BUDS_CLOSED_CHANNEL),
        )
        for (cause in SessionLossCause.entries) assertFalse(SessionDiagnostics.lossCauseLine(cause).contains("another app"))
    }
}
