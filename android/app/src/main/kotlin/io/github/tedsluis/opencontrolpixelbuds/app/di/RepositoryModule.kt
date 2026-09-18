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

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.tedsluis.opencontrolpixelbuds.data.BudsRepositoryImpl
import io.github.tedsluis.opencontrolpixelbuds.data.settings.DebugSettingsStore
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsCompanionPairing
import io.github.tedsluis.opencontrolpixelbuds.hardware.BudsTransport
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine
import io.github.tedsluis.opencontrolpixelbuds.hardware.HfpBatteryReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Wires `:data`'s `BudsRepositoryImpl` into the real `BudsRepository`
 * interface (ARCHITECTURE.md §2.1, DECISIONS.md ADR-001). [BudsTransport]
 * resolves to `RfcommBudsTransport` (`TransportModule.kt`, `ai-sessions/0037`
 * — the real transport is now the default, `FakeBudsTransport` stays only in
 * test source sets).
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /** Application-lifetime scope for `BudsRepositoryImpl`'s background
     * collectors (ARCHITECTURE.md §6 — event-observation coroutines that
     * live as long as the app's own Bluetooth connection does, not tied to
     * any single screen's lifecycle). */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideDebugSettingsStore(@ApplicationContext context: Context): DebugSettingsStore =
        DebugSettingsStore(context)

    @Provides
    @Singleton
    fun provideBudsCompanionPairing(@ApplicationContext context: Context): BudsCompanionPairing =
        BudsCompanionPairing(context)

    @Provides
    @Singleton
    fun provideBudsRepository(
        transport: BudsTransport,
        connectionStateMachine: ConnectionStateMachine,
        companionPairing: BudsCompanionPairing,
        @ApplicationContext context: Context,
        debugSettingsStore: DebugSettingsStore,
        scope: CoroutineScope,
    ): BudsRepository = BudsRepositoryImpl(
        transport = transport,
        connectionStateMachine = connectionStateMachine,
        bondedDeviceProvider = companionPairing::bondedDevice,
        hfpBatteryPercent = HfpBatteryReader(context).observeBievBatteryPercent(),
        debugModeEnabled = debugSettingsStore.debugModeEnabled,
        scope = scope,
    )
}
