# 0004_MAINTENANCE_PROMPT_2026_09_09.md — Clean up TODO.md's stale Phase 3 status; advance non-capture-dependent Phase 3 work (UUID register, connection-lifecycle analysis on existing captures)

**Number:** 0004
**Category:** MAINTENANCE
**Date:** 2026-09-09
**Title:** Clean up TODO.md's stale Phase 3 status; advance non-capture-dependent Phase 3 work (UUID register, connection-lifecycle analysis on existing captures)

---

## Mandatory reading order (do this first, in this order)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8, read, in order, before taking any other
action:

1. `AGENTS.md` (full)
2. `PROJECT.md` (full)
3. `PROJECT_RULES.md` (full)
4. `DECISIONS.md` (every ADR, ADR-001 through the most recent — do not skip to only the latest few)
5. `ARCHITECTURE.md`
6. `PROTOCOL.md` (full, including §2.3's framing table, §5's connection lifecycle, §6's open
   questions, §8's changelog)
7. `TODO.md` (full, including the "Recommended priority order" section and Phase 3 in particular)
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`
9. `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` in full (the immediately preceding session —
   several of its findings are directly relevant background: the `qhr` field register corrections,
   the `MaestroEndpointService` authorization-policy finding, and the `CAP-041` connect-time-burst
   content diff)
10. `REVERSE_ENGINEERING.md`'s UUID register section, its `qhr`/`qjb`/`gaa` entries, and its
    Correlation-status table

This prompt originates from a maintainer question in the chat session that authored it: looking at
`TODO.md`, several Phase 1 captures are still outstanding, Phase 2 (APK reverse engineering) looks
essentially done, and Phase 3 (Protocol reconstruction) shows mostly unchecked `[ ]` items — is Phase
3 blocked on the remaining Phase 1 captures, or can it proceed now? The answer worked out in that
chat session (not re-derived here — treat it as given context): **most of Phase 3's open items are
not capture-blocked at all** — several are simply stale checkboxes for work that is, in substance,
already done or deliberately not applicable (the framing question is resolved per-channel except
DLCI 0x08; the Message Group/Code register is empty by design per `DECISIONS.md` ADR-025 for DLCI
0x04/0x08; the UUID register is partial by design, not blocked). The **one** genuine Phase-1
dependency is bringing battery status fully to 🟢 FACT: Option C (HFP) is already FACT, but Option A
(BLE Battery Notification) is pinned on the still-outstanding clean, connection-free BLE-scan repeat
(`CAP-011` was inconclusive) — that one sub-item stays blocked and is explicitly out of scope for
this prompt.

## AI-assistance boundary throughout (unchanged from `0001`/`0002`/`0003`)

Per `AGENTS.md` §6/§15 and `DECISIONS.md` ADR-017: an AI session may run keyword/string searches,
list candidates, and explain already-surfaced code/captures — but never decides relevance, never
records a finding directly as settled 🟢 FACT in `PROTOCOL.md` without maintainer sign-off, and never
writes or amends a `DECISIONS.md` ADR unilaterally. Task 2's UUID-register work and Task 3's
lifecycle-analysis work will very likely surface candidate FACT promotions or open questions — treat
every one as a proposal for maintainer review, exactly as `0001`–`0003` did, not as something this
session commits itself. If a genuinely promotable finding emerges, stop and ask the maintainer
directly in the session (per the Decision Points norm already established in `0003`), rather than
only writing "awaiting sign-off" into the `RESULT` file.

## Resumability

If this task is interrupted, do not start a new numbered session on resumption — the same
`ai-sessions/0004_MAINTENANCE_RESULT_2026_09_09.md` file (once created) is progressively appended to,
with its header `Status` kept at `partial — resumed` until both tasks below are genuinely finished.

---

## Task 1 — Clean up `TODO.md`'s stale Phase 3 status

`TODO.md`'s `## Phase 3 — Protocol reconstruction` section (distinct from the "Recommended priority
order" section, which is comparatively current) has not been substantively reconciled against actual
project status in some time. Rewrite each item's checkbox/description to reflect what is actually
true today, per the analysis already done in the chat session that authored this prompt:

1. **UUID register item**: currently `[ ]` with no nuance. Update the description to note this is
   partial *by design*, not a blocked/stalled task — cross-reference `CAP-034`'s already-resolved
   GATT handle↔UUID mapping (`PROTOCOL.md` §6, §4.3 Option D) as UUIDs that likely still need
   cross-referencing into `REVERSE_ENGINEERING.md`'s own UUID register (Task 2 below does this work;
   this task only fixes the description).
2. **Message Group/Code register item**: currently phrased as an open task ("fill in... if the Fast
   Pair Message Stream framing hypothesis is confirmed"). The framing hypothesis *is* confirmed
   (§2.1/§4.1), but `DECISIONS.md` ADR-025 and `REVERSE_ENGINEERING.md`'s own register note already
   establish this table is expected to stay **empty by design** for DLCI 0x04/0x08 (their transport
   lives in GMS, not the companion app). Reword so this reads as "confirmed empty, not stalled,"
   matching `REVERSE_ENGINEERING.md`'s own framing — do not leave it looking like unfinished work.
3. **Framing-question item**: currently phrased as if fully open ("resolve the framing question...
   this blocks implementing `FrameEncoder`/`FrameDecoder`"). Per `PROTOCOL.md` §2.3, this is resolved
   *per channel* — DLCI 0x02 and 0x04 are 🟢 FACT and implementation-unblocked; only DLCI 0x08's
   identity remains 🔴 open. Reword to state this precisely, matching `ARCHITECTURE.md` §5's own
   per-channel gate language, so the item doesn't read as a single monolithic blocker it no longer is.
4. **Connection lifecycle item**: currently phrased as fully open ("document the full connection
   lifecycle with real capture evidence... currently an ⚪ ASSUMPTION"). Per `PROTOCOL.md` §5.1, the
   classic BR/EDR link-establishment portion (steps 1–2) is already 🟢 FACT across seven independent
   captures — only steps 3–6 (RFCOMM channel-opening sequence, Message Stream/`libmaestro` handshake
   ordering, first battery notification/command timing relative to the classic link) remain ⚪
   ASSUMPTION. Reword to reflect this split precisely. Task 3 below works on steps 3–6 specifically.
5. **Battery-to-FACT item**: currently phrased as fully open. Reword to note Option C (HFP) is
   already 🟢 FACT (`PROTOCOL.md` §4.3, `DECISIONS.md` ADR-015/ADR-023); only Option A (BLE Battery
   Notification) remains blocked, specifically on the still-outstanding clean, connection-free
   `CAP-011` repeat (Phase 1's own capture backlog) — cross-reference that capture item so the
   dependency is explicit and findable, rather than this item silently duplicating it.

Do not touch the "Recommended priority order" section (already reasonably current as of `ai-sessions/0003`) or any other `TODO.md` section — this task is scoped to Phase 3's own checklist only.

## Task 2 — Fill in the UUID register (`REVERSE_ENGINEERING.md` §UUID register)

The register currently has 3 entries (the two `fzd.java`/`gbm.java` RFCOMM-socket UUIDs and the HID
profile UUID). `PROTOCOL.md` §6's already-resolved `CAP-034` finding independently confirmed a full
15-service GATT UUID mapping via wire capture (Fast Pair Service `0xFE2C` and its
`FE2C1233`–`FE2C1239` characteristics, Device Information `0x180A`, Battery Service `0x180F`/`0x2A19`,
Accessory Non-Owner Service `15190001-...`, and the still-unnamed "Unknown Service"
`109b862f-...`) — none of these are yet cross-referenced into this register, which is scoped to *APK*
findings specifically ("All UUIDs found in the APK, with status").

1. Search `jadx-output/` for each of these UUID strings (both canonical and byte-reversed forms,
   per this project's own established practice for the two RFCOMM UUIDs already in the register) to
   determine which ones the companion app's own code actually references (e.g. via
   `UUID.fromString(...)` or a `BluetoothGattCharacteristic`/`BluetoothGattService` construction) as
   opposed to being purely externally-observed (Bluetooth-SIG-standard or Fast-Pair-spec-defined,
   never literally typed as a string constant in this app's own decompiled source).
2. For each UUID found in the app's own code: add a row to the register with file:line citation,
   per this document's own template, and cross-reference the matching `PROTOCOL.md` §6/§4.3 finding.
3. For each UUID from `CAP-034`'s table *not* found anywhere in the app's own code: do not add a row
   (the register's own scope note is explicit — APK findings only) — instead, note this explicitly as
   a checked-and-negative result in the register's own surrounding text, so a future session doesn't
   re-attempt the same search assuming it was never tried.
4. Do not promote anything to 🟢 FACT in `PROTOCOL.md` from this pass alone — a UUID's mere presence
   as a string literal in the app's code is 🟢 FACT for "this code references this UUID," per this
   document's own status legend, but says nothing new about wire behavior that `CAP-034` hasn't
   already established; record accordingly.

## Task 3 — Connection-lifecycle analysis on existing captures (`PROTOCOL.md` §5, steps 3–6)

`PROTOCOL.md` §5.1 already promotes the classic BR/EDR link-establishment mechanics (steps 1–2) to
🟢 FACT. Steps 3–6 — the RFCOMM channel-opening sequence (which DLCI opens first, second, third:
0x02/0x04/0x08), the Message Stream/`libmaestro` handshake ordering, and when the first battery
notification/app command arrives relative to the classic link completing — remain ⚪ ASSUMPTION,
despite this project already having several captures with a full, untruncated connection sequence on
disk (e.g. `CAP-034`, `CAP-036`, `CAP-037`, `CAP-041` — all confirmed raw-path extractions with zero
truncated frames, per their own FINDINGS.md files).

1. Pick at least one, ideally two, of these existing captures (favor ones already confirmed
   untruncated with a clean single reconnect/pairing window, to keep the sequence unambiguous) and
   extract, in strict chronological order from the classic ACL `Connection Complete` event onward:
   the exact order in which DLCI 0x02, 0x04, and 0x08 open (`SABM`→`UA`), the first payload-bearing
   frame on each, and where the already-known events (DLCI 0x04's `Get ANC state`/`Notify ANC state`
   pair, DLCI 0x08's connect-time burst, DLCI 0x02's connect-time RPC burst, the first HFP
   `AT+BIEV`/`AT+CIND` exchange) land relative to each other and relative to the classic link
   completing.
2. Check whether this ordering/timing is **consistent across the captures compared** — a repeatable
   sequence is itself evidence worth promoting toward HYPOTHESIS/FACT (per `PROJECT_RULES.md` §1's
   promotion rules — multiple independent captures agreeing is exactly the kind of evidence that
   already promoted §5.1's own classic-link mechanics); a sequence that varies capture-to-capture is
   itself a finding (record it as such, not force a single canonical ordering).
3. Write up the result as a proposed replacement/refinement for `PROTOCOL.md` §5's steps 3–6 —
   quoting frame numbers/timestamps per capture, per this project's own hex-and-command evidence
   rule (`PROJECT_RULES.md` rule 4a) — flagged for maintainer review before any status marker
   changes from ⚪/🟡 to 🟢, per the AI-assistance boundary above.
4. This is capture **re-analysis**, not new capture-taking — entirely within this task's own
   non-capture-dependent scope (the raw logs are already on disk).

---

## Guardrails

- Task 1 is documentation/process cleanup — it does not by itself go through the FACT/ADR sign-off
  gate (matching how `ai-sessions/0003`'s own Phase 1 doc-sync work was scoped), except where fixing
  a stale description would require asserting something not already established elsewhere (it
  shouldn't — Task 1 only restates what `PROTOCOL.md`/`DECISIONS.md` already say).
- Tasks 2–3 are exactly the kind of place a new protocol-relevant finding might emerge — treat every
  one as a proposal, never as something this session commits itself, per `AGENTS.md` §6/§15.
- Do not touch Phase 1's remaining capture backlog, Phase 2, Phase 4, or Phase 5 in `TODO.md` — out
  of scope for this prompt.
- Do not attempt to close battery Option A (BLE Battery Notification) — explicitly still
  capture-blocked, out of scope here.
- Log this session per `AI_SESSION_LOG_PROCEDURE.md`: write `ai-sessions/0004_MAINTENANCE_RESULT_2026_09_09.md` with `Status: complete` if both tasks finish, `partial — resumed` if not, or
  `awaiting maintainer sign-off` if Task 2/3 findings are ready for review but not yet reviewed.

## Output

List every file created or changed. For Task 2, list every UUID checked and its found/not-found
result. For Task 3, quote the reconstructed steps-3–6 sequence in full, per capture, with frame
numbers/timestamps, exactly as it would be proposed for `PROTOCOL.md` §5.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0004_MAINTENANCE_PROMPT_2026_09_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0004_MAINTENANCE_PROMPT_2026_09_09
