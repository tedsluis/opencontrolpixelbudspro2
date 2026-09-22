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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.DockState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
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
    /** Shows the system permission prompt for Bluetooth (GrapheneOS: *Nearby devices*) and notifications. */
    val onRequestPermissions: () -> Unit,
    /** Opens this app's page in the system settings — the only way to grant a permission denied "permanently". */
    val onOpenAppSettings: () -> Unit,
    val onConnect: () -> Unit,
    val onDisconnect: () -> Unit,
    val onAncModeSelected: (AncMode) -> Unit,
    val onRefreshAncMode: () -> Unit,
    /** Asks Android directly to add the ANC Quick Settings tile (`StatusBarManager.requestAddTileService`,
     * API 33+) instead of requiring the user to find it themselves via Quick Settings' edit screen
     * (`ai-sessions/0043`, `CAP-060-FINDINGS.md` §4 — the tile's code was correct but never discovered). */
    val onRequestAddAncTile: () -> Unit,
    val onEqGainsChanged: (EqBandGains) -> Unit,
    val onEqPresetSelected: (EqPreset) -> Unit,
    /** Re-reads the Buds' active EQ (`ReadSetting 4:16`, DECISIONS.md ADR-034). */
    val onRefreshEq: () -> Unit,
    val onRing: (RingTarget) -> Unit,
    val onStopRinging: () -> Unit,
    /** Re-reads the battery: a Message Stream claim (Left/Right) and a DLCI 0x08 claim (Case), DECISIONS.md ADR-033/ADR-035. */
    val onRefreshBattery: () -> Unit,
    val onDebugModeChanged: (Boolean) -> Unit,
    /** Shares `BleLogger.exportLog()`'s current ring-buffer snapshot via the system share sheet —
     * local-only (AGENTS.md §9), the destination is the user's own choice, never an automatic
     * network call this app makes itself. */
    val onExportLog: () -> Unit,
)

data class OpenControlUiState(
    val connectionState: ConnectionState,
    val bluetoothEnabled: Boolean,
    val hasBondedDevice: Boolean,
    /** The top-level status derived from Android's Bluetooth state first, the app's own session second
     * (`ai-sessions/0041`: mirror Android's settings). */
    val deviceStatus: DeviceStatus,
    val permissionState: PermissionState,
    /** `null` = no pairing attempt in progress/to report — a status line the Connection screen
     * shows while/after pairing (ARCHITECTURE.md §9.0a), so the maintainer's own "no message why
     * it didn't work" report (`ai-sessions/0036`) can't recur silently. */
    val pairingStatusText: String?,
    /** Why the last session ended unexpectedly (`null` = nothing to explain) — shown under
     * "Disconnected" so an unexpected drop is never a bare state change (`ai-sessions/0039`). */
    val lastConnectionError: BudsError?,
    /** Android itself reports the bonded Buds as connected to this phone (audio/HFP profiles) —
     * distinct from this app's own control channels being open (`ai-sessions/0039` §5). */
    val androidLink: AndroidLink,
    /** Why the last action needing the shared Message Stream channel could not claim it (ADR-032). */
    val messageStreamError: BudsError?,
    val ancMode: AncMode?,
    /** Wall-clock time (epoch millis) [ancMode] was last updated — `null` before any value arrived this
     * app run (`ai-sessions/0043` Phase H). */
    val ancModeUpdatedAt: Long? = null,
    /** Why the Case battery could not be read by the last DLCI 0x08 claim (`null` = read / not attempted), ADR-035. */
    val caseBatteryError: BudsError? = null,
    /** Whether the earbuds sit in the case, from the last `Notify ANC state` (ADR-024). */
    val dockState: DockState = DockState.UNKNOWN,
    /** Wall-clock time [dockState] was last updated — `null` before any `Notify` arrived this app run. */
    val dockStateUpdatedAt: Long? = null,
    /** What the Buds announced at connect (firmware); `null` = nothing yet. */
    val deviceInfo: DeviceInfo? = null,
    /** The earbud a Find My Buds ring was started on and not yet stopped (`null` = none). */
    val ringingTarget: RingTarget? = null,
    val eqProfile: EqBandGains?,
    /** Wall-clock time [eqProfile] was last updated — `null` when [eqProfile] is `null`. */
    val eqProfileUpdatedAt: Long? = null,
    /** Why the last EQ read/write failed (`null` = fine / not attempted) — ADR-034. */
    val eqError: BudsError?,
    val batteryStatus: BatteryStatus,
    /** Wall-clock time [batteryStatus] was last updated — `null` before any reading arrived this app run. */
    val batteryStatusUpdatedAt: Long? = null,
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
 *
 * **`ai-sessions/0043` Phase H — swipe between tabs.** A [HorizontalPager] sits inside the [NavHost]'s
 * single [Routes.CONNECTION]/[Routes.ANC]/.../[Routes.DEBUG] destinations are still reached through the
 * *same* `navController.navigate(...)` call the bottom bar always used (`navigateToTab` below) — a
 * swipe that settles on a new page just calls it with that page's route, so the single-top back-stack
 * semantics this file's own doc comment already describes (`ai-sessions/0037`'s fix) apply identically
 * whether the tab changed by a tap or a swipe. The pager's own page index is kept in sync the other way
 * too: a tap (or a system-back navigation) changes [NavHost]'s current destination, and a
 * [LaunchedEffect] scrolls the pager to match. // TODO(verify): swipe gesture feel and the two-way sync
 * are Android-framework/gesture behavior this session could not exercise on a device or emulator — see
 * `ai-sessions/0043_FEATURE_RESULT_2026_09_22.md`'s re-test instructions.
 */
@Composable
fun OpenControlNavHost(state: OpenControlUiState, actions: OpenControlActions) {
    val navController = rememberNavController()
    val pagerState = rememberPagerState(pageCount = { TAB_DESTINATIONS.size })

    fun navigateToTab(index: Int) {
        navController.navigate(TAB_DESTINATIONS[index].route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentIndex = TAB_DESTINATIONS.indexOfFirst { d -> currentDestination?.hierarchy?.any { it.route == d.route } == true }
        .coerceAtLeast(0)

    // A tap or a system-back navigation changed the current destination: keep the pager's page in sync.
    LaunchedEffect(currentIndex) {
        if (pagerState.currentPage != currentIndex) pagerState.scrollToPage(currentIndex)
    }
    // A completed swipe (the pager settles on a new page): drive the exact same navigation call a
    // bottom-nav tap uses, so back-stack semantics never depend on which trigger changed the tab.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settled ->
            val destination = navController.currentBackStackEntry?.destination
            val liveIndex = TAB_DESTINATIONS.indexOfFirst { d -> destination?.hierarchy?.any { it.route == d.route } == true }
                .coerceAtLeast(0)
            if (settled != liveIndex) navigateToTab(settled)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                TAB_DESTINATIONS.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        // Debug is never shown as "selected," per AGENTS.md §6/§9 — a developer
                        // surface, not one of the app's ordinary tabs — even though it now uses
                        // the exact same navigation mechanics as the primary four.
                        selected = currentIndex == index && destination.route != Routes.DEBUG,
                        onClick = { navigateToTab(index) },
                        icon = { Icon(iconFor(destination.route), contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        // The pager owns the visible content; NavHost (below, zero-size) exists solely to keep the
        // proven single-top back-stack machinery (`ai-sessions/0037`) as the one source of truth for
        // "which tab," including system-back handling — the pager only ever mirrors it (see above).
        NavHost(navController = navController, startDestination = Routes.CONNECTION, modifier = Modifier.size(0.dp)) {
            TAB_DESTINATIONS.forEach { destination -> composable(destination.route) { } }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) { page ->
            when (TAB_DESTINATIONS[page].route) {
                Routes.CONNECTION -> ConnectionScreen(
                    connectionState = state.connectionState,
                    deviceStatus = state.deviceStatus,
                    androidLink = state.androidLink,
                    permissionState = state.permissionState,
                    pairingStatusText = state.pairingStatusText,
                    lastConnectionError = state.lastConnectionError,
                    messageStreamError = state.messageStreamError,
                    batteryStatus = state.batteryStatus,
                    batteryStatusUpdatedAt = state.batteryStatusUpdatedAt,
                    caseBatteryError = state.caseBatteryError,
                    dockState = state.dockState,
                    dockStateUpdatedAt = state.dockStateUpdatedAt,
                    deviceInfo = state.deviceInfo,
                    onRefreshBattery = actions.onRefreshBattery,
                    onRequestEnableBluetooth = actions.onRequestEnableBluetooth,
                    onPair = actions.onPair,
                    onRequestPermissions = actions.onRequestPermissions,
                    onOpenAppSettings = actions.onOpenAppSettings,
                    onConnect = actions.onConnect,
                    onDisconnect = actions.onDisconnect,
                )
                Routes.ANC -> AncScreen(
                    connectionState = state.connectionState,
                    messageStreamError = state.messageStreamError,
                    ancMode = state.ancMode,
                    ancModeUpdatedAt = state.ancModeUpdatedAt,
                    onAncModeSelected = actions.onAncModeSelected,
                    onRefreshAncMode = actions.onRefreshAncMode,
                    onRequestAddAncTile = actions.onRequestAddAncTile,
                )
                Routes.EQ -> EqScreen(
                    connectionState = state.connectionState,
                    gains = state.eqProfile,
                    eqProfileUpdatedAt = state.eqProfileUpdatedAt,
                    eqError = state.eqError,
                    onGainsChanged = actions.onEqGainsChanged,
                    onPresetSelected = actions.onEqPresetSelected,
                    onRefresh = actions.onRefreshEq,
                )
                Routes.FIND_MY_BUDS -> FindMyBudsScreen(
                    connectionState = state.connectionState,
                    messageStreamError = state.messageStreamError,
                    ringingTarget = state.ringingTarget,
                    onRing = actions.onRing,
                    onStop = actions.onStopRinging,
                )
                Routes.DEBUG -> DebugScreen(
                    debugModeEnabled = state.debugModeEnabled,
                    onDebugModeChanged = actions.onDebugModeChanged,
                    unidentifiedFrames = state.unidentifiedFrames,
                    onExportLog = actions.onExportLog,
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
