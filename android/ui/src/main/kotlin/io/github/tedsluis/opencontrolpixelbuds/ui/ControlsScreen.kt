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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure

/**
 * The "Controls" tab (`ai-sessions/0052`, the maintainer's choice in chat 2026-09-26): "Use touch controls" (`qhr` field 4), the press-and-hold
 * action per bud (field 7) — both writable under DECISIONS.md ADR-045 — and the in-ear detection **setting** (field 2), read-only (ADR-036) and
 * explicitly not the live wear state. The ANC-mode list (field 12) is deliberately absent: its bit order is disputed (PROTOCOL.md §4.5.3).
 */
@Composable
fun ControlsScreen(
    connectionState: ConnectionState,
    settings: BudsSettings,
    settingsError: SettingsFailure?,
    onTouchControlsChanged: (Boolean) -> Unit,
    onPressAndHoldChanged: (Bud, HoldAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = connectionState.isReady()
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Controls", style = MaterialTheme.typography.headlineSmall)
            NotConnectedBanner(connectionState)
            if (enabled) SettingsFailureNotice(settingsError)

            SettingSwitchRow("Use touch controls", null, settings.touchControls, enabled, onTouchControlsChanged)
            HorizontalDivider()
            Text("Press and hold", style = MaterialTheme.typography.titleMedium)
            Text(DIGITAL_ASSISTANT_NOTE, style = MaterialTheme.typography.bodySmall)
            HoldRow("Left", settings.holdLeft, enabled) { onPressAndHoldChanged(Bud.LEFT, it) }
            HoldRow("Right", settings.holdRight, enabled) { onPressAndHoldChanged(Bud.RIGHT, it) }
            HorizontalDivider()
            Text(inEarDetectionText(settings.inEarDetection), style = MaterialTheme.typography.bodyLarge)
            Text(settingTime(settings.inEarDetection), style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** One bud's press-and-hold action: the Buds' value is the selected chip; a tap on the other one writes it (ADR-045). */
@Composable
private fun HoldRow(label: String, reading: SettingReading<HoldAction>?, enabled: Boolean, onSelect: (HoldAction) -> Unit) {
    Column {
        Text("$label: ${reading?.value?.let(::holdActionText) ?: SETTING_NOT_READ}", style = MaterialTheme.typography.bodyLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HoldAction.entries.forEach { action ->
                FilterChip(
                    selected = reading?.value == action,
                    onClick = { if (reading?.value != action) onSelect(action) },
                    enabled = enabled,
                    label = { Text(holdActionText(action)) },
                )
            }
        }
        if (reading != null) Text(settingTime(reading), style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * `ai-sessions/0054` I-5 (the maintainer's wording, chat 2026-09-28). In `CAP-063` a "Digital assistant" hold reached nothing on the phone — no HFP
 * `AT+BVRA`, no media command — while the Google app's headphone-assistant service had been stopped by Android (🟡 strong: that service serves it over
 * DLCI 0x08/0x0a, `CAP-063-FINDINGS.md` §7). The app does not detect an assistant app (reading the assistant role needs a privileged permission).
 */
internal const val DIGITAL_ASSISTANT_NOTE: String =
    "Digital assistant needs an assistant app on this phone that supports headphones (for example the Google app). " +
        "Without one, holding the bud may only play a tone."

internal fun inEarDetectionText(reading: SettingReading<Boolean>?): String {
    val value = when (reading?.value) {
        true -> "on"
        false -> "off"
        null -> "not read"
    }
    return "In-ear detection (setting): $value — read-only; not whether a bud is worn"
}
