"""Output schema — SPEC.md §6. Plain dataclasses, JSON-serializable via dataclasses.asdict()."""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class ConstructSite:
    caller_class: str
    caller_method: str
    dex_file: str


@dataclass
class CallSite:
    caller_class: str
    caller_method: str
    called_method: str
    invoke_kind: str
    dex_file: str


@dataclass
class FieldTypeHolder:
    holder_class: str
    field_name: str
    dex_file: str


@dataclass
class RefsResult:
    cls: str
    constructs: list[ConstructSite] = field(default_factory=list)
    calls: list[CallSite] = field(default_factory=list)
    field_type_holders: list[FieldTypeHolder] = field(default_factory=list)

    def counts(self) -> dict[str, int]:
        return {
            "constructs": len(self.constructs),
            "calls": len(self.calls),
            "field_type_holders": len(self.field_type_holders),
        }

    def to_dict(self) -> dict:
        return {
            "class": self.cls,
            "constructs": [vars(c) for c in self.constructs],
            "calls": [vars(c) for c in self.calls],
            "field_type_holders": [vars(f) for f in self.field_type_holders],
            "counts": self.counts(),
        }


@dataclass
class UnreferencedResult:
    cls: str
    total_references: int
    referenced: bool

    def to_dict(self) -> dict:
        return {
            "class": self.cls,
            "total_references": self.total_references,
            "referenced": self.referenced,
        }


@dataclass
class ImplementerSite:
    implementer_class: str
    dex_file: str


@dataclass
class ImplementsResult:
    interface: str
    implementers: list[ImplementerSite] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "interface": self.interface,
            "implementers": [vars(i) for i in self.implementers],
            "count": len(self.implementers),
        }


@dataclass
class FieldWriteSite:
    caller_class: str
    caller_method: str
    dex_file: str


@dataclass
class FieldWritesResult:
    cls: str
    field_name: str
    writes: list[FieldWriteSite] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "class": self.cls,
            "field": self.field_name,
            "writes": [vars(w) for w in self.writes],
            "count": len(self.writes),
        }
