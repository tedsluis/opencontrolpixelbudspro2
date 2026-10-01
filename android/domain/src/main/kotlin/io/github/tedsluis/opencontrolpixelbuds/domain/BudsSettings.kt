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
package io.github.tedsluis.opencontrolpixelbuds.domain

/**
 * One setting's value as the Buds reported it and the wall-clock time it was received: a `ReadSetting` answer ([changedByApp] = `false`, shown
 * "read HH:MM:SS") or this app's write the Buds acknowledged ([changedByApp] = `true`, "changed HH:MM:SS").
 */
data class SettingReading<T>(val value: T, val atMillis: Long, val changedByApp: Boolean = false)

/** What a press-and-hold on one bud does (`qhr` field 7, PROTOCOL.md §4.5.3, ADR-019): wire value 5 or 6. Other values are not interpreted. */
enum class HoldAction(val wire: Int) {
    /** "Active noise control" in the official app. */
    NOISE_CONTROL(5),

    /** "Digital assistant". */
    ASSISTANT(6),
    ;

    companion object {
        fun fromWire(value: Int): HoldAction? = entries.firstOrNull { it.wire == value }
    }
}

/** Why the last settings read ([write] = `false`) or write ([write] = `true`) failed — the screen words the two differently (`ai-sessions/0052`). */
data class SettingsFailure(val error: BudsError, val write: Boolean)

/** Which bud a per-bud setting is for (`qhr` field 7: 1 = Left, 2 = Right). */
enum class Bud { LEFT, RIGHT }

/**
 * The press-and-hold ANC-mode list (`qhr` field 12 = `qht`, "ANC gesture loop"; PROTOCOL.md §4.5.3 2026-09-28 Update, DECISIONS.md ADR-046): which noise-control
 * modes a press and hold cycles through. One list for both buds — the write carries no side (🟢), that the Buds keep one list is 🟡. Wire booleans
 * 1 = [noiseCancellation], 2 = [off], 3 = [transparency], 4 = [adaptive] (🟢, `CAP-056` 1815/1843).
 */
data class AncModeList(
    val noiseCancellation: Boolean,
    val off: Boolean,
    val transparency: Boolean,
    val adaptive: Boolean,
) {
    val selectedCount: Int get() = listOf(noiseCancellation, off, transparency, adaptive).count { it }

    /** At least [MIN_SELECTED] modes are ticked — the official app's rule (`hgj.java:165–168`), enforced by this app before anything is sent. */
    val isValid: Boolean get() = selectedCount >= MIN_SELECTED

    fun isSelected(mode: AncMode): Boolean = when (mode) {
        AncMode.ACTIVE -> noiseCancellation
        AncMode.OFF -> off
        AncMode.TRANSPARENT -> transparency
        AncMode.ADAPTIVE -> adaptive
    }

    /** The same list with [mode] ticked or unticked (the other three unchanged). */
    fun with(mode: AncMode, selected: Boolean): AncModeList = when (mode) {
        AncMode.ACTIVE -> copy(noiseCancellation = selected)
        AncMode.OFF -> copy(off = selected)
        AncMode.TRANSPARENT -> copy(transparency = selected)
        AncMode.ADAPTIVE -> copy(adaptive = selected)
    }

    /** Whether unticking [mode] would leave fewer than [MIN_SELECTED] — then the box cannot be unticked (`ai-sessions/0056`, "Box disabled + line"). */
    fun isLocked(mode: AncMode): Boolean = isSelected(mode) && selectedCount <= MIN_SELECTED

    companion object {
        const val MIN_SELECTED = 2
    }
}

/**
 * The DLCI 0x02 settings this app reads (DECISIONS.md ADR-036, ADR-046) and may write (ADR-045, ADR-046, ADR-047). Every field is `null` until
 * the Buds reported it on this connection (read at Connect, then an acknowledged write) — never a default presented as the Buds' value (AGENTS.md §5).
 */
data class BudsSettings(
    /** `qhr` field 2, the "In-ear detection" **setting** (🟢 label, PROTOCOL.md §4.5.5 2026-09-28 Update) — not whether a bud is worn. Writable (ADR-047). */
    val inEarDetection: SettingReading<Boolean>? = null,
    /** `qhr` field 12, the press-and-hold ANC-mode list (ADR-046). */
    val ancModeList: SettingReading<AncModeList>? = null,
    /** `qhr` field 4, "Use touch controls" (🟢 both directions). */
    val touchControls: SettingReading<Boolean>? = null,
    /** `qhr` field 7, press-and-hold action of the Left bud. */
    val holdLeft: SettingReading<HoldAction>? = null,
    /** `qhr` field 7, press-and-hold action of the Right bud. */
    val holdRight: SettingReading<HoldAction>? = null,
    /** `qhr` field 17, volume balance −100 … +100; **+100 = Left**, −100 = Right (ADR-026). */
    val volumeBalance: SettingReading<Int>? = null,
    /** `qhr` field 19, mono audio. */
    val monoAudio: SettingReading<Boolean>? = null,
    /** `qhr` field 22, conversation detection (🟢 label equivalence, PROTOCOL.md §4.5.1 2026-09-26 Update). */
    val conversationDetection: SettingReading<Boolean>? = null,
) {
    companion object {
        /** ADR-026: the wire range of the balance. */
        val BALANCE_RANGE = -100..100

        /**
         * `ai-sessions/0054` I-3 (the maintainer's choice in chat 2026-09-28, "Snap within ±3"): a released balance within this distance of the centre is
         * written as 0 ("Centre"). `CAP-063`: 12 releases near the centre landed on −11, −10, −4, −1, −1, −2, +11, +7, −6, +3, −4, −1 (frames 5180 … 5245, zigzag-decoded) —
         * never 0 on a 201-step slider; 5 of them are within ±3.
         */
        const val BALANCE_CENTRE_SNAP = 3

        /** The value a released balance slider writes: 0 within ±[BALANCE_CENTRE_SNAP], otherwise itself, clamped to [BALANCE_RANGE]. */
        fun snapBalance(value: Int): Int = if (kotlin.math.abs(value) <= BALANCE_CENTRE_SNAP) 0 else value.coerceIn(BALANCE_RANGE)

        /**
         * `ai-sessions/0064` F-2 (the maintainer's choice "−/+ of 1, slider kept", chat 2026-10-01): one step of the `[‹]`/`[›]` buttons beside the slider.
         * `CAP-066` (FINDINGS §5): 32 drags never reached Right 4 (`17:7`). A step is the Buds' [current] value plus [BALANCE_STEP] toward Left (+, the wire's
         * +100 = Left, ADR-026) or minus it toward Right — **no** centre snap (Centre → Right 1 must be possible). `null` at the end of [BALANCE_RANGE]: nothing
         * to write.
         */
        fun balanceStep(current: Int, towardLeft: Boolean): Int? {
            val next = current + if (towardLeft) BALANCE_STEP else -BALANCE_STEP
            return next.takeIf { it in BALANCE_RANGE }
        }

        /** The `[‹]`/`[›]` step (F-2): 1, so every wire value is reachable. */
        const val BALANCE_STEP = 1
    }
}
