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
import io.github.tedsluis.opencontrolpixelbuds.data.codec.MaestroChannel
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PW_HDLC_CONTROL_UI
import io.github.tedsluis.opencontrolpixelbuds.data.codec.PwRpc
import io.github.tedsluis.opencontrolpixelbuds.data.codec.RpcPacket
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

/**
 * The bytes of the experiment card (`ai-sessions/0096` round 1, `ai-sessions/0098` round 2) — built by the project's own codecs, checked against real capture
 * frames in `ExperimentFramesTest`. **Only in the `experiment` build type; never in a release.** Round 2 keeps A (for the "mode not ticked for press and
 * hold" test of DECISIONS.md ADR-067's Note) and adds F, the Dosimeter (each send approved in chat 2026-10-17, `ai-sessions/0098` Checkpoint 1: F1 … F4).
 * Round 1's B, C, D and E are gone: B and C are in the release (ADR-069/070), D and E were answered (`CAP-078` §6–§7).
 */
object ExperimentFrames {
    /** `maestro_pw.Dosimeter` = `0x73d5d805` (`scripts/pwrpc_decode.py`'s service table; `CAP-027` 1107). */
    val SERVICE_DOSIMETER: Int = PwRpc.nameHash("maestro_pw.Dosimeter")

    /** `SubscribeToLiveDb` = `0x4d93b6e2`, `FetchDailySummaries` = `0x73b772ce` (`CAP-027` 1107, `CAP-058` 5736). */
    val METHOD_SUBSCRIBE_TO_LIVE_DB: Int = PwRpc.nameHash("SubscribeToLiveDb")
    val METHOD_FETCH_DAILY_SUMMARIES: Int = PwRpc.nameHash("FetchDailySummaries")

    /** A (`ai-sessions/0096` E-01): `WriteSetting 4:{13:n}` — payload `22 02 68 n` (n 1 Off, 2 Noise cancellation, 3 Transparency, 4 Adaptive, `qhs`). */
    fun writeAncState(channelId: Int, mode: Int): RpcPacket {
        require(mode in 1..4) { "setting 13 takes 1…4" }
        return request(channelId, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, byteArrayOf(0x22, 0x02, 0x68, mode.toByte()))
    }

    /** F1: `maestro_pw.Maestro/SetWallclock` with `1:<ms since 1970>` — the official app's request form (`CAP-041` 783: `08 a7 83 9b ba 87 34` = 1788707520935). */
    fun setWallclock(channelId: Int, epochMillis: Long): RpcPacket {
        require(epochMillis > 0) { "a wall-clock time" }
        return request(channelId, Maestro.SERVICE_ID, Maestro.METHOD_SET_WALLCLOCK, byteArrayOf(0x08) + varint(epochMillis))
    }

    /** F2: `maestro_pw.Dosimeter/SubscribeToLiveDb`, empty request (`CAP-027` 1107/1212 channel 19, 1107/1214 channel 21). */
    fun subscribeToLiveDb(channelId: Int): RpcPacket = request(channelId, SERVICE_DOSIMETER, METHOD_SUBSCRIBE_TO_LIVE_DB, ByteArray(0))

    /** F3: `maestro_pw.Dosimeter/FetchDailySummaries`, empty request (`CAP-058` 5736 / `CAP-027` 898, channels 19 and 21). */
    fun fetchDailySummaries(channelId: Int): RpcPacket = request(channelId, SERVICE_DOSIMETER, METHOD_FETCH_DAILY_SUMMARIES, ByteArray(0))

    /** F4: `WriteSetting 4:{21:b}` ("Volume level notifications") — `22 03 a8 01 b`, the channel-21 form of `CAP-058` 5747/5753; channel 19 never captured. */
    fun writeField21(channelId: Int, on: Boolean): RpcPacket =
        request(channelId, Maestro.SERVICE_ID, Maestro.METHOD_WRITE_SETTING, byteArrayOf(0x22, 0x03, 0xa8.toByte(), 0x01, if (on) 1 else 0))

    fun hex(bytes: ByteArray): String = bytes.joinToString(" ") { "%02x".format(it.toInt() and 0xff) }

    /**
     * One HDLC frame from DLCI 0x02 (flags included) → the log line for a Dosimeter packet, or `null` for anything else (another service, a frame that does not
     * decode). `<type> ch=N <method> <status> <fields>` — the fields as [describeFields] prints them.
     */
    fun dosimeterLine(frame: ByteArray): Pair<Int, String>? {
        val hdlc = (Hdlc.decode(frame) as? BudsResult.Success)?.value ?: return null
        val rpc = (PwRpc.decode(hdlc.payload) as? BudsResult.Success)?.value ?: return null
        if (rpc.serviceId != SERVICE_DOSIMETER) return null
        val method = when (rpc.methodId) {
            METHOD_SUBSCRIBE_TO_LIVE_DB -> "SubscribeToLiveDb"
            METHOD_FETCH_DAILY_SUMMARIES -> "FetchDailySummaries"
            else -> "0x%08x".format(rpc.methodId)
        }
        return rpc.methodId to "${PwRpc.typeName(rpc.type)} ch=${rpc.channelId} $method ${PwRpc.statusName(rpc.status)} ${describeFields(rpc.payload)}"
    }

    /**
     * A Dosimeter answer or stream packet as `field:value` pairs — varints as numbers, fixed32 as `f32(<IEEE float, 2 decimals>)` (the form of
     * `scripts/pwrpc_decode.py`, so a log line can be checked against the capture), fixed64 as hex, length-delimited bytes as nested `{…}` when they parse, else
     * hex. Raw evidence: no unit, no meaning. A payload that does not parse is printed as hex.
     */
    fun describeFields(payload: ByteArray, depth: Int = 0): String {
        val out = ArrayList<String>()
        var i = 0
        while (i < payload.size) {
            val (key, afterKey) = readVarint(payload, i) ?: return hex(payload)
            val number = key ushr 3
            if (number == 0L) return hex(payload)
            i = afterKey
            when ((key and 7).toInt()) {
                0 -> { val (v, next) = readVarint(payload, i) ?: return hex(payload); out += "$number:$v"; i = next }
                1 -> { if (i + 8 > payload.size) return hex(payload); out += "$number:x64<${hex(payload.copyOfRange(i, i + 8))}>"; i += 8 }
                5 -> {
                    if (i + 4 > payload.size) return hex(payload)
                    val bits = (payload[i].toInt() and 0xff) or ((payload[i + 1].toInt() and 0xff) shl 8) or
                        ((payload[i + 2].toInt() and 0xff) shl 16) or ((payload[i + 3].toInt() and 0xff) shl 24)
                    out += "$number:f32(${String.format(java.util.Locale.ROOT, "%.2f", java.lang.Float.intBitsToFloat(bits))})"
                    i += 4
                }
                2 -> {
                    val (len, next) = readVarint(payload, i) ?: return hex(payload)
                    if (len < 0 || next + len > payload.size) return hex(payload)
                    val inner = payload.copyOfRange(next, next + len.toInt())
                    val nested = if (depth < 4 && inner.isNotEmpty()) describeFields(inner, depth + 1) else null
                    out += if (nested != null && nested != hex(inner)) "$number:{$nested}" else "$number:<${hex(inner)}>"
                    i = next + len.toInt()
                }
                else -> return hex(payload)
            }
        }
        return out.joinToString(" ")
    }

    private fun readVarint(data: ByteArray, start: Int): Pair<Long, Int>? {
        var result = 0L
        var shift = 0
        var i = start
        while (i < data.size && shift < 64) {
            val b = data[i].toInt() and 0xff
            result = result or ((b and 0x7f).toLong() shl shift)
            i++
            if (b and 0x80 == 0) return result to i
            shift += 7
        }
        return null
    }

    private fun varint(value: Long): ByteArray {
        var v = value
        val out = ArrayList<Byte>(10)
        do {
            var b = (v and 0x7f).toInt()
            v = v ushr 7
            if (v != 0L) b = b or 0x80
            out.add(b.toByte())
        } while (v != 0L)
        return out.toByteArray()
    }

    private fun request(channelId: Int, serviceId: Int, methodId: Int, payload: ByteArray) = RpcPacket(
        type = PwRpc.TYPE_REQUEST,
        channelId = channelId,
        serviceId = serviceId,
        methodId = methodId,
        payload = payload,
    )
}

/**
 * The experiment card's runner behind the Debug tab. One call = one request; nothing runs by itself, nothing is retried. Every send and every answer is logged
 * always-on with its bytes (the experiment's evidence; also in the HCI log). Requests go through the repository's own lock, channel and write quarantine.
 */
class ExperimentRunner(private val repo: BudsRepositoryImpl) {

    private val _lastResult = MutableStateFlow<String?>(null)

    /** The last experiment's one-line outcome, for the card. */
    val lastResult: StateFlow<String?> = _lastResult

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var dosimeterListener: Job? = null

    /** A: one `WriteSetting 4:{13:n}`; waits for its RESPONSE as the app's own writes do (≤ 1.5 s). */
    suspend fun writeAncState(mode: Int): String =
        maestro("A", null, requireWriteGate = true, awaitResult = Maestro.METHOD_WRITE_SETTING) { ch -> ExperimentFrames.writeAncState(ch, mode) }

    /** F1: one `SetWallclock` with the phone's clock now; the value sent is logged. */
    suspend fun setWallclock(): String {
        val now = System.currentTimeMillis()
        BleLogger.logConnectionEvent("Experiment F1: the phone's clock $now ms")
        return maestro("F1", null, requireWriteGate = true, awaitResult = Maestro.METHOD_SET_WALLCLOCK) { ch -> ExperimentFrames.setWallclock(ch, now) }
    }

    /** F2: one `SubscribeToLiveDb` on [channelId] (19 or 21); the stream's packets are logged by the Dosimeter listener with a per-minute count. */
    suspend fun subscribeToLiveDb(channelId: Int): String {
        startDosimeterListener()
        return maestro("F2", channelId, requireWriteGate = false, awaitResult = null) { ch -> ExperimentFrames.subscribeToLiveDb(ch) }
    }

    /** F3: one `FetchDailySummaries` on [channelId]; the answer is logged by the Dosimeter listener. */
    suspend fun fetchDailySummaries(channelId: Int): String {
        startDosimeterListener()
        return maestro("F3", channelId, requireWriteGate = false, awaitResult = null) { ch -> ExperimentFrames.fetchDailySummaries(ch) }
    }

    /** F4: one `WriteSetting 4:{21:b}` on [channelId] (channel 19's form was never captured); waits for its RESPONSE. */
    suspend fun writeField21(channelId: Int, on: Boolean): String =
        maestro("F4", channelId, requireWriteGate = true, awaitResult = Maestro.METHOD_WRITE_SETTING) { ch -> ExperimentFrames.writeField21(ch, on) }

    /**
     * Logs every Dosimeter packet the Buds send on DLCI 0x02 — "Experiment F: <type> ch=N <method> <fields>" — and once a minute how many live-stream packets
     * arrived (ADR-060's 🟡 vi, the cost). Started by the first F2/F3 tap, ended with the process. Reads the transport's inbound bytes; sends nothing.
     */
    private fun startDosimeterListener() {
        if (dosimeterListener?.isActive == true) return
        dosimeterListener = scope.launch {
            val buffer = java.io.ByteArrayOutputStream()
            var minuteStart = System.currentTimeMillis()
            var liveCount = 0
            repo.transport.inbound.collect { (dlci, bytes) ->
                if (dlci != Dlci.MAESTRO) return@collect
                for (b in bytes) {
                    if (b == 0x7e.toByte()) {
                        if (buffer.size() > 0) {
                            val frame = byteArrayOf(0x7e) + buffer.toByteArray() + byteArrayOf(0x7e)
                            buffer.reset()
                            ExperimentFrames.dosimeterLine(frame)?.let { (methodId, line) ->
                                if (methodId == ExperimentFrames.METHOD_SUBSCRIBE_TO_LIVE_DB) liveCount++
                                BleLogger.logConnectionEvent("Experiment F: $line")
                            }
                        }
                    } else {
                        buffer.write(b.toInt())
                    }
                }
                val now = System.currentTimeMillis()
                if (now - minuteStart >= 60_000) {
                    BleLogger.logConnectionEvent("Experiment F: $liveCount live-stream packets in the last ${(now - minuteStart) / 1000} s")
                    liveCount = 0
                    minuteStart = now
                }
            }
        }
    }

    /** One request on the announced channel ([channelId] `null`) or on the named one (19 or 21, its ADR-034 address), through the repository's Maestro lock. */
    private suspend fun maestro(label: String, channelId: Int?, requireWriteGate: Boolean, awaitResult: Int?, build: (Int) -> RpcPacket): String {
        val result: BudsResult<String> = repo.eqMutex.withLock {
            val announced = when (val c = repo.awaitMaestroChannel()) {
                is BudsResult.Failure -> return@withLock BudsResult.Failure(c.error)
                is BudsResult.Success -> c.value
            }
            val channel = if (channelId == null) announced else MaestroChannel.forChannel(channelId)
                ?: return@withLock BudsResult.Failure(BudsError.MaestroChannelUnknown(channelId))
            if (requireWriteGate) repo.writeGate(requireModelIdOfClaim = false)?.let { return@withLock BudsResult.Failure(it) }
            val packet = build(channel.channelId)
            val wire = Hdlc.encode(channel.requestAddress, PW_HDLC_CONTROL_UI, PwRpc.encode(packet))
            repo.awaitWriteQuarantine()
            BleLogger.logConnectionEvent("Experiment $label: sending ${ExperimentFrames.hex(wire)} (channel ${channel.channelId})")
            if (awaitResult == null) {
                when (val sent = repo.transport.send(Dlci.MAESTRO, wire)) {
                    is BudsResult.Failure -> sent
                    is BudsResult.Success -> BudsResult.Success("sent (answers in the log: \"Experiment F: …\" and the hex dump)")
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
