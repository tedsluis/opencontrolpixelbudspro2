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

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import kotlinx.coroutines.Job

/** The five tabs a pull can happen on (`ai-sessions/0057`, D-10). */
enum class PullTab { CONNECTION, ANC, SOUND, CONTROLS, FIND }

/**
 * What one pull does — each value is **one existing user action** (`ai-sessions/0057` D-10, the maintainer's decisions 2026-09-29), run once: no automatic
 * refresh, no timer, no retry.
 */
enum class PullAction {
    /** *Refresh battery* (Connection; and Find — the maintainer's choice at the checkpoint). */
    REFRESH_BATTERY,

    /** The ANC tab's Refresh. */
    REFRESH_ANC,

    /** "Read EQ again" and then the settings re-read (`refreshSettings`, D-11), one after the other. */
    REFRESH_SOUND,

    /** The settings re-read (`refreshSettings`, D-11). */
    REFRESH_SETTINGS,

    /** The Connect / Retry button. */
    CONNECT,

    /** What the screen's own button does in that state: "Enable Bluetooth", "Allow", "Open app settings", "Pair a device". */
    ENABLE_BLUETOOTH,
    REQUEST_PERMISSIONS,
    OPEN_APP_SETTINGS,
    PAIR,

    /** While the session is being opened: nothing (the indicator ends at once). */
    NOTHING,
}

/**
 * D-10's table. Where Connect cannot start (Bluetooth off, permission missing, not paired) a pull does what that state's button on the Connection screen does
 * (`ConnectionScreen.kt`) — never a new dialog, never a silent no-op.
 */
fun pullActionFor(tab: PullTab, deviceStatus: DeviceStatus, connectionState: ConnectionState): PullAction = when {
    deviceStatus is DeviceStatus.BluetoothOff -> PullAction.ENABLE_BLUETOOTH
    deviceStatus is DeviceStatus.PermissionMissing ->
        if (deviceStatus.status == PermissionStatus.PERMANENTLY_DENIED) PullAction.OPEN_APP_SETTINGS else PullAction.REQUEST_PERMISSIONS
    deviceStatus is DeviceStatus.NotPaired -> PullAction.PAIR
    connectionState is ConnectionState.Ready -> when (tab) {
        PullTab.CONNECTION, PullTab.FIND -> PullAction.REFRESH_BATTERY
        PullTab.ANC -> PullAction.REFRESH_ANC
        PullTab.SOUND -> PullAction.REFRESH_SOUND
        PullTab.CONTROLS -> PullAction.REFRESH_SETTINGS
    }
    connectionState is ConnectionState.Connecting || connectionState is ConnectionState.Discovering -> PullAction.NOTHING
    else -> PullAction.CONNECT
}

/**
 * Swipe down to refresh / reconnect (D-10). [onPull] starts the tab's action and returns the job it launched — `null` when it only opened a system prompt or
 * did nothing. The indicator shows while that job runs and ends when the repository has answered; it is never a spinner with a fixed duration.
 * `PullToRefreshBox` is `@ExperimentalMaterial3Api` in the resolved `material3` 1.3.0 — the opt-in the maintainer approved (`ai-sessions/0057`).
 * The content must scroll vertically for the pull to reach the box (nested scroll).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PullToRefresh(onPull: () -> Job?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    var running by remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(running) {
        running?.join()
        running = null
    }
    PullToRefreshBox(
        isRefreshing = running != null,
        onRefresh = { if (running == null) running = onPull() },
        modifier = modifier.fillMaxSize(),
        content = content,
    )
}
