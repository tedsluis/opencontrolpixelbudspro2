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
package io.github.tedsluis.opencontrolpixelbuds.app

import androidx.compose.runtime.Composable
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsRepository
import kotlinx.coroutines.CoroutineScope

/**
 * The Debug tab's slot in the debug and release build types: empty. Round 1's experiment card (`ai-sessions/0096`) exists only in the
 * `experiment` build type (src/experiment) — a released APK has no experiment code.
 */
object ExperimentHook {
    @Suppress("UNUSED_PARAMETER")
    @Composable
    fun Content(repository: BudsRepository, scope: CoroutineScope, debugModeEnabled: Boolean) = Unit
}
