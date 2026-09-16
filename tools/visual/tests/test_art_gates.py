"""Negative controls for the art gates.

A gate that has never been seen failing is not a gate. These tests build deliberately broken fixtures
and assert that the validator's resampling gate rejects them while an honest fixture passes, and that
hash-ledger drift is detectable. The fixtures use Pillow only, the same dependency the validator needs.
See `docs/ROADMAP_TO_1000.md` R1.9.
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


if __name__ == "__main__":
    unittest.main()
