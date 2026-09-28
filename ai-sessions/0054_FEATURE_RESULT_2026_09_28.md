# 0054_FEATURE_RESULT_2026_09_28.md — Build the `CAP-063` improvements I-1, I-2, I-3, I-5 and I-4, and write the Group AZ re-test skeleton

**Number:** 0054
**Category:** FEATURE
**Date:** 2026-09-28
**Title:** Implement the improvements the maintainer chose in `ai-sessions/0053` (I-1, I-2, I-3, I-5, I-4), with real `CAP-063` bytes as fixtures, a green gate with mutation checks, and a Group AZ capture skeleton (`CAP-064`)
**Status:** complete

## Progress (final)

- Phases 0, A, B (checkpoint in this chat, §6), C (I-1), D (I-2), E (I-3, I-5, I-4), F (gate §9, mutations §10, compliance §11) and G (skeleton §12,
  documentation §13) are done. No subagent was used; every read and write was done in this session.
- Git at start: `55f1e62`, clean; `origin/main` one commit ahead (`59adc60 docs: auto-regenerate sitemap.xml [skip ci]`) — rebase before pushing.
- Scratchpad (session-local, not in the repo): `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/196c928e-89c4-47c4-8656-d9716fa05122/scratchpad`
  — mutate.py, mutations.txt, final.log, baseline.log, lint_docs.txt.
- Waiting only for the maintainer's commit/push decision (prompt task 16).

## 1. Plain-language summary (what you will see differently)

1. **ANC after putting the Buds back in (I-1).** The ANC buttons no longer go grey when the Buds say they are not worn. The screen says "ANC can only be
   changed while you wear the Buds (checked 16:06:11). Tapping a mode checks again first." A tap asks the Buds once more (the usual short claim of the
   Message Stream): if they now allow it, the mode switches in the same step; if not, nothing is sent and only the "checked" time moves. The Quick Settings
   tile does the same and shows a short message if the Buds still refuse. This fixes `CAP-063`'s six minutes of disabled ANC after re-wearing.
2. **Why the session ended while the app was in the background (I-2).** When the app comes back, the first reading of Android's own Bluetooth state
   decides the text: "Android no longer showed the Buds connected when you returned to the app — …" or "The app's channel was closed while the app was in
   the background; Android showed the Buds connected when you returned. …" — instead of the vague "The Maestro channel (equalizer) was closed".
3. **Balance "Centre" (I-3).** Letting go of the slider within 3 steps of the middle now sets exactly "Centre" (the knob jumps to the middle); the Buds get
   the same bytes the official app sends for the centre.
4. **Battery lines right after Connect (I-4).** For the second or two before the Buds report again, the old lines are marked: "Left: 100% — last seen 16:00:17
   — charging in the case (16:00:17, last connection)".
5. **"Digital assistant" (I-5).** A note under "Press and hold": it needs an assistant app that supports headphones (for example the Google app); without one,
   holding the bud may only play a tone.
6. Nothing new is sent to the Buds (I-1 only uses the ANC `Get` and `Set` the app already sent). Nothing here is hardware-verified yet: the next capture is
   `CAP-064` (Group AZ), whose step list is ready — it also holds the one-bud-in-an-ear test (AY-3) and the steps `CAP-063` skipped. The in-ear-detection-off
   steps went into `CAP-056` (Group AR, Pixel 7a), as you asked.

## 2. Reading (prompt §0) — what was read in full, and what not

- **In full:** `AGENTS.md` (in context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (all 3,238 lines), `DECISIONS.md`
  (ADR-001 … ADR-045 with every dated Update, incl. the 2026-09-28 Updates of ADR-024 and ADR-043), `TODO.md`, `AI_SESSION_LOG_PROCEDURE.md`,
  `0053` RESULT, `CAP-063-FINDINGS.md`, `CAP-063-EVENT-NOTES.md` (incl. Appendix A), `APP_TESTPLAN.md`.
- **Partly (disclosed):** `ai-sessions/INDEX.md` (last rows); `0048` RESULT §1–§11 (all of it, which covers the requested §4, §7–§9); `0052` RESULT §2–§12
  (covers §4–§9); `id_registry.csv` (header and the `CAP-06x` rows).
- **Kotlin, in full, before any change:** `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `BudsRepositoryImplTest.kt`, `SessionLoss.kt`, `SessionLossTest.kt`,
  `SessionReopener.kt`, `SessionDiagnostics.kt`, `OsConnectionObserver.kt`, `DeviceInfo.kt`, `BatteryStatus.kt`, `BudsError.kt`, `AncScreen.kt`,
  `AncTileService.kt`, `ConnectionScreen.kt`, `EqScreen.kt`, `SettingsUi.kt`, `ControlsScreen.kt`, `MainActivity.kt`, `FakeBudsTransport.kt`,
  `Cap061Fixtures.kt`, `Cap062Fixtures.kt`, `SettingsFixtures.kt`.

## 3. Phase 0 — set-up and baseline

- `git log -1`: `55f1e62 docs: prompt 0054 — build the CAP-063 improvements I-1…I-5 and a Group AZ skeleton`; `git status --short`: clean;
  `git fetch && git log --oneline HEAD..origin/main`: `59adc60 docs: auto-regenerate sitemap.xml [skip ci]` (CI's sitemap commit — rebase before pushing).
- Baseline `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL** (the dependency cache was complete;
  `--offline` kept). JUnit XML: `:data` **1536**, `:hardware` **51**, `:domain` **17** tests, 0 failures/errors/skipped. Lint: `:data`, `:hardware`, `:ui`
  "No issues found"; `:app` 0 errors, 1 warning (the pre-existing `DataExtractionRules`). Kotlin compiler warnings: the baseline build was up to date
  (nothing recompiled), so they are recorded from the final clean build (§9) and compared with the 3 pre-existing ones of `0052` §6.

## 4. Phase A — fixtures re-derived from the capture (rule 4a)

Handle `0x000b` = the Buds' classic link (`CAP-063-FINDINGS.md` header; `bluetooth.addr` is empty with this encapsulation). `p2p_dir` 0 = phone → Buds.

```
$ cd captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY
$ tshark -r CAP-063-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm && btrfcomm.len>0 && (frame.number==4774 || … )" \
    -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data
2723  16:00:17.184497  1  0x02  7e00a5032a1e180032120a04082410011204086410021a04086410023a06080110011800080710151dea71de7d5e2590821ee630af35847e
2756  16:00:17.314730  1  0x04  03030003e4e4ff                                     (battery: L/R 100 % charging)
3046  16:01:03.871690  1  0x04  030300036464ff                                     (battery: L/R 100 % not charging)
3050  16:01:03.939645  1  0x04  0813000401e8e808                                   (Notify, Settable e8, ACTIVE)
3059  16:01:04.208432  1  0x02  7e00a5032a181800320c1204086410011a04086410013a06080010001800080710151dea71de7d5e2590821ee6f061fae47e
4180  16:04:32.471256  1  0x04  030300036464ff
4184  16:04:32.536644  1  0x04  0813000401e8e808                                   (Notify, Settable e8, ACTIVE)
4233  16:04:42.495551  0  0x04  0812001401e8e84000000000000000000000000000000000   (app Set ADAPTIVE)
4241  16:04:42.610175  1  0x04  ff010006081201e8e840                               (ACK of 4233)
4244  16:04:42.649115  1  0x04  0813000401e8e840                                   (Notify, ADAPTIVE)
4497  16:05:27.967205  0  0x04  0812001401e8e80800000000000000000000000000000000   (app Set ACTIVE)
4508  16:05:28.144025  1  0x04  ff010006081201e8e808                               (ACK of 4497)
4774  16:06:11.244764  1  0x04  0813000401e80020                                   (Notify, Settable 00, OFF — buds on the table)
$ python3 ../../scripts/pwrpc_decode.py CAP-063-btsnoop_hci.log | grep -E "^ *(2723|3059)\b"
  2723 … SERVER_STREAM ch=21 … SubscribeRuntimeInfo … | 3:0 6:{1:{1:36 2:1} 2:{1:100 2:2} 3:{1:100 2:2}} 7:{1:1 2:1 3:0}   (both charging, Case 36)
  3059 … SERVER_STREAM ch=21 … SubscribeRuntimeInfo … | 3:0 6:{2:{1:100 2:1} 3:{1:100 2:1}} 7:{1:0 2:0 3:0}                (neither, no 6.1)
```

Every byte equals the prompt's §2 list. Notes (the capture wins, nothing contradicts the prompt): 4184 and 4233 are **different claims** (the Refresh claim at
16:04:32 released after its linger; the ADAPTIVE tap at 16:04:42 opened a new claim and sent no `08 11`). I-1's test therefore composes them the way the new
behaviour will run: one claim, `Get` → Notify 4184 → Set 4233 → ACK 4241. 3059's bytes are identical to `CAP-062` 7118 (`Cap062.STREAM_NONE_7118`, channel 21).

**I-2 export times** (`sed -n 586,593p CAP-063-debug-export.log`):
```
586  16:15:21.408 Android link observer stopped
589  16:15:22.008 Session lost: channel 0x02 closed (IOException: bt socket closed, read return: -1); … not a user disconnect
590  16:15:22.009 Session loss cause: undetermined (no reading of Android's link close to the loss)
592  16:15:25.013 Android link observer started
593  16:15:25.062 Android link: PENDING -> NOT_CONNECTED (trigger: profile 1 bound)
```

**I-3 — a real `WriteSetting 4:{17:0}` exists** (the prompt expected none; correction): `for f in captures/*/*btsnoop_hci.log; do python3
scripts/pwrpc_decode.py $f | grep WriteSetting | grep '4:{17:0}'; done` → exactly one, **`CAP-046` frame 1873** (17:05:09.216, channel 19, the official
app's Group AK centre-return sample; `ReadSetting` answers `4:{17:0}` are many — e.g. `CAP-001` 1415 — but they are reads):
```
$ tshark -r CAP-046-btsnoop_hci.log -Y "btrfcomm.len>0 && frame.number>=1873 && frame.number<=1880" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
1873  17:05:09.216323  0  7e003b0310131dea71de7d5e251d9a8c9e2a0522038801004a2d0abb7e   WriteSetting 4:{17:0} (ch 19)
1877  17:05:09.350846  1  7e80a3032a052203880100080710131dea71de7d5e25f5ad21287a36f1ed7e   SubscribeToSettingsChanges mirror 4:{17:0}
1878  17:05:09.353465  1  7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e              empty RESPONSE, status OK (= SettingsWrites.ACK_CH19_1629)
```
So the `17:0` test uses real bytes; no supplementary hand-built frame is needed.

## 5. Phase A — current behaviour and planned change, per item

### 5.1 I-1 (ANC re-check on a disabled tap)
- **Now:** `BudsRepositoryImpl.setAncMode` (`BudsRepositoryImpl.kt:651-658`) returns `BudsError.AncNotAllowed` **before any claim** while `_ancAvailability ==
  NOT_ALLOWED`. The availability is set only in `handleRoutedFrame` from each `Notify`'s Settable byte (`:455-457`, `updateAncAvailability` `:292-298`), and reset
  to `UNKNOWN` at Connect (`openSession`, `:584`). A `Get` is sent only by `refreshAncMode` (`:685-694`, used by the Connect snapshot `launchInitialSnapshot`
  `:1038-1043`, ANC Refresh and *Refresh battery*). `sendAncSet` (`:660-675`) = `withMessageStream` (`:984-1025`, claim → action → 1.5 s linger) + `writeGate`
  (Model ID of this claim, ADR-042) + `Set` + wait ≤ 1 s for ACK/NAK/Notify (`_ancOutcomes`). `AncScreen.kt:89-96` disables the mode buttons while `NOT_ALLOWED`;
  `AncTileService.onClick` (`AncTileService.kt:122-126`) shows a toast and sends nothing; otherwise it calls `repository.setAncMode(next)` in the application scope.
- **Planned:** `setAncMode` while `NOT_ALLOWED` → **one** `withMessageStream` claim: `Get` (`08 11 00 00`, the same bytes `refreshAncMode` sends) and wait ≤ 2 s
  for the claim's `Notify`; Settable non-zero → the `Set` **inside the same claim** through the unchanged gate/ACK path; `00` → `AncNotAllowed`, nothing more
  sent; no `Notify` → `Timeout`, no `Set`. A new replay-0 flow `_ancAvailabilityFresh` (emitted on every `Notify`) is what the re-check waits for (the mode flow
  misses a `Notify` whose mode byte is unknown). `AncNotAllowed` from a re-check is not recorded as a Message Stream (channel) error. `ALLOWED`/`UNKNOWN`
  are unchanged (`sendAncSet` as today). UI: buttons stay enabled while `NOT_ALLOWED`, with a checkpoint wording; the tile sends the tap to the same
  `setAncMode` and shows a toast only when the re-check refuses. Tests: §2's (a)–(e).

### 5.2 I-2 (loss cause after returning to the app)
- **Now:** `classifySessionLoss` (`SessionLoss.kt`): "not connected" from −2 s to +1 s → link lost; else the first reading within 2 s after the loss decides; else
  `UNDETERMINED`. The repository keeps the last 32 readings (`onAndroidLink`, `:300-307`) and re-classifies on every reading (`reclassifyLoss`, `:344-351`); the
  loss time is set in the `connectionLost` collector (`:435`). `MainActivity` forwards every reading while at least STARTED (`MainActivity.kt:313-322`) and
  `onAppVisible(true/false)` on resume/stop (`:338-348`); `onAppVisible` only feeds `SessionReopener` (`BudsRepositoryImpl.kt:309`). The observer stops on
  stop and restarts on start, so no reading exists while the app is not visible — which is why `CAP-063` #8 stayed undetermined (first reading 3.05 s after).
- **Planned (`:domain` first):** two new causes for "read on return" — `ANDROID_LINK_DOWN_ON_RETURN` / `ANDROID_LINK_UP_ON_RETURN` — and a third input to
  `classifySessionLoss`: `lossWhileHidden`. When rules 1–2 give `UNDETERMINED` and the loss happened while the app was not visible (or the app left before a
  deciding reading arrived), the **first reading at or after the loss** decides, whenever it comes (readings only exist once the app is back on screen). Never a
  reading older than the loss. The repository tracks visibility (`onAppVisible`) and passes the flag; texts per checkpoint; `SessionDiagnostics.lossCauseLine`
  gains the two lines.

### 5.3 I-3 (balance "Centre")
- **Now:** `EqScreen.kt:208-231` `BalanceSlider`: 201 positions (`valueRange = -100f..100f`, no steps), `onValueChangeFinished = { onChange(-position.roundToInt()) }`;
  `balanceText` (`SettingsUi.kt:48-52`) shows "Centre" only for 0; `setVolumeBalance` clamps to ±100 (`BudsRepositoryImpl.kt:791-794`).
- **Planned:** per the checkpoint — either a pure snap rule in `:domain` (`BudsSettings`, |v| ≤ N → 0, unit-tested) applied on release, or a "Centre" button that
  writes 0. Repository test: `setVolumeBalance(0)` on channel 19 = `CAP-046` 1873 byte for byte, ACK 1878.

### 5.4 I-5 (Digital-assistant note)
- **Now:** `ControlsScreen.kt:71` "Press and hold" title, then `HoldRow` Left/Right. **Planned:** one line under the title (checkpoint text). No detection.

### 5.5 I-4 (stale per-bud lines at Connect)
- **Now:** `openSession` marks only the Case stale (`:587`); `leftCharging`/`rightCharging` (`ChargingReading`) and the Left/Right percentages keep the previous
  session's values until the new claim's battery frame (`:521-532`) or stream packet (`:540-555`); `budLine` (`ConnectionScreen.kt:380-391`) shows them with
  their old time as if current.
- **Planned:** at Connect (and re-open — same `openSession`) mark each bud's percentage `isStale` and its `ChargingReading` "from the last connection"; the first
  new report of either source replaces what it covers (a battery frame: % and charging; a stream packet: charging). `budLine` words the stale parts
  (checkpoint). Tests: Connect → stale; `CAP-063` 3046 then 3059 clear it; a stream-first Connect (3059) clears the charging mark.

### 5.6 New `transport.send` call sites (prompt task 5)
**None.** I-1 reuses the existing `Get` bytes (`AncFrameEncoder.encode(AncFrame.Get)`, today sent by `refreshAncMode`) and the existing `Set` path inside the
existing `withMessageStream` claim (ADR-009/021/032) — `refreshAncMode` and the re-check share one `Get` call site (`sendAncGetAndAwait`), and the `Set` call site moved into
`ancSetOnClaim` unchanged — so the count of `transport.send(` lines in `:data` stays 8; I-3 writes a value inside ADR-045's range through the existing `writeSetting`; I-2, I-4, I-5 send nothing. No DLCI 0x08, no field 12/2.

## 6. Phase B — checkpoint answers (this chat, 2026-09-28, `AskUserQuestion`; recorded verbatim)

- **"I-1 ANC"** → **"Reverse, wording A (Recommended)"** — the maintainer's confirmation *in this chat* of the `0053` §9 approval to reverse the `0048` I-3
  rule for a user tap (memory rule "Approvals: confirm in chat"). Preview chosen: *ANC screen, buttons ENABLED while not allowed: "ANC can only be changed
  while you wear the Buds (checked 16:06:11). Tapping a mode checks again first." Refused re-check (Buds still say 00): same line, new time: (checked
  16:07:02), nothing else sent. Tile: subtitle stays "Only while worn"; tap -> re-check; allowed -> mode switches; refused -> toast "ANC can only be
  changed while you wear the Buds."*
- **"I-2 loss"** → **"Wording A, honest on 'up' (Recommended)"**: *Link DOWN when you returned: "Android no longer showed the Buds connected when you
  returned to the app — the connection ended while the app was in the background (for example both buds in the case, or out of range). Tap Connect to
  reconnect." Link UP when you returned: "The app's channel was closed while the app was in the background; Android showed the Buds connected when you
  returned. The app reopens its channel by itself while it is on screen, or tap Connect."*
- **"I-3 balance"** → **"Snap within ±3 (Recommended)"** (release at Left 1..3 or Right 1..3 → Centre; "Centre · changed HH:MM:SS").
- **"I-4 stale"** → **"'last seen' + 'last connection' (Recommended)"**: *"Left: 100% — last seen 16:00:17 — charging in the case (16:00:17, last connection)"*;
  after the new report *"Left: 100% (updated 16:01:03) — not charging (out of the case) (16:01:04)"*.
- **"I-5 note"** → **"Hedged wording (Recommended)"**: *"Digital assistant needs an assistant app on this phone that supports headphones (for example the
  Google app). Without one, holding the bud may only play a tone."*
- **"AZ extras"** (multi-select) → no option ticked; the maintainer's own text: **"Maak w-12b in-ear detection off onderdeel van de CAP-056, die ik binnenkort
  zal uitvoeren."** (translated: *make W-12b "in-ear detection off" part of CAP-056, which I will run soon*). So: the `CAP-064` skeleton gets neither the GSND
  check nor W-12b; the W-12b OHD-off steps are added to the Group AR / `CAP-056` plan instead (Phase G).

## 7. What was built, per item

| Item | What | Where | Tests (real bytes) |
|---|---|---|---|
| **I-1** | A mode tap while `NOT_ALLOWED` does one ordinary claim: `Get` → wait ≤ 2 s for the claim's `Notify` (new replay-0 `_ancAvailabilityFresh`, emitted on every `Notify`) → Settable non-zero: the `Set` in the same claim (`ancSetOnClaim`: Safe-Mode gate, ACK/NAK/Notify within 1 s — unchanged rules); `00`: `AncNotAllowed`, nothing more sent; no `Notify`: `Timeout`, no `Set`. `ALLOWED`/`UNKNOWN`: the `Set` directly, as before. A refused re-check is not a Message Stream error. `ancAvailabilityUpdatedAt` now uses the injected clock (production: the same wall clock). UI: mode buttons stay enabled; "ANC can only be changed while you wear the Buds (checked HH:MM:SS). Tapping a mode checks again first."; the tile no longer refuses by itself — it calls `setAncMode` and shows the toast only if the re-check is refused. | `BudsRepositoryImpl.kt` (`setAncMode`, `ancSetOnClaim`, `sendAncGetAndAwait`, `withMessageStream`), `BudsRepository.kt`/`DeviceInfo.kt`/`BudsError.kt` (docs), `AncScreen.kt` (`ancNotAllowedLine`), `OpenControlNavHost.kt`, `MainActivity.kt`, `AncTileService.kt` | 6 repository tests replacing the two `0048` I-3 tests: (a) 4774 → tap ADAPTIVE → `Get`, Notify 4184 → Set = 4233 byte for byte, ACK 4241 applies it, one claim; (b) Notify 4774 again → only the `Get`, `AncNotAllowed`, still `NOT_ALLOWED`, checked time moved, no channel error, released after the linger; (c) a claim that cannot open → its error, nothing sent; an unanswered `Get` → `Timeout`, no Set; (d) the tile's `nextForTile(OFF)` = ACTIVE → `Get`, 4184, Set = 4497, ACK 4508; (e) `UNKNOWN`/`ALLOWED` → the Set directly (4497, 4233); plus `CAP-062` 5998/6000: after a NAK the next tap re-checks and sends no second Set |
| **I-2** | `classifySessionLoss(…, lossWhileHidden)`: rules 1–2 unchanged; when still undetermined and the loss happened while the app was not visible (or the app left before a deciding reading), the first reading at or after the loss decides → `ANDROID_LINK_DOWN_ON_RETURN` / `ANDROID_LINK_UP_ON_RETURN`. Never a reading older than the loss. The repository tracks visibility and passes the flag. Texts as chosen. | `SessionLoss.kt`, `BudsRepositoryImpl.kt` (`appVisible`, `lossWhileHidden`, `onAppVisible`), `SessionDiagnostics.kt`, `ConnectionScreen.kt` (`channelLostMessage`) | domain: `CAP-063` export 526/586/589/592/593 → link down on return (undetermined without the flag); CONNECTED on return → up; older readings never used; UNKNOWN claims nothing; `CAP-063` #9 (export 644/648, 602 ms) keeps the `0048` "Buds closed". Repository: the same export times through `onAppVisible`/`onAndroidLink`; a loss while visible whose app left before a reading; a visible loss with a reading a minute later stays undetermined |
| **I-3** | `BudsSettings.snapBalance`: a release within ±3 writes 0; the knob jumps to the centre; "Centre" is still shown for 0 only. | `BudsSettings.kt`, `EqScreen.kt` (`BalanceSlider`) | `BalanceSnapTest` (the 12 `CAP-063` releases 5180 … 5245, zigzag-decoded: 5 become 0; the ±3/±4 edges and the clamp); repository: `setVolumeBalance(snapBalance(2))` = `CAP-046` frame 1873 byte for byte, ACK 1878 → `SettingReading(0, …, changedByApp)` |
| **I-5** | "Digital assistant needs an assistant app on this phone that supports headphones (for example the Google app). Without one, holding the bud may only play a tone." under "Press and hold". | `ControlsScreen.kt` (`DIGITAL_ASSISTANT_NOTE`) | none needed (text); no Compose tests exist |
| **I-4** | At every Connect/re-open (before the bonded-device lookup, next to the settings reset) `markedFromEarlierSession()`: each bud's % → `isStale`, each `ChargingReading` → `fromEarlierSession`, the Case as in `0048` I-5; each new report replaces what it covers. Line: "Left: 100% — last seen 16:00:17 — charging in the case (16:00:17, last connection)". | `BatteryStatus.kt`, `BudsRepositoryImpl.kt` (`openSession`), `ConnectionScreen.kt` (`budLine`) | Connect after `CAP-063` 2723/2756 → all marked; 3046 → % and charging current; 3059 → charging from the stream, Case still last seen; a stream-first Connect (3059) clears the charging mark while the % stays last seen |

## 8. Corrections to the prompt (the capture wins)

- A real `WriteSetting 4:{17:0}` exists: `CAP-046` frame 1873 (§4) — the I-3 test uses it; no supplementary frame was needed.
- The prompt's (and `0053`'s) "12 releases … `17:21, 19, 7, 1, 1, 3, 22, 14, 11, 6, 7, 1`" are the **raw zigzag varints** `pwrpc_decode.py` prints; the balance
  values are −11, −10, −4, −1, −1, −2, +11, +7, −6, +3, −4, −1 (ADR-019's zigzag rule), matching `CAP-063-FINDINGS.md` §2's "±1…±11". 5 of 12 are within ±3.
- 4184 and 4233 are two different claims in the capture (§4); the I-1 test composes them as the new behaviour will run them.

## 9. Gate (Phase F, task 10)

`./gradlew --offline clean`, then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL** (2 min 1 s).

| After | `:data` | `:hardware` | `:domain` | Lint | Kotlin warnings |
|---|---|---|---|---|---|
| baseline (`55f1e62`) | 1536 | 51 | 17 | `:app` 0 errors / 1 warning (`DataExtractionRules`); others "No issues found" | (up to date, not recompiled) |
| Phase C (I-1) | 1540 | — | — | — | — |
| Phase D (I-2) | 1543 | — | 21 | — | — |
| Phase E / **final, after clean** | **1546** | **51** | **23** | unchanged | the same 3 pre-existing (`BudsCompanionPairing.kt:263`, `:293`, `LinkEvaluation.kt:84`), none new |

No new lint issue, no new suppression (`@Suppress`/`tools:ignore` grep over the changed files: none). Two tests changed because they encoded the reversed
`0048` I-3 rule ("no claim at all while `00`"; "the second Set is held back") — replaced by I-1 tests with real bytes.

## 10. Mutation checks (task 11; `mutate.py`: apply, run `:domain:test` + `:data:testDebugUnitTest`, restore, compare SHA-256)

| # | Mutation | Failing tests | Restored identical |
|---|---|---|---|
| M1 | I-1 sends the Set without the re-check (no `Get`, straight to the Set) | 5 | yes |
| M2 | I-1 sends the Set after a `00` Notify | 2 | yes |
| M3 | I-2 uses a reading older than the loss | 4 | yes |
| M4 | I-2 ignores the post-resume reading (stays undetermined) | 5 | yes |
| M5 | I-4 never clears the stale mark (a new battery frame keeps a stale %) | 1 | yes |
| M6 | I-3 snap removed | 3 | yes |

## 11. Compliance (task 12)

- No `INTERNET`, no new permission; `git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` → empty.
- `transport.send` in the diff: one line removed and one added — the **same** ANC `Get` (`AncFrameEncoder.encode(AncFrame.Get)` on DLCI 0x04) moved into the
  shared `sendAncGetAndAwait`, now used by `refreshAncMode` and the I-1 re-check; the `Set` line is unchanged (moved into `ancSetOnClaim`). **No new call
  site, no new message type**: I-1 sends only what an ANC claim already sends (ADR-009/021/032), only after the user's tap. I-3 writes `17:0`, inside
  ADR-045's range, through the existing `writeSetting`.
- Nothing on DLCI 0x08; no field 12 or field 2 read/write added; every write still passes `writeGate` (ADR-042: ANC `Set` in `ancSetOnClaim`, EQ, settings,
  Ring — 4 call sites, unchanged).
- AGPL-3.0 headers on the two new Kotlin files (`Cap063Fixtures.kt`, `BalanceSnapTest.kt`).
- Known limit, not changed: the tile's next mode is computed from the mode shown (OFF while not worn → ACTIVE), so a tap after re-wearing may set the mode
  the Buds already report again (harmless: the ACK applies it).


## 12. Group AZ re-test skeleton (task 13)

`captures/CAP-064-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AZ/CAP-064-EVENT-NOTES.md` (Pixel 9a / GrapheneOS; `CAP-064` was already registered *planned* — its
`id_registry.csv` row updated to the actual scope). Layout of `CAP-063` Appendix A: A.0 build (hash said aloud), A.1 metadata, A.2 preparation P1–P14 with
`CAP-063`'s lessons marked (minute change filmed at start **and** end, commit hash and Play services' *Nearby devices* state said aloud, **Do Not Disturb**,
**ears visible on film** for the wear steps, an events file, the empty audio track), A.3 start, A.4 steps, A.5 don'ts, A.6 collection, A.7 analysis
checklist and **Refuted if**. Summary of the steps:

| Step | Action | Expected screen | Expected HCI bracket |
|---|---|---|---|
| I-0 / I-1a | buds on the table; tap ADAPTIVE | "(checked HH:MM:SS). Tapping a mode checks again first.", buttons enabled; the time moves | one claim: `08 11` → `08 13 … 00 …`, **no** `08 12` |
| I-1b | buds into the ears on film; tap ADAPTIVE | ADAPTIVE (updated …) | one claim: `08 11` → `08 13 … e8 …` → `08 12 … 40` → ACK |
| I-1c/d | tile tap on the table, then worn | toast / mode switches | as I-1a / I-1b |
| AY-3a–c | one bud visibly in an ear, the other visibly on the table, Refresh; swap; both in | say which | `08 11` → `08 13 01 e8 <Settable> …` — "Settable `0x00` = no bud worn" (ADR-024 Update, 🟡) |
| I-2a (b) | Home; both buds into the case (lid open); 10 s; return | "Android no longer showed the Buds connected when you returned …" | ACL `0x13` while away; no app `SABM` 0x02 while away |
| I-3a–d | balance Left 20, near the middle, Right 4, near the middle | "Left 20", "Centre", "Right 4", "Centre" | `4:{17:40}`, `4:{17:0}`, `4:{17:7}`, `4:{17:0}` → `RESPONSE` OK |
| I-4a/b | Disconnect, change the dock state, Connect; film the first 3 s | "… last seen … (…, last connection)", then the new lines | stream packet / battery burst |
| I-5 | read the note under "Press and hold" | the chosen text | — |
| F7, K1–K5, L2/L3, A5, (E), B4 | the `APP_TESTPLAN.md` steps `CAP-063` skipped | as in `APP_TESTPLAN.md` | as there |

Not in `CAP-064` (maintainer, §6): the GSND check, and W-12b — the in-ear-detection-off steps W1–W5 (with the `0051` F-6 bit-order steps as (A)) were added to
`captures/CAP-056-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AR/CAP-056-EVENT-NOTES.md` ("Additions") and its registry row. Group AZ was **not** added to
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` (not asked).

## 13. Documentation (task 14 — only what changed)

- `ARCHITECTURE.md` §3.1 ANC row (I-1), §6.0b loss-cause bullet (I-2).
- `APP_TESTPLAN.md`: header note "updated 2026-09-28 for the `ai-sessions/0054` build"; F8, G3 (I-1), new C11 (I-2), new E10 (I-4), M3 (I-3), N3 (I-5);
  summary counts C 11, E 10.
- `TODO.md` (the 0053 FEATURE item done; next captures; the in-ear ADR preparation points to `CAP-056` W1–W5; the tile's next-mode limit), `CHANGELOG.md`,
  `README.md` (status block), `ai-sessions/INDEX.md` (0054 row), `id_registry.csv` (`CAP-064`, `CAP-056` rows), the two capture skeletons.
- **Not changed:** `PROTOCOL.md`, `DECISIONS.md` (no FACT or ADR change was needed or asked; I-1 is covered by ADR-009/021/024/032, I-3's value by ADR-045),
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`.
- `python3 scripts/ensure_footers.py` (footer added to this file) and `python3 scripts/lint_docs.py` → **exit 0** (the historical dead-reference list it
  prints is informational).

## 14. Open items

- Nothing is hardware-verified: `CAP-064` (Group AZ). In particular: that a re-wear really turns the `Notify` to `e8` inside the tap's own claim (I-1b), and
  what AY-3 shows for one bud worn.
- The ANC tile's next mode is computed from the mode shown (OFF while not worn → ACTIVE) — a first tap after re-wearing may re-set the current mode (`TODO.md`).
- Group AR (`CAP-056`, Pixel 7a) first — field 12's bit order and W-12b; only then a field-12 ADR and the draft in-ear-detection-write ADR (`0053` §6).
- The Compose texts (I-1, I-2, I-4, I-5 wording, the balance snap's knob jump) are not unit-testable in this project (no Compose tests) — hardware/film check in `CAP-064`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0054_FEATURE_RESULT_2026_09_28.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0054_FEATURE_RESULT_2026_09_28
