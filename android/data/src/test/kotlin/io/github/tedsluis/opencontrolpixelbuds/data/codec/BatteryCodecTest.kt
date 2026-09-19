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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Random

/**
 * DLCI 0x04 "Battery updated" decoder (DECISIONS.md ADR-031 identity, ADR-033 unblock — percentage
 * regime only). Pure byte-array in/out, no Bluetooth (AGENTS.md §11).
 */
class BatteryCodecTest {

    private fun decode(dataHex: String): BatteryFrame =
        (BatteryFrameDecoder.decode(hex("03030003$dataHex")) as BudsResult.Success).value

    @Test
    @DisplayName("CAP-009 frame 1044 (ADR-031's evidence): Left 96 %, Right 93 %")
    fun `decodes the CAP-009 first frame`() {
        val frame = decode("605dff") // b1=96, b2=93, b3 unread
        assertEquals(BatteryLevel.Known(96, null), frame.left)
        assertEquals(BatteryLevel.Known(93, null), frame.right)
    }

    @Test
    @DisplayName("real 2026-09-19 frames from the maintainer's device: 100/100 and 96/95, charging state never fabricated")
    fun `decodes real frames from the maintainer's device`() {
        assertEquals(BatteryFrame(BatteryLevel.Known(100, null), BatteryLevel.Known(100, null)), decode("6464ff"))
        assertEquals(BatteryFrame(BatteryLevel.Known(96, null), BatteryLevel.Known(95, null)), decode("605fff"))
    }

    @Test
    @DisplayName("the charging regime (bit 7 set: 0xe4, 0xdd, ...) is NOT interpreted — reads unavailable (ADR-033)")
    fun `does not interpret bytes above 100`() {
        // 0xe4 / 0xdd are real frames seen while the earbuds sit charging in the case. Reading bit 7 as a
        // charging flag is a proposal awaiting sign-off, so today they must be "unavailable".
        assertEquals(BatteryFrame(BatteryLevel.Unavailable, BatteryLevel.Unavailable), decode("e4e4ff"))
        assertEquals(BatteryFrame(BatteryLevel.Unavailable, BatteryLevel.Unavailable), decode("dddd" + "ff"))
        // One earbud in the case, one not (real: `e4 64`): each byte is judged on its own.
        assertEquals(BatteryFrame(BatteryLevel.Unavailable, BatteryLevel.Known(100, null)), decode("e464ff"))
    }

    @Test
    @DisplayName("boundaries: 0 and 100 are percentages; 101, 0x7f (unknown), 0x80 and 0xff are not")
    fun `percentage boundaries`() {
        assertEquals(BatteryLevel.Known(0, null), decode("0000ff").left)
        assertEquals(BatteryLevel.Known(100, null), decode("6400ff").left)
        for (raw in listOf("65", "7f", "80", "ff")) {
            assertEquals(BatteryLevel.Unavailable, decode(raw + "00" + "ff").left, "0x$raw must not be a percentage")
        }
    }

    @Test
    @DisplayName("the third byte (Case) is never surfaced")
    fun `third byte is not interpreted`() {
        // Whatever b3 is, only left/right exist on the decoded frame — the Case has no field to leak into.
        assertEquals(decode("6464ff"), decode("646400"))
    }

    @Test
    @DisplayName("a different Group/Code, payload width, or a length mismatch is a Failure, never a crash")
    fun `rejects everything that is not this message`() {
        val notBattery = listOf(
            hex("0734000c") + ByteArray(12) { 1 },   // SASS, real group/code
            hex("03030002" + "6464"),                // right message, wrong width
            hex("03030004" + "6464ff00"),            // right message, too wide
            hex("03030003" + "6464"),                // length says 3, only 2 present
            hex("030300"),                           // shorter than a header
            byteArrayOf(),
        )
        for (bytes in notBattery) {
            assertInstanceOf(BudsResult.Failure::class.java, BatteryFrameDecoder.decode(bytes))
        }
        assertInstanceOf(
            BudsError.MalformedFrame::class.java,
            (BatteryFrameDecoder.decode(hex("03030002" + "6464")) as BudsResult.Failure).error,
        )
    }

    @Test
    @DisplayName("fuzz: random and mutated byte sequences never throw (AGENTS.md §11)")
    fun `never throws on arbitrary input`() {
        val random = Random(33)
        repeat(5_000) {
            val bytes = ByteArray(random.nextInt(12)).also(random::nextBytes)
            BatteryFrameDecoder.decode(bytes) // must return Success or Failure, never throw
        }
        val valid = hex("03030003" + "6464ff")
        repeat(2_000) {
            val mutated = valid.copyOf().also { it[random.nextInt(it.size)] = random.nextInt(256).toByte() }
            BatteryFrameDecoder.decode(mutated)
        }
    }
}
