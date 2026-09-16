"""Layer 1 — DEX-level structure via androguard.

Deliberately avoids androguard's `Analysis`/XREF pass (`androguard.misc.AnalyzeAPK`):
that pass builds cross-references for the *entire* APK and costs ~35s on this
APK, none of which this tool needs — detection (SPEC.md §3) only requires
per-class structural facts (interfaces, fields, methods), which are available
directly from each `DEX` object in well under a second. If a future tool
built on top of this one needs XREFs, load them there, separately.
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from loguru import logger as _loguru_logger

_loguru_logger.remove()  # silence androguard's default DEBUG/INFO spam on stderr

from androguard.core.apk import APK  # noqa: E402
from androguard.core.dex import DEX, EncodedField  # noqa: E402

from .models import DispatcherCandidate

# Maps a dex position in the APK's own declared file order (classes.dex,
# classes2.dex, classes3.dex, ...) to the apktool smali directory it was
# decompiled into. classes.dex (position 0) is the one exception: apktool
# names it "smali", not "smali_classes1".
def dex_position_to_smali_dir(position: int) -> str:
    return "smali" if position == 0 else f"smali_classes{position + 1}"


@dataclass
class LoadedApk:
    apk_root: Path
    dex_names: list[str]  # e.g. ["classes.dex", "classes2.dex", "classes3.dex"], APK's own order
    dexes: list[DEX]
    # class descriptor (e.g. "Laie;") -> dex position index into `dexes`/`dex_names`
    class_to_dex_position: dict[str, int]
    # class descriptor -> the androguard ClassDefItem, for cross-dex interface lookups
    class_by_name: dict[str, object]


def load_apk(apk_root: Path) -> LoadedApk:
    """Load `base.apk` under `apk_root` and index every class across all its dex files.

    `apk_root` is a version directory such as
    `reverse-engineering/apk/v1.0.955078536-10253511/` — the same one already
    used for `jadx-output/`/`apktool-output/`.
    """
    apk_path = apk_root / "base.apk"
    if not apk_path.is_file():
        raise FileNotFoundError(f"no base.apk found under {apk_root}")

    apk = APK(str(apk_path))
    dex_names = [n for n in apk.get_files() if n == "classes.dex" or (n.startswith("classes") and n.endswith(".dex"))]
    # APK.get_files() already returns zip-entry order, which is classes.dex,
    # classes2.dex, classes3.dex, ... in every APK this tool has been run
    # against — sort defensively by the numeric suffix so a differently-ordered
    # zip can't silently mismap a class to the wrong smali directory.
    def _dex_sort_key(name: str) -> int:
        digits = name[len("classes"):-len(".dex")]
        return int(digits) if digits else 0

    dex_names = sorted(dex_names, key=_dex_sort_key)
    dexes = [DEX(apk.get_file(name)) for name in dex_names]

    class_to_dex_position: dict[str, int] = {}
    class_by_name: dict[str, object] = {}
    for position, dex in enumerate(dexes):
        for cls in dex.get_classes():
            name = cls.get_name()
            class_to_dex_position[name] = position
            class_by_name[name] = cls

    return LoadedApk(
        apk_root=apk_root,
        dex_names=dex_names,
        dexes=dexes,
        class_to_dex_position=class_to_dex_position,
        class_by_name=class_by_name,
    )


def _is_synthetic_int_field(f: EncodedField) -> bool:
    return f.get_descriptor() == "I" and "synthetic" in f.get_access_flags_string()


def _interface_abstract_method_count(loaded: LoadedApk, interface_name: str) -> int | None:
    """Returns None if the interface itself isn't in this APK's own dex set
    (e.g. a real java.util.function.* interface — those are fine, just not
    independently checkable here; treated as satisfying the check by default,
    per SPEC.md §3 not requiring the interface's own definition to be present).
    """
    iface_cls = loaded.class_by_name.get(interface_name)
    if iface_cls is None:
        return None
    count = 0
    for m in iface_cls.get_methods():
        flags = m.get_access_flags_string()
        if "abstract" in flags:
            count += 1
    return count


def class_shape_matches(loaded: LoadedApk, cls) -> tuple[bool, str | None, str | None]:
    """SPEC.md §3's structural check, points 1-4 (point 5, constructor shape,
    is checked separately in the smali layer since it needs no androguard
    data beyond what's already confirmed here).

    Returns (matches, interface_name_or_None, discriminator_field_name_or_None).
    """
    flags = cls.get_access_flags_string()
    if "synthetic" not in flags or "final" not in flags:
        return False, None, None

    interfaces = cls.get_interfaces()
    if len(interfaces) != 1:
        return False, None, None
    interface_name = interfaces[0]

    abstract_count = _interface_abstract_method_count(loaded, interface_name)
    if abstract_count is not None and abstract_count != 1:
        return False, None, None

    int_fields = [f for f in cls.get_fields() if _is_synthetic_int_field(f)]
    if len(int_fields) != 1:
        return False, None, None
    discriminator_field = int_fields[0].get_name()

    return True, interface_name, discriminator_field


def find_candidates(loaded: LoadedApk) -> list[DispatcherCandidate]:
    """`list` command: SPEC.md §3's shape check, applied to every class in the APK."""
    candidates: list[DispatcherCandidate] = []
    for position, dex in enumerate(loaded.dexes):
        dex_name = loaded.dex_names[position]
        for cls in dex.get_classes():
            matches, interface_name, discriminator_field = class_shape_matches(loaded, cls)
            if matches:
                candidates.append(
                    DispatcherCandidate(
                        class_name=cls.get_name(),
                        dex_file=dex_name,
                        interface_implemented=interface_name,
                        discriminator_field=discriminator_field,
                    )
                )
    return candidates


def resolve_class(loaded: LoadedApk, class_name: str) -> tuple[object, str, str, str] | None:
    """Look up one class by name (accepts both "Laie;" and "aie"/"defpackage.aie"
    spellings) and return (cls, dex_file_name, interface_name, discriminator_field),
    or None if not found or it doesn't match SPEC.md §3's shape.
    """
    descriptor = _normalize_class_name(class_name)
    cls = loaded.class_by_name.get(descriptor)
    if cls is None:
        return None
    matches, interface_name, discriminator_field = class_shape_matches(loaded, cls)
    if not matches:
        return None
    position = loaded.class_to_dex_position[descriptor]
    return cls, loaded.dex_names[position], interface_name, discriminator_field


def _normalize_class_name(name: str) -> str:
    """Accepts "aie", "defpackage.aie", "Laie;" and returns the smali descriptor "Laie;".
    "defpackage." is stripped since it is JADX's own default-package folder
    name, not part of the actual class descriptor (SPEC.md's `list`/`resolve`
    commands accept the short form researchers actually type).
    """
    n = name.strip()
    if n.startswith("L") and n.endswith(";"):
        return n
    if n.startswith("defpackage."):
        n = n[len("defpackage."):]
    n = n.replace(".", "/")
    return f"L{n};"
