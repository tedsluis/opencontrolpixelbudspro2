# 0056_FEATURE_PROMPT_2026_09_28.md — Build the press-and-hold ANC-mode list (`qhr` field 12, ADR-046) and the "In-ear detection" switch (`qhr` field 2, ADR-047) with real `CAP-056` bytes as fixtures, extend the Group AZ re-test skeleton, and list what else can be improved now

**Number:** 0056
**Category:** FEATURE
**Date:** 2026-09-28
**Title:** Implement the items of `ai-sessions/0055_CAPTURE_RESULT_2026_09_28.md` §8 ("Draft outline for the next FEATURE prompt"), based on
`CAP-056-FINDINGS.md` §1–§4 and §8 — field 12 read + write and field 2 write in the app, nothing else new on the wire, real `CAP-056` bytes as test
fixtures, a green test/lint gate with mutation checks, additions to the `CAP-064` (Group AZ) skeleton, and an inventory of further improvements that
are possible now

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer, Android/Kotlin engineer and technical auditor. This prompt is for a **fresh** Claude Code
session in this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, take the time to read **in full**, in
this order, to understand the ground rules before touching the project: `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`,
`PROTOCOL.md` (in particular §4.1, §4.5 preamble, §4.5.3 and §4.5.5 with **all** their dated Updates, including the 2026-09-28 ones, and §6),
`DECISIONS.md` (**every** ADR, ADR-001 … ADR-047, with every dated Update — in particular ADR-019, ADR-024, ADR-034, ADR-036, ADR-042, ADR-044, ADR-045,
**ADR-046** and **ADR-047**), `TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`,
**`ai-sessions/0055_CAPTURE_RESULT_2026_09_28.md` in full** (the source of this session — especially §1, §4, §5 and §8),
**`captures/CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR/CAP-056-FINDINGS.md` in full** (especially §1–§4 and §8), `CAP-056-EVENT-NOTES.md`
(timeline and step mapping), `ai-sessions/0052_FEATURE_RESULT_2026_09_26.md` (how the ADR-045 settings reads/writes were built — the pattern to follow),
`ai-sessions/0054_FEATURE_RESULT_2026_09_28.md` §5–§12 (I-1, the ANC "only while worn" rule this session's field-2 note refers to, and the `CAP-064`
skeleton you will extend), `captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/CAP-064-EVENT-NOTES.md`, `APP_TESTPLAN.md` and `id_registry.csv`.

Read every Kotlin file you change **in full** before changing it — at least `SettingFrame.kt` (`SettingsCodec`, `WRITABLE_FLAG_FIELDS`, `SettingValue`),
`Maestro.kt` (`READABLE_FIELDS`, `readSettingRequest`), `BudsRepository.kt`, `BudsRepositoryImpl.kt` (the Connect-time reads and the ADR-045 writes),
`BudsRepositoryImplTest.kt`, `SettingsCodecTest.kt`, `SettingsFixtures.kt`, `FakeBudsTransport.kt`, `SafeModeGate`/`writeGate` (ADR-042), `ControlsScreen.kt`,
`SettingsUi.kt`, the ViewModel that feeds the Controls tab, and `CodecRouter` if the settings decode is routed there. Say in the RESULT which files you
read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0056_FEATURE_RESULT_2026_09_28.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase **and** after every item built (which items are done and tested, which files are touched but not yet tested, the
last gate result, where intermediate results live — the scratchpad directory: decoded frames, the mutation script; re-create them if the scratchpad
is gone). A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from
there. It never redoes a finished, recorded step, and never assumes an unrecorded step was done. The prompt keeps its number and date; the RESULT
keeps growing in the same file.

**Nothing is taken on trust, including this prompt, `ai-sessions/0055` and `CAP-056-FINDINGS.md`.** Re-derive every frame and byte you use from
`CAP-056-btsnoop_hci.log` with the commands of `CAP-056-FINDINGS.md` (rule 4a) before it becomes a fixture; if anything here disagrees with the capture
or the code, the capture wins — record the correction in the RESULT.

---

## 1. What the maintainer asked for (chat, 2026-09-28, translated)

1. Implement improvements in the OpenControl app based on the findings of `ai-sessions/0055_CAPTURE_RESULT_2026_09_28.md` and `CAP-056-FINDINGS.md`,
   following §8 "Draft outline for the next FEATURE prompt" of that RESULT (items 1–6, worked out in §2 below). The maintainer's choice in the 0055 chat:
   **"Field 12 list + field 2 switch"**, under ADR-046 and ADR-047 (both accepted in that chat).
2. **Find out which further improvements can be made at this moment** (§4 Phase A task 6) — propose them at the checkpoint; build only what is chosen.
3. Read the core files first (§0) to understand the ground rules before auditing or changing anything.
4. Run everything in phases, strictly sequentially; be able to resume automatically after a token limit (§0).
5. **Strict execution rules (the maintainer's own words, translated):**
   - **No assumptions.** Verify everything.
   - **Validate externally.** Use search tools to validate claims against official documentation (Android Developer docs, Bluetooth Core
     Specification, Fast Pair specification) where needed; cite the URL and the exact sentence.
   - **No sampling.** Review the relevant files 100 %, completely and exhaustively, checking every detail.
   - **Best practices.** Industry-standard technical review methods: **evidence traceability** (every claim links back to a specific capture frame,
     log line or file:line) and **structural integrity** checks (files, registry, links, footers).

---

## 2. The items (from `ai-sessions/0055` §8 — verify each against the capture, the ADRs and the code before building)

| # | What | Rules (ADR) | Fixtures (`CAP-056`, re-derive each) | Wire |
|---|---|---|---|---|
| **F12-R** | Read field 12 at Connect with the other settings reads; decode `4:{12:{1:b 2:b 3:b 4:b}}` into four named booleans in the order **1 = Noise cancellation, 2 = Off, 3 = Transparency, 4 = Adaptive** (`PROTOCOL.md` §4.5.3 2026-09-28 Update) | ADR-046 (amends ADR-036: `Maestro.READABLE_FIELDS` + 12), ADR-034 channel rule | read response **1531** `4:{12:{1:1 2:1 3:1 4:1}}` | one more `ReadSetting 4:12` in the existing sequential Connect read |
| **F12-W** | Write field 12 when the user ticks/unticks a mode: always **all four** booleans; refuse (and send nothing) if the result would leave **fewer than two** selected (the official app's rule, `hgj.java:165–168`) | ADR-046, ADR-042 gate, one write per tap, applied only on the empty `RESPONSE` status OK | writes **1689** `{0 1 1 1}`, **1725** `{1 1 1 1}`, **1786** `{1 0 1 1}`, **1815** `{1 1 1 0}` (Adaptive unticked), **1843** `{1 1 0 1}` (Transparency unticked) — all channel 19, address `00 3b`; `RESPONSE` OK **1697**; mirrored push 1696 | `WriteSetting 4:{12:{…}}` — byte-identical to the official app's for channel 19 |
| **F12-UI** | A "Modes for press and hold" list on the Controls tab, **shown once for both buds** (the write carries no side — 🟢; one shared list on the Buds — 🟡, say so in the UI or the note if the checkpoint wants it), four checkboxes with the four labels, the current value with its receive time, the last box of two cannot be unticked | ADR-046; `ARCHITECTURE.md` §3.1 honest-state rules | — | — |
| **F2-W** | Make the existing read-only "In-ear detection" setting a switch: `WriteSetting 4:{2:0|1}` | ADR-047 (field 2 read already under ADR-036), ADR-042 gate, one write per tap, OK-only | channel 19: **2173** `4:{2:1}`, **4048** `4:{2:0}` (= `CAP-024` 1912/1850); channel 21: **3627** `4:{2:1}`, **2849** `4:{2:0}`; read **1502** `4:{2:0}`; `RESPONSE` OK 2179/2855 | `WriteSetting 4:{2:v}` |
| **F2-NOTE** | A one-line note under the switch on what "off" changes: audio does not pause when a bud is taken out, and the "only while worn" ANC check may not apply (Settable with in-ear detection off is 🔴, `CAP-056-FINDINGS.md` §4) — exact text at the checkpoint | ADR-047 Consequences | — | — |
| **AZ** | Add to the `CAP-064` (Group AZ) skeleton: (a) untick Adaptive in OpenControl, then **three long presses on film** → `Notify` modes `08`, `20`, `80` only; (b) in-ear detection **off via OpenControl** → `RESPONSE` OK and Buds SASS `07 11 00 04 01 02 b0 00`; a bud out on film → **no** `PlaybackStatusChanged`; an ANC Refresh with **no bud worn** → which Settable?; then on again → SASS `… b8 00`; (c) for a **Pixel 7a** run with the official app (outside `CAP-064`'s Pixel 9a scope — say so): "open Customize right while a mode is unticked on the left" (one list or two, `CAP-056-FINDINGS.md` §2) | — | — | as listed |

Also check (Phase A) and handle in the design:

- **Channel 21 for field 12.** `CAP-056` has no channel-21 field-12 write. ADR-046 allows the announced channel with its ADR-034 address; a
  channel-21 field-12 request can only be tested with a clearly labelled supplementary structural byte array next to the real channel-19 fixtures
  (`AGENTS.md` §11). Say how you handle it.
- **The official app's double write at 4344/4361** (a `CLIENT_ERROR CANCELLED` 4360, the write again, two OKs, then `CLIENT_ERROR FAILED_PRECONDITION` 4370,
  `CAP-056-FINDINGS.md` §3): OpenControl must send **one** write per tap and never a `CLIENT_ERROR`; confirm the existing write path cannot produce
  either.
- **A write while the session is being re-opened** (the Buds close DLCI 0x02 on wear changes also with in-ear detection off — ADR-047 Context;
  the official app's write went out 3 s after the tap, 4304→4344): what the existing ADR-045 writes do when the channel is closed (error with reason,
  no queue?) — keep that behaviour; do not add a queue or a retry.
- **The "at least two" rule** is enforced in the app (the UI **and** the codec/repository refuse), not left to the Buds.

---

## 3. Decisions already taken, and what still needs the maintainer

- **Already approved in chat (0055 §5):** ADR-046 (field 12 read + write, one list for both buds, ≥ 2 selected) and ADR-047 (field 2 write, with the
  UI note); the next FEATURE = "Field 12 list + field 2 switch". **Memory rule "Approvals: confirm in chat":** these are recorded ADRs, so building
  them needs no new approval — but every **user-visible text** and the placement choices below are confirmed at the Phase B checkpoint in this chat.
- **To decide at the checkpoint:** the list's title and the four labels (proposed: the official app's "Noise cancellation", "Off", "Transparency",
  "Adaptive"), its place on the Controls tab (under "Press and hold"?), whether it is shown/greyed when a bud's action is "Digital assistant" (the
  official app's own behaviour is ⚪ — not checked in code; check `hgj.java`/its layout before proposing), the message when the user tries to untick the
  second-to-last mode, the F2-NOTE text, and any further improvement from Phase A task 6.
- **Not approved, not in scope:** anything else new on the wire (`SubscribeToSettingsChanges`, a new read or write of another field, a periodic or
  automatic request); anything on DLCI 0x08 (GSND — the `04 05` wear value of `CAP-056-FINDINGS.md` §4 is **not** an app channel, ADR-043); a "worn"
  indicator (W-12a — still class C); I-6 (L/R % from the stream — evidence first). If Phase A finds one of these worth doing, it is a checkpoint question
  with the ADR it would need, not a silent addition. **No** `PROTOCOL.md`/`DECISIONS.md` change unless the maintainer approves one in this chat.

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (§0). Record `git log -1`, `git status --short` and `git fetch && git log --oneline HEAD..origin/main`.
2. Baseline gate before any change: `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` (drop `--offline` only if the
   dependency cache is empty, and say so). Record the unit-test counts per module (`:data`, `:hardware`, `:domain`) from the JUnit XML, the lint
   result per module and the Kotlin compiler warnings. If the baseline is not green, stop and report.

### Phase A — evidence and design check (no code change)

3. Re-derive every fixture of §2 from `CAP-056-btsnoop_hci.log` (pre-filter `bthci_acl.chandle==0x0005`; `bluetooth.addr` works in this log, but check;
   `tshark` needs comma-separated sets: `frame.number in {1531,1689,…}`) with `python3 scripts/pwrpc_decode.py` and `tshark … -T fields -e frame.number
   -e frame.p2p_dir -e data.data`; verify each HDLC CRC-32; write the raw bytes, commands and times into the RESULT. Byte-compare 1689/1725/1786 with
   `CAP-021` 5237/5247/5255 and 2173/4048 with `CAP-024` 1912/1850.
4. Read the code paths in full and write, per item, the exact current behaviour (file:line) and the planned change (types, functions, state, tests):
   how `SettingsCodec` encodes/decodes today (why "field 12 has no representation on purpose"), how the Connect-time reads are sequenced and time-boxed
   (ADR-036: sequential, ≤ 3 s, never retried in a loop), how an ADR-045 write is gated, sent, matched to its `RESPONSE` and applied, how the Controls tab
   renders a setting with its time, and where the in-ear detection read is shown today.
5. List every new `transport.send` call site the design adds (expected: **none** new — the field-12 read joins the existing read sequence, the two
   writes use the existing write path). If a new call site is unavoidable, name the ADR that covers it or stop and ask.
6. **Further improvements possible now (the maintainer's item 2).** Go through, exhaustively: `ai-sessions/0055` §9 and `CAP-056-FINDINGS.md` §11 (open
   items), `ai-sessions/0053` §6 (I-6, W-7, W-11, W-12a, W-12b), `ai-sessions/0054` §14, `TODO.md` (Phase 4/5 and "Known technical debt"),
   `ARCHITECTURE.md` §5a, `APP_TESTPLAN.md` and the app's current code. For each candidate give: the problem with its evidence (frame / file:line), the
   change, layer/files, what goes on the wire, the ADR/decision class (**A** possible within current decisions; **B** needs a new maintainer decision —
   name and draft it; **C** needs evidence first — name the capture step; **D** not possible), risk, test and effort. Examples to check (verify, do not
   assume): showing the field-12 list's receive time and the in-ear-detection value's time consistently; whether the Debug tab's raw-settings view
   should now decode field 12; whether ADR-044's re-open and the I-1 ANC re-check need any wording change when in-ear detection is off; stale TODO or
   `ARCHITECTURE.md` rows that still say "field 12 stays gated".

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

7. One question per decision; each option with pros and cons, one marked "(Recommended)", the exact texts and a small ASCII mock-up of the Controls tab
   in the preview:
   - **F12-UI** title, labels, placement, "Digital assistant" handling, the "at least two" message;
   - **F2-NOTE** text (and whether it is always shown or only while the switch is off);
   - **further improvements** (Phase A task 6): which of the class-A items to build now; class-B items only with their draft decision in the preview.
   Record the answers verbatim in the RESULT. Build only what was chosen.

### Phase C — build F12-R, F12-W, F12-UI (ADR-046)

8. `:data`: `SettingValue` gains a field-12 value (four named booleans); `SettingsCodec` encodes `4:{12:{1:b 2:b 3:b 4:b}}` (all four always) and decodes the
   read and the mirrored push; the encoder refuses fewer than two selected (returns no frame — mirror how `WRITABLE_FLAG_FIELDS` refuses other fields);
   `Maestro.READABLE_FIELDS` + 12. Repository: the Connect-time read includes 12; a `setAncModeList(…)`-style write under `writeGate`, applied only on the
   empty `RESPONSE` OK, otherwise the previous value stays and the reason is shown. `:ui`: the chosen list.
9. Tests (JVM, `FakeBudsTransport`, real bytes with frame/time/command in a comment): (a) decode 1531 → all four selected; (b) encode each of 1689, 1725,
   1786, 1815, 1843's states on channel 19 → byte-identical to those frames (incl. CRC); (c) unticking Adaptive produces exactly 1815's bytes and
   Transparency exactly 1843's (the bit-order test); (d) a request that would leave one mode selected sends **nothing** and reports the refusal; (e) OK
   `RESPONSE` 1697 → the new value with its receive time; no `RESPONSE`/an error status → the old value stays with the reason; (f) the Safe-Mode gate
   blocks the write on an unverified firmware; (g) channel 21: one labelled supplementary structural test (address `00 4b`, channel `0x15`, own CRC);
   (h) the existing ADR-045 tests stay green.

### Phase D — build F2-W and F2-NOTE (ADR-047)

10. `:data`: field 2 joins the writable flag fields (encode `4:{2:0|1}`); repository write as for the ADR-045 flags; `:ui`: the switch and the chosen
    note. Tests: 2173/4048 (channel 19) and 3627/2849 (channel 21) byte-identical; read 1502 → off; OK `RESPONSE` → applied with its time; error →
    unchanged with the reason; gate blocks on unverified firmware.

### Phase E — build the further improvements chosen at the checkpoint (if any)

11. One sub-step per chosen item, each with real-byte tests where bytes are involved; update `ARCHITECTURE.md` rows as each item requires.

### Phase F — gate, mutations, compliance

12. `./gradlew --offline clean`, then the full gate of task 2: all green, no new lint issue, no new suppression, no new compiler warning; record the
    new test counts.
13. Mutation checks (apply with a script, run `:data:testDebugUnitTest` / `:domain:test` / `:hardware:test` as relevant, restore, verify byte-identical
    with `sha256sum`), at least: **M1** booleans 3 and 4 swapped in the field-12 encoder; **M2** the same swap in the decoder; **M3** the "at least two"
    check removed (a one-mode write is sent); **M4** only the changed boolean sent instead of all four; **M5** field 2 written with the inverted value;
    **M6** the write applied before its `RESPONSE`. Each must fail at least one test; record which.
14. Compliance: no `INTERNET`, no new permission, no manifest/Gradle/version-catalog change
    (`git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` empty); list every `transport.send` call site in the diff (expected: none
    new); nothing on DLCI 0x08; only fields 12 (read + write) and 2 (write) added to the codec; every write passes `writeGate` (ADR-042); AGPL headers
    on any new Kotlin file; no MAC address at `INFO` or above (`AGENTS.md` §9).

### Phase G — re-test skeleton (Group AZ) and documentation

15. Extend `captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/CAP-064-EVENT-NOTES.md` (Appendix/steps in its existing layout) with the **AZ** steps of §2
    and the re-test of this session's builds: the list shown with all four ticked at Connect (read 12), untick Adaptive → `4:{12:{1:1 2:1 3:1 4:0}}` → OK,
    try to untick down to one → refused, nothing on the wire; in-ear detection off/on via OpenControl → `4:{2:0}`/`4:{2:1}` → OK and SASS `b0`/`b8`; each with
    the expected screen and HCI bracket, and "Refuted if" lines. Keep `CAP-064` a Pixel 9a/GrapheneOS session; put the Pixel 7a "open Customize right" step
    in a clearly marked separate note (a later Pixel 7a capture, not `CAP-064`) and say so. Update `CAP-064`'s `id_registry.csv` row text if its scope
    changes.
16. Documentation, only what changed: `ARCHITECTURE.md` (§5a settings row: fields 12 and 2 built; the Controls-screen description; §3.1 if a value's
    display rule changes), `APP_TESTPLAN.md` (new steps for the list and the switch; a header note "updated for `ai-sessions/0056`"), `PROJECT.md` (the
    touch-controls scope line: the ANC-mode list built), `TODO.md` (done items; open items stay), `CHANGELOG.md`, `README.md` (status block),
    `ai-sessions/INDEX.md` (the 0056 row). Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` — it must exit 0.
17. Finish the RESULT: a plain-language summary first (what the user will see differently), then per item what was built and where, the fixtures with
    their bytes and commands, the gate table, the mutation table, compliance, the further-improvements inventory with the checkpoint's choices, the
    Group AZ additions, the external sources (URL + quoted sentence), open items. Set the Status per `AI_SESSION_LOG_PROCEDURE.md` §4.
18. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits, one commit per concern
    (`feat(app)` for the code and tests; `docs` for the documentation, the skeleton and the session files), each with a *why* and ending with the
    attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (rebase if CI added a commit, never
    force); nothing from a build directory, `android/.kotlin/`, `.vscode/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Wire discipline.** Only three additions: `ReadSetting 4:12` in the existing Connect read, `WriteSetting 4:{12:{…}}` and `WriteSetting 4:{2:0|1}` — each
  after a user action (the read at Connect), byte-identical to the official app's for channel 19, on the announced channel with its ADR-034 address. No
  `SubscribeToSettingsChanges`, no retry loop, no queue, nothing on DLCI 0x08, no other field.
- **Honest state (`AGENTS.md` §5, `ARCHITECTURE.md` §3.1).** A value changes on screen only after the Buds' `RESPONSE` OK; every value keeps its own
  receive time; a failed write leaves the previous value with the reason. The UI never implies per-bud lists (the wire has none) and never implies that
  in-ear detection off is harmless (F2-NOTE).
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval given
  in this chat (`AGENTS.md` §6). ADR-046/047 cover the build; texts and further improvements are confirmed at the Phase B checkpoint.
- **Tests with real bytes** (`AGENTS.md` §11): fixtures from `CAP-056` (and `CAP-021`/`CAP-024` for the byte comparisons) with frame number, time and
  command in a comment; a hand-built frame only as a labelled supplementary structural test next to real fixtures (the channel-21 field-12 case).
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6); every claim in the RESULT links to a frame, log line or file:line; external claims cite the
  URL and the exact sentence.
- **Scope.** Stay within `PROJECT.md`, ADR-046/047 and the items the maintainer chooses; anything else is a checkpoint question or a RESULT note, never a
  silent addition.
- **Subagents.** A subagent may only read and report; every write is done in the main session and verified; re-derive any number a subagent reports.
- **Files.** Before deleting, moving or renaming any file, diff a fresh listing of the source against the copy (checksums); never `rm -rf` from memory.
- **Commits.** Only after the maintainer confirms the final summary (task 18).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0056_FEATURE_PROMPT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0056_FEATURE_PROMPT_2026_09_28
