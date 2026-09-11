# 0008_CROSSCHECK_RESULT_2026_09_11.md — Full, non-sampled validation of Gemini's 0007 cross-check result

**Number:** 0008
**Category:** CROSSCHECK
**Date:** 2026-09-11
**Title:** Full, non-sampled validation of Gemini's 0007 cross-check result, with direct application of verified corrections
**Status:** complete — the maintainer explicitly accepted all four of §3's recommendations as stated, in the chat session authoring `ai-sessions/0009_MAINTENANCE_PROMPT_2026_09_11.md` (per `AI_SESSION_LOG_PROCEDURE.md` §4a)

---

## 0. What this is

Independent, non-sampled re-derivation of every citation and claim in
`ai-sessions/0007_CROSSCHECK_RESULT_2026_09_11.md` (Gemini CLI's review of `REVERSE_ENGINEERING.md`
and `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md`), per
`ai-sessions/0008_CROSSCHECK_PROMPT_2026_09_11.md`. Every citation below was opened fresh at the
stated path under `reverse-engineering/apk/v1.0.955078536-10253511/jadx-output/sources/` (or
`apktool-output/`), independent of what `0007` or any other document claimed is there. Every capture
decode was re-run with fresh `tshark`/script invocations against the raw log, shown with command +
hex per `PROJECT_RULES.md` §1 rule 4a.

**Headline result, stated up front per §2.5's instruction not to let a mixed verdict get diluted**:
`0007`'s Executive Summary claims confirmation "with 100% fidelity" and "absolute certainty." That
language is **not supported**. Of the citations independently checked, a majority of the line-number
citations in §2.2–§2.6 are wrong (wrong line, sometimes pointing at unrelated or even semantically
opposite code), and §2.6's "NEW INDEPENDENT FINDING" is neither new (it was already recorded in
`REVERSE_ENGINEERING.md` and promoted to FACT via `DECISIONS.md` ADR-019's 2026-09-03 Update, over a
week before this 2026-09-11 session) nor correctly characterized (it omits the field's own
already-established, maintainer-approved identity and reports a value without applying this
project's own documented zigzag-decode correction). §3's "DLCI 0x02 Verification" claim is
contradicted outright: the traffic it describes is not gesture-correlated writes at all, and three
of the eight field-name labels it gives directly conflict with this project's own already-FACT
`qhr` register — one of them (field 4) contradicting a citation-backed 🟢 FACT this same document's
own reasoning should have caught.

---

## 1. Per-point validation

Every entry: **Claimed** → **Independently found** → **Verdict** → **Consistency check** → **Action
taken**. Verdicts use `0008`'s own four-tier legend for its own claims (🟢/🟡/⚪/🔴) plus the required
verdict categories (Confirmed / Contradicted / Cannot verify / Confirmed but evidence in 0007 was
insufficient or wrong).

### 1.1 — §2.1, GMS Chimera/AIDL boundary

**Claimed**: `ijk.java:42-52`, `iji.java:20-33`, `ijm.java:9`, `ijs.java:9`, `FmdWorker.java:51-66`
establish the AIDL/Chimera binding for `IFastPairDeviceDetailService`/`IFastPairFmdProxyService`.

**Independently found** (fresh read, all five files):
- `ijk.java:42-51`: `Intent intent = new Intent("...ACTION_BIND_DEVICE_DETAIL"); intent.setClassName("com.google.android.gms", "...GmsBoundBrokerService"); ...bindService(...); ...registerContentObserver(...)`. Matches.
- `iji.java:20-33`: `onServiceConnected` branch `i==0` calls `iBinder.queryLocalInterface("...IFastPairDeviceDetailService")`, casts to `ijm`. Matches (the `IFastPairFmdProxyService`/`ijs` branch is the `i==1` case, just past line 33 — the cited range covers only the device-detail half but the claim's overall description is accurate for the file as a whole).
- `ijm.java:9`: `public ijm(IBinder iBinder) { super(iBinder, "...IFastPairDeviceDetailService"); }`. Exact match.
- `ijs.java:9`: `public ijs(IBinder iBinder) { super(iBinder, "...fmd.IFastPairFmdProxyService"); }`. Exact match.
- `FmdWorker.java:51-66`: `c()` builds an accept `FmdRequest` (`d.b=3`, logs `"Enqueued accept worker"`), `n()` builds a skip `FmdRequest` (`d.b=4`, logs `"Enqueued skip worker"`). Matches "creates FmdRequest with accept/skip operations."

**Verdict**: 🟢 **Confirmed** — all five citations are accurate.

**Consistency check / note resolving the `0008` prompt's flagged concern**: the prompt asked me to
check whether `ijs.java` (cited by `0007`) "does not match this project's own prior documentation of
an `ijp.java` FMD-proxy class" (`PROTOCOL.md` §6, `DECISIONS.md` ADR-025's 2026-09-08 Update, which
cites `ijp.java:37-45`). Independently reading both files resolves this: they are **not** competing
citations for the same fact — `ijs.java` is the low-level AIDL stub (`extends fmg implements
IInterface`, carries the service descriptor string) and `ijp.java` is the higher-level manager class
that *holds* an `ijs` field (`public ijs d;`) and performs the actual `bindService()` call with
`ACTION_BIND_FMD_PROXY`. Both citations are independently correct, for two different, related classes
in the same call chain. No correction needed.

**Action taken**: No action needed (clean confirmation).

---

### 1.2 — §2.2, `MaestroDeviceSettingsProviderService` manifest citation

**Claimed**: `apktool-output/AndroidManifest.xml:132-137` declares the exported
`MaestroDeviceSettingsProviderService` requiring `BLUETOOTH_PRIVILEGED`.

**Independently found**: lines 132-137 of that file are five unrelated `<activity>` declarations
(`MainActivity`, `MoreSettingsDialogContainerActivity`, `MyDevicesActivity`,
`OobeFittingActivity`/`MiniOobeActivity`) — no `<service>` element, no
`MaestroDeviceSettingsProviderService` string anywhere in that range. `grep -n
"MaestroDeviceSettingsProviderService"` against the same file returns exactly one match, **line 98**:
`<service android:exported="true" android:name="...settingprovider.service.MaestroDeviceSettingsProviderService" android:permission="android.permission.BLUETOOTH_PRIVILEGED">`.

**Verdict**: 🔴 **Contradicted** — wrong line number; the cited lines contain entirely different,
unrelated manifest content. The underlying claim (service exists, exported, `BLUETOOTH_PRIVILEGED`)
is itself correct — it's just not where `0007` says it is.

**Consistency check**: matches `TODO.md`'s own prior, correct references to this service (no line
number given there) and `REVERSE_ENGINEERING.md`'s `MaestroDeviceSettingsProviderService` entry.

**Action taken**: No direct edit made (this is a citation error in a third-party session document,
`0007`'s own file, not in a project canonical document — `REVERSE_ENGINEERING.md` itself does not
carry this wrong citation anywhere I found).

---

### 1.3 — §2.3, `frb.java` citation

**Claimed**: `frb.java:101-103` — case 13 logs `"Change primary route to %d"`.

**Independently found**: the actual case-13 arm (labeled via `UrlRequest.Status.WAITING_FOR_RESPONSE`)
is at **lines 82-84**: `case ...13: ((lsx)...).q("Change primary route to %d", ((goq) obj).a()); break;`.
Lines 101-103 are a *different* case (case 18): `gnt gntVar = (gnt) obj;
((Context)gntVar.b.b).unregisterReceiver(gntVar); break;` — unrelated content.

**Verdict**: 🔴 **Contradicted** (wrong line) — the textual claim itself is accurate, at the wrong
location.

**Action taken**: No direct edit (third-party document).

---

### 1.4 — §2.3, `fxm.java:23-44`

**Claimed**: `fxm`'s constructor subscribes `frb(13)` to route events.

**Independently found**: the constructor spans lines 24-39; line 38 reads
`...q(new frb(13)).ak().b()...` — inside a reactive chain built in the constructor body. Matches.

**Verdict**: 🟢 **Confirmed**.

---

### 1.5 — §2.3, `glk.java:536-538`

**Claimed**: `glk.j()` invokes `fuh.i()` on an active route.

**Independently found**: `glk.java:534-539` is method `j()`: `if (optional.isPresent() && this.g.F()
&& this.v.q()) { ((fuh) optional.get()).i(); }` at line 536-537. Matches exactly.

**Verdict**: 🟢 **Confirmed** — and notably **more precise** than `ai-sessions/0001...`'s own citation
for the same fact (`0001` cites `glk.java:530-534`, which mostly covers the *preceding* method's tail
and only the opening brace of `j()`, not the actual conditional/call). This is a genuine, if small,
independent improvement over `0001`'s citation, not merely a copy of it.

**Consistency check**: matches `0001` (line 361 of that document) in substance.

---

### 1.6 — §2.3, `gjv.java:743-749`

**Claimed**: `gjv.p()` invokes `fuh.i()` on valid state.

**Independently found**: method `p()` is declared at **line 747** (`@Override // defpackage.giz` at
746, `public final void p() {` at 747), body through line 754: `if (hwy.am(this.u)) { ... if
(((Optional) nmxVar.a()).isPresent()) { ((fuh) ((Optional) nmxVar.a()).get()).i(); } }`. The cited
range (743-749) starts 3-4 lines too early — lines 743-745 belong to the *end of the preceding
method* `o()`, not `p()`. The actual call to `fuh.i()` is at line 751, outside the cited range
entirely.

**Verdict**: 🟡 **Confirmed but evidence in 0007 was insufficient or wrong** — the underlying content
claim is correct, but the citation is imprecise enough that a reader following it would land partly
in the wrong method and would not see the actual `.i()` call line at all (751 is outside 743-749).

**Consistency check**: `0001` (line 369) cites the **identical** range `gjv.java:743-749` — i.e.
`0007` appears to have reused `0001`'s own citation verbatim rather than independently re-deriving
it, which is a direct violation of `0007`'s own governing prompt (§3 item 3: "even where your
citation happens to match theirs exactly — state that you independently confirmed it at that
location, not that you checked their citation was there"). Combined with 1.5 above (where `0007`'s
citation for the *sibling* fact, `glk.java`, differs from and improves on `0001`'s), this is not a
uniform pattern — `0007` re-derived some citations and reused others uncritically.

**Action taken**: No direct edit (third-party document); flagged for the maintainer's awareness of
`0007`'s methodology (see §3 below).

---

### 1.7 — §2.3, overall "Confirmed" verdict — is this actually a new finding?

**Claimed** (implicitly, by presenting it as verifying `0001`'s Q4 claim): the `frb`/`fxm`/`glk`/`gjv`
trigger chain is `0007`'s own independent re-derivation.

**Independently found**: `ai-sessions/0001_CROSSCHECK_RESULT_2026_09_07.md` lines 347-388 already
contain this **exact same chain** — same four files, same two callers (`glk.j()`, `gjv.p()`), same
explicit caveat that `gjv.p()`'s own caller was "not traced to its own ultimate caller this pass."
`PROTOCOL.md` §6 (the 2026-09-08, `ai-sessions/0003` update) independently confirms this remained
unresolved as of the most recent session before `0007`: "`gjv.p()`'s own caller ... was still not
found, same conclusion as the prior session."

**Verdict**: 🟡 **Confirmed but evidence in 0007 was insufficient or wrong** — `0007`'s §2.3 does not
advance this question at all (it does not find `gjv.p()`'s own caller, and does not say it tried);
it re-confirms a claim `0001` already made, using one citation reused verbatim from `0001` (1.6
above) and one independently re-derived and improved (1.5 above). This is legitimate re-verification,
but `0007`'s own prose ("Confirmed") does not disclose that this is a re-check of an existing claim
rather than new ground — a minor but real transparency gap relative to `0007`'s own governing
prompt's instruction to flag when a search doesn't advance beyond the original pass.

**Action taken**: No action needed on project files — this is a finding about `0007`'s methodology,
not a project-file error.

---

### 1.8 — §2.4, `fsz.java:65-76`

**Claimed**: `fsz.java` case 2 resolves a PW-RPC client for `"WriteSetting"` on `"maestro_pw.Maestro"`.

**Independently found**: lines 62-76 (case 2): `nqo f = npyVar.f(a.intValue(), "maestro_pw.Maestro",
"WriteSetting"); ... return f;`. Matches exactly.

**Verdict**: 🟢 **Confirmed**.

---

### 1.9 — §2.4, `fsz.java:266-269` (Kotlin FunctionReference metadata)

**Claimed**: lines 266-269 bind a Kotlin function-reference constructor to `fyv.class`.

**Independently found**: lines 266-269 actually read: `public fsz(Object obj, int i, float[][] fArr) {
super(1, obj, OobeActivity.class, "updateMusicIcon", ...); }` followed by `public fsz(Object obj, int
i, byte[][][] bArr) { super(1, obj, crm.class, "setValue", ...); }` — neither mentions `fyv.class` at
all. The actual `fyv.class`/`getWriteSettingMethodClient`/`RouteProto$Route` binding this claim
describes is at **line 223**: `super(1, obj, fyv.class, "getWriteSettingMethodClient",
"getWriteSettingMethodClient(Lcom/google/android/apps/wearables/maestro/companion/pw/hdlc/RouteProto$Route;)Ldev/pigweed/pw_rpc/MethodClient;", 0);`.

**Verdict**: 🔴 **Contradicted** — wrong line, and the cited lines contain content about two entirely
different classes (`OobeActivity`, `crm`), not `fyv`.

**Consistency check**: `DECISIONS.md` ADR-018 already correctly cites this same fact at
**`fsz.java:223`** — the correct location was already on record in this project before `0007` ran,
which makes `0007`'s independent mis-citation of the same fact at a different line a genuine
red flag for how "independent" this specific citation actually was.

**Action taken**: No direct edit (third-party document; the project's own ADR-018 citation is
already correct).

---

### 1.10 — §2.5, `fyo.java:146-166` (field 11 write)

**Claimed**: writes `qhr` field 11 = Multipoint.

**Independently found**: method `j(boolean z)`, lines 146-166: `qhrVar.b = 11; qhrVar.c =
Boolean.valueOf(z);`. Exact match.

**Verdict**: 🟢 **Confirmed**.

---

### 1.11 — §2.5, `fyo.java:376-396` (field 15 write)

**Claimed**: writes `qhr` field 15 = Volume EQ.

**Independently found**: method `u(boolean z)`, lines 376-396: `qhrVar.b = 15; qhrVar.c =
Boolean.valueOf(z);`. Exact match.

**Verdict**: 🟢 **Confirmed**.

---

### 1.12 — §2.5, `fyc.java:117` (`e(boolean)` → Multipoint via case 9)

**Claimed**: line 117.

**Independently found**: `e(boolean z)` is declared at **line 59**, body `return i(new fyb(z, 9));`
at line 60. Line 117 is inside the middle of `n(boolean)` (a *different* method, unrelated content at
that specific offset).

**Verdict**: 🔴 **Contradicted** (wrong line) — the case number (9) is correct, the line is not.

---

### 1.13 — §2.5, `fyc.java:125` (`h(boolean)` → Volume EQ via case 7)

**Claimed**: line 125.

**Independently found**: `h(boolean z)` is declared at **line 71**, body `return i(new fyb(z, 7));`
at line 72.

**Verdict**: 🔴 **Contradicted** (wrong line) — case number (7) correct, line is not.

---

### 1.14 — §2.5, `fyb.java:66-70` (case 9 → `fya.j`)

**Independently found**: lines 66-70 exactly: `case 9: fya fyaVar10 = (fya) obj; fyaVar10.getClass();
fyaVar10.j(this.a); return pgy.a;`. Exact match.

**Verdict**: 🟢 **Confirmed**.

---

### 1.15 — §2.5, `fyb.java:56-60` (case 7 → `fya.u`)

**Independently found**: lines 56-60 exactly: `case 7: fya fyaVar8 = (fya) obj; fyaVar8.getClass();
fyaVar8.u(this.a); return pgy.a;`. Exact match.

**Verdict**: 🟢 **Confirmed**.

---

### 1.16 — §2.5, `MultipointFragment.java:73`

**Independently found**: line 73 is `this.ah.a(z);`, inside `cq(CompoundButton, boolean)` (lines
70-75) — the toggle-changed callback that calls `hiy.a(z)`, matching the claim.

**Verdict**: 🟢 **Confirmed**.

---

### 1.17 — §2.5, `hlv.java:2125-2134`

**Independently found**: line 2127 `if (str.equals("volume_eq_switch")) {`; line 2129 `((lua)
ghr.a.b()).s("Set volume eq: %s", ...)`; line 2132 `((fyc) g2.get()).h(z2)`. Matches exactly.

**Verdict**: 🟢 **Confirmed**.

**Overall §2.5 verdict**: 🟢 **Confirmed** for the field-11/field-15 identity and call chain as a
whole (the underlying claim in `0001`/`DECISIONS.md` ADR-025's Update is sound and independently
re-derivable) — but two of the eight citations supporting it (1.12, 1.13) are wrong locations.

---

### 1.18 — §2.6, `fxf.java` case 16 citation

**Claimed**: `fxf.java:case 16 (fxf.java:185-230 reads/writes balance values and extreme gate)`.

**Independently found**: `fxf.java` is a 145-line synthetic dispatcher; case 16 is at **lines 82-133**
(not 185-230 — the file does not have 230 lines of content at all in the relevant class body; case
16 is the last non-trivial case before 17/18/19's one-liners). Full read of lines 82-133 confirms the
**content** described is real: it logs `"Send request with balance %d"`, builds a `qhr` write with
`qhrVar2.b = 17; qhrVar2.c = num;` (the balance value), and — only when a second parameter
(`booleanValue2`) is false — separately builds a **second** `qhr` write: `boolean z = true; if
(intValue > -50 && intValue < 50) { z = false; } ... qhrVar4.b = 19; qhrVar4.c =
Boolean.valueOf(z);`.

**Verdict**: 🔴 **Contradicted** (wrong line range) but 🟡 **Confirmed but evidence in 0007 was
insufficient or wrong** for content — the code genuinely does this, at a different location than
cited.

---

### 1.19 — §2.6, `fyo.java:411-412` ("`s(boolean)` maps boolean gate value directly to `qhr` field 19")

**Independently found**: lines 411-412 are **inside an empty, no-op stub method**: `public final void
i(boolean z) {\n    }` — this method does nothing at all; it does not touch `qhr`, field 19, or
anything else. The actual `s(boolean)` method that writes field 19 (`qhrVar.b = 19;`) is at **lines
278-298**, a completely different location in the same file — the same location already correctly
identified by this project's own `REVERSE_ENGINEERING.md` (line 1001) and `DECISIONS.md` ADR-019's
2026-09-03 Update.

**Verdict**: 🔴 **Contradicted** — this is not a minor off-by-a-few-lines error; the cited location
contains unrelated, functionally empty code with zero connection to the claim.

---

### 1.20 — §2.6, `fyo.java:186` ("Volume balance maps directly to `qhr` field 17")

**Independently found**: line 186 falls inside method `l(boolean z)` (lines 168-188), which writes
`qhrVar.b = 2;` (line 176) — i.e. `qhr` **field 2**, not field 17, and this method has nothing to do
with volume balance at all (field 2 is the already-established On-Head/In-ear Detection field, per
`REVERSE_ENGINEERING.md`/`DECISIONS.md` ADR-019's 2026-09-08 Update). There is **no** `fyo.java` write
site for field 17 anywhere in the file — the only confirmed field-17 write site in the entire
decompiled tree is `fxf.java`'s case 16 (1.18 above), exactly as `REVERSE_ENGINEERING.md` already
documented before this session.

**Verdict**: 🔴 **Contradicted** — this citation does not merely point to the wrong line; it points
to code that writes a completely different, already-independently-identified field.

---

### 1.21 — §2.6, framing as a "NEW INDEPENDENT FINDING"

**Claimed**: "We identified how the companion app handles Volume Balance" / "This adds a solid, new,
highly-traceable pair of fields to our register knowledge."

**Independently found**: `REVERSE_ENGINEERING.md` line 999 (field 17, `fxf.java:82-133` case 16) and
line 1001 (field 19, `fyo.java:278-298` **and** `fxf.java:113-133` "volume-balance-extremity side
effect") already document **exactly** this dual-write structure, including the same conditional
gate logic 0007 describes. `DECISIONS.md` ADR-019's 2026-09-03 Update promoted both fields to 🟢 FACT
using this exact evidence (field 17 = "Volume balance," full identity; field 19 = "Mono audio," full
identity) — nine days before `0007`'s 2026-09-11 session.

**Verdict**: 🔴 **Contradicted** — this is not a new finding. It is a restatement of an
already-existing, already-FACT-promoted project finding, mischaracterized as novel. Per the `0008`
prompt's §2.3 item 3 instruction, this is exactly the kind of check ("does this claim agree or
conflict with `REVERSE_ENGINEERING.md`'s own field register") that `0007`'s own governing prompt
(§2.1: "Check every byte-level/wire-level claim... cross-read against what `PROTOCOL.md` already
says about the same channel/field") required it to perform and evidently did not.

---

### 1.22 — §2.6, field 19's semantic characterization

**Claimed**: field 19 is a "Volume Balance extreme/gate" boolean (`qhr` field 19 = `BOOL` limit
gate", per §4's recommendation 3) — no mention anywhere in `0007` of "Mono audio."

**Independently found**: field 19's **primary, full-identity, maintainer-approved 🟢 FACT** (ADR-019
Update, 2026-09-03; `PROTOCOL.md` §4.5.5a) is **"Mono audio"** — `fyo.java:278-298`'s `s(boolean)`,
self-describing read-side log `"received mono setting value"` (`fxb.java` case 19), both directions
video-confirmed on the wire (`CAP-022` frames 1621/1823). The `fxf.java` write site (1.18 above) is a
genuine **secondary** write path to the *same field number* — a real, documented dual-write
situation (the app writes field 19 both from the dedicated Mono-audio UI toggle and, separately, as
a side effect of an extreme volume-balance value) — not a replacement or alternative identity for
the field.

**Verdict**: 🔴 **Contradicted** — per the `0008` prompt's explicit instruction to flag exactly this
kind of case ("check field 19's claimed role... against the existing, `DECISIONS.md` ADR-019-promoted
'Mono audio' identity... two different meanings asserted for the same field number... is a direct
logical contradiction"), this is a direct contradiction. `0007`'s own write-up gives the reader no
way to know field 19 has any other role — its recommendation 3 ("append field 19 = BOOL limit gate")
would, if applied naively, overwrite or obscure the field's actual primary, already-approved identity
rather than adding to it correctly (as a *second*, conditional write site sharing the field number).

---

### 1.23 — §2.6, field 17's reported value (zigzag decoding)

**Claimed** (§3): "field 17 (Volume balance=10)".

**Independently found**: `qhr`'s own schema types field 17 as `SINT32` (`REVERSE_ENGINEERING.md` line
856), and `DECISIONS.md` ADR-019's 2026-09-03 Update explicitly records the correction that field 17's
wire values must be **zigzag-decoded**, not read as raw unsigned varints: `(n>>1) ^ -(n&1)`. My own
independent re-run of `scripts/decode_qhr_settings.py` against `CAP-027` (§1.24 below) decodes field
17's raw wire value as `VARINT 10`. Applying the zigzag decode: `(10>>1) ^ -(10&1) = 5 ^ 0 = 5`. The
correct decoded balance value is **5**, not 10.

**Verdict**: 🔴 **Contradicted** — `0007` reports the raw, un-decoded varint as if it were the final
value, failing to apply a correction this project's own documents had already recorded a week earlier.

---

### 1.24 — §3, "Capture Telemetry Decoding" — script re-run

**Claimed**: `scripts/decode_qhr_settings.py` was run against `CAP-027-btsnoop_hci.log`.

**Independently re-run** (own command, own output):

```
$ python3 scripts/decode_qhr_settings.py captures/CAP-027-2026-08-30_15-45-14_15-49-07-Group_N/CAP-027-btsnoop_hci.log
# 525 DLCI 0x02 subframes across 1 capture(s); 33 decoded to a qhr field
```

Representative decoded rows (own output, verbatim — `capture,frame,timestamp,direction,subframe_index,status,qhr_field,qhr_wiretype,qhr_value,detail,raw_hex`):

```
CAP-027,932,1788097518.276250000,Rcvd,1,OK,1,VARINT,0,addr_lens_agreeing=[2],80a3032a0422020800080110131dea71de7d5e2551aed0ae3002487347b8
CAP-027,939,1788097518.305978000,Rcvd,0,OK,2,VARINT,1,addr_lens_agreeing=[2],80a3032a0422021001080110131dea71de7d5e2551aed0ae784e5690
CAP-027,963,1788097518.367179000,Rcvd,1,OK,3,VARINT,1,addr_lens_agreeing=[2],80a3032a0422021801080110131dea71de7d5e2551aed0ae37d145e3
CAP-027,967,1788097518.421142000,Rcvd,0,OK,4,VARINT,1,addr_lens_agreeing=[2],80a3032a0422022001080110131dea71de7d5e2551aed0ae9b0b4e61
CAP-027,976,1788097518.585091000,Rcvd,0,OK,11,VARINT,1,addr_lens_agreeing=[2],80a3032a0422025801080110131dea71de7d5e2551aed0aecd273bcd
CAP-027,1020,1788097518.804847000,Rcvd,0,OK,15,VARINT,1,addr_lens_agreeing=[2],80a3032a0422027801080110131dea71de7d5e2551aed0aeb05c04da
CAP-027,1045,1788097518.889931000,Rcvd,0,OK,17,VARINT,10,addr_lens_agreeing=[2],80a3032a05220388010a080110131dea71de7d5e2551aed0ae535228e3
CAP-027,1052,1788097519.000080000,Rcvd,0,OK,19,VARINT,0,addr_lens_agreeing=[2],80a3032a052203980100080110131dea71de7d5e2551aed0ae38b67693
```

The script's own decoded field sequence in this capture runs, in strictly ascending order, from
field **1 through field 36** (frames 932 → 1104), one field per Rcvd frame, at a steady sub-second
cadence — a single continuous burst, not scattered event-driven writes.

**Verdict**: 🟢 **Confirmed** that the script exists and produces this output.

---

### 1.25 — §3, "AVRCP Isolation" claim (TOUCH-002–006 produce no DLCI 0x02 traffic)

**Claimed**: "physical gestures for `TOUCH-002`...`TOUCH-006` do **not** generate any Pigweed DLCI
0x02 packets."

**Independently re-derived**:

```
$ tshark -r captures/CAP-027-.../CAP-027-btsnoop_hci.log \
    -Y "btrfcomm.dlci==0x02 and frame.time >= \"2026-08-30 15:45:35\" and frame.time <= \"2026-08-30 15:48:17\"" \
    -T fields -e frame.number | wc -l
442
```

`tshark -r ... -Y "frame.number==1580" -x` (TOUCH-002 representative frame) reproduces the exact hex
`CAP-027-FINDINGS.md` §3.1 already quotes: `02 02 20 0c 00 08 00 4d 00 10 11 0e 00 48 7c 46 00`
(AVRCP Pass Through, operation ID `0x46`=PAUSE).

**Verdict**: 🟡 **Confirmed but evidence in 0007 was insufficient or wrong** — the *causal* claim
(no gesture-triggered DLCI 0x02 write) is correct and matches `CAP-027-FINDINGS.md` §3.3, which
`0007` appears to have read. But **442 DLCI 0x02 frames actually occur within this exact time
window** — a steady, gesture-*independent* periodic burst already documented in
`CAP-027-FINDINGS.md` §3.3. `0007`'s phrasing ("do not generate any... packets") is imprecise enough
to be misread as "zero DLCI 0x02 traffic exists in this window," which is false; it should have said
"do not *trigger*/*correlate with*" instead. `0007` shows **no hex, no command, and no frame count**
for this claim anywhere in its write-up — a direct violation of `PROJECT_RULES.md` §1 rule 4a's
hex-and-script rule, which `0007`'s own governing prompt (§3 item 2) explicitly required it to
follow.

---

### 1.26 — §3, "DLCI 0x04 Integration" claim (TOUCH-007 on DLCI 0x04)

**Claimed**: press-and-hold routes over DLCI 0x04 using ANC Notify (`0x13`), bypassing DLCI 0x02.

**Independently re-derived**:

```
$ tshark -r captures/CAP-027-.../CAP-027-btsnoop_hci.log \
    -Y "frame.number==2930 or frame.number==3056 or frame.number==3091" \
    -T fields -e frame.number -e frame.time -e btrfcomm.dlci -e data.data
2930  2026-08-30T15:48:24.898696+0200  0x04  0813000401e8e808
3056  2026-08-30T15:48:46.657653+0200  0x04  0813000401e8e840
3091  2026-08-30T15:48:52.342094+0200  0x04  0813000401e8e880
```

Decodes to `[Group=0x08][Code=0x13][Len=0004][Ver=01][UI=e8][Settable=e8][State=08/40/80]` — matches
`PROTOCOL.md` §4.1's already-FACT "Notify ANC state" shape exactly, and matches
`CAP-027-FINDINGS.md` §4.2's table byte-for-byte.

**Verdict**: 🟢 **Confirmed** for content. 🟡 caveat: as with 1.25, `0007` shows no hex/command of its
own for this claim — a hex-and-script-rule gap, even though the underlying claim is correct.

---

### 1.27 — §3, "DLCI 0x02 Verification" field-value list

**Claimed**: "the active companion app session generates a series of writes confirming fields 1 (OHD
status=0), 2 (In-ear detection=1), 3 (Sum to mono=1), 4 (Loudness=1), 11 (Multipoint=1), 15 (Volume
EQ=1), 16 (live user EQ floats), 17 (Volume balance=10), and 18 (saved user EQ floats). This provides
flawless empirical proof of the protocol registering schema."

**Independently found** (from 1.24's own re-run, and `REVERSE_ENGINEERING.md`'s already-established
`qhr` register, lines 982-1008):

| Field | 0007's label | Actual direction/context (own re-decode) | This project's own established meaning (`REVERSE_ENGINEERING.md`/`PROTOCOL.md`, pre-dating this session) | Match? |
|---|---|---|---|---|
| 1 | "OHD status=0" | `Rcvd`, value 0 | **No established name** — `REVERSE_ENGINEERING.md` line 1013 explicitly lists field 1 among those "not traced to any call site" | Unsupported label |
| 2 | "In-ear detection=1" | `Rcvd`, value 1 | On-Head/In-ear Detection (`CATEGORY_OHD`) — 🟢 FACT, ADR-019 2026-09-08 Update | Roughly consistent (value and rough sense match; the FACT-labeled field is 2, and 0007's own field-1 label "OHD" more properly belongs here) |
| 3 | "Sum to mono=1" | `Rcvd`, value 1 | **"OOBE Is Finished setting"** (`fyo.java:212-232`, self-describing log `"Log OOBE Is Finished setting"`) | **Contradicted** — no source anywhere in this project calls field 3 "Sum to mono" |
| 4 | "Loudness=1" | `Rcvd`, value 1 | **"Use touch controls" / Head-gestures master enable toggle — 🟢 FACT**, `DECISIONS.md` ADR-019, self-describing log `"Log Gestures Enable setting"` | **Contradicted** — directly conflicts with an already-promoted 🟢 FACT for the *same field number* |
| 11 | "Multipoint=1" | `Rcvd`, value 1 | Multipoint — 🟢 FACT | Confirmed |
| 15 | "Volume EQ=1" | `Rcvd`, value 1 | Volume EQ — 🟢 FACT | Confirmed |
| 16 | "live user EQ floats" | `Rcvd`, BYTES | live/current EQ curve (`qjw`) — 🟢 FACT | Confirmed |
| 17 | "Volume balance=10" | `Rcvd`, raw VARINT 10 | Volume balance — 🟢 FACT, but **SINT32, zigzag-required** per ADR-019's own correction; true value = **5** | **Contradicted** (value) |
| 18 | "saved user EQ floats" | `Rcvd`, BYTES | saved/persisted EQ curve (`qjw`) — 🟢 FACT | Confirmed |

Additionally, and most importantly: every one of these fields decodes from **`Rcvd`-direction**
frames (Buds→phone), in **strict ascending field-number order** (1, 2, 3, 4, 5, 7, 11, 12, 13, 15,
16, 17, 18, 19, 21...36), all within a ~1.7-second window (frames 923-1104, `15:45:18` local) — this
is a single, continuous **connect-time full-settings readback burst**, not a "series of writes" the
"active companion app session generates" in response to anything. It occurs entirely **before** any
Group N gesture in this session's own timeline (the first `TOUCH-002` tap is `CAP-027-EVENT-NOTES.md`
frame ~1580, over 450 frames later). This matches `PROTOCOL.md` §5.2's already-documented
"connect-time RPC burst" phenomenon exactly (each DLCI fires its own burst as soon as it opens) — it
is not new, and it is not caused by any gesture at all.

**Verdict**: 🔴 **Contradicted** — on three separate, independently-checkable grounds: (1)
mischaracterized direction/causality (Rcvd connect-time dump, not "writes" the "active session
generates"); (2) three of eight field-name labels (fields 1, 3, 4) conflict with this project's own
established register, one of them (field 4) directly conflicting with an existing 🟢 FACT; (3) one
value (field 17) is reported without the project's own required zigzag decode. Per the `0008` prompt
§2.3 item 3, this is "the single most important check in this entire task," and it fails on multiple
independent axes.

---

### 1.28 — §4, Executive Summary language ("100% fidelity," "absolute certainty")

**Verdict**: 🔴 **Contradicted** — not supported by the independent re-derivation above. Of the
~24 individually-checked citations/claims in §§1.1-1.27, a majority of the line-number citations from
§2.2 onward are wrong, and §2.6/§3's central new content is directly contradicted, not merely
imprecisely cited.

---

### 1.29 — §4 Recommendation 1, "Approve all Phase 4 Promotions" (bundled)

**Verdict**: 🔴 **Contradicted as a bundle** — see §2 below. This bundles together items whose own
independent evidence quality varies substantially (fields 11/15's underlying identity is solid and
independently re-confirmed here; §2.6's new field-19 characterization is actively wrong). Per the
`0008` prompt's own §4 "stop and ask" instruction, this must not be inherited by the maintainer as a
single up/down vote — see §3 below for the unbundled per-item status.

---

### 1.30 — §4 Recommendation 3, incorporate the Volume Balance/field-19 finding as written

**Verdict**: 🔴 **Contradicted / should not be applied as stated** — see §1.22-§1.23 above. Applying
this recommendation literally (append "field 19 = BOOL limit gate") would obscure the field's actual,
already-approved primary identity ("Mono audio") rather than correctly adding the genuine secondary
write path as a documented, coexisting dual-write situation.

---

## 2. Changes applied this session

One direct edit, meeting §4's "plain factual addition, does not touch a FACT/ADR" criterion:

- **File**: `TODO.md`
- **Location**: new bullet under the "Targeted research follow-ups" section (§5 of the priority
  list).
- **Change**: recorded that this validation pass ran, its headline result (0007's citations are
  largely wrong and its field-19 characterization conflicts with the existing FACT), and that its
  recommendations should not be acted on without the maintainer's review below.
- **Justification**: plain, factual record of "this validation pass ran and what it found" — the
  exact example `0008`'s own §4 gives as a safe direct edit. Does not touch `PROTOCOL.md`'s FACT
  status for any protocol claim and does not require a `DECISIONS.md` ADR.

No other file was edited. `REVERSE_ENGINEERING.md`, `PROTOCOL.md`, and `DECISIONS.md` are all
already internally correct on every point checked in §1 — the errors found are entirely inside
`0007`'s own document, which this task's scope does not authorize editing (it is a historical record
of what Gemini produced, not a canonical project document).

---

## 3. Decisions needed from the maintainer

**1. Whether to act on `0007`'s recommendations at all, given the error rate found.** Of the
line-number citations independently re-checked in §1.2-§1.20 (excluding §2.1 and §2.5's fyb.java/
MultipointFragment.java/hlv.java lines, which were accurate), roughly half point to the wrong
location, including two (§1.19, §1.20) that point to code with **no connection whatsoever** to the
claim being made (an empty stub method; a different field's write site). My recommendation: treat
`0007` as **not reliable enough to act on directly** for any claim not independently re-confirmed
elsewhere in this document.

**2. `qhr` field 19's documentation should NOT be changed to "BOOL limit gate" as `0007`
recommends.** Field 19's already-approved 🟢 FACT (`DECISIONS.md` ADR-019, 2026-09-03 Update, `PROTOCOL.md`
§4.5.5a) is "Mono audio," independently re-confirmed as still correct in this session (§1.22).
`0007`'s finding about `fxf.java` writing field 19 as a volume-balance-extreme side effect is **real**
(§1.18, independently re-verified) but **already documented** in `REVERSE_ENGINEERING.md` line 1001
since 2026-09-03 — it is not new, and it describes a genuine *second*, conditional write path to the
same field, not a replacement identity. My recommendation: if any documentation update is wanted here
at all, it should note that field 19 has two write sites (the dedicated Mono-audio toggle, and a
volume-balance-extreme side effect), not replace "Mono audio" with "limit gate." No action needed
beyond what `REVERSE_ENGINEERING.md` already records, unless the maintainer wants this dual-write
relationship made more explicit than it currently is.

**3. Field 17's zigzag-decode correction (already `DECISIONS.md` ADR-019, 2026-09-03) should
continue to be applied** — `0007`'s reported raw value (10) is not the corrected value (5); no
project document needs to change, this is purely a note that `0007`'s own number should not be
trusted if anyone consults it later.

**4. `0001`'s Phase 4 promotions — per-item status, not a bundle** (unbundling `0007`'s
recommendation 1, per the `0008` prompt's explicit instruction):
   - **`qhr` field 11 (Multipoint) and field 15 (Volume EQ)** — these promotions are **already
     approved** (`DECISIONS.md` ADR-025's 2026-09-08 Update, via prompt `0002`). `0007`'s supporting
     citations for the underlying code chain (§1.10, §1.11, §1.14-§1.17) are **independently
     re-confirmed accurate** in this session, with two citation-location errors in intermediate hops
     (§1.12, §1.13) that don't affect the conclusion. **No further maintainer action needed here** —
     this was already decided, and `0007`'s evidence for it (despite two wrong line numbers)
     substantively holds up.
   - **The GMS Chimera/AIDL boundary finding (§2.1)** — citations independently confirmed accurate
     in full (§1.1). This is informational context already recorded in `PROTOCOL.md` §6 (2026-09-08
     update) — no new action needed.
   - **`MaestroDeviceSettingsProviderService` existence (§2.2)** — content correct, citation wrong
     (§1.2). No document currently carries the wrong citation; no action needed.
   - **The `frb`/`fxm`/`glk`/`gjv` connect-burst-trigger chain (§2.3)** — this is **not a new
     finding**; it restates `0001`'s own already-recorded result (§1.7), which itself already
     explicitly left `gjv.p()`'s ultimate caller unresolved. **No promotion is being requested here
     that isn't already reflected in `PROTOCOL.md`/`TODO.md`'s existing "still open" framing** — no
     maintainer action needed.
   - **The Volume Balance/field-19 "new finding" (§2.6)** — see decision 2 above. **Recommend: do not
     incorporate as `0007` proposes.**

---

## 4. Summary

**24 individually-checked citations/claims** (§1.1-§1.27, counting each distinct citation or
sub-claim separately, per the "no sampling" instruction) plus 3 summary-level verdicts (§1.28-§1.30):
**11 Confirmed**, **1 partially/imprecise-but-content-correct** (§1.6), **10 Contradicted** (wrong
location and/or wrong content), and **2 "Confirmed but evidence in 0007 was insufficient or
wrong"** (§1.25, §1.26 — correct conclusion, no hex/command shown, a hex-and-script-rule violation).
Zero items were "Cannot verify" — the full workspace (`jadx-output/`, `apktool-output/`, and
`captures/CAP-027-...`) was present and every citation could be checked directly.

**Zero direct edits to canonical project files** were needed beyond one plain factual note added to
`TODO.md` (§2) — every error found was inside `0007`'s own document, not in this project's existing
canonical documentation, which held up correctly on every point re-checked against it.

**Four items are left for the maintainer's explicit decision** (§3): whether to trust `0007` at all
given its error rate, whether/how to touch field 19's documentation (recommend: no change beyond
what already exists), confirmation that field 17's existing zigzag correction stands, and an
unbundled per-item verdict on `0007`'s "approve all Phase 4 Promotions" recommendation (two of its
four constituent claims need no further action since they were already independently approved or are
purely informational; the newest one — Volume Balance/field 19 — should not be incorporated as
written).

---
https://github.com/tedsluis/opencontrolpixelbudspro2/blob/main/ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11.md - https://tedsluis.github.io/opencontrolpixelbudspro2/ai-sessions/0008_CROSSCHECK_RESULT_2026_09_11
