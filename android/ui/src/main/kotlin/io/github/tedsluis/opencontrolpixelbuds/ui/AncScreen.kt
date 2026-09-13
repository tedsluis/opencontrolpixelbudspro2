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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState

/**
 * Minimal, stateless ANC control screen — just enough to wire the pipeline
 * end-to-end (transport -> framing -> UI), per this session's own scope note
 * (`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phase 7). Deliberately
 * takes plain domain values, not a ViewModel — wiring a real
 * `BudsViewModel`/`ToggleAncUseCase` (ARCHITECTURE.md §2) needs the `:app`
 * composition root, which is gated on Phase 6's dependency-injection
 * decision (Hilt vs. manual service locator).
 */
@Composable
fun AncScreen(
    connectionState: ConnectionState,
    ancMode: AncMode?,
    onAncModeSelected: (AncMode) -> Unit,
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
            Text(
                text = "Connection: ${connectionState::class.simpleName}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "ANC mode: ${ancMode?.name ?: "unknown"}",
                style = MaterialTheme.typography.bodyLarge,
            )
            AncMode.entries.forEach { mode ->
                Button(onClick = { onAncModeSelected(mode) }) {
                    Text(mode.name)
                }
            }
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
        )
    }
}
