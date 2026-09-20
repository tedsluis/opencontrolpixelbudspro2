# 0042_FEATURE_RESULT_2026_09_20.md — `LOGS-001` analysed end to end, the first hardware run validated, the mirror/pairing defects fixed, HFP / auto-connect / Case-battery / extra-feature proposals prepared for your decision

**Number:** 0042
**Category:** FEATURE
**Date:** 2026-09-20
**Title:** Full analysis of the `LOGS-001` evidence (two camera films → timestamped events file; HCI snoop log, system log, app debug export, app logcat and screenshots correlated with it), validation of the app against it, fixes for what the evidence proved broken, and decision-ready proposals
**Status:** awaiting maintainer sign-off

> **Why "awaiting maintainer sign-off":** Phases A–D are done — you answered the checkpoint in chat (§11) and everything you approved is built — but **nothing is hardware-verified**: every fix and feature is unit-tested and mutation-checked, its effect on the phone is a HYPOTHESIS with a re-test step (§12). Evidence is cited by timestamp, frame number and log line — never by evidence file name (standing rule).
> **Approvals (chat, 2026-09-20):** HFP → remove; session → mirror only; Case → ADR + on-demand claim; features → firmware line, in-case line, Find state, **ANC tile only** (no EQ preset export/import) and the DLCI 0x02 read-only ADR; capture → keep local; promotions → three (§4). ADR-035 and ADR-036 were written from the drafted text after those answers (`AGENTS.md` §6).

## 0. Plain-language answers to your eleven requests

1. **Videos → events file.** Done. Both recordings are *camera* films (a second device with a burned-in clock, not screen recordings), 40 s and 286 s. Every one of the 163 sampled frames was read; the events file now holds 45 timeline rows (pairing, permission prompts, Connect, EQ presets, Find taps, ANC taps, buds out of the case, Disconnect, Android's Bluetooth panel, Spotify, screen off), the metadata, the integrity pre-flight and the checklists. Clocks are **measured**: the phone runs ≈ 1.3 s ahead of the film's clock (§2).
2. **Logs, HCI, screenshots.** Done: one time-ordered correlation (HCI ↔ app log ↔ system log ↔ film) with frame numbers, commands and raw hex is in `DESKRESEARCH_FINDINGS.md` (2026-09-20, second entry). Headline: the app's log and the HCI log agree to ≤ 70 ms, so every app line can be pinned to a frame.
3. **Does the app work as intended?** *Mostly, yes:* permissions, pairing (the bond itself), Connect (4/4), ANC (4/4 mode taps, the Buds' answer 0.26 s later), EQ read and write (16/16 answered, and the writes persisted in the Buds), Find (heard on the film's microphone), battery incl. charging (matches the film in both states), notification, Debug export. *Two things are broken:* **the Android-state card was wrong for minutes** (it said "Android doesn't show the Buds as connected (yet)" and later "not connected" while Android's own panel said "Actief") and **a successful pairing was reported as a timeout 45 s later**. Both are fixed (§6). Table: §3.
4. **Open questions.** Answered with evidence in §4 (the events file's three questions, ADR-034's open items, DLCI 0x08, the `0041` re-test list). The one that stays open: *why* the Buds closed both RFCOMM channels at 17:21:16 — nothing at HCI level precedes it.
5. **HFP.** Removed, on your decision at the checkpoint: the Buds send `AT+BIEV=2,100` seven times on the wire, the app's receiver got **zero** vendor events, and the value is one earbud's, never the Case. (You accepted the recommendation without the `dumpsys` output of the 0041 rule.) The wire facts and ADR-015/023 stay; `AGENTS.md` §5's HFP paragraph would need a maintainer edit (§7).
6. **What went wrong.** Two lists in §5: the capture procedure (e.g. system log exported 2.5–7 min after the drops, no `dumpsys`, film clock ≠ phone clock, Play services' permission not recorded) with a tightened checklist (§12), and the app (13 items; 9 fixed).
7. **Extra functionality.** Ranked list with evidence level, ADR need, effort, risk, value in §8, plus the consolidated DLCI 0x02 read-only ADR — **written as ADR-036** after your approval (nothing implemented).
8. **Improvements.** §6: the broadcast flag that starved the mirror, "unknown" no longer shown as "not connected", bond outcome read from the stack, re-entry guard for pairing, the trigger in the log, why-did-the-session-end lines, a 1000-line buffer.
9. **Case battery.** Established from the evidence (§9): the Case is **97 %** on the wire in DLCI 0x08 `Group 0x0e Code 0x01` (index 3, ADR-014) — pushed by the Buds within ≈ 165 ms of the channel opening; DLCI 0x04's `b3` is `ff` in all 60 frames; the BLE advertisement has **no** readable battery field. You chose the DLCI 0x08 route: **ADR-035 written and implemented** — a short receive-only claim on Connect and on *Refresh battery*, a value the Buds did not mark fresh shown as "last seen". Whether the Buds push the Case level *without* the phone-side request is the open hardware question (§9, §12).
10. **Checkpoint.** Done in chat (§11): all six questions answered.
11. **Android connected ⇒ app connected.** Two readings (§10). The mirror (reading a) was broken by a receiver flag and by rendering "unknown" as a negative — fixed. You chose **mirror only**: no automatic session opening, no rule change, no ADR; re-test §12 a decides whether that is enough.

## 1. Authorisation and what was NOT done

| Item | State |
|---|---|
| ADRs | **ADR-035** (Case battery, DLCI 0x08 on-demand receive-only claim) and **ADR-036** (DLCI 0x02 read-only `ReadSetting` unblock, nothing implemented) written **after** your chat approval, process notes citing it; updates to ADR-024 and ADR-034 recorded the promotions. Auto-connect: no ADR (declined) |
| FACT promotions | **Three, all approved in chat:** fresh client needs no opening message (`PROTOCOL.md` §2.2a, ADR-034 update); write acknowledgement + persistence (§4.2, ADR-034 update); dock-state byte second confirmation (§4.1, ADR-024 update). Everything else stays labelled 🟡/🔴 in `DESKRESEARCH_FINDINGS.md` |
| `AGENTS.md` | Untouched (maintainer-only file). §5's HFP paragraph would need a maintainer edit now that HFP is removed — text proposed in §7 |
| HFP removal, Case battery, firmware/in-case/Find lines, ANC tile | **Built** (§6b); auto-connect **not built** (declined); EQ preset export/import **not built** (declined); read-only settings UI **not built** (ADR-036 only) |
| Evidence files | Everything under `android/logs/` stays gitignored and uncommitted; no committed file names one |

## 2. Evidence, clocks, coverage (task 1–2)

- **Films:** H.264 720×1280 (rotation −90 metadata; `ffprobe` shows 1280×720), variable frame rate (90 kHz timebase, ≈ 30 fps), AAC 44.1 kHz stereo = the camera's **microphone**. Recording 1: 17:16:59 → 17:17:39 (40.1 s, 1195 frames); recording 2: 17:17:44.5 → 17:22:30 (286.5 s, 8547 frames); 5 s gap. The films carry a **street address** in the overlay — deliberately copied nowhere.
- **Clock mapping (measured):** the phone's status-bar clock flips 17:18→17:19 when the overlay reads 17:18:58.6–58.8 (5 fps crops; repeated at 17:17→18 and 17:19→20) ⇒ **phone ≈ overlay + 1.3 s (±0.5 s)**; `overlay(t) = 17:17:44.5 + t` for recording 2. btsnoop and logcat are UTC; the debug export is phone-local (UTC+2); HCI vs. export agree to ≤ 70 ms after the exact 2 h shift.
- **HCI:** 6 156 packets, 449.56 s, not truncated (`Packet size limit: (not set)`, 0 `cap_len ≠ len`). Buds handles: classic ACL 0x000b, LE 0x0041.
- **Coverage gaps (evidence that is missing or inconsistent):** (1) the 16:56 screenshots belong to an **earlier app process**; the app logcat holds only its lifecycle lines (no `OpenControlBuds` lines) — the 500-line debug buffer of that process is gone; (2) the app logcat export contains only lifecycle lines and the app tag for 15:23–15:24 UTC — the level/tag filter dropped the rest; (3) the system log ends 15:24:37 UTC and has **no Bluetooth-stack lines** for 17:18–17:22 local (only framework/Telecom/CDM/gms lines); (4) no `dumpsys bluetooth_manager` / `companiondevice` output; (5) the Play-services *Nearby devices* state and the app build/commit were not recorded; (6) Debug mode was off; (7) the tap that disconnected the Buds in Android's panel (drop #2) is not on film; (8) the buds in the ears are not filmed.

## 3. Validation table (task 3) — verdict, evidence, re-test

| Feature / behaviour | Verdict | Evidence (phone-local time, HCI frame, film) | Re-test needed |
|---|---|---|---|
| Runtime-permission flow | **works** (not-asked → prompt → allow) | export 17:17:25.334 "showing the system prompt", 17:17:31.126 both `true`; film 17:17:24–30 (two system prompts, Toestaan) | deny / blocked paths (0041 §9a) |
| CDM pairing: picker / association | **works** | system log association id 36 added 15:17:47.992Z, export "association created" 17:17:48.011; film dialog 17:17:45 | — |
| Pairing: duplicate handling | **broken → fixed** | two `associate()` 82 ms apart (export 17:17:41.507/.589; system log `processNewAssociationRequest()` 15:17:41.521/.590); the raw CDM error shown under the button while the picker was open (film 17:17:45) | tap Pair once and quickly twice (§12 b) |
| Pairing: the bond | **works on the phone, reported wrong → fixed** | `createBond()` 17:17:48.024, HCI Authentication Complete frame 691 = 17:17:49.121; export "bond timed out" 17:18:33.046 (no `bond state` line at all) | §12 b |
| Already-bonded handling | **works** | export 17:17:50.445/.450 "reusing existing association", "already bonded — no createBond()"; card "Paired — not connected" 17:17:49 | — |
| Connect (attempts/timing) | **works** (4/4) | Disconnected→Ready in 80 ms, 290 ms, 1.41 s (after a full Android disconnect: includes the ACL page), 293 ms; channel 0x02 attempt 1 every time | — |
| Session stability | **works**; 3 ends, none unexplained by the app | 17:17:54.5–17:19:45.7 = 111 s with 12 claims; 17:20:24–17:21:16 = 52 s (§4) | idle run ≥ 10 min |
| Message Stream claim/release (ADR-032) | **works with contention** | first attempt failed **4/4** while Play services held DLCI 4 (17:17:54.7, 18:13.4, 18:35.2, 18:49.8; 192/290/280/77 ms), then **0/11**; claims 1.5–4.6 s | with Play services' permission denied |
| ANC set / refresh | **works** | 4/4 mode taps: Set `08 12 … e8 e8 08/20/40/80` (frames 2768…2982) → Notify 0.26 s later; UI followed (film 17:19:21–33); Refresh = `08 11` Get claims | audibility in the ears (you) |
| EQ read at Connect | **works** 4/4 | `ReadSetting` answered 4/4 (frames 1455, 3202, 3558, 4855); film's first EQ screen = the read | — |
| EQ write | **accepted and persisted; audibility unknown** | 12/12 `RESPONSE status OK` (ch 21 ×5, ch 19 ×7); the next connection's read returns the last write (`[-2,0,2,3,5]`); sliders followed | listen: change preset while music plays (§12 f) |
| Find My Buds | **works, audible on the film's microphone** | Buds ACK ×4 (frames 2310, 2491, 2506, 2600); 1.3 s tone bursts every 3.0 s at 17:18:38.9…47.9 (Left) and 17:18:55.0…61.0 (Right), ≈ 3.1 s after the command, continuing after the channel release, none after Stop 17:19:03.2 | UI shows nothing while ringing (§5 ii) |
| Battery L/R + charging | **works** | `e4 e4 ff` up to 17:19:03.2 (film "100% (charging)" — buds in the case), `64 64 ff` from 17:19:22 (film "100%" — buds out since ≈ 17:19:11); `b3` = `ff` ×60 → Case "unavailable" by design | mixed state (one bud out) |
| Android-state mirror | **broken → fixed (unverified)** | card wrong 17:17:53–17:19:43 and 17:19:45–17:19:59 while Android said "Actief" (film 17:19:51–55); observer log silent 17:17:31–17:20:10 although the unflagged receiver saw the broadcasts | §12 a |
| Foreground-service notification | **works** | film 17:20:39–45: "OpenControl for Pixel Buds · nu / Connected — ANC: TRANSPARENT" | that it disappears on disconnect |
| Debug tab / export | **works** | "Unidentified frames (30)→(94)", export 149 lines | — |
| Error messages shown | **partly misleading** | "The Maestro channel … Another app may have taken it over, or the Buds dropped it" after **you** disconnected in Android's panel (17:20:19); the false "Pairing failed: … More than one …" | wording (§5 ii) |

**UI vs. Android's panel:** the card contradicted Android for ≈ 110 s (17:17:53–17:19:43, "doesn't show connected" vs. HFP up since 17:17:50.757) and ≈ 14 s (17:19:45–17:19:59, "not connected" vs. "Actief" at 17:19:51), and in the earlier process (16:56 screenshots) for an unknown time.

## 4. Open questions answered (task 4)

Full evidence and commands: `DESKRESEARCH_FINDINGS.md` 2026-09-20 (second entry).

- **What precedes the Buds-side DISC (events file Q1)?** 🟢 FACT for the one instance (17:21:16.023, frames 4595/4597): **nothing** at HCI, L2CAP or ACL level; the Buds' DISC on DLCI 0x02 is the first event, on DLCI 0x04 5 ms later; 0.34 s of silence before it; ACL and A2DP stay up. Whether it is a second host, a Buds timer, or the 0.56 s re-claim: 🔴 OPEN (experiments in §12).
- **Idle / periodic / second host (Q2)?** Not exercised (one continuous tour); the phone's HCI log cannot show a second host — 🔴 OPEN.
- **Does `07 34` expect an answer (Q3)?** 🟢 FACT here: 20 frames, one on each of the 19 DLCI 4 opens (25–406 ms after the `SABM`) plus one extra; the phone sent **0** ACKs; claims that never answered it lasted up to 4.6 s undisturbed → not a simple "answer or die within seconds" timer. Play services replies `07 10`/`06 01` 7 ms later; the app sends none.
- **Was the 89 s deny-mode drop the same?** 🟡 HYPOTHESIS: same class (both channels closed by the Buds within ms, audio up); 🔴 cause unknown; here 6 ms apart, 1.56 s after an app claim, session 52 s old, Play services not holding DLCI 4.
- **Which drop was caused by whom?** #1 (17:19:45.72) **the user's Disconnect tap** — phone-sent DISC frame 3068, finger on the button in the film; #2 (17:20:15.9) **the user's tap in Android's Bluetooth panel** — system log SystemUI dialog interaction 15:20:14.392Z → Telecom HFP disconnecting → phone DISC on HFP (frame 3358) → ACL Disconnect Complete **reason 0x13** (frame 3390) (⚪ the tap itself is off-camera); #3 (17:21:16.0) **the Buds closing only RFCOMM**. Play services: no drop attributable (it only touched DLCI 4/8/10). Android's Bluetooth auto-off: `BluetoothAutoOff … shouldScheduleAlarm: true, delayMillis: 0` was logged at 15:20:14.749Z after drop #2 (GrapheneOS schedules its auto-off when nothing is connected) but the app's Connect at 17:20:22.6 re-established the link 8 s later; it did not cause anything.
- **ADR-034's open items:** *fresh client needs no opening message* — 🟢 FACT on 4/4 connections (first request each time is `ReadSetting 4:16`, answered); *request/response matching* — writes and reads are strictly sequential, 16/16 answered in order, responses omit `call_id`; *channel↔address* — 21 (`00 4b`/`00 a5`) and 19 (`00 3b`/`80 a3`) only, no new pair, no 24/26; *write acknowledgement* — empty `RESPONSE`, status absent/OK, 12/12; *errors* — 36 pw_rpc packets, **0** `status ≠ OK`.
- **First unsolicited packet always the announcement?** 4/4: `GetSoftwareInfo` (`call_id 0xFFFFFFFF`) 22 / 102 / 30 / 58 ms after the `UA`.
- **DLCI 0x08?** Opened by the **phone side** (Play services; the app never opens it): `SABM` frames 1135, 3845, 4662; closed 17:19:35.06, 17:21:16.27, 17:21:46.28. Carries **19** `0e 01` pushes; Case = `0x61` = **97 %** in all 19.
- **`0041` re-test items:** (a) permissions — confirmed for allow, deny paths untested; (b) pairing — picker → paired ✓, already-bonded ✓, **duplicate/bond reporting refuted → fixed**, not-in-pairing-mode untested; (c) card lines — right in some states, **refuted** in others; (d) mirroring — **refuted** ("card lags / never updates" — exactly its refutation clause); (e) battery — confirmed (both regimes), mixed untested; (f) EQ — read/write/persist confirmed, audibility open; (g) HFP — diagnostic worked (6 `CONNECTION_STATE_CHANGED`, 0 vendor), `dumpsys` missing; (h) evidence — supplied. *Nothing regressed:* session stayed up (111 s / 52 s), ANC works, Find rings until Stop.

## 5. What went wrong (task 5)

**(i) The capture procedure** — what reduced the evidence value:
1. The events file was still blank; the template's run plan (idle / periodic / second host) was not followed — a functionality tour was done instead, so the events file's own three questions are only partly answerable.
2. The 16:56 screenshots come from an earlier process with no log.
3. The system log was exported 2.5 min after the last drop (7 min after the first): no Bluetooth-stack lines for the drops.
4. No `dumpsys bluetooth_manager`/`companiondevice` output (blocks closing 0041 §9g).
5. Film clock ≠ phone clock and the phone's status bar shows minutes only → the offset had to be reconstructed from clock flips (±0.5 s).
6. Debug mode off; build/commit and Play-services permission not recorded; the app logcat export dropped the app's own tag lines.
7. In-ear audio (ANC/EQ audibility) cannot be captured by a camera microphone; the tap that caused drop #2 and the buds in the ears are not on film.
8. Good: HCI logging enabled and Bluetooth toggled off/on **on film** (17:17:04–08, first packet 17:17:07.58); the ring buffer did not wrap; the film shows the case.

Concrete improvements (a short checklist for the next run is in §12; `CAPTURE_BLUETOOTH_HCI_SNOOP.md` is left as is — it is a maintainer procedure, a proposal to fold §12 in is in `TODO.md`).

**(ii) The app** — each defect, severity, evidence, fix:

| # | Defect | Sev. | Evidence | Fix |
|---|---|---|---|---|
| D1 | Android-link receiver got **no** system broadcasts (`RECEIVER_NOT_EXPORTED`) | high | §3 | `RECEIVER_EXPORTED` (protected broadcasts) + re-read on resume / bond change / session change / proxy bind — **built** |
| D2 | An **unknown** link rendered as a negative ("Android doesn't show…", "Paired — not connected") | high | film 17:17:53–17:19:57 | new `AndroidLine.UNKNOWN` line — **built, tested** |
| D3 | Successful bond reported as a 45 s timeout | high | frame 691 vs. export 17:18:33 | outcome read from the stack at the end of the wait; receiver `EXPORTED`; timeout cancelled when the flow ends — **built, tested** |
| D4 | Double association request + raw CDM error shown | med | §3 | re-entry guard (`AtomicBoolean` + UI state), `AlreadyInProgress` classification, dismissed picker resets — **built, tested** |
| D5 | `Android link (): …` — trigger missing, 3 lines per bind (46 of 149 lines) | low | export | one line per **change** with its trigger — **built, tested** |
| D6 | Bond-timeout coroutine not cancelled | low | export | cancelled in `awaitClose`; a newer pairing cancels the older one — **built** |
| D7 | Session ends unexplained in the log (user Disconnect left no trace) | med | 17:19:45.720 | `Session ended by the user's Disconnect tap`, `Session lost: channel …; last inbound frame …` — **built, tested** |
| D8 | 500-line buffer | low | export | 1000 — **built** |
| D9 | Message after a **user** disconnect in Android's panel says "Another app may have taken it over…" | low | 17:20:19 | **not fixed** (needs the link state at loss time; would come with the auto-connect work) — `TODO.md` |
| D10 | Find shows no "ringing" state | low | film 17:18:33–17:19:03 | not fixed — proposal (§8) |
| D11 | Battery card shows no age of the reading | low | film 17:19:37 | not fixed — proposal |
| D12 | First claim collides with Play services' Fast Pair | known | ADR-032 | unchanged (4/4 then 0/11) |
| D13 | Picker dismissed → "waiting for you to pick…" forever | low | code | fixed with D4 |

## 6. What was built (Phase B) and verification (task 6, 7, 16)

- `:hardware` — `LinkEvaluation.transitionLine` (pure); `OsConnectionObserver.observe(refresh)` (flag `RECEIVER_EXPORTED`, refresh trigger, transition-only log); `BudsCompanionPairing` (`RECEIVER_EXPORTED`, `associationInFlight`, `cancelPendingAssociation()`, `timeoutJob`, bond state read at timeout); `PairingLogic.outcomeAtTimeout`, `classifyAssociationError`, `PairingFailure.AlreadyInProgress`; `BleLogger` buffer 1000.
- `:domain` — `AndroidLine.UNKNOWN`, `statusCard` maps every `AndroidLink` explicitly.
- `:data` — `SessionDiagnostics` (`lossLine`, `userDisconnectLine`), `BudsRepositoryImpl` logs both and remembers the last inbound channel/time.
- `:ui`/`:app` — text for the unknown line; `MainActivity.startPairing` (re-entry guard, picker-cancel reset, superseding bond job), `linkRefresh` emitted on resume, on bonded-device change and on session change.
- **No new permission, no service, no network, no scan.** The two `EXPORTED` receivers listen to protected system actions only.

### 6b. Phase D — what you approved, built (task 15)

- **HFP removed:** `HfpBatteryReader`, `HfpAtParser` (+ test), the `RepositoryModule` wire, `BudsRepositoryImpl`'s `hfpBatteryPercent`, `BatteryStatus.hfpEarbud`, the UI row; docs relabelled (`ARCHITECTURE.md` §1/§3.1/§4/§5a, `PROTOCOL.md` §4.3 Option C).
- **Case battery (ADR-035):** `Dlci.GSND_CONTROL` (0x08) in `CodecRouter`, `CaseBatteryFrameDecoder` (real bytes from frames 1211/2863/2928; index 3 only; fresh flag → `isStale`), `BudsSdpUuids.GSND_CONTROL`, `BudsRepositoryImpl.readCaseBattery` / `refreshBattery` (one mutex with the Message Stream claims, ≤ 2 s wait, release after 1 s, **nothing sent on DLCI 0x08**), DLCI 0x04's `b3` no longer overwrites the Case, `BatteryLevel.Known.isStale`, a *Refresh battery* button, "last seen" text, a specific error line.
- **Passive extras:** firmware line (`SoftwareInfo` reads only the firmware strings of the announcement — the serial-like field is never read or logged), "Both earbuds are in the case / At least one is out" (`DockState` from the `Notify`'s Settable-toggles, ADR-024), Find "Ringing: Left — tap Stop" (stays until Stop succeeds; the ring keeps sounding after the channel is released).
- **ANC tile:** `AncTileService` (Hilt), cycle ANC → Transparent → Adaptive → Off, only while the session is `Ready` (otherwise a tap opens the app), a self-drawn ring glyph, **no new `uses-permission`** (`BIND_QUICK_SETTINGS_TILE` is required of the binding caller). // TODO(verify): GrapheneOS behaviour.
- **ADR-036:** text only; no code.

```
cd android && ./gradlew --offline assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL
```
**1357 tests, 0 failures** (`:domain` 11 (+3), `:data` 1297 (+18), `:hardware` 49 (−7: `HfpAtParserTest` removed with its parser); Phase B alone was 1343, the session started from 1335). Lint: 0 errors, the same 2 pre-existing manifest warnings, no new suppressions (one new `ModifierParameter` warning was fixed). **Mutation checks (all caught, then restored):** (1) `outcomeAtTimeout` ignoring the stack's bond state → `PairingLogicTest` fails; (2) `AndroidLink.UNKNOWN → NOT_CONNECTED` in `statusCard` → 2 `DeviceStatusTest` tests fail; (3) the Case flag ignored (`isStale = false`) → 2 tests fail; (4) DLCI 0x04's `b3` allowed to overwrite the Case → the repository test fails. **Not unit-testable and therefore untested:** the receiver flag itself, `refresh` delivery, `MainActivity.startPairing` glue, the picker result callback, the tile service (Android-framework-only) — the re-test (§12 a, b, h) covers them.

## 7. HFP verdict and the removal (task 8, 13) — decided: removed

**Recommendation was: remove — done at your word (chat 2026-09-20)** `HfpBatteryReader` (keep every wire fact). Evidence: (1) **on the wire** the Buds send `AT+BIEV=2,100` seven times (frames 1216, 2624, 2631, 2685, 2694, 3077, 3097 — at connect and at the bud removals); (2) **in the app** the receiver, registered from 17:17:25.364, logged six `CONNECTION_STATE_CHANGED` and **no** vendor-specific event; (3) the value is one earbud's (ADR-015) — never the Case; (4) the app has a better source: DLCI 0x04 with the charging flag matched the film in both states; (5) maintenance: a receiver, a parser, a DI wire, a UI row, four docs.
*Value it could still add:* a fallback if DLCI 0x04 cannot be claimed — **but it delivers nothing to the app either way**, so it is no fallback. *Risk of removing:* another OEM might dispatch a vendor event for standard `AT+BIEV` — no evidence for it (this phone is the target; a hidden-API route is banned, `AGENTS.md` §3). *Flips if:* a device is shown to fire `ACTION_VENDOR_SPECIFIC_HEADSET_EVENT` for `AT+BIEV`.
**Unmet condition of your 0041 rule:** the `dumpsys bluetooth_manager | grep -i -B3 -A3 battery` output was not supplied. I judge the two-sided evidence above sufficient; you may waive it or supply it.
**The patch that was applied (description):** delete `hardware/…/HfpBatteryReader.kt`, `hardware/…/hfp/HfpAtParser.kt` and `HfpAtParserTest.kt`; `app/…/di/RepositoryModule.kt` (drop the `HfpBatteryReader(context).observeBievBatteryPercent(…)` argument and import); `data/…/BudsRepositoryImpl.kt` (drop the `hfpBatteryPercent` constructor parameter, its collector and the class comment); `domain/…/BatteryStatus.kt` (drop `hfpEarbud`); `ui/…/ConnectionScreen.kt` (drop the "One earbud (side unknown)" row and its condition); `data/…/BudsRepositoryImplTest.kt` (drop the `hfpBatteryPercent` builder argument and the test at ~669); docs: `PROTOCOL.md` §4.3 Option C and `ARCHITECTURE.md` §3.1 table row / §4 item 3 / §5a row relabelled **"wire-confirmed, not app-consumable"**; `TODO.md`; `CHANGELOG.md`. Proposed `AGENTS.md` §5 edit (maintainer-only file): mark the HFP mechanism "wire-confirmed, not consumable by the app on Android 14+; not implemented". ADR-015/023 stay untouched.

## 8. Extra functionality (task 9)

Ranked by value ÷ (effort + gate). Evidence levels from `PROTOCOL.md`; "ADR" = what `AGENTS.md` §6 needs.

| # | Feature | What it does | Evidence today | ADR / approval | Effort | Risk | Value |
|---|---|---|---|---|---|---|---|
| 1 | **Device info card** (firmware, Safe-Mode display, `ARCHITECTURE.md` §8.1) | Shows `release_5.203` and the component strings the Buds push at connect | 🟢 FACT: the unsolicited `GetSoftwareInfo` (4/4 connections; string decodes to `release_5.203`); which of the three entries is which bud/case: 🟡 | none for the firmware string (passive decode of an already-decoded message); serial-number labelling would need HYPOTHESIS labels | S | none (no send) | med |
| 2 | **Buds in case / out** (dock state) | Shows "in the case" / "out" | 🟢 ADR-024 (`Notify` third byte `00` = both seated, `e8` = otherwise) **and confirmed again here**: `01 e8 00 20` at 17:17:55.47 (both seated, film) vs `01 e8 e8 08` at 17:19:22.31 (out) | none (ADR-024 is FACT; passive) | S | none | med |
| 3 | **Find: ringing indicator + Left/Right state** | UI state while a ring runs; auto-clear | protocol 🟢 (ADR-011) + audio evidence above | none | S | none | med |
| 4 | **Case battery** | Case % | 🟢 identity (ADR-014), value seen (97 %) | **needs ADR** (§9) | M | GMS contention on DLCI 0x08 | high |
| 5 | **Battery reading age** | "last read 12 s ago" (`ARCHITECTURE.md` §4) | n/a | none | S | none | low |
| 6 | **Read-only settings display** (Mono, Volume balance, Conversation detect, in-ear detection, …) | `ReadSetting` for qhr fields already at FACT identity | 🟢 ADR-019/026 identity; read semantics 🟢 ADR-034 (EQ only) | **consolidated ADR (draft below)** | M | low (reads only) | med |
| 7 | **Settings writes** (touch & hold, head gestures, mono, balance, case sounds, multipoint) | change them | 🟢/🟡 per field (§4.5) | per-field ADR after (6) | L | medium (semantics per field; guard by firmware check) | high |
| 8 | **`SubscribeToSettingsChanges`** | live sync when changed by the Buds' buttons/another host | 🟡 (streaming RPC never decoded) | ADR | M | unknown stream semantics | med |
| 9 | **EQ preset export/import (local JSON)**, quick-settings ANC tile | local-only helpers (`AGENTS.md` §1 alternative) | n/a | none (tile: check GrapheneOS FGS rules) | S–M | low | med |
| 10 | **Multipoint management** | which hosts | 🟡 | ADR | L | medium | low (no evidence) |

**ADR-036 (drafted here, written into `DECISIONS.md` after your approval): DLCI 0x02 read-only `ReadSetting` for the fields already at FACT identity.** *Context:* `ARCHITECTURE.md` §5a's consolidated-ADR proposal; ADR-034 unblocked `ReadSetting` for fields 16/18 only. *Decision (proposed):* unblock `ReadSetting 4:N` (no writes, no `SubscribeToSettingsChanges`) for `qhr` fields 4, 7, 11, 15, 17, 19, 22, 27, 28 and 2 — each **only** for the value semantics already 🟢 in ADR-013/019/026 and `PROTOCOL.md` §4.5; a field whose semantics are not FACT is read and shown **raw in the Debug tab only**; requests use the announced channel and the address table of ADR-034; sequential, ≤ 3 s timeout, no retry storm; firmware string must be `release_5.203` else Safe Mode (`ARCHITECTURE.md` §8.1). *Consequences:* `Maestro.readSettingRequest` learns the field list; UI gets a read-only "Settings" card; each write is a later, separate ADR. *Approval:* yours, in chat 2026-09-20 — given.

## 9. Case battery (task 10)

- **(a) DLCI 0x04 `Group 0x03 Code 0x03`:** `b3` = `ff` in **all 60** frames of this capture (`e4 e4 ff`, `64 64 ff`) — 🟢 FACT: the Case cannot come from here (ADR-033's decoder correctly says unavailable).
- **(b) DLCI 0x08 `Group 0x0e Code 0x01` index 3:** 🟢 identity (ADR-014). In this capture the phone side (Play services) opened DLCI 0x08 (frames 1135, 3845, 4662); the Buds pushed 19 messages, the first 165 ms after the channel opened (frame 1211, `… 0a 06 08 61 10 01 18 03 …` = **Case 97 %**), identical in all 19. The phone sent `0e 04` (a zero-length "get") before the first push at every open; whether the Buds push **without** that request is 🟡 (later pushes came unrequested, after ANC changes and bud removals). What opening it costs: a **third RFCOMM connection** (service "GSND CONTROL", UUID `f8d1fbe4-7966-4334-8024-ff96c9330e15`, `PROTOCOL.md` §2.3), contention with Play services (it held DLCI 8 for ≈ 105 s, 50 s and 29 s stretches; a failing second connect closes the incumbent's port — ADR-032's finding), no new permission; `AGENTS.md` §6 needs an **ADR** for acting on the unmapped Group/Code (the request, if needed) — the codec itself is implementable.
- **(c) BLE advertisement:** 741 `0xFE2C` advertisements, 315 from the Buds' *public* (= bonded) address — so ADR-006's `ScanFilter` on the bonded address is possible — but the 54 distinct payloads carry account-key filter + salt + a 4-byte type-6 field; **no** `0x33`/`0x34` battery marker (`PROTOCOL.md` §4.3 Option A's `CAP-011` non-match confirmed a second time). Nothing to decode without an unknown decoding step — 🔴.
- **(d) Anything else:** Android's Bluetooth panel and the Buds' HFP show one battery value (100 %), not the Case. The LE GATT discovery in this log (787 ATT frames on handle 0x0041; `tshark -Y "btatt.opcode==0x11" -T fields -e btatt.uuid16`, frames 704, 768, 5275) lists services 0x1800, 0x1801, 0x1849, 0x184c, 0x1855, 0x1858, 0x185b, 0xfcf1, 0xfef3 and (second connection) 0x180a — **no Battery Service 0x180f, no 0x2a19** (0 matches), so `PROTOCOL.md` §4.3 Option D has nothing to read on this firmware.
- **Result:** no route existed that needed no new approval, so I asked; you approved the DLCI 0x08 route and **ADR-035 was written and implemented**. Its text (as drafted here, now in `DECISIONS.md`): *"DLCI 0x08 is claimed on demand for the Case battery"* — like ADR-032: opened by the user's own action (Connect or a "refresh battery" tap), reads the `0e 01` push (sending `0e 04` only if the hardware test shows a push is not unsolicited), decodes **only** index 3 (Left/Right stay from DLCI 0x04), released after ≤ 1.5 s; a failed claim shows "Case: unavailable" with its reason; new decoder with real bytes from frame 1211 as fixture; `ARCHITECTURE.md` §5a row; `TODO`. (The alternative — do not implement — was declined.)

## 10. "Android says connected ⇒ the app shows / is connected" (task 12)

**(a) Status mirroring** (needs no ADR). It was built in `0041` and the first hardware run **refuted** it (D1, D2). Fixed in §6; precise remaining gap: none known in the evidence (after the observer's restart at 17:20:36 it was correct); whether "connected" should also show when only audio profiles are up: **yes** — that is what it does (A2DP/HFP/LE-Audio proxies), and Android's own panel says the same.
**(b) Automatic session opening** — options:

| | Change | Pros | Cons / cost |
|---|---|---|---|
| (iii) mirror only (Recommended) | nothing beyond (a) | no rule change; the fix is not yet hardware-verified; **a Connect tap also pages the Buds when Android has disconnected** (HCI `Create Connection` frame 3419 at the app's tap) | one tap |
| (i) foreground auto-open | when the app is visible, Android reports CONNECTED, the session is closed and the user has not just tapped Disconnect: `connect()` once per Android-connected episode | saves a tap; no permission/service | rule change (`ARCHITECTURE.md` §6 "user-initiated reconnection only", §6.0a "started by a user-initiated connect", §6.0b deferred (a)); ADR-032 item 2 "the trigger is always a user tap" must be amended for the Connect-time claim (agent detail: skip the DLCI 4 snapshot claim in auto mode); a user Disconnect must stick |
| (ii) CDM presence + service | `CompanionDeviceService` on the existing association, a `connectedDevice` FGS from the callback | connects in the background | manifest service + at least one new permission (`REQUEST_OBSERVE_COMPANION_DEVICE_PRESENCE`, `AGENTS.md` §2 minimal set), Android 14 background-start limits, GrapheneOS behaviour 🟡 untried, battery budget (`ARCHITECTURE.md` §6.1), Play-services contention on every connect; churn in this capture: 1 ACL up by the Buds/pairing, 1 ACL up by the app, 1 full disconnect in 7 min |

**Draft ADR text for (i) (declined at the checkpoint — not written, kept for reference):** *"The MAESTRO session may be opened without a tap when the app is visible and Android reports the bonded Buds connected."* Decision: (1) supersedes `ARCHITECTURE.md` §6's "user-initiated reconnection only" **for this one case**; (2) trigger: `ON_START`/`ON_RESUME` while `AndroidLink.CONNECTED`, session `Disconnected`, no user-Disconnect since the last Android-connected episode, at most once per episode, never from the background; (3) ADR-032 stands for DLCI 0x04 — an auto-opened session claims **no** Message Stream channel at Connect; ANC/battery values appear after the user's first tap or Refresh; (4) the foreground service starts as today (bound to `ConnectionState`); (5) failure shows the specific error, no retry loop. **Implementation plan:** a pure `AutoOpenPolicy` in `:domain` (episode + user-disconnect flags, unit-tested), one collector in `MainActivity`, `BudsRepository.connect(claimSnapshot = false)`.

## 11. Checkpoint — asked and answered in chat (task 14)

| Question | Your answer (2026-09-20) | Done |
|---|---|---|
| (i) HFP | **Verwijderen** | removed (§6b, §7) |
| (ii) Session connecting | **Alleen spiegelen** | nothing built; no ADR; §12 a is the test |
| (iii) Case battery | **ADR schrijven + on-demand claim** | ADR-035 + implementation (§6b, §9) |
| (iv) Features | firmware-kaart + in-case-indicator + Find-status **and** the DLCI 0x02 read-only ADR; free text: *"Maak alleen een ANC-tegel, geen EQ-preset export/import en start met bouwen"* | those four built, ADR-036 written, EQ export/import not built |
| (v) HCI log | **Lokaal houden** | no `CAP-NNN`; findings stay in `DESKRESEARCH_FINDINGS.md` |
| (vi) Promotions | all three offered: *ReadSetting geen openingsbericht*, *write-ack + persisteert*, *dockstatus tweede bevestiging* | promoted with ADR-034/ADR-024 updates and `PROTOCOL.md` update bullets |

## 12. Re-test instructions and the tightened capture checklist (task 18)

Turn **Debug mode** on for the run; keep the phone's clock visible **with seconds** in the same shot as the case; **narrate** what you hear/see (the film's microphone records your voice).
- **(a) Mirror (D1/D2, the critical one).** Bond, then take a bud out of the case and back — the card must follow Android's panel within ~2 s **without leaving the app**; after pairing, before any tap: "Connected to this phone (Android)". The export should show `Android link: … (trigger: CONNECTION_STATE_CHANGED …)` lines. *Refuted if* the card lags > 5 s or the export has no `Android link:` line after a bond/connect.
- **(b) Pairing.** Tap **Pair a device** once (then, on another try, twice fast): one picker, no "More than one…" text; after Allow, "Paired with …" in ≈ 1–2 s, **no** "bond timed out"; export shows `bond state BONDED` or "no bond broadcast … but the stack reports BONDED".
- **(c) Session-end lines.** Tap Disconnect → `Session ended by the user's Disconnect tap (was Ready)`; disconnect in Android's panel → `Session lost: channel 0x02 closed (…); last inbound frame … ms earlier`.
- **(d) UNKNOWN wording.** Start the app with Bluetooth permission just granted: "Paired — Android's connection state isn't known (yet)" must never say "not connected" before the observer answered.
- **(e) Drop hunt (LOGS-001's own questions).** Three runs, each until the Buds close RFCOMM or 10 min: idle after Connect (no taps); one ANC tap per minute; the same with a second host playing audio. Note wall-clock of each tap and of the drop.
- **(f) EQ audibility.** While music plays: Heavy bass → Balanced; say aloud when you hear it change.
- **(g) HFP.** Nothing to test any more (removed). `adb shell dumpsys bluetooth_manager | grep -i -B3 -A3 battery` would only document why.
- **(h) Case battery (ADR-035) — the important new test.** Connect with the buds **in the case, lid open**: the Case line should show a percentage within ~2 s (no "last seen"); take both buds out and tap **Refresh battery**: the Case shows a percentage marked "— last seen" or the specific message ("The Buds didn't report the Case level…"). Export the debug log: `RFCOMM channel 0x08 connected …` or `Case battery not read: Timeout/ChannelUnavailable`. *If the Buds push nothing without the phone-side `0e 04`, the result is "Timeout" every time — then tell me and the next step is an ADR to send `0e 04`.* Also with Play services holding DLCI 0x08 a first attempt may fail once (`ChannelUnavailable`).
- **(i) Extras.** Firmware line shows `release_5.203`; "Both earbuds are in the case…" flips when you take a bud out and tap Refresh (only as fresh as the last claim); Find: after **Ring Left** the screen says "Ringing: Left earbud — tap Stop" until Stop.
- **(j) ANC tile.** Add the "ANC" tile to Quick Settings: connected → each tap steps ANC → Transparent → Adaptive → Off; not connected → "Open the app" and the tap opens the app. *Refuted if* it never appears, or a tap connects/starts something in the background.
- **Checklist:** events file filled *during* the run · build/commit written down · Play-services *Nearby devices* state written down · HCI snoop ON → Bluetooth off/on **on film** · screen recorder **and** a camera in one place with a clock showing seconds · export **system log, debug log and app logcat (all tags) within 1 minute of the drop** · `dumpsys bluetooth_manager` and `dumpsys companiondevice` after the run · case and buds in frame, in-ear moments announced aloud · idle / periodic / second-host runs in the template's order.

## 13. Documentation updated

`DECISIONS.md` (ADR-035, ADR-036, updates to ADR-024 and ADR-034), `id_registry.csv` (ADR-035/036), `PROTOCOL.md` (§2.2a, §4.1, §4.2 update bullets, Option C and Option E notes, change-log row), `ARCHITECTURE.md` (§1 diagram, §3.1, §4, §5a, §6.0b), `DESKRESEARCH_FINDINGS.md` (2026-09-20, second entry), `TODO.md`, `CHANGELOG.md`, `ai-sessions/INDEX.md`, this file and its prompt (Status), `ai-sessions/0041_FEATURE_RESULT_2026_09_20.md` (Status, per `AI_SESSION_LOG_PROCEDURE.md` §4a — its §7 proposals 1–2 were decided in the `0042` checkpoint above, cited from the chat). **Not** changed: `AGENTS.md` (maintainer-only).

## Phase D — what was implemented

Exactly what you approved (§11), nothing else: see §6b for the code, §13 for the documents. **Kept as proposals (unapproved or declined):** automatic session opening (declined), CDM presence (declined), EQ preset export/import (declined), the read-only settings display (only its ADR), any request on DLCI 0x08, settings writes, `SubscribeToSettingsChanges`, multipoint.

## Final summary

The first hardware run showed the app works where it mattered — permissions, pairing, Connect, ANC, EQ read/write (accepted and persisted), Find (heard), battery with charging — and exposed two real defects: the Android-state card was starved of system broadcasts by a non-exported receiver and rendered "unknown" as "not connected" for minutes, and a bond that succeeded in 1.1 s was reported as a timeout. Both are fixed with tests (1343 pass; two guards mutation-checked), and every session end is now explained in the log — which also settled that two of the three drops were the maintainer's own taps and only one (17:21:16, both RFCOMM channels closed by the Buds, ACL up) remains unexplained. After your answers: HFP is removed (wire-only), the Case is read from DLCI 0x08 by a receive-only on-demand claim (ADR-035 — whether the Buds push it unrequested is the open hardware question), the app now shows firmware, dock state and a Find "ringing" state, an ANC Quick Settings tile exists, ADR-036 is written but unimplemented, and the card only mirrors Android (no automatic connect). 1357 tests pass, lint clean; nothing is hardware-verified — §12 is the re-test.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0042_FEATURE_RESULT_2026_09_20.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0042_FEATURE_RESULT_2026_09_20
