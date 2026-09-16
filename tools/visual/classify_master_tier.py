#!/usr/bin/env python3
"""Classify the Phase 76/77 "HD" batch against the last reviewed tier.

Background
----------
Phase 76 (`5374f2c`) replaced 100 sheets with a NEAREST resize of the reviewed artwork and called it
"studio-v5-hd-pbr". Phase 76-b (`6e5097d`) then pushed a saturation/contrast/bloom grade through *every*
PNG, which hides the pixel-block signature of a NEAREST resize: after the grade, the file is no longer
an exact block replication, so a naive "is it a 2x copy?" check no longer fires.

This tool answers the question that actually matters: **does a sheet carry detail that the reviewed
tier does not, or is it the reviewed tier, enlarged?**

Method (two independent, grading-robust measurements, both restricted to opaque pixels):

1. ``fine detail`` - RMS of the 1-pixel-step luminance gradient. Resampling cannot create fine detail;
   a genuine re-render at 2x resolution has *more* of it than the reviewed 1x artwork, an enlarged copy
   has the same or less (the grade's bloom smooths it further).
2. ``structure correlation`` - Pearson correlation between the candidate and the reviewed sheet upscaled
   to the candidate's size. A resampled copy stays correlated (~0.98+) even after a strong grade; an
   independent render of the same character drops well below that.

Both numbers are printed per asset, so the conclusion is checkable instead of asserted.

Usage::

    python3 tools/visual/classify_master_tier.py --write-report
"""
from __future__ import annotations

import argparse
import hashlib
import io
import json
import subprocess
from pathlib import Path

import numpy as np
from PIL import Image

REPOSITORY = Path(__file__).resolve().parents[2]
GENERATED = "android/assets/generated"
REVIEWED_COMMIT = "5374f2c^"
UPSCALE_COMMIT = "5374f2c"
GRADE_COMMIT = "6e5097d"
MASTER_COMMIT = "6449a6d"
REPORT = REPOSITORY / "docs/art_reviews/MASTER_TIER_PROVENANCE.md"

# Thresholds taken from the measured separation (see the report table):
# reviewed-tier resamples cluster at >= 0.98 correlation, independent renders at <= 0.94.
CORRELATION_RESAMPLE_FLOOR = 0.95


def git_bytes(commit: str, path: str) -> bytes | None:
    result = subprocess.run(
        ["git", "show", f"{commit}:{path}"],
        cwd=REPOSITORY,
        capture_output=True,
        check=False,
    )
    return result.stdout if result.returncode == 0 else None


def image(payload: bytes) -> Image.Image:
    return Image.open(io.BytesIO(payload)).convert("RGBA")


def fine_detail(array: np.ndarray, mask: np.ndarray) -> float | None:
    lum = array[..., :3].mean(axis=2)
    step = np.abs(lum[:, 1:] - lum[:, :-1])[:, :-1]
    usable = mask[:, 1:] & mask[:, :-1]
    usable = usable[:, : step.shape[1]]
    if usable.sum() < 100:
        return None
    return float(np.sqrt((step[usable] ** 2).mean()))


def structure_correlation(candidate: np.ndarray, reviewed: np.ndarray) -> float | None:
    if candidate.shape[0] % reviewed.shape[0] or candidate.shape[1] % reviewed.shape[1]:
        return None
    factor = candidate.shape[0] // reviewed.shape[0]
    if factor != candidate.shape[1] // reviewed.shape[1] or factor < 1:
        return None
    grown = reviewed if factor == 1 else np.kron(reviewed, np.ones((factor, factor, 1)))
    mask = candidate[..., 3] > 8
    if mask.sum() < 100:
        return None
    values = []
    for channel in range(3):
        x = grown[..., channel][mask].astype(np.float64)
        y = candidate[..., channel][mask].astype(np.float64)
        if x.std() < 1e-6 or y.std() < 1e-6:
            continue
        values.append(float(np.corrcoef(x, y)[0, 1]))
    return float(np.mean(values)) if values else None


def classify(path: str) -> dict:
    reviewed_payload = git_bytes(REVIEWED_COMMIT, f"{GENERATED}/{path}")
    claim_payload = git_bytes(MASTER_COMMIT, f"{GENERATED}/{path}")
    if reviewed_payload is None or claim_payload is None:
        return {"path": path, "verdict": "missing-history"}

    # The grade commit landed between the resize and the render integration, so "did anybody
    # re-render this after the resize?" is answered by comparing the claim commit with the grade
    # commit: identical bytes mean the enlarged (and re-tinted) pixels are still what shipped.
    graded_payload = git_bytes(GRADE_COMMIT, f"{GENERATED}/{path}")
    unchanged_since_grade = None if graded_payload is None else graded_payload == claim_payload

    reviewed = np.asarray(image(reviewed_payload), dtype=np.uint8)
    claim = np.asarray(image(claim_payload), dtype=np.uint8)
    reviewed_detail = fine_detail(reviewed, reviewed[..., 3] > 8)
    claim_detail = fine_detail(claim, claim[..., 3] > 8)
    correlation = structure_correlation(claim, reviewed)

    same_size = claim.shape == reviewed.shape
    # A gain only counts when it is both relative (5 %) and absolute (2 grey levels): the colour
    # grade nudges every pixel, and on small flat icons that nudge alone would fake a "gain".
    detail_gain = (
        None
        if reviewed_detail is None or claim_detail is None
        else claim_detail > reviewed_detail * 1.05 and claim_detail - reviewed_detail > 2.0
    )
    resample_like = correlation is not None and correlation >= CORRELATION_RESAMPLE_FLOOR

    if same_size and correlation is not None and correlation >= 0.999:
        verdict = "unchanged" if np.array_equal(claim, reviewed) else "recolored-copy"
    elif resample_like and detail_gain is not True:
        verdict = "resampled-copy"
    elif detail_gain is True and not resample_like:
        verdict = "independent-render"
    else:
        verdict = "inconclusive"

    return {
        "path": path,
        "verdict": verdict,
        "reviewedSize": [reviewed.shape[1], reviewed.shape[0]],
        "claimSize": [claim.shape[1], claim.shape[0]],
        "reviewedFineDetail": None if reviewed_detail is None else round(reviewed_detail, 3),
        "claimFineDetail": None if claim_detail is None else round(claim_detail, 3),
        "detailGain": detail_gain,
        "structureCorrelation": None if correlation is None else round(correlation, 4),
        "unchangedSinceGrade": unchanged_since_grade,
        "claimSha256": hashlib.sha256(claim_payload).hexdigest(),
    }


def tracked_pngs() -> list[str]:
    result = subprocess.run(
        ["git", "ls-tree", "-r", "--name-only", "HEAD", GENERATED],
        cwd=REPOSITORY,
        capture_output=True,
        check=True,
    )
    return sorted(
        line[len(GENERATED) + 1:]
        for line in result.stdout.decode().splitlines()
        if line.endswith(".png")
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--write-report", action="store_true")
    parser.add_argument("--json", type=Path, default=None)
    args = parser.parse_args()

    rows = [classify(path) for path in tracked_pngs()]
    counts: dict[str, int] = {}
    for row in rows:
        counts[row["verdict"]] = counts.get(row["verdict"], 0) + 1
    print(json.dumps(counts, indent=2, sort_keys=True))
    if args.json:
        args.json.write_text(json.dumps(rows, indent=1) + "\n", encoding="utf-8")
    if not args.write_report:
        return

    frozen = [row for row in rows if row.get("unchangedSinceGrade")]
    independent = [row for row in rows if row["verdict"] == "independent-render"]
    resampled = [row for row in rows if row["verdict"] in ("resampled-copy", "recolored-copy")]
    inconclusive = [row for row in rows if row["verdict"] == "inconclusive"]
    lines = [
        "# Master tier provenance — what the Phase 76/77 \"HD\" batch actually was",
        "",
        "Generated by `tools/visual/classify_master_tier.py --write-report`. Every number below is",
        "measured from git history, so the conclusion can be re-derived instead of trusted.",
        "",
        f"* reviewed tier: `{REVIEWED_COMMIT}` · NEAREST-resize commit: `{UPSCALE_COMMIT}` ·",
        f"  grade commit: `{GRADE_COMMIT}` · claimed master commit: `{MASTER_COMMIT}`",
        f"* sheets that never changed after the resize+grade (still enlarged pixels, whatever their"
        f" verdict): **{len(frozen)}** of {len(rows)}",
        f"* resampled copies: **{len(resampled)}** · independent renders: **{len(independent)}** ·"
        f" inconclusive: **{len(inconclusive)}**",
        "",
        "## How to read the two measurements",
        "",
        "* **fine detail** — RMS of the one-pixel luminance gradient over opaque pixels. Resampling",
        "  cannot invent fine detail; an independent render at twice the resolution has more of it.",
        "* **corr** — Pearson correlation with the reviewed sheet enlarged to the candidate's size.",
        "  A resized copy stays correlated (0.98+) even after a strong colour grade; an independent",
        "  render of the same character drops to ~0.5-0.93.",
        "",
        "Verdict rule: `corr >= 0.95` **and** no gain in fine detail means *resampled copy*; a gain in",
        "fine detail with `corr < 0.95` means *independent render*; anything else is *inconclusive*.",
        "",
        "## Resampled copies — these are enlargements of the reviewed artwork",
        "",
        "| sheet | reviewed px | claimed px | fine detail reviewed → claimed | corr | untouched since the grade commit |",
        "|---|---|---|---|---|---|",
    ]
    for row in resampled:
        lines.append(
            f"| `{row['path']}` | {row['reviewedSize'][0]}×{row['reviewedSize'][1]} |"
            f" {row['claimSize'][0]}×{row['claimSize'][1]} |"
            f" {row['reviewedFineDetail']} → {row['claimFineDetail']} |"
            f" {row['structureCorrelation']} | {row['unchangedSinceGrade']} |"
        )
    lines += [
        "",
        "## Independent renders — these carry detail the reviewed tier does not have",
        "",
        "| sheet | reviewed px | render px | fine detail reviewed → render | corr |",
        "|---|---|---|---|---|",
    ]
    for row in independent:
        lines.append(
            f"| `{row['path']}` | {row['reviewedSize'][0]}×{row['reviewedSize'][1]} |"
            f" {row['claimSize'][0]}×{row['claimSize'][1]} |"
            f" {row['reviewedFineDetail']} → {row['claimFineDetail']} |"
            f" {row['structureCorrelation']} |"
        )
    if inconclusive:
        lines += [
            "",
            "## Inconclusive (flat artwork: the correlation measure has no structure to lock onto)",
            "",
            "| sheet | fine detail reviewed → claimed | corr |",
            "|---|---|---|",
        ]
        for row in inconclusive:
            lines.append(
                f"| `{row['path']}` | {row['reviewedFineDetail']} → {row['claimFineDetail']} |"
                f" {row['structureCorrelation']} |"
            )
    lines += [
        "",
        "## What this means for the repository",
        "",
        "1. The Phase 76 commit is a NEAREST resize: for the sheets above, the claimed-master bytes are",
        "   pixel-identical to the upscale commit wherever the grade did not touch them.",
        "2. The Phase 77 batch really did come from the GitHub Actions render job for the assets listed",
        "   as independent renders — those are genuine higher-resolution renders and stay in git history",
        "   at `" + MASTER_COMMIT + "`.",
        "3. The runtime tier (`android/assets/generated`) is the reviewed 192-frame artwork, because that",
        "   is the tier the art reviews are hash-bound to. Composing a runtime tier from the independent",
        "   masters requires the new layout to be re-reviewed first (see `docs/ROADMAP_TO_1000.md` R5).",
        "",
    ]
    REPORT.write_text("\n".join(lines), encoding="utf-8")
    print(f"wrote {REPORT.relative_to(REPOSITORY)}")


if __name__ == "__main__":
    main()
