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

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame

/** Route constants (ARCHITECTURE.md §2.4). [DEBUG] is deliberately not
 * listed in [PRIMARY_DESTINATIONS] — it is reachable only via a non-primary
 * entry point, per AGENTS.md §6/§9. */
private object Routes {
    const val CONNECTION = "connection"
    const val ANC = "anc"
    const val EQ = "eq"
    const val FIND_MY_BUDS = "find_my_buds"
    const val DEBUG = "debug"
}

private data class PrimaryDestination(val route: String, val label: String)

private val PRIMARY_DESTINATIONS = listOf(
    PrimaryDestination(Routes.CONNECTION, "Connection"),
    PrimaryDestination(Routes.ANC, "ANC"),
    PrimaryDestination(Routes.EQ, "EQ"),
    PrimaryDestination(Routes.FIND_MY_BUDS, "Find"),
)

/**
 * Groups every callback [OpenControlNavHost] needs — one class instead of a
 * long parameter list, since every screen below needs a different subset of
 * these. Plain lambdas, no `BudsRepository`/Hilt dependency in `:ui`
 * (ARCHITECTURE.md §2's dependency direction — `:ui` depends only on
 * `:domain`); `:app` is what actually wires these to real repository calls.
 */
data class OpenControlActions(
    val onRequestEnableBluetooth: () -> Unit,
    val onPair: () -> Unit,
    val onConnect: () -> Unit,
    val onDisconnect: () -> Unit,
    val onAncModeSelected: (AncMode) -> Unit,
    val onEqGainsChanged: (EqBandGains) -> Unit,
    val onEqPresetSelected: (EqPreset) -> Unit,
    val onRing: (RingTarget) -> Unit,
    val onStopRinging: () -> Unit,
    val onDebugModeChanged: (Boolean) -> Unit,
)

data class OpenControlUiState(
    val connectionState: ConnectionState,
    val bluetoothEnabled: Boolean,
    val hasBondedDevice: Boolean,
    /** `null` = no pairing attempt in progress/to report — a status line the Connection screen
     * shows while/after pairing (ARCHITECTURE.md §9.0a), so the maintainer's own "no message why
     * it didn't work" report (`ai-sessions/0036`) can't recur silently. */
    val pairingStatusText: String?,
    val ancMode: AncMode?,
    val eqProfile: EqBandGains?,
    val batteryStatus: BatteryStatus,
    val unidentifiedFrames: List<UnidentifiedFrame>,
    val debugModeEnabled: Boolean,
)

/**
 * Top-level navigation graph (ARCHITECTURE.md §2.4). A bottom navigation bar
 * covers the four primary destinations; the Debug screen is reached via the
 * top app bar's overflow-style settings icon instead, keeping it out of the
 * primary flow.
 */
@Composable
fun OpenControlNavHost(state: OpenControlUiState, actions: OpenControlActions) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                PRIMARY_DESTINATIONS.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(destination.route), contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.DEBUG) },
                    icon = { Icon(Icons.Filled.Info, contentDescription = "Debug") },
                    label = { Text("Debug") },
                )
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = Routes.CONNECTION, modifier = Modifier) {
            composable(Routes.CONNECTION) {
                ConnectionScreen(
                    connectionState = state.connectionState,
                    bluetoothEnabled = state.bluetoothEnabled,
                    hasBondedDevice = state.hasBondedDevice,
                    pairingStatusText = state.pairingStatusText,
                    batteryStatus = state.batteryStatus,
                    onRequestEnableBluetooth = actions.onRequestEnableBluetooth,
                    onPair = actions.onPair,
                    onConnect = actions.onConnect,
                    onDisconnect = actions.onDisconnect,
                    modifier = Modifier.padding(padding),
                )
            }
            composable(Routes.ANC) {
                AncScreen(
                    connectionState = state.connectionState,
                    ancMode = state.ancMode,
                    onAncModeSelected = actions.onAncModeSelected,
                    modifier = Modifier.padding(padding),
                )
            }
            composable(Routes.EQ) {
                EqScreen(
                    gains = state.eqProfile,
                    onGainsChanged = actions.onEqGainsChanged,
                    onPresetSelected = actions.onEqPresetSelected,
                    modifier = Modifier.padding(padding),
                )
            }
            composable(Routes.FIND_MY_BUDS) {
                FindMyBudsScreen(
                    onRing = actions.onRing,
                    onStop = actions.onStopRinging,
                    modifier = Modifier.padding(padding),
                )
            }
            composable(Routes.DEBUG) {
                DebugScreen(
                    debugModeEnabled = state.debugModeEnabled,
                    onDebugModeChanged = actions.onDebugModeChanged,
                    unidentifiedFrames = state.unidentifiedFrames,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

private fun iconFor(route: String) = when (route) {
    Routes.CONNECTION -> Icons.Filled.Settings
    Routes.ANC -> Icons.Filled.Settings
    Routes.EQ -> Icons.AutoMirrored.Filled.List
    Routes.FIND_MY_BUDS -> Icons.Filled.Notifications
    else -> Icons.Filled.Settings
}
