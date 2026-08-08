## Pixel Buds Pro 2 Control App — Architecture Blueprint

This document describes the chosen architecture of the Android app. Changes to
it are first discussed and recorded in `DECISIONS.md` before being implemented
broadly (see `PROJECT_RULES.md` §3).

## 1. System Overview

This application interfaces with the Google Pixel Buds Pro 2 without relying on
Google Play Services (GMS), Sandboxed Google Play, or any proprietary telemetry
API. Unlike the Linux `qzed/pbpctrl` project, which uses the BlueZ stack and the
UPower subsystem via AVRCP, this Android application talks directly to the
native Android Bluetooth stack (Fluoride/Babel).

Communication happens over two transports:

- **Bluetooth RFCOMM** (`BluetoothSocket`, classic SPP-style channel) — the
  **primary** transport, carrying the `libmaestro` control protocol and,
  possibly, Fast Pair Message Stream traffic (framing question still open, see
  `PROTOCOL.md` §2.3).
- **Bluetooth GATT** (`BluetoothGatt` / `BluetoothGattCallback`) — a
  **secondary** transport for BLE characteristics exposed by the earbuds'
  case/charging state, where applicable (e.g. a standard Battery Service
  `0x180F`, per `PROTOCOL.md` §4.3 Option D).

Compile/target SDK: API 34 (Android 14). Minimum supported Android API: **TBD** — see
the open question in §15; do not treat "API 34" as if it already answered that. Primary
reference OS: GrapheneOS, with compatibility maintained for stock AOSP-based ROMs.

```
┌──────────────────────────────────────────────────────┐
│  :ui  (Jetpack Compose, Material 3)                   │
│  - Screens, Composables                               │
│  - ViewModels (MVVM)                                  │
└──────────────────────┬─────────────────────────────────┘
                        │ observes StateFlow<BudsUiState>
┌──────────────────────▼─────────────────────────────────┐
│  :domain                                              │
│  - Use cases (ToggleAncUseCase, ReadBatteryUseCase,   │
│    UpdateEqUseCase, ...)                              │
│  - Domain models (ConnectionState, AncMode, ...)      │
│  - BudsRepository interface                            │
└──────────────────────┬─────────────────────────────────┘
                        │ implementation
┌──────────────────────▼─────────────────────────────────┐
│  :data                                                │
│  - BudsRepositoryImpl                                  │
│  - MaestroSerializer (protobuf), ProtocolCodec         │
│    (FrameEncoder/FrameDecoder)                         │
│  - Encrypted DataStore (EQ presets, last-known battery) │
└──────────────────────┬─────────────────────────────────┘
                        │ implementation
┌──────────────────────▼─────────────────────────────────┐
│  :hardware                                            │
│  - BudsTransport (interface)                            │
│  - RFCOMM socket manager, secondary GATT client         │
│  - ConnectionStateMachine                               │
│  - ForegroundService                                     │
└──────────────────────────────────────────────────────┘
```

## 2. Project Structure (MVVM & Clean Architecture)

Strict unidirectional data flow across four layers, each its own Gradle module
so dependency direction is enforced by the build graph, not just by
convention:

```
:app            -> wires everything together, hosts MainActivity, DI graph
:ui             -> Jetpack Compose screens, Material 3, ViewModels
:domain         -> use cases, StateFlow-based state holders, sealed error/result types
:data           -> Protobuf/Maestro serializer, frame envelope (de)coder, DataStore prefs
:hardware       -> BluetoothManager, GATT/RFCOMM sockets, ForegroundService
```

- **UI Layer** (`:ui`, Jetpack Compose): 100% open-source Material 3
  components. No OEM theme dependencies. Observes `StateFlow` exposed by
  ViewModels; sends user intents (`onAncToggle()`, `onEqChanged(band, value)`)
  down to the domain layer.
- **Domain Layer** (`:domain`): `BudsViewModel` + use-case classes
  (`ToggleAncUseCase`, `ReadBatteryUseCase`, `UpdateEqUseCase`). Owns
  `ConnectionState`, `BatteryStatus`, `AncMode`, `EqProfile` as immutable state
  models, and defines the `BudsRepository` interface (implemented in `:data`;
  see §2.2). Has no Android framework dependency beyond
  `StateFlow`/Coroutines, making it independently unit-testable.
- **Data Layer** (`:data`): Builds/parses `.proto`-defined messages via
  `protobuf-kotlin-lite`, and wraps them in the `libmaestro` byte envelope
  (see §5) via `ProtocolCodec`. Implements `BudsRepositoryImpl`, translating
  transport-level events from `:hardware` into domain models. Also owns local
  persistence (EQ presets, last-known battery) via encrypted AndroidX
  DataStore (decided; see `AGENTS.md` §10 — no open question here).
- **Hardware Layer** (`:hardware`): Owns raw `BluetoothSocket`/`BluetoothGatt`
  objects and the `ForegroundService` that keeps the RFCOMM channel alive
  during active use. Exposes a small interface (`BudsTransport`) so upper
  layers never touch Android BT APIs directly. Contains the
  `ConnectionStateMachine` (§2.1).

Dependency direction: `:ui → :domain → :data → :hardware`. No reverse imports.

### 2.1 Core transport & protocol components

These components are deliberately isolated from the rest of the app so that:

- protocol knowledge lives in one place and is easy to update as reverse
  engineering progresses (see `PROTOCOL.md`, `PROTOCOL_NOTES.md`);
- this layer is independently unit-testable (with a fake/scripted transport)
  without real hardware, per `AGENTS.md` §11.

| Component | Layer | Responsibility |
|---|---|---|
| `BudsTransport` | `:hardware` (interface consumed by `:data`) | Abstracts the underlying RFCOMM `BluetoothSocket` (primary) and, where applicable, `BluetoothGatt` (secondary — case/charging characteristics). Upper layers see only `send(frame: ByteArray)` / an inbound `Flow<ByteArray>`, never raw Android BT types. |
| `ConnectionStateMachine` | `:hardware` | Explicit state machine (`Disconnected → Connecting → Discovering → Ready → ...`) driving `ConnectionState`. States and transitions must match what's actually observed in captures — see the connection lifecycle in `PROTOCOL.md` §5, which is still an ⚪ ASSUMPTION pending a full end-to-end capture. |
| `ProtocolCodec` (`FrameEncoder` / `FrameDecoder`) | `:data` | Encodes/decodes commands and responses per `PROTOCOL.md` §2 (framing) and §3 (protobuf). Pure Kotlin, no Android dependencies — fully unit-testable against fixed byte-array fixtures, per `AGENTS.md` §11. Must be implemented against whichever framing hypothesis (`PROTOCOL.md` §2.1 vs §2.2) reaches 🟢 FACT confidence — not against placeholders (see `PROTOCOL_NOTES.md` §8). |
| `BudsRepository` / `BudsRepositoryImpl` | interface in `:domain`, implementation in `:data` | Translates protocol-level events into domain models, exposed as `Flow`/`StateFlow` to the domain layer. |

> **Implementation gate for `ProtocolCodec` (coupled to `AGENTS.md` §6):** the
> same event — `PROTOCOL.md` §2's framing question reaching 🟢 FACT confidence
> — triggers two linked requirements, not two independent ones to satisfy
> separately: (1) only then may `FrameEncoder`/`FrameDecoder` be implemented
> (`AGENTS.md` §6), and (2) that same FACT determination must be recorded as a
> `DECISIONS.md` ADR before implementation begins. If the ADR and
> `PROTOCOL.md`'s status ever disagree, treat that disagreement itself as the
> problem to fix — not a reason to add a third, independent check.
>
> If the resolved answer turns out to be "both channels exist" (i.e.
> `libmaestro` control commands use a separate envelope from generic Fast Pair
> Message Stream traffic like Ring/battery/device info), `ProtocolCodec` will
> need **two separate codecs** rather than one shared implementation — the
> same ADR should say so. This does not change the external interface to
> `:domain`, only `:data`'s internal structure.

## 3. Dataflow (Command Pipeline)

Example: **"Activate Transparency Mode"**

1. **UI Event** — user taps "Transparency" in `BudsScreen.kt`.
2. **Domain Intent** — `BudsViewModel.onAncModeSelected(AncMode.TRANSPARENCY)`
   invokes `ToggleAncUseCase`, which calls `BudsRepository`.
3. **Serialization** — `:data`'s `MaestroSerializer` builds
   `Maestro.AncCommand.newBuilder().setMode(TRANSPARENCY).build().toByteArray()`.
4. **Framing** — `ProtocolCodec`/`FrameEncoder` wraps the protobuf bytes in the
   `libmaestro` envelope (magic bytes + length + channel ID + optional
   checksum, or Message Stream framing — see §5).
5. **Transmission** — `:hardware`'s `BudsTransport.send(frame: ByteArray)`
   writes to the `BluetoothSocket` `OutputStream` on `Dispatchers.IO`.
6. **Acknowledgement / State Update** — an inbound frame (or timeout) resolves
   a pending coroutine `Deferred`; on success, `MutableStateFlow<AncMode>` is
   updated via `BudsRepositoryImpl`, which Compose observes and recomposes
   automatically. On failure, a `BudsError` propagates up (§7) and the UI shows
   a retry affordance instead of silently failing.

## 4. Battery Status Logic (Android Fallback)

Android has no equivalent of Linux's BlueZ/UPower/AVRCP battery reporting, so
this layer uses Android-native fallbacks, in priority order (aligned with
`PROTOCOL.md` §4.3):

1. **Primary — Fast Pair Battery Notification (BLE advertisement):** parse the
   officially specified 3-byte (L/R/Case) payload from the BLE advertisement.
   No active connection required. Implement observation of this advertisement
   per the bounded scanning policy in §9.1 — filtered, foreground-triggered,
   time-boxed; never a continuous background scan.
2. **Secondary — RFCOMM Fast Pair Message Stream "Device Information":** once
   connected, read battery via the Message Stream, if/once the exact battery
   message code is confirmed (`PROTOCOL.md` §4.3 Option B).
3. **Tertiary — HFP AT commands:** `BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT`,
   parsing `AT+IPHONEACCEV` / `AT+XAPL` vendor events surfaced by the HFP
   profile proxy.
4. **Last resort — GATT:** if the earbuds expose a standard `Battery Service
   (0x180F)` GATT characteristic for the case, read it directly via
   `BluetoothGatt.readCharacteristic()`.

If none of the above report a value, the UI shows "Battery unavailable" — the
app never fabricates or carries over a stale percentage silently; stale values
are visually marked ("last known: 3 min ago").

> Note: this priority order changed from an earlier assumption of a
> proprietary `libmaestro` query being primary — see `PROTOCOL_NOTES.md` §4.3
> for the reasoning (the Fast Pair mechanisms are officially specified and, for
> the BLE advertisement option, require no active connection at all).

## 5. Protocol Framing & `libmaestro` Envelope (Data Layer Detail)

Producing the protobuf byte array is only step one. Per the `qzed/pbpctrl`
reverse-engineering findings and the official Fast Pair specification, two
competing framing hypotheses are under evaluation — see `PROTOCOL.md` §2 for
the full byte-level detail of each:

```
Hypothesis A — Fast Pair Message Stream framing (official, generic):
+-----------------+----------------+----------------------------+------------------+
| Message Group    | Message Code   | Additional Data Length     | Additional Data  |
| (1B)             | (1B)           | (2B, big-endian)           | (variable)       |
+-----------------+----------------+----------------------------+------------------+

Hypothesis B — proprietary envelope (pbpctrl-derived):
+-----------+--------------+------------+-------------------+-----------------+
| Magic (2B)| Length (2B)  | Channel ID | Protobuf Payload   | Checksum (opt.) |
+-----------+--------------+------------+-------------------+-----------------+
```

- **Framing (outbound):** `FrameEncoder` builds the frame per whichever
  hypothesis is confirmed, appends a checksum only if that hypothesis requires
  one, and hands the resulting bytes to `BudsTransport`.
- **Parsing (inbound):** `FrameDecoder` buffers incoming bytes (RFCOMM streams
  are not message-delimited at the socket level), detects the frame boundary
  per the confirmed hypothesis, extracts exactly the declared payload bytes,
  verifies a checksum if present, and only then hands the inner protobuf bytes
  to the deserializer.
- Any framing mismatch (bad magic/group, length overrun, checksum failure)
  yields a `BudsError.MalformedFrame` — logged locally and dropped, never
  surfaced as a crash.
- Exact byte offsets/opcodes per command are tracked in `PROTOCOL.md` /
  `PROTOCOL_NOTES.md` alongside a reference to the corresponding `pbpctrl`
  source file, so protocol knowledge stays auditable and versioned
  independently of this document.

**Implementation gate:** `FrameDecoder`/`FrameEncoder` must not be implemented
against a placeholder framing — see `PROTOCOL.md` §2.3 for the resolution path
for the open framing question, and `PROTOCOL_NOTES.md` §8 for the
implementation ordering this depends on.

## 6. Bluetooth Resilience & GrapheneOS Degradation

GrapheneOS enforces aggressive security/battery policies, including automatic
Bluetooth deactivation on lock or inactivity. The architecture treats the
physical link as inherently unstable:

- **Connection Lifecycle:** `BudsTransport`/`ConnectionStateMachine`
  continuously observes `BluetoothAdapter.ACTION_STATE_CHANGED`; `STATE_OFF` is
  treated as a normal transition.
- **Graceful Degradation:** an `IOException` from the socket (OS-triggered
  teardown, range loss, peer disconnect) is caught, `ConnectionState` moves to
  `DISCONNECTED`, and all in-flight ViewModel polling coroutines are
  cancelled to avoid leaks or crash loops.
- **Re-connection Strategy:** user-initiated reconnection only — no aggressive
  background polling/retry loops, both to respect battery and to avoid the
  fingerprintable scanning behavior GrapheneOS's threat model discourages (see
  §7 of `AGENTS.md`, and the bounded exception for passive battery-advertisement
  observation in §9.1 below).

## 7. Error Handling Architecture

A shared sealed hierarchy (`:domain`) is used across layers instead of raw
exceptions crossing module boundaries:

```kotlin
sealed class BudsError {
    data object ConnectionLost : BudsError()
    data object Timeout : BudsError()
    data class MalformedFrame(val raw: ByteArray) : BudsError()
    data object UnsupportedFirmware : BudsError()
    data object PermissionDenied : BudsError()
    data class Unknown(val cause: Throwable) : BudsError()
}
```

`:hardware` and `:data` convert all caught exceptions into this type; `:domain`
exposes `StateFlow<BudsUiState>` where `BudsUiState` includes an optional
`BudsError` so the Compose layer can render a specific, actionable message per
failure mode rather than a generic error banner.

## 8. Firmware / Protocol Compatibility

Because `libmaestro`'s wire format can change across Pixel Buds firmware
revisions, each `.proto` file and each entry in `PROTOCOL_NOTES.md` carries the
firmware/library version it was verified against. `UnsupportedFirmware` (§7)
is returned when an inbound frame doesn't match any known schema version,
rather than attempting a best-effort parse that could misreport battery/ANC
state.

## 9. Security & Permission Architecture

- **Zero location tracking:** the manifest declares
  `android:usesPermissionFlags="neverForLocation"` on `BLUETOOTH_SCAN`;
  `BLUETOOTH_PRIVILEGED` and any `ACCESS_*_LOCATION` permission are never
  requested.
- **Companion Device Manager (CDM):** initial pairing/discovery uses
  `CompanionDeviceManager` (API 26+) instead of custom BLE scanning — this
  delegates the scan UI to the OS and grants the app access only to the
  explicitly selected device. See `DECISIONS.md` ADR-005.
- **Local state persistence:** user preferences (custom EQ profiles, last
  known battery) are stored via encrypted AndroidX DataStore. Nothing is ever
  transmitted off-device (see `AGENTS.md` §1 and §9 for the enforcement
  rules).
- **Threat model summary:** the app assumes a privacy-conscious user on a
  hardened OS; it minimizes fingerprintable behavior (no continuous scanning
  for device *discovery*), minimizes permissions, and keeps all diagnostic
  data local and opt-in.

### 9.1 Resolved: passive scanning policy for the Fast Pair Battery Notification

`AGENTS.md` §7 bans continuous background BLE scanning for **device
discovery**. The Fast Pair Battery Notification mechanism (§4, `PROTOCOL.md`
§4.3 Option A) is a different purpose — passively observing an advertisement
from an *already-bonded, known* device to get battery updates without an
active RFCOMM connection.

**Decided (see `DECISIONS.md` ADR-006):** this is permitted, but only as a
narrow, bounded exception, not as general-purpose scanning — filtered to the
already-bonded device, triggered only by a user-visible event rather than a
background timer, time-boxed to roughly the advertisement's own visibility
window, and stopped as soon as the app leaves the foreground.

The authoritative rule text agents must follow — including the exact
filtering, triggering, and timing bounds — lives in `AGENTS.md` §7. This
section exists only to explain *why* the exception exists architecturally; it
deliberately does not restate the specific bounds a second time, so there is
nothing here that can drift out of sync with §7 (`PROJECT_RULES.md` §2).

## 10. Dependency Injection

Preference: **Hilt** (if there is no objection to the Dagger/Hilt dependency —
note Hilt/Dagger does **not** touch the `com.google.android.gms.*` namespace
and does not itself require Google Play Services, so it does not conflict with
the Zero-GMS rule in `AGENTS.md` §1) — or a manual DI approach via a light
service locator, if full independence from Google-authored tooling is also
desired for the build itself.

**This is an open question, not yet decided** — see §13. Whichever option is
chosen must be justified per the dependency policy in `AGENTS.md` §10
(justification, no network/analytics SDK bundled transitively, pinned
versions) and recorded in `DECISIONS.md` before broad adoption.

## 11. State Management

- Kotlin `StateFlow`/`SharedFlow` for all reactive state, per `AGENTS.md` §3.
- `LiveData` is not used in this project (decided; superseded by the
  Compose-first, coroutines-based approach — see `AGENTS.md` §3). This is not
  an open question.

## 12. Logging

- A light logging abstraction (e.g. `BleLogger`) writes to `Logcat` and,
  optionally, to a local in-app ring buffer for an "Export debug log" feature
  — never off-device (see `AGENTS.md` §9). This is essential because BLE
  timing issues are often only reproducible with full logs.
- **Always safe to log:** connection state transitions, MTU value, connection
  parameters.
- **Gated behind an explicit, off-by-default "Debug mode" setting:** raw
  sent/received frame bytes (hex dump) — per `AGENTS.md` §9, verbose
  hex-dump logging of payloads containing device identifiers is not on by
  default.
- **Never logged at `INFO` level or above:** the paired device's MAC address —
  use a truncated/hashed form if a log line needs to disambiguate devices, per
  `AGENTS.md` §9.

## 13. Testing Strategy

- **Unit tests** (`:data`): `ProtocolCodec` (protobuf (de)serialization and
  frame envelope encode/decode) and domain-layer use cases, using fixed
  byte-array fixtures — no real hardware required, no Android dependencies
  needed (pure Kotlin + JUnit5/Kotest, per `AGENTS.md` §11).
- **Unit tests** (`:domain`): ViewModels/use-cases tested against a fake
  `BudsTransport` implementation that emits scripted frames/errors.
- **Instrumented tests** (`:hardware`, optional/manual): `GattConnectionManager`/
  RFCOMM socket handling against a local BLE mock/simulator where possible;
  otherwise a manual test plan (see `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`) against
  real hardware, since no real earbuds are available in a CI runner.
- Per `AGENTS.md` §11, any bug fix tied to a specific malformed/unexpected
  frame adds a regression test with that exact byte sequence (device
  identifiers redacted first).

## 14. Build Configuration Notes

- Kotlin + Jetpack Compose BOM, Gradle version catalog (`libs.versions.toml`)
  for pinned dependency versions.
- `protobuf-kotlin-lite` via the Gradle Protobuf plugin, `.proto` sources
  under `data/src/main/proto/`.
- No `INTERNET` permission anywhere in any module's manifest; a CI/lint check
  should assert this remains true (see `AGENTS.md` §1).

## 15. Open Architecture Questions

> Move to `DECISIONS.md` once decided, following the ADR template.

- [ ] Hilt vs. manual DI (§10).
- [ ] Support for multiple paired Buds simultaneously (multi-device) — in or
      out of scope for v1? Not currently addressed anywhere in `PROJECT.md`'s
      v1 scope list.
- [ ] Minimum supported Android API level: target/compile SDK is set at API 34
      (Android 14+), but the minimum SDK for broader AOSP-ROM compatibility is
      not yet fixed — depends on which BLE/Bluetooth APIs (e.g.
      `CompanionDeviceManager` features, foreground service types) are
      actually required.

> Already decided, not open: persistent settings storage (encrypted AndroidX
> DataStore — see §2 and `AGENTS.md` §10); state management approach
> (`StateFlow`/`SharedFlow` only — see §11); passive BLE scanning policy for
> the Fast Pair Battery Notification (bounded exception — see §9.1,
> `DECISIONS.md` ADR-006).

## 16. Attribution

Protocol structure knowledge is derived from the public reverse-engineering
work of the `qzed/pbpctrl` project (Linux/Rust). No source code from that
project is reused directly; only documented protocol/frame knowledge informs
this Android-native implementation.