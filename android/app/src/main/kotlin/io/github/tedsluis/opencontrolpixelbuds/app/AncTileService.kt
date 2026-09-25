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

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dagger.hilt.android.AndroidEntryPoint
import io.github.tedsluis.opencontrolpixelbuds.R
import android.widget.Toast
import io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A Quick Settings tile that switches the ANC mode with one tap (`ai-sessions/0042`, maintainer-approved in chat 2026-09-20: an ANC tile, no
 * EQ preset export/import).
 *
 * **Only a user action, nothing in the background** (`ARCHITECTURE.md` §6, DECISIONS.md ADR-032): a tap on the tile is the user's own ANC
 * tap — it claims the Message Stream channel exactly like the ANC screen does. **It never opens the session:** when the app's session is
 * not `Ready` the tile says so and a tap opens the app instead (no background connect, no service start from the tile). The tile only
 * reads the repository while it is visible (`onStartListening` … `onStopListening`).
 *
 * The cycle is fixed and documented ([AncMode.nextInTileCycle]): Noise cancelling → Transparent → Adaptive → Off. No new permission:
 * `BIND_QUICK_SETTINGS_TILE` is required *of the caller that binds this service* (only the system), not requested by the app.
 *
 * **I-3 (`ai-sessions/0048`):** while the Buds' last `Notify` reported Settable `0x00` ([AncAvailability.NOT_ALLOWED]) the subtitle says
 * "Only while worn" and a tap shows the reason instead of sending (the Buds would NAK it, `CAP-062` 4/4 tile taps with the buds docked).
 *
 * // TODO(verify): not exercised on a phone — whether GrapheneOS shows/keeps the tile and that the tap works with the app in the
 * // background (`ai-sessions/0042` §12 re-test).
 */
@AndroidEntryPoint
class AncTileService : TileService() {

    @Inject
    lateinit var repository: BudsRepository

    @Inject
    lateinit var applicationScope: CoroutineScope

    private val listenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var listenJob: Job? = null

    @Volatile
    private var latestSession: ConnectionState = ConnectionState.Disconnected

    @Volatile
    private var latestMode: AncMode? = null

    @Volatile
    private var latestAvailability: AncAvailability = AncAvailability.UNKNOWN

    override fun onStartListening() {
        super.onStartListening()
        listenJob?.cancel()
        listenJob = listenScope.launch {
            combine(repository.connectionState, repository.ancMode, repository.ancAvailability) { session, mode, availability ->
                Triple(session, mode, availability)
            }.collect { (session, mode, availability) ->
                latestSession = session
                latestMode = mode
                latestAvailability = availability
                render()
            }
        }
        render()
    }

    override fun onStopListening() {
        listenJob?.cancel()
        listenJob = null
        super.onStopListening()
    }

    override fun onDestroy() {
        listenScope.coroutineContext[Job]?.cancel()
        super.onDestroy()
    }

    override fun onClick() {
        super.onClick()
        if (latestSession !is ConnectionState.Ready) {
            // Never connect from the tile: open the app (which may re-open the session itself while visible, ADR-044).
            val open = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            startActivityAndCollapse(open)
            return
        }
        if (latestAvailability == AncAvailability.NOT_ALLOWED) {
            BleLogger.logConnectionEvent("ANC tile tapped while the Buds allow no ANC change — nothing sent")
            Toast.makeText(this, "ANC can only be changed while you wear the Buds.", Toast.LENGTH_LONG).show()
            return
        }
        val next = AncMode.nextForTile(latestMode)
        BleLogger.logConnectionEvent("ANC tile tapped: ${latestMode ?: "unknown"} -> $next")
        // The application scope, not this service's: the claim outlives the tile's short lifetime (the release job is the repository's).
        applicationScope.launch { repository.setAncMode(next) }
    }

    private fun render() {
        val tile = qsTile ?: return
        val ready = latestSession is ConnectionState.Ready
        tile.label = "ANC"
        tile.icon = Icon.createWithResource(this, R.drawable.ic_anc_tile)
        tile.subtitle = when {
            !ready -> "Open the app"
            latestAvailability == AncAvailability.NOT_ALLOWED -> "Only while worn"
            latestMode == null -> "Tap to switch"
            else -> latestMode.toString().lowercase().replaceFirstChar { it.uppercase() }
        }
        tile.state = if (ready && latestMode != null && latestMode != AncMode.OFF) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
