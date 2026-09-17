"""SPEC.md §10 acceptance criteria: reproduces the already-known `qhr`/`qjc`/`qjb`/`nqx`
schemas exactly (same field count, same field-type/reference info) before this tool is
trusted on anything new, plus the whole-tree count check and the disclosed-limitation
regression (SPEC.md §3).

**Deliberately runs against the maintainer's own locally-decompiled APK tree under
`reverse-engineering/apk/<version>/`, never against a copy committed into this tool's
own `tests/` directory** — the same discipline `lambda_dispatcher_resolver/tests/test_resolver.py`
and `structural_index/tests/test_xref_index.py` already established
(`PROJECT_RULES.md` §8 rule 20; `../BACKLOG.md`'s own governance section). The whole module
is skipped (not failed) when the decompiled tree isn't present.
"""

from __future__ import annotations

from pathlib import Path

import pytest

from schema_batch_extractor import batch_extract, cli

APK_ROOT = Path(__file__).resolve().parents[3] / "apk" / "v1.0.955078536-10253511"

pytestmark = pytest.mark.skipif(
    not (APK_ROOT / "base.apk").is_file(),
    reason=(
        f"no decompiled APK found at {APK_ROOT} — run APK_REVERSE_ENGINEERING_PROCEDURE.md's "
        f"pull+decompile step first; this suite intentionally never ships its own copy of "
        f"decompiled code (PROJECT_RULES.md §8 rule 20)"
    ),
)


@pytest.fixture(scope="module")
def full_scan():
    return batch_extract.scan(APK_ROOT)


def _get(result, short_name: str):
    for c in result.classes:
        if c.cls.rsplit(".", 1)[-1] == short_name:
            return c
    raise AssertionError(f"{short_name} not found in scan result")


class TestKnownSchemas:
    def test_qhr_38_fields(self, full_scan):
        """Acceptance criterion 1 (SPEC.md §10): `qhr` — 38 fields, 1 oneof,
        field-number range [1, 38], and the 7 message_refs REVERSE_ENGINEERING.md's
        own `qhr` entry names (qju once at field 7, qht at 12, qjw twice at 16/18
        collapsing to one dedup'd entry, qhq at 23, qiq at 31, qjf at 35, qis at 37)."""
        qhr = _get(full_scan, "qhr")
        assert qhr.field_count == 38
        assert qhr.oneof_count == 1
        assert qhr.min_field_number == 1
        assert qhr.max_field_number == 38
        assert qhr.message_refs == ["qju", "qht", "qjw", "qhq", "qiq", "qjf", "qis"]
        # Byte-for-byte spot checks against REVERSE_ENGINEERING.md's own qhr field register.
        by_num = {f.field_number: f for f in qhr.fields}
        assert by_num[4].type_name == "BOOL"
        assert by_num[7].type_name == "MESSAGE" and by_num[7].message_ref == "qju"
        assert by_num[12].type_name == "MESSAGE" and by_num[12].message_ref == "qht"
        assert by_num[16].message_ref == "qjw"
        assert by_num[18].message_ref == "qjw"
        assert by_num[29].type_name == "ENUM"

    def test_qjc_and_qja_5_fields_same_shape(self, full_scan):
        """Acceptance criterion 2 (SPEC.md §10): `qjc`/`qja` both decode to
        field_count=5, oneof_count=1, message_refs in field-number order
        [qhx, qjn, qjt, qhr, qjv]."""
        for short in ("qjc", "qja"):
            entry = _get(full_scan, short)
            assert entry.field_count == 5
            assert entry.oneof_count == 1
            assert entry.message_refs == ["qhx", "qjn", "qjt", "qhr", "qjv"]

    def test_nqx_7_fields_no_message_refs(self, full_scan):
        """Acceptance criterion 3 (SPEC.md §10): `nqx` (RpcPacket) — 7 fields,
        0 oneofs, no message-typed fields at all (ENUM/UINT32/FIXED32x2/BYTES/
        UINT32x2), so its own message_refs list is empty and nothing else in
        the register references it via oneof/list/map (it's populated by
        hand-written code, not nested inside another RawMessageInfo schema)."""
        nqx = _get(full_scan, "nqx")
        assert nqx.field_count == 7
        assert nqx.oneof_count == 0
        assert nqx.message_refs == []
        type_names = [f.type_name for f in sorted(nqx.fields, key=lambda f: f.field_number)]
        assert type_names == ["ENUM", "UINT32", "FIXED32", "FIXED32", "BYTES", "UINT32", "UINT32"]

        refs = batch_extract.find_refs(APK_ROOT, "nqx")
        assert refs.referenced_by == []

    def test_qjb_4_fields_sparse_range(self, full_scan):
        """Acceptance criterion 4 (SPEC.md §10): `qjb` — 4 fields, 1 oneof,
        sparse field-number range [3, 6] (not starting at 1, unlike the other
        three known schemas)."""
        qjb = _get(full_scan, "qjb")
        assert qjb.field_count == 4
        assert qjb.oneof_count == 1
        assert qjb.min_field_number == 3
        assert qjb.max_field_number == 6
        by_num = {f.field_number: f for f in qjb.fields}
        assert by_num[3].message_ref == "qjj"
        assert by_num[4].message_ref == "qie"
        assert by_num[5].type_name == "FIXED64"
        assert by_num[6].type_name == "BOOL"


class TestWholeTreeCount:
    def test_total_candidates_matches_documented_807(self, full_scan):
        """Acceptance criterion 5 (SPEC.md §10): reproduces
        REVERSE_ENGINEERING.md's own "Candidate rich schemas" section's
        independently-obtained 807-construction count, with zero unparsable
        constructions, from this tool's real full-tree scan."""
        assert full_scan.total_candidates == 807
        assert full_scan.total_decoded == 807
        assert full_scan.unparsable == []
        assert len(full_scan.classes) == 807


class TestDisclosedLimitation:
    def test_ndi_has_no_incoming_schema_refs_despite_being_a_plain_field_of_nef(self, full_scan):
        """Acceptance criterion 6 (SPEC.md §10 / §3): `nef` genuinely holds `ndi`
        as a plain (non-oneof) MESSAGE-typed field (`nef.v`, confirmed
        independently via `structural_index refs --class ndi` in
        `ai-sessions/0025`'s first pass) -- but the compact RawMessageInfo
        schema string carries no class reference for that field shape at all,
        so this tool's own `refs` correctly returns empty. This is the
        documented, expected behavior (SPEC.md §3), not a bug -- this test
        protects it from accidentally regressing into a false-positive claim
        this data source cannot actually support."""
        ndi = _get(full_scan, "ndi")
        assert ndi.field_count == 35
        refs = batch_extract.find_refs(APK_ROOT, "ndi")
        assert refs.referenced_by == []

    def test_nef_itself_has_zero_oneofs_so_none_of_its_message_fields_are_resolvable(self, full_scan):
        nef = _get(full_scan, "nef")
        assert nef.oneof_count == 0
        assert nef.message_refs == []


class TestErrorHandling:
    def test_unknown_class_in_scan_filter_raises_keyerror(self):
        with pytest.raises(KeyError):
            batch_extract.scan(APK_ROOT, class_filter=["ThisClassDoesNotExistAnywhereInThisApk"])

    def test_unknown_class_in_refs_raises_keyerror(self):
        with pytest.raises(KeyError):
            batch_extract.find_refs(APK_ROOT, "ThisClassDoesNotExistAnywhereInThisApk")


class TestCli:
    def test_cli_scan_outputs_expected_json_shape(self, capsys):
        exit_code = cli.main(["scan", "--apk-root", str(APK_ROOT), "--class", "qhr"])
        assert exit_code == 0
        captured = capsys.readouterr()
        assert '"class": "defpackage.qhr"' in captured.out
        assert '"field_count": 38' in captured.out

    def test_cli_refs_outputs_expected_json_shape(self, capsys):
        exit_code = cli.main(["refs", "--apk-root", str(APK_ROOT), "--class", "qju"])
        assert exit_code == 0
        captured = capsys.readouterr()
        assert '"class": "defpackage.qju"' in captured.out
        assert '"defpackage.qhr"' in captured.out

    def test_cli_unknown_class_is_non_zero_exit(self, capsys):
        exit_code = cli.main(
            ["scan", "--apk-root", str(APK_ROOT), "--class", "ThisClassDoesNotExistAnywhereInThisApk"]
        )
        assert exit_code == 1
