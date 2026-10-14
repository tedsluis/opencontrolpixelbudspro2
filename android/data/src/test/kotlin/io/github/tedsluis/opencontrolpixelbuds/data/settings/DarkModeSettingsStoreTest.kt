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

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.tedsluis.opencontrolpixelbuds.domain.DarkMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * DECISIONS.md ADR-065 item 5 (`ai-sessions/0091`): the key `hold_reclaim_mode` that 1.2.1/1.2.2 wrote into the app's settings file (ADR-062) is ignored —
 * a file holding it still reads and writes dark mode correctly, and the key is left exactly as it was (never migrated, never removed). On the JVM, against a
 * temporary file (`PreferenceDataStoreFactory`, no Android context), read back by a **second** instance over the same file.
 */
class DarkModeSettingsStoreTest {

    @TempDir
    lateinit var dir: File

    private val holdReclaimKey = stringPreferencesKey("hold_reclaim_mode")
    private val darkModeKey = stringPreferencesKey("dark_mode")

    @Test
    @DisplayName("a 1.2.2 settings file with hold_reclaim_mode = RECLAIM: dark mode reads and writes as before, the stored choice stays untouched and unread")
    fun `a stored hold_reclaim_mode is ignored, never migrated`() = runBlocking {
        val file = File(dir, "opencontrol_settings.preferences_pb")
        val first = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val written = PreferenceDataStoreFactory.create(scope = first) { file }
        written.edit { prefs ->
            prefs[holdReclaimKey] = "RECLAIM" // 1.2.2's "This phone has Google Play services"
            prefs[darkModeKey] = "ON"
        }
        first.cancel() // DataStore allows one active instance per file

        val second = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val dataStore = PreferenceDataStoreFactory.create(scope = second) { file }
        val store = DarkModeSettingsStore(dataStore)
        assertEquals(DarkMode.ON, store.darkMode.first(), "dark mode is read from the file")
        store.setDarkMode(DarkMode.OFF)
        assertEquals(DarkMode.OFF, store.darkMode.first())
        val prefs = dataStore.data.first()
        assertEquals("RECLAIM", prefs[holdReclaimKey], "the old key is left as it was — ignored, not migrated, not removed")
        assertEquals(setOf("hold_reclaim_mode", "dark_mode"), prefs.asMap().keys.map { it.name }.toSet(), "no new key")
        second.cancel()
    }
}
