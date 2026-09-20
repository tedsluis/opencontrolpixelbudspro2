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

import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingLogic.AssociationView
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingLogic.BondAction
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingLogic.BondKind
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingLogic.BondedCandidate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/** Pure pairing decisions (`ai-sessions/0041`); the addresses below are made-up, not a real device's. */
class PairingLogicTest {

    @Test
    @DisplayName("normalizeAddress: CDM's lower-case MacAddress.toString() becomes the upper-case form getRemoteDevice() requires (the 'could not resolve' root cause)")
    fun `lower-case addresses are upper-cased`() {
        assertEquals("04:00:6E:CF:6E:07", PairingLogic.normalizeAddress("04:00:6e:cf:6e:07")) // exactly as the system log prints it
        assertEquals("04:00:6E:CF:6E:07", PairingLogic.normalizeAddress(" 04:00:6E:CF:6E:07 "))
        assertEquals("AA:BB:CC:DD:EE:FF", PairingLogic.normalizeAddress("aa:bb:cc:dd:ee:ff"))
    }

    @Test
    fun `anything that is not an address normalises to null`() {
        for (bad in listOf(null, "", "04:00:6e:cf:6e", "04-00-6e-cf-6e-07", "04:00:6e:cf:6e:0g", "04:00:6e:cf:6e:07:08", "Pixel Buds")) {
            assertNull(PairingLogic.normalizeAddress(bad), bad)
        }
    }

    @Test
    @DisplayName("pickAssociation reuses the newest association with a valid address — never requests a duplicate")
    fun `picks the newest association`() {
        val list = listOf(
            AssociationView(26, "04:00:6e:cf:6e:07", "Pixel Buds Pro 2"),
            AssociationView(28, "04:00:6e:cf:6e:07", "Pixel Buds Pro 2"),
            AssociationView(27, "04:00:6e:cf:6e:07", "Pixel Buds Pro 2"),
            AssociationView(99, null, "no address"),
        )
        assertEquals(28, PairingLogic.pickAssociation(list)?.id)
        assertNull(PairingLogic.pickAssociation(emptyList()))
        assertNull(PairingLogic.pickAssociation(listOf(AssociationView(1, "nonsense", null))))
    }

    @Test
    @DisplayName("staleAssociationIds: only older duplicates for the SAME address (ids 26 and 27 of the maintainer's repeated attempts, ids 26–34 in the log)")
    fun `cleans up only same-address duplicates`() {
        val list = listOf(
            AssociationView(26, "04:00:6e:cf:6e:07", null),
            AssociationView(27, "04:00:6E:CF:6E:07", null), // same address, different case
            AssociationView(28, "04:00:6e:cf:6e:07", null),
            AssociationView(30, "11:22:33:44:55:66", null), // a different device: never touched
        )
        val keep = list[2]
        assertEquals(listOf(26, 27), PairingLogic.staleAssociationIds(list, keep))
        assertEquals(emptyList<Int>(), PairingLogic.staleAssociationIds(list, null))
        assertEquals(emptyList<Int>(), PairingLogic.staleAssociationIds(list, AssociationView(5, "garbage", null)))
    }

    @Test
    @DisplayName("chooseBonded: the CDM association's address wins (a renamed device is still found); the name is only a fallback")
    fun `chooses the bonded device by association address`() {
        val candidates = listOf(
            BondedCandidate("11:22:33:44:55:66", "Niro"),
            BondedCandidate("04:00:6E:CF:6E:07", "My earbuds"), // renamed by the user
        )
        assertEquals("04:00:6E:CF:6E:07", PairingLogic.chooseBonded(candidates, setOf("04:00:6e:cf:6e:07"))?.address)
    }

    @Test
    fun `chooseBonded falls back to the name and returns null when nothing matches`() {
        val named = listOf(BondedCandidate("11:22:33:44:55:66", "Niro"), BondedCandidate("04:00:6E:CF:6E:07", "Pixel Buds Pro 2 van Ted"))
        assertEquals("04:00:6E:CF:6E:07", PairingLogic.chooseBonded(named, emptySet())?.address)
        assertNull(PairingLogic.chooseBonded(listOf(BondedCandidate("11:22:33:44:55:66", "Niro")), emptySet()))
        assertNull(PairingLogic.chooseBonded(emptyList(), setOf("04:00:6E:CF:6E:07")))
    }

    @Test
    fun `bond states map to their kind and the action`() {
        assertEquals(BondKind.NONE, PairingLogic.bondKind(10))
        assertEquals(BondKind.BONDING, PairingLogic.bondKind(11))
        assertEquals(BondKind.BONDED, PairingLogic.bondKind(12))
        assertNull(PairingLogic.bondKind(-1))
        assertEquals(BondAction.ALREADY_BONDED, PairingLogic.actionFor(BondKind.BONDED)) // already bonded is success: no createBond()
        assertEquals(BondAction.WAIT_FOR_BOND, PairingLogic.actionFor(BondKind.BONDING))
        assertEquals(BondAction.START_BOND, PairingLogic.actionFor(BondKind.NONE))
        assertEquals(BondAction.START_BOND, PairingLogic.actionFor(null))
    }

    @Test
    @DisplayName("a bond that fell back to NONE: never bonding = Buds not in pairing mode; bonding first = rejected/cancelled")
    fun `classifies how a bond ended`() {
        assertEquals(PairingFailure.NotInPairingMode, PairingLogic.classifyBondEnd(sawBonding = false))
        assertEquals(PairingFailure.BondRejected, PairingLogic.classifyBondEnd(sawBonding = true))
    }

    @Test
    @DisplayName("ai-sessions/0042: a bond the stack reports as BONDED is never a timeout — even when its broadcast never arrived")
    fun `a bonded device at the end of the wait is success not a timeout`() {
        assertNull(PairingLogic.outcomeAtTimeout(BondKind.BONDED))
        assertEquals(PairingFailure.BondTimeout, PairingLogic.outcomeAtTimeout(BondKind.BONDING))
        assertEquals(PairingFailure.BondTimeout, PairingLogic.outcomeAtTimeout(BondKind.NONE))
        assertEquals(PairingFailure.BondTimeout, PairingLogic.outcomeAtTimeout(null)) // unreadable: cannot claim success
    }

    @Test
    @DisplayName("the raw CDM text for a second overlapping request is AlreadyInProgress, not a failure of the running request")
    fun `overlapping association request is classified`() {
        // Exactly what the first hardware run logged 82 ms after the first request.
        assertEquals(PairingFailure.AlreadyInProgress, PairingLogic.classifyAssociationError("More than one AssociationRequests are processing."))
        assertEquals(PairingFailure.AlreadyInProgress, PairingLogic.classifyAssociationError("  more than one associationrequests are processing "))
        assertEquals(PairingFailure.AssociationFailed("User rejected"), PairingLogic.classifyAssociationError("User rejected"))
        assertEquals(PairingFailure.AssociationFailed("association failed"), PairingLogic.classifyAssociationError(null))
    }
}
