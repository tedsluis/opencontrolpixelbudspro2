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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/*
 * The (i) details of `ai-sessions/0057` (D-7, the maintainer's choice "Dialog per card + dot", 2026-09-29): the times and state words of a card ("updated / read /
 * changed HH:MM:SS", "last seen", "not read from the Buds yet", "from the last connection", …) and its explanations are shown in an AlertDialog opened by the
 * card's (i). Every line is produced by the same helper the screen used before (`formatUpdatedAt`, `settingTime`, `budLine`, …) — never re-worded or
 * re-computed. On the main surface a value that is not current keeps a non-text marker: a dot on the (i), the description "Details — not current", the value
 * dimmed ([NOT_CURRENT_ALPHA]) and — for a setting not read yet — its control disabled (U-1, `ai-sessions/0056`).
 */

/** How much a value that is not current (stale, from an earlier connection) is dimmed on the main surface — always together with the (i) dot, never alone. */
internal const val NOT_CURRENT_ALPHA: Float = 0.6f

/**
 * The first (i) line of a card whose values are not current because no session is open (`ai-sessions/0069`, A68-APP-02; the maintainer's choice in chat
 * 2026-10-03, "Keep the value, mark it"): the values stay visible, dimmed, with the (i) dot — never shown as the Buds' current state.
 */
internal const val FROM_LAST_CONNECTION_DETAIL: String = "From the last connection — the app is not connected to the Buds now."

internal const val DETAILS_DESCRIPTION: String = "Details"
internal const val DETAILS_NOT_CURRENT_DESCRIPTION: String = "Details — not current"

/**
 * The (i) of a card: a 48 dp [IconButton] (touch target, Android accessibility guidance) with [OpenControlIcons.Info]. [notCurrent] adds the dot and changes the
 * description, so the state is not carried by colour alone (WCAG 1.4.1). Tapping it opens [DetailsDialog] with [title] and [lines].
 */
@Composable
internal fun InfoButton(title: String, lines: List<String>, modifier: Modifier = Modifier, notCurrent: Boolean = false) {
    var open by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { open = true }, modifier = modifier) {
        Box {
            Icon(OpenControlIcons.Info, contentDescription = if (notCurrent) "$title: $DETAILS_NOT_CURRENT_DESCRIPTION" else "$title: $DETAILS_DESCRIPTION")
            if (notCurrent) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                )
            }
        }
    }
    if (open) DetailsDialog(title, lines) { open = false }
}

/** The details of one card: its lines exactly as given, scrollable, closed with "Close" or back. */
@Composable
internal fun DetailsDialog(title: String, lines: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        },
    )
}

/** A card's title row: the title on the left, the (i) on the right. */
@Composable
internal fun CardTitle(title: String, lines: List<String>, notCurrent: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(end = 8.dp))
        InfoButton(title, lines, notCurrent = notCurrent)
    }
}
