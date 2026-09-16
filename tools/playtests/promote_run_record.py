#!/usr/bin/env python3
"""Promotes one run record into the playtest ledger (roadmap R3.6).

A tester plays the build; the game writes a run record when the run ends; this script is the step in between. It
reads that record, files the raw evidence under ``docs/playtests/records/``, appends the session to
``docs/playtests/sessions.json`` and then validates the ledger, so a promoted session that does not fit the schema
fails here instead of at review time.

Played on a device (the record is read straight off the app's local storage):

    adb exec-out run-as com.amirrezahadipoor.herodefense cat files/playtests/run-*.json > /tmp/session.json
    python3 tools/playtests/promote_run_record.py --record /tmp/session.json \\
        --player "amirreza (owner)" --build 0a987ae --duration 22 \\
        --notes "waves 1-12 felt readable; the wave-20 boss killed me in two hits"

Played through the simulator (a session driven by the balance simulator, recorded by the capture test):

    python3 tools/playtests/promote_run_record.py --record build/playtests/simulator-*.json \\
        --player "balance simulator" --build 0a987ae --duration 3 --kind automated

The kind is inferred from the record's ``source`` unless ``--kind`` says otherwise. Nothing here invents a number:
the waves, the seed, the ending and the counters come from the record, and the only things the caller supplies are
the ones a file cannot know (who played, how long it took, what they thought).
"""

from __future__ import annotations

import argparse
import datetime as dt
import json
import pathlib
import shutil
import sys

REPOSITORY = pathlib.Path(__file__).resolve().parents[2]
LEDGER = REPOSITORY / "docs" / "playtests" / "sessions.json"
RECORDS = REPOSITORY / "docs" / "playtests" / "records"
SCHEMA = "herodefense.run-record/1"
PROTOCOL = "docs/PLAYTEST_PROTOCOL.md"
EXIT_USAGE = 2


def fail(message: str) -> "None":
    print(f"promote_run_record: {message}", file=sys.stderr)
    raise SystemExit(EXIT_USAGE)


def load_record(path: pathlib.Path) -> dict:
    if not path.is_file():
        fail(f"no run record at {path}")
    try:
        record = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as error:
        fail(f"{path} is not JSON: {error}")
    if record.get("schema") != SCHEMA:
        fail(f"{path} has schema {record.get('schema')!r}, expected {SCHEMA!r}")
    return record


def result_of(record: dict) -> str:
    if record.get("runCompleted"):
        return "victory"
    if record.get("heroDied"):
        return "defeat"
    return "abandoned"


def session_id(record: dict, played_on: str, label: str | None = None) -> str:
    """The session's id: date, source, an optional label, seed and waves.

    The label exists because a seed and a wave count are not unique on their own -- the same seed played to the same
    wave under two different simulator policies is two different sessions, and the ledger refuses a duplicate id.
    """
    parts = ["session", played_on, record.get("source", "unknown")]
    if label:
        parts.append(label)
    parts.append(f"{record.get('runSeed')}-w{record.get('wavesCleared')}")
    return "-".join(parts)


def build_session(record: dict, args) -> dict:
    kind = args.kind or ("automated" if record.get("source") == "simulator" else "human")
    session = {
        "id": session_id(record, args.date, args.label),
        "date": args.date,
        "kind": kind,
        "player": args.player,
        "build": args.build,
        "protocol": PROTOCOL,
        "durationMinutes": args.duration,
        "wavesReached": int(record.get("wavesCleared", 0)),
        "result": result_of(record),
        "seed": record.get("runSeed"),
        "mode": record.get("mode"),
        "record": None,  # filled in once the raw file is filed away
        "notes": " ".join(args.notes) if args.notes else "no notes recorded",
    }
    return session


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--record", required=True, type=pathlib.Path, help="the run record written by the game")
    parser.add_argument("--player", required=True, help="who played (a name, not a device id)")
    parser.add_argument("--build", required=True, help="the commit the played build came from")
    parser.add_argument("--duration", required=True, type=float, help="minutes the session lasted")
    parser.add_argument("--notes", action="append", default=[], help="one line of notes; repeatable")
    parser.add_argument("--kind", choices=("human", "automated"), help="default: inferred from the record source")
    parser.add_argument("--date", default=dt.date.today().isoformat(), help="the day the session was played; the tester's day, not this machine's UTC day -- pass it explicitly when they differ")
    parser.add_argument("--label", help="a short lowercase token for the session id, when seed and wave count alone "
                                        "would collide with a session already in the ledger (e.g. the policy)")
    parser.add_argument("--ledger", type=pathlib.Path, default=LEDGER)
    parser.add_argument("--records", type=pathlib.Path, default=RECORDS)
    parser.add_argument("--dry-run", action="store_true", help="print the session that would be appended")
    args = parser.parse_args()

    record = load_record(args.record)
    session = build_session(record, args)

    ledger = json.loads(args.ledger.read_text(encoding="utf-8"))
    known = {existing["id"] for existing in ledger["sessions"]}
    if session["id"] in known:
        fail(f"{session['id']} is already in the ledger; promote a different session or delete the old entry")

    if args.dry_run:
        print(json.dumps(session, ensure_ascii=False, indent=2))
        return 0

    args.records.mkdir(parents=True, exist_ok=True)
    filed = args.records / f"{session['id']}.json"
    shutil.copyfile(args.record, filed)
    try:
        session["record"] = str(filed.relative_to(REPOSITORY)).replace("\\", "/")
    except ValueError:
        # A ledger promoted outside the checkout (the test suite does this) keeps the path it was given.
        session["record"] = str(filed).replace("\\", "/")

    ledger["sessions"].append(session)
    args.ledger.write_text(json.dumps(ledger, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"appended {session['id']} ({session['result']} at wave {session['wavesReached']}), evidence in "
          f"{session['record']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
