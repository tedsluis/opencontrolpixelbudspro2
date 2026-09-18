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

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.tedsluis.opencontrolpixelbuds.data.settings.DebugSettingsStore
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothStateObserver
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsCompanionPairing
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlActions
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlNavHost
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlUiState
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * `:app`'s Hilt composition root (DECISIONS.md ADR-028). Wires the full
 * dependency chain — [budsRepository] (real interface, `RepositoryModule.kt`),
 * [companionPairing]/[bluetoothStateObserver] (`:hardware`), and
 * [debugSettingsStore] (`:data`) — into `:ui`'s [OpenControlNavHost].
 *
 * State hoisting lives here rather than in a dedicated ViewModel class this
 * session (ARCHITECTURE.md §2's diagram labels ViewModels as living in
 * `:ui`, but `:ui` deliberately has no Hilt dependency — see §2.4's own doc
 * comment on `OpenControlActions`/`OpenControlUiState` — so a Hilt-injected
 * ViewModel would need to live in `:app` regardless; collecting flows
 * directly in this Activity, matching `ai-sessions/0013`'s own established
 * pattern, was judged sufficient for this session's scope rather than adding
 * a `BudsViewModel` class that would do little beyond what
 * `collectAsStateWithLifecycle()` already does here).
 *
 * **Honest scope**: [budsRepository] resolves to `BudsRepositoryImpl` wired
 * against `FakeBudsTransport` (`TransportModule.kt`) until `RfcommBudsTransport`
 * is verified against real hardware — every screen below is real, wired code,
 * but not yet exercised against a real Pixel Buds Pro 2 (ARCHITECTURE.md §5a).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var budsRepository: BudsRepository

    @Inject
    lateinit var debugSettingsStore: DebugSettingsStore

    private val companionPairing by lazy { BudsCompanionPairing(this) }
    private val bluetoothStateObserver by lazy { BluetoothStateObserver(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()

            val connectionState by budsRepository.connectionState
                .collectAsStateWithLifecycle(initialValue = ConnectionState.Disconnected)
            val ancMode by budsRepository.ancMode.collectAsStateWithLifecycle(initialValue = null)
            val eqProfile by budsRepository.eqProfile.collectAsStateWithLifecycle(initialValue = null)
            val batteryStatus by budsRepository.batteryStatus.collectAsStateWithLifecycle(
                initialValue = io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus(),
            )
            val bluetoothAdapterState by remember { bluetoothStateObserver.observe() }
                .collectAsStateWithLifecycle(
                    initialValue = io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothAdapterState.OFF,
                )
            val debugModeEnabled by debugSettingsStore.debugModeEnabled.collectAsStateWithLifecycle(initialValue = false)

            var hasBondedDevice by remember { mutableStateOf(companionPairing.bondedDevice() != null) }
            val unidentifiedFrames = remember { mutableStateOf(listOf<UnidentifiedFrame>()) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                budsRepository.unidentifiedFrames.collect { frame ->
                    unidentifiedFrames.value = (unidentifiedFrames.value + frame).takeLast(200)
                }
            }

            val state = OpenControlUiState(
                connectionState = connectionState,
                bluetoothEnabled = bluetoothAdapterState == io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothAdapterState.ON,
                hasBondedDevice = hasBondedDevice,
                ancMode = ancMode,
                eqProfile = eqProfile,
                batteryStatus = batteryStatus,
                unidentifiedFrames = unidentifiedFrames.value,
                debugModeEnabled = debugModeEnabled,
            )

            val actions = OpenControlActions(
                onRequestEnableBluetooth = {
                    startActivity(android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                },
                onPair = {
                    companionPairing.requestAssociation(
                        onPending = { /* :ui has no ActivityResultLauncher hook yet this session —
                            TODO(verify): launching the returned IntentSender against a real
                            picker is not exercised in this environment. */ },
                        onCreated = { hasBondedDevice = companionPairing.bondedDevice() != null },
                        onFailure = { },
                    )
                },
                onConnect = { /* RfcommBudsTransport.connect() needs a resolved BluetoothDevice +
                    channel map — wiring a real connect action through to the UI is the next
                    increment once pairing is exercised against real hardware. */ },
                onDisconnect = { },
                onAncModeSelected = { mode -> scope.launch { budsRepository.setAncMode(mode) } },
                onEqGainsChanged = { gains -> scope.launch { budsRepository.setEqGains(gains) } },
                onEqPresetSelected = { preset -> scope.launch { budsRepository.applyEqPreset(preset) } },
                onRing = { target -> scope.launch { budsRepository.ringBud(target) } },
                onStopRinging = { scope.launch { budsRepository.stopRinging() } },
                onDebugModeChanged = { enabled -> scope.launch { debugSettingsStore.setDebugModeEnabled(enabled) } },
            )

            MaterialTheme {
                OpenControlNavHost(state = state, actions = actions)
            }
        }
    }
}
