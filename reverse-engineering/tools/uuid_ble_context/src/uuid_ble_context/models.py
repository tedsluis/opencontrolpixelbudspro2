"""Output schema — SPEC.md §6. Plain dataclasses, JSON-serializable via `to_dict()`."""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class Occurrence:
    file: str
    line: int
    cls: str
    bt_api_cooccurrence: bool

    def to_dict(self) -> dict:
        return {
            "file": self.file,
            "line": self.line,
            "class": self.cls,
            "bt_api_cooccurrence": self.bt_api_cooccurrence,
        }


@dataclass
class UuidEntry:
    uuid: str
    occurrences: list[Occurrence] = field(default_factory=list)
    # v1.1 (2026-10-06, SPEC.md §3a): the 16 bytes in reverse order, as a UUID string, and whether
    # that form is itself a literal of the tree (then the two entries are one UUID in two byte orders).
    byte_reversed: str = ""
    byte_reversed_in_tree: bool = False

    def to_dict(self) -> dict:
        return {"uuid": self.uuid, "byte_reversed": self.byte_reversed, "byte_reversed_in_tree": self.byte_reversed_in_tree,
                "occurrences": [o.to_dict() for o in self.occurrences]}


@dataclass
class ExtractResult:
    apk_root: str
    total_java_files_scanned: int
    total_uuids_found: int
    uuids: list[UuidEntry] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "apk_root": self.apk_root,
            "total_java_files_scanned": self.total_java_files_scanned,
            "total_uuids_found": self.total_uuids_found,
            "uuids": [u.to_dict() for u in self.uuids],
        }


@dataclass
class FindResult:
    """v1.1 `find` (SPEC.md §5a): where a given UUID occurs as text, in either byte order, with and
    without dashes, across every tree root -- the positive or checked-negative answer for a UUID that
    comes from a capture, not from the tree."""
    uuid: str
    byte_reversed: str
    forms_searched: list[str] = field(default_factory=list)
    files_scanned: dict = field(default_factory=dict)  # tree root -> number of files read
    hits: list[dict] = field(default_factory=list)  # {"form", "file", "line"}

    def to_dict(self) -> dict:
        return {"uuid": self.uuid, "byte_reversed": self.byte_reversed, "forms_searched": self.forms_searched,
                "files_scanned": self.files_scanned, "total_hits": len(self.hits), "hits": self.hits}


@dataclass
class ContextResult:
    uuid: str
    occurrences: list[Occurrence] = field(default_factory=list)
    usage_by_class: dict = field(default_factory=dict)  # class name -> structural_index RefsResult.to_dict(), or {"note": ...}

    def to_dict(self) -> dict:
        return {
            "uuid": self.uuid,
            "occurrences": [o.to_dict() for o in self.occurrences],
            "usage_by_class": self.usage_by_class,
        }
