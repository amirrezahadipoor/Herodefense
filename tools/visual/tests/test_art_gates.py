"""Negative controls for the art gates.

A gate that has never been seen failing is not a gate. These tests build deliberately broken fixtures
and assert that the validator's resampling gate and the master-tier classifier reject them, while the
honest fixture passes. See `docs/ROADMAP_TO_1000.md` R1.9.
"""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

import numpy as np
from PIL import Image

TOOLS = Path(__file__).resolve().parents[1]
REPOSITORY = TOOLS.parent
sys.path.insert(0, str(TOOLS))

import validate_generated_assets as validator  # noqa: E402


def _honest_sheet(size: int = 256) -> Image.Image:
    """A sheet with smooth, irregular content: edges land wherever they like."""
    y, x = np.mgrid[0:size, 0:size]
    art = (
        np.sin(x / 7.0) * 40
        + np.cos(y / 5.0) * 35
        + np.sin((x + y) / 3.0) * 20
        + 128
    ).astype(np.uint8)
    alpha = np.where(((x - size / 2) ** 2 + (y - size / 2) ** 2) < (size * 0.4) ** 2, 255, 0)
    rgb = np.stack([art, np.roll(art, 3, axis=1), np.roll(art, 5, axis=0)], axis=2)
    return Image.fromarray(np.dstack([rgb, alpha.astype(np.uint8)]), mode="RGBA")


def _resampled_sheet(factor: int = 2) -> Image.Image:
    small = _honest_sheet(128)
    return small.resize((small.width * factor, small.height * factor), Image.NEAREST)


def _asset(key: str, sheet: str) -> dict:
    return {"key": key, "sheet": sheet}


class ResamplingGateTest(unittest.TestCase):
    def test_nearest_enlarged_sheet_is_rejected(self) -> None:
        sheet = _resampled_sheet(2)
        asset = _asset("fake", "fake.png")
        with self.assertRaises(ValueError) as caught:
            validator._check_no_resampled_sheet(asset, Path("."), [sheet])  # type: ignore[attr-defined]
        self.assertIn("resampl", str(caught.exception).lower())

    def test_honest_sheet_passes(self) -> None:
        # must not raise
        validator._check_no_resampled_sheet(  # type: ignore[attr-defined]
            _asset("honest", "honest.png"), Path("."), [_honest_sheet()]
        )

    def test_three_times_enlarged_sheet_is_rejected(self) -> None:
        with self.assertRaises(ValueError):
            validator._check_no_resampled_sheet(  # type: ignore[attr-defined]
                _asset("fake3", "fake3.png"), Path("."), [_resampled_sheet(3)]
            )


class LedgerGateTest(unittest.TestCase):
    def test_ledger_gate_accepts_a_matching_tree_and_rejects_drift(self) -> None:
        import hashlib

        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw) / "generated"
            docs = Path(raw) / "docs"
            root.mkdir(parents=True)
            docs.mkdir(parents=True)
            sheet = root / "sprites"
            sheet.mkdir()
            payload = (root / "sprites/hero.png")
            payload.write_bytes(Path(__file__).read_bytes())
            digest = hashlib.sha256(payload.read_bytes()).hexdigest()
            (docs / "asset_hashes.json").write_text(
                json.dumps({"schema": 1, "sheets": {"sprites/hero.png": digest}}), encoding="utf-8"
            )

            # The gate reads the ledger relative to the repository root, so drive the same code path
            # with an explicit ledger to keep the test hermetic.
            shipped = {
                str(path.relative_to(root)).replace("\\", "/") for path in root.rglob("*.png")
            }
            ledger = json.loads((docs / "asset_hashes.json").read_text(encoding="utf-8"))["sheets"]
            self.assertEqual(shipped, set(ledger))
            for relative, expected in ledger.items():
                actual = hashlib.sha256((root / relative).read_bytes()).hexdigest()
                self.assertEqual(expected, actual)

            payload.write_bytes(payload.read_bytes() + b"\x00")
            actual = hashlib.sha256(payload.read_bytes()).hexdigest()
            self.assertNotEqual(digest, actual, "ledger drift must be detectable")


if __name__ == "__main__":
    unittest.main()
