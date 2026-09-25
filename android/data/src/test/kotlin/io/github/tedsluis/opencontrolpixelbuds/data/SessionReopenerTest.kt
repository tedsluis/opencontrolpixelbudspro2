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

import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** DECISIONS.md ADR-044's re-open rules with a scripted re-open that can succeed (`ai-sessions/0048` I-1). */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SessionReopenerTest {

    private class Harness(scope: TestScope, var result: BudsResult<Unit> = BudsResult.Success(Unit)) {
        var open = false
        val triggers = mutableListOf<SessionReopener.Trigger>()
        val reopener = SessionReopener(
            scope = scope.backgroundScope,
            clock = { scope.testScheduler.currentTime },
            sessionOpenOrOpening = { open },
            reopen = { trigger ->
                triggers += trigger
                if (result is BudsResult.Success) open = true
                result
            },
        )
    }

    @Test
    @DisplayName("a Buds DISC -> re-open -> DISC within 10 s does not schedule another (no loop); after 10 s it does")
    fun `chain guard`() = runTest {
        val h = Harness(this)
        h.open = true // a session the user opened
        h.reopener.onVisible(true)
        h.reopener.onAndroidLink(AndroidLink.CONNECTED)
        h.open = false
        h.reopener.onSessionLost()
        advanceTimeBy(1_600); runCurrent()
        assertEquals(listOf(SessionReopener.Trigger.AFTER_LOSS), h.triggers)

        h.open = false
        advanceTimeBy(3_000)
        h.reopener.onSessionLost()
        advanceTimeBy(5_000); runCurrent()
        assertEquals(1, h.triggers.size, "lost 3 s after an automatic re-open: not re-opened again")

        h.reopener.onSessionLost() // 9.6 s after the re-open
        advanceTimeBy(1_600); runCurrent()
        assertEquals(1, h.triggers.size)
        advanceTimeBy(1_000)
        h.reopener.onSessionLost() // > 10 s after it
        advanceTimeBy(1_600); runCurrent()
        assertEquals(2, h.triggers.size)
    }

    @Test
    fun `nothing is attempted while a session is open or opening, or before the link is known connected`() = runTest {
        val h = Harness(this)
        h.reopener.onVisible(true)
        runCurrent()
        assertEquals(0, h.triggers.size, "link unknown")
        h.open = true
        h.reopener.onAndroidLink(AndroidLink.CONNECTED); runCurrent()
        assertEquals(0, h.triggers.size, "already open")
    }

    @Test
    fun `a failed re-open is not retried by the same event`() = runTest {
        val h = Harness(this, result = BudsResult.Failure(BudsError.ChannelUnavailable(0x02, "scripted")))
        h.reopener.onVisible(true)
        h.reopener.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(60_000); runCurrent()
        assertEquals(listOf(SessionReopener.Trigger.LINK_BACK), h.triggers)
    }

    @Test
    fun `two events at the same moment give one attempt`() = runTest {
        val h = Harness(this)
        h.reopener.onAndroidLink(AndroidLink.CONNECTED)
        h.reopener.onVisible(true) // RESUME
        h.reopener.onAndroidLink(AndroidLink.NOT_CONNECTED)
        h.reopener.onAndroidLink(AndroidLink.CONNECTED) // LINK_BACK while the first is in flight/open
        runCurrent()
        assertEquals(listOf(SessionReopener.Trigger.RESUME), h.triggers)
    }

    @Test
    fun `initially disabled stays off until the user's Connect`() = runTest {
        val h = Harness(this)
        val off = SessionReopener(backgroundScope, { testScheduler.currentTime }, { false }, { t -> h.triggers += t; BudsResult.Success(Unit) }, enabled = false)
        off.onVisible(true); off.onAndroidLink(AndroidLink.CONNECTED); runCurrent()
        assertEquals(0, h.triggers.size)
        off.onUserConnect()
        off.onAndroidLink(AndroidLink.NOT_CONNECTED); off.onAndroidLink(AndroidLink.CONNECTED); runCurrent()
        assertEquals(1, h.triggers.size)
    }
}
