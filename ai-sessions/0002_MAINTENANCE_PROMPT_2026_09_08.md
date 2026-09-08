# 0002_MAINTENANCE_PROMPT_2026_09_08.md — Session logging rule additions, 0001 sign-off implementation, and V1 protocol-readiness gap scan

**Number:** 0002
**Category:** MAINTENANCE
**Date:** 2026-09-08
**Title:** Session logging rule additions, 0001 sign-off implementation, and V1 protocol-readiness gap scan

---

## Mandatory reading order (do this first, in this order)

Per `AGENTS.md` §0.1 and the new rule this task itself adds (Task 1 below), read, in order, before
taking any other action:

1. `AGENTS.md` (full)
2. `PROJECT.md` (full)
3. `PROJECT_RULES.md` (full)
4. `DECISIONS.md` (every ADR, ADR-001 through the most recent — do not skip to only the latest few)
5. `ARCHITECTURE.md`
6. `PROTOCOL.md` (full, including its own changelog/open-items sections)
7. `TODO.md`
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md` (this task edits the former and adds an
   entry to the latter)
9. `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` in full (Task 3 below implements its Phase 4
   proposals)

## AI-assistance boundary — read before starting Task 3 specifically

Per `AGENTS.md` §6/§15 and `DECISIONS.md` ADR-017: ordinarily an AI session may only *propose* a
🟢 FACT promotion or an ADR write/amendment, never commit one, absent explicit maintainer sign-off
given for that specific item. **Task 3 below is the documented exception, not a bypass of that
rule**: the maintainer read `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`'s Phase 4 proposals
and explicitly approved all of them, in the chat session on 2026-09-08 that authored this prompt
(the same maintainer identified throughout this project's `git` history as `tedsluis`). That
approval is what Task 3 executes — it is not license to promote or commit anything beyond the
specific items 0001's Phase 4 section names. If implementing any one of them surfaces a doubt, a
contradiction with something read above, or content that has drifted since 0001 was written, stop
and flag it instead of proceeding on your own judgment — same standard as everywhere else in this
project.

---

## Task 1 — `AI_SESSION_LOG_PROCEDURE.md`: require the mandatory reading order in every logged prompt

Add a new, clearly numbered rule to `AI_SESSION_LOG_PROCEDURE.md` stating that every prompt logged
under this convention must itself state — and its executing session must follow — `AGENTS.md`
§0.1's reading order at minimum: the executing AI session must read `AGENTS.md`, `PROJECT.md`,
`PROJECT_RULES.md`, and `DECISIONS.md` before taking any other action, regardless of what the
prompt's own task-specific instructions say. State this as binding on the *prompt author* (every
`NNNN_CATEGORY_PROMPT_YYYY_MM_DD.md` file's body must include this reading order explicitly, in
the same form as this file's own section above) and on the *executing session* (do the reading before proceeding, even if a prompt's author forgot
to restate it). Cross-reference `PROJECT_RULES.md` rule 13/13a and `AGENTS.md` §0.1 rather than
duplicating their content inline.

## Task 2 — `AI_SESSION_LOG_PROCEDURE.md`: define how a RESULT's `Status` field gets updated later

Add a new rule to `AI_SESSION_LOG_PROCEDURE.md`, immediately after §4's `Status` field definition,
stating: a `RESULT` file's `Status` field is not frozen at the value it was given when first
written — a **later** logged session (a new `NNNN_CATEGORY_PROMPT_YYYY_MM_DD.md`/
`NNNN_CATEGORY_RESULT_YYYY_MM_DD.md` pair) may update an earlier entry's `Status` field in place, when and only when the specific condition that value
represents has actually been met in that later session:

- `awaiting maintainer sign-off` → `complete` (or a status reflecting whatever new work followed)
  once the maintainer has actually reviewed and signed off on the pending proposals — the later
  session's own `RESULT` file must cite where/how that sign-off happened (e.g. "approved by the
  maintainer in the chat session that authored prompt NNNN," or a specific `DECISIONS.md` ADR
  number it produced).
- `partial — resumed` → `complete` (or a further `partial — resumed`) once the interrupted task
  actually finishes or is picked up again.

State explicitly: this is an *update to the earlier file's header field*, not a new duplicate
entry — the earlier `NNNN_CATEGORY_RESULT_YYYY_MM_DD.md` file itself is edited in place, and
`ai-sessions/INDEX.md`'s row for that same `NNNN` is updated to match. The later session's own new
pair (its own `NNNN`) is what records *that the update happened and why*; it does not re-narrate the
earlier file's substantive content, matching the spirit of `PROJECT_RULES.md` rule 9a (a reference
document is not an accumulating changelog).

## Task 3 — Implement 0001's approved Phase 4 proposals

`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`'s Phase 4 section ("Write-up (proposals, none
committed)") lists four groups of proposed changes. **The maintainer has approved all of them** (see
the AI-assistance-boundary note above) — implement each, in the target file its own proposal names:

1. **`PROTOCOL.md` §6** — add the four proposed items: (a) `IFastPairDeviceDetailService`/
   `IFastPairFmdProxyService` as informational, out-of-scope-for-this-project's-own-implementation
   context on how the official app sources battery data and handles Find My Device consent; (b) the
   Find My Buds Case/"both" refinement (`FmdWorker`/`ijp` only construct ToS accept/skip requests,
   no ring/play-sound trigger found anywhere in the decompiled source); (c) the new open item on
   `MaestroDeviceSettingsProviderService` as a second, previously-undocumented UI entry point into
   the `qhr`/`WriteSetting` pipeline; (d) the new open item on `MaestroEndpointService`'s
   undetermined gRPC service registrations.
2. **`REVERSE_ENGINEERING.md`** — add the five proposed entries: the GMS Fast Pair client-library
   boundary (`ijk`/`ijp`/`ijm`/`iji`/`gsy`/`TrueWirelessHeadset`/`HeadsetPiece`/`FmdRequest`/
   `FmdResponse`/`FmdWorker`, with the file:line citations already written into 0001's Phase 1
   section); the AOSP settings-extension boundary (`MaestroDeviceSettingsProviderService`/`fhk`/
   `ges`); the `frb`/`fuh`/`glk`/`gjv` `GetSoftwareInfo`-trigger structure (Phase 2's Q4 write-up);
   the `qhr` field 11 (Multipoint) and field 15 (Volume EQ) full call-graph traces, including the
   field-5/`fyo.i()` no-op note; the `MaestroEndpointService` open-questions note.
3. **`TODO.md`** — close the "apply ADR-019's static-analysis method to `qhr` fields 11 and 15"
   item; add the `MaestroDeviceSettingsProviderService` case-ID-to-`qhr`-field forward-trace item;
   add the `MaestroEndpointService` smali-fallback-read item.
4. **`DECISIONS.md`** — add 0001's drafted ADR-025 Update note (quoted in full in 0001's Phase 4),
   dated for the day this task actually runs, with a line noting maintainer approval was given in
   the chat session that authored prompt `0002` (this file). Because Multipoint (`qhr` field 11) and
   Volume EQ (`qhr` field 15) reach full-identity promotion under this same maintainer approval,
   also promote their `PROTOCOL.md` entries (§4.5.2, §4.5.6) from HYPOTHESIS to 🟢 FACT accordingly,
   and record that promotion as its own dated line in the same or an adjacent ADR-025 Update — do
   not silently upgrade a confidence marker without a traceable `DECISIONS.md` line, per
   `PROJECT_RULES.md` rule 4.

After implementing all four groups: update `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`'s own
header `Status` field from `awaiting maintainer sign-off` to `complete` (per Task 2's new rule —
cite this prompt, `0002`, as where the sign-off happened), and update its row in
`ai-sessions/INDEX.md` to match.

## Task 4 — Gap scan: what's still needed before `PROTOCOL.md` is complete enough for actual V1 app development

Produce a structured overview — as a clearly delimited section in this task's own `RESULT` file, not
scattered across other documents — of every category of work still outstanding before `PROTOCOL.md`
is complete enough to start real V1 app implementation. Base this on an actual read of `PROTOCOL.md`'s
own 🔴/🟡 markers, `TODO.md`'s current task list (including this task's own Task 3
additions/closures above), `DECISIONS.md`'s open items, and `README.md`'s "Still open" list — not a
generic guess. Organize the overview by work *type*, at minimum covering:

- **Captures still needed** — which planned-but-not-yet-run `CAP-NNN` sessions (per
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index) close a currently-open protocol question, and
  which open questions have no capture planned for them yet at all.
- **APK reverse-engineering still needed** — concrete, named next steps against the decompiled
  companion app (e.g. items this task's own Task 3 just added to `TODO.md`, plus any other
  `REVERSE_ENGINEERING.md`/`TODO.md` items not yet closed), scoped per `AGENTS.md` §4/ADR-017's
  AI-assistance boundary.
- **Cross-checks still needed** — capture-vs-APK correlation passes not yet done, mirroring what
  `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` itself did for the items it covered.
- **Consistency checks still needed** — internal-document consistency gaps (e.g.
  `scripts/lint_docs.py` findings, `PROJECT_RULES.md` rule 9a violations, stale cross-references)
  distinct from protocol-content gaps.
- **Validation against outside sources** — anywhere a finding currently rests only on this project's
  own captures/APK reading and would benefit from checking against public documentation (Fast Pair /
  HFP / HDLC / Pigweed specs, `qzed/pbpctrl`'s own findings per `AGENTS.md` §12/§14) without
  violating the no-copying rule.
- **Plain documentation updates** — file-maintenance items (e.g. `README.md`'s "Current state"
  section, `CHANGELOG.md`) that don't require new research but are stale relative to this task's own
  Task 3 changes.
- **Anything else** — any category of remaining work this scan surfaces that doesn't fit the above,
  named explicitly rather than forced into one of these buckets.

For each item: name it concretely (file/section/`CAP-NNN`/Test-ID as applicable), state why it
blocks V1-readiness specifically (not just "would be nice to know"), and note whether it's a
research task an AI session can help with (per the AI-assistance boundary) or a maintainer-only call
(hardware access, a product-scope decision, an actual sign-off). Do not propose a priority ranking
beyond what `TODO.md`'s existing "Recommended priority order" already establishes unless this scan
finds a genuine gap in that ordering — if so, flag it as a proposal for the maintainer, not a
decision.

---

## Guardrails

- Tasks 1–2 are documentation/process changes (no FACT/ADR sign-off gate applies, same as the
  original `ai-sessions/` convention itself, per `AGENTS.md` §6/§15's scope).
- Task 3 is the one place in this prompt where `PROTOCOL.md`/`DECISIONS.md` are actually edited with
  FACT/ADR content — do so only for the specific items 0001's Phase 4 names, exactly as approved; do
  not use this as an opportunity to also promote or edit anything else encountered along the way,
  even if it looks similarly well-evidenced — flag anything else found as a new proposal instead,
  same as any other session would.
- Task 4 produces a report, not new commitments — do not implement any of the gap-scan's findings in
  this same session; that is future work for a session logged separately under its own number.
- Log this session itself: once the work is done, write this prompt's own paired RESULT file
  (its path follows directly from this file's own name and `AI_SESSION_LOG_PROCEDURE.md`'s naming
  scheme — check `ai-sessions/INDEX.md` first in case a later number was assigned to something else
  in the meantime) with a `Status` of `complete` if all four tasks finished, or `partial — resumed`
  if not, per `AI_SESSION_LOG_PROCEDURE.md` — including Task 2's own new rule.

## Output

List every file created or changed for Tasks 1–3, quote the new/updated `DECISIONS.md` ADR-025
Update note and the two FACT-promotion lines in full, and include Task 4's full gap-scan overview in
the `RESULT` file.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0002_MAINTENANCE_PROMPT_2026_09_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0002_MAINTENANCE_PROMPT_2026_09_08
