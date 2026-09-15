# 0023_CROSSCHECK_PROMPT_2026_09_15.md — Exhaustive APK cross-validation of CAP-047/CAP-050/CAP-051, with full external spec validation

**Number:** 0023
**Category:** CROSSCHECK
**Date:** 2026-09-15
**Title:** Deep, non-sampled cross-validation of `CAP-047` (Group AL), `CAP-050` (Group AG repeat), and `CAP-051` (Group AM) against the decompiled companion APK — every open item, every unresolved byte value, and every unexplained behavior from all three captures searched from as many independent angles as possible, cross-checked for internal consistency, and validated against public/official sources

---

## How to (re)start this prompt — read this paragraph first, every time

This task is designed to be pasted verbatim into a **new Claude Code chat**, including on a
resumption. It is deliberately open-ended and exhaustive (the maintainer's own instruction: search
from as many angles as possible, do not sample, do not assume) — it will very likely not fit in one
pass depending on rate limits/context.

Before anything else: check whether `ai-sessions/0023_CROSSCHECK_RESULT_2026_09_15.md` already
exists.
- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its per-phase status table (created in Phase 0) says
  which phases are already `done`. Skip straight to the first phase not yet marked `done` and
  continue from there — do not redo a `done` phase's work. Regardless of where you resume, you still
  owe the full "Mandatory reading order" below in this session — `AI_SESSION_LOG_PROCEDURE.md` §8
  requires it for every session that acts on this repo, resumption or not, since a new chat has no
  memory of a previous one.

Every phase boundary is a safe stopping point: before ending a turn (whether because the phase is
done, or because you sense a rate limit / context limit approaching), update
`0023_CROSSCHECK_RESULT_2026_09_15.md`'s status table and `Status` header field (per
`AI_SESSION_LOG_PROCEDURE.md` §4/§5: `partial — resumed` while incomplete) so the next session picks
up cleanly. Do not leave a phase half-done without a note in the result file describing exactly what
was and wasn't finished.

## Mandatory reading order (do this first, in this order, in every session that works on this prompt)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §4/§6 (the `.proto`/opcode extraction rule, the per-channel FACT
   gate, and the rule that an AI must never independently promote a 🟢 FACT or write a
   `DECISIONS.md` ADR), §12 (attribution — no code copied, only behavior reconstructed), and §13.6
   (zero-creativity, evidence-only rule for interpreting bytes/code).
2. `PROJECT.md` (full).
3. `PROJECT_RULES.md` (full) — especially rule 1/4a (FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION
   labeling and the hex-and-script rule), rule 3 (evidence traceability — a frame number, video
   timestamp, or file+line for every claim), rule 9/9a (never silently overwrite `DECISIONS.md`/
   `PROTOCOL.md`; rewrite a `CAP-NNN-FINDINGS.md` in place to state only the current truth, rather
   than stacking dated corrections), rule 13/13a, rule 19/20 (scope — this project's own APK
   analysis, not GMS).
4. `DECISIONS.md` — every ADR, ADR-001 through the most recent. Pay particular attention to:
   - **ADR-003 / ADR-017** — the AI-mechanical-assistance boundary for APK work: you may search,
     list candidates, and explain already-surfaced decompiled/disassembled code (including native
     `.so` disassembly), but you never decide relevance and never unilaterally record a new
     `REVERSE_ENGINEERING.md` HYPOTHESIS/FACT entry as settled — every new correlation this session
     finds is a **labeled proposal**, exactly like every prior APK-RE session's own findings.
   - **ADR-018/ADR-019** — the established method for correlating a wire-confirmed field against the
     recovered `qhr` schema (self-describing log messages, literal preference-key strings, a forward
     trace from a named UI fragment) — this is the same method this session applies to `CAP-051`'s
     own still-unexplained field 13 (§2 below).
   - **ADR-025** — Google Play Services reverse-engineering is **out of scope**; an exhaustive prior
     audit (`AUDIT_REPORT_2026-09-07.md` §1.0) already found **no trace** of DLCI 0x04/0x08 transport
     code (frame construction/parsing) anywhere in this companion app's own decompiled source — that
     conclusion is not re-litigated by this prompt. **This prompt's own APK searching is scoped to
     the companion app's own decompiled output only** (`reverse-engineering/apk/v1.0.955078536-10253511/`)
     — never to Google Play Services itself, and never by inferring or guessing what GMS's own code
     might contain.
5. `ARCHITECTURE.md`.
6. `PROTOCOL.md` (full) — especially:
   - §2.3's three-channel table and its "GSND" naming update (DLCI 0x08 "GSND CONTROL", DLCI 0x0a
     "GSND AUDIO", DLCI 0x06 "DEBUG APP", DLCI 0x12 "BTIS" — all from `CAP-033`'s SDP browse, none
     yet matched to anything in the APK).
   - §4.1's `qhr` field 13 material and the DLCI-0x0a burst's §6 open item (with `CAP-047`'s
     2026-09-15 update already applied).
   - §6 in full — every open item is a candidate this session might advance, not just the ones named
     explicitly below. Read the whole section, not just the DLCI 0x0a/`PRIV-001`/field-13 entries.
7. `TODO.md` (full) — especially the Phase 2 (APK reverse engineering) section and the "Targeted
   research follow-ups" list, which already tracks several of this prompt's own angles (the
   `MaestroEndpointService` service-registry gap, the `gjv.p()` caller trace that two prior static
   passes failed to find, the `qjb`/`GetHardwareInfo` correlation).
8. `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md`.
9. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` — §9's Capture Index rows for `CAP-021`, `CAP-033`, `CAP-040`,
   `CAP-047`, `CAP-048`, `CAP-050`, `CAP-051` (and any other row a Phase below turns out to need).
10. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` in full — the `PRIV-001`, `TOUCH-007`, and `CASE-009` rows
    directly, plus a full read so nothing relevant is missed.
11. `DESKRESEARCH_FINDINGS.md` in full — its template, status legend, and every existing entry. This
    prompt's own external-validation phase (Phase 6) follows this document's established pattern
    exactly, and should add its own dated entry there, not invent a new format.
12. `REVERSE_ENGINEERING.md` **in full, start to finish, not just the sections this prompt names
    below.** This is the single most important reading-order item for this task — it is the
    project's entire accumulated APK knowledge, and this session's job is to push every one of its
    threads further, not just re-read the parts already known to be relevant. Pay special attention
    to:
    - The `qhr` entry (field 13's write call sites: `QuickActionsFragment.java:33-98` →
      `fye.a(qhs)`, and `defpackage/gvi.java:19-33`/`gvj.java:105-110` for the physical gesture
      path) — the exact code `CAP-051` tested and found silent on the wire.
    - The `MaestroDeviceSettingsProviderService`/`fhk`/`ges` entry (a second UI entry point into the
      `WriteSetting` pipeline, six case IDs already traced to `qhr` fields).
    - The `ijk`/`ijp`/`ijm`/`iji`/`gsy`/`TrueWirelessHeadset`/`HeadsetPiece`/`FmdRequest`/
      `FmdResponse`/`FmdWorker` entry (the GMS Chimera-brokered AIDL boundary — relevant to
      `CAP-047`'s per-earbud charging-icon observation).
    - The `MaestroEndpointService` entry (still-open: the Dagger/Hilt multibinding's own assembly
      site, naming the actual registered gRPC services, was never found — two prior static-analysis
      passes gave up on it; see `TODO.md`'s own note that a "more targeted search strategy" is
      needed).
    - The `"GSND"` naming-lead entry (§2602 in that document, "a related string family found, not
      the literal abbreviation itself" — read exactly what *was* found, since that is this session's
      own starting point, not a dead end).
    - The "UUID register" and "Message Group / Code register" sections — both already correctly
      documented as expected-empty for DLCI 0x04/0x08 (per ADR-025), but re-check them anyway as
      part of this session's own exhaustive pass, since a new APK-search angle could still surface a
      genuine miss.
    - The "Known limitations of this analysis" section — read what previous sessions already
      flagged as *not yet tried*, since several of those are exactly the "as many angles as
      possible" instruction this prompt is built around.
13. `captures/CAP-047-2026-09-14_05-51-38_06-35-30-Group_AL/CAP-047-FINDINGS.md` and
    `CAP-047-EVENT-NOTES.md`, in full.
14. `captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-FINDINGS.md` and
    `CAP-050-EVENT-NOTES.md`, in full.
15. `captures/CAP-051-2026-09-14_21-42-55_21-44-09-Group_AM/CAP-051-FINDINGS.md` and
    `CAP-051-EVENT-NOTES.md`, in full.
16. `ai-sessions/0021_CAPTURE_RESULT_2026_09_15.md` and `ai-sessions/0022_CAPTURE_RESULT_2026_09_15.md`
    — the two sessions that produced `CAP-050`/`CAP-051`'s and `CAP-047`'s findings respectively;
    read their own "Summary for the maintainer" sections for the condensed version of what this
    prompt is now trying to cross-validate.
17. `CAP-021-FINDINGS.md` §4a and `CAP-008-FINDINGS.md` §5/§6 (the original DLCI 0x0a burst and the
    SCO/eSCO-ruled-out finding, needed context for §3 below) and `CAP-048-FINDINGS.md` §5 (the
    original ADR-024 counter-example, needed context for §4 below).

## Context (from the maintainer, not re-derived here)

The maintainer has asked, in their own words (translated from Dutch, preserved in full below the
horizontal rule at the end of this prompt for the exact original phrasing): create a
self-contained, multi-phase prompt in which the findings from `CAP-047`, `CAP-050`, and `CAP-051`
are matched against the decompiled APK, with a very extensive and deep search of the decompiled APK
for leads. Cross-checks and consistency checks should be performed. Findings should be validated
against internet sources. No assumptions — base conclusions on facts. No sampling — analyze things
completely. Try as many angles as possible to maximize the chance of matching findings. Where
needed, also involve findings from other captures and from `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
`PROTOCOL.md`, `DESKRESEARCH_FINDINGS.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, and `TODO.md`.

**What "matched against the decompiled APK" means here, precisely.** Each of the three captures left
behind specific, well-defined open items — some are wire-confirmed byte values with no known
semantic meaning (`CAP-050`'s seven private-envelope codes), one is a *contradiction* between
confirmed compiled code and confirmed wire silence (`CAP-051`'s field 13), and several are
behavioral anomalies with no code-level explanation attempted yet (`CAP-047`'s ADR-016 tension, the
per-earbud charging-icon asymmetry, the "GSND AUDIO"/"GSND CONTROL" naming leads). This prompt's job
is to take every one of these, individually, and push the APK-search effort on it as far as it can
go — not a single keyword grep and a shrug, but every angle listed in each phase below, and any
further angle the session itself identifies as promising while working. A phase that finds nothing
new is still a valid, reportable outcome (a strengthened negative), exactly as `PROJECT_RULES.md`
§4/§12 already treat a null result in a capture — the same standard applies to an APK search.

**Known, already-settled negatives — do not silently re-litigate these as if they were still open,
but do treat them as the honest starting point, not an excuse to skip searching.** `AUDIT_REPORT_2026-09-07.md`
§1.0 (referenced by `DECISIONS.md` ADR-025) already ran an exhaustive full-tree string/identifier
search across all 12,545 decompiled files for DLCI 0x04/0x08 transport code and found nothing; a
`grep -ri "gsnd"` sweep already found no literal match. This prompt's own searches should be
**broader and structurally different** from what was already tried (see each phase's own "angles"
list) — repeating the exact same `grep` pattern is not new evidence and should not be reported as
if it were.

## Session bookkeeping & resumability

In Phase 0, create `ai-sessions/0023_CROSSCHECK_RESULT_2026_09_15.md` with the header block required
by `AI_SESSION_LOG_PROCEDURE.md` §4 (`Status: partial — resumed` to start) and a status table with
one row per phase (0–8, see below). After finishing each phase, update that row to `done` with a
two-to-four-line summary of what was found/changed, and re-save the file. Do **not** create a new
numbered pair for a resumption — per §5, the same `0023_CROSSCHECK_RESULT_2026_09_15.md` is
progressively appended to.

This prompt does not instruct automatic git commits (project-wide convention: never commit without
being explicitly asked). Mention clearly, at the end of each session's final turn, what is
uncommitted so the maintainer can decide whether to commit before the next session starts.

## Phase 0 — Setup

Complete the Mandatory reading order above in full. Create
`ai-sessions/0023_CROSSCHECK_RESULT_2026_09_15.md` with the required header (`Status: partial —
resumed`) and an empty phase-status table (Phases 0–8, all `not started` except Phase 0 itself).
Confirm the decompiled APK's exact location and version (`reverse-engineering/apk/v1.0.955078536-10253511/`,
`jadx-output/`, `apktool-output/`, `apktool-output-arm64_v8a/`, `pbtk-output/`) and record it in the
result file — if a newer APK version has since been pulled and decompiled (check
`reverse-engineering/APK_VERSIONS.md`), use the version already on disk and note which one; do not
pull or decompile a new APK version as part of this prompt (out of scope — flag it as a
maintainer decision if the on-disk version looks stale).

## Phase 1 — `CAP-051`: why does `qhr` field 13's confirmed write path stay silent on the wire?

**Starting point (already established, not to be re-derived):** `REVERSE_ENGINEERING.md`'s `qhr`
entry already traces field 13 to exactly two compiled, self-describing call sites —
`QuickActionsFragment.java:33-98` → `fye.a(qhs)` (in-app tap) and `defpackage/gvi.java:19-33` /
`gvj.java:105-110` (physical press-and-hold gesture) — both calling `fye.a(qhs.ANC_STATE_ACTIVE)` or
`fye.a(qhs.ANC_STATE_AWARE)`. `CAP-051` (`CAP-051-FINDINGS.md` §3) tested **all four** concrete
actions this call graph predicts (one in-app tap, three physical gestures) and found **zero**
DLCI 0x02 `Sent`-direction payload of any kind for any of them — not a wrong-field write, a
completely absent one. This is a genuine contradiction between confirmed compiled code and confirmed
wire behavior, not merely an unmapped byte value — the highest-priority item in this prompt.

Trace `fye.a(qhs)` (and its full call chain down to whatever ultimately calls `WriteSetting`/`fyo`,
or fails to) **exhaustively, line by line**, from as many angles as apply:

1. **Full method body read.** Read `fye.java`'s complete decompiled source (not just the `a(qhs)`
   method signature already known) — every branch, every early return, every conditional guard.
   Does `a(qhs)` unconditionally call into the `WriteSetting` pipeline, or is there a gate (a
   feature flag, a GServices/Phenotype experiment check, a build-variant check, a null-check that
   could silently short-circuit, a try/catch that swallows an exception, a coroutine/async dispatch
   that could be getting cancelled)?
2. **Compare against a field that *does* fire.** Pick at least one already-confirmed-firing `qhr`
   write call site (e.g. field 4's `fyo.java:124-144`, or field 19's `fyo.java:278-298`) and
   structurally diff its call chain against field 13's. Field 13 is unusual in this project's own
   catalog: it is dispatched through `fye`, not `fyo` — is `fye` a genuinely different
   code path from every other confirmed-firing field, or a thin wrapper around the same `fyo`/`fyb`/
   `fyc` chain the others use? This structural difference itself may be the explanation, or may be a
   red herring — establish which, with file+line evidence either way.
3. **Check whether `fye.a()` is even reached in practice.** JADX/apktool decompilation can miss
   dynamic dispatch, reflection, or R8-inlined call sites. Cross-check with `apktool`'s smali output
   (`apktool-output/smali*/`) for `fye`'s class file directly — does the smali confirm the same call
   graph JADX shows, or does it reveal an additional caller/gate JADX's higher-level view obscured?
4. **Check whether ANC state writes for this specific trigger route through a *different* channel
   entirely**, not a silently-failing DLCI 0x02 write. `qhr` field 13 is `ANC_STATE`-typed — the
   *exact same* semantic content DLCI 0x04's already-FACT Message Stream `Set ANC state` (`0x12`)
   command carries (`PROTOCOL.md` §4.1). Is it plausible `fye.a()` is dead/vestigial code from an
   earlier app version, fully superseded by the DLCI 0x04 path for the specific triggers `CAP-051`
   tested, and only still fires for some other, untested trigger? Search for *every* other caller of
   `fye.a()` beyond the two already known (`grep -rn "fye\."` or the equivalent method-reference
   search across the full decompiled tree) — is `QuickActionsFragment`/`gvi`/`gvj` really the only
   two, or are there others this project's prior passes missed?
5. **Version/build check.** Confirm the exact APK version `CAP-051` was captured against
   (`1.0.955078536`, per its own Log Metadata) matches the exact version decompiled in
   `reverse-engineering/apk/`. If they differ even at the build-number level, that alone could
   explain a compiled-but-inert code path.
6. **Cross-reference the two known callers' own *reachability* conditions.** Is
   `QuickActionsFragment`'s ANC toggle-group screen actually the same screen `CAP-051`'s in-app tap
   used, or a different, less-common entry point (recall `PROTOCOL.md` §4.1's own confirmed ANC path
   is the Device details screen's own toggle row, not necessarily "Quick actions")? If
   `QuickActionsFragment` is a *different* UI surface than what `CAP-051`'s own video shows being
   tapped, that would fully explain the silence without any code contradiction at all — check this
   first, since it may be the simplest explanation. Cross-reference `CAP-051-EVENT-NOTES.md`'s own
   description of exactly which screen/button was tapped.
7. **Record the outcome plainly, whichever it is.** If the trace resolves the silence (e.g. a gating
   condition found, or the tapped screen turns out not to be `QuickActionsFragment` at all), record
   it as a proposed correction to `CAP-051-FINDINGS.md` §3/§6, with file+line citations. If the trace
   is genuinely exhausted with no explanation found, record that too, explicitly, as a still-open
   question — do not force a conclusion per `AGENTS.md` §13.6.

## Phase 2 — `CAP-050`: the seven unmapped DLCI-0x08 zero-length codes, from every angle

**Starting point:** `CAP-050-FINDINGS.md` §3 decoded 14 full reconnect cycles' worth of the seven
codes (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`, `0e 04`) against a video-confirmed
dock-state bracket. Five resolve to "not a match"/"no candidate found" (their nearest non-zero-length
neighbor stays constant regardless of dock state); two (`04 04`/`04 15`) remain inconclusive — their
neighbors (`Group 0x04 Code 0x05`/`Code 0x16`) fluctuate near dock-state changes but do not
reproduce for the same physical configuration across different reconnects (`CAP-050-FINDINGS.md`
§4).

1. **Systematic official-spec sweep, not just the extensions already checked.** `PROTOCOL.md` §2.3
   already establishes DLCI 0x08 structurally mirrors the *official* Fast Pair Message Stream TLV
   shape (`[Group][Code][Length][Value]`) applied to a private Group namespace — this project has so
   far checked its confirmed content against the Hearable Controls, Device Information, SASS, and
   Find Hub Network extension pages only. Using `WebFetch`/`WebSearch`, fetch and read **every**
   other Google Fast Pair specification extension page listed at
   `developers.google.com/nearby/fast-pair/specifications` (the personalized-name extension, the
   silence-mode extension, the battery-notification extension already covered, the "additional data"
   base Message Stream page, and any other extension listed there this project has not yet fetched)
   and check whether any of them document a Group/Code pair matching `0x05`/`0x0c`, `0x04`/`0x02`,
   `0x04`/`0x04`, `0x04`/`0x11`, `0x04`/`0x13`, `0x04`/`0x15`, or `0x0e`/`0x04` — a genuine numeric
   coincidence with a *different* official extension than the ones already checked would be a
   concrete, externally-verifiable lead. Record every page checked and its result (match or no
   match), per `DESKRESEARCH_FINDINGS.md`'s own template — a clean negative across every remaining
   extension page is itself a valuable, reportable result.
2. **Numeric-literal search in the APK, both hex and decimal, both as raw bytes and as symbolic
   constants.** Search the decompiled tree for the literal byte pairs `0x05,0x0c` / `0x04,0x02` /
   `0x04,0x04` / `0x04,0x11` / `0x04,0x13` / `0x04,0x15` / `0x0e,0x04` (and their decimal
   equivalents, `5,12` / `4,2` / `4,4` / `4,17` / `4,19` / `4,21` / `14,4`) appearing together as a
   pair anywhere in Kotlin/Java source, smali, or resource files — not just inside classes already
   known to be Bluetooth-adjacent. A coincidental match outside the expected code area is weak
   evidence on its own, but is exactly the kind of "try as many angles" search this prompt asks for;
   report it honestly at whatever confidence it actually supports.
3. **Deeper structural cross-reference against the already-known `qhr`/`WriteSetting` field register.**
   Some of these seven codes' Group/Code numbers numerically resemble already-mapped `qhr` field
   numbers on the *other* private channel (DLCI 0x02) — e.g. Code `0x0c`=12, `0x11`=17, `0x13`=19,
   `0x15`=21 are all real, already-promoted `qhr` field numbers (12=`qht`, 17=Volume balance,
   19=Mono audio, 21 unmapped — check `REVERSE_ENGINEERING.md`'s `qhr` register for field 21
   specifically, since it may not have been checked yet). This is very unlikely to be more than
   numeric coincidence (DLCI 0x08 and DLCI 0x02 are independently-numbered, unrelated envelopes per
   `PROTOCOL.md` §2.3), but check it explicitly and record the check (and its negative result, if
   negative) rather than leaving it as an unexamined possibility — the maintainer's own instruction
   is to try every angle, not to pre-filter which ones look promising.
4. **`MaestroDeviceSettingsProviderService`'s six case IDs, re-examined for a DLCI-0x08-shaped
   angle.** `REVERSE_ENGINEERING.md`'s `MaestroDeviceSettingsProviderService` entry already traces
   six numbered case IDs (`2102`/`2103`/`2104`/`2113`/`2115`/`2116`) to `qhr` fields — but recall
   `PRIV-001`'s own seven codes are *not* `qhr` fields, they are DLCI 0x08 codes. Check explicitly
   whether any of those six case IDs, or any *other* case ID in that same dispatcher not yet listed,
   route anywhere other than the `qhr`/`fya`/`ftj` chain — specifically, whether any reach a
   *different* accessor that could plausibly correspond to a DLCI-0x08-shaped read/write instead.
5. **Native library disassembly, if applicable.** Confirm again (per `TODO.md`'s 2026-08-30
   groundwork note) whether any native `.so` in this APK version could plausibly hold DLCI-0x08-
   adjacent logic — the existing finding is that `libmaestro.so`/`libgfps.so` do not exist and the
   app's Maestro logic is pure Kotlin, but re-verify this specific claim against the *current*
   decompiled output's actual native-library list
   (`reverse-engineering/apk/v1.0.955078536-10253511/apktool-output-arm64_v8a/lib/`) rather than
   assuming the 2026-08-30 finding still holds without checking — and if any candidate `.so` is
   present, disassemble and search it per `DECISIONS.md` ADR-017 §4's already-approved boundary.
6. **Record the outcome for each of the seven codes individually** — do not summarize "still
   unresolved" as one blanket statement. For the five already-resolved-negative codes, state
   explicitly whether this session's broader search changes that reading at all (most likely: no,
   and that itself is worth recording as a reinforced negative). For the two still-open codes
   (`04 04`/`04 15`), state explicitly what was tried and what, if anything, was found.

## Phase 3 — `CAP-047`: the "GSND" naming leads, the ADR-016 disconnect tension, and the per-earbud charging-icon asymmetry

1. **"GSND AUDIO" (DLCI 0x0a) / "GSND CONTROL" (DLCI 0x08) — broader search than the prior literal
   `grep -ri "gsnd"` sweep.** Per `REVERSE_ENGINEERING.md`'s own "GSND naming lead" entry, a
   *related* string family was already found (read exactly what that entry says before repeating
   any part of it) — follow that lead further. Also try: the string split across resource files
   (`res/values*/strings.xml` and any other resource XML, not just Kotlin/Java source); a
   case-insensitive search for "GSND" *and* for what it might stand for expanded (e.g. "Google
   Sound", "Gsound", a device-class or codec-family name) via `WebSearch` — is "GSND" a term that
   appears in any public Android/AOSP source, a Bluetooth SIG-registered service class name, or a
   Google-internal naming convention documented anywhere public? A `WebSearch`/`WebFetch` pass is
   appropriate here (external validation), not just an APK grep.
2. **Cross-reference DLCI 0x0a's own already-characterized burst content** (`CAP-021-FINDINGS.md`
   §4a: 100% Rcvd-direction, ~5–6 bursty waves, a repeating `6d b6 db`/`7e ee ed` byte-pattern,
   structurally protobuf-tag-shaped `0a d0 01` = field 1, length 208) against `fux.java`'s
   `maestro_pw.*` RPC service catalog (`GetSoftwareInfo`/`GetHardwareInfo`/`SubscribeRuntimeInfo`/
   `SetWallclock`/`WriteSetting`/`ReadSetting`/`SubscribeToSettingsChanges`/`SubscribeToOobeActions`)
   — is there **any** RPC method in that catalog (or any not yet catalogued — re-run the search for
   further `maestro_pw.*` service/method name literals across the full tree, not just the eight
   already known) whose *name* suggests a bulk/segmented data transfer, consistent with the burst's
   own already-documented "segmented bulk-data or capability/diagnostic dump" character? This is on
   DLCI 0x0a, a *different* channel from `libmaestro`'s own DLCI 0x02 — but the "GSND AUDIO" naming
   and the audio-adjacent RFCOMM channel number (channel 5, immediately after HFP) both suggest it
   could plausibly still be an app-reachable RPC service, unlike DLCI 0x04/0x08 which ADR-025 already
   excluded on direct evidence. Check explicitly whether this exclusion actually applies to DLCI
   0x0a too, or whether that was only ever checked for 0x04/0x08 — re-read `AUDIT_REPORT_2026-09-07.md`
   §1.0's original scope (referenced via ADR-025) to confirm which DLCIs it actually covered.
3. **ADR-016 disconnect-on-redock tension** (`CAP-047-FINDINGS.md` §4: identical `Settable-toggles=0x00`
   dock-sensor reading in three swapped-slot dockings, but only two of three produce an ACL
   disconnect). Since the sensor/disconnect mechanism itself is GMS/firmware-side (out of scope per
   ADR-025), search instead for whether the **companion app's own** code (its Room `device_info`
   table consumer, `gck`/`gcl`/`eht` per `REVERSE_ENGINEERING.md`'s ADR-025-era entry, or any
   dock-state-debounce/hysteresis logic in the app's own domain layer) reveals anything about how the
   app itself *reacts* to a dock-state change that could hint at a client-side factor (e.g. does the
   app itself ever suppress or delay acting on a dock-state notification under some condition?). This
   is a genuinely long-shot angle — report a clean negative plainly if nothing is found, rather than
   speculating about GMS-internal behavior this project cannot inspect.
4. **Per-earbud charging-icon asymmetry** (`CAP-047-FINDINGS.md` §1's bonus observation — swap
   attempt #2 shows only the Right icon charging, swap attempt #3 shows only the Left icon charging,
   never both). Cross-reference against the `IFastPairDeviceDetailService`/`TrueWirelessHeadset`/
   `HeadsetPiece` entry (`REVERSE_ENGINEERING.md`) — does `HeadsetPiece`'s own field set (`batteryLevel`,
   `charging`, `lowLevelThreshold`, ...) reveal anything about how the *charging* boolean specifically
   is derived or displayed (e.g. is it a raw pass-through of a GMS-supplied value with no app-side
   logic at all, which would mean this asymmetry is not explainable from the APK side either)?
   Record the finding either way.

## Phase 4 — Broad additional-angle sweep (not tied to one specific capture's open item)

The maintainer's own instruction is to try as many angles as possible, not just the ones named
above. At minimum, attempt each of the following, and add any further angle this session itself
identifies as promising while working through Phases 1–3:

1. **`MaestroEndpointService`'s Dagger/Hilt multibinding assembly site** — still not found by two
   prior static-analysis passes (`REVERSE_ENGINEERING.md`'s own entry, `TODO.md`'s "Targeted research
   follow-ups"). Try a *different* search strategy than the generic-token searches already attempted
   and already judged unproductive (e.g. search for the multibinding's own `@IntoMap`/`@Provides`
   annotation-adjacent metadata strings, or search apktool's smali output directly for the
   `Map<String, Optional<ofd>>` construction site by its exact generic-signature shape rather than by
   a method-name token).
2. **`gjv.p()`'s own caller** — flagged in `TODO.md` as needing "a more targeted search strategy" than
   the two already-tried generic-token searches. Propose and attempt at least one genuinely different
   strategy (e.g. a call-graph search anchored on `gjv`'s constructor call sites instead of its `.p()`
   method reference, or a smali-level cross-reference search for every `invoke-virtual` targeting
   `gjv`'s class descriptor).
3. **Full manifest re-review** — re-read `AndroidManifest.xml` (both the JADX-reconstructed version
   and, if it differs, apktool's raw version) end to end for any exported service, receiver, provider,
   or intent-filter not already catalogued in `REVERSE_ENGINEERING.md`, cross-checked against every
   open item in `PROTOCOL.md` §6 and `TODO.md` for a possible connection.
4. **Resource/string-table sweep** — search `res/values*/strings.xml` and any other resource XML for
   any string literal that could plausibly relate to any of this session's open items (dock-state
   labels, "GSND"-family names, ANC-state debug strings, or anything else) that a Kotlin/Java-source-only
   search (the method most prior passes have used) would miss.
5. **Any candidate `.so` disassembly not yet attempted** — per Phase 2 item 5's check, if a candidate
   native library turns out to exist that hasn't been disassembled/searched yet, do so now (in scope
   per `DECISIONS.md` ADR-017 §4).

## Phase 5 — Cross-checks and consistency checks

For every new correlation, negative result, or partial finding produced in Phases 1–4:
- Cross-reference it against `PROTOCOL.md`'s full current text (not just the sections already read
  in Phase 0) for any contradiction with an existing FACT-level claim — flag, do not silently
  resolve, any contradiction found (`AGENTS.md` §6).
- Cross-reference it against `DECISIONS.md`'s full ADR history for the same reason.
- Cross-reference it against `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s existing Test-ID catalog — does any
  new finding suggest a Test-ID's `Existence source`/`Note`/`Evidence` column needs an update, or
  that a new Test-ID should be proposed (as a labeled proposal, per `AGENTS.md` §6)?
- Cross-reference it against `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index and Group definitions
  for the same reason.
- Cross-reference it against `TODO.md`'s own tracked open items — close out (mark resolved, with a
  pointer to the evidence) any item this session's work actually resolves; do not silently leave a
  now-stale "still open" framing standing.
- Where two or more of this session's own findings (across Phases 1–4) bear on each other, reconcile
  them explicitly within this session's own write-up rather than leaving an internal contradiction
  for a future reader to notice.

## Phase 6 — External validation

For every claim in Phases 1–4 that maps to a public, external, checkable source (an official Fast
Pair specification page, Pigweed's `pw_rpc`/`pw_hdlc` public documentation, the Bluetooth SIG's
assigned-numbers/service-class registry, AOSP source, or any other public reference), validate it
directly via `WebFetch`/`WebSearch` and cite the exact page/source, following
`DESKRESEARCH_FINDINGS.md`'s own established template (Trigger / Method / Captures examined / Result
/ Promoted to). Add a new, dated entry to `DESKRESEARCH_FINDINGS.md` for this session's own
external-validation work — do not fold it silently into another document's prose. An agreeing
external source raises a finding's confidence toward HYPOTHESIS; it does not itself authorize a 🟢
FACT promotion (per `AGENTS.md` §6 — that still requires explicit maintainer sign-off).

## Phase 7 — Write-up

For each of the three captures, update its own `CAP-NNN-FINDINGS.md` **in place** (per
`PROJECT_RULES.md` rule 9a — rewrite the relevant section to state the current, complete truth
including this session's own advances, rather than appending a dated "Update" block) with:
- Any correlation found, labeled at the confidence level the evidence actually supports
  (🟢/🟡/⚪/🔴 per `PROJECT_RULES.md` §1), with the exact command/search used and file+line or
  frame-number citations (`PROJECT_RULES.md` rule 3/4a).
- A clean, explicit statement for every open item that was searched but not resolved — "searched via
  X/Y/Z, no match found" is a complete, valid entry; do not leave an item looking untried when it was
  actually tried and came back negative.

Also update, as needed:
- `REVERSE_ENGINEERING.md` — new classes/methods found, new correlations to wire evidence, updates
  to the "Known limitations of this analysis" section reflecting what this session tried and what
  remains genuinely untried after it.
- `PROTOCOL.md` §6 — dated updates to the relevant open items (DLCI 0x0a identity, `PRIV-001`'s seven
  codes, field 13's write-path silence), following the non-destructive-update convention.
- `TODO.md` — close out or refine any "Targeted research follow-ups" item this session actually
  advances.
- `DESKRESEARCH_FINDINGS.md` — the new dated entry from Phase 6.
- `id_registry.csv` — only if a genuinely new ID is introduced (unlikely for this prompt, but follow
  the existing discipline if it happens).

**Never independently promote anything to 🟢 FACT in `PROTOCOL.md`, and never write or alter a
`DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15) — every new
correlation this session finds, however strong the evidence, is written up as a clearly labeled
proposal awaiting sign-off, exactly like every prior APK-RE session's own findings in this project.

## Phase 8 — Wrap-up and maintainer summary

- Re-read every file this session modified; confirm every substantive claim cites a frame number,
  video timestamp, file+line, or an external URL (`PROJECT_RULES.md` rule 3).
- Run `python3 scripts/lint_docs.py`; fix any newly introduced unregistered-ID or dead-filename-
  reference finding (a pre-existing, already-noisy finding elsewhere in the repo is not this
  session's responsibility to fix).
- Run `./scripts/ensure_footers.py` if any new or edited `.md` file is missing its footer.
- Update `ai-sessions/INDEX.md`'s row for `0023` to match this session's final `Status`.
- Finalize `0023_CROSSCHECK_RESULT_2026_09_15.md`: `Status: complete` only if every phase's work is
  actually done and no proposed FACT-promotion/ADR is still pending maintainer review; otherwise
  `Status: awaiting maintainer sign-off` (if the only thing left is maintainer review of proposed
  promotions/proposals) or `partial — resumed` (if genuine work remains).
- Write a final summary for the maintainer: for **each** of `CAP-047`/`CAP-050`/`CAP-051`'s open
  items this prompt targeted, state plainly whether a new correlation was found, what it is, and at
  what confidence — or that it was searched exhaustively (list exactly how) and remains genuinely
  unresolved. Include an explicit list of every proposed 🟢 FACT promotion, `DECISIONS.md` ADR, or
  new Test-ID awaiting sign-off, clearly labeled as proposals per Guardrails.

## Guardrails

- **This session's own APK searching is scoped to the companion app's decompiled output only**
  (`reverse-engineering/apk/v1.0.955078536-10253511/`, or whatever version is actually on disk per
  Phase 0) — never Google Play Services, per `DECISIONS.md` ADR-025. Do not pull, decompile, or
  reason about GMS's own code.
- **Never independently promote a finding to 🟢 FACT in `PROTOCOL.md`, and never write or alter a
  `DECISIONS.md` ADR, without explicit maintainer approval** (`AGENTS.md` §6/§15). Propose and label
  clearly as a proposal in each capture's own `CAP-NNN-FINDINGS.md` and in the Phase 8 summary
  instead — this applies even where an external source (Phase 6) agrees with your reading.
- **No assumptions.** Every claim is FACT/HYPOTHESIS/ASSUMPTION/OPEN QUESTION labeled per
  `PROJECT_RULES.md` rule 1; a byte, string, or code path whose meaning isn't derivable from evidence
  actually in hand is an explicit open question, not a filled-in guess (`AGENTS.md` §13.6).
- **No sampling.** Every search described in Phases 1–4 is run to completion across its full stated
  scope (the entire decompiled tree, every relevant spec page, every candidate class) — a partial
  pass through a large search space, reported as if it were the whole thing, is not acceptable
  anywhere in this task, matching the maintainer's own explicit instruction for this prompt.
- Apply the hex-and-script rule (`PROJECT_RULES.md` rule 4a) to every decoded byte sequence: the
  exact command **and** the raw hex bytes, not just the interpretation. Apply the equivalent
  discipline to every code citation: the exact file+line, not just a paraphrase.
- Use `git mv` for any file rename in this task — never a plain `mv`. No rename is expected.
- This is a research/documentation task — no new Bluetooth capture or hardware action is performed
  as part of this prompt (no new `CAP-NNN` session is created).

## Output

At the end of each phase, a short note (in the RESULT file, and to the maintainer if the chat is
still live) of what was found/changed. At the end of Phase 8, the full summary described there.
Every substantive claim in any updated `CAP-NNN-FINDINGS.md`, `REVERSE_ENGINEERING.md`, or
`DESKRESEARCH_FINDINGS.md` entry must cite its evidence (frame number, video timestamp, file+line, or
external URL) per `PROJECT_RULES.md` rule 3.

---

**Original maintainer instruction (Dutch, preserved verbatim for exact phrasing):**

> Maak een nieuwe prompt in het engels voor claude code in ai-sessions/ die zelfstandig kan draaien
> in meerdere fases, waarin: de bevindingen uit CAP-047, CAP-050 en CAP-51 worden matcht met de
> gedecompileerde APK en waarbij zeer uitgebreid en diep in de gedecompileerde APK gezocht wordt naar
> aanknopingspunten. Voer cross checks en consistency checks uit. Valideer bevindingen met bronnen op
> internet. Doe geen aannames, maar baseer je op feiten. Doe geen steekproeven, maar analyseer zaken
> volledig. Probeer zoveel mogelijk invalshoeken om zoveel mogelijk kans te hebben om bevindingen te
> matchen. Wanneer nodig betrek ook bevindingen van andere CAP's en uit
> TESTPLAN_BLUETOOTH_HCI_SNOOP.md, PROTOCOL.md, DESKRESEARCH_FINDINGS.md,
> CAPTURE_BLUETOOTH_HCI_SNOOP.md en TODO.md

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0023_CROSSCHECK_PROMPT_2026_09_15.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0023_CROSSCHECK_PROMPT_2026_09_15
