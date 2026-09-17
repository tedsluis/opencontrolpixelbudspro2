"""SPEC.md §5 — the `trace-branch` and `trace-method` commands."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from .dataflow import trace_branch, trace_method


def _cmd_trace_branch(args: argparse.Namespace) -> int:
    try:
        result = trace_branch(
            Path(args.apk_root), args.cls, args.discriminator, args.start_line, args.start_register
        )
    except (KeyError, FileNotFoundError, ValueError) as e:
        print(f"limited-dataflow: error: {e}", file=sys.stderr)
        return 1
    _emit(result.to_dict(), args.output_dir, "trace_branch.json")
    return 0


def _cmd_trace_method(args: argparse.Namespace) -> int:
    try:
        result = trace_method(Path(args.apk_root), args.cls, args.method, args.start_line, args.start_register)
    except (KeyError, FileNotFoundError, ValueError) as e:
        print(f"limited-dataflow: error: {e}", file=sys.stderr)
        return 1
    _emit(result.to_dict(), args.output_dir, "trace_method.json")
    return 0


def _emit(payload, output_dir: str | None, filename: str) -> None:
    text = json.dumps(payload, indent=2)
    if output_dir:
        out_path = Path(output_dir)
        out_path.mkdir(parents=True, exist_ok=True)
        (out_path / filename).write_text(text + "\n")
    else:
        print(text)


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="limited-dataflow")
    sub = parser.add_subparsers(dest="command", required=True)

    p_branch = sub.add_parser("trace-branch", help="single-basic-block trace over one resolved dispatcher branch")
    p_branch.add_argument("--apk-root", required=True)
    p_branch.add_argument("--class", dest="cls", required=True)
    p_branch.add_argument("--discriminator", type=int, required=True)
    p_branch.add_argument("--start-line", type=int, required=True)
    p_branch.add_argument("--start-register", required=True)
    p_branch.add_argument("--output-dir", default=None)
    p_branch.set_defaults(func=_cmd_trace_branch)

    p_method = sub.add_parser("trace-method", help="single-basic-block trace over any plain method body")
    p_method.add_argument("--apk-root", required=True)
    p_method.add_argument("--class", dest="cls", required=True)
    p_method.add_argument("--method", required=True)
    p_method.add_argument("--start-line", type=int, required=True)
    p_method.add_argument("--start-register", required=True)
    p_method.add_argument("--output-dir", default=None)
    p_method.set_defaults(func=_cmd_trace_method)

    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
