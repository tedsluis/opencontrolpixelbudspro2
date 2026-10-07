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
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList
import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure

/**
 * The "Controls" tab (`ai-sessions/0052`, the maintainer's choice in chat 2026-09-26): "Use touch controls" (`qhr` field 4), the press-and-hold
 * action per bud (field 7) — both writable under DECISIONS.md ADR-045 — the press-and-hold ANC-mode list (field 12, ADR-046) and the "In-ear detection"
 * setting (field 2, ADR-047) — `ai-sessions/0056`, the texts and placement the maintainer chose in chat 2026-09-28. **`ai-sessions/0074`** (the maintainer's
 * choices in chat 2026-10-06: "Gebaren eerst", "alleen labels"): "Head gestures" (field 29, ADR-052) below "Press and hold", "Multipoint" (field 11,
 * ADR-053) below "In-ear detection", then "Case sounds" (fields 28 and 27, ADR-054) — labels only.
 */
@Composable
fun ControlsScreen(
    connectionState: ConnectionState,
    settings: BudsSettings,
    settingsError: SettingsFailure?,
    onTouchControlsChanged: (Boolean) -> Unit,
    onPressAndHoldChanged: (Bud, HoldAction) -> Unit,
    onAncModeSelectedChanged: (AncMode, Boolean) -> Unit,
    onInEarDetectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onMultipointChanged: (Boolean) -> Unit = {},
    onHeadGesturesChanged: (Boolean) -> Unit = {},
    onCaseSoundEarbudsReplacedChanged: (Boolean) -> Unit = {},
    onCaseSoundOtherAlertsChanged: (Boolean) -> Unit = {},
) {
    val enabled = connectionState.isReady()
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NotConnectedBanner(connectionState)
            if (enabled) SettingsFailureNotice(settingsError)

            // `ai-sessions/0057`: one card per group; each card's (i) has its settings' "read / changed HH:MM:SS" lines ([settingTime]) and a dot while one of
            // them is not read from the Buds yet. Functionally unchanged (D-1 … D-4).
            SettingsCard("Touch controls", listOf("Use touch controls: ${settingTime(settings.touchControls)}"), enabled, listOf(settings.touchControls)) {
                SettingSwitchRow("Use touch controls", null, settings.touchControls, enabled, onTouchControlsChanged)
            }
            SettingsCard(
                "Press and hold",
                pressAndHoldDetailLines(settings),
                enabled,
                listOfNotNull(settings.holdLeft, settings.holdRight, settings.ancModeList.takeIf { showAncModeList(settings) }),
                notRead = pressAndHoldNotRead(settings),
            ) {
                Text(DIGITAL_ASSISTANT_NOTE, style = MaterialTheme.typography.bodySmall)
                HoldRow("Left", settings.holdLeft, enabled) { onPressAndHoldChanged(Bud.LEFT, it) }
                HoldRow("Right", settings.holdRight, enabled) { onPressAndHoldChanged(Bud.RIGHT, it) }
                if (showAncModeList(settings)) AncModeListSection(settings.ancModeList, enabled, onAncModeSelectedChanged)
            }
            SettingsCard(HEAD_GESTURES_TITLE, listOf("$HEAD_GESTURES_LABEL: ${settingTime(settings.headGestures)}"), enabled, listOf(settings.headGestures)) {
                SettingSwitchRow(HEAD_GESTURES_LABEL, null, settings.headGestures, enabled, onHeadGesturesChanged)
            }
            SettingsCard("In-ear detection", listOf("In-ear detection: ${settingTime(settings.inEarDetection)}"), enabled, listOf(settings.inEarDetection)) {
                SettingSwitchRow("In-ear detection", IN_EAR_DETECTION_SUBTITLE, settings.inEarDetection, enabled, onInEarDetectionChanged)
                Text(IN_EAR_DETECTION_OFF_NOTE, style = MaterialTheme.typography.bodySmall)
            }
            SettingsCard(MULTIPOINT_LABEL, listOf("$MULTIPOINT_LABEL: ${settingTime(settings.multipoint)}"), enabled, listOf(settings.multipoint)) {
                SettingSwitchRow(MULTIPOINT_LABEL, null, settings.multipoint, enabled, onMultipointChanged)
            }
            SettingsCard(
                CASE_SOUNDS_TITLE,
                listOf(
                    "$CASE_SOUND_EARBUDS_REPLACED_LABEL: ${settingTime(settings.caseSoundEarbudsReplaced)}",
                    "$CASE_SOUND_OTHER_ALERTS_LABEL: ${settingTime(settings.caseSoundOtherAlerts)}",
                ),
                enabled,
                listOf(settings.caseSoundEarbudsReplaced, settings.caseSoundOtherAlerts),
            ) {
                SettingSwitchRow(CASE_SOUND_EARBUDS_REPLACED_LABEL, null, settings.caseSoundEarbudsReplaced, enabled, onCaseSoundEarbudsReplacedChanged)
                SettingSwitchRow(CASE_SOUND_OTHER_ALERTS_LABEL, null, settings.caseSoundOtherAlerts, enabled, onCaseSoundOtherAlertsChanged)
            }
        }
    }
}

/**
 * One settings card. The (i) carries the dot while connected and one of [readings] is not read yet ([notRead]), or — `ai-sessions/0069` A68-APP-02 — while
 * not connected and the card still shows the last connection's values, which are then dimmed and named as such in the first detail line.
 */
@Composable
private fun SettingsCard(
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

/** The press-and-hold card's (i): each bud's action with its time and, while shown, the ANC-mode list's time — the lines this card showed before `0057`. */
internal fun pressAndHoldDetailLines(settings: BudsSettings): List<String> = listOfNotNull(
    "Left: " + (settings.holdLeft?.let { "${holdActionText(it.value)} · ${settingTime(it)}" } ?: SETTING_NOT_READ),
    "Right: " + (settings.holdRight?.let { "${holdActionText(it.value)} · ${settingTime(it)}" } ?: SETTING_NOT_READ),
    if (showAncModeList(settings)) "$ANC_MODE_LIST_TITLE: ${settingTime(settings.ancModeList)}" else null,
)

private fun pressAndHoldNotRead(settings: BudsSettings): Boolean =
    settings.holdLeft == null || settings.holdRight == null || (showAncModeList(settings) && settings.ancModeList == null)

/** One bud's press-and-hold action: the Buds' value is the selected chip; a tap on the other one writes it (ADR-045). */
@Composable
private fun HoldRow(label: String, reading: SettingReading<HoldAction>?, enabled: Boolean, onSelect: (HoldAction) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyLarge) // the selected chip is the Buds' value; "not read" and the time are in the (i)
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

/**
 * `ai-sessions/0056` (the maintainer's choice "Hidden unless Noise control", chat 2026-09-28): the list is shown only while the Left **or** Right press
 * and hold reads "Noise control" — as the official app shows its checklist only under that choice (`hcy.java:42-44`, `hgr.java:40-56`). A press-and-hold
 * action not read from the Buds hides it (nothing is claimed).
 */
internal fun showAncModeList(settings: BudsSettings): Boolean =
    settings.holdLeft?.value == HoldAction.NOISE_CONTROL || settings.holdRight?.value == HoldAction.NOISE_CONTROL

/** The four boxes in the official app's on-screen order (`CAP-056` film, 17:31:33) — which wire boolean each sets is [AncModeList]'s job. */
internal val ANC_MODE_LIST_ORDER: List<Pair<AncMode, String>> = listOf(
    AncMode.ACTIVE to "Noise cancellation",
    AncMode.OFF to "Off",
    AncMode.ADAPTIVE to "Adaptive",
    AncMode.TRANSPARENT to "Transparency",
)

internal const val ANC_MODE_LIST_TITLE: String = "Modes for press and hold (both buds)"

/** Shown under the list; the two last ticked boxes are disabled ("Box disabled + line"). */
internal const val ANC_MODE_LIST_MIN_TEXT: String = "At least two modes must stay selected."

/**
 * `ai-sessions/0074`: the label of `qhr` field 11 — the setting's name (PROTOCOL.md §4.5.2; the official screen's switch reads "Use multipoint",
 * `CAP-069-FINDINGS.md` §2), the label the maintainer chose in chat 2026-10-06; no note ("alleen labels").
 */
internal const val MULTIPOINT_LABEL: String = "Multipoint"

/** `ai-sessions/0074`: field 29's card and switch — "Use head gestures" is the official app's label (PROTOCOL.md §4.5.4); no note ("alleen labels"). */
internal const val HEAD_GESTURES_TITLE: String = "Head gestures"
internal const val HEAD_GESTURES_LABEL: String = "Use head gestures"

/**
 * `ai-sessions/0074`: the "Case sounds" card — ADR-054's labels, chosen by the maintainer in chat 2026-10-06: 28 = "Earbuds replaced" (the official settings
 * list's wording; its switch reads "Bud return", PROTOCOL.md §4.5.8), 27 = "Other alerts"; no note.
 */
internal const val CASE_SOUNDS_TITLE: String = "Case sounds"
internal const val CASE_SOUND_EARBUDS_REPLACED_LABEL: String = "Earbuds replaced"
internal const val CASE_SOUND_OTHER_ALERTS_LABEL: String = "Other alerts"

internal const val IN_EAR_DETECTION_SUBTITLE: String = "Pauses audio when you take a bud out and resumes it when you put it back."

/** Always shown ("Always, full text"): what "off" changes (`CAP-056-FINDINGS.md` §4); the Settable byte with it off is 🔴, hence "may not apply". */
internal const val IN_EAR_DETECTION_OFF_NOTE: String =
    "With it off, audio does not pause when you take a bud out, and the 'only while worn' check for changing noise control may not apply."

/** U-2: the text of [io.github.tedsluis.opencontrolpixelbuds.domain.BudsError.SessionOpening]. */
internal const val SESSION_OPENING_TEXT: String = "The app's channel is being reopened — try again in a moment."

/**
 * Field 12 (ADR-046): one box per mode; a box shows the Buds' value and a tap asks for the other — the box changes only on the Buds' OK. Not read ⇒ all
 * boxes disabled (U-1); a box whose untick would leave fewer than two is disabled ([AncModeList.isLocked]).
 */
@Composable
private fun AncModeListSection(reading: SettingReading<AncModeList>?, enabled: Boolean, onChange: (AncMode, Boolean) -> Unit) {
    Column {
        Text(ANC_MODE_LIST_TITLE, style = MaterialTheme.typography.titleMedium)
        val list = reading?.value
        ANC_MODE_LIST_ORDER.forEach { (mode, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = list?.isSelected(mode) ?: false,
                    onCheckedChange = { onChange(mode, it) },
                    enabled = enabled && list != null && !list.isLocked(mode),
                )
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (list != null && list.selectedCount <= AncModeList.MIN_SELECTED) {
            Text(ANC_MODE_LIST_MIN_TEXT, style = MaterialTheme.typography.bodySmall)
        }
    }
}
