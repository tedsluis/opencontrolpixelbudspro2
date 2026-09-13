# 0013_FEATURE_RESULT_2026_09_13.md — Resolve pending 0012 decisions and documentation consistency, then begin Phase 4 app development (ANC-first)

**Number:** 0013
**Category:** FEATURE
**Date:** 2026-09-13
**Title:** Resolve pending 0012 decisions and documentation consistency, then begin Phase 4 app development (ANC-first)
**Status:** complete — all three pending decisions (Volume Balance promotion, Find My Buds Case/"both"
scope, dependency injection) were explicitly approved by the maintainer in this same chat session
and applied below; nothing remains pending.

---

## Phase status table

| Phase | Description | Status |
|---|---|---|
| 0 | Setup / mandatory reading | complete |
| 1 | CAP-036 "34-frame" burst count | complete — mechanical correction applied |
| 2 | CAP-037 "34 reconnects" count | complete — clarification applied |
| 3 | TODO.md housekeeping note for `0012` | complete |
| 4 | Volume Balance FACT-promotion | **complete — approved and applied (`DECISIONS.md` ADR-026)** |
| 5 | Find My Buds Case/"both" Zero-GMS scope decision | **complete — approved and applied (`DECISIONS.md` ADR-027)** |
| 6 | Dependency-injection approach decision | **complete — approved and applied (`DECISIONS.md` ADR-028), Hilt wired into `:app` and verified** |
| 7 | Phase 4 app development, ANC-first | complete (scoped; see §7 below for exactly what is/isn't verified) |
| 8 | Bounded APK research (GSND naming, `gjv.p()` caller) | complete (time-boxed; both leads advanced, neither fully resolved) |
| Final | Document consistency sweep | complete (two passes — see below) |

## Mandatory reading completed this session

`AGENTS.md` (full), `PROJECT_RULES.md` (full), `PROJECT.md` (full), `ARCHITECTURE.md` (full),
`PROTOCOL.md` (full, §0–§8), `DECISIONS.md` (full, ADR-001–ADR-025), `TODO.md` (full),
`AI_SESSION_LOG_PROCEDURE.md` (full), `ai-sessions/INDEX.md`,
`ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` (full, including §4 and the Finding 106/107/125
detail sections), `captures/CAP-036-.../CAP-036-FINDINGS.md`, `captures/CAP-037-.../CAP-037-FINDINGS.md`,
`captures/CAP-046-.../CAP-046-FINDINGS.md` (full), `id_registry.csv`, targeted sections of
`REVERSE_ENGINEERING.md` (the `frb`/`fuh`/`glk`/`gjv` entry, the Message Group/Code register, the
UUID register), `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 (Capture Index), `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`,
`APK_REVERSE_ENGINEERING_PROCEDURE.md`, `reverse-engineering/APK_VERSIONS.md`.

---

## Phase 1 — CAP-036's "34-frame" DLCI 0x02 burst count (resolved, mechanical correction)

Re-ran `CAP-036-FINDINGS.md`'s own exact cited command (TShark 4.6.8, the same version `0012` used)
against the current workspace log:

```
$ tshark -r CAP-036-btsnoop_hci.log -Y "bthci_acl.chandle==0x0005 and btrfcomm.dlci==2 and frame.p2p_dir==0 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time
```

Result: **45 rows**, not 34 — confirmed independently a second time (not merely restating `0012`'s
own result). All 45 fall inside the same claimed window (06:36:32.597–06:36:35.610), ruling out a
windowing difference. Checked for a mechanical explanation: only 1 duplicate payload exists among
the 45 (not enough to explain an 11-row gap via de-duplication). No explanation found.

**Action taken** (mechanical correction, no maintainer sign-off required, per this prompt's own
Phase 1 instructions and `PROJECT_RULES.md` §1's re-run-beats-prior-claim rule):
- `CAP-036-FINDINGS.md` line 234 corrected in place: "34 rows" → "45 rows," with a one-line
  re-verification note (date, TShark version, pointer to this file and `0012` §4 Item 1).
- `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` §4 Item 1 updated in place to record the
  resolution (item kept, marked resolved, per `AI_SESSION_LOG_PROCEDURE.md` §4a's convention).

## Phase 2 — CAP-037's "34 reconnects" count (resolved, clarified — not a stale count)

Re-ran `CAP-037-FINDINGS.md`'s own cited command:

```
$ tshark -r CAP-037-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
  -T fields -e frame.number -e frame.time -e bthci_evt.status
```

Result: **36 rows** (31 success/`0x00`, 5 Page Timeout/`0x04`), matching `0012`'s own re-derivation
exactly. Read `CAP-037-FINDINGS.md`'s own event timeline (§2) before concluding either number wrong,
per this phase's own instructions. Found a real, coherent counting convention that reconciles the
36-vs-34 gap **exactly**: of the 5 Page Timeouts, 2 are each immediately (0.07s/4.76s later) followed
by a success — counting each such pair as **one** logical reconnect attempt rather than two separate
events, while counting the 3 remaining, trailing Page Timeouts (all consecutive, at the very end of
the log, none followed by any further success — consistent with the case lid closing around
06:28:48) individually, gives: 31 successes − 2 paired-off successes + 2 paired reconnects + 3
standalone failures = **34**, exactly.

This is plausible, not independently confirmed as the document's original counting *intent* — but
it is an exact arithmetic fit with no other tested reading found, and it matches how a human
reviewing the video (a failed-then-immediately-retried attempt looks like one continuous reconnect
action on screen) would naturally count it.

**Action taken** (clarification, not a correction, per this prompt's own Phase 2 instructions):
- `CAP-037-FINDINGS.md` §2 updated in place with the reconciling parenthetical, next to the original
  "34" citation.
- `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` §4 Item 2 updated in place to record the
  resolution.

Also updated `0012`'s own header `Status` field (`complete — open questions pending maintainer
sign-off` → `complete`, per `AI_SESSION_LOG_PROCEDURE.md` §4a) and `ai-sessions/INDEX.md`'s `0012`
row to match, since both §4 items are now closed and neither touched a FACT/ADR.

## Phase 3 — TODO.md housekeeping note (done)

Added a short bullet to `TODO.md`'s "Targeted research follow-ups" section (matching `ai-sessions/0008`'s
own precedent) recording that `0012` ran a full, non-sampled re-derivation of all 130 `0011` findings,
its headline result, and a pointer to `0012` itself — see `TODO.md`, immediately after the existing
`0008`/`0007` bullet.

## Phase 4 — Volume Balance FACT-promotion decision — RESOLVED, approved by the maintainer 2026-09-13

**What would be promoted:** `PROTOCOL.md` §4.5.7's `field 17` (Volume Balance) range and polarity —
from 🟡 HYPOTHESIS (strong) to 🟢 FACT — specifically: the range is exactly **±100**, and
**`field17 = +100` corresponds to the slider's Left extreme, `field17 = -100` to the Right extreme**
(the opposite of this section's own original implicit labeling). The field-*number*/semantic-*name*
identity ("field 17 = Volume balance") is **already** 🟢 FACT (`DECISIONS.md` ADR-019) and is
unaffected either way — this proposal covers only the range/direction reading.

**Evidence for:**
- `CAP-046` (Group AK, 2026-09-12) ran a dedicated, isolated-extreme-position capture (not a
  continuous drag): 8 `field17` writes, zigzag-decoded, land on exactly `-100` (×2), `+100` (×2), or
  a near-zero center value (×4, `0/1/1/2` — small drag-imprecision noise, the same pattern already
  accepted for the ±6.0 EQ clamp's own near-extreme samples). **3 of 3 extreme-position samples were
  independently video-confirmed** against the slider's on-screen handle position, zero
  counter-examples: `-100`→Right extreme, `+100`→Left extreme.
- `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` Finding 125 independently re-derived this same
  capture's wire-side values from the raw log a second time and got an exact byte-level match on all
  8 samples (same zigzag-decoded values, same frame numbers) — the same class of independent
  replication `DECISIONS.md` ADR-022 cited when promoting a comparable finding.
- The reading directly overturns this project's own earlier, unstated assumption (`CAP-022-FINDINGS.md`
  §5 implicitly labeled its first negative sample "Left") — the correction is not incidental, it was
  the entire purpose of the `CAP-046`/Group AK capture.

**Evidence against / what this promotion would explicitly NOT cover:**
- The Left/Right **video correlation** was **not** independently re-watched by `0012` — `0012`
  Finding 125 re-derived only the wire-side zigzag values, not the video-frame-to-slider-position
  correlation. This proposal's video evidence still rests solely on `CAP-046-FINDINGS.md`'s own
  original video pass (3 screenshots: `w_43.1.png`, `w_76.4.png`, `w_112.3.png`/`w_144.0.png`), not
  on any independently-repeated video check.
- **Intermediate-position scaling remains untested** — this capture sampled only the two extremes
  and near-center, never a value between center and ±100. This promotion would not cover whether
  the field scales linearly (or at all predictably) at intermediate slider positions — that stays
  🔴 open regardless of the outcome here.
- The capture's own bonus finding (`field19`/Mono audio firing in lockstep with every Balance-extreme
  write) is explicitly **not** part of this proposal — it stays 🟡 HYPOTHESIS, unpromoted, per
  `CAP-046-FINDINGS.md` §6 item 3's own scoping.

**Draft `DECISIONS.md` ADR — DRAFT, NOT COMMITTED:**

> ## ADR-026 — Volume Balance (`qhr` field 17) range and Left/Right polarity confirmed: ±100, `+100`=Left, `-100`=Right
>
> - **Date**: 2026-09-13 (proposed; not yet accepted)
> - **Status**: Proposed
> - **Context**: `DECISIONS.md` ADR-019 already promoted `field 17`'s field-number/semantic identity
>   ("Volume balance") to 🟢 FACT, explicitly leaving the numeric scale/range and which direction
>   (Left/Right) corresponds to negative vs. positive values open (`PROTOCOL.md` §4.5.7/§6). `CAP-046`
>   (Group AK, 2026-09-12) ran a dedicated isolated-extreme-position capture specifically to close
>   this gap, and `ai-sessions/0012_CROSSCHECK_RESULT_2026_09_12.md` Finding 125 independently
>   re-derived its wire-side values with an exact byte-level match on all 8 samples.
> - **Finding being recorded**: `field 17`'s range clamps at exactly ±100. `field17 = +100`
>   corresponds to the Volume Balance slider's Left extreme; `field17 = -100` corresponds to the
>   Right extreme — the opposite of this project's own earlier, unstated assumption. Evidence:
>   3 of 3 extreme-position samples video-confirmed, zero counter-examples, independently
>   re-derived at the wire level a second time (`0012` Finding 125).
> - **What this ADR does NOT clear**: whether `field17` scales linearly (or at all) between center
>   and the ±100 extremes — untested, no intermediate-position sample exists in any capture to date.
>   The `field17`/`field19` (Mono audio) timing correlation `CAP-046-FINDINGS.md` §3 also found is
>   not covered by this ADR and stays 🟡 HYPOTHESIS.
> - **Decision**: Approved, as proposed (range + polarity only). See `DECISIONS.md` ADR-026.
> - **Consequences**: if approved, `PROTOCOL.md` §4.5.7 and §6's matching open item are updated to
>   record the range/polarity as 🟢 FACT, and any future EQ/Volume-Balance UI implementation can
>   render the slider's Left/Right mapping without hedging — but should still clamp/interpolate
>   defensively for intermediate positions, since linearity there remains unconfirmed.

**Recommendation:** approve as proposed (the wire-side evidence is now independently
double-confirmed and the video correlation, while not re-watched this round, was itself already a
3/3 clean result in the original capture) — but explicitly scope the approval to range+polarity
only, not intermediate-position scaling, matching the draft ADR's own "What this ADR does NOT clear"
section.

**Approved by the maintainer directly in this chat session** ("Approve the Volume Balance
promotion"). Applied: `DECISIONS.md` ADR-026 committed (Status: Accepted); `id_registry.csv`
updated; `PROTOCOL.md` §4.5.7 and its matching §6 open item updated to record the range/polarity as
🟢 FACT; a new §8 changelog row added.

## Phase 5 — Find My Buds Case/"both simultaneously" Zero-GMS scope decision — RESOLVED, approved by the maintainer 2026-09-13

This is a **product/scope decision**, not a FACT promotion — `PROTOCOL.md` §4.4's "Major structural
finding" and §6 Behavior's matching open item, and `TODO.md`'s own framing, all already state this
plainly: *"a genuine Zero-GMS scope trade-off... no capture or static analysis can resolve this,
only a maintainer product decision can."* Nothing new was researched this phase — the trade-off is
already fully characterized; what's missing is the decision itself.

**The trade-off:**
- Left/Right Find My Buds ringing is 🟢 FACT and already implementation-unblocked
  (`DECISIONS.md` ADR-011) — the app can ship this today, with no GMS dependency.
- Case ring and "ring both simultaneously" are **only** reachable, in the official app, via a
  separate "Find Hub / Find My Device" map-view flow that is account/cloud-mediated (video-confirmed
  "Connecting…" state, on-screen copy referencing "another device linked with your Google Account").
  `PROTOCOL.md` §4.4 confirms **zero** local `Group 0x04 Code 0x01` (Ring) traffic occurs while this
  flow is active, across a ~2.5-minute observation window — this is not an assumption, it's a
  checked negative. A later code-level trace (`ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`
  Phase 3, `PROTOCOL.md` §6) additionally found the companion app's own code constructs Find My
  Device Terms-of-Service accept/skip requests only — no ring/play-sound trigger exists anywhere in
  its own decompiled source — supporting evidence, not proof, that no local fallback exists at all.
- Implementing Case/"both" ring would require a GMS/Google-account dependency this project's
  Zero-GMS goal (`AGENTS.md` §1, `PROJECT.md` non-goals) exists specifically to avoid.

**Recommendation:** ship v1 with **Left/Right ring only** (already implementable, `ADR-011`),
documenting Case/"both" as an explicit, permanent limitation in `PROJECT.md`'s non-goals — not a
placeholder for later work, unless a future capture or protocol change finds a genuine local
mechanism (which no evidence to date suggests exists).

**Approved by the maintainer directly in this chat session** ("ship v1 Left/Right only"). Applied:
`DECISIONS.md` ADR-027 committed; `id_registry.csv` updated; `PROJECT.md`'s non-goals gained a new
bullet recording this as a permanent v1 limitation; `PROTOCOL.md` §4.4 and §6 Behavior's matching
open item updated to record the scope decision (the item is checked off — not because the
underlying Find Hub mechanism question is fully research-closed, but because it no longer blocks
anything); `TODO.md`'s "Recommended priority order" item struck through as resolved; a new
`PROTOCOL.md` §8 changelog row added.

## Phase 6 — Dependency-injection approach decision — RESOLVED, approved by the maintainer 2026-09-13

`ARCHITECTURE.md` §10/§15 already frames this as exactly two options:

1. **Hilt/Dagger** — `AGENTS.md` §1 already clarifies Hilt itself does not touch
   `com.google.android.gms.*` and does not itself require Google Play Services, so it is **not**
   disqualified by the Zero-GMS rule on that basis alone.
2. **A manual, light service locator** — full independence from Google-authored build tooling, at
   the cost of hand-written wiring code across all five modules.

**Recommendation:** Hilt. Reasoning: this project's own module graph (`:app` as composition root
wiring four other modules together, `ARCHITECTURE.md` §2) is exactly the shape Hilt/Dagger's
compile-time DI is built to reduce boilerplate for, and it is a mature, widely-used, AndroidX-adjacent
tool with zero GMS/network footprint (verifiable via `./gradlew app:dependencies` before being
pinned, per `AGENTS.md` §10's dependency policy). A manual service locator is a defensible,
equally-valid alternative if the maintainer's own priorities favor build-tooling independence over
reduced wiring boilerplate — this is a genuine judgment call between two reasonable options, not one
with an obviously-superior answer, which is why it is being surfaced rather than decided here.

**Phase 7 initially proceeded with every DI-independent part of the project setup** (below) with
`:app`'s composition-root wiring deferred. **Approved by the maintainer directly in this chat
session** ("use Hilt"). Applied: `DECISIONS.md` ADR-028 committed; `id_registry.csv` updated;
`ARCHITECTURE.md` §10 rewritten to record Hilt as decided (moved out of §15's open-questions list
into its "already decided" list); `:app` actually wired as a real Hilt composition root and
re-verified — see §7's "Hilt wiring" subsection below for the full, actually-built-and-tested detail.

## Phase 7 — Phase 4 app development, ANC-first (complete, scoped)

**A real, working, git-uncommitted Android Studio project now exists at `android/`** (five Gradle
modules: `:app`, `:ui`, `:domain`, `:data`, `:hardware`; version catalog at
`android/gradle/libs.versions.toml`, pinned versions, no `+`). This session had a real toolchain
available (Gradle 9.5.1 wrapper-pinned to 8.9, JDK 21, Android SDK `android-34`/build-tools `34.0.0`,
network access to `google()`/`mavenCentral()`), so everything below was **actually compiled and
actually run**, not merely sketched:

```
$ ./gradlew assembleDebug testDebugUnitTest test
BUILD SUCCESSFUL in 1m 33s   (164 actionable tasks: 143 executed, 21 up-to-date)
```

Produces a real, installable `app/build/outputs/apk/debug/app-debug.apk` (9.46 MB). **232 unit
tests, 0 failures, 0 errors**, across `:data` (225: 10 decoder fixture tests, 9 encoder/round-trip
tests, 206 fuzz-adjacent malformed-input cases) and `:hardware` (7: `ConnectionStateMachine` +
`FakeBudsTransport`). No `INTERNET` permission anywhere (`grep -rn INTERNET **/*.xml` finds only this
file's own explanatory comment); no `com.google.android.gms.*` import anywhere.

1. **Project structure** — `android/settings.gradle.kts` (5 modules), `android/gradle/libs.versions.toml`
   (Kotlin 2.0.20, Coroutines 1.9.0, Compose BOM 2024.09.00, AGP 8.6.0, JUnit5 5.11.0, Kotest 5.9.1 —
   all pinned). No Hilt/DI dependency anywhere yet, per Phase 6's gating.
2. **Domain layer** (`:domain`, pure Kotlin, no Android dependency) — `BudsError`/`BudsResult` sealed
   types and `UnidentifiedFrame` (ARCHITECTURE.md §7), `AncMode` (the four confirmed one-hot wire
   bits, PROTOCOL.md §4.1), `ConnectionState` (Disconnected/Connecting/Discovering/Ready/Failed,
   ARCHITECTURE.md §2.1), and a `BudsRepository` interface. Compiles clean.
3. **Data layer — ANC codec** (`:data`) — `AncFrameEncoder`/`AncFrameDecoder` for DLCI 0x04 Group
   0x08's Get(`0x11`)/Set(`0x12`)/Notify(`0x13`) commands, built exactly to PROTOCOL.md §4.1's
   documented byte layout (not copied from `pbpctrl` or the official app — reconstructed from this
   project's own capture evidence, AGENTS.md §12). **Unit tests use real, `tshark`-extracted fixture
   bytes** (AGENTS.md §11), cited by frame number in the test file itself: `CAP-001` frames
   2039/2041/2132/2134/2159/2162/2193/2195, `CAP-006` frames 1393/1398/1627/1630/1731/1735/1862/1864
   (all 8 Set/Ack pairs, all 4 ANC modes, from two independent sessions), and `CAP-036` frames
   1169/1182 (Get/Notify, including ADR-024's dock-state `Settable=0x00` reading). A 206-case
   fuzz-adjacent test (`AGENTS.md` §11: truncated frames, oversized/undersized declared length,
   an unrecognized mode bit, 200 random byte sequences with a fixed seed) asserts `decode()` never
   throws, always returning a `BudsResult` — confirmed by actually running it, not merely written.
   `// TODO(verify)` documents the one genuinely unresolved area: the Set frame's 16-byte "reserved"
   field's real content is undecoded (every captured official-app Set frame carries a different,
   opaque value there) — the encoder zero-fills it by default, unverified against real hardware.
4. **Hardware layer** (`:hardware`, Android library, `compileSdk 34`/`minSdk 26`) — `BudsTransport`
   interface (`send(channelId, frame)` / `inbound: Flow<Pair<Int, ByteArray>>`, ARCHITECTURE.md
   §2.1) and `FakeBudsTransport` (scripted, used in `ConnectionStateMachineTest`). A
   `RfcommBudsTransport` sketch exists (compiles against `android.bluetooth.*`, follows AGENTS.md §3's
   `Dispatchers.IO`/sealed-error-conversion rules) but is **explicitly not verified** — no physical
   Buds are available in this environment, and per-DLCI socket multiplexing (PROTOCOL.md §2.3's
   three channels) is left as a documented `// TODO(verify)`, not implemented.
5. **Connection state machine** (`:hardware`) — `ConnectionStateMachine` driving
   `Disconnected → Connecting → Discovering → Ready`, plus `Failed(BudsError)`/`onDisconnected()`
   (ARCHITECTURE.md §6: an `IOException` is always a normal transition, never a crash). Kept
   structurally honest per PROTOCOL.md §5's own ⚪ ASSUMPTION scope — a `// TODO(verify)` comment
   says so explicitly. 7/7 unit tests pass.
6. **`:app` composition root — initially gated on Phase 6, then wired once Hilt was approved (same
   session).** See the "Hilt wiring" subsection below. The manifest deliberately declares **no**
   Bluetooth/foreground-service permissions (nothing in this module touches a real Bluetooth device
   — the injected `BudsTransport` resolves to `FakeBudsTransport`), and no `INTERNET` permission,
   ever.
7. **What this phase explicitly does NOT do** (all confirmed still true after implementation, not
   just planned): no EQ/Battery/Find My Buds `FrameEncoder`/`FrameDecoder` (`:data` has ANC only); no
   `BudsRepositoryImpl` wiring `:data` to `:domain` (only the interface exists); no real-hardware
   validation of `RfcommBudsTransport` or `ConnectionStateMachine` against an actual Bluetooth stack
   (both are validated only against fakes/fixtures); no UI screens beyond the single `AncScreen`
   needed to exercise the domain models end-to-end in Compose.

### Hilt wiring (added after Phase 6's approval, same session)

Added Hilt 2.52 + KSP 2.0.20-1.0.25 to the version catalog (pinned, no `+`). `:app` is now a real
Hilt composition root:
- `OpenControlApplication` (`@HiltAndroidApp`), registered in the manifest.
- `MainActivity` (`@AndroidEntryPoint`), field-injecting a real `ConnectionStateMachine` (given an
  `@Inject constructor()` in `:hardware`, `@Singleton`) and a real `BudsTransport`.
- `app/di/TransportModule.kt` (`@Module @InstallIn(SingletonComponent::class)`) binds `BudsTransport`
  to `FakeBudsTransport` — a documented `// TODO(blocked on real-hardware verification)` marks this
  as the binding to swap once `RfcommBudsTransport` is verified against real hardware; `:app` does
  **not** claim a working hardware connection, only a working DI graph.
- `MainActivity` exercises the injected `ConnectionStateMachine` once (`LaunchedEffect(Unit)`, not a
  composition-time side effect) to walk it through `Disconnected → Connecting → Discovering → Ready`
  and logs the injected `BudsTransport`'s state — proving the graph resolves real objects, not
  placeholders.
- `BudsRepositoryImpl` and any real use-case layer remain **not implemented** — Hilt is wired, but
  there is still nothing beyond `ConnectionStateMachine`/`BudsTransport` for it to inject yet, per
  this phase's own unchanged scope (`BudsRepositoryImpl` stays a separate, future task).

**Rebuilt and re-verified end-to-end after adding Hilt:**

```
$ ./gradlew assembleDebug testDebugUnitTest test
BUILD SUCCESSFUL in 2m 2s   (178 actionable tasks: 175 executed, 3 up-to-date)
```

`hiltJavaCompileDebug`/`hiltJavaCompileRelease` ran and generated Hilt's components successfully
(one pre-existing-library deprecation note, not an error). Produces a real, larger
`app-debug.apk` (9.73 MB, up from 9.46 MB pre-Hilt). **Every test result file still reports zero
failures and zero errors** (`:data`'s 225 and `:hardware`'s 7 unit tests, both debug and release
variants) — re-checked directly against the raw JUnit XML, not inferred from the build summary line.
Re-checked Zero-GMS compliance after adding a real third-party dependency: no `INTERNET` permission
anywhere (only this file's own explanatory manifest comment matches "INTERNET"), no
`com.google.android.gms.*` import anywhere in `android/`.

**Guardrail compliance checked explicitly**: Kotlin only (no `.java` source files added); every new
file carries the AGPL-3.0-or-later header (AGENTS.md §12); Coroutines/`Flow`/`StateFlow` throughout,
no `LiveData`; sealed `BudsResult`/`BudsError` used at every fallible boundary in `:data`/`:hardware`,
no bare `catch (e: Exception) {}`; no field number beyond ANC's own confirmed opcodes was implemented
(DLCI 0x02's generic write path, DLCI 0x08 entirely, and EQ/Battery/Find-My-Buds were all correctly
left alone this phase, matching the guardrails' explicit implementation-gate list).

## Phase 8 — Bounded APK research (GSND naming, `gjv.p()` caller) — time-boxed, both leads advanced

Within `DECISIONS.md` ADR-017's mechanical-assistance boundary (search/list/explain only — this
session decided nothing about relevance or promotion; both findings below are recorded in
`REVERSE_ENGINEERING.md` as proposals awaiting maintainer review, matching that document's own house
style for open, code-derived leads).

1. **`"GSND"` naming lead** — re-ran `grep -ri "gsnd"` (clean negative reconfirmed, same as
   `CAP-033-FINDINGS.md` §3's original 2026-08-30 pass) and extended the search to `"gsound"`/
   `"google[._-]?sound"` variants. Found **43 occurrences of `"gsound"`** across two bundled
   tokenized crash/log-string CSV assets (`jadx-output/resources/assets/{1,2}/...`), all as
   substrings of on-device-firmware source-file paths (`services/ble_profiles/voicepath/gsound/...`,
   GATT-server source files, an OTA module). This is a **plausible, unconfirmed** expansion for the
   "GSND" abbreviation in `CAP-033`'s SDP service names ("GSND CONTROL"/"GSND AUDIO") — thematically
   consistent (a BLE audio/voice-control accessory service), but no direct textual link between the
   two strings was found, and this asset class is a firmware crash-string table, not app logic.
   Full write-up: `REVERSE_ENGINEERING.md`'s new `"GSND" naming lead` entry.
2. **`gjv.p()`'s own caller** — **found**, via a materially different search strategy from the two
   prior (failed) passes: a direct smali method-descriptor search (`Lgiz;->p()`) instead of a
   generic Java-source token grep. Traced the caller chain: `gag` (a generic, `Boolean`-gated,
   R8-merged dispatcher whose partially-decompiled code contains a `>= 604800000`ms — **exactly 7
   days** — staleness-check comparison) constructs `ftw` (another generic dispatcher) with case
   index `9`, which invokes `giz.p()`/`gjv.p()`. This is a materially different, and more plausible,
   trigger shape than either of this document's own prior candidates (an OTA-transfer gate, or a
   generic post-connect settling action) — a **periodic, ~weekly staleness check**, not obviously
   connect-time-adjacent at all. Not confirmed against any wire capture this pass; recommended next
   step (tracing `gag`'s own 16 construction sites) explicitly not attempted, per this phase's own
   time-box. Full write-up: `REVERSE_ENGINEERING.md`'s `frb`/`fuh`/`glk`/`gjv` entry, 2026-09-13 update.

Both findings are proposals only — no `PROTOCOL.md`/`DECISIONS.md` edit was made for either.

## Final phase — Document consistency sweep

**First pass** (before the maintainer's approvals arrived):

- **`PROTOCOL.md`** — checked, no change needed for §4.5.7/§4.4/§6 Behavior (both still PROPOSALs
  at that point). No §8 changelog row needed yet (nothing in `PROTOCOL.md` itself had changed).
- **`TODO.md`** — Phase 3's own note added. Phase 4 App-development checklist updated: checked
  "Set up the Android Studio project" and "Implement `ProtocolCodec`... ANC" (both done and
  verified); left unchecked "Implement `BudsTransport`... and `ConnectionStateMachine`" (partial —
  state machine/interface/fake done, real transport unverified), "Decide dependency injection
  approach," "Implement `BudsRepository`/`BudsRepositoryImpl`," "First working end-to-end
  connection + battery status."
- **`DECISIONS.md`** — no new ADR committed yet; ADR-026 drafted only, in this file.
- **`REVERSE_ENGINEERING.md`** — Phase 8's two search results recorded in full.
- **`CAPTURE_BLUETOOTH_HCI_SNOOP.md`**/**`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`** — checked, no change
  needed (see the per-row reasoning below, unchanged by the second pass).

**Second pass** (after the maintainer approved all three pending decisions in this same
conversation — "Approve the Volume Balance promotion, ship v1 Left/Right only, use Hilt"):

- **`DECISIONS.md`** — three new ADRs committed: **ADR-026** (Volume Balance range/polarity → FACT),
  **ADR-027** (Find My Buds Case/"both" — ship v1 without, permanent scope decision), **ADR-028**
  (dependency injection — Hilt). All three registered in `id_registry.csv`.
- **`PROTOCOL.md`** — §4.5.7 and its §6 open item updated to 🟢 FACT for the Volume Balance
  range/polarity. §4.4 and its §6 Behavior open item updated to record the Find My Buds Case/"both"
  scope decision (checked off as no-longer-blocking, not as fully research-closed — the underlying
  Find Hub mechanism question is separately still open, at low priority, per that item's own text).
  A new §8 changelog row added recording both promotions/decisions.
- **`PROJECT.md`** — non-goals gained a new bullet: no Case/"both" Find My Buds ring support,
  citing ADR-027.
- **`ARCHITECTURE.md`** — §10 rewritten to record Hilt as decided (not an open preference); §15's
  open-questions list lost its "Hilt vs. manual DI" line, and its "already decided" list gained both
  the DI decision and the Find My Buds Case/"both" scope decision.
- **`TODO.md`** — the "Recommended priority order" section's DI and Find-My-Buds-Case bullets
  struck through as resolved. Phase 4 App-development checklist: DI checkbox now checked (Hilt,
  wired and verified same session); the Android-Studio-setup and ANC-codec checkboxes' text
  unchanged (already accurate from the first pass).
- **`CAPTURE_BLUETOOTH_HCI_SNOOP.md`** — checked §9's Capture Index rows for `CAP-036`/`CAP-037`
  again: `CAP-036`'s row doesn't quote the "34"/"45" burst count at all — no change needed.
  `CAP-037`'s row does say "34 reconnects over ~20 minutes," but that number is unaffected by
  Phase 2's resolution (34 was, and remains, the correct *logical*-reconnect count; only the
  reconciliation against the raw 36-event count was clarified, in `CAP-037-FINDINGS.md` itself) —
  **no change needed** here either.
- **`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`** — re-checked every Test-ID whose Evidence column points at
  `CAP-036`/`CAP-037` (`OBS-004`, `PAIR-003`) — neither restates the specific "34" count in its own
  Evidence-column text — **no change needed**.
- **`REVERSE_ENGINEERING.md`** — unaffected by the second pass; Phase 8's findings stood as proposals
  throughout (Phase 8 was not one of the three decisions the maintainer approved).

---

## Decisions made (record of the maintainer's approvals)

The maintainer approved all three pending items directly in this chat session, in one message:
*"Approve the Volume Balance promotion, ship v1 Left/Right only, use Hilt."*

1. **Volume Balance (`field 17`) range/polarity → 🟢 FACT.** Approved as proposed (range ±100 +
   Left/Right polarity only, not intermediate-position scaling). `DECISIONS.md` ADR-026.
2. **Find My Buds Case/"both simultaneously" — ship v1 with Left/Right ring only.** `DECISIONS.md`
   ADR-027; `PROJECT.md` non-goals updated.
3. **Dependency injection — Hilt.** `DECISIONS.md` ADR-028; `:app` wired as a real Hilt composition
   root and rebuilt/re-tested successfully (see Phase 7's "Hilt wiring" subsection) — this is not
   just a documentation decision, the code now reflects it.

Nothing further is pending from this session.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0013_FEATURE_RESULT_2026_09_13.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0013_FEATURE_RESULT_2026_09_13
