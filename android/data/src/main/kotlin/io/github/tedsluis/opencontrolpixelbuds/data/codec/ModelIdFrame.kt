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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult

/**
 * Fast Pair Device Information (Group `0x03`) Code `0x01` = **Model ID**, 3 bytes (PROTOCOL.md §0.1, 🟢 FACT: official
 * `deviceinformation` spec + wire). The Buds send it right after every DLCI 0x04 open, e.g. `CAP-059` frame 1049 bundles
 * `03 01 00 03 da 2d b1`; the value is `da 2d b1` in every capture checked (`CAP-001`, `CAP-002`, `CAP-010`, `CAP-036`, `CAP-059`).
 * Read only for the Safe-Mode write gate (DECISIONS.md ADR-042) — it is not a device identifier of the user's unit (it is the model's).
 */
object DeviceInformation {
    const val GROUP: Int = 0x03
    const val CODE_MODEL_ID: Int = 0x01

    /** The Pixel Buds Pro 2's Fast Pair Model ID as seen on the wire (PROTOCOL.md §0.1). */
    const val PIXEL_BUDS_PRO_2_MODEL_ID: String = "da2db1"
}

/** A decoded Model ID, as lower-case hex (e.g. `da2db1`). */
data class ModelIdFrame(val modelIdHex: String)

object ModelIdFrameDecoder {
    fun decode(bytes: ByteArray): BudsResult<ModelIdFrame> {
        fun bad() = BudsResult.Failure(BudsError.MalformedFrame(bytes))
        if (bytes.size != 7) return bad()
        val group = bytes[0].toInt() and 0xFF
        val code = bytes[1].toInt() and 0xFF
        val length = ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
        if (group != DeviceInformation.GROUP || code != DeviceInformation.CODE_MODEL_ID || length != 3) return bad()
        return BudsResult.Success(ModelIdFrame(bytes.copyOfRange(4, 7).joinToString("") { "%02x".format(it) }))
    }
}
