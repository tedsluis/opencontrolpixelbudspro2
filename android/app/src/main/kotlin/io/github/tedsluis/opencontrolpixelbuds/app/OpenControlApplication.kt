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

import android.app.Application
import android.content.Intent
import dagger.hilt.android.HiltAndroidApp
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Hilt composition root's entry point (DECISIONS.md ADR-028), and the owner of [BudsForegroundService]'s lifecycle
 * (ARCHITECTURE.md §6.0a).
 *
 * **Why here and not in `MainActivity` (0044 finding APP-6):** the service used to be started/stopped from a composable over
 * lifecycle-bound state, so a session that ended while the app was in the background was never observed — the service kept running
 * with a stale "Connected — ANC: …" notification until the app came back. It is now driven from the application-lifetime scope, over
 * the same `ConnectionState` the repository publishes: started for any non-`Disconnected`/non-`Failed` state (reachable through the
 * user's own Connect tap, or ADR-044's automatic re-open, which runs only while the app is visible — no background connect exists,
 * ARCHITECTURE.md §6), its text updated while it runs, stopped at `Disconnected`/`Failed`. Nothing here connects, retries or polls.
 */
@HiltAndroidApp
class OpenControlApplication : Application() {

    @Inject
    lateinit var budsRepository: BudsRepository

    @Inject
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            var running = false
            combine(budsRepository.connectionState, budsRepository.ancMode.onStart<io.github.tedsluis.opencontrolpixelbuds.domain.AncMode?> { emit(null) }) { state, mode ->
                notificationText(state, mode)
            }.distinctUntilChanged().collect { text ->
                val intent = Intent(this@OpenControlApplication, BudsForegroundService::class.java)
                if (text != null) {
                    try {
                        // Also updates the text of an already-running service (onStartCommand re-posts the notification).
                        startForegroundService(intent.putExtra(BudsForegroundService.EXTRA_STATUS_TEXT, text))
                        running = true
                    } catch (e: IllegalStateException) {
                        // ForegroundServiceStartNotAllowedException (a subclass): Android refused a start from the background. The
                        // session itself is unaffected; say so instead of crashing (AGENTS.md §8).
                        BleLogger.logConnectionEvent("Foreground service not started: ${BleLogger.describe(e)}")
                    }
                } else if (running) {
                    stopService(intent)
                    running = false
                }
            }
        }
    }

    private fun notificationText(state: ConnectionState, mode: io.github.tedsluis.opencontrolpixelbuds.domain.AncMode?): String? =
        when (state) {
            ConnectionState.Disconnected, is ConnectionState.Failed -> null
            ConnectionState.Connecting -> "Connecting…"
            ConnectionState.Discovering -> "Discovering services…"
            ConnectionState.Ready -> mode?.let { "Connected — ANC: ${it.name}" } ?: "Connected"
        }
}
