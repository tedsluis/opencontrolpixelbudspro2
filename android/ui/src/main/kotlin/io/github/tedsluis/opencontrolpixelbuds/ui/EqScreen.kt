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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure
import kotlin.math.roundToInt
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset

/**
 * The "Sound" tab (was "EQ"; ARCHITECTURE.md §2.4/§6): 5 band sliders within the confirmed
 * ±6.0 range (DECISIONS.md ADR-016) plus the confirmed presets, and — `ai-sessions/0052`, ADR-045 —
 * below them volume balance, mono audio and conversation detection. No "Save as
 * preset" affordance — PROTOCOL.md §4.2's field-16-vs-18 semantics remain
 * 🟡 HYPOTHESIS (DECISIONS.md ADR-020's own scope note), so this session does
 * not ship a UI action that specifically depends on that distinction being
 * settled.
 *
 * [gains] is nullable per ARCHITECTURE.md §3.1's table: it is `null` until the Connect-time
 * `ReadSetting 4:16` answers (or if it failed) — the UI never presents a default `0.0` quintet as if it were a
 * real reading.
 *
 * **`ai-sessions/0039`:** the controls are now shown *even while [gains] is
 * `null`*. Previously a `null` value replaced the whole screen with "change a
 * setting or wait for the Buds to report one" — but every control was hidden, so
 * there was nothing to change, and the Buds never volunteer their EQ on connect
 * (the maintainer's one successful session: the MAESTRO channel was open and
 * delivered its connect-time frame, and no EQ frame ever followed). A preset is
 * a complete quintet, so it needs no known starting value. (As first built the
 * sliders started from flat and the screen said so; since `0059` they are disabled
 * until the EQ is read, and since `ai-sessions/0069` an unread band shows "—", no number.)
 *
 * **`ai-sessions/0059` (A58-APP-02, the maintainer's choice "Disable until read", chat 2026-09-30):** the five sliders are **disabled** until the EQ has
 * been read — one band dragged from the flat start would write the four untouched bands as 0.0, values the Buds never reported (the same rule as every
 * other unread setting, U-1). The presets stay enabled: a preset is a full quintet and assumes nothing.
 *
 * **`ai-sessions/0074` (ADR-055, the maintainer's choices in chat 2026-10-06: Volume EQ at the bottom of the Equalizer card, "alleen labels"):** the
 * "Volume EQ" switch (`qhr` field 15) sits below the presets; its own "read / changed" line is in the Equalizer card's (i), an unread or last-connection
 * value marks that (i) like the EQ itself.
 */
@Composable
fun EqScreen(
    connectionState: ConnectionState,
    gains: EqBandGains?,
    eqProfileUpdatedAt: Long?,
    eqError: BudsError?,
    onGainsChanged: (EqBandGains) -> Unit,
    onPresetSelected: (EqPreset) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    settings: BudsSettings = BudsSettings(),
    settingsError: SettingsFailure? = null,
    onVolumeBalanceChanged: (Int) -> Unit = {},
    onMonoAudioChanged: (Boolean) -> Unit = {},
    onConversationDetectionChanged: (Boolean) -> Unit = {},
    onVolumeEqChanged: (Boolean) -> Unit = {},
) {
    val enabled = connectionState.isReady()
    val slidersEnabled = enabled && gains != null // A58-APP-02: no slider write from an assumed starting quintet
    val shown = gains ?: EqBandGains.FLAT
    // `ai-sessions/0069` A68-APP-02: while no session is open the last connection's values stay, dimmed and marked — never shown as current.
    val eqFromLastConnection = !enabled && gains != null
    val volumeEqFromLastConnection = settingsFromLastConnection(enabled, listOf(settings.volumeEq))
    val equalizerFromLastConnection = eqFromLastConnection || volumeEqFromLastConnection
    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { NotConnectedBanner(connectionState) }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // `ai-sessions/0057` D-7: "EQ updated: HH:MM:SS" is in the (i); the dot marks an EQ not read from the Buds yet.
                        CardTitle(
                            "Equalizer",
                            eqDetailLines(eqProfileUpdatedAt, equalizerFromLastConnection) + "$VOLUME_EQ_LABEL: ${settingTime(settings.volumeEq)}",
                            notCurrent = (enabled && (gains == null || settings.volumeEq == null)) || equalizerFromLastConnection,
                        )
                        EqStatusNotice(connectionState, gains, eqError, onRefresh)
                        Column(modifier = Modifier.alpha(if (eqFromLastConnection) NOT_CURRENT_ALPHA else 1f)) {
                            EqBandSlider("Upper treble", shown.upperTreble, slidersEnabled, known = gains != null) { onGainsChanged(shown.copy(upperTreble = it)) }
                            EqBandSlider("Treble", shown.treble, slidersEnabled, known = gains != null) { onGainsChanged(shown.copy(treble = it)) }
                            EqBandSlider("Mid", shown.mid, slidersEnabled, known = gains != null) { onGainsChanged(shown.copy(mid = it)) }
                            EqBandSlider("Bass", shown.bass, slidersEnabled, known = gains != null) { onGainsChanged(shown.copy(bass = it)) }
                            EqBandSlider("Low bass", shown.lowBass, slidersEnabled, known = gains != null) { onGainsChanged(shown.copy(lowBass = it)) }
                        }
                        Text("Presets", style = MaterialTheme.typography.titleSmall)
                        EqPresetRows(enabled, onPresetSelected)
                        Column(modifier = Modifier.alpha(if (volumeEqFromLastConnection) NOT_CURRENT_ALPHA else 1f)) {
                            SettingSwitchRow(VOLUME_EQ_LABEL, null, settings.volumeEq, enabled, onVolumeEqChanged)
                        }
                    }
                }
            }
            // `ai-sessions/0052` (DECISIONS.md ADR-045): below the EQ, as the prompt and the maintainer's layout choice put them.
            item {
                val readings = listOf(settings.volumeBalance, settings.monoAudio, settings.conversationDetection)
                val fromLastConnection = settingsFromLastConnection(enabled, readings)
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CardTitle(
                            "Balance and audio",
                            withLastConnectionLine(soundSettingsDetailLines(settings), fromLastConnection),
                            notCurrent = (enabled && readings.any { it == null }) || fromLastConnection,
                        )
                        if (enabled) SettingsFailureNotice(settingsError)
                        Column(
                            modifier = Modifier.alpha(if (fromLastConnection) NOT_CURRENT_ALPHA else 1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BalanceSlider(settings.volumeBalance, enabled, onVolumeBalanceChanged)
                            SettingSwitchRow("Mono audio", "Same sound in both ears", settings.monoAudio, enabled, onMonoAudioChanged)
                            SettingSwitchRow(
                                "Conversation detection",
                                "Switch from noise cancellation to transparency when you talk",
                                settings.conversationDetection,
                                enabled,
                                onConversationDetectionChanged,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The EQ card's (i): "EQ updated: HH:MM:SS" — the line the card showed before `ai-sessions/0057` — or, before any read, where the value comes from. */
internal fun eqDetailLines(eqProfileUpdatedAt: Long?, fromLastConnection: Boolean = false): List<String> = withLastConnectionLine(
    listOf(formatUpdatedAt(eqProfileUpdatedAt)?.let { "EQ updated: $it" } ?: EQ_NOT_READ_DETAIL),
    fromLastConnection,
)

/** Shown in place of a number that was not read from the Buds (A68-APP-08) — never a default drawn as a reading. */
internal const val NOT_READ_VALUE: String = "—"

/** `ai-sessions/0074`: the label of `qhr` field 15 (the official app's own, PROTOCOL.md §4.5.6); no note — the maintainer's choice "alleen labels". */
internal const val VOLUME_EQ_LABEL: String = "Volume EQ"

internal const val EQ_NOT_READ_DETAIL: String = "The EQ is read from the Buds at Connect and with \"Read EQ again\"; it has not been read on this connection yet."

/** The settings card's (i) on Sound: each setting's own time line ([settingTime]); the balance with its value, as the row showed it before `ai-sessions/0057`. */
internal fun soundSettingsDetailLines(settings: BudsSettings): List<String> = listOf(
    "Balance: " + (settings.volumeBalance?.let { "${balanceText(it.value)} · ${settingTime(it)}" } ?: SETTING_NOT_READ),
    "Mono audio: ${settingTime(settings.monoAudio)}",
    "Conversation detection: ${settingTime(settings.conversationDetection)}",
)

/**
 * The presets in rows of three (`ai-sessions/0052`, the maintainer's choice "2 rijen: 3 + 2"; 3 + 3 since `ai-sessions/0069` added Flat):
 * `[HEAVY BASS] [LIGHT BASS] [BALANCED]` / `[VOCAL BOOST] [CLARITY] [FLAT]`. One [Row] per chunk with equal-width chips — not `FlowRow`, which is `@ExperimentalLayoutApi` in the pinned
 * `foundation-layout` 1.7.0 (`ai-sessions/0051` §11). A smaller label style keeps "VOCAL BOOST" on one line in a third of a 360 dp screen.
 */
@Composable
private fun EqPresetRows(enabled: Boolean, onPresetSelected: (EqPreset) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        EqPreset.entries.chunked(PRESETS_PER_ROW).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { preset ->
                    AssistChip(
                        onClick = { onPresetSelected(preset) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(
                                preset.name.replace('_', ' '),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                    )
                }
                // The short last row keeps the same chip width as the full ones.
                repeat(PRESETS_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val PRESETS_PER_ROW = 3

/**
 * `ai-sessions/0041`: the EQ is *read* from the Buds at Connect (`ReadSetting 4:16`, DECISIONS.md ADR-034), so the "unknown"
 * wording only appears when that read failed — with the reason, never a generic message — and a failed write is reported the
 * same way instead of being assumed to have worked.
 */
@Composable
private fun EqStatusNotice(
    connectionState: ConnectionState,
    gains: EqBandGains?,
    eqError: BudsError?,
    onRefresh: () -> Unit,
) {
    if (!connectionState.isReady()) return
    if (eqError != null) {
        Text(
            (if (gains == null) "Couldn't read the Buds' current EQ. " else "The last EQ request failed. ") + eqError.userMessage(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        TextButton(onClick = onRefresh) { Text("Read EQ again") }
    } else if (gains == null) {
        Text(
            "Reading the Buds' current EQ… The sliders are off until it arrives; a preset can be chosen now.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    // `ai-sessions/0057`: "EQ updated: HH:MM:SS" is in the card's (i) ([eqDetailLines]).
}

/**
 * Volume balance (`qhr` field 17, ADR-026/045). The wire's +100 is **Left**, so the slider runs from Left (its left end) to Right: slider position =
 * −value. One write per completed drag, as the EQ sliders; the text is the Buds' value ("Left 40"), not the finger position. **`ai-sessions/0054` I-3:** a
 * release within ±3 of the centre writes 0 ("Centre", [BudsSettings.snapBalance]). **`ai-sessions/0057` F-1:** the knob follows the finger only while it is
 * down; on release it shows the Buds' value again and moves only when they acknowledge the write — a refused or unanswered write leaves it where the Buds
 * are (it used to stay at the finger position). Not read from the Buds yet ⇒ disabled (U-1's rule; the time is in the card's (i)).
 */
@Composable
private fun BalanceSlider(reading: SettingReading<Int>?, enabled: Boolean, onChange: (Int) -> Unit) {
    val buds = reading?.value ?: 0
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Balance")
            Text(if (reading != null) balanceText(buds) else NOT_READ_VALUE, style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("L")
            Slider(
                value = dragPosition ?: -buds.toFloat(),
                onValueChange = { dragPosition = it },
                onValueChangeFinished = {
                    dragPosition?.let { onChange(BudsSettings.snapBalance(-it.roundToInt())) }
                    dragPosition = null
                },
                enabled = enabled && reading != null,
                valueRange = -100f..100f,
                modifier = Modifier.weight(1f),
            )
            Text("R")
        }
    }
}

/**
 * [onValueChange] fires once per completed drag ([Slider]'s own `onValueChangeFinished`), not per drag-frame — each call sends a real frame over the RFCOMM
 * `MAESTRO` channel (`BudsRepositoryImpl.setEqGains`). **`ai-sessions/0057` F-1:** the knob and its number follow the finger only while it is down
 * ([dragValue]); after release they show [value] — the Buds' EQ — again, so a refused or unanswered write never leaves a gain on screen the Buds did not take.
 */
@Composable
private fun EqBandSlider(label: String, value: Float, enabled: Boolean, known: Boolean = true, onValueChange: (Float) -> Unit) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val shown = dragValue ?: value
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            // A68-APP-08 (`ai-sessions/0069`, the maintainer's choice "Visual '—' only"): an EQ that was not read shows no number — "0.0" would be a value
            // the Buds never reported. (What a screen reader says for it is a later step, `TODO.md`.)
            Text(if (known) "%.1f".format(shown) else NOT_READ_VALUE)
        }
        Slider(
            value = shown,
            onValueChange = { dragValue = it },
            onValueChangeFinished = {
                dragValue?.let(onValueChange)
                dragValue = null
            },
            enabled = enabled,
            valueRange = EqBandGains.RANGE.start..EqBandGains.RANGE.endInclusive,
        )
    }
}
