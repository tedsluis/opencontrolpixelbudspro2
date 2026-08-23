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
written today can be overtaken by another ADR being added first. Register
every new ADR in `id_registry.csv` (repo root) alongside adding it here —
`scripts/lint_docs.py` checks every `ADR-NNN` reference against that registry
and flags anything unregistered, catching a reused/mistyped number
mechanically instead of relying on a human noticing (see `CHANGELOG.md`'s
`fix:` entry on the `CAP-005`/`CAP-007`/`CAP-010` ID-reuse incident that
motivated this).

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
     `PROTOCOL.md` (never a duplicate results table).
- **Decision**: option 3. See `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` §0 for the
  full reasoning and the Test-ID convention, and `CAPTURE_BLUETOOTH_HCI_SNOOP.md`
  §4's intro for the capture-scenario framing. Every numbered action in
  `CAPTURE` is annotated with its Test-ID; the Capture Index (§9) gained a
  Test(s) column, closing the chain: Test-ID → Group → `CAP-NNN` capture →
  frame → `PROTOCOL.md` finding.
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

## ADR-008 — Fast Pair Account Linking, Ownership Transfer, and the Accessory Non-Owner Service are out of scope

- **Date**: 2026-08-15
- **Status**: Accepted
- **Context**: nRF Connect's cached GATT service list (`CAP-004-FINDINGS.md`
  §6) surfaced a named "Accessory Non-Owner Service" candidate alongside the
  Google Fast Pair Service, and the official Fast Pair spec separately
  defines Account Key-based **Account Linking** (associating a device with a
  Google account) and **Ownership Transfer** (re-linking a device to a new
  owner's account) as part of the broader Fast Pair ecosystem. None of these
  have been targeted by any capture or test plan so far, but nothing had
  explicitly ruled them out either — leaving room for a future session to
  drift into investigating them without a scope check.
- **Options considered**:
  - **In scope, investigate opportunistically** — rejected: these mechanisms
    exist specifically to manage a device's relationship with a *Google
    account*, which is exactly the GMS/cloud dependency this project exists
    to route around (`PROJECT.md` non-goals, `AGENTS.md` §1's Zero-GMS rule).
    Reverse-engineering them would not serve the app's actual feature set
    (ANC, EQ, touch controls, battery, case sounds — `PROJECT.md`'s v1 scope
    list) and risks scope creep into account-security-adjacent territory this
    project has no reason to touch.
  - **Out of scope, explicit** — adds one line of friction (checking this ADR
    before starting related work) in exchange for closing off a
    plausible-looking but unproductive research direction before any time is
    sunk into it.
- **Decision**: Fast Pair **Account Linking**, **Ownership Transfer**, and the
  **Accessory Non-Owner Service** (and any other Fast Pair mechanism whose
  purpose is managing the device's relationship to a Google account rather
  than device control) are explicitly **out of scope** for this project. This
  is a scope boundary, not a protocol finding — no capture time should be
  spent decoding these mechanisms' wire behavior. If a future capture
  incidentally surfaces traffic belonging to one of these mechanisms (as
  `CAP-004` already has, via the GATT service list), it should be
  labeled/skipped as out-of-scope rather than investigated further, and this
  ADR updated only if the maintainer explicitly decides to bring one of these
  in scope later.
- **Consequences**: `PROJECT.md`'s non-goals should reference this ADR (kept
  in sync there); any AI agent encountering Account-Linking/Ownership-Transfer/
  Non-Owner-Service traffic declines to pursue it and points to this entry
  instead of silently expanding scope (`AGENTS.md` §15's "never silently
  expand scope" rule).

## ADR-009 — ANC command channel confirmed as Fast Pair Message Stream (DLCI 0x04); `FrameEncoder` implementation blocked pending `CAP-006`

- **Date**: 2026-08-15
- **Status**: Accepted
- **Context**: `ARCHITECTURE.md` §5's implementation gate requires that a
  DLCI's framing/command identification reach 🟢 FACT in `PROTOCOL.md` **and**
  be recorded as a `DECISIONS.md` ADR before that channel's
  `FrameEncoder`/`FrameDecoder` may be implemented. The ANC Set/Get/Notify
  command was promoted to 🟢 FACT in `PROTOCOL.md` §4.1 on 2026-08-12
  (`CAP-001-FINDINGS.md` §5's "Full resolution") but never got the
  corresponding ADR — this entry closes that gap, per `AGENTS.md` §6's
  requirement that the same FACT determination trigger both the code-gate and
  the ADR together, not one without the other.
- **Finding being recorded**: ANC mode is controlled via Google's official
  Fast Pair **Hearable Controls** extension (`[OFFICIAL-SPEC]`), Message Group
  `0x08`, over the official Fast Pair Message Stream on **DLCI 0x04** — not
  `libmaestro`'s Pigweed-HDLC channel (DLCI 0x02) and not the private DLCI-0x08
  envelope, both of which were live candidates before this resolution. Codes:
  `0x11` Get, `0x12` Set (Seeker→Provider, MAC+ACK), `0x13` Notify
  (Provider→Seeker); one-hot mode bitmask `0x80`=Transparent, `0x40`=Adaptive,
  `0x20`=Off, `0x08`=ANC. Evidence: official spec byte-match plus an internal
  content+timing cross-check within `CAP-001` (4 of 4 decoded `Set` frames
  matched their nearest observed UI tap, in sequence, within ~1.5s) — see
  `PROTOCOL.md` §4.1 for the full write-up.
- **What this ADR does NOT clear, and why `FrameEncoder` stays blocked:** the
  FACT status above covers what a `0x12` frame *means* when one appears — it
  does not establish that every user-initiated ANC change reliably produces
  one. `CAP-001` is the *only* capture with this evidence, and in that single
  capture, **2 of the 6 physical ANC taps produced no matching `0x12` frame at
  all** (`CAP-001-FINDINGS.md` §5's "Not resolved" note and 2026-08-15 risk
  flag). The leading explanation — first-tap UI-state realization while the
  ANC row was still greyed out — is plausible but unconfirmed; the
  alternative (real taps can silently fail to produce a command under some
  condition) would be a functional defect risk in the app being built, not
  just a documentation gap, if implemented on this evidence alone.
- **Decision**: the ANC channel/opcode/framing determination is accepted as 🟢
  FACT for documentation purposes (`PROTOCOL.md` §4.1 stands). **The Kotlin
  `FrameEncoder`/`FrameDecoder` implementation for this specific command is
  explicitly BLOCKED** until `CAP-006` (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s
  Capture Index — a clean, single-tap-per-window repeat of Group B) confirms
  that isolated, individually-triggered ANC taps reliably produce a `0x12`
  frame every time, closing the 2/6 gap. This is a narrower, command-specific
  block layered on top of `ARCHITECTURE.md` §5's general per-DLCI gate — DLCI
  0x04's framing being FACT does not by itself clear every command that rides
  on it for implementation; each command's own reliability evidence matters
  too.
- **Consequences**: implementation of the ANC control feature in `:data`
  waits on `CAP-006`, which should be prioritized accordingly in `TODO.md`.
  If `CAP-006` confirms 100% reliability, this ADR should be updated (not
  superseded — the underlying framing finding doesn't change) to record the
  block as lifted, with a pointer to that capture's evidence. If `CAP-006`
  reproduces misses, that is a new, higher-priority open question for
  `PROTOCOL.md` §6, not a reason to proceed with implementation regardless.
- **Update (2026-08-15): `CAP-006` confirms 100% reliability — the block is
  lifted.** `CAP-006` (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Capture Index) ran
  the exact repeat this ADR called for: Bluetooth enabled and the connection
  allowed to settle, the ANC row confirmed fully active (not greyed out)
  before any tap, then each of the four ANC modes tapped exactly once in
  isolation. Result (`CAP-006-FINDINGS.md` §3): filtering the **entire**
  233s log for Group `0x08` Code `0x12` returns exactly four frames — one per
  tap, in tap order, zero extras, zero misses — each within ~1.3s of its
  video-observed tap (frames 1393/1627/1731/1862, modes
  `0x08`/`0x20`/`0x40`/`0x80` matching Noise Cancellation/Off/Adaptive/
  Transparency respectively). This is a clean 4/4, contrasting with `CAP-001`'s
  4/6 under bundled, unpaused conditions — the leading explanation from this
  ADR's "What this ADR does NOT clear" section (first-tap UI-state
  realization while the row was still greyed out, not a genuine command) is
  now the explanation best supported by the evidence, not merely plausible.
  **The `FrameEncoder`/`FrameDecoder` implementation block for the ANC
  command is lifted.** The underlying framing/opcode finding (`PROTOCOL.md`
  §4.1) is unchanged by this update, per this ADR's own note above that a
  confirming result would not require superseding it. This update does not
  extend to any other channel or command — per `AGENTS.md` §6, the
  implementation gate remains per channel/feature.

## ADR-010 — `PROJECT_RULES.md` rule 19 does not apply to the maintainer's own captures under `captures/`

- **Date**: 2026-08-23
- **Status**: Accepted
- **Note on process**: this ADR was drafted by an AI agent, but per `AGENTS.md` §6's requirement
  for explicit human/maintainer sign-off before an agent commits a new `DECISIONS.md` ADR as
  settled: the maintainer directly reviewed `AUDIT_REPORT_2026-08-22.md`'s finding below and
  explicitly instructed that its recommendations, including this ADR, be carried out (session of
  2026-08-23). That instruction is the explicit approval this rule requires — recorded here so
  the provenance is auditable, not assumed.
- **Context**: `AUDIT_REPORT_2026-08-22.md` found a direct textual conflict between two binding
  project documents. `PROJECT_RULES.md` rule 19 states: *"Sensitive or personal data (e.g. MAC
  addresses of your own devices, account details) is anonymized or excluded via `.gitignore`
  before committing."* `CONTRIBUTING.md`'s "Protocol/capture contributions" section separately
  and explicitly states the opposite for the maintainer's own captures: *"The maintainer's own
  existing and future Bluetooth captures... intentionally retain real data — MAC addresses,
  timestamps, device identifiers... That is a decision only the maintainer can make about their
  own data, and it is not revisited by this document."* This is a real, intentional, long-standing
  practice (every `captures/CAP-NNN-*/` session committed to date retains real identifiers), but
  the deviation from rule 19's literal text had never been recorded as a `DECISIONS.md` ADR, as
  `PROJECT_RULES.md`'s own preamble requires for any knowing deviation from its rules. A reader
  encountering rule 19 in isolation would reasonably (and incorrectly) conclude the repo's own
  capture data is non-compliant with its own rules.
- **Options considered**:
  - Anonymize all existing and future captures to satisfy rule 19 literally — rejected: this data
    is the evidentiary backbone of the entire reverse-engineering effort; the maintainer has
    already made an informed decision (`CONTRIBUTING.md`) to publish their own captures
    unredacted, and redoing that retroactively would provide no privacy benefit to a third party
    (it is the maintainer's own hardware/accounts) while destroying reproducibility for anyone
    trying to correlate a `CAP-NNN` finding back to its exact source bytes.
  - Leave the conflict as-is — rejected: `PROJECT_RULES.md`'s own conflict-resolution clause
    specifically anticipates and requires recording exactly this kind of deviation; leaving it
    unrecorded is itself the gap being fixed.
  - Record the existing, already-practiced exception as a formal ADR, scoped narrowly to
    `captures/CAP-NNN-*` and to the maintainer's own data specifically — chosen.
- **Decision**: `PROJECT_RULES.md` rule 19's anonymize-or-exclude requirement does **not** apply
  to the maintainer's own Bluetooth captures under `captures/CAP-NNN-*/` (raw logs, event notes,
  findings, recordings, and any other artifact type present there) — this is a deliberate,
  informed, maintainer-only exception, not a general relaxation of rule 19. Rule 19 continues to
  apply in full to everything else (e.g. account details, credentials, any data outside
  `captures/`) and, per `CONTRIBUTING.md`'s existing PII-exception section, continues to apply in
  full to any **third-party** contributor's capture data, which must still be redacted before
  submission. `CONTRIBUTING.md`'s existing explanation of *why* the maintainer's own data is
  exempt is unchanged and remains the canonical rationale; this ADR is the formal record of the
  deviation that `PROJECT_RULES.md` itself requires.
- **Consequences**: closes the textual conflict between `PROJECT_RULES.md` and `CONTRIBUTING.md`
  without changing actual practice (which was already consistent with `CONTRIBUTING.md`, not
  rule 19's literal text). Future agents/contributors reading rule 19 should cross-reference this
  ADR and `CONTRIBUTING.md` rather than concluding the repo's own captures are non-compliant.
  Does not affect the separate, unrelated logging rules for the *app's own runtime code*
  (`AGENTS.md` §7/§9 — never log the paired device's MAC address at `INFO` level or above), which
  govern the shipped app's behavior, not this repo's committed research data.