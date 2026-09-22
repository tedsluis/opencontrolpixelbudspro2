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
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.DockState
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
    batteryStatusUpdatedAt: Long?,
    caseBatteryError: BudsError?,
    dockState: DockState,
    dockStateUpdatedAt: Long?,
    deviceInfo: DeviceInfo?,
    onRefreshBattery: () -> Unit,
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
                        ConnectionStateCard(card, connectionState, lastConnectionError, messageStreamError, androidLink, onConnect, onDisconnect)
                    }
                    if (connectionState is ConnectionState.Ready) {
                        BudsInfoCard(dockState, dockStateUpdatedAt, deviceInfo)
                        BatteryCard(batteryStatus, batteryStatusUpdatedAt, caseBatteryError, onRefreshBattery)
                    }
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
    androidLink: AndroidLink,
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
                    AndroidLine.UNKNOWN -> "Paired — Android's connection state isn't known (yet)"
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
                    lastConnectionError?.let { ErrorExplanation(it, androidLink) }
                }
                SessionLine.OPENING -> Text("App control: connecting…")
                SessionLine.FAILED -> (session as? ConnectionState.Failed)?.let { ErrorExplanation(it.error, androidLink) }
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
 * smaller line — so "why did it fail" is visible without exporting a log (ai-sessions/0039).
 * [androidLink] sharpens a [BudsError.ChannelLost] message (`ai-sessions/0043`, `CAP-060-FINDINGS.md`
 * §1): whether Android itself still reports the Buds connected at display time distinguishes an
 * RFCOMM-only closure (the Buds/another app closed this app's own channel, audio unaffected) from a
 * full link loss (Android disconnected too — e.g. a tap in Android's own Bluetooth settings, or the
 * Buds going out of range). This reads Android's *current* link state, not a snapshot taken at the
 * moment of the drop, so it can be wrong if the link state changed again before the user looks — still
 * a strictly more specific answer than the generic message it replaces. */
@Composable
private fun ErrorExplanation(error: BudsError, androidLink: AndroidLink) {
    Text(error.userMessage(androidLink))
    error.technicalDetail()?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
}

/** Dock state (ADR-024) and firmware (ADR-034's announcement) — both passive, nothing sent for them (`ai-sessions/0042`). */
@Composable
private fun BudsInfoCard(dockState: DockState, dockStateUpdatedAt: Long?, deviceInfo: DeviceInfo?) {
    val dock = dockLine(dockState, dockStateUpdatedAt)
    val firmware = deviceInfo?.firmware?.takeIf { it.isNotEmpty() }?.joinToString(" / ")
    if (dock == null && firmware == null) return
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            dock?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            firmware?.let { Text("Firmware: $it", style = MaterialTheme.typography.bodyLarge) }
        }
    }
}

/** The dock line, or `null` when the state is not known — never guessed (`AGENTS.md` §5's spirit, ADR-024).
 * `ai-sessions/0043` Phase H: an actual timestamp replaces the vague "as of the last update" qualifier. */
internal fun dockLine(dockState: DockState, dockStateUpdatedAt: Long? = null): String? {
    val updated = formatUpdatedAt(dockStateUpdatedAt)?.let { " (updated $it)" } ?: ""
    return when (dockState) {
        DockState.BOTH_IN_CASE -> "Both earbuds are in the case$updated."
        DockState.NOT_BOTH_IN_CASE -> "At least one earbud is out of the case$updated."
        DockState.UNKNOWN -> null
    }
}

@Composable
private fun BatteryCard(status: BatteryStatus, batteryStatusUpdatedAt: Long?, caseBatteryError: BudsError?, onRefreshBattery: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Battery", style = MaterialTheme.typography.titleMedium)
            Text(
                "Read from the Buds when the app last reached their Message Stream (at Connect and after each ANC / Find action). " +
                    "\"Charging\" means the earbud reports it is charging.",
                style = MaterialTheme.typography.bodySmall,
            )
            BatteryRow("Left", status.left, batteryStatusUpdatedAt)
            BatteryRow("Right", status.right, batteryStatusUpdatedAt)
            BatteryRow("Case", status.case, batteryStatusUpdatedAt)
            if (status.case is BatteryLevel.Unavailable) caseBatteryError?.let { Text(caseErrorText(it), style = MaterialTheme.typography.bodySmall) }
            TextButton(onClick = onRefreshBattery) { Text("Refresh battery") }
        }
    }
}

@Composable
private fun BatteryRow(label: String, level: BatteryLevel, updatedAt: Long?) {
    val text = when (level) {
        is BatteryLevel.Known -> batteryText(label, level, updatedAt)
        BatteryLevel.Unavailable -> "$label: Battery unavailable"
    }
    Text(text, style = MaterialTheme.typography.bodyLarge)
}

/** Distinct, actionable copy per [BudsError] case (AGENTS.md §8). [androidLink], when known, sharpens
 * [BudsError.ChannelLost] (`ai-sessions/0043`, see [ErrorExplanation]'s doc comment). */
internal fun BudsError.userMessage(androidLink: AndroidLink? = null): String = when (this) {
    BudsError.ConnectionLost -> "Connection lost."
    BudsError.Timeout -> "The Buds didn't respond in time."
    is BudsError.MalformedFrame -> "Received an unexpected response from the Buds."
    BudsError.UnsupportedFirmware -> "This firmware version isn't recognized — running in read-only Safe Mode."
    BudsError.PermissionDenied -> "Bluetooth permission is required."
    is BudsError.ChannelUnavailable ->
        "Couldn't open the ${channelLabel(channelId)}. Another app on this phone — for example Google " +
            "Play services' Fast Pair — may already be using it. Wait a few seconds, then try again."
    is BudsError.ChannelLost -> when (androidLink) {
        AndroidLink.CONNECTED ->
            "The ${channelLabel(channelId)} was closed while Android still shows the Buds connected — likely " +
                "another app on this phone (for example Google Play services) or the Buds themselves closed it. Tap Connect to reconnect."
        AndroidLink.NOT_CONNECTED ->
            "Android no longer shows the Buds connected to this phone — this may be a disconnect from Android's " +
                "own Bluetooth settings, or the Buds went out of range. Tap Connect to reconnect."
        AndroidLink.UNKNOWN, null ->
            "The ${channelLabel(channelId)} was closed. Another app may have taken it over, or the Buds " +
                "dropped it. Tap Connect to reconnect."
    }
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
    0x08 -> "Case-battery channel"
    else -> "Bluetooth channel 0x%02x".format(channelId)
}

/** "Left: 97%", plus " (charging)" and an actual timestamp — "last seen HH:mm:ss" for a value the Buds did not mark
 * fresh (DECISIONS.md ADR-035), "updated HH:mm:ss" otherwise (`ai-sessions/0043` Phase H replaces the bare
 * "— last seen" qualifier, which carried no time, with the actual wall-clock time the value was read). */
internal fun batteryText(label: String, level: BatteryLevel.Known, updatedAt: Long? = null): String {
    val time = formatUpdatedAt(updatedAt)
    val suffix = when {
        level.isStale && time != null -> " — last seen $time"
        level.isStale -> " — last seen"
        time != null -> " (updated $time)"
        else -> ""
    }
    return "$label: ${level.percent}%" + (if (level.isCharging == true) " (charging)" else "") + suffix
}

/** Why the Case could not be read (ADR-035) — the Case is on a different, shared channel than ANC/Find. */
internal fun caseErrorText(error: BudsError): String = when (error) {
    is BudsError.ChannelUnavailable ->
        "The Case channel couldn't be opened — another app on this phone (for example Google Play services) may be using it. Try Refresh battery again."
    BudsError.Timeout -> "The Buds didn't report the Case level (they may not send it while the case is closed or the buds are out)."
    BudsError.ConnectionLost -> "The Case level can only be read while the app is connected."
    else -> "The Case level couldn't be read."
}
