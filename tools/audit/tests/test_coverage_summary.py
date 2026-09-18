"""Roadmap I1: the sentence CI publishes has to be the sentence the report contains."""
import pathlib
import sys
import tempfile
import unittest
import xml.etree.ElementTree as ElementTree

TOOLS = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import coverage_summary  # noqa: E402


class CoverageSummaryTest(unittest.TestCase):
    REPORT = (
        '<?xml version="1.0" encoding="UTF-8"?>'
        '<report name="core">'
        '<counter type="INSTRUCTION" missed="10" covered="30"/>'
        '<counter type="LINE" missed="250" covered="750"/>'
        '<counter type="CLASS" missed="4" covered="96"/>'
        "</report>"
    )

    def write(self, directory: pathlib.Path, text: str) -> pathlib.Path:
        path = directory / "jacocoTestReport.xml"
        path.write_text(text, encoding="utf-8")
        return path

    def test_the_line_counter_is_the_one_reported(self) -> None:
        with tempfile.TemporaryDirectory() as name:
            path = self.write(pathlib.Path(name), self.REPORT)
            self.assertEqual((750, 1000), coverage_summary.line_coverage(str(path)))
        self.assertEqual("core line coverage: 75.0% (750 of 1000 lines)",
                         coverage_summary.sentence(750, 1000))

    def test_a_report_without_a_line_counter_is_a_failure_not_a_zero(self) -> None:
        with tempfile.TemporaryDirectory() as name:
            path = self.write(pathlib.Path(name), self.REPORT.replace('type="LINE"', 'type="BRANCH"'))
            with self.assertRaises(SystemExit):
                coverage_summary.line_coverage(str(path))

    def test_the_summary_file_gets_the_same_sentence_the_log_gets(self) -> None:
        with tempfile.TemporaryDirectory() as name:
            directory = pathlib.Path(name)
            report = self.write(directory, self.REPORT)
            summary = directory / "step.md"
            self.assertEqual(0, coverage_summary.main(
                ["coverage_summary.py", str(report), str(summary)]))
            self.assertIn("core line coverage: 75.0%", summary.read_text(encoding="utf-8"))
            self.assertIn("reported, not gated", summary.read_text(encoding="utf-8"),
                          "the heading is the promise: a future gate gets its own commit and its own baseline")


if __name__ == "__main__":
    unittest.main()
