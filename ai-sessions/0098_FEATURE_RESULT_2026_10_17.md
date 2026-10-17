# 0098_FEATURE_RESULT_2026_10_17.md — Round 2 (release 1.4.0): noise control through setting 13, the L/R battery from runtime info, the ring status, Ring both, the open app items, a Dosimeter experiment

**Number:** 0098
**Category:** FEATURE
**Date:** 2026-10-17
**Title:** Round 2 (release 1.4.0), per `ai-sessions/0098_FEATURE_PROMPT_2026_10_17.md`
**Status:** awaiting maintainer sign-off (Checkpoint 2: commit, push, the pull request)

## Summary (plain language)

**What 1.4.0 changes for the user** (built and unit-tested; **not yet run on hardware**):

- **Noise control** (tab and tile) is switched on the app's own channel to the Buds instead of the Fast Pair channel that Google Play services also uses —
  so in the Owner user (with Play) a mode change no longer collides with Play. With no bud worn the Buds refuse a change, and the app says so. The tab and the
  tile offer only the modes ticked for press and hold; an unticked one is greyed out with a line saying why.
- **Battery Left/Right** come from the same channel as the Case; Connect and *Refresh battery* no longer touch the Fast Pair channel. **The app now uses that
  channel only for Find My Buds.**
- **Find My Buds:** Ring both; while a ring sounds the app keeps the channel open (≤ 20 s), answers the Buds' status and says "Stopped on the bud at …" when a
  touch on the bud (or the Buds' own time-out) stops it; one caption says to take the Buds out of your ears first.
- The Connection tab shows "Noise control: <mode>"; the EQ write is logged; the tile no longer flashes "unavailable" before its first state; four robustness
  fixes (a Disconnect during a Connect wins, a cancelled write quarantines, a late profile proxy is closed, `reclassifyLoss` atomic).
- In both users the same code runs; in the user without Play nothing else holds the Fast Pair channel, in the Owner user Play does — there a ring's hold is
  cut ≈ 4.5–5 s in (`CAP-078`), so "Stopped on the bud" can only appear in the user without Play (a Known issue in `CHANGELOG.md` [1.4.0]).

**The experiment build 1.4.0-exp.1** (never released): its card keeps A (a write of a mode not ticked in field 12) and adds F, the Dosimeter — F1
`SetWallclock`, F2 `SubscribeToLiveDb` (channels 19 and 21), F3 `FetchDailySummaries`, F4 `WriteSetting 4:{21:b}` (channels 21 and 19) — one tap each after
a confirmation naming the bytes; answers and stream packets are logged as raw fields (the format of `scripts/pwrpc_decode.py`), with a live-packet count per
minute; no unit, no health wording. Draft ADR-060 is unchanged.

**What the hardware run (`CAP-079`, Group BO) must show:** no DLCI 0x04 at Connect, Refresh battery, a mode change or the tile; every mode write answered OK
(status 9 with no bud worn); the tile skipping an unticked mode; each ring status ACKed and "Stopped on the bud" after a touch (not after the app's Stop);
Ring both ringing both buds; TalkBack reading each switch with its label; a hold within ≈ 1 s of a Connect shown; a Disconnect during a Connect winning; in
the experiment build, the Dosimeter answers against the sound-level meter at four levels.

## Progress

- **Phase 0 started 2026-10-17.** `feature/0096-experiment-build` at `d6661be` (= origin); 12 commits ahead of `origin/main`, `HEAD..origin/main` empty; draft PR #68
  open (head `d6661be`). Registry: ADR-001 … ADR-066 (66 rows), the next free ADR number is 067.
- **Reading done for the decisions:** `BudsRepositoryImpl.kt` in full (1,833 lines), `RuntimeInfo.kt`, `SafeModeGate.evaluate`, `SettingFrame.kt` (fields,
  builders), the experiment runner's frame builders, `AncTileService` (its calls); `PROTOCOL.md` §4.4/§4.5 (runtime info), ADR-009/027/031/033 status lines.
  Bytes for F1–F3/F4 extracted: `CAP-041` 783, `CAP-027` 898/1107/1212/1214, `CAP-058` 5736/5740/5747/5753 (`scripts/pwhdlc_crc_table.py`, handles 0x0002/0x000d).
- **Checkpoint 1 done** (answers below). Branch `feature/0098-round-2` from `feature/0096-experiment-build` (`d6661be`). Written: ADR-067 … ADR-070 in
  `DECISIONS.md` with pointer Updates on ADR-009/027/031/033, `id_registry.csv` rows, the README count (63 accepted), the `PROJECT.md` non-goal note.
- **ADR-067 note (chat 2026-10-17, "ADR-067 §3" → *"Weigeren + experiment (Recommended)"*):** field 12 is the hold list; a mode not ticked is refused,
  tested on the experiment card in the hardware run. Written as a dated Note on ADR-067.
- **Phase 1, A + B built (`:data`):** `SettingsCodec.ancStateRequest`; `BudsRepositoryImpl.writeAncMode` (setAncMode/stepAncMode — gate, hold list, write,
  RESPONSE/FAILED_PRECONDITION/timeout), `refreshAncMode` = `ReadSetting 4:13`, `ancError` (domain, default flow), `BudsError.AncModeNotInHoldList`; the Connect
  snapshot claim, the Message Stream ANC Get/Set paths, `_ancModeFresh`/`_ancNotifies`/`_ancOutcomes`/`getPending` removed; setting-13 availability never
  provisional (fixed an overflow with no claim); `RuntimeInfo.left/right` from 6.2/6.3; `refreshBattery` = re-subscribe runtime info, waits for a packet.
  Tests: 37 obsolete tests deleted (list in the RESULT's test section), 13 rewritten (Ring instead of ANC for the claim and Safe-Mode tests; I-4/I-8 battery
  expectations), new `Cap078Fixtures.kt` (CAP-078 real frames), new tests (SettingsCodecTest ancStateWrites; 12 repository tests; RuntimeInfoCodecTest L/R
  + a labelled structural out-of-range test). `:data:testDebugUnitTest`: 1,656 tests, 0 failures.
- **C + D built:** `RingTarget.BOTH`; `onRingStatus` (ACK `ff 01 00 02 04 01` while the claim is open; "stopped on the bud" unless it answers the app's
  Stop); the Ring hold as a job holding `claimMutex` until the stop status / Stop / a new Ring / a foreign close / `RING_HOLD_MAX_MS` = 20 s, then the
  linger; `ringStoppedOnBudAt` (domain default flow). Tests: 5 repository tests with CAP-078 bytes, the ring codec's `0x03`. A test found an overflow
  (`now - Long.MIN_VALUE`) in the stopped-on-the-bud check — fixed.
- **UI:** ANC tab (`ancError`, unticked modes disabled with `ANC_HOLD_LIST_HINT_TEXT`), Find (Ring both, "Stopped on the bud at …", a caption "Take the Buds
  out of your ears before ringing — the sound is loud." — ADR-070 item 1 says "the same warning", but the release Find had none; one caption now covers all
  three rings), Connection tab "Noise control: …" (P6), the error text for `AncModeNotInHoldList`; the tile (ADR-067 docs/log; P2: no render before the first
  state). P1: "EQ written (channel N)".
- **2.2 (chat 2026-10-17, "2.2 Retry" → *"Niet wijzigen (Recommended)"*):** 400 ms stays — attempt 2 succeeded 8 of 8, 0.63–0.93 s after attempt 1
  (`CAP-078` E3).
- **2.4:** A68-APP-10 (the non-atomic `openChannel` insert) was already fixed in 1.3.1 (A93-APP-05, `Connection.attach` under the lock) — nothing to do.
  A68-APP-13: (a) a Disconnect during a Connect wins (`disconnectGeneration`; test with `FakeBudsTransport.connectGate`); (b) `reclassifyLoss` stores and logs
  a cause with one compare-and-set (no deterministic test can force the three-caller race — covered by the existing loss tests); (c) a cancelled write
  starts the quarantine (`awaitWriteOrQuarantine`, three write paths; test); (d) `ProfileProxies` closes a proxy bound after `closeAll` (test); the design
  question "a Notify of the old mode ends a Set's wait" is gone with the Message Stream `Set` (ADR-067).
- **2.5:** `:app` got a JUnit 5 set-up (`testOptions.unitTests.isReturnDefaultValues`, the catalogue's JUnit 5 entries, test scope only) and `AppTextsTest`
  (4 tests: the pairing texts, the tile's result codes, the no-browser text). `MainActivity`'s state collection and the DI graph stay without a test (a
  Robolectric `:app` set-up is larger than this round — kept in `TODO.md` §5).
- **F built (the experiment source set only):** `ExperimentFrames.setWallclock/subscribeToLiveDb/fetchDailySummaries/writeField21`, the Dosimeter
  service/method ids by `PwRpc.nameHash`, `dosimeterLine` + `describeFields` (the inbound decoder; a `SharedFlow` collector of DLCI 0x02, sends nothing),
  the runner's F1–F4 (F1/F4 through the write gate and the write quarantine, awaiting the `RESPONSE`; F2/F3 sent and answered in the log), the card trimmed to
  A + F ("Experiments (round 2, not a release)"); round 1's B, C, D, E removed from the card (B/C are in the release, D/E answered).
- **Version:** 1.4.0, versionCode 10400 (`android/app/build.gradle.kts`, the scheme major × 10000 + minor × 100 + patch); the experiment build is 1.4.0-exp.1.
- **`:ui` tests** updated for the changed parameters (`ancError`, `ancModeList`, `ringStoppedOnBudAt`), plus 4 new (the hold-list hint and disabled mode, an
  unread list, Ring both and its caption, "Stopped on the bud"); a test found that the ready-state ring notice had become "Ringing: the Right earbud" — fixed
  back to 1.3.1's wording.
- **Mutation checks (12), the clean gate, the release-APK proof:** below. **Phase 3** done: the `CAP-079` skeleton, the registers, the documents.

## Checkpoint 1 (chat 2026-10-17, `AskUserQuestion`, verbatim answers)

- "Branch" → *"Van feature/0096 (Recommended)"*.
- "ADR A" → *"Accepteren zoals getoond (Recommended)"* → ADR-067.
- "ADR B" → *"Accepteren zoals getoond (Recommended)"* → ADR-068.
- "ADR C" → *"Accepteren zoals getoond (Recommended)"* → ADR-069.
- "ADR D" → *"Accepteren zoals getoond (Recommended)"* → ADR-070.
- "Dosimeter" → *"Ja, alleen als experiment (Recommended)"*.
- "Experiment" → *"Behouden als hulpmiddel (Recommended)"*.
- "2.7" → *"Later (Recommended)"*.
- "F1 Wallclock" → *"Akkoord (Recommended)"*; "F2 LiveDb" → *"Akkoord (Recommended)"*; "F3 Daily" → *"Akkoord (Recommended)"*; "F4 Field 21" → *"Akkoord, ook
  kanaal 19"* (the never-captured channel-19 form included).


## Checkpoint 2 (chat 2026-10-17, `AskUserQuestion`, verbatim answers)

- "Commit+push" → *"Ja, commit en push (Recommended)"*.
- "PR" → *"Ja, draft-PR (Recommended)"*.
- "PR #68" → *"Sluiten met verwijzing (Recommended)"*.

## What was built (files)

`DECISIONS.md` (ADR-067 … ADR-070, pointer Updates on ADR-009/027/031/033, the ADR-067 Note), `id_registry.csv` (ADR-067 … 070, `CAP-079`, `WELL-003`),
`README.md`, `PROJECT.md`, `CHANGELOG.md` `[1.4.0]`, `ARCHITECTURE.md` (§3.1, §3.2, §5a, §6.0b, §12), `PROTOCOL.md` (implementation notes in §4.1, §4.3
Option F, §4.4 and a change-log row — no status changed), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BO block, §9 row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
(BO in the Group column of 15 Test-IDs, `WELL-002`'s Group, new `WELL-003`), `APP_TESTPLAN.md` (§AB), `TODO.md`, `scripts/release_notes.template`,
`ai-sessions/INDEX.md`, captures/CAP-079-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BO/CAP-079-EVENT-NOTES.md (new).
Code: `:domain` `BudsError` (`AncModeNotInHoldList`), `BudsRepository` (`ancError`, `ringStoppedOnBudAt`), `RingTarget.BOTH`; `:data` `BudsRepositoryImpl`,
`codec/SettingFrame.kt`, `codec/RuntimeInfo.kt`, `codec/RingFrame.kt`, `src/experiment/…/ExperimentRunner.kt`; `:hardware` `ProfileProxies.kt`,
testFixtures `FakeBudsTransport` (`connectGate`); `:ui` `AncScreen`, `FindMyBudsScreen`, `ConnectionScreen`, `OpenControlNavHost`; `:app` `MainActivity`,
`AncTileService`, `build.gradle.kts`, `src/experiment/…/ExperimentHook.kt`. Tests: `Cap078Fixtures.kt` (new), `SettingsCodecTest`, `RuntimeInfoCodecTest`,
`RingCodecTest`, `BudsRepositoryImplTest`, `ProfileProxiesTest`, `ExperimentFramesTest`, `AncScreenTest`, `UserMessageTest`, `FindMyBudsScreenTest`,
`AppTextsTest` (new).

## Log strings (always on, payload-free; for the next CAPTURE session)

| Line | Where |
|---|---|
| "Noise-control mode written (channel N): <mode>" | `writeAncMode`, status OK |
| "Noise-control mode not written: the Buds allow no change (FAILED_PRECONDITION — no bud worn?)" | status 9 |
| "Noise-control request failed: <reason>" | `ancFailed` (hold list, list not read, timeout, rejected, channel) |
| "Ring status from the Buds: ringing left / right / both" · "… : stopped" | `onRingStatus` |
| "Ring status acknowledged (ff 01 00 02 04 01)" | the ACK, sent only while DLCI 0x04 is open |
| "Ring: claim held until the Buds report the stop (at most 20 s)" · "Ring: hold ended — the Buds reported the stop / the claim was closed by another client / no stop report within 20 s" | `startRingHold` |
| "Connect finished after a Disconnect tap: its channels are closed again" | `openSession`, A68-APP-13 (a) |
| "EQ written (channel N)" | P1 |
| "ANC tile tapped (shown: <mode>[, the Buds last allowed no change]): the mode after it is written on the session channel" | `AncTileService` |
| Experiment: "Experiment A / F1 / F2 / F3 / F4: sending <hex> (channel N)", "Experiment F1: the phone's clock N ms", "Experiment <x>: <type> <status>" / "… failed: <error>", "Experiment F: <type> ch=N <method> <status> <fields>", "Experiment F: N live-stream packets in the last S s" | `ExperimentRunner` |

## Tests

- **Deleted (37, obsolete with ADR-067/068 — the Message Stream `Get`/`Set`, the Connect snapshot, the battery claim):**
  `setAncMode sends the Get, then the exact Set bytes, and applies the mode on the ACK`; `the Get's Notify does not answer the following Set`; `refreshAncMode sends Get and resolves once a fresh Notify arrives`; `refreshBattery claims only the Message Stream and never opens DLCI 0x08 (ADR-043)`; `an allowed tap sends the Get first and exactly one Set`; `a tap whose Notify reads 00 sends no Set`; `the CAP-064 stale e8 no longer leads to a Set`; `an unknown availability also sends the Get first`; `a tap after a 00 re-checks and sends the Set when the Buds now allow it`; `a failed claim or an unanswered Get sends no Set`; `the tile steps from the fresh Notify, not from the mode shown`; `a Set whose answer is cut off is AnswerCutOff and leaves the mode unconfirmed`; `a Get whose answer is cut off is AnswerCutOff, not Timeout`; `a plain timeout with the channel open stays Timeout`; `an answer that arrives just after the close still counts`; `a Refresh whose Get is cut off after the burst still counts`; `a Refresh re-reads the byte and re-enables the Set (I-3)`; `a Refresh with the channel still open releases it and claims afresh`; `a Refresh re-subscribes to runtime info once`; `a Refresh whose claim brings no battery frame says so and keeps the old values and times`; `a Refresh whose claim fails reports the claim's error and changes no value`; `an ANC tap claims the Message Stream, sends on it, and releases it after the linger`; `the Buds' own Notify wins over the optimistic ANC update`; `a re-Notify of the unchanged mode before a NAK leaves the Buds' mode on screen`; `an ANC Set the Buds NAK is a CommandRejected failure and the previous mode stays (APP-3)`; `an ANC Set the Buds never answer is a Timeout and the previous mode stays (APP-3)`; `a Settable reading within 2 s of the claim is provisional, a later one in the same claim is not (ADR-024, APP-5)`; `the Get's answer is a reading`; `the app's own acknowledged Set is set by this app`; `a Notify of the requested mode while the Set waits is set by this app`; `a differing Get answer is changed since the last read`; `a session's first differing read is changed since the app last saw it`; `the app's own Set after a differing Get stays set by this app`; `the equal read after a differing Get keeps changed since`; `a stream push does not end a Refresh`; `the stream packet after the app's own Set is set by this app`; `the stream packet before the ACK or after a cut-off answer is set by this app`.
- **Rewritten (13):** the claim and Safe-Mode tests now use Ring/Stop instead of an ANC tap (a Find command is the only Message Stream claim); I-4/I-8's
  battery expectations (runtime info); "nothing is held or re-claimed".
- **New:** `SettingsCodecTest` `ancStateWrites` (CAP-078 H 7277/6491/6540/7298/5921); 13 ADR-067/068 repository tests (incl. M3b's "the push after an
  unanswered write is the app's own"); 5 ADR-069/070 repository tests (CAP-078 H 7772 … 7816, 7970, 8203/8211); 2 A68-APP-13 tests (`connectGate`, the
  cancelled write); `RuntimeInfoCodecTest` L/R (real) + one labelled structural out-of-range test; `RingCodecTest` ring both; `ProfileProxiesTest` the late
  proxy; `ExperimentFramesTest` 7 (A = CAP-078; F1 = CAP-041 783; F2 = CAP-027 1212/1214; F3 = CAP-058 5736; F4 = CAP-058 5747/5753 on channel 21 and a
  labelled structural channel-19 test; the decoder against CAP-027 1228/1229 and CAP-058 5740, equal to `scripts/pwrpc_decode.py`'s lines, plus a fuzz of
  20,000 random frames); `:ui` 4; `:app` 4.

### Mutation checks (one at a time, `--max-workers=2`, the source restored and checked by md5 after each)

| # | Rule broken | Mutation | Result |
|---|---|---|---|
| M1 | status 9 → not allowed | `status == PW_STATUS_FAILED_PRECONDITION` → `== 99` | killed (1 failed) |
| M2 | the hold-list check | `if (!list.isSelected(mode))` → `if (false)` | killed (1) |
| M3a | the write's OK is `SET_BY_APP` | `emitAncMode(mode, SET_BY_APP)` → `CHANGED_BY_BUDS` | killed (1) |
| M3b | the window after the write | `lastSetMode == mode && … <= ANC_SET_WINDOW_MS` → `false` | **survived** at first (equivalent while the RESPONSE shows the mode); new test "the push after an unanswered write is the app's own" → killed (1) |
| M4 | the ring ACK only while DLCI 0x04 is open | `if (transport.isChannelOpen(…))` → `if (false)` | killed (1) |
| M5 | the hold ends on the stop status | `it >= startedAt` → `it < startedAt` | killed (1) |
| M6 | "stopped on the bud" not after the app's Stop | the `stopSentAt` condition → `true` | killed (1) |
| M7a | runtime info L/R | `bud(ENTRY_LEFT …), bud(ENTRY_RIGHT …)` swapped | killed (2 in `RuntimeInfoCodecTest`) |
| M7b | an absent entry is "last seen" | `(kept as? Known)?.copy(isStale = true) ?: kept` → `kept` | killed (3) |
| M8 | a Disconnect during a Connect wins | the generation guard → `if false` | killed (1) |
| M9 | quarantine on cancel | `startWriteQuarantine()` removed from the catch | killed (1) |
| M10 | a late proxy is closed | `close?.invoke(profile, proxy)` → `Unit` | killed (1 in `ProfileProxiesTest`) |

Script: /tmp/claude-1000/mut098/run.py (scratch, not committed); the three source files' md5 equal before and after.

## The gate (Phase 2)

`./gradlew --max-workers=1 clean assembleDebug testDebugUnitTest test lint :app:compileReleaseKotlin :app:processReleaseMainManifest lintRelease
assembleRelease assembleExperiment` (the CI task lists of `.github/workflows/android.yml` and `RELEASING.md` A4, plus both APKs) — **BUILD SUCCESSFUL in
7 m 51 s, exit 0**. Tests, failures 0 everywhere: `:domain` 42; `:hardware` 67 (debug) / 67 (release); `:data` 1,666 (debug) / 1,666 (release) / 1,673
(experiment); `:ui` 99 / 99; `:app` 4 / 4 / 4. `lint_docs.py` exit 0; `ensure_footers.py` up to date.

- **Release APK** `android/app/build/outputs/apk/release/app-release.apk` — `io.github.tedsluis.opencontrolpixelbuds`, versionCode 10400, versionName 1.4.0,
  SHA-256 `ed0c8a22b82cbf9c99f1f4891544acf4e9fbba96317118fc143b9fbb3bbf448e` (a local gate build, not the release build of `scripts/release.sh`). **No
  experiment class:** `dexdump` lists 11 class names containing "experiment", all library classes (`androidx.annotation.experimental.*`,
  `kotlin.experimental.*`); 0 `ExperimentRunner`/`ExperimentFrames`; 0 dex strings `maestro_pw.Dosimeter` / `SubscribeToLiveDb` / `Experiment F` (the
  experiment APK: 15 classes, 6 strings — the positive control).
- **Experiment APK** `android/app/build/outputs/apk/experiment/app-experiment.apk` — `….experiment`, 1.4.0-exp.1, SHA-256
  `67f14bdd9716480231aba95add37f167c6b32c8d83b1d1983a61137de9aba5e8` (debug-signed; not committed).
- **Permissions** (`aapt2 dump badging`): `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` and the
  package's own `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` — no `INTERNET`; no manifest changed (`git diff --stat HEAD -- '*/AndroidManifest.xml'` empty).
- **Dependencies:** no implementation dependency changed (`git diff` of `gradle/libs.versions.toml` empty; `:app/build.gradle.kts` adds only
  `testImplementation`/`testRuntimeOnly` JUnit 5 entries the other modules already use); `:app:dependencies --configuration releaseRuntimeClasspath`: 892
  lines, 0 matches for gms/firebase/okhttp/analytics/crashlytics/sentry.

## Files read

In full: `ai-sessions/0098_FEATURE_PROMPT_2026_10_17.md`, `BudsRepositoryImpl.kt` (1,833 lines at the start), `RuntimeInfo.kt`, `SettingFrame.kt`,
`ExperimentRunner.kt`, `ExperimentHook.kt`, `ExperimentFramesTest.kt`, `AncTileService.kt`, `ProfileProxies.kt`, `FindMyBudsScreenTest.kt`,
`CAP-078-EVENT-NOTES.md`'s appendix (the skeleton layout). In part: `DECISIONS.md` (ADR-009/027/031/033/059/060/067 … 070), `PROTOCOL.md` (§4.1, §4.3
Option F, §4.4, the change log), `ARCHITECTURE.md` (§3.1, §3.2, §5a, §6.0b, §12), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BN, §9), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
(the Test-ID rows changed), `APP_TESTPLAN.md` (§AA, the Summary), `TODO.md` (§1, §3, §4, §5), `README.md`, `PROJECT.md`, `CHANGELOG.md` (head, [1.3.1]),
`RELEASING.md` (A1–A6), `.github/workflows/android.yml`, `scripts/pwrpc_decode.py` (usage), `scripts/release_notes.template`, the `:ui` and `:data` tests
touched.

## Deferred documentation (each also in `TODO.md`)

- The hardware run `CAP-079` and its analysis; the 🟢/🟡 of every 1.4.0 behaviour waits for it (`TODO.md` §3, §4).
- `RELEASING.md` B–D (the build, the tested APK, the tag and publication) — the maintainer's, after the run; A5/A6 follow Checkpoint 2.
- `:app` Robolectric tests for `MainActivity` and the DI graph (`TODO.md` §5).
- A68-APP-13 (b): no deterministic test of `reclassifyLoss`'s three-caller race (`TODO.md` §5).
- 2.6 `targetSdk`, 2.7 `strings.xml` / third-party notices, D/E follow-ups and the per-bud in-ear research (`TODO.md` §4, §5).

## Commits

None yet — awaiting Checkpoint 2 (the maintainer's answer in chat). Proposed per concern: (1) docs — ADR-067 … ADR-070 and the registers; (2) feat(data/ui/app)
— the 1.4.0 code and tests; (3) feat(experiment) — the round-2 card; (4) build — 1.4.0; (5) docs — the `CAP-079` skeleton, the test plans, ARCHITECTURE,
PROTOCOL notes, PROJECT, README, CHANGELOG, TODO, the release-notes template, this RESULT and the INDEX row.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0098_FEATURE_RESULT_2026_10_17.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0098_FEATURE_RESULT_2026_10_17
