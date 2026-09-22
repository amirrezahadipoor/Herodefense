"""The arena's luminance parity gate: a render may not ship the display-quality pass back down.

`ARENA_PREMIUM_V2_REVIEW.md` accepted a batch whose whole reason for existing was lifting the arena's value range
so text and actors read on real panels. The next arena render came back measurably darker than the tier it would
replace -- the backdrop by 40 % -- because the master engine's lighting is not the engine that produced the
accepted tier. Nothing in the pipeline said so: every existing gate passed, and the batch was one promotion away
from shipping a dimmer game behind a green workflow.

These tests hold the rule in place:

* both arena review tools carry the tolerance and the escaped hatch (`--allow-parity-failure`), so a batch that
  fails parity can still be recorded as *held* rather than silently not recorded at all;
* the batch tool measures painted mean value against the committed baseline for every key it renders;
* `painted_mean_value` measures the pixels a player sees -- the transparent margin is excluded -- which is the
  difference between a landmark that gained padding and a landmark that lost light.
"""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

from PIL import Image

VISUAL = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(VISUAL))

import create_arena_batch_review
import create_arena_crystal_review

BATCH_TOOL = VISUAL / "create_arena_batch_review.py"
CRYSTAL_TOOL = VISUAL / "create_arena_crystal_review.py"


class ArenaLuminanceParityTest(unittest.TestCase):

    def test_both_arena_tools_state_the_tolerance_and_the_held_flag(self) -> None:
        for path in (BATCH_TOOL, CRYSTAL_TOOL):
            source = path.read_text(encoding="utf-8")
            self.assertIn("MAXIMUM_PAINTED_VALUE_LOSS = 0.10", source, path.name)
            self.assertIn("--allow-parity-failure", source, path.name)
            self.assertIn("painted_mean_value", source, path.name)

    def test_the_tolerance_is_a_tenth_and_the_two_tools_agree(self) -> None:
        self.assertEqual(0.10, create_arena_crystal_review.MAXIMUM_PAINTED_VALUE_LOSS)
        self.assertEqual(1.0 - 0.10, 1.0 - create_arena_batch_review.MAXIMUM_PAINTED_VALUE_LOSS)

    def test_the_batch_tool_measures_every_key_against_the_committed_baseline(self) -> None:
        source = BATCH_TOOL.read_text(encoding="utf-8")
        self.assertIn("luminance_against_baseline(baseline, baseline_entries, key, image)", source)
        # Twice: once on the backdrop branch and once on the props branch. A gate that only covers the backdrop
        # would let a batch ship dark ground and dark landmarks under a bright sky.
        self.assertEqual(2, source.count("luminance_against_baseline(baseline, baseline_entries, key, image)"))
        self.assertIn("enforce_parity(key, record[\"luminance\"], allow_parity_failure)", source)

    def test_painted_mean_ignores_the_transparent_margin(self) -> None:
        def painted(colour: tuple[int, int, int], border: int) -> Image.Image:
            image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
            for y in range(border, 64 - border):
                for x in range(border, 64 - border):
                    image.putpixel((x, y), (*colour, 255))
            return image

        tight = painted((200, 200, 200), 2)
        roomy = painted((200, 200, 200), 20)
        self.assertAlmostEqual(
            create_arena_crystal_review.painted_mean_value(tight),
            create_arena_crystal_review.painted_mean_value(roomy),
            places=3,
            msg="padding is not light: the same pixels in a smaller frame must measure the same",
        )

    def test_a_dimmed_prop_is_caught_and_a_brightened_one_is_not(self) -> None:
        def mono(value: int) -> Image.Image:
            image = Image.new("RGBA", (32, 32), (value, value, value, 255))
            return image

        shipped = mono(120)
        dimmed = mono(100)
        shipped_mean = create_arena_crystal_review.painted_mean_value(shipped)
        dimmed_mean = create_arena_crystal_review.painted_mean_value(dimmed)
        self.assertLess(dimmed_mean / shipped_mean, 1.0 - create_arena_crystal_review.MAXIMUM_PAINTED_VALUE_LOSS)
        self.assertGreater(
            create_arena_crystal_review.painted_mean_value(mono(140)) / shipped_mean,
            1.0 - create_arena_crystal_review.MAXIMUM_PAINTED_VALUE_LOSS,
        )


if __name__ == "__main__":
    unittest.main()
