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
import io.github.tedsluis.opencontrolpixelbuds.hardware.ConnectionStateMachine
import io.github.tedsluis.opencontrolpixelbuds.hardware.FakeBudsTransport
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
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
        val (repo, _) = buildRepository(this)
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

    @Test
    fun `ringBud and stopRinging send the exact CAP-025 wire bytes`() = runTest {
        val (repo, transport) = buildRepository(this)
        advanceUntilIdle()

        repo.ringBud(RingTarget.LEFT)
        assertEquals("0401000102", transport.sent[0].second.toHex())

        repo.stopRinging()
        assertEquals("0401000100", transport.sent[1].second.toHex())
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
        // Group 0x03 Code 0x03 ("Battery updated", ADR-031) — FACT-identified but gated
        // (ARCHITECTURE.md §5a), so this repository must not decode it as anything.
        transport.emit(Dlci.FAST_PAIR_MESSAGE_STREAM, hex("03030003") + byteArrayOf(1, 2, 3))
        job.join()

        assertEquals(0x03, frame?.group)
        assertEquals(0x03, frame?.code)
    }
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
