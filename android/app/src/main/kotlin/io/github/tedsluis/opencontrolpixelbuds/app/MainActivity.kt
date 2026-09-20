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

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.tedsluis.opencontrolpixelbuds.data.settings.DebugSettingsStore
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import io.github.tedsluis.opencontrolpixelbuds.domain.deriveDeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.permissionStatus
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothAdapterState
import io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothStateObserver
import io.github.tedsluis.opencontrolpixelbuds.hardware.BondedLookup
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsCompanionPairing
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsForegroundService
import io.github.tedsluis.opencontrolpixelbuds.hardware.OsConnectionObserver
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingFailure
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingState
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlActions
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlNavHost
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * `:app`'s Hilt composition root (DECISIONS.md ADR-028). Wires the full dependency chain — [budsRepository] (real
 * interface, `RepositoryModule.kt`), [companionPairing]/[bluetoothStateObserver]/[osConnectionObserver] (`:hardware`), and
 * [debugSettingsStore] (`:data`) — into `:ui`'s [OpenControlNavHost].
 *
 * State hoisting lives here rather than in a dedicated ViewModel class (ARCHITECTURE.md §2.4): collecting flows directly in
 * this Activity, matching `ai-sessions/0013`'s pattern, was judged sufficient.
 *
 * **`ai-sessions/0041`:**
 * - The two runtime permissions (`BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`) are checked on start and on every resume and
 *   requested with `RequestMultiplePermissions` (the app used to *declare* them but never ask, so after clearing app data every
 *   Bluetooth call was silently denied). Their state, including "blocked", is its own [PermissionState].
 * - The Connection screen's status mirrors Android's own Bluetooth state ([OsConnectionObserver], collected only while the UI is
 *   visible); this Activity never opens a session by itself — Connect stays a user tap (ARCHITECTURE.md §6).
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
        OsConnectionObserver(this) { (companionPairing.lookupBonded() as? BondedLookup.Found)?.address }
    }

    /** Whether a system permission prompt was already shown since this process started (see [permissionStatus]). */
    private var permissionsRequestedThisRun = false
    private val permissionStateFlow = MutableStateFlow(
        PermissionState(PermissionStatus.NOT_REQUESTED, PermissionStatus.NOT_REQUESTED),
    )

    // Must be registered unconditionally before the Activity reaches STARTED (ComponentActivity's own contract).
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        BleLogger.logConnectionEvent(
            "Permissions: prompt result " + result.entries.joinToString { "${it.key.substringAfterLast('.')}=${it.value}" },
        )
        refreshPermissions("prompt result")
    }

    // Launches the system CDM picker; the actual "device selected" outcome is handled by
    // CompanionDeviceManager.Callback.onAssociationCreated (BudsCompanionPairing) — this launcher only presents the UI.
    private val pairingLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { /* no-op: onCreated/onFailure (BudsCompanionPairing) handle the actual outcome */ }

    private fun computePermissionState(): PermissionState {
        fun status(permission: String) = permissionStatus(
            granted = ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED,
            requestedThisRun = permissionsRequestedThisRun,
            shouldShowRationale = shouldShowRequestPermissionRationale(permission),
        )
        return PermissionState(
            bluetoothConnect = status(Manifest.permission.BLUETOOTH_CONNECT),
            postNotifications = status(Manifest.permission.POST_NOTIFICATIONS),
        )
    }

    /** Recomputes the permission state and logs a change (always-on, `AGENTS.md` §9 — no identifiers involved). */
    private fun refreshPermissions(reason: String) {
        val now = computePermissionState()
        val before = permissionStateFlow.value
        if (now != before) {
            BleLogger.logConnectionEvent(
                "Permissions ($reason): Bluetooth ${before.bluetoothConnect} -> ${now.bluetoothConnect}, " +
                    "notifications ${before.postNotifications} -> ${now.postNotifications}",
            )
        }
        permissionStateFlow.value = now
    }

    private fun requestPermissions() {
        permissionsRequestedThisRun = true
        BleLogger.logConnectionEvent("Permissions: showing the system prompt")
        permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.POST_NOTIFICATIONS))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshPermissions("start")
        setContent {
            val scope = rememberCoroutineScope()

            val connectionState by budsRepository.connectionState
                .collectAsStateWithLifecycle(initialValue = ConnectionState.Disconnected)
            val ancMode by budsRepository.ancMode.collectAsStateWithLifecycle(initialValue = null)
            val eqProfile by budsRepository.eqProfile.collectAsStateWithLifecycle(initialValue = null)
            val eqError by budsRepository.eqError.collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val batteryStatus by budsRepository.batteryStatus.collectAsStateWithLifecycle(
                initialValue = BatteryStatus(),
            )
            val lastConnectionError by budsRepository.lastConnectionError
                .collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val messageStreamError by budsRepository.messageStreamError
                .collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val permissionState by permissionStateFlow.collectAsStateWithLifecycle()
            // Re-created when the Bluetooth grant changes: the profile proxies can only be bound with the permission.
            val androidLink by remember(permissionState.bluetoothConnect.isGranted) { osConnectionObserver.observe() }
                .collectAsStateWithLifecycle(initialValue = AndroidLink.UNKNOWN)
            val bluetoothAdapterState by remember { bluetoothStateObserver.observe() }
                .collectAsStateWithLifecycle(initialValue = BluetoothAdapterState.OFF)
            val debugModeEnabled by debugSettingsStore.debugModeEnabled.collectAsStateWithLifecycle(initialValue = false)

            var bondedLookup by remember { mutableStateOf(companionPairing.lookupBonded()) }
            var pairingState by remember { mutableStateOf<PairingState?>(null) }

            // Pairing done outside this app entirely (Android's own Bluetooth settings) never fires any callback this
            // Activity owns — the only way to notice it is to re-check on every resume (ai-sessions/0036). The same resume
            // also re-checks the permissions and asks for the ones never asked for (ai-sessions/0041).
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        refreshPermissions("resume")
                        if (permissionStateFlow.value.bluetoothConnect == PermissionStatus.NOT_REQUESTED) requestPermissions()
                        bondedLookup = companionPairing.lookupBonded()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            // A grant given while the app is open (prompt or system settings) changes what "bonded" can be seen.
            LaunchedEffect(permissionState.bluetoothConnect) { bondedLookup = companionPairing.lookupBonded() }

            val unidentifiedFrames = remember { mutableStateOf(listOf<UnidentifiedFrame>()) }
            LaunchedEffect(Unit) {
                budsRepository.unidentifiedFrames.collect { frame ->
                    unidentifiedFrames.value = (unidentifiedFrames.value + frame).takeLast(200)
                }
            }

            // ARCHITECTURE.md §6.0a: :app's composition root is the documented owner of BudsForegroundService's start/stop
            // lifecycle, bound to the same ConnectionState this screen already observes — started for any
            // non-Disconnected/non-Failed state (all reachable only via a user-initiated Connect tap, no background
            // reconnect exists), stopped once back at Disconnected or Failed.
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

            val hasBondedDevice = bondedLookup is BondedLookup.Found
            val bluetoothEnabled = bluetoothAdapterState == BluetoothAdapterState.ON
            val state = OpenControlUiState(
                connectionState = connectionState,
                bluetoothEnabled = bluetoothEnabled,
                hasBondedDevice = hasBondedDevice,
                deviceStatus = deriveDeviceStatus(
                    bluetoothEnabled = bluetoothEnabled,
                    bluetoothConnect = permissionState.bluetoothConnect,
                    hasBondedDevice = hasBondedDevice,
                    androidLink = androidLink,
                    session = connectionState,
                ),
                permissionState = permissionState,
                pairingStatusText = pairingState?.toUserMessage(),
                lastConnectionError = lastConnectionError,
                androidLink = androidLink,
                messageStreamError = messageStreamError,
                ancMode = ancMode,
                eqProfile = eqProfile,
                eqError = eqError,
                batteryStatus = batteryStatus,
                unidentifiedFrames = unidentifiedFrames.value,
                debugModeEnabled = debugModeEnabled,
            )

            val actions = OpenControlActions(
                onRequestEnableBluetooth = {
                    // BLUETOOTH_CONNECT is runtime-revocable (AGENTS.md §2) — a denied/missing grant here just means the native
                    // re-enable prompt can't be shown yet, not a crash condition.
                    try {
                        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    } catch (e: SecurityException) {
                        BleLogger.logConnectionEvent("Enable-Bluetooth prompt refused: ${BleLogger.describe(e)}")
                    }
                },
                onPair = {
                    pairingState = PairingState.Requesting
                    companionPairing.requestAssociation(
                        onPending = { intentSender ->
                            pairingLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        },
                        onCreated = { associationInfo ->
                            // CDM's own success callback only grants this app permission to see the device — it does not pair
                            // it (BudsCompanionPairing.observeBonding's doc comment). Classic bonding is a separate step.
                            val device = companionPairing.deviceForAssociation(associationInfo)
                            if (device == null) {
                                pairingState = PairingState.Failed(PairingFailure.DeviceNotResolved)
                            } else {
                                companionPairing.cleanUpDuplicateAssociations(associationInfo)
                                scope.launch {
                                    companionPairing.observeBonding(device).collect { newState ->
                                        pairingState = newState
                                        if (newState is PairingState.Bonded) bondedLookup = companionPairing.lookupBonded()
                                    }
                                }
                            }
                        },
                        onFailure = { failure -> pairingState = PairingState.Failed(failure) },
                    )
                },
                onRequestPermissions = { requestPermissions() },
                onOpenAppSettings = {
                    BleLogger.logConnectionEvent("Permissions: opening the app's system settings")
                    startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)),
                    )
                },
                onConnect = { scope.launch { budsRepository.connect() } },
                onDisconnect = { scope.launch { budsRepository.disconnect() } },
                onAncModeSelected = { mode -> scope.launch { budsRepository.setAncMode(mode) } },
                onRefreshAncMode = { scope.launch { budsRepository.refreshAncMode() } },
                onEqGainsChanged = { gains -> scope.launch { budsRepository.setEqGains(gains) } },
                onEqPresetSelected = { preset -> scope.launch { budsRepository.applyEqPreset(preset) } },
                onRefreshEq = { scope.launch { budsRepository.refreshEq() } },
                onRing = { target -> scope.launch { budsRepository.ringBud(target) } },
                onStopRinging = { scope.launch { budsRepository.stopRinging() } },
                onDebugModeChanged = { enabled -> scope.launch { debugSettingsStore.setDebugModeEnabled(enabled) } },
                onExportLog = {
                    // Local-only hand-off to the system share sheet (AGENTS.md §9) — the user picks the destination, this app
                    // never transmits it anywhere itself.
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

/** Plain, user-facing copy per [PairingState] case — each failure has its own message and a one-line hint (AGENTS.md §8). */
internal fun PairingState.toUserMessage(): String = when (this) {
    PairingState.Requesting -> "Waiting for you to pick your Pixel Buds in the system dialog…"
    PairingState.Bonding -> "Pairing… keep the Buds close to the phone."
    is PairingState.Bonded -> "Paired with ${deviceName ?: "the Buds"}."
    is PairingState.Failed -> failure.toUserMessage()
}

internal fun PairingFailure.toUserMessage(): String = when (this) {
    PairingFailure.CompanionUnavailable ->
        "Pairing from the app isn't available on this device (no companion-device support). Pair in Android's Bluetooth settings instead."
    is PairingFailure.AssociationFailed -> "Pairing failed: the system dialog reported \"$detail\". Try again."
    PairingFailure.DeviceNotResolved ->
        "Pairing failed: the selected device couldn't be looked up. Try again; if it keeps failing, pair in Android's Bluetooth settings."
    PairingFailure.PermissionMissing ->
        "Pairing failed: the Bluetooth (Nearby devices) permission is missing. Allow it and try again."
    PairingFailure.CouldNotStartBond -> "Pairing failed: Android couldn't start pairing. Try again."
    PairingFailure.NotInPairingMode ->
        "The Buds didn't answer. Open the case and hold the pair button on the case for more than 3 seconds until the light " +
            "pulses, then tap Pair a device again."
    PairingFailure.BondRejected ->
        "Pairing was cancelled or rejected. Tap Pair a device to try again (accept the pairing request on the phone)."
    PairingFailure.BondTimeout ->
        "Pairing took too long. Put the Buds back in pairing mode (open the case, hold the pair button for more than 3 seconds) and try again."
}
