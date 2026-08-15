# TODO.md

Open tasks, grouped by phase. Check items off and move completed major items
to `CHANGELOG.md` (see `PROJECT_RULES.md` §6, rule 13, on technical debt
tracking).

## Setup

- [x] Set up the Fedora development workstation (`WORKSTATION_PREPARATIONS.md`)
- [x] Claude Code and Google Antigravity installed and configured
- [x] GitHub repository created + first commit
- [x] License chosen — AGPL-3.0 (see `DECISIONS.md` ADR-002, `LICENSE`)
- [x] Core project documentation drafted: `AGENTS.md`, `PROJECT.md`,
      `PROJECT_RULES.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `PROTOCOL_NOTES.md`,
      `REVERSE_ENGINEERING.md`, `DECISIONS.md`, `CHANGELOG.md`, `README.md`,
      `CAPTURE_BLUETOOTH_HCI_SNOOP.md`, `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
      `SCREENSHOTS_PIXEL_BUDS_APP.md`, `SCREENSHOTS_PIXEL_BUDS_WEB_APP.md`
- [ ] Review the repository for inconsistencies, vagueness, ambiguity,
      contradictions, errors, or undocumented choices — both within each file
      and between files — and address what's found. Status so far:
  - [x] `ARCHITECTURE.md` — reviewed and revised (transport layer naming
        aligned with RFCOMM-primary reality, DI/scanning-policy open questions
        added)
  - [x] `AGENTS.md` — reviewed and revised (stale license section, BLE-only
        framing assumption, and duplicate heading artifacts fixed)
  - [ ] `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md`, `PROTOCOL.md`,
        `REVERSE_ENGINEERING.md`, `README.md`,
        `CHANGELOG.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
        `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
        `WORKSTATION_PREPARATIONS.md` — not yet given a dedicated
        cross-consistency pass; lower risk since most were authored after the
        others, but still pending explicit review.

## Phase 1 — Bluetooth analysis

- [x] **Pipeline validation** — the HCI snoop → bugreport → `btsnooz.py`
      extraction → Wireshark chain (RFCOMM/SPP + BLE dissectors) confirmed
      working via `CAP-001` (Group Z), 2026-08-09. Logged in the Capture
      Index (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9).
- [x] **Pairing/bonding baseline** — forget-and-re-pair captured via `CAP-002`
      (Group A), 2026-08-09; a second, independent baseline via `CAP-003`
      (Group R) and a third via `CAP-004` (Group S). Logged in the Capture
      Index.
- [ ] Log every capture session in the Capture Index
      (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9) with a unique `CAP-NNN` ID and
      metadata (firmware version, Android version, app version, capture
      method — per `PROJECT_RULES.md` rule 11 and rule 14) — ongoing practice,
      not a one-time task; kept unchecked deliberately.
- [ ] Optionally, as a deliberate one-time capture (not before), trigger the
      factory-reset re-pair for comparison (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`
      §4.1 Group P #16 — destructive, also resets the Find My Device link, so
      this is a bonus capture, not a prerequisite). See also
      `WORKSTATION_PREPARATIONS.md`'s Disaster Recovery section — this is the
      same procedure, deliberately triggered as an experiment rather than as
      an emergency recovery step.

**Top priority (updated 2026-08-15) — these two block implementation-readiness
for the app's core v1 features and outrank everything else below, including
the still-open edge-case protocol questions (DLCI 0x08's identity, Groups
0x04/0x05/0x09's semantics, the CTKD generalization, HFP `battchg` vs.
`AT+BIEV` discrepancy, etc. — those stay valuable research but are explicitly
lower priority than finishing ANC/Battery/EQ):**

- [ ] **`CAP-005` (Group T) — EQ command isolation.** EQ's command channel is
      completely unattributed and is this project's original, still-unmet
      implementation goal alongside ANC and Battery
      (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group T, `PROTOCOL.md` §4.2).
- [ ] **`CAP-006` (Group B repeat) — ANC reliability confirmation.** Blocks
      `FrameEncoder` implementation for the ANC command per `DECISIONS.md`
      ADR-009 — `CAP-001`'s only evidence has 2 of 6 taps producing no command
      frame, not yet resolved as UI-state-realization vs. a real reliability
      gap.
- [ ] **`CAP-010` (Group W) — stronger GATT cache-busting for live service
      discovery.** Three captures (`CAP-002`–`CAP-004`) have all failed to
      trigger live GATT discovery via bond removal; Group W is the untried,
      stronger candidate (`pm clear com.android.bluetooth` + BLE-tool cache
      clear, see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group W). Needed to resolve
      the `0x0f2a`/`0x0c0X` handle→UUID gap.

**Next, still important but behind the above:**

- [ ] Capture the "Play sound on Left earbud" (Find My Buds) action
      (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 Group K) — no longer the top
      framing-verification priority now that ANC's channel is independently
      confirmed (`PROTOCOL.md` §4.1), but still an unattributed command worth
      capturing.
- [ ] Passively capture a BLE scan (no active connection) to confirm the
      Battery Notification advertisement byte-for-byte against the official
      spec (`PROTOCOL.md` §4.3 Option A, planned as `CAP-011`) — don't
      combine this with RFCOMM framing analysis. This is a one-off research
      capture, not a template for the app: the production app's own BLE
      scanning stays governed by the narrower bounded exception in
      `AGENTS.md` §7 / `DECISIONS.md` ADR-006 regardless of how broad this
      one-time capture is.

## Phase 2 — APK reverse engineering

- [ ] Obtain the official APK (from the maintainer's own device) and record
      its SHA-256 hash (`REVERSE_ENGINEERING.md` APK metadata table)
- [ ] Run JADX decompilation into `reverse-engineering/apk/jadx-output/`
- [ ] Run apktool decompilation into `reverse-engineering/apk/apktool-output/`
- [ ] Identify BLE/GATT-related classes **and** RFCOMM/Fast-Pair-related
      classes (`BluetoothSocket`, `MessageStream`, `AccountKey`, `FastPair`) —
      see the keyword list in `REVERSE_ENGINEERING.md` §Method and
      `AGENTS.md` §13
- [ ] Extract real `.proto` schemas via `pbtk` (do not hand-reconstruct field
      numbers from decompiled getter/setter names — see
      `REVERSE_ENGINEERING.md` known limitations)

## Phase 3 — Protocol reconstruction

- [ ] Fill in the UUID register (`REVERSE_ENGINEERING.md` §UUID register)
- [ ] Fill in the Message Group/Code register, if the Fast Pair Message Stream
      framing hypothesis is confirmed (`REVERSE_ENGINEERING.md` §Message
      Group/Code register)
- [ ] Resolve the framing question — Message Stream vs. proprietary envelope
      (`PROTOCOL.md` §2.3) and record the determination as a `DECISIONS.md`
      ADR — this blocks implementing `FrameEncoder`/`FrameDecoder` (see
      `AGENTS.md` §6, `ARCHITECTURE.md` §2.1)
- [ ] Document the full connection lifecycle with real capture evidence
      (`PROTOCOL.md` §5, currently an ⚪ ASSUMPTION)
- [ ] Bring the first command (e.g. battery status via the Fast Pair Battery
      Notification, `PROTOCOL.md` §4.3 Option A) to full 🟢 FACT status
- [x] Bring ANC mode switching to full 🟢 FACT status (`PROTOCOL.md` §4.1) —
      **done 2026-08-12** via deskresearch correlation against the official
      Fast Pair "Hearable Controls" spec + `CAP-001`'s existing capture. **Not
      the same as implementation-ready:** `DECISIONS.md` ADR-009 (added
      2026-08-15) explicitly blocks `FrameEncoder` for this command pending
      `CAP-006` — 2 of `CAP-001`'s 6 ANC taps produced no command frame, a gap
      not yet explained. See `CAP-006` under Phase 1's top-priority list above.
- [ ] Log every hypothesis test in the relevant capture's `CAP-NNN-FINDINGS.md` before promoting a finding
      from HYPOTHESIS to FACT (`PROJECT_RULES.md` §4)

## Phase 4 — App development

- [ ] Set up the Android Studio project per `ARCHITECTURE.md` (five Gradle
      modules: `:app`, `:ui`, `:domain`, `:data`, `:hardware`)
- [ ] Decide dependency injection approach — Hilt vs. manual service locator —
      and record it in `DECISIONS.md` (currently open, see `ARCHITECTURE.md`
      §10/§15)
- [x] Decide the passive-scanning policy for the Fast Pair Battery
      Notification — resolved as a bounded exception (filtered,
      foreground-triggered, time-boxed); see `DECISIONS.md` ADR-006,
      `AGENTS.md` §7, `ARCHITECTURE.md` §9.1
- [ ] Implement `ProtocolCodec` (`FrameEncoder`/`FrameDecoder`) with unit tests
      for the first confirmed command(s), against fixed byte-array fixtures
      (`AGENTS.md` §11)
- [ ] Implement `BudsTransport` (RFCOMM primary, secondary GATT for
      case/charging characteristics) and `ConnectionStateMachine`
      (`ARCHITECTURE.md` §2.1)
- [ ] Implement `BudsRepository` / `BudsRepositoryImpl` wiring `:data` to
      `:domain` (`ARCHITECTURE.md` §2.1, `DECISIONS.md` ADR-001)
- [ ] First working end-to-end connection + battery status shown in the UI

## Phase 5 — Testing & documentation

- [ ] Execute `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` on at least 2 devices
      (differing Android version and/or OEM, including GrapheneOS as the
      primary reference target per `ARCHITECTURE.md` §1)
- [ ] Update `README.md` with build instructions once the app builds
- [ ] Decide minimum supported Android API level and record it in
      `DECISIONS.md` (currently open, see `ARCHITECTURE.md` §15)
- [ ] Decide multi-device (multiple paired Buds) support for v1 and record it
      in `PROJECT.md` scope + `DECISIONS.md` (currently open, see
      `ARCHITECTURE.md` §15)
- [ ] Prepare the first public release (tag, `CHANGELOG.md` entry, GitHub
      Release per the manual-update-distribution decision in `AGENTS.md` §1)

## Known technical debt

_(Fill in as quick fixes are made — see `PROJECT_RULES.md` rule 13. Every
entry here should be short-lived: either resolved properly or promoted to a
tracked task above.)_

## Open questions

Open architectural and protocol questions are tracked **at their source only**
— this file does not keep a second, synchronized checkbox list of them, since
that duplication is exactly what caused this file to fall out of sync with
`ARCHITECTURE.md` once already (see `CHANGELOG.md`). Each question has exactly
one home:

- **Protocol-level open questions** (framing hypothesis, unconfirmed opcodes,
  wire-visibility of on-device-only features, etc.) → `PROTOCOL.md` §6.
- **Architecture-level open questions** (DI framework, minimum Android API
  level, multi-device scope, etc.) → `ARCHITECTURE.md` §15.

Check those sections directly when deciding what's still undecided; resolving
one only requires updating it in that one place, plus a `DECISIONS.md` entry
where the rule requires one (`PROJECT_RULES.md` §3).