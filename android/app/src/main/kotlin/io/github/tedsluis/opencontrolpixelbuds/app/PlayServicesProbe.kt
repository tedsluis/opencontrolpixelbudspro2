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
package io.github.tedsluis.opencontrolpixelbuds.app

import android.content.pm.PackageManager
import io.github.tedsluis.opencontrolpixelbuds.domain.PlayServicesPresence
import io.github.tedsluis.opencontrolpixelbuds.hardware.BleLogger

/**
 * DECISIONS.md ADR-063 (`ai-sessions/0089`; the maintainer's choice in chat 2026-10-13): asks Android's package manager whether Google Play services is installed
 * for this user and enabled — the Settings card's information line, nothing more. Nothing of Play services is imported, bound, started or read; no permission
 * and no network (AGENTS.md §1's dated note of 2026-10-13). The query needs the manifest's `<queries>` entry for this one package (package visibility,
 * Android 11+); without it every answer would be "not found".
 */
internal object PlayServicesProbe {
    /** The package name Android is asked about — a name only (ADR-063 item 1). */
    private const val PLAY_SERVICES_PACKAGE = "com.google.android.gms"

    /** Found (enabled), found but disabled, not found for this user — or `null` when the package manager failed (ADR-063 item 4: then no line, never a guess). */
    fun presence(packageManager: PackageManager): PlayServicesPresence? = try {
        val info = packageManager.getApplicationInfo(PLAY_SERVICES_PACKAGE, PackageManager.ApplicationInfoFlags.of(0))
        if (info.enabled) PlayServicesPresence.FOUND else PlayServicesPresence.FOUND_DISABLED
    } catch (e: PackageManager.NameNotFoundException) {
        PlayServicesPresence.NOT_FOUND
    } catch (e: RuntimeException) {
        // AGENTS.md §8: not swallowed — logged (payload-free), and the card shows no line.
        BleLogger.logConnectionEvent("Google Play services presence not determined: ${e.javaClass.simpleName} (ADR-063)")
        null
    }

    /** The always-on log line when the answer changes (no identifier: a package's presence only). */
    fun logLine(presence: PlayServicesPresence?): String = "Google Play services on this phone: " + when (presence) {
        PlayServicesPresence.FOUND -> "found"
        PlayServicesPresence.FOUND_DISABLED -> "found but disabled"
        PlayServicesPresence.NOT_FOUND -> "not found"
        null -> "not determined"
    } + " (ADR-063, information only)"
}
