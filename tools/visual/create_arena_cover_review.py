#!/usr/bin/env python3
"""Audit the arena-cover batch and build the review evidence for it.

The cover is the arena's four families of outcrop -- the standing cover that stops bodies and arrows and the low
cover that stops only bodies. They render on their own (batch `arena-cover`) so that the arena's existing art stays
at the tier it was reviewed at, and this script is the review that has to happen before any of it ships.

What it checks, in the order a reviewer would:

* the candidate is exactly the twelve cover keys -- no more, because a batch that quietly grew is a batch nobody
  reviewed, and no fewer, because a family missing a variant is a family that renders differently per seed;
* every entry states the contract it was built under (`arena-obstacle-premium-v1`, its family, its kind of cover,
  and `runtimeGlow: false`, because ground that glows would claim to be a landmark);
* every sheet is 384 px square, paints something, and keeps a transparent margin, so a prop that bleeds to the edge
  of its frame is caught here rather than on a phone;
* the two kinds are separated by silhouette: the shortest standing family has to paint clearly taller than the
  tallest low family, measured on the pixels, because that is the difference the player is reading;
* no two sheets are the same image, and the batch stays inside its decoded-memory budget.

Usage:
    python3 tools/visual/create_arena_cover_review.py <committed-tree> <candidate> <output>

The generated sheets and the audit JSON are the evidence; the markdown beside them quotes these numbers, and
`promote_arena_cover_batch.py` refuses to copy a pixel the audit does not hash.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw

from create_character_animation_review import canvas_base, checker, presentation_card, text
from review_strips import grade_row, silhouette_view

OBSTACLE_FAMILIES = ("standing_stone", "ruin_slab", "thorn_hedge", "mossy_boulder")
OBSTACLE_VARIANTS = 3
OBSTACLE_COVER = {
    "standing_stone": "shelter",
    "ruin_slab": "shelter",
    "thorn_hedge": "low",
    "mossy_boulder": "low",
}
EXPECTED_KEYS = tuple(
    f"obstacle_{family}_{variant}"
    for family in OBSTACLE_FAMILIES
    for variant in range(OBSTACLE_VARIANTS)
)
PROP_SIZE = 384
DECODED_BUDGET = 12 * PROP_SIZE * PROP_SIZE * 4
MINIMUM_MARGIN = 4
#: How much taller the standing cover has to paint than the low cover, on the pixels.
SHELTER_OVER_LOW = 1.6


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("baseline", type=Path, help="the committed tree the cover is arriving into")
    parser.add_argument("candidate", type=Path, help="the rendered cover batch")
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    baseline = args.baseline.resolve()
    candidate = args.candidate.resolve()
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)

    audit = audit_batch(baseline, candidate)
    create_family_lineup(candidate, output / "arena_cover_families.png")
    create_kind_separation(candidate, audit, output / "arena_cover_kinds.png")

    sheets = sorted(output.glob("*.png"))
    audit["reviewSheets"] = {
        path.name: {"bytes": path.stat().st_size, "sha256": sha256(path)} for path in sheets
    }
    audit["reviewSheetCount"] = len(sheets)
    (output / "arena_cover_audit.json").write_text(
        json.dumps(audit, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    print(
        f"Audited {audit['summary']['assetCount']} cover props and wrote {len(sheets)} review sheets to {output}"
    )


def audit_batch(baseline: Path, candidate: Path) -> dict:
    candidate_manifest_path = candidate / "asset_manifest.json"
    candidate_manifest = read_json(candidate_manifest_path)
    actual_keys = sorted(asset["key"] for asset in candidate_manifest.get("assets", []))
    if actual_keys != sorted(EXPECTED_KEYS):
        raise ValueError(f"Candidate must contain exactly {EXPECTED_KEYS}; found {actual_keys}")

    baseline_manifest = read_json(baseline / "asset_manifest.json")
    baseline_keys = {asset["key"] for asset in baseline_manifest.get("assets", [])}
    already = sorted(baseline_keys & set(EXPECTED_KEYS))
    if already:
        raise ValueError(f"The committed tree already carries cover art: {already}")

    records = []
    total_decoded = 0
    seen = {}
    for key in EXPECTED_KEYS:
        asset = next(item for item in candidate_manifest["assets"] if item["key"] == key)
        validate_contract(asset, key)
        metadata = read_json(candidate / "environment" / f"{key}.json")
        if metadata != asset:
            raise ValueError(f"{key}: manifest entry and per-asset metadata differ")
        image = Image.open(candidate / asset["sheet"]).convert("RGBA")
        if image.size != (PROP_SIZE, PROP_SIZE):
            raise ValueError(f"{key} is {image.size}, expected {(PROP_SIZE, PROP_SIZE)}")
        digest = sha256(candidate / asset["sheet"])
        if digest in seen:
            raise ValueError(f"{key} is the same image as {seen[digest]}")
        seen[digest] = key
        alpha = image.getchannel("A")
        bounds = alpha.getbbox()
        if bounds is None:
            raise ValueError(f"{key} paints nothing")
        margins = {
            "left": bounds[0],
            "top": bounds[1],
            "right": image.width - bounds[2],
            "bottom": image.height - bounds[3],
        }
        if min(margins.values()) < MINIMUM_MARGIN:
            raise ValueError(f"{key} approaches a boundary: {margins}")
        total_decoded += image.width * image.height * 4
        records.append({
            "key": key,
            "coverFamily": asset["coverFamily"],
            "cover": asset["cover"],
            "variant": asset["variant"],
            "sheetSha256": digest,
            "metadataSha256": sha256(candidate / "environment" / f"{key}.json"),
            "dimensions": {"width": image.width, "height": image.height},
            "decodedBytes": image.width * image.height * 4,
            "paintedHeight": bounds[3] - bounds[1],
            "paintedWidth": bounds[2] - bounds[0],
            "alphaMargins": margins,
            "triangles": asset["triangles"],
            "meshParts": asset["meshParts"],
            "materialCount": asset["materialCount"],
            "modelRevision": asset["modelRevision"],
            "runtimeGlow": asset.get("runtimeGlow"),
        })

    if total_decoded > DECODED_BUDGET:
        raise ValueError(f"the cover batch decodes to {total_decoded}, over {DECODED_BUDGET}")
    separation = cover_separation(records)
    return {
        "schemaVersion": 1,
        "batch": "arena-cover-v1",
        "expectedKeys": list(EXPECTED_KEYS),
        "baselineManifestSha256": sha256(baseline / "asset_manifest.json"),
        "candidateManifestSha256": sha256(candidate_manifest_path),
        "candidatePayload": {
            asset["sheet"]: sha256(candidate / asset["sheet"]) for asset in candidate_manifest["assets"]
        },
        "assets": records,
        "coverSeparation": separation,
        "summary": {
            "assetCount": len(records),
            "staticFrameCount": len(records),
            "familyCount": len(OBSTACLE_FAMILIES),
            "coverPropCount": len(EXPECTED_KEYS),
            "shelterPropCount": sum(1 for record in records if record["cover"] == "shelter"),
            "lowPropCount": sum(1 for record in records if record["cover"] == "low"),
            "minimumTransparentAssetMargin": min(
                min(record["alphaMargins"].values()) for record in records
            ),
            "decodedBytes": total_decoded,
            "decodedBudgetBytes": DECODED_BUDGET,
        },
    }


def cover_separation(records: list[dict]) -> dict:
    """The rule the two kinds exist for, measured on the pixels rather than on the scene units."""
    shelter = min(
        record["paintedHeight"] for record in records if record["cover"] == "shelter"
    )
    low = max(record["paintedHeight"] for record in records if record["cover"] == "low")
    if shelter < low * SHELTER_OVER_LOW:
        raise ValueError(
            f"the standing cover paints {shelter}px against {low}px for the low cover: under "
            f"{SHELTER_OVER_LOW}x, the two kinds read alike and the field stops teaching its own rule"
        )
    return {
        "shortestShelterPaintedHeight": shelter,
        "tallestLowPaintedHeight": low,
        "ratio": round(shelter / low, 3),
        "requiredRatio": SHELTER_OVER_LOW,
    }


def validate_contract(asset: dict, key: str) -> None:
    family = key[len("obstacle_"):-2]
    expected = {
        "family": "environment",
        "frameClass": "environment",
        "frameSize": PROP_SIZE,
        "frameWidth": PROP_SIZE,
        "frameHeight": PROP_SIZE,
        "sheetWidth": PROP_SIZE,
        "sheetHeight": PROP_SIZE,
        "modelRevision": "arena-obstacle-premium-v1",
        "assetKind": "obstacle",
        "cover": OBSTACLE_COVER[family],
        "coverFamily": family,
        "variant": int(key[-1]),
        "runtimeGlow": False,
    }
    for field, value in expected.items():
        if asset.get(field) != value:
            raise ValueError(f"{key} {field} is {asset.get(field)!r}; expected {value!r}")
    if asset.get("visualQuality") != "studio-v4-vibrant":
        raise ValueError(f"{key} visualQuality is {asset.get('visualQuality')!r}")
    if not 200 <= int(asset["triangles"]) <= 1_200:
        raise ValueError(f"{key} has {asset['triangles']} triangles, outside the prop band")
    if int(asset["meshParts"]) < 8 or int(asset["materialCount"]) < 3:
        raise ValueError(f"{key} is too simple to be a reviewed prop: {asset['meshParts']} parts")
    if int(asset["renderSupersample"]) < 2 or int(asset["renderSamples"]) < 8:
        raise ValueError(f"{key} was rendered below the reviewed tier")


def create_family_lineup(candidate: Path, output: Path) -> None:
    width, height = 1_710, 1_120
    canvas = canvas_base(width, height, "ARENA COVER — FOUR FAMILIES, THREE VARIANTS EACH")
    draw = ImageDraw.Draw(canvas)
    for index, family in enumerate(OBSTACLE_FAMILIES):
        x = 40 + index * 418
        cover = OBSTACLE_COVER[family]
        text(draw, (x + 190, 84), family.replace("_", " ").upper(), 19, bold=True, anchor="ma")
        text(draw, (x + 190, 108), f"{cover.upper()} COVER", 15, anchor="ma", color="#AFC5BE")
        tallest = None
        for variant in range(OBSTACLE_VARIANTS):
            sprite = asset_image(candidate, f"obstacle_{family}_{variant}")
            card = presentation_card(sprite, "checker", 128, 176)
            canvas.paste(card.convert("RGB"), (x + variant * 132, 132))
            text(draw, (x + variant * 132 + 64, 316), f"v{variant}", 14, anchor="ma")
            if tallest is None:
                tallest = sprite
        silhouette = silhouette_view(tallest).resize((392, 300), Image.Resampling.LANCZOS)
        canvas.paste(silhouette.convert("RGB"), (x, 348))
        text(draw, (x + 196, 664), "SILHOUETTE AT FIELD SCALE", 14, anchor="ma", color="#AFC5BE")
        grade = grade_row(asset_image(candidate, f"obstacle_{family}_0"))
        canvas.paste(grade.convert("RGB"), (x, 706))
    text(draw, (width // 2, 1_000), "STAGE GRADE — VARIANT 0 OF EACH FAMILY", 16, bold=True, anchor="ma")
    canvas.save(output, optimize=True)


def create_kind_separation(candidate: Path, audit: dict, output: Path) -> None:
    """The one picture the field's rule lives or dies by: standing cover against low cover, at field scale."""
    width, height = 1_400, 760
    canvas = canvas_base(width, height, "SHELTER COVER STOPS BODIES AND ARROWS — LOW COVER STOPS BODIES")
    draw = ImageDraw.Draw(canvas)
    field_scale = 0.62
    for column, (label, keys) in enumerate((
        ("SHELTER", ("obstacle_standing_stone_0", "obstacle_ruin_slab_0")),
        ("LOW", ("obstacle_thorn_hedge_0", "obstacle_mossy_boulder_0")),
    )):
        base = 90 + column * 700
        text(draw, (base + 300, 96), label, 22, bold=True, anchor="ma")
        for row, key in enumerate(keys):
            sprite = asset_image(candidate, key)
            size = int(384 * field_scale)
            card = presentation_card(sprite, "checker", size, size)
            canvas.paste(card.convert("RGB"), (base + row * 310, 140))
            text(draw, (base + row * 310 + size // 2, 140 + size + 24), key.replace("obstacle_", "").replace("_", " "),
                 13, anchor="ma")
    separation = audit["coverSeparation"]
    text(
        draw,
        (width // 2, 620),
        f"SHORTEST SHELTER {separation['shortestShelterPaintedHeight']}px vs TALLEST LOW "
        f"{separation['tallestLowPaintedHeight']}px  ({separation['ratio']}x, floor "
        f"{separation['requiredRatio']}x)",
        17, bold=True, anchor="ma",
    )
    canvas.save(output, optimize=True)


def asset_image(root: Path, key: str) -> Image.Image:
    return Image.open(root / "environment" / f"{key}.png").convert("RGBA")


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


if __name__ == "__main__":
    main()
