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
 * Find My Buds screen (ARCHITECTURE.md §2.4/§6). **Left/Right only, by
 * design** — DECISIONS.md ADR-027 makes Case ring and "ring both
 * simultaneously" a deliberate v1 exclusion, so this screen has no
 * disabled-looking "Case"/"Both" affordance that would imply a capability this
 * project does not provide. (ADR-027's 2026-09-24 Update: the spec's `0x03`
 * "ring both" exists but is untested here; offering it needs its own ADR.)
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
                    val buttonModifier = Modifier.fillMaxWidth().height(56.dp)
                    FilledTonalButton(onClick = { onRing(RingTarget.LEFT) }, enabled = ready, modifier = buttonModifier) { Text("Ring Left") }
                    FilledTonalButton(onClick = { onRing(RingTarget.RIGHT) }, enabled = ready, modifier = buttonModifier) { Text("Ring Right") }
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
    val side = if (ringing.target == RingTarget.LEFT) "Left" else "Right"
    return when {
        !sessionReady -> "A ring was started on the $side earbud — reconnect and tap Stop to end it."
        ringing.fromEarlierSession -> "A ring was started on the $side earbud before the app reconnected — it may still be ringing. Tap Stop to end it."
        else -> "Ringing: $side earbud — tap Stop to end it."
    }
}
