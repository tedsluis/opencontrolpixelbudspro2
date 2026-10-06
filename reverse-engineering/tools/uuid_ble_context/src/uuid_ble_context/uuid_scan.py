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

from .models import ContextResult, ExtractResult, FindResult, Occurrence, UuidEntry  # noqa: E402

__all__ = ["extract", "context", "find", "byte_reversed"]

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


def byte_reversed(uuid: str) -> str:
    """The same 128 bits with the 16 bytes in reverse order, as a lower-case UUID string
    ("25e97ff7-24ce-4c4c-8951-f764a708f7b5" <-> "b5f708a7-64f7-5189-4c4c-ce24f77fe925")."""
    h = uuid.replace("-", "").lower()
    if len(h) != 32 or any(ch not in "0123456789abcdef" for ch in h):
        raise ValueError(f"not a 128-bit UUID: {uuid!r}")
    r = bytes.fromhex(h)[::-1].hex()
    return f"{r[0:8]}-{r[8:12]}-{r[12:16]}-{r[16:20]}-{r[20:32]}"


# Every place text can hold a UUID (SPEC.md §5a): decompiled sources, smali, resources, assets, manifests.
_FIND_ROOTS = ("jadx-output/sources", "jadx-output/resources", "apktool-output", "apktool-output-arm64_v8a")
_FIND_SKIP_SUFFIXES = (".png", ".webp", ".jpg", ".so", ".dex", ".ttf", ".otf", ".ogg", ".mp4", ".arsc", ".bin")


def find(apk_root: Path, uuid: str) -> FindResult:
    """SPEC.md §5a: a case-insensitive text search for `uuid` and its byte-reversed form, each with
    and without dashes, over every text file of the four tree roots. Never raises on "not found":
    zero hits with the per-root file counts IS the checked negative."""
    apk_root = Path(apk_root)
    canon = uuid.strip().lower()
    rev = byte_reversed(canon)
    forms = [canon, rev, canon.replace("-", ""), rev.replace("-", "")]
    result = FindResult(uuid=canon, byte_reversed=rev, forms_searched=forms)
    if not (apk_root / "jadx-output" / "sources").is_dir():
        raise FileNotFoundError(f"no jadx-output/sources/ found under {apk_root}")
    for root in _FIND_ROOTS:
        base = apk_root / root
        if not base.is_dir():
            continue
        n = 0
        for f in base.rglob("*"):
            if not f.is_file() or f.suffix.lower() in _FIND_SKIP_SUFFIXES:
                continue
            try:
                text = f.read_text(encoding="utf-8", errors="replace")
            except OSError:  # pragma: no cover
                continue
            n += 1
            low = None
            for form in forms:
                if form[:8] not in text and form[:8] not in (low := low if low is not None else text.lower()):
                    continue
                low = low if low is not None else text.lower()
                pos = low.find(form)
                while pos >= 0:
                    result.hits.append({"form": form, "file": str(f.relative_to(apk_root)), "line": low.count("\n", 0, pos) + 1})
                    pos = low.find(form, pos + 1)
        result.files_scanned[root] = n
    return result


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
    entries = [
        UuidEntry(uuid=u, occurrences=occs, byte_reversed=byte_reversed(u), byte_reversed_in_tree=byte_reversed(u) in by_uuid)
        for u, occs in sorted(by_uuid.items())
    ]
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
