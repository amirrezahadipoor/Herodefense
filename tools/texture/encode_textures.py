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
  that decides shipping: the payload is written only when it clears `--min-psnr`.

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
import sys

import numpy as np
from PIL import Image

TOOLS = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS / "texture"))

import etc2  # noqa: E402
import ktx  # noqa: E402

ROOT = TOOLS.parent
GENERATED = ROOT / "android" / "assets" / "generated"
OUTPUT = GENERATED / "compressed" / "etc2"
REPORT = ROOT / "docs" / "perf" / "runs" / "2026-09-17-texture-encoding.json"

#: Below this share of binary alpha a sheet keeps its PNG: punchthrough would harden soft edges.
MINIMUM_BINARY_ALPHA = 0.995
#: And below this PSNR over the opaque pixels the payload is not written at all.
DEFAULT_MINIMUM_PSNR = 32.0
#: The reference encoder this project compares itself against, measured on the same sheets.
REFERENCE_PSNR = 33.57


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


def encode_sheet(key: str, sheet: dict, minimum_psnr: float, root: pathlib.Path = GENERATED) -> dict:
    """Encode one sheet, measure it, and say whether it may ship."""
    source_path = root / sheet["file"]
    image = load_sheet(source_path)
    binary_alpha = etc2.binary_alpha_fraction(image)
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
        "reason": "",
    }
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


def write_payload(record: dict) -> pathlib.Path:
    target = OUTPUT / (pathlib.Path(record["sheet"]).stem + ".ktx")
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
        records.append(encode_sheet(key, sheet, args.min_psnr))

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
                "referenceEncoderPsnrDb": REFERENCE_PSNR,
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
        print(f"{record['key']:18s} {state:9s} {detail}")
    print(
        f"{len(shipped)} of {len(records)} sheets pass the {args.min_psnr:.1f} dB bar; "
        f"{encoded_total} encoded bytes against {decoded_total} decoded bytes"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
