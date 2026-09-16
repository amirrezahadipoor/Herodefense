from __future__ import annotations

import sys
import unittest
from pathlib import Path

BLENDER_TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(BLENDER_TOOLS))

from hd_pipeline.atlas_layout import MAX_ATLAS_SIZE, plan_grid


class AtlasLayoutTest(unittest.TestCase):
    CLIPS = {"idle": 6, "attack": 8, "hit": 4, "death": 10}

    def test_character_contract_keeps_stable_clip_rows_when_bounded(self) -> None:
        pages, regions = plan_grid(self.CLIPS, 192)

        self.assertEqual([{"index": 0, "width": 1920, "height": 768}], pages)
        self.assertEqual(0, regions["idle"][0]["y"])
        self.assertEqual(576, regions["death"][9]["y"])

    def test_boss_frames_repack_below_the_hard_page_limit(self) -> None:
        pages, regions = plan_grid(self.CLIPS, 256)

        # Phase 54: MAX 2048->4096, so legacy 10*256=2560 now fits, was 2048 before
        self.assertEqual([{"index": 0, "width": 2560, "height": 1024}], pages)
        self.assertEqual(28, sum(map(len, regions.values())))
        self.assertTrue(all(region["x"] + 256 <= MAX_ATLAS_SIZE
                            for frames in regions.values() for region in frames))

    def test_supersampled_batch_spills_deterministically_across_pages(self) -> None:
        pages, regions = plan_grid(self.CLIPS, 512)
        repeat_pages, repeat_regions = plan_grid(self.CLIPS, 512)

        # Phase 54 raised MAX_ATLAS_SIZE to 4096, so the 28-frame batch now fits on one page.
        # The contract is still exact: deterministic plan, bounded pages, frames in clip order.
        self.assertEqual(1, len(pages))
        self.assertEqual(pages, repeat_pages, "atlas plan must be deterministic")
        self.assertEqual(regions, repeat_regions, "atlas regions must be deterministic")
        self.assertTrue(all(page["width"] <= MAX_ATLAS_SIZE for page in pages))
        self.assertTrue(all(page["height"] <= MAX_ATLAS_SIZE for page in pages))
        self.assertEqual({0}, {region["page"] for frames in regions.values() for region in frames})
        self.assertEqual(list(range(10)), [frame["index"] for frame in regions["death"]])
        self.assertEqual(
            sum(len(frames) for frames in regions.values()),
            sum(self.CLIPS.values()),
            "every requested frame must be placed exactly once",
        )

    def test_rejects_a_frame_larger_than_a_page(self) -> None:
        with self.assertRaises(ValueError):
            plan_grid({"idle": 1}, 8192)


if __name__ == "__main__":
    unittest.main()
