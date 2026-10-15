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

import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.random.Random

/**
 * DECISIONS.md ADR-064/065 (`ai-sessions/0091`): the settings-changes subscription and setting 13 against the real frames of [SettingsStream] — the
 * request bytes on both channels, every captured answer and stream packet, value 0, the other fields' stream packets, the unread-field route, and a fuzz
 * over server-stream packets (AGENTS.md §11: an inbound frame is untrusted input).
 */
class SettingsStreamCodecTest {

    /** The whole pw_hdlc frame for a request on the announced channel, as the app puts it on the wire. */
    private fun wire(packet: RpcPacket): String {
        val address = MaestroChannel.forChannel(packet.channelId)!!.requestAddress
        return Hdlc.encode(address, PW_HDLC_CONTROL_UI, PwRpc.encode(packet)).toHex()
    }

    private fun rpc(frameHex: String): RpcPacket =
        (PwRpc.decode((Hdlc.decode(hex(frameHex)) as BudsResult.Success).value.payload) as BudsResult.Success).value

    /** Hdlc → pw_rpc → the router's Maestro classification, the path every inbound frame takes. */
    private fun route(frameHex: String): RoutedFrame? = routeMaestro(rpc(frameHex))

    private fun setting(frameHex: String): RoutedFrame.Setting = assertInstanceOf(RoutedFrame.Setting::class.java, route(frameHex))

    // ---- the two requests: byte-identical to the official app's, whole frame incl. CRC ----

    @Test
    @DisplayName("SubscribeToSettingsChanges: CAP-051 1177 on channel 21 and CAP-056 1451 on channel 19, byte for byte; method 0x2821adf5, empty payload")
    fun `the subscription request on both channels`() {
        assertEquals(SettingsStream.SUBSCRIBE_REQ_CH21_CAP051_1177, wire(Maestro.subscribeToSettingsChangesRequest(21)))
        assertEquals(SettingsStream.SUBSCRIBE_REQ_CH19_CAP056_1451, wire(Maestro.subscribeToSettingsChangesRequest(19)))
        val packet = Maestro.subscribeToSettingsChangesRequest(21)
        assertEquals(PwRpc.TYPE_REQUEST, packet.type)
        assertEquals(0x2821adf5, packet.methodId)
        assertEquals(0, packet.payload.size)
    }

    @Test
    @DisplayName("ReadSetting 4:13: CAP-051 1178 on channel 21 and CAP-056 1450 on channel 19, byte for byte — field 13 is readable since ADR-064")
    fun `the read of setting 13 on both channels`() {
        assertEquals(SettingsStream.READ_13_REQ_CH21_CAP051_1178, wire(Maestro.readSettingRequest(21, SettingsCodec.FIELD_ANC_STATE)!!))
        assertEquals(SettingsStream.READ_13_REQ_CH19_CAP056_1450, wire(Maestro.readSettingRequest(19, SettingsCodec.FIELD_ANC_STATE)!!))
        assertEquals(Settings036.READ_13_REQ, wire(Maestro.readSettingRequest(21, 13)!!), "CAP-036 1517: the official sweep reads it too")
        assertTrue(13 in Maestro.READABLE_FIELDS)
    }

    @Test
    @DisplayName("setting 13 is never written: no flag request, no writable field (ADR-064 item 4)")
    fun `setting 13 has no write path`() {
        assertNull(SettingsCodec.flagRequest(21, SettingsCodec.FIELD_ANC_STATE, true))
        assertEquals(false, SettingsCodec.FIELD_ANC_STATE in SettingsCodec.WRITABLE_FLAG_FIELDS)
    }

    // ---- RESPONSE: the read answers ----

    @Test
    @DisplayName("every captured 4:{13:n} RESPONSE decodes to its mode: 4 Adaptive (CAP-051 1225), 2 Noise cancellation (CAP-056 1468), 3 Transparency (CAP-027 923), 1 Off (CAP-021 1089); stream = false")
    fun `the read answers decode to the four modes`() {
        for ((frame, mode) in listOf(
            SettingsStream.READ_13_RESP_ADAPTIVE_CH21_CAP051_1225 to AncMode.ADAPTIVE,
            SettingsStream.READ_13_RESP_ACTIVE_CH19_CAP056_1468 to AncMode.ACTIVE,
            SettingsStream.READ_13_RESP_TRANSPARENT_CH19_CAP027_923 to AncMode.TRANSPARENT,
            SettingsStream.READ_13_RESP_OFF_CH19_CAP021_1089 to AncMode.OFF,
        )) {
            val routed = setting(frame)
            assertEquals(SettingValue.AncState(mode), routed.value, frame)
            assertEquals(false, routed.stream, "a RESPONSE is a read answer, not a push")
            assertEquals(13, routed.value.field)
        }
    }

    @Test
    @DisplayName("4:{13:0} (CAP-045 2033, CAP-051 2247, CAP-036 1519) is a value — no mode settable — not an unreadable answer")
    fun `value 0 is no mode settable`() {
        for (frame in listOf(
            SettingsStream.READ_13_RESP_NONE_CH21_CAP045_2033,
            SettingsStream.READ_13_RESP_NONE_CH19_CAP051_2247,
            Settings036.READ_13_RESP,
        )) {
            assertEquals(SettingValue.AncState(null), setting(frame).value, frame)
        }
        assertEquals(SettingValue.AncState(null), setting(SettingsStream.STREAM_13_NONE_CH21_CAP045_2277).value)
        assertEquals(true, setting(SettingsStream.STREAM_13_NONE_CH21_CAP045_2277).stream)
    }

    // ---- SERVER_STREAM: the pushes ----

    @Test
    @DisplayName("every captured 4:{13:n} stream packet decodes to its mode with stream = true (CAP-051 1717/1755/1802, CAP-038 2170/2205, CAP-021 1842, CAP-056 2259)")
    fun `the stream packets decode to the four modes`() {
        for ((frame, mode) in listOf(
            SettingsStream.STREAM_13_ACTIVE_CH21_CAP051_1717 to AncMode.ACTIVE,
            SettingsStream.STREAM_13_ADAPTIVE_CH21_CAP051_1755 to AncMode.ADAPTIVE,
            SettingsStream.STREAM_13_TRANSPARENT_CH21_CAP051_1802 to AncMode.TRANSPARENT,
            SettingsStream.STREAM_13_TRANSPARENT_CH19_CAP038_2170 to AncMode.TRANSPARENT,
            SettingsStream.STREAM_13_ACTIVE_CH19_CAP038_2205 to AncMode.ACTIVE,
            SettingsStream.STREAM_13_OFF_CH19_CAP021_1842 to AncMode.OFF,
            SettingsStream.STREAM_13_ADAPTIVE_CH19_CAP056_2259 to AncMode.ADAPTIVE,
        )) {
            val packet = rpc(frame)
            assertEquals(PwRpc.TYPE_SERVER_STREAM, packet.type, frame)
            assertEquals(Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES, packet.methodId, frame)
            val routed = setting(frame)
            assertEquals(SettingValue.AncState(mode), routed.value, frame)
            assertEquals(true, routed.stream, frame)
        }
    }

    @Test
    @DisplayName("ADR-065 item 2: the other fields' stream packets decode through the same decoders — 12 (CAP-056 1696), 2 (CAP-056 2175), 29 (CAP-069 2502); 16 is an EQ frame (CAP-015 2116)")
    fun `other fields from the stream`() {
        assertEquals(
            SettingValue.AncModes(AncModeList(noiseCancellation = false, off = true, transparency = true, adaptive = true)),
            setting(SettingsStream.STREAM_12_CH19_CAP056_1696).value,
        )
        assertEquals(true, setting(SettingsStream.STREAM_12_CH19_CAP056_1696).stream)
        assertEquals(SettingValue.Flag(SettingsCodec.FIELD_IN_EAR_DETECTION, true), setting(SettingsStream.STREAM_2_ON_CH19_CAP056_2175).value)
        assertEquals(SettingValue.HeadGestures(false), setting(SettingsStream.STREAM_29_OFF_CH21_CAP069_2502).value)
        val eq = assertInstanceOf(RoutedFrame.Eq::class.java, route(SettingsStream.STREAM_16_FLAT_CH19_CAP015_2116))
        assertEquals(false, eq.frame.persist)
        assertEquals(listOf(0f, 0f, 0f, 0f, 0f), listOf(eq.frame.gains.lowBass, eq.frame.gains.bass, eq.frame.gains.mid, eq.frame.gains.treble, eq.frame.gains.upperTreble))
    }

    // ---- values and fields the app does not interpret ----

    /** A stream packet `4:{field: varint}` in `CAP-045` 2277's envelope (channel 21), re-sealed — **synthetic**, labelled (AGENTS.md §11). */
    private fun syntheticStream(field: Int, value: Int, type: Int = PwRpc.TYPE_SERVER_STREAM, method: Int = Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES): ByteArray {
        val inner = Varint.encode((field shl 3) or 0) + Varint.encode(value)
        val payload = byteArrayOf(0x22, inner.size.toByte()) + inner
        return Hdlc.encode(Cap061.CHANNEL_21_RESPONSE_ADDRESS, PW_HDLC_CONTROL_UI, PwRpc.encode(RpcPacket(type, 21, Maestro.SERVICE_ID, method, payload)))
    }

    private class Seen {
        var unidentified = 0
        var malformed = 0

        /** A decoder that threw (caught by `decodeGuarded`, `ai-sessions/0094` A93-APP-06): a fuzz run must see none. */
        var faults = 0
    }

    private fun CodecRouter.feedCounting(bytes: ByteArray, seen: Seen): List<RoutedFrame> =
        feed(Dlci.MAESTRO, bytes, 0L, onUnidentified = { seen.unidentified++ }, onMalformed = { seen.malformed++ }, onDecoderFault = { seen.faults++ })

    @Test
    @DisplayName("synthetic: field 13 with every value 0–255 — 0 is none, 1–4 the modes, 5–255 not interpreted (an UnidentifiedFrame, never a mode, never a throw)")
    fun `field 13 with the values 0 to 255`() {
        val router = CodecRouter()
        for (value in 0..255) {
            val seen = Seen()
            val routed = router.feedCounting(syntheticStream(13, value), seen)
            when (value) {
                0 -> assertEquals(listOf<RoutedFrame>(RoutedFrame.Setting(SettingValue.AncState(null), stream = true)), routed)
                1 -> assertEquals(listOf<RoutedFrame>(RoutedFrame.Setting(SettingValue.AncState(AncMode.OFF), stream = true)), routed)
                2 -> assertEquals(listOf<RoutedFrame>(RoutedFrame.Setting(SettingValue.AncState(AncMode.ACTIVE), stream = true)), routed)
                3 -> assertEquals(listOf<RoutedFrame>(RoutedFrame.Setting(SettingValue.AncState(AncMode.TRANSPARENT), stream = true)), routed)
                4 -> assertEquals(listOf<RoutedFrame>(RoutedFrame.Setting(SettingValue.AncState(AncMode.ADAPTIVE), stream = true)), routed)
                else -> {
                    assertEquals(emptyList<RoutedFrame>(), routed, "value $value is not interpreted")
                    assertEquals(1, seen.unidentified, "value $value: an UnidentifiedFrame for the Debug tab")
                }
            }
            assertEquals(0, seen.malformed)
        }
    }

    @Test
    @DisplayName("synthetic: a stream packet for a field this app does not read (10, 20, 31) is UnreadSetting, not an UnidentifiedFrame; the same payload as a RESPONSE stays an RpcResult")
    fun `an unread field from the stream is said, not shown`() {
        val router = CodecRouter()
        for (field in listOf(8, 10, 20, 31)) {
            val seen = Seen()
            assertEquals(listOf<RoutedFrame>(RoutedFrame.UnreadSetting(field)), router.feedCounting(syntheticStream(field, 1), seen), "field $field")
            assertEquals(0, seen.unidentified, "field $field: not an UnidentifiedFrame")
        }
        assertEquals(10, SettingsCodec.fieldOf(rpc(syntheticStream(10, 1).toHex()).payload))
        assertNull(SettingsCodec.decode(rpc(syntheticStream(10, 1).toHex()).payload))
        // A RESPONSE for a field the app never asked for is the ReadSetting's own answer path (an OK result without a readable value), as before.
        val response = router.feedCounting(syntheticStream(10, 1, type = PwRpc.TYPE_RESPONSE, method = Maestro.METHOD_READ_SETTING), Seen())
        assertInstanceOf(RoutedFrame.RpcResult::class.java, response.single())
    }

    @Test
    @DisplayName("a stream packet of field 18 (the saved custom EQ) is an EQ frame with persist = true — read, not shown (ADR-065 item 2)")
    fun `field 18 from the stream is the saved EQ`() {
        val inner = hex("9201190d0000000015000000001d0000000025000000002d00000000") // field 18, the all-zero quintet of CAP-015 2116
        val payload = byteArrayOf(0x22, inner.size.toByte()) + inner
        val packet = RpcPacket(PwRpc.TYPE_SERVER_STREAM, 21, Maestro.SERVICE_ID, Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES, payload)
        val eq = assertInstanceOf(RoutedFrame.Eq::class.java, routeMaestro(packet))
        assertEquals(true, eq.frame.persist)
    }

    @Test
    @DisplayName("fuzz: the real stream packets with random bytes, truncation, oversized lengths and random field numbers, re-sealed, never throw; a real packet still decodes after")
    fun `fuzzed stream packets never throw`() {
        val random = Random(9101)
        val real = listOf(
            SettingsStream.STREAM_13_ACTIVE_CH21_CAP051_1717,
            SettingsStream.STREAM_13_NONE_CH21_CAP045_2277,
            SettingsStream.STREAM_12_CH19_CAP056_1696,
            SettingsStream.STREAM_2_ON_CH19_CAP056_2175,
            SettingsStream.STREAM_29_OFF_CH21_CAP069_2502,
            SettingsStream.STREAM_16_FLAT_CH19_CAP015_2116,
            SettingsStream.READ_13_RESP_ADAPTIVE_CH21_CAP051_1225,
        ).map(::hex)
        val lengths = listOf("ffffffff07", "ffffffff0f", "ffffffffffffffff7f", "f6ffffffffffffff7f", "8080808010").map(::hex)
        val router = CodecRouter()
        val seen = Seen()
        var decoded = 0
        assertTimeoutPreemptively(Duration.ofSeconds(60)) {
            for (frame in real) {
                val hdlc = (Hdlc.decode(frame) as BudsResult.Success).value
                repeat(1_200) {
                    var payload = hdlc.payload.copyOf()
                    when (random.nextInt(5)) {
                        0 -> repeat(1 + random.nextInt(4)) { payload[random.nextInt(payload.size)] = random.nextInt(256).toByte() }
                        1 -> payload = payload.copyOf(random.nextInt(payload.size)) // truncated
                        2 -> { // an oversized length varint somewhere
                            val length = lengths.random(random)
                            val at = random.nextInt(payload.size)
                            for (k in length.indices) if (at + k < payload.size) payload[at + k] = length[k]
                        }
                        3 -> { // a random inner field number with a random varint value
                            val inner = Varint.encode((1 + random.nextInt(60)) shl 3) + Varint.encode(random.nextInt(300))
                            val setting = byteArrayOf(0x22, inner.size.toByte()) + inner
                            payload = PwRpc.encode(
                                RpcPacket(PwRpc.TYPE_SERVER_STREAM, 21, Maestro.SERVICE_ID, Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES, setting),
                            )
                        }
                        else -> payload = ByteArray(random.nextInt(40)) { random.nextInt(256).toByte() } // noise
                    }
                    val routed = router.feedCounting(Hdlc.encode(hdlc.address, hdlc.control, payload), seen)
                    decoded += routed.size
                    for (r in routed) {
                        if (r is RoutedFrame.Setting && r.value is SettingValue.AncState) {
                            val mode = (r.value as SettingValue.AncState).mode
                            assertTrue(mode == null || mode in AncMode.entries) // never a mode outside the four
                        }
                    }
                }
                // Truncated raw frames too (a cut-off read): every prefix of the real frame.
                for (n in 1 until frame.size) router.feedCounting(frame.copyOf(n), seen)
                router.reset(Dlci.MAESTRO)
            }
        }
        assertTrue(seen.unidentified + seen.malformed > 0)
        assertEquals(0, seen.faults, "no decoder threw (a throw would be turned into a malformed frame and hidden)")
        assertEquals(
            listOf<RoutedFrame>(RoutedFrame.Setting(SettingValue.AncState(AncMode.ACTIVE), stream = true)),
            router.feedCounting(hex(SettingsStream.STREAM_13_ACTIVE_CH21_CAP051_1717), seen),
            "nothing was poisoned",
        )
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
