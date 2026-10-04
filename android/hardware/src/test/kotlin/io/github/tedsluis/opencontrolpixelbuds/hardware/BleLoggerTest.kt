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
package io.github.tedsluis.opencontrolpixelbuds.hardware

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** The debug-export ring buffer (`CAP-063`: a 1,000-line buffer wrapped mid-session and the shared text was cut at 65,536 bytes). */
class BleLoggerTest {
    @AfterEach
    fun tearDown() = BleLogger.clear()

    @Test
    fun `keeps the last RING_BUFFER_CAPACITY lines, oldest dropped first`() {
        BleLogger.clear()
        repeat(BleLogger.RING_BUFFER_CAPACITY + 5) { BleLogger.logConnectionEvent("line $it") }
        val lines = BleLogger.exportLog().lines()
        assertEquals(BleLogger.RING_BUFFER_CAPACITY, lines.size)
        assertTrue(lines.first().endsWith(" line 5"), lines.first())
        assertTrue(lines.last().endsWith(" line ${BleLogger.RING_BUFFER_CAPACITY + 4}"), lines.last())
    }

    @Test
    fun `with Debug mode off no payload byte reaches the export`() {
        // AGENTS.md §9 (`ai-sessions/0069`; the gate had no test — `ai-sessions/0068` A68-APP-06). The frame is `CAP-064` 3440, a NAK: no identifier in it.
        BleLogger.clear()
        val frame = byteArrayOf(0xff.toByte(), 0x02, 0x00, 0x03, 0x02, 0x08, 0x12)
        BleLogger.logHexDump(0x04, frame, debugModeEnabled = false)
        assertEquals("", BleLogger.exportLog())
        BleLogger.logHexDump(0x04, frame, debugModeEnabled = true)
        assertTrue(BleLogger.exportLog().endsWith("DLCI 0x04: ff 02 00 03 02 08 12"), BleLogger.exportLog())
    }

    @Test
    fun `a Debug-mode session far beyond 64 KiB is exported whole`() {
        BleLogger.clear()
        // A real DLCI 0x02 stream frame (CAP-062 frame 4845), as Debug mode logs it: ~150 bytes per line.
        val frame = "7e80a3032a1e180032120a04083c10011204086410021a04086410023a06080110011800080710131dea71de7d5e2590821ee6160866417e"
            .chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        repeat(3_000) { BleLogger.logHexDump(0x02, frame, debugModeEnabled = true) }
        val text = BleLogger.exportLog()
        assertTrue(text.toByteArray().size > 65_536 * 5, "size ${text.toByteArray().size}")
        assertEquals(3_000, text.lines().size)
        assertTrue(text.lines().all { it.endsWith("41 7e") })
    }
}
