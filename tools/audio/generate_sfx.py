#!/usr/bin/env python3
"""Author the game's short effects (roadmap R6.2).

Same reasoning as the music: an effect that is code is an effect whose licence, length and loudness are all
reviewable. The 2026-09-13 review measured the audio category at 5.0/10 with "one music loop and 11 SFX",
and named what was missing: bow draw and release variants, pickups, UI, telegraphs and ambience. Those are the
cues this file adds.

Every effect is mono (a UI click does not need stereo) and normalised to a peak that leaves headroom, because
a stack of identical effects clipping is exactly what a rate limiter cannot fix.

Usage:
    python3 tools/audio/generate_sfx.py [--only bow_draw,coin_pickup] [--output android/assets/audio/sfx]
"""
from __future__ import annotations

import argparse
import pathlib

import numpy as np

try:
    import soundfile as sf
except ImportError:  # pragma: no cover
    print("soundfile is required: python3 -m pip install soundfile")
    raise SystemExit(2)

ROOT = pathlib.Path(__file__).resolve().parents[2]
SAMPLE_RATE = 44_100


def seconds(length: float) -> int:
    return int(SAMPLE_RATE * length)


def sweep(start_hz: float, end_hz: float, length: int, kind: str = "sine") -> np.ndarray:
    """A glide from one frequency to another; the backbone of whooshes and UI blips."""
    t = np.arange(length, dtype=np.float32) / SAMPLE_RATE
    progress = np.linspace(0.0, 1.0, length, dtype=np.float32)
    freq = start_hz + (end_hz - start_hz) * progress
    phase = 2.0 * np.pi * np.cumsum(freq) / SAMPLE_RATE
    if kind == "triangle":
        return (2.0 / np.pi * np.arcsin(np.sin(phase))).astype(np.float32)
    return np.sin(phase).astype(np.float32)


def decay(length: int, rate: float) -> np.ndarray:
    return np.exp(-np.linspace(0.0, rate, length, dtype=np.float32))


def noise(length: int, seed: int) -> np.ndarray:
    return np.random.default_rng(seed).standard_normal(length).astype(np.float32)


def lowpass(signal: np.ndarray, cutoff_hz: float) -> np.ndarray:
    """A one-pole low pass: enough to stop noise from sounding like static."""
    alpha = float(np.exp(-2.0 * np.pi * cutoff_hz / SAMPLE_RATE))
    out = np.empty_like(signal)
    previous = 0.0
    for index, sample in enumerate(signal):
        previous = (1.0 - alpha) * float(sample) + alpha * previous
        out[index] = previous
    return out


def highpass(signal: np.ndarray, cutoff_hz: float) -> np.ndarray:
    return signal - lowpass(signal, cutoff_hz)


def build(name: str) -> np.ndarray:
    """One effect per name. Kept as a switch over the cue list rather than data, because each is a gesture."""
    if name == "bow_draw":
        length = seconds(0.42)
        creak = lowpass(noise(length, 3), 900.0) * decay(length, 3.2) * 0.5
        tension = sweep(140.0, 320.0, length, "triangle") * decay(length, 2.4) * 0.35
        return creak + tension
    if name == "bow_release":
        length = seconds(0.28)
        whoosh = highpass(lowpass(noise(length, 5), 3200.0), 400.0) * decay(length, 6.0)
        string = sweep(420.0, 180.0, length) * decay(length, 7.5) * 0.7
        return whoosh * 0.5 + string
    if name == "bow_release_light":
        # The second and third shots of a volley need to differ, or a multi-shot sounds like a stutter.
        length = seconds(0.24)
        whoosh = highpass(lowpass(noise(length, 9), 4200.0), 700.0) * decay(length, 7.5)
        string = sweep(520.0, 240.0, length, "triangle") * decay(length, 8.5) * 0.6
        return whoosh * 0.42 + string
    if name == "bow_release_heavy":
        length = seconds(0.34)
        whoosh = highpass(lowpass(noise(length, 13), 2400.0), 300.0) * decay(length, 5.0)
        string = sweep(300.0, 120.0, length) * decay(length, 6.0) * 0.85
        thump = sweep(90.0, 60.0, seconds(0.14)) * decay(seconds(0.14), 6.5) * 0.5
        out = np.zeros(length, dtype=np.float32)
        out[: len(thump)] += thump

        return whoosh * 0.5 + string + out
    if name == "coin_pickup":
        length = seconds(0.30)
        first = sweep(1180.0, 1180.0, seconds(0.10)) * decay(seconds(0.10), 5.0)
        second = sweep(1760.0, 1760.0, seconds(0.22)) * decay(seconds(0.22), 6.0)
        out = np.zeros(length, dtype=np.float32)
        out[: len(first)] += first * 0.55
        start = len(first)
        out[start:] += second[: length - start] * 0.45
        return out
    if name == "ui_tap":
        length = seconds(0.09)
        click = highpass(lowpass(noise(length, 17), 5000.0), 1200.0) * decay(length, 12.0)
        body = sweep(880.0, 660.0, length, "triangle") * decay(length, 10.0) * 0.5
        return click * 0.6 + body
    if name == "ui_close":
        length = seconds(0.20)
        return sweep(660.0, 220.0, length, "triangle") * decay(length, 6.0) * 0.7
    if name == "telegraph_warning":
        # The readable warning the R3.2 telegraph work needs to be *heard* as well as seen.
        length = seconds(0.70)
        pulse = np.zeros(length, dtype=np.float32)
        for start in (0.0, 0.32):
            start_index = seconds(start)
            segment = seconds(0.20)
            tone = sweep(220.0, 240.0, segment, "triangle") * decay(segment, 3.0)
            pulse[start_index: start_index + segment] += tone * 0.6
        return pulse + sweep(110.0, 100.0, length) * decay(length, 1.6) * 0.3
    if name == "wave_clear":
        length = seconds(1.10)
        out = np.zeros(length, dtype=np.float32)
        for index, frequency in enumerate((392.0, 494.0, 587.0, 784.0)):
            start = seconds(0.10 * index)
            segment = seconds(0.55)
            tone = sweep(frequency, frequency * 1.005, segment, "triangle") * decay(segment, 3.4)
            out[start: start + segment] += tone * (0.42 - 0.06 * index)
        return out
    if name == "ambience_vigil":
        # An eight-second bed: wind, a distant canopy and a low hum, wrapped so it loops without a seam.
        length = seconds(8.0)
        wind = lowpass(noise(length, 23), 420.0)
        wind *= (0.35 + 0.65 * np.abs(np.sin(np.linspace(0, np.pi * 2.0, length)))).astype(np.float32)
        hum = sweep(58.0, 58.0, length, "triangle") * 0.12
        leaves = lowpass(noise(length, 29), 6000.0) * 0.06
        out = wind * 0.30 + hum + leaves
        wrap = seconds(0.05)
        fade = np.linspace(0.0, 1.0, wrap, dtype=np.float32)
        out[:wrap] = out[:wrap] * fade + out[-wrap:] * (1.0 - fade)
        return out[:-wrap]
    raise KeyError(name)


#: Every effect this tool owns, with the peak it is normalised to and why it is that loud.
EFFECTS = {
    "bow_draw": 0.55,
    "bow_release": 0.62,
    "bow_release_light": 0.55,
    "bow_release_heavy": 0.70,
    "coin_pickup": 0.60,
    "ui_tap": 0.45,
    "ui_close": 0.45,
    "telegraph_warning": 0.72,
    "wave_clear": 0.68,
    "ambience_vigil": 0.40,
}


def write_effect(name: str, output: pathlib.Path) -> pathlib.Path:
    np.random.seed(abs(hash(name)) % (2 ** 31))  # stable within a run; the noise sources are pre-seeded
    signal = build(name)
    peak = float(np.max(np.abs(signal))) or 1.0
    signal = (signal * (EFFECTS[name] / peak)).astype(np.float32)
    # Fade the last two milliseconds: an effect that ends on a non-zero sample clicks.
    tail = min(len(signal), seconds(0.002))
    signal[-tail:] *= np.linspace(1.0, 0.0, tail, dtype=np.float32)
    path = output / f"{name}.ogg"
    sf.write(path, signal, SAMPLE_RATE, format="OGG", subtype="VORBIS")
    return path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--only", default=None, help="comma-separated effect names")
    parser.add_argument("--output", default=str(ROOT / "android/assets/audio/sfx"))
    args = parser.parse_args()

    output = pathlib.Path(args.output)
    output.mkdir(parents=True, exist_ok=True)
    names = args.only.split(",") if args.only else sorted(EFFECTS)
    for name in names:
        path = write_effect(name, output)
        shown = path.relative_to(ROOT) if ROOT in path.parents else path
        print(f"{shown}: {path.stat().st_size} bytes")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
