"""SPEC.md §3/§4 — the extraction sweep, the BT-API textual co-occurrence tag, and
the `structural_index`-reused usage-location graph.

`structural_index.xref_index.find_refs`/`load_apk` are imported directly via a
sys.path insertion to the sibling tool's `src/` directory (SPEC.md §4's own
"reuse, stated precisely" note) -- not re-implemented. `structural_index` itself
already imports `lambda_dispatcher_resolver`'s Layer 1 the same way, one hop
further down.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

# Repo layout: reverse-engineering/tools/uuid_ble_context/src/uuid_ble_context/uuid_scan.py
# parents[0]=uuid_ble_context(pkg) [1]=src [2]=uuid_ble_context(tool dir) [3]=tools
_TOOLS_DIR = Path(__file__).resolve().parents[3]
_STRUCTURAL_INDEX_SRC = _TOOLS_DIR / "structural_index" / "src"
if str(_STRUCTURAL_INDEX_SRC) not in sys.path:
    sys.path.insert(0, str(_STRUCTURAL_INDEX_SRC))

from structural_index import xref_index as _sx  # noqa: E402

from .models import ContextResult, ExtractResult, Occurrence, UuidEntry  # noqa: E402

__all__ = ["extract", "context"]

_UUID_RE = re.compile(
    r"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
)

# SPEC.md §3's fixed BLE/GATT/RFCOMM API-name list -- a plain textual
# co-occurrence signal, not a call-graph trace.
_BT_API_NAMES = (
    "BluetoothGatt",
    "BluetoothGattCallback",
    "BluetoothGattCharacteristic",
    "BluetoothGattService",
    "BluetoothSocket",
    "BluetoothDevice",
    "BluetoothAdapter",
    "BluetoothManager",
    "BluetoothHeadset",
    "BluetoothServerSocket",
    "fetchUuidsWithSdp",
    "createRfcommSocketToServiceRecord",
    "createInsecureRfcommSocketToServiceRecord",
    "UUID.fromString",
)


def _class_name_from_path(java_file: Path, sources_root: Path) -> str:
    """Same convention as schema_batch_extractor's own helper -- duplicated
    locally per structural_index's own precedent (SPEC.md §4's cross-reference):
    an 8-line pure function is not worth a cross-tool dependency."""
    rel = java_file.relative_to(sources_root)
    return ".".join(rel.with_suffix("").parts)


def _has_bt_api_cooccurrence(text: str) -> bool:
    return any(name in text for name in _BT_API_NAMES)


def _scan_tree(apk_root: Path) -> tuple[dict[str, list[Occurrence]], int]:
    """One pass over the whole jadx-output/sources/ tree. Returns
    (uuid_lower -> occurrences, total_java_files_scanned)."""
    sources_root = apk_root / "jadx-output" / "sources"
    if not sources_root.is_dir():
        raise FileNotFoundError(f"no jadx-output/sources/ found under {apk_root}")

    java_files = list(sources_root.rglob("*.java"))
    by_uuid: dict[str, list[Occurrence]] = {}

    for jf in java_files:
        try:
            text = jf.read_text(encoding="utf-8", errors="replace")
        except OSError as e:  # pragma: no cover -- SPEC.md §7's "skip, don't abort" rule
            print(f"uuid-ble-context: warning: could not read {jf}: {e}", file=sys.stderr)
            continue

        matches = list(_UUID_RE.finditer(text))
        if not matches:
            continue

        bt_cooccurs = _has_bt_api_cooccurrence(text)
        cls_name = _class_name_from_path(jf, sources_root)
        # Line number for each match, computed once per file.
        for m in matches:
            line_no = text.count("\n", 0, m.start()) + 1
            uuid_lower = m.group(0).lower()
            by_uuid.setdefault(uuid_lower, []).append(
                Occurrence(file=str(jf), line=line_no, cls=cls_name, bt_api_cooccurrence=bt_cooccurs)
            )

    return by_uuid, len(java_files)


def extract(apk_root: Path) -> ExtractResult:
    """SPEC.md §5's `extract` command: Pass 1 + Pass 2 only (never Pass 3)."""
    by_uuid, total_files = _scan_tree(apk_root)
    entries = [UuidEntry(uuid=u, occurrences=occs) for u, occs in sorted(by_uuid.items())]
    return ExtractResult(
        apk_root=str(apk_root),
        total_java_files_scanned=total_files,
        total_uuids_found=len(entries),
        uuids=entries,
    )


def context(apk_root: Path, uuid: str) -> ContextResult:
    """SPEC.md §5's `context` command: the full 3-pass pipeline for one UUID.
    Raises KeyError if the UUID was never found by the Pass-1 sweep at all
    (SPEC.md §7)."""
    by_uuid, _total_files = _scan_tree(apk_root)
    uuid_lower = uuid.strip().lower()
    if uuid_lower not in by_uuid:
        raise KeyError(uuid_lower)

    occurrences = by_uuid[uuid_lower]
    loaded = _sx.load_apk(apk_root)

    usage_by_class: dict = {}
    seen_classes: set[str] = set()
    for occ in occurrences:
        if occ.cls in seen_classes:
            continue
        seen_classes.add(occ.cls)
        try:
            refs = _sx.find_refs(loaded, occ.cls)
        except KeyError:
            # SPEC.md §7: the owning class couldn't be resolved by structural_index
            # (e.g. androguard's own class-descriptor form didn't match) -- omit
            # with a note, never drop the occurrence record itself.
            usage_by_class[occ.cls] = {"note": "structural_index could not resolve this class"}
            continue
        usage_by_class[occ.cls] = refs.to_dict()

    return ContextResult(uuid=uuid_lower, occurrences=occurrences, usage_by_class=usage_by_class)
