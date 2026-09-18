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

import android.util.Log
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

/**
 * Local-only logging (ARCHITECTURE.md §12, AGENTS.md §9) — never transmitted
 * off-device, no network call anywhere in this class or its callers. Writes
 * to `Logcat` and an in-app ring buffer for an "Export debug log" affordance
 * (the Debug screen, `ARCHITECTURE.md` §2.4/§7).
 *
 * - **Always safe to log, no gating**: connection-state transitions, MTU/
 *   connection-parameter changes, pairing events (AGENTS.md §9/§13).
 * - **Gated behind [debugModeEnabled]**: raw payload hex dumps, per-frame
 *   Group/Code/payload detail — off by default.
 * - **Never logged at any level**: the paired device's MAC address in
 *   plaintext — [logConnectionEvent] takes a pre-truncated/hashed string if a
 *   caller needs to disambiguate devices, never the raw address (AGENTS.md
 *   §9's rule applies to this class's own callers, not enforced here by a
 *   type system that can't tell a MAC string from any other string, but
 *   documented here as the contract every caller must follow).
 */
object BleLogger {
    private const val TAG = "OpenControlBuds"
    private const val RING_BUFFER_CAPACITY = 500

    private val ringBuffer = ArrayDeque<String>(RING_BUFFER_CAPACITY)
    private val timestampFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    @Synchronized
    private fun append(line: String) {
        val stamped = "${timestampFormat.format(Date())} $line"
        if (ringBuffer.size >= RING_BUFFER_CAPACITY) ringBuffer.removeFirst()
        ringBuffer.addLast(stamped)
    }

    /** Connection-state transitions, MTU/connection-parameter changes, pairing
     * events — always logged, never gated (AGENTS.md §9). */
    fun logConnectionEvent(message: String) {
        Log.d(TAG, message)
        append(message)
    }

    /** Raw payload hex dumps / per-frame Group-Code detail — only emitted when
     * [debugModeEnabled] is true (the Debug screen's own toggle, persisted via
     * `DebugSettingsStore`). */
    fun logHexDump(channelId: Int, bytes: ByteArray, debugModeEnabled: Boolean) {
        if (!debugModeEnabled) return
        val hex = bytes.joinToString(" ") { "%02x".format(it) }
        val line = "DLCI 0x%02x: %s".format(channelId, hex)
        Log.d(TAG, line)
        append(line)
    }

    fun logMalformedFrame(channelId: Int, debugModeEnabled: Boolean) {
        val line = "DLCI 0x%02x: malformed frame dropped".format(channelId)
        Log.w(TAG, line)
        if (debugModeEnabled) append(line)
    }

    /** Snapshot of the ring buffer for "Export debug log" — local-only, the
     * caller decides how to persist/share it (still never over a network,
     * per AGENTS.md §9). */
    @Synchronized
    fun exportLog(): String = ringBuffer.joinToString("\n")

    @Synchronized
    fun clear() = ringBuffer.clear()
}
