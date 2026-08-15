# DESKRESEARCH_FINDINGS.md

Offline, script-based pattern analyses that correlate or re-examine **existing**
captures — no new Bluetooth capture session involved. This is distinct from a
`CAP-NNN-FINDINGS.md` file, which documents first-pass findings from one
specific capture: an entry here typically spans multiple `CAP-NNN` captures
(e.g. "check this byte pattern across all four existing logs") or re-applies a
new hypothesis to data already on disk.

Findings here are subject to the same rules as anywhere else:
`PROJECT_RULES.md` §1 (FACT/HYPOTHESIS/ASSUMPTION labeling, evidence
traceability) and §2 (findings are promoted directly into `PROTOCOL.md` once
confirmed — there is no intermediate buffer). A deskresearch finding is not a
substitute for a purpose-built capture/experiment where one is warranted — see
`PROJECT_RULES.md` §4 on hypothesis tests; a deskresearch correlation against
existing data is weaker evidence than a fresh, purpose-built capture and
should be labeled accordingly (see e.g. `PROTOCOL.md` §4.1's "Verified with
experiment" note).

Status legend (consistent with `PROTOCOL.md` §0):

- 🟢 **FACT** — observed and repeatedly confirmed.
- 🟡 **HYPOTHESIS** — observed or plausible, not yet independently confirmed.
- ⚪ **ASSUMPTION** — not yet tested, assumed based on comparable/official
  protocols or an older Pixel Buds generation.

---

## Template per entry

```
### <Date> — <short title>

- **Trigger:** why this analysis was run (e.g. an open question in `PROTOCOL.md` §6).
- **Method:** the exact command(s) used (per `PROJECT_RULES.md`'s hex/script
  rule — every decoding of a burst/packet includes the specific
  terminal/python command AND the raw hex bytes it operated on).
- **Captures examined:** which `CAP-NNN` log(s), by ID.
- **Result:** what was found, with status label.
- **Promoted to:** the `PROTOCOL.md` section this was written into, once
  promoted (leave blank until promotion happens).
```

---

## Entries

_(none logged yet in this file — prior deskresearch passes, e.g. the
2026-08-12 and 2026-08-14 cross-capture DLCI 0x02/0x08 analyses, are recorded
as dated addenda directly in `PROTOCOL.md` per its own non-destructive-update
convention; new deskresearch work should be logged here going forward instead
of as inline `PROTOCOL.md` addenda, to keep that document's prose from
accumulating unrelated dated notes.)