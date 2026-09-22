# CHANGELOG.md

All notable changes to this project are documented in this file. Format loosely
based on [Keep a Changelog](https://keepachangelog.com/).

Most entries below are documentation, tooling, and protocol-reconstruction process
rather than app releases, reflecting this project's reverse-engineering-first approach — a v1
Android app now exists (`ai-sessions/0033`, 2026-09-18) but is not yet hardware-verified or
released. See `TODO.md` for current status and `PROJECT.md` for the "definition of done" that will
mark v1.

## [Unreleased]

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
  (**ADR-038**); the per-earbud dock-state candidate (`Group 0x04 Code 0x12`) showed no correlation with handling in this capture (still 🔴 open); the ANC Quick
  Settings tile's code was already correct — it was simply never added to the panel — so the app now offers `StatusBarManager.requestAddTileService()` and a
  redrawn tile icon. Every "(last known)"/"last seen" qualifier is now an actual wall-clock timestamp, threaded through the same per-feature flows with no new
  polling (`ancModeUpdatedAt`/`eqProfileUpdatedAt`/`batteryStatusUpdatedAt`/`dockStateUpdatedAt`). The five tabs now support left/right swipe navigation
  (`HorizontalPager`) kept in sync with the bottom nav bar and the existing single-top back-stack semantics (`ai-sessions/0037`). A session-loss message that
  stayed generic even when Android's own link state already explained the cause now uses that state. 1359 tests, lint 0 errors. Nothing is hardware-verified.
- **2026-09-20 (`ai-sessions/0041`): pairing/permission fixes, Android-state mirroring, charging flag, EQ read.** Fixed the in-app pairing failure
  "Could not resolve the selected device" (CDM's lower-case `MacAddress` was passed to `getRemoteDevice`, which requires upper-case; association reuse and
  duplicate clean-up, already-bonded is success, distinct bond-failure reasons); added the runtime-permission flow the app never had (its absence made a
  cleared-data app see "no bonded device"); the Connection screen now mirrors Android's Bluetooth state (`OsConnectionObserver`, `DeviceStatus`); the battery
  decoder reads the charging flag (ADR-033 update, maintainer-accepted); DLCI 0x02 is decoded as pw_hdlc + pw_rpc (ADR-034, `PROTOCOL.md` §2.2a/§4.2 promotions,
  a codec correction: the old "correlation byte" was the `channel_id`, defaulted to 0), the EQ is read at Connect and write results are surfaced; `HfpBatteryReader`
  logs every headset broadcast action (removal is a later session). `scripts/pwrpc_decode.py` gained frame numbers, `--eq`, `--channels` and a corrected packet-type
  table. Tests: 1335 (1281 before), 0 failures; not hardware-verified.
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
  broadcast string remains usable by third-party apps), and found one genuine error: `PROTOCOL.md`
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
  evidence alone, exactly as ANC/Find My Buds/EQ already are), with a matching `PROJECT.md` non-goals
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
