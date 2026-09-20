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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget

/**
 * Find My Buds screen (ARCHITECTURE.md §2.4/§6). **Left/Right only, by
 * design** — DECISIONS.md ADR-027 makes Case ring and "ring both
 * simultaneously" a permanent, deliberate v1 exclusion, not a placeholder for
 * future work, so this screen has no disabled-looking "Case"/"Both"
 * affordance that would imply a capability this project does not provide.
 */
@Composable
fun FindMyBudsScreen(
    connectionState: ConnectionState,
    messageStreamError: BudsError?,
    ringingTarget: RingTarget?,
    onRing: (RingTarget) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Find My Buds", style = MaterialTheme.typography.headlineSmall)
            NotConnectedBanner(connectionState)
            MessageStreamNotice(messageStreamError)
            RingingNotice(ringingTarget, connectionState.isReady())
            Button(onClick = { onRing(RingTarget.LEFT) }, enabled = connectionState.isReady()) { Text("Ring Left") }
            Button(onClick = { onRing(RingTarget.RIGHT) }, enabled = connectionState.isReady()) { Text("Ring Right") }
            OutlinedButton(onClick = onStop, enabled = connectionState.isReady()) { Text("Stop") }
            MessageStreamHint()
        }
    }
}

/**
 * What the app knows about a running ring (`ai-sessions/0042`): the ring keeps sounding on the Buds after the Message Stream channel is
 * released (heard on the recording) until Stop is sent, so after a Ring tap the screen says so instead of showing nothing.
 */
@Composable
internal fun RingingNotice(ringingTarget: RingTarget?, sessionReady: Boolean) {
    if (ringingTarget == null) return
    val side = if (ringingTarget == RingTarget.LEFT) "Left" else "Right"
    Text(
        if (sessionReady) "Ringing: $side earbud — tap Stop to end it." else "A ring was started on the $side earbud — reconnect and tap Stop to end it.",
        style = MaterialTheme.typography.titleMedium,
    )
}
