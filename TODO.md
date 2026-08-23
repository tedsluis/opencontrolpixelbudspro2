# TODO.md

Open tasks, grouped by phase. Check items off and move completed major items
to `CHANGELOG.md` (see `PROJECT_RULES.md` §6, rule 13, on technical debt
tracking).

## Recommended priority order (added 2026-08-23)

A cross-phase execution order, distinct from the phase grouping below (which organizes tasks by
*kind*, not by *when to do them*). This section is a sequencing layer only — each item's full
description still lives in its own phase/section below (or in `PROTOCOL.md`/`ARCHITECTURE.md` for
protocol/architecture open questions, per this file's own "Open questions" section at the bottom);
nothing here is a second copy of that detail, only a pointer plus the reasoning for the ordering.

1. **Decisions & sign-offs (no new data needed — cheapest, unlocks the most):**
   - Maintainer sign-off on pending 🟢 FACT promotions — see the new Phase 3 item below for the
     current list. Per `AGENTS.md` §6 this step can only be done by the maintainer, not an agent.
   - DI approach (Hilt vs. manual) — Phase 4.
   - Minimum Android API level — Phase 5.
2. **Start Phase 4 app development, ANC-first:** ANC is the only command that is fully 🟢 FACT
   *and* implementation-unblocked (`DECISIONS.md` ADR-009) — building it end-to-end (transport →
   framing → UI) is the cheapest way to prove the whole architecture works. Battery via HFP
   (`PROTOCOL.md` §4.3 Option C, also already 🟢 FACT) is the natural second target — together
   they cover most of `PROJECT.md`'s "Definition of done (v1)".
3. **Start Phase 2 (APK reverse engineering) — currently 0% done.** Can run independently of new
   captures and is likely to cheaply resolve several open `.proto`-field-number and HID-relevance
   questions that captures alone can't (see `REVERSE_ENGINEERING.md`, updated 2026-08-23 with
   HID-related keywords).
4. **Remaining planned captures**, in the order given under Phase 1 below — `CAP-008`/`CAP-009`
   first (combinable in one session), then a clean connection-free repeat of the Battery
   Notification BLE scan (`CAP-011` was inconclusive), then `CAP-018`, `CAP-014`, `CAP-013`, and
   the still-uncaptured main-run-through remainder (`CAP-026`–`CAP-030`).
5. **Targeted research follow-ups**, lowest priority, tracked at their source per this file's
   "Open questions" section: the `CAP-021` DLCI 0x0a burst trigger and the DLCI 0x02 AES-128
   hypothesis (`PROTOCOL.md` §6) — the latter is only really testable once Phase 2 above provides
   a pw_rpc/protobuf schema to check against.

## Setup

- [x] Set up the Fedora development workstation (`WORKSTATION_PREPARATIONS.md`)
- [x] Claude Code and Google Antigravity installed and configured
- [x] GitHub repository created + first commit
- [x] License chosen — AGPL-3.0 (see `DECISIONS.md` ADR-002, `LICENSE`)
- [x] Core project documentation drafted: `AGENTS.md`, `PROJECT.md`,
      `PROJECT_RULES.md`, `ARCHITECTURE.md`, `PROTOCOL.md`, `PROTOCOL_NOTES.md`
      (retired 2026-08-15, see `CHANGELOG.md`),
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
  - [x] `PROJECT_RULES.md`, `PROJECT.md`, `DECISIONS.md`, `PROTOCOL.md`,
        `REVERSE_ENGINEERING.md`, `README.md`,
        `CHANGELOG.md`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`,
        `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
        `WORKSTATION_PREPARATIONS.md` — given a dedicated cross-consistency
        pass via the 2026-08-20 comprehensive documentation audit (see
        `CHANGELOG.md`) and a further external audit on 2026-08-22/23 (see
        `AUDIT_REPORT_2026-08-22.md` and `CHANGELOG.md`'s matching entry).
        This checklist item was left unchecked after that work already
        completed it — closed here to fix the staleness itself.

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

**Top priority (updated 2026-08-18) — these block implementation-readiness
for the app's core v1 features and outrank everything else below, including
the still-open edge-case protocol questions (DLCI 0x08's identity, Groups
0x04/0x05/0x09's semantics, the CTKD generalization, HFP `battchg` vs.
`AT+BIEV` discrepancy, etc. — those stay valuable research but are explicitly
lower priority than finishing ANC/Battery/EQ):**

- [x] **`CAP-005`/`CAP-015` (Group T) — EQ command isolation.** **Done
      2026-08-18** via `CAP-015`, a second, independent Group T session:
      field-to-band mapping promoted to 🟢 FACT (all 5 sliders individually
      isolated, 3 passes each), plus the ±6.0 band-gain clamp and a confirmed
      preset-quintet reference table (`CAP-015-FINDINGS.md`, `PROTOCOL.md`
      §4.2).
- [x] **`CAP-006` (Group B repeat) — ANC reliability confirmation.** **Done
      2026-08-15** — isolated single-tap repeat of all four ANC modes;
      exactly 4 `0x12` "Set ANC state" frames in the whole log, one per tap,
      zero misses (`CAP-006-FINDINGS.md` §3). `CAP-001`'s 2/6 gap does not
      reproduce under isolated conditions. `DECISIONS.md` ADR-009 updated,
      `FrameEncoder` implementation block for the ANC command **lifted**.
- [x] **`CAP-010`/`CAP-017` (Group W) — stronger GATT cache-busting for live
      service discovery.** **Discovery goal achieved 2026-08-16** via
      `CAP-017`, a fresh-GATT-client-app path not originally in this row's
      scope (`pm clear`/Pixel-9a remain untried alternates, now lower
      priority) — 137 live discovery frames, full 15-service GATT profile
      recovered. **Not fully closed:** that session's wire log is
      snaplen-truncated, so the `0x0f2a`/`0x0c0X` handle→UUID mapping is
      still open — a snaplen fix + on-screen characteristic drill-down is
      `CAP-014` (planned, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9).
- [x] **`CAP-016` (Group U re-run) — case/bud-removal hardware events.**
      **Synced into `PROTOCOL.md` 2026-08-18** — promotes 3 🟢 FACTs (§5/§7):
      Buds-initiated reconnect on bud removal, ACL disconnect the instant
      both buds are re-docked, and case-lid open/close producing zero wire
      signal (now 2-capture-confirmed). New open items (RFCOMM channel-bounce
      trigger, ANC settable-toggles byte, a `0x0044` BLE notification burst,
      an `AndroidHeadTracker` HID Feature report) tracked in `PROTOCOL.md` §6
      and `CAP-016-FINDINGS.md`.
- [ ] **`CAP-008` (Group V, planned) — first real phone call.** Resolves
      whether HFP AT-command SLC setup reoccurs. Channel 5/DLCI 0x0a is no
      longer known to be universally silent — `CAP-021` (2026-08-21, Group G)
      recorded a 1123-frame payload burst on it, unrelated to any call
      (`CAP-021-FINDINGS.md` §4a, `PROTOCOL.md` §6) — but what triggers that
      burst, and whether a call also produces one, remains open.
- [ ] **`CAP-009` (Group X, planned) — battery-level discrepancy bracket.**
      Cross-check `AT+CIND`/`battchg` against `AT+BIEV` over a natural
      battery decline — genuinely open, not yet captured; combinable with
      `CAP-008`'s session if timing allows.

**Next, still important but behind the above:**

- [x] **Capture the "Play sound on Left/Right earbud" (Find My Buds) action —
      done, `CAP-025` (2026-08-21).** Left/Right confirmed 🟡 HYPOTHESIS
      (strong), video-correlated, proposed for `PROTOCOL.md` §4.4 promotion
      to 🟢 FACT pending maintainer sign-off. **New finding:** Case/"both"
      route through a separate Find Hub/Find-My-Device-Network mechanism
      with no local wire command — possibly a Zero-GMS hard limit, flagged
      to the maintainer in `PROTOCOL.md` §6 (Behavior) and
      `CAP-025-FINDINGS.md` §7/§8.
- [x] **Passively capture a BLE scan to confirm the Battery Notification
      advertisement — attempted, `CAP-011` (2026-08-21), inconclusive.**
      Fast Pair Service (`0xFE2C`) traffic confirmed present, but the
      procedure deviated (an active RFCOMM connection was present
      throughout, not the intended connection-free scan) and the sampled
      payloads don't structurally match the documented byte layout — see
      `PROTOCOL.md` §4.3 Option A and `CAP-011-FINDINGS.md`. **Still open:**
      a genuinely clean, connection-free repeat is needed.
- [ ] **Added 2026-08-23 — remaining planned captures not yet individually tracked here** (each
      already has its own row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index; listed here
      only so this file's priority ordering covers them too, not as a duplicate description):
      `CAP-018` (Group Y, `0x0044` BLE-notification-burst isolation), `CAP-013` (Group A repeat,
      whether "Forget" fully clears prior BLE association), and the still-uncaptured main
      run-through remainder — `CAP-026` (Group L, passive observation), `CAP-027` (Group N, touch
      gestures), `CAP-028` (Group O, head gestures, needs `CAP-020`'s Head-gestures toggle left
      on), `CAP-029` (Group P, Conversation Detection voice trigger + the optional, destructive
      factory-reset comparison + the still-open shorter-press pairing-mode question), and
      `CAP-030` (Group Q items #19–20, Loud Noise Protection/Adaptive Audio, needs firmware
      ≥4.467). Lower priority than `CAP-008`/`CAP-009`/a clean `CAP-011` repeat above.

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
      Fast Pair "Hearable Controls" spec + `CAP-001`'s existing capture.
      `DECISIONS.md` ADR-009 (added 2026-08-15) blocked `FrameEncoder` for
      this command pending `CAP-006`, since 2 of `CAP-001`'s 6 ANC taps
      produced no command frame. **`CAP-006` (2026-08-15) resolved this — 4/4
      isolated taps produced a matching frame, zero misses — and ADR-009 was
      updated to lift the block.** `FrameEncoder`/`FrameDecoder` for the ANC
      command is now implementation-ready per `AGENTS.md` §6.
- [ ] Log every hypothesis test in the relevant capture's `CAP-NNN-FINDINGS.md` before promoting a finding
      from HYPOTHESIS to FACT (`PROJECT_RULES.md` §4)
- [ ] **Added 2026-08-23 — maintainer sign-off session on pending FACT promotions.** Per
      `AGENTS.md` §6 an agent may propose but never commit these; all evidence is already
      gathered and just needs review: Find My Buds Left/Right (`PROTOCOL.md` §4.4, `CAP-025`),
      the wire-baseline firmware version `"release_5.203"` (`PROTOCOL.md` §0.1, `CAP-023`), and
      the general-purpose DLCI 0x02 settings-write envelope plus its 9+ individual field mappings
      (`PROTOCOL.md` §4.5, `CAP-019`–`CAP-024`). See `TODO.md`'s priority-order section above —
      this is the cheapest, highest-leverage next step (no new capture needed).

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
      (`AGENTS.md` §11). **Recommended first target (added 2026-08-23): ANC**
      (DLCI 0x04 Group `0x08`) — the only command that is both 🟢 FACT and
      implementation-unblocked today (`DECISIONS.md` ADR-009); cheapest way to
      prove the transport/framing/UI pipeline end-to-end.
- [ ] Implement `BudsTransport` (RFCOMM primary, secondary GATT for
      case/charging characteristics) and `ConnectionStateMachine`
      (`ARCHITECTURE.md` §2.1)
- [ ] Implement `BudsRepository` / `BudsRepositoryImpl` wiring `:data` to
      `:domain` (`ARCHITECTURE.md` §2.1, `DECISIONS.md` ADR-001)
- [ ] First working end-to-end connection + battery status shown in the UI.
      **Recommended mechanism (added 2026-08-23): HFP** (`PROTOCOL.md` §4.3
      Option C) — already 🟢 FACT and not blocked, unlike Option A (still
      inconclusive, see Phase 1) or Option B (battery message code
      unconfirmed).

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