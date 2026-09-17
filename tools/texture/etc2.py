#!/usr/bin/env python3
"""A first-party ETC2 encoder for the sheets that ship, and the decoder the tests hold it to.

Why this file exists: roadmap R8.1 wants the catalog in a container the GPU decodes for a quarter of what
RGBA8888 costs, and the render pipeline cannot emit one -- Blender writes PNG, and the encoder has to come from
somewhere. A downloaded prebuilt encoder would put a binary nobody here can read into the toolchain, so the two
block formats the runtime actually needs are implemented in this repository, against the published bit layout:

* **ETC2_RGB8** -- eight bytes per 4x4 block, the ETC1 colour block in *individual* mode. What an opaque sheet
  gets.
* **ETC2_RGB8_PUNCHTHROUGH_ALPHA1** -- the same eight bytes, except that a pixel whose two-bit index is `10` is
  transparent. What a sheet whose alpha is a mask gets, and it is the right format for hard-edged sprites: it
  costs half of ETC2_RGBA8 and represents a binary mask exactly, where the EAC alpha block of ETC2_RGBA8 cannot
  -- its base-and-modifier coding has no way to put 0 and 255 in the same 4x4 block, which would turn every
  mask edge into a halo.

The bit layout is not invented here. It is the layout Google's `etc1` implementation uses -- the decoder libGDX
ships as JNI, and the one Android's own tooling embeds -- and `tests/test_etc2_encoder.py` holds this module to
it by decoding blocks that the reference encoder produced and comparing pixel for pixel.
"""
from __future__ import annotations

import numpy as np

#: The eight intensity-modifier sets of ETC1/ETC2, in the order the per-pixel two-bit index selects them.
MODIFIER_TABLES = (
    (2, 8, -2, -8),
    (5, 17, -5, -17),
    (9, 29, -9, -29),
    (13, 42, -13, -42),
    (18, 60, -18, -60),
    (24, 80, -24, -80),
    (33, 106, -33, -106),
    (47, 183, -47, -183),
)
_MODIFIERS = np.array(MODIFIER_TABLES, dtype=np.int32)

#: GL internal formats this module can produce, and what the GPU decodes from them.
ETC2_RGB8 = 0x9275
ETC2_RGB8_PUNCHTHROUGH_ALPHA1 = 0x9276

#: The punchthrough index. Only ETC2_RGB8_PUNCHTHROUGH_ALPHA1 reads it, and only there does it mean "clear".
PUNCHTHROUGH_INDEX = 2

#: A pixel counts as opaque at or above this alpha; below it, the punchthrough format makes it transparent.
OPAQUE_THRESHOLD = 128

#: How far around a block's mean the base-colour search looks, in 4-bit steps, per channel.
LEVEL_WINDOW = 2


def convert4_to_8(value: int) -> int:
    """A four-bit base colour, expanded the way the specification expands it (replication)."""
    return (value << 4) | value


def convert5_to_8(value: int) -> int:
    """A five-bit base colour, expanded the same way, with the two spare bits repeated at the bottom."""
    return (value << 3) | (value >> 2)


def decoded_bytes_per_pixel(gl_internal_format: int) -> float:
    """The fixed rate these formats decode at; ETC2 is 8 bytes per 16 pixels, every format in this module."""
    if gl_internal_format not in (ETC2_RGB8, ETC2_RGB8_PUNCHTHROUGH_ALPHA1):
        raise ValueError(f"not an ETC2 format this module writes: {gl_internal_format:#x}")
    return 0.5


def carries_alpha(gl_internal_format: int) -> bool:
    return gl_internal_format == ETC2_RGB8_PUNCHTHROUGH_ALPHA1


def binary_alpha_fraction(rgba: np.ndarray) -> float:
    """The share of pixels whose alpha is already 0 or 255 -- the measure that decides punchthrough.

    A sheet below the threshold this module's caller sets has soft alpha somewhere, and punchthrough would
    harden it, so the caller keeps such a sheet as PNG until an EAC alpha encoder exists. The number is
    reported rather than assumed, which is the point: "is this sheet a mask?" stops being a judgement call.
    """
    alpha = rgba[..., 3]
    extremes = np.count_nonzero((alpha == 0) | (alpha == 255))
    return float(extremes) / float(alpha.size)


def decode_blocks(payload: bytes, width: int, height: int, punchthrough: bool) -> np.ndarray:
    """Decode a block stream into an (height, width, 4) RGBA array.

    The layout is the reference's, exactly: a block is two big-endian 32-bit words; the first carries the base
    colours, the table codewords and the two flags, the second carries sixteen two-bit pixel indices -- the low
    bit of a pixel's index at bit `y + 4x` of that word and its high bit at bit `y + 4x + 15`.
    """
    blocks_x = (width + 3) // 4
    blocks_y = (height + 3) // 4
    expected = blocks_x * blocks_y * 8
    if len(payload) != expected:
        raise ValueError(f"expected {expected} bytes for {width}x{height}, got {len(payload)}")

    raw = np.frombuffer(payload, dtype=np.uint8).reshape(-1, 8).astype(np.uint32)
    high = (raw[:, 0] << 24) | (raw[:, 1] << 16) | (raw[:, 2] << 8) | raw[:, 3]
    low = (raw[:, 4] << 24) | (raw[:, 5] << 16) | (raw[:, 6] << 8) | raw[:, 7]

    flip = (high & 1).astype(bool)
    # ETC2 punchthrough is individual-mode layout only: the bit that means "differential" in ETC1 is one of the
    # sixteen index bits there, so a punchthrough decode must not read it as a mode flag. Reading it as one is
    # consistent with an encoder that writes it the same wrong way, which is why the device test -- not the
    # round trip -- is what caught this.
    differential = (np.zeros_like(high, dtype=bool) if punchthrough else (high & 2).astype(bool))
    tables = np.stack([(high >> 5) & 7, (high >> 2) & 7], axis=1)

    # Individual mode: two independent four-bit colours, expanded by replication.
    first = np.stack([((high >> 28) & 0xF) * 17, ((high >> 20) & 0xF) * 17, ((high >> 12) & 0xF) * 17], axis=1)
    other = np.stack([((high >> 24) & 0xF) * 17, ((high >> 16) & 0xF) * 17, ((high >> 8) & 0xF) * 17], axis=1)

    # Differential mode: a five-bit base colour and a signed three-bit delta, which an encoder that never emits
    # this mode still has to be able to read.
    lookup = np.array([0, 1, 2, 3, -4, -3, -2, -1], dtype=np.int32)
    expanded = np.array([(value << 3) | (value >> 2) for value in range(32)], dtype=np.int32)
    bases5 = np.stack([(high >> 27) & 0x1F, (high >> 19) & 0x1F, (high >> 11) & 0x1F], axis=1)
    deltas = np.stack([(high >> 24) & 7, (high >> 16) & 7, (high >> 8) & 7], axis=1)
    diff_first = bases5
    diff_other = np.clip(bases5 + lookup[deltas], 0, 31)
    first = np.where(differential[:, None], expanded[diff_first], first)
    other = np.where(differential[:, None], expanded[np.clip(diff_other, 0, 31)], other)

    xs, ys = np.meshgrid(np.arange(4), np.arange(4), indexing="xy")
    k = 4 * xs + ys                                        # the reference's bit position for pixel (x, y)
    # The reference reads the second index bit as `(low >> (k + 15)) & 2`, which is bit k+16 of the word: the
    # low bits of all sixteen indices sit in bits 0-15 and the high bits in bits 16-31.
    index = ((low[:, None, None] >> k[None, :, :]) & 1) | (
        ((low[:, None, None] >> (k[None, :, :] + 16)) & 1) << 1
    )

    # Which half a pixel belongs to: columns when the flip bit is clear, rows when it is set.
    subblock = np.where(flip[:, None, None], ys[None, :, :] >= 2, xs[None, :, :] >= 2).astype(np.int64)
    table_per_pixel = np.where(subblock == 0, tables[:, 0][:, None, None], tables[:, 1][:, None, None])
    modifiers = np.take_along_axis(_MODIFIERS[table_per_pixel], index[..., None], axis=3)[..., 0]
    base_per_pixel = np.stack(
        [
            np.where(subblock == 0, first[:, channel][:, None, None], other[:, channel][:, None, None])
            for channel in range(3)
        ],
        axis=3,
    )
    colour = np.clip(base_per_pixel + modifiers[..., None], 0, 255).astype(np.uint8)
    alpha = np.where(index == PUNCHTHROUGH_INDEX, 0, 255).astype(np.uint8) if punchthrough else np.full(
        index.shape, 255, dtype=np.uint8
    )
    decoded = np.concatenate([colour, alpha[..., None]], axis=3)
    # Blocks come back in raster order and each is a 4x4 tile: lay them out and crop to the image.
    decoded = decoded.reshape(blocks_y, blocks_x, 4, 4, 4).transpose(0, 2, 1, 3, 4)
    return decoded.reshape(blocks_y * 4, blocks_x * 4, 4)[:height, :width]


def _half_error(colours, weight, allowed, decoded_bases, table):
    """The error and the indices of one half, given the decoded base colour of each of its fifteen... three planes.

    `decoded_bases` is (blocks, 3) already expanded to eight bits, `table` is (blocks,) and `allowed` is the
    (blocks, 8, 4) mask of index values a pixel may use. Returns the weighted total error and the indices.
    """
    modifiers = _MODIFIERS[table]                       # (blocks, 4)
    error = np.zeros((colours.shape[0], colours.shape[1], 4))
    for channel in range(3):
        decoded = np.clip(decoded_bases[:, channel][:, None] + modifiers, 0, 255).astype(np.float64)
        error += (colours[:, :, channel][:, :, None] - decoded[:, None, :]) ** 2
    error = np.where(allowed, error, np.inf)
    # The index is shared by the three channels, so the choice has to be made on their summed error -- scoring
    # each channel separately would flatter every candidate and pick the wrong table, which is exactly what the
    # first version of this function did before the comparison in the tool's report showed it.
    best = error.min(axis=2)
    indices = error.argmin(axis=2).astype(np.int64)
    return (best * weight).sum(axis=1), indices


def _allowed_mask(opaque, allow_transparent):
    """Which two-bit indices each pixel may use: `10` is the punchthrough index and is for clear pixels only."""
    allowed = np.zeros(opaque.shape + (4,), dtype=bool)
    if allow_transparent:
        for value in (0, 1, 3):
            allowed[:, :, value] = opaque
        allowed[:, :, PUNCHTHROUGH_INDEX] = ~opaque
    else:
        allowed[:] = True
    return allowed


def _individual_half(colours, weight, allowed):
    """The best individual-mode half: two independent 4-bit colours, one per half, plus a shared table.

    The search is over the eight modifier tables and, per channel, the 4-bit level within a small window of the
    half's mean -- a window rather than the whole range because the cost is not separable in the three channels
    (the index is shared) and a wider window measures *worse* on the shipped sheets, which the tool's own
    comparison records.
    """
    blocks = colours.shape[0]
    counts = weight.sum(axis=1, keepdims=True)
    means = np.where(counts > 0, (colours * weight[..., None]).sum(axis=1) / np.maximum(counts, 1), 0.0)
    total = np.full(blocks, np.inf)
    best_table = np.zeros(blocks, dtype=np.int64)
    best_bases = np.zeros((blocks, 3), dtype=np.int64)
    best_indices = np.zeros(colours.shape[:2], dtype=np.int64)
    for table_index in range(8):
        levels = np.zeros((blocks, 3), dtype=np.int64)
        cost = np.zeros(blocks)
        for channel in range(3):
            centre = np.clip(np.rint(means[:, channel] / 17.0), 0, 15).astype(np.int64)
            channel_cost = np.full(blocks, np.inf)
            channel_level = np.zeros(blocks, dtype=np.int64)
            for offset in range(-LEVEL_WINDOW, LEVEL_WINDOW + 1):
                candidate = np.clip(centre + offset, 0, 15)
                decoded = candidate * 17
                modifiers = _MODIFIERS[table_index]
                error = np.zeros((blocks, colours.shape[1], 4))
                error += (colours[:, :, channel][:, :, None] - np.clip(
                    decoded[:, None, None] + modifiers[None, None, :], 0, 255)) ** 2
                error = np.where(allowed, error, np.inf).min(axis=2)
                error = (error * weight).sum(axis=1)
                better = error < channel_cost
                channel_cost = np.where(better, error, channel_cost)
                channel_level = np.where(better, candidate, channel_level)
            levels[:, channel] = channel_level
            cost += channel_cost
        better = cost < total
        total = np.where(better, cost, total)
        best_table = np.where(better, table_index, best_table)
        best_bases = np.where(better[:, None], levels, best_bases)

    decoded_bases = best_bases * 17
    _, indices = _half_error(colours, weight, allowed, decoded_bases, best_table)
    return total, decoded_bases, best_table, indices


def _mean_colour(colours, weight):
    counts = weight.sum(axis=1, keepdims=True)
    return np.where(counts > 0, (colours * weight[..., None]).sum(axis=1) / np.maximum(counts, 1), 0.0)


def _table_search(colours, weight, allowed, decoded_bases):
    """The best of the eight modifier tables for a half whose base colours are already fixed."""
    blocks = colours.shape[0]
    total = np.full(blocks, np.inf)
    best_table = np.zeros(blocks, dtype=np.int64)
    best_indices = np.zeros(colours.shape[:2], dtype=np.int64)
    for table_index in range(8):
        cost, indices = _half_error(colours, weight, allowed, decoded_bases, table_index)
        better = cost < total
        total = np.where(better, cost, total)
        best_table = np.where(better, table_index, best_table)
        best_indices = np.where(better[:, None], indices, best_indices)
    return total, best_table, best_indices


def _differential_pair(first_colours, second_colours, first_weight, second_weight, first_allowed, second_allowed):
    """The best differential-mode pair: a 5-bit base for the first half and a signed 3-bit step to the second.

    This is the mode that buys detail -- five bits of base colour instead of four, with the second half defined
    *relative to the first*, which is what textured art wants. The second half is placed within the four steps
    the format allows either way, and both halves then pick their own modifier table.
    """
    steps = np.array([0, 1, 2, 3, -4, -3, -2, -1], dtype=np.int64)
    first_mean = _mean_colour(first_colours, first_weight)
    second_mean = _mean_colour(second_colours, second_weight)
    first_base5 = np.clip(np.rint(first_mean / 8.225), 0, 31).astype(np.int64)
    delta = np.clip(np.rint((second_mean - first_mean) / 8.225), -4, 3).astype(np.int64)
    second_base5 = np.clip(first_base5 + delta, 0, 31)

    five_bit = np.array([convert5_to_8(value) for value in range(32)], dtype=np.float64)
    first_bases = np.stack([five_bit[first_base5[:, channel]] for channel in range(3)], axis=1)
    second_bases = np.stack([five_bit[second_base5[:, channel]] for channel in range(3)], axis=1)

    first_cost, first_table, first_indices = _table_search(first_colours, first_weight, first_allowed, first_bases)
    second_cost, second_table, second_indices = _table_search(
        second_colours, second_weight, second_allowed, second_bases
    )
    step_codes = np.zeros((first_colours.shape[0], 3), dtype=np.int64)
    for code, step in enumerate((0, 1, 2, 3, -4, -3, -2, -1)):
        step_codes = np.where(delta == step, code, step_codes)
    return (
        first_cost + second_cost,
        (first_bases, second_bases),
        (first_table, second_table),
        first_base5,
        step_codes,
    )


def encode_blocks(rgba: np.ndarray, gl_internal_format: int) -> bytes:
    """Encode an RGBA image into ETC2 blocks of the requested format.

    Both colour modes are searched per block and the cheaper one wins, the flip bit is chosen by measured error,
    and the modifier table is chosen per half. The punchthrough format constrains two things: a pixel that is
    opaque may not use the index that means "clear", so the encoder cannot improve a colour by punching a hole
    in it, and the block stays in individual mode -- the punchthrough layout has no differential mode, because
    the bit that would carry the flag is one of the sixteen index bits.
    """
    if gl_internal_format not in (ETC2_RGB8, ETC2_RGB8_PUNCHTHROUGH_ALPHA1):
        raise ValueError(f"not an ETC2 format this module writes: {gl_internal_format:#x}")
    punchthrough = carries_alpha(gl_internal_format)
    height, width = rgba.shape[0], rgba.shape[1]
    padded_h = (height + 3) // 4 * 4
    padded_w = (width + 3) // 4 * 4
    padded = np.zeros((padded_h, padded_w, 4), dtype=np.uint8)
    padded[:height, :width] = rgba
    blocks_y, blocks_x = padded_h // 4, padded_w // 4
    grid = padded.reshape(blocks_y, 4, blocks_x, 4, 4).transpose(0, 2, 1, 3, 4).reshape(-1, 4, 4, 4)
    opaque = (grid[..., 3] >= OPAQUE_THRESHOLD) if punchthrough else np.ones(grid.shape[:3], dtype=bool)

    # k = 4x + y: the k-th entry of a half is the pixel whose index the block layout stores at that bit position.
    xs = np.array([k // 4 for k in range(16)])
    ys = np.array([k % 4 for k in range(16)])
    flat_colour = grid[:, ys, xs, :3]
    flat_opaque = opaque[:, ys, xs]
    allowed_all = _allowed_mask(flat_opaque, punchthrough)

    blocks = grid.shape[0]
    best_cost = np.full(blocks, np.inf)
    best_flip = np.zeros(blocks, dtype=np.int64)
    best_mode = np.zeros(blocks, dtype=np.int64)          # 0 = individual, 1 = differential
    best_levels = np.zeros((blocks, 2, 3), dtype=np.int64)
    best_tables = np.zeros((blocks, 2), dtype=np.int64)
    best_indices = np.zeros((blocks, 16), dtype=np.int64)

    for flip in (0, 1):
        first_selector = (xs < 2) if flip == 0 else (ys < 2)
        second_selector = ~first_selector
        first_colours = flat_colour[:, first_selector, :]
        second_colours = flat_colour[:, second_selector, :]
        first_weight = flat_opaque[:, first_selector].astype(np.float64)
        second_weight = flat_opaque[:, second_selector].astype(np.float64)
        first_allowed = allowed_all[:, first_selector, :]
        second_allowed = allowed_all[:, second_selector, :]

        for mode in ((0,) if punchthrough else (0, 1)):
            if mode == 0:
                first_cost, first_bases, first_table, first_indices = _individual_half(
                    first_colours, first_weight, first_allowed
                )
                second_cost, second_bases, second_table, second_indices = _individual_half(
                    second_colours, second_weight, second_allowed
                )
                first_words = np.rint(first_bases / 17.0).astype(np.int64)
                second_words = np.rint(second_bases / 17.0).astype(np.int64)
            else:
                pair_cost, (first_bases, second_bases), (first_table, second_table), _, step_codes = \
                    _differential_pair(
                        first_colours, second_colours, first_weight, second_weight, first_allowed, second_allowed
                    )
                first_cost = pair_cost
                second_cost = np.zeros(blocks)
                _, first_indices = _half_error(
                    first_colours, first_weight, first_allowed, first_bases, first_table
                )
                _, second_indices = _half_error(
                    second_colours, second_weight, second_allowed, second_bases, second_table
                )
                first_words = np.stack(
                    [
                        np.clip(np.rint(first_bases[:, channel] / 8.225), 0, 31).astype(np.int64)
                        for channel in range(3)
                    ],
                    axis=1,
                )
                second_words = step_codes

            cost = first_cost + second_cost
            better = cost < best_cost
            best_cost = np.where(better, cost, best_cost)
            best_flip = np.where(better, flip, best_flip)
            best_mode = np.where(better, mode, best_mode)
            best_levels = np.where(
                better[:, None, None], np.stack([first_words, second_words], axis=1), best_levels
            )
            best_tables = np.where(better[:, None], np.stack([first_table, second_table], axis=1), best_tables)
            indices = np.zeros_like(best_indices)
            indices[:, first_selector] = first_indices
            indices[:, second_selector] = second_indices
            best_indices = np.where(better[:, None], indices, best_indices)

    differential = best_mode == 1
    # Individual mode carries two four-bit colours per channel; differential mode carries one five-bit base and a
    # three-bit step, in the same bits. The two layouts are assembled separately and then selected per block.
    individual_high = (
        (best_levels[:, 0, 0] << 28)
        | (best_levels[:, 1, 0] << 24)
        | (best_levels[:, 0, 1] << 20)
        | (best_levels[:, 1, 1] << 16)
        | (best_levels[:, 0, 2] << 12)
        | (best_levels[:, 1, 2] << 8)
    )
    differential_high = (
        (best_levels[:, 0, 0] << 27)
        | (best_levels[:, 1, 0] << 24)
        | (best_levels[:, 0, 1] << 19)
        | (best_levels[:, 1, 1] << 16)
        | (best_levels[:, 0, 2] << 11)
        | (best_levels[:, 1, 2] << 8)
    )
    high = (
        np.where(differential, differential_high, individual_high)
        | (best_tables[:, 0] << 5)
        | (best_tables[:, 1] << 2)
        | (differential.astype(np.int64) << 1)
        | best_flip
    ).astype(np.uint32)
    low = np.zeros(blocks, dtype=np.uint32)
    for k in range(16):
        low |= ((best_indices[:, k] & 1) << k).astype(np.uint32)
        low |= (((best_indices[:, k] >> 1) & 1) << (k + 16)).astype(np.uint32)

    out = np.empty((blocks, 8), dtype=np.uint8)
    for byte in range(4):
        out[:, byte] = ((high >> (24 - 8 * byte)) & 0xFF).astype(np.uint8)
        out[:, 4 + byte] = ((low >> (24 - 8 * byte)) & 0xFF).astype(np.uint8)
    return out.tobytes()
