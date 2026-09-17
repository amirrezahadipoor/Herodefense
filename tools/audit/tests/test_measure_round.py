"""The audit's measuring tool has to be re-runnable by a reader, offline, from the tree alone."""
import pathlib
import sys
import tempfile
import unittest
import xml.etree.ElementTree as ElementTree

TOOLS = pathlib.Path(__file__).resolve().parents[1]
REPOSITORY = TOOLS.parents[1]
sys.path.insert(0, str(TOOLS))

import measure_round  # noqa: E402


class FactSheetTest(unittest.TestCase):
    def test_the_fact_sheet_carries_every_section_the_audit_cites(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            sheet = pathlib.Path(directory) / "facts.json"
            self.assertEqual(0, measure_round.main(["measure_round.py", "--json", str(sheet)]))
            import json
            facts = json.loads(sheet.read_text())
        for section in ("git", "tree", "tests", "integrity", "manifest", "ledger", "performance",
                        "content", "docs", "assets"):
            self.assertIn(section, facts, f"the audit cites {section}")
        self.assertEqual(157, facts["ledger"]["entries"])
        self.assertEqual(39, facts["ledger"]["materialMaps"])
        self.assertGreater(facts["tree"]["coreMainLines"], 20_000)
        self.assertIn("decodedBytes", facts["manifest"])

    def test_test_totals_come_out_of_the_junit_xml_and_not_out_of_an_annotation_count(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            results = pathlib.Path(directory)
            for name, tests, failures in (("TEST-A.xml", 7, 0), ("TEST-B.xml", 5, 1)):
                results.joinpath(name).write_text(
                    f'<testsuite tests="{tests}" failures="{failures}" errors="0" skipped="0" time="1.5"/>')
            totals = measure_round.count_tests(results)
        self.assertTrue(totals["measured"])
        self.assertEqual(2, totals["classes"])
        self.assertEqual(12, totals["tests"])
        self.assertEqual(1, totals["failures"])
        self.assertEqual(0, totals["errors"])
        self.assertEqual(0, totals["skipped"])
        self.assertAlmostEqual(3.0, totals["seconds"])

    def test_a_missing_results_directory_is_said_rather_than_counted_as_zero(self) -> None:
        totals = measure_round.count_tests(pathlib.Path("/nonexistent-results"))
        self.assertFalse(totals["measured"])
        self.assertIn("no results", totals["why"])

    def test_the_tool_needs_no_network_to_produce_the_numbers(self) -> None:
        source = (TOOLS / "measure_round.py").read_text(encoding="utf-8")
        for import_name in ("urllib", "requests", "httpx", "socket"):
            self.assertNotIn(f"import {import_name}", source,
                             "an audit figure that needs a network is not reproducible")

    def test_the_frozen_sheet_beside_the_audit_is_this_tool_s_output_shape(self) -> None:
        frozen = REPOSITORY / "docs/audit/MEASUREMENTS_2026-09-17.json"
        self.assertTrue(frozen.is_file(), "the audit cites a frozen fact sheet")
        import json
        facts = json.loads(frozen.read_text())
        for section in ("tree", "tests", "integrity", "manifest", "ledger", "performance",
                        "content", "docs", "assets", "git"):
            self.assertIn(section, facts)


if __name__ == "__main__":
    unittest.main()
