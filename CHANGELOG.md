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

### Removed

- `PROTOCOL_NOTES.md`, `EXPERIMENTS.md` (retired 2026-08-15, see above).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/CHANGELOG.md - https://tedsluis.github.io/opencontrolpixelbudspro2/CHANGELOG
