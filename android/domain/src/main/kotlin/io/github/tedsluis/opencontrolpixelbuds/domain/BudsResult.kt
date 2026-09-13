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
 * Sealed result type every public, failure-capable function returns instead
 * of throwing across a module boundary (AGENTS.md §3, ARCHITECTURE.md §7).
 */
sealed class BudsResult<out T> {
    data class Success<T>(val value: T) : BudsResult<T>()
    data class Failure(val error: BudsError) : BudsResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): BudsResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun onSuccess(action: (T) -> Unit): BudsResult<T> {
        if (this is Success) action(value)
        return this
    }

    inline fun onFailure(action: (BudsError) -> Unit): BudsResult<T> {
        if (this is Failure) action(error)
        return this
    }
}
