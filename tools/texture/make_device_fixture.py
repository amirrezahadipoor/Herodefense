#!/usr/bin/env python3
"""Build the compressed texture the *device* test uploads (roadmap R8.1, the evidence half).

The encoder tool (`encode_textures.py`) refuses to write a container for any sheet that does not clear its
quality bar, and today that is every sheet that ships: the payload stays PNG. That rule is about the game's
payload, and it left the upload path itself unproven -- nothing in the repository had ever handed an ETC2
container to a GPU.

This script builds the one file that closes that gap and nothing else. It crops a fixed 64x64 square out of a
real shipped sheet, encodes it with the repository's own ETC2 encoder in the punchthrough alpha mode, decodes
the result back with the repository's own decoder, and writes three files into the instrumentation assets:

    <name>.ktx    the KTX v1 container, exactly the bytes a device would be handed
    <name>.rgba   what the decoder says those bytes mean, row-major from the top, RGBA8888
    <name>.json   the provenance: source sheet and its hash, crop, mode, payload and expectation hashes

The device test uploads the container through EGL/GLES3 and compares what the GPU produced with `<name>.rgba`,
so the claim "a compressed page uploads and decodes on the device" is checked against this repository's own
idea of what the bytes are, not against a picture of them. The fixture is deliberately *not* a candidate
payload and is never read by the game: it lives under the instrumentation APK's assets, it is 64x64, and the
JSON says so.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import pathlib
import sys

import numpy as np
from PIL import Image

TOOLS = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS / "texture"))

import etc2  # noqa: E402
import ktx  # noqa: E402

ROOT = TOOLS.parent
GENERATED = ROOT / "android" / "assets" / "generated"
FIXTURES = ROOT / "android" / "src" / "androidTest" / "assets" / "etc2"

#: The sheet the crop comes from, and the square it takes: fixed so the committed bytes are reproducible.
#: The window is the busiest 64x64 square of that sheet (1626 distinct colours, 94 % of it opaque), because a
#: flat or empty crop would exercise the upload path without ever asking a GPU to decode anything.
SOURCE_KEY = "rootling"
CROP_X = 640
CROP_Y = 256
SIZE = 64
#: Punchthrough alpha: the mode whose alpha bit and whose transparent-pixel colour the GPU has to agree with.
GL_INTERNAL_FORMAT = etc2.ETC2_RGB8_PUNCHTHROUGH_ALPHA1
NAME = f"{SOURCE_KEY}-{SIZE}"


def source_sheet() -> tuple[pathlib.Path, dict]:
    document = json.loads((GENERATED / "asset_manifest.json").read_text())
    for asset in document["assets"]:
        if asset["key"] == SOURCE_KEY:
            sheet = asset["sheets"][0]
            return GENERATED / sheet["file"], sheet
    raise SystemExit(f"no asset keyed {SOURCE_KEY!r} in the manifest")


def build() -> dict[str, bytes]:
    """Encode the crop and return the three files, without touching the disk."""
    sheet_path, sheet = source_sheet()
    with Image.open(sheet_path) as source:
        full = np.asarray(source.convert("RGBA"), dtype=np.uint8)
    crop = full[CROP_Y:CROP_Y + SIZE, CROP_X:CROP_X + SIZE].copy()
    payload = etc2.encode_blocks(crop, GL_INTERNAL_FORMAT)
    decoded = etc2.decode_blocks(payload, SIZE, SIZE, punchthrough=True)
    difference = decoded.astype(np.int32) - crop.astype(np.int32)
    mean_square = float((difference.astype(np.float64) ** 2).mean())
    psnr = 99.0 if mean_square <= 0.0 else float(10.0 * np.log10(255.0 ** 2 / mean_square))
    document = {
        "name": NAME,
        "purpose": "device evidence for R8.1: the upload and decode path, not a payload the game reads",
        "sourceKey": SOURCE_KEY,
        "sourceSheet": str(sheet_path.relative_to(ROOT)),
        "sourceSheetSha256": hashlib.sha256(sheet_path.read_bytes()).hexdigest(),
        "sourceSheetSize": [sheet["width"], sheet["height"]],
        "crop": {"x": CROP_X, "y": CROP_Y, "width": SIZE, "height": SIZE},
        "glInternalFormat": GL_INTERNAL_FORMAT,
        "container": f"{NAME}.ktx",
        "expectation": f"{NAME}.rgba",
        "roundTripPsnr": round(psnr, 2),
        "containerSha256": hashlib.sha256(
            ktx.write(ktx.Image(SIZE, SIZE, GL_INTERNAL_FORMAT, ktx.GL_RGBA, payload))).hexdigest(),
        "expectationSha256": hashlib.sha256(decoded.tobytes()).hexdigest(),
    }
    return {
        f"{NAME}.ktx": ktx.write(ktx.Image(SIZE, SIZE, GL_INTERNAL_FORMAT, ktx.GL_RGBA, payload)),
        f"{NAME}.rgba": decoded.tobytes(),
        f"{NAME}.json": (json.dumps(document, indent=2, sort_keys=True) + "\n").encode(),
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true", help="fail if the committed fixture differs from a rebuild")
    parser.add_argument("--write", action="store_true", help="write the fixture into the instrumentation assets")
    args = parser.parse_args()
    files = build()
    if args.check:
        problems = []
        for name, expected in files.items():
            path = FIXTURES / name
            if not path.exists():
                problems.append(f"{name} is missing")
            elif path.read_bytes() != expected:
                problems.append(f"{name} differs from a rebuild ({path.stat().st_size} bytes on disk)")
        if problems:
            for problem in problems:
                print(f"FAIL {problem}")
            return 1
        print(f"fixture {NAME} matches a rebuild: {len(files['%s.ktx' % NAME])} container bytes, "
              f"{len(files['%s.rgba' % NAME])} expected pixel bytes, "
              f"{json.loads(files['%s.json' % NAME])['roundTripPsnr']} dB round trip")
        return 0
    if args.write:
        FIXTURES.mkdir(parents=True, exist_ok=True)
        for name, blob in files.items():
            (FIXTURES / name).write_bytes(blob)
            print(f"wrote {name} ({len(blob)} bytes)")
        return 0
    parser.print_help()
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
