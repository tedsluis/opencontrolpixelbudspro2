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

import io.github.tedsluis.opencontrolpixelbuds.domain.AncModeList
import io.github.tedsluis.opencontrolpixelbuds.domain.Bud
import io.github.tedsluis.opencontrolpixelbuds.domain.HoldAction

/**
 * One non-EQ `qhr` setting value, as a `ReadSetting` answer carries it and as a `WriteSetting` request sends it: payload `4:{N: …}`
 * (PROTOCOL.md §4.5, ADR-013/019/034). Only the fields ADR-036/046 let the app read are modelled; [Flag] (2 — ADR-047; 4, 19, 22 — ADR-045; 11 — ADR-053;
 * 27, 28 — ADR-054; 15 — ADR-055),
 * [Balance] (17), [PressAndHold] (7) — ADR-045 — [AncModes] (12, ADR-046) and [HeadGestures] (29, ADR-052) are the only values ever written.
 */
sealed class SettingValue {
    abstract val field: Int

    /** A 0/1 setting: 2 in-ear detection, 4 touch controls, 11 Multipoint, 15 Volume EQ, 19 mono audio, 22 conversation detection, 27 / 28 case sounds. */
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

    /** Field 12, `12{1:b 2:b 3:b 4:b}` — the press-and-hold ANC-mode list (`qht`; 1 NC, 2 Off, 3 Transparency, 4 Adaptive — PROTOCOL.md §4.5.3, ADR-046). */
    data class AncModes(val list: AncModeList) : SettingValue() {
        override val field: Int get() = SettingsCodec.FIELD_ANC_MODE_LIST
    }

    /**
     * Field 29, "Use head gestures" (🟢 PROTOCOL.md §4.5.4, ADR-052). **Not a 0/1 flag:** on the wire [SettingsCodec.HEAD_GESTURES_OFF] = 1 and
     * [SettingsCodec.HEAD_GESTURES_ON] = 2, in both directions (`CAP-069` 2492/2564); a read of any other value is not interpreted ("—").
     */
    data class HeadGestures(val on: Boolean) : SettingValue() {
        override val field: Int get() = SettingsCodec.FIELD_HEAD_GESTURES
    }
}

/**
 * Encoder/decoder for [SettingValue] (hand-written, DECISIONS.md ADR-041). The encoder is byte-identical to the official app's writes for the same
 * channel (`SettingsCodecTest`: `CAP-019` 1720/1808/2293/2482, `CAP-020` 1741/1995, `CAP-021` 1895/3619/4315/4976, `CAP-022` 1621/1823/1922…2099,
 * `CAP-056` 1689/1725/1786/1815/1843/2173/4048/2849/3627, `CAP-041` 2176/2192, `CAP-069` 3161/3212/2492/2564, `CAP-024` 1988/2023/2053/2084, `CAP-058`
 * 5623/5643/5680/5697, `CAP-022` 1871/1895, `CAP-041` 2461). The
 * decoder never throws and returns `null` for anything that is not exactly one readable field of the expected shape (AGENTS.md §11).
 */
object SettingsCodec {
    const val FIELD_IN_EAR_DETECTION = 2
    const val FIELD_TOUCH_CONTROLS = 4
    const val FIELD_PRESS_AND_HOLD = 7
    const val FIELD_MULTIPOINT = 11
    const val FIELD_ANC_MODE_LIST = 12

    /**
     * "Volume EQ" (🟢 PROTOCOL.md §4.5.6; ADR-055). Writes are byte-identical to `CAP-022` 1871/1895 (channel 19) and `CAP-041` 2461 (channel 21, off).
     * TODO(verify): the channel-21 `4:{15:1}` write has never been captured (ADR-055); it is built by the same codec and covered only by a labelled structural
     * test until the hardware run records it — `captures/CAP-070-…/CAP-070-EVENT-NOTES.md` (Group BF), step "Volume EQ on, channel 21"; PROTOCOL.md §4.5.6.
     */
    const val FIELD_VOLUME_EQ = 15
    const val FIELD_VOLUME_BALANCE = 17
    const val FIELD_MONO_AUDIO = 19
    const val FIELD_CONVERSATION_DETECTION = 22

    /** Case sounds "Other alerts" (🟢 label, PROTOCOL.md §4.5.8 Update of 2026-10-06; ADR-054). */
    const val FIELD_CASE_SOUND_OTHER_ALERTS = 27

    /** Case sounds "Earbuds replaced" ("Bud return", 🟢 PROTOCOL.md §4.5.8; ADR-054). */
    const val FIELD_CASE_SOUND_EARBUDS_REPLACED = 28
    const val FIELD_HEAD_GESTURES = 29

    /** Field 29's wire values (ADR-052): 1 = off, 2 = on — `CAP-069` 2492 `e8 01 01` (OFF tap), 2564 `e8 01 02` (ON tap). */
    const val HEAD_GESTURES_OFF = 1
    const val HEAD_GESTURES_ON = 2

    /** The 0/1 fields the decoder reads (ADR-036). */
    private val FLAG_FIELDS = setOf(
        FIELD_IN_EAR_DETECTION, FIELD_TOUCH_CONTROLS, FIELD_MULTIPOINT, FIELD_VOLUME_EQ, FIELD_MONO_AUDIO, FIELD_CONVERSATION_DETECTION,
        FIELD_CASE_SOUND_OTHER_ALERTS, FIELD_CASE_SOUND_EARBUDS_REPLACED,
    )

    /** The 0/1 fields a write may name: 4, 19, 22 (ADR-045), 2 (ADR-047), 11 (ADR-053), 27 and 28 (ADR-054), 15 (ADR-055). */
    val WRITABLE_FLAG_FIELDS: Set<Int> = setOf(
        FIELD_IN_EAR_DETECTION, FIELD_TOUCH_CONTROLS, FIELD_MULTIPOINT, FIELD_VOLUME_EQ, FIELD_MONO_AUDIO, FIELD_CONVERSATION_DETECTION,
        FIELD_CASE_SOUND_OTHER_ALERTS, FIELD_CASE_SOUND_EARBUDS_REPLACED,
    )

    private const val FIELD4 = 4
    private const val WIRETYPE_VARINT = 0
    private const val WIRETYPE_LEN = 2
    private const val QJU_LEFT = 1
    private const val QJU_RIGHT = 2
    private const val QIK_ACTION = 4
    private const val QHO_VALUE = 1
    private const val QHT_NOISE_CANCELLATION = 1
    private const val QHT_OFF = 2
    private const val QHT_TRANSPARENCY = 3
    private const val QHT_ADAPTIVE = 4

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

    /**
     * `WriteSetting 4:{12:{1:b 2:b 3:b 4:b}}` (ADR-046): all four booleans, always, in field order — byte-identical to the official app's writes for the same
     * list (`CAP-056` 1689/1725/1786/1815/1843 on channel 19, `CAP-041` 2176/2192 on channel 21). `null` when fewer than [AncModeList.MIN_SELECTED] are ticked:
     * such a list can never be sent (the official app's rule, `hgj.java:165–168`).
     */
    fun ancModeListRequest(channelId: Int, list: AncModeList): RpcPacket? {
        if (!list.isValid) return null
        fun flag(field: Int, on: Boolean) = tag(field, WIRETYPE_VARINT) + Varint.encode(if (on) 1 else 0)
        val qht = flag(QHT_NOISE_CANCELLATION, list.noiseCancellation) + flag(QHT_OFF, list.off) +
            flag(QHT_TRANSPARENCY, list.transparency) + flag(QHT_ADAPTIVE, list.adaptive)
        return writeRequest(channelId, tag(FIELD_ANC_MODE_LIST, WIRETYPE_LEN) + len(qht) + qht)
    }

    /** `WriteSetting 4:{N:0|1}` for a field in [WRITABLE_FLAG_FIELDS] (ADR-045/047/053/054/055); `null` for any other field — nothing else can be written. */
    fun flagRequest(channelId: Int, field: Int, on: Boolean): RpcPacket? {
        if (field !in WRITABLE_FLAG_FIELDS) return null
        return writeRequest(channelId, tag(field, WIRETYPE_VARINT) + Varint.encode(if (on) 1 else 0))
    }

    /**
     * `WriteSetting 4:{29:1|2}` (ADR-052): [HEAD_GESTURES_ON] for on, [HEAD_GESTURES_OFF] for off — byte-identical to `CAP-069` 2492/2564 on channel 21. Its own
     * function, not [flagRequest]: field 29 is never in [WRITABLE_FLAG_FIELDS], so a 0/1 value can never be written to it.
     */
    fun headGesturesRequest(channelId: Int, on: Boolean): RpcPacket =
        writeRequest(channelId, tag(FIELD_HEAD_GESTURES, WIRETYPE_VARINT) + Varint.encode(if (on) HEAD_GESTURES_ON else HEAD_GESTURES_OFF))

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
            FIELD_ANC_MODE_LIST -> decodeAncModes(f.bytes ?: return null)
            FIELD_HEAD_GESTURES -> when (f.varint) {
                HEAD_GESTURES_OFF -> SettingValue.HeadGestures(false)
                HEAD_GESTURES_ON -> SettingValue.HeadGestures(true)
                else -> null // 0, 3, … : not interpreted (ADR-052: shown as "—")
            }
            else -> null
        }
    }

    /**
     * `qht`: exactly the four booleans 1–4, each once, each 0 or 1 — the only shape in any capture (`qht` has presence bits, so a `false` is sent as `0`,
     * never omitted: `CAP-056` 1531/1689, `CAP-036` 1516). Anything else (a missing, repeated or unknown field, another value) is not interpreted.
     */
    private fun decodeAncModes(qht: ByteArray): SettingValue.AncModes? {
        val fields = Proto.fields(qht) ?: return null
        if (fields.size != 4) return null
        val byNumber = fields.associateBy { it.number }
        if (byNumber.keys != setOf(QHT_NOISE_CANCELLATION, QHT_OFF, QHT_TRANSPARENCY, QHT_ADAPTIVE)) return null
        fun flag(n: Int): Boolean? {
            val f = byNumber.getValue(n)
            if (f.bytes != null) return null
            return when (f.varint) {
                0 -> false
                1 -> true
                else -> null
            }
        }
        return SettingValue.AncModes(
            AncModeList(
                noiseCancellation = flag(QHT_NOISE_CANCELLATION) ?: return null,
                off = flag(QHT_OFF) ?: return null,
                transparency = flag(QHT_TRANSPARENCY) ?: return null,
                adaptive = flag(QHT_ADAPTIVE) ?: return null,
            ),
        )
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
