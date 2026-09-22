from __future__ import annotations

import math
import os
from dataclasses import dataclass
from pathlib import Path

BLENDER_VERSION = "4.2.23"
FRAME_RATE = 12
RENDER_SUPERSAMPLE = 3  # Phase 49: 2->3 for regular enemies sharper
OPAQUE_RENDER_SAMPLES = 32  # Phase 49: 28->32 for cleaner vibrant colors
# Workbench equipment overlays ignore sample counts; the number below is
# provenance-only but raised to preserve thin outline after supersampled downscale.
OVERLAY_RENDER_SAMPLES = 12  # studio-v3: 8→12 thin 1.2 interior lines need working-resolution margin
# Phase 28.3: hero, bosses, and trees render at the top tier; every other
# opaque asset shares the mid-tier floors above.
# Studio-v3 audit: 2×/28 mid, 3×/36 top confirmed sufficient for weighted 2.4/1.2 outline + rim fireflies.
# Phase 49 audit: 3×/32 mid, 4×/48 top for stunning vibrant look.
TOP_TIER_CLASSES = frozenset({"boss", "tree"})
TOP_TIER_KEY_PREFIX = "hero"
TOP_TIER_SUPERSAMPLE = 4  # Phase 49: 3->4 sharper edges, cleaner colors
TOP_TIER_SAMPLES = 48  # Phase 49: 36->48 for bloom + rim firefly free


# The arena's display-quality pass was reviewed under a lifted value range, and the pixels it accepted are the ones
# the game ships: ARENA_PREMIUM_V2_REVIEW.md records the backdrop's mean value rising from about 24 to about 37 of
# 255, and the device brightness gates were pinned on the tier that came out of it. The render engine has changed
# since -- same geometry, same materials, a later Blender and a later lighting rig -- and the same keys come back
# about 40% darker. Measured, not guessed: the arena batch's audit compares every candidate sheet's painted mean
# value against the pixels it would replace and refuses a loss of more than a tenth.
#
# So the arena's full-frame art is rendered with the exposure that puts it back inside that tenth. Calibration, on
# the shipped pixels' painted mean value (rendered 3x/32, published at the reviewed tier):
#
#   key                shipped   @0.0 stops   @0.75 stops
#   ground_tile_0        34.38       18.96        31.79
#   ground_tile_1        42.87       33.48        49.31
#   ground_tile_2        34.54       19.40        32.04
#   crystal_prop_0       82.21       70.50        86.01
#   crystal_prop_1       77.99       64.72        79.79
#   crystal_prop_2       68.99       58.45        72.99
#
# Every one of them clears the tenth-of-a-tenth floor at 0.75 stops, and the spread that remains (-7% to +15%) is
# the tone curve's own, not a per-key grade: one exposure is one lighting rig, and six of them would be six
# unreviewed looks. The cover props are deliberately absent from this set -- they were authored and reviewed under
# the current engine's range and they ship in it, so brightening them would be a new look rather than a
# restoration.
ARENA_LIFT_PREFIXES = ("arena_backdrop", "ground_tile_", "crystal_prop_")
#: Stops of exposure the arena's full-frame art is rendered with.
ARENA_EXPOSURE_STOPS = 0.75


def arena_exposure(asset_key: str) -> float:
    """The exposure a key is rendered with: the arena's lift for its full-frame art, none for everything else."""
    if not asset_key.startswith(ARENA_LIFT_PREFIXES):
        return 0.0
    override = os.environ.get("HD_ARENA_EXPOSURE_STOPS")
    if override is not None:
        try:
            value = float(override)
        except ValueError:
            return ARENA_EXPOSURE_STOPS
        # A typo must not silently render a batch at zero, so only a finite number counts as an override.
        return value if math.isfinite(value) else ARENA_EXPOSURE_STOPS
    return ARENA_EXPOSURE_STOPS


def render_tier(asset_key: str, frame_class: str) -> tuple[int, int]:
    """(supersample, eevee_samples) for one asset; hero/bosses/trees highest."""
    if frame_class in TOP_TIER_CLASSES or asset_key == TOP_TIER_KEY_PREFIX \
            or asset_key.startswith(TOP_TIER_KEY_PREFIX + "_"):
        return (TOP_TIER_SUPERSAMPLE, TOP_TIER_SAMPLES)
    return (RENDER_SUPERSAMPLE, OPAQUE_RENDER_SAMPLES)
OUTLINE_RGBA = (0.0072, 0.0152, 0.0194, 1.0)  # linear-ish #142126
# Phase 37: colored outline per category — hero dark green, bosses dark red, enemies dark violet
OUTLINE_COLORS = {
    "hero": (0.006, 0.032, 0.016, 1.0),      # #0F2A1A dark green
    "boss": (0.08, 0.015, 0.03, 1.0),        # #3A0F1A dark red
    "enemy": (0.018, 0.012, 0.035, 1.0),     # #1A1426 dark violet
    "default": (0.0072, 0.0152, 0.0194, 1.0), # #142126 fallback
}

CAMERA_LOCATION = (6.5, -9.5, 6.2)
CAMERA_TARGET = (0.0, 0.0, 1.15)
CAMERA_SCALE = {
    "character": 3.0,
    "boss": 4.4,
    "item": 2.2,
    "tree": 6.0,
    "environment": 5.1,
    "arena": 11.5,
    "projectile": 1.5,
    "vfx": 2.9,
}
CAMERA_SHIFT_Y = {
    "character": 0.0,
    "boss": 0.06,
    "item": -0.12,
    "tree": 0.12,
    "environment": 0.0,
    "arena": 0.0,
    "projectile": 0.0,
    "vfx": 0.0,
}
FRAME_SIZE = {
    "character": 384,  # Phase 54: 192->384 for 950 score, hero 300px on screen readable
    "boss": 384,       # Phase 54: 256->384 boss sharper
    "item": 192,       # Phase 54: 96->192 icons sharper
    "tree": 384,       # Phase 54: 256->384 tree sharper
    "environment": 384,
    # The portrait backdrop retains frameSize for manifest compatibility while its
    # explicit frameWidth/frameHeight contract is defined below.
    "arena": 720,
    "projectile": 128,  # Phase 54: 64->128 arrows sharper
    "vfx": 256,         # Phase 54: 128->256 VFX sharper
}
FRAME_DIMENSIONS = {
    **{key: (size, size) for key, size in FRAME_SIZE.items()},
    "arena": (720, 1280),
}

CLIPS: dict[str, int] = {
    "idle": 6,
    "attack": 8,
    "hit": 4,
    "death": 10,
}

# Phase 28.4: one-shot effect strips; the engine plays them once per trigger.
VFX_CLIPS: dict[str, int] = {
    "play": 8,
}

REQUIRED_BONES = (
    "root",
    "pelvis",
    "spine",
    "chest",
    "neck",
    "head",
    "upper_arm.L",
    "forearm.L",
    "hand.L",
    "upper_arm.R",
    "forearm.R",
    "hand.R",
    "thigh.L",
    "shin.L",
    "foot.L",
    "thigh.R",
    "shin.R",
    "foot.R",
    "weapon_socket",
    "helmet_socket",
    "armor_socket",
    "boot_socket.L",
    "boot_socket.R",
    "ring_socket.L",
    "ring_socket.R",
)

PALETTE = {
    "outline": "#142126",
    "hero_green": "#2ECC71",  # Phase 34 vibrant: was #1E8A4E S0.78 V0.54 -> S0.77 V0.80 phosphorescent
    "hero_leaf": "#A8FF53",   # Phase 34 vibrant: was #8BF27A S0.50 V0.95 -> S0.67 V1.00 lime phosphor
    "hero_gold": "#FFD700",   # Phase 34 vibrant: was #E8B84B S0.68 V0.91 -> S1.00 V1.00 metallic glossy
    "skin": "#F0C9A8",
    "wood": "#70452C",
    "enemy_rust": "#B5452E",
    "enemy_violet": "#7A5CA8",
    "stone": "#8A9AA6",
    "ink": "#0B1419",
    "parchment": "#E7D8B1",
    "cyan": "#5FCAD2",
    "crimson": "#B84245",
    "amber": "#E19C3B",
}


@dataclass(frozen=True)
class RenderAsset:
    key: str
    family: str
    builder: str
    frame_class: str = "character"
    variant: int = 0


REGULAR_CHARACTERS = (
    RenderAsset("hero", "hero", "hero"),
    RenderAsset("rootling", "enemy", "rootling"),
    RenderAsset("stonekin", "enemy", "stonekin"),
    RenderAsset("gloom_wolf", "enemy", "gloom_wolf"),
    RenderAsset("fungal_brute", "enemy", "fungal_brute"),
    # R3.4: the roster doubles to eight. The four additions are authored so the *mean*
    # health and damage per type stay exactly where they were (117 health and 28 damage
    # across four types, and 234 / 56 across eight), because the wave spawner cycles
    # types uniformly: the wave gets more variety, not more weight.
    RenderAsset("bark_stalker", "enemy", "bark_stalker"),
    RenderAsset("sap_hound", "enemy", "sap_hound"),
    RenderAsset("husk_warden", "enemy", "husk_warden"),
    RenderAsset("bramble_thrall", "enemy", "bramble_thrall"),
)

BOSSES = (
    RenderAsset("ancient_golem", "boss", "ancient_golem", "boss"),
    RenderAsset("thorn_matriarch", "boss", "thorn_matriarch", "boss"),
    RenderAsset("ember_wyrm", "boss", "ember_wyrm", "boss"),
    RenderAsset("void_knight", "boss", "void_knight", "boss"),
)

# Phase 28.4 proof sets; Phases 29.4/30 extend the keys, not the categories.
PROJECTILES = (
    RenderAsset("projectile_arrow", "projectile", "arrow", "projectile"),
)

VFX_ASSETS = (
    RenderAsset("vfx_impact_flash", "vfx", "impact_flash", "vfx"),
    RenderAsset("vfx_shockwave_ring", "vfx", "shockwave_ring", "vfx"),
)

# Hero-worn look reduced to boots + weapon (owned artistically by Phase 29.4).
OVERLAY_VISUAL_SLOTS = frozenset({"boots", "weapon"})


def repository_root(script_file: str) -> Path:
    return Path(script_file).resolve().parents[2]
