#!/usr/bin/env python3
"""Measure an APK against the committed budget and print a summary (roadmap R8.4).

Reads the APK as a zip (no Android tooling needed), measures the whole file, its `assets/` payload, the
per-ABI native libraries, and the size of the largest entries, then compares each against
`docs/perf/apk_budget.json`. Exits non-zero the moment a threshold is crossed, so the build cannot grow
past the budget without somebody changing the budget on purpose.

Usage:
    python3 tools/perf/check_apk_budget.py android/build/outputs/apk/debug/android-debug.apk
    python3 tools/perf/check_apk_budget.py <apk> --budget docs/perf/apk_budget.json --summary-summary
"""
from __future__ import annotations

import argparse
import json
import pathlib
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[2]
DEFAULT_BUDGET = ROOT / "docs" / "perf" / "apk_budget.json"


def measure(apk: pathlib.Path) -> dict:
    """Everything the budget can be compared against, measured from the archive itself."""
    with zipfile.ZipFile(apk) as archive:
        infos = [info for info in archive.infolist() if not info.is_dir()]
        total_compressed = sum(info.compress_size for info in infos)
        assets = sum(info.compress_size for info in infos if info.filename.startswith("assets/"))
        natives: dict[str, int] = {}
        for info in infos:
            if info.filename.startswith("lib/") and info.filename.endswith(".so"):
                abi = info.filename.split("/")[1]
                natives[abi] = natives.get(abi, 0) + info.compress_size
        largest = sorted(
            ((info.filename, info.compress_size) for info in infos),
            key=lambda pair: pair[1],
            reverse=True,
        )[:5]
    return {
        "fileBytes": apk.stat().st_size,
        "entriesCompressedBytes": total_compressed,
        "assetsCompressedBytes": assets,
        "nativeBytes": natives,
        "largestEntries": largest,
    }


def check(measured: dict, budget: dict) -> list[str]:
    """Every way the measurement breaks the budget, in plain language."""
    thresholds = budget["thresholds"]
    problems = []
    if measured["fileBytes"] > thresholds["apkBytes"]:
        problems.append(
            f"APK is {measured['fileBytes']} bytes over the {thresholds['apkBytes']}-byte budget"
        )
    if measured["assetsCompressedBytes"] > thresholds["assetsBytes"]:
        problems.append(
            f"assets payload is {measured['assetsCompressedBytes']} bytes over the "
            f"{thresholds['assetsBytes']}-byte budget"
        )
    for abi, per_abi in thresholds.get("nativeBytesPerAbi", {}).items():
        size = measured["nativeBytes"].get(abi, 0)
        if size > per_abi:
            problems.append(f"{abi} natives are {size} bytes over their {per_abi}-byte budget")
    expected_abis = set(thresholds.get("requiredAbis", []))
    missing = sorted(expected_abis - set(measured["nativeBytes"]))
    if missing:
        problems.append(f"APK is missing native libraries for: {', '.join(missing)}")
    return problems


def summary_lines(measured: dict, budget: dict) -> list[str]:
    thresholds = budget["thresholds"]
    lines = [
        "| Measurement | Value | Budget |",
        "|---|---:|---:|",
        f"| APK file | {measured['fileBytes']:,} bytes | {thresholds['apkBytes']:,} |",
        f"| assets payload (compressed) | {measured['assetsCompressedBytes']:,} bytes | "
        f"{thresholds['assetsBytes']:,} |",
    ]
    for abi, size in sorted(measured["nativeBytes"].items()):
        allowed = thresholds.get("nativeBytesPerAbi", {}).get(abi)
        lines.append(f"| natives {abi} | {size:,} bytes | {allowed:,} |" if allowed
                     else f"| natives {abi} | {size:,} bytes | - |")
    for name, size in measured["largestEntries"]:
        lines.append(f"| largest entry {name} | {size:,} bytes | - |")
    return lines


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("apk", type=pathlib.Path)
    parser.add_argument("--budget", type=pathlib.Path, default=DEFAULT_BUDGET)
    parser.add_argument("--markdown", action="store_true", help="print the table between markers")
    args = parser.parse_args()

    if not args.apk.is_file():
        print(f"no APK at {args.apk}")
        return 2
    budget = json.loads(args.budget.read_text())
    measured = measure(args.apk)
    lines = summary_lines(measured, budget)
    print("\n".join(lines))
    problems = check(measured, budget)
    if problems:
        print("\nBUDGET FAILED:")
        for problem in problems:
            print(f"  - {problem}")
        return 1
    print("\nbudget respected")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
