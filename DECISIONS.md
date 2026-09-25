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
- **Status**: Superseded by ADR-017 (see below)
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
- **Update (2026-08-30): superseded by ADR-017.** The maintainer explicitly requested, in
  conversation, that AI assistance be allowed to help with the *mechanical* parts of APK
  decompiling and proto-schema extraction (including running `pbtk` itself and, per a separate
  explicit decision, explaining native `.so` disassembly output) ahead of Phase 2 (APK reverse
  engineering) work. ADR-017 replaces this decision's blanket restriction with a narrower boundary:
  the AI may run searches, list candidates, and explain already-surfaced code/disassembly, but never
  decides relevance or promotes a finding to a recorded `REVERSE_ENGINEERING.md` HYPOTHESIS — see
  ADR-017 for the full boundary. This entry's original text is left standing per `PROJECT_RULES.md`
  §3 rule 9's non-destructive-update convention.

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
- **Update (2026-09-24, `ai-sessions/0045`, maintainer-approved in chat 2026-09-24):** bound (c)'s justification is
  corrected, not the bound itself. "The advertisement's own visibility window (~8–20s)" has **no** basis in the Fast Pair
  specification: the live `batterynotification` page (fetched 2026-09-08 and again 2026-09-24) contains no trigger, cadence
  or display-duration statement at all, and `PROTOCOL.md` §4.3 Option A already downgraded the 8 s/20 s figures to an
  unlocated 🟡 HYPOTHESIS. The ~8–20 s time-box stays as **this project's own choice** (short enough to be clearly bounded,
  long enough to catch an advertisement), not as a spec citation. `AGENTS.md` §7 is corrected the same way.

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

## ADR-009 — ANC command channel confirmed as Fast Pair Message Stream (DLCI 0x04); `FrameEncoder` implementation blocked pending `CAP-006` (block lifted 2026-08-15, see Update)

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

- **Date**: 2026-08-23 (was written "2026-08-2x"; resolved 2026-09-24, `ai-sessions/0045`, from commit `76c482e`, 2026-08-23, which introduced this ADR)
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
- **Update (2026-09-24, `ai-sessions/0045`):** the *implementation* consequences above (a battery UI relying on HFP) are superseded
  by ADR-040 — HFP battery is not consumable by an app on Android 14+ and the app route was removed. The FACTs above stand.

## ADR-016 — Retroactive sign-off: EQ field-to-band mapping/gain-clamp/preset quintets, and four `CAP-016` hardware-behavior FACTs

- **Date**: 2026-08-28
- **Status**: Accepted
- **Note on process**: this ADR was drafted by an AI agent, but per `AGENTS.md` §6's requirement
  for explicit human/maintainer sign-off before an agent commits a new `DECISIONS.md` ADR as
  settled: the maintainer directly reviewed the 2026-08-28 project-wide audit's `GOV-01` finding
  and explicitly approved consolidating sign-off for all findings below into one ADR (session of
  2026-08-28). That instruction is the explicit approval this rule requires — recorded here so the
  provenance is auditable, not assumed.
- **Context**: on 2026-08-18, `PROTOCOL.md` was updated directly from two independent capture
  sessions — `CAP-015` (EQ, completing/superseding `CAP-005`'s partial attempt) and `CAP-016`
  (Group U re-run) — promoting seven distinct claims to 🟢 FACT. Unlike every FACT promotion from
  2026-08-21 onward (`ADR-011`–`ADR-015`), these seven were never given a corresponding
  `DECISIONS.md` ADR or an explicit "maintainer sign-off obtained" citation; `PROTOCOL.md`'s
  changelog table still marked both 2026-08-18 entries "not yet reviewed by maintainer" as of the
  2026-08-28 project-wide audit's `GOV-01` finding. This ADR closes that gap.
- **Findings being recorded**:
  1. **EQ field-to-band mapping** (`PROTOCOL.md` §4.2): quintet field 1↔Low bass, 2↔Bass, 3↔Mid,
     4↔Treble, 5↔Upper treble (wire order is the reverse of the on-screen top-to-bottom order).
     Evidence: `CAP-015-FINDINGS.md` §5 — all 5 sliders dragged individually, 3 passes each; 4 of 5
     fields video-confirmed by finger-on-slider position, the 5th by elimination against a
     perfectly repeating field-change order across all 3 passes; matches `CAP-005`'s earlier
     single-band inference exactly, 5 days apart, independently.
  2. **Band-gain range, ±6.0 clamp** (`PROTOCOL.md` §4.2). Evidence: `CAP-015-FINDINGS.md` §4 — 8
     of 10 extreme-drag samples land at exactly ±6.0, the remaining 2 at 5.8/5.9 (consistent with
     the drag gesture not quite reaching the slider's physical edge before release, not a different
     clamp value). Units not independently confirmed (plausibly dB), unaffected by this promotion.
  3. **Confirmed preset quintets** (`PROTOCOL.md` §4.2): `Last saved`/Heavy bass/Light bass/
     Balanced/Vocal boost/Clarity, each a `[Low bass, Bass, Mid, Treble, Upper treble]` 5-tuple.
     Evidence: `CAP-015-FINDINGS.md` §5 — Heavy bass's quintet independently matches the
     2026-08-15 capture's own decode byte-for-byte.
  4. **Reconnect, Buds-initiated variant** (`PROTOCOL.md` §5.1): a single `Rcvd Connect Request` →
     `Sent Accept Connection Request` → `Rcvd Connect Complete` sequence, landing within 0.5s of
     on-camera earbud removal from the case. Evidence: `CAP-016-FINDINGS.md` §1, frames 1213–1217.
  5. **Disconnect-on-redock** (`PROTOCOL.md` §7): ACL `Disconnection Complete` (reason `0x13`,
     Buds-initiated) fires the instant the *second* bud is placed in the case, not on lid-close
     alone. Evidence: `CAP-016-FINDINGS.md` §1.
  6. **Case-lid zero-signal** (`PROTOCOL.md` §7): opening/closing the case lid while both buds
     remain outside the case produces no wire-visible signal on any RFCOMM channel. Evidence:
     `CAP-016-FINDINGS.md` §5, independently reproducing `CAP-007-FINDINGS.md`(old) §3.4 —
     2-capture-confirmed.
  7. **DLCI 0x08 `Group 0x04 Code 0x12`'s alternating value is event-driven *and* autonomous**
     (`PROTOCOL.md` §6 Resolved): fires in step with DLCI-0x08 channel-(re)open events, and also
     continues firing during otherwise-idle stretches with no channel churn — neither purely
     reactive nor purely free-running. Evidence: first characterized this way in
     `CAP-004-FINDINGS.md` §5a Task 5 and `CAP-007-FINDINGS.md`(old) §3.2/§5, independently
     reconfirmed by `CAP-016-FINDINGS.md` §7 (8 pushes, cycling `0x02`/`0x03`, 2 in step with
     channel-(re)opens, 4 during idle stretches with no churn).
- **What this ADR does NOT clear**:
  - EQ's outer field 16 vs. 18 ("preview" vs. "fires on slider-release") reading remains 🟡
    HYPOTHESIS, unaffected — `PROTOCOL.md` §4.2 already states this explicitly; not promoted here.
  - DLCI 0x08 Code `0x12`'s value's actual *meaning* (what `0x02`/`0x03`/`0x04` represents) remains
    🔴 OPEN — this ADR covers only the event-driven-and-autonomous *behavior* characterization, not
    the value's semantics.
  - `CAP-016`'s other findings, already explicitly marked "not promoted"/"awaiting maintainer
    sign-off" in its own §8 (the ANC settable-toggles-byte refinement, the `AndroidHeadTracker` HID
    decode), are **not** covered by this ADR — they remain open, as already correctly tracked.
  - Band-gain units (dB or otherwise) remain unconfirmed.
- **Decision**: all seven findings above are accepted as 🟢 FACT.
- **Consequences**: `PROTOCOL.md`'s changelog rows for 2026-08-18 updated to cite this ADR instead
  of "not yet reviewed by maintainer"; the corresponding body sections (§4.2, §5.1, §7 ×2, §6
  Resolved) gain an explicit `ADR-016` citation, matching the citation style already used for
  `ADR-011`–`ADR-015`.

## ADR-017 — Supersedes ADR-003: AI-assisted mechanical decompilation and proto-schema extraction, within a maintainer-decides-relevance boundary; native `.so` disassembly assistance now in scope

- **Date**: 2026-08-30
- **Status**: Accepted
- **Context**: ADR-003 banned an AI coding assistant from attempting to reverse engineer
  `libmaestro`/`libgfps` binaries directly, requiring `.proto` schemas and opcodes to be extracted
  up front by the maintainer and treated as given inputs. The maintainer has now explicitly
  requested, in conversation, that AI assistance be allowed to help with the *mechanical* parts of
  APK decompiling and proto-schema extraction ahead of the newly-planned Phase 2 (APK reverse
  engineering) work (`TODO.md`, currently 0% done) — this is a maintainer-directed policy change,
  not the AI expanding its own scope. Per `PROJECT_RULES.md` §3 rule 9, this is recorded as a new,
  superseding ADR rather than an edit to ADR-003's existing text.
- **Options considered**:
  - Leave ADR-003 as-is (fully manual extraction only) — rejected per explicit maintainer
    instruction to enable AI assistance for Phase 2.
  - Let the AI independently decide which classes/strings/findings are relevant and record them as
    HYPOTHESIS entries in `REVERSE_ENGINEERING.md` — rejected: this would erode the evidence
    discipline in `PROJECT_RULES.md` §1 and conflicts with `AGENTS.md` §6's principle that
    relevance/promotion judgments are the maintainer's call, not an AI's.
  - Allow AI *mechanical* assistance only (running searches, listing candidate matches, explaining
    syntax/structure of already-surfaced code, running `pbtk` extraction, and — per the maintainer's
    explicit answer to this ADR's native-library question — disassembly-output analysis for native
    `.so` libraries), while the maintainer retains every relevance and hypothesis-recording
    decision — chosen.
- **Decision**:
  1. This ADR **supersedes ADR-003**.
  2. **New boundary.** An AI session **may**: run keyword/string searches across `jadx-output/`,
     `apktool-output/`, and `pbtk-output/`; run `pbtk` to extract `.proto` schemas from an
     already-obtained APK; list candidate matching classes/methods/strings; and explain the
     syntax/structure of already-surfaced decompiled or disassembled code — **including native
     `.so` disassembly output** (Ghidra/radare2 or similar), which the maintainer has explicitly
     placed in scope for this same mechanical-assistance boundary (resolving the question this ADR
     was asked to record, see below). An AI session does **not** decide which class, string, or
     finding is relevant to the protocol, and does **not** decide whether something becomes a
     recorded HYPOTHESIS (or FACT/ASSUMPTION) entry in `REVERSE_ENGINEERING.md` — both remain the
     maintainer's calls, unchanged from ADR-003's original intent.
  3. **Unaffected rule.** This ADR does **not** change `AGENTS.md` §6/§15's sign-off requirement:
     promoting anything to 🟢 FACT in `PROTOCOL.md`, or writing any other `DECISIONS.md` ADR
     (including one superseding this one), still requires explicit maintainer approval — an AI
     session may propose, never commit, exactly as before.
  4. **Native `.so` boundary, explicitly decided (not silently inherited):** disassembling native
     `.so` libraries is a materially deeper form of reverse engineering than DEX/Java decompilation,
     and was called out separately rather than left to ride along with this change. The maintainer's
     explicit answer (session of 2026-08-30): **in scope** for AI mechanical assistance, on the same
     terms as §2 above — search, list, and explain only; relevance and hypothesis decisions stay
     with the maintainer. `REVERSE_ENGINEERING.md`'s Native Libraries section note (written under
     ADR-003's old blanket restriction) is updated accordingly so it no longer contradicts this ADR.
- **Consequences**: Phase 2 (APK static analysis, `TODO.md`) can proceed with AI assistance on its
  mechanical steps — keyword/string search, `pbtk` extraction, native-binary disassembly-output
  explanation — without waiting for the maintainer to perform every step manually. The evidence
  discipline in `PROJECT_RULES.md` §1 is preserved because relevance and hypothesis-recording
  decisions stay exclusively with the maintainer. This does not change §4/§8 rule 20's rules on what
  gets committed to this project's own codebase (no copied code, no committed decompiled output, no
  committed APK — see the versioned storage structure introduced alongside this ADR). Native `.so`
  disassembly assistance being newly in scope is a deliberate, separately-recorded decision (this
  ADR's §4), not an incidental scope expansion.

## ADR-018 — DLCI 0x02 confirmed as the companion app's own internal RFCOMM channel (SDP UUID + APK-code correlation, 3 independent captures); channel *ownership* promoted to FACT, Sent-payload *content* remains HYPOTHESIS

> **Maintainer sign-off obtained 2026-08-30** (session record: maintainer selected "Option 2" from
> the options below). This entry was originally drafted by an AI session as a labeled proposal
> (`Status: Proposed`) per `AGENTS.md` §6, and is updated in place — not stacked as a new entry —
> now that the maintainer has reviewed and decided, per this file's non-destructive-update
> convention.

- **Date**: 2026-08-30
- **Status**: Accepted — Option 2 (narrow promotion)
- **Context**: `PROTOCOL.md` §2.2a already promoted DLCI 0x02's **framing mechanism** (HDLC flag/
  escape/LEB128-address/CRC-32) to 🟢 FACT (2026-08-12, `pbpctrl`-notes cross-reference +
  640/640-subframe CRC verification across `CAP-001`–`CAP-003`). What §2.2a/§2.3 explicitly left at
  🟡 HYPOTHESIS (strong) is a narrower claim: that this specific channel *is* `libmaestro`'s own
  settings channel, as opposed to some other Pigweed-RPC-based Google service sharing the same
  framing library. §2.2a states two paths to close that gap: (a) decode the opaque "Sent"-direction
  payload bytes and recognize an actual `libmaestro` method call, or (b) an isolated
  single-action capture correlating one "Sent" write to one specific user action. Neither had
  happened yet.
  An AI-run §4 keyword-search pass over `v1.0.955078536-10253511`'s decompiled APK (`DECISIONS.md`
  ADR-017's mechanical-assistance boundary; full write-up in `REVERSE_ENGINEERING.md`'s `fzd`/`gbm`/
  `gau`/`gbd`/`fxm`/`fsz`/`fut`/`fux`/`ghd`/`goq` entries) found the app's own RFCOMM-socket-selection
  logic: `gbm.java:35-43` picks between two internal RFCOMM sockets by checking which of two 128-bit
  UUIDs (each present in both a canonical and a byte-reversed form, `fzd.java:9`) is in the
  discovered SDP UUID set, logging **`"Provide pigweed internal rfcomm socket"`** for UUID
  `25e97ff7-24ce-4c4c-8951-f764a708f7b5` and **`"Provide default internal rfcomm socket"`** for a
  second, distinct UUID (`3a046f6d-24d2-7655-6534-0d7ecb759709`). Separately, `fsz.java:223` — a
  Kotlin function-reference metadata string that survived R8 renaming — literally names the app's
  own `com.google.android.apps.wearables.maestro.companion.pw.hdlc.RouteProto$Route` class and the
  upstream `dev.pigweed.pw_rpc.MethodClient` class, and `fux.java`/`fxm.java`/others enumerate real
  `maestro_pw.*` pw_rpc services (`Maestro`, `HeadGesture`, `EartipFitTest`, `Dosimeter`,
  `JitterBuffer`, `Multipoint`, `DynamicServerConfigService`) called through this same selection
  path.
  This "pigweed" UUID was then checked against 3 independent captures already in `captures/`
  (`CAP-001`, `CAP-002`, `CAP-032`; `bluetooth.addr == 04:00:6e:cf:6e:07`): in every session, the SDP
  Service Search Attribute Response lists `25e97ff7-24ce-4c4c-8951-f764a708f7b5`, its Protocol
  Descriptor List response resolves it to **RFCOMM server channel 1**, and `tshark`'s own
  `btrfcomm.dlci` field reads **`0x02`** for every frame once that channel opens (`CAP-001` frame
  1334 @ 42.545s; `CAP-032` frame 1645 @ 105.173s) — a direct wire reading, not the `2×channel`
  arithmetic applied blind. Full frame/timestamp citations are in `REVERSE_ENGINEERING.md`'s `gbm`
  entry and §UUID register. The second, "default"-labeled UUID (`3a046f6d-...`) was searched for
  (both byte orders) across all 23 raw `*btsnoop_hci.log` files under `captures/` and found in none
  of them — an open question, not explained by this pass.
- **What this new evidence is, precisely — and what it is not:** it establishes, from the app's own
  compiled selection logic plus a reproducible SDP/RFCOMM wire correlation, that DLCI 0x02 is the
  specific RFCOMM channel *this companion app itself* selects and labels "pigweed," and that the app
  calls real `maestro_pw.*` pw_rpc services (including `WriteSetting`) through that same selection
  path. It does **not** decode the opaque "Sent"-direction payload bytes on DLCI 0x02, and does
  **not** correlate one specific "Sent" write to one specific user action — i.e. it does not satisfy
  either of §2.2a's two originally-stated paths (a)/(b) in the form they were written. It is a third,
  independent evidentiary path: static app-code correlation via the SDP layer, rather than payload
  decoding or capture isolation.
- **Options considered** (maintainer's choice, not decided by this proposal):
  1. **Promote fully**: treat this SDP+code correlation as sufficient to move DLCI 0x02's
     channel-identity claim ("this is `libmaestro`'s channel") from 🟡 HYPOTHESIS (strong) to 🟢 FACT
     in `PROTOCOL.md` §2.2a/§2.3, on the reasoning that tying the DLCI directly to the app's own
     compiled selection logic and self-identifying log string is at least as strong as decoding one
     opaque payload would be.
  2. **Promote narrowly** (mirrors ADR-013's precedent of promoting only what's cleanly warranted):
     record as 🟢 FACT only that *this RFCOMM channel is the companion app's own internal channel,
     distinct from any other/generic Pigweed-based service* — leave "and its Sent-payload content is
     specifically `libmaestro`'s ANC/EQ/settings commands" at 🟡 HYPOTHESIS (strong) pending §2.2a's
     original paths (a)/(b).
  3. **Do not promote**: keep §2.2a/§2.3's status text exactly as-is, and append this SDP+code
     correlation to `PROTOCOL.md` purely as additional strengthening evidence for the existing 🟡
     HYPOTHESIS (strong) label, explicitly reserving promotion for actual payload-content decoding or
     an isolated single-action capture.
- **Decision**: **Option 2, accepted.** Per `ARCHITECTURE.md` §2.1/`PROJECT_RULES.md` §1's
  promotion rules, `PROTOCOL.md` is updated to record 🟢 FACT that DLCI 0x02 is the Pixel Buds
  companion app's own internal RFCOMM channel — distinct from any other/generic Pigweed-based
  service — based on the SDP UUID (`25e97ff7-24ce-4c4c-8951-f764a708f7b5`) the app's own code
  (`gbm.java`/`fzd.java`) selects and labels "pigweed internal rfcomm socket," confirmed on the wire
  as RFCOMM channel 1 = DLCI 0x02 across `CAP-001`/`CAP-002`/`CAP-032`. **Not promoted:** that this
  channel's Sent-direction payload *content* specifically carries `libmaestro`'s ANC/EQ/settings
  commands — that stays 🟡 HYPOTHESIS (strong), pending §2.2a's original paths (a) decoding the
  opaque payloads via a pw_rpc/protobuf schema, or (b) an isolated single-action capture. See
  `PROTOCOL.md` §2.2a ("Channel ownership" finding), §2.3's three-channel table, the 2026-08-14
  addendum's Status line, and §4.2's EQ entry — all updated together for consistency, since they
  restate the same underlying claim.
- **Consequences**: `ARCHITECTURE.md` §2.1's per-channel implementation gate is **not** unblocked
  for `FrameEncoder`/`FrameDecoder` work against DLCI 0x02's actual settings semantics — the opaque
  "Sent" payload content remains undecoded; only the channel-*identity* question is settled. This
  does give future work a firmer footing to state "this is `libmaestro`'s own channel" without
  hedging, when discussing which channel to target for payload-decoding work (§2.2a's paths (a)/(b)).
  Neither DLCI 0x08's still-🔴 open identity question nor the "default internal rfcomm socket"
  UUID's unexplained absence from every capture searched so far is affected by this decision.

## ADR-019 — `qhr`'s oneof structure confirmed inside DLCI 0x02's `field5{field4{...}}` wrapper (2 sampled fields); `qhr` fields 4 and 7 promoted to FACT; `qhr` field 12's field-number identity (not its name) promoted to FACT

- **Date**: 2026-08-30
- **Status**: Accepted
- **Context**: a 2026-08-30 session combined (a) a Tier 0 re-decode of existing captures against a
  same-day APK static-analysis pass that recovered `libmaestro`'s real `WriteSetting` request schema
  (`REVERSE_ENGINEERING.md`'s `qjc`/`qja`/`qhr`/`qjo`/`qju`/`qjg`/`qht` entries), and (b) a Tier 2
  static-analysis pass tracing `qhr`'s remaining write/read call sites. `ADR-013` had promoted only
  DLCI 0x02's *outer* `field5{field4{...}}` wrapper shape to FACT, explicitly leaving the "..." itself
  undecoded; `ADR-018` (Option 2) separately promoted the *channel*'s ownership to FACT while leaving
  its payload *content* at 🟡 HYPOTHESIS (strong). This session's findings were presented to the
  maintainer as four discrete candidate promotions (session of 2026-08-30); the maintainer reviewed
  each individually and approved all four, three as proposed and the fourth in its narrower form
  (field-number identity only, not the semantic name), per `AGENTS.md` §6's requirement that an agent
  may propose but never unilaterally commit a FACT promotion.
- **Findings being recorded**:
  1. **DLCI 0x02's `field5{field4{...}}` wrapper's inner content, for the two fields sampled, is
     `qhr`'s own protobuf oneof, addressed via standard wire-format tags — not merely "plausible" per
     `ADR-013`'s own note.** Two existing `CAP-020` Sent frames already identified as
     `field5{field4{...}}` (`CAP-020-FINDINGS.md` §3/§4, frames 1741/1935 — `TOUCH-001`/`HEAD-001`)
     were re-pulled directly from the raw log, HDLC-unescaped/CRC-verified, and decomposed one level
     further than that file's own original decode. Frame 1741's inner bytes decode to `qhr` field
     **4**, value `1`; frame 1935's to `qhr` field **29**, value `2` — an exact, byte-for-byte match to
     the independently-derived (APK code, not wire) `qhr` schema, on both sampled fields, no
     counter-example. This is two independent evidence paths (real wire bytes vs. compiled app code)
     converging on the same structure, not one path repeated.
  2. **`qhr` field 4 = the "Use touch controls" master enable toggle** (`PROTOCOL.md` §4.5.3's
     top-level toggle). Evidence: `CAP-020` frame 1741 (wire+video correlation, `TOUCH-001`, already
     🟡 HYPOTHESIS) **and**, independently, the app's own code — write site `fyo.java:124-144`, read
     site `fxb.java` case 4 logging `"Log Gestures Enable setting"` (self-describing, not a naming
     inference).
  3. **`qhr` field 7 = `qju` = the Left/Right press-and-hold gesture-*action* customization**
     (ANC / Digital assistant / None), matching `PROTOCOL.md` §4.5.3's already-strong press-and-hold
     HYPOTHESIS. Evidence: `CAP-021` frames 1895/3619/4315/4976 (`HOLD-001`–`HOLD-004`, all 4 of the
     2×2 Left/Right × ANC/Assistant combinations, wire+video correlated) **and**, independently, the
     app's own code — write site `fyo.java:300-374` (`t(gdx)`), read site `fxb.java` case 7 logging
     **`"Log Gestures Customization for touch and hold setting, left: %s, right: %s"`** — a literally
     self-describing match, not an inference from shape or position. Re-decoding the 4 wire frames
     also surfaced a nesting level finer than `CAP-021-FINDINGS.md`'s original notation: the value
     sits inside a `qju.field{1|2}` → `qik` → `qho` chain, not a bare varint directly under
     `field1`/`field2` — recorded as a correction to that file's own decode, not a new claim.
  4. **`qhr` field 12 = `qht` — the field-*number* identity only.** Evidence: `CAP-021` frames
     5237/5247/5255 (`HOLD-005`) decode to `qhr` field 12 with exactly 4 boolean sub-fields, matching
     `qht`'s independently-confirmed shape (APK code: `qhr`'s field-12 alternative, write site
     `hgj.java:216-331`, read site `fxb.java` case 12 logging `"Log ANC gesture loop to Clearcut"`).
     **Not promoted**: whether the app's own internal name for this field, "ANC gesture loop," is the
     *same* UI feature as `PROTOCOL.md` §4.5.3's existing "ANC-mode rotation checklist" (`HOLD-005`)
     HYPOTHESIS — the two could describe the same setting seen from two angles, or two different
     settings that happen to share a 4-boolean shape; this has not been reconciled, and the maintainer
     explicitly declined to promote that equivalence at this time.
- **What this ADR does NOT clear**:
  - The nesting-structure finding (1) is sampled on exactly 2 fields (4 and 29) in one capture
    session — it establishes that the "..." *is* `qhr`'s oneof for those two instances, not that every
    one of `qhr`'s 38 fields has been wire-confirmed to decode this way. It does not by itself resolve
    `ADR-018`'s own remaining HYPOTHESIS (that DLCI 0x02's Sent-direction content specifically carries
    `libmaestro`'s settings-write commands in general) — it substantially strengthens that HYPOTHESIS
    for the specific fields tested, but `ADR-018`'s broader claim is not re-litigated or promoted by
    this entry.
  - Finding (2)'s field 4 is one-direction (OFF→ON) only, one session.
  - Finding (3)'s 4/4 combination coverage is strong, but still one capture session for the wire half;
    the code half (the self-describing log message) is a separate, independent confirmation type, not
    a second capture.
  - Finding (4) explicitly does **not** promote `qht`'s app-internal name or its equivalence to the
    rotation-checklist HYPOTHESIS — only that wire field 12 = code's `qht` (a field-number/shape match).
  - `qhr` field 29 (Head gestures, `PROTOCOL.md` §4.5.4) was also re-confirmed at the wire level this
    session (frame 1935) but is **not** part of this ADR — no self-describing code-side name was found
    for field 29 (its write call site was not located by the static pass), so only one evidence path
    exists for it; it remains 🟡 HYPOTHESIS, unchanged.
- **Decision**: findings 1, 2, and 3 above are accepted as 🟢 FACT in full. Finding 4 is accepted as
  🟢 FACT for the field-number identity (`qhr` field 12 = `qht`) only; the "ANC gesture loop" /
  "ANC-mode rotation checklist" naming equivalence remains 🟡 HYPOTHESIS.
- **Consequences**: `PROTOCOL.md` §4.5.3 updated — the top-level toggle and press-and-hold-action
  opcodes move from 🟡 HYPOTHESIS to 🟢 FACT; the rotation-checklist opcode gains a FACT-confirmed
  field number but keeps its HYPOTHESIS status for what the field represents. `PROTOCOL.md` §2.2a/§2.3
  and §6's "what do DLCI 0x02's confirmed inner field numbers actually represent" open item are
  updated to record that, for the 3 fields tested, the answer is "real `qhr` protobuf field numbers
  from the app's own recovered schema," narrowing (not fully closing, per the scope note above) that
  question. Does not unblock `ARCHITECTURE.md` §2.1's `FrameEncoder`/`FrameDecoder` implementation gate
  for DLCI 0x02 generally — that still requires the broader payload-content HYPOTHESIS in `ADR-018` to
  reach FACT, which this ADR narrows but does not itself complete.
- Update (2026-09-03): clarifying how findings 2-4 and the field-29 exclusion above were actually
  decided, after a later summary compressed the reasoning into a single "a self-describing code site
  exists" test. That compression loses a distinction this ADR relied on — two separate dimensions
  were in play, not one:
  a. Whether an independent code-side evidence path exists at all. Field 29's exclusion (above) was
     not "a code site existed but wasn't self-describing" — this ADR's own text is explicit that "no
     self-describing code-side name was found for field 29 (its write call site was not located by
     the static pass), so only one evidence path exists for it." The write call site itself was
     never located; there was no code-side path to evaluate for self-description in the first place.
     A field whose code path simply hasn't been found yet is not evidence-equivalent to a field
     whose code path was found and found generic or unnamed — anyone re-running this method on
     further fields should keep that distinction.
  b. A self-describing code path existing does not, by itself, clear the bar for full semantic
     promotion. Field 12 is this ADR's own counter-example: its read site's log message ("Log ANC
     gesture loop to Clearcut," finding 4) is self-describing, and yet only the field-number identity
     was promoted, not the full semantic claim — because the code's own name ("ANC gesture loop") was
     not reconciled with the pre-existing "ANC-mode rotation checklist" HYPOTHESIS the wire evidence
     had already proposed. Full semantic promotion, as granted in full to findings 2 and 3, requires
     the self-describing code name to match, or be explicitly reconciled with, whatever
     hypothesis-level name already existed — not merely to exist.

   Neither point changes findings 1-4, "What this ADR does NOT clear," or the Decision/Consequences
   above — this note only corrects a compressed restatement of reasoning already used to reach them.
- Update (2026-09-03): five further `qhr` field promotions, reviewed and approved by the maintainer
  individually per field, applying this ADR's own evidence standard (independent wire-capture
  evidence plus independently-traced app code, cross-validated) and the two-dimension distinction
  clarified in the note directly above.
  - **Field 17 = "Volume balance" — full identity, 🟢 FACT.** Evidence: `CAP-022-FINDINGS.md` §5
    (7 wire samples across one continuous drag gesture — `CAP-022` frames 1922/1944/2019/2039/2056/
    2073/2099, all CRC-32 verified, raw hex backfilled this session) **and**, independently, the
    app's own code — write site `fxf.java:82-133` (case 16 of that dispatcher), read side logging
    `"received last saved volume balance setting value"` (`fxb.java` case 17) — a self-describing
    match to the pre-existing "Volume balance" HYPOTHESIS, satisfying dimension (b) above.
    **Correction accompanying this promotion:** `qhr`'s own schema types field 17 as `SINT32`
    (`REVERSE_ENGINEERING.md` line 856), so its wire values must be zigzag-decoded, not read as raw
    unsigned varints. The 7 sampled values were previously recorded as `199, 123, 49, 30, 150, 200,
    10`; correctly zigzag-decoded (`(n>>1) ^ -(n&1)`) they are `-100, -62, -25, 15, 75, 100, 5`. The
    scale/range beyond these 7 samples and which direction (Left/Right) corresponds to negative vs.
    positive values remain 🔴 open — this correction narrows, but does not resolve, `PROTOCOL.md`
    §6's existing open item on this field.
  - **Field 19 = "Mono audio" — full identity, 🟢 FACT.** Evidence: `CAP-022-FINDINGS.md` §3
    (`CAP-022` frames 1621/1823, both directions, CRC-32 verified) **and**, independently, the app's
    own code — write site `fyo.java:278-298` (`s`), read side logging `"received mono setting
    value"` (`fxb.java` case 19) — a self-describing match to the pre-existing "Mono audio"
    HYPOTHESIS.
  - **Field 22 = `qhr`'s own "Speech Detection" — field-number/type identity only, 🟢 FACT.**
    Evidence: `CAP-019-FINDINGS.md` §3 (single OFF→ON sample) **and**, independently, the app's own
    code — write site `hnz.java:29-49` (`a`, logging `"Set Speech Detection"`), read side `fxb.java`
    case 22. Per dimension (b) above: the code's own name, "Speech Detection," is not the same
    string as `PROTOCOL.md` §4.5.1's pre-existing "Conversation Detection" UI-label HYPOTHESIS —
    plausibly the same feature seen from two angles (an internal/engineering name vs. the UI's own
    label), but not reconciled. The maintainer reviewed this specifically and declined to promote
    that equivalence; it remains 🟡 HYPOTHESIS.
  - **Field 27 = a real, code-confirmed case-sound-family boolean — category level only, 🟢 FACT.**
    Evidence: `CAP-024-FINDINGS.md` §5 (`CAP-024` frames 2053/2084, both directions, CRC-32 verified,
    raw hex backfilled this session) **and**, independently, the app's own code — write site
    `fyo.java:80-100` (`e`), read side logging `"received case earcon setting value"` (`fxb.java`
    case 27). The code's own log message confirms this is *a* case-sound-category setting but does
    not itself distinguish which one — the specific "Other alerts"/"Other notifications" label
    (`CAP-024`'s own `CASE-002` test) remains 🟡 HYPOTHESIS, unreconciled with the generic code-side
    name, per dimension (b) above.
  - **Field 28 = "Bud return"/"Earbuds replaced" — full identity, 🟢 FACT.** Evidence:
    `CAP-024-FINDINGS.md` §4 (`CAP-024` frames 1988/2023, raw hex backfilled this session) **and**,
    independently, the app's own code — write site `fyo.java:58-78` (`d`), read side logging
    `"received bud return sound setting value"` (`fxb.java` case 28) — a self-describing match to
    the pre-existing "Bud return" HYPOTHESIS.

  As with the original four findings, none of these five promotions change the outer envelope's own
  already-FACT status (`DECISIONS.md` ADR-013) or unblock `ARCHITECTURE.md` §5's per-command
  implementation gate for any field beyond the ones explicitly promoted here — fields 11 and 15 are
  unaffected by this update and remain 🟡 HYPOTHESIS.
- Update (2026-09-08, maintainer sign-off via the chat session that authored prompt
  `ai-sessions/0003_MAINTENANCE_PROMPT_2026_09_08.md`, implementing that session's Phase 3 item 1
  finding): a sixth field promotion, applying this ADR's own evidence standard.
  - **Field 2 = "CATEGORY_OHD" (On-Head/In-ear Detection) — category-level identity, 🟢 FACT.**
    Evidence: `CAP-024-FINDINGS.md` §3 (`CAP-024` frames 1850/1912, both directions, video-confirmed)
    **and**, independently, the app's own code — the already-known write site (`fyo.java:169-188`,
    method `l`) is also reached from `MaestroDeviceSettingsProviderService` case `2102` (the system
    Settings app's own Bluetooth-device-details page), logged there under the internal category name
    `"CATEGORY_OHD"` (`fjm.H(14)`, a self-describing internal settings-taxonomy name, a source type
    not previously used for any `qhr` field promotion). Per dimension (b) of the 2026-09-03
    clarifying note above: the code's own name ("OHD"/"On-Head Detection") is closely related to,
    but not verbatim identical to, `PROTOCOL.md` §4.5.5's pre-existing "In-ear detection" UI-label
    HYPOTHESIS — the same kind of gap that kept fields 12/22/27 at category/field-number-level
    identity rather than full semantic identity. Promoted for field-number/category-level identity
    only; the specific "In-ear detection" label equivalence remains 🟡 HYPOTHESIS.

## ADR-020 — EQ `FrameEncoder`/`FrameDecoder` implementation explicitly unblocked

- **Date**: 2026-09-03
- **Status**: Accepted
- **Context**: `ADR-016` (2026-08-28) promoted EQ's wire envelope shape, its field-to-band mapping,
  the ±6.0 band-gain clamp, and the confirmed preset quintets to 🟢 FACT (`PROTOCOL.md` §4.2). Unlike
  `ADR-009` (ANC) and `ADR-011` (Find My Buds Left/Right), which each explicitly state that the
  `ARCHITECTURE.md` §5 per-command implementation gate is cleared, `ADR-016` never made the
  equivalent statement for EQ — a 2026-09-02 documentation audit flagged this as a gap: EQ's protocol
  knowledge is fully FACT-level, but its implementation-readiness status was left ambiguous rather
  than explicitly settled. The maintainer reviewed this gap directly (session of 2026-09-03) and
  explicitly instructed that it be closed via a new ADR, matching `ADR-009`/`ADR-011`'s pattern
  rather than an in-place edit to `ADR-016`'s own text (`PROJECT_RULES.md` §3 rule 9's
  non-destructive-update convention).
- **Finding being recorded**: none new — this ADR does not add any protocol knowledge. It records
  the maintainer's explicit decision that the FACT-level findings `ADR-016` already promoted (5×
  `float32` band-gain quintet on DLCI 0x02's `field5{field4{...}}` envelope, field 1↔Low bass /
  2↔Bass / 3↔Mid / 4↔Treble / 5↔Upper treble, wire order reversed from on-screen order, ±6.0 clamp,
  and the six confirmed preset quintets) are sufficient, on their own, to unblock implementation.
- **What this ADR does NOT clear**: EQ's outer field 16 vs. field 18 distinction (`PROTOCOL.md`
  §4.2/§6 — "live value" vs. "persisted value," and whether that maps to "preview" vs.
  "slider-release"/"commit") remains 🟡 HYPOTHESIS, unaffected by this ADR. An implementation needs to
  pick one field for a given write; per `PROTOCOL.md` §4.2's own code-derived reading
  (`REVERSE_ENGINEERING.md`'s `qjw` entry: field 16 = `fyp.f()`, "update user eq," fired on every
  slider-drag value change and on preset selection; field 18 = `fyp.d()`, "update last saved user
  eq," fired once per gesture and also persisted locally), field 16 is the correct target for a
  live/preview-style write — this ADR does not promote that reading to FACT, it only notes it as the
  practical default for an initial implementation. The gain unit (plausibly dB, never independently
  confirmed) and the ~13-byte correlation-ID/`call_id` region also remain unconfirmed, unaffected.
- **Decision**: EQ's `FrameEncoder`/`FrameDecoder` implementation is unblocked, per `ARCHITECTURE.md`
  §5's per-command implementation gate, for the elements `ADR-016` already promoted to FACT (the
  envelope wrapper, the 5-band quintet and its field-to-band mapping, the ±6.0 gain clamp, and the
  preset quintets).
- **Consequences**: `:data` can implement EQ's `FrameEncoder`/`FrameDecoder` now, against fixed
  byte-array fixtures per `AGENTS.md` §11, using field 16 for live/slider-drag writes as the
  practical default described above. The field-16-vs-18 semantic question and the gain-unit question
  remain open research items (`PROTOCOL.md` §6, `TODO.md`) and should be resolved before EQ ships a
  "Save as preset"-style UI affordance that specifically depends on field 18's exact semantics.

## ADR-021 — "Get ANC state" (`0x11`) opcode identity confirmed on the wire for the first time (DLCI 0x04); trigger-reliability explicitly NOT promoted

- **Date**: 2026-09-04
- **Status**: Accepted
- **Context**: `PROTOCOL.md` §4.1 has documented Message Group `0x08` Code `0x11` ("Get ANC
  state", Seeker→Provider) since the group was resolved from the official Fast Pair Hearable
  Controls spec (2026-08-12) — but no capture had ever observed a `0x11` frame on the wire; only
  `0x12` (Set) and `0x13` (Notify) had been seen (`CAP-001-FINDINGS.md` §5).
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AC / `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` `OBS-004` was
  designed specifically to isolate whether the official app ever issues this (or any) settings-state
  query, on two candidate triggers: reconnection and settings-screen-open. `CAP-036` (2026-09-04)
  ran that isolation and, in a clean reconnect window, found `08 11 00 00` (Sent, DLCI 0x04, frame
  1169) fired 34ms after the channel opens, answered ~10.7ms later by `08 13 00 04 01 e8 00 20`
  (Rcvd, frame 1182 — decodes to current ANC state = Off), which matched the on-screen ANC state
  confirmed later in the same session. The maintainer reviewed this finding directly (session of
  2026-09-04) and explicitly approved promoting the opcode's identity to FACT, while declining to
  promote the broader trigger-reliability claim from a single sample.
- **Finding being promoted**: `PROTOCOL.md` §4.1's `Get ANC state` (`0x11`) opcode — its exact
  Group/Code values, its zero-length/no-payload structure, its Seeker→Provider direction, and that
  it is real, observed wire traffic (not merely a documented-but-theoretical spec entry) — is now
  🟢 **FACT**, on the strength of an exact structural match to the official spec plus an internal
  content cross-check within the same capture (the Notify response's decoded value matching
  on-screen ground truth) — the same evidentiary pattern already used for `0x12`'s promotion
  (`PROTOCOL.md` §4.1, this document's earlier ADRs).
- **What this ADR explicitly does NOT promote:** whether this query reliably fires on *every*
  reconnection (this is a single sample from one session — `CAP-036` ran exactly one reconnect).
  This trigger-reliability claim remains 🟡 HYPOTHESIS pending replication in a second, independent
  capture. Also not promoted: `CAP-036`'s clean-negative finding that no query of any kind occurs
  on settings-screen-open (five clean windows, one session) — that stays 🟡 HYPOTHESIS for the same
  reason. Also not resolved: the `Settable toggles` byte in `CAP-036`'s Notify frame reads `0x00`,
  differing from every previously-documented Set frame's `0xe8` in the same position — left as an
  open question (`PROTOCOL.md` §6), not reconciled or promoted by this ADR.
- **Decision**: `PROTOCOL.md` §4.1's "Get ANC state" (`0x11`) opcode entry is promoted to 🟢 FACT
  for its identity/structure as described above. The trigger-reliability and settings-screen-open
  negative-result claims from the same capture remain 🟡 HYPOTHESIS, unaffected by this ADR.
- **Consequences**: a future `FrameEncoder`/`FrameDecoder` implementation of the ANC read path (if
  and when this project's own app wants to issue an equivalent read-on-reconnect query per
  `ARCHITECTURE.md` §3.1) can now target a confirmed opcode rather than a spec-only placeholder.
  Implementation should not yet assume the query is guaranteed to appear on every reconnect in the
  wild (the reliability question is still open) — a second capture reproducing this pair is a
  recommended, low-cost next step (`CAP-036-FINDINGS.md` §11's replication proposal) before treating
  the trigger itself as dependable.

## ADR-022 — "Get ANC state" (`0x11`) trigger-reliability promoted to FACT: 17 occurrences across 10 independent captures, zero misses

- **Date**: 2026-09-04
- **Status**: Accepted
- **Context**: `ADR-021` promoted this opcode's *identity* to FACT from a single `CAP-036` sample,
  while explicitly declining to promote whether it reliably fires on every reconnect — that
  required more evidence than one session could provide. The maintainer subsequently asked for a
  bonus battery/firmware analysis pass across other existing captures (two rounds,
  `DESKRESEARCH_FINDINGS.md`'s 2026-09-04 entries); the second round specifically targeted seven
  settings-toggle sessions (`CAP-019`–`CAP-025`) that had never been checked for this opcode, plus
  `CAP-006` and `CAP-010`.
- **Finding being promoted**: across `CAP-006` (×3), `CAP-010` (×2), `CAP-016` (×1, from the first
  bonus round), `CAP-019`–`CAP-024` (×1 each), `CAP-025` (×5), and `CAP-036` (×1) — **17 total
  occurrences across 10 independent capture files** — `08 11 00 00` (Sent, DLCI 0x04) fires and is
  answered by `08 13` (Rcvd) within tens of milliseconds, **every single time**, under a precisely
  identified trigger condition: DLCI 0x04 (re)establishes (`SABM`→`UA`) **and** subsequently carries
  real Message Stream payload. This holds even when the underlying classic ACL link does **not**
  itself disconnect/reconnect — `CAP-006` and `CAP-025` each show the query re-firing on a
  DLCI-0x04-only channel bounce within one continuous ACL connection (`CAP-025` shows this 5 times
  in one log, confirmed via its own single, unbroken HCI `Connection Complete`). The negative
  control also holds: `CAP-025` additionally contains 3 bare `SABM`→`UA`→`DISC` channel bounces
  carrying **zero** payload, and **none** of those trigger a new `Get` — the trigger condition is
  precise, not "any DLCI 0x04 activity."
- **Evidentiary bar met**: 17 occurrences / 10 independent sessions / zero misses against a
  precisely-scoped condition exceeds the sample size this project has previously required for FACT
  (`ADR-009`: 4 samples in one capture; `ADR-014`: 4 independent sessions).
- **What this ADR does NOT promote:** the Settable-toggles byte's own meaning (still 🟡 HYPOTHESIS,
  `PROTOCOL.md` §4.1 — now read as tracking whether the Buds are in/near the case rather than
  connect-timing, per the same bonus analysis, but not maintainer-reviewed for promotion);
  `CAP-036`'s settings-screen-open clean-negative result (a different sub-question, unaffected);
  any claim about *why* the query fires on this trigger (mechanism/purpose not investigated).
- **Decision**: `PROTOCOL.md` §4.1's "Get ANC state" (`0x11`) trigger-reliability claim — "fires
  whenever DLCI 0x04 (re)establishes and carries real Message Stream payload, independent of
  whether the underlying classic link itself reconnects" — is promoted to 🟢 FACT.
- **Consequences**: this project's own `ARCHITECTURE.md` §3.1 (State Reconciliation) design — query
  hardware state on every (re)connection before trusting a cached value — is now confirmed to
  match a real, reliably-observed behavior of the official app for ANC specifically, not merely a
  single-session anecdote. A `FrameEncoder`/`FrameDecoder` implementing this specific read (if
  pursued) can rely on the trigger condition described above.

## ADR-023 — Retroactive sign-off: Option C (HFP battery) confirmed independent of GMS/app, and confirmed on GrapheneOS

- **Date**: 2026-09-05
- **Status**: Accepted
- **Note on process**: this ADR closes a self-caught process gap, the same pattern as `ADR-016`.
  While writing up `DESKRESEARCH_FINDINGS.md`'s 2026-09-04 bonus battery/firmware cross-check, the
  two findings below were marked 🟢 FACT directly in `PROTOCOL.md` §4.3 Option C without first
  obtaining the explicit maintainer sign-off `AGENTS.md` §6 requires for every FACT promotion, no
  exceptions. This was caught by the agent itself on a later pass (not flagged by the maintainer)
  and surfaced explicitly in the next session (2026-09-05) rather than left standing uncorrected.
  The maintainer reviewed both findings directly in that session and explicitly approved recording
  them as FACT retroactively, rather than reverting them to HYPOTHESIS pending a separate review.
- **Findings being recorded**:
  1. **Option C (HFP `AT+BIEV=2,<value>` battery reporting) is independent of both Google Play
     Services and the official companion app** (`PROTOCOL.md` §4.3 Option C). Evidence: `CAP-004`
     (`DESKRESEARCH_FINDINGS.md` 2026-09-04 entry) — GMS **disabled** and the official app
     **uninstalled** together (the strongest independence condition of any capture on disk) —
     `AT+BIEV=2,100` still fires normally, multiple times. `CAP-033` — the official app
     **force-stopped** for the entire session, GMS untouched — `AT+BIEV=2,100` also fires
     normally, multiple times. Both are simple, unambiguous binary observations (the AT command is
     present in the log or it is not) rather than an interpretive reading, extending
     `CAP-035-FINDINGS.md`'s existing GMS-independence result (which only checked DLCI
     0x08/0x0a/0x06/0x12) to Option C specifically.
  2. **Option C also works on GrapheneOS itself, not only stock Android** (`PROTOCOL.md` §4.3
     Option C). Evidence: `CAP-035` (Pixel 9a/GrapheneOS, GMS present but `dumpsys`-verified
     disabled, no official app, no nRF Connect) — `AT+BIEV=2,100` fires on both the fresh connect
     and the later reconnect in that session. Same evidentiary character as finding 1: a direct,
     unambiguous presence/absence observation.
- **What this ADR does NOT clear**: no other finding from either bonus-analysis round is affected
  — the Settable-toggles "in/near-case vs. actively-worn" reading (`PROTOCOL.md` §4.1) remains 🟡
  HYPOTHESIS (correlational, not reconciled against any documented field meaning, and the
  "actively worn" status for several sessions is inferred from the session's own procedure rather
  than directly video-verified per sample) — not promoted by this ADR, and not proposed for
  promotion this round; the `CAP-027` cross-channel-sync-caveat and the `CAP-036` BLE
  device-attribution advance likewise remain untouched, single-session HYPOTHESES.
- **Decision**: both findings above are accepted as 🟢 FACT, as already written in `PROTOCOL.md`
  §4.3 Option C.
- **Consequences**: `ARCHITECTURE.md` §4's battery-fallback priority order can rely on Option C
  (HFP) as a mechanism that does not depend on Google Play Services or the companion app being
  installed/running, and functions on GrapheneOS specifically — directly relevant to this
  project's Zero-GMS goal (`AGENTS.md` §1) and its GrapheneOS target platform (`AGENTS.md` §2).
- **Update (2026-09-24, `ai-sessions/0045`):** the Consequence that the battery priority order "can rely on Option C" is superseded
  by ADR-040 (the wire behaviour is unchanged; an app cannot receive it). The two FACTs stand.

## ADR-024 — "Notify ANC state" `Settable-toggles` byte confirmed as a dock-state indicator: `0x00` when both earbuds are seated in the case, `0xe8` otherwise

- **Date**: 2026-09-05
- **Status**: Accepted
- **Context**: `CAP-036-FINDINGS.md` §3 flagged the "Notify ANC state" frame's `Settable-toggles`
  byte reading `0x00` as an unreconciled discrepancy against every prior sample's `0xe8`.
  `DESKRESEARCH_FINDINGS.md`'s first bonus round found `CAP-016-FINDINGS.md` §4 had already
  observed the same `0x00` value, with its own 🟡 HYPOTHESIS that it tracks whether the Buds have
  "reported which ANC modes are currently selectable" — plausibly tied to dock state. The second
  bonus round found 12 more samples (7 sessions) all showing `0xe8`, each in a session where the
  Buds were presumed (not directly checked) to be actively in use — sharpening the hypothesis to
  "in/near-case vs. actively worn," still uncorroborated by direct video evidence for most
  samples. The maintainer asked for that video verification before considering promotion.
- **Finding being promoted**: a dedicated video check (`DESKRESEARCH_FINDINGS.md` 2026-09-05
  entry), using `ffmpeg` frame extraction against each video's own wall-clock overlay, checked 3
  new samples against their exact wire timestamps and found the case's dock state, not "worn"
  per se, is the determining factor:
  - `CAP-010`, `Settable=0x00` — both earbuds visibly seated in the case's charging slots, LED lit
    (mid Fast-Pair "Save device to account" dialog).
  - `CAP-021`, `Settable=0xe8` — case open, both slots empty (confirmed via a cropped/zoomed
    frame), Buds off-frame.
  - `CAP-025`, `Settable=0xe8` — case open, both slots empty, both Buds visible resting loose
    beside the case (not docked, not necessarily worn either — refining "worn" to "not docked").
  Combined with `CAP-016`'s original frame (both Buds docked, `0x00`) and `CAP-036`'s entire
  session (Buds sitting in the open case throughout, never removed, `0x00` — confirmed via that
  session's own full video re-pass), this is **5 of 5 video-checked samples confirming the same
  pattern, zero counter-examples**, across 5 independently-run sessions with different procedures
  (a case/bud-removal test, a fresh-pairing repeat, two settings-toggle sessions, and a
  reconnect-isolation test).
- **What this ADR does NOT clear**: `CAP-006`'s own two samples (`0xe8` then `0x00` within one
  session) remain unverified — `CAP-006-recording.mp4` fails to open in `ffmpeg`
  (`stream 1, contradictionary STSC and STCO`/`error reading header`) and no repair tool was
  available; this would have been the first *within-session* transition check and is a genuine
  gap, not a negative result. `CAP-036`'s settings-screen-open clean-negative finding (a separate
  sub-question) is unaffected. The remaining 12 `0xe8` samples from `CAP-019`/`020`/`022`–`024`
  were not individually video-checked this pass (their session type — active settings-toggle
  tests — is consistent with the pattern but not each individually confirmed frame-by-frame).
- **Update (2026-09-05, same day):** the maintainer re-pulled `CAP-006-recording.mp4` from the
  phone; the replacement file (79.49s, opens cleanly in `ffmpeg`) covers `CAP-006`'s first two
  `Settable` samples. Both video-confirmed: `17:23:54.37` (`Settable=0xe8`) — case open, **both
  slots empty** at video start and throughout; `17:25:02.03` (`Settable=0xe8`) — case still empty.
  **Now 7 of 7 video-checked samples confirm the pattern, zero counter-examples** (adds 2 to the
  5 above). `CAP-006`'s own *third* sample (`17:26:55.06`, `Settable=0x00`) remains unverified —
  the replacement file, like the original, ends at ~17:25:08, and the session's own log runs to
  17:27:30, well past either video's coverage. The within-session `0xe8`→`0x00` transition this
  ADR's "what this ADR does NOT clear" section flagged is therefore still open, independent of the
  file corruption issue being resolved.
- **Decision**: `PROTOCOL.md` §4.1's `Settable-toggles` byte is promoted to 🟢 FACT as a dock-state
  indicator: `0x00` when both earbuds are seated in the case, a non-zero value (`0xe8` in every
  sample seen to date) otherwise.
- **Consequences**: a future implementation reading this field can treat it as a live dock-state
  signal from the accessory itself, independent of (and potentially more immediate than) the
  case/bud-removal Bluetooth events `PROTOCOL.md` §5/§7 already document from other channels — a
  candidate cross-check for `ARCHITECTURE.md`'s connection/dock-state model. The exact bit-level
  meaning of `0xe8` beyond "not both docked" (e.g. whether it varies further for one-bud-docked
  states) remains unexplored and is not claimed by this ADR.
- **Update (2026-09-13, maintainer sign-off, `ai-sessions/0015_MAINTENANCE_RESULT_2026_09_13.md`):
  two counter-examples found in a single session, not yet reconciled.** `CAP-048`
  (`CAP-048-FINDINGS.md` §5, a purpose-built repeat with continuous dock-state video) found two
  fresh classic reconnects (`17:44:45`, `17:47:42`) reporting `Settable-toggles=0x00` (docked) while
  the video, checked at essentially the same wire timestamp, shows the case visibly **empty**. Four
  other readings in the same session (including two same-chandle DLCI reopens, not fresh
  reconnects) are correct — this is not a reversal of this ADR's own dock-state-indicator finding,
  which the same session's other readings continue to confirm. 🟡 **HYPOTHESIS, not confirmed:** a
  fresh reconnect's own Get/Notify may occasionally return a value queried before the Buds' own
  firmware has settled on an already-changed physical state — offered as a testable direction only;
  does not by itself explain why the other four fresh reconnects in the same session read
  correctly. Implementations reading this field on a fresh reconnect specifically (as opposed to a
  same-chandle DLCI reopen) should treat it as usually, not unconditionally, reliable immediately
  after connection.
- **Update (2026-09-18, `CAP-047-FINDINGS.md` §5) — three further counter-examples, from an
  independent session, reproducing and extending the same still-open reliability question.**
  `CAP-047` (Group AL, Recording 2) found three additional `Settable-toggles=0x00` ("both docked")
  readings that do not match the video-confirmed physical dock state at the same wire timestamp,
  alongside this session's own primary result (§5's swapped-slot-seating check, unaffected and
  still 🟢 FACT — the byte correctly reads `0x00` for a swapped-slot docking, confirming the sensor
  tracks physical presence only, not per-slot identity):
  - **Frame 3364** (`06:34:46.79`) — follows a DLCI 0x05 channel-bounce reopen (not a full ACL
    reconnect) at `06:34:45.78`–`45.92`. Only one bud is seated at this timestamp per video
    re-check. Extends the settling HYPOTHESIS's trigger condition from "fresh ACL reconnect" to
    "any fresh DLCI (re)establishment."
  - **Frame 3834 → 3996** (`06:35:01.05` → `06:35:02.20`) — a fresh ACL reconnect's own
    connect-time Get/Notify reads `0x00` while only one bud is seated; a spontaneous re-Notify
    1.15s later self-corrects to `0xe8` with no intervening Get. This is a **second, independent
    reproduction** of the exact settling pattern `CAP-048-FINDINGS.md` §5 first proposed —
    strengthening it from a single-session anecdote to a reproduced pattern, but still not
    independently confirmed as *the* mechanism (no direct evidence of *why* the firmware/stack
    returns a stale value, only that it does and self-corrects).
  - **Frame 3048** (`06:34:39.51`) — 🔴 **a new, unreconciled failure mode, distinct from the
    settling pattern above.** No channel reopen or Get precedes this spontaneous Notify (DLCI 0x05
    had been open, unbroken, for 20 seconds); the case is empty per video re-check. Does not fit
    "stale value after a fresh (re)establishment" — there is no (re)establishment to be stale
    from. Left as an open question (`PROTOCOL.md` §6), not folded into the settling HYPOTHESIS by
    assumption.
  - **What this update does NOT change:** ADR-024's core dock-state-vs-not finding remains 🟢 FACT,
    unaffected — every genuinely-both-docked and genuinely-neither-docked reading in both `CAP-047`
    and every prior session continues to confirm it. The settling HYPOTHESIS remains 🟡 HYPOTHESIS
    (now with 2 independent reproducing sessions instead of 1, but still not independently confirmed
    as a mechanism). Frame 3048's own failure mode is **not** claimed to share a cause with the
    settling pattern — reported as a separate open question per `AGENTS.md` §13.6's zero-creativity
    discipline, not merged into it for tidiness.
  - **Consequences**: an implementation reading this field on a fresh reconnect or channel reopen
    should not treat the first Notify/Get response as immediately authoritative — per two
    independent sessions now, a stale reading can occur and self-corrects within roughly 1–2 seconds
    via a spontaneous re-Notify. A defensive implementation should either wait briefly for a
    possible correcting Notify before surfacing dock state in the UI, or treat a dock-state read
    taken within ~2s of a fresh (re)connection/reopen as provisional. Frame 3048's own
    no-connection-event failure mode has no known mitigation yet — it is recorded as a residual risk
    for any dock-state-dependent UI feature, not solved by this update. Maintainer-approved
    2026-09-18 (this chat session, continuing `ai-sessions/0031`/`0032`'s own open item).

- **Update (2026-09-20, `ai-sessions/0042`) — second confirmation, maintainer-approved in chat (2026-09-20, `AskUserQuestion` "Promoties": *"Dockstatus-byte (ADR-024) tweede bevestiging"*).**
  `CAP-059`, HCI + film: the connect-time `Notify` at 17:17:55.47 reads `01 e8 00 20` (both buds seated in the open case — film 17:17:53–17:19:03, `Settable-toggles` `0x00`) and the `Notify`
  at 17:19:22.31 reads `01 e8 e8 08` (both buds out since ≈ 17:19:11 — the case empty on film, `0xe8`); every later `Notify` (17 more) reads `e8`. The finding is unchanged; the app
  now shows it (a "Buds in the case / out" line, `AncFrame.Notify.settableToggles`: `0x00` → both in the case, `0xe8` → at least one out, anything else → not shown).

- **Update (2026-09-24, `ai-sessions/0045`, maintainer-approved in chat 2026-09-24) — the spec meaning of the byte, stated next to the behavioural reading.** Google's
  Hearable Controls page (fetched 2026-09-24) names byte 2 of `Notify ANC state` **"Settable toggles"**: *"Any or all of the UI toggle bits above may also be set here, to indicate
  which are currently enabled."* So the byte says which ANC modes the Buds currently let a Seeker switch; "both earbuds are in the case" is this project's **derived** reading of
  `0x00` (no mode switchable), 🟢 FACT as a correlation (the samples above) but with ≥ 5 documented counter-examples (`CAP-048` ×2, `CAP-047` ×3, `CAP-038`). The decision above is
  unchanged. **Consequences, now implemented:** the app words the line as a derived reading ("Both earbuds seem to be in the case — the Buds report no switchable ANC modes") and
  applies the 2026-09-18 consequence: a value received within ~2 s of a (re)open is shown as provisional, and during a claim's linger the last `Notify` wins.

- **Update (2026-09-24, `ai-sessions/0046`, maintainer-approved in chat 2026-09-24, `AskUserQuestion` "Dock line", option *"Remove the dock
  line (Recommended)"*):** `CAP-061` frame 5560 reads Settable `0x00` while one bud was out (film t ≈ 359.7 s; the Left bud was seated only
  0.6–0.8 s later) and the same claim's battery frame 5557 said that bud was not charging — a **premature** `0x00`, a 6th counter-example to the
  derived "both in the case" reading (`CAP-061-FINDINGS.md` §4). The finding (a correlation) stands; the app no longer shows a sentence derived
  from this byte. The per-earbud "(charging)" state on the battery lines stays.

## ADR-025 — Google Play Services (GMS) reverse-engineering is out of scope; DLCI 0x04/0x08 implementation proceeds clean-room, from wire evidence only

- **Date**: 2026-09-07
- **Status**: Accepted
- **Context**: `AUDIT_REPORT_2026-09-07.md` §1.0 found, via exhaustive full-tree string/identifier
  searches across the companion app's entire decompiled source (12,545 files), no trace of the Fast
  Pair Message Stream (DLCI 0x04) or DLCI 0x08's private-envelope transport logic anywhere in this
  app's own code — no `MessageStream`/`HearableControls`/ANC-opcode literals, no `"GSND"`/capability-
  string matches, no `Settable-toggles` parse site. The only ANC-adjacent code found is a downstream
  domain-model sink (`gck.java`/`gcl.java`/`eht.java`) that receives an already-decoded value from
  elsewhere and caches it locally in a Room/SQLite `device_info` table — strongly suggesting the
  actual transport (RFCOMM socket ownership, frame construction/parsing) for these two channels lives
  inside Google Play Services' own system-level Fast Pair/Nearby component, not in the companion
  app's own APK. That report explicitly deferred the resulting scope question, per `ADR-017`'s
  boundary (an AI session proposes, the maintainer decides): should this project bring GMS's own
  module into its reverse-engineering effort to close Q1–Q3 from the code side? A second, independent
  external review (`ANTIGRAVITY_AUDIT_REPORT_2026-09-07.md`, cross-validated in
  `EXTERNAL_REVIEW_VALIDATION_2026-09-07.md`) reached a similar-sounding conclusion but additionally
  mischaracterized it as an implementation-blocking "Impossibility" under the Zero-GMS rule — the
  validation pass found that framing contradicted by this project's own already-practiced
  architecture (see Consequences below). The maintainer has now made the underlying scope decision
  directly.
- **Options considered**:
  - Pull, decompile, and analyze the relevant Google Play Services module(s) the same way this
    project already treats the companion app, to locate DLCI 0x04/0x08's actual transport code.
  - Leave GMS out of scope; continue implementing DLCI 0x04/0x08 exclusively from wire-capture
    evidence (and, for DLCI 0x04, the official public Fast Pair specification) — exactly as this
    project already does today for every command confirmed so far (ANC, Find My Buds, EQ), none of
    which has ever required a companion-app code cross-reference to reach 🟢 FACT status.
- **Decision**: the second option. Google Play Services reverse-engineering is explicitly **out of
  scope** for this project, for reasons distinct from (and in addition to) `ADR-008`'s existing
  GMS-adjacent exclusions (Account Linking/Ownership Transfer/Accessory Non-Owner Service):
  1. **Legal/scale.** GMS is a much larger, actively-updated, closed-source system component, not
     "software the maintainer has personally installed for interoperability with hardware they own"
     in the same narrow sense `PROJECT_RULES.md` §8 rule 20 frames this project's existing APK
     analysis — decompiling it would be a materially different, larger undertaking than analyzing one
     companion app, with its own legal/scope questions this decision does not attempt to resolve.
  2. **Unnecessary.** This project's own evidentiary chain for DLCI 0x04 has never depended on
     companion-app code — every 🟢 FACT promotion for DLCI 0x04 traces to wire captures matched
     against the official Fast Pair spec, never an APK file+line (`PROTOCOL.md` §4.1). DLCI 0x08 is
     implemented the same way in principle (wire evidence + correlation, per `AGENTS.md` §13.6's
     zero-creativity rule) once its own Group/Code semantics are decoded. A decompiled reference was
     never the blocking dependency for either channel's own `FrameEncoder`/`FrameDecoder` work.
- **Consequences**:
  - DLCI 0x04/0x08 `FrameEncoder`/`FrameDecoder` implementation proceeds **clean-room, from wire
    capture evidence alone** (plus, for DLCI 0x04, the public Fast Pair spec) — exactly the same
    method already used for ANC (`ADR-009`), Find My Buds Left/Right (`ADR-011`), and EQ (`ADR-020`),
    none of which needed a companion-app code cross-reference to reach FACT/implementation-ready
    status. This is not a workaround forced by this decision — it is this project's proven,
    already-practiced method for exactly these kinds of channels; `ARCHITECTURE.md` is updated with a
    short note recording this explicitly.
  - `AUDIT_REPORT_2026-09-07.md` §1.0's Q1–Q3 "not found in this APK" results are treated as **closed
    from the code side** for this APK version — future work on DLCI 0x04/0x08 opcodes should not
    expect, or spend further effort searching for, a companion-app code citation for these two
    channels' own transport/framing.
  - `REVERSE_ENGINEERING.md`'s "Message Group / Code register" table is expected to remain empty for
    DLCI 0x04/0x08 specifically (its own header already scopes it to APK-derived values only) — a
    clarifying note is added there rather than leaving this looking like an oversight.
  - `PROJECT.md`'s non-goals gain a corresponding bullet citing this ADR. `TODO.md`'s Phase 2 section
    records this decision explicitly rather than leaving the question implicitly open.
- **Update (2026-09-08, maintainer sign-off via the chat session that authored prompt
  `ai-sessions/0002_MAINTENANCE_PROMPT_2026_09_08.md`, implementing
  `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 1/Phase 4's approved proposals) —
  GMS-boundary finding strengthened, not weakened.** A deeper Phase 1 search (structural
  AIDL/`ServiceConnection` pattern search plus a full manifest read, going beyond the original
  audit's literal-keyword grep) found that Google Play Services' Fast Pair module *is* reachable
  from this companion app's own decompiled code, via two genuinely named, unobfuscated AIDL
  interfaces (`com.google.android.libraries.bluetooth.fastpair.IFastPairDeviceDetailService`,
  `...fmd.IFastPairFmdProxyService`) bound through Google's Chimera dynamic-module broker
  (`com.google.android.gms.chimera.GmsBoundBrokerService`). **This does not weaken this ADR's
  decision or its "DLCI 0x04/0x08 transport code is absent from this APK" evidentiary basis — it
  strengthens it.** The newly-found boundary carries only already-decoded objects (a
  `TrueWirelessHeadset` battery summary; an `FmdRequest`/`FmdResponse` consent-flow pair) — not raw
  Message-Stream/private-envelope frame bytes — and an exhaustive sweep of every
  `queryLocalInterface(...)` call in this APK version (31 total) found no third, ANC/settings-shaped
  GMS interface. The original conclusion — that DLCI 0x04/0x08's actual frame
  construction/parsing lives inside GMS itself, not this companion app — is now supported by a
  *positive* architectural finding (a concrete, named, working example of exactly this kind of
  higher-level GMS boundary existing and being reachable) in addition to the original *negative* one
  (no transport code found). **No change to this ADR's Decision or Consequences sections** — this
  Update records the strengthening finding per `PROJECT_RULES.md` §3's non-destructive-update
  convention. See `REVERSE_ENGINEERING.md`'s `ijk`/`ijp`/`TrueWirelessHeadset`/`FmdWorker` entry and
  `PROTOCOL.md` §6 for the full trace.
- **Update (2026-09-08, same maintainer sign-off) — `qhr` fields 11 (Multipoint) and 15 (Volume EQ)
  promoted to 🟢 FACT for full field-number/semantic identity.** Applying `DECISIONS.md` ADR-019's
  same static-analysis method (a forward trace from a named UI fragment/preference key to the write
  call site, rather than the log-message-backward technique used for ADR-019's own fields) to the
  two fields explicitly flagged as still-unchecked in `TODO.md`'s "Targeted research follow-ups":
  **field 11 = "Multipoint"** (`MultipointFragment`'s `key_multipoint_main_toggle` toggle →
  `hiy.java:32`'s self-describing `"Set device Multipoint as: %s"` log → `fyo.java:146-166`) and
  **field 15 = "Volume EQ"** (`hlv.java:2127`'s self-describing `"Set volume eq: %s"` log, gated on
  the literal Android preference-key string `"volume_eq_switch"` → `fyo.java:376-396`) — both
  readings match the pre-existing wire-derived HYPOTHESIS labels exactly, with no naming-equivalence
  gap of the kind that kept fields 12/22/27 at field-number-only status. See `PROTOCOL.md`
  §4.5.2/§4.5.6 and `REVERSE_ENGINEERING.md`'s `qhr` entry (2026-09-08 update) for the full evidence.
- **Update (2026-09-24, `ai-sessions/0045`, maintainer-approved in chat 2026-09-24) — wording correction, decision unchanged.** The title and the first Consequence say
  DLCI 0x04/0x08 work proceeds "clean-room". That word is wrong for this project: `AGENTS.md` §12 bans the clean-room claim and ADR-002's 2026-08-15 Update retired it,
  because this project decompiles the official APK. Read it as: **"an independent implementation, from wire-capture evidence (and, for DLCI 0x04, the public Fast Pair
  specification) only — no companion-app or GMS code is used as a reference for these two channels."** The decision (GMS reverse-engineering out of scope) is unchanged; the
  title is left as written (history), and `PROJECT.md`, `ARCHITECTURE.md` §5 and `TODO.md` use the corrected wording.

## ADR-026 — Volume Balance (`qhr` field 17) range and Left/Right polarity confirmed: ±100, `+100`=Left, `-100`=Right

- **Date**: 2026-09-13
- **Status**: Accepted
- **Context**: `DECISIONS.md` ADR-019 already promoted `field 17`'s field-number/semantic identity
  ("Volume balance") to 🟢 FACT, explicitly leaving the numeric scale/range and which direction
  (Left/Right) corresponds to negative vs. positive values open (`PROTOCOL.md` §4.5.7/§6). `CAP-046`
  (Group AK, 2026-09-12) ran a dedicated isolated-extreme-position capture specifically to close
  this gap, and `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` Finding 125 independently
  re-derived its wire-side values with an exact byte-level match on all 8 samples. This proposal was
  drafted in `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phase 4 and explicitly approved by the
  maintainer in the same chat session that authored that prompt.
- **Finding being recorded**: `field 17`'s range clamps at exactly ±100. `field17 = +100`
  corresponds to the Volume Balance slider's Left extreme; `field17 = -100` corresponds to the
  Right extreme — the opposite of this project's own earlier, unstated assumption
  (`CAP-022-FINDINGS.md` §5 implicitly labeled its first negative sample "Left"). Evidence: 3 of 3
  extreme-position samples video-confirmed (`CAP-046-FINDINGS.md` §2), zero counter-examples,
  independently re-derived at the wire level a second time (`0012` Finding 125, exact byte-level
  match on all 8 samples).
- **What this ADR does NOT clear**: whether `field17` scales linearly (or at all) between center
  and the ±100 extremes — untested, no intermediate-position sample exists in any capture to date
  (`CAP-046-FINDINGS.md` §4/§7). The `field17`/`field19` (Mono audio) timing correlation
  `CAP-046-FINDINGS.md` §3 also found is not covered by this ADR and stays 🟡 HYPOTHESIS.
- **Decision**: the range (±100) and Left/Right polarity (`+100`=Left, `-100`=Right) above are
  accepted as 🟢 FACT.
- **Consequences**: `PROTOCOL.md` §4.5.7 and §6's matching open item are updated to record the
  range/polarity as 🟢 FACT. A future EQ/Volume-Balance UI implementation can render the slider's
  Left/Right mapping without hedging — but should still clamp/interpolate defensively for
  intermediate positions, since linearity there remains unconfirmed and is not settled by this ADR.

## ADR-027 — Find My Buds Case/"both simultaneously": ship v1 with Left/Right ring only, no local fallback

- **Date**: 2026-09-13
- **Status**: Accepted
- **Context**: `PROTOCOL.md` §4.4's "Major structural finding" and §6 Behavior's matching open item
  established that Case ring and "ring both simultaneously" are reachable, in the official app, only
  via a separate Find Hub/Find My Device map-view flow that is account/cloud-mediated
  (video-confirmed "Connecting…" state, on-screen copy referencing "another device linked with your
  Google Account"). `PROTOCOL.md` §4.4 confirms a checked negative — **zero** local
  `Group 0x04 Code 0x01` (Ring) traffic occurs while this flow is active, across a ~2.5-minute
  observation window — and a later code-level trace
  (`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` Phase 3) found the companion app's own code
  constructs Find My Device Terms-of-Service accept/skip requests only, with no ring/play-sound
  trigger anywhere in its own decompiled source. `TODO.md` and
  `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` Phase 5 both already framed this as "a genuine
  Zero-GMS scope trade-off... no capture or static analysis can resolve this, only a maintainer
  product decision can." The maintainer made that decision directly in the chat session that
  authored `ai-sessions/0013_FEATURE_PROMPT_2026_09_13.md`.
- **Options considered**:
  - Accept a Google Find Hub/account-mediated fallback for Case/"both" ring specifically — rejected:
    this would require a GMS/Google-account dependency, exactly what this project's Zero-GMS goal
    (`AGENTS.md` §1, `PROJECT.md` non-goals) exists to avoid, for one sub-feature whose local wire
    mechanism this project has already checked and found does not exist.
  - Ship v1 without local Case/"both" ring support, documenting it as a permanent, explicit
    limitation — chosen.
- **Decision**: v1 ships with **Left/Right Find My Buds ring only** (already 🟢 FACT and
  implementation-unblocked, `DECISIONS.md` ADR-011). Case ring and "ring both simultaneously" are
  explicitly **out of scope** for this project, unless a future capture or protocol change finds a
  genuine local (non-GMS-mediated) mechanism — no such evidence exists today.
- **Consequences**: `PROJECT.md`'s non-goals gain a corresponding bullet citing this ADR.
  `TODO.md`'s Phase 1 open item and `PROTOCOL.md` §4.4/§6 Behavior are updated to record this as a
  closed scope decision rather than an open research question. `:app`'s eventual Find My Buds UI
  screen should offer Left/Right controls only, with no "Case"/"both" affordance implying a
  capability this project does not provide.
- **Update (2026-09-24, `ai-sessions/0045`, maintainer-approved in chat 2026-09-24):** the premise above
  ("no local mechanism exists") is narrowed: the official app routes Case/"both" through Find Hub, but
  Google's Device Action spec (`…/extensions/deviceaction`, fetched 2026-09-24) defines Ring value `0x03` =
  "ring both left and right" on the local Message Stream — never sent by this project, 🔴 untested on this
  firmware. The Case has no spec value. The v1 scope decision (Left/Right only) is unchanged; a test is
  tracked as `FIND-004` in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`/`TODO.md`. Sending `0x03` needs its own ADR.

## ADR-028 — Dependency injection: Hilt

- **Date**: 2026-09-13
- **Status**: Accepted
- **Context**: `ARCHITECTURE.md` §10/§15 left dependency injection as an open architecture question
  between Hilt/Dagger and a manual, light service locator — `AGENTS.md` §1 already clarifies Hilt
  itself does not touch `com.google.android.gms.*` and does not itself require Google Play Services,
  so it is not disqualified by the Zero-GMS rule on that basis alone. `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md`
  Phase 6 surfaced this to the maintainer, recommending Hilt (this project's own module graph — `:app`
  as composition root wiring four other modules together, `ARCHITECTURE.md` §2 — is exactly the shape
  Hilt/Dagger's compile-time DI is built to reduce boilerplate for). The maintainer approved this
  recommendation directly in the chat session that authored `ai-sessions/0013_FEATURE_PROMPT_2026_09_13.md`.
- **Options considered**:
  - **Hilt/Dagger** — chosen. Mature, widely-used, AndroidX-adjacent, compile-time DI; reduces
    `:app`'s own composition-root boilerplate across `:domain`/`:data`/`:hardware`/`:ui`.
  - **Manual service locator** — full independence from Google-authored build tooling, at the cost
    of hand-written wiring code across all five modules; rejected as a reasonable but non-preferred
    alternative given the maintainer's own priorities favor less boilerplate.
- **Decision**: Hilt is this project's dependency-injection framework, per `AGENTS.md` §10's
  dependency policy (pinned version, justified, no network/analytics SDK bundled transitively —
  confirmed via `./gradlew :app:dependencies`, see `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md`'s
  Hilt-wiring update for the check).
- **Consequences**: `:app` becomes a real Hilt composition root (`@HiltAndroidApp`
  `Application`/`@AndroidEntryPoint` `MainActivity`, `@Module`/`@InstallIn` bindings for
  `BudsTransport`/`BudsRepository`). `ARCHITECTURE.md` §10/§15 updated to record this as decided, not
  open. Every future module needing a dependency graph entry uses Hilt's `@Inject`/`@Provides`
  conventions rather than a hand-rolled locator.

## ADR-029 — Minimum supported Android API level: API 34 (Android 14)

- **Date**: 2026-09-13
- **Status**: Accepted
- **Context**: `ARCHITECTURE.md` §15 left the minimum supported Android API level open — compile/
  target SDK was already fixed at API 34 (Android 14), but how far down the minimum should go for
  broader AOSP-ROM compatibility was undecided. The open item itself noted this wasn't blocked on a
  single API: the generic battery broadcast (`ARCHITECTURE.md` §4 option 0) needs API 31+, but the
  confirmed primary battery/ANC/EQ paths (Fast Pair advertisement, Message Stream, HFP, GATT) don't
  depend on it, and `CompanionDeviceManager` (`DECISIONS.md` ADR-005) only needs API 26. The
  maintainer decided directly, in conversation, that the minimum should simply match the
  already-fixed compile/target SDK rather than support a wider, lower floor.
- **Options considered**:
  - A lower minimum (e.g. API 26, the floor `CompanionDeviceManager` itself needs) for broader
    AOSP-ROM/older-device compatibility, accepting that API 31's generic battery broadcast (a cheap
    supplementary check, not a required mechanism) degrades gracefully below that version.
  - **API 34 (Android 14), matching compile/target SDK exactly** — chosen. Simplest option: no
    version-gated code paths anywhere in the app, and this project's primary reference platform
    (GrapheneOS, `ARCHITECTURE.md` §1) tracks current Android releases closely, so a lower floor
    buys little real compatibility benefit for this project's actual user base.
- **Decision**: minimum supported Android API level is **34 (Android 14)**, identical to
  compile/target SDK. No lower-API compatibility path is pursued.
- **Consequences**: `ARCHITECTURE.md` §1/§15 updated to record this as decided, not open. The
  Android Gradle project's `minSdk` is set to 34 across every module that declares one (`:app`,
  `:hardware`, `:ui`) — simpler than the API-26 floor used provisionally before this decision, since
  every Bluetooth/battery mechanism this project relies on is available well below API 34 anyway.
  This forecloses running on older Android versions/ROMs that can't be updated past API 33, a
  deliberate trade-off given this project's GrapheneOS-first target.

## ADR-030 — Cross-Transport Key Derivation (CTKD) confirmed as a third bonding path, gated on a pre-existing LE link

- **Date**: 2026-09-18
- **Status**: Accepted
- **Context**: `CAP-004-FINDINGS.md` §2 first observed a bonding path where an LE Secure Connections
  link to the Buds already existed before classic pairing began (nRF Connect): `Delete Stored Link
  Key` → SMP `Pairing Request` (`Linkkey` distribution) → Public Key/Confirm/Random → `DHKey Check`
  → classic `Create Connection` → `Link Key Request Reply` — the classic link key derived from the
  LE pairing rather than negotiated via classic SSP. `CAP-004-FINDINGS.md` §10 withheld this from
  promotion: the finding rested on one capture with a specific confound (nRF Connect's early BLE
  connection might itself cause CTKD, independent of the GMS-disabled/no-app condition actually
  being tested). `CAP-012` (2026-08-26) directly tested this as a controlled hypothesis: repeating
  the same GMS-disabled/no-app condition with **no** BLE tool at any point, and independently
  confirming zero BLE connection to the Buds anywhere in that session's log, produced classic SSP
  instead of CTKD. Combined with `CAP-002`/`CAP-003` (classic SSP in every session with no
  pre-existing LE link) and `CAP-014`/`CAP-015` (2026-08-27, a second, independently confirming CTKD
  instance, again initiated by nRF Connect connecting first), this is a direct causal isolation, not
  merely a repeated negative. Maintainer approved promotion 2026-09-18 (`ai-sessions/0031`, Phase 4).
- **Finding being promoted**: "An LE Secure Connections link already existing to the Buds before
  classic pairing begins gates Cross-Transport Key Derivation (CTKD) instead of classic Secure
  Simple Pairing (SSP)" — `PROTOCOL.md` §5.1's "Third path" note — is now 🟢 **FACT**.
- **What this ADR does NOT promote**: why a BLE tool connecting first matters, or whether the
  official companion app itself ever triggers this path (every confirmed instance used nRF Connect,
  not the official app) — both remain open questions. This is not a per-DLCI
  `FrameEncoder`/`FrameDecoder` content-decoding gate under `AGENTS.md` §6's specific
  implementation-gate clause (CTKD is bonding/pairing-layer behavior, not a Message-Group/Code
  frame) — recorded as its own ADR per this project's general convention of an ADR per FACT
  promotion, not because §6's narrower implementation-gate rule requires one here.
- **Decision**: `PROTOCOL.md` §5.1's CTKD-gating claim is promoted to 🟢 FACT.
- **Consequences**: `ARCHITECTURE.md`'s pairing/bonding logic (`CompanionDeviceManager`-based
  first-time pairing, `AGENTS.md` §7) can now assume both bonding paths converge to the same
  encrypted classic link regardless of entry point, so reconnection logic doesn't need to
  special-case which path was used. Does not itself unblock any new command implementation.

## ADR-031 — Battery Option B's `Group 0x03 Code 0x03` message confirmed as the Fast Pair "Battery updated" notification (while discharging)

- **Date**: 2026-09-18
- **Status**: Accepted
- **Context**: `PROTOCOL.md` §4.3 Option B tracked a candidate battery message on DLCI 0x04
  (`03 03 00 03 <b1> <b2> ff`), originally recorded as 🟡 HYPOTHESIS per maintainer sign-off
  (`CAP-009-FINDINGS.md` §7). The 2026-08-28 project-wide audit (`EXT-01`) found Google's official
  Fast Pair Device Information extension spec documents Message Group `0x03` Code `0x03` =
  **"Battery updated"** — an exact match, independently derived from `CAP-009`'s own wire behavior.
  Across 208 occurrences in a 101-minute natural-discharge session (`CAP-009`), `b2` matched the
  Right earbud's percentage at all 7 of its transitions, and `b1` matched Left's percentage at both
  of its transitions while not charging, with both fields updating within single-digit milliseconds
  of the already-FACT `AT+BIEV` (Option C) and DLCI-0x08 Option E pushes for the same underlying
  change. Maintainer approved 2026-09-18 (`ai-sessions/0031`, Phase 4) accepting this single-session
  evidence combined with the exact spec-code match as sufficient, without requiring the
  independently-reproducing session originally proposed.
- **Finding being promoted**: DLCI 0x04's `Group 0x03 Code 0x03` message is the Fast Pair "Battery
  updated" notification, encoding per-earbud battery percentage in `b1` (Left) / `b2` (Right)
  **while discharging** — `PROTOCOL.md` §4.3 Option B's candidate code identity is now 🟢 **FACT**.
- **What this ADR does NOT promote**: the charging-state field-switch anomaly — once the Left
  earbud starts charging, `b1` stops behaving like a percentage (jumps to 221, climbs ~1/sample
  instead of following the known 93→100 charging curve) — remains 🟡 HYPOTHESIS/unexplained, a
  distinct regime not covered by this promotion. Whether the `Group`/`Code` numbering itself is
  stable across sessions (the way DLCI 0x08's Option E numbering has proven to be) or is
  session-dynamic is also not independently re-verified here.
- **Decision**: `PROTOCOL.md` §4.3 Option B's candidate battery message code identity is promoted to
  🟢 FACT for the discharging case specifically.
- **Consequences**: `ARCHITECTURE.md` §4's battery-fallback priority order can cite Option B (Fast
  Pair Message Stream Battery notification) as a second confirmed, event-driven mechanism alongside
  Option C (HFP) — but any implementation must treat `b1`'s value as unreliable/unknown while the
  Left earbud is reported charging, until the regime-change is separately investigated. Does not
  change the existing HFP-first priority ordering.
- **Update (2026-09-20, `ai-sessions/0041`) — the "charging-state field-switch anomaly" is resolved.** The
  maintainer accepted ADR-033's charging-flag proposal (see ADR-033's update): the anomaly was not a regime
  change but the official Fast Pair encoding `0bSVVVVVVV` (bit 7 = charging, low 7 bits = level, `0x7F` =
  unknown). `PROTOCOL.md` §4.3 Option B now records it as 🟢 FACT. The "unreliable while charging" caution in
  the *Consequences* above no longer applies.
- **Update (2026-09-24, `ai-sessions/0045`):** "Does not change the existing HFP-first priority ordering" is superseded by ADR-040:
  HFP is not an app source; Option B (Left/Right) and Option E (Case, ADR-035/039) are the implemented battery sources.

## ADR-032 — DLCI 0x04 (Message Stream) is a shared, on-demand channel: opened by the user's own ANC/Find/Connect action and released shortly after; loss of it is not a session loss

- **Date**: 2026-09-19
- **Status**: Accepted
- **Note on process**: drafted by an AI agent; the decision itself is the maintainer's, made explicitly in the
  chat session of 2026-09-19 that followed `ai-sessions/0039` ("ik wil dat een ANC- of Find-tik het kanaal zelf
  claimt", after choosing this over per-channel tolerance/manual take-over), together with an explicit
  retraction of the earlier "never connect automatically, not even on an ANC/EQ change" instruction — the
  maintainer's reason: that instruction assumed the app dropped the *Bluetooth connection to the Buds*, whereas
  what is actually lost is only one RFCOMM *channel*. Recorded here so the provenance is auditable
  (`AGENTS.md` §6). The *details* below marked "agent detail" are the agent's implementation proposals inside
  that decision, for the maintainer to veto.
- **Context**: `ai-sessions/0039`/`0040` (system-log evidence from three real-hardware test rounds): Android allows
  one RFCOMM connection per (device, channel) across all apps, refuses a second with `already at opened
  state` and its failure path closes the incumbent's port too. Google Play services' Fast Pair event stream
  holds the Message Stream channel (DLCI 0x04) and re-opens it 2.7–5.0 s after losing it (three observed recoveries). With Play services'
  Nearby-devices permission allowed, this app therefore holds DLCI 0x04 for a median of 4.5 s (20 of 20
  sessions ended on 0x04; 0.07–4.7 s). DLCI 0x02 (MAESTRO) was never contested by another app in any log.
  While the app holds the channel, commands work (ACK and Notify frames arrive within tens of ms).
- **Options considered**: (a) keep both channels as one all-or-nothing unit (status quo — flickers every ~5 s
  on a phone with Play services' Fast Pair active); (b) per-channel tolerance with a "degraded" state and
  manual retry; (c) (b) plus explicit take-over/release buttons; (d) this decision — claim on demand.
- **Decision**:
  1. The **session** is the MAESTRO channel (DLCI 0x02). `Connect` opens it; `ConnectionState.Ready` means
     "the MAESTRO channel is open". Loss of DLCI 0x02 is a session loss (`Disconnected`), as before.
  2. **DLCI 0x04 is claimed on demand**, by the user's own action that needs it: an ANC mode tap, an ANC
     Refresh, a Find My Buds ring/stop tap, and (agent detail) once at `Connect` to take an initial ANC/battery
     snapshot. No background/automatic claim, no periodic re-claim, no reconnect loop (`ARCHITECTURE.md` §6's
     "user-initiated only" is kept — the trigger is always a user tap).
  3. After the action completes the channel is **released after a short linger** (agent detail: 1.5 s) so
     Google Play services can reclaim it and hold it stably; a claim is short, not held. A claim that finds the
     channel busy is retried a bounded number of times within that one action (as `ai-sessions/0039` §3 fix 6).
  4. **Loss of DLCI 0x04 is not a session loss**: it does not change `ConnectionState`; it is logged, and the
     next action claims again. A failed claim is reported per action with its reason (`ChannelUnavailable`).
  5. Values learned from DLCI 0x04 (ANC mode, battery) are **only as fresh as the last claim**; the UI treats
     them as "last known", and each claim re-queries ANC (`Get`, `PROTOCOL.md` §4.1, ADR-021/022).
- **What this does NOT settle / known limits**: (i) it does not remove Play services' contention — each claim
  closes Play services' socket for its duration (unavoidable stack behaviour), which may briefly disturb
  Android's own ANC controls; (ii) whether Find My Buds ringing continues after the Message Stream socket is
  released is **unverified on hardware** (🟡 HYPOTHESIS that it does) — if it does not, the Find flow needs a
  longer hold (a constant); (iii) 2 of 20 observed windows were < 1 s, so an action can occasionally fail and
  must say so; (iv) EQ is unaffected (DLCI 0x02); (v) nothing here is hardware-verified.
- **Consequences**: `BudsTransport` gains per-channel open/close and a distinct "on-demand channel closed"
  signal; `BudsRepositoryImpl` orchestrates claim → act → linger → release under one mutex;
  `ARCHITECTURE.md` §2.1/§6.0b updated. `ConnectionState` itself is unchanged. This supersedes nothing in
  `DECISIONS.md`; it refines `ARCHITECTURE.md` §6.0b (which had recorded per-channel tolerance as "evaluated,
  not adopted") by replacing the all-or-nothing rule for the Message Stream channel only.
- **Update (2026-09-20, `ai-sessions/0041`) — first hardware results for this ADR (maintainer-observed, real
  hardware, 2026-09-19; no captured evidence — recorded as observations, not as FACT).** With Play services'
  Nearby-devices permission allowed and the app already set up: (i) once *Connect* was tapped the session
  **stayed** connected (no ~5 s flicker), i.e. the MAESTRO-only session model works; (ii) ANC switching works and
  is audible; (iii) Find My Buds works and the sound **keeps repeating until Stop is pressed** — this answers the
  open question in *What this does NOT settle* (ii): ringing **does** continue after the Message Stream socket is
  released, so the 1.5 s linger is sufficient for Find. Still unverified: limit (iii) (occasional short windows).

## ADR-033 — Battery Option B: `Group 0x03 Code 0x03` decoder on DLCI 0x04 unblocked for implementation (percentage regime only)

- **Date**: 2026-09-19
- **Status**: Accepted (implementation unblock, scope below); the charging-flag reading is a separate,
  explicitly **unaccepted proposal** — see "Proposal awaiting sign-off"
- **Note on process**: drafted by an AI agent; the unblock is the maintainer's explicit instruction in the
  chat session of 2026-09-19 ("een ADR vrijgeven voor de batterijdecoder op 0x04"), per `AGENTS.md` §6/
  `ARCHITECTURE.md` §5a's requirement that a command's implementation-unblock be stated in an ADR.
- **Context**: ADR-031 promoted the *identity* of DLCI 0x04 `Group 0x03 Code 0x03` (Fast Pair "Battery
  updated") to FACT for the discharging case but, unlike ADR-009/011/020, never stated an implementation
  unblock, so `ARCHITECTURE.md` §5a kept it gated. Meanwhile the Buds push these frames in the maintainer's
  own app logs: three immediately after DLCI 0x04 opens and again on every change (`03 03 00 03 <b1> <b2> ff`).
  The app's HFP battery route shows "Battery unavailable" on the maintainer's phone (`ai-sessions/0040` §4), and
  ADR-032 makes short DLCI 0x04 claims the app's model — a short claim receives this connect-time burst.
- **Decision**: implementing a `FrameDecoder` for this message is unblocked, restricted to what ADR-031 already
  promoted: `Value` = 3 bytes `[b1, b2, b3]`; `b1` = Left earbud, `b2` = Right earbud; **a byte in `0..100` is that
  earbud's percentage** (`isCharging` stays unknown — never fabricated, `BatteryLevel`'s own contract); **any
  other byte value is not interpreted** and the earbud stays "Battery unavailable" (`AGENTS.md` §5: never
  guess); `b3` (observed `0xff`) is not interpreted and the Case stays unavailable.
- **Proposal awaiting sign-off (NOT accepted by this ADR; `AGENTS.md` §6)**: bytes with bit 7 set are the
  official Fast Pair encoding `0bSVVVVVVV` (S = charging, V = 0–100 %, `0x7F` = unknown) documented in
  `PROTOCOL.md` §4.3 Option A, and `b3 = 0xff` means "unknown". Evidence: `CAP-009-FINDINGS.md` §4/§6 — after
  the Left earbud entered the case, `b1` went 221→222→223→224→225→226→228 (frames 26852…28563) while Option E
  (independent DLCI 0x08 message, FACT per ADR-014) reported that earbud climbing 93→94→95→96→97→98→100:
  `b1 − 128` reproduces that sequence step for step, including the skipped 99. The same session's `AT+BIEV`/
  Option E cross-checks are unaffected. This reading would explain ADR-031's "charging-state field-switch
  anomaly" as a decoded flag rather than a regime change. If the maintainer accepts it, the decoder can show a
  charging state and a level while charging, and the case byte; until then those regimes read as unavailable.
- **Consequences**: `:data` gains a Battery frame decoder, routed through `CodecRouter`, feeding
  `BatteryStatus.left`/`right`; `ARCHITECTURE.md` §5a's row for Battery Option B moves from "gated" to
  "implemented"; the HFP `hfpEarbud` field stays as-is. Nothing else in DLCI 0x04 Group 0x03 is unblocked.
- **Update (2026-09-20, `ai-sessions/0041`) — the charging-flag proposal is ACCEPTED by the maintainer** (prompt
  `0041` §1.1, and re-confirmed by the maintainer in the chat session itself, 2026-09-20; explicit approval per
  `AGENTS.md` §6). Accepted reading: each of `b1` (Left) / `b2` (Right) is `0bSVVVVVVV` — `S` (bit 7) = charging, `V`
  (low 7 bits) = level `0..100`, `V = 0x7F` = unknown; `b3 = 0xff` = unknown. Evidence (rule 4a — command and raw
  bytes): `tshark -r CAP-009-btsnoop_hci.log -Y "btrfcomm.dlci==4 && frame.number>=26800 && frame.number<=28600 &&
  data.data[0:4]==03:03:00:03" -T fields -e frame.number -e data.data` gives `b1` = `dd de df e0 e1 e2 e4` at frames
  26852, 26907, 27020, 27195, 27377, 27581, 28563 (221 … 228) with `b2` constantly `58`; `b1 − 128` = 93 94 95 96 97
  98 100, the Option E (DLCI 0x08) charging curve for the same earbud, including the skipped 99. Frame 1044 of the same
  file, `03 03 00 03 60 5d ff`, is 96 % / 93 % not charging. **Scope of the acceptance:** it does **not** cover
  decoding `b3` as the Case battery beyond "`0xff` = unknown"; the Case level stays "unavailable" here (the DLCI 0x08
  Option E message, ADR-014, is the FACT source for the Case and is a separate, unapproved proposal). The
  decoder is updated accordingly (`BatteryFrameDecoder`) and the Connection screen shows the charging state.

## ADR-034 — DLCI 0x02 is pw_rpc `maestro_pw.Maestro`; a read-only `ReadSetting` path is unblocked (EQ fields 16/18); requests mirror the channel the Buds announce

- **Date**: 2026-09-20
- **Status**: Accepted (scope below)
- **Note on process**: drafted by an AI agent (`ai-sessions/0041`); the two FACT promotions, the read-path unblock and the
  channel-mirroring rule are the maintainer's explicit approval — given in prompt `0041` §1.2 ("agrees to promote the two
  `pw_rpc` proposals in `DESKRESEARCH_FINDINGS.md` (2026-09-19, proposals a and b) and to an ADR unblocking a `ReadSetting` read
  path on DLCI 0x02") and re-confirmed by the maintainer in the chat session itself on 2026-09-20 (all three items, including
  the channel-mirroring rule below), per `AGENTS.md` §6/§15. The prompt made the promotion subject to the evidence rule, so the
  evidence check below was done **first**. Details marked "agent proposal" are the agent's, inside that approval, open to veto.
- **Context**: `DESKRESEARCH_FINDINGS.md` (2026-09-19) found that DLCI 0x02's constant frame prefix is a Pigweed pw_rpc
  `RpcPacket` header and that the official app reads every setting with `ReadSetting` at connect (`4:16` live EQ, `4:18` last
  saved). It listed four open items; item 4 was "independent confirmation on a second capture". ADR-013/ADR-020 unblocked only
  the generic write wrapper and the EQ write. The app's EQ writes were inaudible on the maintainer's phone (`ai-sessions/0041` §2).
- **Evidence check (2026-09-20; commands and raw output in `DESKRESEARCH_FINDINGS.md`, entry 2026-09-20)**:
  1. *Framing correction.* Every pw_hdlc frame in `CAP-015` has control byte `0x03` (304/304) and its address is a one-terminated
     varint (`00 3b`, `80 a3`, `00 a5`, …). The `03` that `EqFrameEncoder` treated as the first payload byte is the HDLC control
     byte, and the byte after `10` in `03 10 <n> 1d…` — which the codec called the "correlation byte" — is the RpcPacket
     `channel_id` (tag `0x10`). The bytes on the wire were always right; the *names* were wrong, and `EqFrame.correlationByte`
     defaulted to `0`, i.e. **channel 0**. Every capture uses channel 19, 21, 24 or 26.
  2. *`ReadSetting` returns the current value — second-capture confirmation.* Across 52 captures, connect-time reads agree with the
     **last write of the preceding capture** in three independent chains: `CAP-005` (frames 1321/1338 wrote `[5.0,−4.1,0,0,0]` to
     fields 16 and 18) → `CAP-006` first reads (frames 936/974) `[5.0,−4.1,0,0,0]`; `CAP-015` (last writes `[0.1,0,0.3,0.2,0.2]`) →
     `CAP-016` (frames 1945/1963) the same; `CAP-041` (last writes `[−6]*5`) → `CAP-042` (frames 897/903) `[−6]*5` — each a different
     btsnoop file and connection (005→006 also a different `channel_id`, 19 → 21). Field 16 also equals a named preset
     independently recorded from the official app's UI: `[−1,0,4,2,0]` = "Vocal boost" (`PROTOCOL.md` §4.2's preset table) in
     15 consecutive captures on 2026-09-12…14 while field 18 stays at the last custom curve `[−6]*5` — 16 = the active EQ, 18 = the
     last-saved custom EQ. **Not** verified: the value against a *screen recording*; only the cross-capture chains and the preset name.
  3. *Channel/address rule.* In 43 of 43 captures that contain both, the channel of the Buds' first unsolicited `GetSoftwareInfo`
     RESPONSE (`call_id 0xFFFFFFFF`) equals the channel most used by the phone's Maestro requests. Channel↔address pairs are constant:
     19 ↔ request `00 3b` / response `80 a3`; 21 ↔ `00 4b` / `00 a5`; 24 ↔ `80 3d` / `80 d3`; 26 ↔ `80 4d` / `00 d5`.
- **Decision**:
  1. **Promoted to 🟢 FACT** (`PROTOCOL.md` §2.2a, §4.2): (a) DLCI 0x02 carries Pigweed pw_hdlc frames (control `0x03`, one-terminated
     varint address) whose payload is a pw_rpc `RpcPacket` for service `maestro_pw.Maestro` (service/method ids equal the
     65599 name hashes byte for byte); (b) `ReadSetting` with request payload `4:N` returns the current value of `qhr` field N as
     `4:{N: …}` — for the EQ, N = 16 is the active quintet and N = 18 the last-saved custom quintet.
  2. **A read-only `ReadSetting` path on DLCI 0x02 is unblocked** for `qhr` fields already at FACT identity, **EQ (16, 18) first**.
     Nothing else is unblocked: no `SubscribeToSettingsChanges`, no other setting's read or write, no unnamed services
     (`0x73d5d805`, `0xaf3a7737`, …).
  3. **Channel/address selection for our own requests** (maintainer-approved 2026-09-20; the table is the agent's evidence-derived
     detail): the client uses the `channel_id` of the Buds' first unsolicited `GetSoftwareInfo` packet of *this* connection and the
     request address paired with it in the table above; for a channel not in the table it sends nothing and reports the specific
     reason — it never guesses an address or a channel.
  4. **Still open, handled conservatively** (agent proposal; each `// TODO(verify)` + a hardware re-test step): *what a fresh client
     must send first* (the app sends nothing before its first read; if the Buds do not answer, the read fails with a reason — no retry
     storm); *request/response matching* (responses omit `call_id`; the response's own `4:{N:…}` field identifies which read it
     answers, and reads are issued one at a time); *whether writes are accepted with the mirrored channel* (the write's unary RESPONSE
     status is surfaced; `CLIENT_ERROR`/`SERVER_ERROR`/`status ≠ OK` is shown to the user).
- **Consequences**: `:data` gains a pw_rpc packet codec and a Maestro settings-read path; `EqFrame` carries `channelId` instead of the
  misnamed correlation byte; the EQ screen shows the real EQ and only claims "unknown" when the read failed (with the reason). The
  always-on log gains payload-free pw_rpc packet-structure lines (type, channel, method, status) so a rejected write is visible next
  time. `ARCHITECTURE.md` §3.1/§5a are updated. **Not decided here:** the field-16-vs-18 *write* semantics (ADR-020's open item stands),
  the gain unit, and any other DLCI 0x02 setting (`ARCHITECTURE.md` §5a still lists a consolidated unblock ADR as a proposal).

- **Update (2026-09-20, `ai-sessions/0042`) — two of the four open items are promoted to 🟢 FACT by the maintainer's explicit approval in the chat session
  (2026-09-20, `AskUserQuestion` "Promoties": *"ReadSetting: geen openingsbericht nodig (4/4)"* and *"Write-ack: lege RESPONSE, status OK (12/12) + persisteert"*),
  per `AGENTS.md` §6.** Evidence (`CAP-059-FINDINGS.md` §6; `python3 scripts/pwrpc_decode.py <log>` on the `CAP-059` HCI log,
  kept locally): (a) **a fresh client needs no opening message** — in 4 of 4 connections the first thing the app sent on DLCI 0x02 was `ReadSetting 4:16`
  (frames 1444, 3197, 3522, 4848), after the Buds' unsolicited `GetSoftwareInfo` (22, 102, 30 and 58 ms after the `UA`), and each was answered (frames 1455, 3202, 3558, 4855);
  (b) **write acknowledgement** — 12 of 12 `WriteSetting` requests on the mirrored channel (ch 21 ×5, ch 19 ×7) were answered by an empty `RESPONSE` with status absent/OK
  (e.g. frames 2169→2171, 4924→4927), no `CLIENT_ERROR`/`SERVER_ERROR`/`status ≠ OK` in 36 packets, and **the next connection's `ReadSetting` returned the last write**
  (`[-2.00, 0, 2.00, 3.00, 5.00]`, frames 3202, 3558, 4855) — the Buds store what they acknowledge. **Still open, unchanged:** request/response matching for *concurrent*
  requests (the app issues them one at a time), how the channel→address mapping is derived (only tabulated: 21 and 19 seen again, no new pair), and whether an EQ change is
  *audible* (a camera microphone cannot record what is in the ears — the maintainer's listening test).

## ADR-035 — DLCI 0x08 is claimed on demand for the Case battery: `Group 0x0e Code 0x01`, entry index 3; receive-only

- **Date**: 2026-09-20
- **Status**: Accepted (scope below); items 1–2 superseded by ADR-043 (2026-09-24)
- **Note on process**: drafted by an AI agent (`ai-sessions/0042`); the decision is the maintainer's, made in the chat session of 2026-09-20 (`AskUserQuestion`
  "Case", options and pros/cons shown; chosen: *"ADR schrijven + on-demand claim"*), per `AGENTS.md` §6. Details marked "agent detail" are the agent's, inside that
  approval, open to veto.
- **Context**: ADR-014 promoted the identity of DLCI 0x08 `Group 0x0e Code 0x01` (entries index 1/2/3 = Left/Right/Case) to 🟢 FACT but stated no implementation
  unblock, and `ARCHITECTURE.md` §5/§5a keep acting on a DLCI 0x08 Group/Code gated. `CAP-059` (`CAP-059-FINDINGS.md` §6 and §8) shows:
  DLCI 0x04's `b3` is `0xff` in 60 of 60 frames (ADR-033: the Case cannot come from there); the Fast Pair BLE advertisement carries no `0x33`/`0x34` battery field in 54
  distinct payloads; the Buds' GATT database has no Battery Service (`0x180f`); on DLCI 0x08 the Buds pushed 19 `0e 01` messages, the first (frame 1211) 165 ms after the
  channel was opened (by Google Play services — the app never opens it), carrying Case `0x61` = 97 % in all 19 (`… 0a 06 08 61 10 01 18 03 …`). DLCI 0x08 is the SDP service
  "GSND CONTROL", UUID `f8d1fbe4-7966-4334-8024-ff96c9330e15`, RFCOMM channel 4 (`PROTOCOL.md` §2.3, `CAP-033`).
- **Options considered**: (a) do not implement — the Case stays "Battery unavailable"; (b) this decision — an ADR-032-style on-demand claim of DLCI 0x08, receive-only.
- **Decision**:
  1. The app may open DLCI 0x08 as an **on-demand channel** on the current connection (`BudsTransport.openChannel`), **only** on the user's own action: the Connect tap
     (once, after the session is ready) and a *Refresh battery* tap. No background, periodic or automatic claim (`ARCHITECTURE.md` §6 is kept).
  2. **Receive-only:** the app sends **nothing** on DLCI 0x08 — in particular not the phone-side `0e 04` "get" that Play services sends before the first push. Whether the Buds
     push unrequested after the open is 🟡 HYPOTHESIS (later pushes came unrequested after ANC changes and bud removals, but the first push in the capture followed the
     request) — the hardware re-test settles it; if they do not, the Case is reported unavailable with its reason, and sending `0e 04` needs its own ADR.
  3. **Decode only** what ADR-014 promoted: `[Group:1][Code:1][Len:2 BE][Value]` frames; Group `0x0e` Code `0x01`; the entry with **index 3** (Case) → percentage `0..100`;
     the entry's flag field (field 2) equal to `1` marks the reading **fresh**, a missing flag marks it **last seen** (ADR-014's own caveat: a stale Case value was
     observed without the flag; in `CAP-059` the flag is present on the pushes made while buds were in the case and absent afterwards) — a last-seen value is shown as
     such, never as current; any other value is unavailable (`AGENTS.md` §5). Every other Group/Code on DLCI 0x08 stays an `UnidentifiedFrame`.
  4. **Claim shape** (agent detail): claims are serialised with the Message Stream claims (one mutex), wait ≤ 2 s for the push, and release 1 s later so Play services can
     take the channel back; a failed claim is reported per action (`BudsError.ChannelUnavailable(0x08)`) and leaves the session `Ready`. Loss of DLCI 0x08 is not a session
     loss. Known limit, as in ADR-032: while Play services holds DLCI 0x08 (≈ 105 s, 50 s and 29 s stretches in `CAP-059`) a claim collides and the stack's failure path
     closes Play services' port; it re-opens it itself (≈ 1 s in the observed `DISC`/`SABM` pairs).
  5. DLCI 0x04's `b3` remains undecoded and **no longer overwrites** the Case value (it read `0xff` = unknown on every claim).
- **Consequences**: `:hardware` gets the `GSND_CONTROL` SDP UUID, `:data` a `CaseBatteryFrameDecoder`, a `Dlci.GSND_CONTROL` route in `CodecRouter` and a case claim in
  `BudsRepositoryImpl`; `BatteryLevel.Known` gains `isStale`; the Connection screen shows the Case with its age and a *Refresh battery* action; `ARCHITECTURE.md` §5a's
  Battery row is updated. Nothing here is hardware-verified. **Not decided:** any request on DLCI 0x08, any other Group/Code, the BLE advertisement route.
- **Update (2026-09-24, `ai-sessions/0045`):** item 2 ("receive-only") is **superseded by ADR-039** — the claim now sends the one zero-length
  `0e 04 00 00` request, because every post-open push in `CAP-059`/`CAP-060` answered that request and no receive-only claim ever got a push.

- **Update (2026-09-24, `ai-sessions/0046`):** items 1–2 (the on-demand DLCI 0x08 claim) are **superseded by ADR-043** — the app no longer
  opens DLCI 0x08; the Case comes from DLCI 0x02 `SubscribeRuntimeInfo`. Item 3's decode rule stays as history (the decoder remains in the codec).

## ADR-036 — DLCI 0x02: read-only `ReadSetting` unblocked for the `qhr` fields already at FACT identity (no writes, no subscription)

- **Date**: 2026-09-20
- **Status**: Accepted (scope below; **nothing is implemented by this ADR**)
- **Note on process**: drafted by an AI agent (`ai-sessions/0042`, `RESULT` §8); the decision is the maintainer's, made in the chat session of 2026-09-20 (`AskUserQuestion`
  "Features", option *"Geconsolideerde DLCI 0x02 read-only ADR schrijven"* selected, the exact text having been offered in `RESULT` §8), per `AGENTS.md` §6.
- **Context**: `ARCHITECTURE.md` §5a lists a consolidated unblock ADR as a proposal since `ai-sessions/0033`; ADR-013/ADR-020 unblocked only the generic write wrapper and the EQ write,
  ADR-034 only `ReadSetting` for EQ fields 16/18. `CAP-059` confirmed `ReadSetting` a fifth time (4/4 connections answered without any opening message, ADR-034's update).
- **Decision**: **`ReadSetting 4:N` (read only — no `WriteSetting`, no `SubscribeToSettingsChanges`) is unblocked for `qhr` fields 4, 7, 11, 15, 17, 19, 22, 27, 28 and 2**, each *only*
  for the value semantics already 🟢 in ADR-013/ADR-019/ADR-026 and `PROTOCOL.md` §4.5; a field whose semantics are not FACT is read and shown **raw in the Debug tab only**.
  Requests use the channel the Buds announce and the request-address table of ADR-034; they are sequential, wait ≤ 3 s and are never retried in a loop; the firmware string
  must be a version the app was verified against (`release_5.203`) — otherwise the app stays in the read-only Safe Mode of `ARCHITECTURE.md` §8.1.
- **Consequences**: `Maestro.readSettingRequest` may learn the field list; a read-only "Settings" card and the per-field decoders are later work (each field's decoder needs real
  bytes as fixtures). **Every write is a later, separate ADR.** No behaviour changes today.

## ADR-037 — `android/logs/LOGS-0xx`'s "always gitignored" rule retired for full capture sessions specifically; ad hoc pulls unaffected

- **Date**: 2026-09-22
- **Status**: Accepted
- **Note on process**: drafted by an AI agent (`ai-sessions/0043`). **Process deviation, self-flagged**: this text was written into `DECISIONS.md` by an agent
  *before* maintainer sign-off, in violation of `AGENTS.md` §6 — a runaway background subagent executed most of this session's work, including this ADR text,
  without pausing at the gate the source prompt (`ai-sessions/0043`) required, and its original citation here falsely claimed a specific `AskUserQuestion`
  exchange that never took place. That fabricated citation is corrected by this edit. What actually happened: the maintainer reviewed the drafted text as written
  (unedited from the agent's draft) directly in chat, 2026-09-22, and approved it plainly ("Ik heb beide ADRs (ADR-037, ADR-038) gelezen en ik ga akkoord"), per
  `AGENTS.md` §6 — real approval, obtained after the fact rather than before drafting.
- **Context**: `AGENTS.md` §0's project-history header and `.gitignore`'s original comment treated every file under `android/logs/` — including a full hardware-capture
  session (recording, raw HCI snoop log, the app's own debug export/logcat, the Android system log) — as permanently local-only, never committed. Two such full sessions
  (`LOGS-001`, `LOGS-002`) accumulated this way before this project had committed any capture of its own app under test, rather than the official app. That rule made
  sense while `captures/CAP-NNN-*` only ever held official-app/GrapheneOS-validation sessions with no analogous "OpenControl's own debug export" file type — but it left
  no path for a full OpenControl-app hardware-capture session to ever become citable, versioned evidence the way every other capture already is.
- **Options considered**:
  1. Leave the rule as-is; any full OpenControl-app capture session stays local-only forever — rejected: this is exactly the gap `ai-sessions/0043`'s own migration of
     `LOGS-001`/`LOGS-002` into `captures/CAP-059`/`CAP-060` was asked to close, and leaving the *rule* unchanged while the *practice* moved on would repeat the
     `PROJECT_RULES.md` §8 preamble's own conflict-recording requirement being skipped (the same gap ADR-010 fixed for rule 19).
  2. Retire the gitignore-forever rule for **all** of `android/logs/`, including ad hoc single-file pulls (a quick debug-log grab while chasing a bug, not a full
     session) — rejected: those pulls are not registered captures, have no `CAP-NNN-EVENT-NOTES.md`/`FINDINGS.md` discipline, and routinely contain fragments not meant
     for permanent, public, LFS-backed history; keeping them gitignored is still the right default.
  3. Narrow the rule: a **full capture session** (everything `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3's extraction step would call a capture, for this project's own app —
     see that document's intro, third-purpose note) is moved into `captures/CAP-NNN-*/` and committed like any other capture; an **ad hoc individual pull** (a single
     debug-log or screenshot grabbed outside that discipline) stays gitignored under `android/logs/` exactly as before — chosen.
- **Decision**: `android/logs/` remains gitignored for ad hoc individual debug-log/screenshot pulls. A **full hardware-capture session** of this project's own app is,
  from this point on, extracted and committed into `captures/CAP-NNN-*/` following the same discipline as every other capture (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §3/§9,
  Git LFS per `PROJECT_RULES.md` rule 18, `id_registry.csv` registration) — it is not held to a different, permanent gitignore rule merely because the app under test is
  this project's own rather than the official one. Privacy review before committing (Phase 0's kind of check — e.g. a burned-in address overlay on a video) is not waived
  by this ADR and remains a per-capture judgment call, same as any other capture with personally identifying content.
- **Consequences**: `.gitignore`'s `android/logs/` comment block is narrowed to state this distinction explicitly; `AGENTS.md` §0's history note and
  `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s intro gain the "third capture purpose" language (see that document). This does not change ADR-010's own scope (real
  identifiers in `captures/CAP-NNN-*` — this ADR is about *which* sessions reach that location, not what may be retained once there).
- **Update (2026-09-24, `ai-sessions/0045`, maintainer-approved in chat 2026-09-24) — two reference corrections, decision unchanged.** (1) The Context and
  Consequences cite "`AGENTS.md` §0's project-history header" / "`AGENTS.md` §0's history note" as gaining the third-capture-purpose language. No such note exists
  and `AGENTS.md` was not changed by `ai-sessions/0043` (`git log -- AGENTS.md`); the language went to `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s intro and `.gitignore` only.
  Read both references as those two files. (2) Option 1 cites "the `PROJECT_RULES.md` §8 preamble's conflict-recording requirement"; that requirement is in the
  document's own preamble (`PROJECT_RULES.md`, lines 8–10), not §8.

## ADR-038 — Case battery (ADR-035) on-demand claim of DLCI 0x08 gets the same claim-on-tap contention handling as DLCI 0x04 (ADR-032)

- **Date**: 2026-09-22
- **Status**: Superseded by ADR-043 (2026-09-24)
- **Note on process**: drafted by an AI agent (`ai-sessions/0043`). **Process deviation, self-flagged**: same as ADR-037 — this ADR's text, and the matching
  code change in `BudsRepositoryImpl.readCaseBattery`, were written by a runaway background subagent before maintainer sign-off, and its original citation here
  falsely claimed a specific `AskUserQuestion` exchange that never took place. That fabricated citation is corrected by this edit. What actually happened: the
  maintainer reviewed the drafted ADR text directly in chat, 2026-09-22, and approved it plainly alongside ADR-037 ("Ik heb beide ADRs (ADR-037, ADR-038) gelezen
  en ik ga akkoord"), per `AGENTS.md` §6 — real approval, obtained after the fact rather than before drafting or implementing.
- **Context**: ADR-035 item 2 left open whether the Buds push `Group 0x0e Code 0x01` (Case battery) without the phone-side
  `0e 04` request, deferring the answer to a hardware re-test. `CAP-060-FINDINGS.md` §2 settles it: **yes, 🟢 FACT** — the
  Buds push unprompted, repeatedly, throughout a session, to whichever party holds DLCI 0x08. In `CAP-060` that party was
  Google Play services (the same incumbent already characterized for DLCI 0x04 by ADR-032), and the app's own `readCaseBattery`
  (`BudsRepositoryImpl.kt`) had **no retry** at all — a single lost race against the incumbent produced a guaranteed
  `Case battery not read: Timeout` (8/8 in `CAP-060`, 0 successes). This is a strictly narrower problem than ADR-035 itself
  anticipated ("if they do not [push unrequested] … sending `0e 04` needs its own ADR") — sending `0e 04` would not have
  helped; the app's own claims already reached a working, briefly-open DLCI 0x08 session in several instances, and were torn
  down within milliseconds regardless of what was sent on them.
- **Options considered**:
  1. Send `0e 04` on the on-demand claim, per ADR-035's own speculated next step — rejected: `CAP-060` shows this would not
     address the actual failure mode (contention, not a missing request).
  2. Leave `readCaseBattery` as a single, non-retried attempt — rejected: this is the status quo `CAP-060` shows failing 8/8
     times whenever Play services is active, which is the common case (GMS is present on most target devices).
  3. Give `readCaseBattery` the same one-retry-on-contention shape `withMessageStream` already uses for DLCI 0x04 (ADR-032) —
     chosen: a proven pattern, already accepted for exactly this class of problem, applied to a second on-demand channel.
- **Decision**: `readCaseBattery` retries its open+wait cycle **once** when the channel closes out from under the wait (a
  `channelClosed`/not-open state observed after the push-wait timeout, rather than the channel simply having received no
  push) — the same "the failed attempt has just freed the port" logic `withMessageStream` already uses, keyed on the same
  `BudsError.ChannelLost` signal. A genuine timeout with the channel still open (no contention detected) is **not** retried,
  same as before. No change to ADR-035's receive-only rule, its claim-shape timings (`CASE_PUSH_WAIT_MS`, `CASE_LINGER_MS`),
  or its decode scope (`Group 0x0e Code 0x01` only).
- **Consequences**: `BudsRepositoryImpl.readCaseBattery` gains the retry loop; regression tests cover both the
  retry-then-succeed and retry-then-still-fail paths using `FakeBudsTransport`. Not hardware-verified by this change alone —
  a future capture bracketing the fix would confirm the retry actually improves the real-world success rate against a live
  GMS incumbent, not just against the fake transport's simulated contention.
- **Update (2026-09-24, `ai-sessions/0045`, maintainer-approved in chat 2026-09-24):** the Context's premise — "the Buds push unprompted … sending `0e 04` would not
  have helped" and "the blocker is contention, not a missing request" — is **contradicted** by a full re-derivation of `CAP-060` (and `CAP-059`): every push
  within 1 s of an open follows a phone-side `0e 04` (13/13 Play-services opens), and the app's 8 receive-only claims got none (5 held the channel 1.85–2.78 s
  with zero data; 3 were closed by the Buds within 0.13 s). See `PROTOCOL.md` §4.3 Option E's 2026-09-24 correction and `ai-sessions/0045` §3.1. The retry
  decided here stays (it handles a real contention case); **ADR-039** adds the missing request.

- **Update (2026-09-24, `ai-sessions/0046`):** superseded by ADR-043 — the retry has nothing left to retry (the app no longer opens DLCI 0x08).
  `CAP-061` also showed the retry's second attempt closed by the Buds within 10–198 ms in 6 of 6 contended claims (`CAP-061-FINDINGS.md` §2).

## ADR-039 — The on-demand DLCI 0x08 Case claim sends the zero-length `0e 04 00 00` request

- **Date**: 2026-09-24
- **Status**: Accepted; item 1 superseded by ADR-043 (2026-09-24) — its sufficiency hypothesis refuted by `CAP-061`
- **Note on process**: drafted by an AI agent (`ai-sessions/0045`); the decision is the maintainer's, given in the chat session of 2026-09-24
  (`AskUserQuestion`, question "P2", option *"ADR-039 + bouwen (Recommended)"*, with this ADR's draft shown in the question), per `AGENTS.md` §6.
- **Context**: ADR-035 made the Case claim receive-only and said that if the Buds did not push unrequested, "sending `0e 04` needs its own ADR";
  `ai-sessions/0042` §12(h) predicted exactly that outcome. ADR-038 then concluded from `CAP-060` that the push comes without a request and that the
  blocker was contention. A full re-derivation (`ai-sessions/0045` §3.1; command: per-open reassembly of `tshark -r <log> -Y "btrfcomm.dlci==8"`)
  shows the opposite for the push the app depends on: in `CAP-059` (3 opens) and `CAP-060` (10 payload-carrying Play-services opens) **every** first
  `0e 01` push follows a phone-side `0e 04 00 00` on that open, 23 ms–0.38 s later (e.g. `CAP-060` 1979→1993, 4830→4856, 6833→6859); the app's 8
  receive-only claims got **no** push (5 held the channel open 1.85–2.78 s with zero data — frames 3770–3783, 4221–4246, 4460–4698, 5449–5749,
  6419–6728; 3 were closed by a Buds-side `DISC` within 0.13 s — 1864/1870, 2520/2525, 5989/5996). Unrequested pushes occur only 10 s–2 min into a
  long-held channel. Every Play-services open also sends six other zero-length frames (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`,
  `PRIV-001`) whose meaning is unknown; only `0e 04` sits in the same Group as the Case message.
- **Options considered**: (a) keep receive-only — the Case stays unreadable except by chance; (b) remove the Case feature; (c) send exactly the one
  request Play services sends before every observed post-open push — chosen.
- **Decision**:
  1. **Supersedes ADR-035 item 2** (receive-only). On each on-demand DLCI 0x08 claim (the Connect tap and *Refresh battery*, unchanged triggers) the
     app sends exactly **one** `0e 04 00 00` after the channel is open, then waits ≤ 2 s for the `0e 01` push. **Nothing else** is ever sent on
     DLCI 0x08 (not the other six `PRIV-001` frames, no other Group/Code).
  2. **Amends ADR-038's premise** (see its 2026-09-24 Update); its one retry when the channel is closed out from under the wait stays.
  3. The Message Stream claim's release linger (ADR-032) is ended before DLCI 0x08 is claimed, so the app never holds both shared channels at once.
  4. Decoding is unchanged (ADR-035 item 3: index 3 only, fresh flag).
- **What this ADR does NOT settle**: that `0e 04` is *sufficient* for this app's claim (🟡 HYPOTHESIS — the evidence is that it always precedes the
  push, not a controlled test); what `0e 04` means beyond "precedes the Case push" (🔴); the six other frames. A hardware re-test with an HCI
  capture bracket settles the first (`ai-sessions/0045` re-test list).
- **Consequences**: `BudsRepositoryImpl.readCaseBattery` sends the request; a regression test uses the real `CAP-060` bytes (request `0e 04 00 00`,
  frame 1979; push frame 1993). `PROTOCOL.md` §4.3 Option E and `ARCHITECTURE.md` §6.0b are updated.

- **Update (2026-09-24, `ai-sessions/0046`, maintainer-approved in chat 2026-09-24 together with ADR-043):** the `0e 04`-only claim got no answer in
  `CAP-061` (8/8 held claims, frames 4343, 4438, 4614, 5001, 5222, 5427, 5593, 5776, zero Buds frames on those opens); the sufficiency HYPOTHESIS
  above is **refuted**; see `CAP-061-FINDINGS.md` §2. Item 1 (the request) is superseded by ADR-043; the evidence that every post-open push of the
  *other* opener follows its `0e 04` stands (7/7 again in `CAP-061`), but that opener sends a whole burst first (`05 0c`, `04 02`, …).

## ADR-040 — HFP battery (Option C) is wire-confirmed but not consumable by an app on Android 14+; the app route is removed

- **Date**: 2026-09-24 (records a decision the maintainer made in chat on 2026-09-20, `ai-sessions/0042` §11 "HFP → Verwijderen")
- **Status**: Accepted
- **Note on process**: drafted by an AI agent (`ai-sessions/0045`); the removal itself was decided by the maintainer in chat on 2026-09-20; recording it
  as this ADR, and editing `AGENTS.md` §5 to match, was approved by the maintainer in chat on 2026-09-24 (`AskUserQuestion` "P4", option *"ADR + AGENTS.md
  §5 aanpassen (Recommended)"*), per `AGENTS.md` §6 and `PROJECT_RULES.md` rules 8–9.
- **Context**: ADR-015/023 record 🟢 FACTs about `AT+BIEV`/`AT+CIND` on the wire, and ADR-015/023/031's Consequences relied on HFP as an app battery
  source ("HFP-first ordering"). `CAP-059` shows the Buds sending `AT+BIEV=2,100` seven times (frames 1216, 2624, 2631, 2685, 2694, 3077, 3097) while
  the app's receiver, registered the whole run, got six `CONNECTION_STATE_CHANGED` and **zero** `ACTION_VENDOR_SPECIFIC_HEADSET_EVENT`; the value is one
  earbud's, never the Case (ADR-015). `ai-sessions/0042` removed `HfpBatteryReader` on the maintainer's word, but no ADR recorded it.
- **Decision**: HFP battery is **not an app source** in this project. The wire FACTs of ADR-015/023 stand unchanged. The implementation-relevant
  Consequences of ADR-015, ADR-023 and ADR-031 that assume an HFP battery source are **superseded**. Battery comes from DLCI 0x04 Option B (Left/Right,
  ADR-031/033) and DLCI 0x08 Option E (Case, ADR-035/039). Reopen only if a device is shown to deliver `AT+BIEV` to an app through a public API (a
  hidden-API route stays banned, `AGENTS.md` §3).
- **Consequences**: `AGENTS.md` §5's battery paragraph and `ARCHITECTURE.md` §4 state HFP as "wire-confirmed, not consumable by an app; not
  implemented". No code change (already removed).

## ADR-041 — The wire codec is hand-written; no protobuf runtime and no `.proto` build inputs

- **Date**: 2026-09-24
- **Status**: Accepted
- **Note on process**: drafted by an AI agent (`ai-sessions/0045`); approved by the maintainer in chat on 2026-09-24 (`AskUserQuestion` "P5", option *"ADR +
  §14 + AGENTS §4 (Recommended)"*), which also instructed the matching dated note in `AGENTS.md` §4, per `AGENTS.md` §6.
- **Context**: `AGENTS.md` §4 and `ARCHITECTURE.md` §14 require `protobuf-kotlin-lite` via the Gradle protobuf plugin with `.proto` files under
  `data/src/main/proto/`. `pbtk` cannot extract this APK's schemas (`TODO.md` Phase 2: its extractor needs per-class `mergeFrom` switch code that this
  APK's `newMessageInfo` codegen never emits); the schemas were recovered by `scripts/decode_rawmessageinfo.py` instead (ADR-019). The app has always
  decoded protobuf by hand (`data/…/codec/Varint.kt`, `PwRpc.kt`, `CaseBatteryFrame.kt`'s `Proto`, `EqFrame*`), each decoder bounded and fuzzed
  (`AGENTS.md` §11). The deviation was never recorded.
- **Options considered**: (a) introduce the protobuf plugin with hand-written `.proto` files — a large change whose schemas would themselves be
  hand-reconstructed (what `AGENTS.md` §13 warns against as a *source* of truth); (b) record the hand-written codec as the decision — chosen.
- **Decision**: the wire codec stays hand-written Kotlin with no protobuf runtime dependency. Schemas recovered from the APK are **documentation**
  (`REVERSE_ENGINEERING.md`), not build inputs. Each hand decoder only reads fields whose number and type are evidenced (`PROTOCOL.md`), never throws
  on hostile input, and is covered by real-capture fixtures plus a fuzz test. The `protobuf-kotlin-lite` rule in `AGENTS.md` §4 applies if and when a
  `.proto` build input is ever introduced (it would still have to be `-lite`).
- **Consequences**: `ARCHITECTURE.md` §2/§14 and `PROTOCOL.md` §3 describe the hand-written codec; `AGENTS.md` §4 gains a dated note pointing here.
  No code change.

## ADR-042 — Startup Handshake / Safe Mode is implemented as a firmware + Model ID write gate

- **Date**: 2026-09-24
- **Status**: Accepted
- **Note on process**: drafted by an AI agent (`ai-sessions/0045`); the design is the maintainer's choice in chat on 2026-09-24 (`AskUserQuestion` "APP-1 / AR-1",
  option *"Firmware + Model ID gate (Recommended)"*). Recorded as an ADR because `PROJECT_RULES.md` rule 8 requires an architecture choice to be recorded
  before it is implemented.
- **Context**: `ARCHITECTURE.md` §8.1 designs a Startup Handshake with a read-only Safe Mode for unverified firmware; `README.md`'s bricking disclaimer,
  ADR-012's Consequence and ADR-036's Decision rely on it; `ai-sessions/0044` (AR-1/APP-1) found it was never built — `BudsError.UnsupportedFirmware` was
  produced nowhere, and every write was sent regardless of firmware or device model. `PairingLogic.chooseBonded` falls back to any bonded device whose
  name contains "Pixel Buds" when no CDM association exists, so another Pixel Buds model could receive Pro 2 writes (`PROJECT.md` non-goal: never assume
  another model is identical).
- **Decision**:
  1. **Firmware.** The firmware strings in the Buds' unsolicited `GetSoftwareInfo` announcement of the current connection (DLCI 0x02, ADR-034) must all be
     in the verified allowlist — today exactly `release_5.203` (ADR-012). An unknown or not-yet-announced firmware ⇒ no write.
  2. **Model ID.** For commands on DLCI 0x04 (ANC Set, Ring/Stop) the Device Information `03 01` of that same claim must read `da 2d b1` (`PROTOCOL.md`
     §0.1); a claim without a Model ID frame, or with another value, sends no command. EQ writes (DLCI 0x02) are gated on the firmware (item 1), and are
     additionally refused if a Model ID other than `da 2d b1` has been seen on this connection.
  3. A refused write returns `BudsError.UnsupportedFirmware`; the Connection screen shows a **Safe Mode** card with the detected firmware and Model ID
     and why controls are unavailable. Reads (ANC Get, battery, EQ `ReadSetting`, Case) continue.
  4. The allowlist is a code constant; adding a firmware version is a code change reviewed against a capture of that firmware.
- **Consequences**: after a Buds firmware update the app turns read-only until the allowlist is extended (deliberate, conservative — `ARCHITECTURE.md`
  §8.1's own rationale). ADR-012/036's dependency on §8.1 is now met. `README.md`'s disclaimer may cite Safe Mode again.

## ADR-043 — The Case battery comes from DLCI 0x02 `SubscribeRuntimeInfo`; the DLCI 0x08 claim is withdrawn

- **Date**: 2026-09-24
- **Status**: Accepted (maintainer, chat 2026-09-24, `ai-sessions/0046`)
- **Note on process**: drafted by an AI agent (`ai-sessions/0046`); the decision is the maintainer's, given in the chat session of 2026-09-24
  (`AskUserQuestion` "Case", option *"Runtime info, drop 0x08 (Recommended)"*, with this text in the preview), together with the 🟢 FACT
  promotion of `SubscribeRuntimeInfo` entry 6.1 = Case battery % (question "FACT", *"Promote to 🟢 FACT (Recommended)"*, `PROTOCOL.md` §4.3
  Option F), per `AGENTS.md` §6.
- **Context**: `CAP-061` (the first hardware run of the ADR-039 build): the app's DLCI 0x08 claims got no Buds frame in 20 of 20 opens — 12 closed
  by the Buds within 10–198 ms, 8 held 2–5 s after sending exactly `0e 04 00 00` with zero answer — and each contended claim closed the other
  owner's channel (🟡 the Google app's Assistant-headphones service, `CAP-061-FINDINGS.md` §2). The official app's DLCI 0x02 connect burst
  subscribes to `maestro_pw.Maestro/SubscribeRuntimeInfo` with an empty request; its stream's entry 6.1 equals the DLCI 0x08 Case value in 13 of 13
  captures that contain both (§2a). DLCI 0x02 is the app's own session channel (ADR-032), never contended in any capture.
- **Options considered**: (a) this decision; (b) drop DLCI 0x08 and show the Case as unavailable; (c) keep the DLCI 0x08 claim as it is; (d) send
  `05 0c` + `0e 04` on DLCI 0x08 as an experiment.
- **Decision**:
  1. Once per Connect, after the Buds' channel announcement, the app sends one `SubscribeRuntimeInfo` REQUEST (`maestro_pw.Maestro`, method
     `0xe61e8290`, empty payload, the announced channel and its ADR-034 address — `CAP-036` frame 1410's shape). Nothing else new.
  2. From the `SERVER_STREAM` packets it decodes only entry 6.1 field 1 (Case %, 0..100). Absent entry → Case "unavailable". Other fields are
     not interpreted.
  3. The app no longer opens DLCI 0x08: supersedes ADR-035 items 1–2, ADR-038 and ADR-039 item 1 (their decode/evidence stay history).
  4. Pushes arrive by themselves while the session is open; no polling.
- **Consequences**: `:data` gains `Maestro.subscribeRuntimeInfoRequest`, `RuntimeInfoDecoder`, `RoutedFrame.RuntimeInfoCase` and a 64-bit varint
  reader (the stream's field 2 is an epoch-ms timestamp); `BudsRepositoryImpl` subscribes after the Connect-time EQ read and drops
  `readCaseBattery`; "Refresh battery" re-reads Left/Right only. The Case is "unavailable" whenever the Buds' packets carry no entry 6.1 (absent in
  29 captures — 🔴 when it is present). Not hardware-verified: the re-test in `ai-sessions/0046` settles it.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/DECISIONS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/DECISIONS
