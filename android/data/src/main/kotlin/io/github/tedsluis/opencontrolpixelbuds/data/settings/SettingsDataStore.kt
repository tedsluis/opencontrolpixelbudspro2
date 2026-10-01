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
import androidx.datastore.preferences.preferencesDataStore

/**
 * The app's one local settings file (plain AndroidX DataStore Preferences, `opencontrol_settings`), shared by [DebugSettingsStore] and [DarkModeSettingsStore]
 * (`ai-sessions/0062` F-6: a second key in the existing file, no new dependency). One delegate for the file: DataStore allows a single active instance per file.
 * Nothing here is sensitive (a Debug-mode switch and a dark-mode choice), so it is not encrypted (`ARCHITECTURE.md` §2/§9, `AGENTS.md` §10).
 */
internal val Context.openControlSettings by preferencesDataStore(name = "opencontrol_settings")
