# 0096_FEATURE_PROMPT_2026_10_16.md — Round 1: an experiment build for five untested requests (ANC via setting 13, the ring status and its ACK, Ring both, on/off-head events, settings 8/9/10) and the skeleton of its hardware run `CAP-078` (Group BN)

**Number:** 0096
**Category:** FEATURE
**Date:** 2026-10-16
**Title:** Build a **debug-signed experiment build** of 1.3.1 — never released — with one Debug-tab card that sends each of five requests no capture has ever
held, one tap = one request, and write the capture skeleton `CAP-078` (Group BN) that tests them on film in both GrapheneOS users. Round 1 of the plan
agreed in chat on 2026-10-16 (after `ai-sessions/0095`): experiments first, then the maintainer's FACT promotions and ADRs, then one release (round 2,
not this session).

---

## 0. How to use this prompt

You are an expert Android/Kotlin engineer, protocol reverse engineer and technical auditor. This prompt is for a **fresh** Claude Code session in this
repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8/§9) — in full, in this order:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`,
`ARCHITECTURE.md` (in full: the experiment card touches §2.4, §3.1, §3.2, §5a, §6.0b, §8.1, §12, §13), `PROTOCOL.md` (§2.2a, §4.1 with every Update/Note/
Correction, §4.4 Find My Buds with its Updates, §4.5 head and §4.5.3, the §6 items named in §2 below), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-066, with
every Update; draft ADR-060 is not a decision), `TODO.md` (§1, §2, §4, §5, §6). Then, per task:

- **The experiments' sources:** `ai-sessions/0090_CROSSCHECK_RESULT_2026_10_14.md` §E-01, §E-03, §E-04, §E-05, §E-12 and its verdict table; `REVERSE_ENGINEERING.md`
  (the `qhr` register incl. its Update of 2026-10-14; the `maestro_pw.Maestro` method table with `SubscribeToOobeActions`; `qje`/`qhp`); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  rows `FIND-004`, `FIND-005`, `TOUCH-007`, `INEAR-001`…`INEAR-006`; `DESKRESEARCH_FINDINGS.md` 2026-10-14 (§9 wear-mode cross-check).
- **The last run:** `captures/CAP-077-2026-10-16_06-56-14_07-10-38-Group_BM/CAP-077-FINDINGS.md` in full (its §5, §9, §14) and its EVENT-NOTES (the layout and the
  method), `ai-sessions/0095_CAPTURE_RESULT_2026_10_16.md`.
- **Every Kotlin source the card touches, in full, before changing it:** `data/…/codec/Maestro.kt`, `PwRpc.kt`, `SettingFrame.kt`/`SettingsCodec`, `CodecRouter.kt`,
  `RingFrame*.kt`, `BudsRepositoryImpl.kt` (`withMessageStream`, `ringBud`, `stopRinging`, `sendRing`, `sendAndAwait`, the Maestro reply flows, `SafeModeGate` use,
  the log strings), `BudsRepository.kt`, `hardware/…/RfcommBudsTransport.kt`, `ui/…/DebugScreen.kt`, `SettingsMenu.kt`, `app/…/MainActivity.kt`,
  `app/…/di/RepositoryModule.kt`, `android/app/build.gradle.kts` and the module build files, `scripts/release.sh`, `.github/workflows/android.yml`.
- **Procedure:** `AI_SESSION_LOG_PROCEDURE.md` (in full), `ai-sessions/INDEX.md`, `id_registry.csv`, `RELEASING.md` (Repository rules, §5, §6, §11a — this session
  makes **no** release), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 and the Group BL/BM blocks, `APP_TESTPLAN.md` §Z.

Say in the RESULT which files you read in full and which only in part.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5):** create ai-sessions/0096_FEATURE_RESULT_2026_10_16.md at the very start with
`**Status:** partial — resumed` and a **Progress** block; update it after every phase and every substantial step (what is done, what is next, files touched but
not yet verified, where scratch results live). A resumed session reads this prompt, then the Progress block, and continues — it never redoes a recorded step.

**Memory and the machine.** 15 GB RAM: Gradle with `--max-workers=1` for a full gate, `--max-workers=2` for single modules; one `tshark` at a time. Scratch files in a
fresh `mktemp -d` under /tmp/claude-1000/, named in documents without backticks.

**Nothing is taken on trust**, including this prompt and the 0090 RESULT: every byte sequence and every claim in §2 is re-derived from the code, the captures, the APK
notes or the external source before it is used.

---

## 1. What the maintainer asked for (chat, 2026-10-16, translated from Dutch)

1. The maintainer listed next steps by goal and priority and asked whether they are valid, correctly phrased, feasible, conflict-free and can be done in one update.
   The answer of the 0095 continuation (accepted by the maintainer: "ja, begin met stap 0 en de prompt voor ronde 1"):
   - **Step 0** (done 2026-10-16, ADR-066, PR #67): capture files of at most 5 MB are ordinary git files; films, full logcats and system logs stay local with their
     hash (Git LFS refuses uploads).
   - **Round 1 (this prompt):** one experiment build plus one filmed capture for **A** `WriteSetting 4:{13:n}` (E-01), **B** the ring status and its ACK (E-04 /
     `FIND-005`), **C** Ring both `0x03` (E-05 / `FIND-004`), **D** `SubscribeToOobeActions` (E-12), **E** `ReadSetting 4:8`, `4:9`, `4:10` (E-03).
   - **Round 2 (later, not this session):** one release (1.4.0) with what passed plus app items (the mode line on the Connection tab, a shorter attempt-2 delay,
     the `A68-APP-10/13` concurrency fixes, `:app` tests, P1/P2 of `CAP-077`, optionally strings in `strings.xml`); the battery Left/Right from the runtime
     info (6.2/6.3 field 1, already 🟢) so the Connect needs no Message Stream claim; `targetSdk` in a release of its own; draft ADR-060 stays blocked.
2. Standing practice: questions in chat (`AskUserQuestion`), one per decision, with options, pros and cons, "(Recommended)" on one; no commit or push without the
   maintainer's "yes"; `main` takes no direct push; no release step.

---

## 2. The five experiments — what is known (re-derive each) and what the run must show

| | Request (all `maestro_pw.Maestro` on the announced channel with its ADR-034 address, unless DLCI 0x04) | Known | Never seen | The run must show |
|---|---|---|---|---|
| **A** (E-01) | `WriteSetting 4:{13:n}`, n = 1 Off, 2 Noise cancellation, 3 Transparency, 4 Adaptive (`qhs`; pbpctrl `cli/src/main.rs:158–175`, `maestro_pw.proto:96`, `:175–181`) | the read and the stream of 13 are 🟢 (`PROTOCOL.md` §4.1, 2026-10-14); 0 writes of 13 in 96 logs (`ai-sessions/0090` E-01) | any `WriteSetting 4:{13:n}` | the `RESPONSE` status; the stream packet(s); the Message Stream `Notify` (to whom: none, the app, or Play in the Owner user); the mode audible on film/said aloud; with **one bud worn**, with **Settable `00`** (both buds out of the ears / on the table), and with **in-ear detection off**; whether the per-wear-state stored mode (`PROTOCOL.md` §6, 🟡 strong) changes with it |
| **B** (E-04, `FIND-005`) | DLCI 0x04: the claim **held while ringing**; the Buds' `04 01 00 01 xx`; the phone's ACK `ff 01 00 02 04 01` (the official app's, `CAP-025` 2048) | every Buds `04 01 00 01 xx` in 96 logs is an echo < 1 s after the phone's own Ring/Stop (40 of 40, 0 unprompted); OpenControl never ACKs | a Buds-side message when the ring is **stopped by touching the bud** | B1: claim held, **no ACK**, Ring Left, stop by touching the bud — does `04 01 00 01 00` come? B2: the same **with** the ACK of each Buds status message (needs approval, §4 Checkpoint 1). With Play (Owner user): how long the held claim lives (`CAP-074`: Play's recovery ≈ 5.3 s) |
| **C** (E-05, `FIND-004`) | DLCI 0x04: `04 01 00 01 03` (Device Action spec: "0x03 … Ring both left and right"; pbpctrl `libgfps/examples/ring.rs:57–64`) | ADR-027's Update of 2026-09-24; one ACK per Ring command (`CAP-025`) | `0x03` | the ACK `ff 01 00 03 04 01 00` or a NAK; both buds audible on film; Stop (`…00`) stops both. **Buds out of the ears** (loud) |
| **D** (E-12) | `SubscribeToOobeActions`, empty request (`REVERSE_ENGINEERING.md`: server-stream, request `nia` empty, response `qje` field 1 = `qhp`) | `qhp` 10 `LEFT_ON_HEAD`, 11 `LEFT_OFF_HEAD`, 12 `RIGHT_ON_HEAD`, 13 `RIGHT_OFF_HEAD` (`qhp.java:8–24`) | the request on any wire | per-bud on/off-head events when buds go in/out of the ears **without** OOBE mode. **Settings 3 and 6 are never written** (`ARCHITECTURE.md` §5a rule) |
| **E** (E-03) | `ReadSetting 4:8`, `4:9`, `4:10` once each | pbpctrl names 9/10 `ALLEGRO_ANCR_STATE_ONE_BUD`/`_TWO_BUDS`; on the gen 1 "status 2"; the APK types 9/10 as `qhs` and never reads them | any read of 8, 9, 10 | the answer or the status; values before and after a wear change (one bud ↔ two buds) and after an A write |

Each byte sequence the card sends is built by the existing codecs and **checked against its source before the build** (rule 4a): A's request against the
`WriteSetting` frames of fields 2/11/29 on both channels (same envelope; field 13 is a plain enum); B's ACK and C's command byte-for-byte against `CAP-025`'s
frames (2048; the Ring frames); D's and E's against the `ReadSetting`/`SubscribeRuntimeInfo` frames and the method hash `PwRpc.nameHash("SubscribeToOobeActions")`
compared with `REVERSE_ENGINEERING.md`'s id. Write each expected frame (hex, both channels 19 and 21 where Maestro) into the skeleton's reference table.

---

## 3. Design constraints of the experiment build (binding)

1. **Never in a release.** The experiment code lives in a separate Gradle **product flavor** (or source set) `experiment`; the release build of `scripts/release.sh`
   must not contain it — prove it with a check on the release APK (e.g. no experiment class name in `classes*.dex`, `unzip -p … | grep -c`, with a positive control
   on the experiment APK). Debug-signed, `applicationIdSuffix ".experiment"`, versionName `1.3.1-exp.1` — it installs **beside** the release app (a different
   package, its own CDM association, its own data).
2. **One app at a time.** Android allows one RFCOMM connection per (device, channel) across all apps (`ARCHITECTURE.md` §6.0b): the skeleton tells the maintainer to
   force-stop the release app before opening the experiment app and the other way round, and to check in the HCI log that only one client opens DLCI 0x02.
3. **The card.** Only in the Debug tab, only while Debug mode is on and the session is `Ready`; a confirmation dialog per experiment naming the exact bytes;
   **one tap = one request**, no loop, no retry, no timer; the Safe-Mode gate (ADR-042) applies to A/B/C (writes/commands); every send and every answer logged
   always-on and payload-labelled ("Experiment A: WriteSetting 4:{13:2} sent (channel 21)", "… RESPONSE status OK", "Experiment B: Buds ring status 00 (…)").
   B holds the claim only between Ring and the Buds' "stopped" status or Stop, with a hard cap (e.g. 30 s) and release on leaving the app.
4. **Nothing else changes.** The release code paths, request order and the Connect sequence stay byte-identical to 1.3.1 (`CAP-077` §2); A–E never run by
   themselves.
5. **Tests.** Codec tests for every new frame (labelled structural tests where no real capture exists yet — `AGENTS.md` §11; the real-byte fixtures follow
   in round 2 from `CAP-078`); fuzz for any new decoder (D's `qje`, B's status); a test that the release variant has no experiment entry point. The full gate
   (`clean build`, `--max-workers=1`) and `scripts/lint_docs.py` exit 0.
6. **AGENTS.md:** Kotlin only, no new dependency, no permission, no network; AGPL header on new files; no hidden API; `BudsResult` errors.

---

## 4. Tasks

### Phase 0 — set-up and Checkpoint 1
1. Create the RESULT (§0). `git switch main && git pull --ff-only`; a branch `feature/0096-experiment-build`. Record `git log -1`, `git status --short`.
2. Re-derive §2's facts (the frames from the captures with `tshark`/`scripts/pwhdlc_crc_table.py`, the method hash, the APK notes, pbpctrl at its pinned commit
   via `gh api` — read only). Any fact that does not hold: stop and report it.
3. **Checkpoint 1 (`AskUserQuestion`, one question per decision, the exact bytes in the preview):** approval of **each new send separately** — A (the four
   writes; also with Settable `00`?), B1 (held claim), B2 (the ACK), C (`0x03`), D (the subscription), E (the three reads); the flavor design (§3.1); whether the
   Owner user (with Play) is part of the run. **Nothing that the maintainer declines is built.**

### Phase 1 — build
4. The flavor, the card and the codec additions as approved; tests first where a test can fail on the present code. Record every changed file.
5. The gate; the release-variant check (§3.1); build the experiment APK (`./gradlew :app:assembleExperimentDebug` or the flavor's task); its SHA-256 into the RESULT.

### Phase 2 — the skeleton `CAP-078` (Group BN)
6. `CAP-078` is registered `planned` in `id_registry.csv` (by the commit of this prompt — update its description); add the Group BN block and the index row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`; new Test-IDs for A, D and E in
   `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (check the registry for free numbers; `FIND-004`/`FIND-005` exist); `APP_TESTPLAN.md` §AA rows.
7. CAP-078-EVENT-NOTES.md (skeleton, the `CAP-077` layout): preparation (P0 users and Play, the experiment APK installed beside the release app, Debug mode on,
   HCI snoop on, **screen recording with sound — say every action aloud — plus the camera on the ears and the case**, Do Not Disturb, the films kept local
   per ADR-066), a reference-frame table (§2), and steps:
   - **Part 1 — user 10 (no Play), the release app 1.3.1:** the steps `CAP-077` did not run — BM-3 TalkBack, BM-4 two holds while disconnected, BM-6 a hold
     within ≈ 1 s of Connect, BM-7 Cancel and a rotation with the dialog open.
   - **Part 2 — user 10, the experiment app:** A (each mode; one bud worn; Settable `00`; in-ear detection off), E before/after a wear change and after an A write,
     D (each bud in/out of the ear, on camera), B1 (and B2 if approved), C (buds out of the ears).
   - **Part 3 — the Owner user with Play (if approved):** A once per mode (who gets the `Notify`), B1 (how long the held claim lives), C once.
   - Expected screen and wire per step; "Refuted if" per experiment; what each outcome means for round 2.
8. A one-page run card for the maintainer (the steps, what to say aloud, which file to pull when) in the skeleton's appendix.

### Phase 3 — documentation, checkpoint, finish
9. `ARCHITECTURE.md` §5a: one line "experiment build 1.3.1-exp.1 (not released)"; `TODO.md` §4/§5 (the experiments now have a build and a skeleton);
   `PROTOCOL.md`: **nothing** (no claim before the run). No ADR in this session (the ADRs follow the analysis of `CAP-078`).
10. **Checkpoint 2 (`AskUserQuestion`):** commit and push the branch (no merge until the run is analysed — or merge now? options with pros and cons); the APK's
    hand-over (path, hash).
11. The RESULT: plain-language summary first; files read; "Deferred documentation" (each in `TODO.md`); "Commits"; the `ai-sessions/INDEX.md` row.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **No release step**, no tag, no `dist/`, no `scripts/release.sh` run, no change to the release variant's behaviour.
- **Every new send needs the maintainer's approval in this chat** (Checkpoint 1); writes of settings **3 and 6 never**; `WriteSetting 4:{13:n}` only from the
  experiment card; nothing sent automatically.
- **No FACT promotion, no ADR** in this session (`AGENTS.md` §6) — the experiments create the evidence, the next CAPTURE session analyses it.
- **Evidence:** every byte sequence with its source frame and command (rule 4a); a negative with its command, exit status and a positive control.
- **Privacy:** no serial, MAC, Wi-Fi name or account in committed text; films local (ADR-066).
- **Files:** no wildcard deletes; diff before moving; `android/domain/bin/` is never staged.

## 6. Deliverables

- The experiment flavor, the Debug-tab card, its tests, the release-variant proof; the experiment APK (path and SHA-256 in the RESULT, not committed).
- The skeleton `CAP-078` (Group BN) with the run card; the registry, index, test-plan and `TODO.md` updates.
- ai-sessions/0096_FEATURE_RESULT_2026_10_16.md and the `ai-sessions/INDEX.md` row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0096_FEATURE_PROMPT_2026_10_16.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0096_FEATURE_PROMPT_2026_10_16
