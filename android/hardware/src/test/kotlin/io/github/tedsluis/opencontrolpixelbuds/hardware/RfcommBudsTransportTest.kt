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
package io.github.tedsluis.opencontrolpixelbuds.hardware

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Regression tests for `ai-sessions/0039`'s transport fixes, driven by scripted [RfcommSocket]s —
 * no real Bluetooth stack (AGENTS.md §11). [CollidingStack] models the one Android-stack behavior
 * the round-1 system log proved (`RFCOMM_CreateConnectionWithSecurity: already at opened state`,
 * `ai-sessions/0039` RESULT §3): a second connection to a channel that is still open fails.
 */
class RfcommBudsTransportTest {

    private val maestro = 0x02
    private val messageStream = 0x04
    private val channels = linkedMapOf(maestro to UUID.randomUUID(), messageStream to UUID.randomUUID())

    // ---- scripted socket plumbing -------------------------------------------------------------

    private sealed interface Event {
        class Bytes(val data: ByteArray) : Event
        class Fail(val error: IOException) : Event
        data object Eof : Event
    }

    /** Blocks in `read()` until the test scripts an event; `close()` unblocks a pending read with
     * an IOException, like a real `BluetoothSocket` whose socket is closed under a blocked read. */
    private class ScriptedInput : InputStream() {
        val events = LinkedBlockingQueue<Event>()

        @Volatile
        var closed = false
        override fun read(): Int = throw UnsupportedOperationException()

        override fun read(b: ByteArray): Int {
            if (closed) throw IOException("bt socket closed, read return: -1")
            return when (val event = events.take()) {
                is Event.Bytes -> event.data.copyInto(b).let { event.data.size }
                is Event.Fail -> throw event.error
                Event.Eof -> -1
            }
        }
    }

    private class ScriptedSocket(val channelId: Int) : RfcommSocket {
        val input = ScriptedInput()
        val written = CopyOnWriteArrayList<ByteArray>()

        @Volatile
        var failWrites: IOException? = null

        @Volatile
        var closed = false
        var connectFailure: IOException? = null
        override fun connect() {
            connectFailure?.let { throw it }
        }

        override val inputStream: InputStream get() = input
        override val outputStream: OutputStream = object : OutputStream() {
            override fun write(b: Int) = throw UnsupportedOperationException()
            override fun write(b: ByteArray) {
                failWrites?.let { throw it }
                written += b
            }
        }

        override fun close() {
            closed = true
            input.closed = true
            input.events.offer(Event.Fail(IOException("socket closed")))
        }
    }

    /** Hands out [ScriptedSocket]s and models "one open RFCOMM connection per channel". */
    private class CollidingStack {
        val all = CopyOnWriteArrayList<ScriptedSocket>()
        private val failuresLeft = mutableMapOf<Int, AtomicInteger>()

        fun failNextConnects(channelId: Int, times: Int) {
            failuresLeft[channelId] = AtomicInteger(times)
        }

        fun open(channelId: Int, @Suppress("UNUSED_PARAMETER") uuid: UUID): RfcommSocket {
            val socket = ScriptedSocket(channelId)
            all += socket
            val scripted = failuresLeft[channelId]
            val stillOpenElsewhere = all.any { it !== socket && it.channelId == channelId && !it.closed }
            if (stillOpenElsewhere) {
                socket.connectFailure = IOException("read failed, socket might closed or timeout, read ret: -1 [already at opened state]")
            } else if (scripted != null && scripted.getAndDecrement() > 0) {
                socket.connectFailure = IOException("scripted connect failure on 04:00:6E:CF:6E:07")
            }
            return socket
        }

        fun socketsFor(channelId: Int) = all.filter { it.channelId == channelId }
    }

    private fun transport(
        attempts: Int = 3,
        fastThresholdMs: Long = 60_000,
    ) = RfcommBudsTransport(
        socketFactory = { _, _, _ -> error("connectWith() is driven directly in these tests") },
        connectAttempts = attempts,
        retryDelayMs = 0,
        fastFailureThresholdMs = fastThresholdMs,
    )

    private fun <T> blocking(block: suspend CoroutineScope.() -> T): T = runBlocking { withTimeout(10_000) { block() } }

    private suspend fun awaitUntil(what: String, condition: () -> Boolean) {
        withTimeout(5_000) { while (!condition()) delay(5) }
    }

    /** Collects [SharedFlow] emissions into a list, subscribed before the caller triggers anything. */
    private class Collector<T>(flow: SharedFlow<T>, scope: CoroutineScope) {
        val items = CopyOnWriteArrayList<T>()

        init {
            // UNDISPATCHED: runs on this thread up to the first suspension, i.e. the subscription is
            // registered before the constructor returns — nothing emitted afterwards can be missed.
            scope.launch(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) { flow.collect { items += it } }
        }
    }

    private suspend fun <T> collecting(flow: SharedFlow<T>, block: suspend (Collector<T>) -> Unit) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val collector = Collector(flow, scope)
            block(collector)
        } finally {
            scope.cancel()
        }
    }

    // ---- connect -----------------------------------------------------------------------------

    @Test
    fun `connect opens every channel in order and forwards inbound bytes tagged with the channel`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.inbound) { inbound ->
            assertInstanceOf(BudsResult.Success::class.java, t.connectWith(channels, stack::open))
            assertTrue(t.connected)
            stack.socketsFor(messageStream).single().input.events.offer(Event.Bytes(byteArrayOf(1, 2, 3)))
            awaitUntil("inbound frame") { inbound.items.isNotEmpty() }
            assertEquals(messageStream, inbound.items.single().first)
            assertEquals(listOf<Byte>(1, 2, 3), inbound.items.single().second.toList())
        }
        t.disconnect()
    }

    @Test
    fun `a fast connect failure is retried within the same connect call and then succeeds`() = blocking {
        val stack = CollidingStack()
        stack.failNextConnects(messageStream, times = 2)
        val t = transport(attempts = 3)

        assertInstanceOf(BudsResult.Success::class.java, t.connectWith(channels, stack::open))

        val attempts = stack.socketsFor(messageStream)
        assertEquals(3, attempts.size)
        // Every socket that failed to connect was closed, not leaked (ai-sessions/0039 rule 3).
        assertTrue(attempts.take(2).all { it.closed })
        assertFalse(attempts.last().closed)
        t.disconnect()
    }

    @Test
    fun `an exhausted connect fails with ChannelUnavailable naming the channel and closes everything opened`() = blocking {
        val stack = CollidingStack()
        stack.failNextConnects(messageStream, times = 99)
        val t = transport(attempts = 3)

        val result = t.connectWith(channels, stack::open)

        val error = (result as BudsResult.Failure).error
        assertInstanceOf(BudsError.ChannelUnavailable::class.java, error)
        assertEquals(messageStream, (error as BudsError.ChannelUnavailable).channelId)
        assertFalse(t.connected)
        assertTrue(stack.all.all { it.closed }, "no socket may survive a failed connect")
        // The detail carries the exception text, with the Bluetooth address redacted (AGENTS.md §9).
        assertTrue(error.detail!!.contains("scripted connect failure"))
        assertFalse(error.detail!!.contains("04:00:6E:CF:6E:07"))
        assertEquals(3, stack.socketsFor(messageStream).size)
    }

    @Test
    fun `a slow connect failure is not retried`() = blocking {
        val stack = CollidingStack()
        stack.failNextConnects(maestro, times = 99)
        // A negative threshold classifies every failure as "slow" (peer unreachable, not a collision).
        val t = transport(attempts = 3, fastThresholdMs = -1)

        val result = t.connectWith(channels, stack::open)

        assertInstanceOf(BudsError.ChannelUnavailable::class.java, (result as BudsResult.Failure).error)
        assertEquals(1, stack.socketsFor(maestro).size)
    }

    // ---- loss handling -----------------------------------------------------------------------

    @Test
    fun `one channel dying closes every channel and reports exactly one loss`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.connectionLost) { losses ->
            t.connectWith(channels, stack::open)

            // Both readers notice — the real-world signature of one link loss (two "Disconnected"
            // lines 9 ms apart in the round-2 debug log).
            stack.socketsFor(messageStream).single().input.events.offer(Event.Fail(IOException("bt socket closed, read return: -1")))
            stack.socketsFor(maestro).single().input.events.offer(Event.Fail(IOException("bt socket closed, read return: -1")))
            awaitUntil("first loss") { losses.items.isNotEmpty() }
            delay(150)

            assertEquals(1, losses.items.size)
            assertTrue(stack.all.all { it.closed }, "the surviving channel's socket must not be left open as a zombie")
            assertFalse(t.connected)
        }
    }

    @Test
    fun `end of stream counts as a loss`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.connectionLost) { losses ->
            t.connectWith(channels, stack::open)
            stack.socketsFor(maestro).single().input.events.offer(Event.Eof)
            awaitUntil("loss") { losses.items.isNotEmpty() }
            assertEquals(maestro, losses.items.single().channelId)
            assertEquals("stream closed (EOF)", losses.items.single().detail)
        }
    }

    @Test
    fun `an explicit disconnect never reports a loss and closes all sockets`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.connectionLost) { losses ->
            t.connectWith(channels, stack::open)
            t.disconnect()
            delay(200)
            assertTrue(losses.items.isEmpty())
            assertTrue(stack.all.all { it.closed })
            assertFalse(t.connected)
        }
    }

    @Test
    fun `a loss from a connection already replaced by a newer connect is never reported`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.connectionLost) { losses ->
            t.connectWith(channels, stack::open)
            val firstGeneration = stack.all.toList()
            // Reconnect over the top (no disconnect() in between) — the old connection is replaced.
            assertInstanceOf(BudsResult.Success::class.java, t.connectWith(channels, stack::open))

            // A late error from the old generation's stream must not surface. (In practice the old
            // readers are already cancelled by the replacement, so this is guarded twice: by
            // isActive and, for the narrow race where a reader is already past that check, by
            // reportLoss()'s `current === connection` test — the latter cannot be forced from a
            // black-box test, so this asserts the observable outcome, not which guard fired.)
            firstGeneration.first().input.events.offer(Event.Fail(IOException("late error from old connection")))
            delay(200)

            assertTrue(losses.items.isEmpty())
            assertTrue(t.connected, "the new connection must be untouched by the stale event")
        }
        t.disconnect()
    }

    @Test
    fun `reconnecting after a loss succeeds because no zombie socket collides with the new one`() = blocking {
        // The failure this reproduces: after a loss the surviving channel's socket stayed open, so the
        // next connect hit "already at opened state" against *our own* leftover (round-1 log,
        // 16:33:56.004, slot 38 app_uid 10338) — and the failed attempt then killed the incumbent.
        val stack = CollidingStack()
        val t = transport(attempts = 1) // no retry: the first reconnect attempt itself must succeed.
        collecting(t.connectionLost) { losses ->
            t.connectWith(channels, stack::open)
            stack.socketsFor(messageStream).single().input.events.offer(Event.Fail(IOException("port closed by stack")))
            awaitUntil("loss") { losses.items.isNotEmpty() }

            val result = t.connectWith(channels, stack::open)

            assertInstanceOf(BudsResult.Success::class.java, result)
            assertTrue(t.connected)
        }
        t.disconnect()
    }

    @Test
    fun `a failed write reports the loss and returns ChannelLost with the reason`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.connectionLost) { losses ->
            t.connectWith(channels, stack::open)
            stack.socketsFor(messageStream).single().failWrites = IOException("Broken pipe")

            val result = t.send(messageStream, byteArrayOf(0x08, 0x11, 0x00, 0x00))

            val error = (result as BudsResult.Failure).error
            assertEquals(BudsError.ChannelLost(messageStream, "IOException: Broken pipe"), error)
            awaitUntil("loss") { losses.items.isNotEmpty() }
            assertFalse(t.connected)
        }
    }

    @Test
    fun `send with no open connection fails with ConnectionLost`() = blocking {
        val result = transport().send(messageStream, byteArrayOf(1))
        assertEquals(BudsError.ConnectionLost, (result as BudsResult.Failure).error)
    }

    // ---- on-demand channels (DECISIONS.md ADR-032) -------------------------------------------

    private val sessionOnly = linkedMapOf(maestro to UUID.randomUUID())
    private val messageStreamUuid = UUID.randomUUID()

    @Test
    fun `an on-demand channel opens on the current connection and delivers inbound bytes`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.inbound) { inbound ->
            t.connectWith(sessionOnly, stack::open)
            assertFalse(t.isChannelOpen(messageStream))

            assertInstanceOf(BudsResult.Success::class.java, t.openChannel(messageStream, messageStreamUuid))

            assertTrue(t.isChannelOpen(messageStream))
            stack.socketsFor(messageStream).single().input.events.offer(Event.Bytes(byteArrayOf(9)))
            awaitUntil("inbound") { inbound.items.isNotEmpty() }
            assertEquals(messageStream, inbound.items.single().first)
        }
        t.disconnect()
    }

    @Test
    fun `opening an already open on-demand channel is a no-op success`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        t.connectWith(sessionOnly, stack::open)
        t.openChannel(messageStream, messageStreamUuid)

        assertInstanceOf(BudsResult.Success::class.java, t.openChannel(messageStream, messageStreamUuid))

        assertEquals(1, stack.socketsFor(messageStream).size)
        t.disconnect()
    }

    @Test
    fun `a deliberate closeChannel closes only that socket and reports nothing`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.channelClosed) { closed ->
            t.connectWith(sessionOnly, stack::open)
            t.openChannel(messageStream, messageStreamUuid)

            t.closeChannel(messageStream)
            delay(200)

            assertTrue(closed.items.isEmpty(), "a deliberate release must never look like a loss")
            assertTrue(stack.socketsFor(messageStream).single().closed)
            assertFalse(t.isChannelOpen(messageStream))
            assertTrue(t.connected, "the session channel must be untouched")
            assertFalse(stack.socketsFor(maestro).single().closed)
        }
        t.disconnect()
    }

    @Test
    fun `an on-demand channel dying is reported on channelClosed and is not a connection loss`() = blocking {
        // The 0039/0040 evidence: Google Play services' failed connect closes our Message Stream port
        // while our MAESTRO port stays open.
        val stack = CollidingStack()
        val t = transport()
        collecting(t.channelClosed) { closed ->
            collecting(t.connectionLost) { losses ->
                t.connectWith(sessionOnly, stack::open)
                t.openChannel(messageStream, messageStreamUuid)

                stack.socketsFor(messageStream).single().input.events.offer(Event.Fail(IOException("bt socket closed, read return: -1")))
                awaitUntil("channel closed") { closed.items.isNotEmpty() }
                delay(150)

                assertEquals(1, closed.items.size)
                assertEquals(messageStream, closed.items.single().channelId)
                assertTrue(losses.items.isEmpty(), "an on-demand channel's loss is not a connection loss")
                assertTrue(t.connected)
                assertFalse(stack.socketsFor(maestro).single().closed)
                assertFalse(t.isChannelOpen(messageStream))
            }
        }
        t.disconnect()
    }

    @Test
    fun `an on-demand channel can be claimed again right after it was taken away`() = blocking {
        // No zombie: the dead on-demand socket is closed, so a re-claim does not collide with our own leftover.
        val stack = CollidingStack()
        val t = transport(attempts = 1)
        collecting(t.channelClosed) { closed ->
            t.connectWith(sessionOnly, stack::open)
            t.openChannel(messageStream, messageStreamUuid)
            stack.socketsFor(messageStream).single().input.events.offer(Event.Fail(IOException("port closed by another client")))
            awaitUntil("channel closed") { closed.items.isNotEmpty() }

            assertInstanceOf(BudsResult.Success::class.java, t.openChannel(messageStream, messageStreamUuid))
            assertTrue(t.isChannelOpen(messageStream))
        }
        t.disconnect()
    }

    @Test
    fun `losing the session channel also closes any on-demand channel and reports one connection loss`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.connectionLost) { losses ->
            t.connectWith(sessionOnly, stack::open)
            t.openChannel(messageStream, messageStreamUuid)

            stack.socketsFor(maestro).single().input.events.offer(Event.Fail(IOException("peer DISC")))
            awaitUntil("loss") { losses.items.isNotEmpty() }
            delay(100)

            assertEquals(1, losses.items.size)
            assertEquals(maestro, losses.items.single().channelId)
            assertTrue(stack.all.all { it.closed })
            assertFalse(t.isChannelOpen(messageStream))
        }
    }

    @Test
    fun `an on-demand open that stays busy fails with ChannelUnavailable and leaves the session alone`() = blocking {
        val stack = CollidingStack()
        val t = transport(attempts = 2)
        t.connectWith(sessionOnly, stack::open)
        stack.failNextConnects(messageStream, times = 99)

        val result = t.openChannel(messageStream, messageStreamUuid)

        val error = (result as BudsResult.Failure).error
        assertEquals(messageStream, (error as BudsError.ChannelUnavailable).channelId)
        assertTrue(t.connected)
        assertFalse(t.isChannelOpen(messageStream))
        assertTrue(stack.socketsFor(messageStream).all { it.closed })
        t.disconnect()
    }

    @Test
    fun `openChannel with no connection fails with ConnectionLost`() = blocking {
        val result = transport().openChannel(messageStream, messageStreamUuid)
        assertEquals(BudsError.ConnectionLost, (result as BudsResult.Failure).error)
    }

    @Test
    fun `a failed write on an on-demand channel closes only that channel`() = blocking {
        val stack = CollidingStack()
        val t = transport()
        collecting(t.channelClosed) { closed ->
            t.connectWith(sessionOnly, stack::open)
            t.openChannel(messageStream, messageStreamUuid)
            stack.socketsFor(messageStream).single().failWrites = IOException("Broken pipe")

            val result = t.send(messageStream, byteArrayOf(0x08, 0x11, 0x00, 0x00))

            assertEquals(BudsError.ChannelLost(messageStream, "IOException: Broken pipe"), (result as BudsResult.Failure).error)
            awaitUntil("closed") { closed.items.isNotEmpty() }
            assertTrue(t.connected)
        }
        t.disconnect()
    }
}
