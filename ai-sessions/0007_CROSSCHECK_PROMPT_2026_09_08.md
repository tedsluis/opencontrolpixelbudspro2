# 0007_CROSSCHECK_PROMPT_2026_09_08.md — Independent Gemini CLI review of the APK reverse-engineering catalog and the 0001 deep cross-check pass

**Number:** 0007
**Category:** CROSSCHECK
**Date:** 2026-09-08
**Title:** Independent Gemini CLI review of the APK reverse-engineering catalog and the 0001 deep cross-check pass

---

> **How to use this file:** this entire document (everything below this line) is the prompt to hand
> to Gemini CLI, run from the repository root of `opencontrolpixelbudspro2` on a machine that has
> `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/` and `apktool-output/` present on
> disk (these are gitignored, local-only — see `reverse-engineering/APK_VERSIONS.md`). Paste it
> verbatim as the initial instruction, or pipe it in (`gemini < this_file`). This is the required
> deliverable regardless of whether `gemini` CLI is actually invoked in any particular environment.

---

## 0. What this is and why you're being asked to do it

You are Gemini, acting as an **independent second model** on the OpenControl for Pixel Buds Pro 2
project — an open-source, GMS-free Android app that controls the Google Pixel Buds Pro 2 over
Bluetooth, being built by reverse-engineering the protocol from packet captures and static analysis
of the official companion APK. This project's own cross-validation practice
(`WORKSTATION_PREPARATIONS.md`'s "Cross-validation between AI models" section) calls for a second,
independent model to re-derive and check a first model's (Claude Code's) reverse-engineering
conclusions before anything is treated as settled — model agreement raises confidence, but per
`AGENTS.md` §6 it never substitutes for the human maintainer's own sign-off, and per this project's
own house rules an AI's *independent re-derivation* is what actually carries evidentiary weight, not
a second AI's agreement with the first AI's prose.

You are reviewing **two bodies of work**, in order of priority:

1. **`REVERSE_ENGINEERING.md`** — the full, cumulative APK reverse-engineering catalog: the
   `qhr`/`qjc`/`qja`/`qjn`/`qjt`/`qhx`/`qjv` protobuf schema family, the `nqx`/`npy`/`nqo`/`npw`/`nqm`
   Pigweed `pw_rpc` transport plumbing, the UUID and Message-Group registers, and every class entry
   this document records.
2. **`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`** (the "deep cross-check pass") —
   **prioritize this one for the most scrutiny.** It is the newest addition to this project's
   evidence base and the *least* independently verified so far: it is the output of a single Claude
   Code session, and while several of its findings have since been acted on (maintainer sign-offs
   recorded in `DECISIONS.md` ADR-019's Update / ADR-025's Update, dated 2026-09-08), no second model
   has ever independently re-derived its central claims from the raw decompiled source. Give
   particular attention to:
   - The GMS Chimera/AIDL boundary (`IFastPairDeviceDetailService`, `IFastPairFmdProxyService`) —
     Phase 1, "Finding 1."
   - The two newly-found UI entry points: `MaestroDeviceSettingsProviderService` (the AOSP
     Bluetooth-Device-Details settings extension) and the still-undetermined `MaestroEndpointService`
     gRPC server — Phase 1's "Genuinely open thread" and Phase 4's proposed open items.
   - `qhr` field 11 (Multipoint) and field 15 (Volume EQ)'s end-to-end traces — Phase 2.
   - The `frb`/`fuh`/`glk`/`gjv` connect-burst-trigger tracing for `fxm.i()`/`GetSoftwareInfo` — Phase 2's
     Q4 write-up.

Your review itself must be **exhaustive and independent** — not a plausibility read of what's
already written, but a re-derivation from the primary sources (the decompiled APK, and where
relevant the wire captures) that happens to be checked against the existing prose, not the reverse.

---

## 1. Mandatory reading order

Read these, **in this order, in full**, before forming any opinion. This project's own
`AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8 both require this as a floor, not optional
boilerplate — do not skip ahead to `REVERSE_ENGINEERING.md` or the APK source on the assumption you
already understand the project from general knowledge of Fast Pair/Pigweed/protobuf.

1. `AGENTS.md` — full. The permanent project-law guardrails; §0, §4, §6, and §15 are the ones most
   load-bearing for this specific task.
2. `PROJECT.md` — full. Goal, scope, and non-goals — in particular the Account
   Linking/Ownership-Transfer/Accessory-Non-Owner-Service exclusion and the GMS-reverse-engineering
   non-goal (both cross-reference `DECISIONS.md` ADR-008/ADR-025, read below).
3. `PROJECT_RULES.md` — full. The binding evidence rules: the four-tier FACT/HYPOTHESIS/
   ASSUMPTION/OPEN QUESTION legend (§1), the hex-and-script rule (§1 rule 4a), and the
   non-destructive-update convention (§3 rule 9/9a) that governs how `REVERSE_ENGINEERING.md` and
   `DECISIONS.md` are structured — you need to recognize this structure to read either file
   correctly.
4. `DECISIONS.md` — **every ADR**, ADR-001 through ADR-025, not just the most recent ones (an
   earlier ADR can still be load-bearing for a later one). Give **extra care** to:
   - **ADR-003** (superseded) and **ADR-017** (supersedes it) — the AI-assistance boundary for APK
     work: search/list/explain only, never decide relevance, never record a `REVERSE_ENGINEERING.md`
     finding or a `DECISIONS.md` ADR unilaterally.
   - **ADR-008** — the Account Linking/Ownership Transfer/Accessory Non-Owner Service scope
     exclusion.
   - **ADR-018** — DLCI 0x02 channel-ownership promoted to FACT (narrow promotion, Option 2) via the
     `gbm`/`fzd` SDP-UUID correlation.
   - **ADR-019** and its three dated Update notes — the `qhr` field-by-field promotion history
     (fields 4, 7, 12, 17, 19, 22, 27, 28, 2, 11, 15) and the precise evidentiary distinction it
     draws between "an independent code-side path exists" and "a self-describing code name exists
     and reconciles with the pre-existing wire-derived label."
   - **ADR-020** — EQ's implementation gate explicitly unblocked (a decision-only entry, no new
     protocol knowledge).
   - **ADR-024** — the ANC `Settable-toggles` byte's dock-state finding.
   - **ADR-025** and its two dated Update notes — the "GMS reverse-engineering is out of scope"
     decision, and the two 2026-09-08 updates that are the direct ancestors of Phase 1/Phase 2's
     findings in the document you are reviewing under item 2 below.
5. `ARCHITECTURE.md` — full. In particular §2.1 (the `CodecRouter`/`BudsTransport` component table),
   §5 (the per-DLCI `FrameEncoder`/`FrameDecoder` implementation gate and its "No decompiled
   reference exists for DLCI 0x04/0x08, by design, not by gap" note), and §15 (open architecture
   questions).
6. `PROTOCOL.md` — full, **including §6's open-questions checklist and §8's changelog table**. This
   is the project's single source of truth for wire-level protocol knowledge; every APK-derived
   claim you check should be cross-read against what this document already says about the same
   channel/field, since `REVERSE_ENGINEERING.md`'s own claims are frequently phrased as "consistent
   with" or "contradicts" a specific `PROTOCOL.md` entry.
7. `TODO.md` — full. In particular the "Recommended priority order" section at the top and the
   "Targeted research follow-ups" items under Phase 2/3 — several of these are the direct
   predecessors of what `ai-sessions/0001...` claims to have closed.
8. `REVERSE_ENGINEERING.md` — **full**. This is one of the two documents under review — read it
   before forming any opinion about it, not while forming one. Pay particular attention to:
   - The `qhr` / `qjc` / `qja` / `qjn` / `qjt` / `qhx` / `qjv` protobuf oneof family and the
     structural argument (in `qjn`'s own entry) that `qhx`/`qjn`/`qjt` are very likely an
     **alternate product's** settings schema, not the Buds Pro 2's own — this project's own memory
     note flags this as a hypothesis worth checking before extending, so verify the DI/method-overlap
     argument (`fyo` vs. `fyw`/`fyx`, both `extends fxz implements fya`) directly rather than taking
     it as established.
   - The `nqx`/`npy`/`nqo`/`npw`/`nqm` Pigweed `pw_rpc` plumbing trace (`RpcPacket`/`Client`/
     `MethodClient`/`Channel`/`Method`) and its claimed field-for-field match to upstream Pigweed's
     real `pw_rpc/internal/packet.proto`.
   - The UUID register and the (empty-by-design) Message Group/Code register, and the stated
     rationale for why the latter is expected to stay empty.
   - Every entry ADR-018/ADR-019/ADR-020 cite as their evidentiary basis (the `gbm`/`fzd` SDP-UUID
     correlation; the `qhr` field write/read call-site table; the `qjw`/EQ field-16-vs-18 trace).
9. `APK_REVERSE_ENGINEERING_PROCEDURE.md` — full. The pull/decompile/search procedure and the
   AI-assistance boundary it restates from ADR-017 — this governs what *you* are and are not allowed
   to do in this same review.
10. `reverse-engineering/APK_VERSIONS.md` — full. Confirms the single analyzed APK version
    (`v1.0.955078536-10253511`, base + `arm64_v8a`/`xxhdpi` splits) and its JADX/apktool/pbtk tool
    versions — your own citations must target this exact version's decompiled output, at this
    project's own on-disk path (`reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/` and
    `apktool-output/`).
11. `AI_SESSION_LOG_PROCEDURE.md` — full. Governs how *your own* output from this task must be
    logged (see §5 below) — read it now, not only when you reach the logging step, since it also
    explains the header-block/status-field conventions the file you're reviewing (item 12) already
    follows.
12. `ai-sessions/INDEX.md` — full. The registry of every logged AI-session prompt/result pair —
    you will need this again in §5 to find the next free session number for your own output.
13. **`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` — in full.** This is the second, and
    higher-priority, document under review. Read it end to end before opening any APK source file —
    you need its full claim inventory (Phase 0's reading list, Phase 1's Findings and Verdict, Phase
    2's Q4/field-11/field-15/field-13/`fsz` write-ups, Phase 3's per-feature table, Phase 4's proposed
    document edits, and the closing Summary) before you can plan which claims to re-derive and in
    what order.

Optional but useful for context (not required, but read if time allows before finalizing your
review): `ai-sessions/0002_MAINTENANCE_RESULT_2026_09_08.md` and
`ai-sessions/0003_MAINTENANCE_RESULT_2026_09_08.md`, which record what the maintainer has *already*
signed off on from `0001`'s proposals (so you know which specific claims already carry a human
decision behind them, distinct from the ones still sitting as unreviewed proposals).

---

## 2. The task

Produce an **extensive, independent review** of both documents named in §0/§1, with the review's own
weight distributed toward `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` as instructed there.
For every notable claim in either document — every 🟢 FACT-for-code-existence assertion, every 🟡
HYPOTHESIS reading, every "Open question," every "What this does NOT establish" caveat, and every
proposed promotion in `0001`'s Phase 4 — you must:

1. **Independently re-derive it**, not just read it and judge plausibility. Open the actual cited
   file(s) at `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/...` (or the
   `apktool-output/` smali equivalent, where a citation says JADX misdecompiled something) directly,
   at the cited line number(s), and confirm — independently, in your own words, from your own read
   of the bytecode/source — that the code says what the document claims it says. **Never accept a
   citation, or this project's own prior conclusion, at face value.** Where a claim rests on running
   `scripts/decode_rawmessageinfo.py` against a `RawMessageInfo`-style class, re-run that exact
   script yourself (it is a small, dependency-free, already-committed Python file — read it first to
   confirm you understand its decoding logic, then run it) rather than trusting the document's stated
   output.
2. **Check every byte-level/wire-level claim against the actual capture data it cites**, using the
   same `tshark`/Python method the source document itself used (the hex-and-script rule below
   requires this of the *sources* you're checking; apply the same standard to your own verification).
   Where a document gives a `tshark` filter and a frame number, re-run that exact filter against the
   named `captures/CAP-NNN-.../CAP-NNN-btsnoop_hci.log` file and confirm the byte sequence matches
   what's quoted, rather than assuming the quoted hex is accurate.
3. **State a verdict** for each claim, per the Output structure in §4 below.

### 2.1 Assess methodology, not only individual claims

This is an explicit, separate instruction, not a restatement of item 1: for each major search or
trace in `0001` (the Q1–Q3 GMS-boundary search, the Q4 connect-burst-trigger trace, the field-11/
field-15 forward-traces, the exhaustive `queryLocalInterface` sweep, the "not found in this APK"
negative results), assess whether the search was **actually exhaustive**, or whether there are
structural patterns, alternative search strategies, or obvious next steps that were not tried. In
particular:

- For every claimed **negative result** ("no call site found," "zero hits," "not found anywhere in
  the decompiled tree") — re-run the search yourself, with at least one variation the original pass
  did not try (a different keyword, a structural pattern search instead of a literal-string grep, a
  check of the `apktool` smali output where JADX may have silently failed to decompile something).
  Treat a "not found" result as **an honest negative from the searches actually performed**, not as
  proof that nothing exists — this project's own established caution about its own prior passes
  (e.g. `REVERSE_ENGINEERING.md`'s repeated framing of exhaustive-vs-partial sweeps, and `0001`'s own
  "not attempted this pass" scope notes) is the standard your review should hold itself to as well.
- For every claim `0001` itself flags as unresolved or "not traced to its own ultimate caller this
  pass" (e.g. `gjv.p()`'s own caller, `MaestroEndpointService`'s registered gRPC service names,
  `fxm.f`'s own value-emitting producer) — attempt the trace yourself. If you succeed where the prior
  pass did not, that is itself a significant finding to report, not just a confirmation/contradiction
  of an existing claim.
- Explicitly state, for each search you re-run, **what you searched and what you did not** — this
  project's evidence rules treat an unstated search scope as a defect in the finding, not a detail
  that can be left implicit.

---

## 3. Guardrails — this project's standing rules, which apply to your review exactly as they apply
   to any other AI session working on this project

These are not optional stylistic preferences — they are this project's binding rules
(`AGENTS.md`, `PROJECT_RULES.md`, `DECISIONS.md`). Follow them throughout, including in your own
review's write-up:

1. **Four-tier status legend.** Every claim you make about the protocol, the APK, or this project's
   own findings must be explicitly labeled 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION,
   using `PROTOCOL.md` §0's canonical legend (the same one `REVERSE_ENGINEERING.md` and
   `PROJECT_RULES.md` §1 use). Never present a HYPOTHESIS or OPEN QUESTION as a FACT, not even
   implicitly through confident phrasing.
2. **Hex & script rule** (`PROJECT_RULES.md` §1 rule 4a). Any wire-level decoding you perform or
   re-verify must include both the exact command you ran (the `tshark` filter, the Python snippet)
   and the raw hex bytes it operated on — never just the resulting interpretation. This lets the
   maintainer (or a third reviewer) re-run your exact command against the same bytes.
3. **Exact citation, always.** Every code-level claim in your review must cite the exact decompiled
   file **and line number** (e.g.
   `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/defpackage/fyo.java:146-166`)
   — a class/method name alone, or a citation copied from the document you're checking, is never
   sufficient on its own. Your own citation must come from your own independent read of that exact
   location.
4. **Independent re-derivation, not a plausibility read** (restated from §2, because this is the
   single most important instruction in this entire prompt). A review that reads "this claim looks
   reasonable given the surrounding context" without opening the cited file yourself and confirming
   it directly does not meet this project's evidence bar and will be rejected by the maintainer.
5. **ADR-017's AI-assistance boundary.** You may search, list candidates, explain already-surfaced
   code/disassembly, and run already-committed mechanical tooling (`scripts/decode_rawmessageinfo.py`,
   `tshark`, `grep`). You must **never**:
   - Decide that a candidate finding is relevant and should be recorded.
   - Write directly into `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, or `DECISIONS.md`.
   - Promote anything to 🟢 FACT in `PROTOCOL.md`, even implicitly.
   - Write a new `DECISIONS.md` ADR, or amend an existing one (including **no Update note appended
     to ADR-025**, even if your review changes its confidence level in either direction).

   Everything you produce is a **proposal**, explicitly labeled as awaiting maintainer review — never
   committed as settled, per `AGENTS.md` §6/§15 and `DECISIONS.md` ADR-017.
6. **Scope boundary — non-goals.** Do not pursue, decode, or comment further on anything in this
   project's out-of-scope list: Fast Pair Account Linking, Ownership Transfer, or the Accessory
   Non-Owner Service (`DECISIONS.md` ADR-008); Firebase/Analytics/Crashlytics-named code; any attempt
   to decompile or analyze Google Play Services itself (`DECISIONS.md` ADR-025 — GMS
   reverse-engineering is explicitly out of scope; your review of `0001`'s GMS-*boundary* findings
   means checking what the companion app's own code does when it *calls out* to GMS, never opening
   or reverse-engineering GMS's own code). If any of these surfaces incidentally while you're
   checking something else, note it as "out-of-scope, skipped" and move on.
7. **External validation is corroboration only, never a substitute or a new source of
   interpretation.** If you consult an external source (the official Fast Pair specification,
   Pigweed's public documentation, the public `protobuf` runtime source, the public `qzed/pbpctrl`
   project's published *notes* — never its code, per `AGENTS.md` §12) to help you understand a
   mechanism, that source may corroborate or contradict a claim, but it never substitutes for
   independently re-deriving the claim from this project's own APK/capture evidence, and it is never
   itself a new interpretation you introduce without a wire/code citation from this project's own
   material to back it.
8. **Never self-promote to 🟢 FACT, and never write or amend a `DECISIONS.md` ADR** — restated for
   emphasis, since this is the rule an earlier external review of this project's own APK work
   violated (see `DECISIONS.md` ADR-025's context section, which describes exactly this mistake by a
   prior cross-validation pass and how it was caught and corrected). Do not repeat that mistake. Even
   where your own independent re-derivation strongly confirms a claim, your output is a **verdict on
   that claim**, not a promotion of it.
9. **Zero-creativity rule for byte decoding** (`AGENTS.md` §13.6). When parsing hex dumps or
   protobuf-lite `RawMessageInfo` structures, be strictly deterministic. Byte offsets, field
   boundaries, and value interpretations come from the actual bytes and the confirmed/documented
   structure — never from a plausible-sounding guess filled in to make an entry look complete. If a
   byte's meaning isn't derivable from evidence in hand, your output is an explicit 🔴 OPEN QUESTION,
   not an invented interpretation.

---

## 4. Output structure

Structure your review as follows. Use Markdown; this becomes the body of a logged project document
(see §5), so write it as a standalone, complete document, not a chat reply.

### 4.1 Per-finding entries

For every claim you review (organize by source document, then by the section/finding number the
source document itself uses, e.g. "`0001` Phase 1, Finding 1" or "`REVERSE_ENGINEERING.md`, `qhr`
entry, field 13"), record:

- **Claim** — a one- or two-sentence restatement of exactly what is being checked (quote the source
  document's own wording where precision matters).
- **Verdict** — exactly one of:
  - **Confirmed** — your own independent re-derivation matches the claim exactly, with no
    unreconciled discrepancy.
  - **Partially confirmed** — some part of the claim holds under independent re-derivation, but
    another part does not, or could not be checked with the evidence available to you; state exactly
    which part is which.
  - **Contradicted** — your own independent re-derivation produces a different result than the
    claim states. This is a serious finding — show your full work.
  - **Unable to confirm** — you attempted independent re-derivation and could not reach a conclusion
    either way (e.g. the workspace/capture file wasn't available to you, or the trace genuinely dead-
    ends at the same point the source document says it does). State exactly what you tried and where
    it stopped — this is different from simply not attempting the check.
- **Your own citations** — every file+line (or capture file + frame number + `tshark`/Python command
  + hex) you used to reach your verdict, independent of whatever the source document cited (even
  where your citation happens to match theirs exactly — state that you independently confirmed it at
  that location, not that you "checked their citation was there").
- **Status label** — 🟢/🟡/⚪/🔴 per §3 item 1, applied to *your own* verdict, not copied from the
  source document.

### 4.2 Methodology assessment section

A dedicated section (per §2.1 above), separate from the per-finding entries, explicitly stating:

- Which searches you re-ran, with what variation from the original.
- Which searches you did **not** re-run, and why (time-boxing is an acceptable reason, but state it
  explicitly — do not let an unattempted search look identical to a completed clean negative).
- Any case where you found something the original pass missed (a caller, a construction site, a
  match the original search's keyword choice would not have surfaced) — flag these prominently, even
  if they don't change any existing claim's truth value, since finding them is itself evidence about
  the original pass's search exhaustiveness.

### 4.3 Summary for the maintainer

A short closing section, mirroring the style of `0001`'s own closing "Summary (for the maintainer)"
section: what's now more strongly confirmed (by independent re-derivation, not just re-reading), what
changed (anything contradicted, or found where the original pass didn't look), and what remains
genuinely open. State plainly whether, in your assessment, each of `0001`'s Phase 4 proposed
promotions/document edits looks ready for the maintainer to accept as-is, needs a specific
correction first, or should not be promoted given what you found — but frame this explicitly as your
own recommendation for the maintainer's decision, not as a decision itself (per §3 item 5/8 above).

---

## 5. Log your own output, per `AI_SESSION_LOG_PROCEDURE.md`

This review is itself a substantive AI-agent session on this project and must be logged the same way
every other one is (`PROJECT_RULES.md` §5 rule 13a, `AI_SESSION_LOG_PROCEDURE.md` in full — you
already read this in §1 item 11):

1. **Check `ai-sessions/INDEX.md` for the next free `Number`** before assigning one — do not assume
   the number in this prompt file's own filename (`0007`) is still free by the time you run; another
   session may have claimed it in the meantime. Re-check at the time you actually write your result.
2. Save your review as `ai-sessions/<NNNN>_CROSSCHECK_RESULT_2026_09_08.md`, where `<NNNN>` is the
   number you confirmed free in step 1, using the exact header block format
   `AI_SESSION_LOG_PROCEDURE.md` §4 requires:
   ```markdown
   # <NNNN>_CROSSCHECK_RESULT_2026_09_08.md — <one-line title>

   **Number:** <NNNN>
   **Category:** CROSSCHECK
   **Date:** 2026-09-08
   **Title:** <one-line title>
   **Status:** awaiting maintainer sign-off
   ```
   Use `Status: awaiting maintainer sign-off` — your review's conclusions are proposals for the
   maintainer, per §3 item 5/8 above, exactly like `0001`'s own status before its Phase 4 items were
   individually reviewed.
3. Save **this prompt file itself** (or, if you were handed its content pasted directly rather than
   as a file, reconstruct it verbatim from what you were given) alongside your result as
   `ai-sessions/<NNNN>_CROSSCHECK_PROMPT_2026_09_08.md`, with the matching header block (no `Status`
   field on `PROMPT` files, per §4's format).
4. Add a new row to `ai-sessions/INDEX.md` for `<NNNN>`, matching the table's existing format and
   column order.
5. Do **not** edit `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, `TODO.md`, or `DECISIONS.md` directly —
   your proposed edits to those documents belong inside your `RESULT` file's own write-up (mirroring
   how `0001`'s Phase 4 presented its proposals), not applied to the live documents themselves.

---

## 6. A note on tone

Be direct about anything you contradict or cannot confirm — this project's own culture (visible
throughout `DECISIONS.md` and `REVERSE_ENGINEERING.md`) treats a clearly-stated negative result or an
honestly-scoped gap as more valuable than an artificially clean-looking pass. Do not soften a
contradiction to make the review read more favorably, and do not pad the review with confirmations of
claims that were never genuinely in doubt just to appear thorough — spend your effort where it's
actually needed: the newest, least-verified claims in `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0007_CROSSCHECK_PROMPT_2026_09_08.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0007_CROSSCHECK_PROMPT_2026_09_08
