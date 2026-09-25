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
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Dlci
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Hdlc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Maestro
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RpcPacket
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
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
            bondedDeviceProvider = { null },
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

    @Test
    fun `the Connect-time read (launchInitialEqRead) waits for the announcement, then reads field 16 on that channel`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        settle()
        transport.onSent = { _, frame ->
            // reply to the read with the requested quintet on the same channel; the runtime-info subscription (ADR-043) gets no reply here
            val rpc = (PwRpc.decode(Hdlc.decode(frame).let { (it as BudsResult.Success).value.payload }) as BudsResult.Success).value
            if (rpc.methodId == Maestro.METHOD_READ_SETTING) {
                assertEquals("2010", rpc.payload.toHex()) // 4:16
                transport.emit(Dlci.MAESTRO, rpcFrame(10496, RpcPacket(PwRpc.TYPE_RESPONSE, rpc.channelId, Maestro.SERVICE_ID, Maestro.METHOD_READ_SETTING, quintetPayload(heavyBass))))
            } else {
                assertEquals(Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO, rpc.methodId)
            }
        }
        repo.launchInitialEqRead()
        advanceTimeBy(1_000)
        assertEquals(0, transport.sent.size) // nothing is sent until the Buds have announced their channel
        transport.emit(Dlci.MAESTRO, helloFrame(21, 10496))
        settle()

        assertEquals(2, transport.sent.size, "the EQ read, then the runtime-info subscription (ADR-043)")
        assertEquals(heavyBass, repo.eqProfile.first())
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
        transport.onSent = { ch, frame ->
            if (ch == Dlci.MAESTRO) {
                val rpc = (PwRpc.decode((Hdlc.decode(frame) as BudsResult.Success).value.payload) as BudsResult.Success).value
                if (rpc.methodId == Maestro.METHOD_READ_SETTING) {
                    transport.emit(Dlci.MAESTRO, rpcFrame(10496, RpcPacket(PwRpc.TYPE_RESPONSE, 21, Maestro.SERVICE_ID, Maestro.METHOD_READ_SETTING, quintetPayload(heavyBass))))
                }
            }
        }
        repo.launchInitialEqRead()
        settle()

        val maestro = transport.sent.filter { it.first == Dlci.MAESTRO }.map { it.second.toHex() }
        assertEquals(2, maestro.size)
        assertEquals("7e004b0310151dea71de7d5e2590821ee66654bfab7e", maestro[1])
        assertEquals(heavyBass, repo.eqProfile.first())
    }

    @Test
    @DisplayName("runtime info: CAP-041 frame 782 sets the Case (79 %); CAP-050 frame 1163 (no Case entry) makes it unavailable; b3 = ff never overwrites it")
    fun `the Case follows the runtime-info stream`() = runTest {
        val (repo, transport) = buildRepository()

        transport.emit(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, runtimeInfoWithCase))
        settle()
        assertEquals(BatteryLevel.Known(79, null, false), repo.batteryStatus.value.case)
        assertNull(repo.caseBatteryError.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "6464ff")) // real CAP-059 frame 2776: b3 = ff on every claim
        settle()
        assertEquals(BatteryLevel.Known(79, null, false), repo.batteryStatus.value.case, "a Left/Right update never touches the Case")

        transport.emit(Dlci.MAESTRO, Hdlc.encode(10496, PW_HDLC_CONTROL_UI, runtimeInfoWithoutCase))
        settle()
        assertEquals(BatteryLevel.Unavailable, repo.batteryStatus.value.case, "never carried over (AGENTS.md §5)")
    }

    @Test
    fun `without an announced channel the runtime-info request is not sent and the Case error says why`() = runTest {
        val (repo, transport) = buildRepository(verified = false)
        repo.launchInitialEqRead()
        advanceTimeBy(3_100) // MAESTRO_ANNOUNCE_WAIT_MS for the EQ read …
        runCurrent()
        advanceTimeBy(3_100) // … and for the subscription
        runCurrent()

        assertEquals(0, transport.sent.size)
        assertEquals(BudsError.MaestroChannelUnknown(null), repo.caseBatteryError.first())
    }

    @Test
    @DisplayName("ADR-024: the Notify's Settable-toggles byte is the dock state — 0x00 both in the case, 0xe8 not, anything else unknown")
    fun `dock state follows the Notify byte`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        runCurrent() // start the repository's backgroundScope collectors before the first emit
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.DockState.UNKNOWN, repo.dockState.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("00", "20")) // CAP-059 frame 1520 (buds seated)
        settle()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.DockState.BOTH_IN_CASE, repo.dockState.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "08")) // frame 2782 (buds out)
        settle()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.DockState.NOT_BOTH_IN_CASE, repo.dockState.first())

        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("12", "08")) // an unconfirmed value is not guessed
        settle()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.DockState.UNKNOWN, repo.dockState.first())
    }

    @Test
    fun `a ring is remembered until Stop succeeds, and a failed ring is never remembered`() = runTest {
        val (repo, transport) = buildRepository()
        settle()
        runCurrent() // start the repository's backgroundScope collectors before the first emit
        assertNull(repo.ringingTarget.first())

        repo.ringBud(RingTarget.RIGHT)
        assertEquals(RingTarget.RIGHT, repo.ringingTarget.first())
        repo.stopRinging()
        assertNull(repo.ringingTarget.first())

        transport.sendShouldFail = BudsError.ConnectionLost
        repo.ringBud(RingTarget.LEFT)
        assertNull(repo.ringingTarget.first(), "a ring whose command never left the phone is not claimed")
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
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known(96, false), status.left)
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known(95, false), status.right)
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
        assertEquals(BatteryLevel.Known(100, false), repo.batteryStatus.value.left)

        val second = launch { repo.batteryStatus.first { (it.left as? BatteryLevel.Known)?.isCharging == true } }
        runCurrent()
        // Real maintainer frame (2026-09-19): 0xe4 = 100 % charging, 0xdd = 93 % charging (ADR-033's 2026-09-20 update).
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "e4ddff"))
        second.join()

        assertEquals(BatteryLevel.Known(100, true), repo.batteryStatus.value.left)
        assertEquals(BatteryLevel.Known(93, true), repo.batteryStatus.value.right)
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
        assertEquals(BatteryLevel.Known(100, false), repo.batteryStatus.value.right)
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
        assertNull(repo.ringingTarget.first())
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
    fun `a dock reading within 2 s of the claim is provisional, a later one in the same claim is not (ADR-024, APP-5)`() = runTest {
        val (repo, transport) = buildRepository()
        transport.onSent = null
        val tap = launch { repo.refreshAncMode() }
        runCurrent() // the claim opens at virtual t = 0
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("00", "20")) // the connect-time answer: "both in the case"
        tap.join()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.DockState.BOTH_IN_CASE, repo.dockState.first())
        assertEquals(true, repo.dockStateProvisional.first())

        advanceTimeBy(1_200) // a spontaneous re-Notify 1.2 s later (CAP-047 frame 3996's pattern) — still inside the linger
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "20"))
        settle()
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.DockState.NOT_BOTH_IN_CASE, repo.dockState.first(), "the last value wins")
        assertEquals(true, repo.dockStateProvisional.first())

        advanceTimeBy(1_000) // 2.2 s after the open
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, ancNotify("e8", "20"))
        settle()
        assertEquals(false, repo.dockStateProvisional.first())
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

/** Device Information Model ID of the Pixel Buds Pro 2 (`CAP-059` frame 1049, PROTOCOL.md §0.1). */
private val MODEL_ID_PRO_2 = hex("03010003da2db1")
