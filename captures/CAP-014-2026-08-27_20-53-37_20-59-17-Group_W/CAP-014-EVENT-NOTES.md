# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group W repeat, GATT handle↔UUID mapping follow-up (`CAP-014`)

**Status:** ✅ **Captured and analyzed — 2026-08-27, 20:53:37–20:59:17.** Folder already carries
the real session date/start-time/end-time (no rename needed). See `CAP-014-FINDINGS.md` for the
full analysis; this file is the raw event timeline and reproducibility metadata it was built from.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group W, repeat/handle-mapping follow-up):**
`GATT-001`'s core discovery goal is already met (`CAP-017`, 18:30 session) via a fresh-GATT-client
path — 137 live discovery frames, full 15-service GATT profile recovered. **Not fully closed:**
that session's wire log was severely ACL-truncated (~15B snaplen), so discovery-response UUID
bytes were never recoverable from the log itself, and no characteristic-level drill-down happened
on screen. This session's originally planned steps were (1) a fixed/longer HCI snoop **snaplen**,
and (2) an on-screen tap into "Accessory Non-Owner Service"/"Unknown Service" — **(2)'s
Accessory-Non-Owner half conflicted with `DECISIONS.md` ADR-008 and was correctly not executed
(§"Procedure" step 4 below and `CAP-014-FINDINGS.md` §2)**.

**Outcome (see `CAP-014-FINDINGS.md` for full detail): (1) succeeded — the snaplen is confirmed
fixed (§0 there) — but the `0x0c0X`/`0x0f2X` handle↔UUID mapping is still 🔴 OPEN, for a newly
identified reason unrelated to snaplen: this session reused an already-bonded phone/cached GATT
client, so Android served cached declarations for that handle range instead of re-transmitting them
on the wire (`CAP-014-FINDINGS.md` §4).** `pm clear com.android.bluetooth` and the Pixel 9a path —
Group W's own actual candidate methods — remain untried and are now the clearly-identified next
step (`CAP-014-FINDINGS.md` §8), not lower-priority alternates.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-014`                     |
|      Group(s)    |          W (repeat — see "Procedure deviation" below: neither Option (a) nor (b) actually used, per `CAP-014-FINDINGS.md` §1) |
|       Date       |                     2026-08-27                     |
| Firmware version |         `release_5.203` (L/R/Case, confirmed on-screen 20:59:07) |
|   Test device    | Pixel 7a (same physical phone as `CAP-001`–`CAP-012`, confirmed via `Read BD ADDR` = `e8:d5:2b:7e:ca:81`, `CAP-014-FINDINGS.md` header) — **not** a fresh/cleared-cache device, see deviation note below |
| Video file       |               `CAP-014-recording.mp4` (340.2s, 20:53:37–20:59:17) |
| Log file         | `CAP-014-btsnoop_hci.log` (947.6s, 4,663 packets, 20:45:57.27–21:01:44.90) — **snaplen confirmed fixed, not truncated** (`capinfos`/`tshark` check, `CAP-014-FINDINGS.md` §0: `frame.cap_len == frame.len` for all 4,663 frames) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            `04:00:6E:…:6E:07`             |

**Snaplen check — confirmed after capturing (`CAP-014-FINDINGS.md` §0):**
```
capinfos CAP-014-btsnoop_hci.log   # Packet size limit: file hdr: (not set); Capture length = 262144
tshark -r CAP-014-btsnoop_hci.log -T fields -e frame.number -e frame.cap_len -e frame.len \
  | awk '$2!=$3 {c++} END{print "mismatches:", c+0}'   # mismatches: 0
```
`CAP-017`'s ~15B-per-frame ACL truncation does **not** reproduce here — this specific blocker is
closed. (It was *not* sufficient on its own to resolve the handle↔UUID mapping — see the
Procedure-deviation note and `CAP-014-FINDINGS.md` §4 for why.)

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group W)

1. **Option (a) — clear the Bluetooth system app's cache directly:**
   `adb shell pm clear com.android.bluetooth` [`GATT-001`]. **Risk:** clears **all** of the
   phone's Bluetooth pairings, not just the Buds' — confirm this is acceptable first. Also clear
   the GATT-client app's own cache if reconnecting with one (a stale on-screen service list can
   look like fresh discovery when it isn't).
   **⚠️ Not actually executed this session** — confirmed via `CAP-014-FINDINGS.md` §1: same
   Pixel 7a as every prior capture, no `pm clear` evidence in the video or nRF Connect debug log.
2. **Option (b) — discover from a phone that has never connected to this device before:** run
   discovery from the Pixel 9a (or any phone that hasn't previously run any app/tool against this
   Buds unit) [`GATT-001`]. Use a fresh install or cleared cache for the GATT-client tool too.
   **⚠️ Not actually executed this session** — the Pixel 7a was used, not the Pixel 9a
   (`CAP-014-FINDINGS.md` §1).
3. Isolate the connect-and-discover sequence as its own window. **Done** — see Event Timeline
   below and `CAP-014-FINDINGS.md` §4's wire-level isolation via `bthci_acl.chandle == 0x0003`.
4. **On-screen drill-down into "Accessory Non-Owner Service" — deliberately NOT executed, per
   `DECISIONS.md` ADR-008.** `PROJECT.md`'s non-goals explicitly place the Accessory Non-Owner
   Service out of scope; this line item should not have been in this session's plan as an active
   instruction. Verified (`CAP-014-FINDINGS.md` §2): no `readCharacteristic`/`write` call targets
   `8e0c0001-1d68-fb92-bf61-48377421680e` anywhere in `CAP-014-nrf-connection.log` or the wire log
   — the service's UUID/name appears only in the unavoidable full-discovery inventory (already an
   accepted exception, `CAP-004-FINDINGS.md` §6), not as an investigated target. **"Unknown
   Service" (`109b862f-…`) was not tapped/read either** (no ADR concern there — simply didn't
   happen), though its characteristic-level UUIDs/properties were visible on screen from passive
   list rendering, not a triggered read — see `CAP-014-FINDINGS.md` §3.

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

**Test-ID annotations (added post-analysis, `AGENTS.md` §13 traceability check):** `GATT-001`
covers the 20:54:43–20:57:44 window (connect → service discovery/listing → CLIENT-tab scroll,
`CAP-014-FINDINGS.md` §4). `PAIR-001` covers 20:55:09–20:55:27 (`device.createBond()` through
`Device bonded`, `CAP-014-FINDINGS.md` §5) — both Test-IDs this Group's row names are clearly
exercised and referenced; no gap to flag.

## Procedure & bonding-mechanism deviation (added post-analysis, style per `CAP-004-EVENT-NOTES.md`/`CAP-010-FINDINGS.md` §1)

This session's on-screen procedure diverges from Group W's own defined method in two ways, both
checked directly against the video and `CAP-014-nrf-connection.log` (a third primary evidence
source for this session, alongside the video and wire log):

1. **Neither Group W Option (a) `pm clear com.android.bluetooth` nor Option (b) (Pixel 9a) was
   used.** nRF Connect itself initiates the classic bond via `device.createBond()` at 20:55:11.345
   (`CAP-014-nrf-connection.log` line 101–102, matches the on-screen debug-log view visible in the
   video at the same timestamp) — not a system-Settings "Forget"-then-re-pair flow, and not on a
   cache-clean device (same Pixel 7a as every prior capture, `e8:d5:2b:7e:ca:81`). No "Forget" tap
   is visible anywhere in the recorded window (20:53:37 onward); whether the Buds were already
   unbonded from this phone before recording started is not determinable from this session's
   artifacts (`CAP-014-FINDINGS.md` §1).
2. **The official Pixel Buds app surfaced twice despite the intent to keep this session's
   Bluetooth activity to nRF Connect only:** a "Pixel Buds Pro 2" pop-up appears and is dismissed
   at 20:54:39 (before the nRF connect attempt even completes), and the official app's own
   "nearby device permissions required" flow appears at 20:58:52–20:58:53, ending on its Firmware
   update screen (20:59:07, used to confirm `release_5.203` for the Log Metadata table above).
3. **Consequence, evidenced in `CAP-014-FINDINGS.md` §4/§5:** because the phone/GATT-client pairing
   was reused rather than fresh, the connection served a cached GATT database for the
   `0x0c0X`/`0x0f2X` handle range instead of re-declaring it live on the wire (§4), and bonding
   used Cross-Transport Key Derivation rather than classic SSP (§5, matching `CAP-004`'s already-
   established "BLE-tool-first → CTKD" pattern). Neither is a failure of this session's snaplen fix
   (§0, confirmed working) — both trace directly to procedure choices, not instrumentation.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 step 6)

- [x] Filter `btatt.opcode == 0x10`/`0x11` (Read By Group Type) and `0x08`/`0x09` (Read By Type) —
      confirm response frames are **not** truncated this time (`frame.cap_len == frame.len`).
      **Done — 0 mismatches across all 4,663 frames (`CAP-014-FINDINGS.md` §0).**
- [x] Recover the `0x0c0X`/`0x0f2X` handle↔UUID mapping from the (now-untruncated) responses.
      **Attempted — still 🔴 OPEN. No declaration response for this handle range exists on the
      wire this session (root cause identified, `CAP-014-FINDINGS.md` §4), not a truncation issue.**
- [x] Confirm/refute the "Accessory Non-Owner Service" and "Unknown Service" (`109b862f-…`)
      identities against their on-screen characteristic reads. **Done — neither was actually read
      this session (Accessory Non-Owner: deliberately, per ADR-008; Unknown Service: simply never
      triggered); both were only passively listed, not confirmed by value (`CAP-014-FINDINGS.md`
      §2/§3).**

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `GATT-001` is clearly referenced above. **Done, see Test-ID
      annotations above.**
- [x] Write `CAP-014-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a). **Done.**
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] ~~Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.~~ **N/A — already named correctly:**
      `CAP-014-2026-08-27_20-53-37_20-59-17-Group_W`.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-014-2026-08-27_20-53-37_20-59-17-Group_W/CAP-014-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-014-2026-08-27_20-53-37_20-59-17-Group_W/CAP-014-EVENT-NOTES
