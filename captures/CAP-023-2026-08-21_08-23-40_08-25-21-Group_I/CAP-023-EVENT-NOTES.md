# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group I, Firmware & device info (`CAP-023`)

**Status:** ✅ Captured and analyzed. Video reviewed via tiled contact sheets (full pass) and
targeted single-frame extraction; wire log cross-checked for both DLCI 0x02 settings-writes and
raw ASCII string search (for the firmware-version resolution goal).

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group I):** attribute the wire commands for the
firmware/device-info screen, and — the session's **primary** goal — resolve `PROTOCOL.md` §0.1's
open "UI-baseline vs. wire-baseline firmware version" question by recording the on-screen firmware
version string next to the wire capture's own timestamp.

**Location note:** the relevant screen is **Device details → More settings → Firmware update**
(not a top-level "Firmware up to date" item as the skeleton assumed) — it shows an "Automatic
updates" toggle, a "Device firmware status" line ("Up to date · Last checked N minutes ago"), and
"Device firmware version" per component (Left earbud / Right earbud / Case).

**Gap, flagged per `AGENTS.md` §13's traceability check:** `FW-003` (serial numbers) and `FW-004`
(connection status) were **not exercised this session** — the video never visits the "About"
sub-page (seen only as an unopened row in "More settings": *"About — Earbuds status, serial
numbers, and m[ore]"*). Only `FW-001` and `FW-002` were captured.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-023`                     |
|      Group(s)    |                         I                          |
|       Date       |                     2026-08-21                     |
| Firmware version | **`release_5.203`** — confirmed on-screen this session, see §Decode |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Video file       |            `CAP-023-recording.mp4` (101.1s, 08:23:40–08:25:21 local time) |
| Log file         |             `CAP-023-btsnoop_hci.log` (08:23:00.709–08:27:44.263) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-019`–`CAP-022` |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group I)

31. **Tap the 'Firmware up to date' check** (manual) [`FW-001`].
32. **Open 'More settings'** to view firmware version per component [`FW-002`].
33. **View serial numbers per component** [`FW-003`] — **not exercised this session** (gap).
34. **View connection status** [`FW-004`] — **not exercised this session** (gap).

## Event Timeline

| Time (local) | Action | Initiator | Test-ID | Wire evidence |
|---|---|---|---|---|
| 08:23:40 | Video start | — | — | Video first frame |
| 08:23:4x–08:24:1x | Bluetooth reconnect; navigate Device details → More settings → Firmware update | User (App) | — | Connection handshake burst (DLCI 0x02/0x04/0x08); DLCI 0x08 frame 849 (`08:23:46.038`) carries the ASCII string `release_5.203` — see §Decode |
| **~08:24:17** (t=37s) | **Firmware update screen fully loaded** — "Left earbud: release_5.203", "Right earbud: release_5.203", "Case: release_5.203" all shown | — | `FW-002` | No new wire traffic accompanies this — the displayed strings match frame 849's connection-time value exactly (see §Decode); the screen reads from an already-cached value, not a live query |
| **~08:24:23** (t=43s) | **Tap 'Up to date' status row** (manual re-check) | User (App) | `FW-001` | **Zero RFCOMM traffic** in a ±15s window around this tap (checked explicitly, not assumed) — see §Decode |
| ~08:24:30 (t=50s) | Incidental tap on 'Automatic updates' toggle (off then back on) | User (App, incidental) | — | Zero RFCOMM traffic — not part of any Group I Test-ID |
| 08:24:5x–08:25:1x | Navigate back to More settings, then back into Firmware update again | User (App) | — | No RFCOMM traffic |
| 08:25:21 | Video end | — | — | Video last frame |

## Decode — resolving `PROTOCOL.md` §0.1's wire-baseline-vs-UI-baseline question

On-screen (video, t=37s/08:24:17): **"Device firmware version" → Left earbud: `release_5.203`,
Right earbud: `release_5.203`, Case: `release_5.203`.**

On the wire, in the **same session**, during the connection-time handshake, **before** the screen
was even opened:

```
$ tshark -r CAP-023-btsnoop_hci.log -Y "btrfcomm.dlci==0x08 and btrfcomm.len>0" \
    -T fields -e frame.number -e frame.time -e data.data | grep -i 72656c656173655f
849  2026-08-21T08:23:46.037600+0200  0302003f08061001220d72656c656173655f352e3230332a0030e6...
874  2026-08-21T08:23:46.415964+0200  020400170801180020002a0d72656c656173655f352e3230333803
878  2026-08-21T08:23:46.440024+0200  020400410801180020012a0d72656c656173655f352e3230333804...
```

```python
bytes.fromhex("72656c656173655f352e323033").decode()
# -> 'release_5.203'
```

**This directly resolves `PROTOCOL.md` §0.1/§6's open question, same session, both channels:** the
on-screen firmware version string the app itself displays (`"release_5.203"`) is **byte-for-byte
identical** to the string already documented on DLCI 0x08's private envelope (Group `0x03` Code
`0x02`, per `CAP-004-FINDINGS.md` §5a's Task 2 and `CAP-002-FINDINGS.md` §2a) — not merely a
plausible reading anymore, but a same-session, on-screen-confirmed match. `"Revision 6"` (DLCI
0x04's official Message Stream Device Information field) does **not** appear anywhere on this
screen or in this session's log at all — the app's own UI does not surface that string as "the
firmware version" the user sees.

**Not fully resolved:** whether `"Revision 6"` still means something else (a protocol/schema
revision number, per `CAP-002-FINDINGS.md` §3's original reading) remains open — this capture
confirms what the *app* calls "firmware version" for the *user-facing* display, not what
`"Revision 6"` itself represents.

**Firmware check is cached, not live-queried:** tapping "Up to date" (`FW-001`) and opening the
Firmware update screen (`FW-002`) produced **zero** RFCOMM traffic on any DLCI, checked explicitly
across the whole ±15s window around each action (`tshark -Y "btrfcomm.len>0 and frame.time_epoch>X
and frame.time_epoch<Y"` returned no rows). Per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q's
three-way-outcome convention (applied here even though this isn't a Group Q item, since the same
"confirmed local action, no wire traffic" shape applies): this is a **positive finding**, not a
gap — the "Device firmware status: Up to date" and per-component version strings are read from
already-cached connection-time data (the DLCI 0x08 handshake above), not fetched fresh on tap.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5)

- [x] Identify which DLCI/channel carries any status query triggered by "More settings". → None —
      confirmed cached, not queried.
- [x] Cross-check the on-screen firmware version string against every on-the-wire version-like
      string in `PROTOCOL.md` §0.1. → Matches `"release_5.203"` exactly; does not match
      `"Revision 6"`, `"cape2_sm"`, or `"500m"`–`"500p"`.
- [x] Compare structure against `PROTOCOL.md` §2's envelope hypotheses. → N/A, no new write frame
      produced this session; the matching string was already-known DLCI 0x08 content.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise — `FW-001`/`FW-002`
      referenced above; `FW-003`/`FW-004` explicitly flagged as **not exercised** (gap).
- [x] Write `CAP-023-FINDINGS.md` per `PROJECT_RULES.md` §2.
- [x] Update `PROTOCOL.md` §0.1 with the firmware-version resolution (non-destructively, per
      `PROJECT_RULES.md` rule 9a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index.
- [x] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-023-2026-08-21_08-23-40_08-25-21-Group_I/CAP-023-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/#/captures/CAP-023-2026-08-21_08-23-40_08-25-21-Group_I/CAP-023-EVENT-NOTES
