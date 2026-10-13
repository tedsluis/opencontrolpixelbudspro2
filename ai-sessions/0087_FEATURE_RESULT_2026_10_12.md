# 0087_FEATURE_RESULT_2026_10_12.md — The Play-user hold fix: a Settings switch between "hint only, no re-claim" and "re-claim after a foreign close with a back-off"

**Number:** 0087
**Category:** FEATURE
**Date:** 2026-10-12
**Title:** Build the maintainer's chosen fix for `CAP-074` (`ai-sessions/0086`): a switch on the Settings tab between (1) "hint only, no re-claim" and (2) "re-claim
after a foreign close while the noise-control tab is shown, with a back-off"; the hint, the corrected ANC (i), ADR-062, tests with `FakeBudsTransport`, mutation
checks, the `CAP-075` (Group BK) skeleton, the documents, the release preparation (no tag, no publication)
**Status:** complete — and released: PR #39 merged 2026-10-12 (its merge commit `31ee183` is the build commit of `scripts/release.sh 1.2.1`), `CAP-075` run by the maintainer and analysed by `ai-sessions/0088` (verdict "no heavy issue"), tag `v1.2.1` on `31ee183` and the release published 2026-10-13 (see Commits)

## Summary (plain language)

**What 1.2.1 does differently** (versionCode 10201; nothing published, no tag, no release — the maintainer's own acts):

1. **A new setting, gear → Settings → "Noise-control channel"** (`DECISIONS.md` ADR-062), between Dark mode and Case sounds, with an (i). It only matters on a phone
   with Google Play services, where Play's Fast Pair takes the noise-control channel back about 5 seconds after this app opened it on the noise-control tab
   (`CAP-074`); without Play services nothing contends and both choices behave the same.
   - **"Show a hint, do not take it back"** (the default): what 1.2.0 did, plus the card tells you. When the channel is taken, the noise-control card shows "The
     noise-control channel was taken by another app at HH:MM:SS (for example Google Play services' Fast Pair). A change made on a bud is not shown until you tap a
     mode, Refresh, or pull down." — below the mode buttons and in the (i). A tap, Refresh, a pull-down or a return to the tab claims the channel again and clears
     the line. Nothing re-claims by itself.
   - **"Take it back while the noise-control tab is open (may fight that app)"**: 3 seconds after the channel is taken the app claims it again (the existing claim
     with its `Get` — nothing new on the wire), then 6 and 12 seconds after the next two cuts, three times per visit of the tab; then it stops and shows the hint with
     "; the app took it back 3 times and stopped". A press-and-hold then shows about 5 seconds of every 9 while the chain runs (≈ 37 s per visit), at the price of a tug
     of war with Play's Fast Pair. A tap, Refresh, a pull or a re-entry of the tab starts a new chain; leaving the tab or the app, a session loss, a Disconnect or
     switching back to the hint cancels a pending re-claim. Never in the background.
2. **The noise-control (i) is corrected:** it no longer says another app "cannot use" the channel while the tab is open — it loses it to this app and may take it
   back about 5 seconds later; the setting decides what happens then.
3. **Nothing else changes:** the ten ADR-061 hold tests of `ai-sessions/0084` run unchanged under the default mode; every request on the wire is the same.

**What you do next** (not done by this session): answer the commit question in the chat; then push and open the pull request, check CI, `scripts/release.sh
1.2.1` on the branch tip (B1–B3), install over the 1.2.0 on the phone in both users and run `CAP-075` (≤ 20 min, the skeleton under `captures/`), then a CAPTURE
session for the verdict. Steps in "Next steps for the maintainer" below.

One process note: nothing was deleted, renamed or published; every mutation was reverted and checked with `cmp`; one mutation run (M1) restored its file through a
wrong relative path and was restored by hand at once (`cmp` equal) before the next run — recorded in §D.

## Progress

- **Phase 0 — done.** `git log -1`: `8a0db34 Merge pull request #37 from tedsluis/capture/cap-074`. `git status --short`: `?? android/domain/bin/` (build
  output, never staged). `git fetch` + `git log --oneline HEAD..origin/main`: empty (a new remote branch `bot/docs-sidebar` only). Branch
  **`feature/hold-reclaim-switch`** created from `main` at `8a0db34`. **Baseline gate (before any change), exit 0:** `cd android && ./gradlew assembleDebug
  testDebugUnitTest test lint --max-workers=2` → BUILD SUCCESSFUL; tests (JUnit XML): `:data` 1655 (debug) + 1655 (release), `:domain` 42, `:hardware` 64 + 64,
  `:ui` 90 + 90 — 0 failures (= `ai-sessions/0084`'s final counts). `python3 scripts/lint_docs.py` → exit 1 with five findings, all "`CAP-075` / `ADR-062` not
  in id_registry.csv" (the IDs this session registers in Phases E/F; the 0087 prompt and the INDEX row already cite them) — nothing else.
- **Reading (done):** see "Files read".
- **Phase A — done** (§A below; no code change).
- **Phase B — done** (§B below, the answers verbatim).
- **Phase C — built and tested (targeted).** Files: `:domain` `HoldReclaimMode.kt` (new: the enum + `ChannelTaken`), `BudsRepository.kt` (`ancChannelTaken`,
  the `setAncTabShown` KDoc); `:data` `settings/HoldReclaimSettingsStore.kt` (new), `BudsRepositoryImpl.kt` (the `holdReclaimMode` flow, `onHeldClaimTaken`,
  `startReclaimChain`/`cancelReclaim`, the `channelClosed` collector, the resets at Connect/Disconnect/loss/the user's claims, `HOLD_RECLAIM_DELAYS_MS`); `:ui`
  `OpenControlNavHost.kt` (`onHoldReclaimModeChanged`, `ancChannelTaken`, `holdReclaimMode`), `AncScreen.kt` (`channelTaken`, `ancChannelTakenLine`, the corrected
  `ANC_HOLD_HINT_TEXT`), `SettingsMenu.kt` (`HoldReclaimCard`, the texts); `:app` `RepositoryModule.kt`, `MainActivity.kt`. Tests: `Cap074Fixtures.kt` (new),
  `BudsRepositoryImplTest.kt` (+7: hint only ×2, take it back ×4, the switch ×1; `buildRepository`/`holdingRepository` take the mode flow), `settings/HoldReclaimSettingsStoreTest.kt`
  (new, 2: the file round trip on a temporary file with a second instance; `fromStored`), `AncScreenTest.kt` (+2, the ADR-061 literal updated), `SettingsMenuTest.kt`
  (+3; two order tests now read unclipped bounds in one scroll state — a `performScrollTo` between readings moved every node once a card was added above).
  Targeted runs green: `BudsRepositoryImplTest` 173, `HoldReclaimSettingsStoreTest` 2, `AncScreenTest` 15, `SettingsMenuTest` 20, `TabRestoreTest` 7.
  `ARCHITECTURE.md` §2 (the Data Layer note), §2.4, §3.2, §5a, §6.0b, §7, §9 updated with the code.
- **Phase D — done** (§D: the full gate green; M1–M7 killed; compliance clean).
- **Phase E — done** (§E: the `CAP-075` skeleton, the registry row, the Group BK section, the planned index row, `APP_TESTPLAN.md` §X).
- **Phase F — done** (§F: ADR-062 and the ADR-061 Update, `id_registry.csv`, `CHANGELOG.md`, `README.md`, `scripts/release_notes.template`, `PROJECT.md`, `TODO.md`,
  `android/app/build.gradle.kts` 1.2.1 / 10201; `ensure_footers` and `lint_docs` exit 0).
- **Phase G — done:** the final gate after the version bump (gate_final.log) **exit 0** — `:data` 1664 + 1664, `:domain` 42, `:hardware` 64 + 64, `:ui` 95 + 95, 0 failures; lint 0 errors. The commit question in the chat.

## B. Phase B — the checkpoint (chat, 2026-10-12, `AskUserQuestion`, answers verbatim)

1. "Re-claim delay" → *"3 s, 6 s, 12 s, then stop (Recommended)"* — `HOLD_RECLAIM_DELAYS_MS = [3000, 6000, 12000]`, cap 3 per tab view (the preview of §A.3's
   candidate (b)).
2. "Default mode" → *"Hint only (Recommended)"* — `HoldReclaimMode.DEFAULT = HINT_ONLY`.
3. "Switch card" → *"After Dark mode, as previewed (Recommended)"* — Dark mode · Noise-control channel · Case sounds · Use different Buds; the card title
   "Noise-control channel", the intro "When another app takes the channel:", the labels "Show a hint, do not take it back" / "Take it back while the
   noise-control tab is open (may fight that app)", the four (i) lines of the preview.
4. "Hint texts" → *"Approve as previewed (Recommended)"* — the mode-1 hint, the mode-2 hint after the cap ("; the app took it back N times and stopped"), the
   corrected `ANC_HOLD_HINT_TEXT`; shown below the mode buttons and in the (i); cleared when the app holds the channel again, at Connect, Disconnect and a loss.
5. "ADR-062" → *"Accept ADR-062 as drafted (Recommended)"* — the preview's text, written in Phase F.
6. "ADR-061 Update" → *"Add the pointer Update (Recommended)"* — the preview's text.
7. "Version" → *"1.2.1 (versionCode 10201) (Recommended)"* — with the `CHANGELOG.md`, `README.md` and release-notes texts of the preview.
8. "CAP-075 scope" → *"Both users (Recommended)"*.
- Scratchpad: `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/c1322bcd-0f50-4b71-8f11-1ef339723296/scratchpad/` (ms_frames.tsv, claims.py,
  claims_out.txt, gate_baseline.log, lint_docs_baseline.log). Re-create by re-running the commands named here if gone.

## A. Phase A — evidence and design (no code change)

### A.1 `CAP-074`'s numbers, re-derived (handle `0x000b`; "B" = `CAP-074-btsnoop_hci.log`)

Command: `tshark -r B -Y "bthci_acl.chandle==0x000b && btrfcomm && (btrfcomm.dlci==4 || btrfcomm.dlci==5)" -T fields -e frame.number -e frame.time_epoch
-e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e data.data` (exit 0, 1,418 frames; positive control `… && frame.number==14500` → the
app's `SABM` 06:40:51.246), fed to claims.py (scratchpad; a claim = the phone's `SABM`, its owner = its first phone payload — `08 11` the app, `03 08` Play
services — its close = the next phone `DISC`). From frame 13400 on (06:38:42, the run): **31 claims, 16 the app's, 15 Play's** (the 06:38 claims before 13450 are
not in this window; `CAP-074-FINDINGS.md` §1 counts 47 over the whole log — the same inventory, the run's part re-derived here).

| What | Re-derived (this session) | `CAP-074-FINDINGS.md` §4 |
|---|---|---|
| Play's loss → the cut of the app's claim (the twelve cuts, 11 following a collision directly) | 5.22, 5.54, 5.49, 5.25, 5.71, 5.24, 5.29, 5.40, 5.23, 5.64, 5.48 s (app `SABM` 14500, 14732, 15051, 15500, 15822, 16124, 16578, 16867, 17188, 17513, 17807 → their `DISC`) | item 3: the same eleven values |
| The app's `SABM` → its cut, claims opened on attempt 2 | 4.68, 4.92, 4.82, 5.13, 4.66, 4.85, 4.66, 5.04, 4.88 s; on attempt 1 into a free channel 1.03 s (15500), 1.61 s (16578), 0.38 s (14845) | item 3 |
| The twelfth cut (14890, 06:41:06.569) | 2.18 s after the previous cut 14810 (06:41:04.385) — Play's **retry** into a channel the app had re-taken on attempt 1 | item 5 |
| Play's retry after a cut | 2.29, 2.42, 2.24, 2.38, 2.55, 2.38, 2.47, 2.54, 2.52, 2.41 s (Play `SABM` 14624, 15152, 15592, 15931, 16245, 16665, 16977, 17303, 17618, 17915); **4.44 s** once (14947, after the retry that collided at 14890) | item 5 (2.2–2.6 s; the 4.44 s named) |
| Play's re-open after the app's own release | 1.64 s (14152 after 14126), 2.96 s (14346 after 14314) | §1's table |

So the design numbers hold as written: **Play recovers 5.2–5.7 s after losing the channel** (and cuts whoever holds it), **retries 2.2–2.6 s after a failed
recovery** (🟡 for other Play versions: `ai-sessions/0040` 2.7–5.0 s, `CAP-059` 1.7–3.1 s), and — one sample — waits 4.4 s for the retry after a retry that
collided. The fixture bytes for the tests (`tshark -r B -Y "frame.number==N" -T fields -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type
-e data.data`, exit 0): B 15853 06:42:30.868 B→P `0813000401e8e840` (the snapshot's answer: ADAPTIVE, Settable `e8`), **B 15860 06:42:33.204 B→P
`0813000401e8e880` (unprovoked, TRANSPARENT — the one hold shown after that Connect)**, B 14525 06:40:51.351 `0813000401e8e840`, B 14552 06:40:54.639
`0813000401e8e840` (the tap's `Get` answered), B 14553 06:40:54.645 P→B `0812001401e8e880` + 16 × `00` (the `Set` TRANSPARENT), B 14555 06:40:54.727
`ff010006081201e8e880` (its ACK), B 14556 06:40:54.753 `0813000401e8e880`, B 15957 06:42:38.734 `0813000401e8e808` (Play's `Get` answered — already NC). The cut
itself has no payload: B 15909 06:42:35.842 is the phone's `DISC` (`0x43`) 5.13 s after the app's `SABM` B 15822 (06:42:30.709); the export's lines for it are
E 321 "RFCOMM on-demand channel 0x04 closed (IOException: bt socket closed, read return: -1)" and E 322 "Message Stream hold ended: the channel was closed,
not by this app" (06:42:35.972/.974).

### A.2 The code paths (read in full) and what changes

- `BudsRepositoryImpl.kt`: the `transport.channelClosed` collector (`:638–647`) resets the codec, clears the Model ID and calls `endHold(HoldEnd.CHANNEL_CLOSED)`
  — the "no re-claim by itself" of ADR-061 (the M5 mutant of `ai-sessions/0084` was `reconcileHold(null)` there). `reconcileHold(endReason)` (`:449–470`) is
  the one entry point for "claim if wanted": under `claimMutex`, nothing when the hold is not wanted; keep an open claim; otherwise `refreshAncMode(GET_RESPONSE_TIMEOUT_MS)`
  when `Ready` — the existing claim code with its `Get` (`08 11 00 00`), whose `finally` (`releaseOrHold`, `:477–487`) keeps the claim and logs "hold started".
  `holdWanted` = `ancTabShown && appVisible` (`:422`); `cancelClaimJobs` (`:1564–1575`) cancels the hold job on a loss and at Disconnect; `endHold` (`:490–494`)
  logs the reason. `debugModeEnabled: Flow<Boolean>` is injected by `RepositoryModule` (`:87`) and collected into a `@Volatile` snapshot (`:613–615`) — the
  pattern for the switch.
- `RfcommBudsTransport.kt`: a foreign close of DLCI 0x04 reaches the app as the reader's `read return: -1` (`:229–236` EOF, `:238–242` `IOException`) →
  `reportChannelClosed` (`:261–270`) → `channelClosed`; a deliberate `closeChannel` (`:347–354`) announces nothing. Unchanged.
- `SessionReopener.kt`: unchanged — after a session loss the ADR-044 re-open runs the Connect sequence, whose snapshot claim becomes the hold (ADR-061, the
  maintainer's "Keep it as the hold"); a pending re-claim is cancelled by `cancelClaimJobs` at the loss, so the re-open never races it.
- `AncScreen.kt`: `ancDetailLines` (`:165–181`) ends with `ANC_HOLD_HINT_TEXT` (`:187–189`, to be corrected); the not-allowed line is the pattern for a line on
  the main surface (`:119`). `ConnectionBanner.kt`'s `MESSAGE_STREAM_HINT_TEXT` (Find) stays.
- `SettingsMenu.kt`: `SettingsTabContent` (`:144–197`) — Dark mode (a radio group, `:161–175`), the Case sounds `SettingsCard` (`:178–190`), "Use different Buds".
  The pattern for the switch: a card with `CardTitle` (`Details.kt:108`) and a radio group as Dark mode's.
- `settings/`: `SettingsDataStore.kt` (one file, `opencontrol_settings`), `DarkModeSettingsStore.kt` (a `stringPreferencesKey`, the enum name, `fromStored`) —
  the pattern for one more key. `RepositoryModule.kt` provides the stores; `MainActivity.kt` collects `darkModeSettingsStore.darkMode` (`:391`) and wires
  `onDarkModeChanged` (`:538`) — the pattern for the switch's state and action; `onAncTabShown` (`:543`).
- `BudsRepositoryImplTest.kt`: `holdingRepository()` (`:3080–3094`) and the ten hold tests (`:3096–3268`); `buildRepository` (`:113–146`) takes no mode flow yet;
  `settle()` = `repeat(6) { runCurrent() }` (`:258`). `FakeBudsTransport.emitChannelClosed` (`:108–111`) is the foreign close.

### A.3 The design (proposed; the open points are the checkpoint's)

**The switch's value** — `:domain` `HoldReclaimMode { HINT_ONLY, RECLAIM }` (`DEFAULT` = the checkpoint's answer; `fromStored(name)` as `DarkMode`). Stored by a
new `HoldReclaimSettingsStore` (`:data/settings`, the key `hold_reclaim_mode` in the existing `opencontrol_settings` file — plain preferences, non-sensitive, no new
dependency), constructed from a `DataStore<Preferences>` with a `Context` convenience constructor, so its round trip is testable on the JVM with
`PreferenceDataStoreFactory` (no Robolectric in `:data`). `RepositoryModule` provides it and injects `holdReclaimMode: Flow<HoldReclaimMode>` into
`BudsRepositoryImpl` as `debugModeEnabled` is; `MainActivity` collects it into `OpenControlUiState.holdReclaimMode` and wires `OpenControlActions.onHoldReclaimModeChanged`.

**The hint state** — `:domain` `ChannelTaken(val atMillis: Long, val reclaimAttempts: Int)`; `BudsRepository.ancChannelTaken: Flow<ChannelTaken?>`. Set in the
`channelClosed` collector when the hold **was open** at the close (`holdOpen` before `endHold`): in mode 1 at once with the close's time (`reclaimAttempts = 0`); in
mode 2 only when the back-off is exhausted (`reclaimAttempts = the cap`). Cleared when a hold starts again (`releaseOrHold`'s "hold started" — a tap, a pull, a
re-entry, the re-claim), at Connect, at Disconnect and at a session loss. Shown on the noise-control card's main surface **and** in its (i) (the checkpoint's text),
never on the tile or in the notification.

**Mode 2's re-claim** — in the `channelClosed` collector, after `endHold(CHANNEL_CLOSED)`, when the hold was open and the mode is `RECLAIM`: `reclaimJob =
scope.launch { delay(HOLD_RECLAIM_DELAYS_MS[attempt]); reconcileHold(null) }` with one always-on, payload-free line per re-claim ("Message Stream re-claim N/M
after a foreign close, D ms later (ADR-062)"); `attempt` counts per tab view and is reset by a re-entry of the tab (`setAncTabShown(true)`), the app's return
(`onAppVisible(true)`), a user claim (a tap, Refresh, a pull, *Refresh battery*) and a Connect; at the cap: no job, the hint state, one line ("Message Stream re-claim
stopped after M foreign closes — a tap, a pull or re-entering the tab claims again (ADR-062)"). `reconcileHold(null)` already checks, under `claimMutex`, that the
hold is still wanted (the tab shown **and** the app visible), that the channel is closed and that the session is `Ready` — otherwise nothing is claimed. The job is
cancelled by `setAncTabShown(false)`, `onAppVisible(false)`, `cancelClaimJobs` (a loss, Disconnect) and by a switch to mode 1 (the mode collector). Nothing new on
the wire: the re-claim is `refreshAncMode`'s claim — `SABM`, `08 11 00 00`, the `DISC` of the usual release when the tab is left. Not a loop: every chain has the cap
and ends with the tab, the app's visibility, the session or the switch (ADR-032 item 2, ADR-044).

**The candidates for the delay (the wire consequence from §A.1; every number is this Play version's, 🟡 for others):**

| Candidate | What happens on the wire | The app holds the channel | Frames per minute while the tab is open |
|---|---|---|---|
| (a) at once (0.5 s, then 1, 2, 4 s) | the channel is still free (Play's retry comes at 2.2–2.6 s): the app's attempt 1 connects; Play's retry collides and the stack cuts the app ≈ 1.7–2.1 s later (as 14845 → 14890); Play's next retry came 4.4 s later (one sample) — a 2–5 s ping-pong that rests on Play's retry back-off | ≈ 2 s of every 3–5 s (≈ 50 %), the windows short | ≈ 12–20 `SABM`/`DISC` pairs |
| **(b) 3.0 s, then 6 s, then 12 s, then stop (cap 3)** | Play's retry has succeeded (2.2–2.6 s); the app's attempt 1 collides and kills Play's socket (56–363 ms), attempt 2 connects 0.4 s later; Play recovers 5.2–5.7 s after its loss and cuts the app; the next step | ≈ 5 s of every 8.5 s during the chain (≈ 60 %); the chain ≈ 37 s (3 + 5.3 + 6 + 5.3 + 12 + 5.3), then the hint until the next tap, pull or re-entry | ≈ 7 pairs per minute during the chain, then 0 |
| (c) 3.0 s every time, cap 10 | as (b), the ≈ 8.5-s cycle ten times | ≈ 60 % for ≈ 85 s, then the hint | ≈ 7 pairs per minute during the chain |
| (d) 3.0 s every time, no cap | the ≈ 8.5-s cycle for as long as the tab is open | ≈ 60 % | ≈ 7 pairs per minute — **not offered**: ADR-032's "no reconnect loop"/the prompt's cap |

3.0 s rather than 2.7 s: the latest observed retry is 2.55 s; 3.0 s leaves 0.45 s so the app's connect finds Play **on** the channel (attempt 1 kills it, attempt 2
connects) instead of racing Play's retry into a free channel (then Play's retry cuts the app within 2 s, as 14845/14890). The doubling (3, 6, 12) is the back-off the
maintainer asked for; the cap 3 keeps the worst case per tab view at ≈ 37 s and three kills of Play's socket. (c) shows more holds at the price of ten kills.

**The texts** are the checkpoint's. **Tests:** `Cap074Fixtures.kt` (the bytes of §A.1 with file, frame, time and direction), `BudsRepositoryImplTest` mode 1 / mode 2 /
switch tests (virtual time: the close, no `openChannel` within 60 s in mode 1; `openChannel` after exactly the chosen delay in mode 2; the second close → the next
step; the cap; the tab left / the app hidden / a loss / a Disconnect during the delay → no claim; the switch to mode 1 cancels a pending re-claim), a
`HoldReclaimSettingsStoreTest` (the round trip on a temporary file), `:ui` `SettingsMenuTest` (the card, its two labels, the selected one, the tap), `AncScreenTest`
(the hint line's literal, the corrected `ANC_HOLD_HINT_TEXT`). The ten hold tests of `ai-sessions/0084` run with mode 1 (their behaviour) and stay unchanged.

### A.4 Text locations (Item 2) and free IDs (Item 3)

`CHANGELOG.md` `[Unreleased]` (line 14, empty) → the new version's block above `[1.2.0]`; `README.md` Status (lines 94–126: the 1.2.0 paragraph at 113–118, the
"Known issue since 1.0.1" block), "Current state (2026-10-10)" (151–175: 73 captures, 60 ADRs); `scripts/release_notes.template` (the Features and the known
issues); `PROJECT.md` lines 148–150 (the 1.2.0 status paragraph); `ARCHITECTURE.md` §2.4 line 152 (the Settings tab's list) and 199 (the ANC (i)), §3.2 line 350
(`MESSAGE_STREAM_LINGER_MS` — one row for the new constants), §5a line 490 (the "Changed by the Buds" row), §6.0b lines 606–617 (the hold paragraph and the
`CAP-074` observation), §7 (no new `BudsError`; the new state type named); `TODO.md` §5 lines 193–199 (the item this build does), §2 line 37–42 (the hold's
branches: `CAP-075` takes the Play-user branch), §3 (the Nearby-devices permission item applies to `CAP-075`). **Free:** `grep -n "CAP-075\|Group BK\b\|ADR-062"`
over `id_registry.csv`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `APP_TESTPLAN.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `TODO.md`, `DECISIONS.md`, `README.md`,
`CHANGELOG.md` → nothing (exit 1 on each; the only matches are this session's prompt, RESULT and INDEX row); positive control `grep -n "Group BJ" id_registry.csv`
→ line 238 (exit 0). `APP_TESTPLAN.md` has no section X (`grep -n "^## X"` → exit 1; the "X1"…"X5" rows are the "After the run" table). The Test-ID for the holds is
`TOUCH-007` with `ANC-001`…`ANC-004`; `PAIR-003`, `BATT-004`, `FW-003` for the Connects, the battery burst and the Info tab.

## D. Phase D — gate, mutations, compliance

**Gate after the code change** (`cd android && ./gradlew assembleDebug testDebugUnitTest test lint --max-workers=2`, gate_after.log): **exit 0**, BUILD
SUCCESSFUL. Tests (JUnit XML): `:data` **1664** (debug) + 1664 (release) — baseline 1655, +9 (7 repository tests, 2 settings-store tests); `:domain` 42
(unchanged); `:hardware` 64 + 64 (unchanged); `:ui` **95** + 95 — baseline 90, +5 (`AncScreenTest` 2, `SettingsMenuTest` 3); 0 failures, 0 skipped. Lint: 0
errors; `:app` 6 warnings and `:ui` 1 — the same files as `ai-sessions/0084`'s baseline (the toolchain-version warnings and `SettingsMenu.kt`'s deprecated
`TabRow`), none in a line this session wrote. The final gate after the version bump: §G.

**Mutation checks** (one at a time, `--max-workers=2`; the scratchpad's mutate.sh copies the file aside, applies an exact one-occurrence replacement, runs the
targeted class, restores the copy and compares with `cmp`; the logs M1.log … M7.log in the scratchpad):

| # | Mutation (`BudsRepositoryImpl.kt` unless said) | Tests run | Result |
|---|---|---|---|
| M1 | mode 1 re-claims after a foreign close (`holdReclaimModeSnapshot = mode` → `= HoldReclaimMode.RECLAIM`) | `BudsRepositoryImplTest` | **killed** — 3 of 173 failed: "hint only — no re-claim after the cut", "the switch", and the ADR-061 test "a channel closed under the hold is not re-claimed by itself" |
| M2 | mode 2 never re-claims (`if (!claimNow) return@launch` → `if (claimNow)`) | the same | **killed** — 3 failed: "take it back — re-claims 3, 6 and 12 s", "a session loss during the delay" (the restarted chain), "the switch" |
| M3 | mode 2 re-claims without the delay (`delay(delayMs)` → `delay(0)`) | the same | **killed** — 5 failed ("not before 3 s" and every test that leaves/loses/disconnects during the delay) |
| M4 | mode 2 keeps re-claiming after the cap (the cap check → `if (false)`, the delay index modulo the size) | the same | **killed** — 1 failed: "re-claims 3, 6 and 12 s … no claim after the cap" |
| M5 | mode 2 re-claims with the tab left / the app hidden (`cancelReclaim` made a no-op; the `claimNow` re-check removed) | the same | **killed** — 2 failed: "leaving the tab or the app during the delay claims nothing", "the switch". *A loss or a Disconnect during the delay is not reached by this mutant:* its re-claim meets `withMessageStream`'s own `Ready` check (the existing ADR-032 guard) and claims nothing — the loss and Disconnect tests pass against M5 for that reason, not for lack of coverage; M3 shows both tests do fail when a claim is actually attempted before the loss/Disconnect |
| M6 | the hint state not set on a foreign close (both `_ancChannelTaken.value = ChannelTaken(…)` lines removed) | the same | **killed** — 3 failed: "hint only", "re-claims 3, 6 and 12 s" (the hint after the cap), "the switch" |
| M7 | the switch's DataStore key not persisted (`HoldReclaimSettingsStore.setMode`'s `edit` → `Unit`) | `HoldReclaimSettingsStoreTest` | **killed** — 1 of 2 failed: the round trip |

After the seven runs the two files equal their pre-mutation copies (`cmp`), `grep` finds no mutated text (`delay(0)`, `if (false)`, `if (job.isActive) return` →
0) and `git status` shows only this session's changes. **Process slip:** the first version of mutate.sh restored the file through a path relative to a directory it
had just left (`cp: … No such file or directory`) — M1's mutation stayed on disk for one step; it was restored by hand from the kept copy at once (`cmp` equal,
`grep` of the mutated string → 0) and the script was fixed to absolute paths before M2.

**Compliance:** `git diff --stat -- '*AndroidManifest.xml' '*.gradle.kts' 'android/gradle/*'` → empty before the version bump (no permission, no dependency —
the only `INTERNET` match in a manifest is the comment at `app/src/main/AndroidManifest.xml:3`); after the bump only `versionCode`/`versionName` and one comment
line of `android/app/build.gradle.kts` differ. `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` → 892 lines on this branch (deps_branch.txt),
the same count as `ai-sessions/0084` recorded for `main`, and no dependency declaration changed (the build files' diff is the version lines only) — no new
artifact on the classpath. The four new Kotlin files (`HoldReclaimMode.kt`, `HoldReclaimSettingsStore.kt`, `Cap074Fixtures.kt`, `HoldReclaimSettingsStoreTest.kt`)
carry the AGPL header. No reflection, no hidden API (`grep` of the diff for `Class.forName`, `getDeclaredMethod`, `@hide`, `SystemApi` → none). The new always-on
log lines are fixed text with a count and a delay in ms — "Noise-control channel taken by another client: the hint is shown, no re-claim (ADR-062, hint only)",
"Message Stream re-claim N/3 after a foreign close, D ms later (ADR-062)", "Message Stream re-claim stopped after N re-claims — …", "Message Stream re-claim
cancelled (ADR-062)" — no payload, no address, no serial. No new wire request: the re-claim is `refreshAncMode`'s existing claim.

## E. Phase E — the `CAP-075` skeleton and the test plan

- captures/CAP-075-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BK/CAP-075-EVENT-NOTES.md (new): `CAP-073`'s skeleton layout — status, the two parts, "not in this run",
  metadata (part 1 user 0 with Play, part 2 user 10 without), P0 (with Play's *Nearby devices* permission and version on film) … P9 (P6: the system log within a
  minute of the first cut), a reference-frame table re-derived from `CAP-074` (the channel-19 Connect reads 15758 … 15846, the snapshot claim 14450/14500/14525,
  the tap 14552–14556, the cut 14577, Play's retry 14624, the shown hold 15853/15860/15909) and `CAP-073` (3484, 3492, 3503, 3531–3547, 3654), BK-1 … BK-14
  (part 1: hint only — the 30 s of holds, the hint, the (i), the tap and the pull; take it back — the 60 s of holds with the three re-claims and the stop, the
  tap's new chain, the cancellations by a tab change, Home and the switch; the Disconnect/Connect), BK-15 … BK-18 (part 2: the `CAP-073` §4 regression in both
  positions, the `DISC` on leaving) and the optional BK-19 … BK-21 (`TODO.md` §2's branches), each with "Expected on screen", "Expected on the wire", Test-ID and
  "Refuted if"; Don'ts; After the run; the analysis checklist (the claim inventory with both clients, the re-claim timings ± 0.3 s, the system log's stack lines,
  the traceability list `PAIR-003`, `BATT-004`, `FW-003`, `ANC-001`…`ANC-004`, `TOUCH-007`). ≤ 20 min on film.
- `id_registry.csv`: `CAP-075,capture,planned,…` after `CAP-074`; `ADR-062,adr,accepted,…` after `ADR-061`.
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md`: the Group BK section after Group BJ; the *planned* `CAP-075` row after `CAP-074` in §9.
- `APP_TESTPLAN.md`: the update note, **section X** (X1 … X12 mapped to BK-1 … BK-21), the Summary row "X 1.2.1 | 12"; the "After the run" rows renamed AR1 … AR5
  (they were X1 … X5 and would have collided — nothing outside that file referenced them: `grep -rn "\bX[1-5]\b"` over the Markdown outside `captures/` finds
  only `PROTOCOL.md`'s `CAP-058` extra tests, a different namespace).
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` is not changed (the Group column gains BK when `CAP-075` is analysed — the skeleton's checklist says so).

## F. Phase F — documentation and release preparation (the approved texts)

- `DECISIONS.md`: **ADR-062** (the preview's text; the process note names the chat answers; Status "Accepted … not yet hardware-verified — `CAP-075`") and the
  **ADR-061 pointer Update** (the preview's text). ADR-060 stays a draft. `id_registry.csv` registers ADR-062.
- `CHANGELOG.md`: `## [1.2.1] - not yet released` above `[1.2.0]` — the intro, Added (the setting), Changed (the (i)), Known issues (the Play user in each mode; the
  1.2.0 ones stay). No date.
- `README.md`: the 1.2.1 Status paragraph after 1.2.0's; "Current state (2026-10-12)": 75 captures (`CAP-074` analyzed, `CAP-075` planned), 61 accepted ADRs
  (ADR-062), `CAP-074` among the own-app captures, the setting in "Implemented in the app".
- `scripts/release_notes.template`: "New in 1.2.1" and "Known issues in 1.2.1" after the Features line (`dist/` untouched).
- `PROJECT.md`: the status paragraph gains `CAP-074`'s finding and 1.2.1.
- `TODO.md`: §5's "the Play-user hold fix" item replaced by "the 1.2.1 release steps (**M**)" (merge, B1–B3, `CAP-075`, the CAPTURE session, an ADR-062 Update, D/E)
  with the open point on Play's timers; §2's hold item points to `CAP-075`'s steps; §3's Nearby-devices item notes `CAP-075`'s P0 asks for it.
- `android/app/build.gradle.kts`: `versionCode = 10201`, `versionName = "1.2.1"` (the comment line names the session).
- `ARCHITECTURE.md` (done in Phase C with the code): §2 (the Data Layer note: a third key), §2.4 (the Settings tab's list; the ANC (i) and the hint line; the card),
  §3.2 (`HOLD_RECLAIM_DELAYS_MS`), §5a (the "Changed by the Buds" row: 1.2.1, not hardware-verified), §6.0b (the ADR-062 paragraph; the "cannot use" sentence
  reworded), §7 (no new `BudsError`: `ChannelTaken` is a state), §9 (three stored values).
- `ai-sessions/INDEX.md`: the 0087 row (complete; the commit question in the chat).
- `PYTHONDONTWRITEBYTECODE=1 python3 scripts/ensure_footers.py` → all footers up to date; `python3 scripts/lint_docs.py` → **exit 0** ("clean") after four backticked
  scratch names in this file were unquoted (the lint's rule; the memory note "lint_docs: backticked scratch names").

## G. Phase G — the final gate and the next steps

**Final gate after the version bump** (gate_final.log): exit 0, BUILD SUCCESSFUL; `:data` 1664 + 1664, `:domain` 42, `:hardware` 64 + 64, `:ui` 95 + 95, 0 failures; lint 0 errors (the same 6 + 1 warnings as the baseline).

### Next steps for the maintainer (instructions — none executed by this session)

1. Answer the commit question in the chat (per-concern commits are proposed there).
2. Push and open the pull request: `git push -u origin feature/hold-reclaim-switch`, `gh pr create --base main --fill`, with the Release checklist of `RELEASING.md`
   in the description — A1–A6 done (A1 this branch, A2 1.2.1/10201, A3 the texts, A4 the gate of §D/§G, A5 the commits, A6 your CI check), B1 for you to fill.
3. Both CI workflows green on the pull request (Android build and test, docs lint). Do not merge yet.
4. **B1** `scripts/release.sh 1.2.1` on the branch tip — it writes `dist/1.2.1`. Note the printed build commit.
5. **B2** the certificate SHA-256 = `a7530f5c…d8dcb` (`README.md` / `SECURITY.md`). **B3** `cp -a dist/1.2.1 ~/opencontrol-1.2.1-tested`.
6. **C1–C2** install over the 1.2.0 on the phone (`adb install -r …`, one APK serves both users, no uninstall) and run `CAP-075` per its skeleton — part 1 in the
   Owner user with Play services (both modes; the system log within a minute of the first cut; the *Nearby devices* permission on film), part 2 in user 10;
   collect the files into its folder.
7. Then a CAPTURE session for C3/C4 (the findings, the verdict, an ADR-062 Update with that run's numbers).

## After the session — the merge and the build (2026-10-12, the maintainer)

The maintainer merged PR #39 with a merge commit (`31ee183`, on their instruction in chat) and the bot PR #38 (`_sidebar.md` only, `935a37a`); bot PR #40
(`sitemap.xml`) was open afterwards. Then **B1**: `scripts/release.sh 1.2.1` (2026-10-12 17:17) — built and verified by the script, **build commit `31ee183`**
(the merge commit; the Info tab will show "build 31ee183", the tag of D2 goes there). Checked by this session: `sha256sum -c` → OK, APK SHA-256
`a60a3cc9b2a80e9dc5fecb8810c9cb8e7cbb20fa9eb7a2a0ce663b89e964963e`; `apksigner verify --print-certs` → `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`
= `README.md` / `SECURITY.md` (**B2**); `aapt2 dump badging` → versionCode 10201, versionName 1.2.1, the four permissions of `ARCHITECTURE.md` §9 plus AndroidX's
private receiver permission, no `INTERNET`; `release-notes.md` has no unfilled `{…}` and carries the 1.2.1 lines and both checksums; `THIRD_PARTY_NOTICES.txt`
136 libraries. **B3** (`cp -a dist/1.2.1 ~/opencontrol-1.2.1-tested`) is the maintainer's own act — not yet made when this was written. `dist/` is gitignored and
not committed (`RELEASING.md` §5); the build's identity is recorded here and in the `CAP-075` skeleton. Because the pull request was merged before the hardware
run, the capture and its analysis go on their own branch and pull request (C3). Next: B3, C1–C2 (`CAP-075`).

## Files read

- **In full:** `AGENTS.md` (system context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `DECISIONS.md` (ADR-001 … ADR-061 with every Update, the draft
  ADR-060), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, `RELEASING.md`, the prompt, `ai-sessions/0086_CAPTURE_PROMPT_2026_10_12.md` and `…RESULT…`, `CAP-074-FINDINGS.md`,
  `CAP-074-EVENT-NOTES.md`, `CAP-073-FINDINGS.md`, `CAP-073-EVENT-NOTES.md`, `ai-sessions/0084_FEATURE_PROMPT_2026_10_10.md` and `…RESULT…`,
  `ai-sessions/0040_FEATURE_RESULT_2026_09_19.md`, `scripts/release_notes.template`; Kotlin: `BudsRepositoryImpl.kt`, `BudsRepository.kt`, `BudsTransport.kt`,
  `RfcommBudsTransport.kt`, `SessionReopener.kt`, `AncScreen.kt`, `ConnectionBanner.kt`, `SettingsMenu.kt`, `SettingsDataStore.kt`, `DarkModeSettingsStore.kt`,
  `DebugSettingsStore.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`, `AncTileService.kt`, `RepositoryModule.kt`, `FakeBudsTransport.kt`, `AncScreenTest.kt`,
  `SettingsMenuTest.kt`, `UserMessageTest.kt`, `Cap045Fixtures.kt`, `Cap072Fixtures.kt`, `DarkMode.kt`, `android/app/build.gradle.kts`; the DLCI 0x04 frames of
  `CAP-074` on handle `0x000b` through the inventory (claims.py).
- **In part:** `PROTOCOL.md` (§2.3, §4.1, §8's last rows — not the rest), `ai-sessions/INDEX.md` (the last rows), `APP_TESTPLAN.md` (the head, §W, "After the
  run", the Summary), `CHANGELOG.md` (the head, `[1.2.0]`), `README.md` (the head, Status, Current state), `id_registry.csv` (the ADR/CAP/Test-ID rows named),
  `ai-sessions/0039_FEATURE_RESULT_2026_09_19.md` (§2.4, §8), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (the Group BI/BJ sections, the §9 header and the last rows),
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the rows of the eight Test-IDs), `BudsRepositoryImplTest.kt` (the set-up and helpers, lines 1–260; the ten hold tests,
  3077–3268; the rest by `grep`), `SettingsUi.kt` and `Details.kt` (the card and (i) helpers), `TabRestoreTest.kt` (the ADR-061 test), `DebugScreen.kt` (the
  switch pattern), the export `CAP-074-opencontrol-debug-20261012-070305.txt` (the hold/close/connect lines by `grep`).
- **Not read:** nothing of the prompt's list beyond the parts named above was skipped knowingly; `PROTOCOL.md`'s other sections and the other capture files were
  not needed and not read.


## Deferred documentation (each also in `TODO.md`)

- `TODO.md` §5: "**M** — the 1.2.1 release steps (`ai-sessions/0087`, built 2026-10-12: the "Noise-control channel" switch, ADR-062): merge the pull request when CI
  is green; `scripts/release.sh 1.2.1` …; the run `CAP-075` …; then a CAPTURE session for the verdict (C3/C4) and an ADR-062 Update with that run's numbers …" (new,
  replaces the "Play-user hold fix" item this session built).
- `TODO.md` §2: the hold item's addition "**`CAP-075` (Group BK, planned by `ai-sessions/0087`) takes the Play-user branch in both switch positions (BK-5 … BK-14)
  and, optionally, the tile, Home and the loss with the tab shown (BK-19 … BK-21).**" (changed).
- `TODO.md` §3: "… `CAP-075`'s P0 asks for it on film (the first time), with Play services' version." (changed).
- `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s Group column for the BK Test-IDs — the analysing session of `CAP-075` (its skeleton's checklist says so).
- Not deferred, noted: `ANC_HOLD_HINT_TEXT` and the hint line are Kotlin literals (the standing string-resources item of `TODO.md` §5).


## Commits

The maintainer's answer (chat 2026-10-12, `AskUserQuestion` "Commit"): *"Commit + push (Recommended)"*. On `feature/hold-reclaim-switch` (from `main` `8a0db34`),
per concern, each with its *why* and the attribution line:

1. `64bb0ef` feat: the Noise-control channel switch — a hint or a re-claim after a foreign close (ADR-062) — `:domain`, `:data`, `:ui`, `:app`, `ARCHITECTURE.md`.
2. `cdf3a78` test: CAP-074 fixtures, the hold-reclaim tests with FakeBudsTransport, the settings-store round trip, the UI texts.
3. `5cd5664` docs: ADR-062 and the ADR-061 Update, 1.2.1 (10201) — CHANGELOG, README, release notes, PROJECT, TODO, id_registry (both new registry rows).
4. `3f373c2` docs(capture): CAP-075 (Group BK) skeleton — the 1.2.1 run in both GrapheneOS users; APP_TESTPLAN §X.
5. `4778d32` docs(ai-sessions): 0087 FEATURE result, INDEX row; then the back-fill of these hashes and the pull-request number (`AI_SESSION_LOG_PROCEDURE.md` §4b item 2).

`git fetch` (`origin/main` had not moved, no rebase needed); pushed without force; **pull request #39** opened on the maintainer's answer ("Open the pull request", chat 2026-10-12: https://github.com/tedsluis/opencontrolpixelbudspro2/pull/39) with the summary and the Release checklist — not merged by this session; no tag, no release. *(The hash of the back-fill commit of this paragraph is completed by the next session.)*

**Release (E3, added 2026-10-13):** PR #39 merged with a merge commit on 2026-10-12 → `31ee183`, built by the maintainer (`scripts/release.sh 1.2.1`, 2026-10-12 17:17, kept as ~/opencontrol-1.2.1-tested; APK SHA-256 `a60a3cc9b2a80e9dc5fecb8810c9cb8e7cbb20fa9eb7a2a0ce663b89e964963e`, certificate `a7530f5ceadcdfc9cddcbb0e9889e7699961e8a4ef18898c4ec7738cc79d8dcb`); the hardware run `CAP-075` (both users) analysed in `ai-sessions/0088` (PR #44, merged 2026-10-13) with the verdict "no heavy issue — release as built"; tag `v1.2.1` on `31ee183`, the release published 2026-10-13 (`RELEASING.md` §13).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0087_FEATURE_RESULT_2026_10_12.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0087_FEATURE_RESULT_2026_10_12
