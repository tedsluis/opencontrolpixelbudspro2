"""SPEC.md §5 — the `extract` and `context` commands."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from .uuid_scan import context, extract


def _cmd_extract(args: argparse.Namespace) -> int:
    try:
        result = extract(Path(args.apk_root))
    except FileNotFoundError as e:
        print(f"uuid-ble-context: error: {e}", file=sys.stderr)
        return 1
    _emit(result.to_dict(), args.output_dir, "uuid_register.json")
    return 0


def _cmd_context(args: argparse.Namespace) -> int:
    try:
        result = context(Path(args.apk_root), args.uuid)
    except FileNotFoundError as e:
        print(f"uuid-ble-context: error: {e}", file=sys.stderr)
        return 1
    except KeyError as e:
        print(f"uuid-ble-context: error: UUID {e} not found under {args.apk_root}", file=sys.stderr)
        return 1
    _emit(result.to_dict(), args.output_dir, f"{result.uuid}_context.json")
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
    parser = argparse.ArgumentParser(prog="uuid-ble-context")
    sub = parser.add_subparsers(dest="command", required=True)

    p_extract = sub.add_parser("extract", help="every UUID-shaped literal in the tree, with occurrences")
    p_extract.add_argument("--apk-root", required=True)
    p_extract.add_argument("--output-dir", default=None)
    p_extract.set_defaults(func=_cmd_extract)

    p_context = sub.add_parser("context", help="one UUID's occurrences + structural_index usage graph")
    p_context.add_argument("--apk-root", required=True)
    p_context.add_argument("--uuid", required=True)
    p_context.add_argument("--output-dir", default=None)
    p_context.set_defaults(func=_cmd_context)

    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
