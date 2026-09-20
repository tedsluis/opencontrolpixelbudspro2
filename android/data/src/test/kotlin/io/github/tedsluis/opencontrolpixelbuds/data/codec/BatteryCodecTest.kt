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
 * DLCI 0x04 "Battery updated" decoder (DECISIONS.md ADR-031 identity, ADR-033 unblock + its 2026-09-20 charging-flag
 * update: each byte is `0bSVVVVVVV`). Pure byte-array in/out, no Bluetooth (AGENTS.md §11).
 */
class BatteryCodecTest {

    private fun decode(dataHex: String): BatteryFrame =
        (BatteryFrameDecoder.decode(hex("03030003$dataHex")) as BudsResult.Success).value

    @Test
    @DisplayName("CAP-009 frame 1044 (ADR-031's evidence): Left 96 %, Right 93 %, not charging")
    fun `decodes the CAP-009 first frame`() {
        val frame = decode("605dff") // b1=96, b2=93, b3 = 0xff (unknown)
        assertEquals(BatteryLevel.Known(96, false), frame.left)
        assertEquals(BatteryLevel.Known(93, false), frame.right)
    }

    @Test
    @DisplayName("real 2026-09-19 frames from the maintainer's device: not charging (64 64, 60 5f)")
    fun `decodes real frames from the maintainer's device`() {
        assertEquals(BatteryFrame(BatteryLevel.Known(100, false), BatteryLevel.Known(100, false)), decode("6464ff"))
        assertEquals(BatteryFrame(BatteryLevel.Known(96, false), BatteryLevel.Known(95, false)), decode("605fff"))
    }

    @Test
    @DisplayName("bit 7 is the charging flag (accepted 2026-09-20): e4 e4 = 100 % charging, dd dd = 93 % charging (real frames)")
    fun `decodes the charging regime`() {
        assertEquals(BatteryFrame(BatteryLevel.Known(100, true), BatteryLevel.Known(100, true)), decode("e4e4ff"))
        assertEquals(BatteryFrame(BatteryLevel.Known(93, true), BatteryLevel.Known(93, true)), decode("ddddff"))
        // One earbud in the case, one not (real: `e4 64`): each byte is judged on its own.
        assertEquals(BatteryFrame(BatteryLevel.Known(100, true), BatteryLevel.Known(100, false)), decode("e464ff"))
    }

    @Test
    @DisplayName("CAP-009 frames 26852…28563: b1 = 221…228 is 93…100 % charging, exactly Option E's curve incl. the skipped 99")
    fun `reproduces the CAP-009 charging sequence`() {
        val b1 = listOf(0xdd, 0xde, 0xdf, 0xe0, 0xe1, 0xe2, 0xe4) // frames 26852, 26907, 27020, 27195, 27377, 27581, 28563
        val expected = listOf(93, 94, 95, 96, 97, 98, 100) // DLCI 0x08 Option E for the same earbud (ADR-014)
        for ((raw, percent) in b1.zip(expected)) {
            val frame = decode("%02x58ff".format(raw)) // b2 stayed 0x58 = 88 % not charging
            assertEquals(BatteryLevel.Known(percent, true), frame.left)
            assertEquals(BatteryLevel.Known(88, false), frame.right)
        }
    }

    @Test
    @DisplayName("boundaries: 0 and 100 are percentages; V = 101, 0x7f (unknown) and 0xff (V = 0x7f) are Unavailable")
    fun `percentage boundaries`() {
        assertEquals(BatteryLevel.Known(0, false), decode("0000ff").left)
        assertEquals(BatteryLevel.Known(100, false), decode("6400ff").left)
        assertEquals(BatteryLevel.Known(0, true), decode("8000ff").left)
        assertEquals(BatteryLevel.Known(100, true), decode("e400ff").left)
        for (raw in listOf("65", "7f", "e5", "ff")) { // V = 101, V = 0x7f, V = 101 charging, V = 0x7f charging
            assertEquals(BatteryLevel.Unavailable, decode(raw + "00" + "ff").left, "0x$raw must not be a percentage")
        }
    }

    @Test
    @DisplayName("the Case (b3) is never decoded: 0xff (unknown) and every other value read Unavailable")
    fun `third byte is not interpreted`() {
        for (b3 in listOf("ff", "00", "50", "e4", "7f")) {
            assertEquals(BatteryLevel.Unavailable, decode("6464$b3").case, "b3=0x$b3")
        }
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
