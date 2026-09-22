#!/usr/bin/env python3
"""Regenerate a rendered batch's review evidence from the batch that was just rendered (roadmap R5.6).

R5.6 asks that the review documents regenerate per batch rather than being written once by hand and left to rot.
In this repository the review document's *evidence* is the per-batch audit JSON plus its contact sheets: the
markdown beside them quotes those numbers, and the manifest binds each asset key to the document through
`categoryReview` (`record_category_review.py` writes that binding). This tool is the regeneration step: given
the batch that was just rendered, it runs the batch's own review generator with the committed tree as the
baseline and the freshly rendered tree as the candidate, and writes the sheets and the audit JSON into an
output directory that the workflow attaches to the run.

A batch with no review generator is an error rather than a silent skip: a render that cannot be reviewed is a
render nobody can accept, and the failure belongs at the point where the batch is named.

Usage:
    python3 tools/visual/regenerate_batch_review.py --batch enemies \\
        --candidate /tmp/hero-defense-runtime --output /tmp/hero-defense-review
    python3 tools/visual/regenerate_batch_review.py --list
"""
from __future__ import annotations

import argparse
import pathlib
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
VISUAL = ROOT / "tools" / "visual"

#: Every batch the render workflow accepts, and the review generator that owns its evidence.
BATCH_REVIEWS = {
    "arena": "create_arena_batch_review.py",
    "arena-cover": "create_arena_cover_review.py",
    "bosses": "create_boss_batch_review.py",
    "ceremony": "create_character_animation_review.py",
    "enemies": "create_enemy_batch_review.py",
    "equipment": "create_equipment_batch_review.py",
    "equipment-overlay": "create_equipment_overlay_review.py",
    "projectiles": "create_projectile_batch_review.py",
    "skill-icons": "create_ui_supplement_review.py",
    "ui": "create_ui_batch_review.py",
    "ui-supplement": "create_ui_supplement_review.py",
    "vfx": "create_vfx_batch_review.py",
    "world-tree": "create_world_tree_batch_review.py",
}


def regenerate(batch: str, candidate: pathlib.Path, output: pathlib.Path) -> int:
    generator = BATCH_REVIEWS[batch]
    script = VISUAL / generator
    if not script.is_file():
        print(f"batch '{batch}' names a review generator that does not exist: tools/visual/{generator}")
        return 1
    if not (candidate / "asset_manifest.json").is_file():
        print(f"candidate tree has no manifest: {candidate}/asset_manifest.json")
        return 1
    output.mkdir(parents=True, exist_ok=True)
    command = [
        sys.executable, str(script),
        str(ROOT / "android" / "assets" / "generated"),
        str(candidate),
        str(output),
    ]
    print("+ " + " ".join(command))
    result = subprocess.run(command, cwd=ROOT, check=False)
    return result.returncode


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--batch")
    parser.add_argument("--candidate", type=pathlib.Path)
    parser.add_argument("--output", type=pathlib.Path)
    parser.add_argument("--list", action="store_true")
    args = parser.parse_args()

    if args.list:
        for batch, generator in sorted(BATCH_REVIEWS.items()):
            exists = "ok" if (VISUAL / generator).is_file() else "MISSING"
            print(f"{batch:18s} {generator} ({exists})")
        return 0

    if not args.batch or not args.candidate or not args.output:
        parser.error("--batch, --candidate and --output are required")
    if args.batch not in BATCH_REVIEWS:
        print(f"unknown batch '{args.batch}'; known batches: {', '.join(sorted(BATCH_REVIEWS))}")
        return 1
    return regenerate(args.batch, args.candidate.resolve(), args.output.resolve())


if __name__ == "__main__":
    raise SystemExit(main())
