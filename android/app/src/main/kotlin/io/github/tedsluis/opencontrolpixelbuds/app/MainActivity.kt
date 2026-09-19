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
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.tedsluis.opencontrolpixelbuds.data.settings.DebugSettingsStore
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothAdapterState
import io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothStateObserver
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsCompanionPairing
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsForegroundService
import io.github.tedsluis.opencontrolpixelbuds.hardware.OsConnectionObserver
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingState
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
 * **Honest scope**: [budsRepository] now resolves to `BudsRepositoryImpl`
 * wired against the real, `BluetoothSocket`-backed `RfcommBudsTransport`
 * (`TransportModule.kt`, `ai-sessions/0037`) — [OpenControlActions.onConnect]/
 * `onDisconnect` call `budsRepository.connect()`/`disconnect()` for real.
 * This is this project's **first** real RFCOMM socket-open attempt against
 * actual Pixel Buds Pro 2 hardware — everything downstream of "sockets
 * opened" (per-DLCI framing, ANC/EQ/Find My Buds commands) remains as
 * untested against real hardware as it was before this session
 * (ARCHITECTURE.md §5a). Pairing itself (CDM picker → classic bonding,
 * `ai-sessions/0036`) is fully wired, including the `createBond()` step
 * CDM's own association callback does **not** perform automatically.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var budsRepository: BudsRepository

    @Inject
    lateinit var debugSettingsStore: DebugSettingsStore

    private val companionPairing by lazy { BudsCompanionPairing(this) }
    private val bluetoothStateObserver by lazy { BluetoothStateObserver(this) }
    private val osConnectionObserver by lazy {
        OsConnectionObserver(this) { companionPairing.bondedDevice()?.address }
    }

    // Must be registered unconditionally before the Activity reaches STARTED (ComponentActivity's
    // own contract) — a class property, not something created inside the Composable content
    // lambda below. Launches the system CDM picker; the actual "device selected" outcome is
    // handled by CompanionDeviceManager.Callback.onAssociationCreated (BudsCompanionPairing),
    // already wired below — this launcher only needs to present the UI (ai-sessions/0035, closing
    // the gap ai-sessions/0033 left open).
    private val pairingLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { /* no-op: onCreated/onFailure (BudsCompanionPairing) handle the actual outcome */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()

            val connectionState by budsRepository.connectionState
                .collectAsStateWithLifecycle(initialValue = ConnectionState.Disconnected)
            val ancMode by budsRepository.ancMode.collectAsStateWithLifecycle(initialValue = null)
            val eqProfile by budsRepository.eqProfile.collectAsStateWithLifecycle(initialValue = null)
            val batteryStatus by budsRepository.batteryStatus.collectAsStateWithLifecycle(
                initialValue = BatteryStatus(),
            )
            val lastConnectionError by budsRepository.lastConnectionError
                .collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val messageStreamError by budsRepository.messageStreamError
                .collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val osConnected by remember { osConnectionObserver.observe() }
                .collectAsStateWithLifecycle(initialValue = false)
            val bluetoothAdapterState by remember { bluetoothStateObserver.observe() }
                .collectAsStateWithLifecycle(initialValue = BluetoothAdapterState.OFF)
            val debugModeEnabled by debugSettingsStore.debugModeEnabled.collectAsStateWithLifecycle(initialValue = false)

            var hasBondedDevice by remember { mutableStateOf(companionPairing.bondedDevice() != null) }
            var pairingState by remember { mutableStateOf<PairingState?>(null) }

            // Pairing done outside this app entirely (Android's own Bluetooth settings) never
            // fires any callback this Activity owns — the only way to notice it is to re-check
            // on every resume (ai-sessions/0036: the maintainer's own report that a
            // Settings-app pairing was invisible to this app).
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        hasBondedDevice = companionPairing.bondedDevice() != null
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val unidentifiedFrames = remember { mutableStateOf(listOf<UnidentifiedFrame>()) }
            LaunchedEffect(Unit) {
                budsRepository.unidentifiedFrames.collect { frame ->
                    unidentifiedFrames.value = (unidentifiedFrames.value + frame).takeLast(200)
                }
            }

            // ARCHITECTURE.md §6.0a: :app's composition root is the documented owner of
            // BudsForegroundService's start/stop lifecycle, bound to the same ConnectionState
            // this screen already observes — started for any non-Disconnected/non-Failed state
            // (all reachable only via a user-initiated Connect tap right now, no background
            // reconnect exists), stopped once back at Disconnected or Failed. The service itself
            // holds no Bluetooth logic (BudsForegroundService's own doc comment) — this is purely
            // the "one source of truth" binding ARCHITECTURE.md §6.0a specifies.
            var foregroundServiceActive by remember { mutableStateOf(false) }
            LaunchedEffect(connectionState, ancMode) {
                val isActive = connectionState != ConnectionState.Disconnected &&
                    connectionState !is ConnectionState.Failed
                if (isActive) {
                    val statusText = when (connectionState) {
                        ConnectionState.Connecting -> "Connecting…"
                        ConnectionState.Discovering -> "Discovering services…"
                        ConnectionState.Ready -> ancMode?.let { "Connected — ANC: ${it.name}" } ?: "Connected"
                        else -> "Connecting…"
                    }
                    startForegroundService(
                        Intent(this@MainActivity, BudsForegroundService::class.java)
                            .putExtra(BudsForegroundService.EXTRA_STATUS_TEXT, statusText),
                    )
                    foregroundServiceActive = true
                } else if (foregroundServiceActive) {
                    stopService(Intent(this@MainActivity, BudsForegroundService::class.java))
                    foregroundServiceActive = false
                }
            }

            val state = OpenControlUiState(
                connectionState = connectionState,
                bluetoothEnabled = bluetoothAdapterState == BluetoothAdapterState.ON,
                hasBondedDevice = hasBondedDevice,
                pairingStatusText = pairingState?.toUserMessage(),
                lastConnectionError = lastConnectionError,
                osConnected = osConnected,
                messageStreamError = messageStreamError,
                ancMode = ancMode,
                eqProfile = eqProfile,
                batteryStatus = batteryStatus,
                unidentifiedFrames = unidentifiedFrames.value,
                debugModeEnabled = debugModeEnabled,
            )

            val actions = OpenControlActions(
                onRequestEnableBluetooth = {
                    // BLUETOOTH_CONNECT is runtime-revocable (AGENTS.md §2) — a denied/missing
                    // grant here just means the native re-enable prompt can't be shown yet, not a
                    // crash condition.
                    try {
                        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    } catch (e: SecurityException) {
                        // Nothing to do — the Connection screen's own Bluetooth-disabled state
                        // stays visible either way, so the user isn't left with silent nothing.
                    }
                },
                onPair = {
                    pairingState = null
                    companionPairing.requestAssociation(
                        onPending = { intentSender ->
                            pairingLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        },
                        onCreated = { associationInfo ->
                            // CDM's own success callback only grants this app permission to see the
                            // device — it does not pair it (BudsCompanionPairing.observeBonding's own
                            // doc comment). Classic bonding is a separate, explicit step.
                            val device = companionPairing.deviceForAssociation(associationInfo)
                            if (device == null) {
                                pairingState = PairingState.Failed("Could not resolve the selected device.")
                            } else {
                                scope.launch {
                                    companionPairing.observeBonding(device).collect { newState ->
                                        pairingState = newState
                                        if (newState is PairingState.Bonded) {
                                            hasBondedDevice = companionPairing.bondedDevice() != null
                                        }
                                    }
                                }
                            }
                        },
                        onFailure = { reason -> pairingState = PairingState.Failed(reason.toString()) },
                    )
                },
                onConnect = { scope.launch { budsRepository.connect() } },
                onDisconnect = { scope.launch { budsRepository.disconnect() } },
                onAncModeSelected = { mode -> scope.launch { budsRepository.setAncMode(mode) } },
                onRefreshAncMode = { scope.launch { budsRepository.refreshAncMode() } },
                onEqGainsChanged = { gains -> scope.launch { budsRepository.setEqGains(gains) } },
                onEqPresetSelected = { preset -> scope.launch { budsRepository.applyEqPreset(preset) } },
                onRing = { target -> scope.launch { budsRepository.ringBud(target) } },
                onStopRinging = { scope.launch { budsRepository.stopRinging() } },
                onDebugModeChanged = { enabled -> scope.launch { debugSettingsStore.setDebugModeEnabled(enabled) } },
                onExportLog = {
                    // Local-only hand-off to the system share sheet (AGENTS.md §9) — the user
                    // picks the destination, this app never transmits it anywhere itself.
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, BleLogger.exportLog())
                        putExtra(Intent.EXTRA_TITLE, "OpenControl debug log")
                    }
                    startActivity(Intent.createChooser(shareIntent, "Export debug log"))
                },
            )

            MaterialTheme {
                OpenControlNavHost(state = state, actions = actions)
            }
        }
    }
}

/** Plain, user-facing copy per [PairingState] case — the Connection screen renders this directly,
 * closing the "no message why it didn't work" gap (`ai-sessions/0036`). */
private fun PairingState.toUserMessage(): String = when (this) {
    is PairingState.Bonding -> "Pairing…"
    is PairingState.Bonded -> "Paired with ${deviceName ?: "the Buds"}."
    is PairingState.Failed -> "Pairing failed: $reason"
}
