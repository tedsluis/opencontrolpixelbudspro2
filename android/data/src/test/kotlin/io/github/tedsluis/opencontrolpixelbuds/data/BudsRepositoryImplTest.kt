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

import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap061
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Cap062
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Dlci
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Hdlc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Maestro
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RpcPacket
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Settings036
import io.github.tedsluis.opencontrolpixelbuds.data.codec.SettingsWrites
import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsSettings
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction
import io.github.tedsluis.opencontrolpixelbuds.domain.SettingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingSource
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.RingNotice
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionLoss
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine
import io.github.tedsluis.opencontrolpixelbuds.hardware.FakeBudsTransport
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
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
            // The real announcement (CAP-061 frame 1508, serial redacted) — not a hand-made one: the synthetic shape without the fixed64
            // field 5 hid the parser defect that put the verified Buds in Safe Mode on hardware (ai-sessions/0046).
            transport.emit(Dlci.MAESTRO, Cap061.announcementFrame())
            settle()
        }
        return repo to transport
    }

    /** The Buds' ACK for an ANC Set (`ff 01 00 06 08 12 01 e8 e8 <mode>`, `CAP-001` frame 2041) or a Ring (`ff 01 00 03 04 01 00`). */
    private suspend fun autoAck(transport: FakeBudsTransport, channel: Int, frame: ByteArray) {
        if (channel != Dlci.FAST_PAIR_MESSAGE_STREAM || frame.size < 4) return
        when (frame.toHex().take(4)) {
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
    fun `setAncMode sends the exact wire bytes and optimistically updates ancMode`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        val result = repo.setAncMode(AncMode.ADAPTIVE)
        assertInstanceOf(BudsResult.Success::class.java, result)

        val sent = transport.sent.single()
        assertEquals(Dlci.FAST_PAIR_MESSAGE_STREAM, sent.first)
        assertEquals("0812001401e8e840" + "00".repeat(16), sent.second.toHex())
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first())
    }

    @Test
    fun `a real inbound ANC Notify frame updates ancMode`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

        var mode: AncMode? = null
        val job = launch { mode = repo.ancMode.first() }
        runCurrent()

        // CAP-036 frame 1182 (PROTOCOL.md §4.1), current state = Off.
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e80020"))
        job.join()

        assertEquals(AncMode.OFF, mode)
    }

    @Test
    fun `refreshAncMode sends Get and resolves once a fresh Notify arrives`() = runTest {
        val (repo, transport) = buildRepository()
        settle()

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
        val (repo, _) = buildRepository()
        settle()

        val result = repo.refreshAncMode()
        assertInstanceOf(BudsResult.Failure::class.java, result)
        assertEquals(BudsError.Timeout, (result as BudsResult.Failure).error)
    }

    // ---- Maestro / EQ (DECISIONS.md ADR-034) ------------------------------------------------------

    /** The Buds' unsolicited announcement of this connection's pw_rpc channel (real header, CAP-036 frame 1405 / CAP-015 frame 1879). */
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
            }
        }
    }

    /** The field (`4:N`) of every `ReadSetting` the app sent, in order, and the method of every DLCI 0x02 request. */
    private fun FakeBudsTransport.maestroRequests() = sent.filter { it.first == Dlci.MAESTRO }.map {
        (PwRpc.decode((Hdlc.decode(it.second) as BudsResult.Success).value.payload) as BudsResult.Success).value
    }

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
        assertEquals(8, requests.size, "the EQ read, six settings reads (ADR-036), then the runtime-info subscription (ADR-043)")
        assertEquals(0.3f, repo.eqProfile.first()!!.mid, 1e-4f) // CAP-036 frame 1525
    }

    @Test
    @DisplayName("Connect reads 2, 4, 7, 17, 19, 22 byte-identical to CAP-036 1445 … 1538 and fills the settings from 1447 … 1540, with their time")
    fun `the Connect-time settings reads`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        answerReadsLikeCap036(transport)
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()
        advanceTimeBy(4_000)
        repo.launchInitialEqRead()
        settle()

        val maestro = transport.sent.filter { it.first == Dlci.MAESTRO }.map { it.second.toHex() }
        assertEquals(
            listOf(Settings036.READ_2_REQ, Settings036.READ_4_REQ, Settings036.READ_7_REQ, Settings036.READ_17_REQ, Settings036.READ_19_REQ, Settings036.READ_22_REQ),
            maestro.subList(1, 7),
        )
        assertEquals(Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO, transport.maestroRequests().last().methodId)
        assertEquals(
            BudsSettings(
                inEarDetection = SettingReading(true, 4_000),
                touchControls = SettingReading(true, 4_000),
                holdLeft = SettingReading(HoldAction.NOISE_CONTROL, 4_000),
                holdRight = SettingReading(HoldAction.NOISE_CONTROL, 4_000),
                volumeBalance = SettingReading(5, 4_000),
                monoAudio = SettingReading(false, 4_000),
                conversationDetection = SettingReading(true, 4_000),
            ),
            repo.settings.value,
        )
        assertNull(repo.settingsError.first())
        assertEquals(emptyList<Int>(), transport.maestroRequests().filter { it.methodId == Maestro.METHOD_READ_SETTING }.map { it.payload[1].toInt() }.filter { it == 12 })
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
        assertEquals(listOf(16, 2, 4, 7, 17, 19, 22), reads, "one pass, nothing retried")
        assertNull(repo.settings.value.touchControls)
        assertNull(repo.settings.value.volumeBalance)
        assertEquals(true, repo.settings.value.conversationDetection?.value)
        assertEquals(BudsError.Timeout, repo.settingsError.first(), "the last reason")
        assertEquals(Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO, transport.maestroRequests().last().methodId)
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

    // ---- Case battery from the runtime-info stream (DECISIONS.md ADR-043), dock state (ADR-024), Find state, firmware ----

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
        assertEquals(8, maestro.size, "EQ read, six settings reads, the subscription")
        assertEquals(1, maestro.count { it == "7e004b0310151dea71de7d5e2590821ee66654bfab7e" })
        assertEquals("7e004b0310151dea71de7d5e2590821ee66654bfab7e", maestro.last())
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
        advanceTimeBy(3_100) // … and for the subscription
        runCurrent()

        assertEquals(0, transport.sent.size)
        assertEquals(BudsError.MaestroChannelUnknown(null), repo.settingsError.first())
        assertEquals(BudsError.MaestroChannelUnknown(null), repo.caseBatteryError.first())
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

    // ---- I-3 (ai-sessions/0048): no ANC Set while the Buds report Settable 0x00 -----------------------------------------------------

    @Test
    @DisplayName("I-3: after CAP-062 frame 6000 (Settable 00) a Set sends nothing and claims nothing; after frame 8706 (e8) it is sent again")
    fun `no Set while the Buds allow no ANC change`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e80020")) // frame 6000
        settle()

        val refused = repo.setAncMode(AncMode.TRANSPARENT)

        assertEquals(BudsError.AncNotAllowed, (refused as BudsResult.Failure).error)
        assertEquals(emptyList<Int>(), transport.openChannelCalls, "not even a claim of DLCI 0x04")
        assertEquals(0, transport.sent.size)
        assertNull(repo.messageStreamError.first(), "not a channel problem")

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e8e840")) // frame 8706
        settle()
        transport.onSent = { ch, frame -> if (frame.toHex().startsWith("0812")) transport.emit(ch, hex("ff010006081201e8e840")) } // frame 8705
        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ADAPTIVE))
        assertEquals("0812001401e8e840" + "00".repeat(16), transport.sent.single().second.toHex()) // frame 8694
    }

    @Test
    @DisplayName("I-3: unknown (no Notify yet) stays enabled; the Buds' NAK (frame 5998) + Notify 00 (6000) is reported, then further Sets are held back")
    fun `unknown is enabled and a NAK still reports as before`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = { ch, frame ->
            if (frame.toHex().startsWith("0812")) {
                transport.emit(ch, hex("ff020003020812")) // frame 5998
                transport.emit(ch, hex("0813000401e80020")) // frame 6000
            }
        }

        val first = repo.setAncMode(AncMode.TRANSPARENT)

        assertEquals(BudsError.CommandRejected(0x02, "not allowed in the current state"), (first as BudsResult.Failure).error)
        assertEquals(1, transport.sent.size, "the first Set was sent: nothing was known yet")
        settle()
        assertEquals(BudsError.AncNotAllowed, (repo.setAncMode(AncMode.ADAPTIVE) as BudsResult.Failure).error)
        assertEquals(1, transport.sent.size, "the second one was not")
    }

    @Test
    fun `a Refresh re-reads the byte and re-enables the Set (I-3)`() = runTest {
        val (repo, transport) = buildRepository()
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
        assertNull(repo.settingsError.first())
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
        assertEquals(BudsError.Timeout, repo.settingsError.first())
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
        assertEquals(BudsError.MaestroRejected("RESPONSE NOT_FOUND"), repo.settingsError.first())
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
        transport.onSent = { ch, frame -> if (ch == Dlci.FAST_PAIR_MESSAGE_STREAM && frame.toHex().startsWith("0812")) transport.emit(ch, hex("ff010006081201e8e808")) }
        settle()
        assertNull(repo.deviceInfo.first())

        transport.emit(Dlci.MAESTRO, Cap061.announcementFrame())
        settle()

        assertEquals(listOf("release_5.203"), repo.deviceInfo.first()?.firmware)
        assertEquals(BudsResult.Success(Unit), repo.setAncMode(AncMode.ACTIVE), "the verified firmware must not be in Safe Mode")
        assertEquals("08120014" + "01e8e808", transport.sent.single().second.toHex().take(16), "the ANC Set left the phone")
        assertNull(repo.safeMode.first())
    }

    // ---- on-demand Message Stream claim (DECISIONS.md ADR-032) ------------------------------------

    @Test
    fun `an ANC tap claims the Message Stream, sends on it, and releases it after the linger`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        assertEquals(emptyList<Int>(), transport.openChannelCalls)

        repo.setAncMode(AncMode.ADAPTIVE)

        assertEquals(listOf(Dlci.FAST_PAIR_MESSAGE_STREAM), transport.openChannelCalls)
        assertEquals(Dlci.FAST_PAIR_MESSAGE_STREAM, transport.sent.single().first)
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
        transport.onSent = null // no ACK: the Notify is the only answer

        val tap = launch { repo.setAncMode(AncMode.ACTIVE) }
        runCurrent()
        // The Buds answer with their real state (Off, CAP-036 frame 1182) — e.g. after refusing the Set.
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e80020"))
        tap.join()

        assertEquals(AncMode.OFF, repo.ancMode.first())
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
        transport.onSent = { ch, frame -> if (frame.toHex().startsWith("0812")) transport.emit(ch, hex("ff02000403081201")) }

        val result = repo.setAncMode(AncMode.ACTIVE)

        assertEquals(BudsError.CommandRejected(0x03, "incorrect message authentication code"), (result as BudsResult.Failure).error)
        assertEquals(AncMode.OFF, repo.ancMode.first())
    }

    @Test
    fun `an ANC Set the Buds never answer is a Timeout and the previous mode stays (APP-3)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "20"))
        settle()
        transport.onSent = null

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
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

/** Device Information Model ID of the Pixel Buds Pro 2 (`CAP-059` frame 1049, PROTOCOL.md §0.1). */
private val MODEL_ID_PRO_2 = hex("03010003da2db1")
