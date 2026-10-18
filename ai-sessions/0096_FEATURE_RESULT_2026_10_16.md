# 0096_FEATURE_RESULT_2026_10_16.md — Round 1: an experiment build for five untested requests and the skeleton of its hardware run `CAP-078` (Group BN)

**Number:** 0096
**Category:** FEATURE
**Date:** 2026-10-16
**Title:** A debug-signed experiment build (never released) with a Debug-tab card for E-01, E-03, E-04, E-05, E-12, and the skeleton `CAP-078` (Group BN)
**Status:** complete (the experiment build and the skeleton are done; the run `CAP-078` is the maintainer's; committed and pushed, draft PR, not merged)

## Summary (plain language first)

**What was built.** A separate "experiment" build of 1.3.1 (`1.3.1-exp.1`, its own package so it installs beside the released app, debug-signed, never published)
with one extra card on gear → Debug. Each button there asks first, showing the exact bytes, then sends one request no capture has ever held: switch noise control
through setting 13 on the app's own channel (A), read settings 8/9/10 (E), subscribe to the on/off-head events (D), ring a bud and keep the shared channel until
the Buds report it was stopped by touch — without and with the phone's acknowledgement (B1/B2), and ring both buds (C). The maintainer approved each of these in
chat. The released app is unchanged: the experiment code exists only in that build type (checked in the release APK: 0 experiment classes); the repository class
only made twelve members `internal`, and the UI got an empty slot.

**What is ready for the run.** The APK (SHA-256 `4e5facfa…d422`) in ~/opencontrol-1.3.1-exp.1/, and the skeleton `CAP-078` (Group BN) with the expected bytes
for every step, three parts (the release app's missing 1.3.1 steps; the experiments in user 10; A, B1, C in the Owner user with Play) and a one-page run card.
**Next for the maintainer:** run `CAP-078`; then a CAPTURE session analyses it, and only then the FACT promotions and ADRs for round 2.

## Progress

- **Started** 2026-10-16 on `feature/0096-experiment-build` at `61a59b9` (the prompt commit, level with origin). Scratch: /tmp/claude-1000/s0096.didt.
- **Phase 0 step 2 done (facts re-derived):** `CAP-025-btsnoop_hci.log` (DLCI 0x04, `tshark -Y 'btrfcomm.len>0 && (btrfcomm.dlci==4||btrfcomm.dlci==5) && (frame contains
  04:01:00:01 || frame contains ff:01:00:02:04:01 || frame contains ff:01:00:03:04:01)'`, exit 0): Ring Right 2040 `04 01 00 01 01` → the Buds' ACK 2044 `ff 01 00 03 04 01 00`
  → the Buds' status 2045 `04 01 00 01 01` → the official app's ACK 2048 `ff 01 00 02 04 01`; Stop 2120 `…00` → 2123 / 2124 `04 01 00 01 00` / 2127; Ring Left 2131 `…02`.
  The Maestro frames are built by the project's envelope (pw_hdlc address `00 3b`/`00 4b`, control `03`, RpcPacket `10 <ch> 1d <service> 25 <method> 2a <len> <payload>`,
  CRC-32 little-endian, `7e`/`7d` escaping) — scratch frames.py; **controls:** it reproduces three real `CAP-077` frames byte for byte (`WriteSetting 4:{2:1}` ch 19
  H 2478, `ReadSetting 4:2` ch 21, `SubscribeRuntimeInfo` ch 19). `h65599("SubscribeToOobeActions")` = `0x57f3becb`, as `DESKRESEARCH_FINDINGS.md` (2026-10-14) and `ai-sessions/0068` record it (the name is the
  APK's literal, `REVERSE_ENGINEERING.md` service table). The derived frames are in frames.txt (scratch) and the Checkpoint-1 previews.
- **Checkpoint 1 answered** (chat 2026-10-16, verbatim in "Checkpoints").
- **Phase 1 (built):**
  - `android/app/build.gradle.kts`, `android/data/build.gradle.kts`: build type `experiment` (from debug; `:app` with `applicationIdSuffix ".experiment"`,
    `versionNameSuffix "-exp.1"`; `matchingFallbacks += "debug"` in both); `:app` debug and release compile `src/noExperiment` (an empty `ExperimentHook`).
  - `data/src/experiment/…/data/experiment/ExperimentRunner.kt`: `ExperimentFrames` (the bytes) and `ExperimentRunner` (A, E, D on DLCI 0x02 through the
    repository's `eqMutex`, channel, write gate and quarantine; B and C on the repository's Message Stream claim with its Safe-Mode gate; B holds the claim until
    the Buds' status `00`, 30 s or Stop; B2 ACKs each status). Every send and outcome logged always-on with the bytes.
  - `app/src/experiment/…/app/ExperimentHook.kt`: the card (Debug mode on and `Ready` only; a confirmation naming the bytes per tap).
  - `:ui`: a generic `extra`/`debugExtra` slot (`DebugScreen`, `SettingsMenuScreen`, `OpenControlNavHost`), empty by default; `MainActivity` passes `ExperimentHook`.
  - `BudsRepositoryImpl`: twelve members and `MaestroReply` from `private` to `internal` (marked "(experiment seam)"); no behaviour change.
  - Tests: `data/src/testExperiment/…/ExperimentFramesTest.kt` — 6 of 6 pass (`./gradlew :data:testExperimentUnitTest`): the envelope control against two
    `CAP-077` frames, the Ring frames and the ACK against `CAP-025` (real bytes), the A/E/D frames as labelled structural tests, the message splitter on real
    bytes and 20,000 random inputs.
  - **Release-variant proof:** `unzip -p <apk> 'classes*.dex' | grep -a -c <name>` — `ExperimentRunner` 29 / `ExperimentFrames` 4 / `RING_STATUS_ACK` 2 in the
    experiment APK, **0 / 0 / 0 in the release APK** (`:app:assembleRelease`, signed with the local keys; built for the check only, not a release).
    `aapt2 dump badging`: experiment `package: name='io.github.tedsluis.opencontrolpixelbuds.experiment' versionCode='10301' versionName='1.3.1-exp.1'`; 0
    INTERNET permission.
- **Phase 2 (done):** the skeleton captures/CAP-078-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BN/CAP-078-EVENT-NOTES.md (reference frames, BN-1 … BN-19 in three parts,
  the run card); Test-IDs `ANC-007`, `ANC-008`, `INEAR-007` registered; BN added to `FIND-001/002/004/005`, `INEAR-001…004`, `TOUCH-007`, `PAIR-003`; the Group BN
  block and the index row; `APP_TESTPLAN.md` §AA; `TODO.md` §4; `ARCHITECTURE.md` §5a note.
- **Gate:** `./gradlew --max-workers=1 clean build` — BUILD SUCCESSFUL in 6 m 33 s, exit 0; 5,407 tests (every variant incl. `experiment`), 0 failures, 0
  skipped; `scripts/lint_docs.py` exit 0. The experiment APK after the clean build has the same SHA-256 as before it (reproducible):
  **`4e5facfa3b6178f518750578f3e1ccc5de169103fd619120ac21740eceadd422`**, kept at ~/opencontrol-1.3.1-exp.1/opencontrol-pixelbudspro2-1.3.1-exp.1.apk with its
  .sha256 (not committed).
- **Checkpoint 2 answered** (below). Committed and pushed; draft PR "do not merge before `CAP-078` is analysed".
- **CI on PR #68:** the first run failed in `:data:testReleaseUnitTest` — `DarkModeSettingsStoreTest` ("multiple DataStores active", FileStorage.kt:52): an
  existing race in the test (the first instance's scope was cancelled without waiting), exposed by a third test variant running in parallel. Fixed in the test
  only (`cancelAndJoin`; `e9e5d09`); three local reruns in all three variants pass; the second CI run is green. Not an app change.

## Checkpoints

**Checkpoint 1 (chat 2026-10-16, `AskUserQuestion`, answers verbatim):**
- "A: 13-write" → *"Ja, n=1–4 incl. varianten (Recommended)"* (also one bud worn, Settable `00`, in-ear detection off).
- "B: ring" → *"B1 + B2 (Recommended)"* (hold the claim while ringing; B2 also ACKs each Buds status `ff 01 00 02 04 01`).
- "C: both" → *"Ja (Recommended)"* (`04 01 00 01 03`, buds out of the ears).
- "D en E" → *"Beide (Recommended)"* (`SubscribeToOobeActions`; `ReadSetting 4:8/9/10`).
- "Flavor" → *"Product flavor 'experiment' (Recommended)"*; then, after this session found that a flavor renames the release tasks (`scripts/release.sh`'s APK
  path, the signing check, CI), "BuildType" → *"BuildType 'experiment' (Recommended)"* — a build type `experiment` (from debug) instead; the release chain unchanged.
- "Play-user" → *"Ja, deel 3 (Recommended)"*.

**Checkpoint 2 (chat 2026-10-16):** "Commit" → *"Commit + push, nog niet mergen (Recommended)"*.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0096_FEATURE_RESULT_2026_10_16.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0096_FEATURE_RESULT_2026_10_16

## Files read

**In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md` (ADR-001 … ADR-066), `AI_SESSION_LOG_PROCEDURE.md` — read earlier in this same chat (the
0095 session, its continuation and this one); this prompt; the files changed here, before their change (`app/build.gradle.kts`, `data/build.gradle.kts`,
`DebugScreen.kt`, the changed parts of `SettingsMenu.kt` / `OpenControlNavHost.kt` / `MainActivity.kt`). **In part:** `BudsRepositoryImpl.kt` (the members made
`internal`, `withMessageStream`, `sendRing`, the write path, `writeGate`, the routed-frame branches for `RpcResult`, `Setting`, `Ring`), `Maestro.kt`, `PwRpc.kt`
(`RpcPacket`), `RingFrame*.kt`, `MessageStreamReply.kt`, `BudsTransport.kt`, `RfcommBudsTransport.kt` (`inbound`), `RepositoryModule.kt`; `ai-sessions/0090` RESULT
§E-01, §E-03, §E-04, §E-05, §E-12; `TODO.md` §4/§5; `REVERSE_ENGINEERING.md` (the service table, `qje`/`qhp`); `scripts/release.sh` (the APK path), the CI
workflow (its Gradle tasks). **Not read in full** in this session: `ARCHITECTURE.md` and `PROTOCOL.md` beyond the sections named in the 0095 RESULT.

## Deferred documentation

- "**Round 1 (`ai-sessions/0096`, 2026-10-16):** E-01, E-03, E-04, E-05 and E-12 have an experiment build … and a skeleton, `CAP-078` (Group BN) — the maintainer runs
  it (**M**); a CAPTURE session analyses it; then the FACTs and ADRs for round 2 (release 1.4.0)." (`TODO.md` §4)

## Commits

On `feature/0096-experiment-build` after `61a59b9` (the prompt): `0292e59` feat(experiment) the build type and the card; `f92eb7f` docs(capture) the CAP-078
skeleton and the registers; `e9e5d09` test: the DataStore test race; this RESULT update. Draft PR #68, not merged.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0096_FEATURE_RESULT_2026_10_16.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0096_FEATURE_RESULT_2026_10_16
