"""Layer 2 — smali source reader.

Deliberately NOT a general smali parser (SPEC.md §4's guardrail against scope
creep). This module recognizes exactly three things, and nothing else:

  1. where a class lives on disk, across the split `smali*/` roots (§4.1);
  2. one named method's line range inside that file;
  3. within that method, either a `packed-switch`/`sparse-switch` dispatch on
     a given discriminator register, or a short `if-eq`/`if-eqz` chain on it —
     and, given a discriminator *value*, which branch it resolves to.

Every regex here matches one specific smali construct verbatim, in the exact
form this compiler (R8/D8 via `apktool`'s baksmali) emits it. If a class's
method doesn't match one of these three shapes, this module reports
"ambiguous" (models.ResolutionStatus) rather than guessing — per
`AGENTS.md` §13.6, applied to this tool's own code, not just to protocol
findings.
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

from .androguard_index import dex_position_to_smali_dir
from .models import SmaliEvidence

_METHOD_START_RE = re.compile(r"^\.method\s+.*\s(?P<name>[^\s(]+)\((?P<params>[^)]*)\).*$")
_END_METHOD_RE = re.compile(r"^\.end method$")
_LABEL_RE = re.compile(r"^:(?P<label>\w+)$")

# iget <dst>, <src>, L<class>;-><field>:I  — the discriminator field read.
# <src> may be a local register (v0, v1, ...) or a parameter register (p0, p1, ...);
# this project's own APK uses both, depending on how many locals a method has
# (see SPEC.md/README's note on the `move-object/from16 v0, p0` aliasing step).
_IGET_INT_RE = re.compile(
    r"^iget\s+(?P<dst>[vp]\d+),\s*(?P<src>[vp]\d+),\s*L(?P<cls>[^;]+);->(?P<field>\w+):I$"
)

_SWITCH_OP_RE = re.compile(r"^(?P<kind>packed-switch|sparse-switch)\s+(?P<reg>[vp]\d+),\s*:(?P<data_label>\w+)$")
_PACKED_SWITCH_DATA_START_RE = re.compile(r"^\.packed-switch\s+(?P<start>0x[0-9a-fA-F]+|-?\d+)$")
_SPARSE_SWITCH_DATA_ENTRY_RE = re.compile(r"^(?P<key>0x[0-9a-fA-F]+|-?\d+)\s*->\s*:(?P<label>\w+)$")
_SWITCH_DATA_END_RE = re.compile(r"^\.end (packed|sparse)-switch$")

_CONST_RE = re.compile(r"^const(?:/4|/16|/high16|-wide/16)?\s+(?P<reg>[vp]\d+),\s*(?P<val>0x[0-9a-fA-F]+|-?\d+)$")
_IF_EQZ_RE = re.compile(r"^if-eqz\s+(?P<reg>[vp]\d+),\s*:(?P<label>\w+)$")
_IF_EQ_RE = re.compile(r"^if-eq\s+(?P<reg1>[vp]\d+),\s*(?P<reg2>[vp]\d+),\s*:(?P<label>\w+)$")

# baksmali emits a bare ".line N" directive (source-line-number metadata,
# often several in a row with nothing else on the line) between almost every
# real instruction, plus blank separator lines. Both are inert and must be
# skipped while scanning for the next *real* instruction — never mistaken
# for "the chain ends here".
def _is_noise_line(line: str) -> bool:
    return line == "" or line.startswith(".line ")


class SmaliShapeError(Exception):
    """Raised when the method doesn't match one of §3/§4's recognized shapes.
    Callers turn this into resolution_status="ambiguous", never a guess."""


def locate_smali_file(apk_root: Path, dex_position: int, class_descriptor: str) -> Path:
    """class_descriptor is the smali form, e.g. "Laie;". Returns the absolute
    path apktool decompiled it to, e.g.
    ".../apktool-output/smali_classes2/aie.smali" — raising if it's not there,
    per SPEC.md §7's "this should not happen" failure mode.
    """
    inner = class_descriptor[1:-1]  # "Laie;" -> "aie", "Lfoo/Bar;" -> "foo/Bar"
    smali_dir = dex_position_to_smali_dir(dex_position)
    candidate = apk_root / "apktool-output" / smali_dir / f"{inner}.smali"
    if candidate.is_file():
        return candidate
    # Fall back to searching every smali*/ root, in case the dex-position ->
    # directory mapping this run assumed doesn't hold for some reason
    # (SPEC.md §4.1's own documented fallback).
    for other_dir in sorted((apk_root / "apktool-output").glob("smali*")):
        alt = other_dir / f"{inner}.smali"
        if alt.is_file():
            return alt
    raise FileNotFoundError(
        f"no smali file found for {class_descriptor} under {apk_root / 'apktool-output'} "
        f"(tried {smali_dir} first, then every smali*/ root)"
    )


@dataclass
class MethodBody:
    lines: list[str]  # 0-indexed; lines[0] is line 1 of the file
    start_line: int  # 1-indexed, the ".method" line itself
    end_line: int  # 1-indexed, the ".end method" line itself


def find_method(smali_path: Path, method_name: str) -> MethodBody:
    """Finds the *non-constructor* method named `method_name` (an interface's
    sole abstract method implementation never collides with `<init>`, so this
    alone disambiguates it from the class's constructor(s), per SPEC.md §3.5).
    """
    lines = smali_path.read_text(encoding="utf-8", errors="replace").split("\n")
    start = None
    for i, raw in enumerate(lines):
        line = raw.strip()
        m = _METHOD_START_RE.match(line)
        if m and m.group("name") == method_name:
            start = i
            break
    if start is None:
        raise SmaliShapeError(f"method '{method_name}' not found in {smali_path}")
    for j in range(start, len(lines)):
        if _END_METHOD_RE.match(lines[j].strip()):
            return MethodBody(lines=lines, start_line=start + 1, end_line=j + 1)
    raise SmaliShapeError(f"'.end method' for '{method_name}' not found in {smali_path}")


@dataclass
class DispatchInfo:
    mechanism: str  # "packed-switch" | "sparse-switch" | "if-chain"
    # value -> (label, label_line [1-indexed])
    branches: dict[int, tuple[str, int]]
    default_branch_start_line: int  # 1-indexed; where the switch/if-chain's own code ends
    all_label_lines: list[int]  # every label line belonging to this dispatch, sorted


def _find_discriminator_read(method: MethodBody, class_descriptor: str, field_name: str) -> tuple[str, int]:
    """Returns (register, line_index [0-indexed into method.lines])."""
    cls_inner = class_descriptor[1:-1]
    for i in range(method.start_line - 1, method.end_line):
        m = _IGET_INT_RE.match(method.lines[i].strip())
        if m and m.group("cls") == cls_inner and m.group("field") == field_name:
            return m.group("dst"), i
    raise SmaliShapeError(f"no 'iget ..., L{cls_inner};->{field_name}:I' found in method body")


def analyze_dispatch(method: MethodBody, class_descriptor: str, field_name: str) -> DispatchInfo:
    reg, read_line = _find_discriminator_read(method, class_descriptor, field_name)

    for i in range(read_line + 1, method.end_line):
        line = method.lines[i].strip()

        m = _SWITCH_OP_RE.match(line)
        if m and m.group("reg") == reg:
            return _analyze_switch(method, m.group("kind"), m.group("data_label"), i)

        if _IF_EQZ_RE.match(line) and _IF_EQZ_RE.match(line).group("reg") == reg:
            return _analyze_if_chain(method, reg, i)
        if _IF_EQ_RE.match(line):
            eq = _IF_EQ_RE.match(line)
            if eq.group("reg1") == reg:
                return _analyze_if_chain(method, reg, i)

    raise SmaliShapeError(
        f"no packed-switch/sparse-switch/if-chain found reading register {reg} "
        f"after the discriminator field read at line {read_line + 1}"
    )


def _find_label_line(method: MethodBody, label: str) -> int:
    for i in range(method.start_line - 1, method.end_line):
        m = _LABEL_RE.match(method.lines[i].strip())
        if m and m.group("label") == label:
            return i
    raise SmaliShapeError(f"label :{label} not found in method")


def _analyze_switch(method: MethodBody, kind: str, data_label: str, switch_op_line: int) -> "DispatchInfo":
    """`switch_op_line` is the 0-indexed line of the `packed-switch`/`sparse-switch`
    opcode itself. A Dalvik switch's "no case matched" fallthrough executes
    whatever comes textually right after that opcode — this is where a
    compiled Java `default:` block (or the absence of one) actually lives,
    *not* after the last case label (verified against `ftw.smali`'s own
    `default:`/`case 17:` pair, which share identical JADX content precisely
    because both paths independently reach `Loto;->c()V`)."""
    data_start = _find_label_line(method, data_label)
    branches: dict[int, tuple[str, int]] = {}

    if kind == "packed-switch":
        i = data_start + 1
        header = _PACKED_SWITCH_DATA_START_RE.match(method.lines[i].strip())
        if not header:
            raise SmaliShapeError(f"expected '.packed-switch <start>' after :{data_label}")
        start_value = int(header.group("start"), 0)
        i += 1
        value = start_value
        while True:
            line = method.lines[i].strip()
            if _SWITCH_DATA_END_RE.match(line):
                break
            lbl = line.lstrip(":")
            branches[value] = (lbl, -1)  # label line resolved below
            value += 1
            i += 1
    else:  # sparse-switch
        i = data_start + 1
        header = method.lines[i].strip()
        if not header.startswith(".sparse-switch"):
            raise SmaliShapeError(f"expected '.sparse-switch' after :{data_label}")
        i += 1
        while True:
            line = method.lines[i].strip()
            if _SWITCH_DATA_END_RE.match(line):
                break
            entry = _SPARSE_SWITCH_DATA_ENTRY_RE.match(line)
            if not entry:
                raise SmaliShapeError(f"unrecognized sparse-switch data entry: {line!r}")
            branches[int(entry.group("key"), 0)] = (entry.group("label"), -1)
            i += 1

    # Resolve each branch's own label line number, and collect every label
    # line used by this dispatch so callers can compute a branch's end
    # boundary as "the next one of these labels, in line order" (SPEC.md §6).
    resolved: dict[int, tuple[str, int]] = {}
    for value, (label, _) in branches.items():
        resolved[value] = (label, _find_label_line(method, label))
    all_label_lines = sorted(line for _, line in resolved.values())

    return DispatchInfo(
        mechanism=kind,
        branches=resolved,
        default_branch_start_line=switch_op_line + 2,  # 1-indexed line right after the opcode
        all_label_lines=all_label_lines,
    )


def _analyze_if_chain(method: MethodBody, reg: str, first_if_line: int) -> DispatchInfo:
    """Recognizes the narrow chain shape actually observed (SPEC.md §10 fixture `krb`):
    an optional leading `if-eqz REG, :L0` (checks value 0), then zero or more
    `const Cn, <value>` immediately followed by `if-eq REG, Cn, :Ln` — all
    testing the SAME `reg`. The first instruction that doesn't fit this shape
    marks the start of the implicit "default" (else) branch.
    """
    branches: dict[int, tuple[str, int]] = {}
    i = first_if_line
    line = method.lines[i].strip()

    m0 = _IF_EQZ_RE.match(line)
    if m0:
        branches[0] = (m0.group("label"), _find_label_line(method, m0.group("label")))
        i += 1
    # else: chain starts directly with an if-eq (no explicit value-0 check) — allowed.

    pending_const: tuple[str, int] | None = None  # (register, value)
    while i < method.end_line:
        line = method.lines[i].strip()
        if _is_noise_line(line):
            i += 1
            continue
        cm = _CONST_RE.match(line)
        if cm:
            pending_const = (cm.group("reg"), int(cm.group("val"), 0))
            i += 1
            continue
        eqm = _IF_EQ_RE.match(line)
        if eqm and eqm.group("reg1") == reg and pending_const and eqm.group("reg2") == pending_const[0]:
            value = pending_const[1]
            branches[value] = (eqm.group("label"), _find_label_line(method, eqm.group("label")))
            pending_const = None
            i += 1
            continue
        # First non-matching instruction (after at least the mandatory first
        # if-check already consumed): this is where the default/else branch begins.
        break

    if not branches:
        raise SmaliShapeError("if-chain detection found no recognizable branches")

    all_label_lines = sorted(line for _, line in branches.values())
    return DispatchInfo(
        mechanism="if-chain",
        branches=branches,
        default_branch_start_line=i + 1,  # 1-indexed
        all_label_lines=all_label_lines,
    )


def resolve_branch(
    method: MethodBody,
    dispatch: DispatchInfo,
    discriminator_value: int,
    smali_path: Path,
) -> tuple[SmaliEvidence, str]:
    """Returns (evidence, resolution_status). resolution_status is
    "resolved" or "resolved-default-branch" — "ambiguous" is raised by the
    caller (cli.py) when analyze_dispatch/find_method itself fails, not here.
    """
    if discriminator_value in dispatch.branches:
        label, label_line = dispatch.branches[discriminator_value]
        status = "resolved"
        start_line = label_line + 1  # 1-indexed
    else:
        # Both mechanisms' "no case matched" fallthrough is wherever the
        # switch opcode / if-chain's own comparisons finish — computed by
        # the analyzer, not re-derived here (see _analyze_switch's own note
        # on why this is NOT "after the last case label").
        label = None
        start_line = dispatch.default_branch_start_line
        status = "resolved-default-branch"

    end_line = method.end_line  # exclusive upper bound if no later label found
    for candidate_line in sorted(dispatch.all_label_lines):
        if candidate_line + 1 > start_line:
            end_line = candidate_line + 1  # 1-indexed line of the next label; branch ends just before it
            break

    branch_line_range = (start_line, end_line - 1) if end_line > start_line else (start_line, start_line)
    raw_text = "\n".join(method.lines[branch_line_range[0] - 1 : branch_line_range[1]])

    evidence = SmaliEvidence(
        path=str(smali_path),
        method_line_range=(method.start_line, method.end_line),
        branch_label=label,
        branch_line_range=branch_line_range,
        raw_text=raw_text,
    )
    return evidence, status
