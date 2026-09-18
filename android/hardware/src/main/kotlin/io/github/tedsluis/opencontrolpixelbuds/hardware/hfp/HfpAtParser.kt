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
package io.github.tedsluis.opencontrolpixelbuds.hardware.hfp

/**
 * Pure-string parsing for the HFP AT-command battery mechanisms confirmed in
 * PROTOCOL.md §4.3 Option C (DECISIONS.md ADR-015/ADR-023) — standard
 * Bluetooth HF Indicator/`AT+CIND` traffic, **not** Apple's
 * `AT+IPHONEACCEV`/`AT+XAPL` vendor commands (AGENTS.md §5 is explicit about
 * this distinction). No Android framework dependency itself — deliberately
 * pure Kotlin so it is unit-testable against fixed capture-derived strings
 * without a real `BluetoothHeadset` (AGENTS.md §11).
 *
 * Lives in `:hardware`, not `:data`, despite being pure parsing logic like
 * every other codec in this project: unlike DLCI 0x02/0x04's `FrameEncoder`/
 * `FrameDecoder` pairs (which `:data`'s `CodecRouter` drives from bytes
 * `:hardware`'s `BudsTransport` merely forwards), this parser is driven
 * directly by `:hardware`'s own `BluetoothHeadset`/`BluetoothProfile`
 * consumption code (`HfpBatteryReader`) — putting it in `:data` would require
 * `:hardware` to depend on `:data`, which is backwards
 * (ARCHITECTURE.md §2's dependency direction is `:data → :hardware`, never
 * the reverse).
 */
object HfpAtParser {

    private val BIEV_BATTERY = Regex("""BIEV\s*=\s*2\s*,\s*(\d{1,3})""")
    private val CIEV = Regex("""\+CIEV:\s*(\d+)\s*,\s*(\d+)""")
    private val CIND_TEST_ENTRY = Regex("""\(\s*"([a-zA-Z]+)"\s*,\s*\([^)]*\)\s*\)""")

    /**
     * `AT+BIEV=2,<0-100>` — HF Indicator #2 is the Bluetooth-SIG-assigned
     * Battery Level indicator (a fixed assigned number, not vendor/session
     * specific, unlike `AT+CIND`'s indicator ordering below). Returns `null`
     * for anything that isn't this exact indicator or an out-of-range value —
     * never clamps a malformed reading into a plausible-looking one.
     */
    fun parseBiev(raw: String): Int? {
        val value = BIEV_BATTERY.find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: return null
        return value.takeIf { it in 0..100 }
    }

    /**
     * `AT+CIND=?`'s test response defines this *device's own* indicator name
     * order, e.g. `+CIND: ("call",(0,1)),("battchg",(0-5))` ->
     * `["call", "battchg"]`. DECISIONS.md ADR-015 confirms `battchg`'s wire
     * *index* is not a fixed constant across devices/firmware — callers must
     * resolve it from this list on each session, never hardcode a position.
     */
    fun parseCindIndicatorOrder(raw: String): List<String> =
        CIND_TEST_ENTRY.findAll(raw).map { it.groupValues[1].lowercase() }.toList()

    /**
     * `AT+CIND?`'s response values, e.g. `+CIND: 1,0,1,4,0,3,0` ->
     * `[1,0,1,4,0,3,0]`, positional against [parseCindIndicatorOrder]'s order.
     * Returns `null` on anything that doesn't parse as a clean comma-separated
     * integer list.
     */
    fun parseCindValues(raw: String): List<Int>? {
        val body = raw.substringAfter("+CIND:", missingDelimiterValue = "").trim()
        if (body.isEmpty()) return null
        val parts = body.split(",")
        val values = ArrayList<Int>(parts.size)
        for (part in parts) {
            values += part.trim().toIntOrNull() ?: return null
        }
        return values
    }

    /**
     * An unsolicited `+CIEV: <index>,<value>` indicator-event push, e.g. the
     * single mid-session `battchg` update `CAP-008-FINDINGS.md` §9 observed.
     * Returns `(index, value)`, or `null` if malformed.
     */
    fun parseCiev(raw: String): Pair<Int, Int>? {
        val match = CIEV.find(raw) ?: return null
        val index = match.groupValues[1].toIntOrNull() ?: return null
        val value = match.groupValues[2].toIntOrNull() ?: return null
        return index to value
    }
}
