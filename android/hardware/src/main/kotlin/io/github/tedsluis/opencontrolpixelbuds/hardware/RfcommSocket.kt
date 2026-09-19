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

import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * The small slice of `BluetoothSocket` that [RfcommBudsTransport] actually uses, extracted so the
 * transport's connection-lifecycle logic (retry, teardown-on-loss, stale-event suppression — the
 * `ai-sessions/0039` fixes) can be unit-tested against a scripted fake without a real Bluetooth
 * stack (AGENTS.md §11). `BluetoothSocket` itself is a final Android framework class with no public
 * constructor, so it cannot be faked directly.
 */
interface RfcommSocket {
    /** Blocks until connected; throws [IOException] on failure (`BluetoothSocket.connect()`'s contract). */
    @Throws(IOException::class)
    fun connect()

    @get:Throws(IOException::class)
    val inputStream: InputStream

    @get:Throws(IOException::class)
    val outputStream: OutputStream

    @Throws(IOException::class)
    fun close()
}

/** Production [RfcommSocket] — a thin pass-through to a real `BluetoothSocket`. */
class BluetoothRfcommSocket(private val socket: BluetoothSocket) : RfcommSocket {
    override fun connect() {
        // BLUETOOTH_CONNECT is runtime-revocable (AGENTS.md §2/§8). The SecurityException is
        // deliberately propagated: RfcommBudsTransport.openChannel() is the real call site that
        // converts it into BudsError.PermissionDenied — lint cannot see that across this interface,
        // so the explicit catch-and-rethrow (same pattern as TransportModule) states the intent
        // without a lint suppression (ai-sessions/0034: zero suppressions).
        try {
            socket.connect()
        } catch (e: SecurityException) {
            throw e
        }
    }
    override val inputStream: InputStream get() = socket.inputStream
    override val outputStream: OutputStream get() = socket.outputStream
    override fun close() = socket.close()
}
