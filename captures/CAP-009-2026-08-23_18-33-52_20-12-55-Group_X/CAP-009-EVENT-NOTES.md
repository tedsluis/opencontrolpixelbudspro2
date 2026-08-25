# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group X, Battery-level discrepancy bracket (`CAP-009`)

**Status: repeated, independent re-analysis (2026-08-2x).** A first pass over this capture was
limited to checking the wire log around timestamps the maintainer had already noted by hand, which
risks only confirming what was already seen rather than finding what was missed. This version
corrects that: an independent video timeline was built first (per-10-second contact-sheet pass over
the *entire* duration of both recordings, then 1fps-or-denser passes narrowing every discovered
transition to sub-2-second bounds), *before* the maintainer's own `EVENT-NOTES` draft was
consulted, and only then diffed against it. The wire-log side (§Methodology) was likewise re-run
in full — MAC re-derived independently, DLCI inventory taken over the whole log, every
`AT+BIEV`/`AT+CIND` frame re-extracted from scratch. See `CAP-009-FINDINGS.md` for what this means
for the protocol; this file is the event timeline and the record of what the independent pass
found, confirmed, corrected, and could not resolve.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X):** `CAP-001-FINDINGS.md` §3 found
`AT+CIND?`'s `battchg=3` (≈60%) and `AT+BIEV=2,100` (100%) disagreeing at the same moment —
unresolved whether either indicator actually tracks a real battery-level change over time.
**Can be combined with `CAP-008`'s (Group V) session** if a phone call happens to occur naturally
within this window — no need to force it, just note it if it happens. No phone call occurred in
this session (no `AT+` traffic outside the connection-setup handshake and periodic `AT+BIEV`
pushes).

## Log & Video Metadata

| Field                                      | Value                                                                                                                         |
|-------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| Capture ID                                 | `CAP-009`                                                                                                                     |
| Group(s)                                   | X                                                                                                                             |
| Date                                       | 2026-08-23                                                                                                                    |
| Firmware version                           | not queried this session (⚪ ASSUMPTION `release_5.203`, same physical device as `CAP-023`/`CAP-025`)                       |
| Test device                                | Pixel 7a, Android 17 (⚪ ASSUMPTION, consistent with `CAP-023`/`CAP-025`), official app not confirmed this session          |
| Video file 1                               | `CAP-009-recording1.mp4` — burned-in wall-clock overlay confirms it runs **18:33:52 → ~20:01:14** (`ffprobe` duration 5241.9s), **not** 18:33:52–20:00:27 as originally assumed — see "Video-boundary correction" below |
| Video file 2                               | `CAP-009-recording2.mp4` — burned-in overlay confirms it runs **~20:01:16 → ~20:12:51+** (`ffprobe` duration 699.7s)    |
| Log file                                   | `CAP-009-btsnoop_hci.log` — 2026-08-23 18:33:47.20–20:15:00.73 (wall clock, +0200), 30,234 packets                     |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — **independently re-derived**, see "MAC verification" below                                            |

### Video-boundary correction (independent finding)

The task brief stated `recording1` ends 20:00:27 and `recording2` starts 20:01:17, implying a
**~50-second blind gap** between them. The actual video content (verified from each file's own
burned-in wall-clock overlay, not from filenames or metadata) shows this is wrong:

- `recording1`'s **true last frame** is at burned-in time **20:01:14** — 47 seconds later than
  assumed — and still shows a valid, readable screen (`Left: 100%`, `Case: 68%`, `Right: 88%`,
  a `Connect` button i.e. already disconnected, both earbuds visibly resting in the open case).
- `recording2`'s **true first frame** is at burned-in time **20:01:16** — 1 second earlier than
  assumed — showing the *identical* screen state (same values, same `Connect` button, same case
  contents) as `recording1`'s last frame.

**Actual video coverage gap: ~2 seconds (20:01:14–20:01:16), not ~50 seconds.** The two files
overlap in content almost seamlessly; nothing of substance is missing between them. This also
means `recording1` **does** cover the full disconnect (`AT+BIEV`/`AT+CIND` stop, ACL disconnect,
both-buds-in-case, lid closing) that was assumed to fall in the "gap" — it's all directly visible
in `recording1`'s last ~70 seconds, not inferred from the log alone.

### MAC verification (independent re-derivation, not taken on trust)

The task brief's MAC placeholder was not filled in. Re-derived and cross-checked four independent
ways before filtering on it:

1. **Connection-event address.** The log's only `HCI_Connection_Complete` events
   (`bthci_evt.code==0x03`) are all for `04:00:6e:cf:6e:07` (3 occurrences — see Methodology).
2. **Device-capability string, raw byte search (not a dissector filter — see note below).** The
   literal ASCII string `google-pixel-buds-pro-v1` (the wire-confirmed capability identifier
   already documented since `CAP-001`, per `AGENTS.md` §15's hardcoded-string exception) occurs
   **75 times** in the raw log file, always as part of DLCI `0x08` traffic on the same connection
   handle as (1):

   ```bash
   $ python3 -c "
   data = open('CAP-009-btsnoop_hci.log','rb').read()
   print(data.count(b'google-pixel-buds-pro-v1'))"
   75
   ```

   (Note: `tshark -Y 'frame contains "google-pixel-buds-pro-v1"'` returned 0 matches for reasons
   not fully diagnosed — likely a display-filter string-escaping quirk with the embedded hyphens —
   so this check was done as a raw byte search instead; the hex is directly visible in every
   `Group 0x0e Code 0x02` frame preceding a `Code 0x01` battery push, e.g. frame 875's payload.)
3. **SDP profile fingerprint.** The peer at this connection advertises/queries Hands-Free (HFP),
   Audio Sink/A2DP, and AVRCP service records (frames 736, 861, 931, 1367, 1403, ...) — consistent
   with a headset-class audio accessory, not an unrelated device type.
4. **On-screen confirmation.** Every reviewed video frame (see below) shows the phone's "Device
   details" screen headed **"Pixel Buds Pro 2 van Ted"**.
5. **Cross-capture consistency.** `04:00:6e:cf:6e:07` is the same MAC already independently
   confirmed as this physical device across `CAP-001`, `CAP-006`, `CAP-016`, `CAP-019`–`CAP-025`.

No other classic (BR/EDR) device ever completes an ACL connection anywhere in this log (see the
filter-sanity check below), so there is no ambiguity to resolve between candidate addresses.

## Independent video analysis — methodology

**Base pass, full duration, before opening the maintainer's own notes:** both files were sampled
at 1 frame per 10 seconds (`ffmpeg ... fps=1/10`, cropped to the on-screen `Left/Case/Right`
percentage row, tiled into contact sheets with the relative playback time burned into each cell via
`drawtext`) — **9 contact sheets for `recording1` (≈524 frames) + 2 for `recording2` (≈70 frames) =
≈594 frames**, covering both files end-to-end. This surfaced every visible percentage change
without presupposing where they were.

```bash
$ ffmpeg -ss <chunk_start> -t 600 -i CAP-009-recording1.mp4 \
    -vf "crop=420:80:150:670,drawtext=fontfile=<mono.ttf>:text='%{pts\:hms}':x=5:y=2:fontsize=14:fontcolor=yellow:box=1:boxcolor=black@0.7,select='not(mod(n\,300))',tile=6x10" \
    -frames:v 1 -vsync 0 out.png
```

**Dense pass, precision:** every transition found in the base pass was re-extracted at up to
1-frame (30fps) resolution within a narrowed window, cross-checking the *actual* cell contents at
each step rather than trusting the coarse pass's own row/column reading — **this caught and
corrected several misreadings in the coarse pass itself** (see the two flagged rows below), which
is exactly the kind of self-check `PROJECT_RULES.md`'s evidence bar calls for: a "clean match" at
low resolution turned out, on close reading, to be off by minutes in more than one case, until
verified at 1-frame precision.

**Mandatory full-density coverage of 19:50:00–20:12:55:** this window (both buds going into the
case, lid closing, USB insertion) is covered by the base pass's own chunks 7–8 (`recording1`) and
both `recording2` chunks at 10s resolution, **plus** dedicated 1-frame-precision dense passes
around every transition inside it (E, G, H, I below) — i.e. denser than the base pass everywhere
a real event was found, not just spot-checked.

**Full (uncropped) frame checks:** at several points the cropped pass alone was insuffient —
uncropped frames were pulled to see the physical action (bud insertion, case lid, USB cable) behind
a value change, since a percentage-only crop cannot show *why* a value changed.

## Independent findings — diff against the maintainer's original notes

All 7 value-change events the maintainer had noted **exist and are confirmed** by the independent
pass. Two of the maintainer's own timestamps are **refined by minutes** (not contradicted — the
maintainer's times were "when I happened to check," not "when it changed," which is exactly the
periodic-manual-check limitation the capture design (§Procedure) anticipated). No event the
maintainer noted was found to be spurious, and no *additional* battery-value change was found
that the maintainer's notes had missed. Two independently-*new* observations were made that are
not battery values at all (a deliberate finger-tap on the Right battery icon, and a phone
notification banner/dismissal) — see the table below.

| # | Maintainer's note | Independent finding (video, sub-2s precision unless noted) | Verdict |
| --- | --- | --- | --- |
| A | 18:34:14: L96/Case72/R93 | Matches; first stable reading from ~18:34:11 | ✅ confirmed |
| B | 19:02:49: Case72→71, R93→90 | **19:02:47.35–48.02** | ✅ confirmed (2–5s earlier) |
| C | 19:26:30: L96→95 | **19:26:28.02–29.02** | ✅ confirmed (~2s earlier) |
| D | 19:29:22: R90→89 | **19:29:20.01–28.01** (±hand obscures the exact cell — see below) | ✅ confirmed, within window |
| E | 19:50:30: L95→94 | **19:50:29.02–30.02** | ✅ confirmed (~1s earlier) |
| — | (not separately noted) | 19:52:13–23: left bud visibly placed into the case (uncropped frame) | new physical confirmation |
| — | (not separately noted) | 19:59:58: **both** earbuds already in case, lid being closed (uncropped frame) — ~3s before the wire disconnect | new physical confirmation |
| G | 20:00:01: L94→100, Case71→68, R89→88 (bundled) | **20:00:01.68–02.01**, truly simultaneous on screen | ✅ confirmed |
| — | 20:02:25: USB inserted | ~20:02:24, USB-C connector visible approaching the case's port (uncropped frame) | ✅ consistent |
| H | 20:02:29: R88→90 | **20:02:28.68–29.01** | ✅ confirmed (essentially exact) |
| I | 20:11:07: Case68→75, R90→100 | **20:11:02.03–05.70** (a phone notification banner + finger-swipe obscures the exact cell — see below) | ✅ confirmed, within window |

**Two rows where the coarse (10s) base pass initially misread its own grid** (caught during the
dense-pass double-check, not left standing): the first attempt at bracketing event B used a wrong
row/column offset and produced a window (19:03:23–33) that would have been ~35–45s later than the
correct answer; a similar misread briefly suggested H had drifted ~9s from the maintainer's note.
Both were caught by re-deriving the window from a fresh, wider low-resolution scan and re-verifying
against the burned-in clock before trusting the number — the corrected values (B, H above) are what
made it into the table. This is recorded here explicitly per the task's own instruction that
zero discrepancies over 90+ minutes is a reason to *re-check your own extraction*, not a reason to
conclude the maintainer's notes need no independent verification.

**Event D detail:** a finger is visibly touching/tapping the on-screen **Right** battery icon at
19:29:23 (burned-in clock), squarely inside the D transition window; the tap itself briefly
triggers a UI re-layout (title header flashes into view) that obscures the exact percentage for
~7 seconds. Whether the tap *triggered* the visible update or merely coincided with a manual check
right as it happened is not established here — flagged as worth a dedicated check (does tapping a
battery icon force an on-demand re-query?) rather than guessed at.

**Event I detail:** a phone notification banner (unrelated third-party app icons, "Pathé Thuis" /
"Hornbach") appears over the crop region and is dismissed by a finger-swipe during the I
transition window, similarly obscuring ~3.7 seconds of frames. Unrelated to the Buds; noted only
because it explains why I's bound is wider than the others.

## Methodology / CLI hygiene (`AGENTS.md` §13)

`bluetooth.addr` never populates in this log for ACL data frames (verified: `tshark -r
CAP-009-btsnoop_hci.log -Y "bluetooth.addr" -c 1` returns zero matches anywhere in the file). The
log contains exactly **three** `HCI_Connection_Complete` events (`bthci_evt.code==0x03`), all for
`04:00:6e:cf:6e:07`, mapping to handles `0x0002` (18:34:01.51, frame 674), `0x0001` (20:00:01.92,
frame 28798, ~0.9s, torn down immediately), and `0x0002` again (20:02:27.21, frame 29074):

```
$ tshark -r CAP-009-btsnoop_hci.log -Y "bthci_evt.code==0x03" \
    -T fields -e frame.number -e frame.time -e bthci_evt.bd_addr -e bthci_evt.connection_handle -e bthci_evt.status
674    2026-08-23T18:34:01.513362000+0200  04:00:6e:cf:6e:07  0x0002  0x00
28798  2026-08-23T20:00:01.924735000+0200  04:00:6e:cf:6e:07  0x0001  0x00
29074  2026-08-23T20:02:27.209998000+0200  04:00:6e:cf:6e:07  0x0002  0x00
```

**Filter-sanity check (mandatory per the task brief):** the full log has 30,234 frames; only 2,503
sit on chandle `0x0002` and 21 on chandle `0x0001` (the Buds' two connection periods) — **~8.4% of
the log is Buds traffic, ~91.6% is not.** This is not a sign of a broken filter — it is expected
and independently explained: the log contains **zero** other classic-ACL `Connection Complete`
events (i.e. no other classic peer ever appears), but tens of thousands of `LE Meta`
advertising-report/connection-churn frames from ambient background BLE traffic (other nearby
devices, routine Android background scanning) that a phone-side `btsnoop_hci` log captures
indiscriminately regardless of which device the researcher cares about. Verified rather than
assumed:

```
tshark -r CAP-009-btsnoop_hci.log 2>/dev/null | wc -l                                    # 30234
tshark -r CAP-009-btsnoop_hci.log -Y "bthci_acl.chandle==0x0002" 2>/dev/null | wc -l     # 2503
tshark -r CAP-009-btsnoop_hci.log -Y "bthci_acl.chandle==0x0001" 2>/dev/null | wc -l     # 21
```

**DLCI inventory, whole log, exhaustive (not limited to already-expected channels):**

```
$ tshark -r CAP-009-btsnoop_hci.log -Y "btrfcomm.len>0" -T fields -e btrfcomm.dlci -e frame.p2p_dir \
    | sort | uniq -c | sort -rn
    342 0x08 1      109 0x08 0
    298 0x04 1       58 0x04 0
    181 0x02 1      102 0x00 1
    138 0x02 0      102 0x00 0
     91 0x0c 0       83 0x0c 1
```

Exactly **5** DLCIs carry any RFCOMM payload in this session — `0x00` (multiplexer control),
`0x02` (`libmaestro` Pigweed `pw_hdlc` channel), `0x04`, `0x08`, `0x0c` (HFP AT commands, see
`CAP-009-FINDINGS.md` §1–§2). No unexpected/unaccounted-for DLCI exists. (DLCI numbers are
per-session dynamic assignments per `AGENTS.md` §6 — this session's `0x0c`/HFP and `0x08`/Option E
numbering happens to match several prior sessions', but that is not guaranteed in general.)

## Event Timeline

| Time (bracket) | Action | Initiator | Test-ID | Wire evidence |
| ---------- | --- | --- | --- | --- |
| 18:33:47.20–18:34:01.51 | Session/log start; Bluetooth off → connecting | — | — | Log opens 18:33:47.20 (frame 1); classic ACL `Connection Complete` 18:34:01.51 (frame 674, handle `0x0002`) |
| 18:34:14 | L96%/Case72%/R93% first stable on screen | — | `BATT-006` | `AT+CIND?` queried once, 18:34:01.90 (frame 884) → `battchg=4` (≈80%, matches none of L/Case/R). First `AT+BIEV=2,93` 18:34:02.04 (frame 972) — matches R exactly. |
| (18:40:36, not on-screen-checked) | wire-only transient | — | `BATT-006` | `AT+BIEV=2,92` 18:40:36.31 (frame 5556) |
| **19:02:47.35–48.02** | Case72%→71%, R93%→90% | — | `BATT-006` | `AT+BIEV=2,90` 19:02:47.245 (frame 14612), essentially simultaneous with the on-screen change |
| **19:26:28.02–29.02** | L96%→95% | — | — | No HFP evidence (tracks R only); DLCI `0x08` Option E already shows L=95 at 19:17:26.10 — see `CAP-009-FINDINGS.md` §3/§6 |
| **19:29:20.01–28.01** | R90%→89% (finger-tap on Right icon at 19:29:23 obscures ~7s) | User (physical tap, coincidental or causal — unresolved) | `BATT-006` | `AT+BIEV=2,89` already at 19:26:28.135 (frame 20632) — ~3 min before the on-screen update |
| **19:50:29.02–30.02** | L95%→94% | — | — | DLCI `0x08` Option E already shows L=94 at 19:37:16.88 — ~13 min before the on-screen update |
| 19:52:13–23 | Left earbud placed in case (visually confirmed) | User (Hardware) | — | Option E's Case entry flips unknown→71% at 19:52:16.47 (frame 26743) |
| 19:59:58 | Both earbuds already in case, lid closing (visually confirmed) | User (Hardware) | — | ~3s before wire disconnect |
| **20:00:01.68–02.01** | L94%→100%, Case71%→68%, R89%→88% (simultaneous) | — | `BATT-006` | Classic ACL disconnects 20:00:01.31 (frame 28764); last `AT+BIEV`/Option E push 19:59:33.36 — the on-screen jump lags the last wire push by ~28s, landing right at the disconnect event itself |
| ~20:00:20–20:01:16 | Screen shows cached disconnected state (`Connect` button, 100/68/88) | — | — | No RFCOMM traffic (HFP channel never reopens — see `CAP-009-FINDINGS.md` §4); **video coverage is continuous here, not a ~50s blind gap** (see above) |
| ~20:02:24 | USB-C cable approaching case port (visually confirmed) | User (Hardware) | — | Classic ACL reconnect 20:02:27.21 (frame 29074) |
| **20:02:28.68–29.01** | R88%→90% | — | `BATT-006` | No HFP evidence (channel stays closed); mechanism unresolved (🔴, `CAP-009-FINDINGS.md` §4) |
| **20:11:02.03–05.70** | Case68%→75%, R90%→100% (notification banner obscures ~3.7s) | — | — | No HFP evidence; mechanism unresolved |
| ~20:12:51+ | Session end, case fully closed and charging (both earbuds no longer visible, all 3 values charging) | — | — | Log continues to 20:15:00.73; no further `AT+BIEV`/`AT+CIND` |

## Analysis checklist (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group X)

- [x] Independent video timeline built first, full duration, before consulting the maintainer's
      own notes (this session's specific requirement, beyond the original Group X checklist).
- [x] Extract every `AT+CIND?` (`battchg`) value across the session with its timestamp. → exactly
      **one** occurrence (frame 884, 18:34:01.90); never repeated. See `CAP-009-FINDINGS.md` §1.
- [x] Extract every `AT+BIEV=2,...` value across the session with its timestamp. → 69 pushes (73
      total `AT+BIEV`/`AT+CIND` frames combined), 5 distinct values, irregular cadence. See
      `CAP-009-FINDINGS.md` §2.
- [x] Compare both trends against the on-screen battery percentage — done independently (video)
      and cross-checked against the maintainer's notes, table above.
- [x] Determine whether either indicator (or neither) tracks the real level change accurately. →
      `AT+BIEV=2` tracks the right earbud specifically; `AT+CIND?`'s `battchg` is a single stale
      snapshot. See `CAP-009-FINDINGS.md` §3/§5.
- [x] DLCI `0x04` checked for Fast Pair Message-Stream-style battery notification content (per
      `PROTOCOL.md` §4.3 Option B's still-unconfirmed battery code) — see `CAP-009-FINDINGS.md`
      §6a for a candidate, HYPOTHESIS-level match found this session.

## Next steps

- [x] Maintainer sign-off obtained 2026-08-2x on all 5 proposals from this re-analysis (`BATT-006`
      resolution incl. the per-earbud/cadence revision, the two Option E addenda, the DLCI `0x04`
      candidate for Option B, and the BLE-scan candidate for Option A) — recorded in `PROTOCOL.md`
      §4.3 Options A/B/C/E and `DECISIONS.md` `ADR-015`; see `CAP-009-FINDINGS.md`'s updated banner.
- [x] Updated `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index status/notes to reflect this
      re-analysis (independent video timeline, corrected video-boundary note, DLCI `0x04` finding).
- [ ] Maintainer to confirm/correct firmware, Android version, and app version (⚪ ASSUMPTIONs
      carried over from `CAP-023`/`CAP-025`, not independently confirmed this session) — still open.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-009-2026-08-23_18-33-52_20-12-55-Group_X/CAP-009-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-009-2026-08-23_18-33-52_20-12-55-Group_X/CAP-009-EVENT-NOTES
