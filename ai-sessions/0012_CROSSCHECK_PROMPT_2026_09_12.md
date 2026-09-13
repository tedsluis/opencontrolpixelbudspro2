# 0012_CROSSCHECK_PROMPT_2026_09_12.md — Independent validation and application of Gemini CLI's 0011 review findings

**Number:** 0012
**Category:** CROSSCHECK
**Date:** 2026-09-12
**Title:** Independent validation and application of Gemini CLI's 0011 review findings

---

## Purpose and scope

`ai-sessions/0011_REVIEW_RESULT_2026_09_12.md` is Gemini CLI's own full, non-sampled review of all
45 captured Bluetooth capture sessions (131 findings: 111 confirmations, 20 minor discrepancies, 0
major, 0 flagged as needing a maintainer decision). It is **not itself trustworthy without
independent re-verification** — `ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md` already
established, for a different Gemini CLI pass over this same project
(`ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md`), that roughly half of that pass's own
file+line citations pointed to the wrong location, two to code with no connection to the claim at
all, and that its "100%/absolute certainty" framing outran what its own cited evidence actually
supported. Read that precedent (in full, during Phase 0) before trusting a single finding in `0011`.

This session's job, for **every one of the 131 findings in `0011`**, one at a time:

1. **Independently re-derive the evidence.** Re-run the exact `tshark`/Python command cited (or, if
   none is cited precisely enough to reproduce, construct the equivalent one yourself) against the
   actual current file, and confirm the output matches what `0011` claims — frame number for frame
   number, byte for byte. Do not accept a citation on Gemini's word.
2. **Cross-match against the decompiled APK** wherever the finding touches a `qhr`/`RpcPacket`
   field number, a Message-Stream Group/Code, a class/method name, or any other APK-derived
   identifier already catalogued in `REVERSE_ENGINEERING.md` — confirm the citation actually
   resolves in `reverse-engineering/apk/<version>/jadx-output/` (falling back to `apktool-output/`
   only where JADX is unreadable), within `DECISIONS.md` ADR-017's mechanical-assistance boundary
   (verify an existing citation, don't newly decide relevance).
3. **Decide, and act, per finding:**
   - **Valid AND valuable** (the re-derivation confirms it, and it adds real value beyond what's
     already documented — a genuine correction, a newly-confirmed detail, a real procedural note):
     process it into the relevant project document(s) yourself, in this same session — see
     "What 'process it in' means" below for the FACT/ADR boundary this still has to respect.
   - **Not valid, or not valuable** (re-derivation contradicts it, the citation doesn't resolve, or
     it's redundant with something already documented and adds nothing new): let it lapse — do not
     add it to any document. Record why, briefly, in this session's own RESULT file so the decision
     is traceable, but do not touch the target document.
   - **Ambiguous, or requires a judgment call only the maintainer can make** (most commonly: the
     finding itself is sound but its own "Advice" asks for a 🟢 FACT promotion or a `DECISIONS.md`
     ADR — see the guardrail below, this is *always* routed here, never decided by the agent):
     do not act on it. Collect it into the "Open questions for the maintainer" section of the
     RESULT file (see Output format) instead of silently deciding either way.

## What "process it in" means — and its hard limit

Most of `0011`'s 20 discrepancies are safe, mechanical corrections in scope for direct
application once re-verified: a wrong frame-number citation in a `CAP-NNN-FINDINGS.md`, a
procedural-deviation note that belongs in a capture's metadata, a `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
Evidence-column pointer that needs updating, an `id_registry.csv` note. Apply these directly —
this is the same kind of correction `ai-sessions/0010`'s own session already made repeatedly (e.g.
its CAP-018/CAP-029/CAP-045/CAP-046/CAP-048 draft-timeline corrections), and it's exactly what the
maintainer is asking this session to do for `0011`'s own findings too.

**The hard limit, restated from `AGENTS.md` §6/§15 — this does not change for this task:**

- **Never independently promote anything to 🟢 FACT in `PROTOCOL.md`.** A large fraction of `0011`'s
  "Advice" fields literally say "Promote X as 🟢 FACT" or "Maintain as a verified 🟢 FACT" — where
  the cited thing is *already* 🟢 FACT in `PROTOCOL.md`, that's a no-op (nothing to change; it's a
  confirmation, log it as such). Where it is **not** already 🟢 FACT (e.g. Finding 22/CAP-006's ANC
  tap-reliability resolution of ADR-009, Finding 30/CAP-009's `BATT-006` resolution, Finding
  51/CAP-015's absolute EQ mapping, Finding 57/CAP-017's `GATT-001` third discovery path, Finding
  59/CAP-017's GATT service-list promotion, and any other finding recommending a status change),
  you may draft the change as an explicit, clearly labeled **PROPOSAL** (same pattern as
  `ai-sessions/0010`'s PROTOCOL.md §4.5.7/§6 edits) — but the emoji/status itself stays whatever it
  already was until the maintainer signs off. Route the underlying decision to "Open questions for
  the maintainer."
- **Never write or alter a `DECISIONS.md` ADR**, including one that "resolves" or "supersedes" an
  existing one (several findings — e.g. Finding 22 citing ADR-009, Finding 30 citing ADR-015 — imply
  an ADR update). Propose the ADR text in the RESULT file's open-questions section for the
  maintainer to author, never in `DECISIONS.md` itself.
- These two rules apply regardless of how confident the re-derivation makes you, and regardless of
  Gemini's own "Advice" wording — model agreement is not the human review `AGENTS.md` §6 requires.

## How to (re)start this prompt

Before anything else: check whether `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` already
exists.
- **It does not exist** → first run. Create it in Phase 0 with a full findings-tracking table (one
  row per `0011` finding number, 1–131) and start from Finding 1.
- **It already exists** → read it in full, including its findings-tracking table. Skip to the first
  `not started` row and continue — do not redo a finding already marked `applied` / `rejected` /
  `routed to maintainer`.

Update the RESULT file's tracking table (and the header `Status` field —
`partial — resumed` while incomplete) before ending any turn, exactly per
`AI_SESSION_LOG_PROCEDURE.md` §4/§5's resumability convention. 131 findings, each independently
re-derived against raw logs/video/APK, will almost certainly span multiple sessions.

## Mandatory reading order (every session that works on this prompt, per `AGENTS.md` §0.1 / `AI_SESSION_LOG_PROCEDURE.md` §8)

1. `AGENTS.md` (full) — §6/§15 above all; also §13.6 (zero-creativity hex parsing) and §12
   (never reproduce Google's own source, describe behavior only).
2. `PROJECT_RULES.md` (full) — rule 1 (FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION), rule 4a
   (hex-and-script: exact command + raw hex for every decode), rule 3 (never silently overwrite or
   contradict a `DECISIONS.md` entry — flag and propose a superseding one instead).
3. `PROJECT.md`, `ARCHITECTURE.md` (full).
4. `PROTOCOL.md` (full) — this is the document most findings will propose changes against; know its
   current state before touching it.
5. `DECISIONS.md` — every ADR. Several `0011` findings cite specific ADR numbers (e.g. ADR-009,
   ADR-015) — read those entries closely enough to know exactly what they currently say before
   evaluating whether a finding actually contradicts or extends them.
6. `TODO.md`.
7. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
8. **`ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md` and
   `ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md` in full.** This is the direct precedent for
   this exact task shape (Gemini produces a review, Claude Code independently re-verifies and
   applies verified corrections) — `0008` is the template for both the rigor bar and, loosely, the
   output structure this session should follow.
9. `ai-sessions/0011_REVIEW_PROMPT_2026_09_12.md` (the instructions Gemini was actually working
   under) and `ai-sessions/0011_REVIEW_RESULT_2026_09_12.md` (full — all 131 findings; this is the
   input queue for this session).
10. `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `REVERSE_ENGINEERING.md`,
    `id_registry.csv` — the documents most findings will propose edits against.
11. `APK_REVERSE_ENGINEERING_PROCEDURE.md` / `reverse-engineering/APK_VERSIONS.md` — confirms which
    decompiled workspace path/version to cross-match against.

## Guardrails

- Every change this session makes to a project document must be traceable to a specific,
  independently re-derived finding — not to `0011`'s own prose. If your own re-run of a command
  produces different output than `0011` claims, trust your own re-run, not `0011`.
- No sampling: go through all 131 findings in order, not a subset. If a finding turns out to be a
  pure duplicate of one already processed (same underlying claim, re-stated for a different
  capture), say so explicitly and link them — don't silently skip.
- Stay inside `DECISIONS.md` ADR-017's mechanical-assistance boundary for every APK cross-match:
  confirm/deny an existing citation, never newly decide that an unrelated class/field is relevant.
- Never reproduce Google's own source code verbatim beyond the minimum needed to cite a finding
  (`AGENTS.md` §12).
- MAC addresses in this project's own captures are intentionally unredacted (`DECISIONS.md`
  ADR-010) — not a finding to flag or fix.
- When a finding's own "Advice" asks for a FACT promotion or an ADR, route it to "Open questions for
  the maintainer" even if your re-derivation fully confirms the underlying evidence — confirmed
  evidence still needs the human sign-off `AGENTS.md` §6 requires before the status changes.
- Do not restructure or rewrite documents beyond what a specific finding justifies — a corrected
  frame number is a one-line edit, not a license to rewrite the surrounding section.

## Output format

`ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md`, structured as:

1. **Header block** per `AI_SESSION_LOG_PROCEDURE.md` §4.
2. **Findings-tracking table**, one row per `0011` finding number (1–131): `Finding # | Capture |
   0011's one-line claim | Re-derivation result (confirmed / contradicted / citation didn't
   resolve) | Disposition (applied / rejected / routed to maintainer / duplicate-of-#N) | Document(s)
   touched`.
3. **Per-finding detail**, grouped by capture (mirroring `0011`'s own structure), each entry with:
   - **0011 finding:** the original claim + advice, quoted or closely paraphrased.
   - **Re-derivation:** the exact command you ran (or exact frame/video/APK location you checked)
     and its actual output — the hex-and-script rule applies here exactly as it would to a fresh
     capture analysis.
   - **Disposition:** applied / rejected / routed to maintainer, with a one-line reason.
   - **Change made** (if applied): which document, which section, what changed — or "none" if
     rejected/routed.
4. **Open questions for the maintainer** — every finding routed there, grouped and summarized (not
   131 separate asks): each item states what the evidence supports, what decision is actually
   needed (a FACT promotion, an ADR, a genuinely ambiguous relevance call), and a proposed
   resolution for the maintainer to accept or reject. This is the section the assisting agent
   should present to the user for explicit go-ahead — per the maintainer's own instruction, do not
   act on these without that sign-off.
5. **Summary**: totals (re-derivation confirmed / contradicted / unresolvable; applied / rejected /
   routed), and which documents were touched.

## Phase 0 — Setup

Complete the mandatory reading order. Create `0012_CROSSCHECK_RESULT_2026_09_12.md` with the
findings-tracking table pre-populated (all 131 rows, status `not started`) from `0011`'s own
Detailed Findings section.

## Phase N — Work through findings in batches (e.g. one capture's findings per phase)

Apply the per-finding procedure above. Update the tracking table and detail section before ending
the turn.

## Final phase — Wrap-up

Write the Summary section. Set `Status: complete — open questions pending maintainer sign-off` (not
plain `complete`) if the "Open questions for the maintainer" section is non-empty — which it almost
certainly will be, since most FACT-promotion/ADR-worthy findings route there by design. Then, in the
chat response (not the RESULT file), give the maintainer a compact summary of exactly those open
questions and ask for explicit go-ahead before anything in that section is acted on further.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0012_CROSSCHECK_PROMPT_2026_09_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0012_CROSSCHECK_PROMPT_2026_09_12
