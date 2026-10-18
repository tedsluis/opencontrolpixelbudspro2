# 0100_FEATURE_RESULT_2026_10_18.md — The 1.4.0 fixes after `CAP-079`: the ring hold, the stale "don't allow" line, three 1.3.x texts, the experiment's SetWallclock answer — and the skeleton of a short hardware run

**Number:** 0100
**Category:** FEATURE
**Date:** 2026-10-18
**Title:** The 1.4.0 fixes after `CAP-079`, per `ai-sessions/0100_FEATURE_PROMPT_2026_10_18.md`
**Status:** complete

## Summary (plain language)

1.4.1 is built on `feature/0100-fixes` (not committed yet), with the four fixes the maintainer chose in chat:
- **The ring hold** is now 65 s instead of 20 s, so the Buds' "stopped" report reaches the app. This is the ADR-069 Update; no new bytes are sent.
- **The red "don't allow" line** clears itself once the Buds report a mode that can be set.
- **Three texts** are reworded.
- **The experiment build's F1** now reports the Buds' answer to `SetWallclock` instead of a time-out.

Every fix has tests that use real `CAP-079` bytes. Each change was also checked with a mutation check: undoing the change makes a test fail.

The full clean gate is green: 5,407 tests and 0 failures. The release APK holds no experiment code and no `INTERNET` permission. No dependency changed.

The skeleton of the short hardware run is `CAP-080` (Group BP). It includes step-by-step instructions for two things the maintainer asked about:
- **A Disconnect during a Connect.** First make the Connect slow by letting Android drop the link; then use "Use different Buds".
- **`WELL-002` without a sound-level meter** (the maintainer's follow-up, below). The run plays a test file with four steps of exactly 10 dB, then compares the
  Buds' day dose with the live values. In the existing `CAP-079` data the dose already equals the integral of the live value in value × minutes.

TalkBack is moved to `TODO.md`. Nothing was sent to the Buds in this session, and no FACT or new ADR was added.

## Progress

- **Phase 0 started** 2026-10-18, in the same Claude Code session that ran `ai-sessions/0099` and wrote this prompt (the maintainer's "Voer prompt … uit"): the
  reading block of `AGENTS.md` §0.1 was done in that session (0099 RESULT "Files read"); the code below was re-read for this session. Branch
  `feature/0098-round-2` at `30ac14b` (= origin; `HEAD..origin/main` 0); draft PR #69 open, draft, head `30ac14b`; working tree clean.
- **§2 re-derived:** `RING_HOLD_MAX_MS = 20_000L` (`BudsRepositoryImpl.kt:1835`), the hold `startRingHold` (1349–1378); `_ancError.value = null` only at 876, 935
  (Connect, Disconnect), 1011 (an OK write), 1050 (a successful Refresh) — not in `applyAncState`; `routeMaestro`'s `answersUs` (`CodecRouter.kt:328`) lists
  ReadSetting, WriteSetting, GetHardwareInfo only; the texts: `ancNotAllowedLine` "… Tapping a mode checks again first." (`AncScreen.kt:216`), `BATTERY_EXPLANATION`
  (`ConnectionScreen.kt:340–342`), `MESSAGE_STREAM_HINT_TEXT` (`ConnectionBanner.kt:85`, used by `FindMyBudsScreen.kt:73`). The frames of §2: `CAP-079-FINDINGS.md`
  §5.2/§3.3/§11, verified in `ai-sessions/0099`. Difference from the prompt: no `ConnectionScreenTest.kt` exists (the battery text has no screen test yet).
- **Checkpoint 1 answered** (below). Branch `feature/0100-fixes` created from `feature/0098-round-2` at `30ac14b`.
- **Measured for the skeleton (BO-16's instruction):** the app's Connect window ("Disconnected -> Connecting" → "Connecting -> Discovering") in the four `CAP-079`
  exports: 0.19 – 0.60 s in 26 of 27 Connects, **4.41 s** once (E1 07:26:52, the ACL had dropped and was paged). **While Connecting the Connection card has no
  Disconnect button** (`DeviceStatus.kt` `statusCard`: `Connecting → CardAction.NONE`); the only other caller of `disconnect()` is "Use different Buds"
  (`MainActivity.kt:278`). So A68-APP-13 (a) is reachable by hand only through gear → Settings → "Use different Buds" during a slow (paging) Connect.
- **Phase 1 (build), one fix at a time:**
  - The ring hold: `RING_HOLD_MAX_MS = 65_000L` (`BudsRepositoryImpl.kt:1840`, KDoc cites `CAP-079` §5.2). Two tests use `CAP-079` H1 3848/3856/3859/3862:
    - a stop 23.9 s after the Ring (BO-10's tone end) is heard and ACKed, and "Stopped on the bud" is set;
    - an untouched ring's stop at 62.9 s is still inside the hold.
  - The red line: `applyAncState` clears `AncNotAllowed` when the Buds report a mode of 1–4 (`BudsRepositoryImpl.kt:425`). Test: status 9 (`CAP-078` 6777);
    13 = 0 (`CAP-078` 6756) keeps it; the push 13 = 4 (`CAP-079` H1 2742, channel 19) clears it.
  - The three texts, as chosen at Checkpoint 1 (`AncScreen.kt` `ancNotAllowedLine`, `ConnectionScreen.kt` `BATTERY_EXPLANATION`, `ConnectionBanner.kt`
    `MESSAGE_STREAM_HINT_TEXT`), with literal screen tests:
    - `AncScreenTest`, two strings updated;
    - new tests in `BatteryCardTest` and `FindMyBudsScreenTest`, which open each (i) dialog.
  - F1: `routeMaestro`'s `answersUs` now includes `METHOD_SET_WALLCLOCK` (`CodecRouter.kt:330`). Tests:
    - `CodecRouterTest` with H3 2563: an OK `RpcResult`, no `UnidentifiedFrame`;
    - `ExperimentFramesTest`: the request equals H3 2559 byte for byte, and the answer is the `RpcResult` that the runner's `awaitResult` matches.
  - New fixtures file `Cap079Fixtures.kt`. Its header gives the commands; each value carries its frame number.
  - Version 10401 / "1.4.1" (`android/app/build.gradle.kts`); the experiment build becomes "1.4.1-exp.1" through its existing suffix.
- **Phase 2 (gate):** see "Mutation checks" and "Gate" below.
- **Phase 3 (documents):**
  - The skeleton is captures/CAP-080-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BP/CAP-080-EVENT-NOTES.md: Group BP, BP-1 … BP-15, two how-to sections and a run card.
  - `id_registry.csv`: CAP-080 registered as "planned".
  - `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`: BP added to the Group column of eight Test-IDs.
  - `CAPTURE_BLUETOOTH_HCI_SNOOP.md`: the Group BP block and a §9 row.
  - `CHANGELOG.md` [1.4.1]; `README.md` (a 1.4.1 line); `ARCHITECTURE.md` §3.2, the §5 ANC paragraph, §5a and §6.0b.
  - `APP_TESTPLAN.md`: section AC and its Summary row.
  - `TODO.md`:
    - the 0100 item marked built;
    - TalkBack as its own §2 item ("later", the maintainer's choice);
    - §3's next capture changed to `CAP-080`.
  - `ensure_footers.py` (1 file); `ai-sessions/INDEX.md` row 0100.

## Checkpoint 1 (chat 2026-10-18, `AskUserQuestion`, verbatim answers)

- "Versie" → *"1.4.1, nieuwe branch (Recommended)"* — 1.4.0 stays as built (the maintainer publishes it); fixes on `feature/0100-fixes`, 10401 / "1.4.1",
  experiment "1.4.1-exp.1", `CHANGELOG.md` [1.4.1].
- "Ring-hold" → *"65 s, geen nieuwe bytes (Recommended)"* — the ADR-069 Update as in the preview.
- "Rode regel" → *"Wissen bij 13 = 1–4 (Recommended)"*.
- "F1" → *"In de release-codec (Recommended)"*.
- "Tekst ANC" → *"'A tap still sends it' (Recommended)"*: "The Buds don't allow changing noise control right now (usually because no bud is in an ear; checked
  10:07:54). A tap still sends the mode; the Buds answer whether they accept it."
- "Tekst batt." → *"Zoals voorgesteld (Recommended)"*: "Left, Right and the Case: sent by the Buds on their own on the app's channel — when the app connects, on
  Refresh battery and whenever a value changes. The Case is reported only while a bud is charging in the case."
- "Tekst Find" → *"Zoals voorgesteld (Recommended)"*: "Ring and Stop use the Buds' Message Stream channel. While a bud rings the app keeps that channel (up to
  about a minute) to hear when the ringing stops. Another app that uses it (for example Google Play services' Fast Pair) may take it back; the app then cannot see
  the stop."
- "Korte run" → *"Ring-stops (Recommended), Disconnect tijdens Connect (Recommended), WELL-002 met meter (Recommended)"*, with the maintainer's note: *"Hoe doe ik
  een disconnect tijdens een connect binnen 0,3 seconden? Ik heb duidelijke instructie nodig bij het testen van een disconnect tijdens een connect. Ik heb
  instructies nodig voor het uitvoeren van WELL-002. Welke voorbereidingen moet ik nemen? Hoe krijg ik een geluidsmeter in beeld? Talkback wil ik later testen.
  Zet het op de TODO.md"* — TalkBack goes to `TODO.md`, not into the skeleton; the skeleton gets step-by-step instructions for both.



## Follow-up in chat: `WELL-002` without a sound-level meter

**The maintainer (chat 2026-10-18, verbatim):** *"Met betrekking tot WELL-002: ik heb geen geluidsmeter met een microfoon. Wat je wilt meten moet ook uit de APK
(reverse-engineering/apk/v1.0.955078536-10253511) te halen zijn. Bedenk een andere manier zonder dat ik een geluidsmeting hoef uit te voeren."*

**From the APK** (JADX, read-only):
- the live value is "the current volume of your earbuds media measured in decibels (dB)" (`strings.xml` `summary_hearing_wellness_current_level_bottom_sheet`);
- it is shown as `round(10·log10(v))`, 0 below 1, clamped 0–120 (`hiw.java:32–37`; `his.java` case 1);
- "Loud" from 85, "Very loud" from 100 (`HearingWellnessLiveDbPreference.java:54–55`);
- the reference table: 30 Whisper · 60 Normal conversation · 70 Office · 80 City traffic (40 hours per week) · 90 Approaching train (4 h) · 100 Nightclub
  (24 min) · 110 Music festival (4 min) (`han.java:157–163`, `plurals.xml`);
- the exposure percentages use the limits `4.843351E10` (24 h) and `3.3903457E11` (7 days), as already recorded in `REVERSE_ENGINEERING.md` (2026-10-09).

The APK says nothing about the unit of the dose. That is answered from the wire.

**New, from `CAP-079` (no new capture):**
- **Method:** `scripts/dosimeter_dose_vs_live.py` (new, PROJECT_RULES rule 4a) on the outputs of `scripts/pwhdlc_crc_table.py` for H3 (CAP-079-btsnoop_hci4.log.last)
  and H4 (CAP-079-btsnoop_hci4.log), exit 0. Between two consecutive `FetchDailySummaries` answers it compares the change of the current day's entry with the
  integral of the `SubscribeToLiveDb` values on the same channel. Each live value is held until the next packet, with at most 2 s per gap; in silence the value is 0.
- **Result:** the ratio is **0.01666, 0.01687, 0.01682** (channel 19: frames 2644 → 4122, 4122 → 5626, 5626 → 7506) and **0.01667, 0.01675, 0.01705**
  (channel 21: 2692 → 4152, 4152 → 6287, 6287 → 7535) — 1/60 = 0.01667. Field 5 changed by the same amounts.
- 🟡 **proposed, not written into `PROTOCOL.md`:** the Buds' dose is the live value integrated over time in minutes.
- **What follows from it:** the 7-day limit `3.3903457E11` = 40 h × 60 × 1.4126 × 10⁸, i.e. 40 h at 81.5 dB by the app's own formula. Its reference table says
  80 dB for 40 h; the 1.5 dB difference is not explained in the code (🟡).

**The meter-free test** (skeleton BP-13/BP-14, "WELL-002 without a sound-level meter"):
- **The file:** `levels.wav`, made with `ffmpeg` (command in the skeleton). It has four pink-noise steps of exactly 10 dB, checked with `volumedetect`: RMS −20.2 /
  −30.2 / −40.2 / −50.2 dBFS, 60 s each, with silence between.
- **Setup:** buds out of the ears, in-ear detection off, the media volume fixed at half.
- **The run:** F3 before → F2 → the file → F3 after.
- **Refuted if:**
  - the medians of `10·log10(v)` do not step by ≈ 10 (a step of ≈ 5 would mean an amplitude, not a power); or
  - Δ dose / ∫ v dt is not ≈ 1/60.
- **If the live value stays 0 out of the ears:** repeat with one bud worn at a quarter volume.
- **Not tested without a meter:** Google's absolute calibration. That is the firmware's job; the official app trusts it too.

**Documents changed for this:**
- the skeleton (title, intro, P7, the WELL-002 section, BP-13/BP-14, the run card);
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BP block, §9 row); `id_registry.csv` (CAP-080);
- `APP_TESTPLAN.md` (AC, AC12); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`WELL-002` row, a dated addition); `TODO.md` (§3 row).

**The maintainer's answer (chat 2026-10-18, verbatim):** *"Ja, vervang de voorwaarde, PROTOCOL-notitie mag, commit en push"*. Done:
- `DECISIONS.md` ADR-060: an Update replaces (i)'s "against a sound-level meter on film" with the meter-free 10-dB-step test and marks (ii) 🟡 supported. The
  draft stays Proposed.
- `TODO.md`: the two blocking lines now name the meter-free `WELL-002`.
- `PROTOCOL.md` §6 Behavior: a 🟡 item (the day dose = the live value integrated in minutes, with the frames and the command), and a §8 row. No status changed.

## Mutation checks (one at a time, `--max-workers=2`, source restored and md5-checked after each — all "restored ok")

| # | Mutation | Tests run | Result |
|---|---|---|---|
| M1 | `RING_HOLD_MAX_MS = 20_000L` | `BudsRepositoryImplTest` (163) | killed — 2 failed (the 23.9-s and 62.9-s stops) |
| M2 | the `ancError` clearing line removed | `BudsRepositoryImplTest` (163) | killed — 1 failed (the settable-push test) |
| M3 | `METHOD_SET_WALLCLOCK` removed from `answersUs` | `CodecRouterTest` (320) | killed — 1 failed (the SetWallclock test) |
| M4 | the ANC line back to "Tapping a mode checks again first." | `AncScreenTest` (18) | killed — 2 failed |
| M5 | battery text "reported only while" → "reported while" | `BatteryCardTest` (10) | killed — 1 failed |
| M6 | Find text "(up to about a minute)" → "(briefly)" | `FindMyBudsScreenTest` (6) | killed — 1 failed |

## Gate

- **Full clean gate:** `./gradlew --no-daemon --max-workers=1 clean assembleDebug testDebugUnitTest test lint :app:compileReleaseKotlin :app:processReleaseMainManifest
  lintRelease assembleRelease assembleExperiment`. Result: BUILD SUCCESSFUL in 8m 23s, exit 0; 5,407 tests, 0 skipped, 0 failures, 0 errors (the JUnit XML of
  this run).
  - **One difference:** `ExperimentFramesTest`'s new F1 test was added after the gate. It is test-only. `:data:testExperimentUnitTest --max-workers=2` was run
    separately: exit 0, both F1 tests listed.
- **Release APK:** `android/app/build/outputs/apk/release/app-release.apk` — 10401 / 1.4.1, SHA-256
  `0233225e75ec60c9089adf28348fd73afddab90827e5e686646903b6564a1b2b` (a local gate build, not the build of `scripts/release.sh`).
  - Permissions (`aapt2 dump badging`): `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` and the
    build-generated `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` — **no `INTERNET`**.
  - **No experiment code** (`dexdump`, as in 0098): 11 class names contain "experiment", all library classes; 0 `ExperimentRunner`/`ExperimentFrames`; 0 dex
    strings `maestro_pw.Dosimeter` / `SubscribeToLiveDb` / `Experiment F`. The only `ExperimentHook` is the empty `src/noExperiment` slot.
  - Positive control, the experiment APK: 26 / 15 / 7.
- **Experiment APK:** `android/app/build/outputs/apk/experiment/app-experiment.apk` — `….experiment`, 10401 / 1.4.1-exp.1, SHA-256
  `7cc4964758e3b2825050f7e544d49a21cd402b7eafdffd671fb1be21c7a6e0a6`.
- **Dependencies:** unchanged. The only `.gradle.kts` change is the version (`git diff --stat`).
- **`lint_docs.py`:** exit 0 after the INDEX row. The "dead filename references" section is informational: renamed skeleton paths in historical logs.

## Files read

`AGENTS.md` and its reading block (in the 0099 session, the same conversation); the 0100 prompt; `CAP-079-FINDINGS.md` §0, §3.3, §5, §8 – §11; `CAP-079-EVENT-NOTES.md`
(the log names); the `CAP-079` skeleton (`a7e765d`); `BudsRepositoryImpl.kt` (`applyAncState`, `writeAncMode`, `startRingHold`, the constants, `connect`);
`CodecRouter.kt` (`routeMaestro`, `RoutedFrame`); `ExperimentRunner.kt`; `SettingsMenu.kt`; `MainActivity.kt` (`useDifferentBuds`); the three text files and
their tests; `.github/workflows/android.yml`; `CHANGELOG.md`, `ARCHITECTURE.md`, `APP_TESTPLAN.md`, `TODO.md`, `README.md`, `id_registry.csv`, the two capture
documents (the parts edited).

## Deferred documentation

- TalkBack (A93-APP-07): in `TODO.md` §2, "later" (the maintainer).
- What a touch on one bud does during Ring both: an open observation in BP-7 (🟡, not in `PROTOCOL.md` — no evidence yet).

## Commits

The maintainer's "commit en push" (chat 2026-10-18), on `feature/0100-fixes` (from `30ac14b`), pushed without force, not merged:
- `2c68118` fix(data): 65-s ring hold, clear "not allowed" on a settable mode, SetWallclock's answer
- `b34bc6c` fix(ui): noise-control, battery and Find texts describe 1.4
- `0da66b8` build: version 1.4.1 (versionCode 10401)
- `5fd8be1` docs(decisions,protocol): ADR-069 and ADR-060 Updates, the Dosimeter dose note
- `1f75de9` docs(capture): CAP-080 (Group BP) skeleton — the 1.4.1 run
- `634122b` docs: 1.4.1 in CHANGELOG, README, ARCHITECTURE, APP_TESTPLAN and TODO
- `862e1ca` docs: ai-sessions/0100 RESULT and INDEX row
- this list: its own commit.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0100_FEATURE_RESULT_2026_10_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0100_FEATURE_RESULT_2026_10_18
