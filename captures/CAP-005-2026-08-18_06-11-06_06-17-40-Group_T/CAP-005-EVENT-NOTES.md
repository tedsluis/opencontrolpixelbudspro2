# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group T Capture (`CAP-005`)

**Status:** Reviewed against `CAP-005-recoding.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `CAP-005-btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `CAP-005-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `CAP-005-FINDINGS.md` is *what it means for the protocol*.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-005`                     |
|      Group(s)    |               T (EQ command isolation)             |
|       Date       |                     2026-08-18                     |
| Firmware version |                   release_5.203                    |
|   Test device    |    Pixel 7a, Android 17 (Official Pixel Buds Companion App v1.0.955078536) |
| Video file       | `CAP-005-recoding.mp4` (sic — filename as recorded on disk)  (wall clock, +0200) |
| Log file         | `CAP-005-btsnoop_hci.log` —  (wall clock, +0200) |

**Scope note (per the maintainer's test design):** 

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s, 1fps sampling), CEST/+0200;
`CAP-005-btsnoop_hci.log` uses the same wall clock, so times below are directly comparable to log
frame timestamps without offset correction.

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-005-btsnoop_hci.log` |
|----------|---|---|---|---|
| 06:11:06 | Start video recording. Screen shows the system **Bluetooth** settings sheet, **"Bluetooth is off"**, `Use Bluetooth` toggled off | - | - | - |
| 06:17:40 | End video recording | — | — | — |

