# CAP-023: Firmware & Device Info Screen (Group I)

Standardized, evidence-based extraction from `CAP-023-btsnoop_hci.log` + `CAP-023-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

## 1. Capture Metadata

| Field | Value |
|---|---|
| Capture ID | `CAP-023` |
| Purpose | Main run-through Group I (`FW-001`–`FW-004`) — **primary goal:** resolve `PROTOCOL.md` §0.1's open "UI-baseline vs. wire-baseline firmware version" question |
| Date | 2026-08-21 |
| Firmware | **`release_5.203`** — confirmed on-screen this session (§3) |
| Test device | Pixel 7a, Android 17 (⚪ ASSUMPTION), official Pixel Buds Companion App (version not visible on screen) |
| Log file | [`CAP-023-btsnoop_hci.log`](./CAP-023-btsnoop_hci.log) — 283.6s, 2026-08-21 08:23:00.709–08:27:44.263 (+0200) |
| Notes file | [`CAP-023-EVENT-NOTES.md`](./CAP-023-EVENT-NOTES.md) |
| Video file | [`CAP-023-recording.mp4`](./CAP-023-recording.mp4) — 101.1s, 08:23:40–08:25:21 local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` — same physical device as `CAP-016`/`CAP-019`–`CAP-022` |

## 2. Methodology & Filtering

Video reviewed via tiled contact sheets (full pass) and targeted single-frame extraction around
the Firmware update screen. DLCI 0x02 was searched for the settings-write shape used throughout
this batch (`CAP-020-FINDINGS.md` §5); DLCI 0x08 was searched by raw ASCII content for the known
firmware-version string:

```
$ tshark -r CAP-023-btsnoop_hci.log -Y "btrfcomm.dlci==0x08 and btrfcomm.len>0" \
    -T fields -e frame.number -e frame.time -e data.data | grep -i 72656c656173655f
```

## 3. Analysis: Firmware version resolution (primary goal)

**On-screen** (video, t=37s = 08:24:17, "Device details → More settings → Firmware update →
Device firmware version"): Left earbud `release_5.203`, Right earbud `release_5.203`, Case
`release_5.203`.

**On the wire**, same session, in the connection-time DLCI 0x08 handshake (**before** the screen
was opened, i.e. independently, not triggered by opening it):

```
849  08:23:46.037600  0302003f08061001220d72656c656173655f352e3230332a0030e6...
```

```python
bytes.fromhex("72656c656173655f352e323033").decode()
# -> 'release_5.203'
```

**Resolution:** the app's own user-facing firmware-version display and DLCI 0x08's private
envelope (Group `0x03` Code `0x02`, already documented in `CAP-002-FINDINGS.md` §2a and
`CAP-004-FINDINGS.md` §5a Task 2) carry the **byte-identical** string, confirmed within one
session with an on-screen cross-check — the first time this project has directly verified the
on-wire string against the app's own displayed value rather than inferring a match across
different capture sessions. `"Revision 6"` (DLCI 0x04's official Message Stream Device Information
field, `CAP-002-FINDINGS.md` §3) does not appear anywhere in this screen or session — the app does
not surface that string to the user as "the firmware version."

**Status:** 🟢 **FACT, promoted 2026-08-23** (`DECISIONS.md` ADR-012) — same-session on-screen +
on-wire match is direct evidence, not an inference; maintainer sign-off obtained per `AGENTS.md`
§6, and `PROTOCOL.md` §0.1's firmware-version framing updated accordingly. **Not resolved:** what
`"Revision 6"` itself represents, if not "the firmware version" as the user sees it.

## 4. Analysis: `FW-001` (manual firmware check) — cached, not live-queried

Tapping "Up to date" (t≈43s/08:24:23) and opening the Firmware update screen (t≈37s/08:24:17)
produced **zero** RFCOMM traffic on any DLCI:

```
$ tshark -r CAP-023-btsnoop_hci.log -Y "btrfcomm.len>0 and frame.time_epoch>1787293445 and frame.time_epoch<1787293475" \
    -T fields -e frame.number
(no output)
```

**Status:** 🟢 **FACT** (a checked negative, not an absence of checking — per
`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group Q's three-way-outcome convention, applied here by analogy):
the "Device firmware status: Up to date · Last checked N minutes ago" display and the manual
re-check tap both read from already-cached connection-time data, confirmed by the complete absence
of wire traffic in a wide window around both actions.

## 5. `FW-002` — per-component firmware version display

Same screen as §3 above — Left/Right/Case each show `release_5.203`. No distinguishing per-component
wire content was needed to attribute this since the values are identical across all three and the
screen itself makes no additional RFCOMM query (§4).

**Status:** 🟢 **FACT** for the UI display; the underlying value's wire correlation is covered by §3.

## 6. `FW-003`/`FW-004` — not exercised (gap)

Neither the "About" sub-page (serial numbers, `FW-003`) nor the "Earbud status: Connected" display
(`FW-004`) was visited in this session's video — flagged explicitly per `AGENTS.md` §13's
traceability check, not silently left blank.

## 7. Conclusions & Next Steps

- **Primary goal achieved:** `PROTOCOL.md` §0.1's wire-baseline-vs-UI-baseline firmware version
  question now has a direct, same-session, on-screen-confirmed answer: `"release_5.203"`.
- `FW-001`/`FW-002` both confirmed with a clean, well-evidenced negative result (cached, not
  queried) — a useful, positive finding in its own right.
- **Recommended next step:** a follow-up capture visiting the "About" page would close the
  `FW-003`/`FW-004` gap.

## 8. Open Questions

- 🔴 What does DLCI 0x04's `"Revision 6"` string represent, if not the user-facing firmware
  version? → already tracked in `PROTOCOL.md` §0.1/§6, unchanged by this capture.
- 🔴 `FW-003` (serial numbers) and `FW-004` (connection status) remain unattributed — no capture
  has visited the "About" page yet. → copied to `PROTOCOL.md` §6.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-023-2026-08-21_08-23-40_08-25-21-Group_I/CAP-023-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-023-2026-08-21_08-23-40_08-25-21-Group_I/CAP-023-FINDINGS
