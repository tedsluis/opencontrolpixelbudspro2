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
