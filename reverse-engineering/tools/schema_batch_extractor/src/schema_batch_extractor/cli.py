"""SPEC.md §5 — the `scan` and `refs` commands."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from .batch_extract import find_refs, scan


def _cmd_scan(args: argparse.Namespace) -> int:
    try:
        result = scan(Path(args.apk_root), class_filter=args.cls, min_field_count=args.min_field_count)
    except FileNotFoundError as e:
        print(f"schema-batch-extractor: error: {e}", file=sys.stderr)
        return 1
    except KeyError as e:
        print(f"schema-batch-extractor: error: class {e} not found under {args.apk_root}", file=sys.stderr)
        return 1
    _emit(result.to_dict(), args.output_dir, "schema_register.json")
    return 0


def _cmd_refs(args: argparse.Namespace) -> int:
    try:
        result = find_refs(Path(args.apk_root), args.cls)
    except FileNotFoundError as e:
        print(f"schema-batch-extractor: error: {e}", file=sys.stderr)
        return 1
    except KeyError as e:
        print(f"schema-batch-extractor: error: class {e} not found under {args.apk_root}", file=sys.stderr)
        return 1
    _emit(result.to_dict(), args.output_dir, f"{result.cls.rsplit('.', 1)[-1]}_refs.json")
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
    parser = argparse.ArgumentParser(prog="schema-batch-extractor")
    sub = parser.add_subparsers(dest="command", required=True)

    p_scan = sub.add_parser("scan", help="decode every RawMessageInfo-shaped class in the tree")
    p_scan.add_argument("--apk-root", required=True)
    p_scan.add_argument("--class", dest="cls", action="append", default=None, help="filter output to these classes only")
    p_scan.add_argument("--min-field-count", type=int, default=None)
    p_scan.add_argument("--output-dir", default=None)
    p_scan.set_defaults(func=_cmd_scan)

    p_refs = sub.add_parser("refs", help="which classes' schemas reference this class (oneof/list/map only)")
    p_refs.add_argument("--apk-root", required=True)
    p_refs.add_argument("--class", dest="cls", required=True)
    p_refs.add_argument("--output-dir", default=None)
    p_refs.set_defaults(func=_cmd_refs)

    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
