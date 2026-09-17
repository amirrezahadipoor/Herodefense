"""What the colour search is worth, measured against an oracle instead of against itself.

A round trip only says the encoder and the decoder agree; it says nothing about whether the encoder chose well.
These tests hold the search to the best answer inside its own search space, brute-forced here over every base
colour the individual mode can carry (sixteen levels per channel, all eight modifier tables, both flips), and they
hold the encoder's whole-sheet answer to a quality floor on an image whose content is known.

The comparison that matters is against `per_channel_error`: choosing each channel's base level on that channel's
own error, then letting the shared two-bit index carry all three, is what this encoder did first, and it left
five decibels on the table against Google's ETC1 reference on the shipped sheets. That choice is what these tests
would catch coming back.
"""
from __future__ import annotations

import pathlib
import sys
import unittest

import numpy as np

TOOLS = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import etc2  # noqa: E402

#: Floors, both measured on 2026-09-17 with the joint base search at its shipped width. The synthetic sheet is
#: adversarial for ETC -- its red ramps down the rows while its green ramps across the columns, and a block's
#: single index is shared by three channels, so it cannot follow both -- and it measures 28.76 dB. The shipped
#: sheet is the measurement that matters: `rootling`'s first 256 columns and rows measure 34.26 dB over the pixels
#: that show. Each floor sits about half a decibel under its measurement, so an unrelated improvement cannot fail
#: it and a regression cannot pass it.
SMOOTH_SHEET_FLOOR_DB = 28.0
SHIPPED_SHEET_FLOOR_DB = 33.5

#: How far above the brute-forced optimum this encoder's local search is allowed to land on the block below.
#: Measured on 2026-09-17: the exhaustive optimum is a weighted error of 257, this encoder leaves 279, and the
#: per-channel search it replaced left 9217. The margin is stated rather than assumed, and it is the price of
#: searching the levels around the mean instead of all sixteen of them.
OPTIMUM_MARGIN = 1.15
#: And how much worse the per-channel choice has to stay for the block to keep proving anything.
PER_CHANNEL_FLOOR = 20.0
SHIPPED_SHEET = "sprites/rootling.png"

#: k = 4x + y: the k-th entry of a half is the pixel whose index the block layout stores at that bit position.
XS = np.array([k // 4 for k in range(16)])
YS = np.array([k % 4 for k in range(16)])


def smooth_sheet(size: int = 64) -> np.ndarray:
    """A deterministic sheet with gradients, a soft disc and a hard bar: what a sprite sheet looks like."""
    image = np.zeros((size, size, 4), dtype=np.uint8)
    rows, columns = np.mgrid[0:size, 0:size]
    image[..., 0] = (rows * 255 // max(size - 1, 1)).astype(np.uint8)
    image[..., 1] = (columns * 255 // max(size - 1, 1)).astype(np.uint8)
    image[..., 2] = ((rows + columns) * 127 // max(2 * (size - 1), 1)).astype(np.uint8)
    image[..., 3] = 255
    centre = (size - 1) / 2.0
    disc = np.hypot(rows - centre, columns - centre) < size * 0.28
    image[disc, 0] = 200
    image[disc, 1] = 90
    image[disc, 2] = 40
    image[size // 2: size // 2 + 3, :, 1] = 255
    return image


def flat_block(block: np.ndarray) -> np.ndarray:
    return block[YS, XS, :3].astype(np.int64)


def best_half_error(half: np.ndarray) -> int:
    """Every base the individual mode can carry for these eight pixels, every table, scored as the block decodes."""
    levels = np.arange(16, dtype=np.int64) * 17
    bases = np.stack(np.meshgrid(levels, levels, levels, indexing="ij"), axis=-1).reshape(-1, 3)
    best = np.inf
    for table in range(8):
        modifiers = np.array(etc2.MODIFIER_TABLES[table], dtype=np.int64)
        decoded = np.clip(bases[:, None, :] + modifiers[None, :, None], 0, 255)      # (bases, 4, 3)
        error = ((half[None, None, :, :] - decoded[:, :, None, :]) ** 2).sum(axis=3)  # (bases, 4, pixels)
        best = min(best, int(error.min(axis=1).sum(axis=1).min()))
    return best


def oracle_error(block: np.ndarray) -> int:
    """The best error *any* individual-mode encoding of this block can leave, brute-forced over both flips."""
    flat = flat_block(block)
    best = np.inf
    for flip in (0, 1):
        first = (XS < 2) if flip == 0 else (YS < 2)
        best = min(best, best_half_error(flat[first]) + best_half_error(flat[~first]))
    return best


def per_channel_error(block: np.ndarray) -> float:
    """What the first version of the search left behind, reproduced exactly.

    It chose every channel's base level on that channel's own error -- scoring each level by the best index *that
    channel* could take -- summed those three numbers and kept the table with the smallest sum. Then one shared
    index was written for all three channels, so the block it had scored as the best was often not the block it
    produced. This function keeps the same selection rule and then measures what it actually produced.
    """
    flat = flat_block(block)
    best = np.inf
    for flip in (0, 1):
        first = (XS < 2) if flip == 0 else (YS < 2)
        total = 0.0
        for selector in (first, ~first):
            half = flat[selector]
            chosen = None
            optimistic = np.inf
            for table in range(8):
                modifiers = np.array(etc2.MODIFIER_TABLES[table], dtype=np.int64)
                levels = []
                score = 0.0
                for channel in range(3):
                    centre = int(np.clip(round(half[:, channel].mean() / 17.0), 0, 15))
                    channel_cost, level = np.inf, centre
                    for offset in range(-2, 3):
                        candidate = int(np.clip(centre + offset, 0, 15))
                        decoded = np.clip(candidate * 17 + modifiers, 0, 255)
                        error = ((half[:, channel][:, None] - decoded[None, :]) ** 2).min(axis=1).sum()
                        if error < channel_cost:
                            channel_cost, level = error, candidate
                    score += channel_cost
                    levels.append(level)
                if score < optimistic:
                    optimistic, chosen = score, (table, np.array(levels, dtype=np.int64) * 17)
            table, bases = chosen
            modifiers = np.array(etc2.MODIFIER_TABLES[table], dtype=np.int64)
            decoded = np.clip(bases[None, :] + modifiers[:, None], 0, 255)
            total += int(((half[:, None, :] - decoded[None, :, :]) ** 2).sum(axis=2).min(axis=1).sum())
        best = min(best, total)
    return best


def quality_of_block(block: np.ndarray) -> float:
    payload = etc2.encode_blocks(block, etc2.ETC2_RGB8)
    decoded = etc2.decode_blocks(payload, block.shape[1], block.shape[0], punchthrough=False)
    difference = (decoded[..., :3].astype(np.float64) - block[..., :3].astype(np.float64))
    return float((difference ** 2).sum())


def peak_signal_to_noise(reference: np.ndarray, decoded: np.ndarray) -> float:
    difference = (decoded[..., :3].astype(np.float64) - reference[..., :3].astype(np.float64))
    mean_square = float((difference ** 2).mean())
    return 99.0 if mean_square <= 0 else float(10.0 * np.log10(255.0 ** 2 / mean_square))


class JointBaseSearchTest(unittest.TestCase):
    def setUp(self) -> None:
        # The block that exposed the per-channel search: a nearly uniform colour whose channels disagree about
        # which base level is best, which is the only situation where the two searches choose differently.
        block = np.zeros((4, 4, 4), dtype=np.uint8)
        block[..., 3] = 255
        block[..., 0] = 110
        block[..., 1] = 145
        block[..., 2] = 63
        block[3, 0, :3] = (90, 123, 44)
        block[3, 1, :3] = (92, 125, 45)
        block[3, 2, :3] = (94, 128, 48)
        block[3, 3, :3] = (96, 130, 50)
        self.block = block

    def test_the_encoder_is_close_to_the_brute_forced_optimum(self) -> None:
        bound = oracle_error(self.block)
        ours = quality_of_block(self.block)
        self.assertLessEqual(ours, bound * OPTIMUM_MARGIN + 1e-6,
                             f"the encoder left {ours} where its own search space allows {bound}")

    def test_the_choice_this_test_catches_is_not_a_hypothetical(self) -> None:
        """A guard on the guard: if the per-channel choice were as good, the test above would prove nothing."""
        bound = oracle_error(self.block)
        self.assertGreater(per_channel_error(self.block), bound * PER_CHANNEL_FLOOR,
                           "this block no longer separates a joint base search from a per-channel one")

    def test_a_smooth_sheet_clears_its_floor(self) -> None:
        image = smooth_sheet(64)
        payload = etc2.encode_blocks(image, etc2.ETC2_RGB8)
        decoded = etc2.decode_blocks(payload, 64, 64, punchthrough=False)
        quality = peak_signal_to_noise(image, decoded)
        self.assertGreaterEqual(quality, SMOOTH_SHEET_FLOOR_DB, f"smooth sheet measured {quality:.2f} dB")

    def test_a_shipped_sheet_clears_its_floor(self) -> None:
        """The measurement that matters: real art, at the size the game draws it, over the pixels that show."""
        from PIL import Image

        path = pathlib.Path(__file__).resolve().parents[3] / "android" / "assets" / "generated" / SHIPPED_SHEET
        self.assertTrue(path.is_file(), f"{SHIPPED_SHEET} is a shipped sheet")
        with Image.open(path) as source:
            full = np.asarray(source.convert("RGBA"), dtype=np.uint8)
        image = full[:256, :256]
        opaque = image[..., 3] >= etc2.OPAQUE_THRESHOLD
        self.assertGreater(int(opaque.sum()), 0, "the crop has pixels to measure")
        payload = etc2.encode_blocks(image, etc2.ETC2_RGB8)
        decoded = etc2.decode_blocks(payload, 256, 256, punchthrough=False)
        difference = (decoded[..., :3].astype(np.float64) - image[..., :3].astype(np.float64))[opaque]
        quality = float(10.0 * np.log10(255.0 ** 2 / (difference ** 2).mean()))
        self.assertGreaterEqual(quality, SHIPPED_SHEET_FLOOR_DB,
                                f"{SHIPPED_SHEET} measured {quality:.2f} dB over its opaque pixels")

    def test_the_floor_is_held_by_the_search_that_earned_it(self) -> None:
        """The floors are measurements of *this* search, so they are stated with the search they were taken on."""
        source = (TOOLS / "etc2.py").read_text(encoding="utf-8")
        half_search = source.split("def _individual_half(")[1].split("def ")[0]
        self.assertIn("itertools.product(range(len(offsets)), repeat=3)", half_search,
                      "the three channels are enumerated together, not one at a time")
        self.assertIn("per_pixel.min(axis=2) * chunk_weight).sum(axis=1)", half_search,
                      "each combination is scored with the shared index, the way the block decodes")
        self.assertIn("JOINT_SEARCH_CHUNK", source, "the search stays chunked, so memory stays bounded")
        self.assertIn("JOINT_LEVEL_RADIUS", source, "how wide the base search looks is a stated number")


if __name__ == "__main__":
    unittest.main()
