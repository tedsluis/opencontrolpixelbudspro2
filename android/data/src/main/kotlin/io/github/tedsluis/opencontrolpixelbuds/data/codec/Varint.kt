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

/** Standard protobuf/LEB128 base-128 varint encode/decode, shared by any DLCI
 * 0x02 payload codec that needs to read/write a protobuf wire-format tag. */
internal object Varint {
    fun encode(value: Int): ByteArray {
        var v = value
        val out = ArrayList<Byte>(4)
        do {
            var b = v and 0x7F
            v = v ushr 7
            if (v != 0) b = b or 0x80
            out.add(b.toByte())
        } while (v != 0)
        return out.toByteArray()
    }

    /** Returns `(value, indexAfterVarint)`, or `null` on a truncated/oversized varint. */
    fun decode(data: ByteArray, start: Int): Pair<Int, Int>? {
        var value = 0
        var shift = 0
        var i = start
        while (true) {
            if (i >= data.size || shift >= 32) return null
            val b = data[i].toInt() and 0xFF
            value = value or ((b and 0x7F) shl shift)
            i++
            if (b and 0x80 == 0) return value to i
            shift += 7
        }
    }
}
