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

/**
 * Reads the firmware version strings out of the Buds' unsolicited `GetSoftwareInfo` push (`maestro_pw.Maestro`, DECISIONS.md ADR-034):
 * the payload is `4:{1:{1:<string> 2:<string>} 2:{…} 3:{…}}` where, in every capture so far, field 2 of each entry is the firmware
 * string (`release_5.203`, `CAP-059` frame 1431: `72 65 6c 65 61 73 65 5f 35 2e 32 30 33`). Field 1 of each entry is an identifier
 * (a serial-number candidate, `PROTOCOL.md` §6) and is **never read out or logged** (`AGENTS.md` §9). Returns the *distinct* field-2
 * strings in order; empty when the payload is not of this shape — never throws.
 *
 * // TODO(verify): which of the three entries is which component (Left/Right/Case) is not established, so the strings are
 * // shown de-duplicated and unlabelled.
 */
internal object SoftwareInfo {
    private const val OUTER_FIELD = 4

    fun firmwareStrings(payload: ByteArray): List<String> {
        val outer = Proto.fields(payload)?.firstOrNull { it.number == OUTER_FIELD && it.bytes != null }?.bytes ?: return emptyList()
        val entries = Proto.fields(outer) ?: return emptyList()
        val strings = LinkedHashSet<String>()
        for (entry in entries) {
            val entryBytes = entry.bytes ?: continue
            val inner = Proto.fields(entryBytes) ?: continue
            val firmware = inner.firstOrNull { it.number == 2 && it.bytes != null }?.bytes ?: continue
            val text = firmware.toString(Charsets.US_ASCII)
            if (text.isNotBlank() && text.length <= 64 && text.all { it.code in 0x20..0x7E }) strings += text
        }
        return strings.toList()
    }
}
