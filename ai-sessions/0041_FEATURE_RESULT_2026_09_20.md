# 0041_FEATURE_RESULT_2026_09_20.md — In-app pairing and permissions fixed, Android's state mirrored, charging flag, EQ read path, HFP diagnostic

**Number:** 0041
**Category:** FEATURE
**Date:** 2026-09-20
**Title:** Make pairing work from inside the app, show the true paired/connected state (mirroring Android), implement the accepted battery charging flag, read the real EQ and find out why EQ writes were inaudible, add the HFP confirming diagnostic, and analyse the LOGS-001 capture
**Status:** awaiting maintainer sign-off

> **Why "awaiting maintainer sign-off":** everything requested that the environment allows is done and verified by the full suite (1335 tests, 0 failures, lint 0 errors),
> but (a) **nothing is hardware-verified** — every claim about behaviour on the phone is a HYPOTHESIS with a re-test step (§9); (b) `LOGS-001` held only the empty
> events skeleton, so task 10's HCI analysis was **not** done (§8); (c) the HFP removal, automatic session connecting and other proposals in §7 need your decision.
> Evidence is cited by timestamp and log line, never by evidence filename (standing rule).

## 0. Answers to your three questions (plain language)

**(a) Why does pairing from the app fail — and is it even built in?** It *is* built in (Companion Device Manager picker → classic bond), and the picker part
works: the system log shows CDM approving the Buds every time. It fails **afterwards, inside the app**: the app turned CDM's address (`04:00:6e:cf:6e:07`,
lower-case — exactly as CDM prints it) into a device with `BluetoothAdapter.getRemoteDevice(String)`, which only accepts **upper-case** hex and throws
`IllegalArgumentException`; the old code swallowed that into "null" and printed *"Could not resolve the selected device."* — before it ever tried to bond. Fixed
(upper-cased address, and `AssociationInfo.associatedDevice` preferred). Evidence level: code path 🟢 FACT (`MainActivity` → `deviceForAssociation`); the lower-case
CDM address 🟢 FACT (system log vs. the upper-case form Telecom prints for the same device); that `getRemoteDevice` rejects lower-case is the documented SDK contract, not
re-read from source in this environment (the SDK jar holds stubs only) and no log line shows the exception → 🟡 HYPOTHESIS (strong). The re-test (§9b) settles it.

**(b) If a device is already paired, why does the app not show that?** Because the app **never asked for the Bluetooth permission**. The manifest declares
`BLUETOOTH_CONNECT`, but a search finds no `requestPermissions`/`RequestPermission`/`checkSelfPermission` anywhere in the code (🟢 FACT). After you cleared the app's
data the grant was gone, `BluetoothAdapter.bondedDevices` threw `SecurityException`, and `bondedDevice()` turned that into "none" — indistinguishable from "nothing
paired". The 09:29 screenshot (app: "No Pixel Buds Pro 2 paired yet"; Android's panel one minute earlier: "Pixel Buds Pro 2 van Ted — Actief, Batterijniveau 100%") is
exactly that picture. That the grant was really missing in *that* run is 🟡 HYPOTHESIS: the supplied logs contain no permission line for the app. Fixed: a real permission
flow (§3), and a missing permission is now its **own** screen state.

**(c) If a device is already connected via Android's settings, why does the app not show that?** By design of the old screen, not (only) a bug: the Connection card printed
the app's *own* session state ("Disconnected") and at most a small grey hint about Android's state, whose source (`OsConnectionObserver`) started with `false` until its
profile proxies had bound (an initial-value race) and was never hardware-verified. For test 1 (permission granted) no screenshot or log was supplied, so which of these
applied there is 🔴 OPEN; both are fixed (§3): Android's state is now the **headline**, the app's session a secondary line. Note: at 09:27 the Buds were *not yet* connected
to the phone — Telecom logs the HFP link coming up at 07:28:21 UTC (09:28:21 local) — so the "Disconnected" screens at 09:27 were consistent with Android at that moment.

## 1. Decisions recorded, and how they were authorised

| Item | Authorisation |
|---|---|
| **ADR-033 update** — charging flag accepted (`0bSVVVVVVV`; `0xff` = unknown; Case not decoded) | Prompt `0041` §1.1, **and re-confirmed by the maintainer in the chat session** (see below) |
| **ADR-034** — `pw_rpc` identification + `ReadSetting` semantics promoted to 🟢 FACT; read-only read path unblocked (EQ 16/18); channel-mirroring rule | Prompt `0041` §1.2 (conditional on the evidence rule — checked first, §5), and re-confirmed in chat; the channel-mirroring rule was put to the maintainer explicitly as an agent proposal and approved |
| **ADR-032 update** — Find My Buds keeps ringing after the channel is released; session stays connected | Recorded as *maintainer-observed, hardware, 2026-09-19* (prompt §2), not as captured FACT |
| **ADR-031 update** — the "field-switch anomaly" is resolved by the charging flag | Follows from ADR-033's update |

The first attempt to write ADR-033/034 was paused by the environment's safety check, because the approvals then existed only in the prompt *file*. I asked, the maintainer
confirmed all three items in chat, and the ADRs were then written. Both new FACT promotions in `PROTOCOL.md` (§2.2a, §4.2, §4.3 Option B) cite the evidence commands.
Registered in `id_registry.csv` (ADR-034). No other FACT was promoted and no other ADR written.

## 2. Timeline reconstructed from the evidence (task 1)

**Occasion A — screenshots 2026-09-19 10:11–10:13 local; no log covers it.** 10:11 Android's Bluetooth panel lists only another saved device ("Niro"); the app says *No Pixel
Buds Pro 2 paired yet* and a system dialog asks to allow the app access to *Pixel Buds Pro 2 van Ted*; 10:12 after Allow: *Pairing failed: Could not resolve the selected
device.*; 10:13 the panel shows the Buds active, battery 100 %.

**Occasion B — screenshots 2026-09-20 09:27–09:29 local; app logcat and system log ending 07:31:47 UTC.**
- 09:27:45/09:27:55/09:28:00: ANC/EQ/Find tabs — "Not connected… Controls are disabled", *Connection: Disconnected*, EQ "unknown" (expected in that state).
- System log (UTC): CDM association requests at 07:25:18 (cancelled by the user, `setResultAndFinish() association=null` at 07:25:45), then **approved and stored at 07:25:48 (id 26),
  07:27:19 (27), 07:27:32 (28), 07:27:37 (29), 07:28:09 (30), 07:28:13 (31), 07:29:30 (32), 07:29:43 (33), 07:29:47 (34)** — every one preceded by `onDeviceFound() … 'Pixel Buds Pro
  2 van Ted' - New device` and `onAssociationApproved() macAddress=…`; further requests were cancelled at 07:29:24, 07:29:35, 07:30:31, 07:31:20. (Your prompt listed three; the log shows **nine** stored associations.)
- 07:28:21–07:28:52: Telecom logs the Buds' HFP link (`changed state to 1, 2`, later `3, 0`) — Android connecting the Buds; the 09:28:27 screenshot shows them active.
- 09:29:33: the app again says "No Pixel Buds Pro 2 paired yet" with the CDM dialog. Google Play services also tried to show its own Fast Pair half-sheet (blocked as a background activity launch at 07:25:38, 07:26:27, 07:27:09).
- App logcat: 873 lines, **zero lines from the app's own tag** — the pairing path logged nothing (§13.3), and the caught exception left no trace.
- No bond-state, `createBond` or permission lines exist in the retained window (Bluetooth-stack coverage is short).

**What the evidence cannot support:** no debug export or screenshot for test 1; no line proving the permission state; nothing about what the old Connection card showed in test 1; the system log's
stack lines cover ~1–3 minutes, so the bond attempt itself (which never happened, per (a)) cannot be seen.

## 3. Verification of the prompt's preliminary leads (§2 of the prompt)

| Lead | Result |
|---|---|
| 1. CDM succeeds, failure is after it | 🟢 Confirmed (nine associations, not three) |
| 2. `getRemoteDevice(mac.toString())` with lower-case | 🟢 the code and the lower-case CDM string are FACT; the SDK rejection is the documented contract (🟡, see 0(a)) |
| 3. No runtime permission request anywhere | 🟢 Confirmed by grep; its effect in *these* runs is 🟡 (no log line) |
| 4. Every attempt created a new association | 🟢 Confirmed (26–34); fixed: reuse + clean-up of same-address duplicates |
| 5. No `BleLogger` lines for pairing | 🟢 Confirmed (0 app-tag lines); fixed: every pairing step is logged, always-on, no address |
| 6. EQ leads | (a) refined: the write frames are byte-identical to the official ones *except* the byte the codec called the "correlation byte" — it is the pw_rpc **`channel_id`**, and the app sent **0** (no capture uses it; real ones are 19/21/24/26, chosen per connection). 🟡 strong HYPOTHESIS for the inaudibility. (b) Confirmed: nothing interpreted responses; the app logged only Debug-mode bytes. |

## 4. What was built

- **Pairing (task 4).** `PairingLogic` (pure, unit-tested): address normalisation; association reuse (`pickAssociation`) and clean-up of same-address duplicates (`staleAssociationIds`; decision:
  the newest is kept, own associations only, exact address only); bonded-device lookup by the association's address (name only as a fallback); bond-state mapping, "already bonded is success", bonding-in-progress waits;
  a bond that ends in `BOND_NONE` is classified — never bonding = *not in pairing mode*, bonding first = *rejected*; 45 s bond timeout (one bounded wait). `BudsCompanionPairing` is the thin Android glue and logs every
  step. `AssociationInfo.associatedDevice` (API 34, checked with `javap`) is preferred, the upper-cased address the fallback. Only CDM is used for discovery (`AGENTS.md` §7).
  New `PairingFailure` reasons each have their own message and a one-line hint (open the case, hold the pair button > 3 s).
- **Permissions (task 5).** Checked on start and on every resume; `RequestMultiplePermissions` for `BLUETOOTH_CONNECT` (GrapheneOS: *Nearby devices*) and `POST_NOTIFICATIONS`; states
  *not asked / denied / blocked* (`permissionStatus`, pure) with an "Open app settings" button for blocked; a missing permission is its own panel — never "not paired" (`AGENTS.md` §8). `POST_NOTIFICATIONS` missing only
  shows a small hint. No new permission, manifest unchanged.
- **Mirroring Android (tasks 5/6).** `DeviceStatus` (`:domain`) = Bluetooth off · permission missing · not paired · paired, not connected · connected to the phone (Android) · connecting · controlled by the app, derived by
  `deriveDeviceStatus`; `statusCard` decides the card for every Android × session combination (e.g. Android connected + session closed → "Connected to this phone (Android)" / "App control: not open yet" with a prominent **Connect**).
  `OsConnectionObserver` was rewritten and is **read-only and visibility-bound**: A2DP/HEADSET/LE_AUDIO proxies, re-read on profile/ACL/bond broadcasts, unregistered when the UI stops; it reports nothing until every proxy has bound
  (fixing the initial-value race), `UNKNOWN` without the permission, matches by normalised address, logs every transition (no address) and debounces a "not connected" for 1.5 s (`transformLatest`, no timer loop).
  **§2/§7 check (as the prompt asked):** no new permission, no service, no socket, no scan; it only reads the OS state of the already-bonded device.
- **Battery (task 7).** `BatteryFrameDecoder`: `0bSVVVVVVV` per earbud → `Known(V, S)` for `V ≤ 100`, else `Unavailable`; `b3` → Case always `Unavailable`. Real bytes as fixtures: `CAP-009` frame 1044 and the 221→228 sequence
  (frames 26852…28563), and the maintainer's `64 64 ff`, `60 5f ff`, `e4 e4 ff`, `dd dd ff`, `e4 64 ff`. The battery card says "(charging)".
- **EQ (task 8).** `Hdlc` now reads the pw_hdlc address as a one-terminated varint (control `0x03`), new `PwRpc` (RpcPacket codec, 65599 name hashes) and `Maestro` (service/method ids, `readSettingRequest` only for fields 16/18,
  channel↔address table). `EqFrame` carries `channelId` (no default). `CodecRouter` classifies Maestro packets into EQ values, the Buds' channel announcement, and write/read results/errors. `BudsRepositoryImpl`: the Connect sequence waits
  (≤ 3 s) for the announcement, then `ReadSetting 4:16`; `setEqGains` waits for the write's `RESPONSE` and reports `MaestroRejected`/`Timeout`/`MaestroChannelUnknown` instead of assuming success; field 18 never replaces the active EQ;
  the EQ screen shows the real EQ and says "couldn't read … <reason>" (with a *Read EQ again* button) only on failure. Always-on, payload-free log line for every inbound packet: `pw_rpc <TYPE> ch=<n> method=<name> status=<name>`.
- **HFP diagnostic (task 9).** `HfpBatteryReader` logs `HFP receiver registered (n actions)` and `HFP broadcast received: <action>` (name only); it additionally registers for the headset connection/audio state actions so a
  working receiver is visible even if `AT+BIEV` never arrives. The confusing "One earbud (side unknown)" row is hidden while its value is `Unavailable`. `HfpBatteryReader` and its wiring are **not** removed.
- **Script.** `scripts/pwrpc_decode.py`: frame numbers, `--eq`, `--channels`, and a corrected `RpcPacket.type` table (the old one had types 2/3/5 wrong).

## 5. Evidence check for the FACT promotion (task 8) — done first

Full write-up with commands and raw hex: `DESKRESEARCH_FINDINGS.md` entry 2026-09-20. Headlines: (1) framing correction — address `00 3b`, control `03`, and the "correlation byte" is the `channel_id` (control `03` in 304/304 packets of `CAP-015`);
(2) **three independent cross-capture chains agree** — the connect-time `ReadSetting` equals the last write of the *previous* capture: `CAP-005`→`CAP-006` (`[5.0,−4.1,0,0,0]`), `CAP-015`→`CAP-016`, `CAP-041`→`CAP-042` (`[−6]*5`); field 16 = "Vocal boost"
`[−1,0,4,2,0]` while field 18 = `[−6]*5` in 15 consecutive captures → 16 = active EQ, 18 = last saved custom EQ; (3) in **43 of 43** captures the channel of the Buds' first unsolicited packet is the channel the phone's Maestro requests use; four
channel↔address pairs are constant. Not checked: the value against a *screen recording*. Both promotions therefore went through as approved. What a fresh client must send first, request/response matching and the channel→address *derivation*
remain open and are handled conservatively (ADR-034 §4).

## 6. Verification

```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL
```
**1335 tests, 0 failures** (`:data` 1276, up 32 from 1244; `:hardware` 52, up 15 from 37; `:domain` 7, new; earlier total 1281). Lint: 0 errors; no new suppressions; the one `:ui` warning (`AutoboxingStateCreation`, EQ slider) was cleared with `mutableFloatStateOf`.
Tests use real captured bytes wherever the capture holds them (pw_hdlc frames from `CAP-015`/`CAP-036`, the real announcement header, the battery sequence) and scripted fakes for everything else (`FakeBudsTransport` gained an `onSent` hook to script the Buds' reply to a given frame).
**Mutation checks (all caught):** (1) address upper-casing removed → 5 `:hardware` tests fail; (2) debounce disabled → 2 fail; (3) charging flag ignored (`and 0x7F`) → 4 fail; (4) write reverted to channel 0 → 2 fail. Each was restored afterwards.
**Not unit-testable and therefore untested (Android-framework-only):** the Compose wording, the CDM service binding/picker, the permission launcher, `BudsRepositoryImpl.connect()`'s glue (needs a real `BluetoothDevice`; the read it launches, `launchInitialEqRead`, *is* tested),
the real broadcast delivery of `OsConnectionObserver` (its evaluation and debounce are tested). Fuzz tests exist for `PwRpc`, `EqFrameDecoder` (incl. NaN/oversize), `Hdlc` and `CodecRouter`.

## 7. Proposals and decisions still open (nothing below is done)

1. **HFP battery — decided by you 2026-09-20: remove it, but only after one confirming run.** *Why (plain language):* the Buds do send the HFP battery indicator (`AT+BIEV`) — that is a wire fact — but Android's Bluetooth stack consumes it internally and the standard
   indicator is not a "vendor-specific" command, so the broadcast our receiver listens for most likely never fires for it (a strong lead from reading AOSP's headset state machine through a summarising fetch, not a line-by-line reading; every session so far showed "Battery unavailable" on the HFP row).
   Battery now comes reliably from the Message Stream (with the charging state). *Verified/corrected:* consistent with `DESKRESEARCH_FINDINGS.md` 2026-09-19; nothing found contradicts it. **Exact follow-up task for the removal session:** remove `HfpBatteryReader`, its `RepositoryModule` wiring and the
   UI row; keep the protocol facts (ADR-015/023) and relabel `PROTOCOL.md` §4.3 / `ARCHITECTURE.md` §4 Option C "wire-confirmed, not app-consumable" — **only if** the confirming run's debug log shows **no** `HFP broadcast received: …VENDOR_SPECIFIC…` event and the `dumpsys` check (§9g) shows the OS consumed the value; if anything does arrive, report it and stop.
2. **Automatic session connecting — not built, needs your decision + an ADR.** Two variants (`ARCHITECTURE.md` §6.0b, `TODO.md`): (a) foreground-only auto-open when Android reports the Buds connected — costs a rule change ("user-initiated only"), no new permission/service; (b) background via CDM device-presence — costs a `CompanionDeviceService`, a
   manifest entry, a `connectedDevice` foreground service started from the callback within Android 14's background-start limits, and the most GrapheneOS risk (🟡, read from the API surface only).
3. Case battery via DLCI 0x08 (ADR-014), BLE Fast Pair battery advertisement (ADR-006), `SubscribeToSettingsChanges` and every other DLCI 0x02 setting — unchanged, unapproved, not implemented.
4. A consolidated ADR for the other DLCI 0x02 settings (`ARCHITECTURE.md` §5a) — unchanged.

## 8. `LOGS-001` and the `07 34` question (task 10)

`android/logs/LOGS-001/` contained only the empty events skeleton (no HCI snoop log, recording, app export or system log), so the analysis of what preceded the Buds' `DISC`/`DM` on DLCI 2/4 was **not** performed — nothing was invented; re-run the task when the files exist.
The cheap part *was* done on the existing captures: across **44 captures / 415** periodic `07 34` messages there are **0** Message Stream ACK (Group `0xFF`) frames on DLCI 0x04 — it is never ACKed; the phone (Play services) sends a Group-7 burst (`07 11`, `07 41`, `07 42`) ~30–55 ms after each one, whether that is an "answer" is 🟡 HYPOTHESIS (`DESKRESEARCH_FINDINGS.md` 2026-09-20, Finding 5).

## 9. Re-test instructions for the maintainer (each fix confirmed or refuted on its own)

Turn **Debug mode** on, export the **debug log** after each step, and export the **system log within a minute** of anything surprising.
- **(a) Clear app data → start.** The system permission prompt appears. *Deny* → a "Bluetooth permission needed" panel with **Allow**; deny again permanently → the panel offers **Open app settings**. *Allow* → it proceeds. *Refuted if* you ever see "No Pixel Buds paired yet" while the permission is off.
- **(b) Pairing from the app.** (i) Buds in pairing mode (case open, button > 3 s): **Pair a device** → system dialog → *Allow* → "Pairing… " → "Paired with …" (a bond dialog may appear). (ii) Buds already paired in Android's settings: the app shows the paired state at once; if you still pair, it says already bonded / succeeds without a new bond.
  (iii) Buds *not* in pairing mode: "The Buds didn't answer. Open the case and hold the pair button…". Afterwards `getMyAssociations` should show one association (the debug log says "reusing existing association" / "removed n duplicate association(s)"). *Refuted if* "could not be looked up" still appears — send the debug log.
- **(c) Paired but not connected / connected in Android but no app session.** The Connection screen's first line matches Android's Bluetooth settings ("Paired — not connected to this phone" / "Connected to this phone (Android)"); with the session closed it says "App control: not open yet" with a prominent **Connect**.
- **(d) Mirroring.** App open: open the case and take a bud out → within a few seconds the card shows connected, without a tap; put it back / close the case → "not connected" after ~1.5 s; repeat quickly (no flicker); Bluetooth off → "Bluetooth is disabled"; start the app *after* the Buds are connected → correct initial state; the session stays closed until you tap Connect.
  The debug log has `Android link (…)` lines for every transition. *Refuted if* the card lags Android's settings by more than a few seconds or never updates (then the non-exported receiver may not get these system broadcasts on GrapheneOS — send the log).
- **(e) Battery.** Earbuds in your ears: percentages; in the case: "(charging)"; one of each: mixed. Compare with Android's own display (the Case stays "unavailable" by design).
- **(f) EQ.** Connect → the EQ tab shows the real current EQ within ~3 s (compare with the official app or by ear); change a preset → is it audible? Export the debug log and send the new `pw_rpc …` lines: `RESPONSE ch=… method=WriteSetting status=OK` means accepted; `SERVER_ERROR`/`NOT_FOUND`/no line = rejected/ignored. If the EQ tab says it couldn't read, the message names the reason.
- **(g) HFP diagnostic.** Connect, export the debug log (look for `HFP receiver registered` and any `HFP broadcast received:`), and run `adb shell dumpsys bluetooth_manager | grep -i -B3 -A3 battery` with the Buds connected — paste the Buds' lines.
- **(h) Evidence for the open capture:** put the HCI snoop log, screen recording, app debug export, app logcat and system log in `android/logs/LOGS-001/` following the events file there.
Also confirm nothing regressed: once connected the session stays connected (ADR-032), ANC works, Find rings until Stop.

## 10. Documentation updated

`DECISIONS.md` (ADR-034; updates to ADR-031/032/033), `id_registry.csv`, `PROTOCOL.md` (§2.2a, §4.2, §4.3 Option B), `ARCHITECTURE.md` (§1 diagram, §3.1, §5a, §6.0b, §9.0a), `TODO.md`, `CHANGELOG.md`, `DESKRESEARCH_FINDINGS.md`, `scripts/pwrpc_decode.py`, `ai-sessions/INDEX.md`,
this file and its prompt. Per `AI_SESSION_LOG_PROCEDURE.md` §4a `0040`'s Status was updated (its §10 proposals 1, 2, 3 and 4 were answered in prompt `0041` §1 and re-confirmed in chat; proposal 5 stays open). No evidence file is named in any committed file.

## Final summary

The in-app pairing failure was the app's own bug — a lower-case address handed to an API that needs upper-case — and "already paired" was invisible because the app never requested its Bluetooth permission; both are fixed with regression tests, together with association re-use, distinct failure messages, and a Connection screen that now leads with Android's own paired/connected state and treats the app's session as a secondary line. The battery decoder shows charging. On the Maestro channel the "correlation byte" turned out to be the pw_rpc channel id, which the app set to 0; the EQ is now read at Connect on the channel the Buds announce, writes are only counted once acknowledged, and every pw_rpc answer is logged so the next test shows whether the Buds accept them. 1335 tests pass; nothing is hardware-verified, `LOGS-001` was empty, and the HFP removal and automatic connecting await your decisions.

---

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0041_FEATURE_RESULT_2026_09_20.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0041_FEATURE_RESULT_2026_09_20
