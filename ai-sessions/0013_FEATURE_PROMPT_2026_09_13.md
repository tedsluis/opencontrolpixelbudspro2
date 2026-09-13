# 0013_FEATURE_PROMPT_2026_09_13.md — Resolve pending 0012 decisions and documentation consistency, then begin Phase 4 app development (ANC-first)

**Number:** 0013
**Category:** FEATURE
**Date:** 2026-09-13
**Title:** Resolve pending 0012 decisions and documentation consistency, then begin Phase 4 app development (ANC-first)

---

## Purpose and scope

`ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` independently re-derived all 130 findings from
Gemini CLI's `0011` review and left the maintainer with a short list of follow-ups. In the chat
session that produced `0012`, the maintainer and the assisting session worked out a prioritized list
of 8 next steps. This prompt executes **7 of those 8** (all except CAP-043, a new capture the
maintainer will run personally — do not attempt it, do not create a skeleton for it, it is fully out
of scope for this prompt):

1. Resolve the CAP-036 "34-frame" DLCI 0x02 burst count discrepancy (`0012` open item 1).
2. Resolve the CAP-037 "34 reconnects" count discrepancy (`0012` open item 2).
3. Add a short housekeeping pointer to `TODO.md` documenting that `0012` ran and what it found.
4. Surface the Volume Balance (`field 17`) ±100/Left-Right-polarity FACT-promotion decision to the
   maintainer (`PROTOCOL.md` §4.5.7's existing PROPOSAL, independently reconfirmed by `0012` Finding
   125) — propose, do not promote.
5. Surface the Find My Buds Case/"both simultaneously" Zero-GMS scope decision to the maintainer
   (`PROTOCOL.md` §4.4 / `PROTOCOL.md` §6 Behavior / `TODO.md`'s own long-standing open item).
6. Surface the dependency-injection approach decision to the maintainer (`ARCHITECTURE.md` §10/§15).
7. Begin Phase 4 app development, ANC-first, to the extent it doesn't depend on an undecided item
   above (`TODO.md`'s own recommended next step).
8. Run a bounded, time-boxed follow-up research pass on the two still-open APK-static-analysis leads
   (DLCI 0x08 "GSND" naming, `gjv.p()`'s own caller) within `DECISIONS.md` ADR-017's
   mechanical-assistance boundary.

**End state this prompt must reach:** `PROTOCOL.md`, `TODO.md`, `DECISIONS.md`,
`REVERSE_ENGINEERING.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, and `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` all
reflect this session's work accurately — nothing this session touches or decides should be left
undocumented or stale in any of those six files. The Final phase below is a mandatory consistency
sweep across exactly these six documents; do not skip it even if every numbered item above already
felt "done" in passing.

## Hard rule for every decision point (items 4, 5, 6 above)

Per `AGENTS.md` §6/§15 (unchanged by anything in `0012`): this session may **propose** a FACT
promotion or draft ADR text, clearly labeled as a proposal awaiting sign-off, but must **never**
commit it as settled. For each of items 4/5/6:

- Write a clear summary (what the evidence shows) and a concrete recommendation into the RESULT
  file, labeled `PROPOSAL — awaiting maintainer sign-off`.
- Do **not** edit `PROTOCOL.md`'s status emoji, do **not** write or edit a `DECISIONS.md` ADR, and
  do **not** proceed with any downstream step that assumes a particular answer (e.g. do not wire a
  specific DI framework into `:app` before item 6 is decided).
- Continue to the *other* phases regardless — a pending decision on one item never blocks progress
  on the independent items. Only phase 7's specific DI-wiring sub-step is allowed to be gated on
  item 6's outcome; everything else in phase 7 is decision-independent (see phase 7 below).
- In the **chat response** at the end of the session (not only in the RESULT file), give the
  maintainer a compact, numbered list of exactly the decisions still needed, each with the
  recommendation, and ask for an explicit answer before anything gated on it proceeds.

## How to (re)start this prompt

Before anything else: check whether `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` already exists.
- **It does not exist** → first run. Create it in Phase 0 with a phase status table (one row per
  phase, 1–8 plus Final) and start from Phase 1.
- **It already exists** → read it in full, including the phase status table and any
  `PROPOSAL — awaiting maintainer sign-off` sections. If the chat context for *this* resumed session
  contains the maintainer's answer to a previously-surfaced decision (items 4/5/6), apply it now
  exactly as instructed (draft or ask the maintainer to confirm the exact `DECISIONS.md` ADR text
  before it is committed — this session may draft it, but committing a new ADR still requires the
  maintainer's explicit go-ahead in this same conversation, not an inference from silence). If no
  answer is available yet, skip straight to the first `not started`/`partial` phase that doesn't
  depend on it and continue from there.

Update the RESULT file's phase status table and the header `Status` field
(`partial — resumed` while incomplete, `awaiting maintainer sign-off` once every phase not gated on
a pending decision is done, per `AI_SESSION_LOG_PROCEDURE.md` §4/§5) before ending any turn.

## Mandatory reading order (do this first, in this order)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §6/§15 (FACT/ADR sign-off gate), §3 (Kotlin/coroutines/no-LiveData
   coding standards, relevant to phase 7), §12 (AGPL header + no-Google-code-copy rules, relevant to
   phase 7), §1/§2 (Zero-GMS, GrapheneOS permissions, relevant to phase 7), §13.6 (zero-creativity
   hex parsing, relevant to phases 1/2/8).
2. `PROJECT_RULES.md` (full) — especially rule 4a (hex-and-script rule), rule 9/9a (non-destructive
   update convention for `DECISIONS.md`/`PROTOCOL.md` vs. rewrite-in-place for `CAP-NNN-FINDINGS.md`).
3. `PROJECT.md`, `ARCHITECTURE.md` (full — especially §2/§5/§7/§10/§15 for phase 7 and item 6).
4. `PROTOCOL.md` (full — especially §4.1 ANC, §4.4 Find My Buds, §4.5.7 Volume Balance, §5, §6).
5. `DECISIONS.md` (full, every ADR — especially ADR-008, ADR-009, ADR-011, ADR-017, ADR-019).
6. `TODO.md` (full — especially the "Recommended priority order" section and Phase 4's checklist).
7. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
8. `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` (full) — the direct source of items 1–4 above;
   read its §4 "Open questions for the maintainer" and its Finding 125/106/107 detail sections
   closely.
9. `REVERSE_ENGINEERING.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` —
   these are three of the six documents this prompt must leave in a current state; know their
   current content before touching them.
10. `captures/CAP-036-.../CAP-036-FINDINGS.md`, `captures/CAP-037-.../CAP-037-FINDINGS.md`,
    `captures/CAP-046-.../CAP-046-FINDINGS.md` — the three capture files phases 1/2/4 act on
    directly.
11. `id_registry.csv`, `APK_REVERSE_ENGINEERING_PROCEDURE.md`,
    `reverse-engineering/APK_VERSIONS.md` — needed for phase 8's APK search.

## Guardrails

- Never promote anything to 🟢 FACT and never write/alter a `DECISIONS.md` ADR without the
  maintainer's explicit, in-conversation sign-off (see "Hard rule" above) — this applies to items
  4/5/6 specifically, but also to anything phase 8 turns up.
- The hex-and-script rule (`PROJECT_RULES.md` rule 4a) applies to phases 1/2: every count claim must
  show the exact command and confirm it against the actual current file, not a restated number.
- `CAP-NNN-FINDINGS.md` edits (phases 1/2) are rewrite-in-place corrections, not accumulating
  blockquote corrections (`PROJECT_RULES.md` rule 9a) — fix the stated count/text directly, do not
  stack a dated "Update:" note on top of the wrong one. A one-line "re-verified `<date>`,
  `tshark <version>`" parenthetical is sufficient provenance.
- Phase 8's APK work stays inside `DECISIONS.md` ADR-017's mechanical-assistance boundary: search,
  list, and explain already-surfaced code; never decide relevance of a new class/string on your own
  authority, never record a new `REVERSE_ENGINEERING.md` HYPOTHESIS without flagging it as a proposal.
- Phase 7's code must only implement protocol behavior already at 🟢 FACT with its implementation
  gate cleared (`ARCHITECTURE.md` §5, `AGENTS.md` §6) — as of this writing that means ANC (DLCI
  0x04, `DECISIONS.md` ADR-009), EQ (`DECISIONS.md` ADR-020), Find My Buds Left/Right (`DECISIONS.md`
  ADR-011), and Battery via HFP Option C (`DECISIONS.md` ADR-015/ADR-023). Do not implement DLCI
  0x02's generic settings-write path beyond what `DECISIONS.md` ADR-013 already unblocks (the
  envelope shape only, not individual field semantics), and do not implement anything touching DLCI
  0x08 (still 🔴 open identity).
- Phase 7 code follows `AGENTS.md` §3 exactly: Kotlin only, Coroutines/Flow (no LiveData), sealed
  `BudsResult`/`BudsError` types (`ARCHITECTURE.md` §7), every new Kotlin file gets the AGPL-3.0
  header (`AGENTS.md` §12), no `INTERNET` permission anywhere, minimal permission set
  (`AGENTS.md` §2), pinned dependency versions only (no `+`) in a version catalog. No code is ever
  copied from the official APK or from `pbpctrl` — only protocol *behavior* is reconstructed
  (`AGENTS.md` §12).
- Phase 7 has no physical Buds hardware available in this environment — scope it to what's
  independently unit-testable (`AGENTS.md` §11: fixed byte-array fixtures, a fake `BudsTransport`)
  and say so explicitly for anything that would need real hardware to actually validate. Do not
  claim end-to-end functionality that hasn't been tested against real hardware.
- Do not restructure or rewrite a document beyond what a specific phase justifies (`PROJECT_RULES.md`
  §3's spirit) — e.g. phase 3's `TODO.md` note is a short addition, not a rewrite of that file's
  structure.
- MAC addresses in this project's own captures are intentionally unredacted (`DECISIONS.md`
  ADR-010) — not a finding to flag or fix in phases 1/2.

## Phase 0 — Setup

Complete the mandatory reading order. Create `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` with
the required header block (`AI_SESSION_LOG_PROCEDURE.md` §4, `Status: partial — resumed` to start)
and a phase status table (rows: 1–8, Final). Register this session in `ai-sessions/INDEX.md` if not
already present (it should already have a `0013` row from when this prompt file was created — verify
it, don't duplicate it).

## Phase 1 — Resolve the CAP-036 "34-frame" DLCI 0x02 burst count

`CAP-036-FINDINGS.md` lines ~232-234 give an exact command for its "34 rows" claim. Re-run that exact
command against the current `CAP-036-btsnoop_hci.log` (record the `tshark` version used — this was
never recorded originally, which is part of why `0012` couldn't resolve this outright). If the result
still doesn't match "34":

- Check whether the discrepancy is explained by something mechanical (duplicate/retransmitted
  frames, a different `tshark` dissector version changing how a borderline frame is classified,
  the window boundary being interpreted differently) before concluding it's simply a stale count.
- If no explanation is found, trust your own re-derivation (`PROJECT_RULES.md` §1's own rule: your
  own re-run beats a prior claim) and correct `CAP-036-FINDINGS.md`'s line in place, with a
  one-line note recording the `tshark` version and date this was re-verified. This is a mechanical
  correction (a frame/subframe count), not a FACT/ADR change — no maintainer sign-off required, per
  the same class of edit `0012`'s own Finding 6 already made to `CAP-002-FINDINGS.md`.
- Update `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md`'s §4 "Item 1" to record that this was
  resolved and how (do not delete the item — mark it resolved in place, consistent with
  `AI_SESSION_LOG_PROCEDURE.md` §4a's convention for updating an earlier session's own record).

## Phase 2 — Resolve the CAP-037 "34 reconnects" count

`CAP-037-FINDINGS.md` lines ~155/162 state "34 reconnects." `0012`'s own re-derivation found 36 raw
Connection Complete events (31 success + 5 Page Timeout) to the Buds' address. Before concluding
either number is simply wrong:

- Read `CAP-037-FINDINGS.md`'s own full event timeline/methodology to determine whether "34" uses a
  different, explicitly-stated counting convention (e.g. a failed-then-immediately-retried pair
  counted as one logical reconnect, or some chandles/attempts excluded for a stated reason).
- If a real, already-intended convention explains the gap, add a one-line clarifying parenthetical
  next to the "34" citation (e.g. "(34 logical reconnects; 36 raw Connection Complete events
  including N immediate retries — see §M)") — this is a clarification, not a correction.
- If no such convention is evident, treat this the same way as Phase 1: trust the independent
  re-derivation, correct the count in place with a one-line re-verification note.
- Update `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md`'s §4 "Item 2" to record the resolution.

## Phase 3 — TODO.md housekeeping note about session 0012

Add a short, factual bullet to `TODO.md`'s "Targeted research follow-ups" section (matching the
precedent `ai-sessions/0008`'s own session set when it added a note to `TODO.md` about itself):
record that `0012` ran a full, non-sampled independent re-derivation of all 130 findings in `0011`,
its headline result (0011's core protocol-decode content held up with zero errors; 5 citation errors
and 1 overclaim were found, all in secondary/background material, all now resolved or superseded by
phases 1/2 above), and a pointer to `0012`'s own file. Keep this to a few lines — it is a pointer,
not a re-narration of `0012`'s content (`PROJECT_RULES.md` rule 9a's spirit).

## Phase 4 — Surface the Volume Balance FACT-promotion decision

Re-read `PROTOCOL.md` §4.5.7's current PROPOSAL text and `CAP-046-FINDINGS.md` in full. Confirm the
PROPOSAL text is still accurate and current (it should be, since `0012` Finding 125 independently
reconfirmed the wire-side evidence with exact byte-level matches on all 8 samples — cite that
confirmation explicitly here as additional supporting evidence, the same way `DECISIONS.md` ADR-022
cited a large replication count). Note explicitly, as `0012` itself flagged, that the Left/Right
video-correlation half of the claim was **not** independently re-watched by `0012` — only the
wire-side zigzag values were re-derived — so this proposal's video-correlation evidence still rests
solely on `CAP-046-FINDINGS.md`'s own original video pass, not on any new confirmation.

Write a `PROPOSAL — awaiting maintainer sign-off` block into the RESULT file:
- What would be promoted: `PROTOCOL.md` §4.5.7's ±100 range and `+100`=Left/`-100`=Right polarity,
  from 🟡 HYPOTHESIS (strong) to 🟢 FACT.
- The evidence for and against (the strong wire-side replication vs. the single-session,
  not-independently-re-checked video correlation, and the still-untested intermediate-position
  scaling question, which this promotion would **not** cover).
- A draft `DECISIONS.md` ADR (numbered as the next free `ADR-NNN` per that file's own registry
  check) — written out in full in the RESULT file, clearly labeled as a **draft, not committed**.
- A recommendation (approve / approve narrower scope / defer pending more evidence).

Do not edit `PROTOCOL.md` or `DECISIONS.md` in this phase. Do not assume approval.

## Phase 5 — Surface the Find My Buds Case/"both simultaneously" scope decision

Re-read `PROTOCOL.md` §4.4's "Major structural finding" and §6 Behavior's corresponding open item,
and `TODO.md`'s own listing of this as a top-priority, non-research decision ("a genuine Zero-GMS
scope trade-off... no capture or static analysis can resolve this, only a maintainer product
decision can"). Write a `PROPOSAL — awaiting maintainer decision` block (this one is a product/scope
call, not a FACT promotion, but still requires the maintainer, not the agent, to decide per
`AGENTS.md` §15's "never silently expand scope" rule and `PROJECT.md`'s own non-goals framing):

- State the trade-off plainly: Case/"both" ring only works today via Google's Find My Device
  Network (account/cloud-mediated) — implementing it would require a GMS/Google-account dependency
  this project's Zero-GMS goal (`AGENTS.md` §1) otherwise refuses; the alternative is shipping v1
  without local Case-ring support (Left/Right only, both already FACT and implemented starting in
  phase 7).
- A recommendation (e.g. ship v1 with Left/Right only, document Case/"both" as an explicit
  documented limitation, revisit only if a future capture finds a local mechanism).
- Note that if the maintainer picks the "ship without" option, `PROJECT.md`'s non-goals section and
  `TODO.md`'s Phase 1 open item should be updated to record the decision explicitly (do this
  automatically once/if the answer is given in this same conversation; otherwise leave it as an open
  item for the next session that has the answer).

## Phase 6 — Surface the dependency-injection approach decision

Re-read `ARCHITECTURE.md` §10/§15 in full. Write a `PROPOSAL — awaiting maintainer decision` block:

- Restate the two options exactly as `ARCHITECTURE.md` §10 already frames them (Hilt/Dagger vs. a
  manual service-locator), and that `AGENTS.md` §1 already clarifies Hilt itself does not touch
  `com.google.android.gms.*` and is not disqualified by the Zero-GMS rule on that basis alone.
- A recommendation, with reasoning (e.g. Hilt reduces composition-root boilerplate across 5 modules
  and is a mature, widely-used AndroidX-adjacent tool with no GMS/network footprint; a manual
  service locator gives full independence from Google-authored build tooling at the cost of more
  hand-written wiring code — pick whichever the maintainer's own priorities favor).
- Note explicitly that phase 7 below will proceed with the DI-independent parts of the project setup
  regardless, and defer only the actual `:app` composition-root wiring until this is decided.

## Phase 7 — Begin Phase 4 app development (ANC-first)

Scope this phase to what's both permitted (see Guardrails above) and realistically completable and
verifiable without physical Buds hardware in this environment:

1. **Project structure.** Set up the Android Studio project per `ARCHITECTURE.md` §2 (five Gradle
   modules: `:app`, `:ui`, `:domain`, `:data`, `:hardware`), with a version catalog
   (`libs.versions.toml`, pinned versions, per `AGENTS.md` §10) for the dependencies actually needed
   this phase (Kotlin, Coroutines, a test framework — JUnit5/Kotest per `AGENTS.md` §11 — and
   `protobuf-kotlin-lite` if the EQ/ANC payload construction benefits from it; do not add Hilt or any
   other DI dependency yet, pending phase 6).
2. **Domain layer.** `BudsError`/`BudsResult` sealed types (`ARCHITECTURE.md` §7) and the core domain
   models needed for ANC (`AncMode`, `ConnectionState`) — pure Kotlin, no Android dependency.
3. **Data layer — ANC codec.** Implement `FrameEncoder`/`FrameDecoder` (or the relevant slice of
   `CodecRouter`) for DLCI 0x04's ANC Set/Get/Notify commands (`PROTOCOL.md` §4.1), building/parsing
   the exact byte layouts already confirmed in `CAP-001`/`CAP-006` (cite the specific frames this
   code is modeled on in a comment or the accompanying test, per `AGENTS.md` §13's evidence-tracing
   spirit — but do not paste the official spec's or `pbpctrl`'s text verbatim, only the observed wire
   *behavior*, per `AGENTS.md` §12). Add unit tests against the exact fixed byte-array fixtures from
   `CAP-001` frames 2039/2041/2132/2134/2159/2162/2193/2195 and `CAP-006`'s four Set/ACK pairs
   (`AGENTS.md` §11) — redact no identifiers, these are protocol bytes, not device-identifying data.
4. **Hardware layer — transport interface.** Define the `BudsTransport` interface
   (`ARCHITECTURE.md` §2.1) and a fake/scripted implementation for the domain/data unit tests
   (`AGENTS.md` §11) — a real `BluetoothSocket`-backed implementation may be stubbed/sketched but
   cannot be meaningfully tested in this environment; say so explicitly rather than claiming it
   works.
5. **Connection state machine.** A minimal `ConnectionStateMachine` skeleton
   (`Disconnected → Connecting → Discovering → Ready`) per `ARCHITECTURE.md` §2.1 — this remains ⚪
   ASSUMPTION per `PROTOCOL.md` §5 for anything beyond the classic BR/EDR link steps already 🟢 FACT
   (`PROTOCOL.md` §5.1), so keep it structurally honest rather than over-specified.
6. **`:app` composition root — gated on phase 6.** If phase 6's DI decision is already known (either
   because this is a resumed run with the maintainer's answer, or the maintainer answers inline
   during this same conversation), wire the composition root accordingly. If not, leave `:app` as an
   empty/minimal shell with a clear `// TODO(blocked on phase 6 DI decision)` marker and say so in
   the RESULT file — do not guess.
7. **What this phase explicitly does NOT do:** EQ/Battery/Find My Buds implementation (leave these
   for a follow-up `FEATURE` session once ANC's pipeline is proven end-to-end, per `TODO.md`'s own
   "cheapest way to prove the whole architecture works" reasoning for starting with ANC alone), any
   real-hardware validation, any UI screens beyond what's strictly needed to wire the pipeline.

Record in the RESULT file exactly what was created/implemented, which tests pass, and what remains
explicitly blocked (DI wiring) or explicitly out of scope (real-hardware validation, other commands).

## Phase 8 — Bounded APK research follow-up (DLCI 0x08 "GSND" naming, `gjv.p()` caller)

Time-box this phase — it is a bonus, not a blocker for anything else in this prompt. Within
`DECISIONS.md` ADR-017's mechanical-assistance boundary (search/list/explain only, never decide
relevance, never record a HYPOTHESIS as settled):

1. Re-run a keyword search (`grep -ri "gsnd"`, and reasonable variants — "gsound", "google.*sound",
   etc.) across `reverse-engineering/apk/<version>/jadx-output/` and `apktool-output/` for the
   current APK version in `reverse-engineering/APK_VERSIONS.md`, looking for anything that could
   explain DLCI 0x08's "GSND CONTROL"/DLCI 0x0a's "GSND AUDIO" SDP service names
   (`PROTOCOL.md` §2.3's 2026-08-30 update). Record the search commands and results — including a
   clean negative — in `REVERSE_ENGINEERING.md`, following its own existing template.
2. Attempt once more to trace `gjv.p()`'s own caller (`REVERSE_ENGINEERING.md`'s `frb`/`fuh`/`glk`/
   `gjv` entry, `PROTOCOL.md` §6) — two prior static-analysis passes failed; if a materially
   different search strategy doesn't present itself quickly, do not sink further time into a third
   attempt with the same method. Record whatever was tried and found (including "still not found,
   third attempt, no new strategy available").
3. Propose (do not commit) any new `REVERSE_ENGINEERING.md` entry or `PROTOCOL.md` open-question
   update this surfaces, clearly labeled as a proposal per ADR-017.

## Final phase — Document consistency sweep and wrap-up

This is mandatory even if every phase above felt complete in passing. Explicitly re-check and update
each of these six documents so none of them is stale relative to this session's work — for each,
either make the update or explicitly note "checked, no change needed":

- **`PROTOCOL.md`** — §8 changelog entry for this session's work; §4.5.7 stays exactly as-is (still
  PROPOSAL) unless the maintainer answered phase 4 inline; §4.4/§6 Behavior updated only if the
  maintainer answered phase 5 inline.
- **`TODO.md`** — check off phase 3's note, the Android-Studio-project-setup checkbox (Phase 4 app
  dev's own list) for whatever phase 7 actually completed, and record the DI/Case-both decisions'
  status (still open, or resolved with a pointer to the new `DECISIONS.md` ADR) — do not leave stale
  unchecked boxes for work phase 7 actually finished, and do not check boxes for work it didn't.
- **`DECISIONS.md`** — no new ADR committed unless the maintainer explicitly approved one inline
  during this conversation; if so, add it now following the existing template and register it in
  `id_registry.csv`.
- **`REVERSE_ENGINEERING.md`** — phase 8's search results recorded (including clean negatives), per
  its own existing template.
- **`CAPTURE_BLUETOOTH_HCI_SNOOP.md`** — confirm §9's Capture Index rows for `CAP-036`/`CAP-037`
  still accurately summarize those captures after phases 1/2's count corrections; update the
  one-line outcome text if it quoted the old count.
- **`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`** — confirm no Test-ID's Evidence-column pointer or status
  text is now stale because of phases 1/2/4's changes; update if so, otherwise note "checked, no
  change needed."

Set `Status: awaiting maintainer sign-off` if any of phases 4/5/6 are still pending an answer (which
is expected on a first pass), or `complete` only if every phase resolved cleanly with no pending
decision. In the **chat response** (not only the RESULT file), give the maintainer:
1. A compact status of what was completed (phases 1/2/3/7/8).
2. The exact decisions still needed (phases 4/5/6), each with its recommendation, ready for a direct
   answer.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0013_FEATURE_PROMPT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0013_FEATURE_PROMPT_2026_09_13
