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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState

/**
 * Start destination (ARCHITECTURE.md §2.4) — every other screen assumes
 * `ConnectionState == Ready`. Renders a **distinct** message per
 * [BudsError] case (AGENTS.md §8 — no generic "Something went wrong"
 * catch-all where a more specific state exists) and the GrapheneOS
 * Bluetooth-disabled re-enable prompt (AGENTS.md §2, ARCHITECTURE.md §9.1)
 * as a native system prompt trigger, not a custom dialog.
 */
@Composable
fun ConnectionScreen(
    connectionState: ConnectionState,
    bluetoothEnabled: Boolean,
    hasBondedDevice: Boolean,
    batteryStatus: BatteryStatus,
    onRequestEnableBluetooth: () -> Unit,
    onPair: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
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
            Text("OpenControl for Pixel Buds", style = MaterialTheme.typography.headlineSmall)

            when {
                !bluetoothEnabled -> {
                    Text("Bluetooth is disabled.", style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = onRequestEnableBluetooth) { Text("Enable Bluetooth") }
                }

                !hasBondedDevice -> {
                    Text("No Pixel Buds Pro 2 paired yet.", style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = onPair) { Text("Pair a device") }
                }

                else -> {
                    ConnectionStateCard(connectionState, onConnect, onDisconnect)
                    if (connectionState is ConnectionState.Ready) {
                        BatteryCard(batteryStatus)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStateCard(
    state: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (state) {
                ConnectionState.Disconnected -> {
                    Text("Disconnected")
                    Button(onClick = onConnect) { Text("Connect") }
                }
                ConnectionState.Connecting -> Text("Connecting…")
                ConnectionState.Discovering -> Text("Discovering services…")
                ConnectionState.Ready -> {
                    Text("Connected")
                    TextButton(onClick = onDisconnect) { Text("Disconnect") }
                }
                is ConnectionState.Failed -> {
                    Text(state.error.userMessage())
                    Button(onClick = onConnect) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
private fun BatteryCard(status: BatteryStatus) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Battery", style = MaterialTheme.typography.titleMedium)
            BatteryRow("Left", status.left)
            BatteryRow("Right", status.right)
            BatteryRow("Case", status.case)
            if (status.left is BatteryLevel.Unavailable && status.right is BatteryLevel.Unavailable) {
                // HFP Option C (this session's only battery source) reports one earbud without a
                // confirmed side (BatteryStatus.hfpEarbud's own doc comment) — shown separately so
                // it's never mistaken for a confirmed Left/Right reading.
                BatteryRow("One earbud (side unknown)", status.hfpEarbud)
            }
        }
    }
}

@Composable
private fun BatteryRow(label: String, level: BatteryLevel) {
    val text = when (level) {
        is BatteryLevel.Known -> "$label: ${level.percent}%" + if (level.isCharging == true) " (charging)" else ""
        BatteryLevel.Unavailable -> "$label: Battery unavailable"
    }
    Text(text, style = MaterialTheme.typography.bodyLarge)
}

/** Distinct, actionable copy per [BudsError] case (AGENTS.md §8). */
internal fun BudsError.userMessage(): String = when (this) {
    BudsError.ConnectionLost -> "Connection lost."
    BudsError.Timeout -> "The Buds didn't respond in time."
    is BudsError.MalformedFrame -> "Received an unexpected response from the Buds."
    BudsError.UnsupportedFirmware -> "This firmware version isn't recognized — running in read-only Safe Mode."
    BudsError.PermissionDenied -> "Bluetooth permission is required."
    is BudsError.Unknown -> "Something went wrong: ${cause.message ?: cause::class.simpleName}"
}
