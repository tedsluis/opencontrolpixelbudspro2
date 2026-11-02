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

import io.github.tedsluis.opencontrolpixelbuds.domain.ComponentSerial

/**
 * Reads the component serial numbers out of the Buds' `GetHardwareInfo` answer (`maestro_pw.Maestro`, DECISIONS.md ADR-058, `ai-sessions/0082`): the
 * payload is `1:<device type> 2:<sku> 5:<hw version> 6:<hw version> 7:{1:<string> 2:<string> 3:<string>} 8:{…}` — `CAP-036` frame 1423 (channel 21),
 * `CAP-024` frame 832 (channel 19); 138 answers in 54 logs carry the same three length-14 strings in fields 7.1, 7.2, 7.3. Only field 7 is read; fields 1, 2,
 * 5, 6 and 8 are not interpreted (ADR-058 item 3). Which component an index is — 1 = Case, 2 = Right bud, 3 = Left bud — is the official app's reading by
 * position (`PROTOCOL.md` §6, 2026-10-09); the labels are `:ui`'s. Never throws; a string that is not printable ASCII of 1–64 characters, a repeated index
 * or a payload of another shape is left out — an answer without any readable serial is an empty list (the caller reports `UnreadableAnswer`).
 */
internal object HardwareInfo {
    private const val FIELD_SERIALS = 7
    private const val MAX_INDEX = 3
    private const val MAX_LENGTH = 64

    fun serials(payload: ByteArray): List<ComponentSerial> {
        val top = Proto.fields(payload) ?: return emptyList()
        val group = top.firstOrNull { it.number == FIELD_SERIALS && it.bytes != null }?.bytes ?: return emptyList()
        val fields = Proto.fields(group) ?: return emptyList()
        val out = ArrayList<ComponentSerial>(MAX_INDEX)
        for (f in fields) {
            if (f.number !in 1..MAX_INDEX || out.any { it.index == f.number }) continue
            val bytes = f.bytes ?: continue
            if (bytes.isEmpty() || bytes.size > MAX_LENGTH) continue
            val text = bytes.toString(Charsets.US_ASCII)
            if (text.all { it.code in 0x21..0x7E }) out += ComponentSerial(f.number, text)
        }
        return out.sortedBy { it.index }
    }

    private const val FIELD_DEVICE_TYPE = 1
    private const val MAX_DEVICE_TYPE = 1_000

    /**
     * Field 1, the device type the official app chooses its settings schema by (`REVERSE_ENGINEERING.md`, 2026-10-06 Update: 1 PRESTO, 2 STRETTO,
     * 4/5 ALLEGRO, 6 LARGO = the Pro 2, 9 AG12AG22). `null` when absent or not a small varint. Never throws.
     */
    fun deviceType(payload: ByteArray): Int? =
        Proto.fields(payload)?.firstOrNull { it.number == FIELD_DEVICE_TYPE && it.bytes == null }?.varint?.takeIf { it in 0..MAX_DEVICE_TYPE }

    /**
     * A log line of the answer's shape for the Gen 1 diagnostics: each top-level field's number with its varint value, or the length of a
     * length-delimited field. Field 7 (the serials) is shown as a count only: serials are device identifiers (`AGENTS.md` §9). Never throws.
     */
    fun shape(payload: ByteArray): String {
        val top = Proto.fields(payload) ?: return "not a protobuf message (${payload.size} bytes)"
        return top.joinToString(" ") { f ->
            when {
                f.number == FIELD_SERIALS -> "${f.number}=<${serials(payload).size} serials>"
                f.varint != null -> "${f.number}=${f.varint}"
                f.bytes != null -> "${f.number}=<${f.bytes.size} bytes>"
                else -> "${f.number}=<fixed>"
            }
        }
    }
}
