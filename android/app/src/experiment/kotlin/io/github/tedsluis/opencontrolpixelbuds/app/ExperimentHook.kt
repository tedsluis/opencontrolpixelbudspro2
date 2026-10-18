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
package io.github.tedsluis.opencontrolpixelbuds.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import io.github.tedsluis.opencontrolpixelbuds.data.BudsRepositoryImpl
import io.github.tedsluis.opencontrolpixelbuds.data.experiment.ExperimentRunner
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The experiment card (`ai-sessions/0096` round 1's A; `ai-sessions/0098` round 2's F, the Dosimeter — each request approved by the maintainer in chat
 * 2026-10-17, Checkpoint 1) — **only in the `experiment` build type**.
 * Shown while Debug mode is on and the session is ready; every button asks first (the exact bytes), then sends one request. Nothing runs by itself.
 */
object ExperimentHook {
    private class Action(val title: String, val bytes: String, val run: suspend (ExperimentRunner) -> Unit)

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun Content(repository: BudsRepository, scope: CoroutineScope, debugModeEnabled: Boolean) {
        val impl = repository as? BudsRepositoryImpl ?: return
        val runner = remember(impl) { ExperimentRunner(impl) }
        val state by repository.connectionState.collectAsState(initial = ConnectionState.Disconnected)
        val last by runner.lastResult.collectAsState()
        var pending by remember { mutableStateOf<Action?>(null) }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Experiments (round 2, not a release)", style = MaterialTheme.typography.titleMedium)
            if (!debugModeEnabled || state !is ConnectionState.Ready) {
                Text("Turn Debug mode on and connect to use them.", style = MaterialTheme.typography.bodySmall)
                return@Column
            }
            Text("A — noise control via setting 13 (WriteSetting 4:{13:n})", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(1 to "Off", 2 to "Noise canc.", 3 to "Transparency", 4 to "Adaptive").forEach { (n, name) ->
                    OutlinedButton(onClick = { pending = Action("A: write 13 = $n ($name)", "WriteSetting payload 22 02 68 0$n", { it.writeAncState(n) }) }) { Text(name) }
                }
            }
            Text("F — Dosimeter (raw values in the log; no unit, not a health reading)", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(onClick = {
                    pending = Action("F1: SetWallclock", "SetWallclock payload 08 <the phone's clock now, ms>", { it.setWallclock() })
                }) { Text("F1 Clock") }
                listOf(19, 21).forEach { ch ->
                    OutlinedButton(onClick = {
                        pending = Action("F2: SubscribeToLiveDb (channel $ch)", "Dosimeter 0x73d5d805 method 0x4d93b6e2, empty request", { it.subscribeToLiveDb(ch) })
                    }) { Text("F2 Live $ch") }
                }
                listOf(19, 21).forEach { ch ->
                    OutlinedButton(onClick = {
                        pending = Action("F3: FetchDailySummaries (channel $ch)", "Dosimeter 0x73d5d805 method 0x73b772ce, empty request", { it.fetchDailySummaries(ch) })
                    }) { Text("F3 Daily $ch") }
                }
            }
            Text("F4 — setting 21 (Volume level notifications)", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(19, 21).forEach { ch ->
                    listOf(true to "on", false to "off").forEach { (on, name) ->
                        OutlinedButton(onClick = {
                            pending = Action("F4: write 21 = $name (channel $ch)", "WriteSetting payload 22 03 a8 01 0${if (on) 1 else 0}", { it.writeField21(ch, on) })
                        }) { Text("$ch $name") }
                    }
                }
            }
            last?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }

        pending?.let { action ->
            AlertDialog(
                onDismissRequest = { pending = null },
                title = { Text(action.title) },
                text = { Text("Sends exactly: ${action.bytes}") },
                confirmButton = {
                    TextButton(onClick = {
                        pending = null
                        scope.launch { action.run(runner) }
                    }) { Text("Send") }
                },
                dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancel") } },
            )
        }
    }
}
