# 0074_FEATURE_RESULT_2026_10_06.md — Build five settings switches (Multipoint ADR-053, Head gestures ADR-052, Case sounds ADR-054, Volume EQ ADR-055) with real capture bytes as fixtures, the screen-reader text for unread values, the hardware-run skeleton, and the preparation of release 1.1.0

**Number:** 0074
**Category:** FEATURE
**Date:** 2026-10-06
**Title:** Implement the switches Multipoint (`qhr` field 11), Head gestures (field 29), Case sounds "Other alerts" and "Earbuds replaced" (fields 27
and 28) and Volume EQ (field 15) in the OpenControl app, the screen-reader text for a value that was not read, the documents, the hardware-run
skeleton and the release preparation for 1.1.0
**Status:** complete — committed on `feature/0074-settings-switches`, pull request opened (not merged); the hardware run `CAP-070` is the maintainer's next step

---

## Summary (plain language)

**What the app user will see in 1.1.0.** On **Controls** three new cards: **Head gestures** ("Use head gestures", below "Press and hold"), **Multipoint**
and **Case sounds** ("Earbuds replaced", "Other alerts"), below "In-ear detection". On **Sound** a **Volume EQ** switch at the bottom of the equalizer. Each
new switch shows the Buds' own value, read when the app connects, with its time in the card's (i); a tap sends one request — the official app's own, for
the same setting — and the switch moves only when the Buds confirm it. A switch whose value was not read now shows "—" instead of a greyed "off" (also the
four existing switches), and every "—" in the app is read by a screen reader as "Not read from the Buds yet". The app reads twelve settings at connect
(seven before). Nothing else new goes to the Buds.

**What the maintainer has to do next.**

1. Decide on the commits (task 24, asked at the end of this session).
2. Build the release APK from the pushed branch tip: `scripts/release.sh 1.1.0` (`RELEASING.md` §5, checklist B1–B3) — you, not the agent.
3. Install it **over 1.0.1** in the test user and run `CAP-070` on film with the screen recording on
   (`captures/CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF/CAP-070-EVENT-NOTES.md`, P0 … BF-end; about 35 minutes — if it runs long, stop after section
   III and film IV–VI as a second film).
4. Put the films, HCI logs, exports, logcat, the two `uiautomator` dumps and the P0/P1 outputs into that folder; a CAPTURE session analyses it and gives the
   release verdict (checklist C3–C4); only then merge, tag and publish (D1–D4).

## Progress

- **Phase 0 — done.** `git log -1`: `fcd863e Merge pull request #6 from tedsluis/docs/adr-054-055-case-sounds-volume-eq`. `git status --short`:
  `?? ai-sessions/0074_FEATURE_PROMPT_2026_10_06.md`. `git fetch && git log --oneline HEAD..origin/main`: empty. Branch `feature/0074-settings-switches`
  from `origin/main`. Baseline gate green (§A.0).
- **Phase A — done** (§A). No code changed.
- **Phase B — done** (§B, answers verbatim).
- **Phase C (Multipoint) — built and tested** (targeted: `:data` `SettingsCodecTest` + `BudsRepositoryImplTest`, `:ui` `ControlsScreenTest`, `:app` compile —
  green). Files: `SettingFrame.kt`, `Maestro.kt`, `BudsSettings.kt`, `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `ControlsScreen.kt`, `OpenControlNavHost.kt`,
  `MainActivity.kt`; tests `SettingsFixtures.kt` (`Settings036.READ_11_*`, new `Settings074`), `SettingsCodecTest.kt`, `BudsRepositoryImplTest.kt`
  (the read list is now one `settingReads` table), `ControlsScreenTest.kt`.
- **Phase D (Head gestures) — built and tested** (same targeted set, green): `SettingValue.HeadGestures`, `SettingsCodec.headGesturesRequest`,
  `HEAD_GESTURES_OFF/ON` = 1/2, field 29 never in `WRITABLE_FLAG_FIELDS`; `setHeadGestures`; the "Head gestures" card below "Press and hold".
- **Phase E (Case sounds) — built and tested** (green): fields 27/28 as flags; `setCaseSoundOtherAlerts` / `setCaseSoundEarbudsReplaced`; the "Case sounds"
  card (Earbuds replaced, Other alerts) below "Multipoint"; the real pushes `CAP-058` 5682/5627 tell 27 and 28 apart on the read side.
- **Phase F (Volume EQ) — built and tested** (green, `EqScreenTest` added to the set): field 15 as a flag with the ADR-055 `// TODO(verify)` on
  `SettingsCodec.FIELD_VOLUME_EQ`; `setVolumeEq`; the switch at the bottom of the Equalizer card, its line in that card's (i). The channel-21 "on" frame is
  `Settings074.VEQ_ON_CH21_DERIVED` (labelled; CRC computed with zlib, `9977e84e`).
- **Phase G (screen-reader text) — built and tested** (`:ui` all tests green): `NotReadValue` + `NOT_READ_DESCRIPTION` (`SettingsUi.kt`) for the EQ bands, the
  balance and — the checkpoint's "— voor alle schakelaars" — every unread switch (`SettingSwitchRow`); tests in `EqScreenTest`, `ControlsScreenTest`.
- **Phase H — done** (§H): clean + full gate green (data 1627, domain 37, hardware 64, ui 69; lint unchanged; 0 warnings); M1–M9 all killed,
  files restored byte-identical (sha256); compliance clean. Two edits after the gate (test-only: the independent zlib cross-check of the channel-19
  frames in `SettingsCodecTest`; a `DisplayName`) — the final gate is re-run in Phase J after the version change.
- **Phase I — done:** `captures/CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF/CAP-070-EVENT-NOTES.md`; `id_registry.csv` (`CAP-070` planned, `INEAR-005`);
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BF, Capture Index row); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`INEAR-005`, BF in 13 rows); `APP_TESTPLAN.md` (section T,
  A5, M1, N1, O8, Summary).
- **Phase J — done:** documents (§J) and the release preparation for 1.1.0 (versionName 1.1.0, versionCode 10100; `CHANGELOG.md`, `README.md`,
  `scripts/release_notes.template`); `ensure_footers.py` (1 file) and `lint_docs.py` exit 0; the final gate after the version change (§H.1).
- **Phase K — done.** Commit question answered in chat 2026-10-06 (`AskUserQuestion`): *"Ja: commit, push, PR (Recommended)"*, *"Per onderwerp
  (Recommended)"*, labels *"Laten zoals gekozen (Recommended)"*. Claude Code was OOM-killed after the fifth commit, during a `lint_docs.py` check of the
  intermediate commits in temporary worktrees (nothing pushed then); the session was resumed, the state checked (`git status`, `git worktree list`: no
  leftover worktree, no Gradle daemon, `origin/main` unchanged) and finished.
- Intermediate results (scratchpad of this session; re-create if gone): `fixtures.py` (the frame re-derivation script quoted in §A.1), `mutate.py` (§H),
  the `scripts/pwrpc_decode.py` output per capture, the script's output per capture, and the gate logs.

---

## A. Phase 0 and Phase A — evidence and design check (no code change)

### A.0 Baseline gate (before any change)

`cd android && ./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` → `BUILD SUCCESSFUL`, exit 0 (the dependency cache was
present; `--offline` kept). Unit tests from the JUnit XML (`*/build/test-results/*/*.xml`): `:data` 1601 (debug) / 1601 (release), `:domain` 37,
`:hardware` 64 / 64, `:ui` 58 / 58 — 0 failures, 0 errors, 0 skipped. Lint (`lint-results-debug.xml`): `:app` 0, `:data` 0, `:hardware` 0, `:ui` 1
(`ModifierParameter`, Warning, `SettingsMenu.kt:88`, pre-existing). Kotlin compiler warnings: 0 `w:` lines — but every module except `:app` was
`UP-TO-DATE`; a clean baseline build (`./gradlew --offline --max-workers=2 clean assembleDebug testDebugUnitTest test`, exit 0) printed **0** `w:`
lines either — the baseline has no Kotlin compiler warning.

### A.1 Fixtures re-derived from the logs (task 3)

**Scoping (`AGENTS.md` §13 step 1):** `bluetooth.addr == 04:00:6e:cf:6e:07` matches **0** frames in all eight logs (`tshark -r <log> -Y "bluetooth.addr
== 04:00:6e:cf:6e:07" | wc -l` → 0, exit 0), so every filter is scoped by connection handle, taken from the HCI Connection Complete events for that
address (`tshark -r <log> -Y "bthci_evt.code==0x03" -T fields -e frame.number -e bthci_evt.bd_addr -e bthci_evt.connection_handle -e bthci_evt.status`):
`CAP-015` 0x0004 (1402), `CAP-019`/`020`/`022`/`024` 0x0002 (474/497/376/383), `CAP-041` 0x0002/0x0004/0x0005 (279/1414/2642), `CAP-058`
0x0005/0x0008/0x000a/0x000c/0x000d (2175/3292/3361/3428/3749), `CAP-069` 0x0002/0x0001/0x0002 (291/6584/7763). Positive control: the handle filter
matches every frame below (each line shows its `chandle`).

**Command** (scratch script, scratchpad `fixtures.py`): for each frame `tshark -r <log> -Y "btrfcomm.len>0 && (btrfcomm.dlci==2 || btrfcomm.dlci==3) &&
(bthci_acl.chandle==<h> …)" -T fields -e frame.number -e frame.time_utc -e frame.p2p_dir -e bthci_acl.chandle -e btrfcomm.dlci -e data.data`, split on
`0x7e`, HDLC-unescape, `zlib.crc32` over address + control + payload against the trailing 4 bytes (little-endian), then the `RpcPacket` fields; for a
`WriteSetting` request the next Buds `RESPONSE` and `SubscribeToSettingsChanges` packet on the same handle. Cross-checked with `python3 scripts/pwrpc_decode.py
<log>` (it reassembles packets split over RFCOMM frames — `CAP-069` 2737's mirror arrives in the reassembled packet ending in frame 2744). **Every frame
below: CRC OK, control `03`.** Times are UTC (film overlay = UTC + 2 h).

**Writes** (phone → Buds; request address `00 4b` = channel 21, `00 3b` = channel 19; `→` = the mirrored stream packet, then the empty `RESPONSE`):

| Item | Capture · frame · time (UTC) | Raw frame | Decoded | Mirror → RESPONSE |
|---|---|---|---|---|
| MP | `CAP-069` 3161 · 14:12:06.338 | `7e004b0310151dea71de7d5e251d9a8c9e2a0422025800ad636bac7e` | ch 21 `4:{11:0}` | 3165 → 3170 |
| MP | `CAP-069` 3245 · 14:12:21.524 | same bytes as 3161 | ch 21 `4:{11:0}` | 3247 → 3254 |
| MP | `CAP-069` 3212 · 14:12:14.919 | `7e004b0310151dea71de7d5e251d9a8c9e2a04220258013b536cdb7e` | ch 21 `4:{11:1}` | 3214 → 3219 |
| MP | `CAP-069` 3275 · 14:12:27.685 | same bytes as 3212 | ch 21 `4:{11:1}` | 3277 → 3285 |
| MP | `CAP-019` 2482 · 05:39:13.783 | = `CAP-069` 3161 | ch 21 `4:{11:0}` | 2486 → 2489 |
| MP | `CAP-019` 2293 · 05:38:01.609 | = `CAP-069` 3212 | ch 21 `4:{11:1}` | 2295 → 2299 |
| HG | `CAP-069` 2492 · 14:09:18.409 | `7e004b0310151dea71de7d5e251d9a8c9e2a052203e80101bc106ba27e` | ch 21 `4:{29:1}` (off) | 2502 → 2506 |
| HG | `CAP-069` 2737 · 14:10:17.740; 2923 · 14:11:01.921 | = 2492 | ch 21 `4:{29:1}` | 2744 (reassembled) → 2744; 2932 → 2934 |
| HG | `CAP-069` 2564 · 14:09:29.409 | `7e004b0310151dea71de7d5e251d9a8c9e2a052203e801020641623b7e` | ch 21 `4:{29:2}` (on) | 2566 → 2568 |
| HG | `CAP-069` 2831 · 14:10:42.847; 3025 · 14:11:18.982 | = 2564 | ch 21 `4:{29:2}` | 2835 → 2837; 3031 → 3033 |
| HG | `CAP-020` 2038 · 05:47:35.122 / 1935 · 05:47:00.005 | = `CAP-069` 2492 / 2564 | ch 21 `29:1` / `29:2` | 2044 → 2046 / 1939 → 1942 |
| HG | `CAP-041` 2268 · 15:14:42.431 (handle 0x0004) | = `CAP-069` 2492 | ch 21 `4:{29:1}` | 2270 → 2271 |
| CS-27 | `CAP-024` 2053 · 06:33:02.060 | `7e003b0310131dea71de7d5e251d9a8c9e2a052203d80100fa03b6d77e` | ch 19 `4:{27:0}` | 2060 → 2061 |
| CS-27 | `CAP-024` 2084 · 06:33:12.485 | `7e003b0310131dea71de7d5e251d9a8c9e2a052203d801016c33b1a07e` | ch 19 `4:{27:1}` | 2090 → 2091 |
| CS-27 | `CAP-058` 5680 · 19:44:43.023 (handle 0x000d) | `7e004b0310151dea71de7d5e251d9a8c9e2a052203d80100bac507f17e` | ch 21 `4:{27:0}` | 5682 → 5683 |
| CS-27 | `CAP-058` 5697 · 19:44:52.871 | `7e004b0310151dea71de7d5e251d9a8c9e2a052203d801012cf500867e` | ch 21 `4:{27:1}` | 5701 → 5702 |
| CS-28 | `CAP-024` 1988 · 06:32:38.084 | `7e003b0310131dea71de7d5e251d9a8c9e2a052203e00100d2b7cefd7e` | ch 19 `4:{28:0}` | 1990 → 1992 |
| CS-28 | `CAP-024` 2023 · 06:32:50.500 | `7e003b0310131dea71de7d5e251d9a8c9e2a052203e001014487c98a7e` | ch 19 `4:{28:1}` | 2029 → 2031 |
| CS-28 | `CAP-058` 5623 · 19:44:25.807 | `7e004b0310151dea71de7d5e251d9a8c9e2a052203e0010092717fdb7e` | ch 21 `4:{28:0}` | 5627 → 5628 |
| CS-28 | `CAP-058` 5643 · 19:44:33.955 | `7e004b0310151dea71de7d5e251d9a8c9e2a052203e00101044178ac7e` | ch 21 `4:{28:1}` | 5647 → 5648 |
| VEQ | `CAP-022` 1871 · 06:16:15.825 | `7e003b0310131dea71de7d5e251d9a8c9e2a04220278003fab19517e` | ch 19 `4:{15:0}` | 1876 → 1877 |
| VEQ | `CAP-022` 1895 · 06:16:24.090 | `7e003b0310131dea71de7d5e251d9a8c9e2a0422027801a99b1e267e` | ch 19 `4:{15:1}` | 1899 → 1900 |
| VEQ | `CAP-015` 3487 · 04:17:19.705 / 3505 · 04:17:29.417 (handle 0x0004) | = `CAP-022` 1871 / 1895 | ch 19 `15:0` / `15:1` | 3494 → 3495 / 3510 → 3511 |
| VEQ | `CAP-041` 2461 · 15:15:05.973 (handle 0x0004) | `7e004b0310151dea71de7d5e251d9a8c9e2a04220278000f47ef397e` | ch 21 `4:{15:0}` | 2463 → 2465 |

The empty `RESPONSE` is byte-identical per channel in every pair: channel 21 `7e00a503080110151dea71de7d5e251d9a8c9e036d4ed87e` (= the existing fixture
`SettingsWrites.ACK_CH21_1731`), channel 19 `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e` (= `ACK_CH19_1629`). The Multipoint SASS capability
flags follow each `CAP-069` write on DLCI 0x04 (`tshark … -Y 'bthci_acl.chandle==0x0002 && btrfcomm.len>0 && (frame.number==3166 || …)'`): 3166
`0711000401029800`, 3215 `071100040102b800`, 3248 `…9800`, 3278 `…b800` (ADR-053's bytes).

**Reads** (Buds → phone `RESPONSE`, response address `00 a5` = channel 21, `80 a3` = channel 19), with the official app's request and the latency:

| Field | Request (frame, raw) | Answer (frame, time UTC, raw) | Decoded | Latency |
|---|---|---|---|---|
| 11 | `CAP-069` 1261 `7e004b0310151dea71de7d5e2551aed0ae2a02200bab27c8457e` | 1267 · 14:06:00.947 `7e00a5032a0422025801080110151dea71de7d5e2551aed0ae6e85eb5c7e` | ch 21 `4:{11:1}` | 68 ms |
| 11 | `CAP-069` 7270 `7e003b0310131dea71de7d5e2551aed0ae2a02200b11675aae7e` | 7272 · 14:20:10.681 `7e80a3032a0422025801080110131dea71de7d5e2551aed0aecd273bcd7e` | ch 19 `4:{11:1}` | 102 ms |
| 11 | `CAP-024` 1009 (= `CAP-069` 7270) | 1014 · 06:31:38.393 (= `CAP-069` 7272) | ch 19 `4:{11:1}` | 241 ms |
| 29 | `CAP-069` 1222 `7e004b0310151dea71de7d5e2551aed0ae2a02201dfa921cb17e` | 1226 · 14:06:00.664 `7e00a5032a052203e80102080110151dea71de7d5e2551aed0ae898482177e` | ch 21 `4:{29:2}` | 56 ms |
| 29 | `CAP-069` 7202 `7e003b0310131dea71de7d5e2551aed0ae2a02201d40d28e5a7e` | 7209 · 14:20:09.097 `7e80a3032a052203e80102080110131dea71de7d5e2551aed0aedac9445f7e` | ch 19 `4:{29:2}` | 149 ms |
| 29 | `CAP-024` 1097 (= `CAP-069` 7202) | 1100 · 06:31:39.865 `7e80a3032a052203e80101080110131dea71de7d5e2551aed0aea3a3394e7e` | ch 19 `4:{29:1}` | 74 ms |
| 29 | (`CAP-020`) | 1183 · 05:46:22.291 `7e00a5032a052203e80101080110151dea71de7d5e2551aed0aef0eeff067e` | ch 21 `4:{29:1}` | — |
| 29 | `CAP-058` 4518 (= `CAP-069` 1222) | 4520 · 19:42:28.036 (= `CAP-069` 1226) | ch 21 `4:{29:2}` | 50 ms |
| 27 | `CAP-024` 1089 `7e003b0310131dea71de7d5e2551aed0ae2a02201b7577edb37e` | 1092 · 06:31:39.689 `7e80a3032a052203d80101080110131dea71de7d5e2551aed0ae2408cb777e` | ch 19 `4:{27:1}` | 58 ms |
| 27 | `CAP-058` 4512 `7e004b0310151dea71de7d5e2551aed0ae2a02201bcf377f587e` | 4514 · 19:42:27.927 `7e00a5032a052203d80101080110151dea71de7d5e2551aed0ae77450d3f7e` | ch 21 `4:{27:1}` | 51 ms |
| 28 | `CAP-024` 1093 `7e003b0310131dea71de7d5e2551aed0ae2a02201cd6e2892d7e` | 1096 · 06:31:39.783 `7e80a3032a052203e00101080110131dea71de7d5e2551aed0ae3dec29a87e` | ch 19 `4:{28:1}` | 84 ms |
| 28 | `CAP-058` 4515 `7e004b0310151dea71de7d5e2551aed0ae2a02201c6ca21bc67e` | 4517 · 19:42:27.975 `7e00a5032a052203e00101080110151dea71de7d5e2551aed0ae6ea1efe07e` | ch 21 `4:{28:1}` | 39 ms |
| 15 | `CAP-024` 1031 `7e003b0310131dea71de7d5e2551aed0ae2a02200f08a337a97e` | 1038 · 06:31:38.756 `7e80a3032a0422027801080110131dea71de7d5e2551aed0aeb05c04da7e` | ch 19 `4:{15:1}` | 72 ms |
| 15 | `CAP-058` 2928 `7e004b0310151dea71de7d5e2551aed0ae2a02200fb2e3a5427e` | 2930 · 19:41:24.811 `7e00a5032a0422027801080110151dea71de7d5e2551aed0ae13fed44b7e` | ch 21 `4:{15:1}` | 45 ms |
| 15 | (`CAP-041`) | 3239 · 15:15:41.784 (handle 0x0005) `7e00a5032a0422027800080110151dea71de7d5e2551aed0aefb252ff27e` | ch 21 `4:{15:0}` | — |

Further reads seen and used only as latency samples: `CAP-069` 1155 → 1164 (15, 49 ms), 1215 → 1220 (27, 40 ms), 1227 → 1236 (28, 46 ms), 7160 → 7162
(15, 108 ms), 7186 → 7188 (27, 21 ms), 7189 → 7197 (28, 194 ms); `CAP-041` 868 and 2007 (`4:{15:1}`, = `CAP-058` 2930's bytes).

**Which channel forms exist as real bytes:**

| Item | Write ch 21 | Write ch 19 | Read answer ch 21 | Read answer ch 19 | Read request ch 21 / 19 |
|---|---|---|---|---|---|
| MP (11) | 0 and 1 | **none captured** | `11:1` | `11:1` | both |
| HG (29) | 1 and 2 | **none captured** | `29:2`, `29:1` | `29:2`, `29:1` | both |
| CS-27 | 0 and 1 | 0 and 1 | `27:1` | `27:1` | both |
| CS-28 | 0 and 1 | 0 and 1 | `28:1` | `28:1` | both |
| VEQ (15) | 0 only — **`15:1` not captured** | 0 and 1 | `15:1`, `15:0` | `15:1` | both |

**Check against the prompt's §2 table and the ADRs:** every frame number, value and channel of the prompt's table and of ADR-052…055 matches the
logs; no correction. Additions not in the prompt: the read requests above, `CAP-058` 4520 (`4:{29:2}`, ch 21), `CAP-020` 1183 (`4:{29:1}`, ch 21,
already in `PROTOCOL.md` §4.5.4's 2026-10-03 Update). `CAP-042`'s `.log.last` was not used.

**The official app's sweep order** (`grep REQUEST.*ReadSetting` over the `scripts/pwrpc_decode.py` output of `CAP-024` / `CAP-058`): ascending field number — `CAP-024` 801 `4:13`, 837 `4:1`,
862 `4:2` … 973 `4:7`, 1009 `4:11`, 1015 `4:12`, … 1031 `4:15`, … 1053 `4:17`, … 1064 `4:19`, … 1070 `4:22`, … 1089 `4:27`, 1093 `4:28`, 1097 `4:29`, …;
`CAP-058` 4452 … 4518 the same. The app's existing order 2, 4, 7, 12, 17, 19, 22 is that order; the five new fields slot in as 2, 4, 7, **11**, 12,
**15**, 17, 19, 22, **27**, **28**, **29**.

### A.2 Code paths, as built, and the planned change (task 4)

Read in full: `SettingFrame.kt`, `Maestro.kt`, `BudsSettings.kt`, `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `SafeModeGate.kt`, `SettingsCodecTest.kt`,
`SettingsFixtures.kt`, `FakeBudsTransport.kt`, `ControlsScreen.kt`, `SettingsUi.kt`, `EqScreen.kt`, `Details.kt`, `ControlsScreenTest.kt`. Read in part:
`BudsRepositoryImplTest.kt` (lines 1–255, 405–625, 1278–1620: the helpers, every settings read/write/Safe-Mode test; the ANC, battery, loss and re-open
tests by name only), `CodecRouter.kt` (`routeMaestro`, lines 270–320), `OpenControlNavHost.kt` (`OpenControlActions`, the tab routing, lines 95–160,
370–413), `MainActivity.kt` (the actions and the pull, lines 480–545).

- **Decode.** Inbound DLCI 0x02 → `CodecRouter` → `routeMaestro` (`CodecRouter.kt:279`): a `ReadSetting`/`WriteSetting`/`SubscribeToSettingsChanges`
  `RESPONSE`/`SERVER_STREAM` with status OK and a payload goes first to `EqFrameDecoder`, then to `SettingsCodec.decode` (`SettingFrame.kt:140`), which
  accepts exactly one `4:{N: …}` field: `FLAG_FIELDS` (2, 4, 19, 22) as 0/1, 17 zigzag ±100, 7, 12. Anything else returns `null`, and an OK `ReadSetting`
  answer then becomes an OK `RpcResult` (`CodecRouter.kt:305–308`) → `UnreadableAnswer` in `readSettings` (`BudsRepositoryImpl.kt:1317`).
  **Planned:** 11, 15, 27, 28 join `FLAG_FIELDS`; field 29 gets its own `SettingValue.HeadGestures(on)` with `HEAD_GESTURES_OFF = 1` / `HEAD_GESTURES_ON =
  2` — decoded only from 1 or 2, anything else `null` (→ "not read", "—").
- **Sequencing.** `launchInitialEqRead` (`:1282`) → `readEq` → `readSettings` → `subscribeRuntimeInfo`, each under `eqMutex`. `readSettings` (`:1295`) loops
  over `SETTING_READ_ORDER` (`:1430`), one `Maestro.readSettingRequest` (`Maestro.kt:271`, `null` outside `READABLE_FIELDS`, `:256`) per field, one
  `sendAndAwait` of ≤ `SETTING_READ_TIMEOUT_MS` = 2 000 ms (`:1441`), no retry; it stops only on a send failure. `refreshSettings` (`:894`) runs the same pass.
  **Planned:** `READABLE_FIELDS` and `SETTING_READ_ORDER` gain 11, 15, 27, 28, 29 in the official order (§A.1); nothing else.
- **Gate, send, match, apply.** Every setter → `writeFlag` (`:1015`) → `writeSetting` (`:1026`): state check (`Ready`, else `SessionOpening` /
  `ConnectionLost`), `eqMutex`, `awaitMaestroChannel` (`:940`, the announced channel and its ADR-034 address, never guessed), `writeGate(false)` (`:1114`,
  `SafeModeGate.evaluate`: firmware allowlist + no contradicting Model ID), the request built by `SettingsCodec`, `awaitWriteQuarantine` (`:949`, ADR-045
  Update), `sendAndAwait` for an OK `RpcResult` of `WriteSetting` (matched by method only — the known limit) within `EQ_WRITE_ACK_TIMEOUT_MS` = 1 500 ms;
  OK → `applySetting(value, clock(), changedByApp = true)` (`:368`); no answer → `startWriteQuarantine` + `Timeout`; error status → `MaestroRejected`; the
  previous value stays in every failure, the reason in `settingsError`. **Planned:** `setMultipoint`, `setVolumeEq`, `setCaseSoundOtherAlerts`,
  `setCaseSoundEarbudsReplaced` = `writeFlag(11|15|27|28, on)`; `setHeadGestures(on)` = `writeSetting(SettingValue.HeadGestures(on)) {
  SettingsCodec.headGesturesRequest(it, on) }` — the same path, lock and quarantine; `WRITABLE_FLAG_FIELDS` gains 11, 15, 27, 28 (never 29).
- **State.** `BudsSettings` (`BudsSettings.kt:91`) holds one `SettingReading<T>?` per field (value, receive time, `changedByApp`); reset to `BudsSettings()`
  in `openSession` (`:674`). **Planned:** `multipoint`, `volumeEq`, `caseSoundOtherAlerts`, `caseSoundEarbudsReplaced`, `headGestures`, all
  `SettingReading<Boolean>?`.
- **The 1.0.1 "current value" rule** lives in `:ui`: `settingsFromLastConnection(ready, readings)` (`SettingsUi.kt:41`), `withLastConnectionLine`, the
  `SettingsCard` dimming + (i) dot (`ControlsScreen.kt:103–123`), and in `:domain` `isCurrent`. A new switch follows it by being passed in a card's
  `readings`. **Not read:** `SettingSwitchRow` (`SettingsUi.kt:96`) disables the switch while `reading == null` and draws it "off"; `NOT_READ_VALUE` "—"
  (`EqScreen.kt:172`) is shown only by the EQ band labels (`:295`) and the balance (`:261`) — **no switch shows "—" today** (a checkpoint question, §B).
- **Screen-reader text today:** none for "—" (the visible text is read as "—", or skipped, depending on the screen reader).

### A.3 New `transport.send` call sites (task 5)

**None.** The five reads go through the existing `readSettings` loop (`transport.send` at `BudsRepositoryImpl.kt:1313`); the five writes through
`writeSetting` (`:1051`). No new DLCI, no new method, no subscription.

### A.4 The read budget (task 6)

The Connect read (and the pull) grows from 7 to 12 `ReadSetting` requests after the EQ read. Bound per request: `SETTING_READ_TIMEOUT_MS` = 2 000 ms
(ADR-036: "≤ 3 s", never retried); no bound exists for the whole pass, and none is added. Measured official-app latencies for exactly these five fields
(§A.1): 21–241 ms, median 58 ms (19 samples, `CAP-024`, `CAP-058`, `CAP-069`). Expected pass: 12 × ≈ 60 ms ≈ 0.7 s, worst observed sample × 12 ≈ 2.9 s
— all answered. Worst case if the Buds answer nothing: 12 × 2 s = 24 s (was 14 s); the pass stops at once on a send failure (`:1324`). During the pass
`eqMutex` is held, so a tap on any setting waits for the pass to end (unchanged behaviour, a longer bound). **No timing constant changes.**

### A.5 RUN, first pass: `TODO.md` §2 against a release-build run in the GrapheneOS user without Play (task 7)

| `TODO.md` §2 item | Fits this run? | Why |
|---|---|---|
| `APP_TESTPLAN.md` H5 with *Read EQ again* | yes | a tap on Sound; needs nothing else |
| S6 ("—" in the first second after ready) | yes | film the phone's screen with Android's screen recorder (the camera could not resolve it in `CAP-068`) |
| S12 (export across a rotation) | yes | auto-rotate on in the test user |
| S9 (two Pixel Buds paired) | **no** | needs a second Pixel Buds device; one pair exists |
| P1 `dumpsys package … \| grep -E "firstInstallTime\|lastUpdateTime"` | yes | `adb shell dumpsys package io.github.tedsluis.opencontrolpixelbuds` lists the per-user install of the test user; this run is an update over 1.0.1 |
| The Left bud out with both worn on channel 19, head in view (L-1) | yes | needs only the Buds and the film; doubles as the channel-19 steps |
| Channel-19 balance frame `17:7` | yes | the channel-19 section (only the Left bud out) |
| B4 double tap (`AlreadyInProgress`) | **no** | needs the Buds forgotten and re-paired in the test user — a separate pairing section that resets the CDM association mid-run; left for a pairing run |
| K5 / BC-12 (GrapheneOS Bluetooth auto-off) | **no** | the setting exists only in the Owner user |
| The swipe between tabs | yes | the screen recording shows it |
| F-4 on a debug build (StrictMode) | **no** | needs a debug build; this run tests the release build |
| A5 wording | — (text) | a correction of `APP_TESTPLAN.md` step A5 (Phase I task 19), not a run step |
| A second device | **no** | one phone |

**Which channel a step can force:** 🟢 FACT (`PROTOCOL.md` §2.2a, Update of 2026-10-01, `CAP-065` 7 of 7): with only the Left bud out of the case the Buds
announce channel 19, with only the Right out channel 21. Earlier runs came to 19 that way or by a Buds-side `DISC` when the Right left an ear with both
worn on 21 (`CAP-066` 3 of 3, `CAP-068` #10/#13). So the three uncaptured forms get steps with one bud out (Left → 19 for 11 and 29; Right → 21 for
`15:1`), and the step records the channel that occurs (Settings → Info shows "Control channel: 19/21").

---

## B. Phase B — checkpoint (answers verbatim, chat 2026-10-06)

Two rounds of `AskUserQuestion` in this chat (Dutch; the question and the chosen option as shown, the maintainer's own note in quotes).

**Round 1**

| Question (header) | Answer |
|---|---|
| (a) "Volgorde en groepering op het tabblad "Controls" (de vier nieuwe schakelaars) en de plek van Volume EQ op "Sound"?" (Indeling) | *"Gebaren eerst (Recommended)"* — Controls: Touch controls · Press and hold · **Head gestures** · In-ear detection · **Multipoint** · **Case sounds** (Earbuds replaced, Other alerts), one card each with its (i); Sound: **Volume EQ** at the bottom of the Equalizer card, below the presets. |
| (b) "De zichtbare teksten per schakelaar (titel en eenregelige noot)? …" (Teksten) | no option selected; the maintainer's note: *"Laat weg uit de notes uit de teksten."* — confirmed in round 2. |
| (d) "Welke tekst leest een screenreader (TalkBack) voor op de plek van "—" …?" (Screenreader) | *"Not read from the Buds yet (Recommended)"* |
| (e) "Versie en releasevoorbereiding (RELEASING.md §4 stap 1–2 …)? Geen tag, geen release.sh, niets gepubliceerd." (Release) | *"1.1.0, voorbereiden (Recommended)"* |

**Round 2**

| Question (header) | Answer |
|---|---|
| (b) "Je notitie bij de teksten was "Laat weg uit de notes uit de teksten." Ik lees dat als: geen noten en geen ondertitels, alleen de labels. Klopt dat?" (Teksten) | *"Ja, alleen labels (Recommended)"* — labels "Use head gestures", "Multipoint", "Earbuds replaced", "Other alerts", "Volume EQ"; card titles "Head gestures", "Multipoint", "Case sounds"; no note, no subtitle; the (i) lines as for the existing switches. |
| "ADR-052/054/055 zeggen: een niet-gelezen waarde wordt als "—" getoond. … Hoe?" (— bij switch) | *"— voor alle schakelaars (Recommended)"* — while not read, "—" (screen reader: "Not read from the Buds yet") stands in place of the switch, for the new **and** the existing switches; a value from the last connection stays a dimmed, disabled switch with the (i) dot. |
| (c) "Fout- en weigermeldingen voor de nieuwe schakelaars?" (Fouttekst) | *"Bestaande teksten (Recommended)"* — "The setting was not changed: <reason>" / "Couldn't read the Buds' settings: <reason>". |
| (f) "Phase A: geen enkele framenummer-correctie op de ADR's. De Connect-leesronde groeit van 7 naar 12 reads … Geen timingconstante gewijzigd. Akkoord?" (Leesbudget) | *"Akkoord, niets wijzigen (Recommended)"* |

Nothing else was asked; no ADR, no `PROTOCOL.md` status and no timing constant is changed by these answers.

---

## C–G. What was built, per item

All five use the existing paths — no new `transport.send` call site (§A.3), no new DLCI, method or subscription; every write goes through
`writeSetting` (`BudsRepositoryImpl.kt`): state check → `eqMutex` → announced channel and its ADR-034 address → `writeGate` (ADR-042) → the codec's request →
the ADR-045 Update's quarantine → one `WriteSetting` → applied only on the empty `RESPONSE` status OK, otherwise the previous value stays and
`settingsError` says why (the existing texts, checkpoint (c)).

| Item | `:data` | `:domain` / repository | `:ui` / `:app` | Tests (all with real bytes unless labelled) |
|---|---|---|---|---|
| **MP** field 11 (ADR-053) | `FIELD_MULTIPOINT` in `FLAG_FIELDS` and `WRITABLE_FLAG_FIELDS`; `Maestro.READABLE_FIELDS` | `BudsSettings.multipoint`; `setMultipoint(on)` = `writeFlag(11, on)`; read order after 7 | "Multipoint" card below "In-ear detection"; `onMultipointChanged` | codec: `CAP-069` 3161/3212 and `CAP-019` 2482/2293 byte for byte incl. CRC, decode back; reads `CAP-036` 1471 (ch 21) / `CAP-024` 1009 (ch 19), answers `CAP-069` 1267/7272, `CAP-036` 1513; **labelled** ch-19 structural test (+ independent zlib frames); repo: switch on ch 21 (read, off, on, times, nothing on DLCI 0x04), **labelled** ch 19, timeout and error status keep the read value, Safe Mode; UI: card, (i) line, tap |
| **HG** field 29 (ADR-052) | `SettingValue.HeadGestures(on)`; `HEAD_GESTURES_OFF = 1`, `HEAD_GESTURES_ON = 2`; `headGesturesRequest`; decode 1/2 only, else `null`; never in `WRITABLE_FLAG_FIELDS` | `BudsSettings.headGestures`; `setHeadGestures(on)`; read order last | "Head gestures" card ("Use head gestures") below "Press and hold" | codec: `CAP-069` 2492/2564, `CAP-020` 2038/1935, `CAP-041` 2268 byte for byte; payloads `2203e80101`/`2203e80102`; reads `CAP-036` 1559 / `CAP-024` 1097, answers 1226, 7209, `CAP-036` 1561 = on, `CAP-024` 1100, `CAP-020` 1183 = off; **labelled** 0 and 3 not interpreted; **labelled** ch 19; repo: ch 21, **labelled** ch 19, a read of 3 → `UnreadableAnswer` (stays "—"), failures, Safe Mode; UI |
| **CS** fields 27/28 (ADR-054) | `FIELD_CASE_SOUND_OTHER_ALERTS` (27), `FIELD_CASE_SOUND_EARBUDS_REPLACED` (28) as flags; readable | `caseSoundOtherAlerts`, `caseSoundEarbudsReplaced`; `setCaseSoundOtherAlerts`, `setCaseSoundEarbudsReplaced`; read order after 22 | "Case sounds" card: "Earbuds replaced", "Other alerts" | codec: all eight writes byte for byte on both channels (`CAP-024` 1988/2023/2053/2084, `CAP-058` 5623/5643/5680/5697); reads `CAP-036` 1553/1556, `CAP-024` 1089/1093, answers 1092/1096, 4514/4517, 1555/1558; the real pushes `CAP-058` 5682 (`27:0`) / 5627 (`28:0`); repo: ch 21 (the push of 28 leaves 27 alone; a write of one leaves the other), ch 19, failures, Safe Mode; UI |
| **VEQ** field 15 (ADR-055) | `FIELD_VOLUME_EQ` as a flag with the `// TODO(verify)` ADR-055 asks for; readable | `volumeEq`; `setVolumeEq`; read order after 12 | the switch at the bottom of the Equalizer card; its line in that card's (i) | codec: `CAP-022` 1871/1895, `CAP-015` 3487/3505 (ch 19), `CAP-041` 2461 (ch 21 off) byte for byte; **labelled** ch-21 "on" = `VEQ_ON_CH21_DERIVED` (zlib `9977e84e`); reads `CAP-036` 1520 / `CAP-024` 1031, answers 1038, `CAP-058` 2930, `CAP-036` 1522 = on, `CAP-041` 3239 = off; repo: ch 19, ch 21 (off real, on **labelled**), failures, Safe Mode; UI: the switch, the (i) line, an unread Volume EQ marks the card |
| **A11Y** | — | — | `NotReadValue` + `NOT_READ_DESCRIPTION` = "Not read from the Buds yet" (`SettingsUi.kt`) for the EQ bands, the balance and every unread switch (`SettingSwitchRow`: "—" in place of the switch) | `EqScreenTest`: each unread band, the unread balance, the unread Volume EQ — the node's text "—" **and** its content description; `ControlsScreenTest`: six "—" with the description and no switch when nothing is read, one when one is unread, none from the last connection |

Existing tests changed because the behaviour changed (each a guard on the old field lists, not weakened): `SettingsCodecTest` (the writable set, the
"not readable" examples now 13 and 39), `PwRpcTest` (the readable set — caught by the full gate, not by the targeted runs), `BudsRepositoryImplTest` (the
read list is one `settingReads` table; the Connect expectations), `EqScreenTest` (the dash counts; the balance helper reads the other switches),
`ControlsScreenTest` (the cards).

## H. Gate, mutations, compliance

### H.1 Gate

| Run | Command | Result | `:data` | `:domain` | `:hardware` | `:ui` | Lint | `w:` |
|---|---|---|---|---|---|---|---|---|
| Baseline (Phase 0) | `./gradlew --offline --max-workers=2 assembleDebug testDebugUnitTest test lint` | exit 0 | 1601 / 1601 | 37 | 64 / 64 | 58 / 58 | `:ui` 1 (`ModifierParameter`, `SettingsMenu.kt:88`), others 0 | 0 (clean build) |
| Phase H | `clean`, then the same | exit 0 | 1627 / 1627 | 37 | 64 / 64 | 69 / 69 | unchanged | 0 |
| Final (after the version change and the last comment edits) | `clean`, then the same | exit 0 | 1627 / 1627 | 37 | 64 / 64 | 69 / 69 | unchanged (`:ui` 1, `ModifierParameter`, `SettingsMenu.kt:88`) | 0 |

Counts are debug / release unit-test tasks from the JUnit XML; 0 failures, 0 errors, 0 skipped in every green run. The first Phase-H run failed once
(`PwRpcTest` — the readable-set guard, updated) and was re-run green. A run between Phase H and the final one was valid for the build but overlapped
two comment edits in `ControlsScreen.kt`, so the final run was repeated on the finished sources. The debug build's output metadata (`android/app/build/outputs/apk/debug/`) reads
`versionCode 10100`, `versionName "1.1.0"`.

### H.2 Mutations (task 16)

Scratch script `mutate.py` (scratchpad): one textual mutation, then `./gradlew --offline --max-workers=2 <task> --tests …`, then the original bytes written back
and `sha256sum` compared; run one at a time. `:data` tasks: `SettingsCodecTest` + `BudsRepositoryImplTest`; `:ui`: `EqScreenTest` + `ControlsScreenTest`.

| # | Mutation | File | Result | Tests that failed (examples) | Restored |
|---|---|---|---|---|---|
| M1 | field 29 written as 0/1 (`if (on) 1 else 0`) | `SettingFrame.kt` | killed | `headGesturesWritesCh21`, the wire-value test, the ch-19 test; repo: HG ch 21, ch 19, failure test (6) | identical |
| M2 | field 29 decoded with on/off swapped | `SettingFrame.kt` | killed | `headGesturesReads`, wire values, ch 19; repo: the Connect reads, HG ch 21/19 (6) | identical |
| M3 | 27 and 28 swapped in the setters | `BudsRepositoryImpl.kt` | killed | repo: CS ch 21, CS ch 19, the failure test (3) | identical |
| M4 | 27 and 28 swapped in `applySetting` | `BudsRepositoryImpl.kt` | killed | repo: CS ch 21, CS ch 19 (2) | identical |
| M5 | field 11 written with `!on` | `BudsRepositoryImpl.kt` | killed | repo: MP ch 21, MP ch 19 (2) | identical |
| M6 | field 15 dropped from the read order | `BudsRepositoryImpl.kt` | killed | repo: the Connect reads, the pull re-read, the unanswered/rejected/undecodable read tests (7) | identical |
| M7 | a new write applied before its `RESPONSE` | `BudsRepositoryImpl.kt` | killed | repo: the four "without an answer or with an error status keeps the value" tests (4) | identical |
| M8 | a new field allowed past the Safe-Mode gate | `BudsRepositoryImpl.kt` | killed | repo: Safe Mode refuses … (1) | identical |
| M9 | the content description removed | `SettingsUi.kt` | killed | `ControlsScreenTest` (2), `EqScreenTest` (3) | identical |

`sha256sum -c` of the three files against the hashes taken before the run: OK, OK, OK (`SettingFrame.kt` `e578108e…`, `BudsRepositoryImpl.kt` `83e57e54…`,
`SettingsUi.kt` `48b61e90…`).

### H.3 Compliance (task 17)

- `git diff --name-only -- '*.toml' '*AndroidManifest.xml' '*.gradle.kts'`: empty before REL; after REL only `android/app/build.gradle.kts` (versionName and
  versionCode). No dependency, plugin or toolchain change.
- `uses-permission` (`grep -n uses-permission */src/main/AndroidManifest.xml`): `BLUETOOTH_CONNECT`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_CONNECTED_DEVICE` — unchanged; `INTERNET` appears only in the manifest comment that forbids it.
- `transport.send` in the diff: 0 added, 0 removed (`git diff -U0 -- android | grep -E '^\+' | grep transport.send` → nothing).
- Nothing on DLCI 0x08/0x0a: no `Dlci.`, `GSND`, `0x08` or `0x0a` added in `*/src/main`.
- Fields added to the codec: exactly 11, 15, 27, 28, 29 (`git diff -U0 -- android/data/src/main | grep 'const val FIELD_'`).
- Every new write passes `writeGate` (one path, `writeSetting`; M8 shows the Safe-Mode test guards it).
- No new Kotlin file (all changes in existing files, each with its AGPL header); no log line added in `*/src/main` (no MAC address, no payload).

## I. The hardware-run skeleton (`CAP-070`, Group BF) and the test plan

Next free numbers checked: `id_registry.csv` ends at `CAP-069`, Group letters at BE (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`; `grep -rln 'CAP-070\|Group BF'` → no
file). Registered *planned*: `CAP-070`; new Test-ID `INEAR-005` (lead L-1 — no existing Test-ID covered it). The screen-reader step is an app-only check
(`APP_TESTPLAN.md` T11), so it has no Test-ID, like the 1.0.1 steps of section S.

| Step | Action | Expected screen | Expected HCI bracket | Test-ID |
|---|---|---|---|---|
| BF-1 | open the app after the update, Controls at once | "—" about 1 s, then the values (S6, screen recording) | `ReadSetting 4:16` + twelve reads 2, 4, 7, 11, 12, 15, 17, 19, 22, 27, 28, 29, each answered | `PAIR-003`, `BATT-004` |
| BF-2 | Info | "App: 1.1.0, build …", no "-dirty"; "Control channel" | nothing sent | — |
| BF-3 | read Controls and Sound | the cards in the chosen order, labels only, "read HH:MM:SS" | the BF-1 answers | `MULTI-001`, `HEAD-001`, `CASE-001`, `CASE-002`, `AUDIO-002` |
| BF-4 … BF-7 | Multipoint, Use head gestures, Earbuds replaced, Other alerts, Volume EQ **off** | each after its OK, "changed …" | `4:{11:0}`, `4:{29:1}`, `4:{28:0}`, `4:{27:0}`, `4:{15:0}` → `RESPONSE`; SASS `… 98 00` only if a claim is open; nothing on DLCI 0x08 | the five |
| BF-8 | Disconnect, Connect | all five read back off | answers `11:0`, `29:1`, `27:0`, `28:0`, `15:0` | `PAIR-003` + the five |
| BF-9, BF-11 | a bud into the case and out, with Earbuds replaced off / on; say whether the case sounds | re-open by itself | Buds `DISC` + app `SABM` ≈ 1.5 s | `CASE-001` (observation), `CASE-004`, `INEAR-002` |
| BF-10 | all five **on** | after each OK | `4:{11:1}`, `4:{29:2}`, `4:{28:1}`, `4:{27:1}`, `4:{15:1}` (on ch 21 the first capture) | the five |
| BF-12 | Volume EQ off/on at low volume; say what you hear | — | `4:{15:0}`, `4:{15:1}` | `AUDIO-002` (observation) |
| BF-13 | only the Left bud out; a tap during the re-open | "The setting was not changed: The app's channel is being reopened …"; then channel 19 | `DISC` → `SABM` → announcement `10 13`; no write for the early tap | `CASE-004`, `INEAR-002`, `PAIR-003` |
| BF-14, BF-15 | Multipoint off/on, Use head gestures off/on on **channel 19** | after each OK | the **derived** frames `…9d8f9dc47e`, `…0bbf9ab37e`, `…fcd6da847e`, `…4687d31d7e` → `RESPONSE` ch 19 | `MULTI-001`, `HEAD-001` |
| BF-16 | balance Right 4 on channel 19 | "Right 4" | `…2a052203880107e9b86e257e` (derived) | `AUDIO-003` |
| BF-17, BF-18 | the Right in too; then the **Left** out of the ear with both worn on 19 (head in view) | re-open by itself | 🟡 predicted Buds `DISC` + announcement 21 | `INEAR-005`, `INEAR-003`, `INEAR-004`, `CASE-005` |
| BF-19, BF-20 | only the Right bud out (channel 21); Volume EQ off, on | after each OK | `…0f47ef397e` (= `CAP-041` 2461), then **derived** `…9977e84e7e` → `RESPONSE` | `AUDIO-002`, `CASE-005`, `INEAR-003` |
| BF-21 | drag a band; *Read EQ again* (H5) | the value written | `WriteSetting 4:{16:…}` → OK; `ReadSetting 4:16` → the same | `EQS-001` |
| BF-22 | swipe through the tabs | the tab follows (screen recording) | — | — |
| BF-23 | export across a rotation (S12) | "Debug log saved (N lines)." | — | — |
| BF-24 | export; Bluetooth off; force-stop; open; two `uiautomator` dumps (Controls, Sound) | the "—" values; each node `content-desc="Not read from the Buds yet"` | — | — |
| BF-25 | Bluetooth on | ready by itself | the twelve reads | `PAIR-003` |

`TODO.md` §2 items **taken** (task 7, §A.5): H5 (BF-21), S6 (BF-1), S12 (BF-23), P1 with `dumpsys` (P1), lead L-1 (BF-18), the channel-19 `17:7` (BF-16), the
swipe (BF-22). **Not taken**, with the reason: S9 (two Pixel Buds), B4 (the Buds forgotten and re-paired), K5/BC-12 (Owner user only), F-4 (a debug build),
a second device; A5 is a text correction, made in `APP_TESTPLAN.md`. Steps of the earlier skeletons reused: C12 (a tap during a re-open) as BF-13; a write in
Safe Mode has no step in `CAP-067`/`CAP-068` (it needs another firmware) and none here. The run is about 35 minutes; the split point is after section III.

`APP_TESTPLAN.md`: section T (T1–T11) after S, A5 corrected as `TODO.md` §2 asked (revoking in Settings ends the process — `CAP-067` §7), M1/N1 ("—" for an
unread switch), O8 (twelve reads), the header line and the Summary row.

## J. Documents

- `ARCHITECTURE.md`: §2.4 (the tree and the tab contents; "—" in place of an unread switch and the screen-reader text), §3.1 (the "current" rule's last
  sentence; the DLCI 0x02 settings row — twelve reads, the four ADRs' writes), the codec-scope paragraph, §5a's settings row (built in `0074`, fixtures, the
  three forms not yet on the wire). **Correction found:** §5a said fields 12 and 2 "are not" hardware-verified; ADR-046's and ADR-047's Updates of
  2026-10-01 record the passed re-test in `CAP-064` — now "12 and 2 in `CAP-064`". §3.2 unchanged (no timing constant changed, checkpoint (f)).
- `PROJECT.md`: the feature list (head gestures, case sounds ticked; Multipoint `[~]` — the switch only; Volume EQ under the equalizer line) and "Status
  after 1.0.x" (the five fields' path to 1.1.0; `CAP-054`'s result in place of "also open").
- `README.md`: "What it does" / "What it does not do", the Status block (1.1.0 prepared, its known issues), "Current state" (70 captures, 3 planned; **55
  ADRs** — it said 51, stale since `ai-sessions/0071`; the built and the still-open lists).
- `CHANGELOG.md`: the `[1.1.0] - not yet released` block (Added, Changed, Known issues); `[Unreleased]` stays empty.
- `TODO.md`: §2 points to `CAP-070` per item and gains the run and its two checks; §4's five build items, the screen-reader item and the §5a/`PROJECT.md`
  item deleted (done); §5 "Accessibility" replaced by the new 🟡 label item; A5 deleted (done).
- `ai-sessions/INDEX.md`: the 0074 row. `PROTOCOL.md`, `DECISIONS.md`: unchanged (nothing approved for them in this chat).
- **REL** (`RELEASING.md` §4 steps 1–2): `android/app/build.gradle.kts` `versionCode = 10100`, `versionName = "1.1.0"`; `CHANGELOG.md`, `README.md`,
  `scripts/release_notes.template` ("New in 1.1.0", "Known issues in 1.1.0", the feature and "Not included" lines). Not run: `scripts/release.sh`, any tag,
  any GitHub release. The commands for the maintainer (`RELEASING.md` §5–§7, run none of them here):

  ```bash
  scripts/release.sh 1.1.0                                   # on the pushed branch tip; note the build commit it prints
  cp -a dist/1.1.0 ~/opencontrol-1.1.0-tested                # keep the tested build
  adb shell pm list users                                    # the test user's id
  adb install --user <id> dist/1.1.0/opencontrol-pixelbudspro2-1.1.0.apk   # over 1.0.1, no uninstall
  # after CAP-070 is analysed and OK, and the pull request is merged with a merge commit:
  git tag -s v1.1.0 <build commit> -m "OpenControl for Pixel Buds Pro 2 1.1.0" && git push origin v1.1.0
  gh release create v1.1.0 --verify-tag --draft --title "OpenControl for Pixel Buds Pro 2 1.1.0" \
    --notes-file ~/opencontrol-1.1.0-tested/release-notes.md ~/opencontrol-1.1.0-tested/*
  ```
- `python3 scripts/ensure_footers.py` → "Updated 1 file(s)" (the new EVENT-NOTES), exit 0; `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` → exit 0
  ("clean"); `scripts/__pycache__/lint_docs.cpython-314.pyc` unchanged (`git status --short scripts/`).

## K. Notes, sources, reading

**RESULT notes (not built, not proposed as scope):**
- 🟡 A switch row's label and its `Switch` are separate accessibility nodes (seen in `ControlsScreenTest`: two switches in one card are siblings of both
  labels), so a screen reader may announce a switch without its name; not tested on a phone → `TODO.md` §5 (**M**).
- The "Volume level notifications" switch (`qhr` field 21, 🟢 since 2026-10-06) is the one 🟢 setting not built; it would need its own ADR.
- **External sources:** none fetched in this session. Two statements in the skeleton are ⚪ assumptions, marked there: that no screen reader is installed in
  the user without Google Play, and that `uiautomator dump` shows a Compose node's content description as `content-desc`. The SASS bit-2 sentence in BF-4
  is quoted from `PROTOCOL.md` §4.5.2 (fetched 2026-10-04 by `ai-sessions/0071`).
- **Subagents:** none used; every number above was derived in this session.

**Read in full:** `AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `DECISIONS.md` (ADR-001 … ADR-055 with every Update), `TODO.md`,
`AI_SESSION_LOG_PROCEDURE.md`, `APP_TESTPLAN.md`, the prompt; the Kotlin files listed in §A.2 as read in full, and `CAP-068-EVENT-NOTES.md`.
**Read in part:** `PROTOCOL.md` — §0, §1, §2.2a, §4.5 (preamble and §4.5.1–§4.5.9) in full, §6 only the entries on fields 11/15/27/28/29 and the case
sounds (located with `grep`); `RELEASING.md` (checklist, flow, §4–§6); `CHANGELOG.md` (the top and 1.0.x); `README.md` (the sections changed);
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BE, §9's header and the 068/069 rows); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (§0, §4 and the rows changed);
`ai-sessions/INDEX.md` (its last rows); `id_registry.csv` (the CAP/Test-ID rows used); the Kotlin files listed in §A.2 as read in part. **Read late and in
part** (after the build, before the final gate — a deviation from the prompt's order, which asked for them first): `CAP-069-FINDINGS.md` §1–§2 in full (the
section list otherwise), `CAP-058-FINDINGS.md` §6, `ai-sessions/0073` §4.2's rows for 11/15/27/28/29 and §4.5 in full, `ai-sessions/0071` (its header and
"Plain-language answers"), the section lists of `ai-sessions/0052`/`0056`/`0069`; `CAP-022`/`CAP-024`/`CAP-058`/`CAP-069` EVENT-NOTES by `grep` for the
labels. Nothing in them contradicts the build; every number used was re-derived from the logs (§A.1). One finding from this late read: **the official
switch labels** — on the official screens the Multipoint switch reads "Use multipoint" (`CAP-069-FINDINGS.md` §2) and field 28's switch "Bud return"
(`CAP-058-EVENT-NOTES.md` X7; "Earbuds replaced" is the settings list's wording, `PROTOCOL.md` §4.5.8). The checkpoint option said "Labels = de officiële
labels"; for these two that was imprecise. The maintainer's chosen labels ("Multipoint", "Earbuds replaced") are built; the code comments now say where each
label comes from; the maintainer is told in the closing summary. **Not read:** `CAP-022-FINDINGS.md` §4 and `CAP-024-FINDINGS.md` §4–§5 beyond `grep` (their
frames were re-derived from the logs instead), the bodies of the `0052`/`0056`/`0069` RESULTs.

## Deferred documentation

Each item is also in `TODO.md`, in the same words where it is a task:

- The five switches of 1.1.0 on hardware — read, off, read back, on (BF-1 … BF-12), with the case-sound and Volume-EQ observations; the three request forms
  no capture holds yet: fields 11 and 29 on channel 19 (BF-14, BF-15), `4:{15:1}` on channel 21 (BF-20) — their real bytes then replace the labelled
  structural fixtures (`Settings074`, `SettingsCodecTest`) and the `// TODO(verify)` on `SettingsCodec.FIELD_VOLUME_EQ` (ADR-055) goes. (`TODO.md` §2)
- The screen-reader text on the phone: every "—" with the description "Not read from the Buds yet" (BF-24, two `uiautomator` dumps). (`TODO.md` §2)
- 🟡 **Accessibility, found while testing `ai-sessions/0074`:** a switch row's label and its `Switch` are separate accessibility nodes … **M** decides whether
  and when. (`TODO.md` §5)
- Read only late or in part, against the prompt's reading list (§K): the bodies of the `ai-sessions/0052`, `0056`, `0069` RESULTs, `CAP-022-FINDINGS.md`
  §4 and `CAP-024-FINDINGS.md` §4–§5 — a later session that changes these switches reads them. (This RESULT only — no task in `TODO.md`.)

## Commits

On `feature/0074-settings-switches` (from `fcd863e`), one per concern:

| Hash | Subject | Checked |
|---|---|---|
| `ac9eae5` | feat(app): Multipoint, head gestures, case sounds and Volume EQ switches (ADR-052…055) | full gate on exactly this tree (the four "—"/a11y files reverted to their commit-1 form): exit 0, `:data` 1627, `:ui` 63 |
| `a7775ff` | feat(ui): "—" for every unread switch and its screen-reader text | the final clean gate (§H.1) ran on this code; only versionName/versionCode differed (committed in `7f8750c`) |
| `753cacf` | docs(capture): CAP-070 skeleton (Group BF) for the 1.1.0 release run, registered planned | — |
| `771386a` | docs: ARCHITECTURE, PROJECT, TODO and APP_TESTPLAN follow the 1.1.0 switches | — |
| `7f8750c` | chore(release): prepare 1.1.0 (versionCode 10100), not yet released | final clean gate exit 0 |
| `0323849` | docs(session): ai-sessions/0074 prompt and result, INDEX row | `lint_docs.py` exit 0 on the final tree (back-filled by `ai-sessions/0075`) |

`lint_docs.py` was **not** run on the intermediate doc commits `753cacf`/`771386a`/`7f8750c` (the attempt caused the OOM above); it is exit 0 on the
final tree. The next session back-fills the hash of the session commit (`AI_SESSION_LOG_PROCEDURE.md` §4b).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0074_FEATURE_RESULT_2026_10_06.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0074_FEATURE_RESULT_2026_10_06
