"""CLI — `lambda-resolver list` / `resolve` / `resolve-all` (SPEC.md §5, extended).

No file under `reverse-engineering/apk/` is ever written to (SPEC.md §8) —
this module only reads, and writes JSON to stdout or to `--output-dir` (which
is never inside `reverse-engineering/apk/`; it's scratch output the caller
chooses, same as any other tool result under this project's conventions).
"""

from __future__ import annotations

import argparse
import json
import sys
from dataclasses import dataclass
from pathlib import Path

from . import androguard_index, jadx_correlate, smali_reader
from .androguard_index import LoadedApk
from .models import DispatcherCandidate, JadxEvidence, ResolveResult
from .smali_reader import DispatchInfo, MethodBody


def _load(apk_root: Path) -> LoadedApk:
    return androguard_index.load_apk(apk_root)


def cmd_list(apk_root: Path, interface_hint: str | None = None) -> list[DispatcherCandidate]:
    loaded = _load(apk_root)
    candidates = androguard_index.find_candidates(loaded)
    if interface_hint is None:
        return candidates
    # A filter on an already-computed detection result, per SPEC.md §5 — it
    # does not change which classes count as candidates (§3), only which of
    # them get printed.
    normalized = interface_hint if interface_hint.startswith("L") else f"L{interface_hint.replace('.', '/')};"
    return [c for c in candidates if c.interface_implemented == normalized]


@dataclass
class ClassAnalysis:
    """Everything about one class that's independent of *which* discriminator
    value is being resolved — computed once, reused by every value
    `resolve-all` asks about, so an N-branch class costs one APK load and one
    smali-dispatch analysis, not N of each."""

    class_descriptor: str
    dex_file: str
    interface_name: str
    discriminator_field: str
    method_name: str
    smali_path: Path
    method: MethodBody
    dispatch: DispatchInfo


def analyze_class(loaded: LoadedApk, apk_root: Path, class_name: str) -> tuple[ClassAnalysis | None, str | None]:
    """Returns (analysis, error_message) — exactly one is None."""
    found = androguard_index.resolve_class(loaded, class_name)
    if found is None:
        return None, (
            f"class '{class_name}' not found in {apk_root}, or it does not match "
            f"SPEC.md §3's structural shape check"
        )
    cls, dex_file, interface_name, discriminator_field = found
    class_descriptor = cls.get_name()
    dex_position = loaded.class_to_dex_position[class_descriptor]
    method_name = _interface_method_name(loaded, interface_name)

    try:
        smali_path = smali_reader.locate_smali_file(apk_root, dex_position, class_descriptor)
        method = smali_reader.find_method(smali_path, method_name)
        dispatch = smali_reader.analyze_dispatch(method, class_descriptor, discriminator_field)
    except (smali_reader.SmaliShapeError, FileNotFoundError) as exc:
        return None, str(exc)

    return (
        ClassAnalysis(
            class_descriptor=class_descriptor,
            dex_file=dex_file,
            interface_name=interface_name,
            discriminator_field=discriminator_field,
            method_name=method_name,
            smali_path=smali_path,
            method=method,
            dispatch=dispatch,
        ),
        None,
    )


def build_result(apk_root: Path, analysis: ClassAnalysis, discriminator_value: int) -> ResolveResult:
    smali_evidence, status = smali_reader.resolve_branch(
        analysis.method, analysis.dispatch, discriminator_value, analysis.smali_path
    )
    jadx_evidence: JadxEvidence = jadx_correlate.correlate(
        apk_root, analysis.class_descriptor, analysis.method_name, analysis.dispatch.mechanism, discriminator_value
    )
    return ResolveResult(
        class_name=analysis.class_descriptor,
        dex_file=analysis.dex_file,
        interface_implemented=analysis.interface_name,
        discriminator_field=analysis.discriminator_field,
        discriminator_value=discriminator_value,
        dispatch_mechanism=analysis.dispatch.mechanism,
        case_count=len(analysis.dispatch.branches),
        smali=smali_evidence,
        jadx=jadx_evidence,
        resolution_status=status,
    )


def _error_result(class_name: str, discriminator_value: int, message: str) -> ResolveResult:
    return ResolveResult(
        class_name=class_name,
        dex_file=None,
        interface_implemented=None,
        discriminator_field=None,
        discriminator_value=discriminator_value,
        dispatch_mechanism=None,
        case_count=None,
        smali=None,
        jadx=None,
        resolution_status="error" if "not found" in message or "shape check" in message else "ambiguous",
        error=message,
    )


def cmd_resolve(apk_root: Path, class_name: str, discriminator_value: int) -> ResolveResult:
    loaded = _load(apk_root)
    analysis, error = analyze_class(loaded, apk_root, class_name)
    if analysis is None:
        return _error_result(class_name, discriminator_value, error)
    return build_result(apk_root, analysis, discriminator_value)


def cmd_resolve_all(apk_root: Path, class_name: str) -> list[ResolveResult]:
    """Resolves every explicitly-defined case for `class_name`, plus exactly
    one extra entry for the default/"no case matched" branch — not an
    arbitrary guessed range of discriminator values (SPEC.md §7's own
    "never guess" rule, applied here too): the values actually resolved are
    read straight from the dispatch's own branch table, which is already the
    complete, authoritative set (a contiguous 0..N-1 range for
    packed-switch/sparse-switch, or the exact set of checked values for an
    if-chain)."""
    loaded = _load(apk_root)
    analysis, error = analyze_class(loaded, apk_root, class_name)
    if analysis is None:
        return [_error_result(class_name, 0, error)]

    values = sorted(analysis.dispatch.branches.keys())
    results = [build_result(apk_root, analysis, v) for v in values]

    default_sentinel = -1
    while default_sentinel in analysis.dispatch.branches:
        default_sentinel -= 1
    results.append(build_result(apk_root, analysis, default_sentinel))

    return results


def _interface_method_name(loaded: LoadedApk, interface_name: str) -> str:
    iface_cls = loaded.class_by_name.get(interface_name)
    if iface_cls is not None:
        for m in iface_cls.get_methods():
            if "abstract" in m.get_access_flags_string():
                return m.get_name()
    # Real java.util.function.* interfaces aren't in this APK's own dex set
    # (androguard_index._interface_abstract_method_count already returns
    # None for those and treats the shape check as satisfied) — fall back to
    # the well-known method name for the ones this project has actually hit.
    known = {
        "Ljava/util/function/BiFunction;": "apply",
        "Ljava/util/function/Function;": "apply",
        "Ljava/util/function/Supplier;": "get",
        "Ljava/util/function/Consumer;": "accept",
        "Ljava/util/concurrent/Callable;": "call",
        "Ljava/lang/Runnable;": "run",
    }
    if interface_name in known:
        return known[interface_name]
    raise smali_reader.SmaliShapeError(
        f"interface {interface_name} not found in this APK's own dex set, and it isn't one of "
        f"the well-known java.util.function.*/Runnable/Callable interfaces this tool "
        f"recognizes by name — cannot determine the dispatch method's name"
    )


def _write_result(result: ResolveResult, output_dir: Path | None, label: str) -> None:
    if output_dir is None:
        json.dump(result.to_json_dict(), sys.stdout, indent=2, default=str)
        print()
        return
    output_dir.mkdir(parents=True, exist_ok=True)
    path = output_dir / f"{label}.json"
    path.write_text(json.dumps(result.to_json_dict(), indent=2, default=str), encoding="utf-8")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(prog="lambda-resolver")
    sub = parser.add_subparsers(dest="command", required=True)

    p_list = sub.add_parser("list", help="list every candidate lambda-dispatcher class in the APK")
    p_list.add_argument("--apk-root", required=True, type=Path)
    p_list.add_argument(
        "--interface-hint",
        default=None,
        help="filter to candidates implementing this interface only (e.g. 'pkk' or 'Lpkk;') "
        "— does not change detection itself (SPEC.md §3), only which results are printed",
    )

    p_resolve = sub.add_parser("resolve", help="resolve one (class, discriminator) pair")
    p_resolve.add_argument("--apk-root", required=True, type=Path)
    p_resolve.add_argument("--class", dest="class_name", required=True)
    p_resolve.add_argument("--discriminator", required=True, type=int)

    p_resolve_all = sub.add_parser(
        "resolve-all",
        help="resolve every defined case of one class, plus its default branch, in one pass",
    )
    p_resolve_all.add_argument("--apk-root", required=True, type=Path)
    p_resolve_all.add_argument("--class", dest="class_name", required=True)
    p_resolve_all.add_argument(
        "--output-dir",
        type=Path,
        default=None,
        help="write one <class>_<value>.json (and <class>_default.json) file per case here, "
        "instead of printing a single JSON array to stdout",
    )

    args = parser.parse_args(argv)

    if args.command == "list":
        candidates = cmd_list(args.apk_root, args.interface_hint)
        json.dump([c.__dict__ for c in candidates], sys.stdout, indent=2)
        print()
        return 0

    if args.command == "resolve":
        result = cmd_resolve(args.apk_root, args.class_name, args.discriminator)
        json.dump(result.to_json_dict(), sys.stdout, indent=2, default=str)
        print()
        return 0 if result.resolution_status != "error" else 1

    if args.command == "resolve-all":
        results = cmd_resolve_all(args.apk_root, args.class_name)
        had_error = any(r.resolution_status == "error" for r in results)

        if args.output_dir is None:
            json.dump([r.to_json_dict() for r in results], sys.stdout, indent=2, default=str)
            print()
        else:
            short_name = args.class_name.strip("L;").rsplit(".", 1)[-1].rsplit("/", 1)[-1]
            for r in results:
                label = (
                    f"{short_name}_default"
                    if r.resolution_status == "resolved-default-branch"
                    else f"{short_name}_{r.discriminator_value}"
                )
                _write_result(r, args.output_dir, label)
            print(f"wrote {len(results)} file(s) to {args.output_dir}", file=sys.stderr)

        return 1 if had_error else 0

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
