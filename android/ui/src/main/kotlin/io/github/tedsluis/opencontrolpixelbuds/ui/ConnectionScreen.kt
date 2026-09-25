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
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.CardAction
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.SafeModeState
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLine
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
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
    deviceInfo: DeviceInfo?,
    onRefreshBattery: () -> Unit,
    lastLossCause: SessionLossCause?,
    onRequestEnableBluetooth: () -> Unit,
    onPair: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
    safeMode: SafeModeState? = null,
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
                        ConnectionStateCard(card, connectionState, lastConnectionError, lastLossCause, messageStreamError, onConnect, onDisconnect)
                    }
                    if (connectionState is ConnectionState.Ready) {
                        safeMode?.let { SafeModeCard(it) }
                        BudsInfoCard(deviceInfo)
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
    lastLossCause: SessionLossCause?,
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
                    lastConnectionError?.let { ErrorExplanation(it, lastLossCause) }
                }
                SessionLine.OPENING -> Text("App control: connecting…")
                SessionLine.FAILED -> (session as? ConnectionState.Failed)?.let { ErrorExplanation(it.error, null) }
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
 * [lossCause] sharpens a [BudsError.ChannelLost] message: it is derived from Android's link state *around* the loss
 * (`ai-sessions/0048` I-7, `classifySessionLoss`) — never from a reading older than the loss, which is what made the old text say
 * "while Android still shows the Buds connected" after the link had already gone (`CAP-062` 06:42:35). */
@Composable
private fun ErrorExplanation(error: BudsError, lossCause: SessionLossCause?) {
    Text(error.userMessage(lossCause))
    error.technicalDetail()?.let {
        Text(it, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Read-only Safe Mode (ARCHITECTURE.md §8.1, DECISIONS.md ADR-042): shown explicitly, never a silent limitation — what was detected and
 * why the controls send nothing.
 */
@Composable
private fun SafeModeCard(state: SafeModeState) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Safe Mode — read-only", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Text(safeModeText(state), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

internal fun safeModeText(state: SafeModeState): String {
    val detected = listOfNotNull(
        state.firmware?.takeIf { it.isNotEmpty() }?.let { "firmware ${it.joinToString(" / ")}" },
        state.modelIdHex?.let { "Fast Pair model $it" },
    ).joinToString(", ").ifEmpty { "nothing announced yet" }
    return "${state.reason} This app only sends ANC, EQ and Find My Buds commands to the Pixel Buds Pro 2 firmware it was " +
        "verified against, so those controls send nothing; battery, ANC state and EQ are still read. Detected: $detected."
}

/**
 * The firmware the Buds announced (ADR-034) — passive, nothing sent for it. No dock sentence any more: the `Notify` byte it was derived
 * from read "both in the case" with one earbud out in `CAP-061` (DECISIONS.md ADR-024 Update 2026-09-24, `ai-sessions/0046`); each
 * earbud's "(charging)" on the battery lines stays.
 */
@Composable
private fun BudsInfoCard(deviceInfo: DeviceInfo?) {
    val firmware = deviceInfo?.firmware?.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: return
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Firmware: $firmware", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun BatteryCard(status: BatteryStatus, batteryStatusUpdatedAt: Long?, caseBatteryError: BudsError?, onRefreshBattery: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Battery", style = MaterialTheme.typography.titleMedium)
            Text(
                "Left/Right %: read from the Buds when the app last reached their Message Stream (at Connect, Refresh and each ANC / Find " +
                    "action). Charging and the Case: sent by the Buds on their own whenever a bud goes into or out of the case.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(budLine("Left", status.left, status.leftCharging, batteryStatusUpdatedAt), style = MaterialTheme.typography.bodyLarge)
            Text(budLine("Right", status.right, status.rightCharging, batteryStatusUpdatedAt), style = MaterialTheme.typography.bodyLarge)
            Text(caseLine(status.case, batteryStatusUpdatedAt), style = MaterialTheme.typography.bodyLarge)
            if (status.case is BatteryLevel.Unavailable) {
                Text(caseBatteryError?.let(::caseErrorText) ?: CASE_NOT_REPORTED, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onRefreshBattery) { Text("Refresh battery") }
        }
    }
}

/** Distinct, actionable copy per [BudsError] case (AGENTS.md §8). [lossCause], when known, sharpens
 * [BudsError.ChannelLost] (`ai-sessions/0048` I-7, see [ErrorExplanation]'s doc comment). */
internal fun BudsError.userMessage(lossCause: SessionLossCause? = null): String = when (this) {
    BudsError.ConnectionLost -> "Connection lost."
    BudsError.Timeout -> "The Buds didn't respond in time."
    is BudsError.MalformedFrame -> "Received an unexpected response from the Buds."
    BudsError.UnsupportedFirmware ->
        "Safe Mode: nothing was sent — the Buds' firmware or model isn't one this app was verified against (read-only)."
    BudsError.PermissionDenied -> "Bluetooth permission is required."
    BudsError.NotPaired -> "No paired Pixel Buds found. Pair them first (Pair a device, or Android's Bluetooth settings)."
    is BudsError.CommandRejected -> "The Buds refused the command ($detail)."
    BudsError.AncNotAllowed -> ANC_NOT_ALLOWED_TEXT
    is BudsError.ChannelUnavailable ->
        "Couldn't open the ${channelLabel(channelId)}. Another app on this phone — for example Google " +
            "Play services' Fast Pair — may already be using it. Wait a few seconds, then try again."
    is BudsError.ChannelLost -> channelLostMessage(channelId, lossCause)
    is BudsError.MaestroChannelUnknown ->
        if (channelId == null) {
            "The Buds didn't announce their control channel in time, so nothing was sent."
        } else {
            "The Buds announced control channel $channelId, which this app has no known address for, so nothing was sent."
        }
    is BudsError.MaestroRejected -> "The Buds rejected the request ($detail)."
    is BudsError.Unknown -> "Something went wrong: ${cause.message ?: cause::class.simpleName}"
}

/**
 * I-7 (`ai-sessions/0048`): what the evidence says, per [SessionLossCause]. Never "likely another app" — no other app ever contended for the
 * MAESTRO channel (DECISIONS.md ADR-032), and in `CAP-062` 7 of 14 session ends were the Buds closing it at a wear/dock change.
 */
internal fun channelLostMessage(channelId: Int, lossCause: SessionLossCause?): String = when (lossCause) {
    SessionLossCause.BUDS_CLOSED_CHANNEL ->
        "The Buds closed the app's channel (this happens when a bud goes in or out of the case or an ear). Android still shows the Buds " +
            "connected; the app reopens its channel by itself while it is on screen, or tap Connect."
    SessionLossCause.ANDROID_LINK_LOST ->
        "Android no longer shows the Buds connected to this phone — after a disconnect in Android's own Bluetooth settings, with both buds " +
            "in the case, or out of range. Tap Connect to reconnect."
    SessionLossCause.UNDETERMINED, null -> "The ${channelLabel(channelId)} was closed. Tap Connect to reconnect."
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

/**
 * One bud's line (`ai-sessions/0048` I-4/I-8): the percentage with the time DLCI 0x04 reported it, then the newest charging report of either
 * source with **its own** time — "charging in the case" (🟡: charging is FACT, "in the case" is not, PROTOCOL.md §4.3 Option F) or "not charging".
 */
internal fun budLine(label: String, level: BatteryLevel, charging: ChargingReading?, fallbackUpdatedAt: Long? = null): String {
    val percent = when (level) {
        is BatteryLevel.Known -> batteryText(label, level, level.receivedAtMillis ?: fallbackUpdatedAt)
        BatteryLevel.Unavailable -> "$label: Battery unavailable"
    }
    val chargingPart = charging?.let {
        val time = formatUpdatedAt(it.atMillis)?.let { t -> " ($t)" } ?: ""
        (if (it.charging) " — charging in the case" else " — not charging (out of the case)") + time
    } ?: ""
    return percent + chargingPart
}

/**
 * The Case line (`ai-sessions/0048` I-5): the current value with its time, or the last value the Buds reported, marked "last seen" with that time —
 * they stop sending it when no bud is charging (🟢, PROTOCOL.md §4.3 Option F). Never a value without its time (AGENTS.md §5).
 */
internal fun caseLine(level: BatteryLevel, fallbackUpdatedAt: Long? = null): String = when (level) {
    is BatteryLevel.Known -> {
        val time = formatUpdatedAt(level.receivedAtMillis ?: fallbackUpdatedAt)
        if (level.isStale) {
            "Case: ${level.percent}% — last seen" + (time?.let { " $it" } ?: "") + " (no bud charging in the case)"
        } else {
            "Case: ${level.percent}%" + (time?.let { " (updated $it)" } ?: "")
        }
    }
    BatteryLevel.Unavailable -> "Case: Battery unavailable"
}

/** "Left: 97%" plus the time it was read — "updated HH:mm:ss", or "last seen HH:mm:ss" for a value that is not current (`ai-sessions/0043`
 * Phase H). The charging state is not part of it any more: it has its own time ([budLine], `ai-sessions/0048` I-8). */
internal fun batteryText(label: String, level: BatteryLevel.Known, updatedAt: Long? = null): String {
    val time = formatUpdatedAt(updatedAt)
    val suffix = when {
        level.isStale && time != null -> " — last seen $time"
        level.isStale -> " — last seen"
        time != null -> " (updated $time)"
        else -> ""
    }
    return "$label: ${level.percent}%$suffix"
}

/**
 * I-3 (`ai-sessions/0048`): shown while the Buds' last `Notify` reports Settable `0x00`. The wording follows DECISIONS.md ADR-024's 2026-09-25
 * Update — 🟡 "not worn", never "in the case" (`CAP-062`: `0x00` also with both buds on the table).
 */
internal const val ANC_NOT_ALLOWED_TEXT: String = "ANC can only be changed while you wear the Buds."

/** No Case value has been reported since the app started (I-5: a reported value stays as "last seen") — never guessed. */
internal const val CASE_NOT_REPORTED: String =
    "Not reported yet — the Buds send the Case level only while a bud is charging in the case."

/** Why the Case level was not requested (ADR-043: one runtime-info request per Connect, on the channel the Buds announced). */
internal fun caseErrorText(error: BudsError): String = "The Case level couldn't be requested: ${error.userMessage()}"
