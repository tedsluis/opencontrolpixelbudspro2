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

    def to_dict(self) -> dict:
        return {"uuid": self.uuid, "occurrences": [o.to_dict() for o in self.occurrences]}


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
