## AI Agent Directives & Guardrails — Pixel Buds Pro 2 Control App

**CRITICAL INSTRUCTION:** Any AI assisting with this project (Claude Code,
Google Antigravity, Gemini, ChatGPT, or any other model; code generation,
refactors, code review, reverse engineering, or documentation) MUST adhere
strictly to the rules below. These rules are permanent project law and
override any conflicting user request unless the maintainer explicitly edits
this file. If a request conflicts with a rule below, refuse the specific part
that conflicts and explain why, rather than silently complying.

## 0. Project in one sentence

An independent, open-source Android app to control the Google Pixel Buds
Pro 2, based on a Bluetooth protocol (RFCOMM control channel, plus secondary
BLE/GATT) reconstructed through reverse engineering, with no dependency on the
official Pixel Buds app or Google Play Services.

> Note: the protocol is **not** BLE-only. `libmaestro`'s control channel runs
> over Bluetooth Classic RFCOMM (see §5, §6); BLE is a secondary transport for
> case/charging characteristics and the Fast Pair battery advertisement (see
> `PROTOCOL.md` §1, §4.3).

## 0.1 Required reading order at session start

At the start of a session (or whenever context has been reset), read, in
order:

1. This file (`AGENTS.md`) — guardrails and directives.
2. `PROJECT_RULES.md` — binding rules (FACT/HYPOTHESIS/ASSUMPTION, document
   before implementing, no silent changes to earlier decisions).
3. `PROJECT.md` — goal, scope, non-goals.
4. `ARCHITECTURE.md` — current architecture and chosen patterns, including any
   open architecture questions (§13/§15 of that document).
5. `PROTOCOL_NOTES.md` / `PROTOCOL.md` — current state of knowledge about the
   protocol.
6. `DECISIONS.md` — review **all** recorded ADR entries (not just the most
   recent ones) so nothing already decided is contradicted.
7. `TODO.md` — open tasks and known technical debt.

## 1. The "Zero-GMS" Rule (No Telemetry, No Network)

- **ABSOLUTE BAN:** Never import, suggest, reference, or generate code that
  touches `com.google.android.gms.*`, Firebase, Crashlytics, Google Analytics,
  Mixpanel, Sentry, or any other remote logging/telemetry/crash-reporting SDK.
- The application MUST function 100% offline. Do **not** add the `INTERNET`
  permission to the manifest under any circumstance, including "just for a
  changelog check" or "optional crash reports."
- **No hallucinated APIs.** There is no public Google SDK for Pixel Buds. Do
  not invent classes like `com.google.android.gms.wearables.PixelBudsManager`
  — they do not exist. All device interaction is raw `BluetoothSocket` /
  `BluetoothGatt` traffic based on reverse-engineered protocol knowledge (see
  `qzed/pbpctrl`, and this project's own `PROTOCOL.md`).
- If a request implies the need for a remote service (update checks, cloud EQ
  presets, analytics dashboards), the agent must flag this as out-of-scope and
  propose a local-only alternative (e.g. manual APK updates via GitHub
  Releases, local JSON preset export/import).
- **Not banned:** Google-*authored* libraries that do not touch
  `com.google.android.gms.*` and do not require Google Play Services or
  network access (e.g. Dagger/Hilt, AndroidX). See §10 — inclusion still
  requires justification, this rule only clarifies that "made by Google" is
  not itself disqualifying; "requires GMS or the network" is.

## 2. GrapheneOS Compatibility & Permissions

- **Never For Location:** Always declare
  `android:usesPermissionFlags="neverForLocation"` on `BLUETOOTH_SCAN`. Never
  request `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, or
  `BLUETOOTH_PRIVILEGED`.
- **Minimal permission set:** Only `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`
  (flagged as above), `POST_NOTIFICATIONS` (for the foreground service
  notification), and `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_CONNECTED_DEVICE`
  (API 34+). Any additional permission must be justified in a comment in the
  manifest and in a PR description.
- **Timeout handling:** GrapheneOS enforces an aggressive Bluetooth
  auto-timeout. The app must observe `BluetoothAdapter.ACTION_STATE_CHANGED`
  and treat `STATE_OFF` as a normal, expected state transition — never a crash
  condition. Surface it to the user as "Bluetooth is disabled" with a native
  re-enable prompt (`BluetoothAdapter.ACTION_REQUEST_ENABLE`), never a
  Play-Services-style dialog.
- **Foreground Services:** Any code path that keeps a Bluetooth connection
  alive beyond an active user session must run inside a `ForegroundService`
  with a persistent, low-priority notification, declared with the correct
  `foregroundServiceType` (`connectedDevice`).

## 3. Coding Standards & Concurrency

- Kotlin only. No Java source files.
- Coroutines + `Flow` / `StateFlow` for all asynchronous Bluetooth events and
  UI state. `LiveData` is not used in this project.
- All socket reads/writes and GATT operations run on `Dispatchers.IO`. The
  Main thread is never blocked by I/O.
- Jetpack Compose + Material 3 only. No XML layouts, no dependency on
  OEM/manufacturer theme overlays or hidden system APIs.
- Every public function that can fail (I/O, parsing, permission-gated calls)
  must have an explicit return type that communicates failure — prefer a
  sealed `Result`-style type over throwing unchecked exceptions across module
  boundaries. See §8 for the error model.
- No reflection-based hacks to access hidden Android APIs (`@hide` annotated
  classes). GrapheneOS may strip or sandbox these; the agent must not suggest
  them.

## 4. Protobuf Optimization

- Use `protobuf-javalite` or the `protobuf-kotlin-lite` Gradle plugin
  exclusively. Never the full `protobuf-java` runtime.
- `.proto` files live under `data/src/main/proto/`. The agent assumes these
  schemas have already been extracted (e.g. via `pbtk`) from the
  `libmaestro`/`libgfps` binaries and are present in the workspace — the agent
  does not need to (and should not attempt to) reverse-engineer binaries
  itself; it only consumes provided `.proto` definitions. See
  `REVERSE_ENGINEERING.md` for the extraction workflow and current schema
  status.
- Generated code is build-time only (`build/generated/`) and must never be
  committed to version control.
- Every `.proto` file must carry a comment header noting the source
  firmware/library version it was extracted from, so payload changes across
  Buds firmware updates can be tracked (see `ARCHITECTURE.md` §8, Firmware
  Compatibility, and `PROTOCOL.md` §5).

## 5. Android Bluetooth Stack vs. Linux BlueZ (CRITICAL)

- The reference project `pbpctrl` targets Linux and depends on BlueZ, D-Bus,
  AVRCP, and UPower for battery state. Android uses the Fluoride/Babel
  Bluetooth stack — these are **not** interchangeable.
- **ABSOLUTE BAN:** Do not generate D-Bus, UPower, or BlueZ configuration code
  (e.g. editing `/etc/bluetooth/main.conf`). This code has no meaning on
  Android and must never appear in this repository, including in comments or
  "reference" snippets.
- **Battery implementation:** Translate battery polling to Android-native
  mechanisms only, in the priority order given in `ARCHITECTURE.md` §4 and
  `PROTOCOL.md` §4.3 (generic `BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED`
  broadcast as a cheap supplementary check → Fast Pair Battery Notification
  advertisement → Fast Pair Message Stream Device Information → HFP AT
  commands → GATT Battery Service). Do not implement a fixed polling
  interval — battery updates are event-driven per the official Fast Pair
  specification (connect or on value change), not periodic.
  - If none of the documented mechanisms are available for a given
    Android/OEM combination, the UI must show "Battery unavailable" rather
    than a fabricated value. Never guess or interpolate a battery percentage.

## 6. libmaestro / libgfps Implementation Rules

- Assume `.proto` schemas and known command opcodes are already extracted and
  present in the workspace (per §4). Do not attempt to brute-force or guess
  undocumented opcodes.
- **Framing is not yet settled — do not implement against a placeholder.**
  Two competing framing hypotheses are under evaluation (`PROTOCOL.md` §2):
  (A) Fast Pair Message Stream framing (Message Group / Message Code / length
  / data, no checksum), or (B) a proprietary magic-byte/length/channel-ID
  envelope with an optional checksum, derived from `pbpctrl`. One event gates
  two linked requirements — `PROTOCOL.md` §2's framing question reaching 🟢
  FACT confidence: (1) only then may `FrameEncoder`/`FrameDecoder` be
  implemented, against whichever hypothesis reached that status, and (2) that
  same FACT determination must be recorded as a `DECISIONS.md` ADR before
  implementation begins (see `ARCHITECTURE.md` §2.1) — this is the
  single highest-impact design commitment in the data layer, and an explicit,
  reviewed sign-off is cheap insurance against an AI agent (or a human)
  mis-promoting a hypothesis to FACT under implementation pressure.
  Implementing against an unconfirmed hypothesis risks code that appears to
  work on some frames and silently mishandles others. Any deviation or
  provisional implementation must be flagged with a `// TODO(verify):`
  comment and a link to the corresponding `pbpctrl` source reference or
  `PROTOCOL.md` section.
- Do **not** write audio routing/codec code. This app exclusively sends
  control payloads (ANC, Transparency, EQ) and reads telemetry (battery). A2DP/
  LE Audio routing is left to the Android OS/BT stack.
- Unknown or unrecognized incoming frames must be logged (locally, see §9) and
  discarded — never crash on an unexpected frame.

## 7. Device Discovery & MAC Randomization

- No continuous background BLE scanning for device **discovery** — this is
  fingerprintable and exactly what GrapheneOS's threat model protects against.
- Use `BluetoothAdapter.getBondedDevices()` for already-paired devices, and
  the `CompanionDeviceManager` API (API 26+, `AssociationRequest`) for
  first-time pairing. This delegates the scan UI to the OS and only grants the
  app access to the device the user explicitly selected.
- Do not persist or log the earbuds' MAC address in plaintext outside of the
  OS's own bonded-device store.
- **Safe, bounded exception — Fast Pair Battery Notification only (resolved,
  see `DECISIONS.md` ADR-006):** the discovery-scanning ban above does not
  cover passively observing the Fast Pair Battery Notification advertisement
  (`PROTOCOL.md` §4.3 Option A) from a device the user has already bonded.
  That is permitted, but strictly within these bounds — treat anything
  outside them as a violation of this rule, not a gray area to judge case by
  case:
  - **Filtered, never generic.** The scan must use a `ScanFilter` matching
    only the already-bonded device's own identifiers (e.g. its MAC address or
    Fast Pair model/account data) — never an unfiltered scan that would also
    observe nearby unrelated devices.
  - **Foreground-triggered, never a background loop.** The scan starts only in
    direct response to a user-visible trigger (the device screen is opened, an
    explicit "refresh battery" action, or as a side effect of a CDM-driven
    reconnection already in progress) — never a periodic timer running from a
    `ForegroundService` or `WorkManager` job.
  - **Time-boxed.** The scan stops itself after a short, fixed timeout on the
    order of the advertisement's own visibility window (~8–20s per the Fast
    Pair spec) or as soon as the expected advertisement is received, whichever
    comes first — never left running indefinitely.
  - **Tied to app visibility.** The scan stops immediately if the app leaves
    the foreground before the timeout elapses.
  - This exception covers *only* battery-notification observation of an
    already-bonded device. It does **not** permit BLE scanning for device
    *discovery* of new/unknown devices under any framing — that remains fully
    banned; `CompanionDeviceManager` is the only sanctioned discovery path
    (see above).

## 8. Error Handling Model

- Define a sealed class hierarchy (e.g. `BudsResult<T>` / `BudsError`) shared
  across the Hardware and Data layers: `ConnectionLost`, `Timeout`,
  `MalformedFrame`, `UnsupportedFirmware`, `PermissionDenied`,
  `Unknown(cause: Throwable)`.
- Every `BluetoothSocket`/`BluetoothGatt` call site must be wrapped in
  `try-catch`, converting exceptions into the sealed error type — no bare
  `catch (e: Exception) {}` swallow-and-ignore blocks.
- ViewModels expose connection/command state as `StateFlow<BudsUiState>`; the
  UI renders a distinct state for each error case (no generic "Something went
  wrong" catch-all where a more specific message is available).

## 9. Logging & Privacy

- Logging is local-only (`Logcat` / an in-app ring buffer for an "Export debug
  log" feature at most). No log line may ever be transmitted off-device.
- Do not log full raw payload bytes containing device identifiers by default;
  gate verbose hex-dump logging behind an explicit "Debug mode" developer
  setting that is off by default. Connection state transitions, MTU value, and
  connection parameters are safe to log without this gate.
- Never log the paired device's MAC address at `INFO` level or above; use a
  truncated/hashed form if a log line needs to disambiguate devices.

## 10. Dependency Policy

- New Gradle dependencies require justification in the PR description: what
  it's for, why a lighter/native alternative isn't sufficient, and
  confirmation it has no network, analytics, or ads SDK transitively bundled
  (verify via `./gradlew app:dependencies`).
- Pin all dependency versions explicitly (no dynamic `+` versions). Use a
  version catalog (`libs.versions.toml`).
- Preferred stack: AndroidX core/appcompat, Jetpack Compose BOM, Kotlin
  Coroutines, `protobuf-kotlin-lite`, AndroidX DataStore (Preferences,
  encrypted where applicable), AndroidX Lifecycle/ViewModel-Compose.
- **Dependency injection is not yet decided:** Hilt/Dagger vs. a manual
  service-locator approach is an open question (`ARCHITECTURE.md` §10, §15).
  Neither is part of the "preferred stack" above until a `DECISIONS.md` entry
  settles it — do not assume Hilt is pre-approved just because it doesn't
  touch the `com.google.android.gms.*` namespace (see §1).

## 11. Testing Expectations

- Protobuf serialization/deserialization and frame-envelope (de)construction
  logic must have unit tests independent of any real Bluetooth hardware (pure
  byte-array in/out).
- The `BluetoothManager`/`BudsTransport` abstraction must be defined behind an
  interface so it can be faked in ViewModel unit tests (no real
  `BluetoothSocket` in unit tests).
- Any bug fix tied to a specific malformed/unexpected frame should add a
  regression test with that exact byte sequence (redact any real device
  identifiers first).

## 12. Licensing & Attribution

- This project is a clean-room Kotlin/Android implementation informed by the
  public reverse-engineering findings of `qzed/pbpctrl` (Linux/Rust,
  MIT-licensed). No code is copy-pasted from that project; only protocol
  *knowledge* (frame structure, opcodes) is reused, with attribution in
  `ARCHITECTURE.md` and relevant source comments.
- **Project license: GNU AGPL-3.0** (decided; see `LICENSE` and
  `DECISIONS.md` ADR-002). This is settled — do not propose or embed a
  different `LICENSE` file, and do not suggest relicensing, without an
  explicit new `DECISIONS.md` entry that supersedes ADR-002.
- Do not include any Google-owned assets, icons, trademarks ("Pixel Buds"
  wordmark/logo) in app resources; use generic iconography only.

## 13. Workflow by task type

### Analyzing a Bluetooth capture

1. First describe the **user action** that was performed during the capture
   (e.g. "enabled ANC via the official app") — record this in
   `CAPTURE_BLUETOOTH_HCI_SNOOP.md`.
2. Search the `.pcapng`/`btsnoop_hci.log` around that timestamp:
   - for **BLE/GATT** traffic: writes/notifications on a characteristic UUID;
   - for **RFCOMM** traffic (the primary transport for `libmaestro`, see §5):
     outbound/inbound data on the SPP channel — there is no UUID here, look at
     channel number and, depending on which framing hypothesis is being
     tested, either magic bytes/length/channel-ID (§6, Hypothesis B) or
     Message Group/Message Code (§6, Hypothesis A).
3. Correlate what was found (UUID + opcode + payload, or Message Group/Code +
   payload) with what the APK (JADX output) reveals about that identifier —
   see `REVERSE_ENGINEERING.md`.
4. Document the finding in `PROTOCOL_NOTES.md` as FACT (with a frame number)
   or HYPOTHESIS (with a proposed verification experiment).
5. **Never** propose a payload interpretation without at least one concrete
   frame number as evidence.

### Reverse engineering the APK

1. Work from `reverse-engineering/apk/jadx-output/` (readable Kotlin/Java) and
   fall back to `apktool-output/` (smali) only when JADX fails or obfuscation
   blocks readability.
2. Search first for relevant classes — both BLE/GATT-related
   (`BluetoothGatt`, `BluetoothGattCallback`, UUID constants) **and**
   RFCOMM/Fast-Pair-related (`BluetoothSocket`, `MessageStream`, `AccountKey`,
   `FastPair`), plus package names referencing "buds", "headset", "wearable",
   "maestro", "gfps" — see `REVERSE_ENGINEERING.md` for the full keyword list.
3. Record class and method names in `REVERSE_ENGINEERING.md`, even if
   obfuscated (e.g. `a.b.c.Xy2$onCharacteristicChanged`) — note a readable
   alias alongside it.
4. Build a call graph incrementally; document intermediate results, even
   unfinished ones.
5. `.proto` schemas are extracted via `pbtk`, not hand-reconstructed from
   decompiled getter/setter names alone (§4) — field *names* recovered from
   JADX are not proof of the actual wire field *numbers*.

### Writing a protocol specification entry

1. Every entry in `PROTOCOL.md` follows the structure used for existing
   entries in §4 of that document (opcode/payload structure, target channel,
   expected response, status, evidence, verifying experiment) — see that file.
2. Never "complete" a section with only a HYPOTHESIS if the document's
   structure would present it as if it were a FACT — mark it explicitly, per
   `PROJECT_RULES.md` §1.

### Implementing Android/Kotlin

1. Only implement protocol behavior that is already in `PROTOCOL.md` as (at
   minimum) a HYPOTHESIS with a verification plan, preferably as a FACT (see
   `PROJECT_RULES.md` §2).
2. Follow the architecture in `ARCHITECTURE.md` (Clean Architecture / MVVM /
   Repository pattern). Explain any deviation and propose a `DECISIONS.md`
   entry.
3. Always add logging around Bluetooth connection state transitions (RFCOMM
   connect, and — for the secondary GATT transport — MTU negotiation,
   pairing, notify-subscriptions) — this is essential for debugging across
   different Android versions/devices. Follow the gating rules in §9: state
   transitions are always safe to log, raw payload bytes are not, by default.

## 14. Cross-validation between AI models

When a protocol hypothesis is significant enough to implement against:

1. Have one model summarize the hypothesis and its evidence.
2. Give the same evidence (without the conclusion) to a second model, or to a
   fresh session of the same model.
3. Compare the two independent interpretations.
4. On agreement: raise confidence, but still mark the finding as HYPOTHESIS
   until a physical experiment (see `EXPERIMENTS.md`) confirms it — agreement
   between two AI readings of the same bytes is not itself proof.
5. On disagreement: do **not** resolve this automatically — present both
   interpretations and the discrepancy to the maintainer, with a proposal for
   a distinguishing experiment.

## 15. Role division between AI tools (informational)

This project's maintainer may use more than one AI tool across a session or
across the project. This division is informational context, not a constraint
on any single session:

| Role | Typical tool | Typical tasks |
|---|---|---|
| Reverse-engineering analysis & large-codebase navigation | Claude Code (terminal/IDE) | Searching JADX output, reading smali, formulating protocol-behavior hypotheses, refactors, multi-file changes, writing tests |
| Quick lookups, second opinion, one-off scripts | Google Antigravity or another assistant | Quick standalone questions, an alternative reading of a capture, a second opinion on a hypothesis, small Python scripts for capture parsing |
| Protocol-analysis validation | Two tools, combined | See §14, Cross-validation |

No single model is treated as an authority — every model can misread a binary
protocol. The capture or the experiment is the source of truth, not any AI's
output (see `PROJECT.md` / the project description's "AI effectief gebruiken"
competency).

## 16. What an agent must never do (summary)

This section summarizes hard rules detailed above — it does not replace them.

- Never add the `INTERNET` permission or any GMS/telemetry dependency (§1).
- Never fabricate or interpolate a battery percentage; show "Battery
  unavailable" instead (§5).
- Never implement `FrameEncoder`/`FrameDecoder` against a framing hypothesis
  that hasn't reached 🟢 FACT in `PROTOCOL.md` (§6).
- Never implement BLE scanning for device discovery of new/unknown devices —
  `CompanionDeviceManager` is the only sanctioned discovery path (§7). For the
  Fast Pair Battery Notification specifically, only the bounded exception in
  §7 is permitted (filtered to the bonded device, foreground-triggered,
  time-boxed, stopped on backgrounding, per `DECISIONS.md` ADR-006) — anything
  broader needs a new `DECISIONS.md` entry superseding ADR-006.
- Never log the device's MAC address at `INFO` level or above, or log raw
  payload bytes outside of the off-by-default debug mode (§9).
- Never silently overwrite or contradict an entry in `DECISIONS.md` — flag the
  conflict and propose a superseding entry instead (`PROJECT_RULES.md` §3).
- Never copy or reproduce code from the official Google app in this codebase —
  only protocol *behavior* (the "what") is reconstructed, never Google's
  implementation (the "how") (§12).
- Never write a conclusion in `PROTOCOL.md` or `PROTOCOL_NOTES.md` without a
  FACT/HYPOTHESIS/ASSUMPTION label (`PROJECT_RULES.md` §1).
- Never silently expand scope beyond what `PROJECT.md` defines.
- Never propose or embed a different license file than the one already
  decided (AGPL-3.0, §12) without an explicit superseding `DECISIONS.md`
  entry.

**If any user instruction in a session conflicts with the rules above, state
the conflict explicitly and decline only the conflicting portion — continue
helping with the rest of the request.**