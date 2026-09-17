"""Output schema — SPEC.md §6. Plain dataclasses, JSON-serializable via `to_dict()`."""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class TraceStep:
    line: int  # 1-indexed
    kind: str  # "alias" | "cast" | "sink_use"
    register: str
    # "alias"
    from_register: str | None = None
    # "cast"
    to_type: str | None = None
    # "sink_use"
    arg_position: int | None = None
    invoke_kind: str | None = None
    called_class: str | None = None
    called_method: str | None = None

    def to_dict(self) -> dict:
        d = {"line": self.line, "kind": self.kind, "register": self.register}
        for k in ("from_register", "to_type", "arg_position", "invoke_kind", "called_class", "called_method"):
            v = getattr(self, k)
            if v is not None:
                d[k] = v
        return d


@dataclass
class TraceResult:
    start_line: int
    start_register: str
    steps: list[TraceStep] = field(default_factory=list)
    final_status: str = "reached_end_of_block"  # | "left_basic_block_scope" | "ambiguous_redefinition"
    final_register: str | None = None
    stop_line: int | None = None  # populated for left_basic_block_scope / ambiguous_redefinition
    stop_text: str | None = None
    extra: dict = field(default_factory=dict)  # class/discriminator/method identifying context

    def to_dict(self) -> dict:
        d = dict(self.extra)
        d.update(
            {
                "start_line": self.start_line,
                "start_register": self.start_register,
                "steps": [s.to_dict() for s in self.steps],
                "final_status": self.final_status,
                "final_register": self.final_register,
            }
        )
        if self.stop_line is not None:
            d["stop_line"] = self.stop_line
            d["stop_text"] = self.stop_text
        return d
