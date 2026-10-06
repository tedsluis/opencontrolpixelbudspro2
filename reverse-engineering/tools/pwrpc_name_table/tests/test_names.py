"""SPEC.md §10 acceptance criteria.

`TestSynthetic` uses invented literals in a tmp tree and an invented wire table (always
runs). `TestRealTree` reads the maintainer's local decompiled tree and names ids that
`PROTOCOL.md` §2.2a already records as FACT; it is skipped (not failed) without the tree.
No decompiled content is stored here (PROJECT_RULES.md §8 rule 20).
"""

from __future__ import annotations

import json
from pathlib import Path

import pytest

from pwrpc_name_table import cli, names

REPO_ROOT = Path(__file__).resolve().parents[4]
APK_ROOT = REPO_ROOT / "reverse-engineering" / "apk" / "v1.0.955078536-10253511"


def _wire(services: dict[str, list[str]]) -> dict:
    """An invented census: {service name: [method names]} -> the JSON shape `census` writes."""
    out = {"logs": {}, "errors": {}, "services": {}, "methods": {}}
    entry = {"count": 1, "dir": {"Sent": 1}, "types": {"REQUEST": 1}, "first": ["x.log", 1], "logs": 1}
    for s, ms in services.items():
        out["services"][f"{names.h65599(s):#010x}"] = dict(entry)
        for m in ms:
            out["methods"][f"{names.h65599(s):#010x}/{names.h65599(m):#010x}"] = dict(entry)
    return out


@pytest.fixture()
def tree(tmp_path: Path) -> Path:
    src = tmp_path / "jadx-output" / "sources" / "defpackage"
    src.mkdir(parents=True)
    (src / "zza.java").write_text('class zza {\n  Object a = make("invented_pkg.Alpha", "DoThing");\n  String b = "quote \\" inside";\n}\n')
    smali = tmp_path / "apktool-output" / "smali"
    smali.mkdir(parents=True)
    (smali / "zzb.smali").write_text('.class Lzzb;\n.method a()V\n    const-string v0, "OnlyInSmali"\n    const-string/jumbo v1, "invented_pkg.Alpha"\n.end method\n')
    return tmp_path


class TestSynthetic:
    def test_hash_matches_the_values_pwrpc_decode_documents(self):
        # scripts/pwrpc_decode.py's own docstring
        assert names.h65599("maestro_pw.Maestro") == 0x7EDE71EA
        assert names.h65599("WriteSetting") == 0x9E8C9A1D

    def test_literals_come_from_java_and_smali_with_escapes(self, tree):
        lits = dict(names.iter_literals(tree))
        assert {"invented_pkg.Alpha", "DoThing", 'quote " inside', "OnlyInSmali"} <= set(lits)
        assert lits["OnlyInSmali"] == "apktool-output/smali/zzb.smali:3"

    def test_match_names_what_is_there_and_lists_what_is_not(self, tree):
        wire = _wire({"invented_pkg.Alpha": ["DoThing", "OnlyInSmali", "NotInTheTree"]})
        r = names.match(tree, wire)
        svc = next(iter(r["services"].values()))
        assert [n["name"] for n in svc["names"]] == ["invented_pkg.Alpha"]
        assert svc["names"][0]["found_in"] == ["jadx-output/sources/defpackage/zza.java:2", "apktool-output/smali/zzb.smali:4"]
        assert r["unnamed_services"] == []
        assert len(r["unnamed_methods"]) == 1 and r["unnamed_methods"][0].endswith(f"{names.h65599('NotInTheTree'):#010x}")
        assert r["distinct_literals_hashed"] == 4 and r["literal_occurrences_scanned"] == 5

    def test_output_holds_only_matched_names(self, tree):
        text = json.dumps(names.match(tree, _wire({"invented_pkg.Alpha": ["DoThing"]})))
        assert "OnlyInSmali" not in text and "quote" not in text

    def test_cli_match_and_errors(self, tree, tmp_path, capsys):
        w = tmp_path / "wire.json"
        w.write_text(json.dumps(_wire({"invented_pkg.Alpha": ["DoThing"]})))
        out = tmp_path / "o" / "t.json"
        assert cli.main(["match", "--apk-root", str(tree), "--wire-ids", str(w), "--output", str(out)]) == 0
        assert json.loads(out.read_text())["unnamed_methods"] == []
        assert cli.main(["match", "--apk-root", str(tmp_path / "nope"), "--wire-ids", str(w)]) == 1
        assert cli.main(["match", "--apk-root", str(tree), "--wire-ids", str(tmp_path / "missing.json")]) == 1
        assert cli.main(["census", "--log", str(tmp_path / "no-such-*.log")]) == 1
        assert cli.main(["hash", "WriteSetting"]) == 0
        assert "0x9e8c9a1d  WriteSetting" in capsys.readouterr().out


@pytest.mark.skipif(not (APK_ROOT / "base.apk").is_file(),
                    reason=f"no decompiled APK found at {APK_ROOT} (PROJECT_RULES.md §8 rule 20: no copy is shipped)")
class TestRealTree:
    def test_names_the_ids_protocol_md_records_as_fact(self):
        # PROTOCOL.md §2.2a (2026-09-20 and 2026-09-30 Updates): ids on the wire and their names
        wire = _wire({"maestro_pw.Maestro": ["WriteSetting", "ReadSetting", "SubscribeRuntimeInfo", "GetHardwareInfo"],
                      "maestro_pw.Dosimeter": ["FetchDailySummaries"],
                      "hr.core.software_update.UpdateHelperService": ["GetRunningVersion"]})
        assert "0x7ede71ea" in wire["services"] and "0x7ede71ea/0xe61e8290" in wire["methods"]
        r = names.match(APK_ROOT, wire)
        assert r["unnamed_services"] == [] and r["unnamed_methods"] == []
        assert r["services"]["0x73d5d805"]["names"][0]["name"] == "maestro_pw.Dosimeter"
        assert r["distinct_literals_hashed"] > 20000

    def test_an_id_with_no_literal_is_reported_unnamed(self):
        # (not 0x00000001: the one-character literal "\\u0000" hashes to 1 — SPEC.md §7 on collisions)
        wire = _wire({"maestro_pw.Maestro": ["WriteSetting"]})
        wire["services"]["0xdeadbeef"] = wire["services"]["0x7ede71ea"]
        r = names.match(APK_ROOT, wire)
        assert r["unnamed_services"] == ["0xdeadbeef"]
