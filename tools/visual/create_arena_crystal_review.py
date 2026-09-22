#!/usr/bin/env python3
"""Audit the crystal refresh and build its review evidence.

The arena's three edge landmarks have been stranded for a round. `build_crystal_prop` moved to
`arena-crystal-premium-v4-vibrant` -- rune studs, moss grounding and an emissive core -- and the catalog still
ships the `premium-v2` render, because the arena batch's audit pinned the old revision and refused the new one.
The batch is unblocked now, and this script is the review that has to happen before the three refreshed landmarks
replace the pixels a phone is showing today.

What it checks, in the order a reviewer would:

* the candidate carries exactly the three original landmarks -- `crystal_prop_3..5` are the hollow variants of the
  second arena and are not part of this batch, so a candidate that claims them is a candidate nobody reviewed;
* every entry states the contract it was built under (`arena-crystal-premium-v4-vibrant`, its identity, and
  `runtimeGlow: true`, because the emissive core is the point of the revision);
* the shipped pixels are not the candidate pixels -- a "refresh" that renders the same bytes is the failure this
  whole record exists to catch;
* every sheet is 384 px square, paints something, keeps a transparent margin, and is distinct from its siblings;
* the refresh holds the shipped tier's luminance: each landmark's painted pixels may not lose more than a tenth
  of their mean value, because the arena round before this one was a display-quality pass and a later render is
  not allowed to quietly undo it. This is the rule the whole current batch trips -- see the measurements below --
  and `--allow-parity-failure` exists so the record can be written as a *held* one instead of not written at all.

Usage:
    python3 tools/visual/create_arena_crystal_review.py <committed-tree> <candidate> <output>
        [--allow-parity-failure]

The generated sheet and the audit JSON are the evidence; the markdown beside them quotes these numbers, and
`promote_arena_crystal_refresh.py` refuses to copy a pixel the audit does not hash.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageStat

from create_character_animation_review import canvas_base, presentation_card, text
from review_strips import grade_row, silhouette_view

CRYSTAL_IDENTITIES = ("azure-waystone-fan", "violet-moon-geode", "amber-root-lantern")
EXPECTED_KEYS = tuple(f"crystal_prop_{variant}" for variant in range(3))
PROP_SIZE = 384
MINIMUM_MARGIN = 4
#: How much of its painted mean value a landmark may lose before the refresh is a dimming rather than a refresh.
MAXIMUM_PAINTED_VALUE_LOSS = 0.10


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("baseline", type=Path, help="the committed tree the refresh is arriving into")
    parser.add_argument("candidate", type=Path, help="the rendered arena batch")
    parser.add_argument("output", type=Path)
    parser.add_argument(
        "--allow-parity-failure",
        action="store_true",
        help="record the luminance numbers as a held decision instead of refusing to write the record",
    )
    args = parser.parse_args()
    baseline = args.baseline.resolve()
    candidate = args.candidate.resolve()
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)

    # The same order as the batch audit: the record is written with the measurements in it whatever the verdict,
    # so a held refresh leaves evidence behind rather than an empty artifact.
    audit = audit_batch(baseline, candidate, allow_parity_failure=True)
    create_marker_lineup(baseline, candidate, output / "arena_crystal_refresh.png")
    create_marker_silhouettes(candidate, output / "arena_crystal_refresh_shapes.png")

    sheets = sorted(output.glob("*.png"))
    audit["reviewSheets"] = {
        path.name: {"bytes": path.stat().st_size, "sha256": sha256(path)} for path in sheets
    }
    audit["reviewSheetCount"] = len(sheets)
    (output / "arena_crystal_refresh_audit.json").write_text(
        json.dumps(audit, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    print(
        f"Audited {len(EXPECTED_KEYS)} crystal landmarks and wrote {len(sheets)} review sheets to {output}"
    )
    if not args.allow_parity_failure:
        for record in audit["assets"]:
            enforce_parity(record, allow_parity_failure=False)


def audit_batch(baseline: Path, candidate: Path, allow_parity_failure: bool = False) -> dict:
    candidate_manifest_path = candidate / "asset_manifest.json"
    candidate_manifest = read_json(candidate_manifest_path)
    candidate_keys = {asset["key"] for asset in candidate_manifest.get("assets", [])}
    missing = sorted(set(EXPECTED_KEYS) - candidate_keys)
    if missing:
        raise ValueError(f"Candidate is missing {missing}")

    baseline_manifest = read_json(baseline / "asset_manifest.json")
    baseline_by_key = {asset["key"]: asset for asset in baseline_manifest.get("assets", [])}
    absent = sorted(set(EXPECTED_KEYS) - set(baseline_by_key))
    if absent:
        raise ValueError(
            f"the committed tree does not carry {absent}: this tool replaces reviewed landmarks, it does not "
            f"introduce new ones"
        )

    records = []
    seen = {}
    for key in EXPECTED_KEYS:
        asset = next(item for item in candidate_manifest["assets"] if item["key"] == key)
        validate_contract(asset, key)
        metadata_path = candidate / "environment" / f"{key}.json"
        if read_json(metadata_path) != asset:
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

        shipped = baseline_by_key[key]
        shipped_path = baseline / shipped["sheet"]
        shipped_digest = sha256(shipped_path)
        if shipped_digest == digest:
            raise ValueError(
                f"{key} renders the same bytes it already ships: a refresh that changes nothing is not a refresh"
            )
        shipped_image = Image.open(shipped_path).convert("RGBA")
        shipped_mean = painted_mean_value(shipped_image)
        candidate_mean = painted_mean_value(image)
        ratio = candidate_mean / shipped_mean if shipped_mean > 0 else 1.0
        record = {
            "key": key,
            "identity": asset.get("prop"),
            "variant": asset.get("variant"),
            "sheetSha256": digest,
            "metadataSha256": sha256(metadata_path),
            "shippedSheetSha256": shipped_digest,
            "shippedModelRevision": shipped.get("modelRevision"),
            "dimensions": {"width": image.width, "height": image.height},
            "decodedBytes": image.width * image.height * 4,
            "alphaMargins": margins,
            "shippedPaintedMeanValue": round(shipped_mean, 3),
            "candidatePaintedMeanValue": round(candidate_mean, 3),
            "paintedMeanValueRatio": round(ratio, 4),
            "triangles": asset["triangles"],
            "meshParts": asset["meshParts"],
            "materialCount": asset["materialCount"],
            "modelRevision": asset["modelRevision"],
            "runtimeGlow": asset.get("runtimeGlow"),
        }
        enforce_parity(record, allow_parity_failure)
        records.append(record)

    return {
        "schemaVersion": 1,
        "batch": "arena-crystal-refresh-v4",
        "expectedKeys": list(EXPECTED_KEYS),
        "baselineManifestSha256": sha256(baseline / "asset_manifest.json"),
        "candidateManifestSha256": sha256(candidate_manifest_path),
        "candidatePayload": {
            asset["sheet"]: sha256(candidate / asset["sheet"])
            for asset in candidate_manifest["assets"]
            if asset["key"] in EXPECTED_KEYS
        },
        "assets": records,
        "summary": {
            "assetCount": len(records),
            "minimumTransparentAssetMargin": min(min(record["alphaMargins"].values()) for record in records),
            "decodedBytes": sum(record["decodedBytes"] for record in records),
            "minimumPaintedMeanValueRatio": round(
                min(record["paintedMeanValueRatio"] for record in records), 4
            ),
            "requiredPaintedMeanValueRatio": round(1.0 - MAXIMUM_PAINTED_VALUE_LOSS, 4),
            "parityDecision": "held" if any(
                record["paintedMeanValueRatio"] < 1.0 - MAXIMUM_PAINTED_VALUE_LOSS for record in records
            ) else "met",
            "shippedRevision": sorted({record["shippedModelRevision"] for record in records}),
            "candidateRevision": sorted({record["modelRevision"] for record in records}),
        },
    }


def enforce_parity(record: dict, allow_parity_failure: bool) -> None:
    ratio = record["paintedMeanValueRatio"]
    if ratio < 1.0 - MAXIMUM_PAINTED_VALUE_LOSS and not allow_parity_failure:
        raise ValueError(
            f"{record['key']} dims from {record['shippedPaintedMeanValue']} to "
            f"{record['candidatePaintedMeanValue']} ({ratio}x) on its painted pixels: the arena's "
            f"display-quality pass is not a later render's to undo. If the point of this record is to hold the "
            f"refresh rather than ship it, pass --allow-parity-failure."
        )


def validate_contract(asset: dict, key: str) -> None:
    variant = int(key.rsplit("_", 1)[1])
    expected = {
        "family": "environment",
        "frameClass": "environment",
        "frameSize": PROP_SIZE,
        "frameWidth": PROP_SIZE,
        "frameHeight": PROP_SIZE,
        "sheetWidth": PROP_SIZE,
        "sheetHeight": PROP_SIZE,
        "modelRevision": "arena-crystal-premium-v4-vibrant",
        "prop": CRYSTAL_IDENTITIES[variant],
        "variant": variant,
        "runtimeGlow": True,
    }
    for field, value in expected.items():
        if asset.get(field) != value:
            raise ValueError(f"{key} {field} is {asset.get(field)!r}; expected {value!r}")
    if asset.get("visualQuality") != "studio-v4-vibrant":
        raise ValueError(f"{key} visualQuality is {asset.get('visualQuality')!r}")
    if not 700 <= int(asset["triangles"]) <= 4_200:
        raise ValueError(f"{key} has {asset['triangles']} triangles, outside the landmark band")
    if int(asset["meshParts"]) < 30 or int(asset["materialCount"]) < 8:
        raise ValueError(f"{key} is too simple to be a reviewed landmark: {asset['meshParts']} parts")
    if int(asset["renderSupersample"]) < 2 or int(asset["renderSamples"]) < 8:
        raise ValueError(f"{key} was rendered below the reviewed tier")


def painted_mean_value(image: Image.Image) -> float:
    """The mean luminance of the pixels a player actually sees.

    A frame's transparent margin is not part of the landmark, and averaging it in would let a prop that gained
    padding look dimmer than it is -- or, worse, hide a real darkening behind a change in silhouette.
    """
    rgb = image.convert("RGB")
    alpha = image.getchannel("A")
    mask = alpha.point(lambda value: 255 if value > 16 else 0)
    statistics = ImageStat.Stat(rgb, mask)
    red, green, blue = statistics.mean
    return 0.299 * red + 0.587 * green + 0.114 * blue


def create_marker_lineup(baseline: Path, candidate: Path, output: Path) -> None:
    """Every landmark at field scale, shipped beside refreshed, with the gradient it will stand under."""
    width, height = 1_400, 1_180
    canvas = canvas_base(width, height, "CRYSTAL LANDMARKS — SHIPPED BESIDE REFRESHED")
    draw = ImageDraw.Draw(canvas)
    for index, key in enumerate(EXPECTED_KEYS):
        x = 40 + index * 456
        variant = int(key.rsplit("_", 1)[1])
        text(draw, (x + 190, 88), CRYSTAL_IDENTITIES[variant].replace("-", " ").upper(),
             17, bold=True, anchor="ma")
        shipped = presentation_card(asset_image(baseline, key), "checker", 176, 176)
        refreshed = presentation_card(asset_image(candidate, key), "checker", 176, 176)
        canvas.paste(shipped.convert("RGB"), (x + 4, 120))
        canvas.paste(refreshed.convert("RGB"), (x + 200, 120))
        text(draw, (x + 92, 312), "SHIPPED", 15, bold=True, anchor="ma")
        text(draw, (x + 288, 312), "REFRESHED", 15, bold=True, anchor="ma")
        canvas.paste(grade_row(asset_image(candidate, key)).convert("RGB"), (x + 4, 344))
        text(draw, (x + 190, 664), "STAGE GRADE — DAWN, AMBER, TEAL, HOLLOW", 13, anchor="ma",
             color="#AFC5BE")
        silhouette = silhouette_view(asset_image(baseline, key)).resize((190, 260), Image.Resampling.LANCZOS)
        silhouette_candidate = silhouette_view(asset_image(candidate, key)).resize(
            (190, 260), Image.Resampling.LANCZOS
        )
        canvas.paste(silhouette.convert("RGB"), (x + 4, 700))
        canvas.paste(silhouette_candidate.convert("RGB"), (x + 200, 700))
    text(draw, (width // 2, 1_010), "READ AT FIELD SCALE — SILHOUETTES ABOVE, COLOUR BELOW", 16,
         bold=True, anchor="ma")
    for index, key in enumerate(EXPECTED_KEYS):
        x = 40 + index * 456
        canvas.paste(presentation_card(asset_image(candidate, key), "dark", 150, 150).convert("RGB"),
                     (x + 100, 1_030))
    canvas.save(output, optimize=True)


def create_marker_silhouettes(candidate: Path, output: Path) -> None:
    """The one question a landmark has to answer at a glance: which of the three am I looking at."""
    canvas = canvas_base(1_000, 460, "IDENTITY AT SILHOUETTE SCALE — ONE SHAPE EACH")
    draw = ImageDraw.Draw(canvas)
    for index, key in enumerate(EXPECTED_KEYS):
        silhouette = silhouette_view(asset_image(candidate, key)).resize((260, 300), Image.Resampling.LANCZOS)
        canvas.paste(silhouette.convert("RGB"), (70 + index * 300, 90))
        text(draw, (200 + index * 300, 404), CRYSTAL_IDENTITIES[index].replace("-", " ").upper(),
             14, bold=True, anchor="ma")
    canvas.save(output, optimize=True)


def asset_image(root: Path, key: str) -> Image.Image:
    return Image.open(root / "environment" / f"{key}.png").convert("RGBA")


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


if __name__ == "__main__":
    main()
