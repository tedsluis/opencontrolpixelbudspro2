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
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * DLCI 0x08 Case battery (`Group 0x0e Code 0x01`, entry index 3; DECISIONS.md ADR-014/ADR-035) and the firmware strings of the
 * Buds' `GetSoftwareInfo` announcement (`ai-sessions/0042`). Fixtures are real bytes from the `CAP-059` HCI log (formerly `LOGS-001`, committed under ADR-037;
 * frame numbers in each test).
 */
class CaseBatteryCodecTest {

    // frame 1211 (17:17:50.895): flag `10 01` present on entry 3 → fresh; Left 100, Right 100, Case 0x61 = 97
    private val fresh = hex("0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060861100118032001")

    // frame 2863 (17:19:26.427, buds out of the case): entry 3 is `0a 04 08 61 18 03` — no flag → last seen
    private val stale = hex("0e0100210a1f0a03616c6c12180a060864100118010a060864100118020a04086118032001")

    // frame 2928: entry 3 without a flag, plus group-level varints `10 01 18 01` after it
    private val staleWithGroupFields = hex("0e0100250a230a03616c6c121c0a060864100118010a060864100118020a0408611803100118012001")

    @Test
    @DisplayName("frame 1211: the Case is 97 % and fresh (flag present)")
    fun `fresh case reading`() {
        val decoded = CaseBatteryFrameDecoder.decode(fresh)
        assertEquals(BatteryLevel.Known(97, isCharging = null, isStale = false), (decoded as BudsResult.Success).value.case)
    }

    @Test
    @DisplayName("frames 2863 and 2928: the same 97 % without the flag is shown as last seen, never as current")
    fun `stale case reading`() {
        for (frame in listOf(stale, staleWithGroupFields)) {
            val decoded = CaseBatteryFrameDecoder.decode(frame)
            assertEquals(BatteryLevel.Known(97, isCharging = null, isStale = true), (decoded as BudsResult.Success).value.case)
        }
    }

    @Test
    @DisplayName("a value outside 0..100 is unavailable, never clamped or guessed (AGENTS.md §5)")
    fun `out of range case value is unavailable`() {
        // entry 3 value 0x7f = 127 (the Fast Pair 'unknown' marker) and 0xc8 = 200 (varint 0xc8 0x01)
        val unknown = hex("0e0100230a210a03616c6c121a0a060864100118010a060864100118020a06087f100118032001")
        assertEquals(BatteryLevel.Unavailable, (CaseBatteryFrameDecoder.decode(unknown) as BudsResult.Success).value.case)
        val over = hex("0e0100240a220a03616c6c121b0a060864100118010a060864100118020a0708c801100118032001")
        assertEquals(BatteryLevel.Unavailable, (CaseBatteryFrameDecoder.decode(over) as BudsResult.Success).value.case)
    }

    @Test
    @DisplayName("only entry index 3 is read: a message without it is malformed, Left/Right are never taken as the Case")
    fun `no case entry is malformed`() {
        val leftRightOnly = hex("0e01001b0a190a03616c6c12120a060864100118010a06086410011802")
        assertInstanceOf(BudsError.MalformedFrame::class.java, (CaseBatteryFrameDecoder.decode(leftRightOnly) as BudsResult.Failure).error)
    }

    @Test
    fun `wrong group or a wrong declared length is malformed`() {
        val otherGroup = fresh.copyOf().also { it[0] = 0x05 }
        val shortLength = fresh.copyOf().also { it[3] = 0x22 }
        for (bad in listOf(otherGroup, shortLength, fresh.copyOfRange(0, 3), fresh.copyOfRange(0, 20))) {
            assertInstanceOf(BudsError.MalformedFrame::class.java, (CaseBatteryFrameDecoder.decode(bad) as BudsResult.Failure).error)
        }
    }

    @Test
    @DisplayName("fuzz (AGENTS.md §11): random, truncated and mutated bytes never throw")
    fun `decoder never throws`() {
        val random = Random(42)
        repeat(3_000) {
            val bytes = ByteArray(random.nextInt(0, 64)).also(random::nextBytes)
            CaseBatteryFrameDecoder.decode(bytes)
            SoftwareInfo.firmwareStrings(bytes)
        }
        val announcement = Cap061.announcementPayload()
        for (cut in announcement.indices) SoftwareInfo.firmwareStrings(announcement.copyOfRange(0, cut))
        repeat(500) {
            val mutated = announcement.copyOf().also { b -> b[random.nextInt(b.size)] = random.nextInt(256).toByte() }
            SoftwareInfo.firmwareStrings(mutated)
        }
        for (cut in fresh.indices) CaseBatteryFrameDecoder.decode(fresh.copyOfRange(0, cut))
        repeat(500) {
            val mutated = fresh.copyOf().also { b -> b[random.nextInt(b.size)] = random.nextInt(256).toByte() }
            CaseBatteryFrameDecoder.decode(mutated)
        }
    }

    // ---- CodecRouter, DLCI 0x08 --------------------------------------------------------------------

    @Test
    fun `DLCI 0x08 routes the Case reading, even when one push arrives in two chunks`() {
        val router = CodecRouter()
        val whole = router.feed(Dlci.GSND_CONTROL, fresh, timestampMillis = 0)
        assertEquals(RoutedFrame.CaseBattery(CaseBatteryFrame(BatteryLevel.Known(97, null, false))), whole.single())
        val split = CodecRouter()
        assertTrue(split.feed(Dlci.GSND_CONTROL, fresh.copyOfRange(0, 9), timestampMillis = 0).isEmpty())
        assertEquals(1, split.feed(Dlci.GSND_CONTROL, fresh.copyOfRange(9, fresh.size), timestampMillis = 0).size)
    }

    @Test
    @DisplayName("every other Group/Code on DLCI 0x08 (frame 1176 `0e 04`, 1166 `04 02`, …) stays an UnidentifiedFrame — ADR-035 decodes the Case only")
    fun `other DLCI 0x08 frames stay unidentified`() {
        val seen = mutableListOf<UnidentifiedFrame>()
        val routed = CodecRouter().feed(Dlci.GSND_CONTROL, hex("0e0400000402" + "0000"), timestampMillis = 7, onUnidentified = { seen += it })
        assertTrue(routed.isEmpty())
        assertEquals(listOf(0x0e to 0x04, 0x04 to 0x02), seen.map { it.group to it.code })
    }

    // ---- firmware strings ----------------------------------------------------------------------------

    private fun lenDelimited(tag: Int, body: ByteArray) = byteArrayOf(tag.toByte(), body.size.toByte()) + body

    private fun softwareInfoEntry(serialLike: String, firmware: String) =
        lenDelimited(0x0a, serialLike.toByteArray()) + lenDelimited(0x12, firmware.toByteArray())

    @Test
    @DisplayName("frame 1431: the announcement carries `release_5.203` three times — shown once; the serial-like field 1 is never read")
    fun `firmware strings are distinct and the identifier is not read`() {
        val entry = softwareInfoEntry("0000000000", "release_5.203") // the real serial-like value is deliberately not a fixture
        val payload = lenDelimited(0x22, lenDelimited(0x0a, entry) + lenDelimited(0x12, entry) + lenDelimited(0x1a, entry))
        assertEquals(listOf("release_5.203"), SoftwareInfo.firmwareStrings(payload))
    }

    @Test
    fun `different firmware per component keeps both, in order`() {
        val a = softwareInfoEntry("1", "release_5.203")
        val b = softwareInfoEntry("2", "release_5.204")
        val payload = lenDelimited(0x22, lenDelimited(0x0a, a) + lenDelimited(0x12, a) + lenDelimited(0x1a, b))
        assertEquals(listOf("release_5.203", "release_5.204"), SoftwareInfo.firmwareStrings(payload))
    }

    @Test
    fun `an unexpected shape yields no firmware`() {
        assertTrue(SoftwareInfo.firmwareStrings(ByteArray(0)).isEmpty())
        assertTrue(SoftwareInfo.firmwareStrings(hex("2202ffff")).isEmpty())
        // a non-printable "firmware" is rejected
        val bad = lenDelimited(0x22, lenDelimited(0x0a, lenDelimited(0x12, byteArrayOf(0x01, 0x02))))
        assertTrue(SoftwareInfo.firmwareStrings(bad).isEmpty())
    }

    // ---- the real announcement (CAP-061 frame 1508, ai-sessions/0046) ------------------------------------------------------

    @Test
    @DisplayName("CAP-061 frame 1508: the real announcement (with its fixed64 field 5) yields release_5.203 — the Safe Mode input")
    fun `the real announcement's firmware is read despite its fixed64 field`() {
        assertEquals(listOf("release_5.203"), SoftwareInfo.firmwareStrings(Cap061.announcementPayload()))
    }

    @Test
    @DisplayName("CAP-061 frame 1508 through the router: MaestroHello(21, [release_5.203])")
    fun `the router puts the real announcement's firmware on the hello`() {
        val routed = CodecRouter().feed(Dlci.MAESTRO, Cap061.announcementFrame(), timestampMillis = 0)
        assertEquals(RoutedFrame.MaestroHello(21, listOf("release_5.203")), routed.single())
    }

    @Test
    fun `fixed64 and fixed32 fields are skipped by size, not treated as unreadable`() {
        // `29 <8 bytes>` (field 5, wire type 1) as in CAP-061 frame 1508, then `35 <4 bytes>` (field 6, wire type 5), then a varint.
        val fields = Proto.fields(hex("2934293fc2f6cbd81a" + "3501020304" + "3805"))
        assertEquals(listOf(5, 6, 7), fields?.map { it.number })
        assertEquals(listOf(null, null, 5), fields?.map { it.varint })
        assertTrue(fields!!.take(2).all { it.bytes == null })
    }

    @Test
    fun `a truncated fixed-width field or a group still makes the message unreadable`() {
        assertEquals(null, Proto.fields(hex("2934293fc2f6cbd8"))) // fixed64 cut to 7 bytes
        assertEquals(null, Proto.fields(hex("35010203"))) // fixed32 cut to 3 bytes
        assertEquals(null, Proto.fields(hex("0b0c"))) // wire type 3 (start group)
    }

    @Test
    fun `the router puts the firmware on the announcement`() {
        val entry = softwareInfoEntry("0000000000", "release_5.203")
        val payload = lenDelimited(0x22, lenDelimited(0x0a, entry))
        val packet = RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_GET_SOFTWARE_INFO, payload, callId = PwRpc.CALL_ID_UNSOLICITED)
        val routed = CodecRouter().feed(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, PwRpc.encode(packet)), timestampMillis = 0)
        assertEquals(RoutedFrame.MaestroHello(21, listOf("release_5.203")), routed.single())
    }
}
