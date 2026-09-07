# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AE, realistic physical reconnect trigger vs. a system-Bluetooth-toggle reconnect (`CAP-038`)

**Status:** ✅ **Captured and analyzed.** All blanks below are filled in from
`CAP-038-btsnoop_hci.log` and `CAP-038-recording.mp4` per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5
(analysis) and §8 (what to update), and `PROJECT_RULES.md` rule 11/14 (reproducibility metadata).
The capture folder has been renamed from the original placeholder
`CAP-038-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AE` to `CAP-038-2026-09-06_06-49-50_06-53-57-Group_AE`
(actual session start/end, per the video/log correction in the Event Timeline below). See
`CAP-038-FINDINGS.md` for the standardized write-up.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
(directly observed, evidence referenced) · 🟡 **HYPOTHESIS** (unverified, with a stated test) ·
⚪ **ASSUMPTION** (treated as true without verification, with a stated reason) · 🔴 **OPEN
QUESTION** (identified gap, no hypothesis yet). Never write a conclusion in this file without one
of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AE, `OBS-005`, new):** `CAP-036`'s reconnect was
triggered by toggling Bluetooth in system settings **while the Buds sat visibly in the open case
the entire session, never removed, never worn** (confirmed via that session's own video re-pass,
`CAP-036-FINDINGS.md` §2) — not the normal user flow of taking the Buds out of the case and putting
them in your ears. That normal flow could plausibly generate wire traffic a pure OS-level toggle
never exercises: an in-ear-detection event, A2DP audio-profile establishment, or a different
sequencing of the DLCI 0x02/0x04/0x08 connection burst than `CAP-036`/`CAP-037` show. This session
tests that directly, with two windows: a genuinely realistic reconnect (Buds removed from the case
and worn), and — time permitting — the case lid being opened **without** touching the Bluetooth
toggle at all, to see whether that alone behaves differently from an OS-level toggle.

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed and in normal use
(not necessarily pinned to one screen this time — let the app behave as it would for a real user),
Google Play Services **enabled** (the normal baseline). Buds already bonded — this is a reconnect,
not a fresh pair.

**⚠️ The rule this capture depends on: no *settings* are touched.** Wearing the Buds and letting
audio/A2DP/in-ear-detection do whatever they naturally do is the point of this session and is not
itself a contamination — but do not tap into EQ, touch-controls, ANC, or any other settings screen.
If a setting is touched by accident, say so explicitly and treat that window as contaminated.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-038`                      |
|      Group(s)    | AE (`OBS-005` — realistic reconnect trigger vs. OS-toggle reconnect; incidental `PAIR-003`, `INEAR`-family) |
|       Date       |                     2026-09-06                      |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android 14. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-038-recording.mp4` — originally documented as 06:17s (`06:49:50`–`06:56:07`); **corrected via `ffprobe`: actual duration 247.07s (~4m07s), `06:49:50`–`~06:53:57` local time** — see Capture-integrity pre-flight and Event Timeline correction below |
| Log file         | `CAP-038-btsnoop_hci.log` — 371.55s, 4,426 packets, 2026-09-06 06:49:58.224–06:56:09.778 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`/`CAP-037` (see those files):
`capinfos` snaplen check + `frame.cap_len==frame.len` mismatch count, raw extraction path
preferred. Result: no snaplen cap; `tshark ... | awk '$2!=$3{c++} END{print c+0}'` → `0`
mismatches. Untruncated. **Note: the video (ffprobe duration 247.07s) ends at ~06:53:57 — about
2m10s before the log's own end (06:56:09.778) and well before this file's originally-documented
"Window 2" (06:54:14 onward). See §2/Event Timeline correction below.**

Extraction path used: Raw step-3 path (`adb bugreport` -> `FS/data/misc/bluetooth/logs/btsnoop_hci.log`).

## Preparation checklist (before recording)

- [x] Buds are **already bonded** to this Pixel 7a. Do **not** "Forget" or re-pair.
- [x] Official Pixel Buds Companion App installed and working.
- [x] Google Play Services **enabled** — carried `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [x] No third-party BLE/GATT tool used at any point.
- [x] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [x] Video recording with a visible wall-clock overlay ready, framed so the case, the act of
      removing/inserting the Buds, and the phone screen are all visible.
- [x] Note whether Bluetooth is on or off before the session starts: Bluetooth is **ON** (06:49:51), but quickly toggled OFF and ON again by the user.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AE)

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active.
2. **Window 1 — realistic reconnect** [`OBS-005`, incidental `PAIR-003`, `INEAR`-family]. With
   Bluetooth already on and the Buds disconnected (either already out of range/off, or manually
   disconnected from system settings first — note which), **physically remove both earbuds from
   the case and insert them into your ears**, the way a normal user would. Note the exact moment
   each earbud is removed from the case, the exact moment each is inserted into an ear, and the
   exact moment the phone shows the connection as active. Then idle ~15–20s without navigating
   anywhere or touching any control on the buds (no taps, no press-and-hold).
3. Clean buffer (~5s).
4. **Window 2, optional/time-permitting — case-lid-only trigger** [`OBS-005`]. With the Buds now
   back in the case and Bluetooth still on, close the case lid fully (confirm on-screen
   disconnect), wait ~5s, then **open the case lid only** — do not touch the Bluetooth toggle or
   any app screen. Note whether/when a reconnect occurs on its own, and how (if at all) it differs
   from Window 1's or `CAP-036`'s toggle-triggered reconnect. Skip freely if the session is
   running long; note whether this window was run at all.
5. Stop video recording and HCI snoop logging.
6. Extract via the raw btsnoop path first, then run the capture-integrity pre-flight.

## Event Timeline

**🟢 CORRECTED via video re-pass (targeted `ffmpeg` frame extraction against the wall-clock overlay,
cross-checked against the wire log, per `CAP-036-FINDINGS.md` §2's precedent). Two significant
corrections: (1) most timestamps below the actual wire connect events run ~15–30s later than the
real wire timing (video frames `w36.png`@06:50:26 and `w176.png`@06:52:46 match the wire's own
Connection Complete events almost exactly); (2) far more importantly, the video itself ends at
~06:53:57 (ffprobe duration 247.07s from a 06:49:50 start) — **before "Window 1 end" is even
reached** in the original documentation, and Window 2 (06:54:14–06:55:53) has NO video coverage
at all. The wire log independently confirms Window 2 never happened on the wire either: the
Buds' last classic connection (chandle `0x0004`) disconnects at `06:53:46.090` and **no further
Connection Complete event for the Buds occurs anywhere in the rest of the log** (which itself runs
to `06:56:09.778`) — only unrelated BLE advertising-scan activity follows. Window 2 is therefore
recorded as NOT VERIFIABLY EXECUTED, not as a clean negative — there is no evidence either way
that the case lid was opened as described.**

| Time (local) | Action / Event | Initiator | Test-ID | Evidence in `CAP-038-btsnoop_hci.log` |
|---|---|---|---|---|
| `06:49:50` | Start video recording; Bluetooth quick-panel shown, Buds "Active" L:100%/C:85%/R:100% (video-confirmed, `t0.png`) | — | — | — |
| `06:50:09`–`11` | Bluetooth toggled OFF then ON (per procedure note) | User (OS) | — | — (predates most wire activity; log itself starts `06:49:58.224`) |
| `06:50:26.121` | **Connection Complete, chandle `0x0001`** — video-confirmed exactly (`w36.png`@06:50:26: Buds visibly out of case, quick-panel shown) — ~16s earlier than the originally-documented `06:50:42`/`06:50:27` estimates | App/OS (Auto) | `OBS-005`, `PAIR-003` | frame 841 |
| `06:50:26.792`/`.821` | 🟢 **CORRECTED** — DLCI `0x04` does NOT carry the Fast Pair Message Stream on this specific connection; it landed on **DLCI `0x05`** instead (RFCOMM channel numbers are session-local, not fixed — `CAP-001-FINDINGS.md` §2's established finding; `libmaestro`'s own channel is correspondingly `0x03`, not `0x02`, this session). Get (`08 11 00 00`)/Notify (`08 13 00 04 01 e8 00 20`) fire normally on DLCI `0x05`: **`Settable-toggles=0x00` (DOCKED)**, Current=`0x20` (Off) — at the very first reconnect immediately after the buds were reported removed from the case, the wire reads **docked**, not undocked. Genuinely tension-worthy for `OBS-005`'s "realistic worn reconnect" framing — see `CAP-038-FINDINGS.md` §3. | App/OS (Auto) | `OBS-005` | frames 1143/1154 |
| `~06:50:34` | User taps gear icon, enters Device details (**not itself contamination** — this is normal `OBS-005` procedure navigation) | User (App) | — | (UI-only, no distinct wire signature) |
| `~06:51:09`–`06:51:57` | User navigates Sound/EQ, In-Ear Detection, Multipoint, Touch Controls screens, toggles sliders/Multipoint (**CONTAMINATION** relative to this Group's "idle after connect" instruction) — lands as 61 non-empty `Sent` frames on chandle `0x0001`'s DLCI `0x03` (`libmaestro`-equivalent this session), not individually field-decoded this pass | User (App) | — | chandle `0x0001` DLCI `0x03`, 61 frames |
| `06:51:57.185` | Disconnection Complete, chandle `0x0001`, reason `0x13` (Remote User Terminated, i.e. Buds-initiated) — occurs **before** the documented end of the settings-browsing window (06:52:23), meaning part of that browsing happened while already disconnected (its later portion lands on chandle `0x0004`'s DLCI `0x02` instead, see below) — plausibly the buds were placed back in the case around here (consistent with `DECISIONS.md` ADR-016 finding 5), not video-confirmed at this exact second | Buds (Auto) | — | frame 2484 |
| `06:52:46.781` | **Connection Complete, chandle `0x0004`** — video-confirmed (`w176.png`) — this is the real "Window 1" reconnect; ~16–27s earlier than the originally-documented `06:53:02`–`13` toggle/connect sequence | App/OS (Auto) | `OBS-005`, `PAIR-003` | frame 2814 |
| `06:52:51.026`/`.211` | DLCI 0x04 Get (`08 11 00 00`)/Notify (`Current=0x80` Transparency, `Settable=0xe8`) immediately after this reconnect | App/OS (Auto) | `OBS-005` | frames 3261/3282 |
| `06:52:53.26`–`~06:53:2x` | **63 `Sent` writes on DLCI `0x02`** (this connection's `libmaestro` numbering) — a second batch of settings-browsing writes, following the 61 already logged on chandle `0x0001`'s DLCI `0x03` above | User (App) | — | frames 3427 onward (63 total) |
| `06:53:04.495`, `06:53:19.910` | Two more DLCI 0x04 Notify frames (NC `0x08`, then Transparency `0x80`) with **no preceding Get and no Set(`0x12`) frame anywhere in this entire log** — see Decode/Analysis, plausibly a hardware press-and-hold gesture (Buds-initiated mode Notify with no phone-side command, matching `CAP-027-FINDINGS.md` §4's established mechanism) | Buds (Auto)? | — | frames 3695, 3847 |
| `06:53:26.030`/`.109` | DLCI 0x04 Get/Notify pair (Transparency `0x80`, `Settable=0xe8`, still undocked) | App/OS (Auto) | `OBS-005` | frames 4116/4122 |
| `06:53:27.150` | DLCI 0x04 Notify: `Settable=0x00` (**docked**), `Current=0x20` (Off) — first docked reading this session, ~17s **before** the documented "Window 1 end" placement time | Buds (Auto) | `OBS-005` | frame 4158 |
| `06:53:44` | Buds visibly held near/placed at the case (video-confirmed, `w234.png`) | User (Hardware) | `OBS-005` | — |
| `06:53:46.090` | Disconnection Complete, chandle `0x0004`, reason `0x13` | Buds (Auto) | `OBS-005` | frame 4247 |
| `06:53:57` | **Video ends** (ffprobe duration 247.07s) — quick-panel shown, Buds listed L:100%/C:85%/R:100%, not "Active" (`t246.5.png`) | — | — | — |
| `06:53:46`–`06:56:09.778` | **No further Buds Connection Complete event anywhere in the log** — only unrelated BLE scan/advertising activity. "Window 2" (documented 06:54:14–06:55:53) has zero video and zero wire evidence. | — | `OBS-005` | (absence, log end frame 4426 @06:56:09.778) |

**Contamination log:** the EQ/In-Ear/Multipoint/Touch-Controls settings browsing spans both
connections — 61 `libmaestro`-write frames land on chandle `0x0001`'s DLCI `0x03` before its
06:51:57 disconnect, and a further 63 land on chandle `0x0004`'s DLCI `0x02` from 06:52:53 onward
— not, as an earlier pass concluded, exclusively on the second connection (that conclusion rested
on searching only the literal DLCI number `0x02`, missing that this session's first connection
used `0x03` for the same logical channel — RFCOMM numbering is session-local,
`CAP-001-FINDINGS.md` §2). Neither batch is individually field-decoded this pass. **Window 2 is
not verifiably executed** — flagged as a capture gap, not a positive or negative result for the
case-lid-trigger question. **The more significant, evidence-based correction this pass found:**
the first reconnect's "Notify ANC state" frame reads `Settable-toggles=0x00` (**docked**), not the
`0xe8` (undocked) `OBS-005`'s "realistic worn reconnect" test intended — see the row above and
`CAP-038-FINDINGS.md` §3 for the full evidence and the open question this raises.

## Decode / Analysis

```
tshark -r CAP-038-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle>" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e btrfcomm.dlci -e btrfcomm.len -e data.data
```

- [x] **Does the "Get ANC state"/"Notify ANC state" pair still fire on this realistic reconnect?**
      **Yes, on both reconnects**, once session-local DLCI renumbering is accounted for
      (`CAP-001-FINDINGS.md` §2): chandle `0x0001` carries it on DLCI `0x05` (frames 1143/1154),
      chandle `0x0004` on the more usual DLCI `0x04` (frames 3261/3282). No miss, consistent with
      `DECISIONS.md` ADR-022.
- [x] **Does the `Settable-toggles` byte match "undocked" (`0xe8`)?** **Mixed, and the actually
      significant result of this session:** the FIRST reconnect (immediately after the Buds were
      reported removed from the case) reads **`0x00` (DOCKED)** (frame 1154) — the opposite of what
      `OBS-005`'s "realistic worn reconnect" test intended. The SECOND reconnect reads `0xe8`
      (undocked) on every Notify while genuinely out-of-case (frames 3282, 3695, 3847, 4122), then
      correctly flips to `0x00` at frame 4158 (06:53:27.150), closely preceding a real disconnect.
      Not force-reconciled — see `CAP-038-FINDINGS.md` §3 for the two equally-consistent
      explanations this capture cannot distinguish between.
- [x] **New traffic not seen in `CAP-036`/`CAP-037`:**
      1. **The first reconnect's `Settable-toggles=0x00` (docked) reading** — a genuine tension with
         this Group's test design (see above and `CAP-038-FINDINGS.md` §3), not an absence of any
         channel — both `libmaestro` and the Fast Pair Message Stream open normally, just under
         different DLCI numbers than usual (`0x03`/`0x05` instead of `0x02`/`0x04`) on this
         connection.
      2. **Two ANC-mode Notify frames with no preceding Get and no Set(`0x12`) anywhere in the log**
         (frames 3695, 3847) — plausibly a hardware press-and-hold gesture accidentally triggered
         while handling the Buds (matches `CAP-027-FINDINGS.md` §4's established Buds-initiated
         Notify-without-Set mechanism exactly), 🟡 HYPOTHESIS, not confirmed on video (camera was on
         the phone screen, not the buds).
      3. **No AVDTP/A2DP signaling anywhere in the log** (`tshark -Y avdtp` → 0 rows) — a clean
         negative; despite the Buds allegedly being worn, no A2DP audio-streaming profile was
         established this session.
      4. **No BLE GATT Battery-Service (`0x180F`) traffic** — the only BLE GATT activity in the log
         belongs to an unrelated device (`48:bd:eb:a0:99:c7`, confirmed via
         `bthci_evt.le_meta_subevent`), excluded per the CLI-hygiene precedent
         (`CAP-014-FINDINGS.md` §4a) — a clean negative once correctly attributed.
      5. **DLCI `0x0b`** opens on chandle `0x0001` (6 frames) but carries no decodable content
         checked this pass — flagged, not pursued (out of this Group's scope).
      Result: item 1 is the session's central tension (§3); item 2 is a new HYPOTHESIS-level
      observation; items 3–4 are clean negatives (once correctly attributed away from the unrelated
      BLE device).
- [ ] **Window 2 (if run):** **not verifiably executed** — see Event Timeline correction. No video
      coverage (video ends ~06:53:57, before Window 2's documented 06:54:14 start) and no wire
      evidence (no further Buds Connection Complete anywhere in the rest of the log). Result:
      inconclusive by absence of evidence, not a negative result for the case-lid-trigger question.
- [x] **Outcome classification: (c), partially — inconclusive for Window 2** (no evidence either
      way), but **(a) for Window 1** — a genuine tension found (the first reconnect reads "docked"
      immediately after a reported case-removal; unexplained ANC Notifies without a Set), recorded
      as 🔴 open question / 🟡 HYPOTHESIS respectively, pending replication. Not a clean (b) negative
      overall.

## Open Questions

- 🔴 Why does the first reconnect's "Notify ANC state" frame read `Settable-toggles=0x00` (docked),
  immediately after the Buds were reported physically removed from the case for this Group's
  "realistic worn reconnect" test? Two explanations are equally consistent with this one data
  point and this capture cannot distinguish them (query timing vs. sensor lag) — see
  `CAP-038-FINDINGS.md` §3.
- 🔴 What produced the two ANC-mode Notify frames (3695, 3847) on DLCI 0x04 with no preceding Get
  and no Set(`0x12`) frame anywhere in the log? Plausibly a hardware press-and-hold gesture
  (matches `CAP-027-FINDINGS.md` §4's mechanism) but not video-confirmed.
- 🔴 Window 2 (case-lid-only reconnect trigger) remains untested — needs a re-run with video
  coverage extending through the full intended session length.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13) — see
      `CAP-038-FINDINGS.md` §8.
- [x] Write `CAP-038-FINDINGS.md` per `PROJECT_RULES.md` §2, hex & script rule (§1 rule 4a).
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index (status → `captured`/`analyzed`) and
      `id_registry.csv`'s `CAP-038` row. **(Owned centrally — not done by this pass.)**
- [ ] Add `OBS-005`'s Evidence column pointer in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (pointer only).
      **(Owned centrally — not done by this pass.)**
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8). **(Owned centrally —
      not done by this pass; the three open questions are recorded above and in
      `CAP-038-FINDINGS.md` §9 ready to be copied in.)**
- [ ] **Do not** promote anything to 🟢 FACT or write a `DECISIONS.md` ADR without explicit
      maintainer sign-off (`AGENTS.md` §6, §15) — propose only. (Honored: nothing in this file or
      `CAP-038-FINDINGS.md` promotes a FACT beyond what was already an approved ADR/FACT before
      this session, and no new ADR was drafted.)
- [x] Rename this capture's folder to the actual session date/start-time/end-time — now
      `CAP-038-2026-09-06_06-49-50_06-53-57-Group_AE`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-038-2026-09-06_06-49-50_06-53-57-Group_AE/CAP-038-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-038-2026-09-06_06-49-50_06-53-57-Group_AE/CAP-038-EVENT-NOTES
