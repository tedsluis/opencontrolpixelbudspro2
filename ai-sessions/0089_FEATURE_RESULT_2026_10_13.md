# 0089_FEATURE_RESULT_2026_10_13.md — The noise-control channel in the Play user after CAP-075: a constant 3-s re-claim step with a longer chain, a "Take it back again" action, a cause label for a re-claim's read, and a clearer switch

**Number:** 0089
**Category:** FEATURE
**Date:** 2026-10-13
**Title:** Build the follow-up of `CAP-075` (`ai-sessions/0088`, "P1 + P2 + P4"): the constant 3-s re-claim step with a longer, capped chain, a "Take it back
again" action on the hint card, a cause line for a mode learned by a re-claim's own read, and the switch renamed and explained (options that say whether the
phone has Google Play services)
**Status:** complete (closed 2026-10-14 in the chat continuation: PR #49 merged, 1.2.2 published with tag `v1.2.2` on `bfd7891` — the maintainer released it after his own test on the phone, without `CAP-076`; see Commits)

## Summary (plain language)

**What 1.2.2 does differently** (versionCode 10202; prepared on branch `feature/channel-control`; nothing committed, no tag, no release):

1. **The setting has a clearer name and clearer choices.** gear → Settings → **"Noise control with Google Play services"**. The card says, in one sentence, that the
   Buds report a press-and-hold to one app at a time, then asks which phone this is: **"This phone has no Google Play services"** (the default; nothing else uses
   the channel — if another app takes it anyway, the noise-control tab says so) or **"This phone has Google Play services"** (while the noise-control tab is open
   the app takes the channel back from Play's Fast Pair 3 seconds after each time it takes it, so a press-and-hold shows about half of the time). Under the intro
   the card says what Android reports: **"Google Play services: found / found but disabled / not found on this phone"** — information only, the choice stays the
   user's (ADR-063; the app asks Android's package manager, nothing more). A choice made in 1.2.1 keeps its meaning ("Take it back" = "has Google Play services").
2. **With Google Play services the app keeps taking the channel back — 3 s after every cut, up to 20 times** (about 3 minutes) per visit of the tab, instead of
   three times at 3, 6 and 12 s. `CAP-075` showed Play takes the channel back after the same 5–6 s every time, so the longer pauses only added dead time; at 3 s
   every cycle is ≈ 9.3 s with ≈ 4.6–4.9 s held — about half of the press-and-holds shown for 3 minutes (1.2.1: 43 % of 44 s, then nothing).
3. **"Take it back again"** under the noise-control hint: one claim with the usual `Get` — no mode written, no battery read — that starts a new round.
4. **A cause line for a mode the app read** after a change it could not see: "Changed since the app last saw it (read at HH:MM:SS): a press-and-hold on a bud or
   the Buds' own change, made while this app did not have the channel." (1.2.1 showed such a mode without a cause, `CAP-075` film 2 s265–269.)
5. Nothing new on the wire; never in the background; the user without Play services sees no difference except the texts.

**What the maintainer does next:** review; say whether to commit and push (asked below); then the pull request, `scripts/release.sh 1.2.2`, the `CAP-076` run
in both users (the skeleton is ready), and a CAPTURE session for the verdict (§G).

## Progress

- Phase 0: branch `feature/channel-control` from `main` at `f4719fb` (up to date with `origin/main`: `git fetch` + `git log HEAD..origin/main` empty);
  `git status --short`: only `?? android/domain/bin/` (build output, never staged). Baseline gate: running.
- Phase 0 gate (before any change, `cd android && ./gradlew assembleDebug testDebugUnitTest test lint --max-workers=2`): BUILD SUCCESSFUL, exit 0 —
  `:data` 1664, `:domain` 42, `:hardware` 64, `:ui` 95 tests, 0 failures. `python3 scripts/lint_docs.py`: exit 1, only "missing or incorrect footer" on this
  new RESULT file (fixed by `ensure_footers.py` in Phase F).
- Phase A: done (below). Phase B: done — answers in §B.
- Phase C: code done — `:domain` (`AncModeCause.CHANGED_SINCE_LAST_READ`, `PlayServicesPresence`, `BudsRepository.reclaimAncChannel`), `:data`
  (`HOLD_RECLAIM_STEP_MS`/`HOLD_RECLAIM_CAP`, `reclaimAncChannel`, `causeOfNotify`), `:ui` (the card, the hint + button, the cause line, the ANC (i)), `:app`
  (`PlayServicesProbe`, the wiring, the manifest `<queries>`); tests: `Cap075Fixtures`, `BudsRepositoryImplTest` (+8, one rewritten), `HoldReclaimSettingsStoreTest`
  (+1), `AncScreenTest`, `SettingsMenuTest`; `:data` and `:ui` green. ARCHITECTURE.md: Phase F.
- Phase D: done — the gate green (`:data` 1672, `:domain` 42, `:hardware` 64, `:ui` 102; lint 0 errors, the 7 warnings pre-existing); M1–M6 killed and reverted
  (§D); compliance (§D).
- Phase E: done — captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/CAP-076-EVENT-NOTES.md (BL-1 … BL-24), the Group BL section and the `CAP-076` index row in
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, the planned `CAP-076` row (and `ADR-063`) in `id_registry.csv`, `APP_TESTPLAN.md` §Y (Y1–Y14) and its Summary row.
- Phase F: done — `DECISIONS.md` (ADR-062 Update 2, ADR-063 — the approved texts), `AGENTS.md` §1 (the dated note), `id_registry.csv`, `TODO.md`, `PROJECT.md`,
  `README.md`, `CHANGELOG.md` `[1.2.2]`, `scripts/release_notes.template`, `android/app/build.gradle.kts` (1.2.2 / 10202), `ARCHITECTURE.md` §2.4, §3.2, §5a,
  §6.0b, §9; `ensure_footers.py` (2 files) and `lint_docs.py` exit 0.
- Phase G: the final gate after every change (`cd android && ./gradlew assembleDebug testDebugUnitTest test lint --max-workers=2`): BUILD SUCCESSFUL, exit 0 —
  `:data` 1672, `:domain` 42, `:hardware` 64, `:ui` 102 tests, 0 failures (baseline 1664 / 42 / 64 / 95: +8 `:data`, +7 `:ui`); lint `:app` 0 errors 6
  warnings (`OldTargetApi`, `GradleDependency` ×5 — pre-existing), `:ui` 1 warning (`ModifierParameter` at `SettingsMenuScreen`'s existing `modifier`
  parameter — pre-existing), `:data`/`:hardware` none; `:app:dependencies --configuration releaseRuntimeClasspath` identical before and after the version bump;
  `lint_docs.py` exit 0. Committed and pushed after the maintainer's yes (§Commits).

## A. Evidence and design (Phase A)

### A.1 Re-derived from `CAP-075-btsnoop_hci2.log` (handle `0x000b`), the export E2 and the logcat extract

Command: `tshark -r CAP-075-btsnoop_hci2.log -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci==4" -T fields -E separator='|' -e frame.number -e frame.time_epoch
-e frame.p2p_dir -e btrfcomm.frame_type -e data.data` (exit 0, 945 lines), claims sorted by a scratch script (owner = the first phone message after the
`SABM`: `08 11 00 00` = this app, `03 08 00 02 01 25` = Play). Times local (+02:00). "cut" = the phone's `DISC` written by the stack when Play's connect is
refused; E2 = `CAP-075-opencontrol-debug-20261013-063533.txt`; X = `CAP-075-logcat-extract.txt`.

| Re-claim | Cut (`DISC`) | Play's retry `SABM` (after the cut) | E2 "re-claim" line | App attempt 1 refused (E2 / X `already at opened state`) | Play's `DISC` (killed) | App `SABM` (after the cut) | Next cut (held) |
|---|---|---|---|---|---|---|---|
| chain 1, 1/3 | 2369 06:30:19.281 | 2401 21.799 (+2.518) | 22.330 "1/3 … 3000 ms" | 22.869 "after 537 ms" / X 9079 22.866 | 2476 22.867 | 2502 23.760 (+4.479) | 2570 28.509 (4.749 s) |
| chain 1, 2/3 | 2570 06:30:28.509 | 2606 31.078 (+2.569) | 34.580 "2/3 … 6000 ms" | 34.999 "416 ms" / X 9587 34.995 | 2694 34.996 | 2716 35.717 (+7.208) | 2770 40.535 (4.818 s) |
| chain 1, 3/3 | 2770 06:30:40.535 | 2805 43.055 (+2.520) | 52.718 "3/3 … 12000 ms" | 53.182 "461 ms" / X 10544 53.180 | 2938 53.181 | 2960 54.026 (+13.491) | 3030 58.742 (4.716 s) |
| chain 2, 1/3 | 06:32:45.3 (E2 45.327; X 15092 45.235) | 4768 (+2.5) | 48.332 "1/3" | 49.128 "792 ms" / X 15465 49.123 | 4842 49.124 | 4865 50.090 (≈ +4.8) | 4924 54.728 (4.638 s) |
| chain 2, 2/3 | 4924 06:32:54.728 | 4957 57.545 (+2.817) | 33:00.865 "2/3" | 01.341 "473 ms" / X 16147 01.336 | 5053 01.337 | 5080 02.235 (+7.507) | 5147 07.158 (4.923 s) |
| chain 3, 1/3 | 5625 06:33:43.272 | 5666 45.853 (+2.581) | 46.454 "1/3" | 47.155 "699 ms" / X 18088 47.154 | 5737 47.154 | 5762 48.118 (+4.846) | 5818 52.672 (4.554 s) |
| chain 3, 2/3 | 5818 06:33:52.672 | 5856 55.344 (+2.672) | 58.749 "2/3" | 59.219 "468 ms" / X 18622 59.216 | 5934 59.218 | 5957 34:00.132 (+7.460) | 6020 04.680 (4.548 s) |
| chain 3, 3/3 | 6020 06:34:04.680 | 6056 07.143 (+2.463) | 16.741 "3/3" | 17.007 "261 ms" / X 19507 17.000 | 6211 17.001 | 6235 17.730 (+13.050) | 6309 22.328 (4.598 s) |

🟢 FACT (this Play version): every re-claim's attempt 1 is refused while Play's retry is on the channel and kills Play's socket (its `DISC` within 3 ms of the
refusal); attempt 2's `SABM` follows 0.7–1.0 s later; every re-claimed hold lives 4.55–4.92 s. The E2 "re-claim" lines are 3 009–3 011 / 6 003–6 006 /
12 003–12 006 ms after the "hold ended: the channel was closed" lines (e.g. 06:30:19.322 → 22.330). So the first step (3.0 s after the close line) is exactly
the step P1 makes constant: a 3-s cycle = cut → 3.0 s → attempt 1 refused (0.26–1.05 s) → attempt 2 → `SABM` ≈ 4.5–4.9 s after the cut → held 4.55–4.92 s →
cut ⇒ **≈ 9.2–9.5 s per cycle, ≈ 50 % held** (the first cycles of the three chains: 9.23, 9.49, 9.40 s). A 3-minute chain ≈ 19 cycles; a cap of 20 re-claims ≈
20 × 9.3 s ≈ 3.1 min. Play's "Retry times remaining" counter stayed at 5 through all three 3-s first cycles (its one decrement, 06:31:33, was in the hint-only
period — `CAP-075-FINDINGS.md` §1); what Play does at 0 is ❓ (`TODO.md` §4).

**Item 3 fixtures** (`tshark -r CAP-075-btsnoop_hci2.log -Y "frame.number==N" -T fields -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data`, exit 0):
the re-claim's `Get` 2968 `08110000` → 2980 `0813000401e8e880` (TRANSPARENT; the app's last report before it: 2735 `…e8e840` ADAPTIVE, 06:30:36.129); 5090 → 5102
`0813000401e8e808` (ACTIVE; last 4893 `…40`); 5349 → 5361 `030300036464ff030300036464ff0813000401e8e880` (two battery messages then TRANSPARENT; last 5102
`…08`). Unprovoked on the app's claim: 3006 `0813000401e8e808` (ACTIVE after 2980's `80`) → stays `CHANGED_BY_BUDS`. A session's first read that differs from
the earlier session's mode: 3964 `0813000401e8e808` (the Connect of 06:31:55's snapshot; the app's last report before it 3538 `…20`) → stays `READ`. A `Get`
answer equal to the shown mode: 4434 `…08` after 4046 `…08` → `READ`.

### A.2 The code paths (read in full before the change)

- Re-claim chain: `BudsRepositoryImpl.onHeldClaimTaken` (`:533`), `startReclaimChain` (`:570`), `cancelReclaim` (`:575`), the `channelClosed` collector (`:733`),
  `reconcileHold` (`:459`), `releaseOrHold` (`:487`), `endHold` (`:501`), `HOLD_RECLAIM_DELAYS_MS` (`:1776`); new chains at `onAppVisible` (`:413`),
  `setAncTabShown` (`:421`), `connect` (`:965`), `setAncModeAfterGet` (`:1045`), `refreshAncMode()` (`:1118`), `refreshBattery` (`:1454`); cancel at the
  setting's collector (`:708`), `cancelClaimJobs` (`:1684`), the loss collector (`:777`), `disconnect` (`:1016`).
- Cause: `causeOfNotify` (`:363`), `emitAncMode` (`:352`), `getPending`/`setPendingMode` (`:265`/`:269`), the `Notify` branch of `handleRoutedFrame` (`:795`);
  `_ancModeCause` reset to `null` at every Connect (`:946`).
- UI: `AncScreen.kt` (`ANC_HOLD_HINT_TEXT` `:203`, `ancChannelTakenLine` `:215`, `ancChangedByBudsLine` `:236`, `ancDetailLines` `:170`), `SettingsMenu.kt`
  (`HOLD_RECLAIM_*` `:218–236`, `HoldReclaimCard` `:238`), `OpenControlNavHost.kt` (`OpenControlActions`, `OpenControlUiState`, `onAncTabShown` `:298`),
  `MainActivity.kt` (`:345–346`, `:400`, `:510`, `:556`).
- **The switch is never changed "with the tab shown":** the setting lives on gear → Settings, and `OpenControlNavHost` reports the noise-control tab as shown
  only with the settings menu closed (`:298`, `!inSettings && … == Routes.ANC`). Leaving Settings back to the noise-control tab is a re-entry —
  `setAncTabShown(true)` → `startReclaimChain()` + `reconcileHold` → one claim (`CAP-075-EVENT-NOTES.md` 06:32:26.5 → 06:32:31: claim #19 `SABM` 4408
  06:32:33.157 on return, without a re-entry by hand). The prompt's "re-entered the tab to get a claim" was this path; no extra code is needed.

### A.3 The design (the checkpoint decides the open values)

- **Item 1:** `HOLD_RECLAIM_DELAYS_MS` (3/6/12 s) → `HOLD_RECLAIM_STEP_MS = 3_000` (measured from the `channelClosed` event — today's measure, kept) and
  `HOLD_RECLAIM_CAP` (the checkpoint's value). `onHeldClaimTaken`: `attempt >= HOLD_RECLAIM_CAP` → the hint `ChannelTaken(takenAt, attempt)` + the "stopped"
  line; otherwise one delay of the step, then the existing re-checked claim. Log line form kept: "Message Stream re-claim N/CAP after a foreign close, 3000 ms
  later (ADR-062)". Everything else of ADR-062 unchanged.
- **Item 2:** `BudsRepository.reclaimAncChannel(): BudsResult<AncMode>` — one always-on line "Message Stream claim by the hint card's action (ADR-062 Update 2)",
  `startReclaimChain()`, then the existing claim with its `Get` (`refreshAncMode(GET_RESPONSE_TIMEOUT_MS)`): no `Set`, no battery read, no
  `SubscribeRuntimeInfo`. `:ui`: a `TextButton` under the hint line, enabled only while `Ready`; `OpenControlActions.onReclaimAncChannel`; `:app` wiring in the
  application scope.
- **Item 3:** `AncModeCause.CHANGED_SINCE_LAST_READ`; `causeOfNotify`: `answersGet` and a mode ≠ the shown one **and** this connection already has a report
  (`_ancModeCause.value != null` — reset at every Connect) → the new cause; otherwise as today. The (i) line `ancChangedSinceLastReadLine(readAt)`.
- **Item 4:** only labels, title, intro, (i), the hint line and the ANC (i) change; the enum names `HINT_ONLY`/`RECLAIM` and the DataStore key `hold_reclaim_mode`
  stay (`HoldReclaimMode.fromStored`, tested).
- Tests (`FakeBudsTransport`, virtual time, `CAP-075` bytes in a new `Cap075Fixtures`): the constant step and the cap, each cancel path, the action (one claim,
  no `Set`, the counter reset), the cause per case, `fromStored`; `:ui` literals. Mutations M1–M6 one at a time.

## B. Checkpoint (Phase B) — the maintainer's answers in chat 2026-10-13, verbatim (`AskUserQuestion`; the previews held the exact texts)

| Question (header) | Answer |
|---|---|
| "Cap" — the chain's cap with the constant 3-s step | **"20 re-claims (Recommended)"** — log "Message Stream re-claim 7/20 …", stop "… stopped after 20 re-claims — …", hint "…; this app took it back 20 times and stopped." |
| "Hint text" | **"Nieuwe tekst (Recommended)"** — hint only: "Another app took the noise-control channel at 06:31:33 — usually Google Play services' Fast Pair. A press-and-hold on a bud is not shown until this app has the channel again: tap Take it back again, a mode, Refresh, or pull down."; after the cap: "…Fast Pair; this app took it back 20 times and stopped. A press-and-hold …" |
| "Button" | **"Eén knop, beide modi (Recommended)"** — "Take it back again" in both modes; HINT_ONLY: one claim held until the next cut; RECLAIM: a new chain of 20; shown only with the hint, disabled while not `Ready` |
| "Cause line" | **"'…while this app did not have the channel' (Recommended)"** — "Changed since the app last saw it (read at 06:33:21): a press-and-hold on a bud or the Buds' own change, made while this app did not have the channel." |
| "Card name" | **"Noise control with Google Play services (Recommended)"** |
| "Options" | **"Titel + uitlegregel (Recommended)"** — intro "The Buds report a press-and-hold on a bud to one app at a time. Choose what fits this phone:"; (•) "This phone has no Google Play services" / "Nothing else uses the noise-control channel. If another app takes it anyway, the noise-control tab says so."; ( ) "This phone has Google Play services" / "While the noise-control tab is open, take the channel back from Play services' Fast Pair 3 seconds after each time it takes it, so a press-and-hold shows about half of the time." |
| "Card (i)" | **"Deze vijf regels (Recommended)"** (the five lines of the preview, built verbatim in `SettingsMenu.kt`) |
| "ANC (i)" | **"Nieuwe zin (Recommended)"** — "While this tab is open the app keeps the Buds' noise-control channel, so a press-and-hold on a bud shows here; it is released 1.5 seconds after you leave the tab. On a phone with Google Play services, Play's Fast Pair takes the channel back about 5 seconds later; what this app does then is set on the Settings tab under Noise control with Google Play services." |
| "Play check" — the optional "Google Play services: found / not found" line | **"Ja, als informatieregel"** (my recommendation was "Nee") |
| "ADR form" | **"ADR-062 Update 2 (Recommended)"** with the preview's text |
| "Version" | **"1.2.2 (10202) (Recommended)"** |
| "CAP-076" | **"Beide gebruikers, als CAP-075 (Recommended)"** |
| "ADR-063" — the package check as its own decision (a new `<queries>` manifest entry) | **"ADR-063 zoals getoond (Recommended)"** with the preview's text (three states: found / found but disabled / not found; information only; the query in `:app` on every resume; a failure shows no line) |
| "AGENTS.md" — a dated note in §1 | **"Ja, gedateerde notitie in §1"** with the preview's text |

Not asked (no decision left): the chain restart "when the user switches the setting with the tab shown" — the setting lives on gear → Settings, where the tab
is never "shown"; the return to the tab is a re-entry that already starts a new chain and claims (A.2). The first step stays measured from the close event
(as today) — stated in A.3 and in the Update 2 text ("measured from the close, as before").


## C. What was built (Phase C)

| Module | File | Change |
|---|---|---|
| `:domain` | `AncModeCause.kt` | `CHANGED_SINCE_LAST_READ` (documented with `CAP-075` 2980/5102/5361) |
| `:domain` | `HoldReclaimMode.kt` | docs: the new labels, 3 s / 20; `PlayServicesPresence` (FOUND, FOUND_DISABLED, NOT_FOUND) — ADR-063; the enum names unchanged |
| `:domain` | `BudsRepository.kt` | `suspend fun reclaimAncChannel(): BudsResult<AncMode>`; docs of `setAncTabShown`, `ancChannelTaken`, `ancModeCause` |
| `:data` | `BudsRepositoryImpl.kt` | `HOLD_RECLAIM_STEP_MS = 3_000`, `HOLD_RECLAIM_CAP = 20` (replacing `HOLD_RECLAIM_DELAYS_MS`); `onHeldClaimTaken` uses them (log "re-claim N/20 …", stop line names Take it back again); `reclaimAncChannel()` (log line, `startReclaimChain()`, `refreshAncMode(GET_RESPONSE_TIMEOUT_MS)`); `causeOfNotify`: `answersGet && _ancModeCause.value != null && _ancMode.value != null && mode != _ancMode.value` → `CHANGED_SINCE_LAST_READ` |
| `:data` | `settings/HoldReclaimSettingsStore.kt` | doc (the name) — the key `hold_reclaim_mode` unchanged |
| `:ui` | `SettingsMenu.kt` | the card: title, intro, `holdReclaimLabel` + `holdReclaimSupportingText` (two-line radio rows), the five (i) lines, `playServicesPresenceLine`; `SettingsMenuScreen(playServicesPresence = …)` |
| `:ui` | `AncScreen.kt` | `ANC_HOLD_HINT_TEXT`, `ancChannelTakenLine` (the new hint), `TAKE_IT_BACK_AGAIN_LABEL` + the `TextButton` under the hint (enabled while `Ready`), `ancChangedSinceLastReadLine`, `ancDetailLines`; `AncScreen(onReclaimAncChannel = …)` |
| `:ui` | `OpenControlNavHost.kt` | `OpenControlActions.onReclaimAncChannel`, `OpenControlUiState.playServicesPresence`, the wiring |
| `:app` | `PlayServicesProbe.kt` (new) | `getApplicationInfo("com.google.android.gms", ApplicationInfoFlags.of(0))` → FOUND / FOUND_DISABLED; `NameNotFoundException` → NOT_FOUND; another `RuntimeException` → `null` + a payload-free log line |
| `:app` | `MainActivity.kt` | the probe on every `ON_RESUME` (logged when the answer changes), `onReclaimAncChannel` → `budsRepository.reclaimAncChannel()` in the application scope |
| `:app` | `AndroidManifest.xml` | `<queries><package android:name="com.google.android.gms" /></queries>` with the ADR-063 comment |
| `:app` | `build.gradle.kts` | `versionCode = 10202`, `versionName = "1.2.2"` |

The log line in the hint-only mode ("Noise-control channel taken by another client: the hint is shown, no re-claim (ADR-062, hint only)") is kept as it was, so
exports of 1.2.1 and 1.2.2 compare line for line.

## D. Tests, mutations, compliance (Phase D)

**Tests added or changed** (real bytes from `CAP-075-btsnoop_hci2.log` in the new `Cap075Fixtures`, each with its frame and command; the timing constants are the
frames' own):

- `BudsRepositoryImplTest`: **rewritten** `take it back - re-claims 3 s after every cut up to 20, then stops with the hint` (was `… 3, 6 and 12 s …` — the only
  test of `ai-sessions/0087` whose delays changed; the 4.749-s re-held life is H2 2502 → 2570); **new** `take it back - the second re-claim follows its cut by 3
  s` (M1), `take it back again - one claim, no Set, a new chain`, `take it back again - hint only claims once and never re-claims`, `take it back again - no
  session, no claim`, `a differing Get answer is changed since the last read` (2735 → 2980, 3006 stays the Buds', 4893 → 5102, 5361, the same mode again →
  `READ`), `a session's first read stays a reading` (3538 → Connect → 3964), `the app's own Set after a differing Get stays set by this app`. The other ADR-061 /
  ADR-062 tests of `ai-sessions/0084`/`0087` pass unchanged (their 3-s first step is the new constant step).
- `HoldReclaimSettingsStoreTest`: `fromStored("HINT_ONLY")`; **new** `a choice stored by 1_2_1 keeps its meaning after the update` (a file written with the raw key
  `hold_reclaim_mode` and the strings `RECLAIM` / `HINT_ONLY`, read by this build's store).
- `AncScreenTest`: the ANC (i) sentence, the hint lines (hint only; 20; once) as literals, Take it back again (shown with the hint and handed over; absent without
  it; disabled while not `Ready`), the changed-since line (on screen and in `ancDetailLines`, with and without a time) and its absence for the other causes.
- `SettingsMenuTest`: the card's position, title, intro, two-line labels, the five (i) lines as literals, the stored names `HINT_ONLY`/`RECLAIM` and `fromStored`;
  the selected row per stored value; ADR-063's three lines and none for `null`; the line shown without changing the choice.

**Mutation checks** (`--max-workers=2`, one at a time, each on `BudsRepositoryImpl.kt` applied by a scratch script, `:data:testDebugUnitTest --tests
'*BudsRepositoryImplTest*'`, the file restored from a copy and compared with `diff -q` after each — "restored"; `grep -c "M[1-6]"` on the file → 0 afterwards):

| # | Mutation | Result | Killed by |
|---|---|---|---|
| M1 | the second re-claim after 6 s (`if (attempt == 1) 6_000L`) | gradle exit 1, killed | the M1 test, the 20-step test, the action test |
| M2 | the chain continues past the cap (`HOLD_RECLAIM_CAP + 1`) | exit 1, killed | the 20-step test, the action test |
| M3 | Take it back again writes a `Set` (`setAncModeAfterGet` instead of `refreshAncMode`) | exit 1, killed | both action tests (the sent frames) |
| M4 | Take it back again does not reset the counter (no `startReclaimChain()`) | exit 1, killed | the action test (no "re-claim 1/20" after the next cut) |
| M5 | a differing `Get` answer labelled `READ` (the new branch removed) | exit 1, killed | `a differing Get answer is changed since the last read` |
| M6 | a re-claim while the tab is not shown (no cancel on leaving the tab or the app, no `holdWanted` in the re-check) | exit 1, killed | `take it back - leaving the tab or the app during the delay claims nothing` |

**Compliance (🟢, each checked):** no new permission (`grep -n uses-permission android/app/src/main/AndroidManifest.xml` → the same four; `<queries>` is not a
permission — ADR-063); no `INTERNET` (the only match of `grep -c INTERNET` is the header comment "No INTERNET permission anywhere"); no dependency (no
`*.gradle.kts`/`libs.versions.toml` change except `versionCode`/`versionName`; `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` before and
after the version bump: identical — `diff` of the two listings empty; the first listing was taken after the code change (no build file had changed) because none was saved
before it); the AGPL header on every new or changed Kotlin file (checked by `head -5 | grep SPDX` on all 17); no hidden API
(`PackageManager.getApplicationInfo` with `ApplicationInfoFlags` is public API 33+); no import of `com.google.android.gms.*` (the package name is one string
constant in `PlayServicesProbe`); the new always-on lines are payload-free and carry no address or serial ("Message Stream claim by the hint card's action
(ADR-062 Update 2)", "re-claim N/20 …", "Google Play services on this phone: found (ADR-063, information only)"); no new wire request (the action and the
re-claims call the existing `refreshAncMode`; the `:data` tests assert the sent frames: only `08 11 00 00`); the stored setting of 1.2.1 readable unchanged (the
1.2.1-file test).

## Files read (in full / in part)

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md` (ADR-001 … ADR-062 with every Update), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, the
  prompt; `CAP-075-FINDINGS.md`; the CAP-075 skeleton EVENT-NOTES of `3f373c2` (the layout); `HoldReclaimMode.kt`, `AncModeCause.kt`, `AncScreen.kt`,
  `Cap074Fixtures.kt`, `HoldReclaimSettingsStoreTest.kt`.
- **In part (the sections the build touches, each section in full before it was changed):** `ARCHITECTURE.md` §2.4, §3.1 (start), §3.2, §5a's ANC rows, §6.0b
  (the 2026-10-10/12/13 paragraphs), §9; `PROTOCOL.md` §4.1's notes of 2026-10-07/12/13 and the Set/ACK tally; `BudsRepositoryImpl.kt` (the header, `:100–590`,
  `:695–830`, `:935–1165`, `:1435–1465`, `:1515–1600`, `:1670–1832`); `BudsRepository.kt` (`:30–120`, `:210–305`); `SettingsMenu.kt` (`:75–275`);
  `OpenControlNavHost.kt` (the actions, the state, the ANC and Settings call sites, `:295–300`); `MainActivity.kt` (imports, `:395–430`, `:500–560`);
  `BudsRepositoryImplTest.kt` (the builder `:100–175`, `:2990–3493`); `AncScreenTest.kt` (`:20–75`, `:150–235`); `SettingsMenuTest.kt` (`:390–475`);
  `FakeBudsTransport.kt` (the fields); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BK, the index rows); `APP_TESTPLAN.md` (the header, §X, the Summary); `CHANGELOG.md`
  (top, `[1.2.1]`); `README.md` (the release paragraphs, "Current state"); `id_registry.csv` (the ADR/CAP rows); `ai-sessions/INDEX.md` (the last rows);
  `CAP-075-EVENT-NOTES.md` (06:32:2x–06:33:3x and the step table); `CAP-075` E2 and the logcat extract (the lines quoted in §A.1).
- **Not read** (named by the prompt, judged not needed after the above): `RELEASING.md`, `0087`/`0088` PROMPT/RESULT files beyond the INDEX rows and the
  CAP-075 analysis they produced, `CAP-074`/`CAP-073` FINDINGS (their numbers are restated, with frames, in `CAP-075-FINDINGS.md` §1/§8 and ADR-062), `PROTOCOL.md`
  §2.3 and §8. Deferred (below).

## G. The maintainer's next steps (instructions — not executed)

1. Review; answer the commit question. After a yes the session commits per concern (Conventional Commits), rebases on `origin/main`, pushes
   `feature/channel-control` without force.
2. Open the pull request: `gh pr create --base main --fill` (with `RELEASING.md`'s Release checklist); both CI workflows green; merge (yours).
3. **B1** `scripts/release.sh 1.2.2` on the merged tip; **B2**; **B3** `cp -a dist/1.2.2 ~/opencontrol-1.2.2-tested`.
4. **C1–C2** the `CAP-076` run — captures/CAP-076-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BL/CAP-076-EVENT-NOTES.md (both users; P0 with Play's *Nearby devices*
   permission on film; P6 the log viewer right after BL-5's chain; the APK pull).
5. A CAPTURE session for C3/C4 (the analysis and the verdict), then D1–D4 / E1–E4.

## Deferred documentation (each also in `TODO.md`)

- `TODO.md` §1: "**M — the 1.2.2 release steps** (prepared in `ai-sessions/0089`, branch `feature/channel-control`): …" — the release path above.
- `TODO.md` §5: "The noise-control channel in the Play user — after `ai-sessions/0089` …: still later — P3 a shorter attempt-2 latency …; P5 the mode on the
  app's own channel …; Whether 20 re-claims in a row drive Play's "Retry times remaining" to 0 and what Play does then is ❓ (§4; `CAP-076` P6 watches it)."
- `TODO.md` §2: the ADR-061/062 untested branches now planned as `CAP-076` BL-9 … BL-11 and BL-22 … BL-24 (the item's added sentence).
- `TODO.md` §3: the `CAP-076` row; the standing P0 item ("`CAP-076`'s P0 asks again").
- Not read in full by this session (prompt §0 asked for them): `RELEASING.md`, `ai-sessions/0087`/`0088` PROMPT and RESULT, `CAP-074-FINDINGS.md` §4/§8/§9,
  `CAP-073-FINDINGS.md` §4, `PROTOCOL.md` §2.3/§8 — none of their content was changed; the facts used are re-derived from `CAP-075` (§A.1). Recorded here for the
  next reading pass — `TODO.md` §6: "Reading owed by `ai-sessions/0089` (its RESULT "Files read"): …".

## Commits

The maintainer's answer in chat 2026-10-13 (`AskUserQuestion` "Commit" → *"Ja: commits per onderwerp + push (Recommended)"*). On `feature/channel-control`:

- `e60fb7c` feat(app): constant 3-s re-claim up to 20, Take it back again, cause line, renamed card, Play-services line (ADR-062 Update 2, ADR-063) — with
  `ARCHITECTURE.md`
- `73a9738` test: CAP-075 fixtures — the constant step, the cap of 20, Take it back again, the cause, the 1.2.1 setting
- `8a5085d` docs: ADR-062 Update 2, ADR-063, the AGENTS.md §1 note, the 1.2.2 texts
- `4d33759` docs(capture): CAP-076 (Group BL) skeleton — the 1.2.2 run in both GrapheneOS users; APP_TESTPLAN §Y
- this file and the `ai-sessions/INDEX.md` row: the commit after these (its hash is the next session's to back-fill, `AI_SESSION_LOG_PROCEDURE.md` §4b).

Pushed without force after `git fetch` and a rebase check. **Continuation in chat 2026-10-13/14:** the session-log commit is `bfd7891` (docs(ai-sessions): 0089
RESULT and INDEX row) — also the build commit of `scripts/release.sh 1.2.2` (B1); PR #49 opened (the maintainer's yes), CI green, B1–B3 by the maintainer;
merged with merge commit `5b204ea` (`gh pr merge --merge`); tag `v1.2.2` (SSH-signed) on `bfd7891`; the release published 2026-10-14 from
`~/opencontrol-1.2.2-tested` with "New in 1.2.2" and "Known issues" paragraphs added to the notes; `CAP-076` not run — the maintainer's choice in chat
2026-10-14 ("Ik heb versie 1.2.2 getest zonder CAP-076 uit te voeren en het werkt"). E1–E4: the pull request of `release/1.2.2-after`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0089_FEATURE_RESULT_2026_10_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0089_FEATURE_RESULT_2026_10_13
