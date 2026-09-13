# CAP-044: SDP UUID branch isolation, 2nd attempt (Group AA repeat, `SDP-001`/`SDP-002`)

Standardized, evidence-based extraction from `CAP-044-btsnoop_hci.log` + `CAP-044-recording.mp4`,
staged here per `PROJECT_RULES.md` §2. Every claim below carries a status per `PROJECT_RULES.md` §1:

- 🟢 **FACT** — directly observed in this capture, with a frame number.
- 🟡 **HYPOTHESIS** — plausible reading of the capture, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not tested here, carried over from other sources.
- 🔴 **OPEN QUESTION** — genuinely unresolved by this capture.

**Capture ID:** `CAP-044` · **Date:** 2026-09-13 · **Firmware:** `release_5.203` (🟢 confirmed
on-wire, frame 4416, DLCI 0x08 Group `0x03` Code `0x02`) · **Phone:** Pixel 7a, Android 17 (🟢
FACT, MP4 container tag) · **App version:** `1.0.955078536` (⚪ ASSUMPTION, maintainer-supplied
context) · **Google Play services:** active (⚪ ASSUMPTION, maintainer-supplied context) ·
**Log file:** `CAP-044-btsnoop_hci.log` (6,363 packets, 850.35s, 12:59:08.43–13:13:18.78 local) ·
**Video:** `CAP-044-recording.mp4` (316.42s, 13:05:51–13:11:07 local) · **Buds MAC (partial):**
`04:00:6e:cf:6e:07` — same physical device as `CAP-001`/`CAP-002`/`CAP-032`/`CAP-033`. No
`.log.last` present (confirmed via `ls` both at prompt-drafting time and this analysis).

**Scope note:** Group AA covers `SDP-001` (tested this session) and `SDP-002` (opportunistic, not
attempted — no firmware update pending, same as `CAP-033`).

**Snaplen integrity:** `frame.cap_len == frame.len` for all 6,363 frames — no truncation.

---

## 1. Video review (Step D) — corrects the draft timeline's timing significantly

The full 316.4s video was reviewed via 5s-interval overview contact sheets (11 sheets × 6 frames)
plus four denser 1–2fps passes around the pairing sequence, the account-link "Save"/"Set up device"
transition, and the second app relaunch. **The draft `CAP-044-EVENT-NOTES.md` timeline's ordering of
events is broadly right, but several of its specific timestamps are off by tens of seconds to over a
minute** — corrected below, with every claim tied to a specific video frame's own burned-in
wall-clock overlay.

| Video-verified time | Event | Notes vs. draft |
|---|---|---|
| 13:05:51 (video t=0) | Screen shows Pixel Buds "App info" (Force stop button present, not visibly mid-tap). Force-stop itself is not visible on camera — occurred off-camera per the draft's own claim, unverifiable from video alone. | Matches draft's claimed pre-recording state; the tap itself is not independently confirmable. |
| 13:05:55–13:05:56 | User opens the recent-apps switcher; a cached thumbnail shows the Pixel Buds app's last-known "Device details" screen (`Connected`, `Left:100% Case:84% Right:100%`) — a **static task-switcher thumbnail**, not proof the app was still running. User taps it, relaunching the app. | Matches draft closely. |
| 13:05:59 | "Forget device?" dialog tapped **"Forget"** from within the relaunched companion app. | Matches draft exactly. |
| 13:06:00–13:06:33 | Screen is on the **system** "Connected devices" Settings page continuously for the entire ~33s window — **no Force-stop action of any kind is visible anywhere in this window** (checked at 1fps, all 34 sampled frames). | 🔴 **New finding, not flagged in the draft**: the draft's timeline shows no second force-stop attempt either, but doesn't explicitly note its *absence* is itself notable — see §2 below for why this matters for `SDP-001`. |
| 13:06:34–13:06:48 | Case opened, pairing button held; a Fast-Pair-style "Pixel Buds Pro 2 / Tap to pair" entry appears directly inside the system "Connected devices" list (not a separate popup card, on closer review); user does **not** tap it — instead taps "Pair new device" → "Available devices" → selects **"Pixel Buds Pro 2 van Ted"** (the classic-name entry, a separate list from the Fast-Pair-style one). | Draft says "Fast Pair prompt appears, user taps Connect" at 13:06:40-ish — video shows the user instead used the manual classic-discovery "Pair new device" path, not the Fast Pair quick-connect surface. Methodologically this is *more* aligned with "system Bluetooth settings only," not less. |
| 13:06:51–13:08:06 | "Pair with Pixel Buds Pro 2 van Ted?" confirmation dialog appears and then sits **untapped on screen for over a minute** (~75s, confirmed static at 5s samples throughout; one brief refresh blip at 13:07:21–13:07:26 where the dialog momentarily isn't shown). | **Not in the draft at all** — the draft jumps straight from "13:07:00 prompt accepted" to "13:08:10 connection completes." The actual delay before tapping "Pair" was far longer than the draft implies. |
| **13:08:07** | Finger visibly taps **"Pair"**. | Draft's "13:07:00 standard pairing prompt accepted" is corrected to **13:08:07** — a 67-second difference. This is the actual tap that triggers the bonding + SDP browse (§2/§3). |
| 13:08:12 | System "Connected devices" shows "Pixel Buds Pro 2 van Ted — Active, 100% battery" — connection established. | Matches wire evidence (Link Key Notification 13:08:08.64) almost exactly. |
| 13:08:17–13:10:11 | A Fast Pair **account-link** prompt ("Save device to ted.sluis@gmail.com...") sits on top of the system "Connected devices" screen, **untapped for nearly two minutes** (checked at 5s samples throughout — this is not the companion app's own UI). | **Not in the draft.** The draft's "13:08:10 ... Device details/Setup UI automatically surfaces on screen" does not match what's on screen — it's a system-level Fast Pair dialog over Settings, not the companion app, for this entire stretch. |
| ~13:10:11 | "Save" tapped → dialog changes to "Set up device" (Skip / Set up). | New detail, not in the draft. |
| ~13:10:15 | "Skip" tapped (not "Set up"). | New detail. |
| 13:10:16–13:10:21 | Settings-app navigation (App info screens visible mid-transition) — consistent with the second Force-stop the draft describes, though the exact tap is not caught cleanly at 1–2fps sampling. | Matches draft's intent; exact frame of the tap itself not isolated. |
| 13:10:21–13:10:24 | Home screen → app drawer/search → **Pixel Buds app launched fresh**, showing the "Allow a connection to your Pixel Buds" / nearby-devices-permission onboarding flow (consistent with a genuinely fresh process start after a force-stop). | Matches draft's "13:10:23 user opens the Pixel Buds app manually from the home screen," within a few seconds. |
| 13:10:24–13:10:29 | Permission screens: "Allow a connection..." → Continue → "Allow the app Pixel Buds to access [nearby devices]?" → **Allow** tapped. | New detail (permission re-grant flow), not in the draft. |
| 13:10:31–13:11:07 (video end) | Full companion-app "Device details" screen open, `Active`, `Left:100% Case:83% Right:100%`, Forget/Disconnect buttons — this is **step 3**, the app-open baseline comparison. | Matches draft; video ends here (13:11:07 exactly, cross-checked against `ffprobe`). |

## 2. Isolation-integrity review — the actual isolation gap is different from, and more specific than, what the draft flagged

The draft `CAP-044-EVENT-NOTES.md` already flags "app resurfaces" as a deviation, but frames it as
happening *after* the SDP browse (at its stated "13:08:10"). The corrected timeline above changes
the picture:

- The companion app **was** briefly running between 13:05:55 and (at the latest) whenever the user
  left it after tapping "Forget" at 13:05:59 — this matches the draft.
- **🔴 New, more specific finding: no Force-stop action of any kind is observed anywhere in the video
  between the in-app "Forget" (13:05:59) and the actual "Pair" tap that triggers bonding + the SDP
  browse (13:08:07)** — a gap of 2 minutes and 8 seconds, spent entirely on system Settings screens
  (Connected devices → pairing button press → Pair new device → the untapped confirmation dialog).
  Whether the app's *process* was still resident in memory (backgrounded, not force-stopped) during
  this window, or was independently reclaimed by Android's own background-process management before
  13:08:07, **cannot be determined from the video or the wire log** — this is recorded as an 🔴 OPEN
  QUESTION, not assumed either way, per `AGENTS.md` §13's zero-creativity rule.
- This means `SDP-001`'s isolation requirement ("force-stop the app first, so it cannot react to the
  pairing at all") is **not cleanly satisfied in this session either**, for a different, more precise
  reason than `CAP-033`'s violation (`CAP-033-FINDINGS.md` §1.1's Forget-before-Force-stop ordering
  issue): here the *order* was correct (Force-stop before Forget, at least for the very first
  force-stop before the video started), but there is no confirmed force-stop covering the actual
  window in which the pairing and SDP browse happened.
- **This is directly relevant to interpreting §3's positive finding below** — see §5's discussion of
  why the SDP query *pattern* observed in this session is unusually informative regardless of this
  gap.

**Step 3 (app-open baseline comparison) — 🟢 CONFIRMED EXECUTED this time**, unlike `CAP-033`'s
`§1.3` (never executed at all). The app is opened on-camera at ~13:10:24 and its "Device details" UI
remains visibly open through the video's end (13:11:07). **However — see §4 — this does not produce
a second, comparable SDP browse**, for a specific, wire-confirmed reason.

## 3. The SDP browse itself (🟢 FACT for what was observed)

```
$ tshark -r CAP-044-btsnoop_hci.log -Y "bluetooth.addr == 04:00:6e:cf:6e:07 and btsdp" \
    -T fields -e frame.number -e frame.time -e _ws.col.Info
```

**All `btsdp` traffic in the entire 850.3s log falls in one window: frames 4071–4890,
13:08:08.897–13:08:18.038 (9.1 seconds)** — immediately following the Link Key Notification (frame
4044, 13:08:08.64, the only bonding event in this log) and the "Pair" tap identified in §1 at
13:08:07. **No `btsdp` traffic exists anywhere else in the log** — not before this window (matching
`CAP-033`'s finding that no SDP traffic exists pre-bonding), and, critically, **not after it either**,
including during and after the app-open step 3 window (13:10:24–13:11:07 on video, and through the
log's own tail to 13:13:18.78, over 4 minutes past the last SDP frame). See §4 for why.

**Unlike `CAP-033`'s single generic "browse all services" wildcard request** (that session's frame
1279), **this session's SDP browse consists of standard-profile queries (PnP Information, L2CAP,
Phonebook Access, Hands-Free, HID, AG Hands-Free, Audio Sink, AVRCP) plus five separate, individually
targeted `Service Search Attribute Request`s, each naming one specific custom 128-bit UUID**:

```
$ tshark -r CAP-044-btsnoop_hci.log -Y "btsdp" -T fields -e frame.number -e _ws.col.Info | grep -i unknown
4236  Sent Service Search Attribute Request : Unknown: Attribute Range (0x0000 - 0xffff)
4297  Sent Service Search Attribute Request : Unknown: Attribute Range (0x0000 - 0xffff)
4351  Sent Service Search Attribute Request : Unknown: Attribute Range (0x0000 - 0xffff)
4505  Sent Service Search Attribute Request : Unknown: Attribute Range (0x0000 - 0xffff)
4888  Sent Service Search Attribute Request : Unknown: Attribute Range (0x0000 - 0xffff)
```

Decoding each request/response pair (`tshark -r CAP-044-btsnoop_hci.log -Y "frame.number==<N>" -V`):

| Request | Response | Custom UUID queried | RFCOMM channel (→ DLCI) | Service Name |
|---|---|---|---|---|
| 4236 | 4242 | `f8d1fbe4-7966-4334-8024-ff96c9330e15` | 4 (→ 0x08) | **GSND CONTROL** |
| 4297 | 4310 | `81c2e72a-0591-443e-a1ff-05f988593351` | 5 (→ 0x0a) | **GSND AUDIO** |
| 4351 | 4365 | `df21fe2c-2515-4fdb-8886-f12c4d67927c` | 2 (→ 0x04) | **GFPS RFCOMM** |
| 4505 | 4507 | `25e97ff7-24ce-4c4c-8951-f764a708f7b5` | 1 (→ 0x02) | **MAESTRO APP** |
| 4888 | 4890 | `df21fe2c-2515-4fdb-8886-f12c4d67927c` (repeat) | 2 (→ 0x04) | **GFPS RFCOMM** (repeat) |

Raw excerpt for the "pigweed"/MAESTRO APP entry (frame 4507, `-x`):
```
Service Attribute: Service Class ID List (0x1), value = Unknown
    Value: Custom UUID: 25e97ff724ce4c4c8951f764a708f7b5 (Unknown)
Service Attribute: Protocol Descriptor List (0x4), value = L2CAP -> RFCOMM:1
    Protocol #2: RFCOMM, RFCOMM Channel: 1 (0x01)
Service Attribute: Service Name (0x100), value = MAESTRO APP
```

**All four channel-name↔UUID↔RFCOMM-channel mappings from `CAP-033-FINDINGS.md` §3 reproduce
byte-for-byte identically in this independent session, 14 days later** — a second, independent
wire-level confirmation of "MAESTRO APP"=DLCI 0x02, "GSND CONTROL"=DLCI 0x08, "GSND AUDIO"=DLCI
0x0a, and "GFPS RFCOMM" (official Fast Pair Message Stream)=DLCI 0x04. This further strengthens
`DECISIONS.md` ADR-018's DLCI-0x02-channel-ownership FACT (now a 5th/6th confirming instance,
counting `CAP-001`/`CAP-002`/`CAP-032`/`CAP-033`/this capture) and adds a second wire-level data
point for the `CAP-033`-introduced "GSND CONTROL"/"GSND AUDIO" naming lead.

**"DEBUG APP" (channel 3, DLCI 0x06) and "BTIS" (channel 9, DLCI 0x12) were never queried in this
session** — consistent with this being a set of *targeted* per-UUID queries (naming only the 4 UUIDs
above) rather than `CAP-033`'s single generic "browse all services" wildcard, which happened to
return every registered record including those two.

**The "default internal rfcomm socket" UUID (`3a046f6d-...`) — checked, still absent:**
```
$ python3 -c "d=open('CAP-044-btsnoop_hci.log','rb').read(); \
    print(d.count(bytes.fromhex('3a046f6d24d2765565340d7ecb759709')), \
          d.count(bytes.fromhex('099775cb7e0d34655576d2246d6f043a')))"
0 0
```
Zero occurrences in either byte order, across the entire 850.3s log — the streak `REVERSE_ENGINEERING.md`'s
`gbm` entry documents (26+ prior files) continues unbroken into a 27th/28th file.

## 4. Why step 3 (app-open) produced no second SDP browse — a specific, wire-confirmed mechanism

```
$ tshark -r CAP-044-btsnoop_hci.log -Y "bthci_evt.code==0x03 and bthci_evt.bd_addr==04:00:6e:cf:6e:07"
    -T fields -e frame.number -e frame.time
2107  13:00:17.19   2117  13:00:22.33   2131  13:00:27.48   2182  13:00:56.83
2194  13:01:01.96   2204  13:01:07.09   3268  13:06:50.60   3644  13:07:29.24
3982  13:08:05.64

$ tshark -r CAP-044-btsnoop_hci.log -Y "btrfcomm.frame_type==0x3f"   # DISC (channel close)
(zero results)
```

The classic ACL connection that forms at **13:08:05.64** (the one immediately preceding the
successful bonding+SDP browse) is the **last** Connection-Complete event in the entire 850.3s log —
there is no reconnect afterward, all the way through the log's end (13:13:18.78, 4m40s past video
end). **Every RFCOMM channel that opens during the 13:08:09–13:08:18 SDP/setup burst (DLCI
`0x00`/`0x02`/`0x04`/`0x08`/`0x0a`/`0x0c`) stays open for the rest of the log — zero `DISC` (channel
close) frames appear anywhere.** Force-stopping the companion app's UI process (~13:10:16–21, §1)
does **not** tear down these RFCOMM channels or the underlying classic ACL link — consistent with
those channels being owned by the OS Bluetooth stack / Google Play services rather than the
foreground app process. **Consequently, step 3's app reopen (~13:10:24) attaches to an
already-connected, already-SDP-browsed session — it has no reason to trigger a fresh over-the-air SDP
transaction, and the wire log confirms it does not.**

This is itself a useful, if negative, result for `SDP-001`'s original comparison goal (does the
UUID set the app sees via its own `fetchUuidsWithSdp()` differ from the system's own browse?): **that
comparison cannot be made from a fresh over-the-air fetch in this session's specific circumstances**,
because the classic connection never dropped between the system-triggered pairing and the later
app-open. Either `fetchUuidsWithSdp()` returns Android's already-cached UUID set without re-querying
the peer when the device is already connected (the most parsimonious explanation, consistent with
Android's public `BluetoothDevice.fetchUuidsWithSdp()` semantics), or the app's own registered
service intents simply never fire a second time for an already-known device — either way, no second,
independently-comparable SDP transaction exists in this log to check against.

## 5. `SDP-001` conclusion — 🟡 HYPOTHESIS, not a clean FACT either way, but for a different reason than `CAP-033`, and with one genuinely new positive lead

- **What actually happened, per §1/§2:** the isolation gap in this session is that no Force-stop is
  confirmed to cover the ~2m8s window between the in-app "Forget" and the actual "Pair" tap that
  triggers the SDP browse — not (as `CAP-033` had) a Forget-before-Force-stop ordering violation, and
  not (as `CAP-033` had) a never-executed step 3. This session fixes both of `CAP-033`'s specific
  gaps (§1.1/§1.3 there) but introduces a different one of its own.
- **What the SDP browse itself looked like, per §3, is a genuinely new and informative data point,
  independent of the isolation question**: this session's SDP transaction is a set of *targeted*,
  one-UUID-at-a-time queries (GSND CONTROL, GSND AUDIO, GFPS RFCOMM ×2, MAESTRO APP) rather than
  `CAP-033`'s single generic "browse all services" wildcard. 🟡 **HYPOTHESIS, proposed for maintainer
  review, not asserted as FACT:** a targeted, known-UUID-by-UUID query pattern reads as more
  consistent with software that already knows which specific services it's looking for (e.g. the
  companion app's own `fetchUuidsWithSdp()`/service-intent-driven lookups, per this Group's own
  stated purpose) than with Android's own default, generic pairing-flow SDP browse — this is a
  plausible reading, not a proven one, since this project has no independent capture of what a
  *confirmed*, unambiguously OS-only SDP browse looks like in querying style (only `CAP-033`'s single
  generic-wildcard example, itself not confirmed to be OS-only given that session's own isolation
  gap).
- **Net effect on `SDP-001`'s original question:** still not cleanly closed either way, for a second
  consecutive attempt — but the specific *character* of the failure has changed in a useful way. Two
  independent sessions have now each captured a different plausible SDP interaction style
  (`CAP-033`: one generic wildcard browse; `CAP-044`: five targeted per-UUID queries), and neither
  session achieves the clean "app confirmed non-existent as a process for the entire window"
  isolation `SDP-001` was designed around. **The "default" UUID's absence, however, is now confirmed
  a 5th consecutive time** (🟢 FACT, unaffected by the isolation gap — that is a checked negative
  regardless of who triggered the browse).
- **Procedure-feasibility question, addressed as instructed by the maintainer's Context (proposal
  only, not a unilateral edit):** across two independent, good-faith attempts (`CAP-033`,
  `CAP-044`), neither has managed a browse window with the companion app process *confirmably* absent
  for its entire duration. `CAP-033`'s gap was a sequencing mistake; this session's gap is more
  structural — Android's own UI flow (Forget only being reachable *from inside* the already-bonded
  device's own companion-app screen, on this OS/hardware combination, as this prompt's own Context
  section anticipated) means every attempt so far has needed to *open* the app at least once
  (to tap Forget) before the isolated re-pairing window even begins, and nothing in the observed
  UI flow gives a clean, app-independent way to confirm the app's process death immediately before
  the "Pair" tap. **Proposed, for maintainer review:** `SDP-001`'s procedure could add an explicit
  verification step (e.g. checking `adb shell dumpsys activity processes | grep -i pixelbuds` — or an
  equivalent on-device check — immediately before tapping "Pair") to close this specific evidentiary
  gap in a 3rd attempt, rather than relying on elapsed time alone. This is offered as a
  procedure-improvement proposal, not a decided change to `CAPTURE_BLUETOOTH_HCI_SNOOP.md` Group AA
  or `TESTPLAN_BLUETOOTH_HCI_SNOOP.md`'s `SDP-001` row.

## 6. `SDP-002` — not attempted

No firmware update was pending or available at capture time, same as `CAP-033-FINDINGS.md` §6.
Recorded as `not attempted`, not forced or left silently blank.

## 7. Cross-check against `PROTOCOL.md`/`DECISIONS.md`

No contradiction with any existing 🟢 FACT claim. This session **strengthens** `DECISIONS.md`
ADR-018 (DLCI 0x02 channel ownership — a further independent SDP+service-name confirmation) and
`CAP-033-FINDINGS.md` §3's "GSND CONTROL"/"GSND AUDIO" naming lead (now 2-for-2 across independent
sessions) without promoting either beyond its current status — no FACT promotion or `DECISIONS.md`
ADR is proposed by this document, per `AGENTS.md` §6.

## 8. Bonus/incidental findings

- **Firmware confirmed on-wire this session too** (🟢 FACT): DLCI 0x08 Group `0x03` Code `0x02`,
  frame 4416 (13:08:09.717) — `72656c656173655f352e323033` = `"release_5.203"`, matching the
  maintainer-supplied session context and `DECISIONS.md` ADR-012's established decode.
- **Pre-video connection churn (12:59:08–13:01:07 local, entirely before the video's own 13:05:51
  start):** six classic Connection-Complete events to the Buds' address in the log's first two
  minutes, with no SDP traffic and no bonding event anywhere near them — plausibly leftover
  connect/reconnect activity from before this specific test's own on-camera procedure began (the
  device may still have been bonded from a prior session at that point). Not investigated further —
  out of this capture's own stated scope, and pre-dating the video's own documented procedure.
- **Repeated connect/disconnect cycling during the pairing attempt itself** (connect 13:06:50 →
  disconnect 13:07:21; connect 13:07:29 → disconnect 13:07:59; connect 13:08:05 → stays connected):
  plausibly ordinary Fast-Pair-driven background connection attempts occurring while the "Pair with..."
  dialog sat untapped (§1) — not decoded further, out of scope for `SDP-001`/`SDP-002`.

## 9. Open Questions (carried forward / new)

- 🔴 Was the companion app's process still resident (backgrounded, not force-stopped) during the
  13:05:59–13:08:07 window, or independently reclaimed by Android before the "Pair" tap? Not
  determinable from video or wire log (§2).
- 🔴 Does a targeted, per-UUID SDP query pattern (this session) reliably distinguish an
  app/GMS-driven SDP fetch from the OS's own default pairing browse, or can the OS's own default flow
  also produce targeted queries under some conditions? Only two data points exist so far
  (`CAP-033`: one generic wildcard; `CAP-044`: five targeted queries) — not enough to generalize.
- 🔴 Whether `BluetoothDevice.fetchUuidsWithSdp()` (or an equivalent call) genuinely returns a cached
  result without a fresh over-the-air query when the device is already connected (§4's proposed
  explanation) — plausible but not independently verified against Android's own platform source or a
  capture that forces a fresh disconnect/reconnect immediately before an app-open.
- 🔴 "DEBUG APP" (DLCI 0x06) and "BTIS" (DLCI 0x12) — neither has ever shown wire traffic in any
  capture to date; unchanged by this session (they weren't queried here at all, unlike `CAP-033`'s
  generic browse which did name them).

## 10. Recommended next steps

- A third `SDP-001` attempt with an explicit, on-device process-liveness check (e.g. `adb shell dumpsys
  activity processes`) immediately before the "Pair" tap, to close the specific evidentiary gap this
  session leaves open (§5's proposal) — maintainer decision needed on whether this is worth a third
  attempt or whether two consistent "default UUID absent" results are sufficient to consider that
  specific sub-question settled regardless of the isolation gap.
- If a third attempt is not pursued, propose to the maintainer that `SDP-001`'s remaining open
  question be narrowed from "does the UUID set differ by triggering path" (seemingly not answerable
  with this hardware/OS's UI constraints) to "has the default UUID ever appeared, under any
  triggering path, in 5 independent sessions" — which already has a clean, repeatedly-confirmed
  negative answer.

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/captures/CAP-044-2026-09-13_13-05-51_13-11-07-Group_AA/CAP-044-FINDINGS.md - https://tedsluis.github.io/opencontrolpixelbudspro2/captures/CAP-044-2026-09-13_13-05-51_13-11-07-Group_AA/CAP-044-FINDINGS
