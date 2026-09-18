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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * String fixtures modeled on `CAP-001-FINDINGS.md` §3 / `CAP-009-FINDINGS.md`
 * §1-3's documented AT-command traffic (PROTOCOL.md §4.3 Option C,
 * DECISIONS.md ADR-015) — this project's own confirmed wire vocabulary, not
 * fabricated strings.
 */
class HfpAtParserTest {

    @Test
    fun `parses a real AT+BIEV battery push`() {
        assertEquals(100, HfpAtParser.parseBiev("AT+BIEV=2,100"))
        assertEquals(88, HfpAtParser.parseBiev("AT+BIEV=2,88\r"))
    }

    @Test
    fun `rejects a different HF indicator number`() {
        // HF Indicator #1 is Enhanced Safety, not Battery Level — must not be
        // misread as a battery value just because it shares the AT+BIEV shape.
        assertNull(HfpAtParser.parseBiev("AT+BIEV=1,1"))
    }

    @Test
    fun `rejects an out-of-range battery value rather than clamping it`() {
        assertNull(HfpAtParser.parseBiev("AT+BIEV=2,150"))
    }

    @Test
    fun `parses the CIND indicator name order`() {
        val order = HfpAtParser.parseCindIndicatorOrder(
            "+CIND: (\"call\",(0,1)),(\"callsetup\",(0-3)),(\"battchg\",(0-5))",
        )
        assertEquals(listOf("call", "callsetup", "battchg"), order)
    }

    @Test
    fun `parses CIND response values positionally`() {
        assertEquals(listOf(0, 0, 1, 4, 0, 3, 0), HfpAtParser.parseCindValues("+CIND: 0,0,1,4,0,3,0"))
    }

    @Test
    fun `parses an unsolicited CIEV battchg push`() {
        // CAP-008-FINDINGS.md §9's single mid-session battchg update.
        assertEquals(6 to 2, HfpAtParser.parseCiev("+CIEV: 6,2"))
    }

    @Test
    fun `malformed AT strings never throw, they return null`() {
        assertNull(HfpAtParser.parseBiev(""))
        assertNull(HfpAtParser.parseCindValues("garbage"))
        assertNull(HfpAtParser.parseCiev("+CIEV:"))
    }
}
