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
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldReclaimMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * DECISIONS.md ADR-062 (`ai-sessions/0087`): the "Noise control with Google Play services" choice (1.2.1: "Noise-control channel") round-trips through the preferences file — on the JVM, against a temporary
 * file (`PreferenceDataStoreFactory`, no Android context), and read back by a **second** instance over the same file, so a value kept in memory only would
 * fail. Nothing stored, or an unknown name, reads as the default (hint only).
 */
class HoldReclaimSettingsStoreTest {

    @TempDir
    lateinit var dir: File

    private fun storeOver(file: File, scope: CoroutineScope) = HoldReclaimSettingsStore(PreferenceDataStoreFactory.create(scope = scope) { file })

    @Test
    fun `the choice is persisted in the preferences file and read back by a fresh instance, nothing stored is hint only`() = runBlocking {
        val file = File(dir, "opencontrol_settings.preferences_pb")
        val first = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val store = storeOver(file, first)
        assertEquals(HoldReclaimMode.HINT_ONLY, store.mode.first(), "nothing stored: the default")
        store.setMode(HoldReclaimMode.RECLAIM)
        assertEquals(HoldReclaimMode.RECLAIM, store.mode.first())
        first.cancel() // DataStore allows one active instance per file

        val second = CoroutineScope(Dispatchers.IO + SupervisorJob())
        assertEquals(HoldReclaimMode.RECLAIM, storeOver(file, second).mode.first(), "read from the file, not from memory")
        second.cancel()
    }

    @Test
    fun `an unknown or missing stored value is the default, the default is hint only`() {
        assertEquals(HoldReclaimMode.HINT_ONLY, HoldReclaimMode.DEFAULT)
        assertEquals(HoldReclaimMode.HINT_ONLY, HoldReclaimMode.fromStored(null))
        assertEquals(HoldReclaimMode.HINT_ONLY, HoldReclaimMode.fromStored("SOMETHING_ELSE"))
        assertEquals(HoldReclaimMode.RECLAIM, HoldReclaimMode.fromStored("RECLAIM"))
        assertEquals(HoldReclaimMode.HINT_ONLY, HoldReclaimMode.fromStored("HINT_ONLY"))
    }

    /**
     * ADR-062 Update 2 (`ai-sessions/0089`): the card was renamed and its options reworded, but a choice stored by 1.2.1 keeps its meaning — the same key
     * (`hold_reclaim_mode`) and the same names. The file is written here as 1.2.1 wrote it (the raw key and string), then read by this build's store.
     */
    @Test
    fun `a choice stored by 1_2_1 keeps its meaning after the update`() = runBlocking {
        for ((stored, expected) in listOf("RECLAIM" to HoldReclaimMode.RECLAIM, "HINT_ONLY" to HoldReclaimMode.HINT_ONLY)) {
            val file = File(dir, "v121_$stored.preferences_pb")
            val writer = CoroutineScope(Dispatchers.IO + SupervisorJob())
            PreferenceDataStoreFactory.create(scope = writer) { file }.edit { it[stringPreferencesKey("hold_reclaim_mode")] = stored }
            writer.cancel()
            val reader = CoroutineScope(Dispatchers.IO + SupervisorJob())
            assertEquals(expected, storeOver(file, reader).mode.first(), "1.2.1's \"$stored\"")
            reader.cancel()
        }
    }
}
