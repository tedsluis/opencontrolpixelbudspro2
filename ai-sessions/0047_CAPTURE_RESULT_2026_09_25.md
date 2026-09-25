# 0047_CAPTURE_RESULT_2026_09_25.md — Full analysis of CAP-062 (the APP_TESTPLAN run of the 0046 build) and a list of app improvements, without changing the app

**Number:** 0047
**Category:** CAPTURE
**Date:** 2026-09-25
**Title:** Fully analyse `CAP-062` (video, HCI snoop log, app debug export, app logcat, system log), recorded while following `APP_TESTPLAN.md`; record it as `CAP-062-EVENT-NOTES.md`/`CAP-062-FINDINGS.md`; correlate the app's behaviour with each protocol; answer the maintainer's five observations with evidence; and produce a prioritised list of app improvements — no change to the app itself
**Status:** complete

> All phases done (the session ran over one usage-limit break and resumed from the Progress block). No file under `android/` was changed. Nothing
> is committed: that waits for the maintainer's go-ahead (task 22). No subagent was used.

## Progress (final)

- Phase 0: files identified, every film frame scanned for privacy, clock offsets measured at both ends; maintainer answers §13; folder moved with
  identical checksums, mode 644, LFS attributes confirmed.
- Phases A–D: `CAP-062-EVENT-NOTES.md`, `CAP-062-FINDINGS.md`.
- Phase E: improvement list §11. Phase F: checkpoint §13; approved changes applied (§5); `lint_docs.py` exit 0.

## 1. Plain-language answers

1. **Find My Buds in the case.** Nobody tried it in this run: whenever both buds were in the case, the Buds had already closed the app's
   connection (and then the Bluetooth link), so the Find buttons were greyed out. Whether a bud that sits in the case rings is still unknown — the
   next capture should dock both buds, reconnect and ring once (I-9). Outside the case, Find worked: Left and Right rang (you can hear it on the
   film) and stopped on Stop — even after Disconnect.
2. **"The app keeps getting disconnected."** It is the Buds, not Android: every time a bud goes in or out of the case, or in or out of an ear,
   the Buds close the app's channel (7 of the 14 session ends) while Bluetooth stays connected. Google's own apps simply reconnect; this app waited
   for a Connect tap because that was your 2026-09-20 decision. You have now decided (ADR-044) that the app reconnects by itself while it is on
   screen — to be built next.
3. **"Refused (not allowed in the current state)" for ANC.** That is the Buds' own answer (Fast Pair NAK reason 2), not an app error — and it
   happens whenever the Buds say "no ANC mode is switchable right now", which in this run was whenever they were **not in your ears** (also lying on
   the table, not only in the case). The official app does not offer ANC then either. The app cannot override the firmware; it should grey the
   buttons out and say "only while you wear the Buds" (I-3). **EQ** was never refused — all 7 EQ changes were accepted (and in 51 captures no EQ write
   was ever refused); EQ in the case was not tried.
4. **Which bud is in / out.** The Buds already send it to the app, on the app's own channel, every time a bud is seated or removed — the app
   ignored it. Now a FACT (401 of 403 packets in 45 captures) and allowed by the ADR-043 Update; to be shown per bud (I-4). It needs the session to be
   open (answer 2).
5. **Bud in the case and its battery.** The Case level (60 %) did show whenever the app was connected with a bud in the case; it disappears when no
   bud is in the case because the Buds stop sending it. With ADR-044 (reconnect), I-4 (per bud) and I-5 (keep the last Case value with its time) the
   screen will show it; I-5 is allowed by the ADR-043 Update.
- **What works:** pairing, Connect/Disconnect, firmware line (no Safe Mode), ANC while worn (6/6 accepted), the Quick Settings tile, EQ
  read/write/persistence, Find, Left/Right battery with charging, the Case %, notification. **What does not:** the ring notice after Disconnect
  (I4), a stale "Android still shows the Buds connected" message, ANC buttons not greyed out, reconnection. **Next session:** all four groups you
  chose (§14).

## 2. Reading done before acting

`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md` (full), `PROTOCOL.md` (full), `DECISIONS.md` (ADR-001 … ADR-043, full), `TODO.md`
(priority list, Phase 3–5, debt, open questions in full; Phase 1/2 history read quickly — disclosed), `AI_SESSION_LOG_PROCEDURE.md`,
`ai-sessions/INDEX.md`, `0046` RESULT (full), `APP_TESTPLAN.md` (full), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` (§1–§3, §4.2, Group AW, §8, §9 rows — not the
older Groups line by line, disclosed), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (the Test-ID rows used), `id_registry.csv` rows used, `CAP-061` EVENT-NOTES and
FINDINGS (full), `CAP-050` (layout). Kotlin read where a finding touches it: `RuntimeInfo.kt`, `BudsRepositoryImpl.kt` (loss handling, `setAncMode`,
`disconnect`/`connect`, runtime-info routing), `FindMyBudsScreen.kt`, `MessageStreamReply.kt`, `ConnectionScreen.kt` (messages).

## 3. `ai-sessions/0046` R1–R8 verdicts

R1 🟢 confirmed · R2 🟢 confirmed (6 ACK worn, 10 NAK not worn, both shown correctly) · R3 🟢 confirmed (7/7 OK, persisted; audibility ⚪) · R4 🟢
confirmed (heard until Stop) · R5 🟢 confirmed (60 % = DLCI 0x08; no app `SABM` on 0x08) · R6 🟢 · R7 🟢 · R8 ⚪ not run. Detail: FINDINGS §1.

## 4. `APP_TESTPLAN.md` results

✅ A3 A4 B1 B3 C1–C4 C6 C7 C9 C10 D1 D2 E1–E7 F4 F8 G1 G2 G4 H1 H3 H4 H6 I1–I3 J1–J3 L1 L2 · ❌ I4 · ⚠️ A1 A2 B2 C8 F1–F3 G3 H2 · not run A5 A6 B4 C5
F5–F7 H5 J4 K1–K5 L3. Per-test evidence: FINDINGS §10 and the EVENT-NOTES timeline.

## 5. What was changed (documentation only)

`captures/CAP-062-…-Group_AX/` (moved + `CAP-062-EVENT-NOTES.md`, `CAP-062-FINDINGS.md`); `DECISIONS.md` (ADR-024 Update, ADR-043 Update, **ADR-044**);
`PROTOCOL.md` (§4.1 note, §4.3 Option F per-bud 🟢 FACT + hardware-verified, §6 two notes, §8 row); `ARCHITECTURE.md` §6.0b (ADR-044 pointer);
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` (Group AX, Capture Index row); `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (12 evidence cells); `id_registry.csv` (CAP-062
analyzed, ADR-044); `TODO.md`; `CHANGELOG.md`; `README.md` (status); `ai-sessions/INDEX.md`. `python3 scripts/ensure_footers.py`, `python3
scripts/lint_docs.py` → exit 0.

## 6. Phase 0 — identification (2026-09-25)

| File | sha256 | Facts |
|---|---|---|
| `CAP-062-btsnoop_hci.log` | `249a9713…6b8ebf` | 11,456 packets, "Packet size limit: (not set)", H4 with linux header, 06:38:41.863250–07:00:15.793623 (1293.93 s); `cap_len≠len` 0; one out-of-order pair (frame 4907, −0.28 ms) |
| `CAP-062-debug-export.log` | `f6e300f7…81b3dd` | 731 lines, 06:38:50.494–06:56:44.568 (phone local) |
| `CAP-062-OpenControl-for-Pixel-Buds-log-091e23cb54d0.txt` | `8cbcbb79…232207` | 256 lines, logcat, `google/tegu/tegu:17/CP2A.260805.005` |
| `CAP-062-recording.mp4` | `1d9a2c49…6cc7c` | 1115.19 s, H.264 1280×720 rotation −90, 33,273 frames (avg 29.84 fps), AAC audio, creation 2026-09-25T04:57:11Z |
| `CAP-062-System-log-8bfd96877cca.txt` | `8168fefe…062888` | 71,598 lines |

`id_registry.csv`: exactly one `planned` row for `CAP-062` (line 106). Last Group letter used: AW (`CAP-061`) → next is **AX**.

### 6a. Privacy scan (every frame)

Method: all 33,311 decoded frames (`ffmpeg … -vf crop=720:200:0:1080 -f rawvideo -pix_fmt gray`, numpy): the overlay line (rows 1249–1271, x 390–715)
holds ≥ 1,451 bright pixels in every frame (timestamp `Sep 25, 2026 HH:MM:SS` only); the band above it and the bottom-left corner have bright pixels
in 9 short windows only, each viewed: screen glare / a finger (t = 63.0, 114.3, 139.9, 230.4, 255.7, 390.0, 556.6, 685.2, 1048.4 s). **No address
overlay.** Personal data on screen (5 fps review): notification shade with legible third-party names/messages at t ≈ 189.3–190.9, 592.3–593.3,
636.4–637.2, 689–690, 929.9–950.3 (longest, scrolled), 966.5–967.3 and 969.3–978.3, 1000–1009, 1015–1022 s; a heads-up message from a named
contact at t ≈ 1036–1040 s; the Quick Settings media card (a third-party video title) at t = 0–4 s and whenever QS is open (594–688, 998–999,
1062–1077 s); the device name "Pixel Buds Pro 2 van Ted" (as in `CAP-061`).

Clock: overlay = 06:38:36.2 + t; the phone's status-bar minute flips 0.7 s (±0.1) after the overlay's matching second at both t ≈ 83 s and t ≈ 1103 s
→ **phone clock = overlay + 0.7 s, no drift**. Logcat/system log (UTC) = export − 2 h 00 min 00.0 s (e.g. `wm_on_activity_result` 04:39:39.614 vs export
06:39:39.610 "picker dismissed").

## 11. Improvement list (Phase E — proposals only; nothing under `android/` was changed)

Ordered by value to the maintainer's five observations, then by effort. "Wire" = what the change sends; every item is tested with real
`CAP-062` bytes in a unit test plus a hardware step with an HCI bracket.

| # | Problem (evidence) | Change | Layer / files | Wire | Rules touched | Risk | Test | Effort |
|---|---|---|---|---|---|---|---|---|
| **I-1** | Obs 2/5: 7 of 14 sessions ended by a Buds-side `DISC` on DLCI 0x02 at a wear/dock change, ACL up (FINDINGS §3) | **Re-open the MAESTRO session automatically** while the app is visible *and* Android reports the Buds connected: after a Buds-side `DISC` (not after the user's Disconnect), and when Android's link comes back (e.g. lid opened after a re-dock). Bounded: one attempt per event, 1–2 s after the event, no loop; a user Disconnect switches it off until the next Connect tap | `:data` `BudsRepositoryImpl` (loss handler), `:app` (visibility + `OsConnectionObserver` events), `ARCHITECTURE.md` §6/§6.0b | same as a Connect tap (DLCI 0x02 `SABM`, `ReadSetting 4:16`, `SubscribeRuntimeInfo`; 0x04 snapshot claim) | **new ADR superseding the 2026-09-20 decision** "no automatic session opening" and amending §6 "user-initiated reconnection only" (it stays event-driven, foreground-only) | re-claims DLCI 0x04 on each reopen (Play services contention, ADR-032) — the reopen could skip the 0x04 snapshot and rely on the stream (I-8) | unit: FakeBudsTransport emits a peer `DISC` → one reconnect, none after a user Disconnect, none while backgrounded; HW: remove a bud → HCI shows Buds `DISC` 0x02 then app `SABM` 0x02 within ≈ 2 s, screen stays "ready" | M |
| **I-2** | Obs 2 "always connected, also in the background" | **CDM device presence** (`startObservingDevicePresence(ObservingDevicePresenceRequest)` on the existing association) + a `CompanionDeviceService` that starts the `connectedDevice` foreground service and the session when the Buds connect | `:hardware` new service, manifest (`BIND_COMPANION_DEVICE_SERVICE`, `REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND`), `:app` | as I-1, but also in the background | **new ADR**; `AGENTS.md` §2 (new permission → justification), ARCHITECTURE §6.0a/§6.0b | Android-16 API (deprecated older one); GrapheneOS: the system log shows CDM presence working for Fitbit (`CDM_DevicePresenceProcessor … Exempting package`, 04:38:43) — untested for this app; battery/privacy cost of a background session | HW: app swiped away, open the case → FGS starts, HCI `SABM` 0x02 | L |
| **I-3** | Obs 3: 10/10 NAK `0x02` with Settable `0x00` (not worn) (FINDINGS §2) | Keep the last `Notify` Settable byte; while `0x00` show "ANC can only be changed while you wear the Buds" and disable the ANC buttons/tile action (a Refresh re-reads it); never send a `Set` then | `:data` (expose settable), `:ui` AncScreen, `AncTileService` | less: no `Set` while `00` | ADR-024 Update (wording "not worn", 🟡) — UI only, no new wire | a stale `00` right after an open (ADR-024 settling note) → the button re-enables on the next `Notify` | unit with frames 6000/8706; HW: buds on the table → buttons disabled, no `08 12` in the HCI log | S |
| **I-4** | Obs 4/5: the stream reports each bud's in-case state; the app drops it (FINDINGS §4, 401/403) | Decode 6.2/6.3 field 2 (and/or 7.1/7.2) as "Left/Right in the case", show "In the case" / "Out of the case" per bud, updated on every push, with its time | `:data` `RuntimeInfo.kt`, `BatteryStatus`/domain model, `:ui` ConnectionScreen | nothing new (the app already subscribes) | **FACT promotion** of the field meanings + an ADR-043 Update/new ADR unblocking the decode (ADR-043 item 2 limits the decode to 6.1) | "in the case" vs "charging" (empty-case case untested) → word it "charging in the case" or mark 🟡 | unit with frames 3760/4845/7033 (real bytes); HW: seat/remove each bud → the line follows within ≈ 1 s without a claim | S |
| **I-5** | Obs 5: Case shows "unavailable" as soon as no bud is docked (7118) | Keep the last Case value with "last seen HH:MM:SS (no bud in the case)" instead of "unavailable" | `:data` RuntimeInfoCase handling, `:ui` copy | nothing | ADR-043 item 2 ("absent entry → unavailable") needs an Update; `AGENTS.md` §5 allows a *last-seen* value with its time, never a fabricated one | a stale value read as current → always show the time and "last seen" | unit: 6.1 present then absent → Known(isStale=true) | S |
| **I-6** | I4 fails (FINDINGS §7.1; ring heard until Stop) | Do not clear `ringingTarget` in `disconnect()`; clear it on Stop ACK only; `connect()` keeps it so the notice says "tap Stop" after reconnect | `:data` `BudsRepositoryImpl.kt:424,469` | nothing | none (bug fix, APP_TESTPLAN I4) | a ring that stopped by itself still shows the notice → wording "may still be ringing" | unit; HW I4 | XS |
| **I-7** | Stale loss wording (FINDINGS §7.2) | Choose the loss text from the *next* Android-link reading (or re-render when it changes); name "the Buds closed the channel (a bud was put in/taken out)" for a peer `DISC` with the link up | `:data` `SessionDiagnostics`, `:ui` ConnectionScreen | nothing | none | — | unit with the 06:42:35 ordering | S |
| **I-8** | 8 *Refresh battery* claims of DLCI 0x04 (ADR-032 contention) | Use the stream for "charging/in the case"; keep the 0x04 claim only for the percentages when asked | `:data` | fewer 0x04 claims | ADR-032/033 unchanged | percentages still need 0x04 | HW: Refresh with the stream active | S |
| **I-9** | Obs 1 unknown (FINDINGS §5) | **Experiment, not code:** both buds docked, lid open, Connect, Ring Left/Stop, HCI + audio | capture (Group AY) | `04 01 00 01 02/00` (ADR-011, nothing new) | none | none | ACK vs NAK `ff 02 00 03 <reason> 04 01` | XS |
| **I-10** | Test-plan gaps (A5, B4, C5, F5–F7, H5, J4, K1–K5, L3, R8) | Re-run those steps; record Play services' *Nearby devices* state (P4) and the build hash (P1) | capture | — | — | — | — | S |

**The five observations, answered in one line each:**
1. **Find in the case** — *still unknown (experiment I-9)*; in practice it fails today because the session is closed whenever both buds are
   docked (Buds `DISC` + ADR-016 ACL drop) — fixing that is I-1/I-2 (new maintainer decision).
2. **Always connected** — *possible with a new maintainer decision*: an ADR superseding "no automatic session opening" (I-1 foreground-only,
   cheaper; I-2 background via CDM presence, costs a service, a permission and GrapheneOS risk).
3. **ANC/EQ refused in the case** — ANC: *not possible, the Buds' firmware refuses it* (NAK `0x02` whenever Settable is `0x00`, 10/10, and that
   includes buds lying outside the case); the app should say so up front (I-3). EQ: *not reproduced* (0 EQ refusals in 51 captures); untested
   while docked.
4. **Which bud is in/out** — *possible with a new maintainer decision* (FACT promotion + decode unblock, I-4): the Buds already push it on the
   app's own channel; it also needs the session to be open (I-1).
5. **Bud in the case and its battery** — *possible with new maintainer decisions*: I-1 (session), I-4 (per bud), I-5 (last-seen Case); the Case
   % itself already works whenever the session is open with a bud docked (06:43:32, 06:46:12, 06:46:55).

## 12. External sources (fetched 2026-09-25)

| URL | Quoted text | Used for |
|---|---|---|
| developers.google.com/nearby/fast-pair/specifications/extensions/acknowledgement | "For a NAK, the reason should also be included as the first byte of additional data."; reason `0x02` "Not allowed due to current state" | FINDINGS §2 |
| developers.google.com/nearby/fast-pair/specifications/extensions/hearablecontrols | "Settable toggles: Any or all of the UI toggle bits above may also be set here, to indicate which are currently enabled."; "When a Provider receives a 'Set ANC state' message, it should Acknowledge, and Notify ANC state to all connected Seekers." (the page states no NAK condition) | FINDINGS §2 |
| developer.android.com/develop/connectivity/bluetooth/companion-device-pairing | "The service is bound when the companion device is within BLE range or connected using Bluetooth."; "Starting with Android 16 (API level 36), `CompanionDeviceManager.startObservingDevicePresence(String)` and `CompanionDeviceService.onDeviceAppeared()` are deprecated." | I-2 |
| developer.android.com/develop/background-work/services/fgs/restrictions-bg-start | "Your app uses the Companion Device Manager and declares the `REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND` permission or the `REQUEST_COMPANION_RUN_IN_BACKGROUND` permission." (an exemption from the background-start restriction) | I-2 |

## 13. Maintainer decisions (this chat, 2026-09-25, `AskUserQuestion`; previews carried the exact texts now in the files)

- **Phase 0:** Privacy → **"Keep it unblurred"**; Migration → **"Yes, as proposed (Recommended)"**.
- **Checkpoint:**
  - FACT stream → **"Promote to 🟢 FACT (Recommended)"** → `PROTOCOL.md` §4.3 Option F (per-bud fields).
  - Settable → **"ADR-024 Update + 🟡 (Recommended)"** → `DECISIONS.md` ADR-024 Update, `PROTOCOL.md` §4.1 note.
  - Session → **"ADR-044 foreground re-open (Recommended)"**.
  - Next build → **"I-3 + I-6 + I-7 (bug/UX)", "I-4 + I-5 (per-bud + last Case)", "I-1 (and I-8)", "I-9 + I-10 (next capture)"** (all four).
  - ADRs → **"Record both (Recommended)"** → ADR-044 Accepted; ADR-043 Update.


## 14. Draft outline for the next FEATURE prompt (0048)

1. Reading order as usual; this RESULT §11/§13, `CAP-062-FINDINGS.md`, ADR-024/043/044 Updates.
2. **Bug/UX (no new wire):** I-6 ring notice survives Disconnect (unit test + APP_TESTPLAN I4); I-7 loss wording from the current Android link;
   I-3 ANC disabled with "only while you wear the Buds" while the claim's `Notify` reads Settable `0x00` (fixtures: frames 6000, 8706).
3. **Stream (ADR-043 Update):** I-4 per-bud "charging in the case" from 6.2/6.3 field 2 and 7.1/7.2 (fixtures: frames 3760, 4845, 7033); I-5 dated
   last-seen Case; I-8 charging from the stream without extra DLCI 0x04 claims.
4. **ADR-044:** I-1 foreground re-open after a Buds-side `DISC`, on Android link-up and on resume; one attempt per event; off after a user
   Disconnect; unit tests with `FakeBudsTransport`.
5. Build/test/lint gate; mutation checks; re-test list with HCI brackets, including Group AY (I-9, the EQ-while-docked and one-bud-in-an-ear
   experiments, the APP_TESTPLAN steps not run in `CAP-062`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0047_CAPTURE_RESULT_2026_09_25.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0047_CAPTURE_RESULT_2026_09_25
