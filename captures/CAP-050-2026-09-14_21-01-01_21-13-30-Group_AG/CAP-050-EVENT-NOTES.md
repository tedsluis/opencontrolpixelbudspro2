# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AG (repeat), `CAP-040` with a genuine trigger (`CAP-050`)

**Status:** 🟢 **Video pass + full log analysis complete** (`ai-sessions/0021_CAPTURE_PROMPT_2026_09_15.md`).
See `CAP-050-FINDINGS.md` for the full wire-level analysis, hex+command evidence, and conclusions.
This file's Event Timeline below has been updated in place (per `PROJECT_RULES.md` §3 rule 9a) to
record what the wire log/further video re-check actually resolved — the two windows this file
originally flagged as ambiguous are now resolved (see the rows below and `CAP-050-FINDINGS.md` §4/§6).

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
| Log file         |     `CAP-050-btsnoop_hci.log` (871.57s, 20,060 packets, 0/20,060 `cap_len≠len` mismatches — untruncated, raw path; see `CAP-050-FINDINGS.md` §0) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |            `04:00:6e:cf:6e:07`             |

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
| 21:01:01 | Session start. Phone shows Settings → Bluetooth, toggle OFF ("Bluetooth is off"). Both earbuds visible outside the case (Left resting to the case's left, Right resting to the case's right on the table), case lid open and empty. | User | `PRIV-001` | see `CAP-050-FINDINGS.md` §1 (session start, no wire action yet) |
| 21:01:07 | Maintainer taps "Use Bluetooth" ON. | User | `PRIV-001` | Wire: BT toggle triggers the session's first `Connection Complete` (chandle `0x0002`, frame 445, `21:01:10.725`) shortly after |
| 21:01:13 | Buds show "Connecting…" then connect; app auto-navigates to Device details. On-screen: Left 37%, Case 75%, Right 45%, "Active", ANC row present, "Disconnect" button (connected). Both earbuds still outside the case (baseline state). | Hardware (BT toggle) | `PRIV-001` | Wire: private-envelope burst opens on DLCI `0x08` (chandle `0x0002`), frame 1283, `21:01:16.977` — `CAP-050-FINDINGS.md` §3 |
| 21:01:29 – 21:01:32 | Right earbud placed into the case's right slot (visible in frame; case lid stays open). Left earbud remains outside. Screen stays "Disconnect" (still connected); Right's battery icon shows charging. | User (Hardware) | `PRIV-001` | not individually wire-correlated this pass (within the first `0x0002` connection, still open) |
| 21:01:38 – 21:01:44 | Left earbud also placed into the case (both slots now occupied, lid still open). By `21:01:44` the screen shows **"Connect"** (disconnected). Dock state at this disconnect: **both docked** (both earbuds visually in the case). | User (Hardware) | `PRIV-001` | Wire: disconnect frame 2051, `21:01:41.280` (reason `0x13`) — `CAP-050-FINDINGS.md` §1 |
| 21:01:49 – 21:01:58 | Right earbud removed from the case (picked up by hand); Left remains seated. By `21:01:58` the screen shows **"Disconnect"** again (reconnected), with a fresh notification banner "Left 38% Case 75% Right 45%". **Dock state at this reconnect: Left docked, Right out.** | User (Hardware) | `PRIV-001` | Wire: `Connection Complete` chandle `0x0001`, frame 3219, `21:01:59.937`; `Settable-toggles=0x00` (docked) at `21:02:04.012` — see `CAP-050-FINDINGS.md` §5 note (single-docked vs. both-docked distinction not separately confirmed for this specific reconnect) |
| 21:02:21 – 21:02:37 | Continued handling of the case/buds (hand repeatedly touching case and buds); screen remains "Disconnect" (connected) throughout this window — no full both-docked state is sustained long enough to produce a disconnect here. | User (Hardware) | `PRIV-001` | no distinct wire event isolated for this handling-only window |
| 21:02:37 – 21:02:39 | Screen shows **"Connect"** (disconnected). **Resolved (`CAP-050-FINDINGS.md` §6):** dense `ffmpeg` re-extraction at `t=96/97/98s` shows both earbuds clearly, normally seated in the case's two slots (no wrong-orientation) — **dock state at this disconnect: both docked.** | User (Hardware) | `PRIV-001` | Wire: chandle `0x0001` disconnect, frame 4948, `21:02:37.728` (reason `0x13`); preceding Notify (frame 4141, `21:02:04.012`) read `Settable-toggles=0x00` (docked), consistent with the video read here — `CAP-050-FINDINGS.md` §5/§6 |
| 21:02:58 – 21:03:00 | Screen shows **"Disconnect"** again (reconnected), notification banner "Left 39% Case 74% Right 46%". **Resolved (`CAP-050-FINDINGS.md` §4/§6):** dense `ffmpeg` re-extraction at `t=117/119s` shows **Left earbud docked, Right earbud held in hand (out)** — not "neither docked" as originally guessed. | User (Hardware) | `PRIV-001` | Wire: chandle `0x0002` (2nd instance) Connect Complete frame 5145, `21:03:00.091`; the following Notify (frame 5684, `21:03:04.173`) reads `Settable-toggles=0x00` (docked) — a genuine mismatch against the video-confirmed Left-only dock state, consistent with `CAP-048-FINDINGS.md` §5's "stale pre-transition reading" pattern, not a contradiction of `DECISIONS.md` ADR-024 — `CAP-050-FINDINGS.md` §4/§5 |
| 21:04:16 – 21:04:20 | Both earbuds again placed into the case (lid stays open); by `21:04:20` area screen shows "Connect" (disconnected). **Dock state: both docked.** (Exact transition second not pinned closer than a ~4s window at this review density.) | User (Hardware) | `PRIV-001` | not individually wire-correlated this pass — falls between chandle `0x0001` (ends `21:02:37.728`) and chandle `0x0002`'s 2nd instance (starts `21:03:00.091`); consistent with a `~21:02:37`–`21:04:03` handling span, not separately isolated |
| 21:04:48 – 21:04:52 | Right earbud removed again; screen returns to "Disconnect" (reconnected). **Dock state at reconnect: Left docked, Right out**. | User (Hardware) | `PRIV-001` | Wire: `Connection Complete` chandle `0x0004`, frame 6659, `21:04:03.465`; `Settable-toggles=0x00` at `21:04:07.485` — `CAP-050-FINDINGS.md` §5 (Left-only-docked configuration confirmed by video at `t=183s`, §4) |
| 21:05:01 – 21:12:44 | Maintainer continues a long series of similar dock/undock actions — earbuds repeatedly placed into and picked out of the case's Left/Right slots, individually and in close succession, with the in-app Connect/Disconnect label flipping in step on multiple further occasions (case battery percentages climb steadily from Case 74%→71% and Left 39%→63%, Right 46%→65% over this window, consistent with real charging while docked between cycles). **Partially resolved (`CAP-050-FINDINGS.md` §1/§5):** the wire log shows 10 further `Connection Complete` events inside this window (chandles `0x0006`,`0x0007`,`0x0008`,`0x0009`,`0x000a`,`0x000b`/`0x000c`,`0x000d`,`0x000c`-2nd — far fewer than "many more than 15" implied by the in-app label alone), each with a confirmed `Settable-toggles` reading (`CAP-050-FINDINGS.md` §5's table) — only the `21:08:19` reconnect (Left docked/Right out, video-confirmed, `CAP-050-FINDINGS.md` §4) was individually video-cross-checked this pass; the remaining reconnects' exact Left/Right/both sub-state was not separately re-verified against video beyond the already-tabulated `Settable-toggles` binary read. A 20-frame supplementary scan of this whole window (`t=260`–`660s`) found no wrong-orientation docking event (`CAP-050-FINDINGS.md` §6). | User (Hardware) | `PRIV-001` | Wire: 10 `Connection Complete` events at `21:05:38.815`/`21:06:31.275`/`21:08:18.776`/`21:09:31.775`/`21:10:05.065`/`21:10:30.036`/`21:10:39.643`/`21:10:59.584`/`21:11:26.330`/`21:12:29.017` (chandles per `CAP-050-FINDINGS.md` §1); dock-state bracket per reconnect in `CAP-050-FINDINGS.md` §5 |
| 21:11:41 – 21:12:44... 21:11:41 - 21:12:53 | Phone screen goes to the lock screen / home screen briefly (app backgrounded or screen timeout) while case/bud handling continues off the Device-details screen; app is reopened to Device details by `21:12:53`, showing "Connect" (disconnected), Left 63%, Case 71%, Right 65%. | User | `PRIV-001` | falls within the large 21:05:01–21:12:44 window above; not separately wire-isolated |
| 21:12:59 – 21:13:09 | Right earbud removed from the case; screen returns to "Disconnect" (reconnected). **Dock state at reconnect: Left docked, Right out** (same pattern as the two earlier cycles | User (Hardware) | `PRIV-001` | Wire: `Connection Complete` chandle `0x000f`, frame 19073, `21:12:57.847`; `Settable-toggles=0x00` at `21:13:01.776` — `CAP-050-FINDINGS.md` §5 |
| 21:13:30 | Video ends. Screen shows "Connect" (disconnected), Left 63%, Case 71%, Right 65%; no earbuds visible outside the case (both apparently docked). | — | `PRIV-001` | Wire: last `Connection Complete` chandle `0x0011`, frame 19868, `21:13:20.208` — private-envelope DLCI not confirmed for this final reconnect, `CAP-050-FINDINGS.md` §2 |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AG)

- [x] For each unmapped code (`05 0c`, `04 02`, `04 04`, `04 11`, `04 13`, `04 15`, `0e 04`), find
      its `Sent` Get frame and the immediately-following `Rcvd` response(s) on the same Group. —
      done, `CAP-050-FINDINGS.md` §3.
- [x] Decode any numeric fields and check whether any field tracks the bracketed value across all
      reconnects. — done, `CAP-050-FINDINGS.md` §3/§4.
- [x] A field that stays constant is not a match; only report a semantic reading for a field that
      visibly tracks the known value repeat after repeat. — applied; 5 of 7 codes' neighbors are
      constant (not a match), 2 remain genuinely inconclusive (not promoted to a semantic reading) —
      `CAP-050-FINDINGS.md` §3/§4/§8.

## Open questions — resolved or carried forward into `CAP-050-FINDINGS.md`

- **Resolved:** the session's actual wire-confirmed count of private-envelope channel (re)opens is
  15 confirmed + 1 inconclusive (not "well over 20") — `CAP-050-FINDINGS.md` §1/§2 explains the gap
  (the in-app Connect/Disconnect label flips more often than the classic link actually tears down and
  rebuilds).
- **Resolved:** both originally-ambiguous dock-state windows (`~21:02:37–39`, `~21:02:58–00`) are now
  resolved from wire+video correlation — see the Event Timeline rows above and `CAP-050-FINDINGS.md`
  §4/§6.
- **Partially resolved:** the `~00:04:00`–`00:11:41` window's 10 individual reconnects each now have a
  wire-confirmed `Settable-toggles` dock-state bracket (`CAP-050-FINDINGS.md` §5); only one of them
  (`21:08:19`) was individually video-cross-checked at finer Left/Right granularity this pass.
- **Answered:** yes, the OS-toggle-triggered initial connect and the later physical dock/undock
  reconnects produce the same private-envelope (re)open signature (all 7 flagged codes fire the same
  way in both) — `CAP-050-FINDINGS.md` §3.
- **New, carried into `PROTOCOL.md` §6 (proposed, awaiting maintainer sign-off):** `Group 0x04 Code
  0x05`/`Code 0x16` fluctuate near dock-state changes but do not reproduce consistently for the same
  physical dock configuration across different reconnects — `CAP-050-FINDINGS.md` §4/§8/§9.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — `PRIV-001` is referenced throughout the Event Timeline above.
- [x] Write `CAP-050-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a). — done.
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path. —
      done.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `PRIV-001` row's Evidence column with a pointer. —
      done.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time — done (`CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG`,
      via `git mv`).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-050-2026-09-14_21-01-01_21-13-30-Group_AG/CAP-050-EVENT-NOTES
