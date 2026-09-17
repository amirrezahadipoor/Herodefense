#!/usr/bin/env python3
"""Measure cold-start time from a logcat capture and compare it against the committed budget (roadmap R8.4).

Android's own `ActivityTaskManager` line is the measurement, not a stopwatch we started ourselves:

    ActivityTaskManager: Displayed com.amirrezahadipoor.herodefense/.android.AndroidLauncher: +1s234ms

`this time`/`total time` on the `am start -W` path are accepted as well, because both are written by the
platform and neither is self-reported by the app.

Usage:
    python3 tools/perf/parse_startup.py android/build/reports/androidTests/diagnostics/emulator-logcat.txt
    python3 tools/perf/parse_startup.py <log> --budget docs/perf/startup_budget.json
"""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
DEFAULT_BUDGET = ROOT / "docs" / "perf" / "startup_budget.json"

# Displayed com.pkg/.Activity: +1s234ms  (the modern form)
DISPLAYED = re.compile(r"Displayed\s+(?P<component>\S+):\s*\+(?P<seconds>\d+)s(?P<millis>\d+)ms")
# TotalTime: 1234  /  ThisTime: 1200  (written by `am start -W`)
AM_TOTAL = re.compile(r"TotalTime:\s*(?P<millis>\d+)")
AM_THIS = re.compile(r"ThisTime:\s*(?P<millis>\d+)")


def parse(text: str, component: str | None = None) -> list[tuple[str, int]]:
    """Every measurement in the capture, as (source, milliseconds), oldest first."""
    found: list[tuple[str, int]] = []
    for match in DISPLAYED.finditer(text):
        name = match.group("component")
        if component and component not in name:
            continue
        millis = int(match.group("seconds")) * 1000 + int(match.group("millis"))
        found.append(("Displayed " + name, millis))
    for match in AM_TOTAL.finditer(text):
        found.append(("TotalTime", int(match.group("millis"))))
    for match in AM_THIS.finditer(text):
        found.append(("ThisTime", int(match.group("millis"))))
    return found


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("log", type=pathlib.Path)
    parser.add_argument("--budget", type=pathlib.Path, default=DEFAULT_BUDGET)
    parser.add_argument("--component", default=None, help="only accept a component containing this text")
    args = parser.parse_args()

    if not args.log.is_file():
        print(f"no logcat capture at {args.log}")
        return 2
    budget = json.loads(args.budget.read_text())
    measurements = parse(args.log.read_text(errors="replace"), args.component)
    if not measurements:
        print("STARTUP NOT MEASURED: the capture holds no platform-displayed-start line")
        print(
            "The emulator boots the app from the instrumentation runner in some runs; a missing line is not "
            "a pass and not a failure, it is a missing measurement."
        )
        return 2
    source, millis = measurements[-1]
    limit = budget["thresholds"]["coldStartMillis"]
    print(f"cold start: {millis} ms via {source} (budget {limit} ms)")
    if millis > limit:
        print(f"STARTUP BUDGET FAILED: {millis} ms over {limit} ms")
        return 1
    print("startup budget respected")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
