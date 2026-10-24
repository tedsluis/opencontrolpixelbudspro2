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
package io.github.tedsluis.opencontrolpixelbuds.data.experiment

import io.github.tedsluis.opencontrolpixelbuds.data.codec.Hdlc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Maestro
import io.github.tedsluis.opencontrolpixelbuds.data.codec.MaestroChannel
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RpcPacket
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.random.Random

/**
 * The experiment card's frames (`ai-sessions/0096` round 1's A, `ai-sessions/0098` round 2's F). Real bytes where a capture holds them; the one frame no capture
 * holds — F4 on channel 19 — is a **labelled structural test** (`AGENTS.md` §11), next to its channel-21 real-byte control.
 */
class ExperimentFramesTest {

    private fun wire(packet: RpcPacket, channel: Int): String {
        val ch = MaestroChannel.forChannel(channel)!!
        return Hdlc.encode(ch.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(packet)).joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    private fun bytes(s: String) = s.replace(" ", "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun `A - WriteSetting 4 13 n equals CAP-078`() {
        // CAP-078-btsnoop_hci.log: channel 21 H 7277 (1 Off), 6491 (2 Noise cancellation), 6540 (3 Transparency), 7298 (4 Adaptive); channel 19 H 5921 (1 Off).
        assertEquals("7e004b0310151dea71de7d5e251d9a8c9e2a0422026801c8652a047e", wire(ExperimentFrames.writeAncState(21, 1), 21))
        assertEquals("7e004b0310151dea71de7d5e251d9a8c9e2a04220268027234239d7e", wire(ExperimentFrames.writeAncState(21, 2), 21))
        assertEquals("7e004b0310151dea71de7d5e251d9a8c9e2a0422026803e40424ea7e", wire(ExperimentFrames.writeAncState(21, 3), 21))
        assertEquals("7e004b0310151dea71de7d5e251d9a8c9e2a0422026804479140747e", wire(ExperimentFrames.writeAncState(21, 4), 21))
        assertEquals("7e003b0310131dea71de7d5e251d9a8c9e2a0422026801f889dc6c7e", wire(ExperimentFrames.writeAncState(19, 1), 19))
        assertThrows(IllegalArgumentException::class.java) { ExperimentFrames.writeAncState(19, 0) }
        assertThrows(IllegalArgumentException::class.java) { ExperimentFrames.writeAncState(19, 5) }
    }

    @Test
    fun `F1 - SetWallclock equals CAP-041`() {
        // CAP-041-btsnoop_hci.log 783, channel 21: the RPC packet `10 15 1d <Maestro> 25 <SetWallclock> 2a 07 08 a7 83 9b ba 87 34` — real bytes.
        assertArrayEquals(
            bytes("10 15 1d ea 71 de 7e 25 4e ed 3b 67 2a 07 08 a7 83 9b ba 87 34"),
            PwRpc.encode(ExperimentFrames.setWallclock(21, 1_788_707_520_935L)),
        )
        assertEquals(Maestro.METHOD_SET_WALLCLOCK, ExperimentFrames.setWallclock(19, 1L).methodId)
        assertThrows(IllegalArgumentException::class.java) { ExperimentFrames.setWallclock(21, 0L) }
    }

    @Test
    fun `F1 - SetWallclock on channel 19 equals CAP-079 and its answer is the RpcResult the runner waits for`() {
        // CAP-079 H3 = CAP-079-btsnoop_hci4.log.last 2559 (08:13:11.642), channel 19: SetWallclock 1:1792303991632 — the whole pw_hdlc frame, real bytes.
        assertEquals("7e003b0310131dea71de7d5e254eed3b672a0708d09692ed9434473d2f287e", wire(ExperimentFrames.setWallclock(19, 1_792_303_991_632L), 19))
        // H3 2563 (08:13:11.720): its empty RESPONSE — since `ai-sessions/0100` an RpcResult with the method the runner's `awaitResult` matches (F1 reported a time-out).
        val routed = io.github.tedsluis.opencontrolpixelbuds.data.codec.CodecRouter()
            .feed(io.github.tedsluis.opencontrolpixelbuds.data.codec.Dlci.MAESTRO, bytes("7e80a303080110131dea71de7d5e254eed3b6712a9053e7e"), timestampMillis = 0)
        val result = routed.single() as io.github.tedsluis.opencontrolpixelbuds.data.codec.RoutedFrame.RpcResult
        assertEquals(Maestro.METHOD_SET_WALLCLOCK, result.methodId)
        assertEquals(true, result.isOk)
    }

    @Test
    fun `F2 F3 - the Dosimeter requests equal CAP-027 and CAP-058`() {
        assertEquals(0x73d5d805, ExperimentFrames.SERVICE_DOSIMETER)
        assertEquals(0x4d93b6e2, ExperimentFrames.METHOD_SUBSCRIBE_TO_LIVE_DB)
        assertEquals(0x73b772ce, ExperimentFrames.METHOD_FETCH_DAILY_SUMMARIES)
        // CAP-027-btsnoop_hci.log 1212 (channel 19) and 1214 (channel 21): SubscribeToLiveDb — real bytes.
        assertEquals("7e003b0310131d05d8d57325e2b6934de4610c6a7e", wire(ExperimentFrames.subscribeToLiveDb(19), 19))
        assertEquals("7e004b0310151d05d8d57325e2b6934de218d6687e", wire(ExperimentFrames.subscribeToLiveDb(21), 21))
        // CAP-058-btsnoop_hci.log 5736: FetchDailySummaries on channels 19 and 21 — real bytes.
        assertEquals("7e003b0310131d05d8d57325ce72b773f3b3c7267e", wire(ExperimentFrames.fetchDailySummaries(19), 19))
        assertEquals("7e004b0310151d05d8d57325ce72b773f5ca1d247e", wire(ExperimentFrames.fetchDailySummaries(21), 21))
    }

    @Test
    fun `F4 - WriteSetting 4 21 b equals CAP-058 on channel 21`() {
        // CAP-058-btsnoop_hci.log 5747 (off) and 5753 (on), channel 21 — real bytes.
        assertEquals("7e004b0310151dea71de7d5e251d9a8c9e2a052203a80100eaadf6a57e", wire(ExperimentFrames.writeField21(21, false), 21))
        assertEquals("7e004b0310151dea71de7d5e251d9a8c9e2a052203a801017c9df1d27e", wire(ExperimentFrames.writeField21(21, true), 21))
    }

    @Test
    fun `structural - F4 on channel 19 differs from channel 21 only in the address and channel id`() {
        // No capture holds channel 19's form (`ai-sessions/0098` Checkpoint 1, F4 "Akkoord, ook kanaal 19"): same payload, the channel-19 envelope.
        val p19 = ExperimentFrames.writeField21(19, true)
        val p21 = ExperimentFrames.writeField21(21, true)
        assertArrayEquals(p21.payload, p19.payload)
        assertEquals(19, p19.channelId)
        val prefix = "7e003b0310131dea71de7d5e251d9a8c9e2a052203a80101"
        assertEquals(prefix, wire(p19, 19).take(prefix.length))
    }

    @Test
    fun `describeFields prints raw fields and never throws`() {
        // CAP-078 H 6493's RPC field 5 `2a 04 22 02 68 02` (the push 4:{13:2}) — real bytes.
        assertEquals("5:{4:{13:2}}", ExperimentFrames.describeFields(bytes("2a 04 22 02 68 02")))
        assertEquals("", ExperimentFrames.describeFields(ByteArray(0)))
        val random = Random(98)
        repeat(20_000) {
            val b = ByteArray(random.nextInt(0, 64)).also { random.nextBytes(it) }
            ExperimentFrames.describeFields(b)
        }
    }

    @Test
    fun `the Dosimeter decoder reads CAP-027 live packets and CAP-058 daily answers as pwrpc_decode does`() {
        // CAP-027-btsnoop_hci.log 1228 (channel 21) and 1229 (channel 19): SubscribeToLiveDb SERVER_STREAM packets — real bytes. The expected text is
        // `scripts/pwrpc_decode.py`'s line for the same frame (its fields after "|").
        assertEquals(
            ExperimentFrames.METHOD_SUBSCRIBE_TO_LIVE_DB to "SERVER_STREAM ch=21 SubscribeToLiveDb OK 2:f32(0.00)",
            ExperimentFrames.dosimeterLine(bytes("7e00a5032a051500000000080710151d05d8d57325e2b6934d1afa3f1a7e")),
        )
        assertEquals(
            "SERVER_STREAM ch=19 SubscribeToLiveDb OK 2:f32(0.00)",
            ExperimentFrames.dosimeterLine(bytes("7e80a3032a051500000000080710131d05d8d57325e2b6934d49b7f9527e"))?.second,
        )
        // CAP-058-btsnoop_hci.log 5740 (channel 21): the FetchDailySummaries RESPONSE to 5736 — real bytes.
        assertEquals(
            "RESPONSE ch=21 FetchDailySummaries OK 1:462 2:{1:228 6:f32(115611.61)} 2:{1:456 6:f32(110871392.00)} 2:{1:457 6:f32(602686272.00)} " +
                "2:{1:458 6:f32(60935708.00)} 2:{1:459 6:f32(471361760.00)} 2:{1:460 6:f32(5102183936.00)} 2:{1:461 6:f32(40887544.00)} " +
                "2:{1:462 6:f32(204890640.00)} 5:f32(245338960.00)",
            ExperimentFrames.dosimeterLine(
                bytes(
                    "7e00a5032a5808ce03120808e40135cecde147120808c803356c78d34c120808c903350db10f4e120808ca03358773684c120808cb033547c3e04d120808cc0335930e984f" +
                        "120808cd03353ef91b4c120808ce03352166434d2d35f9694d080110151d05d8d57325ce72b773b3fac1147e",
                ),
            )?.second,
        )
        // Not the Dosimeter: CAP-078 H 6493, the settings push on channel 21 — real bytes.
        assertEquals(null, ExperimentFrames.dosimeterLine(bytes("7e00a5032a0422026802080710151dea71de7d5e25f5ad2128cbb358d57e")))
        val random = Random(27)
        repeat(20_000) {
            val b = ByteArray(random.nextInt(0, 80)).also { random.nextBytes(it) }
            ExperimentFrames.dosimeterLine(byteArrayOf(0x7e) + b + byteArrayOf(0x7e))
        }
    }
}
