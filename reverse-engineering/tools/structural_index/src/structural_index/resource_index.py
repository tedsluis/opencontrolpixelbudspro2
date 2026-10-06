"""SPEC.md §5c (v1.2, 2026-10-06) — the resource/string-table search deferred since v1.

A different data source from the rest of this tool: `apktool-output/res/` (decoded XML) and the
text of the decompiled sources, not DEX structure — so this module uses the standard library only
and does not load the APK through androguard. One query: string resources whose *name* or *value*
matches, each with the chain  name -> resource id (public.xml) -> where the id / the name is used
(smali `const`, JADX `R.<type>.<name>`, `@<type>/<name>` in res XML).
"""

from __future__ import annotations

import os
import re
from dataclasses import dataclass, field
from pathlib import Path

__all__ = ["ResourceMatch", "ResourceSearchResult", "find_resources"]

_STRING = re.compile(r'<string name="([^"]+)"[^>]*?(?:/>|>(.*?)</string>)')
_ITEM = re.compile(r'<item[^>]*>(.*?)</item>')
_ARRAY_OPEN = re.compile(r'<(string-array|array|integer-array) name="([^"]+)"')
_PUBLIC = re.compile(r'<public type="([^"]+)" name="([^"]+)" id="(0x[0-9a-fA-F]+)"')
_MAX_VALUE = 160  # a resource value is quoted up to this length (SPEC.md §11)


@dataclass
class ResourceMatch:
    type: str  # "string" | "array"
    name: str
    value: str
    defined_at: str  # "res/values/strings.xml:433"
    resource_id: str | None = None
    public_at: str | None = None
    smali_uses: list[str] = field(default_factory=list)  # "smali_classes2/cmi.smali:3111"
    java_uses: list[str] = field(default_factory=list)  # "defpackage/gza.java:291"
    xml_uses: list[str] = field(default_factory=list)  # "res/xml/head_gestures_preference.xml:6"

    def to_dict(self) -> dict:
        return {k: getattr(self, k) for k in ("type", "name", "value", "defined_at", "resource_id", "public_at",
                                              "smali_uses", "java_uses", "xml_uses")}


@dataclass
class ResourceSearchResult:
    apk_root: str
    key_pattern: str | None
    text_pattern: str | None
    strings_scanned: int
    matches: list[ResourceMatch] = field(default_factory=list)
    files_scanned: dict = field(default_factory=dict)

    def to_dict(self) -> dict:
        return {"apk_root": self.apk_root, "key_pattern": self.key_pattern, "text_pattern": self.text_pattern,
                "strings_scanned": self.strings_scanned, "total_matches": len(self.matches),
                "files_scanned": self.files_scanned, "matches": [m.to_dict() for m in self.matches]}


def _clip(value: str) -> str:
    value = " ".join(value.split())
    return value if len(value) <= _MAX_VALUE else value[: _MAX_VALUE - 1] + "…"


def _definitions(values_dir: Path):
    """Yields (type, name, value, 'res/values/<file>:<line>') for every <string> and every array."""
    for fname in ("strings.xml", "arrays.xml"):
        f = values_dir / fname
        if not f.is_file():
            continue
        lines = f.read_text(encoding="utf-8", errors="replace").split("\n")
        i = 0
        while i < len(lines):
            line = lines[i]
            m = _STRING.search(line)
            if m:
                yield "string", m.group(1), m.group(2) or "", f"res/values/{fname}:{i + 1}"
            else:
                a = _ARRAY_OPEN.search(line)
                if a:
                    start, items = i, []
                    while i < len(lines) and f"</{a.group(1)}>" not in lines[i]:
                        items.extend(_ITEM.findall(lines[i]))
                        i += 1
                    if i < len(lines):
                        items.extend(x for x in _ITEM.findall(lines[i]) if x not in items[-1:])
                    yield "array", a.group(2), " | ".join(items), f"res/values/{fname}:{start + 1}"
            i += 1


def find_resources(apk_root: Path, key_pattern: str | None = None, text_pattern: str | None = None,
                   with_usages: bool = True) -> ResourceSearchResult:
    """SPEC.md §5c. At least one of `key_pattern` (on the resource name) and `text_pattern` (on its
    value) is required; both are case-insensitive regular expressions and, given together, must both
    match. Raises FileNotFoundError without `apktool-output/res/values/strings.xml`."""
    if not key_pattern and not text_pattern:
        raise ValueError("give --key and/or --text")
    apk_root = Path(apk_root)
    res = apk_root / "apktool-output" / "res"
    if not (res / "values" / "strings.xml").is_file():
        raise FileNotFoundError(f"no apktool-output/res/values/strings.xml found under {apk_root}")
    key_re = re.compile(key_pattern, re.IGNORECASE) if key_pattern else None
    text_re = re.compile(text_pattern, re.IGNORECASE) if text_pattern else None

    result = ResourceSearchResult(str(apk_root), key_pattern, text_pattern, 0)
    for typ, name, value, where in _definitions(res / "values"):
        result.strings_scanned += 1
        if (key_re is None or key_re.search(name)) and (text_re is None or text_re.search(value)):
            result.matches.append(ResourceMatch(typ, name, _clip(value), where))
    if not result.matches:
        return result

    public = res / "values" / "public.xml"
    ids: dict[tuple[str, str], tuple[str, int]] = {}
    if public.is_file():
        for n, line in enumerate(public.read_text(encoding="utf-8", errors="replace").split("\n"), 1):
            p = _PUBLIC.search(line)
            if p:
                ids[(p.group(1), p.group(2))] = (p.group(3).lower(), n)
    for m in result.matches:
        hit = ids.get((m.type, m.name))
        if hit:
            m.resource_id, m.public_at = hit[0], f"res/values/public.xml:{hit[1]}"
    if not with_usages:
        return result

    by_id = {m.resource_id: m for m in result.matches if m.resource_id}
    by_ref = {f"R.{m.type}.{m.name}": m for m in result.matches}
    by_xml = {f"@{m.type}/{m.name}": m for m in result.matches}
    id_re = re.compile(r"\b(" + "|".join(re.escape(i) for i in by_id) + r")\b", re.IGNORECASE) if by_id else None
    ref_re = re.compile(r"\b(" + "|".join(re.escape(r) for r in by_ref) + r")\b")
    xml_re = re.compile("(" + "|".join(re.escape(x) for x in by_xml) + r")(?![\w.])")

    def walk(root: Path, suffix: str):
        n = 0
        for dirpath, _d, files in os.walk(root):
            for fn in files:
                if fn.endswith(suffix):
                    n += 1
                    yield Path(dirpath) / fn
        result.files_scanned[str(root.relative_to(apk_root))] = n

    if id_re is not None:
        for sroot in sorted((apk_root / "apktool-output").glob("smali*")):
            for f in walk(sroot, ".smali"):
                text = f.read_text(encoding="utf-8", errors="replace")
                if "0x7f" not in text:
                    continue
                for n, line in enumerate(text.split("\n"), 1):
                    if "0x7f" in line and "const" in line:
                        h = id_re.search(line)
                        if h:
                            by_id[h.group(1).lower()].smali_uses.append(f"{f.relative_to(apk_root / 'apktool-output')}:{n}")
    sources = apk_root / "jadx-output" / "sources"
    if sources.is_dir():
        for f in walk(sources, ".java"):
            if f.name == "R.java":
                continue  # the generated id table itself is not a use
            text = f.read_text(encoding="utf-8", errors="replace")
            if "R." not in text:
                continue
            for n, line in enumerate(text.split("\n"), 1):
                if "R." in line:
                    for h in ref_re.finditer(line):
                        by_ref[h.group(1)].java_uses.append(f"{f.relative_to(sources)}:{n}")
    for f in walk(res, ".xml"):
        if f.parent.name.startswith("values"):
            continue  # definitions and translations, not uses
        text = f.read_text(encoding="utf-8", errors="replace")
        if "@" not in text:
            continue
        for n, line in enumerate(text.split("\n"), 1):
            for h in xml_re.finditer(line):
                by_xml[h.group(1)].xml_uses.append(f"res/{f.relative_to(res)}:{n}")
    return result
