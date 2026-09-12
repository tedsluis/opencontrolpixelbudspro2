# CAP-048: Dock-state anomaly with an open ACL, repeat of `CAP-037` (Group AD, `OBS-004`)

Standardized, evidence-based extraction from `CAP-048-btsnoop_hci.log` + `CAP-048-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-048` |
| Purpose | Group AD (repeat of `CAP-037`) — repeat the "Get ANC state" reconnect-reliability + dock-state-transition question with continuous physical dock-state video, specifically to resolve `CAP-037-FINDINGS.md` §5's own flagged anomaly (a mid-connection `Settable-toggles` flip with no preceding `Get`) |
| Date | 2026-09-12 |
| Firmware | ⚪ ASSUMPTION `release_5.203` (carried over) |
| Test device | Pixel 7a, Android 17, official Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Log file | [`CAP-048-btsnoop_hci.log`](./CAP-048-btsnoop_hci.log) — 13,301 packets, 784.45s, `2026-09-12 17:41:43.789–17:54:48.236`. 0 truncated frames. `.log.last` present but **not used** — see §0. |
| Video file | [`CAP-048-recording.mp4`](./CAP-048-recording.mp4) — 663.58s (`ffprobe`), overlay `17:41:41`–`17:52:44` |
| Notes file | [`CAP-048-EVENT-NOTES.md`](./CAP-048-EVENT-NOTES.md) — full corrected timeline |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:07` |

## 0. `.log.last` integrity determination (🟢 FACT — leftover pre-session content, not used)

```
$ capinfos CAP-048-btsnoop_hci.log.last | grep -E "Earliest|Latest"
Earliest packet time: 2026-09-12 17:38:51.523844
Latest packet time:   2026-09-12 17:41:42.042942

$ tshark -r CAP-048-btsnoop_hci.log.last -Y "bthci_evt.code==0x05" -T fields -e frame.number -e frame.time -e bthci_evt.reason
938  2026-09-12T17:41:41.888002+0200  0x16   <- last event in .log.last: locally-terminated disconnect

$ tshark -r CAP-048-btsnoop_hci.log -T fields -e frame.number -e frame.time -e _ws.col.Info | head -1
1  2026-09-12T17:41:43.789406+0200  Sent Reset   <- main log's very first frame: a fresh HCI Reset
```

`.log.last` ends with a disconnect at `17:41:41.888`, essentially simultaneous with this video's own
start (`17:41:41`, overlay). The main log begins independently, 1.75s later, with a genuine `Sent
Reset` — a full Bluetooth-stack restart matching the video's own first two documented steps
("Bluetooth is OFF" → "Bluetooth toggled ON"). **`.log.last` is pre-session buffer content (roughly
the 3 minutes before this session's own procedure began), not this session's own rotated data** —
the same kind of determination `CAP-040-FINDINGS.md` §0 and `CAP-042-FINDINGS.md` §0 made for their
own `.log.last` files, adapted here since no neighboring already-tracked capture's content matches
this file (the gap since `CAP-046`'s own log end, `~17:08`, is ~30 minutes — `.log.last` is
untracked pre-session activity, not a duplicate of a known capture). **Only the main log is used as
this session's evidence below.**

## 2. `OBS-004`/ADR-022 — 13/13 reconnects produce a Get/Notify pair, zero misses (🟢 FACT, large replication)

```
$ tshark -r CAP-048-btsnoop_hci.log -Y 'btrfcomm.dlci==4 and btrfcomm.len>0' -T fields \
    -e frame.number -e frame.time -e bthci_acl.chandle -e frame.p2p_dir -e data.data | grep -E "\s08(11|13)"
(16 rows — 15 distinct Get(`08110000`)/Notify(`0813...`) pairs across 13 chandles, plus 1 extra
 Notify-only anomaly at 17:52:25.140, see §4)

$ tshark -r CAP-048-btsnoop_hci.log -Y 'btrfcomm.dlci==4 and btrfcomm.len>0' -T fields -e bthci_acl.chandle | sort -u
0x0004 0x0005 0x0006 0x0007 0x0008 0x0009 0x000a 0x000b 0x000c 0x000e 0x000f 0x0010 0x0012
```

Every one of the 13 chandles that opened DLCI 0x04 with real Message Stream payload produced a
matching `08 11`/`08 13` Get/Notify pair — **zero misses**, a further large-sample replication of
`DECISIONS.md` ADR-022 (joining `CAP-037`'s own 26/26).

## 3. Connection cadence was much slower, and far less regular, than the draft's claimed table (🟢 FACT, major correction)

The draft Event Timeline claimed 25 tightly-alternating (docked/undocked every ~15–20s) reconnects.
The wire shows **13 distinct classic reconnects** (plus 2 same-chandle DLCI-0x04 reopens and one
7-event connection-retry burst on a single chandle, §6) spread unevenly across the session — some
gaps between reconnects exceed a minute (e.g. `17:44:45`→`17:45:44`, `17:45:44`→`17:46:49`). Full
video review (dense frame extraction at every wire-derived reconnect/transition point — see
`CAP-048-EVENT-NOTES.md`'s corrected table) confirms the buds' actual physical dock state changed
far less often than the draft describes: docked continuously from session start (`17:41:41`) through
at least `17:43:51`, undocked from sometime before `17:44:31` through at least `17:47:43`, then a
docked/undocked alternation resumes later in the session. The draft's own "Deviation" annotations
(rows for reconnect #4 and #17) undersell how far the actual session departed from the planned
alternating pattern.

## 4. `CAP-037-FINDINGS.md` §5's anomaly, directly resolved (🟢 FACT — a genuine real-time dock-state change)

```
$ tshark -r CAP-048-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0010 and btrfcomm.dlci==4 and btrfcomm.len>0' \
    -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
11930  17:52:20.169909  0  08110000                  <- Get
11939  17:52:20.875464  1  0813000401e8e880          <- Notify, Settable=0xe8 (undocked), Current=0x80 (Transparency)
12329  17:52:25.140361  1  0813000401e80020          <- Notify ONLY, no preceding Get, Settable=0x00 (docked!), Current=0x20 (Off)
```

`CAP-037-FINDINGS.md` §5 flagged this exact shape (a second Notify on the same chandle, no new Get,
`Settable-toggles` flips) as an open anomaly it could not resolve, since it lacked continuous
dock-state video. **This capture has that video, and it resolves the anomaly directly**: at
`17:52:25` (video `t=644.1`, frame extracted at the exact wire timestamp), the video shows a hand
**actively placing the right earbud into the case** — a genuine, real-time physical docking action
coinciding with the Notify to within the frame-extraction precision used.

```
$ tshark -r CAP-048-btsnoop_hci.log -Y 'bthci_evt.code==0x05 and frame.time>"2026-09-12 17:52:25"' \
    -T fields -e frame.number -e frame.time -e bthci_evt.connection_handle -e bthci_evt.reason
12489  17:52:30.561826  0x0010  0x13
```

5.4s later, the ACL disconnects (Buds-initiated, reason `0x13`). Video at this exact moment
(`17:52:30`) shows **both** earbuds now seated in the case — the left earbud was docked sometime in
the intervening 5.4s, off-frame of the single extracted moment but confirmed by this follow-up
frame. **This is a clean, third confirming instance of `DECISIONS.md` ADR-016** ("ACL disconnects
the instant both buds are re-docked") — not just a coincidence, since the spontaneous Notify at
`17:52:25` demonstrates the *first* bud's docking is wire-visible (via a Notify) while the ACL
connection stays alive, and only the *second* bud's docking triggers the disconnect.

**Conclusion for `CAP-037`'s own open item**: the mid-connection `Settable-toggles` flip with no
preceding Get is **not** a spontaneous/unexplained artifact — it is the Buds correctly reporting a
genuine, real-time dock-state change via an unprompted Notify, consistent with (not in tension with)
`DECISIONS.md` ADR-016.

## 5. A genuine, video-confirmed counter-example to a simple reading of ADR-024 (🔴 reported plainly, not reconciled away)

Two reconnects report `Settable-toggles=0x00` (docked) while the video, checked at essentially the
same wire timestamp, shows the case **visibly empty**:

```
$ tshark -r CAP-048-btsnoop_hci.log -Y 'bthci_acl.chandle==0x0007 and btrfcomm.dlci==4 and btrfcomm.len>0' \
    -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
4231  17:44:45.415291  0  08110000
4252  17:44:45.521247  1  0813000401e80020    <- Settable=0x00 (docked)
```
Video at `t=185` (overlay `17:44:46`, ~1s after this Notify) shows the case open with **both slots
empty** — no bud in sight nearby (`chk_185.png`).

```
$ tshark -r CAP-048-btsnoop_hci.log -Y 'bthci_acl.chandle==0x000a and btrfcomm.dlci==4 and btrfcomm.len>0' \
    -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
7040  17:47:42.117344  0  08110000
7056  17:47:42.136387  1  0813000401e80020    <- Settable=0x00 (docked)
```
Video at `t=358`–`362` (overlay `17:47:39`–`17:47:43`, bracketing this Notify) shows both earbuds
loose, clearly outside the case, throughout (`pre742_358.png`, `mid_362.png`).

**This directly contradicts a simple "Settable-toggles always reflects the buds' current physical
dock state" reading of ADR-024, in these two specific instances.** Per this task's own Guardrails,
this is reported plainly, not explained away. One candidate, *unconfirmed* mechanism worth noting for
a future session to test directly: both stale readings occur on a **fresh classic ACL reconnect**
(a new chandle, full `Create Connection`→`Connect Complete` sequence), whereas the two *correctly*
live-updating readings found in this same session (§3's `17:50:42` transition and §4's `17:52:25`
anomaly) both occur on a **same-chandle DLCI-level reopen**, without a fresh classic connect. 🟡
**HYPOTHESIS, not established by this capture alone**: a fresh classic reconnect's own Get/Notify
might sometimes return a value queried before the Buds' own firmware has settled on the
already-changed physical state, if the reconnect and the physical action happen close together in
time — this is speculation offered as a testable direction, not a confirmed mechanism, per
`AGENTS.md` §13.6's zero-creativity rule. **Does not affect** the `17:41:54`/`17:42:46`/`17:43:50`/
`17:50:16` readings, which are also fresh reconnects and *do* match video correctly — so a fresh
reconnect is not sufficient on its own to explain the staleness; what distinguishes the two stale
instances from the four correct ones is not established.

## 6. New open item — a connection-retry burst, plausibly tied to a closed case (🔴 OPEN QUESTION, new)

```
$ tshark -r CAP-048-btsnoop_hci.log -Y 'bthci_acl.chandle==0x000b' -T fields -e frame.number -e frame.time -e frame.p2p_dir -e _ws.col.Info \
    | grep -E "Connect Complete|Create Connection"
(7 "Rcvd Connect Complete" events, all chandle 0x000b, from 17:48:51.024 to 17:49:46.910, each preceded
 by its own "Sent Create Connection" — no Disconnection Complete event appears between them)
```
Video at `t≈450` (`17:49:11`, mid-burst) shows the app UI stuck on "Connecting…", and the case
appears closed (only the case's outer shell is visible, no loose buds in frame). 🟡 **HYPOTHESIS,
new, not confirmed**: a closed case may impede reliable classic-link establishment, producing this
repeated connect-retry pattern — plausible given the timing/video correlation, but not confirmed
against any documented mechanism, and not decoded further per `AGENTS.md` §13.6.

## 7. Test-ID traceability

- **`OBS-004`**: exercised extensively (13 reconnects with real payload, well beyond the originally
  planned 5) — see §2 (zero-miss ADR-022 replication) and §4/§5 (the two dock-state findings).

## 8. Conclusions & proposed downstream updates

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- 13/13 zero-miss ADR-022 replication.
- The connection cadence and dock-state changes were much slower/less regular than the draft's own
  claimed table — the draft's per-reconnect timeline does not survive verification.
- `CAP-037-FINDINGS.md` §5's anomaly is directly resolved: a genuine, video-confirmed real-time
  docking action, consistent with ADR-016.
- Two genuine, video-confirmed instances where `Settable-toggles` reads "docked" while the case is
  visibly empty — a real counter-example to a simple reading of ADR-024, reported plainly.
- A new, unexplained connection-retry burst, plausibly (not confirmed) linked to a closed case.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15 — these are proposals, not
promotions):**
1. `CAP-037-FINDINGS.md` §5 / `PROTOCOL.md` §6 — mark this specific class of anomaly as resolved
   (real-time dock-state change, ADR-016-consistent), citing this capture.
2. `PROTOCOL.md` §4.1 / `DECISIONS.md` ADR-024 — add this capture's two counter-example readings as
   a new, explicitly-flagged caveat: `Settable-toggles` is not always a live read on a fresh
   reconnect; two documented instances lag the buds' actual dock state. **Not proposed as a reversal
   of ADR-024** (four other readings in this same session are correct) — proposed as a documented,
   unreconciled exception needing further investigation, per the maintainer's own review.
3. `PROTOCOL.md` §6 — new open item: the `17:48:51`–`17:49:47` connection-retry burst and its
   possible link to a closed case.
4. `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `OBS-004` row — Evidence column pointer to this file.

## 9. Open Questions

- 🔴 What distinguishes the two stale `Settable-toggles` readings (§5) from the four correct ones in
  the same session? A fresh-reconnect-vs-same-chandle-reopen hypothesis is offered but not confirmed.
- 🔴 What causes the `17:48:51`–`17:49:47` connection-retry burst (§6)? Plausibly a closed case, not
  confirmed.
- 🔴 Is there a systematic minimum settling time between a physical dock-state change and a reliable
  `Settable-toggles` read? Not established by this capture.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-048-2026-09-12_17-41-41_17-52-44-Group_AD/CAP-048-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-048-2026-09-12_17-41-41_17-52-44-Group_AD/CAP-048-FINDINGS
