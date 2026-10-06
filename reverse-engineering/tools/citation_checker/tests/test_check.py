"""SPEC.md §10 acceptance criteria.

Two groups. `TestSynthetic*` builds a tiny tree of invented files under pytest's
tmp_path (no decompiled content, always runs) and exercises the parser and every
verdict. `TestRealTree` reads the maintainer's local, gitignored
`reverse-engineering/apk/<version>/` tree and already-confirmed citations of
`REVERSE_ENGINEERING.md`; it is skipped (not failed) when that tree is absent —
this suite never ships a copy of decompiled code (PROJECT_RULES.md §8 rule 20).
"""

from __future__ import annotations

import json
from pathlib import Path

import pytest

from citation_checker import check, cli

REPO_ROOT = Path(__file__).resolve().parents[4]
APK_ROOT = REPO_ROOT / "reverse-engineering" / "apk" / "v1.0.955078536-10253511"


@pytest.fixture()
def tree(tmp_path: Path) -> Path:
    """An invented mini tree: two Java files with the same basename, one smali file, a manifest."""
    src = tmp_path / "jadx-output" / "sources"
    (src / "defpackage").mkdir(parents=True)
    (src / "com" / "example").mkdir(parents=True)
    (src / "defpackage" / "abc.java").write_text(
        "package defpackage;\n\nclass abc {\n    void a() {\n        log(\"Invented log line one\");\n    }\n\n"
        "    void b() {\n        x.b = 13;\n    }\n}\n")
    (src / "com" / "example" / "abc.java").write_text("package com.example;\nclass abc {}\n")
    (src / "com" / "example" / "WidgetFragment.java").write_text("\n".join(f"// line {n}" for n in range(1, 41)) + "\n")
    smali = tmp_path / "apktool-output" / "smali_classes2"
    smali.mkdir(parents=True)
    (smali / "abc.smali").write_text(".class Labc;\n\n.method a()V\n    const/16 v2, 0x1d\n    iput v2, v1, Lzzz;->b:I\n.end method\n")
    (tmp_path / "apktool-output" / "AndroidManifest.xml").write_text("<manifest>\n<application/>\n</manifest>\n")
    resxml = tmp_path / "apktool-output" / "res" / "xml"
    resxml.mkdir(parents=True)
    (resxml / "invented_prefs.xml").write_text("<a>\n<b key=\"invented_key\"/>\n</a>\n")
    split = tmp_path / "apktool-output-arm64_v8a"
    split.mkdir()
    (split / "AndroidManifest.xml").write_text("<manifest/>\n")
    return tmp_path


def _check(tree: Path, tmp_path: Path, markdown: str) -> list[check.Citation]:
    doc = tmp_path / "DOC.md"
    doc.write_text(markdown)
    return check.check_document(doc, check.TreeIndex(tree), doc_name="DOC.md")


class TestSyntheticParser:
    def test_explicit_range_with_en_dash_and_space_before_colon(self):
        cites = check.parse_citations("d", "see `abc.java:4-6` and `abc.smali :4–5` here")
        assert [(c.kind, c.name, c.start, c.end) for c in cites] == [
            ("explicit", "abc.java", 4, 6), ("explicit", "abc.smali", 4, 5)]

    def test_bare_file_and_path_with_ellipsis(self):
        cites = check.parse_citations("d", "the fragment `…/example/WidgetFragment.java` (`:5`, `:9`)")
        assert [(c.kind, c.name, c.start) for c in cites] == [
            ("bare", "…/example/WidgetFragment.java", None),
            ("continuation", "…/example/WidgetFragment.java", 5),
            ("continuation", "…/example/WidgetFragment.java", 9)]

    def test_continuation_does_not_cross_a_blank_line(self):
        cites = check.parse_citations("d", "`abc.java:4` first.\n\nA new paragraph with (`:9`) only.")
        assert [c.kind for c in cites] == ["explicit"]

    def test_table_continuation_uses_the_header_column(self):
        md = ("| Field | Write site | Read case (`WidgetFragment.java`) |\n|---|---|---|\n"
              "| 13 | `abc.java:8-10` | case 13 (`:20-22`) |\n")
        cites = check.parse_citations("d", md)
        cont = [c for c in cites if c.kind == "continuation"]
        assert len(cont) == 1 and cont[0].name == "WidgetFragment.java" and (cont[0].start, cont[0].end) == (20, 22)

    def test_a_time_of_day_is_not_a_continuation(self):
        assert check.parse_citations("d", "at 08:24:17 the screen showed `abc.java` open") [0].kind == "bare"
        assert len(check.parse_citations("d", "at 08:24:17 the screen showed `abc.java` open")) == 1


class TestSyntheticVerdicts:
    def test_confirmed_by_a_quoted_log_string(self, tree, tmp_path):
        c = _check(tree, tmp_path, 'It logs "Invented log line one" (`abc.java:5`).')[0]
        assert c.verdict == "confirmed" and c.resolved == "jadx-output/sources/defpackage/abc.java" and c.candidates == 2

    def test_drifted_reports_the_real_line(self, tree, tmp_path):
        c = _check(tree, tmp_path, 'It sets `x.b = 13;` at `abc.java:3`... no wait, see `abc.java:1`.')[0]
        # the token is on line 9; the first citation says line 3 (slack 3 does not reach it)
        assert c.verdict == "drifted" and c.nearest_token_line == 9

    def test_in_file_no_token_and_range_out_of_file(self, tree, tmp_path):
        cs = _check(tree, tmp_path, "See `abc.java:4-6`.\n\nAnd `abc.java:900`.")
        assert [c.verdict for c in cs] == ["in_file_no_token", "range_out_of_file"]

    def test_missing_file_and_bare_exists(self, tree, tmp_path):
        cs = _check(tree, tmp_path, "`nosuchclass.java:1` and `abc.smali`.")
        assert [c.verdict for c in cs] == ["missing_file", "exists"]

    def test_directory_narrows_and_main_apktool_output_is_preferred(self, tree, tmp_path):
        cs = _check(tree, tmp_path, "`com/example/abc.java:2` and `AndroidManifest.xml:2`.")
        assert cs[0].resolved == "jadx-output/sources/com/example/abc.java"
        assert cs[1].resolved == "apktool-output/AndroidManifest.xml" and cs[1].verdict == "in_file_no_token"

    def test_preference_xml_is_resolved(self, tree, tmp_path):
        c = _check(tree, tmp_path, 'the key `key="invented_key"` (`res/xml/invented_prefs.xml:2`)')[0]
        assert c.resolved == "apktool-output/res/xml/invented_prefs.xml" and c.verdict == "confirmed"

    def test_smali_token(self, tree, tmp_path):
        c = _check(tree, tmp_path, "`abc.smali:5` is `iput v2, v1, Lzzz;->b:I` after `const/16 v2, 0x1d`.")[0]
        assert c.verdict == "confirmed"

    def test_output_never_carries_file_content(self, tree, tmp_path):
        c = _check(tree, tmp_path, "See `abc.java:4-6`.")[0]
        assert "Invented log line one" not in json.dumps(c.to_dict())


class TestSyntheticCli:
    def test_cli_writes_json_and_summary(self, tree, tmp_path, capsys):
        (tmp_path / "DOC.md").write_text('logs "Invented log line one" (`abc.java:5`); `nosuch.java:3`')
        out = tmp_path / "out" / "c.json"
        rc = cli.main(["check", "--apk-root", str(tree), "--repo-root", str(tmp_path), "--doc", "DOC.md", "--output", str(out)])
        assert rc == 0
        payload = json.loads(out.read_text())
        assert payload["summary"]["by_verdict"] == {"confirmed": 1, "missing_file": 1}
        assert "by_verdict" in capsys.readouterr().out

    def test_cli_errors_are_not_silent(self, tree, tmp_path, capsys):
        assert cli.main(["check", "--apk-root", str(tmp_path / "nope"), "--repo-root", str(tmp_path), "--doc", "X.md"]) == 1
        assert cli.main(["check", "--apk-root", str(tree), "--repo-root", str(tmp_path), "--doc", "X.md"]) == 1
        assert "no document matches" in capsys.readouterr().err


real_tree = pytest.mark.skipif(
    not (APK_ROOT / "base.apk").is_file(),
    reason=(f"no decompiled APK found at {APK_ROOT} — this suite never ships its own copy of decompiled code "
            f"(PROJECT_RULES.md §8 rule 20)"),
)


@pytest.fixture(scope="module")
def index():
    return check.TreeIndex(APK_ROOT)


@real_tree
class TestRealTree:
    """Already-confirmed citations as regression fixtures (BACKLOG.md governance)."""

    def _one(self, index, tmp_path, md):
        doc = tmp_path / "D.md"
        doc.write_text(md)
        return check.check_document(doc, index, doc_name="D.md")

    def test_gbm_pigweed_log_string(self, index, tmp_path):
        # REVERSE_ENGINEERING.md, `gbm` entry: the log string at gbm.java:36
        c = self._one(index, tmp_path, 'logging `"Provide pigweed internal rfcomm socket"` (`gbm.java:36`)')[0]
        assert c.verdict == "confirmed" and c.resolved == "jadx-output/sources/defpackage/gbm.java"

    def test_field_29_smali_write_site(self, index, tmp_path):
        # REVERSE_ENGINEERING.md, `qhr` entry, 2026-10-03 Update (ai-sessions/0069)
        md = "`apktool-output/smali_classes2/cmi.smali:3499` is `iput v2, v1, Lqhr;->b:I` directly after `const/16 v2, 0x1d`"
        c = self._one(index, tmp_path, md)[0]
        assert c.verdict == "confirmed" and c.resolved == "apktool-output/smali_classes2/cmi.smali"

    def test_default_package_is_preferred_for_an_ambiguous_basename(self, index, tmp_path):
        c = self._one(index, tmp_path, "`a.java:487-489`")[0]
        assert c.resolved == "jadx-output/sources/defpackage/a.java" and c.candidates > 1

    def test_a_wrong_line_is_reported_as_drifted(self, index, tmp_path):
        c = self._one(index, tmp_path, 'logging `"Provide pigweed internal rfcomm socket"` (`gbm.java:5`)')[0]
        assert c.verdict == "drifted" and c.nearest_token_line == 36

    def test_whole_default_document_set_runs_and_no_range_falls_outside_a_file(self, index):
        cites = []
        for pat in cli.DEFAULT_DOCS:
            for doc in sorted(REPO_ROOT.glob(pat)):
                cites.extend(check.check_document(doc, index, doc_name=str(doc.relative_to(REPO_ROOT))))
        assert len(cites) > 900
        assert not [c for c in cites if c.verdict == "range_out_of_file"]
