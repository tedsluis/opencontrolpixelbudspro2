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

import java.util.UUID

/**
 * SDP UUIDs `BluetoothDevice.createRfcommSocketToServiceRecord()` needs to
 * resolve each DLCI this app implements against. These are wire-protocol
 * literals the peer expects, not app configuration — hardcoding them is the
 * `PROJECT_RULES.md` §8 rule 22 exception, every value traced to a specific
 * capture frame:
 *
 * - [MAESTRO] (DLCI 0x02): `DECISIONS.md` ADR-018 (APK static analysis,
 *   `gbm.java:35-43`/`fzd.java:9`, logged `"Provide pigweed internal rfcomm
 *   socket"`) **and**, independently, `CAP-033-FINDINGS.md` §3's SDP browse
 *   (frame 1279), which names the same UUID's service `"MAESTRO APP"` on
 *   RFCOMM channel 1 — two independent evidentiary paths.
 * - [FAST_PAIR_MESSAGE_STREAM] (DLCI 0x04): `CAP-033-FINDINGS.md` §3's SDP
 *   browse, service name `"GFPS RFCOMM"`, RFCOMM channel 2.
 *
 * Sockets to both have been opened on real hardware (`CAP-059`–`CAP-061`, `RFCOMM channel 0x02/0x04 connected` in the debug exports).
 * DLCI 0x08 ("GSND CONTROL", `f8d1fbe4-7966-4334-8024-ff96c9330e15`, `CAP-033`) is no longer opened by the app (`DECISIONS.md` ADR-043).
 */
object BudsSdpUuids {
    val MAESTRO: UUID = UUID.fromString("25e97ff7-24ce-4c4c-8951-f764a708f7b5")
    val FAST_PAIR_MESSAGE_STREAM: UUID = UUID.fromString("df21fe2c-2515-4fdb-8886-f12c4d67927c")
}
