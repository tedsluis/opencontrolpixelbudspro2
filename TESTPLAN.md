# Test Plan: Pixel Buds Pro 2 Bluetooth HCI Capture

**Status:** sections 1 and 2 checked and expanded based on screenshots of the
official web companion app (strongest source — straight from the app itself), and (b) the
official Google support pages (`support.google.com/googlepixelbuds`). Sections 3 and 4 have
now also been validated.

**Source legend:**
- 🟢 **Screenshot** — seen directly in your own app screenshots
- 🔵 **Official** — confirmed via support.google.com (or another official Google source)
- 🟡 **Secondary** — confirmed via a reliable secondary source (e.g. 9to5Google, Android
  Authority), not directly from Google itself
- 🔴 **Unconfirmed** — still needs empirical verification during your own capture


---

## 1. User Actions from the Pixel Buds App

_Make sure the buds are connected and active._

| **Timestamp** | **Action** | **Initiator** | **Source** | **Note** |
|---|---|---|---|---|
| | Change ANC mode to 'Noise Cancellation' | User (App) | 🟢 | Sends a configuration command to the buds. |
| | Change ANC mode to 'Off' | User (App) | 🟢 | |
| | Change ANC mode to 'Adaptive' | User (App) | 🟢🔵 | Pro 2-specific; added in firmware 4.467 (Sept. 2025). Automatically adjusts volume to the environment. |
| | Change ANC mode to 'Transparency' | User (App) | 🟢 | |
| | Toggle 'Conversation Detection' on / off | User (App) | 🟢 | Switches to Transparency and pauses media when you speak. |
| | Toggle 'Multipoint' on / off | User (App) | 🟢 | Connects to 2 Bluetooth devices simultaneously; may trigger an SDP/connection update. |
| | Select EQ preset: **Standard** | User (App) | 🟢 | Full, fixed preset list from your screenshot — each preset is a separate value to capture. |
| | Select EQ preset: **Bass Boost** | User (App) | 🟢 | |
| | Select EQ preset: **Bass Reduction** | User (App) | 🟢 | |
| | Select EQ preset: **Balanced** | User (App) | 🟢 | |
| | Select EQ preset: **Vocal Boost** | User (App) | 🟢 | |
| | Select EQ preset: **Clarity** | User (App) | 🟢 | |
| | Select EQ preset: **Last saved** | User (App) | 🟢 | Restores a previously saved custom profile. |
| | **Save current EQ as a new preset** ('Save') | User (App) | 🟢 | A distinct write action compared to preset selection — possibly a different protocol command (write vs. select). |
| | Adjust EQ slider **High treble** | User (App) | 🟢 | 5-band EQ; each band is a separate, potentially distinct protocol field. |
| | Adjust EQ slider **Treble** | User (App) | 🟢 | |
| | Adjust EQ slider **Mid** | User (App) | 🟢 | |
| | Adjust EQ slider **Bass** | User (App) | 🟢 | |
| | Adjust EQ slider **Low bass** | User (App) | 🟢 | |
| | Toggle 'Touch controls' fully on / off | User (App) | 🟢🔵 | Enables/disables the touch sensors on the buds. |
| | Toggle 'Head gestures' fully on / off | User (App) | 🟢🔵 | Pixel Buds Pro 2-exclusive. |
| | Change 'Press and hold' Left → **Toggle ANC** | User (App) | 🟢 | Binary choice per earbud. |
| | Change 'Press and hold' Left → **Digital assistant** | User (App) | 🟢🔵 | Per your own screenshot: "Works on Android only." |
| | Change 'Press and hold' Right → **Toggle ANC** | User (App) | 🟢 | |
| | Change 'Press and hold' Right → **Digital assistant** | User (App) | 🟢🔵 | |
| | Check/uncheck specific ANC modes for the press-and-hold rotation | User (App) | 🟢 | Checkbox list: Noise Cancellation, Off, Adaptive, Transparency — determines which modes the buds locally remember for the touch cycle. |
| | Toggle 'Mono audio' on / off | User (App) | 🟢 | |
| | Toggle 'Volume EQ' on / off | User (App) | 🟢 | Boosts bass/treble at lower volume. |
| | Shift the 'Volume balance' (Left/Right) | User (App) | 🟢 | **Note:** stored locally on the earbuds themselves (persistent write, works across devices) — explicitly stated in your screenshot. |
| | Tap the 'Firmware up to date' check | User (App) | 🟢 | Forces a manual update check. |
| | Toggle 'In-ear detection' on / off | User (App) | 🟢 | Automatically plays/pauses audio when worn/not worn. |
| | Toggle case sound 'Earbuds replaced' on / off | User (App) | 🟢 | Setting stored on the chip inside the case. |
| | Toggle case sound 'Other notifications' on / off | User (App) | 🟢 | Covers: charging started, low battery, pairing successful, errors. |
| | **Play sound on Left earbud** ('Find My Buds') | User (App) | 🔵🟡 | **Missing from the original.** Via Find Device/Find Hub integration in the app; individually addressable per component. |
| | **Play sound on Right earbud** | User (App) | 🔵🟡 | |
| | **Play sound on Case** | User (App) | 🔵🟡 | |
| | **Play sound on both earbuds simultaneously** | User (App) | 🟡 | |
| | View firmware version per component (L/R/Case) | User (App, view) | 🟢 | Possibly triggers a status query when 'More settings' is opened. |
| | View serial numbers per component | User (App, view) | 🟢 | Same as above. |
| | View connection status ("Earbud status: Connected") | User (App, view) | 🟢 | |

---

## 2. User Actions via the Case & Buds (Hardware)

_Physical interactions with the device._

| **Timestamp** | **Action** | **Initiator** | **Source** | **Note** |
|---|---|---|---|---|
| | Open the charging case lid | User (Hardware) | 🟢🔵 | Triggers a BLE advertisement / Fast Pair pop-up. |
| | Remove Left earbud from the case | User (Hardware) | 🔵 | Changes state to 'out of case'. |
| | Remove Right earbud from the case | User (Hardware) | 🔵 | |
| | Insert Left earbud into the ear | User (Hardware) | 🔵 | Triggers the in-ear sensor (audio starts, if In-ear detection is on). |
| | Insert Right earbud into the ear | User (Hardware) | 🔵 | |
| | Tap once on a bud | User (Hardware) | 🔵 | Play/pause **or** answer call **or** (Pixel Buds Pro only) leave Conversation Detection mode **or** confirm a choice with Gemini. |
| | Double-tap on a bud | User (Hardware) | 🔵 | Next track **or** end/reject call **or** stop Gemini — missing from the original. |
| | Triple-tap on a bud | User (Hardware) | 🔵 | Previous track. |
| | Swipe forward on a bud | User (Hardware) | 🔵 | Raise volume. |
| | Swipe backward on a bud | User (Hardware) | 🔵 | Lower volume. |
| | Press and hold on a bud | User (Hardware) | 🔵 | Cycles ANC mode (incl. Adaptive) **or** activates Gemini/digital assistant — depending on per-earbud configuration (see section 1). Requires Android 6.0+. |
| | Head gesture: Nod | User (Hardware) | 🔵 | Answers a call. **New:** can also reply to a text via dictation if 'Spoken notifications' is on — English only. |
| | Head gesture: Shake | User (Hardware) | 🔵 | Rejects a call. **New:** can also dismiss a text reply under the same condition. |
| | User starts speaking (voice) | User (Hardware) | 🟢 | Triggers Conversation Detection (if on) — pauses media, switches to Transparency. |
| | Place buds back in the case and close the lid | User (Hardware) | 🔵 | Terminates the active Bluetooth Classic connection. |
| | Hold the case button for **30 seconds** (case open, buds inside, plugged into power) | User (Hardware) | 🔵 | **Correction vs. the original:** this is a **full factory reset**, not just pairing mode. Also explicitly resets the Find My Device link on the Pro 2. |
| | Press the case button briefly/differently to force pairing mode | User (Hardware) | 🔴 | **Still to verify** — no officially confirmed press duration found for a shorter pairing trigger (separate from the 30s reset). Note as an open question in `protocol-notes.md` §7 and determine empirically during your capture. |

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
  changes"** — so this is **event-driven, not a fixed polling interval**. This corrects
  the assumption in the original that polling happens periodically (e.g. "every 1% or
  5%").
- The advertisement is shown for at least 8 seconds when displaying a notification, and is
  automatically hidden after 20 seconds (or sooner, via an explicit "hide" flag).
- **Alternative mechanism, also officially specified:** raw battery data can also be sent
  over RFCOMM once a connection exists, via the **Fast Pair "Message Stream: Device
  Information"** extension. This is presumably **the same mechanism** as the
  `HardwareStatus` hypothesis in `protocol-notes.md` §4.2, option 1 — it deserves a higher
  confidence rating than 🔴 now that we know an officially documented RFCOMM battery route
  exists, even though this is a generic Fast Pair mechanism rather than a Buds-specific
  `libmaestro` detail.

---

## 3. Automatic Actions Initiated by the App

_Background processes, without a direct tap from the user._

| **Timestamp** | **Action** | **Initiator** | **Source** | **Note** |
|---|---|---|---|---|
| | App launches (after force close) → requests status | App (Auto) | 🔵🟡 | Likely via an RFCOMM connect, which per the Fast Pair spec itself is already the trigger for a battery update from the buds (see the call-out above) — so this is largely a section-4 event triggered by an app action. |
| | **Notification with battery status on every reconnect** | App (Auto) | 🔵 | Confirmed: "Each time you connect... a notification will appear showing you where battery life stands." Missing from the original. |
| | App running in the background (battery polling) | App (Auto) | 🔴 **Correction** | The original assumed periodic polling. The official Fast Pair spec instead suggests **event-driven** updates (on value change), not necessarily active polling from the app. Still to confirm whether the Pixel Buds app itself also actively polls on top of this mechanism — a good candidate for an open question in `protocol-notes.md` §7. |
| | Background check/download for a firmware update | App/OS (Auto) | 🔵 | Confirmed: download happens automatically in the background once connected to Android 6.0+ (~10 min), installation happens when the buds are placed back in the case with sufficient charge (~10 min). |
| | Adaptive Audio processing — does it require the app to be active? | App (Auto?) | 🟡 **Unconfirmed** | One source states: "toggles in the Pixel Buds app, which is required for the feature to work" — unclear whether this is purely a UI setting (written to the buds once) or whether the app needs to stay continuously active for this feature. Relevant to your own `ForegroundService` design (`ARCHITECTURE.md` §2/§6) — worth testing yourself: does Adaptive Audio still work if you fully close the official app? |

---

## 4. Automatic Actions Initiated by the Hardware (Buds/Case)

_Sensors and firmware behavior, without a direct action from the app._

| **Timestamp** | **Action** | **Initiator** | **Source** | **Note** |
|---|---|---|---|---|
| | Case broadcasts battery status via BLE advertisement | Buds/Case (Auto) | 🔵 **Now precisely specified** | Official Fast Pair Battery Notification extension: 3 bytes (L/R/Case), advertised when the case opens and/or on value change; visible for at least 8s, hidden after 20s or explicitly. Optional when a single bud is inserted/removed. |
| | Buds update battery status while worn | Buds/Case (Auto) | 🔵 **Correction** | Spec says trigger = "when RFCOMM connects, or when the value changes" — **no fixed step size** (e.g. "every 1%") is officially specified; that detail from the original is therefore an assumption, not confirmed. |
| | Battery data via RFCOMM after connecting (instead of BLE advertisement) | Buds/Case (Auto) | 🔵 | **New, important:** officially specified alternative channel — Fast Pair "Message Stream: Device Information". Strong candidate to match with `protocol-notes.md` §4.2 option 1 (the `HardwareStatus` hypothesis). |
| | In-ear sensor reports 'removed' | Buds (Auto) | 🟢 | Confirmed via your own screenshot (In-ear detection: "pauses audio when not worn"). |
| | 'Low battery' notification (case) | Buds/Case (Auto) | 🔵 | Confirmed specifically for Pro 2/2a: notification for both low case battery and fully charged case. |
| | **Loud Noise Protection**: automatic volume reduction on a sudden loud sound | Buds (Auto) | 🔵 | **Completely missing from the original.** Added in firmware 4.467. Presumably a purely local DSP action on the buds themselves (no phone-side command needed) — interesting to check whether a notify frame is also sent to the phone when this triggers. Does not cover impulse sounds (gunshots, fireworks) per Google itself. |
| | **Adaptive Audio**: dynamic adjustment of the ANC/Transparency balance based on environment | Buds (Auto) | 🔵🟡 | Works "on the fly" using the Tensor A1 chip in the buds. Unclear whether this generates BT traffic toward the phone (e.g. status sync for the UI) or stays entirely on-device — open question for `protocol-notes.md` §7. |
| | Firmware installation when placed back in the case (with sufficient charge) | Buds/Case (Auto) | 🔵 | See section 3 — installation timing is hardware-/case-bound, download timing is app-/OS-bound. |
