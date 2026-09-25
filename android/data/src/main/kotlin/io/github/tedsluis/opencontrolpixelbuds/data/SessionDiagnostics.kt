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
package io.github.tedsluis.opencontrolpixelbuds.data

import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause

/**
 * The always-on, payload-free, address-free log lines that say **why a session ended** (`ai-sessions/0042`). In the first hardware
 * run three sessions ended and the app log said nothing before the state change: one was the user's own *Disconnect* tap
 * (17:19:45.72, the only trace was the HCI log's phone-sent DISC), one the user tapping the Buds in Android's Bluetooth panel,
 * one the Buds closing both RFCOMM channels — indistinguishable in the app's log. These lines make them distinguishable.
 */
object SessionDiagnostics {

    /** A session ended because a channel was lost (peer/stack closed it). [lastInboundChannel] `null` = nothing arrived yet. */
    fun lossLine(channelId: Int, detail: String?, lastInboundChannel: Int?, lastInboundAgeMillis: Long?): String {
        val last = if (lastInboundChannel == null || lastInboundAgeMillis == null) {
            "no frame had arrived in this session"
        } else {
            "last inbound frame was on channel 0x%02x, %d ms earlier".format(lastInboundChannel, lastInboundAgeMillis)
        }
        return "Session lost: channel 0x%02x closed (%s); %s; not a user disconnect".format(channelId, detail ?: "no detail", last)
    }

    /** What Android's link state says about the last loss (`ai-sessions/0048` I-7): the socket detail alone cannot tell (`CAP-062`, 11 of 11 identical). */
    fun lossCauseLine(cause: SessionLossCause): String = "Session loss cause: " + when (cause) {
        SessionLossCause.ANDROID_LINK_LOST -> "Android's link to the Buds went down around the loss"
        SessionLossCause.BUDS_CLOSED_CHANNEL -> "Android still showed the Buds connected right after the loss — the Buds closed the channel"
        SessionLossCause.UNDETERMINED -> "undetermined (no reading of Android's link close to the loss)"
    }

    /** A session ended because the user tapped *Disconnect* — [stateName] is the session state at that moment. */
    fun userDisconnectLine(stateName: String): String = "Session ended by the user's Disconnect tap (was $stateName)"
}
