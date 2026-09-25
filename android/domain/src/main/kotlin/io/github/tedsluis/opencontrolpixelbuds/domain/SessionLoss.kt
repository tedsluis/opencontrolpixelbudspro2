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
 * Why the app's session (the MAESTRO channel, DLCI 0x02) ended when the user did not ask for it — `ai-sessions/0048`, I-7. The transport cannot
 * tell: in `CAP-062` all 11 losses, Buds-side `DISC`s with the classic link up **and** ACL drops, carried the identical socket detail
 * `IOException: bt socket closed, read return: -1` (`CAP-062-debug-export.log`). The cause therefore comes from Android's own link state around
 * the loss ([classifySessionLoss]).
 */
enum class SessionLossCause {
    /** Android's link to the Buds went down around the loss: a disconnect in Android's Bluetooth settings, both buds into the case (ADR-016), range. */
    ANDROID_LINK_LOST,

    /** Android still showed the Buds connected right after the loss: the Buds closed the app's channel themselves (`CAP-062` §3: 7 of 14 ends, each
     * at a bud going into or out of the case or an ear). */
    BUDS_CLOSED_CHANNEL,

    /** No reading of Android's link close enough to the loss (the app was not on screen, or no permission) — nothing is claimed. */
    UNDETERMINED,
}

/** One evaluation of Android's link state ([AndroidLink]) and the wall-clock time it was taken. */
data class LinkReading(val link: AndroidLink, val atMillis: Long)

/**
 * Classifies a session loss at [lossAtMillis] from the [readings] of Android's link taken around it (any order). Evidence-based windows, measured in
 * `CAP-062` (`ai-sessions/0048` RESULT §4): Android-settings disconnects turned the link "not connected" 1.28 s and 1.48 s **before** the loss; the
 * ADR-016 ACL drops 0.11 s and 0.25 s **after** it; Buds-side `DISC`s with both buds going into the case were followed by the ACL drop only 3.0 s and
 * 4.1 s later (the Buds closed the channel first).
 *
 * 1. A "not connected" reading from [LINK_LOST_BEFORE_MS] before to [LINK_LOST_AFTER_MS] after the loss → [SessionLossCause.ANDROID_LINK_LOST].
 * 2. Otherwise the **first** reading taken at or after the loss (within [FRESH_READING_MS]) decides: connected → [SessionLossCause.BUDS_CLOSED_CHANNEL],
 *    not connected → [SessionLossCause.ANDROID_LINK_LOST]. A reading older than the loss is never used for this.
 * 3. Otherwise [SessionLossCause.UNDETERMINED].
 *
 * Pure and re-evaluated whenever a new reading arrives, so the text follows the evidence without any timer (ARCHITECTURE.md §6).
 */
fun classifySessionLoss(lossAtMillis: Long, readings: Collection<LinkReading>): SessionLossCause {
    val linkLost = readings.any {
        it.link == AndroidLink.NOT_CONNECTED && it.atMillis in (lossAtMillis - LINK_LOST_BEFORE_MS)..(lossAtMillis + LINK_LOST_AFTER_MS)
    }
    if (linkLost) return SessionLossCause.ANDROID_LINK_LOST
    val first = readings.filter { it.atMillis in lossAtMillis..(lossAtMillis + FRESH_READING_MS) }.minByOrNull { it.atMillis }
    return when (first?.link) {
        AndroidLink.CONNECTED -> SessionLossCause.BUDS_CLOSED_CHANNEL
        AndroidLink.NOT_CONNECTED -> SessionLossCause.ANDROID_LINK_LOST
        AndroidLink.UNKNOWN, null -> SessionLossCause.UNDETERMINED
    }
}

/** How long before a loss a "not connected" reading still counts (`CAP-062`: 1.28 s and 1.48 s). */
const val LINK_LOST_BEFORE_MS: Long = 2_000L

/** How long after a loss a "not connected" reading still counts (`CAP-062`: 0.11 s and 0.25 s; the Buds-first cases were 3.0 s / 4.1 s). */
const val LINK_LOST_AFTER_MS: Long = 1_000L

/** The first reading after the loss must come within this time to decide anything. */
const val FRESH_READING_MS: Long = 2_000L
