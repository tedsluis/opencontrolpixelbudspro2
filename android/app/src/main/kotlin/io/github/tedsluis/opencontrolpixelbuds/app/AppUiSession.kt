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

import android.bluetooth.BluetoothDevice
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsCompanionPairing
import io.github.tedsluis.opencontrolpixelbuds.hardware.PairingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UI state that must survive a configuration change, held outside the Activity (`DECISIONS.md` ADR-048 item 2; `ai-sessions/0058` A58-APP-08):
 * the pairing progress and its bonding observation — which used to run in the composition's `rememberCoroutineScope()`, so a rotation during pairing
 * cancelled it — and the Debug list of unidentified frames, which used to be `remember`ed and emptied on every rotation. Application-scoped, like every user
 * action (0044 APP-4); the bonding observation uses the application-context [BudsCompanionPairing], never the Activity.
 */
@Singleton
class AppUiSession @Inject constructor(
    repository: BudsRepository,
    private val companionPairing: BudsCompanionPairing,
    private val applicationScope: CoroutineScope,
) {
    /** Pairing progress; the Activity sets `Requesting`/`Failed`/`null` from the system picker's callbacks, the bonding observation the rest. */
    val pairingState = MutableStateFlow<PairingState?>(null)

    /**
     * Whether a system permission prompt was already shown since this process started (`permissionStatus`). Here, not in the Activity
     * (`ai-sessions/0069`, A68-APP-09): as an Activity field it was reset by a rotation, and a denied permission then read "not requested" again.
     */
    @Volatile
    var permissionsRequestedThisRun: Boolean = false

    /**
     * The debug-log text waiting for the system "save as" dialog's answer. Here for the same reason: a rotation while the dialog was open recreated the
     * Activity and the text to write was gone.
     */
    @Volatile
    var pendingLogExport: String? = null

    @Volatile
    private var bondingJob: Job? = null

    private val _unidentifiedFrames = MutableStateFlow<List<UnidentifiedFrame>>(emptyList())

    /** The last [MAX_UNIDENTIFIED_FRAMES] unidentified frames of this process, for the Debug screen. */
    val unidentifiedFrames: StateFlow<List<UnidentifiedFrame>> = _unidentifiedFrames

    init {
        applicationScope.launch {
            repository.unidentifiedFrames.collect { frame -> _unidentifiedFrames.update { (it + frame).takeLast(MAX_UNIDENTIFIED_FRAMES) } }
        }
    }

    /** Classic bonding after a CDM association (ARCHITECTURE.md §9.0a step 5); a newer pairing supersedes an older wait (and its timeout). */
    fun startBonding(device: BluetoothDevice) {
        bondingJob?.cancel()
        bondingJob = applicationScope.launch {
            companionPairing.observeBonding(device).collect { pairingState.value = it }
        }
    }

    private companion object {
        const val MAX_UNIDENTIFIED_FRAMES = 200
    }
}
