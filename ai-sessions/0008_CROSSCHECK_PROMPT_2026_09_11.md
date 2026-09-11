# 0008_CROSSCHECK_PROMPT_2026_09_11.md — Full, non-sampled validation of Gemini's 0007 cross-check result, with direct application of verified corrections

**Number:** 0008
**Category:** CROSSCHECK
**Date:** 2026-09-11
**Title:** Full, non-sampled validation of Gemini's 0007 cross-check result, with direct application of verified corrections

---

> **How to use this file:** this entire document (everything below this line) is the prompt for a
> Claude Code session on this project. Run it from the repository root of `opencontrolpixelbudspro2`,
> on a machine that has `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/` and
> `apktool-output/` present on disk (gitignored, local-only — see `reverse-engineering/APK_VERSIONS.md`)
> and the `captures/` tree available.

---

## 0. What this is and why

`ai-sessions/0007_CROSSCHECK_PROMPT_2026_09_08.md` commissioned Gemini CLI, as an independent second
model, to review `REVERSE_ENGINEERING.md` and `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` (a
Claude Code deep cross-check pass). Gemini's output is `ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md`
— currently `Status: awaiting maintainer sign-off`, with several findings labeled 🟢 FACT and a closing
recommendation to "approve all Phase 4 promotions" from `0001`.

**The maintainer has not yet reviewed `0007_CROSSCHECK_RESULT_2026_09_11.md` for accuracy, and has
explicitly asked for it to be checked before any of it is trusted or acted on.** This project's own
`WORKSTATION_PREPARATIONS.md` cross-validation practice treats model agreement as something that
*raises* confidence, never something that *substitutes* for independent re-derivation — a second
model confidently confirming a claim is not itself evidence the claim is true. This task is that
independent re-derivation, performed by you (Claude Code) against `0007_CROSSCHECK_RESULT_2026_09_11.md`
specifically, using the same standard `0001` and `0007` were themselves supposed to meet.

**Your task has two parts:**

1. **Validate every single point in `0007_CROSSCHECK_RESULT_2026_09_11.md` — completely, not by
   sampling.** Every citation, every claim, every sub-assertion in its Methodology Assessment section.
   Base every verdict on facts you personally re-derive (open the file, run the command, read the
   output) — never on whether Gemini's write-up sounds plausible, detailed, or confident.
2. **Act on what you find**, within this project's existing authority boundaries (§4 below):
   - Where a review point is independently confirmed **and** applying it is a plain factual
     correction/addition that does not touch `PROTOCOL.md`'s FACT status for a protocol claim and
     does not require a `DECISIONS.md` ADR — **apply it directly** to the relevant project file(s).
     The maintainer's instruction in the chat session that produced this prompt is the explicit
     authorization this project's `DECISIONS.md` ADR-017 requires for that kind of direct edit — cite
     this prompt (`ai-sessions/0008_CROSSCHECK_PROMPT_2026_09_11.md`) as that authorization in your
     edit's context/commit, so the provenance is traceable per this project's own conventions.
   - Where a review point would mean promoting something to 🟢 FACT in `PROTOCOL.md`, or writing/
     amending a `DECISIONS.md` ADR, or is otherwise a maintainer-level call (this includes Gemini's
     own "Approve all Phase 4 Promotions" recommendation, and its recommendation to incorporate the
     new Volume Balance/field-19 finding) — **do not apply it.** Instead, present the maintainer with
     a clear, concise summary of exactly what was independently verified (and what wasn't), and ask
     for an explicit decision, the same way `AGENTS.md` §6 already requires for any FACT/ADR
     promotion proposed by an AI session.

---

## 1. Mandatory reading order

Read these, **in full, in this order**, before evaluating a single claim in `0007_CROSSCHECK_RESULT_2026_09_11.md`.
This is a floor per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8, not optional boilerplate.

1. `AGENTS.md` — full, especially §0, §4, §6, §15.
2. `PROJECT.md` — full.
3. `PROJECT_RULES.md` — full, especially §1 (the four-tier legend, the hex-&-script rule) and §3
   rule 9/9a (the non-destructive-update convention that governs how you must edit `DECISIONS.md`/
   `PROTOCOL.md` vs. `REVERSE_ENGINEERING.md`/`CAP-NNN-FINDINGS.md` if you end up applying anything).
4. `DECISIONS.md` — **every ADR**, ADR-001 through ADR-025 and all of ADR-019/ADR-025's dated Update
   notes. Give particular attention to **ADR-017** (the AI-assistance boundary you must operate
   within for this task) and **ADR-019** (the exact `qhr` field-by-field promotion history — you will
   need this to check Gemini's field-11/15 and its new field-17/19 claims against what is *already*
   established, not just against the code).
5. `ARCHITECTURE.md` — full.
6. `PROTOCOL.md` — full, **including §6's open-questions checklist and §8's changelog**. In
   particular §4.5's per-field subsections (§4.5.1–§4.5.8, §4.5.5a) — the current, already-FACT
   meaning of every `qhr` field this review touches lives here.
7. `TODO.md` — full.
8. `REVERSE_ENGINEERING.md` — **full**, again if your memory of it has decayed since an earlier
   session. In particular the `qhr` entry's full field-register table (the one listing fields 2–32
   with their write/read call sites and plausible roles) — this is the ground truth you check every
   new field-identity claim in `0007_CROSSCHECK_RESULT_2026_09_11.md` against.
9. `APK_REVERSE_ENGINEERING_PROCEDURE.md` — full.
10. `reverse-engineering/APK_VERSIONS.md` — full. Confirms the single analyzed APK version
    (`v1.0.955078536-10253511`) and its on-disk path — every citation you check must resolve against
    this exact version.
11. `AI_SESSION_LOG_PROCEDURE.md` — full. Governs how you log this session's own output (§6 below).
12. `ai-sessions/INDEX.md` — full.
13. **`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`** — full. The original document Gemini was
    asked to review; you need its exact claims to judge whether Gemini's "Confirmed" verdicts on it
    actually engaged with what `0001` said, or merely restated it.
14. **`ai-sessions/0007_CROSSCHECK_PROMPT_2026_09_08.md`** — full. The exact instructions Gemini was
    given — you are checking whether Gemini's output actually followed them (independent
    re-derivation from primary sources, full citations, hex-&-script rule, no self-promotion to
    FACT), not only whether its conclusions happen to be correct.
15. **`ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md`** — full. This is the document under
    validation. Read it twice if needed: once for its claims, once for what it does *not* show its
    work for (a citation with no quoted content, a "Confirmed" verdict with no command/hex, a claim
    that isn't cross-checked against this project's own existing register).
16. `ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md` and `ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md`
    — full. These record exactly which of `0001`'s Phase 4 proposals the maintainer has *already*
    signed off on (fields 11/15's promotion among them, via `DECISIONS.md` ADR-025's 2026-09-08
    Update). Do not re-litigate what is already decided — but do check whether `0007`'s own
    "Confirmed" verdict on fields 11/15 is independently correct regardless, since an already-decided
    conclusion being reached via broken or fabricated evidence is still a defect worth surfacing.
17. `captures/CAP-027-2026-08-30_15-45-14_15-49-07-Group_N/CAP-027-FINDINGS.md` and the sibling
    `CAP-027-EVENT-NOTES.md` in the same directory — full. `0007`'s Methodology section (§3) makes
    several specific, checkable claims against this exact capture; read the capture's own findings
    file first so you know what it already established, before re-deriving Gemini's claims against
    the raw log yourself.

---

## 2. Full-coverage validation — no sampling

`0007_CROSSCHECK_RESULT_2026_09_11.md` has 6 numbered findings (§2.1–§2.6) and a Methodology
Assessment (§3) with several further, individually-checkable sub-claims. **You must independently
verify every one of them — not a representative subset.** For each:

### 2.1 Citation-level verification (every citation, every finding)

For every `file:line` citation in every finding (§2.1 through §2.6):

1. Open the exact file at the exact cited path under
   `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/` (or `apktool-output/` for
   manifest/smali citations) and confirm the cited lines exist and contain what the finding claims
   they contain. Quote the actual content you find.
2. If a citation is wrong (wrong line number, class doesn't exist, method body says something
   different from what's claimed, or — as with `ijs.java` in §2.1, which does not match this
   project's own prior documentation of an `ijp.java` FMD-proxy class) — **do not assume it's a
   typo for something nearby.** Search for the actually-correct class/method yourself
   (`grep -rn`, `find`) and report both what was cited and what you actually found.
3. Where a finding claims a class "wires," "dispatches," "subscribes," or "invokes" another — trace
   that call graph yourself, the same way `REVERSE_ENGINEERING.md`'s own entries do, rather than
   accepting the claimed relationship because the two file names appear together in the same bullet.

### 2.2 Script- and tool-level verification

For every script `0007_CROSSCHECK_RESULT_2026_09_11.md` claims to have run (`scripts/decode_rawmessageinfo.py`,
`scripts/decode_qhr_settings.py`, and any `tshark` invocation described in §3):

1. Confirm the script actually exists at the stated path (`ls scripts/`) — do not assume.
2. **Re-run it yourself**, against the same input Gemini's write-up names, and compare your own
   output against what `0007_CROSSCHECK_RESULT_2026_09_11.md` reports. Quote your own command and
   your own output verbatim (per `PROJECT_RULES.md` §1 rule 4a's hex-&-script rule) — a paraphrase
   of "the output matched" is not sufficient; show the actual output.
3. If a described command can't be reconstructed exactly as written (an ambiguous filter, a script
   invoked with unstated arguments), reconstruct the most faithful equivalent yourself, note exactly
   what you had to infer, and report the result under that caveat rather than silently guessing.

### 2.3 Capture-level verification — do the findings match the captures?

This is the specific question the maintainer asked, and it applies to every capture-backed claim in
`0007_CROSSCHECK_RESULT_2026_09_11.md`, most concentrated in its §3 Methodology Assessment:

1. **AVRCP isolation claim** (§3 item 3, "AVRCP Isolation"): re-derive directly from
   `CAP-027-btsnoop_hci.log` (using `tshark` filtered by DLCI, per `PROJECT_RULES.md` §1 rule 4a and
   this project's own established method, `AGENTS.md` §13's capture-analysis workflow) whether
   `TOUCH-002`–`TOUCH-006` genuinely produce **zero** DLCI 0x02 frames and instead ride AVRCP/L2CAP —
   cross-reference the exact timestamps/frame numbers against `CAP-027-EVENT-NOTES.md`'s own
   recorded tap times, don't just check "AVRCP traffic exists somewhere in this log."
2. **DLCI 0x04 press-and-hold claim** (§3 item 3, "DLCI 0x04 Integration"): re-derive directly from
   the same log whether `TOUCH-007` produces a `0x13` (Notify ANC state) frame on DLCI 0x04, and
   confirm this is consistent with `PROTOCOL.md` §4.1's already-established ANC Notify mechanism (not
   a new, distinct mechanism Gemini may have mis-attributed).
3. **The DLCI 0x02 field-value list** (§3 item 3, "DLCI 0x02 Verification" — the claim that this one
   session's traffic shows writes to fields 1, 2, 3, 4, 11, 15, 16, 17, 18): decode every one of these
   directly from the raw log yourself (HDLC-unescape, CRC-verify, decode the wire-format tag per
   `PROTOCOL.md` §2.2a's published method) and check **each field number's claimed value/meaning
   against what `REVERSE_ENGINEERING.md`'s own `qhr` field-register table and `PROTOCOL.md` §4.5
   already establish for that exact field number.** This is the single most important check in this
   entire task — flag explicitly, and do not gloss over, any case where `0007_CROSSCHECK_RESULT_2026_09_11.md`
   states a meaning for a field number that conflicts with an already-established meaning for that
   *same* field number (for example: check field 3's claimed role against the existing "OOBE Is
   Finished setting" identification; check field 4's claimed role against the existing, `DECISIONS.md`
   ADR-019-promoted "Head/touch gestures master enable toggle" identity; check field 19's claimed
   role in §2.6 against the existing, `DECISIONS.md` ADR-019-promoted "Mono audio" identity). Two
   different meanings asserted for the same field number in the same `qhr` schema is a direct logical
   contradiction, not an open question — if you find one, it means at least one of the two claims is
   wrong, and you must determine which (re-derive both independently; do not average or split the
   difference).

### 2.4 The "NEW INDEPENDENT FINDING" (§2.6) — hold this to the same bar, not a lower one

`0007_CROSSCHECK_RESULT_2026_09_11.md`'s §2.6 presents a claim not found in either `0001` or the
existing `REVERSE_ENGINEERING.md` register (a Volume Balance "extreme/gate" mechanism on field 19).
A genuinely new finding from a second model is exactly the kind of result this project's
cross-validation process exists to catch — but it is also exactly the kind of claim most likely to be
an unverified inference dressed up as a finding, since nothing in this project's own prior work
predicted it. Apply **more** scrutiny here, not less: re-derive `fxf.java`'s case 16 body yourself in
full (not just the snippet quoted), confirm independently what field number(s) it actually writes and
under what condition, and check it byte-for-byte against a real `qhr` field-19 write on the wire if
`CAP-022` (the capture `PROTOCOL.md` §4.5.7 already cites for field 17/Volume balance) or any other
capture shows one.

### 2.5 Consistency of the recommendations (§4) with what you actually found

`0007_CROSSCHECK_RESULT_2026_09_11.md`'s closing §4 recommends approving "all Phase 4 Promotions"
and incorporating the new field-17/19 finding, and its §1 Executive Summary claims confirmation "with
100% fidelity" and "absolute certainty." State explicitly whether your own independent re-derivation
actually supports language that strong, for each finding — and if any single citation, script run, or
capture cross-check in §2.1–§3 does not hold up, say so plainly rather than letting an accurate
verdict on most findings soften how a false or unverifiable one is reported.

---

## 3. Guardrails — unchanged from this project's standing rules

These apply to your review and to any edit you make, identically to every other AI session on this
project:

1. **Four-tier status legend** (🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION,
   `PROTOCOL.md` §0's canonical legend) on every claim you make, including your own verdicts.
2. **Hex & script rule** (`PROJECT_RULES.md` §1 rule 4a) — every wire-level check you perform must
   show the exact command and the raw hex bytes it operated on, not just the resulting
   interpretation.
3. **Exact citation, always** — your own citations, from your own independent read, file and line
   number, never copied from `0007_CROSSCHECK_RESULT_2026_09_11.md` without having opened the
   location yourself.
4. **No assumptions, no sampling** (this task's own explicit instruction from the maintainer,
   restated because it is the point of this task): do not extrapolate from a few checked citations
   to "the rest are probably fine too." Every citation, every script run, every capture cross-check
   gets its own independent verification and its own stated verdict.
5. **ADR-017's AI-assistance boundary** still applies to how you search and read code (search, list,
   explain — you are not discovering brand-new APK findings from scratch here, you are checking
   existing ones, but the same rigor applies). It does **not** block you from applying a verified,
   non-promotion correction directly to project files under this task's explicit maintainer
   authorization (§0/§4) — but it does still block you from writing or amending `DECISIONS.md`, and
   from marking anything 🟢 FACT in `PROTOCOL.md`, without the per-item maintainer sign-off `AGENTS.md`
   §6 requires. Zero exceptions, including for a claim you personally, independently re-derive and
   agree with — re-derivation earns you the right to present a confident recommendation, not the
   right to decide.
6. **Zero-creativity rule** (`AGENTS.md` §13.6) for every byte/field decode you perform.
7. **Scope boundary** — do not pursue anything in this project's non-goals list (Account Linking,
   Ownership Transfer, Accessory Non-Owner Service, GMS reverse-engineering itself) even if a citation
   under review brushes against it.

---

## 4. What "apply directly" means, precisely — and where the line is

**Apply directly** (edit the file yourself, in this same session, following each file's own update
convention — rewrite-in-place for `REVERSE_ENGINEERING.md`/`CAP-NNN-FINDINGS.md` per
`PROJECT_RULES.md` §3 rule 9a, dated non-destructive `Update` notes for `PROTOCOL.md`/`DECISIONS.md`
where an edit to those is in-bounds at all) when **all** of the following hold for a given point:

- You independently re-derived it and it checks out (Confirmed), or you independently re-derived it
  and found a concrete, correctable error in `0007_CROSSCHECK_RESULT_2026_09_11.md` itself (a wrong
  citation, a wrong field number, a mischaracterized mechanism) that you can state with the same
  confidence.
- Applying it does **not** newly mark anything 🟢 FACT in `PROTOCOL.md` for a protocol-behavior claim.
- Applying it does **not** require writing or amending a `DECISIONS.md` ADR.
- It is a plain factual correction or addition — e.g., correcting `REVERSE_ENGINEERING.md`'s citation
  for the FMD-proxy descriptor class if `ijp.java` (not `ijs.java`) is what you actually confirm;
  updating `TODO.md` to note that this validation pass ran and what it found; adding a note to
  `REVERSE_ENGINEERING.md`'s existing `qhr` entry recording that a specific new claim was checked and
  found to conflict with (or corroborate) the existing register, at code-existence level, per
  `ADR-017`'s own "may explain already-surfaced code" boundary.

**Stop and ask the maintainer instead** (produce a clear summary — what was claimed, what you
independently found, your recommendation — and wait for an explicit decision before proceeding
further) when a point involves:

- Any new or changed 🟢 FACT status in `PROTOCOL.md` for a protocol-behavior claim (this includes
  Gemini's field-11/field-15 "Confirmed" verdict, even though the maintainer has *already* approved
  that specific promotion via `ai-sessions/0002`/`0003` and `DECISIONS.md` ADR-025's Update — flag
  whether Gemini's supporting evidence for it is itself sound, since a correct conclusion reached via
  unsound verification is still worth the maintainer knowing about).
- Any new `DECISIONS.md` ADR or Update note — including the draft ADR-025 Update note `0001` itself
  proposed, and anything `0007_CROSSCHECK_RESULT_2026_09_11.md`'s §2.6 "new finding" would imply for
  `qhr` field 17/19 if confirmed.
- Gemini's own closing recommendation to "approve all Phase 4 Promotions" as a bundle — present each
  promotion's status (independently confirmed / contradicted / unable to confirm) separately; do not
  let the maintainer inherit a bundled up/down vote when your own findings are mixed.
- Any case where you found `0007_CROSSCHECK_RESULT_2026_09_11.md` to be **wrong** about something
  that a prior, already-signed-off `DECISIONS.md` ADR asserts (e.g., a field-19 conflict against the
  ADR-019-promoted "Mono audio" identity) — this is not a routine correction, it's a signal that
  Gemini's review process itself may be unreliable, which the maintainer needs to know before relying
  on it for anything else.

---

## 5. Output structure

Produce your result as a standalone Markdown document (this becomes your logged `RESULT` file, §6
below). Structure it as:

### 5.1 Per-point validation table/sections

One entry per citation/claim/sub-check from §2.1–§2.5 above (expect on the order of 20–30 individual
entries once every citation and every capture sub-claim is counted separately — this is the point of
"no sampling"). For each:

- **What was claimed** (quote or precisely paraphrase the exact line in `0007_CROSSCHECK_RESULT_2026_09_11.md`).
- **What you independently found** (your own file/line, your own command+output, your own capture
  decode — shown in full, not summarized).
- **Verdict**: `Confirmed` / `Contradicted` / `Cannot verify` (state exactly what's missing —
  workspace not present, capture not present, ambiguous instruction) / `Confirmed but evidence in
  0007 was insufficient or wrong` (the conclusion happens to be right, but Gemini's own citation/work
  shown for it does not actually support it — an important, distinct category, since it says
  something about the reviewing model's reliability independent of whether this one answer was
  right).
- **Consistency check**: does this claim agree or conflict with an existing entry in
  `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, or a `DECISIONS.md` ADR? Name the specific conflicting or
  corroborating entry.
- **Action taken**: `Applied directly to <file>, see diff below` / `Flagged for maintainer decision,
  see §5.3` / `No action needed` (for a clean confirmation of something already fully settled).

### 5.2 Changes applied this session

A complete list of every direct edit you made: file, location, exact before/after (or the diff), and
a one-line justification citing which §4 "apply directly" criterion it met.

### 5.3 Decisions needed from the maintainer

For every item flagged in §4's "stop and ask" category: a short, self-contained summary (claim,
independent finding, your recommendation) — written so the maintainer can decide from this section
alone without re-reading the full per-point validation. **Present this section directly in your
chat response to the maintainer when this task finishes, not only inside the saved `RESULT` file** —
the maintainer asked to be asked, not to have to go find the file and read it themselves.

### 5.4 Summary

A short closing paragraph: how many points were checked (state the exact count, confirming
non-sampled coverage), how many were confirmed / contradicted / unable to verify / confirmed-but-
poorly-evidenced, how many direct edits were applied, and how many decisions are now waiting on the
maintainer.

---

## 6. Log your own output, per `AI_SESSION_LOG_PROCEDURE.md`

1. Check `ai-sessions/INDEX.md` for the next free `Number` at the time you actually run this (don't
   assume `0008`, this prompt file's own number, is still free).
2. Save your result as `ai-sessions/<NNNN>_CROSSCHECK_RESULT_2026_09_11.md`, header per
   `AI_SESSION_LOG_PROCEDURE.md` §4, with `Status: awaiting maintainer sign-off` if any item landed
   in §5.3, or `Status: complete` if every single point was a clean, fully-resolved
   confirm/contradict with no maintainer decision pending (unlikely, given §4's scope, but state
   accurately either way).
3. Save this prompt file itself alongside it as `ai-sessions/<NNNN>_CROSSCHECK_PROMPT_2026_09_11.md`
   (reconstructed verbatim if you were handed its content directly rather than as a file).
4. Add the new row to `ai-sessions/INDEX.md`.
5. List every file you edited directly (§5.2) in your final chat summary to the maintainer, alongside
   §5.3's decisions-needed section — both need to reach the maintainer directly, not only the log.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0008_CROSSCHECK_PROMPT_2026_09_11.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0008_CROSSCHECK_PROMPT_2026_09_11
