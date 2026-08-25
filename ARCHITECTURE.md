## OpenControl for Pixel Buds Pro 2 — Architecture Blueprint

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
  **primary** transport. Three DLCIs coexist on it, each with independent
  framing (`PROTOCOL.md` §2.3, `CodecRouter` in §5 below): DLCI 0x04 (official
  Fast Pair Message Stream, 🟢 FACT), DLCI 0x02 (Pigweed `pw_hdlc`, 🟡
  HYPOTHESIS that this is specifically `libmaestro`), and DLCI 0x08 (a private
  envelope whose protocol identity is 🔴 still open).
- **Bluetooth GATT** (`BluetoothGatt` / `BluetoothGattCallback`) — a
  **secondary** transport for BLE characteristics exposed by the earbuds'
  case/charging state, where applicable (e.g. a standard Battery Service
  `0x180F`, per `PROTOCOL.md` §4.3 Option D).
- **Bluetooth HID** — 🟡 HYPOTHESIS, added 2026-08-14, not yet functionally investigated.
  `CAP-002-FINDINGS.md` §6 observed HID-Control and HID-Interrupt L2CAP channels opened during
  SDP (frames 837–869), alongside an "Input device" toggle in the official app's Device details
  screen. Plausible role: touch/head gestures surfaced as generic HID reports rather than
  proprietary RFCOMM traffic — not confirmed, no HID report content has been captured or decoded.
  Not treated as a confirmed transport for this app's own use until decoded.

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
│  - MaestroSerializer (protobuf), CodecRouter           │
│    (per-DLCI FrameEncoder/FrameDecoder: 0x02/0x04/0x08) │
│  - Encrypted DataStore (EQ presets, last-known battery) │
└──────────────────────┬─────────────────────────────────┘
                        │ consumes BudsTransport(channelId)
┌──────────────────────▼─────────────────────────────────┐
│  :hardware                                            │
│  - BudsTransport (interface, channelId-aware)           │
│  - RFCOMM socket manager, secondary GATT client         │
│  - ConnectionStateMachine                               │
│  - ForegroundService                                     │
└──────────────────────────────────────────────────────┘
```

## 2. Project Structure (MVVM & Clean Architecture)

`:domain` is the isolated center of the module graph, per Clean Architecture's
dependency-inversion principle: it has no dependency on `:ui`, `:data`, or
`:hardware`, and defines the interfaces (`BudsRepository`) that outer layers
implement or consume. Each layer is its own Gradle module so this direction is
enforced by the build graph, not just by convention:

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
  `protobuf-kotlin-lite`, and wraps them in the appropriate per-DLCI byte
  envelope (see §5) via `CodecRouter`. Implements `BudsRepositoryImpl`, translating
  transport-level events from `:hardware` into domain models. Also owns local
  persistence (EQ presets, last-known battery) via encrypted AndroidX
  DataStore (decided; see `AGENTS.md` §10 — no open question here).
- **Hardware Layer** (`:hardware`): Owns raw `BluetoothSocket`/`BluetoothGatt`
  objects and the `ForegroundService` that keeps the RFCOMM channel alive
  during active use. Exposes a small interface (`BudsTransport`) so upper
  layers never touch Android BT APIs directly. Contains the
  `ConnectionStateMachine` (§2.1).

Dependency direction: `:ui → :domain ← :data → :hardware`. `:ui` depends on
`:domain` to observe state and invoke use cases. `:data` depends on `:domain`
(to implement `BudsRepository`) and on `:hardware` (to consume `BudsTransport`
— see §2.1). `:domain` imports nothing from the other three; no module
imports "backwards" against these arrows. `:app` is the composition root: it
depends on all four and wires concrete implementations to interfaces via DI
(§10).

### 2.1 Core transport & protocol components

These components are deliberately isolated from the rest of the app so that:

- protocol knowledge lives in one place and is easy to update as reverse
  engineering progresses (see `PROTOCOL.md`);
- this layer is independently unit-testable (with a fake/scripted transport)
  without real hardware, per `AGENTS.md` §11.

| Component | Layer | Responsibility |
|---|---|---|
| `BudsTransport` | `:hardware` (interface consumed by `:data`) | Abstracts the underlying RFCOMM `BluetoothSocket` (primary) and, where applicable, `BluetoothGatt` (secondary — case/charging characteristics). Upper layers see only `send(channelId: Int, frame: ByteArray)` / an inbound `Flow<Pair<channelId: Int, frame: ByteArray>>`, never raw Android BT types. `channelId` addresses one of the three coexisting RFCOMM DLCIs (`PROTOCOL.md` §2.3) — it is not optional, since the three channels have independent framing and cannot share one send/receive path. |
| `ConnectionStateMachine` | `:hardware` | Explicit state machine (`Disconnected → Connecting → Discovering → Ready → ...`) driving `ConnectionState`. States and transitions must match what's actually observed in captures — see the connection lifecycle in `PROTOCOL.md` §5, which is still an ⚪ ASSUMPTION pending a full end-to-end capture. |
| `CodecRouter` (per-DLCI `FrameEncoder` / `FrameDecoder`) | `:data` | Routes each inbound `(channelId, bytes)` pair from `BudsTransport` to the codec for that DLCI, and routes each outbound command to the codec for whichever DLCI it belongs on. See §5 for the three channels and their framing. Pure Kotlin, no Android dependencies — fully unit-testable against fixed byte-array fixtures, per `AGENTS.md` §11. Each per-DLCI codec is implemented independently, gated on that DLCI's own framing reaching 🟢 FACT confidence (see §5's implementation gate) — one DLCI's codec is never blocked on another's. |
| `BudsRepository` / `BudsRepositoryImpl` | interface in `:domain`, implementation in `:data` | Translates protocol-level events into domain models, exposed as `Flow`/`StateFlow` to the domain layer. |

## 3. Dataflow (Command Pipeline)

Example: **"Activate Transparency Mode"**

1. **UI Event** — user taps "Transparency" in `BudsScreen.kt`.
2. **Domain Intent** — `BudsViewModel.onAncModeSelected(AncMode.TRANSPARENCY)`
   invokes `ToggleAncUseCase`, which calls `BudsRepository`.
3. **Serialization** — `:data`'s `MaestroSerializer` builds
   `Maestro.AncCommand.newBuilder().setMode(TRANSPARENCY).build().toByteArray()`.
4. **Framing** — `CodecRouter` picks the `FrameEncoder` for the DLCI this
   command belongs on (per `PROTOCOL.md` §2.3 — see §5) and wraps the
   protobuf bytes in that channel's envelope.
5. **Transmission** — `:hardware`'s `BudsTransport.send(channelId: Int, frame:
   ByteArray)` writes to the `BluetoothSocket` `OutputStream` on
   `Dispatchers.IO`.
6. **Acknowledgement / State Update** — an inbound frame (or timeout) resolves
   a pending coroutine `Deferred`; on success, `MutableStateFlow<AncMode>` is
   updated via `BudsRepositoryImpl`, which Compose observes and recomposes
   automatically. On failure, a `BudsError` propagates up (§7) and the UI shows
   a retry affordance instead of silently failing.

### 3.1 State Reconciliation (Hardware Is the Source of Truth)

Local `StateFlow` values (`AncMode`, `EqProfile`, connection-scoped state) are
a **cache of the hardware's last-known state, never the authority on it.**
This matters because the app is not the only writer: the official Pixel Buds
app, another paired host, or the hardware's own buttons/touch controls can
change state while this app is disconnected or backgrounded.

- **On every (re)connection**, before trusting or displaying any locally
  cached value, the app queries the actual current state from the hardware
  (ANC mode, battery, EQ, touch-control config) rather than assuming the
  last-known `StateFlow` value still holds. This follows the Startup Handshake
  (§8) — the state query happens only after firmware verification succeeds.
- Locally cached values are marked provisional/stale (§4's "last known: N min
  ago" pattern) until reconciled against a fresh read.
- If a fresh read disagrees with the cached value, the fresh read wins
  unconditionally — the app never keeps showing (or acting on) its own stale
  assumption once the hardware has answered.
- A user-initiated write (e.g. toggling ANC) optimistically updates local
  state for responsiveness, but that optimistic update is provisional until
  the acknowledgement in step 6 above confirms it — same rule, applied to the
  single-command case.

## 4. Battery Status Logic (Android Fallback)

Android has no equivalent of Linux's BlueZ/UPower/AVRCP battery reporting, so
this layer uses Android-native fallbacks, in priority order (aligned with
`PROTOCOL.md` §4.3):

0. **Worth checking first, cheapest — generic OS battery broadcast:**
   register a `BroadcastReceiver` for `BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED`
   (API 31+) or the legacy `android.bluetooth.device.action.BATTERY_LEVEL_CHANGED`
   intent. This costs nothing to implement (no scan permission, no RFCOMM
   connection) and may already surface whatever the OS's own Bluetooth stack
   derives from options 1–4 below — but whether it actually fires for this
   device is unconfirmed (⚪ ASSUMPTION), so it supplements rather than
   replaces the confirmed options that follow. If it works reliably in
   testing, it can be promoted above option 1.
1. **Primary — Fast Pair Battery Notification (BLE advertisement):** parse the
   officially specified 3-byte (L/R/Case) payload from the BLE advertisement.
   No active connection required. Implement observation of this advertisement
   per the bounded scanning policy in §9.1 — filtered, foreground-triggered,
   time-boxed; never a continuous background scan.
2. **Secondary — RFCOMM Fast Pair Message Stream "Device Information":** once
   connected, read battery via the Message Stream, if/once the exact battery
   message code is confirmed (`PROTOCOL.md` §4.3 Option B).
3. **Tertiary — HFP AT commands (confirmed active, `PROTOCOL.md` §4.3 Option
   C):** `BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT`, parsing the
   standard `AT+BIEV` HF Indicator #2 (Battery Level) and/or `AT+CIND`
   `battchg` events surfaced by the HFP profile proxy — **not** Apple's
   `AT+IPHONEACCEV`/`AT+XAPL` (an earlier, incorrect assumption; those are
   iPhone-vendor-specific commands the Buds Pro 2 do not use). Unlike Options
   0/A/B, this mechanism **pushes periodically** (~6–7s) rather than only on
   change — see `PROTOCOL.md` §4.3 for the event-driven-vs-periodic split.
4. **Last resort — GATT:** if the earbuds expose a standard `Battery Service
   (0x180F)` GATT characteristic for the case, read it directly via
   `BluetoothGatt.readCharacteristic()`.

If none of the above report a value, the UI shows "Battery unavailable" — the
app never fabricates or carries over a stale percentage silently; stale values
are visually marked ("last known: 3 min ago").

> Note: this priority order changed from an earlier assumption of a
> proprietary `libmaestro` query being primary — see `PROTOCOL.md` §4.3
> for the reasoning (the Fast Pair mechanisms are officially specified and, for
> the BLE advertisement option, require no active connection at all).

## 5. Protocol Framing & the Three-DLCI Reality (`CodecRouter`, Data Layer Detail)

Producing the protobuf byte array is only step one. Framing is no longer a
binary either/or choice between two competing hypotheses — `PROTOCOL.md` §2.3
establishes that **three RFCOMM DLCIs coexist**, each with its own framing,
and `CodecRouter` dispatches to the right one by `channelId`:

```
DLCI 0x04 — official Fast Pair Message Stream framing (🟢 FACT, spec-verified):
+-----------------+----------------+----------------------------+------------------+
| Message Group    | Message Code   | Additional Data Length     | Additional Data  |
| (1B)             | (1B)           | (2B, big-endian)           | (variable)       |
+-----------------+----------------+----------------------------+------------------+
Carries Device Information, SASS, and the Hearable Controls extension
(Get/Set/Notify ANC state) — see `PROTOCOL.md` §4.1.

DLCI 0x02 — Pigweed `pw_hdlc` framing (🟢 FACT for the framing itself; 🟡
HYPOTHESIS that this is specifically `libmaestro`):
+------+-------------------------+---------+-------------------+------+
| Flag | Address (LEB128 varint) | Control | Payload (protobuf)| Flag |
+------+-------------------------+---------+-------------------+------+
**Address field is per-connection-negotiated, not a small fixed set — do not
hardcode it.** `0x00`/`0xD180` were the first two addresses observed, but a
2026-08-17 cross-capture pass (`DESKRESEARCH_FINDINGS.md`, `PROTOCOL.md` §2.2a/§6)
found two more pairs (`0x1e80`/`0x2680` Sent, answered by `0xe980` Rcvd)
appearing at connection-(re)open/channel-bounce events and carrying the same
content as the original pair. `FrameDecoder` for this DLCI must treat the
Address field as dynamically assigned per connection/reconnect, not match
against a fixed allowlist of known values. Payload content only partly decoded
(device serial + firmware on the Rcvd side) — see `PROTOCOL.md` §2.2a.

DLCI 0x08 — private `[Group][Code][Length][Value]` envelope (🟢 FACT that
it's a real, decodable envelope; 🔴 OPEN QUESTION what protocol it belongs to):
+-----------+----------------+--------------+-------------------+
| Group (1B)| Code (1B)      | Length (2B)  | Value (variable)  |
+-----------+----------------+--------------+-------------------+
Structurally decodable, but Group/Code meanings are largely unmapped — see
`PROTOCOL.md` §2.3.
```

- **Routing (outbound):** for each command, `CodecRouter` selects the
  `FrameEncoder` for the DLCI that command's protocol entry (`PROTOCOL.md`)
  specifies, builds the frame per that DLCI's envelope, appends a checksum
  only if that envelope requires one, and hands the resulting bytes to
  `BudsTransport.send(channelId, frame)`.
- **Routing (inbound):** each DLCI's `FrameDecoder` buffers incoming bytes for
  that channel (RFCOMM streams are not message-delimited at the socket
  level), detects the frame boundary per that channel's envelope, extracts
  exactly the declared payload bytes, verifies a checksum if present, and
  hands the inner bytes to the matching deserializer.
- Any framing mismatch (bad magic/group, length overrun, checksum failure)
  yields a `BudsError.MalformedFrame` — logged locally and dropped, never
  surfaced as a crash. This is distinct from a **structurally valid** frame
  whose Group/Code isn't recognized (most of DLCI 0x08 today): that case does
  not fail to parse, it fails to be *understood* — see §7's
  `UnidentifiedFrame`, which is routed to a Debug UI instead of being dropped
  silently, since silently dropping unclassified wire data would work against
  this project's evidence-based reverse-engineering goal (`AGENTS.md` §6).
- Exact byte offsets/opcodes per command are tracked in `PROTOCOL.md`
  alongside a reference to the corresponding `pbpctrl` source file where
  applicable, so protocol knowledge stays auditable and versioned
  independently of this document.

**Implementation gate, per DLCI:** a given DLCI's `FrameEncoder`/`FrameDecoder`
may only be implemented once that DLCI's own framing (not the other two's)
reaches 🟢 FACT confidence in `PROTOCOL.md` §2.3, and that FACT determination
is recorded as a `DECISIONS.md` ADR before implementation begins (coupled to
`AGENTS.md` §6). As of this writing: DLCI 0x04 and DLCI 0x02 framing are 🟢
FACT and implementable; DLCI 0x08's envelope shape is 🟢 FACT and its codec is
implementable, but *acting on* payloads whose Group/Code is unmapped is not —
those surface as `UnidentifiedFrame` instead (§7).

## 6. Bluetooth Resilience & GrapheneOS Degradation

GrapheneOS enforces aggressive security/battery policies, including automatic
Bluetooth deactivation on lock or inactivity. The architecture treats the
physical link as inherently unstable:

- **Connection Lifecycle:** `BudsTransport`/`ConnectionStateMachine`
  continuously observes `BluetoothAdapter.ACTION_STATE_CHANGED`; `STATE_OFF` is
  treated as a normal transition.
- **Graceful Degradation:** an `IOException` from the socket (OS-triggered
  teardown, range loss, peer disconnect) is caught, `ConnectionState` moves to
  `DISCONNECTED`, and all in-flight ViewModel event-observation coroutines
  (see below) are cancelled to avoid leaks or crash loops.
- **Re-connection Strategy:** user-initiated reconnection only — no aggressive
  background retry loops, both to respect battery and to avoid the
  fingerprintable scanning behavior GrapheneOS's threat model discourages (see
  §7 of `AGENTS.md`, and the bounded exception for passive battery-advertisement
  observation in §9.1 below).
- **Event-observation, not polling:** all inbound state (ANC mode, battery,
  connection status) is obtained by observing a `Flow` fed by inbound frames
  or OS broadcasts (`ACTION_STATE_CHANGED`, the battery broadcast in §4
  option 0, GATT notifications) — coroutines suspend until an event arrives.
  Nothing in this app runs a fixed-interval timer loop that re-reads state on
  a schedule; the only place anything resembling a schedule appears is the
  bounded, foreground-triggered advertisement window in §9.1, which is
  time-boxed by design, not a recurring poll.

### 6.1 Resource Budget (Wakelocks)

- The `ForegroundService` (§1) holds a wakelock only while a command is
  in-flight or an event-observation coroutine actively needs the CPU awake to
  process an inbound frame — never for the lifetime of the connection.
- No wakelock is acquired merely to keep the RFCOMM socket open; the socket
  itself does not require the CPU to stay awake, only active
  transmission/reception does.
- Any wakelock acquired must have a bounded timeout as a backstop (in case a
  release path is missed due to an unexpected exception), in addition to being
  released explicitly on the normal completion path.
- This budget exists because GrapheneOS's aggressive battery policy (this
  section's heading) will fight an app that holds wakelocks liberally, and
  because unnecessary wakelocks are themselves a fingerprintable/battery-drain
  concern independent of the scanning concern §7 of `AGENTS.md` already
  covers.

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

**`UnidentifiedFrame` is deliberately not part of `BudsError`.** A frame with
an unrecognized Group/Code (typically on DLCI 0x08, §5) parsed successfully —
nothing failed — so treating it as an error would misrepresent it to the user
as a problem, and routing it through the error-banner path would bury data
useful to reverse engineering. Instead:

```kotlin
data class UnidentifiedFrame(
    val channelId: Int,
    val group: Int?,
    val code: Int?,
    val raw: ByteArray,
    val timestamp: Instant,
)
```

`CodecRouter` (§5) emits these on a separate `Flow<UnidentifiedFrame>` exposed
by `BudsRepository`, independent of the normal command/state pipeline. A
Debug UI screen (gated behind the same "Debug mode" setting as raw frame
logging, §12) subscribes to this flow so unclassified wire data is visible
and inspectable rather than silently dropped — consistent with the
evidence-based reverse-engineering principle in `AGENTS.md` §6/`PROJECT_RULES.md`
§1.

## 8. Firmware / Protocol Compatibility

Because `libmaestro`'s wire format can change across Pixel Buds firmware
revisions, each `.proto` file and each entry in `PROTOCOL.md` carries the
firmware/library version it was verified against. `UnsupportedFirmware` (§7)
is returned when an inbound frame doesn't match any known schema version,
rather than attempting a best-effort parse that could misreport battery/ANC
state.

### 8.1 Startup Handshake

Before sending any state-changing command on a new connection, the app reads
the firmware version string first (via DLCI 0x02's Rcvd block or DLCI 0x04's
Device Information, whichever is confirmed to carry it for a given
device/firmware — see `PROTOCOL.md` §2.2a/§4.1) and checks it against the set
of firmware versions this app has been verified against.

- **Verified firmware:** proceed normally — Startup Handshake feeds directly
  into the state query described in §3.1.
- **Unrecognized/unverified firmware:** fall back to **Read-Only / Safe
  Mode** — the app displays whatever state it can read (battery, current ANC
  mode if obtainable) but sends no write/control command. An unknown firmware
  revision may have changed command semantics in a way this app cannot detect
  in advance, and sending a command built against the wrong schema risks
  putting the hardware into an unexpected or unrecoverable state. This is a
  deliberate, conservative default to avoid bricking — see `README.md`'s
  bricking disclaimer and `WORKSTATION_PREPARATIONS.md`'s disaster-recovery
  procedure.
- Safe Mode is surfaced to the user explicitly (not a silent limitation) so
  they understand why controls are unavailable, and the detected firmware
  string is logged (subject to §12's logging rules) to make it easy to report
  and later add support for.

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

- **Unit tests** (`:data`): `CodecRouter` (protobuf (de)serialization and
  per-DLCI frame envelope encode/decode) and domain-layer use cases, using fixed
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
- [ ] Minimum supported Android API level: compile/target SDK is set at API 34
      (Android 14), but the minimum SDK for broader AOSP-ROM compatibility is
      not yet fixed. This is not blocked on a single API, so "TBD" here does
      not mean unplanned: the generic battery broadcast (§4 option 0,
      `BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED`) requires **API 31+**,
      but §4's options 1–4 (Fast Pair advertisement, Message Stream, HFP,
      GATT) are the confirmed primary paths and do not depend on API 31 — so a
      lower min API (e.g. API 26 for `CompanionDeviceManager`, ADR-005) is not
      blocked by option 0's availability, which simply degrades gracefully to
      "unavailable on this API level, other options still work." The exact
      floor still depends on which other BLE/Bluetooth APIs (foreground
      service types, etc.) turn out to be required.
- [ ] Added 2026-08-14: is the observed Bluetooth HID surface (§1) architecturally relevant to
      `:hardware`/`BudsTransport` — i.e. does any control feature this app needs actually route
      through HID reports rather than RFCOMM/GATT — or is it exclusively used by parts of the
      official app/OS this project doesn't need to replicate? Unresolved; no HID report content
      has been captured yet. **If confirmed**, `BudsTransport` (§2.1) needs a third input path
      alongside RFCOMM and GATT (an `InputManager`/HID report listener), and `CodecRouter` (§5)
      would need an equivalent HID report decoder — this is a real, not cosmetic, change to both
      components, which is why this item stays open rather than being assumed away.

> Already decided, not open: persistent settings storage (encrypted AndroidX
> DataStore — see §2 and `AGENTS.md` §10); state management approach
> (`StateFlow`/`SharedFlow` only — see §11); passive BLE scanning policy for
> the Fast Pair Battery Notification (bounded exception — see §9.1,
> `DECISIONS.md` ADR-006); **single-device support only for v1** — the app
> targets exactly one paired Pixel Buds Pro 2 at a time (matches `PROJECT.md`'s
> "Definition of done"); simultaneous multi-device support is explicitly out
> of scope until separately proposed and recorded in `DECISIONS.md`.

## 16. Attribution

Protocol structure knowledge is derived from the public reverse-engineering
work of the `qzed/pbpctrl` project (Linux/Rust). No source code from that
project is reused directly; only documented protocol/frame knowledge informs
this Android-native implementation.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ARCHITECTURE.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ARCHITECTURE
