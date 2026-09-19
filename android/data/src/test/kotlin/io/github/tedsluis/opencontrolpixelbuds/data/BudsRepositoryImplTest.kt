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

import io.github.tedsluis.opencontrolpixelbuds.data.codec.Dlci
import io.github.tedsluis.opencontrolpixelbuds.domain.AncMode
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionLoss
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine
import io.github.tedsluis.opencontrolpixelbuds.hardware.FakeBudsTransport
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
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

    private fun buildRepository(
        scope: TestScope,
        connectionStateMachine: ConnectionStateMachine = buildConnectionStateMachine(),
        hfpBatteryPercent: MutableSharedFlow<Int> = MutableSharedFlow(extraBufferCapacity = 8),
    ): Triple<BudsRepositoryImpl, FakeBudsTransport, MutableSharedFlow<Int>> {
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
            hfpBatteryPercent = hfpBatteryPercent,
            debugModeEnabled = MutableStateFlow(false),
            scope = scope.backgroundScope,
        )
        return Triple(repo, transport, hfpBatteryPercent)
    }

    @Test
    fun `setAncMode sends the exact wire bytes and optimistically updates ancMode`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

        val result = repo.setAncMode(AncMode.ADAPTIVE)
        assertInstanceOf(BudsResult.Success::class.java, result)

        val sent = transport.sent.single()
        assertEquals(Dlci.FAST_PAIR_MESSAGE_STREAM, sent.first)
        assertEquals("0812001401e8e840" + "00".repeat(16), sent.second.toHex())
        assertEquals(AncMode.ADAPTIVE, repo.ancMode.first())
    }

    @Test
    fun `a real inbound ANC Notify frame updates ancMode`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

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
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

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
        val (repo, _) = buildRepository(this)
        advanceUntilIdle()

        val result = repo.refreshAncMode()
        assertInstanceOf(BudsResult.Failure::class.java, result)
        assertEquals(BudsError.Timeout, (result as BudsResult.Failure).error)
    }

    @Test
    fun `setEqGains sends the exact wire bytes matching CAP-015 frame 2165 and updates eqProfile`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

        val gains = EqBandGains(upperTreble = 0f, treble = 0f, mid = 0f, bass = 3.0f, lowBass = 5.0f)
        val result = repo.setEqGains(gains)
        assertInstanceOf(BudsResult.Success::class.java, result)

        val sent = transport.sent.single()
        assertEquals(Dlci.MAESTRO, sent.first)
        // CAP-015 frame 2165's own wire bytes, correlation byte 0x00 (this repository's default,
        // not this capture's session-specific 0x13 — see BudsRepositoryImpl's own TODO(verify)).
        assertEquals(
            "7e003b0310001dea71de7d5e251d9a8c9e2a1e221c8201190d0000a04015000040401d0000000025000000002d00000000",
            sent.second.toHex().dropLast(10), // drop the CRC + trailing flag, which depend on the byte content above
        )
        assertEquals(gains, repo.eqProfile.first())
    }

    @Test
    fun `EQ profile resets to unknown on every fresh Ready transition`() = runTest {
        // driveToReady = false: a fresh machine starts at Disconnected, which is already
        // "not Ready" — exactly what this test needs before it drives its own Ready transition
        // below and checks that transition specifically causes the reset.
        val connectionStateMachine = buildConnectionStateMachine(driveToReady = false)
        val (repo, transport) = buildRepository(this, connectionStateMachine = connectionStateMachine)
        advanceUntilIdle()

        repo.setEqGains(EqBandGains(1f, 1f, 1f, 1f, 1f))
        assertEquals(EqBandGains(1f, 1f, 1f, 1f, 1f), repo.eqProfile.first())

        val job = launch { repo.eqProfile.first { it == null } }
        runCurrent()
        connectionStateMachine.onConnectRequested()
        connectionStateMachine.onLinkEstablished()
        connectionStateMachine.onReady()
        job.join()

        assertNull(repo.eqProfile.value)
    }

    @Test
    fun `connect fails with PermissionDenied when no bonded device exists yet`() = runTest {
        // Not Ready: connect() on an already-Ready repository is a deliberate no-op success (see the
        // dedicated test below), so this scenario needs a machine that is still Disconnected.
        val (repo, _) = buildRepository(this, connectionStateMachine = buildConnectionStateMachine(driveToReady = false))
        advanceUntilIdle()

        val result = repo.connect()
        assertInstanceOf(BudsResult.Failure::class.java, result)
        assertEquals(BudsError.PermissionDenied, (result as BudsResult.Failure).error)
    }

    @Test
    fun `a transport-reported connection loss moves connectionState to Disconnected`() = runTest {
        val connectionStateMachine = buildConnectionStateMachine()
        val (repo, transport) = buildRepository(this, connectionStateMachine = connectionStateMachine)
        advanceUntilIdle()

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
        val (repo, transport) = buildRepository(this, connectionStateMachine = machine)
        advanceUntilIdle()

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
        val (repo, transport) = buildRepository(this, connectionStateMachine = machine)
        advanceUntilIdle()
        machine.onConnectRequested()

        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, "stale"))

        assertEquals(ConnectionState.Connecting, machine.state.value)
        assertNull(repo.lastConnectionError.first())
    }

    @Test
    fun `a loss report arriving while Failed is ignored`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        val (_, transport) = buildRepository(this, connectionStateMachine = machine)
        advanceUntilIdle()
        machine.onConnectRequested()
        machine.onError(BudsError.ChannelUnavailable(Dlci.MAESTRO, "scripted"))

        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, "stale"))

        assertEquals(ConnectionState.Failed(BudsError.ChannelUnavailable(Dlci.MAESTRO, "scripted")), machine.state.value)
    }

    @Test
    fun `an explicit disconnect clears the last connection error`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()
        deliverLoss(transport, ConnectionLoss(Dlci.MAESTRO, "scripted"))
        assertEquals(BudsError.ChannelLost(Dlci.MAESTRO, "scripted"), repo.lastConnectionError.first())

        repo.disconnect()

        assertNull(repo.lastConnectionError.first())
    }

    @Test
    fun `connect while already Ready is a no-op success and does not tear the live connection down`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

        val result = repo.connect() // bondedDeviceProvider is null in these tests: only the Ready short-circuit can succeed

        assertInstanceOf(BudsResult.Success::class.java, result)
        assertEquals(true, transport.connected)
    }

    @Test
    fun `ringBud and stopRinging send the exact CAP-025 wire bytes`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

        repo.ringBud(RingTarget.LEFT)
        assertEquals("0401000102", transport.sent[0].second.toHex())

        repo.stopRinging()
        assertEquals("0401000100", transport.sent[1].second.toHex())
    }

    // ---- on-demand Message Stream claim (DECISIONS.md ADR-032) ------------------------------------

    @Test
    fun `an ANC tap claims the Message Stream, sends on it, and releases it after the linger`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()
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
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

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
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()
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
        val (repo, transport) = buildRepository(this, connectionStateMachine = machine)
        advanceUntilIdle()
        repo.setAncMode(AncMode.OFF)

        transport.emitChannelClosed(Dlci.FAST_PAIR_MESSAGE_STREAM, "taken by another client")
        advanceUntilIdle()

        assertEquals(ConnectionState.Ready, machine.state.value)
        repo.setAncMode(AncMode.ADAPTIVE)
        assertEquals(2, transport.openChannelCalls.size, "claimed again after it was taken away")
    }

    @Test
    fun `an ANC or Find tap while the session is not Ready fails without touching the Message Stream`() = runTest {
        val machine = buildConnectionStateMachine(driveToReady = false)
        val (repo, transport) = buildRepository(this, connectionStateMachine = machine)
        advanceUntilIdle()

        assertEquals(BudsError.ConnectionLost, (repo.setAncMode(AncMode.OFF) as BudsResult.Failure).error)
        assertEquals(BudsError.ConnectionLost, (repo.ringBud(RingTarget.RIGHT) as BudsResult.Failure).error)
        assertEquals(emptyList<Int>(), transport.openChannelCalls)
    }

    @Test
    fun `the Buds' own Notify wins over the optimistic ANC update`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

        val tap = launch { repo.setAncMode(AncMode.ACTIVE) }
        runCurrent()
        // The Buds answer with their real state (Off, CAP-036 frame 1182) — e.g. after refusing the Set.
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("0813000401e80020"))
        tap.join()

        assertEquals(AncMode.OFF, repo.ancMode.first())
    }

    @Test
    fun `a Find My Buds tap returns as soon as the Buds ACK, not after the full wait`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()
        var finishedAt = -1L

        val tap = launch { repo.ringBud(RingTarget.LEFT); finishedAt = testScheduler.currentTime }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("ff010003" + "040100")) // ACK of a Ring (real frame)
        tap.join()

        assertEquals(0L, finishedAt, "no virtual time may pass: the ACK ended the wait")
    }

    @Test
    fun `an explicit disconnect clears the Message Stream error`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()
        transport.openChannelShouldFail = BudsError.ChannelUnavailable(Dlci.FAST_PAIR_MESSAGE_STREAM, "busy")
        repo.setAncMode(AncMode.OFF)

        repo.disconnect()

        assertNull(repo.messageStreamError.first())
    }

    @Test
    fun `a Battery updated frame fills Left and Right from the Buds' own push, Case stays unavailable`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

        val job = launch { repo.batteryStatus.first { it.left is io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known } }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "605fff")) // 96 % / 95 %
        job.join()

        val status = repo.batteryStatus.value
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known(96, null), status.left)
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known(95, null), status.right)
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Unavailable, status.case)
    }

    @Test
    fun `a charging-regime battery frame replaces a known percentage with unavailable, never a stale value`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()
        val first = launch { repo.batteryStatus.first { it.left is io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known } }
        runCurrent()
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "6464ff"))
        first.join()

        val second = launch { repo.batteryStatus.first { it.left is io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Unavailable } }
        runCurrent()
        // 0xe4 / 0xdd: the charging regime real logs show while the earbuds sit in the case. ADR-033
        // does not interpret it, so it must read as unavailable — not keep showing the old 100 %.
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003" + "e4ddff"))
        second.join()

        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Unavailable, repo.batteryStatus.value.left)
        assertEquals(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Unavailable, repo.batteryStatus.value.right)
    }

    @Test
    fun `HFP battery pushes update batteryStatus hfpEarbud without fabricating a charging state`() = runTest {
        val (repo, _, hfpBattery) = buildRepository(this)
        advanceUntilIdle()

        val job = launch {
            repo.batteryStatus.first {
                it.hfpEarbud is io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known
            }
        }
        runCurrent()
        hfpBattery.emit(88)
        job.join()

        val status = repo.batteryStatus.value
        val level = status.hfpEarbud
        assertInstanceOf(io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known::class.java, level)
        level as io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel.Known
        assertEquals(88, level.percent)
        assertNull(level.isCharging)
    }

    @Test
    fun `a structurally valid but unrecognized frame surfaces as UnidentifiedFrame, not dropped silently`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

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
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
