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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
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

/**
 * What a screen reader says in place of [NOT_READ_VALUE] (`ai-sessions/0074`, the maintainer's choice in chat 2026-10-06: *"Not read from the Buds yet"* —
 * the words of the (i) lines, so one term is used throughout; `TODO.md` §5 "Accessibility", the half deferred on 2026-10-03).
 */
internal const val NOT_READ_DESCRIPTION: String = SETTING_NOT_READ

/**
 * [NOT_READ_VALUE] ("—") for a value the Buds have not reported on this connection — an EQ band, the balance, a switch — with [NOT_READ_DESCRIPTION] as its
 * content description, so a screen reader says what the dash means instead of reading (or skipping) the character.
 */
@Composable
internal fun NotReadValue(modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.bodyLarge) {
    Text(NOT_READ_VALUE, style = style, modifier = modifier.semantics { contentDescription = NOT_READ_DESCRIPTION })
}

/**
 * `ai-sessions/0069` A68-APP-02: no session is open and at least one of a card's settings still holds a value — the last connection's. The card then keeps
 * the values, dimmed, with the (i) dot and [FROM_LAST_CONNECTION_DETAIL] as its first detail line. (At the next Connect the values are reset to "not read"
 * until that connection's reads answer, as before.)
 */
internal fun settingsFromLastConnection(ready: Boolean, readings: List<SettingReading<*>?>): Boolean = !ready && readings.any { it != null }

/** [lines] with [FROM_LAST_CONNECTION_DETAIL] in front when [fromLastConnection]. */
internal fun withLastConnectionLine(lines: List<String>, fromLastConnection: Boolean): List<String> =
    if (fromLastConnection) listOf(FROM_LAST_CONNECTION_DETAIL) + lines else lines

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
 * One settings card (Controls since `ai-sessions/0057`; gear → Settings since `ai-sessions/0082` item 5). The (i) carries the dot while connected and one of
 * [readings] is not read yet ([notRead]), or — `ai-sessions/0069` A68-APP-02 — while not connected and the card still shows the last connection's values,
 * which are then dimmed and named as such in the first detail line.
 */
@Composable
internal fun SettingsCard(
    title: String,
    detailLines: List<String>,
    enabled: Boolean,
    readings: List<SettingReading<*>?>,
    notRead: Boolean = readings.any { it == null },
    content: @Composable () -> Unit,
) {
    val fromLastConnection = settingsFromLastConnection(enabled, readings)
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CardTitle(title, withLastConnectionLine(detailLines, fromLastConnection), (enabled && notRead) || fromLastConnection)
            Column(
                modifier = Modifier.alpha(if (fromLastConnection) NOT_CURRENT_ALPHA else 1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) { content() }
        }
    }
}

/**
 * One on/off setting: title and an explanatory subtitle; the switch shows the Buds' value; a tap asks for the other value and the switch moves only once the
 * Buds acknowledged it. `ai-sessions/0056` U-1: while the value is not read no tap can write it blind (AGENTS.md §5). `ai-sessions/0057` D-7: the "read /
 * changed HH:MM:SS" line is in the card's (i) ([settingTime]). **`ai-sessions/0074`** (the maintainer's choice in chat 2026-10-06, "— voor alle
 * schakelaars"; ADR-052/054/055): a value not read on this connection shows [NotReadValue] ("—") **in place of** the switch — not a disabled "off" the Buds
 * never reported; a value from the last connection stays a disabled switch (dimmed by its card).
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
        if (reading == null) {
            NotReadValue()
        } else {
            Switch(checked = reading.value, onCheckedChange = { onChange(it) }, enabled = enabled)
        }
    }
}
