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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Whether Android itself currently considers the bonded Buds *connected to this phone* (audio/
 * hands-free profiles up) — as opposed to whether **this app** has opened its own RFCOMM control
 * channels. These are two different things (`ai-sessions/0039` §5): before this class existed the
 * Connection screen said "Disconnected" while Android's own Bluetooth settings said "Connected",
 * because the app only ever knew about its own sockets.
 *
 * Uses only public APIs (`BluetoothProfile.getConnectedDevices()` on the A2DP/HEADSET proxies plus
 * their connection-state broadcasts) — `BluetoothDevice.isConnected()` is `@hide` and banned by
 * AGENTS.md §3. Purely informational: this never triggers a connect (ARCHITECTURE.md §6 —
 * user-initiated connection only).
 *
 * // TODO(verify): not exercised against real hardware in this environment; the profile proxies
 * // are asynchronous, so the first emission is `false` until they bind.
 */
class OsConnectionObserver(
    private val context: Context,
    /** Address of the currently bonded Buds, or `null` — re-resolved on every evaluation so a
     * pairing that happens while this flow is collected is picked up. Never logged (AGENTS.md §9). */
    private val bondedAddress: () -> String?,
) {

    fun observe(): Flow<Boolean> = callbackFlow {
        val proxies = mutableMapOf<Int, BluetoothProfile>()

        fun evaluate() {
            val address = bondedAddress()
            val connected = address != null && proxies.values.any { proxy ->
                try {
                    proxy.connectedDevices.any { it.address == address }
                } catch (e: SecurityException) {
                    false // BLUETOOTH_CONNECT revoked (AGENTS.md §2): "unknown" reads as not connected.
                }
            }
            trySend(connected)
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) = evaluate()
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        val listener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                proxies[profile] = proxy
                evaluate()
            }

            override fun onServiceDisconnected(profile: Int) {
                proxies.remove(profile)
                evaluate()
            }
        }
        val adapter = context.getSystemService<BluetoothManager>()?.adapter
        adapter?.getProfileProxy(context, listener, BluetoothProfile.A2DP)
        adapter?.getProfileProxy(context, listener, BluetoothProfile.HEADSET)

        trySend(false)

        awaitClose {
            context.unregisterReceiver(receiver)
            proxies.forEach { (profile, proxy) -> adapter?.closeProfileProxy(profile, proxy) }
            proxies.clear()
        }
    }.distinctUntilChanged()
}
