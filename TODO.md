# TODO.md

**Open items only** (restructured 2026-10-03, `ai-sessions/0069`, the maintainer's choice in chat: *"Corrections now; TODO.md + CHANGELOG now; rest
later"*). Done work is in `CHANGELOG.md`, in each session's RESULT under `ai-sessions/`, and in git — the file as it was before this restructure
(1,040 lines, mostly completed items) is `git show cd30332:TODO.md`. A new item goes into the group it belongs to, with the session or capture it
came from; a finished item is deleted here and recorded where the work is (`PROJECT_RULES.md` rule 15).

Other documents that point to "`TODO.md` Phase 1…5", its "Phase 2 checklist", its "priority order" or "Known technical debt" mean sections of the
pre-restructure file (same command). The `pbtk` root cause those pointers most often want is in `DECISIONS.md` ADR-041 and
`reverse-engineering/APK_VERSIONS.md`.

Legend: 🔴 open question · 🟡 hypothesis to test · **M** = needs the maintainer (a decision, a sign-off, or an action only they can do).

## 1. After release 1.2.2 (published 2026-10-14, `ai-sessions/0089` and its continuation in chat)

- [x] **1.2.2 published 2026-10-14** (tag `v1.2.2` on build commit `bfd7891`, PR #49 merged with `5b204ea`; E1–E4 by the pull request of `release/1.2.2-after`).
      **No hardware capture:** the maintainer tested 1.2.2 on the phone and released it without `CAP-076` (chat 2026-10-14). **D4 done 2026-10-14:** the published APK
      installed on the Pixel 9a with Obtainium (the maintainer, chat 2026-10-14: "Ik heb opencontrol for buds middels obtainium geupdate naar 1.2.2 op mijn pixel
      9a telefoon"); the published APK's SHA-256 = the tested build's (`d750551a…8a6b`, re-checked with `gh release download`). E1–E4: PR #52.
      (1.2.1: D4 done 2026-10-13 with Obtainium; E1–E4 by PR #45.)
- [x] `CAP-076` (Group BL), **the 1.3.0 run**, captured 2026-10-15 in both users and analysed by `ai-sessions/0092` (`CAP-076-FINDINGS.md`): the two requests
      once per session (12 of 12), every hold on the noise-control tab shown (29 of 29 with Play, 12 of 12 without), three minor defects (§5 below); **verdict
      "release as built + Known issues"** (the maintainer, chat 2026-10-15) — next: `RELEASING.md` D1–D4 (the maintainer's), then E1–E4. Not run (the maintainer
      tested only the changed functionality): BL-10, BL-13, BL-15, BL-18, BL-21, BL-24, BL-25.
- **The Buds' state after `CAP-076` (2026-10-15):** EQ `[−1, 0, 4, 2, 1.69]`, **in-ear detection off** (written 07:06:56, not switched on again), all four
  press-and-hold modes, balance Centre — restore in-ear detection if wanted (**M**).
- The Buds' state after `CAP-074`/`CAP-075`: a custom EQ `[−3.5, 0.5, 1.0, −1.0, 2.5]`, all four press-and-hold modes, balance Centre (`CAP-074-FINDINGS.md` §0;
  unchanged by `CAP-075`: 0 writes) — restore if wanted (**M**). The Owner user's stored "Noise-control channel" choice is "Take it back" and the secondary
  user's too (`CAP-075-FINDINGS.md` §4) — the default "Show a hint" is not restored.

## 2. Hardware verification debt (the app, on film)

Open after `CAP-070` (the 1.1.0 release run, `ai-sessions/0075`, `CAP-070-FINDINGS.md`); each goes into the next app run with its expected bytes. Done in
`CAP-070` and removed here: the five switches on hardware (read, off, read back, on — 89 of 89 writes ACKed), the three request forms no capture held (11 and 29
on channel 19, `15:1` on channel 21), the channel-19 balance `17:7`, the swipe between tabs (camera only), and P1 (a first install in user 10, 🟡).
**`CAP-071`** (the 1.1.1 release run, analysed in `ai-sessions/0080`) settled the update path (1.1.1 over 1.1.0 kept data, dark mode and Debug mode) and the
`SIGQUIT` source (the computer's `adb bugreportz`, `CAP-071-FINDINGS.md` §0) — both removed here. Its film 2 (BG-20 … BG-24) was not made and there was no screen
recording, so the items below stay open. **Next app run — `CAP-072` (Group BH, the 1.2.0 release run, skeleton by `ai-sessions/0082`):** the lid open before the app
starts (P8), no app use before the film (P9), Do Not Disturb on (an incoming call put a third party's number on `CAP-071`'s film), the screen recording on,
observations said aloud. `CAP-072` takes every item below except B4, the CDM picker, K5/BC-12, F-4 and the second device (`ai-sessions/0082` RESULT §I).
**`CAP-072`** (the 1.2.0 release run, analysed in `ai-sessions/0083`) settled lead L-1 (🟢, `PROTOCOL.md` §2.2a Update of 2026-10-10) and S6 (seen on the screen
recording) — both removed here; its film 2 was not made: the maintainer moved C12, S12 and T11 to the run of the **rebuilt 1.2.0** (verdict "fix first",
ADR-061, chat 2026-10-10).
**`CAP-073`** (the rebuilt 1.2.0, analysed in `ai-sessions/0085`, verdict OK — ready to release) settled the ADR-061 hold's main path and balance "Right 4"
(both removed here); the maintainer tested only the changed functionality, so T11, C12, S12, H5, Volume EQ and the worn line with in-ear detection off stay open,
with the hold's untested branches below. **Next app run:** none planned — these items go into the hardware run of the next release (`RELEASING.md` §11a).
- [x] ~~The ADR-061 hold's other branches on hardware~~ — **moot since 1.3.0** (`ai-sessions/0091`, ADR-065: the hold and the re-claim are removed; the tile, Home,
      a loss and the re-open with the tab shown are in `CAP-076` as BL-8/BL-9/BL-10/BL-24 for the stream instead). History: (`CAP-073-FINDINGS.md` §4): the tile with the noise-control tab shown (no second `SABM`, no `DISC`); Home and
      return (the `DISC` ≈ 1.5 s after Home, a new claim on return); a session loss and the ADR-044 re-open while the tab is shown (the snapshot kept); and the
      noise-control (i) after a press-and-hold ("Changed by the Buds at HH:MM:SS …" and the ADR-061 sentence) — not run in `CAP-073`. **`CAP-074` (the Owner user with
      Play services, `ai-sessions/0086`) ran the "another client closes DLCI 0x04 while the tab is shown" branch twelve times:** the hold ended "the channel was closed,
      not by this app" and nothing re-claimed — as designed; the branch works, and it is the cause of the first-hold-only symptom (`CAP-074-FINDINGS.md` §4, §8). Still
      not run: the tile, Home, a session loss / ADR-044 re-open with the tab shown, the (i) line. **`CAP-075` (Group BK, planned by `ai-sessions/0087`) takes the Play-user branch in both switch positions (BK-5 … BK-14) and, optionally, the tile, Home and the loss with the tab shown (BK-19 … BK-21).** **Run (`ai-sessions/0088`, `CAP-075-FINDINGS.md` §4): the Play-user branch in both positions done — hint only 2 cuts, take it back three chains of three re-claims; still not run: the tile, Home/return, a loss with the tab shown (BK-19 … BK-21), and BK-11 … BK-13 (a tab change, Home or the switch within 2 s of a cut — the cancel path ran once, pre-film).** **`CAP-076` (Group BL, skeleton by `ai-sessions/0089`) takes BK-11 … BK-13 again as BL-9 … BL-11 and the optional branches as BL-22 … BL-24.**

- [ ] The screen-reader text on the phone: every "—" with the description "Not read from the Buds yet" — two `uiautomator dump`s (Controls, Sound) with
      Bluetooth off; not done in `CAP-070` (no dump; "—" itself is on film). (`APP_TESTPLAN.md` T11) — not done in `CAP-071` (no film 2, no dump).
- [ ] C12 — a tap during an automatic re-open ("The app's channel is being reopened …", no write afterwards): not done in `CAP-070` (the tap came after
      "ready"). Needs the screen recording. — not done in `CAP-071` (no film 2).
- [ ] `APP_TESTPLAN.md` H5 with the *Read EQ again* button (`CAP-068` and `CAP-070` read the value back only at a reconnect); S12 (export across a
      rotation) — not done in `CAP-068`, `CAP-070`, `CAP-071` or `CAP-072` (no H5 tap; in `CAP-072` the phone was rotated before the save dialog, not during
      it). S9 (needs two Pixel Buds paired) — not planned.
- [ ] The case sound (`CAP-072-FINDINGS.md` §6, the maintainer's choice 2026-10-10): every write and read correct; a faint chime at Right-bud entries with
      "Earbuds replaced" on, none at Left-bud entries. **Proposed experiment** (no skeleton yet): the official app vs OpenControl, Earbuds replaced on/off, each bud
      separately and both, from the ear and from the hand, lid open, 30 s apart, music off, a microphone **at the case**, three repetitions per state.
- [ ] Volume EQ (what is heard) and its writes — not done in `CAP-072` (no `4:{15:…}` write).
- [ ] The worn line with in-ear detection **off** ("Worn: unknown — in-ear detection is off", `INEAR-006`) — not read in `CAP-072` (BH-9 done without a pull).
- [ ] B4 double tap (`AlreadyInProgress`) — needs the Buds forgotten and re-paired.
- [ ] `CAP-058` §9: film the CDM picker so the tapped button is identifiable (twice not identifiable in `CAP-058`).
- [ ] K5 / BC-12 (GrapheneOS Bluetooth auto-off): only in a user where the setting exists (the Owner).
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
- [ ] `CASE-008` (a short case-button press to force pairing mode) — never tested; a capture idea from `CAP-029-FINDINGS.md` §8 item 3 (moved here
      2026-10-07, `ai-sessions/0077`).
- [ ] Documentation only: re-check `CAP-015`'s film at its 15 field-18 frames for a Save tap or a screen change (`CAP-053-FINDINGS.md` §7).

- [ ] Optional, destructive, one-time: the factory-reset re-pair for comparison (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P #16) — it also resets the
      Find My Device link.
- [ ] Record Play services' *Nearby devices* permission state and the phone's Android version (Settings → About, on film) in every capture
      (`A68-CAP-23`: the Pixel 7a's version is unreconciled in `CAP-036`…`041`). `CAP-075`'s P0 asks for it on film (the first time), with Play services' version. **Not done in `CAP-075`** (the P0 film was not made; the user and Play's presence come from the logs, `CAP-075-FINDINGS.md` §0) — still open; `CAP-076`'s P0 asks again. **`CAP-076`: not on film either; read by `adb` at 07:53 instead (`ai-sessions/0092`, CAP-076-adb-checks-0092.log): Play 26.32.34, the *Nearby devices* group granted in user 0, not installed in user 10** — on film still open.
- Standing practice (kept open on purpose): every capture session gets its Capture Index row with firmware, Android and app version
  (`PROJECT_RULES.md` rules 11 and 14); every hypothesis test is logged in the capture's FINDINGS before a promotion (rule 4).

## 4. Protocol: leads and open questions

- [ ] `CAP-075-FINDINGS.md` §12 (`ai-sessions/0088`): (1) what triggers the Buds' own closes of DLCI 0x02 (17:28:47) and 0x02 + 0x04 (06:28:49) with the app
      in the background, after an `AT+BIEV` — the buds into/out of the case off screen? a run with the case moves on camera (`CASE-004`/`005`); (2) what Play's
      `RfcommEventStreamMedium` does when "Retry times remaining" reaches 0 (seen 5 → 4 once, back at 5 next time); (3) the Google app's (uid 10302)
      DLCI 0x08/0x0a traffic on the Buds' ACL in the Owner user (63 frames, the firmware string in its answers) — what it reads; (4) answered by `ai-sessions/0090`: setting 13
      (`ReadSetting 4:13`, and `SubscribeToSettingsChanges` for every change) — nothing moved at the 31 holds because OpenControl never subscribed; **built in
      1.3.0** (`ai-sessions/0091`, ADR-064/065), its hardware run `CAP-076`.

The five switches the maintainer chose (Multipoint, head gestures, the two case sounds, Volume EQ; ADR-052 … ADR-055) and the screen-reader text are built in
1.1.0 (`ai-sessions/0074`) and passed their hardware run `CAP-070` (`ai-sessions/0075`); since `ai-sessions/0076` their tests use the `CAP-070` bytes.

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
  *(`ai-sessions/0090` E-04:* every Buds `04 01 00 01 xx` in the captures is an echo < 1 s after the phone's own Ring/Stop (40 of 40, 0 unprompted); the
  official app ACKs each with `ff 01 00 02 04 01`, OpenControl never. **Proposal (a new send → its own ADR, M):** hold the claim while ringing, ACK the Buds'
  status message, clear "Ringing" when the mask reads 0; **experiment step:** a Ring stopped by touching the bud, with the claim held, filmed.)
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
- 🔴 Why Play services stops re-claiming DLCI 0x04 (`CAP-059`, `CAP-061`) — it did **not** stop in `CAP-074` (2026-10-12, Owner user): 20 of 20 recoveries 5.2–5.7 s
  after losing the channel, a retry 2.2–2.6 s after a collided recovery (`CAP-074-FINDINGS.md` §4 items 3 and 5; `ai-sessions/0040` saw 2.7–5.0 s, `CAP-059` 1.7–3.1 s —
  the Play version and the Nearby-devices permission state were never recorded); what closed the app's claims at `CAP-065` 11:10:06.49 and
  11:21:35.24, and why the Buds closed the session at 11:17:46 with both buds worn.
- 🔴 Why `CAP-064` read Settable `e8` for 28 s with both buds straight from the case (`CAP-065`/`CAP-066`: always `00`); four `e8` Notifies
  with no bud worn in `CAP-047` (1048, 2673, 3996, 4339) are not among ADR-049's samples (L68-10).
- 🔴 Why the Buds switched ANC Transparent → Active between 08:07:34 and 08:12:44 without an app command (`CAP-067` §2). 🟡 a one-bud and a two-bud mode
  (`PROTOCOL.md` §6, item of 2026-10-14, `ai-sessions/0090` E-03): Transparent at both one-worn samples, Active at both two-worn samples. **Experiment (read
  only, `ai-sessions/0090` E-03):** `ReadSetting 4:8`, `4:9`, `4:10` once, then one bud out and in with the settings stream on (pbpctrl: status 2 on the gen 1).
- 🔴 Why Android pages the Buds 0.1 s (or 3.4 ms) after an ACL drop at docking in some cases and not in others (`CAP-064` §1a, `CAP-066`);
  why Android did not re-create the ACL after an ADR-016 drop with the lid open in `CAP-063`.
- 🔴 The swapped-dock ADR-016 disconnect inconsistency between the two recordings of `CAP-047` (carried from `ai-sessions/0022`).
- 🔴 Runtime-info stream: field 3, entry 7.3; whether field 2 of an entry means "in the case" or only "charging" (`PROTOCOL.md` §4.3 Option F).
  🟡 top-level field 2 = wall clock (desk entry of 2026-10-03). *(2026-10-06, `ai-sessions/0073` §4.6:* in the official app's schema an entry's
  field 2 is the charger-type enum `NOT_CHARGING(1)`/`WIRED(2)`/`GENERIC_QI(3)`; top-level fields 2 and 3 are not in that schema; 7.2 ↔ Left,
  7.1 ↔ Right in the code too. Still open: an empty-case test, field 3, 7.3 — per the trace "LL mode".)
- 🔴 `CAP-056`: what makes the Buds close DLCI 0x02 on a wear change; the pause route with GSND closed; Settable with in-ear detection off and
  no bud worn (`PROTOCOL.md` §6). `CAP-064` §10: the Buds closed the session 13 s after a re-insertion with in-ear detection off, not at four other
  wear changes.
- 🔴 `CAP-069` §15: `01 09 00 04 0a 02 0b 06` on DLCI 0x0a at wear changes (not the assistant's `0a 01 03` of its §3); 🟡 `CAP-068` §5 losses #11/#13 =
  a bud out of / into an ear during the ring (head not in view); 🔴 `CAP-058` §9: whether SDP UUID `3a046f6d-…` is published by any firmware or only
  used by another product.
- 🔴 Settings 23, 24, 26, 30, 31, 32 (answered by the Buds, no writer or reader in app 1.0.955078536) and 39 (asked by 1.0.990706425): what they
  are (`ai-sessions/0073` §4.2; `DESKRESEARCH_FINDINGS.md` 2026-10-06). 🟡 the ANC taps filmed in the official app never produced a `WriteSetting`
  of setting 13, although the app's own ANC row writes only that (`ai-sessions/0073` §4.9) — in the next official-app capture, film which
  control is tapped (the app's row or the Play-services/Settings one). 🟡 what the Buds mean by the "primary" bud of the announcement (field 6;
  the app-side routing is answered, `PROTOCOL.md` §2.2a note of 2026-10-06).
- [ ] **Experiment (`ai-sessions/0090` E-01, needs its own approval — a new write):** `WriteSetting 4:{13:n}` from a debug build — n = 1…4 with the
  `RESPONSE` status, the `08 13` Notify on DLCI 0x04 (to whom, if Play holds it), the audible change; variants with one bud worn and with Settable `00`. A
  positive result needs a `PROTOCOL.md` entry first and its own ADR (like ADR-045). Not on any wire yet (0 in 96 logs).
- [ ] **Experiment (`ai-sessions/0090` E-12):** a read-only `SubscribeToOobeActions` **without** OOBE mode (setting 6 is never written, `ARCHITECTURE.md` §5a
  rule) — do per-bud `LEFT_ON_HEAD` … `RIGHT_OFF_HEAD` events (`qhp` 10–13) arrive? Would refine ADR-059's "probably worn" (never per bud).
- Idea, low priority (`ai-sessions/0090` E-17): an explicit `GetSoftwareInfo` as a fallback after `MAESTRO_ANNOUNCE_WAIT_MS` — one MAESTRO open in 523 stayed
  open without an announcement (`CAP-050` 17702, where nothing was answered either), so it would rarely help.
- 🟡 Hearable Controls MAC not enforced (`PROTOCOL.md` §4.1): a firmware that starts enforcing it would NAK with reason `0x03`.
- 🔴 `CAP-069` leftovers: why a bud's charging bit stayed set ≈ 12 s after it left the case (7358 → 7404); why the phone re-opened GSND AUDIO on ACL 3
  (8157 → 8271); Device Information `03 0b` (FHN EID, out of scope) with length 25; field 18 after the Save button (one sample, 6050 → `CAP-053`).
- 🔴 `CAP-072` §4: why the Buds kept Settable `e8` ≥ 42 s with both buds on a table (17:51:19 → 17:52:00.9) while `CAP-065`/`CAP-066` read `00` within 2.6 s.
- 🔴 Remaining battery time (Fast Pair Device Information code `0x04`): never seen on the wire. Ring "both" (`0x03`, `FIND-004`): never sent, never
  captured — sending it needs its own ADR. (The Device Action page, re-fetched 2026-10-14 by `ai-sessions/0090`, still defines `0x03` "Ring both left and
  right"; one ACK per command; a filmed test with the buds out of the ears; the Case has no receiver of its own — pbpctrl `README.md:52`.) Spatial audio / LE Audio (`SPATIAL-001`, `LEAUDIO-001`): not captured.
- 🔴 `PROTOCOL.md` §5.2 steps 4 and 6 of the connection lifecycle (handshake content order, user-command timing); DLCI 0x08's protocol identity.
- 🔴 Why Android's CDM picker listed nothing twice and then offered the bonded Buds directly (`CAP-068-FINDINGS.md` §8).
- 🔴 `CAP-013`'s second BLE link (not re-examined; `CAP-016`'s was a heart-rate wearable — `PROTOCOL.md` §6, Update of 2026-10-03).
- Candidates without a capture scenario: Audio switch (`SWITCH-001`), Hearing wellness (`WELL-001`), eartip fit test (`FIT-001`).
  `maestro_pw.Dosimeter`: values on the wire, meaning 🟡; a display is out of scope (**M**, 2026-09-30). **Volume-level notifications** studied in
  `ai-sessions/0082` (item 4): the phone decides (the official app's worker, 67 % of a 7-day constant, once per 7 days; field 21 only gates it; the Buds push
  nothing) — draft ADR-060 (proposed, not accepted) and the 🟡 that block it; a capture with a sound-level meter (`WELL-002`, no Group yet) comes first.

## 5. App: later work

- [x] **Done 2026-10-14 (`ai-sessions/0091`, branch `feature/settings-stream`): ADR-064 built as 1.3.0** — the stream feeding every read setting, ADR-061/062/063
      superseded by ADR-065 (the hold, the re-claim, the card and the Play line removed), the cause line rewritten, `CAP-076` rewritten for 1.3.0; mutation
      checks M1–M5 caught. **Next:** the maintainer's push, PR and CI; then `RELEASING.md` B1–B3 for 1.3.0 and `CAP-076` (C1–C2), analysed by a CAPTURE
      session (C3/C4). Deferred documentation of 0091: none beyond the items in this file. The prompt's earlier outline (accepted 2026-10-14, `ai-sessions/0090`): one `SubscribeToSettingsChanges` and one `ReadSetting 4:13`
      per MAESTRO session, field 13 only, the mode shown with "Changed by the Buds" without a Message Stream claim; fixtures from `CAP-051` 1177/1755 and `CAP-056`
      1451; then its hardware run (skeleton written by that session — the maintainer's choice, chat 2026-10-14): both users, holds with the noise-control tab
      closed, a wear-change re-home and the re-open's read; optionally the read-only `ReadSetting 4:8/9/10` (§4 — **not** in the 1.3.0 run, the maintainer's
      choice in chat 2026-10-14; a later session may add it). The retirement of ADR-061/062's hold and re-claim is ADR-065 (done).
- [ ] **The noise-control channel in the Play user — after 1.3.0 (ADR-065: the re-claim is gone, the holds no longer depend on the channel):** what remains is
      the per-tap collision with Play's claim (`CAP-075-FINDINGS.md` §5; 1.3.0's known issue) — P3 a shorter attempt-2 latency for the on-demand channel (§9:
      attempt 2 0.7–1.0 s after attempt 1's refusal) is still open (**M**). Whether repeated collisions drive Play's "Retry times remaining" to 0 and what Play
      does then stays ❓ (§4). **`CAP-076` (§5.2 of its findings):** 13 taps with Play present in 12 claims — 8 refused at attempt 1 and done at attempt 2 (0.87–1.11 s later), 2 cut in their linger, 1 cut before the `Get`'s answer (a toast), 1 after it (silent, P2 below); Play's counter went 5 → 4 once (06:46:28) and back.
- [ ] **After `CAP-076` (`ai-sessions/0092`, `CAP-076-FINDINGS.md` §13; the maintainer's choice in chat 2026-10-15: no FEATURE prompt now, recorded here):**
      P1 keep "Changed since the app last saw it" across the reconnect's second, equal read (`causeOfReadAnswer` returns `null` for an equal answer; the
      snapshot's `Notify` came first in 12 of 12 sessions — §6.2) (**S**); P2 a tile tap whose claim closes after the `Get`'s answer and before its `Set`
      reports a failure (§5.3; a `Set` wait that ignores a `Notify` received before the `Set` was written; a test with the delay) (**S–M**); P3 log a same-mode
      push as "unchanged" (§4.4) (**XS**); P4 the two packets per change with both buds out — one Connect on a fresh ACL, then the official app on the same
      ACL (§4.3, a capture); P5 the shorter attempt-2 latency (above); P6 a mode line on the Connection tab (§4.2) (**S**). The seven CloseGuard lines of
      `CAP-076` A1 (06:47:10/44, §0) — which sockets (❓).
- [ ] **After the toolchain upgrade of `ai-sessions/0078`** (Gradle 9.7.1, AGP 9.3.3, Kotlin 2.4.20, BOM 2026.09.00, compileSdk 37):
      (a) hardware-tested in `CAP-071` (`ai-sessions/0080`): no behaviour change found — the same reads, request bytes, answers, re-opens and screens as 1.1.0;
      (b) **M** — raising `targetSdk` (lint `OldTargetApi` now reports 34) is its own session with a hardware run: Android 15 enforces edge-to-edge,
      16 removes the opt-out and turns on predictive back, 17 makes an RFCOMM `read()` return −1 on close (quotes in that RESULT §A.4);
      (c) **M** — `TabRow` is deprecated in material3 1.4.0 and kept with `@Suppress("DEPRECATION")` (`SettingsMenu.kt`); `PrimaryTabRow` narrows the
      indicator to the label, `SecondaryTabRow` turns the selected label OnSurface — a visual choice;
      (d) on the next BOM bump re-check the `TopAppBar` opt-in (`OpenControlNavHost.kt`): still needed because `TopAppBarScrollBehavior` is experimental;
      (e) Gradle 9.8 / AGP 9.4 once Kotlin's "fully supported" range includes them (2.4.20: Gradle ≤ 9.7.0, AGP ≤ 9.3.1);
      (f) one Gradle-10 deprecation remains, inside AGP (`TestFixturesUtil.kt:38`, "Using a Project object as a dependency notation") — re-check on the next
      AGP release.
- [ ] **M** — repository settings (proposals from `ai-sessions/0079` §A.5, nothing changed): (a) make CI a **required status check** on `main` — first give
      `.github/workflows/android.yml` and `.github/workflows/lint-docs.yml` a job that always reports (their path filters skip pull requests that touch neither, and a required check
      that never runs stays "pending" for ever); (b) **allow only merge commits** (the repository also allows squash and rebase merges, while `RELEASING.md` D1
      requires a merge commit).
- [ ] 🟡 **Accessibility, found while testing `ai-sessions/0074`:** a switch row's label and its `Switch` are separate accessibility nodes (siblings in the
      card), so a screen reader may announce "switch, on" without the setting's name — not tested on a phone. A merged row (`Modifier.toggleable` with
      `Role.Switch` on the row, the `Switch` without its own click) would fix it; it changes every switch's semantics (and the tests that find a switch by its
      sibling label) — **M** decides whether and when.
- [ ] `CAP-064` §10: whether the stale "100 %⚡" (I-4a) is drawn dimmed — a `:ui` Robolectric screenshot test settles it without hardware.
- [x] **M — the 1.2.1 release steps:** done — PR #39 merged (the build commit `31ee183`), `CAP-075` run and analysed (`ai-sessions/0088`, PR #44), verdict
      "no heavy issue", tag `v1.2.1` and the release published 2026-10-13 (approved in chat); E1–E4 by the pull request of `release/1.2.1-after` (#45); D4 done 2026-10-13 (Obtainium, §1).
- [ ] **1.3.0 candidate (M):** volume-level notifications from `maestro_pw.Dosimeter` — draft ADR-060 (`DECISIONS.md`, proposed, not accepted; `ai-sessions/0082`
      RESULT §C.4): two new requests per Connect (`SubscribeToLiveDb`, `FetchDailySummaries`), the 7-day dose on the phone, a notification channel, the
      field-21 switch. Blocked until `WELL-002` (the stream against a sound-level meter on film) and the other 🟡 of the draft are 🟢.
- [ ] **String resources:** the UI texts are Kotlin literals; moving them to `strings.xml` is the precondition for any translation.
- [ ] **Instrumented tests** — none exist: `OsConnectionObserver`, `BudsForegroundService`/`AncTileService`, the `BluetoothDevice`-dependent part
      of `BudsRepositoryImpl.connect()` and all of `:app` are covered only by pure-function and Robolectric tests.
- [ ] Third-party notices on the Info tab (releases carry `THIRD_PARTY_NOTICES.txt`; bundling it like the licence text is a possible later step).
- [ ] Auto-connect on lid-open: not possible from any event the app sees (`CAP-064-FINDINGS.md` §1); **M** chose "Nothing now" (2026-10-01).
- [ ] Undecided, each needs its own ADR (**M**): a background session via CDM device presence (`ARCHITECTURE.md` §6.0b (b)); a per-channel
      "degraded" state; the BLE battery advertisement scan (ADR-006's bounded exception). (`SubscribeToSettingsChanges` is decided by ADR-064, 2026-10-14.) (The settings that were
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
- [ ] Optional (**M**, project law): a dated note in `AGENTS.md` §1 pointing to ADR-050 (the Info links hand a URL to the browser). (ADR-063's note was added
      to §1 on 2026-10-13, `ai-sessions/0089`, the maintainer's choice in chat.)
- [ ] **Shorter hardware runs** (`ai-sessions/0083` RESULT "Testing burden", the maintainer's choice 2026-10-10: *"Both tiers + helper + card"*): the two tiers are
      in `RELEASING.md` §11a and `APP_TESTPLAN.md` since `ai-sessions/0084`; still to do (its own session): a one-page run card (the maintainer chose to write it
      with the helper, chat 2026-10-10) and a `scripts/` helper that pulls the HCI log and the exports (bugreport), names the files, decodes the session and
      compares each request with its fixture.
- [ ] Serials still written beyond the first 4 + last 2 characters (ADR-058 item 5; `PROTOCOL.md` done in `ai-sessions/0084`, the maintainer chose "PROTOCOL.md
      only"): in full in `REVERSE_ENGINEERING.md` and seven `ai-sessions` files (0003 RESULT, 0017 PROMPT and RESULT, 0045, 0058, 0068 RESULTs, 0082 PROMPT); the
      last six characters in `DECISIONS.md` ADR-058's Context and in several `ai-sessions` files (`grep -rlI` for the strings' tails) — **M** decides whether the
      historical session logs are edited.

Corrections found by `ai-sessions/0068` and not yet made (sub-review items, each with its check in that RESULT's appendix):

- [ ] Slips still open after `ai-sessions/0081` (interpretive — with the rule-9a session): `CAP-008-FINDINGS.md:381` "eleven silent captures";
      `CAP-033-EVENT-NOTES.md:146–147`; `CAP-047-FINDINGS.md:305–306`, `:379–380`; `CAP-050-FINDINGS.md:81–97`, `:204`, `:210` (re-open/sample
      counts); `CAP-049` (capinfos start, the quoted tail) — `ai-sessions/0068` appendix capA/capB/capD. The 27 arithmetic slips of that list were
      corrected in place by `ai-sessions/0081` (its ledger §B.C).
- [ ] Folder-name end times: `CAP-036` (film ends 06:41:16), `CAP-041` (17:17:40), `CAP-042` (named after the log, not the film). A rename breaks
      every link to the folder — do it with the index restructure.
- [ ] EVENT-NOTES left as templates: unticked "Next steps" (`CAP-037`…`042`), leftover placeholders (`CAP-035`, `CAP-036`), the placeholder timeline
      of `CAP-041`, `CAP-040`'s timeline without its wire events (`A68-CAP-23` [24]).
- [ ] Rule 9a: FINDINGS with more than one dated layer (`CAP-036` 7+, `CAP-041` 5, `CAP-032`/`034` 4, `CAP-017` 5, and others) — fold into
      current text; FINDINGS without a status banner that still need none checked: `CAP-006`…`008`, `011`…`013`, `019`, `020`, `023`, `025`,
      `027`, `029`, `031`…`033`, `035`, `043`, `044`, `051`.
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
- [ ] Reading owed by earlier sessions (`A68-SES-03`): `ai-sessions/0061` did not read `APP_TESTPLAN.md` and several app sources with their tests in
      full; `ai-sessions/0063` read its prompt's full-reading list only in part (its §B) — carry into the next MAINTENANCE reading pass (the rule-9a session).
- [ ] Reading owed by `ai-sessions/0089` (its RESULT "Files read"): `RELEASING.md`, the `ai-sessions/0087`/`0088` PROMPT and RESULT files, `CAP-074-FINDINGS.md`
      §4/§8/§9, `CAP-073-FINDINGS.md` §4 and `PROTOCOL.md` §2.3/§8 were not read in full (none of them was changed; the facts used were re-derived from `CAP-075`)
      — carry into the next reading pass.
- [ ] Reading owed by `ai-sessions/0090` (its RESULT "Files read"): `PROTOCOL.md`, `ARCHITECTURE.md` and `REVERSE_ENGINEERING.md` were read only in the sections
      named by its prompt (and the sections it changed), not in full; the FINDINGS of `CAP-038`, `CAP-045`, `CAP-064`, `CAP-069`, `CAP-075` only at the cited frames
      — carry into the next reading pass.
- Order decided 2026-10-09 (maintainer, chat, `ai-sessions/0081`): all of the above after the 1.2.0 work — first the `ai-sessions/0073` reading and the census
  recount (one CROSSCHECK session), then the rule-9a folds with the slip residue (MAINTENANCE), then the EVENT-NOTES templates (MAINTENANCE), the
  folder renames with the index restructure.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/TODO.md - https://tedsluis.github.io/opencontrolpixelbudspro2/TODO
