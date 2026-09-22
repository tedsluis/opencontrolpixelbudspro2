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
package io.github.tedsluis.opencontrolpixelbuds.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * `ai-sessions/0043` Phase H: every "(last known)"/"last seen" qualifier in the UI is replaced by the
 * actual wall-clock time a value was received, not a vague qualifier or a ticking relative counter
 * (`ARCHITECTURE.md` §6 — this app runs no fixed-interval recomposition timer, so a live "N seconds
 * ago" display is deliberately not used here).
 */
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val TIME_FORMATTER_WITH_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm:ss")

/**
 * Formats [epochMillis] as an absolute local time, e.g. "14:32:07", or "Sep 20, 14:32:07" when it
 * falls on a different calendar day than now (so a genuinely stale value reads as stale, not as if it
 * just happened). Returns `null` when [epochMillis] is `null` (no value received yet this app run).
 */
internal fun formatUpdatedAt(epochMillis: Long?, now: Instant = Instant.now()): String? {
    if (epochMillis == null) return null
    val zone = ZoneId.systemDefault()
    val at = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val nowAt = now.atZone(zone)
    val formatter = if (at.toLocalDate() == nowAt.toLocalDate()) TIME_FORMATTER else TIME_FORMATTER_WITH_DATE
    return formatter.format(at)
}
