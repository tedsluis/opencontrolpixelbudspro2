# CHANGELOG.md

All notable changes to this project are documented in this file. Format loosely
based on [Keep a Changelog](https://keepachangelog.com/).

This project is currently in the reverse-engineering phase — there is no
working app yet, so entries so far are documentation, tooling, and process
rather than app releases. See `TODO.md` for current status and `PROJECT.md`
for the "definition of done" that will mark v1.

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

### Removed

- `PROTOCOL_NOTES.md`, `EXPERIMENTS.md` (retired 2026-08-15, see above).