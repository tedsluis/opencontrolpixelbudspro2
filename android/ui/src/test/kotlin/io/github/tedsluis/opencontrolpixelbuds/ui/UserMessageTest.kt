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
package io.github.tedsluis.opencontrolpixelbuds.ui

import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsError
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingSource
import io.github.tedsluis.opencontrolpixelbuds.domain.SessionLossCause
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Every [BudsError] has its own sentence (AGENTS.md §8), pinned here as literal text (`ai-sessions/0069`: `ai-sessions/0068` A68-APP-06 found two of the
 * error types pinned, A68-GOV-03 a "Something went wrong", A68-APP-07 causes stated as fact). The texts marked "0069" are the maintainer's wording of
 * 2026-10-03: what was observed first, then the possible cause.
 */
class UserMessageTest {

    @Test
    fun `each error type reads as its own sentence`() {
        val expected: List<Pair<BudsError, String>> = listOf(
            BudsError.ConnectionLost to "Connection lost.",
            BudsError.Timeout to "The Buds didn't respond in time.",
            // 0069: no longer "another app took the Buds' channel" as a fact.
            BudsError.AnswerCutOff(0x04, "x") to
                "The channel was closed before the Buds' answer arrived (possibly by another app using it). Tap Refresh to see the current mode.",
            BudsError.MalformedFrame(byteArrayOf(1)) to "Received an unexpected response from the Buds.",
            BudsError.UnsupportedFirmware to
                "Safe Mode: nothing was sent — the Buds' firmware or model isn't one this app was verified against (read-only).",
            BudsError.PermissionDenied to "Bluetooth permission is required.",
            BudsError.NotPaired to "No paired Pixel Buds found. Pair them first (Pair a device, or Android's Bluetooth settings).",
            BudsError.CommandRejected(2, "not allowed") to "The Buds refused the command (not allowed).",
            BudsError.AncNotAllowed to "The Buds don't allow changing noise control right now (usually because no bud is in an ear).",
            BudsError.NoNewBatteryReading to "No new battery reading from the Buds — try again.",
            BudsError.AncModeListTooShort to "At least two modes must stay selected.",
            BudsError.AncModeListNotRead to "The list of modes has not been read from the Buds on this connection, so nothing was sent.",
            BudsError.SessionOpening to "The app's channel is being reopened — try again in a moment.",
            BudsError.UnreadableAnswer to "The Buds answered with a value this app cannot read.",
            // 0069: its own error, not Unknown("EQ field not readable").
            BudsError.SettingNotReadable to "This setting cannot be read by this app.",
            // 0069: the Fast Pair channel is the only one another app is known to contend for (ADR-032) …
            BudsError.ChannelUnavailable(0x04, "x") to
                "Couldn't open the Fast Pair channel. Possible causes: the Buds are out of reach or in the closed case, or another app on this phone " +
                "(for example Google Play services' Fast Pair) is using the channel. Try again in a few seconds.",
            // … so every other channel names no other app (the published 1.0.0 known issue).
            BudsError.ChannelUnavailable(0x02, "x") to
                "Couldn't open the app's channel to the Buds. Possible causes: the Buds are out of reach or in the closed case. Open the case and try again.",
            BudsError.MaestroChannelUnknown(null) to "The Buds didn't announce their control channel in time, so nothing was sent.",
            BudsError.MaestroChannelUnknown(23) to
                "The Buds announced control channel 23, which this app has no known address for, so nothing was sent.",
            BudsError.MaestroRejected("UNKNOWN") to "The Buds rejected the request (UNKNOWN).",
            // 0069 (A68-GOV-03): never "Something went wrong".
            BudsError.Unknown(IllegalStateException("boom")) to "Unexpected error: boom",
            BudsError.Unknown(IllegalStateException()) to "Unexpected error: IllegalStateException",
        )
        for ((error, text) in expected) assertEquals(typeName(error), text, error.userMessage())
        // Every type of the sealed class is in the table above (ChannelLost has its own test below): [typeName] has no `else`, so a new error type does
        // not compile until it is named there — and then this comparison fails until it has a sentence here.
        val covered = expected.map { typeName(it.first) }.toSet() + "ChannelLost"
        assertEquals(ALL_TYPE_NAMES, covered)
    }

    private fun typeName(error: BudsError): String = when (error) {
        BudsError.ConnectionLost -> "ConnectionLost"
        is BudsError.ChannelUnavailable -> "ChannelUnavailable"
        is BudsError.ChannelLost -> "ChannelLost"
        is BudsError.AnswerCutOff -> "AnswerCutOff"
        BudsError.Timeout -> "Timeout"
        is BudsError.MalformedFrame -> "MalformedFrame"
        BudsError.UnsupportedFirmware -> "UnsupportedFirmware"
        BudsError.PermissionDenied -> "PermissionDenied"
        BudsError.NotPaired -> "NotPaired"
        is BudsError.CommandRejected -> "CommandRejected"
        BudsError.AncNotAllowed -> "AncNotAllowed"
        BudsError.NoNewBatteryReading -> "NoNewBatteryReading"
        BudsError.AncModeListTooShort -> "AncModeListTooShort"
        BudsError.AncModeListNotRead -> "AncModeListNotRead"
        BudsError.SessionOpening -> "SessionOpening"
        BudsError.UnreadableAnswer -> "UnreadableAnswer"
        BudsError.SettingNotReadable -> "SettingNotReadable"
        is BudsError.MaestroChannelUnknown -> "MaestroChannelUnknown"
        is BudsError.MaestroRejected -> "MaestroRejected"
        is BudsError.Unknown -> "Unknown"
    }

    private companion object {
        val ALL_TYPE_NAMES = setOf(
            "ConnectionLost", "ChannelUnavailable", "ChannelLost", "AnswerCutOff", "Timeout", "MalformedFrame", "UnsupportedFirmware", "PermissionDenied",
            "NotPaired", "CommandRejected", "AncNotAllowed", "NoNewBatteryReading", "AncModeListTooShort", "AncModeListNotRead", "SessionOpening",
            "UnreadableAnswer", "SettingNotReadable", "MaestroChannelUnknown", "MaestroRejected", "Unknown",
        )
    }

    @Test
    fun `a failed tile tap always has a toast text - the app's own sentence for that error`() {
        // A68-APP-09: only AncNotAllowed and AnswerCutOff used to produce a toast.
        assertEquals("The Buds don't allow changing noise control right now (usually because no bud is in an ear).", ancTileFailureText(BudsError.AncNotAllowed))
        assertEquals("The Buds didn't respond in time.", ancTileFailureText(BudsError.Timeout))
        assertEquals(
            "Safe Mode: nothing was sent — the Buds' firmware or model isn't one this app was verified against (read-only).",
            ancTileFailureText(BudsError.UnsupportedFirmware),
        )
        assertEquals("The Buds refused the command (redundant device action).", ancTileFailureText(BudsError.CommandRejected(4, "redundant device action")))
    }

    @Test
    fun `a lost channel says what was observed for each cause`() {
        val lost = BudsError.ChannelLost(0x02, "IOException: bt socket closed, read return: -1")
        assertEquals(
            // 0069: observed first ("was closed while Android still shows …"), then what the Buds are known to do.
            "The app's channel was closed while Android still shows the Buds connected — the Buds do this when a bud goes in or out of the case or an " +
                "ear. The app reopens its channel by itself while it is on screen, or tap Connect.",
            lost.userMessage(SessionLossCause.BUDS_CLOSED_CHANNEL),
        )
        assertEquals(
            "Android no longer shows the Buds connected to this phone — after a disconnect in Android's own Bluetooth settings, with both buds " +
                "in the case, or out of range. Tap Connect to reconnect.",
            lost.userMessage(SessionLossCause.ANDROID_LINK_LOST),
        )
        assertEquals("The Maestro channel (equalizer) was closed. Tap Connect to reconnect.", lost.userMessage(SessionLossCause.UNDETERMINED))
        assertEquals("The Maestro channel (equalizer) was closed. Tap Connect to reconnect.", lost.userMessage(null))
        for (cause in SessionLossCause.entries) assertFalse(cause.name, lost.userMessage(cause).contains("another app"))
    }

    @Test
    fun `a bud that is not charging is not said to be out of the case`() {
        // 0069 (A68-APP-07): "charging" is what the Buds report; where the bud is, they do not say.
        val at = 1_727_600_000_000L
        val level = BatteryLevel.Known(97, isCharging = false, receivedAtMillis = at)
        val time = formatUpdatedAt(at)
        assertEquals(
            "Left: 97% (updated $time) — not charging ($time)",
            budLine("Left", level, ChargingReading(charging = false, atMillis = at, source = ChargingSource.MESSAGE_STREAM)),
        )
        assertEquals(
            "Left: 97% (updated $time) — charging in the case ($time, last connection)",
            budLine("Left", level, ChargingReading(charging = true, atMillis = at, source = ChargingSource.RUNTIME_INFO, fromEarlierSession = true)),
        )
    }
}
