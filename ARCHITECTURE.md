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

Communication happens over up to three transports (RFCOMM is the only one the app uses today):

- **Bluetooth RFCOMM** (`BluetoothSocket`, classic SPP-style channel) — the
  **primary** transport. Three DLCIs coexist on it, each with independent
  framing (`PROTOCOL.md` §2.3, `CodecRouter` in §5 below): DLCI 0x04 (official
  Fast Pair Message Stream, 🟢 FACT), DLCI 0x02 (Pigweed `pw_hdlc` carrying pw_rpc
  `maestro_pw.Maestro`, 🟢 FACT since `DECISIONS.md` ADR-034), and DLCI 0x08 ("GSND
  CONTROL", a private envelope whose protocol identity is 🔴 still open; the app no longer
  opens it — ADR-043 moved the Case battery to DLCI 0x02's `SubscribeRuntimeInfo` stream).
- **Bluetooth GATT** (`BluetoothGatt` / `BluetoothGattCallback`) — a possible
  **secondary** transport (e.g. a standard Battery Service `0x180F`, `PROTOCOL.md` §4.3
  Option D, whose presence is contested). Not built — no v1 feature needs it.
- **Bluetooth HID** — not a transport for this app. `CAP-002-FINDINGS.md` §6 observed HID-Control and
  HID-Interrupt L2CAP channels during SDP; `CAP-016-FINDINGS.md` §10 decoded HID Feature Report 2 to
  `#AndroidHeadTracker#`, the description string of Android's standard head-tracker HID sensor used by
  spatial audio (`source.android.com/docs/core/interaction/sensors/head-tracker-hid-protocol`). 🟡
  HYPOTHESIS (strong): Android itself consumes it; no control feature of this app routes through HID.
  §15's question is closed on that basis (maintainer-approved 2026-09-24, `ai-sessions/0045`); tracked
  as `SPATIAL-001`.

**No network from the app** (`AGENTS.md` §1): since `ai-sessions/0064` (DECISIONS.md ADR-050) Settings → Info has project links (since `ai-sessions/0066`: the README and the
issue tracker on GitHub; the licence is read in the app only) that hand the URL to another app — the browser — with `Intent.ACTION_VIEW` on the user's tap; this app makes no network request, holds
no `INTERNET` permission and needs no `<queries>`; the full licence text is bundled and readable offline.

Compile/target/minimum SDK: **API 34 (Android 14)** — decided 2026-09-13, `DECISIONS.md` ADR-029;
see §15's "already decided" list. Primary reference OS: GrapheneOS, with compatibility maintained
for stock AOSP-based ROMs.

```
┌──────────────────────────────────────────────────────┐
│  :ui  (Jetpack Compose, Material 3)                   │
│  - Screens, Composables (state hoisted in :app's      │
│    MainActivity — no ViewModel class, see §2)         │
└──────────────────────┬─────────────────────────────────┘
                        │ OpenControlUiState + OpenControlActions
┌──────────────────────▼─────────────────────────────────┐
│  :domain                                              │
│  - Domain models (ConnectionState, AncMode, ...)      │
│    (no use-case classes — see §2)                     │
│  - BudsRepository interface                            │
└──────────────────────┬─────────────────────────────────┘
                        │ implementation
┌──────────────────────▼─────────────────────────────────┐
│  :data                                                │
│  - BudsRepositoryImpl                                  │
│  - CodecRouter (per-DLCI FrameEncoder/FrameDecoder:     │
│    0x02 EQ/settings/Case, 0x04 ANC/Ring/battery/ACK)   │
│  - SafeModeGate (§8.1, ADR-042)                         │
│  - DataStore (Debug Mode toggle and, since 0062, the    │
│    dark mode — see §2's Data Layer note)                │
└──────────────────────┬─────────────────────────────────┘
                        │ consumes BudsTransport(channelId)
┌──────────────────────▼─────────────────────────────────┐
│  :hardware                                            │
│  - BudsTransport (interface, channelId-aware)           │
│  - RFCOMM socket manager (no GATT client exists)        │
│                                                         │
│  - ConnectionStateMachine, BleLogger                    │
│  - ForegroundService, BudsCompanionPairing,             │
│    BluetoothStateObserver, OsConnectionObserver          │
└──────────────────────────────────────────────────────┘
```

## 2. Project Structure (Clean Architecture; no ViewModel — `DECISIONS.md` ADR-048)

`:domain` is the isolated center of the module graph, per Clean Architecture's
dependency-inversion principle: it has no dependency on `:ui`, `:data`, or
`:hardware`, and defines the interfaces (`BudsRepository`) that outer layers
implement or consume. Each layer is its own Gradle module so this direction is
enforced by the build graph, not just by convention:

```
:app            -> wires everything together, hosts MainActivity (UI state holder, ADR-048), AppUiSession, DI graph, tile/application
:ui             -> stateless Jetpack Compose screens, Material 3 (OpenControlUiState in, OpenControlActions out)
:domain         -> domain models, BudsRepository interface, small pure mappings (e.g. the tile state), sealed error/result types
:data           -> hand-written pw_rpc/Message Stream codecs (ADR-041), frame envelope (de)coder, DataStore prefs
:hardware       -> BluetoothManager, RFCOMM sockets, ForegroundService (no GATT client exists — §13)
```

- **UI Layer** (`:ui`, Jetpack Compose): 100% open-source Material 3
  components. No OEM theme dependencies. Observes plain state passed in as
  parameters and sends user intents up via callback lambdas
  (`OpenControlActions`, §2.4) — **as actually built (`ai-sessions/0033`),
  `:ui` holds no ViewModel and no Hilt dependency at all**; state hoisting and
  the `BudsRepository` calls that back these callbacks live in `:app`'s
  `MainActivity` instead (why: `ai-sessions/0033_FEATURE_RESULT_2026_09_18.md` Phase 6 — a
  scope-bounded choice matching `ai-sessions/0013`'s `AncScreen` pattern; §2.4 describes the navigation, not this choice — pointer
  corrected 2026-09-25, `ai-sessions/0050` F-1). **`DECISIONS.md` ADR-048** (2026-09-30, superseding ADR-001's "MVVM in the UI layer"
  clause) records this as the design: no ViewModel; user actions run in the application scope; state that must survive a configuration
  change lives in the repository or in an application-scoped holder — `:app`'s `AppUiSession` (pairing progress, the bonding wait, the
  Debug list of unidentified frames; `ai-sessions/0059`).
- **Domain Layer** (`:domain`): domain models (`ConnectionState`, `AncMode`,
  `EqBandGains`, `BatteryStatus`, `RingTarget`, the `BudsError`/`BudsResult`
  sealed hierarchy) and the `BudsRepository` interface (implemented in
  `:data`; see §2.1). No dedicated use-case classes exist as of
  `ai-sessions/0033` — `BudsRepository`'s own suspend functions
  (`setAncMode`, `setEqGains`, `ringBud`, ...) are called directly by `:app`;
  a `ToggleAncUseCase`-style indirection layer was judged unnecessary
  boilerplate for this app's actual command surface, not an oversight. Has no
  Android framework dependency beyond `StateFlow`/Coroutines, making it
  independently unit-testable.
- **Data Layer** (`:data`): Builds/parses per-DLCI wire frames via `CodecRouter`
  and each feature's `FrameEncoder`/`FrameDecoder` (§5). Implements
  `BudsRepositoryImpl`, translating transport-level events from `:hardware`
  into domain models. Also owns local persistence — as actually built, this
  is the Debug Mode toggle via plain (not encrypted) AndroidX DataStore
  Preferences (`DebugSettingsStore.kt`) and — since `ai-sessions/0062` (F-6) — the dark-mode choice (`DarkModeSettingsStore.kt`, a second key in the
  same file, `SettingsDataStore.kt`); see `DebugSettingsStore.kt`'s comment for why encryption was judged unnecessary for these non-sensitive
  values, and AGENTS.md §10 for the dependency-policy reasoning. No EQ-preset
  or battery persistence exists yet — this project's own state-reconciliation
  design (§3.1) treats the hardware as the sole source of truth for those,
  by design, not as a gap.
- **Hardware Layer** (`:hardware`): Owns raw `BluetoothSocket`/`BluetoothGatt`
  objects and the `ForegroundService` that keeps the RFCOMM channel alive
  during active use. Exposes a small interface (`BudsTransport`) so upper
  layers never touch Android BT APIs directly. Contains the
  `ConnectionStateMachine` (§2.1).

### 2.4 UI Navigation Structure (added `ai-sessions/0033`)

`:ui` uses `androidx.navigation:navigation-compose` (pinned, justified below) for a small,
flat destination graph — one screen per `ARCHITECTURE.md`-recognized v1 feature, plus the settings menu
(Settings / Debug / Info) behind the gear. The Debug tab is always reachable; **Debug mode** is a switch on it
that gates only the hex lines of the log (§7/§12) — as built, (corrected 2026-10-03, `ai-sessions/0069`):

```
Connection screen (start destination)
 ├─ ANC screen
 ├─ Sound screen (the EQ, plus volume balance, mono audio, conversation detection — `ai-sessions/0052`)
 ├─ Controls screen (touch controls, press-and-hold per bud, the press-and-hold ANC-mode list and the in-ear detection switch — `ai-sessions/0052`, `0056`)
 ├─ Battery (rendered as a section of the Connection screen, not a separate destination —
 │   battery is push-driven ambient state, not a user-operated control, so it doesn't need its
 │   own navigation stop; see §3.1's table)
 ├─ Find My Buds screen
 └─ Settings menu (since `ai-sessions/0062`; only reachable via the top app bar's gear — never shown
     in the main bottom/side navigation), with three tabs: Settings (dark mode), Debug (the
     developer-facing Debug screen, per AGENTS.md §6/§9 not part of ordinary use) and Info (the
     app's build, the licence and the project links — `ai-sessions/0064`, ADR-050 — and the Buds' firmware)
```

**As built since `ai-sessions/0057`** (the maintainer's decisions in chat 2026-09-29): a Material 3 top app bar titled "OpenControl" and a bottom
navigation bar with exactly five tabs — **Connection, ANC, Sound, Controls, Find** (Android's guidance: "three to five destinations of equal importance").
Debug is reached only through the top app bar's bug action, as a full-screen destination outside the five tabs; back returns to the tab it was opened from.
**Since `ai-sessions/0062` (F-4, the maintainer's design in chat 2026-10-01):** a **gear** ("Settings", material-icons-core's `Settings` icon) replaces the bug
icon and opens a full-screen **Settings** destination with three tabs — **Settings** (dark mode System / On / Off, default System, F-6), **Debug** (the Debug
screen unchanged: Debug-mode switch, Export debug log, unidentified frames) and **Info** (the app's version, git commit and commit date from `BuildConfig`,
computed locally at build time; the firmware of the Case / Left bud / Right bud from this connection's announcement with its receive time and the announced
control channel, or "Not connected yet", F-5). Back returns to the tab it was opened from. **Since `ai-sessions/0064` (F-6, ADR-050):** Info also shows the
licence ("GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)"), **Read the licence** (the bundled `LICENSE` text, offline), and the links
"README on GitHub", "Report an issue on GitHub" (a "Licence on GitHub" link was removed in `ai-sessions/0066`, ADR-050 Update) — each hands its fixed URL to the browser on a tap (no app to open it ⇒ a message with the
address). **F-1:** the selected tab survives a configuration change (rotation, Android's dark switch): the pager ↔ back-stack sync is skipped until the restored
back stack is known (`CAP-066` K4r: a rotation reset Sound → Connection; `TabRestoreTest`) — **hardware-verified in `CAP-067`** (`ai-sessions/0067`: 14 of 14 configuration changes kept the tab or the Settings menu, release 1.0.0).
(From `ai-sessions/0037` to `0056` Debug was a sixth bottom tab, contrary to the tree above; `0057` restored the documented design.) Every tab can be pulled
down: while the session is `Ready` a pull runs that tab's existing refresh (Connection and Find: *Refresh battery*; ANC: Refresh; Sound: "Read EQ again" then
the settings re-read; Controls: the settings re-read), otherwise the action of the Connection screen's own button in that state (Connect/Retry, Enable
Bluetooth, Allow, Pair) — once per pull, never automatically. The times and state words of each card are in its (i) details dialog (§3.1).

- **Connection screen** is always the start destination — every other screen assumes `ConnectionState
  == Ready` to *act*. As built, every tab can be opened at any time ((corrected 2026-10-03, `ai-sessions/0069`): the earlier text said navigating
  while not connected "is not offered"); a tab opened without a session shows its last values marked as such, or
  nothing, and its actions return `BudsError.ConnectionLost` (or `SessionOpening` while a connect is in flight). The Connection screen is what surfaces `BudsError`
  states and the GrapheneOS Bluetooth-disabled prompt (§6.0a/§9.1).
- **Why a separate screen per feature, not one dense screen**: each of ANC/EQ/Find My Buds has enough
  controls (the EQ alone is 5 sliders + 5 presets, shown in two rows of 3 + 2 — "Last saved" is not a preset in the app; corrected
  `ai-sessions/0052`, `0051` F-5) that combining them would fight the "every implemented
  function is clearly displayed and operable" goal this whole build session is scoped against. **Since `ai-sessions/0052`** (the maintainer's
  choice in chat 2026-09-26) the EQ tab is labelled **"Sound"** and also holds balance, mono and conversation detection; a fifth tab
  **"Controls"** holds touch controls, press-and-hold and the in-ear detection setting (tabs: Connection, ANC, Sound, Controls, Find — Debug moved to the top
  app bar in `ai-sessions/0057`). **Since `ai-sessions/0056`**
  (the maintainer's choices in chat 2026-09-28) it also holds "Modes for press and hold (both buds)" — four boxes, shown only while a bud's press and hold is
  Noise control, the last two ticked boxes disabled with "At least two modes must stay selected." — and "In-ear detection" is a switch with a note on what "off"
  changes (the balance's `[‹]`/`[›]` steps of `ai-sessions/0064` F-2 were removed again in `ai-sessions/0066`, the maintainer's choice — the slider alone, one
write per completed drag); a setting not read from the Buds yet is disabled — the switches, the balance slider and (since `ai-sessions/0059`, the maintainer's
  choice in chat 2026-09-30) the five EQ sliders, while the EQ presets stay usable. **Exception:** the press-and-hold action chips (`HoldRow`,
  `ControlsScreen.kt`) are enabled with the connection only; a chip tapped before the read writes that one field (`ai-sessions/0058` A58-ARCH-05).
- **Justification for `navigation-compose`** (`AGENTS.md` §10's dependency-policy requirement): pure
  AndroidX/Compose-first library, no network/analytics/GMS dependency, the standard Compose-idiomatic
  way to implement a back-stack-aware multi-screen flow without hand-rolling one; avoids the
  main-activity-owned `when`-on-an-enum navigation anti-pattern for a graph this shaped.
- **Swipe navigation (added `ai-sessions/0043`):** an `androidx.compose.foundation.pager.HorizontalPager`
  (already on the classpath transitively via `compose-material3`/`compose-foundation`, no new dependency)
  renders the five tabs' content and is kept in two-way sync with `navController`'s current destination —
  a completed swipe calls the *same* `navController.navigate(...)` (`popUpTo`/`launchSingleTop`/
  `restoreState`) a bottom-nav tap always used, so the single-top back-stack semantics this section
  already describes (and `ai-sessions/0037`'s fix) apply identically regardless of which trigger changed
  the tab. `NavHost` itself is kept as a zero-size, empty-composable back-stack holder — the pager owns
  what's actually rendered. `OpenControlNavHost.kt`'s own doc comment has the exact mechanism.
  Hardware status (corrected 2026-10-03, `ai-sessions/0069`): pull-to-refresh was seen working in `CAP-065` and system back in `CAP-067`; the
  **swipe between tabs has not been identified on film in any capture** (`CAP-062`: "swipe not distinguishable").
  // TODO(verify): the swipe gesture — `APP_TESTPLAN.md` C10.

Dependency direction: `:ui → :domain ← :data → :hardware → :domain`. `:ui` depends on
`:domain` for the models and `OpenControlUiState`'s types (ADR-048: no use cases; `:app` calls `BudsRepository`). `:data` depends on `:domain`
(to implement `BudsRepository`) and on `:hardware` (to consume `BudsTransport`
— see §2.1). `:hardware` depends on `:domain` for the shared result/error and link types (`BudsResult`, `BudsError`,
`AndroidLink`; `hardware/build.gradle.kts` — added to this sentence 2026-09-25, `ai-sessions/0050` F-2). `:domain` imports nothing from the other
four; no module
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
| `BudsTransport` | `:hardware` (interface consumed by `:data`) | Abstracts the underlying RFCOMM `BluetoothSocket` (primary) and, where applicable, `BluetoothGatt` (secondary — case/charging characteristics). Upper layers see only `send(channelId: Int, frame: ByteArray)` / an inbound `Flow<Pair<channelId: Int, frame: ByteArray>>`, never raw Android BT types. `channelId` addresses one of the three coexisting RFCOMM DLCIs (`PROTOCOL.md` §2.3) — it is not optional, since the three channels have independent framing and cannot share one send/receive path. `connectionLost` (added `ai-sessions/0038`, hardened `ai-sessions/0039`) emits at most once per connection, only after every socket of that connection is closed; `openChannel`/`closeChannel`/`channelClosed` (added `ai-sessions/0040`, ADR-032) manage the shared on-demand DLCI 0x04 channel — see §6.0b. |
| `ConnectionStateMachine` | `:hardware` | Explicit state machine (`Disconnected → Connecting → Discovering → Ready → ...`) driving `ConnectionState`. **As built (ADR-032):** `Ready` = "the MAESTRO channel (DLCI 0x02) is open"; `Discovering` is a zero-length pass-through (SDP resolution happens inside the socket open). These are app states, not wire-observed ones (`PROTOCOL.md` §5). |
| `CodecRouter` (per-DLCI `FrameEncoder` / `FrameDecoder`) | `:data` | Routes each inbound `(channelId, bytes)` pair from `BudsTransport` to the codec for that DLCI, and routes each outbound command to the codec for whichever DLCI it belongs on. See §5 for the three channels and their framing. Pure Kotlin, no Android dependencies — fully unit-testable against fixed byte-array fixtures, per `AGENTS.md` §11. Each per-DLCI codec is implemented independently, gated on that DLCI's own framing reaching 🟢 FACT confidence (see §5's implementation gate) — one DLCI's codec is never blocked on another's. |
| `BudsRepository` / `BudsRepositoryImpl` | interface in `:domain`, implementation in `:data` | Translates protocol-level events into domain models, exposed as `Flow`/`StateFlow` to the domain layer. |

## 3. Dataflow (Command Pipeline)

Example: **"Activate Transparency Mode"** (as built, rewritten 2026-09-24 — the earlier `BudsScreen`/`BudsViewModel`/
`ToggleAncUseCase`/`MaestroSerializer` example described classes that never existed; ANC is a Message Stream byte frame, not protobuf):

1. **UI Event** — the user taps "Transparency" in `AncScreen` (`:ui`), which calls the hoisted `OpenControlActions.onAncModeSelected`.
2. **Action** — `:app`'s `MainActivity` launches `BudsRepository.setAncMode(TRANSPARENT)` in the **application scope** (a
   configuration change cannot cancel it, 0044 APP-4).
3. **Claim** — `BudsRepositoryImpl.withMessageStream` claims DLCI 0x04 on demand (ADR-032): the channel's splitter is reset, the
   claim waits for the Buds' Model ID.
4. **Safe-Mode gate** — `SafeModeGate` checks the announced firmware and this claim's Model ID (§8.1, ADR-042); if either is not
   verified, nothing is sent and `UnsupportedFirmware` is returned.
5. **Encoding** — `AncFrameEncoder` builds `08 12 00 14 01 e8 e8 80 <16 bytes>` (`PROTOCOL.md` §4.1).
6. **Transmission** — `RfcommBudsTransport.send(0x04, frame)` writes to the socket on `Dispatchers.IO`.
7. **Acknowledgement / State Update** — `CodecRouter` routes the Buds' ACK (`ff 01 …`) or NAK (`ff 02 …`) as a `RoutedFrame.Reply`
   and their `Notify` as `RoutedFrame.Anc`: an ACK applies the requested mode, a `Notify` applies the Buds' own mode, a NAK is
   `BudsError.CommandRejected` and no answer within 1 s is `BudsError.Timeout` — in both failure cases the previous mode stays. The
   channel is released 1.5 s later.

### 3.1 State Reconciliation (Hardware Is the Source of Truth)

Local `StateFlow` values (`AncMode`, `EqProfile`, connection-scoped state) are
a **cache of the hardware's last-known state, never the authority on it.**
This matters because the app is not the only writer: the official Pixel Buds
app, another paired host, or the hardware's own buttons/touch controls can
change state while this app is disconnected or backgrounded.

- **On every (re)connection**, before trusting or displaying any locally
  cached value, the app queries the actual current state from the hardware
  (ANC mode, battery, EQ) rather than assuming the
  last-known `StateFlow` value still holds. Reads run regardless of firmware; the Startup Handshake
  (§8.1, ADR-042) gates **writes** only.
- **One rule for "current" (built `ai-sessions/0069`, 1.0.1; `ai-sessions/0068` A68-APP-02; the maintainer's choice in chat 2026-10-03, "Keep the value,
  mark it"):** a value is current only while the session is `Ready` **and** the Buds reported it on this connection — `isCurrent(ready, valueAt,
  sessionSince)` in `:domain` (`ValueCurrency.kt`); `BudsRepository.sessionSince` is the time the current connection was requested. A value that is not
  current stays visible, dimmed, with the (i) dot and "from the last connection" in its details: the ANC mode (kept across sessions — a `StateFlow<AncMode?>`
  since `0069`, A68-APP-03, so the newest report is never dropped), the EQ and the settings while nothing is connected (at a Connect they are reset to "not
  read", as before), and the battery values, which are marked at Disconnect and at a session loss, not only at the next Connect. The Quick Settings tile and
  the notification take the mode through the same rule (`currentAncMode`) and show none that is not current. A value that was never read shows "—", not a
  default (the visible text only; what a screen reader says is in `TODO.md`).
- Locally cached values are marked provisional/stale until reconciled against a fresh read. **Added
  `ai-sessions/0043`:** each cached value's own wall-clock receive time is threaded alongside it as a
  sibling `StateFlow<Long?>` (`ancModeUpdatedAt`, `eqProfileUpdatedAt`, `batteryStatusUpdatedAt`,
  `ancAvailabilityUpdatedAt`, `BudsRepository.kt` — (corrected 2026-10-03, `ai-sessions/0069`): this list named `dockStateUpdatedAt`, a symbol that
  no longer exists since ADR-049 replaced the dock-state reading), stamped only at the moment `BudsRepositoryImpl` actually
  receives that value — never a ticking relative counter or a polling timer (§6, "nothing in this app
  runs a fixed-interval timer loop" — a live "N seconds ago" display would need exactly that). The UI
  shows this as an absolute time string (`"updated 14:32:07"`/`"last seen 14:32:07"`), replacing the
  earlier bare "(last known)"/"— last seen" qualifiers that carried no time at all. **Since `ai-sessions/0057`** (the
  maintainer's decision D-7, chat 2026-09-29): the times and state words are shown in each card's (i) details, from the same
  helpers; on the main surface a stale, unread or earlier-session value is dimmed and its card's (i) carries a dot (and the
  description "Details — not current"), and a setting not read yet keeps its control disabled. "Battery unavailable" and every
  error text stay on the main surface.
- If a fresh read disagrees with the cached value, the fresh read wins
  unconditionally — the app never keeps showing (or acting on) its own stale
  assumption once the hardware has answered.
- A user-initiated write (e.g. toggling ANC) changes the shown value only when the Buds answer it (step 7 above: ACK / `Notify`
  for ANC, the empty `RESPONSE` status OK for DLCI 0x02 writes) — no optimistic update (corrected 2026-09-29, `ai-sessions/0057`
  F-4: the earlier text described an optimistic update the app has not made since 2026-09-24). A slider follows the finger only
  while it is down; after release it shows the Buds' value until they acknowledge the new one (`ai-sessions/0057` F-1).

**Per-feature reconciliation mechanism (added `ai-sessions/0033`, since this session's `BudsRepositoryImpl`
must implement this concretely for every v1 feature, not just ANC — the mechanism differs by feature and
should not be assumed uniform):**

| Feature | Reconciliation mechanism on (re)connect | Confidence |
|---|---|---|
| ANC | **Corrected `ai-sessions/0040`:** the connect-time `Get`/`Notify` pair (ADR-021/ADR-022) is the *official client's* own query — a client that sends nothing gets no ANC `Notify`. This app therefore sends its own `AncFrame.Get` during each Message Stream claim (the Connect-time snapshot, ANC Refresh, and each ANC tap's reply) and treats the value as last-known between claims (ADR-032). **A `Set` is only counted once answered (2026-09-24):** ACK → requested mode, `Notify` → the Buds' mode, NAK/no answer → failure, previous mode kept. The `Notify`'s Settable byte is kept as `ancAvailability` (`ai-sessions/0048` I-3, ADR-024 Update 2026-09-25): while it reads `0x00` the Buds NAK a `Set` (reason `0x02`, `CAP-062` 10/10). **Since `ai-sessions/0054` (I-1, maintainer-confirmed in chat 2026-09-28; it reverses `0048` I-3's "claim and send nothing" for a user tap):** the ANC buttons and the tile stay tappable, with "ANC can only be changed while you wear the Buds (checked HH:MM:SS). Tapping a mode checks again first." (🟡 wording); such a tap does one ordinary claim with the `Get` and sends the `Set` **in that claim** only if the answering `Notify` reads Settable non-zero (then the usual ACK rules); if it still reads `0x00` nothing more is sent, the checked time moves and the tile shows a toast — because a `0x00` answer can be old (`CAP-063`: worn again 37 s after `Notify` 4774, disabled for 6 min). ~~Unknown (no `Notify` yet this connection) or non-zero ⇒ the `Set` is sent directly, as before.~~ **Since `ai-sessions/0062` (F-1/F-2, maintainer-confirmed in chat 2026-10-01): every ANC tap — the screen's and the tile's — does one claim with the `Get` first and sends the `Set` in that claim only if the claim's `Notify` reads Settable non-zero; on `0x00` nothing more is sent** (`CAP-064`: an 18-s-old `e8` led to `Set` 3433 → NAK 3440; `CAP-065` F7: an unknown availability sent `Set` 10790 without a `Get`). The tile's next mode is computed from that fresh `Notify`, not from the mode shown. Wording: "The Buds don't allow changing noise control right now (usually because no bud is in an ear). Tapping a mode checks again first." (tile subtitle "Not allowed now") — the byte means "not allowed now", not "not worn" (`CAP-064`: `e8` with no bud worn). **F-3:** a claim closed after its `Set`/`Get` was written and before the answer is `BudsError.AnswerCutOff` (never retried, never "didn't respond in time"); after a cut-off `Set` the shown mode is "not confirmed" (dimmed + the (i) dot) until the Buds' next `Notify` or ACK. **Hardware-verified in `CAP-066` (`ai-sessions/0063`, maintainer-approved in chat 2026-10-01):** 23 of 23 app claims sent `08 11` first; 4 `Set`s, each after that claim's `Notify` read `e8`, all ACKed; 11 `00` answers sent no `Set`; the tile stepped from the fresh `Notify` (A2369 `40` → A2370 `20`). F-3 (`AnswerCutOff`) not exercised (no cut-off occurred). | 🟢 FACT for the `Get`/`Notify` pair; the per-claim use is ADR-032 |
| EQ | No connect-time *push* exists for EQ, but the official app **reads** it — and so does this app since `ai-sessions/0041` (DECISIONS.md ADR-034, maintainer-approved 2026-09-20): the Connect sequence waits for the Buds' unsolicited `GetSoftwareInfo` announcement (which names this connection's pw_rpc channel), then sends `ReadSetting 4:16` (active EQ) on that channel and fills `eqProfile` from the answer; the EQ screen can re-read on demand. `eqProfile` is `null` only until that answer arrives or if it failed — `BudsRepository.eqError` then carries the reason (no announcement in time, a channel with no known address, an error status, a timeout). A write is only counted as done once the Buds' `RESPONSE` arrives (`status` absent/OK); anything else is surfaced, never assumed. Field 18 (last saved custom EQ) never replaces the active value. | 🟢 FACT (ADR-034: identification, `ReadSetting 4:N` semantics, three second-capture chains); 🟡 HYPOTHESIS for what a fresh client must send first and for request/response matching (handled conservatively, `// TODO(verify)`, hardware re-test) |
| Battery (DLCI 0x04 Option B, **implemented `ai-sessions/0040`, ADR-033; charging flag `ai-sessions/0041`**) | Push-based: the Buds send three `Group 0x03 Code 0x03` frames within ~10 ms of the channel opening and again on every change — so each Message Stream claim yields a reading. Each of `b1`/`b2` is `0bSVVVVVVV`: `V` = 0–100 % and `S` = charging (maintainer-accepted 2026-09-20); `V = 0x7F`/`> 100` reads "unavailable"; `b3` (Case) is never decoded (it read `0xff` in 60/60 frames). **The Case comes from DLCI 0x02** (**ADR-043**, 2026-09-24): after the Connect-time EQ read the app sends one `SubscribeRuntimeInfo` request on the announced channel; the Buds then push `SERVER_STREAM` packets by themselves and entry 6.1 field 1 is the Case % — a packet without that entry reads "unavailable". *Refresh battery* re-reads Left/Right — **since `ai-sessions/0052`** always on a **fresh** claim (a claim still lingering is released and opened again, because the Buds send the `03 03` burst only on an open; no burst ⇒ "No new battery reading from the Buds — try again.", values and times unchanged) and it also re-sends one `SubscribeRuntimeInfo` (ADR-043 Update 2026-09-26; no wait, no retry). **Since `ai-sessions/0048` (ADR-043 Update):** the same stream's 6.2/6.3 field 2 (fallback 7.2/7.1) gives each bud's charging state, shown as "charging in the case" with its own time; the newest charging report of either source wins (I-8); a packet without 6.1 keeps the last Case value as "last seen HH:MM:SS" (I-5, in memory only, marked last seen after a reconnect). The DLCI 0x08 claim of ADR-035/038/039 is withdrawn (its `0e 04` got no answer in `CAP-061`, 8/8). | 🟢 FACT (ADR-031 identity, ADR-033 unblock + charging-flag update; `PROTOCOL.md` §4.3 Option F + ADR-043 for the Case) |
| Battery (HFP, Option C) — **removed `ai-sessions/0042`** | Push-based on the wire (`AT+BIEV`/`AT+CIND`, ADR-015/023) but **not consumable by an app**: `CAP-059` shows `AT+BIEV=2,100` twelve times on the wire (seven dissected as HFP, five on the undissected DLCI after the reconnect — corrected 2026-10-03) and **zero** vendor-specific events in the app's receiver (and the value is one earbud's, never the Case). `HfpBatteryReader` and its wiring were removed on the maintainer's decision (chat 2026-09-20); the wire facts stay. | 🟢 FACT on the wire; 🟢 not app-consumable (`CAP-059-FINDINGS.md` §7) |
| DLCI 0x02 settings (`ai-sessions/0052`, `0056`) | Read at Connect: after the EQ read and before `SubscribeRuntimeInfo`, one `ReadSetting 4:N` each for fields 2, 4, 7, 12, 17, 19, 22 (ADR-036, ADR-046), sequential, ≤ 2 s each, never retried; the values are reset to "not read" at every Connect and shown with "read HH:MM:SS". The same pass runs once more on a user pull on "Sound" or "Controls" (`refreshSettings`, `ai-sessions/0057` D-11 — same fields, order and timeout; nothing sent unless `Ready`; a field not answered keeps its last value and time). Writes (ADR-045: 17, 19, 22, 4, 7; ADR-046: 12; ADR-047: 2) are applied only on the Buds' empty `RESPONSE` status OK ("changed HH:MM:SS"); an error status or no answer keeps the previous value and shows the reason. **Field 12** always carries all four booleans, built from the list the Buds last reported (unread ⇒ refused), and a list with fewer than two modes is refused before anything is sent. A tap while the session is being (re)opened is refused with "The app's channel is being reopened — try again in a moment." (nothing queued). | 🟢 FACT for the field identities and the write acknowledgement (ADR-019/026/034/045/046/047, `PROTOCOL.md` §4.5 2026-09-26/28 Updates); not hardware-verified |
| Find My Buds Left/Right | An action, not persisted state. **Since `ai-sessions/0048` (I-6):** the ring this app started is remembered in memory across Disconnect/Connect (it keeps sounding until Stop, `CAP-062`) and cleared only by an ACKed Stop or replaced by a new Ring; after a reconnect it is shown as "may still be ringing" — the app cannot know. | N/A |

**Consequence for `:data`'s codec scope (updated `ai-sessions/0041`):** DLCI 0x02 is decoded as pw_hdlc → pw_rpc `RpcPacket`
(`Hdlc`, `PwRpc`, `Maestro`); the only requests the app builds are the EQ `WriteSetting` (ADR-020), `ReadSetting` for fields 2, 4, 7, 16, 17,
18, 19, 22 and — since `ai-sessions/0056` — 12 (ADR-034/036/046; `Maestro.readSettingRequest` returns `null` for anything else), `WriteSetting` for fields 4, 7,
17, 19, 22 (ADR-045), 12 (ADR-046, never fewer than two modes) and 2 (ADR-047) (`SettingsCodec` — nothing else can be encoded) and `SubscribeRuntimeInfo` (ADR-043)
— updated `ai-sessions/0052`, `0056`. The earlier statement that no EQ
read request may be built is superseded by ADR-034.

### 3.2 Timing constants (as built; added 2026-10-03, `ai-sessions/0069`, `A68-ARCH-05`)

One table, read from the code. Where an ADR or another section names a wait, this table is what the app does; an ADR that states a bound ("≤ 3 s")
is met by the value here. Check with `grep -rn "const val .*_MS" android/*/src/main`.

| Constant | Value | What waits for what | Where | Decision |
|---|---|---|---|---|
| `DEFAULT_CONNECT_ATTEMPTS` | 3 | attempts per RFCOMM channel in one connect | `RfcommBudsTransport` | §6.0b rule 4 |
| `DEFAULT_RETRY_DELAY_MS` | 400 ms | pause between those attempts | `RfcommBudsTransport` | §6.0b |
| `DEFAULT_FAST_FAILURE_THRESHOLD_MS` | 2 000 ms | a failed attempt slower than this is not retried | `RfcommBudsTransport` | §6.0b |
| `MAESTRO_ANNOUNCE_WAIT_MS` | 3 000 ms | a read or write waits for the Buds' channel announcement | `BudsRepositoryImpl` | ADR-034 |
| `EQ_READ_TIMEOUT_MS` | 2 000 ms | one EQ `ReadSetting` waits for its answer | `BudsRepositoryImpl` | ADR-034 |
| `SETTING_READ_TIMEOUT_MS` | 2 000 ms | one settings `ReadSetting` waits for its answer; never retried | `BudsRepositoryImpl` | ADR-036 ("≤ 3 s") |
| `EQ_WRITE_ACK_TIMEOUT_MS` | 1 500 ms | a `WriteSetting` (EQ or setting) waits for the empty `RESPONSE`; also the quarantine after an unanswered write | `BudsRepositoryImpl` | ADR-045 Update |
| — (no constant) | no wait, no retry | the `SubscribeRuntimeInfo` request at Connect and at *Refresh battery* — the stream pushes by itself | `BudsRepositoryImpl` | ADR-043 Update of 2026-10-03 |
| `MODEL_ID_WAIT_MS` | 1 000 ms | a Message Stream command waits for the claim's Model ID frame | `BudsRepositoryImpl` | ADR-042 |
| `SNAPSHOT_TIMEOUT_MS` | 1 000 ms | the Connect-time ANC `Get` waits for the Notify | `BudsRepositoryImpl` | ADR-032 |
| `GET_RESPONSE_TIMEOUT_MS` | 2 000 ms | a manual ANC Refresh waits for the Notify | `BudsRepositoryImpl` | ADR-032 |
| `ACK_WAIT_MS` | 1 000 ms | an ANC `Set` or a Ring waits for the Buds' ACK/NAK/Notify | `BudsRepositoryImpl` | ADR-032 |
| `CUT_OFF_GRACE_MS` | 100 ms | after a claim's close, an answer already received may still pass the codec | `BudsRepositoryImpl` | `ai-sessions/0062` F-3 |
| `BATTERY_BURST_WAIT_MS` | 2 000 ms | *Refresh battery* waits for the `03 03` burst on its fresh claim | `BudsRepositoryImpl` | ADR-031 |
| `MESSAGE_STREAM_LINGER_MS` | 1 500 ms | the Message Stream stays claimed after an action, then is released | `BudsRepositoryImpl` | ADR-032 |
| `AVAILABILITY_PROVISIONAL_MS` | 2 000 ms | a Settable reading this soon after the open is provisional | `BudsRepositoryImpl` | ADR-049 |
| `LOSS_REOPEN_DELAY_MS` | 1 500 ms | delay before the automatic re-open after a Buds-side close | `SessionReopener` | ADR-044 |
| `CHAIN_GUARD_MS` | 10 000 ms | a loss this soon after an automatic re-open schedules no further one | `SessionReopener` | ADR-044 |
| `NOT_CONNECTED_SETTLE_MS` | 1 500 ms | Android's "not connected" must last this long before it is reported | `OsConnectionObserver` | §6.0b |
| `LINK_LOST_BEFORE_MS` / `LINK_LOST_AFTER_MS` | 2 000 / 1 000 ms | window around a session loss in which a link-down reading names the cause | `SessionLoss` (`:domain`) | §6.0b |
| `ADAPTER_OFF_BEFORE_MS` / `ADAPTER_OFF_AFTER_MS` | 2 000 / 1 000 ms | the same window for a Bluetooth-off reading | `SessionLoss` | §6.0b |
| `FRESH_READING_MS` | 2 000 ms | the first link reading after a loss counts if it is this fresh | `SessionLoss` | §6.0b |
| `BOND_TIMEOUT_MS` | 45 000 ms | pairing waits for the bond | `BudsCompanionPairing` | §9.0a |

None of these is a polling interval: each bounds one wait that a user action or a Buds event started (§6, "no fixed-interval timer loop").

## 4. Battery Status Logic (Android Fallback)

Android has no equivalent of Linux's BlueZ/UPower/AVRCP battery reporting, so this layer uses Android-native mechanisms. **As built
(rewritten 2026-09-24, `ai-sessions/0045`; `PROTOCOL.md` §4.3's "current state" note is the protocol side):**

- **Implemented — Message Stream "Battery updated"** (DLCI 0x04 `03 03`, Option B, ADR-031/033): Left/Right with the charging flag,
  pushed on every Message Stream claim.
- **Implemented — DLCI 0x02 `SubscribeRuntimeInfo`** (Option F, ADR-043): the Case, pushed by the Buds after one request per Connect; since
  `ai-sessions/0048` also each bud's charging state and a dated last-seen Case (ADR-043 Update).
- **No longer used — DLCI 0x08 `0e 01`** (Option E, ADR-014): a FACT source, but the app's claim got no answer (`CAP-061`); ADR-043 withdrew it.
- **Not built — BLE Battery Notification advertisement** (Option A, bounded scan per §9.1/ADR-006): it has never matched on the wire
  (`CAP-011`, `CAP-043`, `CAP-059`); the spec says a Provider should not advertise battery data all the time. `BLUETOOTH_SCAN` is not
  even declared until this is built.
- **Not an app source — HFP `AT+BIEV`/`AT+CIND`** (Option C): wire-confirmed, but no vendor-specific event reaches an app on
  Android 14+ (`CAP-059`); removed, **ADR-040**. It does **not** push periodically for the whole session (ADR-015: a settling burst,
  then irregular).
- **Not an app source — generic OS broadcast** (Option 0): `ACTION_BATTERY_LEVEL_CHANGED` is `@SystemApi`, not public API (🟢, `PROTOCOL.md` §4.3
  Option 0, 2026-09-30; `AGENTS.md` §3 bans hidden/system APIs). **Not built — GATT Battery Service** (Option D, contested).

If no source reports a value, the UI shows "Battery unavailable" — the app never fabricates or carries over a percentage silently; every
value carries the wall-clock time it was received (`"updated 14:32:07"` / `"last seen 14:32:07"`, §3.1), never a relative "N min ago"
(which would need a ticking timer, §6).

## 5. Protocol Framing & the Three-DLCI Reality (`CodecRouter`, Data Layer Detail)

The payload bytes are written by hand-written Kotlin codecs — there is no protobuf runtime in the app (ADR-041;
(corrected 2026-10-03, `ai-sessions/0069`): this section opened with "Producing the protobuf byte array is only step one"). `CodecRouter` has an
**inbound** role only: it splits and decodes what arrives per DLCI; outbound frames are built by the per-feature
encoders (`AncFrameEncoder`, `Maestro`, `EqFrameEncoder`, `SettingsCodec`) and handed to the transport by the
repository. Framing is no longer a
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

DLCI 0x02 — Pigweed `pw_hdlc` framing carrying pw_rpc `maestro_pw.Maestro` (🟢 FACT, ADR-034):
+------+------------------------------------+-------------+--------------------------+-----------+------+
| Flag | Address (one-terminated varint)    | Control 03  | RpcPacket (pw_rpc proto) | CRC-32 LE | Flag |
+------+------------------------------------+-------------+--------------------------+-----------+------+
**The address pairs with the RpcPacket `channel_id` the Buds announce per connection (ADR-034's table: 19 ↔ `00 3b`,
21 ↔ `00 4b`, 24 ↔ `80 3d`, 26 ↔ `80 4d`) — never hardcoded, never guessed; an unknown channel sends nothing.** (The earlier
"`0x00`/`0xD180`/`0x1e80`/`0x2680`/`0xe980`" addresses were a wrong byte split, corrected in `PROTOCOL.md` §2.2a.)

DLCI 0x08 — private `[Group][Code][Length][Value]` envelope ("GSND CONTROL"; 🟢 FACT that it's a real, decodable
envelope; 🔴 OPEN QUESTION what protocol it belongs to):
+-----------+----------------+--------------+-------------------+
| Group (1B)| Code (1B)      | Length (2B)  | Value (variable)  |
+-----------+----------------+--------------+-------------------+
The app no longer opens DLCI 0x08 (ADR-043); the codec still decodes `0e 01` (index 3 = Case) for any frame that arrives
and reports every other Group/Code as an `UnidentifiedFrame` — see `PROTOCOL.md` §2.3/§4.3 Option E.
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
  surfaced as a crash. **Resynchronisation (2026-09-24, 0044 APP-2):** a channel's buffer is reset whenever that
  channel is opened, closed or lost, and dropped when a frame would exceed its maximum size (1024 data bytes for
  DLCI 0x04/0x08, 4096 for DLCI 0x02) — a frame cut off by a torn-down on-demand claim can never misalign a later one. This is distinct from a **structurally valid** frame
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

**No decompiled reference exists for DLCI 0x04/0x08, by design, not by gap** (wording corrected 2026-09-24: "clean-room" is ruled
out for this project by `AGENTS.md` §12 — see ADR-025's 2026-09-24 Update). Exhaustive review of
the companion app's own decompiled source found no code anywhere constructing or parsing DLCI 0x04's
Fast Pair Message Stream frames or DLCI 0x08's private envelope — both appear to be implemented
entirely inside Google Play Services rather than the companion app itself, and GMS reverse-engineering
is explicitly out of scope for this project (`DECISIONS.md` ADR-025). This is not a blocker: this
project's own evidentiary chain for DLCI 0x04 has never depended on companion-app code — every 🟢 FACT
promotion for it traces to wire captures matched against the official, public Fast Pair specification
alone (`PROTOCOL.md` §4.1), and `FrameEncoder`/`FrameDecoder` for ANC (ADR-009) and Find My Buds
Left/Right (ADR-011) were implemented this way, with zero APK code cross-reference. (EQ is DLCI 0x02 and
its identification did use APK analysis — ADR-019, ADR-034 — so it is not an example here.)
`FrameEncoder`/`FrameDecoder` work for DLCI 0x04/0x08 proceeds **independently, from capture evidence
(and, for DLCI 0x04, the public spec) only** — the proven, already-practiced method for these channels.

### 5a. Implementation-ready feature summary (refreshed `ai-sessions/0033`, 2026-09-18; re-derived through ADR-049, 2026-09-30)

The per-channel gate above tells you whether a *channel's framing* can be coded against. It does not,
by itself, tell you whether a *specific command* on that channel may be implemented — `AGENTS.md` §6
requires a command's own FACT determination **and** an explicit implementation-unblock statement in a
`DECISIONS.md` ADR, and those two things are tracked per command, not per channel. Re-derived directly
from `PROTOCOL.md` + every `DECISIONS.md` ADR through ADR-049 (not assumed from an earlier session's
summary), the current state is (every write below additionally passes the Safe-Mode gate, §8.1/ADR-042):

| Feature | Channel | FACT status | Unblock ADR | Implementation status |
|---|---|---|---|---|
| ANC (Get/Set/Notify) | DLCI 0x04, Group `0x08` | 🟢 FACT | ADR-009 (explicit "block lifted"), ADR-021/ADR-022, ADR-049 (Settable byte; supersedes ADR-024) | **Implemented** (`AncFrameEncoder`/`AncFrameDecoder`, `:data`) |
| Find My Buds Left/Right | DLCI 0x04, Group `0x04` Code `0x01` | 🟢 FACT | ADR-011 (explicit "implementation is unblocked") | **Implemented** (`RingFrameEncoder`/`RingFrameDecoder`, `:data`) — hardware-verified: Ring Left/Right/Stop sent, ACKed, heard, stopped (`CAP-062-FINDINGS.md`) |
| EQ | DLCI 0x02, pw_rpc `maestro_pw.Maestro` `WriteSetting`/`ReadSetting`, payload `4:{16\|18:{5×float32}}` | 🟢 FACT (pw_rpc identification and `ReadSetting` semantics per ADR-034; envelope, field-to-band mapping, ±6.0 clamp, presets per ADR-016) | ADR-020 (write), **ADR-034** (read-only `ReadSetting` for fields 16/18, channel mirroring) | **Implemented** (`PwRpc`, `Maestro`, `EqFrameEncoder`/`EqFrameDecoder`, `BudsRepositoryImpl.readEq`/`setEqGains`, `:data`) — write ACK and read answer are surfaced; hardware-verified: 5 of 5 `WriteSetting 4:{16:…}` answered OK (`CAP-063` 2374, 6009, 6017, 6026, 8633) **Since 1.0.1 (`ai-sessions/0069`):** a sixth preset **Flat** (0.0 × 5 — the quintet of `CAP-015` frame 2111, within ADR-020's range; named "Flat" because the official "Default" is not captured, `EQP-001`); a gain that is not a finite number is refused before anything is encoded. |
| Battery, Option C (HFP `AT+BIEV`/`AT+CIND`) — **removed `ai-sessions/0042`** (maintainer decision in chat, after the confirming run) | HFP AT-command channel | 🟢 FACT on the wire | None | **Removed** — wire-confirmed, not app-consumable (`CAP-059`: 12 × `AT+BIEV=2,100` on the wire, 0 vendor events in the app) |
| Battery, Option B (DLCI 0x04 `Group 0x03 Code 0x03`) | DLCI 0x04 | 🟢 FACT (message *identity*; ADR-031; charging flag, ADR-033's 2026-09-20 update) | **ADR-033** (`ai-sessions/0040`, maintainer-approved; charging flag accepted 2026-09-20) | **Implemented** (`BatteryFrameDecoder`, `:data`) — `0bSVVVVVVV` per earbud; the Case (`b3`) is not decoded there. Left/Right hardware-seen in `CAP-059`–`CAP-061`. |
| Battery, Case (Option F, DLCI 0x02 `SubscribeRuntimeInfo` entry 6.1) | DLCI 0x02 | 🟢 FACT (`PROTOCOL.md` §4.3 Option F, 2026-09-24) | **ADR-043** (one request per Connect; supersedes the DLCI 0x08 claim of ADR-035/038/039; its 2026-09-26 Update: one more per *Refresh battery*, built `ai-sessions/0052`, hardware-verified in `CAP-063` — 8 of 8 re-subscriptions answered, AY-14; (corrected 2026-10-03, `ai-sessions/0069`): this cell said "not hardware-verified") | **Implemented** (`Maestro.subscribeRuntimeInfoRequest`, `RuntimeInfoDecoder`, `:data`) — Case hardware-verified in `CAP-062`; per-bud charging and the last-seen Case (ADR-043 Update, `ai-sessions/0048`) not hardware-verified. |
| §4.5's other DLCI 0x02 settings (Touch & Hold, Head gestures, In-ear detection, Mono audio, Volume EQ, Volume Balance, Case sounds, Multipoint, Conversation Detection) | DLCI 0x02 | 🟢 FACT for several individual fields' number/semantic identity (ADR-019 and its Updates) | **ADR-036** — read-only `ReadSetting` for fields 2, 4, 7, 11, 15, 17, 19, 22, 27, 28 (no subscription); **ADR-045** (2026-09-26) — `WriteSetting` for 17, 19, 22, 4, 7; **ADR-046** (2026-09-28) — read + write of field 12; **ADR-047** (2026-09-28) — write of field 2 (both built `ai-sessions/0056`) | **As built `ai-sessions/0052`:** reads of 2, 4, 7, 17, 19, 22 at Connect (`SettingsCodec`, fixtures `CAP-036` 1445…1540); **writes of 17, 19, 22, 4, 7 per ADR-045** (fixtures `CAP-019` 1720/1808, `CAP-020` 1741/1995, `CAP-021` 1895/3619/4315/4976, `CAP-022` 1621/1823/1922…2099, byte-identical); UI tabs Sound and Controls. **`ai-sessions/0056`:** field 12 read at Connect and written (ADR-046; fixtures `CAP-056` 1529/1531, 1689/1725/1786/1815/1843, OK 1697; channel 21 `CAP-041` 2176/2192/2198, `CAP-036` 1514/1516) and field 2 written (ADR-047; `CAP-056` 2173/4048, 2849/3627). 11, 15, 27, 28 not read (not asked for). The 0052 settings were hardware-verified in `CAP-063`; 12 and 2 are not (Group AZ, `CAP-064` section VII). |
| Safe Mode / Startup Handshake | DLCI 0x02 announcement + DLCI 0x04 Model ID | 🟢 FACT (firmware string ADR-012/034; Model ID `PROTOCOL.md` §0.1) | **ADR-042** | **Implemented** (`SafeModeGate`, `:data`; Safe Mode card, `:ui`) |
| Find My Buds Case / "ring both" | — | — | ADR-027 (premise narrowed 2026-09-24: spec `0x03` "ring both" untested) | **Out of scope**; sending `0x03` needs its own ADR |

This table is this project's authoritative implementation-readiness list until superseded by a new ADR
— a future session should re-derive it from `DECISIONS.md` directly rather than copying it forward
uncritically, the same discipline this refresh itself applied.

**Resolved 2026-09-20:** the consolidated-ADR PROPOSAL `ai-sessions/0033` recorded here became **ADR-036** (read-only; writes
remain gated per field).

## 6. Bluetooth Resilience & GrapheneOS Degradation

GrapheneOS enforces aggressive security/battery policies, including automatic
Bluetooth deactivation on lock or inactivity. The architecture treats the
physical link as inherently unstable:

- **Connection Lifecycle:** `BluetoothStateObserver` (`:hardware`; (corrected 2026-10-03, `ai-sessions/0069`) — not `BudsTransport`/`ConnectionStateMachine`)
  continuously observes `BluetoothAdapter.ACTION_STATE_CHANGED`; `STATE_OFF` is
  treated as a normal transition.
- **Graceful Degradation:** an `IOException` from the socket (OS-triggered
  teardown, range loss, peer disconnect) is caught, `ConnectionState` moves to
  `DISCONNECTED`, and all in-flight claim/read coroutines of the repository
  (see below) are cancelled to avoid leaks or crash loops.
- **Re-connection Strategy:** user-initiated reconnection only — no aggressive
  background retry loops, both to respect battery and to avoid the
  fingerprintable scanning behavior GrapheneOS's threat model discourages (see
  §7 of `AGENTS.md`, and the bounded exception for passive battery-advertisement
  observation in §9.1 below). **Amended by `DECISIONS.md` ADR-044 (built `ai-sessions/0048`):** while the app is
  *visible* the session is also re-opened by itself, one attempt per event — see §6.0b; never in the background.
- **Event-observation, not polling:** all inbound state (ANC mode, battery,
  connection status) is obtained by observing a `Flow` fed by inbound frames
  or OS broadcasts (`ACTION_STATE_CHANGED`, `ACTION_BOND_STATE_CHANGED`, the profile
  connection broadcasts of §6.0a) — coroutines suspend until an event arrives. (§4's
  option 0 battery broadcast and GATT notifications are not built.)
  Nothing in this app runs a fixed-interval timer loop that re-reads state on
  a schedule; the only place anything resembling a schedule appears is the
  bounded, foreground-triggered advertisement window in §9.1, which is
  time-boxed by design, not a recurring poll.

### 6.0a Foreground Service Lifecycle (added `ai-sessions/0033`)

`AGENTS.md` §2 requires any code path keeping a Bluetooth connection alive beyond an active user
session to run inside a `ForegroundService` with `foregroundServiceType="connectedDevice"` and a
persistent, low-priority notification. Concretely, for this app:

- **Started** when `ConnectionStateMachine` transitions out of `Disconnected` for a **user-initiated**
  connect — or, since `ai-sessions/0048`, ADR-044's automatic re-open, which runs only while the app is visible (so the start is a
  foreground start) — (opening the app with a bonded device present, or an explicit reconnect tap) — not eagerly at
  app process start, and not by a background scheduler (`ARCHITECTURE.md` §6's "user-initiated
  reconnection only" rule applies here too).
- **Notification** shows the current `ConnectionState` (`Connecting`/`Ready`) and, once `Ready`, a one-line summary
  (the current ANC mode) — as built it never shows a `BudsError`: errors are shown in the app, and the notification
  is removed when the session ends (`OpenControlApplication.notificationText`; (corrected 2026-10-03, `ai-sessions/0069`)) — no raw payload content, per
  §12's logging-privacy rules extended to user-visible text.
- **Stopped** when `ConnectionStateMachine` reaches `Disconnected` and no reconnect is in flight, or
  when the user explicitly disconnects from the Connection screen. The service does not restart itself
  automatically (`START_NOT_STICKY`) — matching the "no aggressive background retry loops" rule (§6).
- **Ownership**: lives in `:hardware` alongside `BudsTransport`/`ConnectionStateMachine` (§2's module
  table already lists it there) — `:ui`/`:domain` only observe `ConnectionState`, they never start or
  stop the service directly. **As built (2026-09-24, 0044 APP-6):** `:app`'s `OpenControlApplication` drives it from the
  **application scope** over the repository's `ConnectionState` (+ ANC mode for the text) — so a session that ends while
  the app is in the background still stops it (the earlier composable, bound to the UI lifecycle, missed that). The text
  is updated by re-sending the start intent; a refused background start is logged, never a crash.

### 6.0b Connection-lifecycle rules for the RFCOMM transport (added `ai-sessions/0039`)

Recorded here because they are implementation behaviour forced by a fact about the Android Bluetooth
stack, not a new architectural choice (no `DECISIONS.md` entry; nothing here changes §6's
"user-initiated reconnection only" rule):

- **Android allows one RFCOMM connection per (device, channel), across all apps.** A second client's
  `connect()` to a channel already open fails with `RFCOMM_CreateConnectionWithSecurity: already at opened
  state`, and the stack's failure path then closes the **incumbent's** port too (`ai-sessions/0039` §2.4,
  system-log evidence). On a phone with Google Play services' Fast Pair active, Play services holds the
  Message Stream channel (DLCI 0x04) and re-opens it whenever it drops — so this app and Play services
  can knock each other off it. This app cannot prevent that (it must not touch Play services, `AGENTS.md`
  §1); it can only avoid contending with **itself** and report honestly.
  *External check (2026-09-24, `ai-sessions/0045`):* the Bluetooth RFCOMM specification (Part F:1 §5.4, "DLCI allocation with
  RFCOMM server channels") says the DLCI is formed from the server channel and the session's direction bit and "is thereafter
  used for all packets in both directions between the endpoints", and §2.3.2 gives each multiplexer session its own L2CAP
  channel, with multiple sessions described for connections to *different* devices — so all apps on the phone share one session,
  and one DLC per DLCI, towards the Buds. That the stack's failure path also closes the incumbent is Android behaviour, evidenced
  by the system log only (`ai-sessions/0039` §2.4), not by the spec.
  *Seen again 2026-10-01 (`ai-sessions/0061`, maintainer-approved note, `CAP-065-FINDINGS.md` §3), now in the Bluetooth process's own log:* at 09:27:10.450 UTC
  Play services' connect (`NearbyDiscovery: RfcommEventStreamMedium`, uid 10205) to the Message Stream channel hit `RFCOMM_CreateConnectionWithSecurity: already
  at opened state` and the stack closed OpenControl's port (uid 10338) 0.1 s later. **A reply that arrives after such a close is lost to the app:** a `Set`
  the Buds ACKed after the app's claim was closed (HCI 2640 → close 2649 → ACK 2651) left the app showing the old mode, and a `Get` answered after the close
  (10321 → 10344 → 10356) was reported as "The Buds didn't respond in time." **Built in `ai-sessions/0062` (F-3, the maintainer's choice in chat 2026-10-01):** a
  claim's waits also watch `BudsTransport.channelClosed`; a close after the request and before its answer is `BudsError.AnswerCutOff` (since 1.0.1: "The channel was closed
  before the Buds' answer arrived (possibly by another app using it). Tap Refresh to see the current mode." — what was observed, the cause as a possibility) — not retried (a `ChannelLost` would be retried once by the claim, i.e. a second
  `Set`), not `Timeout`; an answer that turns up within 100 ms of the close still counts (it may have been read before the close). A timeout with the channel
  open stays `Timeout`. After a cut-off `Set` the mode is marked "not confirmed" until the next `Notify`/ACK.
- **A connection is one unit** — *for the session channels opened by `connect()`* (superseded for the on-demand DLCI
  0x04/0x08 channels by ADR-032, see the per-channel bullet below). Any session channel's loss (read failure, EOF, failed
  write) closes *all* of that connection's sockets before `BudsTransport.connectionLost` emits — a surviving socket stays
  "open" in the stack and makes the next `connect()` collide with this app's own leftover.
- **At most one loss report per connection**, and none from a connection already replaced by a newer
  `connect()` or ended by `disconnect()`; `ConnectionStateMachine.onDisconnected()` is idempotent;
  `BudsRepositoryImpl` acts on a loss only while `Discovering`/`Ready`.
- **A failed `connect()` closes the socket that failed**, not only the ones that succeeded.
- **Bounded retry inside one user-initiated `connect()`:** up to 3 attempts per channel, 400 ms apart, only
  for *fast* (< 2 s) failures — a collision returns in well under a second and the failed attempt itself
  frees the port. Slow failures (peer unreachable) are never retried. This ends with the tap that started
  it; it is not a background retry loop.
- **The cause is never discarded:** `BudsError.ChannelUnavailable`/`ChannelLost` carry the channel id and the
  underlying exception text (Bluetooth addresses redacted), shown on the Connection screen and logged.
- **Per-channel handling, decided `ai-sessions/0040` (`DECISIONS.md` ADR-032):** the two channels are *not* one unit.
  The **session is the MAESTRO channel** (DLCI 0x02) — `ConnectionState.Ready` means it is open, and its loss is a session
  loss. The **Message Stream channel (DLCI 0x04) is shared with Google Play services' Fast Pair and is claimed on demand**,
  by the user's own ANC / Find / Connect action (never in the background): claim → act → wait for the Buds' reply → release
  after a 1.5 s linger so Play services can reclaim it. Loss of DLCI 0x04 is *not* a session loss (`BudsTransport.channelClosed`);
  a failed claim is reported per action (`BudsRepository.messageStreamError`) while the session stays `Ready`. Values read from
  DLCI 0x04 (ANC mode, battery) are only as fresh as the last claim. Play services' recovery of its own socket took 2.7–5.0 s in
  three observed cases, so a claim is kept short. This supersedes the "evaluated and not adopted" note `ai-sessions/0039`
  had recorded here.

- **Observing Android's own state is read-only and visibility-bound (`ai-sessions/0041`).** The Connection screen mirrors what Android's
  Bluetooth settings show (paired / connected to this phone) via `OsConnectionObserver`: public APIs only — `BluetoothProfile.getConnectedDevices()`
  on the A2DP/HEADSET/LE_AUDIO proxies, re-read on the profile connection-state, ACL and bond-state broadcasts. It registers only while the UI is
  collected (started/stopped with the lifecycle), opens no socket, claims no channel, starts no service and scans for nothing (`AGENTS.md` §2/§7). Until
  every proxy has bound it reports *nothing* (never "not connected"); without `BLUETOOTH_CONNECT` it reports `UNKNOWN`; a "not connected" must persist
  1.5 s before it is shown (one bounded `transformLatest` delay per event, no timer loop) so a bud coming out of the case does not flicker. The derived
  status (`DeviceStatus`, `:domain`) puts Android's state first and the app's own session second; an open session is authoritative for "controlled".
  **Corrected `ai-sessions/0042` (first hardware run, `CAP-059`):** the receiver was registered `RECEIVER_NOT_EXPORTED` and received **no** system broadcast for
  minutes while an *unflagged* receiver in the same process received the same `BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED` (the actions are protected system
  broadcasts, so it is now `RECEIVER_EXPORTED` — nothing but the system can send them). Matches Android's own guidance (checked
  2026-09-24, developer.android.com "Broadcasts overview"): some system broadcasts come "from highly privileged apps, such as Bluetooth and
  telephony, that … don't run under the system's unique process ID", and a `RECEIVER_NOT_EXPORTED` receiver gets "some system broadcasts …
  but not broadcasts from the highly privileged apps". It additionally re-reads the profile proxies on caller-supplied *refresh* events
  (resume, a change of the bonded device, a change of the app's own session) and on every proxy bind — still event-driven, no timer, still visibility-bound.
  **Since `ai-sessions/0064` (F-4):** every profile proxy the listener delivered is closed exactly once when the flow ends (`ProfileProxies`), also one unbound
  earlier — a Bluetooth-off unbinds them all (`CAP-066` §8: an LE Audio proxy finalized without `close()`; 🟡 the framework's `BluetoothLeAudio` `CloseGuard` may
  still warn). An
  **unknown** link (`AndroidLink.UNKNOWN`: no permission, no bonded address yet, no answer) has its own card line (`AndroidLine.UNKNOWN`, "Paired — Android's connection
  state isn't known (yet)") and is never rendered as "not connected". Transitions are logged once per change with their trigger; every session end is logged with its
  cause (`SessionDiagnostics`: the user's Disconnect tap, or the lost channel plus the age of the last inbound frame). The pairing bond receiver got the same flag, the bond
  state is read directly from the stack at the end of the 45 s wait, that wait is cancelled when the flow ends, and a second `associate()` while one is open is refused
  (`PairingFailure.AlreadyInProgress`).
- **One on-demand channel (since `ai-sessions/0046`).** Besides the MAESTRO session (DLCI 0x02) the app claims only the Message Stream (DLCI 0x04, ADR-032).
  The DLCI 0x08 Case claim of ADR-035/038/039 (`ai-sessions/0042`–`0045`) is **withdrawn by ADR-043**: in `CAP-061` its `0e 04` got no answer (8/8), the Buds closed 12
  further opens within 10–198 ms, and each contended claim closed the other owner's channel. The Case now arrives on the session channel (§3.1).
  Every claim/release is wrapped so a cancelled tap still releases the channel (0044 APP-4). The Quick Settings ANC tile
  (`AncTileService`) is a user tap like the ANC screen's: it switches the mode only while the session is `Ready` and otherwise opens the app — it never connects and never starts
  a service. **Decided (maintainer, chat 2026-09-20): no automatic session opening — the Connection card mirrors Android (read-only) and Connect stays a tap;** the foreground and
  CDM-presence variants above stay unbuilt proposals. **Superseded for the foreground by ADR-044 (maintainer, chat 2026-09-25, `ai-sessions/0047`):** while the app is visible
  and Android reports the Buds connected, the app re-opens the MAESTRO session by itself after a Buds-side `DISC`, when Android's link comes back and
  on resume (one attempt per event, no loop; a Disconnect tap turns it off). The background (CDM-presence) variant stays a proposal.
- **ADR-044 as built (`ai-sessions/0048`, maintainer's detail choices in chat 2026-09-25).** `SessionReopener` (`:data`, owned by `BudsRepositoryImpl`)
  gets three inputs: the session loss (`transport.connectionLost`), every reading of Android's link (`BudsRepository.onAndroidLink`, forwarded by
  `MainActivity` while at least STARTED) and the UI's visibility (`onAppVisible`: `true` on resume, `false` on stop). It opens the session — the same
  Connect sequence as a tap, **including** the short DLCI 0x04 snapshot claim (ANC mode and availability are re-read after a wear change) — when the app
  is visible, Android's link reads connected and no session is open or opening: (a) 1.5 s after a session loss (one bounded delay per event,
  cancelled when the app leaves the foreground; the link is checked when it ends, so an ACL drop re-opens nothing), (b) when a reading turns
  connected, (c) on resume. It is **on from app start** (opening the app with the Buds connected opens the session without a tap); the Disconnect tap
  switches it off until the next Connect tap (in memory, never persisted). One attempt per event; a failed attempt is reported (`Failed` or
  `lastConnectionError`) and never retried; a loss within 10 s of an automatic re-open schedules none (no DISC→reopen→DISC loop). On stop the link
  reading is forgotten, so a pre-background "connected" is never trusted on return. No new permission, service or background work.
- **Why a session ended, from Android's link (`ai-sessions/0048` I-7).** The transport cannot tell a Buds-side `DISC` from an ACL drop (`CAP-062`: all 11
  losses carried `IOException: bt socket closed, read return: -1`). `OsConnectionObserver` therefore emits **every** evaluation (the screen debounces
  for display with `settled`), and `classifySessionLoss` (`:domain`) derives the cause from the readings around the loss — "not connected" from 2 s
  before to 1 s after → the link was lost; otherwise the first reading after the loss (≤ 2 s) → connected = the Buds closed the channel — never from a
  reading older than the loss; no reading near it → nothing is claimed. Windows measured in `CAP-062` (RESULT §4 of `ai-sessions/0048`). **Since
  `ai-sessions/0054` (I-2):** a loss that happened while the app was not visible — or before the app left without a deciding reading — is decided by the
  **first reading after the loss**, however late (readings exist only while the app is on screen, so it is the one taken on return): "not connected" →
  "Android no longer showed the Buds connected when you returned to the app …", "connected" → "The app's channel was closed while the app was in the
  background; Android showed the Buds connected when you returned …" — the latter names no culprit (on return a Buds-side close and a link drop that
  came back look the same). `CAP-063` 16:15:22: loss while hidden, first reading `NOT_CONNECTED` 3.05 s later. **Since `ai-sessions/0064` (F-3, the
maintainer's choice "Final at once"):** `MainActivity` also forwards every reading of Android's Bluetooth adapter (`BluetoothStateObserver`, the same visibility
bound) to `BudsRepository.onBluetoothAdapter`; a TURNING_OFF/OFF reading from 2 s before to 1 s after the loss decides first — "Bluetooth was switched off on this
phone", logged final (never "provisional"). `CAP-066`: both Bluetooth-offs had left a lasting "undetermined (provisional)" — the link read `UNKNOWN` — while the
system log shows `STATE_TURNING_OFF` 0.55 s and 0.81 s before the loss. Every adapter change is logged ("Bluetooth adapter: ON -> TURNING_OFF").
- **Automatic session connecting — (a) decided, (b) not decided:** (corrected 2026-10-03, `ai-sessions/0069`): (a) below is **decided and built** — `DECISIONS.md` ADR-044
  (re-open the MAESTRO session automatically while the app is visible). (b) remains a deferred proposal that needs a maintainer decision and an ADR.
  As first written, automatic session connecting would have to be either
  (a) **foreground-only** — when the app is visible and Android reports the Buds connected, open the MAESTRO session without a tap (needs a decision that a
  visible app may connect by itself, i.e. amending the "user-initiated only" rule above; no new permission or service), or (b) **background, via CDM
  device-presence** — the CDM device-presence-observation API on the existing association, which wakes a `CompanionDeviceService` when the Buds connect
  (needs that service declared in the manifest and a `connectedDevice` foreground service started from the callback within Android 14's
  background-start limits; GrapheneOS may restrict it further — 🟡 HYPOTHESIS, read from the public API surface only, not tried). (a) costs
  a rule change; (b) costs a service, a manifest entry and the most GrapheneOS/Android-14 risk.

### 6.1 Resource Budget (Wakelocks)

**As built:** no wakelock is acquired anywhere (`grep -rn "WakeLock\|PowerManager" android --include=*.kt` → none; `ai-sessions/0058`
A58-ARCH-02). The rules below apply if one is ever added.

- The `ForegroundService` (§6.0a) holds a wakelock only while a command is
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
sealed class BudsError {   // as built, domain/…/BudsError.kt (updated 2026-09-30, ai-sessions/0059)
    data object ConnectionLost : BudsError()
    data class ChannelUnavailable(val channelId: Int, val detail: String?) : BudsError() // socket open failed (`ai-sessions/0039`)
    data class ChannelLost(val channelId: Int, val detail: String?) : BudsError()        // open channel died (`ai-sessions/0039`)
    data class AnswerCutOff(val channelId: Int, val detail: String?) : BudsError()       // claim closed after the request, before its answer (`ai-sessions/0062` F-3); never retried
    data object Timeout : BudsError()
    data class MalformedFrame(val raw: ByteArray) : BudsError()
    data object UnsupportedFirmware : BudsError()    // a write refused by the Safe-Mode gate (§8.1, ADR-042)
    data object PermissionDenied : BudsError()
    data object NotPaired : BudsError()              // no bonded Buds (0044 APP-8)
    data class CommandRejected(val reasonCode: Int, val detail: String) : BudsError() // a Message Stream NAK (0044 APP-3)
    data class MaestroChannelUnknown(val channelId: Int?) : BudsError() // no/unknown pw_rpc channel announced (ADR-034)
    data class MaestroRejected(val detail: String) : BudsError()        // pw_rpc error status (ADR-034)
    data object AncNotAllowed : BudsError()          // ANC Set not sent: Settable 0x00, the Buds would NAK it (ADR-049)
    data object NoNewBatteryReading : BudsError()    // Refresh battery got no "Battery updated" frame (`ai-sessions/0052`)
    data object AncModeListTooShort : BudsError()    // press-and-hold list would keep < 2 modes (ADR-046); nothing sent
    data object AncModeListNotRead : BudsError()     // press-and-hold list not read on this connection (ADR-046); nothing sent
    data object SessionOpening : BudsError()         // a write while the session is (re)opened (`ai-sessions/0056` U-2; EQ too, 0059)
    data object UnreadableAnswer : BudsError()       // ReadSetting answered OK with an undecodable value (`ai-sessions/0059`)
    data object SettingNotReadable : BudsError()     // no read request exists for the field (`ai-sessions/0069`; was Unknown("EQ field not readable"))
    data class Unknown(val cause: Throwable) : BudsError()
}
```

`:hardware` and `:data` convert all caught exceptions into this type; `BudsRepository`
exposes the errors as flows (`eqError`, `settingsError`, `batteryRefreshError`, …), and
`:app`'s `MainActivity` (ADR-048) collects them into `:ui`'s `OpenControlUiState` — the
role the earlier design name `BudsUiState` described (ADR-048; it never became a type) — so the
Compose layer can render a specific, actionable message per failure mode rather than a
generic error banner.

**Since 1.0.1 (`ai-sessions/0069`):** every error type has its own sentence, pinned as literal text in `UserMessageTest` (`Unknown` reads "Unexpected
error: …", never "Something went wrong"); a message says what was observed and names a cause only as a possibility — another app is named only for the
Fast Pair channel, the one channel another client is known to claim (ADR-032). **A decoder that throws** costs one frame: `CodecRouter` decodes each
frame inside `decodeGuarded`, reports the frame as malformed and logs the exception's class ("Decoder fault on channel N: …") — the collector that feeds
the router has no exception handler and would otherwise stop for the life of the process (A68-APP-01). **Around socket calls** `RfcommBudsTransport`
converts a `SecurityException` into `PermissionDenied` and any other `RuntimeException` into `ChannelLost` with its text, on write, read and close
(A68-APP-10). The UI texts are English only; they are Kotlin literals, not string resources (A68-APP-15, `TODO.md`).

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
    val timestampMillis: Long,   // as built (was `timestamp: Instant` in the design)
)
```

`CodecRouter` (§5) emits these on a separate `Flow<UnidentifiedFrame>` exposed
by `BudsRepository`, independent of the normal command/state pipeline. A
Debug UI screen (gated behind the same "Debug mode" setting as raw frame
logging, §12; navigational placement per §2.4 — a non-primary entry point,
never part of the main flow) subscribes to this flow so unclassified wire data is visible
and inspectable rather than silently dropped — consistent with the
evidence-based reverse-engineering principle in `AGENTS.md` §6/`PROJECT_RULES.md`
§1.

## 8. Firmware / Protocol Compatibility

Because `libmaestro`'s wire format can change across Pixel Buds firmware
revisions, each entry in `PROTOCOL.md` carries the firmware/library version it was
verified against (and each `.proto` file would, if one is ever added — none exists as a
build input, `DECISIONS.md` ADR-041). **As built (ADR-042):** `UnsupportedFirmware` (§7) is
returned when a *write* is refused by the Safe-Mode gate below; an inbound frame that matches no known shape is
`MalformedFrame`/`UnidentifiedFrame`, never a best-effort parse that could misreport battery/ANC state.

### 8.1 Startup Handshake

> **Implemented 2026-09-24 (`DECISIONS.md` ADR-042, maintainer-approved design; `SafeModeGate`, `BudsRepositoryImpl.writeGate`).**
> The firmware strings come from the Buds' unsolicited `GetSoftwareInfo` on DLCI 0x02 (every connection, 22–102 ms after the open);
> the allowlist is `{release_5.203}`. Message Stream commands (ANC Set, Ring/Stop) additionally require the Pixel Buds Pro 2's Fast
> Pair Model ID (`da 2d b1`) on the same claim; EQ writes are refused if a different Model ID was seen. Reads are never gated. A refused
> write returns `UnsupportedFirmware`, sends nothing, and the Connection screen shows a Safe Mode card with the detected firmware and
> Model ID. The design text below stays as written; where it says "read the firmware first", the app waits (bounded) for the Buds'
> own announcement instead of sending a request.
> **Corrected 2026-09-24 (`ai-sessions/0046`, `CAP-061`):** the announcement's payload carries a fixed64 field 5 (wire type 1); the reader returned nothing
> for it, so the gate refused every write on the verified firmware. `Proto.fields` now skips fixed64/fixed32 fields and the tests use the real bytes of
> `CAP-061` frame 1508 (`Cap061Fixtures.kt`).

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

- **Zero location tracking:** `BLUETOOTH_SCAN` is **not declared** — nothing in the app scans. If the bounded
  battery-advertisement scan of ADR-006 is ever built, it is declared with
  `android:usesPermissionFlags="neverForLocation"` ((corrected 2026-10-03, `ai-sessions/0069`), `A68-ARCH-01`: this bullet said the manifest
  declares it). `BLUETOOTH_PRIVILEGED` and any `ACCESS_*_LOCATION` permission are never requested.
- **Permissions, as declared** (`grep -n uses-permission android/*/src/main/AndroidManifest.xml`):

  | Permission | Module | Why |
  |---|---|---|
  | `BLUETOOTH_CONNECT` | `:hardware` | RFCOMM sockets to the paired Buds, bonded-device list, profile state |
  | `POST_NOTIFICATIONS` | `:hardware` | the foreground-service notification |
  | `FOREGROUND_SERVICE` | `:hardware` | the connection service |
  | `FOREGROUND_SERVICE_CONNECTED_DEVICE` | `:hardware` | its type on API 34+ |

  The merged manifest adds AndroidX Core's private `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (signature level, this app only). No `INTERNET`.
- **Companion Device Manager (CDM):** initial pairing/discovery uses
  `CompanionDeviceManager` (API 26+) instead of custom BLE scanning — this
  delegates the scan UI to the OS and grants the app access only to the
  explicitly selected device. See `DECISIONS.md` ADR-005.

### 9.0a First-time pairing flow (added `ai-sessions/0033`)

Concrete hand-off from CDM's OS-owned picker to `:hardware`'s own `BudsTransport`, since Phase 4/6 need
this sequence explicit rather than inferred from ADR-005's decision alone:

1. **UI trigger** — the Connection/Pairing screen (§2.4 above) shows a "Pair a device" affordance when
   `BluetoothAdapter.getBondedDevices()` (already-paired path) yields no Buds Pro 2. Tapping it is the
   user-visible trigger CDM itself requires — never invoked automatically on app start.
2. **`CompanionDeviceManager.associate(AssociationRequest, ...)`**, built with a
   `BluetoothDeviceFilter` with a **name pattern** — `(?i).*Pixel\s*Buds.*` (`BudsCompanionPairing.kt`) — and no address
   ((corrected 2026-10-03, `ai-sessions/0069`), `A68-ARCH-02`: the text said "no name/address hardcoded"); the OS's own picker UI lets the user identify
   the Buds, consistent with never hardcoding a MAC per `AGENTS.md` §7/§9. `singleDevice(true)`
   since this project targets exactly one paired device (`ARCHITECTURE.md` §15, `PROJECT.md`
   non-goals).
3. **OS picker UI** renders (system-owned, not this app's own Compose UI) — the user selects the Buds
   Pro 2 from the list CDM itself populates via its own (OS-level, not this app's) scan.
4. **Callback** (`AssociationRequest` result, delivered via a registered `ActivityResultLauncher` or the
   `CompanionDeviceManager.Callback` API, API-level-dependent) hands the app a `BluetoothDevice`
   reference for the selected device.
5. **Classic bonding** — if the returned device is not yet bonded, the app calls
   `BluetoothDevice.createBond()` and observes `BluetoothDevice.ACTION_BOND_STATE_CHANGED`;
   `PROTOCOL.md` §5.1's classic BR/EDR link establishment (SSP or CTKD, depending on whether a prior LE
   link exists — `DECISIONS.md` ADR-030) happens at the OS/Bluetooth-stack level, invisible to this
   app's own code beyond observing the bond-state broadcast.
6. **Hand-off to `BudsTransport`** — once bonded, the resolved `BluetoothDevice` is what
   `RfcommBudsTransport`'s constructor/factory consumes (§2.1's table) to open each DLCI's own RFCOMM
   socket. `ConnectionStateMachine.onConnectRequested()`/`onLinkEstablished()`/`onReady()` (§2.1) drive
   `ConnectionState` through exactly this hand-off, matching `PROTOCOL.md` §5's documented sequence.
   **As built and corrected `ai-sessions/0041`:** the app first asks for the runtime permissions it needs (`BLUETOOTH_CONNECT`, shown on GrapheneOS as
   *Nearby devices*; `POST_NOTIFICATIONS`) — on start and on every resume, with a distinct state for "not asked", "denied" and "blocked (open app
   settings)"; without `BLUETOOTH_CONNECT` the screen says so instead of "not paired". Step 2 first **reuses** this app's existing CDM association (no duplicate
   request); older duplicate associations for the same address are removed with `disassociate()` (own associations only). Step 4's device is resolved from
   `AssociationInfo.associatedDevice`, falling back to the address **upper-cased** (CDM's `MacAddress.toString()` is lower-case and `getRemoteDevice(String)`
   rejects that — the cause of "could not resolve the selected device"). Step 5 skips `createBond()` when the device is already bonded, waits when it is
   bonding, and classifies a bond that falls back to `BOND_NONE` (never bonding = "not in pairing mode", bonding first = rejected) or times out (45 s). The
   bonded Buds is found by the address of the CDM association, not by its name. Every step is logged (always-on, no address).
7. **Reconnection** (every subsequent app launch/Bluetooth toggle) skips steps 1–4 entirely —
   `BluetoothAdapter.getBondedDevices()` already has the device, so the app goes directly to step 5's
   bonding check (normally a no-op, since the link key is already stored) and step 6.
8. **Which bonded device (as built since 1.0.1, `ai-sessions/0069`, A68-APP-04/05; the maintainer's choice in chat 2026-10-03):** the device whose
   address is this app's CDM association. Without an association, a **single** bonded device whose name contains "Pixel Buds" is used (as before); with
   **two or more** the app picks none — status `SeveralBudsPaired`, "More than one Pixel Buds device is paired with this phone. Tap Pair a device to
   choose the one to control." — and the user chooses in Android's picker (`PairingLogic.chooseBondedOrAsk`; the set of bonded devices has no order, so
   "the first" was arbitrary). Another model picked by mistake is caught by Safe Mode's Model ID check (ADR-042).
   **Settings → "Use different Buds"** disconnects and removes this app's own CDM associations (`BudsCompanionPairing.forgetAssociations`; the Bluetooth
   pairing in Android stays); until a new association exists no device is taken by its name (in memory — after a process restart a single bonded
   "Pixel Buds" device is used again). No address is stored by the app and no in-app device list exists (`AGENTS.md` §7).
- **Local state persistence:** as built, only the Debug-mode switch and — since `ai-sessions/0062` — the dark-mode choice are stored (plain AndroidX
  DataStore Preferences, one file — non-sensitive values, §2). Future user data (e.g. custom EQ profiles) would use DataStore, **encrypted where
  applicable** (`AGENTS.md` §10); no EQ or battery value is persisted (the hardware is the source of truth, §3.1).
  Nothing is ever transmitted off-device (see `AGENTS.md` §1 and §9 for the enforcement rules). (Aligned 2026-09-24,
  0044 AR-3 — this bullet used to say "encrypted DataStore for custom EQ profiles and last-known battery".)
- **Links out (`ai-sessions/0064`, DECISIONS.md ADR-050 and its 2026-10-02 Update):** Settings → Info's two fixed GitHub URLs (README, issues) are handed to another app with `Intent.ACTION_VIEW`, only on a
  tap; no WebView, no `INTERNET`, no `<queries>` (developer.android.com: `startActivity()` "doesn't require package visibility"); `ActivityNotFoundException` ⇒ a
  message with the address. The licence text is bundled (`android/ui/src/main/res/raw/license.txt`, a test keeps it equal to `LICENSE`).
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

**Decided 2026-09-13: Hilt** (`DECISIONS.md` ADR-028) — Hilt/Dagger does **not** touch the
`com.google.android.gms.*` namespace and does not itself require Google Play Services, so it does
not conflict with the Zero-GMS rule in `AGENTS.md` §1, and it reduces `:app`'s own composition-root
boilerplate across `:domain`/`:data`/`:hardware`/`:ui` (§2). `:app` is a real Hilt composition root:
`@HiltAndroidApp` `Application`, `@AndroidEntryPoint` `MainActivity`, `@Module`/`@InstallIn` bindings
for `BudsTransport`/`BudsRepository`. Per the dependency policy in `AGENTS.md` §10, the Hilt version
is pinned (no `+`) and confirmed to bundle no network/analytics SDK transitively
(`./gradlew :app:dependencies`).

**No longer an open question** — this section previously left Hilt vs. a manual service locator
undecided (see §15's "Already decided, not open" list, updated to match).

## 11. State Management

- Kotlin `StateFlow`/`SharedFlow` for all reactive state, per `AGENTS.md` §3.
- `LiveData` is not used in this project (decided; superseded by the
  Compose-first, coroutines-based approach — see `AGENTS.md` §3). This is not
  an open question.

## 12. Logging

- A light logging abstraction (e.g. `BleLogger`) writes to `Logcat` and,
  optionally, to a local in-app ring buffer for an "Export debug log" feature
  — never off-device (see `AGENTS.md` §9). **As built (2026-09-27):** the ring buffer keeps the last 20,000 lines and the export is written
  whole to a file the user picks in Android's own "save as" dialog (Storage Access Framework, `ACTION_CREATE_DOCUMENT` — no permission); the
  earlier share-sheet hand-off was cut at 65,536 bytes by the receiving app (`CAP-063-debug-export.log`). This is essential because BLE
  timing issues are often only reproducible with full logs.
- **Always safe to log:** connection state transitions, MTU value, connection
  parameters.
- **Where Debug lives:** the Debug tab of the settings menu behind the top app bar's gear (`ai-sessions/0062`, §2.4); the Debug-mode switch and "Export debug
  log" are there, unchanged.
- **Debug builds only (`ai-sessions/0062` F-8):** `OpenControlApplication` sets `StrictMode.VmPolicy.detectLeakedClosableObjects().penaltyLog()` when
  `BuildConfig.DEBUG`, so a resource finalized without `close()` (`CAP-065`: three "A resource failed to call close.") is logged with its stack; release builds
  set no policy. Local logcat only.
  **First debug run, `CAP-066` (`ai-sessions/0063`, maintainer-approved note):** the 5 violations logged are framework objects, not an app leak — the
  `ParcelFileDescriptor` of a `BluetoothSocket` whose `read()` hit end-of-stream (the module's `read()` marks the socket CLOSED without closing `mPfd`, so the
  app's later `close()` returns "Already closed") and a `BluetoothLeAudio` profile proxy (its `CloseGuard` is opened in the constructor and never closed) —
  🟡 per the AOSP Bluetooth module source `android16-qpr2-release` (`CAP-066-FINDINGS.md` §8). Expect these two in debug logcats.
- **Bluetooth adapter changes** (`ai-sessions/0064` F-3) are logged always-on as state transitions ("Bluetooth adapter: ON -> TURNING_OFF"), and a session loss
  around one is logged "Session loss cause: Bluetooth was switched off on this phone" (§6.0b).
- **Gated behind an explicit, off-by-default "Debug mode" setting:** raw
  sent/received frame bytes (hex dump) — per `AGENTS.md` §9, verbose
  hex-dump logging of payloads containing device identifiers is not on by
  default.
- **Never logged at `INFO` level or above:** the paired device's MAC address —
  use a truncated/hashed form if a log line needs to disambiguate devices, per
  `AGENTS.md` §9.

## 13. Testing Strategy

As built (rewritten 2026-09-24, 0044 AR-2):

- **Unit tests** (`:data`): every codec (`CodecRouter`, ANC/Ring/Battery/Case/Model-ID/ACK-NAK/pw_rpc/HDLC/EQ) against real
  `tshark`-extracted capture bytes, fuzz/property tests (random and truncated input never throws; a valid frame decodes after a
  garbage prefix and a reset), `SafeModeGate`, and `BudsRepositoryImpl` against `FakeBudsTransport` (in `:hardware`'s **test
  fixtures**, never in the app) — pure JVM, JUnit 5 + Kotest assertions.
  Since `ai-sessions/0069`: a **structured fuzz** (`OversizedLengthTest`) mutates real captured frames *inside* their payload and re-seals them with a
  valid CRC — random bytes almost never pass the CRC, so the readers behind it were not reached by the older fuzz tests — with a time limit, because one
  of the defects it covers was an endless loop.
- **Unit tests** (`:hardware`): `RfcommBudsTransport` with scripted sockets (including exceptions that are not `IOException`), `ConnectionStateMachine`,
  pairing/link logic.
- **Unit tests** (`:ui`, Robolectric): each tab's wording and enabled states, every error sentence as literal text. **`:app` has no tests** (the tile
  service, the foreground service and `MainActivity` are covered only through the pure functions they call) — `TODO.md`.
- **Unit tests** (`:domain`): pure domain logic (status derivation, tile cycle). There are no ViewModels or use cases to test.
- **CI** (`.github/workflows/android.yml`, 2026-09-24): builds, runs all unit tests and lint, and asserts no `INTERNET` permission
  in any manifest and in the merged manifest.
- **Hardware**: a manual test plan (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, the re-test lists in `ai-sessions/`), since no earbuds exist
  in a CI runner. No GATT client exists to test.
- Per `AGENTS.md` §11, any bug fix tied to a specific malformed/unexpected
  frame adds a regression test with that exact byte sequence (device
  identifiers redacted first).

## 14. Build Configuration Notes

- Kotlin + Jetpack Compose BOM, Gradle version catalog (`libs.versions.toml`)
  for pinned dependency versions.
- **No protobuf runtime, no `.proto` build inputs** (`DECISIONS.md` ADR-041, 2026-09-24): the wire codec is hand-written
  Kotlin; schemas recovered from the APK are documentation. (`protobuf-kotlin-lite` via the Gradle plugin applies only
  if a `.proto` build input is ever introduced — `AGENTS.md` §4.)
- No `INTERNET` permission anywhere in any module's manifest; asserted by CI (`.github/workflows/android.yml`) on every
  change under `android/` (see `AGENTS.md` §1).
- **Release signing and version** (`ai-sessions/0065`, 2026-10-02): `android/app/build.gradle.kts` signs the `release` build type with a `signingConfig`
  whose four values (`OPENCONTROL_STORE_FILE`, `_KEY_ALIAS`, `_STORE_PASSWORD`, `_KEY_PASSWORD`) come from Gradle properties (`~/.gradle/gradle.properties`)
  or the environment — never from the repository. Without them `packageRelease` fails with a clear message; no unsigned or debug-signed release APK is
  built, and debug builds, tests and lint need no key. `versionCode` = major × 10000 + minor × 100 + patch (1.0.0 = 10000). No R8/minify (the release is the
  code the tests and hardware runs checked). Releases are built by `scripts/release.sh` in a clean worktree (`RELEASING.md`). App name and launcher label:
  `DECISIONS.md` ADR-051.

## 15. Open Architecture Questions

> Move to `DECISIONS.md` once decided, following the ADR template.

**Re-checked `ai-sessions/0045` (2026-09-24):** the HID question below is closed; no architecture question is open.
**Earlier, `ai-sessions/0033` (2026-09-18):** the HID-surface question below is confirmed still the
only genuinely open *architecture* question — no new one surfaced during this session's Phase 1 refresh.
A related but distinct gap *was* found (the missing consolidated implementation-unblock ADR for DLCI
0x02's individual §4.5 settings) — that is a protocol/decision-gate matter, not an architecture
question, so it is tracked as a `PROPOSAL —` note in §5a above and in
`ai-sessions/0033_FEATURE_RESULT_2026_09_18.md`, not added here. *(Resolved since: ADR-036 reads, ADR-045/046/047 writes — §5a's settings row.)*

- [x] **Closed 2026-09-24 (maintainer-approved in chat, `ai-sessions/0045`):** the HID surface is Android's standard
      head-tracker HID sensor (`#AndroidHeadTracker#`, `CAP-016-FINDINGS.md` §10; spatial audio), 🟡 HYPOTHESIS (strong) that
      Android itself consumes it — no control feature of this app routes through HID, so `BudsTransport`/`CodecRouter` need no
      HID path. Tracked as `SPATIAL-001` for completeness. Original item: Added 2026-08-14: is the observed Bluetooth HID surface (§1) architecturally relevant to
      `:hardware`/`BudsTransport` — i.e. does any control feature this app needs actually route
      through HID reports rather than RFCOMM/GATT — or is it exclusively used by parts of the
      official app/OS this project doesn't need to replicate? Unresolved; no HID report content
      has been captured yet. **If confirmed**, `BudsTransport` (§2.1) needs a third input path
      alongside RFCOMM and GATT (an `InputManager`/HID report listener), and `CodecRouter` (§5)
      would need an equivalent HID report decoder — this is a real, not cosmetic, change to both
      components, which is why this item stays open rather than being assumed away.

> Already decided, not open: persistent settings storage (AndroidX DataStore, encrypted where
> applicable — see §2/§9 and `AGENTS.md` §10); state management approach
> (`StateFlow`/`SharedFlow` only — see §11); passive BLE scanning policy for
> the Fast Pair Battery Notification (bounded exception — see §9.1,
> `DECISIONS.md` ADR-006); **single-device support only for v1** — the app
> targets exactly one paired Pixel Buds Pro 2 at a time (a **non-goal** in `PROJECT.md`, not a Definition-of-done
> criterion — (corrected 2026-10-03, `ai-sessions/0069`)); simultaneous multi-device support is explicitly out
> of scope until separately proposed and recorded in `DECISIONS.md`;
> **dependency injection — Hilt** (see §10, `DECISIONS.md` ADR-028, decided
> 2026-09-13); **Find My Buds Case/"both simultaneously" — out of scope for v1**
> (`PROJECT.md` non-goals, `DECISIONS.md` ADR-027, decided 2026-09-13) — Left/Right
> ring is unaffected and already implemented; **minimum supported Android API level
> — API 34 (Android 14), same as compile/target SDK** (§1, `DECISIONS.md` ADR-029,
> decided 2026-09-13) — no lower-API compatibility path is pursued; `CompanionDeviceManager`
> (API 26, ADR-005) is trivially satisfied at this floor (the generic battery broadcast,
> §4 option 0, is `@SystemApi` and not used — `PROTOCOL.md` §4.3, 2026-09-30).

## 16. Attribution

Protocol structure knowledge is derived from the public reverse-engineering
work of the `qzed/pbpctrl` project (Linux/Rust). No source code from that
project is reused directly; only documented protocol/frame knowledge informs
this Android-native implementation.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ARCHITECTURE.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ARCHITECTURE
