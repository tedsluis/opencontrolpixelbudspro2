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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** One of the four [BluetoothAdapter] states this app treats as
 * distinguishable UI states — a superset of what the raw `EXTRA_STATE` int
 * spells out, so callers don't need to know the adapter's own constants. */
enum class BluetoothAdapterState { ON, OFF, TURNING_ON, TURNING_OFF }

/**
 * Observes `BluetoothAdapter.ACTION_STATE_CHANGED` and treats `STATE_OFF` as
 * a normal, expected transition — never a crash condition (AGENTS.md §2,
 * ARCHITECTURE.md §6). The Connection screen (ARCHITECTURE.md §2.4) uses
 * [BluetoothAdapterState.OFF] to show "Bluetooth is disabled" with a native
 * `ACTION_REQUEST_ENABLE` prompt, never a custom/Play-Services-style dialog.
 */
class BluetoothStateObserver(private val context: Context) {

    fun observe(): Flow<BluetoothAdapterState> = callbackFlow {
        var last: BluetoothAdapterState? = null
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val state = mapState(intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) ?: BluetoothAdapter.ERROR)
                // `ai-sessions/0064` F-3: always-on (a state transition, AGENTS.md §9) — `CAP-066`'s export had no trace of either Bluetooth-off.
                adapterTransitionLine(last, state)?.let(BleLogger::logConnectionEvent)
                last = state
                trySend(state)
            }
        }
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        val now = current()
        last = now
        trySend(now)

        awaitClose { context.unregisterReceiver(receiver) }
    }

    /**
     * The adapter state now, read synchronously — the UI's initial value, so the first frame does not show "Bluetooth is disabled" before [observe]'s first
     * value arrives (`ai-sessions/0058` A58-APP-08).
     */
    fun current(): BluetoothAdapterState {
        val adapter = context.getSystemService<BluetoothManager>()?.adapter
        return if (adapter?.isEnabled == true) BluetoothAdapterState.ON else BluetoothAdapterState.OFF
    }

    companion object {
        /** The always-on line for an adapter change (F-3), or `null` when nothing changed: "Bluetooth adapter: ON -> TURNING_OFF". */
        fun adapterTransitionLine(previous: BluetoothAdapterState?, current: BluetoothAdapterState): String? =
            if (previous == current) null else "Bluetooth adapter: ${previous?.name ?: "UNKNOWN"} -> ${current.name}"

        /** What the repository is told ([io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository.onBluetoothAdapter]): only TURNING_OFF and OFF are "off". */
        fun isOn(state: BluetoothAdapterState): Boolean = state == BluetoothAdapterState.ON || state == BluetoothAdapterState.TURNING_ON
    }

    private fun mapState(state: Int): BluetoothAdapterState = when (state) {
        BluetoothAdapter.STATE_ON -> BluetoothAdapterState.ON
        BluetoothAdapter.STATE_OFF -> BluetoothAdapterState.OFF
        BluetoothAdapter.STATE_TURNING_ON -> BluetoothAdapterState.TURNING_ON
        BluetoothAdapter.STATE_TURNING_OFF -> BluetoothAdapterState.TURNING_OFF
        else -> BluetoothAdapterState.OFF // Unknown/ERROR treated as "not usable," same UI as OFF.
    }
}
