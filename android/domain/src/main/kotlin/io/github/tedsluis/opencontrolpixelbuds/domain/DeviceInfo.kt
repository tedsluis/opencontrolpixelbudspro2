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
package io.github.tedsluis.opencontrolpixelbuds.domain

/**
 * Whether the earbuds sit in the case, as the Buds report it in every `Notify ANC state` message
 * (`Settable-toggles` byte; DECISIONS.md ADR-024, second confirmation `ai-sessions/0042`). Only as fresh as the last Message
 * Stream claim (ADR-032).
 */
enum class DockState {
    /** Not reported yet, or a value outside the two confirmed ones — never guessed. */
    UNKNOWN,

    /** `Settable-toggles == 0x00`: both earbuds are seated in the case. */
    BOTH_IN_CASE,

    /** `Settable-toggles == 0xe8`: at least one earbud is out of the case. */
    NOT_BOTH_IN_CASE,
    ;

    companion object {
        /** The confirmed byte values only (ADR-024); anything else is [UNKNOWN]. */
        fun fromSettableToggles(byte: Int): DockState = when (byte) {
            0x00 -> BOTH_IN_CASE
            0xE8 -> NOT_BOTH_IN_CASE
            else -> UNKNOWN
        }
    }
}

/**
 * What the Buds announce about themselves when a session opens (their unsolicited `GetSoftwareInfo`, DECISIONS.md ADR-034):
 * the distinct firmware version strings, e.g. `release_5.203`. Identifiers such as serial numbers are deliberately not carried
 * (`AGENTS.md` §9).
 */
data class DeviceInfo(val firmware: List<String>)
