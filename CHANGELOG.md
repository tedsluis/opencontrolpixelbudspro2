# CHANGELOG.md

All notable changes to this project are documented in this file. Format loosely
based on [Keep a Changelog](https://keepachangelog.com/).

The first release, 1.0.0, was published on 2026-10-03 ([v1.0.0](https://github.com/tedsluis/opencontrolpixelbudspro2/releases/tag/v1.0.0), build commit
`8d8af4b`), after `CAP-067`, the hardware run of the signed APK (`ai-sessions/0067`). Its block below is written for users. Everything before it — documentation, tooling and the protocol reconstruction that led to the app —
is kept unchanged under "History before 1.0.0". See `TODO.md` for current status and `RELEASING.md` for how a release is made.

## [Unreleased]

## [1.0.1] - not yet released

A hotfix for 1.0.0 (`ai-sessions/0069`, from the `ai-sessions/0068` audit). Nothing new is sent to the Buds: the same commands, on the same channels.
To be tested on hardware as `CAP-068` before it is published (`RELEASING.md` §11).

### Fixed

- A frame from the Buds with an oversized length field could stop the app's reader, or loop without end. Both length checks are corrected, and a
  reader that fails now costs only that one frame.
- After Disconnect, or after the connection was lost, the last connection's values could stay on screen as if they were current. They now stay
  visible but dimmed and marked "from the last connection" — the noise-control mode, the equalizer, the settings and the battery values — and the
  Quick Settings tile and the notification no longer show a mode from the last connection.
- A noise-control report from the Buds could be dropped when two arrived close together; the newest one is now always kept.
- With more than one pair of Pixel Buds paired to the phone, the app picked one without asking. It now asks you to choose with *Pair a device*.
- The connect error no longer suggests that another app is using the Buds when they are simply out of reach or in the closed case (the known
  issue of 1.0.0). Other messages now say what was observed and name a cause only as a possibility; "Something went wrong" is gone.
- A tap on the Quick Settings tile that could not change the mode now always says why.
- A permission revoked while connected, or another error from Android's Bluetooth stack, no longer escapes as a crash.
- Rotating the phone while the "save as" dialog of the debug-log export was open lost the export.

### Added

- **Use different Buds** (Settings): forgets which Buds the app controls, so you can pick others with *Pair a device*. The Bluetooth pairing in
  Android stays.
- Equalizer preset **Flat** (all bands 0.0).
- A value that has not been read from the Buds shows "—" instead of a default ("0.0", "Centre").

### Known limits

- English only. A screen reader still announces an unread switch as "off" (the visible "—" is for sighted users only so far).
- Not hardware-verified at the time of writing: everything above — `CAP-068` is the run for it.

## [1.0.0] - 2026-10-03

The first public release of OpenControl for Pixel Buds Pro 2 — an independent, open-source Android app for Google Pixel Buds Pro 2 that works without
the official app and without Google Play services; no `INTERNET` permission, no location permission, no account.

### Added

- Noise control (Noise cancellation, Adaptive, Transparency, Off) and a Quick Settings tile.
- Equalizer: five bands and the presets.
- Battery: Left and Right (with "charging in the case") and the Case.
- Find My Buds: ring the Left or Right bud.
- Controls: touch controls on/off, press and hold per bud, and the noise-control modes press and hold cycles through.
- Sound: balance, mono audio, conversation detection; in-ear detection on/off.
- Settings: dark mode, a Debug screen with a debug-log export, and an Info tab (the app's build, the Buds' firmware, the licence, links to the README and
  the issue tracker).
- Read-only Safe Mode: no setting is changed unless the Buds announce the verified firmware (`release_5.203`).

### Requirements and known limits

- Android 14 (API 34) or newer. Tested on a Pixel 9a with GrapheneOS (Android 17) and Buds firmware `release_5.203` only; other firmware ⇒ Safe Mode.
- Not included: ringing the Case or both buds, firmware updates, multipoint management, head gestures, case sounds, anything needing a Google account.
- Updates are manual (GitHub Releases); the app never checks for them.

### Known issues

- If the Buds cannot be reached (for example the case is closed), the connect error wrongly suggests that another app such as Google Play services is
  using them. Open the case and tap Retry. (In the release notes since publication; recorded here on 2026-10-03.)
- After Disconnect or a lost connection, the battery values and the noise-control mode of the last connection can stay on screen as if current.
- With more than one pair of Pixel Buds paired to the phone, the app picks one without asking.
- A malformed frame from the Buds with an oversized length field can stop the app (found by the `ai-sessions/0068` audit; not seen on hardware).

All four are fixed in 1.0.1.

## History before 1.0.0

*Kept as written: one entry per work session, by kind and then by date (the three entries that were out of date order were moved into place on
2026-10-03, `ai-sessions/0069`). It is project history, not a list of user-visible changes — those are in the release blocks above.*

### Added

- Core documentation and guardrails (`AGENTS.md`, `PROJECT_RULES.md`,
  `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `README.md`).
- Fedora 44 workstation setup (`WORKSTATION_PREPARATIONS.md`): Claude Code,
  Antigravity, Java 21, Kotlin/SDKMAN, Wireshark, Android SDK/adb, JADX,
  apktool.
- Bluetooth HCI snoop capture procedure (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`).
- Test plan mapping user/app/hardware actions to expected Bluetooth traffic,
  validated against official screenshots/support docs
  (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`).
- Reference screenshots for the official app and web companion app.
- `SECURITY.md`, `CONTRIBUTING.md`, `DESKRESEARCH_FINDINGS.md`.
- **2026-09-07 … 2026-09-18 (`ai-sessions/0001`–`0032`), backfilled 2026-09-24 (`ai-sessions/0045`, 0044 finding C-1) — one line per
  session; the RESULT files hold the detail:**
  - `0001` (09-07) deep V1-without-GMS cross-check (2nd pass): GMS Fast Pair AIDL boundary found, `MaestroDeviceSettingsProviderService`
    and `MaestroEndpointService` catalogued. `0002` (09-08) session-logging rules (`AI_SESSION_LOG_PROCEDURE.md` §4a/§8), `0001`
    sign-off applied (ADR-025 Update: `qhr` fields 11/15 → FACT). `0003` (09-08) V1 gap scan: external-spec re-checks, field-18
    Save-button trace, `qhr` field 2 → FACT (ADR-019 Update). `0004` (09-09) `TODO.md` Phase 3 cleanup, UUID-register cross-check,
    `PROTOCOL.md` §5.2 channel-opening order (🟡, maintainer-reviewed). `0005`/`0006` (09-09) skeletons for `CAP-018`,
    `CAP-026`, `CAP-028`–`CAP-030`, `CAP-043`–`CAP-051`.
  - `0007` (09-11, Gemini) review of the APK catalog; `0008` (09-11) full validation of `0007` (about half its line citations wrong);
    `0009` (09-11) field-19 dual-write-path clarification. `0010` (09-12) video+log analysis of eight captures; `0011` (09-12,
    Gemini) review of every capture; `0012` (09-12) validation of `0011` (core decodes held, 5 citation errors). `0013` (09-13)
    ADR-026 (volume balance), ADR-027 (Find Case/"both" out of scope), ADR-028 (Hilt), first Android project; `0014` (09-13)
    ADR-029 (API 34). `0015` (09-13) ADR-024 counter-example Update. `0016` (09-13) `CAP-043`/`CAP-044` analysis. `0017`
    (09-13) Phase 1–3 close-out, designed `CAP-053`–`CAP-057`.
  - `0018`–`0020` (09-14) video-only analyses of `CAP-047`/`CAP-050`/`CAP-051`; `0021`/`0022` (09-15) their log analyses;
    `0023` (09-15) APK cross-validation of those captures (GSND cross-vendor lead, `gjv.p()` caller found). `0024` (09-16)
    `lambda_dispatcher_resolver` run and RE-tooling audit; `0025` (09-16) four new RE tools (`structural_index`,
    `schema_batch_extractor`, `uuid_ble_context`, `limited_dataflow`) and their first findings; `0026`/`0028`/`0029` (09-17)
    sign-off records for `0024`/`0025`/`0027`; `0027` (09-17) `MaestroEndpointService` resolved (empty map),
    `BluetoothPriorityReceiver` traced to `dcservice`. `0030` (09-17) static-analysis worklist; `0031` (09-18) full sign-off
    round: ADR-030 (CTKD), ADR-031 (battery message identity), `CASE-009`; `0032` (09-18) consistency-pass close-out.
- **2026-09-18 (`ai-sessions/0033`): the v1 app, implemented end to end for every genuinely
  FACT-and-unblocked feature.** EQ (`EqFrameEncoder`/`EqFrameDecoder`, DLCI 0x02, byte layout
  re-derived directly from `CAP-015` fixtures), Find My Buds Left/Right
  (`RingFrameEncoder`/`RingFrameDecoder`, DLCI 0x04, `CAP-025` fixtures), and Battery via HFP
  (`HfpAtParser`/`HfpBatteryReader`) all implemented and unit-tested against real capture bytes,
  alongside a new `CodecRouter` doing per-DLCI stream buffering/frame-boundary detection that didn't
  exist in code before this session. `BudsRepositoryImpl` wires all of it to the domain layer,
  implementing `ARCHITECTURE.md` §3.1's per-feature state-reconciliation rules. A 5-screen Compose UI
  (Connection, ANC, EQ, Find My Buds, Debug) via `navigation-compose`. `BleLogger` (always-on
  connection-state logging, Debug-Mode-gated hex dumps, an in-app ring buffer) and
  `DebugSettingsStore` (AndroidX DataStore-persisted Debug Mode toggle). `CompanionDeviceManager`
  pairing (`BudsCompanionPairing`), a `BluetoothAdapter` state observer, and a real multi-DLCI
  `RfcommBudsTransport` (one `BluetoothSocket` per DLCI, resolving the prior session's own
  `// TODO(verify)`), plus `BudsForegroundService`. 1232 unit tests (up from 232), 0 failures;
  `./gradlew assembleDebug testDebugUnitTest test lint` all pass, producing a real debug APK. **None
  of this is verified against real Pixel Buds Pro 2 hardware** — see
  `ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` Phase 8's capability table for the exact,
  feature-by-feature compiles/unit-tested/hardware-verified breakdown.

- **2026-09-20 (`ai-sessions/0041`): pairing/permission fixes, Android-state mirroring, charging flag, EQ read.** Fixed the in-app pairing failure
  "Could not resolve the selected device" (CDM's lower-case `MacAddress` was passed to `getRemoteDevice`, which requires upper-case; association reuse and
  duplicate clean-up, already-bonded is success, distinct bond-failure reasons); added the runtime-permission flow the app never had (its absence made a
  cleared-data app see "no bonded device"); the Connection screen now mirrors Android's Bluetooth state (`OsConnectionObserver`, `DeviceStatus`); the battery
  decoder reads the charging flag (ADR-033 update, maintainer-accepted); DLCI 0x02 is decoded as pw_hdlc + pw_rpc (ADR-034, `PROTOCOL.md` §2.2a/§4.2 promotions,
  a codec correction: the old "correlation byte" was the `channel_id`, defaulted to 0), the EQ is read at Connect and write results are surfaced; `HfpBatteryReader`
  logs every headset broadcast action (removal is a later session). `scripts/pwrpc_decode.py` gained frame numbers, `--eq`, `--channels` and a corrected packet-type
  table. Tests: 1335 (1281 before), 0 failures; not hardware-verified.
- **2026-09-20 (`ai-sessions/0042`): first hardware run (`CAP-059`) analysed; Android-state mirror and bond reporting fixed.** The two camera films, HCI snoop log,
  system log, app exports and screenshots were correlated frame by frame (`DESKRESEARCH_FINDINGS.md`, second 2026-09-20 entry). Findings: pairing, Connect, ANC
  (4/4 taps), EQ read/write (all answered, writes persisted), Find (audible on the film's microphone) and battery with charging work; **the Connection card was wrong for
  minutes** (the `RECEIVER_NOT_EXPORTED` link observer received no system broadcasts, and an unknown link was rendered as "not connected") and **a bond that succeeded in
  1.1 s was reported as a 45 s timeout**. Fixed with tests: `RECEIVER_EXPORTED` for the protected Bluetooth broadcasts plus event-driven re-reads (resume, bond change,
  session change), `AndroidLine.UNKNOWN`, the bond outcome read from the stack, a pairing re-entry guard (one tap had started two CDM requests 82 ms apart), a cancelled bond
  timeout, one log line per link change with its trigger, "why did the session end" log lines (two of the three drops were the maintainer's own taps), a 1000-line
  buffer. **Decided by the maintainer in chat and done in the same session:** `HfpBatteryReader` removed (`AT+BIEV` is on the wire but not deliverable to an app); the Case
  battery is read from DLCI 0x08 by an on-demand, receive-only claim (**ADR-035**, `CaseBatteryFrameDecoder`; a value the Buds did not mark fresh is shown "last seen"); the
  read-only DLCI 0x02 `ReadSetting` unblock is written as **ADR-036** (nothing implemented); a firmware line, a "buds in the case / out" line, a Find "ringing" state and an ANC
  Quick Settings tile were added; automatic session opening was declined (the card mirrors Android). Three protocol items were promoted to FACT with the maintainer's chat approval
  (`PROTOCOL.md` §2.2a, §4.1, §4.2; ADR-024/ADR-034 updates). 1357 tests, lint 0 errors, four guards mutation-checked. Nothing is hardware-verified.
- **2026-09-22 (`ai-sessions/0043`): `CAP-059`/`CAP-060` migrated into `captures/` (ADR-037), `CAP-060` fully analysed, Case-battery contention fix, ANC tile
  discoverability, "last known" replaced with real timestamps, swipe navigation between tabs.** Two full OpenControl hardware-capture sessions, previously kept
  local-only under `android/logs/`, are now committed as `captures/CAP-059-…`/`CAP-060-…` (real device identifiers per ADR-010; a burned-in street-address overlay
  on the `CAP-059` recordings was cropped out before commit, maintainer-reviewed). `CAP-060` (six connection drops, `Case battery not read: Timeout` 8/8) was
  analysed end to end: three distinct drop mechanisms now characterized (Buds-initiated RFCOMM-only closure, a Settings-panel-triggered full disconnect, and a
  third ACL-level pattern correlated with physical handling in 2 of 3 instances); the Buds push Case battery unprompted, confirmed 🟢 FACT (`PROTOCOL.md` §4.3
  Option E) — the real blocker is DLCI 0x08 contention with Google Play services, not a missing request, so `readCaseBattery` now retries once on contention
  (**ADR-038**) — **corrected 2026-09-24 (`ai-sessions/0045`): every post-open push answers a phone-side `0e 04`; ADR-039 adds the request**; the per-earbud dock-state candidate (`Group 0x04 Code 0x12`) showed no correlation with handling in this capture (still 🔴 open); the ANC Quick
  Settings tile's code was already correct — it was simply never added to the panel — so the app now offers `StatusBarManager.requestAddTileService()` and a
  redrawn tile icon. Every "(last known)"/"last seen" qualifier is now an actual wall-clock timestamp, threaded through the same per-feature flows with no new
  polling (`ancModeUpdatedAt`/`eqProfileUpdatedAt`/`batteryStatusUpdatedAt`/`dockStateUpdatedAt`). The five tabs now support left/right swipe navigation
  (`HorizontalPager`) kept in sync with the bottom nav bar and the existing single-top back-stack semantics (`ai-sessions/0037`). A session-loss message that
  stayed generic even when Android's own link state already explained the cause now uses that state. 1359 tests, lint 0 errors. Nothing is hardware-verified.
- **2026-09-24 (`ai-sessions/0045`): every 0044 audit finding validated and processed; ADR-039–042; Case-battery request, Safe Mode gate, ACK/NAK
  handling; CI workflow; `lint_docs.py` clean.** Each finding of `ai-sessions/0044` was re-verified against captures, code and official specs
  (none trusted from 0044) and processed or rejected with a reason, with one maintainer checkpoint in chat. Re-deriving every DLCI 0x08 open in
  `CAP-059`/`CAP-060` showed that the 2026-09-22 "Case battery pushed unprompted" 🟢 FACT was wrong in its essential point: 13/13 Play-services
  opens send `0e 04 00 00` before the first `0e 01` push, 8/8 of OpenControl's receive-only claims got nothing — corrected in place with a dated
  note (`PROTOCOL.md` §4.3 Option E, maintainer-approved). **ADR-039** makes the Case claim send that request (release 0x04 first, nothing else on
  0x08); **ADR-040** records HFP battery as not app-consumable (AGENTS.md §5 note); **ADR-041** the hand-written codec with no protobuf runtime
  (AGENTS.md §4 note); **ADR-042** a Safe Mode gate (verified firmware `release_5.203` + Fast Pair Model ID `da2db1`) for DLCI 0x04 commands and EQ
  writes. Dated Updates on ADR-006/009/015/023/024/025/027/031/035/037/038. App: Message Stream ACK/NAK as one routed type with the spec's NAK
  reasons (ANC Set now reports ACK/NAK/timeout), splitter reset on channel close plus overflow guards, `NotPaired`/`CommandRejected` errors, the
  foreground service driven from the application scope, `BLUETOOTH_SCAN` removed (unused), dock state shown as provisional, `FakeBudsTransport` moved
  to test fixtures. `PROTOCOL.md`: session-nonce FACT, Hearable Controls MAC/nonce 🟡, the connect burst resolved (frame 1423 `GetHardwareInfo`).
  Tooling: `.github/workflows/android.yml` (build, tests, lint, no-INTERNET check on source and merged manifests), `scripts/lint_docs.py` exit 0
  (excludes `.venv`), capture `.txt`/`.png`/`.jpg` moved to Git LFS, executable bits dropped from capture files, `pwrpc_decode.py` names the
  connect-burst RPCs. 0044's coverage gaps closed (capture folders, session history per §4a, remaining Kotlin/tests, RFCOMM and
  `RECEIVER_EXPORTED` checked against official texts; ADR-017 note in `REVERSE_ENGINEERING.md`, maintainer-approved). 1533 tests, lint 0 errors.
  Nothing is hardware-verified — re-test instructions in `ai-sessions/0045_MAINTENANCE_RESULT_2026_09_24.md`.
- **2026-09-24 (`ai-sessions/0046`): `CAP-061` (the 0045 build's first hardware run) analysed end to end; Safe Mode, Case and dock-line
  defects root-caused and fixed; ADR-043.** Video (every frame scanned for privacy; clock offset measured at both ends), HCI log (every Buds
  packet classified, DLCI 0x02/0x04/0x08 decoded per open), debug export, logcat and system log correlated (`CAP-061-EVENT-NOTES.md`,
  `CAP-061-FINDINGS.md`). **Safe Mode / ANC / EQ / Find:** the Buds' firmware announcement carries a fixed64 field (140/140 announcements, 44
  captures); the app's protobuf reader rejected it, so the firmware list was always empty and the ADR-042 gate refused every write on the
  verified firmware — nothing was ever sent. Fixed (`Proto.fields` skips fixed64/fixed32), tests now use the real `CAP-061` frame 1508 (serial
  redacted). **Case:** the app's DLCI 0x08 claim with `0e 04` got no answer (20/20 opens) and knocked the other client off the channel; with the
  maintainer's approval the Case now comes from DLCI 0x02 `SubscribeRuntimeInfo` entry 6.1 (🟢 FACT, 13/13 captures; **ADR-043**, supersedes the
  0x08 claim of ADR-035/038/039; ADR-039 Update). **Dock line** removed (a premature "both in the case" with one bud out, ADR-024 Update); the
  ANC-tile button now says what Android answered. `PROTOCOL.md`: announcement structure 🟢, Option F 🟢, Option E refutation, 🟡 the Google app's
  Assistant-headphones service as the other DLCI 0x08/0x0a owner. Tests `:data` 1480 / `:hardware` 49 / `:domain` 11, lint clean except the 2 old
  `:app` warnings, three mutation checks caught. Not hardware-verified — re-test instructions in `ai-sessions/0046_FEATURE_RESULT_2026_09_24.md`.
- **2026-09-25 (`ai-sessions/0047`): `CAP-062` (the `APP_TESTPLAN.md` run of the 0046 build) analysed end to end; no app change; ADR-044.**
  Video (every frame scanned for privacy, kept unblurred on the maintainer's choice; clock offset measured at both ends; audio used for the ring),
  HCI log (every Buds packet classified, DLCI 0x02/0x04/0x08 decoded per open), debug export, logcat and system log correlated
  (`CAP-062-EVENT-NOTES.md`, `CAP-062-FINDINGS.md`). The 0046 fixes hold on hardware (R1–R7: firmware line, ANC/EQ/Find sent and answered, Case
  from `SubscribeRuntimeInfo` = 60 %, no DLCI 0x08 activity). The maintainer's five observations answered: ANC is refused by the Buds (NAK `0x02`)
  whenever their `Notify` reports no settable mode — not worn, also outside the case (ADR-024 Update, 🟡); the app's session is closed by the Buds
  on every wear/dock change while Android stays connected (7 of 14 ends) — **ADR-044**: re-open automatically while the app is visible (not yet
  built); the runtime-info stream's per-bud fields tell which bud is charging in the case (🟢 FACT, 401/403 packets, 45 captures; ADR-043 Update
  unblocks the decode and a dated last-seen Case). Defects found: the ring notice is cleared by Disconnect (I4), a stale session-loss message.
  Prioritised improvement list I-1 … I-10 in the RESULT.
- **2026-09-25 (`ai-sessions/0048`): the `0047` improvements built (app), with real `CAP-062` bytes as fixtures; no protocol or ADR change.**
  I-1 (**ADR-044** as built): the app re-opens its session by itself while visible and Android shows the Buds connected — 1.5 s after a session loss,
  when Android's link comes back, on resume and at app start; one attempt per event, never in the background, off after a Disconnect tap until the next
  Connect; the re-open repeats the Connect sequence (incl. the DLCI 0x04 snapshot); no second automatic re-open within 10 s of one. I-3: no ANC `Set`
  (not even a claim) while the Buds' `Notify` reads Settable `0x00`; the ANC buttons and the tile say "ANC can only be changed while you wear the
  Buds". I-4/I-8: per-bud "charging in the case" / "not charging" from the runtime-info stream (6.x field 2, 7.x fallback, 6.x wins — `CAP-062` frame
  4500), newest charging report of either source wins, each with its own time. I-5: a packet without the Case entry keeps the Case as "last seen
  HH:MM:SS" (in memory only). I-6: the ring notice survives Disconnect ("may still be ringing" after a reconnect). I-7: the loss text follows
  Android's link *around* the loss (`classifySessionLoss`; the socket detail is identical for all 11 `CAP-062` losses) — never "likely another app".
  `OsConnectionObserver` now emits every reading (display debounced by the screen). Tests: `:data` 1509, `:hardware` 49, `:domain` 17; five mutation
  checks caught. `CAP-062` FINDINGS §7.2 / EVENT-NOTES corrected in place (the stale text lasted ≈ 1.5 s, film t = 240.5 s, not until t = 256 s).
  `APP_TESTPLAN.md` updated (C1, C3, C8, C9, E1–E5, F8, G3, G4, I4). Not hardware-verified — Group AY plan in the RESULT.
- **2026-09-25 (`ai-sessions/0049`, `0050`): an AI audit and its validation.** `0049` (an end-to-end audit by another session) was checked claim
  by claim in `0050` with full enumerations and project-wide cross checks: 16 correct, 12 partly correct, 5 wrong, 1 opinion. Its S1 "security
  flaw" (SEC-01) misread the evidence (a NAKed frame cited as ACKed; both claims are 🟡 in `PROTOCOL.md` §4.1) — no change, maintainer's choice.
  Applied: an adaptive launcher icon from the project's own ANC glyph (clears lint `MissingApplicationIcon`), a binding "fixtures are real bytes"
  rule in `AGENTS.md` §11, an EQ-audibility step for Group AY (`TODO.md`, `APP_TESTPLAN.md` §H), and two documentation defects found by the cross
  checks (`ARCHITECTURE.md` §2: a dangling "§2.4 note" pointer, also in `MainActivity.kt`, and the missing `:hardware → :domain` dependency).
- **2026-09-26/27 (`ai-sessions/0052`): `0051`'s approved proposals recorded, then the settings, the Refresh fix and the EQ layout built.**
  Maintainer-approved in chat 2026-09-26: `PROTOCOL.md` §4.5.1 — "Conversation detection" = `qhr` field 22 🟢 (OFF write `CAP-019` 1720 on film);
  §4.5.3 — "Use touch controls" both directions 🟢 (`CAP-020` 1995 on film) and the `qht` bit-order conflict recorded as 🔴; §4.5.7/§6 — balance
  persists across a reconnect 🟢 (three chains); §6 — `FE2C1238…` = Find Hub "Beacon actions" 🟢. ADR-043 Update (*Refresh battery* re-sends one
  `SubscribeRuntimeInfo`); **ADR-045** (`WriteSetting` for fields 17, 19, 22, 4, 7; field 12 gated). `CAP-019`/`020`/`022` findings rewritten in place.
  App: EQ presets in two rows (3 + 2); *Refresh battery* always on a fresh Message Stream claim (the Buds send the battery burst only on an open) with
  "No new battery reading from the Buds — try again." when none arrives, and a Case note; settings read at Connect (2, 4, 7, 17, 19, 22) and written
  (balance, mono, conversation detection on the tab renamed "Sound"; touch controls, press-and-hold per bud, the in-ear detection setting read-only on
  a new tab "Controls") — each value with its time, changed only on the Buds' OK. `SettingsCodec` byte-identical to the official writes (real
  fixtures from `CAP-019`–`022`/`036`/`062`), fuzzed. Tests: `:data` 1536, `:hardware` 49, `:domain` 17; six mutation checks caught.
  `ARCHITECTURE.md` §2.4 corrected ("5 presets"). New capture skeleton `CAP-063` (Group AY, Pixel 9a). Not hardware-verified.
- **2026-09-27: debug export no longer cut at 64 KiB; `CAP-063` video timeline; prompt 0053.** The `CAP-063` debug export ended mid-line at exactly
  65,536 bytes (the share-sheet `EXTRA_TEXT` hand-off truncated it); "Export debug log" now writes the whole log to a file the user picks in Android's
  "save as" dialog (Storage Access Framework, no permission), and the ring buffer keeps 20,000 lines (was 1,000); `BleLoggerTest` added. `.gitattributes`:
  `captures/**/*.log.last` goes to LFS. `CAP-063-EVENT-NOTES.md`: a first timeline from the film (1 fps, zoomed frames; the film's audio track is
  empty), a step-mapping table (skipped/repeated steps) and open points; the planned procedure kept as Appendix A. New prompt
  `ai-sessions/0053_CAPTURE_PROMPT_2026_09_27.md` for the full analysis.
- **2026-09-28 (`ai-sessions/0053`): `CAP-063` (Group AY) analysed — the hardware re-test of the `0048`/`0052` builds.** Folder renamed to
  `captures/CAP-063-2026-09-27_15-57-33_16-25-03-Group_AY/` (film first frame 15:57:33); `CAP-063-EVENT-NOTES.md` rewritten in phone time with HCI/log
  evidence (clock: phone = overlay + 1.4 s), `CAP-063-FINDINGS.md` new. Nothing refuted: ADR-044's re-open held in all 14 session ends; 90 settings
  reads and 46 writes answered OK, byte-identical to the fixtures; a docked bud ACKs a ring. Maintainer-approved in chat 2026-09-28: `PROTOCOL.md`
  §4.3 Option F — a second `SubscribeRuntimeInfo` is answered (8/8) 🟢, the stream also pushes while docked 🟢 (`CAP-063`); ADR-043 Update; ADR-024
  Update (supporting evidence, stays 🟡); audibility and the docked ring recorded as observations; §6 notes (conversation detection = AVRCP 🟡,
  Digital assistant via GSND 🟡, no ACL re-creation 🔴). Improvement list and next steps in the RESULT; the film stays unblurred (maintainer's choice).
- **2026-09-28 (`ai-sessions/0054`): the `CAP-063` improvements I-1, I-2, I-3, I-5, I-4 built (the maintainer's choices and texts in chat).** I-1: a tap
  on a disabled ANC mode (screen or tile) does one ordinary claim — `Get`, then the `Set` in the same claim only if the Buds' `Notify` now reads Settable
  non-zero; the buttons stay enabled with "(checked HH:MM:SS). Tapping a mode checks again first." — reverses `0048` I-3's "claim nothing" for a user tap
  (confirmed in chat). I-2: a session loss while the app was not on screen is worded from the first reading of Android's link on return (two new
  causes; `CAP-063` 16:15:22). I-3: the balance snaps to "Centre" within ±3 (`17:0` = `CAP-046` frame 1873). I-4: at Connect the previous
  connection's per-bud lines read "last seen … (…, last connection)" until the Buds report again. I-5: a note under "Press and hold" about
  "Digital assistant". Nothing new on the wire (the ANC `Get` moved into one shared call site). Tests with real `CAP-063`/`CAP-046` bytes:
  `:data` 1546, `:hardware` 51, `:domain` 23; six mutation checks caught. New capture skeleton `CAP-064` (Group AZ, Pixel 9a: I-1…I-5, AY-3, the
  skipped `APP_TESTPLAN.md` steps); the W-12b in-ear-detection-off steps added to `CAP-056` (Group AR) on the maintainer's request. Not hardware-verified.
- **2026-09-28 (`ai-sessions/0055`): `CAP-056` (Group AR, Pixel 7a, official app) analysed — the ANC-mode checklist and in-ear detection.** Folder
  renamed to `CAP-056-2026-09-28_17-30-53_17-35-58-Group_AR` (overlay clock), mp4 mode 644; the film is committed unchanged (maintainer's choice). Approved in
  chat: `qht` bit order 1 Noise cancellation / 2 Off / 3 Transparency / 4 Adaptive 🟢 (the on-screen-order reading refuted), the checklist = "ANC gesture loop" 🟢,
  no Left/Right field in the write 🟢 (one list 🟡); "In-ear detection" = `qhr` field 2 🟢 (5 filmed taps + the Buds' SASS capability bit 4); with it off the
  phone does not pause, the Buds still close DLCI 0x02 and send no ANC `Notify` on wear changes; §6 notes (phone-side pause without AVRCP pass-through 🟡, DLCI
  0x08 `04 05` wear value 🟡, field-13 mirror 🟡). New ADR-046 (field 12 read + write) and ADR-047 (field 2 write); ADR-019/024/036/045 dated Updates. No app change.
- **2026-09-28 (`ai-sessions/0056`): the press-and-hold ANC-mode list (field 12, ADR-046) and the "In-ear detection" switch (field 2, ADR-047) built.** Controls tab:
  "Modes for press and hold (both buds)" — read at Connect, four boxes shown while a bud's press and hold is Noise control, one `WriteSetting` with all four
  booleans per tap, never fewer than two ("At least two modes must stay selected."); "In-ear detection" is a switch with a note on what "off" changes. Also (the
  maintainer's choice): a setting not read yet is disabled (U-1) and a tap during a re-open says so (U-2). Real fixtures: `CAP-056` 1529/1531, 1689…1843, 1502, 2173/4048,
  2849/3627, and — found in this session — channel-21 field-12 frames in `CAP-041` 2176/2192/2198 and `CAP-036` 1514/1516. `:data` 1564, `:domain` 25 tests; 7
  mutations caught. `CAP-064` skeleton section VII; `APP_TESTPLAN.md` C12, N1, N7–N11. Not hardware-verified.
- **2026-09-29 (`ai-sessions/0057`): Material 3 UI overhaul, within the maintainer's decisions of 2026-09-29.** A top app bar "OpenControl" with a
  Debug action (Debug is no longer a bottom tab — restoring `ARCHITECTURE.md` §2.4's design), five tabs, own Kotlin icons (Material Symbols, Apache-2.0,
  plus an own Case icon), an app theme (dynamic colour, dark mode), a graphical battery (Left | Case | Right, bars, bolt; "Battery unavailable" without a
  bar), the times and state words behind an (i) dialog on each card with a dot + dimmed value when not current, pull to refresh / reconnect on every tab,
  and `BudsRepository.refreshSettings()` (the Connect-time settings pass once more on a pull on Sound/Controls — the only `:data` change, 3 tests with
  `CAP-036` bytes, 3/3 mutations caught). Slider knobs no longer stay at an unacknowledged value (F-1). Field-12 list, Digital assistant, in-ear switch,
  mono/conversation detection on Sound kept as built. New test-only dependencies for `:ui` (Compose UI test + Robolectric 4.13, JUnit 4.13.2) with 9 tests.
  `APP_TESTPLAN.md` section O, `CAP-064` section VIII. Not hardware-verified. No `PROTOCOL.md`/`DECISIONS.md` change.
- **2026-09-30 (`ai-sessions/0058`): an exhaustive end-to-end audit of the whole project** — 54 findings (A58-GOV/PROT/CAP/ARCH/DEC/HK/SES/APP) and
  five undiscovered protocol leads (L-1…L-5), each with a verification command; no file other than its RESULT changed.
- **2026-09-30 (`ai-sessions/0059`): every 0058 item processed, with the maintainer's decisions in chat.** New ADR-048 (no ViewModel; supersedes
  ADR-001's MVVM clause) and ADR-049 (the Settable byte; supersedes ADR-024's "dock state"); `PROTOCOL.md` §2.2a/§4.1/§4.3/§6 Updates (L-2 HID
  head tracker 🟢, L-4 service names 🟢, L-5 "serial" → version number 🟢, L-1 address derivation 🟡; battery broadcast `@SystemApi`); false
  negatives and FACT claims in capture FINDINGS rewritten with commands and positive controls; status banners on 33 FINDINGS. App: atomic
  connection-state transitions, the ANC tile's first value, `UnreadableAnswer` for an undecodable read, a write quarantine after a timed-out
  `WriteSetting`, EQ sliders off until read, `SessionOpening` on the EQ tab, `AppUiSession` (pairing and Debug list survive a rotation), one clock,
  0 Kotlin warnings — real-capture fixtures, 6/6 mutations caught. `CAP-065` (Group BA) skeleton for the hardware re-test. Not hardware-verified.
- **2026-10-01 (`ai-sessions/0060`): `CAP-064` (Group AZ) analysed** — film (every second, audio included), HCI log, debug export, logcat and system log.
  The "no automatic connect when the case is opened" observation is qualified: opening the lid with both buds docked starts no Bluetooth connection, so ADR-044
  has no event; the app re-opened by itself on 4 of 4 link-backs. AY-3: one worn bud ⇒ Settable `e8`; `e8` also with no bud worn (in-ear detection off; ≈ 28 s
  with it on) — ADR-049 Update; the Buds follow OpenControl's field-12 list (ADR-046 Update); OpenControl's field-2 writes behave as the official app's (ADR-047
  Update); `PROTOCOL.md` §4.1/§4.5.3/§4.5.5 dated Updates. Folder renamed to `CAP-064-2026-10-01_10-04-14_10-29-28-Group_AZ`; `CAP-066` (Group BB) skeleton for the
  leftovers. No app change.
- **2026-10-01 (`ai-sessions/0061`): `CAP-065` (Group BA) analysed** — film (every second at 2 s spacing plus scene changes, audio included), HCI log, debug
  export, logcat and system log. Same installation as `CAP-064` (process continuous, no reinstall). The EQ note "…The sliders are off until it arrives…" is on
  film between ready and the EQ read; the ANC tile is compact, so its subtitle is not on film. Settable: 28 × `00` with no bud worn, `e8` with a bud worn —
  ADR-049 Update (status unchanged). L-1: one bud out ⇒ Left channel 19 / Right 21, 7/7 — `PROTOCOL.md` §2.2a Update (🟢 correlation, 🟡 hosting bud). Also
  `PROTOCOL.md` §4.3 (lid closed: that bud `ff`, no Case) and §5 (every ACL started by the Buds when a bud leaves the case) Updates; `ARCHITECTURE.md` §6.0b
  note (the collision path in the Bluetooth process's log; a reply after the close is lost). Pulls sent exactly their tab's action. Folder renamed to
  `CAP-065-2026-10-01_11-07-30_11-28-23-Group_BA`; `CAP-066` (Group BB) extended (L-1 hosting-bud test, tile subtitle, the robustness steps not done). No
  app change.
- **2026-10-01 (`ai-sessions/0062`): FEATURE — ANC `Get` before every `Set`, the cut-off answer, the settings menu (the maintainer's choices after `CAP-064`/
  `CAP-065`, confirmed in chat).** F-1: every ANC tap (tab and tile) claims once, sends `08 11` and the `08 12` only if that claim's `Notify` reads Settable
  non-zero; the tile steps from that fresh `Notify` (`BudsRepository.stepAncMode`) — no NAK on a stale "allowed" (`CAP-064` 3433 → 3440). F-2: "The Buds don't
  allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first."; tile subtitle "Not allowed now". F-3: a
  claim closed after its request and before the answer is `BudsError.AnswerCutOff` (never retried, never "didn't respond in time", `CAP-065` 2640/2649/2651,
  10321/10344/10356), the mode then "not confirmed" until the next `Notify`. F-4/F-5/F-6: a gear opens Settings (dark mode System/On/Off in the existing
  DataStore), Debug (unchanged) and Info (version, git commit and date via a local `BuildConfig`; firmware of Case/Left/Right with the `PROTOCOL.md` §2.2a
  2026-10-01 Update — 🟢 the official app's mapping, maintainer-approved; the control channel). F-7: the Connection card's Disconnect in the card's own text colour
  (the `CAP-065` dark frame measured ≈ 1.2:1) + a contrast test. F-8: StrictMode `detectLeakedClosableObjects` in debug builds. T-1: provisional loss-cause
  lines marked; T-3: `dataExtractionRules` (exclude everything) and the manifest comment on AndroidX Core's private permission; T-2 left as is. Real-byte fixtures
  `Cap064Fixtures.kt`/`Cap065Fixtures.kt`; gate green from clean (tests `:data` 1581, `:domain` 27, `:hardware` 52, `:ui` 24; lint 0 issues; 0 compiler
  warnings); 10 mutation checks each caught. `CAP-066` (Group BB) adapted to this build. Not hardware-verified.
- **2026-10-01 (`ai-sessions/0063`): `CAP-066` (Group BB) analysed — the first hardware run of the `0062` build.** Film (2-s sheets, scene changes, audio),
  two HCI logs (Bluetooth off/on), two exports, logcats and system logs (before/after a force-stop). Build `043a09b` on the Info tab (app code `ad0c4ba`, not
  "-dirty"); the install's `flags=39` clear is code-cache-only (also in `CAP-064` — its FINDINGS corrected). F-1/F-2 hardware-verified (23/23 claims `08 11`
  first, 4 `Set`s after `e8`, 0 NAK; `ARCHITECTURE.md` §3.1 note); F-3 not exercised. Settable: 28 × `00` none worn, 48 × `e8` worn, straight from the case
  `00` 2.6–175 s — ADR-049 Update + `PROTOCOL.md` §4.1 Update (status unchanged). L-1: the Right out with both worn on 21 ⇒ Buds `DISC` + 19, 2/2 —
  `PROTOCOL.md` §2.2a Update (🟢 that switch, 🟡 the host). `PROTOCOL.md` §5 note (a 2nd sample of the phone re-paging after a drop). StrictMode: the
  violations are framework objects (`ARCHITECTURE.md` §12 note). GrapheneOS auto-off `delayMillis: 0` = disabled (`CAP-065-FINDINGS.md` §0 updated). Defect: the
  rotation resets the tab to Connection (next FEATURE, with balance precision, a named Bluetooth-off loss and proxy closing). Folder renamed to
  `CAP-066-2026-10-01_16-12-57_16-34-50-Group_BB`. No app change, no new capture skeleton.
- **2026-10-01 (`ai-sessions/0064`): FEATURE — the maintainer's choices after `CAP-066`, plus licence and links on Info.** F-1: the tab survives a
  configuration change (`OpenControlNavHost` skips the pager ↔ back-stack sync until the restored back stack is known; `CAP-066` K4r reset Sound → Connection;
  `TabRestoreTest`). F-2: the balance gets `[‹]`/`[›]` steps of 1 beside the unchanged slider, one write per tap (`CAP-066`: 32 drags never reached Right 4;
  fixtures `CAP-066` A7723/A7873, `CAP-064` 6671 = `17:7`). F-3: a session loss around an adapter TURNING_OFF/OFF reading is "Bluetooth was switched off on this
  phone", final at once; adapter changes are logged (fixtures: `CAP-066` export E1 279–283 / E2 164–168 and the system logs' `STATE_CHANGED` lines). F-4: every
  profile proxy obtained is closed once (`ProfileProxies`). F-6: Settings → Info shows the licence (AGPL-3.0-or-later), the bundled licence text and links to the
  `LICENSE`, `README.md` and the issue tracker, handed to the browser on a tap — **DECISIONS.md ADR-050** (maintainer-approved in chat); no manifest change, no
  `INTERNET`, no dependency. Gate green (`:data` 1588, `:domain` 31, `:hardware` 56, `:ui` 41; lint 0; warnings 0); 7 mutations caught. New skeleton
  `CAP-067` (Group BC). Not hardware-verified.
- **2026-10-02 (`ai-sessions/0065`): MAINTENANCE — release preparation (nothing published).** Release signing from `~/.gradle/gradle.properties` or the
  environment, failing clearly without it (never unsigned, never the debug key); version 1.0.0 / versionCode 10000 (major × 10000 + minor × 100 + patch);
  `.gitignore` for key files and `dist/`. App name "OpenControl for Pixel Buds Pro 2", launcher label "OpenControl", the trademark line on Info, README and
  release notes — **DECISIONS.md ADR-051** (maintainer-approved in chat), notes in `AGENTS.md` §12 and `PROJECT.md`. New: `RELEASING.md` (runbook),
  `scripts/release.sh` (builds in a clean worktree, verifies signature/version/no `INTERNET`, writes checksums, notices and notes, prints the publishing
  commands), `scripts/release_notes.template`, `scripts/third_party_notices.py` + `LICENSES/Apache-2.0.txt`, `scripts/readme_media.sh`, issue templates. The
  README rewritten for users first, with the app's screenshots and a compressed screen recording; `PROJECT.md` Definition of done: an evidence table
  (criteria 1–3 not yet shown without Play services). Not hardware-verified.
- **2026-10-02 (`ai-sessions/0066`): FEATURE — two changes before `CAP-067` (the maintainer's request).** The balance's `[‹]`/`[›]` steps of `0064` F-2 are removed
  (the slider alone again, one write per release; the real-byte tests of `17:7` stay); Settings → Info no longer links to `LICENSE` on GitHub — the licence is
  read in the app only (**DECISIONS.md ADR-050 Update**, maintainer-approved in chat). `CAP-067` skeleton, `APP_TESTPLAN.md` and `ARCHITECTURE.md` aligned.
- **2026-10-03 (`ai-sessions/0067`): `CAP-067` (Group BC) analysed — the release-signed 1.0.0 APK (`8d8af4b`) in a GrapheneOS user without Google Play.**
  Film (2-s sheets, narrowed transitions; no audio samples), two HCI logs, two exports (one with Debug mode off), one logcat, no system log. 6 Message Stream claims,
  all the app's, 0 Play-services claims (positive control `CAP-066`); F-1 14/14 (corrected 2026-10-03; was "13/13") incl. the Settings menu across Android's dark switch; Info, licence, links and the
  ADR-051 notice on film; balance Right 4 = `17:7` reached; L-1: Right out on 21 ⇒ 19 (3rd), one bud out on 19 ⇒ 21 (bud not identifiable; `PROTOCOL.md` §2.2a
  Update); no ANC `Set` in the run, F-3 not verifiable (no export after the Bluetooth off/on). Maintainer-approved in chat: **`PROJECT.md` Definition of done 1–3
  ticked** (the ANC change from the maintainer's own test without Play services) and **release `8d8af4b` as 1.0.0**, with one known issue (the connect-failure
  text). Folder renamed to `captures/CAP-067-2026-10-03_07-57-35_08-18-29-Group_BC/`.

### Fixed

- **2026-09-18 (`ai-sessions/0034`): a crash-on-launch in the v1 app, found by the maintainer on
  their own real hardware the first time `ai-sessions/0033`'s debug APK was actually installed.**
  `:app`'s `AndroidManifest.xml` declared `OpenControlApplication`/`MainActivity` with the usual
  relative `".ClassName"` shorthand, which resolves against the module's manifest `namespace`
  (`io.github.tedsluis.opencontrolpixelbuds`) rather than either class's real Kotlin package
  (`io.github.tedsluis.opencontrolpixelbuds.app`, one segment deeper) — silently resolving to a
  class that has never existed and crashing with `ClassNotFoundException` before any app code runs.
  Present since the app skeleton was first created (`ai-sessions/0013`, 2026-09-13); never previously
  exercised, since no prior session had run a real build on a real device. Compounding factor:
  `ai-sessions/0033`'s own Phase 8 build verification had already hit Android lint's `MissingClass`
  check flagging exactly this, misdiagnosed it as an AGP+Hilt tooling false positive, and disabled
  the check — removing the one automated signal that would have caught this pre-emptively. Fixed by
  using fully-qualified class names in the manifest and removing the incorrect lint suppression; full
  build/test/lint suite re-verified clean with zero suppressions.
- **2026-09-18 (`ai-sessions/0035`): a crash-on-pair, found by the maintainer immediately after
  retesting the `ai-sessions/0034` fix on their own real hardware.** Tapping "Pair a device" called
  `CompanionDeviceManager.associate()`, which throws `IllegalStateException` at call time if the
  manifest never declares `<uses-feature android:name="android.software.companion_device_setup">` —
  which this project's manifest never did. Confirmed this one crash fully explains the maintainer's
  report that every other screen/function "didn't work either": the app never survived long enough
  past the Connection screen's only available action to reach them. Fixed by adding the missing
  `uses-feature` declaration, and closed an adjacent, already-disclosed gap in the same pass — the
  `CompanionDeviceManager` picker's returned `IntentSender` was never actually launched via an
  `ActivityResultLauncher`, so fixing only the crash would have left "Pair a device" silently do
  nothing. Full build/test/lint suite re-verified clean (1232 tests, 0 failures).
- **2026-09-18 (`ai-sessions/0036`): the pairing flow itself, found by the maintainer immediately
  after retesting the `ai-sessions/0035` fix — no more crash, but pairing offered arbitrary nearby
  Bluetooth devices and never actually paired the real Pixel Buds Pro 2, with no error shown.** Two
  distinct bugs: (1) `BudsCompanionPairing`'s `BluetoothDeviceFilter` had no name/address/service
  constraint at all, so `CompanionDeviceManager` offered any nearby device one at a time instead of a
  Pixel-Buds-only list; (2) `CompanionDeviceManager.associate()`'s own success callback only grants
  this app permission to see the selected device — it does not perform Bluetooth bonding itself.
  `ARCHITECTURE.md` §9.0a's own design already specified the needed `BluetoothDevice.createBond()` +
  `ACTION_BOND_STATE_CHANGED` step; it had simply never been implemented, so accepting the CDM
  consent dialog silently did nothing further. Fixed: a name-pattern device filter (confirmed against
  the maintainer's own real device), a `PairingState`-driven bonding flow implementing §9.0a's
  already-correct design for the first time, on-screen pairing status/error text, and a resume-time
  re-check so pairing via Android's own Bluetooth settings (which bypasses this app's own CDM flow
  entirely) is no longer invisible to the running app. Full build/test/lint suite re-verified clean
  (1232 tests, 0 failures).
- **2026-09-18 (`ai-sessions/0037`): "Connect" doing nothing with no status shown, plus a bottom-nav
  bug where switching tabs could land on Debug unexpectedly — both found by the maintainer after
  retesting the `ai-sessions/0036` fix, this time successfully paired via Android's own Bluetooth
  settings.** Root cause of "Connect does nothing": `MainActivity`'s `onConnect`/`onDisconnect`
  actions were still the placeholders `ai-sessions/0033` had left in place — `BudsRepository` itself
  had no `connect()`/`disconnect()` at all. Implemented for real: `BudsTransport`/`BudsRepository`
  (domain) gained `connect()`/`disconnect()`; `BudsRepositoryImpl`'s constructor now holds the real
  `ConnectionStateMachine` object (not just its read-only `Flow`) plus a lazily-resolved
  `bondedDeviceProvider`, since the bonded device can only be known at connect time, well after this
  singleton is constructed; Hilt's `TransportModule` now provides the real, `BluetoothSocket`-backed
  `RfcommBudsTransport` instead of `FakeBudsTransport`. Found and fixed a latent bug during this
  refactor: `RfcommBudsTransport.disconnect()` cancelled a single object-lifetime `CoroutineScope`,
  which would have permanently broken any later reconnect attempt — now a fresh scope per `connect()`
  call. Root cause of the nav bug: `OpenControlNavHost` special-cased the Debug tab with a plain
  `navController.navigate()` call while the other four tabs used the
  `popUpTo(start){saveState}+launchSingleTop+restoreState` pattern, letting Debug accumulate
  duplicate back-stack entries the other four's handling never touched; fixed by unifying all five
  destinations under identical navigation mechanics (Debug stays visually distinct only in never
  showing as "selected"). Also fixed in the same pass: `iconFor()` had no `Routes.DEBUG` branch, so
  Debug silently reused the generic Settings icon; and added
  `android:enableOnBackInvokedCallback="true"` to the app manifest. **This is this project's first
  real RFCOMM socket-connect attempt against actual hardware — everything downstream of "sockets
  opened" remains as hardware-unverified as before.** Full build/test/lint suite re-verified clean
  (1233 tests, 0 failures).
- **2026-09-18 (`ai-sessions/0038`): a self-directed audit of the v1 app's remaining gaps — a
  maintainer question ("are there other parts that are unimplemented or too basic?"), not a bug
  report.** Four real, distinct findings, all fixed: (1) `BudsForegroundService` was a fully-built,
  compiling class that nothing ever started or stopped — `MainActivity` now binds it to
  `ConnectionStateMachine`'s transitions per `ARCHITECTURE.md` §6.0a's own already-documented design;
  (2) `BleLogger.exportLog()`'s ring buffer had no UI path to actually reach it — the Debug screen now
  has an "Export debug log" button handing it to the system share sheet, local-only per AGENTS.md §9;
  (3) **no peer-disconnect detection** — `RfcommBudsTransport` privately noticed a dropped link
  (range loss, OS-triggered teardown) via its reader coroutine's `IOException`, but nothing told
  `ConnectionStateMachine`, so the UI could have kept showing `Ready` for a connection that had
  actually already died; fixed with a new `BudsTransport.connectionLost: Flow<Unit>`, carefully
  distinguishing a genuine peer/range-loss drop from the `IOException` this app's own `disconnect()`
  deliberately causes by closing the socket mid-read; (4) `BudsRepository.refreshAncMode()` (built
  and unit-tested since `ai-sessions/0033`) had no UI affordance — `AncScreen` gained a "Refresh"
  button — and a related, more subtle find in the same screen sweep: `EqScreen`'s sliders wired
  `onGainsChanged` (a real DLCI 0x02 wire write) to `Slider`'s continuous `onValueChange`, which would
  have sent one RFCOMM frame per pixel of drag movement, instead of the once-per-drag
  `onValueChangeFinished`. Full build/test/lint suite re-verified clean (1234 tests, 0 failures).
- **2026-09-19 (`ai-sessions/0039`): flaky Connect, an always-empty EQ tab and an app-vs-OS connection
  mismatch, found by the maintainer testing `ai-sessions/0038`'s build on real hardware (two rounds,
  ~28 connect attempts).** Root cause established from Bluetooth-stack logs, not guessed:
  `RFCOMM_CreateConnectionWithSecurity: already at opened state` — Android allows one RFCOMM connection per
  (device, channel) across all apps and its failure path closes the *incumbent's* port too. Contenders:
  Google Play services' Fast Pair event stream on the Message Stream channel (outside this app; nothing here
  touches Play services) and — this app's own defect — leaked sockets: `connectionLost` never closed the
  surviving channel, a failed `connect()` never closed the socket that failed, EOF and failed writes were
  never reported, two readers reported one loss twice, and a stale loss could knock a fresh attempt back
  to `Disconnected`. Fixed in `RfcommBudsTransport` (one-unit teardown, at-most-one/current-connection loss,
  bounded in-tap retry for fast collisions, `RfcommSocket` extracted so it is unit-testable), with the
  reason kept (`BudsError.ChannelUnavailable`/`ChannelLost`) and shown. The EQ tab was a UX dead end
  (controls hidden while the value was unknown, and the Buds never volunteer it), not a socket failure —
  controls are now always shown with an explicit "unknown" banner; ANC/EQ/Find are disabled while not
  connected; an informational "Android shows your Buds as connected" hint was added (`OsConnectionObserver`,
  public APIs only). Design questions (per-channel tolerance, auto-reconnect, lazy Message Stream) are
  maintainer proposals, not decided. Full suite clean (1253 tests, 0 failures; 2 mutants caught). Not
  hardware-verified.
- **2026-09-19 (`ai-sessions/0040`): on-demand claiming of the shared Message Stream channel, a live battery decoder, and two
  research items — after the maintainer's three real-hardware rounds and explicit decisions.** With Google Play services' Fast Pair
  active the app held DLCI 0x04 for a median 4.5 s (20 of 20 sessions ended on that channel, never on DLCI 0x02), because Play services
  re-opens its own socket 2.7–5.0 s after losing it and the Android stack's failure path then closes ours. **ADR-032** (maintainer's
  choice, retracting his earlier "never connect automatically", since only one channel — not the Buds connection — is lost): the
  session is the MAESTRO channel; DLCI 0x04 is claimed for the duration of an ANC / Find / Connect tap (claim → act → wait for the
  Buds' reply → release after 1.5 s) and its loss is not a session loss; a busy channel is reported per action with the reason.
  **ADR-033** (maintainer-requested): the Battery Option B decoder (`Group 0x03 Code 0x03`) is unblocked for the percentage regime;
  bit-7-as-charging-flag is a supported but unaccepted proposal (CAP-009: 221→228 vs. Option E's 93→100, step for step). Also fixed:
  a Ring ACK was routed as an ANC ACK (found by a test), and the optimistic ANC update could overwrite the Buds' own Notify.
  **Research** (`DESKRESEARCH_FINDINGS.md`): the app's HFP battery route most likely cannot receive `AT+BIEV` on Android 14+ (all battery
  rows "unavailable" in every session); DLCI 0x02 is verifiably Pigweed `pw_rpc` (`service_id`/`method_id` match the name hashes of
  `maestro_pw.Maestro`/`WriteSetting` byte for byte) and the official app reads every setting, including the current EQ
  (`ReadSetting 4:16`), on each connect — a read path for the EQ tab, awaiting sign-off. New `scripts/pwrpc_decode.py`. 1281 tests, 0
  failures; not hardware-verified.

### Changed

- Defined a narrow, bounded BLE-scanning exception for the Fast Pair Battery
  Notification (filtered/foreground-triggered/time-boxed) to resolve a
  conflict with the absolute no-scanning rule — `DECISIONS.md` ADR-006.
- Removed `TODO.md`'s duplicated, checkbox-synced open-questions list in
  favor of single-source pointers to `PROTOCOL.md` §6 and `ARCHITECTURE.md`
  §15.
- Tied the `FrameEncoder`/`FrameDecoder` implementation gate to a required
  `DECISIONS.md` ADR, not just 🟢 FACT confidence alone (`ARCHITECTURE.md`
  §2.1, later superseded by the per-DLCI `CodecRouter` design).
- Added an ADR-numbering rule (sequential, never reused) to prevent stale
  hardcoded ADR references elsewhere.
- `CAPTURE_BLUETOOTH_HCI_SNOOP.md`: clarified Group A (lightweight
  forget-and-re-pair) vs. Group P #16 (destructive factory reset) are
  distinct; added the missing §9 Capture Index; made a full reboot the
  default recovery step (§2) with Bluetooth toggle as a faster fallback;
  branched frame-analysis instructions by type (RFCOMM vs. BLE advertisement)
  instead of applying one hypothesis to all captured frames; made `btsnooz.py`
  the primary log-extraction method; corrected several unsubstantiated claims
  (empty-capture cause, "non-rootable" phrasing, a community-forum report
  presented as confirmed); rewrote the encryption FAQ entry to distinguish
  HCI-boundary visibility from genuine link-layer encryption; clarified
  "one action per window" means one user-triggered event, not one frame;
  marked the post-action wait as a heuristic, not a guarantee; added
  observation-start/-end boundary logging for passive windows; replaced a
  binary traffic-observed/not check with a proper 3-way outcome taxonomy for
  Loud Noise Protection/Adaptive Audio.
- Fixed a self-contradictory "Android 14+ (API 34)" phrasing in
  `ARCHITECTURE.md`/`README.md` — separated the decided compile/target SDK
  from the still-open minimum supported API.
- Fixed `TODO.md` undercounting Gradle modules as four instead of five
  (missing `:app`); same gap fixed in `DECISIONS.md` ADR-001.
- Added Group Z (pipeline validation) and a cross-command framing check after
  Group K to `CAPTURE_BLUETOOTH_HCI_SNOOP.md`; reordered `TODO.md` Phase 1 to
  match. Declined to hardcode `CAP-NNN` numbers into `TODO.md` — IDs are
  assigned in the Capture Index as work happens.
- Fixed stale filename references (`PROTOCOL-NOTES.md`, `TESTPLAN_EN.md`)
  found during a cross-file consistency pass.
- `fix:` renumbered three second-attempt capture sessions that had been
  reusing their first attempt's `CAP-NNN` ID with only their folder's
  date/time suffix distinguishing them — a violation of `DECISIONS.md`
  ADR-007's "never reused" ID-format rule, and the direct cause of the
  2026-08-18 Group T session (`CAP-005`) never getting a row of its own in
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index. Reused `CAP-005`
  (2026-08-18, Group T) → `CAP-015`; reused `CAP-007` (2026-08-18, Group U) →
  `CAP-016`; reused `CAP-010` (2026-08-16 18:30, Group W) → `CAP-017`. The
  original, first-attempt sessions (`CAP-005` 2026-08-15, `CAP-007` 09:14-10
  2026-08-16, `CAP-010` 11:42 2026-08-16) keep their original IDs unchanged.
  Renamed the affected folders/files (`git mv`, preserving history) and
  updated every current-state cross-reference across `PROTOCOL.md`,
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, and
  `DESKRESEARCH_FINDINGS.md` to the new IDs; earlier entries in this
  changelog that mention the old IDs are left as-is (historical record).
- `ci:` bumped `actions/checkout@v4` → `@v5` and `actions/setup-python@v5` →
  `@v6` across the three docs-site workflows
  (`.github/workflows/lint-docs.yml`,
  `.github/workflows/update-docs-sidebar.yml`,
  `.github/workflows/update-sitemap.yml`) to clear GitHub's Node 20
  deprecation warning — both actions now declare `using: node24` natively
  instead of being force-run on it.

### Reverse engineering findings

- Identified the official Fast Pair "Battery Notification" and "Message
  Stream: Device Information" extensions as likely covering battery
  reporting, reducing what needs reverse engineering from scratch.
- Corrected an earlier fixed-interval battery-polling assumption — Fast Pair
  battery updates are event-driven (connect or on change), not polled.
- Restructured `CAPTURE_BLUETOOTH_HCI_SNOOP.md`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`
  to remove their overlap: `CAPTURE`'s Groups became capture scenarios,
  `TESTPLAN` became a stable Test-ID catalog (70 IDs) with a thin evidence
  pointer into `PROTOCOL.md`, closing the chain Test-ID → Group → `CAP-NNN` →
  frame → finding. Recorded as `DECISIONS.md` ADR-007. Surfaced two catalog
  gaps (`INEAR-004`, `GATT-001`) not yet covered by a capture scenario at the
  time.
- Ran several information-preservation and consistency audits across
  `AGENTS.md`/`ARCHITECTURE.md`/`PROTOCOL.md`/`CAPTURE`/`TESTPLAN` against
  earlier drafts; found and restored a handful of genuine content losses
  (a dropped `TESTPLAN` row note, a narrowed cross-reference, and a fully
  dropped battery mechanism — `ACTION_BATTERY_LEVEL_CHANGED`, restored as
  `PROTOCOL.md` §4.3 Option 0) and fixed several orphaned
  Test-ID/formatting/traceability gaps. Flagged (not created) a then-missing
  `EXPERIMENTS.md` that seven files referenced — later resolved by retiring
  the concept entirely (see 2026-08-15 entries below).
- Fixed a real `btsnooz.py` bug in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3, found
  via `CAP-001` (Group Z): the bugreport's internal `.txt` keeps Android's
  own generated name regardless of the name passed to `adb bugreport`.
- Formalized Group R (forced GATT re-discovery, bond removal via system
  settings not the app's own "Forget") and Group S (GMS disabled / no Pixel
  Buds app, `GFPS-001`) as documented capture scenarios, each written up from
  an actual capture rather than designed speculatively.
- **2026-08-12 deskresearch pass**, resolving two long-standing open
  questions without a new capture: **ANC mode set/get/notify → 🟢 FACT**
  (Google's official Hearable Controls extension, Message Group `0x08`,
  matches `CAP-001` byte-for-byte with a 4/4 content+timing correlation); and
  **DLCI 0x02's framing → 🟢 FACT** (Pigweed `pw_hdlc`, CRC-32 matching
  `pw_checksum` exactly, 640/640 sub-frames across three captures, matching
  `pbpctrl`'s documented Maestro transport). `PROTOCOL.md` §2.3 restructured
  from a binary framing question into a three-channel table as a result.
  `libmaestro`'s own command content stayed unresolved, deliberately not
  force-closed with an unreviewed ADR.
- **2026-08-14–15: 8-lens project review**, executed in four phases (each
  committed and confirmed separately) —
  - **Phase 1**: renamed the project (trademark), replaced the "clean-room"
    claim with "independent implementation" (reaffirmed AGPL-3.0 unaffected,
    `DECISIONS.md` ADR-002), and reworked `ARCHITECTURE.md` around the
    3-DLCI reality (`CodecRouter`, `UnidentifiedFrame`, State Reconciliation,
    Startup Handshake/Safe Mode, wakelock budget).
  - **Phase 2**: retired `PROTOCOL_NOTES.md` and `EXPERIMENTS.md` as
    intermediate buffers (agents work `CAP-NNN-FINDINGS.md` → `PROTOCOL.md`
    directly); added `DESKRESEARCH_FINDINGS.md`; added AI-guardrail rules
    (no unilateral FACT/ADR promotion, hex-dump determinism, the hex & script
    rule); added `SECURITY.md`/`CONTRIBUTING.md`.
  - **Phase 3**: demoted an over-generalized CTKD finding back to HYPOTHESIS;
    fixed a GMS-vs-app-uninstall confound in the `CAP-004` GMS-dependency
    conclusion; added an AES-128 open question for DLCI 0x02; split the
    battery mechanism docs (Fast Pair event-driven vs. HFP periodic ~6–7s,
    fixing a stale `AT+IPHONEACCEV`/`AT+XAPL` reference along the way); added
    `DECISIONS.md` ADR-008 (Fast Pair Account Linking/Ownership
    Transfer/Non-Owner Service out of scope); corrected Group R's GATT-cache
    claim in favor of Group W; distinguished the UI-baseline vs. still-open
    wire-baseline firmware version.
  - **Phase 4**: added `DECISIONS.md` ADR-009 (ANC channel confirmed, but
    `FrameEncoder` blocked pending `CAP-006` — 2 of `CAP-001`'s 6 ANC taps
    produced no command frame); added a hardware-bricking disclaimer
    (`README.md`) and Disaster Recovery procedure
    (`WORKSTATION_PREPARATIONS.md`); added a hardcoded-wire-string exception
    rule (`PROJECT_RULES.md`); reprioritized `TODO.md` around `CAP-005`
    (Group T, EQ)/`CAP-006` (ANC)/`CAP-010` (Group W) as top priority over
    edge-case protocol research; trimmed this changelog's own verbosity.

- **2026-08-20: comprehensive documentation audit and remediation**, an 8-phase
  review (inventory, structural/cross-reference integrity, traceability,
  FACT/HYPOTHESIS/ASSUMPTION labeling, rule compliance, raw wire-data
  re-verification, editorial review, gap analysis) covering every core doc,
  the full Capture Index, and all `CAP-NNN-FINDINGS.md`/`CAP-NNN-EVENT-NOTES.md` files.
  **Headline: zero 🔴 Critical findings** — independent `tshark` re-derivation
  of every checkable Phase-5 claim (connection lifecycles, ANC frame counts,
  EQ quintets, GATT discovery counts, the DLCI-0x02 CRC-32 pipeline) matched
  the documentation exactly, with no factual discrepancy found. Findings
  resolved, in order of impact:
  - **Status-taxonomy unification**: 🔴 OPEN QUESTION formally added as a
    fourth confidence tier in `PROJECT_RULES.md` §1 rule 1 and `PROTOCOL.md`
    §0 (also `REVERSE_ENGINEERING.md`/`DESKRESEARCH_FINDINGS.md`'s legends) —
    reconciling the written rule with practice already unanimous across every
    `CAP-NNN-FINDINGS.md` file and 16 uses in `PROTOCOL.md`'s own body.
    `PROJECT_RULES.md` rule 4's stale "🟡 Secondary" citation fixed to match.
  - **`PROJECT_RULES.md` rule 9a**: added a grandfather clause (findings dated
    before the rule's own 2026-08-15 introduction aren't retroactively
    required to be cleaned up) and a worked before/after example. Fixed the
    two residual post-rule violations found (`CAP-004-FINDINGS.md` §10,
    `CAP-005-FINDINGS.md` §1) by rewriting them in place.
  - Test-ID `PAIR-002` corrected to `PAIR-001` on `CAP-002`/`CAP-003`/`CAP-004`
    (no factory reset was performed in any of the three; `PAIR-002` is
    reserved for that per `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s own definition).
  - `ARCHITECTURE.md`'s title (predated the project rename) and its §5 DLCI
    0x02 description (predated the 2026-08-17 address-instability finding)
    both brought current; `PROTOCOL.md` §1's transport-overview table and §8's
    changelog table (missing several 2026-08-18 entries) likewise updated.
  - Filename/timestamp corrections: a doubled-prefix typo in a `CAP-001`
    example command; two imprecise timestamps in `DESKRESEARCH_FINDINGS.md`;
    `CAP-005-recoding.mp4` renamed to `CAP-005-recording.mp4` (`git mv`,
    preserving history).
  - Added a `.gitignore` (none existed, despite `PROJECT_RULES.md` rule 19
    referencing one).
  - **New tooling**: `id_registry.csv` (machine-readable `CAP-NNN`/`ADR-NNN`/
    Test-ID registry) and `scripts/lint_docs.py` (dead-filename, unregistered-ID,
    and stale-project-name checks) — running the lint script against the
    as-audited repo immediately surfaced four Test-IDs already in live use
    with no catalog row (`OBS-003`, `APP-001`, `APP-002`, `GFPS-002`), now
    added to `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (plus a new `APP` ID prefix).
  - Two Phase-7 gap-analysis items closed: `CAP-002`'s DLCI 0x03/0x05 traffic
    checked and confirmed to belong to an unrelated device sharing the same
    long, non-restarted log buffer (not the Buds — same class of artifact as
    `CAP-004`'s incidental Fitbit traffic), not a real DLCI-coverage gap;
    `CAP-006`'s DLCI 0x0c traffic noted as in-scope-elsewhere, out-of-scope
    for that ANC-focused session.
  - Added `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §9 open item and
    `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y (`GATT-002`) for isolating
    whether the `CAP-016`-discovered `0x0044` BLE notification burst is
    triggered by BLE connection alone — a second-review suggestion adopted
    after independent verification confirmed the reviewer's own re-derivation.
- **2026-08-21: 8 new captures analyzed** (`CAP-011`, `CAP-019`–`CAP-025`, Groups Q/C/F/G/H/I/J/K),
  closing out most of the app's remaining main-run-through command coverage. **Headline finding**:
  a general-purpose DLCI 0x02 settings-write envelope (`field5{field4{...}}`) identified and
  confirmed across 9+ distinct settings — Conversation Detection, Multipoint (`CAP-019`), Touch
  controls, Head gestures (`CAP-020`), per-earbud press-and-hold + ANC-mode rotation (`CAP-021`),
  Mono audio, Volume EQ, Volume balance (`CAP-022`), In-ear detection, and both Case-sound settings
  (`CAP-024`) — each now has its own `PROTOCOL.md` §4.5 subsection (§4.5.1–§4.5.8) in place of the
  previous bare unmapped-feature bullet list. `PROTOCOL.md` §4.4 (Find My Buds) confirmed for
  Left/Right (`CAP-025`, video-correlated, proposed for 🟢 FACT pending maintainer sign-off), with a
  notable new finding that Case/"both simultaneously" route through a separate, likely
  GMS/account-mediated Find Hub mechanism rather than the local Ring command — flagged as a possible
  Zero-GMS hard limit. `CAP-023` resolved `PROTOCOL.md` §0.1's long-open wire-baseline-vs-UI-baseline
  firmware-version question (on-screen `release_5.203` matches the already-documented DLCI 0x08
  string, same session) and found the firmware-check UI is cached, not live-queried (clean negative
  finding). `CAP-011`'s passive-BLE-scan attempt for the Battery Notification advertisement was
  inconclusive — a procedure deviation (active connection present throughout) and a structural
  non-match against the documented byte layout, recorded honestly rather than force-fit; a clean
  repeat is still needed. Two gaps explicitly flagged rather than silently left blank: `CAP-023`
  never visited the "About" (serial numbers/connection status) screen (`FW-003`/`FW-004`), and
  `HOLD-005`'s 16-frame ANC-rotation-checklist burst can't be split between Left's and Right's lists
  from wire content alone. ~10 new open questions added to `PROTOCOL.md` §6.
- **2026-08-22/23: external audit and maintainer-directed remediation.** A full-repository audit
  (all governance/protocol docs, all 19 real captures, external spec validation) produced a
  report (`AUDIT_REPORT_2026-08-22.md`, a working artifact — not committed alongside this batch,
  its findings are captured here and in the specific entries below it made). Independent
  re-derivation of three wire-level claims (DLCI 0x02 CRC-32, `CAP-006`'s ANC frame
  count, a `CAP-015` EQ quintet) against raw captures found no discrepancies; external
  verification of 13 technical claims against the Bluetooth Core Spec, Google's Fast Pair spec,
  Pigweed source, and Android/AOSP docs confirmed 11 outright, clarified one
  (`BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED` is `@SystemApi`-gated, though the literal
  broadcast string remains usable by third-party apps — *superseded 2026-09-30: the broadcast is not usable by this app, `AGENTS.md` §5's note of that date and ADR-029's Update*), and found one genuine error: `PROTOCOL.md`
  §2.1/§4.4's cited "spec worked ACK example" for the Ring/Find My Buds command did not match
  Google's actual specification — corrected via dated notes (neither observed `CAP-025` ACK
  variant actually matches the real spec example either; §6's open item on the extra byte was
  reopened against the corrected 4-byte tail). Maintainer reviewed the report and directed all
  recommendations be carried out:
  - `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`: 9 stale EQ-row Evidence columns (`EQP-003`–`EQP-007`,
    `EQS-001`/`002`/`003`/`005`) updated to point at `PROTOCOL.md` §4.2, which had confirmed them
    since `CAP-015` (2026-08-18) without the catalog being updated to match; EQ band/preset naming
    reconciled against the actual on-screen labels (screenshot-verified: "Upper treble" not "High
    treble", "Default" not "Standard", "Light bass" for Bass Reduction).
  - `DECISIONS.md` ADR-010 added (`PROJECT_RULES.md` rule 19 does not apply to the maintainer's
    own `captures/` data — a real conflict with `CONTRIBUTING.md`'s existing, already-practiced
    policy that had never been formally recorded as the ADR `PROJECT_RULES.md` itself requires for
    a knowing deviation); rule 19's text cross-linked to it and its stale `.gitignore` reference
    corrected.
  - `PROTOCOL.md` §4.3 Option C annotated to explain why DLCI 0x08 and DLCI 0x09 are both
    legitimately called "channel 4" (one RFCOMM multiplexer session, disambiguated by direction
    bit — confirmed via `tshark`, not a numbering error); Option D gained the Battery Level
    characteristic UUID (`0x2A19`) alongside the already-documented service UUID (`0x180F`); §6
    gained a refined, still-HYPOTHESIS-level characterization of `CAP-021`'s unexplained DLCI 0x0a
    burst (timing/direction/entropy profile suggesting a segmented bulk-data transfer).
  - `TODO.md`'s stale "not yet reviewed" checklist item closed (the work it was waiting on
    completed 2026-08-20, but the checkbox was never updated).
  - `REVERSE_ENGINEERING.md`'s APK keyword list gained HID-related classes, matching the project's
    already-live HID hypothesis; `README.md` and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` gained minor
    completeness/staleness fixes (non-standard per-capture artifacts, the full Group A–Z range).
  - `PROJECT_RULES.md` rule 4a clarified: a `PROTOCOL.md` restatement pointing at an
    already-compliant `CAP-NNN-FINDINGS.md` satisfies the hex-and-script rule without duplicating
    the hex inline.
  - Tooling: `scripts/lint_docs.py` gained a check for Markdown image-syntax references (it
    previously only checked backtick-quoted filenames, missing how the `SCREENSHOTS_*.md` files
    reference `images/`), and `.github/workflows/lint-docs.yml` added so it actually runs in CI.
- **2026-08-23: maintainer sign-off session — three pending FACT promotions reviewed and
  approved**, each recorded with its own `DECISIONS.md` ADR per `AGENTS.md` §6:
  - **Find My Buds Left/Right** (`PROTOCOL.md` §4.4) → 🟢 FACT (`ADR-011`). Case/"both
    simultaneously" remains a separate, unresolved (likely Find-Hub-mediated) mechanism, not
    covered by this promotion.
  - **Wire-baseline firmware version `"release_5.203"`** (`PROTOCOL.md` §0.1, `CAP-023`) → 🟢
    FACT (`ADR-012`). `"Revision 6"`'s meaning stays open, unaffected.
  - **DLCI 0x02's general-purpose settings-write envelope shape** (`PROTOCOL.md` §4.5's shared
    preamble) → 🟢 FACT (`ADR-013`) — narrowly scoped to the outer `field5{field4{...}}}` wrapper
    only. The maintainer explicitly declined to blanket-promote the 9+ individual field mappings
    at the same time; each stays its own, separately-labeled 🟡 HYPOTHESIS, graded by actual
    evidence strength per setting (§4.5.1–§4.5.8 unchanged).
- **2026-08-23: `CAP-011` re-analyzed** after the maintainer spotted a 1% battery drop for both
  earbuds in the session recording. A frame-by-frame video re-derivation corrected the timestamp
  from an initial "~09:45:47" estimate (actually just the screen's first-opened values) to the
  actual change at 09:52:25.8. Wire analysis around that moment found a DLCI 0x08 private-envelope
  message (`Group 0x0e Code 0x01`) whose first two entries track the on-screen Left/Right
  percentages across all 4 occurrences in the log (including a further decline, 88%→87%→86%, in
  two off-camera recurrences), cross-confirmed by a second message (`Group 0x04 Code 0x03`).
  **Cross-checked same day against `CAP-001`/`CAP-002` (both 2026-08-09) and found a clean 3-for-3
  match — the message's 3rd entry is Case, not just Left/Right** — upgrading this from a
  single-session to a 3-session, 12-day-spanning finding (`CAP-011`'s own Case entry reads stale in
  that one session, flagged as its own anomaly, not treated as contradicting the mapping). Refines
  an already-known-but-undecoded message shape first seen in `CAP-002-FINDINGS.md` §2a
  (2026-08-12), not a newly-discovered packet type. **Reviewed and approved by the maintainer the
  same day** — promoted to 🟢 FACT (`DECISIONS.md` ADR-014) for the index→Left/Right/Case mapping
  specifically; `CAP-011`'s stale Case reading, the `flag` field's meaning, and the burst's
  trigger stay open, unaffected by the promotion.

- **2026-08-30: Phase 2 (APK reverse engineering) groundwork — governance, storage, and procedure.**
  The maintainer explicitly requested AI assistance with the *mechanical* parts of APK decompiling
  and proto-schema extraction; `DECISIONS.md` ADR-017 (superseding ADR-003, sign-off obtained on
  the exact wording before it was added) records the new boundary: an AI session may search, list
  candidates, run `pbtk`, and explain already-surfaced code or native `.so` disassembly output —
  the maintainer explicitly placed native `.so` disassembly assistance in scope too — but never
  decides relevance or promotes a `REVERSE_ENGINEERING.md` finding; `AGENTS.md` §6/§15's FACT/ADR
  sign-off requirement is unchanged. Also added: `WORKSTATION_PREPARATIONS.md`'s pbtk section,
  corrected against pbtk's own README rather than assumed (it has two extractors — Java/DEX and
  native-binary-with-reflection-metadata — so native `.so` extraction isn't ruled out the way this
  project first assumed, only unconfirmed against `libmaestro`/`libgfps` specifically); a versioned
  APK storage layout (`reverse-engineering/apk/v<versionName>-<versionCode>/`, indexed in the
  git-tracked `reverse-engineering/APK_VERSIONS.md`; the APK/decompiled/pbtk output itself is never
  committed, per an updated `.gitignore` covering the whole `reverse-engineering/apk/` tree);
  `APK_REVERSE_ENGINEERING_PROCEDURE.md` (pull/store → diff-against-previous-version → decompile →
  `pbtk` extract → keyword search, with an explicit out-of-scope exclusion list for
  AccountLinking/OwnershipTransfer/AccessoryNonOwner/Firebase-Analytics-Crashlytics); and
  `REVERSE_ENGINEERING.md` template updates (mandatory file+line citations, a per-finding
  hypothesis-to-capture-test link, and the same non-destructive-rewrite-in-place convention
  `CAP-NNN-FINDINGS.md` files use). `TODO.md`'s Phase 2 checklist updated to reflect this groundwork
  without checking off any actual analysis work, since no APK has been pulled yet.

- **2026-08-30: Tier 0 (existing-capture re-decode) + Tier 2 (`qjc`/`qja`'s remaining oneof groups)
  static-analysis session, then four pending FACT promotions reviewed and approved by the maintainer
  per-point** (`AGENTS.md` §6), recorded in `DECISIONS.md` ADR-019: **§2.2a** — DLCI 0x02's
  `field5{field4{...}}` wrapper's "..." confirmed 🟢 FACT to be `libmaestro`'s own recovered
  `WriteSetting` schema (`qhr`), byte-decoded for 2 sampled fields (4, 29) against `CAP-020`. **§4.5.3**
  — the top-level "Use touch controls" toggle (`field 4`) and the press-and-hold action-selection
  opcode (`field 7`/`qju`, with a corrected `qik`→`qho` nesting level) promoted to 🟢 FACT in full,
  each backed by both wire+video correlation and a self-describing app-code log message. The
  ANC-mode-rotation-checklist opcode's field number (`field 12`/`qht`) promoted to 🟢 FACT; its
  equivalence to the app's own "ANC gesture loop" name explicitly declined by the maintainer, staying
  🟡 HYPOTHESIS. Separately (Tier 2, not promoted, static-analysis-only): found that `qjc`/`qja`'s
  other 4 oneof groups (`qhx`/`qjn`/`qjt`/`qjv`) are very likely an **alternate product's** settings
  schema, not additional Buds Pro 2 categories — the app's `fya` settings-write interface has 3
  disjoint, DI-separated implementations, one per product variant, and `qjn`'s own internal codename
  (found in a log string) is literally "presto." `qjv` confirmed fully unused in this app version
  (zero construction sites anywhere in the decompiled tree).

- **2026-08-30: `CAP-027` (Group N, touch gestures) captured and analyzed.** `TOUCH-002`–`TOUCH-006`
  (tap/double-tap/triple-tap/swipe forward/swipe backward) confirmed 🟢 FACT as standard AVRCP `Pass
  Through`/`RegisterNotification(VolumeChanged)` traffic — a spec-compliant profile carried over its
  own L2CAP PSM, not an RFCOMM DLCI at all, the single most important structural finding of this
  capture. `TOUCH-007` (press-and-hold) instead rides DLCI 0x04's official Fast Pair Message Stream,
  Group `0x08` Code `0x13` ("Notify ANC state") — the same shape `PROTOCOL.md` §4.1 already documents
  for app-driven ANC taps, now also confirmed produced by the hardware gesture itself. See
  `CAP-027-FINDINGS.md`.
- **2026-08-30: `CAP-033` (Group AA, SDP isolation) captured and analyzed.** Tested whether the
  companion app's own "MAESTRO APP"/"default" internal-RFCOMM-socket SDP UUIDs (`DECISIONS.md`
  ADR-018) are visible in an OS-only, app-force-stopped SDP browse (`SDP-001`) — result capped at 🟡
  HYPOTHESIS by two isolation issues (Forget performed before Force-stop, not after as the procedure
  requires; step 3's app-open baseline comparison never executed), so this is not yet a clean answer
  either way; a repeat is needed. The browse's full named-service table is new, previously
  undocumented content: it independently corroborates ADR-018's DLCI 0x02 = "MAESTRO APP" finding at
  the wire/SDP level (not just APK code), and names DLCI 0x08 "GSND CONTROL" and DLCI 0x0a "GSND
  AUDIO" for the first time — new leads for `PROTOCOL.md` §2.3's/§6's open DLCI-0x08-identity
  question, proposed for maintainer review, not committed as a promotion. See `CAP-033-FINDINGS.md`.
- **2026-09-01: `CAP-034` (Group W, 4th attempt) captured and analyzed — resolves the
  `0x0c0X`/`0x0f2X` GATT handle↔UUID mapping, maintainer sign-off obtained.** Combined `CAP-014`'s
  confirmed-unlimited HCI snaplen with Group W's own long-untried cache-busting method
  (`pm clear com.android.bluetooth` on a Pixel 9a never before connected to this Buds unit) for the
  first time. The resulting discovery burst resolves the full 15-primary-service GATT profile:
  `0x0c00`–`0x0c14` = Google Fast Pair Service (all 5 spec-defined characteristics, plus Message
  Stream PSM and one still-unnamed characteristic), `0x0f20`–`0x0f2a` = Device Information,
  `0x0f30`–`0x0f33` = Battery Service. Corrects an earlier `CAP-017-FINDINGS.md` hypothesis that
  "Unknown Service" (`109b862f-…`) contained this cluster — it occupies a separate handle range and
  its own purpose remains unidentified. See `CAP-034-FINDINGS.md` and `PROTOCOL.md` §4.3 Option D/§6.
- **2026-09-02: `CAP-035` (Group AB, GMS-independence check) captured and analyzed, maintainer
  sign-off obtained.** Tested whether DLCI 0x08 ("GSND CONTROL")/0x0a ("GSND AUDIO")/0x06 ("DEBUG
  APP")/0x12 ("BTIS") depend on Google Play Services, on a GrapheneOS phone with GMS present but
  `dumpsys`-verified disabled. DLCI 0x08's content reproduces byte-identical across a fresh connect
  and a reconnect; DLCI 0x0a opens in lockstep but stays payload-silent both times; DLCI 0x06/0x12
  never open at all — clean negatives for both, the first time either has been specifically checked.
  Strengthens (does not fully close) `CAP-004-FINDINGS.md` §4a's existing "GMS present but disabled"
  finding — a repeat with GMS genuinely uninstalled would close it fully. See `CAP-035-FINDINGS.md`.
- **2026-09-03: documentation audit remediation** (maintainer-directed fixes following a 2026-09-02
  documentation audit): registered `CAP-034`/`CAP-035` in `id_registry.csv` (both had full Capture
  Index rows and were cited throughout `PROTOCOL.md`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md` but were never
  added to the registry); fixed a live CI "Lint docs" failure (added the deliberately-referenced,
  deleted `REVIEW_REPORT.md` to `scripts/lint_docs.py`'s historical-reference allowlist; repaired
  `CAP-035-EVENT-NOTES.md`'s footer, which pointed at a truncated folder path); corrected a stale,
  self-contradictory "not yet traced" note in `REVERSE_ENGINEERING.md`'s Call graph notes section
  (the `fsz`/`fux` → `MethodClient` chain it described as untraced had in fact been fully traced
  earlier in the same document); populated `REVERSE_ENGINEERING.md`'s previously-empty "Native
  libraries" table from the already-documented finding; added the missing extraction commands and
  raw hex to `CAP-008-FINDINGS.md`'s HFP-handshake and eSCO-setup sections, per `PROJECT_RULES.md`
  §1's hex-and-script rule (all of that capture's original conclusions were independently
  re-verified and confirmed correct in the process); refreshed two stale `TODO.md` status
  descriptions (the UUID register is no longer an empty template; the APK keyword-search pass has
  grown well past its originally-cited class-entry count). **`DECISIONS.md` ADR-020** — EQ's
  `FrameEncoder`/`FrameDecoder` implementation explicitly unblocked, closing a gap where `ADR-016`
  had promoted EQ's protocol knowledge to FACT without ever stating the `ARCHITECTURE.md` §5
  implementation gate was cleared (unlike ANC/`ADR-009` and Find My Buds/`ADR-011`); no new protocol
  knowledge, maintainer-approved.

- **2026-09-06: `CAP-037`–`CAP-042` (Groups AD–AI) captured and analyzed — six purpose-built
  repeats of `CAP-036`'s own open questions.** No new FACT promotions; large-scale replication of
  already-FACT findings (`CAP-037`: 26/26 same-session `ADR-022`/`ADR-024` replications; `CAP-039`:
  10 same-session `ADR-024` Set-vs-Get samples) plus several new open items (DLCI 0x08's unmapped
  Get-shaped codes still unattributed; the app's in-app Connect/Disconnect buttons producing zero
  wire signal; a `Settable-toggles` reading in tension with `ADR-024` immediately after physical
  case-removal; DLCI 0x02's connect-time burst shown length-invariant across differing settings
  states; the periodic cross-channel push shown far sparser over a genuinely idle window than
  `CAP-036`'s short sample suggested). See each capture's own findings file.
- **2026-09-07: project-wide audit + cross-validation review cycle, processed and closed out.**
  `AUDIT_REPORT_2026-09-07.md` (this session's own Phase-1/Phase-2 audit, cross-checking
  `CAP-037`–`CAP-042` against the decompiled APK source and auditing the project documentation) and
  `ANTIGRAVITY_AUDIT_REPORT_2026-09-07.md` (an independent external review, Antigravity/Gemini 3.1
  Pro) were cross-validated against each other in `EXTERNAL_REVIEW_VALIDATION_2026-09-07.md`, which
  rejected the external review's `🟢 FACT`-labeled `maestro_pw.Maestro` connect-burst-identity claim
  (an evidentiary overclaim *and* a governance violation — an AI report self-assigning FACT status
  and recommending an ADR, which `AGENTS.md` §6/`DECISIONS.md` ADR-017 do not permit) and its
  `REVIEW_REPORT.md`/"active workspace state" item (not a genuine finding about this repository).
  Confirmed findings were applied directly: **`DECISIONS.md` ADR-025** records that Google Play
  Services reverse-engineering is out of scope (no DLCI 0x04/0x08 transport code was found anywhere
  in the companion app's own decompiled source — both channels are implemented clean-room, from wire
  evidence alone *(wording corrected 2026-09-24 by ADR-025's Update: "independently", not "clean-room", `AGENTS.md` §12)*, exactly as ANC/Find My Buds/EQ already are), with a matching `PROJECT.md` non-goals
  bullet and an `ARCHITECTURE.md` note correcting the external review's "Impossibility"
  mischaracterization of this same situation. `PROTOCOL.md` §8's changelog backfilled for
  2026-09-04/05/06; §4.3 Option C's HFP DLCI corrected from a fixed `0x09` to session-local (`0x0c`
  in the clear majority of captures); §6 gained a `qhr`-field-13 candidate for `CAP-038`'s
  unexplained ANC Notify frames, an app-foreground-vs-IPC refinement for `CAP-042`'s open push-cadence
  question, and the `maestro_pw.Maestro` burst-identity claim recorded at the `🟡 HYPOTHESIS` level
  the evidence actually supports (not the rejected `🟢 FACT`). `README.md`'s two dangling references to a
  never-existent roadmap file removed and its "Current
  state" snapshot refreshed; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §9's stale `OBS-002` open-item removed;
  `reverse-engineering/APK_VERSIONS.md`/`WORKSTATION_PREPARATIONS.md`'s `pbtk` root-cause explanation
  reconciled with `TODO.md`'s more precise diagnosis; `REVERSE_ENGINEERING.md` gained a missing `###`
  header, a note explaining why the Message Group/Code register stays empty for DLCI 0x04/0x08, and a
  low-priority citation-fragility note. All three review documents retired after processing (same
  lifecycle as `AUDIT_REPORT_2026-08-22.md`) — added to `scripts/lint_docs.py`'s historical-reference
  allowlist so the citations to them throughout the docs above don't lint as dead references.

### Removed

- `PROTOCOL_NOTES.md`, `EXPERIMENTS.md` (retired 2026-08-15, see above).
- `AUDIT_REPORT_2026-09-07.md`, `ANTIGRAVITY_AUDIT_REPORT_2026-09-07.md`,
  `EXTERNAL_REVIEW_VALIDATION_2026-09-07.md` (retired 2026-09-07, see above — findings applied,
  historical references kept where cited).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/CHANGELOG.md - https://tedsluis.github.io/opencontrolpixelbudspro2/CHANGELOG
