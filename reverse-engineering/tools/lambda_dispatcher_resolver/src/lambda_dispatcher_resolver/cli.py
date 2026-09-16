"""CLI — `lambda-resolver list` / `lambda-resolver resolve` (SPEC.md §5).

No file under `reverse-engineering/apk/` is ever written to (SPEC.md §8) —
this module only reads and prints JSON to stdout.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from . import androguard_index, jadx_correlate, smali_reader
from .androguard_index import LoadedApk
from .models import DispatcherCandidate, JadxEvidence, ResolveResult


def _load(apk_root: Path) -> LoadedApk:
    return androguard_index.load_apk(apk_root)


def cmd_list(apk_root: Path) -> list[DispatcherCandidate]:
    loaded = _load(apk_root)
    return androguard_index.find_candidates(loaded)


def cmd_resolve(apk_root: Path, class_name: str, discriminator_value: int) -> ResolveResult:
    loaded = _load(apk_root)
    found = androguard_index.resolve_class(loaded, class_name)
    if found is None:
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
            resolution_status="error",
            error=(
                f"class '{class_name}' not found in {apk_root}, or it does not match "
                f"SPEC.md §3's structural shape check"
            ),
        )
    cls, dex_file, interface_name, discriminator_field = found
    class_descriptor = cls.get_name()
    dex_position = loaded.class_to_dex_position[class_descriptor]

    # SPEC.md §3.2: the class's own sole non-constructor method is the
    # interface's abstract method — same name as declared on the interface.
    method_name = _interface_method_name(loaded, interface_name)

    try:
        smali_path = smali_reader.locate_smali_file(apk_root, dex_position, class_descriptor)
        method = smali_reader.find_method(smali_path, method_name)
        dispatch = smali_reader.analyze_dispatch(method, class_descriptor, discriminator_field)
        smali_evidence, status = smali_reader.resolve_branch(method, dispatch, discriminator_value, smali_path)
    except (smali_reader.SmaliShapeError, FileNotFoundError) as exc:
        return ResolveResult(
            class_name=class_descriptor,
            dex_file=dex_file,
            interface_implemented=interface_name,
            discriminator_field=discriminator_field,
            discriminator_value=discriminator_value,
            dispatch_mechanism=None,
            case_count=None,
            smali=None,
            jadx=None,
            resolution_status="ambiguous",
            error=str(exc),
        )

    jadx_evidence: JadxEvidence = jadx_correlate.correlate(
        apk_root, class_descriptor, method_name, dispatch.mechanism, discriminator_value
    )

    return ResolveResult(
        class_name=class_descriptor,
        dex_file=dex_file,
        interface_implemented=interface_name,
        discriminator_field=discriminator_field,
        discriminator_value=discriminator_value,
        dispatch_mechanism=dispatch.mechanism,
        case_count=len(dispatch.branches),
        smali=smali_evidence,
        jadx=jadx_evidence,
        resolution_status=status,
    )


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


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(prog="lambda-resolver")
    sub = parser.add_subparsers(dest="command", required=True)

    p_list = sub.add_parser("list", help="list every candidate lambda-dispatcher class in the APK")
    p_list.add_argument("--apk-root", required=True, type=Path)

    p_resolve = sub.add_parser("resolve", help="resolve one (class, discriminator) pair")
    p_resolve.add_argument("--apk-root", required=True, type=Path)
    p_resolve.add_argument("--class", dest="class_name", required=True)
    p_resolve.add_argument("--discriminator", required=True, type=int)

    args = parser.parse_args(argv)

    if args.command == "list":
        candidates = cmd_list(args.apk_root)
        json.dump([c.__dict__ for c in candidates], sys.stdout, indent=2)
        print()
        return 0

    if args.command == "resolve":
        result = cmd_resolve(args.apk_root, args.class_name, args.discriminator)
        json.dump(result.to_json_dict(), sys.stdout, indent=2, default=str)
        print()
        return 0 if result.resolution_status != "error" else 1

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
