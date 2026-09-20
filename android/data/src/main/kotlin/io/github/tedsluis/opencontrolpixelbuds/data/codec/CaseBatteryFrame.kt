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

import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult

/**
 * DLCI 0x08 ("GSND CONTROL") private envelope `[Group:1][Code:1][Length:2 BE][Value]` (PROTOCOL.md §2.3), `Group 0x0e Code 0x01`:
 * the Buds' per-earbud + case battery push (DECISIONS.md ADR-014 — entry index 3 = Case; ADR-035 — receive-only claim).
 */
object GsndMessageStream {
    const val GROUP_BATTERY: Int = 0x0E
    const val CODE_BATTERY: Int = 0x01

    /** Entry index of the Case (ADR-014: 1 = Left, 2 = Right, 3 = Case). */
    const val CASE_INDEX: Int = 3
}

/** The Case reading of one `0e 01` push — [BatteryLevel.Known] (with `isStale` when the Buds did not mark it fresh) or unavailable. */
data class CaseBatteryFrame(val case: BatteryLevel)

/**
 * Decodes one already-delimited DLCI 0x08 `0e 01` frame and returns **only** the Case (index 3) — Left/Right come from the Message
 * Stream (ADR-033). Any structural failure is [BudsError.MalformedFrame], never thrown (`AGENTS.md` §11: the bytes are
 * environment-controlled). Real fixtures (`LOGS-001`, frames 1211/2863/2928, `CaseBatteryFrameDecoderTest`):
 * `0e 01 00 23 0a 21 0a 03 61 6c 6c 12 1a 0a 06 08 64 10 01 18 01 0a 06 08 64 10 01 18 02 0a 06 08 61 10 01 18 03 20 01` = Left 100,
 * Right 100, Case 0x61 = 97 (flag `10 01` present = fresh).
 *
 * Reading rule (ADR-035): value `0..100` → percentage; the entry's flag (field 2) equal to `1` marks it fresh, otherwise
 * `isStale = true` ("last seen"); any other value → [BatteryLevel.Unavailable]; no index-3 entry → malformed.
 *
 * // TODO(verify): the flag's meaning ("fresh") is ADR-014's 🟡 HYPOTHESIS, consistent with `LOGS-001` (present while the buds sat in
 * // the case, absent afterwards) but with no ground-truth Case level on the film.
 */
object CaseBatteryFrameDecoder {

    fun decode(bytes: ByteArray): BudsResult<CaseBatteryFrame> {
        fun bad() = BudsResult.Failure(BudsError.MalformedFrame(bytes))
        if (bytes.size < 4) return bad()
        val group = bytes[0].toInt() and 0xFF
        val code = bytes[1].toInt() and 0xFF
        val length = ((bytes[2].toInt() and 0xFF) shl 8) or (bytes[3].toInt() and 0xFF)
        if (group != GsndMessageStream.GROUP_BATTERY || code != GsndMessageStream.CODE_BATTERY) return bad()
        if (bytes.size - 4 != length) return bad()
        val value = bytes.copyOfRange(4, bytes.size)

        val top = Proto.fields(value) ?: return bad()
        val inner = top.firstOrNull { it.number == 1 && it.bytes != null }?.bytes ?: return bad()
        val innerFields = Proto.fields(inner) ?: return bad()
        val group2 = innerFields.firstOrNull { it.number == 2 && it.bytes != null }?.bytes ?: return bad()
        val entries = Proto.fields(group2) ?: return bad()
        for (entry in entries) {
            val entryBytes = entry.bytes ?: continue
            if (entry.number != 1) continue
            val fields = Proto.fields(entryBytes) ?: return bad()
            val index = fields.firstOrNull { it.number == 3 && it.bytes == null }?.varint
            if (index != GsndMessageStream.CASE_INDEX) continue
            val percent = fields.firstOrNull { it.number == 1 && it.bytes == null }?.varint
            val fresh = fields.firstOrNull { it.number == 2 && it.bytes == null }?.varint == 1
            val level = if (percent != null && percent in 0..100) {
                BatteryLevel.Known(percent, isCharging = null, isStale = !fresh)
            } else {
                BatteryLevel.Unavailable
            }
            return BudsResult.Success(CaseBatteryFrame(level))
        }
        return bad()
    }
}

/**
 * Minimal protobuf wire reader shared by the small read-only decoders: varint (wire type 0) and length-delimited (wire type 2)
 * fields; anything else, or any truncation, makes the whole message unreadable (`null`).
 */
internal object Proto {
    class Field(val number: Int, val varint: Int?, val bytes: ByteArray?)

    fun fields(data: ByteArray): List<Field>? {
        val out = ArrayList<Field>()
        var i = 0
        while (i < data.size) {
            val (tag, afterTag) = Varint.decode(data, i) ?: return null
            i = afterTag
            val number = tag ushr 3
            if (number == 0) return null
            when (tag and 0x7) {
                0 -> {
                    val (v, next) = Varint.decode(data, i) ?: return null
                    out += Field(number, v, null)
                    i = next
                }
                2 -> {
                    val (len, next) = Varint.decode(data, i) ?: return null
                    if (len < 0 || next + len > data.size) return null
                    out += Field(number, null, data.copyOfRange(next, next + len))
                    i = next + len
                }
                else -> return null
            }
        }
        return out
    }
}
