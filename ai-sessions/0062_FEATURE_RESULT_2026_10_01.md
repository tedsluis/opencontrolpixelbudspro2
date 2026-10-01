# 0062_FEATURE_RESULT_2026_10_01.md — Build ANC `Get`-before-`Set` with the new wording, honest handling of an answer cut off by a closed Message Stream claim, and a settings menu behind a gear icon (Settings with dark mode, Debug, Info with firmware and build number); adapt the `CAP-066` skeleton to the new build

**Number:** 0062
**Category:** FEATURE
**Date:** 2026-10-01
**Title:** Implement the app changes the maintainer chose after `CAP-064` (`ai-sessions/0060` §D/§H) and `CAP-065` (`ai-sessions/0061` checkpoint "FEATURE"), with
real `CAP-064`/`CAP-065` bytes as fixtures, a green test/lint gate with mutation checks, and an updated `CAP-066` (Group BB) skeleton that re-tests them
**Status:** complete

---

## Summary (plain language — what you will see differently)

1. **Every ANC tap asks the Buds first.** On the ANC tab and on the Quick Settings tile, a tap now first asks the Buds whether a change is allowed right now
   and only then switches — so the "refused" answer of `CAP-064` (an 18-second-old "allowed") cannot happen any more. The tile switches to the mode **after the
   one the Buds report at that moment**, not after the one the screen showed.
2. **New wording.** "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." The tile
   says "Not allowed now" and toasts the same sentence when the Buds refuse.
3. **An answer cut off by another app** (Play services taking the channel) now says "The answer was cut off — another app took the Buds' channel. Tap Refresh
   to see the current mode." instead of "The Buds didn't respond in time.", and after a cut-off change the mode buttons are dimmed with a dot on the (i) until
   the Buds report again. Nothing is retried automatically.
4. **A gear replaces the bug icon** and opens **Settings** with three tabs: **Settings** (dark mode: System / On / Off, applied at once and remembered),
   **Debug** (the same Debug screen as before) and **Info** (the app's version and build — e.g. "build d541e00-dirty (2026-10-01)" — and the firmware of the Case,
   the Left bud and the Right bud, with the control channel).
5. The **Disconnect** label on the Connection card is now as legible in dark mode as the card's other text; debug builds log leaked resources (StrictMode);
   backups and device transfers exclude all app data; the session-loss log marks early verdicts "(provisional)".
6. **Not hardware-verified** — the `CAP-066` skeleton is adapted to test all of it (film the Info tab first).

## Progress

- **All phases done (0, A, B, C, D, E, F, G).** Repo at start: `HEAD` = `7e053ff` (prompt 0062); `git status --short` = `?? android/.kotlin/`;
  `git fetch && git log --oneline HEAD..origin/main` = `d541e00 docs: auto-regenerate sitemap.xml [skip ci]` (fast-forwarded with `git pull --ff-only`, new
  `HEAD` = `d541e00`); last commit under `android/` = `b65085a Thu Oct 1 08:37:32 2026 +0200`.
- Phase C (F-1/F-2/F-3), D (F-4/F-5/F-6), E (F-7, F-8, T-1, T-3) built and tested — §J. Phase F: gate §K, mutations §L, compliance §M, `CAP-066` §N,
  documentation §O. Commit: see "Commits".
- Scratchpad (re-create if gone): `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/8e33d9e3-9fa4-40f4-9883-7ba833e2e3c9/scratchpad` — the gate
  logs, counts.py (JUnit XML totals per module), mutate.py (the mutation driver, §L), the fixture extraction output (§D), swinfo_check.py and the decoder output
  (§F), the film frames k4_976/980/986 (§J F-7).

## C. Baseline gate (Phase 0 task 2)

`cd android && ./gradlew --offline clean` then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL** (2 m 53 s, exit 0; the
dependency cache was full, `--offline` worked). Test counts from the JUnit XML (`build/test-results/*/*.xml`, script `counts.py`):

| Module | Task | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|
| `:data` | testDebugUnitTest / testReleaseUnitTest | 1570 / 1570 | 0 | 0 | 0 |
| `:domain` | test | 27 | 0 | 0 | 0 |
| `:hardware` | testDebugUnitTest / testReleaseUnitTest | 52 / 52 | 0 | 0 | 0 |
| `:ui` | testDebugUnitTest / testReleaseUnitTest | 11 / 11 | 0 | 0 | 0 |

Lint (`*/build/reports/lint-results-debug.xml`): `:app` 1 warning (`DataExtractionRules`, `android:allowBackup` deprecated — the known `TODO.md` item, T-3),
`:ui`/`:data`/`:hardware` 0. Kotlin compiler warnings: 0 (`grep -c "^w: " baseline_gate2.log` → 0).

## D. Phase A task 3 — fixtures, re-derived from the captures

Commands (rule 4a), run in `captures/`: `tshark -r <log> -Y "bthci_acl.chandle==0x000b && btrfcomm.len>0 && frame.number==N" -T fields -e frame.number -e
frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data` (exit 0 each); the closes with `-Y "bthci_acl.chandle==0x000b && frame.number==N" … -e
btrfcomm.frame_type`. `p2p_dir` 0 = phone → Buds.

| Capture | Frame | Time (+02:00) | Dir | DLCI | Bytes | Meaning |
|---|---|---|---|---|---|---|
| `CAP-064` | 3200 | 10:07:47.360643 | Buds | 0x04 | `08 13 00 04 01 e8 e8 40` | the app's last `Notify` before the tile tap: Settable `e8` |
| `CAP-064` | 3299 | 10:07:54.688682 | Buds | 0x04 | `08 13 00 04 01 e8 00 20` | Play services' claim reads `00` (the app cannot see it) |
| `CAP-064` | 3433 | 10:08:14.443932 | phone | 0x04 | `08 12 00 14 01 e8 e8 20` + 16 × `00` | the tile's `Set` Off, no `Get` before it |
| `CAP-064` | 3440 | 10:08:14.579657 | Buds | 0x04 | `ff 02 00 03 02 08 12` | NAK, reason `0x02` |
| `CAP-064` | 3443 | 10:08:14.619365 | Buds | 0x04 | `08 13 00 04 01 e8 00 20` | `Notify` Settable `00` |
| `CAP-064` | 4091 | 10:09:49.104454 | phone | 0x04 | `08 11 00 00` | `Get` |
| `CAP-064` | 4103 | 10:09:49.393403 | Buds | 0x04 | `08 13 00 04 01 e8 e8 20` | `Notify` `e8` (AY-3a) |
| `CAP-065` | 5453 | 11:14:33.198711 | phone | 0x05 | `08 11 00 00` | `Get` (BA-5) |
| `CAP-065` | 5465 | 11:14:33.437690 | Buds | 0x05 | `08 13 00 04 01 e8 00 20` | `Notify` `00` |
| `CAP-065` | 6322 | 11:16:41.550382 | phone | 0x05 | `08 11 00 00` | `Get` (BA-8) |
| `CAP-065` | 6334 | 11:16:41.787502 | Buds | 0x05 | `08 13 00 04 01 e8 e8 20` | `Notify` `e8` |
| `CAP-065` | 10790 | 11:22:17.882051 | phone | 0x05 | `08 12 00 14 01 e8 e8 08` + 16 × `00` | `Set` Noise cancellation (F7) |
| `CAP-065` | 10799 | 11:22:18.003074 | Buds | 0x05 | `ff 01 00 06 08 12 01 e8 e8 08` | ACK |
| `CAP-065` | 2613 | 11:10:05.980925 | phone | 0x05 | frame type `0x2f` | `SABM` (the tile's claim) |
| `CAP-065` | 2640 | 11:10:06.345675 | phone | 0x05 | `08 12 00 14 01 e8 e8 08` + 16 × `00` | the tile's `Set` Noise cancellation |
| `CAP-065` | 2649 | 11:10:06.493974 | phone | 0x05 | frame type `0x43` | **`DISC`** — the claim closed |
| `CAP-065` | 2651 | 11:10:06.530139 | Buds | 0x05 | `ff 01 00 06 08 12 01 e8 e8 08` | the ACK, after the close |
| `CAP-065` | 2654, 2655 | 11:10:06.597405 / .598258 | Buds | 0x05 | `08 13 00 04 01 e8 e8 08` | `Notify` after the close |
| `CAP-065` | 10321 | 11:21:35.069919 | phone | 0x05 | `08 11 00 00` | snapshot `Get` |
| `CAP-065` | 10341 | 11:21:35.232662 | Buds | 0x05 | `03 03 00 03 64 e4 ff` | battery (before the close) |
| `CAP-065` | 10344 | 11:21:35.235814 | phone | 0x05 | frame type `0x43` | **`DISC`** |
| `CAP-065` | 10356 | 11:21:35.408228 | Buds | 0x05 | `08 13 00 04 01 e8 00 20` | `Notify` after the close |

**Correction to the prompt (the capture wins):** in `CAP-065` the Message Stream ran on **DLCI 0x05**, not 0x04 (the Buds opened the RFCOMM multiplexer, so
DLCI numbers are session-local — `CAP-065-FINDINGS.md`'s parser already reads "DLCI 4/5"); the app's own channel id for the Message Stream stays its
constant `Dlci.FAST_PAIR_MESSAGE_STREAM` (= 4, an app channel key, resolved by SDP UUID). Every byte above matches the prompt and the FINDINGS.

## E. Phase A task 4 — current behaviour and planned change, per item

**F-1 (`Get` before every `Set`).** Today `BudsRepositoryImpl.setAncMode` (`BudsRepositoryImpl.kt:692–711`): if `_ancAvailability` ≠ `NOT_ALLOWED` (i.e.
`ALLOWED` **or `UNKNOWN`**) the claim sends the `Set` directly (`:693` → `ancSetOnClaim`, `:717–732`); only `NOT_ALLOWED` sends `08 11` first
(`sendAncGetAndAwait`, `:735`) and stops with `AncNotAllowed` on a `00` answer. `AncAvailability` is set from every `Notify` (`handleRoutedFrame`, `:487–497`,
`updateAncAvailability` `:316–322`) and reset to `UNKNOWN` at each Connect (`openSession`, `:628`). The tile (`AncTileService.onClick`, `AncTileService.kt:110`)
computes `AncMode.nextForTile(latestMode)` from the **shown** mode before the claim. **Plan:** `setAncMode` always claims, sends `08 11`, waits for that claim's
`Notify` (availability + mode, one new replay-0 flow), sends `AncNotAllowed` on `00` and nothing more, otherwise `ancSetOnClaim(mode)` in the same claim. A new
repository call for the tile, `stepAncMode(next: (AncMode?) -> AncMode)`, does the same but computes the mode from that fresh `Notify`
(`AncMode.nextForTile(fresh)`). Tests: §B of the prompt (a)–(d), (h) with the fixtures of §D.

**F-2 (wording).** `ANC_NOT_ALLOWED_TEXT` (`ConnectionScreen.kt:523`) "ANC can only be changed while you wear the Buds."; `ancNotAllowedLine`
(`AncScreen.kt:144–145`); tile toast `AncTileService.kt:152`; tile subtitle "Only while worn" (`AncTile.kt:39`). Plan: the texts chosen at the checkpoint.

**F-3 (answer cut off).** Transport: an on-demand channel that dies on its own (reader EOF/`IOException`) is reported on `BudsTransport.channelClosed`
(`RfcommBudsTransport.kt:246–264`, `reportChannelClosed`); a `send` on a closed socket returns `ChannelLost` (`:302–306`). Repository today: the `channelClosed`
collector only resets the codec and the claim's Model ID (`BudsRepositoryImpl.kt:437–441`); a `Set`/`Get` wait (`sendAndAwait`, `:1258–1272`) ignores the close
and ends as `Timeout` after 1 s / 2 s — `CAP-065` 11:21:35 showed "The Buds didn't respond in time." (`ConnectionScreen.kt:399`), and the tile `Set` of
11:10:06 kept OFF silently (the tile toasts only `AncNotAllowed`). `withMessageStream` (`:1122–1164`) retries the action once when it returns
`ChannelLost` — so reusing `ChannelLost` for a cut-off would send the `Set` a second time. `FakeBudsTransport` can script it: `onSent` runs inside `send`, and
`emitChannelClosed(4)` removes the channel and emits — so "write, close, the answer never arrives" is one `onSent` hook. **Plan:** the claim's waits also listen
to `channelClosed` for the Message Stream (subscribed before the send); a close first ⇒ a distinct result (checkpoint: `AnswerCutOff` recommended), not
retried; after a cut-off `Set` the repository marks the shown mode "not confirmed" (a new `ancModeUnconfirmed` flow) until the next `Notify` or ACK.

**F-4 (settings menu).** `OpenControlNavHost.kt:247–302`: top bar title "OpenControl" / "Debug", the bug `IconButton` (`:258–264`) navigates to `Routes.DEBUG`, a
full-screen destination outside the pager; back pops it. `DebugScreen.kt` takes `debugModeEnabled`, `onDebugModeChanged`, `unidentifiedFrames`, `onExportLog`.
**Plan:** the gear (`Icons.Filled.Settings`, already in `material-icons-core`, no new dependency) opens `Routes.SETTINGS`, a full-screen destination with a
`PrimaryTabRow` of three tabs; the Debug tab renders `DebugScreen` unchanged; back returns to the tab it came from.

**F-6 (dark mode).** `OpenControlTheme(darkTheme = isSystemInDarkTheme())` (`OpenControlTheme.kt:34`), called with no argument in `MainActivity.kt:482`.
Persistence: `DebugSettingsStore` (`:data/settings`, a `preferencesDataStore(name = "opencontrol_settings")` delegate private to its file). **Plan:** a `DarkMode`
enum (`SYSTEM`, `ON`, `OFF`) in `:domain`; the same DataStore file (the delegate moved to a shared internal top-level so two stores never open the file twice), a
new key; `MainActivity` collects it and passes `darkTheme` — applied at once, no restart.

**F-5 (Info).** The announcement is parsed by `SoftwareInfo.firmwareStrings` (`SoftwareInfo.kt:35–47`) into **distinct** strings (per-entry structure lost),
carried as `RoutedFrame.MaestroHello(channelId, firmware)` (`CodecRouter.kt:60`, `:254`) into `DeviceInfo(firmware)` (`DeviceInfo.kt`), shown today as the
battery card's (i) line "Firmware: …" (`ConnectionScreen.kt:306`). **Plan:** a per-entry parse (index, firmware string) kept beside the distinct list (Safe
Mode keeps using the distinct list), the announced channel in `DeviceInfo`; `:app` gets `buildFeatures.buildConfig` with `GIT_COMMIT` (short hash, "-dirty" when
tracked files differ, "unknown" without git) computed locally with `providers.exec`. Fixtures: `Cap061` 1508 (existing) and a `CAP-065` announcement.

**F-7 (contrast).** `ConnectionStateCard` (`ConnectionScreen.kt:203–255`): container `primaryContainer`, content `onPrimaryContainer` while the session is open,
but the "Disconnect" action is a `TextButton`, whose label uses **`primary`**, not the card's content colour. **Plan:** the card's action label takes the card's
content colour (`onPrimaryContainer` / `onErrorContainer`), and a JVM test checks WCAG contrast ≥ 4.5:1 for that pair in the light and dark schemes
(Material baseline and the Robolectric dynamic schemes).

**F-8 (leaks).** Read in full: `RfcommBudsTransport` (every failed `connect()` closes its socket, `:200–216`; `closeAll`, `closeChannel`, `reportChannelClosed` close
theirs) and `RfcommSocket.kt`. **No unclosed socket found by reading.** 🟡 The three `CloseGuard` lines sit next to `BluetoothSocket: close() … Already closed`
(`CAP-065-FINDINGS.md` §0), i.e. a finalized object inside the framework (e.g. a `ParcelFileDescriptor` the failed `connect()` created) is a candidate the code
cannot close itself — StrictMode's stack will say. **Plan:** `StrictMode.setVmPolicy(…detectLeakedClosableObjects().penaltyLog())` when `BuildConfig.DEBUG`.

**T-1 (loss-cause log).** `reclassifyLoss` (`BudsRepositoryImpl.kt:374–381`) logs every change of the classified cause; `classifySessionLoss`
(`SessionLoss.kt:73–92`) can still change while a "not connected" reading may arrive (until loss + 1 s, `LINK_LOST_AFTER_MS`). **T-2:** `OpenControlApplication`
starts the service for `Connecting`/`Discovering`/`Ready` (`OpenControlApplication.kt:79–84`). **T-3:** `AndroidManifest.xml` has `android:allowBackup="false"`
and no `dataExtractionRules`.

## F. Phase A task 5 — which announcement entry is which component

- **Code (🟢 FACT, code existence; mechanical search, ADR-017):** `qjb` (the `GetSoftwareInfo` response type, `fux.java:57`) field 4 is `qie`, whose fields 1/2/3
  are Java `c`/`d`/`e` (`qie.java:29` schema string, objects `{"b","c","d","e"}`), each a `qid` {1: `c` string, 2: `d` string} (`qid.java:29`). The app's response
  handler `gaa.d(qjb)` (`gaa.java:920–1030`) copies `qie.c/d/e` into `gdm.c/d/e` through `fzr` case 1 (`fzr.java:53–76`: `qid.c → gdd.c`, `qid.d → gdd.d`,
  identity). The official app's firmware screen `OtaFragment` binds `this.e` = `key_left_bud_firmware_version_pref`, `this.f` = `key_right_bud_…`, `this.ah` =
  `key_case_firmware_version_pref` (`OtaFragment.java:88–90`) and observes `hfh.b → hfb(11)`, `hfh.c → hfb(12)`, `hfh.d → hfb(13)` (`:108–110`). In smali
  (`apktool-output/smali_classes2/`, JADX could not decompile these): `hfb` index 11 → `pswitch_8` sets `OtaFragment.ah` (**Case**), 12 → `pswitch_7` sets `.e`
  (**Left**), 13 → `pswitch_6` sets `.f` (**Right**) (`hfb.smali:834–908`, table `:1422–1442`); `hfh.b = gyg(20)` → the default branch reads **`gdm.c`**
  (`gyg.smali:47–60`, `:1040–1060`), `hfh.c = hff(1)` → `pswitch_12` reads **`gdm.d`**, `hfh.d = hff(0)` → `pswitch_13` reads **`gdm.e`** (`hff.smali:941–1192`,
  table `:1194–1216`); each shown string is `hfh.b(gdd)` = field 2 (`gdd.d`, the firmware string) or, if empty, field 1 (`hfh.java:52–60`).
  **So the official app shows announcement entry 1 as the Case, entry 2 as the Left bud, entry 3 as the Right bud.**
- 🟡 HYPOTHESIS: the Buds' **unsolicited** announcement (`call_id 0xFFFFFFFF`) uses the same layout — it is the same method's response type (`qjb`), but whether
  the app routes the unsolicited packet through `gaa.d` was not traced.
- **Wire (🟢 FACT, a checked negative):** `for f in captures/*/*btsnoop_hci*.log; do python3 scripts/pwrpc_decode.py "$f" | grep GetSoftwareInfo; done`
  (57 logs) → **191** announcements, every one with entries `1`, `2`, `3`, and **all 573 entries** read `(1779298694, release_5.203)` (`swinfo_check.py`): the
  entries never differ, so the wire cannot confirm or refute the mapping. Positive control: the same command lists the announcements, e.g. `CAP-001` 1346.
  Limitation: `pwrpc_decode.py` reads DLCI 0x02 only, so `CAP-064`/`CAP-065` sessions with MAESTRO on DLCI 0x03 are not in the count.
- (Not the same order as the component serials of `GetHardwareInfo` — `CAP-036` frame 1423: `…EC…`, `…DR…`, `…DL…` — a different message type, `qiv`.)
- **Draft `PROTOCOL.md` §2.2a Update for the checkpoint:** in the preview of the F-5 question.

## G. Phase A task 6 — `transport.send` call sites

The design adds **no** new call site and no new message type. Existing sites: `ancSetOnClaim` (`AncFrame.Set`), `sendAncGetAndAwait` (`AncFrame.Get`), `sendRing`,
and the MAESTRO sends (`setEqGains`, `readEq`, `writeSetting`, `readSettings`, `subscribeRuntimeInfo`). F-1 makes the existing `Get` run first on every ANC tap's
claim — covered by ADR-021 (opcode), ADR-022 (trigger) and ADR-032 item 5 ("each claim re-queries ANC (`Get`)"); the "Unknown or non-zero ⇒ `Set` directly" rule it
replaces is an app design detail recorded in `ARCHITECTURE.md` §3.1 (`ai-sessions/0054` I-1), not an ADR. **Not an ADR matter.** F-3 adds no send and no retry.

## H. Files read

- **In full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (the changelog rows of §8 and two long table rows of §7 read cut at
  400 characters), `DECISIONS.md` (ADR-001 … ADR-049 with every Update), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/0060` and `0061` RESULTs, this
  prompt; `BudsRepositoryImpl.kt`, `BudsRepository.kt`, `BudsTransport.kt`, `RfcommBudsTransport.kt`, `RfcommSocket.kt`, `FakeBudsTransport.kt`, `BudsError.kt`,
  `DeviceInfo.kt`, `AncTile.kt`, `AncTileTest.kt`, `AncTileService.kt`, `AncScreen.kt`, `ConnectionScreen.kt`, `ConnectionBanner.kt`, `Details.kt`, `DebugScreen.kt`,
  `OpenControlNavHost.kt`, `OpenControlTheme.kt`, `MainActivity.kt`, `AppUiSession.kt`, `OpenControlApplication.kt`, `DebugSettingsStore.kt`,
  `SessionDiagnostics.kt`, `SoftwareInfo.kt`, `Cap061Fixtures.kt`, `BatteryCardTest.kt`, `PullActionTest.kt`, `app/build.gradle.kts`, `ui/build.gradle.kts`,
  `data/build.gradle.kts`, `libs.versions.toml`, `AndroidManifest.xml` (`:app`).
- **In part:** `CAP-064-FINDINGS.md` §0, §2, §3, §6, §9, §10; `CAP-065-FINDINGS.md` §0, §2, §3, §9, §10; `REVERSE_ENGINEERING.md` (the `fxm`, `qjb`/`qie` entries);
  `SessionLoss.kt` (`classifySessionLoss`); `CodecRouter.kt`, `OpenControlIcons.kt` (grep); the JADX/smali files of §F (the lines cited).
- **Not yet read (later phases):** `APP_TESTPLAN.md`, the `CAP-066` skeleton, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BB), `id_registry.csv`,
  `BudsRepositoryImplTest.kt`, the other fixture files, `EqScreenTest.kt`, `ai-sessions/INDEX.md`.

## I. Phase B — checkpoint answers (chat 2026-10-01, `AskUserQuestion`, verbatim; each question showed the draft text in its preview)

| Question | Answer |
|---|---|
| F-1/F-2 "ANC Get+Set" (the `0060` approval, confirmed here) | *"As drafted (Recommended)"* — ANC tab note "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." ((i): "(checked HH:MM:SS)" after "ear"); refused tap / tile toast "The Buds don't allow changing noise control right now (usually because no bud is in an ear)."; tile subtitle "Not allowed now"; the tile's next mode from the fresh `Notify` |
| F-3 "Cut-off" | *"New AnswerCutOff (Recommended)"* — `BudsError.AnswerCutOff(0x04)`, never retried; text "The answer was cut off — another app took the Buds' channel. Tap Refresh to see the current mode." (ANC tab, Connection card notice, tile toast); after a cut-off `Set` the last confirmed mode dimmed (0.6) + the (i) dot, (i) line "Not confirmed: the answer to the change at HH:MM:SS was cut off — the Buds may have switched. Tap Refresh."; cleared by the next `Notify` or ACK; a timeout with the channel open stays `Timeout`; *Refresh battery*: a cut-off `Get` still counts if the battery burst arrived |
| F-4/F-6 "Settings UI" | *"As drafted (Recommended)"* — gear "Settings" in place of the bug icon; full-screen "Settings" with tabs Settings / Debug / Info; back returns to the tab it came from; Dark mode: "System (follows Android)" (default), "On", "Off", persisted in the existing DataStore, applied at once |
| F-5 "Info tab" | *"Promote + label (Recommended)"* — the draft `PROTOCOL.md` §2.2a Update approved as 🟢 FACT (code) with the 🟡 caveat (text in §F/§K); Info: "App: 0.1.0-dev, build <hash> (<commit date>)", firmware lines "Case / Left bud / Right bud", "Control channel: N", "Not connected yet"; no build-time stamp |
| F-7/F-8 | *"Both as drafted (Recommended)"* — the card's action label in the card's content colour + contrast test; StrictMode `detectLeakedClosableObjects().penaltyLog()` in debug builds only; no code fix (none found) |
| T-1 "T-1 log" | *"Mark provisional (Recommended)"* |
| T-2 "T-2 notif" | *"Leave as is (Recommended)"* |
| T-3 "T-3 manifest" | *"Allow, build T-3 (Recommended)"* — a manifest change is allowed for `dataExtractionRules` and the comment; no new permission |
| CAP-066 | *"Apply as outlined (Recommended)"* — Info tab filmed first, gear in S2/L3/X1, BB-1…BB-4 with `08 11` first, new BB-4t (tile from the fresh `Notify`), watch-only BB-15 (cut-off), K4 with the Settings tab, new BB-16 (Debug tab, back) |


**Notes on the answers:** the (i) line's "(checked HH:MM:SS) after 'ear'" is written "…in an ear; checked HH:MM:SS)…" (one parenthesis). The prompt's
"S2, L3, X1" steps: the `CAP-066` skeleton has no S2 or X1 step (they are `APP_TESTPLAN.md`/`CAP-065` names); the gear replaces the bug icon in L3, A.3 and
`APP_TESTPLAN.md` L1/O1/O3/X1 (correction recorded here).

## J. What was built, per item (where)

| Item | Built | Files | Tests |
|---|---|---|---|
| F-1 | `setAncMode` and the new `stepAncMode` both run `setAncModeAfterGet`: Safe-Mode gate (nothing at all is sent in Safe Mode, not even the `Get`), `08 11`, wait for that claim's `Notify` (`_ancNotifies`, availability + mode); `00` ⇒ `AncNotAllowed`, nothing more; else `ancSetOnClaim(choose(fresh mode))`. The tile calls `stepAncMode(AncMode::nextForTile)` | `BudsRepository.kt`, `BudsRepositoryImpl.kt:~700–770`, `AncTileService.kt` | F-1 (a)–(d), (d'), (h), "a failed claim or an unanswered Get"; 8 older tests adapted (the default scripted Buds answer `08 11` with `CAP-065` 6334) |
| F-2 | `ANC_NOT_ALLOWED_TEXT` (public, also the tile's toast), `ancNotAllowedLine`, tile subtitle "Not allowed now" | `ConnectionScreen.kt`, `AncScreen.kt`, `AncTile.kt` | `AncScreenTest` (2), `AncTileTest` |
| F-3 | `BudsError.AnswerCutOff(channelId, detail)`; `sendAndAwaitOnClaim`/`ClaimWait` watch `channelClosed` (subscribed before the send); an answer within `CUT_OFF_GRACE_MS` = 100 ms of the close still counts; never retried (unlike `ChannelLost`); `ancModeUnconfirmedAt` = the cut-off `Set`'s send time, cleared by a `Notify` with a known mode or an ACK; *Refresh battery* counts a burst that came before the close; UI: dimmed mode row + (i) dot + "Not confirmed: …"; tile toast `ANSWER_CUT_OFF_TEXT` | `BudsError.kt`, `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `ConnectionScreen.kt`, `AncScreen.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`, `AncTileService.kt` | F-3 (e), (f), (g), the grace test (labelled hand-ordered), the Refresh test; `AncScreenTest` (3) |
| F-4 | Gear (`Icons.Filled.Settings`) → `Routes.SETTINGS`, `SettingsMenuScreen` (`TabRow` Settings/Debug/Info, tab kept with `rememberSaveable`); Debug tab = `DebugScreen` unchanged; the unused bug icon removed | `OpenControlNavHost.kt`, `SettingsMenu.kt` (new), `DebugScreen.kt` (comments), `OpenControlIcons.kt` | `SettingsMenuTest` (gear, tabs, Debug, back to ANC) |
| F-5 | `GIT_COMMIT` (short hash, "-dirty" if tracked files differ, "unknown" without git/repo — checked with `GIT_DIR=/nonexistent-dir`, positive control `d541e00-dirty`) and `GIT_COMMIT_DATE` via `providers.exec`; `SoftwareInfo.entries` (index + firmware), `MaestroHello.entries`, `DeviceInfo(entries, maestroChannel, announcedAtMillis)`; Info lines with the approved labels | `app/build.gradle.kts`, `SoftwareInfo.kt`, `CodecRouter.kt`, `DeviceInfo.kt`, `BudsRepositoryImpl.kt`, `SettingsMenu.kt`, `MainActivity.kt` | `CaseBatteryCodecTest` (`CAP-061` 1508, `CAP-065` 1883 real frames; one labelled hand-built), repository test (1883 → deviceInfo), `SettingsMenuTest` (Info, "not connected yet", labels) |
| F-6 | `DarkMode` (`:domain`), `DarkModeSettingsStore` (key `dark_mode` in the existing `opencontrol_settings` file; the delegate moved to `SettingsDataStore.kt` so the file has one DataStore), `OpenControlTheme(darkTheme = darkMode.isDark(isSystemInDarkTheme()))` | `DarkMode.kt`, `SettingsDataStore.kt`, `DarkModeSettingsStore.kt`, `DebugSettingsStore.kt`, `RepositoryModule.kt`, `MainActivity.kt` | `SettingsMenuTest` (On/Off/System change the scheme; stored value read back) |
| F-7 | `connectionCardColors(scheme, session)`; the Disconnect `TextButton` takes the card's content colour. Film evidence (`CAP-065` frame at film t = 980 s, overlay 11:23:50, `ffmpeg -ss 980 -i CAP-065-recording.mp4 -frames:v 1`, WCAG luminance of sampled pixels, PIL): Disconnect label ≈ **1.23:1** against the card (201,137,134) vs (183,121,120); the card's "App control: ready" text ≈ 4.24:1 — a camera image, approximate | `ConnectionScreen.kt` | `CardContrastTest` (≥ 4.5:1 for baseline and Robolectric dynamic light/dark; the action = `on…Container`) |
| F-8 | `StrictMode.setVmPolicy(detectLeakedClosableObjects().penaltyLog())` only when `BuildConfig.DEBUG`; release `BuildConfig.DEBUG = false` (generated file checked). No leak fix (none found by reading, §E) | `OpenControlApplication.kt` | not unit-testable without adding a test dependency to `:app` (none added) |
| T-1 | `lossCauseLine(cause, provisional)`; provisional = not "link went down" and before loss + 1 s | `SessionDiagnostics.kt`, `BudsRepositoryImpl.kt` | `SessionDiagnosticsTest` (+1), repository test (the `CAP-064` §6 #1 sequence, 3 lines) |
| T-3 | `res/xml/data_extraction_rules.xml` (cloud-backup and device-transfer exclude every domain), `android:dataExtractionRules`, manifest comment on `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | `AndroidManifest.xml` (`:app`) | `:app` lint: 1 → 0 issues |
| T-2 | not built (the maintainer's choice) | — | — |

## K. Gate (Phase F task 11)

`./gradlew --offline clean`, then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → BUILD SUCCESSFUL (2 m 56 s, exit 0). Re-run after the
mutations and the documentation (same command): BUILD SUCCESSFUL, exit 0, the same counts, lint 0, 0 compiler warnings.

| Module | Baseline tests | Final tests | Failures | Lint baseline → final | Compiler warnings |
|---|---|---|---|---|---|
| `:data` (debug / release) | 1570 / 1570 | 1581 / 1581 | 0 | 0 → 0 | 0 |
| `:domain` | 27 | 27 | 0 | — | 0 |
| `:hardware` (debug / release) | 52 / 52 | 52 / 52 | 0 | 0 → 0 | 0 |
| `:ui` (debug / release) | 11 / 11 | 24 / 24 | 0 | 0 → 0 | 0 |
| `:app` | — (no unit tests) | — | — | 1 (`DataExtractionRules`) → 0 | 0 |

No new suppression, no new opt-in (`TabRow` used instead of the experimental `PrimaryTabRow`).

## L. Mutation checks (Phase F task 12)

Driver: mutate.py in the scratchpad — apply one exact replacement, run `./gradlew --offline --max-workers=2 :<module>:testDebugUnitTest`, restore, compare
sha256 (one at a time). Every mutation compiled and failed at least one test; every file was restored byte-identical (`BudsRepositoryImpl.kt` sha256
`7099a4c3…6854936` before and after each).

| # | Mutation | Module | Failed tests | Example failing test |
|---|---|---|---|---|
| M1 | the `Set` is sent before the `Get`/`Notify` | `:data` | 13 / 1581 | "setAncMode sends the Get, then the exact Set bytes…" |
| M2 | the `Set` is sent after a `00` `Notify` | `:data` | 2 | F-1 (b), F-1 (c) |
| M3 | `UNKNOWN` skips the `Get` | `:data` | 6 | F-1 (d) |
| M4 | a cut-off is reported as `Timeout` | `:data` | 3 | F-3 (e), (f), the Refresh test |
| M5 | a cut-off `Set` applies the requested mode | `:data` | 1 | F-3 (e) |
| M6 | the tile uses the shown mode, not the fresh one | `:data` | 1 | F-1 (h) |
| M7 | the dark-mode setting is ignored | `:ui` | 2 / 24 | "dark mode On, Off and System change the scheme at once" |
| M8 | Info labels a wrong index (1 ↔ 2) | `:ui` | 1 | "the entry labels are the official app's mapping…" |
| M9 | F-7: the card action back to `primary` | `:ui` | 1 | "the action takes the card's own content colour" — the ≥ 4.5:1 test itself passes with `primary` in the Robolectric/baseline schemes; the low contrast is a device-scheme effect seen on film |
| M10 | T-1: never provisional | `:data` | 1 | "the loss cause lines inside the window are marked provisional" |

## M. Compliance (Phase F task 14)

- No `INTERNET`: `grep -rn "android.permission.INTERNET" android --include=AndroidManifest.xml` (sources) → exit 1; in the merged manifest the only match is
  the comment "No INTERNET permission anywhere…" (positive control: `BLUETOOTH_CONNECT` found). The five `uses-permission` entries are unchanged.
- `git diff -- '*AndroidManifest.xml'`: only `android:dataExtractionRules` and the comment (T-3, allowed). `git diff -- android/gradle/libs.versions.toml`: empty.
  `*.gradle.kts`: only `app/build.gradle.kts` (the `BuildConfig` block).
- `transport.send` call sites in the diff: none new — the `Get` send moved from `sendAndAwait` to `sendAndAwaitOnClaim` (same bytes, `AncFrame.Get`). Nothing on
  DLCI 0x08 (`git diff -U0 -- android | grep "0x08\|GSND"` → nothing). Every ANC `Set` still passes `writeGate` (twice: before the `Get` and in `ancSetOnClaim`).
- AGPL header (`SPDX-License-Identifier: AGPL-3.0-or-later`) on all 9 new Kotlin files. No reflection, no hidden API, no new permission, no new dependency,
  no timer loop (the only new wait is one bounded 100 ms grace per cut-off event).

## N. `CAP-066` changes (Phase F task 13)

`captures/CAP-066-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BB/CAP-066-EVENT-NOTES.md`: status line; purpose VIII; A.0 (build from the Info tab), new P7 (film the Info
tab); BB-1…BB-3 with the new wording, BB-3 the tile subtitle; new BB-4t (tile from the fresh `Notify`); a "Refuted if" for every ANC tap (`08 12` without `08 11`
in the same claim, or after a `00`); BB-8's wire column with the `Get` first; new section VIII — BB-16 (menu, Debug tab, back), K4d (dark mode via the Settings
tab and Android, the Disconnect label filmed), BB-15 (cut-off, watch only); L3 and A.3 via the gear; the analysis checklist. Sections II, III (BB-9), IV, V,
VI and VII unchanged otherwise (destructive steps still last). `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group BB text and Capture Index row, `id_registry.csv` `CAP-066`
row updated.

## O. Documentation (Phase F task 15)

`ARCHITECTURE.md` §1 diagram, §2, §2.4, §3.1 ANC row, §6.0b, §7, §9, §12; `APP_TESTPLAN.md` (header note, P1, D2, F1, F6, F8, G3, K4, L, O1, O3, O12, new
section Q, summary); `PROTOCOL.md` §2.2a 2026-10-01 Update (approved in chat) + §8 row; `REVERSE_ENGINEERING.md` `qjb` entry dated Update (the code trace);
`TODO.md` ("Open after `ai-sessions/0062`", done items struck); `CHANGELOG.md`; `README.md` (status, the permission sentence); `ai-sessions/INDEX.md` (0062 row);
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `id_registry.csv`, the `CAP-066` skeleton. **No** `DECISIONS.md` change (no ADR needed, §G). `python3 scripts/ensure_footers.py`
→ exit 0 (added this file's footer); `python3 scripts/lint_docs.py` → see §P.

## P. External sources consulted (fetched 2026-10-01)

- AOSP `frameworks/base/core/java/android/os/StrictMode.java` (`refs/heads/main`, read with `curl …?format=TEXT | base64 -d`), `VmPolicy.Builder
  .detectLeakedClosableObjects()`: *"Detects when an {@link java.io.Closeable} or other object with an explicit termination method is finalized without having
  been closed."* (The developer.android.com reference page returned only its navigation to the fetch tool.)
- AOSP `frameworks/support` `compose/material3/…/ColorScheme.kt` (`androidx-main`): *"@property onPrimaryContainer The color (and state variants) that should
  be used for content on top of [primaryContainer]."* (m3.material.io returned only its title.)
- W3C WCAG 2.1, Success Criterion 1.4.3: *"The visual presentation of text and images of text has a contrast ratio of at least 4.5:1, except for the
  following:"*; contrast ratio *"(L1 + 0.05) / (L2 + 0.05)"*.

## Q. Open items

- Hardware verification of everything (`CAP-066`). The StrictMode output names the leaked object (F-8). T-1's last provisional line stays when no later
  reading comes. 🟡 whether the unsolicited announcement uses the app's entry layout. `TabRow` vs `PrimaryTabRow` on the next BOM bump. All in `TODO.md`.
- Not in scope, untouched (prompt §2): nothing new on the wire, no auto-connect on lid-open, no address derivation from the channel, no Safe-Mode greying, no
  dependency.

## Deferred documentation

Each item is also in `TODO.md` ("Open after `ai-sessions/0062`"):

- Run `CAP-066` on this build (the skeleton is ready) and analyse it.
- Read the first debug run's StrictMode lines (F-8) and name the leaked object.
- T-1's known limit (a provisional last line).
- The 🟡 of the `PROTOCOL.md` §2.2a 2026-10-01 Update (the unsolicited announcement's layout).
- `TabRow`/`PrimaryTabRow` re-check with the next material3 BOM.

## Commits

Committed and pushed after the maintainer's confirmation in chat (2026-10-01, "Commit and push (Recommended)"):

- `ad0c4ba` — feat(app): ANC Get before every Set, cut-off answers, settings menu with dark mode and Info (code, tests, `BuildConfig`, manifest).
- `92afb62` — docs: session 0062 (documentation, `PROTOCOL.md`/`REVERSE_ENGINEERING.md` Updates, the `CAP-066` skeleton, this RESULT), plus the follow-up
  commit that records these hashes (`git log -1 --format=%h -- ai-sessions/0062_FEATURE_RESULT_2026_10_01.md`).

Not staged: `android/.kotlin/`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0062_FEATURE_RESULT_2026_10_01.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0062_FEATURE_RESULT_2026_10_01
