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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

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
 * Every **change** is logged with its trigger (always-on, no address). The evaluation itself is [LinkEvaluation]
 * (unit-tested); flapping is smoothed by [settled].
 *
 * **`ai-sessions/0042` — why the first hardware run showed a stale card (`LOGS-001`, kept locally):** the receiver used to be
 * registered `RECEIVER_NOT_EXPORTED`, and in that run it received **no** broadcast at all between 17:17:31 and 17:20:10 although
 * the *unflagged* receiver of the (since removed) `HfpBatteryReader` received the very same `BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED`
 * broadcasts (17:17:49.6 and 17:17:50.8) inside that window — the Buds were bonded, connected over HFP/A2DP and listed as
 * "Actief" in Android's panel, while this card kept saying "Android doesn't show the Buds as connected (yet)". These are
 * *protected* system broadcasts (only the system can send them), so the receiver is now registered `RECEIVER_EXPORTED`,
 * which adds no attack surface. Because no broadcast is guaranteed on every device, the state is additionally re-read on
 * every [refresh] event the caller supplies (resume, a bond change, a change of the app's own session) and whenever a
 * profile proxy binds — still event-driven, no timer.
 *
 * // TODO(verify): the flag change is derived from the evidence above, not yet re-tested on the phone — the maintainer
 * // re-test (a) in `ai-sessions/0042` confirms/refutes it (a card that follows Android within ~2 s, without a tap).
 */
class OsConnectionObserver(
    private val context: Context,
    /** Address of the currently bonded Buds, or `null` — re-resolved on every evaluation so a pairing that happens while this
     * flow is collected is picked up. Never logged (AGENTS.md §9). */
    private val bondedAddress: () -> String?,
) {

    /** @param refresh events on which the state is re-read without any broadcast (resume, bond change, session change). */
    fun observe(refresh: Flow<Unit> = emptyFlow()): Flow<AndroidLink> = callbackFlow {
        val proxies = mutableMapOf<Int, BluetoothProfile>()
        val requested = mutableSetOf<Int>()
        var lastLogged: AndroidLink? = null

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
            LinkEvaluation.transitionLine(lastLogged, link, reason, LinkEvaluation.connectedProfiles(address, connectedByProfile))
                ?.let(BleLogger::logConnectionEvent)
            lastLogged = link
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
        // EXPORTED on purpose: all five actions are protected system broadcasts, and a NOT_EXPORTED receiver did not get them on
        // the maintainer's phone (see the class comment). Nothing but the system can send these actions.
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        BleLogger.logConnectionEvent("Android link observer started")
        launch { refresh.collect { evaluate("refresh") } }

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
