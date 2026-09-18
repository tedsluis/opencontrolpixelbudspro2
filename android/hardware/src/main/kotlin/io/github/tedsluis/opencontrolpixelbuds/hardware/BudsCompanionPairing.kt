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

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.regex.Pattern

/** Progress of the classic-bonding step that follows a CDM association
 * (`BudsCompanionPairing.observeBonding`'s own doc comment explains why this
 * step is separate from — and not automatically done by — CDM itself). */
sealed class PairingState {
    data object Bonding : PairingState()
    data class Bonded(val deviceName: String?) : PairingState()
    data class Failed(val reason: String) : PairingState()
}

/**
 * Wraps `CompanionDeviceManager` for first-time pairing per ARCHITECTURE.md
 * §9.0a's documented flow, and `getBondedDevices()` for already-paired
 * reconnection — `AGENTS.md` §7's only two sanctioned discovery paths. No
 * custom BLE scanning anywhere in this class.
 *
 * // TODO(verify): the CDM `associate()` callback shape differs across API
 * // levels (a deprecated `PendingIntent`-based overload vs. the newer
 * // `Executor`+`Callback` overload) — this project's own floor is API 34
 * // (DECISIONS.md ADR-029), so only the newer overload is used here; not
 * // exercised against a real device/picker in this environment.
 */
class BudsCompanionPairing(private val context: Context) {

    private val companionDeviceManager: CompanionDeviceManager? = context.getSystemService()

    /** Already-bonded Buds Pro 2, if any — the reconnect path, no CDM/UI involved.
     * `BLUETOOTH_CONNECT` is a runtime-revocable permission (AGENTS.md §2) — a
     * missing/revoked grant is not a crash condition, it just means "no bonded
     * device visible yet." */
    fun bondedDevice(): BluetoothDevice? {
        val adapter = context.getSystemService<BluetoothManager>()?.adapter ?: return null
        return try {
            adapter.bondedDevices?.firstOrNull { it.name?.contains("Pixel Buds", ignoreCase = true) == true }
        } catch (e: SecurityException) {
            null
        }
    }

    /**
     * Starts the CDM association flow (ARCHITECTURE.md §9.0a steps 1-4). The
     * `IntentSender` [onPending] receives must be launched via an
     * `ActivityResultLauncher` by the caller (`:ui`/`:app`, which owns the
     * `ComponentActivity`) — this class has no Activity context of its own,
     * matching `:hardware`'s role as a UI-independent layer.
     *
     * // TODO(verify): [NAME_PATTERN] filters CDM's own device picker to names
     * // containing "Pixel Buds" (case-insensitive) — confirmed against the
     * // maintainer's own real device, whose classic Bluetooth name starts
     * // "Pixel Buds Pro 2" (ai-sessions/0036). Without this filter, CDM
     * // offered arbitrary nearby Bluetooth devices one at a time instead of a
     * // Pixel-Buds-only list — a real, hardware-confirmed defect, not a
     * // hypothetical one.
     */
    fun requestAssociation(
        onPending: (IntentSender) -> Unit,
        onCreated: (AssociationInfo) -> Unit,
        onFailure: (CharSequence) -> Unit,
    ) {
        val manager = companionDeviceManager ?: return onFailure("CompanionDeviceManager unavailable")
        val filter = BluetoothDeviceFilter.Builder()
            .setNamePattern(NAME_PATTERN)
            .build()
        val request = AssociationRequest.Builder()
            .addDeviceFilter(filter)
            .setSingleDevice(true) // ARCHITECTURE.md §15: single-device support only for v1.
            .build()

        manager.associate(
            request,
            ContextCompat.getMainExecutor(context),
            object : CompanionDeviceManager.Callback() {
                override fun onAssociationPending(intentSender: IntentSender) = onPending(intentSender)
                override fun onAssociationCreated(associationInfo: AssociationInfo) = onCreated(associationInfo)
                override fun onFailure(error: CharSequence?) = onFailure(error ?: "association failed")
            },
        )
    }

    /** Resolves a CDM [AssociationInfo] back to the `BluetoothDevice` it
     * refers to, so [observeBonding] has something to call `createBond()` on. */
    fun deviceForAssociation(info: AssociationInfo): BluetoothDevice? {
        val mac = info.deviceMacAddress ?: return null
        val adapter = context.getSystemService<BluetoothManager>()?.adapter ?: return null
        return try {
            adapter.getRemoteDevice(mac.toString())
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Starts classic Bluetooth bonding for [device] and reports progress.
     *
     * **`CompanionDeviceManager.associate()`'s own success callback
     * (`onAssociationCreated`) does not pair the device** — CDM only grants
     * this app permission to see/communicate with the device the user picked;
     * actual Bluetooth bonding is a separate step this app must trigger
     * itself (`ARCHITECTURE.md` §9.0a step 5's own documented sequence, which
     * had never actually been wired to a real `createBond()` call until this
     * fix — confirmed missing by a real "the app silently does nothing after
     * I tap Allow" report on hardware, `ai-sessions/0036`).
     */
    fun observeBonding(device: BluetoothDevice): Flow<PairingState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return
                val changedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                if (changedDevice?.address != device.address) return
                when (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)) {
                    BluetoothDevice.BOND_BONDED -> {
                        // BLUETOOTH_CONNECT is runtime-revocable (AGENTS.md §2) — the bond itself
                        // already succeeded by this point, so a missing grant here only costs the
                        // display name, not the pairing outcome.
                        val name = try {
                            device.name
                        } catch (e: SecurityException) {
                            null
                        }
                        trySend(PairingState.Bonded(name))
                        close()
                    }
                    BluetoothDevice.BOND_NONE -> {
                        trySend(PairingState.Failed("Pairing was cancelled or rejected by the Buds."))
                        close()
                    }
                    // BOND_BONDING: already reported below, nothing new to say.
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))

        trySend(PairingState.Bonding)
        try {
            if (!device.createBond()) {
                trySend(PairingState.Failed("Could not start pairing."))
                close()
            }
        } catch (e: SecurityException) {
            trySend(PairingState.Failed("Missing Bluetooth permission."))
            close()
        }

        awaitClose { context.unregisterReceiver(receiver) }
    }

    companion object {
        private val NAME_PATTERN: Pattern = Pattern.compile("(?i).*Pixel\\s*Buds.*")
    }
}
