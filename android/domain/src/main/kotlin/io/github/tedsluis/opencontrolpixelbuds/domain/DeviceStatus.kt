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
package io.github.tedsluis.opencontrolpixelbuds.domain

/**
 * State of one runtime permission as this app can know it (`ai-sessions/0041`). Android's own API cannot tell
 * "never asked" from "permanently denied" (`shouldShowRequestPermissionRationale` is false for both), so the
 * distinction comes from whether this run already asked — see [permissionStatus].
 */
enum class PermissionStatus {
    GRANTED,
    NOT_REQUESTED,
    DENIED,
    PERMANENTLY_DENIED,
    ;

    val isGranted: Boolean get() = this == GRANTED
}

/**
 * Pure permission-state decision. [requestedThisRun] = a system prompt was already shown (or attempted) since the
 * process started; a denied permission whose rationale is no longer offered after such a request is permanent —
 * only the system settings can grant it (on GrapheneOS the group is called *Nearby devices*).
 */
fun permissionStatus(granted: Boolean, requestedThisRun: Boolean, shouldShowRationale: Boolean): PermissionStatus = when {
    granted -> PermissionStatus.GRANTED
    !requestedThisRun -> PermissionStatus.NOT_REQUESTED
    shouldShowRationale -> PermissionStatus.DENIED
    else -> PermissionStatus.PERMANENTLY_DENIED
}

/** The two runtime permissions this app asks for (AGENTS.md §2: nothing else). */
data class PermissionState(
    /** `BLUETOOTH_CONNECT` — required for every Bluetooth path (bonded devices, RFCOMM, profile state). */
    val bluetoothConnect: PermissionStatus,
    /** `POST_NOTIFICATIONS` — only the foreground-service notification; not required for anything else. */
    val postNotifications: PermissionStatus,
)

/** Android's own view of the bonded Buds: connected to this phone via an audio/hands-free profile, or not. */
enum class AndroidLink { UNKNOWN, NOT_CONNECTED, CONNECTED }

/**
 * The top-level status the Connection screen shows — Android's Bluetooth state first, this app's own control
 * session as a separate, secondary matter (`ai-sessions/0041`, the maintainer's "mirror Android" requirement).
 * Purely derived; see [deriveDeviceStatus].
 */
sealed class DeviceStatus {
    data object BluetoothOff : DeviceStatus()

    /** A missing/denied Bluetooth permission has its **own** state — never "not paired" (AGENTS.md §8). */
    data class PermissionMissing(val status: PermissionStatus) : DeviceStatus()
    data object NotPaired : DeviceStatus()

    /** Paired; Android does not (yet) report the Buds connected to this phone (case closed, not in the ears). */
    data object PairedNotConnected : DeviceStatus()

    /** Android's Bluetooth settings say connected; this app's control session is not open. */
    data object ConnectedToPhone : DeviceStatus()

    /** This app is opening its control session (a user tap on Connect). */
    data object Connecting : DeviceStatus()

    /** This app's control session is ready (ConnectionState.Ready). */
    data object ControlledByApp : DeviceStatus()
}

/**
 * Derives the [DeviceStatus] for every combination of inputs. Priority: Bluetooth off, then a missing permission
 * (nothing else can be known without it), then not paired, then this app's session (an open session is authoritative
 * for "controlled"), then Android's own link state. A [ConnectionState.Failed] session reads like a closed one — the
 * error itself is shown separately.
 */
fun deriveDeviceStatus(
    bluetoothEnabled: Boolean,
    bluetoothConnect: PermissionStatus,
    hasBondedDevice: Boolean,
    androidLink: AndroidLink,
    session: ConnectionState,
): DeviceStatus = when {
    !bluetoothEnabled -> DeviceStatus.BluetoothOff
    !bluetoothConnect.isGranted -> DeviceStatus.PermissionMissing(bluetoothConnect)
    !hasBondedDevice -> DeviceStatus.NotPaired
    session is ConnectionState.Ready -> DeviceStatus.ControlledByApp
    session is ConnectionState.Connecting || session is ConnectionState.Discovering -> DeviceStatus.Connecting
    androidLink == AndroidLink.CONNECTED -> DeviceStatus.ConnectedToPhone
    else -> DeviceStatus.PairedNotConnected
}

/** What the Android-state line of the Connection card says (`ai-sessions/0041`). */
enum class AndroidLine {
    /** Android's Bluetooth settings show the Buds connected to this phone. */
    CONNECTED,

    /** Paired, but Android does not show them connected (case closed / not in the ears). */
    NOT_CONNECTED,

    /** This app's session is open while Android does not (yet) report the Buds connected — the session wins, say so. */
    NOT_REPORTED_WHILE_CONTROLLED,

    /**
     * Android's link state could not be read (no permission yet, no bonded device yet, or the observer has not answered) —
     * **not** a claim that the Buds are disconnected (`ai-sessions/0042`: in the first hardware run the card said "not connected"
     * for minutes while Android's panel showed the Buds active, because an unknown state was rendered as a negative).
     */
    UNKNOWN,
}

/** What the app-session line says — always secondary to [AndroidLine]. */
enum class SessionLine { OPEN, NOT_OPEN, OPENING, FAILED }

enum class CardAction { CONNECT, RETRY, DISCONNECT, NONE }

/** The Connection card for one [DeviceStatus]: an Android line, an app-session line and the one prominent action. */
data class StatusCard(val android: AndroidLine, val session: SessionLine, val action: CardAction)

/**
 * The card for each combination of *Android state* × *app session* (`ai-sessions/0041`), or `null` for a status that has
 * no card (Bluetooth off, permission missing, not paired — those have their own panels). Examples: Android connected +
 * session closed → "Connected to this phone (Android)" / "not controlled by the app yet" with a prominent **Connect**;
 * a failed session offers **Retry**.
 */
fun statusCard(status: DeviceStatus, androidLink: AndroidLink, session: ConnectionState): StatusCard? {
    val failed = session is ConnectionState.Failed
    val androidLine = when (androidLink) {
        AndroidLink.CONNECTED -> AndroidLine.CONNECTED
        AndroidLink.NOT_CONNECTED -> AndroidLine.NOT_CONNECTED
        AndroidLink.UNKNOWN -> AndroidLine.UNKNOWN
    }
    return when (status) {
        DeviceStatus.ControlledByApp -> StatusCard(
            when (androidLink) {
                AndroidLink.CONNECTED -> AndroidLine.CONNECTED
                AndroidLink.NOT_CONNECTED -> AndroidLine.NOT_REPORTED_WHILE_CONTROLLED
                AndroidLink.UNKNOWN -> AndroidLine.UNKNOWN
            },
            SessionLine.OPEN,
            CardAction.DISCONNECT,
        )
        DeviceStatus.Connecting -> StatusCard(androidLine, SessionLine.OPENING, CardAction.NONE)
        DeviceStatus.ConnectedToPhone -> StatusCard(
            AndroidLine.CONNECTED,
            if (failed) SessionLine.FAILED else SessionLine.NOT_OPEN,
            if (failed) CardAction.RETRY else CardAction.CONNECT,
        )
        DeviceStatus.PairedNotConnected -> StatusCard(
            androidLine,
            if (failed) SessionLine.FAILED else SessionLine.NOT_OPEN,
            if (failed) CardAction.RETRY else CardAction.CONNECT,
        )
        DeviceStatus.BluetoothOff, is DeviceStatus.PermissionMissing, DeviceStatus.NotPaired -> null
    }
}
