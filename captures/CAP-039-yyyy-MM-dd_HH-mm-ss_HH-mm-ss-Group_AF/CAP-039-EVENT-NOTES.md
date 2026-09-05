# Event Notes: Pixel Buds Pro 2 (`libmaestro` / `libgfps`) — Group AF, `Settable-toggles` byte: Set-tap vs. reconnect-Get, same session/dock-state (`CAP-039`)

**Status:** 🔲 **Not yet captured — skeleton only.** Fill in every `___` / `[ ]` below as the
session happens, per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §5 (analysis) and §8 (what to update).
**Do not pre-fill any log-derived value** before the capture exists. Once reviewed, rename this
folder from the placeholder `CAP-039-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AF` to the actual session
date/start-time/end-time.

**Status legend used throughout this file** (`PROJECT_RULES.md` §1, `PROTOCOL.md` §0): 🟢 **FACT**
· 🟡 **HYPOTHESIS** · ⚪ **ASSUMPTION** · 🔴 **OPEN QUESTION**. Never write a conclusion in this
file without one of these four labels.

**Purpose (`CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF, `OBS-006`, new):** the original open question
(`CAP-036-FINDINGS.md` §3) was framed as "does the `Settable-toggles` byte differ between a
`Set`-triggered Notify and a `Get`-triggered Notify?" `DECISIONS.md` ADR-024 has since reframed
this: across 7 video-confirmed samples, the byte tracks **dock state** (`0x00` docked, `0xe8`
otherwise), not trigger type — but every one of those samples came from a **different session**,
so the reframing itself has never been tested by directly comparing a Set and a Get **within one
session, at a fixed, known dock state**. This session does exactly that: one ANC tap (a `Set`)
and one forced reconnect (a `Get`), both performed with the Buds in the **same** dock state
(undocked/worn), to see whether the byte is identical in both cases (supporting ADR-024's
dock-state reading) or still differs by trigger type (meaning ADR-024's reframing was incomplete).

**Method:** Pixel 7a, stock Android, official Pixel Buds Companion App installed and open on
Device details, Google Play Services **enabled**. Buds already bonded, worn in the ears for the
entire session (constant dock state = undocked throughout, so dock state cannot be the explanation
for any difference observed).

**⚠️ Rule:** exactly one ANC tap this session, nothing else touched. Keep the Buds in your ears
(undocked) for the *entire* session, including during the forced reconnect — do not put them back
in the case at any point, or the dock-state variable stops being held constant.

## Log Metadata

|      Field       |                       Value                        |
|------------------|-----------------------------------------------------|
|    Capture ID    |                      `CAP-039`                      |
|      Group(s)    | AF (`OBS-006` — Settable-toggles Set-vs-Get, fixed dock state; incidental `ANC`-family, `PAIR-003`) |
|       Date       |                     `___`                           |
| Firmware version | ⚪ ASSUMPTION `release_5.203` |
|   Test device    | Pixel 7a, Android `___`. **Official Pixel Buds Companion App, Google Play Services enabled** |
| Video file       | `CAP-039-recording.mp4` — `___` |
| Log file         | `CAP-039-btsnoop_hci.log` — `___`s, `___` packets, `___`–`___` local time |
| Buds MAC (partial, per `AGENTS.md` §7/§9) | `04:00:6e:cf:6e:07` |

**Capture-integrity pre-flight** — identical method to `CAP-036`/`CAP-037`/`CAP-038`. Record
results here: `___`

## Preparation checklist (before recording)

- [ ] Buds already bonded and connect normally. Do **not** "Forget" or re-pair.
- [ ] Official Pixel Buds Companion App installed and working; version if visible: `___`
- [ ] Google Play Services **enabled** — carry `CAP-036`'s verification forward as ⚪ ASSUMPTION.
- [ ] No third-party BLE/GATT tool used.
- [ ] Bluetooth HCI snoop logging enabled and the phone rebooted.
- [ ] Video recording with a visible wall-clock overlay ready.
- [ ] **Note the current ANC mode before recording** (so the Set tap's before/after is known):
      `___`
- [ ] Decide in advance which ANC mode you'll tap to (any mode different from the current one is
      fine — the specific mode doesn't matter, only that a genuine `0x12` Set frame is produced).

## Procedure (per `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AF)

1. Start video recording (wall-clock overlay visible), confirm HCI snoop logging active. Buds
   already in ears, already connected, Device details screen open.
2. **Window 1 — the Set** [`ANC`-family, incidental]. Tap a different ANC mode once. Note the
   exact tap time and the resulting on-screen mode. Idle ~5s.
3. Clean buffer (~5s), nothing touched.
4. **Window 2 — the Get** [`OBS-006`, incidental `PAIR-003`]. Force a reconnect **without removing
   the Buds from your ears** — toggle Bluetooth off then on again from system settings (the Buds
   stay in your ears throughout; this only affects the phone side of the link). Note the exact
   toggle-off and toggle-on times, and the on-screen reconnection-complete time. Idle ~10s without
   touching anything.
5. Stop video recording and HCI snoop logging.
6. Extract via the raw btsnoop path first, then run the capture-integrity pre-flight.

## Event Timeline

| Time (local) | Action / Event | Initiator | Test-ID | Evidence in `CAP-039-btsnoop_hci.log` |
|---|---|---|---|---|
| `___` | Start video recording | — | — | — |
| `___` | **Window 1 (Set)** — ANC mode tapped | User (App) | `ANC`-family | frame `___` |
| `___` | On-screen mode updates | App (Auto) | — | frame `___` |
| `___` | **Window 2 start (Get)** — Bluetooth toggled OFF | User (Hardware) | `OBS-006`, `PAIR-003` | frame `___` |
| `___` | Bluetooth toggled ON | User (Hardware) | `OBS-006` | frame `___` |
| `___` | Connection established (on-screen) | App/OS (Auto) | `OBS-006` | frame `___` |
| `___` - `___` | Idle, nothing touched | — | `OBS-006` | frames `___`–`___` |
| `___` | **Window 2 end** | — | `OBS-006` | frame `___` |
| `___` | End video recording | — | — | — |

**Contamination log:** `___`

## Decode / Analysis

```
tshark -r CAP-039-btsnoop_hci.log -Y "bthci_acl.chandle==<Buds chandle> and btrfcomm.dlci==4 and btrfcomm.len>0" \
  -T fields -e frame.number -e frame.time -e frame.p2p_dir -e data.data
```

- [ ] **Window 1's `0x12` Set frame** — decode the resulting `08 13` Notify (if any) and record its
      `Settable-toggles` byte. Result: `___`
- [ ] **Window 2's `08 11`/`08 13` Get/Notify pair** — decode and record its `Settable-toggles`
      byte. Result: `___`
- [ ] **Direct comparison:** are the two byte values identical? If **yes**, this directly supports
      `DECISIONS.md` ADR-024's dock-state reading (trigger type doesn't matter, only dock state,
      held constant here) — record as a same-session confirmation, not a new promotion. If **no**,
      this is a genuine counter-example to ADR-024 as currently written — flag prominently, do not
      quietly reconcile it, and propose revisiting ADR-024's scope. Result: `___`
- [ ] **Outcome classification:** (a) both `0xe8` (undocked, matching ADR-024, trigger-independent)
      → confirms current understanding; (b) they differ despite identical dock state → ADR-024
      needs revisiting, this is a priority finding; (c) inconclusive (a setting other than the one
      ANC tap was touched, buds were docked at some point, log truncated) → 🔴 unconfirmed, re-run.
      Classification: `___`

## Open Questions

- 🔴 `___`

## Next steps after filling this in

- [ ] Cross-reference every Test-ID this Group is supposed to exercise (`AGENTS.md` §13).
- [ ] Write `CAP-039-FINDINGS.md` per `PROJECT_RULES.md` §2, hex & script rule (§1 rule 4a).
- [ ] Update `CAPTURE_BLUETOOTH_HCI_SNOOP.md` §9 Capture Index (status → `captured`/`analyzed`) and
      `id_registry.csv`'s `CAP-039` row.
- [ ] Add `OBS-006`'s Evidence column pointer in `TESTPLAN_BLUETOOTH_HCI_SNOOP.md` (pointer only).
- [ ] Copy every new 🔴 OPEN QUESTION into `PROTOCOL.md` §6 (mandatory, §8).
- [ ] **Do not** promote anything to 🟢 FACT or write/amend a `DECISIONS.md` ADR without explicit
      maintainer sign-off (`AGENTS.md` §6, §15) — propose only, even if this session's result
      would revise ADR-024.
- [ ] Rename this capture's folder to the actual session date/start-time/end-time.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-039-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AF/CAP-039-EVENT-NOTES.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-039-yyyy-MM-dd_HH-mm-ss_HH-mm-ss-Group_AF/CAP-039-EVENT-NOTES
