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
package io.github.tedsluis.opencontrolpixelbuds.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

/**
 * The app's one local settings file (plain AndroidX DataStore Preferences, `opencontrol_settings`), shared by [DebugSettingsStore] and [DarkModeSettingsStore]
 * (`ai-sessions/0062` F-6: a second key in the existing file, no new dependency). One delegate for the file: DataStore allows a single active instance per file.
 * Nothing here is sensitive (a Debug-mode switch and a dark-mode choice), so it is not encrypted (`ARCHITECTURE.md` §2/§9, `AGENTS.md` §10).
 */
internal val Context.openControlSettings by preferencesDataStore(name = "opencontrol_settings")

/**
 * [DataStore.data] that reads a file it cannot read (a corrupt file — `CorruptionException` is an [IOException] — or an I/O error) as empty preferences,
 * i.e. every setting at its default, instead of ending the collector with an exception (`ai-sessions/0094`, A93-APP-05; `AGENTS.md` §8). Logged without a
 * payload. Writes are unchanged: the next write replaces the unreadable file.
 */
internal fun DataStore<Preferences>.readableData(): Flow<Preferences> = data.catch { e ->
    if (e !is IOException) throw e
    BleLogger.logConnectionEvent("Settings file unreadable (${e.javaClass.simpleName}): the defaults are used")
    emit(emptyPreferences())
}
