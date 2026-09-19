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
    /** The link is gone and no more specific cause is known (e.g. a `send()` with no open socket). */
    data object ConnectionLost : BudsError()

    /**
     * Opening RFCOMM channel [channelId]'s socket failed (after this app's own bounded retries).
     * Distinct from [ConnectionLost] on purpose (`ai-sessions/0039`): the OS-level Bluetooth link to
     * the Buds is usually fine when this happens — only this one RFCOMM channel could not be
     * opened, which the Android Bluetooth stack's own log attributes to the channel already being
     * open in another client (`ai-sessions/0039` §3). [detail] is the underlying exception's class
     * and message with any Bluetooth address redacted, for display and the debug log only.
     */
    data class ChannelUnavailable(val channelId: Int, val detail: String?) : BudsError()

    /**
     * A previously open RFCOMM channel [channelId] died while connected (read/write failed or the
     * peer closed the stream). Same [detail] contract as [ChannelUnavailable].
     */
    data class ChannelLost(val channelId: Int, val detail: String?) : BudsError()

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
