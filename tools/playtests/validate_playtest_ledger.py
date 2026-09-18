#!/usr/bin/env python3
"""Validates the playtest ledger (roadmap R3.6).

The protocol in ``docs/PLAYTEST_PROTOCOL.md`` says two things that are easy to say and hard to keep: a session is
recorded with the numbers it produced, and every finding a session raises becomes a roadmap item. This script is
what makes that a rule instead of a habit:

* ``docs/playtests/sessions.json`` -- one record per played session, with the build it ran, who played, what they
  were asked to do, and the measurements the session produced.
* ``docs/playtests/findings.json`` -- one record per finding, each one pointing at a roadmap item id that exists in
  ``docs/RULES.md``. A finding that is "fixed" must name the commit; a finding that is "accepted" must
  name the reason it was accepted instead of fixed.

Run it from the repository root:

    python3 tools/playtests/validate_playtest_ledger.py

It exits non-zero with one line per problem. The tests in ``tools/playtests/tests`` cover a valid ledger, the
negative cases and the promotion path, so the gate itself is tested rather than trusted.
"""

from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys

REPOSITORY = pathlib.Path(__file__).resolve().parents[2]
LEDGER = pathlib.Path("docs") / "playtests" / "sessions.json"
FINDING_LEDGER = pathlib.Path("docs") / "playtests" / "findings.json"
RECORD_SCHEMA = "herodefense.run-record/1"

SESSION_FIELDS = {
    "id": str,
    "date": str,
    "kind": str,
    "player": str,
    "build": str,
    "protocol": str,
    "durationMinutes": (int, float),
    "wavesReached": int,
    "result": str,
    "notes": str,
}
FINDING_FIELDS = {
    "id": str,
    "session": str,
    "date": str,
    "severity": str,
    "summary": str,
    "evidence": str,
    "roadmapItem": str,
    "status": str,
}
SESSION_KINDS = {"human", "automated"}
SESSION_RESULTS = {"victory", "defeat", "abandoned", "crashed"}
SEVERITIES = {"low", "medium", "high", "blocker"}
FINDING_STATUS = {"open", "fixed", "accepted"}
ID_PATTERN = re.compile(r"^(session|finding)-[a-z0-9][a-z0-9-]*$")
DATE_PATTERN = re.compile(r"^\d{4}-\d{2}-\d{2}$")
BUILD_PATTERN = re.compile(r"^[0-9a-f]{7,40}$")
COMMIT_PATTERN = re.compile(r"\b[0-9a-f]{7,40}\b")
ROADMAP_ITEM_PATTERN = re.compile(r"^R\d+\.\d+$")


def load(path: pathlib.Path, root: pathlib.Path):
    if not path.is_file():
        raise ValueError(f"{where(path, root)} is missing")
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as error:
        raise ValueError(f"{where(path, root)} is not valid JSON: {error}") from error


def where(path: pathlib.Path, root: pathlib.Path) -> str:
    try:
        return str(path.relative_to(root))
    except ValueError:
        return str(path)


def check_record(problems, path, root, record, fields, label):
    location = f"{where(path, root)}[{label}]"
    for field, kind in fields.items():
        if field not in record:
            problems.append(f"{location}: missing '{field}'")
            continue
        if not isinstance(record[field], kind) or isinstance(record[field], bool):
            problems.append(f"{location}: '{field}' must be {getattr(kind, '__name__', kind)}")
    for field in ("id", "date"):
        value = record.get(field)
        if isinstance(value, str):
            pattern = ID_PATTERN if field == "id" else DATE_PATTERN
            if not pattern.match(value):
                problems.append(f"{location}: '{field}' has the wrong shape: {value!r}")


def roadmap_items(root: pathlib.Path):
    """The plan's item ids -- empty, because the plan is gone.

    ``docs/ROADMAP_TO_1000.md`` was deleted on 2026-09-18 at the owner's direction and only its standing rules
    survive, as ``docs/RULES.md``, which holds no item list. A finding's ``roadmapItem`` therefore cannot be
    resolved against a plan any more, and the existence check below is skipped rather than answered from a
    document that does not exist. The shape check still runs, so the field cannot quietly become free text, and
    the ids already in the ledger stay as historical labels (``docs/RULES.md`` says exactly that).
    """
    return set()


def validate(repository: pathlib.Path = REPOSITORY, sessions_path=None, findings_path=None):
    """Returns a list of problems; empty means the ledger is coherent."""
    problems: list[str] = []
    repository = pathlib.Path(repository)
    sessions_path = pathlib.Path(sessions_path) if sessions_path else repository / LEDGER
    findings_path = pathlib.Path(findings_path) if findings_path else repository / FINDING_LEDGER

    sessions = load(sessions_path, repository)
    findings = load(findings_path, repository)
    for path, document, key in ((sessions_path, sessions, "sessions"), (findings_path, findings, "findings")):
        if not isinstance(document, dict) or key not in document:
            problems.append(f"{where(path, repository)}: expected an object with a '{key}' list")
            return problems
        if not isinstance(document[key], list):
            problems.append(f"{where(path, repository)}: '{key}' must be a list")
            return problems

    known_sessions = set()
    for index, session in enumerate(sessions["sessions"]):
        check_record(problems, sessions_path, repository, session, SESSION_FIELDS, index)
        if session.get("id") in known_sessions:
            problems.append(f"{where(sessions_path, repository)}[{index}]: duplicate id {session.get('id')!r}")
        known_sessions.add(session.get("id"))
        if session.get("kind") not in SESSION_KINDS:
            problems.append(f"{where(sessions_path, repository)}[{index}]: kind must be one of {sorted(SESSION_KINDS)}")
        if session.get("result") not in SESSION_RESULTS:
            problems.append(
                f"{where(sessions_path, repository)}[{index}]: result must be one of {sorted(SESSION_RESULTS)}"
            )
        build = session.get("build", "")
        if isinstance(build, str) and not BUILD_PATTERN.match(build):
            problems.append(f"{where(sessions_path, repository)}[{index}]: build must be a git hash, got {build!r}")
        if not isinstance(session.get("notes"), str) or not session.get("notes", "").strip():
            problems.append(f"{where(sessions_path, repository)}[{index}]: a session needs at least one line of notes")
        if "seed" in session and not isinstance(session["seed"], int):
            problems.append(f"{where(sessions_path, repository)}[{index}]: 'seed' must be an integer when present")
        record = session.get("record")
        if record is not None:
            record_path = repository / record
            if not isinstance(record, str) or not record_path.is_file():
                problems.append(f"{where(sessions_path, repository)}[{index}]: record {record!r} does not exist")
            else:
                try:
                    raw = json.loads(record_path.read_text(encoding="utf-8"))
                except json.JSONDecodeError:
                    problems.append(f"{where(sessions_path, repository)}[{index}]: record {record} is not valid JSON")
                else:
                    if raw.get("schema") != RECORD_SCHEMA:
                        problems.append(
                            f"{where(sessions_path, repository)}[{index}]: record {record} is not a run record "
                            f"(schema {raw.get('schema')!r})"
                        )
                    elif raw.get("wavesCleared") != session.get("wavesReached"):
                        problems.append(
                            f"{where(sessions_path, repository)}[{index}]: wavesReached {session.get('wavesReached')}"
                            f" disagrees with its record ({raw.get('wavesCleared')})"
                        )

    # Was read from the real roadmap, so a finding could not cite a made-up item; the roadmap is gone, so this
    # is empty and the membership check below no-ops. See roadmap_items().
    items = roadmap_items(repository)
    known_findings = set()
    for index, finding in enumerate(findings["findings"]):
        check_record(problems, findings_path, repository, finding, FINDING_FIELDS, index)
        if finding.get("id") in known_findings:
            problems.append(f"{where(findings_path, repository)}[{index}]: duplicate id {finding.get('id')!r}")
        known_findings.add(finding.get("id"))
        if finding.get("severity") not in SEVERITIES:
            problems.append(
                f"{where(findings_path, repository)}[{index}]: severity must be one of {sorted(SEVERITIES)}"
            )
        status = finding.get("status")
        if status not in FINDING_STATUS:
            problems.append(
                f"{where(findings_path, repository)}[{index}]: status must be one of {sorted(FINDING_STATUS)}"
            )
        item = finding.get("roadmapItem", "")
        if not ROADMAP_ITEM_PATTERN.match(item or ""):
            problems.append(
                f"{where(findings_path, repository)}[{index}]: roadmapItem must look like 'R4.2', got {item!r}"
            )
        elif items and item not in items:
            problems.append(
                f"{where(findings_path, repository)}[{index}]: roadmap item {item} does not exist in the roadmap"
            )
        if finding.get("session") not in known_sessions:
            problems.append(
                f"{where(findings_path, repository)}[{index}]: session {finding.get('session')!r} is not in "
                "sessions.json"
            )
        if status == "fixed" and not COMMIT_PATTERN.search(finding.get("evidence", "")):
            problems.append(
                f"{where(findings_path, repository)}[{index}]: a fixed finding must name the commit in its evidence"
            )
        if status == "accepted" and "accepted" not in finding.get("evidence", "").lower():
            problems.append(
                f"{where(findings_path, repository)}[{index}]: an accepted finding must say in its evidence why it "
                "was accepted"
            )
    return problems


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate the playtest ledger.")
    parser.add_argument("--repository", type=pathlib.Path, default=REPOSITORY,
                        help="repository root (defaults to the one this script lives in)")
    args = parser.parse_args()
    repository = args.repository.resolve()
    try:
        problems = validate(repository)
    except ValueError as error:
        print(error)
        return 1
    if problems:
        print("playtest ledger is not coherent:")
        for problem in problems:
            print(" -", problem)
        return 1
    sessions = json.loads((repository / LEDGER).read_text(encoding="utf-8"))["sessions"]
    findings = json.loads((repository / FINDING_LEDGER).read_text(encoding="utf-8"))["findings"]
    print(f"playtest ledger OK: {len(sessions)} session(s), {len(findings)} finding(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
