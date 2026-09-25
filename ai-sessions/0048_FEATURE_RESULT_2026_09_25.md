# 0048_FEATURE_RESULT_2026_09_25.md — Build the `ai-sessions/0047` improvements: ANC only while worn, ring notice, loss wording, per-bud "in the case", last-seen Case, automatic foreground re-open (ADR-044)

**Number:** 0048
**Category:** FEATURE
**Date:** 2026-09-25
**Title:** Implement the improvements the maintainer chose after `CAP-062` (`ai-sessions/0047` RESULT §11/§13): I-6, I-7, I-3 (bug/UX); I-4, I-5, I-8 (runtime-info stream, ADR-043 Update); I-1 (automatic foreground re-open of the session, ADR-044) — with real `CAP-062` bytes as test fixtures, a green build/test/lint gate, and a hardware re-test list for the next capture (Group AY, including I-9/I-10)
**Status:** complete

## Progress (final)

- Phases 0, A–D, E (checkpoint §7), F (documentation) done. Full gate green (§5–§6). `lint_docs.py` exit 0 (§10).
- Committed and pushed on the maintainer's go-ahead in chat ("commit and push it", 2026-09-25): one code commit and one docs commit (§11). No
  subagent was used; every read and write was done in this session.
- Parallel, not this session's: `ai-sessions/0049_AUDIT_PROMPT_2026_09_25.md` / `0049_AUDIT_RESULT_2026_09_25.md` and their `INDEX.md` row appeared at 13:40–13:44 from another
  session — untouched, and to be left out of this session's commits.

## 1. Plain-language summary (per complaint of `ai-sessions/0047` §1)

1. **"The app keeps losing its session while Android stays connected."** The Buds still close the app's channel whenever a bud goes into or out
   of the case or an ear — the app cannot stop that — but it now **opens it again by itself** while it is on screen and Android shows the Buds
   connected: 1.5 s after the Buds close it, when Android reconnects (e.g. the lid is opened), when you come back to the app, and when you open it.
   One try per event, never in the background; your Disconnect tap turns this off until you tap Connect again (your choices, §7).
2. **"ANC is refused in the case."** The Buds refuse ANC whenever they are not worn (also on the table). The app now greys the ANC buttons out and
   says "ANC can only be changed while you wear the Buds." — it sends nothing then; Refresh re-checks. The Quick Settings tile says "Only while
   worn" and shows the same message instead of sending.
3. **"It doesn't show which bud is in or out."** Each bud's line now says "charging in the case" or "not charging (out of the case)" with the time
   the Buds reported it — they send it by themselves whenever a bud is seated or removed.
4. **"After re-docking it doesn't show the bud in the case and the Case battery."** Besides 1 and 3: the Case level no longer disappears when no
   bud is in the case — it stays as "60 % — last seen 06:46:25 (no bud charging in the case)".
5. **Find in the case:** unknown still — it is the first experiment of the next capture (§9, AY-1). Also fixed: after Disconnect during a ring
   the Find tab keeps saying a ring was started and to tap Stop (it kept ringing in `CAP-062`).
6. **Session-loss message:** it now names what the evidence shows — "The Buds closed the app's channel (…)" or "Android no longer shows the
   Buds connected …" — and never guesses "another app". Nothing in this session is hardware-verified.

## 8. What was built, per item

| Item | What | Where | Tests (real bytes) |
|---|---|---|---|
| **I-6** | The ring notice (`RingNotice`) survives Disconnect/loss; marked `fromEarlierSession`; cleared only by an ACKed Stop, replaced by a new Ring. Text: ready → "Ringing: …" or "… before the app reconnected — it may still be ringing. Tap Stop to end it."; not ready → "… reconnect and tap Stop to end it." | `RingTarget.kt`, `BudsRepository.ringing`, `BudsRepositoryImpl` (`markRingFromEarlierSession`), `FindMyBudsScreen.kt` | 3 repository tests: `CAP-062` 9772 → ACK 9783; Disconnect keeps it; loss keeps it; Stop clears; a new Ring replaces |
| **I-7** | `classifySessionLoss` (`:domain`) from `LinkReading`s: "not connected" within −2 s…+1 s → link lost; else the first reading ≤ 2 s after the loss decides; else undetermined. The repository keeps the last 32 readings, re-classifies on every reading, logs the cause once per change. `OsConnectionObserver` emits every reading; `MainActivity` forwards them and debounces for display. Texts per cause (§7). | `SessionLoss.kt`, `BudsRepositoryImpl` (`onAndroidLink`, `lastLossCause`), `SessionDiagnostics.lossCauseLine`, `OsConnectionObserver.kt`, `MainActivity.kt`, `ConnectionScreen.kt` (`channelLostMessage`) | 6 domain tests (the `CAP-062` export times: 06:42:35 ordering, 06:43:28, the Android-settings disconnect 1.3 s before, the Buds `DISC` 5313, the re-dock with the ACL drop 3.0 s later, no reading), 3 repository tests, 1 diagnostics test |
| **I-3** | `DockState` → `AncAvailability` (`0x00` → NOT_ALLOWED, non-zero → ALLOWED, UNKNOWN before a `Notify`; reset at Connect). `setAncMode` returns `BudsError.AncNotAllowed` and claims/sends nothing while NOT_ALLOWED. ANC buttons disabled + text; Refresh enabled; tile subtitle + toast. | `DeviceInfo.kt`, `BudsError.kt`, `BudsRepository`, `BudsRepositoryImpl`, `AncScreen.kt`, `AncTileService.kt`, `ConnectionScreen.kt` | frames 6000 → nothing sent/claimed; 8706 + ACK 8705 → the Set of frame 8694 byte for byte; NAK 5998 + 6000 on an unknown state → `CommandRejected(0x02)`, the next Set held back; Refresh re-enables |
| **I-4** | `RuntimeInfoDecoder.decode` → `RuntimeInfo(case, leftCharging, rightCharging)`: 6.2/6.3 field 2 (2 = charging, 1 = not), fallback 7.2/7.1; 6.x wins on disagreement; nothing else read. `RoutedFrame.RuntimeInfo`. | `RuntimeInfo.kt`, `CodecRouter.kt` | `Cap062Fixtures.kt`: 3760, 4845, 7033, 7118, 2782 through the router; 4500 (6.x vs 7.x disagreement); CAP-041 782 / CAP-050 1163; fuzz extended (every `CAP-062` frame truncated at every byte + 300 mutations each through HDLC/pw_rpc/decoder) |
| **I-5** | A packet without 6.1 keeps the Case as `Known(isStale = true)` with its own `receivedAtMillis`; at Connect a Case from an earlier session is marked stale; memory only. Texts: "Case: 60% — last seen HH:MM:SS (no bud charging in the case)"; never reported → "Not reported yet — the Buds send the Case level only while a bud is charging in the case." | `BatteryStatus.kt`, `BudsRepositoryImpl`, `ConnectionScreen.kt` (`caseLine`) | the `CAP-062` order 3760→4845→7033→7118; CAP-041/050; never invented from 2782; survives Disconnect |
| **I-8** | `BatteryStatus.leftCharging/rightCharging: ChargingReading(charging, atMillis, source)` — the newest report of DLCI 0x04 (`S` bit) or the stream wins; the percentage keeps its own DLCI 0x04 time. *Refresh battery* unchanged (ADR-032/033). | `BatteryStatus.kt`, `BudsRepositoryImpl`, `ConnectionScreen.kt` (`budLine`) | stream 4845 → DLCI 0x04 `64 64` (CAP-062 4532) → stream 7033 |
| **I-1** | `SessionReopener` (§ARCHITECTURE 6.0b "ADR-044 as built"): visible + link connected + no session → one attempt per event (loss +1.5 s, link back, resume); on from app start; off after Disconnect until Connect; failed attempt reported, not retried; 10 s chain guard; visibility from `MainActivity` (resume/stop). | `SessionReopener.kt`, `BudsRepositoryImpl` (`openSession`, `reopenSession`, `onAppVisible`), `BudsRepository`, `MainActivity.kt`, docs in `OpenControlApplication.kt`/`AncTileService.kt` | 7 repository tests (peer `DISC` while visible → exactly one after 1.5 s; ACL drop → none, link back → one; none after Disconnect until Connect; none in the background; background during the delay cancels; resume → one; reading before resume → one) + 5 `SessionReopenerTest` (chain guard, open/opening, failed not retried, simultaneous events, initially off) |

Documentation: `ARCHITECTURE.md` (§3.1 ANC/Battery/Find rows, §4, §5a Case row, §6 re-connection strategy, §6.0a, §6.0b "ADR-044 as built" and the
loss-cause bullet), `APP_TESTPLAN.md` (C1, C3, C8, C9, E1–E5, F8, G3, G4, I4 + header note), `TODO.md`, `CHANGELOG.md`, `README.md`, `PROJECT.md`
(battery bullet), `ai-sessions/INDEX.md` (0048 row), `CAP-062-FINDINGS.md` §7.2 and `CAP-062-EVENT-NOTES.md` (06:42:35 row) rewritten in place with
the film check of §4. `PROTOCOL.md`/`DECISIONS.md` untouched (no change approved or needed; ADR-044's own "Not yet implemented" note stays as written
history — `ARCHITECTURE.md` §6.0b records it as built).

---

## 2. Reading done before acting

`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (full), `PROTOCOL.md` (full), `DECISIONS.md` (ADR-001 … ADR-044, full,
including the 2026-09-25 Updates of ADR-024 and ADR-043), `TODO.md` (priority list, Phase 4–5, technical debt, open questions in full; the
Phase 1–3 history was read by heading only — disclosed), `AI_SESSION_LOG_PROCEDURE.md`, `ai-sessions/INDEX.md`, `0047` RESULT (full),
`CAP-062-FINDINGS.md` and `CAP-062-EVENT-NOTES.md` (full), `APP_TESTPLAN.md` (full), `0046` RESULT §1–§10. Kotlin read in full before any
change: `BudsRepositoryImpl.kt`, `BudsRepository.kt`, `BatteryStatus.kt`, `DeviceStatus.kt`, `DeviceInfo.kt`, `BudsError.kt`, `RingTarget.kt`,
`SessionDiagnostics.kt`, `RuntimeInfo.kt`, `CodecRouter.kt`, `CaseBatteryFrame.kt`, `BudsTransport.kt`, `RfcommBudsTransport.kt`,
`FakeBudsTransport.kt`, `OsConnectionObserver.kt`, `LinkEvaluation.kt`, `MainActivity.kt`, `OpenControlApplication.kt`, `AncTileService.kt`,
`ConnectionScreen.kt`, `ConnectionBanner.kt`, `AncScreen.kt`, `FindMyBudsScreen.kt`, `OpenControlNavHost.kt`, `BudsRepositoryImplTest.kt`,
`TileCycleTest.kt`, `data/build.gradle.kts`.

## 3. Baseline gate (before any change)

`cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` at `509dfc6` → **BUILD SUCCESSFUL**. Unit tests (debug variant,
JUnit XML): `:data` **1480**, `:hardware` **49**, `:domain` **11**, 0 failures. Lint: `:data`, `:hardware`, `:ui` "No issues found"; `:app` 0 errors,
2 warnings (the pre-existing `DataExtractionRules`, `MissingApplicationIcon`). Same as the `0046` counts.

## 4. Fixtures re-derived from `CAP-062` (rule 4a)

Handle `0x000b` = the Buds' classic link (HCI Connection Complete frame 1179); `bluetooth.addr` is empty with this encapsulation, so every command
pre-filters by handle. `p2p_dir` 0 = phone→Buds, 1 = Buds→phone.

**ANC (I-3), Ring (I-6):**
```
$ tshark -r CAP-062-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm && btrfcomm.len>0 && (frame.number==5989 || …)" \
    -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data
5989  06:45:20.355209  0  0x04  0812001401e8e88000000000000000000000000000000000   (app Set TRANSPARENT)
5998  06:45:20.529051  1  0x04  ff020003020812                                     (NAK, reason 0x02)
6000  06:45:20.529847  1  0x04  0813000401e80020                                   (Notify, Settable 00, OFF)
8694  06:49:17.640056  0  0x04  0812001401e8e84000000000000000000000000000000000   (app Set ADAPTIVE)
8705  06:49:17.833956  1  0x04  ff010006081201e8e840                               (ACK)
8706  06:49:17.834682  1  0x04  0813000401e8e840                                   (Notify, Settable e8, ADAPTIVE)
9772  06:53:13.234975  0  0x04  0401000102                                         (Ring Left)
9783  06:53:13.389045  1  0x04  ff010003040100                                     (ACK)
9784  06:53:13.389944  1  0x04  0401000102                                         (the Buds' echo)
```

**Runtime-info stream (I-4, I-5, I-8)** — `python3 scripts/pwrpc_decode.py CAP-062-btsnoop_hci.log` (decode) and the raw RFCOMM payloads:
```
$ tshark -r CAP-062-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm.dlci==2 && btrfcomm.len>0 && (frame.number==2782 || …)" \
    -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e data.data
2782 06:41:07.216 1 0x02 7e80a3032a181800320c1204086410011a04086410013a06080010001800080710131dea71de7d5e2590821ee6227fa42c7e
3760 06:42:28.851 1 0x02 7e80a3032a1e180032120a04083c10011204086410011a04086410023a06080110001800080710131dea71de7d5e2590821ee639a8c8637e
4845 06:43:32.080 1 0x02 7e80a3032a1e180032120a04083c10011204086410021a04086410023a06080110011800080710131dea71de7d5e2590821ee6160866417e
7033 06:46:25.732 1 0x02 7e00a5032a1e180032120a04083c10011204086410021a04086410013a06080010011800080710151dea71de7d5e2590821ee696e7a89b7e
7118 06:46:35.578 1 0x02 7e00a5032a181800320c1204086410011a04086410013a06080010001800080710151dea71de7d5e2590821ee6f061fae47e
```
Decoded (`pwrpc_decode.py`): 2782 `3:0 6:{2:{1:100 2:1} 3:{1:100 2:1}} 7:{1:0 2:0 3:0}` (no 6.1); 3760 `6:{1:{1:60 2:1} 2:{1:100 2:1} 3:{1:100 2:2}}
7:{1:1 2:0 3:0}` (Right charging); 4845 `… 2:{… 2:2} 3:{… 2:2}} 7:{1:1 2:1 3:0}` (both); 7033 `… 2:{… 2:2} 3:{… 2:1}} 7:{1:0 2:1 3:0}` (Left only);
7118 = 2782's layout on channel 21 (no 6.1). 7033 equals the prompt's quoted bytes (debug export line 298). The packets carry **no identifier** (no
serial, no address — only percentages, flags and the pw_rpc header), so nothing is redacted.

One more real packet was found while checking: **frame 4500** (DLCI **0x03** — session-local MAESTRO numbering on a Buds-initiated connection),
06:43:03.552, `7e80a3032a181800320c1204086410011a04086410023a06080010001800080710131dea71de7d5e2590821ee61f46415a7e` = `6:{2:{1:100 2:1} 3:{1:100 2:2}}
7:{1:0 2:0 3:0}` — 6.3.2 says Right charging while 7.1 says not; the DLCI 0x05 battery frame 4520 (06:43:04.000, `0303000364e4ff`) says Right
charging. A real case of the 6.x.2/7.x disagreement; 6.x.2 agrees with the charging bit (`tshark … -Y "… btrfcomm.dlci==3 …"` / `"… dlci==5 …"`).

**Session ends (I-7, I-1):**
```
$ tshark -r CAP-062-btsnoop_hci.log -Y "frame.number==3814 || frame.number==4667 || frame.number==7225 || frame.number==7226" \
    -T fields -e frame.number -e frame.time -e bthci_evt.code -e bthci_cmd.opcode -e bthci_evt.reason -e bthci_evt.connection_handle
3814 06:42:35.113635 0x05 - 0x13 0x000b     (Disconnection Complete)
4667 06:43:28.155614 0x05 - 0x13 0x000b
7225 06:46:50.190852 0x05 - 0x13 0x000b
7226 06:46:50.202517 -  0x0405              (Create Connection, 12 ms later, not the app)
$ tshark -r CAP-062-btsnoop_hci.log -Y "bthci_acl.chandle==0x000b && btrfcomm.frame_type==0x43 && (btrfcomm.dlci==2 || btrfcomm.dlci==3)" …
Buds-side DISC (p2p_dir 1) of DLCI 0x02: 5313 06:43:40.654, 6258 06:46:04.708, 6848 06:46:22.753, 7192 06:46:46.254, 8514 06:48:52.599,
9362 06:52:05.203, 10163 06:55:04.312; phone-side (the user's Disconnect): 2851, 9256, 9794.
```

**What the app's transport sees (decides I-7's design).** `grep "lost (" CAP-062-debug-export.log`: all 11 session losses — Buds-side `DISC`
with the ACL up **and** ACL drops — carry the identical detail `IOException: bt socket closed, read return: -1`. 🟢 FACT (this capture): **the
transport cannot tell a peer `DISC` from an ACL drop**; the distinction has to come from Android's link state around the loss. Measured from the
same export (the link line vs the loss line): the Android-panel disconnects turned the link `NOT_CONNECTED` 1.28 s (lines 65/66) and 1.48 s
(706/707) **before** the loss; the ADR-016 ACL drops 0.11 s (101/103) and 0.25 s (144/146) **after** it; the Buds-side `DISC`s with both buds
going into the case were followed by the ACL drop 3.0 s (257/259) and 4.1 s (329/331) later.

**Correction of a `0047` detail (film, not a guess):** `CAP-062-FINDINGS.md` §7.2 / the EVENT-NOTES say the stale "…while Android still shows
the Buds connected — likely another app…" text changed "later (t=256)". Frames extracted at t = 239, 240.5, 242, 245 s
(`ffmpeg -ss <t> -i CAP-062-recording.mp4 -frames:v 1`) show it already replaced by "Android no longer shows the Buds connected …" at t = 240.5 s
(overlay 06:42:36) — ≈ 1.5 s after the loss, i.e. the `OsConnectionObserver` 1.5 s "not connected" debounce, not 17 s.

## 5. Gate per phase

| After | `:data` | `:hardware` | `:domain` | Lint |
|---|---|---|---|---|
| baseline (`509dfc6`) | 1480 | 49 | 11 | `:app` 0 errors / 2 warnings (pre-existing); others clean |
| Phase A (I-6, I-7, I-3) | 1490 | 49 | 17 | unchanged |
| Phase B (I-4, I-5, I-8) | 1497 | 49 | 17 | unchanged |
| Phase C / final (clean build) | **1509** | **49** | **17** | unchanged; Kotlin compiler warnings: the same 3 pre-existing (`BudsCompanionPairing.kt` ×2, `LinkEvaluation.kt`) |

## 6. Verification (Phase D)

`./gradlew --offline clean`, then `./gradlew --offline assembleDebug testDebugUnitTest test lint` → **BUILD SUCCESSFUL**, 0 failures, no new lint issue,
no new suppression.

**Mutation checks** (applied with a script, the suite run, restored and checked byte-identical with `cmp`):

| # | Mutation | Failing tests |
|---|---|---|
| M1 | `disconnect()` clears the ring notice again (`_ringing.value = null`) | 1 |
| M2 | the Settable gate in `setAncMode` disabled (`if (false && …)`) | 2 |
| M3 | 6.2/6.3 swapped in `RuntimeInfoDecoder` | 4 |
| M4 | `onUserDisconnect()` leaves the re-open enabled | 1 |
| M5 | `classifySessionLoss` uses the newest reading instead of the first one after the loss | 2 |

**Compliance:** no `INTERNET` permission (manifests unchanged); no Gradle/version-catalog change (no new dependency); no new permission; no new
`transport.send` call site (the diff adds none) — the encoders used are unchanged (`AncFrameEncoder`, `RingFrameEncoder`, `EqFrameEncoder`,
`Maestro.readSettingRequest`, `Maestro.subscribeRuntimeInfoRequest`); I-3 sends **less** (no `Set`, not even a DLCI 0x04 claim, while Settable is
`0x00`); I-1 sends exactly what a Connect tap sends, only while visible. Nothing opens DLCI 0x08. **Nothing here is hardware-verified.**

## 7. Maintainer decisions (this chat, 2026-09-25, `AskUserQuestion`, previews carried the exact texts)

- **Auto start** → **"Also on app open (Recommended)"** — ADR-044 (c) literally: opening the app with the Buds connected in Android opens the session
  without a tap; a Disconnect tap switches the re-open off until the next Connect tap (in memory only).
- **Snapshot** → **"Repeat the snapshot (Recommended)"** — a re-open repeats the whole Connect sequence, including the short DLCI 0x04 claim.
- **Timing** → **"1.5 s + 10 s guard (Recommended)"**.
- **Texts** → **"As built (Recommended)"** (the preview in the question = the texts in `ConnectionScreen.kt`, `AncScreen.kt`, `AncTileService.kt`,
  `FindMyBudsScreen.kt`); the last-seen Case is kept across a reconnect in memory, marked "last seen", never persisted.

No FACT or ADR change was needed or made (`PROTOCOL.md`/`DECISIONS.md` untouched).

## 9. Re-test plan for the next capture (Group AY) — nothing here is hardware-verified

**Before the run (the checklist items missed in `CAP-062`):** write the **build commit hash** (P1) and Play services' ***Nearby devices*** permission
state (P4) in the events file; phone clock with seconds on film at start and end; HCI snoop on, Bluetooth off/on on film; pre-filter the HCI log by
the Buds' classic handle (`bluetooth.addr` is empty with this encapsulation); take all exports within 1 minute of the last action; Debug mode on.

| Step | Action | Expected (screen) | Expected HCI bracket |
|---|---|---|---|
| AY-0 | Buds out of the case (worn), open the app — no tap | connects by itself (I-1 c, app start) | app `SABM` DLCI 0x02 → Buds `GetSoftwareInfo` → app `ReadSetting 4:16` → `SubscribeRuntimeInfo` → DLCI 0x04 snapshot claim (`08 11` → `08 13 … e8 …`) |
| AY-1 (I-9) | Both buds **docked**, lid open, wait until Android reconnects, let the app connect (or tap Connect), **Ring Left**, then **Stop** | does a docked bud ring? (listen + film) | `04 01 00 01 02` → ACK `ff 01 00 03 04 01 00` or NAK `ff 02 00 03 <reason> 04 01`; `04 01 00 01 00` |
| AY-2 | Same state: move one EQ slider | accepted or an error shown | `WriteSetting` → empty `RESPONSE` (status OK) or `status ≠ OK` |
| AY-3 (settable) | One bud **in an ear**, the other on the table; ANC tab → Refresh; then swap the buds; Refresh | buttons enabled or "ANC can only be changed while you wear the Buds." per reading | `08 11` → `08 13 01 e8 <settable> <mode>` each time — the test of "Settable `0x00` = not worn" (ADR-024 Update, 🟡) |
| AY-4 (I-3) | Both buds on the table: ANC tab, tap a mode; tile tap | buttons disabled + text; tile "Only while worn" + toast | **no** `08 12`, no DLCI 0x04 `SABM` for the tap |
| AY-5 (I-1 a) | Buds worn, session ready, app on screen: take one bud out | "The Buds closed the app's channel (…)", then ready again by itself | Buds `DISC` 0x02 → app `SABM` 0x02 ≈ 1.5 s later (one) |
| AY-6 (I-1 a/b) | Put both buds in the case, lid open, app on screen | a re-open, then (ACL drop) "Android no longer shows …" or "The Buds closed …"; when Android reconnects on its own: ready again, both "charging in the case", Case current | Buds `DISC` 0x02; possibly app `SABM` ≈ 1.5 s; ACL `0x13`; Android Create Connection; app `SABM` 0x02 (one per event); no 2nd loss-triggered re-open within 10 s |
| AY-7 (I-4/I-5) | Take the Right bud out, then the Left (app on screen) | Right "not charging (out of the case)", then both; Case "last seen HH:MM:SS (no bud charging in the case)" | stream packets like `CAP-062` 7033/7118 (no request from the app) |
| AY-8 (I-7) | Disconnect in Android's Bluetooth panel | "Android no longer shows the Buds connected …"; **no** automatic re-open until Android reconnects | phone `DISC` HFP → ACL `0x13`; no app `SABM` while the link is down |
| AY-9 (I-1 item 2) | Tap **Disconnect**; take a bud out and back; leave and return to the app | stays closed ("not open yet") | no app `SABM` 0x02 until the next Connect tap |
| AY-10 (I-1 item 3) | Session ready; press Home; take a bud out (the Buds close the session); wait 30 s; return | nothing while in the background; on return: re-opens once | no app `SABM` while the app is not visible; one after resume |
| AY-11 (I-6) | Ring Left; Disconnect; Connect; Stop | "… reconnect and tap Stop …", then "… may still be ringing …", then gone; the ring stops | `04 01 00 01 02` → ACK; `04 01 00 01 00` → ACK |
| AY-12 (I-10) | The `APP_TESTPLAN.md` steps not run in `CAP-062`: A5, B4, C5, F5–F7, H5, J4, K1–K5, L3, and 0045 (E)/(F) | as in `APP_TESTPLAN.md` | as there |

Refuted if: any app `SABM` 0x02 while the app is not visible or after a Disconnect tap before a Connect tap; more than one app `SABM` 0x02 per event;
an `08 12` while the last `Notify` read Settable `00`; a Case value shown without its time; a loss text saying "another app".

## 10. Open items (also in `TODO.md`)

- 🔴 Whether a docked bud rings (AY-1) and whether an EQ write is accepted while docked (AY-2).
- 🟡 Settable `0x00` = not worn (AY-3); "charging" = "in the case" (untested with an empty case); the Buds' `DISC` on wear/dock changes as a
  deliberate reset.
- The success path of ADR-044's re-open is covered only by `SessionReopenerTest` (JVM tests cannot build a `BluetoothDevice`); AY-0/AY-5/AY-6 close it.
- `python3 scripts/ensure_footers.py` (footer added to this file) and `python3 scripts/lint_docs.py` → **exit 0**, "clean" (the dead-reference list it prints is informational, historical session logs only).

## 11. Commits

The plan was one commit per concern (I-6/I-7, I-3/I-4/I-5/I-8, I-1, docs). The code concerns share files (`BudsRepositoryImpl.kt`,
`BudsRepository.kt`, `MainActivity.kt`, `ConnectionScreen.kt`, `BudsRepositoryImplTest.kt`) and were only verified together, so splitting them would
have produced intermediate commits that were never built or tested (`PROJECT_RULES.md` rule 16). The code went in as **one** `feat(app)` commit whose
message lists each item with its reason, the documentation as one `docs` commit. The parallel `0049` files and their `INDEX.md` row were not included.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0048_FEATURE_RESULT_2026_09_25.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0048_FEATURE_RESULT_2026_09_25
