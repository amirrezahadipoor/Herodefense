#!/usr/bin/env python3
"""Log one measured performance run (roadmap R8.5).

A run is a JSON file under `docs/perf/runs/` holding what was measured, by which command, on which commit.
The page `docs/perf/PERFORMANCE.md` is generated from these files, so a number that is not in a run cannot
appear in the documentation.

Usage:
    python3 tools/perf/log_run.py --id 2026-09-17-residency --commit 3b58f4e \
        --command "./gradlew :core:test --tests *RuntimeResidencyTest" --note "..." \
        --metric decodedCatalogBytes=384872448:bytes --metric liveCombatResidencyMiB=95.9:MiB

    python3 tools/perf/log_run.py --list
"""
from __future__ import annotations

import argparse
import datetime as dt
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
RUNS = ROOT / "docs" / "perf" / "runs"
COMMIT = re.compile(r"^[0-9a-f]{7,40}$")
NAME = re.compile(r"^[a-z][A-Za-z0-9]*$")
#: The units a metric may carry. `dB` is here for the texture encoder, whose quality is a signal-to-noise
#: ratio in decibels; everything else is a size, a duration, a rate, a ratio or a plain count.
UNITS = ("bytes", "MiB", "MB", "ms", "s", "fps", "MB/s", "count", "percent", "dB")


def parse_metric(text: str) -> dict:
    """`name=value:unit` — a number and its unit, both required."""
    if "=" not in text or ":" not in text.rsplit("=", 1)[1]:
        raise argparse.ArgumentTypeError(f"expected name=value:unit, got {text!r}")
    name, rest = text.split("=", 1)
    value_text, unit = rest.rsplit(":", 1)
    if not NAME.match(name):
        raise argparse.ArgumentTypeError(f"metric name must be camelCase letters and digits: {name!r}")
    if unit not in UNITS:
        raise argparse.ArgumentTypeError(f"unit must be one of {', '.join(UNITS)}: {unit!r}")
    try:
        value = int(value_text) if unit in ("bytes", "count") else float(value_text)
    except ValueError as error:
        raise argparse.ArgumentTypeError(f"value is not a number: {value_text!r}") from error
    return {"name": name, "value": value, "unit": unit}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--id", help="stable id, e.g. 2026-09-17-residency-at-wave-50")
    parser.add_argument("--commit", help="the commit the measurement was taken on")
    parser.add_argument("--command", help="the command that produced the numbers")
    parser.add_argument("--note", default="", help="one line of context")
    parser.add_argument("--date", default=dt.date.today().isoformat())
    parser.add_argument("--metric", action="append", default=[], type=parse_metric)
    parser.add_argument("--list", action="store_true", help="list the logged runs and exit")
    args = parser.parse_args()

    if args.list:
        for path in sorted(RUNS.glob("*.json")):
            run = json.loads(path.read_text())
            metrics = ", ".join(f"{m['name']}={m['value']}{m['unit']}" for m in run["metrics"])
            print(f"{run['id']:44s} {run['date']} {run['commit']}  {metrics}")
        return 0

    for required in ("id", "commit", "command"):
        if not getattr(args, required):
            parser.error(f"--{required} is required when logging a run")
    if not COMMIT.match(args.commit):
        parser.error(f"--commit must be a hex sha, got {args.commit!r}")
    if not args.metric:
        parser.error("a run with no metrics logs nothing; pass --metric at least once")

    RUNS.mkdir(parents=True, exist_ok=True)
    path = RUNS / f"{args.id}.json"
    run = {
        "id": args.id,
        "date": args.date,
        "commit": args.commit,
        "command": args.command,
        "note": args.note,
        "metrics": args.metric,
    }
    path.write_text(json.dumps(run, indent=2) + "\n")
    print(f"logged {path.relative_to(ROOT)} ({len(args.metric)} metrics)")
    print("now run: python3 tools/perf/render_performance_doc.py --write")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
