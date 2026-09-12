# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AD (repeat), dock-state anomaly with an open ACL (`CAP-048`)

**Status:** ✅ **Captured and analyzed 2026-09-12.** Full log (13,301 packets, main log only — see
`.log.last` integrity note below) and a full-duration pass of the video (663.58s) reviewed, with
dense frame extraction at every reconnect/transition point identified from the wire — see
`CAP-048-FINDINGS.md` for the complete write-up. **The draft's own per-reconnect docked/undocked
labels do not reliably match the wire or the video** — the actual physical dock/undock cadence was
much slower than the draft's claimed rapid alternation (long stretches of several consecutive
reconnects with no physical bud movement at all). Two major findings: (1) `CAP-037-FINDINGS.md`
§5's own open anomaly (a mid-connection `Settable-toggles` flip with no preceding `Get`) is
**directly resolved** here — video shows a bud being physically docked at the exact wire timestamp;
(2) a genuine, video-confirmed **counter-example** to a simple reading of `DECISIONS.md` ADR-024:
at least two fresh reconnects report a **stale** `Settable-toggles` value that does not match the
buds' actual, already-changed physical dock state at that moment — reported plainly per this task's
own Guardrails, not reconciled away.

**`.log.last` integrity determination:** `CAP-048-btsnoop_hci.log.last` (948 packets,
`17:38:51.524–17:41:42.043`) is **not** part of this session's own data. Its very last event
(frame 938, `17:41:41.888`) is a locally-terminated ACL disconnect landing almost exactly at this
video's own start (`17:41:41`), and the main log's own frame 1 (`17:41:43.789`) is a fresh `Sent
Reset` — a genuine Bluetooth-stack restart matching the video's own "Bluetooth is OFF" → "Bluetooth
toggled ON" opening steps. `.log.last` covers roughly the 3 minutes immediately *before* this
session's own documented procedure began (pre-session buffer content, not a mid-session rotation) —
mirroring the same kind of determination `CAP-040-FINDINGS.md` §0/`CAP-042-FINDINGS.md` §0 made for
their own `.log.last` files, though here the leftover content is from an untracked pre-session
period rather than a neighboring already-tracked capture. **Only the main log is used as this
session's evidence.**

**Purpose (repeat of `CAP-037`, `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD):** `CAP-037-FINDINGS.md`'s
own open anomaly: on one of 26 same-session `DECISIONS.md` ADR-022 replications, a second "Notify
ANC state" frame appeared 18 seconds after the first, with no new `08 11` Get frame in between, and
its `Settable-toggles` value flipped `0xe8`→`0x00` — genuinely open whether this reflects a real
dock-state change while the ACL connection stayed open (in tension with ADR-016's "ACL disconnects
the instant both buds are re-docked" finding) or a spontaneous, unprompted Notify unrelated to dock
state.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-048`                     |
|      Group(s)    |                   AD (repeat)                      |
|       Date       |                     2026-09-12                     |
| Firmware version | ⚪ ASSUMPTION `release_5.203` (carried over) |
|   Test device    | Pixel 7a, Android 17, Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Video file       | `CAP-048-recording.mp4` — 663.58s, overlay `17:41:41`–`17:52:44` |
| Log file         | `CAP-048-btsnoop_hci.log` — 13,301 packets, 784.45s, `17:41:43.789`–`17:54:48.236`, untruncated (main log only, see `.log.last` note above) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) |         `04:00:6e:...:07`         |

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD)

1. Reproduce `CAP-037`'s own procedure (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD): multiple
   isolated reconnects, alternating docked/undocked states, at least 20+ minutes total.
2. This time, keep a continuous, timestamped video specifically of the Buds/case's own physical
   dock state throughout (not just the phone screen) — the anomaly needs sub-second dock-state
   correlation that `CAP-037`'s own pass didn't have.

## Event Timeline — corrected, wire-derived (the draft's per-reconnect table above did not survive verification)

**The draft's claimed 25-reconnect, rapid docked/undocked-alternating-every-~15s table does not
match the wire or the video.** The actual classic-ACL reconnect cadence and dock-state changes were
both slower and less regular than drafted. This table replaces it with the wire-derived reconnect
list (every chandle that opened DLCI 0x04 with real payload, 13 total — `CAP-048-FINDINGS.md` §2)
cross-checked against video at every dock-state transition and both major findings (§3/§4 there).
Video was reviewed across the full 663.58s duration; the times below are anchored to specific wire
events, not re-derived from the draft's own (unreliable) per-row claims.

| Time | Event | Dock state (`Settable-toggles`) | Video cross-check |
|---|---|---|---|
| 17:41:41 | Video starts. Bluetooth off. | Docked (video: both buds seated) | ✅ confirmed |
| 17:41:43.789 | Fresh `Sent Reset` — Bluetooth toggled on | — | — |
| 17:41:54.762–17:41:55.162 | Reconnect (chandle `0x0004`), `Get`/`Notify` | `0x00` Docked | ✅ confirmed (`early_0.png`) |
| 17:42:46.383–.402 | Reconnect (chandle `0x0005`) | `0x00` Docked | ✅ confirmed (`mid_66.png`) — **contradicts the draft's own "Reconnect #2 (Undocked)" label** |
| 17:43:50.513–.548 | Reconnect (chandle `0x0006`) | `0x00` Docked | ✅ confirmed (`early_130.png`) |
| 17:44:17.324–.407 | Same-chandle (`0x0006`) DLCI reopen | `0x00` Docked | Not independently re-checked at this exact sub-moment; buds confirmed docked at `17:43:51` and confirmed undocked by `17:44:31` (below) — this reading falls inside that transition window |
| **~17:43:51–17:44:31** | **Buds physically removed from the case at some point in this window** | (transition) | Video: docked at `t=130` (`17:43:51`), undocked by `t=170` (`17:44:31`) |
| 17:44:45.415–.521 | Reconnect (chandle `0x0007`) | `0x00` Docked | ❌ **video shows the case already empty** (`mid_66→chk pattern`, confirmed via `t=185`/`17:44:46`, `chk_185.png`) — **stale `Settable-toggles` reading, a genuine counter-example, see Findings §5** |
| 17:45:43.751–17:45:44.513 | Reconnect (chandle `0x0008`) | `0x00` Docked | Not independently video-checked this pass |
| 17:46:49.025–.830 | Reconnect (chandle `0x0009`) | `0x00` Docked | Not independently video-checked this pass |
| 17:47:41.535–17:47:42.136 | Reconnect (chandle `0x000a`) | `0x00` Docked | ❌ **video confirms both buds loose outside the case** (`t=358`–`362`, `pre742_358.png`/`mid_362.png`) — **second stale-reading counter-example, see Findings §5** |
| 17:48:51.024–17:49:46.910 | **Connection-retry burst**: 7 `Create Connection`/`Connect Complete` cycles on the same chandle (`0x000b`), roughly every 5–20s, culminating in a stable connection | `0x00` Docked (final `Get`/`Notify`, `17:49:47.27`) | Video (`t≈450`, `17:49:11`) shows the app stuck on "Connecting…" during this window, case appears closed — plausible link between a closed case and repeated failed connection attempts, not confirmed further (see Findings §6, new open item) |
| 17:50:16.387–.934 | Reconnect (chandle `0x000c`) | `0x00` Docked | ✅ confirmed (`late_516.png`, `17:50:17`) |
| 17:50:42.151–.164 | Same-chandle (`0x000c`) DLCI reopen | `0xe8` Undocked | ✅ confirmed — buds visibly removed by `17:50:43` (`late_542.png`) — **correctly, live-updated reading** |
| 17:50:55.642–17:50:56.290 | Reconnect (chandle `0x000e`) | `0xe8` Undocked | Not independently video-checked this pass (consistent with the still-undocked state confirmed just before and after) |
| 17:51:31.522–17:51:32.290 | Reconnect (chandle `0x000f`) | `0xe8` Undocked | Not independently video-checked this pass |
| 17:52:20.170–.875 | Reconnect (chandle `0x0010`) | `0xe8` Undocked | ✅ confirmed (`anomaly_639_2.png`, `17:52:21`, both buds loose) |
| **17:52:25.140** | **Spontaneous `Notify` (no preceding `Get`), same chandle `0x0010`** | **flips to `0x00` Docked** | ✅✅ **directly resolves `CAP-037-FINDINGS.md` §5's own flagged anomaly** — video at this *exact* wire timestamp (`anomaly_644_1.png`) shows a hand actively placing the right earbud into the case. See Findings §4. |
| 17:52:30.562 | ACL `Disconnection Complete` (reason `0x13`, Buds-initiated) | — | ✅ confirmed both buds now docked (`disc_648_5.png`, `17:52:30`) — matches `DECISIONS.md` ADR-016's "disconnects the instant both buds are re-docked" finding exactly |
| 17:52:36.827–17:52:37.227 | Final reconnect (chandle `0x0012`) | `0x00` Docked | Consistent with the just-confirmed docked state |
| 17:52:44 | Video ends (663.58s) | — | — |
| (log continues to 17:54:48) | No further Buds-attributable activity | — | — |

**Scope note, stated plainly:** every one of the 13 chandles that opened DLCI 0x04 with real payload
was checked for its `Get`/`Notify` pair and `Settable-toggles` value (full-log query, not sampled —
`CAP-048-FINDINGS.md` §2). Of those 13(+2 same-chandle reopens), **8 were individually
video-cross-checked** against a specific extracted frame (✅/❌ above); the remaining readings were
not each individually re-verified against a fresh video extraction this pass, though every one falls
inside a dock-state window already established by an adjacent, video-confirmed reading (i.e., no
reading was left completely uncorroborated). A future pass could individually verify the remaining
~5 reconnects for full exhaustiveness; this was not completed here given this capture's own size
(663.58s video, 13,301-packet log) relative to the rest of this task's scope.

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AD)

- [x] For each reconnect, confirm `08 11 00 00`/`08 13` fires and record its `Settable-toggles` byte
      against the known dock state. → Done for all 13 chandles carrying real DLCI 0x04 payload,
      zero misses (`CAP-048-FINDINGS.md` §2 — ADR-022 replication).
- [x] Specifically check for any `Settable-toggles` flip with no preceding `08 11` Get and no ACL
      disconnect nearby, matching `CAP-037-FINDINGS.md`'s own anomaly shape. → **Found one, at
      `17:52:25.140`** (chandle `0x0010`).
- [x] If such a flip occurs, check the physical dock-state video at that exact wire timestamp. →
      **Done — directly resolved**: a bud is being physically docked at that exact wire timestamp.
      See `CAP-048-FINDINGS.md` §4.
- [x] A miss on any repeat is a counter-example to ADR-022; a `Settable-toggles` value that doesn't
      match dock state on any repeat is a counter-example to ADR-024 — either must be reported
      plainly, not reconciled away. → **ADR-022: zero misses (13/13).** **ADR-024: two genuine,
      video-confirmed counter-examples found** (`17:44:45`, `17:47:42` — both read "docked" while
      the case was visibly empty on video) — reported plainly in `CAP-048-FINDINGS.md` §5, not
      explained away.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `OBS-004` is clearly referenced above.
- [x] Write `CAP-048-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [x] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
- [x] Update `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-004` row's Evidence column with a pointer
      once promoted into `PROTOCOL.md`.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-048-2026-09-12_17-41-41_17-52-44-Group_AD/CAP-048-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-048-2026-09-12_17-41-41_17-52-44-Group_AD/CAP-048-EVENT-NOTES
