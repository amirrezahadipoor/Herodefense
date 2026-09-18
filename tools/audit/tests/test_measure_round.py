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

    def test_every_section_the_tool_reports_is_a_section_it_can_produce(self) -> None:
        # The audits used to keep a frozen sheet beside them and this test read it back. Those documents were
        # deleted on 2026-09-18 at the owner's direction, so the shape is now checked against the tool itself:
        # a fact sheet nobody can regenerate is not evidence, and this one can be.
        sections = {
            "tree": measure_round.largest_classes,
            "integrity": measure_round.integrity_scan,
            "manifest": measure_round.manifest_facts,
            "ledger": measure_round.ledger_facts,
            "performance": measure_round.performance_runs,
            "content": measure_round.content_flags,
            "docs": measure_round.docs_facts,
            "assets": measure_round.asset_inventory,
            "git": measure_round.git_facts,
        }
        for name, builder in sections.items():
            self.assertTrue(callable(builder), name)
        facts = measure_round.docs_facts()
        for key in ("documents", "roadmapLines", "roadmapDone", "roadmapOpen", "readmeLines"):
            self.assertIn(key, facts)
        self.assertNotIn("audits", facts,
                         "the audit documents are gone, so the tool must not report a permanently empty list")


if __name__ == "__main__":
    unittest.main()
