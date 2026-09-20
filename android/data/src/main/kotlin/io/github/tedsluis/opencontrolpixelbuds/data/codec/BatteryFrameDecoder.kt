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

import io.github.tedsluis.opencontrolpixelbuds.data.codec.BatteryMessageStream.CODE_BATTERY_UPDATED
import io.github.tedsluis.opencontrolpixelbuds.data.codec.BatteryMessageStream.DATA_LENGTH
import io.github.tedsluis.opencontrolpixelbuds.data.codec.BatteryMessageStream.GROUP
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult

/**
 * Parses one already-delimited DLCI 0x04 frame (`[Group][Code][Length:2BE][Data]`, PROTOCOL.md
 * §2.1) as a Fast Pair "Battery updated" message (DECISIONS.md ADR-031, unblocked by ADR-033).
 *
 * **Scope is what ADR-033 (and its 2026-09-20 update) accepted:** each of `b1`/`b2` is `0bSVVVVVVV` —
 * bit 7 = charging, the low 7 bits the percentage; a level `V` in `0..100` is a [BatteryLevel.Known]
 * with `isCharging = S`, and `V = 0x7F` (or any other `V > 100`) is [BatteryLevel.Unavailable]
 * (AGENTS.md §5: never guess a percentage). `b3 = 0xff` is "unknown" and no other `b3` value is decoded as the
 * Case battery (not covered by the acceptance), so the Case is always [BatteryLevel.Unavailable].
 * Real frames: `e4 e4` = 100 % charging both, `dd dd` = 93 % charging both, `e4 64` = Left charging 100 %,
 * Right 100 % not charging (maintainer's logs, 2026-09-19; the on-screen values were not recorded).
 *
 * Structural failure is [BudsError.MalformedFrame], never thrown (AGENTS.md §11); a well-formed
 * frame that is simply not this message (any other Group/Code, or a different payload width) is
 * also a Failure so [CodecRouter] can offer it to the next decoder or surface it as unidentified.
 *
 * Fixtures (`BatteryCodecTest`): `CAP-009` frame 1044's `b1=96, b2=93` and the 221→228 charging sequence
 * (frames 26852…28563), plus real 2026-09-19 frames from the maintainer's own device (`64 64 ff`, `60 5f ff`,
 * `e4 e4 ff`, `dd dd ff`, `e4 64 ff`).
 */
object BatteryFrameDecoder {

    fun decode(bytes: ByteArray): BudsResult<BatteryFrame> {
        if (bytes.size < 4) return BudsResult.Failure(BudsError.MalformedFrame(bytes))

        val group = bytes[0].toInt() and 0xFF
        val code = bytes[1].toInt() and 0xFF
        val declaredLength = ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
        val data = bytes.copyOfRange(4, bytes.size)

        if (data.size != declaredLength) return BudsResult.Failure(BudsError.MalformedFrame(bytes))
        if (group != GROUP || code != CODE_BATTERY_UPDATED || data.size != DATA_LENGTH) {
            return BudsResult.Failure(BudsError.MalformedFrame(bytes))
        }

        return BudsResult.Success(BatteryFrame(left = levelOf(data[0]), right = levelOf(data[1])))
    }

    private fun levelOf(raw: Byte): BatteryLevel {
        val value = raw.toInt() and 0xFF
        val percent = value and 0x7F
        val charging = value and 0x80 != 0
        return if (percent in 0..100) BatteryLevel.Known(percent = percent, isCharging = charging) else BatteryLevel.Unavailable
    }
}
