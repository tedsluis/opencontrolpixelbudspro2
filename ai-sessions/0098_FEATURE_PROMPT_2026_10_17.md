# 0098_FEATURE_PROMPT_2026_10_17.md — Round 2 (release 1.4.0): noise control through setting 13 on the app's own channel, the ring status and its ACK, Ring both, the L/R battery from runtime info, the open app items, a Dosimeter experiment (not a feature), and the skeleton of its hardware run

**Number:** 0098
**Category:** FEATURE
**Date:** 2026-10-17
**Title:** Build **OpenControl 1.4.0** from what round 1 (`ai-sessions/0096` → `CAP-078` → `ai-sessions/0097`) proved on the wire: switch the noise-control mode
with `WriteSetting 4:{13:n}` on the app's own MAESTRO channel (DLCI 0x02) instead of the contested Message Stream `Set`; read the left/right battery from the
runtime-info stream so that a Connect needs no Message Stream claim; listen for and acknowledge the Buds' ringing status while the app holds the Message Stream;
offer **Ring both** (a decision superseding ADR-027 for "both" first); and the open app items the maintainer chose (a mode line on the Connection tab, a shorter
attempt-2 delay, the A68 concurrency fixes, `:app` tests, P1/P2 of `CAP-077`). Every design change starts as an ADR the maintainer accepts in this chat. **If the
maintainer chooses so at Checkpoint 1, the Dosimeter rides along as an experiment only** — four new sends on the experiment build's card (`SetWallclock`,
`Dosimeter.SubscribeToLiveDb`, `Dosimeter.FetchDailySummaries`, field 21 on channel 19), each with its own approval, nothing of it in the release — so that the
same hardware run gathers the evidence draft ADR-060 needs. Then the tests, the capture skeleton of the hardware run (Group BO) — including the two 1.3.1
steps no run has exercised and, if chosen, the Dosimeter part with a sound-level meter on film — and the release preparation (no tag, no publication).

---

## 0. How to use this prompt

You are an expert Android/Kotlin engineer, protocol reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this
repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9).** Take the time to read the core files first, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md` (scope, non-goals — note the Ring-both non-goal tied to ADR-027 — and the Definition of done), `ARCHITECTURE.md`
(§2.4, §3.1 the settings stream and the cause rule, §3.2 the timing constants, §5a with the experiment-build note and its `CAP-078` run note, §6.0b one RFCOMM
connection per channel, §7, §8.1, §9, §12, §13), `PROTOCOL.md` (§2.2a, §2.3; **§4.1 with every Update — in particular the Update of 2026-10-17**; §4.3 and the
runtime-info entry of §4.5 with its per-bud 🟢 FACT of 2026-09-25 and its Updates; **§4.4 with the Update of 2026-10-17**; §6 — the notes and items of
2026-10-17; §8), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-066 with every dated Update; the draft ADR-060 is not a decision) — read with particular care the
ADRs this round may supersede or amend: **ADR-009** (ANC over the Message Stream), **ADR-011** and **ADR-027** (Find My Buds; Case/"both" out of scope),
**ADR-031/033** (L/R battery from the Message Stream), **ADR-043** (Case from runtime info), **ADR-032** (the on-demand claim), **ADR-044** (the re-open),
**ADR-049** (Settable `00`), **ADR-057**, **ADR-058**, **ADR-064/065** (the settings stream, the cause rule), ADR-066 — and `TODO.md` (§1, §2, §4, §5, §6).

Then, per task, before using or changing anything:
- **Round 1, in full:** `ai-sessions/0096_FEATURE_PROMPT_2026_10_16.md` and its RESULT; `ai-sessions/0097_CAPTURE_PROMPT_2026_10_17.md` and its RESULT (Checkpoint 0
  and D answers, verbatim); `captures/CAP-078-2026-10-17_16-23-51_17-17-41-Group_BN/CAP-078-FINDINGS.md` (every section; §3 A, §4 B, §5 C, §8 the kept cause and
  BN-3, §10 sessions, §12 unexpected, §14 proposals P-A … P-E) and its EVENT-NOTES (timeline, step mapping, Test-IDs, and the skeleton in the appendix as the model
  for this round's skeleton).
- **The open app items:** `TODO.md` §5 — the items "After `CAP-076`" (P5 the shorter attempt-2 latency, P6 a mode line on the Connection tab), "After `CAP-077`"
  (P1 log the EQ write, P2 the tile's "unavailable" flash), `A68-APP-13`, `A68-APP-10`, "Instrumented tests"; `CAP-076-FINDINGS.md` §4.2, §5.2, §9 and
  `CAP-077-FINDINGS.md` §5.3, §14 for their evidence; `ai-sessions/0068`'s A68-APP-10/13 text.
- **Every Kotlin source a change touches, in full, before changing it** — at least: `BudsRepositoryImpl.kt` (`setAncMode`, `ancSetOnClaim`, `sendAndAwaitOnClaim`,
  `withMessageStream`, `refreshAncMode`, `causeOfReadAnswer`/`causeOfNotify`/`causeOfStream`, `applyAncState`, the Connect snapshot and `openSession`,
  `launchInitialEqRead`, the battery paths, `ringBud`/`stopRinging`/`sendRing`, `disconnect`, `reclassifyLoss`, every log string), `BudsRepository.kt`,
  `codec/CodecRouter.kt`, the MAESTRO and Message Stream codecs (`Maestro.kt`, `PwRpc.kt`, the settings codec, the ring and battery frames), `RuntimeInfo*`,
  `SessionReopener.kt`, `hardware/…/RfcommBudsTransport.kt` (`openChannel`, `closeAll`, the attempt delays), `AncTileService.kt`, `BudsForegroundService.kt`,
  `ui/…` (`ConnectionScreen`/`AncScreen`/`FindScreen`/`SettingsUi`/`DebugScreen`), `domain/AncMode.kt`, `app/…/MainActivity.kt`, `app/…/di/*`; the experiment
  sources (`android/data/src/experiment/…/ExperimentRunner.kt`, `ExperimentFrames.kt`, the card in `android/app/src/experiment/`) as the tested byte builders; the
  tests of each (`BudsRepositoryImplTest`, the codec tests, `FakeBudsTransport`, `ExperimentFramesTest`).
- **Procedure and release:** `AI_SESSION_LOG_PROCEDURE.md` (in full), `ai-sessions/INDEX.md`, `id_registry.csv`, `RELEASING.md` (in full: the checklist A1–D4,
  "Repository rules", §5, §6, §11a, §13 — this session prepares, it does not release), `CHANGELOG.md` `[1.3.1]`, `APP_TESTPLAN.md` §Z/§AA,
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 and the Group BM/BN blocks, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the rows named in §3 item 9).

Say in the RESULT which files you read in full and which only in part (a section read before it is used is enough for the 1.1 MB of `ARCHITECTURE.md`,
`PROTOCOL.md` and `DECISIONS.md` together; say which sections).

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
ai-sessions/0098_FEATURE_RESULT_2026_10_17.md at the very start with `**Status:** partial — resumed` and a **Progress** block; update it after every phase and every
substantial step (each ADR answered, each feature built and tested, each mutation check, the gate): what is done, what is next, the files touched but not yet
verified, where scratch results live. A resumed session reads this prompt, then the Progress block, re-reads the files the unfinished step touched, and
continues — it never redoes a recorded step and never assumes an unrecorded one was done.

**Memory and the machine.** 15 GB RAM, `/tmp` a 7.7 GB tmpfs. Gradle: `--max-workers=2` for a single module, `--max-workers=1` for the full clean gate (OOM
risk); mutation checks one at a time; after a crash look for leftover mutations (`git diff`). One `tshark` at a time. Never `git worktree add` without a sparse
checkout that excludes `captures/`. Scratch files in a fresh `mktemp -d` under /tmp/claude-1000/, named in documents without backticks (the docs lint flags
backticked names that do not exist).

**Nothing is taken on trust**, including this prompt, the 0097 RESULT and the memory notes: every byte sequence, frame number and timing in §2 is re-derived from
the captures, the code or the cited document before it is used.

---

## 1. What the maintainer asked for (chat, 2026-10-16/17, translated from Dutch)

1. 2026-10-16, the goals by priority (re-read in the summary of `ai-sessions/0096`'s planning): A — ANC via `WriteSetting 4:{13:n}` (E-01); B — the ringing status and
   its ACK (`FIND-005`, E-04); C — Ring both `0x03` (`FIND-004`); D — `SubscribeToOobeActions` (E-12); E — settings 8/9/10 (E-03); F — Dosimeter (draft ADR-060, blocked);
   app items 2.1 a mode line on the Connection tab, 2.2 a shorter attempt-2 delay, 2.3 "unchanged" logging (done in 1.3.1), 2.4 the A68-APP-10/13 concurrency
   fixes, 2.5 `:app` tests, 2.6 `targetSdk` (a separate release), 2.7 `strings.xml` + THIRD_PARTY_NOTICES (optional); "combine what can be combined in one update".
   Round 1 = an experiment build and `CAP-078`; round 2 = the release built on its results.
2. 2026-10-17, `ai-sessions/0097` Checkpoint D (verbatim): "A → PROTOCOL" → *"🟢 FACT-notitie (Recommended)"*; "B/C → §4.4" → *"🟢 FACT B + C (Recommended)"*;
   "D/E → §6" → *"🟢 wire-notities (Recommended)"*; "ADR-027" → *"Besluit in ronde 2 (Recommended)"* ("Round 2 FEATURE prompt item: 'Ring both' button (04 01
   00 01 03) on the Find tab, superseding ADR-027 for both (Case stays out of scope) — draft ADR in the prompt, accepted in chat first"); "Ronde 2" → *"Ja,
   prompt 0098 (Recommended)"* ("noise control through WriteSetting 4:{13:n} on the app's own channel, the ring status and its ACK, Ring both (superseding
   ADR-027 for 'both'), the L/R battery from runtime info; TODO 2.1/2.2/2.4/2.5, P1/P2; [the next capture's] skeleton (incl. TalkBack and a hold during Connect)"); "PR #68" →
   *"Draft tot ronde 2 (Recommended)"*.
3. 2026-10-17 (chat): "Write the prompt for round 2, ai-sessions/0098_FEATURE_PROMPT (release 1.4.0)." Then: "What is needed to take the Dosimeter into this
   feature (SetWallclock is also a new write with its own approval)?" — and, after the answer that it cannot be a feature yet: "Adapt the 0098 prompt so that
   the Dosimeter comes in as an experiment, not as a feature, and Checkpoint 1 asks the four approvals. The other option is to keep the Dosimeter out of this
   round and do a separate experiment round for it later." (Checkpoint 1 offers both.)
4. Standing practice: options with pros and cons and one "(Recommended)" per question, one question per decision; no FACT promotion and no ADR without the
   maintainer's answer in this chat (`AGENTS.md` §6); no commit or push without the maintainer's "yes" in chat; `main` takes no direct push (PRs); no release step
   unless asked.

---

## 2. What round 1 established (re-derive each before relying on it)

- **A (`PROTOCOL.md` §4.1, Update 2026-10-17, 🟢):** `WriteSetting 4:{13:n}` on DLCI 0x02 (channel 19 or 21) switched the mode with no Message Stream claim —
  `CAP-078` 22 sends: 18 `RESPONSE` status 0, each followed within 0.15 s by the settings-stream push `13 = n` (H 5921 → 5923/5924 …); 4 `RESPONSE` status 9
  (`FAILED_PRECONDITION`), no push, all while no bud was worn (push `13 = 0` at H 6756; H 6775 → 6777); in-ear detection off did not prevent it (H 7277 … 7301).
  The client holding DLCI 0x04 receives the Buds' `Notify` for each write (H3 17:12:57.512 … 17:13:28.437). ❓ Audibility (speech not transcribed).
  The 1.3.1 cause rule classed the pushes after the experiment's own writes as "changed_by_buds" (E2 16:46:22.989) — the app knows only its Message Stream `Set`s
  as its own.
- **The L/R battery (`PROTOCOL.md` §4.5 runtime-info entry, 🟢 FACT 2026-09-25):** entry 6.2 = Left, 6.3 = Right, field 1 = %; field 2 / 7.x the charging flags;
  6.1 the Case only while a bud charges (ADR-043). A bud in the closed case drops its entry (`CAP-065` 5874). Today the app takes L/R from the Message Stream
  "Battery updated" `03 03` (ADR-031/033) — which is the only reason a Connect claims DLCI 0x04 (the snapshot also brings the ANC `Notify`, made redundant for the
  mode by ADR-064's `ReadSetting 4:13`). Check what else the snapshot feeds (the Settable byte for "probably worn", ADR-059; firmware/serials come from DLCI 0x02).
- **B (`PROTOCOL.md` §4.4, Update 2026-10-17, 🟢 `FIND-005`):** after each Ring the Buds send their status `04 01 00 01 <value>` and at the stop `04 01 00 01 00`,
  once each, to the Message Stream holder; an unACKed status is not repeated; the phone's ACK is `ff 01 00 02 04 01` (the official app's `CAP-025` 2048). In the Play
  user Play services re-claims 5.24–5.45 s after losing the channel, the app's held claim is closed 4.45–4.86 s after it opened, and Play receives and ACKs the
  stop (`CAP-078-FINDINGS.md` §4.2).
- **C (`PROTOCOL.md` §4.4, 🟢):** `04 01 00 01 03` is ACKed and reported as `03` (H 8203 → 8210/8211; H3 7792 → 7796/7797); a second `03` while ringing got the ACK
  only; 🟡 both buds sounding. ADR-027 (and `PROJECT.md`'s non-goal) still exclude "both" — superseding it is this round's first decision.
- **D and E (`PROTOCOL.md` §6, 🟢 wire):** `SubscribeToOobeActions` answers `10`/`12` at subscribe and ends with status 14 after 15.7 s — not usable as an in-ear
  source; settings 8/9/10 answer status 2 with value 0. **Neither is built in this round** (propose nothing beyond a `TODO.md` line).
- **F — the Dosimeter (draft ADR-060, `PROTOCOL.md` §2.2a and §4.5.8a, `CAP-027`, `CAP-058-FINDINGS.md` §6, `REVERSE_ENGINEERING.md` of 2026-10-09):** the official
  app sends `Dosimeter.SubscribeToLiveDb` (`CAP-027` 1107/1214, both buds' channels; ≈ 2 stream packets/s per channel, 208 in 4 min) and
  `Dosimeter.FetchDailySummaries` (`CAP-058` 5736); the "Volume level notifications" switch writes field 21 (`CAP-058` 5747/5753, channel 21 only); the official
  app sends `SetWallclock` with the phone's clock in ms at every Connect (`CAP-041` 783 `1:1788707520935`) — **OpenControl has never sent any of these**
  (it only names `SetWallclock` when decoding, `Maestro.kt`). ADR-060's seven 🟡 — (i) `qir` field 2 a sound level (`WELL-002`, a meter on film), (ii) `qia` field
  6 a daily dose in the same unit, (iii) the day index's epoch, (iv) the Buds answer a second client, (v) field 21's channel-19 write, (vi) the cost, (vii) the
  clock set by `SetWallclock` first — block any feature; only a run can settle them.
- **1.3.1 behaviour on hardware (`CAP-078` §8–§10):** A93-APP-02 verified; A93-APP-04 (a push during a read) and A93-APP-07 (TalkBack) never exercised; 12 session
  losses, each the Buds' own `DISC`, re-opened 1.509–1.515 s later (chain guard twice).

---

## 3. Leads, scope and constraints — verify each before relying on it

1. **ANC through setting 13 (A).** The tab buttons and the Quick Settings tile switch the mode with `WriteSetting 4:{13:n}` on the open MAESTRO session's channel
   (ADR-034's address); success = the `RESPONSE` status 0 (and/or the push `13 = n` — decide which ends the wait; never a push alone); `FAILED_PRECONDITION` =
   "The Buds don't allow changing noise control right now (no bud worn)" — the existing Settable-`00` text; a timeout = the existing failure text. The app's own
   write within a window counts as `set_by_app` (a new or renamed window — `ANC_SET_WINDOW_MS`), its pushes "(unchanged)". Decide at Checkpoint 1 whether the
   Message Stream `Set` stays as a fallback (pros: older firmware; cons: the Play contention stays reachable) — the evidence says the Settable rule is the same.
   The tile works only while a session is Ready (today it claims DLCI 0x04 on demand; with A it needs DLCI 0x02 open — say what the tile does when not Ready).
   Writes of settings **3 and 6 never**; field 13 only with n = 1 … 4 and only in the list in force (field 12's mode list — check that a write outside it is
   refused by the app, and what the Buds answer is unknown: do not send it).
2. **L/R battery from runtime info.** Show L/R % from 6.2/6.3 field 1 (and the charging flags as today); keep the Message Stream `03 03` as a second source only
   if Checkpoint 1 says so; drop the Connect snapshot claim on DLCI 0x04 if nothing else needs it (the Settable byte — replace by `13 = 0`/"no mode settable"
   from the stream and the 4:13 read, ADR-059's "probably worn" rule rewritten in the ADR). Refresh battery re-subscribes runtime info on DLCI 0x02 (a second
   subscription is answered, `PROTOCOL.md` §4.5 Update 2026-09-28). With A and this, **the app claims DLCI 0x04 only for Find** — say it in the ADR and in
   `ARCHITECTURE.md` §6.0b/§12; ADR-032's on-demand claim remains for Find.
3. **The ring status (B).** While the app holds the Ring's claim it shows "Ringing (left/right/both)" from the Buds' status and "Stopped" from `…00`, ACKs each
   status with `ff 01 00 02 04 01` (the specification's rule; Checkpoint 1: ACK yes/no), and keeps the claim for at most the ring's duration (a constant — the
   observed rings stopped after 14.8–17.1 s; the Device Action page's optional timeout byte is not sent unless decided). In the Play user the claim is cut
   ≈ 4.5 s in (ADR-061/062's history: no re-claim) — the UI must not claim "stopped" it did not hear. The Stop button stays.
4. **Ring both (C).** A "Ring both" action on the Find tab (`04 01 00 01 03`), after the ADR superseding ADR-027 for "both" (the Case stays out of scope) and the
   matching `PROJECT.md` non-goal note (dated, `PROJECT_RULES.md` rule 9). The "take them out of your ears first" warning as today's Ring.
5. **App items:** P6 (2.1) a mode line on the Connection tab ("Noise control: …", with its cause); P5 (2.2) the on-demand channel's attempt-2 delay (`CAP-076` §5.2:
   attempt 2 succeeded 0.87–1.11 s after attempt 1's refusal — measure the constant in `RfcommBudsTransport` and propose the value; with A the on-demand channel
   is used only by Find, so weigh whether 2.2 still matters); 2.4 the A68-APP-13 items (`disconnect()` outside the connect mutex, `reclassifyLoss` without a
   common lock, the call-id quarantine on cancellation, a profile proxy bound after `closeAll`) and A68-APP-10 (the non-atomic insert in `openChannel`) — each
   with a test that forces the interleaving (the stub-`BluetoothDevice` helper of the memory note "Stub BluetoothDevice in :data tests"); 2.5 `:app` tests
   (Robolectric where a JVM test can cover `MainActivity`'s state collection, the tile's state mapping, the DI graph); P1 of `CAP-077` log the EQ write ("EQ
   written (channel N)"); P2 the tile's one- or two-frame "unavailable" flash right after an update (`AncTileService`). **Not in this round:** 2.6 `targetSdk`
   (its own release, the maintainer's choice of 2026-10-15), 2.7 `strings.xml`/THIRD_PARTY_NOTICES (optional — ask at Checkpoint 1 only if time allows), D, E, F.
6. **The experiment build.** Decide at Checkpoint 1: keep the `experiment` build type as a permanent tool for later rounds (its card emptied or kept) or remove it
   — **if F is chosen it stays** (F lives on its card);
   either way the release APK holds no experiment class (the proof of 0096: `unzip -l` / `dexdump`), and the 0096 "(experiment seam)" visibility is reviewed.
6a. **F — the Dosimeter experiment (only if chosen at Checkpoint 1; never in the release).** A section "F — Dosimeter" on the experiment card, one tap = one
   request after a confirmation naming the bytes, each send approved separately at Checkpoint 1: **F1 `SetWallclock`** (the phone's clock in ms, the request
   form of `CAP-041` 783 byte for byte apart from the time; sent once per tap, never automatically); **F2 `SubscribeToLiveDb`** (the bytes of `CAP-027`
   1107/1214 re-addressed to the session's channel; channel 19 and 21 as two buttons — the official app uses both); **F3 `FetchDailySummaries`** (`CAP-058` 5736);
   **F4 field 21 on channel 19** (`WriteSetting 4:{21:b}` — the channel-21 form of `CAP-058` 5747/5753 re-addressed; the channel-19 form was never captured, so
   this send is the riskiest — Checkpoint 1 may decline it). The card logs every answer and stream packet decoded as far as the APK notes allow (`qir`/`qia`
   field numbers and raw values, no unit, no health wording), with a counter of live packets per minute (for the cost, 🟡 vi); the experiment's decoder lives in
   `src/experiment` only; `PwRpc`'s method table may learn the names (a decode-only change). Tests: the four requests built from real bytes (`CAP-027`, `CAP-041`,
   `CAP-058` frames with their commands), the decoder against `CAP-027`'s live packets and `CAP-058`'s daily answer, the release APK without any of it.
7. **Tests — real bytes first (`AGENTS.md` §11):** fixtures from `CAP-078` (A: H 5921/5923/5924, H 6775/6777, H3 4891 …; runtime info: a `SubscribeRuntimeInfo`
   push of `CAP-078` with 6.2/6.3 and one without the Case; ring: H 7772–7817, 7945–8016, 8203–8211; the Play-user cut H3 6605–6692), each with frame and command
   in a comment; hand-built arrays only as labelled supplements; the fuzz test of the decoders extended to any new frame; mutation checks for every new rule
   (M-n: break it, see a test fail, restore — one at a time).
8. **Version and release preparation:** 1.4.0 (versionCode 10400 — check the scheme in `android/app/build.gradle.kts`); `CHANGELOG.md` `[1.4.0]` (Added /
   Changed / Fixed / Known issues — measured, not hoped); `RELEASING.md` A1–A6 as far as they apply before a hardware run; **no** `scripts/release.sh`, no tag, no
   `dist/`, no publication.
9. **The capture skeleton (Group BO; the next free `CAP-NNN` from `id_registry.csv`):** in `CAP-078`'s layout (P0–P7, reference frames from the app's codecs, BO-n
   steps with "expected on screen / on the wire / Refuted if", a run card), in **both users**: the tab and tile switches through setting 13 (with both buds,
   one bud, none worn; in-ear detection off), the battery without any DLCI 0x04 traffic at Connect, the ring with its status line and Stop, Ring both, the mode
   line, and **the two 1.3.1 steps no run has exercised**: TalkBack on the switch rows / Bass slider / tabs (A93-APP-07) and a press-and-hold within ≈ 1 s of a
   Connect (A93-APP-04 — "tap Connect, hold a bud at once; repeat until the mode changes"). **If F is chosen, a Dosimeter part** (experiment app, user 10 first,
   then user 0 for 🟡 iv with Play present): F1 `SetWallclock` once and the phone's clock on film; F2 on both channels; a **sound-level meter** (a stand-alone
   meter preferred) in frame next to the buds, its display readable; three to four fixed levels of one minute each (silence; speech; music at two volumes)
   with the buds worn; F3 before and after; F4 only if approved; the live-packet rate noted. Test-IDs: `WELL-002` (and a new one for the clock/epoch if none
   fits). New Test-IDs only if no existing one fits (register them).

---

## 4. Tasks

### Phase 0 — set-up and Checkpoint 1 (the decisions; ask before building anything)

1. Create the RESULT (§0). Record `git log -1`, `git status --short`, `git fetch`, `git log --oneline main..origin/feature/0096-experiment-build`, the state of
   draft PR #68. `main` has 1.3.1 and ADR-066; the 0096/0097 work (the experiment build, `CAP-078`, the PROTOCOL Updates of 2026-10-17) is on
   `feature/0096-experiment-build` only.
2. **Checkpoint 1 (`AskUserQuestion`, one question per decision, options with pros and cons, one "(Recommended)", the draft text or the code sketch in the
   preview):**
   - **The branch:** a new branch `feature/0098-round-2` from `feature/0096-experiment-build` (carries the docs of 0096/0097; PR #68 closed when round 2's PR opens)
     or from `main` with the 0096/0097 commits cherry-picked; never on `main`.
   - **ADR "A" — noise control through setting 13** (superseding ADR-009's channel for the `Set`; the `Set` as fallback or not; what ends the wait; the
     `set_by_app` window; `FAILED_PRECONDITION` handling; the tile when not Ready): the full ADR text in the preview, headed "(DRAFT — proposed, not accepted)"
     until answered.
   - **ADR "B" — the L/R battery from runtime info and no Connect snapshot** (superseding ADR-031/033's source; ADR-059's "probably worn" input; what
     Refresh does; whether `03 03` stays as a second source).
   - **ADR "C" — the ring status and its ACK** (ACK yes/no; the claim's maximum hold; what the UI says in the Play user).
   - **ADR "D" — Ring both** (superseding ADR-027 for "both"; the Case stays out) and the dated `PROJECT.md` non-goal note.
   - **The Dosimeter (F):** "in this round as an experiment only (Recommended)" or "out of this round — a separate experiment round later"; if in, **four
     separate approvals, one question each, the exact bytes in the preview:** F1 `SetWallclock` (what it changes on the Buds; the official app sends it at every
     Connect; the app would send it only on a tap), F2 `SubscribeToLiveDb` (the stream's cost for the session), F3 `FetchDailySummaries` (read-only), F4 field 21
     on channel 19 (never captured in that form — the riskiest; "decline" is a valid answer). A declined send is not built.
   - **The experiment build** (keep as a tool / remove — fixed to "keep" if F is in); **2.7** (now / later); the scope check: anything in §3 to drop for this
     round.
   Record every answer verbatim. Write each accepted ADR to `DECISIONS.md` (and `id_registry.csv`) **only after** its answer; a declined one is recorded in the
   RESULT and not written. Numbers: take the next free ADR numbers from `id_registry.csv` at that moment (the letters A–D are placeholders; the numbers follow ADR-066 in acceptance order).

### Phase 1 — build (one feature at a time; each with its tests before the next)

3. A (setting 13) → the battery from runtime info and the dropped snapshot → B (the ring status) → C (Ring both) → P6 the mode line → P1/P2 → 2.2 → 2.4 → 2.5 →
   the experiment build as decided → F on the experiment card (if chosen) → 1.4.0's version. After each: the module's tests (`--max-workers=2`), a mutation check per new rule, a `Progress` line.
4. Keep every log string meaningful for the next CAPTURE session (each export line must be matchable to its code; write the new strings into the RESULT).

### Phase 2 — the gate and the proofs

5. The full clean gate (`./gradlew clean test lint assembleRelease … --max-workers=1`, the project's usual task list — read `RELEASING.md` A-steps and
   `.github/workflows/android.yml`), the release APK without experiment classes, `./gradlew app:dependencies` unchanged (no new dependency without §10's
   justification), no `INTERNET` permission, the manifest's permission set unchanged. Record the counts (tests per module, failures 0) and the mutation table.

### Phase 3 — the skeleton, the documents, the release preparation

6. The capture skeleton (§3 item 9) with its run card; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group BO block and §9 row; `id_registry.csv` (the capture, any new
   Test-ID, the ADRs); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (BO in the Group column of the Test-IDs it plans); `APP_TESTPLAN.md` a new section for 1.4.0.
7. `ARCHITECTURE.md` (§3.1 the ANC path and the cause rule, §3.2 the constants, §5a the feature table, §6.0b/§12 the Message Stream used only for Find),
   `PROTOCOL.md` only where an implementation note is due (no status change without the maintainer), `PROJECT.md` (the feature list; the Ring-both non-goal
   note as accepted), `README.md` where a feature list or a known issue changes, `CHANGELOG.md` `[1.4.0]`, `TODO.md` (done items removed; D/E/F and the deferred
   items kept).
8. `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` (exit 0).
9. The RESULT: a plain-language summary first (what 1.4.0 changes for the user, in both users; what the hardware run must show), the phases, the files read in
   full / in part, the ADRs with their answers, the log strings, the mutation table, the gate, "Deferred documentation" (each also in `TODO.md`), "Commits";
   the `ai-sessions/INDEX.md` row (0098) and the 0097 row's back-fill if needed.
10. **Checkpoint 2:** a short summary, then ask whether to commit and push (commits per concern, Conventional Commits with the *why* and the attribution line;
    `git fetch`; push without force), whether to open the round-2 pull request against `main` (draft, "do not merge before the hardware run is analysed") and
    whether to close draft PR #68 with a pointer to it. Never merge; the release steps are the maintainer's after the hardware run.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **ADRs and FACTs only with the maintainer's answer in this chat** (`AGENTS.md` §6, `PROJECT_RULES.md` rule 9): every superseding decision names the ADR it
  supersedes; no status promotion in `PROTOCOL.md`.
- **Wire safety:** only requests already on a wire in `CAP-078` (or earlier) with the bytes the app's codecs build and the tests pin; settings **3 and 6 never**
  written; field 13 only 1 … 4; nothing written in reaction to a stream packet; no Dosimeter, no `SetWallclock`, no `SubscribeToOobeActions` in the release —
  F's four requests only from the experiment card, only the approved ones, never automatically; no health claim or unit in any F text.
- **No release step:** no tag, no `scripts/release.sh`, no `dist/`, no publication, no merge.
- **Zero-GMS, permissions, privacy:** no new permission, no network, no telemetry; no serial, MAC, Wi-Fi name or account in committed text.
- **Evidence:** every byte sequence with its source frame and command (rule 4a); a negative with its command, exit status and a positive control; fixtures are
  real bytes (`AGENTS.md` §11).
- **Files:** no wildcard deletes; diff before moving; `android/domain/bin/` never staged.
- **Subagents** only read and report; every write is done and verified in the main session.

## 6. Deliverables

- OpenControl 1.4.0 (versionCode 10400 as checked): ANC through setting 13 (tab and tile), the L/R battery from runtime info with no Connect claim on the Message
  Stream, the ring status (and its ACK as decided), Ring both, the mode line, P1/P2, the attempt-2 delay as decided, the A68 fixes, `:app` tests; the experiment
  build as decided; tests with real fixtures, the mutation table, the clean gate, the release-APK proof.
- The accepted ADRs (and nothing written for a declined one); the capture skeleton (Group BO) with the run card, incl. TalkBack and the hold during a Connect
  and, if chosen, the Dosimeter part with the sound-level meter.
- If F is chosen: the experiment card's "F — Dosimeter" section with its approved requests, tests and decoder (experiment source set only); the experiment APK's
  path and SHA-256 in the RESULT (not committed); draft ADR-060 unchanged (it is accepted or rewritten only after the run is analysed).
- The document, registry, test-plan, `CHANGELOG.md` `[1.4.0]` and `TODO.md` updates; ai-sessions/0098_FEATURE_RESULT_2026_10_17.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0098_FEATURE_PROMPT_2026_10_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0098_FEATURE_PROMPT_2026_10_17
