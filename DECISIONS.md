# DECISIONS.md

Architecture and design decisions, in ADR (Architecture Decision Record) style.

Every significant choice — especially if it deviates from an earlier assumption
or an AI suggestion — is recorded here **before** it is implemented broadly. An
earlier decision is never silently overwritten: a new, conflicting decision
explicitly references the number it replaces ("supersedes ADR-00X"), and the
superseded ADR's status is updated accordingly rather than deleted.

**Numbering:** ADRs are numbered sequentially and a number is never reused,
even if an ADR is later rejected or superseded. Before adding a new ADR, check
the highest existing number below and use the next one — do not guess or
pre-assign a number in another document (e.g. `TODO.md`), since a task
written today can be overtaken by another ADR being added first.

## Template

```
## ADR-XXX — <title>
- **Date**:
- **Status**: Proposed / Accepted / Rejected / Superseded by ADR-YYY
- **Context**: what problem or question was at play
- **Options considered**:
- **Decision**:
- **Consequences**: what becomes easier/harder as a result
```

---

## ADR-001 — Architecture style: Clean Architecture + MVVM + Repository pattern

- **Date**: _(project start)_
- **Status**: Accepted
- **Context**: A clear separation is needed between (fast-evolving) protocol
  knowledge and the rest of the app, because protocol knowledge keeps changing
  throughout the project as reverse engineering progresses.
- **Options considered**: MVC, MVVM + Clean Architecture, MVI
- **Decision**: Clean Architecture with MVVM in the UI layer and a Repository
  pattern between the domain and data layers, split across five Gradle
  modules (`:app`, `:ui`, `:domain`, `:data`, `:hardware`, with `:app` as the
  composition/DI-wiring module) with enforced one-way dependency
  direction. See `ARCHITECTURE.md` §2.
- **Consequences**: somewhat more boilerplate (module boundaries, sealed result
  types), but protocol changes stay isolated in the data/hardware layers and
  the UI/domain layers remain independently unit-testable without real
  Bluetooth hardware.

## ADR-002 — License: GNU AGPL-3.0

- **Date**: 2026-08-07
- **Status**: Accepted
- **Context**: The project is open source and reconstructs a protocol through
  reverse engineering; the license needs to protect that reverse-engineered
  knowledge and any modified version of the app — including one deployed as a
  network-accessible service — from being turned into a closed-source fork,
  in line with the project's Zero-GMS / privacy-first goals (see `PROJECT.md`,
  `AGENTS.md` §1).
- **Options considered**:
  - **MIT** — maximally permissive, allows closed-source forks; offers no
    protection against a proprietary derivative being redistributed without
    sharing improvements back.
  - **GPL-3.0** — strong copyleft for distributed binaries, but does not cover
    the case of a modified version run only as a network service without
    distributing the binary (the "SaaS loophole").
  - **AGPL-3.0** — same copyleft guarantees as GPL-3.0, and additionally
    requires that anyone running a modified version on a network server make
    the modified source available to that server's users.
- **Decision**: GNU Affero General Public License, version 3 (AGPL-3.0). See
  `LICENSE`.
- **Consequences**: any distributed or network-deployed modified version of
  this app must have its source made available, which keeps future
  improvements to the protocol reconstruction and app in the open. This may
  discourage some proprietary reuse or commercial integrations that would
  otherwise consider a permissive license — considered acceptable given the
  project's privacy/openness goals. Contributors should be aware of the AGPL's
  network-use clause when integrating third-party code.
- **Update (2026-08-15):** reaffirmed after `AGENTS.md` §12 and `README.md`
  dropped the "clean-room" framing in favor of "independent implementation
  based on reverse-engineering" (the earlier phrase was legally imprecise,
  since a true clean-room process requires a second team that never examined
  the original implementation, whereas this project's own reverse engineering
  includes JADX/apktool decompilation of the official APK). That relabeling
  does not change this decision: AGPL-3.0 vs. GPL-3.0 is a question about
  redistribution terms for *this project's own code*, not about how that code
  was derived, and the SaaS-loophole rationale above is unaffected either way.
  GPL-3.0 was re-examined and rejected again for the same reason as
  originally: it does not require sharing modifications made to a version
  deployed only as a network service. AGPL-3.0 stands.

## ADR-003 — Reverse engineering method: capture + APK analysis, no binary reverse engineering of protocol internals by the AI

- **Date**: _(project start)_
- **Status**: Accepted
- **Context**: `.proto` schemas and opcodes referenced by the app are extracted
  from `libmaestro`/`libgfps` binaries via external tooling (e.g. `pbtk`).
  There's a question of whether an AI coding assistant should attempt to
  reverse engineer these binaries directly during a session.
- **Options considered**:
  - Let the AI attempt to reverse engineer/guess undocumented opcodes directly
    from binaries or APK bytecode during implementation.
  - Require `.proto` schemas and opcodes to be extracted up front by the
    maintainer (via `pbtk`/JADX/apktool) and treated as given inputs; the AI
    only consumes and applies already-extracted, evidenced protocol knowledge.
- **Decision**: the second option. See `AGENTS.md` §4 and §6, and the evidence
  rules in `PROJECT_RULES.md` §1.
- **Consequences**: slower iteration when new protocol knowledge is needed
  (requires a maintainer-driven extraction step first), but avoids an AI
  silently inventing plausible-looking but unverified opcodes or APIs, which
  would violate the project's evidence-based reverse-engineering principle.

## ADR-004 — No dependency on Google Play Services or any network permission

- **Date**: _(project start)_
- **Status**: Accepted
- **Context**: The project's core motivation is an app that works fully
  offline and independent of Google Play Services, including on GrapheneOS
  where GMS may be absent or sandboxed.
- **Options considered**:
  - Support an optional GMS-based path (e.g. for update checks or Fast Pair UI
    integration) alongside a GMS-free path.
  - Ban GMS and the `INTERNET` permission entirely, with no exceptions.
- **Decision**: full ban — no `com.google.android.gms.*`, no `INTERNET`
  permission, under any circumstance. See `AGENTS.md` §1.
- **Consequences**: no in-app update checks, crash reporting, or cloud EQ
  presets; updates are distributed manually (e.g. via GitHub Releases). This
  is treated as an acceptable, intentional trade-off rather than a limitation
  to work around.

## ADR-005 — Device discovery via CompanionDeviceManager, no continuous BLE scanning

- **Date**: _(project start)_
- **Status**: Accepted
- **Context**: Continuous background BLE scanning is fingerprintable and
  conflicts with GrapheneOS's threat model, which the app targets as its
  primary reference OS.
- **Options considered**:
  - Custom continuous/periodic BLE scanning for device discovery.
  - `BluetoothAdapter.getBondedDevices()` for already-paired devices plus
    `CompanionDeviceManager` (API 26+) for first-time pairing.
- **Decision**: the second option. See `AGENTS.md` §7 and `ARCHITECTURE.md` §9.
- **Consequences**: pairing UX is delegated to the OS picker rather than a
  custom in-app scan screen, but the app never needs
  `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION` or `BLUETOOTH_PRIVILEGED`,
  and only gains access to the specific device the user selects.

## ADR-006 — Bounded exception to the no-BLE-scanning rule, for the Fast Pair Battery Notification only

- **Date**: 2026-08-08
- **Status**: Accepted
- **Context**: `AGENTS.md` §7 (per ADR-005) bans continuous background BLE
  scanning for device discovery, in line with GrapheneOS's threat model.
  Separately, `PROTOCOL.md` §4.3 Option A identifies the officially documented
  Fast Pair "Battery Notification" BLE advertisement as the lowest-cost
  battery reporting mechanism (no active RFCOMM connection required). Read
  literally, the discovery-scanning ban risked being interpreted as also
  blocking this unrelated, already-bonded-device use case — since agents are
  instructed to strictly follow `AGENTS.md`, a rule with no carve-out could
  cause an agent to refuse to implement `PROTOCOL.md` §4.3 Option A entirely,
  forcing battery status onto the connection-requiring RFCOMM path (Option B)
  as the only available mechanism. This tension was flagged in
  `ARCHITECTURE.md` §9.1 as an open question.
- **Options considered**:
  - Leave the discovery-scanning ban as an absolute, unqualified rule and
    never use BLE scanning for battery reporting, relying only on the
    RFCOMM-connected path (Option B).
  - Treat the Battery Notification as fully exempt from the scanning rule
    with no additional constraints, on the reasoning that it isn't
    "discovery."
  - Define a narrow, explicitly bounded exception: permitted, but only when
    filtered to the bonded device, foreground-triggered, time-boxed, and
    stopped on backgrounding.
- **Decision**: the third option. The exact rule agents must follow is
  recorded in `AGENTS.md` §7 (authoritative wording), summarized for
  architectural context in `ARCHITECTURE.md` §9.1. In short: scanning for the
  Battery Notification is permitted only when (a) filtered to the
  already-bonded device's own identifiers, (b) triggered by a user-visible
  event rather than a background timer, (c) time-boxed to roughly the
  advertisement's own visibility window (~8–20s), and (d) stopped immediately
  if the app leaves the foreground.
- **Consequences**: the app can use the lowest-cost, connection-free battery
  path as originally intended in `PROTOCOL.md` §4.3, without an AI agent
  correctly-but-unhelpfully refusing to implement it as a false positive
  against the discovery-scanning ban. The exception is deliberately narrow —
  any future feature needing broader or continuous scanning (e.g. general
  device discovery) remains fully covered by the original ban in `AGENTS.md`
  §7 and would need its own, separate decision; it is not opened up by this
  ADR.

## ADR-007 — `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Groups are capture scenarios, not tests; `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` is the test/behavior catalog

- **Date**: 2026-08-08
- **Status**: Accepted
- **Context**: `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Groups A–Q and
  `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s four action tables had grown to
  describe largely the same ~66–70 Buds actions/behaviors, but with different
  groupings, wording, and no ID linkage between them, and no structured place
  to record per-action results (only a session-level Capture Index existed).
  This risked the same finding being independently re-described in two
  places, and gave "a Group" no clear relationship to "an official test."
- **Options considered** (evaluated against three criteria: supporting live
  execution, complete/clear recording, and ease of later analysis):
  1. Two files, `CAPTURE` = procedure + testing, `TESTPLAN` = results only —
     rejected: leaves the original duplication largely intact, since Groups
     would still function as a de facto test catalog.
  2. Three files/layers — a stable action catalog, a pure procedure document,
     and a separate results/evidence log — cleanest separation, but adds a
     third artifact and ID namespace before the project has completed even
     one real capture; assessed as premature for the project's current
     stage.
  3. Two files, redefined roles: `CAPTURE`'s Groups become explicit **capture
     scenarios** (how to run an efficient session), `TESTPLAN` becomes a
     stable **action/behavior catalog** with permanent Test-IDs, existence
     confidence, linked Group(s), and a thin evidence pointer into
     `PROTOCOL_NOTES.md`/`PROTOCOL.md` (never a duplicate results table).
- **Decision**: option 3. See `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §0 for the
  full reasoning and the Test-ID convention, and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
  §4's intro for the capture-scenario framing. Every numbered action in
  `CAPTURE` is annotated with its Test-ID; the Capture Index (§9) gained a
  Test(s) column, closing the chain: Test-ID → Group → `CAP-NNN` capture →
  frame → `PROTOCOL_NOTES.md` finding.
- **Consequences**: a Group can now legitimately bundle unrelated Test-IDs
  for capture efficiency (e.g. Group C bundles `CONV-001` and `MULTI-001`)
  without that being a modeling problem. Mapping the two documents onto each
  other surfaced two genuine, previously-untracked gaps (no capture scenario
  yet for `INEAR-004` and `GATT-001`), now recorded in `TESTPLAN`'s open-items
  section rather than silently missing. Trade-off: two ID namespaces
  (`CAP-NNN` sessions, `<AREA>-NNN` tests) instead of one, requiring the same
  numbering discipline as `DECISIONS.md` ADRs (never reused, checked against
  existing entries). If the project later needs option 2's three-layer
  separation (e.g. once results volume grows), this ADR should be superseded
  rather than silently reinterpreted.