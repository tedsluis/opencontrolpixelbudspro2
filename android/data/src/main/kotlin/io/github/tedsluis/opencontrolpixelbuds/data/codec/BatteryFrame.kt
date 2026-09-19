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
package io.github.tedsluis.opencontrolpixelbuds.data.codec

import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel

/**
 * Fast Pair "Battery updated" on DLCI 0x04's Message Stream — Message Group `0x03` ("Device
 * Information"), Code `0x03` (PROTOCOL.md §4.3 Option B, DECISIONS.md ADR-031). Implementation
 * unblocked by ADR-033, **restricted to the percentage regime** ADR-031 promoted.
 */
object BatteryMessageStream {
    const val GROUP: Int = 0x03
    const val CODE_BATTERY_UPDATED: Int = 0x03

    /** Observed and specified payload width: `[b1, b2, b3]`. Any other width is not this message. */
    const val DATA_LENGTH: Int = 3
}

/**
 * One decoded "Battery updated" frame. Only the Left/Right earbud levels are surfaced (ADR-031:
 * `b1` = Left, `b2` = Right). `b3` (observed `0xff`) is deliberately not interpreted — the Case
 * stays "unavailable" until a maintainer-approved ADR says what it means (ADR-033).
 */
data class BatteryFrame(val left: BatteryLevel, val right: BatteryLevel)
