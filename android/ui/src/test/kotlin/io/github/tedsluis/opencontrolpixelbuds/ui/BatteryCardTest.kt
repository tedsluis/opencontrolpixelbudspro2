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

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryLevel
import io.github.tedsluis.opencontrolpixelbuds.domain.BatteryStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingReading
import io.github.tedsluis.opencontrolpixelbuds.domain.ChargingSource
import io.github.tedsluis.opencontrolpixelbuds.domain.ConnectionState
import io.github.tedsluis.opencontrolpixelbuds.domain.DeviceStatus
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionState
import io.github.tedsluis.opencontrolpixelbuds.domain.PermissionStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The battery card of the Connection screen (`ai-sessions/0057` D-7/D-8), rendered under Robolectric: "Battery unavailable" never has a bar, charging has
 * the bolt, a stale value marks the (i), and the (i) dialog shows **the same lines** the helpers produce ([budLine], [caseLine]).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BatteryCardTest {

    @get:Rule
    val compose = createComposeRule()

    private val at = 1_727_600_000_000L

    private fun show(status: BatteryStatus) = compose.setContent {
        OpenControlTheme(darkTheme = false) {
            ConnectionScreen(
                connectionState = ConnectionState.Ready,
                deviceStatus = DeviceStatus.ControlledByApp,
                androidLink = AndroidLink.CONNECTED,
                permissionState = PermissionState(PermissionStatus.GRANTED, PermissionStatus.GRANTED),
                pairingStatusText = null,
                lastConnectionError = null,
                messageStreamError = null,
                batteryStatus = status,
                batteryStatusUpdatedAt = at,
                caseBatteryError = null,
                deviceInfo = null,
                onRefreshBattery = {},
                lastLossCause = null,
                onRequestEnableBluetooth = {},
                onPair = {},
                onRequestPermissions = {},
                onOpenAppSettings = {},
                onConnect = {},
                onDisconnect = {},
            )
        }
    }

    private val progressBars = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)

    @Test
    fun `unavailable parts show the words and no bar`() {
        show(BatteryStatus(left = BatteryLevel.Known(97, isCharging = false, receivedAtMillis = at)))
        compose.onAllNodesWithText("Battery unavailable").assertCountEquals(2) // Case and Right
        compose.onAllNodes(progressBars).assertCountEquals(1) // Left only
        compose.onNodeWithText("97%").assertExists()
    }

    @Test
    fun `a charging bud has the bolt, unknown charging has none`() {
        show(
            BatteryStatus(
                left = BatteryLevel.Known(80, isCharging = true, receivedAtMillis = at),
                right = BatteryLevel.Known(90, isCharging = null, receivedAtMillis = at),
                leftCharging = ChargingReading(charging = true, atMillis = at, source = ChargingSource.RUNTIME_INFO),
            ),
        )
        compose.onAllNodesWithContentDescription("charging").assertCountEquals(1)
    }

    @Test
    fun `a current reading has a plain (i), a last-seen one marks it`() {
        show(BatteryStatus(left = BatteryLevel.Known(97, isCharging = false, receivedAtMillis = at)))
        compose.onNodeWithContentDescription("Battery: $DETAILS_DESCRIPTION").assertExists()
    }

    @Test
    fun `a last-seen Case marks the (i) as not current`() {
        show(BatteryStatus(case = BatteryLevel.Known(60, isCharging = null, isStale = true, receivedAtMillis = at)))
        compose.onNodeWithContentDescription("Battery: $DETAILS_NOT_CURRENT_DESCRIPTION").assertExists()
    }

    @Test
    fun `the (i) dialog shows exactly the helper lines with their times`() {
        val status = BatteryStatus(
            left = BatteryLevel.Known(97, isCharging = false, receivedAtMillis = at),
            case = BatteryLevel.Known(60, isCharging = null, isStale = true, receivedAtMillis = at - 60_000),
            leftCharging = ChargingReading(charging = false, atMillis = at, source = ChargingSource.MESSAGE_STREAM, fromEarlierSession = true),
        )
        show(status)
        compose.onNodeWithContentDescription("Battery: $DETAILS_NOT_CURRENT_DESCRIPTION").performClick()
        for (line in batteryDetailLines(status, at, null)) compose.onNodeWithText(line).assertExists()
        compose.onNodeWithText(budLine("Left", status.left, status.leftCharging, at)).assertExists()
        compose.onNodeWithText(caseLine(status.case, at)).assertExists()
    }
}
