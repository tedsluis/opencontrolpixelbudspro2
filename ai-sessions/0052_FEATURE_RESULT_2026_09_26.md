# 0052_FEATURE_RESULT_2026_09_26.md — Record D-1/D-2/D-3 of `ai-sessions/0051`, then build the EQ preset layout, the Refresh fix and the settings

**Number:** 0052
**Category:** FEATURE
**Date:** 2026-09-26
**Title:** Record the three proposals the maintainer approved in `ai-sessions/0051` (D-1, D-2, D-3), then build the EQ presets in two rows (3 + 2), the *Refresh battery* fix, the Case re-subscription on Refresh, read-only settings (ADR-036) and the settings writes the new ADR unblocks
**Status:** complete

## Progress (final)

- Phases 0, A, B, C, D, E, F and G are done. The final gate after `./gradlew --offline clean` is green (§6), six mutation checks were caught (§7),
  `lint_docs.py` exits 0 (§11).
- The session ran over two days (2026-09-26 and 2026-09-27; a usage-limit break in between). On the maintainer's request a capture skeleton
  `CAP-063` (Group AY) was written in between (§9).
- Committed and pushed on the maintainer's go-ahead in chat (2026-09-27, "Daarna alles committen en pushen") — §12. No subagent was used; every read
  and write was done in this session.
- Intermediate results: scratchpad `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/0cc61406-83d2-4a02-a9d4-db7bfec27e89/scratchpad`
  (pwNNN.txt = pwrpc_decode.py output per capture, raw.sh, counts.sh, mutate.py, film sheets c19.png and c20.png).

## 1. Plain-language summary

1. **Recorded (your approval in this chat):** "Conversation detection" really is setting 22 in both directions, "Use touch controls" (4) was also
   filmed going off, volume balance survives a reconnect, and the unnamed Fast Pair characteristic is Find Hub's "Beacon actions" — all now 🟢 in
   `PROTOCOL.md`. The conflict about which checkbox bit is Adaptive vs Transparency is written down as open. *Refresh battery* may re-ask the
   Case (ADR-043 Update), and **ADR-045** allows the app to change balance, mono, conversation detection, touch controls and press-and-hold.
2. **EQ presets** are now two rows (3 + 2).
3. ***Refresh battery*** always opens the Buds' message channel afresh, because they only send the battery levels when it opens; if nothing
   comes, the screen says "No new battery reading from the Buds — try again." instead of silently keeping old times. It also re-asks the Case once.
4. **Settings:** at Connect the app reads in-ear detection (setting), touch controls, press-and-hold, balance, mono and conversation detection. The
   **"Sound"** tab (the old EQ tab) has balance, mono and conversation detection; a new **"Controls"** tab has touch controls, press-and-hold per
   bud and the in-ear detection setting (read-only). A value changes on screen only after the Buds said OK, and always shows its time.
5. Nothing of this is tested on the Buds yet: that is `CAP-063`, whose step-by-step notes are ready.

## 2. Reading (prompt §0) — what was read in full, and what not

- **In full:** `AGENTS.md` (in context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `DECISIONS.md` (ADR-001 … ADR-044 with every Update),
  `AI_SESSION_LOG_PROCEDURE.md`, `0051` RESULT, `0048` RESULT, `APP_TESTPLAN.md`, `CAP-062-EVENT-NOTES.md`.
- **Partly (disclosed):** `PROTOCOL.md` lines 1–1700 (§0–§5.1 incl. all of §4.5) and §6 lines 2130–2270; `TODO.md` lines 1–120, 663–712, 784–815;
  `ai-sessions/INDEX.md` (tail), `id_registry.csv` (ADR/CAP rows); `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §1–§4 intro, Group AR, AX, Capture Index rows.
  `REVERSE_ENGINEERING.md`'s `qhr`/`qju`/`qht` entries were **not** read; the `qht`/`hgj` claim of D-1(e) was checked in the JADX sources themselves (§3).
- **Kotlin read in full before changing:** `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `BudsRepositoryImplTest.kt`, `CodecRouter.kt`, `Maestro.kt`,
  `PwRpc.kt`, `EqFrame.kt`, `EqFrameEncoder.kt`, `EqFrameDecoder.kt`, `Varint.kt`, `RuntimeInfo.kt`, `BatteryStatus.kt`, `BudsError.kt`, `EqBandGains.kt`,
  `SafeModeGate.kt`, `EqScreen.kt`, `ConnectionScreen.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`, `FakeBudsTransport.kt`, `Cap061Fixtures.kt`,
  `Cap062Fixtures.kt`; `CaseBatteryFrame.kt` (the `Proto` reader) and `PwRpcTest.kt` in the parts used.

## 3. Evidence re-derived before use (rule 4a)

Commands: `python3 scripts/pwrpc_decode.py <log>` per capture; raw bytes with
`tshark -r <log> -Y "btrfcomm.len>0 && (frame.number==N || …)" -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e bthci_acl.chandle -e data.data`.
Every quote of `0051` §3/§7–§15 was confirmed; none was wrong.

| What | Frames | Decode (confirmed) |
|---|---|---|
| Conversation detection | `CAP-019` 1720 → 1731 (mirror 1730), 1808 → 1813 (1812) | `4:{22:0}` 07:36:28.596, `4:{22:1}` 07:36:40.239; channel 21 |
| Touch controls | `CAP-020` 1741 → 1753, 1995 → 2005 | `4:{4:1}` 07:46:44.850, `4:{4:0}` 07:47:20.097 |
| Press-and-hold | `CAP-021` 1895/3619/4315/4976 → 1905/3625/4322/4981 | `7{1:{4:{1:6}}}`, `7{2:…6}`, `7{1:…5}`, `7{2:…5}`; channel 19 |
| Mono | `CAP-022` 1621 → 1629, 1823 → 1835 | `4:{19:1}`, `4:{19:0}` |
| Balance drag | `CAP-022` 1922/1944/2019/2039/2056/2073/2099 → 1927/1950/2022/2045/2059/2081/2104 | raw 199,123,49,30,150,200,10 = −100,−62,−25,15,75,100,5 |
| Balance persistence | `CAP-022` 2099 → `CAP-023` 1047; `CAP-041` 2294 → `CAP-042` 900; `CAP-046` 1957 → `CAP-048` 1319 | 10/10, 199/199, 2/2; the pairs are consecutive captures by date/time (listing of `captures/`) |
| Connect reads | `CAP-036` 1445/1447 (2:1), 1451/1453 (4:1), 1457/1462 (7: both 5), 1526/1528 (17:10 = +5), 1532/1534 (19:0), 1538/1540 (22:1); rejected read 1441 (status UNKNOWN) | as listed |
| Re-subscription | `CAP-062` 2777 → 2782 | `7e003b0310131dea71de7d5e2590821ee6602d65a97e` (ch 19) |
| Refresh burst | `CAP-062` 7088 `SABM`, 7098 `08 11`, 7106/7109 `03030003e464ff`, 7110 `Notify 01 e8 00 20` | the burst comes 0.3 s after the open and before the Notify |
| Film (D-1 a/b) | `CAP-019-recording.mp4` t = 36…42 s; `CAP-020-recording.mp4` t = 63…69 s (`ffmpeg -ss <t> -i … -frames:v 1`), looked at | ON 07:36:26–28, finger 07:36:29, OFF from 07:36:30; ON 07:47:17–19, finger 07:47:20, OFF from 07:47:21 |
| FHN page (D-1 d) | `developers.google.com/nearby/fast-pair/specifications/extensions/fmdn`, fetched 2026-09-26 | Table 1: "Beacon actions \| No \| Read, write and notify \| `FE2C1238-8366-4814-8EB0-01DE32100BEA`" |
| `qht` order (D-1 e) | `qht.java:31` (`b,c,d,e,f`); `hgj.java:216/245/274/303` (`aJ` on→`c`, `aI` off→`d`, `aK` txp→`e`, `aH` adaptive→`f`), keys 115–126 | 1 NC, 2 Off, 3 Transparency, 4 Adaptive |

## 4. Maintainer decisions (this chat, 2026-09-26, `AskUserQuestion`, previews carried the exact texts)

- **D-1** (multi-select): **"(a) conv. detection, (b) touch controls OFF, (c) balance persists, (d)+(e) FHN + qht"** — all five.
- **ADRs** (multi-select): **"D-2 Refresh re-subscribes, D-3 ADR-045 writes"** — both.
- **Checkpoint (Phase E)** — asked right after the Phase D codec/repository work and **before** building any settings UI, so the read-only rows and
  the writes were built once in their final place (a deliberate reorder of D's "read-only UI rows"):
  - **Placement**: **"New 'Controls' tab"** (preview: *Tabs: Connection ANC Sound Controls Find Debug — Sound: sliders, presets, Balance, Mono,
    Conversation detection — Controls: Use touch controls, Hold Left, Hold Right, In-ear detection (setting)*).
  - **Texts**: **"As in the preview (Recommended)"** — *Balance "Left 40 · read 14:32:07" (ends 'L'/'R'; 0 = 'Centre'); "Mono audio — same sound in
    both ears"; "Conversation detection — switch from noise cancellation to transparency when you talk"; "Use touch controls"; "Press and hold — Left:
    Noise control | Digital assistant" (same for Right); "In-ear detection (setting): on — read-only; not whether a bud is worn"; not read: "Not read
    from the Buds yet"; time "read HH:MM:SS" / "changed HH:MM:SS"; read error "Couldn't read the Buds' settings: <reason>"; write error "The setting was
    not changed: <reason>"; Refresh "No new battery reading from the Buds — try again." + "The Buds report the Case level only while a bud is in the case."*

## 5. What was built, per item

| Item | What | Where | Tests (real bytes) |
|---|---|---|---|
| **D-1** | Dated Updates §4.5.1 (label 🟢), §4.5.3 (field 4 both directions 🟢; `qht` conflict 🔴), §4.5.7 + §6 (persistence 🟢), §6 (`FE2C1238` = Beacon actions 🟢), §8 row; findings rewritten in place (rule 9a) | `PROTOCOL.md`; `CAP-019`/`CAP-020`/`CAP-022` EVENT-NOTES + FINDINGS | — |
| **D-2** | ADR-043 Update (2026-09-26) | `DECISIONS.md` | — |
| **D-3** | **ADR-045** (Date, Status, Note on process, Context, Options, Decision, Consequences), registered | `DECISIONS.md`, `id_registry.csv` | — |
| **EQ layout** | `EqPreset.entries.chunked(3)` → one `Row` per chunk, equal-width `AssistChip`s (`Modifier.weight(1f)`), `labelMedium`, 1 line, a spacer fills the short row; no `FlowRow`, no dependency | `EqScreen.kt` | not unit-testable here (no Compose tests); `CAP-063` B2/H0 |
| **Refresh fix** | `refreshBattery` = `withMessageStream(freshOpen = true)`: a lingering claim is closed first; waits ≤ 2 s for a `03 03` frame (subscribed before the open); none ⇒ `BudsError.NoNewBatteryReading` in the new `batteryRefreshError`; texts on the Connection card | `BudsRepositoryImpl.kt`, `BudsRepository.kt`, `BudsError.kt`, `ConnectionScreen.kt`, `OpenControlNavHost.kt`, `MainActivity.kt` | 3 repository tests with `CAP-062` 7098/7106/7110: fresh claim inside the linger (2 opens, 1 close, new times); no burst ⇒ message, old values/times; failed claim ⇒ its error, values unchanged |
| **Re-subscription (D-2)** | one `SubscribeRuntimeInfo` per Refresh (launched, queued behind other Maestro requests, no wait, no retry; a send failure is logged only) | `BudsRepositoryImpl.kt` | exactly two requests for two Refreshes, each = `CAP-062` 2777 byte for byte; no answer ⇒ Case unchanged |
| **Reads (ADR-036)** | `Maestro.READABLE_FIELDS` = {2, 4, 7, 16, 17, 18, 19, 22}; `SettingsCodec.decode` (`4:{N:varint}`, zigzag 17, field 7 nested, never throws); `RoutedFrame.Setting`; Connect sequence EQ read → six reads (sequential, ≤ 2 s each, one pass) → `SubscribeRuntimeInfo`; `BudsSettings` (each value with time, reset at Connect), `settingsError` (`SettingsFailure(error, write)`) | `SettingFrame.kt`, `Maestro.kt`, `CodecRouter.kt`, `EqFrameDecoder.kt` (now only 16/18), `BudsSettings.kt`, repository | `SettingsCodecTest` (14): requests = `CAP-036` 1445…1538, answers 1447…1540 through the router, rejected read 1441, EQ answer 1525 stays EQ, fuzz (random, every truncation, 300 mutations × 17 frames); repository: reads + values + times, unanswered/rejected read (no retry, sequence continues), reset at Connect |
| **Writes (ADR-045)** | `setVolumeBalance` (clamped ±100), `setMonoAudio`, `setConversationDetection`, `setTouchControls`, `setPressAndHold(bud, action)` — one `WriteSetting` each on the announced channel/address, `writeGate` first, applied only on an empty `RESPONSE` OK (`changedByApp = true`); field 12 cannot be encoded | `SettingFrame.kt`, `BudsRepositoryImpl.kt`, `BudsRepository.kt` | byte-identical: `CAP-022` 1922 (−100), 1621/1823, `CAP-021` 1895/3619/4315/4976 on ch 19, `CAP-019` 1720 and `CAP-020` 1995/1741 on ch 21, ACKs 1629/1731; timeout ⇒ read value kept, not retried; error status (hand-built, labelled supplementary — no capture has a rejected write) ⇒ kept + reason; Safe Mode ⇒ nothing sent; not Ready ⇒ nothing sent |
| **UI** | Tab "Sound" (EQ + Balance slider L…R with position = −value, Mono, Conversation detection); new tab "Controls" (touch controls, press-and-hold chips per bud, in-ear detection setting read-only); texts as chosen (§4) | `EqScreen.kt`, `ControlsScreen.kt`, `SettingsUi.kt`, `OpenControlNavHost.kt`, `MainActivity.kt` | compile + lint only; `CAP-063` |

## 6. Gate per phase

| After | `:data` | `:hardware` | `:domain` | Lint |
|---|---|---|---|---|
| baseline (`0f36bd5`) | 1509 | 49 | 17 | `:app` 0 errors / 1 warning (`DataExtractionRules`); others "No issues found" |
| B (EQ layout) | 1509 | 49 | 17 | unchanged |
| C (Refresh) | 1512 | 49 | 17 | unchanged |
| D/F (`:data` runs only) | 1529 → 1536 | — | — | — |
| **final, after `./gradlew --offline clean`** | **1536** | **49** | **17** | unchanged; Kotlin warnings: the same 3 pre-existing (`BudsCompanionPairing.kt` ×2, `LinkEvaluation.kt`), none new |

Four existing tests had to change because they encoded the old behaviour (reads of 16/18 only): `PwRpcTest` "refuses to build a read …" (now asserts
the new set, and that 11/12/15/27/28/29 stay unreadable), and three repository tests of the Connect sequence (now answered with the real `CAP-036`
sweep; they assert the order EQ → six reads → subscription).

## 7. Mutation checks (`mutate.py`: apply, run `:data:testDebugUnitTest`, restore, compare SHA-256 before/after)

| # | Mutation | Failing tests | Restored identical |
|---|---|---|---|
| M1 | zigzag removed (`val zigzag = v`) | 2 | yes |
| M2 | wrong field number (mono 19 → 20) | 9 | yes |
| M3 | write applied without ACK (timeout ⇒ success) | 1 | yes |
| M4 | Refresh reuses the open claim (`if (false && freshOpen …)`) | 1 | yes |
| M5 | re-subscription sent twice per Refresh | 1 | yes |
| M6 | field 12 made readable | 2 | yes |

## 8. Compliance

- No `INTERNET`, no new permission, no manifest / Gradle / version-catalog change (`git diff --name-only -- '*.gradle.kts' '*.toml' '*AndroidManifest.xml'` empty).
- New `transport.send` call sites (all DLCI 0x02): `readSettings` → `ReadSetting 4:N` for 2, 4, 7, 17, 19, 22 (**ADR-036**); `writeSetting` →
  `WriteSetting` for 17, 19, 22, 4, 7 (**ADR-045**). The existing `subscribeRuntimeInfo` site is now also reached from *Refresh battery*
  (**ADR-043 Update 2026-09-26**). The Refresh fix adds claim operations only (close + open of DLCI 0x04, ADR-032), no new message type.
- Nothing is sent on DLCI 0x08; nothing names field 12 (no encoder for it; `readSettingRequest(…, 12)` is `null`, tested; M6).
- Every write passes `writeGate` (ADR-042); the firmware allowlist is unchanged.

## 9. Re-test plan for Group AY — `CAP-063`

Written, on the maintainer's request (chat 2026-09-26), as a complete capture skeleton:
`captures/CAP-063-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AY/CAP-063-EVENT-NOTES.md` (Pixel 9a / GrapheneOS, OpenControl; registered *planned* in
`id_registry.csv`; no Group added to `CAPTURE_BLUETOOTH_HCI_SNOOP.md` — not asked). It merges `0048` §9 (AY-0 … AY-12) and `0051` §19 (AY-13, AY-14)
with this session's steps. Summary:

| Step | Action | Expected screen | Expected HCI bracket |
|---|---|---|---|
| AY-13a/b | Refresh once; then twice within 1 s | new times each time, or "No new battery reading …"; the Case note | per Refresh: (`DISC`) `SABM` 0x04 → `03 03`; one `SubscribeRuntimeInfo` |
| AY-14 | both docked, lid open, idle 2 min, Refresh | Case updated or unchanged with its old time | `SubscribeRuntimeInfo` → `SERVER_STREAM` within 1 s, or none (🔴) |
| D1–D4 | Balance L, R, halfway, centre (say what you hear) | "Left 100 · changed …" etc. | `4:{17:200}`, `4:{17:199}`, … → empty `RESPONSE` |
| D5/D6 | Mono on/off | switch follows | `4:{19:1}`, `4:{19:0}` |
| D7 | Conversation detection off/on, speak | switch follows; what the Buds do | `4:{22:0}`, `4:{22:1}` |
| D8/D9 | Touch controls off/on, tap a bud | tap ignored / works | `4:{4:0}`, `4:{4:1}` |
| D10–D12 | Press-and-hold L/R Assistant ↔ Noise control, hold the bud | chip follows | `4:{7:{1|2:{4:{1:6|5}}}}` |
| D13 | Disconnect, Connect | written values read back | `ReadSetting` answers = last writes |
| B2/H0 | Sound/Controls tabs, presets 3 + 2 | values with "read …"; no label cut off | the six `ReadSetting`s |
| AY-1, AY-2, AY-2b | docked: Ring, EQ write, mono write | ring? accepted? | ACK/NAK; `RESPONSE` or `status ≠ OK` |
| AY-3a–c | one bud worn, the other on the table | ANC enabled or not | `08 11` → `08 13 … <settable>` |
| Group AR (`CAP-056`, Pixel 7a) | the ANC-list re-run with the `0051` F-6 additions | — | `WriteSetting 4:{12:{…}}` per tap |

Refuted if: a settings value changes on screen without the Buds' `RESPONSE`; any request names field 12; two `SubscribeRuntimeInfo` per Refresh; a
Refresh shows new times without a `03 03` frame on its claim; plus the `0048` §9 criteria (repeated in `CAP-063-EVENT-NOTES.md` §7).

## 10. Documentation updated

`ARCHITECTURE.md` (§2.4 tabs and the "5 presets" correction; §3.1 battery row and a new settings row; §3.1's request list; §5a settings and Case
rows), `APP_TESTPLAN.md` (header note, E8/E9, H0, sections M and N, summary), `TODO.md` (done item, `CAP-063` pointer, Group AR additions),
`CHANGELOG.md`, `README.md` status, `PROJECT.md` (touch controls [~], balance/mono/conversation detection [x]), `ai-sessions/INDEX.md` (0052 row; 0051
row and its RESULT's Status → complete, `AI_SESSION_LOG_PROCEDURE.md` §4a — its proposals were approved in this chat and recorded here);
`CAP-034-FINDINGS.md` §4/§8/§9 and `CAP-034-EVENT-NOTES.md` (`FE2C1238…` = FHN "Beacon actions", rewritten in place, rule 9a).

## 11. Open items

- Nothing is hardware-verified; `CAP-063` is the test. Open 🔴: whether the Buds answer a second `SubscribeRuntimeInfo`; whether balance/mono are
  audible as expected; what conversation detection does while talking.
- `qht` field 12 bit order and the one-list question → Group AR (`CAP-056`, Pixel 7a); only then a field-12 ADR.
- `CAP-034-FINDINGS.md`/`CAP-034-EVENT-NOTES.md` called `FE2C1238` unnamed — rewritten in place on the maintainer's request in chat (2026-09-27),
  pointing to `PROTOCOL.md` §6's 2026-09-26 Update (§10).
- The Balance slider shows "Centre" only at exactly 0; the Buds read 1/2 after an imprecise drag (`CAP-046`: 0/1/1/2) — shown as "Left 1". Kept as the
  Buds' value (never rounded); a UX choice for later.
- `python3 scripts/ensure_footers.py` and `python3 scripts/lint_docs.py` → exit 0 (the dead-reference list it prints is informational, historical
  session logs only).

## 12. Commits

- `7551078` "Feature result 0052" — made by the maintainer during the usage-limit break (2026-09-26 09:40): Phase A (`PROTOCOL.md`, `DECISIONS.md`,
  `id_registry.csv`, `CAP-019`/`020`/`022`) and the code/tests of Phases B–D/F as they stood then (`:data` tests green at that point).
- `94e4fb1` `feat(app)` — the settings UI (tabs Sound and Controls), `SettingsFailure`; built and tested together with the rest (final gate §6).
- the `docs` commit on top — the remaining documentation, `CAP-063` skeleton, `CAP-034` rewrite and these session files.
- `git fetch` before pushing: `origin/main` = `7551078`, nothing to rebase. `.vscode/` (not this session's) left untracked.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0052_FEATURE_RESULT_2026_09_26.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0052_FEATURE_RESULT_2026_09_26
