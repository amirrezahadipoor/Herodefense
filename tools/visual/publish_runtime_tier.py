#!/usr/bin/env python3
"""Publish the reviewed runtime tier from a master render batch.

Why this exists: the Blender pipeline renders premium characters onto 384 px frames (sheet 3840x1536), while
the reviewed, shipped tier is 192 px frames on a 1920x768 page — exactly half, same grid, which is how Phase
78 recovered the runtime tier. A fresh render batch therefore cannot be promoted on its own: the master has to
be downsampled onto the reviewed runtime geometry first, and that step only existed inside the one-off recovery
script. This is that step, repeatable and checked:

    python3 tools/visual/publish_runtime_tier.py MASTER_DIR RUNTIME_DIR

* every frame is cropped from the master and resized with LANCZOS into the runtime rect, using
  `restore_runtime_tier.compose_runtime_sheet` — the same algorithm that produced the shipped tier, imported
  rather than re-implemented;
* for a key the reviewed manifest already covers, the halved master grid must equal the reviewed grid exactly.
  A mismatch is an error, never a silent restamp;
* for a key it does not cover, the runtime layout is the halved master grid, accepted only if the page fits the
  reviewed page size and the frame contract matches (one page, the same clips, at least one frame each);
* the sheet, the libGDX atlas text, the per-asset metadata and the manifest entry are rewritten together, so
  nothing can describe a geometry the pixels do not have;
* the entry is stamped with the reviewed tier pin for its frame class (the validator refuses anything else),
  and the master tier and engine line are recorded next to it as `masterRender`, because that is the truth
  about where the pixels came from.

See docs/RULES.md R3.4 and docs/art_reviews/ENEMIES_PREMIUM_V2_REVIEW.md.
"""
from __future__ import annotations

import argparse
import json
import shutil
import re
import sys
from pathlib import Path

from PIL import Image

REPOSITORY = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(Path(__file__).resolve().parent))

from restore_runtime_tier import compose_runtime_sheet  # noqa: E402  (same LOD algorithm as the shipped tier)
from validate_generated_assets import _reviewed_tier  # noqa: E402  (the pinned reviewed tiers)

RUNTIME_PAGE = (1_920, 768)
REVIEWED_VISUAL_QUALITY = "studio-v3"

# The reviewed runtime-tier contract, as every committed asset in android/assets/generated carries it: the tier
# top the shipping tier was fingerprinted at, the page limit the sheets live inside, and the engine line the
# runtime LOD belongs to. The master batch's own numbers are not overwritten -- they move into `masterRender`.
# Alpha at or below this is resampling fringe rather than silhouette: the LANCZOS kernel spreads a 3 %-opacity
# halo one or two pixels past the geometry, which is invisible in play but would let a frame creep towards its
# atlas cell border and trip the frame-border gate for no visible reason. The LOD clears it.
FRINGE_ALPHA_FLOOR = 8

RUNTIME_TIER_TOP = (3, 36)
RUNTIME_PAGE_LIMIT = 2_048
RUNTIME_ENGINE_VERSION = "33.0-studio-v3-3x36-full"
RUNTIME_PIPELINE_VERSION = 3


def halve(value: int, label: str) -> int:
    if value % 2:
        raise ValueError(f"{label}: {value} is not the half of an integer master value")
    return value // 2


def half_frame(frame: dict, label: str) -> dict:
    half = {
        "x": halve(int(frame["x"]), f"{label} x"),
        "y": halve(int(frame["y"]), f"{label} y"),
        "width": halve(int(frame["width"]), f"{label} width"),
        "height": halve(int(frame["height"]), f"{label} height"),
        "index": int(frame["index"]),
    }
    for optional in ("page",):
        if optional in frame:
            half[optional] = int(frame[optional])
    return half


def half_clips(master: dict, label: str) -> dict:
    return {
        clip: [half_frame(frame, f"{label}/{clip}[{frame['index']}]") for frame in frames]
        for clip, frames in master["clips"].items()
    }


def reviewed_layout(reviewed_asset: dict, master_asset: dict) -> dict:
    """The runtime rects to compose into, and the reason we are allowed to use them."""

    label = master_asset["key"]
    master_clips = master_asset["clips"]
    reviewed_clips = reviewed_asset["clips"]
    if set(master_clips) != set(reviewed_clips):
        raise ValueError(f"{label}: master clips {sorted(master_clips)} != reviewed {sorted(reviewed_clips)}")
    for clip, frames in master_clips.items():
        if len(frames) != len(reviewed_clips[clip]):
            raise ValueError(
                f"{label}/{clip}: master has {len(frames)} frames, reviewed has {len(reviewed_clips[clip])}"
            )
    halved = half_clips(master_asset, label)

    def fingerprint(frames: list[dict]) -> list[tuple[int, int, int, int, int]]:
        return sorted(
            (int(frame["x"]), int(frame["y"]), int(frame["width"]), int(frame["height"]), int(frame["index"]))
            for frame in frames
        )

    actual = {clip: fingerprint(frames) for clip, frames in halved.items()}
    expected = {clip: fingerprint(frames) for clip, frames in reviewed_clips.items()}
    if actual != expected:
        differing = sorted(clip for clip in expected if actual[clip] != expected[clip])
        raise ValueError(
            f"{label}: halved master grid does not match the reviewed runtime grid for {differing[:3]}"
        )
    # The reviewed rects are the runtime geometry, ordered by frame index: this is what gets composed into.
    ordered = {
        clip: sorted(
            (
                {
                    "x": int(frame["x"]),
                    "y": int(frame["y"]),
                    "width": int(frame["width"]),
                    "height": int(frame["height"]),
                    "index": int(frame["index"]),
                    "page": int(frame.get("page", 0)),
                }
                for frame in frames
            ),
            key=lambda frame: frame["index"],
        )
        for clip, frames in reviewed_clips.items()
    }
    return ordered


def derived_layout(master_asset: dict, page_limit: int) -> dict:
    """A new key has no reviewed grid yet: halve the master, then check the result is a legal runtime page."""

    label = master_asset["key"]
    if int(master_asset["frameSize"]) % 2:
        raise ValueError(f"{label}: master frame size {master_asset['frameSize']} cannot be halved")
    clips = half_clips(master_asset, label)
    runtime_page = (halve(int(master_asset["sheetWidth"]), f"{label} sheet width"),
                    halve(int(master_asset["sheetHeight"]), f"{label} sheet height"))
    if runtime_page != RUNTIME_PAGE:
        raise ValueError(f"{label}: runtime page {runtime_page} is not the reviewed page {RUNTIME_PAGE}")
    if max(runtime_page) > page_limit:
        raise ValueError(f"{label}: runtime page {runtime_page} exceeds the atlas page limit {page_limit}")
    frames = [frame for clip in clips.values() for frame in clip]
    if not frames:
        raise ValueError(f"{label}: master has no frames")
    frame_size = halve(int(master_asset["frameSize"]), f"{label} frame size")
    for frame in frames:
        if frame["x"] + frame["width"] > runtime_page[0] or frame["y"] + frame["height"] > runtime_page[1]:
            raise ValueError(f"{label}: frame {frame} escapes the runtime page")
        if (frame["width"], frame["height"]) != (frame_size, frame_size):
            raise ValueError(f"{label}: frame {frame} is not a {frame_size}px cell")
    return clips


def half_atlas(text: str, label: str) -> str:
    """libGDX atlas text: page size, per-frame xy/size/orig/offset are master pixels.

    Numbers are replaced in place so the committed formatting survives byte for byte.
    """

    def halve_numbers(payload: str, what: str) -> str:
        return re.sub(
            r"-?\d+",
            lambda match: str(halve(int(match.group()), f"{label} atlas {what}")),
            payload,
        )

    out: list[str] = []
    for line in text.splitlines():
        stripped = line.strip()
        if stripped.startswith("size:") and not line.startswith(" "):
            out.append("size: " + halve_numbers(stripped.split(":", 1)[1].strip(), "page size"))
        elif stripped.startswith(("xy:", "size:", "orig:", "offset:")):
            field, rest = stripped.split(":", 1)
            out.append(f"  {field}: " + halve_numbers(rest.strip(), field))
        else:
            out.append(line)
    return "\n".join(out) + "\n"


def clear_resampling_fringe(image: Image.Image, floor: int = FRINGE_ALPHA_FLOOR) -> int:
    """Erase the sub-visible alpha a resampling kernel spreads outside the silhouette.

    Returns the number of pixels cleared, so the publisher can report how much fringe a batch carried. Only
    pixels strictly below ``floor`` are touched: the anti-aliased edge of the silhouette itself is 4 % and up.
    """
    alpha = image.getchannel("A")
    mask = alpha.point(lambda value: 255 if 0 < value < floor else 0)
    cleared = mask.histogram()[255]
    if cleared:
        image.paste((0, 0, 0, 0), (0, 0), mask)
    return cleared


def runtime_entry(
    master_asset: dict,
    runtime_clips: dict,
    runtime_page: tuple[int, int],
    master_engine_version: str | None = None,
) -> dict:
    entry = dict(master_asset)
    frame_size = halve(int(master_asset["frameSize"]), f"{master_asset['key']} frame size")
    entry["clips"] = runtime_clips
    entry["frameSize"] = frame_size
    entry["sheetWidth"], entry["sheetHeight"] = runtime_page
    entry["sheets"] = [
        {
            "file": sheet["file"],
            "width": runtime_page[0],
            "height": runtime_page[1],
            "decodedBytes": runtime_page[0] * runtime_page[1] * 4,
        }
        for sheet in master_asset["sheets"]
    ]
    if len(entry["sheets"]) != 1:
        raise ValueError(f"{master_asset['key']}: the reviewed runtime tier is a single atlas page")
    entry["renderSupersample"], entry["renderSamples"] = _reviewed_tier(
        master_asset["frameClass"], master_asset["key"]
    )
    entry["visualQuality"] = REVIEWED_VISUAL_QUALITY
    # engineVersion names the reviewed runtime-tier line, exactly as the other 103 committed assets name it; the
    # real render provenance of this LOD is kept next to it rather than thrown away.
    entry["engineVersion"] = RUNTIME_ENGINE_VERSION
    entry["masterRender"] = {
        "frameSize": int(master_asset["frameSize"]),
        "sheetWidth": int(master_asset["sheetWidth"]),
        "sheetHeight": int(master_asset["sheetHeight"]),
        "renderSupersample": int(master_asset["renderSupersample"]),
        "renderSamples": int(master_asset["renderSamples"]),
        "engineVersion": master_asset.get("engineVersion") or master_engine_version,
        "lod": "lanczos-half onto the reviewed runtime grid",
    }
    return entry


def ships_at_master_size(master_asset: dict, reviewed_asset: dict | None = None) -> bool:
    """True when this key is already the size it ships at, so the LOD is a copy rather than a halving.

    The environment class ships the pixels Blender rendered: a prop is one frame on a sheet the size of that
    frame (the arena's backdrop, its ground tiles, its landmarks and its cover), and the reviewed catalog carries
    them at the rendered size. The atlas classes (characters, bosses, the tree) are the ones whose masters are
    twice the reviewed tier and whose sheets are many frames on a page, and they take the halving path.

    The rule reads the master alone, because a batch that renders a key the reviewed catalog does not carry yet
    -- the arena's cover families were exactly that -- has no reviewed entry to compare against, and a new key
    must not be forced through a page composition it was never authored for.
    """
    sheets = master_asset.get("sheets") or [{}]
    sheet = sheets[0]
    frames = sum(len(frames) for frames in (master_asset.get("clips") or {}).values())
    if frames != 1:
        return False
    # An atlas entry describes its grid with frameSize and clips alone; only a static render states its frame
    # width and height, and only a static render is a candidate for this path.
    if "frameWidth" not in master_asset or "frameHeight" not in master_asset:
        return False
    if int(master_asset["sheetWidth"]) != int(master_asset["frameWidth"]):
        return False
    if int(master_asset["sheetHeight"]) != int(master_asset["frameHeight"]):
        return False
    if int(sheet.get("width", 0)) != int(master_asset["frameWidth"]):
        return False
    if int(sheet.get("height", 0)) != int(master_asset["frameHeight"]):
        return False
    if reviewed_asset is None:
        return True
    # A reviewed entry that disagrees about the size means this class halves, whatever the sheet looks like.
    return (
        int(reviewed_asset.get("frameSize", 0)) == int(master_asset["frameSize"])
        and (reviewed_asset.get("sheets") or [{}])[0].get("width") == int(master_asset["sheetWidth"])
        and (reviewed_asset.get("sheets") or [{}])[0].get("height") == int(master_asset["sheetHeight"])
    )


def reviewed_grid(asset: dict) -> str:
    return json.dumps(_ordered_clips(asset), sort_keys=True)


def master_grid(asset: dict) -> str:
    return json.dumps(_ordered_clips(asset), sort_keys=True)


def _ordered_clips(asset: dict) -> dict:
    return {
        clip: sorted(frames, key=lambda frame: int(frame["index"]))
        for clip, frames in (asset.get("clips") or {}).items()
    }


def publish_at_master_size(
    master_dir: Path, runtime_dir: Path, master_asset: dict, master_engine_version: str | None
) -> dict:
    """Copy a master that is already the reviewed tier, and say so in the entry.

    No resampling happens, so no resampling fringe exists and the pixels are copied byte for byte: the review
    behind these files judged exactly these pixels, and anything this step did to them would make that judgement
    about a different image.
    """
    sheet = master_asset["sheets"][0]
    target = runtime_dir / sheet["file"]
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(master_dir / sheet["file"], target)
    if master_asset.get("atlas"):
        atlas = master_asset["atlas"]
        (runtime_dir / atlas).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(master_dir / atlas, runtime_dir / atlas)
    if master_asset.get("sourcePath"):
        metadata_source = master_dir / "sprites" / f"{master_asset['key']}.json"
        if metadata_source.is_file():
            (runtime_dir / "sprites").mkdir(parents=True, exist_ok=True)
            shutil.copy2(metadata_source, runtime_dir / "sprites" / f"{master_asset['key']}.json")

    entry = dict(master_asset)
    entry["renderSupersample"], entry["renderSamples"] = _reviewed_tier(
        master_asset["frameClass"], master_asset["key"]
    )
    entry["visualQuality"] = REVIEWED_VISUAL_QUALITY
    entry["engineVersion"] = RUNTIME_ENGINE_VERSION
    entry["masterRender"] = {
        "frameSize": int(master_asset["frameSize"]),
        "sheetWidth": int(master_asset["sheetWidth"]),
        "sheetHeight": int(master_asset["sheetHeight"]),
        "renderSupersample": int(master_asset["renderSupersample"]),
        "renderSamples": int(master_asset["renderSamples"]),
        "engineVersion": master_asset.get("engineVersion") or master_engine_version,
        "lod": "none: the reviewed tier of this frame class is the size it was rendered at",
    }
    metadata = master_dir / "environment" / f"{master_asset['key']}.json"
    if metadata.is_file():
        payload = json.loads(metadata.read_text(encoding="utf-8"))
        for field in ("renderSupersample", "renderSamples", "visualQuality", "engineVersion", "masterRender"):
            payload[field] = entry[field]
        target_metadata = runtime_dir / "environment" / f"{master_asset['key']}.json"
        target_metadata.parent.mkdir(parents=True, exist_ok=True)
        target_metadata.write_text(json.dumps(payload, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return entry


def publish(master_dir: Path, runtime_dir: Path, reviewed_manifest_path: Path) -> dict:
    master_manifest = json.loads((master_dir / "asset_manifest.json").read_text(encoding="utf-8"))
    reviewed_manifest = json.loads(reviewed_manifest_path.read_text(encoding="utf-8"))
    reviewed_by_key = {asset["key"]: asset for asset in reviewed_manifest["assets"]}
    page_limit = int(master_manifest["maxAtlasPageSize"])

    runtime_dir.mkdir(parents=True, exist_ok=True)
    (runtime_dir / "sprites").mkdir(parents=True, exist_ok=True)
    entries: list[dict] = []
    report: list[dict] = []

    for master_asset in master_manifest["assets"]:
        key = master_asset["key"]
        reviewed_asset = reviewed_by_key.get(key)
        if ships_at_master_size(master_asset, reviewed_asset):
            # This class ships the pixels Blender rendered: the copy is the LOD, and the entry says so.
            (runtime_dir / (master_asset["sheets"][0]["file"])).parent.mkdir(parents=True, exist_ok=True)
            entries.append(publish_at_master_size(
                master_dir, runtime_dir, master_asset, master_manifest.get("engineVersion")
            ))
            report.append({
                "key": key, "geometry": "master-at-reviewed-size", "frameSize": int(master_asset["frameSize"]),
                "frames": sum(len(frames) for frames in master_asset["clips"].values()),
                "fringePixelsCleared": 0,
            })
            continue
        if reviewed_asset is not None:
            clips = reviewed_layout(reviewed_asset, master_asset)
            geometry = "reviewed-grid"
        else:
            clips = derived_layout(master_asset, page_limit)
            geometry = "halved-master-grid"
        frame_size = halve(int(master_asset["frameSize"]), f"{key} frame size")
        runtime_page = RUNTIME_PAGE
        # Both sides are ordered by frame index before composing: `compose_runtime_sheet` walks the two lists
        # in parallel, so an ordering difference would paste frames into the wrong cells.
        ordered_master = dict(master_asset)
        ordered_master["clips"] = {
            clip: sorted(frames, key=lambda frame: int(frame["index"]))
            for clip, frames in master_asset["clips"].items()
        }
        with Image.open(master_dir / master_asset["sheets"][0]["file"]) as handle:
            master_image = handle.convert("RGBA")
        if master_image.size != (int(master_asset["sheetWidth"]), int(master_asset["sheetHeight"])):
            raise ValueError(f"{key}: master sheet pixels {master_image.size} disagree with its metadata")
        sheet = compose_runtime_sheet(
            master_image,
            ordered_master,
            {
                "key": key,
                "sheets": [{"width": runtime_page[0], "height": runtime_page[1]}],
                "clips": clips,
            },
        )
        fringe = clear_resampling_fringe(sheet)
        sheet.save(runtime_dir / master_asset["sheets"][0]["file"], optimize=True)
        atlas_text = (master_dir / master_asset["atlas"]).read_text(encoding="utf-8")
        (runtime_dir / master_asset["atlas"]).write_text(half_atlas(atlas_text, key), encoding="utf-8")
        metadata = json.loads((master_dir / "sprites" / f"{key}.json").read_text(encoding="utf-8"))
        entry = runtime_entry(master_asset, clips, runtime_page, master_manifest.get("engineVersion"))
        for field in ("clips", "frameSize", "sheetWidth", "sheetHeight", "sheets", "renderSupersample",
                      "renderSamples", "visualQuality", "engineVersion", "masterRender"):
            metadata[field] = entry[field]
        (runtime_dir / "sprites" / f"{key}.json").write_text(
            json.dumps(metadata, indent=2, sort_keys=True) + "\n", encoding="utf-8"
        )
        entries.append(entry)
        report.append({"key": key, "geometry": geometry, "frameSize": frame_size,
                       "frames": sum(len(frames) for frames in clips.values()),
                       "fringePixelsCleared": fringe})

    runtime_manifest = dict(master_manifest)
    runtime_manifest["assets"] = entries
    runtime_manifest["decodedBytes"] = sum(sheet["decodedBytes"] for entry in entries for sheet in entry["sheets"])
    master_truth = {
        "pipelineVersion": master_manifest.get("pipelineVersion"),
        "engineVersion": master_manifest.get("engineVersion"),
        "frameSize": int(master_manifest["assets"][0]["frameSize"]),
        "sheet": [
            int(master_manifest["assets"][0]["sheetWidth"]),
            int(master_manifest["assets"][0]["sheetHeight"]),
        ],
        "renderSupersample": master_manifest.get("renderSupersample"),
        "renderSamples": master_manifest.get("opaqueRenderSamples"),
        "renderTierTop": master_manifest.get("renderTierTop"),
        "maxAtlasPageSize": master_manifest.get("maxAtlasPageSize"),
        "generatedCommit": master_manifest.get("generatedCommit"),
        "generatedAt": master_manifest.get("generatedAt"),
        "lod": "lanczos-half onto the reviewed runtime grid",
    }
    runtime_manifest["masterRender"] = master_truth
    # Top-level mirrors the per-asset convention: the shipping tier's fingerprint, with the render that produced
    # it preserved under `masterRender` above. Bucketing both would make the runtime manifest disagree with the
    # reviewed tier every other committed asset is fingerprinted at.
    tiers = {(entry["renderSupersample"], entry["renderSamples"]) for entry in entries}
    if len(tiers) != 1:
        raise ValueError(f"runtime tier publishes one reviewed tier per batch, found {sorted(tiers)}")
    # pipelineVersion follows the same convention as engineVersion: the committed catalog is fingerprinted at the
    # reviewed line's pipeline 3 (PremiumAssetContractTest asserts it), while the render that produced this LOD is
    # recorded in `masterRender` -- R3.4's master batch is pipeline 4.
    runtime_manifest["pipelineVersion"] = RUNTIME_PIPELINE_VERSION
    runtime_manifest["renderSupersample"], runtime_manifest["opaqueRenderSamples"] = tiers.pop()
    runtime_manifest["renderTierTop"] = list(RUNTIME_TIER_TOP)
    runtime_manifest["maxAtlasPageSize"] = RUNTIME_PAGE_LIMIT
    runtime_manifest["runtimeTierPublishedFrom"] = {
        "masterFrameSize": master_manifest["assets"][0]["frameSize"],
        "masterSheet": [master_manifest["assets"][0]["sheetWidth"], master_manifest["assets"][0]["sheetHeight"]],
        "page": list(RUNTIME_PAGE),
        "note": "runtime LOD of the master render batch; see docs/art_reviews/ENEMIES_PREMIUM_V2_REVIEW.md",
    }
    (runtime_dir / "asset_manifest.json").write_text(
        json.dumps(runtime_manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    return {"assets": report, "decodedBytes": runtime_manifest["decodedBytes"]}


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("master", type=Path)
    parser.add_argument("runtime", type=Path)
    parser.add_argument(
        "--reviewed-manifest",
        type=Path,
        default=REPOSITORY / "android" / "assets" / "generated" / "asset_manifest.json",
    )
    args = parser.parse_args()
    if args.master.resolve() == args.runtime.resolve():
        raise ValueError("master and runtime directories must differ")
    result = publish(args.master.resolve(), args.runtime.resolve(), args.reviewed_manifest.resolve())
    for asset in result["assets"]:
        print(f"{asset['key']}: {asset['geometry']} {asset['frameSize']}px x{asset['frames']} frames")
    print(f"runtime tier: {len(result['assets'])} assets, {result['decodedBytes']} decoded bytes")


if __name__ == "__main__":
    main()
