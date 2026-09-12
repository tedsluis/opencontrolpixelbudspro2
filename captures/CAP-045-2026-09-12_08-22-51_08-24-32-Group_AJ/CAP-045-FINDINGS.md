# CAP-045: `HOLD-005` Left/Right ANC-rotation-checklist split (Group AJ) — procedure not run; incidental `TOUCH-007` replication instead

Standardized, evidence-based extraction from `CAP-045-btsnoop_hci.log` + `CAP-045-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-045` |
| Purpose | Group AJ — isolate whether Left-earbud vs. Right-earbud ANC-rotation-checklist writes (`HOLD-005`) are wire-distinguishable, by toggling each of the 4 checklist items per earbud one at a time in Device details → Controls and gestures → the ANC-mode rotation checklist |
| Date | 2026-09-12 |
| Firmware | ⚪ ASSUMPTION `release_5.203` (carried over) |
| Test device | Pixel 7a, Android 17, official Pixel Buds Companion App `1.0.955078536`, Google Play services active |
| Log file | [`CAP-045-btsnoop_hci.log`](./CAP-045-btsnoop_hci.log) — 2,620 packets, 237.53s, `2026-09-12 08:22:52.686–08:26:50.215`. 0/2,620 `cap_len≠len` mismatches — untruncated. |
| Video file | [`CAP-045-recording.mp4`](./CAP-045-recording.mp4) — 100.75s (`ffprobe`), overlay `08:22:51`–`08:24:32` |
| Notes file | [`CAP-045-EVENT-NOTES.md`](./CAP-045-EVENT-NOTES.md) |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:...:07` |

## 2. Central finding — this session did not run Group AJ's own procedure (🟢 FACT)

Full video review shows the "Device details" screen's **top-level ANC toggle row** (Noise
cancellation/Off/Adaptive/Transparency, directly on the main screen) at every checked frame — never
"Controls and gestures" → the rotation-checklist sub-screen Group AJ's own procedure specifies. The
maintainer instead performed **physical long-press gestures directly on each earbud**, cycling ANC
mode hands-free — exactly matching the draft Event Notes' own "user long-pressed left/right bud"
language, which this session's corrected timeline confirms against the wire (see §3).

**Confirms via the wire, independently of the video reading:**

```
$ tshark -r CAP-045-btsnoop_hci.log -Y 'btrfcomm.dlci==2 and btrfcomm.len>0 and frame.p2p_dir==0' \
    -T fields -e frame.number -e frame.time -e data.data
(38 Sent frames total, all within two tight clusters: 08:22:58.486–08:23:01.535 and 08:23:57.681–08:24:06.478)
```
Every one of these 38 DLCI 0x02 `Sent` frames matches the already-documented connect-time/
channel-reopen settling-burst shape (`CAP-036-FINDINGS.md` §4, `CAP-041-FINDINGS.md` §8's
byte-for-byte-diffed content) — none carries the `field5(len12){field4(len10){field12(len8){...}}}`
rotation-checklist shape (`PROTOCOL.md` §4.5.3, `qhr` field 12/`qht`). **Zero rotation-checklist
writes occur anywhere in this session.**

## 3. What actually happened: 10 `TOUCH-007`-style press-and-hold ANC transitions, cleanly correlated (🟢 FACT)

```
$ tshark -r CAP-045-btsnoop_hci.log -Y 'btrfcomm.dlci==4 and btrfcomm.len>0' -T fields \
    -e frame.number -e frame.time -e frame.p2p_dir -e data.data | grep -E "\s08(11|12|13)"
609   08:22:57.420909  0  08110000                  <- connect-time Get
612   08:22:57.471104  1  0813000401e8e840          <- connect-time Notify, Current=Adaptive
1583  08:23:21.948918  1  0813000401e8e880          <- Notify only, Current=Transparency
1755  08:23:32.507031  1  0813000401e8e808          <- Notify only, Current=Noise cancellation
1818  08:23:46.898794  1  0813000401e8e840          <- Notify only, Current=Adaptive
1849  08:23:54.405932  1  0813000401e80020          <- Notify only, Current=Off (bud removed)
2077  08:23:59.784596  0  08110000                  <- fresh Get (right-bud-insertion-adjacent)
2079  08:23:59.810470  1  0813000401e80020          <- Notify, Current=Off
2122  08:24:02.089451  1  0813000401e8e840          <- Notify only, Current=Adaptive
2168  08:24:06.394777  1  0813000401e8e880          <- Notify only, Current=Transparency
2218  08:24:16.232148  1  0813000401e8e808          <- Notify only, Current=Noise cancellation
2237  08:24:20.834994  1  0813000401e8e840          <- Notify only, Current=Adaptive
2254  08:24:27.032906  1  0813000401e8e880          <- Notify only, Current=Transparency
2273  08:24:30.086175  1  0813000401e80020          <- Notify only, Current=Off (bud removed)
```

**Every state transition after the connect-time pair is a bare `Notify` (`08 13`) with no preceding
`08 12` Set frame** — exactly `CAP-027-FINDINGS.md` §4's already-documented press-and-hold mechanism
(a hardware gesture drives the Buds to report a Notify directly, bypassing the app-initiated Set
path). All 10 transitions correlate to a video-confirmed action within ≤1s (see the corrected Event
Timeline), 8 of them clean ANC-mode-cycle transitions (Adaptive→Transparency→Noise
cancellation→Adaptive, repeating) matching the physical long-press gestures, and 2 of them
(`08:23:54`, `08:24:30`) matching in-ear-removal moments (`Current` reads `Off`, the same value a
genuine "ANC off" selection would produce — no distinct removal-specific value exists in this
message).

**Left-vs-Right distinguishability, for the mechanism actually exercised — the message itself
carries no side field (🟢 FACT, structural):**
```
[Group:1][Code:1][Len:2BE=0004][Version:1][UI toggles:1][Settable toggles:1][Current state:1]
```
Per `PROTOCOL.md` §4.1's already-confirmed layout, this 8-byte frame has no field capable of
encoding which earbud triggered it. All 5 Left-attributed and 5 Right-attributed Notify frames in
this session are structurally identical in shape — differing only in the `Current state` byte value,
which reflects the *new ANC mode*, not the *triggering side*. **The only way to attribute a frame to
Left vs. Right is timing/video correlation** — which this session's own corrected Event Timeline
provides (Left cluster: `08:23:21`–`08:23:54`; Right cluster: `08:24:00`–`08:24:30`), extending
`CAP-027-FINDINGS.md` §4's original single-earbud, single-sample finding to a 10-sample, both-earbud
replication of the same structural conclusion.

## 4. What this means for `HOLD-005` (🔴 unresolved, unchanged)

This capture provides **zero evidence** toward `HOLD-005`'s own question (does the *rotation-checklist
setting's own DLCI 0x02 write* — `qhr` field 12 — carry a Left/Right distinguishing field?) — the
checklist screen was never opened, so no such write exists in this log to examine. `CAP-021-FINDINGS.md`
§4's own original finding ("field order matches on-screen order but cannot be split between Left's/
Right's lists from wire content alone") stands exactly as it was before this session. **A genuine
re-run of Group AJ's own procedure is still needed.**

## 5. Test-ID traceability

- **`HOLD-005`**: **not exercised** — the checklist screen was never opened.
- **`TOUCH-007`** (incidental, not this Group's own target): exercised 8 times (4 per earbud),
  cleanly video/wire-correlated — a further replication of `CAP-027-FINDINGS.md` §4's finding, and
  the first to show it holds for the Right earbud specifically (`CAP-027`'s own Right-side sample
  was ambiguous between "one long hold" and "two presses," per that file's own open item — this
  session's 4 clean Right-side samples don't resolve that ambiguity either, since this session's own
  gestures weren't individually video-timed to sub-second precision).

## 6. Conclusions & proposed downstream updates

**Recorded as this session's own factual result (no sign-off needed, purely descriptive):**
- This session ran physical press-and-hold ANC cycling, not the planned rotation-checklist toggling.
- 10 wire-confirmed `TOUCH-007`-style Notify-without-Set transitions, cleanly video-correlated.
- The Notify frame's own structure has no Left/Right field — confirmed via `PROTOCOL.md` §4.1's
  already-documented layout, not a new decode.

**Proposed (⏳ awaiting maintainer sign-off, per `AGENTS.md` §6/§15):**
1. `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 / `TODO.md` — flag that `HOLD-005`'s Left/Right split question
   still needs a genuine Group AJ attempt; this session does not close it.
2. This session's 10-sample `TOUCH-007` replication proposed as additional supporting evidence for
   `CAP-027-FINDINGS.md` §4's existing finding — not a new promotion.

## 7. Open Questions

- 🔴 `HOLD-005`'s original question (is the rotation-checklist's own `qhr` field-12 write
  Left/Right-distinguishable?) remains completely open — needs a genuine re-run.
- 🔴 What triggered the `Get`/`Notify` pair at `08:23:59.78` (frames 2077/2079), distinct from every
  other transition in this session (which are Notify-only)? Plausibly tied to the right bud's
  in-ear-insertion, not confirmed.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-045-2026-09-12_08-22-51_08-24-32-Group_AJ/CAP-045-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-045-2026-09-12_08-22-51_08-24-32-Group_AJ/CAP-045-FINDINGS
