# 0033_FEATURE_PROMPT_2026_09_18.md — Build the v1 app: refresh ARCHITECTURE.md, implement every FACT-and-unblocked feature end to end, and produce an installable, testable build

**Number:** 0033
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Refresh `ARCHITECTURE.md` against the current protocol state, then implement, wire, and UI-complete every v1 feature that is genuinely 🟢 FACT and implementation-unblocked, with GrapheneOS-appropriate hardening and (debug) logging, ending in a build the maintainer can install and test on real hardware
**Status:** prompt only — not yet run

---

## How to (re)start this prompt — read this paragraph first, every time

Before anything else, check whether `ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` already exists.

- **It does not exist** → this is the first run. Start at Phase 0.
- **It already exists** → read it in full. Its phase status table says what's already `done`. Resume
  at the first phase not yet `done`, in order — do not skip ahead, since later phases (UI, logging,
  build verification) depend on earlier ones (domain/data/hardware) actually being finished, not just
  started.

This is a large, multi-phase build task. Update the RESULT file's phase status table and header
`Status` field (`partial — resumed` while incomplete) before ending any turn, so a resumption picks up
cleanly. Do not force every phase into one sitting — a clean phase boundary is a safe stopping point.

## Context (why this prompt exists, from the maintainer)

`ai-sessions/0031`/`0032` closed out the remaining protocol-reconstruction and documentation-consistency
backlog and produced a v1-readiness overview: **nothing outstanding blocks a minimal v1** — ANC, Find My
Buds Left/Right, EQ, and Battery via HFP are already 🟢 FACT and implementation-unblocked. The actual gap
to a shippable v1 is application-layer engineering, not further protocol research (see
`ai-sessions/0031_MAINTENANCE_RESULT_2026_09_18.md` Phase 5).

The maintainer's own instruction for this prompt (translated/summarized, Dutch original preserved
verbatim at the end of this file): **update `ARCHITECTURE.md` first** (it was last substantially edited
2026-09-13, before several later ADRs), **then implement every v1 feature that is genuinely ready**, doing
**everything necessary to design and build the app so the maintainer can test it** — following Android/
GrapheneOS best practices, giving real design attention to the UI so every implemented function is clearly
displayed and operable, adding (debug) logging, and generally doing whatever it takes to make this a good
app — **split into multiple phases run sequentially.**

### Current state of the `android/` project (verified 2026-09-18, re-confirm at Phase 0 — do not trust this summary blindly)

Five Gradle modules exist (`:app`, `:ui`, `:domain`, `:data`, `:hardware`), set up in
`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` and untouched since (`git log -- android/` shows no
commits between `ai-sessions/0014` and now). Roughly 1000 lines of Kotlin total:

- **`:domain`** — `AncMode`, `BudsError` (+ `UnidentifiedFrame`), `BudsResult`, `ConnectionState`, and
  the `BudsRepository` **interface only** (no implementation exists yet).
- **`:data`** — `AncFrameEncoder`/`AncFrameDecoder`/`AncFrame` for DLCI 0x04's ANC Set/Get/Notify,
  tested against real `tshark`-extracted fixture bytes (225 passing tests). Nothing for EQ, Battery, or
  Find My Buds yet.
- **`:hardware`** — `BudsTransport` interface, `ConnectionStateMachine` (unit-tested,
  `Disconnected → Connecting → Discovering → Ready`), `FakeBudsTransport` (scripted, for tests), and
  `RfcommBudsTransport` — **explicitly sketched, not verified**: single-socket only, no per-DLCI
  multiplexing (`PROTOCOL.md` §2.3's three-channel reality), no pairing flow, no foreground service, no
  HFP battery reading. Carries its own `// TODO(verify)` marker.
- **`:ui`** — one screen, `AncScreen.kt` (89 lines), no navigation, no other screens.
- **`:app`** — `MainActivity`, `OpenControlApplication`, Hilt wired (`ai-sessions/0013`), one DI module
  (`TransportModule`).
- **Version catalog** (`android/gradle/libs.versions.toml`) has Kotlin/Coroutines/Compose BOM/Material3/
  Hilt/JUnit5/Kotest pinned — but **no** AndroidX DataStore, no navigation-compose, no
  `protobuf-kotlin-lite` yet; add whichever of these this prompt's work actually needs, pinned, with
  justification, per `AGENTS.md` §10.

### A stale contradiction already found — resolve it early, don't let it block Phase 1

`ARCHITECTURE.md` §15 currently states multi-device support is **"already decided, not open"** (single
Buds Pro 2 only, matching `PROJECT.md`'s Definition of done). `TODO.md`'s Phase 5 checklist still lists
**"Decide multi-device (multiple paired Buds) support for v1... (currently open, see
`ARCHITECTURE.md` §15)"** as an open item. These directly contradict each other. Per `PROJECT_RULES.md`
§1, this is exactly the kind of drift to catch, not silently pick a side on — resolve which one is
current (cross-check `DECISIONS.md` for a controlling ADR) and fix the stale one directly (a plain
documentation correction, not a new decision, if `ARCHITECTURE.md`/`DECISIONS.md` already settled it).

## Mandatory reading order (do this first, in this order)

Per `AGENTS.md` §0.1 and `AI_SESSION_LOG_PROCEDURE.md` §8:

1. `AGENTS.md` (full) — especially §2 (GrapheneOS permissions/foreground services/`STATE_OFF` handling),
   §3 (Kotlin/Coroutines/Flow/sealed-`Result`/no-LiveData rules), §6/§15 (implementation gate: only
   build against a channel/feature already 🟢 FACT **and** explicitly implementation-unblocked — do not
   assume this prompt's own feature list above is exhaustive or still accurate; re-derive it yourself
   from `ARCHITECTURE.md` §5 and `DECISIONS.md`), §7 (device discovery — `CompanionDeviceManager` only,
   the bounded Fast Pair Battery Notification exception), §9 (logging/privacy — debug-mode-gated hex
   dumps, no MAC at INFO+), §11 (testing expectations, fuzz testing for `FrameDecoder`s), §12 (AGPL
   header on every new file, no code copied from the official APK/`pbpctrl`).
2. `PROJECT.md` (full) — functional scope checklist, non-goals, Definition of "done" (v1).
3. `PROJECT_RULES.md` (full).
4. `ARCHITECTURE.md` (full, all 16 sections) — this is what Phase 1 updates; know its current state
   exactly before touching it.
5. `PROTOCOL.md` (full) — you need the exact, current, per-feature FACT status and byte layouts for
   ANC (§4.1), EQ (§4.2/§4.5), Battery (§4.3, all options), Find My Buds (§4.4), and the connection
   lifecycle (§5) and framing reality (§2). Do not implement from memory of an earlier read in this
   conversation — re-verify byte layouts directly against this document at the time you write the code.
6. `DECISIONS.md` (full, every ADR — especially ADR-005/006/007 CDM+scanning, ADR-009 ANC, ADR-011/027
   Find My Buds, ADR-013 DLCI 0x02 envelope, ADR-015/023 Battery HFP, ADR-018/019 DLCI 0x02 structure,
   ADR-020 EQ, ADR-026 Volume Balance, ADR-028 Hilt, ADR-029 API level, ADR-030 CTKD, ADR-031 Battery
   Option B — note ADR-031 records message-code **identity** as FACT; confirm for yourself whether it
   also clears the implementation gate the way ADR-020 explicitly did for EQ, or whether Option B
   implementation needs a separate, explicit unblock decision before you write a decoder for it).
7. `TODO.md` (full) — especially Phase 4/5's checklists (the exact open items this prompt continues)
   and "Known technical debt."
8. `ai-sessions/0013_FEATURE_PROMPT_2026_09_13.md` and `ai-sessions/0013_FEATURE_RESULT_2026_09_13.md`
   (full) — the session that built the current skeleton; know exactly what it did and why before
   extending it.
9. `ai-sessions/0031_MAINTENANCE_RESULT_2026_09_18.md` Phase 5 (the v1-readiness overview) and
   `ai-sessions/0032_MAINTENANCE_RESULT_2026_09_18.md` — the two sessions immediately preceding this one.
10. The actual current `android/` source tree (all files under `android/*/src/`, not just the summary
    above) and `android/gradle/libs.versions.toml`.
11. `SECURITY.md`, `CONTRIBUTING.md`, `README.md` — for Phase 9's build/run instructions and any
    security-relevant constraint on distribution.

## Guardrails

- **Implementation gate, per `AGENTS.md` §6/§15**: implement only a channel/feature that is both 🟢 FACT
  in `PROTOCOL.md` **and** has an explicit `DECISIONS.md` ADR clearing it for implementation (the same
  bar `ARCHITECTURE.md` §2.1 and `ai-sessions/0013`'s own guardrails already applied). Re-derive the
  current list yourself at Phase 0/1 — do not copy this prompt's own "ANC/EQ/Find My Buds L-R/Battery
  HFP" characterization without checking it's still accurate and complete. If a feature's gate status is
  ambiguous (Battery Option B is the known candidate — see reading item 6 above), do **not** implement
  it; instead write a `PROPOSAL — awaiting maintainer decision` block in the RESULT file and skip it.
- **Never promote anything to 🟢 FACT and never write/alter a `DECISIONS.md` ADR** without the
  maintainer's explicit, in-conversation sign-off — unchanged by this being a build-focused prompt.
- **No physical Pixel Buds Pro 2 and no GrapheneOS device in this environment.** Everything must be
  built to compile, pass static analysis/lint, and pass unit tests against fixed fixtures/fakes — but
  end-to-end behavior against real hardware is explicitly **not** verified by this session, and the
  RESULT file and final chat summary must say so plainly, feature by feature, not just once in passing.
  This prompt's own purpose is to hand the maintainer a build worth installing and testing themselves —
  be honest about the boundary between "compiles and unit-tests green" and "confirmed working."
- **Kotlin only, AGPL-3.0-or-later header on every new file** (`AGENTS.md` §12), **Coroutines/Flow only**
  (no LiveData), **Jetpack Compose + Material 3 only** (no XML layouts), all I/O on `Dispatchers.IO`,
  every `BluetoothSocket`/`BluetoothGatt` call site wrapped and converted to the sealed `BudsError`
  hierarchy (no bare `catch (e: Exception) {}`), pinned dependency versions only (version catalog, no
  `+`), no `INTERNET` permission anywhere, minimal permission set with `BLUETOOTH_SCAN` flagged
  `neverForLocation` (`AGENTS.md` §2).
- **No code copied from the official APK or from `pbpctrl`** — only observed wire *behavior* is
  reconstructed (`AGENTS.md` §12). Every new codec file should cite the specific capture frame(s) it's
  modeled on (in a comment or the accompanying test), matching the existing `AncFrameEncoder`/
  `AncFrameDecoder` precedent.
- **Device discovery**: `CompanionDeviceManager` only for first-time pairing; `getBondedDevices()` for
  already-paired reconnection; the Fast Pair Battery Notification scanning exception is filtered,
  foreground-triggered, time-boxed, and stopped on backgrounding — exactly as `AGENTS.md` §7 bounds it,
  no broader interpretation.
- **Logging**: connection-state transitions, MTU/connection parameters are always safe to log
  (`AGENTS.md` §9, §13's "Implementing Android/Kotlin" workflow item 3). Raw payload hex dumps and
  anything that could disambiguate a specific device are gated behind an explicit, off-by-default
  Debug Mode developer setting. Never log the paired device's MAC address at INFO level or above.
- **`UnidentifiedFrame` surfacing**: a structurally-parseable-but-unrecognized frame is never silently
  dropped — it must reach a Debug UI (`ARCHITECTURE.md` §7, `AGENTS.md` §6), not just a log line.
- **Testing**: protobuf/frame-envelope (de)serialization needs pure byte-array unit tests independent of
  real hardware (`AGENTS.md` §11); `BudsTransport` stays behind an interface so ViewModels can be
  unit-tested against `FakeBudsTransport`, never a real `BluetoothSocket`, in tests; every
  `CodecRouter`/per-DLCI `FrameDecoder` needs a fuzz/property-based test (random/mutated/truncated/
  oversized/invalid-checksum input) asserting it degrades to `BudsError.MalformedFrame`/
  `UnidentifiedFrame` and never throws unhandled or crashes, matching `AncFrameDecoder`'s existing
  precedent.
- **Do not silently expand scope.** Case/"both simultaneously" Find My Buds ring stays out of v1
  (`DECISIONS.md` ADR-027); no audio routing/codec code; no telemetry/analytics/crash-reporting SDK of
  any kind, ever.
- Do not restructure or rewrite a document beyond what a specific phase justifies. `ARCHITECTURE.md`
  updates (Phase 1) should be substantive where the protocol/decision state actually moved, not a
  wholesale rewrite of sections that are still accurate.

## Phase 0 — Setup

Complete the mandatory reading order. Create `ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` with the
required header block and a phase status table (one row per phase below, plus Final). Register this
session in `ai-sessions/INDEX.md`. Confirm the current build still works before changing anything:
`cd android && ./gradlew assembleDebug testDebugUnitTest test` — record the exact result (pass/fail
counts) as your baseline. If it doesn't build cleanly on a fresh checkout, fix only what's needed to
restore the `ai-sessions/0013`-documented baseline before proceeding (that's a regression-repair, not
new feature work, and belongs in this phase).

## Phase 1 — Refresh `ARCHITECTURE.md` against the current protocol/decision state

Before writing any new application code, bring `ARCHITECTURE.md` up to date:

1. Resolve the multi-device contradiction flagged above (fix whichever document is stale, cite the
   controlling `DECISIONS.md` ADR).
2. Re-check every section against `DECISIONS.md`'s full ADR list (through ADR-031) and `PROTOCOL.md`'s
   current state — §5 (Protocol Framing) especially needs to reflect the settled per-channel/feature
   gates for ANC/EQ/Find My Buds L-R/Battery precisely, not a stale characterization from 2026-09-13.
3. Add or sharpen whatever architectural detail this prompt's later phases actually need to build
   against, if `ARCHITECTURE.md` doesn't already specify it clearly enough — likely candidates (verify,
   don't assume): the exact `CompanionDeviceManager` first-pairing flow and how it hands off to
   `BudsTransport`; the foreground service's own lifecycle and notification requirements
   (`AGENTS.md` §2); how `BudsRepositoryImpl` should perform `ARCHITECTURE.md` §3.1's
   state-reconciliation-on-reconnect across *all* four features, not just ANC; the UI navigation
   structure (§2's module boundary between `:ui` screens); and where the Debug UI
   (`UnidentifiedFrame`, hex-dump toggle) lives architecturally. Add these as new or expanded
   subsections, not a separate document.
4. §15's Open Architecture Questions: confirm the HID-surface question is still the only genuinely open
   one; if this phase's own work surfaces a new architectural question that only the maintainer can
   settle, add it here as open, don't decide it yourself, and surface it explicitly in the phase's
   own wrap-up note (per this project's established `PROPOSAL —` convention).
5. Record every change made in the RESULT file, section-cited, so it's auditable.

This phase produces a document, not code — do not start Phase 2 until `ARCHITECTURE.md` accurately
describes what Phases 2–7 are about to build.

## Phase 2 — Domain layer: models for every unblocked feature

Extend `:domain` (pure Kotlin, no Android dependency) with whatever's missing for EQ, Battery, and Find
My Buds Left/Right, following `AncMode`/`ConnectionState`'s existing style:

- Domain models for EQ (band values within `DECISIONS.md`'s confirmed gain-clamp range, preset
  identifiers per ADR-016), Battery (per-component: case/left/right, using `BudsError`'s "unavailable"
  path per `AGENTS.md` §5 rather than ever fabricating a value), and Find My Buds (Left/Right ring
  trigger, Case explicitly excluded per ADR-027).
- Extend `BudsRepository`'s interface with the corresponding `Flow`s and suspend functions (mirroring
  `ancMode`/`setAncMode`/`refreshAncMode`'s existing shape).
- Extend `BudsError`/`BudsResult` only if a genuinely new failure mode is needed beyond the existing
  sealed hierarchy (`ConnectionLost`, `Timeout`, `MalformedFrame`, `UnsupportedFirmware`,
  `PermissionDenied`, `Unknown`) — don't add cases speculatively.

## Phase 3 — Data layer: codecs for every unblocked feature

For each feature confirmed implementation-unblocked in Phase 0/1's re-derivation:

- **EQ**: `FrameEncoder`/`FrameDecoder` for the DLCI 0x02 `WriteSetting` envelope (`DECISIONS.md`
  ADR-013's generic shape) carrying the EQ-specific field(s) ADR-020 unblocks. Unit tests against real
  capture fixture bytes (cite the exact frames), following `AncFrameEncoder`/`AncFrameDecoder`'s
  precedent, plus a fuzz test per the Testing guardrail above.
- **Find My Buds Left/Right**: encoder/decoder (or confirm it reuses an already-implemented envelope) for
  the ring-trigger command(s) `DECISIONS.md` ADR-011 confirms. Same fixture + fuzz testing standard.
- **Battery via HFP (Option C)**: this is standard `AT+BIEV`/`AT+CIND` parsing via
  `BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT` (`AGENTS.md` §5), not a custom
  Set/Get/Notify frame codec — implement the AT-response parser in `:data` (or `:hardware`, whichever
  `ARCHITECTURE.md` §2 assigns after Phase 1's refresh) with unit tests against real captured AT strings,
  not synthetic ones.
- If Battery Option B was left unimplemented per the Guardrails' gate check, do not write a decoder for
  it in this phase — note it as deferred, pending the maintainer's decision, in the RESULT file.

## Phase 4 — Hardware layer: real transport, pairing, and connection resilience

1. **`CompanionDeviceManager` pairing flow** (`AGENTS.md` §7, API 26+, already trivially satisfied at
   this project's API 34 floor) for first-time pairing; `BluetoothAdapter.getBondedDevices()` for
   reconnecting to an already-paired device. No broader BLE discovery scanning.
2. **`RfcommBudsTransport`**: resolve its own `// TODO(verify)` — implement real per-DLCI socket
   handling for however many of DLCI 0x02/0x04 (and 0x08 only if a feature you're implementing actually
   needs it — confirm none do, per the Guardrails) this phase's features require, connecting against the
   correct SDP UUIDs (`DECISIONS.md` ADR-018, `CAP-033-FINDINGS.md` §3). Every socket call site wrapped
   per the sealed-error-conversion rule.
3. **Foreground service** (`AGENTS.md` §2) hosting the live connection, correct
   `foregroundServiceType="connectedDevice"`, persistent low-priority notification.
4. **GrapheneOS resilience**: observe `BluetoothAdapter.ACTION_STATE_CHANGED`, treat `STATE_OFF` as a
   normal, expected transition (never a crash), surface "Bluetooth is disabled" with a native
   `ACTION_REQUEST_ENABLE` prompt — never a custom/Play-Services-style dialog.
5. **HFP battery reading**: wire the Phase 3 AT-parser to a real `BluetoothHeadset` profile proxy
   (`BluetoothProfile.ServiceListener`), respecting the documented non-fixed-cadence behavior
   (`AGENTS.md` §5 — don't treat a missed beat as a liveness signal).
6. Say explicitly, per feature, what's structurally implemented-but-hardware-unverified versus what's
   only a documented `// TODO(verify)` still — do not blur this distinction.

## Phase 5 — Repository layer: wire it all together

Implement `BudsRepositoryImpl` in `:data`, wiring `:hardware`'s transport/state machine and each
feature's codec to `:domain`'s interface (`ARCHITECTURE.md` §2.1, `DECISIONS.md` ADR-001). Implement
`ARCHITECTURE.md` §3.1's state-reconciliation pattern for every feature (query real state on every
(re)connection, never trust a stale cached value across a reconnect — the same discipline ANC's own
opcode-reliability FACT, `DECISIONS.md` ADR-021/022, already established empirically for that one
feature). Wire the `:app` Hilt composition root (`TransportModule` and any new modules this phase needs)
to actually provide `BudsRepositoryImpl`, not `FakeBudsTransport`, in the real app (keep the fake wired
only in test source sets).

## Phase 6 — UI: a screen per feature, careful about actually being usable

This is where the maintainer's own emphasis on UI care applies directly — every implemented function
needs to be clearly visible and operable, not just technically wired:

1. **Navigation structure** — add `navigation-compose` (pinned, justified per `AGENTS.md` §10) if
   `ARCHITECTURE.md` §2 (as refreshed in Phase 1) calls for multiple screens rather than one; a
   connection/status screen plus one screen or section per feature (ANC, EQ, Battery, Find My Buds).
2. **Connection/pairing screen**: trigger `CompanionDeviceManager`, show live `ConnectionState`
   (including a distinct rendering for every `BudsError` case per `AGENTS.md` §8 — no generic
   "Something went wrong" catch-all where a more specific state exists), and the GrapheneOS
   Bluetooth-disabled re-enable prompt from Phase 4.
3. **ANC screen**: extend/replace the existing `AncScreen.kt` if Phase 1's navigation refresh changes its
   place in the hierarchy; keep its existing tested behavior.
4. **EQ screen**: band controls within the confirmed gain-clamp range, presets, a "Save as preset"
   affordance only if `DECISIONS.md` ADR-020's own scope note allows it at this confidence level (recall
   the field-16-vs-18 question is still open per `ai-sessions/0031`'s v1-readiness overview) — check this
   at build time, don't assume either way.
5. **Battery display**: case/left/right, each showing "Battery unavailable" rather than any fabricated
   value when the mechanism hasn't reported yet (`AGENTS.md` §5) — never a blank or a stale zero.
6. **Find My Buds screen**: Left/Right ring triggers only; if the UI would naturally suggest a "ring
   both" or "ring case" action, do not add a disabled-looking placeholder for it — per ADR-027 this is a
   deliberate, permanent scope decision, not a future placeholder, so the UI shouldn't imply otherwise.
7. **Debug UI**: a developer-facing screen/section showing `UnidentifiedFrame` occurrences and the
   Debug Mode toggle that gates verbose hex-dump logging (`AGENTS.md` §6/§9) — behind a clearly-labeled
   entry point, not surfaced in the main flow for ordinary use.
8. Material 3 throughout, no XML layouts, dark/light theme support (the project's own `themes.xml`
   already exists — extend it, don't replace the approach). State hoisting/ViewModel-per-screen per
   `ARCHITECTURE.md` §2/§11 (`StateFlow` only).

## Phase 7 — (Debug) logging

1. Always-on: connection state transitions, MTU/connection-parameter changes, pairing events — plain
   Logcat, no gating needed (`AGENTS.md` §9, §13).
2. Debug-mode-gated (off by default, toggled from the Phase 6 Debug UI, persisted via AndroidX
   DataStore — pinned, justified per `AGENTS.md` §10 — since this project has no other persistence
   mechanism decided yet; confirm against `ARCHITECTURE.md` §2/§15 before choosing DataStore over
   something else already decided): raw payload hex dumps, per-frame Group/Code/payload detail.
3. An in-app ring buffer + "Export debug log" affordance (`AGENTS.md` §9) — local-only, no network
   transmission of any log content, ever.
4. Never log the paired device's MAC address at INFO level or above; truncate/hash it if a log line
   needs to disambiguate devices.

## Phase 8 — Build verification and honest capability accounting

1. `./gradlew assembleDebug testDebugUnitTest test lint` (or the project's equivalent static-analysis
   task) — record exact pass/fail counts, comparing against Phase 0's baseline.
2. Fix anything this session's own new code broke; do not silently reduce test coverage to make a build
   green.
3. Write a clear, feature-by-feature capability table in the RESULT file: **compiles** / **unit-tested**
   / **hardware-verified** (expected to be "no" across the board in this environment) / **explicitly
   deferred** (e.g. Battery Option B, if gated per Phase 0's re-derivation) — this is the honest-accounting
   artifact the maintainer needs before testing on real hardware.

## Phase 9 — Documentation consistency sweep and hand-off for testing

1. Update `TODO.md`'s Phase 4/5 checkboxes to reflect exactly what this session completed (check nothing
   it didn't finish, leave nothing unchecked that it did finish).
2. Confirm `ARCHITECTURE.md` still accurately describes the as-built app after Phases 2–7 (a second
   pass, since implementation sometimes reveals a Phase 1 assumption was wrong — fix forward, don't leave
   the architecture doc describing a design that changed during implementation).
3. `README.md`: add or update build/install instructions specific enough for the maintainer to actually
   build the debug APK and side-load it (`./gradlew assembleDebug`, where the APK lands, minimum
   Android version, permissions the app will ask for and why).
4. `CHANGELOG.md`: an entry for this session's work.
5. Run `python3 scripts/lint_docs.py` and `./scripts/ensure_footers.py`; fix anything this session's own
   edits introduced.
6. Update `ai-sessions/INDEX.md`'s `0033` row.
7. Finalize `Status`: `complete` only if every phase finished with no pending maintainer decision;
   `awaiting maintainer sign-off` if a `PROPOSAL —` block (e.g. Battery Option B's gate, or a Phase 1
   architecture question) is still open; `partial — resumed` otherwise.

## Output

At each phase boundary, a short note (RESULT file, and to the maintainer if the chat is still live) of
what was built/changed. Phase 8's own output is the capability-accounting table. At the end, a direct
chat summary covering: what was implemented and where, exactly what remains unverified against real
hardware, any pending `PROPOSAL —` decisions needing the maintainer's answer, and precise instructions
for building and installing the resulting debug APK so the maintainer can start testing it.

---

**Original maintainer instruction (Dutch, preserved verbatim for exact phrasing):**

> Maak een prompt in het engels in ai-sessions/ voor de bouw van de v1 app die: ARCHITECTURE.md moet
> waarschijnlijk eerst geactualiseerd worden. implementeer alle v1 functionaliteit die echt klaar is.
> voer alles uit wat nodig is om de app te ontwerpen en te bouwen, zodat ik het kan testen. Houdt
> rekening met best practices voor het bouwen van android apps op grapheneos. besteed zorg aan de UI,
> zodat alle functies goed weergegeven worden en bedient kunnen worden. Zorg voor (debug) logging. Doe
> alles wat nodig is om er een goede app van te maken. Verdeel het uitvoeren in meerdere fases die
> sequentieel uitgevoerd worden.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0033_FEATURE_PROMPT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0033_FEATURE_PROMPT_2026_09_18
