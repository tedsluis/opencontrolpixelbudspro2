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

import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.getSystemService
import io.github.tedsluis.opencontrolpixelbuds.hardware.hfp.HfpAtParser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Battery via HFP AT commands (PROTOCOL.md §4.3 Option C, DECISIONS.md
 * ADR-015/ADR-023) — the one battery mechanism this session implements
 * (ARCHITECTURE.md §5a). Subscribes to
 * `BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT` and hands each raw
 * AT string to `:data`'s [HfpAtParser] — this class owns only the
 * `BluetoothProfile.ServiceListener`/broadcast plumbing, never the parsing
 * itself, so the parsing stays unit-testable without a real profile proxy.
 *
 * Per `AGENTS.md` §5/DECISIONS.md ADR-015: only `AT+BIEV`'s battery-level
 * pushes are treated as a live source. `AT+CIND`'s `battchg` is a
 * one-time-per-session snapshot and must **not** be treated as live — this
 * class does not even surface it as a percentage, only [HfpAtParser] can
 * parse it for completeness if a future feature needs to.
 *
 * // TODO(verify): `ACTION_VENDOR_SPECIFIC_HEADSET_EVENT` is documented by
 * // AGENTS.md §5 as the mechanism to use, but this project has not
 * // independently re-confirmed against real hardware in this environment
 * // that *standard* (non-Apple-vendor-specific) `AT+BIEV`/`AT+CIND` traffic
 * // is actually delivered through this specific broadcast on this device —
 * // some Android/OEM Bluetooth stacks handle standard HF indicators
 * // internally without exposing them to apps at all. If pushes never
 * // arrive in testing, this is the first thing to re-investigate.
 */
class HfpBatteryReader(private val context: Context) {

    /** Emits a battery percentage (0-100) each time `AT+BIEV=2,<value>`
     * arrives. Which physical earbud it represents is unresolved
     * (`BatteryStatus.hfpEarbud`'s own doc comment) — this class only
     * reports the value, attribution is the repository's concern. */
    fun observeBievBatteryPercent(): Flow<Int> = callbackFlow {
        var headsetProxy: BluetoothHeadset? = null

        val eventReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                // Diagnostic (ai-sessions/0041, DECISIONS.md HFP-battery follow-up): the action *name* of every
                // BluetoothHeadset broadcast this receiver gets — nothing else (no extras, no payload) — so a hardware run
                // shows whether anything at all reaches the app.
                intent?.action?.let { BleLogger.logConnectionEvent("HFP broadcast received: ${it.substringAfterLast('.')}") }
                if (intent?.action != BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT) return
                val cmd = intent.getStringExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD)
                    ?: return
                HfpAtParser.parseBiev(cmd)?.let { percent -> trySend(percent) }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT)
            // Diagnostic-only additions: connection/audio state changes prove the receiver itself works even if the
            // (non-vendor-specific) AT+BIEV battery indicator never arrives.
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
        }
        context.registerReceiver(eventReceiver, filter)
        BleLogger.logConnectionEvent("HFP receiver registered (${filter.countActions()} actions)")

        val serviceListener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                if (profile == BluetoothProfile.HEADSET) headsetProxy = proxy as BluetoothHeadset
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.HEADSET) headsetProxy = null
            }
        }
        val adapter = context.getSystemService<BluetoothManager>()?.adapter
        adapter?.getProfileProxy(context, serviceListener, BluetoothProfile.HEADSET)

        awaitClose {
            context.unregisterReceiver(eventReceiver)
            headsetProxy?.let { adapter?.closeProfileProxy(BluetoothProfile.HEADSET, it) }
        }
    }
}
