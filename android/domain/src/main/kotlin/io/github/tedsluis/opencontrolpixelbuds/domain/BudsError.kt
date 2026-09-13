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
 * Shared error hierarchy for the Hardware and Data layers (ARCHITECTURE.md §7).
 * Every BluetoothSocket/BluetoothGatt call site converts caught exceptions into
 * one of these instead of letting a raw exception cross a module boundary.
 */
sealed class BudsError {
    data object ConnectionLost : BudsError()
    data object Timeout : BudsError()
    data class MalformedFrame(val raw: ByteArray) : BudsError() {
        override fun equals(other: Any?): Boolean =
            other is MalformedFrame && raw.contentEquals(other.raw)

        override fun hashCode(): Int = raw.contentHashCode()
    }
    data object UnsupportedFirmware : BudsError()
    data object PermissionDenied : BudsError()
    data class Unknown(val cause: Throwable) : BudsError()
}

/**
 * A structurally valid frame whose Group/Code isn't recognized (ARCHITECTURE.md §7).
 * Deliberately not a [BudsError] — nothing failed to parse, it failed to be
 * *understood*, and silently dropping it works against this project's
 * evidence-based reverse-engineering goal (AGENTS.md §6).
 */
data class UnidentifiedFrame(
    val channelId: Int,
    val group: Int?,
    val code: Int?,
    val raw: ByteArray,
    val timestampMillis: Long,
) {
    override fun equals(other: Any?): Boolean =
        other is UnidentifiedFrame &&
            channelId == other.channelId &&
            group == other.group &&
            code == other.code &&
            raw.contentEquals(other.raw) &&
            timestampMillis == other.timestampMillis

    override fun hashCode(): Int {
        var result = channelId
        result = 31 * result + (group ?: 0)
        result = 31 * result + (code ?: 0)
        result = 31 * result + raw.contentHashCode()
        result = 31 * result + timestampMillis.hashCode()
        return result
    }
}
