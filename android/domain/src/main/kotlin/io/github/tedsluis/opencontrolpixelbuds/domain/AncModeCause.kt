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
 * Why the shown [AncMode] is what it is (`ai-sessions/0082`, item 1 — `CAP-069-FINDINGS.md` §12 item 8; the maintainer's choice in chat 2026-10-09). The Buds
 * push `Notify ANC state` for every change, whatever caused it (PROTOCOL.md §4.1): the app's own `Set`, a press-and-hold on a bud (`CAP-045`), or the Buds'
 * own change (`CAP-067` §2). The cause is derived from what the app itself had just sent — never from a timer:
 *
 * - [READ] — the `Notify` answered the app's own `Get` (the Connect snapshot, Refresh, the `Get` before every tap), or it is the first mode this app run
 *   ever showed: a reading, not a change.
 * - [SET_BY_APP] — the Buds' ACK of the app's `Set`, or their `Notify` of the requested mode while that `Set` was still waiting for its answer.
 * - [CHANGED_BY_BUDS] — any other `Notify` whose mode differs from the shown one: a press-and-hold on a bud, or the Buds' own change — the app cannot
 *   tell which, and says so.
 *
 * A `Notify` with the mode already shown, while nothing is waiting, leaves the cause as it is (only the mode's time moves). Reset at every Connect.
 */
enum class AncModeCause { READ, SET_BY_APP, CHANGED_BY_BUDS }
