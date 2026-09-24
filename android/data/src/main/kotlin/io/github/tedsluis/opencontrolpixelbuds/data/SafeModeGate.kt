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
package io.github.tedsluis.opencontrolpixelbuds.data

import io.github.tedsluis.opencontrolpixelbuds.data.codec.DeviceInformation
import io.github.tedsluis.opencontrolpixelbuds.domain.SafeModeState

/**
 * The Startup Handshake's write gate (ARCHITECTURE.md §8.1, DECISIONS.md ADR-042) — pure, so every branch is unit-tested.
 *
 * A write/control command is allowed only when (1) every firmware string the Buds announced on this connection is in
 * [VERIFIED_FIRMWARE] and (2) the Fast Pair Model ID is the Pixel Buds Pro 2's — for Message Stream commands it must have been
 * seen on the current claim ([requireModelId]); otherwise a Model ID seen on this connection must not contradict it.
 */
object SafeModeGate {

    /** Firmware versions this app was verified against (ADR-012: `release_5.203`). Extending it is a code change reviewed
     * against a capture of the new firmware (ADR-042 item 4). */
    val VERIFIED_FIRMWARE: Set<String> = setOf("release_5.203")

    sealed class Verdict {
        data object Allowed : Verdict()
        data class Refused(val state: SafeModeState) : Verdict()
    }

    fun evaluate(firmware: List<String>?, modelIdHex: String?, requireModelId: Boolean): Verdict {
        fun refuse(reason: String) = Verdict.Refused(SafeModeState(firmware, modelIdHex, reason))
        if (firmware.isNullOrEmpty()) return refuse("The Buds did not announce their firmware version on this connection.")
        val unverified = firmware.filterNot { it in VERIFIED_FIRMWARE }
        if (unverified.isNotEmpty()) {
            return refuse("Firmware ${unverified.joinToString(" / ")} is not a version this app was verified against.")
        }
        if (modelIdHex != null && modelIdHex != DeviceInformation.PIXEL_BUDS_PRO_2_MODEL_ID) {
            return refuse("The Fast Pair Model ID ($modelIdHex) is not the Pixel Buds Pro 2's.")
        }
        if (requireModelId && modelIdHex == null) return refuse("The Buds did not report their Fast Pair Model ID.")
        return Verdict.Allowed
    }

    /**
     * What the UI shows without a write having been attempted: Safe Mode is visible as soon as a *known* firmware or Model ID
     * contradicts the allowlist; "not announced yet" only becomes Safe Mode once a write is actually refused.
     */
    fun observed(firmware: List<String>?, modelIdHex: String?): SafeModeState? {
        val knownFirmware = firmware?.takeIf { it.isNotEmpty() }
        if (knownFirmware == null && modelIdHex == null) return null
        val verdict = evaluate(knownFirmware ?: VERIFIED_FIRMWARE.toList(), modelIdHex, requireModelId = false)
        return (verdict as? Verdict.Refused)?.state?.copy(firmware = knownFirmware)
    }
}
