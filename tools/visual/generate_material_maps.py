#!/usr/bin/env python3
"""Derive normal, roughness and AO maps for the shaded entities (roadmap R5.5).

Why this file exists: the 2026-09-13 review's art complaint was that everything was a flat RGBA sprite with no
material story -- "no normal maps, no roughness, no ambient occlusion, and nothing in the repository that says
what a surface is made of". A 2D game cannot ship a PBR pipeline, but it can answer the question honestly:
these maps are derived *from the rendered masters that ship*, by a recipe that is in the repository, and their
provenance is the master they came from plus the recipe that read it.

The derivation, in full, so nothing here is a mystery:

* **Height** comes from the master's luminance, because the render lighting already encodes form: a rim-lit
  silhouette is higher than its shaded side. It is blurred slightly so the gradients describe shape rather than
  pixel noise.
* **Normal** is the gradient of that height field: x from the horizontal slope, y from the vertical slope, z from
  the surface facing the camera. Written as an ordinary tangent-space normal map (R,G,B = x,y,z remapped to
  0..255).
* **Roughness** is luma variance, inverted: smooth gradients (cloth, polished plate) read as smoother, high local
  variance (bark, fur, stone) as rougher.
* **AO** is a local contrast darkening: pixels that sit below their neighbourhood's mean luminance are considered
  occluded, which is what the render's own shading already implies.

They are generated at a quarter of the master's resolution and they live in `docs/materials/`, not in the asset
tree, on purpose. These maps exist so the repository has a material definition for every shaded entity and so a
future shading pass (Table B, R9.3) has something real to consume -- and they are deliberately *not* shipped,
because a texture the runtime does not read yet is a texture the measured APK budget should not pay for.

Usage:
    python3 tools/visual/generate_material_maps.py --write
    python3 tools/visual/generate_material_maps.py --check
"""
from __future__ import annotations

import argparse
import hashlib
import json
import pathlib
import sys

import numpy as np
from PIL import Image, ImageFilter

ROOT = pathlib.Path(__file__).resolve().parents[2]
GENERATED = ROOT / "android" / "assets" / "generated"
OUTPUT = ROOT / "docs" / "materials"
PROVENANCE = ROOT / "docs" / "art_reviews" / "MATERIAL_MAPS_PROVENANCE.md"

#: One map set per shaded entity family, taken from the keys the manifest ships.
FAMILIES = {
    "hero": ["hero"],
    "bosses": ["ancient_golem", "ember_wyrm", "thorn_matriarch", "void_knight"],
    "creatures": [
        "bark_stalker", "bramble_thrall", "fungal_brute", "gloom_wolf",
        "husk_warden", "rootling", "sap_hound", "stonekin",
    ],
}

#: A quarter of the master, rounded down to a multiple of four so the block maths stays exact.
SCALE = 4
#: How far the height field is smoothed before it is differentiated.
HEIGHT_BLUR = 1.6
#: Strength of the normal map; a flat sprite would produce a flat map, which is the honest failure mode.
NORMAL_STRENGTH = 2.4


def manifest() -> dict:
    return json.loads((GENERATED / "asset_manifest.json").read_text())


def master_files(document: dict) -> dict[str, str]:
    """The master sheet of every key this tool owns: `key` -> sheet path relative to the generated tree."""
    wanted = {key for keys in FAMILIES.values() for key in keys}
    files: dict[str, str] = {}
    for asset in document["assets"]:
        key = asset.get("key")
        if key not in wanted:
            continue
        sheets = asset.get("sheets") or []
        if sheets:
            files[key] = sheets[0]["file"]
    missing = sorted(wanted - set(files))
    if missing:
        raise SystemExit(f"no master sheet in the manifest for: {', '.join(missing)}")
    return files


def height_field(image: Image.Image) -> np.ndarray:
    luma = np.asarray(image.convert("RGBA").convert("L"), dtype=np.float32) / 255.0
    alpha = np.asarray(image.convert("RGBA"))[:, :, 3].astype(np.float32) / 255.0
    smoothed = Image.fromarray((luma * 255).astype(np.uint8)).filter(
        ImageFilter.GaussianBlur(HEIGHT_BLUR)
    )
    height = np.asarray(smoothed, dtype=np.float32) / 255.0
    # Transparent pixels are not surfaces: they are held at the field's own edge value so the gradient at the
    # silhouette stays a silhouette rather than a wall.
    return height * alpha + height.mean() * (1.0 - alpha)


def normal_map(height: np.ndarray) -> Image.Image:
    gradient_y, gradient_x = np.gradient(height)
    nx = -gradient_x * NORMAL_STRENGTH
    ny = gradient_y * NORMAL_STRENGTH
    nz = np.ones_like(height)
    length = np.sqrt(nx * nx + ny * ny + nz * nz)
    channels = [
        ((nx / length) * 0.5 + 0.5) * 255.0,
        ((ny / length) * 0.5 + 0.5) * 255.0,
        ((nz / length) * 0.5 + 0.5) * 255.0,
        np.full_like(height, 255.0),
    ]
    return Image.fromarray(np.stack(channels, axis=-1).astype(np.uint8), mode="RGBA")


def roughness_map(height: np.ndarray) -> Image.Image:
    local_mean = np.asarray(
        Image.fromarray((height * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(2.0)),
        dtype=np.float32,
    ) / 255.0
    variance = np.abs(height - local_mean) * 4.0
    roughness = np.clip(0.25 + variance, 0.0, 1.0)
    grey = (roughness * 255.0).astype(np.uint8)
    return Image.fromarray(np.stack([grey, grey, grey, np.full_like(grey, 255)], axis=-1), mode="RGBA")


def ao_map(height: np.ndarray) -> Image.Image:
    local_mean = np.asarray(
        Image.fromarray((height * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(4.0)),
        dtype=np.float32,
    ) / 255.0
    occlusion = np.clip(0.35 + 1.4 * (height - local_mean) + 0.65, 0.0, 1.0)
    grey = (occlusion * 255.0).astype(np.uint8)
    return Image.fromarray(np.stack([grey, grey, grey, np.full_like(grey, 255)], axis=-1), mode="RGBA")


def maps_for(path: pathlib.Path) -> dict[str, Image.Image]:
    with Image.open(path) as source:
        image = source.convert("RGBA")
    width = max(4, (image.width // SCALE) // 4 * 4)
    height = max(4, (image.height // SCALE) // 4 * 4)
    small = image.resize((width, height), Image.LANCZOS)
    field = height_field(small)
    return {
        "normal": normal_map(field),
        "roughness": roughness_map(field),
        "ao": ao_map(field),
    }


def sha256(path: pathlib.Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def generate(write: bool) -> list[dict]:
    document = manifest()
    entries: list[dict] = []
    for family, keys in FAMILIES.items():
        for key in keys:
            master = GENERATED / master_files(document)[key]
            for kind, image in maps_for(master).items():
                relative = f"materials/{family}/{key}_{kind}.png"
                target = OUTPUT / family / f"{key}_{kind}.png"
                if write:
                    target.parent.mkdir(parents=True, exist_ok=True)
                    image.save(target, optimize=True)
                entries.append({
                    "key": key,
                    "family": family,
                    "kind": kind,
                    "file": relative,
                    "master": f"android/assets/generated/{master_files(document)[key]}",
                    "masterSha256": sha256(master),
                    "path": str(target.relative_to(ROOT)),
                    "width": image.width,
                    "height": image.height,
                    "bytes": target.stat().st_size if target.is_file() else 0,
                    "sha256": sha256(target) if target.is_file() else "",
                })
    return entries


def render_provenance(entries: list[dict]) -> str:
    lines = [
        "# Material Map Provenance",
        "",
        "Generated by `tools/visual/generate_material_maps.py` (roadmap R5.5). The recipe and the reasoning are",
        "in that file's docstring; this document is the record of what it produced and what each map was read",
        "from.",
        "",
        "Normal, roughness and AO maps for the hero, the four bosses and the eight creatures, derived from the",
        "rendered masters that ship. They are a quarter of the master resolution on purpose: a material definition",
        "in the repository, not tens of megabytes of near-empty channels in a measured APK budget.",
        "",
        "| Entity | Family | Map | File | From master | Master SHA-256 | Map SHA-256 | Size |",
        "|---|---|---|---|---|---|---|---|",
    ]
    for entry in entries:
        lines.append(
            f"| `{entry['key']}` | {entry['family']} | {entry['kind']} | `{entry['path']}` "
            f"| `{entry['master']}` | `{entry['masterSha256'][:16]}...` | `{entry['sha256'][:16]}...` "
            f"| {entry['width']}x{entry['height']} |"
        )
    total = sum(entry["bytes"] for entry in entries)
    lines += [
        "",
        f"{len(entries)} maps, {total} bytes in total, under `docs/materials/` -- deliberately outside the shipped",
        "asset tree, so the measured APK does not carry textures nothing reads yet.",
        "",
        "`tools/visual/validate_material_maps.py` re-derives every map from its master and fails if a committed",
        "file, a master hash or a dimension disagrees with this table.",
        "",
    ]
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--write", action="store_true", help="generate the maps and the provenance document")
    parser.add_argument("--check", action="store_true", help="fail if the committed maps or document are stale")
    args = parser.parse_args()

    entries = generate(write=args.write)
    document = render_provenance(entries)

    if args.write:
        PROVENANCE.parent.mkdir(parents=True, exist_ok=True)
        PROVENANCE.write_text(document, encoding="utf-8")
        print(f"wrote {len(entries)} maps and {PROVENANCE.relative_to(ROOT)}")
        return 0

    if args.check:
        committed = PROVENANCE.read_text(encoding="utf-8") if PROVENANCE.is_file() else ""
        if committed != document:
            print("material maps or their provenance are out of date: run with --write")
            return 1
        print(f"{len(entries)} material maps match the shipped masters")
        return 0

    sys.stdout.write(document)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
