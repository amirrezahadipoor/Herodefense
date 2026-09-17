#!/usr/bin/env python3
"""Every performance number in `docs/**` has to come from a logged run (roadmap R8.5).

The rule, in full:

  * `docs/perf/runs/*.json` are the logged runs. Each one records a command, a commit, a date and the
    metrics that command printed.
  * `docs/perf/PERFORMANCE.md` is generated from them by `render_performance_doc.py`.
  * Any other document that states a performance number has to carry its provenance inline, as one of:
      - `perf:<run-id>`      a measurement, resolved against `docs/perf/runs/`
      - `budget:<file>`      a committed threshold, resolved against `docs/perf/*budget*.json`
      - `code:<path>`        a constant in the source, resolved against the file existing under `core/src`
                             (or `android/src`); the number is a design constant, not a measurement
      - `env:<key>`          a fact about the machine, resolved against `docs/perf/ENVIRONMENT.md`
  * Three documents do not need any of this, each for a stated reason: see `EXEMPT` below.

A citation that resolves to nothing is a failure too, because it looks like provenance without being it.

Usage:
    python3 tools/perf/check_perf_provenance.py
    python3 tools/perf/check_perf_provenance.py --list
"""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
DOCS = ROOT / "docs"
RUNS = DOCS / "perf" / "runs"
PERF_DIR = DOCS / "perf"
ENVIRONMENT = PERF_DIR / "ENVIRONMENT.md"

#: Documents (or directories) allowed to state numbers without a citation, each with its reason. An
#: exemption that cannot be explained is a hole in the rule.
EXEMPT = {
    "ROADMAP_TO_1000.md": "the plan and the work log: it quotes the audit and names target budgets",
    "CRITICAL_REVIEW_2026-09-13.md": "the dated review whose numbers are the finding being fixed",
    "audit/": "the 2026-09-16 audit: a dated record of what the repository measured that day, kept as found",
    "art_reviews/": "per-batch review records: dated summaries of one render batch, backed by that batch's "
                    "tools, its review sheet and docs/asset_hashes.json rather than by a run in docs/perf",
}

#: A performance number: a quantity followed by the unit it is measured in.
NUMBER = re.compile(r"(?<![\w.])(\d+(?:\.\d+)?)\s*(MiB|MB|KiB|KB|GB|ms|fps|MB/s|bytes)\b")
CITATION = re.compile(r"`(perf|budget|code|env):([^`]+)`")
ENV_KEY = re.compile(r"^\|\s*`([a-z0-9\-]+)`\s*\|")


def run_ids() -> set[str]:
    return {path.stem for path in RUNS.glob("*.json")} if RUNS.is_dir() else set()


def budget_files() -> set[str]:
    return {path.name for path in PERF_DIR.glob("*budget*.json")}


def env_keys() -> set[str]:
    if not ENVIRONMENT.is_file():
        return set()
    keys = set()
    for line in ENVIRONMENT.read_text().splitlines():
        match = ENV_KEY.match(line)
        if match:
            keys.add(match.group(1))
    return keys


def source_exists(path: str) -> bool:
    for root in ("core/src", "android/src"):
        if (ROOT / root).joinpath(path).is_file():
            return True
    return False


def problems() -> list[str]:
    known = {"perf": run_ids(), "budget": budget_files(), "env": env_keys()}
    found: list[str] = []
    for path in sorted(DOCS.rglob("*.md")):
        relative = path.relative_to(DOCS).as_posix()
        if relative.startswith("perf/"):
            continue
        exempt = path.name in EXEMPT or any(
            relative.startswith(prefix) for prefix in EXEMPT if prefix.endswith("/")
        )
        for number, line in enumerate(path.read_text().splitlines(), start=1):
            citations = CITATION.findall(line)
            if not exempt and NUMBER.search(line) and not citations:
                found.append(
                    f"docs/{relative}:{number}: uncited performance number: {line.strip()[:120]}"
                )
                continue
            for kind, reference in citations:
                if kind == "code":
                    if not source_exists(reference):
                        found.append(
                            f"docs/{relative}:{number}: cites code:{reference}, which does not exist"
                        )
                elif reference not in known[kind]:
                    found.append(
                        f"docs/{relative}:{number}: cites {kind}:{reference}, which is not recorded"
                    )
    return found


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--list", action="store_true", help="show what the citations resolve against")
    args = parser.parse_args()

    if args.list:
        print("logged runs:")
        for run_id in sorted(run_ids()):
            run = json.loads((RUNS / f"{run_id}.json").read_text())
            print(f"  perf:{run_id}   ({run['commit']}, {len(run['metrics'])} metrics)")
        print("budgets:")
        for name in sorted(budget_files()):
            print(f"  budget:{name}")
        print("environment facts:")
        for key in sorted(env_keys()):
            print(f"  env:{key}")
        print("code constants: any file under core/src or android/src, as `code:<path>`")
        print("exempt:")
        for name, reason in EXEMPT.items():
            print(f"  {name}: {reason}")
        return 0

    found = problems()
    if found:
        print(f"{len(found)} performance number(s) with no logged run behind them:\n")
        for problem in found:
            print("  " + problem)
        print("\nLog the measurement (tools/perf/log_run.py) and cite it as `perf:<id>`, or state the")
        print("constant it comes from as `code:<path>`, or reword the line so it is not a measurement.")
        return 1
    print("every performance number in docs/ traces to a logged run, a budget or a named constant")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
