## Pixel Buds Pro 2 Control App — Architecture Blueprint

## 1\. System Overview

This application interfaces with the Google Pixel Buds Pro 2 without relying on Google Play Services (GMS), Sandboxed Google Play, or any proprietary telemetry API. Unlike the Linux `qzed/pbpctrl` project, which uses the BlueZ stack and the UPower subsystem via AVRCP, this Android application talks directly to the native Android Bluetooth stack (Fluoride/Babel).

Communication happens over two transports:

# Pixel Buds Pro 2 Control App — Architecture Blueprint

-   **Bluetooth RFCOMM** (`BluetoothSocket`, classic SPP-style channel) for the `libmaestro` control protocol.
    
-   **Bluetooth GATT** (`BluetoothGatt` / `BluetoothGattCallback`) for BLE characteristics exposed by the earbuds' case/charging state, where applicable.
    

Target platform: Android 14+ (API 34), GrapheneOS as the primary reference OS, with compatibility maintained for stock AOSP-based ROMs.

## 2\. Project Structure (MVVM & Clean Architecture)

Strict unidirectional data flow across four layers, each its own Gradle module so dependency direction is enforced by the build graph, not just by convention:

```rust
Pixel Buds Pro 2 Control App — Architecture Blueprint:app            -> wires everything together, hosts MainActivity, DI graph
:ui             -> Jetpack Compose screens, Material 3, ViewModels
:domain         -> use cases, StateFlow-based state holders, sealed error/result types
:data           -> Protobuf/Maestro serializer, frame envelope (de)coder, DataStore prefs
:hardware       -> BluetoothManager, GATT/RFCOMM sockets, ForegroundService
```

# Pixel Buds Pro 2 Control App — Architecture Blueprint

-   **UI Layer** (`:ui`, Jetpack Compose): 100% open-source Material 3 components. No OEM theme dependencies. Observes `StateFlow` exposed by ViewModels; sends user intents (`onAncToggle()`, `onEqChanged(band, value)`) down to the domain layer.
    
-   **Domain Layer** (`:domain`): `BudsViewModel` + use-case classes (`ToggleAncUseCase`, `ReadBatteryUseCase`, `UpdateEqUseCase`). Owns `ConnectionState`, `BatteryStatus`, `AncMode`, `EqProfile` as immutable state models. Has no Android framework dependency beyond `StateFlow`/Coroutines, making it independently unit-testable.
    
-   **Data Layer** (`:data`): Builds/parses `.proto`\-defined messages via `protobuf-kotlin-lite`, and wraps them in the `libmaestro` byte envelope (see §5). Also owns local persistence (EQ presets, last-known battery) via encrypted DataStore.
    
-   **Hardware Layer** (`:hardware`): Owns raw `BluetoothSocket`/`BluetoothGatt` objects and the `ForegroundService` that keeps the RFCOMM channel alive during active use. Exposes a small interface (`BudsTransport`) so upper layers never touch Android BT APIs directly.
    

Dependency direction: `:ui → :domain → :data → :hardware`. No reverse imports.

## 3\. Dataflow (Command Pipeline)

Example: **"Activate Transparency Mode"**

# Pixel Buds Pro 2 Control App — Architecture Blueprint

2.  **UI Event** — user taps "Transparency" in `BudsScreen.kt`.
    
3.  **Domain Intent** — `BudsViewModel.onAncModeSelected(AncMode.TRANSPARENCY)` invokes `ToggleAncUseCase`.
    
4.  **Serialization** — `:data`'s `MaestroSerializer` builds `Maestro.AncCommand.newBuilder().setMode(TRANSPARENCY).build().toByteArray()`.
    
5.  **Framing** — the same layer wraps the protobuf bytes in the `libmaestro` envelope (magic bytes + length + channel ID + optional checksum, see §5).
    
6.  **Transmission** — `:hardware`'s `BudsTransport.send(frame: ByteArray)` writes to the `BluetoothSocket` `OutputStream` on `Dispatchers.IO`.
    
7.  **Acknowledgement / State Update** — an inbound frame (or timeout) resolves a pending coroutine `Deferred`; on success, `MutableStateFlow<AncMode>` is updated, which Compose observes and recomposes automatically. On failure, a `BudsError` propagates up (§7) and the UI shows a retry affordance instead of silently failing.
    

## 4\. Battery Status Logic (Android Fallback)

Android has no equivalent of Linux's BlueZ/UPower/AVRCP battery reporting, so this layer uses Android-native fallbacks, in priority order:

# Pixel Buds Pro 2 Control App — Architecture Blueprint

2.  **Primary:** `BroadcastReceiver` on `BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED` (API 31+) / `android.bluetooth.device.action.BATTERY_LEVEL_CHANGED` (legacy).
    
3.  **Secondary (HFP AT commands):** `BluetoothHeadset.ACTION_VENDOR_SPECIFIC_HEADSET_EVENT`, parsing `AT+IPHONEACCEV` / `AT+XAPL` vendor events surfaced by the HFP profile proxy.
    
4.  **Tertiary (GATT, if exposed):** if the earbuds expose a standard `Battery Service (0x180F)` GATT characteristic for the case, read it directly via `BluetoothGatt.readCharacteristic()`.
    

If none of the three report a value, the UI shows "Battery unavailable" — the app never fabricates or carries over a stale percentage silently; stale values are visually marked ("last known: 3 min ago").

## 5\. Protocol Framing & `libmaestro` Envelope (Data Layer Detail)

Producing the protobuf byte array is only step one. Per the `qzed/pbpctrl` reverse-engineering findings, payloads are wrapped in a proprietary envelope before transmission:

```lua
Pixel Buds Pro 2 Control App — Architecture Blueprint+-----------+--------------+------------+-------------------+-----------------+
| Magic (2B)| Length (2B)  | Channel ID | Protobuf Payload   | Checksum (opt.) |
+-----------+--------------+------------+-------------------+-----------------+
```

# Pixel Buds Pro 2 Control App — Architecture Blueprint

-   **Framing (outbound):** `FrameEncoder` prepends the header, appends a checksum if the observed protocol requires one for the target channel, and hands the resulting bytes to `BudsTransport`.
    
-   **Parsing (inbound):** `FrameDecoder` buffers incoming bytes (RFCOMM streams are not message-delimited at the socket level), detects the magic sequence, reads the declared length, extracts exactly that many payload bytes, verifies checksum if present, and only then hands the inner protobuf bytes to the deserializer.
    
-   Any framing mismatch (bad magic, length overrun, checksum failure) yields a `BudsError.MalformedFrame` — logged locally and dropped, never surfaced as a crash.
    
-   Exact byte offsets/opcodes per command are tracked in `PROTOCOL-NOTES.md` alongside a reference to the corresponding `pbpctrl` source file, so protocol knowledge stays auditable and versioned independently of this document.
    

## 6\. Bluetooth Resilience & GrapheneOS Degradation

GrapheneOS enforces aggressive security/battery policies, including automatic Bluetooth deactivation on lock or inactivity. The architecture treats the physical link as inherently unstable:

# Pixel Buds Pro 2 Control App — Architecture Blueprint

-   **Connection Lifecycle:** `BudsTransport` continuously observes `BluetoothAdapter.ACTION_STATE_CHANGED`; `STATE_OFF` is treated as a normal transition.
    
-   **Graceful Degradation:** an `IOException` from the socket (OS-triggered teardown, range loss, peer disconnect) is caught, `ConnectionState` moves to `DISCONNECTED`, and all in-flight ViewModel polling coroutines are cancelled to avoid leaks or crash loops.
    
-   **Re-connection Strategy:** user-initiated reconnection only — no aggressive background polling/retry loops, both to respect battery and to avoid the fingerprintable scanning behavior GrapheneOS's threat model discourages (see §7 of AGENTS.md).
    

## 7\. Error Handling Architecture

A shared sealed hierarchy (`:domain`) is used across layers instead of raw exceptions crossing module boundaries:

```kotlin
Pixel Buds Pro 2 Control App — Architecture Blueprintsealed class BudsError {
    data object ConnectionLost : BudsError()
    data object Timeout : BudsError()
    data class MalformedFrame(val raw: ByteArray) : BudsError()
    data object UnsupportedFirmware : BudsError()
    data object PermissionDenied : BudsError()
    data class Unknown(val cause: Throwable) : BudsError()
}
```

`:hardware` and `:data` convert all caught exceptions into this type; `:domain` exposes `StateFlow<BudsUiState>` where `BudsUiState` includes an optional `BudsError` so the Compose layer can render a specific, actionable message per failure mode rather than a generic error banner.

## 8\. Firmware / Protocol Compatibility

Because `libmaestro`'s wire format can change across Pixel Buds firmware revisions, each `.proto` file and each entry in `PROTOCOL-NOTES.md` carries the firmware/library version it was verified against. `UnsupportedFirmware` (§7) is returned when an inbound frame doesn't match any known schema version, rather than attempting a best-effort parse that could misreport battery/ANC state.

## 9\. Security & Permission Architecture

# Pixel Buds Pro 2 Control App — Architecture Blueprint

-   **Zero location tracking:** the manifest declares `android:usesPermissionFlags="neverForLocation"` on `BLUETOOTH_SCAN`; `BLUETOOTH_PRIVILEGED` and any `ACCESS_*_LOCATION` permission are never requested.
    
-   **Companion Device Manager (CDM):** initial pairing/discovery uses `CompanionDeviceManager` (API 26+) instead of custom BLE scanning — this delegates the scan UI to the OS and grants the app access only to the explicitly selected device.
    
-   **Local state persistence:** user preferences (custom EQ profiles, last known battery) are stored via encrypted AndroidX DataStore. Nothing is ever transmitted off-device (see AGENTS.md §1 and §9 for the enforcement rules).
    
-   **Threat model summary:** the app assumes a privacy-conscious user on a hardened OS; it minimizes fingerprintable behavior (no continuous scanning), minimizes permissions, and keeps all diagnostic data local and opt-in.
    

## 10\. Testing Strategy

# Pixel Buds Pro 2 Control App — Architecture Blueprint

-   **Unit tests** (`:data`): protobuf (de)serialization and frame envelope encode/decode, using fixed byte-array fixtures — no real hardware required.
    
-   **Unit tests** (`:domain`): ViewModels/use-cases tested against a fake `BudsTransport` implementation that emits scripted frames/errors.
    
-   **Instrumented tests** (`:hardware`, optional/manual): run against a real paired device where available; not part of CI given no real earbuds are available in a CI runner.
    

## 11\. Build Configuration Notes

# Pixel Buds Pro 2 Control App — Architecture Blueprint

-   Kotlin + Jetpack Compose BOM, Gradle version catalog (`libs.versions.toml`) for pinned dependency versions.
    
-   `protobuf-kotlin-lite` via the Gradle Protobuf plugin, `.proto` sources under `data/src/main/proto/`.
    
-   No `INTERNET` permission anywhere in any module's manifest; a CI/lint check should assert this remains true (see AGENTS.md §1).
    

## 12\. Attribution

Protocol structure knowledge is derived from the public reverse-engineering work of the `qzed/pbpctrl` project (Linux/Rust). No source code from that project is reused directly; only documented protocol/frame knowledge informs this Android-native implementation.