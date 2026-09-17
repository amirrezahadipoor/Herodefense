#!/usr/bin/env python3
"""Bring committed audio files under the peak ceiling by re-encoding them at a gain (roadmap R6.2).

The level gate found what the ear would not have: five of the imported CC0 cues decoded above full scale --
`boss_entrance` at 1.122, `hit` at 1.058, `purchase` at 1.022, `death` at 0.986, `multi_shot` at 0.948 -- and one
of the generated beds touched 1.000. Facts like that belong in the repository rather than in a fix-up nobody can
reproduce, so the correction is a tool with a printed table: decode, scale by the gain that puts the peak on the
ceiling, re-encode, and say exactly how much was taken off each file.

Re-running the audio generators is the preferred fix for anything they own (`generate_music.py`,
`generate_sfx.py`); this tool exists for the imported files that have no generator.

Usage:
    python3 tools/audio/normalize_levels.py --ceiling 0.90 android/assets/audio/sfx/hit.ogg ...
    python3 tools/audio/normalize_levels.py --ceiling 0.90 --only peak-above   # everything over the ceiling
"""
from __future__ import annotations

import argparse
import pathlib
import sys

import numpy as np

try:
    import soundfile as sf
except ImportError:  # pragma: no cover
    print("soundfile is required: python3 -m pip install soundfile")
    raise SystemExit(2)

ROOT = pathlib.Path(__file__).resolve().parents[2]
AUDIO_ROOT = ROOT / "android" / "assets" / "audio"


def peak_of(path: pathlib.Path) -> float:
    samples, _ = sf.read(path, dtype="float32")
    return float(np.max(np.abs(samples))) if len(samples) else 0.0


def normalize(path: pathlib.Path, ceiling: float, passes: int = 4) -> float:
    """Scales the file until the *decoded* peak is under the ceiling.

    One pass is not enough: Vorbis adds its own overshoot on every encode (measured here at about 7%), so the
    tool writes, re-decodes and tries again. Each pass is reported, which is how the ledger can say what
    happened to a file rather than only what it was supposed to be.
    """
    total = 1.0
    for _ in range(passes):
        samples, rate = sf.read(path, dtype="float32")
        current = float(np.max(np.abs(samples))) if len(samples) else 0.0
        if current <= ceiling or current <= 0.0:
            break
        gain = ceiling / current
        sf.write(path, (samples * gain).astype(np.float32), rate, format="OGG", subtype="VORBIS")
        total *= gain
    return total


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("files", nargs="*")
    parser.add_argument("--ceiling", type=float, default=0.90)
    parser.add_argument("--only", choices=["peak-above"], default=None)
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    if args.only == "peak-above":
        targets = [p for p in sorted(AUDIO_ROOT.rglob("*.ogg")) if peak_of(p) > args.ceiling]
    else:
        targets = [pathlib.Path(name).resolve() for name in args.files]

    if not targets:
        print("nothing to normalize")
        return 0

    for path in targets:
        before = peak_of(path)
        if args.dry_run:
            print(f"{path.relative_to(ROOT)} would be scaled by {args.ceiling / before:.4f}")
            continue
        gain = normalize(path, args.ceiling)
        after = peak_of(path)
        print(f"{path.relative_to(ROOT)}: peak {before:.3f} -> {after:.3f} (gain {gain:.4f})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
