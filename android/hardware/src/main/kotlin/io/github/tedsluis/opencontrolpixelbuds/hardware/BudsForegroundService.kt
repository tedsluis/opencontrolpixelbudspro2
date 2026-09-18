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
package io.github.tedsluis.opencontrolpixelbuds.hardware

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Hosts the live connection while the app has an active/foreground use of it
 * — started/stopped per ARCHITECTURE.md §6.0a's documented lifecycle, never
 * eagerly at process start and never restarted automatically
 * (`START_NOT_STICKY`, matching the "no aggressive background retry loops"
 * rule, ARCHITECTURE.md §6). `:app`'s composition root starts/stops this
 * service in response to `ConnectionStateMachine` transitions — this class
 * itself holds no Bluetooth logic, only the notification/lifecycle shell.
 *
 * // TODO(verify): not exercised against a real foreground-service launch in
 * // this environment (no device/emulator run performed this session) —
 * // compiles and follows the documented shape only.
 */
class BudsForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val statusText = intent?.getStringExtra(EXTRA_STATUS_TEXT) ?: "Connecting…"
        // DECISIONS.md ADR-029: minSdk 34, so the FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE overload
        // (API 29+) is unconditionally available — no version check needed.
        startForeground(NOTIFICATION_ID, buildNotification(statusText), ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        return START_NOT_STICKY
    }

    /** Updates the persistent notification's status line — e.g. current ANC
     * mode once `Ready` (ARCHITECTURE.md §6.0a). Never includes raw payload
     * content (AGENTS.md §9's logging-privacy rules apply to user-visible
     * text too). */
    fun updateStatus(statusText: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    private fun buildNotification(statusText: String): Notification {
        ensureChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OpenControl for Pixel Buds")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW) // AGENTS.md §2: persistent, low-priority.
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .build()
    }

    private fun ensureChannel() {
        // DECISIONS.md ADR-029: minSdk 34, so notification channels (API 26+) always exist —
        // no version check needed.
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Buds connection", NotificationManager.IMPORTANCE_LOW),
        )
    }

    companion object {
        const val EXTRA_STATUS_TEXT = "status_text"
        private const val CHANNEL_ID = "buds_connection"
        private const val NOTIFICATION_ID = 1
    }
}
