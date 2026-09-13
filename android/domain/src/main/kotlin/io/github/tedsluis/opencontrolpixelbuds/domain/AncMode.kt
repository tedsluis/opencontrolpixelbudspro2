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
 * Active Noise Cancelling mode, per PROTOCOL.md §4.1's confirmed one-hot
 * bitmask (Fast Pair "Hearable Controls" extension, Message Group 0x08).
 * The [wireBit] values are the exact bytes observed on the wire
 * (DECISIONS.md ADR-009) — not renumbered for UI convenience.
 */
enum class AncMode(val wireBit: Int) {
    TRANSPARENT(0x80),
    ADAPTIVE(0x40),
    OFF(0x20),
    ACTIVE(0x08),
    ;

    companion object {
        fun fromWireBit(bit: Int): AncMode? = entries.firstOrNull { it.wireBit == bit }
    }
}
