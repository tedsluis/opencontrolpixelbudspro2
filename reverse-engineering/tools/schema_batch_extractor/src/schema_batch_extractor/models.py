"""Output schema — SPEC.md §6. Plain dataclasses, JSON-serializable via `to_dict()`."""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class DecodedField:
    field_number: int
    type_name: str
    is_oneof: bool
    oneof_index: int | None = None
    java_field: str | None = None
    # Only populated where the compact RawMessageInfo schema string itself carries a
    # message-class reference for this field shape (oneof MESSAGE/GROUP, repeated
    # MESSAGE_LIST/GROUP_LIST, or a MAP's default-entry descriptor) -- SPEC.md §3's
    # disclosed limitation: a plain singular MESSAGE/GROUP field carries no such
    # reference in the info string at all, so this stays None for those, always.
    message_ref: str | None = None
    has_presence: bool = False
    hasbit: int | None = None

    def to_dict(self) -> dict:
        return {
            "field_number": self.field_number,
            "type_name": self.type_name,
            "is_oneof": self.is_oneof,
            "oneof_index": self.oneof_index,
            "java_field": self.java_field,
            "message_ref": self.message_ref,
            "has_presence": self.has_presence,
            "hasbit": self.hasbit,
        }


@dataclass
class SchemaEntry:
    cls: str  # e.g. "defpackage.qhr"
    file: str
    field_count: int
    oneof_count: int
    map_field_count: int
    min_field_number: int
    max_field_number: int
    fields: list[DecodedField] = field(default_factory=list)
    message_refs: list[str] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "class": self.cls,
            "file": self.file,
            "field_count": self.field_count,
            "oneof_count": self.oneof_count,
            "map_field_count": self.map_field_count,
            "min_field_number": self.min_field_number,
            "max_field_number": self.max_field_number,
            "fields": [f.to_dict() for f in self.fields],
            "message_refs": self.message_refs,
        }


@dataclass
class ScanResult:
    apk_root: str
    total_java_files_scanned: int
    total_candidates: int
    total_decoded: int
    unparsable: list[str] = field(default_factory=list)
    classes: list[SchemaEntry] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "apk_root": self.apk_root,
            "total_java_files_scanned": self.total_java_files_scanned,
            "total_candidates": self.total_candidates,
            "total_decoded": self.total_decoded,
            "unparsable": self.unparsable,
            "classes": [c.to_dict() for c in self.classes],
        }


@dataclass
class ReferencedBy:
    cls: str
    field_number: int
    context: str  # "oneof" | "list" | "map"


@dataclass
class RefsResult:
    cls: str
    referenced_by: list[ReferencedBy] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "class": self.cls,
            "referenced_by": [
                {"class": r.cls, "field_number": r.field_number, "context": r.context}
                for r in self.referenced_by
            ],
        }
