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

import io.github.tedsluis.opencontrolpixelbuds.data.BudsRepositoryImpl
import io.github.tedsluis.opencontrolpixelbuds.data.MaestroReply
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Dlci
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Hdlc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.Maestro
import io.github.tedsluis.opencontrolpixelbuds.data.codec.MessageStreamReply
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RingMessageStream
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RpcPacket
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.RingTarget
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The bytes of round 1's five experiments (`ai-sessions/0096`, each approved by the maintainer in chat 2026-10-16, Checkpoint 1) — built by the project's own
 * codecs, checked against `CAP-025` and `CAP-077` frames in `ExperimentFramesTest`. **Only in the `experiment` build type; never in a release.**
 */
object ExperimentFrames {
    /** `h65599("SubscribeToOobeActions")` = `0x57f3becb` (`DESKRESEARCH_FINDINGS.md` 2026-10-14; the name is the APK's literal, `REVERSE_ENGINEERING.md`). */
    val METHOD_SUBSCRIBE_TO_OOBE_ACTIONS: Int = PwRpc.nameHash("SubscribeToOobeActions")

    /** E-01: `WriteSetting 4:{13:n}` — payload `22 02 68 n` (field 4 → field 13 = n; n 1 Off, 2 Noise cancellation, 3 Transparency, 4 Adaptive, `qhs`). */
    fun writeAncState(channelId: Int, mode: Int): RpcPacket {
        require(mode in 1..4) { "setting 13 takes 1…4" }
        return request(channelId, Maestro.METHOD_WRITE_SETTING, byteArrayOf(0x22, 0x02, 0x68, mode.toByte()))
    }

    /** E-03: `ReadSetting 4:N` for N = 8, 9, 10 only (`20 N`). */
    fun readSetting(channelId: Int, field: Int): RpcPacket {
        require(field in setOf(8, 9, 10)) { "the experiment reads only 8, 9, 10" }
        return request(channelId, Maestro.METHOD_READ_SETTING, byteArrayOf(0x20, field.toByte()))
    }

    /** E-12: `SubscribeToOobeActions`, empty request (`nia`). Settings 3 and 6 are never written (ARCHITECTURE.md §5a). */
    fun subscribeToOobeActions(channelId: Int): RpcPacket = request(channelId, METHOD_SUBSCRIBE_TO_OOBE_ACTIONS, ByteArray(0))

    /** E-05: Ring both — `04 01 00 01 03` (Device Action spec "0x03 … Ring both left and right"). */
    val RING_BOTH: ByteArray = byteArrayOf(0x04, 0x01, 0x00, 0x01, 0x03)

    /** E-04 B2: the Seeker's ACK of the Buds' own ring status — `ff 01 00 02 04 01`, the official app's `CAP-025` frame 2048. */
    val RING_STATUS_ACK: ByteArray = byteArrayOf(0xff.toByte(), 0x01, 0x00, 0x02, 0x04, 0x01)

    /** A Ring Start for one bud, as the release app sends it (`04 01 00 01 01` Right / `02` Left — `CAP-025` 2040 / 2131). */
    fun ringStart(target: RingTarget): ByteArray = byteArrayOf(0x04, 0x01, 0x00, 0x01, target.wireValue.toByte())

    /** The Message Stream messages in one inbound RFCOMM payload: `[Group][Code][Length:2 BE][Value]`, several per frame possible; a cut tail is ignored. */
    fun messages(payload: ByteArray): List<Triple<Int, Int, ByteArray>> {
        val out = mutableListOf<Triple<Int, Int, ByteArray>>()
        var i = 0
        while (i + 4 <= payload.size) {
            val length = ((payload[i + 2].toInt() and 0xff) shl 8) or (payload[i + 3].toInt() and 0xff)
            if (i + 4 + length > payload.size) break
            out += Triple(payload[i].toInt() and 0xff, payload[i + 1].toInt() and 0xff, payload.copyOfRange(i + 4, i + 4 + length))
            i += 4 + length
        }
        return out
    }

    fun hex(bytes: ByteArray): String = bytes.joinToString(" ") { "%02x".format(it.toInt() and 0xff) }

    private fun request(channelId: Int, methodId: Int, payload: ByteArray) = RpcPacket(
        type = PwRpc.TYPE_REQUEST,
        channelId = channelId,
        serviceId = Maestro.SERVICE_ID,
        methodId = methodId,
        payload = payload,
    )
}

/**
 * Round 1's experiment card behind the Debug tab (`ai-sessions/0096`). One call = one request; nothing runs by itself, nothing is retried. Every send and
 * every answer is logged always-on with the bytes it sent (these are the experiment's evidence; the Buds' answers are in the Debug-mode hex dump and the HCI
 * log). Maestro requests go through the repository's own lock, channel and write quarantine; Message Stream commands through its claim and Safe-Mode gate.
 */
class ExperimentRunner(private val repo: BudsRepositoryImpl) {

    private val _lastResult = MutableStateFlow<String?>(null)

    /** The last experiment's one-line outcome, for the card. */
    val lastResult: StateFlow<String?> = _lastResult

    /** A: one `WriteSetting 4:{13:n}`; waits for its RESPONSE as the app's own writes do (≤ 1.5 s). */
    suspend fun writeAncState(mode: Int): String =
        maestro("A", requireWriteGate = true, awaitResult = Maestro.METHOD_WRITE_SETTING) { ch -> ExperimentFrames.writeAncState(ch, mode) }

    /** E: one `ReadSetting 4:N`, N ∈ {8, 9, 10} — sent only; the answer (a value or an error status) is in the hex log and the HCI log. */
    suspend fun readSetting(field: Int): String =
        maestro("E", requireWriteGate = false, awaitResult = null) { ch -> ExperimentFrames.readSetting(ch, field) }

    /** D: one `SubscribeToOobeActions` (the stream's packets, if any, show in the Debug-mode hex dump and the HCI log). */
    suspend fun subscribeToOobeActions(): String =
        maestro("D", requireWriteGate = false, awaitResult = null) { ch -> ExperimentFrames.subscribeToOobeActions(ch) }

    /** C: Ring both on one claim; the ACK or NAK. Stop with the app's own Stop (a separate claim). */
    suspend fun ringBoth(): String = report("C", repo.withMessageStream { messageStreamCommand("C", ExperimentFrames.RING_BOTH) })

    /**
     * B: Ring one bud and **hold the claim** until the Buds report ring status `00` (stopped on the bud), [holdMs] passes, or the caller is cancelled
     * (the card's Stop) — then the usual release. With [ack] (B2) every Buds status message `04 01 00 01 xx` is answered with [ExperimentFrames.RING_STATUS_ACK].
     */
    suspend fun ringAndHold(target: RingTarget, ack: Boolean, holdMs: Long = 30_000L): String {
        val label = if (ack) "B2" else "B1"
        val result = repo.withMessageStream {
            val started = messageStreamCommand(label, ExperimentFrames.ringStart(target))
            if (started is BudsResult.Failure) return@withMessageStream started
            val stopped = withTimeoutOrNull(holdMs) {
                repo.transport.inbound
                    .filter { (dlci, _) -> dlci == Dlci.FAST_PAIR_MESSAGE_STREAM }
                    .transform { (_, payload) -> ExperimentFrames.messages(payload).forEach { emit(it) } }
                    .filter { (group, code, value) -> group == RingMessageStream.GROUP && code == RingMessageStream.CODE_RING && value.size == 1 }
                    .map { (_, _, value) -> value[0].toInt() and 0xff }
                    .first { status ->
                        BleLogger.logConnectionEvent("Experiment $label: the Buds' ring status 0x%02x".format(status))
                        if (ack) {
                            val sent = repo.transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, ExperimentFrames.RING_STATUS_ACK)
                            BleLogger.logConnectionEvent("Experiment $label: ACK ${ExperimentFrames.hex(ExperimentFrames.RING_STATUS_ACK)} ${if (sent is BudsResult.Success) "sent" else "not sent"}")
                        }
                        status == RingMessageStream.VALUE_STOP
                    }
            }
            BleLogger.logConnectionEvent(
                if (stopped != null) "Experiment $label: the Buds reported the ring stopped — claim released" else "Experiment $label: no stop report within ${holdMs / 1000} s — claim released",
            )
            BudsResult.Success(if (stopped != null) "the Buds reported 00 (stopped)" else "no stop report in ${holdMs / 1000} s")
        }
        return report(label, result)
    }

    private suspend fun messageStreamCommand(label: String, bytes: ByteArray): BudsResult<String> {
        repo.writeGate(requireModelIdOfClaim = true)?.let { return BudsResult.Failure(it) }
        BleLogger.logConnectionEvent("Experiment $label: sending ${ExperimentFrames.hex(bytes)} on DLCI 0x04")
        val (sent, reply) = repo.sendAndAwait(repo._replies, BudsRepositoryImpl.ACK_WAIT_MS, {
            it.answers(RingMessageStream.GROUP, RingMessageStream.CODE_RING)
        }) { repo.transport.send(Dlci.FAST_PAIR_MESSAGE_STREAM, bytes) }
        return when {
            sent is BudsResult.Failure -> sent
            reply == null -> BudsResult.Failure(BudsError.Timeout)
            reply is MessageStreamReply.Nak -> BudsResult.Success("NAK reason 0x%02x".format(reply.reason))
            else -> BudsResult.Success("ACK")
        }
    }

    private suspend fun maestro(label: String, requireWriteGate: Boolean, awaitResult: Int?, build: (Int) -> RpcPacket): String {
        val result: BudsResult<String> = repo.eqMutex.withLock {
            val channel = when (val c = repo.awaitMaestroChannel()) {
                is BudsResult.Failure -> return@withLock BudsResult.Failure(c.error)
                is BudsResult.Success -> c.value
            }
            if (requireWriteGate) repo.writeGate(requireModelIdOfClaim = false)?.let { return@withLock BudsResult.Failure(it) }
            val packet = build(channel.channelId)
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(packet))
            repo.awaitWriteQuarantine()
            BleLogger.logConnectionEvent("Experiment $label: sending ${ExperimentFrames.hex(wire)} (channel ${channel.channelId})")
            if (awaitResult == null) {
                when (val sent = repo.transport.send(Dlci.MAESTRO, wire)) {
                    is BudsResult.Failure -> sent
                    is BudsResult.Success -> BudsResult.Success("sent (answers, if any, in the hex log)")
                }
            } else {
                val (sent, reply) = repo.sendAndAwait(repo._maestroReplies, BudsRepositoryImpl.EQ_WRITE_ACK_TIMEOUT_MS, { reply ->
                    reply is MaestroReply.Result && reply.result.methodId == awaitResult
                }) { repo.transport.send(Dlci.MAESTRO, wire) }
                when {
                    sent is BudsResult.Failure -> sent
                    reply == null -> {
                        repo.startWriteQuarantine()
                        BudsResult.Failure(BudsError.Timeout)
                    }
                    else -> {
                        val r = (reply as MaestroReply.Result).result
                        BudsResult.Success("${PwRpc.typeName(r.type)} ${PwRpc.statusName(r.status)}")
                    }
                }
            }
        }
        return report(label, result)
    }

    private fun report(label: String, result: BudsResult<String>): String {
        val line = when (result) {
            is BudsResult.Success -> "Experiment $label: ${result.value}"
            is BudsResult.Failure -> "Experiment $label failed: ${result.error::class.simpleName}"
        }
        BleLogger.logConnectionEvent(line)
        _lastResult.value = line
        return line
    }
}
