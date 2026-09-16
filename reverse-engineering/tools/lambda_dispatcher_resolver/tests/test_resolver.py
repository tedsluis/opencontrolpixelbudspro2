"""SPEC.md §10 acceptance criteria — the three hand-verified worked examples
plus one adversarial out-of-range fixture.

**Deliberately runs against the maintainer's own locally-decompiled APK tree
under `reverse-engineering/apk/<version>/`, never against a copy committed
into this tool's own `tests/` directory.** An earlier draft of this suite
committed byte-exact excerpts of `aie.smali`/`ftw.smali`/`krb.smali`/`esk.smali`
and their JADX `.java` counterparts as fixtures — that is exactly the
"decompiled Java/smali... banned from this project's codebase even as
research output" `PROJECT_RULES.md` §8 rule 20 (and this repo's own
`.gitignore` comment quoting it) explicitly prohibits, missed while writing
`SPEC.md` §11 and caught only once this suite was checked against that rule
before committing. The whole class below is skipped (not failed) when the
decompiled tree isn't present on the machine running the tests — the same
pattern `APK_REVERSE_ENGINEERING_PROCEDURE.md` already establishes for every
other script in this project that depends on a local decompile having been
run first.

Every assertion is the *exact* expected line range/text (SPEC.md §10's own
"no regression test may be marked passing by inspection alone" rule),
independently re-derived from the raw files while writing this tool, and
cross-checked against `REVERSE_ENGINEERING.md`'s own citations for the same
three classes (its `qhr`/`fye` entry for `aie`, its `frb`-`gjv` entry for
`ftw`).
"""

from __future__ import annotations

from pathlib import Path

import pytest

from lambda_dispatcher_resolver import androguard_index, cli, jadx_correlate, smali_reader

# The one, real, locally-decompiled APK version this tool has been built and
# verified against — see reverse-engineering/APK_VERSIONS.md.
APK_ROOT = Path(__file__).resolve().parents[3] / "apk" / "v1.0.955078536-10253511"

pytestmark = pytest.mark.skipif(
    not (APK_ROOT / "base.apk").is_file(),
    reason=(
        f"no decompiled APK found at {APK_ROOT} — run APK_REVERSE_ENGINEERING_PROCEDURE.md's "
        f"pull+decompile step first; this suite intentionally never ships its own copy of "
        f"decompiled code (PROJECT_RULES.md §8 rule 20)"
    ),
)


def _smali_path(class_short_name: str) -> Path:
    return APK_ROOT / "apktool-output" / "smali_classes2" / f"{class_short_name}.smali"


class TestSmaliReader:
    def test_aie_discriminator_7_packed_switch(self):
        """Fixture #1: `aie`, discriminator 7 -> :pswitch_c (JADX-undecompilable
        method, so smali is the only evidence — this is the same call
        `fyv.c()`'s Map.compute() remapping function makes, per
        REVERSE_ENGINEERING.md's qhr/fye entry."""
        path = _smali_path("aie")
        method = smali_reader.find_method(path, "a")
        assert method.start_line == 59
        assert method.end_line == 3990

        dispatch = smali_reader.analyze_dispatch(method, "Laie;", "c")
        assert dispatch.mechanism == "packed-switch"
        assert len(dispatch.branches) == 20

        evidence, status = smali_reader.resolve_branch(method, dispatch, 7, path)
        assert status == "resolved"
        assert evidence.branch_label == "pswitch_c"
        assert evidence.branch_line_range == (2544, 2610)
        assert evidence.raw_text.startswith("    :pswitch_c\n    move-object/from16 v1, p1")
        assert "invoke-virtual {v0, v1}, Lfyv;->a(Lqjc;)Loqh;" in evidence.raw_text
        assert "invoke-virtual {v0}, Loqh;->p()Lore;" in evidence.raw_text

    def test_ftw_discriminator_9_packed_switch(self):
        """Fixture #2: `ftw`, discriminator 9 -> :pswitch_8 -> the
        OtaApplyWorker completion callback that turned out to hold the
        long-sought caller of `gjv.p()` (REVERSE_ENGINEERING.md's frb-gjv
        entry, 2026-09-15 update). Note the label suffix ("_8") does NOT
        match the discriminator value ("9") — baksmali's label numbering is
        unrelated to the switch's own case values; this is exactly the trap
        a naive "match the label suffix to the discriminator" implementation
        would fall into."""
        path = _smali_path("ftw")
        method = smali_reader.find_method(path, "a")
        assert method.start_line == 38
        assert method.end_line == 724

        dispatch = smali_reader.analyze_dispatch(method, "Lftw;", "b")
        assert dispatch.mechanism == "packed-switch"
        assert len(dispatch.branches) == 18

        evidence, status = smali_reader.resolve_branch(method, dispatch, 9, path)
        assert status == "resolved"
        assert evidence.branch_label == "pswitch_8"
        assert evidence.branch_line_range == (217, 344)
        assert "OtaApplyWorker" in evidence.raw_text
        assert 'Lftj;->A(Ljava/lang/String;)Lfyc;' in evidence.raw_text

    def test_krb_discriminator_1_if_chain(self):
        """Fixture #3: `krb`, discriminator 1 -> the `this.a.a(obj, obj2)`
        branch (:cond_0) — the only one of the three worked examples using
        an if-chain instead of a packed-switch (a 2-case dispatcher, too
        small for the compiler to emit a switch table)."""
        path = _smali_path("krb")
        method = smali_reader.find_method(path, "apply")

        dispatch = smali_reader.analyze_dispatch(method, "Lkrb;", "b")
        assert dispatch.mechanism == "if-chain"
        assert len(dispatch.branches) == 2  # value 0 (:cond_1) and value 1 (:cond_0)

        evidence, status = smali_reader.resolve_branch(method, dispatch, 1, path)
        assert status == "resolved"
        assert evidence.branch_label == "cond_0"
        assert evidence.branch_line_range == (135, 150)
        assert "invoke-interface {p0, p1, p2}, Lpkk;->a(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;" in evidence.raw_text

    def test_ftw_discriminator_out_of_range_resolves_default_branch(self):
        """Adversarial fixture (SPEC.md §10's own explicit requirement): a
        discriminator value with no matching case must resolve to the real
        Dalvik "no case matched" fallthrough — which sits right after the
        `packed-switch` opcode itself, NOT after the last case label (that
        distinction is the whole point of this test: an earlier, wrong
        implementation pointed here at "last label + 2" and silently passed
        by looking plausible)."""
        path = _smali_path("ftw")
        method = smali_reader.find_method(path, "a")
        dispatch = smali_reader.analyze_dispatch(method, "Lftw;", "b")

        evidence, status = smali_reader.resolve_branch(method, dispatch, 999, path)
        assert status == "resolved-default-branch"
        assert evidence.branch_label is None
        # Must land before the first case label (:pswitch_0), not after the last one.
        assert evidence.branch_line_range[0] < min(dispatch.all_label_lines) + 1
        assert "check-cast p0, Loto;" in evidence.raw_text
        assert "invoke-virtual {p0}, Loto;->c()V" in evidence.raw_text

    def test_esk_discriminator_19_packed_switch(self):
        """Bonus fixture, not one of SPEC.md §10's required three, but a
        fourth already-known instance (esk, the pw_rpc send inside
        `fyv.a()`'s Rx chain, REVERSE_ENGINEERING.md's qhr/fye entry) — kept
        here as an extra regression check since it was independently
        verified during this tool's own implementation."""
        path = _smali_path("esk")
        method = smali_reader.find_method(path, "a")
        dispatch = smali_reader.analyze_dispatch(method, "Lesk;", "c")
        evidence, status = smali_reader.resolve_branch(method, dispatch, 19, path)
        assert status == "resolved"
        assert evidence.branch_label == "pswitch_0"
        assert "check-cast p1, Lnqo;" in evidence.raw_text


class TestJadxCorrelate:
    def test_aie_not_decompilable(self):
        ev = jadx_correlate.correlate(APK_ROOT, "Laie;", "a", "packed-switch", 7)
        assert ev.decompilable is False
        assert ev.case_text_found is False
        assert "1926" in ev.note

    def test_ftw_case_9_found(self):
        ev = jadx_correlate.correlate(APK_ROOT, "Lftw;", "a", "packed-switch", 9)
        assert ev.decompilable is True
        assert ev.case_text_found is True
        assert "OtaApplyWorker otaApplyWorker = (OtaApplyWorker) this.a;" in ev.raw_text
        assert '"On apply finished."' in ev.raw_text
        assert "case 10:" not in ev.raw_text  # must not spill into the next case

    def test_krb_if_chain_returns_whole_method_not_a_guessed_case(self):
        ev = jadx_correlate.correlate(APK_ROOT, "Lkrb;", "apply", "if-chain", 1)
        assert ev.decompilable is True
        assert ev.case_text_found is False
        assert "this.a.a(obj, obj2)" in ev.raw_text  # the whole method, not a slice of it
        assert "if-chain" in ev.note

    def test_class_descriptor_to_jadx_path_default_package(self):
        p = jadx_correlate.class_descriptor_to_jadx_path(APK_ROOT, "Laie;")
        assert p == APK_ROOT / "jadx-output" / "sources" / "defpackage" / "aie.java"

    def test_class_descriptor_to_jadx_path_real_package(self):
        p = jadx_correlate.class_descriptor_to_jadx_path(APK_ROOT, "Lcom/foo/Bar;")
        assert p == APK_ROOT / "jadx-output" / "sources" / "com" / "foo" / "Bar.java"


@pytest.fixture(scope="module")
def loaded_apk():
    return androguard_index.load_apk(APK_ROOT)


class TestAndroguardIndex:
    def test_list_finds_all_four_known_fixtures(self, loaded_apk):
        candidates = androguard_index.find_candidates(loaded_apk)
        names = {c.class_name for c in candidates}
        for expected in ("Laie;", "Lesk;", "Lftw;", "Lkrb;"):
            assert expected in names

    def test_resolve_class_maps_to_correct_dex_position(self, loaded_apk):
        found = androguard_index.resolve_class(loaded_apk, "aie")
        assert found is not None
        cls, dex_file, interface_name, discriminator_field = found
        assert dex_file == "classes2.dex"
        assert interface_name == "Lpkk;"
        assert discriminator_field == "c"


class TestResolveAll:
    """`resolve-all`'s own contract (added after the maintainer asked for it
    following the single-value `resolve` command): every explicitly-defined
    case, plus exactly one extra entry for the default branch — never an
    arbitrary guessed range (SPEC.md §7's "never guess" rule)."""

    def test_krb_if_chain_resolves_exactly_its_two_cases_plus_one_default(self):
        results = cli.cmd_resolve_all(APK_ROOT, "krb")
        by_value = {r.discriminator_value: r for r in results}
        assert len(results) == 3  # values 0, 1, and one default entry
        assert by_value[0].smali.branch_label == "cond_1"
        assert by_value[1].smali.branch_label == "cond_0"
        defaults = [r for r in results if r.resolution_status == "resolved-default-branch"]
        assert len(defaults) == 1
        assert defaults[0].discriminator_value not in (0, 1)

    def test_ftw_packed_switch_resolves_all_18_cases_plus_one_default(self):
        results = cli.cmd_resolve_all(APK_ROOT, "ftw")
        assert len(results) == 19  # 18 real cases (0-17) + 1 default
        by_value = {r.discriminator_value: r for r in results if r.resolution_status == "resolved"}
        assert set(by_value.keys()) == set(range(18))
        assert by_value[9].smali.branch_label == "pswitch_8"  # the OtaApplyWorker branch
        default_entries = [r for r in results if r.resolution_status == "resolved-default-branch"]
        assert len(default_entries) == 1
