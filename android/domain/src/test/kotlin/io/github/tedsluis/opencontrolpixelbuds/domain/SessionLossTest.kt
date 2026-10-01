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
 * I-7 (`ai-sessions/0048`): the loss wording follows Android's link state around the loss. Times are the `CAP-062` debug-export times
 * (milliseconds of the day are enough: only differences matter).
 */
class SessionLossTest {
    private fun t(hms: String): Long {
        val (h, m, rest) = hms.split(":")
        val (sec, ms) = rest.split(".")
        return ((h.toLong() * 60 + m.toLong()) * 60 + sec.toLong()) * 1000 + ms.toLong()
    }

    @Test
    @DisplayName("CAP-062 06:42:35: the loss (.135) came 109 ms before the link read NOT_CONNECTED (.244); the older CONNECTED is not used")
    fun `ACL drop right after the loss`() {
        val readings = listOf(LinkReading(AndroidLink.CONNECTED, t("06:42:06.841")))
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(t("06:42:35.135"), readings), "only a reading older than the loss")
        assertEquals(
            SessionLossCause.ANDROID_LINK_LOST,
            classifySessionLoss(t("06:42:35.135"), readings + LinkReading(AndroidLink.NOT_CONNECTED, t("06:42:35.244"))),
        )
    }

    @Test
    fun `a first fresh CONNECTED reading is overruled by a NOT_CONNECTED within 1 s (re-render, CAP-062 06h43m28)`() {
        val loss = t("06:43:28.183")
        val fresh = listOf(LinkReading(AndroidLink.CONNECTED, loss + 20))
        assertEquals(SessionLossCause.BUDS_CLOSED_CHANNEL, classifySessionLoss(loss, fresh))
        assertEquals(SessionLossCause.ANDROID_LINK_LOST, classifySessionLoss(loss, fresh + LinkReading(AndroidLink.NOT_CONNECTED, t("06:43:28.432"))))
    }

    @Test
    fun `an Android-settings disconnect turned the link off 1_3 s before the loss (CAP-062 06h41m51)`() {
        val readings = listOf(LinkReading(AndroidLink.NOT_CONNECTED, t("06:41:51.600")), LinkReading(AndroidLink.NOT_CONNECTED, t("06:41:52.950")))
        assertEquals(SessionLossCause.ANDROID_LINK_LOST, classifySessionLoss(t("06:41:52.899"), readings))
    }

    @Test
    @DisplayName("a Buds-side DISC with the link up (CAP-062 frame 5313, 06:43:40.671): Android still connected right after")
    fun `Buds closed the channel`() {
        val loss = t("06:43:40.671")
        val readings = listOf(LinkReading(AndroidLink.CONNECTED, t("06:43:34.004")), LinkReading(AndroidLink.CONNECTED, loss + 15))
        assertEquals(SessionLossCause.BUDS_CLOSED_CHANNEL, classifySessionLoss(loss, readings))
    }

    @Test
    @DisplayName("both buds into the case (CAP-062 06:46:04.730): Buds DISC first, the ACL drop 3.0 s later does not change the cause")
    fun `the later ACL drop of a re-dock stays a Buds-side close`() {
        val loss = t("06:46:04.730")
        val readings = listOf(LinkReading(AndroidLink.CONNECTED, loss + 30), LinkReading(AndroidLink.NOT_CONNECTED, t("06:46:07.745")))
        assertEquals(SessionLossCause.BUDS_CLOSED_CHANNEL, classifySessionLoss(loss, readings))
    }

    @Test
    fun `no reading near the loss (app not on screen) or an unknown link claims nothing`() {
        val loss = 100_000L
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, emptyList()))
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, listOf(LinkReading(AndroidLink.CONNECTED, loss + 60_000))), "minutes later")
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, listOf(LinkReading(AndroidLink.UNKNOWN, loss + 10))))
    }

    // ---- I-2 (ai-sessions/0054): a loss while the app was not on screen is decided by the first reading on return -------------------------------
    // CAP-063-debug-export.log: line 526 16:13:07.841 CONNECTED (the last reading logged before, readings are logged on change), 586 16:15:21.408
    // "Android link observer stopped" (the app left), 589 16:15:22.008 "Session lost", 592 16:15:25.013 observer started, 593 16:15:25.062 NOT_CONNECTED.

    @Test
    @DisplayName("CAP-063 16:15:22.008: loss while hidden, first reading on return NOT_CONNECTED 3.05 s later -> link down on return")
    fun `CAP-063 background loss is the link down, read on return`() {
        val loss = t("16:15:22.008")
        val readings = listOf(LinkReading(AndroidLink.CONNECTED, t("16:13:07.841")), LinkReading(AndroidLink.NOT_CONNECTED, t("16:15:25.062")))
        assertEquals(SessionLossCause.ANDROID_LINK_DOWN_ON_RETURN, classifySessionLoss(loss, readings, lossWhileHidden = true))
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, readings), "while visible the 0048 windows still apply (3.05 s is outside)")
    }

    @Test
    fun `a CONNECTED first reading on return is the link up on return, not a claim about who closed the channel`() {
        val loss = t("16:15:22.008")
        val readings = listOf(LinkReading(AndroidLink.CONNECTED, t("16:15:25.062")), LinkReading(AndroidLink.NOT_CONNECTED, t("16:15:40.000")))
        assertEquals(SessionLossCause.ANDROID_LINK_UP_ON_RETURN, classifySessionLoss(loss, readings, lossWhileHidden = true), "the first one decides")
    }

    @Test
    fun `a reading older than the loss is never used, also not on return`() {
        val loss = t("16:15:22.008")
        val older = listOf(LinkReading(AndroidLink.CONNECTED, t("16:13:07.841")), LinkReading(AndroidLink.NOT_CONNECTED, t("16:15:19.500")))
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, older, lossWhileHidden = true), "nothing after the loss yet")
        assertEquals(
            SessionLossCause.ANDROID_LINK_UP_ON_RETURN,
            classifySessionLoss(loss, older + LinkReading(AndroidLink.CONNECTED, t("16:16:30.000")), lossWhileHidden = true),
            "the NOT_CONNECTED 2.5 s before the loss is outside rule 1 and older than the loss",
        )
    }

    @Test
    fun `on return an unknown link claims nothing, and a reading within the 0048 windows keeps its 0048 cause`() {
        val loss = 100_000L
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, listOf(LinkReading(AndroidLink.UNKNOWN, loss + 60_000)), lossWhileHidden = true))
        // CAP-063 #9 (export 641 observer stopped 16:16:04.440, 644 loss 16:16:05.580, 648 CONNECTED 16:16:06.182): hidden, but the reading came 602 ms
        // after the loss — the 0048 rule 2 keeps "the Buds closed the channel" (export 649, FINDINGS §6).
        val hiddenLoss = t("16:16:05.580")
        assertEquals(
            SessionLossCause.BUDS_CLOSED_CHANNEL,
            classifySessionLoss(hiddenLoss, listOf(LinkReading(AndroidLink.CONNECTED, t("16:16:06.182"))), lossWhileHidden = true),
        )
    }

    // ---- F-3 (ai-sessions/0064): a Bluetooth-off loss --------------------------------------------------------------------------------------------
    // CAP-066 debug export E1 (CAP-066-opencontrol-debug-20261001-162556.txt) lines 279-283 and E2 (…-163412.txt) lines 164-168, local time; the adapter
    // states from the system logs (UTC = local − 2 h): CAP-066-System-log-d55db7f4e1c8.txt 38635 (14:13:00.837, 12 → 13 TURNING_OFF), 38754
    // (14:13:01.361, 13 → 10 OFF); CAP-066-System-log-7f4bb1d5ea2c.txt 42742 (14:32:16.820, 12 → 13), 42895 (14:32:17.651, 13 → 10).

    @Test
    @DisplayName("CAP-066 E1 16:13:01.386: adapter TURNING_OFF 0.55 s before the loss -> Bluetooth was switched off (was: undetermined)")
    fun `CAP-066 E1 Bluetooth off`() {
        val loss = t("16:13:01.386") // E1 281 "Session lost: channel 0x02 closed …"
        val readings = listOf(LinkReading(AndroidLink.UNKNOWN, t("16:13:00.971"))) // E1 279 "Android link: CONNECTED -> UNKNOWN"
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, readings), "E1 282, the CAP-066 verdict without an adapter reading")
        val adapter = listOf(AdapterOffReading(t("16:13:00.837")), AdapterOffReading(t("16:13:01.361")))
        assertEquals(SessionLossCause.BLUETOOTH_OFF, classifySessionLoss(loss, readings, adapterOff = adapter))
        assertEquals(SessionLossCause.BLUETOOTH_OFF, classifySessionLoss(loss, readings, adapterOff = adapter.take(1)), "TURNING_OFF alone is enough")
    }

    @Test
    @DisplayName("CAP-066 E2 16:32:17.632: adapter TURNING_OFF 0.81 s before, OFF 19 ms after -> Bluetooth was switched off")
    fun `CAP-066 E2 Bluetooth off`() {
        val loss = t("16:32:17.632") // E2 166
        val readings = listOf(LinkReading(AndroidLink.UNKNOWN, t("16:32:17.010"))) // E2 164
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, readings), "E2 167")
        assertEquals(
            SessionLossCause.BLUETOOTH_OFF,
            classifySessionLoss(loss, readings, adapterOff = listOf(AdapterOffReading(t("16:32:16.820")), AdapterOffReading(t("16:32:17.651")))),
        )
        assertEquals(SessionLossCause.BLUETOOTH_OFF, classifySessionLoss(loss, readings, adapterOff = listOf(AdapterOffReading(t("16:32:17.651")))), "OFF after")
    }

    @Test
    fun `an adapter-off reading outside 2 s before to 1 s after the loss claims nothing, and it wins over a lost link inside the window`() {
        val loss = 100_000L
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, emptyList(), adapterOff = listOf(AdapterOffReading(loss - 2_001))))
        assertEquals(SessionLossCause.UNDETERMINED, classifySessionLoss(loss, emptyList(), adapterOff = listOf(AdapterOffReading(loss + 1_001))))
        assertEquals(SessionLossCause.BLUETOOTH_OFF, classifySessionLoss(loss, emptyList(), adapterOff = listOf(AdapterOffReading(loss - 2_000))))
        assertEquals(
            SessionLossCause.BLUETOOTH_OFF,
            classifySessionLoss(loss, listOf(LinkReading(AndroidLink.NOT_CONNECTED, loss + 100)), adapterOff = listOf(AdapterOffReading(loss - 500))),
            "switching Bluetooth off also takes the link down: the adapter reading names the cause",
        )
        assertEquals(
            SessionLossCause.BUDS_CLOSED_CHANNEL,
            classifySessionLoss(loss, listOf(LinkReading(AndroidLink.CONNECTED, loss + 20)), adapterOff = listOf(AdapterOffReading(loss - 60_000))),
            "an old Bluetooth-off (a minute earlier) changes nothing",
        )
    }
}
