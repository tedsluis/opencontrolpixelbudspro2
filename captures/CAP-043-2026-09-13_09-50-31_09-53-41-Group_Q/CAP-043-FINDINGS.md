# CAP-043: Passive BLE Scan for Battery Notification, connection-free repeat (Group Q repeat, `BATT-002`/`BATT-003`)

Standardized, evidence-based extraction from `CAP-043-btsnoop_hci.log` + `CAP-043-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-043` |
| Purpose | Repeat of `CAP-011` (Group Q item #18) — clean, connection-free passive BLE scan for the Fast Pair Battery Notification advertisement (`PROTOCOL.md` §4.3 Option A) |
| Date | 2026-09-13 |
| Firmware | `release_5.203` (⚪ ASSUMPTION — maintainer-supplied session context; not independently re-derived from this capture's own wire data, since zero DLCI 0x08/classic traffic exists in this log to carry a firmware string) |
| Test device | Pixel 7a, Android 17 (🟢 FACT — `com.android.version: 17` MP4 container tag, confirmed via `ffprobe`), official Pixel Buds Companion App force-stopped for the entire session (see §2), Google Play services active (⚪ ASSUMPTION, maintainer-supplied context) |
| App version | `1.0.955078536` (⚪ ASSUMPTION, maintainer-supplied context — not wire-verifiable this session, no DLCI 0x08 traffic) |
| Log file | [`CAP-043-btsnoop_hci.log`](./CAP-043-btsnoop_hci.log) — 1,572 packets, 306.895s, 2026-09-13 09:50:27.996–09:55:34.891 (+0200) |
| Notes file | [`CAP-043-EVENT-NOTES.md`](./CAP-043-EVENT-NOTES.md) |
| Video file | [`CAP-043-recording.mp4`](./CAP-043-recording.mp4) — 189.897s (30fps, 1280×720 sensor / 720×1280 displayed), 09:50:31–09:53:41 local time (`ffprobe`/burned-in overlay cross-checked, see §2) |
| Buds MAC (classic, partial per `AGENTS.md` §7/§9) | Not observed anywhere in this session's log — see §3 (isolation confirmed both by dissector-filter and raw-byte scan) |
| `.log.last` present? | No (`ls` confirmed both at prompt-drafting time and at analysis time) — Step G of the shared methodology not applicable |

## 2. Methodology, video review, and isolation check (the entire point of this repeat)

**Video (Step D):** the full 189.9s video was reviewed via tiled contact sheets (`ffmpeg` frame
extraction at 5s intervals, cropped to the phone-screen region, 6 frames per sheet — 7 sheets
covering the entire duration) plus a denser 2fps pass (t=104–120s) to resolve one ambiguous
thumbnail read (see below). The screen shows the system Bluetooth settings panel
("Tap to connect or disconnect a device") for the **entire** video, starting with "Bluetooth is
off" (t=0, 09:50:31) and the "Use Bluetooth" toggle off, transitioning to on within the first ~5s,
after which the bonded "Pixel Buds Pro 2 van Ted" entry appears showing a static
`L: 100%, C: 84%, R: 100% batt...` line that **never changes for the rest of the video**. No app is
ever opened, no navigation away from this screen occurs, and the bottom-bar "X apps are active"
indicator reads a constant "2" throughout — a single 5s-sampled thumbnail at t=115s appeared to read
"3 apps are active" on first pass; a denser 2fps re-check across t=104–120.5s (34 frames) shows
"2 apps are active" at every one of those samples, including the exact t=115s frame — the "3" reading
was a thumbnail-compression misread, not a real transition. No further anomaly is visible anywhere
in the video.

**Isolation check (🟢 FACT, the specific procedure deviation `CAP-011` had):**
```
$ tshark -r CAP-043-btsnoop_hci.log -Y "bluetooth.addr == 04:00:6e:cf:6e:07" | wc -l
0
$ tshark -r CAP-043-btsnoop_hci.log -Y "btrfcomm" | wc -l
0
$ tshark -r CAP-043-btsnoop_hci.log -Y "btsdp" | wc -l
0
$ python3 -c "d=open('CAP-043-btsnoop_hci.log','rb').read(); print(d.count(bytes.fromhex('04006ecf6e07')))"
0
```
Zero classic-side traffic to the Buds' known classic address (`04:00:6e:cf:6e:07`, from
`CAP-001`/`CAP-002`/`CAP-032`/`CAP-033`) anywhere in the log — no Connection Complete
(`bthci_evt.code==0x03`, checked separately, 0 matches across the whole log), no RFCOMM, no SDP.
`tshark`'s own protocol-hierarchy stats (`-z io,phs`) confirm the **entire** log's non-HCI-command/
event content is exactly `bthci_acl → btl2cap → btatt` (BLE GATT only) — there is no `btrfcomm`
branch in the hierarchy at all. **This is a genuinely clean, connection-free capture** — the specific
procedure deviation that capped `CAP-011` (an active classic RFCOMM+GATT connection present
throughout) does not reproduce here.

One coincidental raw-byte match of the classic MAC's **reversed** byte order (`07 6e cf 6e 00 04`)
was found at file offset 3902, inside a `Vendor Command 0x0157` parameter blob (`... 01 57 fd 0a 02
00 04 07 6e cf 6e 00 04 02 00 ...`) — a controller RPA-management vendor command, not any
BD_ADDR-carrying HCI field (`tshark`'s own dissector, which resolves BD_ADDR endianness correctly,
found zero matches). Checked and dismissed as coincidental vendor-parameter byte overlap, following
the same precedent `DECISIONS.md` ADR-018 already used for an analogous single reversed-byte
coincidence in `CAP-033`.

**Video/log timing note, 🔴 not fully resolved (does not affect the isolation conclusion above):**
the log's *only* Bluetooth-adapter `Reset` command (frame 1, the standard full HCI bring-up burst
that completes by log-relative t≈0.1s) falls at absolute **09:50:27.996** local — about 3 seconds
*before* the video's own t=0 frame (09:50:31), which still shows "Bluetooth is off" with the toggle
in the off position. The log contains **exactly one** `Reset` command for its entire 306.9s span (no
second bring-up burst later), so the log cannot show a *separate*, later toggle-on event matching the
video's on-screen animation. Two explanations are consistent with the evidence but neither is
confirmed: (a) this `Reset` reflects the phone's own boot-up (a reboot is required to enable HCI
snoop logging per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2), independent of the later manual toggle the
video shows, and Android's Bluetooth "off" toggle does not necessarily re-issue a full controller
`Reset`; or (b) the video overlay and the log's absolute clock have a small offset not otherwise
detected. **Not investigated further — flagged as an open, non-blocking timing note**, since it does
not change §3's isolation conclusion (which holds regardless of exactly which wall-clock moment
"counts" as the toggle) or affect any Option A payload finding below (§3's own timestamps are taken
directly from the log, independent of this note).

## 3. Analysis: Fast Pair Service (`0xFE2C`) BLE advertisement presence and Option A structural match

```
$ tshark -r CAP-043-btsnoop_hci.log -Y "bthci_evt.le_meta_subevent==0x0d and btcommon.eir_ad.entry.uuid_16==0xfe2c" \
    -T fields -e bthci_evt.bd_addr -e bthci_evt.rssi
```

**320 matching frames, 2 distinct BLE addresses, cleanly separated by RSSI:**

| Address | Frames | RSSI range | Reading |
|---|---|---|---|
| `52:39:01:d3:49:1e` | 60 | **-20 to -23 dBm** | 🟢 FACT — this project's own Buds/Case unit, per the same close-range-RSSI reasoning `CAP-011-FINDINGS.md` §3 used (-25 to -39 dBm there) |
| `4d:f4:ca:40:0c:f7` | 260 | -84 to -104 dBm | 🟡 HYPOTHESIS — an unrelated, distant Fast-Pair-capable device, per `AGENTS.md` §13's own precedent for incidental co-occurring device traffic (e.g. `CAP-004`'s Fitbit). Not this project's own hardware — RSSI alone is ~60-80 dB weaker than the own-device signal and 3-orders-of-magnitude less powerful; not investigated further, out of scope. |

**Sampled service-data payload, own-Buds address (`52:39:01:d3:49:1e`), byte-for-byte identical across all 60 occurrences spanning the full log (frame 133, t=1.06s, through frame 1548, t=295.34s):**

```
$ tshark -r CAP-043-btsnoop_hci.log -Y "frame.number==133" -V
    Service Data - 16 bit UUID
        Length: 22
        Type: Service Data - 16 bit UUID (0x16)
        UUID 16: Google LLC (0xfe2c)
        Service Data: 105213d38028442154a62ab029be65f6a05016
```
Raw payload bytes (19 bytes, following the 2-byte `0xFE2C` UUID):
```
10 52 13 d3 80 28 44 21 54 a6 2a b0 29 be 65 f6 a0 50 16
```

`PROTOCOL.md` §4.3 Option A's documented layout: `[Flags:1=0x00][Account Key Data:var]
[Battery length&type:1, expects 0x33 show / 0x34 hide][L:1][R:1][Case:1]`.

**Checked mechanically (per `AGENTS.md` §13's zero-creativity rule):** byte 0 is `0x10`, not the
required `0x00` Flags value. No byte at any offset in the 19-byte payload equals `0x33` or `0x34` —
there is no candidate position for the documented Battery-length-&-type marker. The payload is
structurally consistent with a generic Fast Pair discovery frame (Account Key Filter-shaped, first
byte plausibly a length/version header per the general Fast Pair discovery-frame convention) rather
than the Battery Notification extension specifically — the same reading `CAP-011-FINDINGS.md` §4 and
`CAP-036-FINDINGS.md` §12.4 already gave their own non-matching payloads (both of which also started
with a non-`0x00` first byte). No further interpretation of the `0x10` header or the remaining bytes
is asserted — an unrecoverable byte's meaning is an open question, not a guess (`PROJECT_RULES.md`
rule 1).

**Status: 🔴 OPEN QUESTION / SECOND CONFIRMED NON-MATCH, under clean isolation this time.** Per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5's "don't force-fit" instruction, and per the Analysis checklist
in `CAP-043-EVENT-NOTES.md`.

## 4. `BATT-002`/`BATT-003` conclusion — closes `CAP-011-FINDINGS.md` §4's open item, with a clean negative

`CAP-011-FINDINGS.md` §4 left two explanations open for its own non-matching payload: (a) the
active-connection procedure deviation was itself an unaccounted-for confound, or (b) the observed
payload is simply a different Fast Pair advertisement sub-type than the Battery Notification
extension. **This repeat isolates explanation (a) and rules it out**: with a rigorously confirmed,
genuinely connection-free capture (§2/§3 above — zero classic connection, zero RFCOMM, zero SDP for
the Buds' own address across the entire 306.9s log), the own-Buds Fast Pair advertisement **still**
does not structurally match Option A's documented layout, and in fact carries the exact same
first-byte signature (`0x10`, not `0x00`) already seen in `CAP-011` (non-isolated) and `CAP-036`
(also non-isolated). Since the non-match reproduces identically whether or not a classic connection
is present, **an active classic RFCOMM connection suppressing/altering the Battery Notification
advertisement is no longer a plausible explanation** — this closes `CAP-011-FINDINGS.md` §4's open
question (a) with a negative result, strengthening explanation (b): the Buds Pro 2, at least under
these idle/case-closed conditions, do not appear to broadcast a Battery-Notification-shaped
`0xFE2C` service-data frame at all; what they do broadcast is a stable, non-rotating (over this
~5-minute window), Account-Key-Filter-shaped frame instead.

**`BATT-003` ("Buds update battery status while worn... trigger = when RFCOMM connects, or when the
value changes") is not testable by this session's own design** — the case stayed closed and idle for
the entire capture (per `CAP-043-EVENT-NOTES.md`'s procedure), the on-screen system Bluetooth-settings
battery line (`L: 100%, C: 84%, R: 100%`) never changed across the video, and the own-Buds
advertisement payload never changed either (§3) — there was no battery-value change and no RFCOMM
connection for this trigger to fire on. Recorded as **not exercised this session**, not as a negative
result for `BATT-003` specifically.

**Incidental finding, not part of this capture's own scope:** the same advertisement also carries a
`0xFEAA` (Google LLC) service-data entry (`402c7c6ad4440e5e95f67898874c2353e4161fdbe7db`, frame 133)
from the *same* address/RSSI as the own-Buds `0xFE2C` entry — i.e. the Buds/Case broadcast both UUIDs
in the same advertisement packet. Not decoded or pursued further here (out of `BATT-002`/`BATT-003`'s
scope), flagged only so it isn't silently lost.

**A separate observation, worth surfacing since the system UI itself is informative here:** the
system Bluetooth settings panel displays a live-looking `L/C/R` battery line for the bonded device
for the *entire* session, despite this session's own isolation check (§2/§3) confirming **no**
classic connection, **no** RFCOMM, and **no** GATT session to the Buds ever formed, and the one BLE
advertisement actually observed from the Buds does not structurally carry a battery value at all
(§3). This value must therefore be a **cached, last-known reading** carried over from a previous
session, not a value refreshed by anything observed on the wire in this capture — 🟡 HYPOTHESIS, not
independently confirmed further (e.g. by checking whether the value matches a specific prior
session's own last reading), but directly relevant to `ARCHITECTURE.md` §3.1's "state is a cache,
never the authority" design principle for this project's own future implementation.

## 5. Cross-check against `PROTOCOL.md`/`DECISIONS.md`

No contradiction found with any existing 🟢 FACT claim. This capture's own result (a second
confirmed non-match) is consistent with, and strengthens, `PROTOCOL.md` §4.3 Option A's existing 🟡
HYPOTHESIS-level status — it does not by itself justify any FACT promotion (there is nothing
positive to promote), and per `AGENTS.md` §6 no such promotion is asserted here regardless.

## 6. Open Questions (carried forward / new)

- 🔴 What Fast Pair discovery-frame sub-type *is* the Buds Pro 2's idle/case-closed `0xFE2C`
  advertisement, if not the Battery Notification extension? (`PROTOCOL.md` §6 — this capture adds a
  second confirmed non-match but does not itself identify the correct sub-type.)
- 🔴 Does the Battery Notification extension ever fire at all for this hardware, under *any*
  condition (e.g. a bud freshly removed/inserted, matching the spec's "optional when a single bud is
  inserted/removed" language) — this session tested only the idle/case-closed condition, per its own
  procedure.
- 🔴 The ~3-second video-overlay-vs-log-absolute-clock discrepancy noted in §2 — not investigated
  further, flagged for awareness only.
- 🟡 What triggers the system Bluetooth settings panel's `L/C/R` battery display when no live
  connection or matching advertisement exists this session (§4's last observation) — plausibly a
  cached value, not independently confirmed.

## 7. Recommended next steps

- A capture bracketing an actual bud insertion/removal event, still connection-free, to test the
  spec's "optional when a single bud is inserted/removed" trigger condition for the Battery
  Notification specifically (not attempted by this session's own case-closed-and-idle procedure).
- If the Battery Notification extension is never observed under any bracketed condition across
  further attempts, this is itself worth raising to the maintainer as a candidate for re-labeling
  Option A's status in `PROTOCOL.md` §4.3 from "🟡 HYPOTHESIS (confirmed as used by the Buds Pro 2
  specifically)" toward a more skeptical framing — a proposal for maintainer review, not asserted
  here.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-043-2026-09-13_09-50-31_09-53-41-Group_Q/CAP-043-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-043-2026-09-13_09-50-31_09-53-41-Group_Q/CAP-043-FINDINGS
