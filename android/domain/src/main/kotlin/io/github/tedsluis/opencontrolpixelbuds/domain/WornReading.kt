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
 * The "probably worn" indicator of DECISIONS.md ADR-059 (`ai-sessions/0082` item 3; the maintainer's decision in chat 2026-10-09: show it hedged, as
 * "probably"). One reading derived from state the app already holds — **no new wire traffic**:
 *
 * - the Settable byte of the Buds' last `Notify ANC state` on this connection ([AncAvailability]) and its time — ADR-049: 🟢 `00` ⇒ a `Set` is NAKed; 🟡
 *   (strong) `00` ⇒ no bud worn (56 filmed samples, `CAP-065`/`CAP-066`, 0 counter-examples); the converse is **refuted** (`CAP-064`: `e8` with in-ear
 *   detection off, and ≈ 28 s of `e8` with both buds on a table, in-ear detection on);
 * - the in-ear detection setting (`qhr` field 2): with it off the byte says nothing about wearing (`CAP-064` 10394/10600);
 * - each bud's charging state from the runtime-info stream (ADR-043 Update; 🟡 charging = in the case): a charging bud is not worn.
 *
 * The byte is not per bud — the reading never names a bud as worn. It is as old as the last `Notify` (Connect, Refresh, a noise-control tap), never live.
 */
sealed class WornReading {
    /** No `Notify` on this connection yet: "—" (screen reader: not read from the Buds yet). */
    data object NotRead : WornReading()

    /** Field 2 = 0: with in-ear detection off the Buds report `e8` with no bud worn (`CAP-064`) — the byte says nothing about wearing. */
    data object InEarDetectionOff : WornReading()

    /** Field 2 not read on this connection: whether the byte means anything is unknown. */
    data object InEarDetectionNotRead : WornReading()

    /** Both buds report charging on this connection (6.2/6.3 field 2 = 2, `CAP-062` 4845): both in the case, so neither is worn. */
    data object BothInCase : WornReading()

    /** Settable `00` (`CAP-065` 5465/5707/6019): the Buds refuse a noise-control change — in every filmed sample no bud was in an ear (🟡 strong). */
    data class NotWorn(val checkedAtMillis: Long?) : WornReading()

    /** Settable non-zero (`CAP-065` 6334): the Buds allow a change — on film at least one bud in an ear, except `CAP-064`'s ≈ 28 s (🟡). */
    data class ProbablyWorn(val checkedAtMillis: Long?) : WornReading()
}

/**
 * ADR-059 item 1, in this order: no `Notify` this connection → [WornReading.NotRead]; field 2 = 0 → [WornReading.InEarDetectionOff]; field 2 not read →
 * [WornReading.InEarDetectionNotRead]; both buds charging **on this connection** → [WornReading.BothInCase]; Settable `00` → [WornReading.NotWorn];
 * non-zero → [WornReading.ProbablyWorn]. A charging report from an earlier connection says nothing about now and is ignored (mutation M7: a bud whose flag
 * is not `true` is never "in the case").
 */
fun wornReading(
    availability: AncAvailability,
    checkedAtMillis: Long?,
    inEarDetection: SettingReading<Boolean>?,
    leftCharging: ChargingReading?,
    rightCharging: ChargingReading?,
): WornReading {
    if (availability == AncAvailability.UNKNOWN) return WornReading.NotRead
    if (inEarDetection == null) return WornReading.InEarDetectionNotRead
    if (!inEarDetection.value) return WornReading.InEarDetectionOff
    fun inCase(reading: ChargingReading?) = reading != null && reading.charging && !reading.fromEarlierSession
    if (inCase(leftCharging) && inCase(rightCharging)) return WornReading.BothInCase
    return if (availability == AncAvailability.NOT_ALLOWED) WornReading.NotWorn(checkedAtMillis) else WornReading.ProbablyWorn(checkedAtMillis)
}
