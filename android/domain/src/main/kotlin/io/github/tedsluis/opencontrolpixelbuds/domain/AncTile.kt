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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart

/** What the Quick Settings ANC tile shows (`:app`'s `AncTileService`), derived only from what the repository reports. */
data class AncTileState(
    val ready: Boolean,
    val mode: AncMode?,
    val availability: AncAvailability,
    val subtitle: String,
    /** `Tile.STATE_ACTIVE` when true. */
    val active: Boolean,
)

fun ancTileState(session: ConnectionState, mode: AncMode?, availability: AncAvailability): AncTileState {
    val ready = session is ConnectionState.Ready
    val subtitle = when {
        !ready -> "Open the app"
        // `ai-sessions/0062` F-2 (the maintainer's wording, chat 2026-10-01): the byte says "not allowed now", not "not worn" (`CAP-064`: e8 with no bud worn).
        availability == AncAvailability.NOT_ALLOWED -> "Not allowed now"
        mode == null -> "Tap to switch"
        else -> mode.toString().lowercase().replaceFirstChar { it.uppercase() }
    }
    return AncTileState(ready, mode, availability, subtitle, active = ready && mode != null && mode != AncMode.OFF)
}

/**
 * The tile's state stream. `combine` emits only after **every** source has emitted once, and the repository's ANC mode has no value until the
 * Buds report one in this process — so without the leading `null` the tile would stay on "Open the app" while the session is `Ready` (A58-APP-01,
 * `ai-sessions/0059`; `OpenControlApplication` guards the same flow the same way).
 */
fun ancTileStates(
    connectionState: Flow<ConnectionState>,
    ancMode: Flow<AncMode>,
    ancAvailability: Flow<AncAvailability>,
): Flow<AncTileState> = combine(connectionState, ancMode.onStart<AncMode?> { emit(null) }, ancAvailability, ::ancTileState)
