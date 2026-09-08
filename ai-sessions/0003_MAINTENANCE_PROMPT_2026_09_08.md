# 0003_MAINTENANCE_PROMPT_2026_09_08.md — Execute 0002's Task 4 gap scan (APK RE, cross-checks, consistency, external validation, documentation)

**Number:** 0003
**Category:** MAINTENANCE
**Date:** 2026-09-08
**Title:** Execute 0002's Task 4 gap scan (APK RE, cross-checks, consistency, external validation, documentation)

---

## Mandatory reading order (do this first, in this order)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8 (added by prompt `0002`), read, in order,
before taking any other action:

1. `AGENTS.md` (full)
2. `PROJECT.md` (full)
3. `PROJECT_RULES.md` (full)
4. `DECISIONS.md` (every ADR, ADR-001 through the most recent — do not skip to only the latest few)
5. `ARCHITECTURE.md`
6. `PROTOCOL.md` (full, including its own changelog/open-items sections)
7. `TODO.md`
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`
9. `ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md` in full — this task executes its Task 4
   gap-scan, and `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` (which `0002` itself
   implemented) for the deeper background behind several of these items
10. `REVERSE_ENGINEERING.md` (full, including the `qhr` entry, the `ijk`/`ijp`/GMS-boundary entry,
    the `MaestroDeviceSettingsProviderService` entry, the `frb`/`fuh`/`glk`/`gjv` entry, and the
    `MaestroEndpointService` entry — all added by prompt `0002`)

## AI-assistance boundary throughout (unchanged from `0001`/`0002`)

Per `AGENTS.md` §6/§15 and `DECISIONS.md` ADR-017: an AI session may run keyword/string searches,
`pbtk`/disassembly-output analysis, list candidates, and explain already-surfaced code — but never
decides relevance, never records a finding directly as settled in `REVERSE_ENGINEERING.md`, never
self-promotes anything to 🟢 FACT in `PROTOCOL.md`, and never writes or amends a `DECISIONS.md` ADR.
Everything below is proposals for maintainer review unless explicitly marked otherwise. This applies
to Phase 3 (APK reverse-engineering) and Phase 4 (cross-checks) in full; Phases 1, 2, and 5 are
largely documentation/validation work that does not touch protocol claims, but any sub-item that
*does* surface a candidate FACT promotion or ADR-worthy finding is still subject to this boundary.

## Resumability — this task may span multiple sessions

This is a long, multi-phase task. A token/rate-limit interruption partway through is expected, not
exceptional. Follow `AI_SESSION_LOG_PROCEDURE.md` §5 exactly: do **not** start a new numbered session
on resumption — the **same** `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md` file (once created)
is progressively appended to, phase by phase and sub-item by sub-item, with its header `Status` kept
at `partial — resumed` until everything below is genuinely finished (then `complete`, or `awaiting
maintainer sign-off` if proposals are still pending review). A resumed session reads its own
`RESULT` file first and continues from wherever it stopped — never re-doing a phase or sub-item
already checkpointed there. Checkpoint (write a paragraph to the `RESULT` file) after **each**
completed sub-item within a phase, not only at the end of a whole phase, so a mid-phase interruption
loses as little work as possible.

## Decision points — ask, don't just record

Whenever this task reaches a point where a genuine decision is the maintainer's to make — not
something this session can resolve by reading more code or running more searches — **stop, give a
concise summary of the choice and its trade-offs, and ask the maintainer directly in the session**,
rather than only burying it as a written proposal in the `RESULT` file for someone to notice later.
This includes, at minimum:

- Any point where a finding is strong enough that it could plausibly be signed off for 🟢 FACT
  promotion or a `DECISIONS.md` ADR/ADR-Update immediately (mirroring how `qhr` fields 11/15 and the
  ADR-025 Update were handled in prompts `0001`/`0002`) — present the finding and ask, rather than
  only writing it up as "awaiting maintainer sign-off" and moving on.
- Whether this session (and future `MAINTENANCE`-category sessions like it) warrant a `CHANGELOG.md`
  entry — `ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md`'s own "Consistency checks still
  needed" section explicitly left this as the maintainer's call.
- The flagged genuine gap in `TODO.md`'s existing "Recommended priority order" (it is stale relative
  to `CAP-034`–`CAP-042` and `DECISIONS.md` ADR-020–ADR-025) — per `PROJECT_RULES.md` §3 rule 9,
  propose the specific sync/reordering, do not silently apply it.
- Anything Phase 3/4 surfaces that isn't cleanly covered by an item already named below.

## Phases, in priority order (chosen for this task — reasoning given per phase)

Covers `ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md`'s Task 4 gap-scan in full, **except** its
"Captures still needed" section (out of scope here — that work needs the maintainer's own hardware
and is already correctly tracked in `TODO.md`/`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index).

### Phase 1 — Consistency checks & Plain documentation updates (combined)

**Do this first.** Reasoning: cheap, mechanical, no research risk, and it establishes an accurate
baseline before new findings from later phases are layered on top — avoids editing the same
`TODO.md`/`README.md` sections twice. `0002_MAINTENANCE_RESULT_2026_09_08.md`'s own write-up already
notes these two Task 4 categories overlap almost entirely; treat them as one execution pass.

1. **`TODO.md` sync pass.** Its "Recommended priority order" §3 and the Phase 1 "Top priority"/
   "Next" checklists have not been substantively updated since ~2026-08-30 and do not reflect
   `CAP-034` (2026-09-01 — which *closed* the Group W GATT cache-busting item `TODO.md` still lists
   as needing "a genuine attempt at Group W's own untried cache-busting methods"), `CAP-035`–
   `CAP-042`, `DECISIONS.md` ADR-020 through ADR-025, or `0002`'s own Multipoint/Volume EQ
   promotions. Bring these current: add checkmarks/notes for completed captures, close the stale
   Group W item, and update the "current highest-leverage single next step" language (which still
   names `qhr` fields 11/15/17/19/22/27/28 as unclosed, when all 7 are now done). Per the Decision
   Points section above: since this is a genuine gap in an *existing* priority ordering (not just
   missing detail), present the specific proposed reordering to the maintainer before applying it.
2. **`README.md` sync pass.** Its "Current state" date header and "Still open"/"Confirmed and
   implementation-ready" lists are stale relative to today's Multipoint/Volume EQ promotions (move
   Multipoint out of "Still open"; the "Confirmed and implementation-ready" list should gain it).
3. **Ask the maintainer** whether this session (and `MAINTENANCE`-category sessions generally)
   should produce a `CHANGELOG.md` entry — do not decide this unilaterally, per the Decision Points
   section above.
4. Not part of this session's scope, but note if encountered: `scripts/lint_docs.py`'s existing
   dead-filename findings in `CAP-036`/`CAP-038`/`CAP-039`/`CAP-040`'s event-notes/findings files,
   and a `PROJECT_RULES.md` rule 9a compliance sweep of `CAP-NNN-FINDINGS.md` files dated on/after
   2026-08-15 — both flagged in `0002_MAINTENANCE_RESULT_2026_09_08.md` as not investigated; if
   this phase has spare time/budget after 1–3 above, a first pass is welcome, but do not let it
   block Phase 2 onward.

### Phase 2 — Validation against outside sources

Reasoning: cheap (external fetches only, no code tracing), independent of the APK work in Phase 3,
and may quickly resolve or downgrade some open items before deeper analysis effort is spent chasing
them.

1. Re-verify `PROTOCOL.md` §4.3 Option A's "shown ≥8s, auto-hidden after 20s" Battery Notification
   visibility-timing claim directly against the official Fast Pair spec pages (the 2026-09-03
   re-check found no matching text on the `batterynotification` extension page specifically — check
   whether it lives on a different page, e.g. the base Message Stream spec).
2. Cross-check the Ring action's two ACK variants' extra byte(s) against the corrected spec's own
   worked-example tail (`01 3C`) more thoroughly, and against `qzed/pbpctrl`'s own notes if they
   cover this specific Action-group ACK shape.
3. Cross-check the DLCI 0x02 Address-field-renegotiation hypothesis (`PROTOCOL.md` §6) against
   Pigweed's public `pw_rpc` documentation for how channel/client addressing is typically
   negotiated — protocol *knowledge* only, per `AGENTS.md` §12/`DECISIONS.md` ADR-003's no-code-reuse
   rule.
4. Re-check `qzed/pbpctrl`'s own published notes
   (`https://raw.githubusercontent.com/qzed/pbpctrl/main/docs/Notes.md`) for anything beyond the
   already-consulted transport-framing paragraph — confirm there isn't further published detail on
   EQ/settings semantics that would strengthen or contradict this project's own independently-derived
   findings.
5. Check `FE2C1238…`'s official name and the "Unknown Service" (`109b862f-…`)'s purpose
   (`CAP-034-FINDINGS.md` §8) against the live, evolving Fast Pair spec pages and the Bluetooth SIG
   assigned-numbers database — the Fast Pair spec has already been found to have drifted once
   against this project's own citations (§4.3 Option A's timing-claim downgrade), so re-checking
   rather than trusting an old citation is the right default.

### Phase 3 — APK reverse-engineering

Reasoning: the largest, most token-intensive phase of this task — the most likely place to hit a
rate limit. Broken into independently-resumable sub-items; checkpoint after each one.

1. Trace `MaestroDeviceSettingsProviderService`'s 6 case IDs (2102/2103/2104/2113/2115/2116) to
   their specific `qhr` field numbers, using the same forward-trace technique (named UI
   fragment/preference key → write call site) already used for Multipoint/Volume EQ
   (`REVERSE_ENGINEERING.md`'s `MaestroDeviceSettingsProviderService` entry, `TODO.md`'s matching
   item).
2. `apktool` smali-fallback read of `MaestroEndpointService.onCreate()`
   (`APK_REVERSE_ENGINEERING_PROCEDURE.md` §6) — its bytecode is JADX-undecompilable; determine
   which gRPC service(s) this exported, no-permission-gated on-device server actually registers, and
   who binds to it (`REVERSE_ENGINEERING.md`'s `MaestroEndpointService` entry, `TODO.md`'s matching
   item).
3. Trace `gjv.p()`'s own caller (`REVERSE_ENGINEERING.md`'s `frb`/`fuh`/`glk`/`gjv` entry) — the
   remaining open link needed to determine whether `fxm.i()`'s `GetSoftwareInfo` fetch genuinely
   fires inside the `CAP-036`/`CAP-041` connect-time settling window. This item was flagged in
   `0002_MAINTENANCE_RESULT_2026_09_08.md`'s gap scan but never added to `TODO.md` — add it there as
   part of this phase (see Phase 5 item 1 below) once traced or confirmed still open.
4. Trace `fyd.d`/`fyd.e`'s own call sites in the EQ UI fragment (`REVERSE_ENGINEERING.md`'s `qjw`
   entry, pre-existing `TODO.md` item) — the step needed to connect `qjw` field 16/18's
   code-derived "live vs. persisted" reading to the wire-observed "drag vs. release" timing.
   Directly relevant to whether EQ's "Save as preset" UI can ship (`DECISIONS.md` ADR-020's own
   scope note).
5. Apply the same field-register method to `qhr`'s remaining, not-yet-traced fields (1, 6, 8, 14,
   20, 24–26, 30–38) — lower priority, since most are plausibly write-silent/read-inert like fields
   6/8/9/10 and no known v1-scope feature depends on them; do this only if time/budget remains after
   1–4 above.
6. Re-attempt decoding DLCI 0x02's opaque "Sent" blocks (the AES-128-encryption hypothesis,
   `PROTOCOL.md` §6) against the now-recovered `qhr`/`qjc`/`qja`/`nqx` schema — this was explicitly
   blocked pending "a pw_rpc/protobuf schema to check against," which now substantially exists.
   Check whether the opaque blocks decode structurally now, before concluding they're encrypted.

### Phase 4 — Cross-checks

Reasoning: several of these depend on Phase 3's outputs (the decoded `GetSoftwareInfo` response
shape, the `gjv.p()` trace) — run after Phase 3, not before.

1. `CAP-041`'s DLCI 0x02 connect-time-burst content diff across differing settings states — the
   *length*-level negative result is done; a full byte-for-byte content diff was out of that
   session's time budget and remains open. Directly relevant to `ARCHITECTURE.md` §3.1's "does
   `libmaestro` carry a settings-state read-back" question. This is a re-analysis of an existing
   capture already on disk — no new capture needed.
2. A byte-level correlation between a fresh capture's DLCI 0x02 connect-time burst and the decoded
   `GetSoftwareInfo` response shape (`qjb`) — **note:** per `0002_MAINTENANCE_RESULT_2026_09_08.md`,
   this specific cross-check needs a *fresh* capture, which is out of this task's scope (see Phase 1
   in `0002`'s report, "Captures still needed"); do what can be done against *existing* capture data
   instead (e.g. checking whether any already-captured connect-time burst's frame-count/shape is
   consistent with `qjb`'s decoded structure), and explicitly flag if the question can only be fully
   closed by a new capture.
3. `TrueWirelessHeadset.modelId` cross-referenced against DLCI 0x04's already-FACT Device
   Information "Model ID" (`da 2d b1`) — check whether a live-device value dump is already available
   anywhere in this project's existing captures/docs; if not, flag this as needing the maintainer's
   own device access rather than attempting to guess a value.

### Phase 5 — Anything else

1. Formalize the `gjv.p()`-caller-trace item into `TODO.md`'s "Targeted research follow-ups" list if
   Phase 3 item 3 didn't fully close it (this was identified as a gap in `0002_MAINTENANCE_RESULT_2026_09_08.md`
   itself — the item was named but never added to `TODO.md`).
2. Note/confirm the `CAP-041`/`CAP-037` re-analysis-only categorization from
   `0002_MAINTENANCE_RESULT_2026_09_08.md`'s "Anything else" section (existing-capture re-analysis,
   not a new capture, not APK work — a third category worth keeping distinct for future prioritization)
   — informational, no action needed beyond confirming it's still accurate.
3. Final synthesis: pull together every proposal from Phases 2–4 into the specific
   `PROTOCOL.md`/`REVERSE_ENGINEERING.md`/`TODO.md` edits they would produce, clearly labeled as
   proposals awaiting maintainer review (per the AI-assistance boundary above) — do not commit any of
   them without going through the Decision Points process above first.

## Guardrails

- Phase 1's documentation-sync work is process/bookkeeping, not a protocol claim — it does not by
  itself go through the FACT/ADR sign-off gate, matching `AI_SESSION_LOG_PROCEDURE.md`'s own scope
  note. The one exception: if syncing `TODO.md`'s priority order surfaces a genuine reordering
  decision (flagged above), that goes through the Decision Points process, not a silent edit.
- Phases 3–4 are the only places new protocol-relevant findings might emerge — treat every finding
  there as a proposal, exactly as `0001`/`0002` did, never as something this session commits itself.
- Do not expand scope beyond the six Task 4 categories named above (APK reverse-engineering,
  cross-checks, consistency checks, validation against outside sources, plain documentation updates,
  anything else) — "Captures still needed" is explicitly out of scope for this prompt.
- Log this session itself per `AI_SESSION_LOG_PROCEDURE.md`: write (and, on resumption, append to)
  `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md`, with `Status: partial — resumed` until every
  phase above is genuinely finished, then `complete` (if nothing is left pending maintainer review)
  or `awaiting maintainer sign-off` (if proposals remain for the maintainer to decide on).

## Output

For each phase, record what was found/changed and where; quote any proposed `PROTOCOL.md`/
`REVERSE_ENGINEERING.md`/`DECISIONS.md` text in full in the `RESULT` file rather than only
summarizing it, matching `0001`/`0002`'s own level of detail. List every file created or changed.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0003_MAINTENANCE_PROMPT_2026_09_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0003_MAINTENANCE_PROMPT_2026_09_08
