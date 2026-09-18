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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
 * [gains] is nullable per ARCHITECTURE.md §3.1's table: EQ has no confirmed
 * read-on-reconnect opcode, so a fresh connection's value is genuinely
 * unknown, not merely "not yet loaded" — the UI must say so, not show a
 * default `0.0` quintet that looks like a real reading.
 */
@Composable
fun EqScreen(
    gains: EqBandGains?,
    onGainsChanged: (EqBandGains) -> Unit,
    onPresetSelected: (EqPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        if (gains == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "EQ state unknown — change a setting or wait for the Buds to report one.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Surface
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Equalizer", style = MaterialTheme.typography.headlineSmall)

            EqBandSlider("Upper treble", gains.upperTreble) { onGainsChanged(gains.copy(upperTreble = it)) }
            EqBandSlider("Treble", gains.treble) { onGainsChanged(gains.copy(treble = it)) }
            EqBandSlider("Mid", gains.mid) { onGainsChanged(gains.copy(mid = it)) }
            EqBandSlider("Bass", gains.bass) { onGainsChanged(gains.copy(bass = it)) }
            EqBandSlider("Low bass", gains.lowBass) { onGainsChanged(gains.copy(lowBass = it)) }

            Text("Presets", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(EqPreset.entries) { preset ->
                    AssistChip(
                        onClick = { onPresetSelected(preset) },
                        label = { Text(preset.name.replace('_', ' ')) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EqBandSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text("%.1f".format(value))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = EqBandGains.RANGE.start..EqBandGains.RANGE.endInclusive,
        )
    }
}
