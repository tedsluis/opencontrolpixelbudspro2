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
 * Whether the Buds currently let this app switch the ANC mode — the `Settable toggles` byte of their last `Notify ANC state` (Hearable Controls
 * spec: "to indicate which are currently enabled"). Replaces the earlier `DockState` (`ai-sessions/0048`, I-3): DECISIONS.md ADR-024's
 * 2026-09-25 Update records 🟢 (`CAP-062`) that a `Set` while the byte reads `0x00` is NAKed with reason `0x02` "Not allowed due to current state"
 * (10/10) and every `Set` with `0xe8` is ACKed (6/6), and 🟡 (strong) that `0x00` means **no bud worn** — not "in the case". Only as fresh as the
 * last Message Stream claim (ADR-032).
 */
enum class AncAvailability {
    /** No `Notify` yet on this connection — the controls stay enabled (the Buds' own answer remains the authority). */
    UNKNOWN,

    /** `Settable toggles` is non-zero (`0xe8` in every sample): a `Set` is accepted. */
    ALLOWED,

    /** `Settable toggles == 0x00`: the Buds refuse a `Set` (NAK `0x02`); every tap re-reads the byte first and sends a `Set` only if it is non-zero (`ai-sessions/0062` F-1). */
    NOT_ALLOWED,
    ;

    companion object {
        /** `0x00` → [NOT_ALLOWED]; any other value → [ALLOWED] (a `Notify` with a non-zero value re-enables, `ai-sessions/0048`). */
        fun fromSettableToggles(byte: Int): AncAvailability = if (byte == 0x00) NOT_ALLOWED else ALLOWED
    }
}

/**
 * What the Buds announce about themselves when a session opens (their unsolicited `GetSoftwareInfo`, DECISIONS.md ADR-034): [firmware] = the distinct
 * firmware version strings, e.g. `release_5.203` (what the Safe-Mode gate checks, ADR-042). **Since `ai-sessions/0062` (F-5):** also [entries] — each entry of
 * the announcement's field 4 with its own index — the pw_rpc channel the Buds announced ([maestroChannel]) and when the announcement arrived
 * ([announcedAtMillis]), for the Info tab. The entries' version numbers (`PROTOCOL.md` §2.2a, L-5) are not carried. **Since `ai-sessions/0082` (ADR-058):**
 * [serials] — the component serial numbers the Buds answer to the app's one `GetHardwareInfo` per Connect, with [serialsReadAtMillis]; empty until the
 * answer arrived. Serials are device identifiers (`AGENTS.md` §9): shown on the Info tab, never logged.
 */
data class DeviceInfo(
    val firmware: List<String>,
    val entries: List<FirmwareEntry> = emptyList(),
    val maestroChannel: Int? = null,
    val announcedAtMillis: Long? = null,
    val serials: List<ComponentSerial> = emptyList(),
    val serialsReadAtMillis: Long? = null,
)

/**
 * One serial number of the `GetHardwareInfo` answer's field 7 (DECISIONS.md ADR-058, `ai-sessions/0082`): its field number [index] (1, 2 or 3 in every capture —
 * 138 answers in 54 logs, always the same three strings in the same order) and the string. Which component an index is — **1 = Case, 2 = Right bud, 3 = Left
 * bud** — is the official app's own reading of the answer by position (`gaa.java:45–96` → `fwg.java:182–215`, 🟢 for its code; the strings' EC/DR/DL marks
 * agree; `PROTOCOL.md` §6, 2026-10-09); the labels are `:ui`'s.
 */
data class ComponentSerial(val index: Int, val serial: String)

/**
 * One entry of the announcement's field 4: its field number [index] (1, 2 or 3 in every capture) and its firmware string (field 2). Which component an index is
 * — 1 = Case, 2 = Left bud, 3 = Right bud — is the official app's own mapping (`PROTOCOL.md` §2.2a, 2026-10-01 `ai-sessions/0062` Update, 🟢 FACT for the
 * app's code, 🟡 that the unsolicited announcement uses the same layout); in every capture so far the three entries are identical (191 of 191).
 */
data class FirmwareEntry(val index: Int, val firmware: String)

/**
 * The app's read-only Safe Mode (ARCHITECTURE.md §8.1, DECISIONS.md ADR-042): active when the connected Buds' firmware is not in the
 * verified allowlist or their Fast Pair Model ID is not the Pixel Buds Pro 2's. [firmware]/[modelIdHex] are what was detected
 * (`null` = not seen), [reason] a short, user-readable explanation. Model IDs identify a *model*, not the user's unit.
 */
data class SafeModeState(val firmware: List<String>?, val modelIdHex: String?, val reason: String)
