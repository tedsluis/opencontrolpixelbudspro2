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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.ThrowingSupplier
import java.time.Duration
import kotlin.random.Random

/**
 * Length fields the Buds never send (`ai-sessions/0068` `A68-APP-01`, fixed in `ai-sessions/0069`): a length-delimited protobuf field whose
 * declared length is near the top of its integer range made `next + len` overflow, pass the bounds check, and then throw
 * (`copyOfRange`) or loop without end — inside a frame whose pw_hdlc CRC is valid, so nothing before the reader rejects it. An inbound
 * frame is untrusted input (AGENTS.md §11).
 *
 * **The oversized-length byte sequences here are synthetic** — no capture holds such a frame. Per AGENTS.md §11 they are supplementary
 * structural tests next to real fixtures: each sits inside a real envelope, and the structured fuzz at the end mutates real captured frames
 * (`CAP-036`, `CAP-059`, `CAP-061`, `CAP-062`, `CAP-063`) and re-seals them with a valid CRC.
 */
class OversizedLengthTest {
    /** Synthetic: field 1, length-delimited, declared length `0x7fffffff` — an Int that overflows when the offset is added. */
    private val intMaxLength = hex("0affffffff07")

    /** Synthetic: an RpcPacket whose payload field (5) declares the length `0x7fffffffffffffff` (Long.MAX_VALUE). */
    private val longMaxLength = hex("2affffffffffffffff7f")

    /** Synthetic: field 1 with the declared length `0x7ffffffffffffff6` — offset + length wraps to the field's own start. */
    private val wrappingLength = hex("0af6ffffffffffffff7f")

    /** Synthetic: DLCI 0x08 `Group 0x0e Code 0x01`, 6 value bytes = [intMaxLength]. */
    private val gsndFrame = hex("0e0100060affffffff07")

    private fun sealed(packet: RpcPacket): ByteArray = Hdlc.encode(Cap061.CHANNEL_21_RESPONSE_ADDRESS, PW_HDLC_CONTROL_UI, PwRpc.encode(packet))

    private class Seen {
        val malformed = ArrayList<ByteArray>()
        var unidentified = 0
    }

    private fun CodecRouter.feedCounting(channel: Int, bytes: ByteArray, seen: Seen): List<RoutedFrame> =
        feed(channel, bytes, 0L, onUnidentified = { seen.unidentified++ }, onMalformed = { seen.malformed += it })

    @Test
    @DisplayName("synthetic: Proto.fields refuses a length that overflows the offset (was: IllegalArgumentException from copyOfRange)")
    fun `an Int-range length is refused by the settings and runtime-info readers`() {
        assertNull(Proto.fields(intMaxLength))
        assertNull(SettingsCodec.decode(intMaxLength))
        assertNull(RuntimeInfoDecoder.decode(intMaxLength))
        assertTrue(SoftwareInfo.entries(intMaxLength).isEmpty())
    }

    @Test
    @DisplayName("synthetic: PwRpc.decode refuses a Long-range length (was: IllegalArgumentException) and one that wraps (was: an endless loop)")
    fun `a Long-range length is MalformedFrame, promptly`() {
        for (bytes in listOf(longMaxLength, wrappingLength)) {
            val result = assertTimeoutPreemptively(Duration.ofSeconds(2), ThrowingSupplier { PwRpc.decode(bytes) })
            assertInstanceOf(BudsResult.Failure::class.java, result)
            assertInstanceOf(BudsError.MalformedFrame::class.java, (result as BudsResult.Failure).error)
        }
    }

    @Test
    @DisplayName("synthetic payloads in CRC-valid frames: the router reports them and keeps decoding (runtime info, ReadSetting, announcement, DLCI 0x08)")
    fun `the router survives an oversized length in each reader`() {
        val maestro = listOf(
            RpcPacket(PwRpc.TYPE_SERVER_STREAM, 21, Maestro.SERVICE_ID, Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO, intMaxLength),
            RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_READ_SETTING, intMaxLength),
            RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_GET_SOFTWARE_INFO, intMaxLength, callId = PwRpc.CALL_ID_UNSOLICITED),
        )
        for (packet in maestro) {
            val router = CodecRouter()
            val seen = Seen()
            assertTimeoutPreemptively(Duration.ofSeconds(2)) { router.feedCounting(Dlci.MAESTRO, sealed(packet), seen) }
            // The stream is not poisoned: a real frame right after it still decodes (`CAP-036` frame 1447, ReadSetting field 2).
            val after = router.feedCounting(Dlci.MAESTRO, hex(Settings036.READ_2_RESP), seen)
            assertEquals(1, after.size, "method ${packet.methodId}")
            assertInstanceOf(RoutedFrame.Setting::class.java, after.single())
        }
        // A Maestro frame whose RpcPacket itself carries the Long-range lengths (the frame's CRC is valid).
        for (rpc in listOf(longMaxLength, wrappingLength)) {
            val router = CodecRouter()
            val seen = Seen()
            val frame = Hdlc.encode(Cap061.CHANNEL_21_RESPONSE_ADDRESS, PW_HDLC_CONTROL_UI, rpc)
            assertTimeoutPreemptively(Duration.ofSeconds(2)) { router.feedCounting(Dlci.MAESTRO, frame, seen) }
            assertEquals(1, seen.unidentified + seen.malformed.size)
        }
        val router = CodecRouter()
        val seen = Seen()
        assertTrue(router.feedCounting(Dlci.GSND_CONTROL, gsndFrame, seen).isEmpty())
        assertEquals(1, seen.unidentified + seen.malformed.size)
        // `CAP-059` frame 1211: the real `0e 01` push still decodes on the same router.
        val real = hex("0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060861100118032001")
        assertInstanceOf(RoutedFrame.CaseBattery::class.java, router.feedCounting(Dlci.GSND_CONTROL, real, seen).single())
    }

    @Test
    @DisplayName("the router's guard: a decoder that throws costs one frame, reported as malformed — never the collector")
    fun `decodeGuarded turns a thrown exception into a malformed report`() {
        val frame = hex("7e0102037e")
        val reported = ArrayList<ByteArray>()
        val result = decodeGuarded(frame, { reported += it }) { error("a decoder bug") }
        assertNull(result)
        assertEquals(1, reported.size)
        assertTrue(reported.single().contentEquals(frame))
        assertEquals(7, decodeGuarded(frame, { reported += it }) { 7 })
        assertEquals(1, reported.size)
    }

    @Test
    @DisplayName("structured fuzz: real frames with mutated payload bytes and injected length varints, re-sealed with a valid CRC, never throw or hang")
    fun `mutated real frames with a valid CRC never throw`() {
        val random = Random(6901)
        // Real pw_hdlc frames: `CAP-062` 2782 (runtime info), `CAP-063` 2723 (runtime info, both charging), `CAP-036` 1447/1459/1525
        // (ReadSetting answers for fields 2, 7 and 16) and `CAP-061` 1508 (the announcement).
        val realFrames = listOf(
            hex(Cap062.STREAM_NONE_2782),
            hex(Cap063.STREAM_BOTH_CHARGING_2723),
            hex(Settings036.READ_2_RESP),
            hex(Settings036.READ_7_RESP),
            hex(Settings036.READ_16_RESP),
            Cap061.announcementFrame(),
        )
        val lengths = listOf("ffffffff07", "ffffffff0f", "ffffffffffffffff7f", "f6ffffffffffffff7f", "ffffffffffffffffff01", "8080808010").map(::hex)
        val router = CodecRouter()
        val seen = Seen()
        assertTimeoutPreemptively(Duration.ofSeconds(60)) {
            for (frame in realFrames) {
                val decoded = (Hdlc.decode(frame) as BudsResult.Success).value
                repeat(1_500) {
                    val payload = decoded.payload.copyOf()
                    when (random.nextInt(3)) {
                        0 -> repeat(1 + random.nextInt(3)) { payload[random.nextInt(payload.size)] = random.nextInt(256).toByte() }
                        1 -> { // overwrite a run with an oversized length varint
                            val length = lengths.random(random)
                            val at = random.nextInt(payload.size)
                            for (k in length.indices) if (at + k < payload.size) payload[at + k] = length[k]
                        }
                        else -> { // a length-delimited tag followed by an oversized length, at a random position
                            val at = random.nextInt(payload.size)
                            val injected = byteArrayOf(((1 + random.nextInt(15)) shl 3 or 2).toByte()) + lengths.random(random)
                            for (k in injected.indices) if (at + k < payload.size) payload[at + k] = injected[k]
                        }
                    }
                    router.feedCounting(Dlci.MAESTRO, Hdlc.encode(decoded.address, decoded.control, payload), seen)
                }
            }
            // The same on the two [Group][Code][Len][Value] channels: `CAP-059` 1211 (`0e 01`) with its value bytes mutated, header intact.
            val gsnd = hex("0e0100230a210a03616c6c121a0a060864100118010a060864100118020a060861100118032001")
            repeat(3_000) {
                val frame = gsnd.copyOf()
                val length = lengths.random(random)
                val at = 4 + random.nextInt(frame.size - 4)
                for (k in length.indices) if (at + k < frame.size) frame[at + k] = length[k]
                router.feedCounting(Dlci.GSND_CONTROL, frame, seen)
            }
        }
        // Nothing was poisoned: a real frame still decodes on each channel afterwards.
        assertInstanceOf(RoutedFrame.Setting::class.java, router.feedCounting(Dlci.MAESTRO, hex(Settings036.READ_2_RESP), seen).single())
        assertTrue(seen.unidentified + seen.malformed.size > 0)
    }
}
