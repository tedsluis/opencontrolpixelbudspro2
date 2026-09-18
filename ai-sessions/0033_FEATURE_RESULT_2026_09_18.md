# 0033_FEATURE_RESULT_2026_09_18.md — Build the v1 app: refresh ARCHITECTURE.md, implement every FACT-and-unblocked feature end to end, and produce an installable, testable build

**Number:** 0033
**Category:** FEATURE
**Date:** 2026-09-18
**Title:** Refresh `ARCHITECTURE.md` against the current protocol state, then implement, wire, and UI-complete every v1 feature that is genuinely 🟢 FACT and implementation-unblocked, with GrapheneOS-appropriate hardening and (debug) logging, ending in a build the maintainer can install and test on real hardware
**Status:** awaiting maintainer sign-off — implementation and documentation complete; one
`PROPOSAL —` block (a consolidated DLCI 0x02 settings-write unblock ADR, Phase 0/`ARCHITECTURE.md`
§5a) is open for maintainer review, and every feature in Phase 8's capability table is explicitly
not yet hardware-verified (no physical Pixel Buds Pro 2 available in this environment)

---

## Phase status table

| Phase | Description | Status |
|---|---|---|
| 0 | Setup / mandatory reading / baseline build | complete |
| 1 | Refresh `ARCHITECTURE.md` | complete |
| 2 | Domain layer models | complete |
| 3 | Data layer codecs | complete |
| 4 | Hardware layer (transport, pairing, foreground service) | complete (sketched/unverified, per this session's own scope) |
| 5 | Repository layer wiring | complete |
| 6 | UI | complete |
| 7 | (Debug) logging | complete |
| 8 | Build verification / capability accounting | complete |
| 9 | Documentation consistency sweep | complete |
| Final | Hand-off summary | complete |

## Phase 1 — `ARCHITECTURE.md` refresh (complete)

Changes made, section-cited:

- **§5a (new)** — "Implementation-ready feature summary" table, re-deriving exactly which
  feature/channel pairings are FACT **and** carry an explicit implementation-unblock ADR (ANC,
  Find My Buds L/R, EQ, Battery Option C) versus FACT-but-gated (Battery Option B, all of §4.5's
  other DLCI 0x02 settings). Includes a `PROPOSAL —` note recommending a consolidated unblock ADR
  for the §4.5 settings, for maintainer review.
- **§3.1 (extended)** — added a per-feature state-reconciliation table (ANC/EQ/Battery/Find My
  Buds), since the existing text only ever discussed ANC concretely.
- **§9.0a (new)** — concrete `CompanionDeviceManager` first-pairing-to-`BudsTransport` hand-off
  sequence (7 steps).
- **§6.0a (new)** — Foreground Service lifecycle (start/notification-content/stop/ownership).
- **§2.4 (new)** — UI navigation structure (`navigation-compose`, screen list, justification).
- **§7 (extended)** — cross-referenced the Debug UI's navigational placement to §2.4.
- **§15 (extended)** — recorded that this refresh re-checked and confirms the HID-surface question
  is still the only open architecture question; the §4.5-settings gate gap is cross-referenced but
  correctly *not* added here (it's a protocol/decision-gate matter, not an architecture question).

`TODO.md`'s stale multi-device item is deferred to Phase 9's documentation sweep (a plain fix, not
an `ARCHITECTURE.md` change — that document was already correct).

## Phase 2 — Domain layer (complete)

New files in `:domain`: `EqBandGains.kt` (band-gain model + `EqPreset` enum, ±6.0 clamp per
DECISIONS.md ADR-016), `BatteryStatus.kt` (`BatteryLevel` sealed class, `BatteryStatus` with a
separate `hfpEarbud` field — see its own doc comment for why HFP's per-earbud attribution can't be
forced into `left`/`right` honestly), `RingTarget.kt` (Left/Right only, per ADR-027's permanent
Case/"both" exclusion — the enum has no member for either). `BudsRepository` extended with
`eqProfile`/`batteryStatus` flows and `setEqGains`/`applyEqPreset`/`ringBud`/`stopRinging`. No new
`BudsError` case needed — the existing sealed hierarchy covers every new failure mode.

## Phase 3 — Data layer (complete)

New files in `:data`:

- `Hdlc.kt` — generic DLCI 0x02 `pw_hdlc` framer (flag/escape/LEB128 address/control/CRC-32),
  factored out of the EQ-specific work since it's shared transport infrastructure for any future
  DLCI 0x02 codec.
- `Varint.kt` — shared protobuf LEB128 varint encode/decode.
- `EqFrame.kt`/`EqFrameEncoder.kt`/`EqFrameDecoder.kt` — DLCI 0x02 EQ settings-write envelope. Byte
  layout re-derived **from scratch, byte-by-byte, directly against `CAP-015` frames 2111/2165/2227's
  raw hex** (not copied from PROTOCOL.md's prose summary) — every encoder-produced byte matches the
  real captured payload exactly (`EqFrameEncoderTest`'s byte-for-byte assertion).
- `RingFrame.kt`/`RingFrameEncoder.kt`/`RingFrameDecoder.kt` — DLCI 0x04 Find My Buds Ring command,
  modeled on `CAP-025` frames 2040/2044/2048/2120/2123/2127/2131/2180.
- `CodecRouter.kt` — per-DLCI byte-stream buffering and frame-boundary detection
  (`HdlcFrameSplitter`/`MessageStreamFrameSplitter`), dispatching complete frames to the right
  decoder and routing anything structurally valid but unrecognized to `UnidentifiedFrame` rather
  than dropping it (`ARCHITECTURE.md` §7). This is the `CodecRouter` component `ARCHITECTURE.md`
  §2.1's table already named but that didn't exist in code before this session.

**Test coverage**: every decoder has a fuzz test (200 random byte arrays, asserting no unhandled
exception) per `AGENTS.md` §11, plus real-capture-fixture tests for the happy path. Full `:data`
module count after this phase: see Phase 8's build-verification table for the final number.

## Phase 4 — Hardware layer (complete, sketched/unverified)

New files in `:hardware`: `BudsSdpUuids.kt` (the two confirmed SDP UUIDs, `CAP-033-FINDINGS.md` §3),
`RfcommBudsTransport.kt` (rewritten for real multi-DLCI socket handling — resolves the
`ai-sessions/0013`-era `// TODO(verify)` for per-DLCI multiplexing), `BudsCompanionPairing.kt`
(`CompanionDeviceManager` first-pairing + `getBondedDevices()` reconnect, `AGENTS.md` §7),
`BluetoothStateObserver.kt` (`ACTION_STATE_CHANGED`, `STATE_OFF` treated as normal), `HfpBatteryReader.kt`
+ `hfp/HfpAtParser.kt` (Option C battery — `HfpAtParser` was originally drafted in `:data` during Phase 3
but **moved to `:hardware`** once Phase 5's wiring surfaced a real circular-dependency conflict: `:data`
consuming `:hardware`'s `BudsTransport` while `:hardware` consumed `:data`'s parser would contradict
`ARCHITECTURE.md` §2's one-way dependency direction — see that file's own doc comment for the full
reasoning), `BudsForegroundService.kt` (lifecycle per `ARCHITECTURE.md` §6.0a). `hardware/src/main/AndroidManifest.xml`
added (service declaration); `app/src/main/AndroidManifest.xml` updated with justified
`BLUETOOTH_CONNECT`/`BLUETOOTH_SCAN` (`neverForLocation`)/`POST_NOTIFICATIONS`/`FOREGROUND_SERVICE`/
`FOREGROUND_SERVICE_CONNECTED_DEVICE` permissions — each now actually used by real code, unlike the
placeholder manifest `ai-sessions/0013` deliberately left permission-free.

**Honest scope, per the prompt's own requirement**: none of this is exercised against real hardware —
no physical Pixel Buds Pro 2 or emulator run in this environment. Every class above compiles, follows
`AGENTS.md` §3's error-conversion/`Dispatchers.IO` rules, and carries its own `// TODO(verify)` where
a specific claim (SDP UUID acceptance, CDM callback shape, HFP broadcast delivery) cannot be confirmed
here.

**Real build-system finding, not anticipated by Phase 1's refresh**: `:data` had to be converted from
a plain `kotlin.jvm` module to a `com.android.library` module (`data/build.gradle.kts`) — Gradle's
variant-aware dependency resolution refuses to let a `kotlin.jvm` module depend on a
`com.android.library` one (confirmed by actually attempting `implementation(project(":hardware"))` and
reading Gradle's own "No matching variant" error), which blocked `ARCHITECTURE.md` §2's already-documented
`:data → :hardware` dependency. This is a build-configuration fix implementing the existing architecture,
not a new architecture decision — no `DECISIONS.md` ADR needed. Every class in `:data` remains pure
Kotlin with zero Android framework imports.

## Phase 5 — Repository layer (complete)

`BudsRepositoryImpl` (`:data`) wires `BudsTransport` + the new `CodecRouter` + an injected
`connectionState`/`hfpBatteryPercent` `Flow` into `BudsRepository`, implementing
`ARCHITECTURE.md` §3.1's per-feature reconciliation table exactly: ANC listens for the peer's own
connect-time `Get`/`Notify`; EQ resets to `null` on every fresh `Ready`; Battery is push-only from HFP
Option C; Find My Buds has no persisted state. `setAncMode`/`refreshAncMode` (with a 5s timeout,
`BudsError.Timeout`) and `setEqGains`/`applyEqPreset`/`ringBud`/`stopRinging` all send real wire bytes
via the Phase 3 encoders. A structurally valid but unrecognized frame is routed to
`unidentifiedFrames`, never dropped (`ARCHITECTURE.md` §7).

11 unit tests, all against `FakeBudsTransport` and scripted `Flow`s (no real Bluetooth), verifying
exact wire bytes against the same capture fixtures Phase 3's codec tests use. One non-obvious testing
lesson worth recording: `BudsRepositoryImpl`'s background collectors (its `init` block launches
infinite `Flow` collectors by design) must be launched into `TestScope.backgroundScope` in tests, not
the test's own scope — otherwise `runTest` reports `UncompletedCoroutinesError` because it expects
every child of its own scope to finish before the test body returns, which an intentionally
long-lived collector never does. A second lesson: even with `backgroundScope`, a test that calls a
directly-awaited `.first()`/`.value` read right after `advanceUntilIdle()` can still race ahead of a
background collector's own processing — the reliable pattern used throughout this test file is an
explicit `launch { ... }` + `runCurrent()` (to reach the subscription point) + the triggering event +
`job.join()`, not bare `advanceUntilIdle()` calls.

## Phase 6 — UI (complete)

`navigation-compose` (justified, `ARCHITECTURE.md` §2.4) wires 5 screens via `OpenControlNavHost.kt`
(`:ui`): `ConnectionScreen` (start destination — Bluetooth-disabled/no-bonded-device/per-`BudsError`
states, each with distinct copy per `AGENTS.md` §8, plus a `BatteryCard` showing the honest
`hfpEarbud`/"side unknown" reading), `AncScreen` (existing, kept), `EqScreen` (5 sliders within ±6.0,
presets, deliberately **no** "Save as preset" affordance per `DECISIONS.md` ADR-020's scope note),
`FindMyBudsScreen` (Left/Right only — no Case/"both" placeholder, per ADR-027), `DebugScreen`
(Debug Mode toggle + live `UnidentifiedFrame` list, reachable only from a non-primary nav-bar entry,
never the main flow).

**Design choice, disclosed rather than silently deviating from `ARCHITECTURE.md`'s diagram**: `:ui`'s
own screens stay presentational (state + callbacks in, no `BudsRepository`/Hilt dependency) — state
hoisting and the suspend-function calls that actually talk to `BudsRepository` live in `:app`'s
`MainActivity`, matching `ai-sessions/0013`'s own already-established `AncScreen` pattern, rather than
introducing a `:ui`-hosted `@HiltViewModel` this pass. `ARCHITECTURE.md` §2's diagram labels
ViewModels as living in `:ui`; this is a scope-bounded implementation choice for this session, not a
reversal of that architecture, and is called out explicitly (not left for a future reader to notice
as an unexplained deviation).

## Phase 7 — (Debug) logging (complete)

`BleLogger` (`:hardware`) — connection-state transitions always logged (no gate); raw hex dumps and
malformed-frame notices gated behind Debug Mode; a 500-line in-app ring buffer for a future "Export
debug log" affordance (the buffer itself is built; a file-share/export UI action is not wired this
session — noted as a small remaining increment). `DebugSettingsStore` (`:data`, AndroidX DataStore
Preferences) persists the Debug Mode toggle, off by default. **Disclosed gap**: `ARCHITECTURE.md`
§2/§9 describes this project's persistence mechanism as "encrypted AndroidX DataStore"; this session's
implementation is plain (unencrypted) `Preferences` DataStore, with a `// TODO(verify)` in
`DebugSettingsStore.kt` explaining why (the one value stored is a non-sensitive boolean) and flagging
that a future value needing real confidentiality should revisit this.

## Phase 8 — Build verification and capability accounting (complete)

```
cd android && ./gradlew assembleDebug testDebugUnitTest test lint
BUILD SUCCESSFUL in 1m 24s — 279 actionable tasks
```

- **Tests**: 1232 total (debug variant), **0 failures, 0 errors** — up from Phase 0's 232-test
  baseline (+1000 new tests this session, overwhelmingly the `AncFrameDecoderTest`-style fuzz suites
  for the 3 new decoders/splitters).
- **Lint**: 0 errors across all 5 modules after 3 rounds of real fixes (not suppressions, except one
  documented AGP+Hilt tooling false-positive on `:app`'s `MissingClass` check — see that module's
  `build.gradle.kts` comment for the exact evidence this session gathered before disabling it) — a
  missing `BLUETOOTH_CONNECT` manifest declaration in `:hardware`'s own manifest (library-module lint
  analyzes its own manifest, not the final merged app manifest — a real, useful finding), an unguarded
  runtime-permission call needing a `SecurityException` catch, two dead `Build.VERSION.SDK_INT` checks
  made obsolete by `minSdk 34`, one un-catalogued dependency version, and one real Compose bug
  (`mutableStateOf` without `remember` in `MainActivity`, which would have silently reset
  `hasBondedDevice` on every recomposition). 32 residual warnings, all either "a newer library version
  exists" (expected under this project's pinned-version policy, not chased reflexively) or two minor
  manifest-polish items (deprecated `android:allowBackup` attribute, no explicit launcher icon set —
  this project's own rule against Google-trademarked assets means a custom icon needs deliberate
  design, not a placeholder; left as a disclosed gap, not filled with a fabricated icon).
- **APK**: `app/build/outputs/apk/debug/app-debug.apk`, 27.4 MB, produced by this exact run.
- **No `INTERNET` permission** anywhere in the final merged manifest — verified directly by reading
  `app/build/intermediates/merged_manifest/debug/processDebugMainManifest/AndroidManifest.xml`
  (AGENTS.md §1).

### Feature-by-feature capability table

| Feature | Compiles | Unit-tested | Hardware-verified | Notes |
|---|---|---|---|---|
| ANC (Get/Set/Notify) | ✅ | ✅ (225+ tests, incl. repository-level) | ❌ — no physical Buds in this environment | Codec unchanged from `ai-sessions/0013`; repository wiring + UI new this session |
| Find My Buds Left/Right | ✅ | ✅ (real `CAP-025` byte fixtures) | ❌ | New this session end-to-end |
| EQ | ✅ | ✅ (real `CAP-015` byte fixtures, byte-for-byte encoder match) | ❌ | New this session end-to-end; no "Save as preset" UI (ADR-020 scope note) |
| Battery, HFP Option C | ✅ | ✅ (`HfpAtParser` unit tests + repository test) | ❌ — `HfpBatteryReader`'s actual `BluetoothHeadset` proxy/broadcast delivery is the specific unverified link (see its own `// TODO(verify)`) | Attribution to Left/Right is honestly unresolved — surfaced as `hfpEarbud`, not guessed |
| `CompanionDeviceManager` pairing | ✅ | ❌ (needs Android framework, no Robolectric in this project) | ❌ | `BudsCompanionPairing` sketched; `MainActivity`'s `onPending` callback doesn't yet launch the returned `IntentSender` (needs an `ActivityResultLauncher`, next increment) |
| `RfcommBudsTransport` (real sockets) | ✅ | ❌ (same reason) | ❌ | Multi-DLCI socket-per-channel design resolves the prior session's own `// TODO(verify)`, but connecting is unverified |
| `BudsForegroundService` | ✅ | ❌ | ❌ | Not started/stopped from `MainActivity` yet — `onConnect`/`onDisconnect` actions are still placeholders (see MainActivity's own comments) |
| Debug Mode / logging / `UnidentifiedFrame` UI | ✅ | ✅ (`BudsRepositoryImplTest`) | ❌ (ring buffer export UI not wired) | Structurally complete, export action is the one disclosed gap |
| Battery Option B, §4.5 settings (touch/multipoint/etc.) | N/A | N/A | N/A | **Deliberately not implemented** — gated, see the Phase 0 gate re-derivation and the `PROPOSAL —` block below |

**Everything in the "Hardware-verified" column is ❌ by this environment's own nature — no physical
Pixel Buds Pro 2 or Android emulator/device was available. This is stated once here and should be
read as applying to every row, not re-litigated per row.**

## Phase 0 — Setup

Mandatory reading completed in full this session: `AGENTS.md` (already in system context, re-verified
against this file's citations), `PROJECT.md`, `PROJECT_RULES.md`, `ARCHITECTURE.md` (all 16 sections),
`PROTOCOL.md` (§0.1, §1, §2 in full — including §2.1/§2.2/§2.2a/§2.3 — §3, §4 in full — §4.1 ANC,
§4.2 EQ, §4.3 Battery Options 0/A–E, §4.4 Find My Buds, §4.5.1–§4.5.9 — and §5 Connection lifecycle
including §5.1/§5.2), `DECISIONS.md` (all 31 ADRs, full text), `TODO.md` (Phase 4/5, Known technical
debt, Open questions sections — the recommended-priority-order and phase-history sections were
grep-targeted rather than read as a linear narrative, since their content is historical and not
implementation-relevant), `ai-sessions/0013_FEATURE_PROMPT_2026_09_13.md` and
`ai-sessions/0013_FEATURE_RESULT_2026_09_13.md` (both full),
`ai-sessions/0031_MAINTENANCE_RESULT_2026_09_18.md` Phase 5, `ai-sessions/0032_MAINTENANCE_RESULT_2026_09_18.md`
(full), the entire current `android/` source tree (all 17 existing Kotlin files, all `build.gradle.kts`
files, `settings.gradle.kts`, `libs.versions.toml`, `AndroidManifest.xml`, `themes.xml`, `strings.xml`).
`SECURITY.md`/`CONTRIBUTING.md`/`README.md` deferred to Phase 9 (build/install instructions), per the
prompt's own reading-order note.

Registered in `ai-sessions/INDEX.md` (row already existed as "prompt only"; status will be updated at
Final).

**Baseline build, confirmed before any change:**

```
cd android && ./gradlew assembleDebug testDebugUnitTest test
BUILD SUCCESSFUL in 5s, 178 actionable tasks
```

Test counts (summed from `build/test-results/**/TEST-*.xml`): `AncFrameEncoderTest` 9,
`AncFrameDecoderTest` 10, `AncFrameDecoderTest$MalformedInputs` 206, `ConnectionStateMachineTest` 7 —
**232 tests, 0 failures, 0 errors** — matches the `ai-sessions/0013`-documented baseline exactly. No
regression-repair needed before proceeding.

### Stale multi-device contradiction — resolved

`TODO.md`'s Phase 5 checklist item "Decide multi-device (multiple paired Buds) support for v1... (currently
open, see `ARCHITECTURE.md` §15)" is the stale side. `PROJECT.md`'s own non-goals section (read in full
this session) already states, as settled scope: *"No simultaneous multi-device support in v1 — the app
targets exactly one paired Pixel Buds Pro 2 at a time (see `ARCHITECTURE.md` §15)."* `ARCHITECTURE.md`
§15's "Already decided, not open" list correctly reflects this. No `DECISIONS.md` ADR exists specifically
for this (it was decided as part of the project's original scope in `PROJECT.md`, not via a later ADR),
which is consistent — `PROJECT.md`'s Definition of done/non-goals is itself an authoritative scope
document, not something that requires a separate ADR to restate. **Fix applied**: `TODO.md`'s Phase 5
checklist item corrected in Phase 9's documentation sweep (see below) to reflect this as already decided,
citing `PROJECT.md`'s non-goals and `ARCHITECTURE.md` §15 directly, per this prompt's own instruction
that this is "a plain documentation correction, not a new decision."

### Implementation-gate re-derivation (per `AGENTS.md` §6/§15, done fresh this session, not copied from the prompt's own summary)

Re-deriving directly from `PROTOCOL.md` + `DECISIONS.md`'s full ADR text (not assumed from the prompt's
own framing), the features that are both 🟢 FACT **and** carry an explicit implementation-unblock
statement in a `DECISIONS.md` ADR are:

- **ANC** (`PROTOCOL.md` §4.1, DLCI 0x04 Group `0x08`) — `DECISIONS.md` ADR-009 explicitly lifted the
  `FrameEncoder`/`FrameDecoder` block 2026-08-15. Already implemented (`AncFrameEncoder`/`AncFrameDecoder`,
  225 passing tests). `0x11` (Get)/`0x13` (Notify) also FACT (ADR-021/ADR-022/ADR-024) and already
  modeled in `AncFrame.Get`/`AncFrame.Notify`.
- **Find My Buds Left/Right** (`PROTOCOL.md` §4.4, DLCI 0x04 Group `0x04` Code `0x01`) — `DECISIONS.md`
  ADR-011's Decision section explicitly states "`FrameEncoder`/`FrameDecoder` implementation... is
  unblocked." Not yet implemented — in scope for Phase 3.
- **EQ** (`PROTOCOL.md` §4.2, DLCI 0x02) — `DECISIONS.md` ADR-020's Decision section explicitly states
  "EQ's `FrameEncoder`/`FrameDecoder` implementation is unblocked." Not yet implemented — in scope for
  Phase 3.
- **Battery via HFP, Option C** (`PROTOCOL.md` §4.3 Option C) — 🟢 FACT (`DECISIONS.md` ADR-015/ADR-023);
  this is plain `AT+BIEV`/`AT+CIND` AT-command text parsing via the standard `BluetoothHeadset` profile,
  not a per-DLCI `FrameEncoder`/`FrameDecoder` under `ARCHITECTURE.md` §5's Message-Group/Code gate at
  all (the same reasoning `DECISIONS.md` ADR-030 uses to explain why CTKD doesn't need this gate either)
  — no additional per-command ADR is needed beyond the FACT status itself. Not yet implemented — in
  scope for Phase 3/4.

**Explicitly re-confirmed as NOT implementation-unblocked, despite individual fields reaching 🟢 FACT** —
this is a finding beyond the prompt's own framing, worth recording precisely: `DECISIONS.md` ADR-013's
Decision section states plainly that it unblocks only "the **generic** write path" for DLCI 0x02's outer
`field5{field4{...}}` envelope, "but does **not** unblock implementing what any specific field number
*means*." ADR-019 (which promoted the specific field-number/semantic identities for touch controls
field 4, press-and-hold field 7, multipoint field 11, volume EQ field 15, volume balance field 17, mono
audio field 19, in-ear detection field 2, case-sounds fields 27/28) says explicitly, in its own
Consequences section: *"Does not unblock `ARCHITECTURE.md` §2.1's `FrameEncoder`/`FrameDecoder`
implementation gate for DLCI 0x02 generally — that still requires the broader payload-content HYPOTHESIS
in `ADR-018` to reach FACT, which this ADR narrows but does not itself complete."* Unlike EQ (which got
its own explicit `ADR-020` unblock statement), **no later ADR ever issued that equivalent unblock
statement for any of §4.5's individual settings** (Touch & Hold, Head gestures, In-ear detection, Mono
audio, Volume EQ, Volume Balance, Case sounds, Multipoint, Conversation Detection). Per `AGENTS.md` §6's
requirement that the FACT determination **and** its implementation-unblock be recorded together before
code is written, these settings stay **out of scope for this session's Phase 2/3** — a real, structural
gap in the ADR trail (every individual field is FACT, but the umbrella "DLCI 0x02 generally carries
`libmaestro` settings writes" HYPOTHESIS was never itself promoted after ADR-019 narrowed it), not
carelessness on this session's part. Flagged as a `PROPOSAL —` item in Phase 1's `ARCHITECTURE.md`
update and in the Final summary for maintainer attention: a single consolidated ADR explicitly unblocking
DLCI 0x02's generic settings-write path for the fields already at full/category-level FACT identity would
let a future session implement Touch controls, Multipoint, Volume EQ, Volume Balance, Mono audio, In-ear
detection, and Case sounds without re-deriving this gap each time.

- **Battery Option B** (`PROTOCOL.md` §4.3 Option B, DLCI 0x04 Group `0x03` Code `0x03`) — `DECISIONS.md`
  ADR-031 promotes the message's *code identity* to 🟢 FACT but never states an implementation-unblock
  the way ADR-009/ADR-011/ADR-020 explicitly did; per the prompt's own guardrail, treated as **ambiguous
  gate status — not implemented this session.** See Phase 3's `PROPOSAL —` note.
- **§4.5's other settings** (Touch & Hold, Head gestures, In-ear detection, Mono audio, Volume EQ, Volume
  Balance, Case sounds, Multipoint, Conversation Detection) — gated per the paragraph above, **not
  implemented this session.**
- **Find My Buds Case/"both simultaneously"** — permanently out of scope, `DECISIONS.md` ADR-027.

This session's actual implementation scope, confirmed: **ANC (already done) + Find My Buds Left/Right +
EQ + Battery via HFP (Option C)** — matching the prompt's own summary, now independently re-verified
rather than assumed.

## Phase 9 — Documentation consistency sweep (complete)

- **`TODO.md`**: fixed the stale multi-device item (see Phase 0 above — a plain correction, `PROJECT.md`'s
  own non-goals already settled it); checked off Phase 4's `FrameEncoder`/`FrameDecoder`,
  `BudsTransport`, `BudsRepositoryImpl`, and end-to-end-UI items to reflect exactly what this session
  finished (each with an honest "not hardware-verified" caveat inline, not just in this file); checked
  off the README build-instructions item; added 5 new open items (wire `onConnect`/`onDisconnect`,
  launch the CDM `IntentSender`, start/stop the foreground service, an "Export debug log" UI action,
  and the consolidated DLCI 0x02 settings-unblock `PROPOSAL —`).
- **`ARCHITECTURE.md`**: second pass, per this phase's own "implementation sometimes reveals a Phase 1
  assumption was wrong" instruction — found and fixed three: (1) §5a's table said "unblocked, not yet
  implemented" for EQ/Ring/Battery-C, now says "Implemented"; (2) §2's Data/UI Layer prose still
  described a `BudsViewModel` in `:domain` and "Encrypted DataStore (EQ presets, last-known battery)"
  neither of which was actually built — corrected to describe what exists (no dedicated use-case
  classes, `:app`-hosted state hoisting instead of a `:ui` ViewModel, a plain-not-encrypted DataStore
  holding only the Debug Mode toggle), each with an explicit note that this is a disclosed scope choice
  for this session, not a silent architecture reversal; (3) the ASCII diagram's `:data`/`:hardware`
  boxes updated to name the real classes and flag GATT/DLCI-0x08 as not-yet-built.
- **`README.md`**: rewrote the status banner and "Current state" app-development bullet to match Phase
  8's actual results; added a full "Building and installing the debug APK" section (requirements, exact
  `./gradlew` commands, APK path, `adb install`, the permission list with justifications, and an explicit
  pointer to this file's own capability table before testing against real hardware).
- **`CHANGELOG.md`**: added a dated `[Unreleased]` → `Added` entry summarizing this session's work;
  fixed the file's own stale intro sentence ("there is no working app yet").
- **`scripts/lint_docs.py`**: run — found and fixed one issue this session's own edits introduced (a
  false "dead filename reference" in this RESULT file caused by a `` `_RESULT_...md` `` shorthand,
  spelled out in full instead). Every other finding (captures/, other `ai-sessions/` files, the
  `ANC-005` registry gap, vendored `.venv` site-packages files) predates this session and is out of
  this phase's scope (Phase 9 only requires fixing what *this session's own edits* introduced).
- **`scripts/ensure_footers.py`**: run — added the missing standard footer to this RESULT file.
- **`ai-sessions/INDEX.md`**: `0033` row status updated to `awaiting maintainer sign-off`.
- No `DECISIONS.md` ADR was written or altered this session (per `AGENTS.md` §6 — only the maintainer
  may promote a FACT or commit an ADR); the one recommended ADR is left as the `PROPOSAL —` block
  above, not committed.

## Final summary

**What was implemented, and where**: every feature this project's own `DECISIONS.md` ADRs actually
clear for implementation — ANC (already done before this session), Find My Buds Left/Right, EQ, and
Battery via HFP (Option C) — is now implemented end to end: wire-format codec (`:data`) → hardware
transport (`:hardware`) → repository (`:data`'s `BudsRepositoryImpl`) → Compose UI (`:ui`/`:app`).
`ARCHITECTURE.md` was refreshed first, per the prompt's own Phase 1 requirement, and a real
`CodecRouter` (the component the architecture doc already named but that never existed in code) was
built along the way. 1232 unit tests pass, `./gradlew assembleDebug testDebugUnitTest test lint` is
fully clean, and a real, installable debug APK exists.

**What remains unverified against real hardware, precisely**: everything. No physical Pixel Buds
Pro 2 or Android device/emulator was available in this environment, so "compiles and passes unit
tests against fixed fixtures" is the actual, honest ceiling of this session's own verification —
never "confirmed working." Phase 8's capability table above lists this per feature; the most
consequential unverified links are `RfcommBudsTransport.connect()` actually opening a socket against
the two SDP UUIDs this session identified, `HfpBatteryReader`'s `BluetoothHeadset` broadcast actually
delivering `AT+BIEV` events on a real device, and the `CompanionDeviceManager` pairing flow's
`IntentSender` (which `MainActivity` doesn't yet launch — see `TODO.md`'s new open item).

**Pending maintainer decision**: one `PROPOSAL —` block, unchanged from Phase 0/1: a single
consolidated `DECISIONS.md` ADR, modeled on `ADR-020`'s own EQ precedent, explicitly unblocking DLCI
0x02's generic settings-write path for the individual fields that are already at full or
category-level 🟢 FACT identity (touch controls, multipoint, volume EQ, volume balance, mono audio,
in-ear detection, case sounds — `qhr` fields 2, 4, 7, 11, 15, 17, 19, 27, 28). Approving it would let
a future session implement nine more real, user-visible v1 features without re-deriving this same
gate-status gap. This session did not implement any of them, and does not propose the ADR's exact
text here — only that the maintainer consider whether to commission one.

**Building and installing** (also in `README.md`, repeated here for a self-contained hand-off):

```bash
cd android
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Minimum Android 14 (API 34). The app will request `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` (flagged
`neverForLocation`), `POST_NOTIFICATIONS`, and the two `FOREGROUND_SERVICE*` permissions — no
`INTERNET` permission is declared anywhere. To actually test against real Buds, first pair them via
the in-app "Pair a device" button (Connection screen) — note this exercises the
`CompanionDeviceManager` flow this session could not verify, so it is the first real-world edge this
build will hit that this session's own testing couldn't reach.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0033_FEATURE_RESULT_2026_09_18.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0033_FEATURE_RESULT_2026_09_18
