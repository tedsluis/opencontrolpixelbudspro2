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
package io.github.tedsluis.opencontrolpixelbuds.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.tedsluis.opencontrolpixelbuds.hardware.BluetoothRfcommSocket
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsTransport
import io.github.tedsluis.opencontrolpixelbuds.hardware.RfcommBudsTransport
import javax.inject.Singleton

/**
 * Binds [BudsTransport] to the real, `BluetoothSocket`-backed
 * [RfcommBudsTransport] (`ai-sessions/0037` — the maintainer now has a real
 * bonded Pixel Buds Pro 2 to test against; `FakeBudsTransport` stays wired
 * only in :hardware's test fixtures, never in the shipped app, per AGENTS.md §11).
 * The default socket factory uses `BluetoothDevice.createRfcommSocketToServiceRecord()`
 * directly — the standard Android RFCOMM-client pattern, no custom transport
 * logic beyond what `RfcommBudsTransport` itself already implements.
 */
@Module
@InstallIn(SingletonComponent::class)
object TransportModule {
    @Provides
    @Singleton
    fun provideBudsTransport(): BudsTransport = RfcommBudsTransport(
        // BLUETOOTH_CONNECT is runtime-revocable (AGENTS.md §2/§8) — lint's MissingPermission
        // check can't see across this lambda into RfcommBudsTransport.connect()'s own
        // `catch (e: SecurityException)` (RfcommBudsTransport.kt), which is the real call site
        // that converts a denied/revoked permission into BudsError.PermissionDenied, so the
        // exception must be allowed to propagate out of this factory unhandled, not swallowed
        // here.
        socketFactory = { _, uuid, device ->
            try {
                BluetoothRfcommSocket(device.createRfcommSocketToServiceRecord(uuid))
            } catch (e: SecurityException) {
                throw e
            }
        },
    )
}
