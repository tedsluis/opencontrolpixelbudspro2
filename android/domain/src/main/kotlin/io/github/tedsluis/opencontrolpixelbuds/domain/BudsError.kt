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
    /**
     * A write/control command was refused by the app's own Safe-Mode gate (ARCHITECTURE.md §8.1, DECISIONS.md ADR-042): the
     * connected Buds' firmware is not one this app was verified against, has not been announced yet, or the Fast Pair Model ID is
     * not the Pixel Buds Pro 2's. Nothing was sent.
     */
    data object UnsupportedFirmware : BudsError()
    data object PermissionDenied : BudsError()

    /** No bonded Pixel Buds was found (pairing is a separate, earlier step) — distinct from a missing permission (0044 APP-8). */
    data object NotPaired : BudsError()

    /**
     * The Buds answered a Message Stream command with a NAK (Fast Pair acknowledgement spec): [reasonCode] as sent
     * (`0x00` not supported, `0x01` device busy, `0x02` not allowed in the current state, `0x03` incorrect MAC, `0x04` redundant),
     * [detail] its readable name. Nothing about the command is assumed to have happened.
     */
    data class CommandRejected(val reasonCode: Int, val detail: String) : BudsError()

    /**
     * An ANC `Set` was not sent because the Buds' `Notify ANC state` — the one the tap's own claim asked for (`ai-sessions/0054` I-1) — reported no
     * switchable mode (Settable `0x00`); they NAK a `Set` then (reason `0x02`, `CAP-062` 10/10; DECISIONS.md ADR-049).
     */
    data object AncNotAllowed : BudsError()

    /**
     * *Refresh battery* reached the Message Stream but the Buds sent no "Battery updated" (`03 03`) frame on that claim (`ai-sessions/0052`): the
     * Left/Right values and their times are unchanged — the screen must not suggest a new reading. The Buds send the frame on every fresh open
     * (120 of 120 app opens, `ai-sessions/0051` §9).
     */
    data object NoNewBatteryReading : BudsError()

    /**
     * A change of the press-and-hold ANC-mode list was refused by the app itself: it would leave fewer than [AncModeList.MIN_SELECTED] modes ticked (the
     * official app's rule, `hgj.java:165–168`; DECISIONS.md ADR-046). Nothing was sent.
     */
    data object AncModeListTooShort : BudsError()

    /**
     * A change of the press-and-hold ANC-mode list was refused because the list was not read from the Buds on this connection: every write carries all
     * four booleans (ADR-046), so the other three must be the Buds' own values, never guessed (AGENTS.md §5). Nothing was sent.
     */
    data object AncModeListNotRead : BudsError()

    /**
     * A settings or EQ write (the EQ since `ai-sessions/0059`) was tapped while the session is being (re)opened — ADR-044's automatic re-open, or a Connect in progress (`ai-sessions/0056` U-2;
     * `CAP-056`: 3.3 s between the Buds' `DISC` 4168 and the re-open 4304). Nothing was sent and nothing is queued.
     */
    data object SessionOpening : BudsError()

    /**
     * The Buds answered a `ReadSetting` with status OK, but with a value this app cannot decode (e.g. a press-and-hold action other than 5/6, a field-12
     * list without all four booleans) — `ai-sessions/0059`, A58-APP-03. The value stays "not read"; the read ends at once instead of waiting for a
     * `Timeout` that would wrongly say the Buds did not answer. The raw bytes are only in Debug mode's hex dump (AGENTS.md §9).
     */
    data object UnreadableAnswer : BudsError()

    /**
     * The Buds did not announce which pw_rpc channel this connection uses (their unsolicited `GetSoftwareInfo`
     * push, DECISIONS.md ADR-034) or announced one this app has no known HDLC address for. [channelId] is `null`
     * when nothing was announced in time. Nothing is sent — the app never guesses a channel or an address.
     */
    data class MaestroChannelUnknown(val channelId: Int?) : BudsError()

    /** The Buds answered a Maestro request with an error instead of accepting it; [detail] is structure only
     * (e.g. `RESPONSE NOT_FOUND`), never payload bytes (AGENTS.md §9). */
    data class MaestroRejected(val detail: String) : BudsError()
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
