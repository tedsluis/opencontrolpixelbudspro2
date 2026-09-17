"""SPEC.md §10 acceptance criteria: reproduces REVERSE_ENGINEERING.md's own UUID
register (5 literal forms for 3 registered UUIDs), confirms the whole-tree count
(6, including one genuinely new, non-Bluetooth find), and checks the
structural_index-reused usage graph and the BT-API co-occurrence signal both
discriminate correctly.

**Deliberately runs against the maintainer's own locally-decompiled APK tree
under `reverse-engineering/apk/<version>/`, never against a copy committed into
this tool's own `tests/` directory** — the same discipline the other three
tools already established (`PROJECT_RULES.md` §8 rule 20). The whole module is
skipped (not failed) when the decompiled tree isn't present.
"""

from __future__ import annotations

from pathlib import Path

import pytest

from uuid_ble_context import cli, uuid_scan

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
def extracted():
    return uuid_scan.extract(APK_ROOT)


class TestExtract:
    def test_reproduces_the_5_known_register_literal_forms(self, extracted):
        """Acceptance criterion 1 (SPEC.md §10)."""
        found = {u.uuid for u in extracted.uuids}
        for expected in (
            "25e97ff7-24ce-4c4c-8951-f764a708f7b5",
            "099775cb-7e0d-3465-5576-d2246d6f043a",
            "3a046f6d-24d2-7655-6534-0d7ecb759709",
            "b5f708a7-64f7-5189-4c4c-ce24f77fe925",
            "00001124-0000-1000-8000-00805f9b34fb",
        ):
            assert expected in found

    def test_total_uuids_found_is_6(self, extracted):
        """Acceptance criterion 2 (SPEC.md §10)."""
        assert extracted.total_uuids_found == 6
        assert len(extracted.uuids) == 6

    def test_cooccurrence_signal_discriminates_fqg_vs_fzd_and_gbm(self, extracted):
        """Acceptance criterion 5 (SPEC.md §10): the co-occurrence signal
        actually discriminates, verified against the real tree rather than
        assumed. `fzd.java` and `gbm.java` both show `True` (both import
        `android.bluetooth.BluetoothDevice` and/or call `UUID.fromString`
        directly) -- but `fqg.java` (the already-known `REVERSE_ENGINEERING.md`
        `gbb`/`gbc` construction-wiring class, which merely passes the UUID
        constant through without using any of SPEC.md §3's literal API names
        anywhere in its own file text) shows `False`. This is itself a real,
        useful finding about the heuristic's own precision limits (a
        genuinely Bluetooth-adjacent file can still show `False` if it never
        names a BT API directly), not a failure of the signal -- disclosed as
        exactly this kind of textual, not semantic, check in SPEC.md §3."""
        entry = next(u for u in extracted.uuids if u.uuid == "25e97ff7-24ce-4c4c-8951-f764a708f7b5")
        by_class: dict[str, list[bool]] = {}
        for o in entry.occurrences:
            by_class.setdefault(o.cls, []).append(o.bt_api_cooccurrence)
        assert all(by_class["defpackage.fzd"])
        assert all(by_class["defpackage.gbm"])
        assert all(v is False for v in by_class["defpackage.fqg"])


class TestContext:
    def test_new_non_bluetooth_uuid_is_a_checked_negative(self):
        """Acceptance criterion 3 (SPEC.md §10): the 6th, genuinely new UUID
        this tool's design process surfaced is a WorkManager-internal sentinel
        string, not Bluetooth-related -- exactly two occurrences (a read-side
        and a write-side use of the same literal sentinel, both in the same
        file), no BT-API co-occurrence for either."""
        result = uuid_scan.context(APK_ROOT, "95ed6082-b8e9-46e8-a73f-ff56f00f5d9d")
        assert len(result.occurrences) == 2
        assert all(o.cls == "defpackage.ehs" for o in result.occurrences)
        assert all(o.bt_api_cooccurrence is False for o in result.occurrences)

    def test_fzd_usage_graph_matches_structural_index_direct_run(self):
        """Acceptance criterion 4 (SPEC.md §10): reproduces
        `structural_index refs --class fzd` exactly, via this tool's own reuse
        path rather than a re-implementation."""
        result = uuid_scan.context(APK_ROOT, "25e97ff7-24ce-4c4c-8951-f764a708f7b5")
        fzd_usage = result.usage_by_class["defpackage.fzd"]
        assert fzd_usage["counts"]["calls"] == 11
        assert fzd_usage["counts"]["field_type_holders"] == 6

    def test_unknown_uuid_raises_keyerror(self):
        with pytest.raises(KeyError):
            uuid_scan.context(APK_ROOT, "00000000-0000-0000-0000-000000000000")


class TestCli:
    def test_cli_extract_outputs_expected_json_shape(self, capsys):
        exit_code = cli.main(["extract", "--apk-root", str(APK_ROOT)])
        assert exit_code == 0
        captured = capsys.readouterr()
        assert '"total_uuids_found": 6' in captured.out

    def test_cli_context_outputs_expected_json_shape(self, capsys):
        exit_code = cli.main(
            ["context", "--apk-root", str(APK_ROOT), "--uuid", "25e97ff7-24ce-4c4c-8951-f764a708f7b5"]
        )
        assert exit_code == 0
        captured = capsys.readouterr()
        assert '"defpackage.fzd"' in captured.out

    def test_cli_unknown_uuid_is_non_zero_exit(self, capsys):
        exit_code = cli.main(
            ["context", "--apk-root", str(APK_ROOT), "--uuid", "00000000-0000-0000-0000-000000000000"]
        )
        assert exit_code == 1
