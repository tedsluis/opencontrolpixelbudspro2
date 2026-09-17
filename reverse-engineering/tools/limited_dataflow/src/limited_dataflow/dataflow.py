"""SPEC.md §3/§4 — the four-shape single-basic-block trace, and the two entry
points (`trace_branch`/`trace_method`) that reuse `lambda_dispatcher_resolver`
directly for method/branch location (sys.path insertion to the sibling tool's
`src/` directory, not re-implemented).
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

_TOOLS_DIR = Path(__file__).resolve().parents[3]
_LDR_SRC = _TOOLS_DIR / "lambda_dispatcher_resolver" / "src"
if str(_LDR_SRC) not in sys.path:
    sys.path.insert(0, str(_LDR_SRC))

from lambda_dispatcher_resolver import smali_reader  # noqa: E402
from lambda_dispatcher_resolver.androguard_index import (  # noqa: E402
    _normalize_class_name,
    load_apk,
)
from lambda_dispatcher_resolver.cli import analyze_class  # noqa: E402

from .models import TraceResult, TraceStep  # noqa: E402

__all__ = ["trace_lines", "trace_branch", "trace_method"]

_REG_TOKEN_RE = re.compile(r"[vp]\d+")
_LABEL_RE = re.compile(r"^:(?P<label>\w+)$")
_BRANCH_RE = re.compile(r"^(goto(?:/16|/32)?|if-\w+|packed-switch|sparse-switch)\b")
_MOVE_OBJECT_RE = re.compile(r"^move-object(?:/from16|/16)?\s+([vp]\d+),\s*([vp]\d+)$")
_CHECK_CAST_RE = re.compile(r"^check-cast\s+([vp]\d+),\s*L([^;]+);$")
_INVOKE_RE = re.compile(
    r"^(invoke-[a-z]+(?:/range)?)\s+\{([^}]*)\},\s*L([^;]+);->([^\(]+)\(([^\)]*)\).*$"
)


def _is_noise(line: str) -> bool:
    """Same convention as lambda_dispatcher_resolver.smali_reader's own
    `_is_noise_line` -- duplicated locally per this project's own precedent
    (structural_index/SPEC.md §4) that an 8-line pure function is not worth a
    cross-tool dependency."""
    return line == "" or line.startswith(".line ")


def _parse_invoke_args(args_str: str) -> list[str]:
    args_str = args_str.strip()
    if not args_str:
        return []
    if ".." in args_str:
        start_reg, end_reg = (a.strip() for a in args_str.split(".."))
        prefix = start_reg[0]
        start_n, end_n = int(start_reg[1:]), int(end_reg[1:])
        return [f"{prefix}{n}" for n in range(start_n, end_n + 1)]
    return [a.strip() for a in args_str.split(",") if a.strip()]


def trace_lines(lines: list[str], start_idx: int, end_idx: int, start_register: str) -> TraceResult:
    """SPEC.md §3's four-shape recognizer. `lines` is 0-indexed (e.g.
    `MethodBody.lines`, or a synthetic list for a unit test). `start_idx` is
    the 0-indexed line that DEFINES `start_register` -- tracing begins at
    `start_idx + 1`. `end_idx` is the 0-indexed inclusive upper bound."""
    steps: list[TraceStep] = []
    current = start_register
    i = start_idx + 1
    while i <= end_idx:
        raw_line = lines[i].strip()
        if _is_noise(raw_line):
            i += 1
            continue

        if _LABEL_RE.match(raw_line) or _BRANCH_RE.match(raw_line):
            return TraceResult(
                start_line=start_idx + 1,
                start_register=start_register,
                steps=steps,
                final_status="left_basic_block_scope",
                final_register=current,
                stop_line=i + 1,
                stop_text=raw_line,
            )

        m = _MOVE_OBJECT_RE.match(raw_line)
        if m and m.group(2) == current:
            new_reg = m.group(1)
            steps.append(TraceStep(line=i + 1, kind="alias", register=new_reg, from_register=current))
            current = new_reg
            i += 1
            continue

        m = _CHECK_CAST_RE.match(raw_line)
        if m and m.group(1) == current:
            steps.append(TraceStep(line=i + 1, kind="cast", register=current, to_type=m.group(2)))
            i += 1
            continue

        m = _INVOKE_RE.match(raw_line)
        if m:
            invoke_kind, args_str, called_cls, called_method = m.group(1), m.group(2), m.group(3), m.group(4)
            args = _parse_invoke_args(args_str)
            if current in args:
                steps.append(
                    TraceStep(
                        line=i + 1,
                        kind="sink_use",
                        register=current,
                        arg_position=args.index(current),
                        invoke_kind=invoke_kind,
                        called_class=called_cls,
                        called_method=called_method,
                    )
                )
            i += 1
            continue

        # Row "-": anything else that names the traced register at all is an
        # explicit, honest stop -- never a guess (SPEC.md §3/AGENTS.md §13.6).
        if current in _REG_TOKEN_RE.findall(raw_line):
            return TraceResult(
                start_line=start_idx + 1,
                start_register=start_register,
                steps=steps,
                final_status="ambiguous_redefinition",
                final_register=current,
                stop_line=i + 1,
                stop_text=raw_line,
            )

        i += 1

    return TraceResult(
        start_line=start_idx + 1,
        start_register=start_register,
        steps=steps,
        final_status="reached_end_of_block",
        final_register=current,
    )


def _check_start_line(lines: list[str], start_idx: int, start_register: str, lo: int, hi: int) -> None:
    """SPEC.md §7: --start-line must be inside the given range and must
    actually mention --start-register, or this is a hard error."""
    start_line = start_idx + 1
    if not (lo <= start_line <= hi):
        raise ValueError(f"--start-line {start_line} is outside the valid range [{lo}, {hi}]")
    line_text = lines[start_idx].strip()
    if start_register not in _REG_TOKEN_RE.findall(line_text):
        raise ValueError(f"line {start_line} does not mention register {start_register!r}: {line_text!r}")


def trace_branch(
    apk_root: Path, class_name: str, discriminator: int, start_line: int, start_register: str
) -> TraceResult:
    """SPEC.md §5's `trace-branch`: resolves the branch exactly as
    `lambda_dispatcher_resolver resolve` would (reused directly), then runs
    the trace bounded to that branch's own line range."""
    loaded = load_apk(apk_root)
    analysis, err = analyze_class(loaded, apk_root, class_name)
    if analysis is None:
        raise KeyError(err)

    evidence, resolution_status = smali_reader.resolve_branch(
        analysis.method, analysis.dispatch, discriminator, analysis.smali_path
    )
    lo, hi = evidence.branch_line_range
    start_idx = start_line - 1
    _check_start_line(analysis.method.lines, start_idx, start_register, lo, hi)

    result = trace_lines(analysis.method.lines, start_idx, hi - 1, start_register)
    result.extra = {
        "class": analysis.class_descriptor,
        "discriminator": discriminator,
        "branch_resolution_status": resolution_status,
    }
    return result


def trace_method(
    apk_root: Path, class_name: str, method_name: str, start_line: int, start_register: str
) -> TraceResult:
    """SPEC.md §5's `trace-method`: any class's plain method body, no
    dispatcher resolution -- still single-basic-block only (the trace stops
    at the first boundary it hits, exactly as `trace_branch` would)."""
    loaded = load_apk(apk_root)
    descriptor = _normalize_class_name(class_name)
    if descriptor not in loaded.class_to_dex_position:
        raise KeyError(descriptor)
    dex_position = loaded.class_to_dex_position[descriptor]
    smali_path = smali_reader.locate_smali_file(apk_root, dex_position, descriptor)
    method = smali_reader.find_method(smali_path, method_name)

    lo, hi = method.start_line, method.end_line - 1  # exclude the ".end method" line itself
    start_idx = start_line - 1
    _check_start_line(method.lines, start_idx, start_register, lo, hi)

    result = trace_lines(method.lines, start_idx, hi - 1, start_register)
    result.extra = {"class": descriptor, "method": method_name}
    return result
