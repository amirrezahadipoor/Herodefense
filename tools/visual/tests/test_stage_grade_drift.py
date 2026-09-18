"""The runtime stage grade and the review strips are the same recipe (roadmap R5.4).

Every review sheet is stamped with the four stage grades from `review_strips.STAGE_GRADES`, and the game now
renders the arena under the same arc (`core/.../render/StageGrade.java`). Two files, one recipe: this test reads
both and fails if they ever disagree, because a strip that shows a grade the game does not render is a claim
this repository does not get to make.
"""
from __future__ import annotations

import re
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "visual"))

import review_strips  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
GRADE = ROOT / "core" / "src" / "main" / "java" / "com" / "amirrezahadipoor" / "herodefense" / "render" / "StageGrade.java"


class StageGradeDriftTest(unittest.TestCase):
    def test_the_runtime_stops_are_the_review_recipe(self) -> None:
        source = GRADE.read_text()
        block = source.split("private static final float[][] STOPS = {")[1].split("};")[0]
        runtime = [
            tuple(float(value.strip().rstrip("f")) for value in row.split(","))
            for row in re.findall(r"\{([-0-9.f, ]+)\}", block)
        ]
        strips = [tuple((*multipliers, lift)) for _, multipliers, lift in review_strips.STAGE_GRADES]
        # The tooling's first two stops are a BASE row and DAWN; the runtime arc starts at DAWN.
        graded_strips = strips[1:]
        self.assertEqual(len(graded_strips), len(runtime), "the number of stage stops differs")
        for index, (strip, runtime_stop) in enumerate(zip(graded_strips, runtime)):
            for value, expected in zip(runtime_stop, strip):
                self.assertAlmostEqual(expected, value, places=4,
                                       msg=f"stop {index} channel {value} vs the strip's {expected}")

    def test_the_names_match_the_review_strips(self) -> None:
        source = GRADE.read_text()
        names = re.findall(r'"([A-Z]+ \d+-\d+)"', source)
        strip_names = [name for name, _, _ in review_strips.STAGE_GRADES]
        self.assertEqual(strip_names[1:], names)


if __name__ == "__main__":
    unittest.main()
