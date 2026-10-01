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

/**
 * The profile proxies one [OsConnectionObserver] collection obtained (`ai-sessions/0064` F-4, the maintainer's choice "Build as described", chat 2026-10-01).
 * [bound] is what can be read now (a proxy leaves it when its service unbinds); every proxy ever delivered is also kept until [closeAll], which closes **each
 * one exactly once** — including one unbound before the flow ended. `CAP-066` (FINDINGS §8): a `BluetoothLeAudio` proxy was finalized without `close()`
 * (StrictMode 14:32:36.652 UTC, created at `OsConnectionObserver.kt:137`) because the old code closed only the proxies still bound when the flow ended, and
 * AOSP's `BluetoothAdapter.onBluetoothOff` unbinds every proxy at a Bluetooth-off. 🟡 (`CAP-066-FINDINGS.md` §8): in AOSP `android16-qpr2-release`
 * `BluetoothLeAudio.close()` never closes its `CloseGuard`, so StrictMode may still report an LE Audio proxy the app did close.
 *
 * Pure (no Android type: [P] is `BluetoothProfile` in the app, any object in a test). Not thread-safe by itself: the observer touches it from the main thread
 * (the service listener) and from `awaitClose`.
 */
class ProfileProxies<P : Any> {
    private val current = mutableMapOf<Int, P>()

    /** Every (profile, proxy) the listener delivered, in order, each proxy object once (by identity). */
    private val obtained = mutableListOf<Pair<Int, P>>()

    /** The proxies bound now, by profile — what an evaluation reads. */
    val bound: Map<Int, P> get() = current

    /** The listener's `onServiceConnected`. */
    fun onBound(profile: Int, proxy: P) {
        current[profile] = proxy
        if (obtained.none { it.second === proxy }) obtained += profile to proxy
    }

    /** The listener's `onServiceDisconnected`: no longer readable, still to be closed. */
    fun onUnbound(profile: Int) {
        current.remove(profile)
    }

    /** Closes every proxy obtained — bound or not — exactly once, then forgets them all. */
    fun closeAll(close: (profile: Int, proxy: P) -> Unit) {
        obtained.forEach { (profile, proxy) -> close(profile, proxy) }
        obtained.clear()
        current.clear()
    }
}
