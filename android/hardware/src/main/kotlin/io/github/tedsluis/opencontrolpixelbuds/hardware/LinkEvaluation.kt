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

import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transformLatest

/** Pure evaluation of Android's own connection state for the bonded Buds (`ai-sessions/0041`) — see [OsConnectionObserver]. */
object LinkEvaluation {

    /**
     * @param permissionOk `BLUETOOTH_CONNECT` granted — without it the profile proxies throw, so nothing can be known.
     * @param requestedProfiles profile proxies this observer asked the OS for.
     * @param connectedByProfile for each profile whose proxy has **bound**, the (any-case) addresses it lists as connected.
     * @return [AndroidLink.CONNECTED] if any bound profile lists the device; [AndroidLink.NOT_CONNECTED] only once **every**
     * requested proxy has bound and none lists it; [AndroidLink.UNKNOWN] without permission or without a bonded address;
     * `null` = not determinable yet (proxies still binding) — never reported as "not connected" (the initial-value race
     * `ai-sessions/0039` left open).
     */
    fun evaluate(
        bondedAddress: String?,
        permissionOk: Boolean,
        requestedProfiles: Set<Int>,
        connectedByProfile: Map<Int, Collection<String>>,
    ): AndroidLink? {
        if (!permissionOk) return AndroidLink.UNKNOWN
        val wanted = PairingLogic.normalizeAddress(bondedAddress) ?: return AndroidLink.UNKNOWN
        val connected = connectedByProfile.values.any { list -> list.any { PairingLogic.normalizeAddress(it) == wanted } }
        return when {
            connected -> AndroidLink.CONNECTED
            connectedByProfile.keys.containsAll(requestedProfiles) -> AndroidLink.NOT_CONNECTED
            else -> null
        }
    }

    /**
     * The one always-on log line for a **change** of the Android-link state, or `null` when nothing changed (`ai-sessions/0042`).
     * The old code logged every evaluation — three identical lines per profile bind, with an empty `()` where the trigger
     * belonged — which used up a tenth of the (then 500-line, now 1000-line) ring buffer without saying why anything happened. [previous] `null` =
     * nothing determined yet; [trigger] is the short broadcast/event name (never an address); [profiles] only matter for
     * [AndroidLink.CONNECTED].
     */
    fun transitionLine(previous: AndroidLink?, current: AndroidLink?, trigger: String, profiles: List<Int>): String? {
        if (previous == current) return null
        fun name(link: AndroidLink?) = link?.name ?: "PENDING"
        val via = if (current == AndroidLink.CONNECTED && profiles.isNotEmpty()) " via profiles $profiles" else ""
        return "Android link: ${name(previous)} -> ${name(current)} (trigger: $trigger)$via"
    }

    /** Names of the profiles that list the device, for the always-on log (no address). */
    fun connectedProfiles(bondedAddress: String?, connectedByProfile: Map<Int, Collection<String>>): List<Int> {
        val wanted = PairingLogic.normalizeAddress(bondedAddress) ?: return emptyList()
        return connectedByProfile.filter { (_, list) -> list.any { PairingLogic.normalizeAddress(it) == wanted } }.keys.sorted()
    }
}

/**
 * Debounces flapping (a bud taken out of the case can produce several connect/disconnect events in a row): a
 * [AndroidLink.NOT_CONNECTED] is only passed on if no newer state arrives within [notConnectedDelayMs]; a
 * [AndroidLink.CONNECTED]/[AndroidLink.UNKNOWN] passes immediately. One bounded delay per event — no timer loop,
 * nothing runs while nothing changes (ARCHITECTURE.md §6).
 */
fun Flow<AndroidLink>.settled(notConnectedDelayMs: Long): Flow<AndroidLink> =
    distinctUntilChanged().transformLatest { link ->
        if (link == AndroidLink.NOT_CONNECTED) delay(notConnectedDelayMs)
        emit(link)
    }.distinctUntilChanged()
