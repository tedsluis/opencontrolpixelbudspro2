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
import android.app.StatusBarManager
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.tedsluis.opencontrolpixelbuds.BuildConfig
import io.github.tedsluis.opencontrolpixelbuds.R
import io.github.tedsluis.opencontrolpixelbuds.data.settings.DarkModeSettingsStore
import io.github.tedsluis.opencontrolpixelbuds.data.settings.DebugSettingsStore
import io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DarkMode
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceInfo
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.RingNotice
import io.github.tedsluis.opencontrolpixelbuds.domain.SafeModeState
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
import io.github.tedsluis.opencontrolpixelbuds.domain.deriveDeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.permissionStatus
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothAdapterState
import io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothStateObserver
import io.github.tedsluis.opencontrolpixelbuds.hardware.BondedLookup
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsCompanionPairing
import io.github.tedsluis.opencontrolpixelbuds.hardware.OsConnectionObserver
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingFailure
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingState
import io.github.tedsluis.opencontrolpixelbuds.hardware.settled
import io.github.tedsluis.opencontrolpixelbuds.ui.AppBuildInfo
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlActions
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlNavHost
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlTheme
import io.github.tedsluis.opencontrolpixelbuds.ui.OpenControlUiState
import io.github.tedsluis.opencontrolpixelbuds.ui.PullAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * `:app`'s Hilt composition root (DECISIONS.md ADR-028). Wires the full dependency chain — [budsRepository] (real
 * interface, `RepositoryModule.kt`), [companionPairing]/[bluetoothStateObserver]/[osConnectionObserver] (`:hardware`), and
 * [debugSettingsStore] (`:data`) — into `:ui`'s [OpenControlNavHost].
 *
 * State hoisting lives here rather than in a dedicated ViewModel class (ARCHITECTURE.md §2, UI Layer; `ai-sessions/0033` Phase 6): collecting flows directly in
 * this Activity, matching `ai-sessions/0013`'s pattern, was judged sufficient.
 *
 * **`ai-sessions/0041`:**
 * - The two runtime permissions (`BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`) are checked on start and on every resume and
 *   requested with `RequestMultiplePermissions` (the app used to *declare* them but never ask, so after clearing app data every
 *   Bluetooth call was silently denied). Their state, including "blocked", is its own [PermissionState].
 * - The Connection screen's status mirrors Android's own Bluetooth state ([OsConnectionObserver], collected only while the UI is
 *   visible). **Since `ai-sessions/0048` (DECISIONS.md ADR-044):** this Activity reports its visibility (resume/stop) and every reading of
 *   Android's link to the repository, which re-opens the session by itself while the app is visible and Android reports the Buds connected —
 *   one attempt per event, never in the background; a Disconnect tap switches it off until the next Connect tap.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var budsRepository: BudsRepository

    @Inject
    lateinit var debugSettingsStore: DebugSettingsStore

    /** The Settings tab's dark mode (`ai-sessions/0062` F-6), applied to the theme at once. */
    @Inject
    lateinit var darkModeSettingsStore: DarkModeSettingsStore

    /**
     * The application-lifetime scope (`RepositoryModule`). Every user action runs here, not in the composition's scope: a rotation or
     * any other configuration change must not cancel a Connect or a claim half-way (0044 finding APP-4 — a cancelled Connect used to
     * leave the state stuck at "Connecting" with no button, and a cancelled claim kept DLCI 0x04/0x08 from Play services).
     */
    @Inject
    lateinit var applicationScope: CoroutineScope

    /** Pairing progress, the bonding observation and the unidentified frames — outside the Activity (ADR-048 item 2, A58-APP-08). */
    @Inject
    lateinit var uiSession: AppUiSession

    // The system picker (associate) and the bonded lookup stay on this Activity-context instance; the bonding wait runs on the application-context
    // singleton in [AppUiSession] (A58-APP-08), so a rotation no longer cancels it.
    private val companionPairing by lazy { BudsCompanionPairing(this) }
    private val bluetoothStateObserver by lazy { BluetoothStateObserver(this) }
    private val osConnectionObserver by lazy {
        OsConnectionObserver(this) { (companionPairing.lookupBonded() as? BondedLookup.Found)?.address }
    }

    /**
     * Events on which Android's link state is re-read without waiting for a system broadcast (`ai-sessions/0042`): a resume, a
     * change of the bonded device, a change of this app's own session. Event-driven, no timer.
     */
    private val linkRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** The latest raw reading of Android's link (`OsConnectionObserver`); the screen shows it debounced ([settled]). */
    private val latestLink = MutableStateFlow(AndroidLink.UNKNOWN)

    /** Pairing progress — in [AppUiSession] so a rotation keeps it (and the picker's result callback below can reset it). */
    private val pairingStateFlow get() = uiSession.pairingState

    /** Whether a system permission prompt was already shown since this process started (see [permissionStatus]). */
    private var permissionsRequestedThisRun: Boolean
        get() = uiSession.permissionsRequestedThisRun // survives a rotation (A68-APP-09)
        set(value) { uiSession.permissionsRequestedThisRun = value }
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
    ) { result ->
        // onCreated/onFailure (BudsCompanionPairing) handle the actual outcome. A dismissed picker delivers no callback at all:
        // without this the "waiting for you to pick…" state and the in-flight guard would stay forever (ai-sessions/0042).
        if (result.resultCode == RESULT_CANCELED && pairingStateFlow.value == PairingState.Requesting) {
            companionPairing.cancelPendingAssociation()
            pairingStateFlow.value = null
        }
    }

    /**
     * "Export debug log" (`ARCHITECTURE.md` §12, AGENTS.md §9 — local only): the system's own "save as" dialog (Storage Access Framework,
     * `ACTION_CREATE_DOCUMENT` — no permission, no dependency) and the **whole** ring buffer written to the file the user picks. It replaced a
     * share-sheet `EXTRA_TEXT` hand-off whose receiving app cut the text at exactly 65,536 bytes, mid-line (`CAP-063-debug-export.log`). The
     * snapshot is taken at the tap, so the file holds the log as it was when the user asked for it.
     */
    private var pendingLogExport: String?
        get() = uiSession.pendingLogExport // survives a rotation while the "save as" dialog is open (A68-APP-09)
        set(value) { uiSession.pendingLogExport = value }

    private val logExportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val text = pendingLogExport
        pendingLogExport = null
        if (uri == null || text == null) return@registerForActivityResult
        applicationScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                        ?: error("no output stream")
                }
            }
            val lines = text.count { it == '\n' } + 1
            val message = result.fold(
                onSuccess = { "Debug log saved ($lines lines)." },
                onFailure = { "The debug log could not be saved: ${BleLogger.describe(it)}" },
            )
            BleLogger.logConnectionEvent(if (result.isSuccess) "Debug log exported ($lines lines)" else message)
            withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show() }
        }
    }

    private fun startLogExport() {
        pendingLogExport = BleLogger.exportLog()
        val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmmss", java.util.Locale.US).format(java.util.Date())
        logExportLauncher.launch("opencontrol-debug-$stamp.txt")
    }

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

    /**
     * The pairing entry point behind "Pair a device". Re-entry guard (`ai-sessions/0042`): one tap started two CDM requests 82 ms
     * apart and the second one's raw error was shown while the first picker was open — nothing new starts while a pairing runs.
     */
    /** Counts "Use different Buds" runs, so the composition re-reads the bonded lookup after each ([setContent]). */
    private val bondedChanged = MutableStateFlow(0)

    /**
     * "Use different Buds" (`ai-sessions/0069`, A68-APP-05; the maintainer's choice in chat 2026-10-03): close the session, then remove this app's own
     * CompanionDeviceManager associations — the Bluetooth pairing in Android stays. The Connection tab then offers *Pair a device*, which opens Android's
     * picker. Nothing is sent to the Buds beyond the normal Disconnect.
     */
    private fun useDifferentBuds() {
        applicationScope.launch {
            budsRepository.disconnect()
            companionPairing.forgetAssociations()
            pairingStateFlow.value = null
            bondedChanged.value += 1
            linkRefresh.tryEmit(Unit)
        }
    }

    private fun startPairing() {
        val running = pairingStateFlow.value
        if (running == PairingState.Requesting || running == PairingState.Bonding) {
            BleLogger.logConnectionEvent("Pairing: tap ignored — a pairing attempt is already in progress")
            return
        }
        pairingStateFlow.value = PairingState.Requesting
        companionPairing.requestAssociation(
            onPending = { intentSender ->
                pairingLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            },
            onCreated = { associationInfo ->
                // CDM's own success callback only grants this app permission to see the device — it does not pair it
                // (BudsCompanionPairing.observeBonding's doc comment). Classic bonding is a separate step.
                val device = companionPairing.deviceForAssociation(associationInfo)
                if (device == null) {
                    pairingStateFlow.value = PairingState.Failed(PairingFailure.DeviceNotResolved)
                } else {
                    companionPairing.cleanUpDuplicateAssociations(associationInfo)
                    // Application-scoped (A58-APP-08): a rotation during pairing no longer cancels the bond wait. `Bonded` re-reads the bonded lookup
                    // below (LaunchedEffect on the pairing state), not through a callback into this Activity.
                    uiSession.startBonding(device)
                }
            },
            onFailure = { failure ->
                // A rejected duplicate says nothing about the request that is still open — keep showing that one.
                if (failure != PairingFailure.AlreadyInProgress) pairingStateFlow.value = PairingState.Failed(failure)
            },
        )
    }

    /**
     * Settings → Info's links (`ai-sessions/0064` F-6, DECISIONS.md ADR-050): the URL is handed to another app — the browser fetches the page, this app makes no
     * network request and needs no permission. developer.android.com (package-visibility use cases, fetched 2026-10-01): "Because the startActivity() method
     * doesn't require package visibility to start another application's activity, you don't need to add a <queries> element" — and it recommends catching
     * `ActivityNotFoundException`, done here with a message that shows the address.
     */
    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            BleLogger.logConnectionEvent("Info link not opened: no app on this phone opens web links (${BleLogger.describe(e)})")
            Toast.makeText(this, noBrowserText(url), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshPermissions("start")
        setContent {
            val connectionState by budsRepository.connectionState
                .collectAsStateWithLifecycle(initialValue = ConnectionState.Disconnected)
            val ancMode by budsRepository.ancMode.collectAsStateWithLifecycle(initialValue = null)
            val ancModeUpdatedAt by budsRepository.ancModeUpdatedAt.collectAsStateWithLifecycle(initialValue = null as Long?)
            val sessionSince by budsRepository.sessionSince.collectAsStateWithLifecycle(initialValue = null as Long?)
            val ancAvailability by budsRepository.ancAvailability.collectAsStateWithLifecycle(initialValue = AncAvailability.UNKNOWN)
            val ancAvailabilityUpdatedAt by budsRepository.ancAvailabilityUpdatedAt.collectAsStateWithLifecycle(initialValue = null as Long?)
            val ancModeUnconfirmedAt by budsRepository.ancModeUnconfirmedAt.collectAsStateWithLifecycle(initialValue = null as Long?)
            val eqProfile by budsRepository.eqProfile.collectAsStateWithLifecycle(initialValue = null)
            val eqProfileUpdatedAt by budsRepository.eqProfileUpdatedAt.collectAsStateWithLifecycle(initialValue = null as Long?)
            val eqError by budsRepository.eqError.collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val batteryStatus by budsRepository.batteryStatus.collectAsStateWithLifecycle(
                initialValue = BatteryStatus(),
            )
            val batteryStatusUpdatedAt by budsRepository.batteryStatusUpdatedAt.collectAsStateWithLifecycle(initialValue = null as Long?)
            val caseBatteryError by budsRepository.caseBatteryError
                .collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val settings by budsRepository.settings.collectAsStateWithLifecycle(initialValue = BudsSettings())
            val settingsError by budsRepository.settingsError.collectAsStateWithLifecycle(initialValue = null as SettingsFailure?)
            val batteryRefreshError by budsRepository.batteryRefreshError.collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val safeMode by budsRepository.safeMode.collectAsStateWithLifecycle(initialValue = null as SafeModeState?)
            val deviceInfo by budsRepository.deviceInfo.collectAsStateWithLifecycle(initialValue = null as DeviceInfo?)
            val ringing by budsRepository.ringing.collectAsStateWithLifecycle(initialValue = null as RingNotice?)
            val lastConnectionError by budsRepository.lastConnectionError
                .collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val lastLossCause by budsRepository.lastLossCause.collectAsStateWithLifecycle(initialValue = null as SessionLossCause?)
            val messageStreamError by budsRepository.messageStreamError
                .collectAsStateWithLifecycle(initialValue = null as BudsError?)
            val permissionState by permissionStateFlow.collectAsStateWithLifecycle()
            // Re-created when the Bluetooth grant changes: the profile proxies can only be bound with the permission. Collected only while the
            // UI is at least STARTED (visibility-bound, ARCHITECTURE.md §6.0b). Every reading goes to the repository (the loss cause, I-7, and the
            // re-open rule, ADR-044 — `ai-sessions/0048`); the screen shows the debounced value ([settled]) so a bud coming out of the case does
            // not flicker the card.
            val lifecycleOwnerForLink = LocalLifecycleOwner.current
            val linkReadings = remember(permissionState.bluetoothConnect.isGranted) { osConnectionObserver.observe(linkRefresh) }
            LaunchedEffect(linkReadings, lifecycleOwnerForLink) {
                lifecycleOwnerForLink.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    linkReadings.collect { link ->
                        latestLink.value = link
                        budsRepository.onAndroidLink(link)
                    }
                }
            }
            val androidLink by remember { latestLink.settled(OsConnectionObserver.NOT_CONNECTED_SETTLE_MS) }
                .collectAsStateWithLifecycle(initialValue = AndroidLink.UNKNOWN)
            // `ai-sessions/0064` F-3: every adapter reading also goes to the repository (an off reading around a session loss names its cause), collected
            // while the UI is at least STARTED — the same visibility bound as the link readings above. The screen shows the latest value.
            val adapterStates = remember { MutableStateFlow(bluetoothStateObserver.current()) }
            LaunchedEffect(lifecycleOwnerForLink) {
                lifecycleOwnerForLink.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    bluetoothStateObserver.observe().collect { adapter ->
                        adapterStates.value = adapter
                        budsRepository.onBluetoothAdapter(BluetoothStateObserver.isOn(adapter))
                    }
                }
            }
            val bluetoothAdapterState by adapterStates.collectAsStateWithLifecycle()
            val debugModeEnabled by debugSettingsStore.debugModeEnabled.collectAsStateWithLifecycle(initialValue = false)
            val darkMode by darkModeSettingsStore.darkMode.collectAsStateWithLifecycle(initialValue = DarkMode.DEFAULT)

            var bondedLookup by remember { mutableStateOf(companionPairing.lookupBonded()) }
            // "Use different Buds" (A68-APP-05) changes which bonded device is this app's — re-read the lookup when it has run.
            val bondedChanges by bondedChanged.collectAsStateWithLifecycle()
            LaunchedEffect(bondedChanges) { bondedLookup = companionPairing.lookupBonded() }
            val pairingState by pairingStateFlow.collectAsStateWithLifecycle()
            LaunchedEffect(pairingState) { if (pairingState is PairingState.Bonded) bondedLookup = companionPairing.lookupBonded() }

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
                        linkRefresh.tryEmit(Unit)
                        // ADR-044 (c): visible again — the session may be re-opened once Android reports the Buds connected.
                        budsRepository.onAppVisible(true)
                    } else if (event == Lifecycle.Event.ON_STOP) {
                        // Not visible any more: no automatic re-open in the background (ADR-044 item 3).
                        budsRepository.onAppVisible(false)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            // A grant given while the app is open (prompt or system settings) changes what "bonded" can be seen.
            LaunchedEffect(permissionState.bluetoothConnect) { bondedLookup = companionPairing.lookupBonded() }
            // Android's link state is re-read when the bonded device or this app's own session changes — the live system
            // broadcasts are not guaranteed on every device (ai-sessions/0042).
            LaunchedEffect(bondedLookup, connectionState) { linkRefresh.tryEmit(Unit) }

            // ADR-048 item 2 (A58-APP-08): kept in [AppUiSession], so a rotation no longer empties the Debug list.
            val unidentifiedFrames by uiSession.unidentifiedFrames.collectAsStateWithLifecycle()

            // The foreground service is driven from the application scope (OpenControlApplication), not from this lifecycle-bound
            // UI state — so a session that ends while the app is in the background still stops it (0044 finding APP-6).

            val hasBondedDevice = bondedLookup is BondedLookup.Found
            val bluetoothEnabled = bluetoothAdapterState == BluetoothAdapterState.ON
            val state = OpenControlUiState(
                connectionState = connectionState,
                deviceStatus = deriveDeviceStatus(
                    bluetoothEnabled = bluetoothEnabled,
                    bluetoothConnect = permissionState.bluetoothConnect,
                    hasBondedDevice = hasBondedDevice,
                    severalCandidates = bondedLookup is BondedLookup.SeveralCandidates,
                    androidLink = androidLink,
                    session = connectionState,
                ),
                permissionState = permissionState,
                pairingStatusText = pairingState?.toUserMessage(),
                lastConnectionError = lastConnectionError,
                lastLossCause = lastLossCause,
                androidLink = androidLink,
                messageStreamError = messageStreamError,
                ancMode = ancMode,
                ancModeUpdatedAt = ancModeUpdatedAt,
                sessionSince = sessionSince,
                ancAvailability = ancAvailability,
                ancAvailabilityUpdatedAt = ancAvailabilityUpdatedAt,
                ancModeUnconfirmedAt = ancModeUnconfirmedAt,
                caseBatteryError = caseBatteryError,
                batteryRefreshError = batteryRefreshError,
                safeMode = safeMode,
                deviceInfo = deviceInfo,
                ringing = ringing,
                eqProfile = eqProfile,
                eqProfileUpdatedAt = eqProfileUpdatedAt,
                eqError = eqError,
                settings = settings,
                settingsError = settingsError,
                batteryStatus = batteryStatus,
                batteryStatusUpdatedAt = batteryStatusUpdatedAt,
                unidentifiedFrames = unidentifiedFrames,
                debugModeEnabled = debugModeEnabled,
                darkMode = darkMode,
                appBuild = APP_BUILD,
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
                onPair = { startPairing() },
                onRequestPermissions = { requestPermissions() },
                onOpenAppSettings = {
                    BleLogger.logConnectionEvent("Permissions: opening the app's system settings")
                    startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)),
                    )
                },
                onConnect = { applicationScope.launch { budsRepository.connect() } },
                onDisconnect = { applicationScope.launch { budsRepository.disconnect() } },
                onAncModeSelected = { mode -> applicationScope.launch { budsRepository.setAncMode(mode) } },
                onRefreshAncMode = { applicationScope.launch { budsRepository.refreshAncMode() } },
                onRequestAddAncTile = {
                    // Asks Android directly to add the tile (API 33+, this app's minSdk) instead of
                    // requiring the user to find it themselves via Quick Settings' own edit screen
                    // (`ai-sessions/0043`, `CAP-060-FINDINGS.md` §4). A user action only — never
                    // called automatically on launch/connect (ARCHITECTURE.md §6).
                    getSystemService(StatusBarManager::class.java)?.requestAddTileService(
                        ComponentName(this, AncTileService::class.java),
                        "ANC",
                        Icon.createWithResource(this, R.drawable.ic_anc_tile),
                        ContextCompat.getMainExecutor(this),
                    ) { result ->
                        BleLogger.logConnectionEvent("Add ANC tile request result: $result")
                        // `CAP-061`: the result was 1 (already added) and the screen showed nothing — say what happened.
                        Toast.makeText(this, ancTileResultText(result), Toast.LENGTH_LONG).show()
                    }
                },
                onEqGainsChanged = { gains -> applicationScope.launch { budsRepository.setEqGains(gains) } },
                onEqPresetSelected = { preset -> applicationScope.launch { budsRepository.applyEqPreset(preset) } },
                onRefreshEq = { applicationScope.launch { budsRepository.refreshEq() } },
                // DECISIONS.md ADR-045 (`ai-sessions/0052`): one write per completed gesture, in the application scope (0044 APP-4).
                onVolumeBalanceChanged = { v -> applicationScope.launch { budsRepository.setVolumeBalance(v) } },
                onMonoAudioChanged = { on -> applicationScope.launch { budsRepository.setMonoAudio(on) } },
                onConversationDetectionChanged = { on -> applicationScope.launch { budsRepository.setConversationDetection(on) } },
                onTouchControlsChanged = { on -> applicationScope.launch { budsRepository.setTouchControls(on) } },
                onPressAndHoldChanged = { bud, action -> applicationScope.launch { budsRepository.setPressAndHold(bud, action) } },
                // DECISIONS.md ADR-046 / ADR-047 (`ai-sessions/0056`): one write per tap, in the application scope.
                onAncModeSelectedChanged = { mode, selected -> applicationScope.launch { budsRepository.setAncModeSelected(mode, selected) } },
                onInEarDetectionChanged = { on -> applicationScope.launch { budsRepository.setInEarDetection(on) } },
                // DECISIONS.md ADR-053 (`ai-sessions/0074`): one write per tap, in the application scope.
                onMultipointChanged = { on -> applicationScope.launch { budsRepository.setMultipoint(on) } },
                onHeadGesturesChanged = { on -> applicationScope.launch { budsRepository.setHeadGestures(on) } }, // ADR-052
                onCaseSoundEarbudsReplacedChanged = { on -> applicationScope.launch { budsRepository.setCaseSoundEarbudsReplaced(on) } }, // ADR-054
                onCaseSoundOtherAlertsChanged = { on -> applicationScope.launch { budsRepository.setCaseSoundOtherAlerts(on) } }, // ADR-054
                onVolumeEqChanged = { on -> applicationScope.launch { budsRepository.setVolumeEq(on) } }, // ADR-055
                onRing = { target -> applicationScope.launch { budsRepository.ringBud(target) } },
                onStopRinging = { applicationScope.launch { budsRepository.stopRinging() } },
                onRefreshBattery = { applicationScope.launch { budsRepository.refreshBattery() } },
                onDebugModeChanged = { enabled -> applicationScope.launch { debugSettingsStore.setDebugModeEnabled(enabled) } },
                onDarkModeChanged = { mode -> applicationScope.launch { darkModeSettingsStore.setDarkMode(mode) } },
                onExportLog = { startLogExport() },
                onOpenUrl = { url -> openUrl(url) },
                onUseDifferentBuds = { useDifferentBuds() },
            )
            // `ai-sessions/0057` D-10: a pull runs the one existing action `pullActionFor` chose — the same repository call or system prompt its button makes —
            // once, in the application scope; the returned job lets the pull indicator end when the repository has answered. Nothing is retried or scheduled.
            val actionsWithPull = actions.copy(
                onPull = { action ->
                    when (action) {
                        PullAction.REFRESH_BATTERY -> applicationScope.launch { budsRepository.refreshBattery() }
                        PullAction.REFRESH_ANC -> applicationScope.launch { budsRepository.refreshAncMode() }
                        // "Read EQ again", then the settings re-read (D-11) — one after the other, never in parallel.
                        PullAction.REFRESH_SOUND -> applicationScope.launch {
                            budsRepository.refreshEq()
                            budsRepository.refreshSettings()
                        }
                        PullAction.REFRESH_SETTINGS -> applicationScope.launch { budsRepository.refreshSettings() }
                        PullAction.CONNECT -> applicationScope.launch { budsRepository.connect() }
                        PullAction.ENABLE_BLUETOOTH -> { actions.onRequestEnableBluetooth(); null }
                        PullAction.REQUEST_PERMISSIONS -> { actions.onRequestPermissions(); null }
                        PullAction.OPEN_APP_SETTINGS -> { actions.onOpenAppSettings(); null }
                        PullAction.PAIR -> { actions.onPair(); null }
                        PullAction.NOTHING -> null
                    }
                },
            )

            // F-6: the stored choice, or Android's own setting for "System" — recomposed (not recreated) when either changes.
            OpenControlTheme(darkTheme = darkMode.isDark(isSystemInDarkTheme())) {
                OpenControlNavHost(state = state, actions = actionsWithPull)
            }
        }
    }
}

/** F-6: the message when no app can open a link (the maintainer's preview, chat 2026-10-01) — the address is shown so it can be typed elsewhere. */
internal fun noBrowserText(url: String): String = "No app on this phone can open web links. The address is $url"

/** F-5: the build identity computed locally at build time (`app/build.gradle.kts`) — "unknown" parts when git was not available. */
private val APP_BUILD = AppBuildInfo(BuildConfig.VERSION_NAME, BuildConfig.GIT_COMMIT, BuildConfig.GIT_COMMIT_DATE)

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
    PairingFailure.AlreadyInProgress -> "A pairing request is already open — finish it in the system dialog first."
}

/**
 * The outcome of `StatusBarManager.requestAddTileService` in words (developer.android.com `StatusBarManager`, checked 2026-09-24:
 * `TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED` = 1 "the tile was already added and the user was not prompted", `…_TILE_ADDED` = 2,
 * `…_TILE_NOT_ADDED` = 0; errors are 1000–1005).
 */
internal fun ancTileResultText(result: Int): String = when (result) {
    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> "The ANC tile was added to Quick Settings."
    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
        "The ANC tile is already in Quick Settings — open Quick Settings fully and swipe through its pages, or edit it to move the tile."
    StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> "The ANC tile was not added."
    else -> "Android could not add the ANC tile (code $result)."
}
