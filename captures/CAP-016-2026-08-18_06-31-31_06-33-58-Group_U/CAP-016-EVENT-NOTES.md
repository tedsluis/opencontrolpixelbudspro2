# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group U Capture (`CAP-016`, 2026-08-18 re-run)

**Status:** Reviewed frame-by-frame against `CAP-016-recording.mp4`'s burned-in wall-clock overlay
(1s resolution throughout; sub-second resolution via extra frame extractions around every
transition of interest) and cross-checked against `CAP-016-btsnoop_hci.log` via `tshark`. This
supersedes the placeholder draft that previously occupied this file (it contained only the video's
start/end lines).

**Do not confuse this capture with the other, older `CAP-007` folder**
(`CAP-007-2026-08-16_09-14-10_09-17-57-Group_U/`). That is a separate, independent session (case
opened before recording, bud lifted from the still-open case, no case-close/reopen bracket, no
buds-back-in-case ending) with its own `CAP-007-EVENT-NOTES.md`/`CAP-007-FINDINGS.md` — this
document only covers the `06-31-31` session's own video/log pair.

**Scope note (per `PROJECT_RULES.md` §1 — stated plainly rather than silently reinterpreted):**
`CAPTURE_BLUETOOTH_HCI_SNOOP.md`'s own Group U write-up defines a narrower procedure (bracket
DLCI 0x08 Group `0x04` Code `0x12`'s liveness value around a bud-removed/case-closed/idle
sequence). This session's actual on-camera content is broader and more general-purpose: a full
Bluetooth-on → both-buds-removed → case-close/reopen (empty) → both-buds-docked → disconnect
→ case-close cycle. It exercises `CASE-004`/`CASE-005` (bud removed from case) cleanly and
`CASE-006` (buds docked, lid closed) cleanly, and it does re-confirm the DLCI 0x08 Code `0x12`
liveness behavior opportunistically (see `CAP-016-FINDINGS.md` §4). **It does not exercise
`INEAR-002`/`INEAR-003`/`INEAR-004`** — no earbud is ever shown being inserted into or removed
from an ear on camera; both earbuds go straight from the case to the user's hand/off-camera and
back, never past frame into an ear. This is flagged explicitly rather than left for a reader to
assume otherwise.

## Log Metadata

|      Field       |                       Value                        |
|------------------|----------------------------------------------------|
|    Capture ID    |                      `CAP-016`                     |
|      Group(s)    | U (case/bud-removal focus — see scope note above)  |
|       Date       |                     2026-08-18                     |
| Firmware version | `release_5.203` — confirmed on-wire this session (frames 1544/1584/1590, ASCII `release_5.203`) |
|   Test device    | Pixel 7a, Android 17 (build `CP2A.260705.006`, visible on-screen at 06:31:45) — Android's own system Bluetooth "Device details" page, not the dedicated Pixel Buds companion app (never shown on camera) |
| Video file       | `CAP-016-recording.mp4` — 147.39s, 720x1280, 30fps; starts 06:31:31, ends 06:33:58 (wall clock, +0200, burned-in on-screen overlay, whole-second resolution) |
| Log file         | `CAP-016-btsnoop_hci.log` — 785.31s, 3,404 packets, 2026-08-18 06:23:12.4636–06:36:17.7734 (wall clock, +0200). `capinfos CAP-016-btsnoop_hci.log`. **The log is a shared, non-restarted buffer starting ~8m19s before the video and ending ~2m20s after it** — `tshark`'s own `frame.time` field is in local wall-clock (+0200) and is directly comparable to the video's on-screen overlay with no offset arithmetic needed. |
|    Devices       | Phone `e8:d5:2b:7e:ca:81` (`Google_7e:ca:81`, Pixel 7a — same as `CAP-001`–`CAP-007`(old)); peer/Buds classic BD_ADDR `04:00:6e:cf:6e:07` (`Google_cf:6e:07`, same physical device); a **second**, distinct BLE address `4f:25:00:85:9a:b1` also appears (see §1) |

## Method

Base filtering follows `AGENTS.md` §13's CLI-hygiene rule. This log's Bluetooth address filter
(`bluetooth.addr`) does not populate for this capture's dissector/encapsulation (H4-with-Linux-header,
confirmed empty for all 3,404 frames), so filtering here instead uses the ACL **connection handle**
recovered from the `Connect Complete`/`LE Enhanced Connection Complete` events (`bthci_acl.chandle`),
which is the equivalent per-link filter for this log:

```
tshark -r CAP-016-btsnoop_hci.log -Y "bthci_acl.chandle==0x0001"   # classic link to cf:6e:07
tshark -r CAP-016-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002"   # BLE link to 4f:25:00:85:9a:b1
```

Only these two connection handles exist anywhere in the 785s log (`tshark -r CAP-016-btsnoop_hci.log -T fields -e bthci_acl.chandle | sort -u`) — i.e. no third device (see §5's unrelated earbud) ever
establishes a Bluetooth session with the phone in this log.

## Event Timeline

Times are the video's own on-screen wall-clock overlay (±1s; sub-second brackets extracted with
extra `ffmpeg -vf fps=5`/`fps=10` passes are noted explicitly where used). `frame.time` values from
`tshark` are quoted directly (already in local +0200 wall clock) wherever log evidence is cited.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-016-btsnoop_hci.log` |
|---|---|---|---|---|
| 06:31:31 | Start video recording. Screen: system **Bluetooth** settings sheet, "Bluetooth is off", `Use Bluetooth` toggled off. Case sits atop the phone, **lid already open**, both earbuds visible inside — case was opened before recording started (off-camera; not captured) | — | — | — |
| 06:31:35 | Tap **Use Bluetooth** toggle | User (App) | — | — |
| 06:31:40 | Quick Settings tile & notification shade show "Pixel Buds Pro 2 van Ted — Left 100% Case 100% Right 100% battery" (BLE-only battery info; no classic profile connected yet) | App (Auto) | — | `LE Enhanced Connection Complete` at **06:31:40.983** (frame 691) to peer `4f:25:00:85:9a:b1`, connection handle `0x0002` — 🟡 HYPOTHESIS this is the Buds' BLE/GATT link (address differs from the classic BD_ADDR; consistent with a resolvable/random BLE address, common for Fast-Pair-capable peripherals — not independently confirmed to be the same physical device beyond the exact timing match) |
| 06:31:45 | Quick Settings panel shown, Bluetooth tile active (purple); Android build `17 (CP2A.260705.006)` visible on-screen | — | — | — |
| 06:31:55 | **Device details** screen for "Pixel Buds Pro 2 van Ted": Left 100%/Case 100%/Right 100%, `Forget`/`Connect` buttons (classic profile **not yet connected**) | — | — | — |
| 06:32:00–02 | Earbud in the slot **nearest the case hinge** lifted out by hand (held off-camera from here on) | User (Hardware) | `CASE-004`/`CASE-005` (see note below) | `Rcvd Connect Request` **06:32:02.531** (frame 1213, Buds-initiated) → `Sent Accept Connection Request` **06:32:02.545** (1214) → `Rcvd Connect Complete` **06:32:02.749** (1217, handle `0x0001`, `04:00:6e:cf:6e:07`, status `0x00`) — 🟢 FACT: this is a **Buds-initiated page**, not a phone-side `Create Connection` (contrast `CAP-001-FINDINGS.md` §1's three phone-initiated attempts) |
| 06:32:03–07 | (not visible on screen — app UI mid-transition) | — | — | RFCOMM channels 0/1/2/4/5 (DLCI `0x00`/`0x02`/`0x04`/`0x08`/`0x0a`) all opened (frames 1458–1750); firmware string `release_5.203` exchanged (frames 1544, 1584, 1590); first ANC-state notify pushed, `08 13 00 04 01 e8 00 20` (frame 1521, **06:32:03.567**) — "settable toggles" byte `0x00`, current-state byte `0x20` (Off) |
| 06:32:04–05 | App UI updates: "Active" label + `Disconnect` button replace `Forget`/`Connect`; "Active noise control" row shown but **no mode highlighted** | App (Auto) | — | (see above — coincides with RFCOMM channel-open burst) |
| 06:32:19–20 | **Second (last) earbud** lifted out of case — case now fully empty, lid still open | User (Hardware) | `CASE-004`/`CASE-005` (see note below) | ANC-state notify updates to `08 13 00 04 01 e8 e8 80` (frame 2128, **06:32:18.631**) — settable-toggles byte now `0xe8` (matches the constant seen throughout `CAP-001`/`CAP-004`), current-state `0x80` (Transparency). **No RFCOMM channel teardown/reopen accompanies this push** (checked: zero SABM/UA/DISC frames in the surrounding ±5s window) — a clean, isolated state-change push, not a generic re-announcement-on-reopen |
| 06:32:20 | App UI updates: **Transparency** becomes the highlighted ANC mode | App (Auto) | — | (~1.4s UI lag after the 06:32:18.631 wire push, consistent with app rendering latency) |
| 06:32:20–44 | Case remains open and empty; connection stays **Active**; ANC stays on **Transparency**; phone/case idle on stand (earbuds off-camera) | — | — | Only routine periodic traffic: repeated ANC-Transparency re-notifies (frames 2220 @06:32:23.195, 2245 @06:32:25.013, 2273 @06:32:27.265 — all `...e8e880`), periodic device-info/HFP-battery heartbeat, and DLCI 0x08 Code `0x12` pushes cycling `0x02`/`0x03` (frames 1536, 2148, 2396, 2479 …) |
| 06:32:44–51 | Hand closes the (**empty**) case lid; connection remains Active throughout (both buds are elsewhere, not in the case) | User (Hardware) | `OBS-003` (step 2) | Only the same routine periodic traffic continues through this window (e.g. frames ~2505–2544, `google-pixel-buds-pro-v1` device-info re-announce + `AT+BIEV`-shaped HFP heartbeat) — **zero dedicated wire signal tied to the lid closing** |
| 06:33:02–04 | Case lid **reopened** (still empty) | User (Hardware) | `OBS-003` (step 2, reverse) | Same — no dedicated signal found in this window either |
| 06:33:15.94–06:33:21.87 | (no camera-visible physical action — case sits open+empty, no hands near case/phone in frame) | — | — | Full RFCOMM multiplexer channel bounce: DLCI `0x02`/`0x04`/`0x08`/`0x0a` all `DISC`+reopened in sequence (frames 2770–3044, first `DISC` at **06:33:15.941**, last reopen `UA` at **06:33:19.615**) — ACL link itself undisturbed (no `Disconnect Complete`/`Connect Complete` near this window). ANC re-announced mid-bounce: still `...e8e880` (Transparency) at frame 2768 (06:33:15.940) and frame 3012 (06:33:22.359), then reverts to `...e800 20` (settable=`0x00`, Off) at frame 3054 (**06:33:23.456**) |
| ≈06:33:16 | App UI: ANC row **loses its Transparency highlight**, all 4 buttons revert to unselected | App (Auto) | — | Coincides with the channel-bounce's start (06:33:15.94) — see `CAP-016-FINDINGS.md` §3 for discussion; **no physical trigger identified in the video for this bounce** |
| 06:33:16–45 | An earbud/case that is **visibly a different product** from the white Pixel Buds Pro 2 (black body, gray oval capacitive touch pad) becomes visible near the case and is briefly handled by hand; it is **never placed into the Pixel Buds case** and never appears connected in the log | — | — | No third Bluetooth session of any kind exists anywhere in the 785s log (only handles `0x0001`/`0x0002`, both already accounted for) — **camera-only incidental object, zero wire correlation**; flagged explicitly so a future pass does not mistake it for a Buds action |
| 06:33:38–40 | First Pixel Bud placed back into an empty case slot (its icon shows the charging-bolt afterward) | User (Hardware) | `CASE-006` (part) | No RFCOMM data frame or control-frame burst found in this window; only unrelated `LE Extended Advertising Report` noise from other nearby BLE devices, plus one `Mode Change` baseband event at **06:33:41.680** — 🔴 not attributable, same caveat as `CAP-007-FINDINGS.md`(old) §3.5 (no documented mechanism ties a baseband mode change to app-level UI/case state directly) |
| 06:33:44–45 | **Second** Pixel Bud placed into the remaining case slot — both slots now filled | User (Hardware) | `CASE-006` (part) | Routine periodic HFP-battery-shaped frame at **06:33:45.072** (frames 3230/3231, `03 03 00 03 e4 64 ff`) immediately precedes the disconnect below — no distinct "docked" signal |
| **06:33:45.152** | — | Buds (Auto) | `CASE-006` (end) | 🟢 FACT: `Disconnection Complete` (frame 3235), handle `0x0001`, status `0x00`, reason **`0x13`** ("Remote User Terminated Connection") — the classic ACL link drops **the instant both buds are docked**; nothing beyond the routine heartbeat precedes it |
| 06:33:46 | App UI updates: "Active" label and the entire "Active noise control" section disappear; `Disconnect` reverts to `Connect` | App (Auto) | — | (~0.8s UI lag after the 06:33:45.152 wire event) |
| 06:33:52–53 | Case lid closes, both buds inside | User (Hardware) | `CASE-006` (end) | — |
| 06:33:53 onward | Both `Left` and `Right` icons show the charging-bolt | App (Auto) | — | — |
| 06:33:57/58 | End of video recording (case closed, `Connect` shown, both buds charging) | — | — | — |

**Physical slot vs. app Left/Right, stated cautiously (🔴 not confirmed):** the case's internal
`L`/`R` markings are never visible on camera, so which physical slot (hinge-side vs. outer) maps to
the app's "Left" vs. "Right" battery icon is **not established** by this capture — described above
by physical position only, per `PROJECT_RULES.md` §1's "no invented interpretation" rule.

## Corrections vs. the placeholder draft

- The placeholder draft contained only the video's start (06:31:31) and end (06:33:58) lines and an
  incomplete metadata table (missing firmware/device/file-duration fields, both marked blank). This
  revision fills in the full metadata table and the complete 20-row event timeline above, all
  independently re-derived from the video and log.
