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

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DarkMode
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import kotlinx.coroutines.Job

/** Route constants (ARCHITECTURE.md §2.4). */
private object Routes {
    const val CONNECTION = "connection"
    const val ANC = "anc"
    const val EQ = "eq"
    const val CONTROLS = "controls"
    const val FIND_MY_BUDS = "find_my_buds"

    /** The settings menu (Settings / Debug / Info) — `ai-sessions/0062` F-4; it replaced the Debug destination of `ai-sessions/0057`. */
    const val SETTINGS = "settings"
}

private data class TabDestination(val route: String, val label: String, val icon: ImageVector, val pullTab: PullTab)

/**
 * The five bottom-nav tabs (ARCHITECTURE.md §2.4; `ai-sessions/0057` D-6, Android's guidance "three to five destinations of equal importance"), all with
 * the same single-top navigation (`popUpTo(start){saveState} + launchSingleTop + restoreState`, `ai-sessions/0037`). **Debug is not a tab** — since
 * `ai-sessions/0062` (F-4) it is the Debug tab of the settings menu behind the top app bar's gear ([Routes.SETTINGS]), never in the main bottom/side navigation
 * (§2.4); back returns from the menu to the tab it was opened from (as Debug did since the maintainer's choice "Full screen + back", 2026-09-29).
 */
private val TAB_DESTINATIONS = listOf(
    TabDestination(Routes.CONNECTION, "Connection", OpenControlIcons.Connection, PullTab.CONNECTION),
    TabDestination(Routes.ANC, "ANC", OpenControlIcons.NoiseControl, PullTab.ANC),
    // `ai-sessions/0052`: the EQ tab also holds balance, mono and conversation detection, so it is labelled "Sound"; touch controls and
    // press-and-hold have their own "Controls" tab (the maintainer's choice in chat 2026-09-26).
    TabDestination(Routes.EQ, "Sound", OpenControlIcons.Sound, PullTab.SOUND),
    TabDestination(Routes.CONTROLS, "Controls", OpenControlIcons.Controls, PullTab.CONTROLS),
    TabDestination(Routes.FIND_MY_BUDS, "Find", OpenControlIcons.Find, PullTab.FIND),
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
    /** DLCI 0x02 settings writes (DECISIONS.md ADR-045, `ai-sessions/0052`) — each one `WriteSetting`, applied only on the Buds' OK. */
    val onVolumeBalanceChanged: (Int) -> Unit = {},
    val onMonoAudioChanged: (Boolean) -> Unit = {},
    val onConversationDetectionChanged: (Boolean) -> Unit = {},
    val onTouchControlsChanged: (Boolean) -> Unit = {},
    val onPressAndHoldChanged: (io.github.tedsluis.opencontrolpixelbuds.domain.Bud, io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction) -> Unit = { _, _ -> },
    /** Ticks/unticks one mode of the press-and-hold ANC-mode list (ADR-046, `ai-sessions/0056`). */
    val onAncModeSelectedChanged: (AncMode, Boolean) -> Unit = { _, _ -> },
    /** The "In-ear detection" switch (ADR-047, `ai-sessions/0056`). */
    val onInEarDetectionChanged: (Boolean) -> Unit = {},
    /** Re-reads Left/Right: a Message Stream claim (DECISIONS.md ADR-033); the Case and charging come from the runtime-info stream (ADR-043). */
    val onRefreshBattery: () -> Unit,
    val onDebugModeChanged: (Boolean) -> Unit,
    /** The Settings tab's dark mode (`ai-sessions/0062` F-6): stored by `:app`, applied at once. */
    val onDarkModeChanged: (DarkMode) -> Unit = {},
    /** Saves `BleLogger.exportLog()`'s current ring-buffer snapshot through the system save dialog
     * (`CreateDocument`) — local-only (AGENTS.md §9), the destination is the user's own choice, never an
     * automatic network call this app makes itself. */
    val onExportLog: () -> Unit,
    /**
     * A pull (swipe down) on a tab (`ai-sessions/0057` D-10): runs the one existing action [PullAction] names and returns the job it launched in the
     * application scope — `null` when it only opened a system prompt or did nothing — so the pull indicator lasts exactly as long as the action.
     */
    val onPull: (PullAction) -> Job? = { null },
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
    /** Why the last session loss happened as far as Android's link shows (`ai-sessions/0048` I-7); `null` = nothing to explain. */
    val lastLossCause: io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause? = null,
    /** Android itself reports the bonded Buds as connected to this phone (audio/HFP profiles) —
     * distinct from this app's own control channels being open (`ai-sessions/0039` §5). */
    val androidLink: AndroidLink,
    /** Why the last action needing the shared Message Stream channel could not claim it (ADR-032). */
    val messageStreamError: BudsError?,
    val ancMode: AncMode?,
    /** Wall-clock time (epoch millis) [ancMode] was last updated — `null` before any value arrived this
     * app run (`ai-sessions/0043` Phase H). */
    val ancModeUpdatedAt: Long? = null,
    /** Whether the Buds currently allow an ANC `Set` (`ai-sessions/0048` I-3). */
    val ancAvailability: io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability =
        io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability.UNKNOWN,
    /** When the Buds last reported [ancAvailability] — shown as "(checked HH:MM:SS)" while it reads not allowed (`ai-sessions/0054` I-1). */
    val ancAvailabilityUpdatedAt: Long? = null,
    /** When the last ANC change's answer was cut off — the shown mode is not confirmed until the next `Notify` (`ai-sessions/0062` F-3); `null` = confirmed. */
    val ancModeUnconfirmedAt: Long? = null,
    /** Why the last *Refresh battery* brought no new reading (`null` = it did / none yet), `ai-sessions/0052`. */
    val batteryRefreshError: BudsError? = null,
    /** Why the Case level could not be requested this connection (`null` = requested / not yet), ADR-043. */
    val caseBatteryError: BudsError? = null,
    /** Non-null while the app is in read-only Safe Mode (ARCHITECTURE.md §8.1, ADR-042). */
    val safeMode: io.github.tedsluis.opencontrolpixelbuds.domain.SafeModeState? = null,
    /** What the Buds announced at connect (firmware); `null` = nothing yet. */
    val deviceInfo: DeviceInfo? = null,
    /** The Find My Buds ring this app started and has not seen stopped (`null` = none) — kept across Disconnect (I-6). */
    val ringing: io.github.tedsluis.opencontrolpixelbuds.domain.RingNotice? = null,
    val eqProfile: EqBandGains?,
    /** Wall-clock time [eqProfile] was last updated — `null` when [eqProfile] is `null`. */
    val eqProfileUpdatedAt: Long? = null,
    /** Why the last EQ read/write failed (`null` = fine / not attempted) — ADR-034. */
    val eqError: BudsError?,
    /** The DLCI 0x02 settings as the Buds reported them (ADR-036/045, `ai-sessions/0052`). */
    val settings: io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings = io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings(),
    /** Why the last settings read or write failed (`null` = fine / not attempted). */
    val settingsError: io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure? = null,
    val batteryStatus: BatteryStatus,
    /** Wall-clock time [batteryStatus] was last updated — `null` before any reading arrived this app run. */
    val batteryStatusUpdatedAt: Long? = null,
    val unidentifiedFrames: List<UnidentifiedFrame>,
    val debugModeEnabled: Boolean,
    /** The Settings tab's dark-mode choice (`ai-sessions/0062` F-6); `:app` applies it to the theme. */
    val darkMode: DarkMode = DarkMode.DEFAULT,
    /** The app's build identity for the Info tab (`ai-sessions/0062` F-5), from `BuildConfig`. */
    val appBuild: AppBuildInfo = AppBuildInfo.UNKNOWN,
)

/**
 * Top-level navigation (ARCHITECTURE.md §2.4). A [Scaffold] with a top app bar ("OpenControl", the gear of the settings menu) and a bottom bar with the five
 * tabs.
 *
 * **`ai-sessions/0043` Phase H — swipe between tabs.** A [HorizontalPager] renders the tabs; a swipe that settles on a new page calls the *same*
 * `navController.navigate(...)` a bottom-nav tap uses (`navigateToTab`), so the single-top back-stack semantics (`ai-sessions/0037`) apply identically
 * whether the tab changed by a tap or a swipe, and a tap (or a system-back navigation) scrolls the pager to match. [NavHost] itself is a zero-size, empty
 * back-stack holder — the one source of truth for "which destination", including system back. **`ai-sessions/0057`/`0062`:** the settings menu is a
 * destination of that same graph outside the pager: while it is current the pager is replaced by [SettingsMenuScreen] and the top bar shows "Settings" and a back
 * arrow; back pops it. Every tab sits in a
 * [PullToRefresh] whose action is [pullActionFor] (D-10). // TODO(verify): swipe, pull and back are gesture behaviour this session could not exercise on a
 * device — `APP_TESTPLAN.md` (updated for `ai-sessions/0057`).
 */
@OptIn(ExperimentalMaterial3Api::class) // TopAppBar is experimental in the resolved material3 1.3.0 — approved at the 0057 checkpoint.
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
    val inSettings = currentDestination?.hierarchy?.any { it.route == Routes.SETTINGS } == true
    val currentIndex = TAB_DESTINATIONS.indexOfFirst { d -> currentDestination?.hierarchy?.any { it.route == d.route } == true }
        .coerceAtLeast(0)

    // A tap or a system-back navigation changed the current tab: keep the pager's page in sync (not while the settings menu is shown).
    LaunchedEffect(currentIndex, inSettings) {
        if (!inSettings && pagerState.currentPage != currentIndex) pagerState.scrollToPage(currentIndex)
    }
    // A completed swipe (the pager settles on a new page): drive the exact same navigation call a bottom-nav tap uses, so back-stack semantics never
    // depend on which trigger changed the tab.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settled ->
            val destination = navController.currentBackStackEntry?.destination
            if (destination?.hierarchy?.any { it.route == Routes.SETTINGS } == true) return@collect
            val liveIndex = TAB_DESTINATIONS.indexOfFirst { d -> destination?.hierarchy?.any { it.route == d.route } == true }
                .coerceAtLeast(0)
            if (settled != liveIndex) navigateToTab(settled)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (inSettings) "Settings" else "OpenControl") },
                navigationIcon = {
                    if (inSettings) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!inSettings) {
                        // F-4: the gear (material-icons-core's `Settings`, already a dependency) in the place of the 0057 bug icon.
                        IconButton(onClick = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!inSettings) {
                NavigationBar {
                    TAB_DESTINATIONS.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = currentIndex == index,
                            onClick = { navigateToTab(index) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = Routes.CONNECTION, modifier = Modifier.size(0.dp)) {
            TAB_DESTINATIONS.forEach { destination -> composable(destination.route) { } }
            composable(Routes.SETTINGS) { }
        }
        if (inSettings) {
            SettingsMenuScreen(
                darkMode = state.darkMode,
                onDarkModeChanged = actions.onDarkModeChanged,
                debugModeEnabled = state.debugModeEnabled,
                onDebugModeChanged = actions.onDebugModeChanged,
                unidentifiedFrames = state.unidentifiedFrames,
                onExportLog = actions.onExportLog,
                appBuild = state.appBuild,
                deviceInfo = state.deviceInfo,
                modifier = Modifier.padding(padding),
            )
        } else {
            HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) { page ->
                val tab = TAB_DESTINATIONS[page]
                PullToRefresh(onPull = { actions.onPull(pullActionFor(tab.pullTab, state.deviceStatus, state.connectionState)) }) {
                    TabContent(tab.route, state, actions)
                }
            }
        }
    }
}

@Composable
private fun TabContent(route: String, state: OpenControlUiState, actions: OpenControlActions) {
    when (route) {
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
            deviceInfo = state.deviceInfo,
            onRefreshBattery = actions.onRefreshBattery,
            lastLossCause = state.lastLossCause,
            safeMode = state.safeMode,
            batteryRefreshError = state.batteryRefreshError,
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
            ancAvailability = state.ancAvailability,
            ancAvailabilityUpdatedAt = state.ancAvailabilityUpdatedAt,
            ancModeUnconfirmedAt = state.ancModeUnconfirmedAt,
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
            settings = state.settings,
            settingsError = state.settingsError,
            onVolumeBalanceChanged = actions.onVolumeBalanceChanged,
            onMonoAudioChanged = actions.onMonoAudioChanged,
            onConversationDetectionChanged = actions.onConversationDetectionChanged,
        )
        Routes.CONTROLS -> ControlsScreen(
            connectionState = state.connectionState,
            settings = state.settings,
            settingsError = state.settingsError,
            onTouchControlsChanged = actions.onTouchControlsChanged,
            onPressAndHoldChanged = actions.onPressAndHoldChanged,
            onAncModeSelectedChanged = actions.onAncModeSelectedChanged,
            onInEarDetectionChanged = actions.onInEarDetectionChanged,
        )
        Routes.FIND_MY_BUDS -> FindMyBudsScreen(
            connectionState = state.connectionState,
            messageStreamError = state.messageStreamError,
            ringing = state.ringing,
            onRing = actions.onRing,
            onStop = actions.onStopRinging,
        )
    }
}
