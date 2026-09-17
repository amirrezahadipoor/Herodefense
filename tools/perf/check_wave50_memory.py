#!/usr/bin/env python3
"""Read the wave-50 residency measurement out of an emulator logcat capture and check it (roadmap R8.3).

The instrumented test `WaveFiftyMemoryTest` runs a saved run at wave 50, asks the device itself
(`dumpsys meminfo`) what the process holds, and logs one line per measurement:

    HERODEFENSE_PERF wave=50 totalPssKb=284112 totalRssKb=402880 graphicsKb=98120

This script is the gate: it parses those lines, compares the PSS against
`docs/perf/wave50_memory_budget.json`, and exits 2 when the capture holds no measurement at all — a
measurement that did not happen is neither a pass nor a failure, and reporting it as a pass would be a lie.

Usage:
    python3 tools/perf/check_wave50_memory.py <logcat> [--budget docs/perf/wave50_memory_budget.json]
"""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
DEFAULT_BUDGET = ROOT / "docs" / "perf" / "wave50_memory_budget.json"
MARKER = "HERODEFENSE_PERF"
FIELD = re.compile(r"(\w+)=([\d,]+)")


def measurements(text: str) -> list[dict]:
    """Every measurement line in the capture, as dictionaries of integers."""
    found = []
    for line in text.splitlines():
        if MARKER not in line:
            continue
        values = {name: int(number.replace(",", "")) for name, number in FIELD.findall(line)}
        if "totalPssKb" in values:
            found.append(values)
    return found


def problems(measured: dict, budget: dict) -> list[str]:
    thresholds = budget["thresholds"]
    issues = []
    pss_kib = measured["totalPssKb"]
    if pss_kib > thresholds["totalPssKib"]:
        issues.append(
            f"process PSS at wave 50 is {pss_kib} KiB, over the {thresholds['totalPssKib']} KiB budget"
        )
    graphics = measured.get("graphicsKb")
    if graphics is not None and graphics > thresholds["graphicsKib"]:
        issues.append(
            f"graphics memory at wave 50 is {graphics} KiB, over the {thresholds['graphicsKib']} KiB budget"
        )
    return issues


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("log", type=pathlib.Path)
    parser.add_argument("--budget", type=pathlib.Path, default=DEFAULT_BUDGET)
    args = parser.parse_args()

    if not args.log.is_file():
        print(f"no logcat capture at {args.log}")
        return 2
    budget = json.loads(args.budget.read_text())
    found = measurements(args.log.read_text(errors="replace"))
    if not found:
        print("WAVE-50 RESIDENCY NOT MEASURED: the capture holds no "
              f"{MARKER} line")
        print("The instrumented test did not run, or it did not reach wave 50. That is a missing measurement.")
        return 2
    latest = found[-1]
    limit = budget["thresholds"]["totalPssKib"]
    print(f"wave-50 residency: PSS {latest['totalPssKb']} KiB (budget {limit} KiB), "
          f"RSS {latest.get('totalRssKb', 0)} KiB, graphics {latest.get('graphicsKb', 0)} KiB")
    issues = problems(latest, budget)
    if issues:
        print("\nMEMORY BUDGET FAILED:")
        for issue in issues:
            print(f"  - {issue}")
        return 1
    print("memory budget respected")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
