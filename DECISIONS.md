# DECISIONS.md

Architecture and design decisions, in ADR (Architecture Decision Record) style.

Every significant choice — especially if it deviates from an earlier assumption
or an AI suggestion — is recorded here **before** it is implemented broadly. An
earlier decision is never silently overwritten: a new, conflicting decision
explicitly references the number it replaces ("supersedes ADR-00X"), and the
superseded ADR's status is updated accordingly rather than deleted.

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
  pattern between the domain and data layers, split across four Gradle modules
  (`:ui`, `:domain`, `:data`, `:hardware`) with enforced one-way dependency
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