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
}
