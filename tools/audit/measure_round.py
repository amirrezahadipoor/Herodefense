#!/usr/bin/env python3
"""Measure this round the way the 2026-09-16 audit measured the last one, so the re-score cites numbers.

The audit that produced `docs/roadmap` scored nine in-scope categories 550 of 1,000 and named what each
deduction was for. Phase 97 is that audit run again on the finished round: same granularity, same rule that
no category passes on prose, and this time the figures have to be reproducible from the tree rather than
transcribed from a conversation. This tool is the measuring half -- it reads the repository, the test
results, the asset manifest and the filed performance runs, and prints every fact a category can be scored
against:

    python3 tools/audit/measure_round.py                     # the fact sheet, for a person to read
    python3 tools/audit/measure_round.py --json out.json      # the same, as numbers

What it deliberately does not do is score. A score is a judgement, and a tool that emitted one would be
laundering a judgement as a measurement; the judgement lives in `docs/audit/AUDIT_2026-09-17.md` beside the
figures, and each deduction there names the measurement it is about.
"""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import subprocess
import sys
import xml.etree.ElementTree as ElementTree

ROOT = pathlib.Path(__file__).resolve().parents[2]


def java_files(relative: str) -> list[pathlib.Path]:
    return sorted(path for path in (ROOT / relative).rglob("*.java"))


def read(path: pathlib.Path) -> str:
    return path.read_text(encoding="utf-8", errors="ignore")


def count_tests(results: pathlib.Path) -> dict:
    """Totals from the JUnit XML a real `:core:test` run leaves behind, not from a count of annotations."""
    total = {"classes": 0, "tests": 0, "failures": 0, "errors": 0, "skipped": 0, "seconds": 0.0}
    if not results.is_dir():
        return {"measured": False, "why": f"no results at {results.relative_to(ROOT)}"}
    for report in sorted(results.glob("TEST-*.xml")):
        root = ElementTree.parse(report).getroot()
        total["classes"] += 1
        total["tests"] += int(root.get("tests", 0))
        total["failures"] += int(root.get("failures", 0))
        total["errors"] += int(root.get("errors", 0))
        total["skipped"] += int(root.get("skipped", 0))
        total["seconds"] += float(root.get("time", 0.0))
    total["measured"] = True
    return total


def java_shape(path: pathlib.Path) -> dict:
    text = read(path)
    methods = len(re.findall(r"^    (?:public|private|protected|static|final|synchronized|@Override|void|int"
                             r"|boolean|float|double|long|String)[^\n;=]*\([^;]*\)\s*\{", text, re.M))
    fields = len(re.findall(r"^    (?:private|public|protected|final|static)[^\n;()]*;\s*$", text, re.M))
    return {"name": path.name, "lines": len(text.splitlines()), "methods": methods, "fields": fields}


def largest_classes() -> list[dict]:
    core = [java_shape(path) for path in java_files("core/src/main/java")
            if "package-info" not in path.name]
    core.sort(key=lambda shape: shape["lines"], reverse=True)
    return core[:5]


def integrity_scan() -> dict:
    """The scan `TestIntegrityTest` performs, reported as counts so the audit can quote them."""
    sources = list(java_files("core/src/test/java")) + list(java_files("android/src/androidTest/java"))
    patterns = {
        "vacuous": re.compile(r"assert(?:True|False|Equals)\s*\(\s*(?:true|false|null\s*,\s*null)\s*\)"),
        "disabled": re.compile(r"@(?:Disabled|Ignore)\b"),
        "softened": re.compile(r"//\s*relaxed\b"),
    }
    found = {key: 0 for key in patterns}
    found["exemptions"] = 0
    for path in sources:
        text = read(path)
        for key, pattern in patterns.items():
            found[key] += len(pattern.findall(text))
        found["exemptions"] += len(re.findall(r"//\s*integrity-exempt:\s*\S", text))
    workflows = sorted((ROOT / ".github/workflows").glob("*.yml"))
    found["workflow_always_true"] = sum(len(re.findall(r"\|\|\s*true", read(path))) for path in workflows)
    found["test_integrity_test"] = any("TestIntegrityTest" in path.name for path in sources)
    return found


def manifest_facts() -> dict:
    path = ROOT / "android/assets/generated/asset_manifest.json"
    if not path.is_file():
        return {}
    manifest = json.loads(read(path))
    facts = {
        "engineVersion": manifest.get("engineVersion"),
        "visualQuality": manifest.get("visualQuality"),
        "assetCount": len(manifest.get("assets", [])),
    }
    for key in ("decodedBytes", "decodedCatalogBudgetBytes", "maxAtlasPageSize",
                "liveCombatResidencyBytes", "decodedCombatResidencyBudgetBytes", "decodedCatalogBytes"):
        value = manifest.get(key)
        if value is not None:
            facts[key] = value
    return facts


def ledger_facts() -> dict:
    ledger = ROOT / "docs/asset_hashes.json"
    facts = {"entries": 0}
    if ledger.is_file():
        try:
            facts["entries"] = len(json.loads(read(ledger)).get("sheets", {}))
        except (ValueError, TypeError):
            facts["entries"] = "unreadable"
    facts["ledgerPolicy"] = json.loads(read(ledger)).get("policy") if ledger.is_file() else None
    facts["materialMaps"] = len(list((ROOT / "docs/materials").rglob("*.png"))) \
        if (ROOT / "docs/materials").is_dir() else 0
    return facts


def performance_runs() -> dict:
    """Every filed run as {id: {metric: value}}, plus the four the rubric's performance category cites."""
    runs: dict[str, dict] = {}
    for path in sorted((ROOT / "docs/perf/runs").glob("*.json")):
        payload = json.loads(read(path))
        metrics = {}
        for metric in payload.get("metrics", []):
            if isinstance(metric, dict) and "name" in metric:
                metrics[metric["name"]] = metric.get("value")
        runs[payload.get("id", path.stem)] = {
            "commit": payload.get("commit"),
            "metrics": metrics,
        }
    def find(*needles: str) -> dict | None:
        for name, payload in runs.items():
            if all(needle in name for needle in needles):
                return {"id": name, **payload}
        return None

    return {
        "runCount": len(runs),
        "ids": sorted(runs),
        "composedTier": find("composed-tier"),
        "wave50": find("wave50"),
        "coldStart": find("cold-start"),
        "apkSize": find("apk-size"),
    }


def content_flags() -> dict:
    """The presence of a feature is a fact; whether it is any good is the judge's problem, not the tool's."""
    main = ROOT / "core/src/main/java/com/amirrezahadipoor/herodefense"
    sources = {path: read(path) for path in java_files("core/src/main/java")}
    joined = "\n".join(sources.values())
    android = "\n".join(read(path) for path in
                        java_files("android/src/main/java") + java_files("android/src/androidTest/java"))
    return {
        "packages": sorted(entry.name for entry in main.iterdir() if entry.is_dir()),
        "onboardingFiles": len(list((main / "onboarding").glob("*.java")))
        if (main / "onboarding").is_dir() else 0,
        "storyFiles": len(list((main / "story").glob("*.java"))) if (main / "story").is_dir() else 0,
        "backButton": bool(re.search(r"Keys\.BACK|onBackPressed|keycode\.BACK",
                                     joined + android, re.I)),
        "persian": bool(re.search(r'"fa"|\bfa-IR\b|Locale\.fa|فارسی', joined + android)),
        "rightToLeft": bool(re.search(r"rightToLeft|\bRTL\b", joined + android)),
        "accessibility": bool(re.search(r"accessibil|colour[- ]?blind|colorBlind|screenReader",
                                        joined + android, re.I)),
        "haptics": bool(re.search(r"Vibration|haptic|vibrate", joined + android, re.I)),
        "equipmentFiles": len(list((ROOT / "core/src/main/java/com/amirrezahadipoor/herodefense/items")
                                   .rglob("*.java"))) if True else 0,
    }


def docs_facts() -> dict:
    docs = sorted((ROOT / "docs").rglob("*.md"))
    roadmap = read(ROOT / "docs/ROADMAP_TO_1000.md")
    return {
        "documents": len(docs),
        "roadmapLines": len(roadmap.splitlines()),
        "roadmapDone": len(re.findall(r"^- \[x\]", roadmap, re.M)),
        "roadmapPartial": len(re.findall(r"^- \[~\]", roadmap, re.M)),
        "roadmapOpen": len(re.findall(r"^- \[ \]", roadmap, re.M)),
        "audits": sorted(path.name for path in (ROOT / "docs/audit").glob("AUDIT_*.md")),
        "readmeLines": len(read(ROOT / "README.md").splitlines()),
    }


def asset_inventory() -> dict:
    assets = ROOT / "android/assets"
    inventory = {}
    for kind in ("png", "ktx", "ogg", "json", "frag", "vert", "ttf"):
        files = sorted(assets.rglob(f"*.{kind}"))
        inventory[kind] = {"count": len(files), "bytes": sum(path.stat().st_size for path in files)}
    inventory["totalBytes"] = sum(path.stat().st_size for path in assets.rglob("*") if path.is_file())
    audio = assets / "audio"
    inventory["audioFiles"] = len([path for path in audio.rglob("*") if path.is_file()])
    inventory["audioBytes"] = sum(path.stat().st_size for path in audio.rglob("*") if path.is_file())
    return inventory


def git_facts() -> dict:
    def git(*arguments: str) -> str:
        return subprocess.run(["git", "-C", str(ROOT), *arguments], capture_output=True,
                              text=True, check=False).stdout.strip()
    return {
        "head": git("rev-parse", "--short", "HEAD"),
        "commits": git("rev-list", "--count", "HEAD"),
        "first": git("log", "--reverse", "--format=%as", "-1"),
        "last": git("log", "-1", "--format=%as"),
        "trackedBytes": int(git("cat-file", "-s", "HEAD^{tree}") or 0),
    }


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--json", type=pathlib.Path, default=None, help="write the facts here as JSON")
    parser.add_argument("--results", type=pathlib.Path,
                        default=ROOT / "core/build/test-results/test",
                        help="the JUnit XML directory a real test run left behind")
    arguments = parser.parse_args(argv[1:])

    core_main = java_files("core/src/main/java")
    core_test = java_files("core/src/test/java")
    facts = {
        "git": git_facts(),
        "tree": {
            "coreMainFiles": len(core_main),
            "coreMainLines": sum(len(read(path).splitlines()) for path in core_main),
            "coreTestFiles": len(core_test),
            "coreTestLines": sum(len(read(path).splitlines()) for path in core_test),
            "androidFiles": len(java_files("android/src/main/java")),
            "androidTestFiles": len(java_files("android/src/androidTest/java")),
            "blenderTools": len(list((ROOT / "tools/blender").rglob("*.py"))),
            "visualTools": len(list((ROOT / "tools/visual").rglob("*.py"))),
            "largestClasses": largest_classes(),
        },
        "tests": count_tests(arguments.results),
        "integrity": integrity_scan(),
        "manifest": manifest_facts(),
        "ledger": ledger_facts(),
        "performance": performance_runs(),
        "content": content_flags(),
        "docs": docs_facts(),
        "assets": asset_inventory(),
    }

    text = json.dumps(facts, indent=2, sort_keys=True)
    if arguments.json:
        arguments.json.write_text(text + "\n")
        print(f"wrote {arguments.json}")
    else:
        print(text)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
