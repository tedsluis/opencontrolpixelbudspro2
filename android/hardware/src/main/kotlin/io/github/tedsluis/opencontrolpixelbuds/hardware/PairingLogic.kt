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
 * Pure decision logic of the pairing path (`ai-sessions/0041`) — no Android types, so every rule is unit-tested without a
 * `BluetoothDevice`. The Android-facing class is [BudsCompanionPairing].
 */
object PairingLogic {
    private val MAC = Regex("^([0-9A-F]{2}:){5}[0-9A-F]{2}$")

    /**
     * Canonical form of a Bluetooth address: upper-case `XX:XX:XX:XX:XX:XX`, or null if it is not one.
     *
     * The root cause of "Pairing failed: Could not resolve the selected device" (`ai-sessions/0041` §2): CDM's
     * `android.net.MacAddress.toString()` is **lower-case** (`04:00:6e:cf:6e:07`, exactly as the system log prints it),
     * whereas `BluetoothAdapter.getRemoteDevice(String)` accepts only **upper-case** hex and throws
     * `IllegalArgumentException` — which the old code turned into "could not resolve".
     */
    fun normalizeAddress(raw: String?): String? = raw?.trim()?.uppercase()?.takeIf { MAC.matches(it) }

    /** What the pairing logic needs to know about one CDM association (`AssociationInfo`). */
    data class AssociationView(val id: Int, val address: String?, val displayName: String?)

    /** One entry of the OS's bonded-device store. */
    data class BondedCandidate(val address: String, val name: String?)

    /** The association to reuse: the newest (highest id) one with a valid address — never a duplicate request. */
    fun pickAssociation(associations: List<AssociationView>): AssociationView? =
        associations.filter { normalizeAddress(it.address) != null }.maxByOrNull { it.id }

    /** Older associations for the *same* address as [keep] — duplicates left by earlier attempts, safe to disassociate. */
    fun staleAssociationIds(associations: List<AssociationView>, keep: AssociationView?): List<Int> {
        val keepAddress = normalizeAddress(keep?.address) ?: return emptyList()
        return associations.filter { it.id != keep?.id && normalizeAddress(it.address) == keepAddress }.map { it.id }
    }

    /**
     * Which bonded device is the Buds: the one whose address matches a CDM association of this app (so a renamed device is
     * still found), and only as a fallback one whose name contains "Pixel Buds".
     */
    fun chooseBonded(candidates: List<BondedCandidate>, associatedAddresses: Set<String>): BondedCandidate? {
        val wanted = associatedAddresses.mapNotNull { normalizeAddress(it) }.toSet()
        return candidates.firstOrNull { normalizeAddress(it.address) in wanted }
            ?: candidates.firstOrNull { it.name?.contains("Pixel Buds", ignoreCase = true) == true }
    }

    enum class BondKind { NONE, BONDING, BONDED }

    /** Maps `BluetoothDevice.BOND_*` (10/11/12); null for anything else. */
    fun bondKind(androidBondState: Int): BondKind? = when (androidBondState) {
        10 -> BondKind.NONE
        11 -> BondKind.BONDING
        12 -> BondKind.BONDED
        else -> null
    }

    enum class BondAction { ALREADY_BONDED, WAIT_FOR_BOND, START_BOND }

    /** Already bonded is success (no `createBond()`); already bonding is waited for; only NONE starts a bond. */
    fun actionFor(kind: BondKind?): BondAction = when (kind) {
        BondKind.BONDED -> BondAction.ALREADY_BONDED
        BondKind.BONDING -> BondAction.WAIT_FOR_BOND
        else -> BondAction.START_BOND
    }

    /**
     * A bond that fell back to `BOND_NONE`: if it never reached `BOND_BONDING` the Buds were not reachable — almost always
     * "not in pairing mode"; if it did, the pairing was rejected/cancelled. (The stack's numeric reason code is a hidden
     * constant, not used — AGENTS.md §3.)
     */
    fun classifyBondEnd(sawBonding: Boolean): PairingFailure =
        if (sawBonding) PairingFailure.BondRejected else PairingFailure.NotInPairingMode
}

/** Why a pairing attempt did not end in a bonded device — each has its own user-facing message (AGENTS.md §8). */
sealed class PairingFailure {
    data object CompanionUnavailable : PairingFailure()
    data class AssociationFailed(val detail: String) : PairingFailure()
    data object DeviceNotResolved : PairingFailure()
    data object PermissionMissing : PairingFailure()
    data object CouldNotStartBond : PairingFailure()
    data object NotInPairingMode : PairingFailure()
    data object BondRejected : PairingFailure()
    data object BondTimeout : PairingFailure()
}

/** Progress of a pairing attempt (association → resolve → bond). */
sealed class PairingState {
    data object Requesting : PairingState()
    data object Bonding : PairingState()
    data class Bonded(val deviceName: String?) : PairingState()
    data class Failed(val failure: PairingFailure) : PairingState()
}

/** Result of looking for the already-bonded Buds: a missing permission is **not** "nothing bonded" (AGENTS.md §8). */
sealed class BondedLookup {
    data class Found(val address: String) : BondedLookup()
    data object NoneBonded : BondedLookup()
    data object PermissionMissing : BondedLookup()
}
