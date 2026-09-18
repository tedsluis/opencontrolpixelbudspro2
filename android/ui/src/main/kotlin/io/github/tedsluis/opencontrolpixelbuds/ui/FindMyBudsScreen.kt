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
            Button(onClick = { onRing(RingTarget.LEFT) }) { Text("Ring Left") }
            Button(onClick = { onRing(RingTarget.RIGHT) }) { Text("Ring Right") }
            OutlinedButton(onClick = onStop) { Text("Stop") }
        }
    }
}
