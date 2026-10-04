# TODO.md

**Open items only** (restructured 2026-10-03, `ai-sessions/0069`, the maintainer's choice in chat: *"Corrections now; TODO.md + CHANGELOG now; rest
later"*). Done work is in `CHANGELOG.md`, in each session's RESULT under `ai-sessions/`, and in git — the file as it was before this restructure
(1,040 lines, mostly completed items) is `git show cd30332:TODO.md`. A new item goes into the group it belongs to, with the session or capture it
came from; a finished item is deleted here and recorded where the work is (`PROJECT_RULES.md` rule 15).

Other documents that point to "`TODO.md` Phase 1…5", its "Phase 2 checklist", its "priority order" or "Known technical debt" mean sections of the
pre-restructure file (same command). The `pbtk` root cause those pointers most often want is in `DECISIONS.md` ADR-041 and
`reverse-engineering/APK_VERSIONS.md`.

Legend: 🔴 open question · 🟡 hypothesis to test · **M** = needs the maintainer (a decision, a sign-off, or an action only they can do).

## 1. After release 1.0.1 (published 2026-10-04, `ai-sessions/0070`)

- [ ] **M** Decide whether two behaviours built in 1.0.1 on your chat answers get an ADR each (none was written — `AGENTS.md` §6): the device choice
      (no silent pick, "Use different Buds") and the "current value" rule (`ARCHITECTURE.md` §3.1, §9.0a item 8 describe them as built). Both seen on
      hardware in `CAP-068` (S7/S8; S1, S2, S4); the maintainer chose "Not now" again (chat 2026-10-04, `ai-sessions/0070`).

## 2. Hardware verification debt (the app, on film)

Open after `CAP-068` (`ai-sessions/0070`); each goes into the next app run with its expected bytes.

- [ ] `APP_TESTPLAN.md` H5 with the *Read EQ again* button (`CAP-068` read the value back only at a reconnect); S6 ("—" in the first second after
      ready — film the phone's screen, not the camera), S12 (export across a rotation), S9 (needs two Pixel Buds paired) — not shown in `CAP-068`.
- [ ] P1 of a release run: record `dumpsys package … | grep -E "firstInstallTime|lastUpdateTime"` in the test user — `CAP-068`'s logs suggest a fresh
      install, not an update over 1.0.0 (`CAP-068-FINDINGS.md` §0).
- [ ] The Left bud taken out with both worn on channel 19, head in view (lead L-1 of `ai-sessions/0061`; half answered in `CAP-066`) — 🔴. Not in
      `CAP-068`; add it to the next run with both buds worn.
- [ ] The channel-19 balance frame `17:7`; B4 double tap (`AlreadyInProgress`) — not in `CAP-068` (B4 needs the Buds forgotten).
- [ ] K5 / BC-12 (GrapheneOS Bluetooth auto-off): only in a user where the setting exists (the Owner).
- [ ] The swipe between tabs — not identified on film in any capture (`ARCHITECTURE.md` §2.4, `// TODO(verify)`).
- [ ] F-4 on a **debug** build: StrictMode lines after a Bluetooth off/on (🟡 the framework's `BluetoothLeAudio` may still warn —
      `CAP-066-FINDINGS.md` §8). Not observable on a release build.
- [ ] `APP_TESTPLAN.md` A5 wording: revoking *Nearby devices* in Settings ends the process; the "You denied the permission" screen appears only
      after a denial in the prompt (`CAP-067` §7) — correct the test plan step when it is next run.
- [ ] Execute the test plans on a second device (another Android version or OEM) — never done; one phone (Pixel 9a, GrapheneOS) so far.

## 3. Planned captures

| Capture | Group | What | Phone / app |
|---|---|---|---|
| `CAP-069` | BE | head gestures and Multipoint off/on on film, assistant hold, tap on the current ANC mode, EQ "Default", wear states, Ring stopped on the bud | Pixel 7a, official app |
| `CAP-054` | AP | the `0xFE2C` advertisement right after the lid opens, connection-free (redesigned 2026-10-03, L68-6) | Pixel 7a, no app |
| `CAP-053` | AO | EQ outer field 16 vs 18: Save tap, navigate away, slider release — each isolated | Pixel 7a, official app |
| `CAP-055` | AQ | Nod/Shake head gestures with an active call or notification (needs a second phone) | Pixel 7a, official app |
| `CAP-058` | AT | `SDP-001`/`SDP-002`, third attempt, with an on-device process-liveness check | Pixel 7a |
| `CAP-030` | Q #19–20 | Loud Noise Protection / Adaptive Audio (firmware naming never reconciled with `release_5.203`, `PROTOCOL.md` §0.1) | Pixel 7a, official app |

- [ ] Optional, destructive, one-time: the factory-reset re-pair for comparison (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P #16) — it also resets the
      Find My Device link.
- [ ] Record Play services' *Nearby devices* permission state and the phone's Android version (Settings → About, on film) in every capture
      (`A68-CAP-23`: the Pixel 7a's version is unreconciled in `CAP-036`…`041`).
- Standing practice (kept open on purpose): every capture session gets its Capture Index row with firmware, Android and app version
  (`PROJECT_RULES.md` rules 11 and 14); every hypothesis test is logged in the capture's FINDINGS before a promotion (rule 4).

## 4. Protocol: leads and open questions

Next app features, chosen by the maintainer (chat 2026-10-03, "Features": *"Yes to both, after CAP-069"*):

- [ ] **Head gestures on/off** — `qhr` field 29, 🟡 1 = off / 2 = on (`PROTOCOL.md` §4.5.4). Needs `CAP-069` section I, then **M**'s promotion and an ADR.
- [ ] **Multipoint on/off** — field 11, 🟡 (`PROTOCOL.md` §4.5.2; SASS flag bit 🟡). Needs `CAP-069` section II, then **M**'s promotion and an ADR.

Proposals awaiting the maintainer (`DESKRESEARCH_FINDINGS.md`, entry of 2026-10-03 — nothing applied as a status change):

- [ ] **M** §4.3 Option E: "cross-confirms the Right value" → "equals the lower of the two bud levels (🟡)".
- [ ] **M** §2.3: add DLCI 0x08 Code `0x03` and Code `0x05` with their 🟡 readings.
- [ ] **M** `REVERSE_ENGINEERING.md`: name field 2 of the runtime-info and software-info messages "wall clock, ms (🟡)".
- [ ] **M** Open proposals from earlier capture FINDINGS (`ai-sessions/0059`): `CAP-008` §5/§4 (eSCO/mSBC, `CALL-001`); `CAP-026` item 3 (the
      short Case form); `CAP-029` item 3 (`CASE-008`); `CAP-037` item 3 (Settable ↔ Current co-occurrence); `CAP-047` items 4 and 5;
      `CASE-007` 🔵 → 🟢 (`A68-SES-03`).

Open questions (each with where it is described):

- 🔴 DLCI 0x08 Code `0x05`: the meaning of values 1, 3, 4, 5, 6 (→ `CAP-069` VI); Code `0x16` follows field 29 with one counter-sample
  (`CAP-029` 3757); field 2 of Code `0x03`.
- 🔴 The `CAP-021` DLCI 0x0a waves: the trigger is not on film (→ `CAP-069` III). 🟡 Who owns DLCI 0x08/0x0a — the Google app's
  assistant service is the lead (`CAP-061-FINDINGS.md` §2).
- 🔴 A tap on the current ANC mode: OpenControl sends the `Set` and the Buds ACK it (`CAP-068`, `PROTOCOL.md` §4.1 Update 2026-10-04); what the
  official app sends is `CAP-069` IV — then **M** decides whether OpenControl should skip it (chat 2026-10-04: "Keep the Set; decide after CAP-069").
- 🔴 Ring status: what the Buds send when the ringing is stopped on the bud (L68-2 → `CAP-069` VII; `CAP-068` could not observe it — the app's claim
  is released 1.5 s after the Ring, `CAP-068-FINDINGS.md` §6). Known limit of 1.0.1: the "Ringing" notice stays until Stop is tapped. The app does not read the
  Buds' ring-status message; whether it should is a decision after those runs (**M**, "Nothing new on the wire; test first").
- 🔴 The Fast Pair battery advertisement on case-open (L68-6 → `CAP-054`). The only route to a Case level without a connection (ADR-006).
- 🔴 Why the Buds close both RFCOMM channels (`CAP-059`/`CAP-060-FINDINGS.md` §1): the idle / periodic / second-host / quick re-claim
  experiments were never run as isolated tests. After docking, the Buds refuse about nine re-opens each until the link drops (`CAP-040`,
  L68-10) — relevant to the re-open back-off.
- 🔴 Why Play services stops re-claiming DLCI 0x04 (`CAP-059`, `CAP-061`); what closed the app's claims at `CAP-065` 11:10:06.49 and
  11:21:35.24, and why the Buds closed the session at 11:17:46 with both buds worn.
- 🔴 Why `CAP-064` read Settable `e8` for 28 s with both buds straight from the case (`CAP-065`/`CAP-066`: always `00`); four `e8` Notifies
  with no bud worn in `CAP-047` (1048, 2673, 3996, 4339) are not among ADR-049's samples (L68-10).
- 🔴 Why the Buds switched ANC Transparent → Active between 08:07:34 and 08:12:44 without an app command (`CAP-067` §2).
- 🔴 Why Android pages the Buds 0.1 s (or 3.4 ms) after an ACL drop at docking in some cases and not in others (`CAP-064` §1a, `CAP-066`);
  why Android did not re-create the ACL after an ADR-016 drop with the lid open in `CAP-063`.
- 🔴 The swapped-dock ADR-016 disconnect inconsistency between the two recordings of `CAP-047` (carried from `ai-sessions/0022`).
- 🔴 Runtime-info stream: field 3, entry 7.3; whether field 2 of an entry means "in the case" or only "charging" (`PROTOCOL.md` §4.3 Option F).
  🟡 top-level field 2 = wall clock (desk entry of 2026-10-03).
- 🔴 `CAP-056`: what makes the Buds close DLCI 0x02 on a wear change; the pause route with GSND closed; Settable with in-ear detection off and
  no bud worn (`PROTOCOL.md` §6).
- 🟡 Whether the unsolicited announcement reaches the official app's `gaa.d` (`PROTOCOL.md` §2.2a, Update of 2026-10-01).
- 🟡 Hearable Controls MAC not enforced (`PROTOCOL.md` §4.1): a firmware that starts enforcing it would NAK with reason `0x03`.
- 🔴 Remaining battery time (Fast Pair Device Information code `0x04`): never seen on the wire. Ring "both" (`0x03`, `FIND-004`): never sent, never
  captured — sending it needs its own ADR. Spatial audio / LE Audio (`SPATIAL-001`, `LEAUDIO-001`): not captured.
- 🔴 `PROTOCOL.md` §5.2 steps 4 and 6 of the connection lifecycle (handshake content order, user-command timing); DLCI 0x08's protocol identity.
- 🔴 Why Android's CDM picker listed nothing twice and then offered the bonded Buds directly (`CAP-068-FINDINGS.md` §8).
- 🔴 `CAP-013`'s second BLE link (not re-examined; `CAP-016`'s was a heart-rate wearable — `PROTOCOL.md` §6, Update of 2026-10-03).
- Inventory not re-derived in `ai-sessions/0069` (L68-9, a sub-review result): which settings the official app reads at connect (fields 1–5, 7,
  11–13, 15–19, 21–32, 34–38; never 6, 8, 9, 10, 14, 20, 33), `DynamicServerConfigService/SetConfig` (not named in `PROTOCOL.md`), and the services
  never seen on the wire (`HeadGesture`, `EartipFitTest`, `JitterBuffer`). Check: `python3 scripts/pwrpc_decode.py` over `CAP-036`/`CAP-041`.
- The UUID register (`REVERSE_ENGINEERING.md`): a checked negative for `CAP-034`'s eight GATT UUIDs; "exhaustive" is not claimed. The Buds'
  Extended Inquiry Result lists five custom 128-bit UUIDs before pairing (`CAP-033` frame 1072) — not yet in the register.
- Candidates without a capture scenario: Audio switch (`SWITCH-001`), Hearing wellness (`WELL-001`), eartip fit test (`FIT-001`).
  `maestro_pw.Dosimeter`: values on the wire, meaning 🟡; a display is out of scope (**M**, 2026-09-30).

## 5. App: later work

- [ ] **Dependency and tool upgrade — its own session after 1.0.1** (**M**, "Own session after 1.0.1"): Gradle, AGP, Kotlin, the Compose BOM, Hilt;
      on that bump re-check the `@ExperimentalMaterial3Api` opt-ins (`TopAppBar`, `PullToRefreshBox`) and `TabRow` vs `PrimaryTabRow`
      (`SettingsMenu.kt`). Add `distributionSha256Sum` to the Gradle wrapper properties (`A68-HK-05`).
- [ ] **Accessibility:** a setting that was not read shows "—" (the maintainer chose the visual form only, 2026-10-03); a content description
      ("not read") for screen readers is the deferred half.
- [ ] **Balance precision:** `CAP-067` reached Right 4 on the 17th drag (7 of 17 snapped to Centre). Options: a live value label while dragging,
      slider `steps`, or the step buttons back.
- [ ] **String resources:** the UI texts are Kotlin literals; moving them to `strings.xml` is the precondition for any translation.
- [ ] **Instrumented tests** — none exist: `OsConnectionObserver`, `BudsForegroundService`/`AncTileService`, the `BluetoothDevice`-dependent part
      of `BudsRepositoryImpl.connect()` and all of `:app` are covered only by pure-function and Robolectric tests.
- [ ] Third-party notices on the Info tab (releases carry `THIRD_PARTY_NOTICES.txt`; bundling it like the licence text is a possible later step).
- [ ] Auto-connect on lid-open: not possible from any event the app sees (`CAP-064-FINDINGS.md` §1); **M** chose "Nothing now" (2026-10-01).
- [ ] Undecided, each needs its own ADR (**M**): a background session via CDM device presence (`ARCHITECTURE.md` §6.0b (b)); a per-channel
      "degraded" state; the BLE battery advertisement scan (ADR-006's bounded exception); `SubscribeToSettingsChanges`; the settings that are
      readable but not shown (11, 15, 27, 28).
- [ ] Safe Mode: grey out Sound/Controls (offered in `ai-sessions/0057`, not chosen) — revisit only if a Safe-Mode run shows the per-tap refusal
      confuses.
- [ ] From `ai-sessions/0068` `A68-APP-13`, not changed in 1.0.1 — each needs a test that forces the interleaving, which the present test set-up
      cannot produce (no `BluetoothDevice` in unit tests): `disconnect()` runs outside the connect mutex (a Disconnect during a Connect);
      `reclassifyLoss` is reached from three paths without a common lock; a write's call-id quarantine is set on timeout but not on cancellation;
      a profile proxy bound after `closeAll` is never closed. And one design question: after an ANC `Set`, a `Notify` of the *old* mode ends the wait
      as success, so a NAK that follows is not reported (the mode shown is right; a labelled test records it; no capture shows that order). An ACK
      applies the requested mode, not the bytes the ACK echoes.
- [ ] From `A68-APP-10`, 🟡 not tested: the on-demand channel insert in `RfcommBudsTransport.openChannel` is not atomic with `closeAll` (a socket
      could be left open); `startForegroundService` followed at once by `stopService` on a connect that fails fast (`CAP-068` BD-29: no crash in
      the app's buffers; no system log in the user without Play, so a system-side exception stays unverified); bonded-device lookups run on the main thread.
- [ ] From `A68-APP-09`: the pull-to-refresh guard and the action buttons have no in-flight guard that survives a rotation — a second tap starts a
      second call, which the repository serialises; not shown to be a defect.
- [ ] Tests for `:app` (the module has no test set-up): the tile service's rendering, the foreground-service start/stop, `MainActivity` as the state
      holder, "Use different Buds" end to end (`A68-APP-06`).
- Known limits, left as they are: a loss cause logged "(provisional)" stays the last line when no later link reading arrives (T-1, `0062`); the
  notification flashes on a connect that fails fast (**M**, 2026-10-01); `RfcommBudsTransportTest`'s 10 s timeout was not reproduced in 200 runs
  — the helper prints every thread's stack if it recurs.
- Keep in step: `android/ui/src/main/res/raw/license.txt` with `LICENSE` (`SettingsMenuTest` fails otherwise).

## 6. Documents

Restructures the maintainer deferred (chat 2026-10-03, "Restructures"); each is a MAINTENANCE session of its own, with a before/after check that no
statement is lost (`ai-sessions/0068` §6 has the proposal for each):

- [ ] `ARCHITECTURE.md` — "as built" text; dated notes become current text or move to `DECISIONS.md`; §5a gets a "verified in capture" column (§6.1).
- [ ] `PROTOCOL.md` — a "Current" block per entry with the history below it; a one-page wire summary; §6 open items only (§6.13). All FACT-text
      changes as dated proposals for **M**.
- [ ] `REVERSE_ENGINEERING.md` — split into current reference, checked negatives and trace history; a settings register table; a service/method
      table (§6.6, `A68-RE-05`). Line citations drift with every edit of that file — cite by entry name until then.
- [ ] `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` — Evidence cells to pointers; a Test-ID × capture coverage table (§6.9).
- [ ] `CAPTURE_BLUETOOTH_HCI_SNOOP.md` — the index as one line per capture, generated from `id_registry.csv`; a "how to count" box (§6.4). Fold
      the tightened capture checklist of `ai-sessions/0042` RESULT §12 into it (**M**: procedure).
- [ ] `DESKRESEARCH_FINDINGS.md` — the cross-capture analyses that live only in session RESULTs or ADR text (runtime-info comparison of
      2026-09-25, the announcement census, the call-id census) get entries with their commands, or a pointer line at the top (§6.2).
- [ ] `AGENTS.md` §5 and §6 — shorten each to the rule plus one pointer (**M**: project law; "Four edits, no shortening" for now).
- [ ] Optional (**M**, project law): a dated note in `AGENTS.md` §1 pointing to ADR-050 (the Info links hand a URL to the browser).

Corrections found by `ai-sessions/0068` and not yet made (sub-review items, each with its check in that RESULT's appendix):

- [ ] Arithmetic and cross-reference slips in `CAP-001`, `004`, `008`, `010`, `012`, `015` ("0.05–1.9 s" is 1.14–8.89 s), `047`, `049`, `050`,
      `060` (`A68-CAP-16`; appendix capA/capB/capD), and the rest of `CAP-031`…`034` (`A68-CAP-23` [21]).
- [ ] Folder-name end times: `CAP-036` (film ends 06:41:16), `CAP-041` (17:17:40), `CAP-042` (named after the log, not the film). A rename breaks
      every link to the folder — do it with the index restructure.
- [ ] EVENT-NOTES left as templates: unticked "Next steps" (`CAP-037`…`042`), leftover placeholders (`CAP-035`, `CAP-036`), the placeholder timeline
      of `CAP-041`, `CAP-040`'s timeline without its wire events (`A68-CAP-23` [24]).
- [ ] Rule 9a: FINDINGS with more than one dated layer (`CAP-036` 7+, `CAP-041` 5, `CAP-032`/`034` 4, `CAP-017` 5, and others) — fold into
      current text; FINDINGS without a status banner that still need none checked: `CAP-006`…`008`, `011`…`013`, `019`, `020`, `023`, `025`,
      `027`, `029`, `031`…`033`, `035`, `043`, `044`, `051`.
- [ ] `ARCHITECTURE.md` §3.1: "11 `00` answers sent no `Set`" in `CAP-066` — the audit's sub-review counts 12; not re-derived (`A68-ARCH-03`).
- [ ] `DECISIONS.md` ADR-040 Context: "`AT+BIEV=2,100` seven times" — 12 on the wire (`PROTOCOL.md` §4.3 Option C carries the correction; the
      ADR's own text needs **M**'s dated Update).
- [ ] `scripts/decode_qhr_settings.py` is superseded by `scripts/pwrpc_decode.py` and describes the address format wrongly; four stale statements
      in `reverse-engineering/tools/` backlog/spec files (`A68-RE-06`).
- [ ] Deferred items of earlier sessions that never reached this file (`A68-SES-03`): `ai-sessions/0061` ("Not read in this session …"),
      `ai-sessions/0063` (one item) — read those RESULTs and carry what is still open.
- [ ] `scripts/__pycache__/lint_docs.cpython-314.pyc` is a tracked file (a compiled-Python artefact; the prompt of `ai-sessions/0069` names `__pycache__` as never to be
      staged). `.gitignore` already ignores `__pycache__/`, so it only needs untracking (`git rm --cached`) — **M**: a deletion from the repository. Until then run the scripts with
      `PYTHONDONTWRITEBYTECODE=1`, or the file shows up as modified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/TODO.md - https://tedsluis.github.io/opencontrolpixelbudspro2/TODO
