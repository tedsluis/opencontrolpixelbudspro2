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

## ADR-011 — Find My Buds Left/Right confirmed as Fast Pair Message Stream Action (DLCI 0x04, Group `0x04`, Code `0x01`); `FrameEncoder` implementation unblocked

- **Date**: 2026-08-23
- **Status**: Accepted
- **Context**: `PROTOCOL.md` §4.4 carried a 🟡 HYPOTHESIS (strong) finding from `CAP-025`
  (2026-08-21): Ring commands for the Left/Right earbuds ride the same Fast Pair Message Stream
  channel (DLCI 0x04) already established as 🟢 FACT for ANC (`ADR-009`), using Group `0x04`
  (Action), Code `0x01` (Ring). The maintainer reviewed this finding directly (session of
  2026-08-23) and gave explicit sign-off to promote it, per `AGENTS.md` §6's requirement that an
  agent may propose but never unilaterally commit a FACT promotion.
- **Finding being recorded**: `Group=0x04`/`Code=0x01` on DLCI 0x04, `Value` byte `0x01` = start
  ringing Right, `0x02` = start ringing Left, `0x00` = stop/mute (shared, not per-earbud). Evidence:
  4 action/response pairs (2 starts, 2 stops) in `CAP-025`, each individually video-correlated to a
  specific tap under Group K's one-action-per-window discipline, riding the same envelope
  mechanism already confirmed for ANC — not merely a surface resemblance to the spec's own worked
  example. See `PROTOCOL.md` §4.4 for the full write-up.
- **What this ADR does NOT clear**: Case and "both simultaneously" are a **separate, unresolved
  mechanism** (`PROTOCOL.md` §4.4's "Major structural finding") — video-confirmed to route through
  a different, likely GMS/Find-Hub-mediated path with **zero** local `Group 0x04 Code 0x01` traffic
  across a ~2.5-minute observation window. This ADR covers Left/Right only; Case/"both" stays
  🔴 OPEN QUESTION, flagged separately as a possible Zero-GMS scope limit.
  Also unresolved: the exact content of the second ACK variant's extra byte(s) — an audit pass on
  2026-08-23 found the previously-cited "spec worked example" for the ACK itself was miscited
  (`PROTOCOL.md` §2.1's correction); this affects the ACK-byte interpretation only, not the
  Group/Code/Value command mapping this ADR records.
- **Decision**: the Ring command's channel/opcode/value-mapping determination is accepted as 🟢
  FACT for Left/Right specifically. `FrameEncoder`/`FrameDecoder` implementation for this command
  is unblocked, per `ARCHITECTURE.md` §5's per-command implementation gate — no further capture is
  required before implementation begins, unlike ANC's `ADR-009` (which needed `CAP-006`'s isolated
  repeat to close a reliability gap; `CAP-025` already used the same isolated, single-tap-per-window
  methodology from the start).
- **Consequences**: Left/Right Find My Buds can be implemented in `:data` immediately. Case/"both"
  stays out of scope for implementation until the separate Find Hub question is resolved (see
  `PROTOCOL.md` §6, Behavior).

## ADR-012 — Wire-baseline firmware version confirmed as `"release_5.203"` (DLCI 0x08, Group `0x03`, Code `0x02`)

- **Date**: 2026-08-23
- **Status**: Accepted
- **Context**: `PROTOCOL.md` §0.1 had tracked, since 2026-08-14, an open question distinguishing
  the UI-baseline firmware version (`"release_5.203"`, confirmed via official app screenshot) from
  whichever value(s) the same string might correspond to on the wire, given four different
  version-like strings were independently documented across multiple channels
  (`"release_5.203"`, `"Revision 6"`, `"cape2_sm"`, `"500m"`–`"500p"`). `CAP-023` (2026-08-21)
  captured, for the first time, a session that recorded both the app's own firmware-display screen
  *and* the wire traffic. The maintainer reviewed this finding directly (session of 2026-08-23) and
  gave explicit sign-off to promote it.
- **Finding being recorded**: in `CAP-023`, the on-screen "Device firmware version" (Left/Right/Case,
  all `release_5.203`, video-confirmed at 08:24:17) is byte-for-byte identical to the string
  independently present on DLCI 0x08's private envelope (Group `0x03` Code `0x02`) in the *same
  session's* connection-time handshake (frame 849, 08:23:46.038) — critically, **before** the
  firmware screen was even opened, ruling out the screen-open action itself as the source of the
  wire value. This is the first same-session match between an on-screen value and a wire value this
  project has recorded for this question.
- **What this ADR does NOT clear**: what `"Revision 6"` (DLCI 0x04's official Fast Pair Device
  Information field, Code `0x09`) represents, if not the user-facing firmware version, stays
  🔴 OPEN QUESTION — this ADR resolves which string the app calls "the firmware version," not what
  every other version-like string on the wire means. `"cape2_sm"`/`"500m"`–`"500p"` likewise remain
  unresolved, unchanged by this ADR.
- **Decision**: `"release_5.203"`, as carried on DLCI 0x08's private envelope (Group `0x03` Code
  `0x02`), is accepted as 🟢 FACT to be what the official app displays as the Buds' firmware
  version.
- **Consequences**: any future Startup Handshake / firmware-compatibility check
  (`ARCHITECTURE.md` §8.1) implemented against DLCI 0x08's Group `0x03` Code `0x02` value can treat
  it as the authoritative firmware-version string, not merely a plausible candidate. Does not by
  itself unblock any `FrameEncoder`/`FrameDecoder` work — this is a data-field identification, not a
  command channel.

## ADR-013 — DLCI 0x02 general-purpose settings-write envelope shape confirmed (`field5{field4{...}}}` outer wrapper); generic write-path implementation unblocked, individual field semantics remain HYPOTHESIS

- **Date**: 2026-08-23
- **Status**: Accepted
- **Context**: `PROTOCOL.md` §4.5's shared preamble documented a 🟡 HYPOTHESIS (strong) finding
  from the 2026-08-21 capture batch (`CAP-019`–`CAP-024`): every one of 9+ distinct settings
  (Conversation Detection, Multipoint, Touch controls, Head gestures, press-and-hold ×4, ANC-mode
  rotation, Mono audio, Volume EQ, Volume balance, In-ear detection, 2 Case-sound toggles) writes
  through DLCI 0x02 inside an identical two-level outer wrapper, `field 5 { field 4 { ... } }`,
  across 6 independent capture sessions with zero counter-examples. The maintainer reviewed this
  finding directly (session of 2026-08-23) and gave explicit sign-off to promote *the envelope
  pattern itself* — explicitly declining to blanket-promote every individual field mapping at the
  same time, since those vary widely in evidence strength (see below).
- **Finding being recorded**: the outer `field5{field4{...}}}` wrapper (standard protobuf
  wire-format tags), preceded by a constant, cross-session-stable 13-byte prefix, is a genuine,
  general-purpose `libmaestro` settings-apply envelope — not a coincidental per-setting shape. This
  cross-capture, no-counter-example replication (9+ settings, 6 sessions, multiple days) is
  comparable in kind to how DLCI 0x02's own HDLC framing mechanism was promoted to FACT in
  `PROTOCOL.md` §2.2a.
- **What this ADR explicitly does NOT clear — narrower than it may look:** only the outer
  wrapper's existence and shape is FACT. Each subsection's *specific* field-number-to-setting
  mapping in `PROTOCOL.md` §4.5.1–§4.5.8 remains individually 🟡 HYPOTHESIS, unchanged by this ADR,
  reflecting genuinely different evidence strength per setting:
  - Better-evidenced (2+ independent samples within their capture): In-ear detection (both
    directions), Volume EQ (both directions), press-and-hold (4/4 Left/Right × ANC/Assistant
    combinations).
  - Single-sample, one direction only: Conversation Detection, Multipoint, the Touch-controls and
    Head-gestures top-level toggles, and one of the two Case-sound toggles ("Bud return," whose one
    sample isn't even cleanly disambiguated from a screen-open state-sync).
  - Volume Balance: field identity plausible, but scale/direction is explicitly still 🔴 open —
    unaffected by this ADR.
  No individual field mapping is promoted by this ADR. A future ADR (or a batch of them) would be
  needed before promoting any specific field's meaning, following the same per-item sign-off
  process used here.
- **Decision**: the envelope shape/pattern is accepted as 🟢 FACT. Per `ARCHITECTURE.md` §5's
  per-command implementation gate, this unblocks implementing the **generic** write path — the
  `FrameEncoder` logic that builds the two-level wrapper and the constant prefix — but does **not**
  unblock implementing what any specific field number *means*; a `FrameEncoder` call site that
  writes a real setting still requires its own field's HYPOTHESIS to be independently strengthened
  and separately promoted first.
- **Consequences**: `:data`'s `CodecRouter` can implement and unit-test the shared envelope
  encode/decode logic now, against fixed byte-array fixtures, ahead of any specific setting being
  wired up — but no UI control for an individual setting (Conversation Detection, Multipoint, etc.)
  should ship against this ADR alone.

## ADR-014 — DLCI 0x08 `Group 0x0e Code 0x01` confirmed as a per-earbud+case battery push (index=1/2/3 → Left/Right/Case)

- **Date**: 2026-08-23
- **Status**: Accepted
- **Context**: while re-analyzing `CAP-011` for an unrelated, maintainer-requested task (locating
  the exact video timestamp of a 1%-battery UI change), a message on DLCI 0x08 — the private
  envelope whose overall identity remains 🔴 OPEN QUESTION (§2.3) — was found to decode to 3
  repeated `[value, flag, index]` entries. Within `CAP-011` alone, entries index=1/2 tracked the
  on-screen Left/Right percentages across 4 occurrences in one session, including a video-confirmed
  live change. To check whether this held beyond one session, the same decode was run against
  `Group 0x0e Code 0x01` frames in `CAP-001` and `CAP-002` (both 2026-08-09, 12 days before
  `CAP-011`), picked near each session's own independently-recorded on-screen battery notification.
  The maintainer reviewed this cross-capture result directly (session of 2026-08-23) and gave
  explicit sign-off to promote it, per `AGENTS.md` §6.
- **Finding being recorded**: `Group 0x0e Code 0x01`'s three repeated entries correspond to Left
  (index=1), Right (index=2), and Case (index=3) battery percentages. Evidence: a clean 3-for-3
  match against on-screen values in both `CAP-001` (frame 1114: `[100,100,62]` vs. on-screen "Left
  100% Case 62% Right 100%") and `CAP-002` (frame 49024: `[100,100,57]` vs. on-screen "Left 100%
  Case 57% Right 100%"), plus `CAP-011`'s own 4-occurrence, video-correlated Left/Right tracking
  (including a live 93→92/88→87 transition matched ~0.86s before the UI itself updated) and an
  independent cross-check via a second message (`Group 0x04 Code 0x03`) at the same 4 moments. This
  is a **semantic decode of an already-structurally-known message**, not a newly-found packet type
  — `CAP-002-FINDINGS.md` §2a documented the same shape back on 2026-08-12 without interpreting it.
  See `PROTOCOL.md` §4.3 Option E and `CAP-011-FINDINGS.md` §7 for the full write-up.
- **What this ADR does NOT clear:**
  - **`CAP-011`'s own Case (index=3) reading is stale**, not live — it reads 92 throughout that
    session against an on-screen Case value that stayed at 89%, unlike `CAP-001`/`CAP-002` where
    index=3 matched live. The index→component mapping is accepted as FACT; this session-specific
    staleness is a separate, still-open behavioral question (plausibly tied to that session's own
    documented procedure deviation — the case sat open and empty throughout — not confirmed).
  - **The `flag` field (`field2`)'s meaning** — observed as `1` on every fresh reading and absent
    on `CAP-011`'s one stale reading, plausibly a "fresh/valid" bit, not confirmed as such.
  - **The burst's trigger** — recurs at irregular intervals in `CAP-011` (4:02, 2:56, 8:21 apart);
    checked against that session's own near-continuous BLE reconnect churn and found no
    correlation. Genuinely unresolved.
  - **DLCI 0x08's own identity/ownership** as a channel — unaffected by this ADR, still 🔴 OPEN
    QUESTION (§2.3); this ADR resolves one message's meaning on that channel, not what the channel
    itself is or belongs to.
- **Decision**: the index=1/2/3 → Left/Right/Case mapping for DLCI 0x08's `Group 0x0e Code 0x01`
  message is accepted as 🟢 FACT.
- **Consequences**: this becomes a fifth candidate battery-reporting mechanism (`PROTOCOL.md` §4.3
  Option E), usable as a secondary/cross-validation signal alongside the already-FACT HFP option
  (C) if implemented — but not yet placed in the implementation-priority ordering, since its
  trigger/cadence is still unconfirmed and one observed session showed a stale field. Does not
  itself unblock `FrameEncoder`/`FrameDecoder` work on DLCI 0x08 more broadly — that channel's
  other Groups (`0x01`/`0x02`/`0x05`/`0x09`) remain unidentified, unaffected by this ADR.

## ADR-015 — `BATT-006` resolved: `AT+CIND` `battchg` confirmed a stale single snapshot; `AT+BIEV` confirmed per-earbud (Right), not a fixed-aggregate/fixed-cadence indicator

- **Date**: 2026-08-2x
- **Status**: Accepted
- **Context**: `BATT-006` (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, added 2026-08-14) asked whether
  `AT+CIND?`'s `battchg` or `AT+BIEV=2`'s HF Indicator #2 (or neither) tracks a real battery-level
  change over time, following `CAP-001-FINDINGS.md` §3's single-snapshot disagreement between the
  two. `CAP-009` (2026-08-23) ran a dedicated, purpose-built 101-minute natural-discharge bracket
  for this question, then an independent repeat pass re-derived the same conclusions from a fresh
  video timeline and a full (not spot-checked) re-scan of the wire log. The maintainer reviewed
  `CAP-009-FINDINGS.md` §1–§5 directly and gave explicit sign-off to promote/record the findings
  below, per `AGENTS.md` §6.
- **Finding being recorded**:
  1. **`AT+CIND?`'s `battchg` is a single, non-repeating snapshot** — queried exactly once, at HFP
     Service Level Connection setup, and never refreshed again for the rest of the session,
     regardless of real battery-level changes on the peer. Evidence: 101 minutes, one query
     (frame 884), zero repeats, including after a full reconnect later in the same log; the peer's
     Right earbud genuinely changed by ~13 percentage points in that window with no `battchg`
     update at all.
  2. **`AT+BIEV=2` tracks a real, individual earbud's percentage — specifically Right in this
     session — not a fixed aggregate of Left/Right/Case.** All 5 of its distinct values across the
     session matched the Right earbud's on-screen percentage at every transition; none of Left's
     or Case's on-screen values ever appeared in the `AT+BIEV` sequence. This revises the project's
     earlier working assumption (`PROTOCOL.md` §4.3 Option C, pre-`CAP-009`) that both HFP
     indicators report one aggregate value.
  3. **`AT+BIEV`'s push cadence is not a fixed ~6–7s rate for the life of the connection.** The
     ~6–7s spacing `CAP-001` observed is a connection-settling burst — `CAP-009` shows gaps
     widening to a median of ~20s and as much as ~14.6 minutes once the session goes idle.
  See `PROTOCOL.md` §4.3 Option C and `CAP-009-FINDINGS.md` §1–§5 for the full write-up.
- **What this ADR does NOT clear:**
  - **Whether `AT+BIEV` always reports physical-Right, or whichever earbud is currently
    HFP-primary** — R happened to be primary in this one session; a session with confirmed-L
    primary is needed to distinguish these. Recorded as 🟡 HYPOTHESIS in `PROTOCOL.md`, not FACT.
  - **Whether `AT+CIND?`'s `battchg` is itself aggregate or per-earbud** — it was only ever
    observed once per session (here and in `CAP-001`), so this remains untested either way.
  - **What exactly triggers an `AT+BIEV` push once the connection has settled** — `CAP-009` cannot
    distinguish "push-on-change, with the change itself this infrequent" from "a poll that simply
    slows down while idle." Recorded as 🟡 HYPOTHESIS.
  - **Two further `CAP-009` findings are explicitly *not* covered by this ADR** — proposed
    separately, at HYPOTHESIS level, and not requiring FACT-level sign-off: DLCI `0x04`'s
    `Group 0x03 Code 0x03` as a candidate for `PROTOCOL.md` §4.3 Option B's still-open battery
    code, and a BLE Fast Pair scan as a candidate explanation for post-reconnect on-screen updates
    (`PROTOCOL.md` §4.3 Option A). Both remain 🟡 HYPOTHESIS pending further verification.
- **Decision**: `battchg`'s single-snapshot behavior, and `AT+BIEV`'s per-earbud (not aggregate)
  tracking of Right in `CAP-009`, are accepted as 🟢 FACT. `AT+BIEV`'s non-fixed push cadence is
  accepted as 🟢 FACT for the specific claim "not a sustained ~6–7s rate"; the precise trigger
  mechanism remains 🟡 HYPOTHESIS.
- **Consequences**: `BATT-006` is closed as a Test-ID (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`). Any
  future battery-UI implementation relying on HFP (`AGENTS.md` §5) must not treat `AT+CIND` as a
  live source, must not assume `AT+BIEV` represents a combined/aggregate value, and must not use a
  missed ~6–7s beat as a liveness signal — `AGENTS.md` §5 updated accordingly. Does not resolve
  DLCI `0x04`/BLE-scan HYPOTHESES noted above; those need their own follow-up before any further
  promotion.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/DECISIONS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/DECISIONS
