"""SPEC.md §10b (v1.2, 2026-10-06) — the resource/string-table search.

`TestSynthetic` builds invented resource and source files under tmp_path (always runs);
`TestRealTree` reproduces an already-recorded chain from the maintainer's local tree
(REVERSE_ENGINEERING.md, `qhr` entry, 2026-10-03 Update) and is skipped without it.
"""

from __future__ import annotations

from pathlib import Path

import pytest

from structural_index import cli, resource_index

APK_ROOT = Path(__file__).resolve().parents[3] / "apk" / "v1.0.955078536-10253511"


@pytest.fixture()
def tree(tmp_path: Path) -> Path:
    values = tmp_path / "apktool-output" / "res" / "values"
    values.mkdir(parents=True)
    (values / "strings.xml").write_text(
        '<resources>\n    <string name="key_invented_toggle">invented_toggle</string>\n'
        '    <string name="title_invented">Invented feature</string>\n    <string name="empty_one" />\n</resources>\n')
    (values / "arrays.xml").write_text(
        '<resources>\n    <string-array name="invented_modes">\n        <item>One</item>\n        <item>Two</item>\n'
        '    </string-array>\n</resources>\n')
    (values / "public.xml").write_text(
        '<resources>\n    <public type="string" name="key_invented_toggle" id="0x7f140001" />\n'
        '    <public type="string" name="title_invented" id="0x7f140002" />\n</resources>\n')
    xml = tmp_path / "apktool-output" / "res" / "xml"
    xml.mkdir()
    (xml / "prefs.xml").write_text('<x android:key="@string/key_invented_toggle" android:title="@string/title_invented_other" />\n')
    smali = tmp_path / "apktool-output" / "smali"
    smali.mkdir()
    (smali / "zza.smali").write_text(".method a()V\n    const v8, 0x7f140001\n    const v9, 0x7f140099\n.end method\n")
    src = tmp_path / "jadx-output" / "sources" / "defpackage"
    src.mkdir(parents=True)
    (src / "zzb.java").write_text("class zzb {\n  String a = get(R.string.key_invented_toggle);\n  String b = get(R.string.key_invented_toggle_x);\n}\n")
    return tmp_path


class TestSynthetic:
    def test_key_search_gives_the_whole_chain(self, tree):
        r = resource_index.find_resources(tree, key_pattern="^key_invented")
        assert r.strings_scanned == 4 and len(r.matches) == 1
        m = r.matches[0]
        assert (m.value, m.defined_at, m.resource_id, m.public_at) == (
            "invented_toggle", "res/values/strings.xml:2", "0x7f140001", "res/values/public.xml:2")
        assert m.smali_uses == ["smali/zza.smali:2"]
        assert m.java_uses == ["defpackage/zzb.java:2"]  # not the longer name on line 3
        assert m.xml_uses == ["res/xml/prefs.xml:1"]

    def test_text_search_is_case_insensitive_and_finds_arrays(self, tree):
        r = resource_index.find_resources(tree, text_pattern="invented FEATURE|two")
        assert [(m.type, m.name) for m in r.matches] == [("string", "title_invented"), ("array", "invented_modes")]
        assert r.matches[1].value == "One | Two" and r.matches[1].resource_id is None

    def test_a_zero_result_is_not_an_error_and_a_missing_tree_is(self, tree, tmp_path):
        assert resource_index.find_resources(tree, key_pattern="nothing_like_this").matches == []
        with pytest.raises(FileNotFoundError):
            resource_index.find_resources(tmp_path / "nope", key_pattern="x")
        with pytest.raises(ValueError):
            resource_index.find_resources(tree)

    def test_cli(self, tree, capsys):
        assert cli.main(["strings", "--apk-root", str(tree), "--key", "empty", "--no-usages"]) == 0
        assert '"name": "empty_one"' in capsys.readouterr().out
        assert cli.main(["strings", "--apk-root", str(tree)]) == 1


@pytest.mark.skipif(not (APK_ROOT / "base.apk").is_file(),
                    reason=f"no decompiled APK found at {APK_ROOT} (PROJECT_RULES.md §8 rule 20: no copy is shipped)")
class TestRealTree:
    def test_head_gestures_key_chain_matches_the_recorded_one(self):
        # REVERSE_ENGINEERING.md, `qhr` entry, 2026-10-03 Update: key_head_gestures_toggle = 0x7f140315
        # (public.xml:5762, strings.xml:433), compared in cmi.b()'s branch for discriminator 5
        r = resource_index.find_resources(APK_ROOT, key_pattern="^key_head_gestures_toggle$")
        assert len(r.matches) == 1
        m = r.matches[0]
        assert (m.resource_id, m.defined_at, m.public_at) == ("0x7f140315", "res/values/strings.xml:433", "res/values/public.xml:5762")
        assert any(u.startswith("smali_classes2/cmi.smali:") for u in m.smali_uses)
        assert any("HeadGesturesSettingFragment" in u for u in m.java_uses + m.smali_uses)
        assert any(u.startswith("res/xml/head_gestures_preference.xml:") for u in m.xml_uses)

    def test_strings_xml_size_matches_the_resource_sweep_entry(self):
        # REVERSE_ENGINEERING.md, "Resource/string-table sweep": "Quartz" is title_feature_a_pref (strings.xml:960)
        r = resource_index.find_resources(APK_ROOT, text_pattern="^Quartz$", with_usages=False)
        assert [(m.name, m.defined_at) for m in r.matches] == [("title_feature_a_pref", "res/values/strings.xml:960")]
        assert r.strings_scanned > 900
