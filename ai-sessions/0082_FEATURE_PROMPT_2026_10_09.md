# 0082_FEATURE_PROMPT_2026_10_09.md — Build 1.2.0: "Changed by the Buds" in the ANC details, the component serial numbers, a "probably worn" indicator (ADR-049), two tab moves, and a design study for volume-level notifications (Dosimeter) — with real capture bytes as fixtures, a green gate, the hardware-run skeleton and the release preparation

**Number:** 0082
**Category:** FEATURE
**Date:** 2026-10-09
**Title:** Implement in the OpenControl app, as release 1.2.0: (1) the (i)-details line "Changed by the Buds" when a `Notify` changes the ANC mode
without a tap in the app; (2) the serial numbers per component (Case / Left / Right) from `GetHardwareInfo` field 7 on the Info tab; (3) a worn
indicator built on ADR-049's Settable byte and the runtime-info in-case flags, shown as "probably worn" (the maintainer's decision); (5) the two Case
sounds switches moved from the Controls tab to gear → Settings; (6) the Conversation detection switch moved from Sound to Controls — plus (4) a design
study, no code, of how volume-level ("recommended exposure limit exceeded") notifications could be built from `maestro_pw.Dosimeter`, including the
APK reverse engineering it needs and the ADR it would require. Real capture bytes as fixtures, a green test/lint gate with mutation checks, the
documents that follow the code, the skeleton of the hardware run (with the open film items of `TODO.md` §2), and the release preparation for 1.2.0
(no tag, no publication)

---

## 0. How to use this prompt

You are an expert software architect, reverse engineer, Android/Kotlin engineer and technical auditor. This prompt is for a **fresh** Claude Code
session in this repository. Run the phases in §4 **strictly in order**; do not start a phase before the previous one is done and recorded.

**Reading first (mandatory, `AGENTS.md` §0.1, `AI_SESSION_LOG_PROCEDURE.md` §8).** Before any other action, read **in full**, in this order:
`AGENTS.md`, `PROJECT_RULES.md`, `PROJECT.md`, `ARCHITECTURE.md`, `PROTOCOL.md` (in particular §2.2a with **all** its dated Updates — the
`GetHardwareInfo`/`GetSoftwareInfo` paragraphs and the Dosimeter paragraph of 2026-10-06 —, §4.1 "Notify ANC state" with its Settable-toggles bullet,
§4.3 Option F, §4.5 preamble, §4.5.8 and **§4.5.8a Volume level notifications**, and §6), `DECISIONS.md` (**every** ADR, ADR-001 … ADR-057, with every
dated Update — in particular ADR-009, ADR-021/022, ADR-032, ADR-034, ADR-042, ADR-043 with its Update, ADR-044, ADR-045, ADR-047, ADR-048,
**ADR-049 with its two Updates**, ADR-053, ADR-054, ADR-056, ADR-057), `TODO.md`. Then read `AI_SESSION_LOG_PROCEDURE.md` (in particular §4b and §9),
`ai-sessions/INDEX.md`, `RELEASING.md`, `APP_TESTPLAN.md`, `CHANGELOG.md`, `id_registry.csv`, `REVERSE_ENGINEERING.md`'s `qhr` register (field 21 and
its 2026-10-06 Update), its `maestro_pw.*` catalog and the "decoded shapes register" (`qhz`, `qia`, `qir`), and:

- `ai-sessions/0074_FEATURE_PROMPT_2026_10_06.md` and `ai-sessions/0074_FEATURE_RESULT_2026_10_06.md` — the pattern of the last feature build
  (reads, writes, fixtures, gate, mutations, run skeleton, release preparation); this prompt follows it;
- `ai-sessions/0076_FEATURE_RESULT_2026_10_07.md` — how fixtures were replaced by real `CAP-070` frames;
- `ai-sessions/0081_REVIEW_RESULT_2026_10_08.md` §C.5 — "What 1.2.0 should know first";
- `ai-sessions/0073_CROSSCHECK_RESULT_2026_10_06.md` §4 — the code-side answers: `GetHardwareInfo` (`qiv`, `gck.java:288` "serial number %s, sku %s"),
  the device-type key, the runtime-info charger-type enum, the `Dosimeter` shapes;
- `CAP-036-FINDINGS.md` (frame 1423, `GetHardwareInfo` RESPONSE with field 7), `CAP-058-FINDINGS.md` §6 and its EVENT-NOTES rows X11/X12 (the Hearing
  wellness page and the field-21 switch), `CAP-064-FINDINGS.md` §3 and `CAP-065-FINDINGS.md` §3 and `CAP-066-FINDINGS.md` (the Settable-byte samples
  with the ears on film), `CAP-069-FINDINGS.md` §12 item 8 and its §2 (taps on the current mode, the Buds' own `Notify`), `CAP-067-FINDINGS.md` §2
  (the Transparent → Active change without an app command), `CAP-045-FINDINGS.md` and `CAP-021-FINDINGS.md` §4 (press-and-hold ANC cycling on the
  wire — the `Notify` frames a physical change produces);
- `CAP-071-EVENT-NOTES.md` and `CAP-071-FINDINGS.md` — the layout of the last release-build run and what it left open (film 2: L-1, C12, S12, T11).

Read every Kotlin file you change **in full** before changing it — at least `DeviceInfo.kt` (`AncAvailability`, `DeviceInfo`, `FirmwareEntry`),
`BatteryStatus.kt` (`ChargingReading`, the runtime-info fields), `BudsRepository.kt`, `BudsRepositoryImpl.kt` (the ANC `Notify` path, `ancMode`,
`ancModeUpdatedAt`, `ancModeUnconfirmedAt`, the ADR-044 re-open, the runtime-info subscription, the Connect read sequence), `AncFrame.kt`,
`Maestro.kt` (`READABLE_FIELDS`, the request builders, whether `GetHardwareInfo` is **sent**), `PwRpc.kt`, `SoftwareInfo.kt`, `SettingFrame.kt`,
`BudsSettings.kt`, `AncScreen.kt` (`ancDetailLines`, `ancNotAllowedLine`), `ConnectionScreen.kt`, `ControlsScreen.kt`, `EqScreen.kt`,
`SettingsMenu.kt` (`SettingsTab`, `DarkModeSettings`, `InfoTab`), `SettingsUi.kt`, `Details.kt`, `OpenControlNavHost.kt`, `MainActivity.kt` (the state
holder, ADR-048), `BudsForegroundService.kt`, and their tests (`BudsRepositoryImplTest.kt`, `SettingsFixtures.kt`, `FakeBudsTransport.kt`, the `:ui`
Robolectric tests). Say in the RESULT which files you read in full and which only partly.

**Resuming after a rate or token limit (`AI_SESSION_LOG_PROCEDURE.md` §5) — you must be able to continue automatically.** Create
**ai-sessions/0082_FEATURE_RESULT_2026_10_09.md** at the very start, with `**Status:** partial — resumed` and a **Progress** block, and update that
block at the end of every phase **and** after every item built (which items are done and tested, which files are touched but not yet tested, the
last gate result, where intermediate results live — the scratchpad directory: decoded frames, the mutation script; re-create them if the scratchpad
is gone). A resumed session reads this prompt, then the RESULT's Progress block, re-reads the files the unfinished step touched, and continues from
there. It never redoes a finished, recorded step, and never assumes an unrecorded step was done.

**Nothing is taken on trust, including this prompt, the ADRs' frame numbers and the earlier RESULTs.** Re-derive every frame and byte you use from
its capture log (`PROJECT_RULES.md` rule 4a) before it becomes a fixture; if anything here disagrees with a capture or with the code, the capture
wins — record the correction in the RESULT and, where it touches an ADR's or a 🟢 text, bring it to the checkpoint.

---

## 1. What the maintainer asked for (chat, 2026-10-09, translated)

1. **"Changed by the Buds"** in the (i) details of the noise-control card when a `Notify` changes the ANC mode without a tap in the app
   (`CAP-069-FINDINGS.md` §12 item 8). No protocol work — UI, the state behind it, and one test.
2. **Serial numbers per component** (the `[~]` line of `PROJECT.md`'s functional scope: "firmware shown; serial numbers not read"). The maintainer's
   understanding: `GetHardwareInfo` is already read at Connect, so only the fields need showing. **Check whether that answer is 🟢 and whether the app
   actually sends the request** (§2 item 2) before building.
3. **Worn indicator** — the only `[ ]` line of the functional scope ("whether a bud is worn is not shown"). ADR-049 item 3: Settable `00` ⇒ no bud
   worn (🟡 strong); `e8` ⇒ at least one bud worn, except with in-ear detection off (`e8` always) and `CAP-064`'s ≈ 28 s of `e8` with both buds on the
   table. **The maintainer's decision (M, chat 2026-10-09): show it as "probably worn"** — an honest, hedged indicator, not a certainty. The ADR that
   records this is drafted in Phase A and approved at the checkpoint (§3).
4. **Volume-level notifications:** how could the app notify the user when the recommended exposure limit is exceeded, the way the official app's
   "Volume level notifications" switch (Hearing wellness, `qhr` field 21, `PROTOCOL.md` §4.5.8a) promises? The maintainer expects this to need
   further reverse engineering of the official APK. **This item is a design study with its RE, not an implementation** — see §2 item 4 and §3.
5. **Move the two Case sounds switches** ("Earbuds replaced", "Other alerts", ADR-054) from the Controls tab to **gear → Settings** (the first tab of
   the settings menu, next to dark mode and "Use different Buds").
6. **Move the Conversation detection switch** (field 22, ADR-045) from the Sound tab ("Balance and audio" card) to the **Controls** tab.

Items 5 and 6 are the maintainer's new answer on tab placement (chat 2026-10-09); they replace the placement decided for `ai-sessions/0074` (Case
sounds on Controls) and `ai-sessions/0052` (Conversation detection on Sound). Do not ask again *which* tab; the order and grouping *within* the tab is
a checkpoint question.

---

## 2. The items (verify each against the captures, the ADRs and the code before building)

### Item 1 — "Changed by the Buds" (ANC details)

- **What the wire gives:** on the app's own DLCI 0x04 claim (held for the session, ADR-044), the Buds push `Notify ANC state` (`08 13 00 04 …`) when
  the mode changes by any cause — a press-and-hold on a bud (`CAP-021` §4, `CAP-045`), the Buds' own change (`CAP-067` §2: Transparent → Active with
  no app command), or the app's own `Set` (`08 12`, ACKed, then the `Notify`). The repository already turns every `Notify` into `ancMode` +
  `ancModeUpdatedAt` (`BudsRepository.kt`), without recording *why* the mode changed.
- **Build:** track the cause of the current `ancMode`: "set by this app" when the `Notify` follows the app's own ACKed `Set` for that mode within the
  existing write window (re-use the ADR-045/F-3 bookkeeping — do not add a timer constant without the checkpoint); "read" for the connect-time or
  refresh `Get`'s answer; "changed by the Buds" for any other `Notify` whose mode differs from the shown one. Show it only in the (i) details
  (`ancDetailLines`) as one line: *"Changed by the Buds at HH:MM:SS (a press-and-hold on a bud, or the Buds' own change)."* — wording to confirm at
  the checkpoint. Nothing on the main surface, no notification, no new request.
- **Fixture:** a real `Notify` sequence from a capture with a physical press-and-hold change while a client held the Message Stream — `CAP-045`
  (Group AJ) lists them: 612 connect-time `Notify` (`… 01 e8 e8 40`, Adaptive), then Notify-only 1583 (`… e8 e8 80`, Transparency), 1755 (`… e8 e8 08`,
  Noise cancellation), 1818 (`… e8 e8 40`, Adaptive) — `CAP-045-FINDINGS.md:55–58`; `CAP-021` §4 (`TOUCH-007`) is the second source. Re-derive the bytes
  (`tshark -r <log> -Y 'frame.number==1583' -T fields -e data.data`). One
  repository test: `Set` → ACK → `Notify` (same mode) does **not** produce the line; an unsolicited `Notify` with another mode does; the connect `Get`'s
  `Notify` does not.
- **Test-ID:** `ANC-006` and `TOUCH-007` rows of `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` for the run skeleton (a press-and-hold on a bud while OpenControl is
  open → the line appears).

### Item 2 — Serial numbers per component

- **What is known (re-derive):** `GetHardwareInfo` (`maestro_pw.Maestro`, method id `0x28eca5e3` = `h65599("GetHardwareInfo")`, `fux.java`) answers
  with field 7 holding three strings — `CAP-036` frame 1415 (request) → 1423 (RESPONSE): `57071WRBEC0251`, `57081WRBDR3209` (corrected 2026-10-03),
  `57071WRBDL3147`; the method identity and the field are 🟢 FACT (`PROTOCOL.md` §6, resolved 2026-09-24; §2.2a Update of 2026-10-06 names field 7 "the
  component serials"). Which string is the Case, the Left or the Right bud is **not** 🟢: the substring reading "EC" = case, "DL"/"DR" = left/right is a
  🟡 from `ai-sessions/0017` (Group AS was withdrawn, `CAPTURE_BLUETOOTH_HCI_SNOOP.md`); `gck.java:288` logs "serial number %s, sku %s, hardware
  version" (`ai-sessions/0073` §4) — check in the JADX output whether the official app assigns the three strings to components by **position**
  (field 7's repeated order) or by **content**, and whether `GetHardwareInfo` is sent **per channel** (19 and 21) with the answer describing that
  bud; the per-channel address table of `fux.java:90–103` (channels 18–22 ↔ CASE, LEFT_BT_CORE, LEFT_SENSOR_HUB, RIGHT_BT_CORE, RIGHT_SENSOR_HUB) is
  the lead.
- **Does the app send it?** Phase A answers this from `Maestro.kt`/`BudsRepositoryImpl.kt`. A first grep while writing this prompt found the method id only
  in the name table (`Maestro.kt:37` `METHOD_GET_HARDWARE_INFO`, `PwRpc.kt:198` for decoding) and no call site — so expect that the app decodes but does
  **not** send it (verify). If `GetHardwareInfo` is **not** sent, showing serials
  adds **one unary request per Connect** on the announced channel (the official app sends it inside its connect burst, `CAP-036` 1415; every later
  capture has it, e.g. `CAP-058` §1's sweep) — a new wire request, which needs an **ADR** (draft **ADR-058** for the checkpoint: one request per
  Connect, byte-identical to the official app's, through the ADR-042 Safe-Mode gate for the send, no retry; the reply read for field 7 only). If the
  app already sends it, no ADR is needed and the item is UI only — say which in the RESULT with the `file:line`.
- **Build:** on the Info tab (`SettingsMenu.kt` `InfoTab`), under the firmware lines: "Serial numbers" with one line per string. **Honest state:**
  attribute a string to Case / Left / Right only if Phase A makes the mapping 🟢 (code path + a wire sample on each channel); otherwise show the three
  strings "as reported, in the Buds' order" with an (i) line saying the component is not confirmed — never a guess. "—" until the answer arrived.
  Serial numbers are device identifiers: **redact them in every fixture, log line and RESULT** (keep the first four and last two characters, `AGENTS.md`
  §9, §11) and never log them at `INFO` or above.
- **Fixture:** `CAP-036` 1423 (official app, channel 19) and one answer from an OpenControl capture if the app already sends the request (else from
  `CAP-070`/`CAP-071`'s official-app-free logs if present; if none exists, the run skeleton records the first one).

### Item 3 — Worn indicator ("probably worn")

- **What is known (ADR-049 and its two Updates, re-derive the frames):** (1) 🟢 Settable `00` ⇒ the Buds NAK a `Set` (reason `0x02`); `e8` ⇒ ACK.
  (2) 🟢 both buds in the case ⇒ `00`. (3) 🟡 strong: `00` ⇒ no bud worn — 28 + 28 samples with the ears on film (`CAP-065`, `CAP-066`), 0 counter-examples;
  the converse "no bud worn ⇒ `00`" is **refuted**: in-ear detection **off** ⇒ `e8` with no bud worn (`CAP-064` 10394, 10600), and in-ear detection on ⇒
  `e8` for ≈ 28 s with both buds on the table straight from the case (`CAP-064` 2299…2759; did not recur in `CAP-065`/`CAP-066`). (4) The byte is not
  per bud: `e8` means "at least one bud worn", never which one. (5) The runtime-info stream (`SubscribeRuntimeInfo`, ADR-043, `PROTOCOL.md` §4.3
  Option F) gives per bud whether it is **charging / in the case** (`7.2` Left, `7.1` Right; entry field 2 = the charger-type enum, `ai-sessions/0073`
  §4.6) — a bud in the case is not worn (🟢 correlation), a bud out of the case may or may not be. (6) GSND CONTROL Code `0x05` (3/4/6 = none/one/both
  worn, 🟡, `CAP-069` §8) is on DLCI 0x08, which the app does not open (ADR-043) — **not** a source for this item.
- **The maintainer's decision (M, chat 2026-10-09):** show the indicator as **"probably worn"** — hedged wording, with the time of the sample.
- **Build, without new wire traffic:** derive one of these from state the app already holds — "Probably worn (checked HH:MM:SS)" when the last
  `Notify`'s Settable byte is non-zero **and** in-ear detection reads on (field 2 = 1) **and** not both buds report in-case; "Not worn (checked …)" when
  the byte is `00`; "In the case" per bud from the runtime-info flags; "Unknown — in-ear detection is off" when field 2 = 0 (the byte then says nothing);
  "—" when no `Notify` arrived in this session. The sample is the last `Notify` the app received (connect `Get`, pull-to-refresh, a tap) — **not live**;
  always show its time, and let pull-to-refresh re-read it through the existing `Get`. Place it where the maintainer confirms at the checkpoint
  (proposal: a line on the Connection screen's battery card, with the (i) details explaining the sources and the two known limits: the byte says
  "at least one bud", and the ≈ 28 s case of `CAP-064`).
- **ADR:** this is a user-visible interpretation of ADR-049's 🟡 item 3 (`AGENTS.md` §13 "Implementing": a HYPOTHESIS with a verification plan may be
  implemented when labelled as such). Draft **ADR-059** for the checkpoint: what is shown, from which bytes, the wording, the limits, the verification
  the run skeleton adds (`INEAR-005`'s head-in-view steps: both out → "Not worn", one in → "Probably worn", both in the case → "In the case"). No
  PROTOCOL.md status change.
- **Fixtures:** `CAP-065` BA-5/BA-6/BA-7/BA-8 `Notify` frames (5465, 5707, 6019, 6334 — re-derive) and `CAP-064` 2299/10394 for the two limits; a
  runtime-info stream packet with `7:{1:0 2:0}` and one with a bud charging (`CAP-062` §4, re-derive); the field-2 read from `CAP-064`. One repository
  test per displayed state; one `:ui` test for the texts.

### Item 4 — Volume-level notifications (design study + reverse engineering; no code in 1.2.0 unless the maintainer says otherwise)

- **What is known:** the official app's switch "Volume level notifications" (Device details → Hearing wellness) writes `qhr` field 21 (🟢,
  `PROTOCOL.md` §4.5.8a, `CAP-058` X12 frames 5747/5753/5767/5790); opening the Hearing wellness page calls `maestro_pw.Dosimeter.FetchDailySummaries`
  (`CAP-058` 5736; response `qhz`: fields 1, 3, 4 `UINT32`, field 2 a repeated `qia` — plausibly one entry per day —, field 5 `FLOAT`); the connect
  burst subscribes `Dosimeter.SubscribeToLiveDb` on both channels (server stream, `qir` field 2 a `FLOAT`, "plausibly a live dB reading"; e.g. `CAP-027`
  416 packets, 208 per channel; values rise within a session — 🟡 a cumulative dose, unit unknown, `PROTOCOL.md` §2.2a 2026-10-06). The APK has a
  `HearingWellnessNotificationWorker` with a once-per-7-days throttle (`REVERSE_ENGINEERING.md`, the `0x240c8400` = 604 800 000 ms check) — a strong
  🟡 that the **phone**, not the Buds, decides when to notify, from Dosimeter data. A Dosimeter **display** was declared out of scope on 2026-09-30
  ("Not now — note only", `PROTOCOL.md` §2.2a, `TODO.md` §4); the maintainer's question of 2026-10-09 re-opens the topic for **notifications** — as a
  study first.
- **Reverse engineering (read-only, `AGENTS.md` §13 "Reverse engineering the APK", `REVERSE_ENGINEERING.md`'s workflow):** in
  `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/` (the version with JADX output; `v1.0.990706425-10260911` has only the APK files —
  decompile it only if the older code is inconclusive, with the tools under `reverse-engineering/tools`, and record the command), answer with
  `file:line` citations: (a) what `HearingWellnessNotificationWorker` computes, from which RPC (`FetchDailySummaries`, `SubscribeToLiveDb`, or a
  setting read), on which schedule (WorkManager periodic? only while connected?), and against which threshold (quote the constants — do **not** assert
  a WHO/EU figure the code does not contain); (b) what `qhz`/`qia`/`qir`'s fields mean in the code (names, units, the daily entry's shape); (c) whether
  field 21 changes anything **on the Buds** or only gates the phone-side worker (does any capture show a Buds-initiated message after the limit?
  `CAP-009`'s 101-minute and `CAP-042`'s 37-minute idle logs are the places to look; a negative needs its command, exit status and a positive control);
  (d) whether the Buds ever send a Dosimeter value unsolicited (outside the subscription); (e) the notification's text and channel in the official
  app (strings, for the design — not to copy).
- **Design (RESULT only):** the app-side mechanism this project could build, within `AGENTS.md` §1/§5/§9: the foreground service already holds the
  MAESTRO channel for the session — a `SubscribeToLiveDb` on the announced channel (one request per Connect, like `SubscribeRuntimeInfo`) would feed a
  dose computation on the phone; no polling timer, no background scanning, no network; a local notification channel; the field-21 switch as the
  user's on/off; what is stored locally (DataStore, per day) and what the (i) text can honestly claim; the battery and UX cost of holding the stream;
  what cannot be claimed until a capture correlates `qir` values with a sound-level meter on film (a verification plan with a Test-ID proposal). End
  with a **draft ADR-060** (one new server-stream request per Connect; the computation; the wording) and the list of 🟡 that must become 🟢 first.
  **Nothing of this is built in this session** unless the maintainer, at the checkpoint, chooses "build it in 1.2.0" — then it becomes its own phase
  after Phase F and gets the same fixture, gate and run-skeleton treatment as the other items.

### Item 5 — Case sounds switches → gear → Settings

- **Today:** `ControlsScreen.kt` shows the "Case sounds" card (fields 28 "Earbuds replaced", 27 "Other alerts", ADR-054, `ai-sessions/0074`) below
  Multipoint/Head gestures; `SettingsMenu.kt`'s `SETTINGS` tab shows `DarkModeSettings` (dark mode, "Use different Buds").
- **Build:** move the card, its labels, its (i) lines and its screen-reader text to the `SETTINGS` tab (pass the `BudsSettings` reading and the two
  callbacks through `SettingsMenu`; keep the write path unchanged); the Controls tab loses the card; tests follow (`ControlsScreen` tests, a
  `SettingsMenu` test that the two switches render with their values and times). Order and card title on the Settings tab: a checkpoint question with
  an ASCII mock-up. Note in the card's (i) that the setting lives on the case and is read at Connect (unchanged wording, ADR-054).

### Item 6 — Conversation detection → Controls

- **Today:** `EqScreen.kt` (Sound tab) shows "Conversation detection" (field 22, ADR-045, `ai-sessions/0052`) in the "Balance and audio" card with
  Balance and Mono audio.
- **Build:** move the switch row, its label, note and screen-reader text to the Controls tab (proposal: into the "In-ear detection" card or its own
  card — a checkpoint question with the mock-up of the whole Controls tab after items 5 and 6); the Sound tab keeps Balance, Mono audio and Volume EQ.
  Tests follow. `APP_TESTPLAN.md` sections M and N move their steps accordingly (M5 → N).

---

## 3. Decisions already taken, and what still needs the maintainer

- **Already decided (chat 2026-10-09):** the six items of §1 and their scope; **"probably worn"** as the indicator's reading (item 3); **Case sounds
  on gear → Settings** (item 5) and **Conversation detection on Controls** (item 6) — placements, not ADRs; item 4 is a **study** (RE + design + draft
  ADR), not an implementation, unless the maintainer changes that at the checkpoint. ADR-049, ADR-054, ADR-045, ADR-043 and ADR-044 cover the bytes
  the items read; building on them needs no new approval **except** where an item adds a request on the wire (item 2 if `GetHardwareInfo` is not yet
  sent) or shows a 🟡 to the user (item 3) — those get an ADR each.
- **To decide at the Phase B checkpoint (memory rule "Approvals: confirm in chat" — an approval quoted only in a file does not count), one
  `AskUserQuestion` per decision, each option with pros and cons and one "(Recommended)", the exact draft text in the preview:**
  1. item 1's (i) wording and the cause bookkeeping (any timing constant);
  2. item 2: the Phase A finding (sent or not), **ADR-058** if a request is added, and the component attribution (🟢 by position/channel, or "as
     reported");
  3. item 3: **ADR-059** (the drafted text), the placement (battery card vs noise-control card), the four texts, the (i) explanation with its two limits;
  4. item 4: accept the design as a 1.3.0 candidate with **ADR-060** drafted (Recommended), or build it in 1.2.0 (its own phase; the maintainer accepts
     the hardware-run dependency), or drop it; and the new Test-ID(s) it proposes;
  5. items 5 and 6: the layout of the Settings tab and of the Controls tab after the moves (ASCII mock-ups), and every changed user-visible text;
  6. the version (**1.2.0**, `versionCode` 10200 per `RELEASING.md` §4) and whether this session prepares the release commit (**REL**);
  7. any correction to an ADR's or a 🟢 text that Phase A finds (frame numbers, counts).
- **Not approved, not in scope:** anything else new on the wire (another field, `SubscribeToSettingsChanges`, a periodic or automatic request, a
  read of setting 13, the `GetHardwareInfo` versions beyond field 7, a Dosimeter request before ADR-060 is accepted); anything on DLCI 0x08 or
  0x0a; a per-bud worn claim (the byte cannot give one); a notification for item 1 or item 3; a dependency, Gradle-plugin, manifest or toolchain
  change (`targetSdk` stays 34 — its own session, `TODO.md` §5); the `TODO.md` §6 documentation sessions. If Phase A finds one of these worth doing, it
  is a RESULT note with the ADR it would need — not a checkpoint proposal to widen this session, and never a silent addition. **No** `PROTOCOL.md`
  status change and **no** ADR or ADR Update unless the maintainer approves it in this chat (`AGENTS.md` §6).

---

## 4. Tasks

### Phase 0 — set-up

1. Create the RESULT file (header per `AI_SESSION_LOG_PROCEDURE.md` §4, `**Status:** partial — resumed`, Progress block). Record `git log -1`, the
   branch (`git branch --show-current`; `main` takes no direct push — work on a new branch `feature/1.2.0` from an up-to-date `main`: `git fetch`,
   `git status`), the next free capture number and Group letter (`id_registry.csv`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 — `CAP-072`/Group BH if nothing
   was added) and the next free Test-ID numbers you may need.
2. Baseline gate, **before any change**: `./gradlew --offline --max-workers=2 clean`, then the project's test and lint tasks as `ai-sessions/0074`
   RESULT's gate table lists them (unit tests of `:data`, `:domain`, `:hardware`, `:ui`, `:app`; `lint`; the docs lint
   `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py`). Record test counts per module, warnings and lint results; the gate must be green before
   and after every phase. If the baseline is not green, stop and report — do not fix unrelated failures silently.

### Phase A — evidence and design check (no code change)

3. **Item 1:** locate the `Notify` path in `BudsRepositoryImpl.kt` and the ADR-045/F-3 bookkeeping; find and re-derive the press-and-hold `Notify`
   frames (`CAP-045`, `CAP-021` §4) and `CAP-067` §2's unexplained change; design the cause state (an enum next to `ancMode`, exposed as a `Flow`) and
   the one test. Write the (i) wording for the checkpoint.
4. **Item 2:** determine from the code whether `GetHardwareInfo` is sent (`grep -n 0x28eca5e3`/`GetHardwareInfo` in `android/`); re-derive `CAP-036`
   1415/1423 and the field-7 strings; read the JADX code for the component attribution (`gck.java:288`, the `qiv` decoder, `fux.java:90–103`); check
   the OpenControl captures (`CAP-067`…`CAP-071`) for a `GetHardwareInfo` request/answer (command + positive control). Draft ADR-058 if needed.
5. **Item 3:** re-derive the ADR-049 sample frames named in §2 and the runtime-info packets; read the field-2 handling (`ai-sessions/0056`); design the
   derived state (one `Flow` in the repository, pure function tested in `:domain`), the texts, the placement; draft ADR-059 with its verification steps.
6. **Item 4:** do the reverse engineering of §2 item 4 (a)–(e) — read-only, `file:line` for every claim, commands with exit status and a positive
   control for every negative; write the design and draft ADR-060; list the 🟡 that block a build. Record what you read in full and in part. If the
   JADX output of 1.0.955078536 cannot answer (a), say so and estimate the decompilation of 1.0.990706425 as a step, do not start it without asking.
7. **Items 5 and 6:** read the three screens and `SettingsMenu`, the navigation host and `MainActivity`'s wiring; draw the two mock-ups; list the
   `APP_TESTPLAN.md` steps and `ARCHITECTURE.md` §2.4 sentences that move.
8. **Run skeleton input:** list the `TODO.md` §2 items that fit this run (L-1 with the head in view / `INEAR-005`, C12, S12, T11, H5, the case-sound
   and Volume-EQ observations, B4, the CDM picker on film) with their expected bytes or screens.
9. Record everything in the RESULT (per item: evidence, frames, design, open questions) and update the Progress block.

### Phase B — checkpoint (stop and ask, in this chat, via `AskUserQuestion`)

10. Show a short summary (per item: what Phase A found, what would be built, what it needs), then ask the questions of §3 — one per decision, each
    option with pros and cons, one "(Recommended)", the exact draft text (ADR-058/059/060, the texts, the mock-ups) in the preview. Record every answer
    verbatim in the RESULT. Build only what was approved; an item not approved becomes a RESULT note with its TODO line.

### Phase C — Item 1: "Changed by the Buds"

11. Build the cause state and the (i) line; the repository test with the real `Notify` fixture (frame number, time and command in a comment); the
    `:ui` test for the line. Gate green. Progress block.

### Phase D — Item 2: serial numbers

12. If approved: the request (only if ADR-058 was approved — byte-identical to the official app's, through the gate, one per Connect) and/or the
    decoding of field 7 (`PwRpc.kt`/`SoftwareInfo.kt` pattern: never throws, malformed → `MalformedFrame`); the Info tab lines with the agreed
    attribution; redaction in logs; the codec test with `CAP-036` 1423 (redacted) and a fuzz case (`AGENTS.md` §11). Gate green. Progress block.

### Phase E — Item 3: worn indicator

13. Build the derived state as a pure function in `:domain` (inputs: last Settable byte + time, field-2 reading, the two in-case flags; output: one of
    the five readings), its tests (one per reading, with the real fixtures), the repository `Flow`, the UI line and (i) text in the approved place,
    the `:ui` test. No new request. Gate green. Progress block.

### Phase F — Items 5 and 6: the two tab moves

14. Move the Case sounds card to the Settings tab and the Conversation detection row to the Controls tab as approved; adapt the tests; verify with
    the Robolectric tests that every switch still writes the same bytes (the fixtures of `ai-sessions/0076` stay valid — the wire is unchanged).
    `APP_TESTPLAN.md` sections M and N (and T where the Case sounds steps live) follow. Gate green. Progress block.

### Phase G — Item 4 build (only if the maintainer chose "build it in 1.2.0"; otherwise skip)

15. As approved at the checkpoint, with ADR-060 accepted in chat: the request, the computation, the notification, DataStore, fixtures from a real
    `SubscribeToLiveDb` stream (`CAP-027`), the honest texts, the tests, the run-skeleton steps with the sound-level-meter correlation. Gate green.

### Phase H — gate, mutations, compliance

16. `./gradlew --offline --max-workers=2 clean`, then the full gate of task 2: all green, no new lint issue, no new suppression, no new compiler
    warning; record the new test counts per module.
17. Mutation checks — apply each with a script, run the relevant test task, restore, verify the file byte-identical with `sha256sum`. **Run them one
    at a time with `--max-workers=2`** (parallel runs have exhausted memory before) and, after any crash, check for a leftover mutation before
    continuing. At least: **M1** the cause set to "changed by the Buds" for the app's own ACKed `Set`; **M2** the cause not reset at Connect; **M3**
    field 7 read from the wrong field number; **M4** a serial logged unredacted at `INFO` (must fail a test or a lint rule — add one if none exists);
    **M5** "Probably worn" shown with Settable `00`; **M6** "Probably worn" shown with in-ear detection off; **M7** "In the case" shown for a bud whose
    runtime-info flag is 0; **M8** the Case sounds switch writing field 27 for the "Earbuds replaced" row after the move; **M9** Conversation detection
    writing field 19 (Mono audio) instead of 22 after the move; **M10** (if ADR-058) the `GetHardwareInfo` request sent past the Safe-Mode gate on an unverified firmware. Each
    must fail at least one test; record which.
18. Compliance: no `INTERNET`, no new permission, no manifest or version-catalog change, no new dependency
    (`git diff --name-only -- '*.toml' '*AndroidManifest.xml'` empty; the only Gradle change is the version of **REL**, if chosen); every
    `transport.send` call site in the diff listed (expected: none new, or exactly the ADR-058 request); nothing on DLCI 0x08/0x0a; no new codec field
    except field 7 of `GetHardwareInfo` (and nothing of item 4 unless Phase G ran); every write passes the ADR-042 gate; AGPL headers on any new Kotlin
    file; no MAC address or serial number at `INFO` or above and no raw payload outside debug mode (`AGENTS.md` §9).

### Phase I — the hardware-run skeleton (RUN) and the test plan

19. Create the capture folder and its `…-EVENT-NOTES.md` skeleton in the layout of `CAP-071-EVENT-NOTES.md` (build and metadata, preparation with the
    lessons of `CAP-067`…`CAP-071` — film with sound, screen recording on, the system log, `adb bugreportz`'s SIGQUIT note —, start, steps with
    expected screen and expected HCI bracket, don'ts, collection, analysis checklist with Test-IDs and "Refuted if" lines). Register the capture
    *planned* in `id_registry.csv`, add its Group section and Capture Index row (`CAPTURE_BLUETOOTH_HCI_SNOOP.md`), and new Test-IDs where a step has
    none (`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`, registered). Contents:
    - item 1: a press-and-hold ANC change on a bud with OpenControl open → the `Notify` → the (i) line; a tap in the app → no line;
    - item 2: the Info tab's serial lines on film; if ADR-058: the request/answer bytes on the announced channel;
    - item 3: `INEAR-005`'s head-in-view sequence read against the indicator (both out → "Not worn"; one in → "Probably worn"; both in the case →
      "In the case"; in-ear detection off → "Unknown"); the ≈ 28 s case: buds straight from the case to the table, the indicator watched for 60 s;
    - items 5 and 6: each moved switch once off/on with its expected `WriteSetting` bytes (unchanged from `CAP-070`/`CAP-071`) and the read-back at a
      reconnect;
    - every `TODO.md` §2 item that fits (task 8), each with its expected bytes or screen — this run is the third chance for film 2's items;
    - P1 with `dumpsys package … | grep -E "firstInstallTime|lastUpdateTime"`: an **update over 1.1.1**, record both times.
    Keep the run to one sitting; if it does not fit, propose a split at the end of the session (a question, not a silent cut).
20. `APP_TESTPLAN.md`: a new section for this build (after section U), one step per item; move M5 to N and the Case sounds steps to a gear → Settings
    section; correct any step the moves make stale.

### Phase J — documentation and release preparation

21. Documentation, only what changed: `ARCHITECTURE.md` (§5a rows: the ANC row's cause line, `GetHardwareInfo`/serials, the worn indicator with its
    ADR; §2.4 for the tabs; §3.1 only if a rule changed — dated current text, rule 9a), `PROJECT.md` (the functional scope: `[~]` serials → `[x]` or
    the honest state, `[ ]` worn → `[x]` with "probably"; "Status after 1.0.x"), `CHANGELOG.md` (`[Unreleased]`, or the 1.2.0 block if **REL** was
    chosen), `README.md` (status and feature list), `TODO.md` (finished items deleted — among them `TODO.md` §5's `CAP-069` §12 item 8 line —, open
    ones kept, new ones added: item 4's ADR-060 candidate and its verification capture), `REVERSE_ENGINEERING.md` (item 4's findings as a dated
    section with `file:line`), `ai-sessions/INDEX.md` (the 0082 row). `PROTOCOL.md` and `DECISIONS.md`: only what was approved in this chat (ADR-058/
    059/060 as accepted or as "proposed" drafts in the RESULT only — an unaccepted ADR is **not** written into `DECISIONS.md`).
22. **REL, only if chosen at the checkpoint:** `RELEASING.md` §4 steps 1–2 — `versionName` 1.2.0 and `versionCode` 10200 in
    `android/app/build.gradle.kts`, the `CHANGELOG.md` block marked "not yet released" with its Known issues (among them: the worn indicator's two
    limits; the serial attribution if not 🟢), `README.md`, `scripts/release_notes.template`. Do **not** run `scripts/release.sh`, create a tag, or
    publish; print the commands of `RELEASING.md` §5–§6 for the maintainer and run none.
23. Run `python3 scripts/ensure_footers.py` and `PYTHONDONTWRITEBYTECODE=1 python3 scripts/lint_docs.py` — it must exit 0
    (`scripts/__pycache__/lint_docs.cpython-314.pyc` is tracked; `git checkout` it if it changed).

### Phase K — finish

24. Finish the RESULT: a plain-language summary first (what the app user will see differently; what the maintainer has to do next, step by step),
    then per item what was built and where, the fixtures with their bytes and commands (serials redacted), the checkpoint answers verbatim, item 4's
    RE findings and design, the gate table, the mutation table, compliance, the run skeleton as a table (step · action · expected screen · expected
    HCI bracket · Test-ID), the `TODO.md` §2 items taken and not taken, external sources (URL + quoted sentence), what was read in full and in part,
    and the two closing sections **"Deferred documentation"** (each item also in `TODO.md`, in the same words) and **"Commits"**. Status per
    `AI_SESSION_LOG_PROCEDURE.md` §4 and §4b.
25. Show the maintainer a short summary and **ask whether to commit, push and open a pull request**. Only after a yes: Conventional Commits, one
    commit per concern, each building and passing on its own (`PROJECT_RULES.md` rule 16) — e.g. `feat(app)` per item with its tests, `refactor(ui)`
    for the two moves, `docs` for the documents, `docs(re)` for item 4's findings, `docs` for the run skeleton and the registry, `chore(release)` for
    **REL**, `docs` for the session files — each with a *why* and ending with the attribution line from the session's system reminder; `git fetch` and
    rebase before pushing, never force; nothing from a build directory, `android/.kotlin/`, `dist/`, `.vscode/` or `__pycache__` staged. The pull
    request is not merged by the agent unless the maintainer says so.

---

## 5. Guardrails (binding, in addition to `AGENTS.md`)

- **Wire discipline.** Items 1, 3, 5 and 6 add **nothing** on the wire. Item 2 adds at most one unary `GetHardwareInfo` request per Connect, and only
  with ADR-058 approved in this chat, byte-identical to the official app's, on the announced channel with its ADR-034 address, through the ADR-042
  gate — no retry, no queue. Item 4 adds nothing unless Phase G was chosen. Nothing on another DLCI, no subscription beyond those the app already
  sends, no timer.
- **Honest state (`AGENTS.md` §5, `ARCHITECTURE.md` §3.1).** Every shown value keeps its own receive time; "—" for a value that was not read; the worn
  indicator says "probably" and never names a bud as worn; a serial is attributed to a component only when the attribution is 🟢; a cause line says
  "changed by the Buds" only for a `Notify` the app did not provoke. No text claims an audible or behavioural effect that no capture supports.
- **Labels.** Everything written down carries 🟢/🟡/⚪/🔴 (`PROJECT_RULES.md` §1). ADR-049 item 3 stays 🟡; the indicator's (i) text says so in plain
  words.
- **Approvals only in this chat.** Never promote, demote or correct a 🟢 FACT, and never write or update an ADR, without the maintainer's approval
  given in this chat (`AGENTS.md` §6). ADR-058/059/060 are drafts until approved; an unapproved draft lives in the RESULT and `TODO.md`, not in
  `DECISIONS.md`.
- **Tests with real bytes (`AGENTS.md` §11).** Fixtures come from the logs with frame number, time and command in a comment; a hand-built frame only
  as a labelled supplementary structural test next to real fixtures. Redact device identifiers (MAC, serials).
- **Evidence.** Zero creativity with hex (`AGENTS.md` §13 step 6); every claim in the RESULT links to a frame, a log line or a `file:line`; a negative
  needs its command, exit status and a positive control (step 8); external claims cite the URL and the exact sentence; APK claims cite `file:line` in
  the JADX output and never copy code (`AGENTS.md` §12).
- **Privacy.** Serial numbers are device identifiers: redacted in fixtures, RESULT and logs; never at `INFO` or above (`AGENTS.md` §9).
- **Scope.** `PROJECT.md`, the ADRs named and §1. Everything else is a RESULT note, never a silent addition. No dependency, Gradle-plugin, manifest
  or toolchain change; `targetSdk` stays.
- **Subagents.** A subagent may only read and report; every write is done in the main session and verified; re-derive any number a subagent reports,
  and say in the RESULT what was taken over without re-derivation.
- **Files.** Before deleting, moving or renaming any file, look at the target and diff a fresh listing; never `rm -rf` or a wildcard delete from
  memory; no `git worktree add`.
- **Release.** No tag, no `scripts/release.sh`, no GitHub release, nothing public. The release verdict belongs to the session that analyses the
  hardware run.
- **Commits.** Only after the maintainer confirms the final summary (task 25).

---

## 6. Deliverables

- **ai-sessions/0082_FEATURE_RESULT_2026_10_09.md** — Progress block, plain-language summary, per-item record with fixtures, the item-4 RE findings
  and design with the draft ADR-060, checkpoint answers, gate/mutation/compliance tables, the run skeleton's table, "Deferred documentation",
  "Commits", footer.
- The app with the "Changed by the Buds" line, the serial numbers on the Info tab, the "probably worn" indicator, the Case sounds switches on gear →
  Settings and Conversation detection on Controls; their tests; a green gate.
- ADR-058 (if a request was added) and ADR-059 in `DECISIONS.md` **only if approved in chat**; otherwise as proposals in the RESULT and `TODO.md`.
- The hardware-run skeleton, registered *planned*, with the open `TODO.md` §2 items it takes and the verification steps of ADR-059.
- The documents that follow the code; `REVERSE_ENGINEERING.md`'s dated item-4 section; the release preparation for 1.2.0 if chosen.
- Nothing left silently open: every item built, moved to the run, registered in `TODO.md` for a named later session, or "awaiting maintainer" with
  the question asked.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0082_FEATURE_PROMPT_2026_10_09.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0082_FEATURE_PROMPT_2026_10_09
