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
 * Connection lifecycle state (ARCHITECTURE.md §2.1). This shape covers only
 * the classic BR/EDR link steps already 🟢 FACT in PROTOCOL.md §5.1 —
 * anything beyond that (the exact Message-Stream/libmaestro handshake
 * ordering) is still ⚪ ASSUMPTION per PROTOCOL.md §5, so this stays
 * structurally honest rather than over-specified.
 */
sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Discovering : ConnectionState()
    data object Ready : ConnectionState()
    data class Failed(val error: BudsError) : ConnectionState()
}
