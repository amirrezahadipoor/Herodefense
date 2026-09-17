"""Negative and positive controls for the performance gates (roadmap R8.4/R8.5).

Every gate here is checked twice: once on input that has to pass, and once on input that has to fail. A gate
whose failing case has never been exercised is a gate nobody knows is working.
"""
from __future__ import annotations

import json
import pathlib
import random
import subprocess
import sys
import tempfile
import unittest
import zipfile

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1]))

import check_apk_budget  # noqa: E402
import check_wave50_memory  # noqa: E402
import parse_startup  # noqa: E402
import render_performance_doc  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parents[3]


def body(size: int) -> bytes:
    """Incompressible bytes, so a fixture of N bytes stays about N bytes inside the zip.

    A fixture of repeated characters compresses to nothing, which would make a budget test pass for the
    wrong reason: the measurement would never reach the threshold being tested. Seeded, so runs are stable.
    """
    return random.Random(7).randbytes(size)


def make_apk(path: pathlib.Path, assets_bytes: int, natives: dict[str, int]) -> pathlib.Path:
    """A zip shaped like an APK: an assets payload and one native library per ABI."""
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as archive:
        archive.writestr("assets/generated/hero.png", body(assets_bytes))
        archive.writestr("classes.dex", body(1024))
        for abi, size in natives.items():
            archive.writestr(f"lib/{abi}/libgdx.so", body(size))
    return path


class CheckApkBudgetTest(unittest.TestCase):
    def setUp(self) -> None:
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.dir = pathlib.Path(self.tmp.name)
        self.budget = {
            "thresholds": {
                "apkBytes": 40_000_000,
                "assetsBytes": 20_000_000,
                "nativeBytesPerAbi": {"arm64-v8a": 10_000_000, "armeabi-v7a": 10_000_000,
                                      "x86_64": 10_000_000},
                "requiredAbis": ["arm64-v8a", "armeabi-v7a", "x86_64"],
            }
        }

    def test_an_apk_inside_the_budget_passes(self) -> None:
        apk = make_apk(self.dir / "ok.apk", 200_000,
                       {"arm64-v8a": 1_000_000, "armeabi-v7a": 900_000, "x86_64": 950_000})
        measured = check_apk_budget.measure(apk)
        self.assertEqual([], check_apk_budget.check(measured, self.budget))
        self.assertGreater(measured["assetsCompressedBytes"], 0)
        self.assertEqual({"arm64-v8a", "armeabi-v7a", "x86_64"}, set(measured["nativeBytes"]))

    def test_an_oversized_apk_fails_with_a_reason_that_names_the_number(self) -> None:
        apk = make_apk(self.dir / "big.apk", 2_000_000,
                       {"arm64-v8a": 1_000_000, "armeabi-v7a": 1_000_000, "x86_64": 1_000_000})
        budget = json.loads(json.dumps(self.budget))
        budget["thresholds"]["apkBytes"] = 10_000
        problems = check_apk_budget.check(check_apk_budget.measure(apk), budget)
        self.assertTrue(problems)
        self.assertIn("budget", problems[0])

    def test_a_missing_abi_fails_even_when_every_size_is_fine(self) -> None:
        apk = make_apk(self.dir / "one-abi.apk", 200_000, {"arm64-v8a": 1_000_000})
        problems = check_apk_budget.check(check_apk_budget.measure(apk), self.budget)
        self.assertEqual(1, len(problems), problems)
        self.assertIn("missing native libraries", problems[0])
        self.assertIn("armeabi-v7a", problems[0])

    def test_the_summary_states_every_threshold_it_compares(self) -> None:
        apk = make_apk(self.dir / "ok.apk", 200_000,
                       {"arm64-v8a": 1_000_000, "armeabi-v7a": 900_000, "x86_64": 950_000})
        text = "\n".join(check_apk_budget.summary_lines(check_apk_budget.measure(apk), self.budget))
        for token in ("APK file", "assets payload", "arm64-v8a", "largest entry"):
            self.assertIn(token, text)

    def test_the_cli_exits_non_zero_when_the_budget_breaks(self) -> None:
        apk = make_apk(self.dir / "cli.apk", 200_000,
                       {"arm64-v8a": 1_000_000, "armeabi-v7a": 900_000, "x86_64": 950_000})
        budget_path = self.dir / "budget.json"
        budget = json.loads(json.dumps(self.budget))
        budget["thresholds"]["apkBytes"] = 1000
        budget_path.write_text(json.dumps(budget))
        code = subprocess.run(
            [sys.executable, str(pathlib.Path(check_apk_budget.__file__)), str(apk),
             "--budget", str(budget_path)],
            capture_output=True, text=True, check=False,
        ).returncode
        self.assertEqual(1, code)

    def test_every_committed_budget_is_wired_into_a_workflow(self) -> None:
        """A budget nobody runs is a number, not a gate."""
        workflows = "\n".join(
            path.read_text() for path in (ROOT / ".github/workflows").glob("*.yml")
        )
        script = "\n".join(
            path.read_text() for path in (ROOT / "tools/perf").glob("*.py")
        )
        for budget_path in sorted((ROOT / "docs/perf").glob("*budget*.json")):
            budget = json.loads(budget_path.read_text())
            self.assertTrue(budget["thresholds"], f"{budget_path.name} has no thresholds")
            self.assertIn("enforced_by", budget)
            enforcer = budget["enforced_by"].split(",")[0].strip().split(" ")[0]
            self.assertTrue(
                pathlib.Path(enforcer).is_file(),
                f"{budget_path.name} names an enforcer that does not exist: {enforcer}",
            )
            self.assertIn(
                pathlib.Path(enforcer).name, workflows,
                f"{budget_path.name} is enforced by {enforcer}, which no workflow runs",
            )
            self.assertIn(
                budget_path.name, script,
                f"no tool reads {budget_path.name}: its thresholds are decoration",
            )


class ParseStartupTest(unittest.TestCase):
    def test_the_platform_displayed_line_is_parsed(self) -> None:
        text = (
            "09-17 06:20:11.001  1234  1234 I ActivityTaskManager: Displayed "
            "com.amirrezahadipoor.herodefense/.android.AndroidLauncher: +1s234ms\n"
        )
        self.assertEqual([("Displayed com.amirrezahadipoor.herodefense/.android.AndroidLauncher", 1234)],
                         parse_startup.parse(text))

    def test_am_start_timing_is_parsed_too(self) -> None:
        text = "TotalTime: 1420\nThisTime: 1380\n"
        self.assertEqual([("TotalTime", 1420), ("ThisTime", 1380)], parse_startup.parse(text))

    def test_a_capture_without_a_measurement_is_a_missing_measurement_not_a_pass(self) -> None:
        self.assertEqual([], parse_startup.parse("I/SomeOtherTag: nothing to see here\n"))

    def test_the_component_filter_ignores_other_apps(self) -> None:
        text = (
            "Displayed com.example.other/.Main: +2s000ms\n"
            "Displayed com.amirrezahadipoor.herodefense/.android.AndroidLauncher: +1s100ms\n"
        )
        found = parse_startup.parse(text, "amirrezahadipoor")
        self.assertEqual([("Displayed com.amirrezahadipoor.herodefense/.android.AndroidLauncher", 1100)],
                         found)

    def test_the_cli_reports_a_missing_measurement_distinctly(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            log = pathlib.Path(tmp) / "logcat.txt"
            log.write_text("no startup line here\n")
            result = subprocess.run(
                [sys.executable, str(pathlib.Path(parse_startup.__file__)), str(log)],
                capture_output=True, text=True, check=False,
            )
            self.assertEqual(2, result.returncode, result.stdout)
            self.assertIn("NOT MEASURED", result.stdout)

    def test_the_cli_fails_a_slow_start_and_passes_a_quick_one(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            budget = tmp_path / "startup_budget.json"
            budget.write_text(json.dumps({"thresholds": {"coldStartMillis": 1200}}))
            slow = tmp_path / "slow.txt"
            slow.write_text("Displayed com.amirrezahadipoor.herodefense/.android.AndroidLauncher: +3s500ms\n")
            fast = tmp_path / "fast.txt"
            fast.write_text("Displayed com.amirrezahadipoor.herodefense/.android.AndroidLauncher: +0s900ms\n")
            slow_code = subprocess.run(
                [sys.executable, str(pathlib.Path(parse_startup.__file__)), str(slow),
                 "--budget", str(budget)], capture_output=True, text=True, check=False).returncode
            fast_code = subprocess.run(
                [sys.executable, str(pathlib.Path(parse_startup.__file__)), str(fast),
                 "--budget", str(budget)], capture_output=True, text=True, check=False).returncode
            self.assertEqual(1, slow_code)
            self.assertEqual(0, fast_code)


class WaveFiftyMemoryTest(unittest.TestCase):
    BUDGET = {"thresholds": {"totalPssKib": 409600, "graphicsKib": 163840}}

    def test_a_measurement_line_is_parsed_with_its_thousands_separators(self) -> None:
        text = (
            "I/HERODEFENSE_PERF: HERODEFENSE_PERF wave=50 totalPssKb=284,112 totalRssKb=402,880 "
            "graphicsKb=98,120\n"
        )
        found = check_wave50_memory.measurements(text)
        self.assertEqual(1, len(found))
        self.assertEqual(284112, found[0]["totalPssKb"])
        self.assertEqual(98120, found[0]["graphicsKb"])

    def test_lines_without_a_measurement_are_ignored(self) -> None:
        self.assertEqual([], check_wave50_memory.measurements(
            "I/HERODEFENSE_PERF: entering wave\nI/other: totalPssKb=1\n"
        ))

    def test_a_set_inside_the_budget_passes_and_an_oversized_one_fails_with_the_number(self) -> None:
        ok = {"totalPssKb": 300_000, "totalRssKb": 400_000, "graphicsKb": 100_000}
        self.assertEqual([], check_wave50_memory.problems(ok, self.BUDGET))
        over = {"totalPssKb": 500_000, "totalRssKb": 600_000, "graphicsKb": 200_000}
        issues = check_wave50_memory.problems(over, self.BUDGET)
        self.assertEqual(2, len(issues), issues)
        self.assertIn("500000", issues[0].replace(",", ""))
        self.assertIn("graphics", issues[1])

    def test_the_cli_distinguishes_missing_from_failing(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_path = pathlib.Path(tmp)
            budget = tmp_path / "wave50_memory_budget.json"
            budget.write_text(json.dumps(self.BUDGET))
            empty = tmp_path / "empty.txt"
            empty.write_text("nothing measured here\n")
            missing = subprocess.run(
                [sys.executable, str(pathlib.Path(check_wave50_memory.__file__)), str(empty),
                 "--budget", str(budget)], capture_output=True, text=True, check=False)
            self.assertEqual(2, missing.returncode)
            self.assertIn("NOT MEASURED", missing.stdout)

            good = tmp_path / "good.txt"
            good.write_text("I/HERODEFENSE_PERF: HERODEFENSE_PERF wave=50 totalPssKb=250000 "
                            "totalRssKb=300000 graphicsKb=90000\n")
            passed = subprocess.run(
                [sys.executable, str(pathlib.Path(check_wave50_memory.__file__)), str(good),
                 "--budget", str(budget)], capture_output=True, text=True, check=False)
            self.assertEqual(0, passed.returncode, passed.stdout)

    def test_the_shipped_budget_is_the_one_the_app_asserts(self) -> None:
        shipped = json.loads((ROOT / "docs/perf/wave50_memory_budget.json").read_text())
        threshold = shipped["thresholds"]["totalPssKib"]
        source = (ROOT / "core/src/main/java/com/amirrezahadipoor/herodefense/render/"
                  "RuntimeResidency.java").read_text()
        self.assertIn(str(threshold), source,
                      "the app-side constant and the committed budget have to be the same number")


class PerformancePageTest(unittest.TestCase):
    def test_runs_are_well_formed_and_the_page_is_in_sync(self) -> None:
        runs = render_performance_doc.load_runs()
        self.assertTrue(runs, "at least one logged run has to exist for the rule to be meaningful")
        ids = set()
        for run in runs:
            self.assertNotIn(run["id"], ids, "two runs share an id")
            ids.add(run["id"])
            self.assertRegex(run["commit"], r"^[0-9a-f]{7,40}$")
            self.assertRegex(run["date"], r"^\d{4}-\d{2}-\d{2}$")
            self.assertTrue(run["command"].strip())
            self.assertTrue(run["metrics"], f"{run['id']} logs no metrics")
            for metric in run["metrics"]:
                self.assertIn(metric["unit"], ("bytes", "MiB", "MB", "ms", "s", "fps", "MB/s",
                                               "count", "percent"))
        expected = render_performance_doc.render(runs, render_performance_doc.load_budgets())
        committed = (ROOT / "docs/perf/PERFORMANCE.md").read_text()
        self.assertEqual(expected, committed,
                         "PERFORMANCE.md is stale: run tools/perf/render_performance_doc.py --write")

    def test_a_hand_edited_number_shows_up_as_a_diff(self) -> None:
        runs = render_performance_doc.load_runs()
        budgets = render_performance_doc.load_budgets()
        page = render_performance_doc.render(runs, budgets)
        # the one thing the gate exists to catch: a number typed into the page instead of logged
        tampered = page.replace(str(runs[0]["metrics"][0]["value"]), "1", 1)
        self.assertNotEqual(page, tampered)
        self.assertNotEqual(tampered, (ROOT / "docs/perf/PERFORMANCE.md").read_text())


if __name__ == "__main__":
    unittest.main()
