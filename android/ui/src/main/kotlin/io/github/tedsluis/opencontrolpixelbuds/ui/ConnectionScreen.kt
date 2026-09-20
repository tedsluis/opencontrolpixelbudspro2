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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLine
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.CardAction
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLine
import io.github.tedsluis.opencontrolpixelbuds.domain.StatusCard
import io.github.tedsluis.opencontrolpixelbuds.domain.statusCard

/**
 * Start destination (ARCHITECTURE.md §2.4) — every other screen assumes `ConnectionState == Ready`. Renders a **distinct**
 * message per [BudsError] case (AGENTS.md §8 — no generic "Something went wrong" catch-all where a more specific state
 * exists) and the GrapheneOS Bluetooth-disabled re-enable prompt (AGENTS.md §2, ARCHITECTURE.md §9.1) as a native system
 * prompt trigger, not a custom dialog.
 *
 * **`ai-sessions/0041` — mirrors Android:** the top of the screen shows the Buds' state *as Android's Bluetooth settings do*
 * (paired / connected to this phone), with this app's own control session as a separate, secondary line. A missing
 * Bluetooth permission has its **own** panel — it is never presented as "No Pixel Buds paired yet".
 */
@Composable
fun ConnectionScreen(
    connectionState: ConnectionState,
    deviceStatus: DeviceStatus,
    androidLink: AndroidLink,
    permissionState: PermissionState,
    pairingStatusText: String?,
    lastConnectionError: BudsError?,
    messageStreamError: BudsError?,
    batteryStatus: BatteryStatus,
    onRequestEnableBluetooth: () -> Unit,
    onPair: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("OpenControl for Pixel Buds", style = MaterialTheme.typography.headlineSmall)

            when (deviceStatus) {
                DeviceStatus.BluetoothOff -> {
                    Text("Bluetooth is disabled.", style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = onRequestEnableBluetooth) { Text("Enable Bluetooth") }
                }

                is DeviceStatus.PermissionMissing -> PermissionPanel(deviceStatus.status, onRequestPermissions, onOpenAppSettings)

                DeviceStatus.NotPaired -> {
                    Text("No Pixel Buds Pro 2 paired yet.", style = MaterialTheme.typography.bodyLarge)
                    Button(onClick = onPair) { Text("Pair a device") }
                    // Status/error feedback for the pairing attempt itself (ai-sessions/0036, /0041).
                    pairingStatusText?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }

                else -> {
                    val card = statusCard(deviceStatus, androidLink, connectionState)
                    if (card != null) {
                        ConnectionStateCard(card, connectionState, lastConnectionError, messageStreamError, onConnect, onDisconnect)
                    }
                    if (connectionState is ConnectionState.Ready) BatteryCard(batteryStatus)
                }
            }

            if (deviceStatus !is DeviceStatus.PermissionMissing && !permissionState.postNotifications.isGranted) {
                NotificationsHint(permissionState.postNotifications, onRequestPermissions, onOpenAppSettings)
            }
        }
    }
}

/**
 * The Bluetooth permission is its own state (AGENTS.md §8): a distinct explanation and the right next step for each of
 * "not asked yet", "denied" and "blocked" (only the system settings can grant it; on GrapheneOS the group is *Nearby devices*).
 */
@Composable
private fun PermissionPanel(status: PermissionStatus, onRequest: () -> Unit, onOpenSettings: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Bluetooth permission needed", style = MaterialTheme.typography.titleMedium)
            Text(
                "This app needs the \"Nearby devices\" permission to see your paired Pixel Buds and to talk to them. " +
                    "It is used for nothing else, and the app never uses the network.",
                style = MaterialTheme.typography.bodyMedium,
            )
            when (status) {
                PermissionStatus.PERMANENTLY_DENIED -> {
                    Text(
                        "The permission is blocked. Open the app's settings and allow \"Nearby devices\" (on Android: Permissions → Nearby devices).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = onOpenSettings) { Text("Open app settings") }
                }
                PermissionStatus.DENIED -> {
                    Text("You denied the permission.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onRequest) { Text("Allow") }
                }
                else -> Button(onClick = onRequest) { Text("Allow") }
            }
        }
    }
}

@Composable
private fun NotificationsHint(status: PermissionStatus, onRequest: () -> Unit, onOpenSettings: () -> Unit) {
    Text(
        "Notifications are off, so the \"connected\" notification can't be shown while the app controls your Buds. This does not affect anything else.",
        style = MaterialTheme.typography.bodySmall,
    )
    if (status == PermissionStatus.PERMANENTLY_DENIED) {
        TextButton(onClick = onOpenSettings) { Text("Open app settings") }
    } else {
        TextButton(onClick = onRequest) { Text("Allow notifications") }
    }
}

@Composable
private fun ConnectionStateCard(
    card: StatusCard,
    session: ConnectionState,
    lastConnectionError: BudsError?,
    messageStreamError: BudsError?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Line 1 — Android's own view, the same as its Bluetooth settings.
            Text(
                when (card.android) {
                    AndroidLine.CONNECTED -> "Connected to this phone (Android)"
                    AndroidLine.NOT_CONNECTED -> "Paired — not connected to this phone"
                    AndroidLine.NOT_REPORTED_WHILE_CONTROLLED -> "Android doesn't show the Buds as connected (yet)"
                },
                style = MaterialTheme.typography.titleMedium,
            )
            if (card.android == AndroidLine.NOT_CONNECTED) {
                Text(
                    "Open the case or put the Buds in your ears. Connect will still try.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            // Line 2 — this app's own control session, secondary.
            when (card.session) {
                SessionLine.OPEN -> Text("App control: ready")
                SessionLine.NOT_OPEN -> {
                    Text("App control: not open yet — tap Connect to control the Buds from this app.")
                    // Never a bare "not open" after an unexpected drop (ai-sessions/0039): say why.
                    lastConnectionError?.let { ErrorExplanation(it) }
                }
                SessionLine.OPENING -> Text("App control: connecting…")
                SessionLine.FAILED -> (session as? ConnectionState.Failed)?.let { ErrorExplanation(it.error) }
            }
            if (card.session == SessionLine.OPEN) messageStreamError?.let { MessageStreamNotice(it) }
            when (card.action) {
                CardAction.CONNECT -> Button(onClick = onConnect) { Text("Connect") }
                CardAction.RETRY -> Button(onClick = onConnect) { Text("Retry") }
                CardAction.DISCONNECT -> TextButton(onClick = onDisconnect) { Text("Disconnect") }
                CardAction.NONE -> Unit
            }
        }
    }
}

/** The plain-language message plus, when the error carries one, the raw technical detail in a
 * smaller line — so "why did it fail" is visible without exporting a log (ai-sessions/0039). */
@Composable
private fun ErrorExplanation(error: BudsError) {
    Text(error.userMessage())
    error.technicalDetail()?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BatteryCard(status: BatteryStatus) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Battery", style = MaterialTheme.typography.titleMedium)
            Text(
                "Read from the Buds when the app last reached their Message Stream (at Connect and after each ANC / Find action). " +
                    "\"Charging\" means the earbud reports it is charging.",
                style = MaterialTheme.typography.bodySmall,
            )
            BatteryRow("Left", status.left)
            BatteryRow("Right", status.right)
            BatteryRow("Case", status.case)
            if (status.left is BatteryLevel.Unavailable && status.right is BatteryLevel.Unavailable &&
                status.hfpEarbud !is BatteryLevel.Unavailable
            ) {
                // HFP Option C reports one earbud without a confirmed side (BatteryStatus.hfpEarbud's own doc
                // comment) — shown separately so it's never mistaken for a confirmed Left/Right reading, and hidden
                // while it has no value (ai-sessions/0041: a permanent "unavailable" row was only confusing).
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
    is BudsError.ChannelUnavailable ->
        "Couldn't open the ${channelLabel(channelId)}. Another app on this phone — for example Google " +
            "Play services' Fast Pair — may already be using it. Wait a few seconds, then try again."
    is BudsError.ChannelLost ->
        "The ${channelLabel(channelId)} was closed. Another app may have taken it over, or the Buds " +
            "dropped it. Tap Connect to reconnect."
    is BudsError.MaestroChannelUnknown ->
        if (channelId == null) {
            "The Buds didn't announce their control channel in time, so nothing was sent."
        } else {
            "The Buds announced control channel $channelId, which this app has no known address for, so nothing was sent."
        }
    is BudsError.MaestroRejected -> "The Buds rejected the request ($detail)."
    is BudsError.Unknown -> "Something went wrong: ${cause.message ?: cause::class.simpleName}"
}

/** The underlying exception text for the errors that carry one — shown small, never the only message. */
internal fun BudsError.technicalDetail(): String? = when (this) {
    is BudsError.ChannelUnavailable -> detail
    is BudsError.ChannelLost -> detail
    else -> null
}

/**
 * Human label for an RFCOMM channel id (`:data`'s `Dlci` constants — `:ui` cannot depend on `:data`,
 * ARCHITECTURE.md §2, so the two values are mirrored here: 0x02 = `Dlci.MAESTRO`, 0x04 =
 * `Dlci.FAST_PAIR_MESSAGE_STREAM`, PROTOCOL.md §2.3).
 */
internal fun channelLabel(channelId: Int): String = when (channelId) {
    0x02 -> "Maestro channel (equalizer)"
    0x04 -> "Message Stream channel (ANC, Find My Buds)"
    else -> "Bluetooth channel 0x%02x".format(channelId)
}
