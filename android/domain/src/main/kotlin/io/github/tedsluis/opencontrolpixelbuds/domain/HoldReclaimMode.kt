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
 * The Settings tab's "Noise-control channel" choice (DECISIONS.md ADR-062, `ai-sessions/0087`; the maintainer's choice in chat 2026-10-12): what the app does
 * when another client — Google Play services' Fast Pair on a phone that has it (`CAP-074`: ≈ 5 s after it lost the channel to this app) — closes the Message
 * Stream claim the app holds while the noise-control tab is on screen (ADR-061).
 *
 * - [HINT_ONLY] (the default): the hold ends and nothing re-claims (ADR-061 as written); the noise-control card says the channel was taken and when; the next tap,
 *   Refresh, pull or re-entry of the tab claims again.
 * - [RECLAIM]: the app claims again 3, 6 and 12 s after the first, second and third foreign close of one visit of the tab — the existing claim with its `Get`
 *   (nothing new on the wire) — then stops and shows the hint; never in the background, never after a session loss, never when the tab or the app was left.
 *
 * Stored locally (AndroidX DataStore, `:data/settings`), never sent anywhere.
 */
enum class HoldReclaimMode {
    HINT_ONLY,
    RECLAIM,
    ;

    companion object {
        /** The maintainer's choice in chat 2026-10-12 ("Default mode" → "Hint only"): no tug of war with another app unless the user opts in. */
        val DEFAULT: HoldReclaimMode = HINT_ONLY

        /** The stored name back to the setting; anything unknown (or nothing stored) is the default — never a crash on an old or damaged value. */
        fun fromStored(name: String?): HoldReclaimMode = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/**
 * The noise-control card's hint (ADR-062 item 2): the Message Stream claim the app held while the noise-control tab was shown was closed by someone else at
 * [atMillis] — in mode [HoldReclaimMode.HINT_ONLY] at the first such close, in mode [HoldReclaimMode.RECLAIM] once the re-claims are exhausted
 * ([reclaimAttempts] = the re-claims made, 0 in the hint-only mode). Cleared when the app holds the channel again, at Connect, Disconnect and a session loss.
 */
data class ChannelTaken(val atMillis: Long, val reclaimAttempts: Int)
