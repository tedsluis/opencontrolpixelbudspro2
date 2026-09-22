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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset

/**
 * EQ screen (ARCHITECTURE.md §2.4/§6): 5 band sliders within the confirmed
 * ±6.0 range (DECISIONS.md ADR-016) plus the confirmed presets. No "Save as
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
 * a complete quintet and a slider move sends all five bands, so neither needs a
 * known starting value — the sliders simply start from flat and the screen says
 * plainly that this is *not* the Buds' current setting.
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
) {
    val enabled = connectionState.isReady()
    val shown = gains ?: EqBandGains.FLAT
    Surface(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Text("Equalizer", style = MaterialTheme.typography.headlineSmall) }
            item { NotConnectedBanner(connectionState) }
            item { EqStatusNotice(connectionState, gains, eqProfileUpdatedAt, eqError, onRefresh) }
            item { EqBandSlider("Upper treble", shown.upperTreble, enabled) { onGainsChanged(shown.copy(upperTreble = it)) } }
            item { EqBandSlider("Treble", shown.treble, enabled) { onGainsChanged(shown.copy(treble = it)) } }
            item { EqBandSlider("Mid", shown.mid, enabled) { onGainsChanged(shown.copy(mid = it)) } }
            item { EqBandSlider("Bass", shown.bass, enabled) { onGainsChanged(shown.copy(bass = it)) } }
            item { EqBandSlider("Low bass", shown.lowBass, enabled) { onGainsChanged(shown.copy(lowBass = it)) } }

            item { Text("Presets", style = MaterialTheme.typography.titleMedium) }
            items(EqPreset.entries) { preset ->
                AssistChip(
                    onClick = { onPresetSelected(preset) },
                    enabled = enabled,
                    label = { Text(preset.name.replace('_', ' ')) },
                )
            }
        }
    }
}

/**
 * `ai-sessions/0041`: the EQ is *read* from the Buds at Connect (`ReadSetting 4:16`, DECISIONS.md ADR-034), so the "unknown"
 * wording only appears when that read failed — with the reason, never a generic message — and a failed write is reported the
 * same way instead of being assumed to have worked.
 */
@Composable
private fun EqStatusNotice(
    connectionState: ConnectionState,
    gains: EqBandGains?,
    eqProfileUpdatedAt: Long?,
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
            "Reading the Buds' current EQ… The sliders below start from flat (0.0) until it arrives and do NOT yet show the Buds' setting.",
            style = MaterialTheme.typography.bodyMedium,
        )
    } else {
        formatUpdatedAt(eqProfileUpdatedAt)?.let {
            Text("EQ updated: $it", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * [onValueChange] fires once per completed drag ([Slider]'s own
 * `onValueChangeFinished`), not per drag-frame — each call sends a real
 * frame over the RFCOMM `MAESTRO` channel (`BudsRepositoryImpl.setEqGains`),
 * so wiring it to the continuous `onValueChange` callback instead would have
 * spammed a wire write per pixel of drag movement. [localValue] tracks the
 * drag smoothly in the meantime and re-syncs from [value] whenever it
 * changes for a reason other than this slider's own drag (a preset tap, or a
 * real Notify frame arriving) — `remember(value)`'s own re-keying handles
 * that, since [value] never itself changes mid-drag (`ai-sessions/0038`).
 */
@Composable
private fun EqBandSlider(label: String, value: Float, enabled: Boolean, onValueChange: (Float) -> Unit) {
    var localValue by remember(value) { mutableFloatStateOf(value) }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text("%.1f".format(localValue))
        }
        Slider(
            value = localValue,
            onValueChange = { localValue = it },
            onValueChangeFinished = { onValueChange(localValue) },
            enabled = enabled,
            valueRange = EqBandGains.RANGE.start..EqBandGains.RANGE.endInclusive,
        )
    }
}
