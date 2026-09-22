"""The arena lift: the exposure the display-quality pass' pixels were measured at, and nothing else's.

`ARENA_PREMIUM_V2_REVIEW.md` accepted the arena at a lifted value range, and the pixels it accepted are the ones
the game ships and the device brightness gates were pinned on. The render engine changed since, and the same
geometry came back about 40% darker (measured by `create_arena_batch_review.py`: painted mean value, transparent
padding excluded). The lift below is the one knob that puts it back, and this file is why it cannot quietly grow,
shrink, or leak onto art that was reviewed at the current engine's range.
"""
from __future__ import annotations

import os
import sys
import unittest
from pathlib import Path

BLENDER_TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(BLENDER_TOOLS))

from hd_pipeline.config import ARENA_EXPOSURE_STOPS, ARENA_LIFT_PREFIXES, arena_exposure

#: Painted mean values measured on the shipped pixels and on a local render, per stop of exposure. The audit in
#: `create_arena_batch_review.py` refuses a candidate that is more than a tenth below the shipped value.
CALIBRATION = {
    # key: (shipped, at 0.0 stops, at 0.75 stops)
    "ground_tile_0": (34.38, 18.96, 31.50),
    "ground_tile_1": (42.87, 33.48, 49.31),
    "ground_tile_2": (34.54, 19.40, 32.04),
    "crystal_prop_0": (82.21, 70.50, 86.01),
    "crystal_prop_1": (77.99, 64.72, 79.79),
    "crystal_prop_2": (68.99, 58.45, 72.99),
}


class ArenaExposureTest(unittest.TestCase):

    def tearDown(self) -> None:
        os.environ.pop("HD_ARENA_EXPOSURE_STOPS", None)

    def test_the_arena_full_frame_art_carries_the_lift(self) -> None:
        for key in ("arena_backdrop", "arena_backdrop_2", "ground_tile_0", "ground_tile_5",
                    "crystal_prop_0", "crystal_prop_3"):
            self.assertEqual(ARENA_EXPOSURE_STOPS, arena_exposure(key), key)

    def test_nothing_else_carries_it(self) -> None:
        # The cover props were authored and reviewed under the current engine's range and they ship in it; the
        # characters, bosses and icons were reviewed at zero for their whole lives.
        for key in ("obstacle_standing_stone_0", "obstacle_thorn_hedge_2", "gloom_wolf", "hero",
                    "ancient_golem", "world_tree_healthy", "ui_frame_button", "arrow_normal"):
            self.assertEqual(0.0, arena_exposure(key), key)

    def test_the_lift_clears_the_floor_on_every_calibrated_key(self) -> None:
        floor = 1.0 - 0.10
        for key, (shipped, unlit, lit) in CALIBRATION.items():
            with self.subTest(key=key):
                self.assertLess(unlit / shipped, floor, "the gate this lift answers is not vacuous")
                self.assertGreaterEqual(
                    lit / shipped, floor, f"{key} still dims at {ARENA_EXPOSURE_STOPS} stops"
                )
                self.assertLess(
                    lit / shipped, 1.20, f"{key} overshoots its reviewed range by more than a fifth"
                )

    def test_the_lift_is_one_number_between_zero_and_two_stops(self) -> None:
        self.assertGreater(ARENA_EXPOSURE_STOPS, 0.0)
        self.assertLess(ARENA_EXPOSURE_STOPS, 2.0)
        self.assertEqual(("arena_backdrop", "ground_tile_", "crystal_prop_"), ARENA_LIFT_PREFIXES)

    def test_the_calibration_override_only_reaches_the_arena(self) -> None:
        os.environ["HD_ARENA_EXPOSURE_STOPS"] = "1.4"
        self.assertEqual(1.4, arena_exposure("ground_tile_0"))
        self.assertEqual(0.0, arena_exposure("obstacle_ruin_slab_0"))

    def test_a_typo_in_the_override_renders_the_calibrated_lift_not_zero(self) -> None:
        for bad in ("", "fast", "nan", "inf"):
            with self.subTest(value=bad):
                os.environ["HD_ARENA_EXPOSURE_STOPS"] = bad
                self.assertEqual(ARENA_EXPOSURE_STOPS, arena_exposure("arena_backdrop"))


if __name__ == "__main__":
    unittest.main()
