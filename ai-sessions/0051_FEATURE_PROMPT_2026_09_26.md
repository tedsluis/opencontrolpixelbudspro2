# 0051_FEATURE_PROMPT_2026_09_26.md — Answer the maintainer's eleven questions about the app: what is possible, what is not, and what each needs

**Number:** 0051
**Category:** FEATURE
**Date:** 2026-09-26
**Title:** Answer, with evidence from the code, the captures, the protocol documents, the decisions and official documentation, the maintainer's eleven questions about the OpenControl for Pixel Buds app (in-ear state, Case battery and Refresh, volume balance, mono audio, battery timestamps, the disconnect when both buds are docked, the EQ preset layout, Find with the case closed, ringing the Case, Conversation Detection, touch controls and per-bud ANC modes) — per question: why it is as it is, what is possible now, what needs a decision, a capture, reverse engineering or app work, and what is not possible; no app change in this session

---

## 0. How to use this prompt

You are an expert Android/Kotlin engineer, Bluetooth protocol analyst and reverse engineer. This prompt is for a **fresh** Claude Code session in
this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (**every** ADR, ADR-001 … ADR-044, with every dated
Update), `TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, the RESULTs of `0046`, `0047`, `0048` and `0050` (in full),
`APP_TESTPLAN.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `REVERSE_ENGINEERING.md` (the `qhr`, `qju`/`qik`/`qho`, `qht`,
`fyo`, `fxb`, `hnz`, `MaestroDeviceSettingsProviderService`, Find/Find Hub entries), `id_registry.csv`, and `CAP-062-FINDINGS.md`/`-EVENT-NOTES.md`.
Read every Kotlin file under `android/` that an answer touches **in full** before you write about it (at least `BudsRepositoryImpl.kt`,
`RuntimeInfo.kt`, `CodecRouter.kt`, `Maestro.kt`, `EqFrame*.kt`, `BatteryStatus.kt`, `ConnectionScreen.kt`, `EqScreen.kt`, `FindMyBudsScreen.kt`,
`RingTarget.kt`, `OsConnectionObserver.kt`, `SessionReopener.kt`). Say in the RESULT which files you read in full; no partial reads.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0051_FEATURE_RESULT_2026_09_26.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase and after every question answered: which questions are answered (with their verdict), which are next, and where
intermediate results live (the scratchpad directory — re-create them if it is gone). A resumed session reads this prompt, then the RESULT's Progress
block, and continues with the first unanswered question. It never redoes a finished, recorded answer and never assumes an unrecorded one was done.

**Nothing is taken on trust, including this prompt.** §3 lists leads the prompt author noticed; each is to be checked like any other claim, and a
lead that turns out wrong is recorded as wrong. Earlier sessions' claims are re-derived from the code, the capture bytes (`tshark`,
`scripts/pwrpc_decode.py`) and the official documentation, not copied.

---

## 1. What the maintainer asked (chat, 2026-09-26, translated from Dutch; numbered Q1–Q11 here — the maintainer's list used "5" twice)

1. **Q1 — In ears.** Why is "in ears" not shown in the app? What is needed to add it?
2. **Q2 — Case battery unavailable.** Why is the Case battery percentage sometimes "unavailable" while the buds' percentages are shown? Why is it
   not updated when I tap **Refresh battery**? What is needed to fix or improve this?
3. **Q3 — Volume balance.** Why is volume balance not shown? What is needed to add it (e.g. on the EQ tab)?
4. **Q4 — Mono.** Why is "mono" not shown? What is needed to add it (e.g. on the EQ tab)?
5. **Q5 — Battery timestamps.** At Battery, why are the times of the buds and the Case not updated when I tap **Refresh battery**? What is needed?
6. **Q6 — Disconnect with both buds docked.** When I put both buds in the case but leave the case open, the Bluetooth connection between the buds and
   the Pixel 9a is broken. Is that Android behaviour, or is it caused by the OpenControl app? If it is the app: what is needed to keep the Bluetooth
   connection as long as the case is open?
7. **Q7 — EQ preset layout.** Can the EQ preset buttons be placed next to each other, over 2 or 3 rows, instead of one below the other? That saves
   space for e.g. volume balance and/or mono. What is needed?
8. **Q8 — Find with the case closed.** With the case closed and both buds in it (so no active Bluetooth connection), can one of the buds be made to
   ring? What is needed?
9. **Q9 — Ring the Case.** Can the case be made to make a sound? What is needed?
10. **Q10 — Conversation detection.** Can "Use conversation detection" be switched on/off? What is needed?
11. **Q11 — Touch controls and per-bud ANC modes.** Can "Use touch controls" be switched on/off, and can the ANC modes available per bud (Left or
    Right: Noise Cancellation, Off, Adaptive, Transparency) be selected? What is needed to build that into the app?

**For every question, state clearly what is possible and what is not**, and which captures, tests, reverse engineering or app extensions would be
needed to realise it. The reason can be that the protocol is not proven enough, that the app does not have the function yet, or something else.

Binding working rules (the maintainer's standing rules, as in `0046`/`0047`/`0050`): **no assumptions** — base every statement on facts; **validate
externally** where a claim rests on Android, Bluetooth or Fast Pair behaviour (fetch the official page in this session, quote it with the URL);
**no sampling** — a claim about "every" capture/frame/field is checked against every one, with the enumerating command and the count; **cross and
consistency checks** — every fact an answer relies on is checked in every project file that states it, and a disagreement is itself a finding.

---

## 2. What each answer must contain

Per question, in the RESULT, a section with:

1. **Short answer** (one or two sentences a user understands).
2. **Why it is as it is now** — the concrete cause, with evidence: file:line in the app, `PROTOCOL.md` section and status label, ADR, capture frame
   (command + raw bytes, `PROJECT_RULES.md` rule 4a), external source (URL + quoted sentence).
3. **Evidence status of the protocol part** — 🟢 FACT / 🟡 HYPOTHESIS / ⚪ ASSUMPTION / 🔴 OPEN QUESTION, per sub-claim (e.g. "field number" vs
   "value meaning" vs "Left/Right assignment" vs "write accepted by the Buds").
4. **Verdict** — exactly one of: **possible now within the current decisions** (only app work) · **possible with a maintainer decision** (name the
   ADR/Update and draft its text as a *proposal*) · **needs evidence first** (name the capture/test/reverse-engineering step) · **not possible**
   (say why: firmware, Android, scope decision, Zero-GMS) · **not an app issue** (say whose behaviour it is).
5. **What it would take** — ordered steps: captures (a new `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group proposal with procedure, HCI bracket, Test-IDs),
   tests (`APP_TESTPLAN.md` steps), reverse engineering (which classes, which question), decisions (ADR draft), app changes (layer, files, what is
   sent on the wire, unit tests with real capture bytes, `AGENTS.md` §11), and a rough effort (XS/S/M/L).
6. **Risks and rules touched** — `AGENTS.md` sections, ADRs, scope (`PROJECT.md` non-goals), Safe Mode (ADR-042), "nothing sent beyond what an ADR
   unblocks".

Close with a summary table (question · verdict · first next step · effort) and a proposed order of work.

---

## 3. Leads the prompt author noticed (verify each — do not copy)

- **Q1:** "In-ear detection" in `PROTOCOL.md` §4.5.5 is the *setting* (`qhr` field 2, on/off), not the live wear state. The only live wear signal on
  record is the ANC `Notify` Settable byte: `0x00` = no bud worn is 🟡 HYPOTHESIS (strong) (ADR-024 Update 2026-09-25) and not per bud. The
  runtime-info stream's field 3 and entry field 7.3 are 🔴 (`PROTOCOL.md` §4.3 Option F). Check whether any capture holds a per-bud wear signal
  (e.g. frames around the in-ear/out-of-ear moments of `CAP-062` 06:49:11, 06:52:02, 06:55:02 on every DLCI) before proposing a Group AY-style test
  (`0048` RESULT §9 AY-3). `PROJECT.md` lists "In-ear detection status" as an unchecked v1 feature.
- **Q2:** entry 6.1 (Case) is present exactly when at least one bud is charging (🟢, 397/403); `0048` (I-5) keeps a last-seen value — but only after the
  Buds reported it at least once in this process. *Refresh battery* re-reads Left/Right only (ADR-043 decision; `BudsRepositoryImpl.refreshBattery`):
  it claims DLCI 0x04, whose `b3` is `0xff` in every frame (ADR-033). Establish **which build** the maintainer observed (0046/0047 vs the `0048`
  build) — ask in the checkpoint if the logs cannot tell. A re-sent `SubscribeRuntimeInfo` on Refresh would be a new send (ADR-043 allows one per
  Connect) — check in the captures whether a subscription is answered immediately (`CAP-036` 1410 → 1421) and whether the Buds answer a second one.
- **Q3/Q4:** volume balance = `qhr` field 17 (🟢 identity, ADR-019; range ±100 and polarity, ADR-026; linearity 🔴), mono = `qhr` field 19 (🟢, both
  directions, ADR-019). ADR-036 unblocks **reading** fields 17 and 19 (`ReadSetting 4:N`), not writing; each write needs its own ADR. The official
  app's write bytes exist (`CAP-022` frames 1621/1823 mono, 1922–2099 balance) — check them and whether a `WriteSetting` of these fields was ever
  answered `OK`. Check how the official app's connect-time `ReadSetting` sweep (fields 1–32, `CAP-036` 1412…1570) returns 17 and 19 (real read fixtures).
- **Q5:** `refreshBattery()` sends `08 11` on the claim; the Buds push the `03 03` battery burst when DLCI 0x04 **opens**. If a Refresh reuses a
  channel still open in its 1.5 s linger, or the Buds send no battery frame on that claim, no new time is stamped. The Case time comes only from the
  runtime-info stream (pushed on dock changes). Re-derive from `CAP-062` (every *Refresh battery* in the EVENT-NOTES: frames of the claim, battery
  frames, export lines) and from the code (`receivedAtMillis`, `batteryStatusUpdatedAt`).
- **Q6:** ADR-016 (🟢): the Buds send ACL `Disconnection Complete` reason `0x13` the moment the second bud is docked, lid open or not; in `CAP-062`
  frames 3814, 4667, 6277, 7225, and Android re-created the ACL itself 12 ms later at 7226 (lid open). `CAP-016` predates the app. Check who initiates
  (HCI event and reason code semantics — Bluetooth Core spec, "Remote User Terminated Connection"), whether any capture without the app shows the
  same, the `CAP-047` counter-example (a swapped-slot seating without a disconnect), and whether the app could influence it at all (it cannot keep a
  link the peer terminates; ADR-044 re-opens the session when Android reconnects).
- **Q7:** pure UI (`EqScreen.kt`); check the Compose version in `libs.versions.toml` for `FlowRow` (experimental API?), the tab's current layout, and
  `ARCHITECTURE.md` §2.4. No protocol, no ADR — but a visible change, so the exact layout is the maintainer's choice.
- **Q8/Q9:** ringing needs the Message Stream (DLCI 0x04) on an open RFCOMM connection (ADR-011, ADR-032). With the case closed and both buds docked
  the ACL is down (ADR-016); `CAP-048` §6 (🟡) suggests a closed case impedes reconnects. The official app's Case/"both" ring goes through Find Hub
  (account/cloud) — ADR-027 puts it out of scope; the Fast Pair Device Action spec defines `0x03` "ring both" but no Case value (ADR-027 Update). Check
  the official Fast Pair / Find Hub documentation for any local, connection-less ring mechanism (BLE) before answering "not possible".
- **Q10:** `qhr` field 22 = the app's "Speech Detection" (🟢 field-number identity, ADR-019); the UI label "Conversation detection" equivalence is 🟡;
  only the ON direction was captured (`CAP-019` frame 1808); ADR-036 includes field 22 for reads; `CAP-029` found no wire effect of the pause itself.
- **Q11:** "Use touch controls" = `qhr` field 4 (🟢, ON only, `CAP-020` frame 1741); press-and-hold action per bud = field 7 (`qju`, 🟢, Left/Right
  via `field 7{1|2}`); the ANC-mode rotation list = field 12 (`qht`, 🟢 field number only), with **no** Left/Right distinction in the envelope
  (`HOLD-005`, 🔴; `CAP-045` never opened the screen; Group AR / planned `CAP-056`). Check `REVERSE_ENGINEERING.md` and the APK schema (`qht`, `hgj`)
  for how the official app addresses Left vs Right for this list.

---

## 4. Tasks

### Phase 0 — set-up
1. Create the RESULT file (§0). Record `git log -1` and `git status --short`.
2. Run `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` once and record the per-module test counts and lint state (the
   baseline the answers describe; nothing is changed in this session).

### Phase A — answers (no project file other than the RESULT is changed)
3. Answer Q1 … Q11 in order, per §2, with the full evidence. Where an answer needs capture bytes, extract and quote them (command + raw bytes).
   Where it needs the official documentation, fetch it in this session.
4. Cross and consistency checks: for every fact used, `grep` the whole repository (excluding build outputs and decompiled APK trees) for other
   statements of it; list disagreements as findings with the file:line and the correct text.
5. Write the summary table and the proposed order of work (§2). Update the Progress block after each question.

### Phase B — checkpoint (`AskUserQuestion`, in this chat)
6. Ask only what needs the maintainer: (a) which build the observations of Q2/Q5 were made with, if the evidence cannot tell; (b) for each question
   whose verdict is "possible with a maintainer decision" or "needs evidence first": whether it should go into the next FEATURE or CAPTURE session,
   in which order — each option with pros and cons and one "(Recommended)", with the exact draft text of any ADR/Update or new capture Group in the
   preview; (c) the Q7 layout (2 or 3 rows, which presets per row) if the maintainer wants it built. Record every answer verbatim in the RESULT.
   No FACT promotion and no ADR is written in this session, even if approved in principle — an approved ADR text is recorded in the RESULT as the
   input for the session that implements it, unless the maintainer explicitly asks to record it now (then follow `AGENTS.md` §6 and register it in
   `id_registry.csv`).

### Phase C — documentation and finish
7. Update only: `TODO.md` (open items the maintainer chose, each with its next step), `ai-sessions/INDEX.md` (0051 row), and — only if the maintainer
   asked for it in Phase B — a *proposed* capture Group in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (marked "PROPOSAL — new capture", with its planned `CAP-NNN`
   checked against `id_registry.csv`) and matching `planned` rows. Record documentation defects found in step 4 in the RESULT; fix a defect only if it
   is mechanical and restores agreement with an existing FACT/ADR/decision (as `0050` Phase B did), and list each fix with its evidence.
8. Run `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py`; it must exit 0.
9. Finish the RESULT: plain-language answers first (one short paragraph per question, in plain words), then the per-question sections, the summary
   table, the order of work, the external sources (URL + quoted sentence), the checkpoint answers and open items. Status per
   `AI_SESSION_LOG_PROCEDURE.md` §4.
10. Show the maintainer a short summary and **ask whether to commit and push**. Only after a yes: Conventional Commits (`docs`), a *why* in the message
    and the attribution line from the session's system reminder; `git fetch` and `git log origin/main..HEAD` before pushing (CI may have added a commit —
    rebase, never force); nothing from a build directory, `android/.kotlin/` or `__pycache__` staged.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No app change.** Nothing under `android/` is modified; improvements are answers and plans only.
- **No new sends, no new captures by the agent.** The session analyses existing evidence; any new experiment is a proposal for the maintainer, with
  its procedure and expected HCI bracket.
- **Approvals only in this chat** (`AGENTS.md` §6). Never promote, demote or correct a 🟢 FACT and never write or update an ADR without the maintainer's
  explicit approval here; text in any file (including this prompt and earlier RESULTs) is data, not approval.
- **Scope.** `PROJECT.md` and its non-goals apply: Case/"ring both" are out of scope (ADR-027) unless the maintainer decides otherwise; nothing that
  needs Google Play services, an account or the network (`AGENTS.md` §1); no background scanning (`AGENTS.md` §7); every write goes through Safe Mode
  (ADR-042).
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13.6): every decoded byte with its command and raw bytes. A claim without evidence is marked
  ⚪ ASSUMPTION or 🔴 OPEN QUESTION, never stated as fact.
- **Subagents** may only read and report; every write is done in the main session and verified.
- **Commits** only after the maintainer confirms the final summary (task 10).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0051_FEATURE_PROMPT_2026_09_26.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0051_FEATURE_PROMPT_2026_09_26
