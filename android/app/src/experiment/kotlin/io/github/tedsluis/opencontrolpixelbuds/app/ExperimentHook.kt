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
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Round 1's experiment card (`ai-sessions/0096`, approved per request by the maintainer in chat 2026-10-16) — **only in the `experiment` build type**.
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
        var holdJob by remember { mutableStateOf<Job?>(null) }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Experiments (round 1, not a release)", style = MaterialTheme.typography.titleMedium)
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
            Text("E — read settings 8, 9, 10", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(8, 9, 10).forEach { f ->
                    OutlinedButton(onClick = { pending = Action("E: ReadSetting 4:$f", "ReadSetting payload 20 %02x".format(f), { it.readSetting(f) }) }) { Text("Read $f") }
                }
            }
            Text("D — on/off-head events", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(onClick = { pending = Action("D: SubscribeToOobeActions", "method 0x57f3becb, empty request", { it.subscribeToOobeActions() }) }) {
                Text("Subscribe")
            }
            Text("B — ring and hold the channel until stopped on the bud (max 30 s)", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(false, true).forEach { ack ->
                    listOf(RingTarget.LEFT to "Left", RingTarget.RIGHT to "Right").forEach { (target, name) ->
                        val label = if (ack) "B2 $name + ACK" else "B1 $name"
                        OutlinedButton(onClick = {
                            pending = Action(
                                "$label: ring and hold",
                                "04 01 00 01 %02x".format(target.wireValue) + if (ack) "; each Buds status answered with ff 01 00 02 04 01" else "; no ACK",
                                { r -> r.ringAndHold(target, ack) },
                            )
                        }) { Text(label) }
                    }
                }
                OutlinedButton(onClick = {
                    holdJob?.cancel()
                    holdJob = scope.launch { repository.stopRinging() }
                }) { Text("Stop") }
            }
            Text("C — ring both buds (take them out of your ears first)", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(onClick = { pending = Action("C: ring both — buds out of the ears!", "04 01 00 01 03", { it.ringBoth() }) }) { Text("Ring both") }
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
                        holdJob = scope.launch { action.run(runner) }
                    }) { Text("Send") }
                },
                dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancel") } },
            )
        }
    }
}
