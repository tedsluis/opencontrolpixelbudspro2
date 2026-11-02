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

    /** Firmware versions the Pro 2 was verified against (ADR-012: `release_5.203`). Extending it is a code change reviewed
     * against a capture of the new firmware (ADR-042 item 4). Since Gen 1 support, the per-model sets live in [DeviceProfiles]. */
    val VERIFIED_FIRMWARE: Set<String> get() = DeviceProfiles.PRO_2.firmware

    sealed class Verdict {
        data object Allowed : Verdict()
        data class Refused(val state: SafeModeState) : Verdict()
    }

    /**
     * [target] = what the write is for; `null` asks "is any write allowed" (the UI's [observed] check). [deviceType] = `GetHardwareInfo` field 1, if
     * read. The Pro 2 rules and texts are unchanged; a Pro 2 that reports a device type other than 6 is now also refused.
     */
    fun evaluate(
        firmware: List<String>?,
        modelIdHex: String?,
        requireModelId: Boolean,
        target: WriteTarget? = null,
        deviceType: Int? = null,
        /** `true` for the UI's [observed] check: a device type that has not been read yet is not a reason to show Safe Mode. */
        forDisplay: Boolean = false,
    ): Verdict {
        val profile = DeviceProfiles.forFirmware(firmware)
        fun refuse(reason: String) = Verdict.Refused(SafeModeState(firmware, modelIdHex, reason, profile?.model))
        if (firmware.isNullOrEmpty()) return refuse("The Buds did not announce their firmware version on this connection.")
        if (profile == null) {
            val unverified = firmware.filterNot { fw -> DeviceProfiles.ALL.any { fw in it.firmware } }.ifEmpty { firmware }
            return refuse("Firmware ${unverified.joinToString(" / ")} is not a version this app was verified against.")
        }
        val label = profile.model.label
        val writable = if (target == null) profile.allowsAnyWrite else profile.allows(target)
        if (!writable) {
            return refuse(
                if (target == null) "$label detected (firmware ${firmware.joinToString(" / ")}). Changing settings on this model is not verified yet, so the app is read-only."
                else "$label detected: this setting cannot be changed on this model.",
            )
        }
        if (modelIdHex != null && profile.modelIdHex != null && modelIdHex != profile.modelIdHex) {
            return refuse("The Fast Pair Model ID ($modelIdHex) is not the $label's.")
        }
        if (deviceType != null && profile.deviceType != null && deviceType != profile.deviceType) {
            return refuse("The Buds report device type $deviceType, not the $label's (${profile.deviceType}).")
        }
        if (profile.requireDeviceType && deviceType == null && !forDisplay) return refuse("The Buds have not reported their device type on this connection.")
        if (requireModelId && modelIdHex == null) return refuse("The Buds did not report their Fast Pair Model ID.")
        if (requireModelId && profile.modelIdHex == null) return refuse("The $label's Fast Pair Model ID is not known to this app yet.")
        return Verdict.Allowed
    }

    /**
     * What the UI shows without a write having been attempted: Safe Mode is visible as soon as a *known* firmware, Model ID or device type
     * contradicts the allowlist (or the model is read-only); "not announced yet" only becomes Safe Mode once a write is actually refused.
     */
    fun observed(firmware: List<String>?, modelIdHex: String?, deviceType: Int? = null): SafeModeState? {
        val knownFirmware = firmware?.takeIf { it.isNotEmpty() }
        if (knownFirmware == null && modelIdHex == null && deviceType == null) return null
        val verdict = evaluate(knownFirmware ?: VERIFIED_FIRMWARE.toList(), modelIdHex, requireModelId = false, deviceType = deviceType, forDisplay = true)
        return (verdict as? Verdict.Refused)?.state?.let { it.copy(firmware = knownFirmware, model = if (knownFirmware == null) null else it.model) }
    }
}
