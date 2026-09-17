#!/usr/bin/env python3
"""Reassemble the pixels a device test logged, so a GPU decode survives the uninstall.

`connectedDebugAndroidTest` uninstalls both APKs when it finishes and the emulator is stopped right after the
script returns, so a file the test wrote in the app's own storage is gone by the time anything can pull it.
logcat is the one channel that outlives both, and the smoke script already captures it into the reports
artifact, so the texture test logs the decode it produced as base64 with its length and its sha256, and this
tool puts it back together:

    python3 tools/texture/decode_device_evidence.py <logcat-file> <output-directory>

It writes `<name>.rgba` per logged buffer -- and `<name>.png` as well when Pillow is available, because a
picture is what a person looks at. A buffer whose reassembled bytes do not match the logged sha256 is left
unwritten with a line saying so: evidence that cannot be trusted is worse than no evidence.
"""
from __future__ import annotations

import base64
import hashlib
import pathlib
import sys

TAG = "HERODEFENSE_TEXTURE_PIXELS"


def reassemble(text: str) -> dict[str, bytes]:
    headers: dict[str, int] = {}
    chunks: dict[str, list[str]] = {}
    digests: dict[str, str] = {}
    for line in text.splitlines():
        if TAG not in line:
            continue
        # logcat writes `I HERODEFENSE_TEXTURE_PIXELS: <body>`, so the tag is followed by a colon that the
        # split leaves in front of the body -- and the base64 alphabet cannot contain one, so stripping it is
        # safe for the chunk lines as well.
        body = line.split(TAG, 1)[1].lstrip(": \t").strip()
        if not body:
            continue
        name, _, rest = body.partition(" ")
        rest = rest.strip()
        if rest.startswith("bytes="):
            parts = dict(piece.split("=", 1) for piece in rest.split() if "=" in piece)
            headers[name] = int(parts["bytes"])
            digests[name] = parts.get("sha256", "")
        elif rest == "end":
            continue
        elif rest:
            chunks.setdefault(name, []).append(rest)
    buffers: dict[str, bytes] = {}
    for name, expected in headers.items():
        joined = "".join(chunks.get(name, []))
        try:
            payload = base64.b64decode(joined)
        except ValueError as error:
            print(f"{name}: the logged base64 does not decode ({error})")
            continue
        if len(payload) != expected:
            print(f"{name}: logged {expected} bytes, reassembled {len(payload)}")
            continue
        digest = hashlib.sha256(payload).hexdigest()
        if digests.get(name) and digest != digests[name]:
            print(f"{name}: the reassembled bytes do not match the logged sha256")
            continue
        buffers[name] = payload
    return buffers


def write(buffers: dict[str, bytes], output: pathlib.Path) -> None:
    output.mkdir(parents=True, exist_ok=True)
    for name, payload in buffers.items():
        (output / f"{name}.rgba").write_bytes(payload)
        print(f"{name}.rgba: {len(payload)} bytes")
        try:
            from PIL import Image
        except ImportError:
            continue
        side = int(len(payload) ** 0.5 // 4)
        if side * side * 4 != len(payload):
            print(f"{name}: {len(payload)} bytes is not a square RGBA image")
            continue
        image = Image.frombytes("RGBA", (side, side), payload)
        image.save(output / f"{name}.png", optimize=True)
        print(f"{name}.png: {side}x{side}")


def main(argv: list[str]) -> int:
    if len(argv) != 3:
        print(__doc__.strip().splitlines()[-1])
        return 2
    source = pathlib.Path(argv[1])
    if not source.is_file():
        print(f"no capture at {source}")
        return 0
    buffers = reassemble(source.read_text(encoding="utf-8", errors="ignore"))
    if not buffers:
        print(f"the capture carries no {TAG} buffers")
        return 0
    write(buffers, pathlib.Path(argv[2]))
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
