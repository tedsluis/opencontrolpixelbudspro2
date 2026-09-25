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

import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame

/** DLCI numbers this app implements against (ARCHITECTURE.md §5a). */
object Dlci {
    const val MAESTRO: Int = 0x02
    const val FAST_PAIR_MESSAGE_STREAM: Int = 0x04

    /** "GSND CONTROL" (PROTOCOL.md §2.3): only the Case battery push is decoded (DECISIONS.md ADR-035). */
    const val GSND_CONTROL: Int = 0x08
}

/** One fully-decoded, recognized inbound frame, tagged by which feature it belongs to. */
sealed class RoutedFrame {
    data class Anc(val frame: AncFrame) : RoutedFrame()

    /** An ACK/NAK on the Message Stream, for whichever command it echoes (0044 finding APP-3). */
    data class Reply(val reply: MessageStreamReply) : RoutedFrame()

    /** The Model ID the Buds send on every Message Stream open (Safe-Mode gate, DECISIONS.md ADR-042). */
    data class ModelId(val frame: ModelIdFrame) : RoutedFrame()
    data class Eq(val frame: EqFrame) : RoutedFrame()
    data class Ring(val frame: RingFrame) : RoutedFrame()
    data class Battery(val frame: BatteryFrame) : RoutedFrame()

    /** The Case reading of a DLCI 0x08 `0e 01` push (ADR-035); Left/Right stay from [Battery]. */
    data class CaseBattery(val frame: CaseBatteryFrame) : RoutedFrame()

    /** The Case reading of a DLCI 0x02 `SubscribeRuntimeInfo` stream packet (DECISIONS.md ADR-043) — the app's Case source. */
    data class RuntimeInfoCase(val case: BatteryLevel) : RoutedFrame()

    /**
     * The Buds' unsolicited `GetSoftwareInfo` push announcing the pw_rpc channel of this connection (ADR-034); [firmware] = the
     * distinct firmware strings it carries (`ai-sessions/0042`), empty if the payload had another shape.
     */
    data class MaestroHello(val channelId: Int, val firmware: List<String> = emptyList()) : RoutedFrame()

    /**
     * The Buds' answer to one of *our* Maestro requests that carries no EQ value: the empty `RESPONSE` to a
     * `WriteSetting`, or an error (`status != OK`, or a `CLIENT_ERROR`/`SERVER_ERROR` packet) for a read or write.
     * [status] is `null` when the field is absent — OK on the wire (`CAP-015` frame 2117).
     */
    data class RpcResult(val channelId: Int, val methodId: Int, val type: Int, val status: Int?) : RoutedFrame() {
        val isOk: Boolean get() = type == PwRpc.TYPE_RESPONSE && (status == null || status == 0)
    }
}

/**
 * Per-DLCI byte-stream buffering and frame-boundary detection (ARCHITECTURE.md
 * §2.1/§5) — `BudsTransport.inbound` emits raw, not-necessarily-frame-aligned
 * byte chunks as they arrive from the socket (RFCOMM is a byte stream, not
 * message-delimited at the socket level); this router is what turns those
 * chunks into actual decoded frames, one call to [feed] per inbound
 * `(channelId, bytes)` pair.
 *
 * DLCI 0x02 frames are HDLC flag-delimited (PROTOCOL.md §2.2a); DLCI 0x04
 * frames are length-prefixed (`[Group][Code][Len:2BE][Data]`, PROTOCOL.md
 * §2.1) — each channel therefore needs its own boundary-detection strategy,
 * matching ARCHITECTURE.md §5's "each per-DLCI codec is implemented
 * independently" rule. DLCI 0x02/0x04/0x08 are handled — any other channelId
 * is out of scope (ARCHITECTURE.md §5a) and is ignored rather than guessed at.
 *
 * **Resynchronisation (0044 finding APP-2):** each channel's splitter is [reset] whenever that channel is opened, closed or
 * lost, and every splitter drops its buffer when a frame would exceed its maximum size — so one frame cut off by a contended,
 * torn-down on-demand channel (ADR-032/038) can never misalign the frames of a later claim. [feed] and [reset] are synchronised:
 * the repository calls them from different coroutines.
 */
class CodecRouter {
    private val maestroSplitter = HdlcFrameSplitter()
    private val messageStreamSplitter = MessageStreamFrameSplitter()
    private val gsndSplitter = MessageStreamFrameSplitter() // DLCI 0x08 uses the same [Group][Code][Len:2BE][Value] framing (§2.3)

    /** Drops any partial frame buffered for [channelId] (a fresh open, a deliberate close, or a lost channel). */
    @Synchronized
    fun reset(channelId: Int) {
        when (channelId) {
            Dlci.MAESTRO -> maestroSplitter.reset()
            Dlci.FAST_PAIR_MESSAGE_STREAM -> messageStreamSplitter.reset()
            Dlci.GSND_CONTROL -> gsndSplitter.reset()
        }
    }

    /** [reset] for every channel — a new connection starts with empty buffers. */
    @Synchronized
    fun resetAll() {
        maestroSplitter.reset()
        messageStreamSplitter.reset()
        gsndSplitter.reset()
    }

    /**
     * Feeds one inbound `(channelId, bytes)` chunk and returns every
     * [RoutedFrame] that could be fully decoded from it (zero, one, or more —
     * a single socket read can complete several small frames, or none if the
     * chunk only completes part of one). A structurally valid but
     * unrecognized frame (e.g. a DLCI 0x02 payload for a still-gated §4.5
     * setting) is reported as an [UnidentifiedFrame] via [onUnidentified]
     * rather than silently dropped (AGENTS.md §6). A frame that fails to
     * parse structurally at all is logged by the caller via the returned
     * [BudsResult.Failure]s in [onMalformed] and dropped — never surfaced as
     * a crash (ARCHITECTURE.md §5).
     */
    @Synchronized
    fun feed(
        channelId: Int,
        bytes: ByteArray,
        timestampMillis: Long,
        onUnidentified: (UnidentifiedFrame) -> Unit = {},
        onMalformed: (ByteArray) -> Unit = {},
        onRpcPacket: (RpcPacket) -> Unit = {},
    ): List<RoutedFrame> {
        val routed = mutableListOf<RoutedFrame>()

        when (channelId) {
            Dlci.MAESTRO -> for (frame in maestroSplitter.feed(bytes, onMalformed)) {
                when (val decoded = Hdlc.decode(frame)) {
                    is BudsResult.Failure -> onMalformed(frame)
                    is BudsResult.Success -> {
                        val payload = decoded.value.payload
                        val unidentified = {
                            onUnidentified(
                                UnidentifiedFrame(
                                    channelId = channelId,
                                    group = null,
                                    code = null,
                                    raw = payload,
                                    timestampMillis = timestampMillis,
                                ),
                            )
                        }
                        when (val rpc = PwRpc.decode(payload)) {
                            is BudsResult.Failure -> unidentified()
                            is BudsResult.Success -> {
                                onRpcPacket(rpc.value)
                                routeMaestro(rpc.value)?.let { routed += it } ?: unidentified()
                            }
                        }
                    }
                }
            }

            Dlci.FAST_PAIR_MESSAGE_STREAM -> for (frame in messageStreamSplitter.feed(bytes, onMalformed)) {
                val group = frame.getOrNull(0)?.toInt()?.and(0xFF)
                val code = frame.getOrNull(1)?.toInt()?.and(0xFF)

                val replyResult = MessageStreamReplyDecoder.decode(frame)
                if (replyResult is BudsResult.Success) {
                    routed += RoutedFrame.Reply(replyResult.value)
                    continue
                }
                val modelIdResult = ModelIdFrameDecoder.decode(frame)
                if (modelIdResult is BudsResult.Success) {
                    routed += RoutedFrame.ModelId(modelIdResult.value)
                    continue
                }
                val ancResult = AncFrameDecoder.decode(frame)
                if (ancResult is BudsResult.Success) {
                    routed += RoutedFrame.Anc(ancResult.value)
                    continue
                }
                val ringResult = RingFrameDecoder.decode(frame)
                if (ringResult is BudsResult.Success) {
                    routed += RoutedFrame.Ring(ringResult.value)
                    continue
                }
                val batteryResult = BatteryFrameDecoder.decode(frame)
                if (batteryResult is BudsResult.Success) {
                    routed += RoutedFrame.Battery(batteryResult.value)
                    continue
                }
                // Structurally short/malformed frames never reach here distinctly from
                // "recognized by neither decoder" — both decoders already validate the
                // shared [Group][Code][Len:2BE][Data] header, so a length mismatch fails
                // both; only a well-formed-but-unrecognized Group/Code reaches this branch
                // for a frame with a plausible header, which is exactly UnidentifiedFrame's
                // purpose (ARCHITECTURE.md §7). A frame too short to even have a header is
                // reported as malformed instead.
                if (frame.size < 4) {
                    onMalformed(frame)
                } else {
                    onUnidentified(
                        UnidentifiedFrame(
                            channelId = channelId,
                            group = group,
                            code = code,
                            raw = frame,
                            timestampMillis = timestampMillis,
                        ),
                    )
                }
            }

            Dlci.GSND_CONTROL -> for (frame in gsndSplitter.feed(bytes, onMalformed)) {
                val decoded = CaseBatteryFrameDecoder.decode(frame)
                if (decoded is BudsResult.Success) {
                    routed += RoutedFrame.CaseBattery(decoded.value)
                } else if (frame.size < 4) {
                    onMalformed(frame)
                } else {
                    // Every other Group/Code on DLCI 0x08 stays unidentified (ADR-035: only the Case battery is understood).
                    onUnidentified(
                        UnidentifiedFrame(
                            channelId = channelId,
                            group = frame[0].toInt() and 0xFF,
                            code = frame[1].toInt() and 0xFF,
                            raw = frame,
                            timestampMillis = timestampMillis,
                        ),
                    )
                }
            }

            else -> Unit // Out of this session's implementation scope (ARCHITECTURE.md §5a) — ignored.
        }

        return routed
    }
}

/**
 * Classifies one Maestro pw_rpc packet (ADR-034), or returns null when it is not something this app understands
 * (other services, other methods, other settings) — the caller then reports it as an `UnidentifiedFrame`.
 */
internal fun routeMaestro(packet: RpcPacket): RoutedFrame? {
    if (packet.serviceId != Maestro.SERVICE_ID) return null
    val method = packet.methodId
    if (method == Maestro.METHOD_GET_SOFTWARE_INFO && packet.type == PwRpc.TYPE_RESPONSE &&
        packet.callId == PwRpc.CALL_ID_UNSOLICITED
    ) {
        return RoutedFrame.MaestroHello(packet.channelId, SoftwareInfo.firmwareStrings(packet.payload))
    }
    val settingsMethod = method == Maestro.METHOD_READ_SETTING || method == Maestro.METHOD_WRITE_SETTING ||
        method == Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES
    val carriesValue = packet.type == PwRpc.TYPE_RESPONSE || packet.type == PwRpc.TYPE_SERVER_STREAM
    if (settingsMethod && carriesValue && packet.payload.isNotEmpty() &&
        (packet.status == null || packet.status == 0)
    ) {
        val eq = EqFrameDecoder.decode(packet)
        if (eq is BudsResult.Success) return RoutedFrame.Eq(eq.value)
    }
    // ADR-043: the runtime-info stream the app subscribes to once per Connect; only the Case entry is read.
    if (method == Maestro.METHOD_SUBSCRIBE_RUNTIME_INFO &&
        (packet.type == PwRpc.TYPE_SERVER_STREAM || packet.type == PwRpc.TYPE_RESPONSE) &&
        (packet.status == null || packet.status == 0) && packet.payload.isNotEmpty()
    ) {
        RuntimeInfoDecoder.caseBattery(packet.payload)?.let { return RoutedFrame.RuntimeInfoCase(it) }
    }
    val answersUs = method == Maestro.METHOD_READ_SETTING || method == Maestro.METHOD_WRITE_SETTING
    val isError = packet.type == PwRpc.TYPE_CLIENT_ERROR || packet.type == PwRpc.TYPE_SERVER_ERROR
    if (answersUs && (packet.type == PwRpc.TYPE_RESPONSE || isError)) {
        return RoutedFrame.RpcResult(packet.channelId, method, packet.type, packet.status)
    }
    return null
}

/** Splits a DLCI 0x02 byte stream on unescaped `0x7E` flags, per PROTOCOL.md
 * §2.2a's shared-flag HDLC convention (one frame's trailing flag doubles as
 * the next frame's leading flag). Returns each complete frame with both its
 * own leading and trailing flag byte, ready for [Hdlc.decode]. */
internal class HdlcFrameSplitter(private val maxFrameBytes: Int = MAX_HDLC_FRAME_BYTES) {
    private val buffer = ArrayList<Byte>()
    private var inFrame = false

    fun reset() {
        buffer.clear()
        inFrame = false
    }

    fun feed(bytes: ByteArray, onOverflow: (ByteArray) -> Unit = {}): List<ByteArray> {
        val frames = mutableListOf<ByteArray>()
        for (b in bytes) {
            if ((b.toInt() and 0xFF) == 0x7E) {
                if (inFrame && buffer.isNotEmpty()) {
                    frames += byteArrayOf(0x7E) + buffer.toByteArray() + byteArrayOf(0x7E)
                    buffer.clear()
                }
                inFrame = true
            } else if (inFrame) {
                buffer.add(b)
                if (buffer.size > maxFrameBytes) {
                    // No flag for longer than any real frame: garbage or a lost flag. Drop it and wait for the next flag.
                    onOverflow(buffer.toByteArray())
                    reset()
                }
            }
            // Bytes arriving before the very first flag (stream not yet
            // resynced) are discarded, not buffered.
        }
        return frames
    }
}

/** Splits a DLCI 0x04 byte stream using the length-prefixed
 * `[Group:1][Code:1][Len:2BE][Data:Len]` framing (PROTOCOL.md §2.1). */
internal class MessageStreamFrameSplitter(private val maxDataBytes: Int = MAX_MESSAGE_STREAM_DATA_BYTES) {
    private val buffer = ArrayList<Byte>()

    fun reset() = buffer.clear()

    fun feed(bytes: ByteArray, onOverflow: (ByteArray) -> Unit = {}): List<ByteArray> {
        buffer.addAll(bytes.toList())
        val frames = mutableListOf<ByteArray>()
        while (true) {
            if (buffer.size < 4) break
            val declaredLength = ((buffer[2].toInt() and 0xFF) shl 8) or (buffer[3].toInt() and 0xFF)
            if (declaredLength > maxDataBytes) {
                // A length no real frame has (a corrupted or misaligned header): waiting for it would block the stream until the
                // app restarts. Drop the buffer; the next claim starts clean anyway (reset()).
                onOverflow(buffer.toByteArray())
                buffer.clear()
                break
            }
            val totalLength = 4 + declaredLength
            if (buffer.size < totalLength) break
            frames += buffer.subList(0, totalLength).toByteArray()
            repeat(totalLength) { buffer.removeAt(0) }
        }
        return frames
    }
}

/**
 * Largest accepted Message Stream data length (DLCI 0x04/0x08). The largest real frame in the captures is DLCI 0x08's
 * `02 04 00 41` (65 data bytes, `CAP-060` frame 1316); the spec's length is a uint16, so 1024 leaves wide headroom without letting
 * a corrupted header stall the stream.
 */
internal const val MAX_MESSAGE_STREAM_DATA_BYTES: Int = 1024

/** Largest accepted unescaped pw_hdlc frame body between two flags (DLCI 0x02); real pw_rpc packets are well under 300 bytes. */
internal const val MAX_HDLC_FRAME_BYTES: Int = 4096
