#!/usr/bin/env python3
"""Turn a Jacoco report into one sentence, for a CI step summary and for a human.

Roadmap I1. Coverage here is reported and not gated, on purpose: a gate set before anybody has seen the number
is a gate set to a guess, and this suite is heavy on source-scanning and string-table tests, whose "coverage"
measures how much of a test helper ran rather than how much of the game is exercised. So the first commit
publishes the number and the report; the first gate gets set from a measured baseline, in its own commit, with
the classes it excludes named.

Usage: coverage_summary.py <jacocoTestReport.xml> [summary-file]
Prints the line-coverage sentence, and appends it to the summary file when one is given (CI passes
$GITHUB_STEP_SUMMARY). Exits non-zero only when the report cannot be read or has no LINE counter -- a missing
number is a failure, a low number is information.
"""
import sys
import xml.etree.ElementTree as ElementTree


def line_coverage(path: str) -> tuple[int, int]:
    """(covered, total) LINE counts from a Jacoco XML report."""
    root = ElementTree.parse(path).getroot()
    for counter in root.findall("counter"):
        if counter.get("type") == "LINE":
            missed = int(counter.get("missed"))
            covered = int(counter.get("covered"))
            return covered, covered + missed
    raise SystemExit(f"{path}: no LINE counter, so this report measures nothing a reader can use")


def sentence(covered: int, total: int) -> str:
    percent = 100.0 * covered / total if total else 0.0
    return f"core line coverage: {percent:.1f}% ({covered} of {total} lines)"


def main(argv: list[str]) -> int:
    if len(argv) < 2:
        print(__doc__)
        return 2
    covered, total = line_coverage(argv[1])
    text = sentence(covered, total)
    print(text)
    if len(argv) > 2:
        with open(argv[2], "a", encoding="utf-8") as summary:
            summary.write("### Coverage (reported, not gated)\n" + text + "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
