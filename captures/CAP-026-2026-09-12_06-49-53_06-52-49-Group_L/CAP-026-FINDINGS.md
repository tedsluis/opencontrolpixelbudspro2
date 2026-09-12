# CAP-026: Passive/automatic observation windows (Group L, `BATT-001`, `OBS-001`)

Standardized, evidence-based extraction from `CAP-026-btsnoop_hci.log` + `CAP-026-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-026` |
| Purpose | Group L — main run-through, passive observation windows: (1) idle wait after connecting, watching for the battery-status-on-reconnect notification (`BATT-001`); (2) force-close and reopen the app, watching for any status query on launch (`OBS-001`) |
| Date | 2026-09-12 |
| Firmware | ⚪ ASSUMPTION `release_5.203` (carried over) |
| Test device | Pixel 7a, Android 17, official Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Log file | [`CAP-026-btsnoop_hci.log`](./CAP-026-btsnoop_hci.log) — 2,179 packets, 326.64s, `2026-09-12 06:49:20.832–06:54:47.467`. 0/2,179 `cap_len≠len` mismatches — untruncated. |
| Video file | [`CAP-026-recording.mp4`](./CAP-026-recording.mp4) — 176.57s (`ffprobe`), overlay `06:49:53`–`06:52:49` |
| Notes file | [`CAP-026-EVENT-NOTES.md`](./CAP-026-EVENT-NOTES.md) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:07` |

## 2. Methodology

CLI-hygiene address enumeration (`AGENTS.md` §13):

```
$ tshark -r CAP-026-btsnoop_hci.log -T fields -e bthci_evt.bd_addr -e bthci_acl.dst.bd_addr \
    -e bthci_acl.src.bd_addr | tr '\t' '\n' | grep -v '^$' | sort -u
04:00:6e:cf:6e:07   # Buds
e8:d5:2b:7e:ca:81   # phone controller
(7 further addresses — unrelated background BLE noise, not pursued)
```

Video reviewed via dense `ffmpeg -ss <t> -frames:v 1` extraction: 2s resolution across `t=0–20`
(Bluetooth-toggle-to-notification), spot checks every ~10s through the first idle window
(`t=26–86`), 2s resolution across the multitask/reopen transition (`t=84–110`), and spot checks
every ~20s to the video's end (`t=110–176`). No frame shows the buds/case moved.

## 3. `BATT-001` — battery-status-on-reconnect notification, wire-correlated (🟢 FACT for this session)

**Connect sequence, single clean attempt:**

```
$ tshark -r CAP-026-btsnoop_hci.log -Y "bthci_evt.bd_addr==04:00:6e:cf:6e:07 and bthci_evt.code==0x03" \
    -T fields -e frame.number -e frame.time
413  2026-09-12T06:50:00.878011+0200
```

**The two independently-documented battery mechanisms both fire within ~1s of `Connect Complete`:**

```
$ tshark -r CAP-026-btsnoop_hci.log -Y "btrfcomm.dlci==4 and btrfcomm.len>0" -T fields \
    -e frame.number -e frame.time -e data.data | grep "030300"
702  2026-09-12T06:50:01.296213+0200  030300036464ff
```
Decoded (`PROTOCOL.md` §4.3 Option B candidate, `Group 0x03 Code 0x03`): `L=0x64`(100), `R=0x64`(100)
— matches on-screen Left/Right 100% exactly.

```
$ tshark -r CAP-026-btsnoop_hci.log -Y "btrfcomm.dlci==8 and btrfcomm.len>0 and frame.p2p_dir==1" \
    -T fields -e frame.number -e frame.time -e data.data
871  2026-09-12T06:50:01.653740+0200  0e0100210a1f0a03616c6c12180a060864100118010a060864100118020a04085f18032001
```
Decoded (`DECISIONS.md` ADR-014, `Group 0x0e Code 0x01`): outer `field1(len31){ field1="all",
field2(len24){ entry1: value=0x64(100),flag=1,idx=1(Left); entry2: value=0x64(100),flag=1,idx=2(Right);
entry3: value=0x5f(95),idx=3(Case), **no flag field (short/no-flag form)** } field4=1 }`.

**Left=100/Right=100 match the on-screen values exactly at every video frame checked. The Case
value — `0x5f`=95 — does *not* match the on-screen 93% shown consistently at `t=20/40/60/80/110/150/176`
(every frame checked, before and after this connect-time read).** This is a further, independent
occurrence of the same "short/no-flag form carries a stale/cached value" pattern `DECISIONS.md`
ADR-014's addendum first proposed from `CAP-011`/`CAP-009` — this session adds a fourth data point,
not previously reported for a fresh connect-time read specifically (the earlier instances were mid-
or late-session). 🟡 HYPOTHESIS, strengthens (does not newly promote) the existing reading.

**Both mechanisms' data lands on the wire at `06:50:00.9–06:50:01.7`, roughly 12–13s before the
battery-status notification becomes visible in the notification shade on screen (`t=20`, overlay
`06:50:13`)** — consistent with the notification being populated from already-delivered connect-time
data rather than triggering its own separate query, and directly confirming
`TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `BATT-001` claim ("each time you connect... a notification will
appear showing you where battery life stands") at the wire-correlation level for the first time —
previously 🔵 confirmed only at the official-docs/UI-behavior level.

## 4. `OBS-001` — force-close/reopen produces zero wire-visible query (🟢 FACT, clean negative)

The classic ACL connection is never torn down anywhere in this 326.64s log — a single `Connect
Complete` (frame 413) and **zero** `Disconnection Complete` events for the entire session:

```
$ tshark -r CAP-026-btsnoop_hci.log -Y "bthci_evt.code==0x05" -T fields -e frame.number
(0 rows)
```

The video-observed app force-close (recent-apps screen at `t=86`, overlay `06:51:19`) and reopen
(app-drawer tap at `t=96`→"Device details" reloading at `t=98`, overlay `06:51:29`–`06:51:31`)
therefore happen entirely at the app-UI layer, with the underlying RFCOMM connection and every DLCI
staying open throughout. Checked directly for any wire signal in and around this transition:

```
$ tshark -r CAP-026-btsnoop_hci.log -Y '(btrfcomm.dlci==2 or btrfcomm.dlci==4 or btrfcomm.dlci==8) \
    and btrfcomm.len>0 and frame.time>="2026-09-12 06:51:00"' -T fields -e frame.number
(0 rows)
```

**Zero DLCI 0x02/0x04/0x08 data frames anywhere from `06:51:00` (well before the multitask-switch
that ends the first idle window) through the end of the log (`06:54:47`)** — covering the rest of the
`BATT-001` idle wait, the full force-close, the app reopen, and the entire second observation window.
This is a clean negative for `OBS-001`'s own question ("does the app issue any status query
specifically on launch, distinct from the already-FACT reconnect-time `Get ANC state` query?"): **no**,
not in this capture — a force-close-and-reopen that never tears down the underlying classic
connection produces no new query. This extends `CAP-036-FINDINGS.md`'s existing "settings-screen-open
produces zero traffic" negative result to a different, previously-untested trigger (app force-close
then relaunch, not just navigating to a settings screen within an already-open app).

## 5. Cross-checks

- DLCI 0x04's `Get ANC state`/`Notify ANC state` pair fires as expected at connect
  (`06:50:01.36`/`.40`, frames 731/739, `Settable=0x00`=docked matching the buds video-confirmed
  sitting in the open case, `Current=0x20`=Off) — a further, unremarkable ADR-021/022/024 replication.
- No contradiction found with any existing 🟢 FACT.

## 6. Test-ID traceability

- **`BATT-001`** (primary): wire-correlated for the first time — see §3.
- **`OBS-001`** (primary): clean negative — see §4.

## 7. Conclusions & proposed downstream updates

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- Battery data (DLCI 0x04 Option B candidate + DLCI 0x08 Option E) both deliver at connect time,
  ~12–13s before the on-screen notification appears.
- The Case field's connect-time value (95) does not match the on-screen 93% shown throughout the
  rest of the video — a further instance of the short/no-flag-form-may-be-stale pattern.
- App force-close+reopen (connection never dropped) produces zero DLCI 0x02/0x04/0x08 traffic.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15):**
1. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `BATT-001` row — mark as wire-correlated, pointer to this file.
2. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-001` row — record the clean negative, pointer to this
   file.
3. `DECISIONS.md` ADR-014's short/no-flag-form-Case-value hypothesis — this session's own reading
   (95 vs. on-screen 93%) proposed as a fourth supporting data point, not a new promotion.

## 8. Open Questions

- 🔴 Why does the DLCI 0x08 Option E connect-time Case reading (95) not match the on-screen value
  (93%) in this session, when Left/Right both matched exactly? Consistent with, not confirming,
  the existing "short/no-flag form may be stale" hypothesis.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-026-2026-09-12_06-49-53_06-52-49-Group_L/CAP-026-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-026-2026-09-12_06-49-53_06-52-49-Group_L/CAP-026-FINDINGS
