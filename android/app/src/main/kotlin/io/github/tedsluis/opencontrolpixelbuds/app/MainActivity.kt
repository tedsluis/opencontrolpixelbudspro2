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

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsTransport
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine
import io.github.tedsluis.opencontrolpixelbuds.ui.AncScreen
import javax.inject.Inject

/**
 * `:app`'s Hilt composition root (DECISIONS.md ADR-028). [connectionStateMachine]
 * and [budsTransport] are real, already-tested `:hardware` components,
 * resolved through Hilt's dependency graph rather than constructed by hand —
 * this is what "the composition root is wired" means concretely. What is
 * still **not** wired is a real hardware connection: [budsTransport] resolves
 * to `FakeBudsTransport` (see `di/TransportModule.kt`) until
 * `RfcommBudsTransport` is verified against real hardware, and no
 * `BudsRepository`/use-case layer exists yet to turn [ancMode] taps into an
 * actual outbound frame — that remains out of this session's own scope
 * (`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phase 7).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var connectionStateMachine: ConnectionStateMachine

    @Inject
    lateinit var budsTransport: BudsTransport

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var ancMode by mutableStateOf(AncMode.OFF)
            val connectionState by connectionStateMachine.state.collectAsState()

            // Proves the injected ConnectionStateMachine and BudsTransport are
            // real, working instances resolved from Hilt's graph, not
            // placeholders — walks Disconnected -> Connecting -> Discovering
            // -> Ready once, and logs the (fake, for now) transport's state.
            // Connection-state transitions are always safe to log (AGENTS.md §9).
            LaunchedEffect(Unit) {
                Log.d(TAG, "BudsTransport injected, connected=${budsTransport.connected}")
                connectionStateMachine.onConnectRequested()
                connectionStateMachine.onLinkEstablished()
                connectionStateMachine.onReady()
            }

            MaterialTheme {
                AncScreen(
                    connectionState = connectionState,
                    ancMode = ancMode,
                    onAncModeSelected = { ancMode = it },
                )
            }
        }
    }
}
