"""SPEC.md §10 acceptance criteria — the three hand-verified worked examples from
`ai-sessions/0023`/`ai-sessions/0024`, re-run as structured queries instead of raw
`grep`.

**Deliberately runs against the maintainer's own locally-decompiled APK tree under
`reverse-engineering/apk/<version>/`, never against a copy committed into this
tool's own `tests/` directory** — the same discipline
`lambda_dispatcher_resolver/tests/test_resolver.py` already established
(`PROJECT_RULES.md` §8 rule 20; `BACKLOG.md`'s own governance section). The whole
module is skipped (not failed) when the decompiled tree isn't present.
"""

from __future__ import annotations

from pathlib import Path

import pytest

from structural_index import cli, xref_index

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
def loaded():
    return xref_index.load_apk(APK_ROOT)


class TestRefs:
    def test_esk_construction_sites_match_known_call_sites(self, loaded):
        """Acceptance criterion 1 (SPEC.md §10): `esk`'s construction sites must
        include the two already hand-documented call sites this project's own
        REVERSE_ENGINEERING.md `esk` entry cites — `fyv.java:48` (discriminator 19,
        the already-fully-documented `WriteSetting` send) and `gcp.java:51`
        (discriminator 20, the default-branch `device_info` DAO write) — found by
        `ai-sessions/0024`'s own by-hand `grep -rn "new esk("` pass."""
        result = xref_index.find_refs(loaded, "esk")
        callers = {(c.caller_class, c.caller_method) for c in result.constructs}
        assert ("defpackage.fyv", "a") in callers
        assert ("defpackage.gcp", "g") in callers
        # 21 real+adjacent constructor overload call sites total, per
        # REVERSE_ENGINEERING.md's esk entry ("12+ constructor overloads").
        assert len(result.constructs) == 21

    def test_gjv_p_sole_caller_via_abstract_supertype(self, loaded):
        """Acceptance criterion 2 (SPEC.md §10): `giz.p()`'s sole caller —
        `ai-sessions/0023`'s own `Lgiz;->p(` smali-grep finding
        (`REVERSE_ENGINEERING.md`'s `frb`/`fuh`/`glk`/`gjv` entry's 2026-09-15
        update) — reproduced from one structured `--class giz --method p` query,
        including through the abstract-supertype receiver-type indirection that
        made this call site invisible to a `giz`-typed-*field* search across
        three separate manual sessions before the smali-descriptor grep found it."""
        result = xref_index.find_refs(loaded, "giz", method_filter="p")
        assert len(result.calls) == 1
        assert result.calls[0].caller_class == "defpackage.ftw"
        assert result.calls[0].invoke_kind == "invoke-virtual"

    def test_method_filter_does_not_affect_constructs_or_field_type_holders(self, loaded):
        """A `--method` filter narrows (b) only — SPEC.md §5's own contract."""
        unfiltered = xref_index.find_refs(loaded, "giz")
        filtered = xref_index.find_refs(loaded, "giz", method_filter="p")
        assert unfiltered.constructs == filtered.constructs
        assert unfiltered.field_type_holders == filtered.field_type_holders
        assert len(unfiltered.calls) >= len(filtered.calls)

    def test_class_not_found_raises_keyerror(self, loaded):
        with pytest.raises(KeyError):
            xref_index.find_refs(loaded, "ThisClassDoesNotExistAnywhereInThisApk")


class TestUnreferenced:
    def test_aie_non_catalogued_classes_are_all_externally_referenced(self, loaded):
        """Acceptance criterion 3 (SPEC.md §10): the "which of `aie`'s never-
        catalogued referenced classes have zero external references" check,
        `ai-sessions/0024`'s own third named acceptance criterion. `Laly`/`Lcvo`/
        `Lgza` are three of `aie`'s own non-Bluetooth-relevant referenced classes
        (REVERSE_ENGINEERING.md's `qhr`/`fye` entry's `aie` addendum, discriminators
        0/6/16) — all three are generic, widely-reused app-UI plumbing (a
        capability/UI-callback interface, an ExoPlayer/media-session-shaped state
        object, a Slice value-holder), so the correct answer is that none of them
        has zero external references."""
        results = xref_index.find_unreferenced(loaded, ["aly", "cvo", "gza"])
        by_class = {r.cls: r for r in results}
        assert by_class["defpackage.aly"].referenced is True
        assert by_class["defpackage.cvo"].referenced is True
        assert by_class["defpackage.gza"].referenced is True
        assert all(r.total_references > 0 for r in results)


class TestImplements:
    def test_fya_direct_implementers_only_not_transitive(self, loaded):
        """SPEC.md §10a acceptance criterion 4 (v1.1, ai-sessions/0027): `fya` is a
        real interface (`fya.java:5`, `public interface fya`) with 2 *direct*
        implementers (`fxz.java:5`, `fyo.java:7`) — `fyw`/`fyx` only reach it
        transitively via `extends fxz`, matching REVERSE_ENGINEERING.md's own
        hand-derived `grep -rln "implements fya\\|extends fxz" .` finding (4 files
        total) and demonstrating this query's own disclosed direct-only scope on
        real data, not merely asserting it."""
        result = xref_index.find_implementers(loaded, "fya")
        classes = {i.implementer_class for i in result.implementers}
        assert classes == {"defpackage.fxz", "defpackage.fyo"}
        assert "defpackage.fyw" not in classes
        assert "defpackage.fyx" not in classes

    def test_interface_not_found_raises_keyerror(self, loaded):
        with pytest.raises(KeyError):
            xref_index.find_implementers(loaded, "ThisClassDoesNotExistAnywhereInThisApk")

    def test_ofd_abstract_class_has_zero_direct_interface_implementers(self, loaded):
        """`ofd` (`ofd.java:5`, `public abstract class ofd`) is an abstract class,
        not an interface — `mie`/`oex`/`ofb` all `extends` it (a superclass
        relationship, confirmed via androguard's own `get_superclassname()`),
        never `implements` it. This is a real, disclosed limit of a direct-
        `implements`-only query (SPEC.md §5a) demonstrated on real data — not a
        bug, and a correction to this project's own prior "authorization-policy
        interface" wording for `ofd` (REVERSE_ENGINEERING.md's `MaestroEndpointService`
        entry)."""
        result = xref_index.find_implementers(loaded, "ofd")
        assert result.implementers == []


class TestFieldWrites:
    def test_bluetooth_priority_receiver_field_c_written_by_hilt_injector(self, loaded):
        """SPEC.md §10a acceptance criterion 5 (v1.1, ai-sessions/0027): `fqm.o()`
        (`fqm.java:2710`, the Hilt member-injector for `BluetoothPriorityReceiver`,
        `@Override // defpackage.gnr`) writes `bluetoothPriorityReceiver.c = (fzd)
        this.ac.a();` — the sole write site for this field anywhere in the APK,
        independently read and confirmed before being trusted as a fixture."""
        result = xref_index.find_field_writes(
            loaded,
            "com.google.android.apps.wearables.maestro.companion.phone.bluetoothpriority.BluetoothPriorityReceiver",
            "c",
        )
        assert len(result.writes) == 1
        assert result.writes[0].caller_class == "defpackage.fqm"
        assert result.writes[0].caller_method == "o"

    def test_class_not_found_raises_keyerror(self, loaded):
        with pytest.raises(KeyError):
            xref_index.find_field_writes(loaded, "ThisClassDoesNotExistAnywhereInThisApk", "x")

    def test_never_written_field_name_returns_empty_not_error(self, loaded):
        """SPEC.md §7's own disclosed note: a field name that doesn't correspond
        to any real write site (whether never-written or simply misspelled)
        legitimately returns an empty, not-an-error result."""
        result = xref_index.find_field_writes(loaded, "esk", "thisFieldNameDoesNotExist")
        assert result.writes == []


class TestCli:
    def test_cli_refs_outputs_expected_json_shape(self, capsys):
        exit_code = cli.main(
            [
                "refs",
                "--apk-root",
                str(APK_ROOT),
                "--class",
                "giz",
                "--method",
                "p",
            ]
        )
        assert exit_code == 0
        captured = capsys.readouterr()
        assert '"caller_class": "defpackage.ftw"' in captured.out
        assert '"class": "defpackage.giz"' in captured.out

    def test_cli_unknown_class_is_non_zero_exit(self, capsys):
        exit_code = cli.main(
            [
                "refs",
                "--apk-root",
                str(APK_ROOT),
                "--class",
                "ThisClassDoesNotExistAnywhereInThisApk",
            ]
        )
        assert exit_code == 1

    def test_cli_implements_outputs_expected_json_shape(self, capsys):
        exit_code = cli.main(
            ["implements", "--apk-root", str(APK_ROOT), "--interface", "fya"]
        )
        assert exit_code == 0
        captured = capsys.readouterr()
        assert '"implementer_class": "defpackage.fyo"' in captured.out
        assert '"interface": "defpackage.fya"' in captured.out

    def test_cli_implements_unknown_interface_is_non_zero_exit(self, capsys):
        exit_code = cli.main(
            [
                "implements",
                "--apk-root",
                str(APK_ROOT),
                "--interface",
                "ThisClassDoesNotExistAnywhereInThisApk",
            ]
        )
        assert exit_code == 1

    def test_cli_field_writes_outputs_expected_json_shape(self, capsys):
        exit_code = cli.main(
            [
                "field-writes",
                "--apk-root",
                str(APK_ROOT),
                "--class",
                "com.google.android.apps.wearables.maestro.companion.phone.bluetoothpriority.BluetoothPriorityReceiver",
                "--field",
                "c",
            ]
        )
        assert exit_code == 0
        captured = capsys.readouterr()
        assert '"caller_class": "defpackage.fqm"' in captured.out
        assert '"caller_method": "o"' in captured.out

    def test_cli_field_writes_unknown_class_is_non_zero_exit(self, capsys):
        exit_code = cli.main(
            [
                "field-writes",
                "--apk-root",
                str(APK_ROOT),
                "--class",
                "ThisClassDoesNotExistAnywhereInThisApk",
                "--field",
                "x",
            ]
        )
        assert exit_code == 1
