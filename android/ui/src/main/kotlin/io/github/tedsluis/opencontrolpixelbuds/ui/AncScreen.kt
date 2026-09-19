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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState

/**
 * Minimal, stateless ANC control screen — takes plain domain values, not a
 * ViewModel, matching `MainActivity`'s own state-hoisting-in-the-Activity
 * pattern (its doc comment). [ancMode] is normally kept current without any
 * action here — `BudsRepositoryImpl` simply observes the peer's own
 * connect-time `Get`/`Notify` pair (ARCHITECTURE.md §3.1) — [onRefreshAncMode]
 * exposes the already-implemented `BudsRepository.refreshAncMode()` manual
 * re-query for the case that pair is ever missed or the value looks stale
 * (`ai-sessions/0038` — this method existed and was unit-tested since
 * `ai-sessions/0033` but had no UI affordance to actually reach it).
 */
@Composable
fun AncScreen(
    connectionState: ConnectionState,
    ancMode: AncMode?,
    onAncModeSelected: (AncMode) -> Unit,
    onRefreshAncMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NotConnectedBanner(connectionState)
            Text(
                text = "Connection: ${connectionState::class.simpleName}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "ANC mode: ${ancMode?.name ?: "unknown"}",
                style = MaterialTheme.typography.bodyLarge,
            )
            AncMode.entries.forEach { mode ->
                Button(onClick = { onAncModeSelected(mode) }, enabled = connectionState.isReady()) {
                    Text(mode.name)
                }
            }
            TextButton(onClick = onRefreshAncMode, enabled = connectionState.isReady()) { Text("Refresh") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AncScreenPreview() {
    MaterialTheme {
        AncScreen(
            connectionState = ConnectionState.Ready,
            ancMode = AncMode.ADAPTIVE,
            onAncModeSelected = {},
            onRefreshAncMode = {},
        )
    }
}
