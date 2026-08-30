# Findings: `CAP-033` (Group AA — SDP UUID branch isolation for `gbm.a()`'s "default internal rfcomm socket" path)

Standardized, evidence-based extraction from `CAP-033-btsnoop_hci.log` + `CAP-033-recording.mp4`,
staged here for later promotion into `PROTOCOL.md` per `PROJECT_RULES.md` §2. Every claim below
carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-033` · **Date:** 2026-08-30 · **Firmware:** `release_5.203` (🟢 confirmed
on-wire, 3 occurrences) · **Phone:** Pixel 7a · **App version:** n/a — force-stopped throughout ·
**Log file:** `CAP-033-btsnoop_hci.log` (2,459 packets, 15:16:57.20–15:22:43.50 local/+0200) ·
**Video:** `CAP-033-recording.mp4` (169.16s, 15:17:03–15:19:52 local) · **Buds MAC (partial):**
`04:00:6e:cf:6e:07` — same physical device as `CAP-021`/`CAP-027`.

**Scope note:** Group AA covers `SDP-001` (tested this session) and `SDP-002` (opportunistic, not
attempted — no firmware update was pending).

---

## 1. Isolation-integrity review (required before anything else — see this session's own
`CAP-033-EVENT-NOTES.md` and the reason Group AA exists at all)

The entire value of this session's evidence depends on the companion app genuinely never having a
chance to influence the SDP browse being observed. Three specific concerns were checked before any
conclusion was drawn from the wire data.

### 1.1 Order of Forget vs. Force-stop — 🔴 confirmed procedure violation, but scoped

The written procedure (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA step 1) requires the app to be
force-stopped **before** "Forget." Frame-by-frame video review (2fps extraction, 15:17:15–15:17:41)
shows the opposite order actually happened:

| Video time | What's on screen |
|---|---|
| 15:17:22 | "Forget device?" confirmation dialog shown |
| 15:17:24 | "Forget" tapped — device forgotten |
| 15:17:26 | Back at the home screen (Bluetooth settings closed) |
| 15:17:30 | Recent-apps view, navigating to the Pixel Buds app's info page |
| 15:17:32 | App info screen, "Force stop" button visible |
| 15:17:34 | "Force stop" tapped, confirmation dialog appearing |
| 15:17:36 | Force stop completed |

**Forget (15:17:24) precedes Force-stop (15:17:34) by ~10 seconds** — the reverse of the written
procedure. This is a real, confirmed deviation and is recorded as such regardless of its practical
effect (per this session's own instructions: flag it "ongeacht wat je verder vindt").

**Why this is judged not to have contaminated the SDP-browse evidence specifically:** the actual
re-pairing flow (case opened 15:17:53, pairing button held 15:17:55, device selected 15:18:01,
pair confirmed 15:18:11) and the SDP browse itself (frames 1256–1873, 15:18:09.42–15:18:18.82, §2
below) both happened entirely **after** Force-stop completed at 15:17:34 — i.e. by the time there
was anything for the app to possibly react to, it had already been stopped for ~18+ seconds. The
10-second window during which the deviation existed (15:17:24–15:17:34, app alive but device
already forgotten) produced no bonding or SDP activity at all in the log. This scoping does **not**
erase the violation — it is still recorded as a deviation in §6/`SDP-001`'s status below — but it
means the specific evidence in §2/§3 was not collected while the app was both alive and paired.

### 1.2 The 15:18:16 popup — not a violation, but the original note was imprecise

Video review (2fps, 15:18:05–15:18:25) shows the "user cancels pixel buds app popup" moment
(originally logged in `CAP-033-EVENT-NOTES.md` at 15:18:16) is **not** the Pixel Buds companion app
opening. It is a **system-level** Fast Pair dialog:

| Video time | What's on screen |
|---|---|
| 15:18:11–15:18:14 | Phone still on the Pixel Buds "App info" settings screen (Force stop / Uninstall / Archive buttons visible) |
| 15:18:15 | A dialog titled **"Pixel Buds Pro 2"** appears, reading *"Save device to ted.sluis@gmail.com to connect more quickly to your other devices"*, with Cancel/Save buttons — this is Android/GMS's own Fast Pair account-linking prompt, not an activity belonging to the Pixel Buds companion app's own process |
| 15:18:16 | "Cancel" tapped |
| 15:18:16–15:18:17 | Dialog replaced by a normal system notification banner ("Pixel Buds Pro 2 — Left 100% Case 37% Right 100%"); the underlying screen is still the Pixel Buds app's "App info" settings page |

This falls comfortably inside the 60-second post-bonding isolation window (bonding complete
~15:18:09, window extends to ~15:19:09) and does **not** foreground or launch the companion app —
the app info screen is a system Settings page, not the app itself running. The original event-note
label is corrected in this session's `CAP-033-EVENT-NOTES.md`. **Conclusion: not a violation.**

### 1.3 Step 3 (open the app for baseline comparison) — 🔴 confirmed never executed

Two independent checks confirm step 3 of the procedure ("open the companion app normally... baseline
for in-session comparison") never happened, on- or off-camera:

- **Video:** 2fps sampling from 15:18:16 through the last frame at 15:19:52 shows the screen
  statically on the Pixel Buds "App info" settings page for the entire remainder of the recording —
  "Screen time: 0 minutes today" is visible and unchanged throughout, and no tap/navigation away
  from that screen occurs.
- **Log:** the log continues **2m51s past the end of the video** (to 15:22:43.50), but
  `tshark -r CAP-033-btsnoop_hci.log -Y "btsdp"` returns **zero** frames after frame 1873
  (15:18:18.82) anywhere in that remaining span. If the app had been opened and run its own
  `fetchUuidsWithSdp()` at any point — even after the camera stopped — a second SDP browse would be
  expected; none appears.

**Per this session's own instructions:** this is reported explicitly as "the before/after
comparison is not possible this session," rather than forcing a conclusion from the pre-app-open
half alone.

### 1.4 Net effect on `SDP-001`'s evidentiary status

Per this session's own instructions, if either 1.1 or 1.2 turns out to be a genuine violation, the
`SDP-001` conclusion must not be recorded as 🟢 FACT regardless of what the wire data shows. §1.1
**is** a confirmed order violation (even though its practical blast radius on the SDP-browse window
itself looks contained); §1.2 is not a violation. Combined with §1.3's missing comparison half,
**`SDP-001`'s result in this capture is capped at 🟡 HYPOTHESIS** — see §6.

## 2. The SDP browse (🟢 FACT for what was observed)

`tshark -r CAP-033-btsnoop_hci.log -Y "bluetooth.addr == 04:00:6e:cf:6e:07 and btsdp" -T fields
-e frame.number -e frame.time -e _ws.col.Info` — **all** `btsdp` traffic in the entire log falls in
one tight window, **frames 1256–1873, 15:18:09.417–15:18:18.822** (9.4 seconds), immediately following
bonding completion (`Link Key Notification`, frame 1242, 15:18:09.171):

| Phase | Frames | Time range | Content |
|---|---|---|---|
| Pre-bonding | — | — | No `btsdp` traffic exists before frame 1242 (bonding) in this log |
| **Isolation window (step 2)** | 1256–1873 | 15:18:09.417–15:18:18.822 | The **entire** SDP browse — PnP Information, L2CAP, Phonebook Access, Hands-Free, HID, AG Hands-Free, Audio Sink, and the full "browse all services" response (frame 1279, §3) |
| Post-app-open (step 3) | — (none) | — | **No `btsdp` traffic exists anywhere after frame 1873**, including the 2m51s of log recorded past the video's end (§1.3) |

Every SDP frame that exists in this session falls inside the pre-app-open isolation window — there
is no "after" phase to compare it against (§1.3).

## 3. UUID → RFCOMM-channel → service-name mapping (🟡 HYPOTHESIS, proposal — see framing below)

Frame 1279 (`Rcvd Service Search Attribute Response`, answering a generic "browse all services"
request) is a large (1232-byte reassembled) response that names every RFCOMM service the Buds
advertise, including human-readable service-name strings. Raw hex (`tshark -r
CAP-033-btsnoop_hci.log -Y "frame.number==1279" -x`, reassembled-SDP section, offsets relative to
that 1232-byte blob):

```
...
0420  01 00 0d 09 00 01 35 11 1c 25 e9 7f f7 24 ce 4c    (UUID 25e97ff7-24ce-4c4c-8951-f764a708f7b4)
0430  4c 89 51 f7 64 a7 08 f7 b4 09 00 04 35 0c 35 03
0440  19 01 00 35 05 19 00 03 08 03 09 00 09 35 16 35    (RFCOMM channel = 0x03)
0450  14 1c 25 e9 7f f7 24 ce 4c 4c 89 51 f7 64 a7 08
0460  f7 b4 09 01 02 09 01 00 25 09 44 45 42 55 47 20    ("DEBUG APP")
0470  41 50 50 ...
0480  01 35 11 1c 25 e9 7f f7 24 ce 4c 4c 89 51 f7 64    (UUID 25e97ff7-24ce-4c4c-8951-f764a708f7b5 = "pigweed", ADR-018)
0490  a7 08 f7 b5 09 00 04 35 0c 35 03 19 01 00 35 05
04a0  19 00 03 08 01 09 00 09 35 16 35 14 1c 25 e9 7f    (RFCOMM channel = 0x01)
04b0  f7 24 ce 4c 4c 89 51 f7 64 a7 08 f7 b5 09 01 02
04c0  09 01 00 25 0b 4d 41 45 53 54 52 4f 20 41 50 50    ("MAESTRO APP")
```

(Full 1232-byte blob available in the raw capture; the excerpt above is the tail containing the two
records most relevant to `DECISIONS.md` ADR-018/this Group's stated purpose. The complete record
set, decoded the same way — service-name string + UUID + `[Group:1][Code:1][Length:2][RFCOMM
channel:1]` protocol descriptor — across the full response:)

| SDP service name (wire string) | UUID | RFCOMM channel | Corresponding DLCI (`channel×2`) | Already documented as |
|---|---|---|---|---|
| **"MAESTRO APP"** | `25e97ff7-24ce-4c4c-8951-f764a708f7b5` | 1 | `0x02` | `libmaestro`/Pigweed channel — the "pigweed" UUID from `DECISIONS.md` ADR-018, byte-for-byte |
| **"GFPS RFCOMM"** | `df21fe2c-2515-4fdb-8886-f12c4d67927c` | 2 | `0x04` | Official Fast Pair Message Stream (`PROTOCOL.md` §2.1/§4.1) |
| **"DEBUG APP"** | `25e97ff7-24ce-4c4c-8951-f764a708f7b4` (last byte `b4`, one bit different from "pigweed"'s `b5`) | 3 | `0x06` | Not previously documented — no traffic on this DLCI observed in this session |
| **"GSND CONTROL"** | `f8d1fbe4-7966-4334-8024-ff96c9330e15` | 4 | **`0x08`** | The private, 🔴-open-identity envelope (`PROTOCOL.md` §2.3) |
| **"GSND AUDIO"** | `81c2e72a-0591-443e-a1ff-05f988593351` | 5 | `0x0a` | The unattributed DLCI 0x0a burst from `CAP-021-FINDINGS.md` §4a |
| "Handsfree" (standard UUID16 `0x111E`) | — | 6 | `0x0c` | HFP AT-command channel (`PROTOCOL.md` §4.3 Option C) — traffic on this DLCI in this session is ordinary HFP SLC setup |
| **"BTIS"** | `e7ab2241-ca64-4a69-ac02-05f5c6fe2d62` | 9 | `0x12` | Not previously documented — no traffic on this DLCI observed in this session |

**Raw-byte scan confirms the "pigweed"/"MAESTRO APP" UUID is genuinely present** (not just in the
dissected SDP record): `python3` scan of the raw log file finds `25e97ff724ce4c4c8951f764a708f7b5`
twice (offsets 68980, 69021 — both inside frame 1279's payload) and its byte-reversed form once
(offset 55861, inside frame 1072, a `Rcvd Extended Inquiry Result` — almost certainly a coincidental
16-byte match inside arbitrary EIR advertisement bytes, not a meaningful protocol occurrence, and
not investigated further here as it is unrelated to this Group's scope). **The "default" UUID
(`3a046f6d-24d2-7655-6534-0d7ecb759709`) and its byte-reversed form: zero occurrences**, in the raw
byte scan across the entire log, not just the SDP-dissected frames — this session does not change
the standing negative result already established across every other capture this project has.

**Framing of this finding — explicitly not a promotion.** This is a genuinely new correlation:
`PROTOCOL.md` §2.3 records DLCI 0x08's protocol identity as 🔴 **OPEN QUESTION**, and
`CAP-021-FINDINGS.md` §4a records DLCI 0x0a's content as 🔴 **OPEN QUESTION**. This session's SDP
browse gives both channels an on-the-wire **service name** ("GSND CONTROL" and "GSND AUDIO"
respectively) and a UUID, for the first time — a concrete new lead, not a full resolution: knowing
a channel is *named* "GSND CONTROL" does not by itself reveal its Group/Code semantics, and
"GSND"'s expansion is not determinable from this evidence alone. Per `AGENTS.md` §6, this is
recorded here as a 🟡 **HYPOTHESIS awaiting maintainer review**, not committed as a `PROTOCOL.md`
promotion or a `DECISIONS.md` entry — see §7's recommended next step for how to close it out.
Similarly, this SDP record independently corroborates — from wire-visible service-name strings
rather than only APK static analysis — `DECISIONS.md` ADR-018's finding that DLCI 0x02 is the
companion app's own "Maestro" socket; this strengthens that existing, already-FACT finding rather
than changing its status.

## 4. Standard/expected SDP records (🟢 FACT, no further action)

The browse also returned ordinary, already-expected records not specific to this Group's question:
PnP Information, L2CAP, Phonebook Access Client, Hands-Free (§3's table), HID, AG Hands-Free, Audio
Sink (A2DP), AVRCP (Target/Controller), Generic Audio, "Android Gamepad"/"Android HID Device" (HID
descriptor for touch controls, consistent with `ARCHITECTURE.md` §1's HID hypothesis, not further
investigated here), and PnP-class records for "Google Inc." Not itemized further — out of this
Group's scope and not gesture/UUID-relevant.

## 5. `SDP-001` conclusion (🟡 HYPOTHESIS — capped by §1.4, not a full negative or positive)

- **What the pre-app-open (OS-only, force-stopped-app) SDP browse actually returned:** the full
  named service list, including "MAESTRO APP" (the "pigweed" UUID) — i.e. the OS's own default
  pairing-time SDP browse already retrieves the complete custom-UUID set, not some reduced subset,
  even with the companion app definitely not running. The "default" UUID from `DECISIONS.md`
  ADR-018 does not appear, consistent with every prior capture.
- **Why this cannot be raised to 🟢 FACT or even a clean 🟡/negative this session:** §1.4 — the
  Forget/Force-stop order violation (§1.1), even though scoped away from the SDP evidence window
  itself, and the complete absence of a step-3 comparison (§1.3) together mean this session answers
  only half of `SDP-001`'s actual question ("does the UUID set depend on *who* triggers the
  browse") — there is no "app-triggered" browse in this log to compare the OS-triggered one
  against.
- **Net status:** 🟡 HYPOTHESIS — "the default UUID does not appear during an OS-only,
  app-force-stopped SDP browse" (consistent with, not novel beyond, the existing cross-capture
  negative result) — pending a repeat session that (a) force-stops the app strictly before Forget,
  and (b) actually completes step 3, before this can be written into `PROTOCOL.md`/`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s
  `SDP-001` row as resolved either way.

## 6. `SDP-002` — not attempted

No firmware update was pending or available at capture time. Per the procedure's own guidance,
recorded here as `not attempted` rather than forced or left silently blank.

## 7. Open questions

- 🔴 What do "GSND CONTROL" (DLCI 0x08) and "GSND AUDIO" (DLCI 0x0a) actually stand for/do? (§3) —
  proposed as a lead for `PROTOCOL.md` §2.3/§6 and `CAP-021-FINDINGS.md` §4a, pending maintainer
  review; not committed as a promotion.
- 🔴 "DEBUG APP" (channel 3, DLCI 0x06) and "BTIS" (channel 9, DLCI 0x12): neither showed any
  traffic in this session, so nothing beyond their SDP-advertised name/UUID/channel is known. Not
  previously documented anywhere in this project's `.md` files (checked via grep before writing
  this section).
- 🔴 A proper, isolation-clean repeat of `SDP-001` (force-stop strictly before Forget, and an
  actually-executed step 3) is still needed before this Test-ID can be considered closed either
  way — see §5.

## 8. Recommended next steps

- Repeat `SDP-001` with the two isolation issues fixed: force-stop the app *before* tapping
  "Forget" (not after), and make sure step 3 (opening the app) actually happens on-camera before
  the recording stops.
- Cross-check "GSND CONTROL"/"GSND AUDIO" against `REVERSE_ENGINEERING.md`'s JADX output for any
  string table entry containing "GSND" (a maintainer/AI keyword-search pass, per the
  `AGENTS.md` §6/`DECISIONS.md` ADR-017 mechanical-assistance boundary already used for the
  "pigweed"/"default" UUID discovery itself) — this is the natural next step to turn §3's lead into
  something promotable.
- Decode DLCI 0x0a in a session that actually opens it (this one didn't) using the SDP-confirmed
  "GSND AUDIO" identity as a naming anchor for `CAP-021-FINDINGS.md` §4a's still-unattributed
  1123-frame burst.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-033-2026-08-30_15-17-03_15-19-52-Group_AA/CAP-033-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-033-2026-08-30_15-17-03_15-19-52-Group_AA/CAP-033-FINDINGS
