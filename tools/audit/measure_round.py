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
laundering a judgement as a measurement; the judgement lived in the audit documents beside the
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
        # A path outside the repository has no relative form, and a reader still needs to see which one was
        # asked for -- the first version of this raised `ValueError` on exactly that.
        try:
            named = results.relative_to(ROOT)
        except ValueError:
            named = results
        return {"measured": False, "why": f"no results at {named}"}
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


OR_TRUE = re.compile(r"\|\|\s*true")


def or_true_count(text: str) -> int:
    """How many workflow lines swallow a failure with an or-true suffix.

    Comment lines do not count. Three of these were removed from the asset workflow on 2026-09-18 (roadmap I3)
    and each removal left a comment explaining what the suffix had been hiding, so a counter that read prose
    would have kept reporting three swallowed failures in a workflow with none -- the same mistake M1 fixed for
    the content flags, in the other direction. A metric that cannot tell a failure from a sentence about a
    failure is a metric nobody can act on.
    """
    return sum(1 for line in text.splitlines()
               if OR_TRUE.search(line) and not line.strip().startswith("#"))


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
    found["workflow_always_true"] = sum(or_true_count(read(path)) for path in workflows)
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


BLOCK_COMMENT = re.compile(r"/\*.*?\*/", re.S)
LINE_COMMENT = re.compile(r"//[^\n]*")


def strip_comments(text: str) -> str:
    """The same file with its comments blanked out, line numbering and length preserved.

    Blanked rather than removed: a comment's characters become spaces and its newlines stay newlines, so
    ``line 41`` still means line 41 afterwards. A ``//`` inside a string literal would blank the rest of that
    line, which can only cause a flag to under-report, and every flag now names the lines it matched, so an
    under-report is visible instead of silent.
    """
    def blank(match: re.Match) -> str:
        return "".join("\n" if char == "\n" else " " for char in match.group(0))

    return LINE_COMMENT.sub(blank, BLOCK_COMMENT.sub(blank, text))


def code_matches(sources: dict, pattern: str, limit: int = 8, per_file: int = 2) -> list:
    """``file:line`` for code lines matching ``pattern``, comments excluded.

    Two caps, and both are about which evidence a reader sees rather than about what counts. ``per_file`` stops
    one chatty file from filling the list: paths are scanned in sorted order, and without a per-file cap
    ``accessibility`` reported three mentions in a helper and four in the composer and never reached
    ``settings/GameSettings.java``, which is the field the player actually toggles. ``limit`` then keeps the
    printed list short enough to read.
    """
    found = []
    compiled = re.compile(pattern, re.I)
    for path in sorted(sources):
        hits = 0
        for number, line in enumerate(strip_comments(sources[path]).splitlines(), start=1):
            if compiled.search(line):
                found.append(f"{path.name}:{number}")
                hits += 1
                if hits >= per_file:
                    break
        if len(found) >= limit:
            return found[:limit]
    return found


def content_flags() -> dict:
    """Which features exist in the code, and which lines each claim rests on.

    These flags used to be keyword searches over whole files, javadoc included. That is how this tool came to
    report ``accessibility: true`` for a repository whose only match was the word "accessibility" in a
    ``GameSettings`` javadoc line and which has no accessibility feature at all -- no colour-blind palette, no
    screen-reader labels, no font size, no reduced-motion switch (roadmap item M1, from the 2026-09-18 audit).
    A flag satisfied by a sentence is not a flag about the game.

    So the search runs over code with comments stripped, and every flag is reported next to the lines that
    produced it. ``flagEvidence`` exists so a reader can check a claim rather than trust it, and so that a flag
    which is true for a weak reason says which reason: ``accessibility`` is now false, and a false flag with an
    empty evidence list is a truer sentence than a true one with a javadoc behind it.
    """
    main = ROOT / "core/src/main/java/com/amirrezahadipoor/herodefense"
    core = {path: read(path) for path in java_files("core/src/main/java")}
    android_paths = java_files("android/src/main/java") + java_files("android/src/androidTest/java")
    android = {path: read(path) for path in android_paths}
    everything = {**core, **android}

    patterns = {
        # A caught BACK key or an overridden Android callback: an API the platform calls, not a word.
        "backButton": r"Keys\.BACK|onBackPressed|setCatchKey|keycode\.BACK",
        # A locale the game can be set to, or Persian text in a string table.
        "persian": r'"fa"|fa-IR|PERSIAN|[\u0600-\u06FF]',
        # Mirroring is a call, not an aspiration.
        "rightToLeft": r"rightToLeft|mirrored|\bRTL\b",
        # A setting, a palette or a label a screen reader could read. None of these exist today.
        "accessibility": r"colourBlind|colorBlind|screenReader|fontSize|reducedMotion|highContrast",
        # A vibrator the platform exposes, reached from game code.
        "haptics": r"Vibrator|hapticFeedback|vibrate\(",
    }
    evidence = {name: code_matches(everything, pattern) for name, pattern in patterns.items()}
    flags = {name: bool(lines) for name, lines in evidence.items()}
    return {
        "packages": sorted(entry.name for entry in main.iterdir() if entry.is_dir()),
        "onboardingFiles": len(list((main / "onboarding").glob("*.java")))
        if (main / "onboarding").is_dir() else 0,
        "storyFiles": len(list((main / "story").glob("*.java"))) if (main / "story").is_dir() else 0,
        "equipmentFiles": len(list((main / "items").rglob("*.java"))) if (main / "items").is_dir() else 0,
        **flags,
        "flagEvidence": evidence,
    }


def docs_facts() -> dict:
    """How much documentation the repository carries.

    There used to be four counts of roadmap checkboxes here. The roadmap was deleted on 2026-09-18 at the
    owner's direction -- only its standing rules survive, as ``docs/RULES.md`` -- so what is left to measure is
    how many documents exist and how long the two a reader meets first are. A count of ticked boxes in a file
    that no longer exists would be a number with nothing behind it, which rule 3 of ``docs/RULES.md`` is about.
    """
    docs = sorted((ROOT / "docs").rglob("*.md"))
    return {
        "documents": len(docs),
        "rulesLines": len(read(ROOT / "docs/RULES.md").splitlines()),
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
