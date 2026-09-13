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

import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode

/**
 * Message Group 0x08 ("Hearable Controls") on DLCI 0x04's official Fast Pair
 * Message Stream (PROTOCOL.md §4.1, §2.1). Group/Code values are the exact
 * wire constants confirmed by DECISIONS.md ADR-009/ADR-021/ADR-022.
 */
object AncMessageStream {
    const val GROUP: Int = 0x08
    const val CODE_GET: Int = 0x11
    const val CODE_SET: Int = 0x12
    const val CODE_NOTIFY: Int = 0x13
    const val ACK_GROUP: Int = 0xFF
    const val ACK_CODE: Int = 0x01
}

/**
 * One decoded/encodable Hearable Controls frame. Field names follow
 * PROTOCOL.md §4.1's own layout tables.
 */
sealed class AncFrame {
    /** Seeker -> Provider, no payload. Fires reliably on every DLCI 0x04
     * (re)establishment that carries real Message Stream traffic
     * (DECISIONS.md ADR-022) — this project's own connect-time state query. */
    data object Get : AncFrame()

    /**
     * Seeker -> Provider, MAC+ACK. [settableModesMask]/[enabledModesMask]
     * default to `0xe8`, the constant this project has observed in every
     * captured Set frame to date (PROTOCOL.md §4.1) — a wire-protocol
     * constant the peer expects, not app configuration (PROJECT_RULES.md §8
     * rule 22's hardcoded-strings exception).
     *
     * // TODO(verify): the 16-byte [reserved] field's real content is
     * // undecoded — every captured official-app Set frame carries a
     * // different, opaque value here (CAP-001/CAP-006 fixtures), and this
     * // project has no evidence for what a compliant value should be. This
     * // encoder zero-fills it by default; whether the Buds accept a
     * // zero-filled reserved field has not been verified against real
     * // hardware (none available in this environment). See PROTOCOL.md §4.1.
     */
    data class Set(
        val mode: AncMode,
        val seekerVersion: Int = 0x01,
        val settableModesMask: Int = 0xe8,
        val enabledModesMask: Int = 0xe8,
        val reserved: ByteArray = ByteArray(16),
    ) : AncFrame() {
        override fun equals(other: Any?): Boolean =
            other is Set &&
                mode == other.mode &&
                seekerVersion == other.seekerVersion &&
                settableModesMask == other.settableModesMask &&
                enabledModesMask == other.enabledModesMask &&
                reserved.contentEquals(other.reserved)

        override fun hashCode(): Int {
            var result = mode.hashCode()
            result = 31 * result + seekerVersion
            result = 31 * result + settableModesMask
            result = 31 * result + enabledModesMask
            result = 31 * result + reserved.contentHashCode()
            return result
        }
    }

    /**
     * Provider -> Seeker, periodic/on-change status report. [settableToggles]
     * is confirmed as a dock-state indicator: `0x00` when both earbuds are
     * seated in the case, `0xe8` otherwise (DECISIONS.md ADR-024).
     * [currentModeBit] may not match any known [AncMode] one-hot bit; callers
     * should treat a null [currentMode] as "unrecognized," never crash.
     */
    data class Notify(
        val version: Int,
        val uiToggles: Int,
        val settableToggles: Int,
        val currentModeBit: Int,
    ) : AncFrame() {
        val currentMode: AncMode? get() = AncMode.fromWireBit(currentModeBit)
    }

    /** Response to a [Set], per PROTOCOL.md §2.1's Message Stream ACK shape. */
    data class Ack(val echoedGroup: Int, val echoedCode: Int, val data: ByteArray) : AncFrame() {
        override fun equals(other: Any?): Boolean =
            other is Ack &&
                echoedGroup == other.echoedGroup &&
                echoedCode == other.echoedCode &&
                data.contentEquals(other.data)

        override fun hashCode(): Int {
            var result = echoedGroup
            result = 31 * result + echoedCode
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
}
