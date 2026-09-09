# 0004_MAINTENANCE_RESULT_2026_09_09.md — Clean up TODO.md's stale Phase 3 status; advance non-capture-dependent Phase 3 work (UUID register, connection-lifecycle analysis on existing captures)

**Number:** 0004
**Category:** MAINTENANCE
**Date:** 2026-09-09
**Title:** Clean up TODO.md's stale Phase 3 status; advance non-capture-dependent Phase 3 work (UUID register, connection-lifecycle analysis on existing captures)
**Status:** complete

---

## Mandatory reading order — completed

This session continues the same conversation that produced `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md`
(no context reset in between) — `AGENTS.md`, `PROJECT.md`, `PROJECT_RULES.md`, `DECISIONS.md` (every
ADR), `ARCHITECTURE.md`, `PROTOCOL.md` (full), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, and
`ai-sessions/INDEX.md` were all read in full during that immediately preceding session and remain
current context here. `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` (this prompt's own item 9)
was authored by that same preceding session. Re-read fresh for this session: `TODO.md`'s Phase 3
section (to confirm current exact wording before editing) and `REVERSE_ENGINEERING.md`'s UUID
register section (to confirm its current exact state before extending it).

**AI-assistance boundary acknowledged**: per `AGENTS.md` §6/§15 and `DECISIONS.md` ADR-017, unchanged
from `0001`–`0003`.

---

## Task 1 — Clean up `TODO.md`'s stale Phase 3 status — done

Rewrote all 5 items flagged in the prompt: the UUID-register item (now notes it's partial by design,
names the concrete `CAP-034` cross-referencing next step), the Message Group/Code register item
(checked `[x]`, reworded to state it's confirmed empty by design per `DECISIONS.md` ADR-025, not
stalled work), the framing-question item (reworded to state it's resolved per-channel — only DLCI
0x08 remains open, not a monolithic blocker), the connection-lifecycle item (reworded to note steps
1–2 are already 🟢 FACT, only steps 3–6 remain ⚪ ASSUMPTION), and the battery-to-FACT item (reworded
to note ANC and battery Option C are both already done, only Option A remains and is capture-blocked,
cross-referenced to the Phase 1 `CAP-011` repeat rather than silently duplicating it).

**Files changed**: `TODO.md` (Phase 3 section, 5 items rewritten).

## Task 2 — Fill in the UUID register — done, clean negative

Searched the entire decompiled companion app (`jadx-output/sources/` and `apktool-output/smali*/`,
case-insensitive, both full 128-bit and short 16/32-bit forms) for all 8 GATT UUIDs `CAP-034` already
resolved via wire capture (Fast Pair Service `0xFE2C` + its 7 characteristics `FE2C1233`–`FE2C1239`,
Device Information `0x180A`, Battery Service `0x180F`, Battery Level `0x2A19`, Firmware Revision
`0x2A26`, Accessory Non-Owner Service `15190001-...`, "Unknown Service" `109b862f-...`).

**Result: zero genuine matches anywhere in the companion app's own decompiled source.** 5 smali hits
surfaced from the raw grep; each was individually opened and confirmed to be a coincidental hex
substring inside an unrelated numeric constant (a double literal, a resource ID, a hashCode-shaped
constant, a `serialVersionUID`, a switch-case hash) — none is an actual UUID reference. Per this
task's own instruction, no new register rows were added for these UUIDs (their wire-level identity
is already fully established in `PROTOCOL.md` directly from `CAP-034`); instead, the clean negative
itself was recorded in the register's own surrounding text so a future session doesn't re-attempt
the same search assuming it was never tried. This corroborates, rather than adds to, `DECISIONS.md`
ADR-025's existing finding that this companion app's own code contains no Fast-Pair-GATT handling —
consistent with that transport living entirely inside Google Play Services.

**Files changed**: `REVERSE_ENGINEERING.md` (UUID register section, new negative-result note),
`TODO.md` (Phase 3's UUID-register item updated to record this pass's completion).

## Task 3 — Connection-lifecycle analysis on existing captures — done, a genuine promotion candidate found

Extracted the exact RFCOMM channel-opening order (every `SABM` event, per DLCI, from the classic ACL
`Connection Complete` onward) across 6 independent fresh-reconnect instances spanning 3 capture
sessions already on disk (`CAP-036`; `CAP-041` Windows A/B/C; `CAP-037`, two different chandles' own
first reconnects) — no new capture needed, exactly as scoped.

**Result**: all 6 instances show the same macro-order — multiplexer control channel (`0x00`) → HFP
(`0x0c`, confirmed fastest to open) → the official Fast Pair Message Stream (`0x04`) → the private
envelope (`0x08`) and the silent channel (`0x0a`) in either relative order (the only sub-ordering
that varies) → **`libmaestro`'s own channel (`0x02`) opens last, every single time**, by a
substantial and consistent 0.6–1.3 second margin. While checking further, found a genuine,
honestly-scoped counter-example: a mid-session RFCOMM channel-bounce in `CAP-037` (confirmed via HCI
`Disconnection Complete` events to sit *inside* one continuous, unbroken ACL connection, not a fresh
reconnect) shows `libmaestro`'s channel opening *third*, not last — recorded as a real exception, not
suppressed to keep a cleaner story. The "opens last" pattern's 6/6 record holds specifically for
fresh RFCOMM-multiplexer establishment following a new/renewed classic ACL connection.

This also explains, mechanistically, several already-individually-documented findings (DLCI 0x04's
Get/Notify ANC pair, DLCI 0x08's zero-length Get-shaped burst, and DLCI 0x02's own connect-time RPC
burst each fire within tens of milliseconds of *their own* channel opening) — since `libmaestro`'s
channel reliably opens last, its own burst is, as a direct consequence, also reliably the last burst
in the overall sequence.

Full command + frame numbers + timestamps for all 6 instances (plus the exception) written into a
new `PROTOCOL.md` §5.2 — proposed as 🟡 HYPOTHESIS (strong), not committed, per `AGENTS.md`
§6/`DECISIONS.md` ADR-017. This sample size/replication pattern is comparable to findings this
project has already promoted to 🟢 FACT (`DECISIONS.md` ADR-014's 4 independent sessions) — flagged
to the maintainer directly (see below) rather than only left as "awaiting sign-off" in this file.

**Files changed**: `PROTOCOL.md` (new §5.2, §5's closing Status/Evidence summary updated, new §8
changelog row), `TODO.md` (Phase 3's connection-lifecycle item updated to record step 3's progress).

## Decision point — resolved with the maintainer

Per the established norm (`ai-sessions/0003`), the Task 3 finding above was put to the maintainer
directly rather than only written up as "awaiting sign-off": promote "DLCI 0x02 opens last on a
fresh reconnect" to 🟢 FACT, or keep it at HYPOTHESIS (strong)? **The maintainer chose to keep it at
HYPOTHESIS (strong)**, pending more independent sessions or a dedicated purpose-built capture, given
the one known exception (the mid-session channel-bounce) isn't yet explained. `PROTOCOL.md` §5.2 and
its closing Status line, and the matching changelog row, were updated to record this as a reviewed,
deliberate decision — not an oversight, and not something a future session should re-propose without
new evidence.

**Files changed for this decision**: `PROTOCOL.md` (§5.2's own status line, §5's closing Status
summary, the §8 changelog row), `TODO.md` (the connection-lifecycle item's wording).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0004_MAINTENANCE_RESULT_2026_09_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0004_MAINTENANCE_RESULT_2026_09_09
