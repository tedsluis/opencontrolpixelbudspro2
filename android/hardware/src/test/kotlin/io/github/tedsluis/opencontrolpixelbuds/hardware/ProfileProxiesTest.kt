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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * `ai-sessions/0064` F-4: every profile proxy the listener delivered is closed exactly once when the observer's flow ends — also one unbound before (`CAP-066`
 * FINDINGS §8: an LE Audio proxy unbound at a Bluetooth-off was never closed). No Bluetooth: the proxies are plain objects, the "adapter" records its calls.
 */
class ProfileProxiesTest {
    private val a2dp = 2
    private val headset = 1
    private val leAudio = 22

    private class FakeProxy(val name: String) {
        override fun toString() = name
    }

    @Test
    @DisplayName("bound, unbound (a Bluetooth-off), then the flow ends: the unbound proxy is closed too, each once")
    fun `an unbound proxy is still closed once`() {
        val proxies = ProfileProxies<FakeProxy>()
        val a = FakeProxy("A2DP"); val h = FakeProxy("HEADSET"); val le = FakeProxy("LE_AUDIO")
        proxies.onBound(a2dp, a); proxies.onBound(headset, h); proxies.onBound(leAudio, le)
        proxies.onUnbound(leAudio) // AOSP BluetoothAdapter.onBluetoothOff disconnects every proxy: onServiceDisconnected
        assertEquals(setOf(a2dp, headset), proxies.bound.keys, "an unbound proxy is not read any more")

        val closed = mutableListOf<Pair<Int, FakeProxy>>()
        proxies.closeAll { profile, proxy -> closed += profile to proxy }
        assertEquals(listOf(a2dp to a, headset to h, leAudio to le), closed, "all three, each exactly once")

        proxies.closeAll { profile, proxy -> closed += profile to proxy }
        assertEquals(3, closed.size, "a second close closes nothing again")
        assertTrue(proxies.bound.isEmpty())
    }

    @Test
    fun `the same proxy bound again after Bluetooth comes back is closed once, a new proxy object for the same profile too`() {
        val proxies = ProfileProxies<FakeProxy>()
        val le = FakeProxy("LE_AUDIO"); val le2 = FakeProxy("LE_AUDIO-2")
        proxies.onBound(leAudio, le)
        proxies.onUnbound(leAudio)
        proxies.onBound(leAudio, le) // AOSP rebinds the same object on Bluetooth-on
        proxies.onUnbound(leAudio)
        proxies.onBound(leAudio, le2) // or a different one
        assertEquals(mapOf(leAudio to le2), proxies.bound)

        val closed = mutableListOf<FakeProxy>()
        proxies.closeAll { _, proxy -> closed += proxy }
        assertEquals(listOf(le, le2), closed)
    }
}
