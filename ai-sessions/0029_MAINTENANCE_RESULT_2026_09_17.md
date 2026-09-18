# 0029_MAINTENANCE_RESULT_2026_09_17.md — Record maintainer sign-off on `ai-sessions/0027`'s three post-`0028` continuation findings

**Number:** 0029
**Category:** MAINTENANCE
**Date:** 2026-09-17
**Title:** Record the maintainer's review and sign-off, given directly in this chat session, on the three code-level findings `ai-sessions/0027` produced *after* `ai-sessions/0028` already recorded sign-off on its earlier findings
**Status:** complete

## What happened, in order

1. **Mandatory reading order completed in full**, per `AGENTS.md` §0.1 and this prompt's own step 7/9:
   `AGENTS.md` (full, via system context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`,
   `DECISIONS.md` (full ADR header sweep, ADR-001 through ADR-029 — confirmed none conflicts with any
   of the three findings below), `PROTOCOL.md` §6 (Open questions, read from its start), `TODO.md`
   (grepped for item M/H/B/`MaestroEndpointService`/`fwe`/`onBind` cross-references — confirmed
   consistent, no stale claims), `AI_SESSION_LOG_PROCEDURE.md` §4a and `ai-sessions/INDEX.md` in full,
   `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md` in full (all three "Further continuation, same
   session" subsections that postdate `0028`'s own sign-off text), `ai-sessions/0028_MAINTENANCE_RESULT_2026_09_17.md`
   in full (confirming its list A/B genuinely does not cover the three findings below — they postdate
   it), and `REVERSE_ENGINEERING.md`'s three corresponding entries in full (the "Resource/string-table
   sweep" section's newest 2026-09-17 update for item M, the "Candidate rich schemas outside this
   pass's traced call graph" section's newest 2026-09-17 update for item H, and the
   `MaestroEndpointService` entry's newest 2026-09-17 update for item B).

   *(Note: an earlier attempt in this same session to delegate this reading to a background fork
   failed with a rate-limit error before producing any output — no partial results existed to build
   on, so the full mandatory reading was redone directly, from scratch, in the main session.)*

2. **Confirmation checks, per this prompt's own instructions**:
   - **All three findings are genuinely already-labeled 🟢 FACT "mechanical code-existence/structure
     facts,"** not protocol-behavior/wire claims — each traces to a direct code/smali read (a field
     assignment, an exception-message string, a `Binder` subclass check), none involves a wire capture
     or touches `PROTOCOL.md`'s `FrameEncoder`/`FrameDecoder` implementation gate, and none required or
     produced a `DECISIONS.md` ADR. No complications found; the prompt's own framing held up under
     re-verification.
   - **Confirmed a second (HYPOTHESIS/OPEN-QUESTION-relabeling) list is genuinely not needed** — all
     three are already-correct 🟢 FACT entries; nothing in this batch reclassifies a hypothesis or open
     question.
   - `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md`'s own `Status` header was already `complete`
     (unchanged by this entry, matching that file's own stated convention for "bonus" continuations
     within an already-`complete` file).

3. **Sign-off list compiled and presented** in chat — one list (no A/B split needed, per the
   confirmation above), covering:
   - **Item M** — the `fpz` case-5 "device type == 1" condition, traced to the connected earbuds' own
     `GetHardwareInfo` response (`qiv` field 1); the ordinal's own human-readable name confirmed as a
     second, independently-exhausted static-analysis dead end. `REVERSE_ENGINEERING.md`,
     "Resource/string-table sweep (item M, `ai-sessions/0027`)" section, newest (2026-09-17) dated
     update (~lines 3957–4004). Commit `a497df1`.
   - **Item H** — a 4th, separate pool of 28 previously-uncatalogued leads (`fwe.java`'s own 24 private
     byte-stream parser methods, plus 4 of `fwk`'s remaining logged outputs); 10 resolved to concrete
     Bluetooth Classic connection-quality/crash/OTA-status telemetry field names, 18 explicitly left
     open. `REVERSE_ENGINEERING.md`, "Candidate rich schemas outside this pass's traced call graph"
     section, newest (2026-09-17) dated update (~lines 2654–2753). Commit `ed90cd5`.
   - **Item B** — whether `onBind()`'s returned Binder object is real or inert; resolved real, tracing
     the exact value to a genuine `android.os.Binder` subclass (`oge`) with a live UID-based
     authorization handler, independently confirmed to never read the already-known-empty service map.
     `REVERSE_ENGINEERING.md`, "`MaestroEndpointService`" section, newest (2026-09-17) dated update
     (~lines 3561–3621). Commit `40e1c02`.

4. **Maintainer decision**: approved all three findings in full, via the structured sign-off question
   presented in this chat session — selected "Approve all three: All three findings (M, H, B) are
   accepted as accurately recorded, exactly as listed above."

## Scope note — what this sign-off does and does not cover

Matches `ai-sessions/0026`'s and `0028`'s own established framing exactly:

- **Does** mean: the maintainer has reviewed and accepts these three findings as accurately recorded
  and worth keeping in `REVERSE_ENGINEERING.md` at their current, already-assigned 🟢 FACT evidence
  tier.
- **Does not** mean: promoting any 🟡 HYPOTHESIS or 🔴 OPEN QUESTION item to 🟢 FACT, or writing/altering
  any `DECISIONS.md` ADR. No ADR was written or implied by this entry. This sign-off was sought as good
  practice/transparency (the same reasoning `0026`/`0028` already gave), not because `AGENTS.md` §6
  strictly required it for these three mechanical code-facts.
- Does not resolve any of the genuinely open leads these three findings themselves left open (the 18
  still-unnamed `fwe` sub-message classes; what happens when an authorized call reaches `ofm.a()`'s own
  dispatch logic with no service registered; whether the `presto_mr1`/"device type" check has ever
  fired against the maintainer's own paired unit) — all remain exactly as flagged, unaffected by this
  entry.

## Status fields updated

- `ai-sessions/0027_MAINTENANCE_RESULT_2026_09_17.md`: `Status` left unchanged (`complete`) — already
  correct per that file's own "bonus continuation within an already-complete file" convention; nothing
  to update.
- `ai-sessions/INDEX.md`: row for `0029` updated from `prompt only — not yet run` to `complete`.

## Uncommitted

Nothing from this specific entry has been committed yet as of this file's own creation — this
prompt/result pair and the `ai-sessions/INDEX.md` row update are the only artifacts of this session and
remain to be committed and pushed in the same turn this file is written.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0029_MAINTENANCE_RESULT_2026_09_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0029_MAINTENANCE_RESULT_2026_09_17
