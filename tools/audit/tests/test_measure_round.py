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
        # The ledger pins one hash per shipped PNG, so it grows with the art rather than with the code: the count
        # is read off the ledger the sheet reports rather than pinned to a number that a render batch moves.
        ledger = json.loads(
            (REPOSITORY / "docs" / "asset_hashes.json").read_text(encoding="utf-8")
        )["sheets"]
        self.assertEqual(len(ledger), facts["ledger"]["entries"])
        self.assertGreater(facts["ledger"]["entries"], 100)
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
        for key in ("documents", "rulesLines", "readmeLines"):
            self.assertIn(key, facts)
        self.assertNotIn("audits", facts,
                         "the audit documents are gone, so the tool must not report a permanently empty list")


if __name__ == "__main__":
    unittest.main()


class ContentFlagTest(unittest.TestCase):
    """Roadmap M1: a flag that can be satisfied by a sentence is not a flag about the game."""

    FLAGS = ("backButton", "persian", "rightToLeft", "accessibility", "haptics")

    def test_a_feature_named_only_in_a_comment_is_not_reported(self) -> None:
        source = (
            "/** Device-local accessibility/audio/inventory preferences. */\n"
            "public class Fake {\n"
            "    // a colourBlind palette would go here\n"
            "}\n"
        )
        self.assertEqual([], measure_round.code_matches(
            {pathlib.Path("Fake.java"): source}, r"colourBlind|screenReader|accessibil"),
            "the word appeared twice, both times in prose: this is the exact shape of the false positive that"
            " made the tool report accessibility for a game that has none")

    def test_the_same_feature_in_code_is_reported_with_its_line(self) -> None:
        source = (
            "/** preferences */\n"
            "public class Fake {\n"
            "    public boolean colourBlind;\n"
            "}\n"
        )
        self.assertEqual(["Fake.java:3"], measure_round.code_matches(
            {pathlib.Path("Fake.java"): source}, r"colourBlind"))

    def test_stripping_comments_keeps_the_line_numbering_intact(self) -> None:
        source = "first\n/* a comment\nspans two lines */\nfourth // and a trailing one\nfifth\n"
        stripped = measure_round.strip_comments(source).splitlines()
        self.assertEqual(5, len(stripped), "an evidence line that points at the wrong line is worse than none")
        self.assertEqual("first", stripped[0].strip())
        self.assertNotIn("spans", stripped[2])
        self.assertEqual("fourth", stripped[3].strip())
        self.assertEqual("fifth", stripped[4].strip())

    def test_accessibility_is_reported_from_a_setting_and_not_from_a_word(self) -> None:
        # This assertion was "accessibility is false" until roadmap G3a shipped a reduced-motion switch, which
        # is what the flag is for and which flipped it. The old message said to replace it with a check that the
        # feature is real rather than merely mentioned, and that is this: the evidence has to name the settings
        # field a player toggles, because a javadoc word is exactly what made the flag lie before.
        content = measure_round.content_flags()
        self.assertTrue(content["accessibility"],
                        "the reduced-motion setting stopped being detected, which means the pattern, the field"
                        " or the comment-stripping moved")
        self.assertTrue(any(entry.startswith("GameSettings.java")
                            for entry in content["flagEvidence"]["accessibility"]),
                        "accessibility is reported from "
                        + str(content["flagEvidence"]["accessibility"])
                        + ", and none of those is the settings field the player toggles: a flag that cannot"
                          " point at a setting is a flag reading prose again")

    def test_every_flag_is_its_own_evidence_and_the_evidence_still_exists(self) -> None:
        content = measure_round.content_flags()
        roots = [REPOSITORY / "core/src", REPOSITORY / "android/src"]
        for flag in self.FLAGS:
            evidence = content["flagEvidence"][flag]
            self.assertEqual(content[flag], bool(evidence),
                             f"{flag} and its evidence disagree, which means one of them is computed twice")
            for entry in evidence:
                name, _, line = entry.rpartition(":")
                paths = [path for root in roots for path in root.rglob(name)]
                self.assertTrue(paths, f"{flag} cites {name}, which is not in the tree")
                length = len(paths[0].read_text(encoding="utf-8", errors="ignore").splitlines())
                self.assertLessEqual(int(line), length,
                                     f"{flag} cites {entry}, but that file is only {length} lines long")

    def test_the_features_that_do_exist_are_still_reported_as_existing(self) -> None:
        content = measure_round.content_flags()
        for flag in ("backButton", "persian", "rightToLeft", "haptics"):
            self.assertTrue(content[flag], f"{flag} stopped being detected: the pattern or the code moved")


class IntegrityScanTest(unittest.TestCase):
    """Roadmap I3: a metric about swallowed failures has to count failures, not sentences about them."""

    def test_a_comment_mentioning_the_suffix_is_not_a_swallowed_failure(self) -> None:
        text = (
            "# `|| true` was what made this step's own failure invisible\n"
            "run: python3 tools/render/render_hash_log.py write out || true\n"
            "          # a trailing comment with || true in it\n"
        )
        self.assertEqual(1, measure_round.or_true_count(text))

    def test_no_workflow_in_this_repository_swallows_a_failure_any_more(self) -> None:
        self.assertEqual(0, measure_round.integrity_scan()["workflow_always_true"],
                         "a workflow line ends in an or-true suffix, so a step can fail without the job failing:"
                         " either remove the suffix or say in the line why the failure does not matter")
