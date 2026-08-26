# Test Plan: Pixel Buds Pro 2 — Action & Behavior Catalog

**Role of this document:** this is the stable **catalog** of known and suspected Pixel
Buds Pro 2 actions/behaviors — *what* could be investigated, each with a permanent
**Test-ID**. It answers *"what do we know exists, and have we found protocol evidence for
it yet?"*

This document does **not** describe how to run a capture session (see
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` for that), and it does **not** hold the actual
protocol findings (see the relevant `CAP-NNN-FINDINGS.md` and `PROTOCOL.md` for
those). It only points at them. See `AGENTS.md` §0.1 and `PROJECT_RULES.md` §2
for why findings live in exactly one place.

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
Test-ID (this doc)  →  Capture scenario / Group  →  Capture session (Capture Index, CAPTURE §9)  →  frame(s)  →  finding (CAP-NNN-FINDINGS.md / PROTOCOL.md)
```

### 0.2 Two unrelated confidence axes — don't conflate them

- **Existence source** (this document's 🟢🔵🟡🔴 icons): *does this action/behavior exist
  at all*, per official screenshots/support docs/secondary sources. This is about the
  product, not the protocol.
- **Protocol confidence** (`PROTOCOL.md`'s own 🔴🟡🟢⚪ /
  FACT-HYPOTHESIS-ASSUMPTION labels): *is the wire-level behavior confirmed*.

A row here can be 🟢-confirmed-to-exist while its protocol evidence is still completely
unconfirmed — that's normal, not a contradiction. This document only ever carries the
first axis; the **Evidence** column is a pointer to the second, never a restatement of it.

### 0.3 Reading the tables

| Column | Meaning |
|---|---|
| **ID** | Permanent Test-ID. Never reused or renumbered once assigned (same rule as `DECISIONS.md` ADR numbers and `CAPTURE_BLUETOOTH_HCI_SNOOP.md` `CAP-NNN` IDs). Register every new Test-ID in `id_registry.csv` (repo root) when adding its catalog row — `scripts/lint_docs.py` checks every reference against it (this caught `OBS-003`/`APP-001`/`APP-002`/`GFPS-002` in active use with no catalog row on 2026-08-20, before this registry existed). |
| **Description** | The action or behavior being investigated. |
| **Initiator** | Who/what triggers it: User (App), User (Hardware), App (Auto), Buds/Case (Auto). |
| **Capture scenario(s)** | The Group letter(s) in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §4.1 where this is captured, or "—" if none exists yet (see §9). |
| **Existence source** | 🟢 Screenshot / 🔵 Official / 🟡 Secondary / 🔴 Unconfirmed — see legend below. |
| **Note** | Background/context, carried over from the original research. |
| **Evidence** | Pointer only, e.g. `PROTOCOL.md §4.1`, added once a finding exists. Blank (`—`) until then — never fill this with the finding itself. |

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
| `GATT` | Secondary BLE/GATT transport (service/characteristic discovery) |
| `GFPS` | Google Fast Pair Service phone-side behavior (vs. Buds-initiated) |
| `CALL` | In-call HFP/SCO audio behavior (added 2026-08-14) |
| `APP` | Official-app first-run onboarding (post-pairing handoff to the app UI), added 2026-08-20 |

---

## 1. Catalog — User actions from the Pixel Buds app

_Make sure the buds are connected and active._

| ID | Description | Initiator | Capture scenario(s) | Existence source | Note | Evidence |
|---|---|---|---|---|---|---|
| `ANC-001` | ANC → Off | User (App) | B | 🟢 | | `PROTOCOL.md` §4.1 |
| `ANC-002` | ANC → Noise Cancellation | User (App) | B | 🟢 | Sends a configuration command to the buds. | `PROTOCOL.md` §4.1 |
| `ANC-003` | ANC → Adaptive | User (App) | B | 🟢🔵 | Pro 2-specific; added in firmware 4.467 (Sept. 2025). Automatically adjusts volume to the environment. | `PROTOCOL.md` §4.1 |
| `ANC-004` | ANC → Transparency | User (App) | B | 🟢 | | `PROTOCOL.md` §4.1 |
| `CONV-001` | Toggle 'Conversation Detection' on/off | User (App) | C | 🟢 | Switches to Transparency and pauses media when you speak. | `PROTOCOL.md` §4.5.1 |
| `MULTI-001` | Toggle 'Multipoint' on/off | User (App) | C | 🟢 | Connects to 2 Bluetooth devices simultaneously; may trigger an SDP/connection update. | `PROTOCOL.md` §4.5.2 |
| `EQP-001` | EQ preset: Standard | User (App) | D | 🟢 | Full, fixed preset list from screenshots — each preset is a separate value to capture. On-screen preset-dropdown label is actually **"Default"**, not "Standard" (screenshot-confirmed 2026-08-23, `images/pixel_buds_app_equalizer_2.png`); not itself exercised in `CAP-015`'s 5-preset run, so still unconfirmed on the wire. | — |
| `EQP-002` | EQ preset: Bass Boost | User (App) | D, T | 🟢 | **T (added 2026-08-14):** top-priority isolated capture target now that EQ is known not to share ANC's channel — see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group T. **Captured 2026-08-15 (`CAP-005`, on-screen preset label "Heavy bass").** | `PROTOCOL.md` §4.2 |
| `EQP-003` | EQ preset: Bass Reduction | User (App) | D | 🟢 | On-screen preset label "Light bass" (screenshot-confirmed 2026-08-23). **Captured 2026-08-18 (`CAP-015`).** | `PROTOCOL.md` §4.2 |
| `EQP-004` | EQ preset: Balanced | User (App) | D | 🟢 | **Captured 2026-08-18 (`CAP-015`).** | `PROTOCOL.md` §4.2 |
| `EQP-005` | EQ preset: Vocal Boost | User (App) | D | 🟢 | On-screen preset label "Vocal boost". **Captured 2026-08-18 (`CAP-015`).** | `PROTOCOL.md` §4.2 |
| `EQP-006` | EQ preset: Clarity | User (App) | D | 🟢 | **Captured 2026-08-18 (`CAP-015`).** | `PROTOCOL.md` §4.2 |
| `EQP-007` | EQ preset: Last saved | User (App) | D | 🟢 | Restores a previously saved custom profile. **Captured 2026-08-18 (`CAP-015`) as that session's baseline quintet.** | `PROTOCOL.md` §4.2 |
| `EQP-008` | Save current EQ as a new preset ('Save') | User (App) | D | 🟢 | A distinct write action compared to preset selection — possibly a different protocol command (write vs. select). Preview-vs-save wire semantics still unresolved, see `PROTOCOL.md` §4.2/§6. | — |
| `EQS-001` | EQ slider: Upper treble | User (App) | E | 🟢 | 5-band EQ; each band is a separate, potentially distinct protocol field. On-screen label is "Upper treble" (screenshot-confirmed 2026-08-23, `images/pixel_buds_app_equalizer_1.png` — corrects this row's earlier "High treble" label to match `PROTOCOL.md` §4.2's usage). **Captured 2026-08-18 (`CAP-015`).** | `PROTOCOL.md` §4.2 |
| `EQS-002` | EQ slider: Treble | User (App) | E | 🟢 | **Captured 2026-08-18 (`CAP-015`).** | `PROTOCOL.md` §4.2 |
| `EQS-003` | EQ slider: Mid | User (App) | E | 🟢 | **Captured 2026-08-18 (`CAP-015`).** | `PROTOCOL.md` §4.2 |
| `EQS-004` | EQ slider: Bass | User (App) | E, T | 🟢 | **T (added 2026-08-14):** second, structurally different isolated action for Group T's cross-command check — see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group T. **Captured 2026-08-15 (`CAP-005`) — also revealed an additional, distinct "Save"-tap wire burst not anticipated by the original test design.** | `PROTOCOL.md` §4.2 |
| `EQS-005` | EQ slider: Low bass | User (App) | E | 🟢 | **Captured 2026-08-18 (`CAP-015`).** | `PROTOCOL.md` §4.2 |
| `TOUCH-001` | Toggle 'Touch controls' fully on/off | User (App) | F | 🟢🔵 | Enables/disables the touch sensors on the buds. | `PROTOCOL.md` §4.5.3 |
| `HEAD-001` | Toggle 'Head gestures' fully on/off | User (App) | F | 🟢🔵 | Pixel Buds Pro 2-exclusive. | `PROTOCOL.md` §4.5.4 |
| `HOLD-001` | Press-and-hold Left → Toggle ANC | User (App) | G | 🟢 | Binary choice per earbud. | `PROTOCOL.md` §4.5.3 |
| `HOLD-002` | Press-and-hold Left → Digital assistant | User (App) | G | 🟢🔵 | Per screenshot: "Works on Android only." | `PROTOCOL.md` §4.5.3 |
| `HOLD-003` | Press-and-hold Right → Toggle ANC | User (App) | G | 🟢 | | `PROTOCOL.md` §4.5.3 |
| `HOLD-004` | Press-and-hold Right → Digital assistant | User (App) | G | 🟢🔵 | | `PROTOCOL.md` §4.5.3 |
| `HOLD-005` | Check/uncheck a specific ANC mode in the press-and-hold rotation | User (App) | G | 🟢 | Checkbox list: Noise Cancellation, Off, Adaptive, Transparency — determines which modes the buds locally remember for the touch cycle. | `PROTOCOL.md` §4.5.3 |
| `AUDIO-001` | Toggle 'Mono audio' on/off | User (App) | H | 🟢 | | `PROTOCOL.md` §4.5.6 |
| `AUDIO-002` | Toggle 'Volume EQ' on/off | User (App) | H | 🟢 | Boosts bass/treble at lower volume. | `PROTOCOL.md` §4.5.6 |
| `AUDIO-003` | Shift the 'Volume balance' (Left/Right) | User (App) | H | 🟢 | **Note:** stored locally on the earbuds themselves (persistent write, works across devices) — explicitly stated in the app's own screenshot. | `PROTOCOL.md` §4.5.7 |
| `FW-001` | Tap the 'Firmware up to date' check | User (App) | I | 🟢 | Forces a manual update check. | `CAP-023-FINDINGS.md` §4 (cached, not live-queried) |
| `FW-002` | View firmware version per component (L/R/Case) | User (App, view) | I | 🟢 | Possibly triggers a status query when 'More settings' is opened. | `PROTOCOL.md` §0.1, `CAP-023-FINDINGS.md` §3 |
| `FW-003` | View serial numbers per component | User (App, view) | I | 🟢 | Same as above. | — (not exercised, `CAP-023-EVENT-NOTES.md` §6 gap) |
| `FW-004` | View connection status ("Earbud status: Connected") | User (App, view) | I | 🟢 | | — (not exercised, `CAP-023-EVENT-NOTES.md` §6 gap) |
| `INEAR-001` | Toggle 'In-ear detection' on/off | User (App) | J | 🟢 | Automatically plays/pauses audio when worn/not worn. | `PROTOCOL.md` §4.5.5 |
| `CASE-001` | Toggle case sound 'Earbuds replaced' on/off | User (App) | J | 🟢 | Setting stored on the chip inside the case. | `PROTOCOL.md` §4.5.8 |
| `CASE-002` | Toggle case sound 'Other notifications' on/off | User (App) | J | 🟢 | Covers: charging started, low battery, pairing successful, errors. | `PROTOCOL.md` §4.5.8 |
| `FIND-001` | Play sound on Left earbud ('Find My Buds') | User (App) | K | 🔵🟡 | Via Find Device/Find Hub integration in the app; individually addressable per component. | `PROTOCOL.md` §4.4 |
| `FIND-002` | Play sound on Right earbud | User (App) | K | 🔵🟡 | | `PROTOCOL.md` §4.4 |
| `FIND-003` | Play sound on Case | User (App) | K | 🔵🟡 | | `PROTOCOL.md` §4.4 (found to require Find Hub network path, not the local Ring command) |
| `FIND-004` | Play sound on both earbuds simultaneously | User (App) | K | 🟡 | | `PROTOCOL.md` §4.4 (same as `FIND-003`) |

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
| `CASE-008` | Press the case button briefly/differently to force pairing mode | User (Hardware) | P | 🔴 | **Still to verify** — no officially confirmed press duration found for a shorter pairing trigger, separate from the 30s reset. Open question, see §9 below and `PROTOCOL.md` §6. | — |

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
| `BATT-002` | Case broadcasts battery status via BLE advertisement | Buds/Case (Auto) | Q | 🔵 | Official Fast Pair Battery Notification extension: 3 bytes (L/R/Case), advertised when the case opens and/or on value change; visible ≥8s, hidden after 20s or explicitly. Optional when a single bud is inserted/removed. | `PROTOCOL.md` §4.3 Option A (`CAP-011`: inconclusive — Fast Pair Service traffic present, but not structurally matching this layout; procedure deviation, see `CAP-011-FINDINGS.md`) |
| `BATT-003` | Buds update battery status while worn | Buds/Case (Auto) | Q | 🔵 | Trigger = "when RFCOMM connects, or when the value changes" — no fixed step size (e.g. "every 1%") is officially specified. | `PROTOCOL.md` §4.3 Option A (`CAP-011`: inconclusive, same as `BATT-002`); also §4.3 Option E (`CAP-011`, added 2026-08-23: a DLCI 0x08 message tracking a live 1% Left/Right drop, 🟡 HYPOTHESIS pending sign-off) |
| `BATT-004` | Battery data via RFCOMM after connecting (instead of BLE advertisement) | Buds/Case (Auto) | A | 🔵 | Officially specified alternative channel — Fast Pair "Message Stream: Device Information". Presumed to be the same channel as the `HardwareStatus` hypothesis in `PROTOCOL.md` §3 — likely not a Buds-specific protobuf schema at all. See `PROTOCOL.md` §4.3 Option B. Piggybacks on the Group A connect capture rather than needing its own scenario (not Group Z — that scenario is a throwaway pipeline check, not a genuine evidence-gathering opportunity). | — |
| `INEAR-004` | In-ear sensor reports 'removed' (bud taken out of ear, not placed back in case) | Buds (Auto) | U | 🟢 | Confirmed via screenshot (In-ear detection: "pauses audio when not worn"). **Gap closed 2026-08-14:** `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group U now brackets this specific transition, added to test whether DLCI 0x08 Group `0x04` Code `0x12`'s alternating value is event-driven (`CAP-004-FINDINGS.md` §5a Task 5). | — |
| `BATT-005` | 'Low battery' notification (case) | Buds/Case (Auto) | — | 🔵 | Confirmed specifically for Pro 2/2a: notification for both low case battery and fully charged case. Opportunistic only — no dedicated scenario, since it requires genuinely low battery; see §9. | — |
| `BATT-006` | Battery-level change bracketing: `AT+CIND`/`battchg` vs. `AT+BIEV` cross-check over a natural battery decline | Buds/Case (Auto) | X | 🔴 | Added 2026-08-14. `CAP-001-FINDINGS.md` §3 found `battchg=3` (≈60%) and `AT+BIEV=2,100` (100%) disagreeing at the same moment — unresolved whether either indicator tracks a real level change. See `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X. **Resolved 2026-08-23 by `CAP-009`, maintainer sign-off obtained (`ADR-015`)**: `battchg` is 🟢 FACT a single non-repeating snapshot at SLC setup, never a live reading; `AT+BIEV=2` is 🟢 FACT per-earbud (Right, this session, not a fixed aggregate) and pushes at an irregular (not fixed ~6–7s) cadence once idle. | `CAP-009-FINDINGS.md` §1–§5, `PROTOCOL.md` §4.3 Option C, `ADR-015` (also a bonus 4th-session, 75-occurrence confirmation of §4.3 Option E in §6, plus two further HYPOTHESES on DLCI 0x04/Option B and a BLE-scan/Option A candidate in §4/§7) |
| `LOUD-001` | Loud Noise Protection: automatic volume reduction on a sudden loud sound | Buds (Auto) | Q | 🔵 | Added in firmware 4.467. Presumably a purely local DSP action (no phone-side command needed) — worth checking whether a notify frame is also sent to the phone. Does not cover impulse sounds (gunshots, fireworks) per Google. | — |
| `ADAPT-002` | Adaptive Audio: dynamic adjustment of the ANC/Transparency balance based on environment | Buds (Auto) | Q | 🔵🟡 | Works "on the fly" using the Tensor A1 chip in the buds. Unclear whether this generates BT traffic toward the phone (e.g. status sync for the UI) or stays entirely on-device. | — |
| `FWUPD-002` | Firmware installation when placed back in the case (with sufficient charge) | Buds/Case (Auto) | — | 🔵 | Installation timing is hardware-/case-bound (see `FWUPD-001` for download timing, which is app-/OS-bound); no dedicated scenario yet — long-duration; see §9. | — |
| `OBS-003` | Passive observation window covering two distinct sub-brackets used together in Group U: (1) closing the case lid while the connection stays active and both buds are elsewhere (not the normal `CASE-006` buds-back-in-case sequence), and (2) a deliberate ≥3-minute idle wait with nothing touched | User (Hardware) / passive | U | 🟢 | **Added retroactively 2026-08-20** — this Test-ID has been in active use since 2026-08-14 (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group U steps 2–3) and is already referenced in `CAP-007-FINDINGS.md`/`CAP-007-EVENT-NOTES.md` and `CAP-016-EVENT-NOTES.md`, but never had its own catalog row — gap found by `scripts/lint_docs.py`. Sub-bracket (1) confirmed zero dedicated wire signal in 2 independent captures (`CAP-007-FINDINGS.md` §3.4, `CAP-016-EVENT-NOTES.md`); sub-bracket (2) confirmed DLCI 0x08 Code `0x12` continues firing autonomously through the idle window (`CAP-007-FINDINGS.md` §3.2). | `PROTOCOL.md` §7, §6 Resolved |

---

## 4a. Catalog — In-call audio behavior (added 2026-08-14)

_None of `CAP-001`–`CAP-004` ever contains an actual phone call — the one scenario that would
exercise HFP's Service Level Connection setup and channel 5/DLCI 0x0a's audio path._

| ID | Description | Initiator | Capture scenario(s) | Existence source | Note | Evidence |
|---|---|---|---|---|---|---|
| `CALL-001` | Phone call during an active Buds connection (HFP SLC setup, SCO/eSCO pairing) | User (Phone) | V | 🔵 | Standard HFP behavior per the Bluetooth spec; not yet observed in this project's own captures — `CAP-002-FINDINGS.md` §5 found zero `AT+` traffic anywhere outside `CAP-001`'s own pairing-time handshake across a full day, and `CAP-001-FINDINGS.md` §6 Task 6 ruled out any SCO/eSCO HCI event in all four captures to date. | — |

## 5. `PAIR` / secondary-transport area (cross-referenced from Group A / Pixel 9a §4.2, not duplicated)

| ID | Description | Initiator | Capture scenario(s) | Existence source | Note | Evidence |
|---|---|---|---|---|---|---|
| `PAIR-001` | Pairing / bonding handshake (forget-and-re-pair baseline) | User (Hardware) | A | 🔵 | Lightweight, safely repeatable — see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group A #1 for the exact procedure (do not confuse with `CASE-007`'s factory reset). Also exercised on the Pixel 9a session (§4.2 #1), and incidentally on Group R (forced bond removal there also clears the classic bond). | — |
| `PAIR-002` | Pairing / bonding handshake from a true factory-reset state | User (Hardware) | P (via `CASE-007`) | 🔵 | Optional, one-time, destructive comparison capture — see `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group P #16. | — |
| `PAIR-003` | Disconnect and reconnect to an already-bonded device | User (Hardware) | — | 🟡 | Distinct protocol state from `PAIR-001`/`PAIR-002` (no bonding handshake, per `PROTOCOL.md` §5) — captured on the Pixel 9a session (§4.2 #4); **first Pixel 7a occurrence added 2026-08-26 (`CAP-012`, incidental — a manual disconnect/reconnect via system Bluetooth settings during a Group S session, `CAP-012-FINDINGS.md` §6):** reuses the stored classic link key (no SSP), and — new data point — retriggers a full HFP AT-command SLC handshake, relevant to `PROTOCOL.md` §6's open question on when that handshake recurs. | `CAP-012-FINDINGS.md` §6 |
| `PAIR-004` | 'Forget' clears a pre-existing BLE association/link-key completely, even one not established via the Pixel Buds app | User (Hardware) | A (repeat) | 🔴 | Added 2026-08-14. `CAP-001-FINDINGS.md` §6 found a BLE link and a still-valid link key both existing *before* the on-screen "Forget" tap and before the case reopened — distinct claim from `PAIR-001`'s "fresh pairing procedure." See `CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s Group-A repeat note. | — |
| `APP-001` | Tap 'Set up' — hands off from the pairing UI (system settings or a generic BLE tool) to the official Pixel Buds app's first-run onboarding screen | User (App) | A | 🟢 | **Added retroactively 2026-08-20** — in active use since `CAP-002`/`CAP-003` (2026-08-09/10) but never had a catalog row — gap found by `scripts/lint_docs.py`. No RFCOMM traffic tied to this specific tap in either capture; it is a local UI-navigation event, not a protocol event. | — |
| `APP-002` | Tap 'Continue' on the app's "Allow a connection to your Pixel Buds" nearby-device permission screen | User (App) | A | 🟢 | **Added retroactively 2026-08-20** — in active use since `CAP-002`/`CAP-003` (2026-08-09/10) but never had a catalog row — gap found by `scripts/lint_docs.py`. Immediately precedes the system `CompanionDeviceManager` "Allow" dialog (`DECISIONS.md` ADR-005) in both captures. | — |
| `GATT-001` | Full GATT service/characteristic discovery, forced (bond removed first) | User (Hardware) | R, W | 🔵 | The secondary BLE/GATT transport (`ARCHITECTURE.md` §1), distinct from the RFCOMM `libmaestro` channel. Zero-discovery across `CAP-002`–`CAP-004` and the 11:42 `CAP-010` attempt (bond removal alone doesn't force it); achieved for the first time via a fresh third-party GATT client (`CAP-017`, 18:30 session), though a truncated wire-log snaplen leaves the handle↔UUID mapping still open — see `CAP-017-FINDINGS.md` and `PROTOCOL.md` §4.3 Option D. | `PROTOCOL.md` §4.3 |
| `GATT-002` | Added 2026-08-20. Whether the `0x0044` BLE `Handle Value Notification` burst (`CAP-016-FINDINGS.md` §11 — 73 frames, 23 with a recurring `0xfea9` marker, right after the BLE link forms) is triggered by the BLE connection forming alone, independent of any bud/case action | Buds/Case (Auto) | Y | 🔴 | Wire-discovered only (`CAP-016`, incidental to that session's own bud-removal procedure) — no official/secondary source confirms this as a documented feature; existence-source rating reflects that, not doubt about the frames themselves. Every capture showing this burst to date also has a bud action nearby, so the trigger is unisolated. See `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y for the isolation procedure. | — |
| `GFPS-001` | Fast Pair Message Stream traffic (the `[Group][Code][Length][Value]`-framed channel identified in `CAP-002` `CAP-002-FINDINGS.md` §3) present or absent with the Pixel Buds app uninstalled and Google Play Services disabled | User (Hardware) | S | 🟡 | Captured and analyzed in `CAP-004` (`CAP-004-FINDINGS.md` §4) — result is a **mixed outcome**, not a clean present/absent: `CAP-002` §3's specific channel-2/DLCI-0x04 TLV content (Model ID, `"Revision 6"`, etc.) is **absent** under the combined GMS-disabled-and-app-uninstalled condition (channel 2 never opens) — GMS-and/or-app-dependent, unresolved which (this session changed both variables together, a confound not yet isolated — see `CAP-004-FINDINGS.md` §4a). But a related, structurally distinct piece of content on channel 4/DLCI 0x08, already documented in `CAP-001` (`google-pixel-buds-pro-v1`, `Europe/Amsterdam`), reappears **unchanged** — not GMS-dependent (not confounded, since surviving both removed at once is a clean presence result). `CAP-002` had treated these as one finding; they are now known to be two separate mechanisms with different dependencies, which is why this is 🟡 rather than a clean 🟢/🔴 — see `CAP-004` `CAP-004-FINDINGS.md` §4/§9 for the full reasoning and remaining open sub-question (new, unidentified Message Stream Groups `0x04`/`0x05`/`0x09` also surfaced by this capture). **Note added 2026-08-14:** `CAP-004-FINDINGS.md` §8 item 4 flags that this session's §2 finding (Cross-Transport Key Derivation bonding, not classic SSP) might be an artifact of nRF Connect's early BLE connection rather than of GMS being disabled — `CAP-004` deviated from Group S's system-settings-only procedure. A clean repeat of Group S (system settings only, no BLE tool) would isolate this; the `GFPS-001` result itself is not expected to change. **Repeat captured 2026-08-26 (`CAP-012`), confirming the isolation:** no BLE tool used, no BLE connection to the Buds at any point (independently verified on the wire), and classic bonding used SSP, not CTKD (`CAP-012-FINDINGS.md` §2/§10) — this confirms `CAP-004`'s CTKD result was specifically an nRF-Connect artifact, not a GMS-disabled effect. The channel-topology half of this row's own result (DLCI 0x04 never opens) reproduces cleanly in `CAP-012` too, but `CAP-012`'s log turned out to be severely ACL-truncated, so the payload-content half (Model ID/`"Revision 6"`/`"in-use"` presence-or-absence) is **inconclusive** there, not a second confirming data point (`CAP-012-FINDINGS.md` §1/§4) — a further repeat with a working, untruncated snoop log is still needed to fully close that part. | `CAP-004`, `CAP-012` |
| `GFPS-002` | Tap 'Save' in the Fast Pair "Save device to \<account\>" dialog (account-linking confirmation) | User (App) | A (incidental) | 🔴 | **Added retroactively 2026-08-20** — in active use since `CAP-002` (2026-08-09) but never had a catalog row — gap found by `scripts/lint_docs.py`. **Out of scope to investigate further per `DECISIONS.md` ADR-008** (Fast Pair Account Linking is explicitly excluded) — recorded here only so the tag isn't a dangling reference, not as an invitation to pursue it. No local Bluetooth traffic was observed for this tap (`CAP-002-EVENT-NOTES.md`), consistent with it being a cloud/GMS-side action. | — |

---

## 6. Firmware/OS-level compatibility notes (not separate tests)

Carried over from the original research, relevant context rather than standalone catalog
entries:

- Pixel Buds Pro 2-specific features (Adaptive ANC, Head Gestures, Loud Noise Protection)
  were added in firmware 4.467 (Sept. 2025) — confirm the test device's firmware is at or
  above this before expecting `ANC-003`, `HEAD-*`, or `LOUD-001` to be present at all.
- `TOUCH-007`'s press-and-hold digital-assistant behavior requires Android 6.0+.

---

## 7. Relationship to `PROTOCOL.md`

Once a capture provides evidence for a Test-ID:

1. Record the finding in that capture's `CAP-NNN-FINDINGS.md` first, per
   `PROJECT_RULES.md` §2 — include the Test-ID and the `CAP-NNN` capture ID it came from.
2. Once promoted to `PROTOCOL.md`, update this row's **Evidence** column to point at the
   relevant section (e.g. `PROTOCOL.md §4.1`) — a pointer only, not a restated finding.
3. Never leave a Test-ID's Evidence column with anything more than a pointer. If you find
   yourself writing more than a section reference in this column, that content belongs in
   the capture's `CAP-NNN-FINDINGS.md` instead.

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
- [ ] `BATT-005` — low battery notification (opportunistic only, needs genuinely low
      battery)
- [ ] `PAIR-003` — disconnect/reconnect to an already-bonded device, on the Pixel 7a
      specifically (currently only exercised on the Pixel 9a session)

_(`GATT-002`, added 2026-08-20 for the `0x0044` BLE notification-burst isolation question, is_
_**not** listed here — it now has both a Test-ID (§5) and a capture scenario_
_(`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y), so it doesn't belong in this "no Group yet" list,_
_matching how `INEAR-004`/`GATT-001` came off this list once Groups U/R/W existed for them.)_

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/TESTPLAN_BLUETOOTH_HCI_SNOOP.md - https://tedsluis.github.io/opencontrolpixelbudspro2/TESTPLAN_BLUETOOTH_HCI_SNOOP
