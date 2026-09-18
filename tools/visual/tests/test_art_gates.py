"""Negative controls for the art gates.

A gate that has never been seen failing is not a gate. These tests build deliberately broken fixtures
and assert that the validator's resampling gate rejects them while an honest fixture passes, and that
hash-ledger drift is detectable. The fixtures use Pillow only, the same dependency the validator needs.
See `docs/RULES.md` R1.9.
"""
from __future__ import annotations

import hashlib
import json
import math
import sys
import tempfile
import unittest
from pathlib import Path

from PIL import Image, ImageDraw

TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import validate_generated_assets as validator  # noqa: E402


def _honest_sheet(size: int = 256) -> Image.Image:
    """A sheet with smooth, irregular content: edges land wherever they like."""
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    for i in range(0, size, 3):
        shade = int(128 + 100 * math.sin(i / 7.0))
        draw.line([(0, i), (size, int(i + 30 * math.sin(i / 11.0)))], fill=(shade, 255 - shade, (shade * 2) % 255, 255), width=3)
    draw.ellipse([size * 0.2, size * 0.2, size * 0.8, size * 0.8], outline=(255, 220, 120, 255), width=5)
    draw.polygon(
        [(size * 0.5, size * 0.1), (size * 0.9, size * 0.7), (size * 0.1, size * 0.7)],
        outline=(90, 200, 255, 255),
    )
    return image


def _resampled_sheet(factor: int) -> Image.Image:
    small = _honest_sheet(128)
    return small.resize((small.width * factor, small.height * factor), Image.NEAREST)


class ResamplingGateTest(unittest.TestCase):
    def test_nearest_enlarged_sheet_is_rejected(self) -> None:
        with self.assertRaises(ValueError) as caught:
            validator._check_no_resampled_sheet(  # type: ignore[attr-defined]
                {"key": "fake", "sheet": "fake.png"}, Path("."), [_resampled_sheet(2)]
            )
        self.assertIn("resampl", str(caught.exception).lower())

    def test_three_times_enlarged_sheet_is_rejected(self) -> None:
        with self.assertRaises(ValueError):
            validator._check_no_resampled_sheet(  # type: ignore[attr-defined]
                {"key": "fake3", "sheet": "fake3.png"}, Path("."), [_resampled_sheet(3)]
            )

    def test_honest_sheet_passes(self) -> None:
        validator._check_no_resampled_sheet(  # type: ignore[attr-defined]
            {"key": "honest", "sheet": "honest.png"}, Path("."), [_honest_sheet()]
        )


class LedgerGateTest(unittest.TestCase):
    def test_ledger_lists_the_shipped_tree_and_detects_drift(self) -> None:
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw) / "generated"
            docs = Path(raw) / "docs"
            sheet_dir = root / "sprites"
            sheet_dir.mkdir(parents=True)
            docs.mkdir(parents=True)
            payload_path = sheet_dir / "hero.png"
            payload_path.write_bytes(b"sheet-bytes")
            digest = hashlib.sha256(payload_path.read_bytes()).hexdigest()
            ledger_path = docs / "asset_hashes.json"
            ledger_path.write_text(
                json.dumps({"schema": 1, "sheets": {"sprites/hero.png": digest}}),
                encoding="utf-8",
            )

            shipped = {
                str(path.relative_to(root)).replace("\\", "/") for path in root.rglob("*.png")
            }
            ledger = json.loads(ledger_path.read_text(encoding="utf-8"))["sheets"]
            self.assertEqual(shipped, set(ledger), "the ledger must list exactly the shipped PNGs")
            for relative, expected in ledger.items():
                self.assertEqual(
                    expected, hashlib.sha256((root / relative).read_bytes()).hexdigest()
                )

            payload_path.write_bytes(payload_path.read_bytes() + b"\x00")
            self.assertNotEqual(
                digest,
                hashlib.sha256(payload_path.read_bytes()).hexdigest(),
                "ledger drift must be detectable",
            )


class ReviewedTierPinningTest(unittest.TestCase):
    """The manifest must describe art that exists, at a tier that was reviewed (roadmap R1.6)."""

    def test_equipment_overlays_are_pinned_to_the_overlay_tier(self) -> None:
        self.assertEqual((2, 12), validator._reviewed_tier("character", "equipment_acorn_band"))  # type: ignore[attr-defined]

    def test_top_tier_and_mid_tier_art_keep_their_rendered_settings(self) -> None:
        self.assertEqual((3, 36), validator._reviewed_tier("boss", "ancient_golem"))  # type: ignore[attr-defined]
        self.assertEqual((3, 36), validator._reviewed_tier("tree", "world_tree_healthy"))  # type: ignore[attr-defined]
        self.assertEqual((2, 28), validator._reviewed_tier("character", "rootling"))  # type: ignore[attr-defined]

    def test_raised_config_tiers_map_back_to_what_was_rendered(self) -> None:
        self.assertEqual(
            {(4, 48): (3, 36), (3, 32): (2, 28)},
            validator.REVIEWED_TIER_FOR_CONFIGURED,  # type: ignore[attr-defined]
            "the config was raised without a delivered re-render; keep the mapping explicit",
        )


if __name__ == "__main__":
    unittest.main()


class BatchReviewRegenerationTest(unittest.TestCase):
    """Roadmap R5.6: every batch the render workflow accepts has a review generator, and the workflow runs it."""

    def test_every_batch_has_a_generator_that_exists(self) -> None:
        import importlib.util

        spec = importlib.util.spec_from_file_location(
            "regenerate_batch_review", TOOLS / "regenerate_batch_review.py"
        )
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        self.assertGreaterEqual(len(module.BATCH_REVIEWS), 8)
        for batch, generator in module.BATCH_REVIEWS.items():
            self.assertTrue(
                (TOOLS / generator).is_file(),
                f"{batch} names {generator}, which does not exist",
            )

    def test_the_render_workflow_regenerates_the_review_for_its_batch(self) -> None:
        workflow = (TOOLS.parents[1] / ".github" / "workflows" / "generate-visual-assets.yml").read_text()
        self.assertIn("regenerate_batch_review.py", workflow)
        self.assertIn("${{ inputs.batch }}", workflow)


class MaterialMapGateTest(unittest.TestCase):
    """Roadmap R5.5: the material maps are re-derived, not trusted, and a drifted map is caught."""

    def test_the_shipped_material_maps_re_derive_from_their_masters(self) -> None:
        import importlib.util

        spec = importlib.util.spec_from_file_location(
            "validate_material_maps", TOOLS / "validate_material_maps.py"
        )
        module = importlib.util.module_from_spec(spec)
        sys.modules["validate_material_maps"] = module
        spec.loader.exec_module(module)
        self.assertGreaterEqual(len(module.provenance_rows()), 12)
        self.assertEqual(module.main(), 0)

    def test_a_changed_map_is_a_failure_not_a_pass(self) -> None:
        """Negative control: the gate must be able to fail. A one-pixel edit has to be caught."""
        import importlib.util
        import shutil

        spec = importlib.util.spec_from_file_location(
            "validate_material_maps_negative", TOOLS / "validate_material_maps.py"
        )
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)

        row = module.provenance_rows()[0]
        original = module.ROOT / row["path"]
        with tempfile.TemporaryDirectory() as directory:
            backup = Path(directory) / "map.png"
            shutil.copyfile(original, backup)
            try:
                with Image.open(original) as image:
                    edited = image.convert("RGBA")
                edited.putpixel((0, 0), (1, 2, 3, 255))
                edited.save(original)
                self.assertEqual(module.main(), 1, "an edited map must fail the gate")
            finally:
                shutil.copyfile(backup, original)
        self.assertEqual(module.main(), 0, "restoring the map must restore the gate")
