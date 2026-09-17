# 0029_MAINTENANCE_PROMPT_2026_09_17.md — Compile and record maintainer sign-off on `ai-sessions/0027`'s three post-`0028` continuation findings

**Number:** 0029
**Category:** MAINTENANCE
**Date:** 2026-09-17
**Title:** Compile a plain-language sign-off list for the three code-level findings `ai-sessions/0027` accumulated *after* `ai-sessions/0028` already recorded sign-off on its earlier findings (item M's `fpz` case-5 "device type == 1" trace, item H's `fwe`/`fwk` 4th sub-message pool, item B's `onBind()` real-vs-inert Binder resolution); present it to the maintainer; record whatever decision is given

---

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new Claude Code chat**, including on a
resumption. Before anything else: check whether `ai-sessions/0029_MAINTENANCE_RESULT_2026_09_17.md`
already exists.

- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. If its `Status` header field is already `complete`, this
  task is done — do not repeat it. If `partial — resumed`, continue from wherever its own notes leave
  off.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §6 (never independently promote a 🟢 FACT protocol-behavior claim or
   write a `DECISIONS.md` ADR without maintainer approval) and §13.6 (zero-creativity, evidence-only).
2. `PROJECT.md`, `PROJECT_RULES.md` (full), `ARCHITECTURE.md` (full).
3. `DECISIONS.md` — full ADR history.
4. `PROTOCOL.md` §6 in full.
5. `TODO.md` in full.
6. `AI_SESSION_LOG_PROCEDURE.md` §4a specifically (updating an earlier `RESULT` file's `Status` field
   in place once a sign-off actually happens) and `ai-sessions/INDEX.md`.
7. `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md` **in full** — this is the file whose three
   newest subsections this prompt is about. Pay specific attention to the three subsections headed
   `### Further continuation, same session` that appear **after** the text confirming `ai-sessions/0028`'s
   own sign-off was already recorded (i.e. the subsections titled around "item M's own true final
   thread…", "item H's actual 4th remaining pool…", and "item B's own last remaining open thread…") —
   these three are the ones `ai-sessions/0028` did **not** cover, since they were written after it.
8. `ai-sessions/0028_MAINTENANCE_RESULT_2026_09_17.md` in full — so the new list this session compiles
   is demonstrably the *complement* of what `0028` already got sign-off on, not a re-ask of the same
   ground.
9. `REVERSE_ENGINEERING.md`'s three corresponding entries in full: the "Resource/string-table sweep"
   section's newest dated update (item M), the "Candidate rich schemas outside this pass's traced call
   graph" section's newest dated update (item H), and the `MaestroEndpointService` entry's newest dated
   update (item B).

## Context (from the maintainer, not re-derived here)

In the same chat session that produced `ai-sessions/0027`/`0028`, the maintainer asked the assistant to
continue tracing items M, B, and H's remaining leads several more times *after* `ai-sessions/0028`
already recorded sign-off on the findings that existed at that point. Three more substantive,
evidence-backed findings resulted, each committed and pushed separately:

1. **Item M — the `fpz` case-5 "device type == 1" condition.** Traced to the connected earbuds' own
   `GetHardwareInfo` response (`qiv` field 1), with the ordinal's own human-readable name confirmed as
   a second, independently-exhausted static-analysis dead end (same character as the earlier
   feature-index-6 finding). Commit `a497df1`.
2. **Item H — a 4th, separate pool of 28 previously-uncatalogued leads** (`fwe.java`'s own 24
   private byte-stream parser methods, plus 4 of `fwk`'s remaining logged outputs) that earlier
   "item H closed" claims never actually covered. 10 resolved to concrete Bluetooth Classic
   connection-quality/crash/OTA-status telemetry field names via self-describing exception/log text;
   18 explicitly left open, not glossed over. Commit `ed90cd5`.
3. **Item B — whether `onBind()`'s returned Binder object is real or inert.** Resolved: real. Traces
   the exact value to a genuine `android.os.Binder` subclass (`oge`) with a live UID-based
   authorization handler, and independently confirms this transport-construction chain never reads the
   already-known-empty service map at all (sharpening the existing "vestigial" finding). Commit
   `40e1c02`.

**Why this needs its own entry.** `AI_SESSION_LOG_PROCEDURE.md` §4a's own convention: a `RESULT` file's
`Status` can only move from `awaiting maintainer sign-off` (or be otherwise marked reviewed) once the
maintainer has actually reviewed and signed off, and the later session recording that must cite where/
how it happened. `ai-sessions/0028` cannot serve as that citation for these three findings, since they
did not exist yet when `0028` was written — this prompt closes that gap, following the exact same
precedent `0026`→`0028` already set for an earlier round of the same pattern.

## What "sign-off" means here, precisely — and what it does not mean

Per `AGENTS.md` §6/§15 and `PROJECT_RULES.md` §1, matching `0026`'s and `0028`'s own already-established
framing exactly:

- **Does** mean: the maintainer has reviewed and accepts these three findings as accurately recorded
  and worth keeping in `REVERSE_ENGINEERING.md` at their current, already-assigned evidence tier.
- **Does not** mean: promoting any 🟡 HYPOTHESIS or 🔴 OPEN QUESTION item to 🟢 FACT, or writing/altering
  any `DECISIONS.md` ADR. All three findings above are already-labeled 🟢 FACT **mechanical
  code-existence/structure facts** (a literal field assignment, a literal class-extends relationship, a
  literal exception-message string) — none is a protocol-behavior/wire claim, so per this project's own
  established convention (`ai-sessions/0025`'s precedent, reaffirmed by `0026`/`0028`) none of them
  *strictly* requires this gate in the first place. This sign-off is sought as good practice/
  transparency, the same reasoning `0026`/`0028` already gave, not because `AGENTS.md` §6 demands it
  here.

## Task

1. Re-read the three findings (per the reading-order step 7/9 above) and compile a short,
   plain-language list — one entry per finding, each with a one-sentence summary and its
   `REVERSE_ENGINEERING.md` section/file+line location — suitable for a non-AI-agent maintainer to read
   and approve or push back on, mirroring the exact list format `ai-sessions/0026`'s own context
   section used for its list A/list B split (here there is only one list; nothing in these three
   findings is a HYPOTHESIS/OPEN QUESTION relabeling, so no second list is needed — confirm this is
   actually true while re-reading, and flag it plainly if it turns out not to be).
2. Present that list to the maintainer directly in the chat.
3. Record whatever decision the maintainer gives — full approval, partial approval, or requested
   changes — **verbatim or a faithful paraphrase if the exchange was conversational**, exactly as
   `ai-sessions/0026`/`0028` already did for their own maintainer exchanges. Do not assume approval;
   wait for and record the actual answer.
4. If approved: update `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md`'s own `Status` field only if
   it is not already `complete` (check first — it may already be, since these were "bonus" continuations
   within an already-`complete` file, matching that file's own stated convention). If any part of the
   list is *not* approved as-is, record the maintainer's specific objection and do **not** edit
   `REVERSE_ENGINEERING.md` to silently resolve it — flag it as a follow-up item instead, per
   `PROJECT_RULES.md` §3.
5. Update `ai-sessions/INDEX.md` with a new row for `0029`.

## Guardrails

- **Do not fabricate or assume the maintainer's decision.** If this prompt is run non-interactively
  with no way to actually reach the maintainer, stop after step 2 and mark the `RESULT` file's `Status`
  as `partial — resumed`, clearly stating the list is compiled and awaiting the maintainer's actual
  answer — do not write a fabricated "approved" outcome.
- **Never independently promote a finding to 🟢 FACT in `PROTOCOL.md`, and never write or alter a
  `DECISIONS.md` ADR**, regardless of how the sign-off conversation goes (`AGENTS.md` §6/§15).
- This is a short, single-purpose entry — do not re-open or re-litigate item M/B/H's own substantive
  conclusions here; that work is already done and cited above.

## Output

`ai-sessions/0029_MAINTENANCE_RESULT_2026_09_17.md` documenting: the compiled list, the maintainer's
actual response, and the resulting `Status` updates (if any) to `ai-sessions/0027`'s own header field.
Updated `ai-sessions/INDEX.md`. A short note to the maintainer at the end of the turn confirming what
was recorded.

---

**Original maintainer instruction (Dutch, preserved verbatim for exact phrasing):**

> Maak 2 prompts in ai-sessions/ in het engels conform de project regels die: 1) Sign-off vastleggen.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0029_MAINTENANCE_PROMPT_2026_09_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0029_MAINTENANCE_PROMPT_2026_09_17
