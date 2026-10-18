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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.RingNotice
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget

/**
 * Find My Buds screen (ARCHITECTURE.md §2.4/§6): Ring Left, Ring Right and — since DECISIONS.md ADR-070 (`ai-sessions/0098`, superseding ADR-027 for
 * "both") — Ring both; the Case stays out of scope and has no affordance. **DECISIONS.md ADR-069:** the notice follows the Buds' own ringing status, and a
 * stop they report without the app's Stop says "Stopped on the bud at …" ([ringStoppedOnBudAt]). **ADR-070 item 1:** one caption above the buttons says to
 * take the Buds out of the ears first (none existed before 1.4.0; it covers all three rings).
 * **`ai-sessions/0057`:** tonal Ring buttons and an outlined Stop; the ringing notice stays visible, the channel explanation is in the card's (i).
 */
@Composable
fun FindMyBudsScreen(
    connectionState: ConnectionState,
    messageStreamError: BudsError?,
    ringing: RingNotice?,
    onRing: (RingTarget) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    ringStoppedOnBudAt: Long? = null,
) {
    val ready = connectionState.isReady()
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NotConnectedBanner(connectionState)
            MessageStreamNotice(messageStreamError)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardTitle("Find My Buds", listOf(MESSAGE_STREAM_HINT_TEXT))
                    RingingNotice(ringing, ready)
                    if (ringing == null) ringStoppedOnBudAt?.let { Text(ringStoppedOnBudText(it), style = MaterialTheme.typography.bodyMedium) }
                    Text(RING_EARS_WARNING_TEXT, style = MaterialTheme.typography.bodyMedium)
                    val buttonModifier = Modifier.fillMaxWidth().height(56.dp)
                    FilledTonalButton(onClick = { onRing(RingTarget.LEFT) }, enabled = ready, modifier = buttonModifier) { Text("Ring Left") }
                    FilledTonalButton(onClick = { onRing(RingTarget.RIGHT) }, enabled = ready, modifier = buttonModifier) { Text("Ring Right") }
                    FilledTonalButton(onClick = { onRing(RingTarget.BOTH) }, enabled = ready, modifier = buttonModifier) { Text("Ring both") }
                    OutlinedButton(onClick = onStop, enabled = ready, modifier = buttonModifier) { Text("Stop") }
                }
            }
        }
    }
}

/**
 * What the app knows about a running ring (`ai-sessions/0042`): the ring keeps sounding on the Buds after the Message Stream channel is
 * released — and after Disconnect (`CAP-062`) — until Stop is sent, so after a Ring tap the screen says so instead of showing nothing. A ring
 * from an earlier session *may* have stopped by itself; the app cannot know, so it says "may still be ringing" (`ai-sessions/0048` I-6).
 */
@Composable
internal fun RingingNotice(ringing: RingNotice?, sessionReady: Boolean) {
    if (ringing == null) return
    Text(ringingNoticeText(ringing, sessionReady), style = MaterialTheme.typography.titleMedium)
}

internal fun ringingNoticeText(ringing: RingNotice, sessionReady: Boolean): String {
    val which = when (ringing.target) {
        RingTarget.LEFT -> "the Left earbud"
        RingTarget.RIGHT -> "the Right earbud"
        RingTarget.BOTH -> "both earbuds"
    }
    return when {
        !sessionReady -> "A ring was started on $which — reconnect and tap Stop to end it."
        ringing.fromEarlierSession -> "A ring was started on $which before the app reconnected — it may still be ringing. Tap Stop to end it."
        // The 1.3.x wording without "the" ("Ringing: Right earbud"); both: "Ringing: Both earbuds".
        else -> "Ringing: ${which.removePrefix("the ").replaceFirstChar { it.uppercase() }} — tap Stop to end it."
    }
}

/** DECISIONS.md ADR-069: the Buds reported the ringing stopped without the app's Stop — touched on the bud, or their own time-out. */
internal fun ringStoppedOnBudText(at: Long): String = "Stopped on the bud" + (formatUpdatedAt(at)?.let { " at $it" } ?: "") + "."

/** DECISIONS.md ADR-070 item 1: the ring is loud — said once, above the three Ring buttons. */
internal const val RING_EARS_WARNING_TEXT: String = "Take the Buds out of your ears before ringing — the sound is loud."
