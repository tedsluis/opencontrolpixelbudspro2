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

Open after `CAP-068` (`ai-sessions/0070`); each goes into the next app run with its expected bytes. **Taken into the 1.1.0 run `CAP-070`** (skeleton by
`ai-sessions/0074`, `captures/CAP-070-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_BF/CAP-070-EVENT-NOTES.md`) — they stay here until that run is analysed:

- [ ] **M** Run `CAP-070` (Group BF) on the 1.1.0 release APK, installed over 1.0.1 (`RELEASING.md` checklist B–C); a later CAPTURE session analyses it and
      gives the release verdict (`ai-sessions/0074` §1 point 3).
- [ ] The five switches of 1.1.0 on hardware — read, off, read back, on (BF-1 … BF-12), with the case-sound and Volume-EQ observations; the three request forms
      no capture holds yet: fields 11 and 29 on channel 19 (BF-14, BF-15), `4:{15:1}` on channel 21 (BF-20) — their real bytes then replace the labelled
      structural fixtures (`Settings074`, `SettingsCodecTest`) and the `// TODO(verify)` on `SettingsCodec.FIELD_VOLUME_EQ` (ADR-055) goes.
- [ ] The screen-reader text on the phone: every "—" with the description "Not read from the Buds yet" (BF-24, two `uiautomator` dumps).
- [ ] `APP_TESTPLAN.md` H5 with the *Read EQ again* button (`CAP-068` read the value back only at a reconnect) → BF-21; S6 ("—" in the first second after
      ready — film the phone's screen, not the camera) → BF-1 on the screen recording; S12 (export across a rotation) → BF-23. S9 (needs two Pixel Buds paired)
      — not in `CAP-070` either.
- [ ] P1 of a release run: record `dumpsys package … | grep -E "firstInstallTime|lastUpdateTime"` in the test user — `CAP-068`'s logs suggest a fresh
      install, not an update over 1.0.0 (`CAP-068-FINDINGS.md` §0) → `CAP-070` P1.
- [ ] The Left bud taken out with both worn on channel 19, head in view (lead L-1 of `ai-sessions/0061`; half answered in `CAP-066`) — 🔴 → `CAP-070` BF-18
      (`INEAR-005`).
- [ ] The channel-19 balance frame `17:7` → `CAP-070` BF-16. B4 double tap (`AlreadyInProgress`) — not in `CAP-068`/`CAP-070` (B4 needs the Buds forgotten).
- [ ] K5 / BC-12 (GrapheneOS Bluetooth auto-off): only in a user where the setting exists (the Owner).
- [ ] The swipe between tabs — not identified on film in any capture (`ARCHITECTURE.md` §2.4, `// TODO(verify)`) → `CAP-070` BF-22 (screen recording).
- [ ] F-4 on a **debug** build: StrictMode lines after a Bluetooth off/on (🟡 the framework's `BluetoothLeAudio` may still warn —
      `CAP-066-FINDINGS.md` §8). Not observable on a release build.
- [ ] Execute the test plans on a second device (another Android version or OEM) — never done; one phone (Pixel 9a, GrapheneOS) so far.

## 3. Planned captures

| Capture | Group | What | Phone / app |
|---|---|---|---|
| `CAP-055` | AQ | Nod/Shake head gestures with an active call or notification (needs a second phone) | Pixel 7a, official app |
| `CAP-030` | Q #19–20 | Loud Noise Protection / Adaptive Audio (firmware naming never reconciled with `release_5.203`, `PROTOCOL.md` §0.1) | Pixel 7a, official app |

From `ai-sessions/0072` (`CAP-053`/`054`/`058`, analysed 2026-10-06) — procedures for a next attempt, no skeleton written:

- [ ] Group AO, step 5 only: drag a slider and wait until the **Save button is enabled** (the code's condition: the curve the Buds echoed equals
      neither a preset nor the saved curve), then — one run each — the system **Back** gesture, the app-bar arrow, and opening another screen
      from the EQ screen, no Save: the code predicts one `WriteSetting 4:{18:…}` and the "saved" toast each time; then **Home** instead (predicted:
      no write) and Back with the Save button disabled (predicted: no write). `hod` is a navigation destination listener
      (`ai-sessions/0073` §4.4; `CAP-053-FINDINGS.md` §3, §8).
- [ ] Group AT, 4th attempt: disable the companion app first (`adb shell pm disable-user …maestro.companion`, reversible), keep `logcat -b events`
      running, `pidof` with its positive control, pair from Settings → Pair new device only; then re-enable and open the app (`CAP-058-FINDINGS.md` §7).
      Positive controls and the list of the app's exported components that can start its process: `ai-sessions/0073` §4.10 (`pm list packages -d`
      must list the package; `pidof` must stay empty after Settings → Connected devices is opened).
- [ ] Documentation only: re-check `CAP-015`'s film at its 15 field-18 frames for a Save tap or a screen change (`CAP-053-FINDINGS.md` §7).

- [ ] Optional, destructive, one-time: the factory-reset re-pair for comparison (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P #16) — it also resets the
      Find My Device link.
- [ ] Record Play services' *Nearby devices* permission state and the phone's Android version (Settings → About, on film) in every capture
      (`A68-CAP-23`: the Pixel 7a's version is unreconciled in `CAP-036`…`041`).
- Standing practice (kept open on purpose): every capture session gets its Capture Index row with firmware, Android and app version
  (`PROJECT_RULES.md` rules 11 and 14); every hypothesis test is logged in the capture's FINDINGS before a promotion (rule 4).

## 4. Protocol: leads and open questions

The five switches the maintainer chose (Multipoint, head gestures, the two case sounds, Volume EQ; ADR-052 … ADR-055) and the screen-reader text are built in
1.1.0 (`ai-sessions/0074`); their hardware run is in §2.

- [ ] Decompile the official app **1.0.990706425** (versionCode `10260911`) with JADX and apktool (ADR-017 boundary). Its first purpose is met
      without it: the two ids of `CAP-069` are named from literals of 1.0.955078536 — `0xbf6c9399` = `a10a20.kpi.Kpi`/`KpiStream`, `0x8d99df93` =
      `maestro_pw.JitterBuffer`/`SetJitterBufferSizePreference` (`ai-sessions/0073` §4.1, `PROTOCOL.md` §2.2a note of 2026-10-06). What the newer
      version is still needed for: setting **39** (it requests `ReadSetting 4:39`, `CAP-069` 1333 → `UNKNOWN`), the schemas of settings 23 and 31
      (their answers carry fields 1.0.955078536 does not know), whether the `find_device` placeholder (Play services item 1002) is still requested,
      and why it calls the KPI stream and the jitter-buffer preference. Pulled 2026-10-05 from the Pixel 7a (`adb pull` of base + 2 splits, same
      signing certificate as 1.0.955078536) into `reverse-engineering/apk/v1.0.990706425-10260911/` (gitignored); not decompiled yet (**M**, chat
      2026-10-05: *"Voer het nog niet uit. Dat kan later."*). Also add the three files' SHA-256 to `reverse-engineering/APK_VERSIONS.md`, whose
      1.0.990706425 paragraph still says "Not pulled". Start with `citation_checker` and `pwrpc_name_table` on the new tree
      (`APK_REVERSE_ENGINEERING_PROCEDURE.md` §7 steps 7–8; design notes for the diff in `reverse-engineering/tools/BACKLOG.md`).

Proposals awaiting the maintainer (`DESKRESEARCH_FINDINGS.md`, entry of 2026-10-03 — nothing applied as a status change):

- [ ] **M** §4.3 Option E: "cross-confirms the Right value" → "equals the lower of the two bud levels (🟡)".
- [ ] **M** §2.3: add DLCI 0x08 Code `0x03` and Code `0x05` with their 🟡 readings.
- [ ] **M** `REVERSE_ENGINEERING.md`: name field 2 of the runtime-info and software-info messages "wall clock, ms (🟡)".
- [ ] **M** Open proposals from earlier capture FINDINGS (`ai-sessions/0059`): `CAP-008` §5/§4 (eSCO/mSBC, `CALL-001`); `CAP-026` item 3 (the
      short Case form); `CAP-029` item 3 (`CASE-008`); `CAP-037` item 3 (Settable ↔ Current co-occurrence); `CAP-047` items 4 and 5;
      `CASE-007` 🔵 → 🟢 (`A68-SES-03`).

Open questions (each with where it is described):

- 🔴 GSND CONTROL Code `0x05`: 🟡 3 = no bud worn, 4 = one, 6 = both (`CAP-069` §8); value 5 (twice, unfilmed) and 1 (never seen) open — test: one bud
  inserted with the other in the hand, ears on film. 🟡 Code `0x16` = head gestures active (`CAP-069` §1); field 2 of Code `0x03`.
- 🔴 The DLCI 0x0a waves are assistant sessions opened by `01 09 … 0a 01 03` (`CAP-069` §3, 3 of 3), but the hold itself has not been filmed; the hold codes
  03/05/01/21 are 🟡. 🟡 Who owns DLCI 0x08/0x0a — the Google app's
  assistant service is the lead (`CAP-061-FINDINGS.md` §2).
- 🔴 Ring status: what the Buds send when the ringing is stopped on the bud (L68-2; `CAP-069`: the official app has no Find device any more and Find Hub
  rang nothing; 2026-10-05 the maintainer downgraded the official app to 1.0.955078536 on the Pixel 7a and it shows no Find device either — 🟡 removed
  by something outside the APK version (server-side flag or Play services), unverified; `CAP-058` (2026-10-05, 1.0.955078536): Settings' Device details showed **Find device** while the Buds were
  bonded but not connected and not after they connected (🟡 shown only while not connected — `CAP-058-FINDINGS.md` §4) — test with OpenControl's own Ring while a client holds the Message Stream; `CAP-068` could not observe it — the app's claim
  is released 1.5 s after the Ring, `CAP-068-FINDINGS.md` §6). Known limit of 1.0.1: the "Ringing" notice stays until Stop is tapped. The app does not read the
  Buds' ring-status message; whether it should is a decision after those runs (**M**, "Nothing new on the wire; test first").
  *(2026-10-06, `ai-sessions/0073` §4.8:* the Find device row is not built by the companion APK — its `find_device` preference is a hidden
  placeholder that Google Play services fills (item id 1002, `gow.java:24`); label, visibility and target are Play services' — class C, no APK
  version can explain the row's absence.)
- 🔴 `CAP-054`: no clear Battery Notification field on case-open (`PROTOCOL.md` §4.3 Option A, Update of 2026-10-06), yet Android showed L/C/R
  without a connection — what it decoded (the closed-case advertiser's bytes after the salt, `0x2a`/`0x29` lid-state byte, or the Random Resolvable
  Data); the `0x1a` field of the bud-out advertisers (`CAP-054-FINDINGS.md` §2, §9).
- 🔴 `CAP-058`: what restarted the companion app ≈ 10 s after the force stop (🟡 Settings' device pages); why the official app's `ReadSetting` sweep
  began 16 s after MAESTRO opened; why "Media audio" off also closes GSND CONTROL/AUDIO (`CAP-058-FINDINGS.md` §1, §6, §9).
- 🔴 `CAP-053`: why the EQ screen redrew the Mid thumb 10 s after the drag (UI only; `CAP-053-FINDINGS.md` §4).
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
  🟡 top-level field 2 = wall clock (desk entry of 2026-10-03). *(2026-10-06, `ai-sessions/0073` §4.6:* in the official app's schema an entry's
  field 2 is the charger-type enum `NOT_CHARGING(1)`/`WIRED(2)`/`GENERIC_QI(3)`; top-level fields 2 and 3 are not in that schema; 7.2 ↔ Left,
  7.1 ↔ Right in the code too. Still open: an empty-case test, field 3, 7.3 — per the trace "LL mode".)
- 🔴 `CAP-056`: what makes the Buds close DLCI 0x02 on a wear change; the pause route with GSND closed; Settable with in-ear detection off and
  no bud worn (`PROTOCOL.md` §6).
- 🔴 Settings 23, 24, 26, 30, 31, 32 (answered by the Buds, no writer or reader in app 1.0.955078536) and 39 (asked by 1.0.990706425): what they
  are (`ai-sessions/0073` §4.2; `DESKRESEARCH_FINDINGS.md` 2026-10-06). 🟡 the ANC taps filmed in the official app never produced a `WriteSetting`
  of setting 13, although the app's own ANC row writes only that (`ai-sessions/0073` §4.9) — in the next official-app capture, film which
  control is tapped (the app's row or the Play-services/Settings one). 🟡 what the Buds mean by the "primary" bud of the announcement (field 6;
  the app-side routing is answered, `PROTOCOL.md` §2.2a note of 2026-10-06).
- 🟡 Hearable Controls MAC not enforced (`PROTOCOL.md` §4.1): a firmware that starts enforcing it would NAK with reason `0x03`.
- 🔴 `CAP-069` leftovers: why a bud's charging bit stayed set ≈ 12 s after it left the case (7358 → 7404); why the phone re-opened GSND AUDIO on ACL 3
  (8157 → 8271); Device Information `03 0b` (FHN EID, out of scope) with length 25; field 18 after the Save button (one sample, 6050 → `CAP-053`).
- [ ] **M** The maintainer's Buds were left with the EQ on a custom "Last saved" curve and the press-and-hold mode list with Off ticked after `CAP-069`
      (P7 was Balanced, Off unticked) — restore if wanted before the next run.
- 🔴 Remaining battery time (Fast Pair Device Information code `0x04`): never seen on the wire. Ring "both" (`0x03`, `FIND-004`): never sent, never
  captured — sending it needs its own ADR. Spatial audio / LE Audio (`SPATIAL-001`, `LEAUDIO-001`): not captured.
- 🔴 `PROTOCOL.md` §5.2 steps 4 and 6 of the connection lifecycle (handshake content order, user-command timing); DLCI 0x08's protocol identity.
- 🔴 Why Android's CDM picker listed nothing twice and then offered the bonded Buds directly (`CAP-068-FINDINGS.md` §8).
- 🔴 `CAP-013`'s second BLE link (not re-examined; `CAP-016`'s was a heart-rate wearable — `PROTOCOL.md` §6, Update of 2026-10-03).
- Candidates without a capture scenario: Audio switch (`SWITCH-001`), Hearing wellness (`WELL-001`), eartip fit test (`FIT-001`).
  `maestro_pw.Dosimeter`: values on the wire, meaning 🟡; a display is out of scope (**M**, 2026-09-30).

## 5. App: later work

- [ ] **Dependency and tool upgrade — its own session after 1.0.1** (**M**, "Own session after 1.0.1"): Gradle, AGP, Kotlin, the Compose BOM, Hilt;
      on that bump re-check the `@ExperimentalMaterial3Api` opt-ins (`TopAppBar`, `PullToRefreshBox`) and `TabRow` vs `PrimaryTabRow`
      (`SettingsMenu.kt`). Add `distributionSha256Sum` to the Gradle wrapper properties (`A68-HK-05`).
- [ ] 🟡 **Accessibility, found while testing `ai-sessions/0074`:** a switch row's label and its `Switch` are separate accessibility nodes (siblings in the
      card), so a screen reader may announce "switch, on" without the setting's name — not tested on a phone. A merged row (`Modifier.toggleable` with
      `Role.Switch` on the row, the `Switch` without its own click) would fix it; it changes every switch's semantics (and the tests that find a switch by its
      sibling label) — **M** decides whether and when.
- [ ] **Balance precision:** `CAP-067` reached Right 4 on the 17th drag (7 of 17 snapped to Centre). Options: a live value label while dragging,
      slider `steps`, or the step buttons back.
- [ ] **String resources:** the UI texts are Kotlin literals; moving them to `strings.xml` is the precondition for any translation.
- [ ] **Instrumented tests** — none exist: `OsConnectionObserver`, `BudsForegroundService`/`AncTileService`, the `BluetoothDevice`-dependent part
      of `BudsRepositoryImpl.connect()` and all of `:app` are covered only by pure-function and Robolectric tests.
- [ ] Third-party notices on the Info tab (releases carry `THIRD_PARTY_NOTICES.txt`; bundling it like the licence text is a possible later step).
- [ ] Auto-connect on lid-open: not possible from any event the app sees (`CAP-064-FINDINGS.md` §1); **M** chose "Nothing now" (2026-10-01).
- [ ] Undecided, each needs its own ADR (**M**): a background session via CDM device presence (`ARCHITECTURE.md` §6.0b (b)); a per-channel
      "degraded" state; the BLE battery advertisement scan (ADR-006's bounded exception); `SubscribeToSettingsChanges`. (The settings that were
      readable but not shown — 11, 15, 27, 28 — are built in 1.1.0 with ADR-053, ADR-055, ADR-054, as is 29 with ADR-052.)
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
- [ ] Reading and checks `ai-sessions/0073` owed (its "Files read" and "Deferred documentation"): `ARCHITECTURE.md`, the five older tool
      `SPEC.md` files and the earlier APK-session RESULTs were not read in full by the main session; `PROTOCOL.md` only in the sections it
      touched (three sub-reviews swept it); 63 of the citations its documentation edits added resolve inside their file but carry no quoted
      token for `citation_checker` to compare; the statements marked "per the trace" in its §4 were taken from a sub-review without
      line-by-line re-derivation; the class-A rows of its §4.11 were not worked (among them the EQ gain unit and the `"cape2_sm"`/`"500m"`
      strings); the capture FINDINGS that raised the answered questions (`CAP-069-FINDINGS.md` §9, `CAP-053-FINDINGS.md` §8,
      `CAP-058-FINDINGS.md` §4 and §7, `CAP-051-FINDINGS.md`) carry no pointer to its code-side answers (not among the approved drafts).
- [ ] Recount the cross-capture censuses of `ai-sessions/0073` without duplicated logs: `CAP-042`'s `.log.last` is leftover `CAP-041` content, so
      every packet and "in N logs" count of its §4.1/§4.2 and of `DESKRESEARCH_FINDINGS.md`'s entry of 2026-10-06 includes `CAP-041` twice (dated
      correction there). First list which `.log.last` files overlap a neighbouring capture (first timestamp and one frame's bytes), then rerun
      `pwrpc_name_table census` on the de-duplicated set and correct the figures as dated notes (**M** for the texts).
- [ ] Deferred items of earlier sessions that never reached this file (`A68-SES-03`): `ai-sessions/0061` ("Not read in this session …"),
      `ai-sessions/0063` (one item) — read those RESULTs and carry what is still open.
- [ ] `scripts/__pycache__/lint_docs.cpython-314.pyc` is a tracked file (a compiled-Python artefact; the prompt of `ai-sessions/0069` names `__pycache__` as never to be
      staged). `.gitignore` already ignores `__pycache__/`, so it only needs untracking (`git rm --cached`) — **M**: a deletion from the repository. Until then run the scripts with
      `PYTHONDONTWRITEBYTECODE=1`, or the file shows up as modified.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/TODO.md - https://tedsluis.github.io/opencontrolpixelbudspro2/TODO
