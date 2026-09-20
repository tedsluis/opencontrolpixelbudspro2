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
@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.tedsluis.opencontrolpixelbuds.hardware

import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** Android-state observation model (`ai-sessions/0041`): initial value, live transitions, permission, debounce. */
class LinkEvaluationTest {
    private val buds = "04:00:6E:CF:6E:07"
    private val a2dp = 2
    private val headset = 1
    private val leAudio = 22
    private val requested = setOf(a2dp, headset, leAudio)

    @Test
    @DisplayName("without BLUETOOTH_CONNECT nothing can be read: UNKNOWN, never 'not connected'")
    fun `permission missing is unknown`() {
        assertEquals(AndroidLink.UNKNOWN, LinkEvaluation.evaluate(buds, permissionOk = false, requested, emptyMap()))
    }

    @Test
    @DisplayName("initial-value race: proxies still binding is 'not yet known' (null), not NOT_CONNECTED")
    fun `unbound proxies are not yet known`() {
        assertNull(LinkEvaluation.evaluate(buds, true, requested, emptyMap()))
        assertNull(LinkEvaluation.evaluate(buds, true, requested, mapOf(a2dp to emptyList<String>())))
    }

    @Test
    fun `NOT_CONNECTED only after every requested proxy has bound and none lists the Buds`() {
        val all = mapOf(a2dp to emptyList<String>(), headset to emptyList(), leAudio to listOf("11:22:33:44:55:66"))
        assertEquals(AndroidLink.NOT_CONNECTED, LinkEvaluation.evaluate(buds, true, requested, all))
    }

    @Test
    @DisplayName("CONNECTED as soon as ONE bound profile lists the Buds — even while other proxies are still binding; case-insensitive")
    fun `one profile is enough`() {
        assertEquals(AndroidLink.CONNECTED, LinkEvaluation.evaluate(buds, true, requested, mapOf(a2dp to listOf("04:00:6e:cf:6e:07"))))
        assertEquals(listOf(a2dp), LinkEvaluation.connectedProfiles(buds, mapOf(a2dp to listOf("04:00:6e:cf:6e:07"), headset to emptyList())))
    }

    @Test
    fun `no bonded address is unknown`() {
        assertEquals(AndroidLink.UNKNOWN, LinkEvaluation.evaluate(null, true, requested, mapOf(a2dp to listOf(buds))))
        assertEquals(AndroidLink.UNKNOWN, LinkEvaluation.evaluate("not an address", true, requested, emptyMap()))
    }

    @Test
    @DisplayName("debounce: a bud coming out of the case flaps CONNECTED/NOT_CONNECTED/CONNECTED — the UI sees only CONNECTED")
    fun `flapping is smoothed`() = runTest {
        val raw = MutableSharedFlow<AndroidLink>()
        val seen = mutableListOf<AndroidLink>()
        val job = launch { raw.settled(notConnectedDelayMs = 1_500).toList(seen) }
        runCurrent()
        raw.emit(AndroidLink.CONNECTED); runCurrent()
        raw.emit(AndroidLink.NOT_CONNECTED); runCurrent()
        advanceTimeBy(400)
        raw.emit(AndroidLink.CONNECTED); runCurrent()
        advanceTimeBy(5_000)
        assertEquals(listOf(AndroidLink.CONNECTED), seen)
        job.cancel()
    }

    @Test
    @DisplayName("debounce: a NOT_CONNECTED that persists is shown after the settle delay, a CONNECTED at once")
    fun `a lasting disconnect is shown after the delay`() = runTest {
        val raw = MutableSharedFlow<AndroidLink>()
        val seen = mutableListOf<AndroidLink>()
        val job = launch { raw.settled(notConnectedDelayMs = 1_500).toList(seen) }
        runCurrent()
        raw.emit(AndroidLink.CONNECTED); runCurrent()
        raw.emit(AndroidLink.NOT_CONNECTED); runCurrent()
        advanceTimeBy(1_400)
        assertEquals(listOf(AndroidLink.CONNECTED), seen)
        advanceTimeBy(200)
        assertEquals(listOf(AndroidLink.CONNECTED, AndroidLink.NOT_CONNECTED), seen)
        raw.emit(AndroidLink.CONNECTED); runCurrent()
        assertEquals(listOf(AndroidLink.CONNECTED, AndroidLink.NOT_CONNECTED, AndroidLink.CONNECTED), seen)
        job.cancel()
    }
}
