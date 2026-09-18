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
package io.github.tedsluis.opencontrolpixelbuds.data.codec

import io.github.tedsluis.opencontrolpixelbuds.domain.EqBandGains

/**
 * DLCI 0x02 EQ settings-write envelope (PROTOCOL.md §4.2/§4.5's shared
 * preamble, DECISIONS.md ADR-013/ADR-016/ADR-020). Byte layout re-derived
 * this session directly from `CAP-015` frames 2111/2165/2227 (see
 * `EqFrameEncoderTest`/`EqFrameDecoderTest` for the fixtures):
 *
 * ```
 * [0x03 0x10 <correlationByte> <10-byte constant tail>]   -- 13-byte prefix, PROTOCOL.md §4.5 intro
 * [0x2a <len>]                                            -- field 5 (outer wrapper), wiretype 2
 * [0x22 <len>]                                            -- field 4 (inner wrapper), wiretype 2
 * [<varint tag: field 16 or 18, wiretype 2> <len=25>]     -- "live"/"save" outer field, PROTOCOL.md §4.2
 * [(tag:1 wiretype5 + float32LE:4)] x5                    -- fields 1..5 = Low bass/Bass/Mid/Treble/Upper treble
 * ```
 *
 * `persist == false` (field 16) is ADR-020's documented practical default for
 * a live/preview-style write. `persist == true` (field 18) mirrors CAP-015's
 * observed "fires shortly after field 16, save-shaped" frames.
 *
 * // TODO(verify): field 16 vs. 18's exact semantics ("preview" vs.
 * // "slider-release" vs. "Save button") remain 🟡 HYPOTHESIS, unresolved
 * // even after a 2026-09-08/2026-09-13 code-level trace found a genuine,
 * // unreconciled tension against the wire-timing reading (PROTOCOL.md
 * // §4.2). Do not ship a "Save as preset" UI affordance against [persist]
 * // without re-checking PROTOCOL.md §4.2/§6 first, per DECISIONS.md
 * // ADR-020's own scope note.
 *
 * // TODO(verify): [correlationByte] (the 13-byte prefix's one
 * // session-specific byte, offset 2) has no confirmed derivation — this
 * // project has only ever observed it, never traced how the official app
 * // computes it (PROTOCOL.md §4.5 intro calls it a "request/response
 * // correlation ID"). A caller that doesn't know the peer's expected value
 * // for the current session has no evidenced way to supply one; this
 * // encoder defaults to 0x00, unverified against real hardware.
 */
data class EqFrame(
    val gains: EqBandGains,
    val persist: Boolean = false,
    val correlationByte: Int = 0x00,
)
