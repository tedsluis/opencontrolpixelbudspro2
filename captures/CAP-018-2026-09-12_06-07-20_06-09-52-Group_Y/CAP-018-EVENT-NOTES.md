# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group Y, BLE-only isolation of the `0x0044` notification burst (`CAP-018`)

**Status:** ✅ **Captured and analyzed 2026-09-12.** Full video (152.16s, `ffprobe`-confirmed) and
full log (2,309 packets, untruncated, `capinfos`/`cap_len==len` verified) reviewed in full — see
`CAP-018-FINDINGS.md` for the complete write-up. The draft Event Timeline below (as originally
filled in by hand the same day) understated the BT-toggle-to-connect timing and did not know about
the session's second, unrelated BLE connection — both corrected here against direct video-frame and
wire evidence, per this task's own instruction not to trust the draft timeline as-is.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y, added 2026-08-20):** `CAP-016-FINDINGS.md`
§11 found a 73-frame `Handle Value Notification` burst on BLE ATT handle `0x0044` (connection
handle `0x0002`, 23 of the 73 frames containing a recurring `0xfea9` byte-pair marker), confined
to a ~29s window right after the BLE link forms and before the classic link exists. Not yet
isolated from a physical trigger — every capture that shows this burst to date also has a bud
removal/insertion happening nearby in the same session. This Group isolates whether the burst is
triggered by the BLE connection forming alone, independent of any bud/case action.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-018`                     |
|      Group(s)    |                         Y                          |
|       Date       |                     2026-09-12                     |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over; not re-checked on-screen or on-the-wire this session) |
|   Test device    |     Pixel 7a, Android 17, Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Video file       | `CAP-018-recording.mp4` — 152.16s (`ffprobe`), overlay `06:07:20`–`06:09:52`, 1280×720 @30fps |
| Log file         | `CAP-018-btsnoop_hci.log` — 2,309 packets, 409.75s, `2026-09-12 06:06:05.722`–`06:12:55.471`, untruncated (`cap_len==len` for all 2,309 frames) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:...:07` (classic)         |

**Preparation (required before starting):**
- [x] Confirm the Buds are already **bonded** to this phone — Group Y assumes an existing bond, not a
      fresh pairing; do not "Forget"/re-pair as part of this session.
- [x] Confirm Bluetooth is currently **off** (or the BLE link to the Buds has not yet formed) before
      the timed action begins — the whole point of this session is to observe the link forming from a
      cold start, so starting from an already-connected state defeats the test.
- [x] Confirm HCI snoop logging (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` §2) is already **enabled and
      actively running** *before* Bluetooth is (re-)enabled — per §2/§6's "a Bluetooth restart is
      required for the snoop-log setting to take effect" caution. Do not enable logging and Bluetooth
      in the same step; logging must already be live when the timed action starts.

**Isolation check (required — this is the whole point of the session):** confirm and record
explicitly that the buds/case were **not** touched at any point before, during, or for at least
60s after the BLE link formed — the opposite of Group M's procedure, which deliberately triggers
bud/case events.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y)

- [x] **Preparation step:** confirm, in this order, (a) HCI snoop logging is already enabled and
      running, (b) the Buds are bonded but Bluetooth is currently off / the BLE link is not yet
      formed. Note the exact time this starting state was confirmed.
- [x] **Enable Bluetooth and let the phone's BLE link to the already-paired Buds form on its own**
      [`GATT-002`] — do not touch the buds or the case at any point before, during, or for at least
      60s after the link forms. Note the exact time Bluetooth was (re-)enabled or the BLE link began
      forming.
- [x] Keep the observation window open and logging for at least 60s past that point, per the usual
      observation-window discipline (note observation start, any event of interest, observation end).

## Event Timeline (corrected against full video review + full log analysis, 2026-09-12)

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 06:07:20 | Video starts. Buds+case sit untouched on top of the phone. Bluetooth Settings sheet open, "Bluetooth is off." | User | — | Video frame `t=0` (`f_000.png`). |
| ~06:07:23 | Bluetooth toggle tapped ON (finger visible near toggle, "Use Bluetooth" animates on) | User (Hardware) | `GATT-002` | Video frames `t=2`–`t=4` (overlay `06:07:22`→`06:07:24`). Corrected from the draft's `06:08:26` — that value was ~63s too late; the real toggle tap is here. |
| 06:07:23.926–06:07:24.023 | Classic BR/EDR controller comes up (Write Extended Inquiry Response / Write Scan Enable / Write Page Scan Activity) | System (OS/controller) | — | Log frames 328–363 — confirms the video-observed toggle tap to within ~1s of wall-clock (near-zero video/log drift, consistent with this project's other captures). |
| 06:07:24–06:08:25 | Paired-device row shows cached "L:100%, C:95%, R:100%" info, **not yet marked Active** — a client-side cached display, not a live connection (see Findings §2) | System (UI) | — | Video frames `t=6`…`t=65` (`h_006.png`…`g_065.png`) all show the identical non-Active state; log shows only background `LE Extended Advertising Report`s in this window, no `Create Connection`. |
| 06:08:27.773 | Phone sends classic `Create Connection` to the Buds (`04:00:6e:cf:6e:07`) | System (OS, phone-initiated) | `GATT-002` | Log frame 761. |
| 06:08:28.615 | Classic `Connect Complete` (status 0x00) — stored link key reused, no SSP | System | `GATT-002` | Log frame 763. |
| 06:08:28.615–~06:08:30 | RFCOMM multiplexer + DLCIs `0x00/0x02/0x04/0x08/0x0a/0x0c` open; DLCI 0x04 `Get ANC state`(`08110000`)/`Notify`(`Settable=0x00`=docked, `Current=0x20`=Off) fires per ADR-021/022 | System (App/OS auto) | — | Log frames 1017–1041 (Get 1024, Notify 1041, `Settable=0x00` matches the buds visibly sitting in the open case on video). |
| ~06:08:50 | Device row turns purple/"Active. L:100%, R:100% batte…"; bottom notification shows full "Left 100% Case 95% Right 100%" | System (UI) | `GATT-002` | Video frame `t=90` (`j_090.png`). |
| 06:07:20–06:09:52 | **Isolation check: buds/case never touched, entire video** — same static framing/shadow in every reviewed frame (`t=0,2,4,6,20,35,45,55,65,90,120,151`) | — | `GATT-002` | Video review, full duration. |
| 06:09:52 | Video ends (152.16s after start) | System | `GATT-002` | Video frame `t=151` (`j_151.png`, overlay `06:09:51`). |
| 06:10:09.976–06:10:10.008 | **A *second*, unrelated LE connection forms** — `LE Extended Create Connection`/`Enhanced Connection Complete` to address `40:a8:ef:16:bb:35` (chandle `0x0004`) — **not** the Buds' own classic address, and **not video-covered** (20s after the video ends) | Buds/Case or unrelated device (Auto) | `GATT-002` | Log frames 1580/1589 — see Findings §3 for why this is attributed to an unrelated nearby device, not the Buds. |
| 06:10:10.034–06:10:10.409 | Full bidirectional GATT primary-service discovery on chandle `0x0004`: GAP/GATT/Device Information, two "Unknown" 128-bit-UUID services (handles `0x0040–0x0045` and `0x0050–0x0054`), and a standard **Heart Rate** service (`0x180D`) | System ↔ peer | — | Log frames 1607–1690 (full walk); Heart Rate response at frame 1663. |
| 06:10:12.474–06:10:12.9 (approx.) | The `0x0044` `Handle Value Notification` burst (23 frames, chandle `0x0004`) — same shape/marker (`a9fe`) as `CAP-016-FINDINGS.md` §11's original burst | Peer device (Auto) | `GATT-002` | Log frames 1929–2028 (full range); see Findings §3. |

**Note on the draft's original timestamps:** the hand-filled draft (06:07:21 "confirmed off", 06:08:26
toggle, 06:08:27 "link forms", 06:09:46 window end) was off by roughly a minute on the toggle/connect
timing and did not know about the second LE connection at 06:10:10 (after its own stated window end)
— both corrected above from direct video-frame and wire evidence, per this task's instruction to
verify rather than trust the draft.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Y)

- [x] Filter for `btatt.opcode==0x1b and btatt.handle==0x0044`. → 23 frames found, chandle `0x0004`.
- [x] If the burst appears despite no bud/case action anywhere in/near the window: narrows the
      trigger to "BLE link establishment alone" (`PROTOCOL.md` §6). → **Refined further, not simply
      confirmed**: the burst appears, but on a connection (chandle `0x0004`, address
      `40:a8:ef:16:bb:35`) this session's own evidence attributes to an unrelated nearby BLE
      device (GATT discovery finds a standard Heart Rate service, no Fast Pair Service, no
      `0x0c0X` cluster), not the Buds' own classic address (`04:00:6e:cf:6e:07`, chandle `0x0003`,
      zero ATT traffic). See `CAP-018-FINDINGS.md` §3.
- [ ] If it does not appear: points back toward a bud/case physical action as the real trigger —
      N/A, the burst did appear (see above).
- [x] Either outcome closes this open question — don't leave it ambiguous. → Closed with a
      **different** answer than either of the two originally anticipated outcomes: the isolation
      test (no bud/case touch) is satisfied, but the burst itself is now evidenced as *not*
      Buds-originated in this capture — see Findings.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `GATT-002` is clearly referenced above.
- [x] Write `CAP-018-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `GATT-002` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-018-2026-09-12_06-07-20_06-09-52-Group_Y/CAP-018-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-018-2026-09-12_06-07-20_06-09-52-Group_Y/CAP-018-EVENT-NOTES
