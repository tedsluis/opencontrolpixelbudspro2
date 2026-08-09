# Test Plan: Pixel Buds Pro 2 — Action & Behavior Catalog

**Role of this document:** this is the stable **catalog** of known and suspected Pixel
Buds Pro 2 actions/behaviors — *what* could be investigated, each with a permanent
**Test-ID**. It answers *"what do we know exists, and have we found protocol evidence for
it yet?"*

This document does **not** describe how to run a capture session (see
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` for that), and it does **not** hold the actual
protocol findings (see `PROTOCOL_NOTES.md` / `PROTOCOL.md` for those). It only points at
them. See `AGENTS.md` §0.1 and `PROJECT_RULES.md` §2 for why findings live in exactly one
place.

**Status:** sections 1 and 2 checked and expanded based on:
* (a) [screenshots](./SCREENSHOTS_PIXEL_BUDS_WEB_APP.md) of the official web companion app (`http://mypixelbuds.google.com/`, strongest source — straight from the app itself).
* (b) [screenshots](./SCREENSHOTS_PIXEL_BUDS_APP.md) of the official Android app (`https://play.google.com/store/apps/details?id=com.google.android.apps.wearables.maestro.companion`).
* (c) the official Google support pages (`support.google.com/googlepixelbuds`). Sections 3 and 4 have now also been validated.

---

## 0. How to read this document

### 0.1 A Test-ID is not a Group — this is the core design decision

`CAPTURE_BLUETOOTH_HCI_SNOOP.md` groups actions into **capture scenarios** (Group A–Q,
plus Z for pipeline validation) — these describe *how to efficiently run a capture
session* covering several related actions at once. A Group is an operational bundle, not
a research question.

**A Test-ID in this catalog is the research question itself** — one Test-ID per distinct
action/behavior, regardless of which Group it happens to be captured under. Most Test-IDs
map to exactly one Group; a few don't map to any Group yet (see §9) because no capture
procedure has been designed for them (typically because they're long-running background
processes, not a discrete tap).

The chain this produces, matching the project's evidence rules
(`PROJECT_RULES.md` §1, §3):

```
Test-ID (this doc)  →  Capture scenario / Group  →  Capture session (Capture Index, CAPTURE §9)  →  frame(s)  →  finding (PROTOCOL_NOTES.md / PROTOCOL.md)
```

### 0.2 Two unrelated confidence axes — don't conflate them

- **Existence source** (this document's 🟢🔵🟡🔴 icons): *does this action/behavior exist
  at all*, per official screenshots/support docs/secondary sources. This is about the
  product, not the protocol.
- **Protocol confidence** (`PROTOCOL_NOTES.md` / `PROTOCOL.md`'s own 🔴🟡🟢⚪ /
  FACT-HYPOTHESIS-ASSUMPTION labels): *is the wire-level behavior confirmed*.

A row here can be 🟢-confirmed-to-exist while its protocol evidence is still completely
unconfirmed — that's normal, not a contradiction. This document only ever carries the
first axis; the **Evidence** column is a pointer to the second, never a restatement of it.

### 0.3 Reading the tables

| Column | Meaning |
|---|---|
| **ID** | Permanent Test-ID. Never reused or renumbered once assigned (same rule as `DECISIONS.md` ADR numbers and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` `CAP-NNN` IDs). |
| **Description** | The action or behavior being investigated. |
| **Initiator** | Who/what triggers it: User (App), User (Hardware), App (Auto), Buds/Case (Auto). |
| **Capture scenario(s)** | The Group letter(s) in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 where this is captured, or "—" if none exists yet (see §9). |
| **Existence source** | 🟢 Screenshot / 🔵 Official / 🟡 Secondary / 🔴 Unconfirmed — see legend below. |
| **Note** | Background/context, carried over from the original research. |
| **Evidence** | Pointer only, e.g. `PROTOCOL_NOTES.md §4.1`, added once a finding exists. Blank (`—`) until then — never fill this with the finding itself. |

**Existence-source legend:**
- 🟢 **Screenshot** — seen directly in the project's own app screenshots
- 🔵 **Official** — confirmed via support.google.com (or another official Google source)
- 🟡 **Secondary** — confirmed via a reliable secondary source (e.g. 9to5Google, Android
  Authority), not directly from Google itself
- 🔴 **Unconfirmed** — still needs empirical verification during a capture

### 0.4 ID area prefixes

| Prefix | Area |
|---|---|
| `PAIR` | Pairing / bonding |
| `ANC` | Active Noise Control modes |
| `CONV` | Conversation Detection |
| `MULTI` | Multipoint |
| `EQP` | Equalizer presets |
| `EQS` | Equalizer sliders / custom save |
| `TOUCH` | Touch controls (toggle + physical gestures) |
| `HEAD` | Head gestures (toggle + physical gestures) |
| `HOLD` | Press-and-hold configuration |
| `AUDIO` | Mono audio, volume EQ, volume balance |
| `FW` | Firmware/device info (version, serial, connection status, manual check) |
| `FWUPD` | Firmware background download/install |
| `INEAR` | In-ear detection (toggle + sensor events) |
| `CASE` | Case lid/button, case sounds, buds in/out of case |
| `FIND` | Find My Buds |
| `OBS` | App-driven passive/automatic observation |
| `BATT` | Battery reporting mechanisms |
| `LOUD` | Loud Noise Protection |
| `ADAPT` | Adaptive Audio |

---

## 1. Catalog — User actions from the Pixel Buds app

_Make sure the buds are connected and active._

| ID | Description | Initiator | Capture scenario(s) | Existence source | Note | Evidence |
|---|---|---|---|---|---|---|
| `ANC-001` | ANC → Off | User (App) | B | 🟢 | | — |
| `ANC-002` | ANC → Noise Cancellation | User (App) | B | 🟢 | Sends a configuration command to the buds. | — |
| `ANC-003` | ANC → Adaptive | User (App) | B | 🟢🔵 | Pro 2-specific; added in firmware 4.467 (Sept. 2025). Automatically adjusts volume to the environment. | — |
| `ANC-004` | ANC → Transparency | User (App) | B | 🟢 | | — |
| `CONV-001` | Toggle 'Conversation Detection' on/off | User (App) | C | 🟢 | Switches to Transparency and pauses media when you speak. | — |
| `MULTI-001` | Toggle 'Multipoint' on/off | User (App) | C | 🟢 | Connects to 2 Bluetooth devices simultaneously; may trigger an SDP/connection update. | — |
| `EQP-001` | EQ preset: Standard | User (App) | D | 🟢 | Full, fixed preset list from screenshots — each preset is a separate value to capture. | — |
| `EQP-002` | EQ preset: Bass Boost | User (App) | D | 🟢 | | — |
| `EQP-003` | EQ preset: Bass Reduction | User (App) | D | 🟢 | | — |
| `EQP-004` | EQ preset: Balanced | User (App) | D | 🟢 | | — |
| `EQP-005` | EQ preset: Vocal Boost | User (App) | D | 🟢 | | — |
| `EQP-006` | EQ preset: Clarity | User (App) | D | 🟢 | | — |
| `EQP-007` | EQ preset: Last saved | User (App) | D | 🟢 | Restores a previously saved custom profile. | — |
| `EQP-008` | Save current EQ as a new preset ('Save') | User (App) | D | 🟢 | A distinct write action compared to preset selection — possibly a different protocol command (write vs. select). | — |
| `EQS-001` | EQ slider: High treble | User (App) | E | 🟢 | 5-band EQ; each band is a separate, potentially distinct protocol field. | — |
| `EQS-002` | EQ slider: Treble | User (App) | E | 🟢 | | — |
| `EQS-003` | EQ slider: Mid | User (App) | E | 🟢 | | — |
| `EQS-004` | EQ slider: Bass | User (App) | E | 🟢 | | — |
| `EQS-005` | EQ slider: Low bass | User (App) | E | 🟢 | | — |
| `TOUCH-001` | Toggle 'Touch controls' fully on/off | User (App) | F | 🟢🔵 | Enables/disables the touch sensors on the buds. | — |
| `HEAD-001` | Toggle 'Head gestures' fully on/off | User (App) | F | 🟢🔵 | Pixel Buds Pro 2-exclusive. | — |
| `HOLD-001` | Press-and-hold Left → Toggle ANC | User (App) | G | 🟢 | Binary choice per earbud. | — |
| `HOLD-002` | Press-and-hold Left → Digital assistant | User (App) | G | 🟢🔵 | Per screenshot: "Works on Android only." | — |
| `HOLD-003` | Press-and-hold Right → Toggle ANC | User (App) | G | 🟢 | | — |
| `HOLD-004` | Press-and-hold Right → Digital assistant | User (App) | G | 🟢🔵 | | — |
| `HOLD-005` | Check/uncheck a specific ANC mode in the press-and-hold rotation | User (App) | G | 🟢 | Checkbox list: Noise Cancellation, Off, Adaptive, Transparency — determines which modes the buds locally remember for the touch cycle. | — |
| `AUDIO-001` | Toggle 'Mono audio' on/off | User (App) | H | 🟢 | | — |
| `AUDIO-002` | Toggle 'Volume EQ' on/off | User (App) | H | 🟢 | Boosts bass/treble at lower volume. | — |
| `AUDIO-003` | Shift the 'Volume balance' (Left/Right) | User (App) | H | 🟢 | **Note:** stored locally on the earbuds themselves (persistent write, works across devices) — explicitly stated in the app's own screenshot. | — |
| `FW-001` | Tap the 'Firmware up to date' check | User (App) | I | 🟢 | Forces a manual update check. | — |
| `FW-002` | View firmware version per component (L/R/Case) | User (App, view) | I | 🟢 | Possibly triggers a status query when 'More settings' is opened. | — |
| `FW-003` | View serial numbers per component | User (App, view) | I | 🟢 | Same as above. | — |
| `FW-004` | View connection status ("Earbud status: Connected") | User (App, view) | I | 🟢 | | — |
| `INEAR-001` | Toggle 'In-ear detection' on/off | User (App) | J | 🟢 | Automatically plays/pauses audio when worn/not worn. | — |
| `CASE-001` | Toggle case sound 'Earbuds replaced' on/off | User (App) | J | 🟢 | Setting stored on the chip inside the case. | — |
| `CASE-002` | Toggle case sound 'Other notifications' on/off | User (App) | J | 🟢 | Covers: charging started, low battery, pairing successful, errors. | — |
| `FIND-001` | Play sound on Left earbud ('Find My Buds') | User (App) | K | 🔵🟡 | Via Find Device/Find Hub integration in the app; individually addressable per component. | — |
| `FIND-002` | Play sound on Right earbud | User (App) | K | 🔵🟡 | | — |
| `FIND-003` | Play sound on Case | User (App) | K | 🔵🟡 | | — |
| `FIND-004` | Play sound on both earbuds simultaneously | User (App) | K | 🟡 | | — |

---

## 2. Catalog — User actions via the case & buds (hardware)

_Physical interactions with the device._

| ID | Description | Initiator | Capture scenario(s) | Existence source | Note | Evidence |
|---|---|---|---|---|---|---|
| `CASE-003` | Open the charging case lid | User (Hardware) | M | 🟢🔵 | Triggers a BLE advertisement / Fast Pair pop-up. | — |
| `CASE-004` | Remove Left earbud from the case | User (Hardware) | M | 🔵 | Changes state to 'out of case'. | — |
| `CASE-005` | Remove Right earbud from the case | User (Hardware) | M | 🔵 | | — |
| `INEAR-002` | Insert Left earbud into the ear | User (Hardware) | M | 🔵 | Triggers the in-ear sensor (audio starts, if In-ear detection is on). | — |
| `INEAR-003` | Insert Right earbud into the ear | User (Hardware) | M | 🔵 | | — |
| `TOUCH-002` | Tap once on a bud | User (Hardware) | N | 🔵 | Play/pause **or** answer call **or** (Pixel Buds Pro only) leave Conversation Detection mode **or** confirm a choice with Gemini. | — |
| `TOUCH-003` | Double-tap on a bud | User (Hardware) | N | 🔵 | Next track **or** end/reject call **or** stop Gemini. | — |
| `TOUCH-004` | Triple-tap on a bud | User (Hardware) | N | 🔵 | Previous track. | — |
| `TOUCH-005` | Swipe forward on a bud | User (Hardware) | N | 🔵 | Raise volume. | — |
| `TOUCH-006` | Swipe backward on a bud | User (Hardware) | N | 🔵 | Lower volume. | — |
| `TOUCH-007` | Press and hold on a bud | User (Hardware) | N | 🔵 | Cycles ANC mode (incl. Adaptive) **or** activates Gemini/digital assistant, depending on per-earbud configuration (see `HOLD-*`). Requires Android 6.0+. | — |
| `HEAD-002` | Head gesture: Nod | User (Hardware) | O | 🔵 | Answers a call. Can also reply to a text via dictation if 'Spoken notifications' is on — English only. | — |
| `HEAD-003` | Head gesture: Shake | User (Hardware) | O | 🔵 | Rejects a call. Can also dismiss a text reply under the same condition. | — |
| `CONV-002` | User starts speaking (voice), triggering Conversation Detection | User (Hardware) | P | 🟢 | Triggers Conversation Detection (if on) — pauses media, switches to Transparency. | — |
| `CASE-006` | Place buds back in the case and close the lid | User (Hardware) | M | 🔵 | Terminates the active Bluetooth Classic connection. | — |
| `CASE-007` | Hold the case button for 30 seconds (case open, buds inside, plugged into power) | User (Hardware) | P | 🔵 | This is a **full factory reset**, not just pairing mode. Also resets the Find My Device link on the Pro 2 — destructive, do this deliberately and last (see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P #16). | — |
| `CASE-008` | Press the case button briefly/differently to force pairing mode | User (Hardware) | P | 🔴 | **Still to verify** — no officially confirmed press duration found for a shorter pairing trigger, separate from the 30s reset. Open question, see §9 below and `PROTOCOL_NOTES.md` §7. | — |

---

## ⚠️ Key finding affecting sections 3 & 4: the official Fast Pair battery spec

While validating, it turned out that battery reporting for this type of earbuds does **not
need to be fully reverse-engineered** — Google has publicly specified it via the **Google
Fast Pair Service (GFPS) "Battery Notification" extension**
(`developers.google.com/nearby/fast-pair/specifications/extensions/batterynotification`).
Key points, taken directly from the official spec:

- Battery is advertised as **3 bytes** in the BLE advertisement: left earbud, right
  earbud, case (in that order). Each byte: 1 status bit (charging yes/no) + a 7-bit
  percentage (0–100; `0bS1111111` = unknown).
- **Trigger:** an update is sent **"when RFCOMM connects, or when the battery value
  changes"** — event-driven, not a fixed polling interval.
- The advertisement is shown for at least 8 seconds when displaying a notification, and is
  automatically hidden after 20 seconds (or sooner, via an explicit "hide" flag).
- **Alternative mechanism, also officially specified:** raw battery data can also be sent
  over RFCOMM once a connection exists, via the **Fast Pair "Message Stream: Device
  Information"** extension — see `BATT-004` below and `PROTOCOL.md` §4.3 Option B. This
  deserves a higher confidence rating than 🔴 now that an officially documented RFCOMM
  battery route is known to exist, even though it's a generic Fast Pair mechanism rather
  than a confirmed Buds-specific `libmaestro` detail.

---

## 3. Catalog — Automatic actions initiated by the app

_Background processes, without a direct tap from the user._

| ID | Description | Initiator | Capture scenario(s) | Existence source | Note | Evidence |
|---|---|---|---|---|---|---|
| `OBS-001` | App launches (after force close) → requests status | App (Auto) | L | 🔵🟡 | Likely via an RFCOMM connect, which per the Fast Pair spec is itself the trigger for a battery update (see the call-out above). | — |
| `BATT-001` | Notification with battery status on every reconnect | App (Auto) | L | 🔵 | Confirmed: "Each time you connect... a notification will appear showing you where battery life stands." | — |
| `OBS-002` | App running in the background (battery polling) | App (Auto) | — | 🔴 | The Fast Pair spec suggests event-driven updates (on value change), not necessarily active polling from the app. Still to confirm whether the official app also actively polls on top of this — no dedicated capture scenario yet, since this is an ambient/long-duration behavior rather than a discrete action; see §9. | — |
| `FWUPD-001` | Background check/download for a firmware update | App/OS (Auto) | — | 🔵 | Confirmed: download happens automatically in the background once connected to Android 6.0+ (~10 min); no dedicated capture scenario yet — long-duration, low priority for early captures; see §9. | — |
| `ADAPT-001` | Adaptive Audio processing — does it require the app to be active? | App (Auto?) | Q (partial) | 🟡 | One source states the feature "requires" the app; unclear whether this is a one-time write or requires the app to stay active. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q #20 captures the triggering/observation; the "does it need the app active" sub-question needs its own experiment (close the official app fully, then retest) — relevant to `ARCHITECTURE.md` §2/§6 `ForegroundService` design. | — |

---

## 4. Catalog — Automatic actions initiated by the hardware (buds/case)

_Sensors and firmware behavior, without a direct action from the app._

| ID | Description | Initiator | Capture scenario(s) | Existence source | Note | Evidence |
|---|---|---|---|---|---|---|
| `BATT-002` | Case broadcasts battery status via BLE advertisement | Buds/Case (Auto) | Q | 🔵 | Official Fast Pair Battery Notification extension: 3 bytes (L/R/Case), advertised when the case opens and/or on value change; visible ≥8s, hidden after 20s or explicitly. Optional when a single bud is inserted/removed. | — |
| `BATT-003` | Buds update battery status while worn | Buds/Case (Auto) | Q | 🔵 | Trigger = "when RFCOMM connects, or when the value changes" — no fixed step size (e.g. "every 1%") is officially specified. | — |
| `BATT-004` | Battery data via RFCOMM after connecting (instead of BLE advertisement) | Buds/Case (Auto) | A / Z | 🔵 | Officially specified alternative channel — Fast Pair "Message Stream: Device Information". Presumed to be the same channel as the `HardwareStatus` hypothesis in `PROTOCOL_NOTES.md` §3.1 — likely not a Buds-specific protobuf schema at all. See `PROTOCOL.md` §4.3 Option B. Piggybacks on any connect capture (Group A or Z) rather than needing its own scenario. | — |
| `INEAR-004` | In-ear sensor reports 'removed' (bud taken out of ear, not placed back in case) | Buds (Auto) | — | 🟢 | Confirmed via screenshot (In-ear detection: "pauses audio when not worn"). **Gap noted during this restructuring:** no existing Group captures this specific transition — `INEAR-002`/`INEAR-003` cover insertion, and `CASE-006` covers full case return, but "worn → removed, still out of case" has no dedicated step yet. Candidate addition to a future Group M revision; see §9. | — |
| `BATT-005` | 'Low battery' notification (case) | Buds/Case (Auto) | — | 🔵 | Confirmed specifically for Pro 2/2a: notification for both low case battery and fully charged case. Opportunistic only — no dedicated scenario, since it requires genuinely low battery; see §9. | — |
| `LOUD-001` | Loud Noise Protection: automatic volume reduction on a sudden loud sound | Buds (Auto) | Q | 🔵 | Added in firmware 4.467. Presumably a purely local DSP action (no phone-side command needed) — worth checking whether a notify frame is also sent to the phone. Does not cover impulse sounds (gunshots, fireworks) per Google. | — |
| `ADAPT-002` | Adaptive Audio: dynamic adjustment of the ANC/Transparency balance based on environment | Buds (Auto) | Q | 🔵🟡 | Works "on the fly" using the Tensor A1 chip in the buds. Unclear whether this generates BT traffic toward the phone (e.g. status sync for the UI) or stays entirely on-device. | — |
| `FWUPD-002` | Firmware installation when placed back in the case (with sufficient charge) | Buds/Case (Auto) | — | 🔵 | Installation timing is hardware-/case-bound (see `FWUPD-001` for download timing, which is app-/OS-bound); no dedicated scenario yet — long-duration; see §9. | — |

---

## 5. `PAIR` / secondary-transport area (cross-referenced from Group A / Pixel 9a §4.2, not duplicated)

| ID | Description | Initiator | Capture scenario(s) | Existence source | Note | Evidence |
|---|---|---|---|---|---|---|
| `PAIR-001` | Pairing / bonding handshake (forget-and-re-pair baseline) | User (Hardware) | A | 🔵 | Lightweight, safely repeatable — see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group A #1 for the exact procedure (do not confuse with `CASE-007`'s factory reset). Also exercised on the Pixel 9a session (§4.2 #1). | — |
| `PAIR-002` | Pairing / bonding handshake from a true factory-reset state | User (Hardware) | P (via `CASE-007`) | 🔵 | Optional, one-time, destructive comparison capture — see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P #16. | — |
| `PAIR-003` | Disconnect and reconnect to an already-bonded device | User (Hardware) | — | 🔵 | Distinct protocol state from `PAIR-001`/`PAIR-002` (no bonding handshake, per `PROTOCOL.md` §5) — captured on the Pixel 9a session (§4.2 #4) today; no dedicated Pixel 7a scenario yet, see §9. | — |
| `GATT-001` | Open the Bluetooth device detail screen in system settings | User (Hardware) | — | 🔵 | May trigger GATT service/characteristic discovery on some Android versions — the secondary BLE/GATT transport (`ARCHITECTURE.md` §1), distinct from the RFCOMM `libmaestro` channel. Captured on the Pixel 9a session (§4.2 #3) today; identified during this catalog's restructuring, no dedicated Pixel 7a scenario yet, see §9. | — |

---

## 6. Firmware/OS-level compatibility notes (not separate tests)

Carried over from the original research, relevant context rather than standalone catalog
entries:

- Pixel Buds Pro 2-specific features (Adaptive ANC, Head Gestures, Loud Noise Protection)
  were added in firmware 4.467 (Sept. 2025) — confirm the test device's firmware is at or
  above this before expecting `ANC-003`, `HEAD-*`, or `LOUD-001` to be present at all.
- `TOUCH-007`'s press-and-hold digital-assistant behavior requires Android 6.0+.

---

## 7. Relationship to `PROTOCOL_NOTES.md` / `PROTOCOL.md`

Once a capture provides evidence for a Test-ID:

1. Record the finding in `PROTOCOL_NOTES.md` (working notes) first, per
   `PROJECT_RULES.md` §2 — include the Test-ID and the `CAP-NNN` capture ID it came from.
2. Once promoted to `PROTOCOL.md`, update this row's **Evidence** column to point at the
   relevant section (e.g. `PROTOCOL.md §4.1`) — a pointer only, not a restated finding.
3. Never leave a Test-ID's Evidence column with anything more than a pointer. If you find
   yourself writing more than a section reference in this column, that content belongs in
   `PROTOCOL_NOTES.md` instead.

## 8. Relationship to the Capture Index

`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9's Capture Index lists actual capture **sessions**
(one row per `CAP-NNN`, potentially covering many Test-IDs at once). This document lists
**Test-IDs** (one row per distinct behavior, potentially captured across many sessions
over time, e.g. once per firmware version). The two are deliberately not merged into one
table — see the discussion in `DECISIONS.md` if this needs revisiting.

## 9. Open items — Test-IDs without a capture scenario yet

Consolidated list of catalog rows not yet covered by a Group in
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1, so they aren't silently forgotten:

- [ ] `OBS-002` — background battery polling (ambient, long-duration)
- [ ] `FWUPD-001` / `FWUPD-002` — background firmware download/install (long-duration,
      low priority for early captures)
- [ ] `INEAR-004` — bud removed from ear without returning to case (genuine gap, no
      existing Group step covers this specific transition — candidate for a Group M
      addition)
- [ ] `BATT-005` — low battery notification (opportunistic only, needs genuinely low
      battery)
- [ ] `PAIR-003` — disconnect/reconnect to an already-bonded device, on the Pixel 7a
      specifically (currently only exercised on the Pixel 9a session)
- [ ] `GATT-001` — GATT service/characteristic discovery trigger, on the Pixel 7a
      specifically (currently only exercised on the Pixel 9a session; the secondary
      BLE/GATT transport per `ARCHITECTURE.md` §1 is otherwise untested on the primary
      device)