"""SPEC.md §5 — the `refs` and `unreferenced` commands."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from .xref_index import find_refs, find_unreferenced, load_apk


def _cmd_refs(args: argparse.Namespace) -> int:
    loaded = load_apk(Path(args.apk_root))
    try:
        result = find_refs(loaded, args.cls, method_filter=args.method)
    except KeyError as e:
        print(f"structural-index: error: class {e} not found under {args.apk_root}", file=sys.stderr)
        return 1
    payload = result.to_dict()
    _emit(payload, args.output_dir, f"{result.cls.rsplit('.', 1)[-1]}_refs.json")
    return 0


def _cmd_unreferenced(args: argparse.Namespace) -> int:
    loaded = load_apk(Path(args.apk_root))
    try:
        results = find_unreferenced(loaded, args.cls)
    except KeyError as e:
        print(f"structural-index: error: class {e} not found under {args.apk_root}", file=sys.stderr)
        return 1
    payload = [r.to_dict() for r in results]
    _emit(payload, args.output_dir, "unreferenced.json")
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
    parser = argparse.ArgumentParser(prog="structural-index")
    sub = parser.add_subparsers(dest="command", required=True)

    p_refs = sub.add_parser("refs", help="constructs/calls/field-type-holders for one class")
    p_refs.add_argument("--apk-root", required=True)
    p_refs.add_argument("--class", dest="cls", required=True)
    p_refs.add_argument("--method", default=None, help="restrict (b) call sites to this method name")
    p_refs.add_argument("--output-dir", default=None)
    p_refs.set_defaults(func=_cmd_refs)

    p_unref = sub.add_parser("unreferenced", help="check whether each given class has zero external references")
    p_unref.add_argument("--apk-root", required=True)
    p_unref.add_argument("--class", dest="cls", action="append", required=True)
    p_unref.add_argument("--output-dir", default=None)
    p_unref.set_defaults(func=_cmd_unreferenced)

    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
