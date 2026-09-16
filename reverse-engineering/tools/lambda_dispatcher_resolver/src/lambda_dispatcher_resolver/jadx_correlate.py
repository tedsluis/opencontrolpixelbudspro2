"""Layer 3 — JADX correlation.

Given a class + dispatch mechanism + resolved discriminator value, finds the
matching Java source in `jadx-output/sources/` and, for switch-based
dispatch, the specific `case N:` block — never claiming more precision than
the text actually supports (SPEC.md §7).

Known, deliberate limitation (discovered while building this against the
`krb` fixture, not anticipated in SPEC.md's original text): JADX renders an
`if`-chain-compiled dispatcher as a nested `if`/ternary expression, not a
Java `switch` statement — there is no `case N:` text to search for. For
`if-chain` mechanism results, this module returns the *whole* method body
with `case_text_found=False` rather than guessing which part of a ternary
corresponds to which discriminator value.
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

from .models import JadxEvidence

_DUMP_SKIPPED_RE = re.compile(r"Method dump skipped, instructions count:\s*(\d+)")

# Matches `case 9:` and the literal-cast form `case UrlRequest.Status.SENDING_REQUEST /* 12 */:`
# — both forms this project's own decompiled sources actually use.
_CASE_RE = re.compile(r"^\s*case\s+(?:[\w.]+\s*/\*\s*)?(?P<value>\d+)\s*(?:\*/)?\s*:")
_DEFAULT_RE = re.compile(r"^\s*default\s*:")


def class_descriptor_to_jadx_path(apk_root: Path, class_descriptor: str) -> Path:
    """"Laie;" (no package — R8's default-package classes) -> .../sources/defpackage/aie.java
    "Lcom/foo/Bar;" -> .../sources/com/foo/Bar.java
    """
    inner = class_descriptor[1:-1]
    if "/" in inner:
        rel = inner + ".java"
    else:
        rel = f"defpackage/{inner}.java"
    return apk_root / "jadx-output" / "sources" / rel


def _find_method_start(lines: list[str], method_name: str) -> int | None:
    """Heuristic method-declaration search (SPEC.md §4's Layer 3 scope: text
    correlation only, not a Java parser). Looks for a line declaring a method
    named `method_name` — skips constructors (named after the class, never
    matching our target's short synthetic method names like "a"/"apply")."""
    decl_re = re.compile(rf"\b{re.escape(method_name)}\s*\(")
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith("//") or stripped.startswith("*") or stripped.startswith("/*"):
            continue
        if decl_re.search(line) and ("{" in line or (i + 1 < len(lines) and "{" in lines[i + 1])):
            # Exclude constructor-shaped lines (no return type before the name)
            # is not needed here — constructors are named after the class,
            # and this search is only ever run with the interface's own
            # abstract method name, which never collides with a class name
            # in this project's obfuscated (short, lowercase) naming.
            return i
    return None


def _find_method_end(lines: list[str], start: int) -> int:
    """Finds the matching closing brace for the method starting at `start`,
    by brace-depth counting from the method's own opening `{`."""
    depth = 0
    started = False
    for i in range(start, len(lines)):
        for ch in lines[i]:
            if ch == "{":
                depth += 1
                started = True
            elif ch == "}":
                depth -= 1
                if started and depth == 0:
                    return i
    return len(lines) - 1


def _extract_case_block(lines: list[str], method_start: int, method_end: int, value: int) -> tuple[int, int] | None:
    case_line = None
    case_indent = None
    for i in range(method_start, method_end + 1):
        m = _CASE_RE.match(lines[i])
        if m and int(m.group("value")) == value:
            case_line = i
            case_indent = len(lines[i]) - len(lines[i].lstrip())
            break
    if case_line is None:
        return None

    end = method_end
    for i in range(case_line + 1, method_end + 1):
        stripped = lines[i]
        indent = len(stripped) - len(stripped.lstrip())
        if indent <= case_indent and (_CASE_RE.match(stripped) or _DEFAULT_RE.match(stripped) or stripped.strip() == "}"):
            end = i - 1
            break
    return case_line, end


def correlate(
    apk_root: Path,
    class_descriptor: str,
    method_name: str,
    dispatch_mechanism: str,
    discriminator_value: int,
) -> JadxEvidence:
    path = class_descriptor_to_jadx_path(apk_root, class_descriptor)
    if not path.is_file():
        return JadxEvidence(
            path=None,
            decompilable=False,
            case_text_found=False,
            note=f"no JADX source found at expected path {path}",
        )

    text = path.read_text(encoding="utf-8", errors="replace")
    lines = text.split("\n")

    method_start = _find_method_start(lines, method_name)
    if method_start is None:
        return JadxEvidence(
            path=str(path),
            decompilable=False,
            case_text_found=False,
            note=f"could not locate a '{method_name}(' method declaration in this file",
        )
    method_end = _find_method_end(lines, method_start)
    method_text = "\n".join(lines[method_start : method_end + 1])

    dump_match = _DUMP_SKIPPED_RE.search(method_text)
    if dump_match:
        return JadxEvidence(
            path=str(path),
            decompilable=False,
            case_text_found=False,
            note=f"Method dump skipped, instructions count: {dump_match.group(1)} — "
            f"smali is the only available evidence for this branch.",
        )

    if dispatch_mechanism == "if-chain":
        return JadxEvidence(
            path=str(path),
            decompilable=True,
            case_text_found=False,
            note=(
                "dispatch_mechanism is 'if-chain' — JADX renders this as a nested if/ternary "
                "expression, not a Java switch statement, so there is no 'case N:' text to "
                "search for. Returning the whole method body; the specific branch for this "
                "discriminator value must be read manually from it (see SPEC.md's documented "
                "limitation)."
            ),
            raw_text=method_text,
        )

    block = _extract_case_block(lines, method_start, method_end, discriminator_value)
    if block is None:
        return JadxEvidence(
            path=str(path),
            decompilable=True,
            case_text_found=False,
            note=f"decompiled, but no 'case {discriminator_value}:' text found "
            f"(possibly the default branch, or a differently-shaped switch) — "
            f"returning the whole method body instead of guessing.",
            raw_text=method_text,
        )
    start, end = block
    return JadxEvidence(
        path=str(path),
        decompilable=True,
        case_text_found=True,
        note="",
        raw_text="\n".join(lines[start : end + 1]),
    )
