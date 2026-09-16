"""SPEC.md §3/§4 — the one query this tool exists to answer, built directly on
`lambda_dispatcher_resolver`'s own Layer 1 (`androguard_index.load_apk`), imported
via a sys.path insertion to the sibling tool's `src/` directory rather than
re-implemented (SPEC.md §4's own "reuse, stated precisely" note).
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

# Sibling tool: reverse-engineering/tools/{structural_index,lambda_dispatcher_resolver}/src
_LAMBDA_RESOLVER_SRC = (
    Path(__file__).resolve().parents[3] / "lambda_dispatcher_resolver" / "src"
)
if str(_LAMBDA_RESOLVER_SRC) not in sys.path:
    sys.path.insert(0, str(_LAMBDA_RESOLVER_SRC))

from lambda_dispatcher_resolver.androguard_index import (  # noqa: E402
    LoadedApk,
    _normalize_class_name,
    load_apk,
)

from .models import CallSite, ConstructSite, FieldTypeHolder, RefsResult, UnreferencedResult

__all__ = ["load_apk", "normalize_class_name", "find_refs", "find_unreferenced"]

# Re-exported under a public name — `_normalize_class_name` is a private helper
# in the upstream tool; this module's own callers should not reach past this
# module for it.
normalize_class_name = _normalize_class_name

_NEW_INSTANCE_KINDS = {"new-instance"}
_INVOKE_KINDS = {
    "invoke-virtual",
    "invoke-direct",
    "invoke-static",
    "invoke-super",
    "invoke-interface",
    # "/range" variants (many-argument calls) carry the same operand shape.
    "invoke-virtual/range",
    "invoke-direct/range",
    "invoke-static/range",
    "invoke-super/range",
    "invoke-interface/range",
}

# Matches "...Lclass/path;->methodName(params)ret" and captures the method name.
_INVOKE_METHOD_RE = re.compile(r"->([^\(]+)\(")


def _short_name(descriptor: str) -> str:
    """"Lcom/foo/Bar;" -> "com.foo.Bar"; "Laie;" -> "defpackage.aie" (JADX's own
    default-package convention, matching lambda_dispatcher_resolver's own
    citation style in REVERSE_ENGINEERING.md)."""
    inner = descriptor[1:-1]  # strip leading L and trailing ;
    if "/" in inner:
        return inner.replace("/", ".")
    return f"defpackage.{inner}"


def find_refs(loaded: LoadedApk, class_name: str, method_filter: str | None = None) -> RefsResult:
    """SPEC.md §3's query, run once for `class_name` (any of the three accepted
    spellings). Raises KeyError if the class isn't present anywhere in the APK's
    dex set — callers (CLI) turn that into SPEC.md §7's non-zero-exit error."""
    target = normalize_class_name(class_name)
    if target not in loaded.class_by_name:
        raise KeyError(target)

    result = RefsResult(cls=_short_name(target))

    # (a)/(b): one pass over every class's every method's decoded instructions.
    for position, dex in enumerate(loaded.dexes):
        dex_file = loaded.dex_names[position]
        for cls in dex.get_classes():
            caller_class = _short_name(cls.get_name())
            for method in cls.get_methods():
                code = method.get_code()
                if code is None:
                    continue  # abstract/native — no instruction stream to scan
                try:
                    bc = code.get_bc()
                    instructions = list(bc.get_instructions())
                except Exception:  # pragma: no cover — SPEC.md §7's "skip, don't abort" rule
                    print(
                        f"structural-index: warning: failed to decode {caller_class}.{method.get_name()}, skipping",
                        file=sys.stderr,
                    )
                    continue
                for ins in instructions:
                    name = ins.get_name()
                    if name not in _NEW_INSTANCE_KINDS and name not in _INVOKE_KINDS:
                        continue
                    try:
                        out = ins.get_output(0)
                    except Exception:  # pragma: no cover
                        continue
                    if name in _NEW_INSTANCE_KINDS:
                        if out.rstrip().endswith(target):
                            result.constructs.append(
                                ConstructSite(
                                    caller_class=caller_class,
                                    caller_method=method.get_name(),
                                    dex_file=dex_file,
                                )
                            )
                        continue
                    # invoke-* : look for "<target>->" as an unambiguous substring
                    # (a class descriptor is a self-delimiting token; concatenating
                    # the literal "->" removes any risk of matching a longer,
                    # differently-named class that merely starts with the same
                    # characters).
                    marker = f"{target}->"
                    if marker not in out:
                        continue
                    method_match = _INVOKE_METHOD_RE.search(out[out.index(marker) :])
                    called_method = method_match.group(1) if method_match else "?"
                    if method_filter is not None and called_method != method_filter:
                        continue
                    result.calls.append(
                        CallSite(
                            caller_class=caller_class,
                            caller_method=method.get_name(),
                            called_method=called_method,
                            invoke_kind=name,
                            dex_file=dex_file,
                        )
                    )

    # (c): a separate, cheap pass over field *declarations* (not instructions).
    for position, dex in enumerate(loaded.dexes):
        dex_file = loaded.dex_names[position]
        for cls in dex.get_classes():
            holder_class = _short_name(cls.get_name())
            for f in cls.get_fields():
                if f.get_descriptor() == target:
                    result.field_type_holders.append(
                        FieldTypeHolder(
                            holder_class=holder_class,
                            field_name=f.get_name(),
                            dex_file=dex_file,
                        )
                    )

    return result


def find_unreferenced(loaded: LoadedApk, class_names: list[str]) -> list[UnreferencedResult]:
    """SPEC.md §5's `unreferenced` command: for each class, run the same §3 query
    and report whether *any* reference exists across (a)/(b)/(c) combined."""
    out: list[UnreferencedResult] = []
    for name in class_names:
        refs = find_refs(loaded, name)
        total = sum(refs.counts().values())
        out.append(UnreferencedResult(cls=refs.cls, total_references=total, referenced=total > 0))
    return out
