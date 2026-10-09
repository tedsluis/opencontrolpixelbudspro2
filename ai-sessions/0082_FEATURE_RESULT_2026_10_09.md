# 0082_FEATURE_RESULT_2026_10_09.md — Build 1.2.0: "Changed by the Buds" in the ANC details, the component serial numbers, a "probably worn" indicator (ADR-049), two tab moves, and a design study for volume-level notifications (Dosimeter) — with real capture bytes as fixtures, a green gate, the hardware-run skeleton and the release preparation

**Number:** 0082
**Category:** FEATURE
**Date:** 2026-10-09
**Title:** Implement in the OpenControl app, as release 1.2.0: (1) the (i)-details line "Changed by the Buds" when a `Notify` changes the ANC mode
without a tap in the app; (2) the serial numbers per component (Case / Left / Right) from `GetHardwareInfo` field 7 on the Info tab; (3) a worn
indicator built on ADR-049's Settable byte and the runtime-info in-case flags, shown as "probably worn"; (5) the two Case sounds switches moved from
the Controls tab to gear → Settings; (6) the Conversation detection switch moved from Sound to Controls — plus (4) a design study, no code, of
volume-level notifications from `maestro_pw.Dosimeter`. Real capture bytes as fixtures, a green gate with mutation checks, the documents, the
hardware-run skeleton and the release preparation for 1.2.0 (no tag, no publication)
**Status:** complete (the task was resumed once after a context reset; finished 2026-10-09 — commits pending the maintainer's answer, "Commits" below)

---

## Progress

- **Phase 0 — done.** `git log -1`: `ad91f48 Merge pull request #19 from tedsluis/bot/sitemap`. `git status --short` before the branch:
  `?? ai-sessions/0082_FEATURE_PROMPT_2026_10_09.md`. `git fetch && git log --oneline HEAD..origin/main`: empty. Branch `feature/1.2.0` created from
  `main` at `ad91f48`. Next free numbers: capture `CAP-072`, Group **BH** (`id_registry.csv` ends at `CAP-071`; `captures/` ends at `Group_BG`;
  `grep 'Group BH\|CAP-072'` over the registry, the capture index and the test plan → nothing). Highest Test-IDs in use: `ANC-006`, `TOUCH-007`,
  `INEAR-005`, `CASE-009`, `HOLD-005`, `WELL-001`, `FW-004`, `OBS-007`. Highest ADR: ADR-057 (so ADR-058/059/060 are free). Baseline gate green (§A.0).
- **Phase A — done** (§A). No code changed.
- **Phase B — done** (§B, answers verbatim).
- **Phase C (item 1) — built and tested** (targeted: `:data` `BudsRepositoryImplTest` 150, `:ui` `AncScreenTest` 11, `:app` compile — green). Files: `AncModeCause.kt` (new, `:domain`), `BudsRepository.kt`, `BudsRepositoryImpl.kt`, `AncScreen.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`; tests `Cap045Fixtures.kt` (new), `BudsRepositoryImplTest.kt` (5 tests), `AncScreenTest.kt` (4 tests).
- **Phase D (item 2) — built and tested** (targeted: `:data` 494 incl. `HardwareInfoCodecTest` 6 + 5 repository tests, `:ui` `SettingsMenuTest` 14, `:app` compile — green). Files: `DeviceInfo.kt` (`ComponentSerial`, `serials`), `BudsRepository.kt` (`serialsError`), `HardwareInfo.kt` (new reader), `Maestro.kt` (`getHardwareInfoRequest`), `CodecRouter.kt` (`RoutedFrame.HardwareInfo`), `BudsRepositoryImpl.kt` (`readHardwareInfo` after the subscription), `SettingsMenu.kt` (serial lines), `OpenControlNavHost.kt`, `MainActivity.kt`; tests `HardwareInfoFixtures.kt` + `HardwareInfoCodecTest.kt` (new), `BudsRepositoryImplTest.kt`, `SettingsMenuTest.kt`.
- **Phase E (item 3) — built and tested** (targeted: `:domain` 44 incl. `WornReadingTest` 7, `:data` 494 incl. the worn repository test, `:ui` `BatteryCardTest` 9, `:app` compile — green). Files: `WornReading.kt` (new, `:domain`: the sealed class + `wornReading()`), `BudsRepository.kt`/`BudsRepositoryImpl.kt` (`wornReading` = `combine` of four existing flows), `ConnectionScreen.kt` (the line, `wornLine`, `WORN_EXPLANATION`), `OpenControlNavHost.kt`, `MainActivity.kt`; tests `WornReadingTest.kt` (new), `BudsRepositoryImplTest.kt`, `BatteryCardTest.kt`.
- **Phase F (items 5 and 6) — built and tested** (targeted: `:ui` `ControlsScreenTest` + `EqScreenTest` + `SettingsMenuTest` = 43, `:app` compile — green; the write paths are untouched, the `ai-sessions/0076` fixtures stay valid). Files: `SettingsUi.kt` (`SettingsCard` now shared), `ControlsScreen.kt` (Case sounds out, Conversation detection card in), `EqScreen.kt` (row out), `SettingsMenu.kt` (the card + `CASE_SOUNDS_NOTE`), `OpenControlNavHost.kt`; tests the three classes.
- **Phase G — skipped** (item 4 is a 1.3.0 candidate, §B).
- **Phase H — done** (§H: the final clean gate exit 0; M1–M10 all killed; compliance clean).
- **Phase I — done** (§I: `CAP-072`/Group BH skeleton, registry, capture index, `INEAR-006`/`WELL-002`, `APP_TESTPLAN.md` §V).
- **Phase J — done** (§J: the documents; REL 1.2.0/10200; `ensure_footers` + `lint_docs` exit 0).
- **Phase K — this file; the closing question to the maintainer is in the chat.**
- Intermediate results (scratchpad of this session, `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/170c9fdb-4a0f-4dc7-994a-2ab9ab35b5dc/scratchpad`;
  re-create if gone): the gate-baseline log (`gate_baseline`, a scratchpad file); the `tshark`/`pwrpc_decode.py` commands of §A are quoted in full and re-runnable.

---

## A. Phase 0 and Phase A — evidence and design check (no code change)

### A.0 Baseline gate (before any change)

`cd android && ./gradlew --offline --max-workers=2 clean assembleDebug testDebugUnitTest test lint` → `BUILD SUCCESSFUL in 3m 43s`, exit 0. Unit tests
from the JUnit XML (`*/build/test-results/*/*.xml`): `:data` 1627 (debug) / 1627 (release), `:domain` 37, `:hardware` 64 / 64, `:ui` 70 / 70 — 0 failures,
0 errors, 0 skipped. Lint (`lint-results-debug.xml`): `:app` 1 (`OldTargetApi`, pre-existing, `TODO.md` §5), `:ui` 1 (`ModifierParameter`,
`SettingsMenu.kt`, pre-existing), `:data` 0, `:hardware` 0. Kotlin compiler warnings: 0 `w:` lines on a clean build.

### A.1 Item 1 — "Changed by the Buds": evidence and design

**Code path (read in full: `BudsRepositoryImpl.kt`, `BudsRepository.kt`, `AncFrame.kt`, `AncScreen.kt`).** Every `Notify ANC state` reaches
`handleRoutedFrame` (`BudsRepositoryImpl.kt:548–561`): it updates `ancAvailability` (the Settable byte), emits the `AncNotify` the F-1 tap waits for, and —
for a known mode byte — clears `ancModeUnconfirmedAt`, calls `emitAncMode` (mode + `ancModeUpdatedAt = clock()`) and feeds `_ancModeFresh`/`_ancOutcomes`.
The app's own `Set` applies the mode on the ACK (`ancSetOnClaim`, `:812–816`) or accepts the Buds' `Notify` as the outcome (`:817`). Nothing records *why*
the mode changed. The ADR-045/F-3 bookkeeping that exists: `sentAt` of a `Set` (`:799`) and `ancModeUnconfirmedAt` (a cut-off answer); the claim's `Get`
is sent by `sendAncGetAndAwait` (`:823`) for the Connect snapshot (`launchInitialSnapshot`, `:1291`), the Refresh (`refreshAncMode`) and every tap (`setAncModeAfterGet`).

**Frames re-derived (`AGENTS.md` §13 step 1: `tshark -r CAP-045-btsnoop_hci.log -Y "bluetooth.addr == 04:00:6e:cf:6e:07" | wc -l` → 0, exit 0; the
Buds' handle from `-Y "bthci_evt.code==0x03"`: frame 294 `04:00:6e:cf:6e:07` handle `0x0002`).** Command: `tshark -r CAP-045-btsnoop_hci.log -Y
'btrfcomm.dlci==4 and btrfcomm.len>0' -T fields -e frame.number -e frame.time_utc -e frame.p2p_dir -e bthci_acl.chandle -e data.data | grep -E
"\s08(11|12|13)"` (exit 0; every line on handle `0x0002`):

| Frame | UTC | Dir | Bytes | Reading |
|---|---|---|---|---|
| 609 | 06:22:57.420 | phone | `08110000` | the official client's connect-time `Get` |
| 612 | 06:22:57.471 | Buds | `0813000401e8e840` | the answer: Adaptive, Settable `e8` |
| 1583 | 06:23:21.948 | Buds | `0813000401e8e880` | `Notify` only (no `Get`, no `Set` before it): Transparency |
| 1755 | 06:23:32.507 | Buds | `0813000401e8e808` | `Notify` only: Noise cancellation |
| 1818 | 06:23:46.898 | Buds | `0813000401e8e840` | `Notify` only: Adaptive |
| 1849 | 06:23:54.405 | Buds | `0813000401e80020` | `Notify` only: Off, Settable `00` (a bud removed) |

Byte-identical to `CAP-045-FINDINGS.md` §3's table (film: press-and-hold changes, `TOUCH-007`). The whole log holds **no** `08 12` (`grep -c "\s0812"` → 0;
positive control: `CAP-001` has 4). `CAP-021` §4's frames (5237 …) are field-12 `WriteSetting`s, not `Notify`s — the second source named by the prompt for
press-and-hold *cycling* is `CAP-021`'s **film** of holds; the wire there is DLCI 0x02, so `CAP-045` is the fixture source. `CAP-067` §2 (the Transparent →
Active change with no app command between 08:07:34 and 08:12:44, B474 `01 e8 e8 08` after a re-open's `Get`) is a change that this design classes as
**read** (it was learned from a `Get`'s answer on a new claim), not "changed by the Buds" — correctly: the app cannot know when it happened.

**Design (no new wire traffic, no timer constant):**

- `:domain` `enum class AncModeCause { SET_BY_APP, READ, CHANGED_BY_BUDS }`; `BudsRepository.ancModeCause: Flow<AncModeCause?>` — the cause of the current
  `ancMode`, `null` before any report of this connection (reset at Connect with the other per-connection state, before the bonded-device lookup, so a test
  without a `BluetoothDevice` can check it — mutation M2).
- Bookkeeping inside the existing claim code, no new constant: (1) `getPending` is set when the claim's `Get` is sent (`sendAncGetAndAwait`) and cleared by
  the first `Notify` after it or when that wait ends — that `Notify` is **READ**; (2) `setPendingMode` is set when the `Set` is sent (`ancSetOnClaim`, next to
  `sentAt`) and cleared when its wait ends (ACK, NAK, timeout, cut-off) — the ACK is **SET_BY_APP**, and a `Notify` carrying the requested mode while it is
  pending is **SET_BY_APP** too (the Buds answer a `Set` with ACK + `Notify` in either order, `PROTOCOL.md` §4.1); (3) any other `Notify` whose mode
  **differs** from the shown one is **CHANGED_BY_BUDS**; (4) a `Notify` with the same mode and nothing pending leaves the cause as it is (only the time moves).
- UI: one line in `ancDetailLines` (the Noise control card's (i)), only while the cause is CHANGED_BY_BUDS — wording for the checkpoint:
  *"Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."* Nothing on the main surface, no notification.
- Tests: repository — the connect `Get` answered by `CAP-045` 612 → READ; `Set` → ACK → `Notify` same mode → SET_BY_APP (the existing autoAck path); an
  unsolicited `CAP-045` 1583 → CHANGED_BY_BUDS; Connect resets it. `:ui` — the line appears only for CHANGED_BY_BUDS, with the time.

### A.2 Item 2 — serial numbers: evidence

**Is `GetHardwareInfo` sent? No.** `grep -rn "GetHardwareInfo\|METHOD_GET_HARDWARE_INFO" android/*/src/main` → `Maestro.kt:37` (the id, with the comment
"names only, for the debug log's method column — this app never sends them") and `PwRpc.kt:198` (the name table); no call site. On the wire: `for f in
captures/CAP-067-*/…log* CAP-068 CAP-070 CAP-071; do python3 scripts/pwrpc_decode.py "$f" | grep -c GetHardwareInfo; done` → **0 in all 14 logs** of the
OpenControl release runs (exit 0; the same loop counts `SubscribeRuntimeInfo` 2–89 per log; positive control `CAP-036`: 2 lines — the request 1415 and the
answer 1423). So showing serials adds **one unary request per Connect** — ADR-058 (draft §B).

**The official app's request and answer, re-derived.** `CAP-036`: Connection Complete frame 906 `04:00:6e:cf:6e:07` handle `0x0005`;
`tshark -r CAP-036-btsnoop_hci.log -Y 'frame.number==1415 || frame.number==1423' -T fields -e frame.number -e frame.time_utc -e frame.p2p_dir -e
bthci_acl.chandle -e btrfcomm.dlci -e data.data` (exit 0):

- 1415 (04:36:32.628 UTC, phone, handle `0x0005`, DLCI 2): `7e 00 4b 03 10 15 1d ea 71 de 7d 5e 25 e3 a5 ec 28 f9 67 61 b5 7e` — address `00 4b` (channel 21),
  control `03`, `RpcPacket` `10 15` (channel 21) `1d ea 71 de 7e` (`maestro_pw.Maestro`) `25 e3 a5 ec 28` (`GetHardwareInfo` = `h65599` `0x28eca5e3`), no payload,
  CRC `f9 67 61 b5`. `python3 scripts/pwrpc_decode.py CAP-036-btsnoop_hci.log | grep GetHardwareInfo` → `1415 … REQUEST ch=21 … method=GetHardwareInfo`.
- 1423 (04:36:32.711, Buds, 83 ms later; the RFCOMM frame carries three pw_hdlc frames, the first is the answer): `7e 00 a5 03 2a 44 08 06 10 11 28 09 30 07 3a 30
  0a 0e 35 37 30 37 31 57 52 42 45 43 30 32 35 31 12 0e 35 37 30 38 31 57 52 42 44 52 33 32 30 39 1a 0e 35 37 30 37 31 57 52 42 44 4c 33 31 34 37 42 08 30 30 31 33
  32 30 39 33 08 01 10 15 1d ea 71 de 7d 5e 25 e3 a5 ec 28 36 db 71 c0 7e` → decoded `RESPONSE ch=21 … GetHardwareInfo | 1:6 2:17 5:9 6:7 7:{1:"5707…51"
  2:"5708…09" 3:"5707…47"} 8:{…}` — field 7 = a message with three length-14 strings in fields 1, 2, 3 (`0a 0e`, `12 0e`, `1a 0e`). Serials are device
  identifiers and are redacted to 4 + 2 characters here and in every fixture comment (`AGENTS.md` §9).

**Census over every log** (`for f in captures/*/*btsnoop_hci*.log*; do python3 scripts/pwrpc_decode.py "$f" | grep GetHardwareInfo | grep RESPONSE …`): **138
answers in 54 logs** — channel 19: 49, channel 21: 88, channel 24: 1 — **every one with the same three strings in the same order** (`1:5707…51 2:5708…09
3:5707…47`), and 138 requests, one per connection, on the announced channel. The answer is the same whichever bud hosts the session: the request is **not**
per bud, one request yields all three serials.

**Which string is which component — the code (`AGENTS.md` §13 "Reverse engineering", mechanical read, ADR-017).** `gaa.java:45–96` (`gaa.a(qiv)`, the
`GetHardwareInfo` response handler): when `qiv.c == 4` it reads the `qjm` of field 4, else when `== 7` the `qjr` of field 7 — both 3-string messages
(`qjm.java:30` / `qjr.java:30`: `"\u0001ဈ\u0000\u0002ဈ\u0001\u0003ဈ\u0002"` with Java fields `c`, `d`, `e` = proto fields 1, 2, 3) — and copies them **by field
number**: field 1 (`c`) → `gdv.c` (bit 1), field 2 (`d`) → `gdv.d` (bit 2), field 3 (`e`) → `gdv.e` (bit 4) (`gaa.java:60–96`, the `qjr` branch `:97–130`
identical). `fwg.java:182–215` then reads `gdv.e` as the **left** serial ("Missing left serial number"), `gdv.d` as **right**, `gdv.c` as **case**. So the
official app's reading is **by position: field 7.1 = Case, 7.2 = Right bud, 7.3 = Left bud** — 🟢 for the app's code. The wire agrees with the substring
reading: 7.1 ends `…EC0251` (case), 7.2 `…DR3209` (right), 7.3 `…DL3147` (left). This closes, on the code side, `ai-sessions/0073` §4.6's "🟡 that the holder
keeps the wire order" (the holder is filled field by field, not from a list). The `gck.java:288` log line ("serial number %s, sku %s, hardware version …")
prints the `gdv` holder as one Optional; fields 1 (device type 6), 2 (SKU 17), 5/6 (hardware versions) are not in scope (prompt §3).

**Status proposal for the checkpoint:** the prompt's own bar for attributing a serial to a component — "code path + a wire sample on each channel" — is met
(code 🟢; wire: 49 samples on 19, 88 on 21, 1 on 24, same order). `PROTOCOL.md` §6's "Which serial belongs to which component … stays 🟡" would be
promoted to 🟢 FACT ("field 7 lists Case, Right, Left in that order — the official app's own reading, consistent with the strings' EC/DR/DL marks") — only
with the maintainer's word; otherwise the Info tab shows the three strings "as reported".

**Safe Mode and the request.** The prompt's ADR-058 text says "through the ADR-042 Safe-Mode gate for the send". ADR-042 item 3 says reads continue in
Safe Mode ("Reads (ANC Get, battery, EQ ReadSetting, Case) continue"), and the app's other connect-time reads (`ReadSetting`, `SubscribeRuntimeInfo`) are sent
to an unverified firmware. A gated read would be a first; an ungated one follows ADR-042 as written. Both options are at the checkpoint (Q2).

**Where it would live:** `Maestro.getHardwareInfoRequest(channelId)` (next to `subscribeRuntimeInfoRequest`); a `HardwareInfo` reader in `:data` (field 7
only, three strings of printable ASCII ≤ 64 chars, anything else → not read; never throws; fuzzed with `OversizedLengthTest`'s pattern); `RoutedFrame.HardwareInfo`;
`routeMaestro` matches `RESPONSE` for the method with status OK; `BudsRepositoryImpl.launchInitialEqRead` sends it after `subscribeRuntimeInfo`, waits ≤
`SETTING_READ_TIMEOUT_MS` for the answer (the existing 2 s bound, no new constant), no retry; `DeviceInfo` gains `serials: List<ComponentSerial>` + the receive
time; the Info tab shows "Serial numbers (from the Buds, HH:MM:SS):" with one line per string under the firmware lines, "—" until the answer arrived, the
reason when it failed. Logging: the always-on log prints only the pw_rpc summary (method, status — `RpcPacket.summary()` has no payload); a serial never
appears in a log line (the raw hex dump is Debug mode only, `AGENTS.md` §9); a unit test asserts the log lines of the read contain no serial (M4).

**Fixtures:** `CAP-036` 1415/1423 (channel 21); the channel-19 request is the same packet on `00 3b`/channel 19 — a real channel-19 request exists
(49 of them, e.g. the first log of the census with `ch=19`), to be quoted by frame in Phase D. No OpenControl capture holds the exchange (the app never sent
it); the run skeleton records the first one.

### A.3 Item 3 — the worn indicator: evidence and design

**Frames re-derived.** `CAP-065` (handle `0x000b` for `04:00:6e:cf:6e:07`, Connection Complete 600 … 9852): `tshark -r CAP-065-btsnoop_hci.log -Y
'bthci_acl.chandle==0x000b && (btrfcomm.dlci==4 || btrfcomm.dlci==5) && (frame.number==5465 || …6334)' -T fields -e frame.number -e frame.time_utc -e
frame.p2p_dir -e btrfcomm.dlci -e data.data` (exit 0): 5465 09:14:33.437 `0813000401e80020` (BA-5 both loose on the table), 5707 09:15:00.485 `…01e80020`
(BA-6 Left docked, Right loose), 6019 09:15:53.518 `…01e80020` (BA-7 lid closed, Right outside), 6334 09:16:41.787 `0813000401e8e820` (BA-8 both worn) —
as ADR-049's 2026-10-01 Update. `CAP-064` (handle `0x000b`): 2299 08:06:15.005 `0813000401e8e820`, 2542 `…e8e820`, 2699 `…e8e840`, 2759 `…e8e840` (the ≈ 28 s
of `e8` with both buds on the table, in-ear detection on); 10394 08:24:52.250 and 10600 08:25:16.548 `0813000401e8e820` on DLCI 5 (in-ear detection off,
no bud worn); 4103 `…e8e820` / 4533 `…e8e840` (one bud worn); 5591 `0813000401e80020` (one docked, one loose) — all as `CAP-064-FINDINGS.md` §3.
**Field 2:** `python3 scripts/pwrpc_decode.py CAP-064-btsnoop_hci.log | grep -E "ReadSetting.*4:2$|4:\{2:"` — every connect read answers `4:{2:1}` (1260, 2290,
3162, …); the writes `WriteSetting 4:{2:0}` 9922 and `4:{2:1}` 10817 (ADR-047's re-test) bracket the in-ear-off samples above. **Runtime info (`CAP-062`):**
2782 `3:0 6:{2:{1:100 2:1} 3:{1:100 2:1}} 7:{1:0 2:0 3:0}` (no 6.1 — no bud charging, both out), 3760 `6:{1:{1:60 2:1} 2:{1:100 2:1} 3:{1:100 2:2}} 7:{1:1 2:0 3:0}`
(Right charging), 4845 `… 2:{1:100 2:2} 3:{1:100 2:2}} 7:{1:1 2:1 3:0}` (both), 7033 `… 2:{1:100 2:2} 3:{1:100 2:1}} 7:{1:0 2:1 3:0}` (Left charging) — as
`CAP-062-FINDINGS.md` §4; the app already decodes these into `leftCharging`/`rightCharging` (`RuntimeInfoDecoder`, 6.x field 2 first, 7.x as fallback).

**What the app already holds (no new wire traffic, no new decoder):** `ancAvailability` + `ancAvailabilityUpdatedAt` (the last `Notify`'s Settable byte and
its time; reset to UNKNOWN at Connect), `settings.inEarDetection` (field 2, read at Connect), `batteryStatus.leftCharging` / `rightCharging`
(`ChargingReading` with `fromEarlierSession`). Every input of the indicator exists; the derived reading is a pure function.

**Design.** `:domain` `WornReading` (sealed): `NotRead` ("—", no `Notify` this connection), `InEarDetectionOff`, `InEarDetectionNotRead`, `BothInCase`,
`NotWorn(checkedAtMillis)`, `ProbablyWorn(checkedAtMillis)`; `fun wornReading(availability, checkedAt, inEarDetection: SettingReading<Boolean>?, left:
ChargingReading?, right: ChargingReading?)` with this precedence: no `Notify` → NotRead; field 2 = 0 → InEarDetectionOff (the byte says nothing then,
`CAP-064` 10394/10600); field 2 not read → InEarDetectionNotRead; both charging flags `true` **from this session** → BothInCase (a charging bud is not worn,
🟢 correlation); Settable `00` → NotWorn; non-zero → ProbablyWorn. `BudsRepository.wornReading: Flow<WornReading>` = `combine` of the four existing flows.
UI: one line on the Connection screen's **battery card** (proposal) plus the per-bud "in the case" state and the two limits in the card's (i); texts at the
checkpoint (Q3). Never names a bud as worn (the byte is not per bud). Tests: `:domain` one per reading with the fixtures above; repository: the flow follows
`CAP-065` 5465/6334 and `CAP-064` 10394 + the field-2 read; `:ui`: the texts.

### A.4 Item 4 — volume-level notifications: reverse engineering (read-only, `v1.0.955078536-10253511`, JADX unless noted)

(a) **What `HearingWellnessNotificationWorker` computes, from which RPC, on which schedule, against which threshold.**
- The class (`…/notification/hearingwellness/HearingWellnessNotificationWorker.java:27–66`) is an `RxWorker`. `m(context, address)` (`:46–55`) builds a
  one-time request (`emt` = `OneTimeWorkRequest.Builder`; `enh.g(Duration)` sets `this.c.g = duration.toMillis()` = the initial delay, `enh.java:72–78`) with
  tag `HEARING_WELLNESS_NOTIFICATION_WORK`, initial delay `j = Duration.ofMinutes(1)` (`:30`), input `DEVICE_ADDRESS`, and enqueues it as unique work
  `HEARING_WELLNESS_NOTIFICATION_WORK_<address>` with policy int `2` (`eng.h(String, int, …)`, `eng.java:24`; the enum name behind `2` is not recoverable
  from the stripped code — ⚪). `l(context)` (`:41–44`) cancels all work with that tag.
- **Scheduled** from the session: `fvp.i()` (`fvp.java:58–64`, run when the Dosimeter connection starts — `fut.java:102–103` per `ai-sessions/0073`)
  subscribes `this.l.C(new fst(14))` → `fsy(this, 7)`: `fst` case 14 = `gcl.x()` (`fst.java`), and `fsy` case 7 (`fsy.java`): `true` → log "HearingWellnessNotificationWorker:
  scheduled" + `m(context, address)`; `false` → "… cancelled" + `l(context)`. `gcl.x()` (`gcl.java:550–563`) returns the device record's flag `gdw.x` when its
  presence bit `2097152` is set, else `true` — the stored value of the **"Volume level notifications" switch** (field 21 is written by `hey.java:165–190`
  from the same preference, `REVERSE_ENGINEERING.md` `qhr` register row 21). Also scheduled by `fyh.a()` (`fyh.java:32–36`) right after it fetched both
  daily summaries (below), and cancelled in `fvp.b()` (`:51–56`, the session's end). **No periodic work:** one-time, 1 minute after each session start, replaced/
  appended per policy `2` — not a `PeriodicWorkRequest`, not "only while connected" beyond that (WorkManager runs it when its delay elapses).
- **What it computes** (`c()`, `:58–65`): the device record `gck.b(address)` → `ftq(13)` (`Optional.isPresent`) → `gau(11)` (`get`) → `gau(12)`
  (`gau.java:104–131`): returns `false` when `!gcl.x()` (switch off) or when both summaries are absent; else `hwy.aj(left = gcl.q(), right = gcl.r())` →
  `gau(13)` = `hwy.ai(gdg)` → `gag(lastNotified, 10)` → `fsm(this, address, 5)`.
  - `gcl.q()`/`r()` (`gcl.java:430–474`): the stored **left** (`gdf.c`) and **right** (`gdf.e`) daily summaries (`gdg`).
  - `hwy.aj(gdg, gdg)` (`hwy.java`): for the day index `max(left.c, right.c)` and the 7 before it, the per-day dose is `(left.d[day] + right.d[day]) / 2`
    (one present → that one, else 0) — **left and right averaged per day**.
  - `hwy.ai(gdg)` (`hwy.java:1979–1998`): sums the doses of the current day index and the 6 before it (7 days) and returns
    `round(sum / 3.3903457E11f × 100)` — a percentage of a **7-day** constant.
  - `gag` discriminator 10 (`apktool-output/smali_classes2/gag.smali:438–468`, read in full): `if-lt p1, 0x43` → `false` when the percentage is **below 67**;
    otherwise `true` when no last-notified time is stored, or when `Instant.now().minusMillis(last).toEpochMilli() >= 0x240c8400` (604 800 000 ms = 7 days).
  - `fsm` case 5 (`fsm.java:171–215`): on `true` creates the channel `"HW APPROACHING MAX NOTIFICATION"` (name `R.string.maestro_notification_title`, importance 3),
    posts a notification with title `maestro_notification_title` and body `hearing_wellness_approaching_max_notification_body`, tap → the app's hearing-wellness
    fragment, logs app event type 16, and stores `now` as the last-notified time (`gck.P(address, new gcg(Long, 13))`).
  - **Threshold constants in the code:** 7-day limit `3.3903457E11` (`hwy.java`), 24-hour limit `4.843351E10` (`his.java:28`: exposure % = `g / 4.843351E10 × 100`
    from `qhz` field 5), ratio exactly 7.0; notify at **≥ 67 %** of the 7-day limit, at most once per **7 days**; the live level is "loud" at **≥ 85 dB**
    (`his.java:36`, `max >= 85`). The code names no WHO/EU figure; the unit of the dose is not in the code (the live value is linear, shown as
    `round(10·log10(v))` dB, `hiw.java:32–37`).
(b) **`qhz` / `qia` / `qir` in the code.** `fyf.java:49–94` calls `maestro_pw.Dosimeter/FetchDailySummaries` per bud (`goq.LEFT_BT_CORE` and `RIGHT_BT_CORE`,
  i.e. channels 19 and 21, `fyh.java:28–36`, 3 retries) and copies `qhz` into `gdg`: field 1 (`b`) → `gdg.c` = the **current day index**; field 2 (`c`, repeated
  `qia`) → `gdg.d`, each `qia` → `gda` {1 = day index, 6 = that day's dose (`gda.d`, float)}; field 3 (`d`) → `gdg.e` (summed in `hwy.aj`, unnamed); field 4
  (`e`) → `gdg.f` (unnamed); field 5 (`f`) → `gdg.g` = the **24-hour dose** (exposure %). `qir` field 2 = the live linear level per bud (`hiw.java`); the Hearing
  wellness page shows `round(10·log10(v))` dB clamped 0–120 with "Loud" ≥ 85. Wire sample (`CAP-058` 2845, channel 21): `1:462 2:{1:228 6:…} 2:{1:456 …} …
  2:{1:462 6:f32(204890640)} 5:f32(245338960)` — 7 recent day entries 456–462 and one old (228); 24-h exposure = 245 338 960 / 4.843351E10 = **0.5 %**.
(c) **Does field 21 change anything on the Buds?** Code: it only gates the phone-side worker (`gcl.x()` → `fsy` case 7); the Buds' own `qhr` field 21 is
  written and read back (`CAP-058` X12) but no code reads a Buds-initiated message for it. Wire: `python3 scripts/pwrpc_decode.py <log> | grep
  call_id=4294967295 | awk '{print $9}' | sort | uniq -c` → `CAP-009` (101 min idle): 9 × `method=GetSoftwareInfo`, nothing else; `CAP-042` (37 min idle): 1 ×
  `GetSoftwareInfo` (exit 0; the same filter finds the unsolicited announcement in every log — the positive control). No Buds-initiated Dosimeter or other
  packet exists in either idle log.
(d) **Unsolicited Dosimeter values:** `grep -c "Dosimeter.*call_id=4294967295"` over all 85 logs → **0 in every log** (positive control: `call_id=4294967295`
  matches `GetSoftwareInfo` in `CAP-036`). Every `SubscribeToLiveDb` `SERVER_STREAM` follows a `REQUEST` on its channel (`CAP-009`: requests 1505/1506,
  first stream packets 9272/9273; re-requested at 14809, 15025, 18207, 18445, 19560, 19793, 20029, 26788 — each after a `FetchDailySummaries` pair);
  `CAP-042`: `FetchDailySummaries` 769/771, two `CLIENT_ERROR FAILED_PRECONDITION` 888/893, then `SubscribeToLiveDb` 967/968 and 9 packets per channel.
  The Buds never push a Dosimeter value without a subscription.
(e) **The notification's text and channel:** channel id `"HW APPROACHING MAX NOTIFICATION"`, channel name and title = `maestro_notification_title` = "Pixel Buds"
  (`strings.xml:494`), body `hearing_wellness_approaching_max_notification_body` = "Consider lowering the volume of your earbuds to limit your audio exposure"
  (`:301`), notification id `hearing_wellness_approaching_max_notification_id` (`:302`). The switch's own summary: `summary_hearing_wellness_notifications`
  = "Pixel Buds will notify you if the recommended exposure limit is exceeded" (`:799`) — the UI says "exceeded", the code notifies at 67 %.

**Design (RESULT only; nothing built):** see §B's draft ADR-060 and the list of 🟡 that block a build.

### A.5 Items 5 and 6 — what moves

`ControlsScreen.kt:103–123` holds the "Case sounds" card (`SettingsCard`, private to that file) with `CASE_SOUNDS_TITLE`, the two labels and the (i) lines
"<Label>: read/changed HH:MM:SS"; `SettingsMenu.kt:104/125–153` renders the Settings tab (`DarkModeSettings`: dark mode + "Use different Buds") with no Buds
state at all — `SettingsMenuScreen` gets no `connectionState`, `settings`, `settingsError` or setting callbacks today (`OpenControlNavHost.kt:329–341`).
`EqScreen.kt:144–171` holds the "Balance and audio" card with the balance, "Mono audio" and "Conversation detection" (subtitle "Switch from noise
cancellation to transparency when you talk"); `soundSettingsDetailLines` (`:191–195`) lists the three. The write paths (`setCaseSound…`,
`setConversationDetection`) and `MainActivity`'s wiring (`:514`, `:523–524`) stay as they are; only the screens that call the callbacks change.
Plan: `SettingsCard` moves to `SettingsUi.kt` (internal) so both tabs use it; `SettingsMenuScreen` gains `connectionState`, `settings`, `settingsError` and the
two case-sound callbacks (defaults keep the existing tests compiling); the Controls tab gets a "Conversation detection" card (Q5 decides where); `EqScreen`'s
card keeps Balance and Mono audio and `soundSettingsDetailLines` loses its third line. `APP_TESTPLAN.md`: M5 → N (new N12), T5/T6 → the new 1.2.0 section
with the Settings tab; `ARCHITECTURE.md` §2.4's tree and the two sentences naming the tabs' contents follow. Mock-ups: §B Q5.

### A.6 Run-skeleton input (`TODO.md` §2 against this run)

| `TODO.md` §2 item | Fits | Expected |
|---|---|---|
| T11 screen-reader text (two `uiautomator` dumps, Bluetooth off) | yes | `content-desc="Not read from the Buds yet"` on every "—" (Controls, Sound; now also the Settings tab's two switches) |
| C12 — a tap during an automatic re-open | yes (screen recording) | "The setting was not changed: The app's channel is being reopened …", no `WriteSetting` |
| H5 *Read EQ again* | yes | `ReadSetting 4:16` → the same quintet |
| S6 "—" in the first second (screen recording) | yes | "—" then values |
| S12 export across a rotation | yes | "Debug log saved (N lines)." |
| L-1 with the head in view (`INEAR-005`) | yes — doubles as the worn-indicator steps | Left out with both worn on 19 → Buds `DISC` + announcement 21; the indicator reads "Probably worn" before and after |
| Case-sound / Volume-EQ observations (audio) | yes | said aloud on the film |
| B4 double tap (`AlreadyInProgress`) | **no** | needs the Buds forgotten and re-paired |
| CDM picker on film (`CAP-058` §9) | **no** (same reason) | — |
| K5 / BC-12, F-4 debug StrictMode, a second device | **no** | Owner user only / debug build / one phone |
| P1 `dumpsys package … firstInstallTime/lastUpdateTime` | yes | an update over 1.1.1: `lastUpdateTime` later than `firstInstallTime` |

### A.7 Files read (for §K)

In full: the prompt; `AGENTS.md` (system context), `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md` (ADR-001 … ADR-057 with every Update), `TODO.md`,
`AI_SESSION_LOG_PROCEDURE.md`, `ARCHITECTURE.md`, `ai-sessions/0074` RESULT, `ai-sessions/0076` RESULT; Kotlin: `Maestro.kt`, `DeviceInfo.kt`, `BatteryStatus.kt`,
`BudsRepository.kt`, `BudsRepositoryImpl.kt`, `AncFrame.kt`, `PwRpc.kt`, `SoftwareInfo.kt`, `SettingFrame.kt`, `BudsSettings.kt`, `AncScreen.kt`,
`ControlsScreen.kt`, `EqScreen.kt`, `SettingsMenu.kt`, `SettingsUi.kt`, `Details.kt`, `OpenControlNavHost.kt`, `MainActivity.kt`, `ConnectionScreen.kt`,
`CodecRouter.kt`, `RuntimeInfo.kt`, `Varint.kt`, `BudsForegroundService.kt`; tests `ControlsScreenTest.kt`, `EqScreenTest.kt`, `SettingsMenuTest.kt`,
`AncScreenTest.kt`. In part: `PROTOCOL.md` (§2.2a, §4.1, §4.3 Option F, §4.5 preamble, §4.5.8, §4.5.8a in full; §6 the `GetHardwareInfo`/serial/Dosimeter/
field-21 entries by `grep`), `REVERSE_ENGINEERING.md` (the `qhr` register row 21 and its Update, the `maestro_pw.*` catalog rows, the decoded-shapes register,
the `HearingWellnessNotificationWorker` trace, the `qiv` device-type trace), `ai-sessions/0073` §4.6–§4.7, `ai-sessions/0081` §C.5 and §D, `ai-sessions/INDEX.md`
(tail), `RELEASING.md` (§4), `APP_TESTPLAN.md` (sections M, N, T, U, the end), `CHANGELOG.md` (head), `README.md` (by `grep`), `id_registry.csv` (by `grep`),
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the rows used), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BG, the `CAP-071` row), `CAP-071-EVENT-NOTES.md` (head),
`CAP-045-FINDINGS.md` §3, `CAP-021-FINDINGS.md` §4, `CAP-067-FINDINGS.md` §2, `CAP-069-FINDINGS.md` §12, `CAP-058-FINDINGS.md` §6, `CAP-064-FINDINGS.md` §3,
`CAP-062-FINDINGS.md` §4; `BudsRepositoryImplTest.kt` (lines 20–260, the helpers; the test names by `grep`), `FakeBudsTransport.kt` (signatures),
`SettingsFixtures.kt` (object names), `Hdlc.kt` (signatures); JADX: `gaa.java:45–130`, `fwg.java:178–218`, `qiv.java`, `qjm.java`/`qjr.java`/`gdv.java` (schema
lines), `fux.java:85–105`, `HearingWellnessNotificationWorker.java` (full), `fvp.java` (full), `gau.java:104–131`, `fsm.java:171–220`, `fsy.java` (cases 6–7),
`ftq`/`fst` (the cases named), `gcl.java` (`q`, `r`, `x`), `hwy.java` (`ai`, `aj`), `his.java:20–40`, `hiw.java:25–45`, `fyf.java:49–94`, `fyh.java:25–45`,
`enh.java`/`eng.java` (the methods named), `gag.smali:438–468`, `strings.xml` (the hearing-wellness strings). Not read: `CAP-065`/`CAP-066` §3 bodies beyond the
frames re-derived, `CAP-071-FINDINGS.md`, the bodies of `ai-sessions/0052`/`0056`/`0069`.

## B. Phase B — checkpoint (answers verbatim, chat 2026-10-09)

Two rounds of `AskUserQuestion` in this chat (Dutch; the question's header and the chosen option label as shown; the preview of each chosen option is the
draft the maintainer approved — ADR-058 and ADR-059 as quoted in §A/§D/§E, ADR-060 as a draft only).

| Question (header) | Answer |
|---|---|
| "Item 1 tekst" — the (i) line and the cause bookkeeping of §A.1 | *"Zoals voorgesteld (Recommended)"* — line *"Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."*, only while the cause is CHANGED_BY_BUDS; the rule: Get → first `Notify` = READ; Set → ACK = SET_BY_APP, `Notify` with the requested mode while the Set is open = SET_BY_APP; another `Notify` with a mode other than the shown one = CHANGED_BY_BUDS; same mode, nothing open = unchanged; Connect → `null`. Fixtures `CAP-045` 612/1583, `CAP-036` 1182. |
| "Item 2 ADR-058" — the finding (not sent), the request, the attribution, the gate | *"ADR-058, ongegate read, labels Case/Right/Left + 🟢 (Recommended)"* — **ADR-058 accepted** as drafted in the preview (one `GetHardwareInfo` per Connect after `SubscribeRuntimeInfo`, byte-identical to `CAP-036` 1415 / its channel-19 form, no retry, ≤ 2 s; a read, not gated by Safe Mode; field 7 only; Info tab lines labelled Case / Right bud / Left bud by position; serials never logged, redacted 4 + 2), and **`PROTOCOL.md` §6's 🟡 "which serial belongs to which component" → 🟢 FACT** (the official app's own reading, `gaa.java:45–96` + `fwg.java:182–215`; wire-consistent on channels 19, 21, 24). |
| "Item 3 ADR-059" — the indicator, its place, its texts | *"Batterijkaart + teksten zoals voorgesteld (Recommended)"* — **ADR-059 accepted** as drafted: the pure function and its six readings in the stated order, the texts "—", "Worn: unknown — in-ear detection is off", "Worn: unknown — the in-ear detection setting was not read", "Both buds in the case", "Not worn (checked HH:MM:SS)", "Probably worn (checked HH:MM:SS)"; one line on the battery card; the (i) explanation with the two limits; never a bud named; ADR-049 item 3 stays 🟡; verification `INEAR-006` in `CAP-072`. |
| "Item 4 Dosimeter" — what to do with the design study | *"1.3.0-kandidaat, ADR-060 als concept (Recommended)"* — **nothing built**; ADR-060 stays a draft in this RESULT (§C.4) and `TODO.md`; `REVERSE_ENGINEERING.md` gets the findings; Test-ID proposal `WELL-002`. |
| "Settings-tab" — item 5's order | *"Dark mode · Case sounds · Use different Buds (Recommended)"* — the card between the dark-mode group and "Use different Buds", with the (i) lines "Earbuds replaced: read …", "Other alerts: changed …", "These settings live on the case and are read when the app connects." |
| "Controls-tab" — item 6's place | *"Eigen kaart na In-ear detection, ondertitel behouden (Recommended)"* — an own "Conversation detection" card after "In-ear detection" and before "Multipoint", the subtitle "Switch from noise cancellation to transparency when you talk" kept; the (i) "Conversation detection: read HH:MM:SS". |
| "Release" — version and REL | *"1.2.0 / 10200, voorbereiden (Recommended)"* — `versionCode = 10200`, `versionName = "1.2.0"`; the `[1.2.0] - not yet released` block, README, `scripts/release_notes.template`; no tag, no `release.sh`. Phase A found no correction to an ADR or a 🟢 text; the closed 🟡 of `ai-sessions/0073` §4.6 becomes a dated note in `REVERSE_ENGINEERING.md`. |

**What the answers unblock:** ADR-058 and ADR-059 are written into `DECISIONS.md` as Accepted (maintainer, chat 2026-10-09, with the texts shown in the
previews); `PROTOCOL.md` §6's serial-attribution item is promoted (dated, `ai-sessions/0082`); ADR-060 is **not** written into `DECISIONS.md`. Nothing else on the
wire, no `PROTOCOL.md` status change beyond that one line. One detail inside the approved rule, decided while building: a `Notify` that arrives while **no mode
is shown yet** (the app never had a mode this run) is READ, not "changed" — nothing was shown that could have changed.


## Summary in plain language (read this first)

**What the app user sees differently in 1.2.0** (every new text is in the code's string constants, quoted here verbatim):

1. **Noise control (i):** when the Buds change the mode on their own — a press-and-hold on a bud, or their own change — the (i) adds *"Changed by the
   Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."* A mode the app set, or read at Connect or on a pull, has no such line. Nothing
   on the main surface, no notification, nothing sent.
2. **Gear → Info:** under the firmware, *"Serial numbers (from the Buds, HH:MM:SS):"* then *"Case: …"*, *"Right bud: …"*, *"Left bud: …"* and the note
   *"Labelled by position as the official app labels them: the Buds list the Case, the Right bud, then the Left bud."* — one `GetHardwareInfo` per
   connection, sent last of the Connect-time requests, never retried; *"Serial numbers: not read — <reason>"* when the Buds do not answer. The serials
   are never written to the app's log.
3. **Connection → battery card:** a **Worn** line under the three battery columns — *"Probably worn (checked HH:MM:SS)"*, *"Not worn (checked …)"*,
   *"Both buds in the case"*, *"Worn: unknown — in-ear detection is off"*, *"Worn: unknown — the in-ear detection setting was not read"*, or "—" before the
   first `Notify`; the (i) explains the two limits in plain words (the Buds say "at least one bud", never which; `CAP-064`'s ≈ 28 s). Nothing new is sent.
4. **Gear → Settings** now holds Dark mode · **Case sounds** (Earbuds replaced, Other alerts; the (i) ends with *"These settings live on the case and are
   read when the app connects."*) · Use different Buds. The two switches left Controls.
5. **Controls** has an own **Conversation detection** card after In-ear detection (subtitle kept); Sound keeps Balance, Mono audio and Volume EQ.
6. **Nothing** for volume-level notifications — studied only (§C.4): a 1.3.0 candidate behind a draft ADR-060 and a capture (`WELL-002`).

**What the maintainer has to do next, step by step:**

1. Answer the question at the end of this session (commit, push, open a pull request from `feature/1.2.0` — or not).
2. Decide whether `CAP-072` is one sitting (≈ 45 min with film 2) or two (`CAP-071` had no film 2) — §I asks this.
3. `RELEASING.md` §5–§6 for 1.2.0 when the branch is merged — the commands are printed in §J; this session ran none of them.
4. Film `CAP-072` from its skeleton (P0–P9, BH-1…BH-26); a CAPTURE session analyses it and gives the release verdict for 1.2.0.
5. Later, at leisure: accept, change or drop the draft ADR-060 (it is a **Proposed** entry — see "Deviation" below).

**One deviation from the prompt, flagged:** the prompt (task 21, §5 "Approvals") says an unaccepted ADR lives in the RESULT and `TODO.md`, **not** in
`DECISIONS.md`. `scripts/lint_docs.py` fails on any `ADR-NNN` cited anywhere without a registry row and a `## ADR-NNN` heading in `DECISIONS.md`, and
ADR-060 is cited by the prompt, this RESULT, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`WELL-002`) and `TODO.md`. The way out that keeps the gate green and the
rule honest: `DECISIONS.md` carries ADR-060 as a heading whose status line says **Proposed — a draft awaiting maintainer sign-off; not accepted, nothing
built**, registered `proposed` in `id_registry.csv`. No agent accepted anything; the maintainer may delete the entry (and the citations) or accept it
later. If the maintainer prefers the entry gone now, say so and the citations are rewritten without the number.

---

## C. Phase C — Item 1: "Changed by the Buds" (built)

**Rule, as approved (§B):** the cause of each shown ANC mode is one of READ, SET_BY_APP, CHANGED_BY_BUDS (`AncModeCause.kt`, `:domain`). The app's own
`Get` (`08 11`) marks the next `Notify` as READ; the app's own `Set` marks the `Notify` of the requested mode and the ACK as SET_BY_APP; a `Notify` the app
did not provoke whose mode **differs** from the shown one is CHANGED_BY_BUDS; the same mode again changes nothing; a `Notify` while **no** mode is shown
yet is READ. Connect resets the cause to `null`. The flow `ancModeCause` goes `BudsRepositoryImpl` → `OpenControlUiState` → `AncScreen`, which adds
`ancChangedByBudsLine(at)` to the (i) only while the cause is CHANGED_BY_BUDS (`AncScreen.kt:192–193`).

**Fixtures (`Cap045Fixtures.kt`, `CAP-045-btsnoop_hci.log`, the official app's session; re-derived with `tshark -r … -Y 'btrfcomm.dlci==8 && frame.number
in {609 612 1583 1755 1818 1849}' -T fields -e frame.number -e frame.p2p_dir -e data.data`, handle scoped as in §A.1):**

| Frame | Direction | Bytes | Meaning |
|---|---|---|---|
| 609 | phone → Buds | `08 11 00 00` | the app's `Get` |
| 612 | Buds → phone | `08 13 00 04 01 e8 e8 40` | the answer: Adaptive, Settable `e8` |
| 1583 | Buds → phone | `08 13 00 04 01 e8 e8 80` | unprovoked: Transparent (a press-and-hold on film, `CAP-045-FINDINGS.md` §3) |
| 1755 | Buds → phone | `08 13 00 04 01 e8 e8 08` | unprovoked: Active |
| 1818 | Buds → phone | `08 13 00 04 01 e8 e8 40` | unprovoked: Adaptive |
| 1849 | Buds → phone | `08 13 00 04 01 e8 00 20` | Off, Settable `00` (used by item 3) |

**Tests:** `BudsRepositoryImplTest` +5 (the five "Item 1: …" names in §H's list — Get's answer = reading; unsolicited other mode = changed; own ACKed Set =
set by app; Notify of the requested mode while the Set waits = set by app; Connect resets), `AncScreenTest` +4 (the line with the mode's time; no line
after a read; no line after a set; no time / no mode).

## D. Phase D — Item 2: serial numbers (built, ADR-058 accepted)

**What was built:** `Maestro.getHardwareInfoRequest(channelId)` (the empty-payload unary call, method `0x28eca5e3`, the announced channel's ADR-034
address); `HardwareInfo.serials(payload)` (`:data` codec, reads field 7's fields 1..3 only — printable ASCII 0x21..0x7E, 1–64 chars, no repeated index,
sorted; never throws); `CodecRouter` routes an OK `RESPONSE` to that method with serials as `RoutedFrame.HardwareInfo(serials)`, an OK answer without
field 7 as a plain `RpcResult` (→ `UnreadableAnswer`); `BudsRepositoryImpl.readHardwareInfo()` runs under `eqMutex` after `subscribeRuntimeInfo` in
`launchInitialEqRead` (order: EQ read → twelve settings reads → `SubscribeRuntimeInfo` → `GetHardwareInfo`), `SETTING_READ_TIMEOUT_MS`, errors
`Timeout` / `UnreadableAnswer` / `MaestroRejected` into `serialsError`, no retry, **not gated by Safe Mode** (ADR-058 item 2 / ADR-042 item 3: a read);
`DeviceInfo.serials: List<ComponentSerial>` + `serialsReadAtMillis` (`MaestroHello` keeps them via `_deviceInfo.update`); the Info tab's lines
(`SettingsMenu.kt:249–272`); log line at INFO: `"Hardware info read (channel N): K serial numbers"` — the count, never a string.

**Fixtures (`HardwareInfoFixtures.kt`; commands quoted in the file header and §A.2):**

| Constant | Source | Bytes |
|---|---|---|
| `REQUEST_CH21_CAP036_1415` | `CAP-036` 1415, phone → Buds, ch 21 | `7e004b0310151dea71de7d5e25e3a5ec28f96761b57e` |
| `REQUEST_CH19_CAP024_801` | `CAP-024` 801, phone → Buds, ch 19 | `7e003b0310131dea71de7d5e25e3a5ec28ff1ebbb77e` |
| `RESPONSE_CH21_CAP036_1423_REDACTED` | `CAP-036` 1423, Buds → phone | the real frame with the three 14-char strings replaced by `5707XXXXXXXX51`, `5708XXXXXXXX09`, `5707XXXXXXXX47` and the CRC re-sealed (real CRC `36 db 71 c0`, verified) |
| `RESPONSE_CH19_CAP024_832_REDACTED` | `CAP-024` 832, Buds → phone | the same strings, field 8 `…94`; real CRC `a4 a6 c5 17`, verified |

Redaction = first 4 + last 2 characters kept, as the prompt asks; the same form is used in this RESULT and the run skeleton.

**Tests:** `HardwareInfoCodecTest` 6 (request on both channels; field 7 by position; the channel-19 answer; the router; labelled structural cases; fuzz),
`BudsRepositoryImplTest` +5 (the "ADR-058: …" names), `SettingsMenuTest` +3 (the lines; not read / failed / nothing without an announcement; the
failed-read line). Existing tests adapted: request-count assertions `3 + settingReads.size` with `GetHardwareInfo` last; `answerReadsLikeCap036` answers it.

## E. Phase E — Item 3: the worn indicator (built, ADR-059 accepted)

**What was built:** `WornReading.kt` (`:domain`): the sealed class (NotRead, InEarDetectionOff, InEarDetectionNotRead, BothInCase, NotWorn(checkedAt),
ProbablyWorn(checkedAt)) and the pure function `wornReading(availability, checkedAtMillis, inEarDetection, leftCharging, rightCharging)` in ADR-059's
order: no `Notify` → NotRead; field 2 not read → InEarDetectionNotRead; field 2 = 0 → InEarDetectionOff; both charging **this connection** → BothInCase;
Settable `00` → NotWorn; else ProbablyWorn. `BudsRepositoryImpl.wornReading` = `combine` of `_ancAvailability`, `_ancAvailabilityUpdatedAt`, `_settings`,
`_batteryStatus` — **no new wire traffic**. `ConnectionScreen.kt`: the line under the battery columns (`wornLine()`, the four constants, `WORN_EXPLANATION`
with the approved text incl. "(CAP-064)"), the (i) lines. `_ancAvailability` and `_ancModeCause` are now reset **before** the bonded lookup in
`openSession` (a failed connect also clears them — the bug the first full gate caught, §H).

**Fixtures (named in the tests, re-derived in §A.3):** `CAP-065` 5465 (`00`, both buds loose on the table, in-ear on) and 6334 (`e8`, both worn); `CAP-064`
10394 (`e8`, in-ear detection off, both on the table); `CAP-062` 3760 (Right charging only) and 4845 (both charging); `CAP-045` 1849 (`00`, Off).

**Tests:** `WornReadingTest` 7, `BudsRepositoryImplTest` +1 ("ADR-059: not read until a Notify; … Connect resets"), `BatteryCardTest` +3 (the dash with the
screen-reader text; the times on the card and in the (i); the literal texts).

## F. Phase F — Items 5 and 6: the two moves (built)

`SettingsCard` moved from `ControlsScreen.kt` to `SettingsUi.kt` (internal, shared). `SettingsMenu.kt`: `SettingsTabContent` = Dark mode · Case sounds card
(`CASE_SOUNDS_TITLE`, the two labels, `CASE_SOUNDS_NOTE`) · Use different Buds; `SettingsMenuScreen` gains `connectionState`, `settings`, `settingsError`
and the two case-sound callbacks (defaults keep the old call sites compiling). `ControlsScreen.kt`: the Case sounds card removed; a "Conversation
detection" card after In-ear detection (`CONVERSATION_DETECTION_LABEL`/`_SUBTITLE`, `onConversationDetectionChanged`). `EqScreen.kt`: the row removed,
`soundSettingsDetailLines` = Balance, Mono audio. `OpenControlNavHost.kt` wires both. The write paths (`setCaseSound…`, `setConversationDetection`) and
`MainActivity`'s wiring are untouched — the `ai-sessions/0076` fixtures (`CAP-019` 1720/1808, `CAP-070` A770/A777/A1992/A1995) still verify the bytes.

**Tests:** `SettingsMenuTest` +3 (the card's place, (i) lines and note; not connected: dimmed/marked; connected and unread: two dashes with the
screen-reader text), `ControlsScreenTest` +1 (the card's place, subtitle, a tap asks for the other value), `EqScreenTest` +1 (no conversation row; the (i)
lists two); the dash-count assertions of both adapted (Controls 7 switches, Sound 8 or 6 dashes).

## C.4 Phase G — Item 4: design study only (nothing built; ADR-060 a draft)

The reverse engineering is §A.4 (with `file:line`) and, dated, `REVERSE_ENGINEERING.md`'s 2026-10-09 section. **Design, for 1.3.0 if the maintainer
wants it** (the draft ADR-060 in `DECISIONS.md`, status Proposed):

- **What the official app does (🟢 from code, 🟡 on the wire):** nothing on the Buds decides. Once per connection, one minute after the session starts,
  a one-time worker sums the seven most recent daily doses (`FetchDailySummaries` per bud, Left/Right averaged per day), divides by `3.3903457E11` and
  notifies at **≥ 67 %**, at most once per 7 days; the "Volume level notifications" switch (field 21) only gates the worker; the Buds never push a
  Dosimeter value (0 unsolicited packets in 85 logs, §A.4 (d)).
- **What OpenControl would add:** two requests per Connect (`FetchDailySummaries` on both channels — or the hosting channel only, open), optionally
  `SubscribeToLiveDb` for a live level; the 7-day sum on the phone with the same constant; one notification channel; the field-21 switch (already 🟢 as
  a setting, `ai-sessions/0077`) shown on gear → Settings and gating the computation. No timer: the computation runs at Connect only.
- **Why not in 1.2.0 (the 🟡 that block it):** the unit of `qia` field 6 and `qhz` field 5 is not in the code; the 67 %/7-day constants are the official
  app's, not a documented limit; whether `FetchDailySummaries` on the non-hosting channel answers (`CAP-042`'s `FAILED_PRECONDITION`); and the live
  level's dB mapping (`round(10·log10(v))`) has never been checked against a meter — `WELL-002`. Each needs a capture before a user-facing number
  ("you are at 67 %") is honest (`AGENTS.md` §5's spirit: never a fabricated figure).

## H. Phase H — gate, mutations, compliance

**Gate.** Command: `cd android && ./gradlew --offline --max-workers=2 clean assembleDebug testDebugUnitTest test lint`. Counts from the JUnit XML
(`*/build/test-results/<task>/*.xml`, `tests=` summed, debug/release equal).

| Gate | Exit | `:data` | `:domain` | `:hardware` | `:ui` | Lint | `w:` |
|---|---|---|---|---|---|---|---|
| Baseline (§A.0, before any change) | 0 | 1627 | 37 | 64 | 70 | `:app` 1 `OldTargetApi`, `:ui` 1 `ModifierParameter` (both pre-existing) | 0 |
| First full gate after Phase F | **1** | 1644, **1 failed** (the ADR-059 repository test: NotRead vs InEarDetectionNotRead after a failed connect — `_ancAvailability` was reset after the bonded lookup) | 44 | 64 | 84 | — | — |
| After the fix (class re-run, scratchpad `fix_worn`) | 0 | the class green | | | | | |
| **Final clean gate** (after the version change, scratchpad `gate_final`) | **0** | **1644** | **44** | **64** | **84** | the same two, nothing new; `:data`/`:hardware` 0 | **0** |

New tests: `:data` +17 (6 codec, 11 repository), `:domain` +7, `:ui` +14. No suppression added (`grep -rn "@Suppress\|@SuppressLint" android/*/src/main`
unchanged against `main`).

**Mutations** — each applied by `mutate.py` (scratchpad), the relevant test task run with `--max-workers=2`, one at a time, the file restored and its
`sha256sum` compared (all "restored identical"; the log is the mutation log (`mutations`, a scratchpad file) in the scratchpad):

| M | Mutation (file) | Result | Killed by (first failing test) |
|---|---|---|---|
| M1 | the ACKed own `Set` recorded as CHANGED_BY_BUDS (`BudsRepositoryImpl.kt`) | killed (1) | Item 1: the app's own Set (Get → Notify → Set → ACK), then the Buds' Notify of that mode: set by this app |
| M2 | the cause not reset at Connect (`BudsRepositoryImpl.kt`) | killed (1) | Item 1: a new Connect resets the cause |
| M3 | field 7 read as field 8 (`HardwareInfo.kt`) | killed (8) | ADR-058: Connect sends exactly one GetHardwareInfo …; the codec tests |
| M4 | a serial logged at INFO (`BudsRepositoryImpl.kt`) | killed (1) | ADR-058: Connect sends exactly one GetHardwareInfo … (the test asserts the log line carries the count and no serial string) |
| M5 | "Probably worn" with Settable `00` (`WornReading.kt`) | killed (2) | CAP-065 5465 (00 …) → not worn |
| M6 | "Probably worn" with in-ear detection off (`WornReading.kt`) | killed (1) | CAP-064 10394 (e8, in-ear detection off …) → unknown |
| M7 | "In the case" for a bud whose flag is not `true` (`WornReading.kt`) | killed (3) | M7 guard: one bud charging (CAP-062 3760) … is never 'in the case' |
| M8 | "Earbuds replaced" writing field 27 after the move (`SettingsMenu.kt`) | killed (1) | the Settings tab shows the Case sounds card … (the tap asks for field 28) |
| M9a | Conversation detection's card calling the mono callback (`ControlsScreen.kt`) | killed (1) | ai-sessions 0082 — Conversation detection moved here … a tap asks for the other value |
| M9b | `setConversationDetection` writing field 19 (`BudsRepositoryImpl.kt`) | killed (1) | ADR-045 on channel 21: conversation detection OFF = CAP-019 1720 … |
| M10 | `GetHardwareInfo` gated behind Safe Mode (`BudsRepositoryImpl.kt`) | killed (4) | ADR-058 item 2 / ADR-042 item 3: a read is not gated … |

**Compliance** (commands run on the final tree):

| Check | Command / evidence | Result |
|---|---|---|
| No manifest, version-catalog or dependency change | `git diff --name-only -- '*.toml' '*AndroidManifest.xml'` | empty (exit 0); the only Gradle diff is `versionCode`/`versionName` in `android/app/build.gradle.kts` |
| `transport.send` call sites in the diff | `git diff -U0 -- android \| grep '^[+-].*transport\.send'` | two re-indented existing calls (the ANC `Get`/`Set` moved inside a try/finally) and **one new**: `readHardwareInfo`'s `transport.send(Dlci.MAESTRO, wire)` — the ADR-058 request |
| Nothing on DLCI 0x08/0x0a | `git diff -- android \| grep -i '0x08\b\|0x0a\b'` | only the two test `emit`s on `FAST_PAIR_MESSAGE_STREAM`; no new DLCI constant |
| No new codec field except field 7 | `HardwareInfo.kt` reads field 7 only; `CodecRouter` adds one method to `answersUs` | yes |
| Every write passes the ADR-042 gate | no write path changed (`git diff` of `setCaseSound…`, `setConversationDetection`: none) | yes; the one new request is a read, ungated by ADR-058 item 2 |
| AGPL headers on new Kotlin files | `grep -c 'SPDX-License-Identifier: AGPL-3.0-or-later'` over the 7 new files | 1 each |
| No MAC or serial at INFO+, no raw payload outside debug mode | `git diff -- android \| grep '^+.*Log' \| grep -i 'serial\|address\|mac'` → nothing; the one new log line prints a **count** | yes (M4 guards it) |
| No `INTERNET`, no new permission | the manifest is unchanged (above) | yes |
| Docs | `python3 scripts/ensure_footers.py` (all up to date); `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` | exit 0; `scripts/__pycache__` unchanged |

## I. Phase I — the hardware-run skeleton (`CAP-072`, Group BH, planned)

Registered: `id_registry.csv` (`CAP-072` planned, `INEAR-006`, `WELL-002`), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group BH section before §4.3, Capture Index
row), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (`INEAR-006` new; `INEAR-005` groups "BF, BH"; `FW-003` updated; `WELL-002` new, no Group), `APP_TESTPLAN.md`
(section V, V1–V19; M5 → N12; T5/T6, N1, M1 notes; Summary row). The skeleton
`captures/CAP-072-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BH/CAP-072-EVENT-NOTES.md` follows `CAP-071`'s layout. The steps in one table:

| Step | Action | Expected on screen | Expected HCI bracket | Test-ID |
|---|---|---|---|---|
| P1 | Dark mode On, Debug mode on in 1.1.1; `dumpsys … firstInstallTime/lastUpdateTime`; install 1.2.0 over it; `dumpsys` again | — | — | — |
| BH-1 | open the app (lid open); Controls within 1 s | dark; "—" then values; Conversation detection card, no Case sounds card | `4:16`, twelve reads, `SubscribeRuntimeInfo`, **one** `GetHardwareInfo` = `CAP-036` 1415 / `CAP-024` 801, answered | `PAIR-003`, `BATT-004`, `FW-003` |
| BH-2 | gear → Settings (Case sounds card, (i)), Debug, Info (serials, hold 3 s) | the choices kept; "Serial numbers (from the Buds, HH:MM:SS):" Case / Right bud / Left bud + note | nothing | `FW-003` |
| BH-3 | Connection: Worn line, (i) | "Both buds in the case" | the `6.2`/`6.3` charging flags | `INEAR-006` |
| BH-4 | Dark mode System | follows | — | — |
| BH-5 | only the Left in; Info; pull on Connection | "Probably worn (checked …)" | announcement 19; thirteen requests; `08 11` → `Notify` `e8` | `INEAR-005`, `INEAR-006` |
| BH-6 | the Right in; pull | "Probably worn" later time | `Notify` `e8` | `INEAR-006` |
| BH-7 | both worn on 19: the Left out, in view; pull | re-open by itself; new channel; "Probably worn" | 🟡 Buds `DISC` + announcement 21 (L-1) | `INEAR-005`, `INEAR-004`, `INEAR-006` |
| BH-8 | both on the table; pull; ANC tap | "Not worn (checked …)"; tap refused | `Notify` Settable `00`; no `Set` | `INEAR-006`, `INEAR-004`, `ANC-004` |
| BH-9 | in-ear detection off; pull; on; pull | "Worn: unknown — in-ear detection is off"; then "Not worn" | `4:{2:0}` OK; `Notify`; `4:{2:1}` OK; `Notify` `00` | `INEAR-006`, `INEAR-004` |
| BH-10 | both in; pull; four ANC taps; (i) each | "Probably worn"; no "Changed by the Buds" line | per tap `08 11` → `Notify` → `Set` → ACK | `ANC-001`…`ANC-004`, `INEAR-006` |
| BH-11 | press and hold the Left bud ×2; (i) each | the mode changes; "Changed by the Buds at HH:MM:SS (…)"; the time moves | unprovoked `08 13 … e8 e8 xx`, no `08 11`; nothing from the app | `ANC-001`…`ANC-004` |
| BH-12 | a tap; a pull on ANC; the tile; (i) each | the line gone each time | claims `08 11` → `Notify` (→ `Set` → ACK) | `ANC-001`…`ANC-004` |
| BH-13 | ears → case → table; pull at 5/15/30/45/60 s | "Probably worn" ≤ ≈ 30 s then "Not worn" (or "Both buds in the case" first) | `Notify` `e8` → `00`; flags 2 → 0 | `INEAR-006` |
| BH-14 | Settings → Case sounds off/on ×2 with the bud in/out of the case, say the sound; Other alerts off/on | each after its OK; (i) "changed …" | `4:{28:0}`/`{28:1}`, `4:{27:0}`/`{27:1}` = `CAP-070`/`CAP-071` | `CASE-001`, `CASE-002` |
| BH-15 | Controls → Conversation detection off/on; speak 5 s | each after its OK | `4:{22:0}` = `CAP-019` 1720, `{22:1}` = 1808 | `CONV-001` |
| BH-16 | Sound: Balanced, slider, Read EQ again, Right 4, Centre, mono, Volume EQ | each after its OK; no conversation row | the references of the table | `EQP-001`, `EQS-001`, `AUDIO-001`…`003` |
| BH-17 | Controls: touch, press and hold, mode list w/o Off, head gestures, Multipoint | each after its OK | the references | `HOLD-005`, `HEAD-001`, `MULTI-001` |
| BH-18 | Disconnect, Connect; Sound, Controls, Settings, Info | every value read back; the same serials, new time; Worn "—" then "Probably worn" | thirteen requests; one `GetHardwareInfo`, same answer | `PAIR-003`, `FW-003` |
| BH-19 | Ring Left, Stop, Ring Right, Stop | the ring | `04 01 00 01 02/00/01/00` | `FIND-001`, `FIND-002` |
| BH-20 | the case (lid open) 10 s, out again | cause text; re-open by itself; Worn "—" then a reading; serials again | `DISC` → `SABM` ≈ 1.5 s; thirteen requests | `CASE-004`, `CASE-005`, `PAIR-003` |
| BH-21 | Home 2 min | no crash | — | — |
| BH-22 | Export; Bluetooth off 10 s; on | "Bluetooth is disabled."; re-open; serials "Not read" then read | the export lines; **no serial/MAC in the export**; thirteen requests | `PAIR-003` |
| BH-end1 | Export; minute change; stop film 1 | "Debug log saved (N lines)." | — | — |
| BH-23 (film 2) | Right into the case; tap Multipoint before "ready" | "The setting was not changed: …"; unchanged | no `WriteSetting` | `CASE-004` |
| BH-24 | export across a rotation | "Debug log saved (N lines)."; content | — | — |
| BH-25 | Bluetooth off; force-stop; open; dumps of Controls, Sound, Settings | every "—" with `Not read from the Buds yet` (7 / 8 / 2) | — | — |
| BH-26 | Bluetooth on; Export; stop the system log; minute change; stop film 2 | ready; values and serials back | thirteen requests | `PAIR-003` |

**`TODO.md` §2 items taken:** T11 (three dumps now), C12, H5, S6, S12, L-1/`INEAR-005` (BH-5…BH-7), the case-sound and Volume-EQ observations, the P1
`dumpsys` update check. **Not taken** (and why, §A.6): B4 and the CDM picker (the Buds must be forgotten and re-paired — a separate run), K5/BC-12 (Owner
user), F-4 (debug build only), a second device (one phone). **One sitting?** Film 1 is ≈ 35 min, film 2 ≈ 10 min; `CAP-071` dropped its film 2. The
skeleton allows film 2 on another day — the maintainer decides; the question is in the session's closing summary, not silently cut.

## J. Phase J — documentation and release preparation (REL chosen)

Changed, only what the code changed: `ARCHITECTURE.md` (§2.4 tree and tab texts; §3.1 ANC row's cause sentence + rows "Serial numbers" and "Worn
indicator"; the codec-scope paragraph; §5a three rows), `PROJECT.md` (serials `[x]`, worn `[x]` "probably"; the 1.2.0 status paragraph), `CHANGELOG.md`
(`[1.2.0] - not yet released`: Added / Changed / Studied, not built / Known issues), `README.md` (the 1.2.0 paragraph; Current state 2026-10-09: 72
captures, 3 planned; 59 accepted ADRs + one draft; the implemented list; "Protocol-known but not built"), `TODO.md` (§2 next run = `CAP-072` and the
`INEAR-006` doubling; §4 Dosimeter; §5: the `CAP-069` §12 item 8 line deleted, the 1.3.0 candidate, the fixture swap after `CAP-072`, the `CAP-064`
wording note), `REVERSE_ENGINEERING.md` (the dated item-4 section + the serial-position note), `PROTOCOL.md` (§6 serial attribution promoted 🟢 with the
maintainer's approval; §8 row), `DECISIONS.md` (ADR-058, ADR-059 Accepted; ADR-060 Proposed draft — the deviation above), `ai-sessions/INDEX.md` (the 0082
row), `scripts/release_notes.template`, `android/app/build.gradle.kts` (`versionCode = 10200`, `versionName = "1.2.0"`). `ensure_footers` and `lint_docs`
exit 0.

**Not run, for the maintainer (`RELEASING.md` §5–§6), after the merge of `feature/1.2.0`:**

```
git checkout main && git pull
git checkout -b release/1.2.0
scripts/release.sh 1.2.0            # builds and signs dist/1.2.0/, prints the APK and certificate SHA-256
git tag -a v1.2.0 -m "OpenControl for Pixel Buds Pro 2 1.2.0"
git push origin release/1.2.0 --tags
gh release create v1.2.0 dist/1.2.0/opencontrol-pixelbudspro2-1.2.0.apk --notes-file <release notes from scripts/release_notes.template>
```

(Verify each line against `RELEASING.md` before running — this list is a reminder, not the procedure; the release verdict waits for `CAP-072`.)

## K. Sources and reading

**External sources:** none consulted in Phases C–K (Phase A's are in §A.4: the APK's own strings and classes, `file:line`). **Read in full in this
part:** the prompt (again, Phases H–K), `CAP-071-EVENT-NOTES.md`'s appendix (the skeleton layout), `APP_TESTPLAN.md` sections U and Summary,
`AI_SESSION_LOG_PROCEDURE.md` §4–§5, `WornReading.kt`, `HardwareInfo.kt`, `HardwareInfoFixtures.kt`, the mutation log (`mutations`, a scratchpad file). **In part:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
(Group BG, the index), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the ANC/INEAR rows), `PROTOCOL.md` §8 header, the gate logs (counts from the JUnit XML, not the
console). Nothing was taken from a subagent (none used).

## Deferred documentation

Each line also in `TODO.md`, in these words:

- `TODO.md` §5: "**1.3.0 candidate (M):** volume-level notifications from `maestro_pw.Dosimeter` — draft ADR-060 … Blocked until `WELL-002` … and the other
  🟡 of the draft are 🟢."
- `TODO.md` §5: "After `CAP-072`: replace the redacted-answer fixtures of `GetHardwareInfo` (`HardwareInfoFixtures`: `CAP-036` 1423 / `CAP-024` 832 with the
  serials X-ed out) by OpenControl's own exchange from that run (serials redacted the same way); ADR-058/ADR-059 Updates with the run's result (**M**)."
- `TODO.md` §5: "The worn indicator's (i) text names `CAP-064` to the user (the approved wording, `ai-sessions/0082` §B); reword without the capture id if
  the maintainer wants (**M**)."
- `TODO.md` §2: "In `CAP-072` the same steps read the worn indicator (`INEAR-006`, ADR-059)." (L-1 stays open until that run.)
- `TODO.md` §4: the Dosimeter line's addition ("**Volume-level notifications** studied in `ai-sessions/0082` (item 4) … a capture with a sound-level meter
  (`WELL-002`, no Group yet) comes first").

## Commits

Made after the maintainer's "yes" in chat (2026-10-09, `AskUserQuestion` "Commit/PR": *"Ja: commit, push en PR (Recommended)"*; ADR-060 stays as the
Proposed draft; `CAP-072` in one sitting). Six commits, not the nine sketched in task 25: the five app items share `BudsRepository(Impl)`, the UI state,
`MainActivity`'s wiring and `SettingsMenu.kt` (serial lines + the Case sounds card), so per-item commits would not compile on their own
(`PROJECT_RULES.md` rule 16 outranks the example split). The app commit's tree is the final gate's tree minus the version line (§H).

| Hash | Subject |
|---|---|
| `ca9debe` | feat(app): 1.2.0 — "Changed by the Buds", serial numbers, the worn line, two tab moves |
| `d7456e4` | docs(re): the official app's volume-level notification and the serial-position attribution |
| `5896222` | docs: ADR-058/059 accepted, draft ADR-060, PROTOCOL §6, the documents after 1.2.0 |
| `168d9a5` | docs: CAP-072 (Group BH) run skeleton, capture index, INEAR-006/WELL-002, APP_TESTPLAN §V |
| `0926b32` | chore(release): 1.2.0 (versionCode 10200), release-notes template |
| (next) | docs(ai-sessions): 0082 FEATURE prompt, result and INDEX row — its own hash is back-filled by the next session (`AI_SESSION_LOG_PROCEDURE.md` §4b item 2) |

Not staged: `android/domain/bin/` (an untracked build-tool output that appeared during the session; not the project's). Then `git push -u origin
feature/1.2.0` and a pull request to `main` (not merged by the agent).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0082_FEATURE_RESULT_2026_10_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0082_FEATURE_RESULT_2026_10_09
