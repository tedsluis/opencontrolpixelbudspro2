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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.tedsluis.opencontrolpixelbuds.domain.DarkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the dark-mode choice of the Settings tab (`ai-sessions/0062` F-6, the maintainer's choice in chat 2026-10-01: System / On / Off, default System) in
 * the app's existing settings file ([openControlSettings]). Stored as the [DarkMode] name; an unknown or missing value reads as [DarkMode.DEFAULT].
 */
class DarkModeSettingsStore(private val context: Context) {

    val darkMode: Flow<DarkMode> = context.openControlSettings.data.map { prefs -> DarkMode.fromStored(prefs[DARK_MODE_KEY]) }

    suspend fun setDarkMode(mode: DarkMode) {
        context.openControlSettings.edit { prefs -> prefs[DARK_MODE_KEY] = mode.name }
    }

    companion object {
        private val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
    }
}
