# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group U Capture (`CAP-007`)

**Status:** Reviewed against `CAP-007-recording.mp4` frame-by-frame (using the video's own
burned-in wall-clock overlay, sampled at up to 1s resolution around every event of interest) and
cross-checked against `CAP-007-btsnoop_hci.log` via `tshark`. 

## Log Metadata

|      Field       |                       Value                        |
|------------------|----------------------------------------------------|
|    Capture ID    |                      `CAP-007`                     |
|      Group(s)    | U  |
|       Date       |                     2026-08-18                     |
| Firmware version | `release_5.203` |
|   Test device    | Pixel 7a. Android's own Bluetooth   |
| Video file       | `CAP-007-recording.mp4` —  |
| Log file         | `CAP-007-btsnoop_hci.log` —  |



## Event Timeline

Timestamps are the video's own on-screen wall-clock overlay (±1s where video is the only source);


| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-007-btsnoop_hci.log` |
|----------|---|---|---|---|
| 06:31:31 | Start video recording. Screen shows the system **Bluetooth** settings sheet, "Bluetooth is off", `Use Bluetooth` toggled off. | - | - | - |
| 06:33:58 | End video recording. | — | — | — |
