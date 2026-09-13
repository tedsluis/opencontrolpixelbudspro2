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
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsTransport
import io.github.tedsluis.opencontrolpixelbuds.hardware.FakeBudsTransport
import javax.inject.Singleton

/**
 * Binds [BudsTransport] to [FakeBudsTransport] for now — the real
 * `RfcommBudsTransport` (`:hardware`) is sketched but explicitly unverified
 * against real hardware (no physical Buds available in this environment,
 * `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phase 7). This module
 * exists to prove the Hilt composition root actually resolves an interface
 * binding end-to-end, not to claim a working hardware connection.
 *
 * // TODO(blocked on real-hardware verification): swap this binding for
 * // `RfcommBudsTransport` once it is implemented and verified end-to-end
 * // against a real Pixel Buds Pro 2, per `ARCHITECTURE.md` §2.1.
 */
@Module
@InstallIn(SingletonComponent::class)
object TransportModule {
    @Provides
    @Singleton
    fun provideBudsTransport(): BudsTransport = FakeBudsTransport()
}
