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
4. **Remaining planned captures** (updated 2026-08-28 — `CAP-008`, `CAP-009`, `CAP-013`, `CAP-014`
   are done, see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9): a clean connection-free repeat of the Battery
   Notification BLE scan (`CAP-011` was inconclusive), a genuine attempt at Group W's own untried
   cache-busting methods (`pm clear com.android.bluetooth` or the Pixel 9a — `CAP-010`/`CAP-017`/
   `CAP-014` all failed to try either), then `CAP-018` and the still-uncaptured main-run-through
   remainder (`CAP-026`–`CAP-030`) — `CAP-027`/`CAP-028` (physical touch/head gestures) are the
   highest-value of these, being core v1 features with zero capture coverage so far.
5. **Targeted research follow-ups**, lowest priority, tracked at their source per this file's
   "Open questions" section: the `CAP-021` DLCI 0x0a burst trigger and the DLCI 0x02 AES-128
   hypothesis (`PROTOCOL.md` §6) — the latter is only really testable once Phase 2 above provides
   a pw_rpc/protobuf schema to check against. **Added 2026-08-28
   (2026-08-28 project-wide audit, Phase 5), three specific new-capture ideas, none yet designed
   in `CAPTURE_BLUETOOTH_HCI_SNOOP.md`:**
   - `HOLD-005`'s Left/Right ANC-rotation-checklist split (`PROTOCOL.md` §6) — a purpose-built
     capture isolating one earbud's rotation list at a time (the envelope carries no
     Left/Right-distinguishing field for this specific write, unlike `HOLD-001`–`HOLD-004`).
   - Volume balance (`field 17`) scale/direction (`CAP-022-FINDINGS.md` §5, `PROTOCOL.md` §4.5.7/§6)
     — a capture with isolated extreme-position samples (not a continuous drag) plus tighter video
     correlation.
   - The `CAP-021` DLCI 0x0a burst trigger, more precisely: a purpose-built hypothesis test
     (`PROJECT_RULES.md` §4's fixed template — hypothesis, setup, expected outcome, actual outcome,
     conclusion) bracketing candidate triggers one at a time (app backgrounded/foregrounded, a
     scheduled sync window, a charge-state change) — the burst recurred in exactly 1 of 16 sessions
     checked so far, so passively waiting for it to reappear is not expected to work.

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
- [x] **`CAP-010`/`CAP-017`/`CAP-014` (Group W) — stronger GATT cache-busting for live
      service discovery.** **Discovery goal achieved 2026-08-16** via
      `CAP-017`, a fresh-GATT-client-app path not originally in this row's
      scope — 137 live discovery frames, full 15-service GATT profile
      recovered. **`CAP-014` (2026-08-27) fixed that session's snaplen truncation but still
      did not close the mapping** — the `0x0f2a`/`0x0c0X` handle→UUID mapping remains open
      (`CAP-014-FINDINGS.md` §4/§8): 3 attempts now, and `pm clear com.android.bluetooth`/the
      Pixel 9a — Group W's own actual candidate methods — remain untried in all of them. That
      combination (proven snaplen fix + an actually-untried cache-busting method) is the clear
      next step.
- [x] **`CAP-016` (Group U re-run) — case/bud-removal hardware events.**
      **Synced into `PROTOCOL.md` 2026-08-18** — promotes 3 🟢 FACTs (§5/§7):
      Buds-initiated reconnect on bud removal, ACL disconnect the instant
      both buds are re-docked, and case-lid open/close producing zero wire
      signal (now 2-capture-confirmed). New open items (RFCOMM channel-bounce
      trigger, ANC settable-toggles byte, a `0x0044` BLE notification burst,
      an `AndroidHeadTracker` HID Feature report) tracked in `PROTOCOL.md` §6
      and `CAP-016-FINDINGS.md`.
- [x] **`CAP-008` (Group V) — first real phone call.** **Done 2026-08-26.** Both open
      questions resolved: the full HFP AT-command SLC handshake reoccurs on a fresh classic-link
      connection, and two clean SCO/eSCO pairs appear, one per call. DLCI 0x0a stayed silent
      through both calls, ruling it out as the call's audio path (`CAP-021`'s later, unrelated
      1123-frame burst on that same DLCI remains a separate, still-open question — `PROTOCOL.md`
      §6). See `CAP-008-FINDINGS.md`.
- [x] **`CAP-009` (Group X) — battery-level discrepancy bracket.** **Done 2026-08-23.**
      `AT+CIND`/`battchg` confirmed a stale single snapshot; `AT+BIEV` confirmed per-earbud
      (Right, this session), not a fixed aggregate, and non-fixed-cadence — `BATT-006` closed,
      maintainer-approved (`DECISIONS.md` ADR-015). See `CAP-009-FINDINGS.md`.

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
- [x] **`CAP-013`/`CAP-031`/`CAP-032` (Group A repeat) — whether "Forget" fully clears prior BLE
      association.** **Done 2026-08-27**, on the fourth attempt (`CAP-032`) — the first three
      (`CAP-001`'s original session, `CAP-013`, `CAP-031`) all either predate the question or
      failed to capture the pre-clearing-action window; `CAP-032`, extracted via the raw path
      instead of the lossy `btsnooz` fallback, finally captured it and found a clean
      counter-example (no prior BLE link/valid key for that session) — `CAP-001`'s own
      session-specific puzzle (why *that* session had residual state) remains independently open,
      see `PROTOCOL.md` §6 (Behavior). See `CAP-032-FINDINGS.md`.
- [ ] **Updated 2026-08-28 — remaining planned captures not yet individually tracked here** (each
      already has its own row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index; listed here
      only so this file's priority ordering covers them too, not as a duplicate description):
      `CAP-018` (Group Y, `0x0044` BLE-notification-burst isolation), and the still-uncaptured main
      run-through remainder — `CAP-026` (Group L, passive observation), `CAP-027` (Group N, touch
      gestures — never attempted, a core v1 feature area with zero capture coverage so far),
      `CAP-028` (Group O, head gestures, needs `CAP-020`'s Head-gestures toggle left on — also
      never attempted), `CAP-029` (Group P, Conversation Detection voice trigger + the optional,
      destructive factory-reset comparison + the still-open shorter-press pairing-mode question),
      and `CAP-030` (Group Q items #19–20, Loud Noise Protection/Adaptive Audio, needs firmware
      ≥4.467 — worth double-checking this against the project's `release_5.203` baseline first,
      since the two version identifiers have never been explicitly reconciled, `PROTOCOL.md` §0.1).
      Lower priority than a clean `CAP-011` repeat and a properly-done Group W attempt above.

## Phase 2 — APK reverse engineering

- [x] **Groundwork/tooling — done 2026-08-30.** Governance, storage, and procedure now in place so
      the actual analysis work below can start; none of it constitutes analysis having happened yet:
      `DECISIONS.md` ADR-017 (supersedes ADR-003) permits AI mechanical assistance — search, `pbtk`
      extraction, native `.so` disassembly explanation — within a maintainer-decides-relevance
      boundary; `WORKSTATION_PREPARATIONS.md` documents `pbtk` installation/real scope/dependencies;
      a versioned APK storage structure exists (`reverse-engineering/apk/v<versionName>-<versionCode>/`,
      indexed in the git-tracked `reverse-engineering/APK_VERSIONS.md`, itself gitignored for the
      APK/decompiled output per `.gitignore`); `APK_REVERSE_ENGINEERING_PROCEDURE.md` documents the
      full pull → decompile → extract → search → analyze procedure, including the
      diff-against-previous-version pass and the out-of-scope exclusion list (AccountLinking/
      OwnershipTransfer/AccessoryNonOwner/Firebase-Analytics-Crashlytics); `REVERSE_ENGINEERING.md`'s
      template now requires a file+line citation per finding and a hypothesis-to-capture-test link.
- [ ] Obtain the official APK (from the maintainer's own device), record it in
      `reverse-engineering/APK_VERSIONS.md` (SHA-256, versionName/versionCode, pull date, source
      device, provenance), and store it under `reverse-engineering/apk/v<versionName>-<versionCode>/`
      per `APK_REVERSE_ENGINEERING_PROCEDURE.md` §2 — **not started**, despite the groundwork above.
- [ ] Run JADX decompilation into `reverse-engineering/apk/v<versionName>-<versionCode>/jadx-output/`
- [ ] Run apktool decompilation into `reverse-engineering/apk/v<versionName>-<versionCode>/apktool-output/`
- [ ] Identify BLE/GATT-related classes **and** RFCOMM/Fast-Pair-related
      classes (`BluetoothSocket`, `MessageStream`, `AccountKey`, `FastPair`) —
      see the keyword list in `REVERSE_ENGINEERING.md` §Method,
      `APK_REVERSE_ENGINEERING_PROCEDURE.md` §4's search-efficiency techniques, and
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
- [x] **Added 2026-08-23 — maintainer sign-off session on pending FACT promotions. Done
      2026-08-23.** Per `AGENTS.md` §6 an agent may propose but never commit these; the maintainer
      reviewed all three and approved: Find My Buds Left/Right → 🟢 FACT (`PROTOCOL.md` §4.4,
      `DECISIONS.md` ADR-011); the wire-baseline firmware version `"release_5.203"` → 🟢 FACT
      (`PROTOCOL.md` §0.1, ADR-012); the general-purpose DLCI 0x02 settings-write envelope
      **shape** → 🟢 FACT, but explicitly **not** its 9+ individual field mappings, which stay 🟡
      HYPOTHESIS per the maintainer's own narrower decision (`PROTOCOL.md` §4.5, ADR-013).

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

- **Capture extraction path matters, added 2026-08-28.** Four captures (`CAP-012`, `CAP-013`,
  `CAP-017`, `CAP-031`) lost significant byte-level payload content to severe ACL truncation from
  the `btsnooz.py`-from-bugreport fallback path (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3 step 4); the
  one session extracted via the raw `btsnoop_hci.log` path instead (`CAP-032`) came out fully
  untruncated. Always check §3 step 3 (the raw file) first and prefer it whenever present — see
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3's own PROPOSAL note for the full detail.

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

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/TODO.md - https://tedsluis.github.io/opencontrolpixelbudspro2/TODO
