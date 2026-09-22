#!/usr/bin/env python3
"""Guards for tools/visual/publish_runtime_tier.py — the master-to-runtime LOD step.

The tool is what turns a 384 px master render into the reviewed 192 px runtime tier, so its two failure modes
matter: composing onto a grid that is not the reviewed one, and publishing a geometry the pixels do not have.
Both are checked here on synthetic masters, because a real batch costs a 20-minute CI render per attempt.
"""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

import publish_runtime_tier as tier  # noqa: E402

CLIPS = {"idle": 2}
FRAME = 8


def master_asset(frames: list[tuple[int, int]], key: str = "test_creature") -> dict:
    return {
        "key": key,
        "frameClass": "character",
        "frameSize": FRAME * 2,
        "sheetWidth": FRAME * 4,
        "sheetHeight": FRAME * 2,
        "renderSupersample": 3,
        "renderSamples": 32,
        "engineVersion": "73.0-studio-v5-hd-pbr-4x48-pbr",
        "visualQuality": "studio-v5-hd-pbr",
        "atlas": f"sprites/{key}.atlas",
        "sheets": [{"file": f"sprites/{key}.png", "width": FRAME * 4, "height": FRAME * 2,
                    "decodedBytes": FRAME * 4 * FRAME * 2 * 4}],
        "clips": {
            "idle": [
                {"x": x * FRAME * 2, "y": y * FRAME * 2, "width": FRAME * 2, "height": FRAME * 2,
                 "index": index, "page": 0}
                for index, (x, y) in enumerate(frames)
            ]
        },
    }


def write_master(root: Path, asset: dict) -> None:
    (root / "sprites").mkdir(parents=True, exist_ok=True)
    (root / "asset_manifest.json").write_text(
        json.dumps(
            {
                "maxAtlasPageSize": 4096,
                "pipelineVersion": 4,
                "renderSupersample": 3,
                "opaqueRenderSamples": 32,
                "renderTierTop": [4, 48],
                "engineVersion": "73.0-studio-v5-hd-pbr-4x48-pbr",
                "generatedCommit": "0" * 40,
                "assets": [asset],
            }
        ),
        encoding="utf-8",
    )
    Image.new("RGBA", (asset["sheetWidth"], asset["sheetHeight"]), (10, 20, 30, 255)).save(
        root / asset["sheets"][0]["file"]
    )
    if "atlas" not in asset:
        return
    (root / asset["atlas"]).write_text(
        f"{asset['key']}.png\nsize: {asset['sheetWidth']},{asset['sheetHeight']}\nformat: RGBA8888\n"
        f"filter: Nearest,Nearest\nrepeat: none\n{asset['key']}_idle\n  rotate: false\n"
        f"  xy: 0, 0\n  size: {FRAME * 2}, {FRAME * 2}\n  orig: {FRAME * 2}, {FRAME * 2}\n"
        "  offset: 0, 0\n  index: 0\n",
        encoding="utf-8",
    )
    (root / "sprites" / f"{asset['key']}.json").write_text(json.dumps(asset), encoding="utf-8")


def reviewed_manifest(reviewed: dict) -> Path:
    directory = Path(tempfile.mkdtemp())
    path = directory / "asset_manifest.json"
    path.write_text(json.dumps({"assets": [reviewed]}), encoding="utf-8")
    return path


class PublishRuntimeTierTest(unittest.TestCase):
    def setUp(self) -> None:
        tier.RUNTIME_PAGE = (FRAME * 2, FRAME)  # a synthetic page: half of the synthetic master
        self.reviewed_grid = {
            "key": "test_creature",
            "clips": {
                "idle": [
                    {"x": 0, "y": 0, "width": FRAME, "height": FRAME, "index": 0, "page": 0},
                    {"x": FRAME, "y": 0, "width": FRAME, "height": FRAME, "index": 1, "page": 0},
                ]
            },
        }

    def test_publishes_half_geometry_and_records_the_master(self) -> None:
        with tempfile.TemporaryDirectory() as workspace:
            master_dir, runtime_dir = Path(workspace) / "master", Path(workspace) / "runtime"
            write_master(master_dir, master_asset([(0, 0), (1, 0)]))
            result = tier.publish(master_dir, runtime_dir, reviewed_manifest(self.reviewed_grid))
            entry = json.loads((runtime_dir / "asset_manifest.json").read_text())["assets"][0]
            self.assertEqual(FRAME, entry["frameSize"])
            self.assertEqual([FRAME * 2, FRAME], [entry["sheetWidth"], entry["sheetHeight"]])
            self.assertEqual(FRAME * 2 * FRAME * 4, entry["sheets"][0]["decodedBytes"])
            self.assertEqual((2, 28), (entry["renderSupersample"], entry["renderSamples"]))
            self.assertEqual("studio-v3", entry["visualQuality"])
            self.assertEqual(FRAME * 2, entry["masterRender"]["frameSize"])
            self.assertEqual([{"x": 0, "y": 0, "width": FRAME, "height": FRAME, "index": 0},
                              {"x": FRAME, "y": 0, "width": FRAME, "height": FRAME, "index": 1}],
                             [{k: frame[k] for k in ("x", "y", "width", "height", "index")}
                              for frame in entry["clips"]["idle"]])
            with Image.open(runtime_dir / entry["sheets"][0]["file"]) as sheet:
                self.assertEqual((FRAME * 2, FRAME), sheet.size)
            self.assertEqual(1, result["decodedBytes"] // (FRAME * 2 * FRAME * 4))

    def test_top_level_ships_the_reviewed_contract_and_keeps_the_master_truth(self) -> None:
        with tempfile.TemporaryDirectory() as workspace:
            master_dir, runtime_dir = Path(workspace) / "master", Path(workspace) / "runtime"
            write_master(master_dir, master_asset([(0, 0), (1, 0)]))
            tier.publish(master_dir, runtime_dir, reviewed_manifest(self.reviewed_grid))
            manifest = json.loads((runtime_dir / "asset_manifest.json").read_text())
            entry = manifest["assets"][0]
            # What ships is fingerprinted at the reviewed runtime tier, so the runtime manifest reads like the
            # other 103 committed assets and the promotion tools' fingerprint checks still mean something.
            self.assertEqual(3, manifest["pipelineVersion"])
            self.assertEqual((2, 28), (manifest["renderSupersample"], manifest["opaqueRenderSamples"]))
            self.assertEqual([3, 36], manifest["renderTierTop"])
            self.assertEqual(2_048, manifest["maxAtlasPageSize"])
            self.assertEqual("33.0-studio-v3-3x36-full", entry["engineVersion"])
            # ... and what produced it is on the record, per asset and per batch: the 3x/32 master render is not
            # hidden behind the runtime numbers.
            self.assertEqual(3, entry["masterRender"]["renderSupersample"])
            self.assertEqual(32, entry["masterRender"]["renderSamples"])
            self.assertEqual("73.0-studio-v5-hd-pbr-4x48-pbr", entry["masterRender"]["engineVersion"])
            self.assertEqual(4, manifest["masterRender"]["pipelineVersion"])
            self.assertEqual([4, 48], manifest["masterRender"]["renderTierTop"])
            self.assertEqual("73.0-studio-v5-hd-pbr-4x48-pbr", manifest["masterRender"]["engineVersion"])

    def test_a_class_that_ships_at_its_master_size_is_copied_and_says_so(self) -> None:
        # The arena's props: the reviewed tier of the environment class is the size Blender rendered, so the LOD
        # is a copy -- and halving it would produce a sheet the reviewed grid does not describe. The entry has to
        # say which of the two happened, or a reviewer cannot tell a published master from a published half.
        asset = master_asset([(0, 0)], key="crystal_prop_0")
        asset["frameClass"] = "environment"
        asset["frameSize"] = FRAME
        asset["frameWidth"] = FRAME
        asset["frameHeight"] = FRAME
        asset["sheetWidth"] = FRAME
        asset["sheetHeight"] = FRAME
        asset["sheets"] = [{"file": "environment/crystal_prop_0.png", "width": FRAME, "height": FRAME,
                            "decodedBytes": FRAME * FRAME * 4}]
        asset["clips"] = {"idle": [{"x": 0, "y": 0, "width": FRAME, "height": FRAME, "index": 0, "page": 0}]}
        asset.pop("atlas")
        master = Path(tempfile.mkdtemp())
        (master / "environment").mkdir(parents=True, exist_ok=True)
        write_master(master, asset)
        reviewed = Path(tempfile.mkdtemp())
        (reviewed / "asset_manifest.json").write_text(
            json.dumps({"assets": [{"key": "crystal_prop_0", "frameSize": FRAME,
                                    "sheets": [{"width": FRAME, "height": FRAME}],
                                    "clips": asset["clips"]}]}),
            encoding="utf-8",
        )
        runtime = Path(tempfile.mkdtemp())
        result = tier.publish(master, runtime, reviewed / "asset_manifest.json")
        self.assertEqual("master-at-reviewed-size", result["assets"][0]["geometry"])
        entry = json.loads((runtime / "asset_manifest.json").read_text(encoding="utf-8"))["assets"][0]
        self.assertEqual(FRAME, entry["frameSize"], "the reviewed tier of this class is the render size")
        self.assertIn("none: the reviewed tier", entry["masterRender"]["lod"])
        self.assertEqual(
            asset["visualQuality"], entry["visualQuality"],
            "the pixels are the batch's own, so the quality line stays the batch's own",
        )
        self.assertEqual(
            (master / "environment" / "crystal_prop_0.png").read_bytes(),
            (runtime / "environment" / "crystal_prop_0.png").read_bytes(),
            "the pixels a review accepted must be the pixels that ship",
        )

    def test_resampling_fringe_is_cleared_without_touching_the_silhouette(self) -> None:
        # A resampling kernel spreads a few percent of alpha past the geometry. That halo is invisible, but it
        # walks a frame towards its atlas cell border, so the LOD erases exactly it: nothing at 0, nothing at the
        # anti-aliased edge (4 % and up).
        halo = Image.new("RGBA", (5, 1))
        halo.putdata([(10, 10, 10, 0), (10, 10, 10, 3), (10, 10, 10, 7), (10, 10, 10, 8), (10, 10, 10, 255)])
        cleared = tier.clear_resampling_fringe(halo)
        self.assertEqual(2, cleared)
        self.assertEqual([0, 0, 0, 8, 255], [pixel[3] for pixel in halo.getdata()])

    def test_a_grid_that_is_not_the_reviewed_one_is_refused(self) -> None:
        with tempfile.TemporaryDirectory() as workspace:
            master_dir, runtime_dir = Path(workspace) / "master", Path(workspace) / "runtime"
            write_master(master_dir, master_asset([(0, 0), (1, 0)]))
            wrong = json.loads(json.dumps(self.reviewed_grid))
            wrong["clips"]["idle"][1]["x"] = FRAME * 3
            with self.assertRaises(ValueError) as caught:
                tier.publish(master_dir, runtime_dir, reviewed_manifest(wrong))
            self.assertIn("does not match the reviewed runtime grid", str(caught.exception))

    def test_an_odd_master_geometry_is_refused(self) -> None:
        with tempfile.TemporaryDirectory() as workspace:
            master_dir, runtime_dir = Path(workspace) / "master", Path(workspace) / "runtime"
            asset = master_asset([(0, 0), (1, 0)])
            asset["frameSize"] = FRAME * 2 + 1
            write_master(master_dir, asset)
            with self.assertRaises(ValueError) as caught:
                tier.publish(master_dir, runtime_dir, reviewed_manifest(self.reviewed_grid))
            self.assertIn("is not the half of an integer master value", str(caught.exception))

    def test_atlas_text_is_halved_with_the_sheet(self) -> None:
        with tempfile.TemporaryDirectory() as workspace:
            master_dir, runtime_dir = Path(workspace) / "master", Path(workspace) / "runtime"
            write_master(master_dir, master_asset([(0, 0), (1, 0)]))
            tier.publish(master_dir, runtime_dir, reviewed_manifest(self.reviewed_grid))
            text = (runtime_dir / "sprites" / "test_creature.atlas").read_text()
            self.assertIn(f"size: {FRAME * 2},{FRAME}", text)
            self.assertIn(f"  size: {FRAME}, {FRAME}", text)


if __name__ == "__main__":
    unittest.main()
