# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group W repeat, GATT handle↔UUID mapping follow-up (`CAP-014`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-014-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_W` to the actual session
date/start-time/end-time, e.g. `CAP-014-2026-09-01_11-00-00_11-15-00-Group_W`.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group W, repeat/handle-mapping follow-up):**
`GATT-001`'s core discovery goal is already met (`CAP-017`, 18:30 session) via a fresh-GATT-client
path — 137 live discovery frames, full 15-service GATT profile recovered. **Not fully closed:**
that session's wire log was severely ACL-truncated (~15B snaplen), so discovery-response UUID
bytes were never recoverable from the log itself, and no characteristic-level drill-down happened
on screen. **Primary remaining need for this repeat:**
1. a fixed/longer HCI snoop **snaplen** this time (so ATT response payloads aren't truncated), and
2. an **on-screen tap into "Accessory Non-Owner Service" and "Unknown Service" (`109b862f-…`)** to
   read their characteristics/handles directly.

This is the one step that would resolve the `0x0c0X`/`0x0f2X` handle↔UUID mapping this project
has wanted since `CAP-002`. `pm clear com.android.bluetooth` and the Pixel 9a path remain
untried, now-lower-priority alternates if the snaplen/drill-down approach doesn't fully close it.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-014`                     |
|      Group(s)    |          W (repeat, done properly / handle-mapping follow-up) |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    | TBD (Pixel 7a or Pixel 9a — fresh/cleared GATT-client app) |
| Video file       |               TBD — `CAP-014-recording.mp4`        |
| Log file         | TBD — `CAP-014-btsnoop_hci.log` (**confirm snaplen fixed before capturing** — see `CAP-017-FINDINGS.md` §4b's truncation finding) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Snaplen check (required before capturing):** confirm the HCI snoop capture is configured to
NOT truncate ACL payloads this time — `CAP-017`'s ~15B-per-frame truncation is the specific
failure mode this repeat exists to avoid. Note here exactly how this was confirmed/configured.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group W)

1. **Option (a) — clear the Bluetooth system app's cache directly:**
   `adb shell pm clear com.android.bluetooth` [`GATT-001`]. **Risk:** clears **all** of the
   phone's Bluetooth pairings, not just the Buds' — confirm this is acceptable first. Also clear
   the GATT-client app's own cache if reconnecting with one (a stale on-screen service list can
   look like fresh discovery when it isn't).
2. **Option (b) — discover from a phone that has never connected to this device before:** run
   discovery from the Pixel 9a (or any phone that hasn't previously run any app/tool against this
   Buds unit) [`GATT-001`]. Use a fresh install or cleared cache for the GATT-client tool too.
3. Isolate the connect-and-discover sequence as its own window.
4. **On-screen drill-down (new this session):** tap into "Accessory Non-Owner Service" and
   "Unknown Service" (`109b862f-…`) specifically, to read their characteristics/handles — note
   the exact time of each tap.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 20:53:37 | start of video CAP-014-recording.mp4.
| 20:53:41 | user launches nrf connect app.
| 20:54:07 | in bluetooth settings: user enables bluetooth.
| 20:54:26 | in nrf app: user selects "scan".
| 20:54:30 | user opens pxel buds pro 2 case.
| 20:54:34 | user presses pair button on case. 
| 20:54:39 | in pixel buds pro 2 pop-up: user selects close
| 20:54:43 | in nrf app: user selects connect to Pixel buds pro 2 van Ted
| 20:54:46 | in nrf app: client not connected
| 20:54:48 | in nrf app: user shows log
| 20:54:52 | in nrf app: user selects debug log
| 20:55:09 | in nrf app: user selects bound
| 20:55:24 | user selects pair with Pixel buds pro 2 van Ted
| 20:55:27 | in pixel buds pro pop-up: user selects close
| 20:55:29 | notification appears: pixel buds pro 2 left 100% Case 49% Right 100%
| 20:55:58 - 20:57:44 | user selects client tab and shows UUIDs contents
| 20:57:51 - 20:58:21 | user show debug logging
| 20:58:43 | in bluetooth settings: user selects pixel buds pro 2 device details
| 20:58:52 | user selects "nearby device permissions requiered"
| 20:58:53 | in pixel buds app: user selects continue
| 20:59:07 | user show firmware version
| 20:59:17 | end of video

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 step 6)

- [ ] Filter `btatt.opcode == 0x10`/`0x11` (Read By Group Type) and `0x08`/`0x09` (Read By Type) —
      confirm response frames are **not** truncated this time (`frame.cap_len == frame.len`).
- [ ] Recover the `0x0c0X`/`0x0f2X` handle↔UUID mapping from the (now-untruncated) responses.
- [ ] Confirm/refute the "Accessory Non-Owner Service" and "Unknown Service" (`109b862f-…`)
      identities against their on-screen characteristic reads.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `GATT-001` is clearly referenced above.
- [ ] Write `CAP-014-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-014-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_W/CAP-014-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-014-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_W/CAP-014-EVENT-NOTES
