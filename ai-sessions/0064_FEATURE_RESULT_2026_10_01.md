# 0064_FEATURE_RESULT_2026_10_01.md — Keep the tab across a configuration change, make the balance precise, name a Bluetooth-off loss, close every profile proxy, and add licence / README / issues links to Settings → Info; write the `CAP-067` (Group BC) skeleton

**Number:** 0064
**Category:** FEATURE
**Date:** 2026-10-01
**Title:** Build the app changes the maintainer chose after `CAP-066` (`ai-sessions/0063` checkpoint "FEATURE") plus licence, README and issue links on the
Info tab, with real `CAP-066` bytes and log lines as fixtures, a green test/lint gate with mutation checks, and a new `CAP-067` (Group BC) skeleton
**Status:** complete

---

## Summary (plain language — what you will see differently)

1. **Turning the phone keeps your tab.** In `CAP-066` rotating the phone on Sound jumped to Connection; now every tab (and Settings → Info) stays where it was,
   also when Android's own dark switch restarts the screen.
2. **Balance is precise.** Two small buttons `‹` and `›` sit beside the balance slider; each tap moves the Buds' balance one step (Left/Right 1) and sends one
   change. Right 4 — never reached by dragging in `CAP-066` — is four taps from Centre. The slider itself works as before.
3. **Switching Bluetooth off is named.** When Bluetooth is switched off while the app is open, the debug log now says "Session loss cause: Bluetooth was switched
   off on this phone" (final, not "undetermined (provisional)") and logs each Bluetooth on/off step.
4. **Tidier Bluetooth bookkeeping.** The app now closes every Bluetooth profile helper it opened, also one Android dropped at a Bluetooth-off (a debug-log
   warning in `CAP-066`; an LE Audio warning may still appear — that one is Android's own).
5. **Settings → Info** shows the licence ("GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)"), **Read the licence** (the full text, offline),
   and links to the licence, the README and the issue tracker on GitHub — they open in your browser when you tap them; the app itself still has no internet
   access (DECISIONS.md ADR-050, approved in chat).
6. **Not hardware-tested yet.** `CAP-067` (Group BC) is the planned run, skeleton written.

## Progress

- **Phase 0, A, B: done** (checkpoint §G). Phase 0: Mandatory reading of `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (ADR-001 …
  ADR-049 with every Update), `TODO.md` done in full; `AI_SESSION_LOG_PROCEDURE.md` and `ai-sessions/INDEX.md` read in full.
- Repo at start: `HEAD` = `d5fdf10` (prompt 0064); `git status --short` = `?? android/.kotlin/`; `git fetch && git log --oneline HEAD..origin/main` = empty
  (nothing to fast-forward); last commit under `android/` = `ad0c4ba Thu Oct 1 15:38:00 2026 +0200`.
- **Phase C: F-1 and F-2 built and tested** (module test runs green: `:ui` TabRestoreTest 6/6 + EqScreenTest 9/9, `:domain` 28/28, `:data`
  SettingsCodecTest 24/24 and BudsRepositoryImplTest 123/123). Files touched: `OpenControlNavHost.kt`, `EqScreen.kt`, `BudsSettings.kt`, tests
  `TabRestoreTest.kt` (new), `EqScreenTest.kt`, `BalanceSnapTest.kt`, `SettingsCodecTest.kt`, `BudsRepositoryImplTest.kt`, fixtures `Cap066Fixtures.kt` (new).
- **Phase D: F-3 and F-4 built and tested** (`:domain` 31/31, `:hardware` 56/56, `:ui` 38/38, `:data` 1588/1588, `:app` compiles). Files touched:
  `SessionLoss.kt`, `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `SessionDiagnostics.kt`, `ConnectionScreen.kt`, `BluetoothStateObserver.kt`, `MainActivity.kt`,
  `OsConnectionObserver.kt`, `ProfileProxies.kt` (new); tests `SessionLossTest.kt`, `SessionDiagnosticsTest.kt`, `BudsRepositoryImplTest.kt`,
  `BluetoothAdapterLogTest.kt` (new), `LossWordingTest.kt` (new), `ProfileProxiesTest.kt` (new). 
- **Phase E: F-6 built and tested** (`:ui` 41/41, `:app` compiles). Files: `SettingsMenu.kt`, `OpenControlNavHost.kt` (`onOpenUrl`), `MainActivity.kt`
  (`openUrl`, `noBrowserText`), `android/ui/src/main/res/raw/license.txt` (new, `sha256` = `LICENSE`'s `e759409d…34648c9`); test `SettingsMenuTest.kt`. No manifest
  change.
- **Phase F and G: done** — gate §K, mutations §L, compliance §I, `CAP-067` §J, documentation §N; commit pending the maintainer's answer (see "Commits").
- Scratchpad: `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/5b9789ba-b996-4096-9dfc-a175ee79e0fe/scratchpad` (counts.py and mutate.py copied
  from the 0062 scratchpad; re-create if gone).

## C. Baseline gate (Phase 0 task 2)

`cd android && ./gradlew --offline clean` (exit 0) then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL** in 3 m 21 s
(exit 0). Test counts from the JUnit XML (scratchpad `counts.py`):

| Module | Task | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|
| `:data` | testDebugUnitTest / testReleaseUnitTest | 1581 / 1581 | 0 | 0 | 0 |
| `:domain` | test | 27 | 0 | 0 | 0 |
| `:hardware` | testDebugUnitTest / testReleaseUnitTest | 52 / 52 | 0 | 0 | 0 |
| `:ui` | testDebugUnitTest / testReleaseUnitTest | 24 / 24 | 0 | 0 | 0 |

Lint (`*/build/reports/lint-results-debug.xml`): `:app`, `:ui`, `:data`, `:hardware` — 0 issues each. Kotlin compiler warnings: 0
(`grep -c "^w: " baseline_gate.log` → 0).

## D. Phase A — fixtures, re-derived (task 3)

**F-2 balance (real bytes).** Command (rule 4a), in the `CAP-066` folder: `tshark -r CAP-066-btsnoop_hci.log.last -Y "bthci_acl.chandle==0x000b &&
btrfcomm.len>0 && frame.number==N" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data` (exit 0 each):

| Frame | Time | Dir | DLCI | Bytes | Meaning |
|---|---|---|---|---|---|
| A7723 | 16:30:40.202 | phone→Buds | 0x02 | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 0b c2 f4 d8 2c 7e` | `WriteSetting 4:{17:11}` ch 19 = zigzag −6 = Right 6 |
| A7873 | 16:31:27.679 | phone→Buds | 0x02 | `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 00 4a 2d 0a bb 7e` | `WriteSetting 4:{17:0}` ch 19 = Centre |

Both equal the prompt's bytes. **A captured `17:7` exists** (positive search, not derived): `for f in captures/*/*btsnoop_hci*.log
captures/*/*btsnoop_hci.log.last; do python3 scripts/pwrpc_decode.py "$f" | grep -E "4:\{17:[0-9]+\}"; done` → 258 lines, 26 with `4:{17:7}` (positive control:
6 with `17:11`). Writes of `17:7`: `CAP-063` 5211, 5242, 5368 and `CAP-064` 6671 — all **channel 21**; the rest are `ReadSetting` answers. `tshark … frame.number==6671`
on `CAP-064-btsnoop_hci.log` (10:14:28.242, phone→Buds, DLCI 0x02): `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03 88 01 07 a9 7d 5e df 03 7e`,
answered by 6673 `7e 00 a5 03 08 01 10 15 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 03 6d 4e d8 7e` (empty `RESPONSE` OK); `CAP-063` 5211 is byte-identical. **No
channel-19 `17:7` exists in any capture** — on channel 19 the encoder's `17:7` is derived (from A7723's header with `07` and its CRC-32), labelled as such in a test.

**F-3 Bluetooth-off loss (real log lines).** Debug export E1 (`CAP-066-opencontrol-debug-20261001-162556.txt`), lines 279–283 (local time):
`16:13:00.971 Android link: CONNECTED -> UNKNOWN (trigger: CONNECTION_STATE_CHANGED)`, `16:13:01.383 RFCOMM channel 0x02 lost (IOException: bt socket closed,
read return: -1) …`, `16:13:01.386 Session lost: channel 0x02 closed …`, `16:13:01.390 Session loss cause: undetermined (no reading of Android's link close to the
loss) (provisional: …)`, `16:13:01.424 ConnectionState: Ready -> Disconnected`. Export E2 (`CAP-066-opencontrol-debug-20261001-163412.txt`), lines 164–168:
`16:32:17.010 … CONNECTED -> UNKNOWN`, `16:32:17.621 RFCOMM channel 0x02 lost …`, `16:32:17.632 Session lost …`, `16:32:17.642 Session loss cause: undetermined …
(provisional …)`, `16:32:17.645 Ready -> Disconnected`. **The adapter state at those moments** (the app does not log it — new evidence from the system logs, UTC = local
− 2 h): `grep -n "BluetoothAutoOff: Intent" CAP-066-System-log-*.txt` → `CAP-066-System-log-d55db7f4e1c8.txt` line 38635 `14:13:00.837 … STATE_CHANGED …
PREVIOUS_STATE=12, … STATE=13` (ON → TURNING_OFF) and line 38754 `14:13:01.361 … PREVIOUS_STATE=13, … STATE=10` (→ OFF); `CAP-066-System-log-7f4bb1d5ea2c.txt`
line 42742 `14:32:16.820 … 12 → 13` and line 42895 `14:32:17.651 … 13 → 10`. So the adapter was reported turning off **0.55 s (E1) and 0.81 s (E2) before**
the loss; it was fully off 0.02 s before (E1) and 0.03 s after (E2). Positive control: the same grep finds the two Bluetooth-**on** sequences (10 → 11 → 12 at
14:13:05.329/.695 and 14:32:24.378/.960). ⚪ ASSUMPTION: the app's own `ACTION_STATE_CHANGED` receiver got the broadcast within milliseconds of these system lines
(the app does not log adapter changes; its link observer did log the related `CONNECTION_STATE_CHANGED` at 16:13:00.971 / 16:32:17.010).
🟡 Why the link read `UNKNOWN`: `LinkEvaluation.evaluate` returns `UNKNOWN` when the bonded address resolves to `null` (`LinkEvaluation.kt:42`), which is what an
adapter that is turning off gives — consistent with both sequences, not separately verified.

**F-1 red test (the cause, confirmed).** `TabRestoreTest` (`:ui`, Robolectric, `StateRestorationTester`): tap a bottom tab, check its card, call
`emulateSavedInstanceStateRestore()`, check the same card and the selected item. On the unchanged code: **4 of 5 tab cases fail** at the first assertion after
the restore (`TabRestoreTest.kt:104` — ANC, Sound, Controls, Find: the card is gone); Connection passes (it is page 0 anyway); the Settings/Info case passes
(the menu and its Info tab are kept — `rememberSaveable` in `SettingsMenu.kt:82`; the pager is not shown in the menu). Command: `./gradlew --offline
--max-workers=2 :ui:testDebugUnitTest --tests '*TabRestoreTest*'` (exit 1). This reproduces `CAP-066` K4r in a JVM test.

## E. Phase A — per item: today, the plan, the tests, the mutation (task 4)

- **F-1.** Today: `OpenControlNavHost.kt:239–243` — `currentIndex` falls back to 0 (`coerceAtLeast(0)`) while `currentBackStackEntryAsState()` is still `null`
  on the first composition after a relaunch; `:246–248` scrolls the restored pager to page 0; `:251–258` then sees settled page 0 ≠ the restored Sound and
  navigates to Connection. Plan: make the current index `null` while the back stack is unknown, and skip both syncs then (the pager keeps its own restored page;
  once the back stack is known the two agree). Tests: `TabRestoreTest` (5 tabs + Settings/Info, back still returns to the tab). Mutation M1: the unconditional
  sync (index 0 while unknown) → the 4 tab cases fail.
- **F-2.** Today: `EqScreen.kt:231–256` `BalanceSlider` — continuous `-100f..100f`, one write per release, `BudsSettings.snapBalance` (±3 → 0); the label
  shows the Buds' value only. Plan: the checkpoint's choice. Tests: `EqScreenTest` (a tap/gesture → exactly one `onChange` with the expected value; Right 4
  reachable) and, since the wire path is unchanged (`SettingsCodec.balanceRequest`), a codec test that `17:7` on channel 21 is byte-identical to `CAP-064` 6671.
  Mutation M2: per the chosen control (e.g. a button that writes twice, or the old continuous slider path).
- **F-3.** Today: `classifySessionLoss` (`SessionLoss.kt:73–92`) knows only link readings; `BudsRepositoryImpl` gets no adapter state (MainActivity collects
  `BluetoothStateObserver` for the screen only, `MainActivity.kt:333–334`). Plan: MainActivity forwards every adapter reading (while visible, as the link
  readings) to a new `BudsRepository.onBluetoothAdapter(on)`; `classifySessionLoss` gains a rule 0 — an adapter-off reading (TURNING_OFF or OFF) from 2 s before
  to 1 s after the loss ⇒ a new cause `BLUETOOTH_OFF`, logged final (no "(provisional)"); `SessionDiagnostics.lossCauseLine` and `channelLostMessage` get its
  text; an always-on log line for each adapter change. Tests: `SessionLossTest`/`SessionDiagnosticsTest`/`BudsRepositoryImplTest` built from the E1/E2 times
  above; the existing provisional/link tests stay green. Mutation M3: rule 0 removed (an adapter-off loss → `UNDETERMINED`).
- **F-4.** Today: `OsConnectionObserver.kt:121–127` removes a proxy from the map on `onServiceDisconnected`; `awaitClose` (`:141–144`) closes only the map's
  members. Plan: a small pure bookkeeping class in `:hardware` that keeps every proxy the listener delivered (identity) and closes each exactly once in
  `awaitClose`; the observer uses it. Test: a JVM test with plain proxy objects (bound, unbound, re-bound, flow end → each closed once). Mutation M4: close only
  the currently bound ones → the test fails. Note (`CAP-066-FINDINGS.md` §8, 🟡): in AOSP `android16-qpr2-release` `BluetoothLeAudio` opens its `CloseGuard` and
  never closes it, so StrictMode may still report it after this fix — the goal is that the app closes every proxy it obtained.
- **F-6.** Today: `SettingsMenu.kt:163–176` `InfoTab` — the build line and the firmware lines only. Plan: per the checkpoint (below). Test: `SettingsMenuTest`.
  Mutation M5: a wrong URL.

## F. Phase A — F-6 facts (task 5)

- **URLs:** `git remote get-url origin` → `git@github.com:tedsluis/opencontrolpixelbudspro2.git`; `README.md`'s footer is
  `https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/README.md`; `git ls-files LICENSE README.md` → both tracked. `curl -s -o /dev/null -w
  '%{http_code}' -L <url>` → 200 for `…/blob/main/LICENSE`, `…/blob/main/README.md` and `…/issues`; negative control `…/blob/main/NO_SUCH_FILE.md` → 404.
- **Licence:** `LICENSE` is the GNU Affero General Public License, Version 3, 19 November 2007 (235 lines); 117 Kotlin files carry `SPDX-License-Identifier:
  AGPL-3.0-or-later` and the FSF notice "either version 3 of the License, or (at your option) any later version" (`AGENTS.md` §12, ADR-002) → the line
  "GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)" is accurate.
- **Opening a URL from an app (Android 11+)** — developer.android.com "Fulfill common use cases while having limited package visibility" (`/training/package-visibility/use-cases`,
  fetched 2026-10-01 with `curl`, text extracted from the HTML): *"When an app that targets Android 11 or higher uses an intent to start an activity in another
  app, the most straightforward approach is to invoke the intent and handle the ActivityNotFoundException exception if no app is available."* … *"To open a
  URL, use an intent that contains the ACTION_VIEW intent action … An ActivityNotFoundException occurs because there isn't an app installed on the device that
  can open the URL. (This is unusual.) It's recommended that your app catch and handle the ActivityNotFoundException if it occurs. Because the startActivity()
  method doesn't require package visibility to start another application's activity, you don't need to add a <queries> element to your app's manifest or make
  any changes to an existing <queries> element. This is true for both implicit and explicit intents that open a URL."* ⇒ **no manifest change** is needed for
  option (a)/(c).

## G. Phase B — checkpoint answers (chat 2026-10-01, `AskUserQuestion`, each option with pros/cons and a preview; verbatim)

| Question | Answer |
|---|---|
| F-1 rotation | *"Skip sync until known (Recommended)"* |
| F-2 balance | *"−/+ of 1, slider kept (Recommended)"* — the preview: `[‹]` one step toward Left (wire +1), `[›]` toward Right (wire −1), content descriptions "Balance one step to the left" / "… right", disabled until read, at ±100 or when not connected; each tap = the Buds' value ±1 → one write; the slider unchanged (±3 snap) |
| F-3 BT off | *"Final at once (Recommended)"* — log "Bluetooth adapter: ON -> TURNING_OFF", "Session loss cause: Bluetooth was switched off on this phone"; card "Bluetooth was switched off on this phone, which closed the app's channel. Tap Connect to reconnect." |
| F-4 proxies | *"Build as described (Recommended)"* |
| F-6 links | *"Links + bundled licence (Recommended)"* — the Info layout of the preview (Licence: the AGPL line, "Read the licence", "Licence on GitHub"; Project: "README on GitHub", "Report an issue on GitHub", "Links open in your browser; this app itself has no internet access."; no browser ⇒ a toast with the URL); no manifest change |
| F-6 ADR | *"Yes, ADR-050 as drafted (Recommended)"* |
| F-5 CAP-067 | *"As listed, destructive last (Recommended)"* |

## K. Final gate (Phase F task 10)

`./gradlew --offline clean` (exit 0), then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL** in 2 m 38 s (exit 0):

| Module | Task | Tests (baseline → now) | Failures | Errors | Skipped |
|---|---|---|---|---|---|
| `:data` | testDebugUnitTest / testReleaseUnitTest | 1581 → **1588** / 1581 → **1588** | 0 | 0 | 0 |
| `:domain` | test | 27 → **31** | 0 | 0 | 0 |
| `:hardware` | testDebugUnitTest / testReleaseUnitTest | 52 → **56** / 52 → **56** | 0 | 0 | 0 |
| `:ui` | testDebugUnitTest / testReleaseUnitTest | 24 → **41** / 24 → **41** | 0 | 0 | 0 |

Lint: `:app`, `:ui`, `:data`, `:hardware` — 0 issues each (as at baseline). Kotlin compiler warnings: 0 (`grep -c "^w: " final_gate.log`). Suppressions:
`grep -rn "@Suppress\|SuppressLint\|tools:ignore" android` → 1 (the existing `RfcommBudsTransportTest.kt:126`), the same at `HEAD`.

## L. Mutation checks (Phase F task 11)

Driver: scratchpad `mutate064.py` — applies one mutation (exact-string replacement, asserted to occur once), runs that module's tests with `./gradlew --offline
--max-workers=2 :<module>:<task>`, restores the file and compares `sha256` before/after. One at a time, sequentially.

| # | Mutation | Module | Gradle exit | Tests failed / run | Failing tests (first) | Restored byte-identical |
|---|---|---|---|---|---|---|
| M1 | F-1: the `HEAD` code of the sync block restored exactly (the unconditional pager sync — `currentIndex` falls back to 0, no null guard in the collector) | `:ui` | 1 | 4 / 41 | Sound / Controls / Find / ANC is kept across a configuration change — the same 4 that failed on the unchanged code (§D) | yes (`5b3667c49bd0…`) |
| M2 | F-2: a balance write per drag frame | `:ui` | 1 | 1 / 41 | a drag on the slider is still exactly one write, on release | yes (`9d07ea829ea3…`) |
| M2b | F-2: steps of 5 (Right 4 not reachable) — :ui tests | `:ui` | 1 | 2 / 41 | a step from Centre writes 1 or minus 1 - the centre snap does not apply to a step; Right 4 is reachable - one tap on the right step from Right 3 writes exactly -4 (17 colon 7, CAP-064 6671) | yes (`b081ad560b7b…`) |
| M3 | F-3: an adapter-off loss classified as undetermined (rule 0 removed) | `:domain` | 1 | 3 / 31 | CAP-066 E1 16:13:01.386: adapter TURNING_OFF 0.55 s before the loss -> Bluetooth was switched off (was: undete; an adapter-off reading outside 2 s before to 1 s after the loss claims nothing, and it wins over a lost link i; CAP-066 E2 16:32:17.632: adapter TURNING_OFF 0.81 s before, OFF 19 ms after -> Bluetooth was switched off | yes (`68844f8a07f5…`) |
| M3b | F-3: a Bluetooth-off cause logged provisional (not final at once) | `:data` | 1 | 1 / 1588 | F-3, CAP-066 E1: TURNING_OFF 16:13:00.837, link UNKNOWN .971, OFF 01.361, loss 01.386 -> one final line 'Bluet | yes (`ce30787a1423…`) |
| M4 | F-4: awaitClose closes only the bound (map) proxies | `:hardware` | 1 | 2 / 56 | bound, unbound (a Bluetooth-off), then the flow ends: the unbound proxy is closed too, each once; the same proxy bound again after Bluetooth comes back is closed once, a new proxy object for the same profile  | yes (`6ea3506a1ddd…`) |
| M5 | F-6: a wrong Info URL (issues → issue) | `:ui` | 1 | 1 / 41 | Info shows the licence line and the three links, each tap hands exactly its URL to the opener | yes (`73562556287a…`) |

Every mutation fails at least one test; no compile error; every file restored byte-identical. (A first, non-literal M1 — a hand-written fallback in the
collector — failed 7 tests; it was replaced by the exact `HEAD` block above so the check is a true revert.)


## H. What was built, per item (Phase C–E)

- **F-1** — `OpenControlNavHost.kt`: `currentIndex` is `null` while `currentBackStackEntryAsState()` is `null`; the nav → pager effect returns then; the
  settled-page collector returns while `navController.currentBackStackEntry` is `null`. Tests: `TabRestoreTest` (new, 6). The cause is confirmed (red test §D,
  exact-revert mutation M1 §L).
- **F-2** — `BudsSettings.balanceStep`/`BALANCE_STEP = 1` (`:domain`); `EqScreen.kt` `BalanceSlider`: `[‹]`/`[›]` (`BalanceStepButton`, content descriptions
  "Balance one step to the left/right"), disabled until read, while not connected and at the end of the range; the slider unchanged. Wire unchanged
  (`SettingsCodec.balanceRequest`, ADR-045). Tests: `BalanceSnapTest` (+1), `EqScreenTest` (+7), `SettingsCodecTest` (+2: A7723, A7873, `CAP-064` 6671 byte for
  byte; a labelled derived channel-19 check), `BudsRepositoryImplTest` (+1: four steps → four writes, the fourth = 6671, ACK 6673).
- **F-3** — `:domain` `SessionLossCause.BLUETOOTH_OFF`, `AdapterOffReading`, rule 0 in `classifySessionLoss` (2 s before / 1 s after); `BudsRepository.onBluetoothAdapter`;
  `BudsRepositoryImpl` keeps adapter-off readings and logs `BLUETOOTH_OFF` as final; `SessionDiagnostics.lossCauseLine` and `channelLostMessage` texts;
  `BluetoothStateObserver.adapterTransitionLine`/`isOn` and an always-on adapter log line; `MainActivity` collects the adapter states while STARTED and forwards
  each. Tests: `SessionLossTest` (+3, CAP-066 E1/E2), `BudsRepositoryImplTest` (+3), `SessionDiagnosticsTest` (+1), `BluetoothAdapterLogTest` (new, 2),
  `LossWordingTest` (new, 1).
- **F-4** — `ProfileProxies` (new, `:hardware`, pure): `onBound`/`onUnbound`/`closeAll` — every proxy obtained closed once; `OsConnectionObserver` uses it.
  Tests: `ProfileProxiesTest` (new, 2).
- **F-6** — `SettingsMenu.kt`: `ProjectLinks`, `LICENCE_LINE`, `LINKS_NOTE`, the Info tab's Licence/Project sections, `LicenceDialog` reading
  `android/ui/src/main/res/raw/license.txt` (new, = `LICENSE`); `OpenControlActions.onOpenUrl`; `MainActivity.openUrl` (`Intent.ACTION_VIEW`,
  `ActivityNotFoundException` ⇒ `noBrowserText` toast + an always-on log line without the URL). No manifest change. Tests: `SettingsMenuTest` (+3: the line
  and the three URLs exactly, the dialog offline, the bundled file = `LICENSE`). ADR-050 written (approved).

## I. Compliance (Phase F task 12)

- **No `INTERNET`:** `grep -rn "INTERNET" android --include=*.xml --include=*.kt --include=*.kts` (excluding build dirs) → only two comments
  (`app/src/main/AndroidManifest.xml:3`, `SettingsMenu.kt:172`); positive control: `BLUETOOTH_CONNECT` found in both manifests. Merged manifest
  (`app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`): `INTERNET` only in the comment (line 3); the permissions are
  `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` and AndroidX's app-private receiver permission, as before;
  no `<queries>`.
- `git diff -- '*AndroidManifest.xml'` → empty (no manifest change — none was needed or approved). `git diff -- android/gradle/libs.versions.toml
  android/*/build.gradle.kts` → empty (no new dependency).
- `transport.send(` call sites in `BudsRepositoryImpl.kt`: 8 at `HEAD`, 8 now; `writeGate(` call sites: 6 and 6 — every write still passes the gate (ADR-042).
  No protocol change: no new message, no new claim, nothing on DLCI 0x08.
- No network code: `grep -rnE "java\.net\.|HttpURLConnection|WebView|okhttp|Socket\(" android/*/src --include=*.kt` → only the Bluetooth `RfcommSocket`
  classes (no network API); positive control `grep -rnE "Uri\.parse"` finds `MainActivity.kt:294` (the link hand-off).
- AGPL header (`SPDX-License-Identifier: AGPL-3.0-or-later` + FSF notice) on all 6 new Kotlin files. No MAC address and no raw bytes in any new log line
  (the adapter lines carry state names only; the link line has no URL). No ViewModel, no hidden API, no reflection, no timer: F-3 forwards broadcasts; F-1 is
  composition logic only.

## J. The `CAP-067` (Group BC) skeleton (Phase F task 13)

`captures/CAP-067-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BC/CAP-067-EVENT-NOTES.md` — the `CAP-066` skeleton's layout: purposes; A.0; A.1 with P7 = the Info tab
first (build, licence, Read the licence, each link opened on film); steps with "Expected on screen", "Expected on the wire" and "Refuted if" — I F-1 (BC-1, BC-2,
BC-3r), II F-2 (BC-3 … BC-5e; the `17:7` bytes: channel 21 = `CAP-064` 6671, channel 19 derived `7e 00 3b 03 10 13 1d ea 71 de 7d 5e 25 1d 9a 8c 9e 2a 05 22 03
88 01 07 e9 b8 6e 25 7e` — CRC-32 computed by a scratch script from A7723's unescaped body with `07`, labelled derived), III BB-12 (BC-6a/BC-6), IV BB-15
(BC-7), V K4d (BC-8), VI L3 (BC-9), VII F-3/F-4 (BC-10, BC-11, BC-11s), VIII K5 (BC-12), Restore (BC-R: Right 4), IX destructive A5, (E), B4, Z1; A.3; A.4
with the registry Test-IDs `AUDIO-003`, `INEAR-002`–`004`, `PAIR-003`, `BATT-004`, `PAIR-001`. Registered: `id_registry.csv` `CAP-067,capture,planned,…`;
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group BC section and Capture Index row; `_sidebar.md`; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` Group column `BC` for those 7 Test-IDs
(traceability, `AGENTS.md` §13.7). `id_registry.csv` checked first: `CAP-067` and `ADR-050` were free.

## M. External sources

- developer.android.com, "Fulfill common use cases while having limited package visibility" (`https://developer.android.com/training/package-visibility/use-cases`,
  fetched 2026-10-01 with `curl -sL`, text extracted from the HTML and read in place): *"When an app that targets Android 11 or higher uses an intent to start an
  activity in another app, the most straightforward approach is to invoke the intent and handle the ActivityNotFoundException exception if no app is
  available."*; *"It's recommended that your app catch and handle the ActivityNotFoundException if it occurs. Because the startActivity() method doesn't require
  package visibility to start another application's activity, you don't need to add a <queries> element to your app's manifest or make any changes to an existing
  <queries> element. This is true for both implicit and explicit intents that open a URL."*
- GitHub (HTTP status only, `curl -s -o /dev/null -w '%{http_code}' -L`): `…/blob/main/LICENSE` 200, `…/blob/main/README.md` 200, `…/issues` 200; negative
  control `…/blob/main/NO_SUCH_FILE.md` 404.

## N. Documentation changed (Phase F task 14)

`DECISIONS.md` ADR-050 (approved in chat, §G) and `id_registry.csv` (ADR-050, `CAP-067`); `ARCHITECTURE.md` §1 (no network; the links), §2.4 (Info's licence and
links, F-1, the balance steps), §6.0b (the Bluetooth-off cause; the proxies), §9 (links out), §12 (the adapter log lines); `APP_TESTPLAN.md` (header note
"updated 2026-10-01 for the `ai-sessions/0064` build", M3, K1, K4, **section R**, summary row); `TODO.md` ("Open after `ai-sessions/0064`"; the `0063` FEATURE item
and the `lint_docs` item marked done); `CHANGELOG.md`; `README.md` (status block); `ai-sessions/INDEX.md` (0064 row); `CAPTURE_BLUETOOTH_HCI_SNOOP.md`;
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`; `_sidebar.md`. **No `PROTOCOL.md` change** (none was proposed). `python3 scripts/ensure_footers.py` → exit 0 (one footer
added, this file); `python3 scripts/lint_docs.py` → **exit 0** after fixing three of this session's own references (the licence path without its `android/`
prefix); the `0063` prompt's pre-rename `CAP-066` folder is now in the "historical" bucket.

## O. Files read (in full / in part)

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `DECISIONS.md` (ADR-001 … ADR-049 with every Update),
  `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, this prompt, `ai-sessions/0063` RESULT, `APP_TESTPLAN.md`, the `CAP-066` skeleton (Appendix A of its EVENT-NOTES);
  Kotlin: `OpenControlNavHost.kt`, `SettingsMenu.kt`, `EqScreen.kt`, `BudsSettings.kt`, `SessionDiagnostics.kt`, `SessionLoss.kt`, `LinkEvaluation.kt`,
  `OsConnectionObserver.kt`, `BluetoothStateObserver.kt`, `BudsRepositoryImpl.kt`, `MainActivity.kt`, `ConnectionScreen.kt`, `BudsRepository.kt` (lines 1–60 of
  the interface body — the part around the change — and the rest by grep, see below), `AndroidManifest.xml` (`:app`), `app/build.gradle.kts`,
  `ui/build.gradle.kts`, `hardware/build.gradle.kts`, `gradle/libs.versions.toml`; tests `SettingsMenuTest`, `EqScreenTest`, `SessionDiagnosticsTest`,
  `SessionLossTest`, `BalanceSnapTest`, `Cap065Fixtures.kt`, `SettingsFixtures.kt` (lines 20–141).
- **In part:** `ai-sessions/INDEX.md` (header and the last rows); `CAP-066-FINDINGS.md` (header and §5–§12; §0–§4 not read — no item depends on them);
  `CAP-066-EVENT-NOTES.md` (title and Appendix A); the two `CAP-066` debug exports (E1 lines 265–300, E2 150–185) and system logs (grep of `BluetoothAutoOff`
  and 14:13:00–02); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (§9 header, the Group BB section, the Capture Index rows; §3 not read); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  (the rows of the Test-IDs named); `id_registry.csv` (grep); `BudsRepositoryImplTest.kt` (the balance, settings-write and I-7/T-1 loss tests, lines
  1240–1300 and 1670–1810); `SettingsCodecTest.kt` (lines 1–100, 151, 254); `LinkEvaluationTest.kt` (head); `README.md` (status block, §License); `LICENSE`
  (head; compared whole by `sha256sum`).
- **Order deviation, said plainly:** the prompt asks to read every Kotlin file in full *before* changing it. `ConnectionScreen.kt` and `BudsRepository.kt` were
  changed after reading only the parts around the change (`ConnectionScreen.kt` lines 95–135, 270–300, 400–470; the interface lines 40–70) and were read in
  full afterwards, before the final gate — nothing found that conflicts with the change. `BudsRepositoryImplTest.kt` and `SettingsCodecTest.kt` (tests) were
  changed after partial reads only.

## P. Open items

- Nothing of this build is hardware-verified — `CAP-067`.
- 🟡 F-4 may not silence StrictMode (`BluetoothLeAudio`'s own `CloseGuard`, `CAP-066-FINDINGS.md` §8).
- ⚪ F-3 assumes the app's adapter broadcast arrives within milliseconds of the system's (§D); a Bluetooth-off while the app is not visible is still decided on
  return (visibility-bound readings, unchanged rule).
- The channel-19 `17:7` frame has never been captured (the derived bytes are in the `CAP-067` skeleton and a labelled supplementary test).
- `AGENTS.md` §1 says "100% offline, no INTERNET"; ADR-050's reading ("the app itself makes no network request") is not written into `AGENTS.md` — a project-law
  edit the maintainer may want (deferred below; not done: `AGENTS.md` changes need the maintainer).

## Deferred documentation

Each item is also in `TODO.md` ("Open after `ai-sessions/0064`"), except the last, which is added there now:

- The `CAP-067` run and its analysis (FINDINGS, `PROTOCOL.md`/`ARCHITECTURE.md` notes for what it verifies).
- The bundled licence must follow `LICENSE` (a test enforces it).
- 🟡 F-4/StrictMode and ⚪ the F-3 timing assumption — settled by `CAP-067` BC-10/BC-11s.
- An optional dated note in `AGENTS.md` §1 pointing to ADR-050 (the maintainer's decision whether to edit project law).

## Commits

Committed and pushed after the maintainer's confirmation in chat (2026-10-01, `AskUserQuestion` "Commit": *"Commit and push (Recommended)"*):

- `511e39a` — feat(app): tab kept on rotation, balance steps, Bluetooth-off loss, profile proxies, Info licence and links (0064) — the code and tests of
  F-1 … F-6 together (several files carry two items, e.g. `MainActivity.kt` F-3 + F-6).
- `8171bbb` — docs: ADR-050 and the 0064 documentation.
- The commit that adds this file — docs: CAP-067 (Group BC) skeleton and session 0064 files (`CAP-067-EVENT-NOTES.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, `_sidebar.md`, `ai-sessions/INDEX.md`, this RESULT).

Not staged: `android/.kotlin/`, build directories.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0064_FEATURE_RESULT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0064_FEATURE_RESULT_2026_10_01
