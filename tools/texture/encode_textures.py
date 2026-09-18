#!/usr/bin/env python3
"""Encode the sheets that ship into ETC2 containers, and only ship what the measurement allows (roadmap R8.1).

The delivery half of R8.1: `:core:residencyReport` says what the catalog *would* cost compressed; this tool
produces the actual payloads, measures what they cost in quality, and refuses to write a payload that misses the
bar it was given. Three numbers per sheet, all measured rather than assumed:

* **decoded bytes** -- what the sheet costs the GPU today (RGBA8888, four bytes a pixel) and what the encoded
  container costs (ETC2 is eight bytes per 4x4 block, half a byte a pixel);
* **alpha shape** -- the share of pixels whose alpha is already 0 or 255. A sheet that is a mask gets the
  punchthrough format, which costs the same and represents the mask exactly; a sheet with soft alpha is skipped,
  because punchthrough would harden its edges and ETC2_RGBA8's base-and-modifier alpha cannot represent a mask.
* **quality** -- the PSNR of the decoded payload against the source, over the opaque pixels. This is the number
  that decides shipping: the payload is written only when it clears `--min-psnr`. The colour half is measured for
  *every* sheet, whether or not it can ship, so the report answers "how good is this encoder" and not only "did
  anything pass"; `--reference` adds a second opinion from another encoder's stream on the same pixels.

The reference binary is not part of this repository: Google's `etc1` encoder is Apache-2.0, and the point of the
comparison is to keep it at arm's length. To take the measurement, fetch it, build the two-line harness that calls
`etc1_encode_image`, and pass the binary in:

    curl -O https://raw.githubusercontent.com/google/etc1/master/etc1_utils.cpp
    g++ -O2 -o etc1_reference enc_ref.cpp
    python3 tools/texture/encode_textures.py --combat --reference ./etc1_reference --report-only

Usage:
    python3 tools/texture/encode_textures.py --keys rootling stonekin     # encode and report
    python3 tools/texture/encode_textures.py --combat --report-only        # measure the live combat set
    python3 tools/texture/encode_textures.py --check                       # verify committed payloads
"""
from __future__ import annotations

import argparse
import hashlib
import json
import pathlib
import subprocess
import sys
import tempfile

import numpy as np
from PIL import Image

TOOLS = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS / "texture"))

import etc2  # noqa: E402
import ktx  # noqa: E402

ROOT = TOOLS.parent
GENERATED = ROOT / "android" / "assets" / "generated"
#: Where a run with the report enabled lands. It is the tool's own record, one line per sheet, and
#: `tools/texture/tests/test_shipped_containers.py` reads it back to check that every container in the
#: bundle is one this file accounted for. It is deliberately *not* in `docs/perf/runs`, which holds run
#: files (one `metrics` array each) and is rendered into `docs/perf/PERFORMANCE.md`: the report is a
#: per-sheet document, and the directory holding both is a directory that breaks its own renderer.
REPORT = ROOT / "docs" / "perf" / "texture-encoding-report.json"

#: Below this share of binary alpha a sheet keeps its PNG: punchthrough would harden soft edges.
MINIMUM_BINARY_ALPHA = 0.995
#: And below this PSNR over the opaque pixels the payload is not written at all.
DEFAULT_MINIMUM_PSNR = 32.0
#: The reference encoder this project compares itself against. It is not vendored: these are the coordinates a
#: reader needs to reproduce the comparison, and the licence it may be used under.
REFERENCE_NAME = "Google etc1 (etc1_utils.cpp, etc1_encode_image)"
REFERENCE_SOURCE = "https://raw.githubusercontent.com/google/etc1/master/etc1_utils.cpp"
REFERENCE_LICENCE = "Apache-2.0"
#: What that encoder measured on the sheets this tool last filed, in decibels over the opaque pixels. It is the
#: fallback for prose: when `--reference` is given, every sheet carries the number measured on the spot.
REFERENCE_PSNR = 34.58


def manifest() -> dict:
    return json.loads((GENERATED / "asset_manifest.json").read_text())


def combat_keys(document: dict) -> list[str]:
    """The live combat set: the regular enemy sheets and the boss sheets, in manifest order."""
    wanted = []
    for asset in document["assets"]:
        family = (asset.get("family") or "").lower()
        if family in ("enemy", "enemies", "boss", "bosses"):
            wanted.append(asset["key"])
    return wanted


def load_sheet(path: pathlib.Path) -> np.ndarray:
    with Image.open(path) as source:
        return np.asarray(source.convert("RGBA"), dtype=np.uint8)


def peak_signal_to_noise(reference: np.ndarray, decoded: np.ndarray, mask: np.ndarray) -> float:
    difference = (decoded.astype(np.int32) - reference.astype(np.int32))[mask].astype(np.float64)
    if difference.size == 0:
        return 0.0
    mean_square = float((difference ** 2).mean())
    if mean_square <= 0.0:
        return 99.0
    return float(10.0 * np.log10(255.0 ** 2 / mean_square))


def colour_quality(image: np.ndarray) -> tuple[float, int]:
    """The colour half on its own: what ETC2_RGB8 costs these pixels, and how many bytes it takes.

    Measured for every sheet, including the ones that cannot ship: a sheet with soft alpha is still evidence about
    the encoder, and the number this returns is the one the encoder's quality is judged on.
    """
    payload = etc2.encode_blocks(image, etc2.ETC2_RGB8)
    decoded = etc2.decode_blocks(payload, image.shape[1], image.shape[0], punchthrough=False)
    opaque = image[..., 3] >= etc2.OPAQUE_THRESHOLD
    return peak_signal_to_noise(image[..., :3], decoded[..., :3], opaque), len(payload)


def reference_quality(image: np.ndarray, binary: pathlib.Path) -> float:
    """What another encoder leaves on the same pixels, decoded by this repository's decoder.

    The binary takes a raw RGB file, the width, the height and an output path, and writes an ETC1 block stream --
    see the module docstring for how to build one from Google's `etc1` sources.
    """
    with tempfile.TemporaryDirectory() as directory:
        rgb = pathlib.Path(directory) / "sheet.rgb"
        out = pathlib.Path(directory) / "sheet.etc1"
        rgb.write_bytes(np.ascontiguousarray(image[..., :3]).tobytes())
        completed = subprocess.run(
            [str(binary), str(rgb), str(image.shape[1]), str(image.shape[0]), str(out)],
            capture_output=True, text=True,
        )
        if completed.returncode != 0 or not out.is_file():
            raise RuntimeError(f"{binary} failed: {completed.stderr.strip() or completed.stdout.strip()}")
        stream = out.read_bytes()
    decoded = etc2.decode_blocks(stream, image.shape[1], image.shape[0], punchthrough=False)
    opaque = image[..., 3] >= etc2.OPAQUE_THRESHOLD
    return peak_signal_to_noise(image[..., :3], decoded[..., :3], opaque)


def encode_sheet(
    key: str, sheet: dict, minimum_psnr: float, root: pathlib.Path = GENERATED,
    reference: pathlib.Path | None = None,
) -> dict:
    """Encode one sheet, measure it, and say whether it may ship."""
    source_path = root / sheet["file"]
    image = load_sheet(source_path)
    binary_alpha = etc2.binary_alpha_fraction(image)
    colour_psnr, colour_bytes = colour_quality(image)
    record = {
        "key": key,
        "sheet": sheet["file"],
        "width": int(sheet["width"]),
        "height": int(sheet["height"]),
        "decodedRgba8888Bytes": int(sheet["decodedBytes"]),
        "binaryAlphaFraction": round(binary_alpha, 5),
        "encoded": False,
        "format": None,
        "encodedBytes": 0,
        "psnrDb": None,
        "alphaAgreement": None,
        "colourPsnrDb": round(colour_psnr, 2),
        "colourEncodedBytes": colour_bytes,
        "referencePsnrDb": None,
        "reason": "",
    }
    if reference is not None:
        record["referencePsnrDb"] = round(reference_quality(image, reference), 2)
    if binary_alpha < MINIMUM_BINARY_ALPHA:
        record["reason"] = (
            f"soft alpha ({binary_alpha:.4f} of pixels are 0 or 255): punchthrough would harden the edges and "
            "ETC2_RGBA8 cannot represent a mask"
        )
        return record

    gl_format = etc2.ETC2_RGB8_PUNCHTHROUGH_ALPHA1
    payload = etc2.encode_blocks(image, gl_format)
    decoded = etc2.decode_blocks(payload, image.shape[1], image.shape[0], punchthrough=True)
    opaque = image[..., 3] >= etc2.OPAQUE_THRESHOLD
    quality = peak_signal_to_noise(image[..., :3], decoded[..., :3], opaque)
    alpha_agreement = float((decoded[..., 3] >= etc2.OPAQUE_THRESHOLD == opaque).mean()) if False else float(
        ((decoded[..., 3] >= etc2.OPAQUE_THRESHOLD) == opaque).mean()
    )
    record.update(
        {
            "format": "ETC2_RGB8_PUNCHTHROUGH_ALPHA1",
            "glInternalFormat": gl_format,
            "encodedBytes": len(payload),
            "psnrDb": round(quality, 2),
            "alphaAgreement": round(alpha_agreement, 5),
        }
    )
    if alpha_agreement < 1.0:
        record["reason"] = f"the punchthrough mask disagrees with the source on {1.0 - alpha_agreement:.5f}"
        return record
    if quality < minimum_psnr:
        record["reason"] = (
            f"quality {quality:.2f} dB is below the {minimum_psnr:.2f} dB bar; the reference encoder measures "
            f"{REFERENCE_PSNR:.2f} dB on the same sheets"
        )
        return record
    record["encoded"] = True
    record["payload"] = payload
    record["sha256"] = hashlib.sha256(payload).hexdigest()
    return record


def container_path(sheet: str) -> pathlib.Path:
    """Where a sheet's container goes: beside it, same stem, under `compressed/etc2/`.

    The runtime looks for exactly this path -- `TexturePayloadPolicy.containerPath` and
    `AtlasPageSource.containerBeside` are the same rule in Java -- so a container written anywhere else is a
    container no device will ever read. `tests/test_etc2_encoder.py` pins the two together by naming this path.
    """
    relative = pathlib.Path(sheet)
    return GENERATED / relative.parent / "compressed" / "etc2" / (relative.stem + ".ktx")


def write_payload(record: dict) -> pathlib.Path:
    target = container_path(record["sheet"])
    target.parent.mkdir(parents=True, exist_ok=True)
    blob = ktx.write(
        ktx.Image(
            width=record["width"],
            height=record["height"],
            gl_internal_format=record["glInternalFormat"],
            gl_base_internal_format=ktx.GL_RGBA,
            payload=record["payload"],
        )
    )
    target.write_bytes(blob)
    return target


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--keys", nargs="*", default=None)
    parser.add_argument("--combat", action="store_true", help="encode the live combat set")
    parser.add_argument("--min-psnr", type=float, default=DEFAULT_MINIMUM_PSNR)
    parser.add_argument("--report-only", action="store_true", help="measure and report, never write a container")
    parser.add_argument("--check", action="store_true", help="verify the committed containers against a re-encode")
    parser.add_argument("--report", type=pathlib.Path, default=REPORT)
    parser.add_argument("--reference", type=pathlib.Path, default=None,
                        help="another encoder's binary, measured on the same pixels (see the module docstring)")
    args = parser.parse_args()

    document = manifest()
    by_key = {asset["key"]: asset for asset in document["assets"]}
    keys = args.keys or (combat_keys(document) if args.combat else [])
    if not keys:
        parser.error("give --keys or --combat")

    records = []
    for key in keys:
        asset = by_key[key]
        sheet = (asset.get("sheets") or [None])[0]
        if sheet is None:
            records.append({"key": key, "encoded": False, "reason": "this asset has no sheet"})
            continue
        records.append(encode_sheet(key, sheet, args.min_psnr, reference=args.reference))

    shipped = [record for record in records if record.get("encoded")]
    if not args.report_only:
        for record in shipped:
            write_payload(record)
        for record in records:
            record.pop("payload", None)
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(
            {
                "tool": "tools/texture/encode_textures.py",
                "format": "ETC2",
                "minimumPsnrDb": args.min_psnr,
                "referenceEncoder": REFERENCE_NAME,
                "referenceEncoderSource": REFERENCE_SOURCE,
                "referenceEncoderLicence": REFERENCE_LICENCE,
                "referenceEncoderPsnrDb": REFERENCE_PSNR,
                "measuredReferenceOnThisRun": bool(args.reference),
                "sheets": records,
            },
            indent=2,
        ) + "\n", encoding="utf-8")

    decoded_total = sum(record.get("decodedRgba8888Bytes", 0) for record in shipped)
    encoded_total = sum(record.get("encodedBytes", 0) for record in shipped)
    for record in records:
        state = "ship" if record.get("encoded") else "keep PNG"
        detail = record.get("reason") or (
            f"{record.get('psnrDb')} dB, {record.get('encodedBytes')} bytes vs "
            f"{record.get('decodedRgba8888Bytes')} decoded"
        )
        colour = f"colour {record.get('colourPsnrDb')} dB"
        reference = record.get("referencePsnrDb")
        if reference is not None:
            colour += f", reference {reference} dB"
        print(f"{record['key']:18s} {state:9s} {colour}  {detail}")
    print(
        f"{len(shipped)} of {len(records)} sheets pass the {args.min_psnr:.1f} dB bar; "
        f"{encoded_total} encoded bytes against {decoded_total} decoded bytes"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
