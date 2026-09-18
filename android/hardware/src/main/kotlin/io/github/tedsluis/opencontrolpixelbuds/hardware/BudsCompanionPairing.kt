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
import android.content.Context
import android.content.IntentSender
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

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
     */
    fun requestAssociation(
        onPending: (IntentSender) -> Unit,
        onCreated: (AssociationInfo) -> Unit,
        onFailure: (CharSequence) -> Unit,
    ) {
        val manager = companionDeviceManager ?: return onFailure("CompanionDeviceManager unavailable")
        val filter = BluetoothDeviceFilter.Builder().build()
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
}
