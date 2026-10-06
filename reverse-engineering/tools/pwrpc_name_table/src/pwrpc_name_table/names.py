"""SPEC.md §3/§4 — the wire census and the literal sweep.

The 65599 hash and the capture reassembly are `scripts/pwrpc_decode.py`'s own
(imported, not re-implemented — SPEC.md §4). Only names whose hash equals an id seen
on the wire are ever reported; the sweep's full literal list is never written anywhere
(SPEC.md §11).
"""

from __future__ import annotations

import collections
import re
import sys
from pathlib import Path

_REPO_ROOT = Path(__file__).resolve().parents[5]
_SCRIPTS_DIR = _REPO_ROOT / "scripts"
if str(_SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(_SCRIPTS_DIR))

import pwrpc_decode as _pw  # noqa: E402

__all__ = ["h65599", "census", "iter_literals", "match"]

h65599 = _pw.h65599

_JAVA_STRING = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
_SMALI_STRING = re.compile(r'^\s*const-string(?:/jumbo)? [vp]\d+, "((?:[^"\\]|\\.)*)"\s*$')
_ESCAPES = {"n": "\n", "t": "\t", "r": "\r", "b": "\b", "f": "\f", "0": "\0", '"': '"', "'": "'", "\\": "\\"}
_ESC = re.compile(r"\\(u[0-9a-fA-F]{4}|.)")


def _unescape(s: str) -> str:
    if "\\" not in s:
        return s
    return _ESC.sub(lambda m: chr(int(m.group(1)[1:], 16)) if m.group(1)[0] == "u" and len(m.group(1)) == 5
                    else _ESCAPES.get(m.group(1), m.group(1)), s)


def census(logs: list[Path]) -> dict:
    """Every pw_rpc service id and (service, method) id on the wire of `logs`, with packet counts,
    directions, packet types, the number of logs and the first (log, frame). A log tshark cannot
    read is listed under `errors`, never skipped silently (SPEC.md §7)."""
    services: dict[str, dict] = {}
    methods: dict[str, dict] = {}
    per_log: dict[str, int] = {}
    errors: dict[str, str] = {}
    for log in logs:
        name = Path(log).name
        n = 0
        try:
            for frame, _src, _addr, _control, d, _dlci, direction in _pw.packets(str(log)):
                if not (isinstance(d.get(3), bytes) and len(d[3]) == 4 and isinstance(d.get(4), bytes) and len(d[4]) == 4):
                    continue
                s, m = int.from_bytes(d[3], "little"), int.from_bytes(d[4], "little")
                n += 1
                ptype = _pw.TYPES.get(d.get(1, 0), str(d.get(1)))
                for table, key in ((services, f"{s:#010x}"), (methods, f"{s:#010x}/{m:#010x}")):
                    e = table.setdefault(key, {"count": 0, "dir": collections.Counter(), "types": collections.Counter(),
                                               "first": [name, frame], "logs": set()})
                    e["count"] += 1
                    e["dir"][direction] += 1
                    e["types"][ptype] += 1
                    e["logs"].add(name)
        except Exception as ex:  # noqa: BLE001 — reported per log
            errors[name] = f"{type(ex).__name__}: {ex}"
            continue
        per_log[name] = n
    for table in (services, methods):
        for e in table.values():
            e["logs"] = len(e["logs"])
            e["dir"] = dict(e["dir"])
            e["types"] = dict(e["types"])
    return {"logs": per_log, "errors": errors, "services": services, "methods": methods}


def iter_literals(apk_root: Path):
    """Yields (literal, 'relative/path:line') for every Java string literal under
    jadx-output/sources/ and every smali `const-string` under apktool-output*/smali*/."""
    apk_root = Path(apk_root)
    sources = apk_root / "jadx-output" / "sources"
    if not sources.is_dir():
        raise FileNotFoundError(f"no jadx-output/sources/ found under {apk_root}")
    for jf in sources.rglob("*.java"):
        try:
            text = jf.read_text(encoding="utf-8", errors="replace")
        except OSError:  # pragma: no cover
            continue
        if '"' not in text:
            continue
        rel = str(jf.relative_to(apk_root))
        for n, line in enumerate(text.split("\n"), 1):
            if '"' in line:
                for m in _JAVA_STRING.finditer(line):
                    yield _unescape(m.group(1)), f"{rel}:{n}"
    for root in sorted(apk_root.glob("apktool-output*/smali*")):
        for sf in root.rglob("*.smali"):
            try:
                text = sf.read_text(encoding="utf-8", errors="replace")
            except OSError:  # pragma: no cover
                continue
            if "const-string" not in text:
                continue
            rel = str(sf.relative_to(apk_root))
            for n, line in enumerate(text.split("\n"), 1):
                if "const-string" in line:
                    m = _SMALI_STRING.match(line)
                    if m:
                        yield _unescape(m.group(1)), f"{rel}:{n}"


def match(apk_root: Path, wire: dict, max_sites: int = 4) -> dict:
    """Hash every distinct literal once and report the ones equal to a wire id (SPEC.md §3)."""
    service_ids = {int(k, 16) for k in wire["services"]}
    method_ids = {int(k.split("/")[1], 16) for k in wire["methods"]}
    wanted = service_ids | method_ids
    seen: set[str] = set()
    total = 0
    hits: dict[int, dict[str, list[str]]] = {}
    for lit, where in iter_literals(apk_root):
        total += 1
        if lit in seen:
            if hits:
                for names in hits.values():
                    if lit in names and len(names[lit]) < max_sites:
                        names[lit].append(where)
            continue
        seen.add(lit)
        h = h65599(lit)
        if h in wanted:
            hits.setdefault(h, {})[lit] = [where]

    def names_for(i: int) -> list[dict]:
        return [{"name": n, "found_in": w} for n, w in sorted(hits.get(i, {}).items())]

    services = {k: {**v, "names": names_for(int(k, 16))} for k, v in sorted(wire["services"].items())}
    methods = {k: {**v, "names": names_for(int(k.split("/")[1], 16))} for k, v in sorted(wire["methods"].items())}
    return {
        "apk_root": str(apk_root),
        "literal_occurrences_scanned": total,
        "distinct_literals_hashed": len(seen),
        "wire_service_ids": len(services),
        "wire_method_ids": len(methods),
        "services": services,
        "methods": methods,
        "unnamed_services": [k for k, v in services.items() if not v["names"]],
        "unnamed_methods": [k for k, v in methods.items() if not v["names"]],
    }
