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

import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult

/**
 * One Pigweed pw_rpc `RpcPacket` — the payload of every DLCI 0x02 pw_hdlc frame (PROTOCOL.md §2.2a,
 * 🟢 FACT, DECISIONS.md ADR-034). Protobuf fields: 1 `type`, 2 `channel_id`, 3 `service_id` (fixed32),
 * 4 `method_id` (fixed32), 5 `payload`, 6 `status`, 7 `call_id`. A REQUEST omits `type` (0).
 */
class RpcPacket(
    val type: Int,
    val channelId: Int,
    val serviceId: Int,
    val methodId: Int,
    val payload: ByteArray = ByteArray(0),
    val status: Int? = null,
    val callId: Long? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is RpcPacket && type == other.type && channelId == other.channelId && serviceId == other.serviceId &&
            methodId == other.methodId && payload.contentEquals(other.payload) && status == other.status &&
            callId == other.callId

    override fun hashCode(): Int {
        var r = type
        r = 31 * r + channelId
        r = 31 * r + serviceId
        r = 31 * r + methodId
        r = 31 * r + payload.contentHashCode()
        r = 31 * r + (status ?: -1)
        r = 31 * r + (callId?.hashCode() ?: 0)
        return r
    }

    /** Payload-free description for the always-on log (AGENTS.md §9): structure only, never bytes. */
    fun summary(): String =
        "pw_rpc ${PwRpc.typeName(type)} ch=$channelId method=${PwRpc.methodName(serviceId, methodId)} " +
            "status=${PwRpc.statusName(status)}"
}

object PwRpc {
    const val TYPE_REQUEST = 0
    const val TYPE_RESPONSE = 1
    const val TYPE_CLIENT_STREAM = 2
    const val TYPE_CLIENT_ERROR = 4
    const val TYPE_SERVER_ERROR = 5
    const val TYPE_SERVER_STREAM = 7

    /** pw_rpc's "no call id" marker on a push the Buds send without a request (PROTOCOL.md §2.2a). */
    const val CALL_ID_UNSOLICITED = 0xFFFFFFFFL

    /** pw_rpc/pw_tokenizer 65599 name hash: `h = len; c = 65599; h += c*byte; c *= 65599` (mod 2^32). */
    fun nameHash(name: String): Int {
        val bytes = name.toByteArray(Charsets.UTF_8)
        var h = bytes.size
        var coefficient = 65599
        for (b in bytes) {
            h += coefficient * (b.toInt() and 0xFF)
            coefficient *= 65599
        }
        return h
    }

    fun encode(packet: RpcPacket): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        if (packet.type != TYPE_REQUEST) {
            out.write(0x08)
            out.write(varint(packet.type.toLong()))
        }
        out.write(0x10)
        out.write(varint(packet.channelId.toLong() and 0xFFFFFFFFL))
        out.write(0x1d)
        out.write(fixed32(packet.serviceId))
        out.write(0x25)
        out.write(fixed32(packet.methodId))
        if (packet.payload.isNotEmpty()) {
            out.write(0x2a)
            out.write(varint(packet.payload.size.toLong()))
            out.write(packet.payload)
        }
        packet.status?.let {
            out.write(0x30)
            out.write(varint(it.toLong()))
        }
        packet.callId?.let {
            out.write(0x38)
            out.write(varint(it))
        }
        return out.toByteArray()
    }

    /** Structural failure is [BudsError.MalformedFrame], never thrown (AGENTS.md §11). */
    fun decode(bytes: ByteArray): BudsResult<RpcPacket> {
        var type = 0
        var channelId = 0
        var serviceId: Int? = null
        var methodId: Int? = null
        var payload = ByteArray(0)
        var status: Int? = null
        var callId: Long? = null
        var i = 0
        fun bad() = BudsResult.Failure(BudsError.MalformedFrame(bytes))
        while (i < bytes.size) {
            val (tag, afterTag) = readVarint(bytes, i) ?: return bad()
            i = afterTag
            val field = (tag shr 3).toInt()
            val wire = (tag and 7).toInt()
            if (field == 0) return bad()
            when (wire) {
                0 -> {
                    val (v, next) = readVarint(bytes, i) ?: return bad()
                    i = next
                    when (field) {
                        1 -> type = v.toInt()
                        2 -> channelId = v.toInt()
                        6 -> status = v.toInt()
                        7 -> callId = v
                    }
                }
                5 -> {
                    if (i + 4 > bytes.size) return bad()
                    val v = (bytes[i].toInt() and 0xFF) or ((bytes[i + 1].toInt() and 0xFF) shl 8) or
                        ((bytes[i + 2].toInt() and 0xFF) shl 16) or ((bytes[i + 3].toInt() and 0xFF) shl 24)
                    i += 4
                    when (field) {
                        3 -> serviceId = v
                        4 -> methodId = v
                    }
                }
                2 -> {
                    val (len, next) = readVarint(bytes, i) ?: return bad()
                    if (len < 0 || next + len > bytes.size) return bad()
                    if (field == 5) payload = bytes.copyOfRange(next, next + len.toInt())
                    i = next + len.toInt()
                }
                1 -> {
                    if (i + 8 > bytes.size) return bad()
                    i += 8
                }
                else -> return bad()
            }
        }
        if (serviceId == null || methodId == null) return bad()
        return BudsResult.Success(RpcPacket(type, channelId, serviceId, methodId, payload, status, callId))
    }

    fun typeName(type: Int): String = when (type) {
        TYPE_REQUEST -> "REQUEST"
        TYPE_RESPONSE -> "RESPONSE"
        TYPE_CLIENT_STREAM -> "CLIENT_STREAM"
        TYPE_CLIENT_ERROR -> "CLIENT_ERROR"
        TYPE_SERVER_ERROR -> "SERVER_ERROR"
        TYPE_SERVER_STREAM -> "SERVER_STREAM"
        else -> "type$type"
    }

    /** pw_rpc `Status` (google.rpc.Code numbering); `null` = field absent = OK on the wire (CAP-015 frame 2117). */
    fun statusName(status: Int?): String = when (status) {
        null, 0 -> "OK"
        1 -> "CANCELLED"
        2 -> "UNKNOWN"
        3 -> "INVALID_ARGUMENT"
        5 -> "NOT_FOUND"
        9 -> "FAILED_PRECONDITION"
        12 -> "UNIMPLEMENTED"
        else -> "status$status"
    }

    fun methodName(serviceId: Int, methodId: Int): String =
        if (serviceId == Maestro.SERVICE_ID) {
            when (methodId) {
                Maestro.METHOD_WRITE_SETTING -> "WriteSetting"
                Maestro.METHOD_READ_SETTING -> "ReadSetting"
                Maestro.METHOD_SUBSCRIBE_TO_SETTINGS_CHANGES -> "SubscribeToSettingsChanges"
                Maestro.METHOD_GET_SOFTWARE_INFO -> "GetSoftwareInfo"
                else -> "Maestro/0x%08x".format(methodId)
            }
        } else {
            "0x%08x/0x%08x".format(serviceId, methodId)
        }

    private fun varint(value: Long): ByteArray {
        var v = value
        val out = ArrayList<Byte>(5)
        do {
            var b = (v and 0x7F).toInt()
            v = v ushr 7
            if (v != 0L) b = b or 0x80
            out.add(b.toByte())
        } while (v != 0L)
        return out.toByteArray()
    }

    private fun fixed32(v: Int) = byteArrayOf(
        (v and 0xFF).toByte(), ((v shr 8) and 0xFF).toByte(), ((v shr 16) and 0xFF).toByte(), ((v shr 24) and 0xFF).toByte(),
    )

    /** Returns `(value, indexAfter)`, or null for a truncated/over-long (> 10 byte) varint. */
    internal fun readVarint(data: ByteArray, start: Int): Pair<Long, Int>? {
        var value = 0L
        var shift = 0
        var i = start
        while (true) {
            if (i >= data.size || shift >= 70) return null
            val b = data[i].toInt() and 0xFF
            value = value or ((b and 0x7F).toLong() shl shift)
            i++
            if (b and 0x80 == 0) return value to i
            shift += 7
        }
    }
}
