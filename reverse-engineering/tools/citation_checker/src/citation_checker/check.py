"""SPEC.md §3/§4 — parse the citations a markdown document makes into the decompiled
tree, resolve each to a file on disk, and check its line range and the tokens the
citing sentence itself quotes.

Pure standard library. Reads the markdown documents and the local, gitignored
`reverse-engineering/apk/<version>/` tree; writes nothing but its own JSON
(SPEC.md §8). The JSON carries line numbers and the tokens the *markdown* quotes —
never a line of decompiled text (SPEC.md §11).
"""

from __future__ import annotations

import os
import re
from dataclasses import asdict, dataclass, field
from pathlib import Path

__all__ = ["TreeIndex", "Citation", "parse_citations", "check_document", "summarize", "DEFAULT_SLACK"]

DEFAULT_SLACK = 3  # lines either side of a cited range in which a quoted token still counts as "in range"
_WINDOW = 320  # characters of the citing sentence searched for quoted tokens, each side
_MIN_QUOTED = 6  # shortest "double-quoted" string used as an expected token
_MIN_CODE = 8  # shortest `backticked` code token used as an expected token

_EXT = r"(?:java|smali|xml)"
_RANGE = r"(\d+)(?:\s*[-–—]\s*:?(\d+))?"
# `path/Name.java:12-34`, `Name.smali :12–34` (a space before the colon occurs in the documents)
_EXPLICIT = re.compile(r"(?<![\w/.$…-])((?:[\w$.…-]+/)*[\w$-]+\." + _EXT + r")`?\s?:" + _RANGE)
# a bare `Name.java` in backticks, no line
_BARE = re.compile(r"`((?:[\w$.…-]+/)*[\w$-]+\.(?:java|smali))`(?!\s?:\d)")
# a continuation: `:12-34` or (`:12`) referring to the file named last in the same paragraph
_CONT = re.compile(r"(?<![\w.)])`?:" + _RANGE + r"`?(?=[\s,;.)`/]|$)")
_QUOTED = re.compile(r'"([^"\n]{%d,200})"' % _MIN_QUOTED)
_CODE = re.compile(r"`([^`\n]{%d,200})`" % _MIN_CODE)
_PATHLIKE = re.compile(r"^[\w$./ -]+\.(?:java|smali|xml|md|py|kt|log)(?::[\d–—-]+)?$")


@dataclass
class TokenCheck:
    token: str
    in_range: bool
    found_lines: list[int]  # first few 1-based lines of the cited file that contain the token


@dataclass
class Citation:
    doc: str
    doc_line: int
    raw: str
    kind: str  # "explicit" | "continuation" | "bare"
    name: str  # the cited file name as written (may carry directories)
    start: int | None = None
    end: int | None = None
    resolved: str | None = None  # path relative to the apk root
    candidates: int = 0
    file_lines: int | None = None
    verdict: str = ""
    nearest_token_line: int | None = None
    tokens: list[TokenCheck] = field(default_factory=list)

    def to_dict(self) -> dict:
        return asdict(self)


class TreeIndex:
    """Basename → paths for jadx-output/sources/**, apktool-output*/smali*/**, and the apktool
    AndroidManifest.xml and res/{values,xml,layout,navigation}/*.xml files (SPEC.md §3)."""

    def __init__(self, apk_root: Path):
        self.apk_root = Path(apk_root)
        if not (self.apk_root / "jadx-output" / "sources").is_dir():
            raise FileNotFoundError(f"no jadx-output/sources/ found under {apk_root}")
        self.by_name: dict[str, list[str]] = {}
        self._lines: dict[str, list[str]] = {}
        roots = [self.apk_root / "jadx-output" / "sources"]
        for apktool in sorted(self.apk_root.glob("apktool-output*")):
            roots.extend(sorted(p for p in apktool.glob("smali*") if p.is_dir()))
            manifest = apktool / "AndroidManifest.xml"
            if manifest.is_file():
                self._add(manifest)
            for sub in ("values", "xml", "layout", "navigation"):  # decoded resources the documents cite
                d = apktool / "res" / sub
                if d.is_dir():
                    for x in sorted(d.glob("*.xml")):
                        self._add(x)
        for root in roots:
            for dirpath, _dirs, files in os.walk(root):
                for fn in files:
                    if fn.endswith((".java", ".smali")):
                        self._add(Path(dirpath) / fn)

    def _add(self, path: Path) -> None:
        self.by_name.setdefault(path.name, []).append(str(path.relative_to(self.apk_root)))

    def resolve(self, name: str) -> tuple[str | None, int]:
        """Returns (best path or None, number of candidates). A citation that names
        directories must match them as a path suffix; otherwise the default package
        (`defpackage/` in JADX, the top of a smali root) is preferred."""
        base = name.rsplit("/", 1)[-1]
        cands = self.by_name.get(base, [])
        if "/" in name:
            # "com/google/.../OtaFragment.java": match the literal parts in order
            parts = [p for p in name.split("/") if p and p not in ("...", "…")]
            def ok(c: str) -> bool:
                pos = 0
                segs = c.split("/")
                for part in parts:
                    try:
                        pos = segs.index(part, pos) + 1
                    except ValueError:
                        return False
                return True
            narrowed = [c for c in cands if ok(c)]
            if narrowed:
                cands = narrowed
        if not cands:
            return None, 0
        if len(cands) == 1:
            return cands[0], 1
        def rank(c: str) -> tuple[int, int, int, str]:
            segs = c.split("/")
            default_pkg = "defpackage" in segs or (segs[-2].startswith("smali") if len(segs) >= 2 else False)
            return (1 if segs[0].startswith("apktool-output-") else 0, 0 if default_pkg else 1, len(segs), c)
        return sorted(cands, key=rank)[0], len(cands)

    def lines(self, rel: str) -> list[str]:
        if rel not in self._lines:
            self._lines[rel] = (self.apk_root / rel).read_text(encoding="utf-8", errors="replace").split("\n")
        return self._lines[rel]


def _paragraph_bounds(text: str, pos: int) -> tuple[int, int]:
    a = text.rfind("\n\n", 0, pos)
    b = text.find("\n\n", pos)
    return (0 if a < 0 else a + 2, len(text) if b < 0 else b)


def _cells(text: str, line_start: int, line_end: int) -> list[tuple[int, int]]:
    """(start, end) of each cell of the markdown table row occupying text[line_start:line_end]."""
    bars = [i for i in range(line_start, line_end) if text[i] == "|" and (i == line_start or text[i - 1] != "\\")]
    return [(a + 1, b) for a, b in zip(bars, bars[1:])]


def _continuation_file(text: str, pos: int, named: list[tuple[int, int, str]]) -> str | None:
    """The file a bare `:N` refers to (SPEC.md §3): in a table row, the file named last in the
    same cell, else the one file the header row names in that column; in prose, the file
    named last before it in the same paragraph."""
    ls = text.rfind("\n", 0, pos) + 1
    le = text.find("\n", pos)
    le = len(text) if le < 0 else le
    if text[ls:le].lstrip().startswith("|"):
        cells = _cells(text, ls, le)
        col = next((i for i, (a, b) in enumerate(cells) if a <= pos < b), None)
        if col is None:
            return None
        a, b = cells[col]
        same = [n for s, _e, n in named if a <= s < pos]
        if same:
            return same[-1]
        hs = ls  # walk up to the table's first row
        while hs > 0:
            ps = text.rfind("\n", 0, hs - 1) + 1
            if not text[ps:hs].lstrip().startswith("|"):
                break
            hs = ps
        he = text.find("\n", hs)
        hcells = _cells(text, hs, he if he >= 0 else len(text))
        if col < len(hcells):
            ha, hb = hcells[col]
            head = [n for s, _e, n in named if ha <= s < hb]
            if len(head) == 1:
                return head[0]
        return None
    pa, _pb = _paragraph_bounds(text, pos)
    prev = [n for s, _e, n in named if pa <= s < pos]
    return prev[-1] if prev else None


def parse_citations(doc_name: str, text: str) -> list[Citation]:
    """SPEC.md §3: explicit, continuation and bare citations, in document order."""
    found: list[tuple[int, int, Citation]] = []
    taken: list[tuple[int, int]] = []
    for m in _EXPLICIT.finditer(text):
        start = int(m.group(2))
        end = int(m.group(3)) if m.group(3) else start
        found.append((m.start(), m.end(), Citation(doc_name, text.count("\n", 0, m.start()) + 1, m.group(0).strip("`"),
                                                   "explicit", m.group(1), start, end)))
        taken.append((m.start(), m.end()))
    for m in _BARE.finditer(text):
        if any(a <= m.start() < b for a, b in taken):
            continue
        found.append((m.start(), m.end(), Citation(doc_name, text.count("\n", 0, m.start()) + 1, m.group(0).strip("`"),
                                                   "bare", m.group(1))))
    named = sorted((s, e, c.name) for s, e, c in found)
    for m in _CONT.finditer(text):
        if any(a <= m.start() < b for a, b in taken):
            continue
        name = _continuation_file(text, m.start(), named)
        if name is None:
            continue  # no file named earlier in the paragraph/cell/column: not attributable (SPEC.md §7)
        start = int(m.group(1))
        end = int(m.group(2)) if m.group(2) else start
        found.append((m.start(), m.end(), Citation(doc_name, text.count("\n", 0, m.start()) + 1, m.group(0).strip("`"),
                                                   "continuation", name, start, end)))
    found.sort(key=lambda t: t[0])
    for i, (s, e, c) in enumerate(found):
        c._span = (s, e)  # type: ignore[attr-defined]
    return [c for _s, _e, c in found]


def _expected_tokens(text: str, span: tuple[int, int], neighbours: list[tuple[int, int]]) -> list[str]:
    """Quoted strings and backticked code in the citing sentence: the text between the
    previous and the next citation, at most _WINDOW characters each side, inside the paragraph."""
    pa, pb = _paragraph_bounds(text, span[0])
    lo = max(pa, span[0] - _WINDOW, max((e for _s, e in neighbours if e <= span[0]), default=0))
    hi = min(pb, span[1] + _WINDOW, min((s for s, _e in neighbours if s >= span[1]), default=len(text)))
    window = " ".join(text[lo:hi].split())
    tokens: list[str] = []
    for m in _QUOTED.finditer(window):
        tokens.append(m.group(1))
    for m in _CODE.finditer(window):
        t = m.group(1).strip()
        if _PATHLIKE.match(t) or t.startswith(":") or " " not in t and "." not in t and "(" not in t and ">" not in t:
            continue  # a file name, a continuation, or a bare word (class names occur everywhere)
        tokens.append(t)
    out: list[str] = []
    for t in tokens:
        if t not in out:
            out.append(t)
    return out


def _variants(token: str) -> list[str]:
    v = [token]
    if "\\" not in token and '"' in token:
        v.append(token.replace('"', '\\"'))
    if token.endswith(("…", "...")):
        v.append(token.rstrip("….").rstrip())
    return v


def check_document(doc_path: Path, index: TreeIndex, slack: int = DEFAULT_SLACK, doc_name: str | None = None) -> list[Citation]:
    text = Path(doc_path).read_text(encoding="utf-8", errors="replace")
    cites = parse_citations(doc_name or str(doc_path), text)
    spans = [c._span for c in cites]  # type: ignore[attr-defined]
    for c in cites:
        span = c._span  # type: ignore[attr-defined]
        del c._span  # type: ignore[attr-defined]
        c.resolved, c.candidates = index.resolve(c.name)
        if c.resolved is None:
            c.verdict = "missing_file"
            continue
        lines = index.lines(c.resolved)
        c.file_lines = len(lines)
        if c.kind == "bare":
            c.verdict = "exists"
            continue
        if not (1 <= c.start <= c.end <= len(lines)):
            c.verdict = "range_out_of_file"
            continue
        lo, hi = max(1, c.start - slack), min(len(lines), c.end + slack)
        others = [s for s in spans if s != span]
        any_in_range = False
        nearest: int | None = None
        for tok in _expected_tokens(text, span, others):
            hits = [n for n, line in enumerate(lines, 1) if any(v in line for v in _variants(tok))]
            if not hits:
                continue  # the sentence quotes it about something else (another file, the wire, the UI)
            in_range = any(lo <= n <= hi for n in hits)
            any_in_range = any_in_range or in_range
            if not in_range:
                d = min(hits, key=lambda n: min(abs(n - c.start), abs(n - c.end)))
                if nearest is None or min(abs(d - c.start), abs(d - c.end)) < min(abs(nearest - c.start), abs(nearest - c.end)):
                    nearest = d
            c.tokens.append(TokenCheck(tok, in_range, hits[:5]))
        if any_in_range:
            c.verdict = "confirmed"
        elif c.tokens:
            c.verdict = "drifted"
            c.nearest_token_line = nearest
        else:
            c.verdict = "in_file_no_token"
    return cites


def summarize(cites: list[Citation]) -> dict:
    by_doc: dict[str, dict[str, int]] = {}
    total: dict[str, int] = {}
    for c in cites:
        by_doc.setdefault(c.doc, {})[c.verdict] = by_doc.setdefault(c.doc, {}).get(c.verdict, 0) + 1
        total[c.verdict] = total.get(c.verdict, 0) + 1
    return {"total_citations": len(cites), "by_verdict": dict(sorted(total.items())), "by_document": by_doc}
