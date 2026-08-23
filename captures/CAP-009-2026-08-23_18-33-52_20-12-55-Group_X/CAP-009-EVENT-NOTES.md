# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group X, Battery-level discrepancy bracket (`CAP-009`)

**Status:** Reviewed against the maintainer's manual video timestamps and cross-checked against
`CAP-009-btsnoop_hci.log` via `tshark`. See `CAP-009-FINDINGS.md` in this same folder for the
standardized, evidence-graded protocol findings extracted from this correlation — this file is
the *event timeline*, `CAP-009-FINDINGS.md` is *what it means for the protocol*.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X):** `CAP-001-FINDINGS.md` §3 found
`AT+CIND?`'s `battchg=3` (≈60%) and `AT+BIEV=2,100` (100%) disagreeing at the same moment —
unresolved whether either indicator actually tracks a real battery-level change over time.
**Can be combined with `CAP-008`'s (Group V) session** if a phone call happens to occur naturally
within this window — no need to force it, just note it if it happens. No phone call occurred in
this session (no `AT+` traffic outside the connection-setup handshake and periodic `AT+BIEV`
pushes — see `CAP-009-FINDINGS.md` §2).

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-009`                     |
|      Group(s)    |                         X                          |
|       Date       |                     2026-08-23                     |
| Firmware version | not queried this session (⚪ ASSUMPTION `release_5.203`, same physical device as `CAP-023`/`CAP-025`) |
|   Test device    | Pixel 7a, Android 17 (⚪ ASSUMPTION, consistent with `CAP-023`/`CAP-025`), official app not confirmed this session |
| Video files       | `CAP-009-recording1.mp4` (18:33:52–20:00:27), `CAP-009-recording2.mp4` (20:01:17–20:12:57) — camera restarted after ~90 min; not reviewed frame-by-frame by the AI agent, see note below |
| Log file         | `CAP-009-btsnoop_hci.log` — 2026-08-23 18:33:47.20–20:15:00.73 (wall clock, +0200), 30,234 packets |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-001`/`CAP-006`/`CAP-016`/`CAP-019`–`CAP-025` |

**Note on video:** this is a multi-hour passive-observation session — a continuous screen
recording is not expected/required the way it is for a short, action-isolated capture. The
maintainer manually noted the on-screen battery percentage at each visible change (timeline
below); the AI agent did not independently re-review the two `.mp4` files (see
`CAP-009-FINDINGS.md` §0 methodology note) — analysis here cross-checks the maintainer's
timestamps against the wire log rather than re-deriving them from video.

**Methodology / CLI hygiene (`AGENTS.md` §13):** `bluetooth.addr` never populates in this log for
ACL data frames (verified: `tshark -r CAP-009-btsnoop_hci.log -Y "bluetooth.addr" -c 1` returns
zero matches anywhere in the file — Wireshark's BTSnoop-HCI dissector only populates this field on
frames that carry an explicit `BD_ADDR`, e.g. `bthci_evt` name/connection events, not ACL data).
The whole log contains exactly **three** `HCI_Connection_Complete` events (`bthci_evt.code==0x03`),
all for the Buds' MAC `04:00:6e:cf:6e:07`, mapping to connection handles `0x0002` (18:34:01.51,
frame 674), `0x0001` (20:00:01.92, frame 28798, a ~0.9s connection immediately torn down), and
`0x0002` again (20:02:27.21, frame 29074):

```
$ tshark -r CAP-009-btsnoop_hci.log -Y "bthci_evt.code==0x03" \
    -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.connection_handle -e bthci_evt.status
674    2026-08-23T18:34:01.513362000+0200  04:00:6e:cf:6e:07  0x0002  0x00
28798  2026-08-23T20:00:01.924735000+0200  04:00:6e:cf:6e:07  0x0001  0x00
29074  2026-08-23T20:02:27.209998000+0200  04:00:6e:cf:6e:07  0x0002  0x00

$ tshark -r CAP-009-btsnoop_hci.log -Y "btrfcomm" -T fields -e bthci_acl.chandle | sort -u
0x0002
```

Since no other device ever completes a classic ACL connection in this entire log, and all RFCOMM
traffic sits exclusively on handle `0x0002`, filtering by `bthci_acl.chandle==0x0002` (for the two
periods it's live: 18:34:01–20:00:01 and 20:02:27–end) is used below as the address-filter
equivalent required by `AGENTS.md` §13, in place of the non-functional `bluetooth.addr` filter.

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X)

1. Start logging well before an expected, natural battery-level decline (e.g. at the start of a
   normal day of use), keeping the connection active/idle rather than disconnecting between
   checks.
2. Periodically (e.g. every 15–30 minutes) note the wall-clock time — no action needed, both
   indicators are expected to update on their own per their respective triggers.
3. End the session after a natural, visible battery-percentage drop has occurred on screen.

## Event Timeline

Wire timestamps below are `CAP-009-btsnoop_hci.log`'s own frame times (same wall clock as the
video overlay, +0200, no offset correction needed). See `CAP-009-FINDINGS.md` for the full
extraction commands and hex payloads behind each cited frame.

| Time | Action | Initiator | Test-ID | Wire evidence / Notes |
|----------|---|---|---|---|
| 18:33:52 (log starts 18:33:47.20, frame 1) | Session start | — | — | btsnoop log opens 18:33:47.20 (frame 1), before the on-screen 18:33:52; classic ACL `Connection Complete` to `04:00:6e:cf:6e:07` at 18:34:01.51 (frame 674, handle `0x0002`, status `0x00`) |
| 18:34:14 | Left ear bud 96%, case 72%, right ear bud 93%. | — | `BATT-006` | `AT+CIND?` queried once at 18:34:01.90 (frame 884) → `battchg=4` (0–5 scale, ≈80%) — matches none of L96/case72/R93 exactly (see `CAP-009-FINDINGS.md` §1). First `AT+BIEV=2,93` at 18:34:02.04 (frame 972) — matches R=93% exactly, 12s before this on-screen check. |
| — (18:40:36, not on-screen-checked) | (no video note — wire-only transient) | — | `BATT-006` | `AT+BIEV=2,92` at 18:40:36.31 (frame 5556) — R briefly at 92% between checks; never separately confirmed on screen. |
| 19:02:49 | Case decrease 72% → 71%, right ear bud 93% → 90%. | — | `BATT-006` | `AT+BIEV=2,90` at 19:02:47.25 (frame 14612) — 2s **before** this on-screen check; matches R's new value. No `AT+CIND`/`AT+BIEV` evidence for the case's 72→71 (HFP's Battery Level HF indicator does not carry a case value, per `CAP-001-FINDINGS.md` §3). |
| 19:26:30 | left ear bud 96% → 95%. | — | — | No `AT+BIEV`/`AT+CIND` evidence expected (HFP's `AT+BIEV=2` tracks R in this session, not L — `CAP-009-FINDINGS.md` §3); `AT+BIEV=2` itself stays at 90 through this window. **But** DLCI `0x08`'s Option E message (`CAP-009-FINDINGS.md` §6) already shows L=95 as early as 19:17:26.10 (frame 18029) — **~9 min before** this on-screen check. |
| 19:29:22 | right ear bud 90% → 89%. | — | `BATT-006` | `AT+BIEV=2,89` already seen at 19:26:28.14 (frame 20632) — **~3 min before** this on-screen check; likely the same underlying change, noticed late (periodic manual checks, not continuous monitoring). |
| 19:50:30 | left ear bud 95% → 94%. | — | — | No `AT+BIEV`/`AT+CIND` evidence expected (same reasoning as 19:26:30). Option E (DLCI `0x08`) shows L=94 already at 19:37:16.88 (frame 24391) — **~13 min before** this on-screen check. |
| 19:52:30 | left ear bud in case. | — | — | Connection stays up (handle `0x0002` unchanged); no HFP-channel effect (no `AT+BIEV`/`AT+CIND` traffic in a ±20s window). Option E (DLCI `0x08`) **does** react: Case's entry flips from a long-lived "unknown" placeholder to a real value 1.3s after contact (frame 26743, 19:52:16.47) and L begins a monotonic charging climb (93%→100% by 19:58:21) — see `CAP-009-FINDINGS.md` §6. |
| 20:00:01 | right ear bud in case, left bud increase 94% → 100%, case decrease 71% → 68%, right bud decrease 89% → 88%. | — | `BATT-006` | `AT+BIEV=2,88` already seen at 19:50:29.37 (frame 26459) — **~10 min before** this check (same "noticed late" pattern). Classic ACL disconnects at 20:00:01.31 (frame 28764, handle `0x0002`, reason `0x13` remote-terminated) — consistent with both buds now in the case. This is the **last** `AT+BIEV`/`AT+CIND` frame in the entire capture (see `CAP-009-FINDINGS.md` §4). |
| 20:00:01–20:00:02 | (brief reconnect, not separately on-screen-noted) | Auto | — | `Connect Request`/`Connect Complete` 20:00:01.74–.92 (frames 28794/28798, handle `0x0001`) → SDP query for **Audio Source only** (frame 28849) → `Disconnect Complete` 20:00:02.78 (frame 28864, reason `0x13`). No RFCOMM/HFP channel opened — A2DP-only blip. |
| 20:02:25 | insert usb power into case. | — | — | Classic ACL reconnect `Connect Complete` at 20:02:27.21 (frame 29074, handle `0x0002` reused) — 2s after this on-screen action; again SDP-queries **Audio Source only** (frame 29122) — no HFP/RFCOMM-SPP channel (DLCI `0x0c`) reopens for the rest of the capture. |
| 20:02:29 | right ear bud increase 88% → 90%. | — | — | No HFP evidence (HFP/RFCOMM channel never reopens after 20:02:27 — see row above). A small BLE `ATT` notification burst (Handle `0x0044`) appears at 20:03:06 (frame 29294), ~37s later — too far to confidently attribute; flagged 🔴 open question in `CAP-009-FINDINGS.md` §4. |
| 20:11:07 | case increase 68% → 75%, right ear bud increase to 88% → 100%. | — | — | No HFP evidence (same reason). No `AT+CIND`/`AT+BIEV` anywhere after frame 28704 (19:59:33). |
| 20:12:55 | Session end — visible battery-percentage drop confirmed on screen | — | — | Log continues to 20:15:00.73 (30,234 total packets) — no further `AT+BIEV`/`AT+CIND` traffic in the tail either. |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X)

- [x] Extract every `AT+CIND?` (`battchg`) value across the session with its timestamp. →
      exactly **one** occurrence, at connection setup (frame 884, 18:34:01.90); never repeated,
      including after the 20:02:27 reconnect. See `CAP-009-FINDINGS.md` §1.
- [x] Extract every `AT+BIEV=2,...` value across the session with its timestamp. → 69 pushes,
      5 distinct values (93→92→90→89→88), irregular intervals (a few seconds to ~23 min apart);
      last push at 19:59:33 (frame 28704), none after the 20:02:27 reconnect. See
      `CAP-009-FINDINGS.md` §2.
- [x] Compare both trends against the on-screen battery percentage noted at each periodic check
      above. → done in the Event Timeline table above and `CAP-009-FINDINGS.md` §3.
- [x] Determine whether either indicator (or neither) tracks the real level change accurately. →
      `AT+BIEV=2` tracks the right earbud's percentage specifically (not an aggregate, not L, not
      case) in this session; `AT+CIND?`'s `battchg` is a single stale/init-time-only snapshot that
      never refreshes. See `CAP-009-FINDINGS.md` §3/§5 for the FACT/HYPOTHESIS breakdown.

**Bonus, beyond `BATT-006`'s HFP mandate:** DLCI `0x08`'s `Group 0x0e Code 0x01` message is
**already 🟢 FACT** as a per-earbud+case battery push (`PROTOCOL.md` §4.3 Option E, `DECISIONS.md`
ADR-014, cross-confirmed in `CAP-001`/`CAP-002`/`CAP-011`) — and `PROTOCOL.md` explicitly named
this capture as the next opportunity for a "fully purpose-built confirmation." This session
delivers that: 75 occurrences across 101 minutes, L/R matching the maintainer's on-screen values
throughout, plus the first-ever Option E view of a live charge cycle (L climbing 93%→100% after
being placed in the case) and the case's own reading transitioning from a long-lived "unknown"
placeholder to real values once a bud makes contact. See `CAP-009-FINDINGS.md` §6 for the full
decode, hex, and two new (HYPOTHESIS-level, not yet maintainer-reviewed) observations proposed as
an addendum to Option E. Separately, DLCI `0x04` carries a **structurally distinct**,
**not**-decoded pattern that only partially overlaps numerically with battery values (clean
counter-example: frame 26852, `03030003dd58ff` — leading byte 221, outside any valid percentage
range) — kept explicitly unresolved, see `CAP-009-FINDINGS.md` §7.

## Next steps after filling this in

- [x] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13's
      traceability check) — confirm `BATT-006` is clearly referenced above, not silently missing.
      `BATT-006` is referenced in 5 timeline rows above (18:34:14, 19:02:49, 19:29:22, 20:00:01,
      plus the wire-only 18:40:36 row) — no gap.
- [x] Write `CAP-009-FINDINGS.md` per `PROJECT_RULES.md` §2, using this file's timeline as the
      evidence source, following the hex & script rule (§1 rule 4a).
- [ ] Update this session's row in `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index — status
      from `planned` to `analyzed`, fill in Android/firmware/app-version columns and the log path.
      **Not done by the AI agent** — firmware/Android/app version are ⚪ ASSUMPTIONs carried over
      from `CAP-023`/`CAP-025` (same physical device), not independently confirmed this session;
      maintainer should confirm or correct before updating the Capture Index.
- [x] Rename this capture's folder from the `yyyy-MM-dd_HH-mm-ss_HH-mm-ss` placeholder to the
      actual session date/start-time/end-time. Already done — folder is
      `CAP-009-2026-08-23_18-33-52_20-12-55-Group_X`.
