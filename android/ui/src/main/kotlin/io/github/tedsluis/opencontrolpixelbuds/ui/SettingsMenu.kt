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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.DarkMode
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame

/**
 * The app's own build identity for the Info tab (`ai-sessions/0062` F-5): [versionName], the git [commit] it was built from (short hash, "-dirty" when tracked
 * files differed from it, "unknown" without git) and that commit's date — computed **locally at build time** (`app/build.gradle.kts`, `BuildConfig`), never
 * fetched. No build-time stamp (the maintainer's choice, chat 2026-10-01: reproducible builds).
 */
data class AppBuildInfo(val versionName: String, val commit: String, val commitDate: String) {
    companion object {
        val UNKNOWN = AppBuildInfo("unknown", "unknown", "unknown")
    }
}

/** The three tabs of the settings menu, in order (the maintainer's design, chat 2026-10-01). */
internal enum class SettingsTab(val label: String) { SETTINGS("Settings"), DEBUG("Debug"), INFO("Info") }

/**
 * The settings menu (`ai-sessions/0062` F-4, the maintainer's design in chats 2026-10-01): a full-screen destination reached from the top app bar's gear, outside
 * the five bottom tabs (ARCHITECTURE.md §2.4), with three tabs — **Settings** (dark mode), **Debug** (today's [DebugScreen], unchanged) and **Info** (the app's
 * build and the Buds' firmware). The selected tab survives a rotation; back (the top bar's arrow or the system's) returns to the tab the menu was opened from.
 */
@Composable
fun SettingsMenuScreen(
    darkMode: DarkMode,
    onDarkModeChanged: (DarkMode) -> Unit,
    debugModeEnabled: Boolean,
    onDebugModeChanged: (Boolean) -> Unit,
    unidentifiedFrames: List<UnidentifiedFrame>,
    onExportLog: () -> Unit,
    appBuild: AppBuildInfo,
    deviceInfo: DeviceInfo?,
    /** Hands a URL to another app (the browser) — `:app` starts `Intent.ACTION_VIEW` (`ai-sessions/0064` F-6, DECISIONS.md ADR-050); injected so `:ui` stays testable. */
    onOpenUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    /** "Use different Buds" (`ai-sessions/0069`, A68-APP-05): `:app` disconnects and removes this app's own pairing association. */
    onUseDifferentBuds: () -> Unit = {},
) {
    var selected by rememberSaveable { mutableIntStateOf(SettingsTab.SETTINGS.ordinal) }
    Column(modifier = modifier.fillMaxSize()) {
        // `TabRow` is deprecated in material3 1.4.0 ("Replaced with PrimaryTabRow and SecondaryTabRow"); kept, with its warning suppressed, so the menu
        // looks as before — Primary label, full-width indicator (the maintainer's choice in chat 2026-10-07, `ai-sessions/0078`). PrimaryTabRow would narrow
        // the indicator to the label; SecondaryTabRow would turn the selected label OnSurface.
        @Suppress("DEPRECATION")
        TabRow(selectedTabIndex = selected) {
            SettingsTab.entries.forEach { tab ->
                Tab(selected = selected == tab.ordinal, onClick = { selected = tab.ordinal }, text = { Text(tab.label) })
            }
        }
        when (SettingsTab.entries[selected]) {
            SettingsTab.SETTINGS -> DarkModeSettings(darkMode, onDarkModeChanged, onUseDifferentBuds)
            SettingsTab.DEBUG -> DebugScreen(
                debugModeEnabled = debugModeEnabled,
                onDebugModeChanged = onDebugModeChanged,
                unidentifiedFrames = unidentifiedFrames,
                onExportLog = onExportLog,
            )
            SettingsTab.INFO -> InfoTab(appBuild, deviceInfo, onOpenUrl)
        }
    }
}

/** The labels of the dark-mode choice, in the order shown (the maintainer's words: System, On, Off; System is the default). */
internal fun darkModeLabel(mode: DarkMode): String = when (mode) {
    DarkMode.SYSTEM -> "System (follows Android)"
    DarkMode.ON -> "On"
    DarkMode.OFF -> "Off"
}

/** F-6: one radio group; a choice is stored and applied at once (the theme follows the stored value — no restart). */
@Composable
private fun DarkModeSettings(darkMode: DarkMode, onDarkModeChanged: (DarkMode) -> Unit, onUseDifferentBuds: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        // Scrollable since `ai-sessions/0069`: with "Use different Buds" below the radio group the tab no longer fits every screen or font size.
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Dark mode", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.selectableGroup()) {
                DarkMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = darkMode == mode, onClick = { onDarkModeChanged(mode) }, role = Role.RadioButton)
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = darkMode == mode, onClick = null) // the row is the touch target (Android's radio-group guidance)
                        Text(darkModeLabel(mode), modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
            // `ai-sessions/0069` A68-APP-05 (the maintainer's wording, chat 2026-10-03): the only way to control other Buds used to be clearing the app's data.
            Text(USE_DIFFERENT_BUDS_TITLE, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            Text(USE_DIFFERENT_BUDS_TEXT, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onUseDifferentBuds) { Text(USE_DIFFERENT_BUDS_TITLE) }
        }
    }
}

internal const val USE_DIFFERENT_BUDS_TITLE: String = "Use different Buds"

internal const val USE_DIFFERENT_BUDS_TEXT: String =
    "Forgets which Buds this app controls (the Bluetooth pairing in Android stays). You then pick the Buds again with Pair a device."

/** F-5: "App: 1.0.1, build <commit> (<commit date>)" — the build identity, never fetched. */
internal fun appBuildLine(build: AppBuildInfo): String = "App: ${build.versionName}, build ${build.commit} (${build.commitDate})"

/** The Info tab's line when the Buds have not announced anything on this connection. */
internal const val INFO_NOT_CONNECTED: String = "Not connected yet — the Buds report their firmware when the app connects."

/**
 * Which part of the Buds an announcement entry is: the official app's own mapping (`PROTOCOL.md` §2.2a, 2026-10-01 Update — 🟢 FACT for the app's code,
 * maintainer-approved in chat 2026-10-01, `ai-sessions/0062`): entry 1 = Case, 2 = Left bud, 3 = Right bud. Any other index is shown by its number, never
 * guessed.
 */
internal fun firmwareComponentLabel(index: Int): String = when (index) {
    1 -> "Case"
    2 -> "Left bud"
    3 -> "Right bud"
    else -> "Part $index"
}

/**
 * F-5: the Buds' part of the Info tab — the heading with the announcement's receive time, one line per entry in the announcement's order, and the announced
 * control channel; [INFO_NOT_CONNECTED] without an announcement on this connection.
 */
internal fun firmwareInfoLines(deviceInfo: DeviceInfo?): List<String> {
    if (deviceInfo == null || deviceInfo.firmware.isEmpty()) return listOf(INFO_NOT_CONNECTED)
    val heading = "Firmware (from the Buds' announcement" + (formatUpdatedAt(deviceInfo.announcedAtMillis)?.let { ", $it" } ?: "") + "):"
    val entries = deviceInfo.entries.sortedBy { it.index }.map { "${firmwareComponentLabel(it.index)}: ${it.firmware}" }
        .ifEmpty { deviceInfo.firmware.map { "Firmware: $it" } }
    return listOf(heading) + entries + listOfNotNull(deviceInfo.maestroChannel?.let { "Control channel: $it" })
}

/**
 * `ai-sessions/0064` F-6 (the maintainer's request and choice "Links + bundled licence", chat 2026-10-01; DECISIONS.md ADR-050): the project's links. **Since
 * `ai-sessions/0066`** (the maintainer's request, chat 2026-10-02) the licence is only read in the app ("Read the licence") — no link to `LICENSE`. Fixed
 * constants, opened only by a tap, by another app (the browser) — this app makes no network request and has no `INTERNET` permission (AGENTS.md §1).
 * Checked 2026-10-01 (`ai-sessions/0064` RESULT §F): `git remote get-url origin` = `git@github.com:tedsluis/opencontrolpixelbudspro2.git`; each URL answers 200.
 */
internal object ProjectLinks {
    const val README_URL: String = "https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/README.md"
    const val ISSUES_URL: String = "https://github.com/tedsluis/opencontrolpixelbudspro2/issues"
}

/** The licence line (F-6): the SPDX identifier every source file carries (`AGPL-3.0-or-later`, AGENTS.md §12, ADR-002) and the `LICENSE` file's licence. */
internal const val LICENCE_LINE: String = "GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)"

/**
 * `ai-sessions/0065`, DECISIONS.md ADR-051 (the maintainer's wording, chat 2026-10-02): the app's name names the compatible product as plain text only; this
 * line says it is not Google's app. The same sentence is in the README and the release notes.
 */
internal const val TRADEMARK_NOTICE: String =
    "Works with Google Pixel Buds Pro 2. Not affiliated with or endorsed by Google. Pixel Buds is a trademark of Google LLC."

/** Under the links (F-6): where they open. */
internal const val LINKS_NOTE: String = "Links open in your browser; this app itself has no internet access."

@Composable
private fun InfoTab(appBuild: AppBuildInfo, deviceInfo: DeviceInfo?, onOpenUrl: (String) -> Unit) {
    var showLicence by rememberSaveable { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("This app", style = MaterialTheme.typography.titleMedium)
            Text(appBuildLine(appBuild), style = MaterialTheme.typography.bodyMedium)
            Text(TRADEMARK_NOTICE, style = MaterialTheme.typography.bodySmall)
            Text("Licence", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Text(LICENCE_LINE, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { showLicence = true }) { Text("Read the licence") }
            Text("Project", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            TextButton(onClick = { onOpenUrl(ProjectLinks.README_URL) }) { Text("README on GitHub") }
            TextButton(onClick = { onOpenUrl(ProjectLinks.ISSUES_URL) }) { Text("Report an issue on GitHub") }
            Text(LINKS_NOTE, style = MaterialTheme.typography.bodySmall)
            Text("The Buds", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            firmwareInfoLines(deviceInfo).forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
    if (showLicence) LicenceDialog(onClose = { showLicence = false })
}

/** F-6: the full licence text, bundled (`res/raw/license.txt`, byte-identical to the repository's `LICENSE` — `SettingsMenuTest`), readable offline. */
@Composable
private fun LicenceDialog(onClose: () -> Unit) {
    // LocalResources, not LocalContext.current.resources (lint LocalContextResourcesRead, `ai-sessions/0078`): the same file and text.
    val resources = LocalResources.current
    val text = remember { resources.openRawResource(R.raw.license).bufferedReader().use { it.readText() } }
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } },
        title = { Text("Licence") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text, style = MaterialTheme.typography.bodySmall)
            }
        },
    )
}
