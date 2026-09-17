# 0027_MAINTENANCE_PROMPT_2026_09_17.md — Trace `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY`'s senders; advance items M, B, and H's remaining leads

**Number:** 0027
**Category:** MAINTENANCE
**Date:** 2026-09-17
**Title:** Trace who sends `BluetoothPriorityReceiver`'s `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY` broadcast within the companion app's own code; run item M's resource/string-table sweep; build `structural_index`'s deferred `implements`-query and use it (plus a field-write search) on item B's Dagger-multibinding assembly site; trace at least one of item H's 7 new schema leads or its 17-class unattributed naming cluster one level further

---

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new Claude Code chat**, including on a
resumption. Before anything else: check whether `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md`
already exists.

- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says which
  phases are already `done`. Skip straight to the first phase not yet marked `done` and continue from
  there — do not redo a `done` phase's work. Regardless of where you resume, you still owe the full
  "Mandatory reading order" below in this session — `AI_SESSION_LOG_PROCEDURE.md` §8 requires it for
  every session that acts on this repo, resumption or not, since a new chat has no memory of a
  previous one.

Every phase boundary is a safe stopping point: before ending a turn, update
`0027_MAINTENANCE_RESULT_2026_09_17.md`'s status table and `Status` header field
(`partial — resumed` while incomplete) so the next session picks up cleanly.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §4/§6 (mechanical-assistance boundary, `DECISIONS.md` ADR-017;
   never independently promote a FACT or write a `DECISIONS.md` ADR) and §13.6 (zero-creativity,
   evidence-only rule).
2. `PROJECT.md`, `PROJECT_RULES.md` (full), `ARCHITECTURE.md` (full).
3. `DECISIONS.md` — full ADR history, at minimum ADR-017 and ADR-025.
4. `PROTOCOL.md` §6 in full.
5. `TODO.md` in full, especially Phase 2.
6. `REVERSE_ENGINEERING.md` **in full, start to finish** — this project's entire accumulated APK
   knowledge. Pay special attention to the `BluetoothPriorityReceiver` entry (added 2026-09-16, the
   direct subject of Phase 1 below), the `MaestroEndpointService` entry and its 2026-09-16 `update`
   (item B, Phase 3 below), and the "Candidate rich schemas outside this pass's traced call graph"
   section's full 2026-09-16 update (item H, Phase 4 below).
7. All 5 existing tools' own SPEC.md and README.md in full:
   `reverse-engineering/tools/lambda_dispatcher_resolver/`,
   `reverse-engineering/tools/structural_index/` (**especially** its own §2.2/"Explicitly deferred to
   a later version" note on the `implements`-query — the direct input to Phase 3 below),
   `reverse-engineering/tools/schema_batch_extractor/`, `reverse-engineering/tools/uuid_ble_context/`,
   `reverse-engineering/tools/limited_dataflow/`.
8. `reverse-engineering/tools/BACKLOG.md` in full (current state — all 4 top-priority tools already
   marked implemented).
9. `APK_REVERSE_ENGINEERING_PROCEDURE.md` in full.
10. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
11. `ai-sessions/0024_AUDIT_RESULT_2026_09_16.md`, `ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md`,
    and `ai-sessions/0026_MAINTENANCE_RESULT_2026_09_17.md` in full — the three most recent sessions,
    all now `Status: complete`, whose open findings this prompt directly continues.

## Context (from the maintainer, not re-derived here)

The maintainer asked, in their own words (translated from Dutch, preserved in full below the
horizontal rule at the end of this prompt), to create a prompt in `ai-sessions/` that:

1. Traces the callers/senders of `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY`.
2. Investigates item M (a resource/string-table sweep); item B (finding the Dagger-multibinding
   assembly site, which waits on `structural_index`'s deferred `implements`-query); and traces
   item H's 7 new schema leads or its 17-class unattributed naming cluster further.

**Why this specific set, and why now.** All four are open items this project's own record already
names as the concrete next static-analysis steps, per `ai-sessions/0026`'s own maintainer-approved
closeout: `BluetoothPriorityReceiver` (item L) is flagged there as "the single highest-leverage open
lead," with an explicit open question already on record in `REVERSE_ENGINEERING.md` itself (its own
"Open questions" bullet: *"what actually sends the `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY`
broadcast (in-app self-broadcast, or an external sender)"*) — Phase 1 below answers exactly that
question, as far as static analysis of this app's own code can. Items M, B, and H are the next-cheapest,
tool-supported or tool-adjacent items named in that same closeout.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md` with the header block required
by `AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with one
row per phase (0–6, see below). This prompt does not instruct automatic git commits — mention clearly,
at the end of each session's final turn, what is uncommitted.

## Phase 0 — Setup

Complete the mandatory reading order above. Create `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md`
with the required header and an empty phase-status table. Re-confirm the decompiled APK version on
disk still matches `reverse-engineering/APK_VERSIONS.md`'s row (`v1.0.955078536-10253511`) — flag and
do not re-decompile if it has changed. Re-run all 5 existing tools' test suites
(`.venv/bin/python3 -m pytest tests/ -v`, from each tool's own directory under
`reverse-engineering/tools/`) and confirm every one is still green before relying on or extending any
of them.

## Phase 1 — Who sends `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY`?

`REVERSE_ENGINEERING.md`'s `BluetoothPriorityReceiver` entry (added 2026-09-16) confirms the receiver
itself — package `com.google.android.apps.wearables.maestro.companion.phone.bluetoothpriority`,
exported, action `com.google.android.apps.wearables.maestro.companion.ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY`,
extras `EXTRA_BD_ADDR`/`EXTRA_PRIORITY`/`EXTRA_DATA_DIRECTION`, a direct field of the already-catalogued
`fzd` (`InternalRfcommUuidRegistry`) — but explicitly leaves open *who sends this broadcast*.

1. Search the whole decompiled tree (`jadx-output/sources/`, falling back to `apktool-output/` smali
   if JADX misses something) for every occurrence of the literal action string
   `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY` (both the fully-qualified form and any locally-built
   `"com.google.android.apps.wearables.maestro.companion." + "ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY"`-style
   construction) and for the three extra-key strings (`EXTRA_BD_ADDR`, `EXTRA_PRIORITY`,
   `EXTRA_DATA_DIRECTION`), outside `BluetoothPriorityReceiver.java` itself and the manifest.
2. Search for any `Intent`/`PendingIntent` construction, `sendBroadcast`/`sendOrderedBroadcast`/
   `startService`/`startForegroundService` call, or `IntentFilter` registration anywhere else in the
   app that could plausibly target this receiver or action (by component name, not just the action
   string, in case the string itself is built dynamically or obfuscated differently at the call site).
3. If a genuine in-app sender is found: trace it — what triggers *it* (a UI action, a system broadcast
   receiver of its own, a scheduled job), and record the full chain in
   `REVERSE_ENGINEERING.md`'s `BluetoothPriorityReceiver` entry as a dated update, labeled
   FACT/HYPOTHESIS/OPEN QUESTION per `PROJECT_RULES.md` §1.
4. **If no in-app sender is found anywhere in this app's own decompiled code**: this is a legitimate,
   recordable checked-negative result, not a failed search — record it explicitly as such (per
   `AGENTS.md` §13.6, an unresolved search stopped after an honest, thorough attempt is not the same
   as an unexamined gap; state exactly what was searched and how). This would be strong evidence the
   actual sender lives outside this app (the Android Bluetooth stack itself, or another
   Google/system component) — note this reading explicitly as a 🟡 HYPOTHESIS, not a FACT, since
   static analysis of *this* app's own code cannot positively confirm an external sender, only fail to
   find an internal one. **Do not attempt to resolve this further via a new Bluetooth capture as part
   of this prompt** — per this prompt's own Guardrails, that decision belongs to the maintainer, not
   this session; flag it clearly as the natural next step if the maintainer wants to pursue it (e.g.
   bracketing a capture around whatever real-world action might plausibly trigger a connection-priority
   change — a multipoint switch, a call starting/ending, or a third-device connection) rather than
   attempting to guess a trigger without evidence.

## Phase 2 — Item M: resource/string-table sweep

None of the 5 existing tools cover this data source (`reverse-engineering/tools/structural_index/SPEC.md`
§2.2 explicitly
defers it; the others are DEX/bytecode-only). This is a manual/`grep`-driven pass over
`reverse-engineering/apk/v1.0.955078536-10253511/apktool-output/res/` (string resources, XML resource
files — not the DEX/JADX tree):

1. Search `res/values*/strings.xml` (and any other `values*` resource files) for anything
   Bluetooth/RE-relevant not already catalogued: log tags, notification channel names, permission
   rationale text, feature-flag-shaped names, anything referencing "priority," "classic," "connection,"
   or other terms this project's own keyword list (`APK_REVERSE_ENGINEERING_PROCEDURE.md` §4) already
   uses.
2. Apply the same exclusion list `APK_REVERSE_ENGINEERING_PROCEDURE.md` §4.1 already names
   (AccountLinking/OwnershipTransfer/AccessoryNonOwner/Firebase-Analytics-Crashlytics) — skip past
   these, don't investigate them.
3. Cross-reference every candidate string against `REVERSE_ENGINEERING.md`'s existing catalogue before
   recording it as new (the same "cross-reference before writing a new entry" workflow
   `ai-sessions/0024`'s own Phase 4 named).
4. Record findings (or a genuine "swept, nothing new found" checked negative) in
   `REVERSE_ENGINEERING.md`, labeled per `PROJECT_RULES.md` §1.

## Phase 3 — Item B: the `MaestroEndpointService` Dagger-multibinding assembly site

`REVERSE_ENGINEERING.md`'s `MaestroEndpointService` entry (2026-09-16 update) already narrows this to
two named, not-yet-tried approaches:

1. **Build `structural_index`'s deferred `implements`-query** ("list every class implementing
   interface `X`" — `reverse-engineering/tools/structural_index/SPEC.md` §2.2's own explicitly-deferred
   scope item). This is a small, additive extension to an already-shipped, tested tool — add it as a
   new CLI subcommand (e.g. `implementers --interface <name>`) reusing the same `androguard_index.py`
   Layer 1 the tool already has, with its own pytest coverage (skip-without-APK, regression-tested
   against a known-true case first, matching every other tool's own discipline in this project).
   Update `reverse-engineering/tools/structural_index/SPEC.md` and README.md to document the new
   capability; this does not need a whole new tool directory or its own entry in
   `reverse-engineering/tools/BACKLOG.md` — it graduates an already-named deferred
   feature of an existing tool.
2. **Use it** to enumerate every Dagger `@Provides`/`@Binds`/`@IntoMap`-shaped generated factory class
   in the app, looking for whatever interface/key type underlies `MaestroEndpointService`'s own
   `Map<String, Optional<ofd>>` service registration (`MaestroEndpointService.b`, per the existing
   entry's own citation).
3. **Also try the second named approach**: a field-*write* search (as opposed to `structural_index`
   v1's existing field-*type-declaration* search) for `MaestroEndpointService.b`'s own setter — i.e.
   who actually writes a value into that field, not just who declares a field of its type. This may be
   achievable with `structural_index`'s existing v1 capability plus manual smali reading, or may need
   its own small extension — use judgment, document whichever path is taken.
4. Record the result — resolution, a further checked negative, or a genuinely new narrowing — in
   `REVERSE_ENGINEERING.md`'s `MaestroEndpointService` entry as a dated update, per `PROJECT_RULES.md`
   §1. If the assembly site is still not found after both approaches, say so plainly rather than
   forcing an inconclusive result into a false resolution.

## Phase 4 — Item H: trace at least one new lead or naming-cluster member further

`REVERSE_ENGINEERING.md`'s "Candidate rich schemas" section names two untraced sets from the 2026-09-16
`ai-sessions/0025` pass:

- **7 new schema leads**: `mqm`, `mra`, `mqk`, `mtg`, `qak`, `qaz`, `qam` — each surfaced via
  `schema_batch_extractor`'s oneof/list/map reference graph, none traced past its own discovery.
- **A 17-class unattributed field-holder naming cluster**: `kii`/`koq`/`pzr`/`jau`/`msc`/`jaj`/`jjn`/
  `jjx`/`jkl`/`jsg`/`kip`/`kiq`/`kob`/`kol`/`qan`/`fwe`/`nbm` — plausibly (unconfirmed) a bundled
  feedback/diagnostics subsystem, per that section's own explicit, labeled guess.

Trace **at least one, up to a handful** of these (do not attempt all 24 — per `AGENTS.md` §13.6, trace
what's promising one level further and stop, recording the rest as still-open rather than blowing this
phase's own scope): read the actual class source for whichever candidates are chosen, using
`structural_index refs`/`schema_batch_extractor refs` to find their own callers/references first, then
reading the resulting call sites directly for a self-describing log message, string literal, or
class/method name that narrows what the class is for. Record findings (resolution, narrowing, or an
honest "read it, still don't know" open question) in `REVERSE_ENGINEERING.md`'s "Candidate rich
schemas" section as a dated update.

## Phase 5 — Cross-checks and write-up

- Cross-reference every finding from Phases 1-4 against `PROTOCOL.md`'s full text, `DECISIONS.md`'s
  full ADR history, `DESKRESEARCH_FINDINGS.md`, `TODO.md`, and any `CAP-NNN-FINDINGS.md` files that
  actually exist (not placeholder/not-yet-captured directories) — flag, do not silently resolve, any
  contradiction found.
- Update `REVERSE_ENGINEERING.md` (all findings, each labeled per `PROJECT_RULES.md` §1),
  `reverse-engineering/tools/structural_index/SPEC.md`/`README.md` (the new `implements`-query, if
  built), `TODO.md` (closing out any item this session actually resolved), and `ai-sessions/INDEX.md`.
- Every proposed protocol-level finding is labeled awaiting maintainer sign-off (`AGENTS.md` §6/§15) —
  nothing is promoted to 🟢 FACT, no `DECISIONS.md` ADR is written or altered. (Mechanical code-
  existence/structure findings, e.g. "class X implements interface Y," can be 🟢 FACT on their own
  terms, same as every prior session here — the distinction is protocol-*behavior* claims, not code
  facts.)

## Phase 6 — Wrap-up and maintainer summary

- Re-read every file this session modified; confirm every substantive claim cites a file+line or frame
  reference (`PROJECT_RULES.md` rule 3).
- Run `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py`; fix anything this session's own
  edits introduce (a pre-existing, already-noisy finding elsewhere is not this session's
  responsibility).
- Update `ai-sessions/INDEX.md`'s row for `0027` to match this session's final `Status`.
- Finalize `0027_MAINTENANCE_RESULT_2026_09_17.md`: `Status: complete` only if every phase is actually
  done and no proposal is pending review; otherwise `awaiting maintainer sign-off` or
  `partial — resumed`.
- Write a final summary for the maintainer, answering all four points from the Context section
  directly: (1) who sends `ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY`, or the checked-negative result
  and its capture-territory implication if no sender was found; (2) item M's sweep result; (3) item B's
  status after both named approaches; (4) what was found tracing item H's leads/cluster, and what
  remains untraced.

## Guardrails

- **Scoped to the companion app's own decompiled output only**
  (`reverse-engineering/apk/v1.0.955078536-10253511/`, or whatever version is actually on disk per
  Phase 0) — never Google Play Services, per `DECISIONS.md` ADR-025.
- **Never independently promote a protocol-behavior finding to 🟢 FACT in `PROTOCOL.md`, and never
  write or alter a `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15).
- **No sampling, no assumptions.** A search that comes back empty is recorded as a checked negative
  with what was actually searched, not silently dropped or filled in with a guess (`AGENTS.md` §13.6).
- **No new Bluetooth capture or hardware action is performed as part of this prompt** — including for
  Phase 1's own open question. If static analysis genuinely cannot resolve who sends the
  connection-priority broadcast, the deliverable is a clear, evidence-based flag for the maintainer's
  own capture-planning decision, not a capture itself.
- **No decompiled APK content is ever committed**, in the `structural_index` extension's source, tests,
  or fixtures, or anywhere else — tests read from the maintainer's local, gitignored
  `reverse-engineering/apk/<version>/` tree and skip themselves when it's absent.
- Every proposed protocol-level finding is labeled awaiting maintainer sign-off; mechanical code-facts
  may be recorded as 🟢 FACT on their own terms, same as this project's existing practice.

## Output

At the end of each phase, a short note (in the RESULT file, and to the maintainer if the chat is still
live) of what was found/changed. At the end of Phase 6, the full summary described there.

---

**Original maintainer instruction (Dutch, preserved verbatim for exact phrasing):**

> Maak een prompt voor claude code in het engels in ai-sessions/ die: 1) de aanroepers van
> ACTION_TRIGGER_CLASSIC_CONNECTION_PRIORITY opspoort. 2) Onderzoek item M — een string/resource-sweep;
> item B — de Dagger-multibinding-plek vinden, wacht op structural_index's uitgestelde
> implements-query; de 7 nieuwe schema-leads of het cluster van 17 onbekende klassen uit item H verder
> natrekken.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0027_MAINTENANCE_PROMPT_2026_09_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0027_MAINTENANCE_PROMPT_2026_09_17
