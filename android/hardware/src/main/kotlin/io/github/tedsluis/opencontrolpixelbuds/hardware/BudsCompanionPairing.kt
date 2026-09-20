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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Wraps `CompanionDeviceManager` for first-time pairing per ARCHITECTURE.md §9.0a's documented flow, and
 * `getBondedDevices()` for already-paired reconnection — `AGENTS.md` §7's only two sanctioned discovery paths. No custom
 * BLE scanning anywhere in this class. Every decision (address normalisation, which association/bonded device, what a bond
 * outcome means) lives in [PairingLogic] and is unit-tested; this class is the thin Android glue around it, and logs every
 * step (always-on, no address — AGENTS.md §9/§13).
 *
 * // TODO(verify): the CDM `associate()` callback and picker are not exercised against a real device in this environment
 * // — the maintainer re-test (b) in `ai-sessions/0041` confirms/refutes the fixes for the "could not resolve the selected
 * // device" failure.
 */
class BudsCompanionPairing(private val context: Context) {

    private val companionDeviceManager: CompanionDeviceManager? = context.getSystemService()

    /** This app's own CDM associations, as pure views ([PairingLogic.AssociationView]). Empty if CDM is unavailable. */
    private fun associations(): List<Pair<AssociationInfo, PairingLogic.AssociationView>> =
        companionDeviceManager?.myAssociations.orEmpty().map { info ->
            info to PairingLogic.AssociationView(info.id, info.deviceMacAddress?.toString(), info.displayName?.toString())
        }

    /**
     * Looks for the already-bonded Buds — the reconnect path, no CDM picker involved. A missing `BLUETOOTH_CONNECT` grant is
     * reported as [BondedLookup.PermissionMissing], **never** as "nothing bonded" (`ai-sessions/0041`: after clearing app
     * data the old code swallowed the `SecurityException` and the app claimed "No Pixel Buds paired yet" while Android showed
     * them connected). The device is identified by the address of this app's CDM association (a renamed device is still
     * found); the name "Pixel Buds" is only a fallback.
     */
    fun lookupBonded(): BondedLookup {
        if (!BluetoothPermissions.hasConnect(context)) return BondedLookup.PermissionMissing
        val adapter = context.getSystemService<BluetoothManager>()?.adapter ?: return BondedLookup.NoneBonded
        val bonded = try {
            adapter.bondedDevices.orEmpty().map { PairingLogic.BondedCandidate(it.address, safeName(it)) }
        } catch (e: SecurityException) {
            BleLogger.logConnectionEvent("Bonded-device lookup: permission revoked (${BleLogger.describe(e)})")
            return BondedLookup.PermissionMissing
        }
        val associated = associations().mapNotNull { (_, view) -> view.address }.toSet()
        val chosen = PairingLogic.chooseBonded(bonded, associated)
        return if (chosen == null) BondedLookup.NoneBonded else BondedLookup.Found(chosen.address)
    }

    /** The bonded Buds' `BluetoothDevice`, or null (no permission and nothing bonded both read as null here — use
     * [lookupBonded] when the difference matters). Used by the repository at Connect time. */
    fun bondedDevice(): BluetoothDevice? {
        val found = lookupBonded() as? BondedLookup.Found ?: return null
        val adapter = context.getSystemService<BluetoothManager>()?.adapter ?: return null
        return try {
            adapter.getRemoteDevice(found.address)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Starts the CDM association flow (ARCHITECTURE.md §9.0a steps 1-4) — **or reuses** this app's existing association for
     * the Buds instead of creating a duplicate (`ai-sessions/0041`: every attempt used to add one, ids 26–34 in one afternoon's log). The
     * `IntentSender` [onPending] receives must be launched via an `ActivityResultLauncher` by the caller (`:app`, which
     * owns the `ComponentActivity`).
     *
     * // TODO(verify): [NAME_PATTERN] filters CDM's own device picker to names containing "Pixel Buds" (case-insensitive) —
     * // confirmed against the maintainer's real device (`ai-sessions/0036`).
     */
    fun requestAssociation(
        onPending: (IntentSender) -> Unit,
        onCreated: (AssociationInfo) -> Unit,
        onFailure: (PairingFailure) -> Unit,
    ) {
        val manager = companionDeviceManager
        if (manager == null) {
            BleLogger.logConnectionEvent("Pairing: CompanionDeviceManager unavailable")
            return onFailure(PairingFailure.CompanionUnavailable)
        }
        val existing = associations()
        val reuse = PairingLogic.pickAssociation(existing.map { it.second })
        if (reuse != null) {
            BleLogger.logConnectionEvent("Pairing: reusing existing association (${existing.size} total) — no new picker")
            existing.first { it.second.id == reuse.id }.first.let(onCreated)
            return
        }
        val filter = BluetoothDeviceFilter.Builder().setNamePattern(NAME_PATTERN).build()
        val request = AssociationRequest.Builder()
            .addDeviceFilter(filter)
            .setSingleDevice(true) // ARCHITECTURE.md §15: single-device support only for v1.
            .build()
        BleLogger.logConnectionEvent("Pairing: association requested (CDM picker)")
        manager.associate(
            request,
            ContextCompat.getMainExecutor(context),
            object : CompanionDeviceManager.Callback() {
                override fun onAssociationPending(intentSender: IntentSender) = onPending(intentSender)
                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    BleLogger.logConnectionEvent("Pairing: association created")
                    onCreated(associationInfo)
                }

                override fun onFailure(error: CharSequence?) {
                    BleLogger.logConnectionEvent("Pairing: association failed (${error ?: "no reason given"})")
                    onFailure(PairingFailure.AssociationFailed(error?.toString() ?: "association failed"))
                }
            },
        )
    }

    /**
     * Resolves a CDM [AssociationInfo] to the `BluetoothDevice` it refers to. Prefers `AssociationInfo.associatedDevice`
     * (API 34); falls back to the address **upper-cased** — `MacAddress.toString()` is lower-case and
     * `BluetoothAdapter.getRemoteDevice(String)` rejects lower-case (`ai-sessions/0041`, the "could not resolve the selected
     * device" root cause). The reason for a null result is logged.
     */
    fun deviceForAssociation(info: AssociationInfo): BluetoothDevice? {
        info.associatedDevice?.bluetoothDevice?.let {
            BleLogger.logConnectionEvent("Pairing: device resolved from the association's own device")
            return it
        }
        val address = PairingLogic.normalizeAddress(info.deviceMacAddress?.toString())
        if (address == null) {
            BleLogger.logConnectionEvent("Pairing: association has no usable Bluetooth address")
            return null
        }
        val adapter = context.getSystemService<BluetoothManager>()?.adapter
        if (adapter == null) {
            BleLogger.logConnectionEvent("Pairing: no Bluetooth adapter")
            return null
        }
        return try {
            adapter.getRemoteDevice(address).also { BleLogger.logConnectionEvent("Pairing: device resolved from the association address") }
        } catch (e: IllegalArgumentException) {
            BleLogger.logConnectionEvent("Pairing: address rejected by the adapter (${BleLogger.describe(e)})")
            null
        }
    }

    /**
     * Housekeeping decision (documented in ARCHITECTURE.md §9.0a): after an association for the Buds exists, older duplicate
     * associations of *this app* for the **same address** (left by earlier attempts) are removed with `disassociate()`, the
     * newest is kept. It touches only this app's own associations and only exact-address duplicates.
     */
    fun cleanUpDuplicateAssociations(keep: AssociationInfo) {
        val manager = companionDeviceManager ?: return
        val all = associations()
        val keepView = all.firstOrNull { it.second.id == keep.id }?.second
            ?: PairingLogic.AssociationView(keep.id, keep.deviceMacAddress?.toString(), keep.displayName?.toString())
        val stale = PairingLogic.staleAssociationIds(all.map { it.second }, keepView)
        stale.forEach { id ->
            try {
                manager.disassociate(id)
            } catch (e: RuntimeException) {
                BleLogger.logConnectionEvent("Pairing: could not remove a duplicate association (${BleLogger.describe(e)})")
            }
        }
        if (stale.isNotEmpty()) BleLogger.logConnectionEvent("Pairing: removed ${stale.size} duplicate association(s)")
    }

    /**
     * Starts classic Bluetooth bonding for [device] (unless it is already bonded or bonding) and reports progress.
     *
     * **`CompanionDeviceManager.associate()`'s own success callback does not pair the device** — CDM only grants this app
     * permission to see it; bonding is a separate step (`ai-sessions/0036`). Already-bonded is success; a bond that falls
     * back to `BOND_NONE` is classified by [PairingLogic.classifyBondEnd]; a bond that takes longer than
     * [BOND_TIMEOUT_MS] ends with [PairingFailure.BondTimeout] (one bounded wait, not a retry loop).
     */
    fun observeBonding(device: BluetoothDevice): Flow<PairingState> = callbackFlow {
        var sawBonding = false
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return
                val changed = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                if (changed?.address != device.address) return
                when (PairingLogic.bondKind(intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1))) {
                    PairingLogic.BondKind.BONDING -> {
                        sawBonding = true
                        BleLogger.logConnectionEvent("Pairing: bond state BONDING")
                        trySend(PairingState.Bonding)
                    }
                    PairingLogic.BondKind.BONDED -> {
                        BleLogger.logConnectionEvent("Pairing: bond state BONDED")
                        trySend(PairingState.Bonded(safeName(device)))
                        close()
                    }
                    PairingLogic.BondKind.NONE -> {
                        val failure = PairingLogic.classifyBondEnd(sawBonding)
                        BleLogger.logConnectionEvent("Pairing: bond state NONE — $failure")
                        trySend(PairingState.Failed(failure))
                        close()
                    }
                    null -> Unit
                }
            }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)

        val kind = try {
            PairingLogic.bondKind(device.bondState)
        } catch (e: SecurityException) {
            BleLogger.logConnectionEvent("Pairing: permission missing when reading the bond state")
            trySend(PairingState.Failed(PairingFailure.PermissionMissing))
            close()
            null
        }
        if (!isClosedForSend) {
            when (PairingLogic.actionFor(kind)) {
                PairingLogic.BondAction.ALREADY_BONDED -> {
                    BleLogger.logConnectionEvent("Pairing: already bonded — no createBond()")
                    trySend(PairingState.Bonded(safeName(device)))
                    close()
                }
                PairingLogic.BondAction.WAIT_FOR_BOND -> {
                    BleLogger.logConnectionEvent("Pairing: bonding already in progress — waiting")
                    sawBonding = true
                    trySend(PairingState.Bonding)
                }
                PairingLogic.BondAction.START_BOND -> {
                    BleLogger.logConnectionEvent("Pairing: createBond()")
                    trySend(PairingState.Bonding)
                    try {
                        if (!device.createBond()) {
                            trySend(PairingState.Failed(PairingFailure.CouldNotStartBond))
                            close()
                        }
                    } catch (e: SecurityException) {
                        trySend(PairingState.Failed(PairingFailure.PermissionMissing))
                        close()
                    }
                }
            }
        }
        if (!isClosedForSend) {
            launch {
                delay(BOND_TIMEOUT_MS)
                BleLogger.logConnectionEvent("Pairing: bond timed out")
                trySend(PairingState.Failed(PairingFailure.BondTimeout))
                close()
            }
        }

        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun safeName(device: BluetoothDevice): String? = try {
        device.name
    } catch (e: SecurityException) {
        null // BLUETOOTH_CONNECT revoked: only the display name is lost (AGENTS.md §2).
    }

    companion object {
        private val NAME_PATTERN: Pattern = Pattern.compile("(?i).*Pixel\\s*Buds.*")

        /** Upper bound for the classic bond to complete once started. */
        const val BOND_TIMEOUT_MS = 45_000L
    }
}
