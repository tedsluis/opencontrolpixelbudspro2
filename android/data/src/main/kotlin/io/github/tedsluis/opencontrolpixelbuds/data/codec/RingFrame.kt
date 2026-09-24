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
package io.github.tedsluis.opencontrolpixelbuds.data.codec

import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget

/**
 * Message Group 0x04 ("Action") on DLCI 0x04's official Fast Pair Message
 * Stream (PROTOCOL.md §4.4, §2.1). Group/Code/Value constants are the exact
 * wire values confirmed by DECISIONS.md ADR-011.
 */
object RingMessageStream {
    const val GROUP: Int = 0x04
    const val CODE_RING: Int = 0x01
    const val VALUE_STOP: Int = 0x00
}

/**
 * One decoded/encodable Find My Buds Ring frame (Left/Right only —
 * ARCHITECTURE.md §5a/DECISIONS.md ADR-027: Case and "ring both" are out of
 * v1 scope, so this type has no representation for them at all). ADR-027's
 * 2026-09-24 Update: the Device Action spec does define `0x03` = ring both,
 * untested on this firmware; sending it needs its own ADR (`FIND-004`).
 */
sealed class RingFrame {
    /** Seeker -> Provider. Modeled on `CAP-025` frames 2040/2131 (Start). */
    data class Start(val target: RingTarget) : RingFrame()

    /** Seeker -> Provider, value `0x00`, shared (not per-earbud). Modeled on
     * `CAP-025` frames 2120/2180 (Stop). */
    data object Stop : RingFrame()

    // The ACK/NAK to a Start/Stop (`CAP-025` shows two ACK variants, with and without a trailing state byte) is decoded by
    // MessageStreamReplyDecoder — one routed type for every command (0044 finding APP-3).
}
