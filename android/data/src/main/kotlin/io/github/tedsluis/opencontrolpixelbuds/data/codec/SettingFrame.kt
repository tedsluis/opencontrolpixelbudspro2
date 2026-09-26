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

import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction

/**
 * One non-EQ `qhr` setting value, as a `ReadSetting` answer carries it and as a `WriteSetting` request sends it: payload `4:{N: …}`
 * (PROTOCOL.md §4.5, ADR-013/019/034). Only the fields ADR-036 lets the app read are modelled; only [Flag] (4, 19, 22), [Balance] (17) and
 * [PressAndHold] (7) are ever written (ADR-045). Field 12 has no representation on purpose.
 */
sealed class SettingValue {
    abstract val field: Int

    /** A 0/1 setting: 2 in-ear detection (read only), 4 touch controls, 19 mono audio, 22 conversation detection. */
    data class Flag(override val field: Int, val on: Boolean) : SettingValue()

    /** Field 17, `sint32` (zigzag) −100 … +100, +100 = Left (ADR-026). */
    data class Balance(val value: Int) : SettingValue() {
        override val field: Int get() = SettingsCodec.FIELD_VOLUME_BALANCE
    }

    /**
     * Field 7, `7{1|2:{4:{1:<action>}}}` (1 = Left, 2 = Right; ADR-019). A read answer carries both buds, a write one; a bud whose action is not
     * 5/6 is `null` (not interpreted).
     */
    data class PressAndHold(val left: HoldAction?, val right: HoldAction?) : SettingValue() {
        override val field: Int get() = SettingsCodec.FIELD_PRESS_AND_HOLD
    }
}

/**
 * Encoder/decoder for [SettingValue] (hand-written, DECISIONS.md ADR-041). The encoder is byte-identical to the official app's writes for the same
 * channel (`SettingsCodecTest`: `CAP-019` 1720/1808, `CAP-020` 1741/1995, `CAP-021` 1895/3619/4315/4976, `CAP-022` 1621/1823/1922…2099). The
 * decoder never throws and returns `null` for anything that is not exactly one readable field of the expected shape (AGENTS.md §11).
 */
object SettingsCodec {
    const val FIELD_IN_EAR_DETECTION = 2
    const val FIELD_TOUCH_CONTROLS = 4
    const val FIELD_PRESS_AND_HOLD = 7
    const val FIELD_VOLUME_BALANCE = 17
    const val FIELD_MONO_AUDIO = 19
    const val FIELD_CONVERSATION_DETECTION = 22

    /** The 0/1 fields the decoder reads (ADR-036). */
    private val FLAG_FIELDS = setOf(FIELD_IN_EAR_DETECTION, FIELD_TOUCH_CONTROLS, FIELD_MONO_AUDIO, FIELD_CONVERSATION_DETECTION)

    /** The 0/1 fields a write may name (ADR-045) — not 2. */
    val WRITABLE_FLAG_FIELDS: Set<Int> = setOf(FIELD_TOUCH_CONTROLS, FIELD_MONO_AUDIO, FIELD_CONVERSATION_DETECTION)

    private const val FIELD4 = 4
    private const val WIRETYPE_VARINT = 0
    private const val WIRETYPE_LEN = 2
    private const val QJU_LEFT = 1
    private const val QJU_RIGHT = 2
    private const val QIK_ACTION = 4
    private const val QHO_VALUE = 1

    /**
     * The `WriteSetting` request for one bud's press-and-hold action (ADR-045): `4:{7:{1|2:{4:{1:<5|6>}}}}` — one bud per write, as the official
     * app sends it (`CAP-021` frames 1895/3619/4315/4976).
     */
    fun pressAndHoldRequest(channelId: Int, bud: Bud, action: HoldAction): RpcPacket {
        val qho = tag(QHO_VALUE, WIRETYPE_VARINT) + Varint.encode(action.wire)
        val qik = tag(QIK_ACTION, WIRETYPE_LEN) + len(qho) + qho
        val side = tag(if (bud == Bud.LEFT) QJU_LEFT else QJU_RIGHT, WIRETYPE_LEN) + len(qik) + qik
        val qju = tag(FIELD_PRESS_AND_HOLD, WIRETYPE_LEN) + len(side) + side
        return writeRequest(channelId, qju)
    }

    /** `WriteSetting 4:{17:<zigzag(value)>}` (ADR-045); [value] is clamped to −100 … +100 (ADR-026). */
    fun balanceRequest(channelId: Int, value: Int): RpcPacket {
        val v = value.coerceIn(-100, 100)
        val zigzag = (v shl 1) xor (v shr 31)
        return writeRequest(channelId, tag(FIELD_VOLUME_BALANCE, WIRETYPE_VARINT) + Varint.encode(zigzag))
    }

    /** `WriteSetting 4:{N:0|1}` for a field in [WRITABLE_FLAG_FIELDS] (ADR-045); `null` for any other field — nothing else can be written. */
    fun flagRequest(channelId: Int, field: Int, on: Boolean): RpcPacket? {
        if (field !in WRITABLE_FLAG_FIELDS) return null
        return writeRequest(channelId, tag(field, WIRETYPE_VARINT) + Varint.encode(if (on) 1 else 0))
    }

    private fun writeRequest(channelId: Int, inner: ByteArray) = RpcPacket(
        type = PwRpc.TYPE_REQUEST,
        channelId = channelId,
        serviceId = Maestro.SERVICE_ID,
        methodId = Maestro.METHOD_WRITE_SETTING,
        payload = tag(FIELD4, WIRETYPE_LEN) + len(inner) + inner,
    )

    private fun tag(field: Int, wireType: Int): ByteArray = Varint.encode((field shl 3) or wireType)

    private fun len(bytes: ByteArray): ByteArray = Varint.encode(bytes.size)

    /** Reads `4:{N: …}` for one of the ADR-036 fields; `null` for any other shape, field or value. Never throws. */
    fun decode(payload: ByteArray): SettingValue? {
        val top = Proto.fields(payload) ?: return null
        if (top.size != 1 || top[0].number != FIELD4) return null
        val inner = Proto.fields(top[0].bytes ?: return null) ?: return null
        if (inner.size != 1) return null
        val f = inner[0]
        return when (f.number) {
            in FLAG_FIELDS -> when (f.varint) {
                0 -> SettingValue.Flag(f.number, false)
                1 -> SettingValue.Flag(f.number, true)
                else -> null
            }
            FIELD_VOLUME_BALANCE -> {
                val raw = f.varint ?: return null
                val value = (raw ushr 1) xor -(raw and 1)
                if (value in -100..100) SettingValue.Balance(value) else null
            }
            FIELD_PRESS_AND_HOLD -> decodePressAndHold(f.bytes ?: return null)
            else -> null
        }
    }

    private fun decodePressAndHold(qju: ByteArray): SettingValue.PressAndHold? {
        val sides = Proto.fields(qju) ?: return null
        if (sides.isEmpty() || sides.any { it.number != QJU_LEFT && it.number != QJU_RIGHT || it.bytes == null }) return null
        fun action(side: Int): HoldAction? {
            val qik = sides.firstOrNull { it.number == side }?.bytes?.let(Proto::fields) ?: return null
            val qho = qik.firstOrNull { it.number == QIK_ACTION }?.bytes?.let(Proto::fields) ?: return null
            return qho.firstOrNull { it.number == QHO_VALUE && it.bytes == null }?.varint?.let(HoldAction::fromWire)
        }
        val left = action(QJU_LEFT)
        val right = action(QJU_RIGHT)
        return if (left == null && right == null) null else SettingValue.PressAndHold(left, right)
    }
}
