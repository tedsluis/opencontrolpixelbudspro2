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
@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.tedsluis.opencontrolpixelbuds.data

import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap045
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap061
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap062
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap063
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap064
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap065
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap066Balance
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap072
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Dlci
import io.github.tedsluis.opencontrolpixelbuds.data.codec.HardwareInfoFixtures
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Hdlc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Maestro
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RpcPacket
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Settings036
import io.github.tedsluis.opencontrolpixelbuds.data.codec.SettingsWrites
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Settings056
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Settings070
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Settings074
import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeCause
import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
import io.github.tedsluis.opencontrolpixelbuds.domain.WornReading
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingSource
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.EqPreset
import io.github.tedsluis.opencontrolpixelbuds.domain.RingNotice
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionLoss
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine
import io.github.tedsluis.opencontrolpixelbuds.domain.isCurrent
import io.github.tedsluis.opencontrolpixelbuds.hardware.FakeBudsTransport
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

private fun hex(s: String): ByteArray {
    val clean = s.trim()
    return ByteArray(clean.length / 2) { i -> clean.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
}

class BudsRepositoryImplTest {

    /** A fresh [ConnectionStateMachine] starts at `Disconnected` (its own default) — [driveToReady]
     * replays the real `Disconnected -> Connecting -> Discovering -> Ready` sequence
     * ([ConnectionStateMachine]'s only public path to `Ready`) so most tests below can assume an
     * already-connected repository without duplicating that sequence inline. */
    private fun buildConnectionStateMachine(driveToReady: Boolean = true): ConnectionStateMachine {
        val machine = ConnectionStateMachine()
        if (driveToReady) {
            machine.onConnectRequested()
            machine.onLinkEstablished()
            machine.onReady()
        }
        return machine
    }

    /**
     * By default the scripted Buds behave like the verified device (DECISIONS.md ADR-042): they announce firmware `release_5.203` on
     * the MAESTRO channel, send the Pro 2's Model ID (`03 01 00 03 da 2d b1`, `CAP-059` frame 1049) on every Message Stream open, and
     * ACK every ANC `Set` / Ring command the way `CAP-001` frame 2041 / `CAP-025` frame 2044 do. [verified] = false starts from a
     * silent peer (no announcement, no Model ID, no ACKs) for the tests about those very things.
     */
    private suspend fun TestScope.buildRepository(
        scope: TestScope = this,
        connectionStateMachine: ConnectionStateMachine = buildConnectionStateMachine(),
        verified: Boolean = true,
        /** Called on every connect attempt that reaches the bonded-device lookup (a re-open attempt, ADR-044, counts here). */
        onConnectAttempt: () -> Unit = {},
    ): Pair<BudsRepositoryImpl, FakeBudsTransport> {
        val transport = FakeBudsTransport()
        // backgroundScope, not `scope` itself: BudsRepositoryImpl's init block launches
        // collectors that run for the component's whole lifetime by design (they observe
        // an infinite transport/battery stream) — runTest expects every child of its own
        // scope to finish by the test's end, and only backgroundScope's children are
        // exempted (auto-cancelled instead), which matches what these collectors actually are.
        val repo = BudsRepositoryImpl(
            transport = transport,
            connectionStateMachine = connectionStateMachine,
            // No real BluetoothDevice needed — none of the tests below exercise connect() against
            // a bonded device (see the dedicated connect()-failure test instead).
            bondedDeviceProvider = { onConnectAttempt(); null },
            debugModeEnabled = MutableStateFlow(false),
            scope = scope.backgroundScope,
            clock = { scope.testScheduler.currentTime },
        )
        settle() // start the repository's collectors: a SharedFlow drops what is emitted before anyone collects
        if (verified) {
            transport.onOpenChannel = { channel -> if (channel == Dlci.FAST_PAIR_MESSAGE_STREAM) transport.emit(channel, MODEL_ID_PRO_2) }
            transport.onSent = { channel, frame -> autoAck(transport, channel, frame) }
            // The real announcement (CAP-061 frame 1508, field-1 version number redacted) — not a hand-made one: the synthetic shape without the fixed64
            // field 5 hid the parser defect that put the verified Buds in Safe Mode on hardware (ai-sessions/0046).
            transport.emit(Dlci.MAESTRO, Cap061.announcementFrame())
            settle()
        }
        return repo to transport
    }

    /**
     * The Buds' ACK for an ANC Set (`ff 01 00 06 08 12 01 e8 e8 <mode>`, `CAP-001` frame 2041) or a Ring (`ff 01 00 03 04 01 00`), and — since every ANC
     * tap sends `08 11` first (`ai-sessions/0062` F-1) — the answer to a `Get`: `CAP-065` frame 6334 (Settable `e8`, mode OFF, both buds worn).
     */
    private suspend fun autoAck(transport: FakeBudsTransport, channel: Int, frame: ByteArray) {
        if (channel != Dlci.FAST_PAIR_MESSAGE_STREAM || frame.size < 4) return
        when (frame.toHex().take(4)) {
            "0811" -> transport.emit(channel, hex(Cap065.NOTIFY_E8_OFF_6334))
            "0812" -> transport.emit(channel, hex("ff010006081201e8e8") + byteArrayOf(frame[7]))
            "0401" -> transport.emit(channel, hex("ff010003040100"))
        }
    }

    /** The real announcement's layout (CAP-061 frame 1508: 3 entries, fixed64 field 5, field 6) with only the firmware string swapped. */
    private fun helloWithFirmware(channel: Int, responseAddress: Int, firmware: String): ByteArray {
        val serial = byteArrayOf(0x0a, 10) + "0000000000".toByteArray()
        val entry = serial + byteArrayOf(0x12, firmware.length.toByte()) + firmware.toByteArray()
        val one = byteArrayOf(entry.size.toByte()) + entry
        val entries = byteArrayOf(0x0a) + one + byteArrayOf(0x12) + one + byteArrayOf(0x1a) + one
        val payload = byteArrayOf(0x22, entries.size.toByte()) + entries + hex("2934293fc2f6cbd81a" + "3000")
        return Hdlc.encode(
            responseAddress,
            PW_HDLC_CONTROL_UI,
            PwRpc.encode(RpcPacket(PwRpc.TYPE_RESPONSE, channel, Maestro.SERVICE_ID, Maestro.METHOD_GET_SOFTWARE_INFO, payload, callId = PwRpc.CALL_ID_UNSOLICITED)),
        )
    }

    @Test
    fun `setAncMode sends the Get, then the exact Set bytes, and applies the mode on the ACK`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        val result = repo.setAncMode(AncMode.ADAPTIVE)
        assertInstanceOf(BudsResult.Success::class.java, result)

        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM, Dlci.FAST_PAIR_MESSAGE_STREAM), transport.sent.map { it.first })
        assertEquals(listOf("08110000", "0812001401e8e840" + "00".repeat(16)), transport.sent.map { it.second.toHex() })
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first())
    }

    @Test
    fun `a real inbound ANC Notify frame updates ancMode`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        assertNull(repo.ancMode.first(), "no report yet in this app run")

        // CAP-036 frame 1182 (PROTOCOL.md §4.1), current state = Off.
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e80020"))
        settle()

        assertEquals(AncMode.OFF, repo.ancMode.first())
    }

    @Test
    fun `refreshAncMode sends Get and resolves once a fresh Notify arrives`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.onSent = null // the test answers the Get itself

        var result: BudsResult<AncMode>? = null
        val job = launch { result = repo.refreshAncMode() }
        runCurrent() // let refreshAncMode send its Get and suspend waiting for a fresh Notify —
        // without advancing virtual time, so the 5s timeout can't race ahead of the emit below.
        assertEquals(Dlci.FAST_PAIR_MESSAGE_STREAM, transport.sent.single().first)
        assertEquals("08110000", transport.sent.single().second.toHex())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e80008")) // Active
        job.join()

        assertInstanceOf(BudsResult.Success::class.java, result)
        assertEquals(AncMode.ACTIVE, (result as BudsResult.Success).value)
    }

    @Test
    fun `refreshAncMode times out if no Notify ever arrives`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.onSent = null // the Buds answer nothing — and the claim stays open (a close would be AnswerCutOff, F-3)

        val result = repo.refreshAncMode()
        assertInstanceOf(BudsResult.Failure::class.java, result)
        assertEquals(BudsError.Timeout, (result as BudsResult.Failure).error)
    }

    // ---- Maestro / EQ (DECISIONS.md ADR-034) ------------------------------------------------------

    /**
     * The Buds' unsolicited announcement of this connection's pw_rpc channel — **a hand-built, header-only frame** (the real header of CAP-036 frame 1405 /
     * CAP-015 frame 1879, **no payload**): a labelled supplementary fixture (AGENTS.md §11; labelled `ai-sessions/0069`, A68-APP-06) for the tests that only
     * need "the channel is announced". It carries no firmware, so it never satisfies Safe Mode by itself — the real announcement with its payload is
     * [Cap061.announcementFrame], which [buildRepository] emits for every verified repository.
     */
    private fun helloFrame(channel: Int, responseAddress: Int) = Hdlc.encode(
        responseAddress,
        PW_HDLC_CONTROL_UI,
        PwRpc.encode(
            RpcPacket(PwRpc.TYPE_RESPONSE, channel, Maestro.SERVICE_ID, Maestro.METHOD_GET_SOFTWARE_INFO, callId = PwRpc.CALL_ID_UNSOLICITED),
        ),
    )

    private fun rpcFrame(responseAddress: Int, packet: RpcPacket) = Hdlc.encode(responseAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(packet))

    private fun quintetPayload(gains: EqBandGains, field: Int = 16) =
        io.github.tedsluis.opencontrolpixelbuds.data.codec.EqFrameEncoder.settingsPayload(
            io.github.tedsluis.opencontrolpixelbuds.data.codec.EqFrame(gains, persist = field == 18, channelId = 0),
        )

    /** `advanceUntilIdle()` does not run the repository's `backgroundScope` collectors; `runCurrent()` does — a few rounds
     * let an emitted frame travel collector -> reply flow -> waiting request. */
    private fun TestScope.settle() = repeat(6) { runCurrent() }

    private val heavyBass = EqBandGains(upperTreble = 0f, treble = 0f, mid = 0f, bass = 3.0f, lowBass = 5.0f)

    @Test
    fun `setEqGains on the announced channel sends CAP-015 frame 2165 byte for byte, waits for the ACK and updates eqProfile`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)) // channel 19 <-> request address 00 3b
        transport.onSent = { _, _ -> transport.emit(Dlci.MAESTRO, hex("7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e")) } // frame 2117
        settle()

        val result = repo.setEqGains(heavyBass)
        assertInstanceOf(BudsResult.Success::class.java, result)

        val sent = transport.sent.single()
        assertEquals(Dlci.MAESTRO, sent.first)
        assertEquals(
            "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015000040401d0000000025000000002d000000007b1bccbe7e",
            sent.second.toHex(), // CAP-015 frame 2165, whole frame incl. CRC
        )
        assertEquals(heavyBass, repo.eqProfile.first())
        assertNull(repo.eqError.first())
    }

    @Test
    @DisplayName("A68-APP-16: the Flat preset writes CAP-015 frame 2111 byte for byte (the official app's all-zero quintet, 06:12:13.279)")
    fun `the Flat preset sends the captured all-zero write`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0))
        transport.onSent = { _, _ -> transport.emit(Dlci.MAESTRO, hex("7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e")) } // frame 2117, the Buds' OK
        settle()

        assertInstanceOf(BudsResult.Success::class.java, repo.applyEqPreset(EqPreset.FLAT))

        assertEquals(
            "7e003b0310131dea71de7d5e251d9a8c9e2a1e221c8201190d0000000015000000001d0000000025000000002d00000000881667fe7e",
            transport.sent.single().second.toHex(), // CAP-015 frame 2111, whole frame incl. CRC
        )
        assertEquals(EqBandGains.FLAT, repo.eqProfile.first())
    }

    @Test
    @DisplayName("A68-APP-11: a gain that is not a number is refused before anything is sent")
    fun `setEqGains with a NaN band sends nothing`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0))
        settle()

        val result = repo.setEqGains(EqBandGains(Float.NaN, 0f, 0f, 0f, 0f))

        assertInstanceOf(BudsResult.Failure::class.java, result)
        assertEquals(emptyList<Pair<Int, ByteArray>>(), transport.sent)
        assertNull(repo.eqProfile.first())
    }

    @Test
    fun `setEqGains on another announced channel uses that channel and its address (24 - 80 3d)`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(24, 13504))
        transport.onSent = { _, _ -> transport.emit(Dlci.MAESTRO, rpcFrame(13504, RpcPacket(PwRpc.TYPE_RESPONSE, 24, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING))) }
        settle()

        assertInstanceOf(BudsResult.Success::class.java, repo.setEqGains(heavyBass))
        assertEquals("7e803d0310181dea71de7d5e25", transport.sent.single().second.toHex().take(26))
    }

    @Test
    fun `setEqGains without any announcement sends nothing and reports why`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        settle()

        val result = repo.setEqGains(heavyBass)
        assertEquals(BudsError.MaestroChannelUnknown(null), (result as BudsResult.Failure).error)
        assertEquals(0, transport.sent.size)
        assertNull(repo.eqProfile.value)
        assertEquals(BudsError.MaestroChannelUnknown(null), repo.eqError.first())
    }

    @Test
    fun `an announced channel with no known address sends nothing (never guessed)`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(22, 0x28c0))
        settle()

        val result = repo.setEqGains(heavyBass)
        assertEquals(BudsError.MaestroChannelUnknown(22), (result as BudsResult.Failure).error)
        assertEquals(0, transport.sent.size)
    }

    @Test
    fun `a rejected write is reported with the Buds' status and the profile is not updated`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0))
        transport.onSent = { _, _ ->
            transport.emit(Dlci.MAESTRO, rpcFrame(0x28c0, RpcPacket(PwRpc.TYPE_RESPONSE, 19, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, status = 5)))
        }
        settle()

        val result = repo.setEqGains(heavyBass)
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), (result as BudsResult.Failure).error)
        assertNull(repo.eqProfile.value)
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), repo.eqError.first())
    }

    @Test
    fun `a write the Buds never answer is a Timeout, not an assumed success`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()

        val result = repo.setEqGains(heavyBass)
        assertEquals(BudsError.Timeout, (result as BudsResult.Failure).error)
        assertNull(repo.eqProfile.value)
    }

    @Test
    fun `refreshEq sends CAP-036 frame 1523 and fills eqProfile from the real response (frame 1525)`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        transport.onSent = { _, _ ->
            transport.emit(Dlci.MAESTRO, hex("7e00a5032a1e221c8201190dc0cccc3d15000000001da099993e25c0cc4c3e2dc0cc4c3e080110151dea71de7d5e2551aed0ae85b618ed7e"))
        }
        settle()

        val result = repo.refreshEq()
        assertInstanceOf(BudsResult.Success::class.java, result)
        assertEquals("7e004b0310151dea71de7d5e2551aed0ae2a02201047eeadcf7e", transport.sent.single().second.toHex())
        val gains = repo.eqProfile.first()!!
        assertEquals(0.1f, gains.lowBass, 1e-4f)
        assertEquals(0.3f, gains.mid, 1e-4f)
        assertNull(repo.eqError.first())
    }

    @Test
    fun `a failed read leaves the EQ unknown and says why`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0))
        transport.onSent = { _, _ ->
            // CAP-015 frame 1920's shape: a RESPONSE to ReadSetting carrying status UNKNOWN.
            transport.emit(Dlci.MAESTRO, rpcFrame(0x28c0, RpcPacket(PwRpc.TYPE_RESPONSE, 19, Maestro.SERVICE_ID, Maestro.METHOD_READ_SETTING, status = 2)))
        }
        settle()

        val result = repo.refreshEq()
        assertEquals(BudsError.MaestroRejected("RESPONSE UNKNOWN"), (result as BudsResult.Failure).error)
        assertNull(repo.eqProfile.value)
        assertEquals(BudsError.MaestroRejected("RESPONSE UNKNOWN"), repo.eqError.first())
    }

    /** The official Buds of `CAP-036`: every `ReadSetting 4:N` on channel 21 is answered with that sweep's real frame (Settings036). */
    private fun answerReadsLikeCap036(transport: FakeBudsTransport) {
        transport.onSent = { ch, frame ->
            if (ch == Dlci.MAESTRO) {
                val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
                if (rpc.methodId == Maestro.METHOD_READ_SETTING) {
                    Settings036.ANSWERS[rpc.payload[1].toInt()]?.let { transport.emit(Dlci.MAESTRO, hex(it)) }
                }
                // ADR-058: the Buds answer the one GetHardwareInfo as in CAP-072 A 7596 (= CAP-036 1423's bytes; serials redacted in the fixture).
                if (rpc.methodId == Maestro.METHOD_GET_HARDWARE_INFO) transport.emit(Dlci.MAESTRO, hex(HardwareInfoFixtures.RESPONSE_CH21_CAP072_A7596_REDACTED))
            }
        }
    }

    /** The field (`4:N`) of every `ReadSetting` the app sent, in order, and the method of every DLCI 0x02 request. */
    private fun FakeBudsTransport.maestroRequests() = sent.filter { it.first == Dlci.MAESTRO }.map {
        (PwRpc.decode((Hdlc.decode(it.second) as BudsResult.Success).value.payload) as BudsResult.Success).value
    }

    /**
     * The Connect-time (and pull) settings reads, in order — the official app's ascending sweep (ADR-036, ADR-046; `ai-sessions/0074` adds 11, 15, 27, 28 and
     * 29, ADR-052 … ADR-055) — each with the real request of the `CAP-036` sweep on channel 21 (frames 1445, 1451, 1457, 1471, 1514, 1520, 1526, 1532, 1538,
     * 1553, 1556, 1559).
     */
    private val settingReads: List<Pair<Int, String>> = listOf(
        2 to Settings036.READ_2_REQ, 4 to Settings036.READ_4_REQ, 7 to Settings036.READ_7_REQ, 11 to Settings036.READ_11_REQ,
        12 to Settings056.READ_12_REQ_CH21_1514, 15 to Settings036.READ_15_REQ, 17 to Settings036.READ_17_REQ, 19 to Settings036.READ_19_REQ, 22 to Settings036.READ_22_REQ,
        27 to Settings036.READ_27_REQ, 28 to Settings036.READ_28_REQ, 29 to Settings036.READ_29_REQ,
    )
    private val settingReadFields: List<Int> get() = settingReads.map { it.first }
    private val cap036SettingReads: List<String> get() = settingReads.map { it.second }

    @Test
    fun `the Connect-time read (launchInitialEqRead) waits for the announcement, then reads field 16 on that channel`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        settle()
        answerReadsLikeCap036(transport)
        repo.launchInitialEqRead()
        advanceTimeBy(1_000)
        assertEquals(0, transport.sent.size) // nothing is sent until the Buds have announced their channel
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()

        val requests = transport.maestroRequests()
        assertEquals("2010", requests.first().payload.toHex(), "4:16 first")
        assertEquals(
            3 + settingReads.size,
            requests.size,
            "the EQ read, the settings reads (ADR-036, ADR-046, ADR-053), the runtime-info subscription (ADR-043), then GetHardwareInfo (ADR-058)",
        )
        assertEquals(Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO, requests[requests.size - 2].methodId)
        assertEquals(Maestro.METHOD_GET_HARDWARE_INFO, requests.last().methodId, "last, after the subscription")
        assertEquals(0.3f, repo.eqProfile.first()!!.mid, 1e-4f) // CAP-036 frame 1525
    }

    @Test
    @DisplayName("Connect reads 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29 byte-identical to CAP-036 1445 … 1559 and fills the settings from 1447 … 1561, with their time")
    fun `the Connect-time settings reads`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        answerReadsLikeCap036(transport)
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()
        advanceTimeBy(4_000)
        repo.launchInitialEqRead()
        settle()

        val maestro = transport.sent.filter { it.first == Dlci.MAESTRO }.map { it.second.toHex() }
        assertEquals(cap036SettingReads, maestro.subList(1, 1 + settingReads.size))
        assertEquals(Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO, transport.maestroRequests().let { it[it.size - 2] }.methodId)
        assertEquals(Maestro.METHOD_GET_HARDWARE_INFO, transport.maestroRequests().last().methodId, "ADR-058: last")
        assertEquals(
            BudsSettings(
                inEarDetection = SettingReading(true, 4_000),
                touchControls = SettingReading(true, 4_000),
                holdLeft = SettingReading(HoldAction.NOISE_CONTROL, 4_000),
                holdRight = SettingReading(HoldAction.NOISE_CONTROL, 4_000),
                ancModeList = SettingReading(AncModeList(noiseCancellation = true, off = false, transparency = true, adaptive = true), 4_000), // 1516
                volumeBalance = SettingReading(5, 4_000),
                monoAudio = SettingReading(false, 4_000),
                conversationDetection = SettingReading(true, 4_000),
                multipoint = SettingReading(true, 4_000), // 1513
                headGestures = SettingReading(true, 4_000), // 1561: 29:2 = on
                caseSoundOtherAlerts = SettingReading(true, 4_000), // 1555
                caseSoundEarbudsReplaced = SettingReading(true, 4_000), // 1558
                volumeEq = SettingReading(true, 4_000), // 1522
            ),
            repo.settings.value,
        )
        assertNull(repo.settingsError.first()?.error)
        assertEquals(listOf(12), transport.maestroRequests().filter { it.methodId == Maestro.METHOD_READ_SETTING }.map { it.payload[1].toInt() }.filter { it == 12 }, "read once (ADR-046)")
    }

    @Test
    fun `an unanswered or rejected settings read leaves that field not read, says why, is not retried, and the sequence goes on`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        transport.onSent = { ch, frame ->
            val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
            if (ch == Dlci.MAESTRO && rpc.methodId == Maestro.METHOD_READ_SETTING) {
                when (val field = rpc.payload[1].toInt()) {
                    4 -> transport.emit(Dlci.MAESTRO, hex(Settings036.READ_REJECTED_1441)) // a real rejected read (status UNKNOWN)
                    17 -> Unit // no answer
                    else -> Settings036.ANSWERS[field]?.let { transport.emit(Dlci.MAESTRO, hex(it)) }
                }
            }
        }
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()
        repo.launchInitialEqRead()
        settle()
        advanceTimeBy(2_100) // SETTING_READ_TIMEOUT_MS for field 17
        settle()

        val reads = transport.maestroRequests().filter { it.methodId == Maestro.METHOD_READ_SETTING }.map { it.payload[1].toInt() }
        assertEquals(listOf(16) + settingReadFields, reads, "one pass, nothing retried")
        assertNull(repo.settings.value.touchControls)
        assertNull(repo.settings.value.volumeBalance)
        assertEquals(true, repo.settings.value.conversationDetection?.value)
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure(BudsError.Timeout, write = false), repo.settingsError.first(), "the last reason, a read")
        assertEquals(Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO, transport.maestroRequests().let { it[it.size - 2] }.methodId)
        assertEquals(Maestro.METHOD_GET_HARDWARE_INFO, transport.maestroRequests().last().methodId, "ADR-058: the sequence goes on to the hardware-info read")
    }

    /**
     * A58-APP-03 (`ai-sessions/0059`): an answered but undecodable `ReadSetting` ends the wait at once as [BudsError.UnreadableAnswer], not as a 2 s
     * `Timeout`. **Supplementary, hand-built:** the real `CAP-036` frame 1462 (`4:{7:{1:{4:{1:5}} 2:{4:{1:5}}}}`, both buds Active noise control) with both
     * action values changed from 5 to 7 (no such action) and the HDLC frame re-encoded (new CRC); the real 1462 itself is decoded by the test above.
     */
    @Test
    fun `an answered but undecodable settings read is UnreadableAnswer at once, not a Timeout, and the pass goes on`() = runTest {
        val real = (Hdlc.decode(hex(Settings036.READ_7_RESP)) as BudsResult.Success).value
        val mutatedPayload = hex(real.payload.toHex().replace("2202080512042202080508", "2202080712042202080708"))
        val mutated = Hdlc.encode(real.address, real.control, mutatedPayload)
        val (repo, transport) = buildRepository(verified = false)
        transport.onSent = { ch, frame ->
            val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
            if (ch == Dlci.MAESTRO && rpc.methodId == Maestro.METHOD_READ_SETTING) {
                when (val field = rpc.payload[1].toInt()) {
                    7 -> transport.emit(Dlci.MAESTRO, mutated)
                    else -> Settings036.ANSWERS[field]?.let { transport.emit(Dlci.MAESTRO, hex(it)) }
                }
            }
        }
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()
        val start = currentTime
        repo.launchInitialEqRead()
        settle()

        val reads = transport.maestroRequests().filter { it.methodId == Maestro.METHOD_READ_SETTING }.map { it.payload[1].toInt() }
        assertEquals(listOf(16) + settingReadFields, reads, "the pass went on without waiting")
        assertEquals(start, currentTime, "no timeout was waited for")
        assertNull(repo.settings.value.holdLeft)
        assertNull(repo.settings.value.holdRight)
        assertEquals(5, repo.settings.value.volumeBalance?.value, "the next fields were read")
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure(BudsError.UnreadableAnswer, write = false), repo.settingsError.first())
    }

    // ---- D-11 (ai-sessions/0057): the settings re-read on a user pull (Sound / Controls) -------------------------------------------------------------------

    @Test
    @DisplayName("D-11: a pull re-reads the Connect-time fields byte-identical to CAP-036 1445 … 1538 — nothing else — and stamps the answers (1447 … 1540) with the new time")
    fun `refreshSettings re-reads the Connect-time fields once, in order, and nothing else`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        answerReadsLikeCap036(transport)
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()
        advanceTimeBy(5_000)

        val result = repo.refreshSettings()

        assertEquals(BudsResult.Success(Unit), result)
        assertEquals(cap036SettingReads, transport.sent.map { (ch, frame) -> assertEquals(Dlci.MAESTRO, ch); frame.toHex() }, "exactly the Connect-time reads, in order")
        assertEquals(SettingReading(true, 5_000), repo.settings.value.inEarDetection) // 1447
        assertEquals(SettingReading(5, 5_000), repo.settings.value.volumeBalance) // 1528: zigzag 10 = +5
        assertEquals(SettingReading(true, 5_000), repo.settings.value.conversationDetection) // 1540
        assertNull(repo.settingsError.first())
    }

    @Test
    fun `refreshSettings sends nothing while the session is not Ready`() = runTest {
        val (repo, transport) = buildRepository(connectionStateMachine = buildConnectionStateMachine(driveToReady = false))
        answerReadsLikeCap036(transport)

        assertEquals(BudsResult.Failure(BudsError.ConnectionLost), repo.refreshSettings())
        assertEquals(0, transport.sent.size)
        assertEquals(BudsSettings(), repo.settings.value)
    }

    @Test
    fun `an unanswered field in a re-read keeps its earlier value and time, is not retried, and the pass goes on`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        answerReadsLikeCap036(transport)
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()
        advanceTimeBy(1_000)
        repo.refreshSettings() // first pass at t = 1 000: every field answered
        transport.sent.clear()
        // Second pass at t = 5 000: the Buds do not answer field 17 (balance); every other field is answered with its CAP-036 frame.
        transport.onSent = { ch, frame ->
            val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
            val field = rpc.payload[1].toInt()
            if (ch == Dlci.MAESTRO && rpc.methodId == Maestro.METHOD_READ_SETTING && field != 17) {
                Settings036.ANSWERS[field]?.let { transport.emit(Dlci.MAESTRO, hex(it)) }
            }
        }
        advanceTimeBy(4_000)

        val result = repo.refreshSettings()

        assertEquals(BudsResult.Failure(BudsError.Timeout), result)
        assertEquals(cap036SettingReads, transport.sent.map { it.second.toHex() }, "one pass, field 17 not retried")
        assertEquals(SettingReading(5, 1_000), repo.settings.value.volumeBalance, "the earlier value with its own time")
        assertEquals(SettingReading(true, 5_000), repo.settings.value.inEarDetection)
        assertEquals(SettingReading(true, 7_000), repo.settings.value.conversationDetection, "read after field 17's 2 s wait")
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure(BudsError.Timeout, write = false), repo.settingsError.first())
    }

    @Test
    fun `a new connect resets the settings to not read`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_22_RESP))
        settle()
        assertEquals(true, repo.settings.value.conversationDetection?.value)
        repo.connect() // stops at NotPaired, after the reset
        assertEquals(BudsSettings(), repo.settings.value)
    }

    @Test
    fun `a stream update for the saved EQ (field 18) never replaces the active EQ`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.emit(Dlci.MAESTRO, rpcFrame(10496, RpcPacket(PwRpc.TYPE_SERVER_STREAM, 21, Maestro.SERVICE_ID, Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES, quintetPayload(heavyBass, 16))))
        settle()
        assertEquals(heavyBass, repo.eqProfile.value)

        transport.emit(Dlci.MAESTRO, rpcFrame(10496, RpcPacket(PwRpc.TYPE_SERVER_STREAM, 21, Maestro.SERVICE_ID, Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES, quintetPayload(EqBandGains.FLAT, 18))))
        settle()
        assertEquals(heavyBass, repo.eqProfile.value)
    }

    @Test
    fun `every inbound pw_rpc packet is logged as structure only, always on (no payload, no bytes)`() = runTest {
        val (_, transport) = buildRepository()
        settle()
        BleLogger.clear()
        transport.emit(Dlci.MAESTRO, rpcFrame(10496, RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, status = 5)))
        settle()

        val log = BleLogger.exportLog()
        assert("pw_rpc RESPONSE ch=21 method=WriteSetting status=NOT_FOUND" in log) { log }
    }

    @Test
    fun `a new connect resets the EQ to unknown before anything can be Ready (0044 APP-7)`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        transport.emit(Dlci.MAESTRO, rpcFrame(10496, RpcPacket(PwRpc.TYPE_SERVER_STREAM, 21, Maestro.SERVICE_ID, Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES, quintetPayload(heavyBass))))
        settle()
        assertEquals(heavyBass, repo.eqProfile.value)

        repo.connect() // no bonded device here, so it stops at NotPaired — but the reset has already happened, synchronously

        assertNull(repo.eqProfile.value)
    }

    @Test
    fun `a state transition to Ready never wipes an EQ value read for this connection (the old asynchronous reset could, 0044 APP-7)`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        // The Connect-time read's answer lands first …
        transport.emit(Dlci.MAESTRO, rpcFrame(10496, RpcPacket(PwRpc.TYPE_SERVER_STREAM, 21, Maestro.SERVICE_ID, Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES, quintetPayload(heavyBass))))
        settle()
        // … and only then is the Ready transition observed (the order a slow Dispatchers.Default collector could produce).
        machine.onConnectRequested()
        machine.onLinkEstablished()
        machine.onReady()
        settle()

        assertEquals(heavyBass, repo.eqProfile.value)
    }

    @Test
    fun `connect fails with NotPaired (not PermissionDenied) when no bonded device exists yet`() = runTest {
        // Not Ready: connect() on an already-Ready repository is a deliberate no-op success (see the
        // dedicated test below), so this scenario needs a machine that is still Disconnected.
        val (repo, _) = buildRepository(connectionStateMachine = buildConnectionStateMachine(driveToReady = false))
        settle()

        val result = repo.connect()
        assertInstanceOf(BudsResult.Failure::class.java, result)
        assertEquals(BudsError.NotPaired, (result as BudsResult.Failure).error) // 0044 APP-8
    }

    @Test
    fun `a transport-reported connection loss moves connectionState to Disconnected`() = runTest {
        val connectionStateMachine = buildConnectionStateMachine()
        val (repo, transport) = buildRepository(connectionStateMachine = connectionStateMachine)
        settle()

        val job = launch { repo.connectionState.first { it is ConnectionState.Disconnected } }
        runCurrent()
        transport.emitConnectionLost()
        job.join()

        assertInstanceOf(ConnectionState.Disconnected::class.java, connectionStateMachine.state.value)
    }

    /** Emits [loss] and returns only after the repository's own `connectionLost` collector has
     * had it delivered: a second, later subscriber is resumed after the repository's (FIFO) and
     * `join()` drives the scheduler until it has seen the event too. */
    private suspend fun TestScope.deliverLoss(transport: FakeBudsTransport, loss: ConnectionLoss) {
        val witness = launch { transport.connectionLost.first() }
        runCurrent()
        transport.emitConnectionLost(loss)
        witness.join()
        runCurrent()
    }

    @Test
    fun `a connection loss records why, naming the channel and the underlying reason`() = runTest {
        val machine = buildConnectionStateMachine()
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        settle()

        deliverLoss(transport, ConnectionLoss(channelId = Dlci.FAST_PAIR_MESSAGE_STREAM, detail = "IOException: bt socket closed, read return: -1"))

        // ai-sessions/0039: never a bare Disconnected — the reason must be available to the UI.
        assertEquals(ConnectionState.Disconnected, machine.state.value)
        assertEquals(
            BudsError.ChannelLost(Dlci.FAST_PAIR_MESSAGE_STREAM, "IOException: bt socket closed, read return: -1"),
            repo.lastConnectionError.first(),
        )
    }

    @Test
    fun `a loss report arriving while Connecting is ignored and cannot knock a fresh attempt back to Disconnected`() = runTest {
        // The round-1/2 logs showed "Failed -> Disconnected" 7-50 ms after a failed attempt: a stale
        // loss from the previous session being applied to the new one.
        val machine = buildConnectionStateMachine(driveToReady = false)
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        settle()
        machine.onConnectRequested()

        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, "stale"))

        assertEquals(ConnectionState.Connecting, machine.state.value)
        assertNull(repo.lastConnectionError.first())
    }

    @Test
    fun `a loss report arriving while Failed is ignored`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        val (_, transport) = buildRepository(connectionStateMachine = machine)
        settle()
        machine.onConnectRequested()
        machine.onError(BudsError.ChannelUnavailable(Dlci.MAESTRO, "scripted"))

        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, "stale"))

        assertEquals(ConnectionState.Failed(BudsError.ChannelUnavailable(Dlci.MAESTRO, "scripted")), machine.state.value)
    }

    @Test
    fun `an explicit disconnect clears the last connection error`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, "scripted"))
        assertEquals(BudsError.ChannelLost(Dlci.MAESTRO, "scripted"), repo.lastConnectionError.first())

        repo.disconnect()

        assertNull(repo.lastConnectionError.first())
    }

    @Test
    fun `connect while already Ready is a no-op success and does not tear the live connection down`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        val result = repo.connect() // bondedDeviceProvider is null in these tests: only the Ready short-circuit can succeed

        assertInstanceOf(BudsResult.Success::class.java, result)
        assertEquals(true, transport.connected)
    }

    @Test
    fun `ringBud and stopRinging send the exact CAP-025 wire bytes`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        repo.ringBud(RingTarget.LEFT)
        assertEquals("0401000102", transport.sent[0].second.toHex())

        repo.stopRinging()
        assertEquals("0401000100", transport.sent[1].second.toHex())
    }

    // ---- Case battery from the runtime-info stream (DECISIONS.md ADR-043), the Settable byte (ADR-049), Find state, firmware ----

    /** `Notify ANC state` `01 e8 <settable> <mode>` — real values from CAP-059 frames 1520 (`e8 00 20`) and 2782 (`e8 e8 08`). */
    private fun ancNotify(settable: String, mode: String) = hex("08130004" + "01e8" + settable + mode)

    /** Real `SubscribeRuntimeInfo` SERVER_STREAM packets (ai-sessions/0046): CAP-041 frame 782 (Case 79) and CAP-050 frame 1163 (no Case entry). */
    private val runtimeInfoWithCase = hex("2a2510ce829bba8734180032120a04084f10011204086410021a04086410023a06080110011800080710151dea71de7e2590821ee6")
    private val runtimeInfoWithoutCase = hex("2a1f10a584ae8a8a341800320c1204082510011a04082d10013a06080010001800080710151dea71de7e2590821ee6")

    @Test
    fun `refreshBattery claims only the Message Stream and never opens DLCI 0x08 (ADR-043)`() = runTest {
        val (repo, transport) = buildRepository()
        val job = launch { repo.refreshBattery() }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "08"))
        settle()
        job.join()

        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.openChannelCalls)
        assertEquals(emptyList<Int>(), transport.sent.map { it.first }.filter { it == Dlci.GSND_CONTROL })
    }

    @Test
    @DisplayName("Connect: EQ read, then exactly one SubscribeRuntimeInfo, byte-identical to the official app's CAP-036 frame 1410 (ADR-043)")
    fun `the Connect-time sequence subscribes to runtime info once, after the EQ read`() = runTest {
        val (repo, transport) = buildRepository()
        answerReadsLikeCap036(transport)
        repo.launchInitialEqRead()
        settle()

        val maestro = transport.sent.filter { it.first == Dlci.MAESTRO }.map { it.second.toHex() }
        assertEquals(3 + settingReads.size, maestro.size, "EQ read, the settings reads (field 12 added by ADR-046, 11 by ai-sessions/0074), the subscription, GetHardwareInfo (ADR-058)")
        assertEquals(1, maestro.count { it == "7e004b0310151dea71de7d5e2590821ee66654bfab7e" })
        assertEquals("7e004b0310151dea71de7d5e2590821ee66654bfab7e", maestro[maestro.size - 2], "the subscription right after the settings reads")
        assertEquals(HardwareInfoFixtures.REQUEST_CH21_CAP072_A7593, maestro.last(), "ADR-058: GetHardwareInfo last")
        assertEquals(0.3f, repo.eqProfile.first()!!.mid, 1e-4f)
    }

    @Test
    @DisplayName("runtime info: CAP-041 frame 782 sets the Case (79 %); CAP-050 frame 1163 (no Case entry) keeps it as last seen; b3 = ff never overwrites it")
    fun `the Case follows the runtime-info stream`() = runTest {
        val (repo, transport) = buildRepository()
        advanceTimeBy(5_000)
        transport.emit(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, runtimeInfoWithCase))
        settle()
        assertEquals(BatteryLevel.Known(79, null, false, receivedAtMillis = 5_000), repo.batteryStatus.value.case)
        assertNull(repo.caseBatteryError.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "6464ff")) // real CAP-059 frame 2776: b3 = ff on every claim
        settle()
        assertEquals(BatteryLevel.Known(79, null, false, receivedAtMillis = 5_000), repo.batteryStatus.value.case, "a Left/Right update never touches the Case")

        advanceTimeBy(7_000)
        transport.emit(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, runtimeInfoWithoutCase))
        settle()
        assertEquals(
            BatteryLevel.Known(79, null, isStale = true, receivedAtMillis = 5_000),
            repo.batteryStatus.value.case,
            "I-5: last seen, with the time it was last reported — never presented as current (AGENTS.md §5)",
        )
    }

    // ---- I-4 / I-5 / I-8 (ai-sessions/0048): the CAP-062 stream, in the order it arrived -----------------------------------------

    @Test
    @DisplayName("I-4/I-5: CAP-062 3760 -> 4845 -> 7033 -> 7118: per-bud charging follows each push; the Case stays 60 % last seen after 7118")
    fun `per-bud charging and the last-seen Case from the CAP-062 stream`() = runTest {
        val (repo, transport) = buildRepository()
        suspend fun push(frame: String) = transport.emit(Dlci.MAESTRO, hex(frame))
        val left = { repo.batteryStatus.value.leftCharging }
        val right = { repo.batteryStatus.value.rightCharging }

        advanceTimeBy(1_000); push(Cap062.STREAM_RIGHT_3760); settle()
        assertEquals(ChargingReading(false, 1_000, ChargingSource.RUNTIME_INFO), left())
        assertEquals(ChargingReading(true, 1_000, ChargingSource.RUNTIME_INFO), right())
        assertEquals(BatteryLevel.Known(60, null, false, 1_000), repo.batteryStatus.value.case)

        advanceTimeBy(1_000); push(Cap062.STREAM_BOTH_4845); settle()
        assertEquals(true, left()?.charging); assertEquals(true, right()?.charging)

        advanceTimeBy(1_000); push(Cap062.STREAM_LEFT_7033); settle()
        assertEquals(ChargingReading(true, 3_000, ChargingSource.RUNTIME_INFO), left())
        assertEquals(ChargingReading(false, 3_000, ChargingSource.RUNTIME_INFO), right())
        assertEquals(BatteryLevel.Known(60, null, false, 3_000), repo.batteryStatus.value.case)

        advanceTimeBy(9_846); push(Cap062.STREAM_NONE_7118); settle()
        assertEquals(false, left()?.charging); assertEquals(false, right()?.charging)
        assertEquals(BatteryLevel.Known(60, null, isStale = true, receivedAtMillis = 3_000), repo.batteryStatus.value.case)
        assertEquals(emptyList<Pair<Int, ByteArray>>(), transport.sent, "nothing is requested for any of it (no polling, no new request)")
    }

    @Test
    @DisplayName("I-8: the newest charging report wins — a DLCI 0x04 frame (64 e4) after the stream, then the stream (7033) again")
    fun `charging follows the newest of the stream and the Message Stream`() = runTest {
        val (repo, transport) = buildRepository()
        advanceTimeBy(1_000); transport.emit(Dlci.MAESTRO, hex(Cap062.STREAM_BOTH_4845)); settle()
        assertEquals(true, repo.batteryStatus.value.rightCharging?.charging)

        advanceTimeBy(1_000); transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0303000364" + "64ff")); settle() // CAP-062 4532: neither charging
        assertEquals(ChargingReading(false, 2_000, ChargingSource.MESSAGE_STREAM), repo.batteryStatus.value.rightCharging)
        assertEquals(BatteryLevel.Known(100, false, false, 2_000), repo.batteryStatus.value.right, "the percentage keeps its own DLCI 0x04 time")

        advanceTimeBy(1_000); transport.emit(Dlci.MAESTRO, hex(Cap062.STREAM_LEFT_7033)); settle()
        assertEquals(ChargingReading(true, 3_000, ChargingSource.RUNTIME_INFO), repo.batteryStatus.value.leftCharging)
        assertEquals(BatteryLevel.Known(100, false, false, 2_000), repo.batteryStatus.value.left, "the stream never changes a percentage")
    }

    // ---- I-4 (ai-sessions/0054): at Connect the previous connection's per-bud lines are marked until the Buds report again ------------------------
    // CAP-063: 16:00:17 both docked (DLCI 0x04 2756 `e4 e4`, stream 2723 both charging, Case 36); Connect 16:01:02.42 with both buds out; the first
    // new reports are the battery burst 3046 (16:01:03.872, `64 64`) and the stream 3059 (16:01:04.208, neither charging). Virtual 0 = 16:00:17.184.

    private suspend fun TestScope.previousSessionOf2723(transport: FakeBudsTransport) {
        transport.emit(Dlci.MAESTRO, hex(Cap063.STREAM_BOTH_CHARGING_2723)) // 16:00:17.184
        advanceTimeBy(130); transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap063.BATTERY_BOTH_CHARGING_2756)) // 16:00:17.315
        settle()
    }

    @Test
    @DisplayName("I-4: Connect marks both buds' % 'last seen' and charging 'last connection'; CAP-063 3046 then 3059 make them current again")
    fun `the previous connection's per-bud lines are marked until the new reports`() = runTest {
        val (repo, transport) = buildRepository(connectionStateMachine = buildConnectionStateMachine(driveToReady = false))
        previousSessionOf2723(transport)
        assertEquals(ChargingReading(true, 130, ChargingSource.MESSAGE_STREAM), repo.batteryStatus.value.leftCharging)

        advanceTimeBy(45_105)
        repo.connect() // 16:01:02.42 (stops at the bonded-device lookup in a JVM test — the marking has happened)

        val marked = repo.batteryStatus.value
        assertEquals(BatteryLevel.Known(100, true, isStale = true, receivedAtMillis = 130), marked.left)
        assertEquals(BatteryLevel.Known(100, true, isStale = true, receivedAtMillis = 130), marked.right)
        assertEquals(ChargingReading(true, 130, ChargingSource.MESSAGE_STREAM, fromEarlierSession = true), marked.leftCharging)
        assertEquals(ChargingReading(true, 130, ChargingSource.MESSAGE_STREAM, fromEarlierSession = true), marked.rightCharging)
        assertEquals(BatteryLevel.Known(36, null, isStale = true, receivedAtMillis = 0), marked.case, "the Case as in 0048 I-5")

        advanceTimeBy(1_452); transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap063.BATTERY_NONE_CHARGING_3046)); settle() // 16:01:03.872
        assertEquals(BatteryLevel.Known(100, false, receivedAtMillis = 46_687), repo.batteryStatus.value.left)
        assertEquals(ChargingReading(false, 46_687, ChargingSource.MESSAGE_STREAM), repo.batteryStatus.value.rightCharging)

        advanceTimeBy(336); transport.emit(Dlci.MAESTRO, hex(Cap063.STREAM_NONE_CHARGING_3059)); settle() // 16:01:04.208
        assertEquals(ChargingReading(false, 47_023, ChargingSource.RUNTIME_INFO), repo.batteryStatus.value.leftCharging)
        assertEquals(BatteryLevel.Known(36, null, isStale = true, receivedAtMillis = 0), repo.batteryStatus.value.case, "no 6.1: still last seen")
    }

    @Test
    @DisplayName("I-4: a Connect whose first report is the stream (3059) clears the charging mark at once; the % stays 'last seen' until DLCI 0x04 reports")
    fun `a stream-first Connect clears the charging mark`() = runTest {
        val (repo, transport) = buildRepository(connectionStateMachine = buildConnectionStateMachine(driveToReady = false))
        previousSessionOf2723(transport)
        advanceTimeBy(45_105); repo.connect()

        advanceTimeBy(1_000); transport.emit(Dlci.MAESTRO, hex(Cap063.STREAM_NONE_CHARGING_3059)); settle()

        val status = repo.batteryStatus.value
        assertEquals(ChargingReading(false, 46_235, ChargingSource.RUNTIME_INFO), status.leftCharging)
        assertEquals(ChargingReading(false, 46_235, ChargingSource.RUNTIME_INFO), status.rightCharging)
        assertEquals(BatteryLevel.Known(100, true, isStale = true, receivedAtMillis = 130), status.left, "each value keeps its own staleness and time")
    }

    @Test
    fun `before any Case report the Case is unavailable, a stream without 6_1 does not invent one (I-5)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Cap062.STREAM_NONE_2782)); settle()
        assertEquals(BatteryLevel.Unavailable, repo.batteryStatus.value.case)
    }

    @Test
    fun `the last-seen Case survives a Disconnect, marked last seen, kept in memory only (I-5)`() = runTest {
        val (repo, transport) = buildRepository()
        advanceTimeBy(1_000); transport.emit(Dlci.MAESTRO, hex(Cap062.STREAM_BOTH_4845)); settle()
        repo.disconnect()
        val case = repo.batteryStatus.value.case as BatteryLevel.Known
        assertEquals(60, case.percent)
        assertEquals(1_000L, case.receivedAtMillis)
    }

    @Test
    fun `without an announced channel the runtime-info request is not sent and the Case error says why`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        repo.launchInitialEqRead()
        advanceTimeBy(3_100) // MAESTRO_ANNOUNCE_WAIT_MS for the EQ read …
        runCurrent()
        advanceTimeBy(3_100) // … for the settings reads …
        runCurrent()
        advanceTimeBy(3_100) // … for the subscription …
        runCurrent()
        advanceTimeBy(3_100) // … and for the hardware-info read (ADR-058)
        runCurrent()

        assertEquals(0, transport.sent.size)
        assertEquals(BudsError.MaestroChannelUnknown(null), repo.settingsError.first()?.error)
        assertEquals(BudsError.MaestroChannelUnknown(null), repo.caseBatteryError.first())
        assertEquals(BudsError.MaestroChannelUnknown(null), repo.serialsError.first())
    }

    // ---- ADR-058 (`ai-sessions/0082` item 2): one GetHardwareInfo per Connect, the serial numbers on the Info tab ----

    /** The Buds of `CAP-036`/`CAP-024`: every ReadSetting answered, the GetHardwareInfo by [answer] (`null` = silence). */
    private fun answerHardwareInfo(transport: FakeBudsTransport, answer: String?) {
        transport.onSent = { ch, frame ->
            if (ch == Dlci.MAESTRO) {
                val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
                if (rpc.methodId == Maestro.METHOD_READ_SETTING) Settings036.ANSWERS[rpc.payload[1].toInt()]?.let { transport.emit(Dlci.MAESTRO, hex(it)) }
                if (rpc.methodId == Maestro.METHOD_GET_HARDWARE_INFO) answer?.let { transport.emit(Dlci.MAESTRO, hex(it)) }
            }
        }
    }

    @Test
    @DisplayName("ADR-058: Connect sends exactly one GetHardwareInfo, byte-identical to CAP-072 A 7593 (ch 21, = CAP-036 1415), after the subscription; A 7596's serials reach deviceInfo with their time")
    fun `Connect reads the serial numbers once, on channel 21`() = runTest {
        BleLogger.clear()
        val (repo, transport) = buildRepository(verified = false)
        // The whole RFCOMM read of A 7596: a runtime-info packet, then the answer — as OpenControl received it in CAP-072.
        answerHardwareInfo(transport, HardwareInfoFixtures.RFCOMM_CH21_CAP072_A7596_REDACTED)
        transport.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_5.203"))
        settle()
        advanceTimeBy(4_000)
        repo.launchInitialEqRead()
        settle()

        val hardwareRequests = transport.sent.filter { it.first == Dlci.MAESTRO && it.second.toHex() == HardwareInfoFixtures.REQUEST_CH21_CAP072_A7593 }
        assertEquals(1, hardwareRequests.size, "exactly one, byte-identical to the official app's")
        val info = repo.deviceInfo.first()!!
        assertEquals(HardwareInfoFixtures.SERIALS_REDACTED, info.serials.map { it.serial })
        assertEquals(listOf(1, 2, 3), info.serials.map { it.index })
        assertEquals(currentTime, info.serialsReadAtMillis)
        assertEquals(listOf("release_5.203"), info.firmware, "the announcement's firmware stays")
        assertNull(repo.serialsError.first())
        // AGENTS.md §9 / mutation M4: no serial in any log line — the always-on log says how many, never which.
        val log = BleLogger.exportLog()
        for (serial in HardwareInfoFixtures.SERIALS_REDACTED) assertEquals(false, serial in log, "serial in the log")
        assertEquals(true, "Hardware info read (channel 21): 3 serial numbers" in log)
    }

    @Test
    @DisplayName("ADR-058: on channel 19 the request is CAP-072 A 8616 (= CAP-024 801) and the answer A 8620 gives the same three serials in the same order")
    fun `the serial numbers are read on channel 19 too`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        answerHardwareInfo(transport, HardwareInfoFixtures.RESPONSE_CH19_CAP072_A8620_REDACTED)
        transport.emit(Dlci.MAESTRO, helloWithFirmware(19, 10432, "release_5.203")) // response address `80 a3` (channel 19, ADR-034)
        settle()
        repo.launchInitialEqRead()
        settle()
        assertEquals(1, transport.sent.count { it.second.toHex() == HardwareInfoFixtures.REQUEST_CH19_CAP072_A8616 })
        assertEquals(HardwareInfoFixtures.SERIALS_REDACTED, repo.deviceInfo.first()!!.serials.map { it.serial })
    }

    @Test
    @DisplayName("ADR-058: no answer within 2 s -> Timeout in serialsError, never retried, the serials stay empty; an OK answer without field 7 -> UnreadableAnswer")
    fun `an unanswered or unreadable hardware-info read keeps the serials empty and says why`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        answerHardwareInfo(transport, null)
        transport.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_5.203"))
        settle()
        repo.launchInitialEqRead()
        settle()
        advanceTimeBy(2_100)
        runCurrent()
        assertEquals(1, transport.sent.count { it.second.toHex() == HardwareInfoFixtures.REQUEST_CH21_CAP072_A7593 }, "sent once, never retried")
        assertEquals(BudsError.Timeout, repo.serialsError.first())
        assertEquals(emptyList<io.github.tedsluis.opencontrolpixelbuds.domain.ComponentSerial>(), repo.deviceInfo.first()!!.serials)

        // Labelled structural case (no capture holds it): the answer without field 7.
        val (repo2, transport2) = buildRepository(verified = false)
        val withoutSerials = RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_GET_HARDWARE_INFO, hex("0806101128093007"))
        answerHardwareInfo(transport2, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, PwRpc.encode(withoutSerials)).toHex())
        transport2.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_5.203"))
        settle()
        repo2.launchInitialEqRead()
        settle()
        assertEquals(BudsError.UnreadableAnswer, repo2.serialsError.first())
        assertEquals(emptyList<io.github.tedsluis.opencontrolpixelbuds.domain.ComponentSerial>(), repo2.deviceInfo.first()!!.serials)
    }

    @Test
    @DisplayName("ADR-058 item 2 / ADR-042 item 3: a read is not gated — the request is sent to an unverified firmware too (M10 guards the opposite)")
    fun `the hardware-info read is sent on an unverified firmware`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        answerHardwareInfo(transport, HardwareInfoFixtures.RESPONSE_CH21_CAP072_A7596_REDACTED)
        transport.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_9.999"))
        settle()
        repo.launchInitialEqRead()
        settle()
        assertEquals(1, transport.sent.count { it.second.toHex() == HardwareInfoFixtures.REQUEST_CH21_CAP072_A7593 })
        assertEquals(HardwareInfoFixtures.SERIALS_REDACTED, repo.deviceInfo.first()!!.serials.map { it.serial })
        assertEquals(listOf("release_9.999"), repo.safeMode.first()?.firmware, "Safe Mode is observed (the Connection card) — it refuses writes, not this read")
    }

    @Test
    @DisplayName("ADR-058: Disconnect clears deviceInfo (serials included) and the error; nothing is persisted")
    fun `Disconnect drops the serials with the rest of deviceInfo`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        answerHardwareInfo(transport, HardwareInfoFixtures.RESPONSE_CH21_CAP072_A7596_REDACTED)
        transport.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_5.203"))
        settle()
        repo.launchInitialEqRead()
        settle()
        assertEquals(3, repo.deviceInfo.first()!!.serials.size)
        repo.disconnect()
        assertNull(repo.deviceInfo.first())
        assertNull(repo.serialsError.first())
    }

    @Test
    @DisplayName("I-3 / ADR-024: the Notify's Settable-toggles byte — 0x00 not allowed, non-zero allowed, unknown before any Notify")
    fun `ANC availability follows the Notify byte`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        runCurrent() // start the repository's backgroundScope collectors before the first emit
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability.UNKNOWN, repo.ancAvailability.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("00", "20")) // CAP-059 frame 1520 (buds seated)
        settle()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability.NOT_ALLOWED, repo.ancAvailability.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "08")) // frame 2782 (buds out)
        settle()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability.ALLOWED, repo.ancAvailability.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("12", "08")) // any non-zero value re-enables (I-3)
        settle()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability.ALLOWED, repo.ancAvailability.first())
    }

    @Test
    fun `a ring is remembered until Stop succeeds, and a failed ring is never remembered`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        runCurrent() // start the repository's backgroundScope collectors before the first emit
        assertNull(repo.ringing.first())

        repo.ringBud(RingTarget.RIGHT)
        assertEquals(RingNotice(RingTarget.RIGHT), repo.ringing.first())
        repo.stopRinging()
        assertNull(repo.ringing.first())

        transport.sendShouldFail = BudsError.ConnectionLost
        repo.ringBud(RingTarget.LEFT)
        assertNull(repo.ringing.first(), "a ring whose command never left the phone is not claimed")
    }

    // ---- ai-sessions/0062 F-1: every ANC tap's claim sends the Get first; the Set only on a non-zero Settable (replaces 0054 I-1's "only when known 00") ----

    /** The Buds of `CAP-063`: the app's `Get` is answered by [getAnswer]; an ANC `Set` ADAPTIVE (4233) by its ACK (4241), ACTIVE (4497) by 4508. */
    private fun answerLikeCap063(transport: FakeBudsTransport, getAnswer: String?) {
        transport.onSent = { ch, frame ->
            when (frame.toHex()) {
                Cap063.GET_ANC -> getAnswer?.let { transport.emit(ch, hex(it)) }
                Cap063.SET_ADAPTIVE_4233 -> transport.emit(ch, hex(Cap063.ACK_ADAPTIVE_4241))
                Cap063.SET_ACTIVE_4497 -> transport.emit(ch, hex(Cap063.ACK_ACTIVE_4508))
            }
        }
    }

    private suspend fun TestScope.notAllowedFrom4774(transport: FakeBudsTransport, repo: BudsRepositoryImpl) {
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap063.NOTIFY_SETTABLE_00_4774)) // 16:06:11.245, buds on the table
        settle()
        assertEquals(AncAvailability.NOT_ALLOWED, repo.ancAvailability.first())
    }

    /** The Buds of `CAP-065`: the `Get` answered by [getAnswer]; the `Set` ACTIVE (10790) by its ACK (10799) unless [setAnswer] says otherwise. */
    private fun answerLikeCap065(
        transport: FakeBudsTransport,
        getAnswer: String?,
        setAnswer: (suspend (Int) -> Unit)? = { ch -> transport.emit(ch, hex(Cap065.ACK_ACTIVE_10799)) },
    ) {
        transport.onSent = { ch, frame ->
            when (frame.toHex()) {
                Cap065.GET -> getAnswer?.let { transport.emit(ch, hex(it)) }
                Cap065.SET_ACTIVE_10790 -> setAnswer?.invoke(ch)
            }
        }
    }

    @Test
    @DisplayName("F-1 (a): ALLOWED (CAP-065 6334); a tap sends 08 11 first, the claim's Notify is 6334 (e8) -> exactly one Set = 10790, applied on ACK 10799")
    fun `an allowed tap sends the Get first and exactly one Set`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap065.NOTIFY_E8_OFF_6334)); settle()
        assertEquals(AncAvailability.ALLOWED, repo.ancAvailability.first())
        answerLikeCap065(transport, getAnswer = Cap065.NOTIFY_E8_OFF_6334)

        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ACTIVE))

        assertEquals(listOf(Cap065.GET, Cap065.SET_ACTIVE_10790), transport.sent.map { it.second.toHex() }, "Get first, then one Set, byte-identical")
        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.openChannelCalls, "both in one claim")
        assertEquals(AncMode.ACTIVE, repo.ancMode.first(), "applied on the ACK")
        assertNull(repo.ancModeUnconfirmedAt.first())
    }

    @Test
    @DisplayName("F-1 (b): the claim's Notify is CAP-065 5465 (00) -> no Set, AncNotAllowed (the tile's toast), not a channel error, the claim released")
    fun `a tap whose Notify reads 00 sends no Set`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap065.NOTIFY_E8_OFF_6334)); settle() // the app last saw "allowed"
        answerLikeCap065(transport, getAnswer = Cap065.NOTIFY_00_OFF_5465)
        advanceTimeBy(5_000)

        assertEquals(BudsError.AncNotAllowed, (repo.setAncMode(AncMode.ACTIVE) as BudsResult.Failure).error)
        assertEquals(BudsError.AncNotAllowed, (repo.stepAncMode(AncMode::nextForTile) as BudsResult.Failure).error, "the tile's path: AncTileService toasts this")

        assertEquals(listOf(Cap065.GET, Cap065.GET), transport.sent.map { it.second.toHex() }, "the Get only, each time — no 08 12")
        assertEquals(AncAvailability.NOT_ALLOWED, repo.ancAvailability.first())
        assertEquals(5_000L, repo.ancAvailabilityUpdatedAt.value, "the answer is stamped: the screen's \"(checked …)\" moves")
        assertNull(repo.messageStreamError.first(), "the Buds' answer, not a channel problem")
        advanceTimeBy(1_600); runCurrent()
        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.closeChannelCalls, "the claim is released as always")
    }

    @Test
    @DisplayName("F-1 (c): CAP-064 — the app's last Notify 3200 read e8 (18 s old); the tile's tap now sends 08 11 first, gets 3443's bytes (00) and sends no Set (no NAK 3440)")
    fun `the CAP-064 stale e8 no longer leads to a Set`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap064.NOTIFY_E8_ADAPTIVE_3200)); settle()
        assertEquals(AncAvailability.ALLOWED, repo.ancAvailability.first())
        advanceTimeBy(27_000) // 10:07:47.36 -> 10:08:14.44; Play services' claim read 00 at 3299 in between — invisible to the app
        transport.onSent = { ch, frame ->
            when (frame.toHex()) {
                Cap064.GET_4091 -> transport.emit(ch, hex(Cap064.NOTIFY_00_OFF_3443))
                Cap064.SET_OFF_3433 -> transport.emit(ch, hex(Cap064.NAK_3440)) // what the Buds did to the Set without a Get
            }
        }

        val result = repo.stepAncMode(AncMode::nextForTile)

        assertEquals(BudsError.AncNotAllowed, (result as BudsResult.Failure).error)
        assertEquals(listOf(Cap064.GET_4091), transport.sent.map { it.second.toHex() }, "08 11 only — the Set 3433 (and its NAK 3440) cannot happen")
        assertEquals(AncMode.OFF, repo.ancMode.first(), "the claim's Notify (3443's bytes) is the Buds' mode")
    }

    @Test
    @DisplayName("F-1 (d): availability UNKNOWN after a failed snapshot (CAP-065 F7 sent Set 10790 without a Get) -> the Get goes first too")
    fun `an unknown availability also sends the Get first`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = null
        assertEquals(BudsError.Timeout, (repo.refreshAncMode() as BudsResult.Failure).error) // the snapshot got no Notify
        assertEquals(AncAvailability.UNKNOWN, repo.ancAvailability.first())
        advanceTimeBy(5_000)
        transport.sent.clear()
        answerLikeCap065(transport, getAnswer = Cap065.NOTIFY_E8_OFF_6334)

        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ACTIVE))
        assertEquals(listOf(Cap065.GET, Cap065.SET_ACTIVE_10790), transport.sent.map { it.second.toHex() })
    }

    @Test
    @DisplayName("F-1 (d'): NOT_ALLOWED from CAP-063 4774, the claim's Notify is 4184 (e8) -> one Set = 4233 in that claim, applied on ACK 4241 (0054 I-1 kept)")
    fun `a tap after a 00 re-checks and sends the Set when the Buds now allow it`() = runTest {
        val (repo, transport) = buildRepository()
        notAllowedFrom4774(transport, repo)
        answerLikeCap063(transport, getAnswer = Cap063.NOTIFY_SETTABLE_E8_4184)

        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ADAPTIVE))
        assertEquals(listOf(Cap063.GET_ANC, Cap063.SET_ADAPTIVE_4233), transport.sent.map { it.second.toHex() }, "Get, then exactly one Set")
        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.openChannelCalls, "both in one claim")
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first(), "applied on the ACK")
        assertEquals(AncAvailability.ALLOWED, repo.ancAvailability.first())
        assertNull(repo.messageStreamError.first())
    }

    @Test
    @DisplayName("F-1: a claim that cannot open reports its error and sends nothing; a Get the Buds never answer (channel open) is a Timeout without a Set")
    fun `a failed claim or an unanswered Get sends no Set`() = runTest {
        val (repo, transport) = buildRepository()
        notAllowedFrom4774(transport, repo)
        val busy = BudsError.ChannelUnavailable(Dlci.FAST_PAIR_MESSAGE_STREAM, "read failed, socket might closed")
        transport.openChannelShouldFail = busy

        assertEquals(busy, (repo.setAncMode(AncMode.ADAPTIVE) as BudsResult.Failure).error)
        assertEquals(0, transport.sent.size)
        assertEquals(busy, repo.messageStreamError.first())

        transport.openChannelShouldFail = null
        answerLikeCap063(transport, getAnswer = null)
        assertEquals(BudsError.Timeout, (repo.setAncMode(AncMode.ADAPTIVE) as BudsResult.Failure).error)
        assertEquals(listOf(Cap063.GET_ANC), transport.sent.map { it.second.toHex() })
        assertEquals(AncAvailability.NOT_ALLOWED, repo.ancAvailability.first())
    }

    @Test
    @DisplayName("F-1 (h): the tile's next mode comes from the claim's fresh Notify (6334: OFF -> ACTIVE), not from the mode shown (ADAPTIVE -> OFF)")
    fun `the tile steps from the fresh Notify, not from the mode shown`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap064.NOTIFY_E8_ADAPTIVE_3200)); settle() // shown: ADAPTIVE
        assertEquals(AncMode.OFF, AncMode.nextForTile(repo.ancMode.first()), "the shown mode would give OFF")
        answerLikeCap065(transport, getAnswer = Cap065.NOTIFY_E8_OFF_6334) // the Buds are in OFF by now (e.g. after re-wearing)

        assertEquals(BudsResult.Success(Unit), repo.stepAncMode(AncMode::nextForTile))

        assertEquals(listOf(Cap065.GET, Cap065.SET_ACTIVE_10790), transport.sent.map { it.second.toHex() }, "OFF (fresh) -> ACTIVE")
        assertEquals(AncMode.ACTIVE, repo.ancMode.first())
    }

    // ---- ai-sessions/0062 F-3: an answer cut off by the claim's close --------------------------------------------------------------------

    private val closedBySocketEof = "IOException: bt socket closed, read return: -1"

    @Test
    @DisplayName("F-3 (e): CAP-065 — Set 2640 written, the claim closed (DISC 2649), the ACK 2651 never delivered -> AnswerCutOff, mode unchanged and unconfirmed; Notify 2654 clears it")
    fun `a Set whose answer is cut off is AnswerCutOff and leaves the mode unconfirmed`() = runTest {
        val (repo, transport) = buildRepository()
        answerLikeCap065(transport, getAnswer = Cap065.NOTIFY_E8_OFF_6334, setAnswer = { ch -> transport.emitChannelClosed(ch, closedBySocketEof) })
        advanceTimeBy(3_000)

        val result = repo.stepAncMode(AncMode::nextForTile)

        assertEquals(BudsError.AnswerCutOff(Dlci.FAST_PAIR_MESSAGE_STREAM, closedBySocketEof), (result as BudsResult.Failure).error)
        assertEquals(listOf(Cap065.GET, Cap065.SET_ACTIVE_2640), transport.sent.map { it.second.toHex() }, "never retried: one Set")
        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.openChannelCalls, "no second claim")
        assertEquals(AncMode.OFF, repo.ancMode.first(), "the requested mode is not applied")
        assertEquals(3_000L, repo.ancModeUnconfirmedAt.first(), "marked not confirmed, with the time of the change")
        assertEquals(BudsError.AnswerCutOff(Dlci.FAST_PAIR_MESSAGE_STREAM, closedBySocketEof), repo.messageStreamError.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap065.NOTIFY_E8_ACTIVE_2654)); settle() // a later Notify (e.g. the next claim's)
        assertEquals(AncMode.ACTIVE, repo.ancMode.first())
        assertNull(repo.ancModeUnconfirmedAt.first(), "the Buds' own report clears the mark")
    }

    @Test
    @DisplayName("F-3 (f): CAP-065 — snapshot Get 10321 written, the claim closed (DISC 10344), no Notify (10356 came after) -> AnswerCutOff, not Timeout")
    fun `a Get whose answer is cut off is AnswerCutOff, not Timeout`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = { ch, frame -> if (frame.toHex() == Cap065.GET) transport.emitChannelClosed(ch, closedBySocketEof) }

        val refresh = repo.refreshAncMode()
        assertEquals(BudsError.AnswerCutOff(Dlci.FAST_PAIR_MESSAGE_STREAM, closedBySocketEof), (refresh as BudsResult.Failure).error)
        assertEquals(listOf(Cap065.GET), transport.sent.map { it.second.toHex() }, "never retried")

        transport.sent.clear()
        val tap = repo.setAncMode(AncMode.ACTIVE)
        assertEquals(BudsError.AnswerCutOff(Dlci.FAST_PAIR_MESSAGE_STREAM, closedBySocketEof), (tap as BudsResult.Failure).error)
        assertEquals(listOf(Cap065.GET), transport.sent.map { it.second.toHex() }, "no Set after a cut-off Get")
        assertNull(repo.ancModeUnconfirmedAt.first(), "a cut-off Get changed nothing the app had applied")
    }

    @Test
    @DisplayName("F-3 (g): a Set and a Get the Buds never answer while the channel stays open are Timeout, not AnswerCutOff")
    fun `a plain timeout with the channel open stays Timeout`() = runTest {
        val (repo, transport) = buildRepository()
        answerLikeCap065(transport, getAnswer = Cap065.NOTIFY_E8_OFF_6334, setAnswer = null)
        assertEquals(BudsError.Timeout, (repo.setAncMode(AncMode.ACTIVE) as BudsResult.Failure).error)
        assertNull(repo.ancModeUnconfirmedAt.first(), "a Timeout with the channel open marks nothing")
        advanceTimeBy(5_000)
        answerLikeCap065(transport, getAnswer = null)
        assertEquals(BudsError.Timeout, (repo.refreshAncMode() as BudsResult.Failure).error)
        assertEquals(true, transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM), "the claim was never closed under the waits")
    }

    @Test
    @DisplayName("F-3, labelled supplementary structural test (hand-ordered, not a capture): an answer delivered right after the close is still taken")
    fun `an answer that arrives just after the close still counts`() = runTest {
        val (repo, transport) = buildRepository()
        answerLikeCap065(transport, getAnswer = Cap065.NOTIFY_E8_OFF_6334, setAnswer = { ch ->
            transport.emitChannelClosed(ch, closedBySocketEof)
            transport.emit(ch, hex(Cap065.ACK_ACTIVE_10799)) // read before the close, still in the codec (CUT_OFF_GRACE_MS)
        })

        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ACTIVE))
        assertEquals(AncMode.ACTIVE, repo.ancMode.first())
        assertNull(repo.ancModeUnconfirmedAt.first())
    }

    @Test
    @DisplayName("F-3: Refresh battery — the burst (CAP-065 10341) came before the close (10344), the Get's Notify (10356) after it -> a new reading, no error")
    fun `a Refresh whose Get is cut off after the burst still counts`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onOpenChannel = { ch ->
            if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM) {
                transport.emit(ch, MODEL_ID_PRO_2)
                transport.emit(ch, hex(Cap065.BATTERY_10341))
            }
        }
        transport.onSent = { ch, frame -> if (frame.toHex() == Cap065.GET) transport.emitChannelClosed(ch, closedBySocketEof) }
        advanceTimeBy(1_000)

        assertEquals(BudsResult.Success(Unit), repo.refreshBattery())
        assertNull(repo.batteryRefreshError.first())
        assertEquals(BatteryLevel.Known(100, true, receivedAtMillis = 1_000), repo.batteryStatus.value.right)
        assertEquals(BudsError.AnswerCutOff(Dlci.FAST_PAIR_MESSAGE_STREAM, closedBySocketEof), repo.messageStreamError.first(), "the ANC answer was cut off")

        transport.onOpenChannel = { ch -> if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM) transport.emit(ch, MODEL_ID_PRO_2) } // no burst this time
        advanceTimeBy(5_000)
        assertEquals(BudsError.AnswerCutOff(Dlci.FAST_PAIR_MESSAGE_STREAM, closedBySocketEof), (repo.refreshBattery() as BudsResult.Failure).error)
    }

    @Test
    fun `a Refresh re-reads the byte and re-enables the Set (I-3)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = null // the test answers the Get itself
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e80020"))
        settle()
        val refresh = launch { repo.refreshAncMode() }
        runCurrent()
        assertEquals("08110000", transport.sent.single().second.toHex(), "Refresh is still sent while not allowed")
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e8e840"))
        refresh.join()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability.ALLOWED, repo.ancAvailability.first())
    }

    // ---- settings writes (DECISIONS.md ADR-045, ai-sessions/0052) ---------------------------------------------------------------------

    /** The Buds ACK every WriteSetting with the real empty RESPONSE of that channel (`CAP-022` 1629 on 19, `CAP-019` 1731 on 21). */
    private fun ackWrites(transport: FakeBudsTransport, channel: Int) {
        val ack = if (channel == 19) SettingsWrites.ACK_CH19_1629 else SettingsWrites.ACK_CH21_1731
        transport.onSent = { ch, frame ->
            val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
            if (ch == Dlci.MAESTRO && rpc.methodId == Maestro.METHOD_WRITE_SETTING) transport.emit(Dlci.MAESTRO, hex(ack))
        }
    }

    @Test
    @DisplayName("ADR-045 on channel 19: balance −100 = CAP-022 1922, mono = 1621/1823, press-and-hold = CAP-021 1895/3619/4315/4976; ACK 1629 applies each with its time")
    fun `settings writes on channel 19 are byte-identical and applied on the ACK`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle()
        ackWrites(transport, 19)
        advanceTimeBy(2_000)

        assertEquals(BudsResult.Success(Unit), repo.setVolumeBalance(-100))
        assertEquals(SettingReading(-100, 2_000, changedByApp = true), repo.settings.value.volumeBalance)
        assertEquals(BudsResult.Success(Unit), repo.setMonoAudio(true))
        assertEquals(BudsResult.Success(Unit), repo.setMonoAudio(false))
        assertEquals(BudsResult.Success(Unit), repo.setPressAndHold(Bud.LEFT, HoldAction.ASSISTANT))
        assertEquals(BudsResult.Success(Unit), repo.setPressAndHold(Bud.RIGHT, HoldAction.ASSISTANT))
        assertEquals(BudsResult.Success(Unit), repo.setPressAndHold(Bud.LEFT, HoldAction.NOISE_CONTROL))
        assertEquals(BudsResult.Success(Unit), repo.setPressAndHold(Bud.RIGHT, HoldAction.NOISE_CONTROL))

        assertEquals(
            listOf(
                SettingsWrites.BALANCE_DRAG[0].first, SettingsWrites.MONO_ON_1621, SettingsWrites.MONO_OFF_1823,
                SettingsWrites.HOLD_LEFT_ASSISTANT_1895, SettingsWrites.HOLD_RIGHT_ASSISTANT_3619,
                SettingsWrites.HOLD_LEFT_ANC_4315, SettingsWrites.HOLD_RIGHT_ANC_4976,
            ),
            transport.sent.map { it.second.toHex() },
            "one write per call, byte for byte",
        )
        assertEquals(SettingReading(false, 2_000, true), repo.settings.value.monoAudio)
        assertEquals(SettingReading(HoldAction.NOISE_CONTROL, 2_000, true), repo.settings.value.holdLeft)
        assertEquals(SettingReading(HoldAction.NOISE_CONTROL, 2_000, true), repo.settings.value.holdRight)
        assertNull(repo.settingsError.first()?.error)
    }

    @Test
    @DisplayName("I-3: a centred balance (BudsSettings.snapBalance of a release at Left 2) is CAP-046 frame 1873 byte for byte; ACK 1878 shows 'Centre'")
    fun `a snapped balance writes 17_0 like the official app`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle() // CAP-046 ran on channel 19
        ackWrites(transport, 19) // 1878 = the channel's empty RESPONSE (identical to CAP-022 1629)
        advanceTimeBy(3_000)

        assertEquals(BudsResult.Success(Unit), repo.setVolumeBalance(BudsSettings.snapBalance(2)))

        assertEquals(listOf(SettingsWrites.BALANCE_CENTRE_1873), transport.sent.map { it.second.toHex() })
        assertEquals(SettingReading(0, 3_000, changedByApp = true), repo.settings.value.volumeBalance)
    }

    @Test
    @DisplayName("Right 4 on channel 21 is CAP-064 6671 (17:7) byte for byte; the empty RESPONSE 6673 applies it (the CAP-066 BB-10 target)")
    fun `a balance of Right 4 writes the captured 17_7`() = runTest {
        val (repo, transport) = buildRepository() // the CAP-061 announcement: channel 21 (CAP-064 6671 ran on 21)
        transport.onSent = { ch, frame ->
            val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
            if (ch == Dlci.MAESTRO && rpc.methodId == Maestro.METHOD_WRITE_SETTING) transport.emit(Dlci.MAESTRO, hex(Cap066Balance.ACK_CH21_6673))
        }
        advanceTimeBy(1_000)

        assertEquals(BudsResult.Success(Unit), repo.setVolumeBalance(-4))

        assertEquals(listOf(Cap066Balance.RIGHT_4_CH21_6671), transport.sent.map { it.second.toHex() }, "one write, byte for byte")
        assertEquals(SettingReading(-4, 1_000, changedByApp = true), repo.settings.value.volumeBalance)
    }

    @Test
    @DisplayName("ADR-045 on channel 21: conversation detection OFF = CAP-019 1720, touch controls OFF = CAP-020 1995; ACK 1731")
    fun `settings writes on channel 21 are byte-identical`() = runTest {
        val (repo, transport) = buildRepository() // the CAP-061 announcement: channel 21
        ackWrites(transport, 21)

        assertEquals(BudsResult.Success(Unit), repo.setConversationDetection(false))
        assertEquals(BudsResult.Success(Unit), repo.setTouchControls(false))
        assertEquals(BudsResult.Success(Unit), repo.setTouchControls(true))

        assertEquals(
            listOf(SettingsWrites.CONV_OFF_1720, SettingsWrites.TOUCH_OFF_1995, SettingsWrites.TOUCH_ON_1741),
            transport.sent.map { it.second.toHex() },
        )
        assertEquals(false, repo.settings.value.conversationDetection?.value)
        assertEquals(true, repo.settings.value.touchControls?.value)
    }

    @Test
    fun `a write the Buds never answer is a Timeout and the value read at Connect stays`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_19_RESP)); settle() // read: mono off
        val read = repo.settings.value.monoAudio
        transport.onSent = null

        val result = repo.setMonoAudio(true)

        assertEquals(BudsError.Timeout, (result as BudsResult.Failure).error)
        assertEquals(1, transport.sent.size, "sent once, never retried")
        assertEquals(read, repo.settings.value.monoAudio, "never optimistic")
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure(BudsError.Timeout, write = true), repo.settingsError.first())
    }

    /**
     * A58-APP-04 (`ai-sessions/0059`, ADR-045 Update 2026-09-30): a write that timed out may still be answered late, and pw_rpc answers carry no `call_id`.
     * The next request waits until 1.5 s after that timeout; the late answer (the real `CAP-019` frame 1731, channel 21's empty `RESPONSE` OK) lands in that
     * window, nobody takes it, and the next write — never answered here — is a `Timeout`, not a success borrowed from the first write's answer.
     */
    @Test
    fun `a late answer to a timed-out write is not taken by the next write (write quarantine)`() = runTest {
        val (repo, transport) = buildRepository() // the CAP-061 announcement: channel 21
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_19_RESP)); settle() // read: mono off
        val read = repo.settings.value.monoAudio
        val sentAt = mutableListOf<Long>()
        transport.onSent = { _, _ -> sentAt += currentTime } // the Buds answer nothing in time
        val start = currentTime

        val first = repo.setMonoAudio(true) // no answer within 1.5 s
        assertEquals(BudsError.Timeout, (first as BudsResult.Failure).error)
        val second = async { repo.setMonoAudio(false) } // a new tap 0.1 s later
        advanceTimeBy(100); runCurrent()
        assertEquals(1, transport.sent.size, "the next request is held, not sent")
        advanceTimeBy(400); runCurrent()
        transport.emit(Dlci.MAESTRO, hex(SettingsWrites.ACK_CH21_1731)) // the first write's late answer, 2.0 s after it was sent
        runCurrent()

        assertEquals(BudsError.Timeout, (second.await() as BudsResult.Failure).error, "the late answer was not borrowed")
        assertEquals(listOf(start, start + 3_000), sentAt, "the second write left only when the quarantine ended (1.5 s timeout + 1.5 s)")
        assertEquals(read, repo.settings.value.monoAudio, "never applied")
    }

    @Test
    @DisplayName("an error status keeps the previous value and shows the reason (supplementary: hand-built RESPONSE with status 5 — no capture has a rejected write)")
    fun `a rejected write keeps the previous value`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_22_RESP)); settle()
        transport.onSent = { _, _ ->
            transport.emit(Dlci.MAESTRO, rpcFrame(10496, RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, status = 5)))
        }

        val result = repo.setConversationDetection(false)

        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), (result as BudsResult.Failure).error)
        assertEquals(true, repo.settings.value.conversationDetection?.value)
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), repo.settingsError.first()?.error)
    }

    @Test
    fun `Safe Mode refuses every settings write and sends nothing (ADR-042)`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        transport.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_6.000"))
        settle()

        for (r in listOf(
            repo.setVolumeBalance(40), repo.setMonoAudio(true), repo.setConversationDetection(true),
            repo.setTouchControls(false), repo.setPressAndHold(Bud.LEFT, HoldAction.ASSISTANT),
        )) assertEquals(BudsError.UnsupportedFirmware, (r as BudsResult.Failure).error)
        assertEquals(emptyList<Pair<Int, ByteArray>>(), transport.sent)
        assertEquals(BudsSettings(), repo.settings.value)
    }

    @Test
    fun `no settings write while the session is not Ready`() = runTest {
        val (repo, transport) = buildRepository(connectionStateMachine = buildConnectionStateMachine(driveToReady = false))
        assertEquals(BudsError.ConnectionLost, (repo.setMonoAudio(true) as BudsResult.Failure).error)
        assertEquals(0, transport.sent.size)
    }

    // ---- the press-and-hold ANC-mode list (field 12, ADR-046) and the in-ear detection switch (field 2, ADR-047) — ai-sessions/0056 -------------

    /** The Buds ACK every WriteSetting with the real empty RESPONSE of `CAP-056` (1697 on channel 19, 2855 on channel 21). */
    private fun ackWrites056(transport: FakeBudsTransport, channel: Int) {
        val ack = if (channel == 19) Settings056.ACK_CH19_1697 else Settings056.ACK_CH21_2855
        transport.onSent = { ch, frame ->
            val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
            if (ch == Dlci.MAESTRO && rpc.methodId == Maestro.METHOD_WRITE_SETTING) transport.emit(Dlci.MAESTRO, hex(ack))
        }
    }

    private val allModes = AncModeList(noiseCancellation = true, off = true, transparency = true, adaptive = true)

    @Test
    @DisplayName("F12 (a)/(b)/(e), channel 19: read 1531 (all four), then the CAP-056 tap sequence 1689, 1725, 1786, 1725 (=1802), 1815, 1725 (=1830), 1843 byte for byte, each applied on ACK 1697")
    fun `the ANC-mode list taps of CAP-056 are byte-identical and applied only on the OK`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle()
        transport.emit(Dlci.MAESTRO, hex(Settings056.READ_12_RESP_ALL_1531)); settle()
        assertEquals(SettingReading(allModes, 0), repo.settings.value.ancModeList)
        ackWrites056(transport, 19)
        advanceTimeBy(5_000)

        for ((mode, selected) in listOf(
            AncMode.ACTIVE to false, AncMode.ACTIVE to true, AncMode.OFF to false, AncMode.OFF to true,
            AncMode.ADAPTIVE to false, AncMode.ADAPTIVE to true, AncMode.TRANSPARENT to false,
        )) assertEquals(BudsResult.Success(Unit), repo.setAncModeSelected(mode, selected), "$mode $selected")

        assertEquals(
            listOf(
                Settings056.WRITE_NC_OFF_1689, Settings056.WRITE_ALL_1725, Settings056.WRITE_OFF_OFF_1786, Settings056.WRITE_ALL_1725,
                Settings056.WRITE_ADAPTIVE_OFF_1815, Settings056.WRITE_ALL_1725, Settings056.WRITE_TRANSPARENCY_OFF_1843,
            ),
            transport.sent.map { it.second.toHex() },
            "one write per tap, all four booleans every time",
        )
        assertEquals(SettingReading(allModes.with(AncMode.TRANSPARENT, false), 5_000, changedByApp = true), repo.settings.value.ancModeList)
        assertNull(repo.settingsError.first()?.error)
    }

    @Test
    @DisplayName("F12 (d), channel 21: read CAP-036 1516 {1 0 1 1}; unticking Adaptive = CAP-041 2198 {1 0 1 0}, OK 2855; unticking one more is refused, nothing sent")
    fun `the list never goes below two modes`() = runTest {
        val (repo, transport) = buildRepository() // the CAP-061 announcement: channel 21
        transport.emit(Dlci.MAESTRO, hex(Settings056.READ_12_RESP_OFF_UNTICKED_1516)); settle()
        ackWrites056(transport, 21)

        assertEquals(BudsResult.Success(Unit), repo.setAncModeSelected(AncMode.ADAPTIVE, false))
        assertEquals(listOf(Settings056.WRITE_TWO_LEFT_CH21_2198), transport.sent.map { it.second.toHex() })
        val two = repo.settings.value.ancModeList

        for (mode in listOf(AncMode.ACTIVE, AncMode.TRANSPARENT)) {
            assertEquals(BudsError.AncModeListTooShort, (repo.setAncModeSelected(mode, false) as BudsResult.Failure).error)
        }
        assertEquals(1, transport.sent.size, "nothing sent for a refused untick")
        assertEquals(two, repo.settings.value.ancModeList)
        assertEquals(BudsError.AncModeListTooShort, repo.settingsError.first()?.error)
        // Ticking a mode back is allowed (two or more stay ticked): one more write.
        assertEquals(BudsResult.Success(Unit), repo.setAncModeSelected(AncMode.ADAPTIVE, true))
        assertEquals(2, transport.sent.size)
    }

    @Test
    @DisplayName("F12 on channel 21 with real CAP-041 bytes: Adaptive off = 2192, Transparency off (from all) = 2176")
    fun `ANC-mode list writes on channel 21`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings056.READ_12_RESP_OFF_UNTICKED_1516)); settle()
        ackWrites056(transport, 21)
        assertEquals(BudsResult.Success(Unit), repo.setAncModeSelected(AncMode.OFF, true)) // {1 1 1 1}
        assertEquals(BudsResult.Success(Unit), repo.setAncModeSelected(AncMode.ADAPTIVE, false)) // {1 1 1 0}
        assertEquals(BudsResult.Success(Unit), repo.setAncModeSelected(AncMode.ADAPTIVE, true)) // {1 1 1 1}
        assertEquals(BudsResult.Success(Unit), repo.setAncModeSelected(AncMode.TRANSPARENT, false)) // {1 1 0 1}
        val sent = transport.sent.map { it.second.toHex() }
        assertEquals(Settings056.WRITE_ADAPTIVE_OFF_CH21_2192, sent[1])
        assertEquals(Settings056.WRITE_TRANSPARENCY_OFF_CH21_2176, sent[3])
        assertEquals(sent[0], sent[2], "the same list, the same bytes")
    }

    @Test
    fun `an ANC-mode list not read on this connection is never written (the other three would be guessed)`() = runTest {
        val (repo, transport) = buildRepository()
        ackWrites056(transport, 21)
        assertEquals(BudsError.AncModeListNotRead, (repo.setAncModeSelected(AncMode.ADAPTIVE, false) as BudsResult.Failure).error)
        assertEquals(0, transport.sent.size)
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure(BudsError.AncModeListNotRead, write = true), repo.settingsError.first())
    }

    @Test
    @DisplayName("F12 (e): no RESPONSE -> Timeout, the read list stays; an error status (supplementary, hand-built status 5) -> the reason, list unchanged")
    fun `a failed ANC-mode list write keeps the previous list`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle()
        transport.emit(Dlci.MAESTRO, hex(Settings056.READ_12_RESP_ALL_1531)); settle()
        val read = repo.settings.value.ancModeList
        transport.onSent = null

        assertEquals(BudsError.Timeout, (repo.setAncModeSelected(AncMode.ADAPTIVE, false) as BudsResult.Failure).error)
        assertEquals(listOf(Settings056.WRITE_ADAPTIVE_OFF_1815), transport.sent.map { it.second.toHex() }, "sent once, never retried")
        assertEquals(read, repo.settings.value.ancModeList, "never optimistic")

        transport.onSent = { _, _ ->
            transport.emit(Dlci.MAESTRO, rpcFrame(0x28c0, RpcPacket(PwRpc.TYPE_RESPONSE, 19, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, status = 5)))
        }
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), (repo.setAncModeSelected(AncMode.ADAPTIVE, false) as BudsResult.Failure).error)
        assertEquals(read, repo.settings.value.ancModeList)
    }

    @Test
    fun `the Buds' own settings-change push (CAP-056 1696) is their report of the list`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings056.PUSH_NC_OFF_1696)); settle()
        assertEquals(allModes.with(AncMode.ACTIVE, false), repo.settings.value.ancModeList?.value)
        assertEquals(false, repo.settings.value.ancModeList?.changedByApp)
    }

    @Test
    @DisplayName("F2 (ADR-047): read 1502 = off; channel 19 on/off = CAP-056 2173 / 4048 with ACK 1697 (2179/4056), applied with the time")
    fun `the in-ear detection switch on channel 19`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle()
        transport.emit(Dlci.MAESTRO, hex(Settings056.READ_2_RESP_OFF_1502)); settle()
        assertEquals(SettingReading(false, 0), repo.settings.value.inEarDetection)
        ackWrites056(transport, 19)
        advanceTimeBy(7_000)

        assertEquals(BudsResult.Success(Unit), repo.setInEarDetection(true))
        assertEquals(SettingReading(true, 7_000, changedByApp = true), repo.settings.value.inEarDetection)
        assertEquals(BudsResult.Success(Unit), repo.setInEarDetection(false))
        assertEquals(SettingReading(false, 7_000, changedByApp = true), repo.settings.value.inEarDetection)
        assertEquals(listOf(Settings056.WRITE_INEAR_ON_2173, Settings056.WRITE_INEAR_OFF_4048), transport.sent.map { it.second.toHex() })
    }

    @Test
    @DisplayName("F2 on channel 21: CAP-056 3627 / 2849 with ACK 2855; no answer -> Timeout and the value stays")
    fun `the in-ear detection switch on channel 21`() = runTest {
        val (repo, transport) = buildRepository()
        ackWrites056(transport, 21)
        assertEquals(BudsResult.Success(Unit), repo.setInEarDetection(true))
        assertEquals(BudsResult.Success(Unit), repo.setInEarDetection(false))
        assertEquals(listOf(Settings056.WRITE_INEAR_ON_CH21_3627, Settings056.WRITE_INEAR_OFF_CH21_2849), transport.sent.map { it.second.toHex() })
        val before = repo.settings.value.inEarDetection
        transport.onSent = null
        assertEquals(BudsError.Timeout, (repo.setInEarDetection(true) as BudsResult.Failure).error)
        assertEquals(before, repo.settings.value.inEarDetection)
        assertEquals(false, before?.value)
    }

    // ---- the five switches of ai-sessions/0074 (ADR-052 … ADR-055) ----------------------------------------------------------------------------------

    /** The Buds answer every WriteSetting with [ack] (a real empty `RESPONSE` of that channel) — and nothing else. */
    private fun answerWritesWith(transport: FakeBudsTransport, ack: String) {
        transport.onSent = { ch, frame ->
            val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
            if (ch == Dlci.MAESTRO && rpc.methodId == Maestro.METHOD_WRITE_SETTING) transport.emit(Dlci.MAESTRO, hex(ack))
        }
    }

    /** A hand-built `RESPONSE` with status 5 (`NOT_FOUND`) for a write — supplementary: no capture has a rejected write. */
    private fun rejectWrites(transport: FakeBudsTransport, channel: Int, responseAddress: Int) {
        transport.onSent = { _, _ ->
            transport.emit(Dlci.MAESTRO, rpcFrame(responseAddress, RpcPacket(PwRpc.TYPE_RESPONSE, channel, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, status = 5)))
        }
    }

    @Test
    @DisplayName("MP (ADR-053), channel 21: read CAP-069 1267 = on; OFF = CAP-069 3161, ON = 3212 byte for byte, each applied on the empty RESPONSE 3170 with its time")
    fun `the Multipoint switch on channel 21`() = runTest {
        val (repo, transport) = buildRepository() // the CAP-061 announcement: channel 21
        advanceTimeBy(1_000)
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_11_RESP_CH21_1267)); settle()
        assertEquals(SettingReading(true, 1_000), repo.settings.value.multipoint)
        answerWritesWith(transport, Settings074.ACK_CH21_3170)
        advanceTimeBy(5_000)

        assertEquals(BudsResult.Success(Unit), repo.setMultipoint(false))
        assertEquals(SettingReading(false, 6_000, changedByApp = true), repo.settings.value.multipoint)
        assertEquals(BudsResult.Success(Unit), repo.setMultipoint(true))
        assertEquals(SettingReading(true, 6_000, changedByApp = true), repo.settings.value.multipoint)
        assertEquals(listOf(Settings074.MP_OFF_CH21_3161, Settings074.MP_ON_CH21_3212), transport.sent.map { it.second.toHex() }, "one write per tap")
        assertEquals(setOf(Dlci.MAESTRO), transport.sent.map { it.first }.toSet(), "nothing on DLCI 0x04: the SASS answer is not waited for (ADR-053)")
        assertNull(repo.settingsError.first())
    }

    @Test
    @DisplayName("MP, channel 19: read CAP-069 7272 = on; OFF = CAP-070 A3668, ON = A3679 (OpenControl 1.1.0) byte for byte, each applied on the RESPONSE A3671")
    fun `the Multipoint switch on channel 19`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle()
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_11_RESP_CH19_7272)); settle()
        assertEquals(true, repo.settings.value.multipoint?.value)
        answerWritesWith(transport, Settings070.ACK_CH19_A3671)

        assertEquals(BudsResult.Success(Unit), repo.setMultipoint(false))
        assertEquals(false, repo.settings.value.multipoint?.value)
        assertEquals(BudsResult.Success(Unit), repo.setMultipoint(true))
        assertEquals(true, repo.settings.value.multipoint?.value)
        assertEquals(listOf(Settings070.MP_OFF_CH19_A3668, Settings070.MP_ON_CH19_A3679), transport.sent.map { it.second.toHex() }, "one write per tap")
        assertEquals(setOf(Dlci.MAESTRO), transport.sent.map { it.first }.toSet())
    }

    @Test
    fun `a Multipoint write without an answer or with an error status keeps the value read at Connect and says why`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_11_RESP)); settle()
        val read = repo.settings.value.multipoint
        transport.onSent = null

        assertEquals(BudsError.Timeout, (repo.setMultipoint(false) as BudsResult.Failure).error)
        assertEquals(1, transport.sent.size, "sent once, never retried")
        assertEquals(read, repo.settings.value.multipoint, "never applied before the Buds' OK")
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure(BudsError.Timeout, write = true), repo.settingsError.first())

        advanceTimeBy(2_000) // past the write quarantine (ADR-045 Update)
        rejectWrites(transport, 21, 10496)
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), (repo.setMultipoint(false) as BudsResult.Failure).error)
        assertEquals(read, repo.settings.value.multipoint)
    }

    @Test
    @DisplayName("HG (ADR-052), channel 21: read CAP-069 1226 (29:2) = on; OFF = CAP-069 2492 (29:1), ON = 2564 (29:2) byte for byte, applied on the RESPONSE 2506")
    fun `the head gestures switch on channel 21`() = runTest {
        val (repo, transport) = buildRepository()
        advanceTimeBy(2_000)
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_29_ON_CH21_1226)); settle()
        assertEquals(SettingReading(true, 2_000), repo.settings.value.headGestures)
        answerWritesWith(transport, Settings074.ACK_CH21_2506)
        advanceTimeBy(1_000)

        assertEquals(BudsResult.Success(Unit), repo.setHeadGestures(false))
        assertEquals(SettingReading(false, 3_000, changedByApp = true), repo.settings.value.headGestures)
        assertEquals(BudsResult.Success(Unit), repo.setHeadGestures(true))
        assertEquals(SettingReading(true, 3_000, changedByApp = true), repo.settings.value.headGestures)
        assertEquals(listOf(Settings074.HG_OFF_CH21_2492, Settings074.HG_ON_CH21_2564), transport.sent.map { it.second.toHex() })
        assertEquals(setOf(Dlci.MAESTRO), transport.sent.map { it.first }.toSet(), "nothing on GSND CONTROL or any other DLCI (ADR-052)")
    }

    @Test
    @DisplayName("HG, channel 19: read CAP-024 1100 (29:1) = off; ON = CAP-070 A3685 (29:2), OFF = A3614 (29:1) (OpenControl 1.1.0) byte for byte, applied on A3671")
    fun `the head gestures switch on channel 19`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle()
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_29_OFF_CH19_CAP024_1100)); settle()
        assertEquals(false, repo.settings.value.headGestures?.value)
        answerWritesWith(transport, Settings070.ACK_CH19_A3671)

        assertEquals(BudsResult.Success(Unit), repo.setHeadGestures(true))
        assertEquals(true, repo.settings.value.headGestures?.value)
        assertEquals(BudsResult.Success(Unit), repo.setHeadGestures(false))
        assertEquals(false, repo.settings.value.headGestures?.value)
        assertEquals(listOf(Settings070.HG_ON_CH19_A3685, Settings070.HG_OFF_CH19_A3614), transport.sent.map { it.second.toHex() })
        assertEquals(setOf(Dlci.MAESTRO), transport.sent.map { it.first }.toSet(), "nothing on GSND CONTROL or any other DLCI (ADR-052)")
    }

    /**
     * ADR-052: a read value other than 1 or 2 is not interpreted. **Supplementary, hand-built:** the real `CAP-036` frame 1561 (`4:{29:2}`) with the value
     * changed to 3 and the frame re-sealed (new CRC) — no capture holds such a value.
     */
    @Test
    fun `a head-gesture read of 3 stays not read (UnreadableAnswer), never on or off`() = runTest {
        val real = (Hdlc.decode(hex(Settings036.READ_29_RESP)) as BudsResult.Success).value
        val three = Hdlc.encode(real.address, real.control, hex(real.payload.toHex().replace("2203e80102", "2203e80103")))
        val (repo, transport) = buildRepository(verified = false)
        transport.onSent = { ch, frame ->
            val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
            if (ch == Dlci.MAESTRO && rpc.methodId == Maestro.METHOD_READ_SETTING) {
                when (val field = rpc.payload[1].toInt()) {
                    29 -> transport.emit(Dlci.MAESTRO, three)
                    else -> Settings036.ANSWERS[field]?.let { transport.emit(Dlci.MAESTRO, hex(it)) }
                }
            }
        }
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496)); settle()
        repo.launchInitialEqRead(); settle()

        assertNull(repo.settings.value.headGestures)
        assertEquals(true, repo.settings.value.multipoint?.value, "the other fields were read")
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.SettingsFailure(BudsError.UnreadableAnswer, write = false), repo.settingsError.first())
    }

    @Test
    fun `a head-gesture write without an answer or with an error status keeps the value read at Connect`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_29_RESP)); settle()
        val read = repo.settings.value.headGestures
        transport.onSent = null
        assertEquals(BudsError.Timeout, (repo.setHeadGestures(false) as BudsResult.Failure).error)
        assertEquals(listOf(Settings074.HG_OFF_CH21_2492), transport.sent.map { it.second.toHex() }, "sent once, never retried")
        assertEquals(read, repo.settings.value.headGestures)
        advanceTimeBy(2_000)
        rejectWrites(transport, 21, 10496)
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), (repo.setHeadGestures(false) as BudsResult.Failure).error)
        assertEquals(read, repo.settings.value.headGestures)
    }

    @Test
    @DisplayName("CS (ADR-054), channel 21: reads CAP-058 4514/4517 = on; the push 5627 (28:0) turns only Earbuds replaced off; 5680 / 5643 byte for byte, ACK 5683")
    fun `the case sounds switches on channel 21`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_27_RESP_CH21_4514))
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_28_RESP_CH21_4517)); settle()
        transport.emit(Dlci.MAESTRO, hex(Settings074.STREAM_28_OFF_CH21_5627)); settle()
        assertEquals(true, repo.settings.value.caseSoundOtherAlerts?.value, "27 untouched by a push of 28")
        assertEquals(false, repo.settings.value.caseSoundEarbudsReplaced?.value)
        answerWritesWith(transport, Settings074.ACK_CH21_5683)
        advanceTimeBy(4_000)

        assertEquals(BudsResult.Success(Unit), repo.setCaseSoundOtherAlerts(false))
        assertEquals(SettingReading(false, 4_000, changedByApp = true), repo.settings.value.caseSoundOtherAlerts)
        assertEquals(false, repo.settings.value.caseSoundEarbudsReplaced?.value, "28 untouched by a write of 27")
        assertEquals(BudsResult.Success(Unit), repo.setCaseSoundEarbudsReplaced(true))
        assertEquals(SettingReading(true, 4_000, changedByApp = true), repo.settings.value.caseSoundEarbudsReplaced)
        assertEquals(false, repo.settings.value.caseSoundOtherAlerts?.value, "27 untouched by a write of 28")
        assertEquals(listOf(Settings074.CS27_OFF_CH21_5680, Settings074.CS28_ON_CH21_5643), transport.sent.map { it.second.toHex() })
    }

    @Test
    @DisplayName("CS on channel 19: reads CAP-024 1092/1096; 1988 (28:0), 2023 (28:1), 2053 (27:0), 2084 (27:1) byte for byte, each applied on the ACK 2061")
    fun `the case sounds switches on channel 19`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle()
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_27_RESP_CH19_1092))
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_28_RESP_CH19_1096)); settle()
        answerWritesWith(transport, Settings074.ACK_CH19_2061)

        assertEquals(BudsResult.Success(Unit), repo.setCaseSoundEarbudsReplaced(false))
        assertEquals(BudsResult.Success(Unit), repo.setCaseSoundEarbudsReplaced(true))
        assertEquals(BudsResult.Success(Unit), repo.setCaseSoundOtherAlerts(false))
        assertEquals(false, repo.settings.value.caseSoundOtherAlerts?.value)
        assertEquals(true, repo.settings.value.caseSoundEarbudsReplaced?.value)
        assertEquals(BudsResult.Success(Unit), repo.setCaseSoundOtherAlerts(true))

        assertEquals(
            listOf(Settings074.CS28_OFF_CH19_1988, Settings074.CS28_ON_CH19_2023, Settings074.CS27_OFF_CH19_2053, Settings074.CS27_ON_CH19_2084),
            transport.sent.map { it.second.toHex() },
        )
    }

    @Test
    fun `a case-sound write without an answer or with an error status keeps the value read at Connect`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_27_RESP))
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_28_RESP)); settle()
        val read27 = repo.settings.value.caseSoundOtherAlerts
        val read28 = repo.settings.value.caseSoundEarbudsReplaced
        transport.onSent = null
        assertEquals(BudsError.Timeout, (repo.setCaseSoundOtherAlerts(false) as BudsResult.Failure).error)
        assertEquals(read27, repo.settings.value.caseSoundOtherAlerts)
        advanceTimeBy(2_000)
        rejectWrites(transport, 21, 10496)
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), (repo.setCaseSoundEarbudsReplaced(false) as BudsResult.Failure).error)
        assertEquals(read28, repo.settings.value.caseSoundEarbudsReplaced)
        assertEquals(listOf(Settings074.CS27_OFF_CH21_5680, Settings074.CS28_OFF_CH21_5623), transport.sent.map { it.second.toHex() }, "one each, never retried")
    }

    @Test
    @DisplayName("VEQ (ADR-055), channel 19: read CAP-024 1038 = on; OFF = CAP-022 1871, ON = 1895 byte for byte, applied on the RESPONSE 1877")
    fun `the Volume EQ switch on channel 19`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)); settle()
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_15_ON_CH19_1038)); settle()
        assertEquals(true, repo.settings.value.volumeEq?.value)
        answerWritesWith(transport, Settings074.ACK_CH19_1877)
        advanceTimeBy(8_000)

        assertEquals(BudsResult.Success(Unit), repo.setVolumeEq(false))
        assertEquals(SettingReading(false, 8_000, changedByApp = true), repo.settings.value.volumeEq)
        assertEquals(BudsResult.Success(Unit), repo.setVolumeEq(true))
        assertEquals(listOf(Settings074.VEQ_OFF_CH19_1871, Settings074.VEQ_ON_CH19_1895), transport.sent.map { it.second.toHex() })
    }

    @Test
    @DisplayName("VEQ, channel 21: a read of CAP-041 3239 = off; ON = CAP-070 A4800 (OpenControl 1.1.0), applied on the RESPONSE A4802; OFF = CAP-041 2461")
    fun `the Volume EQ switch on channel 21`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings074.READ_15_OFF_CH21_CAP041_3239)); settle()
        assertEquals(false, repo.settings.value.volumeEq?.value)
        answerWritesWith(transport, Settings070.ACK_CH21_A4802)

        assertEquals(BudsResult.Success(Unit), repo.setVolumeEq(true))
        assertEquals(true, repo.settings.value.volumeEq?.value)
        assertEquals(BudsResult.Success(Unit), repo.setVolumeEq(false))
        assertEquals(false, repo.settings.value.volumeEq?.value)
        assertEquals(listOf(Settings070.VEQ_ON_CH21_A4800, Settings074.VEQ_OFF_CH21_CAP041_2461), transport.sent.map { it.second.toHex() })
    }

    @Test
    fun `a Volume EQ write without an answer or with an error status keeps the value read at Connect`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_15_RESP)); settle()
        val read = repo.settings.value.volumeEq
        transport.onSent = null
        assertEquals(BudsError.Timeout, (repo.setVolumeEq(false) as BudsResult.Failure).error)
        assertEquals(read, repo.settings.value.volumeEq)
        advanceTimeBy(2_000)
        rejectWrites(transport, 21, 10496)
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), (repo.setVolumeEq(false) as BudsResult.Failure).error)
        assertEquals(read, repo.settings.value.volumeEq)
        assertEquals(listOf(Settings074.VEQ_OFF_CH21_CAP041_2461, Settings074.VEQ_OFF_CH21_CAP041_2461), transport.sent.map { it.second.toHex() })
    }

    @Test
    fun `Safe Mode refuses the ANC-mode list and the in-ear detection writes and sends nothing (ADR-042)`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        transport.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_6.000"))
        transport.emit(Dlci.MAESTRO, hex(Settings056.READ_12_RESP_OFF_UNTICKED_1516))
        settle()
        assertEquals(BudsError.UnsupportedFirmware, (repo.setAncModeSelected(AncMode.ADAPTIVE, false) as BudsResult.Failure).error)
        assertEquals(BudsError.UnsupportedFirmware, (repo.setInEarDetection(true) as BudsResult.Failure).error)
        assertEquals(BudsError.UnsupportedFirmware, (repo.setMultipoint(true) as BudsResult.Failure).error) // ADR-053 through the same gate
        assertEquals(BudsError.UnsupportedFirmware, (repo.setHeadGestures(false) as BudsResult.Failure).error) // ADR-052 too
        assertEquals(BudsError.UnsupportedFirmware, (repo.setCaseSoundOtherAlerts(false) as BudsResult.Failure).error) // ADR-054
        assertEquals(BudsError.UnsupportedFirmware, (repo.setCaseSoundEarbudsReplaced(false) as BudsResult.Failure).error)
        assertEquals(BudsError.UnsupportedFirmware, (repo.setVolumeEq(true) as BudsResult.Failure).error) // ADR-055
        assertEquals(emptyList<Pair<Int, ByteArray>>(), transport.sent)
    }

    @Test
    @DisplayName("U-2: a settings tap while the session is being (re)opened -> SessionOpening, nothing sent, nothing queued")
    fun `a settings write during a re-open says so and sends nothing`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false).also { it.onConnectRequested() } // Connecting
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        assertEquals(BudsError.SessionOpening, (repo.setInEarDetection(true) as BudsResult.Failure).error)
        assertEquals(BudsError.SessionOpening, (repo.setMonoAudio(true) as BudsResult.Failure).error)
        machine.onLinkEstablished(); machine.onReady(); settle()
        advanceTimeBy(5_000); settle()
        assertEquals(0, transport.sent.count { it.first == Dlci.MAESTRO && it.second.toHex().contains("1d9a8c9e2a") }, "no write went out later (no queue)")
    }

    @Test
    @DisplayName("0058 A58-APP-08: an EQ change while the session is being (re)opened -> SessionOpening on the EQ tab, nothing sent, nothing queued")
    fun `an EQ write during a re-open says so and sends nothing`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false).also { it.onConnectRequested() } // Connecting
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        assertEquals(BudsError.SessionOpening, (repo.setEqGains(heavyBass) as BudsResult.Failure).error)
        assertEquals(BudsError.SessionOpening, repo.eqError.first())
        machine.onLinkEstablished(); machine.onReady(); settle()
        advanceTimeBy(5_000); settle()
        assertEquals(0, transport.sent.count { it.first == Dlci.MAESTRO && it.second.toHex().contains("1d9a8c9e2a") }, "no write went out later (no queue)")
    }

    // ---- Refresh battery (ai-sessions/0052): always a fresh claim; no burst is said, never suggested ------------------------------------

    /** The Buds of the CAP-062 06:46:29 Refresh: on every open the Model ID and the battery burst (frame 7106), a `Get` answered by 7110. */
    private fun scriptRefreshBuds(transport: FakeBudsTransport, burst: Boolean = true) {
        transport.onOpenChannel = { ch ->
            if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM) {
                transport.emit(ch, MODEL_ID_PRO_2)
                if (burst) transport.emit(ch, hex(Cap062.BATTERY_BURST_7106))
            }
        }
        transport.onSent = { ch, frame -> if (frame.toHex() == Cap062.GET_ANC_7098) transport.emit(ch, hex(Cap062.NOTIFY_7110)) }
    }

    @Test
    @DisplayName("Refresh inside the linger: the lingering claim is released and a new one opened; CAP-062 burst 7106 stamps the new time")
    fun `a Refresh with the channel still open releases it and claims afresh`() = runTest {
        val (repo, transport) = buildRepository()
        scriptRefreshBuds(transport)
        advanceTimeBy(1_000)
        assertEquals(BudsResult.Success(Unit), repo.refreshBattery())
        assertEquals(BatteryLevel.Known(100, true, receivedAtMillis = 1_000), repo.batteryStatus.value.left)
        assertEquals(true, transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM), "lingering")

        advanceTimeBy(600) // inside the 1.5 s linger
        assertEquals(BudsResult.Success(Unit), repo.refreshBattery())

        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM, Dlci.FAST_PAIR_MESSAGE_STREAM), transport.openChannelCalls, "a second SABM, not a reused claim")
        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.closeChannelCalls, "the lingering claim was released first")
        assertEquals(BatteryLevel.Known(100, true, receivedAtMillis = 1_600), repo.batteryStatus.value.left)
        assertEquals(BatteryLevel.Known(100, false, receivedAtMillis = 1_600), repo.batteryStatus.value.right)
        assertNull(repo.batteryRefreshError.first())
    }

    @Test
    @DisplayName("ADR-043 Update: each Refresh sends exactly one SubscribeRuntimeInfo, byte-identical to CAP-062 2777; no answer changes nothing")
    fun `a Refresh re-subscribes to runtime info once`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.MAESTRO, helloFrame(19, 0x28c0)) // channel 19, as in CAP-062
        advanceTimeBy(1_000); transport.emit(Dlci.MAESTRO, hex(Cap062.STREAM_BOTH_4845)); settle() // a Case value with its time
        scriptRefreshBuds(transport)
        val case = repo.batteryStatus.value.case

        repo.refreshBattery(); settle()
        advanceTimeBy(5_000)
        repo.refreshBattery(); settle()
        advanceTimeBy(5_000); settle()

        val maestro = transport.sent.filter { it.first == Dlci.MAESTRO }.map { it.second.toHex() }
        assertEquals(listOf(Cap062.SUBSCRIBE_RUNTIME_INFO_2777, Cap062.SUBSCRIBE_RUNTIME_INFO_2777), maestro, "one per Refresh, nothing else, no retry")
        assertEquals(case, repo.batteryStatus.value.case, "no answer: the Case keeps its value and its time")
        assertNull(repo.caseBatteryError.first())
    }

    @Test
    fun `a Refresh whose claim brings no battery frame says so and keeps the old values and times`() = runTest {
        val (repo, transport) = buildRepository()
        scriptRefreshBuds(transport)
        advanceTimeBy(1_000)
        repo.refreshBattery()
        scriptRefreshBuds(transport, burst = false)
        advanceTimeBy(5_000) // the first claim is released by now

        val result = repo.refreshBattery()

        assertEquals(BudsError.NoNewBatteryReading, (result as BudsResult.Failure).error)
        assertEquals(BudsError.NoNewBatteryReading, repo.batteryRefreshError.first())
        assertEquals(BatteryLevel.Known(100, true, receivedAtMillis = 1_000), repo.batteryStatus.value.left, "no new time is suggested")
    }

    @Test
    fun `a Refresh whose claim fails reports the claim's error and changes no value`() = runTest {
        val (repo, transport) = buildRepository()
        scriptRefreshBuds(transport)
        advanceTimeBy(1_000)
        repo.refreshBattery()
        advanceTimeBy(5_000)
        val busy = BudsError.ChannelUnavailable(Dlci.FAST_PAIR_MESSAGE_STREAM, "busy")
        transport.openChannelShouldFail = busy

        val result = repo.refreshBattery()

        assertEquals(busy, (result as BudsResult.Failure).error)
        assertEquals(busy, repo.batteryRefreshError.first())
        assertEquals(BatteryLevel.Known(100, true, receivedAtMillis = 1_000), repo.batteryStatus.value.left)
    }

    // ---- I-7 (ai-sessions/0048): the loss cause follows Android's link around the loss ------------------------------------------

    private val socketEof = "IOException: bt socket closed, read return: -1" // CAP-062: identical for every loss (11/11)

    @Test
    @DisplayName("T-1 (ai-sessions/0062), CAP-064 §6 #1: CONNECTED 126 ms after the loss, NOT_CONNECTED 165 ms after — the first verdicts are logged as provisional, the last as final")
    fun `the loss cause lines inside the window are marked provisional`() = runTest {
        val (repo, transport) = buildRepository()
        BleLogger.clear()
        advanceTimeBy(10_000)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        advanceTimeBy(126); repo.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(39); repo.onAndroidLink(AndroidLink.NOT_CONNECTED)

        val lines = BleLogger.exportLog().lines().filter { "Session loss cause" in it }
        assertEquals(3, lines.size, lines.joinToString("\n"))
        assert("undetermined" in lines[0] && "(provisional" in lines[0]) { lines[0] }
        assert("the Buds closed the channel" in lines[1] && "(provisional" in lines[1]) { lines[1] }
        assert("went down" in lines[2] && "provisional" !in lines[2]) { lines[2] }
    }

    // ---- F-3 (ai-sessions/0064): a Bluetooth-off loss is named, and final at once ---------------------------------------------------------------
    // CAP-066 export E1 (CAP-066-opencontrol-debug-20261001-162556.txt) lines 279-283 / E2 (…-163412.txt) lines 164-168 (local time) and the adapter states of the
    // system logs (UTC = local − 2 h): System-log-d55db7f4e1c8 38635 14:13:00.837 (12 → 13 TURNING_OFF), 38754 14:13:01.361 (13 → 10 OFF); System-log-7f4bb1d5ea2c
    // 42742 14:32:16.820 (12 → 13), 42895 14:32:17.651 (13 → 10).

    @Test
    @DisplayName("F-3, CAP-066 E1: TURNING_OFF 16:13:00.837, link UNKNOWN .971, OFF 01.361, loss 01.386 -> one final line 'Bluetooth was switched off on this phone'")
    fun `the CAP-066 E1 Bluetooth-off loss is named and final`() = runTest {
        val (repo, transport) = buildRepository()
        repo.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(10_000)
        BleLogger.clear()
        repo.onBluetoothAdapter(on = false) // 16:13:00.837 TURNING_OFF
        advanceTimeBy(134); repo.onAndroidLink(AndroidLink.UNKNOWN) // 16:13:00.971, E1 279
        advanceTimeBy(390); repo.onBluetoothAdapter(on = false) // 16:13:01.361 OFF
        advanceTimeBy(25); deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof)) // 16:13:01.386, E1 281

        assertEquals(SessionLossCause.BLUETOOTH_OFF, repo.lastLossCause.first())
        val lines = BleLogger.exportLog().lines().filter { "Session loss cause" in it }
        assertEquals(1, lines.size, lines.joinToString("\n"))
        assert(lines[0].endsWith("Session loss cause: Bluetooth was switched off on this phone")) { "final at once, no (provisional): ${lines[0]}" }
    }

    @Test
    @DisplayName("F-3, CAP-066 E2: TURNING_OFF 16:32:16.820, link UNKNOWN 17.010, loss 17.632, OFF 17.651 -> Bluetooth was switched off; without the adapter: undetermined")
    fun `the CAP-066 E2 Bluetooth-off loss is named, the old verdict without the adapter reading stays undetermined`() = runTest {
        val (repo, transport) = buildRepository()
        advanceTimeBy(10_000)
        repo.onBluetoothAdapter(on = false) // 16:32:16.820
        advanceTimeBy(190); repo.onAndroidLink(AndroidLink.UNKNOWN) // 16:32:17.010, E2 164
        advanceTimeBy(622); deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof)) // 16:32:17.632, E2 166
        assertEquals(SessionLossCause.BLUETOOTH_OFF, repo.lastLossCause.first())
        advanceTimeBy(19); repo.onBluetoothAdapter(on = false) // 16:32:17.651
        assertEquals(SessionLossCause.BLUETOOTH_OFF, repo.lastLossCause.first())

        val (repo2, transport2) = buildRepository() // the CAP-066 build: no adapter reading reached the repository
        advanceTimeBy(10_000)
        repo2.onAndroidLink(AndroidLink.UNKNOWN)
        advanceTimeBy(622); deliverLoss(transport2, ConnectionLoss(Dlci.MAESTRO, socketEof))
        assertEquals(SessionLossCause.UNDETERMINED, repo2.lastLossCause.first(), "E2 167")
    }

    @Test
    fun `an adapter turning on, or an off reading long before the loss, names nothing`() = runTest {
        val (repo, transport) = buildRepository()
        repo.onBluetoothAdapter(on = false)
        advanceTimeBy(60_000) // Bluetooth was off a minute ago and came back
        repo.onBluetoothAdapter(on = true)
        repo.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(5_000)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        advanceTimeBy(15); repo.onAndroidLink(AndroidLink.CONNECTED)
        assertEquals(SessionLossCause.BUDS_CLOSED_CHANNEL, repo.lastLossCause.first())
    }

    @Test
    @DisplayName("I-7, CAP-062 06:42:35 ordering: the link was CONNECTED before the loss, NOT_CONNECTED 109 ms after it -> Android link lost")
    fun `the 06h42m35 ordering never shows the stale connected reading`() = runTest {
        val (repo, transport) = buildRepository()
        repo.onAndroidLink(AndroidLink.CONNECTED) // an old reading, before the loss
        advanceTimeBy(28_000)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        assertEquals(SessionLossCause.UNDETERMINED, repo.lastLossCause.first(), "a reading older than the loss is not used")

        advanceTimeBy(109)
        repo.onAndroidLink(AndroidLink.NOT_CONNECTED)
        assertEquals(SessionLossCause.ANDROID_LINK_LOST, repo.lastLossCause.first())
    }

    @Test
    @DisplayName("I-7, a Buds-side DISC with the link up (CAP-062 frame 5313): the refresh right after the loss still reads CONNECTED")
    fun `a peer DISC with the link up is the Buds closing the channel`() = runTest {
        val (repo, transport) = buildRepository()
        repo.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(6_000)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        advanceTimeBy(15)
        repo.onAndroidLink(AndroidLink.CONNECTED) // the re-evaluation the session change triggers
        assertEquals(SessionLossCause.BUDS_CLOSED_CHANNEL, repo.lastLossCause.first())

        advanceTimeBy(3_000) // both buds into the case: the ACL drop follows 3.0 s later (CAP-062 06:46:04 -> 06:46:07)
        repo.onAndroidLink(AndroidLink.NOT_CONNECTED)
        assertEquals(SessionLossCause.BUDS_CLOSED_CHANNEL, repo.lastLossCause.first(), "the Buds closed it first")
    }

    @Test
    fun `the user's own Disconnect leaves no loss to explain (I-7 case c)`() = runTest {
        val (repo, transport) = buildRepository()
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        repo.onAndroidLink(AndroidLink.CONNECTED)
        assertEquals(SessionLossCause.BUDS_CLOSED_CHANNEL, repo.lastLossCause.first())
        repo.disconnect()
        assertNull(repo.lastLossCause.first())
        assertNull(repo.lastConnectionError.first())
    }

    // ---- I-2 (ai-sessions/0054): a loss while the app was not on screen is re-classified from the first reading on return ---------------------
    // CAP-063-debug-export.log: 526 16:13:07.841 CONNECTED; 586 16:15:21.408 observer stopped (the app left); 589 16:15:22.008 loss (ACL 0x13, both
    // buds seated); 592 16:15:25.013 observer started (back); 593 16:15:25.062 NOT_CONNECTED. Virtual time: 0 = 16:13:07.841.

    @Test
    @DisplayName("I-2, CAP-063 16:15:22: hidden at the loss, undetermined until the first reading on return (NOT_CONNECTED, 3.05 s later) -> link down on return")
    fun `a background loss is decided by the reading taken on return`() = runTest {
        val (repo, transport) = buildRepository()
        repo.onAppVisible(true)
        repo.onAndroidLink(AndroidLink.CONNECTED) // 16:13:07.841
        advanceTimeBy(133_567); repo.onAppVisible(false) // 16:15:21.408
        advanceTimeBy(600)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof)) // 16:15:22.008
        assertEquals(SessionLossCause.UNDETERMINED, repo.lastLossCause.first(), "no reading can come while the app is away")

        advanceTimeBy(3_005); repo.onAppVisible(true) // ≈ 16:15:25.013
        assertEquals(SessionLossCause.UNDETERMINED, repo.lastLossCause.first(), "the older CONNECTED reading is not used")
        advanceTimeBy(49); repo.onAndroidLink(AndroidLink.NOT_CONNECTED) // 16:15:25.062
        assertEquals(SessionLossCause.ANDROID_LINK_DOWN_ON_RETURN, repo.lastLossCause.first())
    }

    @Test
    @DisplayName("I-2: a loss while visible whose deciding reading never came before the app left is also read on return (CONNECTED -> link up on return)")
    fun `leaving before a deciding reading defers the cause to the return`() = runTest {
        val (repo, transport) = buildRepository()
        repo.onAppVisible(true)
        repo.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(10_000)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        advanceTimeBy(300); repo.onAppVisible(false) // gone before any reading
        advanceTimeBy(60_000); repo.onAppVisible(true)
        repo.onAndroidLink(AndroidLink.CONNECTED)
        assertEquals(SessionLossCause.ANDROID_LINK_UP_ON_RETURN, repo.lastLossCause.first(), "a minute later: said to be read on return")
    }

    @Test
    fun `a loss while visible with no reading at all stays undetermined (the 0048 rule, unchanged)`() = runTest {
        val (repo, transport) = buildRepository()
        repo.onAppVisible(true)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        advanceTimeBy(60_000)
        repo.onAndroidLink(AndroidLink.NOT_CONNECTED)
        assertEquals(SessionLossCause.UNDETERMINED, repo.lastLossCause.first(), "while visible a reading a minute later says nothing about the loss")
    }

    // ---- I-1 (ai-sessions/0048, DECISIONS.md ADR-044): automatic foreground re-open -----------------------------------------------
    // connect() needs a real BluetoothDevice, which a JVM test cannot build: every attempt stops at the bonded-device lookup (NotPaired), which
    // is exactly what these tests count. The success-path rules (the chain guard) are in SessionReopenerTest.

    private suspend fun TestScope.reopenHarness(): Triple<BudsRepositoryImpl, FakeBudsTransport, () -> Int> {
        var attempts = 0
        val (repo, transport) = buildRepository(onConnectAttempt = { attempts++ })
        return Triple(repo, transport, { attempts })
    }

    @Test
    @DisplayName("I-1 (a): a Buds-side DISC with the link up while visible -> exactly one re-open, 1.5 s later")
    fun `a peer DISC while visible re-opens once after the delay`() = runTest {
        val (repo, transport, attempts) = reopenHarness()
        repo.onAppVisible(true)
        repo.onAndroidLink(AndroidLink.CONNECTED)
        assertEquals(0, attempts(), "a session is open: nothing to do")

        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        repo.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(1_400); runCurrent()
        assertEquals(0, attempts(), "not before the delay")
        advanceTimeBy(200); runCurrent()
        assertEquals(1, attempts())
        advanceTimeBy(60_000); runCurrent()
        assertEquals(1, attempts(), "one attempt per event — no loop, no retry")
        assertEquals(BudsError.NotPaired, repo.lastConnectionError.first(), "a failed re-open is reported")
    }

    @Test
    fun `no re-open when Android's link is down after the delay (an ACL drop), then one when it comes back (I-1 b)`() = runTest {
        val (repo, transport, attempts) = reopenHarness()
        repo.onAppVisible(true)
        repo.onAndroidLink(AndroidLink.CONNECTED)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        advanceTimeBy(109); repo.onAndroidLink(AndroidLink.NOT_CONNECTED)
        advanceTimeBy(5_000); runCurrent()
        assertEquals(0, attempts())

        repo.onAndroidLink(AndroidLink.CONNECTED) // Android re-creates the ACL on lid-open (CAP-062 frame 7226)
        runCurrent()
        assertEquals(1, attempts())
        repo.onAndroidLink(AndroidLink.CONNECTED); runCurrent()
        assertEquals(1, attempts(), "a repeated CONNECTED reading is not a new event")
    }

    @Test
    fun `none after the user's Disconnect, until the next Connect tap (I-1 item 2)`() = runTest {
        val (repo, _, attempts) = reopenHarness()
        repo.onAppVisible(true)
        repo.disconnect()
        repo.onAndroidLink(AndroidLink.NOT_CONNECTED); repo.onAndroidLink(AndroidLink.CONNECTED)
        repo.onAppVisible(false); repo.onAppVisible(true); repo.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(10_000); runCurrent()
        assertEquals(0, attempts())

        repo.connect() // the user's tap (counts once itself) re-enables it
        assertEquals(1, attempts())
        repo.onAndroidLink(AndroidLink.NOT_CONNECTED); repo.onAndroidLink(AndroidLink.CONNECTED); runCurrent()
        assertEquals(2, attempts(), "enabled again")
    }

    @Test
    fun `none while backgrounded - a loss, the delay, a link change (I-1 item 3)`() = runTest {
        val (repo, transport, attempts) = reopenHarness()
        repo.onAppVisible(true)
        repo.onAndroidLink(AndroidLink.CONNECTED)
        repo.onAppVisible(false)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        repo.onAndroidLink(AndroidLink.NOT_CONNECTED); repo.onAndroidLink(AndroidLink.CONNECTED)
        advanceTimeBy(10_000); runCurrent()
        assertEquals(0, attempts())
    }

    @Test
    fun `going to the background during the delay cancels the pending re-open`() = runTest {
        val (repo, transport, attempts) = reopenHarness()
        repo.onAppVisible(true)
        repo.onAndroidLink(AndroidLink.CONNECTED)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, socketEof))
        advanceTimeBy(700)
        repo.onAppVisible(false)
        advanceTimeBy(5_000); runCurrent()
        assertEquals(0, attempts())
    }

    @Test
    @DisplayName("I-1 (c): resume -> one open, once Android's link is read as connected (the pre-background reading is not trusted)")
    fun `resume opens once`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        var attempts = 0
        val (repo, _) = buildRepository(connectionStateMachine = machine, onConnectAttempt = { attempts++ })
        repo.onAndroidLink(AndroidLink.CONNECTED) // a reading taken before the app was visible
        repo.onAppVisible(false)
        repo.onAppVisible(true) // resume: the old reading was forgotten
        runCurrent()
        assertEquals(0, attempts)
        repo.onAndroidLink(AndroidLink.CONNECTED) // the observer's first reading after resume
        runCurrent()
        assertEquals(1, attempts)
        repo.onAppVisible(true); runCurrent() // a second resume with the session still closed (the attempt failed): one more event, one more attempt
        assertEquals(2, attempts)
    }

    @Test
    fun `a reading taken between start and resume opens on resume`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        var attempts = 0
        val (repo, _) = buildRepository(connectionStateMachine = machine, onConnectAttempt = { attempts++ })
        repo.onAndroidLink(AndroidLink.CONNECTED) // ON_START: the observer's first reading, not yet visible
        repo.onAppVisible(true) // ON_RESUME
        runCurrent()
        assertEquals(1, attempts)
    }

    // ---- I-6 (ai-sessions/0048): the ring notice survives Disconnect -------------------------------------------------------------

    @Test
    @DisplayName("I-6: CAP-062 frame 9772 (Ring Left) ACKed by 9783; Disconnect keeps the notice (the ring sounded on until Stop); an ACKed Stop clears it")
    fun `a ring notice survives Disconnect and is cleared only by an ACKed Stop`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = { ch, frame -> if (frame.toHex() == "0401000102" || frame.toHex() == "0401000100") transport.emit(ch, hex("ff010003040100")) } // 9783
        assertEquals(BudsResult.Success(Unit), repo.ringBud(RingTarget.LEFT))
        assertEquals("0401000102", transport.sent.single().second.toHex()) // CAP-062 frame 9772
        assertEquals(RingNotice(RingTarget.LEFT), repo.ringing.first())

        repo.disconnect()

        assertEquals(RingNotice(RingTarget.LEFT, fromEarlierSession = true), repo.ringing.first(), "kept: the app cannot know it stopped")
    }

    @Test
    fun `a session loss keeps the ring notice, marked as from an earlier session (I-6)`() = runTest {
        val (repo, transport) = buildRepository()
        repo.ringBud(RingTarget.RIGHT)
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, "IOException: bt socket closed, read return: -1"))
        assertEquals(RingNotice(RingTarget.RIGHT, fromEarlierSession = true), repo.ringing.first())
    }

    @Test
    fun `after a reconnect an ACKed Stop clears the notice, a new Ring replaces it (I-6)`() = runTest {
        val machine = buildConnectionStateMachine()
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        repo.ringBud(RingTarget.LEFT)
        repo.disconnect()
        transport.connected = true
        machine.onConnectRequested(); machine.onLinkEstablished(); machine.onReady() // the reconnect (connect() itself needs a BluetoothDevice)
        transport.openChannels.clear()
        transport.emit(Dlci.MAESTRO, Cap061.announcementFrame()) // a new session announces its firmware again
        settle()
        repo.ringBud(RingTarget.RIGHT)
        assertEquals(RingNotice(RingTarget.RIGHT), repo.ringing.first(), "a new Ring of the other side replaces the old notice")
        repo.stopRinging()
        assertNull(repo.ringing.first())
    }

    @Test
    @DisplayName("CAP-061 frame 1508 (real bytes): the announcement's firmware becomes deviceInfo and clears the Safe Mode gate")
    fun `the real announcement's firmware becomes deviceInfo and unlocks writes`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        transport.onOpenChannel = { ch -> if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM) transport.emit(ch, MODEL_ID_PRO_2) }
        transport.onSent = { ch, frame ->
            if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM && frame.toHex() == Cap065.GET) transport.emit(ch, hex(Cap065.NOTIFY_E8_OFF_6334))
            if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM && frame.toHex().startsWith("0812")) transport.emit(ch, hex("ff010006081201e8e808"))
        }
        settle()
        assertNull(repo.deviceInfo.first())

        transport.emit(Dlci.MAESTRO, Cap061.announcementFrame())
        settle()

        assertEquals(listOf("release_5.203"), repo.deviceInfo.first()?.firmware)
        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ACTIVE), "the verified firmware must not be in Safe Mode")
        assertEquals(listOf(Cap065.GET, Cap065.SET_ACTIVE_10790), transport.sent.map { it.second.toHex() }, "the Get, then the ANC Set left the phone")
        assertNull(repo.safeMode.first())
    }

    @Test
    @DisplayName("F-5: CAP-065 frame 1883 (whole real frame) -> deviceInfo with the three entries, channel 19 and its receive time — what the Info tab shows")
    fun `the announcement's entries, channel and time reach deviceInfo`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        advanceTimeBy(4_000)
        transport.emit(Dlci.MAESTRO, hex(Cap065.ANNOUNCEMENT_FRAME_1883))
        settle()

        val info = repo.deviceInfo.first()
        assertEquals(listOf("release_5.203"), info?.firmware)
        assertEquals(listOf(1, 2, 3), info?.entries?.map { it.index })
        assertEquals(19, info?.maestroChannel)
        assertEquals(4_000L, info?.announcedAtMillis)
        repo.disconnect()
        assertNull(repo.deviceInfo.first(), "after Disconnect the Info tab says not connected yet")
    }

    // ---- on-demand Message Stream claim (DECISIONS.md ADR-032) ------------------------------------

    @Test
    fun `an ANC tap claims the Message Stream, sends on it, and releases it after the linger`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        assertEquals(emptyList<Int>(), transport.openChannelCalls)

        repo.setAncMode(AncMode.ADAPTIVE)

        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.openChannelCalls)
        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM, Dlci.FAST_PAIR_MESSAGE_STREAM), transport.sent.map { it.first }, "Get and Set on the one claim")
        assertEquals(true, transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM), "still claimed during the linger")

        advanceTimeBy(1_600)
        runCurrent()

        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.closeChannelCalls)
        assertEquals(false, transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM), "released so Play services can take it back")
    }

    @Test
    fun `a second action inside the linger reuses the open channel and only the last one releases it`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        repo.ringBud(RingTarget.LEFT)
        advanceTimeBy(700)
        repo.stopRinging()

        assertEquals(1, transport.openChannelCalls.size, "no second claim while the first is still held")
        advanceTimeBy(1_000) // 1.0 s after the second action: the first action's timer must have been cancelled
        runCurrent()
        assertEquals(emptyList<Int>(), transport.closeChannelCalls)
        advanceTimeBy(700)
        runCurrent()
        assertEquals(1, transport.closeChannelCalls.size)
    }

    @Test
    fun `a busy Message Stream fails the action with the reason, sends nothing, and clears on the next success`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        val busy = BudsError.ChannelUnavailable(Dlci.FAST_PAIR_MESSAGE_STREAM, "read failed, socket might closed")
        transport.openChannelShouldFail = busy

        val failed = repo.setAncMode(AncMode.OFF)

        assertEquals(busy, (failed as BudsResult.Failure).error)
        assertEquals(busy, repo.messageStreamError.first())
        assertEquals(emptyList<Pair<Int, ByteArray>>().size, transport.sent.size)
        // The session itself is untouched — Play services holding DLCI 0x04 is not a lost connection.
        assertInstanceOf(ConnectionState.Ready::class.java, repo.connectionState.first())

        transport.openChannelShouldFail = null
        assertInstanceOf(BudsResult.Success::class.java, repo.setAncMode(AncMode.OFF))
        assertNull(repo.messageStreamError.first())
    }

    @Test
    fun `losing the on-demand channel is not a session loss and the next tap claims it again`() = runTest {
        val machine = buildConnectionStateMachine()
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        settle()
        repo.setAncMode(AncMode.OFF)

        transport.emitChannelClosed(Dlci.FAST_PAIR_MESSAGE_STREAM, "taken by another client")
        settle()

        assertEquals(ConnectionState.Ready, machine.state.value)
        repo.setAncMode(AncMode.ADAPTIVE)
        assertEquals(2, transport.openChannelCalls.size, "claimed again after it was taken away")
    }

    @Test
    fun `an ANC or Find tap while the session is not Ready fails without touching the Message Stream`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        settle()

        assertEquals(BudsError.ConnectionLost, (repo.setAncMode(AncMode.OFF) as BudsResult.Failure).error)
        assertEquals(BudsError.ConnectionLost, (repo.ringBud(RingTarget.RIGHT) as BudsResult.Failure).error)
        assertEquals(emptyList<Int>(), transport.openChannelCalls)
    }

    @Test
    fun `the Buds' own Notify wins over the optimistic ANC update`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.onSent = { ch, frame ->
            when (frame.toHex().take(4)) {
                "0811" -> transport.emit(ch, ancNotify("e8", "40")) // the claim's Get: allowed, Adaptive
                // No ACK: the Buds answer the Set with their real state (Off, CAP-036 frame 1182's bytes) — the Notify is the only answer.
                "0812" -> transport.emit(ch, hex("0813000401e80020"))
            }
        }

        val result = repo.setAncMode(AncMode.ACTIVE)

        assertEquals(BudsResult.Success(Unit), result)
        assertEquals(AncMode.OFF, repo.ancMode.first())
    }

    @Test
    @DisplayName(
        "A68-APP-13, labelled supplementary structural test (a hand-ordered sequence of real frames, not a capture): a Notify of the old mode " +
            "followed by a NAK — the Notify is the answer, the screen keeps the Buds' own mode",
    )
    fun `a re-Notify of the unchanged mode before a NAK leaves the Buds' mode on screen`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.onSent = { ch, frame ->
            when (frame.toHex().take(4)) {
                "0811" -> transport.emit(ch, hex(Cap064.NOTIFY_E8_OFF_4103)) // allowed, OFF
                "0812" -> {
                    transport.emit(ch, hex(Cap064.NOTIFY_E8_OFF_4103)) // the Buds repeat their unchanged mode …
                    transport.emit(ch, hex(Cap064.NAK_3440)) // … and then refuse the Set (`CAP-064` 3440's bytes)
                }
            }
        }

        val result = repo.setAncMode(AncMode.ADAPTIVE)
        settle()

        // What `ai-sessions/0068` flagged: the result is Success although the Set was refused. The mode shown is right — OFF, the Buds' own report, never the
        // requested ADAPTIVE — which is the documented meaning of a Notify answer ("their Notify has already applied the real one"). No capture shows a
        // Notify between a Set and its NAK (`CAP-062`, `CAP-064`: the NAK comes first), so the wait is left as it is; `TODO.md` carries the question.
        assertEquals(BudsResult.Success(Unit), result)
        assertEquals(AncMode.OFF, repo.ancMode.first())
        assertNull(repo.ancModeUnconfirmedAt.first())
    }

    @Test
    fun `a Find My Buds tap returns as soon as the Buds ACK, not after the full wait`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.onSent = null // the test emits the ACK itself
        var finishedAt = -1L

        val tap = launch { repo.ringBud(RingTarget.LEFT); finishedAt = testScheduler.currentTime }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("ff010003" + "040100")) // ACK of a Ring (real frame)
        tap.join()

        assertEquals(0L, finishedAt, "no virtual time may pass: the ACK ended the wait")
    }

    @Test
    fun `an explicit disconnect clears the Message Stream error`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.openChannelShouldFail = BudsError.ChannelUnavailable(Dlci.FAST_PAIR_MESSAGE_STREAM, "busy")
        repo.setAncMode(AncMode.OFF)

        repo.disconnect()

        assertNull(repo.messageStreamError.first())
    }

    @Test
    fun `a Battery updated frame fills Left and Right from the Buds' own push, Case stays unavailable`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        val job = launch { repo.batteryStatus.first { it.left is io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known } }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "605fff")) // 96 % / 95 %
        job.join()

        val status = repo.batteryStatus.value
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known(96, false, receivedAtMillis = 0), status.left)
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known(95, false, receivedAtMillis = 0), status.right)
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Unavailable, status.case)
    }

    @Test
    fun `a charging-regime battery frame shows the charging state and replaces the earlier value, the Case stays unavailable`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        val first = launch { repo.batteryStatus.first { it.left is BatteryLevel.Known } }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "6464ff")) // both in the ears, 100 %
        first.join()
        assertEquals(BatteryLevel.Known(100, false, receivedAtMillis = 0), repo.batteryStatus.value.left)

        val second = launch { repo.batteryStatus.first { (it.left as? BatteryLevel.Known)?.isCharging == true } }
        runCurrent()
        // Real maintainer frame (2026-09-19): 0xe4 = 100 % charging, 0xdd = 93 % charging (ADR-033's 2026-09-20 update).
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "e4ddff"))
        second.join()

        assertEquals(BatteryLevel.Known(100, true, receivedAtMillis = 0), repo.batteryStatus.value.left)
        assertEquals(BatteryLevel.Known(93, true, receivedAtMillis = 0), repo.batteryStatus.value.right)
        assertEquals(BatteryLevel.Unavailable, repo.batteryStatus.value.case)
    }

    @Test
    fun `a byte the decoder cannot read (0x7f = unknown) replaces a known value with unavailable, never a stale percentage`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        val first = launch { repo.batteryStatus.first { it.left is BatteryLevel.Known } }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "6464ff"))
        first.join()
        val second = launch { repo.batteryStatus.first { it.left is BatteryLevel.Unavailable } }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "7f64ff"))
        second.join()
        assertEquals(BatteryLevel.Unavailable, repo.batteryStatus.value.left)
        assertEquals(BatteryLevel.Known(100, false, receivedAtMillis = 0), repo.batteryStatus.value.right)
    }

    @Test
    fun `a structurally valid but unrecognized frame surfaces as UnidentifiedFrame, not dropped silently`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        var frame: io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame? = null
        val job = launch { frame = repo.unidentifiedFrames.first() }
        runCurrent()
        // Group 0x07 (SASS) Code 0x34 — a real periodic Buds message with no known meaning yet
        // (PROTOCOL.md §6). (This test used Group 0x03 Code 0x03 until ADR-033 unblocked its decoder.)
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0734000c") + ByteArray(12) { 1 })
        job.join()

        assertEquals(0x07, frame?.group)
        assertEquals(0x34, frame?.code)
    }

    // ---- 0044 audit fixes (ai-sessions/0045) ---------------------------------------------------------------------------------

    @Test
    fun `an ANC Set the Buds NAK is a CommandRejected failure and the previous mode stays (APP-3)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "20")) // known mode: Off
        settle()
        // NAK layout per the Fast Pair acknowledgement spec: ff 02 <len> <reason> <group> <code> — reason 0x03 = incorrect MAC.
        transport.onSent = { ch, frame ->
            if (frame.toHex() == Cap065.GET) transport.emit(ch, hex(Cap065.NOTIFY_E8_OFF_6334)) // the claim's Get: allowed (F-1)
            if (frame.toHex().startsWith("0812")) transport.emit(ch, hex("ff02000403081201"))
        }

        val result = repo.setAncMode(AncMode.ACTIVE)

        assertEquals(BudsError.CommandRejected(0x03, "incorrect message authentication code"), (result as BudsResult.Failure).error)
        assertEquals(AncMode.OFF, repo.ancMode.first())
    }

    @Test
    fun `an ANC Set the Buds never answer is a Timeout and the previous mode stays (APP-3)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "20"))
        settle()
        // The claim's Get is answered (allowed, F-1); the Set is not — and the channel stays open (a close would be AnswerCutOff, F-3).
        transport.onSent = { ch, frame -> if (frame.toHex() == Cap065.GET) transport.emit(ch, hex(Cap065.NOTIFY_E8_OFF_6334)) }

        val result = repo.setAncMode(AncMode.TRANSPARENT)

        assertEquals(BudsError.Timeout, (result as BudsResult.Failure).error)
        assertEquals(AncMode.OFF, repo.ancMode.first(), "never shown as updated without an answer")
    }

    @Test
    fun `a Ring the Buds NAK fails with the reason and no ringing is claimed`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = { ch, frame -> if (frame.toHex().startsWith("0401")) transport.emit(ch, hex("ff02000401040100")) } // spec example: busy

        val result = repo.ringBud(RingTarget.LEFT)

        assertEquals(BudsError.CommandRejected(0x01, "device busy"), (result as BudsResult.Failure).error)
        assertNull(repo.ringing.first())
    }

    @Test
    fun `a tap cancelled while it waits for the Buds still releases the Message Stream (APP-4)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = null // no answer: the tap is still waiting when it is cancelled

        val tap = launch { repo.setAncMode(AncMode.ADAPTIVE) }
        settle()
        assertEquals(true, transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM))
        tap.cancel()
        settle()
        advanceTimeBy(1_600)
        runCurrent()

        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.closeChannelCalls)
        assertEquals(false, transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM))
    }

    @Test
    fun `a frame cut off by a closed on-demand channel cannot misalign the next claim's frames (APP-2)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("081300")) // the first 3 bytes of a Notify — then Play services takes the channel
        settle()
        transport.emitChannelClosed(Dlci.FAST_PAIR_MESSAGE_STREAM, "taken back")
        settle()

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "40")) // a complete Notify on the next claim
        settle()

        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first())
    }

    @Test
    fun `Safe Mode - an unverified firmware refuses every write, sends nothing and says why (ADR-042)`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        transport.onOpenChannel = { ch -> if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM) transport.emit(ch, MODEL_ID_PRO_2) }
        transport.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_6.000"))
        settle()
        assertEquals(listOf("release_6.000"), repo.safeMode.first()?.firmware, "visible as soon as the announcement arrives")

        assertEquals(BudsError.UnsupportedFirmware, (repo.setAncMode(AncMode.ACTIVE) as BudsResult.Failure).error)
        assertEquals(BudsError.UnsupportedFirmware, (repo.ringBud(RingTarget.RIGHT) as BudsResult.Failure).error)
        assertEquals(BudsError.UnsupportedFirmware, (repo.setEqGains(heavyBass) as BudsResult.Failure).error)
        assertEquals(emptyList<Pair<Int, ByteArray>>(), transport.sent, "no write left the phone")
    }

    @Test
    fun `Safe Mode - reads still work on an unverified firmware (ADR-042)`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        transport.emit(Dlci.MAESTRO, helloWithFirmware(21, 10496, "release_6.000"))
        settle()

        val job = launch { repo.refreshAncMode() }
        runCurrent()
        assertEquals("08110000", transport.sent.single().second.toHex())
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "08"))
        job.join()
        assertEquals(AncMode.ACTIVE, repo.ancMode.first())
    }

    @Test
    fun `Safe Mode - a Model ID other than the Pro 2's refuses Message Stream commands (ADR-042)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onOpenChannel = { ch -> if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM) transport.emit(ch, hex("03010003aabbcc")) }

        val result = repo.setAncMode(AncMode.ACTIVE)

        assertEquals(BudsError.UnsupportedFirmware, (result as BudsResult.Failure).error)
        assertEquals("aabbcc", repo.safeMode.first()?.modelIdHex)
        assertEquals(0, transport.sent.size)
    }

    @Test
    fun `Safe Mode - a claim without a Model ID sends no command after a bounded wait (ADR-042)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onOpenChannel = null

        var result: BudsResult<Unit>? = null
        val tap = launch { result = repo.setAncMode(AncMode.ACTIVE) }
        runCurrent()
        advanceTimeBy(1_100) // MODEL_ID_WAIT_MS
        runCurrent()
        tap.join()

        assertEquals(BudsError.UnsupportedFirmware, (result as BudsResult.Failure).error)
        assertEquals(0, transport.sent.size)
    }

    @Test
    fun `Safe Mode - no firmware announcement at all refuses a write after a bounded wait (ADR-042)`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        transport.onOpenChannel = { ch -> if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM) transport.emit(ch, MODEL_ID_PRO_2) }

        var result: BudsResult<Unit>? = null
        val tap = launch { result = repo.setAncMode(AncMode.ACTIVE) }
        runCurrent()
        advanceTimeBy(3_100) // MAESTRO_ANNOUNCE_WAIT_MS
        runCurrent()
        tap.join()

        assertEquals(BudsError.UnsupportedFirmware, (result as BudsResult.Failure).error)
        assertEquals(0, transport.sent.size)
    }

    @Test
    fun `a Settable reading within 2 s of the claim is provisional, a later one in the same claim is not (ADR-024, APP-5)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = null
        val tap = launch { repo.refreshAncMode() }
        runCurrent() // the claim opens at virtual t = 0
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("00", "20")) // the connect-time answer: "both in the case"
        tap.join()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability.NOT_ALLOWED, repo.ancAvailability.first())
        assertEquals(true, repo.ancAvailabilityProvisional.first())

        advanceTimeBy(1_200) // a spontaneous re-Notify 1.2 s later (CAP-047 frame 3996's pattern) — still inside the linger
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "20"))
        settle()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.AncAvailability.ALLOWED, repo.ancAvailability.first(), "the last value wins")
        assertEquals(true, repo.ancAvailabilityProvisional.first())

        advanceTimeBy(1_000) // 2.2 s after the open
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "20"))
        settle()
        assertEquals(false, repo.ancAvailabilityProvisional.first())
    }

    // ---- `ai-sessions/0069`: A68-APP-03 (the newest ANC report is never dropped) and A68-APP-02 (one "current" rule; values marked at session end) ----

    @Test
    @DisplayName("A68-APP-03: three Notifies while a collector is busy — the mode is the last one (the replay-1 SharedFlow kept the first)")
    fun `the newest ANC report is never dropped by a slow collector`() = runTest {
        val (repo, transport) = buildRepository()
        val seen = ArrayList<AncMode?>()
        // A collector that is still busy with its first value while the next two arrive (a recomposition, a tile render).
        backgroundScope.launch { repo.ancMode.collect { seen += it; kotlinx.coroutines.delay(10_000) } }
        settle()
        // `CAP-063` 4184 (`e8 08`, ACTIVE), `CAP-064` 3200 (`e8 40`, ADAPTIVE), `CAP-064` 4103 (`e8 20`, OFF).
        for (frame in listOf(Cap063.NOTIFY_SETTABLE_E8_4184, Cap064.NOTIFY_E8_ADAPTIVE_3200, Cap064.NOTIFY_E8_OFF_4103)) {
            transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(frame))
            settle()
        }
        assertEquals(AncMode.OFF, repo.ancMode.first(), "the Buds' last report")
        assertEquals(currentTime, repo.ancModeUpdatedAt.first())
    }

    @Test
    @DisplayName("A68-APP-02: the ANC mode survives a new Connect but is not current until this connection reports one")
    fun `a mode from the last connection is not current after a new Connect`() = runTest {
        val (repo, transport) = buildRepository()
        assertNull(repo.sessionSince.first(), "no Connect in this app run yet")
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap064.NOTIFY_E8_ADAPTIVE_3200))
        settle()
        val reportedAt = repo.ancModeUpdatedAt.first()

        repo.disconnect()
        advanceTimeBy(5_000)
        repo.connect() // no bonded device in this test: the attempt ends NotPaired, but it is a new connection attempt all the same
        settle()

        assertEquals(currentTime, repo.sessionSince.first())
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first(), "kept, not cleared")
        assertEquals(false, isCurrent(ready = true, valueAtMillis = reportedAt, sessionSinceMillis = repo.sessionSince.first()))

        advanceTimeBy(300)
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap064.NOTIFY_E8_OFF_4103))
        settle()
        assertEquals(true, isCurrent(ready = true, valueAtMillis = repo.ancModeUpdatedAt.first(), sessionSinceMillis = repo.sessionSince.first()))
    }

    @Test
    @DisplayName("A68-APP-02: Disconnect and a lost session mark the battery values as last seen at once, not only at the next Connect")
    fun `the battery values are marked when the session ends`() = runTest {
        for (end in listOf("disconnect", "loss")) {
            val (repo, transport) = buildRepository()
            // `CAP-063` frame 2756: both buds charging, 100 %.
            transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap063.BATTERY_BOTH_CHARGING_2756))
            settle()
            assertEquals(false, (repo.batteryStatus.value.left as BatteryLevel.Known).isStale, end)

            if (end == "disconnect") repo.disconnect() else transport.emitConnectionLost(ConnectionLoss(channelId = 2, detail = "scripted"))
            settle()

            val status = repo.batteryStatus.value
            assertEquals(true, (status.left as BatteryLevel.Known).isStale, end)
            assertEquals(true, (status.right as BatteryLevel.Known).isStale, end)
            assertEquals(true, status.leftCharging?.fromEarlierSession, end)
        }
    }

    // ---- ADR-059 (`ai-sessions/0082` item 3): the worn reading, derived from state the app already holds ----

    @Test
    @DisplayName("ADR-059: not read until a Notify; CAP-065 5465 (00) -> not worn; 6334 (e8) -> probably worn; CAP-062 4845 -> both in the case; field 2 = 0 -> unknown; Connect resets")
    fun `the worn reading follows the Notify, field 2 and the charging flags`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        assertEquals(WornReading.NotRead, repo.wornReading.first())

        transport.emit(Dlci.MAESTRO, hex(Settings036.READ_2_RESP)) // CAP-036 1447: `4:{2:1}`, in-ear detection on
        settle()
        assertEquals(WornReading.NotRead, repo.wornReading.first(), "still no Notify on this connection")

        advanceTimeBy(1_000)
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap065.NOTIFY_00_OFF_5465)) // both buds loose on the table
        settle()
        assertEquals(WornReading.NotWorn(currentTime), repo.wornReading.first())

        advanceTimeBy(1_000)
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap065.NOTIFY_E8_OFF_6334)) // both worn
        settle()
        val checkedAt = currentTime
        assertEquals(WornReading.ProbablyWorn(checkedAt), repo.wornReading.first())

        advanceTimeBy(1_000)
        transport.emit(Dlci.MAESTRO, hex(Cap062.STREAM_BOTH_4845)) // both buds charging
        settle()
        assertEquals(WornReading.BothInCase, repo.wornReading.first())

        transport.emit(Dlci.MAESTRO, hex(Cap062.STREAM_NONE_2782)) // neither charging: back to the byte's reading, with the Notify's own time
        settle()
        assertEquals(WornReading.ProbablyWorn(checkedAt), repo.wornReading.first())

        // The app's own write of field 2 = 0 (CAP-064 9922 → OK): the byte says nothing any more.
        transport.onSent = { ch, frame -> if (ch == Dlci.MAESTRO) transport.emit(Dlci.MAESTRO, hex(SettingsWrites.ACK_CH21_1731)) }
        assertEquals(BudsResult.Success(Unit), repo.setInEarDetection(false))
        settle()
        assertEquals(WornReading.InEarDetectionOff, repo.wornReading.first())

        repo.disconnect()
        repo.connect() // NotPaired in this test, but a new connection attempt: the Settable byte and field 2 are this connection's to re-read
        settle()
        assertEquals(WornReading.NotRead, repo.wornReading.first())
    }

    // ---- `ai-sessions/0082` item 1: why the shown ANC mode is what it is — read, set by this app, or changed by the Buds (the maintainer's rule, chat 2026-10-09) ----

    @Test
    @DisplayName("Item 1: the answer to the app's own Get is a reading (CAP-045 609 -> 612), never \"changed by the Buds\"")
    fun `the Get's answer is a reading`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        assertNull(repo.ancModeCause.first(), "nothing reported yet")
        transport.onSent = null // the test answers the Get itself
        val job = launch { repo.refreshAncMode() }
        runCurrent()
        assertEquals(Cap045.GET_609, transport.sent.single().second.toHex())
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_ADAPTIVE_612))
        job.join()
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first())
        assertEquals(AncModeCause.READ, repo.ancModeCause.first())
    }

    @Test
    @DisplayName("Item 1: a Notify the app did not provoke, with another mode, is changed by the Buds (CAP-045 1583, 1755, 1818); the same mode again changes nothing")
    fun `an unsolicited Notify with another mode is changed by the Buds`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.onSent = null
        val job = launch { repo.refreshAncMode() }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_ADAPTIVE_612)) // the Get's answer
        job.join()
        assertEquals(AncModeCause.READ, repo.ancModeCause.first())

        advanceTimeBy(24_477) // 06:23:21.948 − 06:22:57.471
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_TRANSPARENT_1583)) // a press-and-hold on a bud
        settle()
        assertEquals(AncMode.TRANSPARENT, repo.ancMode.first())
        assertEquals(AncModeCause.CHANGED_BY_BUDS, repo.ancModeCause.first())
        assertEquals(currentTime, repo.ancModeUpdatedAt.first(), "the line's time is the mode's receive time")

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_ACTIVE_1755))
        settle()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_ADAPTIVE_1818))
        settle()
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first())
        assertEquals(AncModeCause.CHANGED_BY_BUDS, repo.ancModeCause.first())

        // The Buds repeat the mode already shown (nothing pending): the cause stays, only the time moves.
        advanceTimeBy(1_000)
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_ADAPTIVE_1818))
        settle()
        assertEquals(AncModeCause.CHANGED_BY_BUDS, repo.ancModeCause.first())
        assertEquals(currentTime, repo.ancModeUpdatedAt.first())
    }

    @Test
    @DisplayName("Item 1: the app's own Set (Get -> Notify -> Set -> ACK), then the Buds' Notify of that mode: set by this app, never changed by the Buds")
    fun `the app's own acknowledged Set is set by this app`() = runTest {
        val (repo, transport) = buildRepository() // autoAck: the Get answered by CAP-065 6334 (OFF, e8), the Set ACKed
        settle()

        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ADAPTIVE))
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first())
        assertEquals(AncModeCause.SET_BY_APP, repo.ancModeCause.first())

        // The Buds' own Notify after the ACK (as CAP-059 2779 -> 2782): the requested mode — still the app's change.
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_ADAPTIVE_612))
        settle()
        assertEquals(AncModeCause.SET_BY_APP, repo.ancModeCause.first())
    }

    @Test
    @DisplayName("Item 1: the Buds answer the Set with their Notify of the requested mode before any ACK — the app's change, not theirs")
    fun `a Notify of the requested mode while the Set waits is set by this app`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        transport.onSent = { ch, frame ->
            when (frame.toHex().take(4)) {
                "0811" -> transport.emit(ch, hex(Cap065.NOTIFY_E8_OFF_6334))
                "0812" -> transport.emit(ch, hex(Cap045.NOTIFY_TRANSPARENT_1583)) // the requested mode, as a Notify, no ACK
            }
        }
        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.TRANSPARENT))
        assertEquals(AncMode.TRANSPARENT, repo.ancMode.first())
        assertEquals(AncModeCause.SET_BY_APP, repo.ancModeCause.first())
    }

    @Test
    @DisplayName("Item 1: a new Connect resets the cause; the first Notify of this app run is a reading")
    fun `Connect resets the cause and the first mode ever shown is a reading`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        // No Get pending, no mode shown yet: nothing could have changed.
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_ADAPTIVE_612))
        settle()
        assertEquals(AncModeCause.READ, repo.ancModeCause.first())
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_TRANSPARENT_1583))
        settle()
        assertEquals(AncModeCause.CHANGED_BY_BUDS, repo.ancModeCause.first())

        repo.disconnect()
        repo.connect() // no bonded device in this test: NotPaired, but a new connection attempt all the same (A68-APP-02)
        settle()
        assertEquals(AncMode.TRANSPARENT, repo.ancMode.first(), "the mode is kept (from the last connection)")
        assertNull(repo.ancModeCause.first(), "its cause is this connection's to re-establish")
    }

    // ---- DECISIONS.md ADR-061 (`ai-sessions/0084`): the Message Stream claim held while the noise-control tab is on screen ----------------------------------

    /** The repository with the app visible and the noise-control tab shown: the entry claim's `Get` answered by [getAnswer] (a real `Notify`). */
    private suspend fun TestScope.holdingRepository(getAnswer: String = Cap045.NOTIFY_ADAPTIVE_612): Pair<BudsRepositoryImpl, FakeBudsTransport> {
        val (repo, transport) = buildRepository()
        transport.onSent = { ch, frame ->
            when (frame.toHex().take(4)) {
                "0811" -> transport.emit(ch, hex(getAnswer))
                "0812" -> transport.emit(ch, hex("ff010006081201e8e8") + byteArrayOf(frame[7])) // CAP-001 2041's ACK shape
                "0401" -> transport.emit(ch, hex("ff010003040100"))
            }
        }
        repo.onAppVisible(true) // Android's link is UNKNOWN in these tests: ADR-044's re-open never fires
        settle()
        repo.setAncTabShown(true)
        settle()
        return repo to transport
    }

    @Test
    @DisplayName("ADR-061: entering the noise-control tab claims once — one open, the Get 08 11 00 00 — and holds it: nothing released after 10 s")
    fun `entering the noise-control tab claims once and holds the claim`() = runTest {
        BleLogger.clear()
        val (repo, transport) = holdingRepository()

        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.openChannelCalls, "one claim")
        assertEquals(listOf(Cap072.GET_A13499), transport.sent.map { it.second.toHex() }, "the existing claim's Get, nothing else")
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first())
        assertEquals(AncModeCause.READ, repo.ancModeCause.first(), "the Get's answer is a reading")
        advanceTimeBy(10_000); runCurrent()
        assertEquals(emptyList<Int>(), transport.closeChannelCalls, "held, not released")
        assertEquals(true, transport.isChannelOpen(Dlci.FAST_PAIR_MESSAGE_STREAM))
        val log = BleLogger.exportLog()
        assertEquals(true, "Message Stream hold started: the noise-control tab is on screen (ADR-061)" in log)
    }

    @Test
    @DisplayName("ADR-061: during the hold CAP-045 1583 (unprovoked, nothing pending) arrives and is shown as changed by the Buds")
    fun `an unprovoked Notify during the hold is changed by the Buds`() = runTest {
        val (repo, transport) = holdingRepository(getAnswer = Cap045.NOTIFY_ADAPTIVE_612)
        advanceTimeBy(24_477) // CAP-045: 06:22:57.471 -> 06:23:21.948
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap045.NOTIFY_TRANSPARENT_1583))
        settle()
        assertEquals(AncMode.TRANSPARENT, repo.ancMode.first())
        assertEquals(AncModeCause.CHANGED_BY_BUDS, repo.ancModeCause.first())
        assertEquals(currentTime, repo.ancModeUpdatedAt.first())
        assertEquals(1, transport.sent.size, "the app sends nothing in reaction")
        assertEquals(1, transport.openChannelCalls.size)
    }

    @Test
    @DisplayName("ADR-061: CAP-072 A 13515 -> A 13519 and B 825 — the Buds' own changes during the hold are changed by the Buds; the Settable byte follows")
    fun `the CAP-072 Notify frames during the hold`() = runTest {
        val (repo, transport) = holdingRepository(getAnswer = Cap072.NOTIFY_OFF_00_A13515)
        assertEquals(AncMode.OFF, repo.ancMode.first())
        assertEquals(AncModeCause.READ, repo.ancModeCause.first())
        assertEquals(AncAvailability.NOT_ALLOWED, repo.ancAvailability.first())

        advanceTimeBy(63) // A 13515 -> A 13519
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap072.NOTIFY_TRANSPARENT_A13519))
        settle()
        assertEquals(AncMode.TRANSPARENT, repo.ancMode.first())
        assertEquals(AncModeCause.CHANGED_BY_BUDS, repo.ancModeCause.first())
        assertEquals(AncAvailability.ALLOWED, repo.ancAvailability.first())

        advanceTimeBy(60_000)
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex(Cap072.NOTIFY_ACTIVE_B825))
        settle()
        assertEquals(AncMode.ACTIVE, repo.ancMode.first())
        assertEquals(AncModeCause.CHANGED_BY_BUDS, repo.ancModeCause.first())
        assertEquals(1, transport.openChannelCalls.size)
        assertEquals(emptyList<Int>(), transport.closeChannelCalls)
    }

    @Test
    @DisplayName("ADR-061: a tap during the hold reuses the open channel (no second claim), sends its Get first, is set by this app, and the claim stays held")
    fun `a tap during the hold reuses the channel`() = runTest {
        val (repo, transport) = holdingRepository(getAnswer = Cap072.NOTIFY_TRANSPARENT_A11938)

        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ACTIVE))
        assertEquals(1, transport.openChannelCalls.size, "no second SABM")
        assertEquals(
            listOf(Cap072.GET_A13499, Cap072.GET_A13499, Cap065.SET_ACTIVE_10790),
            transport.sent.map { it.second.toHex() },
            "the entry's Get, the tap's Get, then its Set (CAP-065 10790 = CAP-072 A 11939's bytes)",
        )
        assertEquals(AncModeCause.SET_BY_APP, repo.ancModeCause.first())
        advanceTimeBy(10_000); runCurrent()
        assertEquals(emptyList<Int>(), transport.closeChannelCalls, "still held after the tap")
    }

    @Test
    @DisplayName("ADR-061: the tile during the hold (stepAncMode) is no second claim and does not end the hold")
    fun `the tile during the hold keeps it`() = runTest {
        val (repo, transport) = holdingRepository(getAnswer = Cap072.NOTIFY_TRANSPARENT_A11938)
        assertEquals(BudsResult.Success(Unit), repo.stepAncMode(AncMode::nextForTile))
        assertEquals(1, transport.openChannelCalls.size)
        advanceTimeBy(10_000); runCurrent()
        assertEquals(emptyList<Int>(), transport.closeChannelCalls)
    }

    @Test
    @DisplayName("ADR-061: leaving the tab releases the claim after MESSAGE_STREAM_LINGER_MS (1.5 s), not before; the log names the reason")
    fun `leaving the tab releases after the linger`() = runTest {
        BleLogger.clear()
        val (repo, transport) = holdingRepository()
        advanceTimeBy(5_000); runCurrent()
        repo.setAncTabShown(false)
        runCurrent()
        advanceTimeBy(1_400); runCurrent()
        assertEquals(emptyList<Int>(), transport.closeChannelCalls, "not before 1.5 s")
        advanceTimeBy(200); runCurrent()
        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.closeChannelCalls)
        assertEquals(true, "Message Stream hold ended: the noise-control tab was left" in BleLogger.exportLog())
    }

    @Test
    @DisplayName("ADR-061: leaving the app (onAppVisible false) releases after 1.5 s, not before")
    fun `leaving the app releases after the linger`() = runTest {
        BleLogger.clear()
        val (repo, transport) = holdingRepository()
        repo.onAppVisible(false)
        runCurrent()
        advanceTimeBy(1_400); runCurrent()
        assertEquals(emptyList<Int>(), transport.closeChannelCalls)
        advanceTimeBy(200); runCurrent()
        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.closeChannelCalls)
        assertEquals(true, "Message Stream hold ended: the app is not visible" in BleLogger.exportLog())
    }

    @Test
    @DisplayName("ADR-061: re-entering the tab after the release claims once more; a quick return within the linger keeps the claim instead")
    fun `re-entering the tab claims again, a quick return keeps the claim`() = runTest {
        val (repo, transport) = holdingRepository()
        repo.setAncTabShown(false); runCurrent()
        advanceTimeBy(700); runCurrent()
        repo.setAncTabShown(true); settle() // back within the linger: the pending release is cancelled, no new claim
        advanceTimeBy(10_000); runCurrent()
        assertEquals(1, transport.openChannelCalls.size)
        assertEquals(emptyList<Int>(), transport.closeChannelCalls)

        repo.setAncTabShown(false); runCurrent()
        advanceTimeBy(1_600); runCurrent()
        assertEquals(1, transport.closeChannelCalls.size)
        repo.setAncTabShown(true); settle()
        assertEquals(2, transport.openChannelCalls.size, "one new claim with its Get")
        assertEquals(listOf(Cap072.GET_A13499, Cap072.GET_A13499), transport.sent.map { it.second.toHex() })
    }

    @Test
    @DisplayName("ADR-061: the Buds (or another client) close DLCI 0x04 under the hold — no re-claim by itself; the next tap claims again and holds")
    fun `a channel closed under the hold is not re-claimed by itself`() = runTest {
        BleLogger.clear()
        val (repo, transport) = holdingRepository(getAnswer = Cap072.NOTIFY_TRANSPARENT_A11938)
        transport.emitChannelClosed(Dlci.FAST_PAIR_MESSAGE_STREAM, "stream closed (EOF)")
        settle()
        advanceTimeBy(30_000); runCurrent()
        assertEquals(1, transport.openChannelCalls.size, "no re-claim by itself")
        assertEquals(true, "Message Stream hold ended: the channel was closed, not by this app" in BleLogger.exportLog())

        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ACTIVE))
        assertEquals(2, transport.openChannelCalls.size, "the tap claims again")
        advanceTimeBy(10_000); runCurrent()
        assertEquals(emptyList<Int>(), transport.closeChannelCalls, "and the claim is held again (the tab is still shown)")
    }

    @Test
    @DisplayName("ADR-061: a session loss ends the hold; nothing claims by itself afterwards (the next claim — a pull here — is held again)")
    fun `a session loss under the hold claims nothing by itself`() = runTest {
        BleLogger.clear()
        val machine = buildConnectionStateMachine()
        val (repo, transport) = buildRepository(connectionStateMachine = machine)
        transport.onSent = { ch, frame -> if (frame.toHex().take(4) == "0811") transport.emit(ch, hex(Cap045.NOTIFY_ADAPTIVE_612)) }
        repo.onAppVisible(true); repo.setAncTabShown(true); settle()
        assertEquals(1, transport.openChannelCalls.size)

        transport.emitConnectionLost(ConnectionLoss(channelId = Dlci.MAESTRO, detail = "bt socket closed, read return: -1"))
        transport.openChannels.clear() // the real transport has closed every socket of the connection before it reports the loss (BudsTransport.connectionLost)
        settle()
        assertEquals(ConnectionState.Disconnected, repo.connectionState.first())
        assertEquals(true, "Message Stream hold ended: the session was lost" in BleLogger.exportLog())
        advanceTimeBy(30_000); runCurrent()
        assertEquals(1, transport.openChannelCalls.size, "no claim by itself after the loss")

        // A new session (here driven directly — connect() needs a BluetoothDevice): its first claim is kept as the hold, the tab still shown.
        transport.connected = true
        machine.onConnectRequested(); machine.onLinkEstablished(); machine.onReady()
        assertEquals(BudsResult.Success(AncMode.ADAPTIVE), repo.refreshAncMode())
        assertEquals(2, transport.openChannelCalls.size)
        advanceTimeBy(10_000); runCurrent()
        assertEquals(emptyList<Int>(), transport.closeChannelCalls, "held again: the noise-control tab is still shown")
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

/** Device Information Model ID of the Pixel Buds Pro 2 (`CAP-059` frame 1049, PROTOCOL.md §0.1). */
private val MODEL_ID_PRO_2 = hex("03010003da2db1")
