"""SPEC.md §5 — the `check` command."""

from __future__ import annotations

import argparse
import glob
import json
import sys
from pathlib import Path

from .check import DEFAULT_SLACK, TreeIndex, check_document, summarize

# The documents that cite the decompiled tree (SPEC.md §5); paths relative to --repo-root.
DEFAULT_DOCS = (
    "REVERSE_ENGINEERING.md",
    "PROTOCOL.md",
    "DECISIONS.md",
    "DESKRESEARCH_FINDINGS.md",
    "ARCHITECTURE.md",
    "TODO.md",
    "APK_REVERSE_ENGINEERING_PROCEDURE.md",
    "captures/*/CAP-*-FINDINGS.md",
)


def _cmd_check(args: argparse.Namespace) -> int:
    repo = Path(args.repo_root).resolve()
    try:
        index = TreeIndex(Path(args.apk_root))
    except FileNotFoundError as e:
        print(f"citation-checker: error: {e}", file=sys.stderr)
        return 1
    patterns = args.doc or list(DEFAULT_DOCS)
    docs: list[Path] = []
    for pat in patterns:
        hits = sorted(glob.glob(str(repo / pat))) if not Path(pat).is_absolute() else sorted(glob.glob(pat))
        if not hits:
            print(f"citation-checker: error: no document matches {pat!r} under {repo}", file=sys.stderr)
            return 1
        docs.extend(Path(h) for h in hits)
    cites = []
    for d in docs:
        try:
            name = str(d.relative_to(repo))
        except ValueError:
            name = str(d)
        cites.extend(check_document(d, index, slack=args.slack, doc_name=name))
    if args.verdict:
        shown = [c for c in cites if c.verdict in args.verdict]
    else:
        shown = cites
    payload = {"apk_root": str(args.apk_root), "slack_lines": args.slack, "documents": len(docs),
               "summary": summarize(cites), "citations": [c.to_dict() for c in shown]}
    text = json.dumps(payload, indent=2, ensure_ascii=False)
    if args.output:
        Path(args.output).parent.mkdir(parents=True, exist_ok=True)
        Path(args.output).write_text(text + "\n")
        print(json.dumps(payload["summary"], indent=2, ensure_ascii=False))
    else:
        print(text)
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="citation-checker")
    sub = parser.add_subparsers(dest="command", required=True)
    p = sub.add_parser("check", help="check every file:line citation into the decompiled tree")
    p.add_argument("--apk-root", required=True)
    p.add_argument("--repo-root", default=str(Path(__file__).resolve().parents[5]))
    p.add_argument("--doc", action="append", default=None,
                   help="a document or glob relative to --repo-root (repeatable); default: the documents of SPEC.md §5")
    p.add_argument("--slack", type=int, default=DEFAULT_SLACK, help="lines either side of a range that still count as in range")
    p.add_argument("--verdict", action="append", default=None, help="only list citations with this verdict (repeatable)")
    p.add_argument("--output", default=None, help="write the JSON here and print only the summary")
    p.set_defaults(func=_cmd_check)
    return parser


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
