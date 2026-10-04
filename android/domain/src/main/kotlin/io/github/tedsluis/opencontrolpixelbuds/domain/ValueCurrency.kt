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

/*
 * One rule for "is this value current?" (`ai-sessions/0069`, A68-APP-02; the maintainer's choice in chat 2026-10-03, "Keep the value, mark it"):
 * a value is current only while the session is Ready **and** the Buds reported it on this connection. A value that is not current stays visible —
 * dimmed, with the (i) dot and "from the last connection" in its details — and is never offered to the tile or the notification as the Buds' state.
 * Every tab, the Quick Settings tile and the notification use these functions; none has a rule of its own.
 */

/** The Buds reported the value at or after [sessionSinceMillis], the moment the current connection was requested ([BudsRepository.sessionSince]). */
fun isFromThisSession(valueAtMillis: Long?, sessionSinceMillis: Long?): Boolean =
    valueAtMillis != null && sessionSinceMillis != null && valueAtMillis >= sessionSinceMillis

/** [ready] = the session is [ConnectionState.Ready]. */
fun isCurrent(ready: Boolean, valueAtMillis: Long?, sessionSinceMillis: Long?): Boolean =
    ready && isFromThisSession(valueAtMillis, sessionSinceMillis)

/** The ANC mode while it is current, `null` otherwise — what the tile and the notification may show as the Buds' mode. */
fun currentAncMode(
    connectionState: Flow<ConnectionState>,
    ancMode: Flow<AncMode?>,
    ancModeUpdatedAt: Flow<Long?>,
    sessionSince: Flow<Long?>,
): Flow<AncMode?> = combine(connectionState, ancMode, ancModeUpdatedAt, sessionSince) { state, mode, updatedAt, since ->
    mode?.takeIf { isCurrent(state is ConnectionState.Ready, updatedAt, since) }
}

/** [currentAncMode] of a repository. */
fun BudsRepository.currentAncMode(): Flow<AncMode?> = currentAncMode(connectionState, ancMode, ancModeUpdatedAt, sessionSince)
