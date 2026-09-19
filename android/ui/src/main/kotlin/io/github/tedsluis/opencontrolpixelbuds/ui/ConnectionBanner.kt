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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState

/** True only when the app's own control channels are open — every command screen gates on this. */
internal fun ConnectionState.isReady(): Boolean = this is ConnectionState.Ready

/**
 * One-line "commands won't work right now" notice shown at the top of the ANC/EQ/Find screens
 * whenever the app is not [ConnectionState.Ready] (`ai-sessions/0039`: before this, those screens
 * showed fully enabled buttons while "Connection: Disconnected", and a tap silently did nothing).
 * Renders nothing when connected.
 */
@Composable
internal fun NotConnectedBanner(state: ConnectionState, modifier: Modifier = Modifier) {
    if (state.isReady()) return
    val what = when (state) {
        ConnectionState.Connecting, ConnectionState.Discovering -> "Still connecting…"
        is ConnectionState.Failed -> "Not connected (${state.error.userMessage()})"
        else -> "Not connected to the Buds."
    }
    Text(
        text = "$what Controls are disabled — open the Connection tab to connect.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}

/**
 * Why the last ANC/Find action could not reach the Buds' Message Stream channel (DECISIONS.md
 * ADR-032) — shown on the screens whose actions need that channel. Renders nothing when there is
 * no error. The session (the Connection card) can be perfectly healthy while this is shown.
 */
@Composable
internal fun MessageStreamNotice(error: BudsError?, modifier: Modifier = Modifier) {
    if (error == null) return
    Text(
        text = error.userMessage(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
    error.technicalDetail()?.let {
        Text(text = it, style = MaterialTheme.typography.bodySmall, modifier = modifier)
    }
}

/** One-line explanation of what a tap on these screens does to the shared channel (ADR-032). */
@Composable
internal fun MessageStreamHint(modifier: Modifier = Modifier) {
    Text(
        text = "Each action briefly claims the Buds' Message Stream channel. Another app that uses it " +
            "(for example Google Play services' Fast Pair) may lose it for a moment.",
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}
