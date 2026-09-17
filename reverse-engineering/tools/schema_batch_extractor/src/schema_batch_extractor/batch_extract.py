"""SPEC.md §3/§4 — the batch scan and reverse-reference query, built directly on
`scripts/decode_rawmessageinfo.py` (imported via a sys.path insertion to the
repo-root `scripts/` directory, rather than re-implemented or modified —
SPEC.md §4's own "reuse, stated precisely" note).
"""

from __future__ import annotations

import sys
from pathlib import Path

# Repo layout: reverse-engineering/tools/schema_batch_extractor/src/schema_batch_extractor/batch_extract.py
# parents[0]=schema_batch_extractor(pkg) [1]=src [2]=schema_batch_extractor(tool dir) [3]=tools
# [4]=reverse-engineering [5]=repo root
_REPO_ROOT = Path(__file__).resolve().parents[5]
_SCRIPTS_DIR = _REPO_ROOT / "scripts"
if str(_SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(_SCRIPTS_DIR))

import decode_rawmessageinfo as _raw  # noqa: E402

from .models import DecodedField, ReferencedBy, RefsResult, ScanResult, SchemaEntry  # noqa: E402

__all__ = ["scan", "find_refs"]


def _class_name_from_path(java_file: Path, sources_root: Path) -> str:
    """"jadx-output/sources/defpackage/qhr.java" -> "defpackage.qhr";
    "jadx-output/sources/com/foo/Bar.java" -> "com.foo.Bar" -- JADX's own
    package-to-directory convention, matching structural_index's `_short_name`
    output style so class names are comparable across both tools' JSON."""
    rel = java_file.relative_to(sources_root)
    return ".".join(rel.with_suffix("").parts)


def _strip_class_suffix(ref_src: str | None) -> str | None:
    """"qju.class" -> "qju"; leaves anything not matching this exact shape as-is
    (never guessed at, per AGENTS.md §13.6 -- if the source text isn't the plain
    `<Name>.class` form, the raw text is kept rather than mangled)."""
    if ref_src is None:
        return None
    if ref_src.endswith(".class"):
        return ref_src[: -len(".class")]
    return ref_src


def _field_message_ref(entry: dict) -> tuple[str | None, str | None]:
    """Returns (referenced_class_or_None, context_or_None). Only three field
    shapes carry a message-class reference in the compact schema string at all
    (SPEC.md §3): a oneof MESSAGE/GROUP alternative, a repeated
    MESSAGE_LIST/GROUP_LIST field, and a MAP field's default-entry descriptor.
    A plain singular MESSAGE/GROUP field carries none -- returns (None, None),
    by design, not a gap in this function."""
    if "message_class_ref" in entry and entry["message_class_ref"]:
        context = "oneof" if entry.get("is_oneof") else "list"
        return _strip_class_suffix(entry["message_class_ref"]), context
    if "map_default_entry_ref" in entry and entry["map_default_entry_ref"]:
        return _strip_class_suffix(entry["map_default_entry_ref"]), "map"
    return None, None


def _to_decoded_fields(decoded: dict) -> list[DecodedField]:
    out: list[DecodedField] = []
    for entry in decoded["fields"]:
        ref, _context = _field_message_ref(entry)
        out.append(
            DecodedField(
                field_number=entry["field_number"],
                type_name=entry["type_name"],
                is_oneof=entry.get("is_oneof", False),
                oneof_index=entry.get("oneof_index"),
                java_field=entry.get("java_field"),
                message_ref=ref,
                has_presence=entry.get("supports_presence", False),
                hasbit=entry.get("hasbits_index"),
            )
        )
    return out


def _to_schema_entry(cls_name: str, file_path: Path, decoded: dict) -> SchemaEntry:
    fields = _to_decoded_fields(decoded)
    # message_refs: deduped, in field-number order -- every field whose own
    # RawMessageInfo entry carries a class reference (SPEC.md §3).
    seen: set[str] = set()
    message_refs: list[str] = []
    for f in fields:
        if f.message_ref is not None and f.message_ref not in seen:
            seen.add(f.message_ref)
            message_refs.append(f.message_ref)
    return SchemaEntry(
        cls=cls_name,
        file=str(file_path),
        field_count=decoded.get("field_count", 0),
        oneof_count=decoded.get("oneof_count", 0),
        map_field_count=decoded.get("map_field_count", 0),
        min_field_number=decoded.get("min_field_number", 0),
        max_field_number=decoded.get("max_field_number", 0),
        fields=fields,
        message_refs=message_refs,
    )


def scan(apk_root: Path, class_filter: list[str] | None = None, min_field_count: int | None = None) -> ScanResult:
    """SPEC.md §5's `scan` command. Always decodes the full tree (§5's own
    "never a shortcut" contract) -- `class_filter`/`min_field_count` only
    narrow what's *returned*, after the fact; `total_candidates`/`total_decoded`
    always describe the full, unfiltered run."""
    sources_root = apk_root / "jadx-output" / "sources"
    if not sources_root.is_dir():
        raise FileNotFoundError(f"no jadx-output/sources/ found under {apk_root}")

    java_files = list(sources_root.rglob("*.java"))
    all_classes: list[SchemaEntry] = []
    unparsable: list[str] = []
    total_candidates = 0

    for jf in java_files:
        try:
            text = jf.read_text(encoding="utf-8", errors="replace")
        except OSError as e:  # pragma: no cover -- SPEC.md §7's "skip, don't abort" rule
            print(f"schema-batch-extractor: warning: could not read {jf}: {e}", file=sys.stderr)
            continue
        if "new naa(" not in text:
            continue

        constructions = _raw.find_naa_constructions(text)
        cls_name = _class_name_from_path(jf, sources_root)
        for c in constructions:
            total_candidates += 1
            if c["objects"] is None:
                unparsable.append(str(jf))
                continue
            decoded = _raw.decode_info_string(c["info_string"], c["objects"])
            all_classes.append(_to_schema_entry(cls_name, jf, decoded))

    output_classes = all_classes
    if class_filter:
        wanted = {_normalize(name) for name in class_filter}
        known_short = {c.cls.rsplit(".", 1)[-1] for c in all_classes}
        known_full = {c.cls for c in all_classes}
        for name in wanted:
            if name not in known_short and name not in known_full:
                raise KeyError(name)
        output_classes = [c for c in output_classes if c.cls in wanted or c.cls.rsplit(".", 1)[-1] in wanted]

    if min_field_count is not None:
        output_classes = [c for c in output_classes if c.field_count >= min_field_count]

    return ScanResult(
        apk_root=str(apk_root),
        total_java_files_scanned=len(java_files),
        total_candidates=total_candidates,
        total_decoded=len(all_classes),
        unparsable=unparsable,
        classes=output_classes,
    )


def _normalize(name: str) -> str:
    """Accepts "qhr", "defpackage.qhr" -- returns the short form used to compare
    against a class's own trailing path segment. Does not accept/require the
    raw "Lqhr;" smali descriptor (this tool never touches DEX/smali data)."""
    n = name.strip()
    if n.startswith("defpackage."):
        return n[len("defpackage.") :]
    return n


def find_refs(apk_root: Path, class_name: str) -> RefsResult:
    """SPEC.md §5's `refs` command: runs a full `scan` once, then returns every
    class in the register whose own decoded schema references `class_name` via
    a oneof/list/map field (SPEC.md §3's disclosed scope). Raises KeyError if
    `class_name` is not present anywhere in the register at all -- i.e. it has
    no `new naa(...)` schema of its own (SPEC.md §7). This does NOT require
    `class_name` to itself be *referenced*: a class with a real schema but zero
    incoming oneof/list/map references legitimately returns an empty
    `referenced_by` list (SPEC.md §7's "legitimate zero" framing) -- only a
    class this register never decoded at all is an error."""
    result = scan(apk_root)
    target_short = _normalize(class_name).rsplit(".", 1)[-1]

    known_short = {c.cls.rsplit(".", 1)[-1] for c in result.classes}
    if target_short not in known_short:
        raise KeyError(target_short)

    referenced_by: list[ReferencedBy] = []
    for entry in result.classes:
        for f in entry.fields:
            if f.message_ref is not None and f.message_ref.rsplit(".", 1)[-1] == target_short:
                context = "oneof" if f.is_oneof else ("map" if f.type_name == "MAP" else "list")
                referenced_by.append(ReferencedBy(cls=entry.cls, field_number=f.field_number, context=context))

    resolved_full = next((c.cls for c in result.classes if c.cls.rsplit(".", 1)[-1] == target_short), target_short)
    return RefsResult(cls=resolved_full, referenced_by=referenced_by)
