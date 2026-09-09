# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AA (repeat, 2nd attempt), SDP UUID branch isolation (`CAP-044`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `TBD` below after recording,
per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update), and
`PROJECT_RULES.md` rule 11/14 (reproducibility metadata). Once reviewed, rename this folder from
the placeholder `CAP-044-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AA` to the actual session
date/start-time/end-time, e.g. `CAP-044-2026-09-15_08-30-00_08-40-00-Group_AA`.

**Purpose (repeat of `CAP-033`/`SDP-001`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA):**
`CAP-033-FINDINGS.md` §8 found the "default internal rfcomm socket" UUID still absent from the SDP
browse (an interesting negative), but the session's own procedure deviated — "Forget" preceded
"Force-stop" by ~10s (the reverse of the intended order) and step 3 (an app-open baseline
comparison) was never executed at all — capping the result at 🟡 HYPOTHESIS rather than a clean
FACT either way. This repeat corrects both deviations in one session.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-044`                     |
|      Group(s)    |               AA (repeat, 2nd attempt)             |
|       Date       |                        TBD                         |
| Firmware version |                        TBD                         |
|   Test device    |       TBD (Pixel 7a, Android version)              |
| Video file       |    TBD — screenshot/video confirming the Force-stop-before-Forget ordering |
| Log file         |             TBD — `CAP-044-btsnoop_hci.log`        |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Isolation check (required — this is the whole point of the repeat):** confirm and record the
exact wall-clock order of Force-stop (must be first) vs. Forget (must be second), and confirm
step 3 (the app-open SDP browse) was actually executed this time — the two procedure gaps that
capped `CAP-033` at 🟡 HYPOTHESIS.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA, `SDP-001`)

1. **Force-stop** the official Pixel Buds Companion App **first**, confirmed via Settings → Apps
   (screenshot/video timestamp), *before* touching the Buds' pairing at all — the reverse of
   `CAP-033`'s own accidental ordering.
2. With the app still force-stopped, perform the per-device "Forget" (not a broader Bluetooth
   reset) on the Buds, then re-pair via system Bluetooth settings only (no companion app
   involvement) and capture the SDP Service Search Attribute Response during that re-pair.
3. **Execute step 3 this time** — after the SDP browse above is captured, *open* the official
   companion app (still logging) and capture a second SDP browse triggered by the app itself, to
   compare against step 2's system-only browse.
4. Extract via the raw log path (not `btsnooz.py`), per the same technical-debt note as `CAP-043`.

## Event Timeline

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| TBD | Official app force-stopped, confirmed via Settings → Apps (screenshot/video) | User | `SDP-001` | Conn. state: TBD |
| TBD | "Forget" performed via system Bluetooth settings (per-device, not a broader reset) | User (Hardware) | `SDP-001` | TBD |
| TBD | Re-pair via system Bluetooth settings' "Pair new device" flow; SDP browse captured | User (Hardware) | `SDP-001` | TBD |
| TBD | Observation window ≥60s after bonding completes, app still not opened | — | `SDP-001` | TBD |
| TBD | Official companion app opened, second SDP browse captured (app-triggered) | User (App) | `SDP-001` | TBD |
| TBD | Logging stopped, extracted via raw path | — | — | TBD |
| — | (opportunistic only, separate session) `SDP-002` — before/after a firmware OTA update | — | `SDP-002` | Not attempted this session unless an update is pending |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA)

- [ ] Pre-filter by address, then filter to `btsdp` per §13's CLI-hygiene rule:
      `tshark -r CAP-044-btsnoop_hci.log -Y "bluetooth.addr == <buds MAC> and btsdp" -T fields -e frame.number -e frame.time_relative -e btsdp.data_element.value.uuid_128 -e btsdp.protocol.channel`
- [ ] Does the "default internal rfcomm socket" UUID (`3a046f6d-...`, either byte order) appear in
      *either* SDP browse this time (system-only, or app-triggered)?
- [ ] Does the "MAESTRO APP"/"GSND CONTROL"/"GSND AUDIO" naming (`CAP-033-FINDINGS.md` §3)
      reproduce identically in the system-only browse as well as the app-triggered one?
- [ ] Record the result plainly either way — a second consecutive "pigweed"-only negative narrows
      the explanation toward a static-analysis-only dead-code question (out of this Group's scope,
      per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA's own "explicitly out of scope" note), not
      toward a further repeat.

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `SDP-001` (and `SDP-002` if attempted) are clearly referenced
      above.
- [ ] Write `CAP-044-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `SDP-001` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [ ] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-044-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AA/CAP-044-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-044-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AA/CAP-044-EVENT-NOTES
