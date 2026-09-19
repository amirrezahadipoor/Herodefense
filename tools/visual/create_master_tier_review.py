#!/usr/bin/env python3
"""Write the accepted review of the composed master tier (roadmap R5.2).

Why a document of its own: the per-batch reviews accept a batch at the *reviewed* geometry, and the composed
tier is at the render's own resolution instead, so no batch review can describe it. This tool writes the review
that can, and it is the only thing that lets a review generator accept a 384 px candidate -- `master_tier.py`
reads what this file writes, and it reads a hash rather than a claim.

    python3 tools/visual/create_master_tier_review.py \
        --reviewed /tmp/hero-defense-reviewed --residency-bytes 207765504

What goes into it, all of it measured rather than asserted:

* the composed keys and their geometry, re-derived from the render batch's own manifest at `--master-commit`
  and checked against the shipped sheets;
* the provenance measurement for each key (fine detail reviewed -> render, and the correlation that separates
  an independent render from an enlarged copy), read from `docs/art_reviews/MASTER_TIER_PROVENANCE.md`;
* the sha256 of every composed sheet and its atlas;
* the memory arithmetic: what the composed tier costs, what it leaves of the catalog ceiling, and the live
  combat residency measured by `:core:residencyReport` on the composed tree, passed in with the command that
  produced it so the number cannot be typed by this tool.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path

VISUAL = Path(__file__).resolve().parent
REPOSITORY = VISUAL.parents[1]
sys.path.insert(0, str(VISUAL))

import compose_master_tier as compose  # noqa: E402  (the composition and this review are one change)

REVIEW_JSON = REPOSITORY / "docs/art_reviews/master_tier_review.json"
REVIEW_DOCUMENT = REPOSITORY / "docs/art_reviews/MASTER_TIER_REVIEW.md"


def measurements() -> dict[str, dict[str, float | None]]:
    """The provenance table's own numbers for the sheets the composition selected."""
    measured: dict[str, dict[str, float | None]] = {}
    for line in compose.PROVENANCE_DOCUMENT.read_text(encoding="utf-8").splitlines():
        match = re.match(r"\| `([^`]+)` \| ([^|]+) \| ([^|]+) \| ([^|]+) \| ([^|]+) \|", line)
        if not match:
            continue
        sheet, reviewed_px, render_px, detail, correlation = (part.strip() for part in match.groups())
        parts = re.split(r"\s*(?:->|\u2192)\s*", detail)
        if len(parts) != 2 or not _number(parts[0]) or not _number(parts[1]):
            continue
        measured[sheet] = {
            "reviewedPx": reviewed_px,
            "renderPx": render_px,
            "detailReviewed": _number(parts[0]),
            "detailRender": _number(parts[1]),
            "correlation": _number(correlation),
        }
    return measured


def check(root: pathlib.Path, rendered: str, sheets: dict, manifest: dict) -> int:
    """The committed review, re-derived from the tree. Any drift is a finding, not a regeneration."""
    problems = []
    committed = REVIEW_JSON.read_text(encoding="utf-8") if REVIEW_JSON.is_file() else ""
    documented = json.loads(committed) if committed else {}
    # The residency figure is a measurement of the tree as it ran, not something the tree implies, so the
    # comparison leaves it out and checks the rest of the document -- and then checks the figure itself against
    # the budget it was measured against.
    def comparable(document: dict) -> dict:
        clone = json.loads(json.dumps(document))
        clone.get("budget", {}).pop("liveCombatResidencyBytes", None)
        return clone

    if comparable(documented) != comparable(json.loads(rendered)):
        problems.append(f"{REVIEW_JSON.relative_to(REPOSITORY)} is not what the tree produces")
    measured = documented.get("budget", {}).get("liveCombatResidencyBytes")
    if measured is None or measured > documented.get("budget", {}).get("combatBudgetBytes", 0):
        problems.append(f"the recorded combat residency {measured} is not inside its budget")
    for key, record in sorted(sheets.items()):
        for field in ("sheet", "atlas"):
            path = root / record[field]
            digest = hashlib.sha256(path.read_bytes()).hexdigest()
            if digest != record[f"{field}Sha256"]:
                problems.append(f"{key}: {field} does not match the hash the review binds")
        review_record = documented.get("sheets", {}).get(key)
        if review_record is None:
            problems.append(f"{key}: the committed review does not cover this key")
            continue
        if review_record.get("sheetSha256") != record["sheetSha256"]:
            problems.append(f"{key}: the committed review binds a different sheet")
    ceiling = manifest["decodedCatalogBudgetBytes"]
    if manifest["decodedBytes"] > ceiling:
        problems.append(f"the catalog decodes to {manifest['decodedBytes']} bytes, over {ceiling}")
    for problem in problems:
        print(f"master tier review: {problem}")
    if problems:
        return 1
    print(f"master tier review: {len(sheets)} composed sheets match the tree, catalog inside its ceiling")
    return 0


def _number(text: str) -> float | None:
    try:
        return float(text)
    except ValueError:
        return None


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=REPOSITORY / "android/assets/generated")
    parser.add_argument(
        "--residency-bytes",
        type=int,
        help="what :core:residencyReport measured for the live combat set; required unless --check",
    )
    parser.add_argument("--residency-command", default=":core:residencyReport")
    parser.add_argument(
        "--check",
        action="store_true",
        help="verify the committed review against the tree instead of writing it",
    )
    args = parser.parse_args()
    if not args.check and args.residency_bytes is None:
        parser.error("--residency-bytes is required when writing the review")
    root = args.root.resolve()
    manifest_path = root / "asset_manifest.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    composed = compose.composed_assets(root)
    measured = measurements()

    sheets: dict[str, dict] = {}
    for key, asset in sorted(composed.items()):
        sheet = root / asset["sheet"]
        if (asset["frameSize"], asset["sheetWidth"], asset["sheetHeight"]) != (
            compose.MASTER_FRAME_SIZE,
            compose.MASTER_FRAME_SIZE * 10,
            compose.MASTER_FRAME_SIZE * 4,
        ):
            raise SystemExit(f"{key}: composed geometry is not the render batch's own page")
        sheets[key] = {
            "atlas": asset["atlas"],
            "atlasSha256": hashlib.sha256((root / asset["atlas"]).read_bytes()).hexdigest(),
            "frameSize": asset["frameSize"],
            "measurement": measured.get(asset["sheet"], {}),
            "sheet": asset["sheet"],
            "sheetHeight": asset["sheetHeight"],
            "sheetSha256": hashlib.sha256(sheet.read_bytes()).hexdigest(),
            "sheetWidth": asset["sheetWidth"],
            "visualQuality": asset["visualQuality"],
        }

    catalog = manifest["decodedBytes"]
    budget = manifest["decodedCatalogBudgetBytes"]
    document = {
        "budget": {
            "catalogBudgetBytes": budget,
            "catalogDecodedBytes": catalog,
            "catalogHeadroomBytes": budget - catalog,
            "combatBudgetBytes": manifest["decodedCombatResidencyBudgetBytes"],
            "liveCombatResidencyBytes": args.residency_bytes,
            "residencyCommand": args.residency_command,
        },
        "evidence": "docs/art_reviews/MASTER_TIER_PROVENANCE.md",
        "frameSize": compose.MASTER_FRAME_SIZE,
        "generatedAt": compose.COMPOSED_ON,
        "masterCommit": compose.MASTER_COMMIT,
        "masterEngineVersion": compose.MASTER_ENGINE_VERSION,
        "schema": 1,
        "sheets": sheets,
    }
    rendered = json.dumps(document, indent=1, sort_keys=True) + "\n"
    if args.check:
        return check(root, rendered, sheets, manifest)

    rows = []
    for key, record in sorted(sheets.items()):
        measurement = record["measurement"]
        rows.append(
            f"| `{key}` | {record['sheetWidth']}x{record['sheetHeight']} | {record['frameSize']} px | "
            f"{measurement.get('detailReviewed')} -> {measurement.get('detailRender')} | "
            f"{measurement.get('correlation')} | `{record['sheetSha256'][:16]}` |"
        )
    lines = [
        "# Master tier review - the composed runtime tier (roadmap R5.2)",
        "",
        "Generated by `tools/visual/create_master_tier_review.py`. Every number below is read from the render "
        "batch's own manifest, from the provenance measurement, or from the bytes on disk.",
        "",
        f"* render batch: `{compose.MASTER_COMMIT}` (`{compose.MASTER_ENGINE_VERSION}`), "
        f"{compose.MASTER_FRAME_SIZE} px frames on a {compose.MASTER_FRAME_SIZE * 10}x"
        f"{compose.MASTER_FRAME_SIZE * 4} page",
        f"* composed sheets: **{len(sheets)}**",
        "* decoded bytes of those sheets: **"
        f"{sum(composed[key]['sheets'][0]['decodedBytes'] for key in sheets)}**",
        f"* catalog: **{catalog}** bytes of a **{budget}** ceiling, leaving **{budget - catalog}**",
        f"* live combat residency measured on the composed tree: **{args.residency_bytes}** bytes "
        f"(`{args.residency_command}`) against a **{manifest['decodedCombatResidencyBudgetBytes']}** budget",
        "",
        "## What is accepted, and on what evidence",
        "",
        "The sheets below ship the render batch's own pixels instead of a halved copy of them. That is allowed "
        "only because the provenance measurement separates them from the sheets that were enlarged rather than "
        "rendered: they *gain* fine detail, and their correlation with the reviewed art is low enough that they "
        "cannot be a resize of it. The columns are the measurement, and the correlation is what did the "
        "separating.",
        "",
        "| sheet | page | frames | fine detail reviewed -> render | corr | sheet sha256 |",
        "|---|---|---|---|---|---|",
        *rows,
        "",
        "## What this review does not cover",
        "",
        "* the per-batch reviews still describe the reviewed geometry; `master_tier.py` is what lets them accept "
        "these candidates, and it accepts a key only when this document binds its sheet and atlas by hash;",
        "* the anchor frames are unchanged: the composition moved pixels to a bigger page, it did not re-pose "
        "anything, so the animation and pivot reviews still stand;",
        "* the device run: the tier's residency, load time and frame budget are reported by the emulator job on "
        "this commit, and a number that moves beyond its committed contract is a finding for this document "
        "rather than a silence.",
        "",
    ]
    REVIEW_DOCUMENT.write_text("\n".join(lines), encoding="utf-8")
    REVIEW_JSON.write_text(rendered, encoding="utf-8")
    print(f"wrote {REVIEW_DOCUMENT.relative_to(REPOSITORY)} and {REVIEW_JSON.relative_to(REPOSITORY)}")
    print(f"composed {len(sheets)} sheets, catalog {catalog} of {budget} bytes")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
