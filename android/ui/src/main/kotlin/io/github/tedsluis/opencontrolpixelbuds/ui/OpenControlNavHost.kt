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

/** Route constants (ARCHITECTURE.md §2.4). */
private object Routes {
    const val CONNECTION = "connection"
    const val ANC = "anc"
    const val EQ = "eq"
    const val FIND_MY_BUDS = "find_my_buds"
    const val DEBUG = "debug"
}

private data class TabDestination(val route: String, val label: String)

/**
 * Every bottom-nav destination, **including** Debug — all five use identical
 * single-top-tab navigation semantics (`popUpTo(start){saveState} +
 * launchSingleTop + restoreState`). Debug used to be special-cased with a
 * plain `navigate()` call, which let it accumulate duplicate back-stack
 * entries never covered by the other tabs' `popUpTo`/`saveState` handling —
 * a real, reproducible bug (not just a style choice) confirmed by a
 * maintainer report of tabs "disappearing" behind Debug after navigating
 * with the system back gesture (`ai-sessions/0037`). Debug stays visually
 * distinct only in that it's never *highlighted* as the current primary
 * destination (AGENTS.md §6/§9 — it's a developer surface, not part of
 * ordinary use) — see [selected] below.
 */
private val TAB_DESTINATIONS = listOf(
    TabDestination(Routes.CONNECTION, "Connection"),
    TabDestination(Routes.ANC, "ANC"),
    TabDestination(Routes.EQ, "EQ"),
    TabDestination(Routes.FIND_MY_BUDS, "Find"),
    TabDestination(Routes.DEBUG, "Debug"),
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
 * Top-level navigation graph (ARCHITECTURE.md §2.4). A single bottom
 * navigation bar covers all five destinations, including Debug — kept out of
 * the *ordinary* flow only by never being shown as "selected" (see
 * [TAB_DESTINATIONS]'s own doc comment for why it still needs the same
 * navigation mechanics as the primary four, not a visually separate entry
 * point as originally designed).
 */
@Composable
fun OpenControlNavHost(state: OpenControlUiState, actions: OpenControlActions) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                TAB_DESTINATIONS.forEach { destination ->
                    val isCurrent = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        // Debug is never shown as "selected," per AGENTS.md §6/§9 — a developer
                        // surface, not one of the app's ordinary tabs — even though it now uses
                        // the exact same navigation mechanics as the primary four.
                        selected = isCurrent && destination.route != Routes.DEBUG,
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
    Routes.DEBUG -> Icons.Filled.Info
    else -> Icons.Filled.Settings
}
