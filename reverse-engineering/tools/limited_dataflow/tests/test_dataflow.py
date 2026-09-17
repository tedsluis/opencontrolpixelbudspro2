"""SPEC.md §10 acceptance criteria: the already-hand-traced `esk` discriminator 19 ->
`WriteSetting` chain (item 1), the adversarial basic-block-boundary fixture (item 2), a
synthetic ambiguous-redefinition fixture (item 3), and the out-of-range hard error (item 4).

**Deliberately runs its APK-dependent cases against the maintainer's own locally-decompiled
tree under `reverse-engineering/apk/<version>/`, never against a copy committed into this
tool's own `tests/` directory** -- the same discipline `schema_batch_extractor/tests/
test_batch_extract.py` and `uuid_ble_context/tests/test_uuid_scan.py` already established
(`PROJECT_RULES.md` §8 rule 20; `../BACKLOG.md`'s own governance section). Those cases are
skipped (not failed) when the decompiled tree isn't present. The synthetic-fixture case
(TestSyntheticFixtures below) is a handful of hand-written smali lines authored for this test
file, not an excerpt of the companion app's own source (SPEC.md §10 item 3's own framing) --
it does not depend on the APK tree at all and always runs.

**Corrections against SPEC.md's own §10 prose, made honestly rather than weakening a test to
match a bug (this session's own instructions):**

- Item 1's SPEC.md prose (§6's worked JSON example, and §10 item 1's own text) asserts
  `final_status: "reached_end_of_block"` for the `v0` trace through `esk` discriminator 19.
  Running the real tool against the real APK shows this is not what happens: after the
  documented `cast`-to-`qjc` step and the `sink_use` into `Lfys;-><init>`, the *same* `v0`
  register slot is legitimately reused later in the same branch by `const-wide/16 v0, 0x5`
  (the RPC call's own 5-second timeout setup, `REVERSE_ENGINEERING.md`'s `esk` entry) --  a
  real redefinition unrelated to the traced qjc/fys value, correctly caught by SPEC.md §3's
  own row "-" guardrail ("anything else that names the traced register at all ... is an
  explicit stop, not a guess"). The tool's behavior is correct per its own §3 rules; SPEC.md's
  §10/§6 prose describing the *expected final_status* for this exact fixture was wrong. This
  test asserts the real, verified `ambiguous_redefinition` outcome, and SPEC.md's own §6/§10
  text should be read with this correction in mind (noted here rather than silently
  "fixed" by weakening the assertion -- see the tool-4 write-up in
  `ai-sessions/0025_MAINTENANCE_RESULT_2026_09_16.md` for the full account of this
  correction). The `cast`/`sink_use` steps themselves, and the disclosed
  never-reaches-`nqo.e(...)` boundary (SPEC.md §1/§9), are exactly as documented and still
  the tool's own headline confirmation.
- Item 2's SPEC.md prose names `p1` as the traced register for the adversarial
  basic-block-boundary fixture. Against the real APK, `p1` does not reach a label/branch
  boundary at all -- it is reassigned by `move-result-object p1` (line 347, well before any
  label) first, which is *also* a correct `ambiguous_redefinition`, not the
  `left_basic_block_scope` outcome item 2 is meant to exercise. `v1` (defined by the branch's
  own `new-instance v1, Lfys;` at line 329, the wrapper object item 1's `sink_use` step
  constructs) survives, unredefined, all the way to the next `:pswitch_1` label at line 442 --
  this is the register this test uses to actually exercise row 4, per item 2's own stated
  intent ("confirming row 4 fires and does not silently wander into a sibling branch's code").
"""

from __future__ import annotations

from pathlib import Path

import pytest

from limited_dataflow import cli, dataflow
from limited_dataflow.dataflow import trace_lines

APK_ROOT = Path(__file__).resolve().parents[3] / "apk" / "v1.0.955078536-10253511"

_apk_missing = pytest.mark.skipif(
    not (APK_ROOT / "base.apk").is_file(),
    reason=(
        f"no decompiled APK found at {APK_ROOT} -- run APK_REVERSE_ENGINEERING_PROCEDURE.md's "
        f"pull+decompile step first; this suite intentionally never ships its own copy of "
        f"decompiled code (PROJECT_RULES.md §8 rule 20)"
    ),
)


@_apk_missing
class TestEskDiscriminator19PrimaryFixture:
    """SPEC.md §10 item 1 -- the already-hand-traced `esk` discriminator 19 ->
    `WriteSetting` chain (REVERSE_ENGINEERING.md's `esk`/`qhr`/`fye` entries)."""

    def test_trace_branch_v0_cast_then_sink_use_then_ambiguous_redefinition(self):
        result = dataflow.trace_branch(APK_ROOT, "esk", 19, start_line=325, start_register="v0")
        d = result.to_dict()
        assert d["class"] == "Lesk;"
        assert d["discriminator"] == 19
        assert d["branch_resolution_status"] == "resolved"

        steps = d["steps"]
        assert len(steps) == 2
        assert steps[0] == {"line": 333, "kind": "cast", "register": "v0", "to_type": "qjc"}
        assert steps[1] == {
            "line": 337,
            "kind": "sink_use",
            "register": "v0",
            "arg_position": 1,
            "invoke_kind": "invoke-direct",
            "called_class": "fys",
            "called_method": "<init>",
        }
        # The disclosed boundary (SPEC.md §1/§9): the trace never reaches `nqo`/`e` --
        # that call lives inside `fys`'s own body, out of single-basic-block scope.
        assert not any(s.get("called_class") == "nqo" for s in steps)

        # Corrected outcome (see module docstring): v0's register slot is legitimately
        # reused later in the same branch by the RPC timeout setup's own const-wide load,
        # correctly caught as an explicit stop rather than a silent guess.
        assert d["final_status"] == "ambiguous_redefinition"
        assert d["stop_line"] == 386
        assert d["stop_text"] == "const-wide/16 v0, 0x5"


@_apk_missing
class TestBasicBlockBoundaryAdversarialFixture:
    """SPEC.md §10 item 2 -- tracing past a resolved branch's own end, over the whole
    method (`trace-method`), must stop at the next real label/switch boundary (row 4),
    never wander into a sibling branch's code. See module docstring for why `v1`
    (not SPEC.md's literal `p1`) is the register that actually exercises this."""

    def test_trace_method_v1_survives_to_next_pswitch_label(self):
        result = dataflow.trace_method(APK_ROOT, "esk", "a", start_line=329, start_register="v1")
        d = result.to_dict()
        assert d["class"] == "Lesk;"
        assert d["method"] == "a"

        steps = d["steps"]
        assert [s["kind"] for s in steps] == ["sink_use", "sink_use", "sink_use"]
        assert steps[0]["line"] == 337 and steps[0]["called_class"] == "fys"
        assert steps[1]["line"] == 342 and steps[1]["called_class"] == "oqh" and steps[1]["called_method"] == "c"
        assert steps[2]["line"] == 394 and steps[2]["called_class"] == "oqh" and steps[2]["called_method"] == "m"

        assert d["final_status"] == "left_basic_block_scope"
        assert d["stop_line"] == 442
        assert d["stop_text"] == ":pswitch_1"


@_apk_missing
class TestOutOfRangeStartLineHardError:
    """SPEC.md §10 item 4 / §7 -- an out-of-range --start-line is a hard error, never a
    silent nearest-line guess."""

    def test_trace_branch_raises_valueerror_on_out_of_range_start_line(self):
        with pytest.raises(ValueError, match=r"outside the valid range \[315, 441\]"):
            dataflow.trace_branch(APK_ROOT, "esk", 19, start_line=1, start_register="v0")

    def test_trace_branch_raises_valueerror_when_start_line_omits_start_register(self):
        """§7's second named failure mode: the line at --start-line must actually mention
        --start-register, or this is a hard error (caller mis-identified the starting
        point) -- not a nearby-line guess."""
        with pytest.raises(ValueError, match=r"does not mention register 'v5'"):
            dataflow.trace_branch(APK_ROOT, "esk", 19, start_line=325, start_register="v5")

    def test_cli_trace_branch_out_of_range_is_non_zero_exit(self, capsys):
        exit_code = cli.main(
            [
                "trace-branch",
                "--apk-root",
                str(APK_ROOT),
                "--class",
                "esk",
                "--discriminator",
                "19",
                "--start-line",
                "1",
                "--start-register",
                "v0",
            ]
        )
        assert exit_code == 1
        captured = capsys.readouterr()
        assert "outside the valid range" in captured.err

    def test_cli_trace_branch_valid_input_outputs_expected_json_shape(self, capsys):
        exit_code = cli.main(
            [
                "trace-branch",
                "--apk-root",
                str(APK_ROOT),
                "--class",
                "esk",
                "--discriminator",
                "19",
                "--start-line",
                "325",
                "--start-register",
                "v0",
            ]
        )
        assert exit_code == 0
        captured = capsys.readouterr()
        assert '"class": "Lesk;"' in captured.out
        assert '"called_class": "fys"' in captured.out


class TestSyntheticFixtures:
    """SPEC.md §10 item 3 -- a deliberately simple, hand-written smali snippet (not from
    the live APK, per §10 item 3's own framing) exercising `ambiguous_redefinition` and
    the three alias-preserving shapes directly against `trace_lines`. Always runs; no
    APK tree required."""

    def test_ambiguous_redefinition_before_any_recognized_shape_applies(self):
        lines = [
            "move-result-object v0",
            "move-result-object v1",
            "invoke-virtual {v1}, Ljava/lang/Object;->toString()Ljava/lang/String;",
            "move-result-object v0",
            "return-object v0",
        ]
        result = trace_lines(lines, start_idx=0, end_idx=len(lines) - 1, start_register="v0")
        d = result.to_dict()
        assert d["steps"] == []
        assert d["final_status"] == "ambiguous_redefinition"
        assert d["stop_line"] == 4
        assert d["stop_text"] == "move-result-object v0"

    def test_alias_then_cast_then_sink_use_then_reached_end_of_block(self):
        """The three alias-preserving shapes (SPEC.md §3 rows 1-3) chained together,
        followed by an unrelated line that does not mention the traced register at all --
        must reach the end of the given range cleanly."""
        lines = [
            "new-instance v0, Ljava/lang/Object;",
            "move-object v1, v0",
            "check-cast v1, Ljava/lang/String;",
            "invoke-virtual {v1}, Ljava/lang/String;->trim()Ljava/lang/String;",
            "invoke-static {}, Ljava/lang/System;->gc()V",
        ]
        result = trace_lines(lines, start_idx=0, end_idx=len(lines) - 1, start_register="v0")
        d = result.to_dict()
        assert d["steps"] == [
            {"line": 2, "kind": "alias", "register": "v1", "from_register": "v0"},
            {"line": 3, "kind": "cast", "register": "v1", "to_type": "java/lang/String"},
            {
                "line": 4,
                "kind": "sink_use",
                "register": "v1",
                "arg_position": 0,
                "invoke_kind": "invoke-virtual",
                "called_class": "java/lang/String",
                "called_method": "trim",
            },
        ]
        assert d["final_status"] == "reached_end_of_block"
        assert d["final_register"] == "v1"

    def test_left_basic_block_scope_on_label(self):
        lines = [
            "move-result-object v0",
            ":some_label",
            "return-object v0",
        ]
        result = trace_lines(lines, start_idx=0, end_idx=len(lines) - 1, start_register="v0")
        d = result.to_dict()
        assert d["final_status"] == "left_basic_block_scope"
        assert d["stop_line"] == 2
        assert d["stop_text"] == ":some_label"

    def test_left_basic_block_scope_on_goto(self):
        lines = [
            "move-result-object v0",
            "goto :label_x",
            "return-object v0",
        ]
        result = trace_lines(lines, start_idx=0, end_idx=len(lines) - 1, start_register="v0")
        d = result.to_dict()
        assert d["final_status"] == "left_basic_block_scope"
        assert d["stop_line"] == 2
        assert d["stop_text"] == "goto :label_x"
