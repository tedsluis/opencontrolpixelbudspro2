# CAP-029: Voice & case button (Group P, `CONV-002`, `CASE-007`, `PAIR-002`; `CASE-008` not exercised)

Standardized, evidence-based extraction from `CAP-029-btsnoop_hci.log` + `CAP-029-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-029` |
| Purpose | Group P — Conversation Detection voice trigger (`CONV-002`), a full factory reset (`CASE-007`) and its re-pair (`PAIR-002`), and the open shorter-press pairing-mode question (`CASE-008`, precondition: Conversation Detection already enabled from `CAP-019`) |
| Date | 2026-09-12 |
| Firmware | ⚪ ASSUMPTION `release_5.203` (carried over) |
| Test device | Pixel 7a, Android 17, official Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Log file | [`CAP-029-btsnoop_hci.log`](./CAP-029-btsnoop_hci.log) — 4,944 packets, 385.64s, `2026-09-12 07:53:51.908–08:00:17.547`. 0/4,944 `cap_len≠len` mismatches — untruncated. |
| Video file | [`CAP-029-recording.mp4`](./CAP-029-recording.mp4) — 251.55s (`ffprobe`), overlay `07:53:53`–`07:58:05` |
| Notes file | [`CAP-029-EVENT-NOTES.md`](./CAP-029-EVENT-NOTES.md) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:07` — same physical unit across the factory reset (re-confirmed by device/log continuity, not a different peer) |

**Decision on item 16 (`CASE-007`):** run — this session performs the full 30s factory-reset hold, per
video/wire evidence below.

## 2. `CONV-002` — media visibly pauses, but zero wire-visible signal accompanies it (🟢 FACT, clean negative)

Video: Spotify plays "Amy Winehouse Best Of" at `t=20` (overlay `07:54:14`, play/pause icon showing
pause-bars, i.e. actively playing); by `t=22` (`07:54:16`) the icon has flipped to a play-triangle
(media now paused) — matching `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s documented Conversation Detection
behavior ("pauses media when you speak").

```
$ tshark -r CAP-029-btsnoop_hci.log -Y 'btrfcomm.dlci==4 and btrfcomm.len>0 and \
    frame.time>="2026-09-12 07:54:00" and frame.time<="2026-09-12 07:54:30"' -T fields \
    -e frame.number -e frame.time -e frame.p2p_dir -e data.data
756  07:54:03.252952  0  08110000                 <- Get ANC state (connect-time)
777  07:54:03.268553  1  0813000401e8e840         <- Notify, Current=0x40 (Adaptive)
(no further Group 0x08 frames anywhere in this window)
```

**No `0x12` Set / `0x13` Notify ANC-state frame occurs anywhere near the pause (checked the full
`07:54:00`–`07:54:30` window, and separately the entire pre-reset session `07:53:51`–`07:54:55`) —
the ANC mode never changes from the connect-time `Adaptive` reading.** Also checked and ruled out:
no AVRCP/AVCTP traffic anywhere in this window (`avctp or avrcp` filter, 0 rows) and no DLCI 0x02
write (no `field5{field4{...}}` settings-write shape appears). The only RFCOMM activity in the exact
pause window (`07:54:14`–`07:54:17`) is routine zero-length DLCI-0x02 flow-control `UIH` frames at
roughly 1-per-second — background keepalive, not a data payload.

**Conclusion:** this capture is a clean negative for a wire-visible Conversation-Detection command.
Either (a) the "switch to Transparency" behavior `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s own note
describes did not actually trigger this time (ANC was already Adaptive, which functionally already
lets outside sound through — a plausible reason no mode switch was needed), or (b) the media-pause
itself is driven by something other than an ANC-mode command this project's known channels would
show (e.g. a phone-side/app-side decision from the Buds' own mic stream, not a documented Bluetooth
control message). Not resolved either way — recorded as a genuine open question, not force-fit into
either explanation.

## 3. `CASE-007` — factory reset reproduces the established pattern (🟢 FACT)

```
$ tshark -r CAP-029-btsnoop_hci.log -Y 'bthci_cmd.opcode==0x0c12' -T fields -e frame.number -e frame.time
2091  07:55:13.694660   <- during the 30s button hold
2497  07:56:12.053071
2860  07:56:19.723113
3316  07:56:34.035326   <- the final one, immediately preceding the fresh SSP handshake
```

Four `Delete Stored Link Key` events across the reset/re-pair sequence (video: button held
`t=68`–`t=98`, overlay `07:55:02`–`07:55:32`), interleaved with reconnect attempts (Connection
Complete at `07:55:44.76`, `07:56:13.37`, `07:56:22.37`, `07:56:34.35` — each followed by a fresh
disconnect except the last). This matches the already-documented factory-reset pattern from
`CAP-001`/`CAP-002`'s own Group P precedent — a third/fourth confirming instance, not a new finding.

## 4. `PAIR-002` — genuine fresh SSP handshake, not a key-reuse reconnect (🟢 FACT)

```
$ tshark -r CAP-029-btsnoop_hci.log -Y 'frame.time>="2026-09-12 07:56:34" and frame.time<="2026-09-12 07:56:35"' \
    -T fields -e frame.number -e frame.time -e frame.p2p_dir -e _ws.col.Info
3336  07:56:34.355867  0  Sent Authentication Requested
3338  07:56:34.356783  1  Rcvd Link Key Request
3339  07:56:34.357427  0  Sent Link Key Request Negative Reply
3341  07:56:34.366532  1  Rcvd IO Capability Request
3342  07:56:34.367167  0  Sent IO Capability Request Reply
3352  07:56:34.386213  1  Rcvd IO Capability Response
3370  07:56:34.652865  1  Rcvd User Confirmation Request
```

`Link Key Request` answered with a **Negative** Reply (no stored key — consistent with the reset
having genuinely erased bonding state), followed by the full IO Capability/User Confirmation SSP
exchange — matching `PROTOCOL.md` §5.1's already-FACT "fresh pairing" path. Video confirms the reset
device reappearing in "Pair new device" (`t=135`, `07:56:09`) and the subsequent first-run onboarding
(SDP browse, RFCOMM/HID/AVDTP connects, permission grants) completing by `t=195` (`07:57:09`).

## 5. `CASE-008` — not attempted this session (⚪ genuinely untested, not force-fit)

The draft Event Timeline's own procedure only describes the 30s hold (`CASE-007`) and the re-pair —
no separate "shorter press" action is described or visible. Full video review confirms this: from
`t=195` (`07:57:09`, onboarding complete) through the video's end (`t=251`, `07:58:05`), the "Device
details" screen stays static, "Active," with no case-button interaction of any kind visible on
camera. The wire log agrees — no further `Delete Stored Link Key`, no ACL bounce of the Buds' classic
connection, anywhere after `07:56:34`. `CASE-008`'s own open question (`PROTOCOL.md` §6) remains
exactly as open as before this capture.

## 6. Correction to the draft: the device is never "forgotten" a second time (🟢 FACT, major correction)

The draft Event Timeline claimed "07:57:56 user forgets the device again in Bluetooth settings." This
did **not** happen:

- **Video**: the very last frame (`t=251`, overlay `07:58:05`) still shows "Device details" with
  "Active" status, a "Forget" button present (meaning the device is still paired) — if a Forget tap
  had occurred, this screen would have navigated away and the device would no longer be listed.
- **Wire**: no `Delete Stored Link Key` command after `07:56:34.035` (the reset's own final one,
  §3), and the classic ACL connection to the Buds (established `07:56:34.348`) has **no**
  `Disconnection Complete` event anywhere in the rest of the log, which itself continues to
  `08:00:17.547` — well past the video's own end.

The one `Disconnection Complete` after `07:56:34` (frame 4482, `07:57:01.589`, reason `0x16`) belongs
to a **different** connection handle (`0x0009`) — a short-lived LE connection that performed routine
post-pairing GATT service discovery (frames 4038–4482) and then closed on its own, not the Buds'
classic connection. This is normal, expected behavior, not a hidden second "forget."

## 7. Test-ID traceability

- **`CONV-002`**: exercised — clean negative, see §2.
- **`CASE-007`**: exercised — confirms established pattern, see §3.
- **`PAIR-002`**: exercised — fresh SSP confirmed, see §4.
- **`CASE-008`**: **not exercised** this session, see §5.

## 8. Conclusions & proposed downstream updates

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- Conversation Detection's media-pause effect is visually confirmed but produces zero wire-visible
  RFCOMM command on any of the three main DLCIs.
- The factory reset and subsequent re-pair both reproduce this project's already-established
  patterns cleanly.
- `CASE-008` was not attempted; the draft's claimed final "forget" did not occur.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15):**
1. `PROTOCOL.md` §6 (Behavior) — add a new 🔴 open question: does Conversation Detection's
   media-pause/ANC-switch ever produce a wire-visible command, or is the on-screen effect driven
   entirely by a mechanism outside this project's already-documented channels?
2. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `CONV-002` row — record the clean negative, pointer to this
   file.
3. `CASE-008`'s own open item — unchanged, still needs a dedicated attempt.

## 9. Open Questions

- 🔴 Why does Conversation Detection's on-screen media-pause produce no wire-visible RFCOMM signal
  on DLCI 0x02/0x04/0x08 — is the effect driven by something this project hasn't identified yet, or
  did the ANC-mode-switch simply not trigger this time (already-Adaptive ANC)?
- 🔴 `CASE-008` (shorter/different case-button press) remains completely untested by this project.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-029-2026-09-12_07-53-53_07-58-05-Group_P/CAP-029-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-029-2026-09-12_07-53-53_07-58-05-Group_P/CAP-029-FINDINGS
