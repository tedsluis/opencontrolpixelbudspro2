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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure

/*
 * Shared texts and rows for the DLCI 0x02 settings (`ai-sessions/0052`, DECISIONS.md ADR-036/045). The texts are the ones the maintainer chose in chat
 * (2026-09-26, "As in the preview"). A value shown is always the Buds' own (read at Connect, or a write they acknowledged) with its time — never
 * a value the app assumed (AGENTS.md §5, ARCHITECTURE.md §3.1).
 */

internal const val SETTING_NOT_READ: String = "Not read from the Buds yet"

/** "read HH:MM:SS" for a value read from the Buds, "changed HH:MM:SS" for this app's acknowledged write. */
internal fun settingTime(reading: SettingReading<*>?): String {
    if (reading == null) return SETTING_NOT_READ
    val time = formatUpdatedAt(reading.atMillis) ?: return if (reading.changedByApp) "changed" else "read"
    return (if (reading.changedByApp) "changed " else "read ") + time
}

/** Wire +100 = Left, −100 = Right (DECISIONS.md ADR-026): "Left 40", "Right 25", "Centre". */
internal fun balanceText(value: Int): String = when {
    value > 0 -> "Left $value"
    value < 0 -> "Right ${-value}"
    else -> "Centre"
}

internal fun holdActionText(action: HoldAction): String = when (action) {
    HoldAction.NOISE_CONTROL -> "Noise control"
    HoldAction.ASSISTANT -> "Digital assistant"
}

/** The read/write failure line, worded per kind (the chosen texts). */
internal fun settingsFailureText(failure: SettingsFailure): String =
    if (failure.write) {
        "The setting was not changed: ${failure.error.userMessage()}"
    } else {
        "Couldn't read the Buds' settings: ${failure.error.userMessage()}"
    }

@Composable
internal fun SettingsFailureNotice(failure: SettingsFailure?) {
    failure ?: return
    Text(settingsFailureText(failure), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
}

/**
 * One on/off setting: title and an explanatory subtitle; the switch shows the Buds' value; a tap asks for the other value and the switch moves only once the
 * Buds acknowledged it. `ai-sessions/0056` U-1: while the value is not read the switch is disabled — it would otherwise show an "off" the Buds never reported
 * and a tap would write "on" blind (AGENTS.md §5). `ai-sessions/0057` D-7: the "read / changed HH:MM:SS" line is in the card's (i) ([settingTime]).
 */
@Composable
internal fun SettingSwitchRow(
    title: String,
    subtitle: String?,
    reading: SettingReading<Boolean>?,
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        Switch(
            checked = reading?.value ?: false,
            onCheckedChange = { onChange(it) },
            enabled = enabled && reading != null,
        )
    }
}
