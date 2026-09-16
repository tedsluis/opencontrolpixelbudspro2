"""Output data model for the Lambda Dispatcher Resolver.

Mirrors SPEC.md §6's JSON schema exactly. These are plain, JSON-serializable
dataclasses — no behavior, no relevance judgments (SPEC.md §8): every field
here describes a mechanical lookup result, never a claim about what a branch
means for the Bluetooth protocol.
"""

from __future__ import annotations

from dataclasses import asdict, dataclass, field
from typing import Literal, Optional

DispatchMechanism = Literal["packed-switch", "sparse-switch", "if-chain"]
ResolutionStatus = Literal["resolved", "resolved-default-branch", "ambiguous", "error"]


@dataclass
class SmaliEvidence:
    path: str
    method_line_range: tuple[int, int]
    branch_label: Optional[str]
    branch_line_range: Optional[tuple[int, int]]
    raw_text: str


@dataclass
class JadxEvidence:
    path: Optional[str]
    decompilable: bool
    # True only when a textual `case N:` (or its literal-cast form) was found
    # and used to bound raw_text to that specific branch. False for if-chain
    # dispatch (JADX renders those as nested if/ternary expressions with no
    # `case` label to search for — see SPEC.md's discovered limitation,
    # recorded in README.md) or when the case label could not be located.
    case_text_found: bool
    note: str
    raw_text: Optional[str] = None


@dataclass
class DispatcherCandidate:
    class_name: str
    dex_file: str
    interface_implemented: str
    discriminator_field: str


@dataclass
class ResolveResult:
    class_name: str
    dex_file: Optional[str]
    interface_implemented: Optional[str]
    discriminator_field: Optional[str]
    discriminator_value: int
    dispatch_mechanism: Optional[DispatchMechanism]
    case_count: Optional[int]
    smali: Optional[SmaliEvidence]
    jadx: Optional[JadxEvidence]
    resolution_status: ResolutionStatus
    error: Optional[str] = None

    def to_json_dict(self) -> dict:
        return asdict(self)
