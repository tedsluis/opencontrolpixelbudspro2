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

import io.github.tedsluis.opencontrolpixelbuds.domain.AndroidLink
import io.github.tedsluis.opencontrolpixelbuds.domain.BudsResult
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * DECISIONS.md ADR-044 (maintainer, chat 2026-09-25): re-open the MAESTRO session by itself **while the app is visible and Android reports the
 * Buds connected** — (a) [LOSS_REOPEN_DELAY_MS] after a session loss (a Buds-side `DISC` with the link up: `CAP-062` 7 of 14 session ends, each at
 * a wear/dock change), (b) when Android's link comes back, (c) on resume. **One attempt per event, no loop, no timer**: each event gives at most
 * one attempt, a failed attempt is reported and never retried; the only wait is one bounded delay per loss event (as the link debounce,
 * ARCHITECTURE.md §6.0b). The user's Disconnect tap switches it off until the next Connect tap. Nothing here runs in the background: [onVisible]
 * `false` cancels a pending re-open and forgets the link state (no readings arrive while the UI is not visible).
 *
 * Agent details (`ai-sessions/0048`, open to veto): the 1.5 s delay (inside ADR-044's 1–2 s); a loss within [CHAIN_GUARD_MS] of an automatic
 * re-open does not schedule another (a Buds `DISC` → re-open → `DISC` cycle cannot become a loop); a re-open repeats the whole Connect sequence
 * (ADR-044's Consequences, including the DLCI 0x04 snapshot that re-reads the ANC availability, I-3).
 *
 * @param enabled initial state before any Connect/Disconnect tap in this process (never persisted).
 * @param sessionOpenOrOpening whether the session is Ready, Connecting or Discovering — then nothing is attempted.
 * @param reopen opens the session the way a Connect tap does, without re-enabling this policy.
 */
internal class SessionReopener(
    private val scope: CoroutineScope,
    private val clock: () -> Long,
    private val sessionOpenOrOpening: () -> Boolean,
    private val reopen: suspend (Trigger) -> BudsResult<Unit>,
    @Volatile private var enabled: Boolean = true,
) {
    enum class Trigger { AFTER_LOSS, LINK_BACK, RESUME }

    @Volatile
    private var visible = false

    @Volatile
    private var link = AndroidLink.UNKNOWN

    @Volatile
    private var pendingLossReopen: Job? = null

    @Volatile
    private var inFlight: Job? = null

    @Volatile
    private var lastAutoOpenAt: Long? = null

    /** The user's Connect tap: re-enables the policy (ADR-044 item 2). */
    fun onUserConnect() {
        enabled = true
        cancelPending()
    }

    /** The user's Disconnect tap: off until the next Connect tap (ADR-044 item 2). */
    fun onUserDisconnect() {
        enabled = false
        cancelPending()
    }

    fun onVisible(isVisible: Boolean) {
        visible = isVisible
        if (!isVisible) {
            cancelPending()
            link = AndroidLink.UNKNOWN // readings stop while not visible: the last one says nothing about "now"
        } else {
            attempt(Trigger.RESUME)
        }
    }

    fun onAndroidLink(reading: AndroidLink) {
        val cameBack = reading == AndroidLink.CONNECTED && link != AndroidLink.CONNECTED
        link = reading
        if (cameBack) attempt(Trigger.LINK_BACK)
    }

    /** The session was lost (not by the user). The link state is checked when the delay has passed, not now. */
    fun onSessionLost() {
        if (!enabled || !visible) return
        val last = lastAutoOpenAt
        if (last != null && clock() - last < CHAIN_GUARD_MS) {
            BleLogger.logConnectionEvent("Automatic re-open skipped: the session was lost %d ms after an automatic re-open".format(clock() - last))
            return
        }
        pendingLossReopen?.cancel()
        pendingLossReopen = scope.launch {
            delay(LOSS_REOPEN_DELAY_MS)
            attempt(Trigger.AFTER_LOSS)
        }
    }

    private fun attempt(trigger: Trigger) {
        if (!enabled || !visible || link != AndroidLink.CONNECTED || sessionOpenOrOpening()) return
        if (inFlight?.isActive == true) return
        inFlight = scope.launch {
            BleLogger.logConnectionEvent("Automatic re-open of the session (ADR-044, trigger: $trigger)")
            when (val result = reopen(trigger)) {
                is BudsResult.Success -> lastAutoOpenAt = clock()
                is BudsResult.Failure -> BleLogger.logConnectionEvent(
                    "Automatic re-open failed (${result.error::class.simpleName}) — not retried; the next event or a Connect tap may try again",
                )
            }
        }
    }

    private fun cancelPending() {
        pendingLossReopen?.cancel()
        pendingLossReopen = null
    }

    companion object {
        /** ADR-044 (a): 1–2 s after a Buds-side `DISC`; the middle of that range (agent detail). */
        const val LOSS_REOPEN_DELAY_MS = 1_500L

        /** A loss this soon after an automatic re-open schedules no further one (agent detail, "no loop"). */
        const val CHAIN_GUARD_MS = 10_000L
    }
}
