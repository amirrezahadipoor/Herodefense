"""Tests for the playtest ledger gate and the promotion path (roadmap R3.6).

Run from the repository root:

    python3 -m unittest discover -s tools/playtests/tests -t .

The gate is the only thing standing between the ledger and a hand-typed session, so it is tested the way the
repository tests everything else: a valid case, a negative case for every rule, and an end-to-end promotion.
"""

from __future__ import annotations

import json
import pathlib
import subprocess
import sys
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[3]
VALIDATOR = ROOT / "tools" / "playtests" / "validate_playtest_ledger.py"
PROMOTER = ROOT / "tools" / "playtests" / "promote_run_record.py"

sys.path.insert(0, str(ROOT / "tools" / "playtests"))
import validate_playtest_ledger as gate  # noqa: E402  (the test drives the real module, not a copy)


def a_run_record(**overrides) -> dict:
    record = {
        "schema": "herodefense.run-record/1",
        "source": "device",
        "platform": "Android",
        "appVersion": 1,
        "mode": "STANDARD",
        "ascensionTier": 0,
        "runSeed": 5206173254,
        "wavesCleared": 37,
        "runCompleted": False,
        "heroDied": True,
        "activeTrials": "",
        "recordedAtMillis": 1758000000000,
        "measurements": {"totalKills": "418", "defeatedBosses": "5"},
    }
    record.update(overrides)
    return record


def a_session(**overrides) -> dict:
    session = {
        "id": "session-2026-09-16-device-5206173254-w37",
        "date": "2026-09-16",
        "kind": "human",
        "player": "amirreza (owner)",
        "build": "0a987ae",
        "protocol": "docs/PLAYTEST_PROTOCOL.md",
        "durationMinutes": 22,
        "wavesReached": 37,
        "result": "defeat",
        "notes": "waves 1-12 read clearly; the wave-20 boss killed me in two hits",
    }
    session.update(overrides)
    return session


def a_finding(**overrides) -> dict:
    finding = {
        "id": "finding-boss-double-hit",
        "session": "session-2026-09-16-device-5206173254-w37",
        "date": "2026-09-16",
        "severity": "high",
        "summary": "the wave-20 boss killed the hero in two hits from full health",
        "evidence": "run record w37 with 0.0 damage taken on waves 1-19, then full health to zero in 3.4 s",
        "roadmapItem": "R4.1",
        "status": "open",
    }
    finding.update(overrides)
    return finding


class LedgerGateTest(unittest.TestCase):
    def setUp(self) -> None:
        self._temporary = tempfile.TemporaryDirectory()
        self.repository = pathlib.Path(self._temporary.name)
        (self.repository / "docs" / "playtests" / "records").mkdir(parents=True)
        (self.repository / "docs" / "ROADMAP_TO_1000.md").write_text(
            "## R3\n\n- [ ] **R3.6 Human playtest protocol**\n\n## R4\n\n- [ ] **R4.1 Threats that scale.**\n"
            "\n- [ ] **R5.2 Compose a runtime tier.**\n",
            encoding="utf-8",
        )
        self.write_sessions([])
        self.write_findings([])

    def tearDown(self) -> None:
        self._temporary.cleanup()

    def write_sessions(self, sessions):
        (self.repository / "docs" / "playtests" / "sessions.json").write_text(
            json.dumps({"sessions": sessions}, indent=2) + "\n", encoding="utf-8"
        )

    def write_findings(self, findings):
        (self.repository / "docs" / "playtests" / "findings.json").write_text(
            json.dumps({"findings": findings}, indent=2) + "\n", encoding="utf-8"
        )

    def problems(self):
        return gate.validate(self.repository)

    def test_an_empty_ledger_is_valid(self):
        self.assertEqual([], self.problems())

    def test_a_recorded_session_with_a_finding_that_cites_a_real_item_is_valid(self):
        self.write_sessions([a_session()])
        self.write_findings([a_finding()])
        self.assertEqual([], self.problems())

    def test_a_session_must_say_who_played_on_which_build(self):
        self.write_sessions([a_session(build="the build I had locally")])
        self.assertTrue(any("build must be a git hash" in problem for problem in self.problems()))

    def test_a_session_without_notes_is_not_a_session(self):
        self.write_sessions([a_session(notes="   ")])
        self.assertTrue(any("at least one line of notes" in problem for problem in self.problems()))

    def test_a_result_outside_the_vocabulary_is_rejected(self):
        self.write_sessions([a_session(result="went fine")])
        self.assertTrue(any("result must be one of" in problem for problem in self.problems()))

    def test_two_sessions_cannot_share_an_id(self):
        self.write_sessions([a_session(), a_session()])
        self.assertTrue(any("duplicate id" in problem for problem in self.problems()))

    def test_a_finding_must_cite_a_roadmap_item_that_exists(self):
        self.write_sessions([a_session()])
        self.write_findings([a_finding(roadmapItem="R9.9")])
        self.assertTrue(any("does not exist in the roadmap" in problem for problem in self.problems()))

    def test_a_finding_must_belong_to_a_recorded_session(self):
        self.write_sessions([a_session()])
        self.write_findings([a_finding(session="session-from-memory")])
        self.assertTrue(any("is not in sessions.json" in problem for problem in self.problems()))

    def test_a_fixed_finding_has_to_name_the_commit(self):
        self.write_sessions([a_session()])
        self.write_findings([a_finding(status="fixed", evidence="fixed it while I was in there")])
        self.assertTrue(any("must name the commit" in problem for problem in self.problems()))

        self.write_findings([a_finding(status="fixed", evidence="fixed in 5b78835 by lowering the boss burst")])
        self.assertEqual([], self.problems())

    def test_an_accepted_finding_has_to_say_why_it_was_accepted(self):
        self.write_sessions([a_session()])
        self.write_findings([a_finding(status="accepted", evidence="left as is")])
        self.assertTrue(any("why it was accepted" in problem for problem in self.problems()))

        self.write_findings(
            [a_finding(status="accepted", evidence="accepted: the boss is meant to punish a no-potion run")]
        )
        self.assertEqual([], self.problems())

    def test_a_session_whose_record_disagrees_with_it_is_rejected(self):
        self.write_sessions([a_session(record="docs/playtests/records/session.json", wavesReached=61)])
        (self.repository / "docs" / "playtests" / "records" / "session.json").write_text(
            json.dumps(a_run_record()), encoding="utf-8"
        )
        self.assertTrue(any("disagrees with its record" in problem for problem in self.problems()))

    def test_a_missing_record_file_is_rejected(self):
        self.write_sessions([a_session(record="docs/playtests/records/nope.json")])
        self.assertTrue(any("does not exist" in problem for problem in self.problems()))

    def test_the_command_line_reports_a_broken_ledger_and_exits_non_zero(self):
        self.write_sessions([a_session(kind="vibes")])
        completed = subprocess.run(
            [sys.executable, str(VALIDATOR), "--repository", str(self.repository)],
            capture_output=True, text=True, check=False,
        )
        self.assertEqual(1, completed.returncode)
        self.assertIn("kind must be one of", completed.stdout)


class PromotionTest(unittest.TestCase):
    """The path a session takes from the game to the ledger, end to end."""

    def setUp(self) -> None:
        self._temporary = tempfile.TemporaryDirectory()
        self.repository = pathlib.Path(self._temporary.name)
        (self.repository / "docs" / "playtests").mkdir(parents=True)
        (self.repository / "docs" / "ROADMAP_TO_1000.md").write_text(
            "- [ ] **R3.6 Human playtest protocol**\n\n- [ ] **R4.1 Threats that scale.**\n", encoding="utf-8"
        )
        (self.repository / "docs" / "playtests" / "sessions.json").write_text(
            json.dumps({"sessions": []}, indent=2) + "\n", encoding="utf-8"
        )
        (self.repository / "docs" / "playtests" / "findings.json").write_text(
            json.dumps({"findings": []}, indent=2) + "\n", encoding="utf-8"
        )
        self.record = self.repository / "run-1758000000000-w37-died.json"
        self.record.write_text(json.dumps(a_run_record(), indent=2) + "\n", encoding="utf-8")

    def tearDown(self) -> None:
        self._temporary.cleanup()

    def promote(self, *extra):
        return subprocess.run(
            [
                sys.executable, str(PROMOTER),
                "--record", str(self.record),
                "--player", "amirreza (owner)",
                "--build", "0a987ae",
                "--duration", "22",
                "--date", "2026-09-16",
                "--notes", "waves 1-12 read clearly",
                "--ledger", str(self.repository / "docs" / "playtests" / "sessions.json"),
                "--records", str(self.repository / "docs" / "playtests" / "records"),
                *extra,
            ],
            capture_output=True, text=True, check=False,
        )

    def test_promotion_files_the_evidence_and_validates(self):
        completed = self.promote()
        self.assertEqual(0, completed.returncode, completed.stderr)

        ledger = json.loads((self.repository / "docs" / "playtests" / "sessions.json").read_text())
        self.assertEqual(1, len(ledger["sessions"]))
        session = ledger["sessions"][0]
        self.assertEqual("defeat", session["result"], "the record says the hero died")
        self.assertEqual(37, session["wavesReached"], "the wave count comes from the record, not from the caller")
        self.assertEqual("human", session["kind"], "a device record is a human session")
        self.assertEqual(5206173254, session["seed"])

        filed = self.repository / session["record"]
        self.assertTrue(filed.is_file(), f"the raw record must be filed as evidence: {session['record']}")
        self.assertEqual(a_run_record(), json.loads(filed.read_text()))

        checked = subprocess.run(
            [sys.executable, str(VALIDATOR), "--repository", str(self.repository)],
            capture_output=True, text=True, check=False,
        )
        self.assertEqual(0, checked.returncode, checked.stdout)

    def test_a_simulator_record_promotes_as_an_automated_session(self):
        simulator = self.repository / "simulator.json"
        simulator.write_text(
            json.dumps(a_run_record(source="simulator", platform="headless", runCompleted=True, heroDied=False)),
            encoding="utf-8",
        )
        self.record = simulator
        completed = self.promote()
        self.assertEqual(0, completed.returncode, completed.stderr)

        session = json.loads((self.repository / "docs" / "playtests" / "sessions.json").read_text())["sessions"][0]
        self.assertEqual("automated", session["kind"])
        self.assertEqual("victory", session["result"], "a completed run is a victory, not a defeat")

    def test_the_same_session_cannot_be_promoted_twice(self):
        self.assertEqual(0, self.promote().returncode)
        again = self.promote()
        self.assertEqual(2, again.returncode)
        self.assertIn("already in the ledger", again.stderr)

    def test_a_file_that_is_not_a_run_record_is_refused(self):
        self.record.write_text(json.dumps({"notes": "I think I reached wave 40"}), encoding="utf-8")
        completed = self.promote()
        self.assertEqual(2, completed.returncode)
        self.assertIn("schema", completed.stderr)


if __name__ == "__main__":
    unittest.main()
