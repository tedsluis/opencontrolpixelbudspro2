# 0051_FEATURE_RESULT_2026_09_26.md — Answer the maintainer's eleven questions about the app: what is possible, what is not, and what each needs

**Number:** 0051
**Category:** FEATURE
**Date:** 2026-09-26
**Title:** Answer, with evidence from the code, the captures, the protocol documents, the decisions and official documentation, the maintainer's eleven questions about the OpenControl for Pixel Buds app; no app change in this session
**Status:** awaiting maintainer sign-off

## Progress

- Phase 0: done. Start commit `bb4845a`; `git status --short` clean before this file was created. Gate (§3): BUILD SUCCESSFUL.
- Phase A: done — Q1 … Q11 answered (§5–§15), cross/consistency checks (§16), summary table and order of work (§17–§18).
- Phase B (checkpoint): done — answers in §20.
- Phase C: done — `TODO.md` (open items the maintainer chose), `ai-sessions/INDEX.md` (0051 row); no capture Group added (not asked for);
  no documentation defect fixed (none of F-1…F-8 is mechanical within the prompt's rule — each is recorded for the next session). Footer/lint §22.
- Intermediate results: scratchpad `/tmp/claude-1000/-home-tedsluis-git-opencontrolpixelbudspro2/16c1e41d-887c-4656-b7a8-bf8dbce6a567/scratchpad`
  (`opens.py`, `subs.py`, `xval.py`, scratchpad f1719, scratchpad fother, scratchpad rti, scratchpad disc all, film frames scratchpad c19 sheet/scratchpad c20 sheet) — every
  command is also written out below, so the scratchpad can be re-created.

## 1. Plain-language answers

1. **In ears.** The Buds do not tell the app *which* bud is in an ear. The only wear hint is one byte in the ANC status ("no ANC mode can be
   switched now"), which in `CAP-062` meant "no bud worn" — but that is still a hypothesis and it is not per bud. The app already uses it to grey
   out ANC. Showing "worn / not worn" needs one small test first (one bud in an ear, one on the table); "which bud" has no known signal at all.
2. **Case battery unavailable / Refresh.** The Buds only send the Case level while at least one bud is charging in the case; with both buds out,
   there is nothing to read, from any channel the app may use. *Refresh battery* only re-reads Left/Right (a design decision, ADR-043). Re-asking
   the Case on Refresh is a new request and needs your decision; it only helps while a bud is in the case.
3. **Volume balance.** Not built. The protocol part is well proven (field 17, ±100, polarity, 30 of 30 official writes accepted, the value
   survives reconnects). Reading it is already allowed; changing it needs an ADR from you. Then it is ordinary app work.
4. **Mono.** Same as balance (field 19): proven in both directions, reading allowed, writing needs an ADR. Small app work.
5. **Battery times after Refresh.** In the latest build each bud's time changes only when the Buds actually send a new battery message, which they do
   every time the app newly opens their message channel (120 of 120 opens in the app captures). If you tap Refresh within ≈ 2.5 s of an earlier
   action, the channel is still open, nothing new arrives and the time stays — and the Case time never changes on Refresh by design. Which build you
   used decides which of these you saw (question in the checkpoint).
6. **Disconnect with both buds docked.** It is the Buds, not Android and not the app: every time, the Buds themselves end the Bluetooth link
   (reason `0x13`, "remote user terminated"), and the phone sent no disconnect command. With the lid open Android reconnects by itself within
   milliseconds and that link then stays up; the app (current build) re-opens its channel while it is on screen. The app cannot prevent the drop.
7. **EQ preset layout.** Yes — pure layout work, no protocol, no ADR. Two or three per row; your choice of layout.
8. **Find with the case closed.** Not possible for this app. With the case closed and both buds docked there is no Bluetooth link to send a ring
   over. Google's Find Hub can ring over Bluetooth LE, but only with keys that Google Play services creates and keeps (owner account key, ephemeral
   identity key); this project does not have them and has ruled that route out.
9. **Ring the Case.** Not possible: the local ring command has no Case value; Find Hub's LE ring has a "ring case" bit but needs the same owner
   keys (ADR-027, ADR-008).
10. **Conversation detection.** Possible after your decision. New in this session: `CAP-019`'s film shows the official switch turned **off** (frame
    1720) and on (1808) — both directions were in fact captured. Reading is allowed; writing needs an ADR.
11. **Touch controls and per-bud ANC modes.** "Use touch controls" (field 4): possible after your decision; `CAP-020`'s film also shows the **off**
    tap (frame 1995). Press-and-hold per bud (field 7): proven per bud. The ANC-mode list is **one** list on the wire and in the official app's code
    — not one per bud — and which bit is Adaptive vs Transparency is disputed (code vs our hypothesis); that needs a capture first.

## 2. Reading (prompt §0)

Read in full in this session: `AGENTS.md` (in context), `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `AI_SESSION_LOG_PROCEDURE.md`,
`ai-sessions/INDEX.md`, `0047` RESULT, `0048` RESULT, `CAP-062-FINDINGS.md`, `CAP-062-EVENT-NOTES.md`. **Disclosed partial reads:** `PROTOCOL.md` §0–§5
line by line (lines 1–1917) and §6/§7 by heading plus every passage the answers touch (keyword search + the passages at 2253–2260, 2335–2350,
2640–2725, 2860–2900, 2925–3060, 3116–3126); `DECISIONS.md` by ADR heading list (ADR-001 … ADR-044 with every Update heading) and in full for
ADR-011, 013, 016, 019 (+ its Updates), 024 (+ Updates), 026, 027 (+ Update), 032 (+ Update), 033 (+ Update), 034 (+ Update), 036, 040, 041, 042,
043 (+ Update), 044; `TODO.md` head (lines 1–40) and the technical-debt section (770–851); `0046` RESULT not read beyond what `0047`/`0048` quote;
`0050` RESULT §1–§4; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Groups AR and AX; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` and `APP_TESTPLAN.md` not re-read (their
rows are cited only through `CAP-062`/`0048`); `REVERSE_ENGINEERING.md` `qjg`/`qht`, `qjo`/`qju` entries in full, the others by heading.
**Kotlin read in full:** `BudsRepositoryImpl.kt`, `RuntimeInfo.kt`, `CodecRouter.kt`, `Maestro.kt`, `EqFrame.kt`, `EqFrameEncoder.kt`,
`EqFrameDecoder.kt`, `BatteryStatus.kt`, `RingTarget.kt`, `ConnectionScreen.kt`, `EqScreen.kt`, `FindMyBudsScreen.kt`, `OsConnectionObserver.kt`,
`SessionReopener.kt`, `EqBandGains.kt`, `TimeFormat.kt`; `MainActivity.kt`/`OpenControlNavHost.kt` by the lines cited; the `7498cbc` (0046 build)
versions of `ConnectionScreen.BatteryCard` and `BudsRepositoryImpl.handleRoutedFrame` (via `git show`). No subagent was used.

## 3. Phase 0 — baseline

`git log -1`: `bb4845a docs: add prompt 0051 …`; working tree clean. `cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint` →
**BUILD SUCCESSFUL**; JUnit XML: `:data` 1509, `:hardware` 49, `:domain` 17 tests, 0 failures, 0 skipped. Lint: `:data`/`:hardware`/`:ui` "No issues
found"; `:app` 0 errors, 1 warning (`AndroidManifest.xml:37` `DataExtractionRules` — the `MissingApplicationIcon` warning of `0050` is gone since
`8291737`). Nothing under `android/` was changed in this session.

## 4. Commands used throughout (rule 4a)

- DLCI 0x02 decode: `python3 scripts/pwrpc_decode.py <log>` (DLCI 0x02 only).
- Per Message Stream open: `opens.py` = `tshark -r <log> -Y "btrfcomm && (btrfcomm.dlci==4 || btrfcomm.dlci==5)" -T fields -e frame.number
  -e frame.time_epoch -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.frame_type -e btrfcomm.len -e data.data -e bthci_acl.chandle`, grouped per
  (handle, DLCI) from `SABM` to `DISC`, counting Buds-side data frames containing `03030003`.
- HCI disconnects: `tshark -r <log> -Y "bthci_cmd.opcode==0x0406 || bthci_evt.code==0x05" -T fields -e frame.number -e bthci_cmd.opcode
  -e bthci_evt.code -e bthci_evt.reason -e bthci_evt.connection_handle -e bthci_cmd.connection_handle`.
- All loops ran over **every** `captures/CAP-*/CAP-*btsnoop_hci*.log` (53 files).

---

## 5. Q1 — "In ears" is not shown

**Short answer.** The Buds send no per-bud wear state that this project has found. The only wear-related signal is the ANC `Notify` "Settable" byte
(`0x00` = no ANC mode switchable), 🟡 "no bud worn", not per bud; the app already uses it to disable ANC.

**Why it is as it is.**
- `PROTOCOL.md` §4.5.5 "In-ear detection" is the **setting** (`qhr` field 2, on/off; 🟢 field/category identity, ADR-019 Update) — read-only access is
  unblocked by ADR-036 but not built (`Maestro.READABLE_FIELDS = {16, 18}`, `Maestro.kt:43`). Across all captures the official app's reads of field 2
  return `1` ×93 and `0` ×4 (scratchpad fother, command §4) — that is the switch, not the live state.
- The live hint: `AncAvailability` from the `Notify` Settable byte (`BudsRepositoryImpl.kt:411`, `setAncMode` refuses at `:594`). `CAP-062`:
  10/10 NAKs with Settable `00`, 6/6 ACKs with `e8`, `00` also with both buds on the table (ADR-024 Update 2026-09-25, 🟢 for the NAK rule, 🟡
  (strong) for "not worn"). Not per bud, and only fresh on a DLCI 0x04 claim.
- **Checked this session — no per-bud wear signal at the `CAP-062` wear changes** (all DLCIs, HCI, LE handle `0x000b`/`0x0042`):
  `tshark -r CAP-062-btsnoop_hci.log -Y "frame.time >= … && (btrfcomm.len>0 || bthci_evt.code==0x05 || btrfcomm.frame_type==0x43 ||
  btrfcomm.frame_type==0x2f || bthid || btatt || bthfp)"` for 06:48:48–06:49:15, 06:51:58–06:52:08, 06:54:58–06:55:08: out of the ears (06:52:05)
  → only the Buds' `DISC` of DLCI 0x02 (9362) and `AT+BIEV=2,100` on DLCI 0x09 (9372, `41542b424945563d322c3130300d`); into the ears (06:55:04) →
  only the `DISC` (10163); ATT traffic in the window is handle `0x0041` = another device. The `DISC` itself says "something changed", not what.
- **Runtime-info stream fields 3 and 7.3:** `3:0` in **438 of 438** `SERVER_STREAM` packets of all captures, `7.3 = 0` in 438 of 438 (scratchpad rti:
  every `SubscribeRuntimeInfo` `SERVER_STREAM` line of `pwrpc_decode.py` over the 53 logs), including `CAP-062` 8612/8649 taken while the buds were
  (per film) worn — they never vary, so nothing links them to wear.

**Evidence status.** field 2 identity 🟢 (category); its UI label 🟡; Settable `00` ⇒ NAK 🟢; `00` = "no bud worn" 🟡 (strong); a per-bud wear
signal 🔴 (none found); field 3 / 7.3 meaning 🔴 (constant 0).

**Verdict: needs evidence first** (for "worn / not worn", both buds together); per bud: **not possible** on current knowledge (no signal known).

**What it would take.**
1. Capture (Group AY, step AY-3 of `0048` §9, already planned): one bud in an ear, the other on the table, `08 11`→`08 13` via ANC Refresh; swap;
   both in; both out. HCI bracket: `08 11 00 00` → `08 13 00 04 01 e8 <settable> <mode>` per step. Add: film the ears.
2. Decision: if AY-3 confirms, promote "Settable `00` = no bud worn" (ADR-024 Update) — draft in §19.
3. App (S): a "Worn: yes / no (both buds) — as of HH:MM:SS" line from `ancAvailability` (already in `:data`), explicitly "not per bud"; fresh only
   per claim (ADR-032), so it would be refreshed by the existing snapshot/ANC Refresh. Unit tests with `CAP-062` frames 6000 (`00`) and 8706 (`e8`).
4. Optional (S, no decision needed): show the **In-ear detection setting** read-only via ADR-036 (`ReadSetting 4:2`); fixture: any `CAP-036` read of
   field 2. Label "In-ear detection (setting)" and not as the live state.

**Risks/rules.** `AGENTS.md` §5 spirit (never show a fabricated state); ADR-024's settling caveat (a `00` within ~2 s of an open can be stale);
nothing new is sent.

## 6. Q2 — Case battery "unavailable", not updated by *Refresh battery*

**Short answer.** The Buds put the Case level in the runtime-info stream only while at least one bud is charging; with both buds out there is no Case
value on any channel the app uses. *Refresh battery* re-reads Left/Right only, by decision.

**Why.**
- **Firmware behaviour, re-derived this session:** entry 6.1 present ⇔ a charging bit set in the nearest DLCI 0x04/0x05 battery frame (±3 s):
  **410 of 417** stream packets in 44 capture files (`xval.py`; mismatches `CAP-006` 2959/2968, `CAP-016` 2199, `CAP-038` 3579/3605, `CAP-040` 6084 —
  all at a transition). `0047` counted 397/403 in 45 captures with a slightly different pairing; the mismatch sets agree except `CAP-040` 6084. 🟢
  (Option F, 2026-09-25). DLCI 0x08 does keep pushing the last Case value (the Google app's channel, `CAP-062` 2667 `0a 04 08 3c 18 03`, stale form,
  ADR-014), but the app no longer opens DLCI 0x08 (ADR-043, withdrawn after 20/20 failed claims in `CAP-061`).
- **Code (current build, 0048):** `RuntimeInfo` without 6.1 keeps a known Case as "last seen HH:MM:SS" (`BudsRepositoryImpl.kt:492–496`), but only
  if the Buds reported it at least once **in this app process**; otherwise "Case: Battery unavailable" + "Not reported yet — the Buds send the Case
  level only while a bud is charging in the case." (`ConnectionScreen.kt:392, 415–416`). `refreshBattery()` = one DLCI 0x04 claim with `08 11`
  (`BudsRepositoryImpl.kt:798–804`); the Case is not requested there (ADR-043 Consequences: "Refresh battery re-reads Left/Right only").
- **0046 build (`CAP-062`):** a packet without 6.1 set the Case to "unavailable" at once (`git show 7498cbc:…BudsRepositoryImpl.kt`, line 402–405);
  the wording was "The Buds haven't reported the Case level on this connection" (`CAP-062-FINDINGS.md` §7.4). Which build you used decides which you
  saw — asked in the checkpoint.
- **Could a Refresh re-ask?** A second `SubscribeRuntimeInfo` is a new send (ADR-043: one per Connect). Evidence: **141** requests in 44 capture files,
  **138** answered by a `SERVER_STREAM` within 1 s; the 3 unanswered were cut by a disconnect 0.1–0.6 s later (`CAP-006` 3230 → ACL `0x13` 3235;
  `CAP-047`-2 2004 → 2033; `CAP-009` 19571 → Buds `DISC` 19575). **No connection ever carried a second request** (0 of all opens) — whether the Buds
  answer a re-subscription on an open channel is 🔴 untested. Why it would matter: while docked the stream is silent between dock changes
  (`CAP-062` 06:46:57–06:48:52, E6), so the Case % and its time go stale while the Case charges or discharges.

**Evidence status.** 6.1 = Case % 🟢; 6.1 present ⇔ a bud charging 🟢; "charging" = "in the case" 🟡; a re-subscription answered 🔴.

**Verdict: possible with a maintainer decision** (re-subscribe on Refresh, ADR-043 Update) — with the firmware limit that **nothing** can show a
current Case level while no bud is in the case (**not possible**, firmware).

**What it would take.** (1) Checkpoint: which build. (2) Decision (draft §19, D-2). (3) App (S): on Refresh, if the session is open, send one
`SubscribeRuntimeInfo` (the `Maestro.subscribeRuntimeInfoRequest` bytes, `CAP-036` frame 1410 shape) besides the DLCI 0x04 claim; show "Case: last seen
…" / "Case reported only while a bud is in the case" as now. Unit test: the request bytes equal the Connect-time request of `CAP-062` 2777. (4)
Hardware step (Group AY): both buds docked, idle 2 min, Refresh → a `SERVER_STREAM` within 1 s or not (bracket: app `REQUEST … SubscribeRuntimeInfo`
→ Buds `SERVER_STREAM`). If unanswered, drop the idea.

**Risks/rules.** A second open server stream per connection could, in principle, duplicate pushes (🔴); Safe Mode is irrelevant (a read); `AGENTS.md`
§5: keep "last seen" with its time.

## 7. Q3 — Volume balance

**Short answer.** Not built because writes are gated per field. The protocol is well proven; you need to approve a write ADR, then it is app work.

**Why / evidence.**
- Identity `qhr` field 17 = Volume balance 🟢 (ADR-019 Update 2026-09-03), `sint32` (zigzag), range ±100 and `+100` = Left, `-100` = Right 🟢 (ADR-026).
- Reads unblocked (ADR-036), not implemented (`Maestro.kt:43` allows 16/18 only). Real read fixture: `CAP-036` 1526 request
  `7e004b0310151dea71de7d5e2551aed0ae2a022011d1deaab87e` (`ReadSetting 4:17`) → 1528 `7e00a5032a05220388010a…` = `4:{17:10}` (zigzag 10 = **+5**).
- **Official writes: 30 of 30 writes of fields 17/19 answered `RESPONSE` status OK** (`CAP-022` 9/9, `CAP-041` 5/5, `CAP-046` 16/16). Raw: `CAP-022`
  1922 `7e003b0310131dea71de7d5e251d9a8c9e2a0622048801c701bcfac4347e` = `4:{17:199}` (−100) → 1927 `7e80a303080110131dea71de7d5e251d9a8c9e4c05e6d97e`
  (empty RESPONSE); each write is mirrored on `SubscribeToSettingsChanges` (1926).
- **Persistence (new consolidation):** the first read of the *next* capture equals the last write of the previous one in three chains — `CAP-022`
  (last `17:10`) → `CAP-023` read `17:10` (1047); `CAP-041` (last `17:199`) → `CAP-042` `17:199`; `CAP-046` (last `17:2`) → `CAP-048` `17:2` (table from
  scratchpad f1719, 192 reads, 30 writes; sorted by the capture folders' dates). `PROTOCOL.md` §4.5.7/§6 still call persistence "not tested" — finding F-3.
- 🔴 Only the ±100 extremes and ~0 are film-correlated; intermediate values (`CAP-022`: −62, −25, 15, 75) exist but their slider positions were not
  filmed. For our own slider this only matters for a linear −100…+100 mapping, which is what the wire already carries.
- The official app places Balance on Device details → **Sound**, next to Conversation detection, Equalizer and Mono audio (`CAP-019` film at 07:36:27,
  scratchpad c19 sheet).

**Verdict: possible with a maintainer decision** (write ADR, draft D-3 §19). Read-only display is **possible now** (ADR-036).

**What it would take.** (1) ADR (D-3). (2) App (M): `Maestro.READABLE_FIELDS` += 17 (and 19); a `SettingFrame` codec for `4:{N:varint}` (zigzag for
17) — encoder byte-identical to `CAP-022` 1922 when built for channel 19, decoder tested on `CAP-036` 1528 and `CAP-022` 1926; a `setVolumeBalance(Int)`
in `BudsRepository` that passes `writeGate` (ADR-042), waits for the empty `RESPONSE` like `setEqGains`; the read joins the Connect sequence
(`launchInitialEqRead`); a slider on the EQ tab (−100 L … +100 R — label polarity per ADR-026), one write per release as the EQ sliders. Fuzz test
extended. (3) Hardware (Group AY): move the slider, `WriteSetting 4:{17:n}` → empty RESPONSE; reconnect → `ReadSetting` returns n; listen.

**Risks/rules.** Safe Mode gates the write; nothing sent before the ADR; audibility not established (as for EQ).

## 8. Q4 — Mono

**Short answer.** Same situation as balance; field 19 is proven in both directions.

**Evidence.** `qhr` field 19 = Mono audio 🟢 (ADR-019 Update), both directions on film (`CAP-022` 1621 `…2a052203980101…` = `4:{19:1}` → RESPONSE 1629;
1823 `4:{19:0}` → 1835). All 30 writes of 17/19 OK (§7). Reads: 96 of the 192 reads above are field 19 (`1` only after `CAP-041`/`CAP-046` writes);
read fixture `CAP-036` 1532 `…2a022013…` → 1534 `7e00a5032a052203980100…` = `4:{19:0}`. `CAP-046-FINDINGS.md` §3 records a 🟡 timing link between
17 and 19 writes (not a coupling claim).

**Verdict: possible with a maintainer decision** (write ADR D-3, together with balance). Read now possible (ADR-036).

**What it would take (S).** Same codec as Q3 (varint, no zigzag); a switch on the EQ tab; tests on `CAP-022` 1621/1823 and `CAP-036` 1534; hardware:
toggle → `4:{19:1}` → empty RESPONSE, reconnect → read 1, listen (mono is audible).

## 9. Q5 — Battery times not updated on *Refresh battery*

**Short answer.** Current build: each bud's time changes when the Buds send a new battery message; they send it every time the channel is newly opened.
A Refresh that finds the channel still open (≈ 2.5 s after the last action) gets no new message. The Case time is never touched by Refresh.

**Why.**
- `refreshBattery()` → `refreshAncMode()` → `withMessageStream` (`BudsRepositoryImpl.kt:798–804, 626, 844–878`): the channel is opened (and its buffer
  reset) **only** `if (!transport.isChannelOpen(DLCI 0x04))` (`:854`); a Refresh inside the 1.5 s linger (`MESSAGE_STREAM_LINGER_MS`, `:958`) after
  a previous claim's action (the Connect snapshot, ANC tap/Refresh, Find) cancels the release and only sends `08 11`; the Buds answer `08 13` but no
  `03 03` burst → no new time, while the refresh reports success.
- **The burst on every open, re-derived:** `opens.py` over all 53 logs: **273 of 297** DLCI 0x04/0x05 opens with a `UA` contain a Buds `03 03 00 03`
  frame; the 24 without: 21 bare `SABM`/`UA`/`DISC` bounces with zero Buds payload (`CAP-003` ×1, `CAP-011` ×3, `CAP-025` ×13, `CAP-040` ×4) and 3
  `CAP-040` opens (7496, 7627, 7738) where the Buds sent Device Information (`030a…`) and ACKs but no battery and no `Notify` (another client's opens,
  `07 11` writes). In the four app captures: **120 of 120** (`CAP-059` 19, `CAP-060` 27, `CAP-061` 31, `CAP-062` 43). Example: *Refresh* at
  06:46:28 in `CAP-062` — export lines 306–312: `RFCOMM channel 0x04 connected` 06:46:29.303, `03 03 00 03 e4 64 ff` at .610/.639.
- **Stamps in the current build:** each bud's level is stamped at receipt (`stamped(now)`, `:473–474`) and its charging state separately (`:475–476`);
  the Case only from the stream (`:492–496`). In the **0046 build** all three rows shared one `batteryStatusUpdatedAt` (`git show 7498cbc`,
  `BatteryCard`), so there a Refresh with a burst moved all three times, including the Case's — the behaviour you describe for the Case matches the
  current build.
- Other ways a Refresh changes nothing: a failed claim (`messageStreamError` shown in the Connection card, `:861`), no `Notify` in 2 s (`Timeout`).

**Evidence status.** The burst on open 🟢 (120/120 app opens; 273/297 all); the linger path is code fact; what you saw 🔴 until the build/log is known.

**Verdict: possible now within the current decisions** (app work), once the observation is pinned down.

**What it would take (S).** (1) Checkpoint: build + whether a debug export exists. (2) App: in `refreshBattery`, if DLCI 0x04 is still open from an
earlier claim, close it and open a new claim (both are ordinary claim/release operations of ADR-032 — no new message type), so the Buds' open-time burst
arrives; or, simpler, show "Battery: no new reading (channel already open) — try again in a few seconds". Show the Case line's own time as today,
plus "the Case is reported by the Buds only when a bud goes in or out of the case" next to the Refresh button. Unit test: `FakeBudsTransport` with the
channel open → Refresh → a close+open, and the `CAP-062` 06:46:29 burst bytes stamp new times. (3) Hardware: Refresh twice within 1 s; HCI: two
`SABM`s on DLCI 0x04, each followed by `03 03`.

**Risks/rules.** Each extra claim briefly takes DLCI 0x04 from Play services (ADR-032 contention, known); no polling (`ARCHITECTURE.md` §6).

## 10. Q6 — Disconnect when both buds are docked (lid open)

**Short answer.** Not the app and not Android: the Buds end the link. With the lid open Android reconnects immediately and the new link stays.

**Evidence.**
- ADR-016 item 5 (🟢): ACL `Disconnection Complete` reason `0x13` when the second bud is seated, lid open or not; `CAP-016` (official app era,
  before this app existed) and `CAP-048` (no OpenControl) show it too (scratchpad disc all: `CAP-016` 1, `CAP-048` 6 remote `0x13`).
- **Who initiates, checked this session:** in `CAP-062` the phone sent **no** `HCI_Disconnect` (opcode `0x0406`) for the Buds' classic handle
  `0x000b` at all; its only `HCI_Disconnect` (frame 2355, LE handle `0x0042`) produced `Disconnection Complete` reason **`0x16`** (2357); all six
  classic ends are **`0x13` without a preceding command** (3065, 3814, 4667, 6277, 7225, 10529). Across all 53 logs (scratchpad disc all): every
  `Disconnection Complete` that follows a local `HCI_Disconnect` reads `0x16` (all of them, e.g. `CAP-037` 22, `CAP-048` 6), and no local disconnect
  ever reads `0x13` — so `0x13` here means the peer ended the link (the Core specification's controller error codes name `0x13` "Remote User
  Terminated Connection" and `0x16` "Connection Terminated by Local Host"; the Bluetooth SIG page fetched this session lists these headings, but its
  HTML rendering returned no body text — see §19).
- Timing (`CAP-062`): the Buds first `DISC` DLCI 0x02 (6258, 7192), then drop the ACL 2.8–3.9 s later (6277, 7225); Android's own `Create Connection`
  12 ms later (7226, lid open, no app activity); that ACL then stayed up **9.6 minutes** (7226 → next `0x13` at 10529, an Android-settings
  disconnect) — including 2 minutes with both buds docked (session 9, 06:46:54.6–06:48:52.6, `CAP-062-FINDINGS.md` §3).
- **Lid closed:** after 3814 (06:42:35.1) no phone `Create Connection` and no ACL until the Buds paged the phone at 06:42:52.27 (3951), after the lid
  was opened and a bud removed (`tshark -Y "frame.number>=3814 && frame.number<=3960 && (bthci_cmd.opcode==0x0405 || bthci_evt.code==0x03 ||
  bthci_evt.code==0x04 || bthci_evt.code==0x05)"`). `CAP-048` §6 (🟡) links a retry burst to a closed case.
- Counter-example (🔴, `PROTOCOL.md` §6): `CAP-047` Recording 1 — a swapped-slot seating without an ACL drop.
- The app (current build): ADR-044 re-opens its channel when Android's link comes back while the app is visible (`SessionReopener.kt:92–96`,
  `LINK_BACK`), and the loss text says "Android no longer shows the Buds connected — … with both buds in the case …" (`ConnectionScreen.kt:338–340`).

**Evidence status.** Buds-initiated `0x13` on the second docking 🟢 (ADR-016); "lid-open reconnect by Android" 🟢 for `CAP-062` (one observation of
the 12 ms reconnect; other sessions show Buds-initiated pages instead, `PROTOCOL.md` §5.1); the swapped-slot exception 🔴.

**Verdict: not an app issue** (the Buds' firmware ends the link; Android restores it with the lid open). The app cannot keep a link the peer
terminates. Keeping the *app session* open in the background after Android restores the link is the unbuilt CDM-presence proposal (`ARCHITECTURE.md`
§6.0b, needs its own ADR).

**What it would take.** Nothing to fix; optionally (XS) a Connection-card hint "Putting both buds in the case ends the Bluetooth link — the Buds do
this; with the lid open Android reconnects by itself." Hardware confirmation of the app side is `0048` §9 AY-6.

## 11. Q7 — EQ preset buttons side by side

**Short answer.** Yes; UI only.

**Why it is as it is.** `EqScreen.kt:97–104`: `items(EqPreset.entries)` puts one `AssistChip` per row in the `LazyColumn`. There are **5** presets
(`EqBandGains.kt:58–64`: Heavy bass, Light bass, Balanced, Vocal boost, Clarity; "Last saved" deliberately excluded). `ARCHITECTURE.md` §2.4:153 says
"5 sliders + 6 presets" — finding F-5.

**Technical note.** Compose BOM `2024.09.00` resolves `foundation-layout` **1.7.0** (`./gradlew :ui:dependencies`); in that artifact the only `FlowRow`
overload is annotated `@ExperimentalLayoutApi` (`javap -v …/FlowLayoutKt.class`: `RuntimeInvisibleAnnotations: …ExperimentalLayoutApi` on `FlowRow`
and `FlowColumn`), i.e. it needs `@OptIn`. A stable alternative: `EqPreset.entries.chunked(n)` → one `Row` per chunk with `Modifier.weight(1f)` chips.
The current Android guide presents FlowRow as an ordinary API (fetched: "`FlowRow` and `FlowColumn` are composables that are similar to `Row` and
`Column`, but differ in that items flow into the next line when the container runs out of space.") — the opt-in is specific to the pinned 1.7.0.

**Verdict: possible now within the current decisions** (no protocol, no ADR; a visible layout change — your choice).

**What it would take (XS).** `EqScreen.kt` only: e.g. 3 + 2 (two rows) or 2 + 2 + 1 (three rows) with `chunked`; no new dependency; the long
`VOCAL BOOST` label fits a third of a 360 dp screen only with a smaller label style (⚪ not measured). Frees vertical space for Q3/Q4. Not unit-testable
here (no Compose tests, `TODO.md`); check on the phone.

## 12. Q8 — Ring a bud with the case closed

**Short answer.** Not possible for this app.

**Why.**
- The local ring (`04 01 00 01 <01|02|00>`, ADR-011) is a Message Stream message on DLCI 0x04 over the classic link. With both buds docked the Buds
  drop the link (ADR-016, Q6) and with the lid closed no link came back in `CAP-062` (17 s window, §10). Whether a **docked** bud rings with the lid
  **open** is 🔴 and planned (AY-1).
- The only connection-less mechanism documented is Google's Find Hub Network (FHN). Fetched this session
  (`developers.google.com/nearby/fast-pair/specifications/extensions/fmdn`): Beacon actions characteristic "`FE2C1238-8366-4814-8EB0-01DE32100BEA`";
  ring over a BLE **GATT write** ("Octet 0: uint8 Ring operation - A bitmask having the following values: Bit 1 (0x01): Ring right | Bit 2 (0x02): Ring
  left | Bit 3 (0x04): Ring case"); authentication "The first 8 bytes of `HMAC-SHA256(account key, …)`" and "Ring key: Defined as
  `SHA256(ephemeral identity key || 0x02)`…"; "Ephemeral identity key (EIK): A 32-byte key chosen at random by the Seeker when performing the FHN
  provisioning process." The Seeker that provisioned these Buds is Google Play services / Find Hub; this app has neither the account key nor the EIK.
- Scope: account key / owner keys are Play services' (ADR-008, ADR-025); no GMS (`AGENTS.md` §1); no scanning beyond ADR-006 (`AGENTS.md` §7); a GATT
  client is not built (`ARCHITECTURE.md` §1). The page also states "Find Hub network requires location services and Bluetooth to be turned on.
  Requires cell service or internet connection." (the network part is for locating, not for a GATT ring — ⚪).
- Whether the Buds even advertise/connect over LE with the lid closed is 🔴 (the FHN page only says a provisioned Provider "is expected to advertise
  FHN frames at least once every 2 seconds").

**Verdict: not possible** (no link with the case closed; the connection-less FHN route needs owner keys held by Google — scope/Zero-GMS).

**What it would take.** Nothing to build. Related, in scope: AY-1 (docked bud, lid open, link up) — if it rings, the app can offer Find while docked.

## 13. Q9 — Ring the Case

**Short answer.** Not possible.

**Why.** The local Device Action ring has no Case value — fetched this session (`…/extensions/deviceaction`): "0x00 (0b00000000): All components
should stop ringing", "0x01 … Ring right, stop ringing left", "0x02 … Ring left, stop ringing right", "0x03 (0b00000011): Ring both left and right";
nothing for a case. FHN has "Bit 3 (0x04): Ring case" but only through the owner-key-authenticated GATT write (§12). The official app routes Case ring
through Find Hub (🟢, zero local `04 01` frames, `PROTOCOL.md` §4.4). ADR-027: Case/"both" out of scope; "ring both" `0x03` is local but untested and
needs its own ADR (`FIND-004`) — not a Case ring.

**Verdict: not possible** (no local Case command; FHN needs owner keys — ADR-027/ADR-008, Zero-GMS).

## 14. Q10 — Conversation detection on/off

**Short answer.** Possible after your decision; the protocol evidence is stronger than documented.

**Evidence.**
- `qhr` field 22 = the app's "Speech Detection" 🟢 (field/type, ADR-019 Update); the UI-label equivalence 🟡.
- **New this session — both directions on film.** `CAP-019` frame 1720 (07:36:28.596) `7e004b0310151dea71de7d5e251d9a8c9e2a052203b00100225fc3b77e` =
  `WriteSetting 4:{22:0}` → empty RESPONSE 1731 (status OK), mirrored 1730; film (scratchpad c19 sheet, overlay 07:36:27 → 07:36:31): the official Sound
  screen's **"Conversation detection — Automatically switch from noise cancellation to transparency when talking"** switch is ON, a finger taps it at
  07:36:29, it reads OFF at 07:36:31; frame 1808 `4:{22:1}` is the ON tap (07:36:41, `CAP-019` EVENT-NOTES). So the UI switch writes field 22 in both
  directions. `CAP-019-EVENT-NOTES.md` says the setting was "found OFF at the start" and `PROTOCOL.md` §4.5.1 "OFF direction not captured" — finding
  F-1. Both writes answered OK (2/2). Reads: `4:{22:1}` in 96 of 96 reads (scratchpad fother).
- `CAP-029-FINDINGS.md` §2: the feature's media-pause itself has no wire effect (not needed for a toggle).
- Read access: field 22 is in ADR-036, but its semantics are not full FACT → ADR-036 allows it "raw in the Debug tab only".

**Verdict: possible with a maintainer decision** — (a) promote the label equivalence ("Conversation detection" = field 22, both directions, film +
wire) and (b) a write ADR (D-3). Drafts in §19.

**What it would take (S).** Codec as Q3/Q4 (varint); a switch (Sound/EQ tab); tests with `CAP-019` 1720/1808 and a `CAP-036` read of 22; hardware:
toggle → `4:{22:n}` → empty RESPONSE; talk while worn to hear the switch to transparency.

## 15. Q11 — "Use touch controls" and the ANC modes per bud

**Short answer.** Touch controls on/off: possible after your decision (both directions now on film). Press-and-hold action per bud: proven. The ANC-mode
list is **one** list, not one per bud, on the wire and in the official app's code; which bit is Adaptive vs Transparency needs a capture.

**Evidence.**
- **Field 4** ("Use touch controls") 🟢 (ADR-019). **New:** `CAP-020` frame 1995 (07:47:20.097) `7e004b0310151dea71de7d5e251d9a8c9e2a042202200053908d4b7e`
  = `4:{4:0}` → RESPONSE 2005 OK; film (scratchpad c20 sheet): the toggle is ON at 07:47:19, a finger taps it at 07:47:20, OFF at 07:47:21. The EVENT-NOTES
  call this "an incidental tap … no corresponding DLCI 0x02 … data frame" and `PROTOCOL.md` §4.5.3 "🔴 Not yet tested in the OFF direction" — finding
  F-2. Reads: `1` ×90, `0` ×6.
- **Field 7** (press-and-hold action, `qju`) 🟢 per bud: `7{1:…}` = Left, `7{2:…}` = Right, value 5 = ANC, 6 = Assistant (`CAP-021` 1895/3619/4315/4976,
  e.g. 1895 `22083a060a0422020806`). The read returns **both** buds in one message (`4:{7:{1:{4:{1:5}} 2:{4:{1:5}}}}` ×94). In ADR-036.
- **Field 12** (`qht`, "ANC gesture loop") — field number 🟢 only. The official app shows the checklist under both "Customize left" and "Customize
  right" (`CAP-021-EVENT-NOTES.md` line 18–20, 61), but: the 16 `CAP-021` writes carry no Left/Right field; the **read** returns one 4-bool message
  (`{1:1 2:0 3:1 4:1}` ×89, 96 reads); and the code has **one** preference screen with four keys (`hgj.java:115–129`) writing one `qht`
  (`hgj.java:216–331`). 🟡 (code + wire): one shared list. **Field→mode disagreement (finding F-6):** `qht`'s `RawMessageInfo`
  (`qht.java:31`, objects `b,c,d,e,f`) maps fields 1–4 to Java fields `c,d,e,f`; `hgj` sets `c` for `anc_preference_key_on`, `d` for `…_off`, `e` for
  `…_txp`, `f` for `…_adaptive` (getters `aJ/aI/aK/aH` → `this.f/ai/aj/ah`, `hgj.java:66–96, 115–126`) ⇒ 1 = Noise cancellation, 2 = Off, **3 =
  Transparency, 4 = Adaptive** — while `PROTOCOL.md` §4.5.3's 🟡 reads the order as on screen (3 = Adaptive, 4 = Transparency). `REVERSE_ENGINEERING.md`
  (`qjg`/`qht` entry) already states the code reading; `PROTOCOL.md` does not mention the conflict. Field 12 is **not** in ADR-036.
- Writes to fields 2/4/7/12/22 in all captures: **36 of 36** answered OK (`CAP-019` 2, `CAP-020` 2, `CAP-021` 21, `CAP-024` 2, `CAP-041` 9).

**Verdict: needs evidence first** for the ANC-mode list (the bit mapping and the "one list" reading); **possible with a maintainer decision** for the
touch-controls toggle and the press-and-hold action (write ADR D-3).

**What it would take.** (1) Capture — Group AR (planned `CAP-056`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` 1398) extended: on "Customize left" untick
**Adaptive** only (≥ 10 s), then open "Customize right" on film and see whether Adaptive is unticked there too; then untick **Transparency** only;
HCI: one `WriteSetting 4:{12:{…}}` per tap — which bit clears settles F-6. (2) Decisions: field-12 semantics promotion + ADR-036 extension to field 12
(read) + write ADR. (3) App (M): a "Touch & hold" card: touch controls switch (field 4), per-bud action (field 7, ANC / Assistant), one ANC-mode list
(field 12) with the official app's rule that at least two modes stay selected (⚪ — not checked in code); tests with `CAP-020` 1741/1995, `CAP-021`
1895–4976 and 5237…5415.

---

## 16. Cross and consistency checks (prompt task 4) — findings

| # | Where | What the file says | What the evidence says | Kind |
|---|---|---|---|---|
| F-1 | `PROTOCOL.md` §4.5.1 (lines 1364–1374), `DECISIONS.md:520` (ADR-013), `CAP-019-EVENT-NOTES.md` 34–35 | Conversation detection: single OFF→ON sample; "found OFF at the start"; OFF direction not captured | `CAP-019` 1720 `4:{22:0}` at 07:36:28.6 is a filmed ON→OFF tap, answered OK (1731) | protocol-status text → **maintainer** |
| F-2 | `PROTOCOL.md` §4.5.3 (1417–1418), `DECISIONS.md:909` (ADR-019), `CAP-020-EVENT-NOTES.md` 47, `CAP-020-FINDINGS.md` 161/182 | touch controls OFF not tested; the 07:47:17–20 tap produced no frame | `CAP-020` 1995 `4:{4:0}` at 07:47:20.1, filmed ON→OFF, answered OK (2005) | protocol-status text → **maintainer** |
| F-3 | `PROTOCOL.md` §4.5.7 (1559–1565) and §6 (2253–2256), `CAP-022-FINDINGS.md:225` | balance persistence across reconnect untested | 3 chains: `CAP-022`→`023` (10), `CAP-041`→`042` (199), `CAP-046`→`048` (2) | protocol-status text → **maintainer** |
| F-4 | `PROTOCOL.md` §6 (2150, 2160–2167), `CAP-034-FINDINGS.md` 255/482, `CAP-034-EVENT-NOTES.md:146` | `FE2C1238…` "still unnamed" | the FHN spec (fetched) names it "Beacon actions" | open question answered by an official page → **maintainer** (it closes a 🔴 in `PROTOCOL.md`) |
| F-5 | `ARCHITECTURE.md:153` | "EQ alone is 5 sliders + 6 presets" | the app shows 5 presets (`EqBandGains.kt:58–64`; "Last saved" excluded by design) | mechanical |
| F-6 | `PROTOCOL.md` §4.5.3 (1432–1443) vs `REVERSE_ENGINEERING.md` `qht` update | 🟡 field order = on-screen order (NC/Off/**Adaptive/Transparency**) | code: 1 NC, 2 Off, **3 Transparency, 4 Adaptive** (`qht.java:31`, `hgj.java`) — unreconciled, not mentioned in `PROTOCOL.md` | disagreement → capture (Q11) |
| F-7 | ADR-016 item 5 / `PROTOCOL.md` §7 | the drop fires "the instant" the second bud is seated | `CAP-062`: 0.5–3 s after seating, after a Buds `DISC` of DLCI 0x02 (§10); `CAP-047` one seating without a drop | imprecise wording, no status change |
| F-8 | `PROTOCOL.md` §4.3 Option F "401/403, 397/403" | counts | re-derived 410/417 with a ±3 s nearest-frame pairing (§6); same conclusion, same mismatch set + `CAP-040` 6084 | consistent (method note only) |

Checked and consistent (no finding): `ARCHITECTURE.md` §3.1 battery row and `ConnectionScreen.kt:289–291` ("Refresh … Left/Right", Case by push);
ADR-036's field list vs `Maestro.READABLE_FIELDS` (16/18 — reads unblocked, not built, as documented); ADR-043 Update vs `BudsRepositoryImpl.kt:487–502`;
ADR-044 vs `SessionReopener.kt`; `PROJECT.md` "[ ] In-ear detection status" (unchecked, correct); `TODO.md` Group AY items (AY-1/2/3) match `0048` §9.

## 17. Summary

| Q | Verdict | First next step | Effort |
|---|---|---|---|
| Q1 in ears | needs evidence first (both buds); per bud not possible (no signal) | AY-3 one-bud-in-an-ear test | S (after test) |
| Q2 Case / Refresh | possible with a maintainer decision (re-subscribe on Refresh); no Case while no bud in the case — firmware | your build answer; D-2 | S |
| Q3 balance | possible with a maintainer decision (write ADR); read possible now | D-3 | M |
| Q4 mono | possible with a maintainer decision (write ADR); read possible now | D-3 | S |
| Q5 times | possible now (app work) | your build answer; close+reopen the claim on Refresh | S |
| Q6 disconnect docked | not an app issue (Buds end the link; Android reconnects with the lid open) | none (optional hint) | XS |
| Q7 preset layout | possible now | your layout choice | XS |
| Q8 Find, case closed | not possible (no link; FHN needs Google-held owner keys) | — (AY-1 covers lid open) | — |
| Q9 ring Case | not possible (no local command; FHN needs owner keys; ADR-027) | — | — |
| Q10 conversation detection | possible with a maintainer decision (label promotion + write ADR) | D-1(a), D-3 | S |
| Q11 touch / ANC modes | touch + press-and-hold: maintainer decision; ANC-mode list: needs evidence first | Group AR re-run (F-6) | M |

## 18. Proposed order of work

1. **Decisions (no data needed):** F-1/F-2/F-3/F-4 status corrections; D-3 (settings write ADR for 17, 19, 22, 4, 7); D-2 (Refresh re-subscribe).
2. **FEATURE session:** Q7 layout (XS) → Q5 Refresh fix (S) → read-only settings (ADR-036: 2, 4, 7, 17, 19, 22) → balance/mono on the EQ tab (Q3/Q4) →
   conversation detection + touch controls/press-and-hold (Q10/Q11 part) → Q2 re-subscribe (if D-2).
3. **CAPTURE session (Group AY, already planned, + additions):** AY-1 (Find docked), AY-2 (EQ docked), AY-3 (wear), the new Q2 re-subscribe and Q5
   double-Refresh steps, balance/mono/conversation/touch hardware checks, and the Group AR ANC-list re-run (F-6).

## 19. Drafts (proposals — **not** decisions; recorded for the maintainer)

- **D-1 (PROTOCOL.md status updates, needs approval per `AGENTS.md` §6):** (a) §4.5.1: "OFF direction captured: `CAP-019` frame 1720 `4:{22:0}`, filmed
  ON→OFF at 07:36:28–31, RESPONSE OK 1731 — the UI's 'Conversation detection' switch writes field 22 in both directions" and promote the label
  equivalence to 🟢; (b) §4.5.3: "OFF direction captured: `CAP-020` frame 1995 `4:{4:0}`, filmed ON→OFF at 07:47:19–21, RESPONSE OK 2005"; (c) §4.5.7/§6:
  "persistence across a reconnect: 🟢 — three chains (…)"; (d) §6: "`FE2C1238…` = Find Hub Network 'Beacon actions' (official FHN page, fetched
  2026-09-26)"; (e) §4.5.3: add F-6's code reading next to the 🟡 on-screen-order reading. Plus the matching `CAP-019`/`CAP-020` EVENT-NOTES/FINDINGS
  rewrites.
- **D-2 (ADR-043 Update):** "*Refresh battery* additionally sends one `SubscribeRuntimeInfo` REQUEST (same bytes as at Connect) while the session is
  open; nothing else new. If the Buds do not answer within 1 s, nothing is retried and the Case stays as it was (with its time). Hardware-verify before
  relying on it."
- **D-3 (new the next free ADR number (045 per `id_registry.csv`, not registered — a proposal), settings writes):** "`WriteSetting` is unblocked for `qhr` fields 17 (Volume balance, `sint32` −100…+100, +100 = Left), 19 (Mono
  audio, 0/1), 22 (Conversation detection, 0/1 — after D-1(a)), 4 (Use touch controls, 0/1) and 7 (press-and-hold action per bud, `7{1|2:{4:{1:5|6}}}`),
  each byte-identical to the official app's captured writes, on the announced channel (ADR-034), through the Safe-Mode gate (ADR-042), one write per user
  action, success only on the empty `RESPONSE` with status OK; the current value is read at Connect (ADR-036) and shown with its time. Field 12 stays
  read/write-gated until its bit mapping is captured."
- **Capture additions for Group AY (the next free capture number per `id_registry.csv` (063), not registered):** AY-13 Refresh twice within 1 s (Q5); AY-14 both docked, idle
  2 min, Refresh → re-subscribe answered? (Q2, only after D-2); AR-style: ANC-list Left/Right + bit mapping (Q11).

## 20. Checkpoint answers (Phase B, `AskUserQuestion`, chat 2026-09-26 — recorded verbatim)

- **Build** (Q2/Q5): **"0048-build (huidig)"**. ⇒ Q2: the Case line behaves as designed in that build (reported only while a bud charges; kept as
  "last seen" only after a report in this app process); Q5: the Case time never moves on Refresh (by design) and a bud's time stays when the Refresh
  reuses a still-open claim (§9) — the Q5 fix goes into the next FEATURE session.
- **EQ layout** (Q7): **"2 rijen: 3 + 2 (Recommended)"** — preview `[HEAVY BASS] [LIGHT BASS] [BALANCED]` / `[VOCAL BOOST] [CLARITY]`.
- **Besluiten**: **"D-1 statuscorrecties", "D-3 settings-write ADR", "D-2 Refresh herabonneert"** (all three). The question stated that an approved
  text is recorded here as input only unless the maintainer explicitly asks to record it now; that was not asked. ⇒ No `PROTOCOL.md`/`DECISIONS.md`
  change and no `id_registry.csv` row in this session; the texts of §19 (D-1, D-2, D-3) are the input for the next session, which records them per
  `AGENTS.md` §6 (with a short re-confirmation in its own chat, per the project's "approvals in chat" practice) and registers that new ADR.
- **Volgorde**: **"Eerst FEATURE, dan CAPTURE (Recommended)"** — next a FEATURE session (record D-1/D-2/D-3; Q7 layout; Q5 Refresh fix; read-only
  settings under ADR-036; balance, mono, conversation detection, touch controls, press-and-hold per bud under that new ADR; Refresh re-subscribe under the
  ADR-043 Update), then one CAPTURE session (Group AY + the §19 additions + the Group AR ANC-list re-run).

## 21. External sources (fetched 2026-09-26)

| URL | Quoted text | Used for |
|---|---|---|
| developers.google.com/nearby/fast-pair/specifications/extensions/fmdn | "Beacon actions \| No \| Read, write and notify \| `FE2C1238-8366-4814-8EB0-01DE32100BEA`"; "Octet 0: uint8 Ring operation - A bitmask having the following values: Bit 1 (0x01): Ring right \| Bit 2 (0x02): Ring left \| Bit 3 (0x04): Ring case"; "Ring key: Defined as `SHA256(ephemeral identity key \|\| 0x02)`, truncated to the first 8 bytes."; "Ephemeral identity key (EIK): A 32-byte key chosen at random by the Seeker when performing the FHN provisioning process."; "The Provider should continue ringing regardless of the connection status of the Seeker that requested ringing."; "After provisioning, the Provider is expected to advertise FHN frames at least once every 2 seconds."; "Find Hub network requires location services and Bluetooth to be turned on. Requires cell service or internet connection." | Q8, Q9, F-4 |
| developers.google.com/nearby/fast-pair/specifications/extensions/deviceaction | "0x00 (0b00000000): All components should stop ringing"; "0x01 (0b00000001): Ring right, stop ringing left"; "0x02 (0b00000010): Ring left, stop ringing right"; "0x03 (0b00000011): Ring both left and right" | Q9 |
| bluetooth.com … Core-54 … controller-error-codes.html | section headings "2.19. Remote User Terminated Connection (0x13)" and "2.22. Connection Terminated by Local Host (0x16)" (the rendered page returned headings only, no body text) | Q6 (the wire-side proof is the local-vs-remote count in §10) |
| developer.android.com/develop/ui/compose/layouts/flow | "`FlowRow` and `FlowColumn` are composables that are similar to `Row` and `Column`, but differ in that items flow into the next line when the container runs out of space." | Q7 |

Quotes are from the fetch tool's page rendering (a summarising model): the FHN and Device Action quotes are exact spec-table strings; the EIK/ring-key
formulas should be re-read on the page itself before being cited in `PROTOCOL.md`.

## 22. Open items

- `python3 scripts/ensure_footers.py` → footer added; `python3 scripts/lint_docs.py` → **exit 0**, "clean" (the dead-reference list it prints is
  informational, historical session logs only). Scratchpad file names are written without backticks so the linter does not treat them as repo files.
- Nothing is committed yet: that waits for the maintainer (prompt task 10). Nothing is hardware-verified; no file under `android/` was changed.
- Open for the next sessions: §19 D-1/D-2/D-3 (approved as input, §20), F-5 (`ARCHITECTURE.md:153` "6 presets"), F-6 (Group AR), F-7 (wording).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0051_FEATURE_RESULT_2026_09_26.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0051_FEATURE_RESULT_2026_09_26
