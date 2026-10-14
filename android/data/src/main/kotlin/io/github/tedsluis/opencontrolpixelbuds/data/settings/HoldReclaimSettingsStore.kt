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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldReclaimMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the Settings tab's "Noise control with Google Play services" choice (1.2.1: "Noise-control channel") (DECISIONS.md ADR-062, `ai-sessions/0087`) in the app's existing settings file
 * ([openControlSettings] — a third key, no new dependency, as [DarkModeSettingsStore]). Stored as the [HoldReclaimMode] name; an unknown or missing value reads
 * as [HoldReclaimMode.DEFAULT]. Non-sensitive (a two-way choice), so not encrypted (`ARCHITECTURE.md` §2/§9, `AGENTS.md` §10).
 *
 * The primary constructor takes the [DataStore] itself so the round trip is unit-tested on the JVM against a temporary file
 * (`PreferenceDataStoreFactory`, no Android context); the app constructs it from its [Context].
 */
class HoldReclaimSettingsStore(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.openControlSettings)

    val mode: Flow<HoldReclaimMode> = dataStore.data.map { prefs -> HoldReclaimMode.fromStored(prefs[HOLD_RECLAIM_MODE_KEY]) }

    suspend fun setMode(mode: HoldReclaimMode) {
        dataStore.edit { prefs -> prefs[HOLD_RECLAIM_MODE_KEY] = mode.name }
    }

    companion object {
        private val HOLD_RECLAIM_MODE_KEY = stringPreferencesKey("hold_reclaim_mode")
    }
}
