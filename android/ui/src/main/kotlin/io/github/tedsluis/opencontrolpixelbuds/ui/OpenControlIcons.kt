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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The app's own icons (`ai-sessions/0057`, the maintainer's decision D-5, 2026-09-29): Kotlin `ImageVector`s, no icon dependency (`material-icons-extended`
 * was rejected — very large, and the app ships unminified), no XML drawable, no Google-owned artwork or "Pixel Buds" branding (AGENTS.md §12).
 *
 * Unless noted, the path data is the SVG `d` string of a **Material Symbols** icon (Outlined, weight 400, 24 px), © Google, licensed under the **Apache License,
 * Version 2.0** (https://github.com/google/material-design-icons/blob/master/LICENSE), fetched 2026-09-29 from
 * `https://raw.githubusercontent.com/google/material-design-icons/master/symbols/web/<name>/materialsymbolsoutlined/<name>_24px.svg`. Those SVGs use the viewBox
 * `0 -960 960 960`, so every path is drawn in a group moved down by 960. Filled black like the core icons: `Icon` tints them with the content colour, so
 * they follow the light and dark themes.
 */
object OpenControlIcons {
    /** Material Symbols `earbuds` (Apache-2.0). */
    val Earbud: ImageVector by lazy {
        symbol(
            "Earbud",
            "M320-120q-83 0-141.5-58.5T120-320v-392q0-54 33-91t87-37q54 0 87 33t33 87q0 51-34.5 85.5T240-600h-40v280q0 50 35 85t85 35q50 0 85-35t35-85v-320q0-83 " +
                "58.5-141.5T640-840q83 0 141.5 58.5T840-640v400q0 51-38.5 85.5T712-120q-51 0-81.5-34.5T600-240q0-51 34.5-85.5T720-360h40v-280q0-50-35-85t-85-35q-50 " +
                "0-85 35t-35 85v320q0 83-58.5 141.5T320-120ZM200-680h40q17 0 28.5-11.5T280-720q0-17-11.5-28.5T240-760q-17 0-28.5 11.5T200-720v40Zm520 480q17 0 " +
                "28.5-11.5T760-240v-40h-40q-17 0-28.5 11.5T680-240q0 17 11.5 28.5T720-200Zm0-40ZM240-720Z",
        )
    }

    /**
     * The charging case — **drawn for this project** (no Material Symbol shows an earbud case): a rounded case outline with the lid's seam across it, even-odd
     * filled, in the same 960-unit grid as the symbols.
     */
    val Case: ImageVector by lazy {
        symbol(
            "Case",
            // Outer outline (x 120…840, y −720…−240, corner radius 160).
            // Path data needs no space between "Z" and "M": lint's TextConcatSpace (new with AGP 9) does not apply (`ai-sessions/0078`).
            //noinspection TextConcatSpace
            "M280-240q-66 0-113-47t-47-113v-160q0-66 47-113t113-47h400q66 0 113 47t47 113v160q0 66-47 113t-113 47H280Z" +
                // Inner cut-out (x 200…760, y −640…−320, corner radius 80) — even-odd makes it a hole.
                "M280-320h400q33 0 56.5-23.5T760-400v-160q0-33-23.5-56.5T680-640H280q-33 0-56.5 23.5T200-560v160q0 33 23.5 56.5T280-320Z" +
                // The lid's seam, inside the hole (filled again under even-odd).
                "M200-500h560v40H200Z",
            fillType = PathFillType.EvenOdd,
        )
    }

    /** Material Symbols `bolt` (Apache-2.0) — "charging". */
    val Charging: ImageVector by lazy {
        symbol("Charging", "m422-232 207-248H469l29-227-185 267h139l-30 208ZM320-80l40-280H160l360-520h80l-40 320h240L400-80h-80Zm151-390Z")
    }

    /** Material Symbols `info` (Apache-2.0) — the (i) that opens a card's details (D-7). */
    val Info: ImageVector by lazy {
        symbol(
            "Info",
            "M440-280h80v-240h-80v240Zm40-320q17 0 28.5-11.5T520-640q0-17-11.5-28.5T480-680q-17 0-28.5 11.5T440-640q0 17 11.5 28.5T480-600Zm0 520q-83 " +
                "0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 " +
                "156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z",
        )
    }

    /** Material Symbols `bluetooth` (Apache-2.0) — the Connection tab. */
    val Connection: ImageVector by lazy {
        symbol("Connection", "M440-80v-304L256-200l-56-56 224-224-224-224 56-56 184 184v-304h40l228 228-172 172 172 172L480-80h-40Zm80-496 76-76-76-74v150Zm0 342 76-74-76-76v150Z")
    }

    /** Material Symbols `noise_control_on` (Apache-2.0) — the ANC tab. */
    val NoiseControl: ImageVector by lazy {
        symbol(
            "NoiseControl",
            "M520-240q51 0 85.5-35t34.5-85h-80q0 17-11.5 28.5T520-320q-13 0-23-7.5T482-348q-6-17-14.5-33.5T445-412l-54-54q-15-15-23-34.5t-8-39.5q0-42 29-71t71-29q36 " +
                "0 63.5 22.5T558-560h81q-8-69-59-114.5T460-720q-75 0-127.5 52.5T280-540q0 37 14 70.5t40 59.5l55 55q7 7 10.5 15.5T406-322q13 36 44.5 59t69.5 23Zm20-160q26 " +
                "0 43-17.5t17-42.5q0-26-17-43t-43-17q-25 0-42.5 17T480-460q0 25 17.5 42.5T540-400ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 " +
                "127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93q-134 " +
                "0-227 93t-93 227q0 134 93 227t227 93Z",
        )
    }

    /** Material Symbols `equalizer` (Apache-2.0) — the Sound tab. */
    val Sound: ImageVector by lazy {
        symbol("Sound", "M160-160v-320h160v320H160Zm240 0v-640h160v640H400Zm240 0v-440h160v440H640Z")
    }

    /** Material Symbols `touch_app` (Apache-2.0) — the Controls tab. */
    val Controls: ImageVector by lazy {
        symbol(
            "Controls",
            "M419-80q-28 0-52.5-12T325-126L107-403l19-20q20-21 48-25t52 11l74 45v-328q0-17 11.5-28.5T340-760q17 0 29 11.5t12 28.5v472l-97-60 104 133q6 7 14 11t17 " +
                "4h221q33 0 56.5-23.5T720-240v-160q0-17-11.5-28.5T680-440H461v-80h219q50 0 85 35t35 85v160q0 66-47 113T640-80H419ZM167-620q-13-22-20-47.5t-7-52.5q0-83 " +
                "58.5-141.5T340-920q83 0 141.5 58.5T540-720q0 27-7 52.5T513-620l-69-40q8-14 12-28.5t4-31.5q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 17 4 31.5t12 28.5l-69 " +
                "40Zm335 280Z",
        )
    }

    /** Material Symbols `notifications_active` (Apache-2.0) — the Find tab (a ringing bell). */
    val Find: ImageVector by lazy {
        symbol(
            "Find",
            "M80-560q0-100 44.5-183.5T244-882l47 64q-60 44-95.5 111T160-560H80Zm720 0q0-80-35.5-147T669-818l47-64q75 55 119.5 138.5T880-560h-80ZM160-200v-80h80v-280q0-83 " +
                "50-147.5T420-792v-28q0-25 17.5-42.5T480-880q25 0 42.5 17.5T540-820v28q80 20 130 84.5T720-560v280h80v80H160Zm320-300Zm0 420q-33 0-56.5-23.5T400-160h160q0 " +
                "33-23.5 56.5T480-80ZM320-280h320v-280q0-66-47-113t-113-47q-66 0-113 47t-47 113v280Z",
        )
    }

    /** One 24 dp icon from a path in the symbols' 960-unit grid (viewBox `0 -960 960 960`). */
    private fun symbol(name: String, pathData: String, fillType: PathFillType = PathFillType.NonZero): ImageVector =
        ImageVector.Builder(name = "OpenControl.$name", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 960f, viewportHeight = 960f)
            .addGroup(translationY = 960f)
            .addPath(pathData = PathParser().parsePathString(pathData).toNodes(), pathFillType = fillType, fill = SolidColor(Color.Black))
            .clearGroup()
            .build()
}
