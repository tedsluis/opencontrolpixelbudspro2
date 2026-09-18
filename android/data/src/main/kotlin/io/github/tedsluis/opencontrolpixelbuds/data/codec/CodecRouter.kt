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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.domain.UnidentifiedFrame

/** DLCI numbers this app implements against (ARCHITECTURE.md §5a). */
object Dlci {
    const val MAESTRO: Int = 0x02
    const val FAST_PAIR_MESSAGE_STREAM: Int = 0x04
}

/** One fully-decoded, recognized inbound frame, tagged by which feature it belongs to. */
sealed class RoutedFrame {
    data class Anc(val frame: AncFrame) : RoutedFrame()
    data class Eq(val frame: EqFrame) : RoutedFrame()
    data class Ring(val frame: RingFrame) : RoutedFrame()
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
 * independently" rule. Only DLCI 0x02/0x04 are handled — any other channelId
 * is out of this session's implementation scope (ARCHITECTURE.md §5a) and is
 * ignored rather than guessed at.
 */
class CodecRouter {
    private val maestroSplitter = HdlcFrameSplitter()
    private val messageStreamSplitter = MessageStreamFrameSplitter()

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
    fun feed(
        channelId: Int,
        bytes: ByteArray,
        timestampMillis: Long,
        onUnidentified: (UnidentifiedFrame) -> Unit = {},
        onMalformed: (ByteArray) -> Unit = {},
    ): List<RoutedFrame> {
        val routed = mutableListOf<RoutedFrame>()

        when (channelId) {
            Dlci.MAESTRO -> for (frame in maestroSplitter.feed(bytes)) {
                when (val decoded = Hdlc.decode(frame)) {
                    is BudsResult.Failure -> onMalformed(frame)
                    is BudsResult.Success -> {
                        when (val eq = EqFrameDecoder.decode(decoded.value.payload)) {
                            is BudsResult.Success -> routed += RoutedFrame.Eq(eq.value)
                            is BudsResult.Failure -> onUnidentified(
                                UnidentifiedFrame(
                                    channelId = channelId,
                                    group = null,
                                    code = null,
                                    raw = decoded.value.payload,
                                    timestampMillis = timestampMillis,
                                ),
                            )
                        }
                    }
                }
            }

            Dlci.FAST_PAIR_MESSAGE_STREAM -> for (frame in messageStreamSplitter.feed(bytes)) {
                val group = frame.getOrNull(0)?.toInt()?.and(0xFF)
                val code = frame.getOrNull(1)?.toInt()?.and(0xFF)

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

            else -> Unit // Out of this session's implementation scope (ARCHITECTURE.md §5a) — ignored.
        }

        return routed
    }
}

/** Splits a DLCI 0x02 byte stream on unescaped `0x7E` flags, per PROTOCOL.md
 * §2.2a's shared-flag HDLC convention (one frame's trailing flag doubles as
 * the next frame's leading flag). Returns each complete frame with both its
 * own leading and trailing flag byte, ready for [Hdlc.decode]. */
internal class HdlcFrameSplitter {
    private val buffer = ArrayList<Byte>()
    private var inFrame = false

    fun feed(bytes: ByteArray): List<ByteArray> {
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
            }
            // Bytes arriving before the very first flag (stream not yet
            // resynced) are discarded, not buffered.
        }
        return frames
    }
}

/** Splits a DLCI 0x04 byte stream using the length-prefixed
 * `[Group:1][Code:1][Len:2BE][Data:Len]` framing (PROTOCOL.md §2.1). */
internal class MessageStreamFrameSplitter {
    private val buffer = ArrayList<Byte>()

    fun feed(bytes: ByteArray): List<ByteArray> {
        buffer.addAll(bytes.toList())
        val frames = mutableListOf<ByteArray>()
        while (true) {
            if (buffer.size < 4) break
            val declaredLength = ((buffer[2].toInt() and 0xFF) shl 8) or (buffer[3].toInt() and 0xFF)
            val totalLength = 4 + declaredLength
            if (buffer.size < totalLength) break
            frames += buffer.subList(0, totalLength).toByteArray()
            repeat(totalLength) { buffer.removeAt(0) }
        }
        return frames
    }
}
