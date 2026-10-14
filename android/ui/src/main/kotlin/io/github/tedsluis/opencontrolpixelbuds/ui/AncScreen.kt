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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeCause
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ChannelTaken
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.isCurrent

/**
 * Minimal, stateless ANC control screen — takes plain domain values, not a ViewModel, matching `MainActivity`'s own state-hoisting-in-the-Activity pattern.
 * [ancMode] is the Buds' own report (a `Notify`, or the ACK of a `Set`) — ARCHITECTURE.md §3.1 — and [onRefreshAncMode] re-queries it (`ai-sessions/0038`).
 *
 * **I-1 (`ai-sessions/0054`, replaces `0048` I-3's disabled buttons):** while [ancAvailability] is [AncAvailability.NOT_ALLOWED] (the Buds' last `Notify`
 * reported Settable `0x00`) the mode buttons **stay enabled** under [ancNotAllowedLine]: a tap first asks the Buds again and switches only if they now allow it
 * (since `ai-sessions/0062` F-1 every tap asks first, whatever the last reading).
 *
 * **`ai-sessions/0062` F-3:** while [ancModeUnconfirmedAt] is set (the answer to a change was cut off — the Buds may have switched) the mode buttons keep the
 * last confirmed mode, dimmed, the (i) carries the not-current dot and its first line says why, until the Buds' next `Notify` clears it.
 *
 * **`ai-sessions/0057`:** four large mode buttons (2 × 2) — the Buds' current mode is the filled one with a check mark (not colour alone), the others outlined;
 * an unknown mode fills none and says "ANC mode: unknown". The times ("updated …", "checked …"), the session line and the Message-Stream explanation are in
 * the card's (i) details, from the same helpers.
 *
 * **`ai-sessions/0082` item 1 (the maintainer's wording, chat 2026-10-09):** while [ancModeCause] is [AncModeCause.CHANGED_BY_BUDS] — a `Notify` the app did
 * not provoke changed the mode — the (i) carries [ancChangedByBudsLine]; nothing on the main surface, no notification.
 *
 * **DECISIONS.md ADR-061 (`ai-sessions/0084`):** while this tab is shown the repository holds the Message Stream claim (reported by [OpenControlNavHost]), so
 * such a `Notify` reaches the app; the (i) ends with [ANC_HOLD_HINT_TEXT].
 *
 * **DECISIONS.md ADR-062 (`ai-sessions/0087`, the maintainer's texts in chat 2026-10-12):** while [channelTaken] is set — the held channel was closed by another
 * app (Google Play services' Fast Pair, `CAP-074`) and, in the "take it back" mode, the re-claims are exhausted — [ancChannelTakenLine] is shown below the mode
 * buttons and in the (i); it clears when the app holds the channel again. **ADR-062 Update 2 (`ai-sessions/0089`, the maintainer's choices in chat 2026-10-13):**
 * under that line the card offers [TAKE_IT_BACK_AGAIN_LABEL] ([onReclaimAncChannel]: a new chain and the existing claim with its `Get`, no `Set`), enabled only
 * while the session is `Ready`; a mode the app's own `Get` found changed since it last saw it ([AncModeCause.CHANGED_SINCE_LAST_READ]) gets
 * [ancChangedSinceLastReadLine] in the (i).
 */
@Composable
fun AncScreen(
    connectionState: ConnectionState,
    messageStreamError: BudsError?,
    ancMode: AncMode?,
    ancModeUpdatedAt: Long?,
    ancAvailability: AncAvailability,
    onAncModeSelected: (AncMode) -> Unit,
    onRefreshAncMode: () -> Unit,
    onRequestAddAncTile: () -> Unit,
    modifier: Modifier = Modifier,
    ancAvailabilityUpdatedAt: Long? = null,
    ancModeUnconfirmedAt: Long? = null,
    sessionSince: Long? = null,
    ancModeCause: AncModeCause? = null,
    channelTaken: ChannelTaken? = null,
    onReclaimAncChannel: () -> Unit = {},
) {
    val ready = connectionState.isReady()
    val notAllowed = ready && ancAvailability == AncAvailability.NOT_ALLOWED
    val unconfirmed = ancModeUnconfirmedAt != null
    // `ai-sessions/0069` A68-APP-02: a mode the Buds reported on an earlier connection (or while nothing is connected now) is kept, dimmed and marked —
    // until this connection's first Notify or ACK ([isCurrent], the one rule of every tab).
    val fromLastConnection = ancMode != null && !isCurrent(ready, ancModeUpdatedAt, sessionSince)
    val notCurrent = unconfirmed || fromLastConnection
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NotConnectedBanner(connectionState)
            MessageStreamNotice(messageStreamError)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CardTitle(
                        "Noise control",
                        ancDetailLines(
                            connectionState, ancMode, ancModeUpdatedAt, notAllowed, ancAvailabilityUpdatedAt, ancModeUnconfirmedAt, fromLastConnection, ancModeCause,
                            channelTaken,
                        ),
                        notCurrent = notCurrent,
                    )
                    if (ancMode == null) Text(ancModeLine(null, null), style = MaterialTheme.typography.bodyLarge)
                    // The reason a tap may not switch stays visible; its time is in the (i).
                    if (notAllowed) Text(ancNotAllowedLine(null), style = MaterialTheme.typography.bodyMedium)
                    // ADR-062: the channel was taken by another app — on the main surface too (the maintainer's choice in chat 2026-10-12, "Hint texts"), with its time.
                    if (channelTaken != null) {
                        Text(ancChannelTakenLine(channelTaken), style = MaterialTheme.typography.bodyMedium)
                        // ADR-062 Update 2 ("one button, both modes"): the claim again, without a mode write.
                        TextButton(onClick = onReclaimAncChannel, enabled = ready, modifier = Modifier.align(Alignment.End)) { Text(TAKE_IT_BACK_AGAIN_LABEL) }
                    }
                    ANC_MODE_LIST_ORDER.chunked(2).forEach { row ->
                        // F-3 / A68-APP-02: a mode that is not confirmed or not current is dimmed — always together with the (i) dot and its line, never alone
                        // (WCAG 1.4.1).
                        val rowModifier = Modifier.fillMaxWidth().alpha(if (notCurrent) NOT_CURRENT_ALPHA else 1f)
                        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { (mode, label) ->
                                AncModeButton(label, selected = ancMode == mode, enabled = ready, modifier = Modifier.weight(1f)) { onAncModeSelected(mode) }
                            }
                        }
                    }
                    TextButton(onClick = onRefreshAncMode, enabled = ready, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Refresh") }
                }
            }
            TextButton(onClick = onRequestAddAncTile) { Text("Add ANC Quick Settings tile") }
        }
    }
}

/** One mode: filled with a check mark when it is the Buds' current mode, outlined otherwise; 72 dp high. */
@Composable
private fun AncModeButton(label: String, selected: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val content: @Composable () -> Unit = {
        if (selected) Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 4.dp))
        Text(label, textAlign = TextAlign.Center)
    }
    val sized = modifier.height(72.dp).semantics { this.selected = selected }
    if (selected) {
        Button(onClick = onClick, enabled = enabled, modifier = sized) { content() }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = sized) { content() }
    }
}

/** "ANC mode: X (updated HH:MM:SS)" / "ANC mode: unknown" — the line the screen showed before `ai-sessions/0057`, now the first line of its (i). */
internal fun ancModeLine(ancMode: AncMode?, updatedAt: Long?, fromLastConnection: Boolean = false): String = when {
    ancMode == null -> "ANC mode: unknown"
    // The maintainer's wording (chat 2026-10-03): "Mode: … - from the last connection (updated 14:32:07)".
    fromLastConnection -> "ANC mode: ${ancMode.name} — from the last connection" + (formatUpdatedAt(updatedAt)?.let { " (updated $it)" } ?: "")
    else -> "ANC mode: ${ancMode.name}" + (formatUpdatedAt(updatedAt)?.let { " (updated $it)" } ?: "")
}

/**
 * The ANC card's (i) lines: the not-confirmed line after a cut-off answer (F-3), the mode with its time, the "changed by the Buds" line when a `Notify` the app did
 * not provoke changed the mode (`ai-sessions/0082` item 1) or the "changed since the app last saw it" line (ADR-062 Update 2), the "checked HH:MM:SS" line while not allowed, the channel-taken line (ADR-062), the session line, the
 * hold explanation (ADR-061).
 */
internal fun ancDetailLines(
    connectionState: ConnectionState,
    ancMode: AncMode?,
    ancModeUpdatedAt: Long?,
    notAllowed: Boolean,
    checkedAt: Long?,
    unconfirmedAt: Long? = null,
    fromLastConnection: Boolean = false,
    cause: AncModeCause? = null,
    channelTaken: ChannelTaken? = null,
): List<String> = listOfNotNull(
    unconfirmedAt?.let(::ancUnconfirmedLine),
    ancModeLine(ancMode, ancModeUpdatedAt, fromLastConnection),
    if (cause == AncModeCause.CHANGED_BY_BUDS && ancMode != null) ancChangedByBudsLine(ancModeUpdatedAt) else null,
    if (cause == AncModeCause.CHANGED_SINCE_LAST_READ && ancMode != null) ancChangedSinceLastReadLine(ancModeUpdatedAt) else null,
    if (notAllowed) ancNotAllowedLine(checkedAt) else null,
    channelTaken?.let(::ancChannelTakenLine),
    "Connection: ${connectionState::class.simpleName}",
    ANC_HOLD_HINT_TEXT,
)

/**
 * DECISIONS.md ADR-061 (the maintainer's text in chat 2026-10-10, `ai-sessions/0084`): the noise-control card's (i) says that the channel is held while this tab
 * is open — Find keeps [MESSAGE_STREAM_HINT_TEXT], since its taps still claim briefly. Corrected by ADR-062 (`ai-sessions/0087`: Play's Fast Pair takes the
 * channel back, `CAP-074`) and reworded by ADR-062 Update 2 (`ai-sessions/0089`, the maintainer's text in chat 2026-10-13, "ANC (i)") with the card's new name.
 */
internal const val ANC_HOLD_HINT_TEXT: String = "While this tab is open the app keeps the Buds' noise-control channel, so a press-and-hold on a bud shows " +
    "here; it is released 1.5 seconds after you leave the tab. On a phone with Google Play services, Play's Fast Pair takes the channel back about 5 " +
    "seconds later; what this app does then is set on the Settings tab under Noise control with Google Play services."

/** ADR-062 Update 2 (`ai-sessions/0089`, the maintainer's choice in chat 2026-10-13, "Button"): the hint's action, the same label in both modes. */
internal const val TAKE_IT_BACK_AGAIN_LABEL: String = "Take it back again"

/**
 * DECISIONS.md ADR-062 — the maintainer's texts in chat 2026-10-12 ("Hint texts"), reworded by ADR-062 Update 2 (`ai-sessions/0089`, chat 2026-10-13, "Hint
 * text"): the line while [ChannelTaken] is set — with the close's time, and in the "Google Play services" mode with the re-claims made before the app stopped.
 * Play services' Fast Pair is named as the usual taker only (the app cannot see who took the channel — the stack closed its socket, `CAP-075-FINDINGS.md` §5).
 */
internal fun ancChannelTakenLine(taken: ChannelTaken): String {
    val time = formatUpdatedAt(taken.atMillis)?.let { " at $it" } ?: ""
    val reclaimed = when (taken.reclaimAttempts) {
        0 -> ""
        1 -> "; this app took it back once and stopped"
        else -> "; this app took it back ${taken.reclaimAttempts} times and stopped"
    }
    return "Another app took the noise-control channel$time — usually Google Play services' Fast Pair$reclaimed. A press-and-hold on a bud is not shown " +
        "until this app has the channel again: tap $TAKE_IT_BACK_AGAIN_LABEL, a mode, Refresh, or pull down."
}

/**
 * `ai-sessions/0062` F-2 (the maintainer's wording, chat 2026-10-01): "The Buds don't allow changing noise control right now (usually because no bud is in an
 * ear; checked 10:07:54). Tapping a mode checks again first." — the time is when the Buds last answered; without one it is left out (the main surface).
 */
internal fun ancNotAllowedLine(checkedAt: Long?): String =
    ANC_NOT_ALLOWED_TEXT.removeSuffix(").") + (formatUpdatedAt(checkedAt)?.let { "; checked $it" } ?: "") + "). Tapping a mode checks again first."

/**
 * `ai-sessions/0082` item 1 (the maintainer's wording, chat 2026-10-09): the (i) line while the shown mode came from a `Notify` the app did not provoke — a
 * press-and-hold on a bud (`CAP-045` 1583/1755/1818) or the Buds' own change (`CAP-067` §2); the app cannot tell which and says so. [changedAt] is the
 * mode's own receive time; without one the time is left out.
 */
internal fun ancChangedByBudsLine(changedAt: Long?): String =
    "Changed by the Buds" + (formatUpdatedAt(changedAt)?.let { " at $it" } ?: "") + " (a press-and-hold on a bud, or the Buds' own change)."

/**
 * ADR-062 Update 2 (`ai-sessions/0089`, the maintainer's wording in chat 2026-10-13, "Cause line"): the (i) line while the shown mode came from the app's own
 * `Get` and differs from the mode it last saw on this connection — `CAP-075` 2980/5102/5361: the change was made on Play services' claim or between claims.
 * [readAt] is the mode's own receive time; without one the time is left out.
 */
internal fun ancChangedSinceLastReadLine(readAt: Long?): String =
    "Changed since the app last saw it" + (formatUpdatedAt(readAt)?.let { " (read at $it)" } ?: "") +
        ": a press-and-hold on a bud or the Buds' own change, made while this app did not have the channel."

/** F-3 (the maintainer's wording, chat 2026-10-01): the (i) line while the shown mode is not confirmed; [changedAt] is when the cut-off change was sent. */
internal fun ancUnconfirmedLine(changedAt: Long): String =
    "Not confirmed: the answer to the change" + (formatUpdatedAt(changedAt)?.let { " at $it" } ?: "") +
        " was cut off — the Buds may have switched. Tap Refresh."

@Preview(showBackground = true)
@Composable
private fun AncScreenPreview() {
    MaterialTheme {
        AncScreen(
            connectionState = ConnectionState.Ready,
            messageStreamError = null,
            ancMode = AncMode.ADAPTIVE,
            ancModeUpdatedAt = System.currentTimeMillis(),
            ancAvailability = AncAvailability.ALLOWED,
            onAncModeSelected = {},
            onRefreshAncMode = {},
            onRequestAddAncTile = {},
        )
    }
}
