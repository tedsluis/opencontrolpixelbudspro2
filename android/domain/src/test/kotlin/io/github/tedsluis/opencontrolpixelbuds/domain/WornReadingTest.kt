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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * The "probably worn" reading of DECISIONS.md ADR-059 (`ai-sessions/0082` item 3) — one test per reading, each with the decoded values of a real frame
 * (the frames themselves are decoded in `:data`'s codec tests; here their decoded values, with the frame number): the Settable byte as [AncAvailability]
 * (`08 13 00 04 01 e8 <settable> <mode>`), `qhr` field 2 as a [SettingReading], the runtime-info charging flags as [ChargingReading]s.
 */
class WornReadingTest {

    private val at = 1_727_600_000_000L
    private val inEarOn = SettingReading(true, at - 5_000) // `CAP-064` 1260: `ReadSetting` answer `4:{2:1}`
    private val inEarOff = SettingReading(false, at - 5_000, changedByApp = true) // `CAP-064` 9922: `WriteSetting 4:{2:0}`, OK
    private val notCharging = ChargingReading(false, at - 1_000, ChargingSource.RUNTIME_INFO) // `CAP-062` 2782: 6.2.2 = 1, 6.3.2 = 1
    private val charging = ChargingReading(true, at - 1_000, ChargingSource.RUNTIME_INFO) // `CAP-062` 4845: 6.2.2 = 2, 6.3.2 = 2

    @Test
    @DisplayName("no Notify on this connection -> not read (the dash), whatever the other inputs say")
    fun `not read before the first Notify`() {
        assertEquals(WornReading.NotRead, wornReading(AncAvailability.UNKNOWN, null, inEarOn, notCharging, notCharging))
        assertEquals(WornReading.NotRead, wornReading(AncAvailability.UNKNOWN, null, inEarOff, charging, charging))
    }

    @Test
    @DisplayName("CAP-064 10394 (e8, in-ear detection off, both buds on the table): the byte says nothing -> unknown, in-ear detection is off")
    fun `in-ear detection off makes the byte meaningless`() {
        assertEquals(WornReading.InEarDetectionOff, wornReading(AncAvailability.ALLOWED, at, inEarOff, notCharging, notCharging))
        assertEquals(WornReading.InEarDetectionOff, wornReading(AncAvailability.NOT_ALLOWED, at, inEarOff, notCharging, notCharging), "a 00 is not 'not worn' either")
    }

    @Test
    @DisplayName("field 2 not read on this connection -> unknown, the setting was not read")
    fun `in-ear detection not read`() {
        assertEquals(WornReading.InEarDetectionNotRead, wornReading(AncAvailability.ALLOWED, at, null, notCharging, notCharging))
        assertEquals(WornReading.InEarDetectionNotRead, wornReading(AncAvailability.NOT_ALLOWED, at, null, notCharging, notCharging))
    }

    @Test
    @DisplayName("CAP-062 4845 (both buds charging, this connection) -> both buds in the case, whatever the byte says")
    fun `both buds charging means both in the case`() {
        assertEquals(WornReading.BothInCase, wornReading(AncAvailability.NOT_ALLOWED, at, inEarOn, charging, charging))
        assertEquals(WornReading.BothInCase, wornReading(AncAvailability.ALLOWED, at, inEarOn, charging, charging))
    }

    @Test
    @DisplayName("M7 guard: one bud charging (CAP-062 3760: Right only), a flag from the last connection, or no flag is never 'in the case'")
    fun `one charging bud or a stale flag is not both in the case`() {
        assertEquals(WornReading.ProbablyWorn(at), wornReading(AncAvailability.ALLOWED, at, inEarOn, notCharging, charging))
        assertEquals(WornReading.ProbablyWorn(at), wornReading(AncAvailability.ALLOWED, at, inEarOn, charging.copy(fromEarlierSession = true), charging))
        assertEquals(WornReading.ProbablyWorn(at), wornReading(AncAvailability.ALLOWED, at, inEarOn, null, charging))
        assertEquals(WornReading.NotWorn(at), wornReading(AncAvailability.NOT_ALLOWED, at, inEarOn, null, null))
    }

    @Test
    @DisplayName("CAP-065 5465 (00, both buds loose on the table, in-ear detection on) -> not worn, with the report's time")
    fun `Settable 00 is not worn`() {
        assertEquals(WornReading.NotWorn(at), wornReading(AncAvailability.NOT_ALLOWED, at, inEarOn, notCharging, notCharging))
        assertEquals(WornReading.NotWorn(null), wornReading(AncAvailability.NOT_ALLOWED, null, inEarOn, notCharging, notCharging))
    }

    @Test
    @DisplayName("CAP-065 6334 (e8, both worn, in-ear detection on) -> probably worn, with the report's time — never a bud named")
    fun `Settable non-zero is probably worn`() {
        assertEquals(WornReading.ProbablyWorn(at), wornReading(AncAvailability.ALLOWED, at, inEarOn, notCharging, notCharging))
    }
}
