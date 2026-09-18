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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame

/**
 * Developer-facing Debug screen (AGENTS.md §6/§9, ARCHITECTURE.md §2.4/§7) —
 * reachable only via a non-primary entry point, never part of the main flow.
 * Shows every [UnidentifiedFrame] this session has seen (structurally valid
 * but unrecognized wire data, surfaced rather than silently dropped — this
 * project's own evidence-based reverse-engineering principle) and the Debug
 * Mode toggle that gates verbose hex-dump logging elsewhere in the app.
 */
@Composable
fun DebugScreen(
    debugModeEnabled: Boolean,
    onDebugModeChanged: (Boolean) -> Unit,
    unidentifiedFrames: List<UnidentifiedFrame>,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Debug", style = MaterialTheme.typography.headlineSmall)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Debug mode (verbose hex-dump logging)")
                Switch(checked = debugModeEnabled, onCheckedChange = onDebugModeChanged)
            }

            HorizontalDivider()

            Text("Unidentified frames (${unidentifiedFrames.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(unidentifiedFrames) { frame ->
                    Text(
                        "DLCI 0x%02x  Group=%s Code=%s  %d bytes".format(
                            frame.channelId,
                            frame.group?.let { "0x%02x".format(it) } ?: "?",
                            frame.code?.let { "0x%02x".format(it) } ?: "?",
                            frame.raw.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
