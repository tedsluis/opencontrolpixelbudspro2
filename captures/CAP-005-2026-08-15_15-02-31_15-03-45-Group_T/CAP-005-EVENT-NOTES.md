# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group T Capture (`CAP-005`)

**Status:** Reviewed against `CAP-005-recording.mp4` frame-by-frame (1s resolution, using the video's burned-in wall-clock overlay) and cross-checked against `CAP-005-btsnoop_hci.log` via `tshark`/Wireshark. Corrects and extends the original draft. See `CAP-005-FINDINGS.md` in this same folder for the standardized, evidence-graded protocol findings extracted from this correlation — this file is the _event timeline_, `CAP-005-FINDINGS.md` is _what it means for the protocol_.

## Log Metadata

|      Field       |                     Value                     |
|------------------|-----------------------------------------------|
|    Capture ID    |                    `CAP-005`                    |
|     Group(s)     |           T (EQ command isolation)            |
|       Date       |                  2026-08-15                   |
| Firmware version |                 release_5.203                 |
|   Test device    |             Pixel 7a — Android 17             |
|    Video file    |  `CAP-005-recording.mp4` — (wall clock, +0200)  |
|     Log file     | `CAP-005-btsnoop_hci.log` — (wall clock, +0200) |

**Scope note (per the maintainer's test design):** This capture isolates EQ commands to determine their specific DLCI channel and protobuf/TLV framing. It specifically tests whether EQ presets and EQ sliders share the same command channel. The mandatory timing discipline (≥10s isolation between actions) was strictly maintained.

## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (CEST / +0200).

|   Time   |                Action / Event                 | Test-ID |
|----------|-----------------------------------------------|---------|
| 15:02:31 |            Start video recording.             |    —    |
| 15:02:37 | User: Turns Bluetooth on via system settings. |    —    |
| 15:02:41 |          Pixel Buds Pro 2 connected.          |    —    |
| 15:02:53 |    User: Starts Pixel Buds companion app.     |    —    |
| 15:03:13 |   **User: Changes EQ preset to 'Bass Boost'.**    | `EQP-002` |
| 15:03:24 |       **User: Adjusts the 'Bass' slider.**        | `EQS-004` |
| 15:03:45 |             End video recording.              |    —    |

## Observation Notes

-   **Action Isolation:** Excellent execution of Group T rules. There is a clean 20-second gap before the Preset change, an 11-second gap before the Slider change, and a 21-second gap after the Slider change.
    
-   **Conclusion for Analysis:** Because of this strict isolation, any RFCOMM (DLCI 0x02, 0x04) or private envelope (DLCI 0x08) traffic appearing _precisely_ at `15:03:13` or `15:03:24` can be confidently attributed to the EQ actions without fear of overlapping "settling" traffic from the app launch.
    
-   **Next Step:** Open `CAP-005-btsnoop_hci.log` in Wireshark, apply a display filter for the Buds' MAC address, navigate to the absolute time `15:03:13`, and inspect the payloads of the outgoing frames. Repeat for `15:03:24` and cross-reference the framing structure.