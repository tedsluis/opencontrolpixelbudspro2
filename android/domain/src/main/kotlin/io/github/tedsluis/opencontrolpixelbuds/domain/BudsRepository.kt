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

import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing surface onto the Buds, implemented in `:data` (ARCHITECTURE.md
 * §2.1). Local state exposed here is a cache of the hardware's last-known
 * state, never the authority on it (ARCHITECTURE.md §3.1) — [ancMode] is
 * reconciled against a fresh read on every (re)connection, not assumed to
 * still hold across a reconnect.
 */
interface BudsRepository {
    val connectionState: Flow<ConnectionState>
    val ancMode: Flow<AncMode>
    val unidentifiedFrames: Flow<UnidentifiedFrame>

    suspend fun setAncMode(mode: AncMode): BudsResult<Unit>
    suspend fun refreshAncMode(): BudsResult<AncMode>
}
