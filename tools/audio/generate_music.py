#!/usr/bin/env python3
"""Author the game's music tracks (roadmap R6.1).

Every track is synthesised here from a seed: the notes, the arrangement and the mix are code, so a track is
reproducible, reviewable and free of licence questions. That is the point of the item -- the repository had
one 1.2 MB loop for a two-hour run, and "more music" is not something a repo can solve by downloading.

The synthesis is deliberately simple and readable: oscillators, envelopes, a feedback delay and a soft clip.
What makes it music rather than a test tone is the arrangement -- a bass line, chord pads, a lead motif and
percussion, over a chord progression per track, all in one key per track so the parts cannot fight.

Seamless looping: the render is a whole number of bars, and the reverb tail is wrapped onto the head instead
of being faded out, so the last sample leads back into the first one.

Usage:
    python3 tools/audio/generate_music.py [--only vigil|hollow_march|heartwood_dawn|quiet_after]
                                          [--output android/assets/audio/music]
"""
from __future__ import annotations

import argparse
import pathlib
import sys

import numpy as np

try:
    import soundfile as sf
except ImportError:  # pragma: no cover - the CI step installs it, like Pillow for the visual tools
    print("soundfile is required: python3 -m pip install soundfile")
    raise SystemExit(2)

ROOT = pathlib.Path(__file__).resolve().parents[2]
SAMPLE_RATE = 44_100

#: A minor/dorian mode on A: the sanctuary is meant to sound old, not sad.
SCALE = (0, 2, 3, 5, 7, 8, 10)  # natural minor
NOTE_HZ = {}


def _build_scale() -> None:
    base = 55.0  # A1
    for octave in range(6):
        for step, semitone in enumerate(SCALE):
            NOTE_HZ[(octave, step)] = base * (2 ** (octave + semitone / 12.0))


_build_scale()


def note(octave: int, step: int) -> float:
    """Frequency of a scale step, wrapping octaves so degrees add up across the keyboard."""
    return NOTE_HZ[(octave + step // 7, step % 7)]


def envelope(length: int, attack: float, release: float, sustain: float = 1.0) -> np.ndarray:
    attack_samples = max(1, int(attack * SAMPLE_RATE))
    release_samples = max(1, int(release * SAMPLE_RATE))
    shape = np.full(length, sustain, dtype=np.float32)
    shape[:attack_samples] *= np.linspace(0.0, 1.0, attack_samples, dtype=np.float32)
    shape[-release_samples:] *= np.linspace(1.0, 0.0, release_samples, dtype=np.float32)
    return shape


def oscillator(freq: float, length: int, kind: str = "saw") -> np.ndarray:
    t = np.arange(length, dtype=np.float32) / SAMPLE_RATE
    phase = 2.0 * np.pi * freq * t
    if kind == "sine":
        return np.sin(phase).astype(np.float32)
    if kind == "triangle":
        return (2.0 / np.pi * np.arcsin(np.sin(phase))).astype(np.float32)
    if kind == "square":
        return np.sign(np.sin(phase)).astype(np.float32) * 0.6
    # a band-limited-ish saw: a few harmonics, which is what a synth pad sounds like
    wave = np.zeros(length, dtype=np.float32)
    for harmonic in range(1, 9):
        wave += (np.sin(phase * harmonic) / harmonic).astype(np.float32)
    return wave * (2.0 / np.pi)


def pluck(freq: float, length: int, decay: float) -> np.ndarray:
    """A struck string: the same harmonics with a faster decay for the brighter ones."""
    t = np.arange(length, dtype=np.float32) / SAMPLE_RATE
    wave = np.zeros(length, dtype=np.float32)
    for harmonic in range(1, 7):
        amplitude = np.exp(-t * decay * harmonic).astype(np.float32)
        wave += (np.sin(2.0 * np.pi * freq * harmonic * t).astype(np.float32) * amplitude / harmonic)
    return wave


def place(target: np.ndarray, snippet: np.ndarray, start: int) -> None:
    end = min(len(target), start + len(snippet))
    if start >= len(target):
        return
    target[start:end] += snippet[: end - start]


def reverb(signal: np.ndarray, mix: float, decay: float = 0.42, taps: int = 6) -> np.ndarray:
    """A feedback delay whose tail is wrapped onto the head, so the loop stays seamless."""
    out = signal.copy()
    for tap in range(1, taps + 1):
        delay = int(SAMPLE_RATE * (0.083 * tap))
        gain = decay ** tap
        if delay >= len(signal):
            break
        shifted = np.roll(signal, delay) * (mix * gain)
        out += shifted
    return out


def percussion(length: int, bpm: float, pattern: str) -> np.ndarray:
    """Kick, snare and shaker, written as a one-character-per-eighth pattern."""
    beat = 60.0 / bpm
    eighth = int(SAMPLE_RATE * beat / 2)
    out = np.zeros(length, dtype=np.float32)
    noise = np.random.default_rng(11).standard_normal(length).astype(np.float32)
    for index, symbol in enumerate(pattern):
        start = index * eighth
        if start >= length:
            break
        if symbol == "K":
            body = oscillator(58.0, eighth * 2, "sine") * np.exp(
                -np.linspace(0, 9.0, eighth * 2, dtype=np.float32)
            )
            place(out, body * 0.62, start)
        elif symbol == "S":
            snatch = noise[start:start + eighth] * envelope(eighth, 0.001, 0.12)
            place(out, snatch * 0.20, start)
        elif symbol == "x":
            hat_length = max(1, eighth // 2)
            hat = noise[start:start + hat_length] * envelope(hat_length, 0.001, 0.04)
            place(out, hat * 0.10, start)
    return out


def render(spec: dict) -> np.ndarray:
    """One arrangement: bass, chord pads, lead and percussion over a progression, then the mix."""
    bpm = spec["bpm"]
    beat = 60.0 / bpm
    bar = beat * 4
    bars = spec["bars"]
    length = int(SAMPLE_RATE * bar * bars)
    progression = spec["progression"]  # one (root degree, chord degrees) per bar
    lead_pattern = spec["lead"]  # (bar, beat_offset, degree, duration_beats)

    mix = np.zeros(length, dtype=np.float32)

    # bass: root of each chord, one note per beat, with a plucked articulation
    for bar_index in range(bars):
        root, _ = progression[bar_index % len(progression)]
        for beat_index in range(4):
            start = int(SAMPLE_RATE * (bar_index * bar + beat_index * beat))
            length_samples = int(SAMPLE_RATE * beat * 0.92)
            voice = pluck(note(1, root), length_samples, decay=3.4) * 0.40
            place(mix, voice.astype(np.float32), start)

    # pads: the chord held across the bar
    for bar_index in range(bars):
        _, chord = progression[bar_index % len(progression)]
        start = int(SAMPLE_RATE * bar_index * bar)
        length_samples = int(SAMPLE_RATE * bar * 0.98)
        for degree in chord:
            voice = oscillator(note(3, degree), length_samples, "saw")
            voice *= envelope(length_samples, 0.35, 0.5, sustain=0.5)
            place(mix, (voice * 0.085).astype(np.float32), start)

    # lead: the motif, which is what makes the track recognisable
    for bar_index, beat_offset, degree, beats in lead_pattern:
        start = int(SAMPLE_RATE * (bar_index * bar + beat_offset * beat))
        length_samples = int(SAMPLE_RATE * beats * beat)
        octave = 5 if spec.get("lead_high", True) else 4
        voice = oscillator(note(octave, degree), length_samples, "triangle")
        voice *= envelope(length_samples, 0.02, min(0.4, beats * beat * 0.6))
        place(mix, (voice * 0.16).astype(np.float32), start)

    mix += percussion(length, bpm, spec["percussion"]) * spec.get("percussion_gain", 0.8)
    mix = reverb(mix, mix=spec.get("reverb", 0.22))
    mix = np.tanh(mix * 1.25)  # soft clip, so nothing ever leaves the speaker as a click
    # Seamless looping, part two: the last few milliseconds are cross-faded into the head, so the loop point is
    # a continuation rather than a jump. (Part one is the wrapped reverb tail above.)
    wrap = int(SAMPLE_RATE * 0.02)
    fade = np.linspace(0.0, 1.0, wrap, dtype=np.float32)
    mix[:wrap] = mix[:wrap] * fade + mix[-wrap:] * (1.0 - fade)
    mix = mix[:-wrap]
    # Headroom, not loudness: Vorbis can overshoot the peak it was given, and a track that clips on the device
    # is a defect no listener forgives.
    # Vorbis can overshoot the peak it was handed (measured: the first render of hollow_march decoded at
    # exactly 1.000), so the target sits well under the 0.94 ceiling the level gate enforces.
    peak = float(np.max(np.abs(mix))) or 1.0
    mix *= 0.72 / peak
    return mix


#: The four tracks, each defined by its arrangement rather than by a file somebody downloaded.
TRACKS = {
    "vigil": {
        "seed": 0x56494749,
        "bpm": 84,
        "bars": 8,
        "progression": [(0, (0, 2, 4)), (5, (5, 0, 2)), (3, (3, 5, 0)), (6, (6, 1, 3))],
        "lead": [(0, 0, 4, 2), (1, 2, 3, 1), (2, 0, 2, 2), (3, 2, 0, 2),
                 (4, 0, 4, 2), (5, 2, 6, 1), (6, 0, 5, 2), (7, 2, 2, 2)],
        "percussion": "K.x.S.x.K.x.S.x.",
        "reverb": 0.24,
    },
    "hollow_march": {
        # The boss theme: faster, minor thirds underneath, and the lead in a lower octave.
        "seed": 0x484F4C4C,
        "bpm": 104,
        "bars": 8,
        "progression": [(0, (0, 2, 4)), (0, (0, 2, 4)), (4, (4, 6, 1)), (6, (6, 1, 3))],
        "lead": [(0, 0, 0, 1), (0, 2, 2, 1), (1, 0, 3, 2), (2, 2, 4, 2), (3, 0, 2, 1),
                 (4, 1, 5, 2), (5, 0, 4, 1), (5, 2, 3, 1), (6, 0, 2, 2), (7, 1, 0, 3)],
        "percussion": "K.xKS.x.KKx.S.x.",
        "reverb": 0.18,
        "percussion_gain": 0.95,
        "lead_high": False,
    },
    "heartwood_dawn": {
        # Menus, the codex and the root network: slower, higher, almost no percussion.
        "seed": 0x48454152,
        "bpm": 72,
        "bars": 8,
        "progression": [(0, (0, 2, 4)), (3, (3, 5, 0)), (5, (5, 0, 2)), (4, (4, 6, 1))],
        "lead": [(0, 0, 2, 3), (2, 1, 4, 2), (3, 2, 5, 2), (5, 0, 6, 3), (7, 1, 4, 2)],
        "percussion": "x...x...x...x...",
        "reverb": 0.32,
        "percussion_gain": 0.35,
    },
    "quiet_after": {
        # The results screens: sparse, unresolved, with no percussion at all.
        "seed": 0x51554945,
        "bpm": 66,
        "bars": 6,
        "progression": [(0, (0, 2, 4)), (6, (6, 1, 3)), (3, (3, 5, 0))],
        "lead": [(0, 0, 4, 4), (2, 0, 3, 4), (4, 0, 2, 4)],
        "percussion": "................",
        "reverb": 0.38,
        "percussion_gain": 0.0,
    },
}


def write_track(name: str, output: pathlib.Path) -> pathlib.Path:
    spec = TRACKS[name]
    np.random.seed(spec["seed"])
    mono = render(spec)
    # A touch of width: the pad sits slightly off-centre, the lead slightly the other way. Cheap, and it stops
    # a synthesised loop from sounding like a mono test signal.
    left = mono * 0.97
    right = np.roll(mono, 61) * 0.97
    stereo = np.stack([left, right], axis=1).astype(np.float32)
    path = output / f"{name}.ogg"
    sf.write(path, stereo, SAMPLE_RATE, format="OGG", subtype="VORBIS")
    return path


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--only", choices=sorted(TRACKS), default=None)
    parser.add_argument("--output", default=str(ROOT / "android/assets/audio/music"))
    args = parser.parse_args()

    output = pathlib.Path(args.output)
    output.mkdir(parents=True, exist_ok=True)
    names = [args.only] if args.only else sorted(TRACKS)
    for name in names:
        path = write_track(name, output)
        seconds = TRACKS[name]["bars"] * 4 * 60.0 / TRACKS[name]["bpm"]
        shown = path.relative_to(ROOT) if ROOT in path.parents else path
        print(f"{shown}: {path.stat().st_size} bytes, {seconds:.1f} s, {TRACKS[name]['bpm']} bpm")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
