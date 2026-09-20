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

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Whether Android itself currently considers the bonded Buds *connected to this phone* (audio/hands-free/LE-audio profiles
 * up) — as opposed to whether **this app** has opened its own RFCOMM control channels (`ai-sessions/0039` §5,
 * `ai-sessions/0041`: the Connection screen mirrors Android's Bluetooth settings, including the moment a bud is taken out
 * of the case).
 *
 * **Read-only and visibility-bound** (AGENTS.md §2/§7): public APIs only — `BluetoothProfile.getConnectedDevices()` on the
 * A2DP/HEADSET/LE_AUDIO proxies, re-read on every relevant broadcast (profile connection state, ACL connect/disconnect, bond
 * state). It registers when the flow is collected — the UI collects it only while visible — and unregisters on cancel. It
 * never opens a socket, claims a channel or starts a service; it is not scanning and discovers nothing.
 *
 * Every transition is logged (always-on, no address). The evaluation itself is [LinkEvaluation] (unit-tested); flapping is
 * smoothed by [settled].
 *
 * // TODO(verify): not exercised against real hardware in this environment — the maintainer re-test (a)–(d) in
 * // `ai-sessions/0041` §"Re-test" confirms/refutes it. In particular whether a non-exported receiver receives these
 * // system broadcasts on GrapheneOS, and that the proxies list the Buds as soon as Android shows them connected.
 */
class OsConnectionObserver(
    private val context: Context,
    /** Address of the currently bonded Buds, or `null` — re-resolved on every evaluation so a pairing that happens while this
     * flow is collected is picked up. Never logged (AGENTS.md §9). */
    private val bondedAddress: () -> String?,
) {

    fun observe(): Flow<AndroidLink> = callbackFlow {
        val proxies = mutableMapOf<Int, BluetoothProfile>()
        val requested = mutableSetOf<Int>()

        fun evaluate(reason: String) {
            val permissionOk = BluetoothPermissions.hasConnect(context)
            val connectedByProfile: Map<Int, List<String>> = if (!permissionOk) {
                emptyMap()
            } else {
                proxies.mapValues { (_, proxy) ->
                    try {
                        proxy.connectedDevices.map { it.address }
                    } catch (e: SecurityException) {
                        emptyList() // revoked between the check and the call: treated as "not listed"; next evaluation re-checks
                    }
                }
            }
            val address = bondedAddress()
            val link = LinkEvaluation.evaluate(address, permissionOk, requested.toSet(), connectedByProfile)
            BleLogger.logConnectionEvent(
                "Android link (): ${link ?: "not yet known"}" +
                    if (link == AndroidLink.CONNECTED) " via profiles ${LinkEvaluation.connectedProfiles(address, connectedByProfile)}" else "",
            )
            if (link != null) trySend(link)
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) = evaluate(intent?.action?.substringAfterLast('.') ?: "broadcast")
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        BleLogger.logConnectionEvent("Android link observer started")

        val listener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                proxies[profile] = proxy
                evaluate("profile $profile bound")
            }

            override fun onServiceDisconnected(profile: Int) {
                proxies.remove(profile)
                evaluate("profile $profile unbound")
            }
        }
        val adapter = context.getSystemService<BluetoothManager>()?.adapter
        if (adapter == null || !BluetoothPermissions.hasConnect(context)) {
            // No permission (or no adapter): nothing can be read — say so instead of claiming "not connected".
            trySend(AndroidLink.UNKNOWN)
        } else {
            for (profile in listOf(BluetoothProfile.A2DP, BluetoothProfile.HEADSET, BluetoothProfile.LE_AUDIO)) {
                // getProfileProxy returns false when the profile is unsupported on this device — then it is not waited for.
                if (adapter.getProfileProxy(context, listener, profile)) requested += profile
            }
        }

        awaitClose {
            context.unregisterReceiver(receiver)
            proxies.forEach { (profile, proxy) -> adapter?.closeProfileProxy(profile, proxy) }
            proxies.clear()
            BleLogger.logConnectionEvent("Android link observer stopped")
        }
    }.distinctUntilChanged().settled(NOT_CONNECTED_SETTLE_MS)

    companion object {
        /** A "not connected" must persist this long before it is shown — a bud coming out of the case flaps. */
        const val NOT_CONNECTED_SETTLE_MS = 1_500L
    }
}
