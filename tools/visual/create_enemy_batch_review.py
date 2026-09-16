#!/usr/bin/env python3
"""Audit and render exhaustive review evidence for the eight regular enemies."""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw

from create_character_animation_review import (
    CLIP_ORDER,
    CharacterFrames,
    canvas_base,
    create_motion_sheet,
    create_readability_sheet,
    presentation_card,
    text,
)

from review_strips import grade_row

# Studio-v3 contact-sheet mode: same side-by-side-against-baseline layout as premium-v2,
# baseline is the current premium-v2 output, candidate is studio-v3 (weighted 2.4/1.2 + rim/highlight).
STUDIO_TIER_BASELINE_QUALITY = "premium-v2"
STUDIO_TIER_CANDIDATE_QUALITY = "studio-v3"
# Contact sheet uses readability_sheet(old, new) with old=premium-v2, new=studio-v3


ENEMIES = (
    ("rootling", "Rootling", "rootling-thorn-scout-v2", "premium-humanoid-v2", "rootling-skirmisher-v2"),
    ("stonekin", "Stonekin", "stonekin-rune-bulwark-v2", "premium-heavy-humanoid-v2", "stonekin-juggernaut-v2"),
    ("gloom_wolf", "Gloom Wolf", "gloom-wolf-shadow-stalker-v2", "premium-quadruped-mapped-v2", "gloom-wolf-pouncer-v2"),
    ("fungal_brute", "Fungal Brute", "fungal-brute-spore-bruiser-v2", "premium-heavy-humanoid-v2", "fungal-brute-brawler-v2"),
    # R3.4: the roster doubles. These four have no predecessor render, which the audit records
    # explicitly (`predecessor: "none"`) instead of inventing a comparison: their readability sheet
    # is the new master against itself, and the review document says so.
    ("bark_stalker", "Bark Stalker", "bark-stalker-moss-climber-v2", "premium-humanoid-v2", "bark-stalker-lurker-v2"),
    ("sap_hound", "Sap Hound", "sap-hound-resin-runner-v2", "premium-quadruped-mapped-v2", "sap-hound-runner-v2"),
    ("husk_warden", "Husk Warden", "husk-warden-shield-bearer-v2", "premium-heavy-humanoid-v2", "husk-warden-bulwark-v2"),
    ("bramble_thrall", "Bramble Thrall", "bramble-thrall-thorn-lumberer-v2", "premium-heavy-humanoid-v2", "bramble-thrall-lumber-v2"),
)
EXPECTED_CLIPS = {"idle": 6, "attack": 8, "hit": 4, "death": 10}
MIN_UNIQUE = {"idle": 5, "attack": 7, "hit": 3, "death": 9}
EXPECTED_PIVOT = {"units": "normalized-bottom-left", "x": 0.5, "y": 0.12}


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("baseline", type=Path)
    parser.add_argument("candidate", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    baseline = args.baseline.resolve()
    candidate = args.candidate.resolve()
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)

    audit = audit_batch(baseline, candidate)
    baseline_keys = {asset["key"] for asset in read_json(baseline / "asset_manifest.json")["assets"]}
    for key, label, *_ in ENEMIES:
        new = CharacterFrames(candidate, key)
        # A first render has no predecessor to compare against: its readability sheet is the new master against
        # itself, exactly as the audit records it (`predecessor: "none"`).
        old = CharacterFrames(baseline, key) if key in baseline_keys else new
        create_motion_sheet(new, label, output / f"{key}_full_motion.png")
        create_readability_sheet(old, new, label, output / f"{key}_readability.png")
    create_lineup_sheet(baseline, candidate, audit, output / "regular_enemies_lineup.png")

    sheet_paths = sorted(output.glob("*.png"))
    audit["reviewSheets"] = {
        path.name: {"sha256": sha256(path), "bytes": path.stat().st_size}
        for path in sheet_paths
    }
    audit["reviewSheetCount"] = len(sheet_paths)
    audit_path = output / "regular_enemies_audit.json"
    audit_path.write_text(json.dumps(audit, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(
        f"Audited {audit['summary']['assetCount']} enemies and "
        f"{audit['summary']['frameCount']} frames; wrote {len(sheet_paths)} review sheets to {output}"
    )


def audit_batch(baseline: Path, candidate: Path) -> dict:
    baseline_manifest_path = baseline / "asset_manifest.json"
    candidate_manifest_path = candidate / "asset_manifest.json"
    baseline_manifest = read_json(baseline_manifest_path)
    candidate_manifest = read_json(candidate_manifest_path)
    expected_keys = [record[0] for record in ENEMIES]
    candidate_keys = sorted(entry["key"] for entry in candidate_manifest["assets"])
    if candidate_keys != sorted(expected_keys):
        raise ValueError(f"Candidate must contain exactly {expected_keys}; found {candidate_keys}")
    if candidate_manifest.get("pipelineVersion", 0) < 3:
        raise ValueError("Candidate pipelineVersion must be at least 3")
    if candidate_manifest.get("frameRate") != 12:
        raise ValueError("Candidate frame rate must remain 12 fps")
    if candidate_manifest.get("renderSupersample") != 2:
        raise ValueError("Candidate must use 2x supersampling")
    if candidate_manifest.get("opaqueRenderSamples") != 28:
        raise ValueError("Candidate must use 24 opaque samples")
    if candidate_manifest.get("renderTierTop") != [3, 36]:
        raise ValueError("Candidate must carry the Phase 28.3 tier fingerprint")

    required_bones = candidate_manifest.get("requiredBones", [])
    if len(required_bones) != 25 or len(set(required_bones)) != 25:
        raise ValueError("Enemy rigs must expose the locked 25-bone contract")

    baseline_entries = {entry["key"]: entry for entry in baseline_manifest["assets"]}
    candidate_entries = {entry["key"]: entry for entry in candidate_manifest["assets"]}
    records = []
    total_frames = 0
    global_margins = {side: 10_000 for side in ("left", "top", "right", "bottom")}
    total_decoded = 0

    for key, label, revision, rig_profile, animation_profile in ENEMIES:
        entry = candidate_entries[key]
        first_render = key not in baseline_entries
        validate_metadata(entry, key, revision, rig_profile, animation_profile, required_bones)
        metadata_entry = read_json(candidate / "sprites" / f"{key}.json")
        if metadata_entry != entry:
            raise ValueError(f"{key} manifest and per-asset metadata differ")
        candidate_character = CharacterFrames(candidate, key)
        clip_records = {}
        asset_margins = {side: 10_000 for side in global_margins}
        all_hashes: list[str] = []

        for clip, expected_count in EXPECTED_CLIPS.items():
            regions = sorted(entry["clips"].get(clip, []), key=lambda value: value["index"])
            if len(regions) != expected_count:
                raise ValueError(f"{key} {clip} has {len(regions)} frames, expected {expected_count}")
            if [region["index"] for region in regions] != list(range(expected_count)):
                raise ValueError(f"{key} {clip} frame indices are not contiguous")
            frame_hashes = []
            clip_margins = {side: 10_000 for side in global_margins}
            for index, region in enumerate(regions):
                if region.get("page", 0) != 0:
                    raise ValueError(f"{key} must remain a single-page atlas")
                if region["width"] != 192 or region["height"] != 192:
                    raise ValueError(f"{key} {clip}/{index} is not a native 192 px frame")
                frame = candidate_character.frame(clip, index)
                alpha_box = frame.getchannel("A").getbbox()
                if alpha_box is None:
                    raise ValueError(f"{key} {clip}/{index} is empty")
                left, top, right, bottom = alpha_box
                margins = {
                    "left": left,
                    "top": top,
                    "right": frame.width - right,
                    "bottom": frame.height - bottom,
                }
                if min(margins.values()) < 2:
                    raise ValueError(f"{key} {clip}/{index} approaches the frame boundary: {margins}")
                for side, margin in margins.items():
                    clip_margins[side] = min(clip_margins[side], margin)
                    asset_margins[side] = min(asset_margins[side], margin)
                    global_margins[side] = min(global_margins[side], margin)
                digest = hashlib.sha256(frame.tobytes()).hexdigest()
                frame_hashes.append(digest)
                all_hashes.append(digest)
            unique_count = len(set(frame_hashes))
            if unique_count < MIN_UNIQUE[clip]:
                raise ValueError(f"{key} {clip} has only {unique_count} unique visible frames")
            clip_records[clip] = {
                "frameCount": expected_count,
                "uniqueVisibleFrames": unique_count,
                "minimumAlphaMargins": clip_margins,
            }
            total_frames += expected_count

        candidate_sheet = candidate / entry["sheet"]
        candidate_atlas = candidate / entry["atlas"]
        candidate_metadata = candidate / "sprites" / f"{key}.json"
        candidate_hash = sha256(candidate_sheet)
        if first_render:
            baseline_hash = candidate_hash
        else:
            baseline_hash = sha256(baseline / baseline_entries[key]["sheet"])
            if baseline_hash == candidate_hash:
                raise ValueError(f"{key} candidate sheet is byte-identical to the baseline")
        if len(set(all_hashes)) < 20:
            raise ValueError(f"{key} has insufficient full-set motion diversity")
        for sheet in entry["sheets"]:
            total_decoded += int(sheet["width"]) * int(sheet["height"]) * 4

        records.append({
            "key": key,
            "label": label,
            "predecessor": "none" if first_render else "premium-v2",
            "baselineSheetSha256": baseline_hash,
            "candidateSheetSha256": candidate_hash,
            "candidateAtlasSha256": sha256(candidate_atlas),
            "candidateMetadataSha256": sha256(candidate_metadata),
            "modelRevision": revision,
            "rigProfile": rig_profile,
            "animationProfile": animation_profile,
            "triangles": entry["triangles"],
            "meshParts": entry["meshParts"],
            "materialCount": entry["materialCount"],
            "rigBoneCount": entry["rigBoneCount"],
            "minimumAlphaMargins": asset_margins,
            "clips": clip_records,
        })

    # The batch doubled from four enemies to eight (R3.4), so the per-batch decoded budget doubles too:
    # 8 x 1920 x 768 x 4 = 47,185,920 bytes, and the committed budget carries 1 MiB of headroom over it.
    decoded_limit = 48 * 1024 * 1024
    if total_decoded > decoded_limit:
        raise ValueError(f"Enemy batch decodes to {total_decoded} bytes, over {decoded_limit}")
    return {
        "schemaVersion": 1,
        "batch": "regular-enemies-premium-v2",
        "baselineManifestSha256": sha256(baseline_manifest_path),
        "candidateManifestSha256": sha256(candidate_manifest_path),
        "expectedKeys": expected_keys,
        "frameContract": EXPECTED_CLIPS,
        "minimumUniqueVisibleFrames": MIN_UNIQUE,
        "assets": records,
        "summary": {
            "assetCount": len(records),
            "frameCount": total_frames,
            "singlePageAtlasCount": len(records),
            "decodedBytes": total_decoded,
            "decodedBudgetBytes": decoded_limit,
            "minimumAlphaMargins": global_margins,
            "minimumTriangles": min(record["triangles"] for record in records),
            "maximumTriangles": max(record["triangles"] for record in records),
            "minimumMeshParts": min(record["meshParts"] for record in records),
            "minimumMaterialCount": min(record["materialCount"] for record in records),
        },
    }


def validate_metadata(
    entry: dict,
    key: str,
    revision: str,
    rig_profile: str,
    animation_profile: str,
    required_bones: list[str],
) -> None:
    expected = {
        "family": "enemy",
        "builder": key,
        "frameClass": "character",
        "frameSize": 192,
        "sheetWidth": 1920,
        "sheetHeight": 768,
        "pivot": EXPECTED_PIVOT,
        "alphaMode": "STRAIGHT_RGBA",
        "frameRate": 12,
        "renderSupersample": 2,
        "renderSamples": 28,
        "boneAnimated": True,
        "visualQuality": "studio-v3",
        "modelRevision": revision,
        "rigProfile": rig_profile,
        "animationProfile": animation_profile,
    }
    for field, value in expected.items():
        if entry.get(field) != value:
            raise ValueError(f"{key} {field} is {entry.get(field)!r}; expected {value!r}")
    if entry.get("bones") != sorted(required_bones):
        raise ValueError(f"{key} does not match the locked rig bone names")
    if entry.get("rigBoneCount") != 25:
        raise ValueError(f"{key} must retain all 25 bones")
    if not 900 <= int(entry.get("triangles", 0)) <= 4_000:
        raise ValueError(f"{key} triangle count is outside the premium enemy budget")
    if int(entry.get("meshParts", 0)) < 32:
        raise ValueError(f"{key} requires at least 32 purposeful mesh parts")
    if int(entry.get("materialCount", 0)) < 6:
        raise ValueError(f"{key} requires at least six coherent materials")
    if len(entry.get("sheets", [])) != 1:
        raise ValueError(f"{key} must remain on one atlas page")
    if not entry.get("silhouette") or not entry.get("materialStory"):
        raise ValueError(f"{key} is missing art-direction metadata")


def create_lineup_sheet(baseline: Path, candidate: Path, audit: dict, output: Path) -> None:
    """One shared scale sheet for the whole roster: eight enemies, four per block.

    An enemy with no predecessor render (R3.4's four additions) shows its own master in the baseline row,
    because there is nothing older to show; the audit's `predecessor` field records which is which.
    """
    columns = 4
    width, height = 1_900, 1_900
    canvas = canvas_base(width, height, "REGULAR ENEMIES — PREMIUM V2 LINEUP & GAMEPLAY HIERARCHY")
    draw = ImageDraw.Draw(canvas)
    text(
        draw,
        (width // 2, 77),
        "One shared scale: baseline idle, premium idle, and premium attack impact for every regular enemy.",
        18,
        anchor="ma",
        color="#AFC5BE",
    )
    row_labels = ("BASELINE", "PREMIUM IDLE", "ATTACK IMPACT", "SILHOUETTE")
    row_positions = (150, 380, 610, 840)
    baseline_keys = {
        asset["key"] for asset in read_json(baseline / "asset_manifest.json")["assets"]
    }
    for block in range((len(ENEMIES) + columns - 1) // columns):
        block_top = block * 880
        for label, y in zip(row_labels, row_positions):
            text(draw, (30, block_top + y + 100), label, 18, bold=True, anchor="lm", color="#F2D58A")
    audit_by_key = {entry["key"]: entry for entry in audit["assets"]}
    for index, (key, label, *_rest) in enumerate(ENEMIES):
        block = index // columns
        column = index % columns
        block_top = block * 880
        new = CharacterFrames(candidate, key)
        old = CharacterFrames(baseline, key) if key in baseline_keys else new
        x = 200 + column * 420
        text(draw, (x + 180, block_top + 128), label.upper(), 22, bold=True, anchor="ma")
        frames = (old.frame("idle", 0), new.frame("idle", 0), new.frame("attack", 4))
        modes = ("checker", "checker", "checker", "silhouette")
        for y, sprite, mode in zip(row_positions, frames + (frames[1],), modes):
            card = presentation_card(sprite, mode, 360, 205)
            canvas.paste(card.convert("RGB"), (x, block_top + y))
            draw.rounded_rectangle(
                (x, block_top + y, x + 360, block_top + y + 205), 10, outline="#58706A", width=2
            )
        record = audit_by_key[key]
        text(
            draw,
            (x + 180, block_top + 1302),
            f"{record['triangles']:,} triangles  •  {record['meshParts']} purposeful parts",
            15,
            anchor="ma",
            color="#AFC5BE",
        )
        text(
            draw,
            (x + 180, block_top + 1327),
            f"{record['materialCount']} coherent materials  •  25-bone animated rig",
            15,
            anchor="ma",
            color="#AFC5BE",
        )
    grade = grade_row(CharacterFrames(candidate, ENEMIES[0][0]).frame("idle", 0))
    canvas.paste(grade.convert("RGB"), ((width - grade.width) // 2, 1795))
    text(draw, ((width // 2), 1780), f"STAGE GRADE — {ENEMIES[0][1].upper()} (REPRESENTATIVE)",
         16, bold=True, anchor="ma", color="#F2D58A")
    canvas.save(output, optimize=True)


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


if __name__ == "__main__":
    main()
