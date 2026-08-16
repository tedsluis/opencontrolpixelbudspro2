# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group S Capture (`CAP-010`)

**Status:** Reviewed against `CAP-010-recording.mp4` frame-by-frame (1s resolution, using the video's
burned-in wall-clock overlay) and cross-checked against `CAP-010-btsnoop_hci.log` via `tshark`/Wireshark.
Corrects and extends the original draft. See `CAP-010-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `CAP-010-FINDINGS.md` is *what it means for the protocol*.

## Log Metadata

|      Field       |                    Value                     |
|------------------|----------------------------------------------|
|    Capture ID    |                   `CAP-010`                   |
|      Group(s)    | S (`GFPS-001` — |
|       Date       |                  2026-08-16                  |
| Firmware version | release_5.203 |
|   Test device    | Pixel 7a, Android 17 — ) |
| Video file       | `CAP-010-recording.mp4`  |
| Log file         | `CAP-010-btsnoop_hci.log` — . |

## Event Timeline

| Time | Action / Event | Initiator | Test-ID | Evidence in `CAP-010-btsnoop_hci.log` |
|---|---|---|---|---|
| 11:42:31 | Start video recording.  |  | — | — |
| 11:42:37 | User turns on bluetooth.  |  | — | — |
| 11:42:51 | User selects `Pair new device`. |  | — | — |
| 11:42:57 | User opens Pixel buds pro 2 case. |  | — | — |
| 11:42:58 | User press pair button on pixel buds pro 2 case. |  | — | — |
| 11:43:02 | Pixel Buds Pro 2 van Ted Appaers in the list of available devices. |  | — | — |
| 11.43:28 | User selects Pixel Buds pro 2 in the list of available devices. |  | — | — |
| 11:43:29 | popup request to Pair with Pixel Buds pro 2 van Ted. |  | — | — |
| 11:43:34 | User selects 'pair'  |  | — | — |
| 11:43:41 | new popup request to save Pixel Buds Pro 2 device to connect more quickly. |  | — | — |
| 11:43:46 | notification appears: Pixel Buds Pro 2: Lef 100%, Case 47% Right 100% |  | — | — |
| 11:43:55 |User selects save (device). |  | — | — |
| 11:43:56 | popup request to 'set up' device. |  | — | — |
| 11:44:07 | User selects 'set up' |  | — | — |
| 11:44:24 | user select continue. |  | — | — |
| 11:44:33 | user selects Allow the app Pixel Buds to access Pixel Buds Pro 2 van Ted. |  | — | — |
| 11:45:01 | End video recording | — | — | — |
