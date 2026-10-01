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
package io.github.tedsluis.opencontrolpixelbuds.domain

/**
 * The app's dark-mode setting (`ai-sessions/0062` F-6, the maintainer's choice in chat 2026-10-01): **System** (follow Android, the default), **On**, **Off**.
 * Stored locally (AndroidX DataStore, `:data/settings`), never sent anywhere.
 */
enum class DarkMode {
    SYSTEM,
    ON,
    OFF,
    ;

    /** Whether the dark colour scheme applies, given Android's own setting [systemIsDark]. */
    fun isDark(systemIsDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemIsDark
        ON -> true
        OFF -> false
    }

    companion object {
        val DEFAULT: DarkMode = SYSTEM

        /** The stored name back to the setting; anything unknown (or nothing stored) is the default — never a crash on an old or damaged value. */
        fun fromStored(name: String?): DarkMode = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
