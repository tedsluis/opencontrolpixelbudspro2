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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "opencontrol_settings")

/**
 * Persists the Debug Mode toggle (ARCHITECTURE.md §7/§12, AGENTS.md §6/§9) —
 * off by default, so verbose hex-dump logging never turns itself on across
 * an app restart without the user explicitly having enabled it.
 *
 * // TODO(verify): `ARCHITECTURE.md` §2/§9 describes this project's local
 * // persistence mechanism as "encrypted AndroidX DataStore." This
 * // implementation uses plain (unencrypted) `Preferences` DataStore — the
 * // one value stored here (a boolean toggle) carries no device identifiers
 * // or credentials, so encrypting it specifically wasn't judged worth the
 * // added `androidx.security.crypto` dependency this session, but the
 * // architecture's own documented intent is encryption-at-rest; a future
 * // session adding a genuinely sensitive value here (e.g. a cached EQ
 * // preset tied to account state, if that's ever added) should revisit this.
 */
class DebugSettingsStore(private val context: Context) {

    val debugModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DEBUG_MODE_KEY] ?: false
    }

    suspend fun setDebugModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[DEBUG_MODE_KEY] = enabled }
    }

    companion object {
        private val DEBUG_MODE_KEY = booleanPreferencesKey("debug_mode_enabled")
    }
}
