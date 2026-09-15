# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AG (repeat), `CAP-040` with a genuine trigger (`CAP-050`)

**Status:** 🟡 **Video-only pass complete.** `.log`/`CAP-050-FINDINGS.md` analysis is a separate,
future, out-of-scope task (see `ai-sessions/0019_CAPTURE_PROMPT_2026_09_14.md`). Every row below is
sourced from `CAP-050-recording.mp4` alone — the `Wire evidence / Notes` column is intentionally left
`TBD` throughout.

**Purpose (repeat of `CAP-040`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AG):** `CAP-040-FINDINGS.md`
§3's own correlation attempt for DLCI 0x08's 7 unmapped zero-length `[Group][Code][00 00]`-shaped
frames failed because the session used the app's own in-app "Connect"/"Disconnect" buttons as the
bracketing trigger — but `CAP-040-FINDINGS.md` §1 separately found those buttons produce **zero
wire-visible signal** at all, invalidating the intended correlation (DLCI 0x08 opened exactly once,
so all 7 codes had only one sample each). This repeat is required to use a trigger confirmed to
actually reopen DLCI 0x08 (OS-level Bluetooth toggle, or physical case/bud docking/undocking).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-050`                     |
|      Group(s)    |                   AG (repeat)                      |
|       Date       |                    2026-09-14                      |
| Firmware version |                  `release_5.203`                   |
|   Test device    |  Pixel 7a, official Pixel Buds Companion App + GMS enabled. The app was open on the Device details screen for the entire recording (video-confirmed continuously, `00:00:00`–`00:12:29`) — no deviation observed, except one brief interval (`00:11:41`–`00:11:50`, wall-clock `21:12:44`–`21:12:53`) where the phone's screen showed the lock screen / home screen instead of the app (the app was reopened to Device details afterward). |
| Video file       |    `CAP-050-recording.mp4`, duration `748.80s` (`ffprobe`), 1280×720, 30fps. Burned-in wall-clock overlay: start `14 sep 2026 21:01:01`, end `14 sep 2026 21:13:30`. |
| Log file         |     TBD — pending separate log-analysis session — `CAP-050-btsnoop_hci.log` |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            TBD             |

**Trigger check (required — this is the whole point of the repeat):** the video shows **two**
distinct trigger types used across the session:
1. **OS-level Bluetooth toggle** — once, at the very start (`00:00:04`–`00:00:10`, wall-clock
   `21:01:07`–`21:01:13`): the phone's own Settings → Bluetooth screen is on screen, "Use Bluetooth"
   is OFF, and the maintainer taps it ON, after which the Buds show "Connecting…" then connect. This
   establishes the session's baseline connection.
2. **Physical case/bud docking and undocking** — for the entire remainder of the session
   (`00:00:10`–`00:12:29`): the maintainer repeatedly places one or both earbuds into the case and/or
   removes them, visibly triggering the official app's "Disconnect"/"Connect" button label to flip
   between the two states. The app's own in-app Connect/Disconnect buttons are **not** tapped anywhere
   in this video — every state change traces to a physical dock/undock action, consistent with this
   repeat's purpose. Whether each of these physical actions actually reopened DLCI 0x08 on the wire is
   explicitly deferred to the separate, out-of-scope log-analysis session; this session can only
   report the physical/UI-observed action and the resulting in-app Connect/Disconnect label.

## Procedure — planned (skeleton, unchanged below) vs. what the video actually shows

**Planned procedure (from the CAP-040/Group AG skeleton):** use a trigger confirmed to reopen DLCI
0x08 (OS toggle or physical dock/undock, never the in-app buttons), and repeat it ~10–15 times, each
cycle bracketing one plausibly-relevant value (e.g. dock state: Left/Right/both/neither docked).

**What the video actually shows:** the maintainer's own account (order deviations occurred; the
order in which Left/Right were placed/removed, and relative to the case lid, sometimes varied from a
clean single-variable protocol) is **confirmed** by the footage, and the deviation is larger than a
simple re-ordering — the actual session is **far more continuous and higher-frequency** than the
skeleton's "10–15 discrete, isolated reconnects" framing suggests. Across the full ~12.5-minute
recording, the maintainer continuously and repeatedly picks up, docks, undocks, and re-docks one or
both earbuds — at a pace of roughly one physical action every 5–15 seconds for most of the session —
producing many more than 15 distinguishable Connect/Disconnect transitions (a conservative count from
the in-app label alone is well over 20 across the full session; see the Event Timeline below for the
subset that could be confidently timestamped and described from the review pass actually performed).
The case lid is never closed in this video — it stays open throughout every dock/undock action
observed. Left and Right are frequently handled in close succession (often within 1–5 seconds of each
other), meaning several individual cycles do not cleanly isolate a single-bud change the way the
skeleton's one-variable-at-a-time framing intends — see the explicit open-question rows below.

**Review method used for this pass:** the full 748.8s video was reviewed via (a) a uniform baseline
of frames extracted every 5s across the entire duration (150 frames), and (b) automated scene-change
detection (`ffmpeg select='gt(scene,0.02)'`) that flagged 99 additional moments of visually distinct
change (hand entering/leaving frame, case content changing, on-screen Connect/Disconnect label
flipping), reviewed individually. This combination provides full-duration coverage of every
discrete/distinguishable action without frame-by-frame (per-1/30s) playback. Given the density
described above, exact sub-second ordering of every single micro-action (e.g. which finger touched
which bud first within a single ~2-second handling burst) could not be resolved at this review
density for every cycle — rows below where this matters are marked as an explicit open question
rather than guessed, per the guardrails in `ai-sessions/0019_CAPTURE_PROMPT_2026_09_14.md`.

## Event Timeline

Times are given as `video-relative (wall-clock)`. "Dock state" describes what is visibly seated in
the case's two slots at/immediately around the moment of the in-app label change; "Screen state"
is the app's own Connect/Disconnect button label, read literally from the screen.

| Time (video / wall-clock) | Action | Initiator | Test-ID | Wire evidence / Notes |
|---|---|---|---|---|
| 21:01:01 | Session start. Phone shows Settings → Bluetooth, toggle OFF ("Bluetooth is off"). Both earbuds visible outside the case (Left resting to the case's left, Right resting to the case's right on the table), case lid open and empty. | User | `PRIV-001` | TBD |
| 21:01:07 | Maintainer taps "Use Bluetooth" ON. | User | `PRIV-001` | TBD |
| 21:01:13 | Buds show "Connecting…" then connect; app auto-navigates to Device details. On-screen: Left 37%, Case 75%, Right 45%, "Active", ANC row present, "Disconnect" button (connected). Both earbuds still outside the case (baseline state). | Hardware (BT toggle) | `PRIV-001` | TBD |
| 21:01:29 – 21:01:32 | Right earbud placed into the case's right slot (visible in frame; case lid stays open). Left earbud remains outside. Screen stays "Disconnect" (still connected); Right's battery icon shows charging. | User (Hardware) | `PRIV-001` | TBD |
| 21:01:38 – 21:01:44 | Left earbud also placed into the case (both slots now occupied, lid still open). By `21:01:44` the screen shows **"Connect"** (disconnected). Dock state at this disconnect: **both docked** (both earbuds visually in the case). | User (Hardware) | `PRIV-001` | TBD |
| 21:01:49 – 21:01:58 | Right earbud removed from the case (picked up by hand); Left remains seated. By `21:01:58` the screen shows **"Disconnect"** again (reconnected), with a fresh notification banner "Left 38% Case 75% Right 45%". **Dock state at this reconnect: Left docked, Right out.** | User (Hardware) | `PRIV-001` | TBD |
| 21:02:21 – 21:02:37 | Continued handling of the case/buds (hand repeatedly touching case and buds); screen remains "Disconnect" (connected) throughout this window — no full both-docked state is sustained long enough to produce a disconnect here. | User (Hardware) | `PRIV-001` | TBD |
| 21:02:37 – 21:02:39 | Screen shows **"Connect"** (disconnected). At this moment the case shows one earbud (Left, charging-icon green) apparently seated and the other (Right) not shown as charging — **dock state at this disconnect is an open question**: the icon read and the visible case content were not fully consistent in this review pass (hand partially obscures the case in the nearest frames), so which earbud(s) were actually seated cannot be confidently stated. Flagged for the log-analysis session rather than guessed. | User (Hardware) | `PRIV-001` | TBD — **open question: dock state ambiguous** |
| 21:02:58 – 21:03:00 | Screen shows **"Disconnect"** again (reconnected), notification banner "Left 39% Case 74% Right 46%". Both earbuds visible outside the case in the nearest surrounding frames. **Dock state at this reconnect: appears to be neither docked (both out)**, though the precise moment of the underlying physical undock that produced this specific reconnect was not isolated with certainty — flagged as lower-confidence. | User (Hardware) | `PRIV-001` | TBD — lower-confidence dock-state read |
| 21:04:16 – 21:04:20 | Both earbuds again placed into the case (lid stays open); by `21:04:20` area screen shows "Connect" (disconnected). **Dock state: both docked.** (Exact transition second not pinned closer than a ~4s window at this review density.) | User (Hardware) | `PRIV-001` | TBD |
| 21:04:48 – 21:04:52 | Right earbud removed again; screen returns to "Disconnect" (reconnected). **Dock state at reconnect: Left docked, Right out**. | User (Hardware) | `PRIV-001` | TBD |
| 21:05:01 – 21:12:44 | Maintainer continues a long series of similar dock/undock actions — earbuds repeatedly placed into and picked out of the case's Left/Right slots, individually and in close succession, with the in-app Connect/Disconnect label flipping in step on multiple further occasions (case battery percentages climb steadily from Case 74%→71% and Left 39%→63%, Right 46%→65% over this window, consistent with real charging while docked between cycles). **This entire window contains many more than the skeleton's planned ~10–15 cycles; individual sub-cycles within it were not each separately timestamped/attributed to a specific Left/Right/both/neither dock state at this review's frame density — flagged collectively as an open question requiring either denser video re-review or direct wire-timestamp correlation in the future log-analysis session**, rather than fabricating a per-cycle breakdown this pass cannot support with confidence. | User (Hardware) | `PRIV-001` | TBD — **open question: individual cycles in this window not separately attributed** |
| 21:11:41 – 21:12:44... 21:11:41 - 21:12:53 | Phone screen goes to the lock screen / home screen briefly (app backgrounded or screen timeout) while case/bud handling continues off the Device-details screen; app is reopened to Device details by `21:12:53`, showing "Connect" (disconnected), Left 63%, Case 71%, Right 65%. | User | `PRIV-001` | TBD |
| 21:12:59 – 21:13:09 | Right earbud removed from the case; screen returns to "Disconnect" (reconnected). **Dock state at reconnect: Left docked, Right out** (same pattern as the two earlier cycles | User (Hardware) | `PRIV-001` | TBD |
| 21:13:30 | Video ends. Screen shows "Connect" (disconnected), Left 63%, Case 71%, Right 65%; no earbuds visible outside the case (both apparently docked). | — | `PRIV-001` | TBD |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AG)

- [ ] For each unmapped code (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`, `0e 04`), find
      its `Sent` Get frame and the immediately-following `Rcvd` response(s) on the same Group. —
      requires the separate, out-of-scope log-analysis session.
- [ ] Decode any numeric fields and check whether any field tracks the bracketed value across all
      reconnects. — requires the separate, out-of-scope log-analysis session.
- [ ] A field that stays constant is not a match; only report a semantic reading for a field that
      visibly tracks the known value repeat after repeat. — requires the separate, out-of-scope
      log-analysis session.

## Open questions for the future log-analysis session

- The session contains far more Connect/Disconnect transitions (well over 20, by the in-app label
  alone) than the ~10–15 the skeleton's procedure planned for — the log-analysis session should count
  actual DLCI 0x08 (re)opens against this video's own timestamp overlay directly, rather than assume
  the count matches the plan.
- Several cycles (flagged explicitly above) have an ambiguous or unconfirmed dock state at the
  moment of reconnect — cross-reference the wire timestamp against the video overlay time directly
  for those specific windows rather than relying on this file's necessarily coarser description.
- The `~00:04:00`–`00:11:41` window was not broken into individually-attributed sub-cycles at this
  review's density (uniform 5s baseline + 99 scene-change-triggered frames) — if per-cycle DLCI 0x08
  correlation requires finer attribution than "many dock/undock actions occurred in this window with
  Case/Left/Right batteries climbing from 74/39/46% to 71/63/65%", a denser (sub-second) re-review of
  this specific window against the video overlay is recommended before drawing conclusions from it.
- Confirm from the wire log whether the OS Bluetooth-toggle-triggered initial connect
  (`00:00:04`–`00:00:12`) and the physical dock/undock-triggered reconnects later in the session
  produce the same DLCI 0x08 (re)open signature, or a distinguishable one — this video pass cannot
  answer that.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — `PRIV-001` is referenced throughout the Event Timeline above.
- [ ] Write `CAP-050-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a). — separate, future, out-of-scope
      session.
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path. —
      deliberately **not** done in this video-only session; stays `planned`.
- [ ] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PRIV-001` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`. — separate, future, out-of-scope session.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time — done (`CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG`,
      via `git mv`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-EVENT-NOTES
