"""SPEC.md §5 — `census`, `match` and `hash`."""

from __future__ import annotations

import argparse
import glob
import json
import sys
from pathlib import Path

from .names import census, h65599, match

DEFAULT_LOGS = "captures/*/*btsno*_hci*.log*"


def _emit(payload, output: str | None) -> None:
    text = json.dumps(payload, indent=1, ensure_ascii=False)
    if output:
        Path(output).parent.mkdir(parents=True, exist_ok=True)
        Path(output).write_text(text + "\n")
    else:
        print(text)


def _cmd_census(args: argparse.Namespace) -> int:
    logs = [Path(p) for pat in (args.log or [str(Path(args.repo_root) / DEFAULT_LOGS)]) for p in sorted(glob.glob(pat))]
    if not logs:
        print("pwrpc-name-table: error: no capture log matches", file=sys.stderr)
        return 1
    result = census(logs)
    _emit(result, args.output)
    print(f"{len(result['logs'])} logs, {sum(result['logs'].values())} packets, {len(result['services'])} service ids, "
          f"{len(result['methods'])} method ids, {len(result['errors'])} unreadable", file=sys.stderr)
    return 0


def _cmd_match(args: argparse.Namespace) -> int:
    try:
        wire = json.loads(Path(args.wire_ids).read_text())
    except (OSError, ValueError) as e:
        print(f"pwrpc-name-table: error: cannot read {args.wire_ids}: {e}", file=sys.stderr)
        return 1
    try:
        result = match(Path(args.apk_root), wire)
    except FileNotFoundError as e:
        print(f"pwrpc-name-table: error: {e}", file=sys.stderr)
        return 1
    _emit(result, args.output)
    print(f"{result['distinct_literals_hashed']} distinct literals hashed; unnamed: {len(result['unnamed_services'])} of "
          f"{result['wire_service_ids']} services, {len(result['unnamed_methods'])} of {result['wire_method_ids']} methods",
          file=sys.stderr)
    return 0


def _cmd_hash(args: argparse.Namespace) -> int:
    for name in args.name:
        print(f"{h65599(name):#010x}  {name}")
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="pwrpc-name-table")
    sub = parser.add_subparsers(dest="command", required=True)
    p = sub.add_parser("census", help="every pw_rpc service/method id on the wire of the capture logs")
    p.add_argument("--repo-root", default=str(Path(__file__).resolve().parents[5]))
    p.add_argument("--log", action="append", default=None, help="a log or glob (repeatable); default: every capture log")
    p.add_argument("--output", default=None)
    p.set_defaults(func=_cmd_census)
    p = sub.add_parser("match", help="hash every string literal of the tree and name the wire ids")
    p.add_argument("--apk-root", required=True)
    p.add_argument("--wire-ids", required=True, help="the JSON written by `census`")
    p.add_argument("--output", default=None)
    p.set_defaults(func=_cmd_match)
    p = sub.add_parser("hash", help="print the 65599 hash of the given names")
    p.add_argument("name", nargs="+")
    p.set_defaults(func=_cmd_hash)
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
